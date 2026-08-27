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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.dbPort.ISQLStatementRewriter;
import it.finmatica.history.records.internal.compiere.history.HSTSysConfig;
import it.finmatica.history.records.internal.compiere.history.HistorySelectionData;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

import it.finmatica.history.records.sql.ADDateSourceResolver;
import it.finmatica.history.records.sql.ADHistoryReplacer;
import it.finmatica.history.records.sql.HistorySQLParser;

public class HistorySQLRewriter implements ISQLStatementRewriter {
	
	private static final String SELECT = "select";
	private static final String ACL_CLASS = "ACLGenericModelEventsHandler";
	private static final String ACL_METHOD = "availableForWrite";
	private static final CLogger log = CLogger.getCLogger(HistorySQLRewriter.class);
	
	// Cache tabelle storicizzate
	private List<String> historicizedTables = null;
	private long lastRefresh = 0L;
	private static final long CACHE_TTL_MS = 30 * 60 * 1000; // 30 minuti
	private static final ThreadLocal<Boolean> disableRewrite = ThreadLocal.withInitial(() -> Boolean.FALSE);
	
	private ADDateSourceResolver	resolver = new ADDateSourceResolver();
	private ADHistoryReplacer replacer = new ADHistoryReplacer();
	private Boolean						timeMachineDisabled = null;
	
	// Statistiche
	
	private static final boolean ENABLE_STATS = true;
	private static final long PRINT_EVERY_MS = 10*60*1000; // una volta ogni 10 minuti
	private long	rewriteOverhead = 0;
	private long	rewriteCount = 0;
	private long	evaluatedCount = 0;
	private long	cacheHits = 0;
	private long	nextPrintTimestamp = System.currentTimeMillis() + PRINT_EVERY_MS;
	
	@Override
	public String rewriteStatements(String sqlStatements) {
		
		HistorySelectionData hsd = HistorySelectionData.getCurrent();
		
		if(hsd != null && hsd.isDisableTimeMachine())
			return sqlStatements;
		
		if(isTimeMachineDisabled())
			return sqlStatements;
		
		long nanoTime = System.nanoTime();
		
		// is a select ?
		
		String check = sqlStatements.trim().toLowerCase();
		
		if(!check.startsWith(SELECT) || stacktraceContains(ACL_CLASS, ACL_METHOD) || !doesSelectContainHistoricizedTable(check))
			return sqlStatements;
		
		ReplacerStats stats = new ReplacerStats();
		
		HistorySQLParser parser = new HistorySQLParser(sqlStatements, hsd, stats);
		
		String parsed = null;
		
		try {
			parsed = parser.process(replacer,resolver);
		
			if(parsed == null)
				parsed = sqlStatements;
			else if(ENABLE_STATS)
				rewriteCount++;
		}
		catch(Exception t) {
			log.log(Level.WARNING, "HST: error parsing statement", t);
			log.log(Level.WARNING, sqlStatements);
			parsed = sqlStatements;
		}
				
		if(ENABLE_STATS && log.isLoggable(Level.WARNING)) {
			nanoTime = System.nanoTime() - nanoTime;
			rewriteOverhead += nanoTime;
			evaluatedCount++;
			
			if(stats.foundInCache)
				cacheHits++;
					
			long msNow = System.currentTimeMillis();
			
			if(msNow > nextPrintTimestamp) {
				nextPrintTimestamp = msNow + PRINT_EVERY_MS;
				
				StringBuilder sbLineOne = new StringBuilder(">>>>>>> HST STATS: Total sql rewrite ops: ");
				
				sbLineOne.append(rewriteCount).append('/').append(evaluatedCount);
				sbLineOne.append(", overhead (ms): ").append(rewriteOverhead/1000000);
				
				log.warning(sbLineOne.toString());
				
				StringBuilder sbLineTwo = new StringBuilder(">>>>>>> HST CACHE STATS: Cache size ");
				sbLineTwo.append(stats.cacheSize);
				sbLineTwo.append(", cacheHits = ").append(cacheHits);
				
				log.warning(sbLineTwo.toString());
			}
		}
						
		return parsed;
	}
	
	public boolean isTimeMachineDisabled() {

		if(timeMachineDisabled != null)
			return timeMachineDisabled;
		// s.coletta@ads.it 2026-08-27 - break re-entrant recursion: HSTSysConfig sets
		// DISABLE_TIMEMACHINE before calling MSysConfig, which re-enters here via DB layer;
		// treat infra queries as if the time machine is disabled to avoid stack overflow.
		HistorySelectionData hsd = HistorySelectionData.getCurrent();
		if(hsd != null && hsd.isDisableTimeMachine())
			return true;
		timeMachineDisabled = HSTSysConfig.isTimeMachineDisabled();
		return timeMachineDisabled;
	}

	@Override
	public boolean rewriteIsCacheable() {

		HistorySelectionData hsd = HistorySelectionData.getCurrent();
		if(hsd != null && hsd.isDisableTimeMachine())
			return true;
		return isTimeMachineDisabled();
	}
	
	private boolean stacktraceContains(String className, String method) {
		
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		for(StackTraceElement trace : stackTrace) {
			if(trace.getClassName().contains(className) && trace.getMethodName().equals(method))
				return true;
		}
		
		return false;
	}
	
	private boolean doesSelectContainHistoricizedTable(String statement) {
		
		// Disabilito il check per la query sulle tabelle storicizzate per evitare la ricorsione
		if(Boolean.TRUE.equals(disableRewrite.get()))
	        return false;
		
		long now = System.currentTimeMillis();
	    
	    // Se la cache è vuota o scaduta, ricarico
	    if(historicizedTables == null || (now - lastRefresh) > CACHE_TTL_MS) {
	    	List<String> tablesList = new ArrayList<>();
	        String sql = "SELECT TableName FROM AD_Table WHERE HST_HistoryMode != 'N'";
	        disableRewrite.set(true);
	        try(PreparedStatement ps = DB.prepareStatement(sql, null);
	            ResultSet rs = ps.executeQuery()) {
	            while(rs.next()) {
	                tablesList.add(rs.getString(1).toLowerCase());
	            }
	        } catch(Exception e) {
	            log.log(Level.WARNING, "Errore nella ricerca delle tabelle storicizzate", e);
	            return false;
	        } finally {
	            disableRewrite.remove();
	        }
	        historicizedTables = tablesList;
	        lastRefresh = now;
	    }

	    // Controlla lo statement per la presenza di tabelle storicizzate
	    for(String table : historicizedTables) {
	    	statement = statement.toLowerCase();
	    	if(statement.contains(" " + table + " ") || 		// nome tabella semplice
	    			statement.contains(" " + table + ",") ||	// clausola from con multiple tabelle
	    			statement.contains(" " + table + ")") ||	// subquery
	    			statement.contains(" " + table + "\n") ||	// fine riga dopo il nome tabella
	    			statement.contains(" " + table + "\t"))		// tab dopo il nome tabella
	            return true;
	    }
		
		return false;
	}

}
