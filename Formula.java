import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.EmptyStackException;

/**
 * The Class Formula.
 *
 * @author Trevor Stevens
 * @author Brian Schreiber
 */
public class Formula {

    private Scanner sc;
    private Stack<Boolean> booleanStack;
    private Stack<HashObject> hashObjectStack;
    private float rankArray[][];
    private double powArray[];
    private Object clauseList[];
    private HashMap<Integer, HashObject> hashMap;
    private HashObject hashObj;
    private int numVariables,numClauses,key,one;
    private int shift = 0;
    private boolean clauseSizeZeroResult;
    private boolean justBackTracked = false;
    /**
     * Instantiates a new formula.
     *
     * @param fileName the file name
     */
    Formula(String fileName) {
        importCNF(fileName);
        rankArray = new float[2][numVariables];
        booleanStack = new Stack<Boolean>();
        hashObjectStack = new Stack<HashObject>();
        populateHashMap();
        rankVariables();
    }

    /**
     * Import cnf file and store clauses in
     * clauseList.
     *
     * @param fileName the file name
     */
    private void importCNF(String fileName) {
        int clause, i, nextVar, size;
        int tmp[];
        try {
            sc = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.exit(1);
        }

        while (sc.findInLine("p cnf") == null) {
            sc.nextLine();
        }

        sc.findInLine("p cnf");
        numVariables = sc.nextInt();
        numClauses = sc.nextInt();
        clauseList = new Object[numClauses];
        ArrayList<Integer> list = new ArrayList<Integer>(numVariables/4);

        /*
         * Populate the clause list
         */
        for (clause = 0; sc.hasNextInt(); clause++) {
            nextVar = sc.nextInt();
            while (nextVar != 0) {
                list.add(nextVar);
                if (nextVar == 0) {
                    break;
                }
                nextVar = sc.nextInt();
            }
            size = list.size();
            tmp = new int[size];
            for (i = 0; i < size; i++) {
                tmp[i] = list.get(i);
            }
            clauseList[clause] = new Clause(size, tmp );
            list.clear();
        }
        sc.close();
    }

    /**
     * Populate the hash map.
     */
    private void populateHashMap() {
        hashMap = new HashMap<Integer, HashObject>(numVariables*2);//, (float)0.5);
        Clause clauseAtI;
        HashObject hashTmp;
        int clauseVar,clauseVarKey,i,j;
        HashObject prevHashObj;
        int clauseAtISize;
        for (i = 0; i < numClauses; i++) {
            clauseAtI = (Clause) clauseList[i];
            clauseAtISize = clauseAtI.size();
            for (j = 0; j < clauseAtISize; j++) {
                clauseVar = clauseAtI.get(j);
                clauseVarKey = Math.abs(clauseVar); //abs of variable for key
                prevHashObj = hashMap.get(clauseVarKey);
                if (prevHashObj == null) {
                    hashTmp = new HashObject();
                    hashTmp.variableNumber(clauseVarKey);
                    hashMap.put(clauseVarKey, hashTmp);
                    prevHashObj = hashTmp;
                }
                if (clauseVar > 0) {
                    prevHashObj.addClausePos(clauseAtI);
                } else {
                    prevHashObj.addClauseNeg(clauseAtI);
                }
            }
        }
    }

    /**
     * Fill pow array, with a few of
     * 2^n powers.
     */
    private void fillPowArray(int size) {
        powArray = new double[size];
        for (int i = 0; i < size; i++) {
            powArray[i] = Math.pow(2, i * -1);
        }
    }

    /**
     * Rank the variables for the
     * first time.
     */
    private void rankVariables() {
        int clength,size,i,j;
        float sum = 0;
        fillPowArray(30);
        int powLength = powArray.length;

        for (i = 1; i <= numVariables; i++) {     // Creates List
            hashObj =  hashMap.get(i);
            if (hashObj != null) {
                size = hashObj.posSize();
                for (j = 0; j < size; j++) {        // Sums the rank in the posList
                    Clause tmpClause = hashObj.getP(j);
                    clength = tmpClause.size();
                    if (clength < powLength) {
                        sum += powArray[clength];
                    } else {
                        sum += Math.pow(2, (clength * -1));
                    }
                }
                size = hashObj.negSize();
                for (j = 0; j < size; j++) {        // Sums the rank in the negList

                    clength = hashObj.getN(j).size();
                    if (clength < powLength) {
                        sum += powArray[clength];
                    } else {
                        sum += Math.pow(2, (clength * -1));
                    }
                }
                rankArray[0][i - 1] = i;            // Stores the Variable in the first column
                rankArray[1][i - 1] = sum;          // Stores the Ranking in the second column
                sum = 0;
            }

        }

        mergeSort();
    }

    /**
     * Re-rank variables.
     */
    public void reRankVariables() {
        Clause tmpClause;
        boolean swapLargest;
        int clength, size, i, j, k, currentMaxKey, currentSmart, absOne;
        int maxValueKey = -1;
        float currentMaxRank;
        float sum = 0;
        double maxValue;
        double checkValue = 0;
        int powLength = powArray.length;

        one = lengthOneCheck();
        int pSize, nSize, bigger, s;
        if (one == 0) {
            for (i = shift; i < numVariables; i++) {
                hashObj = (hashMap.get((int) rankArray[0][i]));
                if (hashObj == null) {
                    //rankArray[1][i] = 0; //not sure if good/bad
                    continue;
                }
                pSize = hashObj.posSize();
                nSize = hashObj.negSize();
                bigger = nSize < pSize ? pSize : nSize;
                for (s = 0; s < bigger; s++) {
                    if (s < pSize) {
                        tmpClause = hashObj.getP(s);
                        clength = tmpClause.size();
                        if (clength < powLength) {
                            sum += powArray[clength];
                        } else {
                            sum += Math.pow(2, (clength * -1));
                        }
                    }
                    if (s < nSize) {
                        // Neg half
                        tmpClause = hashObj.getN(s);
                        clength = tmpClause.size();
                        if (clength < powLength) {
                            sum += powArray[clength];
                        } else {
                            sum += Math.pow(2, (clength * -1));
                        }
                    }
                }
                rankArray[1][i] = sum;          // Stores the Ranking in the second column
                sum = 0;
            }
        }

        maxValue = checkValue;
        swapLargest = false;
        if (one != 0) {
            absOne = Math.abs(one);
            for (i = shift; i < numVariables; i++) {
                if (absOne == (int) rankArray[0][i]) {
                    maxValue = rankArray[0][i];
                    maxValueKey = i;
                }
            }
            swapLargest = true;
        } else {
            for (i = shift; i < numVariables; i++) { //Move largest var to shift
                checkValue = rankArray[1][i];
                if (maxValue < checkValue) {
                    maxValueKey = i;
                    maxValue = checkValue;
                    swapLargest = true;
                }
            }
        }
        //Switch the maxValueKey to the shift position
        if (swapLargest) {
            currentMaxKey = (int) rankArray[0][shift];
            currentMaxRank = rankArray[1][shift];
            rankArray[0][shift] = rankArray[0][maxValueKey];
            rankArray[1][shift] = rankArray[1][maxValueKey];

            rankArray[0][maxValueKey] = currentMaxKey;
            rankArray[1][maxValueKey] = currentMaxRank;
        }
    }

    /**
     * Forward tracks down the tree.
     */
    public void forwardTrack() {
        reRankVariables();
        Clause clause;
        HashObject nextVarObj;
        boolean booleanValue;
        int var, absKey, actualSize, j, i;
        int varNeg = 0;
        if (one != 0) {
            var = one;
            absKey = Math.abs(var);
            nextVarObj =  hashMap.get(absKey);
            hashMap.remove(absKey);
        } else {
            var = (int) rankArray[0][shift];
            absKey = Math.abs(var);
            nextVarObj =  hashMap.get(absKey);
            hashMap.remove(absKey);
        }
        /*
         * This if and else statement determine whether
         * to branch true or false by checking if it has
         * just back tracked or not.
         */
        if (!justBackTracked && var > 0) {
            booleanValue = true;
            varNeg = var * -1;
            int listSize = nextVarObj.posSize();
            int opsitListSize = nextVarObj.negSize();
            for (i = 0; i < listSize; i++) {
                clause = nextVarObj.getP(i);
                actualSize = clause.actualSize();
                for (j = 0; j < actualSize; j++) {
                    key = clause.get(j);
                    absKey = Math.abs(key);
                    if (key != 0 && absKey != var) {
                        hashObj = hashMap.get(absKey);
                        if (hashObj != null) {
                            hashObj.removeClause(clause);
                        }
                    }
                }
            }

            /*
             * This loop removes all varNeg occurrences
             * from all clauses in clauseList.
             */
            for (i = 0; i < opsitListSize; i++) {
                (nextVarObj.getN(i)).removeVar(varNeg);
            }

            hashObjectStack.push(nextVarObj);
            booleanStack.push(booleanValue);
            justBackTracked = false;
            shift++;
        } else {
            var = Math.abs(var);
            booleanValue = false;
            varNeg = var;
            int listSize = nextVarObj.negSize();
            int opsitListSize = nextVarObj.posSize();

            for (i = 0; i < listSize; i++) {
                clause = nextVarObj.getN(i);
                actualSize = clause.actualSize();
                for (j = 0; j < actualSize; j++) {
                    key = clause.get(j);
                    absKey = Math.abs(key);
                    if (key != 0 && absKey != var) {
                        hashObj = hashMap.get(absKey);
                        if (hashObj != null) {
                            hashObj.removeClause(clause);
                        }
                    }
                }
            }

            /*
             * This loop removes all varNeg occurrences
             * from all clauses in clauseList.
             */
            for (i = 0; i < opsitListSize; i++) {
                (nextVarObj.getP(i)).removeVar(varNeg);
            }
            hashObjectStack.push(nextVarObj);
            booleanStack.push(booleanValue);
            justBackTracked = false;
            shift++;
        }
    }

    /**
     * Back tracks up the tree.
     */
    public void backTrack() throws EmptyStackException {
        //  Reduce runtime overhead && clean up code
        while (!booleanStack.pop()) {
            shift--;
            rePopulate((int) rankArray[0][shift], hashObjectStack.pop(), false);
        }
        shift--;
        rePopulate((int) rankArray[0][shift], hashObjectStack.pop(), true);
        justBackTracked = true;
    }

    /**
     * Re-populate the hashMap
     *
     * @param key the key
     * @param rePopObj the object being repopulated
     * @param varSetTo the boolean value of the object
     */
    private void rePopulate(int key, HashObject rePopObj, boolean varSetTo) {
        Clause clause;
        int var, negkey, rVarSize, rClauseSize, i, j, actualSize;
        if (varSetTo) {
            negkey = key * -1;
            rVarSize = rePopObj.negSize();
            rClauseSize = rePopObj.posSize();
            for (i = 0; i < rClauseSize; i++) {
                clause = rePopObj.getP(i);
                actualSize = clause.actualSize();
                for (j = 0; j < actualSize; j++) {
                    var = clause.get(j);
                    if (var != 0) {
                        hashObj = hashMap.get(Math.abs(var));
                        if (hashObj != null && var > 0) {
                            hashObj.addClausePos(clause);
                        } else if (hashObj != null) {
                            hashObj.addClauseNeg(clause);
                        }
                    }
                }
            }
            for (i = 0; i < rVarSize; i++) {
                clause = rePopObj.getN(i);
                clause.addVar(negkey);
            }
            hashMap.put(key, rePopObj);
        } else {
            negkey = key;
            rVarSize = rePopObj.posSize();
            rClauseSize = rePopObj.negSize();
            for (i = 0; i < rClauseSize; i++) {
                clause = rePopObj.getN(i);
                actualSize = clause.actualSize();
                for (j = 0; j < actualSize; j++) {
                    var = clause.get(j);
                    if (var != 0) {
                        hashObj = hashMap.get(Math.abs(var));
                        if (hashObj != null && var > 0) {
                            hashObj.addClausePos(clause);
                        } else if (hashObj != null) {
                            hashObj.addClauseNeg(clause);
                        }
                    }
                }
            }
            for (i = 0; i < rVarSize; i++) {
                clause = rePopObj.getP(i);
                clause.addVar(negkey);
            }
            hashMap.put(key, rePopObj);
        }
    }

    /**
     * Check for if a clause is size zero.
     *
     * @return true, if successful
     */
    public boolean clauseSizeZero() {
        int length = clauseList.length;
        for (int i = 0; i < length; i++) {
            if (((Clause) clauseList[i]).size() == 0) {
                clauseSizeZeroResult = true;
                return clauseSizeZeroResult;
            }
        }
        clauseSizeZeroResult = false;
        return clauseSizeZeroResult;
    }

    /**
     * Returns result of last method call to clauseSizeZero
     * @return clauseSizeZero last result
     * @see clauseSizeZero()
     */

    public boolean getLastClauseSizeResult() {
        return clauseSizeZeroResult;
    }

    /**
     * Check if there it is a valid solution.
     *
     * @return true, if successful
     */
    public boolean validSolution() {
        if ( !clauseSizeZero() && (hashMap.isEmpty() || allEmptyKeyMap())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check to see if all keys in hashMap are
     * empty.
     *
     * @return true, if successful
     */
    private boolean allEmptyKeyMap() {
        int i;
        HashObject tmp;
        for (i = shift; i < numVariables; i++) {
            tmp = hashMap.get((int) rankArray[0][i]);
            if (tmp != null && (!tmp.posEmpty() || !tmp.negEmpty())) {
                return false;
            }
        }
        return true;
    }
    /**
     * Finds and returns highest ranked unit variable
     * @return
     */
    private int lengthOneCheck() {
        HashObject tmp;
        int tmpVar, size, i, k;

        for (k = shift; k < numVariables; k++) {
            tmp = hashMap.get((int) rankArray[0][k]);
            if (tmp != null) {
                size = tmp.posSize();
                for (i = 0; i < size; i++) {
                    tmpVar = tmp.getP(i).lengthOne();
                    if (tmpVar != 0) {
                        return tmpVar;
                    }
                }
                size = tmp.negSize();
                for (i = 0; i < size; i++) {
                    tmpVar = tmp.getN(i).lengthOne();
                    if (tmpVar != 0) {
                        return tmpVar;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Merge sort.
     */
    private void mergeSort() {
        float temp[][] = new float[2][numVariables];
        mergeSort(temp, 0 + shift, numVariables - 1);
    }

    /**
     * Merge sort.
     *
     * @param temp the temp Array
     * @param lowerBound the lower bound
     * @param upperBound the upper bound
     */
    private void mergeSort(float temp[][], int lowerBound, int upperBound) {
        if (lowerBound == upperBound) {
            return;                   // If index == 1 do nothing
        } else {
            int mid = (lowerBound + upperBound) / 2;    // Get midpoint

            mergeSort(temp, lowerBound, mid);     // Lower Half

            mergeSort(temp, mid + 1, upperBound);     // Upper Half

            merge(temp, lowerBound, mid + 1, upperBound); // Merge both Halves

        }
    }

    /**
     * Merge.
     *
     * @param temp the temp array
     * @param lower the lower
     * @param highMid the high mid
     * @param upperBound the upper bound
     */
    private void merge(float temp[][], int lower, int highMid, int upperBound) {
        int i = 0;              // temp index

        int lowerBound = lower;       // saves lower value

        int lowMid = highMid - 1;       // sets the lower mid point

        int n = upperBound - lowerBound + 1;  // number of ints in range

        while (lower <= lowMid && highMid <= upperBound) {
            if (rankArray[1][lower] > rankArray[1][highMid]) {
                temp[0][i] = rankArray[0][lower];
                temp[1][i++] = rankArray[1][lower++];
            } else {
                temp[0][i] = rankArray[0][highMid];
                temp[1][i++] = rankArray[1][highMid++];
            }
        }

        while (lower <= lowMid) {
            temp[0][i] = rankArray[0][lower];
            temp[1][i++] = rankArray[1][lower++];
        }

        while (highMid <= upperBound) {
            temp[0][i] = rankArray[0][highMid];
            temp[1][i++] = rankArray[1][highMid++];
        }

        for (i = 0; i < n; i++) {
            rankArray[1][lowerBound + i] = temp[1][i];
            rankArray[0][lowerBound + i] = temp[0][i];
        }
    }

    /**
     * Print Solution Set.
     */
    public void printSolution() {
        while (!hashObjectStack.isEmpty()) {
            if (booleanStack.pop()) {
                System.out.print(hashObjectStack.pop().getVariableNumber()+" ");
            } else {
                // If false negate variable
                System.out.print(hashObjectStack.pop().getVariableNumber()*-1+" ");
            }
        }
    }
}

