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
package it.finmatica.history.records.internal.compiere.model;

import org.compiere.model.MColumn;
import org.compiere.model.PO;

public class POUtil
{
	private POUtil() { /* utility */ }

	/**
	 * Replica della signature Finmatica PO.copyValues(from, to, copyKeyColumn,
	 * copyStandardColumn, copyNotAllowCopyColumn, copyNewValues, withNulls).
	 *
	 * @param from                      PO sorgente (vecchio, esistente, non modificato in sessione)
	 * @param to                        PO destinazione (nuovo, non salvato)
	 * @param copyKeyColumn             se true copia anche le colonne chiave
	 * @param copyStandardColumn        se true copia anche le standard columns (AD_Client_ID, AD_Org_ID, IsActive, ecc.)
	 * @param copyNotAllowCopyColumn    se true copia anche le colonne con IsAllowCopy=N
	 * @param copyNewValues             se true copia i valori correnti (m_newValues post-modifica); se false copia i valori salvati (m_oldValues)
	 * @param withNulls                 se true imposta null sulle colonne null della source (default: skip null)
	 */
	public static void copyValues(PO from, PO to,
			boolean copyKeyColumn, boolean copyStandardColumn, boolean copyNotAllowCopyColumn,
			boolean copyNewValues, boolean withNulls)
	{
		String fromTableName = from.get_TableName();
		int colCount = from.get_ColumnCount();

		for (int i = 0; i < colCount; i++)
		{
			String colName = from.get_ColumnName(i);

			// skip audit columns sempre (Created/CreatedBy/Updated/UpdatedBy)
			// — vengono comunque rigenerati dal framework su saveEx()
			if ("Created".equals(colName) || "CreatedBy".equals(colName)
					|| "Updated".equals(colName) || "UpdatedBy".equals(colName))
				continue;

			MColumn col = MColumn.get(from.getCtx(), fromTableName, colName);
			if (col == null)
				continue;

			// skip virtual columns (non hanno valore proprio, sono computate)
			if (col.isVirtualColumn())
				continue;
			// skip UUID column sempre (deve essere univoco) — IDEMPIERE-67
			if (col.isUUIDColumn())
				continue;
			// skip key column se non richiesto
			if (col.isKey() && !copyKeyColumn)
				continue;
			// skip standard column se non richiesto
			if (col.isStandardColumn() && !copyStandardColumn)
				continue;
			// skip NotAllowCopy column se non richiesto
			if (!col.isAllowCopy() && !copyNotAllowCopyColumn)
				continue;

			// la colonna deve esistere anche nel target (per sicurezza con tabelle HST
			// che potrebbero divergere dalla source per qualche colonna)
			int toIdx = to.get_ColumnIndex(colName);
			if (toIdx < 0)
				continue;

			Object val;
			if (copyNewValues && from.is_ValueChanged(i))
				val = from.get_Value(i);            // valore corrente (post-modifica in memoria)
			else
				val = from.get_ValueOld(i);         // valore salvato a DB

			if (val == null)
			{
				if (withNulls)
					to.set_ValueOfColumn(colName, null);
				// altrimenti skip — non sovrascrivere col null
			}
			else
			{
				to.set_ValueOfColumn(colName, val);
			}
		}
	}
}
