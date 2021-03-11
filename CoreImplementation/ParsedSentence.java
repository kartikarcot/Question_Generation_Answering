package CoreImplementation;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class ParsedSentence {
    public Tree sentenceTree;
    public String sentenceText;
    public List<String> sentenceTokens;
    public List<String> sentenceTags;

    public ParsedSentence(CoreSentence sentence) {
        // parse the sentence into a tree
        sentenceTree = sentence.constituencyParse();

        // store all the tokens
        sentenceTokens = new ArrayList<>();
        List<Label> originalTokenArray = sentenceTree.yield();
        for (int i = 0; i < originalTokenArray.size(); i++) {
            sentenceTokens.add(originalTokenArray.get(i).toString());
        }

        // store all the tags
        sentenceTags = sentence.nerTags();

        // store raw sentence
        sentenceText = sentence.text();
    }

    public void print(boolean debugMode) {
        if (!debugMode) return;
        System.out.println("-------------------------------------------------------");
        System.out.println(sentenceText);
        System.out.println("Sentence Tree: "+ sentenceTree);
        System.out.print("Tokens:");
        for (String token : sentenceTokens) {
            System.out.print(" " + token);
        }
        System.out.println();
        System.out.print("Tags:");
        for (String tag : sentenceTags) {
            System.out.print(" " + tag);
        }
        System.out.println();
        System.out.println("-------------------------------------------------------");
    }
}
