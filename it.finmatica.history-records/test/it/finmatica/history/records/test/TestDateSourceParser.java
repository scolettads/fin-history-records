package it.finmatica.history.records.test;

import org.compiere.model.history.DateSourceUtil;
import org.compiere.model.history.TableAndField;
import org.junit.Assert;
import org.junit.Test;

public class TestDateSourceParser
{
	public static final String TEST_SUBQUERY = "(select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#)";
	
	public static final int TEST_ID = 100001;
	public static final String TEST_SUBQUERY_ID = "(select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = 100001)";
	
	public static final String TEST_TABLE = "o";
	public static final String TEST_SUBQUERY_TABLE = "(select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = o.c_order_id)";

	@Test
	public void testParseDateSource()
	{
		TableAndField taf = DateSourceUtil.parseTableAndField("c_orderline.c_order_id#");
		Assert.assertNull(taf);
		
		taf = DateSourceUtil.parseTableAndField("#.#");
		Assert.assertNull(taf);

		taf = DateSourceUtil.parseTableAndField("#c_orderline.c_order_id#");
		Assert.assertEquals("c_orderline", taf.getTable());
		Assert.assertEquals("c_order_id", taf.getField());
	}
	
	@Test
	public void validateOk()
	{
		boolean bValid = DateSourceUtil.validateSubquery(TEST_SUBQUERY);
		Assert.assertTrue(bValid);
	}
	
	@Test
	public void testId()
	{
		String parsed = DateSourceUtil.replaceInSubquery(TEST_SUBQUERY, TEST_ID);
		Assert.assertEquals(TEST_SUBQUERY_ID, parsed);		
	}
	
	@Test
	public void testTable()
	{
		String parsed = DateSourceUtil.replaceInSubquery(TEST_SUBQUERY, TEST_TABLE);
		Assert.assertEquals(TEST_SUBQUERY_TABLE, parsed);
	}

}
