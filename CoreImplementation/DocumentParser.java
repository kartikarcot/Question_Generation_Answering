package CoreImplementation;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

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
        //props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");

         props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref");

        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        //MG: Changing from neural to statistical to make the model run faster
        props.setProperty("coref.algorithm", "statistical");

        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        System.out.println("Built Stanford CoreNLP Pipeline.");

        /* Previous code
        // create a document object
        document = new CoreDocument(documentStr);
        // annotate the document
        System.out.println("Annotating the document");
        pipeline.annotate(document);
        */

        document = new CoreDocument(documentStr);
        System.out.println("Annotating the document");
        pipeline.annotate(document);

        Annotation doc_annotations = document.annotation();


        // ToDo: Coreference Resolution
        System.out.println("---");
        System.out.println("coref chains");
        for (CorefChain cc : doc_annotations.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            System.out.println("\t" + cc);
        }
        for (CoreMap sentence : doc_annotations.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println("---");
            System.out.println("mentions");
            for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                System.out.println("\t" + m);
            }
        }

        // store parsed sentences
        parsedSentences = new ArrayList<>();
        for (CoreSentence sentence : document.sentences()) {
            parsedSentences.add(new ParsedSentence(sentence));
        }
    }
}
