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
package it.finmatica.history.records;

import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;

import it.finmatica.history.records.validator.AD_ColumnValidator;
import it.finmatica.history.records.validator.AD_TableValidator;
import it.finmatica.history.records.validator.DateSourceConfValidator;
import it.finmatica.history.records.validator.TreeValidator;
import it.finmatica.history.records.internal.idempiere.util.model.AbstractBootstrapValidator;

public class HSTValidator extends AbstractBootstrapValidator {
	
	public HSTValidator()
	{
		super();
		addValidator(new AD_TableValidator());
		addValidator(new AD_ColumnValidator());
		addValidator(new DateSourceConfValidator());
		addValidator(new TreeValidator());
	}
	
	@Override
	public void initialize(ModelValidationEngine engine, MClient mClient) 
	{
		super.initialize(engine, mClient);
	}
	
}
