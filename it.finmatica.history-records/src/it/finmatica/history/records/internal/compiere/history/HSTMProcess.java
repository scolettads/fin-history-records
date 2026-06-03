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

import org.compiere.model.X_AD_Process;

/** Wrapper class for process additional field
 * 
 * @author s.coletta@ads.it
 *
 */
public class HSTMProcess {

	public static final String COLUMNNAME_AD_InfoWindow_ID = "AD_InfoWindow_ID";
	
	public static int getAD_InfoWindow_ID(X_AD_Process p) {
		Integer ii = (Integer) p.get_Value(COLUMNNAME_AD_InfoWindow_ID);
		if (ii == null)
			return 0;
		return ii.intValue();
	}

	public static void setAD_InfoWindow_ID(X_AD_Process p,
			int AD_InfoWindow_ID) {
		if (AD_InfoWindow_ID < 1)
			p.set_ValueOfColumn(COLUMNNAME_AD_InfoWindow_ID, null);
		else
			p.set_ValueOfColumn(COLUMNNAME_AD_InfoWindow_ID,
					Integer.valueOf(AD_InfoWindow_ID));
	}
}
