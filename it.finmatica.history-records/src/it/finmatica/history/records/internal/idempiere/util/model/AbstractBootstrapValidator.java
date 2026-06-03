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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.adempiere.model.ImportValidator;
import org.adempiere.process.ImportProcess;
import org.compiere.acct.Fact;
import org.compiere.model.FactsValidator;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.compiere.util.Util;

public abstract class AbstractBootstrapValidator implements ModelValidator,FactsValidator,ImportValidator
{
	private ArrayList<ModelValidator> 	m_lstValidators = new ArrayList<ModelValidator>(5);
	private int													m_AD_Client_ID = -1;
	private HashSet<String>							m_setDocValidator = new HashSet<String>(),
																			m_setModelValidator = new HashSet<String>(),
																			m_setFactsValidator = new HashSet<String>(),
																			m_setImportValidator = new HashSet<String>();
	
	public void addValidator(ModelValidator mv)
	{
		m_lstValidators.add(mv);
		
		if(mv instanceof BootstrappableValidator)
		{
			BootstrappableValidator bv = (BootstrappableValidator)mv;
			
			bv.setBootstrapValidator(this);
		}		
	}
	
	@Override
	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}

	@Override
	public void initialize(ModelValidationEngine engine, MClient mClient)
	{		
		m_AD_Client_ID = getAD_Client_ID(mClient);
		
		for(ModelValidator mv:m_lstValidators)
		{
			mv.initialize(engine, mClient);
		}
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		StringBuilder sb = new StringBuilder();
		
		for(ModelValidator mv:m_lstValidators)
		{
			String sOut = mv.login(AD_Org_ID,AD_Role_ID,AD_User_ID);
			
			if(!Util.isEmpty(sOut))
				sb.append(sOut).append('\n');
		}
		
		if(sb.length() > 0)
			return sb.toString();
		else		
			return null;
	}
	
	public static final int getAD_Client_ID(MClient mClient)
	{
		int AD_Client_ID = -1;
		
		if (mClient != null) 
		{	
			AD_Client_ID = mClient.getAD_Client_ID();
		}
		else  
		{
			AD_Client_ID = 	Env.getAD_Client_ID(Env.getCtx());			
		}
		
		return AD_Client_ID;
	}
	
	public interface BootstrappableValidator
	{
		void setBootstrapValidator(AbstractBootstrapValidator abv);
	}

	@Override
	public String docValidate(PO po, int timing)
	{
		StringBuilder sb = new StringBuilder();
		
		for(ModelValidator mv:m_lstValidators)
		{
			String sOut = mv.docValidate(po, timing);
			
			if(!Util.isEmpty(sOut))
				sb.append(sOut).append('\n');
		}
		
		if(sb.length() > 0)
			return sb.toString();
		else		
			return null;
	}

	@Override
	public String modelChange(PO po, int type) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		
		for(ModelValidator mv:m_lstValidators)
		{
			String sOut = mv.modelChange(po, type);
			
			if(!Util.isEmpty(sOut))
				sb.append(sOut).append('\n');
		}
		
		if(sb.length() > 0)
			return sb.toString();
		else		
			return null;
	}

	@Override
	public void validate(ImportProcess process, Object importModel, Object targetModel, int timing)
	{
		for(ModelValidator mv:m_lstValidators)
		{
			if(mv instanceof ImportValidator)
			{
				ImportValidator	iv = (ImportValidator)mv; 
				iv.validate(process, importModel, targetModel, timing);
			}
		}
	}

	@Override
	public String factsValidate(MAcctSchema schema, List<Fact> facts, PO po)
	{
		StringBuilder sb = new StringBuilder();
		
		for(ModelValidator mv:m_lstValidators)
		{
			if(mv instanceof FactsValidator)
			{
				FactsValidator fv = (FactsValidator)mv;
				String sOut = fv.factsValidate(schema, facts, po);
				
				if(!Util.isEmpty(sOut))
					sb.append(sOut).append('\n');
			}
		}
		
		if(sb.length() > 0)
			return sb.toString();
		else		
			return null;
	}
	
	public void addModelChange(ModelValidationEngine engine,String sTableName)
	{
		if(m_setModelValidator.contains(sTableName) == false)
		{
			m_setModelValidator.add(sTableName);
			engine.addModelChange(sTableName, this);
		}
	}
	
	public void addDocValidate(ModelValidationEngine engine,String sTableName)
	{
		if(m_setDocValidator.contains(sTableName) == false)
		{
			m_setDocValidator.add(sTableName);
			engine.addDocValidate(sTableName, this);
		}
	}
	
	public void addFactsValidate(ModelValidationEngine engine,String sTableName)
	{
		if(m_setFactsValidator.contains(sTableName) == false)
		{
			m_setFactsValidator.add(sTableName);
			engine.addFactsValidate(sTableName, this);
		}
	}
	
	public void addImportValidate(ModelValidationEngine engine,String sTableName)
	{
		if(m_setImportValidator.contains(sTableName) == false)
		{
			m_setImportValidator.add(sTableName);
			engine.addImportValidate(sTableName, this);
		}
	}	
	
}
