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

import net.sf.jsqlparser.schema.Table;

public class FromTable
{
	public static final FromTable fromTable(Table table)
	{
		FromTable ft = new FromTable();
		
		if(table.getAlias() != null)
		{
			ft.alias = table.getAlias().getName();
		}
			
		ft.table = table.getName();
		
		return ft;	
	}
	
	public String	table;
	public String	alias;
	
	public String getEffectiveName()
	{
		if(alias != null)
			return alias;
		
		return table;
	}
	
	@Override
	public String toString()
	{	
		String toString = "table=" + table;
		
		if(alias != null)
		{
			toString += ", alias=" + alias;
		}
		
		return toString; 
	}
	
	
}
