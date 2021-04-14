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

		// remove As such
		//String

		// remove , after PP
		// Eg: In 2000, Kartik walked on earth
		String ppCommaTregex = "PP"+nounPhraseIdx+"$+ /,/=tobepruned";
		String operation = "prune tobepruned";
		List<String> operations = new ArrayList<>();
		operations.add(operation);

		// tsurgeon operations
		// Note: tsurgeon wrapper operates in place
		Tree sentenceTreeCopied = sentenceTree.deepCopy();
		TsurgeonWrapper tsurgeon = new TsurgeonWrapper(sentenceTreeCopied, ppCommaTregex, operations);

		// pull up the preposition from PP
		// Eg Kartik is a group of cells.
		// What is Kartik a group of
		String ppPreposition = "@/NP/ < (PP"+nounPhraseIdx+"=prepositionalPhrase < IN=preposition)";
		String mvOperation = "move preposition $+ prepositionalPhrase";
		operations = new ArrayList<>();
		operations.add(mvOperation);
		tsurgeon = new TsurgeonWrapper(tsurgeon.resultingTree, ppPreposition, operations);

		Tree processedSentenceTree = tsurgeon.resultingTree;
		// regex for selecting the required nounphrase
		String answerPhraseExtractTregex = "ROOT=root < (SQ=qclause << /^(NP|PP|SBAR)"+nounPhraseIdx+"$/=answer < VP=predicate)";
		if (mainClauseSubject) {
			answerPhraseExtractTregex = "ROOT=root < (S=qclause << mainclausesub=answer)";
		}
		TregexPattern answerPhraseExtractPattern = TregexPattern.compile(answerPhraseExtractTregex);
		TregexMatcher answerPhraseExtractMatcher = answerPhraseExtractPattern.matcher(processedSentenceTree);
		answerPhraseExtractMatcher.find();

		Tree phraseToMove = answerPhraseExtractMatcher.getNode("answer");
		/*System.out.println("----- Phrase to Move ------");
		System.out.println(phraseToMove);
		System.out.println("---------------------------");*/

		if (phraseToMove == null) return questionTrees;

		// if phraseToMove is not an NP, take it as the first NP child
		String npLabel = phraseToMove.label().toString();
		if (!npLabel.contains("NP")) {
			System.out.println("Label: "+phraseToMove.label());
			String findNPPattern = npLabel+" << NP=toreplace";
			TregexMatcherWrapper matcher = new TregexMatcherWrapper(findNPPattern, sentenceTreeCopied);
			if (matcher.matcher.find()) {
				phraseToMove = matcher.matcher.getNode("toreplace");
			} else {
				return questionTrees;
			}
		} else {
			String findCommaPattern = npLabel + " << (NP=tobereplace . /,/)";
			TregexMatcherWrapper commaMatcher = new TregexMatcherWrapper(findCommaPattern, sentenceTreeCopied);
			if (commaMatcher.matcher.find()) {
				phraseToMove = commaMatcher.matcher.getNode("tobereplace");
			}
		}


		Tree preposition = null;
		if (!mainClauseSubject && phraseToMove.value().matches("PP.*")) {
			Label label = phraseToMove.yield().get(0);
			System.out.println("Label value: " + label.value());
			preposition = phraseToMove.getChild(0);
			System.out.println("Preposition: "+preposition);
		}

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
		/*System.out.println("-------- NER Tag ----------");
		for (String noun : answerTokens) {
			System.out.print(noun + " ");
		}
		System.out.println();
		for (String nerT : tags) {
			System.out.print(nerT + " ");
		}
		System.out.println();
		System.out.println("Final Tag: "+ finalTag);
		System.out.println("---------------------------");*/


		if (finalTag == null) return questionTrees;
		String questionType = determineQuestionType(finalTag);
		// Question type will return null if the nertag was "0". Don't form questions about this.
		if (questionType != null) {
			//System.out.println("Before Question Gen: "+processedSentenceTree);

			// remove useless Subordinate clause
			RemoveUselessPredicate removeUselessPredicate = new RemoveUselessPredicate(processedSentenceTree);

			// remove the noun phrase comma

			// remove the noun phrase
			Tree newTree = removeUselessPredicate.resultingTree.deepCopy();
			String pruneOperation = "prune answer";
			//String pruneOperation2 = "prune adjacentcomma";
			String addQuestionType = "insert " + questionType + " >0 qclause";
			operations = new ArrayList<>();
			//operations.add(pruneOperation2);
			operations.add(pruneOperation);
			operations.add(addQuestionType);
			tsurgeon = new TsurgeonWrapper(newTree, answerPhraseExtractTregex, operations);
			//System.out.println("After Question Gen: "+newTree);
			// if preposition exists then add it
			if (preposition != null) {
				Tree sqTree = newTree.getChild(0);
				sqTree.addChild(sqTree.children().length-1, preposition);
			}
			questionTrees.add(newTree);
		}
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
		if (nerTag.equals("O")) {
			return null;
		}
		else if (nerTag.equals("PERSON")) {
			return "(Ques Who)";
		} else if (nerTag.equals("ORGANIZATION")) {
			return "(Ques Where)";
		} else if (nerTag.equals("DATE")) {
			return "(Ques When)";
		} else if (nerTag.equals("DURATION")) {
			return "(Ques (SubQ How) (SubQ long))";
		} else {
			return "(Ques What)";
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
