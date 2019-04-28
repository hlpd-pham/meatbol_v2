package meatbol;
public enum SubClassif 
{
    EMPTY,      // empty

    // OPERAND's subclassifications
    IDENTIFIER,         // identifier
    INTEGER,            // integer constant
    FLOAT,              // float constant
    BOOLEAN,            // boolean constant
    STRING,             // string constant
    DATE,               // date constant
    VOID,               // void

    // CONTROL's subclassifications
    FLOW,       // flow statement (e.g., if)
    END,        // end statement (e.g., endif)
    DECLARE,    // declare statement (e.g., Int)
    DEBUG,      //  for debugger

    // FUNCTION's subclassfications
    BUILTIN,    // builtin function (e.g., print)
    USER,        // user-defined function

    // Simple operator classification
    PLUS,           //  addition
    MINUS,          //  subtraction
    MULTIPLY,       //  multiplication
    DIVIDE,         //  division
    UNARYMINUS,     //  unary minus
    SMALLER,        //  smaller than
    LARGER,         //  larger than
    SEQUAL,         //  smaller than or equal to
    LEQUAL,         //  larger than or equal to
    EQUI,           //  equivalant
    NEQUI,          //  not equivalent
    PEQUAL,         //  plus equal
    MEQUAL,         //  minus equal
    ASSIGN,         //  assignment
    ARRAY_SLICE,    //  array slicing
    EXPO,           //  exponent (^)

    GOOD
}
