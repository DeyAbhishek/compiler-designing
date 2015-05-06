package cop5555sp15;

import java.util.ArrayList;

import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import static cop5555sp15.TokenStream.Kind.*;

public class Scanner {

    TokenStream stream;
    
	public Scanner(TokenStream stream) {
	  
		this.stream = stream;
				
	}


	// Fills in the stream.tokens list with recognized tokens 
     //from the input
	public void scan() {
    
		char[] input = stream.inputChars;
		String str = new String(input);
		
		int n = input.length;
		
		ArrayList<Token> tokens = stream.tokens;
		Token token;
		int line = 1;
		
		
		int i = 0;
		for(i = 0; i < n; ){
			char ch = input[i];
			
			
			
			
			if(ch == '\t'){
				i++;
			}
		 if(ch == '\n' ){
				line++;
	}
		
		 if(ch=='\r'){
				line++;
			i++;
				if(i < n - 1){
					if(input[i + 1]=='\n'){
				
					i++;
				}
			}
			}
			
			
			if(ch == '.'){
				if(i<n-1)
					{
					if(input[i + 1] == '.'){
					token = stream.new Token(Kind.RANGE, i, i+2, line);
					tokens.add(token);
					i+=2;
				}
					else{
						token = stream.new Token(Kind.DOT, i, i + 1, line);
						tokens.add(token);
						i++;
					}
					}
				else{
					token = stream.new Token(Kind.DOT, i, i + 1, line);
					tokens.add(token);
					i++;
				}
			}
			
			else if(ch == ';'){
				token = stream.new Token(Kind.SEMICOLON, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		    
			else if (ch == ','){
				token = stream.new Token(Kind.COMMA, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		     
			else if (ch == '(' ){
				token = stream.new Token(Kind.LPAREN, i, i + 1, line);
				tokens.add(token);
				i++;
			}
			else if( ch ==')'){
				token = stream.new Token(Kind.RPAREN, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        
			else if(ch == '['){
				token = stream.new Token(Kind.LSQUARE, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        
			else if(ch == ']'){
				token = stream.new Token(Kind.RSQUARE, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        
			else if (ch == '{'){
				token = stream.new Token(Kind.LCURLY, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        
			else if (ch == '}'){
				token = stream.new Token(Kind.RCURLY, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        
			else if (ch== ':'){
				token = stream.new Token(Kind.COLON, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        
			else if(ch == '?'){
				token = stream.new Token(Kind.QUESTION, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        

			
			else if(ch == '='){
			int k = 1;
			if(i < n - 1){
				if(input[i+1]== '=') k =2;
			}
			if(k==1){
				token = stream.new Token(Kind.ASSIGN, i, i + 1, line);
				tokens.add(token);
				i++;
			}
			else{
				token = stream.new Token(Kind.EQUAL, i, i + 2, line);
				tokens.add(token);
				i+=2;
			}
			}
			
			
			else if (ch == '|'){
				token = stream.new Token(Kind.BAR, i, i + 1, line);
				tokens.add(token);
				i++;
			}
			else if(ch == '&'){
				token = stream.new Token(Kind.AND, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        

			else if (ch == '+'){
				token = stream.new Token(Kind.PLUS, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        
			else if(ch == '-'){
				if(i<n-1)
					{
					if(input[i + 1] == '>'){
					
					token = stream.new Token(Kind.ARROW, i, i+2, line);
					tokens.add(token);
					i+=2;
				}
					else{
						token = stream.new Token(Kind.MINUS, i, i + 1, line);
						tokens.add(token);
						i++;
					}
					}
				
				else{
					token = stream.new Token(Kind.MINUS, i, i + 1, line);
					tokens.add(token);
					i++;
				}
			}

			else if ( ch == '*'){
				token = stream.new Token(Kind.TIMES, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        

			else if (ch == '/') {
				int t = i;
				if(i < n-1){
					
					if(input[i+1]=='*'){
						
						i+=2;

						
						while(i <= n-2 && !str.substring(i, i+2).equals("*/")){
						
							i++;
						}
						
						i = i+2;
						if(i >= n+1){
							token = stream.new Token(Kind.UNTERMINATED_COMMENT, t, n, line);
							tokens.add(token);
						}
						
					}else{
						token = stream.new Token(Kind.DIV, i, i + 1, line);
						tokens.add(token);
						i++;
					}
				}
				else{
				token = stream.new Token(Kind.DIV, i, i + 1, line);
				tokens.add(token);
				i++;
			}
			}

			else if (ch == '%'){
				token = stream.new Token(Kind.MOD, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		      

			else if (ch == '@'){
				token = stream.new Token(Kind.AT, i, i + 1, line);
				tokens.add(token);
				i++;
			}
		        
			else if (ch == '!'){
				if(i<n-1 && input[i + 1] == '='){
					token = stream.new Token(Kind.NOTEQUAL, i, i+2, line);
					tokens.add(token);
					i+=2;
				}
				else{
					token = stream.new Token(Kind.NOT, i, i + 1, line);
					tokens.add(token);
					i++;
				}
			}

			else if (ch == '<'){
				if(i<n-1)
				{
				if(input[i + 1] == '<'){
			
					token = stream.new Token(Kind.LSHIFT, i, i+2, line);
					tokens.add(token);
					i+=2;
				}
				
				else if ( input[i + 1] == '='){
					token = stream.new Token(Kind.LE, i , i + 2, line);
					tokens.add(token);
					i += 2;
				}
				
				else{
					token = stream.new Token(Kind.LT, i, i + 1, line);
					tokens.add(token);
					i++;
				}
			}
				else{

					token = stream.new Token(Kind.LT, i, i + 1, line);
					tokens.add(token);
					i++;
				}
			}

			else if (ch == '>'){
				if(i<n-1){
					if(input[i + 1] == '>'){
				
					token = stream.new Token(Kind.RSHIFT, i, i+2, line);
					tokens.add(token);
					i+=2;
				}
				else if (input[i + 1] == '='){
					token = stream.new Token(Kind.GE, i , i + 2, line);
					tokens.add(token);
					i += 2;
				}
				else{
					token = stream.new Token(Kind.GT, i, i + 1, line);
					tokens.add(token);
					i++;
				}
			}else{
				token = stream.new Token(Kind.GT, i, i + 1, line);
				tokens.add(token);
				i++;
			}
			}
				
				
		       	else if(input[i] == '0'){
						token = stream.new Token(Kind.INT_LIT, i, i + 1, line);
						tokens.add(token);
						i++;
					}
				else if(Character.isDigit(input[i]) && input[i] != '0'){
						int j = i;
						i++;
						if(i<n){
						while(Character.isDigit(input[i]) && i < n){
						i++;	
						if(i==n) break;
						}
						}
						token = stream.new Token(Kind.INT_LIT, j, i , line);
						tokens.add(token);
					}
			
			
			
			
					else if(i + 4 < n && input[i] == 't' && str.substring(i, i + 4).equals("true") && !Character.isJavaIdentifierPart(input[i+4])){
						token = stream.new Token(Kind.BL_TRUE, i, i + 4, line);
						tokens.add(token);
						i+=4;
					}
					else if(i + 4 == n && input[i] == 't' && str.substring(i, i + 4).equals("true")){
						token = stream.new Token(Kind.BL_TRUE, i, i + 4, line);
						tokens.add(token);
						i+=4;
					}
					
			
					else if(i + 4 < n && input[i] == 'n' && str.substring(i, i + 4).equals("null") && !Character.isJavaIdentifierPart(input[i+4])){
						token = stream.new Token(Kind.NL_NULL, i, i + 4, line);
						tokens.add(token);
						i+=4;
					}
					else if(i + 4 == n && input[i] == 'n' && str.substring(i, i + 4).equals("null")){
						token = stream.new Token(Kind.NL_NULL, i, i + 4, line);
						tokens.add(token);
						i+=4;
					}
					else if(i + 5 < n && input[i] == 'f' && str.substring(i, i + 5).equals("false") && !Character.isJavaIdentifierPart(input[i+5])){
						token = stream.new Token(Kind.BL_FALSE, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
					else if(i + 5 == n && input[i] == 'f' && str.substring(i, i + 5).equals("false")){
						token = stream.new Token(Kind.BL_FALSE, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
					else if(i + 3 < n && input[i] == 'i' && str.substring(i, i + 3).equals("int") && !Character.isJavaIdentifierPart(input[i+3])){
						token = stream.new Token(Kind.KW_INT, i, i + 3, line);
						tokens.add(token);
						i+=3;
					}
					else if(i + 3 == n && input[i] == 'i' && str.substring(i, i + 3).equals("int")){
						token = stream.new Token(Kind.KW_INT, i, i + 3, line);
						tokens.add(token);
						i+=3;
					}
					else if(i + 6 < n && input[i] == 's' && str.substring(i, i + 6).equals("string") && !Character.isJavaIdentifierPart(input[i+6])){
						token = stream.new Token(Kind.KW_STRING, i, i + 6, line);
						tokens.add(token);
						i+=6;
					}
					else if(i + 6 == n && input[i] == 's' && str.substring(i, i + 6).equals("string")){
						token = stream.new Token(Kind.KW_STRING, i, i + 6, line);
						tokens.add(token);
						i+=6;
					}
			
					else if(i + 4 < n && input[i] == 's' && str.substring(i, i + 4).equals("size") && !Character.isJavaIdentifierPart(input[i+4])){
						token = stream.new Token(Kind.KW_SIZE, i, i + 4, line);
						tokens.add(token);
						i+=4;
					}
					else if(i + 4 == n && input[i] == 's' && str.substring(i, i + 4).equals("size")){
						token = stream.new Token(Kind.KW_SIZE, i, i + 4, line);
						tokens.add(token);
						i+=4;
					}
					else if(i + 3 < n && input[i] == 'k' && str.substring(i, i + 3).equals("key") && !Character.isJavaIdentifierPart(input[i+3])){
						token = stream.new Token(Kind.KW_KEY, i, i + 3, line);
						tokens.add(token);
						i+=3;
					}
					else if(i + 3 == n && input[i] == 'k' && str.substring(i, i + 3).equals("key")){
						token = stream.new Token(Kind.KW_KEY, i, i + 3, line);
						tokens.add(token);
						i+=3;
					}
					else if(i + 5 < n && input[i] == 'v' && str.substring(i, i + 5).equals("value") && !Character.isJavaIdentifierPart(input[i+5])){
						token = stream.new Token(Kind.KW_VALUE, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
					else if(i + 5 == n && input[i] == 'v' && str.substring(i, i + 5).equals("value")){
						token = stream.new Token(Kind.KW_VALUE, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
			
					else if(i + 7 < n && input[i] == 'b' && str.substring(i, i + 7).equals("boolean") && !Character.isJavaIdentifierPart(input[i+7])){
						token = stream.new Token(Kind.KW_BOOLEAN, i, i + 7, line);
						tokens.add(token);
						i+=7;
					}
					else if(i + 7 == n && input[i] == 'b' && str.substring(i, i + 7).equals("boolean")){
						token = stream.new Token(Kind.KW_BOOLEAN, i, i + 7, line);
						tokens.add(token);
						i+=7;
					}
					else if(i + 6 < n && input[i] == 'i' && str.substring(i, i + 6).equals("import") && !Character.isJavaIdentifierPart(input[i+6])){
						token = stream.new Token(Kind.KW_IMPORT, i, i + 6, line);
						tokens.add(token);
						i+=6;
					}
					else if(i + 6 == n && input[i] == 'i' && str.substring(i, i + 6).equals("import")){
						token = stream.new Token(Kind.KW_IMPORT, i, i + 6, line);
						tokens.add(token);
						i+=6;
					}
					else if(i + 5 < n && input[i] == 'c' && str.substring(i, i + 5).equals("class") && !Character.isJavaIdentifierPart(input[i+5])){
						token = stream.new Token(Kind.KW_CLASS, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
					else if(i + 5 == n && input[i] == 'c' && str.substring(i, i + 5).equals("class")){
						token = stream.new Token(Kind.KW_CLASS, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
					else if(i + 3 < n && input[i] == 'd' && str.substring(i, i + 3).equals("def") && !Character.isJavaIdentifierPart(input[i+3])){
						token = stream.new Token(Kind.KW_DEF, i, i + 3, line);
						tokens.add(token);
						i+=3;
					}
					else if(i + 3 == n && input[i] == 'd' && str.substring(i, i + 3).equals("def")){
						token = stream.new Token(Kind.KW_DEF, i, i + 3, line);
						tokens.add(token);
						i+=3;
					}
					else if(i + 5 < n && input[i] == 'w' && str.substring(i, i + 5).equals("while") && !Character.isJavaIdentifierPart(input[i+5])){ 
						token = stream.new Token(Kind.KW_WHILE, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
					else if(i + 5 == n && input[i] == 'w' && str.substring(i, i + 5).equals("while")){
						token = stream.new Token(Kind.KW_WHILE, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
					else if(i + 2 < n && input[i] == 'i' && str.substring(i, i + 2).equals("if") && !Character.isJavaIdentifierPart(input[i+2])){
						token = stream.new Token(Kind.KW_IF, i, i + 2, line);
						tokens.add(token);
						i+=2;
					}
					else if(i + 2 == n && input[i] == 'i' && str.substring(i, i + 2).equals("if")){
						token = stream.new Token(Kind.KW_IF, i, i + 2, line);
						tokens.add(token);
						i+=2;
					}
					else if(i + 4 < n && input[i] == 'e' && str.substring(i, i + 4).equals("else") && !Character.isJavaIdentifierPart(input[i+4])){
						token = stream.new Token(Kind.KW_ELSE, i, i + 4, line);
						tokens.add(token);
						i+=4;
					}
					else if(i + 4 ==n && input[i] == 'e' && str.substring(i, i + 4).equals("else")){
						token = stream.new Token(Kind.KW_ELSE, i, i + 4, line);
						tokens.add(token);
						i+=4;
					}
					else if(i + 6 < n && input[i] == 'r' && str.substring(i, i + 6).equals("return") && !Character.isJavaIdentifierPart(input[i+6])){
						token = stream.new Token(Kind.KW_RETURN, i, i + 6, line);
						tokens.add(token);
						i+=6;
					}
					else if(i + 6 ==n && input[i] == 'r' && str.substring(i, i + 6).equals("return")){
						token = stream.new Token(Kind.KW_RETURN, i, i + 6, line);
						tokens.add(token);
						i+=6;
					}
					else if(i + 5 < n && input[i] == 'p' && str.substring(i, i + 5).equals("print") && 
							!Character.isJavaIdentifierPart(input[i+5])){
						token = stream.new Token(Kind.KW_PRINT, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
					else if(i + 5 == n && input[i] == 'p' && str.substring(i, i + 5).equals("print")){
						token = stream.new Token(Kind.KW_PRINT, i, i + 5, line);
						tokens.add(token);
						i+=5;
					}
					
			
			
			
					else if(Character.isJavaIdentifierStart(input[i])){
			
							int j = i++;
						
						if(i<n){
						while(Character.isJavaIdentifierPart(input[i])){
							i++;
							if(i == n)break;
						}
						}
						token = stream.new Token(Kind.IDENT, j, i, line);
						tokens.add(token);
					}
					
					
					else if(ch== '\"'){
						boolean flag = false;
								
						int j = i;
						i++;
						
						while(input[i]!='\"'){
							i++;
						    if(i==n){
								flag = true;
								token = stream.new Token(Kind.UNTERMINATED_STRING, j, i, line);
								tokens.add(token);
								break;
							}
						    
						    else{   //handling " \\\" " structure
								if (input[i-1]=='\\' && input[i]=='\"'){
									i+=1;
									}
						    }
							
						}
					      i++;
					    
                      // if(i!=n){
						if(!flag){
					      token = stream.new Token(Kind.STRING_LIT, j, i, line);
						tokens.add(token);
                        }
					}
			
					
					else{
						if(Character.isWhitespace(input[i])){
							i++;
						}
						else
						{
							token = stream.new Token(Kind.ILLEGAL_CHAR, i, i+1, line);
							tokens.add(token);
						
							i++;
					}
					}
					
					
			   
			
			}
			

		token = stream.new Token(Kind.EOF, i, i, line);
		tokens.add(token);
			
			
			
			
			
		}
		
		
	}