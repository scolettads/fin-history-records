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
// scoletta@ads.it - History Records Plugin
// ITabpanelFactory non esiste in vanilla iDempiere 13.
// Sostituita con IADTabPanelFactory (org.adempiere.webui.factory).
// Firma metodo: getInstance(String type) → IADTabpanel (era newADTabpanel() → ADTabpanel).
package it.finmatica.history.records.ui.zk.extension;

import org.adempiere.webui.adwindow.IADTabpanel;
import org.adempiere.webui.factory.IADTabPanelFactory;

import it.finmatica.history.records.ui.zk.window.HSTADTabpanel;

public class HSTTabpanelFactory implements IADTabPanelFactory {

	@Override
	public IADTabpanel getInstance(String type) {
		return new HSTADTabpanel();
	}
}
