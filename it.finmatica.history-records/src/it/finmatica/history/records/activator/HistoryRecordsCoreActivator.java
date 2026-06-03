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
// scoletta@ads.it - History Records Plugin
// Bundle-Activator del plugin core it.finmatica.history-records.
//
// Estende Incremental2PackActivator del core iDempiere (bundle org.adempiere.plugin.utils):
// al boot di iDempiere (evento FrameworkEvent.STARTLEVEL_CHANGED) esegue, in sequenza
// (in un thread del pool iDempiere):
//
//   1) runStructureMigration() — applica gli script SQL embedded in
//      /META-INF/migration/<dbtype>/structure.sql (DDL + INSERT su tabelle "dictionary
//      system": AD_Element, AD_Reference, AD_Ref_List, AD_Table, AD_Column e ALTER/CREATE
//      delle 4 tabelle fisiche del plugin). Eseguito con UNA singola connessione JDBC
//      dedicata + UNA singola transazione → evita il self-deadlock tipico del PackIn iDempiere
//      su Postgres (DDL transazionali, AccessExclusiveLock su pg_class).
//
//   2) installPackage() — pack-in standard via Incremental2PackActivator dei file
//      /META-INF/2Pack_<version>_<desc>.zip che contengono i dati "soft" (AD_Process,
//      AD_Window/Tab/Field, AD_Message, AD_InfoWindow, AD_Menu, AD_Val_Rule, ecc.).
//      Solo INSERT su righe, niente DDL → niente lock pg_class.
//
// Idempotenza:
//   - runStructureMigration(): per-statement try/catch. Ogni statement viene tentato
//     individualmente; se fallisce con SQL state "duplicate object" (Postgres 42701/42P07,
//     Oracle ORA-01430/ORA-00955) → log INFO + continua. Per ogni altro errore SQL →
//     rollback + abort. Questo permette di arricchire structure.sql con nuove ALTER TABLE
//     tra una versione e l'altra del bundle: gli statement già applicati saranno no-op
//     silenziosi, i nuovi verranno eseguiti.
//   - installPackage(): Incremental2PackActivator registra in AD_Package_Imp ogni zip
//     applicato e salta quelli con stessa PK_Version.
//
// Convenzione versioning 2pack:
//   META-INF/2Pack_1.0.0_coredata.zip   → dati cluster CORE
//   META-INF/2Pack_1.0.1_verifydata.zip → dati cluster VERIFY
//   META-INF/2Pack_1.0.2_treesdata.zip  → dati cluster TREES
package it.finmatica.history.records.activator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.adempiere.plugin.utils.Incremental2PackActivator;
import org.adempiere.util.ServerContext;
import org.compiere.Adempiere;
import org.compiere.model.ServerStateChangeEvent;
import org.compiere.model.ServerStateChangeListener;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.framework.FrameworkEvent;

public class HistoryRecordsCoreActivator extends Incremental2PackActivator
{

	private static final CLogger log = CLogger.getCLogger(HistoryRecordsCoreActivator.class);

	private static final String MIGRATION_BASE_PATH = "/META-INF/migration/";
	private static final String MIGRATION_FILE_NAME = "structure.sql";

	/**
	 * scoletta@ads.it - History Records Plugin
	 * Override di frameworkStarted() per intercettare l'evento di start del framework
	 * OSGi e iniettare la chiamata a runStructureMigration() PRIMA di installPackage()
	 * (che è il pack-in standard ereditato).
	 */
	@Override
	public void frameworkEvent(FrameworkEvent event)
	{
		if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED)
		{
			scheduleStartupTasks();
		}
	}

	/**
	 * Programma l'esecuzione delle attività di startup (migration + pack-in) nel thread
	 * pool di iDempiere, oppure registra un listener per quando il server sarà avviato
	 * se non lo è ancora.
	 */
	private void scheduleStartupTasks()
	{
		if (service == null)
			return;

		if (Adempiere.isStarted())
		{
			Adempiere.getThreadPoolExecutor().execute(this::runStartupTasks);
		}
		else
		{
			Adempiere.addServerStateChangeListener(new ServerStateChangeListener()
			{
				@Override
				public void stateChange(ServerStateChangeEvent event)
				{
					if (event.getEventType() == ServerStateChangeEvent.SERVER_START && service != null)
					{
						runStartupTasks();
					}
				}
			});
		}
	}

	/**
	 * Esegue in sequenza: migration di structure (SQL embedded) + pack-in standard 2pack.
	 * Tutto avviene dentro lo stesso thread, in modo che il pack-in trovi le tabelle
	 * fisiche e i record dictionary di structure già pronti.
	 */
	private void runStartupTasks()
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(HistoryRecordsCoreActivator.class.getClassLoader());
			setupPackInContext();

			runStructureMigration();   // 1) DDL + dictionary system records (single tx)
			installPackageWrapper();   // 2) pack-in zip standard
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "Startup tasks failed for " + getName(), ex);
		}
		finally
		{
			ServerContext.dispose();
			service = null;
			Thread.currentThread().setContextClassLoader(cl);
		}
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * Wrapper per chiamare il pack-in standard ereditato. Il metodo originale
	 * installPackage() di Incremental2PackActivator è private; per riusarlo lo
	 * chiamiamo indirettamente attraverso la stessa logica del super.frameworkEvent(),
	 * MA senza re-fire dell'evento. Usiamo reflection sul metodo privato.
	 */
	private void installPackageWrapper()
	{
		try
		{
			java.lang.reflect.Method m = Incremental2PackActivator.class.getDeclaredMethod("installPackage");
			m.setAccessible(true);
			m.invoke(this);
		}
		catch (NoSuchMethodException e)
		{
			log.log(Level.SEVERE, "Cannot find installPackage() in Incremental2PackActivator. Pack-in skipped.", e);
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "installPackage() failed", e);
		}
	}

	// ============================================================
	// STRUCTURE MIGRATION
	// ============================================================

	/**
	 * Applica lo script SQL di migration di structure (DDL fisiche).
	 *
	 * Idempotency: per-statement try/catch. Se uno statement fallisce con codice
	 * "duplicate object" (colonna/tabella già esistente) → log INFO + continua.
	 * Per ogni altro errore SQL → rollback + abort.
	 *
	 * Questo permette di ARRICCHIRE structure.sql con nuove ALTER TABLE tra un release
	 * e l'altra: gli statement già applicati saranno no-op silenziosi, i nuovi
	 * verranno eseguiti.
	 */
	private void runStructureMigration()
	{
		String dbDialect = detectDbDialect();
		String resourcePath = MIGRATION_BASE_PATH + dbDialect + "/" + MIGRATION_FILE_NAME;
		URL scriptUrl = context.getBundle().getEntry(resourcePath);
		if (scriptUrl == null)
		{
			log.warning("Structure migration script not found: " + resourcePath + ". Skipping migration.");
			return;
		}

		List<String> statements = loadAndSplitStatements(scriptUrl);
		if (statements.isEmpty())
		{
			log.warning("Structure migration script is empty: " + resourcePath);
			return;
		}

		log.warning("Applying structure migration for " + getName()
				+ " — dialect=" + dbDialect + ", statements=" + statements.size() + " ...");

		Connection conn = null;
		Statement stmt = null;
		String currentSql = null;
		int applied = 0;
		int skipped = 0;
		try
		{
			conn = DB.getConnection(true);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();

			for (String sql : statements)
			{
				currentSql = sql;
				try
				{
					stmt.execute(sql);
					applied++;
					conn.commit();
				}
				catch (java.sql.SQLException sqlEx)
				{
					if (isDuplicateObjectError(sqlEx))
					{
						log.info("Skipping (already applied): " + firstWords(sql, 80));
						// Postgres: dopo una SQLException la transazione va in stato "abort",
						// quindi va fatto savepoint+rollback to savepoint per continuare.
						// Più semplice: facciamo rollback + ricominciamo la transazione.
						conn.rollback();
						stmt.close();
						stmt = conn.createStatement();
						skipped++;
					}
					else
					{
						throw sqlEx;
					}
				}
			}
			
			log.warning("Structure migration completed: " + applied + " applied, " + skipped + " skipped (already present).");
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "Structure migration failed at statement:\n" + currentSql, ex);
			try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
			throw new RuntimeException("Structure migration aborted", ex);
		}
		finally
		{
			DB.close(stmt);
			if (conn != null) {
				try { conn.setAutoCommit(true); } catch (Exception ignore) {}
				try { conn.close(); } catch (Exception ignore) {}
			}
		}
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * True se l'errore SQL corrisponde a "oggetto già esistente":
	 *   DDL duplicate object:
	 *     - Postgres: SQLSTATE 42701 (duplicate_column) / 42P07 (duplicate_table)
	 *     - Oracle:   ORA-01430 (column already exists) / ORA-00955 (name already used)
	 *   DML unique violation (INSERT su PK già presente):
	 *     - Postgres: SQLSTATE 23505 (unique_violation)
	 *     - Oracle:   ORA-00001 (unique constraint violated)
	 */
	private boolean isDuplicateObjectError(java.sql.SQLException ex)
	{
		String state = ex.getSQLState();
		if ("42701".equals(state) || "42P07".equals(state) || "23505".equals(state))
			return true;
		int code = ex.getErrorCode();
		if (code == 1430 || code == 955 || code == 1)
			return true;
		// fallback testuale
		String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
		return msg.contains("already exists")
				|| msg.contains("ora-01430") || msg.contains("ora-00955") || msg.contains("ora-00001")
				|| msg.contains("duplicate column") || msg.contains("duplicate table")
				|| msg.contains("duplicate key") || msg.contains("unique constraint");
	}

	/** Tronca uno statement SQL alle prime N parole per logging conciso. */
	private String firstWords(String sql, int maxLen)
	{
		String s = sql.replaceAll("\\s+", " ").trim();
		return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
	}

	/** Ritorna "postgresql" o "oracle" in base al dialetto rilevato. */
	private String detectDbDialect()
	{
		String dbName = DB.getDatabase().getName().toLowerCase();
		if (dbName.contains("postgres")) return "postgresql";
		return "oracle";
	}

	/**
	 * Carica lo script SQL dal classpath e lo splitta sui terminatori statement (';').
	 * Gestisce: linee di commento (--, /* ... * /), stringhe single-quote, statement multilinea.
	 */
	private List<String> loadAndSplitStatements(URL scriptUrl)
	{
		List<String> statements = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inSingleQuote = false;
		boolean inLineComment = false;
		boolean inBlockComment = false;

		try (InputStream is = scriptUrl.openStream();
			 BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
		{
			int ch;
			char prev = 0;
			while ((ch = rd.read()) != -1)
			{
				char c = (char) ch;

				if (inLineComment)
				{
					if (c == '\n') { inLineComment = false; current.append(c); }
					prev = c;
					continue;
				}
				if (inBlockComment)
				{
					if (prev == '*' && c == '/') inBlockComment = false;
					prev = c;
					continue;
				}
				if (!inSingleQuote && prev == '-' && c == '-')
				{
					// rimuovi il '-' già aggiunto, segna commento di linea
					current.deleteCharAt(current.length() - 1);
					inLineComment = true;
					prev = c;
					continue;
				}
				if (!inSingleQuote && prev == '/' && c == '*')
				{
					current.deleteCharAt(current.length() - 1);
					inBlockComment = true;
					prev = c;
					continue;
				}
				if (c == '\'')
				{
					inSingleQuote = !inSingleQuote;
				}
				if (c == ';' && !inSingleQuote)
				{
					String s = current.toString().trim();
					if (!s.isEmpty()) statements.add(s);
					current.setLength(0);
					prev = c;
					continue;
				}
				current.append(c);
				prev = c;
			}
			String tail = current.toString().trim();
			if (!tail.isEmpty()) statements.add(tail);
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "Failed reading migration script: " + scriptUrl, ex);
		}
		return statements;
	}
}
