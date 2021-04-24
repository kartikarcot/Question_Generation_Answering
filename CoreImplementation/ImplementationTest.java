package CoreImplementation;


import edu.stanford.nlp.ling.Label;
import com.sun.tools.javac.Main;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.simple.Token;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import java.util.*;

public class ImplementationTest {
	public static void main(String[] args) throws Exception {

		boolean debug = true;
		// test document string

		// String originalString = "Alvin is a student at CMU University. He is a Master's Student! Alvin wanted to play";
		//String originalString = "Alvin wanted to play. Alvin is walking his dog. Students need a break. Karthik is sad.";
		//load the wiki file
		DataLoader dataLoader = new DataLoader(args[0]);
		String originalString = dataLoader.getText();
		Integer noQuestions = Integer.parseInt(args[1]);

		DocumentParser docParser = new DocumentParser(originalString);

		// filter sentences
		SentenceSelection selector = new SentenceSelection();
		List<ParsedSentence> filteredSentences = selector.filter(docParser.parsedSentences);

		// Object for decomposing predicates
		DecomposePredicate decomposer = new DecomposePredicate();

		// Object for inverting predicates
		SubjectAuxillaryInversion invertor = new SubjectAuxillaryInversion();

		// Generate Question
		GenerateQuestion generator = new GenerateQuestion();
		// Post process the final question
		// PostProcessQuestion postprocesser = new PostProcessQuestion();
		HashSet<GeneratedQuestion> questions = new HashSet<>();

		LexicalizedParser lp = LexicalizedParser.loadModel(
						"edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"
		);

		//MainClauseSubjectVerbConversion thirdPersonForm = new MainClauseSubjectVerbConversion();
		/*// System.out.println("------------------- Third Person Form Test -------------------");
		// System.out.println("is VBZ: "+thirdPersonForm.convertToAppropriateForm("is", "VBZ"));
		// System.out.println("walking VBG: "+thirdPersonForm.convertToAppropriateForm("walking", "VBG"));
		// System.out.println("wanted VBD: "+thirdPersonForm.convertToAppropriateForm("wanted", "VBD"));*/

		// output sentences for a check
		int sentenceCounter = 0;
		for (ParsedSentence sentence : filteredSentences) {
			sentenceCounter++;
			if (sentence.sentenceText.contains("\n")) continue;
			if (sentence.sentenceTokens.size() <= 1) continue;
			// print sentence
//			sentence.print(debug);

			String firstWord = sentence.sentenceTokens.get(0).toLowerCase();
			// System.out.println("First Word: " + firstWord);
			if (firstWord.equals("there-1") || firstWord.equals("these-1") || firstWord.equals("it-1")
							|| (firstWord.equals("another-1") && sentence.sentenceTokens.get(1).toLowerCase().equals("example-2"))) {
				continue;
			}

			// remove leading phrases
			RemoveLeadingPhrases removeLeadingPhrases1 = new RemoveLeadingPhrases(sentence.sentenceTree);
			Tree removedPhrases = removeLeadingPhrases1.resultingTree;

			// put advp into vp
			PutAdverbIntoNP putAdverbIntoVP = new PutAdverbIntoNP(removedPhrases);
			Tree adverbProcessedPhrase = putAdverbIntoVP.resultingTree;

			// System.out.println("After ADVP moved");
			// System.out.println(adverbProcessedPhrase);

			// mark unremovable
			MarkUnmovable markUnmovable = new MarkUnmovable(adverbProcessedPhrase);

			// main clasue matcher
			// System.out.println("------------------- Main Clause Matcher -----------------");
			MainClauseMatcher mainMatcher = new MainClauseMatcher(markUnmovable.resultingTree);

			// System.out.println("Subject: " + mainMatcher.resultingSubject);
			//// System.out.println("Verb: "+mainMatcher.resultingVerb+", "+mainMatcher.resultingVerbTag);
			//// System.out.println("Before: "+mainMatcher.resultingTree);
			TregexPattern pronounPattern = TregexPattern.compile("ROOT <<, PRP");
			TregexMatcher pronounMatcher = pronounPattern.matcher(sentence.sentenceTree);
			Boolean firstWordPronoun = pronounMatcher.find();

			if (mainMatcher.resultingSubject != null) {
				Tree labeledTree = MarkUnmovable.removeUnmovable(mainMatcher.resultingTree);
				List<Tree> questionTrees = new ArrayList<>();
				// System.out.println(mainMatcher.resultingTree.toString());
				// System.out.println("Removed unmovable phrases" + labeledTree.toString());
				//MainSubjectVerbTense verbTense = new MainSubjectVerbTense(labeledTree, mainMatcher.resultingVerb, mainMatcher.resultingVerbTag);
				//System.out.print("After: ");
				//// System.out.println(verbTense.resultingTree);

				// remove leading phrases
				RemoveLeadingPhrases removeLeadingPhrases = new RemoveLeadingPhrases(labeledTree);
				labeledTree = removeLeadingPhrases.resultingTree;

				// generate question
				questionTrees = generator.generateQuestions(labeledTree,
								0, sentence.sentenceTags, sentence.sentenceTokens, sentence.tagMap, true);

				if (!firstWordPronoun) {
					TregexPattern mainSubjectPattern = TregexPattern.compile("mainclausesub << (DT|PRP)");
					TregexMatcher mainSubjectMatcher = mainSubjectPattern.matcher(mainMatcher.resultingSubject);
					Boolean found = mainSubjectMatcher.find();
					if (!found) {
						String questionTreeString = "(ROOT (SBARQ (WHNP (WP What)) (SQ (MD can) (NP (PRP you)) (VP (VB say) (PP (IN about) " + mainMatcher.resultingSubject.toString() + "))) (. .)))";
						Tree customQuestion = Tree.valueOf(questionTreeString);
						List<String> operations = new ArrayList<>();
						operations.add("relabel msb NP");
						TsurgeonWrapper relabelOperation = new TsurgeonWrapper(customQuestion, "ROOT << mainclausesub=msb", operations);
						questionTrees.add(relabelOperation.resultingTree);
					}
				}

				for (Tree questionTree : questionTrees) {
					PrepositionalPhraseComma prepositionalPhraseComma = new PrepositionalPhraseComma(questionTree);

					String question = "";
					List<Label> questionYield = prepositionalPhraseComma.resultingTree.yield();
					for (Label leave : questionYield) {
						question += leave.value() + " ";
					}
					String answerPhrase = "";
					List<Label> answerPhraseYield = mainMatcher.resultingSubject.yield();
					for (Label leave : answerPhraseYield) {
						answerPhrase += leave.value() + " ";
					}
					Tree parse = lp.parse(question);
					if (parse.yield().size() >= 5) {
						List<Label> yield = parse.yield();
						Double score = 0.;
						String question_str = yield.stream().map(e -> e.value()).reduce("",(e1,e2)-> e1+" "+e2).strip();
						if (question_str.matches("What can you say about .*")) {
							Double upper = 25.;
							Double lower = 0.;
							score = parse.score() - (Math.random() * (upper - lower)) + lower;
							// System.out.println("Added noise"+score);
						}
						else {
							score = parse.score();
						}
						GeneratedQuestion newQ = new GeneratedQuestion(question, sentence.sentenceText, answerPhrase, score);
						questions.add(newQ);
					}
				}
			}


			if (!firstWordPronoun) {
				// find the main clause: Example Tregex Usage
				NounPhraseMatcher nounPhrase = new NounPhraseMatcher(mainMatcher.resultingTree);
				// System.out.println("----------------- Noun Phrase Matcher -------------------");
				//// System.out.println(nounPhrase.treeWithNounPhrasesMarked);

				nounPhrase.treeWithNounPhrasesMarked = MarkUnmovable.removeUnmovable(nounPhrase.treeWithNounPhrasesMarked);

				// remove leading phrases
				RemoveLeadingPhrases removeLeadingPhrases = new RemoveLeadingPhrases(nounPhrase.treeWithNounPhrasesMarked);
				Tree labeledTree = removeLeadingPhrases.resultingTree;

				// generate a question for each marked nounphrase
				Integer index = 0;
				for (Tree np : nounPhrase.resultingNodes) {
					//// System.out.println("Noun Phrase: " + np);
					//NodePruner nodePruner = new NodePruner(nounPhrase.treeWithNounPhrasesMarked, np.label().toString());
					//// System.out.println("Sentence with Noun Phrase Removed: " + nodePruner.resultingTree);

					Tree decomposedPredicateTree = null;
					// Decompose predicates
					decomposedPredicateTree = decomposer.decomposePredicate(labeledTree);

					// Perform tsurgeon manipulations is Bob a student at CMU (subject auxillary inversion)
					Tree subAuxInverted = invertor.invertSubjectAuxillary(decomposedPredicateTree);

					//Relabel main clause
					RelabelMainClause relabelObj = new RelabelMainClause(subAuxInverted);
					Tree mainClauseRelabeledTree = (relabelObj.sentenceTreeCopy);
					//// System.out.println("Text with relabeled main clause " + mainClauseRelabeledTree.toString());

					// generate question
					List<Tree> questionTrees = generator.generateQuestions(mainClauseRelabeledTree,
									index, sentence.sentenceTags, sentence.sentenceTokens, sentence.tagMap, false);


					for (Tree questionTree : questionTrees) {

						// add commas around PP
						PrepositionalPhraseComma prepositionalPhraseComma = new PrepositionalPhraseComma(questionTree);
						String question = "";
						List<Label> questionYield = prepositionalPhraseComma.resultingTree.yield();
						for (Label leave : questionYield) {
							question += leave.value() + " ";
						}
						String answerPhrase = "";
						List<Label> answerPhraseYield = np.yield();
						for (Label leave : answerPhraseYield) {
							answerPhrase += leave.value() + " ";
						}
						Tree parse = lp.parse(question);
						if (parse.yield().size() >= 5) {
							GeneratedQuestion newQ = new GeneratedQuestion(question, sentence.sentenceText, answerPhrase, parse.score());
							questions.add(newQ);
						}
					}
					// Identify NER type of Noun Phrase and Choose Question type accordingly and insert it in the beginning
					// Post process the final question
					//postprocesser.postProcessQuestion(mainClauseRelabeledTree);
					index++;
				}
				// System.out.println("--------------------------------------------------------");


				// Identify NER type of Noun Phrase
				// Choose Question type accordingly
				// Construct final question
				// Write to text file
			}
		}
		HashMap<Integer, List<GeneratedQuestion>> hashmapOfQuestions = new HashMap<>();
		hashmapOfQuestions.put(0, new ArrayList<>());
		hashmapOfQuestions.put(1, new ArrayList<>());
		hashmapOfQuestions.put(2, new ArrayList<>());
		hashmapOfQuestions.put(3, new ArrayList<>());
		for (GeneratedQuestion q : questions) {
			Integer bin = q.length / 10;
			// totally 4 bins
			if (bin > 3) bin = 3;
			if (hashmapOfQuestions.containsKey(bin)) {
				hashmapOfQuestions.get(bin).add(q);
			}
			else{
				hashmapOfQuestions.put(bin, new ArrayList<>());
				hashmapOfQuestions.get(bin).add(q);
			}
		}

		// split the questions as 40% 30% 15% 15%
		Integer bin_4 = Math.min(hashmapOfQuestions.get(3).size(), (int) Math.round(noQuestions * 0.1));
		Integer bin_3 = Math.min(hashmapOfQuestions.get(2).size(), (int) Math.round(noQuestions * 0.15));
		Integer bin_2 = Math.min(hashmapOfQuestions.get(1).size(), (int) Math.round(noQuestions * 0.3));
		Integer bin_1 = Math.min(hashmapOfQuestions.get(0).size(), Math.max(0, noQuestions - bin_2 - bin_3 - bin_4));
		List<Integer> noQuestionArray = Arrays.asList(new Integer[]{bin_1, bin_2, bin_3, bin_4});
		for (int i = 0; i < 4; i++) {
			List<GeneratedQuestion> bin_questions = hashmapOfQuestions.get(i);
			Collections.sort(bin_questions);
			for (int j = 0; j < noQuestionArray.get(i); j++) {
				System.out.println(bin_questions.get(j).generatedQuestion);
			}
		}
	}
}
