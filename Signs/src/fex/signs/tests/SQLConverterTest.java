package fex.signs.tests;

import org.junit.jupiter.api.Test;

import fex.signs.signs.CommandTransformer;

class SQLConverterTest {
	
	
	@Test
	void testVorhanden() {
		CommandTransformer ct = CommandTransformer.getInstance();
		ct.getInfo(36);
		
	}
	
	@Test
	void testNichtVorhanden() {
		CommandTransformer ct = CommandTransformer.getInstance();
		ct.getInfo(5);
	}
	
	@Test
	void maxID() {
		CommandTransformer ct = CommandTransformer.getInstance();
		System.out.println(ct.getMaxID());
	}

}
