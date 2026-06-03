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
/** Generated Model - DO NOT CHANGE */
package it.finmatica.history.records.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for T_HSTCheckResults
 *  @author iDempiere (generated) 
 *  @version Release 4.1 - $Id$ */
public class X_T_HSTCheckResults extends PO implements I_T_HSTCheckResults, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20170503L;

    /** Standard Constructor */
    public X_T_HSTCheckResults (Properties ctx, int T_HSTCheckResults_ID, String trxName)
    {
      super (ctx, T_HSTCheckResults_ID, trxName);
      /** if (T_HSTCheckResults_ID == 0)
        {
			setT_HSTCheckResults_ID (0);
        } */
    }

    /** Load Constructor */
    public X_T_HSTCheckResults (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 4 - System 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_T_HSTCheckResults[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_PInstance getAD_PInstance() throws RuntimeException
    {
		return (org.compiere.model.I_AD_PInstance)MTable.get(getCtx(), org.compiere.model.I_AD_PInstance.Table_Name)
			.getPO(getAD_PInstance_ID(), get_TrxName());	}

	/** Set Process Instance.
		@param AD_PInstance_ID 
		Instance of the process
	  */
	public void setAD_PInstance_ID (int AD_PInstance_ID)
	{
		if (AD_PInstance_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_AD_PInstance_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_AD_PInstance_ID, Integer.valueOf(AD_PInstance_ID));
	}

	/** Get Process Instance.
		@return Instance of the process
	  */
	public int getAD_PInstance_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_PInstance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Table getAD_Table() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Table)MTable.get(getCtx(), org.compiere.model.I_AD_Table.Table_Name)
			.getPO(getAD_Table_ID(), get_TrxName());	}

	/** Set Table.
		@param AD_Table_ID 
		Database Table information
	  */
	public void setAD_Table_ID (int AD_Table_ID)
	{
		if (AD_Table_ID < 1) 
			set_Value (COLUMNNAME_AD_Table_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Table_ID, Integer.valueOf(AD_Table_ID));
	}

	/** Get Table.
		@return Database Table information
	  */
	public int getAD_Table_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Table_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_User getAD_User() throws RuntimeException
    {
		return (org.compiere.model.I_AD_User)MTable.get(getCtx(), org.compiere.model.I_AD_User.Table_Name)
			.getPO(getAD_User_ID(), get_TrxName());	}

	/** Set User/Contact.
		@param AD_User_ID 
		User within the system - Internal or Business Partner Contact
	  */
	public void setAD_User_ID (int AD_User_ID)
	{
		if (AD_User_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_AD_User_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_AD_User_ID, Integer.valueOf(AD_User_ID));
	}

	/** Get User/Contact.
		@return User within the system - Internal or Business Partner Contact
	  */
	public int getAD_User_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_User_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Date From.
		@param DateFrom 
		Starting date for a range
	  */
	public void setDateFrom (Timestamp DateFrom)
	{
		set_Value (COLUMNNAME_DateFrom, DateFrom);
	}

	/** Get Date From.
		@return Starting date for a range
	  */
	public Timestamp getDateFrom () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateFrom);
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	public org.compiere.model.I_AD_Table getHST_DestTable() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Table)MTable.get(getCtx(), org.compiere.model.I_AD_Table.Table_Name)
			.getPO(getHST_DestTable_ID(), get_TrxName());	}

	/** Set Destination Table.
		@param HST_DestTable_ID 
		It represent the table of destination
	  */
	public void setHST_DestTable_ID (int HST_DestTable_ID)
	{
		if (HST_DestTable_ID < 1) 
			set_Value (COLUMNNAME_HST_DestTable_ID, null);
		else 
			set_Value (COLUMNNAME_HST_DestTable_ID, Integer.valueOf(HST_DestTable_ID));
	}

	/** Get Destination Table.
		@return It represent the table of destination
	  */
	public int getHST_DestTable_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HST_DestTable_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set History Record.
		@param HST_Record_ID History Record	  */
	public void setHST_Record_ID (int HST_Record_ID)
	{
		if (HST_Record_ID < 1) 
			set_Value (COLUMNNAME_HST_Record_ID, null);
		else 
			set_Value (COLUMNNAME_HST_Record_ID, Integer.valueOf(HST_Record_ID));
	}

	/** Get History Record.
		@return History Record	  */
	public int getHST_Record_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HST_Record_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Record ID.
		@param Record_ID 
		Direct internal record ID
	  */
	public void setRecord_ID (int Record_ID)
	{
		if (Record_ID < 0) 
			set_ValueNoCheck (COLUMNNAME_Record_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_Record_ID, Integer.valueOf(Record_ID));
	}

	/** Get Record ID.
		@return Direct internal record ID
	  */
	public int getRecord_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Record_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set History Check Result.
		@param T_HSTCheckResults_ID History Check Result	  */
	public void setT_HSTCheckResults_ID (int T_HSTCheckResults_ID)
	{
		if (T_HSTCheckResults_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_T_HSTCheckResults_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_T_HSTCheckResults_ID, Integer.valueOf(T_HSTCheckResults_ID));
	}

	/** Get History Check Result.
		@return History Check Result	  */
	public int getT_HSTCheckResults_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_T_HSTCheckResults_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}