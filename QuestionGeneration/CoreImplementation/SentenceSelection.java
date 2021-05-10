package CoreImplementation;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SentenceSelection {
	/*
	This can get as interesting as we want. There are several algorithms like lexrank and textrank
	that rank sentences in paragraphs for saliency. We can also filter out sentence types which we do not
	handle with our tregex rules. For now I will implement elimination of questions.
	 */
	private static List<Function<ParsedSentence, Boolean>> filters;
	public SentenceSelection() {
		filters = new ArrayList<>();
		// Add any new filters here
		// If it is simple tregex filters add them in tregexFilters() function
		filters.add(SentenceSelection::tregexFilters);
	}

	/*
	Add tregex phrases to filter out here.
	Should return true if sentence needs to be filtered out
	 */
	private static Boolean tregexFilters(ParsedSentence sentence) {
		String[] tregexTemplates = new String[] {
						"ROOT=root << SBARQ",
						"ROOT<<,(DT)",
						"ROOT < (S=mainclause < CC)"
		};
		for (String template : tregexTemplates) {
			TregexPattern questionPattern = TregexPattern.compile(template);
			// make a matcher
			TregexMatcher matcher = questionPattern.matcher(sentence.sentenceTree);
			if (matcher.matches())
				return true;
		}
		return false;
	}

	public List<ParsedSentence> filter(List<ParsedSentence> parsedSentences) {
		List<ParsedSentence> filteredSentences = new ArrayList<>();
		for (ParsedSentence sentence : parsedSentences) {
			Boolean filtered = false;
			for (Function<ParsedSentence, Boolean> filter : this.filters) {
				if (filter.apply(sentence)) {
					filtered = true;
					break;
				}
			}
			if (filtered)
				continue;
			// if we are here the sentence is viable for question generation
			downcaseTheFirstToken(sentence);
			filteredSentences.add(sentence);
		}
		return filteredSentences;
	}

	private void downcaseTheFirstToken(ParsedSentence sentence) {
		String firstTag = sentence.sentenceTags.get(0);
		String firstToken = sentence.sentenceTokens.get(0);
		String firstWord = firstToken.split("-")[0];
		if (firstTag.equals("O") && !firstWord.equals(firstWord.toLowerCase()))
		{
			// fix ner tags array
			sentence.sentenceTokens.set(0, firstToken.toLowerCase());
			// fix the tree as well
			try {
				TregexPattern searchPattern = TregexPattern.compile(firstWord + "=renamedtag");
				TsurgeonPattern p = Tsurgeon.parseOperation("relabel renamedtag " + firstWord.toLowerCase());
				List<Tree> changedTree = Tsurgeon.processPatternOnTrees(searchPattern, p, sentence.sentenceTree);
				sentence.sentenceTree = changedTree.get(0);
			} catch (Exception e) {}
			//// System.out.println("Changed tree: " + sentence.sentenceTree);
		}
	}
}
