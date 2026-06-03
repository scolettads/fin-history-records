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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.compiere.model.MSysConfig;
import it.finmatica.history.records.internal.compiere.history.HistorySelectionData;
import org.compiere.util.Env;

/** SysConfig utility class
 * 
 * @author s.coletta@ads.it
 *
 */
public class HSTSysConfig
{
	public static final String DISABLE_TIMEMACHINE = "HST_DISABLE_TIMEMACHINE";
	public static final String FIN_HST_StartDate_Range = "FIN_HST_StartDate_Range";
	public static final String FIN_HST_StartDate_Fixed = "FIN_HST_StartDate_Fixed";

	/** Is time machine disabled ?
	 * 
	 * @return true if deisabled
	 */
	public static boolean isTimeMachineDisabled()
	{
		boolean bDisabled = false;
		
		HistorySelectionData currentHST = HistorySelectionData.getCurrent();
		
		try
		{
			HistorySelectionData.setCurrent(HistorySelectionData.DISABLE_TIMEMACHINE);
			bDisabled =  MSysConfig.getBooleanValue(DISABLE_TIMEMACHINE, false);
		}
		finally
		{
			HistorySelectionData.setCurrent(currentHST);
		}
		
		return bDisabled;
	}
	
	public static Timestamp getHST_StartDate_Fixed(){
		Timestamp timestamp = null;
		try{
			String startDateFixed = MSysConfig.getValue(FIN_HST_StartDate_Fixed, "", Env.getAD_Client_ID(Env.getCtx()), Env.getAD_Org_ID(Env.getCtx()));
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			Date date = formatter.parse(startDateFixed);
			timestamp = new Timestamp(date.getTime());
		}catch (Exception e) {
			// In caso di errore nel recupero o nel formato ritorno null e procedo
			timestamp = null;
		}
		return timestamp;
	}

	public static int getHST_StartDate_Range(){
		int startDateRange = MSysConfig.getIntValue(FIN_HST_StartDate_Range, 0, Env.getAD_Client_ID(Env.getCtx()), Env.getAD_Org_ID(Env.getCtx()));
		
		return startDateRange;
	}

}
