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

	final String outCompiledCorrectAssignmentType =
			"[PROGRAM.input]\n" + 
			"PROGRAM;\n" + 
			"VAR BYTE A, BYTE B, BYTE C;\n" + 
			"VAR LONG X, LONG Y, LONG Z;\n" + 
			"BEGIN\n" + 
			"  B=5;\n" + 
			"  C=2;\n" + 
			"  A=B+C;\n" + 
			"  Y=3;\n" + 
			"  Z=4;\n" + 
			"  X=Y*Z;\n" + 
			"  WRITE(A);\n" + 
			"  WRITE(X);\n" + 
			"END.\n" + 
			"\n" + 
			"[PROGRAM.output]\n" + 
			"\n" + 
			"	WARMST	EQU $A01E\n" + 
			"A:	DC.B 0\n" + 
			"B:	DC.B 0\n" + 
			"C:	DC.B 0\n" + 
			"X:	DC.L 0\n" + 
			"Y:	DC.L 0\n" + 
			"Z:	DC.L 0\n" + 
			"MAIN:\n" + 
			"	MOVE #5, D0\n" + 
			"	LEA B(PC), A0\n" + 
			"	MOVE D0, (A0)\n" + 
			"	MOVE #2, D0\n" + 
			"	LEA C(PC), A0\n" + 
			"	MOVE D0, (A0)\n" + 
			"	MOVE B(PC), D0\n" + 
			"	MOVE D0, -(SP) \n" + 
			"	MOVE C(PC), D0\n" + 
			"	ADD (SP)+, D0\n" + 
			"	LEA A(PC), A0\n" + 
			"	MOVE D0, (A0)\n" + 
			"	MOVE #3, D0\n" + 
			"	LEA Y(PC), A0\n" + 
			"	MOVE D0, (A0)\n" + 
			"	MOVE #4, D0\n" + 
			"	LEA Z(PC), A0\n" + 
			"	MOVE D0, (A0)\n" + 
			"	MOVE Y(PC), D0\n" + 
			"	MOVE D0, -(SP) \n" + 
			"	MOVE Z(PC), D0\n" + 
			"	MULS (SP)+, D0\n" + 
			"	LEA X(PC), A0\n" + 
			"	MOVE D0, (A0)\n" + 
			"	MOVE A(PC), D0\n" + 
			"	BSR WRITE\n" + 
			"	MOVE X(PC), D0\n" + 
			"	BSR WRITE\n" + 
			"	DC WARMST\n" + 
			"	END MAIN\n";

	final String errCompiledCorrectAssignmentType = "";
	
	@Test
	public void testTypeSafeAssignment() throws IOException {
		VariablenMitTyp vmt = new VariablenMitTyp("test/Test_assignmentMitKorrektenTypen");
		vmt.compile();
		assertEquals(outCompiledCorrectAssignmentType, outStream.toString());
		assertEquals(errCompiledCorrectAssignmentType, errStream.toString());
	}

	final String errCompiledIncorrectAssignmentType = "\n" + 
			"*** ERROR: variable type doesn't match expected type of assignment.	look=';'\n";


	@Test
	public void testTypeUnsafeAssignment() throws IOException {
		VariablenMitTyp vmt = new VariablenMitTyp("test/Test_assignmentMitInkorrektenTypen");
		try {
			vmt.compile();
			fail("exception was expected, but not thrown");
		} catch (RuntimeException e) {
			assertEquals(errCompiledIncorrectAssignmentType, errStream.toString());
		}
	}
}
