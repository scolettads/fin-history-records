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
package it.finmatica.history.records.extension;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;

import it.finmatica.history.records.model.MHSTCheckResults;
import it.finmatica.history.records.model.MHSTDateSourceConf;
import it.finmatica.history.records.model.MHSTUpdCheckConf;

public class HSTModelFactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		if(tableName.equals(MHSTCheckResults.Table_Name))
			return MHSTCheckResults.class;
		else if(tableName.equals(MHSTUpdCheckConf.Table_Name))
			return MHSTUpdCheckConf.class;
		else if(tableName.equals(MHSTDateSourceConf.Table_Name))
			return MHSTDateSourceConf.class;
		
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		if(tableName.equals(MHSTCheckResults.Table_Name))
			return new MHSTCheckResults(Env.getCtx(), Record_ID, trxName);
		else if(tableName.equals(MHSTUpdCheckConf.Table_Name))
			return new MHSTUpdCheckConf(Env.getCtx(), Record_ID, trxName);
		else if(tableName.equals(MHSTDateSourceConf.Table_Name))
			return new MHSTDateSourceConf(Env.getCtx(), Record_ID, trxName);
		
		return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		if(tableName.equals(MHSTCheckResults.Table_Name))
			return new MHSTCheckResults(Env.getCtx(), rs, trxName);
		else if(tableName.equals(MHSTUpdCheckConf.Table_Name))
			return new MHSTUpdCheckConf(Env.getCtx(), rs, trxName);
		else if(tableName.equals(MHSTDateSourceConf.Table_Name))
			return new MHSTDateSourceConf(Env.getCtx(), rs, trxName);
		
		return null;
	}
	
	
}
