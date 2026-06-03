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
// - IInfoFactory2 non esiste → implementa IInfoFactory.
// - create(int,int) e create(int,int,String) erano di IInfoFactory2 → non più @Override.
// - create(WindowNo,...,GridField,AD_PInstance_ID) → rimosso AD_PInstance_ID (non in IInfoFactory).
// - Aggiunti i 2 overload con SQLFragment richiesti da IInfoFactory vanilla 13.
package it.finmatica.history.records.ui.zk.extension;

import org.adempiere.webui.factory.IInfoFactory;
import org.adempiere.webui.info.InfoWindow;
import org.adempiere.webui.panel.InfoPanel;
import org.compiere.model.GridField;
import org.compiere.model.Lookup;
import org.compiere.model.MInfoWindow;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.idempiere.db.util.SQLFragment;

import it.finmatica.history.records.ui.zk.info.VerifyChangeImpactInfoWindow;

public class HSTInfoFactory implements IInfoFactory
{
	private static final String Q_INFOWINDOW = "SELECT AD_InfoWindow_ID FROM AD_Process WHERE Value = 'HST_VerifyChangeImpact'";

	public boolean isVerifyChangeImpactInfoWindow(int AD_InfoWindow_ID)
	{
		boolean bIsIWindow = false;

		if (AD_InfoWindow_ID > 0)
		{
			int ProcessInfoWindowID = DB.getSQLValue(null, Q_INFOWINDOW);
			bIsIWindow = (ProcessInfoWindowID == AD_InfoWindow_ID);
		}

		return bIsIWindow;
	}

	@Override
	public InfoPanel create(int WindowNo, String tableName, String keyColumn,
			String value, boolean multiSelection, String whereClause, int AD_InfoWindow_ID, boolean lookup)
	{
		return create(WindowNo, tableName, keyColumn, value, multiSelection, whereClause, AD_InfoWindow_ID, lookup, null);
	}

	@Override
	public InfoPanel create(Lookup lookup, GridField field, String tableName,
			String keyColumn, String value, boolean multiSelection,
			String whereClause, int AD_InfoWindow_ID)
	{
		return create(-1, tableName, keyColumn, value, multiSelection, whereClause, AD_InfoWindow_ID, true, field);
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * Nuovo overload richiesto da IInfoFactory vanilla 13 (con SQLFragment).
	 */
	@Override
	public InfoPanel create(Lookup lookup, GridField field, String tableName,
			String keyColumn, String value, boolean multiSelection,
			int AD_InfoWindow_ID, SQLFragment sqlFilter)
	{
		return null; // HST non usa SQLFragment; non gestito
	}

	@Override
	public InfoWindow create(int AD_InfoWindow_ID)
	{
		return create(AD_InfoWindow_ID, 0);
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * Era @Override di IInfoFactory2.create(int,int). In vanilla 13 IInfoFactory
	 * non ha questo metodo → mantenuto come metodo pubblico ordinario.
	 */
	public InfoWindow create(int AD_InfoWindow_ID, int AD_PInstance_ID)
	{
		return create(AD_InfoWindow_ID, AD_PInstance_ID, null);
	}

	@Override
	public InfoPanel create(int WindowNo, String tableName, String keyColumn,
			String value, boolean multiSelection, String whereClause,
			int AD_InfoWindow_ID, boolean lookup, GridField gridField)
	{
		if (isVerifyChangeImpactInfoWindow(AD_InfoWindow_ID))
		{
			return new VerifyChangeImpactInfoWindow(WindowNo, tableName, keyColumn, value,
					multiSelection, whereClause, AD_InfoWindow_ID, lookup, gridField);
		}
		return null;
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * Nuovo overload richiesto da IInfoFactory vanilla 13 (con SQLFragment).
	 */
	@Override
	public InfoPanel create(int WindowNo, String tableName, String keyColumn,
			String value, boolean multiSelection,
			int AD_InfoWindow_ID, boolean lookup, GridField field, SQLFragment sqlFilter)
	{
		return null; // HST non usa SQLFragment; non gestito
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * Era @Override di IInfoFactory2.create(int,int,String). In vanilla 13
	 * IInfoFactory non ha questo metodo → mantenuto come metodo pubblico ordinario.
	 */
	public InfoWindow create(int AD_InfoWindow_ID, int AD_PInstance_ID, String value)
	{
		if (isVerifyChangeImpactInfoWindow(AD_InfoWindow_ID))
		{
			MInfoWindow infoWindow = new MInfoWindow(Env.getCtx(), AD_InfoWindow_ID, (String) null);
			String tableName = infoWindow.getAD_Table().getTableName();
			String keyColumn = tableName + "_ID";
			InfoPanel info = create(-1, tableName, keyColumn, value, false, null, AD_InfoWindow_ID, false, null);
			if (info instanceof InfoWindow)
				return (InfoWindow) info;
			else
				return null;
		}
		return null;
	}
}
