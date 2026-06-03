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
package it.finmatica.history.records.internal.compiere.history;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.GridTable;
import org.compiere.model.MSysConfig;
import org.compiere.print.PrintData;
import org.compiere.print.PrintDataElement;
import org.compiere.util.CLogger;
import org.compiere.util.KeyNamePair;

/** Utility for date source management
 * 
 * @author s.coletta@ads.it
 *
 */
public class DateSourceUtil
{
	private static CLogger log = CLogger.getCLogger(DateSourceUtil.class);
	
	private static final String Q_DATESOURCETABLE_BYNAME = "SELECT c.script FROM HST_DateSourceConf c inner join AD_Table t on (t.AD_Table_ID = c.AD_Table_ID) and t.IsActive='Y' and c.IsActive='Y' and lower(t.tablename) = ? AND c.AD_Client_ID in (0,?) AND c.AD_Org_ID = 0 ORDER BY c.AD_Client_ID DESC";
	private static final String Q_DATESOURCETABLE_BYID = "SELECT c.script FROM HST_DateSourceConf c where AD_Table_ID = ? AND c.AD_Client_ID in (0,?) AND c.AD_Org_ID = 0 ORDER BY c.AD_Client_ID DESC";

	public static final char TABLEFIELD_MARKER = '#',
														TABLEFIELD_SEP = '.';
	
	public static final String SYSCONFIG_DATESETUP_DEBUG_MODE  = "HST_ENABLE_DATESETUP_TESTMODE"; 
	
	/** Parse a date source string in form:
	 * #table.field# (#c_orderline.c_order_id#)
	 * 
	 * @param tableAndField
	 * @return
	 */
	public static TableAndField parseTableAndField(String tableAndField)
	{
		TableAndField taf = null;
		
		if(tableAndField == null || tableAndField.length() < 5) // #t.f# (at least 5 chars)
			return taf;
		
		int startAt = tableAndField.indexOf(TABLEFIELD_MARKER),
				endAt = tableAndField.indexOf(TABLEFIELD_MARKER, startAt+1);
		
		if(startAt >= 0 && endAt > startAt)
		{
			int sep = tableAndField.indexOf(TABLEFIELD_SEP,startAt);
			
			if(sep > 0)
			{
				String table = tableAndField.substring(startAt + 1,sep); // Skip first #
				String field = tableAndField.substring(sep+1,endAt); // Skip last #
				
				taf = new TableAndField(table,field);
			}			
		}
		
		return taf;
	}
	
	/** Validate a subquery that will be used as a date source.
	 * 
	 * @param query
	 * @return true if valid
	 */
	public static boolean validateSubquery(String query)
	{
		int start = query.indexOf(TABLEFIELD_MARKER),
				end = query.indexOf(TABLEFIELD_MARKER, start+1);
		
		if(start == 0) // Table and field format
		{
			return (parseTableAndField(query) != null);
		}
				
		if(start < 0 || end < 0) // Nothing to do
		{
			return false;
		}
		
		String tableAndField = query.substring(start+1, end);
		
		return tableAndField.indexOf(TABLEFIELD_SEP) > 0;
	}
	
	/** Parse a subquery used as date source
	 * 
	 * @param query
	 * @param table
	 * @param id
	 * @return
	 */
	public static String replaceInSubquery(String query,String table,int id)
	{
		if(table == null && id < 0) // Nothing to replace with
			return query;
		
		int start = query.indexOf(TABLEFIELD_MARKER),
				end = query.indexOf(TABLEFIELD_MARKER, start+1);
		
		if(start < 0 || end < 0) // Nothing to do
		{
			return query;
		}
		
		StringBuilder sbReplaced = new StringBuilder(query.substring(0,start));
		
		if(id > 0) // Replace id
		{
			sbReplaced.append(Integer.toString(id));
		}
		else
		{
			int sep = query.indexOf(TABLEFIELD_SEP,start+1);
			
			if(sep > 0)
			{
				String field = query.substring(sep,end);
				
				sbReplaced.append(table);
				sbReplaced.append(field);
			}
		}
		
		sbReplaced.append(query.substring(end+1));
		
		return sbReplaced.toString();
	}

	/** Replace with table in a source query
	 * 
	 * @param query
	 * @param table
	 * @return parsed string
	 */
	public static String replaceInSubquery(String query,String table)
	{
		return replaceInSubquery(query, table, -1);
	}
	
	/**  Replace with id in a date source query
	 * 
	 * @param query
	 * @param id
	 * @return parsed string
	 */
	public static String replaceInSubquery(String query,int id)
	{
		return replaceInSubquery(query, null, id);
	}
	
	/** Get history selection data from a table name 
	 * 
	 * @param mixedcaseTable
	 * @return history selection data, or null if no selection data is avalaible for the input table
	 */
	public static HistorySelectionData fromSourceTable(final String mixedcaseTable,  final int AD_Client_ID)
	{
		if(HSTSysConfig.isTimeMachineDisabled())
			return null;
			
		HistorySelectionData hsd = null;
		final String table = mixedcaseTable.toLowerCase();
		
		String script = HDB.getSQLValueString(HistorySelectionData.DISABLE_TIMEMACHINE, null, Q_DATESOURCETABLE_BYNAME, table, AD_Client_ID);
		
		if(script != null)
		{
			script = script.trim();
			
			if(script.indexOf(TABLEFIELD_MARKER) == 0)
			{
				hsd = new HistorySelectionData(script);
			}
			else
			{
				hsd = new HistorySelectionData(script, -1);
			}
		}
		
		return hsd;
	}
	
	/** Get history selection data from a table id 
	 * 
	 * @param mixedcaseTable
	 * @return history selection data, or null if no selection data is avalaible for the input table
	 */
	public static HistorySelectionData fromSourceTable(final int  AD_Table_ID, final int AD_Client_ID)
	{
		if(HSTSysConfig.isTimeMachineDisabled())
			return null;
			
		HistorySelectionData hsd = null;
		
		String script = HDB.getSQLValueString(HistorySelectionData.DISABLE_TIMEMACHINE, null, Q_DATESOURCETABLE_BYID, AD_Table_ID, AD_Client_ID);
		
		if(script != null)
		{
			script = script.trim();
			
			if(script.indexOf(TABLEFIELD_MARKER) == 0)
			{
				hsd = new HistorySelectionData(script);
			}
			else
			{
				hsd = new HistorySelectionData(script, -1);
			}
		}
		
		return hsd;
	}
	
	/** Resolve date in selection data with the correct date from input tab/row
	 * 
	 * @param hsd history selection data
	 * @param tab tab 
	 * @param currentRow current row
	 */
	public static void resolveDateInSelectionData(HistorySelectionData hsd,GridTab tab, int currentRow)
	{
		TableAndField taf = hsd.getParsedTableAndField();
		
		if(taf != null)
		{
			if(taf.getTable().equalsIgnoreCase(tab.getTableName()))
			{
				String fieldName = taf.getField();
				GridTable gridTable = tab.getTableModel();
				int fieldsCount = gridTable.getColumnCount();
				Object oTS = null;
				boolean bFieldFound = false;
								
				for(int i=0; i<fieldsCount; i++)
				{
					String columnName = gridTable.getColumnName(i);
					
					if(columnName.equalsIgnoreCase(fieldName))
					{
						oTS = gridTable.getValueAt(currentRow, i);
						bFieldFound = true;
						break;
					}
				}
								
				if(bFieldFound == false)
				{
					throw new AdempiereException("History setup error, field not found:" + 
							fieldName);
				}
				
				if(oTS != null) // Can be null if its a new record
				{
					if((oTS instanceof Timestamp) == false)
					{
						throw new AdempiereException("History setup error, field is not a date:" + 
								fieldName);
					}
					
					hsd.setHistoryDate((Timestamp)oTS);
				}
			}
			else
			{
				throw new AdempiereException("History setup error, expected table:" + 
						tab.getTableName() + ", found:" + taf.getTable());
			}		
		}
		else if(hsd.getSubQuery() != null)
		{
			String subQuery = hsd.getSubQuery();
			TableAndField subTaf = parseTableAndField(subQuery);
			
			if(subTaf != null)
			{
				int recordID = -1;
				String keyFieldName = subTaf.getField();
				
				for(GridField gf:tab.getFields())
				{
					if(gf.getColumnName().equalsIgnoreCase(keyFieldName))
					{
						Number nr = (Number)gf.getValue();
						
						if(nr != null) // Can be null if its a new record
						{
							recordID = nr.intValue();
						}
						break;
					}
				}
				
				if(recordID > 0)
				{
					String dateSQL = DateSourceUtil.replaceInSubquery(subQuery, tab.getTableName(), recordID);
					
					Timestamp ts = HDB.getSQLValueTS(HistorySelectionData.DISABLE_TIMEMACHINE, null, dateSQL);
					
					if(ts == null)
					{
						int iIdMarkerPos = keyFieldName.toLowerCase().indexOf("_id");
						String guessTableName = keyFieldName.substring(0,iIdMarkerPos); // Per standard iDempiere il nome tabella e' uguale al nome della chiava rimosso '_id' finale.
						String sRecordExistsQuery = "SELECT " + keyFieldName + " FROM " + guessTableName + " WHERE " + keyFieldName + " = ?";
						int existsId = HDB.getSQLValue(HistorySelectionData.DISABLE_TIMEMACHINE, null, sRecordExistsQuery, recordID);
						
						if(existsId > 0) // Se l'id (quindi il record) non e' piu ottenibile a db, il record e' stato cancellato, altrimenti abbiamo un errore di setup
						{
							ts = manageDateError("History setup error, date field is null:" + 
								dateSQL);
						}
					}
					
					hsd.setHistoryDate(ts);
				}
			}
			else
			{
				throw new AdempiereException("History setup error, table/field not found in null:" + subQuery);
			}

		}
		else
		{
			throw new AdempiereException("History setup error, no script or table+field:" + 
					hsd.toString());
		}		
	}
	
	/** Resolve date in selection data in prit data
	 * 
	 * @param hsd history selection data
	 * @param printData print data
	 */
	public static void resolveDateInSelectionData(HistorySelectionData hsd,PrintData printData)
	{
		TableAndField taf = hsd.getParsedTableAndField();
		
		if(taf != null)
		{
			if(taf.getTable().equalsIgnoreCase(printData.getTableName()))
			{
				PrintDataElement pdeField = null;
				String fieldName = taf.getField();
				
				int nc = printData.getNodeCount();
								
				for(int i=0; i < nc; i++)
				{
					Object obj = printData.getNode(i);
					if(obj instanceof PrintDataElement)
					{
						PrintDataElement pde = (PrintDataElement)obj;
						
						if(pde.getColumnName().equalsIgnoreCase(fieldName))
						{
							pdeField = pde;
							break;
						}
					}
				}
				
				if(pdeField == null)
				{
					throw new AdempiereException("History setup error, field not found:" + 
							fieldName);
				}
				
				Object oTS = pdeField.getValue();
				
				if((oTS instanceof Timestamp) == false)
				{
					throw new AdempiereException("History setup error, field is not a date:" + 
							fieldName);
				}
				
				hsd.setHistoryDate((Timestamp)oTS);
			}
			else
			{
				throw new AdempiereException("History setup error, expected table:" + 
						printData.getTableName() + ", found:" + taf.getTable());
			}		
		}
		else if(hsd.getSubQuery() != null)
		{			
			String subQuery = hsd.getSubQuery();
			TableAndField sourceTaf = DateSourceUtil.parseTableAndField(subQuery);
			
			if(sourceTaf == null)
				throw new AdempiereException("History setup error, unable to parse table and field from subquery:" + 
						subQuery);
			
			int nodesCount = printData.getNodeCount();
			PrintDataElement pdeField = null;
			
			for(int i=0; i < nodesCount; i++)
			{
				Object node = printData.getNode(i);
				
				if(node instanceof PrintDataElement)
				{
					PrintDataElement pde = (PrintDataElement)node;
					
					if(pde.getColumnName().equalsIgnoreCase(sourceTaf.getField()))
					{
						pdeField = pde;
						break;
					}
				}
			}
			
			if(pdeField != null)
			{
				Object val = pdeField.getValue();
				
				int id = -1;
				
				if(val instanceof KeyNamePair)
				{
					KeyNamePair knp = (KeyNamePair)val;
					id = knp.getKey();
				}
				else if(val instanceof Integer)
				{
					Integer iVal = (Integer)val;
					id = iVal.intValue();
				}
				
				String dateSQL = DateSourceUtil.replaceInSubquery(subQuery, printData.getTableName(),id);
				
				Timestamp ts = HDB.getSQLValueTS(HistorySelectionData.DISABLE_TIMEMACHINE, null, dateSQL);
				
				if(ts == null)
				{
					ts = manageDateError("History setup error, date field is null:" + 
							dateSQL);
				}
				
				hsd.setHistoryDate(ts);
			}
			else
			{
				throw new AdempiereException("History setup error, no id colimn:" + 
						sourceTaf.getField());
			}
		}
		else
		{
			throw new AdempiereException("History setup error, no script or table+field:" + 
					hsd.toString());
		}		
	}
	
	/** Resolve date in selection data in prit data
	 * 
	 * @param hsd history selection data
	 * @param printData print data
	 */
	public static void resolveDateInSelectionData(HistorySelectionData hsd,ResultSet rs)
	{
		TableAndField taf = hsd.getParsedTableAndField();
		
		try
		{
			if(taf != null)
			{
				String fieldName = taf.getField();
				
				Timestamp ts = rs.getTimestamp(fieldName);
				hsd.setHistoryDate(ts);
			}
			else if(hsd.getSubQuery() != null)
			{			
				String subQuery = hsd.getSubQuery();
				TableAndField sourceTaf = DateSourceUtil.parseTableAndField(subQuery);
				
				if(sourceTaf == null)
					throw new AdempiereException("History setup error, unable to parse table and field from subquery:" + 
							subQuery);
				
				String fieldName = sourceTaf.getField();
				
				Timestamp ts = rs.getTimestamp(fieldName);
				hsd.setHistoryDate(ts);			
			}
			else
			{
				throw new AdempiereException("History setup error, no script or table+field:" + 
						hsd.toString());
			}
		}
		catch(SQLException e)
		{
			if(log.isLoggable(Level.INFO))
				log.info("Column not found: " + e.getMessage());
		}
	}	
	
	
	/** Execute code with the specified input data
	 * 
	 * @param hsd
	 * @param runnable
	 * @throws Exception
	 */
	public static void doWithHistoryData(HistorySelectionData hsd, IHistoryDataRunnable runnable, Object ... params) throws Exception
	{
		try
		{
			HistorySelectionData.setCurrent(hsd);
			runnable.run(params);
		}
		finally
		{
			HistorySelectionData.reset();
		}
	}
	
	private static final Timestamp manageDateError(String msg)
	{
		if(MSysConfig.getBooleanValue(SYSCONFIG_DATESETUP_DEBUG_MODE, true))
		{
			throw new AdempiereException(msg);
		}
		else if(log.isLoggable(Level.INFO))
		{
			log.info(msg);
		}
		
		return new Timestamp(System.currentTimeMillis()); // Need to return a date, should be irrelevant
	}
}
