package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

public class MainClauseMatcher {

    public static final String PATTERN1 =
            "ROOT < (S <, (NP|SBAR=mainclausesub . (VP=mainclauseverbphrase  <<, VB|VBZ|VBD|VBG|VBN|VBP|VBZ=mainclauseverb)))";
    public static final String PATTERN2 =
            "ROOT < (S <, (PP $+ (/,/ $+ (NP|SBAR=mainclausesub . (VP=mainclauseverbphrase  <<, VB|VBZ|VBD|VBG|VBN|VBP|VBZ=mainclauseverb)))))";

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
        TregexMatcherWrapper matcher = new TregexMatcherWrapper(PATTERN1, sentenceTreeCopied);
        boolean found = matcher.matcher.find();
        if(!found) {
            matcher = new TregexMatcherWrapper(PATTERN2, sentenceTreeCopied);
            found = matcher.matcher.find();
        }
        if (found) {
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
