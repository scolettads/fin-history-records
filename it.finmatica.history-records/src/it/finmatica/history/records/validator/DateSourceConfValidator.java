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

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.PO;
import org.compiere.util.Msg;
import it.finmatica.history.records.util.HSTMessages;
import it.finmatica.history.records.model.MHST_DateSourceConf;
import it.finmatica.history.records.model.X_HST_DateSourceConf;
import it.finmatica.history.records.internal.idempiere.util.model.AbstractBoostrappableValidator;

public class DateSourceConfValidator extends AbstractBoostrappableValidator{

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}

	@Override
	public String modelChange(PO model, int nType) throws Exception 
	{
		if(model instanceof MHST_DateSourceConf)
		{
			MHST_DateSourceConf mhst_DateSourceConf= (MHST_DateSourceConf) model;
			if(nType == TYPE_BEFORE_NEW || nType == TYPE_BEFORE_CHANGE)
			{
				if(verifyScript(mhst_DateSourceConf.getScript()) == false)
					throw new AdempiereException(Msg.getMsg(mhst_DateSourceConf.getCtx(), HSTMessages.HST_DateSourceConf_ERR));
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
		addModelChange(engine, X_HST_DateSourceConf.Table_Name);
	}
	
	public static boolean verifyScript(String script)
	{
		String hashtag = "#";
		String XHSTX = "XHSTX";
		int length = XHSTX.length();
		
		int index = script.indexOf(hashtag);
		if(index >= 0 && script.lastIndexOf(hashtag, index) >= 0)
		{
			String scriptTrimmed = script.trim();
			if(scriptTrimmed.startsWith(hashtag) && scriptTrimmed.endsWith(hashtag))
				return true;
			else
			{
				//variabile usata per sapere se non ci sono piu' stringhe con quella sequenza di caratteri
				int lastIndexXHSTX = 0;
				int indexXHSTX = script.indexOf(XHSTX)+length;
				char nextChar = script.charAt(indexXHSTX);
				
				while(Character.isWhitespace(nextChar)==false)
				{
					lastIndexXHSTX= indexXHSTX+length;
					indexXHSTX = script.indexOf(XHSTX, indexXHSTX)+length;
					if(lastIndexXHSTX < indexXHSTX)
						nextChar = script.charAt(indexXHSTX);
					else
						break;
				}
				
				if(Character.isWhitespace(nextChar))
				{
					if (script.indexOf(XHSTX+".")>=0)
						return true;
				}
			}
		}
		
		return false;
	}
	
}
