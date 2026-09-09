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
package it.finmatica.history.records.validator;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.Query;
import it.finmatica.history.records.internal.compiere.history.HSTSysConfig;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import it.finmatica.history.records.internal.compiere.util.WhereClauseAndParams;
// scoletta@ads.it - History Records Plugin
// rimosso import org.jfree.util.Log (access restriction in jcommon scaricato da Maven),
// sostituito con CLogger standard iDempiere
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

import it.finmatica.history.records.model.HSTMColumn;
import it.finmatica.history.records.model.HSTMTable;
import it.finmatica.history.records.model.HSTPO;
import it.finmatica.history.records.model.HSTPO.RecordHST;
import it.finmatica.history.records.util.HSTMessages;
import it.finmatica.history.records.internal.idempiere.base.util.SavedFromUI;
import org.compiere.util.Msg;
// scoletta@ads.it - History Records Plugin
// rimosso import it.idempiere.util.DialogHelper, sostituito con logging standard

public class HistoryRecordEventHandler extends AbstractEventHandler
{
	// scoletta@ads.it - History Records Plugin
	private static final CLogger log = CLogger.getCLogger(HistoryRecordEventHandler.class);
	private static final String VALUE_TO_SET = " = ? , ";

	@Override
	protected void initialize() 
	{
		registerEvent(IEventTopics.PO_BEFORE_NEW);
		registerEvent(IEventTopics.PO_BEFORE_CHANGE);
		registerEvent(IEventTopics.PO_AFTER_DELETE);
		registerEvent(IEventTopics.PO_AFTER_NEW);
	}

	@Override
	protected void doHandleEvent(Event event) 
	{
		String topic = event.getTopic();
		Calendar calendar = Calendar.getInstance();
		PO poHST = getPO(event);
		Properties ctx = poHST.getCtx();
		String trxName = poHST.get_TrxName();
		int levelHistory = HSTPO.getLevelHistory(poHST);
		
		if(levelHistory > HSTPO.HST_LVL_NONE)
		{
			RecordHST recordHST = new RecordHST(null,poHST);
			
			// s.coletta@ads.it 2026-09-09
			if(levelHistory > HSTPO.HST_LVL_BASE
					&& topic.equals(IEventTopics.PO_BEFORE_CHANGE)
					&& HSTPO.getHSTActualRecord(poHST).equals(HSTPO.HSTActualRcd_Yes))
			{
				int nColumn = poHST.get_ColumnCount();
				for(int index = 0 ; index<nColumn;index++)
				{
					if(poHST.get_ColumnName(index).equals(HSTPO.COLUMNNAME_HSTFromDate)
							|| poHST.get_ColumnName(index).equals(HSTPO.COLUMNNAME_HSTToDate)
							|| poHST.get_ColumnName(index).equals(HSTPO.COLUMNNAME_HSTActualRecord))
						continue;
					
					if(poHST.is_ValueChanged(index))
					{
						throw new AdempiereException(Msg.getMsg(ctx,HSTMessages.HST_ERR_IMPOSSIBLE_MODIFY_ACTUAL_HISTORY_RECORD));
					}
				}
			}
			
			if(levelHistory > HSTPO.HST_LVL_BASE && topic.equals(IEventTopics.PO_BEFORE_CHANGE))
			{
				String tableNameHST = poHST.get_TableName();
				MTable mTableHST = MTable.get(ctx, tableNameHST);

				String tableName = tableNameHST.replace(HSTPO.SUFFIX_HISTORY_TABLE, "");
				MColumn[] mColumnsHST = mTableHST.getColumns(false);
				
				for(MColumn mColumnHST:mColumnsHST)
				{
					if(poHST.is_ValueChanged(mColumnHST.getColumnName()) && 
							(mColumnHST.getColumnName().equals(HSTPO.COLUMNNAME_HSTFromDate) 
									|| mColumnHST.getColumnName().equals(HSTPO.COLUMNNAME_HSTToDate)
									|| mColumnHST.getColumnName().equals(HSTPO.COLUMNNAME_HSTActualRecord)) == false)
					{
						MTable mTable = MTable.get(ctx, tableName);
						MColumn column =  mTable.getColumn(mColumnHST.getColumnName());
						if(HSTMColumn.isHST_IsHstColumn(column) == false)
						{
							throw new AdempiereException(Msg.getMsg(ctx,HSTMessages.HST_ERR_IMPOSSIBLE_MODIFY_COLUMN_NOT_HISTORY_IN_HISTORY_RECORD));
						}
					}
				}
			}

			if(levelHistory > HSTPO.HST_LVL_NONE 
					&& (poHST.is_ValueChanged(HSTPO.COLUMNNAME_HSTToDate) 
					||	poHST.is_ValueChanged(HSTPO.COLUMNNAME_HSTFromDate)) 
					&& 	topic.equals(IEventTopics.PO_BEFORE_CHANGE) )
			{
				Timestamp newHSTFromDate = HSTPO.getHSTFromDate(poHST); 
				Timestamp newHSTToDate = HSTPO.getHSTToDate(poHST);

				if(newHSTFromDate.after(newHSTToDate))
					throw new AdempiereException(Msg.getMsg(ctx,HSTMessages.HST_ERR_START_DATE));
				
				String columnID = recordHST.columnNameLinkHSTRecord;
				
				String columnname_ID = poHST.get_TableName()+"_ID";
				StringBuilder sbWhere = new StringBuilder(columnID);
				sbWhere.append(" = ? And (HSTFromDate <= ? Or HSTToDate >= ?) and ").append(columnname_ID).append(" != ?");
				
				Query mQuery = new Query(ctx, poHST.get_TableName(),sbWhere.toString(), trxName);
				mQuery.setParameters(poHST.get_Value(columnID) ,newHSTFromDate,newHSTToDate,poHST.get_Value(columnname_ID));
				List<PO> pos = mQuery.list();
				
				for(PO po :pos)
				{
					Timestamp HSTFromDate = HSTPO.getHSTFromDate(po); 
					Timestamp HSTToDate = HSTPO.getHSTToDate(po);
					
					if(HSTFromDate.equals(newHSTFromDate))
						throw new AdempiereException(Msg.getMsg(ctx,HSTMessages.HST_ERR_MODIFY_OVERLAP_HISTORY_RECORD));
					else if(HSTToDate.equals(newHSTToDate))
						throw new AdempiereException(Msg.getMsg(ctx,HSTMessages.HST_ERR_MODIFY_OVERLAP_HISTORY_RECORD));
					else if(HSTFromDate.after(newHSTFromDate) && HSTToDate.before(newHSTToDate))
						throw new AdempiereException(Msg.getMsg(ctx,HSTMessages.HST_ERR_OVERWRITE_ALL_HISTORY_RECORDS));
				}
				
				sbWhere = new StringBuilder(columnID);
				sbWhere.append(" = ? And (HSTFromDate >= ? AND HSTToDate <= ?) and ").append(columnname_ID).append(" != ?");
				
				mQuery = new Query(ctx, poHST.get_TableName(),sbWhere.toString(), trxName);
				mQuery.setParameters(poHST.get_Value(columnID) ,newHSTFromDate,newHSTToDate,poHST.get_Value(columnname_ID));
				pos = mQuery.list();
				
				for(PO po :pos)
				{
					Timestamp HSTFromDate = HSTPO.getHSTFromDate(po); 
					Timestamp HSTToDate = HSTPO.getHSTToDate(po);
					
					if(HSTFromDate.after(newHSTFromDate) && HSTToDate.before(newHSTToDate))
						throw new AdempiereException(Msg.getMsg(ctx,HSTMessages.HST_ERR_OVERWRITE_ALL_HISTORY_RECORDS));
				}
				
				StringBuilder sqlUpdate = new StringBuilder ("update ");
				sqlUpdate.append(poHST.get_TableName()).append(" set HSTFromDate = ? , HSTToDate = ? where ").append(columnname_ID).append(" = ? ");
				Object[] para = new Object[3];
				
				int i = 0;
				para[i++] = newHSTFromDate;
				para[i++] = newHSTToDate;
				para[i++] = poHST.get_Value(columnname_ID);
				
				DB.executeUpdate(sqlUpdate.toString(),para,false,trxName);
				
				if(poHST.is_ValueChanged(HSTPO.COLUMNNAME_HSTFromDate))
				{
					StringBuilder sbWhere2 = new StringBuilder(columnID);
					sbWhere2.append(" = ? And HSTFromDate < ? and ").append(columnname_ID).append(" != ?");
					
					Query qQuery = new Query(ctx, poHST.get_TableName(),sbWhere2.toString(), trxName);
					qQuery.setOrderBy("HSTFromDate desc");
					qQuery.setParameters(poHST.get_Value(columnID) ,newHSTFromDate,poHST.get_Value(columnname_ID));
					PO poToDate = qQuery.first();
					
					if(poToDate != null)
					{
						calendar.setTimeInMillis(newHSTFromDate.getTime());
						calendar.add(Calendar.DAY_OF_MONTH, -1);
						Timestamp toDateToSet = new Timestamp( calendar.getTimeInMillis());
						
						if(HSTPO.getHSTToDate(poToDate).equals(toDateToSet ) == false)
						{
							HSTPO.setHSTToDate(poToDate,toDateToSet);
							newHSTToDate = HSTPO.getHSTToDate(poToDate);
							poToDate.saveEx();
						}
					}
				}
				
				if(poHST.is_ValueChanged(HSTPO.COLUMNNAME_HSTToDate))
				{
					StringBuilder sbWhere3 = new StringBuilder(columnID);
					sbWhere3.append(" = ? And HSTTodate > ? and ").append(columnname_ID).append(" != ?");
					
					Query fQuery = new Query(ctx, poHST.get_TableName(),sbWhere3.toString(), trxName);
					fQuery.setOrderBy("HSTToDate");
					fQuery.setParameters(poHST.get_Value(columnID) ,newHSTToDate,poHST.get_Value(columnname_ID));
					PO poFromDate = fQuery.first();
					
					if(poFromDate != null)
					{
						calendar.setTimeInMillis(newHSTToDate.getTime());
						calendar.add(Calendar.DAY_OF_MONTH, +1);
						Timestamp fromDateToSet = new Timestamp( calendar.getTimeInMillis());
						
						if(HSTPO.getHSTFromDate(poFromDate).equals(fromDateToSet ) == false)
						{
							HSTPO.setHSTFromDate(poFromDate,fromDateToSet);
							poFromDate.saveEx();
						}
					}
				}
			}

			if(levelHistory > HSTPO.HST_LVL_NONE 
					&& topic.equals(IEventTopics.PO_AFTER_DELETE))
			{
				String tableNameHST = poHST.get_TableName();
				String columnName = recordHST.columnNameLinkHSTRecord;
				
				int tableID = MTable.getTable_ID(recordHST.tableNameFrom);
				MTable table = new MTable(ctx, tableID, trxName);
				MColumn[] columns = table.getColumns(false);
				
				StringBuilder where = new StringBuilder();
				List<Object> parameters = new ArrayList<Object>();
				boolean checkKey = false;
				
				for(MColumn column : columns)
				{
					boolean isWhere = true;
					
					if(column.isKey())
					{
						String columnname = column.getColumnName();
						if(isWhere)
							where.append(columnname).append(" = ?");
						else
							where.append(" AND ").append(columnname).append(" = ?");
						parameters.add(poHST.get_Value(columnname));
						
						checkKey = true;
					}
				}
				
				if(!checkKey)
				{
					String columnUU = poHST.get_ValueAsString(recordHST.tableNameFrom + "_UU");
					
					if(columnUU != null && !columnUU.isEmpty())
					{
						where.append(recordHST.tableNameFrom).append("_UU = ?");
						parameters.add(columnUU);
						
						checkKey = true;
					}
				}
				
				if(!checkKey)
				{
					where.append(columnName).append(" = ?");
					parameters.add(poHST.get_ValueAsInt(columnName));
				}
				
				Query mQuery = new Query(ctx, tableNameHST, where.toString(), trxName);
				mQuery.setParameters(parameters);
				int nPos = mQuery.count();
				
				if(nPos == 0)
					throw new AdempiereException(Msg.getMsg(ctx,HSTMessages.HST_ERR_ONE_HISTORY_RECORD_IS_MANDATORY_FOR_HISTORY_TABLE));
				
				Timestamp HSTFromDateToDelete = HSTPO.getHSTFromDate(poHST);
				
				Query aQuery = new Query(ctx, tableNameHST,columnName+" = ? and HSTFromDate < ?", trxName);
				aQuery.setParameters(poHST.get_ValueAsInt(columnName),HSTFromDateToDelete);
				aQuery.setOrderBy("HSTToDate Desc");
				PO poToModify = aQuery.first();
				
				if(poToModify != null)
				{
					HSTPO.setHSTToDate(poToModify, HSTPO.getHSTToDate(poHST));
					poToModify.saveEx(trxName);
				}
				
			}

			// s.coletta@ads.it 2026-09-09
			if(levelHistory > HSTPO.HST_LVL_BASE
					&& poHST.is_ValueChanged(HSTPO.COLUMNNAME_HSTActualRecord)
					&& topic.equals(IEventTopics.PO_BEFORE_CHANGE))
			{
				throw new AdempiereException(Msg.getMsg(ctx,HSTMessages.HST_ERR_NO_MANUAL_MODIFY));
			}
			else if(levelHistory > HSTPO.HST_LVL_BASE && 
					poHST.is_ValueChanged(HSTPO.COLUMNNAME_HSTActualRecord) && 
					topic.equals(IEventTopics.PO_BEFORE_CHANGE) && 
					HSTPO.getHSTActualRecord(poHST).equals(HSTPO.HSTActualRcd_Yes))
			{
				actualizeData(poHST, recordHST);
			}
			else if(levelHistory > HSTPO.HST_LVL_BASE
					&&	(poHST.is_ValueChanged(HSTPO.COLUMNNAME_HSTFromDate) ||
					poHST.is_ValueChanged(HSTPO.COLUMNNAME_HSTToDate) || 
					topic.equals(IEventTopics.PO_BEFORE_NEW)))
			{
				String actualRecord = HSTPO.getHSTActualRecord(poHST);
				
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				
				Timestamp now = new Timestamp(calendar.getTimeInMillis());
				
				Timestamp HSTFromDate = HSTPO.getHSTFromDate(poHST);
				Timestamp HSTToDate = HSTPO.getHSTToDate(poHST);
				
				if((now.after(HSTFromDate) || now.getTime() == HSTFromDate.getTime()) &&
						(now.before(HSTToDate)||now.getTime() == HSTToDate.getTime()))
				{
					HSTPO.setHSTActualRecord(poHST,HSTPO.HSTActualRcd_Yes);
					if(topic.equals(IEventTopics.PO_BEFORE_NEW) == false)
					{
						if(actualRecord == null || actualRecord.equals(HSTPO.HSTActualRcd_No) || 
								actualRecord.equals(HSTPO.HSTActualRcd_Future) )
						{
							actualizeData(poHST, recordHST);
						}
					}
				}
				else if(now.before(HSTFromDate))
				{
					HSTPO.setHSTActualRecord(poHST, HSTPO.HSTActualRcd_Future);
				}
				else
				{
					HSTPO.setHSTActualRecord(poHST, HSTPO.HSTActualRcd_No);
				}
			}
		}
		else if(topic.equals(IEventTopics.PO_AFTER_NEW) )
		{
			PO po = poHST;
			int AD_Table_ID = po.get_Table_ID();
			MTable mTable = MTable.get(ctx, AD_Table_ID, trxName);
		
			String historyMode = HSTMTable.getHST_HistoryMode(mTable);
			
			if(historyMode != null && (historyMode.equals(HSTMTable.HSTMode_Storicizzata) || historyMode.equals(HSTMTable.HSTMode_TimeMachine) || historyMode.equals(HSTMTable.HSTMode_Log) ))
			{
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				
				Timestamp HSTFromDate = new Timestamp(calendar.getTimeInMillis());
				//FIN(ES) - #44309
				Timestamp fixedDateStart = HSTSysConfig.getHST_StartDate_Fixed();
				if(fixedDateStart != null){
					HSTFromDate = fixedDateStart;
				} else if(historyMode.equals(HSTMTable.HSTMode_Log)) {
					// AB - #86292 - 04/03/2026 - Implementazione nuova modalita' di storicizzazione "Log"
					// Alla creazione di un nuovo record il relativo record di storico avra' come start date 01/01/2000
					HSTFromDate = Timestamp.valueOf(LocalDateTime.of(2000, 1, 1, 0, 0));
				} else {
					int rangeDateStart = HSTSysConfig.getHST_StartDate_Range();
					if(rangeDateStart != 0) {
						Date d = new Date(HSTFromDate.getTime());
						Calendar c = Calendar.getInstance();
						c.setTime(d);
						c.add(Calendar.DAY_OF_YEAR, rangeDateStart);
						HSTFromDate=new Timestamp((c.getTime()).getTime());
					}
				}				
				calendar.set(Calendar.YEAR,3000);
				calendar.set(Calendar.MONTH, 11);
				calendar.set(Calendar.DAY_OF_MONTH,31);
				
				Timestamp HSTToDate = new Timestamp(calendar.getTimeInMillis());
				HSTPO.createHSTRecord(po, HSTFromDate, HSTToDate,true,trxName);
				
			}
		}
		//modifica di un record master propagata su tutti gli hst 
		else if(topic.equals(IEventTopics.PO_BEFORE_CHANGE) )
		{	
			PO lastPOHST = null ;
			PO po = poHST;
			int AD_Table_ID = po.get_Table_ID();
			MTable mTable = MTable.get(ctx, AD_Table_ID, trxName);
		
			String historyMode = HSTMTable.getHST_HistoryMode(mTable);
			
			if(historyMode != null && (historyMode.equals(HSTMTable.HSTMode_Storicizzata) ||
					historyMode.equals(HSTMTable.HSTMode_TimeMachine) || 
						historyMode.equals(HSTMTable.HSTMode_Log)))
			{
				String tableName = po.get_TableName();
				String tableNameHST = tableName+HSTPO.SUFFIX_HISTORY_TABLE;
				
				// AB - #86292 - 04/03/2026 - Implementazione nuova modalita' di storicizzazione "Log"
				// che in caso di modifica a colonne storicizzate crea un nuovo record di storico 
				// o aggiorna l'attuale in caso la data di inizio validita' sia uguale alla data attuale
				if(historyMode.equals(HSTMTable.HSTMode_Log) ) {
					String checkExistingHistoryRecordSql = "select 1 from " + tableNameHST + " where HSTFromDate = trunc(sysdate) and " + tableName + "_ID = ?";
					boolean checkExistingHistoryRecord = DB.getSQLValue(trxName, checkExistingHistoryRecordSql, po.get_ID()) > 0;
					if(!checkExistingHistoryRecord) {
						HSTPO.createAndInsertHSTRecord(po, Timestamp.valueOf(LocalDateTime.now()), true, true, trxName);
						return;
					}
				}
				
				MColumn[] mColumns = mTable.getColumns(false);
				StringBuilder updateBaseRecord = new StringBuilder("update ");
				updateBaseRecord.append(tableNameHST).append(" set ");
				
				String updateInit = updateBaseRecord.toString();
				StringBuilder updateActualRecord = new StringBuilder(updateInit);
				Map<Integer,WhereClauseAndParams> updateFutureRecords = new HashMap <Integer, WhereClauseAndParams>();
				List <Object> parametersUpdateBaseRecord = new ArrayList<>();
				List <Object> parametersActRecord = new ArrayList<>();
				
				boolean updated = false;
				boolean updatedBy = false;
				
				for(MColumn mColumn : mColumns)
				{
					String columnName = mColumn.getColumnName();
					
					if(po.is_ValueChanged(columnName) && columnName.endsWith("_UU") == false)
					{
						if (columnName.equals("UpdatedBy"))					
							updatedBy = true;					
						else if (columnName.equals("Updated"))
							updated = true;
						
						if(HSTMColumn.isHST_IsHstColumn(mColumn))
						{
							StringBuilder where = new StringBuilder();
							List<Object> parameters = new ArrayList<Object>();
							boolean checkKey = false;
							
							for(MColumn column : mColumns)
							{
								boolean isWhere = true;
								
								if(column.isKey())
								{
									String columnname = column.getColumnName();
									if(isWhere)
										where.append(columnname).append(" = ?");
									else
										where.append(" AND ").append(columnname).append(" = ?");
									parameters.add(po.get_Value(columnname));
									
									checkKey = true;
								}
							}
							
							if(!checkKey)
							{
								String columnUU = po.get_ValueAsString(tableName + "_UU");
								
								if(columnUU != null && !columnUU.isEmpty())
								{
									where.append(tableName).append("_UU = ?");
									parameters.add(columnUU);
									
									checkKey = true;
								}
							}
							
							if(!checkKey)
							{
								where.append(tableName).append("_ID = ?");
								parameters.add(po.get_ID());
							}
							
							where.append(" AND HSTActualRecord in ('Y','F')");
							
							Query mQuery = new Query(ctx, tableNameHST, where.toString(), trxName);
							mQuery.setParameters(parameters);
							List <PO> poHSTs = mQuery.list();
							for(PO poHSTtoModify : poHSTs)
							{
								if(HSTPO.getHSTActualRecord(poHSTtoModify).equals(HSTPO.HSTActualRcd_Yes))
								{
									/*
									if(po.get_Value(columnName) instanceof Boolean)
										updateActualRecord.append(columnName).append(" = '").append(Util.asString(po.get_ValueAsBoolean(columnName))).append("' , ");
									else if(po.get_Value(columnName) instanceof String || po.get_Value(columnName) instanceof Timestamp)
										updateActualRecord.append(columnName).append(" = '").append(po.get_Value(columnName)).append("' , ");
									else 
										updateActualRecord.append(columnName).append(" = ").append(po.get_Value(columnName)).append(" , ");
									*/
									updateActualRecord.append(columnName).append(VALUE_TO_SET);
									parametersActRecord.add(po.get_Value(columnName));
								}
								else
								{
									Object value = poHSTtoModify.get_Value(columnName);
									Object valueOLD = po.get_ValueOld(columnName);		
											
									if((value == null && valueOLD == null) 
											|| (value != null && valueOLD != null && value.equals(valueOLD)))
									{
										Integer column_ID = poHSTtoModify.get_ValueAsInt(poHSTtoModify.get_TableName()+"_ID");
										
										WhereClauseAndParams whereClauseAndParams = updateFutureRecords.get(column_ID);
										String updateString = null;
										
										if(whereClauseAndParams != null &&	
												whereClauseAndParams.getWhere() != null)
										{
											updateString = whereClauseAndParams.getWhere();
										
											StringBuilder sb = new StringBuilder(updateString);
											sb.append(columnName).append(VALUE_TO_SET);
											List<Object> params =  whereClauseAndParams.getParams();
											params.add(po.get_Value(columnName));
											
											//get lista parametri e aggiunta parametro
											/*if(upupdateStringdat)
											updateString = new String();
											if(po.get_Value(columnName) instanceof String || po.get_Value(columnName) instanceof Timestamp)
												updateString.append(columnName).append(" = '").append(po.get_Value(columnName)).append("' , ");
											else if (po.get_Value(columnName) instanceof Boolean)
												updateString.append(columnName).append(" = '").append(Util.asString(po.get_ValueAsBoolean(columnName))).append("' , ");
											else
												updateString.append(columnName).append(" = ").append(po.get_Value(columnName)).append(" , ");
											*/
											
										}
										else 
										{
											/*
											if(po.get_Value(columnName) instanceof String || po.get_Value(columnName) instanceof Timestamp)
												updateString.append(columnName).append(" = '").append(po.get_Value(columnName)).append("' , ");
											else if (po.get_Value(columnName) instanceof Boolean)
												updateString.append(columnName).append(" = '").append(Util.asString(po.get_ValueAsBoolean(columnName))).append("' , ");
											else
												updateString.append(columnName).append(" = ").append(po.get_Value(columnName)).append(" , ");
											*/
											List<Object> params = new ArrayList<>();
											params.add(po.get_Value(columnName));
											whereClauseAndParams = new WhereClauseAndParams(updateInit+columnName+VALUE_TO_SET,params);
											updateFutureRecords.put(column_ID, whereClauseAndParams);
										}
									}
									else if(SavedFromUI.isSavedFromUI(poHST))
									{
										// scoletta@ads.it - History Records Plugin
										// sostituito DialogHelper.warn con logging (no UI feedback in vanilla iDempiere 13)
										log.warning(Msg.translate(Env.getCtx(), HSTMessages.HST_WARN_HISTORY_RECORD_FUTURE_VALIDITY));
									}
								}
								//FIN(ES) - salvo l'ultimo record di storico modificato
								lastPOHST = poHSTtoModify ;
							}
						}
						else
						{
							/*
							if(po.get_Value(columnName) instanceof Boolean)
								update.append(columnName).append(" = '").append(Util.asString(po.get_ValueAsBoolean(columnName))).append("' , ");
							else if (po.get_Value(columnName) instanceof String || po.get_Value(columnName) instanceof Timestamp)
								update.append(columnName).append(" = '").append(po.get_Value(columnName)).append("' , ");
							else
								update.append(columnName).append(" = ").append(po.get_Value(columnName)).append(" , ");
							*/
							
							updateBaseRecord.append(columnName).append(VALUE_TO_SET);
							parametersUpdateBaseRecord.add(po.get_Value(columnName));
						}
					}
				}
				int no = 0;
				
				Timestamp tsNow = new Timestamp(System.currentTimeMillis());
				int updatedByUser = Integer.valueOf(Env.getContextAsInt(po.getCtx(), "#AD_User_ID"));
				
				if(updateBaseRecord.toString().contains(VALUE_TO_SET))
				{
					updateBaseRecord.delete(updateBaseRecord.length()-3, updateBaseRecord.length()-1);
					
					if(!updated)
					{
						updateBaseRecord.append(", Updated = ? ");
						parametersUpdateBaseRecord.add(tsNow);
					}
					
					if(!updatedBy)
					{
						updateBaseRecord.append(", UpdatedBy = ? ");
						parametersUpdateBaseRecord.add(updatedByUser);						
					}
					
					boolean checkKey = false; 
					
					for(MColumn mColumn : mColumns)
					{
						boolean isWhere = true;
						
						if(mColumn.isKey())
						{
							String columnName = mColumn.getColumnName();
							if(isWhere)
								updateBaseRecord.append(" WHERE ").append(columnName).append(" = ?");
							else
								updateBaseRecord.append(" AND ").append(columnName).append(" = ?");
							parametersUpdateBaseRecord.add(po.get_Value(columnName));
							
							checkKey = true;
						}
					}
					
					if(!checkKey)
					{
						String columnUU = po.get_ValueAsString(tableName + "_UU");
						
						if(columnUU != null && !columnUU.isEmpty())
						{
							updateBaseRecord.append(" WHERE ").append(tableName).append("_UU = ?");
							parametersUpdateBaseRecord.add(columnUU);
							
							checkKey = true;
						}
					}
					
					if(!checkKey)
						updateBaseRecord.append(" where ").append(tableName).append("_ID = ").append(po.get_ID());
					
					no = DB.executeUpdate(updateBaseRecord.toString(),parametersUpdateBaseRecord.toArray(),false ,trxName);
					
				}
				
				if(updateActualRecord.toString().contains(VALUE_TO_SET))
				{
					updateActualRecord.delete(updateActualRecord.length()-3,updateActualRecord.length()-1);
					
					if(!updated)
					{
						updateActualRecord.append(", Updated = ? ");
						parametersActRecord.add(tsNow);
					}
					
					if(!updatedBy)
					{
						updateActualRecord.append(", UpdatedBy = ? ");
						parametersActRecord.add(updatedByUser);						
					}
					
					updateActualRecord.append(" where HSTActualRecord = 'Y' and ").append(tableName).append("_ID = ").append(po.get_ID());
					no = DB.executeUpdate(updateActualRecord.toString(),parametersActRecord.toArray(),false , trxName);
				}
				
				if(updateFutureRecords.isEmpty()==false)
				{
					for(Entry<Integer,WhereClauseAndParams> entry : updateFutureRecords.entrySet())
					{
						Integer id = entry.getKey();
						WhereClauseAndParams whereClauseAndParams = entry.getValue(); 
						List<Object> params = entry.getValue().getParams();
						
						StringBuilder sbUpdate = new StringBuilder();
						sbUpdate.append(whereClauseAndParams.getWhere()).delete(whereClauseAndParams.getWhere().length()-3, whereClauseAndParams.getWhere().length()-1);
						
						if(!updated)
						{
							sbUpdate.append(", Updated = ? ");
							params.add(tsNow);
						}
						
						if(!updatedBy)
						{
							sbUpdate.append(", UpdatedBy = ? ");
							params.add(updatedByUser);						
						}
						
						sbUpdate.append(" where ").append(tableNameHST).append("_ID = '").append(id).append("'");
						
						no += DB.executeUpdate(sbUpdate.toString(),params.toArray(),false, trxName);
					}
				}
				
				// scoletta@ads.it - History Records Plugin
				log.info("numero di record modificati: "+no);
			}
			if( "Y".equals(Env.getCtx().getProperty("#FINLO_IsIdempiereERP", "N")) && lastPOHST!=null 
					&& (
							lastPOHST.get_TableName().equals("M_Product_HST")
							||
							lastPOHST.get_TableName().equals("M_Product_Category_Acct_HST")
						)
				){
				//FIN(ES) - 42571 - forzo la chiamata al validator per queste particolari tabelle 
				ModelValidationEngine.get().fireModelChange(lastPOHST, ModelValidator.TYPE_AFTER_CHANGE);
			}
		}
	}
	
	public static void actualizeData(PO poHST,RecordHST recordHST)
	{
		Properties ctx = poHST.getCtx();
		String trxName = poHST.get_TrxName();
		
		String tableName = recordHST.tableNameFrom;
		int record_ID = recordHST.recordLinkHSTRecord_ID;
		
		PO poToModify = MTable.get(ctx, tableName).getPO(record_ID, trxName);
		int nColumns = poToModify.get_ColumnCount();
		boolean isChanged = false;
		for(int index= 0;index < nColumns; index++)
		{
			String columnName = poToModify.get_ColumnName(index);
			
			//SN Bug #36198: prendo la colonna per controllare se non � una colonna virtuale
			MColumn column = MColumn.get(ctx, tableName, columnName);
			
			if(columnName.equalsIgnoreCase(MColumn.COLUMNNAME_Updated) == false && 
				columnName.equalsIgnoreCase(MColumn.COLUMNNAME_UpdatedBy) == false &&
				columnName.equalsIgnoreCase(MColumn.COLUMNNAME_Created) == false && 
				columnName.equalsIgnoreCase(MColumn.COLUMNNAME_CreatedBy) == false &&
				columnName.equalsIgnoreCase(tableName+HSTPO.SUFFIX_UU) == false && 
				!column.isVirtualColumn() //SN Bug #36198: escludo le colonne virtuali
				)
			{
				boolean isEquals = false;
				Object value = poHST.get_Value(columnName);
				
				if(value != null)
					isEquals = value.equals(poToModify.get_Value(columnName));
				else if(poToModify.get_Value(columnName) != null)
					poToModify.get_Value(columnName).equals(value);
				else
					isEquals = true;
				
				if(columnName.contains(HSTPO.SUFFIX_HISTORY_TABLE) == false && 
						poToModify.get_ColumnIndex(columnName) > 0 &&
						isEquals == false)
				{
					poToModify.set_ValueOfColumn(columnName, value);
					isChanged = true;
				}
			}
		}
		
		if(isChanged)
		{
			poToModify.saveEx(trxName);
		}
	}
	
	
}
