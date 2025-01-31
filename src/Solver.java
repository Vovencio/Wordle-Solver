import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;

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
    // 1 - Eliminated
    // 0 - Not Eliminated
    // The last element is the amount of eliminations
    private int[] eliminated;
    private int ELIMINATED_NUM;

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

        eliminated = new int[dictionary.length+1];
        ELIMINATED_NUM = dictionary.length;

        // 0 are eliminated at the beginning
        // I know that this is redundant, but it's a reminder for myself
        eliminated[ELIMINATED_NUM] = 0;

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

    // Return the eliminated list with all words without an amount of a letter
    int[] amountLetter(int letter, int num, int[] eliminations) {
        for (int i = 0; i < dictionary.length; i++){
            // Only not eliminated elements
            if (eliminations[i] == 0){
                if (letterCount[i][letter] != num){
                    eliminations[ELIMINATED_NUM]++;
                    eliminations[i] = 1;
                }
            }
        }

        return eliminations;
    }

    // Return the word list without a letter at a place with a specified minimum
    int[] placeLetter(int letter, int place, int minimum, int[] eliminations) {
        for (int i = 0; i < dictionary.length; i++){
            // Only not eliminated elements
            if (eliminations[i] == 0){
                if (letterCount[i][letter] < minimum || letterAt[i][place] == letter){
                    eliminations[ELIMINATED_NUM]++;
                    eliminations[i] = 1;
                }
            }
        }

        return eliminations;
    }

    // Return the word list with a letter at a place
    int[] rightLetter(int letter, int num, int[] eliminations) {
        for (int i = 0; i < dictionary.length; i++){
            // Only not eliminated elements
            if (eliminations[i] == 0){
                if (letterAt[i][num] != letter){
                    eliminations[ELIMINATED_NUM]++;
                    eliminations[i] = 1;
                }
            }
        }

        return eliminations;
    }

    public void limitWords(int[] answer, int word){
        for (int i = 0; i < 5; i++){
            int count;
            switch (answer[i]){
                case 0:
                    count = 0;
                    for (int j = 0; j < 5; j++) {
                        if (i != j) {
                            if (letterAt[word][i] == letterAt[word][j] && answer[j] != 0) {
                                count++;
                            }
                        }
                    }
                    amountLetter(letterAt[word][i], count, eliminated);
                    break;
                case 1:
                    count = 1;
                    for (int j = 0; j < 5; j++) {
                        if (i != j) {
                            if (letterAt[word][i] == letterAt[word][j] && answer[j] != 0) {
                                count++;
                            }
                        }
                    }
                    placeLetter(letterAt[word][i], i, count, eliminated);
                    break;
                case 2:
                    rightLetter(letterAt[word][i], i, eliminated);
                    break;
                default:
                    System.err.println("Something is extremely wrong...");
            }
        }
    }

    public int wordsLeft(){
        return dictionary.length - eliminated[ELIMINATED_NUM];
    }

    public void testLimits(String path){
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            JSONObject json = new JSONObject(content);

            JSONArray ansArraysJson = json.getJSONArray("ansArrays");
            int[][] ansArrays = new int[ansArraysJson.length()][];
            for (int i = 0; i < ansArraysJson.length(); i++) {
                JSONArray innerArray = ansArraysJson.getJSONArray(i);
                ansArrays[i] = new int[innerArray.length()];
                for (int j = 0; j < innerArray.length(); j++) {
                    ansArrays[i][j] = innerArray.getInt(j);
                }
            }

            JSONArray wordsJson = json.getJSONArray("words");
            String[] words = new String[wordsJson.length()];
            for (int i = 0; i < wordsJson.length(); i++) {
                words[i] = wordsJson.getString(i);
            }

            JSONArray expectedLengthsJson = json.getJSONArray("expectedLengths");
            int[] expectedLengths = new int[expectedLengthsJson.length()];
            for (int i = 0; i < expectedLengthsJson.length(); i++) {
                expectedLengths[i] = expectedLengthsJson.getInt(i);
            }

            // Example output to verify data
            System.out.println("Loaded " + words.length + " words.");

            for (int i = 0; i < ansArrays.length; i++){
                Arrays.fill(eliminated, 0);
                limitWords(ansArrays[i], findWord(words[i]));

                if (wordsLeft() != expectedLengths[i]){
                    System.err.printf("Test failed! Word: %s. %nGot: %d, expected %d!" +
                            "%nAnswer: %s", words[i], wordsLeft(), expectedLengths[i], Arrays.toString(ansArrays[i]));
                    return;
                }
            }

            System.out.println("testLimits tests passed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] leftWords(){
        int count = 0;

        // Get the amount of non-eliminated words
        for (int i = 0; i < dictionary.length; i++){
            if (eliminated[i] != 1){
                count++;
            }
        }

        String[] leftWords = new String[count];
        count = 0;
        // Fill the array
        for (int i = 0; i < dictionary.length; i++){
            if (eliminated[i] != 1){
                leftWords[count] = dictionary[i];
                count++;
            }
        }

        return leftWords;
    }
}
