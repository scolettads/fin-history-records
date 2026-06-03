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
package it.finmatica.history.records.sql;

import it.finmatica.history.records.internal.compiere.history.DateSourceUtil;
import it.finmatica.history.records.internal.compiere.history.HistorySelectionData;
import org.compiere.util.CCache;

/** Resolve date source using adempiere dictionary 
 * 
 * @author s.coletta@ads.it
 *
 */
public class ADDateSourceResolver implements DateSourceResolver
{
	private CCache<String, HistorySelectionData> cHSD = new CCache<>("ADDateSourceResolver.Script", 50);
	
	@Override
	public HistorySelectionData isDateSourceTable(String table, int AD_Client_ID)
	{
		HistorySelectionData hsd = null;
		table = table.toLowerCase();
		
		if(cHSD.containsKey(table))
		{
			hsd = cHSD.get(table);
		}
		else
		{
			hsd = DateSourceUtil.fromSourceTable(table, AD_Client_ID);
			cHSD.put(table, hsd);
		}
		
		return hsd;
	}

}
