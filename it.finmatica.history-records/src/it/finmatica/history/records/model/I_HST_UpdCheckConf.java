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
package it.finmatica.history.records.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for HST_UpdCheckConf
 *  @author iDempiere (generated) 
 *  @version Release 4.1
 */
@SuppressWarnings("all")
public interface I_HST_UpdCheckConf 
{

    /** TableName=HST_UpdCheckConf */
    public static final String Table_Name = "HST_UpdCheckConf";

    /** AD_Table_ID=1000170 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 7 - System - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(7);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name AD_Table_ID */
    public static final String COLUMNNAME_AD_Table_ID = "AD_Table_ID";

	/** Set Table.
	  * Database Table information
	  */
	public void setAD_Table_ID (int AD_Table_ID);

	/** Get Table.
	  * Database Table information
	  */
	public int getAD_Table_ID();

	public org.compiere.model.I_AD_Table getAD_Table() throws RuntimeException;

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name HST_CheckQuery */
    public static final String COLUMNNAME_HST_CheckQuery = "HST_CheckQuery";

	/** Set Check Query.
	  * Query for the check
	  */
	public void setHST_CheckQuery (String HST_CheckQuery);

	/** Get Check Query.
	  * Query for the check
	  */
	public String getHST_CheckQuery();

    /** Column name HST_CheckUserDef */
    public static final String COLUMNNAME_HST_CheckUserDef = "HST_CheckUserDef";

	/** Set Check User Definition.
	  * Query used to Check the User Definition
	  */
	public void setHST_CheckUserDef (String HST_CheckUserDef);

	/** Get Check User Definition.
	  * Query used to Check the User Definition
	  */
	public String getHST_CheckUserDef();

    /** Column name HST_DestTable_ID */
    public static final String COLUMNNAME_HST_DestTable_ID = "HST_DestTable_ID";

	/** Set Destination Table.
	  * It represent the table of destination
	  */
	public void setHST_DestTable_ID (int HST_DestTable_ID);

	/** Get Destination Table.
	  * It represent the table of destination
	  */
	public int getHST_DestTable_ID();

	public org.compiere.model.I_AD_Table getHST_DestTable() throws RuntimeException;

    /** Column name HST_UpdCheckConf_ID */
    public static final String COLUMNNAME_HST_UpdCheckConf_ID = "HST_UpdCheckConf_ID";

	/** Set Update Check Configuration	  */
	public void setHST_UpdCheckConf_ID (int HST_UpdCheckConf_ID);

	/** Get Update Check Configuration	  */
	public int getHST_UpdCheckConf_ID();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();
}
