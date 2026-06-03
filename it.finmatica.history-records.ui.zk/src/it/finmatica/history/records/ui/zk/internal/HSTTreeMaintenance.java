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
// Spostato dal bundle it.finmatica.history-records.ui (non portato su 13).
// Package rinominato da it.finmatica.history.records.ui.form
// a it.finmatica.history.records.ui.zk.internal per contenimento nel bundle ui.zk.
package it.finmatica.history.records.ui.zk.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.compiere.apps.form.TreeMaintenance;
import org.compiere.model.MRole;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

public class HSTTreeMaintenance extends TreeMaintenance {

	public static final String MSG_CREATE_NEW_TREE = "HST_CREATE_NEW_TREE";
	public static final String MSG_FILTER_TREE = "HST_FILTER_TREE";
	public static final String MSG_ADD_TO_TREE = "HST_ADD_TO_TREE";
	public static final String MSG_REMOVE_FROM_TREE = "HST_REMOVE_FROM_TREE";
	public static final String MSG_REMOVE_FILTER = "HST_REMOVE_FILTER";

	public static KeyNamePair[] getVersionTreeData(int defaultADTreeID)
	{
		StringBuilder sb = new StringBuilder("SELECT AD_Tree_ID, Name ");
		sb.append(" FROM AD_Tree ");
		sb.append(" WHERE IsActive='Y' AND TreeType NOT IN ('BB','PC') ");
		if (defaultADTreeID > 0) {
			sb.append(" and ad_tree_id in ( ");
			sb.append("select t.ad_tree_id ");
			sb.append(" from AD_Tree t  ");
			sb.append(" join AD_Tree d on t.treetype= d.treetype and d.ad_tree_id = ");
			sb.append(defaultADTreeID);
			sb.append(" where ((t.ad_table_id is null and d.ad_table_id is null) or t.ad_table_id = d.ad_table_id) ");
			sb.append(" and (d.treetype not in ('EV','U1','U2','U3','U4') or t.c_element_id=d.c_element_id) ");
			sb.append(" ) ");
		}
		sb.append(" AND HSTIsPrimaryTree = 'Y' ");
		sb.append(" AND AD_Client_ID = " + Env.getAD_Client_ID(Env.getCtx()));
		sb.append(" ORDER BY 2 ");

		KeyNamePair[] versions = DB.getKeyNamePairs(MRole.getDefault().addAccessSQL(sb.toString(),
				"AD_Tree", MRole.SQL_NOTQUALIFIED, MRole.SQL_RO), false);

		// Ordinamento per nome fatto in Java (l'ORDER BY SQL non è affidabile su tutti i DB)
		List<KeyNamePair> versionsList = Arrays.asList(versions);
		Collections.sort(versionsList, new Comparator<KeyNamePair>() {
			@Override
			public int compare(KeyNamePair o1, KeyNamePair o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		return versionsList.toArray(new KeyNamePair[0]);
	}

	public static KeyNamePair[] getTreeData(int parentID)
	{
		StringBuilder sb = new StringBuilder("SELECT AD_Tree_ID, ");
		sb.append(" name||' - '||to_char(hstfromdate, 'dd/MM/yyyy')||' - '||to_char(hsttodate, 'dd/MM/yyyy') as Name ");
		sb.append(" FROM AD_Tree ");
		sb.append(" WHERE IsActive='Y' AND HSTParentTree_ID = ");
		sb.append(parentID);
		sb.append(" AND AD_Client_ID = " + Env.getAD_Client_ID(Env.getCtx()));
		sb.append(" ORDER BY hstfromdate, hsttodate ");

		return DB.getKeyNamePairs(
				MRole.getDefault().addAccessSQL(sb.toString(), "AD_Tree", MRole.SQL_NOTQUALIFIED, MRole.SQL_RO), false);
	}

	public static int getCurrenActiveTreeDataID(int parentID)
	{
		StringBuilder sb = new StringBuilder("SELECT AD_Tree_ID ");
		sb.append(" FROM AD_Tree ");
		sb.append(" WHERE IsActive='Y' AND HSTParentTree_ID = ");
		sb.append(parentID);
		sb.append(" AND AD_Client_ID = " + Env.getAD_Client_ID(Env.getCtx()));
		sb.append(" AND CURRENT_TIMESTAMP between hstfromdate and hsttodate ");
		sb.append(" ORDER BY hstfromdate ");

		return DB.getSQLValue(null,
				MRole.getDefault().addAccessSQL(sb.toString(), "AD_Tree", MRole.SQL_NOTQUALIFIED, MRole.SQL_RO));
	}
}
