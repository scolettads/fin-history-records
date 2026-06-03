package it.finmatica.history.records.test;

import java.sql.Timestamp;
import java.text.MessageFormat;

import org.compiere.model.history.HistorySelectionData;

import it.finmatica.history.records.extension.ReplacerStats;
import it.finmatica.history.records.sql.HistorySQLParser;

public class NODBHistoryDateParser extends HistorySQLParser
{
	public static final MessageFormat DATE_FORMAT = new MessageFormat("DATE''{0,date,yyyy-MM-dd}''");
	private static ReplacerStats stats = new ReplacerStats();
	
	public NODBHistoryDateParser(String stmt, HistorySelectionData hsd)
	{
		super(stmt, hsd, stats);
	}

	@Override
	public String timestampToString(Timestamp ts)
	{
		Object params[] = {ts}; 
		return DATE_FORMAT.format(params);
	}
}