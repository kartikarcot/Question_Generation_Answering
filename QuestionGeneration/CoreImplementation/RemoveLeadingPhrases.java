package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class RemoveLeadingPhrases {
    public Tree resultingTree;
    RemoveLeadingPhrases(Tree sentenceTree) {
        // deep copy so that the tree does not get altered
        resultingTree = sentenceTree.deepCopy();

        // pattern: Tregex for labelling the nodes
        // Reference: Heilman's work: http://www.cs.cmu.edu/~ark/mheilman/questions/
        String pattern = "(/,/=commaToDelete $- (PP|ADVP|ADJP=ppToDelete [!<< NP])) !$-- NP";

        // operation: Tsurgeon command for operating on the labelled tree
        String operation1 = "prune commaToDelete";
        String operation2 = "prune ppToDelete";


        // can be a list of Tsurgeon operations
        List<String> operations = new ArrayList<>();
        operations.add(operation1);
        operations.add(operation2);

        // tsurgeon operations
        // Note: tsurgeon wrapper operates in place
        TsurgeonWrapper tsurgeon = new TsurgeonWrapper(resultingTree, pattern, operations);
    }
}
