package CoreImplementation;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Map;

// class for converting a document string into a parsed document
public class DocumentParser {

    CoreDocument document;
    List<ParsedSentence> parsedSentences;

    //Added this if we want to keep a track of the original document pre-coref
    CoreDocument originalDocument;
    List<ParsedSentence> originalParsedSentences;

    public DocumentParser(String documentStr) {
        initialize(documentStr);
    }

    public void initialize(String documentStr) {
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        //props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");

         props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        props.setProperty("ner.combinationMode", "HIGH_RECALL");
        props.setProperty("parse.maxlen", "35");

        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // System.out.println("Built Stanford CoreNLP Pipeline.");

        originalDocument  = new CoreDocument(documentStr);
        // System.out.println("Annotating the document");
        pipeline.annotate(originalDocument);

        //Annotation doc_annotations = originalDocument.annotation();

        parsedSentences = new ArrayList<>();
        for (CoreSentence sentence : originalDocument.sentences()) {
            ParsedSentence p = new ParsedSentence(sentence);
            if (p.sentenceTree.yield().size() > 35)
                continue;
            parsedSentences.add(p);
        }



    }
}
