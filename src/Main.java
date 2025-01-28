import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Solver solver = new Solver("path.txt");

        System.out.println(Arrays.toString(solver.answerArray(solver.findWord("sassy"), solver.findWord("essay"))));
    }
}