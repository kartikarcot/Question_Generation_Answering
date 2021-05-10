package CoreImplementation;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class MarkUnmovable {
    // Idea inspired by Heilman's work: http://www.cs.cmu.edu/~ark/mheilman/questions/
    String[] unmovablePhrases = new String[] {
            "ROOT=root << (PP $+ PP=unmovable)",
            "ROOT=root << (PP=unmovable $+ UNMOVABLE-PP)",
            "ROOT=root << (VP < (S=unmovable $,, /,/))",
            //"ROOT=root < (S < PP|ADJP|ADVP|S|SBAR=unmovable)",
            "ROOT=root << (/\\.*/ < CC << NP|ADJP|VP|ADVP|PP=unmovable)",
            "ROOT=root << (SBAR < (IN|DT < /[^that]/) << NP|PP=unmovable)",
            "ROOT=root << (SBAR < /^WH.*P$/ << NP|ADJP|VP|ADVP|PP=unmovable)",
            "ROOT=root << (SBAR <, IN|DT < (S < (NP=unmovable !$,, VP)))",
            "ROOT=root << (S < (VP <+(VP) (VB|VBD|VBN|VBZ < be|being|been|is|are|was|were|am) <+(VP) (S << NP|ADJP|VP|ADVP|PP=unmovable)))",
            "ROOT=root << (NP << (PP=unmovable !< (IN < of|about)))",
            "ROOT=root << (PP << PP=unmovable)",
            "ROOT=root << (NP $ VP << PP=unmovable)",
            "ROOT=root << (SBAR=unmovable [ !> VP | $-- /,/ | < RB ])",
            "ROOT=root << (SBAR=unmovable !< WHNP < (/^[^S].*/ !<< that|whether|how))",
            "ROOT=root << (NP=unmovable < EX)",
            "ROOT=root << (/^S/ < `` << NP|ADJP|VP|ADVP|PP=unmovable)",
            "ROOT=root << (PP=unmovable !< /.*NP/)",
            "ROOT=root << (NP|PP|ADJP|ADVP|PP << (NP|ADJP|VP|ADVP=unmovable))",
            "ROOT=root << (@UNMOVABLE << NP|ADJP|VP|ADVP|PP=unmovable)"
    };
    public Tree resultingTree;

    public MarkUnmovable(Tree inputTree) {initialize(inputTree);}
    public void initialize(Tree inputTree) {
        this.resultingTree = inputTree.deepCopy();

        for (String unmovablePhrase : unmovablePhrases) {
            TregexMatcherWrapper matcher = new TregexMatcherWrapper(unmovablePhrase, this.resultingTree);

            // get the matching nodes
            while (matcher.matcher.find()) {
                Tree foundMatch = matcher.matcher.getNode("unmovable");
                String label = foundMatch.label().toString();
                // prepending a special value to the label
                foundMatch.label().setValue("UNMOVABLE-"+label);
            }

        }
    }

    public static Tree removeUnmovable(Tree inputTree){
        String answerPhraseTreeString = inputTree.toString();
        answerPhraseTreeString = answerPhraseTreeString.replaceAll("UNMOVABLE-", "");
        answerPhraseTreeString = answerPhraseTreeString.replaceAll("-\\d+ ", " ");
        return Tree.valueOf(answerPhraseTreeString);
    }
}
