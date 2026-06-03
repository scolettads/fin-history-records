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
package it.finmatica.history.records.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MTable;
import org.compiere.model.MTree;
import org.compiere.model.PO;
import org.compiere.model.Query;
import it.finmatica.history.records.internal.compiere.history.HSTMTree;
import org.compiere.util.DB;
import org.compiere.util.Msg;
import org.compiere.util.TimeUtil;

import it.finmatica.history.records.util.HSTMessages;

public class HSTPO {
	private static final String AD_CLIENT_ID = "AD_Client_ID";
	public static final String COLUMNNAME_HSTActualRecord = "HSTActualRecord";
	public static final String HSTActualRcd_No = "N";
	public static final String HSTActualRcd_Yes = "Y";
	public static final String HSTActualRcd_Future = "F";
	public static final String COLUMNNAME_HSTFromDate = "HSTFromDate";
	public static final String COLUMNNAME_HSTToDate = "HSTToDate";
	public static final String SUFFIX_HISTORY_TABLE = "_HST";
	public static final String SUFFIX_UU = "_UU";

	public static final int HST_LVL_NONE= 0;
	public static final int HST_LVL_BASE = 1;
	public static final int HST_LVL_FULL = 2;
	
	public static PO createHSTRecord(PO po,Timestamp HSTFromDate,boolean saveRecord,String trxName)
	{
		String tableName = po.get_TableName();
		String tableNameHST = tableName+SUFFIX_HISTORY_TABLE;
		
		// scoletta@ads.it - History Records Plugin
		// In vanilla iDempiere 13 il costruttore PO(ctx, tableName, trx) non esiste
		// (era una modifica core Finmatica). Pattern vanilla equivalente:
		// MTable.getPO(0, trx) crea un nuovo PO con type dinamico — se la tabella
		// non e' registrata in alcun IModelFactory (caso tipico per le tabelle *_HST
		// generate dinamicamente), ritorna un GenericPO. Esattamente quello che serve.
		PO poHST = MTable.get(po.getCtx(), tableNameHST).getPO(0, trxName);
		
		// scoletta@ads.it - History Records Plugin
		// Vanilla iDempiere 13 ha solo PO.copyValues(from, to) a 2 argomenti che
		// salta key/standard/notAllowCopy columns (logica di "duplicate record").
		// Per la storicizzazione serve una copia COMPLETA: utility nel plug-in.
		// Parametri originali Finmatica: copyKeyColumn=true, copyStandardColumn=true,
		// copyNotAllowCopyColumn=true, copyNewValues=true, withNulls=false
		it.finmatica.history.records.internal.compiere.model.POUtil.copyValues(
				po, poHST, true, true, true, true, false);
		poHST.setAD_Org_ID(po.getAD_Org_ID());
		poHST.setIsActive(po.isActive());
		poHST.set_ValueOfColumn(AD_CLIENT_ID, po.getAD_Client_ID());
		
		setHSTFromDate(poHST, HSTFromDate);
		
		if(saveRecord)
			poHST.saveEx(trxName);
		
		return poHST;
	}	
	
	public static PO createHSTRecord(PO po,Timestamp HSTFromDate ,Timestamp HSTToDate,boolean saveRecord,String trxName)
	{
		PO poHST = createHSTRecord(po,HSTFromDate,false,trxName);
		setHSTToDate(poHST, HSTToDate);
		
		if(saveRecord)
			poHST.saveEx(trxName);
		
		return poHST;
	}
	
	public static class RecordHST
	{
		public String columnNameLinkHSTRecord;
		public String tableNameFrom;
		public String tableNameHST;
		public int recordLinkHSTRecord_ID;
		
		public RecordHST(PO po,PO poHST) 
		{
			if(po != null)
				tableNameHST = po.get_TableName()+SUFFIX_HISTORY_TABLE;
			else if(poHST != null)
			{
                if(poHST.get_TableName().endsWith(SUFFIX_HISTORY_TABLE) == false)
                {
                    tableNameHST = poHST.get_TableName()+SUFFIX_HISTORY_TABLE;
                }
                else
                {
                    tableNameHST = poHST.get_TableName();
                }
            }

			else
				throw new AdempiereException("Errore passare o il PO o il POHST");
			
			
			MTable t = MTable.get(poHST != null ? poHST.getCtx():po.getCtx(), tableNameHST);
			if (t != null && !t.isView())
			{
				tableNameFrom = tableNameHST.replaceAll(SUFFIX_HISTORY_TABLE, "");
				columnNameLinkHSTRecord = tableNameFrom+"_ID";
			}
			else
			{
				if(po == null)
					po=poHST;
					
				if(po.get_TableName().equals(MTree.Table_Name))
				{
					tableNameHST = MTree.Table_Name;
					tableNameFrom = MTree.Table_Name;
					columnNameLinkHSTRecord = HSTMTree.COLUMNNAME_HSTParentTree_ID; 
				}
			}
			
			if(po != null)
				recordLinkHSTRecord_ID = po.get_ValueAsInt(columnNameLinkHSTRecord);
			else if(poHST != null)
				recordLinkHSTRecord_ID = poHST.get_ValueAsInt(columnNameLinkHSTRecord);
		}
	}
	
	public static void validateHistoryRecord (PO historyRecordToCreate,Properties ctx, RecordHST recordHST,Timestamp p_Date,String trxName,boolean setActualRecord,boolean saveHSTRecord)
	{
		List<PO> historyRecordsToSave = new ArrayList<>();
		PreparedStatement psmt = null;
		ResultSet rs = null;
		Timestamp NEWToDate = null;
		
		String tableNameHST = recordHST.tableNameHST;
		String columnNameHST = recordHST.columnNameLinkHSTRecord;
		StringBuilder sql = new StringBuilder();
		String columnNameHST_ID = tableNameHST+"_ID";
		
		sql.append("Select ").append(HSTPO.COLUMNNAME_HSTFromDate).append(",").append(columnNameHST_ID).append(" from ")
		.append(tableNameHST).append(" where ").append(columnNameHST).append(" = ? And HSTToDate >= ?");
		
		try
		{
			psmt = DB.prepareStatement(sql.toString(),trxName);
			psmt.setInt(1,recordHST.recordLinkHSTRecord_ID);
			psmt.setTimestamp(2, p_Date);
			rs = psmt.executeQuery();
			Calendar calendar = Calendar.getInstance();
			calendar.set(3000,11,31,0,0);
			
			if(p_Date.after(new Timestamp(calendar.getTimeInMillis())))
				throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_DATE_INPUT_AFTER_3000_IS_NOT_SUPPORTED));
			
			boolean firstFound = false;
			while(rs.next())
			{
				if(firstFound==false)
					firstFound=true;
				
				Timestamp dateFrom = rs.getTimestamp(HSTPO.COLUMNNAME_HSTFromDate);
				int HST_ID = rs.getInt(columnNameHST_ID);
				
				if(p_Date.after(dateFrom))
				{
					calendar.setTimeInMillis(p_Date.getTime());
					calendar.add(Calendar.DAY_OF_MONTH, -1);
					
					Query uQuery = new Query(ctx,tableNameHST, columnNameHST_ID+" = ? ", trxName);
					uQuery.setParameters(HST_ID);
					PO historyRecord = uQuery.first();
					HSTPO.setHSTToDate(historyRecord, new Timestamp(calendar.getTimeInMillis()));
					
					historyRecordsToSave.add(historyRecord);
				}
				else if( p_Date.before(dateFrom))
				{
					if(NEWToDate == null)
					{
						NEWToDate = dateFrom;
					}
					else if(NEWToDate.after(dateFrom))
					{
						NEWToDate = dateFrom;
					}
				}
			}
			
			if(firstFound==false)
			{
				String whereClause = columnNameHST+" = ? And HSTToDate < ?";
				
				Query mQuery = new Query(ctx, tableNameHST, whereClause, trxName);
				mQuery.setParameters(recordHST.recordLinkHSTRecord_ID,p_Date);
				int no = mQuery.count();
				
				if(no>0)
					throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERROR_CREATE_HOLE_INTO_HISTORICIZATION));
			}
				
			if(NEWToDate == null)
			{
				calendar.set(3000,11,31,0,0);
				NEWToDate = new Timestamp(calendar.getTimeInMillis());
			}
			else
			{
				calendar.setTimeInMillis(NEWToDate.getTime());
				calendar.add(Calendar.DAY_OF_YEAR, -1);
				NEWToDate = new Timestamp (calendar.getTimeInMillis());
			}
			
		}
		catch (Exception e) 
		{
			throw new AdempiereException(e);
		}
		finally
		{
			DB.close(rs, psmt);
			rs = null;
			psmt = null;
		}
		
		HSTPO.setHSTFromDate(historyRecordToCreate,p_Date);
		HSTPO.setHSTToDate(historyRecordToCreate,NEWToDate);

		if(setActualRecord)
			HSTPO.setHSTActualRecord(historyRecordToCreate, HSTPO.HSTActualRcd_No);
		
		if(saveHSTRecord)
			historyRecordToCreate.saveEx(trxName);
		
		for(PO hstRecord:historyRecordsToSave)
		{
			hstRecord.saveEx(trxName);
		}
	}
	
	public static int getLevelHistory(PO po)
	{
		int value = HST_LVL_NONE;
		
		if(po.get_ColumnIndex(COLUMNNAME_HSTFromDate)>0 
				&& po.get_ColumnIndex(COLUMNNAME_HSTToDate) > 0)
		{
			value = HST_LVL_BASE;
			
			if(po.get_TableName().endsWith(SUFFIX_HISTORY_TABLE))
				value=HST_LVL_FULL;
		}
		
		return value;
	}
	
	public static void setHSTActualRecord(PO po, String HSTActualRecord) {
		po.set_ValueOfColumn(COLUMNNAME_HSTActualRecord, HSTActualRecord);
	}

	public static String getHSTActualRecord(PO po) {
		return (String) po.get_Value(COLUMNNAME_HSTActualRecord);
	}
	
	public static Timestamp getHSTFromDate(PO po)
			throws RuntimeException {
		return (Timestamp) po.get_Value(COLUMNNAME_HSTFromDate);
	}

	public static void setHSTFromDate(PO po, Timestamp dtFrom) {
		po.set_ValueOfColumn(COLUMNNAME_HSTFromDate, TimeUtil.getDay(dtFrom));
	}
	
	public static Timestamp getHSTToDate(PO po)
			throws RuntimeException {
		return (Timestamp) po.get_Value(COLUMNNAME_HSTToDate);
	}

	public static void setHSTToDate(PO po, Timestamp dtTo) {
		po.set_ValueOfColumn(COLUMNNAME_HSTToDate, TimeUtil.getDay(dtTo));
	}
	
	public static void checkDateFrom(Properties ctx, RecordHST recordHST, int Record_ID, Timestamp date,
			String trxName) {
		
		StringBuilder whereClause1 = new StringBuilder("HSTFromDate = ? and ").append(recordHST.columnNameLinkHSTRecord)
				.append(" = ?");

		Query mQuery = new Query(ctx, recordHST.tableNameHST, whereClause1.toString(), trxName);
		mQuery.setParameters(date, Record_ID);
		int no = mQuery.count();

		if (no > 0) {
			throw new AdempiereException(
					Msg.getMsg(ctx, HSTMessages.HST_ERR_HISTORY_EXIST_ANOTHER_RECORD_WITH_THIS_DATE));
		}
	}

	
	public static PO createAndInsertHSTRecord(PO recordToHistory,Timestamp dateFrom,boolean setActualRecord,boolean saveRecordHst,final String trxName)
	{
		RecordHST recordHST = new RecordHST(recordToHistory,null);
		PO historyRecord = HSTPO.createHSTRecord(recordToHistory,dateFrom,false,trxName);
		checkDateFrom(historyRecord.getCtx(), recordHST, recordHST.recordLinkHSTRecord_ID, dateFrom, trxName);
		validateHistoryRecord(historyRecord, recordToHistory.getCtx(), recordHST, dateFrom, trxName, setActualRecord, saveRecordHst);
		
		return historyRecord;
	}
}
