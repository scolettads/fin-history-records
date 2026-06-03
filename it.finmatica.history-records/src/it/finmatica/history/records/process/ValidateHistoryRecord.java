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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import it.finmatica.history.records.model.HSTPO;
// scoletta@ads.it - History Records Plugin
// rimosso import it.idempiere.util.ProcessHelper, sostituito RETURN_OK con stringa standard

public class ValidateHistoryRecord extends SvrProcess
{
	private int AD_Client_ID = 0;
	private int AD_Org_ID = 0;
	private boolean validFromTomorrow = false;
	
	@Override
	protected void prepare() 
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("AD_Client_ID"))
				AD_Client_ID = para[i].getParameterAsInt();
			else if (name.equals("AD_Org_ID"))
				AD_Org_ID = para[i].getParameterAsInt();
			else if (name.equals("validFromTomorrow"))
				validFromTomorrow = para[i].getParameterAsBoolean();
		}
	}

	@Override
	protected String doIt() throws Exception 
	{
		Properties ctx = getCtx();
		String trxName = get_TrxName();
		
		Query mQuery = new Query(ctx,MTable.Table_Name,"HST_HistoryMode in ('S','T') ", trxName);
		List<MTable> mTables = mQuery.list();
		
		Calendar cal =Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		if(validFromTomorrow)
			cal.add(Calendar.DAY_OF_YEAR, 1);
		
		String tableName = null;
		String keyColumnName = null;
		
		List<Object> parameter = new ArrayList<Object>();
		parameter.add(new Timestamp(cal.getTimeInMillis()));
		
		for(MTable mTable:mTables)
		{
			StringBuilder update = new StringBuilder("update ");
			update.append(mTable.getTableName()).append(HSTPO.SUFFIX_HISTORY_TABLE).append(" set ")
			.append(HSTPO.COLUMNNAME_HSTActualRecord).append(" = 'N' ")
			.append("where HSTToDate < ? and HSTActualRecord != 'N' ");
			
			if(AD_Client_ID > 0)
				update.append(" And AD_Client_ID = ").append(AD_Client_ID);
			
			if(AD_Org_ID > 0)
				update.append(" And AD_Org_ID = ").append(AD_Org_ID);
			
			DB.executeUpdate(update.toString(),parameter.toArray(),false,trxName);
		}
		
		StringBuilder where = new StringBuilder();
		where.append("HSTActualRecord = 'F' And HSTFromDate <= ? and HSTToDate >= ?");
		
		if(AD_Client_ID > 0)
			where.append(" And AD_Client_ID = ").append(AD_Client_ID);
		
		if(AD_Org_ID > 0)
			where.append(" And AD_Org_ID = ").append(AD_Org_ID);
		
		for(MTable mTable:mTables)
		{
			tableName = mTable.get_ValueAsString(MTable.COLUMNNAME_TableName);
			Query nQuery = new Query(ctx, tableName+HSTPO.SUFFIX_HISTORY_TABLE,where.toString(), trxName);
			nQuery.setParameters(new Timestamp(cal.getTimeInMillis()),new Timestamp(cal.getTimeInMillis()));
			List<PO> HSTPos = nQuery.list();
				
			for(PO HSTpo : HSTPos)
			{
				keyColumnName = tableName+"_ID";
				int record_ID = HSTpo.get_ValueAsInt(keyColumnName);				
				
				Query oQuery = new Query(ctx, tableName+HSTPO.SUFFIX_HISTORY_TABLE,keyColumnName+"= ? And HSTActualRecord = 'Y' ", trxName);
				oQuery.setParameters(record_ID);
				PO poActual = oQuery.first();
				
				if(poActual != null)
				{
					HSTPO.setHSTActualRecord(poActual, HSTPO.HSTActualRcd_No);
					poActual.saveEx(trxName);
				}
				
				HSTPO.setHSTActualRecord(HSTpo, HSTPO.HSTActualRcd_Yes);
				HSTpo.saveEx(trxName);
			}
		}
		
		// scoletta@ads.it - History Records Plugin
		// sostituito ProcessHelper.RETURN_OK con stringa "@Ok@" standard iDempiere
		return "@Ok@";
	}

}
