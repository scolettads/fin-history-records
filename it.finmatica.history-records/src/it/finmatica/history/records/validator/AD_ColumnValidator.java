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

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.PO;
import org.compiere.model.X_AD_Column;
import org.compiere.model.X_AD_Table;
import org.compiere.util.Msg;

import it.finmatica.history.records.util.HSTMessages;
import it.finmatica.history.records.model.HSTMAD_Column;
import it.finmatica.history.records.model.HSTMAD_Table;
import it.finmatica.history.records.internal.idempiere.util.model.AbstractBoostrappableValidator;

public class AD_ColumnValidator extends AbstractBoostrappableValidator{

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}
	
	@Override
	public String modelChange(PO model, int nType) throws Exception
	{
		if(nType == TYPE_BEFORE_NEW || nType == TYPE_BEFORE_CHANGE)
		{
			if(model instanceof MColumn)
			{
				MColumn mColumn = (MColumn) model;
				Properties ctx = model.getCtx();
				String trxName = model.get_TrxName();
				
				if(HSTMAD_Column.isHST_IsHstColumn(mColumn))
				{
					int AD_Table_ID = mColumn.getAD_Table_ID();
					MTable mTable = MTable.get(ctx, AD_Table_ID, trxName);
					
					String historyMode = HSTMAD_Table.getHST_HistoryMode(mTable);
					if(historyMode.equals(HSTMAD_Table.HST_HISTORYMODE_None))
					{
						throw new AdempiereException(Msg.getMsg(ctx, HSTMessages.HST_ERR_IMPOSSIBLE_HISTORY_AD_COLUMN));
					}
					
				}
			}
		}
		return "";
	}
	

	@Override
	public String docValidate(PO po, int timing) {
		return null;
	}

	@Override
	public void initialize(ModelValidationEngine engine, MClient mClient) 
	{
		super.initialize(engine, mClient);
		addModelChange(engine, X_AD_Column.Table_Name);
	}
}
