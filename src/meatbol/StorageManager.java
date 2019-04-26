package meatbol;

import java.util.ArrayList;
import java.util.HashMap;

public class StorageManager {

    /** Hashmap stores primitive variables (Float, Int, Boolean, Date, String)
     */
    public HashMap<String, ResultValue> storage = new HashMap<>();

    /** Hashmap stores array variables (FIXED_ARRAY, UNBOUND_ARRAY)
     */
    public HashMap<String, ArrayList<ResultValue>> storageM = new HashMap<>();

    /** Hashmap stores array data type (Float, Int, ...) and structure type
     *  (FIXED or UNBOUND)
     */
    public HashMap<String, ResultValue> arrayType = new HashMap<>();

    /** Hashmap stores max allocated size allowed for each array
     *  (-1 if array is unbound)
     */
    public HashMap<String, Integer> maxSize = new HashMap<>();

    /**
     * storage manager constructor
     */
    public StorageManager() {}

    /**
     * Storage manager method getVariable for primitive type
     *
     * @param parser        Parser
     * @param variableStr   name of variable
     * @return              corresponding ResultValue of the variableStr
     * @throws Exception    Undeclared variable
     */
    public ResultValue getVariable(Parser parser, String variableStr) throws Exception {
        try {
            ResultValue res;
            res = storage.get(variableStr);
            if (res == null)
                throw new NullPointerException();
            return res;
        }
        catch (NullPointerException e) {
            parser.error("Undeclared variable '%s'", variableStr);
        }
        return null;
    } // END getVariable

    /**
     * Storage manager method replace method for primitive type
     *
     * @param parser        Parser
     * @param variableStr   name of variable
     * @param res           new ResultValue for corresponding variableStr
     * @return              new ResultValue for corresponding variableStr
     * @throws Exception    Undeclared variable
     */
    public ResultValue replace(Parser parser, String variableStr, ResultValue res) throws Exception
    {
        try {
            ResultValue toReplace = storage.get(variableStr);
            if (toReplace == null)
                throw new NullPointerException();
            toReplace.value = res.value;
            return storage.put(variableStr, toReplace);
        } catch (NullPointerException e) {
            parser.error("Undeclared variable '%s'", variableStr);
        }
        return null;
    } // END replace

    /**
     * Array initialization:
     * - Array is meatbol is an ArrayList of ResultValue
     * - When an array is initialize, a corresponding array (ArrayList<ResultValue>),
     * array structure (ResultValue), allocated size (int) will be mapped to storageM,
     * arrayType, and maxSize
     * - If the array is unbound, its maxSize will be -1
     * - A ResultValue will also be put in the hashmap for regular variable (for array
     * to array assignments). The value of this ResultValue will not have the '['
     * <p>
     * return value is the array list of ResultValue
     *
     * @param parser            Parser
     * @param variableStr       name of array
     * @param resM              array of meatbol elements
     * @param arrayStructure    structure of array
     * @param allocatedSize     max size allowed for array
     * @return                  array of (allocated) meatbol elements
     */
    public ArrayList<ResultValue> initializeArray(Parser parser, String variableStr,
                                                  ArrayList<ResultValue> resM, ResultValue arrayStructure,
                                                  int allocatedSize)
    {
        // array structure
        arrayType.put(variableStr, arrayStructure);

        // array allowed sized
        if (arrayStructure.structure == IdenClassif.UNBOUND_ARRAY)
            maxSize.put(variableStr, -1);
        else
            maxSize.put(variableStr, allocatedSize);

        // array identifier
        ResultValue arrayIdentifier = arrayStructure;
        String arrayIdenStr = variableStr.replaceAll("\\[", "");
        arrayIdentifier.value = arrayIdenStr;
        arrayIdentifier.type = arrayStructure.type;
        storage.put(arrayIdenStr, arrayIdentifier);

        // meatbol array
        return storageM.put(variableStr, resM);
    } // END initializeArray

    /**
     *
     * @param parser
     * @param variableStr
     * @return
     * @throws Exception
     */
    public int arrayMaxSize(Parser parser, String variableStr) throws Exception
    {
        try {
            ResultValue res = arrayType.get(variableStr);
            if (res == null)
                throw new NullPointerException();
            int result = maxSize.get(variableStr);
            return result;
        }
        catch (NullPointerException e) {
            parser.error("Undeclared variable '%s'", variableStr);
        }
        return -1;
    } // END arrayMaxSize

    /**
     * Storage manager replace method for array type
     *
     * @param parser
     * @param variableStr
     * @param resM
     * @return
     * @throws Exception
     */
    public ArrayList<ResultValue> replaceArray(Parser parser, String variableStr, ArrayList<ResultValue> resM) throws Exception
    {
        try {
            ArrayList<ResultValue> toReplace = storageM.get(variableStr);
            if (toReplace == null)
                throw new NullPointerException();
            toReplace = new ArrayList<>(resM);
            return storageM.put(variableStr, toReplace);
        }
        catch (NullPointerException e) {
            parser.error("Undeclared variable '%s'", variableStr);
        }
        return null;
    } // END replaceArray

    /**
     * Storage manager getVariable method for array type
     *
     * @param parser
     * @param variableStr
     * @return
     * @throws Exception
     */
    public ArrayList<ResultValue> getArray(Parser parser, String variableStr) throws Exception
    {
        try {
            ArrayList<ResultValue> resM;
            resM = storageM.get(variableStr);
            if (resM == null)
                throw new NullPointerException();
            return resM;
        }
        catch (NullPointerException e) {
            parser.error("Undeclared variable '%s'", variableStr);
        }
        return null;
    } // END getArray

    /**
     *
     * @param parser
     * @param variableStr
     * @return
     * @throws Exception
     */
    public ResultValue getArrayStructure(Parser parser, String variableStr) throws Exception {
        try {
            ResultValue res;
            res = arrayType.get(variableStr);
            if (res == null)
                throw new NullPointerException();
            return res;
        }
        catch (NullPointerException e) {
            parser.error("Undeclared variable '%s'", variableStr);
        }
        return null;
    }

    /**
     *
     * @param parser
     * @param variableStr
     * @return
     * @throws Exception
     */
    public int getAllocatedLength(Parser parser, String variableStr) throws Exception {
        try {
            ArrayList<ResultValue> resArrayM = storageM.get(variableStr);
            if (resArrayM == null)
                throw new NullPointerException();
            return resArrayM.size();
        }
        catch (NullPointerException e) {
            parser.error("Undeclared Variable '%s'", variableStr);
        }
        return 0;
    }

}
