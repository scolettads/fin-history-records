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
package it.finmatica.history.records.ui.zk.internal;

import java.util.Properties;

import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.action.IAction;
import org.adempiere.webui.adwindow.ADWindow;
import org.adempiere.webui.adwindow.ADWindowContent;
import org.adempiere.webui.apps.ProcessModalDialog;
import org.compiere.model.GridTab;
import org.compiere.model.MRole;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Process;
import org.compiere.process.ProcessInfo;
import org.compiere.util.Env;

public abstract class AbstractProcessAction implements IAction
{
	private Properties ctx;
	private int AD_Process_ID = 0;

	protected AbstractProcessAction(String processValue)
	{
		ctx = Env.getCtx();
		Query mQuery = new Query(Env.getCtx(), X_AD_Process.Table_Name, "Value = ?", null);
		mQuery.setParameters(processValue);
		AD_Process_ID = mQuery.firstId();
	}

	/**
	 * Visibilità del toolbar button basata sull'accesso al processo.
	 * Non è @Override perché in vanilla iDempiere 13 IAction non espone
	 * isVisible. Il framework non chiama questo metodo; resta disponibile
	 * per chiamata diretta dal codice del plugin.
	 */
	public boolean isVisible(Object target)
	{
		Boolean isVisible = true;

		if (target instanceof ADWindow)
		{
			isVisible = MRole.getDefault().getProcessAccess(AD_Process_ID);
		}

		return isVisible != null && isVisible.booleanValue();
	}

	@Override
	public void execute(Object target)
	{
		if (target instanceof ADWindow)
		{
			ADWindow win = (ADWindow) target;
			ADWindowContent content = win.getADWindowContent();

			GridTab activeTab = content.getActiveGridTab();
			int table_ID = activeTab.getAD_Table_ID();
			int record_ID = activeTab.getRecord_ID();

			ProcessInfo pi = new ProcessInfo("newProcess", AD_Process_ID, table_ID, record_ID);
			pi.setAD_User_ID(Env.getAD_User_ID(ctx));
			pi.setAD_Client_ID(Env.getAD_Client_ID(ctx));

			int windowNo = content.getWindowNo();
			// scoletta@ads.it - History Records Plugin
			// vanilla 13: ProcessInfo non ha getWindowNo(). Passiamo il windowNo tramite
			// setTransientObject so che CreateHistoryRecord possa recuperarlo per il refresh.
			pi.setTransientObject(windowNo);
			// scoletta@ads.it - History Records Plugin
			// firma vanilla 13: (EventListener, WindowNo, AD_Process_ID, tableId, recordId, String recordUU, boolean autoStart)
			ProcessModalDialog dialog = new ProcessModalDialog(win.getADWindowContent(), windowNo, AD_Process_ID, table_ID, record_ID, null, true);

			if (dialog.isValid())
			{
				dialog.setParent(win.getComponent());
				dialog.setSizable(true);
				dialog.setBorder("normal");
				LayoutUtils.openOverlappedWindow(win.getComponent(), dialog, "middle_center");
			}
		}
	}
}
