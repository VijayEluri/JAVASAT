/**
 * Main Class for Davis-Putnam Solver
 * @author Trevor Stevens
 */
import java.util.EmptyStackException;

public final class Solver {

    public static boolean done = false;


    private Solver(){

    }
    /**
     * @param args the command line arguments
     */

    public static void main(final String[] args) {
        final Formula formula = new Formula(args[0]);
        try {
            while (!done) {
                if (formula.validSolution()) {
                    done = true;
                } else if (formula.getLastClauseSizeResult()) {
                    formula.backTrack();
                } else {
                    formula.forwardTrack();
                }
            }
        } catch (EmptyStackException e) {
            // Empty Stack print No Solution & Exit
            System.out.println("Unsolvable Solution");
            System.exit(0);
        }

        System.out.println("Solvable Solution");
        formula.printSolution();
        System.exit(0);
    }
}