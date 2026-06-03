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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MDocType;
import org.compiere.model.MMessage;
import org.compiere.model.MNote;
import org.compiere.model.MRoleOrgAccess;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Table;
import it.finmatica.history.records.internal.compiere.history.DateSourceUtil;
import it.finmatica.history.records.internal.compiere.history.HDB;
import it.finmatica.history.records.internal.compiere.history.HistorySelectionData;
import it.finmatica.history.records.internal.compiere.history.TableAndField;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;

import it.finmatica.history.records.model.HSTMTable;
import it.finmatica.history.records.model.HSTPO;
import it.finmatica.history.records.model.MHSTCheckResults;
import it.finmatica.history.records.model.MHSTUpdCheckConf;
import it.finmatica.history.records.util.HSTMessages;
// scoletta@ads.it - History Records Plugin
// rimosso import it.idempiere.util.EnvHelper, inline di createDummyWindowNo

public class VerifyChangeImpact extends SvrProcess {
	
	private static final Timestamp FAR_FUTURE_DATE;
	
	static
	{
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(3000, 11, 31); // 31/12/3000
		FAR_FUTURE_DATE = new Timestamp(cal.getTimeInMillis());
	}

	private int p_AD_Client_ID = -1, p_AD_Org_ID = -1, p_AD_Table_ID = -1, p_Record_ID = -1;
	private Timestamp p_DateFrom, p_DateTo;
	private boolean p_IsCreateNote, p_IsOpenInfo;

	private static final String KEY_COL_RECORD_ID = "Record_ID", KEY_COL_DATEFROM = "DateFrom",
			KEY_COL_DOCTYPE = "C_DocType_ID", KEY_COL_DOCTYPETARGET = "C_DocTypeTarget_ID", KEY_COL_DOCNO = "DocumentNo", KEY_COL_DOCDATE = "DocumentDate",
			KEY_COL_VATNO = "VATLEdgerNo", KEY_COL_VATDATE = "VATLedgerDate", KEY_COL_DESCR = "Message";

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
			} else if (name.equals("AD_Table_ID")) {
				p_AD_Table_ID = ((BigDecimal) para[i].getParameter()).intValue();
			} else if (name.equals("Record_ID")) {
				p_Record_ID = ((BigDecimal) para[i].getParameter()).intValue();
			} else if (name.equals("DateFrom")) {
				p_DateFrom = (Timestamp) para[i].getParameter();
			} else if (name.equals("DateTo")) {
				p_DateTo = (Timestamp) para[i].getParameter();
			} else if (name.equals("IsCreateNote")) {
				p_IsCreateNote = "Y".equals(para[i].getParameter());
			} else if (name.equals("IsOpenInfo")) {
				p_IsOpenInfo = "Y".equals(para[i].getParameter());
			} else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {

		Properties ctx = getCtx();
		String sTrx = get_TrxName();
		
		if(p_AD_Table_ID < 0)
			p_AD_Table_ID = getProcessInfo().getTable_ID();
		
		if(p_Record_ID < 0)
			p_Record_ID = getProcessInfo().getRecord_ID();
		
		checkImpact(ctx, p_AD_Table_ID, p_AD_Client_ID, p_AD_Org_ID, Env.getAD_Role_ID(ctx), p_DateFrom, p_DateTo, p_Record_ID, 
				getAD_PInstance_ID(), getAD_User_ID(), p_IsCreateNote, log, sTrx);

		return "Execution completed";
	}

	private static void setDeafultParams(PreparedStatement pstmt_sub, MHSTUpdCheckConf ucc, boolean sourceHST, boolean destHST,
			HistorySelectionData hsd, int Record_ID, int AD_Client_ID, int AD_Org_ID, MRoleOrgAccess[] orgsEnabled,
			Timestamp DateFrom, Timestamp DateTo) throws SQLException {

		int i = 1;

		pstmt_sub.setInt(i++, Record_ID);
		pstmt_sub.setInt(i++, AD_Client_ID);
		if (AD_Org_ID > 0) {
			pstmt_sub.setInt(i++, AD_Org_ID);
		} else {
			for (MRoleOrgAccess mRoleOrgAccess : orgsEnabled) {
				pstmt_sub.setInt(i++, mRoleOrgAccess.getAD_Org_ID());
			}
		}

		if (destHST || hsd != null) {
			pstmt_sub.setTimestamp(i++, DateFrom);
			if (DateTo != null) {
				pstmt_sub.setTimestamp(i++, DateTo);
			}
		}

	}

	private static String getDefaultVerifyQuery(int AD_Org_ID, MHSTUpdCheckConf ucc, String sourceTableName, String destTableName,
			boolean sourceHST, boolean destHST, HistorySelectionData hsd, MRoleOrgAccess[] orgsEnabled, Timestamp DateTo) {
		StringBuilder rule = new StringBuilder();
		
		if(DateTo == null)
			DateTo = FAR_FUTURE_DATE;

		rule.append("select *  from ").append(destTableName).append(" where isactive = 'Y' and ")
				.append(sourceTableName).append("_ID = ? and AD_Client_ID in (0,?) ");

		if (AD_Org_ID > 0) {
			rule.append(" and AD_Org_ID in (0,?) ");
		} else {
			rule.append(" and AD_Org_ID in (0");
			for (int i = 1; i <= orgsEnabled.length; i++) {
				rule.append(",?");
			}
			rule.append(") ");
		}

		if (destHST) {
			rule.append(" and ");
			rule.append(HSTPO.COLUMNNAME_HSTToDate);
			rule.append(" >= ? ");
			if (DateTo != null) {
				rule.append(" and ");
				rule.append(HSTPO.COLUMNNAME_HSTFromDate);
				rule.append(" <= ? ");
			}
		}

		else if (hsd != null) {
			rule.append(" and (");
			
			if(hsd.getTableAndField() != null)
			{
				TableAndField taf = hsd.getParsedTableAndField();
				
				rule.append(destTableName)
					.append(DateSourceUtil.TABLEFIELD_SEP)
					.append(taf.getField());
			}
			else
			{
				rule.append(DateSourceUtil.replaceInSubquery(hsd.getSubQuery(), destTableName));
			}
			
			rule.append(") ");
			if (DateTo != null) {
				rule.append(" between ? and ? ");
			} else {
				rule.append(" >= ? ");
			}
		}

		return rule.toString();
	}

	// scoletta@ads.it - History Records Plugin
	// openInfo() era un metodo Finmatica aggiunto a SvrProcess nel core modificato 8.2.
	// Non esiste in vanilla iDempiere 13 — metodo rimosso. Se serve il comportamento
	// (apertura automatica InfoWindow a fine processo), si implementera' via AD_Process.IsDirectPrint
	// o overridando postProcess(boolean) che e' l'hook vanilla standard.

	public static void checkImpact(Properties ctx, int AD_Table_ID, int AD_Client_ID, 
			int AD_Org_ID, int AD_Role_ID, Timestamp DateFrom, Timestamp DateTo, int Record_ID, int AD_PInstance_ID, 
			int AD_User_ID, boolean IsCreateNote, CLogger logger, String sTrx){
		
		if(DateTo == null)
		{
			DateTo = FAR_FUTURE_DATE;
		}
		
		Properties paramCtx = new Properties(ctx);
		// scoletta@ads.it - History Records Plugin
		// inline di EnvHelper.createDummyWindowNo: genera WindowNo negativo univoco
		int winNo = -new java.util.Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE);
			
		//create context for params
		Env.setContext(paramCtx, winNo, "AD_Client_ID", AD_Client_ID);
		Env.setContext(paramCtx, winNo, "AD_Org_ID", AD_Org_ID);
		Env.setContext(paramCtx, winNo, "AD_Table_ID", AD_Table_ID);
		Env.setContext(paramCtx, winNo, "Record_ID", Record_ID);
		Env.setContext(paramCtx, winNo, "DateFrom", DB.TO_DATE(DateFrom));
		if (DateTo != null) {
			Env.setContext(paramCtx, winNo, "DateTo", DB.TO_DATE(DateTo));
		}
		
		StringBuilder sb = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		MRoleOrgAccess[] orgsEnabled = MRoleOrgAccess.getOfRole(ctx, AD_Role_ID);

		sb.append("AD_Table_ID=?");
		params.add(AD_Table_ID);
		sb.append(" AND ");
		sb.append("AD_Client_ID IN (0,?)");
		params.add(AD_Client_ID);
		sb.append(" AND ");
		sb.append("AD_Org_ID IN (0,?)");
		params.add(AD_Org_ID);
		
		//retrieve destTables linked with source
		Query q = new Query(ctx, MHSTUpdCheckConf.Table_Name, sb.toString(), sTrx).setParameters(params)
				.setOnlyActiveRecords(true);

		List<MHSTUpdCheckConf> list = q.list();
		Properties resultCtx = new Properties(ctx);

		// loop for each table
		for (MHSTUpdCheckConf ucc : list) {
			String rule = null;
			PreparedStatement pstmt_sub = null;
			ResultSet rs_sub = null;
			boolean isQueryDefault = false;
			String destTableName = "";
			int destTableNameID = 0;
			boolean destHST = false;
			String destPK = null;
			try {
				if (!Util.isEmpty(ucc.getHST_CheckQuery())) {
					if (ucc.getHST_CheckQuery().indexOf("@") > 0) {
						rule = Env.parseContext(paramCtx, winNo, ucc.getHST_CheckQuery(), false, true);
					} else {
						rule = ucc.getHST_CheckQuery();
					}

					pstmt_sub = HDB.prepareStatement(HistorySelectionData.DISABLE_TIMEMACHINE, rule, sTrx);

				} else {//if rule is missing create it with default structure
					boolean sourceHST = HSTMTable.isStoricizzata((X_AD_Table) ucc.getAD_Table());
					String sourceTableName = ucc.getAD_Table().getTableName();
					destHST = HSTMTable.isStoricizzata((X_AD_Table) ucc.getHST_DestTable());
					destTableName = ucc.getHST_DestTable().getTableName();

					if (destHST) {
						destTableName = destTableName + "_HST";
					}
					
					destTableNameID = MTable.getTable_ID(destTableName);

					HistorySelectionData hsd = null;

					// costruire la rule
					if (!destHST) {
						hsd = DateSourceUtil.fromSourceTable(ucc.getHST_DestTable().getTableName(), AD_Client_ID);						
					}
					
					destPK = destTableName + "_ID";
					
					rule = getDefaultVerifyQuery(AD_Org_ID, ucc, sourceTableName, destTableName, sourceHST, destHST, hsd, orgsEnabled, DateTo);
					pstmt_sub = HDB.prepareStatement(HistorySelectionData.DISABLE_TIMEMACHINE, rule, sTrx);
					setDeafultParams(pstmt_sub, ucc, sourceHST, destHST, hsd, Record_ID, AD_Client_ID, AD_Org_ID, orgsEnabled, DateFrom, DateTo);
					isQueryDefault = true;
				}

				rs_sub = pstmt_sub.executeQuery();

				ResultSetMetaData rsmeta = rs_sub.getMetaData();
				DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Env.getLanguage(ctx).getLocale());

				int iColumns = rsmeta.getColumnCount();
				List<String> columns = new ArrayList<String>();
				for (int col = 1; col <= iColumns; col++) {
					String colName = rsmeta.getColumnLabel(col).toLowerCase();
					columns.add(colName);
				}

				while (rs_sub.next()) {

					resultCtx.clear();

					for (String colName : columns) {
						Object val = rs_sub.getObject(colName);
						if (val != null) {

							if (isQueryDefault) {
								if (colName.equals(destPK.toLowerCase())) {
									colName = KEY_COL_RECORD_ID.toLowerCase();
								} else if (colName.equals("documentno") || colName.equals("value")) {
									colName = KEY_COL_DOCNO.toLowerCase();
								} else if (colName.equals("documentdate") || colName.equals("dateordered")
										|| colName.equals("dateinvoiced")) {
									colName = KEY_COL_DOCDATE.toLowerCase();
								}
								if (destHST && colName.equals("hstfromdate")) {
									colName = KEY_COL_DATEFROM.toLowerCase();
								}
							}

							if (val instanceof Timestamp) {
								Timestamp tsVal = (Timestamp)val;
								Env.setContext(resultCtx, winNo, colName, df.format(tsVal));
							} else {
								Env.setContext(resultCtx, winNo, colName, val.toString());
							}
						}
					}

					if (!columns.contains("datefrom")) {
						Env.setContext(resultCtx, winNo, "datefrom", df.format(DateFrom));
					}

					// scoletta@ads.it - History Records Plugin
					// PO.create(ctx,tableName,trx) era Finmatica custom; vanilla: new M-class(ctx,0,trx)
					MHSTCheckResults tmp = new MHSTCheckResults(ctx, 0, sTrx);
					tmp.setAD_PInstance_ID(AD_PInstance_ID);
					tmp.setAD_Table_ID(AD_Table_ID);
					tmp.setRecord_ID(Record_ID);
					tmp.setDateFrom(DateFrom);
					if (destTableNameID > 0) {
						tmp.setHST_DestTable_ID(destTableNameID);
					} else {
						tmp.setHST_DestTable_ID(ucc.getHST_DestTable_ID());
					}
					tmp.setHST_Record_ID(Env.getContextAsInt(resultCtx, winNo, KEY_COL_RECORD_ID.toLowerCase()));
					StringBuilder description = new StringBuilder();
					String separator = " ";
					if (columns.contains(KEY_COL_DESCR.toLowerCase())) {
						description.append(Env.getContext(resultCtx, winNo, KEY_COL_DESCR.toLowerCase()));
					} else {
						if (columns.contains(KEY_COL_DOCNO.toLowerCase())) {
							if (!Util.isEmpty(description.toString())) {
								description.append(separator);
							}
							description.append(Env.getContext(resultCtx, winNo, KEY_COL_DOCNO.toLowerCase()));
						}

						if (columns.contains(KEY_COL_VATNO.toLowerCase())) {
							if (!Util.isEmpty(description.toString())) {
								description.append(separator);
							}
							description.append(Env.getContext(resultCtx, winNo, KEY_COL_VATNO.toLowerCase()));
						}

						if (columns.contains(KEY_COL_DOCDATE.toLowerCase())) {
							if (!Util.isEmpty(description.toString())) {
								description.append(separator);
							}
							description.append(Env.getContext(resultCtx, winNo, KEY_COL_DOCDATE.toLowerCase()));
						}

						if (columns.contains(KEY_COL_VATDATE.toLowerCase())) {
							if (!Util.isEmpty(description.toString())) {
								description.append(separator);
							}
							description.append(Env.getContext(resultCtx, winNo, KEY_COL_VATDATE.toLowerCase()));
						}
						
						boolean docTypeResolved = false;

						if (columns.contains(KEY_COL_DOCTYPE.toLowerCase())) {
							if (!Util.isEmpty(description.toString())) {
								description.append(separator);
							}
							
							int C_DocType_ID = Env.getContextAsInt(resultCtx, winNo, KEY_COL_DOCTYPE.toLowerCase());

							if(C_DocType_ID > 0)
							{
								MDocType dt = new MDocType(ctx, C_DocType_ID, sTrx);
								description.append(dt.getNameTrl());
								docTypeResolved = true;
							}
						}
						
						// Scaliamo sul doc type target se il doc type non c'e' o non e' valorizzato
						
						if (docTypeResolved == false && columns.contains(KEY_COL_DOCTYPETARGET.toLowerCase())) {
							if (!Util.isEmpty(description.toString())) {
								description.append(separator);
							}
							
							int C_DocType_ID = Env.getContextAsInt(resultCtx, winNo, KEY_COL_DOCTYPETARGET.toLowerCase());

							if(C_DocType_ID > 0)
							{
								MDocType dt = new MDocType(ctx, C_DocType_ID, sTrx);
								description.append(dt.getNameTrl());
							}
						}

						if (Util.isEmpty(description.toString())) {
							description
									.append(destTableName + separator + tmp.getHST_Record_ID());
						}
					}
					tmp.setDescription(description.toString());

					int userID = 0;
					if (!Util.isEmpty(ucc.getHST_CheckUserDef())) {
						if (ucc.getHST_CheckUserDef().indexOf("@") > 0) {
							rule = Env.parseContext(resultCtx, winNo, ucc.getHST_CheckUserDef().toLowerCase(), false,
									true);
						} else {
							rule = ucc.getHST_CheckQuery();
						}

						userID = DB.getSQLValue(sTrx, rule);

						if (userID <= 0) {
							throw new AdempiereException(
									"HST_CheckUserDef not returning valid AD_User_ID. SQL[" + rule + "]");
						}

					} else {
						userID = AD_User_ID;
					}

					tmp.setAD_User_ID(userID);

					tmp.saveEx();

					if (IsCreateNote) {
						// scoletta@ads.it - History Records Plugin
						MNote note = new MNote(ctx, 0, sTrx);
						note.setAD_User_ID(tmp.getAD_User_ID());
						note.setAD_Message_ID(MMessage.get(ctx, HSTMessages.ADNOTE_MSG).get_ID());
						note.setAD_Table_ID(tmp.getHST_DestTable_ID());
						note.setRecord_ID(tmp.getHST_Record_ID());
						note.setDescription(Msg.getMsg(ctx, HSTMessages.ADNOTE_MSG));
						note.setTextMsg(tmp.getDescription());

						note.saveEx();
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, rule, e);
				throw new AdempiereException(e);
			} finally {
				DB.close(rs_sub, pstmt_sub);
			}

		}
	}

}
