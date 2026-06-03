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

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import it.finmatica.history.records.process.CreateNewTree;
//import it.finmatica.history.records.process.CreateNewTree;
import it.finmatica.history.records.process.MassiveVerifyChangeImpact;
import it.finmatica.history.records.process.ValidateHistoryRecord;
import it.finmatica.history.records.process.VerifyChangeImpact;

public class HSTProcessFactory implements IProcessFactory{

	@Override
	public ProcessCall newProcessInstance(String className) {
		
		if(className.equalsIgnoreCase(VerifyChangeImpact.class.getName()))
			return new VerifyChangeImpact();
		else if(className.equalsIgnoreCase(MassiveVerifyChangeImpact.class.getName()))
			return new MassiveVerifyChangeImpact();
		else if(className.equalsIgnoreCase(CreateNewTree.class.getName()))
			return new CreateNewTree();
		else if(className.equalsIgnoreCase(ValidateHistoryRecord.class.getName()))
			return new ValidateHistoryRecord();
		else
			return null;
	}

}
