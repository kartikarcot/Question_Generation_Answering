package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class NodePruner {
    public Tree resultingTree;
    NodePruner(Tree sentenceTree, String nodeName) {
        // deep copy so that the tree does not get altered
        resultingTree = sentenceTree.deepCopy();

        // pattern: Tregex for labelling the nodes
        String pattern = nodeName+"=tobepruned";

        // operation: Tsurgeon command for operating on the labelled tree
        String operation = "prune tobepruned";

        // can be a list of Tsurgeon operations
        List<String> operations = new ArrayList<>();
        operations.add(operation);

        // tsurgeon operations
        // Note: tsurgeon wrapper operates in place
        TsurgeonWrapper tsurgeon = new TsurgeonWrapper(resultingTree, pattern, operations);
    }
}
