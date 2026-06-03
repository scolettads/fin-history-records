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
package it.finmatica.history.records.util;

public interface HSTMessages
{
	// Generic messages
	public static final String	ADNOTE_MSG = "HST_CheckResultSubject";
		
	public static final String HST_DateSourceConf_ERR = "HST_DateSourceConf_ERR",//Le regole di determinazione della data devono sempre avere il campo id tracchiuso tra # e l'alias della tabella deve essere XHSTX.
			HST_ERR_IMPOSSIBLE_MODIFY_ACTUAL_HISTORY_RECORD = "HST_ERR_IMPOSSIBLE_MODIFY_ACTUAL_HISTORY_RECORD",//"Non e' possibile modificare un record storico attuale. Modificare il record principale per propagare correttamente le modifiche"
			HST_ERR_START_DATE = "HST_ERR_START_DATE",//"La data di inizio validita' deve essere minore o uguale alla data di fine validita'"
			HST_ERR_MODIFY_OVERLAP_HISTORY_RECORD = "HST_ERR_MODIFY_OVERLAP_HISTORY_RECORD",//"La modifica sovrapporrebbe esattamente due record di storico"
			HST_ERR_OVERWRITE_ALL_HISTORY_RECORDS = "HST_ERR_OVERWRITE_ALL_HISTORY_RECORDS",//Il nuovo range di date sovrascriverebbe interi record di storicizzazione
			HST_ERR_ONE_HISTORY_RECORD_IS_MANDATORY_FOR_HISTORY_TABLE = "HST_ERR_ONE_HISTORY_RECORD_IS_MANDATORY_FOR_HISTORY_TABLE",//"Per una tabella storicizzata deve sempre esistere almeno un record di storicizzazione"
			HST_ERR_HISTORY_EXIST_ANOTHER_RECORD_WITH_THIS_DATE = "HST_ERR_HISTORY_EXIST_ANOTHER_RECORD_WITH_THIS_DATE",//"E' già presente uno storico con la medesima data di inizio validita'"
			HST_ERR_NO_MANUAL_MODIFY ="HST_ERR_NO_MANUAL_MODIFY", //"Non e' possibile modificare manualmente il tipo storico"
			HST_EXIST_ANOTHER_HISTORY_RECORDS_WITH_NEXT_DATE ="HST_EXIST_ANOTHER_HISTORY_RECORDS_WITH_NEXT_DATE",//"Sono gia' presenti record di storicizzazione con data successiva "
			HST_ERR_FOR_VIEW_HISTORY_MUST_BE_VIEW = "HST_ERR_FOR_VIEW_HISTORY_MUST_BE_VIEW",//L'unica storicizzazione applicabile alle viste e' quella di tipo Vista
			HST_ERR_HISTORY_ONLY_TABLE_REGISTRY	= "HST_ERR_HISTORY_ONLY_TABLE_REGISTRY",	//La storicizzazione e' possibile unicamente per tabelle presenti su almeno una finestra anagrafica o con storicizzabilita' forzata
			HST_ERR_NO_HISTORY_FOR_TABLE_NOT_REGISTRY = "HST_ERR_NO_HISTORY_FOR_TABLE_NOT_REGISTRY",	//Non e' possibile storicizzare una tabella non anagrafica (legata a maschere di tipo Transazione)
			HST_ERR_IMPOSSIBLE_HISTORY_AD_COLUMN = "HST_ERR_IMPOSSIBLE_HISTORY_AD_COLUMN", //Non e' possibile storicizzare colonne di una tabella non storicizzata
			HST_WARN_HISTORY_RECORD_FUTURE_VALIDITY = "HST_WARN_HISTORY_RECORD_FUTURE_VALIDITY",		//Presenti record storici con validita' futura con valori differenti da quello in aggiornamento. La modifica su tali record non sara' propagata		
			HST_ERR_IMPOSSIBLE_MODIFY_COLUMN_NOT_HISTORY_IN_HISTORY_RECORD = "HST_ERR_IMPOSSIBLE_MODIFY_COLUMN_NOT_HISTORY_IN_HISTORY_RECORD", //Non e' possibile modificare colonne non storicizzate su un record di storico
			HST_ERROR_CREATE_HOLE_INTO_HISTORICIZATION = "HST_ERROR_CREATE_HOLE_INTO_HISTORICIZATION",
			HST_DATE_INPUT_AFTER_3000_IS_NOT_SUPPORTED = "HST_DATE_INPUT_AFTER_3000_IS_NOT_SUPPORTED" 
			;
}
