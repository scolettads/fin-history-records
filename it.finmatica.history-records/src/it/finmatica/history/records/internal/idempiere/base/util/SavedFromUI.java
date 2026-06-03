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
package it.finmatica.history.records.internal.idempiere.base.util;

import org.compiere.model.PO;

/** Utility class to check if a PO is saved from UI.
 *  Replaces GridTable.isSavedFromUI
 * 
 * @author s.coletta@ads.it
 *
 */
public class SavedFromUI
{
	private static final POThreadList savedFromUI = new POThreadList();
	
	/**
	 * Add po to list of object saved from UI
	 * 
	 * @param po
	 */
	public static void add(PO po)
	{
			savedFromUI.addPO(po);
	}
	
	/**
	 * Remove po from list of object saved by UI. Compare is performed by ==
	 * @param po
	 */
	public static void remove(PO po)
	{
			savedFromUI.removePO(po);
	}
	
	/**
	 * Check if po is saved from ui, compare is performed by ==
	 * @param po
	 * @return true if saved is performed by ui, false otherwise (eg. save from process)
	 */
	public static boolean  isSavedFromUI(PO po)
	{
		return savedFromUI.hasPO(po);
	}

}
