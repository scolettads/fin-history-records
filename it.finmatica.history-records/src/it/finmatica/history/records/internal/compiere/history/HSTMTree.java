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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MRole;
import org.compiere.model.MTable;
import org.compiere.model.MTree;
import org.compiere.model.PO;
import org.compiere.model.X_AD_Tree;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class HSTMTree {

	public static final String COLUMNNAME_HSTFromDate = "HSTFromDate";
	public static final String COLUMNNAME_HSTToDate = "HSTToDate";
	public static final String COLUMNNAME_HSTIsPrimaryTree = "HSTIsPrimaryTree";
	public static final String COLUMNNAME_HSTParentTree_ID = "HSTParentTree_ID";
	public static final String COLUMNNAME_C_Element_ID = "C_Element_ID";

	private static CLogger log = CLogger.getCLogger(HSTMTree.class);

	public static Timestamp getHSTFromDate(X_AD_Tree t) throws RuntimeException {
		return (Timestamp) t.get_Value(COLUMNNAME_HSTFromDate);
	}

	public static void setHSTFromDate(X_AD_Tree t, Timestamp HSTFromDate) {
		t.set_ValueOfColumn(COLUMNNAME_HSTFromDate, HSTFromDate);
	}

	public static Timestamp getHSTToDate(X_AD_Tree t) throws RuntimeException {
		return (Timestamp) t.get_Value(COLUMNNAME_HSTToDate);
	}

	public static void setHSTToDate(X_AD_Tree t, Timestamp HSTToDate) {
		t.set_ValueOfColumn(COLUMNNAME_HSTToDate, HSTToDate);
	}

	public static boolean isHSTIsPrimaryTree(X_AD_Tree t) {
		Object oo = t.get_Value(COLUMNNAME_HSTIsPrimaryTree);
		if (oo != null) {
			if (oo instanceof Boolean)
				return ((Boolean) oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	public static void setHSTIsPrimaryTree(X_AD_Tree t, boolean HSTIsPrimaryTree) {
		t.set_ValueOfColumn(COLUMNNAME_HSTIsPrimaryTree, Boolean.valueOf(HSTIsPrimaryTree));
	}

	public static int getHSTParentTree_ID(X_AD_Tree t) {
		Integer ii = (Integer) t.get_Value(COLUMNNAME_HSTParentTree_ID);
		if (ii == null)
			return 0;
		return ii.intValue();
	}

	public static void setHSTParentTree_ID(X_AD_Tree t, int HSTParentTree_ID) {
		if (HSTParentTree_ID < 1)
			t.set_ValueOfColumn(COLUMNNAME_HSTParentTree_ID, null);
		else
			t.set_ValueOfColumn(COLUMNNAME_HSTParentTree_ID, Integer.valueOf(HSTParentTree_ID));
	}

	public static List<PO> getTreeNodes(MTree t, int AD_User_ID, boolean onlyActive) {
		Properties ctx = t.getCtx();
		String sTrx = t.get_TrxName();
		List<PO> list = new ArrayList<>();
		
		// code copied from MTree.loadNodes()
		StringBuilder sql = new StringBuilder();
		if (t.getTreeType().equals(X_AD_Tree.TREETYPE_Menu))
		// specific sql, need to load TreeBar
		{
			sql.append("SELECT tn.* FROM ").append(t.getNodeTableName())
					.append(" tn LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID")
					.append(" AND tn.Node_ID=tb.Node_ID AND tb.IsFavourite = 'Y'")
					.append(AD_User_ID != -1 ? " AND tb.AD_User_ID=? " : "");
			// #1 (conditional)
			sql.append(") WHERE tn.AD_Tree_ID=? "); // #2
			if (onlyActive)
				sql.append(" AND tn.IsActive='Y' ");
			sql.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		} else {
			String sourceTableName = MTree.getSourceTableName(t.getTreeType());
			if (sourceTableName == null) {
				if (t.getAD_Table_ID() > 0)
					sourceTableName = MTable.getTableName(ctx, t.getAD_Table_ID());
			}
			sql.append("SELECT tn.* FROM ").append(sourceTableName).append(" st LEFT OUTER JOIN ")
					.append(t.getNodeTableName())
					.append(" tn ON (tn.Node_ID=st.").append(sourceTableName).append("_ID) WHERE tn.AD_Tree_ID=?"); // #2
			if (onlyActive)
				sql.append(" AND tn.IsActive='Y' ");
			sql.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
			sql = new StringBuilder(
					MRole.getDefault().addAccessSQL(sql.toString(), "st", MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO));
			// SQL_RO for Org_ID = 0
		}
		if (log.isLoggable(Level.FINEST))
			log.finest(sql.toString());
		// The Node Loop
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			//
			pstmt = DB.prepareStatement(sql.toString(), sTrx);
			int idx = 1;
			if (AD_User_ID != -1 && t.getTreeType().equals(MTree.TREETYPE_Menu))
				pstmt.setInt(idx++, AD_User_ID);
			pstmt.setInt(idx++, t.getAD_Tree_ID());
			// Get Tree
			rs = pstmt.executeQuery();
			while (rs.next()) {
				// scoletta@ads.it - History Records Plugin
				PO po = MTable.get(ctx, t.getNodeTableName()).getPO(rs, sTrx);
				list.add(po);
			}

		} catch (SQLException e) {
			log.log(Level.SEVERE, sql.toString(), e);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		return list;
	}

	public static int getC_Element_ID(X_AD_Tree t) {
		Integer ii = (Integer) t.get_Value(COLUMNNAME_C_Element_ID);
		if (ii == null)
			return 0;
		return ii.intValue();
	}

	public static void setC_Element_ID(X_AD_Tree t, int C_Element_ID) {
		if (C_Element_ID < 1)
			t.set_ValueOfColumn(COLUMNNAME_C_Element_ID, null);
		else
			t.set_ValueOfColumn(COLUMNNAME_C_Element_ID, Integer.valueOf(C_Element_ID));
	}
}
