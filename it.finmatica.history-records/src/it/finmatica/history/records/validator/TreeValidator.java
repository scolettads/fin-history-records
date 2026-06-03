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

import java.util.Properties;

import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.PO;
import org.compiere.model.X_AD_Tree;
import org.compiere.model.X_C_Element;
import it.finmatica.history.records.internal.compiere.history.HSTMTree;
import org.compiere.util.DB;
import org.compiere.util.TimeUtil;


import it.finmatica.history.records.model.HSTPO;
import it.finmatica.history.records.model.HSTPO.RecordHST;
import it.finmatica.history.records.internal.idempiere.util.model.AbstractBoostrappableValidator;

public class TreeValidator extends AbstractBoostrappableValidator {

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}

	@Override
	public String modelChange(PO model, int nType) throws Exception {
		Properties ctx = model.getCtx();
		String trxName = model.get_TrxName();

		if (model instanceof X_AD_Tree) {
			X_AD_Tree mTree = (X_AD_Tree) model;
			
			if (nType == TYPE_AFTER_NEW || nType == TYPE_AFTER_CHANGE) {
			
				if (HSTMTree.isHSTIsPrimaryTree(mTree)) {
					DB.executeUpdate("Update AD_Tree set HSTParentTree_ID = AD_Tree_ID where AD_Tree_ID = ?",
							mTree.get_ID(), false, trxName);
				}
			}
			
			if (nType == TYPE_BEFORE_NEW){
				if (HSTMTree.isHSTIsPrimaryTree(mTree)) {
					HSTMTree.setHSTToDate(mTree, TimeUtil.getDay(3000, 12, 31));
				}else{
					RecordHST recordHST = new RecordHST(null, mTree);
					HSTPO.checkDateFrom(ctx, recordHST, HSTMTree.getHSTParentTree_ID(mTree), HSTMTree.getHSTFromDate(mTree), trxName);
					HSTPO.validateHistoryRecord(mTree,ctx,recordHST,HSTMTree.getHSTFromDate(mTree),trxName,false,false);
				}
			}
		}else if (model instanceof X_C_Element) {
			X_C_Element element = (X_C_Element) model;
			if (nType == TYPE_AFTER_NEW){
				X_AD_Tree mTree = new X_AD_Tree(ctx, element.getAD_Tree_ID(), trxName);
				HSTMTree.setC_Element_ID(mTree, element.get_ID());
				mTree.saveEx(trxName);
			}
		}
		return "";
	}

	@Override
	public String docValidate(PO po, int timing) {
		return null;
	}

	@Override
	public void initialize(ModelValidationEngine engine, MClient mClient) {
		super.initialize(engine, mClient);
		addModelChange(engine, X_AD_Tree.Table_Name);
		addModelChange(engine, X_C_Element.Table_Name);
	}
}
