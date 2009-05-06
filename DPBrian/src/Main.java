/**
 * Main Class for Davis-Putnam Solver
 * @author trevorstevens
 */
public class Main {

    public static boolean done = false;

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        Formula f = new Formula(args[0]);
        while (!done) {
            if (f.validSolution()) {
                done = true;
            } else if (f.getLastClauseSizeResult()){   //f.clauseSizeZero()) {
                f.backTrack();
            } else {
                f.forwardTrack();
            }
        }
        System.out.println("solvable Solution");
        System.exit(0);
    }
}
