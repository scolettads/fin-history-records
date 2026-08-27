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
package it.finmatica.history.records.internal.idempiere.util.model;

import org.adempiere.model.ImportValidator;
import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.idempiere.acct.base.model.FactsValidator;

import it.finmatica.history.records.internal.idempiere.util.model.AbstractBootstrapValidator.BootstrappableValidator;


public abstract class AbstractBoostrappableValidator implements ModelValidator,BootstrappableValidator
{
	private AbstractBootstrapValidator	m_valBootstrap;
	private int													m_AD_Client_ID = -1;
	
	@Override
	public void setBootstrapValidator(AbstractBootstrapValidator abv)
	{
		m_valBootstrap = abv;		
	}
	
	public ModelValidator getValidator()
	{
		if(m_valBootstrap != null)
			return m_valBootstrap;
		else
			return this;		
	}
	
	public FactsValidator getFactsValidator()
	{
		if(m_valBootstrap != null)
			return m_valBootstrap;
		else
			return (FactsValidator)this;		
	}

	public ImportValidator getImportValidator()
	{
		if(m_valBootstrap != null)
			return m_valBootstrap;
		else
			return (ImportValidator)this;		
	}

	@Override
	public int getAD_Client_ID()
	{
		if(m_valBootstrap != null)
		{
			return m_valBootstrap.getAD_Client_ID();
		}
		else
		{
			return m_AD_Client_ID;
		}
	}

	@Override
	public void initialize(ModelValidationEngine engine, MClient mClient)
	{
		if(m_valBootstrap == null)
		{
			m_AD_Client_ID = AbstractBootstrapValidator.getAD_Client_ID(mClient);
		}		
	}
	
	public void addModelChange(ModelValidationEngine engine,String sTableName)
	{
		ModelValidator	mVal = getValidator();
		
		if(mVal instanceof AbstractBootstrapValidator)
		{
			((AbstractBootstrapValidator) mVal).addModelChange(engine, sTableName);
		}
		else
		{
			engine.addModelChange(sTableName, mVal);
		}
	}
	
	public void addDocValidate(ModelValidationEngine engine,String sTableName)
	{
		ModelValidator	mVal = getValidator();
		
		if(mVal instanceof AbstractBootstrapValidator)
		{
			((AbstractBootstrapValidator) mVal).addDocValidate(engine, sTableName);
		}
		else
		{
			engine.addDocValidate(sTableName, mVal);
		}
	}

	
	public void addImportValidate(ModelValidationEngine engine,String sTableName)
	{
		ImportValidator	mVal = getImportValidator();
		
		if(mVal instanceof AbstractBootstrapValidator)
		{
			((AbstractBootstrapValidator) mVal).addImportValidate(engine, sTableName);
		}
		else
		{
			engine.addImportValidate(sTableName, mVal);
		}
	}	

}
