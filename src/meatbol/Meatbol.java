/*
  This is a simple driver for the first programming assignment.
  Command Arguments:
      java Meatbol arg1
             arg1 is the meatbol source file name.
  Output:
      Prints each token in a table.
  Notes:
      1. This creates a SymbolTable object which doesn't do anything
         for this first programming assignment.
      2. This uses the student's Scanner class to get each token from
         the input file.  It uses the getNext method until it returns
         an empty string.
      3. If the Scanner raises an exception, this driver prints 
         information about the exception and terminates.
      4. The token is printed using the Token::printToken() method.
 */
package meatbol;

public class Meatbol
{
    public static void main(String[] args)
    {
        try
        {
            Parser parser = new Parser(args[0]);
            parser.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}