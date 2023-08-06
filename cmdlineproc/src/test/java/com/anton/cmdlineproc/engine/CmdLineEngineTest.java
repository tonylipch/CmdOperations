package com.anton.cmdlineproc.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.beans.IntrospectionException;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.anton.cmdlineproc.annotations.Option;
import com.anton.cmdlineproc.sample.Params;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

class CmdLineEngineTest  {

	
	@Test
	void testHelp() {
		var engine = new CmdLineEngine(new Params(), "");
		engine.provideHelp();
		assertEquals(4, engine.getOutput().size());
	}
	
	@Test
	void testHelpByCommandLine() {
		var engine = new CmdLineEngine(new Params(), "");
		engine.parseCommandLine(new String [] {"--help"});
		assertEquals(4, engine.getOutput().size());
	}
	
	
	@Test
	void testParseInteger() {
		assertEquals(Integer.valueOf(12), CmdLineEngine.parse(Integer.class, "12")); 
	}
	
	@Test
	void testParseFloat() {
		assertEquals(Float.valueOf(12), CmdLineEngine.parse(Float.class, "12")); 
	}
	
	@Test
	void testParseDouble() {
		assertEquals(Double.valueOf(12), CmdLineEngine.parse(Double.class, "12")); 
	}
	
	@Test
	void testParseLong() {
		assertEquals(Long.valueOf(12), CmdLineEngine.parse(Long.class, "12")); 
	}
	
	@Test
	void testParseString() {
		assertEquals("12", CmdLineEngine.parse(String.class, "12"));
	}
	
	public enum TestEnum {
		A, B
	}
	
	@Test
	void testParseEnum() {
		assertEquals(TestEnum.A, CmdLineEngine.parse(TestEnum.class, "A"));
	}
	
	@Test
	void testParseBigDecimal() {
		assertEquals(BigDecimal.valueOf(12), CmdLineEngine.parse(BigDecimal.class, "12")); 
	}
	
	@Test
	void testDefaults() {
		var params = new Params();
		var parser = new CmdLineEngine(params, "");
		assertTrue(parser.parseCommandLine(new String[0]));
		assertEquals(0, params.getLeft());
		assertEquals(1, params.getRight());
		assertEquals(Params.Operation.plus, params.getOperation());
	}
	

	@Test
	void testParseCommandLine() {
		var params = new Params();
		var parser = new CmdLineEngine(params, "");
		assertTrue(parser.parseCommandLine(new String[] {"-l", "10", "--right", "2", "-o", "mul"}));
		assertEquals(10, params.getLeft());
		assertEquals(2, params.getRight());
		assertEquals(Params.Operation.mul, params.getOperation());
	}
	
	static class WrongBean {
		Integer fieldA;
		Integer fieldB;
	}
	
	@Test
	void testWrongBean() {
		// class does not match to parameters bean requirements at all
		var thrown = assertThrows(
		           ParseException.class,
		           () -> new CmdLineEngine(new WrongBean(), ""),
		           "Expected ParseException to throw, but it didn't"
		    );

		    assertTrue(thrown.getMessage().equals("Couldn't get parameters declaration in class com.anton.cmdlineproc.engine.CmdLineEngineTest$WrongBean. Use com.anton.cmdlineproc.annotations.Option for declare parameters."));
	}
	
	static class WrongMarkup1 {
		@Option(shortName="a", longName="a")
		@Getter @Setter
		Integer fieldA;
		
		@Option(shortName="b", longName="b")
		Integer fieldB;
	}
	
	@Test
	void testWrongMarkup1() {
		// class does not have all the necessary getters and setters
		var thrown = assertThrows(
		           ParseException.class,
		           () -> new CmdLineEngine(new WrongMarkup1(), ""),
		           "Expected ParseException to throw, but it didn't"
		    );
		    assertTrue(thrown.getMessage().equals("Couldn't get property descriptors for fields of [fieldB] of bean class com.anton.cmdlineproc.engine.CmdLineEngineTest$WrongMarkup1. Make sure that you provided necessary getters and setters"));
	}

	@Data
	static class WrongMarkup2 {
		@Option(shortName="a", longName="a")
		Integer fieldA;
		
		@Option(shortName="a", longName="a")
		Integer fieldB;
	}
	
	@Test
	void testWrongMarkup2() {
		// parameters bean has dublicates in parameter name declarations 
		var thrown = assertThrows(
		           ParseException.class,
		           () -> new CmdLineEngine(new WrongMarkup2(), ""),
		           "Expected ParseException to throw, but it didn't"
		    );
		    assertTrue(thrown.getMessage().equals("Dublicate declaration of parameter '-a', see fields fieldA, fieldB"));
	}
	
	@Data
	static class TestParams {
		@Option(shortName="a", longName="a")
		Integer fieldA;
		
		@Option(shortName="b", longName="b", required = true)
		Integer fieldB;
	}
	
	
	@Test
	void testMissingParameters() {
		var params = new TestParams();
		var paramsEngine = new CmdLineEngine(params, "");
		assertFalse(paramsEngine.parseCommandLine(new String[] {"-a", "10"}));
		var output = paramsEngine.getOutput();
		assertEquals(2, output.size());
		assertEquals("-b( --b )  mandatory  ", output.get(1));
	}
	
	@Test
	void testGetMeta() throws IntrospectionException {
		var meta = CmdLineEngine.getFieldsMeta(Params.class);
		assertEquals(3, meta.size());
		assertEquals("-l", meta.get(0).getShortName());
		assertEquals("-r", meta.get(1).getShortName());
		assertEquals("-o", meta.get(2).getShortName());
		assertEquals(Params.Operation.class, meta.get(2).getFieldType());
	}
	
	
}
