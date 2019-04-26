package meatbol;

/*STEntry class
 * This class will provide the base object for:
 *      STFunction
 *      STIdentifier
 *      STControl
 */
public class STEntry
{
    /** The string representation of the symbol that associate with all its properties*/
    public String symbol = "";

    /** The primary classification for that entry*/
    public Classif primClassif = Classif.EMPTY;

    /**
     *STEntry()
     *
     * STEntry constructor: initializes symbol and primClassif
     * <p>
     *
     * @param symbol            -   the symbol's string representation
     * @param primClassif       -   the symbol's primary classification
     */
    STEntry (String symbol, Classif primClassif)
    {
        this.symbol = symbol;
        this.primClassif = primClassif;
    }// END OF STENTRY CONSTRUCTOR

}// END OF STENTRY CLASS
