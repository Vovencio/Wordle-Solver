import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Solver solver = new Solver("C:\\Users\\KnyylLapAss\\Documents\\GitHub\\Wordle-Solver\\src\\dictionary.txt");

        solver.testFindWord();
        
        solver.testLimits("C:\\Users\\KnyylLapAss\\Documents\\GitHub\\Wordle-Solver\\python-solver\\test_data.json");
    }
}