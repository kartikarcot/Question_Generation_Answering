package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class RemoveUselessPredicate {

    Tree resultingTree;

    public RemoveUselessPredicate(Tree inputTree) {
        initialize(inputTree);
    }

    public void initialize(Tree inputTree) {
        // deep copy so that the tree does not get altered
        resultingTree = inputTree.deepCopy();

        // pattern: Tregex for labelling the nodes
        String pattern = "/,/=tobepruned1 $+ SBAR=tobepruned2";

        // operation: Tsurgeon command for operating on the labelled tree
        String operation1 = "prune tobepruned1";
        String operation2 = "prune tobepruned2";

        // can be a list of Tsurgeon operations
        List<String> operations = new ArrayList<>();
        operations.add(operation1);
        operations.add(operation2);

        // tsurgeon operations
        // Note: tsurgeon wrapper operates in place
        TsurgeonWrapper tsurgeon = new TsurgeonWrapper(resultingTree, pattern, operations);

        // pattern: Tregex for labelling the nodes
        pattern = "SBAR=tobepruned !<<, that";

        // operation: Tsurgeon command for operating on the labelled tree
        String operation = "prune tobepruned";

        // can be a list of Tsurgeon operations
        operations = new ArrayList<>();
        operations.add(operation);
        tsurgeon = new TsurgeonWrapper(resultingTree, pattern, operations);
    }
}
