package cop5555sp15.ast;

import java.util.ArrayList;

import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Kind.*;
import cop5555sp15.TypeConstants;
import cop5555sp15.symbolTable.SymbolTable;

public class TypeCheckVisitor implements ASTVisitor, TypeConstants {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		ASTNode node;

		public TypeCheckException(String message, ASTNode node) {
			super(node.firstToken.lineNumber + ": " + message);
			this.node = node;
		}
	}

	SymbolTable symbolTable;

	public TypeCheckVisitor(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	boolean check(boolean condition, String message, ASTNode node)
			throws TypeCheckException {
		if (condition)
			return true;
		throw new TypeCheckException(message, node);
	}
		
	//==============================

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		
		if (arg == null) {
			program.JVMName = program.name;
		} else {
			program.JVMName = arg + "/" + program.name;
		}
		// ignore the import statement
		if (!symbolTable.insert(program.name, null)) {
			throw new TypeCheckException("name already in symbol table",
					program);
		}
		program.block.visit(this, true);
		return null;
	}

	
	/**
	 * Blocks define scopes. Check that the scope nesting level is the same at
	 * the end as at the beginning of block
	 */
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		int numScopes = symbolTable.enterScope();
		// visit children
		for (BlockElem elem : block.elems) {
			elem.visit(this, arg);
		}
		int numScopesExit = symbolTable.leaveScope();
		check(numScopesExit > 0 && numScopesExit == numScopes,
				"unbalanced scopes", block);
		return null;
	}

	//LEFT
	/** gets the type from the enclosed expression */
	@Override
	public Object visitListOrMapElemExpression(
			ListOrMapElemExpression listOrMapElemExpression, Object arg)
			throws Exception {
	/*	listOrMapElemExpression.expression.visit(this, arg);
		String type = listOrMapElemExpression.expression.getType();
		check(type.equals(intType), " Error in ListOrMapExpression", listOrMapElemExpression);
		return type;
		*/
		String ident = listOrMapElemExpression.identToken.getText();
		String type = (String)listOrMapElemExpression.expression.visit(this, arg);
		String resultType = "";
		String wholeType = "";
		Declaration dec =  symbolTable.lookup(ident);
		check(dec!=null, "variable is not declared", listOrMapElemExpression);
		
		if( ((VarDec)dec).type.getClass().getName().contains("ListType")  )
		{
			check(type.equals(intType), "int type needed", listOrMapElemExpression);
			resultType = ((ListType)((VarDec)dec).type).type.getJVMType();
			wholeType = "Ljava/util/List<"+resultType+">;";
			//System.out.println("in typeCheckVisitor.java: visitListOrMapElemExpression, resultType is " + resultType);
		}
		else if( ((VarDec)dec).type.getClass().getName().contains("KeyValueType")  )
		{
			
		}

		listOrMapElemExpression.setType(wholeType);
		return resultType;
		}	
	
	@Override
	public Object visitStringLitExpression(
			StringLitExpression stringLitExpression, Object arg)
			throws Exception {
		stringLitExpression.setType(stringType);
		return stringType;
	}
	

	/**
	 * Ensure that types on left and right hand side are compatible.
	 */
	@Override
	public Object visitAssignmentStatement(
			AssignmentStatement assignmentStatement, Object arg)
			throws Exception {
		assignmentStatement.lvalue.visit(this, arg);
		String lhsType = assignmentStatement.lvalue.getType();
		assignmentStatement.expression.visit(this, arg);	
		String exprType = assignmentStatement.expression.getType();

		return null;
		
	}

	
	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg)
			throws Exception {
		printStatement.expression.visit(this, null);
		return null;
	}
  

	// nothing to do here
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement,
			Object arg) throws Exception {
		
		return null;
	}

	@Override
	public Object visitSimpleType(SimpleType simpleType, Object arg)
			throws Exception {
		return simpleType.getJVMType();
	}

	@Override
	public Object visitSizeExpression(SizeExpression sizeExpression, Object arg)
			throws Exception {
		sizeExpression.expression.visit(this, arg);
		String type = sizeExpression.expression.getType();
		sizeExpression.setType(type);
		sizeExpression.expression.setType(type);
		return sizeExpression.expression.getType();
	}

	@Override
	public Object visitValueExpression(ValueExpression valueExpression,
			Object arg) throws Exception {
	
		return valueExpression.expression.getType();
	}
	
	@Override
	public Object visitListType(ListType listType, Object arg) throws Exception {
		
//		return listType.type.getJVMType();
		listType.type.visit(this, arg);
		return listType.getJVMType();
		}



	/**
	 * expression type is int
	 */
	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,
			Object arg) throws Exception {
		intLitExpression.setType(intType);
		return intType;
	}
	
//LEFT
	/**
	 * Checks that both expressions have type int.
	 * 
	 * Note that in spite of the name, this is not in the Expression type
	 * hierarchy.
	 */
	@Override
	public Object visitRangeExpression(RangeExpression rangeExpression,
			Object arg) throws Exception {
		
		if((rangeExpression.lower.getType() == intType) && (rangeExpression.upper.getType() == intType)){
			return intType;
		}
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	@Override
	public Object visitExpressionLValue(ExpressionLValue expressionLValue,
			Object arg) throws Exception {
		
		expressionLValue.expression.visit(this, arg);
		String LValType = expressionLValue.expression.getType();
		expressionLValue.setType(LValType); 
		return expressionLValue.getType();
	}

	@Override
	public Object visitExpressionStatement(
			ExpressionStatement expressionStatement, Object arg)
			throws Exception {
		Type type = (Type) expressionStatement.expression.visit(this, arg);
		expressionStatement.expression.setType(type.toString());
		return expressionStatement.expression.getType();
	}

    @Override
    public Object visitIdentLValue(IdentLValue identLValue, Object arg) throws Exception {
    //	System.out.println("TypeCheckVisitor :: visitIdentLvalue");
	VarDec identDec = (VarDec)symbolTable.lookup(identLValue.identToken.getText());
	if(identDec == null)
	    throw new TypeCheckException("undeclared variable", identLValue);
	identLValue.setType(identDec.type.getJVMType());
	return identDec.type.getJVMType();
    }

@Override
	public Object visitIfElseStatement(IfElseStatement ifElseStatement,
			Object arg) throws Exception {
	
		ifElseStatement.expression.visit(this, arg);
		String condType = ifElseStatement.expression.getType();
		if(!condType.equals(booleanType)) {
			throw new TypeCheckException("If Else Statement Error", ifElseStatement);	
		}
		ifElseStatement.ifBlock.visit(this,arg); 
		ifElseStatement.elseBlock.visit(this,arg); 
		return null;
	}

	/**
	 * expression type is boolean
	 */
	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg)
			throws Exception {
		
		ifStatement.expression.visit(this, arg);
		String condType = ifStatement.expression.getType();
		
		if(!condType.equals(booleanType)) {
			throw new TypeCheckException("If Statement Error", ifStatement);
		}
		ifStatement.block.visit(this,arg); 
		return null;
	}



	/**
	 * Sets the expressionType to booleanType and returns it
	 * 
	 * @param booleanLitExpression
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object visitBooleanLitExpression(
			BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		booleanLitExpression.setType(booleanType);
		return booleanType;
	}
	@Override
	public Object visitUndeclaredType(UndeclaredType undeclaredType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"undeclared types not supported");
	}
	/**
	 * if ! and boolean, then boolean else if - and int, then int else error
	 */
	@Override
	public Object visitUnaryExpression(UnaryExpression unaryExpression,
			Object arg) throws Exception {

		if(unaryExpression.op.kind != Kind.NOT && unaryExpression.op.kind != Kind.MINUS) {
			throw new TypeCheckException("Unary Expression Error", unaryExpression);
		}
		unaryExpression.expression.visit(this, arg);
		String ExprType = unaryExpression.expression.getType();
		if(unaryExpression.op.kind == Kind.NOT && !ExprType.equals(booleanType)) {
			throw new TypeCheckException("Unary Expression Error", unaryExpression);		
		} 
		else if(unaryExpression.op.kind == Kind.MINUS && !ExprType.equals(intType)) {
			throw new TypeCheckException("Unary Expression Error", unaryExpression);
		} else {
			unaryExpression.setType(ExprType);
			return ExprType;
		}

	}
	

	/**
	 * Ensure that both types are the same, save and return the result type
	 */
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression,
			Object arg) throws Exception {


			
			binaryExpression.expression0.visit(this, arg);
				String Expr0 =  binaryExpression.expression0.getType();
				binaryExpression.expression1.visit(this, arg);
				String Expr1 = binaryExpression.expression1.getType();
				binaryExpression.setType(null);
				if(binaryExpression.op.kind == Kind.PLUS ) { if(Expr0.equals(Expr1) && !Expr0.equals(booleanType) && !Expr1.equals(booleanType)) {
						binaryExpression.setType(Expr0.toString());
					}  
					else {
								throw new TypeCheckException("Binary Expression Error", binaryExpression);
						}
					
					
				}
				else if (binaryExpression.op.kind == Kind.MINUS || binaryExpression.op.kind == Kind.TIMES){
					if(Expr0.equals(Expr1) && (Expr0.equals(intType))){
						binaryExpression.setType(Expr0.toString());
					} else {
						throw new TypeCheckException("Binary Expression Error", binaryExpression);
					}			
				}	
				else if (binaryExpression.op.kind == Kind.AND || binaryExpression.op.kind == Kind.BAR){ 
					
					if(Expr0.equals(Expr1) && Expr0.equals(booleanType)) {
						binaryExpression.setType(booleanType);
					} else {
						throw new TypeCheckException("Binary Expression Error", binaryExpression);
					}
				}
				else if (binaryExpression.op.kind ==  Kind.DIV){
					if(Expr0.equals(Expr1) && Expr0.equals(intType)) {
						binaryExpression.setType(intType);;
					} else {
						throw new TypeCheckException("Binary Expression Error", binaryExpression);
					}
				}
				else if (binaryExpression.op.kind == Kind.EQUAL|| binaryExpression.op.kind ==  Kind.NOTEQUAL
						|| binaryExpression.op.kind == Kind.LT || binaryExpression.op.kind == Kind.GT
						|| binaryExpression.op.kind == Kind.LE || binaryExpression.op.kind == Kind.GE){ 
					if(Expr0.equals(Expr1)) {
						binaryExpression.setType(booleanType);;
					} else {
						throw new TypeCheckException("Binary Expression Error", binaryExpression);
					}
				}
				
				return binaryExpression.getType();
	}
	
	@Override
	public Object visitWhileStarStatement(
			WhileStarStatement whileStarStatement, Object arg) throws Exception {
		
		if(whileStarStatement.expression.getType() != booleanType)
			throw new TypeCheckException("While Star Statement Error", whileStarStatement);
		else 
			return whileStarStatement.expression.getType();
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg)
			throws Exception {
		/*
		whileStatement.expression.visit(this, arg);
		String condType = whileStatement.expression.getType();
		
		if(!condType.equals(booleanType)) {
			throw new TypeCheckException("While Statement Error", whileStatement);	
		}
		whileStatement.block.visit(this,arg); 
		return null;
		*/
		String type = (String)whileStatement.expression.visit(this, arg);
		check(type.equals(booleanType), "boolean type needed", whileStatement);
		whileStatement.block.visit(this, arg);
		return null;
	}


	  /**
     * Check that name has been declared in scope Get its type from the
     * declaration.
     * 
     */
    @Override
    public Object visitIdentExpression(IdentExpression identExpression,
				       Object arg) throws Exception {
   // 	System.out.println("TypeCheckVisitor :: visitIdentExpression");
	VarDec identDec = (VarDec)symbolTable.lookup(identExpression.identToken.getText());
	if(identDec == null)
	    throw new TypeCheckException("undeclared variable", identExpression);

	identExpression.setType(identDec.type.getJVMType()); 
	return identDec.type.getJVMType();
	
    	}
	
	/**
	 * check that this variable has not already been declared in the same scope.
	 */
	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws Exception {
	
		if(!symbolTable.insert(varDec.identToken.getText(), varDec)) {
			String error_msg = new String("Declaration " + varDec.identToken.getText() + " is already defined in the current scope\n");
			throw new TypeCheckException(error_msg, varDec);
		} 
			symbolTable.insert(varDec.identToken.getText(), varDec);
			
		varDec.type.visit(this, arg);
		return null;
	}
	@Override
	public Object visitQualifiedName(QualifiedName qualifiedName, Object arg) {
		return null;
	}

	/**
	 * A closure defines a new scope Visit all the declarations in the
	 * formalArgList, and all the statements in the statementList construct and
	 * set the JVMType, the argType array, and the result type
	 * 
	 * @param closure
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object visitClosure(Closure closure, Object arg) throws Exception {
		
		throw new UnsupportedOperationException("not yet implemented");
	}
	/**
	 * Make sure that the name has not already been declared and insert in
	 * symbol table. Visit the closure
	 */
	@Override
	public Object visitClosureDec(ClosureDec closureDec, Object arg) {
		return null;
	}
	/**
	 * Check that the given name is declared as a closure Check the argument
	 * types The type is the return type of the closure
	 */
	@Override
	public Object visitClosureEvalExpression(
			ClosureEvalExpression closureExpression, Object arg)
			throws Exception {
		return null;
	}
	@Override
	public Object visitClosureExpression(ClosureExpression closureExpression,
			Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitKeyExpression(KeyExpression keyExpression, Object arg)
			throws Exception {
		return null;
	}
	
	@Override
	public Object visitKeyValueExpression(
			KeyValueExpression keyValueExpression, Object arg) throws Exception {
		return null;
	}
	@Override
	public Object visitKeyValueType(KeyValueType keyValueType, Object arg)
			throws Exception {
		return null;
	}
	//LEFT
	
	// visit the expressions (children) and ensure they are the same type
	// the return type is "Ljava/util/ArrayList<"+type0+">;" where type0 is the
	// type of elements in the list
	// this should handle lists of lists, and empty list. An empty list is
	// indicated by "Ljava/util/ArrayList;".
	@Override
	public Object visitListExpression(ListExpression listExpression, Object arg)
			throws Exception {
		/*if(listExpression.expressionList.size()==0) return "Ljava/util/ArrayList;";
		listExpression.expressionList.get(0).visit(this, arg);
		String type = listExpression.expressionList.get(0).getType();
		for(Expression exp : listExpression.expressionList){
		exp.visit(this, arg);
		String t = exp.getType();
		check(t.equals(type), "Error in List Expression", listExpression);
	
		}
		
		return type;
		*/
		String resultType = "";
		//List<String> types = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		for(Expression e : listExpression.expressionList)
			types.add((String)e.visit(this, arg));
		
		for(int i = 0; i < types.size()-1; ++i)
			check(types.get(i).equals(types.get(i+1)), " incompatible type", listExpression);
	
		if(types.size() == 0)
			resultType = emptyList;
		else
			resultType = "Ljava/util/ArrayList<"+ types.get(0)+">;";
		
		listExpression.setType(resultType);
		return resultType;
	}
	
	@Override
	public Object visitMapListExpression(MapListExpression mapListExpression,
			Object arg) throws Exception {
		return null;
	}	

	/**
	 * All checking will be done in the children since grammar ensures that the
	 * rangeExpression is a rangeExpression.
	 */
	@Override
	public Object visitWhileRangeStatement(
			WhileRangeStatement whileRangeStatement, Object arg)
			throws Exception {
		return null;

	}


}
