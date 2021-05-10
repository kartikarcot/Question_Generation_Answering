package CoreImplementation;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class SentenceSimplifier {
	public static Tree simplify(Tree sentence) {
		Tree sentenceCopy = sentence.deepCopy();
		// Reference: Heilman's work: http://www.cs.cmu.edu/~ark/mheilman/questions/
		String tregexOpStr = "ROOT=root < (S=mainclause < (/[,:]/=comma $ (/SBAR|ADVP|ADJP|CC|PP|S|NP/=fronted !< (IN < if|unless) $++ NP=subject)))";
		List<String> leadingAdverbOperation = new ArrayList<>();
		leadingAdverbOperation.add("prune comma");
		leadingAdverbOperation.add("prune fronted");
		TsurgeonWrapper removeLeadingAdverbs = new TsurgeonWrapper(sentenceCopy, tregexOpStr, leadingAdverbOperation);

		return removeLeadingAdverbs.resultingTree;
	}
}
