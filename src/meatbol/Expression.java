package meatbol;

import java.util.ArrayList;
import java.util.Stack;

public class Expression {

    //Stack Out has the postfix expression within it already. We get this from expr(); FYI
    public static ResultValue expression(Parser parser, Stack<ResultValue> Out) throws Exception {
        Stack<ResultValue> postfixStack = new Stack<>();
        Stack<ResultValue> resultStack = new Stack<>();
        ResultValue res = new ResultValue();

        ResultValue op1 = new ResultValue();
        ResultValue op2 = new ResultValue();

        boolean bLengthFunc = false;
        boolean bSpaceFunc, bElemFunc, bMaxElemFunc = false;

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
                case BUILTIN:
                    // TODO built in functions for expression

                    if (resultStack.isEmpty())
                        parser.error("No parameters passed into function '%s'", temp.value);

                    ResultValue funcArg = resultStack.pop();

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
            else
            {
                //ignore
                //System.out.printf("You did something wrong, resultStack still has %d elements\n", resultStack.size());
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

