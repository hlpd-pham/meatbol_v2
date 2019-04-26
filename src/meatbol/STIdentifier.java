package meatbol;


public class STIdentifier extends STEntry
{
    /** The declaration type of the identifier*/
    public SubClassif dclType = SubClassif.EMPTY;

    /** The structure of the identifier*/
    public IdenClassif structure = IdenClassif.EMPTY;

    /** The parameter type: not pram, by ref or by val*/
    public IdenClassif param = IdenClassif.EMPTY;

    /**nonLocal base Address Ref (0 - local, 1 - surrounding, ..., k- surrounding,99-global)*/
    public int nonLocal;

    /**
     * STIdentifier()
     *
     * STIdentifier constructor: initialize symbol, primary classification, declare type
     * structure, param type, non-Local
     * <p>
     *
     * @param symbol            -   the symbol's string representation
     * @param primClassif       -   the symbol's primary classification
     * @param dclType           -   the declaration type
     * @param structure         -   the structure for the identifier

     */
    STIdentifier(String symbol, Classif primClassif, SubClassif dclType, IdenClassif structure)
    {
        super(symbol, primClassif);
        this.dclType = dclType;
        this.structure = structure;
        this.nonLocal = 0;
    }// END OF STIDENTIFIER CONSTRUCTOR

}// END OF STIDENTIFIER CLASS
