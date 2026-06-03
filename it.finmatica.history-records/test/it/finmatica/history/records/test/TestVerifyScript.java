package it.finmatica.history.records.test;

import org.junit.Assert;
import org.junit.Test;

import it.finmatica.history.records.validator.DateSourceConfValidator;

public class TestVerifyScript 
{
	@Test
	public void test() 
	{
		String [] valuesOK = {"select * from XHSTX where XHSTX.test_id = 12345 AND XHSTX.test_name = #Name# ",
								"# aaaaaaa #",
								"#bbbbb#",
								"#e#e#e#",
								"# a # ",
								" # asdeer #",
								"#aaa#\n",
								"\n#aaa#",
								"#aaa#\t",
								"\t#aaa#"};
		
		String [] valuesNotOK = {"select * from XHSTXwhere XHSTX.test_id = 12345 AND XHSTX.test_name = #pluto# ",
									"select XHSTX.test_id from XHSTX;",
									"select * from XHSTX ##",
									"# aaa # aa",
									"# ert#eaer#e",
									"test",
									"aa# aadse #"};
		
		for(String script : valuesOK)
			Assert.assertEquals(DateSourceConfValidator.verifyScript(script), true);
		
		for(String script : valuesNotOK)
			Assert.assertEquals(DateSourceConfValidator.verifyScript(script), false);
	}
}
