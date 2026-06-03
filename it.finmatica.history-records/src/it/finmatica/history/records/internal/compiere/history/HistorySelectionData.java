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

import java.io.Serializable;
import java.sql.Timestamp;

public class HistorySelectionData implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5302962145825577419L;
	
	private static final ThreadLocal<HistorySelectionData> s_currentHsd = new ThreadLocal<>();
	
	public static final HistorySelectionData DISABLE_TIMEMACHINE = new HistorySelectionData(true);
	
	public static HistorySelectionData getCurrent()
	{
		return s_currentHsd.get();
	}
	
	public static void reset()
	{
		s_currentHsd.set(null);
	}
	
	public static void setCurrent(HistorySelectionData hsd)
	{
		s_currentHsd.set(hsd);
	}
	
	// public String 		tableName = null;
	
	private Timestamp			historyDate = null;
	private String				tableAndField = null;
	private String				subQuery; // Required id populatd or forTable
	private String				forTable; // 
	private int						id = -1;
	private TableAndField	parsedTableAndField = null;
	
	private boolean 		disableTimeMachine = false;
	
	public HistorySelectionData(Timestamp tsDate)
	{
		historyDate = tsDate;
	}

	public HistorySelectionData(String subquery,int id)
	{
		this.subQuery = subquery;
		this.id = id;
	}
	
	public HistorySelectionData(String subquery,String forTable)
	{
		this.subQuery = subquery;
		this.forTable = forTable;
	}
	
	public HistorySelectionData(String tableAndField)
	{
		this.tableAndField = tableAndField;
		this.parsedTableAndField = DateSourceUtil.parseTableAndField(tableAndField);
	}
	
	public HistorySelectionData(boolean disableTimeMachine)
	{
		this.disableTimeMachine = disableTimeMachine;
	}
		
	public Timestamp getHistoryDate()
	{
		return historyDate;
	}
	
	public void setHistoryDate(Timestamp historyDate)
	{
		this.historyDate = historyDate;
	}

	public String getTableAndField()
	{
		return tableAndField;
	}

	public String getSubQuery()
	{
		return subQuery;
	}

	public int getId()
	{
		return id;
	}

	public boolean isDisableTimeMachine()
	{
		return disableTimeMachine;
	}
	
	public void setId(int id)
	{
		if(subQuery == null)
			throw new UnsupportedOperationException("Cannot set history reference id on non-subquery date source");
		
		this.id = id;
	}
	
	public TableAndField getParsedTableAndField()
	{
		return parsedTableAndField;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("HistorySelectionData [");
		if (historyDate != null)
			builder.append("historyDate=").append(historyDate).append(", ");
		if (tableAndField != null)
			builder.append("tableAndField=").append(tableAndField).append(", ");
		if (subQuery != null)
			builder.append("subQuery=").append(subQuery).append(", ");
		builder.append("id=").append(id).append(", disableTimeMachine=")
				.append(disableTimeMachine).append("]");
		return builder.toString();
	}

	public String getForTable()
	{
		return forTable;
	}
}
