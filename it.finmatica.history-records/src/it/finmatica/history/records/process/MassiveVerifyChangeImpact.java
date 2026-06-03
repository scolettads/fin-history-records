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
package it.finmatica.history.records.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MRoleOrgAccess;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

public class MassiveVerifyChangeImpact extends SvrProcess {

	private int p_AD_Client_ID, p_AD_Org_ID, hours;

	private MRoleOrgAccess[] orgsEnabled;

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("AD_Client_ID")) {
				p_AD_Client_ID = ((BigDecimal) para[i].getParameter()).intValue();
			} else if (name.equals("AD_Org_ID")) {
				p_AD_Org_ID = ((BigDecimal) para[i].getParameter()).intValue();
			} else if (name.equals("IntervalHours")) {
				hours = ((BigDecimal) para[i].getParameter()).intValue();
			} else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {

		Properties ctx = getCtx();
		String sTrx = get_TrxName();
		int AD_Role_ID = Env.getAD_Role_ID(ctx);
		int AD_PInstance_ID = getAD_PInstance_ID();
		int AD_User_ID = getAD_User_ID();
		orgsEnabled = MRoleOrgAccess.getOfRole(ctx, AD_Role_ID);

		String sqlTables = "select ad_table_id, tablename from ad_table where hst_historymode in ('S','T')";

		Map<Integer, String> tables = new HashMap<>();
		Map<Integer, List<Map<String, Object>>> params = new HashMap<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sqlTables, sTrx);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				tables.put(rs.getInt(1), rs.getString(2));
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, sqlTables, e);
		} finally {
			DB.close(rs, pstmt);
		}

		for (Map.Entry<Integer, String> table : tables.entrySet()) {

			int tableID = table.getKey();
			String tableName = table.getValue();

			String primaryKey = tableName + "_ID";

			StringBuilder sqlRecord = new StringBuilder();
			sqlRecord.append("select ad_client_id, ad_org_id, ");
			sqlRecord.append(primaryKey);
			sqlRecord.append(" , hstfromdate, hsttodate  from ");
			sqlRecord.append(tableName).append("_hst where created >= ");
			if (DB.isOracle()) {
				sqlRecord.append(" (sysdate - ?/24) ");
			} else if (DB.isPostgreSQL()) {
				sqlRecord.append(" (current_timestamp - replace('X hours','X',to_char(?,'9990'))::interval) ");
			}
			if (p_AD_Client_ID > 0) {
				sqlRecord.append(" AND ad_client_id = ? ");
				if (p_AD_Org_ID > 0) {
					sqlRecord.append(" and AD_Org_ID in (0,?) ");
				} else {
					sqlRecord.append(" and AD_Org_ID in (0");
					for (int i = 1; i <= orgsEnabled.length; i++) {
						sqlRecord.append(",?");
					}
					sqlRecord.append(") ");
				}
			}

			params.put(tableID, new ArrayList<>());

			try {
				pstmt = DB.prepareStatement(sqlRecord.toString(), sTrx);
				int i = 1;
				pstmt.setInt(i++, hours);
				if (p_AD_Client_ID > 0) {
					pstmt.setInt(i++, p_AD_Client_ID);
					if (p_AD_Org_ID > 0) {
						pstmt.setInt(i++, p_AD_Org_ID);
					} else {
						for (MRoleOrgAccess mRoleOrgAccess : orgsEnabled) {
							pstmt.setInt(i++, mRoleOrgAccess.getAD_Org_ID());
						}
					}
				}

				rs = pstmt.executeQuery();
				while (rs.next()) {
					Map<String, Object> parametri = new HashMap<>();
					parametri.put("AD_Client_ID", rs.getInt(1));
					parametri.put("AD_Org_ID", rs.getInt(2));
					// parametri.put("AD_Table_ID", tableID);
					parametri.put("Record_ID", rs.getInt(3));
					parametri.put("DateFrom", rs.getTimestamp(4));
					parametri.put("DateTo", rs.getTimestamp(5));

					params.get(tableID).add(parametri);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, sqlRecord.toString(), e);
			} finally {
				DB.close(rs, pstmt);
			}

		}

		Trx trxTable = null;
		StringBuilder msg = new StringBuilder();

		for (Map.Entry<Integer, List<Map<String, Object>>> table : params.entrySet()) {
			int tableID = table.getKey();
			List<Map<String, Object>> paraList = table.getValue();
			msg.append("Table ");
			msg.append(tables.get(tableID));
			msg.append(": \n");
			if (paraList.isEmpty()){
				msg.append("No records to check; \n");
				continue;
			}
			int recID = 0;
			int checked = 0;
			try {

				trxTable = Trx.get(Trx.createTrxName("MassiveVCI_TableID" + tableID), true);

				for (Map<String, Object> parametri : paraList) {

					int clID = (int) parametri.get("AD_Client_ID");
					int orgID = (int) parametri.get("AD_Org_ID");
					Timestamp dtF = (Timestamp) parametri.get("DateFrom");
					Timestamp dtT = (Timestamp) parametri.get("DateTo");
					recID = (int) parametri.get("Record_ID");

					VerifyChangeImpact.checkImpact(ctx, tableID, clID, orgID, AD_Role_ID, dtF, dtT, recID,
							AD_PInstance_ID, AD_User_ID, true, log, trxTable.getTrxName());
					checked++;
				}

				trxTable.commit();
			} catch (Exception e) {

				if (trxTable != null) {
					trxTable.rollback();
				}

				log.severe("MassiveVCI: TableID " + tableID + ", RecordID " + recID + " ERROR: " + e.getMessage());
			} finally {

				if (trxTable != null) {
					trxTable.close();
				}
			}
			
			msg.append(paraList.size());
			msg.append(" records to check, ");
			msg.append(checked);
			msg.append(" checked; \n");
		}
		
		return msg.toString();
	}

}
