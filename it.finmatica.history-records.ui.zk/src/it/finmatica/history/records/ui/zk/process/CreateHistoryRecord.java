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
package it.finmatica.history.records.ui.zk.process;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.adempiere.model.MBroadcastMessage;
import org.adempiere.util.Callback;
import org.adempiere.webui.adwindow.ADWindow;
import org.adempiere.webui.session.SessionManager;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DisplayType;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.compiere.util.TrxRunnable;
import org.idempiere.broadcast.BroadcastMsgUtil;

import it.finmatica.history.records.model.HSTPO;
import it.finmatica.history.records.model.HSTPO.RecordHST;
import it.finmatica.history.records.util.HSTMessages;
// scoletta@ads.it - History Records Plugin
// rimosso import it.idempiere.util.ProcessHelper, sostituito RETURN_OK con stringa standard "@Ok@"

public class CreateHistoryRecord extends SvrProcess{

	private Timestamp p_Date = null;
	private int AD_Table_ID = 0;
	private SimpleDateFormat df = DisplayType.getDateFormat(DisplayType.Date); 

	@Override
	protected void prepare() 
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("Date"))
			{
				p_Date = (Timestamp)para[i].getParameter();
			}
		}
		AD_Table_ID = getTable_ID();
	}

	@Override
	protected String doIt() throws Exception 
	{
		String trxName = get_TrxName();
		Properties ctx = getCtx();

		MTable mTable = MTable.get(ctx, AD_Table_ID, trxName);
		// scoletta@ads.it - History Records Plugin
		PO poToHistory = MTable.get(ctx, mTable.get_ValueAsString(MTable.COLUMNNAME_TableName)).getPO(getRecord_ID(), trxName);
		
		RecordHST recordHST = new RecordHST(poToHistory,null);
		
		String tableNameHST = recordHST.tableNameHST;
		String columnNameHST = recordHST.columnNameLinkHSTRecord;
		
		HSTPO.checkDateFrom(ctx, recordHST, getRecord_ID(), p_Date, trxName);
		
		StringBuilder whereClause = new StringBuilder(HSTPO.COLUMNNAME_HSTFromDate).append(" > ? and ").append(columnNameHST).append(" = ? ");
		Query mQuery = new Query(ctx, tableNameHST, whereClause.toString(), trxName);
		mQuery.setParameters(p_Date,getRecord_ID());
		List<PO> pos = mQuery.list();
		
		if(pos.size() > 0)
		{
			Callback<Boolean> callback = new Callback<Boolean>() 
			{
				@Override
				public void onCallback(Boolean result) 
				{
					if(result)
					{
						PO recordToHistory = MTable.get(ctx, recordHST.tableNameFrom).getPO(getRecord_ID(), trxName); // refactor 2026-05-18
						continueProcess(p_Date,recordToHistory,null);
						// scoletta@ads.it - History Records Plugin
						// vanilla 13: ProcessInfo non ha getWindowNo(). Il windowNo viene passato
						// tramite setTransientObject da AbstractProcessAction.execute().
						Object transientObj = getProcessInfo().getTransientObject();
						int windowNo = (transientObj instanceof Integer) ? (Integer) transientObj : 0;
						Object window = (windowNo > 0) ? SessionManager.getAppDesktop().findWindow(windowNo) : null;
						if(window instanceof ADWindow)
						{
							ADWindow adWindow = (ADWindow)window;
							adWindow.getADWindowContent().onRefresh();
						}
					}
					else
						return ;
				}
			};
			
			StringBuilder sb = new StringBuilder();
			
			for(PO po:pos)
			{
				Date datetoFormat = new Date(HSTPO.getHSTFromDate(po).getTime());
				sb.append(" \n").append(df.format(datetoFormat));
			}
			if(processUI!=null){
				// PROCESSO CHIAMATO DA INTERFACCIA ( chiedo conferma )
				processUI.ask(Msg.getMsg(ctx, HSTMessages.HST_EXIST_ANOTHER_HISTORY_RECORDS_WITH_NEXT_DATE)+sb.toString(), callback);
			}else{
				// PROCESSO CHIAMATO DA JAVA ( avviso )
				PO recordToHistory = MTable.get(ctx, recordHST.tableNameFrom).getPO(getRecord_ID(), trxName); // refactor 2026-05-18
				continueProcess(p_Date,recordToHistory,null);

				MBroadcastMessage msg = new MBroadcastMessage(ctx, 0, null);
				msg.setBroadcastMessage(Msg.getMsg(ctx, HSTMessages.HST_EXIST_ANOTHER_HISTORY_RECORDS_WITH_NEXT_DATE)+sb.toString());
				msg.setBroadcastType(MBroadcastMessage.BROADCASTTYPE_Immediate);
				msg.setTarget(MBroadcastMessage.TARGET_User);
				msg.setBroadcastFrequency(MBroadcastMessage.BROADCASTFREQUENCY_JustOnce);
				msg.setAD_User_ID(getAD_User_ID());
				msg.save();

				BroadcastMsgUtil.publishBroadcastMessage(msg.get_ID(), null);
			}
				
		}
		else
		{
			PO recordToHistory = MTable.get(ctx, recordHST.tableNameFrom).getPO(getRecord_ID(), trxName); // refactor 2026-05-18
			continueProcess(p_Date,recordToHistory,trxName);
		}
		
		// scoletta@ads.it - History Records Plugin
		return "@Ok@";
	}
	
	public void continueProcess(final Timestamp p_Date,PO recordToHistory,final String trxName)
	{
		Trx.run(trxName, new TrxRunnable() 
		{
			@Override
			public void run(String trxName) 
			{
				/*PO historyRecord = HSTPO.createHSTRecord(recordFromCopy,p_Date,false);
				
				HSTPO.validateHistoryRecord(historyRecord,getCtx(),recordHST,p_Date,trxName,true,true);
				*/
				HSTPO.createAndInsertHSTRecord(recordToHistory, p_Date, true, true,trxName);
			}
		});
	}
	
}
