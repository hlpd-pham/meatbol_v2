package meatbol;

public enum IdenClassif
{
    EMPTY,          // empty
    // Identifier's structure
    PRIMITIVE,      //primitive data type
    FIXED_ARRAY,    //fixed array (eg. array)
    UNBOUND_ARRAY,  //unbounded collections (list, arrayList)

    // Identifier's param type
    NOT_PARAM,      //when it is not a param, could be data type of the actual param
    BY_REF,         //when the parameter is passed by reference
    BY_VAL          //when the parameter is passsd by value
}
