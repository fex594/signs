package fex.signs.tests;

import fex.signs.signs.CommandTransformer;

public class ConverterTest{
	public static void main(String[] args) {
		CommandTransformer ct= CommandTransformer.getInstance();
		ct.getInfo(36);
		ct.getInfo(5);
		System.out.println(ct.getMaxID());
		
	}
}
