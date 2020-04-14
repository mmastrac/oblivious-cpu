package com.grack.hidecpu2.assembler;

import java.io.IOException;
import java.util.Arrays;

import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;

import static org.junit.Assert.*;

@RunWith(Theories.class)
public class CompilerTest {
	/**
	 * Attempt to compile all the samples.
	 */
	@Theory
	public void compileFile(@ParametersSuppliedBy(AllTests.class) String file) throws IOException {
		CharSource src = Resources.asCharSource(getClass().getResource("/" + file), Charsets.UTF_8);
		Parser parser = new Parser(src);
		Program program = parser.parse();
		Compiler compiler = new Compiler();
		compiler.compile(program);
	}
	
	/**
	 * Attempt to assemble all the samples.
	 */
	@Theory
	public void assembleFile(@ParametersSuppliedBy(AllTests.class) String file) throws IOException {
		CharSource src = Resources.asCharSource(getClass().getResource("/" + file), Charsets.UTF_8);
		Parser parser = new Parser(src);
		Program program = parser.parse();
		Compiler compiler = new Compiler();
		compiler.compile(program);
		int[] code = program.getProgram("code");
		int[] data = program.getProgram("data");
		
		assertTrue(code.length > 0);
		assertTrue(data.length > 0);
		
		System.out.println(file);
		System.out.println(program);
		System.out.println("code: " + Arrays.toString(code));
		System.out.println("data: " + Arrays.toString(data));
	}
}
