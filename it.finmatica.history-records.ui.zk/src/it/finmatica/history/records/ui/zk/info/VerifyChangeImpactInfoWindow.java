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
// scoletta@ads.it - History Records Plugin
// Adattato per vanilla iDempiere 13:
// - I costruttori con whereClause String esistono ancora (deprecated) → mantenuti.
// - Il 4° costruttore (con AD_PInstance_ID) non ha equivalente in InfoWindow vanilla 13:
//   AD_PInstance_ID ignorato, delega al costruttore a 9 argomenti (deprecated).
package it.finmatica.history.records.ui.zk.info;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.info.InfoWindow;
import org.compiere.model.GridField;
// scoletta@ads.it - History Records Plugin
// vanilla 13: PO.get(ctx, tableName, id, trxName) era utility Finmatica nel core 8.2,
// non presente in vanilla. Sostituita con MTable.get(ctx, tableName).getPO(id, trxName).
import org.compiere.model.MTable;
import org.compiere.util.Env;

import it.finmatica.history.records.model.I_T_HSTCheckResults;

public class VerifyChangeImpactInfoWindow extends InfoWindow
{
	private static final long serialVersionUID = 5030826587179431719L;

	@SuppressWarnings("deprecation")
	public VerifyChangeImpactInfoWindow(int WindowNo, String tableName,
			String keyColumn, String queryValue, boolean multipleSelection,
			String whereClause, int AD_InfoWindow_ID)
	{
		super(WindowNo, tableName, keyColumn, queryValue, multipleSelection,
				whereClause, AD_InfoWindow_ID);
	}

	@SuppressWarnings("deprecation")
	public VerifyChangeImpactInfoWindow(int WindowNo, String tableName,
			String keyColumn, String queryValue, boolean multipleSelection,
			String whereClause, int AD_InfoWindow_ID, boolean lookup)
	{
		super(WindowNo, tableName, keyColumn, queryValue, multipleSelection,
				whereClause, AD_InfoWindow_ID, lookup);
	}

	@SuppressWarnings("deprecation")
	public VerifyChangeImpactInfoWindow(int WindowNo, String tableName,
			String keyColumn, String queryValue, boolean multipleSelection,
			String whereClause, int AD_InfoWindow_ID, boolean lookup, GridField field)
	{
		super(WindowNo, tableName, keyColumn, queryValue, multipleSelection,
				whereClause, AD_InfoWindow_ID, lookup, field);
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * vanilla 13: InfoWindow non ha costruttore con AD_PInstance_ID.
	 * Il parametro AD_PInstance_ID è ignorato; si delega al costruttore
	 * con whereClause (deprecated ma ancora presente).
	 */
	@SuppressWarnings("deprecation")
	public VerifyChangeImpactInfoWindow(int WindowNo, String tableName,
			String keyColumn, String queryValue, boolean multipleSelection,
			String whereClause, int AD_InfoWindow_ID, boolean lookup, GridField field,
			int AD_PInstance_ID)
	{
		super(WindowNo, tableName, keyColumn, queryValue, multipleSelection,
				whereClause, AD_InfoWindow_ID, lookup, field);
		// AD_PInstance_ID non disponibile nel costruttore InfoWindow vanilla 13
	}

	@Override
	public void zoom()
	{
		Integer recordId = contentPanel.getSelectedRowKey();
		if (recordId == null)
			return;

		I_T_HSTCheckResults checkResult = (I_T_HSTCheckResults) MTable.get(Env.getCtx(), I_T_HSTCheckResults.Table_Name).getPO(recordId, null);

		AEnv.zoom(checkResult.getHST_DestTable_ID(), checkResult.getHST_Record_ID());
	}
}
