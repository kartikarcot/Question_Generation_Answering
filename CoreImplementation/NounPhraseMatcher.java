package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class NounPhraseMatcher {
    public static final String PATTERN =
            "NP=nounphrase";

    public Tree treeWithNounPhrasesMarked;
    public List<Tree> resultingNodes;

    public NounPhraseMatcher(Tree sentenceTree) {
        initialize(sentenceTree);
    }

    public void initialize(Tree sentenceTree) {
        // deep copy so that the input tree is not altered
        treeWithNounPhrasesMarked = sentenceTree.deepCopy();

        // initialize matcher
        // note TregexMatcherWrapper operates inplace for the tree
        TregexMatcherWrapper matcher = new TregexMatcherWrapper(PATTERN, treeWithNounPhrasesMarked);

        // find the matched patterns
        resultingNodes = new ArrayList<>();
        int counter = 0;
        while (matcher.matcher.find()) {
            // store the found nodes
            Tree node = matcher.matcher.getNode("nounphrase");

            // mark the NP's with their indices
            node.label().setValue(node.label().toString()+Integer.toString(counter++));

            resultingNodes.add(node);
        }
    }
}
