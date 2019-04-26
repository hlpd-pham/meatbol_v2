package meatbol;

import javax.xml.transform.Result;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.abs;

public class Utility {

    /*******************************************************************************************************************
     *ADD Numeric function: ADD 2 numeric value and return a result value.
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of the actual calculation value
     * with its type depending on the first operand type
     * @param parser        -   provide a error handling method
     * @param res01         -   the numeric operand 1
     * @param res02         -   the numeric operand 2
     * @return  res         -   the result value of the calculation and type based on the first operand
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue add(Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if the two operands are int/float/string that can be converted into a numeric then cont
        // if not then the numeric class will handle the error of wrong data type for the operation
        Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
        Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");

        // if first operand is an INT ==> add INT
        if(res01.type == SubClassif.INTEGER)
        {
            int temp;
            temp = nOp1.integervalue + nOp2.integervalue;
            res.value = String.valueOf(temp);
        }
        // if first operand is a FLOAT ==> add FLOAT
        else if(res01.type == SubClassif.FLOAT)
        {
            double temp;
            temp = nOp1.doubleValue + nOp2.doubleValue;
            res.value = String.valueOf(temp);
        }
        // if first operand is a STRING then check is it an int / float
        // if the control flow can reach this point w/o raising error then this is a numeric String
        else if(res01.type == SubClassif.STRING){

            // if first operand is a INT STRING ==> add INT
            if (nOp1.type == SubClassif.INTEGER)
            {
                int temp;
                temp = nOp1.integervalue + nOp2.integervalue;
                res.value = String.valueOf(temp);
            }
            // if first operand is a FLOAT STRING ==> add FLOAT
            else if(nOp1.type == SubClassif.FLOAT)
            {
                double temp;
                temp = nOp1.doubleValue + nOp2.doubleValue;
                res.value = String.valueOf(temp);
            }
        }

        // The result send out will be the same data type as the operand1
        res.type = nOp1.type;
        return res;

    }// END OF ADD METHOD

    /*******************************************************************************************************************
     *SUBTRACT Numeric function: SUBTRACT 2 numeric value and return a result value.
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of the actual calculation value
     * with its type depending on the first operand type
     * @param parser        -   provide a error handling method
     * @param res01         -   the numeric operand 1
     * @param res02         -   the numeric operand 2
     * @return  res         -   the result value of the calculation and type based on the first operand
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue subtract(Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if the two operands are int/float/string that can be converted into a numeric then cont
        // if not then the numeric class will handle the error of wrong data type for the operation
        Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
        Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");

        // if first operand is an INT ==> subtract INT
        if(res01.type == SubClassif.INTEGER)
        {
            int temp;
            temp = nOp1.integervalue - nOp2.integervalue;
            res.value = String.valueOf(temp);
        }
        // if first operand is a FLOAT ==> subtract FLOAT
        else if(res01.type == SubClassif.FLOAT)
        {
            double temp;
            temp = nOp1.doubleValue - nOp2.doubleValue;
            res.value = String.valueOf(temp);
        }
        // if first operand is a STRING then check is it an int / float
        // if the control flow can reach this point w/o raising error then this is a numeric String
        else if(res01.type == SubClassif.STRING){

            // if first operand is a INT STRING ==> subtract INT
            if (nOp1.type == SubClassif.INTEGER)
            {
                int temp;
                temp = nOp1.integervalue - nOp2.integervalue;
                res.value = String.valueOf(temp);
            }
            // if first operand is a FLOAT STRING ==> subtract FLOAT
            else if(nOp1.type == SubClassif.FLOAT)
            {
                double temp;
                temp = nOp1.doubleValue - nOp2.doubleValue;
                res.value = String.valueOf(temp);
            }
        }

        // The result send out will be the same data type as the operand1
        res.type = nOp1.type;
        return res;

    }// END OF SUBTRACT METHOD

    /*******************************************************************************************************************
     *MULTIPLY Numeric function: MULTIPLY 2 numeric value and return a result value.
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of the actual calculation value
     * with its type depending on the first operand type
     * @param parser        -   provide a error handling method
     * @param res01         -   the numeric operand 1
     * @param res02         -   the numeric operand 2
     * @return  res         -   the result value of the calculation and type based on the first operand
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue multiply(Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if the two operands are int/float/string that can be converted into a numeric then cont
        // if not then the numeric class will handle the error of wrong data type for the operation
        Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
        Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");

        // if first operand is an INT ==> multiply INT
        if(res01.type == SubClassif.INTEGER)
        {
            int temp;
            temp = nOp1.integervalue * nOp2.integervalue;
            res.value = String.valueOf(temp);
        }
        // if first operand is a FLOAT ==> multiply FLOAT
        else if(res01.type == SubClassif.FLOAT)
        {
            double temp;
            temp = nOp1.doubleValue * nOp2.doubleValue;
            res.value = String.valueOf(temp);
        }
        // if first operand is a STRING then check is it an int / float
        // if the control flow can reach this point w/o raising error then this is a numeric String
        else if(res01.type == SubClassif.STRING){

            // if first operand is a INT STRING ==> multiply INT
            if (nOp1.type == SubClassif.INTEGER)
            {
                int temp;
                temp = nOp1.integervalue * nOp2.integervalue;
                res.value = String.valueOf(temp);
            }
            // if first operand is a FLOAT STRING ==> multiply FLOAT
            else if(nOp1.type == SubClassif.FLOAT)
            {
                double temp;
                temp = nOp1.doubleValue * nOp2.doubleValue;
                res.value = String.valueOf(temp);
            }
        }

        // The result send out will be the same data type as the operand1
        res.type = nOp1.type;
        return res;

    }// END OF MULTIPLY METHOD

    /*******************************************************************************************************************
     *DIVIDE Numeric function: DIVIDE 2 numeric value and return a result value.
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of the actual calculation value
     * with its type depending on the first operand type
     * @param parser        -   provide a error handling method
     * @param res01         -   the numeric operand 1
     * @param res02         -   the numeric operand 2
     * @return  res         -   the result value of the calculation and type based on the first operand
     * @throws Exception    -   the exception of non numeric String and divided by 0
     */
    public static ResultValue divide(Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if the two operands are int/float/string that can be converted into a numeric then cont
        // if not then the numeric class will handle the error of wrong data type for the operation
        Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
        Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");

        // if first operand is an INT ==> divide INT
        if(res01.type == SubClassif.INTEGER)
        {
            int temp;
            try{
                temp = nOp1.integervalue / nOp2.integervalue;
                res.value = String.valueOf(temp);

            }catch(Exception e){
                parser.error("Line '%d': Can not divide by 0", parser.iLineNr);
            }
        }
        // if first operand is a FLOAT ==> divide FLOAT
        else if(res01.type == SubClassif.FLOAT)
        {
            double temp;
            try{
                temp = nOp1.doubleValue / nOp2.doubleValue;
                res.value = String.valueOf(temp);
            }catch(Exception e){
                parser.error("Line '%d': Can not divide by 0", parser.iLineNr);
            }
        }
        // if first operand is a STRING then check is it an int / float
        // if the control flow can reach this point w/o raising error then this is a numeric String
        else if(res01.type == SubClassif.STRING){

            // if first operand is a INT STRING ==> divide INT
            if (nOp1.type == SubClassif.INTEGER)
            {
                int temp;
                try{
                    temp = nOp1.integervalue / nOp2.integervalue;
                    res.value = String.valueOf(temp);

                }catch(Exception e){
                    parser.error("Line '%d': Can not divide by 0", parser.iLineNr);
                }
            }
            // if first operand is a FLOAT STRING ==> divide FLOAT
            else if(nOp1.type == SubClassif.FLOAT)
            {
                double temp;
                try{
                    temp = nOp1.doubleValue / nOp2.doubleValue;
                    res.value = String.valueOf(temp);
                }catch(Exception e){
                    parser.error("Line '%d': Can not divide by 0", parser.iLineNr);
                }
            }
        }

        // The result send out will be the same data type as the operand1
        res.type = nOp1.type;
        return res;

    }// END OF DIVIDE METHOD

    /*******************************************************************************************************************
     *POWER Numeric function: Raise the first operand to the second operand.
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of the actual calculation value
     * with its type depending on the first operand type
     * @param parser        -   provide a error handling method
     * @param res01         -   the numeric operand 1
     * @param res02         -   the numeric operand 2
     * @return  res         -   the result value of the calculation and type based on the first operand
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue power(Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if the two operands are int/float/string that can be converted into a numeric then cont
        // if not then the numeric class will handle the error of wrong data type for the operation
        Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
        Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");

        // if first operand is an INT ==> power INT
        if (res01.type == SubClassif.INTEGER) {
            int temp;
            temp = (int) Math.pow((double) nOp1.integervalue, (double) nOp2.integervalue);
            res.value = String.valueOf(temp);
        }
        // if first operand is a FLOAT ==> power FLOAT
        else if (res01.type == SubClassif.FLOAT) {
            double temp;
            temp = Math.pow(nOp1.doubleValue, nOp2.doubleValue);
            res.value = String.valueOf(temp);
        }
        // if first operand is a STRING then check is it an int / float
        // if the control flow can reach this point w/o raising error then this is a numeric String
        else if (res01.type == SubClassif.STRING) {

            // if first operand is a INT STRING ==> power INT
            if (nOp1.type == SubClassif.INTEGER) {
                int temp;
                temp = (int) Math.pow((double) nOp1.integervalue, (double) nOp2.integervalue);
                res.value = String.valueOf(temp);
            }
            // if first operand is a FLOAT STRING ==> power FLOAT
            else if (nOp1.type == SubClassif.FLOAT) {
                double temp;
                temp = Math.pow(nOp1.doubleValue, nOp2.doubleValue);
                res.value = String.valueOf(temp);
            }
        }

        // The result send out will be the same data type as the operand1
        res.type = nOp1.type;
        return res;
    }// END OF POWER METHOD

    /*******************************************************************************************************************
     *UNARY MINUS Numeric function: Apply a unary minus on an operand
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of the actual calculation value
     * with its type depending on the first operand type
     * @param parser        -   provide a error handling method
     * @param res01         -   the numeric operand 1
     * @return  res         -   the result value of the calculation and type based on the first operand
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue UMinus(Parser parser, ResultValue res01) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }

        // if the operand í int/float/string that can be converted into a numeric then cont
        // if not then the numeric class will handle the error of wrong data type for the operation
        Numeric nOp1 = new Numeric(parser, res01, "1st Operand");

        // if first operand is an INT ==> negate INT
        if (res01.type == SubClassif.INTEGER) {
            int temp;
            temp = -1 * nOp1.integervalue;
            res.value = String.valueOf(temp);
        }
        // if first operand is a FLOAT ==> negate FLOAT
        else if (res01.type == SubClassif.FLOAT) {
            double temp;
            temp = -1 * nOp1.doubleValue;
            res.value = String.valueOf(temp);
        }
        // if first operand is a STRING then check is it an int / float
        // if the control flow can reach this point w/o raising error then this is a numeric String
        else if (res01.type == SubClassif.STRING) {

            // if first operand is a INT STRING ==> negate INT
            if (nOp1.type == SubClassif.INTEGER) {
                int temp;
                temp = -1 * nOp1.integervalue;
                res.value = String.valueOf(temp);
            }
            // if first operand is a FLOAT STRING ==> negate FLOAT
            else if (nOp1.type == SubClassif.FLOAT) {
                double temp;
                temp = -1 * nOp1.doubleValue;
                res.value = String.valueOf(temp);
            }
        }

        // The result send out will be the same data type as the operand1
        res.type = res01.type;
        return res;
    }// END OF UNARY MINUS METHOD

    /*******************************************************************************************************************
     *EQUIVALENT function: Check if 2 operand is the same
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of T or F from the comparison
     * with depending on the first operand type but the result value data type is always boolean
     * @param parser        -   provide a error handling method
     * @param res01         -   the result value operand 1
     * @param res02         -   the result value operand 2
     * @return  res         -   the result value of the comparison and type is boolean
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue equi(Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }


        // if numeric comparison
        if (res01.type == SubClassif.INTEGER || res01.type == SubClassif.FLOAT){

            // if the two operands are int/float/string that can be converted into a numeric then cont
            // if not then the numeric class will handle the error of wrong data type for the operation
            Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
            Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");
            // if first operand is an INT ==> power INT
            if (res01.type == SubClassif.INTEGER) {
                if (nOp1.integervalue == nOp2.integervalue)
                    res.value = "T";
                else
                    res.value = "F";

            }
            // if first operand is a FLOAT ==> power FLOAT
            else if (res01.type == SubClassif.FLOAT) {
                if (nOp1.doubleValue == nOp2.doubleValue)
                    res.value = "T";
                else
                    res.value = "F";

            }
        }
        //if the first operand is a string
        else if(res01.type == SubClassif.STRING){
            if(res02.type == SubClassif.INTEGER || res02.type == SubClassif.FLOAT || res02.type == SubClassif.STRING){

                //Sring comparison
                if(res01.value.equals(res02.value))
                    res.value = "T";
                else
                    res.value = "F";
            }
            else{
                parser.error("Line '%d': Expecting STRING, INTEGER, FLOAT for the second operand but received '%s' data type.",
                        parser.iLineNr, res02.type.toString());
            }
        }
        else{

            parser.error("Line '%d': Expecting type STRING, INTEGER, FLOAT for the first operand but received '%s' data type.",
                    parser.iLineNr, res01.type.toString());
        }

        // The result send out will be the same data type as the operand1
        res.type = SubClassif.BOOLEAN;
        return res;
    }// END OF EQUI METHOD

    /*******************************************************************************************************************
     *NOT EQUIVALENT function: Check if 2 operand is NOT the same
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of T or F from the comparison
     * with depending on the first operand type but the result value data type is always boolean
     * @param parser        -   provide a error handling method
     * @param res01         -   the result value operand 1
     * @param res02         -   the result value operand 2
     * @return  res         -   the result value of the comparison and type is boolean
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue nEqui(Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if numeric comparison
        if (res01.type == SubClassif.INTEGER || res01.type == SubClassif.FLOAT){

            // if the two operands are int/float/string that can be converted into a numeric then cont
            // if not then the numeric class will handle the error of wrong data type for the operation
            Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
            Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");
            // if first operand is an INT ==> power INT
            if (res01.type == SubClassif.INTEGER) {
                if (nOp1.integervalue != nOp2.integervalue)
                    res.value = "T";
                else
                    res.value = "F";

            }
            // if first operand is a FLOAT ==> power FLOAT
            else if (res01.type == SubClassif.FLOAT) {
                if (nOp1.doubleValue != nOp2.doubleValue)
                    res.value = "T";
                else
                    res.value = "F";

            }
        }
        //if the first operand is a string
        else if(res01.type == SubClassif.STRING){
            if(res02.type == SubClassif.INTEGER || res02.type == SubClassif.FLOAT || res02.type == SubClassif.STRING){

                //String comparison
                if(!(res01.value.equals(res02.value)))
                    res.value = "T";
                else
                    res.value = "F";
            }
            else{
                parser.error("Line '%d': Expecting STRING, INTEGER, FLOAT for the second operand but received '%s' data type.",
                        parser.iLineNr, res02.type.toString());
            }
        }
        else{
            parser.error("Line '%d': Expecting type STRING, INTEGER, FLOAT for the first operand but received '%s' data type.",
                    parser.iLineNr, res01.type.toString());
        }

        // The result send out will be the same data type as the operand1
        res.type = SubClassif.BOOLEAN;
        return res;
    }// END OF NEQUI METHOD

    /*******************************************************************************************************************
     *LESS THAN function: Check if operand 1 is less than
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of T or F from the comparison
     * with depending on the first operand type but the result value data type is always boolean
     * @param parser        -   provide a error handling method
     * @param res01         -   the result value operand 1
     * @param res02         -   the result value operand 2
     * @return  res         -   the result value of the comparison and type is boolean
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue less(Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if numeric comparison
        if (res01.type == SubClassif.INTEGER || res01.type == SubClassif.FLOAT){

            // if the two operands are int/float/string that can be converted into a numeric then cont
            // if not then the numeric class will handle the error of wrong data type for the operation
            Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
            Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");
            // if first operand is an INT ==> power INT
            if (res01.type == SubClassif.INTEGER) {
                if (nOp1.integervalue < nOp2.integervalue)
                    res.value = "T";
                else
                    res.value = "F";

            }
            // if first operand is a FLOAT ==> power FLOAT
            else if (res01.type == SubClassif.FLOAT) {
                if (nOp1.doubleValue < nOp2.doubleValue)
                    res.value = "T";
                else
                    res.value = "F";

            }
        }
        //if the first operand is a string
        else if(res01.type == SubClassif.STRING){
            if(res02.type == SubClassif.INTEGER || res02.type == SubClassif.FLOAT || res02.type == SubClassif.STRING){

                //String comparison
                if(res01.value.compareTo(res02.value) < 0 )
                    res.value = "T";
                else
                    res.value = "F";
            }
            else{
                parser.error("Line '%d': Expecting STRING, INTEGER, FLOAT for the second operand but received '%s' data type.",
                        parser.iLineNr, res02.type.toString());
            }
        }
        else{
            parser.error("Line '%d': Expecting type STRING, INTEGER, FLOAT for the first operand but received '%s' data type.",
                    parser.iLineNr, res01.type.toString());
        }

        // The result send out will be the same data type as the operand1
        res.type = SubClassif.BOOLEAN;
        return res;
    }// END OF LESS THAN METHOD


    /*******************************************************************************************************************
     *LESS THAN EQUAL function: Check if operand 1 is less than or equal
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of T or F from the comparison
     * with depending on the first operand type but the result value data type is always boolean
     * @param parser        -   provide a error handling method
     * @param res01         -   the result value operand 1
     * @param res02         -   the result value operand 2
     * @return  res         -   the result value of the comparison and type is boolean
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue lEqui (Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if numeric comparison
        if (res01.type == SubClassif.INTEGER || res01.type == SubClassif.FLOAT){

            // if the two operands are int/float/string that can be converted into a numeric then cont
            // if not then the numeric class will handle the error of wrong data type for the operation
            Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
            Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");
            // if first operand is an INT ==> power INT
            if (res01.type == SubClassif.INTEGER) {
                if (nOp1.integervalue <= nOp2.integervalue)
                    res.value = "T";
                else
                    res.value = "F";

            }
            // if first operand is a FLOAT ==> power FLOAT
            else if (res01.type == SubClassif.FLOAT) {
                if (nOp1.doubleValue <= nOp2.doubleValue)
                    res.value = "T";
                else
                    res.value = "F";

            }
        }
        //if the first operand is a string
        else if(res01.type == SubClassif.STRING){
            if(res02.type == SubClassif.INTEGER || res02.type == SubClassif.FLOAT || res02.type == SubClassif.STRING){

                //String comparison
                if(res01.value.compareTo(res02.value) <= 0 )
                    res.value = "T";
                else
                    res.value = "F";
            }
            else{
                parser.error("Line '%d': Expecting STRING, INTEGER, FLOAT for the second operand but received '%s' data type.",
                        parser.iLineNr, res02.type.toString());
            }
        }
        else{
            parser.error("Line '%d': Expecting type STRING, INTEGER, FLOAT for the first operand but received '%s' data type.",
                    parser.iLineNr, res01.type.toString());
        }

        // The result send out will be the same data type as the operand1
        res.type = SubClassif.BOOLEAN;
        return res;
    }// END OF LESS THAN OR EQUAL METHOD


    /*******************************************************************************************************************
     *GREATER THAN function: Check if operand 1 is greater than
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of T or F from the comparison
     * with depending on the first operand type but the result value data type is always boolean
     * @param parser        -   provide a error handling method
     * @param res01         -   the result value operand 1
     * @param res02         -   the result value operand 2
     * @return  res         -   the result value of the comparison and type is boolean
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue more (Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if numeric comparison
        if (res01.type == SubClassif.INTEGER || res01.type == SubClassif.FLOAT){

            // if the two operands are int/float/string that can be converted into a numeric then cont
            // if not then the numeric class will handle the error of wrong data type for the operation
            Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
            Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");
            // if first operand is an INT ==> power INT
            if (res01.type == SubClassif.INTEGER) {
                if (nOp1.integervalue > nOp2.integervalue)
                    res.value = "T";
                else
                    res.value = "F";

            }
            // if first operand is a FLOAT ==> power FLOAT
            else if (res01.type == SubClassif.FLOAT) {
                if (nOp1.doubleValue > nOp2.doubleValue)
                    res.value = "T";
                else
                    res.value = "F";

            }
        }
        //if the first operand is a string
        else if(res01.type == SubClassif.STRING){
            if(res02.type == SubClassif.INTEGER || res02.type == SubClassif.FLOAT || res02.type == SubClassif.STRING){

                //String comparison
                if(res01.value.compareTo(res02.value) > 0 )
                    res.value = "T";
                else
                    res.value = "F";
            }
            else{
                parser.error("Line '%d': Expecting STRING, INTEGER, FLOAT for the second operand but received '%s' data type.",
                        parser.iLineNr, res02.type.toString());
            }
        }
        else{
            parser.error("Line '%d': Expecting type STRING, INTEGER, FLOAT for the first operand but received '%s' data type.",
                    parser.iLineNr, res01.type.toString());
        }

        // The result send out will be the same data type as the operand1
        res.type = SubClassif.BOOLEAN;
        return res;
    }// END OF GREATER THAN METHOD (aka. more)


    /*******************************************************************************************************************
     *GREATER THAN OR EQUAL function: Check if operand 1 is greater than or equal
     * The result is based on the first operand
     * <p>
     * The result value is populated with a string representation of T or F from the comparison
     * with depending on the first operand type but the result value data type is always boolean
     * @param parser        -   provide a error handling method
     * @param res01         -   the result value operand 1
     * @param res02         -   the result value operand 2
     * @return  res         -   the result value of the comparison and type is boolean
     * @throws Exception    -   the exception of non numeric String
     */
    public static ResultValue mEqui (Parser parser, ResultValue res01, ResultValue res02) throws Exception {

        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if numeric comparison
        if (res01.type == SubClassif.INTEGER || res01.type == SubClassif.FLOAT){

            // if the two operands are int/float/string that can be converted into a numeric then cont
            // if not then the numeric class will handle the error of wrong data type for the operation
            Numeric nOp1 = new Numeric(parser, res01, "1st Operand");
            Numeric nOp2 = new Numeric(parser, res02, "2nd Operand");
            // if first operand is an INT ==> power INT
            if (res01.type == SubClassif.INTEGER) {
                if (nOp1.integervalue >= nOp2.integervalue)
                    res.value = "T";
                else
                    res.value = "F";

            }
            // if first operand is a FLOAT ==> power FLOAT
            else if (res01.type == SubClassif.FLOAT) {
                if (nOp1.doubleValue >= nOp2.doubleValue)
                    res.value = "T";
                else
                    res.value = "F";

            }
        }
        //if the first operand is a string
        else if(res01.type == SubClassif.STRING){
            if(res02.type == SubClassif.INTEGER || res02.type == SubClassif.FLOAT || res02.type == SubClassif.STRING){

                //String comparison
                if(res01.value.compareTo(res02.value) >= 0 )
                    res.value = "T";
                else
                    res.value = "F";
            }
            else{
                parser.error("Line '%d': Expecting STRING, INTEGER, FLOAT for the second operand but received '%s' data type.",
                        parser.iLineNr, res02.type.toString());
            }
        }
        else{
            parser.error("Line '%d': Expecting type STRING, INTEGER, FLOAT for the first operand but received '%s' data type.",
                    parser.iLineNr, res01.type.toString());
        }

        // The result send out will be the same data type as the operand1
        res.type = SubClassif.BOOLEAN;
        return res;
    }// END OF GREATER THAN OR EQUAL METHOD (aka. mEqui)

    /**
     *CONCATENATION FUNCTION: concat 2 String or 2 identifier that is String together
     * <p>
     *  To use this function, 2 operands have to be both String data type to concat together and
     *  return a resultValue with type String
     * @param parser        -   provide a error handling method
     * @param res01         -   the result value operand 1
     * @param res02         -   the result value operand 2
     * @return  res         -   the result value of the concatenation and type is String
     * @throws Exception    -   raise exception for all data type beside String
     */
    public static ResultValue concat (Parser parser, ResultValue res01, ResultValue res02) throws  Exception{
        ResultValue res = new ResultValue();

        // if any of the result value is identifier
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op1 with the real value in storageManager
        }
        if(res02.type == SubClassif.IDENTIFIER){
            res02 = parser.storageMgr.getVariable(parser,res02.value);      // replace op2 with the real value in storageManager
        }

        // if the operands are anything else beside STRING ==> raise error
        if (res01.type != SubClassif.STRING || res02.type != SubClassif.STRING) {
            parser.error("Line '%d': Expecting type STRING for '#' operation but received '%s' for operand 1 and '%s' for operand 2",
                    parser.iLineNr, res01.type, res02.type);
        }

        res.value = ""+ res01.value + res02.value;
        res.type = SubClassif.STRING;

        return res;
    }// END OF CONCATENATION METHOD


    /**
     * TO INTEGER METHOD: converting numeric String or double number into an integer
     * <p>
     *  This method take in 1 operand (result value) to convert that numeric into an integer representation
     * @param parser        -   provide an error handling method
     * @param res01         -   the operand that needed to be casted
     * @return res          -   the integer representation for the operand (if the operand can be converted into numeric)
     * @throws Exception    -   if the operand can not be converted into a numeric
     */
     public static ResultValue toInt(Parser parser, ResultValue res01) throws Exception{
        //the return resultValue variable
        ResultValue res = new ResultValue();

        // if any of the result value is identifier then get the real value
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op with the real value in storageManager
        }

        // if the operand is int/float/string that can be converted into a numeric then continue
        // if not then the numeric class will handle the error of wrong data type for the operation
        Numeric nOp1 = new Numeric(parser, res01, "1st Operand");

        // to integer op
        res.type = SubClassif.INTEGER;
        res.value = Integer.toString(nOp1.integervalue);

        return res;

    }//END OF TO INTEGER METHOD

    /**
     * TO FLOAT METHOD: converting numeric String or integer number into a double
     * <p>
     *  This method take in 1 operand (result value) to convert that numeric into an floating point representation
     * @param parser        -   provide an error handling method
     * @param res01         -   the operand that needed to be casted
     * @return res          -   the floating point representation for the operand (if the operand can be converted into numeric)
     * @throws Exception    -   if the operand can not be converted into a numeric
     */
    public static ResultValue toFloat(Parser parser, ResultValue res01) throws Exception{
        //the return resultValue variable
        ResultValue res = new ResultValue();

        // if any of the result value is identifier then get the real value
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op with the real value in storageManager
        }

        // if the operand is int/float/string that can be converted into a numeric then continue
        // if not then the numeric class will handle the error of wrong data type for the operation
        Numeric nOp1 = new Numeric(parser, res01, "1st Operand");

        // to double op
        res.type = SubClassif.FLOAT;
        res.value = Double.toString(nOp1.doubleValue);

        return res;

    }//END OF TO FLOAT METHOD

    /**
     * TO STRING METHOD: converting integer or double into String
     * <p>
     *  This method take in 1 operand (result value) to convert that numeric into an String representation
     *  The operand itself need to be a numeric or else the conversion will fail
     * @param parser        -   provide an error handling method
     * @param res01         -   the operand that needed to be casted
     * @return res          -   the String representation for the operand (if the operand can be converted into numeric)
     * @throws Exception    -   if the operand can not be converted into a numeric
     */
    public static ResultValue toString(Parser parser, ResultValue res01) throws Exception{
        //the return resultValue variable
        ResultValue res = new ResultValue();

        // if any of the result value is identifier then get the real value
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op with the real value in storageManager
        }

        // if the operand is int/float/string that can be converted into a numeric then continue
        // if not then the numeric class will handle the error of wrong data type for the operation
        Numeric nOp1 = new Numeric(parser, res01, "1st Operand");

        // toString from Int
        if(nOp1.type == SubClassif.INTEGER){
            res.value = Integer.toString(nOp1.integervalue);
        }
        // toString from double
        else{
            res.value = Double.toString(nOp1.doubleValue);
        }
        res.type = SubClassif.STRING;

        return res;

    }//END OF TO STRING METHOD

    /**
     * LENGTH METHOD: take in a String type object(either direct or stored variable) and return the length
     * <p>
     *     This method take in 1 argument, determined if it is String
     *     and return the total length for that String type
     * @param parser        -   provide an error handling method
     * @param res01         -   the input argument
     * @return res          -   result Value that contain the length of the String
     * @throws Exception    -   throw exception if the argument is not a String
     */
    public static ResultValue LENGTH (Parser parser, ResultValue res01) throws Exception{
        ResultValue res = new ResultValue();
        // if any of the result value is identifier then get the real value
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op with the real value in storageManager
        }

        // if the object is not String type then raise error
        if (res01.type != SubClassif.STRING)
            parser.error("LENGTH method require argument to be of STRING type.");

        // if it is String then return its length
        res.value =  Integer.toString(res01.value.length());
        res.type = SubClassif.INTEGER;

        return res;
    }// END OF LENGTH METHOD

    /**
     * SPACES METHOD:take in a String type object(either direct or stored variable) and return T or F
     * <p>
     *    This method takes in 1 argument, determined if it is String,
     *    then return whether or not the String contrains a space or empty.
     * @param parser        -   provide an error handling method
     * @param res01         -   the input argument
     * @return res          -   result Value that contain T of F
     * @throws Exception    -   throw exception if the argument is not a String
     */
    public static ResultValue SPACES(Parser parser, ResultValue res01) throws  Exception{
        ResultValue res = new ResultValue();

        // if any of the result value is identifier then get the real value
        if (res01.type == SubClassif.IDENTIFIER ){
            res01 = parser.storageMgr.getVariable(parser,res01.value);      // replace op with the real value in storageManager
        }

        // if the object is not String type then raise error
        if (res01.type != SubClassif.STRING)
            parser.error("SPACES method requires argument to be of STRING type.");

        // if the String is empty or contain any space then return true
        if(res01.value.isEmpty()||res01.value.contains(" "))
            res.value = "T";
        else
            res.value = "F";

        res.type = SubClassif.BOOLEAN;
        return res;

    }// END OF SPACES METHOD

    public static ResultValue ELEM (Parser parser, ResultValue  res01) throws Exception{
        ResultValue res = new ResultValue();

        // if the argument input is not an array (by itself is an identifier)
        if(!(res01.structure == IdenClassif.FIXED_ARRAY || res01.structure==IdenClassif.UNBOUND_ARRAY))
             parser.error("ELEM method requires argument to be an array");

        res.type = SubClassif.INTEGER;
        res.value = Integer.toString(parser.storageMgr.getArray(parser,res01.value).size());

        return res;
    }

    public static ResultValue MAX(Parser parser, ResultValue res01) throws Exception{
        ResultValue res = new ResultValue();

        // if unbounded array: return -1, fixed: the max number, else: raise error
        if (res01.structure == IdenClassif.UNBOUND_ARRAY){
            res.value = "-1";
        }else if (res01.structure == IdenClassif.FIXED_ARRAY){
            //TODO: Aaccess max
            res.value = Integer.toString(parser.storageMgr.arrayMaxSize(parser,res01.value));
        }else{
            parser.error("MAX method requires argument to be an array");
        }

        res.type = SubClassif.INTEGER;
        return res;
    }

    public static ResultValue dateDiff(Parser parser, ResultValue res01, ResultValue res02) throws Exception{
         //Next leap year is 2020, happens every 4 years
        ResultValue res = new ResultValue();
        int result = abs(getDays(res01)- getDays(res02));
        res.value = "" + result;
        return res;
    }
    public static int getDays(ResultValue res){
        int[] nonLeapYears = {31,28,31,30,31,30,31,31,30,31,30,31};
        int[] LeapYears = {31,29,31,30,31,30,31,31,30,31,30,31};
        String years = "";
        String days = "";
        String months = "";
        int iTotalDays = 0;

        for(int i = 0; i < res.value.length(); i++){
            if(i<4)
                years = years + res.value.charAt(i);
            if(i==6||i==5)
                months = months + res.value.charAt(i);
            if(i==8||i==9)
                days = days + res.value.charAt(i);
        }
        //Days Section
        iTotalDays += Integer.parseInt(days);
        //Months Section
        int iYears = Integer.parseInt(years);
        int iMonths = Integer.parseInt(months);
        if(iYears%4==0){    //Leap year, so months are different
            for(int i = 0; i < iMonths; i++){
                iTotalDays += LeapYears[i];
            }
        }
        else{
            for(int i = 0; i < iMonths; i++){
                iTotalDays += nonLeapYears[i];
            }
        }
        //Years section
        iTotalDays = iTotalDays + iYears*365 + (iYears/4);
        return iTotalDays;
    }
}
