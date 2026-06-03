/***********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Author:       s.coletta@ads.it                                      *
 * Company:      Finmatica S.p.A.                                      *
 * Organization: Associazione ERP Open Source Italia                   *
 **********************************************************************/
package it.finmatica.history.records.sql;

import java.util.ArrayList;
import java.util.List;

import it.finmatica.history.records.extension.ReplacerStats;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;

/** Process and replace table with history-enabled equivalent 
 * 
 * @author s.coletta@ads.it
 *
 */
public abstract class HistoryReplacer
{	
	/** Check if the input table is time-machine enabled 
	 * 
	 * @param fromTable table to check
	 * @return true if histroy is enabled
	 */
	public abstract boolean hasHistory(FromTable fromTable);
	
	/** Generates the replaced table (subquery or HST table)
	 * 
	 * @param fromTable	table
	 * @param dateSource	date source to use when generating sub-queries
	 * @param isInsideFrom is called inside the top-level from
	 * @return the new table (or 'virtual' table)
	 */
	public abstract String getReplacedTable(FromTable fromTable, String dateSource, boolean isInsideFrom);
	
	/** Generate where expression to be added to the main 'where'
	 * 
	 * @param fromTable table
	 * @param isInnerJoin is equivalent to an inner join ?
	 * @param dateSource	date source to use when generating sub-queries
	 * @param isInsideFrom is called inside the top-level from
	 * @return the where clause, null if nothing has to be added
	 */
	public abstract String getWhereExpression(FromTable fromTable,boolean isInnerJoin, String dateSource, boolean isInsideFrom);
		
	/** Process query replacements
	 * 
	 * @param stmt parsed statemente
	 * @param dateSource date source as a string, can be null, if so its a 'dry run' (evalute query but does not replace, needed to keep the cache coherent)
	 * @param stats stats
	 * @throws JSQLParserException
	 */
	public void process(Statement stmt,String dateSource,ReplacerStats stats) throws JSQLParserException
	{
		if(stmt instanceof Select)
		{
			Select select = (Select)stmt;
			
			if(select.getSelectBody() instanceof SetOperationList)
			{
				SetOperationList sol = (SetOperationList)select.getSelectBody();
				
				if(sol.getSelects() != null)
				{
					for(SelectBody selectBody:sol.getSelects())
					{
						processSelect(selectBody, dateSource, true, false, stats);
					}
				}				
			}
			else
			{
				processSelect(select.getSelectBody(), dateSource, true, false,stats);
			}
		}			
	}
	
	/** Process a single select
	 * 
	 * @param sb						select body
	 * @param dateSource 		dateSource date source as a string, can be null, if so its a 'dry run' (evalute query but does not replace)
	 * @param topLevel			true if its a top level query
	 * @param bCalledInFrom	true if its called in a top-level 'from' clause 
	 * @param stats					statistics
	 * @throws JSQLParserException
	 */
	private void processSelect(SelectBody sb, String dateSource,final boolean topLevel,final boolean bCalledInFrom,ReplacerStats stats) throws JSQLParserException
	{
		if(sb instanceof PlainSelect)
		{
			PlainSelect ps = (PlainSelect)sb;
			
			// Parse select subqueries
			
			List<SelectItem> selectItems = ps.getSelectItems();
			
			for(SelectItem si:selectItems)
			{
				if(si instanceof SelectExpressionItem)
				{
					SelectExpressionItem sei = (SelectExpressionItem)si;
					
					if(sei.getExpression() instanceof SubSelect)
					{
						SubSelect ss = (SubSelect)sei.getExpression();
						processSelect(ss.getSelectBody(), dateSource, false,
								(bCalledInFrom && topLevel == false), stats); // Arrivo da una from, e non e' di primo livello	
					}
				}				
			}
			
			// Parse where (prima delle join per evitare di ri-parsare una condizione appena generata)
			
			Expression whereExpression = ps.getWhere();

			if(whereExpression != null)
			{
				WhereClauseVisitor selectVisitor = new WhereClauseVisitor(dateSource, topLevel, bCalledInFrom, stats);				
				whereExpression.accept(selectVisitor);
				
				ArrayList<JSQLParserException> visitExceptions = selectVisitor.getVisitExceptions();
								
				if(visitExceptions.size() > 0)
				{
					JSQLParserException e = visitExceptions.get(0);
					throw e;
				}
			}
			
			// From
			
			FromItem fi = ps.getFromItem();
			boolean bEffectiveFrom = bCalledInFrom;
			
			if(topLevel == true) // Se sono al primo livello, allora questa e' la madre di tutte le from
				bEffectiveFrom = true;
			
			if(fi != null && fi instanceof Table)
			{
				Table table = (Table)fi;
				FromTable ft = FromTable.fromTable(table);
				
				ReplacementInfo repInfo = getReplacementInfo(ft, true, dateSource, bEffectiveFrom, stats); // Singola tabella puo considerarsi nel caso base 
				
				if(repInfo != null)
				{
					ps.setFromItem(repInfo.fromItem);
					addToWhere(ps,repInfo);
				}				
			}
			else if( fi instanceof SubSelect)
			{
				SubSelect ss = (SubSelect)fi;
				processSelect(ss.getSelectBody(), dateSource, false, bEffectiveFrom, stats);				
			}
			
			// Join
			
			List<Join> joins = ps.getJoins();
			
			if(joins != null)
			{
				for(Join join:joins)
				{
					FromItem ri = join.getRightItem();
							
					if(ri instanceof Table)
					{
						Table 					table = (Table)ri;
						FromTable 			ft = FromTable.fromTable(table);
						
						// Il 'simple join' (Tabelle 'libere') viene trattato come un inner join
						
						boolean bInnerEquivalent = join.isInner() || join.isSimple();
						
						ReplacementInfo repInfo = getReplacementInfo(ft,bInnerEquivalent, dateSource, bEffectiveFrom, stats); 
						
						if(repInfo != null)
						{
							join.setRightItem(repInfo.fromItem);
							
							if(repInfo.whereExpression != null)
							{
								if(bInnerEquivalent)
								{
									addToWhere(ps,repInfo);
								}
								else
								{
									if(join.getOnExpression() != null)
									{
										AndExpression joinOnAnd = new AndExpression(join.getOnExpression(), 
												repInfo.whereExpression);
										
										join.setOnExpression(joinOnAnd);
									}
									else
									{
										join.setOnExpression(repInfo.whereExpression);
									}
								}
							}
						}						
					}
					else if( ri instanceof SubSelect)
					{
						SubSelect ss = (SubSelect)ri;
						processSelect(ss.getSelectBody(), dateSource,  false, bEffectiveFrom, stats);		
					}							
				}
			}
		}
	}
	
	/** Add to the where clause
	 * @param ps			select on wich to add the expression
	 * @param repInfo	Replacement info
	 */
	private void addToWhere(PlainSelect ps,ReplacementInfo repInfo)
	{
		if(repInfo.whereExpression != null)
		{
			Expression currentExpr = ps.getWhere();
			Expression newExpr = null;
			
			if(currentExpr != null)
			{
				newExpr = new AndExpression(currentExpr, 
						repInfo.whereExpression);
			}
			else
			{
				newExpr = repInfo.whereExpression;
			}
			
			ps.setWhere(newExpr);
		}
	}
	
	/** Get replacement info
	 * 
	 * @param fromTable		table
	 * @param isInnerJoin	equivalent to inner join ?
	 * @param dateSource date source as a string, can be null, if so its a 'dry run' (evaluate query but does not replace, needed to keep the cache coherent)
	 * @param isInsideFrom
	 * @param stats
	 * @return
	 * @throws JSQLParserException
	 */
	private ReplacementInfo getReplacementInfo(FromTable fromTable, boolean isInnerJoin, String dateSource,boolean isInsideFrom, ReplacerStats stats) throws JSQLParserException
	{
		FromItem replacementItem = null;
		Expression whereExpression = null;
		
		if(hasHistory(fromTable))
		{
			stats.historyEnabledTables++;
			
			if(dateSource != null) // Null datesource = dry run
			{
				String replacement = getReplacedTable(fromTable,dateSource, isInsideFrom);
				
				if(replacement.toLowerCase().startsWith("select"))
				{
					Select newSelect = (Select)CCJSqlParserUtil.parse(replacement);
					SubSelect newSub = new SubSelect();
					newSub.setSelectBody(newSelect.getSelectBody());
					
					replacementItem = newSub;
				}
				else
				{
					replacementItem = new Table(replacement);
				}
				
				String whereExpr = getWhereExpression(fromTable, isInnerJoin, dateSource, isInsideFrom);
				
				if(whereExpr != null)
				{
					whereExpression = CCJSqlParserUtil.parseCondExpression(whereExpr);
				}
			}
		}
		
		if(replacementItem != null)
		{
			Alias alias = new Alias(fromTable.getEffectiveName(),false);
			replacementItem.setAlias(alias);
		}
		
		ReplacementInfo rc = null;
		
		if(replacementItem != null)
		{
			rc = new ReplacementInfo();
			rc.fromItem = replacementItem;
			rc.whereExpression = whereExpression;
		}
		
		return rc;
	}
	
	/**  Simple replecement data container
	 * 
	 * @author s.coletta@ads.it
	 *
	 */
	public class ReplacementInfo
	{
		Expression whereExpression;
		FromItem	fromItem;
	}
	
	/** Visitor to parse every sub-select inside a where clause 
	 * 
	 * @author s.coletta@ads.it
	 *
	 */
	private class WhereClauseVisitor extends ExpressionVisitorAdapter
	{
		private boolean bFrom = false;
		private boolean bTopLevel = false;
		private String	dateSource = null;
		private ReplacerStats stats = null;
		private ArrayList<JSQLParserException> visitExceptions = new ArrayList<>();
		
		public WhereClauseVisitor(String dateSource, boolean bTopLevel, boolean bFrom, ReplacerStats stats)
		{
			this.dateSource = dateSource;
			this.bFrom = bFrom;
			this.bTopLevel = bTopLevel;
			this.stats = stats;
		}			

		@Override
		public void visit(SelectExpressionItem selectExpressionItem)
		{
			if(selectExpressionItem.getExpression() instanceof SubSelect)
			{
				SubSelect ss = (SubSelect)selectExpressionItem.getExpression();
				process(ss);
			}
		}
		
		@Override
		public void visit(SubSelect subSelect)
		{
			process(subSelect);
		}
		
		private void process(SubSelect ss)
		{
			try
			{
				processSelect(ss.getSelectBody(), dateSource, false,
					(bFrom && bTopLevel == false), stats); // Arrivo da una from, e non e' di primo livello
			}
			catch(JSQLParserException e)
			{
				visitExceptions.add(e);
			}
		}


		public ArrayList<JSQLParserException> getVisitExceptions()
		{
			return visitExceptions;
		}

		/* Copied from ExpressionVisitAdapter with added null check
		 * 
		 * (non-Javadoc)
		 * @see net.sf.jsqlparser.expression.ExpressionVisitorAdapter#visit(net.sf.jsqlparser.expression.CaseExpression)
		 */
		@Override
	    public void visit(CaseExpression expr) 
		{
			if(expr.getSwitchExpression() != null)
			{
				expr.getSwitchExpression().accept(this);
			}
			
			if(expr.getWhenClauses() != null)
			{
	        for (Expression x : expr.getWhenClauses()) {
	            x.accept(this);
	        }
			}
			
			if(expr.getElseExpression() != null)
			{
	        expr.getElseExpression().accept(this);
			}
		}
	
	}


}
