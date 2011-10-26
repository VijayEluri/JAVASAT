import java.util.ArrayList;

/**
 *
 * @author Trevor Stevems
 */
public class HashObject {

    final private ArrayList<Clause> pos;
    final private ArrayList<Clause> neg;
    private int variableNumber;
    public HashObject() {
        pos = new ArrayList<Clause>();
        neg = new ArrayList<Clause>();
    }

    public void removeClause(final Clause clause) {
        pos.remove(clause);
        neg.remove(clause);
    }

    public void addClausePos(final Clause clause) {
        pos.add(clause);
    }

    public void addClauseNeg(final Clause clause) {
        neg.add(clause);
    }

    public Clause getP(final int clause) {
        return pos.get(clause);
    }

    public Clause getN(final int clause) {
        return neg.get(clause);
    }

    public int posSize() {
        return pos.size();
    }

    public int negSize() {
        return neg.size();
    }

    public boolean posEmpty() {
        return pos.isEmpty();
    }

    public boolean negEmpty() {
        return neg.isEmpty();
    }

    //  Factor out these methods duplicate function above.
    //  Refactor code to use other methods.
    public void createAddPos(final Clause clause) {
        pos.add(clause);
    }

    public void createAddNeg(final Clause clause) {
        neg.add(clause);
    }

    @Override
    public String toString() {
        String returnString = pos.toString()+" ";
        returnString += neg.toString();
        return returnString;
    }

    public void variableNumber(final int variable) {
        variableNumber = variable;
    }

    public int getVariableNumber() {
        return variableNumber;
    }
}
