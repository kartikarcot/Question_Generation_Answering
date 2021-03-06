package CoreImplementation;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import java.util.ArrayList;
import java.util.List;

public class GenerateQuestion {
	/*
	Args:
	sentenceTree: (Tree) This will be a tree with subject and auxillary inverted and S relabeled as SQ
	nounPhraseIdx: (Integer) This will be the index of the noun phrase to change
	 */
	public List<Tree> generateQuestions(Tree sentenceTree, Integer nounPhraseIdx, List<String> nerTags, List<String> sentenceTokens, boolean mainClauseSubject) {
		// container for all possible questions generated
		List<Tree> questionTrees = new ArrayList<>();

		// regex for selecting the required nounphrase
		String answerPhraseExtractTregex = "ROOT=root < (SQ=qclause << /^(NP|PP|SBAR)"+Integer.toString(nounPhraseIdx)+"$/=answer < VP=predicate)";
		if (mainClauseSubject) {
			answerPhraseExtractTregex = "ROOT=root < (S=qclause << mainclausesub=answer < VP=predicate)";
		}
		TregexPattern answerPhraseExtractPattern = TregexPattern.compile(answerPhraseExtractTregex);
		TregexMatcher answerPhraseExtractMatcher = answerPhraseExtractPattern.matcher(sentenceTree);
		answerPhraseExtractMatcher.find();

		Tree phraseToMove = answerPhraseExtractMatcher.getNode("answer");
		System.out.println("----- Phrase to Move ------");
		System.out.println(phraseToMove);
		System.out.println("---------------------------");

		// phrase answer
		List<Label> answerTokensLabel = phraseToMove.yield();
		List<String> answerTokens = new ArrayList<>();
		//System.out.println("---- Answer Tokens ----");
		for (Label label: answerTokensLabel) {
			answerTokens.add(label.toString());
		}
		//System.out.println("Tag");
		List<String> tags = new ArrayList<>();
		int answerTokenCounter = 0;
		for (int i = 0; i < sentenceTokens.size(); i++) {
			String nerTag = nerTags.get(i);
			String token = sentenceTokens.get(i);
			if (answerTokenCounter >= answerTokens.size()) break;
			if (answerTokenCounter == 0) {
				if (token.equals(answerTokens.get(answerTokenCounter))) {
					tags.add(nerTag);
					answerTokenCounter++;
				}
			} else {
				tags.add(nerTag);
				answerTokenCounter++;
			}
		}
		String finalTag = determineNERtag(tags);
		System.out.println("-------- NER Tag ----------");
		for (String noun : answerTokens) {
			System.out.print(noun + " ");
		}
		System.out.println();
		for (String nerT : tags) {
			System.out.print(nerT + " ");
		}
		System.out.println();
		System.out.println("Final Tag: "+ finalTag);
		System.out.println("---------------------------");


		if (finalTag == null) return questionTrees;
		String questionType = determineQuestionType(finalTag);
		System.out.println("Before Question Gen: "+sentenceTree);

		// remove the noun phrase
		Tree newTree = sentenceTree.deepCopy();
		String pruneOperation = "prune answer";
		String addQuestionType = "insert (QUES "+questionType+") >0 qclause";
		List<String> operations = new ArrayList<>();
		operations.add(pruneOperation);
		operations.add(addQuestionType);
		TsurgeonWrapper tsurgeon = new TsurgeonWrapper(newTree, answerPhraseExtractTregex, operations);
		System.out.println("After Question Gen: "+newTree);
		questionTrees.add(newTree);
		return questionTrees;
		// extract the noun phrase out of the prepositional phrase
		/*String extractionTregex = "PP !>> NP ?< RB|ADVP=adverb [< (IN|TO=preposition !$ IN) | < (IN=preposition $ IN=preposition2)] < NP=object";
		TregexPattern extractionPattern = TregexPattern.compile(extractionTregex);
		TregexMatcher extractionMatcher = extractionPattern.matcher(phraseToMove);
		Tree answerNP = phraseToMove;
		String answerPreposition = "";
		Tree answerPrepositionModifier = null;
		if (extractionMatcher.find()) {
			System.out.println("Noun Phrase Found");
			answerNP = extractionMatcher.getNode("object");
			answerPreposition = extractionMatcher.getNode("preposition").yield().toString();
			Tree answerPreposition2 = extractionMatcher.getNode("preposition2");
			if (answerPreposition2 != null) {
				answerPreposition += " " + answerPreposition2.yield().toString();
			}
			answerPrepositionModifier = extractionMatcher.getNode("adverb");
			System.out.println("---- Answer Preposition ----");
			System.out.println(answerPreposition);
			System.out.println("----------------------------");

			System.out.println("---- Answer Preposition Modifier ----");
			System.out.println(answerPrepositionModifier);
			System.out.println("-------------------------------------");
		} else {*/
			// ToDo: check if this is a partitive construction
			/*String partitiveConstructionTregex = "NP <<# DT|JJ|CD|RB|NN|JJS|JJR=syntactichead < (PP < (IN < of) < (NP <<# NN|NNS|NNP|NNPS=semantichead)) !> NP ";
			TregexPattern partitiveConstructionPattern = TregexPattern.compile(partitiveConstructionTregex);
			TregexMatcher partitiveConstructionMatcher = partitiveConstructionPattern.matcher(phraseToMove);
			if (partitiveConstructionMatcher.find()) {
					Tree syntacticHead = partitiveConstructionMatcher.getNode("syntactichead");
					if ()
			}*/
		//}
		// ToDo: identify question type
		//List<String> questionTypes = identifyQuestionTypes(nerTags, start, end);
		//return questionTrees;
	}

	private String determineNERtag(List<String> nerTags) {
		for (int i = nerTags.size() -1; i >=0; i--) {
			if (!nerTags.get(i).equals("O")) {
				return nerTags.get(i);
			}
		}
		return "O";
	}

	private String determineQuestionType(String nerTag) {
		if (nerTag.equals("PERSON")) {
			return "Who";
		} else if (nerTag.equals("ORGANIZATION")) {
			return "Where";
		} else {
			return "What";
		}
	}

	/*
	startOfNP gives the start idx in ner tags
	endOfNP gives the end idx in ner tags
	 */
	private List<String> identifyQuestionTypes(List<String> nerTags, Integer startOfNP, Integer endOfNP) {
		List<String> questionTypes = new ArrayList<>();
		// dummy for now need to change based on ner tags
		questionTypes.add("(WHNP (WRB who))");
		return questionTypes;
	}
}
