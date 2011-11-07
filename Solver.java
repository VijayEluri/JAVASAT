import java.util.NoSuchElementException;

public final class Solver {

    public static void main(final String[] args) {
        final Formula formula = new Formula(args[0]);
        try {
            while (!formula.validSolution()) {
                if (formula.getCachedClauseSizeZeroResult()) {
                    formula.backTrack();
                } else {
                    formula.forwardTrack();
                }
            }
        } catch (NoSuchElementException e) {
            // Empty Stack print No Solution & Exit
            System.out.println("Unsolvable Solution");
            System.exit(0);
        }

        System.out.println("Solvable Solution");
        formula.printSolution();
        System.exit(0);
    }
}