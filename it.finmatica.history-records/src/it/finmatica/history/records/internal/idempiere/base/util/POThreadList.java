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

import java.util.ListIterator;

import org.compiere.model.PO;

/** Utility class for list of PO saved in a thread.
 * 
 * @author s.coletta@ads.it
 *
 */

public class POThreadList
{	
	private final POListThreadLocal poList = new POListThreadLocal();
		
	/**
	 * Add po to list of saved objects
	 * 
	 * @param po
	 */
	
	public PO addPO(PO po)
	{
			poList.get().add(po);
			return po;
	}
	
	/**
	 * Check if po is saved from this source
	 * @param po
	 * @return true if saved by this source, false otherwise
	 */
	
	public boolean  hasPO(PO po)
	{		
		for(PO o :  poList.get())
		{
			if(o.equals(po))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Remove po from list of object kept by this source
	 * @param po
	 */
	
	public PO removePO(PO po)
	{
			ListIterator<PO> it = poList.get().listIterator();
		PO removed = null;

			while(it.hasNext())
			{
				PO o = it.next();
				
				if(o == po)
				{
					it.remove();
				removed = po;
					break;
				}			
			}

		return removed;
	}
}
