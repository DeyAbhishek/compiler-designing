package cop5555sp15.ast;

import org.objectweb.asm.*;

import cop5555sp15.TokenStream.Kind;
import cop5555sp15.symbolTable.SymbolTable;
import cop5555sp15.TypeConstants;

public class CodeGenVisitor implements ASTVisitor, Opcodes, TypeConstants {

	ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
	// Because we used the COMPUTE_FRAMES flag, we do not need to
	// insert the mv.visitFrame calls that you will see in some of the
	// asmifier examples. ASM will insert those for us.
	// FYI, the purpose of those instructions is to provide information
	// about what is on the stack just before each branch target in order
	// to speed up class verification.
	FieldVisitor fv;
	String className;
	String classDescriptor;

	// This class holds all attributes that need to be passed downwards as the
	// AST is traversed. Initially, it only holds the current MethodVisitor.
	// Later, we may add more attributes.
	static class InheritedAttributes {
		public InheritedAttributes(MethodVisitor mv) {
			super();
			this.mv = mv;
		}

		MethodVisitor mv;
	}
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		className = program.JVMName;
		classDescriptor = 'L' + className + ';';
		cw.visit(52, // version
				ACC_PUBLIC + ACC_SUPER, // access codes
				className, // fully qualified classname
				null, // signature
				"java/lang/Object", // superclass
				new String[] { "cop5555sp15/Codelet" } // implemented interfaces
		);
		cw.visitSource(null, null); // maybe replace first argument with source
									// file name

		// create init method
		{
			MethodVisitor mv;
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(3, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V", false);
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}

		// generate the execute method
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "execute", // name of top
																	// level
																	// method
				"()V", // descriptor: this method is parameterless with no
						// return value
				null, // signature.  This is null for us, it has to do with generic types
				null // array of strings containing exceptions
				);
		mv.visitCode();
		Label lbeg = new Label();
		mv.visitLabel(lbeg);
		mv.visitLineNumber(program.firstToken.lineNumber, lbeg);
		program.block.visit(this, new InheritedAttributes(mv));
		mv.visitInsn(RETURN);
		Label lend = new Label();
		mv.visitLabel(lend);
		mv.visitLocalVariable("this", classDescriptor, null, lbeg, lend, 0);
		mv.visitMaxs(0, 0);  //this is required just before the end of a method. 
		                     //It causes asm to calculate information about the
		                     //stack usage of this method.
		mv.visitEnd();

		
		cw.visitEnd();
		return cw.toByteArray();
	}
	

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		for (BlockElem elem : block.elems) {
			elem.visit(this, arg);
		}
		return null;
	}


	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(printStatement.firstToken.getLineNumber(), l0);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
				"Ljava/io/PrintStream;");
		printStatement.expression.visit(this, arg); // adds code to leave value
													// of expression on top of
													// stack.
													// Unless there is a good
													// reason to do otherwise,
													// pass arg down the tree
		String etype = printStatement.expression.getType();
		if (etype.equals("I") || etype.equals("Z")
				|| etype.equals("Ljava/lang/String;")) {
			String desc = "(" + etype + ")V";
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					desc, false);
		} else
			throw new UnsupportedOperationException(
					"printing list or map not yet implemented");
		return null;
	}

	@Override
	public Object visitStringLitExpression(
			StringLitExpression stringLitExpression, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		mv.visitLdcInsn(stringLitExpression.value);
		return null;
	}

	
	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,
			Object arg) throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv; // this should be the
															// first statement
															// of all visit
															// methods that
															// generate
															// instructions
		mv.visitLdcInsn(intLitExpression.value);
		
		return null;
	}
	
	@Override
	public Object visitBooleanLitExpression(
			BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		mv.visitLdcInsn(booleanLitExpression.value);
		return null;
	}
	
		@Override
		public Object visitIdentExpression(IdentExpression identExpression,
				Object arg) throws Exception {
	
		//	MethodVisitor mv = ((InheritedAttributes) arg).mv;
			//mv.visitFieldInsn(GETSTATIC, className, identExpression.identToken.getText(), intType);
			//return null;
			MethodVisitor mv = ((InheritedAttributes) arg).mv;
			mv.visitVarInsn(ALOAD, 0);
			String ident = identExpression.identToken.getText();
			String type = identExpression.getType();
			if(type.equals("I") || type.equals("Z") || type.equals(stringType))
				mv.visitFieldInsn(GETFIELD, className, ident, type);
			/*else
			{
				if(type.contains("I"))
				{
					
					char l[] = type.toCharArray();
					int i;
					int angleCount = 0;
					for(i = l.length-1; i >= 0; --i)
					{
						if(l[i] == 'I')
							break;
						else
							++angleCount;
					}
					type = new String(l, 0, i-0-angleCount+1);
					//System.out.println("in CodeGenVisitor.java: visitIdentExpression, " + ident + ", " + type);
					mv.visitFieldInsn(GETFIELD, className, ident, type+";");
				}
				else if(type.contains("Z"))
				{
					type = type.replace("Z", "Ljava/lang/Boolean;");
				}
			}
			*/
			return null;
			
		}
		
		

		@Override
		public Object visitIdentLValue(IdentLValue identLValue, Object arg)
				throws Exception {
			return null;
		}
		@Override
		public Object visitReturnStatement(ReturnStatement returnStatement,
				Object arg) throws Exception {
		return null;
		}
		
		@Override
		public Object visitUnaryExpression(UnaryExpression unaryExpression,
				Object arg) throws Exception {
			
			MethodVisitor mv = ((InheritedAttributes) arg).mv;
			unaryExpression.expression.visit(this, arg);
			if(unaryExpression.op.kind == Kind.MINUS) {
				mv.visitInsn(INEG);
			} 
			else if(unaryExpression.op.kind == Kind.NOT) {
				Label EndofJumpLabel = new Label();
				Label falseLabel = new Label();
				mv.visitJumpInsn(IFEQ, falseLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, EndofJumpLabel);
				mv.visitLabel(falseLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(EndofJumpLabel);
			} 
			return null;
			
		}

		
		@Override
		public Object visitVarDec(VarDec varDec, Object arg) throws Exception {
		
			/*String fieldName = varDec.identToken.getText();  
			varDec.type.visit(this, null);		
			String fieldType = varDec.type.getJVMType();
			fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
			fv.visitEnd();

			return null;
			*/

				//System.out.println("in CodeGenVisitor.java:  visitVarDec");
				String varName = varDec.identToken.getText();
				
				String type = (String)varDec.type.visit(this, arg);
				
				//System.out.println("here");
				if(type.equals("I") || type.equals("Z") || type.equals("Ljava/lang/String;"))
				{
					fv = cw.visitField(0, varName, type, null, null);
					fv.visitEnd();
				}
				else
				{
					type = type.replace("I", "Ljava/lang/Integer;");
					type = type.replace("Z", "Ljava/lang/Boolean;");
					String fieldType = "Ljava/util/List;";
					//System.out.println("in CodeGenVisitor.java:  visitVarDec, " + type + ", " + varName);
					fv = cw.visitField(0, varName, fieldType, type, null);
					fv.visitEnd();
				}
				
			    return null;
			}
		
	@Override
	public Object visitAssignmentStatement(
			AssignmentStatement assignmentStatement, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitAssignmentStatement");
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		IdentLValue identLValue = ((IdentLValue)assignmentStatement.lvalue);
		String lvaluetype = identLValue.getType();
		String ident = identLValue.identToken.getText();
		String expressionType = assignmentStatement.expression.getType();
		if(lvaluetype.equals(intType) || lvaluetype.equals(booleanType) || lvaluetype.equals(stringType))
		{
			mv.visitVarInsn(ALOAD, 0);
			assignmentStatement.expression.visit(this, arg);
			mv.visitFieldInsn(PUTFIELD, className, ident, lvaluetype);
		}
		else
		{
			if(lvaluetype.contains("I"))
			{
				
				char l[] = lvaluetype.toCharArray();
				int i;
				int angleCount = 0;
				for(i = l.length-1; i >= 0; --i)
				{
					if(l[i] == 'I')
						break;
					else
						++angleCount;
				}
				lvaluetype = new String(l, 0, i-0-angleCount+1);
				
				l = expressionType.toCharArray();
				angleCount = 0;
				for(i = l.length-1; i >= 0; --i)
				{
					if(l[i] == 'I')
						break;
					else
						++angleCount;
				}
				if(i != -1)
					expressionType = new String(l, 0, i-angleCount+1);
				String type = expressionType.substring(1);
				//System.out.println("in CodeGenVisitor.java: visitAssignmentStatement, " + ident + ", " + lvaluetype + ", " + expressionType + ", " + type);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitTypeInsn(NEW, type);
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, type, "<init>", "()V", false);
				mv.visitFieldInsn(PUTFIELD, className, ident, lvaluetype+";");
				assignmentStatement.expression.visit(this, arg);
				int number = ((ListExpression)assignmentStatement.expression).expressionList.size();
				if(number == 0)
				{
					//System.out.println("number is 0");
				}
				else
				{
					for(i = 0; i < number; ++i)
					{
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, className, ident, lvaluetype+";");
						mv.visitInsn(SWAP);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
						mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
						mv.visitInsn(POP);
					}
					
				}
			}
			else if(lvaluetype.contains("Z"))
			{
				lvaluetype = lvaluetype.replace("Z", "Ljava/lang/Boolean;");
			}
			else//contains Ljava/lang/String
			{
				
			}
		}
		return null;
	}


	
		@Override
		public Object visitBinaryExpression(BinaryExpression binaryExpression,
				Object arg) throws Exception {
			MethodVisitor mv = ((InheritedAttributes) arg).mv;

			Kind op = binaryExpression.op.kind;
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			String type1 = binaryExpression.expression0.getType();
			String type2 = binaryExpression.expression1.getType();
			
			
			if(binaryExpression.op.kind == Kind.EQUAL){		
				if(type1.compareTo("I") == 0 || type1.compareTo("Z") == 0){

					Label neq = new Label();
					mv.visitJumpInsn(IF_ICMPNE, neq);

					mv.visitInsn(ICONST_1);
					Label end = new Label();
					mv.visitJumpInsn(GOTO, end);

					mv.visitLabel(neq);
					mv.visitInsn(ICONST_0);

					mv.visitLabel(end);
				}
				 
				
				else if(type1.compareTo("Ljava/lang/String;") == 0){				
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I");
					Label ne = new Label();
					mv.visitJumpInsn(IFNE, ne);

					mv.visitInsn(ICONST_1);				
					Label end = new Label();
					mv.visitJumpInsn(GOTO, end);

					mv.visitLabel(ne);
					mv.visitInsn(ICONST_0);

					mv.visitLabel(end);	
				}
				type1 = "Z";
			}
			else if(binaryExpression.op.kind == Kind.NOTEQUAL){		
				if(type1.compareTo("I") == 0 || type1.compareTo("Z") == 0){

					Label neq = new Label();
					mv.visitJumpInsn(IF_ICMPEQ, neq);

					mv.visitInsn(ICONST_1);
					Label end = new Label();
					mv.visitJumpInsn(GOTO, end);

					mv.visitLabel(neq);
					mv.visitInsn(ICONST_0);

					mv.visitLabel(end);
				}
				else if(type1.compareTo("Ljava/lang/String;") == 0){				
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I");
					Label ne = new Label();
					mv.visitJumpInsn(IFEQ, ne);

					mv.visitInsn(ICONST_1);				
					Label end = new Label();
					mv.visitJumpInsn(GOTO, end);

					mv.visitLabel(ne);
					mv.visitInsn(ICONST_0);

					mv.visitLabel(end);	
				}
				type1 = "Z";
			}
			else if(binaryExpression.op.kind == Kind.PLUS){
				//addition
				if(type1.compareTo("I") == 0 && type2.compareTo("I") == 0){
					mv.visitInsn(IADD);
				}
				//string concatenation
				else if(type1.compareTo("Ljava/lang/String;") == 0 || type2.compareTo("Ljava/lang/String;") == 0){
					mv.visitInsn(SWAP);
					mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
					mv.visitInsn(DUP);				
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "("+type1+")Ljava/lang/StringBuilder;");
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "("+type2+")Ljava/lang/StringBuilder;");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");	
					type1 = "Ljava/lang/String;";
				}
			}
			else if ( binaryExpression.op.kind == Kind.MINUS){
				if(type1.compareTo("I") == 0){	
					mv.visitInsn(ISUB);
				}
		
			}
			else if ( binaryExpression.op.kind ==  Kind.TIMES){
				mv.visitInsn(IMUL);
			}
			else if ( binaryExpression.op.kind ==  Kind.DIV){
				mv.visitInsn(IDIV);
			}
			else if ( binaryExpression.op.kind ==  Kind.AND){
				Label label = new Label();
				mv.visitInsn(SWAP);
				Label valFalseOp1 = new Label();
				mv.visitJumpInsn(IFEQ, valFalseOp1);
				Label valFalseOp2 = new Label();
				mv.visitJumpInsn(IFEQ, valFalseOp2);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, label);
				mv.visitLabel(valFalseOp1);
				mv.visitInsn(POP);
				mv.visitLabel(valFalseOp2);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label);				
			
				
			}
			else if ( binaryExpression.op.kind ==  Kind.BAR){
		
				Label l = new Label();
				mv.visitInsn(SWAP);
				Label valTrueOp1 = new Label();
				mv.visitJumpInsn(IFNE, valTrueOp1);
				Label valTrueOp2 = new Label();
				mv.visitJumpInsn(IFNE, valTrueOp2);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l);
				mv.visitLabel(valTrueOp1);
				mv.visitInsn(POP);
				mv.visitLabel(valTrueOp2);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l);
				

			}
			else if ( binaryExpression.op.kind ==  Kind.LT ||binaryExpression.op.kind ==  Kind.GE
					|| binaryExpression.op.kind == Kind.GT || binaryExpression.op.kind ==  Kind.LE){	// <
				Label JumpOnTrueLabel = new Label();
				if(binaryExpression.op.kind ==  Kind.GT)
					mv.visitJumpInsn(IF_ICMPGT, JumpOnTrueLabel);
				if(binaryExpression.op.kind ==  Kind.LT)
					mv.visitJumpInsn(IF_ICMPLT, JumpOnTrueLabel);
				if(binaryExpression.op.kind ==  Kind.GE) 
					mv.visitJumpInsn(IF_ICMPGE, JumpOnTrueLabel);
				if(binaryExpression.op.kind ==  Kind.LE) // less than OR equal to
					mv.visitJumpInsn(IF_ICMPLE, JumpOnTrueLabel);
				
				Label EndofJumpLabel = new Label();
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, EndofJumpLabel);
				mv.visitLabel(JumpOnTrueLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(EndofJumpLabel);
				
				
			}

			return null;
		}
		

	
	

		@Override
		public Object visitExpressionLValue(ExpressionLValue expressionLValue,
				Object arg) throws Exception {
			return null;
		}
		

		@Override
		public Object visitExpressionStatement(
				ExpressionStatement expressionStatement, Object arg)
				throws Exception {
			return null;
			}
		
		@Override
		public Object visitQualifiedName(QualifiedName qualifiedName, Object arg) {
			return null;
		}

		@Override
		public Object visitUndeclaredType(UndeclaredType undeclaredType, Object arg)
				throws Exception {
			return null;
			}
			

		@Override
		public Object visitSimpleType(SimpleType simpleType, Object arg)
				throws Exception {
			return simpleType.getJVMType();
		}

		@Override
		public Object visitClosure(Closure closure, Object arg) throws Exception {
		return null;
		}

	@Override
	public Object visitClosureDec(ClosureDec closureDeclaration, Object arg)
			throws Exception {
		return null;
	}

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
	public Object visitIfElseStatement(IfElseStatement ifElseStatement,
			Object arg) throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		ifElseStatement.expression.visit(this, arg);
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		ifElseStatement.ifBlock.visit(this,arg);
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		
		mv.visitLabel(l1);
		ifElseStatement.elseBlock.visit(this,arg);
		mv.visitLabel(l2);
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		ifStatement.expression.visit(this, arg);
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		ifStatement.block.visit(this,arg);
		mv.visitLabel(l1);
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

	@Override
	public Object visitListExpression(ListExpression listExpression, Object arg)
			throws Exception {
	/*	MethodVisitor mv = ((InheritedAttributes) arg).mv;
		mv.visitTypeInsn(NEW, "java/util/ArrayList");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
		return null;
		*/
		for(Expression e : listExpression.expressionList)
			e.visit(this, arg);
		return null;
	}

	@Override
	public Object visitListOrMapElemExpression(
			ListOrMapElemExpression listOrMapElemExpression, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitListOrMapElemExpression");
		
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		String ident = listOrMapElemExpression.identToken.getText();
		
		String type = "";
		String signature = "";
		if(listOrMapElemExpression.getType().contains("List"))
		{
			String wholeDesc = listOrMapElemExpression.getType();

			if(wholeDesc.contains("I"))
			{
				char[] l = wholeDesc.toCharArray();
				int i;
				int angleCount = 0;
				for(i = l.length-1;  i >= 0; --i)
				{
					if(l[i] == 'I')
						break;
					else
						++angleCount;
				}
				type = new String(l, 1, i-angleCount);
				signature = wholeDesc.replace("I", "Ljava/lang/Integer;");
			}
			else if(wholeDesc.contains("Z"))
			{
				char[] l = wholeDesc.toCharArray();
				int i;
				int angleCount = 0;
				for(i = l.length-1; i >= 0; --i)
				{
					if(l[i] == 'I')
						break;
					else
						++angleCount;
				}
				type = new String(l, 1, i-angleCount);
				signature = wholeDesc.replace("Z", "Ljava/lang/Boolean;");
			}
			else
			{
				//to be implemented
			}
			//System.out.println("in codeGenVisitor.java: visitListOrMapElemExpression, " + ident + ", " + wholeDesc +", " + type + ", "+signature);
		}
		else if(listOrMapElemExpression.getType().contains("Hash"))
		{}
		
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, ident, "L"+type+";");
		listOrMapElemExpression.expression.visit(this, arg);
		mv.visitMethodInsn(INVOKEINTERFACE, type, "get", "(I)Ljava/lang/Object;", true);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		return null;
	}


	@Override
	public Object visitListType(ListType listType, Object arg) throws Exception {
		return listType.getJVMType();
		
	}

	@Override
	public Object visitMapListExpression(MapListExpression mapListExpression,
			Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitRangeExpression(RangeExpression rangeExpression,
			Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitSizeExpression(SizeExpression sizeExpression, Object arg)
			throws Exception {
	/*	MethodVisitor mv = ((InheritedAttributes) arg).mv;
		String type = "()" + sizeExpression.expression.getType();
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", type, true);
		return null;
		*/
//System.out.println("in CodeGenVisitor.java: visitSizeExpression");
			MethodVisitor mv = ((InheritedAttributes) arg).mv;
			String owner = "";
			if(sizeExpression.expression.getType().contains("List"))
				owner = "java/util/List";
			sizeExpression.expression.visit(this, arg);
			
			mv.visitMethodInsn(INVOKEINTERFACE, owner, "size", "()I", true);
			sizeExpression.setType(intType);
			return intType;
		}


	@Override
	public Object visitValueExpression(ValueExpression valueExpression,
			Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitWhileRangeStatement(
			WhileRangeStatement whileRangeStatement, Object arg)
			throws Exception {
		return null;
	}

	@Override
	public Object visitWhileStarStatement(WhileStarStatement whileStarStatment,
			Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg)
			throws Exception {
		/*MethodVisitor mv = ((InheritedAttributes) arg).mv;
		Label l1 = new Label();
		mv.visitJumpInsn(GOTO, l1);
		whileStatement.block.visit(this, arg);
		Label l2 = new Label();
		whileStatement.expression.visit(this, arg);
		mv.visitJumpInsn(IFNE, l2);
		mv.visitLabel(l1);
		mv.visitLabel(l2);
		return null;
		*/
MethodVisitor mv = ((InheritedAttributes) arg).mv; 
		
		Label l1 = new Label();
		mv.visitJumpInsn(GOTO, l1);
		Label l2 = new Label();
		
		mv.visitLabel(l2);
		whileStatement.block.visit(this, arg);
		
		mv.visitLabel(l1);
		whileStatement.expression.visit(this, arg);
		mv.visitJumpInsn(IFNE, l2);
		return null;
		
		
	}

	
}
