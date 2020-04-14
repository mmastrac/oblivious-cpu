package com.grack.hidecpu2.assembler;

import java.io.IOException;

import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;

@RunWith(Theories.class)
public class ParserTest {
	/**
	 * Attempt to parse all the samples.
	 */
	@Theory
	public void parseFile(@ParametersSuppliedBy(AllTests.class) String file) throws IOException {
		CharSource src = Resources.asCharSource(getClass().getResource("/" + file), Charsets.UTF_8);
		Parser parser = new Parser(src);
		Program program = parser.parse();
//		System.out.println(file);
//		System.out.println(program);		
	}
}
