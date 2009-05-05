
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

/**
 * The Class Formula.
 * 
 * @author Trevor Stevens & Brian Schreiber
 */
public class Formula {

    private Object clauseList[];
    private float rankArray[][];
    private Stack<Boolean> booleanStack = new Stack<Boolean>();
    private Stack<HashObject> hashObjectStack = new Stack<HashObject>();
    private Scanner sc;
    private HashMap<Integer, HashObject> hashMap;
    private HashObject hashObj;
    public int numVariables;
    private int numClauses;
    private int clauseLength;
    public int shift = 0;
    private int key;
    private boolean justBackTracked = false;
    private double powArray[];
    private Stack<Integer> PropVarStack = new Stack< Integer>();
    /**
     * Instantiates a new formula.
     * 
     * @param fileName the file name
     */
    Formula(String fileName) {
        importCNF(fileName);
        rankArray = new float[2][numVariables];
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
        try {
            sc = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (sc.findInLine("p cnf") == null) {
            sc.nextLine();
        }

        sc.findInLine("p cnf");
        numVariables = sc.nextInt();			// Reading in number of variables.

        numClauses = sc.nextInt();				// Reading in number of clauses

        clauseList = new Object[numClauses];
        ArrayList<Integer> list = new ArrayList<Integer>();

        /*
         * Populate the clause list
         */
        for (int clause = 0; sc.hasNextInt(); clause++) {
            int nextVar = sc.nextInt();

            while (nextVar != 0) {
                list.add(nextVar);
                if (nextVar == 0) {
                    break;
                }
                nextVar = sc.nextInt();
            }
            if (clause == 0) {
                clauseLength = list.size();
            }
            int tmp[] = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                tmp[i] = list.get(i);
            }

            Clause tmpClause = new Clause(tmp.length);
            tmpClause.addArray(tmp);
            tmpClause.setClauseNumber(clause);
            clauseList[clause] = tmpClause;
            list.clear();
        } // End for

    }

    /**
     * Populate the hash map.
     */
    private void populateHashMap() {
        hashMap = new HashMap<Integer, HashObject>(numVariables);
        Clause clauseAtI;
        int clauseVar;
        int clauseVarKey;
        HashObject prevHashObj;

        for (int i = 0; i < numClauses; i++) {
            clauseAtI = (Clause) clauseList[i];
            for (int j = 0; j < clauseAtI.size(); j++) {

                clauseVar = clauseAtI.get(j);

                clauseVarKey = Math.abs(clauseVar); //abs of variable for key

                prevHashObj = (HashObject) hashMap.get(clauseVarKey); //hashobj for key clauseVar

                if (prevHashObj == null) {
                    HashObject hashTmp = new HashObject();  //numClauses, numVariables, clauseLength);
                    hashMap.put(clauseVarKey, hashTmp);
                    prevHashObj = (HashObject) hashMap.get(clauseVarKey);
                }

                if (clauseVar > 0) {
                    prevHashObj.addClausePos(clauseAtI);
                } else {
                    prevHashObj.addClauseNeg(clauseAtI);
                }
                hashMap.put(clauseVarKey, prevHashObj);
            }
        }

    }

    /**
     * Fill pow array, with a few of
     * 2^n powers.
     */
    private void fillPowArray() {
        powArray = new double[15];
        for (int i = 0; i < 15; i++) {
            powArray[i] = Math.pow(2, i * -1);
        }
    }

    /**
     * Rank the variables for the
     * first time.
     */
    private void rankVariables() {
        int clength;
        int size;
        float sum = 0;

        fillPowArray();

        for (int i = 1; i <= numVariables; i++) {			// Creates List

            hashObj = (HashObject) hashMap.get(i);
            if (hashObj == null) {
                System.out.println("Null at i of " + i);
            } else {
                size = hashObj.posSize();
                for (int j = 0; j < size; j++) {				// Sums the rank in the posList

                    Clause tmpClause = hashObj.getP(j);
                    clength = tmpClause.size();
                    if (clength < powArray.length) {
                        sum += powArray[clength];
                    } else {
                        sum += Math.pow(2, (clength * -1));
                    }
                }
                size = hashObj.negSize();
                for (int j = 0; j < size; j++) {				// Sums the rank in the negList

                    clength = hashObj.getN(j).size();
                    if (clength < powArray.length) {
                        sum += powArray[clength];
                    } else {
                        sum += Math.pow(2, (clength * -1));
                    }
                }
                rankArray[0][i - 1] = i;						// Stores the Variable in the first column
                rankArray[1][i - 1] = sum;					// Stores the Ranking in the second column
                sum = 0;
            }

        } // end for loop

        mergeSort();
    }

    /**
     * Re-rank variables.
     */
    public void reRankVariables() {
        int clength;
        int size;
        double maxValue;
        double checkValue = 0;
        int maxValueKey = -1;
        int currentMaxKey;
        boolean swapLargest;
        float sum = 0;
        float currentMaxRank;
        int one = 0;
        Clause tmpClause;
        //Length addition commented out
        //Runs slower than external method
//        int oneClause[][];
//        oneClause = new int[numClauses][2];
//        int tmp;

        for (int i = shift; i < numVariables; i++) {
            hashObj = (HashObject) hashMap.get((int) rankArray[0][i]);
            if (hashObj == null) {
                //System.out.println("Null");
            } else {
                size = hashObj.posSize();
                for (int j = 0; j < size; j++) {				// Sums the rank in the posList
                    tmpClause = hashObj.getP(j);
                    clength = tmpClause.size();
//                    oneClause[tmpClause.ClauseNumber()][0] += 1;
                    if (clength < powArray.length) {
                        sum += powArray[clength];
                    } else {
                        sum += Math.pow(2, (clength * -1));
                    }
                }
                size = hashObj.negSize();
                for (int j = 0; j < size; j++) {				// Sums the rank in the negList

                    tmpClause = hashObj.getN(j);
                    clength = tmpClause.size();
//                    oneClause[tmpClause.ClauseNumber()][1] += 1;
                    if (clength < powArray.length) {
                        sum += powArray[clength];
                    } else {
                        sum += Math.pow(2, (clength * -1));
                    }
                }
                rankArray[1][i] = sum;					// Stores the Ranking in the second column

                sum = 0;
            }
        }
        maxValue = checkValue;
        swapLargest = false;
        for (int i = shift; i < numVariables; i++) { //Move largest var to shift

            checkValue = rankArray[1][i];
            if (maxValue < checkValue) {
                maxValueKey = i;
                maxValue = checkValue;
                swapLargest = true;
            }
        }

        //Tried to add length check into ranking made program slower
        //boolean finished = false;

//        for (int i = 0; i < numClauses && !finished; i++) {
//            if (oneClause[i][0] == 1 && oneClause[i][1] == 0) {
//                tmpClause = (Clause) clauseList[i];
//                for (int j = 0; j < tmpClause.actualSize(); j++) {
//                    tmp = tmpClause.get(j);
//                    if (tmp != 0) {
//                        one = tmp;
//                        finished = true;
//                    }
//                }
//            } else if (oneClause[i][1] == 1 && oneClause[i][0] == 0) {
//                tmpClause = (Clause) clauseList[i];
//                for (int j = 0; j < tmpClause.actualSize(); j++) {
//                    tmp = tmpClause.get(j);
//                    if (one != 0) {
//                        one = tmp;
//                        finished = true;
//                    }
//
//                }
//            }
//        }

        // Might be able to use ranking number for length one check
        // Set unit variable as shift if one exists
        one = lenghtOneCheck();
        if (one != 0) {
            for (int i = shift; i < numVariables; i++) {
                if (Math.abs(one) == (int) rankArray[0][i]) {
                    maxValue = rankArray[0][i];
                    maxValueKey = i;
                    PropVarStack.push(one);
                }

            }
        } else {
            PropVarStack.push(0);
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
        int var;
        int absKey;
        int varNeg = 0;
        //int oneCheck = lenghtOneCheck();
        if(PropVarStack.peek() != 0){
            var = PropVarStack.pop();
            nextVarObj = (HashObject) hashMap.get(Math.abs(var));
            hashMap.remove(Math.abs(var));
        }
        else{
            var = (int) rankArray[0][shift];
            nextVarObj = (HashObject) hashMap.get(Math.abs(var));
            hashMap.remove(Math.abs(var));
            //PropVarStack.pop();
        }
//        }
        /*
         * This if and else statement determine whether
         * to branch true or false by checking if it has
         * just back tracked or not.
         */
        if (!justBackTracked && var > 0 ) {
            booleanValue = true;
            varNeg = var * -1;
            ///////
            int listSize = nextVarObj.posSize();
            int opsitListSize = nextVarObj.negSize();

            for (int i = 0; i < listSize; i++) {
                clause = (Clause) nextVarObj.getP(i);
                for (int j = 0; j < clause.actualSize(); j++) {
                    key = clause.get(j);
                    absKey = Math.abs(key);
                    if (key != 0 && absKey != var) {
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
            for (int i = 0; i < opsitListSize; i++) {
                ((Clause) nextVarObj.getN(i)).removeVar(varNeg);
            }

            hashObjectStack.push(nextVarObj);
            booleanStack.push(booleanValue);
            justBackTracked = false;
            shift++;

        //////
        } else {
            var = Math.abs(var);
            booleanValue = false;
            varNeg = var;
            //////
            int listSize = nextVarObj.negSize();
            int opsitListSize = nextVarObj.posSize();

            for (int i = 0; i < listSize; i++) {
                clause = (Clause) nextVarObj.getN(i);
                for (int j = 0; j < clause.actualSize(); j++) {
                    key = clause.get(j);
                    absKey = Math.abs(key);
                    if (key != 0 && absKey != var) {
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
            for (int i = 0; i < opsitListSize; i++) {
                ((Clause) nextVarObj.getP(i)).removeVar(varNeg);
            }

            hashObjectStack.push(nextVarObj);
            booleanStack.push(booleanValue);
            justBackTracked = false;
            shift++;

        //////
        }
    }

    /**
     * Back tracks up the tree.
     */
    public void backTrack() {
        boolean propBacktrackF = false;
        if (!PropVarStack.isEmpty() && PropVarStack.peek() != 0) {
            propBacktrackF = (PropVarStack.pop() < 0);
        }
        try {
            while (propBacktrackF || !(Boolean) booleanStack.pop()) {
                shift--;
                int insertKey = (int) rankArray[0][shift];//Added absolute value
                HashObject insertObj = (HashObject) hashObjectStack.pop();
                rePopulate2(insertKey, insertObj, false);
                if (!PropVarStack.isEmpty() && PropVarStack.peek() != 0) {
                    propBacktrackF = (PropVarStack.pop() < 0);
                } else {
                    propBacktrackF = false;
                }
            }
            shift--;
            int insertKey = (int) rankArray[0][shift]; //InsertKey Will be positive REMOVE ABS!!!!!
            HashObject insertObj = (HashObject) hashObjectStack.pop();
            rePopulate2(insertKey, insertObj, true);
            justBackTracked = true;
        } catch (EmptyStackException e) {
            System.out.print("Un");		//Concatenates with solvable solution in main mwahahahaahhaa
            Main.done = true;
        }
    }

    /**
     * Re-populate the hashMap
     * 
     * @param key the key
     * @param rePopObj the object being repopulated
     * @param varSetTo the boolean value of the object
     */
    private void rePopulate2(int key, HashObject rePopObj, boolean varSetTo) {
//    	System.out.println("Repopulating: ...");
        Clause clause;
        int var;
        int negkey;
        int rVarSize;
        int rClauseSize;

        if (varSetTo) {
            negkey = key * -1;
            /////

            rVarSize = rePopObj.negSize();
            rClauseSize = rePopObj.posSize();

            //Return Clauses GOOOO
            for (int i = 0; i < rClauseSize; i++) {
                clause = rePopObj.getP(i);
                for (int j = 0; j < clause.actualSize(); j++) {
                    var = clause.get(j);
                    if (var != 0) {
                        hashObj = hashMap.get(Math.abs(var));
                        if (hashObj != null && var > 0) {
                            hashObj.addClausePos(clause);
                        } else if (hashObj != null) {
                            hashObj.addClauseNeg(clause);
                        }
                    }// End if(var == 0)

                }// End for (j < clause.actualSize())   		

            }// End for(i < rClauseSize)

            //Return Negated Vars Hoooooo!
            for (int i = 0; i < rVarSize; i++) {
                clause = rePopObj.getN(i);
                clause.addVar(negkey);
            }
            hashMap.put(key, rePopObj);


        /////
        } else {
            negkey = key;
            ///////////

            rVarSize = rePopObj.posSize();
            rClauseSize = rePopObj.negSize();

            //Return Clauses GOOOO
            for (int i = 0; i < rClauseSize; i++) {
                clause = rePopObj.getN(i);
                for (int j = 0; j < clause.actualSize(); j++) {
                    var = clause.get(j);
                    if (var != 0) {
                        hashObj = hashMap.get(Math.abs(var));
                        if (hashObj != null && var > 0) {
                            hashObj.addClausePos(clause);
                        } else if (hashObj != null) {
                            hashObj.addClauseNeg(clause);
                        }
                    }// End if(var == 0)

                }// End for (j < clause.actualSize())   		

            }// End for(i < rClauseSize)

            //Return Negated Vars Hoooooo!
            for (int i = 0; i < rVarSize; i++) {
                clause = rePopObj.getP(i);
                clause.addVar(negkey);
            }
            hashMap.put(key, rePopObj);

        ///////

        }

    }

    /**
     * Check for if a clause is size zero.
     * 
     * @return true, if successful
     */
    public boolean clauseSizeZero() {

        for (int i = 0; i < clauseList.length; i++) {
            if (((Clause) clauseList[i]).size() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if there it is a valid solution.
     * 
     * @return true, if successful
     */
    public boolean validSolution() {
        if ((hashMap.isEmpty() || allEmptyKeyMap()) && !clauseSizeZero()) {
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
        Collection<HashObject> remKeys = hashMap.values();
        Iterator<HashObject> keyIterator = remKeys.iterator();
        while (keyIterator.hasNext()) {
            HashObject tmp = (HashObject) keyIterator.next();
            if (!tmp.posEmpty() || !tmp.negEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int lenghtOneCheck() {
        int size;
        int var;
        int tmp;
        Clause tmpClause;
        int oneClause[][];
        Set<Integer> remKeys = hashMap.keySet();
        oneClause = new int[numClauses] [2];
        Iterator<Integer> keyIterator = remKeys.iterator();
        while (keyIterator.hasNext()) {
            var = keyIterator.next();
            hashObj = (HashObject) hashMap.get(var);
            if (hashObj == null) {
            } else {
                size = hashObj.posSize();
                for (int j = 0; j < size; j++) {
                    tmpClause = hashObj.getP(j);
                    oneClause[tmpClause.ClauseNumber()][0] += 1;
                }
                size = hashObj.negSize();
                for (int j = 0; j < size; j++) {
                    tmpClause = hashObj.getN(j);
                    oneClause[tmpClause.ClauseNumber()][1] += 1;
                }
            }
        }
        for(int i =0; i< numClauses;i++){
            if(oneClause[i][0] == 1 && oneClause[i][1] == 0){
                tmpClause = (Clause)clauseList[i];
                for(int j = 0; j < tmpClause.actualSize(); j++){
                    tmp = tmpClause.get(j);
                    if(tmp != 0){
                        return tmp;
                    }
                }
            }
            else if(oneClause[i][1] == 1 && oneClause[i][0] == 0){
                tmpClause = (Clause)clauseList[i];
                for(int j = 0; j < tmpClause.actualSize(); j++){
                    tmp = tmpClause.get(j);
                    if(tmp != 0){
                        return tmp;
                    }
                }
            }
        }
        return 0;
    }
    //move to seperate class?
    /**
     * Merge sort.
     */
    private void mergeSort() {
        float temp[][] = new float[2][numVariables];
        mergeSort(temp, 0 + shift, numVariables - 1);
    } // End mergeSort()


    /**
     * Merge sort.
     * 
     * @param temp the temp Array
     * @param lowerBound the lower bound
     * @param upperBound the upper bound
     */
    private void mergeSort(float temp[][], int lowerBound, int upperBound) {
        if (lowerBound == upperBound) {
            return;										// If index == 1 do nothing

        } else {
            int mid = (lowerBound + upperBound) / 2;		// Get midpoint

            mergeSort(temp, lowerBound, mid);			// Lower Half

            mergeSort(temp, mid + 1, upperBound);			// Upper Half

            merge(temp, lowerBound, mid + 1, upperBound);	// Merge both Halves

        }  // End else

    } // End mergSort(int[],int,int)


    /**
     * Merge.
     * 
     * @param temp the temp array
     * @param lower the lower
     * @param highMid the high mid
     * @param upperBound the upper bound
     */
    private void merge(float temp[][], int lower, int highMid, int upperBound) {
        int i = 0;							// temp index

        int lowerBound = lower;				// saves lower value

        int lowMid = highMid - 1;				// sets the lower mid point

        int n = upperBound - lowerBound + 1;	// number of ints in range

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
    } // End merge(int[],int,int,int)

}

