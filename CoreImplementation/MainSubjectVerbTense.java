package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class MainSubjectVerbTense {
    public static final String PATTERN =
            "ROOT < (S <, (mainclausesub . (VP=mainclauseverbphrase  <<, VB|VBG|VBN|VBP=verbtobeprocessed)))";
    public Tree resultingTree;
    public MainClauseSubjectVerbConversion mainClauseSubjectVerbConversion = new MainClauseSubjectVerbConversion();

    public MainSubjectVerbTense(Tree sentenceTree, String verb, String verbTag) {
        // deep copy so that the tree does not get altered
        resultingTree = sentenceTree.deepCopy();

        // get the verb to be placed
        String newVerb = mainClauseSubjectVerbConversion.convertToAppropriateForm(verb, verbTag);

        // operation: Tsurgeon command for operating on the labelled tree
        String operation = "replace verbtobeprocessed "+newVerb;

        // can be a list of Tsurgeon operations
        List<String> operations = new ArrayList<>();
        operations.add(operation);

        // tsurgeon operations
        // Note: tsurgeon wrapper operates in place
        TsurgeonWrapper tsurgeon = new TsurgeonWrapper(resultingTree, PATTERN, operations);
    }
}
