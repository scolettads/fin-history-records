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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.db.Database;
import it.finmatica.history.records.internal.compiere.history.DateSourceUtil;
import it.finmatica.history.records.internal.compiere.history.HistorySelectionData;
import it.finmatica.history.records.internal.compiere.history.TableAndField;
import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import it.finmatica.history.records.extension.ReplacerStats;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;

/** Parse sql statements for history replacement  
 * 
 * @author s.coletta@ads.it
 *
 */
public class HistorySQLParser
{
	private static final CLogger log = CLogger.getCLogger(HistorySQLParser.class);
	
	// Copy from DB_PostgreSQL.NATIVE_MARKER (not exported)
	
	public static final String PG_NATIVE_MARKER = "NATIVE_"+Database.DB_POSTGRESQL+"_KEYWORK";
	
	private static final CCache<String, String> cacheSQL = new CCache<>("HistorySQLParser_SQLCache", 5000, 0);
	
	private String statement;
	private List<FromTable>	dateSourceTables = new ArrayList<>();
	private HistorySelectionData	historySelectionData;
	private ReplacerStats stats;

	/** Create a parser for a given statement and history selection data
	 * 
	 * @param stmt 	statement to parse
	 * @param hsd		history selection data
	 * @param stats statistics
	 */
	public HistorySQLParser(String stmt, HistorySelectionData hsd, ReplacerStats stats)
	{
		historySelectionData = hsd;
		statement = stmt;
		stats.cacheSize = cacheSQL.size();
		this.stats = stats;
	}
	
	/** Convert a date to a string
	 * 
	 * @param ts date
	 * @return string equivalent
	 */
	public String timestampToString(Timestamp ts)
	{
		return DB.TO_DATE(historySelectionData.getHistoryDate(), false);
	}
	
	/** Process the query using the specified date source resolver and replacer
	 * 
	 * @param replacer replacer
	 * @param resolver resolver
	 * @return the replaced query, or null if nothing was replaced
	 * 
	 * @throws JSQLParserException
	 */
	public String process(HistoryReplacer replacer, DateSourceResolver resolver) throws JSQLParserException
	{
		String dateSource = null;
		
		if(cacheSQL.containsKey(statement))
		{
			stats.foundInCache = true;
			return null;
		}
		
		if(log.isLoggable(Level.FINEST))
		{
			log.finest("Parsing statement:" + statement);
		}
		
		if(log.isLoggable(Level.FINE))
		{
			if(historySelectionData != null)
			{
				log.fine("Using HistorySelectionData:" + historySelectionData.toString());
			}
			else
			{
				log.fine("Using NULL HistorySelectionData, will be searched for");
			}
		}
		
		if(historySelectionData != null)
		{
			if(historySelectionData.getHistoryDate() != null)
			{			
				dateSource = timestampToString(historySelectionData.getHistoryDate());
			}
			else if(historySelectionData.getSubQuery() != null && historySelectionData.getId() > 0)
			{
				dateSource = DateSourceUtil.replaceInSubquery(historySelectionData.getSubQuery(), historySelectionData.getId());
			}
		}
		
		// PostgreSQL DB uses a custom native prefix unparsable to the sql parser. If present, we remove it, parse the query, then add it again
		
		String nativeSuffix = null;
		int suffixPos = statement.indexOf(PG_NATIVE_MARKER);
		
		if(suffixPos > 0)
		{
			nativeSuffix = statement.substring(suffixPos);
			statement = statement.substring(0, suffixPos);
		}
				
		Statement stmt = CCJSqlParserUtil.parse(statement);
		
		if(dateSource == null)
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
							if(selectBody instanceof PlainSelect)
							{
								PlainSelect ps = (PlainSelect)selectBody;
								searchForDateSourceTables(ps);
							}						
						}
					}				
				}
				else
				{
					searchForDateSourceTables(select);
				}
			}
			
			if(historySelectionData != null && historySelectionData.getTableAndField() != null)
			{
				TableAndField taf = historySelectionData.getParsedTableAndField();
				
				if(taf != null)
				{
					String table = taf.getTable().toLowerCase();
					
					for(FromTable ft:dateSourceTables)
					{
						if(ft.table.equalsIgnoreCase(table))
						{
							StringBuilder sbDateSource = new StringBuilder(ft.getEffectiveName());
							sbDateSource.append(DateSourceUtil.TABLEFIELD_SEP).append(taf.getField());
							
							dateSource = sbDateSource.toString();
							break;
						}
					}
				}
			}
		}
		
		// Search a match for subquery and table.
		
		if(dateSource == null && 
				historySelectionData != null && historySelectionData.getForTable() != null && historySelectionData.getSubQuery() != null)
		{
			for(FromTable ft:dateSourceTables)
			{
				if(ft.table.equalsIgnoreCase(historySelectionData.getForTable()))
				{
					dateSource = DateSourceUtil.replaceInSubquery(historySelectionData.getSubQuery(), ft.getEffectiveName());
					break;
				}
			}
		}
		
		// Nothing specified, search first date source table.
		
		if(dateSource == null)
		{
			for(FromTable ft:dateSourceTables)
			{
				HistorySelectionData hsd = resolver.isDateSourceTable(ft.table, Env.getAD_Client_ID(Env.getCtx())); 
				
				if(hsd != null && hsd.getSubQuery() != null)
				{
						dateSource = DateSourceUtil.replaceInSubquery(hsd.getSubQuery(), ft.getEffectiveName());
						break;
				}
			}
		}
		
		if(log.isLoggable(Level.FINE))
		{
			log.fine("Selected date source:" + dateSource);
		}
		
		String outStmt = null;
		
		// We need to process every statement to have the stats properly filled, needed to decide if the statement is cacheable 
		replacer.process(stmt,dateSource, stats);
		
		if(dateSource != null)
		{			
			outStmt = stmt.toString();
			
			// restore native suffix
			
			if(nativeSuffix != null)
				outStmt = outStmt + " " + nativeSuffix;
		}
		
		if(stats.historyEnabledTables == 0)
		{
			cacheSQL.put(statement, statement);
		}
		
		return outStmt;
	}
	
	/** Parse the query searching for potential date source tables
	 * 
	 * @param stmt parsed statement
	 */
	public void searchForDateSourceTables(Statement stmt)
	{
		PlainSelect plainSelect = null;
		
		if(stmt instanceof Select)
		{
			Select select = (Select)stmt;
			SelectBody selectBody = select.getSelectBody();
			
			if(selectBody instanceof PlainSelect)
			{
				plainSelect = (PlainSelect)selectBody;
				
				searchForDateSourceTables(plainSelect);
			}
		}		
	}
	
	/** @see searchForDateSourceTables
	 * @param plainSelect select clause
	 */
	public void searchForDateSourceTables(PlainSelect plainSelect)
	{
		FromItem fromItem = plainSelect.getFromItem();
				
		searchForDateSourceTables(fromItem);
		
		List<Join> joins = plainSelect.getJoins();
		
		if(joins != null)
		{
			for(Join join:joins)
			{
				FromItem rightItem = join.getRightItem();
				
				if(rightItem instanceof Table)
				{
					searchForDateSourceTables(rightItem);
				}			
			}
		}
	}
	
	/** @see searchForDateSourceTables
	 * @param fromItem from clause item
	 */
	public void searchForDateSourceTables(FromItem fromItem)
	{
		if(fromItem instanceof Table)
		{
			Table table = (Table)fromItem;
			FromTable ft = FromTable.fromTable(table);
							
			dateSourceTables.add(ft);
		}
		else if(fromItem instanceof SubSelect)
		{
			SubSelect sselect = (SubSelect)fromItem;
			
			SelectBody sb = sselect.getSelectBody();
			
			if(sb instanceof PlainSelect)
			{
				searchForDateSourceTables((PlainSelect)sb);
			}			
		}		
	}
	
	/** Get all source tables
	 * @return source tables
	 */
	public List<FromTable> getDateSourceTables()
	{
		return dateSourceTables;
	}

}
