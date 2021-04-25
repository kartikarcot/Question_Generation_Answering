package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class PrepositionalPhraseComma {
    public Tree resultingTree;

    public PrepositionalPhraseComma (Tree inputTree) {
        initialize(inputTree);
    }

    public void initialize(Tree inputTree) {
        // deep copy so that the tree does not get altered
        resultingTree = inputTree.deepCopy();

        // pattern: Tregex for labelling the nodes
        //// System.out.println("PP Input Tree: "+ resultingTree);
        String pattern = "SQ|S=toplevel < ((@/PP/=pptobesurrounded !$-/,/) $+ /,/=commaremove )";


        // operation: Tsurgeon command for operating on the labelled tree
        String operation = "[prune commaremove] [move pptobesurrounded >-2 toplevel]";

        // can be a list of Tsurgeon operations
        List<String> operations = new ArrayList<>();
        operations.add(operation);

        // tsurgeon operations
        // Note: tsurgeon wrapper operates in place
        TsurgeonWrapper tsurgeon = new TsurgeonWrapper(resultingTree, pattern, operations);
    }
}
