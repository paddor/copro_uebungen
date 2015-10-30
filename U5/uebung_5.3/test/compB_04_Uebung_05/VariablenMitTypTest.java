package compB_04_Uebung_05;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VariablenMitTypTest {
	
	ByteArrayOutputStream outStream, errStream;

	@Before
	public void setUp() throws Exception {
		outStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outStream));
		errStream = new ByteArrayOutputStream();
		System.setErr(new PrintStream(errStream));
	}

	@After
	public void tearDown() throws Exception {
		System.setOut(null);
		System.setErr(null);
	}

	final String outCompiledWithCorrectTypes =
		"[PROGRAM.input]\n" + 
		"PROGRAM;\n" + 
		"VAR LONG A,WIDE B;\n" + 
		"VAR BYTE C, BYTE D;\n" + 
		"BEGIN\n" + 
		"  B=10;\n" + 
		"  A=5*B;\n" + 
		"  WRITE(A);\n" + 
		"END.\n" + 
		"\n" + 
		"[PROGRAM.output]\n" + 
		"\n" + 
		"	WARMST	EQU $A01E\n" + 
		"A:	DC.L 0\n" + 
		"B:	DC.W 0\n" + 
		"C:	DC.B 0\n" + 
		"D:	DC.B 0\n" + 
		"MAIN:\n" + 
		"	MOVE #10, D0\n" + 
		"	LEA B(PC), A0\n" + 
		"	MOVE D0, (A0)\n" + 
		"	MOVE #5, D0\n" + 
		"	MOVE D0, -(SP) \n" + 
		"	MOVE B(PC), D0\n" + 
		"	MULS (SP)+, D0\n" + 
		"	LEA A(PC), A0\n" + 
		"	MOVE D0, (A0)\n" + 
		"	MOVE A(PC), D0\n" + 
		"	BSR WRITE\n" + 
		"	DC WARMST\n" + 
		"	END MAIN\n";

	@Test
	public void testWithCorrectTypes() throws IOException {
		VariablenMitTyp vmt = new VariablenMitTyp("test/Test_mitTypen");
		vmt.compile();
		assertEquals(outCompiledWithCorrectTypes,
				outStream.toString());
		assertEquals("", errStream.toString());
	}


	final String outCompiledWithIncorrectTypes =
			"[PROGRAM.input]\n" + 
			"PROGRAM;\n" + 
			"VAR int A,WIDE B;\n" + 
			"VAR BYTE C, LONG D;\n" + 
			"BEGIN\n" + 
			"  B=10;\n" + 
			"  C=5;\n" + 
			"  D=B*C;\n" + 
			"  WRITE(D);\n" + 
			"END.\n" + 
			"\n" + 
			"[PROGRAM.output]\n" + 
			"\n" + 
			"	WARMST	EQU $A01E\n" + 
			"B:	DC.W 0\n" + 
			"C:	DC.B 0\n" + 
			"D:	DC.L 0\n" + 
			"MAIN:\n" + 
			"	MOVE #10, D0\n" + 
			"	LEA B(PC), A0\n" + 
			"	MOVE D0, (A0)\n" + 
			"	MOVE #5, D0\n" + 
			"	LEA C(PC), A0\n" + 
			"	MOVE D0, (A0)\n" + 
			"	MOVE B(PC), D0\n" + 
			"	MOVE D0, -(SP) \n" + 
			"	MOVE C(PC), D0\n" + 
			"	MULS (SP)+, D0\n" + 
			"	LEA D(PC), A0\n" + 
			"	MOVE D0, (A0)\n" + 
			"	MOVE D(PC), D0\n" + 
			"	BSR WRITE\n" + 
			"	DC WARMST\n" + 
			"	END MAIN\n";
	
	
	final String errCompiledWithIncorrectTypes =
			"\n" + 
			"*** ERROR: Fehlerhafte Deklaration: INT A.	look=','\n";
	
	@Test
	public void testWithIncorrectTypes() throws IOException {
		VariablenMitTyp vmt = new VariablenMitTyp("test/Test_mitFalschenTypen");
		vmt.compile();
		assertEquals( outCompiledWithIncorrectTypes, outStream.toString());
		assertEquals(errCompiledWithIncorrectTypes, errStream.toString());
	}
}
