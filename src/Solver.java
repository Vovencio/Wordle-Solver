import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Solver {

    // Array of all words
    private String[] dictionary;

    // Metadata for faster calculation
    //
    // First Dimension:  Word
    // Second Dimension: Letter
    // Third Dimension:  Place
    // -> 0-4 are indexes; 5 is if it contains the letter at all.
    private int[][][] doesContain;
    private int[][] letterAt;
    private int[][] letterCount;

    // Is the word eliminated? Should it be considered in further searches?
    private boolean[] eliminated;

    public void loadFromFile(String filePath){

        List<String> linesList;

        // Read all lines into a List
        try {
            linesList = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] linesArray = linesList.toArray(new String[0]);

        dictionary = Arrays.stream(linesArray)
                .filter(s -> s.matches("[a-z]{5}"))
                .toArray(String[]::new);
    }

    public void computeWords(){
        for (int i = 0; i < dictionary.length ; i++){
            String currentWord = dictionary[i];
            for (int l = 0; l < 5; l++){
                int currentChar = currentWord.charAt(l) - 'a';
                doesContain[i][currentChar][l]++;
                doesContain[i][currentChar][5]++;
                letterAt[i][l] = currentChar;

                letterCount[i][currentChar]++;
            }
        }
    }

    public int findWord(String word){
        int searchedID = wordToInt(word);

        int upperBoundary = dictionary.length - 1;
        int lowerBoundary = 0;

        while (lowerBoundary <= upperBoundary) {
            int x = (upperBoundary + lowerBoundary) / 2; // Calculate middle index
            int val = wordToInt(dictionary[x]);

            if (val > searchedID) upperBoundary = x - 1;
            else if (val < searchedID) lowerBoundary = x + 1;
            if (val == searchedID) return x;
        }

        System.err.printf("Did not find the word %s.\n", word);
        return -1;
    }

    public void testFindWord(){
        for (int i = 0; i < dictionary.length; i++){
            if (findWord(dictionary[i]) != i){
                System.err.printf("Did not find the word %s.\n", dictionary[i]);
                System.err.printf("Expected %d, Received: %d .\n", i, findWord(dictionary[i]));
                System.err.printf("Expected %s, Received: %s .\n", dictionary[i], dictionary[findWord(dictionary[i])]);
                return;
            }
        }
        System.out.println("testFindWord tests passed!");
    }

    int wordToInt(String word){
        // Every wordle word should get a unique ID
        int num = 0;
        for (int i = 0; i < 5; i++){
            int currentChar = word.charAt(i) - 'a';
            num += (int) (currentChar * Math.pow(26, 4 - i));
        }

        return num;
    }

    public Solver(String path){
        loadFromFile(path);
        doesContain = new int[dictionary.length][26][6];
        letterCount = new int[dictionary.length][26];
        letterAt = new int[dictionary.length][5];

        computeWords();
    }

    public int[] answerArray(int word, int goal){
        // Answer Array as such:
        // 0: Gray, the letter is not present
        // 1: Yellow, the letter is on the wrong spot
        // 2: Green, the letter exists on the given spot

        int[] answer = new int[5];
        int[] usedTimes = new int[26];

        for (int i = 0; i < 5; i++){
            // Green letter
            if (letterAt[word][i] == letterAt[goal][i]){
                answer[i] = 2;
                usedTimes[letterAt[word][i]]++;
            }
        }

        for (int i = 0; i < 5; i++){
            // Yellow letter
            if (letterAt[word][i] != letterAt[goal][i]
                    && doesContain[goal][letterAt[word][i]][5] - usedTimes[letterAt[word][i]] > 0
                    && answer[i] == 0){
                answer[i] = 1;
                usedTimes[letterAt[word][i]]++;
            }
        }

        return answer;
    }
}
