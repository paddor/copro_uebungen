package compB_04_Uebung_05;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.BufferedReader;

/**
 * Syntactic sugar: comments with single character delimiters {...}
 * constant declarations
 * <block> ::= <statement> ( ';' <statement> )*
 * <statement> ::= <assignment> | <if> | <while> ... | null 
 */

public class VariablenMitTyp {
	/**
	 * system wide constant declarations
	 */
	private static final char TAB = '\t';
	private static final char CR = '\r';
	private static final char LF = '\n';
	private static final char BLANK = ' ';

	/**
	 * system variables
	 */
	
	/**
	 * input file (Reader/Stream)
	 */
	Reader in; 
	/**
	 * program file name
	 */
	String fName = ""; 

	/**
	 * look ahead symbol
	 */
	private char look; 
	
	private char token;
	private String lookValue;
	/**
	 * label count
	 */
	private int lCount; 
	
	/**
	 * Symbol Table mini:
	 * part 1: 	reserved words (KWlist: key word list)
	 * 			coded in 1-character form in KWCode
	 * part 2: 	identifiers (entries:=<symbol,type>)
	 * 			see:addEntry
	 */
	private int nEntry; // nr of entries
	private final int MAXENTRY = 100; // ..or less
	String[] symTab;	// var names
	char[] typeTab;		// type of symbol: 'v' for variable
	
	/**
	 * definition of keywords and token types
	 */
	private final String[] KWlist = { "IF", "ELSE", "ENDIF", "WHILE", "ENDWHILE", 
									"READ", "WRITE", "VAR", "END", "BYTE", "WIDE", "LONG"};
	/**
	 * coding of keywords: i=IF,l=ELSE,w=WHILE,...
	 * x = identifier (see below: getName)
	 */
	private final String KWcode = "xileweRWvettt";
	int nkw = KWlist.length;
	int nkw1 = nkw+1;
	
	/** 
	 * initialize - constructor
	 */
	private VariablenMitTyp() {
		symTab = new String[MAXENTRY];
		typeTab= new char[MAXENTRY];
		for (int i=0; i<MAXENTRY; i++) {
			symTab[i] = "";
			typeTab[i]=' ';
		}
		lCount = 0;
		nEntry = 0;
	}

	public VariablenMitTyp(String fn) throws IOException {
		this();
		fName = fn;
		File file = new File(fName);
		in = new FileReader(file);
		BufferedReader br = new BufferedReader(in);
		String line;
		System.out.println("[PROGRAM.input]");
		while (	(line = br.readLine()) != null ) {
			System.out.println(line);
		} 
		br.close();
		System.out.println();
		System.out.println("[PROGRAM.output]");
		System.out.println();
		in = new FileReader(file);
		getChar();
	}

	public VariablenMitTyp(InputStream is) {
		this();
		in = new InputStreamReader(is);
		getChar();
	}

	/**
	 * read a new character from input stream
	 */
	private void getChar() {
		try {
			look = (char) in.read();
		} catch (IOException e) {
			System.err.println("[getChar]IOException: File " + fName);
		}
	}
	
	/**
	 * report an error
	 */
	private void error(String error) {
		System.err.println();
		System.err.println("*** ERROR: " + error + '.' + "\tlook='" + look+"'" );
	}

	/**
	 * report error and halt
	 */
	private void abort(String reason) throws RuntimeException {
		error(reason);
		throw new RuntimeException();
	}
	
	/**
	 * report what was expected
	 */
	private void expected(String msg) {
		abort(msg + " expected");
	}
	
	/**
	 * report an undefined identifier
	 */
	private void undefined(String n) {
		abort("Undefined identifier "+ n);
	}
	
	/**
	 * report a duplicate identifier
	 */
	private void duplicate(String id) {
		abort( "Duplicate Identifier " + id);
	}

	/**
	 * check to make sure the current token is an identifier
	 */
	private void checkIdent() {
		if ( token != 'x' ) expected("Identifier");
	}

	/**
	 * recognize an alpha character
	 */
	private boolean isAlpha(char c) {
		return Character.isLetter(c);
	}

	/**
	 * recognize a decimal digit
	 */
	private boolean isDigit(char c) {
		return Character.isDigit(c);
	}

	/**
	 * recognize alphanumeric characters
	 */
	private boolean isAlphaNum(char c) {
		return (isAlpha(c) || isDigit(c));
	}	
	
	/**
	 * recognize an add operator
	 */
	private boolean isAddOp(char c) {
		return isIn(c, '+', '-');
	}
	
	/**
	 * recognize a multiplication operator
	 */
	private boolean isMulOp(char c) {
		return isIn(c, '*', '/');
	}

	/** 
	 * recognize a Boolean or operator
	 */
	private boolean isOrOp(char c) {
		return isIn(c, '|', '~'); // OR, XOR
	}
	
	/**
	 * recognize a relop
	 */
	private boolean isRelOp(char c) {
		return isIn(c, '=', '#', '<', '>');
	}

	/**
	 * recognize white space
	 */
	private boolean isWhite(char c) {
		// CR LF are in fin()
		return isIn(c, TAB, BLANK, CR, LF, '{');
	}
	
	/**
	 * skip over leading white spaces
	 */
	private void skipWhite() {
		while (isWhite(look)) {
			if ( look == '{') skipComment();
			else getChar();
		}
	}
	
	/**
	 * skip a comment field 
	 */
	private void skipComment() {
		while ( look == '}' ) {
			getChar();
			if ( look == '{' ) skipComment();
		}
		getChar();
	}

	/**
	 * keywords table lookup: -1 means 'not found'
	 */
	private int lookup(String keyW) {
		for (int i = 0; i < KWlist.length;i++) {
			if (keyW.equals(KWlist[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * locate a symbol in the symbol table
	 * @param sym
	 * @return index of entry sym in symTab; -1 if not present
	 */
	@SuppressWarnings("unused")
	private int locate(String sym) {
		for (int i=0; i<symTab.length; i++) {
			if (symTab[i].equals(sym)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * look for symbol in table
	 */
	private boolean inTable(String sym) {
		for (String symb: symTab) {
			if (symb.equals(sym)) return true;
		}
		return  false;
	}
	
	/**
	 * check to see if an identifier is in the symbol table
	 * report an error if it is not
	 */
	private void checkTable(String sym) {
		if ( ! inTable(sym) )  undefined(sym);
	}
	
	/**
	 * check the symbol table for a duplicate identifier
	 * report an error if it is not
	 */
	private void checkDup(String sym) {
		if ( inTable(sym) ) duplicate(sym); 
	}
	
	/**
	 * add a new Entry to Symbol Table
	 */
	private void addEntry(String sym, char typ) {
		checkDup(sym);
		if ( nEntry == MAXENTRY ) abort("Symbol Table full!");
		nEntry++;
		symTab[nEntry]=sym;
		typeTab[nEntry]=typ;
	}
	
	/**
	 * recognize / get an identifier: 
	 * <ident> ::= <letter> [ <letter> |<digit> ]*
	 */
	private void getName() {
		skipWhite();
		if (!isAlpha(look)) {
			expected("[getName]Name");
		}
		lookValue = "";
		token = 'x';
		do {
			lookValue = lookValue + upCase(look);
			getChar();
		} while (isAlphaNum(look));
	}
	
	/**
	 * get a number
	 */
	private void getNum() {
		skipWhite();
		if ( ! isDigit(look) ) {
			expected("Number");
		} 
		token = '#';
		lookValue = "";
		do {		
			lookValue = lookValue + look;
			getChar();
		} while ( isDigit(look) );
	}
	
	/**
	 * get an operator
	 */
	private void getOp() {
		skipWhite();
		token = look;
		lookValue = Character.toString(look);
		getChar();
	}
	
	/**
	 * get next input token
	 */
	private void next() {
		skipWhite();
		if ( isAlpha(look) ) {
			getName();
		}
		else {
			if ( isDigit(look) ) {
				getNum();
			}
			else getOp();
		}
	}

	/**
	 * get an identifier and scan it for keywords
	 */
	private void scan() {
		if ( token=='x' ) { 
			token = KWcode.charAt(lookup(lookValue)+1);
		}
	}
	
	/**
	 * match a specific input string
	 */
	private void matchString(String x) {
		if (!lookValue.equals(x)) {
			expected("[matchString]lookValue= " + lookValue + "\t" + x + " ");
		}
		next();
	}	

	/**
	 * match a semicolon
	 */
	private void semi() {
		if ( token == ';' ) next();
	}

	/**
	 * output a string with tab
	 */
	private void emit(String s) {
		System.out.print(TAB + s);
	}

	/**
	 * output a string with tab and CRLF
	 */
	private void emitLn(String s) {
		emit(s);
		System.out.println();
	}
	
	/**
	 * generate a new (unique) label
	 */
	private String newLabel() {
		String s;
		s = "L" + lCount;
		lCount++;
		return s;
	}
	
	/**
	 * post a label to output
	 */
	private void postLabel(String label) {
		System.out.println(label+':');
	}

	/**
	 * clear primary (accumulator) register
	 */
	private void clear() {
		emitLn("CLR D0");
	}
	
	/**
	 * negate the primary register
	 */
	@SuppressWarnings("unused")
	private void negate() {
		emitLn("NEG D0");
	}
	
	/**
	 * complement the primary register
	 */
	private void notIt() {
		emitLn("NOT D0");
	}
	
	/**
	 * load a constant value to primary register
	 */
	private void loadConst(String n) {
		emit("MOVE #");
		System.out.println(n+ ", D0");
	}
	
	/**
	 * load a variable to primary register 
	 */
	private void loadVar(String name) {
		if ( ! inTable(name) ) {
			undefined(name);
		}
		emitLn("MOVE "+name + "(PC), D0");
	}
	
	/**
	 * push primary onto stack
	 */
	private void push() {
		emitLn("MOVE D0, -(SP) ");
	}
	
	/**
	 * add top of stack to primary
	 */
	private void popAdd() {
		emitLn("ADD (SP)+, D0");
	}
	
	/**
	 * subtract primary from top of stack
	 */
	private void popSub() {
		emitLn("SUB (SP)+, D0");
		emitLn("NEG D0");
	}
	
	/**
	 * multiply top of stack by primary
	 */
	private void popMul() {
		emitLn("MULS (SP)+, D0");
	}
	
	/**
	 * divide top of stack by primary
	 */
	private void popDiv() {
		emitLn("MOVE (SP)+, D7");
		emitLn("EXT.L D7");
		emitLn("DIVS D0, D7");
		emitLn("MOVE D7, D0");
	}
	
	/**
	 * AND top of stack with primary
	 */
	private void popAnd() {
		emitLn("AND (SP)+, D0");
	}
	
	/**
	 * OR top of stack with primary
	 */
	private void popOr() {
		emitLn("OR (SP)+, D0");
	}
	
	/**
	 * XOR top of stack with primary
	 */
	private void popXor() {
		emitLn("EOR (SP)+, D0");
	}
	
	/**
	 * compare top of stack with primary
	 */
	private void popCompare() {
		emitLn("CMP (SP)+, D0");
	}
	
	/**
	 * set D0 if compare was =
	 */
	private void setEqual() {
		emitLn("SEQ D0");
		emitLn("EXT D0");
	}
	
	/**
	 * set D0 if compare was !=
	 */
	private void setNEqual() {
		emitLn("SNE D0");
		emitLn("EXT D0");
	}
	
	/**
	 * set D0 if compare was >
	 */
	private void setGreater() {
		emitLn("SLT D0");
		emitLn("EXT D0");
	}
	
	/**
	 * set D0 if compare was <
	 */
	private void setLess() {
		emitLn("SGT D0");
		emitLn("EXT D0");
	}
	
	/**
	 * set D0 if compare was <=
	 */
	private void setLessOrEqual() {
		emitLn("SGE D0");
		emitLn("EXT D0");
	}

	/**
	 * set D0 if compare was >=
	 */
	private void setGreaterOrEqual() {
		emitLn("SLE D0");
		emitLn("EXT D0");
	}
	
	/**
	 * store primary to variable
	 */
	private void store(String name) {
		emitLn("LEA " + name + "(PC), A0");
		emitLn("MOVE D0, (A0)");
	}	
	
	/**
	 * branch unconditional
	 */
	private void branch(String label) {
		emitLn("BRA "+ label);
	}
	
	/**
	 * branch false
	 */
	private void branchFalse(String label) {
		emitLn("TST D0");
		emitLn("BEQ "+ label);
	}

	/**
	 * read variable to primary register
	 */
	private void readIt(String name) {
		emitLn("BSR READ");
		store( name );
	}

	/**
	 * write variable from primary register
	 */
	private void writeIt() {
		emitLn("BSR WRITE");
	}
	
	/**
	 * write header info required by assembler
	 */
	private void header() {
		emitLn("WARMST"+TAB+"EQU $A01E");
	}
	
	/**
	 * write the prolog
	 */
	private void prolog() {
		postLabel("MAIN");
	}

	/**
	 * write the epilog
	 */
	private void epilog() {
		emitLn("DC WARMST");
		emitLn("END MAIN");
	}
	
	/**
	 * allocate storage for a static variable
	 * @param type 
	 */
	private void allocate(String name, String value, char type) {
		System.out.println(name + ":"+TAB+"DC." + type + " " + value);
	}
	
	/**
	 * parse and translate a math factor
	 */
	private void factor() {
		if ( token == '(' ) {
			next();
			boolExpression();
			matchString(")");
		} else {
			if ( token == 'x' ) {
				loadVar(lookValue);
			} else {
				if ( token == '#' ) {
					loadConst(lookValue);					
				} else {
					expected( "Math Factor" );
				}
			}
			next();
		}
	}

	/**
	 * recognize and translate e multiplication
	 */
	private void multiply() {
		next();
		factor();
		popMul();
	}

	/**
	 * recognize and translate a divide
	 */
	private void divide() {
		next();
		factor();
		popDiv();
	}

	/**
	 * parse and translate a math term
	 */
	private void term() {
		factor();
		while ( isMulOp(token) ) {
			push();
			switch (token) {
			case '*':
				multiply();
				break;
			case '/':
				divide();
				break;

			default:
				break;
			}
		}
	}
	
	/**
	 * recognize and translate an add
	 */
	private void add() {
		next();
		term();
		popAdd();
	}
	
	/**
	 * recognize and translate a subtract
	 */
	private void subtract() {
		next();
		term();
		popSub();
	}

	/**
	 * parse and translate an expression
	 */
	private void expression() {
		if( isAddOp(token) ) {
			clear();
		} else {
			term();
		}
		while ( isAddOp(token) ) {
			push();
			switch (token) {
			case '+':
				add();
				break;
			case '-':
				subtract();
				break;

			default:
				break;
			}
		}
	}	
	
	/**
	 * get another expression and compare
	 */
	private void compareExpression() {
		expression();
		popCompare();
	}
	
	/**
	 * get the next expression and compare
	 */
	private void nextExpression() {
		next();
		compareExpression();
	}

	/**
	 * recognize and translate a relational "EQUALS"
	 */
	private void equal() {
		nextExpression();
		setEqual();
	}

	/**
	 * recognize and translate a relational "EQUALS"
	 */
	private void lessOrEqual() {
		nextExpression();
		setLessOrEqual();
	}
	
	/**
	 * recognize and translate a relational "NOT EQUALS"
	 * old: #; new: <>
	 */
	private void notEqual() {
		nextExpression();
		setNEqual();
	}
	
	/**
	 * recognize and translate a relational "LESS THAN"
	 */
	private void less() {
		next();
		switch (token) {
		case '=':
			lessOrEqual();
			break;
		case '>':
			notEqual();
			break;

		default:
			compareExpression();
			setLess();			
			break;
		}
	}
	
	/**
	 * recognize and translate a relational "GREATER THAN"
	 */
	private void greater() {
		next();
		if ( token == '=' ) {
			nextExpression();
			setGreaterOrEqual();
		} else {
			compareExpression();
			setGreater();
		}
	}
	
	/**
	 * parse and translate a relation
	 */
	private void relation() {
		expression();
		if (isRelOp(token)) {
			push();
			switch (token) {
			case '=':
				equal();
				break;
			case '<':
				less();
				break;
			case '>':
				greater();
				break;

			default:
				break;
			}
		}
	}

	/**
	 * parse and translate a Boolean factor with leading NOT
	 */
	private void notFactor() {
		if (token == '!') {
			next();
			relation();
			notIt();
		} else {
			relation();
		}
	}
	
	/**
	 * parse and translate a Boolean term
	 */
	private void boolTerm() {
		notFactor();
		while ( token == '&') {
			push();
			next();
			notFactor();
			popAnd();
		}
	}
	
	/**
	 * recognize and translate a Boolean OR
	 */
	private void boolOr() {
		next();
		boolTerm();
		popOr();
	}
	
	/**
	 * recognize and translate an exclusive or - XOR
	 */
	private void boolXor() {
		next();
		boolTerm();
		popXor();
	}
	
	/**
	 * parse and translate a Boolean expression
	 */
	private void boolExpression() {
		boolTerm();
		while ( isOrOp(token)) {
			push();
			switch (token) {
			case '|':
				boolOr();
				break;
			case '~':
				boolXor();
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * parse and translate an assignment statement
	 */
	private void assignment() {
		String name;
		// 
		checkTable(lookValue);
		name = lookValue;
		next();
		matchString("=");
		boolExpression();
		store(name);
	}
	
	/**
	 * recognize and translate an IF construct
	 */
	private void doIf() {
		String label1, label2;
		//
		next();
		boolExpression();
		label1 = newLabel();
		label2 = label1;
		branchFalse(label1);
		block();
		if (token == 'l') {// l = ELSE
			next();
			label2 = newLabel();
			branch(label2);
			postLabel(label1);
			block();
		}
		postLabel(label2);
		matchString("ENDIF");
	}

	/**
	 * parse & and translate a WHILE statement
	 * WHILE <condition> <block> ENDWHILE
	 */
	private void doWhile() {
		String label1, label2;
		//
		next();
		label1 = newLabel();
		label2 = newLabel();
		postLabel(label1);
		boolExpression();
		branchFalse(label2);
		block();
		matchString("ENDWHILE");
		branch(label1);
		postLabel(label2);
	}	
	
	/**
	 * read a single variable
	 */
	private void readVar() {
		checkIdent();
		checkTable(lookValue);
		readIt(lookValue);
		next();
	}

	private void doRead() {
		next();
		matchString("(");
		readVar();
		while ( token == ',' ){
			next();
			readVar();
		}
		matchString(")");
	}
	
	private void doWrite() {
		next();
		matchString("(");
		expression();
		writeIt();
		while ( token == ',' ) {
			next();
			expression();
			writeIt();
		}
		matchString(")");
	}
	
	/**
	 * parse and translate a single statement
	 */
	private void statement() {
		scan();
		switch (token) {
		case 'i':
			doIf();
			break;
		case 'w':
			doWhile();
			break;
		case 'R':
			doRead();
			break;
		case 'W':
			doWrite();
			break;
		case 'x':
			assignment();
			break;

		default:
			break;
		}
	}
	
	/**
	 * parse and translate a block of statements
	 */
	private void block() {
		statement();
		while ( token == ';')  {
			next();
			statement();
		}
	}
	
	/**
	 * allocate storage for a variable
	 */
	private void alloc() {
		next();
		scan();
		if (token == 't') {
			char type = verifyType(lookValue);
			next();
			if (token != 'x') expected("Variable Name");
	//		checkDup(lookValue);
			addEntry(lookValue, type);
			allocate(lookValue, "0", type);
		} else {
			String t = lookValue;
			next();
			String n = lookValue;
			error("Fehlerhafte Deklaration: " + t + " " + n);
		}

		next();
	}
		
	private char verifyType(String lookValue2) {
		char type = lookValue2.charAt(0);
		if (!isVarType(type)) {
			abort("invalid variable type");
		}
		return type;
	}

	private boolean isVarType(char type) {
		return isIn(type, 'B', 'W', 'L');
	}

	/**
	 * parse and translate global declarations
	 */
	private void topDecls() {
			scan();
		while (token == 'v') {
			alloc();
			while ( token == ',') {
				alloc();
			}
			semi();
			scan();
		}

//		scan();
//		do {
//			alloc();
//			while ( token == ',') {
//				alloc();
//			}
//			semi();
//			scan();
//		} while (token == 'v');
//		scan();
	}

	/**
	 * Helper Methods
	 */
	
	/**
	 * switch character to upper case
	 */
	private char upCase(char c) {
		return Character.toUpperCase(c);
	}

	/**
	 * check whether look is one of the following characters this simulates a
	 * simple and short set data type
	 */
	private boolean isIn(char look, char... c) {
		// var args: first is look then set of values
		for (int i : c) {
			if (i == look)
				return true;
		}
		return false;
	}
	
	public void compile() {
		next();
		matchString("PROGRAM");
		semi();
		header();
		topDecls();
		matchString("BEGIN");
		prolog();
		block();
		matchString("END");
		epilog();
	}
	
	public static void main(String[] args) throws IOException {
		System.err.println("Enter multiple character keywords, single character variables based program");
//		VariablenMitTyp vmt = new VariablenMitTyp("Test_VMT");
//		VariablenMitTyp vmt = new VariablenMitTyp("Test_ohneTypen");
//		VariablenMitTyp vmt = new VariablenMitTyp("Test_mitTypen");
		VariablenMitTyp vmt = new VariablenMitTyp("Test_mitFalschenTypen");
//		VariablenMitTyp vnt = new VariablenMitTyp(System.in);

		try {
			vmt.compile();
		} catch(RuntimeException e) {
			System.exit(99);
		}
	}
}

/*

Enter multiple character keywords, single character variables based program
[PROGRAM.input]
PROGRAM;
VAR A,B;
BEGIN
  B=10;
  A=5*B;
  WRITE(A);
END.

[PROGRAM.output]

	WARMST	EQU $A01E
A:	DC 0
B:	DC 0
MAIN:
	MOVE #10, D0
	LEA B(PC), A0
	MOVE D0, (A0)
	MOVE #5, D0
	MOVE D0, -(SP) 
	MOVE B(PC), D0
	MULS (SP)+, D0
	LEA A(PC), A0
	MOVE D0, (A0)
	MOVE A(PC), D0
	BSR WRITE
	DC WARMST
	END MAIN

	
*/