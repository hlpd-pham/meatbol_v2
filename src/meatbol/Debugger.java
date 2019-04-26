package meatbol;
/**
* Debugger class made for the booleans required in parser. Object created and booleans turned on and off which
* allows the debugging print statements to turn on. This object is mostly for passing any booleans the while/if stmt's need,
* but after alot of redesign, it's more optional at this point.
* Base object for:
*   Parser
*/
public class Debugger
{
 
    public boolean bShowToken = false;
    public boolean bShowExpr = false;
    public boolean bShowAssign = false;
    public boolean bShowStmt = false;
    
    /** 
    *Debugger()
    *
    *Debugger constructor: initializes all booleans to false to start
    *<p>
    *
    *@param bShowToken - boolean for showing the current token
    *@param bShowExpr - boolean for showing expression debugging
    *@param bShowAssign - boolean for showing assignment debugging
    *@param bShowStmt - boolean for showing stmt debugging
    */
    Debugger(boolean bShowToken, boolean bShowExpr, boolean bShowAssign, boolean bShowStmt)
    {
        this.bShowToken = bShowToken;
        this.bShowExpr = bShowExpr;
        this.bShowAssign = bShowAssign;
        this.bShowStmt = bShowStmt;
    } //end of debugger constructor
}//end of class
