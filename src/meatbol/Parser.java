package meatbol;

import java.awt.image.AreaAveragingScaleFilter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    /** Source File Name
     */
    public String sourceFileNm = "";
    /** Symboltable
     */
    public SymbolTable st = new SymbolTable();
    /** Scanner
     */
    public Scanner scan;
    /** Line Numer used in whiteStmt and forStmt
     */
    public int iLineNr;
    /** Assignment operators
     */
    private final static String assignmentOps = "=+=-=";
    /** Storage Manager
     */
    public StorageManager storageMgr = new StorageManager();
    /**Debugger object
     */
    public Debugger debug;

    public int counter = 0;

    /**
     *Parser constructor
     *@param sourceFileNm       source file name
     *@throws Exception         error thrown from scanner
     */
    public Parser(String sourceFileNm) throws Exception
    {
        this.scan = new Scanner(sourceFileNm, st);
        this.iLineNr = this.scan.iSourceLineNr;
        this.debug = new Debugger(false, false, false, false);
    } // END constructor

    /**
     *Assign new value variableStr for variable res
     * keep the declared type of res
     *<p>
     *return type is a ResultValue
     *
     * @param variableStr   name of variable
     * @param res           destination ResultValue
     * @return              replaced ResultValue from storageMgr
     */
    private ResultValue assign(String variableStr, ResultValue res) throws Exception
    {
        return storageMgr.replace(this, variableStr, res);
    } // END assign

    /**
     *
     *     Assumptions: Given by Professor Clark
     *     1. currentToken is on the variable.
     *     2. We have an expr() subroutine which parses an expr and returns the result (i.e. value) for that expression.
     *     3. We are not yet handling subscripts or slices.
     *     4. We have a constructor  function which converts a result object into a
     *          Numeric object.
     *     Returns:
     *         ResultValue which is what was assigned to the target.
     *     Notes:
     *     1. RHS of assignment operator is an expression.  We will call that result operand 2.
     *     2. LHS of assignment is the target variable.  For "+=" and "-=" operations,
     *                  it is also the first operand of the "+" or "-".
     *
     * @param bExec         boolean value to show if we are going to execute
     *                      the next statements
     * @return              ResultValue of assigned variable
     * @throws Exception    ParserException (read code below for more details)
     */
    ResultValue assignmentStmt(Boolean bExec) throws Exception
    {
        // returning value
        ResultValue res = new ResultValue();

        if (!bExec) {
            skipTo(";");
            res.type = SubClassif.GOOD;
            return res;
        }

        if (scan.currentToken.subClassif != SubClassif.IDENTIFIER) {
            error("expected a variable for the target of an assignment");
        }

        // variable String
        String variableStr = scan.currentToken.tokenStr;

        // variable Token
        Token variableToken = scan.currentToken;

        ResultValue arrayIdentifier = new ResultValue();

        // if token doesn't contain an open bracket, it might still
        // reference an array
        if (!variableStr.contains("["))
            arrayIdentifier = storageMgr.getVariable(this, variableStr);
            // otherwise, it might be referencing an array or a string
        else {
            try
            {
                // if array variable can't be found in storage manager,
                // it might be a string
                storageMgr.getArray(this, variableStr);
            }
            catch (ParserException e)
            {
                // accesing string index
                variableStr = variableStr.replaceAll("\\[", "");
                ResultValue stringIdentifier = storageMgr.getVariable(this, variableStr);
                if (stringIdentifier.type != SubClassif.STRING)
                    error("Invalid argument for accessing indices '%s'", variableStr);

                // move cursor pass the open bracket
                scan.getNext();
                // move cursor to the beginning of the expression for accessing index
                scan.getNext();

                // Get array index reference
                ResultValue arrayIndex = expr("]", false);

                // access array at index
                if (arrayIndex.type != SubClassif.ARRAY_SLICE &&
                        !arrayIndex.terminatingStr.equals("~"))
                {
                    // array index
                    arrayIndex = Utility.toInt(this, arrayIndex);

                    // get array index
                    int iArrayIndex = Integer.parseInt(arrayIndex.value);

                    // target subscript cannot be < 0
                    if (iArrayIndex < 0)
                        error("Target subsript cannot be lower than 0. Found '%d'", iArrayIndex);

                    // Boundary check
                    if (iArrayIndex > stringIdentifier.value.length())
                        error("Index out of bound: '%s'", iArrayIndex);

                    if (!scan.getNext().equals("="))
                        error("Expected '=' for string element assignment. Found '%s'",
                                scan.currentToken.tokenStr);

                    // move cursor pass assignment operator
                    scan.getNext();

                    ResultValue res01;
                    res01 = expr(";", true);
                    if (res01.type != SubClassif.STRING)
                        error("Result of expression must be type string");

                    // building new string
                    StringBuilder newStringVal = new StringBuilder(stringIdentifier.value);
                    for (int i = 0; i < res01.value.length(); i++) {
                        if (iArrayIndex >= stringIdentifier.value.length())
                            newStringVal.append(res01.value.charAt(i));
                        else
                            newStringVal.setCharAt(iArrayIndex, res01.value.charAt(i));
                        iArrayIndex++;
                    }
                    stringIdentifier.value = newStringVal.toString();
                    storageMgr.replace(this, variableStr, stringIdentifier);
                }
                // slicing of string
                else {
                    String arraySlice = arrayIndex.value;

                    if (arraySlice.length() == 2)
                    {
                        int iSubscript = Integer.parseInt(arraySlice.replaceAll("~",""));
                        String idenValue = stringIdentifier.value;

                        // slicing doesn't support negative subscript
                        if (iSubscript < 0)
                            error("Negative subscript in slicing");

                        // boundary check
                        if (iSubscript >= idenValue.length())
                            error("Index out of bound '%d'", iSubscript);

                        // case 1: '~op1'
                        if (arraySlice.indexOf("~") == 0)
                        {
                            // keep the end part of the string
                            String keep = idenValue.substring(iSubscript);

                            if (!scan.getNext().equals("="))
                                error("Expected '=' for string element assignment. Found '%s'",
                                        scan.currentToken.tokenStr);

                            // move cursor pass assignment operator
                            scan.getNext();

                            // evaluate
                            ResultValue res01;
                            res01 = expr(";", true);
                            if (res01.type != SubClassif.STRING)
                                error("Result of expression must be type string");

                            // append the expression result to the part we kept before
                            res01.value += keep;
                            stringIdentifier.value = res01.value;
                            storageMgr.replace(this, variableStr, stringIdentifier);
                        }
                        // case 2: 'op1~'
                        else {
                            // keep from index zero to subscript
                            String keep = idenValue.substring(0, iSubscript);

                            if (!scan.getNext().equals("="))
                                error("Expected '=' for string element assignment. Found '%s'",
                                        scan.currentToken.tokenStr);

                            // move cursor pass assignment operator
                            scan.getNext();

                            // evaluate
                            ResultValue res01;
                            res01 = expr(";", true);
                            if (res01.type != SubClassif.STRING)
                                error("Result of expression must be type string");

                            // append the expression result to the part we kept before
                            res01.value = keep + res01.value;
                            stringIdentifier.value = res01.value;
                            storageMgr.replace(this, variableStr, stringIdentifier);
                        }
                    }
                    // case 3: 'op1~op2'
                    else if (arraySlice.length() == 3)
                    {
                        String[] parts = arraySlice.split("~");
                        int iStartSlicing = Integer.parseInt(parts[0]);
                        int iEndSlicing = Integer.parseInt(parts[1]);
                        String sbIdentifierVal = stringIdentifier.value;

                        // subscript check
                        if (iStartSlicing > iEndSlicing)
                            error("Array error: starting subscript cannot be smaller that ending subscript");

                        // boundary check
                        if (iEndSlicing > (sbIdentifierVal.length()-1) ||
                                iStartSlicing > (sbIdentifierVal.length()-1) )
                            error("Array error: Index out of bound");

                        // array slicing doesn't support negative subscript
                        if (iStartSlicing < 0 || iEndSlicing < 0)
                            error("Array error: Index for array slicing cannot be negative");

                        // from index 0 of string to iStartSlicing
                        String startKeep = "";
                        // from index iEndSlicing to the end of string
                        String endKeep = "";
                        // from iStartSlicing to iEndSlicing

                        if (iStartSlicing != 0)
                            startKeep = sbIdentifierVal.substring(0, iStartSlicing);

                        endKeep = sbIdentifierVal.substring(iEndSlicing);

                        if (!scan.getNext().equals("="))
                            error("Expected '=' for string element assignment. Found '%s'",
                                    scan.currentToken.tokenStr);

                        // move cursor pass assignment operator
                        scan.getNext();

                        // evaluate
                        ResultValue res01;
                        res01 = expr(";", true);
                        if (res01.type != SubClassif.STRING)
                            error("Result of expression must be type string");

                        stringIdentifier.value = startKeep + res01.value + endKeep;
                        storageMgr.replace(this, variableStr, stringIdentifier);
                    }
                    else
                        error("Error in string slicing assignment. Found '%s'", arraySlice);
                }

                return stringIdentifier;
            }
        }


        // if the identifier is just a regular variable, go to assignment for
        // regular variables
        if (arrayIdentifier.structure != IdenClassif.UNBOUND_ARRAY &&
                arrayIdentifier.structure != IdenClassif.FIXED_ARRAY)
        {
            // other type of assignments (token is not an array identifier)
            // - assignment for array element
            // - array to array assignment (source is a slice)
            if ((variableToken.idenClassif == IdenClassif.UNBOUND_ARRAY ||
                    variableToken.idenClassif == IdenClassif.FIXED_ARRAY) &&
                    variableStr.contains("[")) {

                // move cursor pass the open bracket
                scan.getNext();
                // move cursor to the beginning of the expression for accessing index
                scan.getNext();

                // Get array index reference
                ResultValue arrayIndex = expr("]", false);

                // assignment for array element
                if (arrayIndex.type != SubClassif.ARRAY_SLICE &&
                        !arrayIndex.terminatingStr.equals("~"))
                {
                    // array index
                    arrayIndex = Utility.toInt(this, arrayIndex);

                    // get array index
                    int iArrayIndex = Integer.parseInt(arrayIndex.value);

                    // target subsript cannot be < 0
                    if (iArrayIndex < 0)
                        error("Target subsript cannot be lower than 0. Found '%d'", iArrayIndex);

                    // get array from token string
                    ArrayList<ResultValue> arrayResM = storageMgr.getArray(this, variableToken.tokenStr);

                    // Boundary check
                    try {
                        // if array is unbound, out of bound exception will be
                        // automatically thrown
                        if (variableToken.idenClassif == IdenClassif.UNBOUND_ARRAY)
                            res = arrayResM.get(iArrayIndex);
                            // with fixed array, we have to check
                        else {
                            // get size of array
                            int maxArraySize = storageMgr.arrayMaxSize(this, variableStr);
                            // if index is larger than array's size --> raise exception
                            if (iArrayIndex > maxArraySize)
                                throw new IndexOutOfBoundsException();
                            // otherwise, we have to populate empty ResultValues until the iArrayIndex
                            // position in the array
                            int allocatedLength = storageMgr.getAllocatedLength(this, variableStr);
                            if (allocatedLength < iArrayIndex) {
                                for (int i = allocatedLength; i <= iArrayIndex; i++)
                                    arrayResM.add(i, new ResultValue());
                            }
                        }
                    }
                    catch (IndexOutOfBoundsException e) {
                        error("Index out of bound: '%s'", iArrayIndex);
                    }

                    // get the assignment operator and check it
                    scan.getNext();
                    if (scan.currentToken.primClassif != Classif.OPERATOR) {
                        error("expected assignment operator.  Found '%s'", scan.currentToken.tokenStr);
                    }

                    String operatorStr = scan.currentToken.tokenStr;
                    ResultValue resO2;

                    // move cursor pass the assignment operator
                    scan.getNext();

                    switch(operatorStr) {
                        case "=":
                            res = expr(";", false);
                            break;
                        case "-=":
                            // get evaluated value of expression
                            resO2 = expr(";", false);
                            // expression must be numeric, raise exception if not
                            // pass this (the parser) you can know where the line number is
                            if (resO2.type != SubClassif.INTEGER && resO2.type != SubClassif.FLOAT) {
                                error("Expect operand to be numeric type. Found '%s'", scan.currentToken.tokenStr);
                            }
                            res = Utility.subtract(this, res, resO2);
                            break;
                        case "+=":
                            // get evaluated value of expression
                            resO2 = expr(";", false);
                            // expression must be numeric, raise exception if not
                            // pass this (the parser) you can know where the line number is
                            if (resO2.type != SubClassif.INTEGER && resO2.type != SubClassif.FLOAT) {
                                error("Expect operand to be numeric type. Found '%s'", scan.currentToken.tokenStr);
                            }
                            res = Utility.add(this, res, resO2);
                            break;
                        default:
                            error("Invalid assignment operator '%s'", operatorStr);
                            break;
                    }

                    // check result type
                    if (res.structure != IdenClassif.PRIMITIVE)
                        error("Assignment for array index must be from a primitive type");

                    // set new element for array
                    arrayResM.set(iArrayIndex, res);

                    //Debug handling
                    if(debug.bShowAssign)
                        System.out.printf("...Assign result into '%s' is '%s'\n", variableStr, res.value);

                    storageMgr.replaceArray(this, variableToken.tokenStr, arrayResM);
                }
                // array to array assignment with slice
                else
                {
                    // Slice of source
                    ResultValue srcSlice = arrayIndex;

                    // check for assignment operator
                    if (!scan.getNext().equals("="))
                        error("Expect '=' at assignment statement. Found '%s'", scan.currentToken);

                    // NOTE: at this point, cursor is at the identifier carries name of
                    // the source array

                    // move cursor pass the assignment operator
                    scan.getNext();

                    // destination array name
                    String dstArrayName = variableToken.tokenStr;

                    // identifier for src array
                    Token srcArrayToken = scan.currentToken;

                    // result ArrayList
                    ArrayList<ResultValue> resArray = new ArrayList<>();

                    try
                    {
                        // evaluate expression
                        res = expr(";", false);
                        // sliced array assigned to sliced array

                        ResultValue dstStruct = storageMgr.getArrayStructure(this, variableStr);

                        // verify type
                        if (dstStruct.type == SubClassif.INTEGER)
                            res = Utility.toInt(this, res);
                        else if (dstStruct.type == SubClassif.FLOAT)
                            res = Utility.toFloat(this, res);
                        else if (res.type != dstStruct.type)
                            error("Expect assigned value to be type '%s'.  Found type '%s'",
                                    variableToken.subClassif.toString(), res.type.toString());

                        // expression result is primitive type
                        if (res.structure == IdenClassif.PRIMITIVE || res.structure == IdenClassif.EMPTY)
                        {
                            if (srcSlice.value.length() == 2)
                            {
                                int iSubscript = Integer.parseInt(srcSlice.value.replaceAll("~",""));

                                // slicing doesn't support negative subscript
                                if (iSubscript < 0)
                                    error("Negative subscript in slicing");

                                // boundary check
                                if (iSubscript >= storageMgr.arrayMaxSize(this, dstArrayName) &&
                                        storageMgr.arrayMaxSize(this, dstArrayName) != -1)
                                    error("Index out of bound '%d'", iSubscript);

                                // destination array
                                ArrayList<ResultValue> dstArray = storageMgr.getArray(this, dstArrayName);

                                // TODO fix the if-else block below

                                // myGradeM[~1] = gradeM;
                                if (srcSlice.value.indexOf('~') == 0)
                                {
                                    // clear element (or populate empty element until index
                                    for (int i = 0; i < iSubscript; i++)
                                    {
                                        try
                                        {
                                            dstArray.set(i, res);
                                        }
                                        catch (IndexOutOfBoundsException e){
                                            dstArray.add(res);
                                        }
                                    }

                                    resArray = dstArray;
                                }
                                // myGradeM[1~] = gradeM;
                                else {
                                    // array limit
                                    int arrayLimit = 0;

                                    // fixed array
                                    if (dstStruct.structure == IdenClassif.FIXED_ARRAY) {

                                        arrayLimit = storageMgr.arrayMaxSize(this, dstArrayName);

                                    }
                                    // unbound array
                                    else if (dstStruct.structure == IdenClassif.UNBOUND_ARRAY) {
                                        arrayLimit = storageMgr.getAllocatedLength(this, dstArrayName);
                                    }

                                    // check if element exist (or populate empty element until index
                                    for (int i = 0; i < iSubscript; i++)
                                    {
                                        try
                                        {
                                            dstArray.get(i);
                                        }
                                        catch (IndexOutOfBoundsException e){
                                            dstArray.add(new ResultValue());
                                        }
                                    }

                                    // populate array
                                    for (int i = iSubscript; i < arrayLimit; i++) {
                                        try {
                                            dstArray.set(i, res);
                                        }
                                        catch (IndexOutOfBoundsException e){
                                            dstArray.add(res);
                                        }
                                    }

                                    resArray = dstArray;
                                }
                            }
                        }
                        else {
                            System.out.println("Got here");
                        }
                    }
                    catch (ParserException pe)
                    {
                        // whole array assigned to sliced array
                        // Eg: myGradeM[~1] = gradeM;
                        if (!pe.diagnostic.equals("No element access when referencing array"))
                            throw pe;

                        // source array
                        ArrayList<ResultValue> srcArray = storageMgr.getArray(this, srcArrayToken.tokenStr + "[");

                        if (srcSlice.value.length() == 2)
                        {
                            int iSubscript = Integer.parseInt(srcSlice.value.replaceAll("~",""));

                            // slicing doesn't support negative subscript
                            if (iSubscript < 0)
                                error("Negative subscript in slicing");

                            // boundary check
                            if (iSubscript >= storageMgr.arrayMaxSize(this, dstArrayName) &&
                                    storageMgr.arrayMaxSize(this, dstArrayName) != -1)
                                error("Index out of bound '%d'", iSubscript);

                            // name of source array
                            String srcArrayName = srcArrayToken.tokenStr + "[";
                            // structure of source array
                            ResultValue srcStruct = storageMgr.getArrayStructure(this, srcArrayName);

                            // structure of destination array
                            ResultValue dstStruct = storageMgr.getArrayStructure(this, dstArrayName);

                            // verify data type
                            if (srcStruct.type != dstStruct.type)
                                error("Expect assigned value to be type '%s'.  Found type '%s'",
                                        dstStruct.type.toString(), srcStruct.type.toString());

                            // destination array
                            ArrayList<ResultValue> dstArray = storageMgr.getArray(this, dstArrayName);

                            // myGradeM[~1] = gradeM;
                            if (srcSlice.value.indexOf('~') == 0)
                            {
                                // array limit
                                int arrayLimit = 0;

                                // clear element (or populate empty element until index
                                for (int i = 0; i < iSubscript; i++)
                                {
                                    try
                                    {
                                        dstArray.set(i, new ResultValue());
                                    }
                                    catch (IndexOutOfBoundsException e){
                                        dstArray.add(new ResultValue());
                                    }
                                }

                                // If the source array size is larger than the ending subscript,
                                // it is not an error. It simply only copies enough to fill
                                // until the subscript of the destination array
                                if ( srcArray.size() >= iSubscript)
                                    arrayLimit = iSubscript;
                                    // If the source array size is smaller than the ending subscript,
                                    // it only fills up the corresponding elements of the source array
                                else
                                    arrayLimit = srcArray.size();

                                // start replacement
                                for (int i = 0; i < arrayLimit; i++)
                                {
                                    ResultValue srcElement = srcArray.get(i);
                                    dstArray.set(i, srcElement);
                                }

                                resArray = dstArray;
                            }
                            // myGradeM[1~] = gradeM;
                            else {
                                // array limit
                                int arrayLimit = 0;

                                // fixed array
                                if (srcStruct.structure == IdenClassif.FIXED_ARRAY) {
                                    int srcAllocatedSize = storageMgr.getAllocatedLength(this, srcArrayName);
                                    int dstMaxSize = storageMgr.arrayMaxSize(this, dstArrayName);

                                    // If source array is larger than destination array, it is not an error.
                                    // It simply only copies enough to fill the source array
                                    if (srcAllocatedSize >= (dstMaxSize-iSubscript))
                                        arrayLimit = dstMaxSize;

                                        // If source array is smaller than destination array, it only fills up
                                        // the corresponding elements of the source array
                                    else
                                        arrayLimit = srcAllocatedSize;

                                }
                                // unbound array
                                else if (srcStruct.structure == IdenClassif.UNBOUND_ARRAY) {
                                    int srcAllocatedSize = storageMgr.getAllocatedLength(this, srcArrayName);
                                    int dstAllocatedSize = storageMgr.getAllocatedLength(this, dstArrayName);

                                    // If source array is larger than destination array, it is not an error.
                                    // It simply only copies enough to fill the source array
                                    if (srcAllocatedSize >= dstAllocatedSize)
                                        arrayLimit = dstAllocatedSize;

                                        // If source array is smaller than destination array, it only fills up
                                        // the corresponding elements of the source array
                                    else
                                        arrayLimit = srcAllocatedSize;
                                }

                                // check if element exist (or populate empty element until index
                                for (int i = 0; i < iSubscript; i++)
                                {
                                    try
                                    {
                                        dstArray.get(i);
                                    }
                                    catch (IndexOutOfBoundsException e){
                                        dstArray.add(new ResultValue());
                                    }
                                }

                                // populate array
                                int j = 0;
                                for (int i = iSubscript; i < arrayLimit; i++) {
                                    ResultValue srcVariable = new ResultValue();

                                    srcVariable = srcArray.get(j);
                                    try {
                                        dstArray.set(i, srcVariable);
                                    }
                                    catch (IndexOutOfBoundsException e){
                                        dstArray.add(srcVariable);
                                    }
                                    j++;

                                }

                                resArray = dstArray;
                            }
                        }
                    }

                    // assign new value for target array
                    storageMgr.replaceArray(this, dstArrayName, resArray);
                }

            }
            // assignment for regular variable and array to array assignment
            else {

                // get the assignment operator and check it
                scan.getNext();
                if (scan.currentToken.primClassif != Classif.OPERATOR) {
                    error("expected assignment operator.  Found '%s'", scan.currentToken.tokenStr);
                }

                // operator String
                String operatorStr = scan.currentToken.tokenStr;
                ResultValue resO2;
                ResultValue resO1;

                // move cursor pass the assignment operator
                scan.getNext();

                switch(operatorStr) {
                    case "=":
                        resO2 = expr(";", false);
                        res = assign(variableStr, resO2);
                        break;
                    case "-=":

                        // get evaluated value of expression
                        resO2 = expr(";", false);
                        // expression must be numeric, raise exception if not
                        // pass this (the parser) you can know where the line number is
                        if (resO2.type != SubClassif.INTEGER && resO2.type != SubClassif.FLOAT) {
                            error("Expect operand to be numeric type. Found '%s'", scan.currentToken.tokenStr);
                        }
                        resO1 = getVariableValue(variableStr);
                        res = assign(variableStr, Utility.subtract(this, resO1, resO2));
                        break;
                    case "+=":

                        // get evaluated value of expression
                        resO2 = expr(";", false);
                        // expression must be numeric, raise exception if not
                        // pass this (the parser) you can know where the line number is
                        if (resO2.type != SubClassif.INTEGER && resO2.type != SubClassif.FLOAT) {
                            error("Expect operand to be numeric type. Found '%s'", scan.currentToken.tokenStr);
                        }
                        resO1 = getVariableValue(variableStr);
                        res = assign(variableStr, Utility.add(this, resO1, resO2));
                        break;
                    default:
                        error("Invalid assignment operator '%s'", operatorStr);
                        break;
                }
                //Debug handling
                if(debug.bShowAssign)
                    System.out.printf("...Assign result into '%s' is '%s'\n", variableStr, res.value, operatorStr);
            }
        }
        // array to array assignment
        else {
            // get the assignment operator and check it
            if (!scan.getNext().equals("=")) {
                error("expected assignment operator '='.  Found '%s'", scan.currentToken.tokenStr);
            }

            // move cursor pass the assignment operator
            scan.getNext();

            // NOTE: at this point, current token carries the source value to
            // be assigned

            // verify data type
            if (scan.currentToken.subClassif != SubClassif.IDENTIFIER &&
                    scan.currentToken.subClassif != arrayIdentifier.type) {
                error("Expect assigned value to be type '%s'.  Found type '%s'",
                        arrayIdentifier.type.toString(),
                        scan.currentToken.subClassif.toString());
            }

            // name of array to be assigned (destination array)
            String dstArrayName = arrayIdentifier.value + "[";
            // new array to be assigned
            ArrayList<ResultValue> resArray = new ArrayList<>();
            // maximum number of elements allowed to be assigned into array
            int maxElem = storageMgr.arrayMaxSize(this, dstArrayName);

            // temporary variable for adding to new arraylist
            ResultValue srcVariable = new ResultValue();

            if (scan.currentToken.idenClassif != IdenClassif.PRIMITIVE)
                error("Invalid identifier classification '%s'", scan.currentToken.idenClassif.toString());

            // assigned value is a constant
            if (scan.currentToken.subClassif != SubClassif.IDENTIFIER) {
                srcVariable.structure = IdenClassif.PRIMITIVE;
                srcVariable.type = arrayIdentifier.type;
                srcVariable.value = scan.currentToken.tokenStr;
                srcVariable.terminatingStr = "";

                // if array is numeric type
                if (arrayIdentifier.type == SubClassif.FLOAT)
                    srcVariable = Utility.toFloat(this, srcVariable);
                if (arrayIdentifier.type == SubClassif.INTEGER)
                    srcVariable = Utility.toInt(this, srcVariable);

                // populate array
                for (int i = 0; i < maxElem; i++)
                    resArray.add(srcVariable);
            }
            // assigned value is a variable (can be primitive or array)
            else {
                srcVariable = storageMgr.getVariable(this,
                        scan.currentToken.tokenStr);

                // identifier is primitive variable
                if (srcVariable.structure != IdenClassif.UNBOUND_ARRAY &&
                        srcVariable.structure != IdenClassif.FIXED_ARRAY) {
                    if (arrayIdentifier.type == SubClassif.FLOAT)
                        srcVariable = Utility.toFloat(this, srcVariable);
                    if (arrayIdentifier.type == SubClassif.INTEGER)
                        srcVariable = Utility.toInt(this, srcVariable);
                    if (srcVariable.type != arrayIdentifier.type)
                        error("Expect assigned value to be type '%s'.  Found type '%s'",
                                arrayIdentifier.type.toString(),
                                srcVariable.type.toString());
                    // populate array
                    for (int i = 0; i < maxElem; i++)
                        resArray.add(srcVariable);
                }
                // identifier is array
                else {
                    // name of source array
                    String srcArrayName = scan.currentToken.tokenStr + "[";
                    // structure of source array
                    ResultValue srcStruct = storageMgr.getArrayStructure(this, srcArrayName);
                    // structure of destination array
                    ResultValue dstStruct = storageMgr.getArrayStructure(this, dstArrayName);

                    // verify data type
                    if (srcStruct.type != dstStruct.type)
                        error("Expect assigned value to be type '%s'.  Found type '%s'",
                                dstStruct.type.toString(), srcStruct.type.toString());

                    // source array
                    ArrayList<ResultValue> srcArray = storageMgr.getArray(this, srcArrayName);

                    // destination array
                    ArrayList<ResultValue> dstArray = storageMgr.getArray(this, dstArrayName);

                    int arrayLimit = 0;

                    // fixed array
                    if (srcVariable.structure == IdenClassif.FIXED_ARRAY) {
                        int srcAllocatedSize = storageMgr.getAllocatedLength(this, srcArrayName);
                        int dstMaxSize = storageMgr.arrayMaxSize(this, dstArrayName);

                        // If source array is larger than destination array, it is not an error.
                        // It simply only copies enough to fill the source array
                        if (srcAllocatedSize >= dstMaxSize)
                            arrayLimit = dstMaxSize;

                            // If source array is smaller than destination array, it only fills up
                            // the corresponding elements of the source array
                        else
                            arrayLimit = srcAllocatedSize;

                    }
                    // unbound array
                    else if (srcVariable.structure == IdenClassif.UNBOUND_ARRAY) {
                        int srcAllocatedSize = storageMgr.getAllocatedLength(this, srcArrayName);
                        int dstAllocatedSize = storageMgr.getAllocatedLength(this, dstArrayName);

                        // If source array is larger than destination array, it is not an error.
                        // It simply only copies enough to fill the source array
                        if (srcAllocatedSize >= dstAllocatedSize)
                            arrayLimit = dstAllocatedSize;

                            // If source array is smaller than destination array, it only fills up
                            // the corresponding elements of the source array
                        else
                            arrayLimit = srcAllocatedSize;
                    }
                    else
                        error("Invalid Identifier structure '%s'", srcVariable.structure.toString());

                    // populate array
                    for (int i = 0; i < arrayLimit; i++) {
                        srcVariable = srcArray.get(i);
                        try {
                            dstArray.set(i, srcVariable);
                        }
                        catch (IndexOutOfBoundsException e){
                            dstArray.add(srcVariable);
                        }
                    }

                    resArray = dstArray;
                }
            }

            // put new array into storageMgr
            storageMgr.replaceArray(this, dstArrayName, resArray);
        }


        return res;
    } // END assignmentStmt

    /**
     *declareVariable takes in the declaration type as a string, and then sets the ResultValue object
     * to the desired declaration type. Also stores the variable within the storage manager
     * @param declareType   -   Subclassif type the new variable is meant to be
     * @return res          -   ResultValue object, containing the correct type
     * @throws Exception
     */
    public ResultValue declareVariable(String declareType) throws Exception
    {
        ResultValue res = new ResultValue();
        // get the next token, it should be an identifier
        scan.getNext();
        Token idenToken = scan.currentToken;

        // if token is not an identifier
        if (idenToken.subClassif != SubClassif.IDENTIFIER) {
            error("Expected Identifier at declaration, found '%s'", scan.currentToken.tokenStr);
            return res;
        }

        // variable value is empty
        res.value = "";
        // assign declare type
        switch (declareType) {
            case "Int":
                res.type = SubClassif.INTEGER;
                break;
            case "Float":
                res.type = SubClassif.FLOAT;
                break;
            case "String":
                res.type = SubClassif.STRING;
                break;
            case "Bool":
                res.type = SubClassif.BOOLEAN;
                break;
            case "Date":
                res.type = SubClassif.DATE;
                break;
            default:
                break;
        }

        String nextTokenStr = scan.getNext();

        if (idenToken.idenClassif != IdenClassif.FIXED_ARRAY &&
                idenToken.idenClassif != IdenClassif.UNBOUND_ARRAY)
            // store variable in storage manager
            storageMgr.storage.put(idenToken.tokenStr, res);

        // end of declaration statement
        if (nextTokenStr.equals(";")) {
            // variable type is primitive
            res.structure = IdenClassif.PRIMITIVE;

            // store variable in storage manager
            storageMgr.storage.put(idenToken.tokenStr, res);

            return res;
        }
        // initialization with declaration
        else if(nextTokenStr.equals("="))
        {
            // variable type is primitive
            res.structure = IdenClassif.PRIMITIVE;

            // store variable in storage manager
            storageMgr.storage.put(idenToken.tokenStr, res);

            //Move past equals and into the next token(Should be whats being inputted into the var.
            scan.getNext();

            ResultValue res02;
            // evaluate value for variable
            res02 = expr(";", false);

            if (res02.type != res.type)
                error("Mismatch type with '%s'", idenToken.tokenStr);

            res = assign(idenToken.tokenStr, res02);
            return res;
        }
        // array assignment
        else if (nextTokenStr.equals("[")) {

            // assign type of array
            res.structure = idenToken.idenClassif;

            // meatbol array
            ArrayList<ResultValue> arrayM = new ArrayList<>();

            var iArraySize = 0;

            switch (scan.getNext()) {

                // Fixed array - Rule 3 of declaration
                case ("]"):
                    // identifier is a fixed array
                    res.structure = IdenClassif.FIXED_ARRAY;

                    // check if next token is an equal sign
                    if (!scan.getNext().equals("=")) {
                        error("Expect '=' at declaration,  found '%s'", scan.currentToken.tokenStr);
                        return res;
                    }

                    // assign array based on type
                    iArraySize = iAssignArrayElement(res, arrayM);
                    break;

                // Unbound array - Rule 1 and 2
                case ("unbound"):
                    // identifier is an unbound array
                    res.structure = IdenClassif.UNBOUND_ARRAY;

                    // check for closing bracket
                    if (!scan.getNext().equals("]")) {
                        error("Expect ']' at declaration,  found '%s'", scan.currentToken.tokenStr);
                        return res;
                    }

                    switch (scan.getNext()) {
                        // Rule 1 for unbound array declaration
                        case "=":
                            // assign array based on type
                            assignArrayElement(res, arrayM);
                            break;
                        // Rule 2 for unbound array declaration
                        case ";":
                            break;
                        default:
                            error("Unexpected token '%s'", scan.currentToken.tokenStr);
                    }

                    break;

                // Fixed array - Rule 1 and 2
                default:

                    // Get array size
                    ResultValue arraySize = expr("]", false);

                    // array size must be an integer
                    arraySize = Utility.toInt(this, arraySize);

                    // get array size
                    iArraySize = Integer.parseInt(arraySize.value);

                    // check for closing bracket
                    if (!scan.currentToken.tokenStr.equals("]")) {
                        error("Expect ']' at declaration,  found '%s'", scan.currentToken.tokenStr);
                        return res;
                    }

                    // Fixed array - Rule 1 and 2
                    switch (scan.nextToken.tokenStr) {
                        // Rule 1
                        case "=":
                            scan.getNext();
                            break;
                        // Rule 2
                        case ";":
                            res.value = "0";
                            break;
                        default:
                            error("Unexpected Separator '%s'", scan.getNext());
                            break;
                    }

                    // assign array based on type
                    assignArrayElement(res, arrayM);

                    break;
            }

            // assign string symbol for the array name
            res.value = idenToken.tokenStr;

            // store variable in array storage manager
            storageMgr.initializeArray(this, res.value, arrayM, res, iArraySize);

        }
        else // there wasn't an equals, semicolon, or open bracket, so throw error.
            error("Expected either ; or = at the end of declaration statement, found '%s'", scan.currentToken.tokenStr);


        return res;
    } // END declarevariable

    /**
     *expr takes an ending seperator for the current expression, and pops numerous tokens off a stack to get the desired
     * result. It then returns a ResultValue object containing the newly evaluated result.
     * @param endSeparator      end separator that expression will stop at
     * @return res              ResultValue containing the popped tokens in precedence order
     * @throws Exception        ParserExpection (see code for more details)
     */
    public ResultValue expr(String endSeparator, boolean bStringSlice) throws Exception
    {
        ArrayList<Token> out = new ArrayList<>();
        Stack<Token> stack = new Stack<>();
        // begin on the first token of the expression

        // flag for array slicing
        boolean bArraySlicing = false;

        if (!(scan.currentToken.idenClassif == IdenClassif.UNBOUND_ARRAY ||
                scan.currentToken.idenClassif == IdenClassif.FIXED_ARRAY))
        {
            if(scan.currentToken.primClassif == Classif.OPERAND){
                out.add(scan.currentToken);
            }
            else {
                if (scan.currentToken.tokenStr.equals("~"))
                    out.add(scan.currentToken);
                else
                    stack.push(scan.currentToken);
            }
        }
        else {
            if (!bStringSlice)
                stack.push(scan.currentToken);
        }

        scan.getNext();

        while (!endSeparator.contains(scan.currentToken.tokenStr)) {

            switch (scan.currentToken.primClassif) {
                case OPERAND:
                    out.add(scan.currentToken);
                    break;
                case OPERATOR:

                    // array slicing
                    if (scan.currentToken.tokenStr.equals("~"))
                    {
                        out.add(scan.currentToken);
                        bArraySlicing = true;
                        break;
                    }

                    while(!stack.isEmpty()) { // recognize flow control
                        if (scan.currentToken.getPrecedence()> stack.peek().getStackPrecedence())
                            break;
                        out.add(stack.pop());
                    }
                    stack.push(scan.currentToken);
                    break;
                case SEPARATOR:
                    switch (scan.currentToken.tokenStr) {
                        case "(":
                            stack.push(scan.currentToken);
                            break;
                        case ")":
                            boolean bFoundParen = false;
                            while (!stack.isEmpty()) {
                                Token popped = stack.pop();
                                if (popped.tokenStr.equals("(")) {
                                    bFoundParen = true;
                                    break;
                                }
                                out.add(popped);
                            }

                            // function detected after left paren
                            if (!stack.isEmpty()) {
                                Token peeked = stack.peek();
                                if (peeked.subClassif == SubClassif.BUILTIN)
                                    out.add(stack.pop());
                            }

                            if (!bFoundParen)
                                error("Missing left paren in expression");
                            break;
                        case "[":
                            break;
                        case "]":
                            boolean bFoundArray = false;
                            while(!stack.isEmpty()) {
                                Token popped = stack.pop();
                                if (popped.idenClassif == IdenClassif.UNBOUND_ARRAY ||
                                        popped.idenClassif == IdenClassif.FIXED_ARRAY)
                                {
                                    bFoundArray = true;
                                    out.add(popped);
                                    break;
                                }
                                out.add(popped);
                            }

                            if (!bFoundArray)
                                error("Missing array element in expression");
                            break;
                        default:
                            error("... invalid separator ...");
                    }
                    break;
                default:
                    error("Within expression, found invalid token '%s'", scan.currentToken.tokenStr);
                    break;
            }
            scan.getNext();
        }
        while (!stack.isEmpty()) {
            Token popped = stack.pop();
            if (popped.tokenStr.equals("("))
                error("Missing right paren in expression");
            out.add(popped);
        }

        // return result variable
        ResultValue res = new ResultValue();

        // if the expression is used for array slicing
        // assign tilda '~' as terminating string
        if (bArraySlicing && (out.size() < 3))
        {
            for (Token token : out) {
                res.value += token.tokenStr;
            }
            res.terminatingStr = "~";
            return res;
        }

        // otherwise, evaluate the expression and return
        res = evaluate(out, false);
        return res;
    } // END expr

    /**
     * execute all Meatbol statements in given file name
     * @throws Exception    see executeStatement description
     */
    public void execute() throws Exception {
        executeStatements(true);
    } // END excute

    /**
     *evaluate takes in an arraylist of tokens, moves through the arraylist and checks classifiers to determine
     * actions based off of them. All get pushed on the stack depending on the type it is. They then get popped
     * off the stack in order and use Utility methods to evaluate their values, finally returning the final res object.
     * @param Out           translated postfix expression from infix expression
     * @return  res         ResultValue containing the final result of the popping and evaluation utility methods
     * @throws Exception    ParserException (see code for more details)
     */
    public ResultValue evaluate(ArrayList<Token> Out, boolean bStringSlice) throws Exception
    {
        ResultValue res = new ResultValue();

        Stack<ResultValue> pstStack = new Stack<>();
        Token operator = new Token();

        for (int i = 0; i < Out.size(); i++) {
            //System.out.printf("Out has: %s (%s)\n", Out.get(i).tokenStr, Out.get(i).subClassif.toString());
            if (Out.get(i).primClassif == Classif.OPERAND) {

                ResultValue operand = new ResultValue();
                ResultValue temp = new ResultValue();

                switch (Out.get(i).subClassif) {
                    case IDENTIFIER:
                        temp.type = Out.get(i).subClassif;
                        temp.value = Out.get(i).tokenStr;
                        temp.structure = Out.get(i).idenClassif;

                        if (Out.get(i).idenClassif != IdenClassif.UNBOUND_ARRAY &&
                                Out.get(i).idenClassif != IdenClassif.FIXED_ARRAY) {
                            operand = storageMgr.getVariable(this, Out.get(i).tokenStr);
                            pstStack.push(operand);
                        }
                        else {
                            ResultValue arrayIdentifier = storageMgr.getVariable(this,
                                    Out.get(i).tokenStr.replaceAll("\\[", ""));
                            pstStack.push(arrayIdentifier);
                        }

                        break;
                    case FLOAT:
                        operand.type = SubClassif.FLOAT;
                        operand.structure = IdenClassif.PRIMITIVE;
                        operand.value = Out.get(i).tokenStr;
                        operand.terminatingStr = "";
                        pstStack.push(operand);
                        break;
                    case INTEGER:
                        operand.type = SubClassif.INTEGER;
                        operand.structure = IdenClassif.PRIMITIVE;
                        operand.value = Out.get(i).tokenStr;
                        operand.terminatingStr = "";
                        pstStack.push(operand);
                        break;
                    case STRING:
                        operand.type = SubClassif.STRING;
                        operand.structure = IdenClassif.PRIMITIVE;
                        operand.value = Out.get(i).tokenStr;
                        operand.terminatingStr = "";
                        pstStack.push(operand);
                        break;
                    case DATE:
                        operand.type = SubClassif.DATE;
                        operand.structure = IdenClassif.PRIMITIVE;
                        operand.value = Out.get(i).tokenStr;
                        operand.terminatingStr = "";
                        pstStack.push(operand);
                        break;
                    case BOOLEAN:
                        operand.type = SubClassif.BOOLEAN;
                        operand.structure = IdenClassif.PRIMITIVE;
                        operand.value = Out.get(i).tokenStr;
                        operand.terminatingStr = "";
                        pstStack.push(operand);
                    default:
                        // TODO error?
                        break;
                }
            }
            // if token is a built in function, push it to stack
            else if (Out.get(i).subClassif == SubClassif.BUILTIN) {
                pstStack.push(Out.get(i).toResult());
            }
            // no precedence yet
            else {
                operator = Out.get(i);
                if (operator.subClassif == SubClassif.UNARYMINUS) {
                    ResultValue pop = pstStack.pop();
                    pop.value = "-" + pop.value;
                    pstStack.push(pop);
                }
                else {
                    //Element is an operator so push it into the list
                    ResultValue operator2 = new ResultValue();
                    operator2.value = operator.tokenStr;

                    switch (operator.tokenStr) {
                        case "+":
                            operator2.type = SubClassif.PLUS;
                            pstStack.push(operator2);
                            break;
                        case "-":
                            operator2.type = SubClassif.MINUS;
                            pstStack.push(operator2);
                            break;
                        case "*":
                            operator2.type = SubClassif.MULTIPLY;
                            pstStack.push(operator2);
                            break;
                        case "/":
                            operator2.type = SubClassif.DIVIDE;
                            pstStack.push(operator2);
                            break;
                        case "^":
                            operator2.type = SubClassif.EXPO;
                            pstStack.push(operator2);
                            break;
                        case "#":
                            operator2.type = SubClassif.USER;
                            pstStack.push(operator2);
                            break;
                        case "~":
                            operator2.type = SubClassif.ARRAY_SLICE;
                            pstStack.push(operator2);
                            break;
                        default:
                            error("Unknown operator, %s", operator.tokenStr);
                            break;
                    }
                }
            }
        }

        res = Expression.expression(this, pstStack, bStringSlice);
        return res;
    } // END evaluate

    /**
     * error takes in, most importantly, diagnostic text which helps us determine exactly which part of the code
     * is creating an error within our algorithms, not necessarily java-breaking errors.
     * @param fmt           error message
     * @param varArgs       string parameters
     * @throws Exception
     */
    public void error(String fmt, Object... varArgs) throws Exception
    {
        String diagnosticTxt = String.format(fmt, varArgs);
        throw new ParserException(this.iLineNr
                , diagnosticTxt
                , this.sourceFileNm);
    } // END error

    /**
     *evalCond is called when encountering an if or while condition that needs checked. It looks ahead to the next
     * two tokens to see, in total, what the left operand, operator, and right operand are, and then evaluates the
     * trueness of it based off of the operator and operands. It then returns a ResultValue that is given value from
     * the various utility checks
     *<p>
     * @return              boolean type ResultValue
     * @throws Exception    ParserException (see code below for more details)
     */
    public ResultValue evalCond() throws Exception
    {
        String sLogicOperands = "<,>,<=,>=,!=,and,or,==,not,in,notin";
        String logicalOperator;
        // begin on the first token of the expression
        scan.getNext();

        ResultValue res = new ResultValue();
        ResultValue leftRes = new ResultValue();
        ResultValue rightRes = new ResultValue();

        //System.out.printf("Current Token before evalconds: %s\n", scan.currentToken.tokenStr);
        // not is just 1 operator so it doesn't have left op
        if (!(scan.currentToken.tokenStr.equals("not")))
            leftRes = expr(sLogicOperands, false);


        //System.out.printf("Should be the logic in evalconds: %s\n", scan.currentToken.tokenStr);
        logicalOperator = scan.currentToken.tokenStr;
        scan.getNext();

        //in case of array then this can not handle normally
        // we FLOP so we can't do slicing for array for 'in' and 'notin' operator
        try{
            this.storageMgr.getArray(this,scan.currentToken.tokenStr+"[");
            rightRes.value = scan.currentToken.tokenStr;
            rightRes.structure = IdenClassif.FIXED_ARRAY;
            skipTo(":");
        }
        catch(Exception e){
            rightRes = expr(":", false);
        }


        switch (logicalOperator) {
            case "==":
                res = Utility.equi(this, leftRes, rightRes);
                break;
            case "<=":
                res = Utility.lEqui(this, leftRes, rightRes);
                break;
            case ">=":
                res = Utility.mEqui(this, leftRes, rightRes);
                break;
            case "<":
                res = Utility.less(this, leftRes, rightRes);
                break;
            case ">":
                res = Utility.more(this, leftRes, rightRes);
                break;
            case "!=":
                res = Utility.nEqui(this, leftRes, rightRes);
                break;
            case "not":
                res = Utility.not(this,rightRes);
                break;
            case "in":
                res = Utility.in(this,leftRes,rightRes);
                break;
            case"notin":
                res = Utility.notin(this,leftRes,rightRes);
                break;
            default:
                // TODO error
                break;
        }
        return res;
    } // END evalCond

    /**
     *executeStatements takes in a boolean the may or may not let the code to actually be executed. After,
     * it parses each token, checking what operation it must act based on the subclassif. The various cases
     * then let it switch between various methods to actually execute that line of code and get it's desired
     * affect. At the end, it alters ending tokens so that the flow statements can parse through the lines logically.
     *<p>
     * @param bExec         Boolean to see if we want to run the code in this section, or to skip it all
     * @return              ResultValue
     * @throws Exception    ParserException (see code below for more details)
     */
    public ResultValue executeStatements(Boolean bExec) throws Exception
    {

        ResultValue res = new ResultValue();

        if (bExec) {
            while (scan.getNext() != "") {

                //System.out.printf("Execute: %s\n", scan.currentToken.tokenStr);

                if(scan.currentToken.primClassif == Classif.EOF )
                {
                    System.out.print("Program finished.\n");
                    System.exit(0);
                }


                if (iLineNr != scan.iSourceLineNr)
                    iLineNr = scan.iSourceLineNr;

                switch (scan.currentToken.subClassif) {
                    // declare variable
                    case DECLARE:
                        String declareType = ((STControl) st.getSymbol(scan.currentToken.tokenStr)).symbol;
                        declareVariable(declareType);
                        break;
                    // print function
                    case BUILTIN:
                        // print function
                        if (st.getSymbol(scan.currentToken.tokenStr).symbol.equals("print")) {
                            printFunc();
                        }
                        else {
                            // TODO Error Handling?
                            break;
                        }
                        break;
                    // assign value to variable
                    case IDENTIFIER:
                        assignmentStmt(true);
                        break;
                    // if, while, for
                    case FLOW:
                        // if the keyword is if ==> call if stmt
                        if (st.getSymbol(scan.currentToken.tokenStr).symbol.equals("if")) {
                            ifStmt(true, debug);
                        }
                        // if the keyword is while ==> call while stmt
                        else if (st.getSymbol(scan.currentToken.tokenStr).symbol.equals("while")) {
                            whileStmt(true, debug);
                        }
                        //if the keyword is for ==> call for stmt
                        else if (st.getSymbol(scan.currentToken.tokenStr).symbol.equals("for")){
                            forStmt(true,debug);
                        }
                        else {
                            // TODO Error Handling?
                        }
                        if(scan.currentToken.tokenStr.equals("break")) {
                            res = scan.currentToken.toResult();
                            res.terminatingStr = "break";
                            return res;
                        }
                        break;
                    // end of if, while for
                    case DEBUG:
                        scan.getNext(); //Current is Assign/Expr/etc Next is on/off
                        //Check which is the following word to it, swapping between bShowToken, bShowExpr,
                        // bShowAssign, bShowStmt.
                        if(scan.currentToken.tokenStr.equals("Expr"))
                        {
                            if(scan.nextToken.tokenStr.equals("on"))
                                debug.bShowExpr = true;
                            else if(scan.nextToken.tokenStr.equals("off"))
                                debug.bShowExpr = false;
                            else
                                error("Debug given a non-on/off value");
                        }
                        else if(scan.currentToken.tokenStr.equals("Stmt"))
                        {
                            if(scan.nextToken.tokenStr.equals("on"))
                                scan.bShowStmt = true;
                            else if(scan.nextToken.tokenStr.equals("off"))
                                scan.bShowStmt = false;
                            else
                                error("Debug given a non-on/off value");
                        }
                        else if(scan.currentToken.tokenStr.equals("Assign"))
                        {
                            if(scan.nextToken.tokenStr.equals("on"))
                                debug.bShowAssign = true;
                            else if(scan.nextToken.tokenStr.equals("off"))
                                debug.bShowAssign = false;
                            else
                                error("Debug given a non-on/off value");

                        }
                        else if(scan.currentToken.tokenStr.equals("Token"))
                        {
                            if(scan.nextToken.tokenStr.equals("on"))
                                debug.bShowToken = true;
                            else if(scan.nextToken.tokenStr.equals("off"))
                                debug.bShowToken = false;
                            else
                                error("Debug given a non-on/off value");
                        }
                        scan.getNext(); //current is on/off next is ;
                        break;

                    case END:
                        res = scan.currentToken.toResult();
                        return res;
                }
            }
        }

        // because the isourceline can only affect the getNext routine at the end of each line
        // so we have to go back one extra line so the nex time it is called, it will be on the control token we need
        scan.iColPos = 0;
        scan.iSourceLineNr = iLineNr-1;
        if(scan.nextToken.tokenStr.equals("for")||scan.nextToken.tokenStr.equals("while")
                ||scan.nextToken.tokenStr.equals("if")||scan.nextToken.tokenStr.equals("else")
                ||scan.nextToken.tokenStr.equals("when"))
            skipTo(":");
        else
            skipTo(";");

        String flowToken = scan.getNext();
        String endToken = "";                               // having correspond endToken for each flow token

        switch (flowToken) {
            case "if":
                endToken = "endif";
                break;
            case "else":
                endToken = "endif";
                break;
            case "while":
                endToken = "endwhile";
                break;
            case "for":
                endToken = "endfor";
                break;
            default:
                break;
        }

        int counter = 0;

        // find endif , endwhile, endfor
        if (flowToken.equals("if") || flowToken.equals("while") || flowToken.equals("for")|| flowToken.equals("else")) {
            while (scan.getNext() != "") {

                if (iLineNr != scan.iSourceLineNr)
                    iLineNr = scan.iSourceLineNr;

                if (scan.currentToken.tokenStr.equals(flowToken))
                    counter++;
                if (scan.currentToken.tokenStr.equals(endToken))
                    counter--;

                if (scan.currentToken.tokenStr.equals("else") &&
                        flowToken.equals("if"))
                    break;

                if (counter == -1)
                    break;

            }
            res = scan.currentToken.toResult();
        }


        return res;
    } // END executeStatements

    /**
     *getVariableValue is a simple function that takes in a string, and gets the variable from the storage Manager.
     * it does this to alleviate the syntax for calling the getVariable value.
     *<p>
     * @param variableStr       name of variable
     * @return                  ResultValue of that variable name in storageMgr
     * @throws Exception        Undeclared Variable
     */
    private ResultValue getVariableValue(String variableStr) throws Exception
    {
        return storageMgr.getVariable(this, variableStr);
    } // END getVariableValue

    /**
     * Take in a boolean as argument, execute the statements within the if block if the parameter is true
     * If the boolean is false, it will look for the else block and execute it. If it can't find an else
     * block, it will ignore the if block.
     *
     * @param bExec         boolean value says whether we're executing this block of code
     * @return              This variable actually doesn't mean anything
     * @throws Exception    ParserException (see code below for more details)
     */
    ResultValue ifStmt(Boolean bExec, Debugger debug) throws Exception
    {
        int saveLineNr = scan.currentToken.iSourceLineNr;

        // if we need to evaluate the condition
        if (bExec)
        {
            // we are executing (not ignoring)
            ResultValue resCond = evalCond(); // similar to expression(), but return a boolean

            // if the condition returns True
            if(resCond.type == SubClassif.BOOLEAN && resCond.value.equals("T")) {
                // continue executing


                ResultValue resTemp = executeStatements(true);


                // what ended the statements after the true part? else: or endif;
                //System.out.printf("Hey, terminating string is: %s\n", resTemp.terminatingStr);

                if (resTemp.terminatingStr.equals("else")) {
                    if (!scan.getNext().equals(":")) {
                        error("expected ':' after 'else'");
                    }
                    resTemp = executeStatements(false); // since the condition was true, ignore the else part

                }
                if(resTemp.value.equals("break")){
                    //resTemp = executeStatements(false);
                    resTemp.value = scan.currentToken.tokenStr;
                    resTemp.type = SubClassif.FLOW;
                    return resTemp;
                }

                if (!resTemp.terminatingStr.equals("endif")) {
                    error("expected 'endif' for an 'if' beginning line '%d'", saveLineNr);
                }
                if (!scan.getNext().equals(";")) {
                    error("expected ‘:’ after ‘endif’");
                }
            }
            else {
                // Cond returned False, ignore execution


                ResultValue resTemp = executeStatements(false);
                if (resTemp.terminatingStr.equals("else")) {
                    if (!scan.getNext().equals(":")) {
                        error("expected ':' after 'else'");
                    }
                    resTemp = executeStatements(true); // since the condition was false, execute the else part
                }
                if (!resTemp.terminatingStr.equals("endif")) {
                    error("expected 'endif' for an 'if' beginning line '%d'", saveLineNr);
                }
                if (!scan.getNext().equals(";")) {
                    error("expected ';' after 'endif'");
                }
            }
            return resCond;
        }
        else {
            // we are ignoring execution
            // we want to ignore the conditional, true part, and false part
            skipTo(":");
            ResultValue resTemp = executeStatements(false);
            if (resTemp.terminatingStr.equals("else")) {
                if (!scan.getNext().equals(":")) {
                    error("expected ':' after 'else'");
                }
                resTemp = executeStatements(false); // since the condition was true, ignore the else part
            }
            if (!resTemp.terminatingStr.equals("endif")) {
                error("expected 'endif' for an 'if' beginning line '%d'", saveLineNr);
            }
            if (!scan.getNext().equals(";")) {
                error("expected ‘:’ after ‘endif’");
            }
            return resTemp;
        }
    } // END ifStmt

    /**
     *printFunc uses an arraylist to hold the various tokens that need to be printed after encountering a
     * "print" call in the input text. From here, it also evaluates the passed parameters with the print
     * function and adds it to the arraylist. It then prints the arraylist at the end.
     *
     * @throws Exception    ParserException (see code below for more details)
     */
    public void printFunc() throws Exception
    {
        ResultValue res = new ResultValue();

        // if next token is not a left paren
        if (!scan.getNext().equals("(")) {
            error("Expected '(' in print statement, found '%s'", scan.currentToken.tokenStr);
        }

        boolean bAddLiteral = false;
        boolean sepSwitch = false;
        boolean bIgnoreComma = false;
        int internalParen = 0;
        Stack<Token> stack = new Stack<>();
        ArrayList<Token> Out = new ArrayList<>();

        //scan.getNext();
        // keep printing until meeting right paren
        while (!scan.getNext().equals(")") || internalParen >= 1) {
            //System.out.printf("expr: %s (%s)\n", scan.currentToken.tokenStr, scan.currentToken.subClassif.toString());
            //System.out.printf("internalParen: %d\n", internalParen);
            switch(scan.currentToken.primClassif) {
                // if there is a comma as separator for print function
                case SEPARATOR:
                    // some other separator is detected other than comma

                    switch (scan.currentToken.tokenStr) {
                        case "(":
                            //System.out.printf("Left Paren found |%s|%d| --> Need to upgrade precedence\n", scan.nextToken.tokenStr,internalParen);
                            internalParen++;
                            stack.push(scan.currentToken);
                            break;
                        case ")":
                            //System.out.printf("Right Paren found |%s|%d --> Need to downgrade precedence\n", scan.nextToken.tokenStr, internalParen);
                            internalParen--;
                            boolean bFoundParen = false;
                            bIgnoreComma = false;
                            while(!stack.isEmpty()) {
                                Token popped = stack.pop();
                                if(popped.tokenStr.equals("(")) {
                                    bFoundParen = true;
                                    break;
                                }
                                Out.add(popped);
                            }

                            // function detected after left paren
                            if (!stack.isEmpty()) {
                                Token peeked = stack.peek();
                                if (peeked.subClassif == SubClassif.BUILTIN)
                                    Out.add(stack.pop());
                            }

                            if (!bFoundParen)
                                error("Missing left paren in expression");
                            break;
                        case "[":
                            break;
                        case "]":
                            while(!stack.isEmpty()) {
                                Token popped = stack.pop();
                                if (popped.idenClassif == IdenClassif.UNBOUND_ARRAY ||
                                        popped.idenClassif == IdenClassif.FIXED_ARRAY) {
                                    Out.add(popped);
                                    break;
                                }
                                Out.add(popped);
                            }
                            break;
                        case ",":
                            // if a comma wasn't detected before
                            for(int i = 0; i < stack.size(); i++)
                            {
                                if(stack.get(i).tokenStr.equals("dateDiff"))
                                    bIgnoreComma = true;
                                else if(stack.get(i).tokenStr.equals("dateAdj"))
                                    bIgnoreComma = true;
                                else if(stack.get(i).tokenStr.equals("dateAge"))
                                    bIgnoreComma = true;
                            }
                            if(!bIgnoreComma) {
                                if (!sepSwitch) {
                                    sepSwitch = true;
                                    //showStack(Out);
                                    if (Out.size() > 0) {
                                        while (!stack.empty()) {
                                            Out.add(stack.pop());
                                        }

                                        res = evaluate(Out, true);
                                        System.out.printf("%s ", res.value);
                                    }
                                    stack.clear();
                                    Out.clear();
                                    bAddLiteral = false;
                                    bIgnoreComma = false;
                                    continue;
                                }
                                // if a comma was detected before
                                else {
                                    // evaluate the expression between the separators
                                    while (!stack.empty()) {
                                        Out.add(stack.pop());
                                    }
                                    res = evaluate(Out, true);
                                    bIgnoreComma = false;
                                    // if there is nothing between the commas
                                    if (res.value.equals("") && res.type != SubClassif.STRING) {
                                        error("Empty expression between commas");
                                    }

                                    // print value of the evaluated expression to std
                                    if (res.type == SubClassif.FLOAT && !res.value.contains("."))
                                        res.value += ".0";
                                    //System.out.printf("--%s--", res.type.toString());
                                    System.out.printf("%s ", res.value);

                                    // reset stack and out
                                    stack = new Stack<>();
                                    Out = new ArrayList<>();

                                }
                            }
                            bIgnoreComma = false;
                            break;
                        default:
                            error("Expected ',' as separtor in print statement, found '%s'", scan.currentToken.tokenStr);
                    }

                    break;
                // if there is an operand, it will indicate an expression
                // evualuate the expression, then print the result
                case OPERAND:
                    if (!(scan.currentToken.idenClassif == IdenClassif.UNBOUND_ARRAY ||
                            scan.currentToken.idenClassif == IdenClassif.FIXED_ARRAY))
                        Out.add(scan.currentToken);
                        // array indicator can also be string
                    else
                    {
                        stack.push(scan.currentToken);
                    }
                    break;
                // if it is an operator, push it into the stack
                case OPERATOR:

                    if (scan.currentToken.tokenStr.equals("~"))
                    {
                        Out.add(scan.currentToken);
                        break;
                    }

                    while(!stack.isEmpty()) { // recognize flow control
                        if (scan.currentToken.getPrecedence() > stack.peek().getStackPrecedence())
                            break;
                        Out.add(stack.pop());
                    }
                    stack.push(scan.currentToken);
                    break;
                case FUNCTION:
                    stack.push(scan.currentToken);
                    break;
                default:
                    // TODO error?
                    break;
            }
        }
        // pop out what's left in the stack and evaluate
        if (Out.size() > 0) {
            while (!stack.empty()) {
                Out.add(stack.pop());
            }

            res = evaluate(Out, true);
            // if there is nothing between the commas
            if (res.value.equals("")) {
                error("Empty expression between commas");
            }
            // print value of the evaluated expression to std
            //System.out.printf("--%s--",res.type.toString());
            if (res.type == SubClassif.FLOAT && !res.value.contains("."))
                res.value += ".0";
            System.out.printf("%s",res.value);
        }

        // should be a semicolon to terminate print statement
        if (!scan.getNext().equals(";")) {
            System.out.printf("Got %s instead\n", scan.currentToken.tokenStr);
            error("Expected ';' to terminate print statement. Found '%s'", scan.currentToken.tokenStr);
        }

        // advance print cursor to next line
        System.out.print("\n");
    } // END printFunc

    /**
     *skipTo takes in a string with the ending parameter, and hops through the line till it hits the parameter.
     * if it hits an End Of File, it exits the program.
     * @param endParam      string with the desired token to skip to.
     * @throws Exception    ParserException (see code below for more details)
     */
    private void skipTo(String endParam) throws Exception
    {
        while (!scan.getNext().equals( endParam) ){
            if(scan.currentToken.primClassif == Classif.EOF)
            {
                System.out.print("Program finished.\n");
                return;
            }
        }

    } // END skipTo

    /**
     * Take in a boolean as argument, execute within the block if the parameter ends up true,
     * if not, it skips the entire code block and moves past it to the endwhile
     * @param bExec         boolean says whether we're executing or not
     * @param debug         Debugger object for storing booleans
     * @return              This return value doesn't mean anything
     * @throws Exception    ParserException (see code below for more details)
     */
    ResultValue whileStmt(Boolean bExec, Debugger debug) throws Exception
    {
        int saveLineNr = scan.currentToken.iSourceLineNr;

        if(debug.bShowStmt)
        {
            String leftOp = scan.currentToken.tokenStr;
            scan.getNext();
            String logicOp = scan.currentToken.tokenStr;
            String rightOp = scan.nextToken.tokenStr;
            System.out.printf(">while: %s %s %s\n", leftOp, logicOp, rightOp);
            scan.iSourceLineNr = saveLineNr;
        }

        if(bExec)
        {
            ResultValue resCond = evalCond();

            //while the condition returns true
            if(resCond.type == SubClassif.BOOLEAN && resCond.value.equals("T"))
            {
                //Executing the lines within the while
                ResultValue resTemp = executeStatements(true);

                if(scan.currentToken.tokenStr.equals("break")){
                    //ResultValue resTemp = executeStatements(false);
                    while(!scan.currentToken.tokenStr.equals("endwhile")){
                        scan.getNext();
                    }
                    return resCond;
                }

                //Errors to check the endWhile lines
                if(!resTemp.terminatingStr.equals("endwhile"))
                {
                    error("Expected 'endwhile' for a 'while' beginning line '%d'", saveLineNr);
                }
                if(!scan.nextToken.tokenStr.equals(";"))
                {
                    error("Expected ‘;’ after ‘endwhile’");
                }
                scan.iSourceLineNr = saveLineNr-1;
            }
            else //Condition is false, no execution
            {
                //ignore the executing lines
                ResultValue resTemp = executeStatements(false);

                if(!resTemp.terminatingStr.equals("endwhile"))
                {
                    error("Expected 'endwhile' for a 'while' beginning line '%d'", saveLineNr);
                }
                if(!scan.nextToken.tokenStr.equals(";"))
                {
                    error("Expected ‘;’ after ‘endwhile’");
                }
            }
            return resCond;
        }
        else //Skipping over the lines because the bExec is false
        {
            //Ignoring execution now
            ResultValue resTemp = executeStatements(false);
            if(!resTemp.terminatingStr.equals("endwhile"))
            {
                error("Expected 'endwhile' for a 'while' beginning line '%d'", saveLineNr);
            }
            if(!scan.getNext().equals(";"))
            {
                error("Expected ‘;’ after ‘endwhile’");
            }
            return resTemp;
        }
    } // END OF WHILE STATEMENT

    /**
     *For Statement: take in a boolean argument and pass on to sub classification of for loop:
     * <p>
     *     This function's main purpose is to determine which type of loop we are dealing with,
     *     pass on the correct argument value for the subclassification loop to execute
     * @param bExec     -   the boolean value to indicate whether or not we are going to exec this loop
     * @param debug     -   the debugger for debugging purposes
     * @throws Exception-   ParserException
     */
    public ResultValue forStmt(boolean bExec, Debugger debug) throws Exception{
        // saveLineNr for error handling and go back
        int saveLineNr = scan.currentToken.iSourceLineNr;
        ResultValue res = new ResultValue();

        // scan the whole line to determine what type of for stmt that is
        String forStmtScan = scan.currentToken.tokenStr;
        String temp = scan.getNext();
        while(!(temp.equals(":"))){
            //if the token pass to another line ==> raise missing ':' error
            if (scan.currentToken.iSourceLineNr != saveLineNr)
                error("Expecting ':' at the end of the for Stmt");
            forStmtScan = forStmtScan +" "+ temp;
            temp = scan.getNext();

        }

        //System.out.println(forStmtScan);
        //System.out.println(scan.currentToken.tokenStr);
        // determined if it is for-to or for-in type
        if(forStmtScan.contains("in")) {
            String [] forStmtSplit = forStmtScan.split(" ");

            //[1] will hold the cv , [3] will hold the array name
            //==> send in the 2 name will eliminate the extra scanning issue
            forInStmt(bExec,debug);
        }
        else if(forStmtScan.contains("to")) {
            if(forStmtScan.contains("by"))
                res = forToStmt(bExec,debug,true);
            else
                res = forToStmt(bExec,debug,false);
        }
        else{
            error("Invalid for statement format");
        }
        return res;
    }// END OF FOR STATEMENT


    /**
     *For In loop: a sub-classification of for loop:
     * <p>
     *     This function will take care of the for in type of loop
     *     This function will deal with array collection or a String variable (we can see this as the generic char array)
     *
     * @param bExec     -   the boolean value to indicate whether or not we are going to exec this loop
     * @param debug     -   the debugger for debugging purposes
     * @return
     * @throws Exception-   ParserException
     */
    public ResultValue forInStmt(boolean bExec, Debugger debug)throws Exception{
        /*NOTE:
        collection          - the original copy of the array or String (not to be change to maintain the length)
        collection_token    - the String variable in StgMng of the collection should there be any (only in STRING case)
        collection_name     - the name of the collection (can be an array or a String var)
        dummy_collection    - dummy collection keep track of the changes user make to replace the collection at the end
        dummy_key           - the key used in storage manager
        cv                  - counting variable (the item) not the index, but change base on the index
        cv_token_str        - the variable name for cv
        * */

        ResultValue cv = new ResultValue();
        String cv_token_str = "";
        String collection_name = "";                 //this could be a String

        // RESET THE LINE NUMBER FOR ACTUAL PARSING
        // (REFER TO FORTO FOR MORE EXPLANATION)
        scan.iColPos = 0;
        scan.iSourceLineNr = iLineNr-1;
        if(scan.nextToken.tokenStr.equals("for")||scan.nextToken.tokenStr.equals("while")
                ||scan.nextToken.tokenStr.equals("if")||scan.nextToken.tokenStr.equals("else")
                ||scan.nextToken.tokenStr.equals("when")) {
            skipTo(":");
        }
        else {
            skipTo(";");
        }

        // GETTING AND CHECKING INFO
        int saveLineNr = scan.currentToken.iSourceLineNr+1;
        scan.getNext();                                 //token: "for"
        cv_token_str = scan.getNext();                  //token: counting var
        if(cv_token_str.equals("in"))
            error("Missing 'COUNTING VARIABLE' for the loop");
        scan.getNext();                                 //token: "in"
        collection_name = scan.getNext();                    //token: 1st expr after in: could be an array name or a String
        if(collection_name.equals(":"))
            error("Missing 'COLLECTION' for the loop");


        //CURRENT TOKEN: the token right after in
        //DETERMINE IF DEALING WITH ARRAY OR STRING EXPR
        //check current token if it is an array or a String type
        //      If array ==> retrieve
        //      If String (both literal or variable) ==> treat it as an expr

        // if in the collection spot is a String then this one act as a dummy
        // other wise it should retrieve the info of that variable
        ResultValue collection_token = new ResultValue();
        collection_token.value = "";
        collection_token.type = SubClassif.GOOD;
        if(scan.currentToken.subClassif == SubClassif.IDENTIFIER){
            collection_token = this.storageMgr.getVariable(this,scan.currentToken.tokenStr);
        }

        // if the token it self is a string or a var with subclass String

        if((collection_token.structure == IdenClassif.UNBOUND_ARRAY)||
                (collection_token.structure == IdenClassif.FIXED_ARRAY)) {
            //TODO: ARRAY LOOP
            // MODIFY THIS CHECK EXIST DOWN HERE FOR ARRAY
            /*array_token   -   the array itself with all the elem
              array_name    -   the name of the collection that has [ at the end to access array StrMng
             * */
            String array_name = collection_name+"[";
            ArrayList<ResultValue> old_arrayM = new ArrayList<>(this.storageMgr.getArray(this, collection_name +"["));
            ArrayList<ResultValue> arrayM = new ArrayList<>();
            for (ResultValue elem : old_arrayM) arrayM.add(elem);
            skipTo(":");                        //token: ":"

            // CREATING INDEX FOR EACH ITEM IN THE ARRAY
            //  - STORE USING LINE NUMBER (SIMILAR TO FOR-TO)
            //  - IF EXIST: RETRIEVE
            //  - ELSE: CREATE AND INIT TO 0
            ResultValue index = new ResultValue();
            try{
                index = this.storageMgr.getVariable(this,Integer.toString(saveLineNr));

            }catch(Exception e){
                // if doesn't exist initialized it with new index
                index.value = "0";
                index.type = SubClassif.INTEGER;
                this.storageMgr.storage.put(Integer.toString(saveLineNr),index);
            }

            // CREATING LIMIT:
            //  - TYPE: INT
            //  - VALUE: THE LENGTH OF WHAT EVER IN COLLECTION
            ResultValue limit = new ResultValue();
            limit.type = SubClassif.INTEGER;
            limit.value = Integer.toString(arrayM.size());

            // CREATING INCR:
            //  - TYPE: INT
            //  - VALUE: 1
            ResultValue incr = new ResultValue();
            incr.type = SubClassif.INTEGER;
            incr.value = "1";



            //****** IMPORTANT NOTE: FOR IN WILL NOT LET OUTSIDE VAR AFFECT ITERATION
            // create a temporary place on the storage manager to store the original value
            // after finish execute, we pop back (like save on the stack and pop)
            try{
                this.storageMgr.storage.put("2"+cv_token_str,this.storageMgr.getVariable(this,cv_token_str));
            }catch(Exception e){}

            //CHECK IF CV EXIST, IF NOT THEN CREATE IT WITH THE ARRAY'S DATA TYPE
            ResultValue cv1 = new ResultValue();
            try{
                //EXIST:    -   retrieve
                //          -   check data type
                cv1 = storageMgr.getVariable(this,cv_token_str);
            }
            //if fail then we declare
            catch (Exception e){
                //NOT EXIST
                //          -   assign with the first char of collection
                cv1 = arrayM.get(0);
                this.storageMgr.storage.put(cv_token_str,cv1);
            }
            cv.value = cv1.value;
            cv.type = cv1.type;
            cv.structure = cv1.structure;
            if (cv.type != arrayM.get(0).type)
                error("The 'ITEM' variable: '%s' doesn't have the same type as the 'ARRAY'",cv_token_str);

            //*** WE WANT LAST ITERATION VALUE CARRY OUTSIDE THE LOOP BUT NOT FOR NEXT ITERATION
            // ==> every iteration: check if the first spot has the correct original character
            //                      if not then reinitialized it
            if((cv1.value != arrayM.get(0).value)&&
                    (index.value.equals("0"))){
                cv.value = arrayM.get(0).value;
                cv.type = arrayM.get(0).type;
                cv.structure = arrayM.get(0).structure;
                this.storageMgr.storage.put(cv_token_str,cv);
            }


            //========================================================================
            //EXPLAIN: Behind the scene, we use index to access everything
            // so this should be very familiar with FOR TO loop
            //========================================================================

            // CREATING A DUMMY array: for case that the use alter the element
            ArrayList<ResultValue> dummy_arrayM;
            String dummy_key = Integer.toString(saveLineNr)+"a[";
            try{
                dummy_arrayM =new  ArrayList <ResultValue>(this.storageMgr.getArray(this,dummy_key));
            }catch (Exception e){
                dummy_arrayM =new  ArrayList <ResultValue>();
                this.storageMgr.storageM.put(dummy_key,dummy_arrayM);
            }
            //EXECUTE
            if(bExec) {
                //EXECUTE BLOCK:

                ResultValue resCond = new ResultValue();
                resCond = Utility.less(this, index, limit);
                if (resCond.value.equals("T")) {
                    ResultValue res = executeStatements(true);

                    //update cv, index and store them back to the storage manager

                    //Just in case we have an array out of bound
                    // (I can check for it but try catch is easier than having to deal with all the conversion)
                    try {
                        //AT THIS STEP:
                        // dummy collection will append the currently changed cv (if user choose to change anything)
                        // cv will get reset to the next item according to the original String collection

                        //update dummy_array
                        ResultValue temp = this.storageMgr.getVariable(this, cv_token_str);
                        cv.type = temp.type;
                        cv.value = temp.value;
                        cv.structure = temp.structure;
                        dummy_arrayM.add(Integer.parseInt(index.value),cv);
//                        System.out.println("Dummy array "+ dummy_arrayM.get(0).value);
                        this.storageMgr.replaceArray(this, dummy_key, dummy_arrayM);



                        //update index
                        index.value = String.valueOf(Integer.parseInt(index.value) + Integer.parseInt(incr.value));
                        this.storageMgr.replace(this, Integer.toString(saveLineNr), index);

                        //update cv based on the original String collection's next iteration
//                        arrayM.set(Integer.parseInt(index.value)-1,cv);
                        cv = arrayM.get(Integer.parseInt(index.value));
                        this.storageMgr.replace(this, cv_token_str, cv);

                    } catch (IndexOutOfBoundsException e) {
                    }


                    //After executing: check if it has end for
                    if (!(scan.currentToken.tokenStr.equals("endfor"))) {
                        error("Expected 'endfor' for a 'for' beginning line '%d'", saveLineNr);
                    }
                    if (!(scan.nextToken.tokenStr.equals(";"))) {
                        error("Expected ‘;’ after ‘endfor’");
                    }
                    scan.iSourceLineNr = saveLineNr - 1;
                }else {
                    ResultValue resTemp = executeStatements(false);
                    // when the loop is done or fail:
                    //  - remove the counting var instance in that loop (like a scope of the cv)
                    //  - remove the dummy collection
                    //  - remove the cv

                    //rm index
                    this.storageMgr.storage.remove(Integer.toString(saveLineNr));
                    //store replace the original with the dummy array
                    this.storageMgr.replaceArray(this,collection_name+"[",dummy_arrayM);
                    //remove the dummy array
                    this.storageMgr.storageM.remove(dummy_key);
                    //remove the counting variable
                    this.storageMgr.storage.remove(cv_token_str);
                    //try to put back the value before the loop
                    try{
                        this.storageMgr.storage.put(cv_token_str,this.storageMgr.getVariable(this,"2"+cv_token_str));
                        this.storageMgr.storage.remove("2"+cv_token_str);
                    }catch (Exception e){}

                    //After executing: check if it has end for
                    if(!(scan.currentToken.tokenStr.equals("endfor")))
                    {
                        error("Expected 'endfor' for a 'for' beginning line '%d'", saveLineNr);
                    }
                    if(!(scan.getNext().equals(";")))
                    {
                        error("Expected ‘;’ after ‘endfor’");
                    }
                }
                return resCond;
            }//NO EXECUTE BLOCK
            else {
                ResultValue resTemp = executeStatements(false);
                // when the loop is done or fail:
                //  - remove the counting var instance in that loop (like a scope of the cv)
                //  - remove the dummy collection
                //  - remove the cv                this.storageMgr.storage.remove(Integer.toString(saveLineNr));
                //rm index
                this.storageMgr.storage.remove(Integer.toString(saveLineNr));
                //store replace the original with the dummy array
                this.storageMgr.replaceArray(this,collection_name+"[",dummy_arrayM);
                //remove the dummy array
                this.storageMgr.storageM.remove(dummy_key);
                //remove the counting variable
                this.storageMgr.storage.remove(cv_token_str);
                //try to put back the value before the loop
                try{
                    this.storageMgr.storage.put(cv_token_str,this.storageMgr.getVariable(this,"2"+cv_token_str));
                    this.storageMgr.storage.remove("2"+cv_token_str);
                }catch (Exception e){}

                //After executing: check if it has end for
                if (!(scan.currentToken.tokenStr.equals("endfor"))) {
                    error("Expected 'endfor' for a 'for' beginning line '%d'", saveLineNr);
                }
                if (!(scan.getNext().equals(";"))) {
                    error("Expected ‘;’ after ‘endfor’");
                }
                return resTemp;
            }
        }
        else if((scan.currentToken.subClassif == SubClassif.STRING)||
                (collection_token.type == SubClassif.STRING))
        {
            //TODO: STRING LITERAL OR STRIG VAR LOOP
            //Evaluate the expression
            ResultValue collection = new ResultValue();
            collection = expr(":",false);         //token: ":"

            //========================================================================
            //EXPLAIN: Behind the scene, we use index to access everything
            // so this should be very familiar with FOR TO loop
            //========================================================================

            // CREATING INDEX FOR EACH ITEM IN THE ARRAY
            //  - STORE USING LINE NUMBER (SIMILAR TO FOR-TO)
            //  - IF EXIST: RETRIEVE
            //  - ELSE: CREATE AND INIT TO 0
            ResultValue index = new ResultValue();
            try{
                index = this.storageMgr.getVariable(this,Integer.toString(saveLineNr));

            }catch(Exception e){
                // if doesn't exist initialized it with new index
                index.value = "0";
                index.type = SubClassif.INTEGER;
                this.storageMgr.storage.put(Integer.toString(saveLineNr),index);
            }

            // CREATING LIMIT:
            //  - TYPE: INT
            //  - VALUE: THE LENGTH OF WHAT EVER IN COLLECTION
            ResultValue limit = new ResultValue();
            limit.type = SubClassif.INTEGER;
            limit.value = Integer.toString(collection.value.length());

            // CREATING INCR:
            //  - TYPE: INT
            //  - VALUE: 1
            ResultValue incr = new ResultValue();
            incr.type = SubClassif.INTEGER;
            incr.value = "1";

            //****** IMPORTANT NOTE: FOR IN WILL NOT LET OUTSIDE VAR AFFECT ITERATION
            // create a temporary place on the storage manager to store the original value
            // after finish execute, we pop back (like save on the stack and pop)
            try{
                this.storageMgr.storage.put("1"+cv_token_str,this.storageMgr.getVariable(this,cv_token_str));
            }catch(Exception e){}

            //CHECK IF CV EXIST, IF NOT THEN CREATE IT WITH THE ARRAY'S DATA TYPE
            try{
                //EXIST:    -   retrieve
                //          -   check data type
                cv = storageMgr.getVariable(this,cv_token_str);
            }
            //if fail then we declare
            catch (Exception e){
                //NOT EXIST -   create the variable
                //          -   assign with the first char of collection
                ResultValue temp = new ResultValue();
                temp.value = Character.toString(collection.value.toCharArray()[0]);
                temp.type = SubClassif.STRING;

                this.storageMgr.storage.put(cv_token_str,temp);
            }
            if (cv.type != SubClassif.STRING )
                error("The 'ITEM' variable: '%s' have to be String type",cv_token_str);

            //*** WE WANT LAST ITERATION VALUE CARRY OUTSIDE THE LOOP BUT NOT FOR NEXT ITERATION
            // ==> every iteration: check if the first spot has the correct original character
            //                      if not then restore it
            if ((cv.value != Character.toString(collection.value.toCharArray()[0]))&&
                    (index.value.equals("0")))
                cv.value = Character.toString(collection.value.toCharArray()[0]);


            // CREATING A DUMMY COLLECTION: for case that the use alter the element
            ResultValue dummy_collection = new ResultValue();
            String dummy_key = Integer.toString(saveLineNr)+"a";
            try{
                dummy_collection = this.storageMgr.getVariable(this,dummy_key);
            }catch (Exception e){
                dummy_collection.type = SubClassif.STRING;
                dummy_collection.value = "";
                this.storageMgr.storage.put(dummy_key,dummy_collection);
            }

            //EXECUTE
            if(bExec){
                //EXECUTE BLOCK:

                ResultValue resCond = new ResultValue();
                resCond = Utility.less(this, index, limit);
                if(resCond.value.equals("T") ) {
                    ResultValue res = executeStatements(true);

                    //update cv, index and store them back to the storage manager
                    //update index
                    index.value = String.valueOf(Integer.parseInt(index.value) + Integer.parseInt(incr.value));
                    this.storageMgr.replace(this,Integer.toString(saveLineNr),index);

                    //update cv
                    ResultValue temp = new ResultValue();
                    //Just in case we have an array out of bound
                    // (I can check for it but try catch is easier than having to deal with all the conversion)
                    try{
                        //AT THIS STEP:
                        // dummy collection will concat with the currently changed cv (if user choose to change anything)
                        // cv will get reset to the next item according to the original String collection

                        //update dummy_collection
                        dummy_collection.value = dummy_collection.value +this.storageMgr.getVariable(this,cv_token_str).value;
                        this.storageMgr.replace(this,dummy_key,dummy_collection);

                        //update cv based on the original String collection
                        temp.value = Character.toString(collection.value.toCharArray()[Integer.parseInt(index.value)]);
                        temp.type = SubClassif.STRING;

                        this.storageMgr.replace(this,cv_token_str,temp);

                    }catch (ArrayIndexOutOfBoundsException e){ }


                    if(scan.currentToken.tokenStr.equals("break")){
                        //ResultValue resTemp = executeStatements(false);
                        while(!scan.currentToken.tokenStr.equals("endfor")){
                            scan.getNext();
                        }
                        return resCond;
                    }


                    //After executing: check if it has end for
                    if(!(scan.currentToken.tokenStr.equals("endfor")))
                    {
                        error("Expected 'endfor' for a 'for' beginning line '%d'", saveLineNr);
                    }
                    if(!(scan.nextToken.tokenStr.equals(";")))
                    {
                        error("Expected ‘;’ after ‘endfor’");
                    }
                    scan.iSourceLineNr = saveLineNr -1 ;
                }else {
                    ResultValue resTemp = executeStatements(false);
                    // when the loop is done or fail:
                    //  - remove the counting var instance in that loop (like a scope of the cv)
                    //  - remove the dummy collection
                    //  - remove the cv
                    this.storageMgr.storage.remove(Integer.toString(saveLineNr));
                    if(collection_token.type == SubClassif.STRING)
                        collection.value = dummy_collection.value;
                    this.storageMgr.storage.remove(dummy_key);
                    this.storageMgr.storage.remove(cv_token_str);
                    try{
                        this.storageMgr.storage.put(cv_token_str,this.storageMgr.getVariable(this,"1"+cv_token_str));
                        this.storageMgr.storage.remove("1"+cv_token_str);
                    }catch (Exception e){}

                    //After executing: check if it has end for
                    if(!(scan.currentToken.tokenStr.equals("endfor")))
                    {
                        error("Expected 'endfor' for a 'for' beginning line '%d'", saveLineNr);
                    }
                    if(!(scan.getNext().equals(";")))
                    {
                        error("Expected ‘;’ after ‘endfor’");
                    }
                }
                return resCond;
            }
            //NO EXECUTE BLOCK
            else {
                ResultValue resTemp = executeStatements(false);
                // when the loop is done or fail:
                //  - remove the counting var instance in that loop (like a scope of the cv)
                //  - remove the dummy collection
                //  - remove the cv                this.storageMgr.storage.remove(Integer.toString(saveLineNr));
                if(collection_token.type == SubClassif.STRING)
                    collection.value = dummy_collection.value;
                this.storageMgr.storage.remove(dummy_key);
                this.storageMgr.storage.remove(cv_token_str);
                try{
                    this.storageMgr.storage.put(cv_token_str,this.storageMgr.getVariable(this,"1"+cv_token_str));
                    this.storageMgr.storage.remove("1"+cv_token_str);
                }catch (Exception e){}

                //After executing: check if it has end for
                if (!(scan.currentToken.tokenStr.equals("endfor"))) {
                    error("Expected 'endfor' for a 'for' beginning line '%d'", saveLineNr);
                }
                if (!(scan.getNext().equals(";"))) {
                    error("Expected ‘;’ after ‘endfor’");
                }
                return resTemp;

            }
        }
        else{
            //if in that place is something other than an array or a string then error
            this.error("Expecting an 'ARRAY' or a 'STRING' expression in for the 'COLLECTION' of the loop.");
        }
        ResultValue dummy = new ResultValue();
        return dummy;

    }// END OF FOR IN STATEMENT

    /**
     *For To loop: a sub-classification of for loop:
     * <p>
     *      This function will take care of the for to type of loop
     *      This function will deal counting variable, manipulating indexes
     *           to access different type of element inside a collection or simply just a normal counting loop
     * @param bExec     -   the boolean value to indicate whether or not we are going to exec this loop
     * @param debug     -   the debugger for debugging purposes
     * @param by        -   a boolean value to indicate if we are having the incr argument
     * @return
     * @throws Exception-   ParserException
     */
    public ResultValue forToStmt(boolean bExec, Debugger debug, boolean by)throws Exception{
        ResultValue limit = new ResultValue();
        ResultValue cv = new ResultValue();
        ResultValue incr = new ResultValue();
        //TODO: something to differentiate the initialization of the for loop vs loop back

        // reset the scanner to the beginning of the for stmt so we can actually parse in this time
        scan.iColPos = 0;
        scan.iSourceLineNr = iLineNr-1;

        //since it has to go all the way to the end of next line to reset the line number
        // we should anticipate what is the ending symbol for the next statement
        // if FLOW type: ':', Else: ';'
        if(scan.nextToken.tokenStr.equals("for")||scan.nextToken.tokenStr.equals("while")
                ||scan.nextToken.tokenStr.equals("if")||scan.nextToken.tokenStr.equals("else")
                ||scan.nextToken.tokenStr.equals("when"))
            skipTo(":");
        else
            skipTo(";");


        int saveLineNr = scan.currentToken.iSourceLineNr+1;
        scan.getNext();                                  //getting the FOR keyword
        String cv_token_str = scan.getNext();            //getting the counting variable's name

        //get counting variable from storage, if not exist then create then store
        try{
            cv = this.storageMgr.getVariable(this,cv_token_str);
        }catch(Exception e){
            cv.value = Integer.toString(0);
            cv.type = SubClassif.INTEGER;
            this.storageMgr.storage.put(cv_token_str,cv);
        }

        // check if we can assign the expression to cv or not
        //      pass the bucket to check if cv is already declare
        // current token: cv
        if(!(scan.nextToken.tokenStr.equals("=")))
            error("Expecting '=' operator after the counting variable");
        else{
            scan.getNext();                         //current token: '='
            scan.getNext();                         //current token: the beginning of the expression
            // assign any expression all the way to keyword 'to' to the cv variable + check exist
            cv = assign(cv_token_str,expr("to", false));
        }

        //current token: 'TO'
        // if we have a by keyword in for stmt then the end of limit expr is a by
        if(by){
            scan.getNext();                         //get first token of limit expr
            //check missing limit or not
            if(scan.currentToken.tokenStr.equals("by"))
                error("Missing 'LIMIT' expression for the loop");
            limit = expr("by", false);         //curent token on "BY"
            scan.getNext();                         //get first token of incr expr
            //check Missing incr or not
            if(scan.currentToken.tokenStr.equals(":"))
                error("Missing 'INCREMENT' expression for the loop");
            incr = expr(":", false);           //curent token on ":"
        }
        //else the end separator is :
        else{
            scan.getNext();                         //get first token of limit expr
            //check missing limit or not
            if(scan.currentToken.tokenStr.equals(":"))
                error("Missing 'LIMIT' expression for the loop");
            limit = expr(":", false);           //current token on ":"
            incr.value = "1";
            incr.type = SubClassif.INTEGER;
        }

        //AT THIS STEP:----------------------------------------
        //  - DONE WITH GETTING INFO                          -
        //  - PREPING EXECUTION                               -
        //-----------------------------------------------------

        // we will use the line number as key to the storage manager to save the counting variable
        // try to access it, if catch mean it doesn't exist, just create one
        //      if pass, use that counting variable to continue
        try{
            cv = this.storageMgr.getVariable(this,Integer.toString(saveLineNr));
            ResultValue cv_stg = new ResultValue();
            cv_stg.type = cv.type;
            cv_stg.value = cv.value;
            this.storageMgr.replace(this,cv_token_str,cv_stg);

        }catch(Exception e){
            // if doesn't exist initialized it with new cv
            ResultValue cv_stg = new ResultValue();
            cv_stg.type = cv.type;
            cv_stg.value = cv.value;
            this.storageMgr.storage.put(Integer.toString(saveLineNr),cv_stg);
        }


        //when the code need to be exec
        if(bExec){
            // while the condition is still true ==> execute and inc counter
            ResultValue resCond = new ResultValue();
            resCond = Utility.less(this, cv, limit);
            if(resCond.value.equals("T") ) {
                ResultValue res = executeStatements(true);

                //if user change the counting variable ==> 2 values are not the same ==> update
                if(!(this.storageMgr.getVariable(this,cv_token_str).value.equals(this.storageMgr.getVariable(this,Integer.toString(saveLineNr)).value))){
                    this.storageMgr.replace(this,Integer.toString(saveLineNr),this.storageMgr.getVariable(this,cv_token_str));
                }
                //update cv value and replace the one in the storageMng
                cv.value = String.valueOf(Integer.parseInt(cv.value) + Integer.parseInt(incr.value));
                this.storageMgr.replace(this,Integer.toString(saveLineNr),cv);
                this.storageMgr.replace(this,cv_token_str,cv);
                //After executing: check if it has end for

                if(scan.currentToken.tokenStr.equals("break")){
                    //ResultValue resTemp = executeStatements(false);
                    while(!scan.currentToken.tokenStr.equals("endfor")){
                        scan.getNext();
                    }
                    return resCond;
                }

                if(!(scan.currentToken.tokenStr.equals("endfor")))
                {
                    error("Expected 'endfor' for a 'for' beginning line '%d'", saveLineNr);
                }
                if(!(scan.nextToken.tokenStr.equals(";")))
                {
                    error("Expected ‘;’ after ‘endfor’");
                }
                scan.iSourceLineNr = saveLineNr -1 ;
            }
            else {
                ResultValue resTemp = executeStatements(false);
                // when the loop is done or fail just remove the counting var instance in that loop (like a scope of the cv)
                this.storageMgr.storage.remove(Integer.toString(saveLineNr));

                //After executing: check if it has end for
                if(!(scan.currentToken.tokenStr.equals("endfor")))
                {
                    error("Expected 'endfor' for a 'for' beginning line '%d'", saveLineNr);
                }
                if(!(scan.getNext().equals(";")))
                {
                    error("Expected ‘;’ after ‘endfor’");
                }
            }
            return resCond;
        }
        else {
            ResultValue resTemp = executeStatements(false);
            // when the loop is done or fail just remove the counting var instance in that loop (like a scope of the cv)
            this.storageMgr.storage.remove(Integer.toString(saveLineNr));
            //After executing: check if it has end for
            if (!(scan.currentToken.tokenStr.equals("endfor"))) {
                error("Expected 'endfor' for a 'for' beginning line '%d'", saveLineNr);
            }
            if (!(scan.getNext().equals(";"))) {
                error("Expected ‘;’ after ‘endfor’");
            }
            return resTemp;
        }

    }// END OF FOR TO STATEMENT

    /**
     * - Take a ResultValue, which is going to be the key in the hashmap
     * for arrays in meatbol, and an ArrayList of ResultValue, which
     * is the value in the hashmap for arrays in meatbol.
     * - This function will parse and assign corresponding elements until
     * it hits a semicolon
     * - The declared array type is based on ResultValue res
     *
     * @param res           ResultValue: contains array's name and its type (fixed, unbound)
     * @param arrayM        ArrayList<ResultValue>
     * @throws Exception
     *      - If element has type conflicting with declared type
     */
    private int iAssignArrayElement(ResultValue res, ArrayList<ResultValue> arrayM) throws Exception
    {
        int iArraySize = 0;
        // assign array based on type
        while (!scan.getNext().equals(";")) {
            if (scan.currentToken.tokenStr.equals(","))
                continue;
            else {

                iArraySize++;

                // verify element type
                if (res.type == SubClassif.INTEGER || res.type == SubClassif.FLOAT)
                    Utility.toFloat(this, scan.currentToken.toResult());
                else if ((scan.currentToken.primClassif != Classif.OPERAND ||
                        scan.currentToken.subClassif != SubClassif.IDENTIFIER) &&
                        scan.currentToken.subClassif != res.type) {
                    error("Conflicting declaration type '%s'", scan.currentToken.tokenStr);
                }

                // temporary value for array element
                ResultValue temp = new ResultValue();

                switch (scan.currentToken.idenClassif) {

                    // if token is a primitive
                    case PRIMITIVE:
                        switch (scan.currentToken.subClassif) {
                            case IDENTIFIER:
                                temp = storageMgr.getVariable(this, scan.currentToken.tokenStr);
                                arrayM.add(temp);
                                break;
                            case FLOAT:
                                temp.structure = IdenClassif.PRIMITIVE;
                                temp.type = SubClassif.FLOAT;
                                temp.value = scan.currentToken.tokenStr;
                                temp.terminatingStr = "";
                                Utility.toFloat(this, temp);
                                arrayM.add(temp);
                                break;
                            case INTEGER:
                                temp.structure = IdenClassif.PRIMITIVE;
                                temp.type = SubClassif.INTEGER;
                                temp.value = scan.currentToken.tokenStr;
                                temp.terminatingStr = "";
                                Utility.toInt(this, temp);
                                arrayM.add(temp);
                                break;
                            case STRING:
                                temp.structure = IdenClassif.PRIMITIVE;
                                temp.type = SubClassif.STRING;
                                temp.value = scan.currentToken.tokenStr;
                                temp.terminatingStr = "";
                                arrayM.add(temp);
                                break;
                            case BOOLEAN:
                                break;
                            case DATE:
                                break;
                            default:
                                error("Invalid sub-classification for token");
                                break;
                        }
                        break;
                    // if token is a fixed array
                    case FIXED_ARRAY:

                        break;
                    // if token is a fixed array
                    case UNBOUND_ARRAY:
                        break;
                    default:
                        error("Invalid identifier classification");
                        break;
                }

            }
        } // end while
        return iArraySize;
    } // END assignArrayElement

    /**
     * - Take a ResultValue, which is going to be the key in the hashmap
     * for arrays in meatbol, and an ArrayList of ResultValue, which
     * is the value in the hashmap for arrays in meatbol.
     * - This function will parse and assign corresponding elements until
     * it hits a semicolon
     * - The declared array type is based on ResultValue res
     *
     * @param res           ResultValue: contains array's name and its type (fixed, unbound)
     * @param arrayM        ArrayList<ResultValue>
     * @throws Exception
     *      - If element has type conflicting with declared type
     */
    private void assignArrayElement(ResultValue res, ArrayList<ResultValue> arrayM) throws Exception
    {
        // assign array based on type
        while (!scan.getNext().equals(";")) {
            if (scan.currentToken.tokenStr.equals(","))
                continue;
            else {

                // verify element type
                if (scan.currentToken.subClassif == SubClassif.INTEGER ||
                        scan.currentToken.subClassif == SubClassif.FLOAT)
                    Utility.toFloat(this, scan.currentToken.toResult());
                else if (scan.currentToken.subClassif != SubClassif.IDENTIFIER &&
                        scan.currentToken.subClassif != res.type) {
                    error("Conflicting declaration type '%s'", scan.currentToken.tokenStr);
                }

                // temporary value for array element
                ResultValue temp = new ResultValue();

                switch (scan.currentToken.idenClassif) {

                    // if token is a primitive
                    case PRIMITIVE:
                        switch (scan.currentToken.subClassif) {
                            case IDENTIFIER:
                                temp = storageMgr.getVariable(this, scan.currentToken.tokenStr);
                                arrayM.add(temp);
                                break;
                            case FLOAT:
                                temp = scan.currentToken.toResult();
                                temp = Utility.toFloat(this, temp);
                                temp.structure = IdenClassif.PRIMITIVE;
                                arrayM.add(temp);
                                break;
                            case INTEGER:
                                temp = scan.currentToken.toResult();
                                temp = Utility.toInt(this, temp);
                                temp.structure = IdenClassif.PRIMITIVE;
                                arrayM.add(temp);
                                break;
                            case STRING:
                            case BOOLEAN:
                            case DATE:
                                temp = scan.currentToken.toResult();
                                temp.structure = IdenClassif.PRIMITIVE;
                                arrayM.add(temp);
                                break;
                            default:
                                error("Invalid sub-classification for token");
                                break;
                        }
                        break;
                    // if token is a fixed array
                    case FIXED_ARRAY:

                        break;
                    // if token is a fixed array
                    case UNBOUND_ARRAY:
                        break;
                    default:
                        error("Invalid identifier classification");
                        break;
                }

            }
        } // end while
    } // END assignArrayElement
}
