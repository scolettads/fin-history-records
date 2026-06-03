package it.finmatica.history.records.test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.compiere.model.history.HistorySelectionData;
import org.junit.Assert;
import org.junit.Test;

import it.finmatica.history.records.sql.ADDateSourceResolver;
import it.finmatica.history.records.sql.ADHistoryReplacer;
import it.finmatica.history.records.sql.DateSourceResolver;
import it.finmatica.history.records.sql.FromTable;
import it.finmatica.history.records.sql.HistoryReplacer;
import it.finmatica.history.records.sql.HistorySQLParser;
import it.idempiere.util.QueryRepo;

public class TestSqlRewrite
{
	public static final DateSourceResolver RESOLVER = new ADDateSourceResolver();
	
	public static final String QUERY_BASE = "it/finmatica/history/records/test/sql/";
	public static final String REWRITE = "_rewrite";
	public static final String TEST_SUBSELECT = QUERY_BASE + "query_subselect";
	public static final String TEST_NSTRING = QUERY_BASE + "n-string";
	public static final String TEST_ENDCASE = QUERY_BASE + "end-case";
	public static final String TEST_EXPORTRIGHECORDERLINE1 = QUERY_BASE + "export_righe_c_orderline_1";
	public static final String TEST_INFOBPARTNER = QUERY_BASE + "info_bpartner";
	public static final String TEST_INFOBPARTNER2 = QUERY_BASE + "info_bpartner_2";	
	public static final String TEST_INFOBPARTNER_COUNT = QUERY_BASE + "info_bpartner_count";
	public static final String TEST_LOOKUP_ORDINE_INDIRIZZO_BPARTNER = QUERY_BASE + "lookup_ordine_indirizzo_bpartner";
	public static final String TEST_LOOKUP_ORDINE_INDIRIZZO_FATTURAZIONE = QUERY_BASE + "lookup_ordine_indirizzo_fatturazione";
	public static final String TEST_STAMPAPARTITEAPERTE = QUERY_BASE + "stampa_partite_aperte";
	public static final String TEST_INNERJOIN1 = QUERY_BASE + "inner-join-1";
	public static final String TEST_INNERJOIN2 = QUERY_BASE + "inner-join-2";
	public static final String TEST_INNERJOIN3 = QUERY_BASE + "inner-join-3";
	public static final String TEST_OUTERJOIN1 = QUERY_BASE + "outer-join-1";
	public static final String TEST_ACLSUBQUERY = QUERY_BASE + "acl-subquery";
	public static final String TEST_NATIVE_POSTGRESQL = QUERY_BASE + "native-postgresql";
	
	@Test
	public void rewriteTableField() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("#c_invoice.dateacct#");		
		HistoryReplacer	replacer = new TestHistoryReplacer("c_doctype");

		parseAndCompare(TEST_SUBSELECT, hsd, replacer);
	}
	
	@Test
	public void rewriteNString() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("#c_invoice.dateacct#");		
		HistoryReplacer	replacer = new TestHistoryReplacer("c_doctype");
		
		parseAndCompare(TEST_NSTRING, hsd, replacer);		
	}
	
	@Test
	public void rewriteInnerJoin1() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#","c_orderline");		
		HistoryReplacer	replacer = new TestHistoryReplacer("m_product");
		
		parseAndCompare(TEST_INNERJOIN1, hsd, replacer);		
	}
	
	@Test
	public void rewriteInnerJoin2() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#","c_orderline");
		HistoryReplacer	replacer = new TestHistoryReplacer("m_product");
		
		parseAndCompare(TEST_INNERJOIN2, hsd, replacer);		
	}
	
	@Test
	public void rewriteInnerJoin3() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#","c_orderline");		
		HistoryReplacer	replacer = new TestHistoryReplacer("m_product");
		
		parseAndCompare(TEST_INNERJOIN3, hsd, replacer);		
	}
	
	@Test
	public void rewriteOuterJoin1() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#","c_orderline");		
		HistoryReplacer	replacer = new TestHistoryReplacer("m_product");
		
		parseAndCompare(TEST_OUTERJOIN1, hsd, replacer);		
	}
	
	@Test
	public void rewriteExportRigheCOrderLine1() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#",1007279);		
		HistoryReplacer	replacer = new TestHistoryReplacer("m_product");
		
		parseAndCompare(TEST_EXPORTRIGHECORDERLINE1, hsd, replacer);		
	}
	
	@Test
	public void rewriteInfoBPartner1() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#",1007279);
		HistoryReplacer	replacer = new TestHistoryReplacer("C_BPartner","C_BPartner_Location");
		
		parseAndCompare(TEST_INFOBPARTNER, hsd, replacer);		
	}
	
	@Test
	public void rewriteInfoBPartner2() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#","c_orderline");		
		HistoryReplacer	replacer = new TestHistoryReplacer("C_BPartner","C_BPartner_Location");
		
		parseAndCompare(TEST_INFOBPARTNER2, hsd, replacer);		
	}
	
	@Test
	public void rewriteInfoBPartnerCount() throws Exception
	{
		@SuppressWarnings("deprecation")
		Timestamp tsRefDate = new Timestamp(117, 11, 31, 0, 0, 0, 0);
		
		HistorySelectionData hsd = new HistorySelectionData(tsRefDate);		
		HistoryReplacer	replacer = new TestHistoryReplacer("C_BPartner","C_BPartner_Location");
		
		parseAndCompare(TEST_INFOBPARTNER_COUNT, hsd, replacer);		
	}
	
	@Test
	public void rewriteLookupOrdineIndirizzoBPartner() throws Exception
	{
		@SuppressWarnings("deprecation")
		Timestamp tsRefDate = new Timestamp(117, 11, 31, 0, 0, 0, 0);
		
		HistorySelectionData hsd = new HistorySelectionData(tsRefDate);		
		HistoryReplacer	replacer = new TestHistoryReplacer("C_BPartner_Location");
		
		parseAndCompare(TEST_LOOKUP_ORDINE_INDIRIZZO_BPARTNER, hsd, replacer);		
	}
	
	@Test
	public void rewriteOrdineIndirizzoFatturazione() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#",123);		
		HistoryReplacer	replacer = new TestHistoryReplacer("C_BPartner_Location");
		
		parseAndCompare(TEST_LOOKUP_ORDINE_INDIRIZZO_FATTURAZIONE, hsd, replacer);		
	}
	
	@Test
	public void rewriteStampaPartiteAperte() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("#RV_OpenItem.DateInvoiced#");		
		HistoryReplacer	replacer = new TestHistoryReplacer("C_Activity","C_Activity_Trl","C_BPartner");
		
		parseAndCompare(TEST_STAMPAPARTITEAPERTE, hsd, replacer);
	}
	
	@Test
	public void rewriteACLSubquery() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#","c_orderline");	
		HistoryReplacer	replacer = new TestHistoryReplacer("m_product");
		
		parseAndCompare(TEST_ACLSUBQUERY, hsd, replacer);
	}
	
	@Test
	public void rewriteNativePG() throws Exception
	{
		@SuppressWarnings("deprecation")
		Timestamp tsRefDate = new Timestamp(117, 11, 31, 0, 0, 0, 0);		
		HistorySelectionData hsd = new HistorySelectionData(tsRefDate);
		HistoryReplacer	replacer = new TestHistoryReplacer("c_invoice");
		
		parseAndCompare(TEST_NATIVE_POSTGRESQL, hsd, replacer);
	}
	
	private void parseAndCompare(String sqlName,HistorySelectionData hsd, HistoryReplacer	replacer) throws Exception
	{
		String statement = QueryRepo.getQuery(sqlName, this.getClass().getClassLoader());
		String rewrite = QueryRepo.getQuery(sqlName + REWRITE, this.getClass().getClassLoader());
				
		HistorySQLParser sqlParser = new NODBHistoryDateParser(statement, hsd);
		String parsed = sqlParser.process(replacer, RESOLVER);
		
		Assert.assertEquals(normalize(rewrite), normalize(parsed));
	}
	
	private String normalize(String text)
	{
		return text.toLowerCase()
					.replaceAll("\n", "")
					.replaceAll("\r","")
					.replaceAll("\t","")
					.replaceAll(" ","");
	}
		
	@Test
	public void rewriteEndCase() throws Exception
	{
		HistorySelectionData hsd = new HistorySelectionData("#c_invoice.dateacct#");		
		HistoryReplacer	replacer = new TestHistoryReplacer("c_doctype");
		
		parseAndCompare(TEST_ENDCASE, hsd, replacer);		
	}

	public static class TestHistoryReplacer extends ADHistoryReplacer
	{
		private List<String> tableNames = new ArrayList<>();
		
		public TestHistoryReplacer(String ... tableNames)
		{
			for(String tn:tableNames)
			{
				this.tableNames.add(tn.toLowerCase());
			}
		}
		
		@Override
		public boolean hasHistory(FromTable fromTable)
		{
			String lowerTable = fromTable.table.toLowerCase();
			
			return tableNames.contains(lowerTable);
		}
		
	}
}
