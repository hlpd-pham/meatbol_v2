package meatbol;

import java.util.ArrayList;
import java.util.Stack;

public class Expression {

    //Stack Out has the postfix expression within it already. We get this from expr(); FYI
    public static ResultValue expression(Parser parser, Stack<ResultValue> Out) throws Exception {
        Stack<ResultValue> resultStack = new Stack<>();
        ResultValue res = new ResultValue();

        ResultValue op1 = new ResultValue();
        ResultValue op2 = new ResultValue();

        for (int i = 0; i < Out.size(); i++) { //Loop through out, we need to make the postfix
            //System.out.printf("Expression: %s  (%d/%d)\n",Out.get(i).value, i, Out.size()-1);

            ResultValue temp = Out.get(i);

            // access array element
            if (temp.structure == IdenClassif.FIXED_ARRAY ||
                    temp.structure == IdenClassif.UNBOUND_ARRAY) {

                ArrayList<ResultValue> tempArray = new ArrayList<>();

                if (!temp.value.contains("["))
                    tempArray = parser.storageMgr.getArray(parser, temp.value + "[");
                else
                    tempArray = parser.storageMgr.getArray(parser, temp.value);

                if (!resultStack.empty()) {
                    ResultValue index = resultStack.pop();
                    try {
                        Utility.toInt(parser, index);
                        int iArrayIndex = Integer.parseInt(index.value);

                        // negative subscript
                        if (iArrayIndex < 0)
                        {
                            ResultValue resELEM = Utility.ELEM(parser, temp);
                            int maxPopulated = Integer.parseInt(resELEM.value);
                            iArrayIndex = maxPopulated + iArrayIndex;
                        }

                        resultStack.push(tempArray.get(iArrayIndex));
                    }
                    catch (ParserException e) {
                        parser.error("index for accessing array must be an integer");
                    }
                }
                else
                    if (i == Out.size() - 1)
                        parser.error("No element access when referencing array");
                    else
                        if (Out.get(i+1).type == SubClassif.BUILTIN)
                            resultStack.push(temp);

                continue;
            }

            switch(temp.type){
                case INTEGER:
                    resultStack.push(temp);
                    //System.out.printf("Pushing INT\n");
                    break;
                case FLOAT:
                    resultStack.push(temp);
                    //System.out.printf("Pushing FLOAT\n");
                    break;
                case STRING:
                    //System.out.printf("Pushing STRING\n");
                    resultStack.push(temp);
                    break;
                case DATE:
                    resultStack.push(temp);
                    break;
                case BUILTIN:
                    // TODO built in functions for expression

                    if (resultStack.isEmpty())
                        parser.error("No parameters passed into function '%s'", temp.value);

                    ResultValue funcArg = resultStack.pop();
                    ResultValue funcArg2 = new ResultValue();

                    switch (temp.value) {
                        case "LENGTH":
                            resultStack.push(Utility.LENGTH(parser, funcArg));
                            break;
                        case "SPACES":
                            resultStack.push(Utility.SPACES(parser, funcArg));
                            break;
                        case "ELEM":
                            if (!funcArg.value.contains("["))
                                funcArg.value += "[";
                            resultStack.push(Utility.ELEM(parser, funcArg));
                            break;
                        case "MAXELEM":
                            if (!funcArg.value.contains("["))
                                funcArg.value += "[";
                            resultStack.push(Utility.MAX(parser, funcArg));
                            break;
                        case "dateDiff":
                            funcArg2 = resultStack.pop();
                            resultStack.push(Utility.dateDiff(parser, funcArg, funcArg2));
                            break;
                        default:
                            break;
                    }
                default:
                    break;
            }
            switch (temp.value) {
                case "+":
                    op2 = resultStack.pop();
                    op1 = resultStack.pop();
                    //System.out.printf("%s+++%s\n", op1.value, op2.value);
                    res = Utility.add(parser, op1, op2);
                    resultStack.add(res);
                    break;
                case "-":
                    op2 = resultStack.pop();
                    op1 = resultStack.pop();
                    //System.out.printf("%s---%s\n", op1.value, op2.value);
                    res = Utility.subtract(parser, op1, op2);
                    resultStack.add(res);
                    break;
                case "*":
                    op2 = resultStack.pop();
                    op1 = resultStack.pop();
                    //System.out.printf("%s***%s\n", op1.value, op2.value);
                    res = Utility.multiply(parser, op1, op2);
                    resultStack.add(res);
                    break;
                case "/":
                    op2 = resultStack.pop();
                    op1 = resultStack.pop();
                    //System.out.printf("%s///%s\n", op1.value, op2.value);
                    res = Utility.divide(parser, op1, op2);
                    resultStack.add(res);
                    break;
                case "^":
                    op2 = resultStack.pop();
                    op1 = resultStack.pop();
                    //System.out.printf("%s^^^%s\n", op1.value, op2.value);
                    res = Utility.power(parser, op1, op2);
                    resultStack.add(res);
                    break;
                case "#":
                    op2 = resultStack.pop();
                    op1 = resultStack.pop();
                    //System.out.printf("%s###%s\n", op1.value, op2.value);
                    res = Utility.concat(parser, op1, op2);
                    resultStack.add(res);
                    break;
                case "~":
                    resultStack.push(temp);
                    break;
                default:
                    //System.out.printf("OOF\n");
                    //Parser.error("Unknown operator");
                    break;
            }

        }
            if(resultStack.size()==1){
                op1 = resultStack.pop();
                return op1;
            }
            // access character in string
            if (resultStack.size()==2)
            {
                // string token
                op1 = resultStack.pop();
                // integer token for accessing string
                op2 = resultStack.pop();

                // check if op2 is an integer
                try {
                    Utility.toInt(parser, op2);
                }
                catch(ParserException e)
                {
                    parser.error("Index of string must be integer");
                }

                // sanity check
                if (op1.type != SubClassif.STRING)
                    parser.error("Token '%s' is not string type", op1.value);

                // let's go!!!!!!!!!!!!!
                int iStringIndex = Integer.parseInt(op2.value);
                char temp = op1.value.charAt(iStringIndex);
                res.value = String.valueOf(temp);
                return res;
            }
            // array slice
            else {
                // case 1: arrayM[op1~op2]
                if (resultStack.size() == 4)
                {
                    // array slice for string
                    if (resultStack.get(0).type == SubClassif.INTEGER &&
                        resultStack.get(2).type == SubClassif.INTEGER  &&
                        resultStack.get(1).type == SubClassif.ARRAY_SLICE &&
                        resultStack.get(3).type == SubClassif.STRING)
                    {
                        int iStartSlicining = Integer.parseInt(resultStack.get(0).value);
                        int iEndSlicing = Integer.parseInt(resultStack.get(2).value);
                        String sbIdentifierVal = resultStack.get(3).value;
                        StringBuilder sbReturnVal = new StringBuilder();

                        // subscript check
                        if (iStartSlicining > iEndSlicing)
                            parser.error("Array error: starting subscript cannot be smaller that ending subscript");

                        // boundary check
                        if (iEndSlicing > (sbIdentifierVal.length()-1) ||
                                iStartSlicining > (sbIdentifierVal.length()-1) )
                            parser.error("Array error: Index out of bound");

                        // array slicing doesn't support negative subscript
                        if (iStartSlicining < 0 || iEndSlicing < 0)
                            parser.error("Array error: Index for array slicing cannot be negative");

                        // build the slice for string
                        for (int i = iStartSlicining; i < iEndSlicing; i++)
                            sbReturnVal.append(sbIdentifierVal.charAt(i));


                        res.type = SubClassif.STRING;
                        res.value = sbReturnVal.toString();
                        res.structure = IdenClassif.PRIMITIVE;

                        return res;
                    }
                    else if (resultStack.get(0).type == SubClassif.INTEGER &&
                            resultStack.get(2).type == SubClassif.INTEGER  &&
                            resultStack.get(1).type == SubClassif.ARRAY_SLICE &&
                            (   resultStack.get(3).structure == IdenClassif.UNBOUND_ARRAY ||
                                resultStack.get(3).structure == IdenClassif.FIXED_ARRAY))
                    {
                        int iStartSlicining = Integer.parseInt(resultStack.get(0).value);
                        int iEndSlicing = Integer.parseInt(resultStack.get(1).value);

                        // subscript check
                        if (iStartSlicining > iEndSlicing)
                            parser.error("Array error: starting subscript cannot be smaller that ending subscript");

//                        // boundary check
//                        if (iEndSlicing > (sbIdentifierVal.length()-1) ||
//                                iStartSlicining > (sbIdentifierVal.length()-1) )
//                            parser.error("Array error: Index out of bound");

                        // array slicing doesn't support negative subscript
                        if (iStartSlicining < 0 || iEndSlicing < 0)
                            parser.error("Array error: Index for array slicing cannot be negative");
                    }
                    else
                        parser.error("Array Error: Invalid expression for array slicing");
                }
                // case 2: arrayM[op1~]
                // case 3: arrayM[~op1]
                else if (resultStack.size() == 3 && resultStack.get(2).type == SubClassif.STRING) {

                    // boolean flag to choose case 2 or 3
                    boolean bSubscriptStart = false;
                    String sbIdentifierVal = resultStack.get(2).value;
                    StringBuilder sbReturnVal = new StringBuilder();

                    if (resultStack.get(0).type == SubClassif.INTEGER &&
                        resultStack.get(1).type == SubClassif.ARRAY_SLICE)
                        bSubscriptStart = true;
                    else if (resultStack.get(1).type == SubClassif.INTEGER &&
                            resultStack.get(0).type == SubClassif.ARRAY_SLICE)
                        bSubscriptStart = false;
                    else
                        parser.error("Array Error: Invalid expression for array slicing");

                    int iSubscript;

                    // assign value for iSubscript
                    if (bSubscriptStart)
                        iSubscript = Integer.parseInt(resultStack.get(0).value);
                    else
                        iSubscript = Integer.parseInt(resultStack.get(1).value);

                    // subscript check
                    if (iSubscript < 0)
                        parser.error("Array error: Index for array slicing cannot be negative");

                    // boundary check
                    if (iSubscript > (sbIdentifierVal.length()-1))
                        parser.error("Array error: Index out of bound");

                    // get the substring
                    if (bSubscriptStart)
                    {
                        // build the slice for string
                        for (int i = iSubscript; i < sbIdentifierVal.length(); i++)
                            sbReturnVal.append(sbIdentifierVal.charAt(i));
                    }
                    else
                    {
                        // build the slice for string
                        for (int i = 0; i < iSubscript; i++)
                            sbReturnVal.append(sbIdentifierVal.charAt(i));
                    }

                    res.type = SubClassif.STRING;
                    res.value = sbReturnVal.toString();
                    res.structure = IdenClassif.PRIMITIVE;

                    return res;

                }
                else
                    parser.error("Array Error: Invalid expression for array slicing");

            }

        return op1;
    }
    public int precedence(Parser parser) throws Exception {
        return getPrecedence(parser, false);
    }
    public int stkPrecedence(Parser parser) throws Exception {
        return getPrecedence(parser, true);
    }
    private int getPrecedence(Parser parser, boolean bStkPreced) throws Exception {
        if(parser.scan.currentToken.primClassif == Classif.OPERAND)
        {
            if(bStkPreced)
                return 0;
            else
                return 16;
        }
        if(parser.scan.currentToken.primClassif == Classif.FUNCTION)
        {
            if(bStkPreced)
                return 2;
            else
                return 15;
        }
        switch(parser.scan.currentToken.tokenStr)
        {
            case "(":
            case "{":
                if(bStkPreced)
                    return 2;
                else
                    return 15;
            case "-":
                if(parser.scan.currentToken.subClassif == SubClassif.UNARYMINUS)
                    return 12;
                else
                    return 8;
            case "^":
                if (bStkPreced)
                    return 10;
                else
                    return 11;
            case "~":
                return 14;
            case "*":
            case "/":
                return 9;
            case "+":
                return 8;
            case "#":
                return 7;
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "==":
            case "!=":
            case "in":
            case "notin":
                return 6;
            case "not":
                return 5;
            case "and":
            case "or":
                return 4;
            default:
                parser.error("Expected a precedence value, %s", parser.scan.currentToken.tokenStr);
                return -1;

        }
    }
}

