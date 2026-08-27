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

import it.finmatica.history.records.internal.compiere.history.HistorySelectionData;
import it.finmatica.history.records.sql.ADHistoryReplacer;

// s.coletta@ads.it 2026-08-27
public class HistoryUIBehaviour implements IUIBehaviour
{

	/**
	 * Returns a date-scoped cache key suffix when the time machine is active on
	 * the lookup's target table.  Each history date gets its own isolated bucket
	 * so caching stays active; returning null in normal mode is a zero-overhead
	 * no-op (identical to vanilla iDempiere behaviour).
	 */
	@Override
	public String getLookupCacheKeySuffix(Lookup lookup, MLookupInfo lookupInfo)
	{
		HistorySelectionData hsd = HistorySelectionData.getCurrent();
		if (hsd == null || hsd.isDisableTimeMachine() || hsd.getHistoryDate() == null)
			return null;

		if (lookupInfo == null && lookup instanceof MLookup)
			lookupInfo = ((MLookup) lookup).getLookupInfo();

		String tableName = (lookupInfo != null) ? lookupInfo.TableName : null;
		if (tableName != null && ADHistoryReplacer.hasHistory(tableName))
			return "HST@" + hsd.getHistoryDate().getTime();

		return null;
	}

	/** Read-only when the time machine is active (veto semantics: false = deny). */
	@Override
	public boolean isTabEditable(Properties ctx, GridTab tab)
	{
		HistorySelectionData hsd = HistorySelectionData.getCurrent();
		if (hsd == null || hsd.isDisableTimeMachine() || hsd.getHistoryDate() == null)
			return true;
		return false;
	}

	/** Read-only when the time machine is active (veto semantics: false = deny). */
	@Override
	public boolean isFieldEditable(Properties ctx, GridField field,
			boolean checkContext, boolean isGrid)
	{
		HistorySelectionData hsd = HistorySelectionData.getCurrent();
		if (hsd == null || hsd.isDisableTimeMachine() || hsd.getHistoryDate() == null)
			return true;
		return false;
	}

}
