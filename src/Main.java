import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Solver solver = new Solver("dictionary.txt");

        System.out.println(solver.wordsLeft());
        solver.limitWords(new int[] {0, 0, 0, 0, 0}, solver.findWord("hello"));
        System.out.println(solver.wordsLeft());
    }
}