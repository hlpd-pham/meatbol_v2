package meatbol;

public class Numeric {
    public int integervalue;
    public double doubleValue;
    public String strValue;   // display value
    public SubClassif type;   // INTEGER, FLOAT

    /**
     * Numeric constructor for assignment statement that can handle error if the send in return value is not a numeric
     * <p>
     * When the constructor accept a value as a numeric, it will automatically assign the corresponding
     * value for each data type in the numeric class: double, int, String
     * @param parser        -   provide the error method for raising errors
     * @param res           -   the result value that the method will have to check if it is a numeric
     * @param sFirst        -   the operator that is associate with the result value
     * @param sSecond       -   the operand that is raising error
     * @return res          -   the result value after putting it down as a numeric with all data for different data type filled
     * @exception Exception -   if the passed in result value can not be parse into a numeric value then raise exception
     */
    public Numeric(Parser parser, ResultValue res, String sFirst, String sSecond) throws Exception
    {
        this.type = res.type;

        //if the passed in result value's type is an int or a float then just changing to other formats
        if(this.type == SubClassif.INTEGER)
        {
            this.integervalue = Integer.parseInt(res.value);
            this.doubleValue = (double) this.integervalue;
            this.strValue = res.value;
            this.type = SubClassif.INTEGER;
        }
        else if(this.type == SubClassif.FLOAT)
        {
            this.doubleValue = Double.parseDouble(res.value);
            this.integervalue = (int)this.doubleValue;
            this.strValue = res.value;
            this.type = SubClassif.FLOAT;
        }
        // if the passed in result value is a string then check if it can be parse into a number
        // if not then raise an error for invalid argument
        else if(this.type == SubClassif.STRING)
        {
            // try if a string is an int, if fails, it could be a float or a non numeric String
            try{
                this.integervalue = Integer.parseInt(res.value);
                this.doubleValue = (double) this.integervalue;
                this.strValue = res.value;
                this.type = SubClassif.INTEGER;
            }
            catch (Exception e){
                // try if a string is a float, if fails ==> it is a non numeric string
                try{
                    this.doubleValue = Double.parseDouble(res.value);
                    this.integervalue = (int)this.doubleValue;
                    this.strValue = res.value;
                    this.type = SubClassif.FLOAT;
                }
                catch (Exception ex){
                    parser.error("Invalid '%s' for operator: '%s' at line '%d'",sSecond,sFirst,parser.iLineNr);
                }
            }
        }
        // the rest of the case (other data type will be count as invalid expression)
        else{
            parser.error("Invalid '%s' for operator: '%s' at line '%d'",sSecond,sFirst,parser.iLineNr);
        }
    }


    /**
     * Numeric Constructor for Utility call
     * <p>
     * When the constructor accept a value as a numeric, it will automatically assign the corresponding
     * value for each data type in the numeric class: double, int, String
     * @param parser        -   provide the error method for raising errors
     * @param res           -   the result value that the method will have to check if it is a numeric
     * @param sFirst        -   the operand that Utility are trying to make numeric
     * @return res          -   the result value after putting it down as a numeric with all data for different data type filled
     * @throws Exception    -   if the passed in result value can not be parse into a numeric value then raise exception
     */
    public Numeric(Parser parser, ResultValue res, String sFirst) throws Exception
    {
        this.type = res.type;

        //if the passed in result value's type is an int or a float then just changing to other formats
        if(this.type == SubClassif.INTEGER)
        {
            this.integervalue = Integer.parseInt(res.value);
            this.doubleValue = (double) this.integervalue;
            this.strValue = res.value;
            this.type = SubClassif.INTEGER;
        }
        else if(this.type == SubClassif.FLOAT)
        {
            this.doubleValue = Double.parseDouble(res.value);
            this.integervalue = (int)this.doubleValue;
            this.strValue = res.value;
            this.type = SubClassif.FLOAT;
        }
        // if the passed in result value is a string then check if it can be parse into a number
        // if not then raise an error for invalid argument
        else if(this.type == SubClassif.STRING)
        {
            // try if a string is an int, if fails, it could be a float or a non numeric String
            try{
                this.integervalue = Integer.parseInt(res.value);
                this.doubleValue = (double) this.integervalue;
                this.strValue = res.value;
                this.type = SubClassif.INTEGER;
            }
            catch (Exception e){
                // try if a string is a float, if fails ==> it is a non numeric string
                try{
                    this.doubleValue = Double.parseDouble(res.value);
                    this.integervalue = (int)this.doubleValue;
                    this.strValue = res.value;
                    this.type = SubClassif.FLOAT;
                }
                catch (Exception ex){
                    parser.error("Invalid '%s'  at line '%d'", sFirst,parser.iLineNr);
                }
            }
        }
        // the rest of the case (other data type will be count as invalid expression)
        else{
            parser.error("Invalid '%s'  at line '%d'", sFirst,parser.iLineNr);
        }
    }
}
