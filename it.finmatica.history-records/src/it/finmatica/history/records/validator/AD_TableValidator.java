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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.MColumn;
import org.compiere.model.MTab;
import org.compiere.model.MTable;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Tab;
import org.compiere.model.X_AD_Table;
import org.compiere.util.DB;
import org.compiere.util.Msg;

import it.finmatica.history.records.model.HSTMAD_Column;
import it.finmatica.history.records.model.HSTMAD_Table;
import it.finmatica.history.records.util.HSTMessages;
import it.finmatica.history.records.internal.idempiere.util.model.AbstractBoostrappableValidator;

public class AD_TableValidator extends AbstractBoostrappableValidator{

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}
	
	private static final String maxWindowType = "select max(ad_window.windowtype) as windowtype"
										+ "  from ad_tab inner join ad_window on ad_window.ad_window_id = ad_tab.ad_window_id"
										+ " where ad_tab.ad_table_id = ? and ad_window.windowtype <> 'Q'";

	@Override
	public String modelChange(PO model, int nType) throws Exception
	{
		if(nType == TYPE_BEFORE_NEW || nType == TYPE_BEFORE_CHANGE)
		{
			if(model instanceof MTable)
			{
				MTable mTable = (MTable) model;
				String trxName = model.get_TrxName();
				Properties ctx = model.getCtx();
				
				String historyMode = HSTMAD_Table.getHST_HistoryMode(mTable);
				
				if((historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_TimeMachine) || 
						historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_Historicizing) ||
							historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_Log)) &&
						mTable.isView())
				{
					throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERR_FOR_VIEW_HISTORY_MUST_BE_VIEW));
				}
				
				if(historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_None)==false &&
						HSTMAD_Table.isHST_IsForceHstApplication(mTable) == false)
				{

					PreparedStatement psmt = null;
					ResultSet rs = null;

					try {
						psmt =DB.prepareStatement(maxWindowType, trxName);
						psmt.setInt(1, mTable.getAD_Table_ID());
						rs = psmt.executeQuery();

						if(rs.next())
						{
							String windowType = rs.getString("windowtype");
							/* Rimosso per problematica packin
							 * if(windowType == null)
						{
							throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERR_HISTORY_ONLY_TABLE_REGISTRY));
						}
						else*/ 
							if(windowType != null && windowType.equals("T"))
							{
								throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERR_NO_HISTORY_FOR_TABLE_NOT_REGISTRY));
							}
						}
						/*else
					{
						throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERR_HISTORY_ONLY_TABLE_REGISTRY));
					}*/
					}
					finally
					{
						DB.close(rs, psmt);
						rs = null;
						psmt = null;
					}
				}
				
				String historyModeOld = HSTMAD_Table.getHST_HistoryModeOld(mTable);
				if(historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_None) && historyModeOld != null &&
						historyModeOld.equals(HSTMAD_Table.HST_HISTORYMODE_None) == false)
				{
					Query mQuery = new Query(ctx,MColumn.Table_Name,"AD_Table_ID = ?", trxName);
					mQuery.setParameters(mTable.getAD_Table_ID());
					List<MColumn> columns = mQuery.list();
					
					for(MColumn column :columns)
					{
						HSTMAD_Column.setHST_IsHstColumn(column, false);
						column.saveEx(trxName);
					}
				}
				
			}
		}
		else if(nType == TYPE_AFTER_NEW || nType == TYPE_AFTER_CHANGE)
		{
			if(model instanceof MTab)
			{
				MTab mTab = (MTab) model;
				String trxName = model.get_TrxName();
				Properties ctx = model.getCtx();
				// scoletta@ads.it - History Records Plugin
				MTable mTable = MTable.get(ctx, mTab.getAD_Table_ID(), trxName);
				
				String historyMode = HSTMAD_Table.getHST_HistoryMode(mTable);
				
				if((historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_TimeMachine) || 
						historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_Historicizing) ||
							historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_Log)) &&
						mTable.isView())
				{
					throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERR_FOR_VIEW_HISTORY_MUST_BE_VIEW));
				}
				
				if(historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_None)==false &&
						HSTMAD_Table.isHST_IsForceHstApplication(mTable) == false)
				{

					PreparedStatement psmt = null;
					ResultSet rs = null;
					try {
						psmt =DB.prepareStatement(maxWindowType, trxName);
						psmt.setInt(1, mTable.getAD_Table_ID());
						rs = psmt.executeQuery();

						if(rs.next())
						{
							String windowType = rs.getString("windowtype");
							if(windowType == null)
							{
								throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERR_HISTORY_ONLY_TABLE_REGISTRY));
							}
							else if(windowType.equals("T"))
							{
								throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERR_NO_HISTORY_FOR_TABLE_NOT_REGISTRY));
							}
						}
						else
						{
							throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERR_HISTORY_ONLY_TABLE_REGISTRY));
						}
					}
					finally
					{
						DB.close(rs, psmt);
						rs = null;
						psmt = null;
					}

				}
				
				String historyModeOld = HSTMAD_Table.getHST_HistoryModeOld(mTable);
				if(historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_None) && historyModeOld != null &&
						historyModeOld.equals(HSTMAD_Table.HST_HISTORYMODE_None) == false)
				{
					Query mQuery = new Query(ctx,MColumn.Table_Name,"AD_Table_ID = ?", trxName);
					mQuery.setParameters(mTable.getAD_Table_ID());
					List<MColumn> columns = mQuery.list();
					
					for(MColumn column :columns)
					{
						HSTMAD_Column.setHST_IsHstColumn(column, false);
						column.saveEx(trxName);
					}
				}
				
			}
		}
		
		return "";
	}

	@Override
	public String docValidate(PO po, int timing) {
		return null;
	}

	@Override
	public void initialize(ModelValidationEngine engine, MClient mClient) 
	{
		super.initialize(engine, mClient);
		addModelChange(engine, X_AD_Table.Table_Name);
		addModelChange(engine, X_AD_Tab.Table_Name);
	}
}
