package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

public class MainClauseMatcher {

    public static final String PATTERN =
            "ROOT < (S <, (NP|SBAR=mainclausesub . (VP=mainclauseverbphrase  <<, VB|VBZ|VBD|VBG|VBN|VBP|VBZ=mainclauseverb)))";

    public Tree resultingTree;
    public Tree resultingSubject;
    public Tree resultingVerbNode;
    public String resultingVerb = null;
    public String resultingVerbTag = null;

    public MainClauseMatcher(Tree sentenceTree) {
        initialize(sentenceTree);
    }

    public void initialize(Tree sentenceTree) {
        Tree sentenceTreeCopied = sentenceTree.deepCopy();
        TregexMatcherWrapper matcher = new TregexMatcherWrapper(PATTERN, sentenceTreeCopied);
        if (matcher.matcher.find()) {
            this.resultingSubject = matcher.matcher.getNode("mainclausesub");

            // if a main clause subject is found, label it
            if (this.resultingSubject != null) {
                this.resultingSubject.label().setValue("mainclausesub");
            }
            this.resultingVerbNode = matcher.matcher.getNode("mainclauseverb");
            if (this.resultingVerbNode != null) {
                resultingVerb = this.resultingVerbNode.getChild(0).toString();
                resultingVerbTag = this.resultingVerbNode.label().toString();
            }
        }
        this.resultingTree = matcher.matchingTree;
    }
}
