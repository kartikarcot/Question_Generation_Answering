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
		String originalString = "Alvin wanted to play. He is walking his dog. Students need a break. Karthik is sad. He loves NLP.";

		//load the wiki file
		//DataLoader dataLoader = new DataLoader("D:\\Users\\Mansi Goyal\\IdeaProjects\\Question_Generation_Answering\\CoreImplementation\\Development_data\\set1\\set1\\a1.txt");
		//String originalString = dataLoader.getText();

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
		for (ParsedSentence sentence : filteredSentences) {
			// print sentence
			//sentence.print(debug);

			// main clasue matcher
			System.out.println("------------------- Main Clause Matcher -----------------");
			MainClauseMatcher mainMatcher = new MainClauseMatcher(sentence.sentenceTree);
			//System.out.println("Subject: "+mainMatcher.resultingSubject);
			//System.out.println("Verb: "+mainMatcher.resultingVerb+", "+mainMatcher.resultingVerbTag);
			//System.out.println("Before: "+mainMatcher.resultingTree);
			if (mainMatcher.resultingSubject != null) {
				MainSubjectVerbTense verbTense = new MainSubjectVerbTense(mainMatcher.resultingTree, mainMatcher.resultingVerb, mainMatcher.resultingVerbTag);
				//System.out.print("After: ");
				//System.out.println(verbTense.resultingTree);

				// generate question
				List<Tree> questionTrees = generator.generateQuestions(verbTense.resultingTree,
						0, sentence.sentenceTags, sentence.sentenceTokens, true);

				for (Tree questionTree : questionTrees) {
					String question = "";
					List<Label> questionYield = questionTree.yield();
					for (Label leave : questionYield) {
						question+=leave.value()+ " ";
					}
					GeneratedQuestion newQ = new GeneratedQuestion(question, sentence.sentenceText);
					questions.add(newQ);
				}
			}


			// find the main clause: Example Tregex Usage
			NounPhraseMatcher nounPhrase = new NounPhraseMatcher(mainMatcher.resultingTree);
			System.out.println("----------------- Noun Phrase Matcher -------------------");
			System.out.println(nounPhrase.treeWithNounPhrasesMarked);

			// generate a question for each marked nounphrase
			Integer index = 0;
			for (Tree np : nounPhrase.resultingNodes) {
				System.out.println("Noun Phrase: " + np);
				//NodePruner nodePruner = new NodePruner(nounPhrase.treeWithNounPhrasesMarked, np.label().toString());
				//System.out.println("Sentence with Noun Phrase Removed: " + nodePruner.resultingTree);

				Boolean isSubject = false;
				Tree decomposedPredicateTree = null;
				// check if current NP is subject
				if (isSubject) {
					// implement this
				}
				// else then decompose predicate
				else {
					// Decompose predicates
					decomposedPredicateTree = decomposer.decomposePredicate(nounPhrase.treeWithNounPhrasesMarked);

				}
				// Perform tsurgeon manipulations is Bob a student at CMU (subject auxillary inversion)
				Tree subAuxInverted = invertor.invertSubjectAuxillary(decomposedPredicateTree);

				//Relabel main clause
				RelabelMainClause relabelObj = new RelabelMainClause(subAuxInverted);
				Tree mainClauseRelabeledTree = (relabelObj.sentenceTreeCopy);
				System.out.println("Text with relabeled main clause " + mainClauseRelabeledTree.toString());

				// generate question
				List<Tree> questionTrees = generator.generateQuestions(mainClauseRelabeledTree,
												index, sentence.sentenceTags, sentence.sentenceTokens, false);

				for (Tree questionTree : questionTrees) {
					String question = "";
					List<Label> questionYield = questionTree.yield();
					for (Label leave : questionYield) {
						question+=leave.value()+ " ";
					}
					GeneratedQuestion newQ = new GeneratedQuestion(question, sentence.sentenceText);
					questions.add(newQ);
				}
				// Identify NER type of Noun Phrase and Choose Question type accordingly and insert it in the beginning
				// Post process the final question
				postprocesser.postProcessQuestion(mainClauseRelabeledTree);
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
	}
}
