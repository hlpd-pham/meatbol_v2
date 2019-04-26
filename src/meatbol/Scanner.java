package meatbol;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * Scanner class
 * Provides access to current token and next token
 * If statements can span multiple source text lines, this approach
 * hide the text line details from the parser since the parser doesn't need to understand them
 */
public class Scanner{

    /** token terminator
     */
    public final static String delimiters = " \t;:()\'\"=!<>+-*/[]#,^\n";
    /** operators in Meatbol
     */
    public final static String operators = "+-*/<>=!^#";
    /** separators in Meatbol
     */
    public final static String separators = "(),:;[]";
    /** EOF returned code
     */
    public final static int END_OF_FILE = 101;
    /** continue to scan file returned code
     */
    public final static int CONTINUE_FILE = 102;
    /** source file name
     */
    public String sourceFileNm = "";
    /** array list of source code text lines
     */
    public ArrayList<String> sourceLineM;
    /** object responsible for providing symbol definition
     */
    public SymbolTable symbolTable = new SymbolTable();
    /** char[] for current text line
     */
    public char[] textCharM;
    /** Line Number in sourceLineM for current text line
     */
    public int iSourceLineNr;
    /** Column Position within the current text line
     */
    public int iColPos;
    /** The token established with the most recent call to getNext().
     */
    public Token currentToken;
    /** The token following the currentToken.
     */
    public Token nextToken;
    /** Hash map for token precedence
     */
    public static HashMap<String,Integer> hToken = new HashMap<String, Integer>();
    /** Hash map for instack precedence
     */
    public static HashMap<String,Integer> hStack = new HashMap<String, Integer>();
    /** The showtoken for debugger
     */
    public boolean bShowToken;
    /**ShowStmt
     */
    public boolean bShowStmt;

    /**
     * Scanner constructor: read in a source file name and a SymbolTable (currently a dummy class)
     * - Assign each line of the file as a string to attribute sourceLineM
     * - Initialize textCharM by first element of sourceLineM
     * - Initialize iColPos, iSourceLineNr
     * - Initialize currentToken and nextToken to new Token object
     * - Get the first token into nextToken
     * <p>
     *
     * @param sourceFileNm
     * @param symbolTable
     * @throws Exception
     */
    public Scanner(String sourceFileNm, SymbolTable symbolTable) throws Exception {

        // save sourceFileNm and symbolTable
        this.sourceFileNm = sourceFileNm;
        this.symbolTable = symbolTable;

        // read specified source file and populate sourceLineM
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sourceFileNm));
            String line;
            ArrayList<String> sourceLines = new ArrayList<>();
            while ( (line = reader.readLine()) != null ) {
                sourceLines.add(line);
            }
            this.sourceLineM = sourceLines;

            // initialize textCharM
            this.textCharM = sourceLines.get(0).toCharArray();

            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // initialize iSourceLineNr and iColPos
        this.iSourceLineNr = 1;
        this.iColPos = 0;

        // initialize currentToken and nextToken to new objects
        this.currentToken = new Token();
        this.nextToken = new Token();

        // initialize the precedence
        initializePrecedence();
        // default bShowToken to False
        bShowToken = false;
        //Default bShowStmt is false
        bShowStmt = false;
        // get the first token into nextToken
        getNext();

        // initialize precedence
        initializePrecedence();

    } // END OF CONSTRUCTOR

    /**
     * - Provides value for current token: tokenStr, primClassif, subClassif
     * - Sets tokenStr appropriately
     * - Automatically skips white space (blanks, tabs, \n) which isn't within
     *   a string literal; therefore, automatically skips blank lines
     * - Automatically advances to next line of input (and prints that line
     *   with its line nr (relative to 1)).
     * <p>
     * The returned value tokenStr of currentToken
     *
     * @return              a string for the next token
     * @throws Exception    if the token is an invalid numeric constant
     *                      or string literal not terminated
     */
    public String getNext() throws Exception {


        // if scanner has reached the end of file
        if (currentToken.primClassif == Classif.EOF) {
            return "";
        }

        // set currentToken to nextToken
        currentToken = nextToken;
        nextToken = new Token();

        // determine if next getNext() call will get to the end of file
        // and set primClassif of nextToken to EOF
        int endOfFile = skipWhiteSpace();
        if (endOfFile == END_OF_FILE) {
            nextToken.primClassif = Classif.EOF;
        }
        // otherwise, parse nextToken
        else {
            int startToken = iColPos;                           // starting index of token in current textCharM
            int i;                                              // variable for looping

            // loop and check if scanner encounters a delimiter
            for (i = iColPos; i < textCharM.length; i++) {
                if (delimiters.indexOf(textCharM[i]) != -1)
                    break;
            }

            int endToken = i;                                     // ending index of token in current textCharM

            // if a token starts and ends at the same position, it is a delimiter
            if (startToken == endToken)
                handleDelimiter();
                // otherwise, it is an operand
            else
                handleOperand();

            // assign position where the token starts in current textCharM
            nextToken.iColPos = startToken;
            // assign line number of token
            nextToken.iSourceLineNr = iSourceLineNr;
        }
        if (bShowToken)
        {
            System.out.printf("Token '%s' LineNr: %d Precedence: %d\n", currentToken.tokenStr,
                    currentToken.iSourceLineNr, currentToken.precedence);
        }
//        if(currentToken.primClassif == Classif.EOF)
//        {
//            System.out.printf("Program finsihed..\n");
//            System.exit(0);
//        }

        return currentToken.tokenStr;
    }// END OF GET NEXT

    /**
     * - Automatically skips white space (blanks, tabs, \n) which isn't within
     * a string literal; therefore, automatically skips blank lines
     *
     * <p>
     * The returned value is an int:
     *  - 101 if the scanner has not reached the end of file.
     *  - 102 if the scanner has reached the end of file
     *
     * @return result   an integer specifies if scanner has reached the end of file
     */
    private int skipWhiteSpace() {
        int result = CONTINUE_FILE;

        // prints out the first line of the file
//        if (iColPos == 0 && iSourceLineNr == 1)
//            System.out.printf(" %d %s\n", iSourceLineNr, sourceLineM.get(iSourceLineNr-1));

        // Advance to next line if:
        // - Column position runs out of bound of current textCharM
        if (iColPos == textCharM.length ) {
            result = advanceLine();
            // if end of file
            if (result == END_OF_FILE)
                return result;
        }

        // if a line is empty, or token is a space (literal space character, tab, new line),
        // or a comment indicator '//' is detected
        // then advances to next line
        while ( textCharM.length == 0 || textCharM[iColPos] == ' ' ||
                textCharM[iColPos] == '\t' || textCharM[iColPos] == '\n' ||
                (textCharM[iColPos] == '/' && textCharM[iColPos+1] == '/') ) {

            // skip to next line if a comment is met
            if ( iColPos < textCharM.length && (textCharM[iColPos] == '/' && textCharM[iColPos+1] == '/')) {
                result = advanceLine();
                // if end of file
                if (result == END_OF_FILE)
                    return result;
            }
            else {
                iColPos++;

                // Advance to next line if:
                // - Column position runs out of bound of current textCharM
                // - The line is empty (textCharM.length == 0)
                if ( iColPos == textCharM.length || textCharM.length == 0 ) {
                    result = advanceLine();
                    // if end of file
                    if (result == END_OF_FILE)
                        return result;
                }

            }
        }

        return result;
    }//END OF SKIP WHITE SPACE

    /**
     * Resets column position and reassigns textCharM to the next
     * string in sourceLineM
     * <p>
     * The returned value is an int:
     *  - 101 if the scanner has not reached the end of file.
     *  - 102 if the scanner has reached the end of file
     *
     * @return result   an integer specifies if scanner has reached the end of file
     */
    private int advanceLine() {

        int result = CONTINUE_FILE;                                 // return result

        // there is no more line to advance --> end of file
        if (iSourceLineNr == sourceLineM.size())
            return END_OF_FILE;

        // reset column position
        iColPos = 0;
        // advance line number
        iSourceLineNr++;
        // advance textCharM to next string in sourcelineM
        textCharM = sourceLineM.get(iSourceLineNr-1).toCharArray();
//        // whenever a new line is advanced to, print it out
        if(bShowStmt)
            System.out.printf(" %d %s\n", iSourceLineNr, sourceLineM.get(iSourceLineNr-1));

        return result;
    }// END OF ADVANCE LINE

    /**
     * Distinguishes operators to separators and assign tokenStr, primClassif, and
     * subClassif to nextToken. if character at the current position is
     * a string delimiter. It reads until the string delimiter is met.
     * If it does not, an exception is thrown
     * <p>
     *
     * @throws Exception    if the token is a string literal not terminated
     */
    private void handleDelimiter() throws Exception{

        // token is an operator
        if (operators.indexOf(textCharM[iColPos]) != -1) {

            nextToken.primClassif = Classif.OPERATOR;
            nextToken.subClassif = SubClassif.EMPTY;

            // check for combine 2 characters operators, else tokenStr is just the current operator
            if (iColPos < (textCharM.length-1) && textCharM[iColPos+1] == '=') {
                nextToken.tokenStr = ("" + textCharM[iColPos] + textCharM[iColPos+1]);
                // assign sub classification for token string on next token
                operatorClassification(nextToken.tokenStr);
                iColPos += 2;
                return;
            }
            else
            {
                nextToken.tokenStr = (""+textCharM[iColPos]);
                // assign sub classification for token string on next token
                operatorClassification(nextToken.tokenStr);
                iColPos++;
                return;
            }



        }
        // token is a separator
        else if (separators.indexOf(textCharM[iColPos]) != -1) {
            nextToken.primClassif = Classif.SEPARATOR;
            nextToken.subClassif = SubClassif.EMPTY;

            // if token is a parenthesis, assign stack and expression precedences
            if (textCharM[iColPos] == '(') {
                nextToken.precedence = hToken.get(String.valueOf(textCharM[iColPos]));
                nextToken.stackPrecedence = hStack.get(String.valueOf(textCharM[iColPos]));
            }
        }
        // token is a string delimiter
        else {

            try {
                // set attributes for nextToken
                nextToken.primClassif = Classif.OPERAND;
                nextToken.subClassif = SubClassif.STRING;
                nextToken.idenClassif = IdenClassif.PRIMITIVE;

                // initialize string delimiter
                char stringDelimiter = textCharM[iColPos];
                // advance cursor
                iColPos++;
                // get the string literal
                while (textCharM[iColPos] != stringDelimiter) {

                    // if the user is escaping the string delimiter, append the backslash
                    // character and the string delimiter to the string literal
                    if (textCharM[iColPos] == '\\') {

                        switch (textCharM[iColPos+1]) {
                            case '\'':
                                textCharM[iColPos] = '\'';
                                nextToken.tokenStr += String.valueOf(textCharM, iColPos, 1);
                                iColPos += 2;
                                break;
                            case '"':
                                textCharM[iColPos] = '"';
                                nextToken.tokenStr += String.valueOf(textCharM, iColPos, 1);
                                iColPos += 2;
                                break;
                            case '\\':
                                textCharM[iColPos] = '\\';
                                nextToken.tokenStr += String.valueOf(textCharM, iColPos, 1);
                                iColPos += 2;
                                break;
                            case 'n':
                                textCharM[iColPos] = 0x0A;
                                nextToken.tokenStr += String.valueOf(textCharM, iColPos, 1);
                                iColPos += 2;
                                break;
                            case 't':
                                textCharM[iColPos] = 0x09;
                                nextToken.tokenStr += String.valueOf(textCharM, iColPos, 1);
                                iColPos += 2;
                                break;
                            case 'a':
                                textCharM[iColPos] = 0x07;
                                nextToken.tokenStr += String.valueOf(textCharM, iColPos, 1);
                                iColPos += 2;
                                break;
                            default:
                                nextToken.tokenStr += textCharM[iColPos];
                                iColPos += 1;
                        }
                    }
                    // otherwise, append the token to the string literal
                    else {
                        nextToken.tokenStr += textCharM[iColPos];
                        iColPos++;
                    }
                }

                // advance iColPos for next getNext() call
                iColPos++;

                return;
            }
            // if the user does not terminate the string literal in the same line
            catch (ArrayIndexOutOfBoundsException e) {
                String message = String.format("Line %d String literal not terminated: '%s', File: %s",
                        iSourceLineNr, nextToken.tokenStr, sourceFileNm);

                throw new Exception(message);
            }
        }

        // assign tokenStr attribute for nextToken and advance iColPos for next getNext() call
        nextToken.tokenStr += textCharM[iColPos];
        iColPos++;
    }// END OF HANDLE DELIMITER

    /**
     * Distinguishes different types of operands: integer, float or identifier
     * and assigns tokenStr, primClassif, and subClassif to nextToken
     * <p>
     *
     * @throws Exception    if the token is an invalid numeric constant
     */
    private void handleOperand() throws Exception {
        nextToken.primClassif = Classif.OPERAND;

        // get the token
        while (iColPos < textCharM.length && delimiters.indexOf(textCharM[iColPos]) == -1) {
            nextToken.tokenStr += textCharM[iColPos];
            iColPos++;
        }

        // if token string starts with a number, it's numeric value
        if (Character.isDigit(nextToken.tokenStr.toCharArray()[0])) {

            try {
                Integer.parseInt(nextToken.tokenStr);
                nextToken.subClassif = SubClassif.INTEGER;
            }
            catch (Exception e) {
                try {
                    Double.parseDouble(nextToken.tokenStr);
                    nextToken.subClassif = SubClassif.FLOAT;
                }
                catch (Exception e1) {
                    String message = String.format("Line %d Invalid numeric constant: '%s', File: %s",
                            iSourceLineNr, nextToken.tokenStr, sourceFileNm);
                    throw new Exception(message);
                }
            }

            // numerics are all primitive
            nextToken.idenClassif = IdenClassif.PRIMITIVE;
        }
        // otherwise, it is an identifier
        else
        {
            // check if token is a Meatbol symbol
            STEntry mbSymbol = symbolTable.getSymbol(nextToken.tokenStr);
            // if mbSymbol is null, the token is not a symbol
            if (mbSymbol == null) {

                // move cursor to the next token to find possible bracket
                // for array
                skipWhiteSpace();

                // store the old column position so we can reset it
                // if the token is a fixed array
                int oldColPos = iColPos;

                // array token
                if (textCharM[iColPos] == '[') {

                    // determine fixed or unbound array
                    iColPos++;
                    skipWhiteSpace();

                    // show the array identfier with a "[" attached.
                    nextToken.tokenStr += "[";

                    // look up next token if it is 'unbound' keyword
                    // and set IdenClassif for the token
                    char[] subsetM = Arrays.copyOfRange(textCharM, iColPos, (iColPos + 6) );
                    String subsetStr = new String(subsetM);

                    // we differentiate if the token is unbound or fixed array by the
                    // keyword unbound
                    if (subsetStr.equals("unbound")) {
                        nextToken.idenClassif = IdenClassif.UNBOUND_ARRAY;
                    }
                    else
                        nextToken.idenClassif = IdenClassif.FIXED_ARRAY;

                    hToken.put(nextToken.tokenStr, 16);
                    hStack.put(nextToken.tokenStr, 0);

                    // we have to reset column position
                    // so we can evaluate whatever between the brackets
                    iColPos = oldColPos;
                }
                // primitive token
                else {
                    nextToken.idenClassif = IdenClassif.PRIMITIVE;
                }

                // set the subClassification to Identifier, because both
                // primitive and array are identifier (which are stored
                // in storageMgr)
                nextToken.subClassif = SubClassif.IDENTIFIER;
            }
            // otherwise, the token is a Meatbol symbol
            else {
                nextToken.primClassif = mbSymbol.primClassif;

                if (mbSymbol instanceof STControl)  {
                    nextToken.subClassif = ((STControl) mbSymbol).subClassif;
                }
                else if (mbSymbol instanceof STFunction) {
                    nextToken.subClassif = ((STFunction) mbSymbol).definedBy;
                }
                else {
                    nextToken.subClassif = SubClassif.IDENTIFIER;
                }
            }
        }

    }// END OF HANDLE OPERAND

    /**
     * Distinguishes different types of operator: +, -, *, /, =, >, <, ^, unary minus
     * <p>
     * @param op    -   The operator that needed to be classify
     */
    public void operatorClassification(String op)
    {

        switch (op){
            case "+":
                nextToken.subClassif = SubClassif.PLUS;
                break;
            case "-":
                if (unaryMinus())
                    nextToken.subClassif = SubClassif.UNARYMINUS;
                else
                    nextToken.subClassif = SubClassif.MINUS;
                break;
            case "*":
                nextToken.subClassif = SubClassif.MULTIPLY;
                break;
            case "/":
                nextToken.subClassif = SubClassif.DIVIDE;
                break;
            case "=":
                nextToken.subClassif = SubClassif.ASSIGN;
                break;
            case ">":
                nextToken.subClassif = SubClassif.LARGER;
                break;
            case "<":
                nextToken.subClassif = SubClassif.SMALLER;
                break;
            case "^":
                nextToken.subClassif = SubClassif.EXPO;
                break;
            case "==":
                nextToken.subClassif = SubClassif.EQUI;
                break;
            case "!=":
                nextToken.subClassif = SubClassif.NEQUI;
                break;
            case ">=":
                nextToken.subClassif = SubClassif.LEQUAL;
                break;
            case "<=":
                nextToken.subClassif = SubClassif.SEQUAL;
                break;
            case "+=":
                nextToken.subClassif = SubClassif.PEQUAL;
                break;
            case "-=":
                nextToken.subClassif = SubClassif.MEQUAL;
                break;
        }
        // assign stack precedence and expression precedence
        if (!"=-=+=".contains(op)) {
            nextToken.precedence = hToken.get(nextToken.tokenStr);
            nextToken.stackPrecedence = hStack.get(nextToken.tokenStr);
        }
    }// END OF OPERATOR CLASSIFICATION

    /**
     * Identify if a - is a unary or binary minus
     * Things that can precede a unary minus
     * 1. assignment operators: =, -=, +=
     * 2. binary operators:     +, -, *, /, ^, <, >, <=, >=, !=, #, and, or
     * 3. separator: (,[, ‘,’
     * 4. unary operator: unary minus and NOT
     * 5. control symbol: select while if when1. assignment operators: =, -=, +=
     *
     * @return  true     -   when before the minus sign are those symbol
     *          false    -   when it is not
     */
    public boolean unaryMinus(){
        // the list that contains all cases that can be used to detect as U-
        String precedingList= "+-*/^#=><>=<=-=+=!=([,andornot==";
        // if prceding the minus is one of these then
        if (precedingList.contains(currentToken.tokenStr))
            return true;
        else
            return false;
    }// END OF UNARY MINUS DETECTOR

    /**
     * Initialized the hash map for token and instack precedence
     * <p>
     */
    public void initializePrecedence(){
        hToken.put("(",15);
        hStack.put("(",2);

        hToken.put("u-",12);
        hStack.put("u-",12);

        hToken.put("^",11);
        hStack.put("^",10);

        hToken.put("*",9);
        hStack.put("*",9);
        hToken.put("/",9);
        hStack.put("/",9);

        hToken.put("+",8);
        hStack.put("+",8);
        hToken.put("-",8);
        hStack.put("-",8);

        hToken.put("#",7);
        hStack.put("#",7);

        hToken.put(">",6);
        hStack.put(">",6);
        hToken.put(">=",6);
        hStack.put(">=",6);
        hToken.put("<",6);
        hStack.put("<",6);
        hToken.put("<=",6);
        hStack.put("<=",6);
        hToken.put("==",6);
        hStack.put("==",6);
        hToken.put("!=",6);
        hStack.put("!=",6);
        hToken.put("in",6);
        hStack.put("in",6);
        hToken.put("notin",6);
        hStack.put("notin",6);

        hToken.put("not",5);
        hStack.put("not",5);

        hToken.put("and",4);
        hStack.put("and",4);
        hToken.put("or",4);
        hStack.put("or",4);

        hToken.put("LENGTH",16);
        hStack.put("LENGTH",2);
        hToken.put("ELEM",16);
        hStack.put("ELEM",2);
        hToken.put("MAXELEM",16);
        hStack.put("MAXELEM",2);
        hToken.put("SPACES",16);
        hStack.put("SPACES",2);
    }
}


