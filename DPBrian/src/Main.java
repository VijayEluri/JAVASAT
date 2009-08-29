/**
 * Main Class for Davis-Putnam Solver
 * @author Trevor Stevens
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
            } else if (f.getLastClauseSizeResult()){
                f.backTrack();
            } else {
                f.forwardTrack();
            }
        }
        System.out.println("solvable Solution");
        //Add method of displaying solved puzzle.
        // Do not run if no solution.
        System.exit(0);
    }
}