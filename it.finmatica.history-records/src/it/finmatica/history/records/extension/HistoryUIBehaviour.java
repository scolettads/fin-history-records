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

import java.util.Properties;

import org.adempiere.base.IUIBehaviour;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.Lookup;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupInfo;

import it.finmatica.history.records.sql.ADHistoryReplacer;

public class HistoryUIBehaviour implements IUIBehaviour
{

	@Override
	public Boolean isLookupCacheable(Lookup lookup, MLookupInfo lookupInfo)
	{
		String tableName = null;
		
		if(lookupInfo == null)
			lookupInfo = ((MLookup)lookup).getLookupInfo();
		
		if(lookupInfo != null)
		{
			tableName = lookupInfo.TableName;
		}
		
		if(tableName != null)
		{
			return !ADHistoryReplacer.hasHistory(tableName);
		}
		
		return Boolean.TRUE;
	}

	@Override
	public Boolean isEditable(Properties ctx, GridTab tab)
	{
		return null;
	}

	@Override
	public Boolean isEditable(Properties ctx, GridField field,
			boolean checkContext, boolean isGrid)
	{
		return null;
	}

}
