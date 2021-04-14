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

         props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref");

        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        //MG: Options: neural, statistical
        props.setProperty("coref.algorithm", "statistical");

        props.setProperty("ner.combinationMode", "HIGH_RECALL");

        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        System.out.println("Built Stanford CoreNLP Pipeline.");

        originalDocument  = new CoreDocument(documentStr);
        System.out.println("Annotating the document");
        pipeline.annotate(originalDocument);

        Annotation doc_annotations = originalDocument.annotation();


        // ToDo: Coreference Resolution
        Map<Integer, CorefChain> corefs = doc_annotations.get(CorefCoreAnnotations.CorefChainAnnotation.class);

        originalParsedSentences = new ArrayList<ParsedSentence>();

        StringBuilder corefResolvedSentenceDocument = new StringBuilder();

        //Reference: https://stackoverflow.com/questions/30182138/how-to-replace-a-word-by-its-most-representative-mention-using-stanford-corenlp
        for (CoreSentence coreSentence: originalDocument.sentences()) {

            CoreMap sentence = coreSentence.coreMap();

            //Corresponding resolved sentence
            List<String> corefResolvedSentence = new ArrayList<String>();

            //Parse through tokens for each of the sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                Integer ClusterId= token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);

                if(corefs.get(ClusterId)!=null){

                    CorefChain coref_chain = corefs.get(ClusterId);

                    //Fetch sentence index
                    int sentence_idx = coref_chain.getRepresentativeMention().sentNum -1;
                    CoreMap corefSentence = doc_annotations.get(CoreAnnotations.SentencesAnnotation.class).get(sentence_idx);

                    List<CoreLabel> corefSentenceTokens = corefSentence.get(CoreAnnotations.TokensAnnotation.class);
                    CorefChain.CorefMention reprMent = coref_chain.getRepresentativeMention();

                    if (token.index() <= reprMent.startIndex || token.index() >= reprMent.endIndex) {

                        for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
                            CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
                            corefResolvedSentence.add(matchedLabel.word().replace("'s", ""));

                        }
                    }

                    else {
                        //No resolution needed
                        corefResolvedSentence.add(token.word());
                    }


                }else{

                    //No resolution needed
                    corefResolvedSentence.add(token.word());


                }


            }
            ParsedSentence parsedObj = new ParsedSentence(coreSentence, corefResolvedSentence);
            originalParsedSentences.add(parsedObj);
            //System.out.println(parsedObj.corefResolvedSentenceText);
            corefResolvedSentenceDocument.append(" "+parsedObj.corefResolvedSentenceText);
        }

     //Added to annotate the coref resolved document
        System.out.println(corefResolvedSentenceDocument.toString());
        document  = new CoreDocument(corefResolvedSentenceDocument.toString());
        System.out.println("Annotating the coref resolved document");
        pipeline.annotate(document);

        parsedSentences = new ArrayList<>();
        for (CoreSentence sentence : document.sentences()) {
            parsedSentences.add(new ParsedSentence(sentence));
        }




    }
}
