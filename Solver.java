/**
 * Main Class for Davis-Putnam Solver
 * @author Trevor Stevens
 */
import java.util.EmptyStackException;

public class Solver {

    public static boolean done = false;

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {
        Formula f = new Formula(args[0]);
        try {
            while (!done) {
                if (f.validSolution()) {
                    done = true;
                } else if (f.getLastClauseSizeResult()) {
                    f.backTrack();
                } else {
                    f.forwardTrack();
                }
            }
        } catch (EmptyStackException e) {
            // Empty Stack print No Solution & Exit
            System.out.println("Unsolvable Solution");
            System.exit(0);
        }

        System.out.println("Solvable Solution");
        f.printSolution();
        System.exit(0);
    }
}