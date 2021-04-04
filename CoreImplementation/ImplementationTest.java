package CoreImplementation;

import com.sun.tools.javac.Main;
import edu.stanford.nlp.trees.Tree;

import java.util.List;

public class ImplementationTest {
	public static void main(String[] args) throws Exception {

		boolean debug = true;
		// test document string
		// String originalString = "Alvin is a student at CMU University. He is a Master's Student! Alvin wanted to play";
		String originalString = "Alvin wanted to play. Alvin is walking his dog. Students need a break.";

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

		MainClauseSubjectVerbConversion thirdPersonForm = new MainClauseSubjectVerbConversion();
		/*System.out.println("------------------- Third Person Form Test -------------------");
		System.out.println("is VBZ: "+thirdPersonForm.convertToAppropriateForm("is", "VBZ"));
		System.out.println("walking VBG: "+thirdPersonForm.convertToAppropriateForm("walking", "VBG"));
		System.out.println("wanted VBD: "+thirdPersonForm.convertToAppropriateForm("wanted", "VBD"));*/

		// output sentences for a check
		for (ParsedSentence sentence : filteredSentences) {
			// print sentence
			sentence.print(debug);

			// main clasue matcher
			System.out.println("------------------- Main Clause Matcher -----------------");
			MainClauseMatcher mainMatcher = new MainClauseMatcher(sentence.sentenceTree);
			System.out.println("Subject: "+mainMatcher.resultingSubject);
			System.out.println("Verb: "+mainMatcher.resultingVerb+", "+mainMatcher.resultingVerbTag);
			System.out.println("Before: "+mainMatcher.resultingTree);
			if (mainMatcher.resultingSubject != null) {
				MainSubjectVerbTense verbTense = new MainSubjectVerbTense(mainMatcher.resultingTree, mainMatcher.resultingVerb, mainMatcher.resultingVerbTag);
				System.out.print("After: ");
				System.out.println(verbTense.resultingTree);
			}


			// find the main clause: Example Tregex Usage
			NounPhraseMatcher nounPhrase = new NounPhraseMatcher(mainMatcher.resultingTree);
			System.out.println("----------------- Noun Phrase Matcher -------------------");
			System.out.println(nounPhrase.resultingTree);
			for (Tree np : nounPhrase.resultingNodes) {
				System.out.println("Noun Phrase: " + np);
				NodePruner nodePruner = new NodePruner(nounPhrase.resultingTree, np.label().toString());
				System.out.println("Sentence with Noun Phrase Removed: " + nodePruner.resultingTree);
			}
			System.out.println("--------------------------------------------------------");
			// Decompose predicates
			Tree decomposedTree = decomposer.decomposePredicate(sentence.sentenceTree);
			// Perform tsurgeon manipulations is Bob a student at CMU (subject auxillary inversion)
			Tree subAuxInverted = invertor.invertSubjectAuxillary(decomposedTree);

			//Relabel main clause
			RelabelMainClause relabelObj = new RelabelMainClause(subAuxInverted);
			Tree relabeledTree = (relabelObj.sentenceTreeCopy);
			System.out.println("Text with relabeled main clause " + relabeledTree.toString());

			// Identify NER type of Noun Phrase
			// Choose Question type accordingly
			// Construct final question
			// Write to text file
		}
	}
}
