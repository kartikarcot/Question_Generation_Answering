package CoreImplementation;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenerateQuestion {
	/*
	Args:
	sentenceTree: (Tree) This will be a tree with subject and auxillary inverted and S relabeled as SQ
	nounPhraseIdx: (Integer) This will be the index of the noun phrase to change
	 */
	public List<Tree> generateQuestions(Tree sentenceTree, Integer nounPhraseIdx, List<String> nerTags, List<String> sentenceTokens, Map<String, String> tagMap, boolean mainClauseSubject) {
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
		// System.out.println("----- Phrase to Move ------");
		// System.out.println(phraseToMove);
		// System.out.println("---------------------------");

		if (phraseToMove == null) return questionTrees;

		Tree preposition = null;
		if (!mainClauseSubject && phraseToMove.value().matches("PP.*")) {

			Label label = phraseToMove.yield().get(0);
			// System.out.println("Label value: " + label.value());
			preposition = phraseToMove.getChild(0);
			Label childLabel = preposition.label();
			if (!childLabel.toString().equals("IN")) preposition = null;
			// System.out.println("Preposition: "+preposition);
			// System.out.println("Preposition Label: "+childLabel);
		}

		// if phraseToMove is not an NP, take it as the first NP child
		String npLabel = phraseToMove.label().toString();
		if (!npLabel.contains("NP") && !npLabel.contains("mainclausesub")) {
			// System.out.println("Label: " + phraseToMove.label());
			String findNPPattern = npLabel + " << NP=toreplace";
			TregexMatcherWrapper matcher = new TregexMatcherWrapper(findNPPattern, sentenceTreeCopied);
			if (matcher.matcher.find()) {
				phraseToMove = matcher.matcher.getNode("toreplace");
			} else {
				return questionTrees;
			}
			npLabel = phraseToMove.label().toString();
		}
		if (npLabel.contains("NP")) {

			String findNPPPPattern = npLabel + " < (@/NP/=tobereplace $+ @/PP/)";
			TregexMatcherWrapper nPPPMatcher = new TregexMatcherWrapper(findNPPPPattern, phraseToMove);
			if (nPPPMatcher.matcher.find()) {
				phraseToMove = nPPPMatcher.matcher.getNode("tobereplace");
			} else {

				String findCommaPattern = npLabel + " << (NP=tobereplace . /,/)";
				TregexMatcherWrapper commaMatcher = new TregexMatcherWrapper(findCommaPattern, phraseToMove);
				if (commaMatcher.matcher.find()) {
					phraseToMove = commaMatcher.matcher.getNode("tobereplace");
				}
			}
		}

		//npLabel = phraseToMove.label().toString();

		// phrase answer
		List<Label> answerTokensLabel = phraseToMove.yield();
		List<String> answerTokens = new ArrayList<>();
		//// System.out.println("---- Answer Tokens ----");
		for (Label label: answerTokensLabel) {
			answerTokens.add(label.value());
		}
		//// System.out.println("Tag");
		//List<String> tokens = new ArrayList<>();
		/*int answerTokenCounter = 0;
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
		}*/
		String finalTag = determineNERtag(answerTokens, tagMap);
		/*// System.out.println("-------- NER Tag ----------");
		for (String noun : answerTokens) {
			System.out.print(noun + " ");
		}
		// System.out.println();
		for (String nerT : tags) {
			System.out.print(nerT + " ");
		}
		// System.out.println();
		// System.out.println("Final Tag: "+ finalTag);
		// System.out.println("---------------------------");*/


		if (finalTag == null) {
			if (mainClauseSubject) {

			}
			return questionTrees;
		}
		String questionType = determineQuestionType(finalTag);
		// Question type will return null if the nertag was "0". Don't form questions about this.
		if (questionType != null) {
			//// System.out.println("Before Question Gen: "+processedSentenceTree);

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
			//// System.out.println("After Question Gen: "+newTree);
			// if preposition exists then add it
			if (preposition != null) {
				Tree sqTree = newTree.getChild(0);
				sqTree.addChild(sqTree.children().length-1, preposition);
			}
			questionTrees.add(newTree);
		}
		return questionTrees;

	}

	private String determineNERtag(List<String> tokens, Map<String, String> tagMap) {
		// System.out.println("Determine NER Tag");

		// check if pronoun and get ner tag
		for (String token : tokens) {
			if (token.toLowerCase().matches("he|her|him|she|his|them|their|they|I|me|my|mine|you|your"))
				return "PERSON";
			if (token.toLowerCase().matches("it|its"))
				return "THING";
		}

		for (String token : tokens) {
			String tag = tagMap.get(token);
			if (tag == null) continue;
			// System.out.println(token + " : " + tag);
			if (!tag.equals("O") && !tag.equals("NATIONALITY")) {
				if (!tag.matches("DATE|TIME") && tokens.get(0).toLowerCase().matches("in|at"))
					return "LOCATION";
				return tag;
			}
		}
		/*for (int i = nerTags.size() -1; i >=0; i--) {
			if (!nerTags.get(i).equals("O") && !nerTags.get(i).equals("NATIONALITY")) {
				return nerTags.get(i);
			}
		}*/
		return "O";
	}

	private String determineQuestionType(String nerTag) {
		if (nerTag.equals("O")) {
			return null;
		}
		else if (nerTag.equals("ORGANIZATION")  ||nerTag.equals("PERSON") || nerTag.equals("NATIONALITY")) {
			return "(Ques Who)";
		} else if ( nerTag.equals("LOCATION")) {
			return "(Ques Where)";
		} else if (nerTag.equals("DATE")) {
			return "(Ques When)";
		} else if (nerTag.equals("DURATION")) {
			return "(Ques (SubQ How) (SubQ long))";
		} else if (nerTag.equals("NUMBER")) {
			return "(Ques (SubQ How) (SubQ many))";
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