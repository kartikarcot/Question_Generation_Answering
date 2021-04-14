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
		DataLoader dataLoader = new DataLoader("a1.txt");
		String originalString = dataLoader.getText();
		//String originalString = "The term \"domestic dog\" is generally used for both domesticated and feral varieties.";
		//String originalString = "An adult female is a bitch.";
		//String originalString = "An adult male capable of reproduction is a stud.";
		//String originalString = "In 1758, the taxonomist Linnaeus published in his Systema Naturae the classification of species.";
		//String originalString = "The domestic dog (Canis lupus familiaris or Canis familiaris) is a member of the genus Canis (canines), which forms part of the wolf-like canids, and is the most widely abundant terrestrial carnivore. The dog and the extant gray wolf are sister taxa as modern wolves are not closely related to the wolves that were first domesticated, which implies that the direct ancestor of the dog is extinct. The dog was the first species to be domesticated and has been selectively bred over millennia for various behaviors, sensory capabilities, and physical attributes.";
		//String originalString = "A constellation is a group of stars that are considered to form imaginary outlines or meaningful patterns on the celestial sphere, typically representing animals, mythological people or gods, mythological creatures, or manufactured devices. The 88 modern constellations are formally defined regions of the sky together covering the entire celestial sphere.";
//		String originalString = "A constellation is a group of stars that are considered to form imaginary outlines or meaningful patterns on the celestial sphere, typically representing animals, mythological people or gods, mythological creatures, or manufactured devices. The 88 modern constellations are formally defined regions of the sky together covering the entire celestial sphere.\n" +
//				"Origins for the earliest constellations likely goes back to prehistory, whose now unknown creators collectively used them to related important stories of either their beliefs, experiences, creation or mythology. As such, different cultures and countries often adopted their own set of constellations outlines, some that persisted into the early 20th Century. Adoption of numerous constellations have significantly changed throughout the centuries. Many have varied in size or shape, while some became popular then dropped into obscurity. Others were traditionally used only by various cultures or single nations.\n" +
//				"The Western-traditional constellations are the forty-eight Greek classical patterns, as stated in both Aratus' work Phenomena or Ptolemy's Almagest — though their existence probably predates these constellation names by several centuries. Newer constellations in the far southern sky were added much later during the 15th to mid-18th century, when European explorers began travelling to the southern hemisphere. Twelve important constellations are assigned to the zodiac, where the Sun, Moon, and planets all follow the ecliptic. The origins of the zodiac probably date back into prehistory, whose astrological divisions became prominent around 400BCE within Babylonian or Chaldean astronomy.\n" +
//				"In 1928, the International Astronomical Union (IAU) ratified and recognized 88 modern constellations, with contiguous boundaries defined by right ascension and declination. Therefore, any given point in a celestial coordinate system lies in one of the modern constellations. Some astronomical naming systems give the constellation where a given celestial object is found along with a designation in order to convey an approximate idea of its location in the sky. e.g. The Flamsteed designation for bright stars consists of a number and the genitive form of the constellation name.\n" +
//				"Another type of smaller popular patterns or groupings of stars are called asterisms, and differ from the modern or former constellations by being areas with identifiable shapes or features that can be used by novice observers learning to navigate the night sky. Such asterisms often refer to several stars within a constellation or may share boundaries with several constellations. Examples of asterisms include: The Pleiades and The Hyades within the constellation of Taurus, the False Cross crossing the southern constellations of both Carina and Vela, or Venus' Mirror in the constellation of Orion.";
//		String originalString = "Twelve important constellations are assigned to the zodiac, where the Sun, Moon, and planets all follow the ecliptic.";
		//String originalString = "As such, different cultures and countries often adopted their own set of constellations outlines, some that persisted into the early 20th Century.";
		//String originalString = "As the protagonist of the Pokémon anime, Ash has appeared in all episodes of the anime, all the films and several of the television specials.\n" +
		//		"Due to the huge popularity, success, and longevity of the Pokémon anime series around the world since its debut, Ash has gone on to become one of the most well-known and recognizable animated characters of all-time (due to his status as the protagonist of the Pokémon anime), though is often overshadowed in representation by the almost universally identifiable franchise mascot, Pikachu.";
		//String originalString = "The classical Zodiac is a product of a revision of the Old Babylonian system in later Neo-Babylonian astronomy 6th century BC.";
		//String originalString = "Greek astronomy essentially adopted the older Babylonian system in the Hellenistic era, first introduced to Greece by Eudoxus of Cnidus in the 4th century BC.";
		//String originalString = "It was only in 1930 that Eugene Delporte, the Belgian astronomer created an authoritative map demarcating the areas of sky under different constellations.";
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
