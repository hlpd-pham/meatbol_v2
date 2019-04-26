package meatbol;
import java.util.*;

/*
 * SymbolTable class
 * Provides a hash table for all relevant symbols in the program.
 * As for now, Symboltable only contains the core symbols and basic built-in function
 */
public class SymbolTable
{
    /** The hash table that store all symbols in the program*/
    public HashMap <String, STEntry> ht = new HashMap<String, STEntry>();

    /** number of required arguments for each function - for now it is 99*/
    public int VAR_ARGS = 99;

    /**
     *Symboltable()
     *
     * SymbolTable constructor: calls initGlobal method which will add all basics symbols
     * and built-in function into the symbol table
     * <p>
     */
    public SymbolTable()
    {
        initGlobal();
    }// END OF SYMBOL TABLE CONSTRUCTOR

    /**
     *initGlobal()
     *
     * This method populates the hash table of the program with basic symbols and built-in functions
     * <p>
     */
    private void initGlobal()
    {
        // debug elements
        ht.put("debug", new STControl("debug",Classif.DEBUG,SubClassif.DEBUG));
        ht.put("def", new STControl("def", Classif.CONTROL,SubClassif.FLOW));
        ht.put("enddef", new STControl("enddef",Classif.CONTROL,SubClassif.END));

        // if statement elements
        ht.put("if", new STControl("if", Classif.CONTROL,SubClassif.FLOW));
        ht.put("endif", new STControl("endif",Classif.CONTROL,SubClassif.END));
        ht.put("else", new STControl("else",Classif.CONTROL,SubClassif.END));

        // for statement elements
        ht.put("for", new STControl("for",Classif.CONTROL,SubClassif.FLOW));
        ht.put("endfor", new STControl("endfor", Classif.CONTROL, SubClassif.END));

        // while statements
        ht.put("while", new STControl("while",Classif.CONTROL,SubClassif.FLOW));
        ht.put("endwhile", new STControl("endwhile", Classif.CONTROL, SubClassif.END));

        // declare tokens
        ht.put("Int", new STControl("Int",Classif.CONTROL,SubClassif.DECLARE));
        ht.put("Float", new STControl("Float",Classif.CONTROL,SubClassif.DECLARE));
        ht.put("String", new STControl("String",Classif.CONTROL,SubClassif.DECLARE));
        ht.put("Bool", new STControl("Bool",Classif.CONTROL,SubClassif.DECLARE));
        ht.put("Date", new STControl("Date",Classif.CONTROL,SubClassif.DECLARE));
        // for array types
        ht.put("unbound", new STControl("unbound", Classif.CONTROL, SubClassif.DECLARE));

        // boolean constants
        ht.put("T", new STControl("T",Classif.OPERAND,SubClassif.BOOLEAN));
        ht.put("F", new STControl("F",Classif.OPERAND,SubClassif.BOOLEAN));

        // logical operators
        ht.put("and", new STEntry("and",Classif.OPERATOR));
        ht.put("or", new STEntry("or",Classif.OPERATOR));
        ht.put("not", new STEntry("not",Classif.OPERATOR));
        ht.put("in", new STEntry("in",Classif.OPERATOR));
        ht.put("notin", new STEntry("in",Classif.OPERATOR));

        // function keywords
        ht.put("print", new STFunction("print",Classif.FUNCTION,SubClassif.VOID,SubClassif.BUILTIN, VAR_ARGS));
        ht.put("LENGTH", new STFunction("LENGTH",Classif.FUNCTION,SubClassif.INTEGER,SubClassif.BUILTIN, VAR_ARGS));
        ht.put("ELEM", new STFunction("ELEM",Classif.FUNCTION,SubClassif.INTEGER,SubClassif.BUILTIN, VAR_ARGS));
        ht.put("MAXELEM", new STFunction("MAXELEM",Classif.FUNCTION,SubClassif.INTEGER,SubClassif.BUILTIN, VAR_ARGS));
        ht.put("SPACES", new STFunction("SPACES",Classif.FUNCTION,SubClassif.INTEGER,SubClassif.BUILTIN, VAR_ARGS));

    }//END OF INIT GLOBAL

    /**
     *getSymbol()
     *
     * getSymbol returns the STEntry for the symbol given in the parameter.
     * <p>
     *
     * this method's return value is a STEntry object if the string given is in the list of keys of the hash table.
     * It return a null if the string given is not in the list
     *
     * @param  symbol            -       a string containing the key entry for the hash table.
     * @return STEntry object    -       if the hash table contains the key string
     *         null              -       if the key string was not found in the has table
     */
    public STEntry getSymbol(String symbol)
    {
        try
        {
            return ht.get(symbol);
        }
        catch(NullPointerException e)
        {
            return null;
        }
    }// END OF GET SYMBOL
    /**
     * putSymbol()
     *
     * putSymbol takes the STEntry object and inserts it into the hash table
     * and uses the symbol string as the key for the hash table
     * <p>
     *
     * @param symbol             -       A string that will be the key for the hash table
     * @param entry              -       A STEntry object containing the information about the current key
     */
    public void putSymbol(String symbol, STEntry entry)
    {
        ht.put(symbol, entry);
    }// END OF PUT SYMBOL

}// END OF SYMBOL TABLE CLASS
