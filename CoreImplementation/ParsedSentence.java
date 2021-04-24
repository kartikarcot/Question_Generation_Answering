package CoreImplementation;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedSentence {
    public Tree sentenceTree;
    public String sentenceText;
    public List<String> sentenceTokens;
    public List<String> sentenceTags;
    public Map<String, String> tagMap;

    public String corefResolvedSentenceText;
    public List<String> corefResolvedSentenceTokens;

    public ParsedSentence(CoreSentence sentence) {
        // parse the sentence into a tree
//        sentenceTree = sentence.constituencyParse();


        // store all the tokens
        sentenceTokens = new ArrayList<>();
        List<Label> originalTokenArray = sentence.constituencyParse().yield();
        for (int i = 0; i < originalTokenArray.size(); i++) {
            sentenceTokens.add(originalTokenArray.get(i).toString());
        }

        // store all the tags
        sentenceTags = sentence.nerTags();

        // store raw sentence
//        sentenceText = sentence.text();

        // create map
        tagMap = new HashMap<>();
        for (int i = 0; i < sentenceTags.size(); i++) {
            tagMap.put(originalTokenArray.get(i).value(), sentenceTags.get(i));
        }

        // simplify the sentence
        sentenceTree = SentenceSimplifier.simplify(sentence.constituencyParse());
        sentenceText =sentenceTree.getLeaves().stream().map(element-> element.value()).reduce("", (e1,e2) -> e1+" "+e2);

    }
    public ParsedSentence(CoreSentence sentence, List<String> corefResolvedSentence) {

        // parse the sentence into a tree
        sentenceTree = sentence.constituencyParse();

        // store all the tokens
        sentenceTokens = new ArrayList<>();
        corefResolvedSentenceTokens = new ArrayList<>();

        List<Label> originalTokenArray = sentenceTree.yield();

        for (int i = 0; i < originalTokenArray.size(); i++) {
            sentenceTokens.add(originalTokenArray.get(i).toString());
            corefResolvedSentenceTokens.add(corefResolvedSentence.get(i));
        }

        // store all the tags
        sentenceTags = sentence.nerTags();

        // store raw sentence
        sentenceText = sentence.text();

        // create map
        tagMap = new HashMap<>();
        for (int i = 0; i < sentenceTags.size(); i++) {
            tagMap.put(originalTokenArray.get(i).value(), sentenceTags.get(i));
        }

        // store corref resolved raw sentence
        corefResolvedSentenceText = String.join(" ", corefResolvedSentence);

    }

    public void print(boolean debugMode) {
        if (!debugMode) return;
        // System.out.println("-------------------------------------------------------");
        // System.out.println(sentenceText);
        // System.out.println("Sentence Tree: "+ sentenceTree);
        System.out.print("Tokens:");
        for (String token : sentenceTokens) {
            System.out.print(" " + token);
        }
        // System.out.println();
        System.out.print("NER Tags:");
        for (String tag : sentenceTags) {
            System.out.print(" " + tag);
        }
        // System.out.println();
        // System.out.println("Tag Maps:");
        for (Map.Entry<String, String>entry : tagMap.entrySet()) {
            // System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        // System.out.println("-------------------------------------------------------");
    }
}
