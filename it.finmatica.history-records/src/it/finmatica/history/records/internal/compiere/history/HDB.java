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

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.compiere.util.CPreparedStatement;
import org.compiere.util.DB;

/** Utility class wrapping DB functions with history management.
 * 
 * 
 * @author s.coletta@ads.it
 *
 */
public class HDB
{
	private static HistorySelectionData setNewHSD(HistorySelectionData hsdNew)
	{
		HistorySelectionData hsdPrev = HistorySelectionData.getCurrent();
		
		if(hsdNew != null)
			HistorySelectionData.setCurrent(hsdNew);
		
		return hsdPrev;
	}
	
	private static void resetHSD(HistorySelectionData hsdCurrent,HistorySelectionData hsdPrevious)
	{
		if(hsdCurrent != null)
		{
			if(hsdPrevious != null)
				HistorySelectionData.setCurrent(hsdPrevious);
			else
				HistorySelectionData.reset();
		}
	}
	
	/** @see DB.getSQLValueTS
	 * 
	 * @param hsd history selection data 
	 */
	public static Timestamp getSQLValueTS(HistorySelectionData hsd,String trxName,String sql,Object... params)
	{
		Timestamp ts = null;
		HistorySelectionData prevHSD = setNewHSD(hsd);
		
		try
		{
			ts = DB.getSQLValueTS(trxName, sql, params);
		}
		finally
		{
			resetHSD(hsd,prevHSD);
		}
		
		return ts;
	}
	
	/** @see DB.getSQLValueString
	 * 
	 * @param hsd history selection data 
	 */

	public static String getSQLValueString(HistorySelectionData hsd,String trxName,String sql, Object... params)
	{
		String str = null;
		HistorySelectionData prevHSD = setNewHSD(hsd);
		
		try
		{
			if(hsd != null)
				HistorySelectionData.setCurrent(hsd);
			
			str = DB.getSQLValueString(trxName, sql, params);
		}
		finally
		{
			resetHSD(hsd,prevHSD);
		}
		
		return str;
	}
	
	/** @see DB.getSQLValue
	 * 
	 * @param hsd history selection data 
	 */
	
	public static int getSQLValue(HistorySelectionData hsd,String trxName,String sql, Object... params)
	{
		int val = -1;
		HistorySelectionData prevHSD = setNewHSD(hsd);
		
		try
		{
			if(hsd != null)
				HistorySelectionData.setCurrent(hsd);
			
			val = DB.getSQLValue(trxName, sql, params);
		}
		finally
		{
			resetHSD(hsd,prevHSD);
		}
		
		return val;
	}
	
	/** @see DB.getSQLValueBD
	 * 
	 * @param hsd history selection data 
	 */
	
	public static BigDecimal getSQLValueBD(HistorySelectionData hsd,String trxName,String sql, Object... params)
	{
		BigDecimal val = null;
		HistorySelectionData prevHSD = setNewHSD(hsd);
		
		try
		{
			if(hsd != null)
				HistorySelectionData.setCurrent(hsd);
			
			val = DB.getSQLValueBD(trxName, sql, params);
		}
		finally
		{
			resetHSD(hsd,prevHSD);
		}
		
		return val;
	}
	
	/** @see DB.prepareStatement
	 * 
	 * @param hsd history selection data 
	 */
	
	public static CPreparedStatement prepareStatement(HistorySelectionData hsd,String sql,String trxName)
	{
		CPreparedStatement pstmt = null;
		HistorySelectionData prevHSD = setNewHSD(hsd);
		
		try
		{
			if(hsd != null)
				HistorySelectionData.setCurrent(hsd);
			
			pstmt = DB.prepareStatement(sql, trxName);
		}
		finally
		{
			resetHSD(hsd,prevHSD);
		}
		
		return pstmt;
	}

}
