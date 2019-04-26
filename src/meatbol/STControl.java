package meatbol;

public class STControl extends STEntry
{
    /** The subclassification of the control token*/
    public SubClassif subClassif;

    /**
     *STControl()
     *
     * STControl constructor: initializes symbol, primary Classification and sub-classification
     * <p>
     *
     * @param symbol            -   the symbol's string representation
     * @param primClassif       -   the symbol's primary classification
     * @param subClassif        -   the symbol's sub classification
     */
    STControl(String symbol, Classif primClassif, SubClassif subClassif)
    {
        super(symbol, primClassif);
        this.subClassif = subClassif;
    }// END OF STCCONTROL CONTRUCTOR

}// END OF STCONTROL CLASS
