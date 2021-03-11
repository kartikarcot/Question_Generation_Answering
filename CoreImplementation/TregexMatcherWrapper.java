package CoreImplementation;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class TregexMatcherWrapper {
    public Tree matchingTree;
    public TregexMatcher matcher;
    public TregexMatcherWrapper (String pattern, Tree matchingTree) {
        this.matchingTree = matchingTree;
        TregexPattern tregexPattern = TregexPattern.compile(pattern);
        matcher = tregexPattern.matcher(this.matchingTree);
    }
}
