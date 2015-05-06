package cop5555sp15;

import java.util.ArrayList;
import java.util.List;

import cop5555sp15.ast.*;
import static cop5555sp15.TokenStream.Kind.AND;
import static cop5555sp15.TokenStream.Kind.ARROW;
import static cop5555sp15.TokenStream.Kind.ASSIGN;
import static cop5555sp15.TokenStream.Kind.AT;
import static cop5555sp15.TokenStream.Kind.BAR;
import static cop5555sp15.TokenStream.Kind.BL_FALSE;
import static cop5555sp15.TokenStream.Kind.BL_TRUE;
import static cop5555sp15.TokenStream.Kind.COLON;
import static cop5555sp15.TokenStream.Kind.COMMA;
import static cop5555sp15.TokenStream.Kind.DIV;
import static cop5555sp15.TokenStream.Kind.DOT;
import static cop5555sp15.TokenStream.Kind.EOF;
import static cop5555sp15.TokenStream.Kind.EQUAL;
import static cop5555sp15.TokenStream.Kind.GE;
import static cop5555sp15.TokenStream.Kind.GT;
import static cop5555sp15.TokenStream.Kind.IDENT;
import static cop5555sp15.TokenStream.Kind.INT_LIT;
import static cop5555sp15.TokenStream.Kind.KW_BOOLEAN;
import static cop5555sp15.TokenStream.Kind.KW_CLASS;
import static cop5555sp15.TokenStream.Kind.KW_DEF;
import static cop5555sp15.TokenStream.Kind.KW_ELSE;
import static cop5555sp15.TokenStream.Kind.KW_IF;
import static cop5555sp15.TokenStream.Kind.KW_IMPORT;
import static cop5555sp15.TokenStream.Kind.KW_INT;
import static cop5555sp15.TokenStream.Kind.KW_PRINT;
import static cop5555sp15.TokenStream.Kind.KW_RETURN;
import static cop5555sp15.TokenStream.Kind.KW_STRING;
import static cop5555sp15.TokenStream.Kind.KW_WHILE;
import static cop5555sp15.TokenStream.Kind.LCURLY;
import static cop5555sp15.TokenStream.Kind.LE;
import static cop5555sp15.TokenStream.Kind.LPAREN;
import static cop5555sp15.TokenStream.Kind.LSHIFT;
import static cop5555sp15.TokenStream.Kind.LSQUARE;
import static cop5555sp15.TokenStream.Kind.LT;
import static cop5555sp15.TokenStream.Kind.MINUS;
import static cop5555sp15.TokenStream.Kind.MOD;
import static cop5555sp15.TokenStream.Kind.NOT;
import static cop5555sp15.TokenStream.Kind.NOTEQUAL;
import static cop5555sp15.TokenStream.Kind.PLUS;
import static cop5555sp15.TokenStream.Kind.RANGE;
import static cop5555sp15.TokenStream.Kind.RCURLY;
import static cop5555sp15.TokenStream.Kind.RPAREN;
import static cop5555sp15.TokenStream.Kind.RSHIFT;
import static cop5555sp15.TokenStream.Kind.RSQUARE;
import static cop5555sp15.TokenStream.Kind.SEMICOLON;
import static cop5555sp15.TokenStream.Kind.STRING_LIT;
import static cop5555sp15.TokenStream.Kind.TIMES;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;
		Kind[] expected;
		String msg;

		SyntaxException(Token t, Kind expected) {
			this.t = t;
			msg = "";
			this.expected = new Kind[1];
			this.expected[0] = expected;

		}

		public SyntaxException(Token t, String msg) {
			this.t = t;
			this.msg = msg;
		}

		public SyntaxException(Token t, Kind[] expected) {
			this.t = t;
			msg = "";
			this.expected = expected;
		}

		public String getMessage() {
			StringBuilder sb = new StringBuilder();
			sb.append(" error at token ").append(t.toString()).append(" ")
					.append(msg);
			sb.append(". Expected: ");
			for (Kind kind : expected) {
				sb.append(kind).append(" ");
			}
			return sb.toString();
		}
	}

	TokenStream tokens;
	Token t;

	Parser(TokenStream tokens) {
		this.tokens = tokens;
		t = tokens.nextToken();
		
	}

	private Kind match(Kind kind) throws SyntaxException {
		if (isKind(kind)) {
			consume();
			return kind;
		}
		throw new SyntaxException(t, kind);
	}

	private Kind match(Kind... kinds) throws SyntaxException {
		Kind kind = t.kind;
		if (isKind(kinds)) {
			consume();
			return kind;
		}
		StringBuilder sb = new StringBuilder();
		for (Kind kind1 : kinds) {
			sb.append(kind1).append(kind1).append(" ");
		}
		throw new SyntaxException(t, "expected one of " + sb.toString());
	}

	private boolean isKind(Kind kind) {
		return (t.kind == kind);
	}

	private void consume() {
		if (t.kind != EOF)
			t = tokens.nextToken();
	}

	private boolean isKind(Kind... kinds) {
		for (Kind kind : kinds) {
			if (t.kind == kind)
				return true;
		}
		return false;
	}
	

	//This is a convenient way to represent fixed sets of
	//token kinds.  You can pass these to isKind.
	static final Kind[] REL_OPS = { BAR, AND, EQUAL, NOTEQUAL, LT, GT, LE, GE };
	static final Kind[] WEAK_OPS = { PLUS, MINUS };
	static final Kind[] STRONG_OPS = { TIMES, DIV };
	static final Kind[] VERY_STRONG_OPS = { LSHIFT, RSHIFT };


	
	private Kind[] FIRSTStatement() throws SyntaxException {
		Kind[] arr = {IDENT, KW_PRINT, KW_WHILE, KW_IF, MOD, KW_RETURN, Kind.NL_NULL };
		return arr;
	}
	
	
	private Declaration Declaration() throws SyntaxException {
		match(KW_DEF);
	    if(tokens.nextToNextToken().kind==Kind.ASSIGN){
	    	ClosureDec cd = ClosureDec();
	    	return cd;
	    }
	    else{
	    	VarDec vd = VarDec();
	    	return vd;
	    }
	   
	}
	
	private Statement Statement() throws SyntaxException {
		Token firsttoken =t;
		LValue lvalue = null;
		Expression exp = null;
		Block block = null;		
		if(isKind(IDENT)) {
			lvalue = LValue();
			match(ASSIGN);
			exp = Expression();
			return new AssignmentStatement(t, lvalue, exp);
		}
		else if(isKind(KW_PRINT)){
			match(KW_PRINT);
			exp = Expression();
			return new PrintStatement(t, exp);
		}
		
		else if(isKind(KW_IF)){
		
		 //Token t1 = t;
		 
			match(KW_IF);
			match(LPAREN);
		    exp = Expression();
			
			match(RPAREN);
			Block ifBlock = Block();
			
			if(isKind(KW_ELSE)){
				match(KW_ELSE);
				Block elseBlock = Block();
			//	match(SEMICOLON);  //New Addition
				 return new IfElseStatement(t, exp, ifBlock, elseBlock);
			}
			else{
			//match(SEMICOLON);  //New Addition
			return new IfStatement(t, exp, ifBlock);
		}
		
		}
		
		else if(isKind(MOD)){
			match(MOD);
			exp = Expression();
			return new ExpressionStatement(t, exp);
		}
		else if(isKind(KW_RETURN)){
			match(KW_RETURN);
			exp = Expression();
			return new ReturnStatement(t, exp);
		}
		
		
		else if(isKind(KW_WHILE)){
		//	Token t1 = t;
			boolean range = false;
			boolean star = false;
			RangeExpression rexp = null;
			Expression exp2 = null;
			match(KW_WHILE);
			
			if(isKind(TIMES)){  //WhileSTAR
				star = true;
			match(TIMES);
			match(LPAREN);
			//Token t2 = t;
			exp = Expression();
			
			if(isKind(RANGE)){  //RangeExpression
			range = true;
			match(RANGE);
			exp2 = Expression();
			rexp = new RangeExpression(t, exp, exp2);
		    
			}
			}
			
			else{		//While
				match(LPAREN);
				exp = Expression();
			}
			match(RPAREN);
			block = Block();
			
			//match(SEMICOLON);     //New Addition After Seeing The Test Case Patterns
			
			if(range) return new WhileRangeStatement(t, rexp, block);
			else if (star && !range) return new WhileStarStatement(t, exp, block);
			else 
				return new WhileStatement(t, exp, block);
					
		}
		else if(isKind(Kind.NL_NULL)){
			match(Kind.NL_NULL);
			return null;
			
		}
		else{
			
			return null;
		
			
		}
	
	//	return null;
		 
	}
	
	
	private VarDec VarDec() throws SyntaxException {
		Type type = null;
		Token identToken = t;
		match(IDENT);
		if(isKind(Kind.NL_NULL)){
			match(Kind.NL_NULL);
		}
		else if (isKind(COLON)){
			match(COLON);
			type = Type();
		}
		return new VarDec(t, identToken, type);
	}
	
	private ClosureDec ClosureDec() throws SyntaxException {
		Token identToken = t;
		match(IDENT);
		match(ASSIGN);
		Closure closure = Closure();
		return new ClosureDec(t, identToken, closure);
	}	
	
	private Closure Closure() throws SyntaxException {
		match(LCURLY);
		List<VarDec> formalArgList = FormalArgList();
		match(ARROW);
		
		List<Statement> statementList = new ArrayList<Statement>();
		
		while(isKind(FIRSTStatement()) || isKind(SEMICOLON)){  //isKind(SEMICOLON) is added because Statement can be empty in which
																// case SEMICOLON will be encountered as it belongs to FOLLOW(Statement)
			Statement s = Statement();
			statementList.add(s);
			match(SEMICOLON);
		}
		match(RCURLY);
		
		return new Closure(t, formalArgList, statementList);
	}
	
	
	private Type Type() throws SyntaxException {
		//Token tokenType = t;
		if(isKind(KW_INT) || isKind(KW_BOOLEAN) || isKind(KW_STRING)){
			SimpleType simpleType = SimpleType();
			return simpleType;
		}
		else if(tokens.nextToNextToken().kind == AT){
			KeyValueType();
			return new KeyValueType(t, keyType, valueType);
		}
		else{
			ListType();
			return new ListType(t, lstType);
		}
		
	}

	private SimpleType SimpleType() throws SyntaxException {
		Token tp = t;
		if (isKind(KW_INT))	match(KW_INT);
		else if(isKind(KW_BOOLEAN)) match(KW_BOOLEAN);
		else match(KW_STRING);
		return new SimpleType(t, tp);
	}

	Type valueType;
	SimpleType keyType;
	
	private KeyValueType KeyValueType() throws SyntaxException {
		match(AT);
		match(AT);
		match(LSQUARE);
		keyType = SimpleType();
		match(COLON);
		valueType = Type();
		match(RSQUARE);
		
		return new KeyValueType(t, keyType, valueType);
	}

	Type lstType;
	private ListType ListType() throws SyntaxException {
		match(AT);
		match(LSQUARE);
		lstType = Type();
		match(RSQUARE);
		
		return new ListType(t, lstType);
	}

	private List<VarDec> FormalArgList() throws SyntaxException {
		formalArgList = new ArrayList<VarDec>();
		VarDec cd;
		if(isKind(Kind.NL_NULL)){
		match(Kind.NL_NULL);
	}
	
	else if(isKind(IDENT)){
		cd = VarDec();
		formalArgList.add(cd);
		while(isKind(COMMA)){
			match(COMMA);
			cd = VarDec();
			formalArgList.add(cd);
		}
	}
	else{
		
	}
	
	return formalArgList;
	}

	private LValue LValue() throws SyntaxException {
		Token identToken = t;
		match(IDENT);
		if(isKind(LSQUARE)){
			match(LSQUARE);
			Expression expression = Expression();
			match(RSQUARE);
			return  new ExpressionLValue(t, identToken, expression);
		}
		
		return new IdentLValue(t, identToken);
	}
	
	
	List<Expression> closureEvalExpressionList;
	
	private ClosureEvalExpression ClosureEvalExpression() throws SyntaxException {
		Token identToken = t;
			match(IDENT);
			match(LPAREN);
			closureEvalExpressionList = Expressionlist();
			match(RPAREN);
			
			return new ClosureEvalExpression(t, identToken, closureEvalExpressionList);
	}
	
	
	List<Expression> expList;
	private List<Expression> Expressionlist() throws SyntaxException {
		expList = new ArrayList<Expression>();
		Expression exp;
		if(isKind(Kind.NL_NULL)) match(Kind.NL_NULL);
		else if(isKind(FIRSTFactor())){
			exp = Expression();
			expList.add(exp);
			while(isKind(COMMA)){
				match(COMMA);
				exp = Expression();
				expList.add(exp);
			}
		}
		else{
			
		}
		
		return expList;
	}
	
	
	
	
	List<Expression> listList;
	
	private ListExpression List() throws SyntaxException {
		listList = new ArrayList<Expression>();
		match(AT);
		match(LSQUARE);
		listList = Expressionlist();
		match(RSQUARE);
		
		return new ListExpression(t, listList);
	}
	
	private Expression Expression() throws SyntaxException {
		Expression e0 = null;
        Expression e1 = null;
		e0 = Term();
		while (isKind(REL_OPS)) 
        {   Token op = t;
        	   consume(); 
            e1 = Term(); 
		    e0 = new BinaryExpression(t, e0,op,e1);
		}
		
         return e0;
	
		
	}
	
	private Expression Term() throws SyntaxException {
		Expression e0 = null;
        Expression e1 = null;
		e0 = Elem();
		while (isKind(WEAK_OPS)) 
        {   Token op = t;
        	   consume(); 
            e1 = Elem(); 
		    e0 = new BinaryExpression(t, e0,op,e1);
		}
		
         return e0;
	
		
	}
	
	
	
	private Expression Elem() throws SyntaxException {
		Expression e0 = null;
        Expression e1 = null;
		e0 = Thing();
		while (isKind(STRONG_OPS)) 
        {   Token op = t;
        	   consume(); 
            e1 = Thing(); 
		    e0 = new BinaryExpression(t, e0,op,e1);
		}
		
         return e0;
	
		
	}
	
	private Expression Thing() throws SyntaxException {
		Expression e0 = null;
        Expression e1 = null;
		e0 = Factor();
		while (isKind(VERY_STRONG_OPS)) 
        {   Token op = t;
        	   consume(); 
            e1 = Factor(); 
		    e0 = new BinaryExpression(t, e0,op,e1);
		}
		
         return e0;
	
		
	}
	

	
	List<KeyValueExpression> lstKVE;
	
	private List<KeyValueExpression> KeyValueList() throws SyntaxException {
		lstKVE = new ArrayList<KeyValueExpression>();
		
		KeyValueExpression kve;
		
		if(isKind(Kind.NL_NULL)) match(Kind.NL_NULL);
		else if(isKind(FIRSTFactor())){
			kve = KeyValueExpression();
			lstKVE.add(kve);
			while(isKind(COMMA)){
				match(COMMA);
				kve = KeyValueExpression();
				lstKVE.add(kve);
			}
		}
		else {
		
		}
		
		return lstKVE;
	}
	
	private Kind[] FIRSTFactor() throws SyntaxException{
		Kind[] fact={IDENT, INT_LIT, BL_FALSE, BL_TRUE, STRING_LIT, LPAREN, NOT, MINUS, Kind.KW_KEY, Kind.KW_VALUE
				, AT, LCURLY};
		return fact;
	}
	
	private KeyValueExpression KeyValueExpression() throws SyntaxException {
		Expression key = Expression();
		match(COLON);
		Expression value = Expression();
		
		return new KeyValueExpression(t, key, value);
	}
	
	
	
	List<KeyValueExpression> mapList;
	
	private MapListExpression MapList() throws SyntaxException {
		match(AT);
		match(AT);
		match(LSQUARE);
		mapList = KeyValueList();
		match(RSQUARE);
		
		return new MapListExpression(t, mapList);
		
	}
	
	
	private RangeExpression RangeExpression() throws SyntaxException {
			Expression lower = Expression();
			match(RANGE);
			Expression upper = Expression();
			return new RangeExpression(t, lower, upper);
	}
	
	private void RelOp() throws SyntaxException {
		if(isKind(BAR))
			match(BAR);
		else if(isKind(AND))
			match(AND);
		else if(isKind(EQUAL))
			match(EQUAL);
		else if(isKind(NOTEQUAL))
			match(NOTEQUAL);
		else if(isKind(LT))
			match(LT);
		else if(isKind(GT))
			match(GT);
		else if(isKind(LE))
			match(LE);
		else
			match(GE);
		
	}
	
	private void WeakOp() throws SyntaxException {
		if(isKind(PLUS))
			match(PLUS);
		else
			match(MINUS);
		
	}
	
	private void StrongOp() throws SyntaxException {
		if(isKind(TIMES))
			match(TIMES);
		else
			match(DIV);
		
	}
	
	private Expression Factor() throws SyntaxException {
		if(isKind(IDENT)){
			if(tokens.nextToNextToken().kind == LPAREN){  //ClosureEvalExpression being handled here
				Token identToken = t;
				ClosureEvalExpression();
				return new ClosureEvalExpression(t, identToken, closureEvalExpressionList);
			}
			else{
				Token identToken = t;
			match(IDENT);
			if(isKind(LSQUARE)){
				match(LSQUARE);
				Expression expression = Expression();
				match(RSQUARE);
				return new ListOrMapElemExpression(t, identToken, expression);
			}
			return new IdentExpression(t, identToken);
		}
		}
		else if(isKind(INT_LIT)){
			int value = t.getIntVal();
			match(INT_LIT);
			return new IntLitExpression(t, value);
		}
		else if(isKind(BL_TRUE)) { boolean value = t.getBooleanVal();  match(BL_TRUE);   return new BooleanLitExpression(t, value);}
		else if(isKind(BL_FALSE)) { boolean value = t.getBooleanVal();  match(BL_FALSE);   return new BooleanLitExpression(t, value);}
		else if (isKind(STRING_LIT)) 
			{
			String value = t.getText();
			match(STRING_LIT);
			return new StringLitExpression(t, value);
			}
		else if(isKind(LPAREN)){
			match(LPAREN);
			Expression expression = Expression();
			match(RPAREN);
			return expression;
		}
		else if(isKind(NOT)){
			Token op = t;
			match(NOT);
			Expression expression = Factor();
			return new UnaryExpression(t, op, expression);
		}
		else if(isKind(MINUS)){
			Token op = t;
			match(MINUS);
			Expression expression =	Factor();
			return new UnaryExpression(t, op, expression);
		}
		else if(isKind(Kind.KW_SIZE)){
			match(Kind.KW_SIZE);
			match(LPAREN);
			Expression expression = Expression();
			match(RPAREN);
			return new SizeExpression(t, expression);
		}
		else if(isKind(Kind.KW_KEY)){
			match(Kind.KW_KEY);
			match(LPAREN);
			Expression expression = Expression();
			match(RPAREN);
			return new KeyExpression(t, expression);
		}
		else if(isKind(Kind.KW_VALUE)){
			match(Kind.KW_VALUE);
			match(LPAREN);
			Expression expression = Expression();
			match(RPAREN);
			return new ValueExpression(t, expression);
		}
		
		
		else if(isKind(AT)){
			if(tokens.nextToNextToken().kind == AT){
				
						MapList();
				return new MapListExpression(t, mapList);
			}
			else{
				List();
				return new ListExpression(t, listList);
			}
		}
		else{
			Closure closure = Closure();
			return new ClosureExpression(t, closure);
		}
		
	}
	
	private void VeryStrongOp() throws SyntaxException {
		if(isKind(LSHIFT))
			match(LSHIFT);
		else
			match(RSHIFT);
	}
	
	
   //New Addition Starts Here
	

	List<SyntaxException> exceptionList = new ArrayList<SyntaxException>();
	List<QualifiedName> imports = new ArrayList<QualifiedName>();
	List<BlockElem> blockList;
	List<VarDec> formalArgList;
	
	public void parse1() throws SyntaxException {
		Program();
		match(EOF);
	}
	
	private Program Program() throws SyntaxException {
		List<QualifiedName> imp = ImportList();
		match(KW_CLASS);
		String className = t.getText();
		match(IDENT);
		Block b = Block();
		return new Program(t, imp,className, b );
	}
	
	public Program parse() {
		
		Program p = null;
		try {
		p = Program();
		if (p != null)
		match(EOF);
		} catch (SyntaxException e) {
		exceptionList.add(e);
		}
		
		if (exceptionList.isEmpty()){
		return p;
		}
		else
		{
			//for(SyntaxException ex : exceptionList) System.out.println(ex);
		return null;
		}
	}
	
	public List<SyntaxException> getExceptionList(){
		return exceptionList;
	}
	private List<QualifiedName> ImportList() throws SyntaxException {
		while(isKind(KW_IMPORT)){
		match(KW_IMPORT);
		String a = t.getText();
		match(IDENT);
		
		while(isKind(DOT)){
			match(DOT);
			a  += "/" + t.getText();
			match(IDENT);
		}
		imports.add(new QualifiedName(t, a));
		a = "";
		match(SEMICOLON);
		}
		return imports;
	}
	
	private Block Block() throws SyntaxException {
	
	/*	blockList = new ArrayList<BlockElem>();
		match(LCURLY);
		Kind[] first = FIRSTStatement();
		while(isKind(KW_DEF) || isKind(FIRSTStatement()) || isKind(SEMICOLON)){
		if(isKind(KW_DEF)){
				Declaration dec = Declaration();
				blockList.add(dec);
				match(SEMICOLON);
			}
		else if(isKind(SEMICOLON)){
			match(SEMICOLON);
		}
			else{
				Statement st = Statement();
			//	blockList.add(st);
				match(SEMICOLON);
			}
		}
		
		match(RCURLY);
		return new Block(t, blockList);
		*/
		Token firstToken=t;
		List<BlockElem> list=new ArrayList<BlockElem>();
		BlockElem blockElem=null;
		match(LCURLY);
		while(isKind(KW_DEF) || isKind(IDENT) || isKind(KW_PRINT) || isKind(KW_WHILE) || isKind(KW_IF) || isKind(MOD) || isKind(KW_RETURN) || isKind(SEMICOLON))
		{
			if(isKind(IDENT) || isKind(KW_PRINT) || isKind(KW_WHILE) || isKind(KW_IF) || isKind(MOD) || isKind(KW_RETURN) || isKind(SEMICOLON))
			{
				blockElem=Statement();
				
			}
			else 
				blockElem=Declaration();
			
			if(blockElem!=null)
				list.add(blockElem);
			match(SEMICOLON);
		}		
		match(RCURLY);
		return new Block(firstToken,list);
	}

	public String getErrors() {
		// TODO Auto-generated method stub
		return exceptionList.get(0).toString();
	}

}