package cop5555sp15;
/**
 * This example illustrates a class called EvaluateBooleanExpression
 * which evaluates the value of a boolean expression 
 * only when the value of the integer variable a is greater than 0.
 * Otherwise it prints 0. The expected output is
 0
 0
 5you
 (!!!false & !!true | !true) evaluates to true
 0
 */
public class Example1 {
	public static void main(String[] args) throws Exception{
				
		String source = "class EvaluateBooleanExpression{\n"
				+ "def a: int;  \n if(a != 0){"
				+ "if (!!!false & !!true | !true){print \"(!!!false & !!true | !true) evaluates to true\";}\n"
				+ "else {print \"(!!!false & !!true | !true) evaluates to false\";};\n"
				+ "} else {print a;};"
				+ "}";
				Codelet codelet = CodeletBuilder.newInstance(source);
				codelet.execute();
				int a = CodeletBuilder.getInt(codelet,  "a");
				System.out.println(a);
				CodeletBuilder.setInt(codelet, "a", 5);
				System.out.println(CodeletBuilder.getInt(codelet,  "a"));
				codelet.execute();
				CodeletBuilder.setInt(codelet, "a", 0);
				codelet.execute();

	
		}
}
