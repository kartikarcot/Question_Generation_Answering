package CoreImplementation;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.StringUtils;

import java.util.List;

public class PostProcessQuestion {
	// Assumption: All sentences end with "." or nothing
	public Tree postProcessQuestion(Tree sentenceTree) {
		Tree sentenceTreeCopy = sentenceTree.deepCopy();
		// Upcase the first token
		Tree firstTokenNode = sentenceTreeCopy.getLeaves().get(0);
		String firstToken = firstTokenNode.toString();
		firstToken = StringUtils.capitalize(firstToken);
		firstTokenNode.setValue(firstToken);
		// Change punctuation
		int sentenceSize = sentenceTreeCopy.getLeaves().size();
		// Check if last leaf is punctuation (.)
		Tree lastTokenNode = sentenceTreeCopy.getLeaves().get(sentenceSize-1);
		String lastToken = lastTokenNode.toString();
		if (lastToken.equals("."))
		{
			lastTokenNode.setValue("?");
		}
		else{
			// need to insert punctuation
			TregexPattern searchPattern = TregexPattern.compile("SQ=mainclause !< /\\./");
			TsurgeonPattern p = Tsurgeon.parseOperation("insert (. ?) >-1 mainclause");
			List<Tree> changedTree = Tsurgeon.processPatternOnTrees(searchPattern, p, sentenceTreeCopy);
			sentenceTreeCopy = changedTree.get(0);
		}
		// Remove extraneous modifiers
		System.out.println("Post Processed String: " + sentenceTreeCopy.toString());
		return sentenceTreeCopy;
	}
}
