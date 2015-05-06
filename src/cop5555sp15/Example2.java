package cop5555sp15;
/**
 * This example illustrates division of two integers.
 * Initially the value of the two integers variables a and b are 0, so nothing is done
 * as division by 0 is not allowed.
 * Then the value of the integer variables are modified in the codelet and set to non-zero values.
 * Then the program prints the quotient produced by the division of those two integer variables.  
 * The expected output is
Value of b is 0. 
Division by ZERO is not allowed.
---------------------------------------
Current Value of a: 0
Current Value of b: 0
Updated value of a: 100
Updated Value of b: 25
---------------------------------------
Thank You! 
Now I can do division of two numbers:

100
/ 
25
= 
4

 */
public class Example2 {
	public static void main(String[] args) throws Exception{
		String source = "class DivisionOfTwoNumbers{\n"
		+ "def a: int; \n def b: int;\n"
		+ "if (b == 0){print \"Value of b is 0. \nDivision by ZERO is not allowed."
		+ "\n---------------------------------------\";}\n"
		+ "else {print \"---------------------------------------\"; "
		+ "print \"Thank You! \nNow I can do division of two numbers:\n\"; "
		+ "print a ; print \"/ \";print b ; print \"= \" ; print  a / b;};\n"
		+ "}";
		Codelet codelet = CodeletBuilder.newInstance(source);
		codelet.execute();
		int a = CodeletBuilder.getInt(codelet,  "a");
		int b = CodeletBuilder.getInt(codelet,  "b");
		System.out.println("Current Value of a: " +a);
		System.out.println("Current Value of b: " + b);
		CodeletBuilder.setInt(codelet, "a", 100);
		CodeletBuilder.setInt(codelet, "b", 25);
		System.out.println("Updated value of a: " + CodeletBuilder.getInt(codelet,  "a"));
		System.out.println("Updated Value of b: " + CodeletBuilder.getInt(codelet,  "b"));
		codelet.execute();
		}
}
