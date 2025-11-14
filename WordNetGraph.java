package main;

import edu.princeton.cs.algs4.In;

import java.util.*;

import static java.lang.Integer.parseInt;

/**
// * WordNet
// *
// * A lightweight implementation of a WordNet-style lexical graph.
// *
// * This class:
// *  - Parses two input files:
// *      1) A synsets file mapping IDs → words
// *      2) A hyponyms (edges) file mapping IDs → child IDs
// *  - Builds:
// *      - wordToIDs: mapping from each word to the integer IDs that contain it
// *      - IDtoWords: mapping from each ID to the words it represents
// *      - hash: adjacency list representing the hyponym graph
// *  - Supports hyponym lookup using DFS graph traversal.
// */

public class WordNet {

    private TreeMap<Integer, ArrayList<Integer>> hash;      // adjacency list representing the hyponym graph, parent synset ID -> list of all child synset IDs
    private TreeMap<String, HashSet<Integer>> wordToIDs;      // maps each individual word to all synset IDs containing it
    private TreeMap<Integer, ArrayList<String>> IDtoWords;    // maps each synset ID to the list of words in that synset

    public WordNet(String firstFileName, String secondFileName) {     // parsing of the two files
        hash = new TreeMap<>();
        wordToIDs = new TreeMap<>();
        IDtoWords = new TreeMap<>();
        In firstFile = new In(firstFileName);
        In secondFile = new In(secondFileName);
        while (firstFile.hasNextLine()) {
            String line = firstFile.readLine();
            String[] parts = line.split(",", 3);
            int id = parseInt(parts[0]);
            String[] wordsArray = parts[1].split(" ");
            ArrayList<String> words = new ArrayList<>(Arrays.asList(wordsArray));
            IDtoWords.put(id, words);

            for (String word : wordsArray) {
                if (wordToIDs.containsKey(word)) {
                    wordToIDs.get(word).add(id);
                } else {
                    HashSet<Integer> ids = new HashSet<>();
                    ids.add(id);
                    wordToIDs.put(word, ids);
                }
            }
        }
        while (secondFile.hasNextLine()) {
            String line = secondFile.readLine();
            String parts[] = line.split(",");
            ArrayList<Integer> ids = new ArrayList<>();
            int mainID = Integer.parseInt(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                ids.add(Integer.parseInt(parts[i]));
            }
            hash.put(mainID, ids);
        }
    }

    public void dfs (Integer id, HashSet<Integer> ids) {       // Depth-first search to collect all descendant synset IDs
        if (!ids.contains(id)) {
            ids.add(id);
        }
        if (hash.containsKey(id)) {                   //recurse on children if present
            for (Integer neighbor : hash.get(id)) {
                dfs(neighbor, ids);
            }
        }
    }

    // returns hyponyms for a comma-separated list of words
    // Intersection logic = only keep hyponyms common to all words
  
    public TreeSet<String> getHyponyms(String wordList) {
        String[] words = wordList.split(",");
        HashSet<String> result = getHyponymsSingle(words[0]);
        for (int i = 1; i < words.length; i++) {
            HashSet<String> current = getHyponymsSingle(words[i]);
            HashSet<String> novelty = new HashSet<>();
            for (String s : current) {
                if (result.contains(s)) {
                    novelty.add(s);
                }
            }
            result = novelty;
        }
        return new TreeSet<>(result);
    }

//    /**
//     * Returns all hyponyms of a single word by:
//     *  1. Finding all IDs that contain the word
//     *  2. DFS over the graph to collect descendant IDs
//     *  3. Returning all words in the reachable synsets
//     */
  
    public HashSet<String> getHyponymsSingle(String word) {
        HashSet<Integer> ids = new HashSet<>();
        HashSet<String> words = new HashSet<>();
        if (!wordToIDs.containsKey(word)) {
            return words;
        }
        for (Integer id : wordToIDs.get(word)) {
            dfs(id, ids);
        }
        for (Integer id : ids) {
            ArrayList<String> temporary = IDtoWords.get(id);
            for (String w : temporary) {
                words.add(w);

            }
        }
        return words;
    }
}

