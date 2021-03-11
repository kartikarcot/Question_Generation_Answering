package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

public class MainClauseMatcher {

    public static final String PATTERN =
            "ROOT < (S < (NP|SBAR=mainclausesub $+ /,/ !$++ NP|SBAR))";

    public Tree resultingTree;
    public Tree resultingNode;

    public MainClauseMatcher(Tree sentenceTree) {
        initialize(sentenceTree);
    }

    public void initialize(Tree sentenceTree) {
        TregexMatcherWrapper matcher = new TregexMatcherWrapper(PATTERN, sentenceTree);
        if (matcher.matcher.find()) {
            this.resultingNode = matcher.matcher.getNode("mainclausesub");
        }
        this.resultingTree = matcher.matchingTree;
    }
}
