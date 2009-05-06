/*
 * Contains 2 ArrayLists for Pos and Neg Clauses
 * Contains the clauses that each Variable is contained in
 */

import java.util.ArrayList;

/**
 *
 * @author trevorstevens
 */
public class HashObject {

    ArrayList<Clause> pos;
    ArrayList<Clause> neg;
    int variableNumber;
    HashObject(){
        pos = new ArrayList<Clause>();
        neg = new ArrayList<Clause>();
    }

    public void removeClause(Clause clause) {
        pos.remove(clause);
        neg.remove(clause);
    }

    public void addClausePos(Clause clause) {
        pos.add(clause);
    }

    public void addClauseNeg(Clause clause) {
        neg.add(clause);

    }

    public Clause getP(int k) {
        return pos.get(k);
    }

    public Clause getN(int k) {
        return neg.get(k);
    }

    public int posSize() {
        return pos.size();
    }

    public int negSize() {
        return neg.size();
    }

    boolean posEmpty() {
        return pos.size() == 0;
    }

    boolean negEmpty() {
        return neg.size() == 0;
    }

    public void createAddPos(Clause clause) {
        pos.add(clause);
    }

    public void createAddNeg(Clause clause) {
        neg.add(clause);
    }
    
    @Override
    public String toString(){
        String returnString = pos.toString()+" ";
        returnString += neg.toString();
        return returnString;
    }

    public void variableNumber(int variable) {
        variableNumber = variable;
    }
    public int getVariableNumber(){
        return variableNumber;
    }
}
