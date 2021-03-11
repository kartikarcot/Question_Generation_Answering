package CoreImplementation;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// class for converting a document string into a parsed document
public class DocumentParser {

    CoreDocument document;
    List<ParsedSentence> parsedSentences;

    public DocumentParser(String documentStr) {
        initialize(documentStr);
    }

    public void initialize(String documentStr) {
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");

        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        document = new CoreDocument(documentStr);
        // annotate the document
        pipeline.annotate(document);

        // ToDo: Coreference Resolution
        // store parsed sentences
        parsedSentences = new ArrayList<>();
        for (CoreSentence sentence : document.sentences()) {
            parsedSentences.add(new ParsedSentence(sentence));
        }
    }
}
