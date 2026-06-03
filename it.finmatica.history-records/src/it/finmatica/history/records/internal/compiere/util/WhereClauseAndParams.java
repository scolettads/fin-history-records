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
package it.finmatica.history.records.internal.compiere.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains both a where clause (may be partial) and its related parameters.
 * The main use case is with factory methods providing filter where clause. May be useful in all situation where a query is dinamically generated 
 * and returned for use in other classes.   
 *
 * @author s.coletta@ads.it
 *  	   <li> IDEMPIERE-3216-new-extension-to-customize-activities-query
 */

public class WhereClauseAndParams implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6135636566874305863L;
	
	private String where;
	private List<Object> params;
			
	public WhereClauseAndParams(String where,List<Object> params)
	{
		this.where = where;
		this.params = params;
	}
	
	public WhereClauseAndParams()
	{
		this(null,new ArrayList<Object>());
	}
	
	public String getWhere() {
		return where;
	}
	public void setWhere(String where) {
		this.where = where;
	}
	public List<Object> getParams() {
		return params;
	}
	public void setParams(List<Object> params) {
		this.params = params;
	}
}