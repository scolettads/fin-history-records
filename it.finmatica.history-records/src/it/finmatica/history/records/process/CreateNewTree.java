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

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MTable;
import org.compiere.model.MTree;
import org.compiere.model.PO;
import it.finmatica.history.records.internal.compiere.history.HSTMTree;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.ServerProcessCtl;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.compiere.util.Util;

public class CreateNewTree extends SvrProcess {

	private boolean p_IsNewVersion, p_CopySourceTree, p_IsAllNodes;
	private Timestamp p_DateFrom;
	private String p_Name;
	private static final String MSG_MISSING_VRS_NAME = "HST_ERR_MISSING_VRS_NAME";
	private static final String VERIFIY_TREE_PROCESS = "AD_Tree Verify";


	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("IsNewVersion")) {
				p_IsNewVersion = "Y".equals(para[i].getParameter());
			} else if (name.equals("DateFrom")) {
				p_DateFrom = (Timestamp) para[i].getParameter();
			} else if (name.equals("Name")) {
				p_Name = (String) para[i].getParameter();
			} else if (name.equals("CopySourceTree")) {
				p_CopySourceTree = "Y".equals(para[i].getParameter());
			} else if (name.equals("IsAllNodes")) {
				p_IsAllNodes = "Y".equals(para[i].getParameter());
			}else 
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {

		Properties ctx = getCtx();
		String sTrx = get_TrxName();
		
		if (p_IsNewVersion && Util.isEmpty(p_Name)){
			throw new AdempiereException(Msg.getMsg(ctx, MSG_MISSING_VRS_NAME));
		}
		
		int sourceTreeID = getRecord_ID();
		MTree source = new MTree(ctx, sourceTreeID, sTrx);
		MTree sourceParent = new MTree(ctx, HSTMTree.getHSTParentTree_ID(source), sTrx);
		MTree dest = new MTree(ctx, 0, sTrx);
		
		PO.copyValues(source, dest);
		//not copied with PO.copyValues
		dest.setAD_Org_ID(source.getAD_Org_ID());
		
		HSTMTree.setHSTIsPrimaryTree(dest, p_IsNewVersion);
		if (p_IsNewVersion){
			dest.setName(p_Name);
			dest.setIsAllNodes(p_IsAllNodes);
		}else{
			//set temporary name because we don't have new ID yet
			dest.setName(sourceParent.getName() + " - " + System.currentTimeMillis());
			HSTMTree.setHSTParentTree_ID(dest, sourceParent.getAD_Tree_ID());
			dest.setIsAllNodes(source.isAllNodes());
		}
		HSTMTree.setHSTFromDate(dest, p_DateFrom);
		
		dest.saveEx(sTrx);
		
		if (!p_IsNewVersion){
			dest.setName(sourceParent.getName() + " - " + dest.get_ID());
		}
		
		dest.saveEx();
		
		if (p_CopySourceTree){
			
			List<PO> nodes = HSTMTree.getTreeNodes(source, getAD_User_ID(), true);
			
			for (PO po : nodes) {
				
				if (po.get_ValueAsInt("Node_ID") > 0){
					// default node with ID = 0, added by default when creating
					// new Tree, so here we have to skip it
					// scoletta@ads.it - History Records Plugin
				// PO.create(ctx,tableName,trx) era Finmatica custom; vanilla equivalente: MTable.getPO(0,trx)
				PO copy = MTable.get(ctx, source.getNodeTableName()).getPO(0, sTrx);
					PO.copyValues(po, copy);
					copy.set_ValueOfColumn("AD_Tree_ID", dest.getAD_Tree_ID());
					copy.set_ValueOfColumn("Node_ID", po.get_Value("Node_ID"));
					copy.saveEx(sTrx);
				}
			}
		}
		
		if (dest.isAllNodes()){
			Trx trx = Trx.get(sTrx, false);
			int AD_Process_ID = MProcess.getProcess_ID(
					VERIFIY_TREE_PROCESS, null);

			MPInstance instance = new MPInstance(Env.getCtx(),
					AD_Process_ID, 0);
			if (!instance.save()) {
				return Msg.getMsg(Env.getCtx(),
						"ProcessNoInstance");
			}

			ProcessInfo pi = new ProcessInfo(VERIFIY_TREE_PROCESS
					, AD_Process_ID);
			pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());

			pi.setRecord_ID(dest.get_ID());
			
			ServerProcessCtl.process(pi, trx);

		}
		
		return String.valueOf(dest.get_ID());
	}

}
