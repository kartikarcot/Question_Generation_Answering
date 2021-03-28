package CoreImplementation;

import edu.stanford.nlp.trees.Tree;

import java.util.List;

public class ImplementationTest {
    public static void main(String[] args) {

        boolean debug = true;
        // test document string
        //String originalString = "Alvin is a student at CMU University. He is a Master's Student!";

        //load the wiki file
        DataLoader dataLoader = new DataLoader("D:\\Users\\Mansi Goyal\\IdeaProjects\\Question_Generation_Answering\\CoreImplementation\\Development_data\\set1\\set1\\a1.txt");
        String originalString = dataLoader.getText();

        // parse sentence
        DocumentParser docParser = new DocumentParser(originalString);

        // filter sentences
        SentenceSelection selector = new SentenceSelection();
        List<ParsedSentence> filteredSentences = selector.filter(docParser.parsedSentences);
        // output sentences for a check
        for (ParsedSentence sentence : filteredSentences) {
            // print sentence
            sentence.print(debug);

            // find the main clause: Example Tregex Usage
            NounPhraseMatcher nounPhrase = new NounPhraseMatcher(sentence.sentenceTree);
            System.out.println("----------------- Noun Phrase Matcher -------------------");
            System.out.println(nounPhrase.resultingTree);
            for (Tree np : nounPhrase.resultingNodes) {
                System.out.println("Noun Phrase: " + np);
                NodePruner nodePruner = new NodePruner(nounPhrase.resultingTree, np.label().toString());
                System.out.println("Sentence with Noun Phrase Removed: " + nodePruner.resultingTree);
            }
            System.out.println("--------------------------------------------------------");


        }
            // Decompose predicates ?
            // Perform tsurgeon manipulations is Bob a student at CMU?
            // Identify NER type of Noun Phrase
            // Choose Question type accordingly
            // Construct final question
            // Write to text file




    }
}
