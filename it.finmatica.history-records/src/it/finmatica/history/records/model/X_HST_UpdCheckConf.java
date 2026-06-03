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
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for HST_UpdCheckConf
 *  @author iDempiere (generated) 
 *  @version Release 4.1 - $Id$ */
public class X_HST_UpdCheckConf extends PO implements I_HST_UpdCheckConf, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20170418L;

    /** Standard Constructor */
    public X_HST_UpdCheckConf (Properties ctx, int HST_UpdCheckConf_ID, String trxName)
    {
      super (ctx, HST_UpdCheckConf_ID, trxName);
      /** if (HST_UpdCheckConf_ID == 0)
        {
			setAD_Table_ID (0);
			setHST_DestTable_ID (0);
			setHST_UpdCheckConf_ID (0);
        } */
    }

    /** Load Constructor */
    public X_HST_UpdCheckConf (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 7 - System - Client - Org 
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
      StringBuffer sb = new StringBuffer ("X_HST_UpdCheckConf[")
        .append(get_ID()).append("]");
      return sb.toString();
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

	/** Set Check Query.
		@param HST_CheckQuery 
		Query for the check
	  */
	public void setHST_CheckQuery (String HST_CheckQuery)
	{
		set_Value (COLUMNNAME_HST_CheckQuery, HST_CheckQuery);
	}

	/** Get Check Query.
		@return Query for the check
	  */
	public String getHST_CheckQuery () 
	{
		return (String)get_Value(COLUMNNAME_HST_CheckQuery);
	}

	/** Set Check User Definition.
		@param HST_CheckUserDef 
		Query used to Check the User Definition
	  */
	public void setHST_CheckUserDef (String HST_CheckUserDef)
	{
		set_Value (COLUMNNAME_HST_CheckUserDef, HST_CheckUserDef);
	}

	/** Get Check User Definition.
		@return Query used to Check the User Definition
	  */
	public String getHST_CheckUserDef () 
	{
		return (String)get_Value(COLUMNNAME_HST_CheckUserDef);
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

	/** Set Update Check Configuration.
		@param HST_UpdCheckConf_ID Update Check Configuration	  */
	public void setHST_UpdCheckConf_ID (int HST_UpdCheckConf_ID)
	{
		if (HST_UpdCheckConf_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HST_UpdCheckConf_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_HST_UpdCheckConf_ID, Integer.valueOf(HST_UpdCheckConf_ID));
	}

	/** Get Update Check Configuration.
		@return Update Check Configuration	  */
	public int getHST_UpdCheckConf_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HST_UpdCheckConf_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}