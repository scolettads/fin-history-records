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
import org.compiere.util.Util;

public class HSTMTable {

	public static final String COLUMNNAME_HST_HistoryMode = "HST_HistoryMode";
	public static final String HSTMode_Nessuna = "N";
	public static final String HSTMode_Storicizzata = "S";
	public static final String HSTMode_TimeMachine = "T";
	public static final String HSTMode_Vista = "V";
	public static final String HSTMode_Log = "L";

	public static final String COLUMNNAME_HST_IsForceHstApplication = "HST_IsForceHstApplication";
	
	public static void setHST_HistoryMode(X_AD_Table t, String HST_HistoryMode) {
		t.set_ValueOfColumn(COLUMNNAME_HST_HistoryMode, HST_HistoryMode);
	}

	public static String getHST_HistoryMode(X_AD_Table t) {
		return (String) t.get_Value(COLUMNNAME_HST_HistoryMode);
	}

	public static boolean isStoricizzata(X_AD_Table t) {
		return isMode(t, HSTMode_Storicizzata) || isMode(t, HSTMode_TimeMachine) || isMode(t, HSTMode_Log);
	}

	private static boolean isMode(X_AD_Table t, String mode) {
		String hm = getHST_HistoryMode(t);
		if (Util.isEmpty(hm)) {
			return false;
		}

		return hm.equals(mode);
	}

	public static boolean isHST_IsForceHstApplication(X_AD_Table t) {
		Object oo = t.get_Value(COLUMNNAME_HST_IsForceHstApplication);
		if (oo != null) {
			if (oo instanceof Boolean)
				return ((Boolean) oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	public static void setHST_IsForceHstApplication(X_AD_Table t, boolean HST_IsForceHstApplication) {
		t.set_ValueOfColumn(COLUMNNAME_HST_IsForceHstApplication, Boolean.valueOf(HST_IsForceHstApplication));
	}
	
}
