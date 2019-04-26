package meatbol;

import java.util.ArrayList;

public class STFunction extends STEntry
{
    /** The return type of the function*/
    public SubClassif returnType = SubClassif.EMPTY;

    /** The type of function: built-in or user-define*/
    public SubClassif definedBy = SubClassif.EMPTY;

    /** The number of arguments needed for the function*/
    public int numArgs;

    /** Reference to an ArrayList of formal parameters*/
    public ArrayList<STIdentifier> paramList;

    /** Reference to the function's symbol table if it is a user-defined function*/
    public SymbolTable symbolTable;

    /**
     * STFunction()
     *
     * STFunction constructor: initialize symbol, primary classification, return type, defined type, and number
     * of required arguments
     * <p>
     *
     * @param symbol            -   the symbol's string representation
     * @param primClassif       -   the symbol's primary classification
     * @param returnType        -   function's return type
     * @param definedBy         -   function's define type
     * @param numArgs           -   number of required arguments
     */
    STFunction(String symbol, Classif primClassif, SubClassif returnType, SubClassif definedBy,int numArgs  )
    {
        super(symbol, primClassif);
        this.returnType = returnType;
        this.definedBy = definedBy;
        this.numArgs = numArgs;
        paramList = new ArrayList<STIdentifier>();
        //this.paramList.addAll(paramList);
        //this.symbolTable = new SymbolTable();
    }// END OF STFUNCTION CONSTRUCTOR

}//END OF STFUNCTION CLASS
