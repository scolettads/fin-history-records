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
package it.finmatica.history.records.model;

import org.compiere.model.X_AD_Table;

public class HSTMAD_Table 
{
	/** Column name HST_HistoryMode */
    public static final String COLUMNNAME_HST_HistoryMode = "HST_HistoryMode";
    
    /** Column name HST_IsForceHstApplication */
    public static final String COLUMNNAME_HST_IsForceHstApplication = "HST_IsForceHstApplication";
    
	/** None = N */
	public static final String HST_HISTORYMODE_None = "N";
	/** Historicizing = S */
	public static final String HST_HISTORYMODE_Historicizing = "S";
	/** Time Machine = T */
	public static final String HST_HISTORYMODE_TimeMachine = "T";
	/** View = V */
	public static final String HST_HISTORYMODE_View = "V";
	/** Log = L */
	public static final String HST_HISTORYMODE_Log = "L";
	/** Set History Mode.
		@param HST_HistoryMode 
		History Mode for the table
	  */
	public static void setHST_HistoryMode (X_AD_Table AD_Table,String HST_HistoryMode)
	{

		AD_Table.set_ValueOfColumn (COLUMNNAME_HST_HistoryMode, HST_HistoryMode);
	}

	/** Get History Mode.
		@return History Mode for the table
	  */
	public static String getHST_HistoryMode (X_AD_Table AD_Table) 
	{
		String historyMode = (String)AD_Table.get_Value(COLUMNNAME_HST_HistoryMode);
		
		if(historyMode == null)
			historyMode = HST_HISTORYMODE_None;
		
		return historyMode; 
	}
	
	/** Get History Mode Old.
	@return History Mode Old for the table
	 */
	public static String getHST_HistoryModeOld (X_AD_Table AD_Table) 
	{
		return (String)AD_Table.get_ValueOld(COLUMNNAME_HST_HistoryMode);
	}

	/** Set Is Force Hst Application.
		@param HST_IsForceHstApplication 
		Force Applicability of the Historicizing 
	  */
	public static void setHST_IsForceHstApplication (X_AD_Table AD_Table,boolean HST_IsForceHstApplication)
	{
		AD_Table.set_ValueOfColumn (COLUMNNAME_HST_IsForceHstApplication, Boolean.valueOf(HST_IsForceHstApplication));
	}

	/** Get Is Force Hst Application.
		@return Force Applicability of the Historicizing 
	  */
	public static boolean isHST_IsForceHstApplication (X_AD_Table AD_Table) 
	{
		Object oo = AD_Table.get_Value(COLUMNNAME_HST_IsForceHstApplication);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
}
