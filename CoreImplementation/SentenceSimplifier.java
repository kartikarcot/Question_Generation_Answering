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
		String tregexOpStr = "ROOT=root < (S=mainclause < (/[,:]/=comma $ (/SBAR|ADVP|ADJP|CC|PP|S|NP/=fronted !< (IN < if|unless) $++ NP=subject)))";
		List<String> leadingAdverbOperation = new ArrayList<>();
		leadingAdverbOperation.add("prune comma");
		leadingAdverbOperation.add("prune fronted");
		TsurgeonWrapper removeLeadingAdverbs = new TsurgeonWrapper(sentenceCopy, tregexOpStr, leadingAdverbOperation);


		// remove leading adverbs
//		String leadingAdverbPattern = "ROOT< (S <1 ADVP=leadadv <2 /,/=leadcomma)";
//		List<String> leadingAdverbOperation = new ArrayList<>();
//		leadingAdverbOperation.add("prune leadadv");
//		leadingAdverbOperation.add("prune leadcomma");
//		TsurgeonWrapper removeLeadingAdverbs = new TsurgeonWrapper(sentenceCopy, leadingAdverbPattern, leadingAdverbOperation);
		System.out.println("Initial tree: "+sentenceCopy.toString());
		System.out.println("Formatted tree: "+removeLeadingAdverbs.resultingTree.toString());
		return removeLeadingAdverbs.resultingTree;
	}
}
