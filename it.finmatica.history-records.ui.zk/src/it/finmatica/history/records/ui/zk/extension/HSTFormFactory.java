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
// Rimossa dipendenza it.idempiere.webui.extension.BaseFormFactory (non esiste in vanilla 13).
// Implementa direttamente IFormFactory.
package it.finmatica.history.records.ui.zk.extension;

import org.adempiere.webui.factory.IFormFactory;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.IFormController;
import org.compiere.util.CLogger;

import it.finmatica.history.records.ui.zk.form.WTreeMaintenance;

public class HSTFormFactory implements IFormFactory {

	private static final CLogger log = CLogger.getCLogger(HSTFormFactory.class);

	@Override
	public ADForm newFormInstance(String formName) {
		ADForm form = null;

		if (formName.startsWith("org.compiere.apps.form.VTreeMaintenance")) {
			IFormController controller = (IFormController) new WTreeMaintenance();
			ADForm adForm = controller.getForm();
			adForm.setICustomForm(controller);
			form = adForm;
		}

		return form;
	}
}
