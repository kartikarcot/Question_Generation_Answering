package CoreImplementation;


import edu.stanford.nlp.ling.Label;
import com.sun.tools.javac.Main;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class ImplementationTest {
	public static void main(String[] args) throws Exception {

		boolean debug = true;
		// test document string

		// String originalString = "Alvin is a student at CMU University. He is a Master's Student! Alvin wanted to play";
		//String originalString = "Alvin wanted to play. Alvin is walking his dog. Students need a break. Karthik is sad.";
		//load the wiki file
		DataLoader dataLoader = new DataLoader("noun_counting_data/a1.txt");
		String originalString = dataLoader.getText();
		System.out.println(originalString);

		// parse sentence
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
		PostProcessQuestion postprocesser = new PostProcessQuestion();
		List<GeneratedQuestion> questions = new ArrayList<>();

		//MainClauseSubjectVerbConversion thirdPersonForm = new MainClauseSubjectVerbConversion();
		/*System.out.println("------------------- Third Person Form Test -------------------");
		System.out.println("is VBZ: "+thirdPersonForm.convertToAppropriateForm("is", "VBZ"));
		System.out.println("walking VBG: "+thirdPersonForm.convertToAppropriateForm("walking", "VBG"));
		System.out.println("wanted VBD: "+thirdPersonForm.convertToAppropriateForm("wanted", "VBD"));*/

		// output sentences for a check
		int sentenceCounter = 0;
		for (ParsedSentence sentence : filteredSentences) {
			sentenceCounter++;
			if (sentence.sentenceText.contains("\n")) continue;
			if (sentence.sentenceTokens.size() <= 1 ) continue;
			// print sentence
			sentence.print(debug);

			String firstWord = sentence.sentenceTokens.get(0).toLowerCase();
			System.out.println("First Word: "+ firstWord);
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

			System.out.println("After ADVP moved");
			System.out.println(adverbProcessedPhrase);

			// mark unremovable
			MarkUnmovable markUnmovable = new MarkUnmovable(adverbProcessedPhrase);

			// main clasue matcher
			System.out.println("------------------- Main Clause Matcher -----------------");
			MainClauseMatcher mainMatcher = new MainClauseMatcher(markUnmovable.resultingTree);

			System.out.println("Subject: "+mainMatcher.resultingSubject);
			//System.out.println("Verb: "+mainMatcher.resultingVerb+", "+mainMatcher.resultingVerbTag);
			//System.out.println("Before: "+mainMatcher.resultingTree);
			if (mainMatcher.resultingSubject != null) {
				Tree labeledTree = MarkUnmovable.removeUnmovable(mainMatcher.resultingTree);
				//MainSubjectVerbTense verbTense = new MainSubjectVerbTense(labeledTree, mainMatcher.resultingVerb, mainMatcher.resultingVerbTag);
				//System.out.print("After: ");
				//System.out.println(verbTense.resultingTree);

				// remove leading phrases
				RemoveLeadingPhrases removeLeadingPhrases = new RemoveLeadingPhrases(labeledTree);
				labeledTree = removeLeadingPhrases.resultingTree;

				// generate question
				List<Tree> questionTrees = generator.generateQuestions(labeledTree,
						0, sentence.sentenceTags, sentence.sentenceTokens, true);

				for (Tree questionTree : questionTrees) {
					PrepositionalPhraseComma prepositionalPhraseComma = new PrepositionalPhraseComma(questionTree);

					String question = "";
					List<Label> questionYield = prepositionalPhraseComma.resultingTree.yield();
					for (Label leave : questionYield) {
						question+=leave.value()+ " ";
					}
					String answerPhrase = "";
					List<Label> answerPhraseYield = mainMatcher.resultingSubject.yield();
					for (Label leave : answerPhraseYield) {
						answerPhrase += leave.value() + " ";
					}
					GeneratedQuestion newQ = new GeneratedQuestion(question, sentence.sentenceText, answerPhrase);
					questions.add(newQ);
				}
			}


			// find the main clause: Example Tregex Usage
			NounPhraseMatcher nounPhrase = new NounPhraseMatcher(mainMatcher.resultingTree);
			System.out.println("----------------- Noun Phrase Matcher -------------------");
			//System.out.println(nounPhrase.treeWithNounPhrasesMarked);

			nounPhrase.treeWithNounPhrasesMarked = MarkUnmovable.removeUnmovable(nounPhrase.treeWithNounPhrasesMarked);

			// remove leading phrases
			RemoveLeadingPhrases removeLeadingPhrases = new RemoveLeadingPhrases(nounPhrase.treeWithNounPhrasesMarked);
			Tree labeledTree = removeLeadingPhrases.resultingTree;

			// generate a question for each marked nounphrase
			Integer index = 0;
			for (Tree np : nounPhrase.resultingNodes) {
				//System.out.println("Noun Phrase: " + np);
				//NodePruner nodePruner = new NodePruner(nounPhrase.treeWithNounPhrasesMarked, np.label().toString());
				//System.out.println("Sentence with Noun Phrase Removed: " + nodePruner.resultingTree);

				Tree decomposedPredicateTree = null;
				// Decompose predicates
				decomposedPredicateTree = decomposer.decomposePredicate(labeledTree);

				// Perform tsurgeon manipulations is Bob a student at CMU (subject auxillary inversion)
				Tree subAuxInverted = invertor.invertSubjectAuxillary(decomposedPredicateTree);

				//Relabel main clause
				RelabelMainClause relabelObj = new RelabelMainClause(subAuxInverted);
				Tree mainClauseRelabeledTree = (relabelObj.sentenceTreeCopy);
				//System.out.println("Text with relabeled main clause " + mainClauseRelabeledTree.toString());

				// generate question
				List<Tree> questionTrees = generator.generateQuestions(mainClauseRelabeledTree,
												index, sentence.sentenceTags, sentence.sentenceTokens, false);

				for (Tree questionTree : questionTrees) {

					// add commas around PP
					PrepositionalPhraseComma prepositionalPhraseComma = new PrepositionalPhraseComma(questionTree);
					String question = "";
					List<Label> questionYield = prepositionalPhraseComma.resultingTree.yield();
					for (Label leave : questionYield) {
						question+=leave.value()+ " ";
					}
					String answerPhrase = "";
					List<Label> answerPhraseYield = np.yield();
					for (Label leave : answerPhraseYield) {
						answerPhrase += leave.value() + " ";
					}
					GeneratedQuestion newQ = new GeneratedQuestion(question, sentence.sentenceText, answerPhrase);
					questions.add(newQ);
				}
				// Identify NER type of Noun Phrase and Choose Question type accordingly and insert it in the beginning
				// Post process the final question
				//postprocesser.postProcessQuestion(mainClauseRelabeledTree);
				index++;
			}
			System.out.println("--------------------------------------------------------");



			// Identify NER type of Noun Phrase
			// Choose Question type accordingly
			// Construct final question
			// Write to text file
		}

		System.out.println("************* Final Questions ***************");
		for (GeneratedQuestion q : questions) {
			System.out.println(q);
		}
		System.out.println("Total Number of Sentences: "+sentenceCounter);
		System.out.println("Total Number of Questions: "+questions.size());
	}
}
