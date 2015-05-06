package cop5555sp15;

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

public class SimpleParser {

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

	SimpleParser(TokenStream tokens) {
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


	public void parse() throws SyntaxException {
		Program();
		match(EOF);
	}

	private void Program() throws SyntaxException {
		ImportList();
		match(KW_CLASS);
		match(IDENT);
		Block();
	}

	private void ImportList() throws SyntaxException {
		while(isKind(KW_IMPORT)){
		match(KW_IMPORT);
		match(IDENT);
		
		while(isKind(DOT)){
			match(DOT);
			match(IDENT);
		}
		
		match(SEMICOLON);
		}
		
	}

	private void Block() throws SyntaxException {
		match(LCURLY);
		Kind[] first = FIRSTStatement();
		while(isKind(KW_DEF) || isKind(first) || isKind(SEMICOLON)){
		if(isKind(KW_DEF)){
				Declaration();
				match(SEMICOLON);
			}
			else{
				Statement();
				match(SEMICOLON);
			}
		}
		
		match(RCURLY);
		
	}
	
	private Kind[] FIRSTStatement() throws SyntaxException {
		Kind[] arr = {IDENT, KW_PRINT, KW_WHILE, KW_IF, MOD, KW_RETURN, Kind.NL_NULL };
		return arr;
	}
	
	
	private void Declaration() throws SyntaxException {
		match(KW_DEF);
	    if(tokens.nextToNextToken().kind==Kind.ASSIGN){
	    	ClosureDec();
	    }
	    else{
	    	VarDec();
	    }
	}
	
	private void Statement() throws SyntaxException {
		if(isKind(IDENT)) {
			LValue();
			match(ASSIGN);
			Expression();
		}
		else if(isKind(KW_PRINT)){
			match(KW_PRINT);
			Expression();
		}
		else if(isKind(KW_IF)){
			match(KW_IF);
			match(LPAREN);
			Expression();
			//if(isKind(RANGE)){
				//match(RANGE);
				//Expression();
			//}
			match(RPAREN);
			Block();
			
			if(isKind(KW_ELSE)){
				match(KW_ELSE);
				Block();
			}
		}
		else if(isKind(MOD)){
			match(MOD);
			Expression();
		}
		else if(isKind(KW_RETURN)){
			match(KW_RETURN);
			Expression();
		}
		else if(isKind(KW_WHILE)){
			/*while(isKind(KW_WHILE)){
				match(KW_WHILE);
				match(LPAREN);
				Expression();
				match(RPAREN);
			}
			
			while(isKind(KW_WHILE)){
				match(KW_WHILE);
			}
			
			match(LPAREN);
			Expression();
			
			if(isKind(RANGE)){  //RangeExpression
			match(RANGE);
			Expression();
		    }
			
			match(RPAREN);
			
			Block();
			*/
			match(KW_WHILE);
			if(isKind(TIMES)){ 
			match(TIMES);
			match(LPAREN);
			Expression();
			
			if(isKind(RANGE)){  //RangeExpression
			match(RANGE);
			Expression();
			//RangeExpression();
		    }
			}
			else{
				match(LPAREN);
				Expression();
			}
			match(RPAREN);
			
			Block();
			
		}
		else if(isKind(Kind.NL_NULL)){
			match(Kind.NL_NULL);
		}
		else{
			
		}
	}
	
	
	private void VarDec() throws SyntaxException {
		match(IDENT);
		if(isKind(Kind.NL_NULL)){
			match(Kind.NL_NULL);
		}
		else if (isKind(COLON)){
			match(COLON);
			Type();
		}
	}
	
	private void ClosureDec() throws SyntaxException {
		match(IDENT);
		match(ASSIGN);
		Closure();
	}	
	
	private void Closure() throws SyntaxException {
		match(LCURLY);
		FormalArgList();
		match(ARROW);
		while(isKind(FIRSTStatement()) || isKind(SEMICOLON)){  //isKind(SEMICOLON) is added because Statement can be empty in which
																// case SEMICOLON will be encountered as it belongs to FOLLOW(Statement)
			Statement();
			match(SEMICOLON);
		}
		match(RCURLY);
	}
	
	
	private void Type() throws SyntaxException {
		if(isKind(KW_INT) || isKind(KW_BOOLEAN) || isKind(KW_STRING)){
			SimpleType();
		}
		else if(tokens.nextToNextToken().kind == AT){
			KeyValueType();

		}
		else{
			ListType();
		}
	}

	private void SimpleType() throws SyntaxException {
		if (isKind(KW_INT))	match(KW_INT);
		else if(isKind(KW_BOOLEAN)) match(KW_BOOLEAN);
		else match(KW_STRING);
	}

	private void KeyValueType() throws SyntaxException {
		match(AT);
		match(AT);
		match(LSQUARE);
		SimpleType();
		match(COLON);
		Type();
		match(RSQUARE);
	}

	private void ListType() throws SyntaxException {
		match(AT);
		match(LSQUARE);
		Type();
		match(RSQUARE);
	}

	private void FormalArgList() throws SyntaxException {
	if(isKind(Kind.NL_NULL)){
		match(Kind.NL_NULL);
	}
	
	else if(isKind(IDENT)){
		VarDec();
		while(isKind(COMMA)){
			match(COMMA);
			VarDec();
		}
	}
	else{
		
	}
	}

	private void LValue() throws SyntaxException {
		match(IDENT);
		if(isKind(LSQUARE)){
			match(LSQUARE);
			Expression();
			match(RSQUARE);
		}
	}
	
	private void ClosureEvalExpression() throws SyntaxException {
			match(IDENT);
			match(LPAREN);
			Expressionlist();
			match(RPAREN);
	}
	
	private void Expressionlist() throws SyntaxException {
		if(isKind(Kind.NL_NULL)) match(Kind.NL_NULL);
		else if(isKind(FIRSTFactor())){
			Expression();
			while(isKind(COMMA)){
				match(COMMA);
				Expression();
			}
		}
		else{
			
		}
	}
	
	private void List() throws SyntaxException {
		match(AT);
		match(LSQUARE);
		Expressionlist();
		match(RSQUARE);
	}
	
	private void Expression() throws SyntaxException {
		Term();
		while(isKind(REL_OPS)){
			RelOp();
			Term();
		}
	}
	
	
	private void KeyValueList() throws SyntaxException {
		if(isKind(Kind.NL_NULL)) match(Kind.NL_NULL);
		else if(isKind(FIRSTFactor())){
			KeyValueExpression();
			while(isKind(COMMA)){
				match(COMMA);
				KeyValueExpression();
			}
		}
		else {
		
		}
	}
	
	private Kind[] FIRSTFactor() throws SyntaxException{
		Kind[] fact={IDENT, INT_LIT, BL_FALSE, BL_TRUE, STRING_LIT, LPAREN, NOT, MINUS, Kind.KW_KEY, Kind.KW_VALUE
				, AT, LCURLY};
		return fact;
	}
	
	private void KeyValueExpression() throws SyntaxException {
		Expression();
		match(COLON);
		Expression();
	}
	
	private void MapList() throws SyntaxException {
		match(AT);
		match(AT);
		match(LSQUARE);
		KeyValueList();
		match(RSQUARE);
	}
	
	
	private void RangeExpression() throws SyntaxException {
			Expression();
			match(RANGE);
			Expression();
	}
	
	
	private void Term() throws SyntaxException {
		Elem();
		while(isKind(WEAK_OPS)){
			WeakOp();
			Elem();
		}
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
	
	private void Elem() throws SyntaxException {
		Thing();
		while (isKind(STRONG_OPS)){
			StrongOp();
			Thing();
		}
	}
	
	private void WeakOp() throws SyntaxException {
		if(isKind(PLUS))
			match(PLUS);
		else
			match(MINUS);
	}
	
	private void Thing() throws SyntaxException {
		Factor();
		while(isKind(VERY_STRONG_OPS)){
			VeryStrongOp();
			Factor();
		}
	}
	
	private void StrongOp() throws SyntaxException {
		if(isKind(TIMES))
			match(TIMES);
		else
			match(DIV);
	}
	
	private void Factor() throws SyntaxException {
		if(isKind(IDENT)){
			if(tokens.nextToNextToken().kind == LPAREN){  //ClosureEvalExpression being handled here
				ClosureEvalExpression();
			}
			else{
			match(IDENT);
			if(isKind(LSQUARE)){
				match(LSQUARE);
				Expression();
				match(RSQUARE);
			}
		}
		}
		else if(isKind(INT_LIT)){
			match(INT_LIT);
		}
		else if(isKind(BL_TRUE)) match(BL_TRUE);
		else if(isKind(BL_FALSE)) match(BL_FALSE);
		else if (isKind(STRING_LIT)) match(STRING_LIT);
		else if(isKind(LPAREN)){
			match(LPAREN);
			Expression();
			match(RPAREN);
		}
		else if(isKind(NOT)){
			match(NOT);
			Factor();
		}
		else if(isKind(MINUS)){
			match(MINUS);
			Factor();
		}
		else if(isKind(Kind.KW_SIZE)){
			match(Kind.KW_SIZE);
			match(LPAREN);
			Expression();
			match(RPAREN);
		}
		else if(isKind(Kind.KW_KEY)){
			match(Kind.KW_KEY);
			match(LPAREN);
			Expression();
			match(RPAREN);
		}
		else if(isKind(Kind.KW_VALUE)){
			match(Kind.KW_VALUE);
			match(LPAREN);
			Expression();
			match(RPAREN);
		}
		
		
		else if(isKind(AT)){
			if(tokens.nextToNextToken().kind == AT){
				MapList();
			}
			else{
				List();
			}
		}
		else{
			Closure();
		}
	}
	
	private void VeryStrongOp() throws SyntaxException {
		if(isKind(LSHIFT))
			match(LSHIFT);
		else
			match(RSHIFT);
	}

}