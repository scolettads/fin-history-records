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

import java.text.MessageFormat;

import it.finmatica.history.records.internal.compiere.history.HDB;
import it.finmatica.history.records.internal.compiere.history.HistorySelectionData;
import org.compiere.util.CCache;

/** Generates sub-query replacement or table/where replacements
 * 
 * @author s.coletta@ads.it
 *
 */
public class ADHistoryReplacer extends HistoryReplacer
{
	private static final String HST_SUFFIX = "_HST";
	private static final String HAS_HISTORY = "SELECT 1 From AD_Table WHERE lower(TableName)=? AND HST_HistoryMode IN ('T','V')";
	
	// 0: table to replace
	// 1: date source

	private static String REPLACED_QUERY_FMT = "(SELECT * FROM {0}_HST WHERE ({1}) BETWEEN {0}_HST.HSTFromDate and {0}_HST.HSTToDate)";
	private static final MessageFormat REPLACED_QUERY = new MessageFormat(REPLACED_QUERY_FMT);
	
	private static String WHEREEXPRESSION_FMT = "(({1}) BETWEEN {0}.HSTFromDate and {0}.HSTToDate)";
	private static final MessageFormat WHEREEXPRESSION = new MessageFormat(WHEREEXPRESSION_FMT);
	
	private static CCache<String, Boolean>	tableCache = new CCache<>("ADHistoryReplacer.Table",300);
	
	public String getHistoryTableName(String table)
	{
		return table + HST_SUFFIX;
	}
	
	public static boolean hasHistory(String table)
	{
		table = table.toLowerCase();
		
		if(tableCache.containsKey(table))
			return tableCache.get(table);
		
		final int hasHistory = HDB.getSQLValue(HistorySelectionData.DISABLE_TIMEMACHINE, null, HAS_HISTORY, table);
		boolean bHasHistory = (hasHistory > 0);
		
		tableCache.put(table, Boolean.valueOf(bHasHistory));
		return bHasHistory;
	}

	/* (non-Javadoc)
	 * @see it.finmatica.history.records.sql.HistoryReplacer#hasHistory(it.finmatica.history.records.sql.FromTable)
	 */
	@Override
	public boolean hasHistory(FromTable fromTable)
	{
		return hasHistory(fromTable.table);
	}

	/* (non-Javadoc)
	 * @see it.finmatica.history.records.sql.HistoryReplacer#getReplacedTable(it.finmatica.history.records.sql.FromTable, java.lang.String, boolean)
	 */
	@Override
	public String getReplacedTable(FromTable fromTable, String dateSource, boolean isInsideFrom)
	{
		String replaced = null;
		
		if(isInsideFrom)
		{
			replaced = getHistoryTableName(fromTable.table);
		}
		else
		{
			String params[] = {fromTable.table, dateSource};
			
			replaced = REPLACED_QUERY.format(params);			
		}
		
		return replaced;
	}

	@Override
	public String getWhereExpression(FromTable fromTable, boolean isInnerJoin, String dateSource, boolean isInsideFrom)
	{
		String whereExpression = null;
		
		if(isInsideFrom)
		{
			String params[] = {fromTable.getEffectiveName(), dateSource};
			
			whereExpression = WHEREEXPRESSION.format(params);		
		}
		
		return whereExpression;
	}

}
