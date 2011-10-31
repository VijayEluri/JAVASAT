import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.EmptyStackException;
import static java.lang.Math.abs;

/**
 * The Class Formula.
 *
 * @author Trevor Stevens
 * @author Brian Schreiber
 */
public class Formula {

    private Scanner sc;
    final private Stack<Boolean> booleanStack;
    final private Stack<HashObject> hashObjectStack;
    private float rankArray[][];
    private HashMap<Integer, Double> powerMap;
    private Clause[] clauseList;
    private HashMap<Integer, HashObject> hashMap;
    private HashObject hashObj;
    private int numVariables,numClauses;
    private int shift;
    private boolean clauseSizeZeroResult;
    private boolean justBackTracked;
    /**
     * Instantiates a new formula.
     *
     * @param fileName the file name
     */
    Formula(final String fileName) {
        importCNF(fileName);
        rankArray = new float[2][numVariables];
        booleanStack = new Stack<Boolean>();
        hashObjectStack = new Stack<HashObject>();
        powerMap = new HashMap<Integer, Double>();
        populateHashMap();
        rankVariables();
    }

    /**
     * Import cnf file and store clauses in
     * clauseList.
     *
     * @param fileName the file name
     */
    private void importCNF(final String fileName) {
        int clause, i, nextVar, size;
        int tmp[];
        try {
            sc = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            System.exit(1);
        }

        while (sc.findInLine("p cnf") == null) {
            sc.nextLine();
        }

        numVariables = sc.nextInt();
        numClauses = sc.nextInt();
        clauseList = new Clause[numClauses];
        final ArrayList<Integer> list = new ArrayList<Integer>(numVariables/4);

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
        hashMap = new HashMap<Integer, HashObject>(1 + (int) (numVariables / 0.75));
        Clause clauseAtI;
        HashObject hashTmp;
        int clauseVar,clauseVarKey,i,j;
        HashObject prevHashObj;
        int clauseAtISize;
        for (i = 0; i < numClauses; i++) {
            clauseAtI = clauseList[i];
            clauseAtISize = clauseAtI.size();
            for (j = 0; j < clauseAtISize; j++) {
                clauseVar = clauseAtI.get(j);
                clauseVarKey = abs(clauseVar); //abs of variable for key
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
     * Rank the variables for the
     * first time.
     */
    private void rankVariables() {
        int i;

        for (i = 1; i <= numVariables; i++) {     // Creates List
            rankArray[0][i - 1] = i;            // Stores the Variable in the first column
            rankArray[1][i - 1] = 0.0f;//sum;          // Stores the Ranking in the second column
        }

        // mergeSort();
    }

    /**
     * Re-rank variables.
     */
    public void reRankVariables() {
        //Clause tmpClause;
        //boolean swapLargest = false;
        int clength, i, currentMaxKey;
        int maxValueKey = -1;
        float currentMaxRank;
        float sum = 0;
        Double tmp;
        double maxValue = 0;

        int pSize, nSize, bigger, s;
        for (i = shift; i < numVariables; i++) {
            hashObj = hashMap.get((int) rankArray[0][i]);
            if (hashObj == null) {
                rankArray[1][i] = 0; //not sure if good/bad
                continue;
            }
            pSize = hashObj.posSize();
            nSize = hashObj.negSize();
            bigger = nSize < pSize ? pSize : nSize;
            for (s = 0; s < bigger; s++) {
                if (s < pSize) {
                    clength = hashObj.getP(s).size();
                    tmp = powerMap.get(clength);
                    if (tmp != null) {
                        sum += tmp;
                    } else {
                        tmp =  Math.pow(2, (clength * -1));
                        sum += tmp;
                        powerMap.put(clength, tmp);
                    }
                }
                if (s < nSize) {
                    clength = hashObj.getN(s).size();
                    tmp = powerMap.get(clength);
                    if (tmp != null) {
                        sum += tmp;
                    } else {
                        tmp =  Math.pow(2, (clength * -1));
                        sum += tmp;
                        powerMap.put(clength, tmp);
                    }
                }
            }

            if(maxValue < sum ){ //finds if sum is the largest so far
                maxValueKey = i;
                maxValue = sum;
            }

            rankArray[1][i] = sum;          // Stores the Ranking in the second column
            sum = 0;
        }

        //Switch the maxValueKey to the shift position
        currentMaxKey = (int) rankArray[0][shift];
        currentMaxRank = rankArray[1][shift];
        rankArray[0][shift] = rankArray[0][maxValueKey];
        rankArray[1][shift] = rankArray[1][maxValueKey];

        rankArray[0][maxValueKey] = currentMaxKey;
        rankArray[1][maxValueKey] = currentMaxRank;
    }

    private int unitPropCheck(){
        int unitVar, unitKey;
        unitKey = lengthOneCheck();
        if(unitKey != 0){
            unitVar = (unitKey < 0 ) ? (int) rankArray[0][abs(unitKey)]*-1 : (int) rankArray[0][abs(unitKey)];
            shiftToUnit(unitKey);
        }
        else
            unitVar = 0;
        return unitVar;
    }

    private void shiftToUnit(final int unitKey){
        int currentMaxKey;
        float currentMaxRank;
        int absUnitKey = abs(unitKey);

        currentMaxKey = (int) rankArray[0][shift];
        currentMaxRank = rankArray[1][shift];
        rankArray[0][shift] = rankArray[0][absUnitKey];
        rankArray[1][shift] = rankArray[1][absUnitKey];

        rankArray[0][absUnitKey] = currentMaxKey;
        rankArray[1][absUnitKey] = currentMaxRank;
    }

    /**
     * Forward tracks down the tree.
     */
   public void forwardTrack() {
       Clause clause;
       HashObject nextVarObj;
       boolean booleanValue;
       int var, absKey, actualSize, j, i, opsitListSize, listSize, varNeg, key;

       var = unitPropCheck();
       if (var != 0) {
           absKey = abs(var);
       } else {
           reRankVariables();
           var = (int) rankArray[0][shift];
           absKey = abs(var);
       }
       
       nextVarObj =  hashMap.get(absKey);
       hashMap.remove(absKey);
       
       /*
        * This statement determines whether
        * to branch true or false by checking if it has
        * just back tracked or not.
        */
       booleanValue = (!justBackTracked && var > 0) ? true : false;  //
       var = absKey; //abs(var); // always positive: p or n
       varNeg = booleanValue ? -var : var;//var * -1 : var; //flip for negitive: pos * -1 = n : neg * -1 = p
       if (booleanValue) {
           listSize = nextVarObj.posSize();
           opsitListSize = nextVarObj.negSize();
       } else {
           listSize = nextVarObj.negSize();
           opsitListSize = nextVarObj.posSize();
       }

       for (i = 0; i < listSize; i++) {
           clause = (booleanValue) ? (Clause) nextVarObj.getP(i) : (Clause) nextVarObj.getN(i);
           actualSize = clause.actualSize();
           for (j = 0; j < actualSize; j++) {
               key = clause.get(j);
               if (key != 0 && (absKey = abs(key)) != var) {
                   hashObj = (HashObject) hashMap.get(absKey);
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
          ((booleanValue) ? nextVarObj.getN(i) : nextVarObj.getP(i)).removeVar(varNeg);
       }

       hashObjectStack.push(nextVarObj);
       booleanStack.push(booleanValue);
       justBackTracked = false;
       shift++;
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
    private void rePopulate(final int key,final HashObject rePopObj,final boolean varSetTo) {
        Clause clause;
        int var, negkey, rVarSize, rClauseSize, i, j, actualSize;
        if (varSetTo) {
            negkey = -key;
            rVarSize = rePopObj.negSize();
            rClauseSize = rePopObj.posSize();
            for (i = 0; i < rClauseSize; i++) {
                clause = rePopObj.getP(i);
                actualSize = clause.actualSize();
                for (j = 0; j < actualSize; j++) {
                    var = clause.get(j);
                    if (var != 0) {
                        hashObj = hashMap.get(abs(var));
                        if (hashObj != null){
                          if(var > 0) {
                              hashObj.addClausePos(clause);
                          } else {
                              hashObj.addClauseNeg(clause);
                          }
                        }
                    }
                }
            }
            for (i = 0; i < rVarSize; i++) {
                rePopObj.getN(i).addVar(negkey);
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
                        hashObj = hashMap.get(abs(var));
                        if (hashObj != null){
                           if( var > 0) {
                             hashObj.addClausePos(clause);
                           } else {
                             hashObj.addClauseNeg(clause);
                           }
                        }
                    }
                }
            }
            for (i = 0; i < rVarSize; i++) {
                rePopObj.getP(i).addVar(negkey);
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
        final int length = clauseList.length;
        for (int i = 0; i < length; i++) {
            if (((Clause) clauseList[i]).size() == 0) {
                return (clauseSizeZeroResult = true);
            }
        }
        return (clauseSizeZeroResult = false);
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
        return ( !clauseSizeZero() && (hashMap.isEmpty() || allEmptyKeyMap())); // ? true : false;
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
                        return k;
                    }
                }
                size = tmp.negSize();
                for (i = 0; i < size; i++) {
                    tmpVar = tmp.getN(i).lengthOne();
                    if (tmpVar != 0) {
                        return -k;
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
        final float temp[][] = new float[2][numVariables];
        mergeSort(temp, 0 + shift, numVariables - 1);
    }

    /**
     * Merge sort.
     *
     * @param temp the temp Array
     * @param lowerBound the lower bound
     * @param upperBound the upper bound
     */
    private void mergeSort(final float temp[][],final int lowerBound, final int upperBound) {
        if (lowerBound == upperBound) {
            return;                   // If index == 1 do nothing
        } else {
            final int mid = (lowerBound + upperBound) / 2;    // Get midpoint

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

        final int lowerBound = lower;       // saves lower value

        final int lowMid = highMid - 1;       // sets the lower mid point

        final int n = upperBound - lowerBound + 1;  // number of ints in range

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
                System.out.print(-hashObjectStack.pop().getVariableNumber()+" ");
            }
        }
    }
}
