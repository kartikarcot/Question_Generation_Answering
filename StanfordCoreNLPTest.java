import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StanfordCoreNLPTest {
    public static void main(String[] args) {
//        String originalString = "Alvin is a student at CMU University.";
        String originalString = "Kartik hates reading about grammar.";
        Morphology morphology = new Morphology();
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");

        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = new CoreDocument(originalString);
        // annnotate the document
        pipeline.annotate(document);

        // text of the first sentence
        CoreSentence sentence = document.sentences().get(0);
        String sentenceText = sentence.text();
        System.out.println("Example: sentence");
        System.out.println(sentenceText);

        List<String> sentenceTokens = new ArrayList<>();
        List<String> supersenseTags = new ArrayList<>();


        // constituency parse for the second sentence
        Tree constituencyParse = sentence.constituencyParse();
        System.out.println("Example: constituency parse");
        System.out.println(constituencyParse);
        System.out.println();

        List<Label> originalTokenArray = constituencyParse.yield();
        for (int i = 0; i < originalTokenArray.size(); i++) {
            sentenceTokens.add(originalTokenArray.get(i).toString());
        }
        supersenseTags = sentence.nerTags();
        for (String tag : supersenseTags) {
            System.out.println(tag);
        }

        // Tregex conditions
        String[] unmovablePhrases = new String[] {
                "ROOT=root << (VP < (S=unmovable $,, /,/))",
                "ROOT=root < (S < PP|ADJP|ADVP|S|SBAR=unmovable)",
                "ROOT=root << (/\\.*/ < CC << NP|ADJP|VP|ADVP|PP=unmovable)",
                "ROOT=root << (SBAR < (IN|DT < /[^that]/) << NP|PP=unmovable)",
                "ROOT=root << (SBAR < /^WH.*P$/ << NP|ADJP|VP|ADVP|PP=unmovable)",
                "ROOT=root << (SBAR <, IN|DT < (S < (NP=unmovable !$,, VP)))",
                "ROOT=root << (S < (VP <+(VP) (VB|VBD|VBN|VBZ < be|being|been|is|are|was|were|am) <+(VP) (S << NP|ADJP|VP|ADVP|PP=unmovable)))",
                "ROOT=root << (NP << (PP=unmovable !< (IN < of|about)))",
                "ROOT=root << (PP << PP=unmovable)",
                "ROOT=root << (NP $ VP << PP=unmovable)",
                "ROOT=root << (SBAR=unmovable [ !> VP | $-- /,/ | < RB ])",
                "ROOT=root << (SBAR=unmovable !< WHNP < (/^[^S].*/ !<< that|whether|how))",
                "ROOT=root << (NP=unmovable < EX)",
                "ROOT=root << (/^S/ < `` << NP|ADJP|VP|ADVP|PP=unmovable)",
                "ROOT=root << (PP=unmovable !< /.*NP/)"
        };

        // ToDo: check the validity of the sentence

        // ToDo: Put Leading Adverb Phrases inside Verb phrases

        // ToDo: down case first token


        Tree questionConvertionTree = constituencyParse.deepCopy();
        // mark phrases that should not be answer phrases
        for (String unmovablePhrase : unmovablePhrases) {
            // make a TregexPattern
            TregexPattern unmovablePattern = TregexPattern.compile(unmovablePhrase);
            // make a matcher
            TregexMatcher matcher = unmovablePattern.matcher(questionConvertionTree);

            // get the matching nodes
            while (matcher.find()) {
                Tree foundMatch = matcher.getNode("unmovable");
                String label = foundMatch.label().toString();
                // prepending a special value to the label
                foundMatch.label().setValue("UNMOVABLE-"+label);
            }

        }
        System.out.println("------------------ Unmovable Phrases Marked ----------------");
        System.out.println(questionConvertionTree);
        System.out.println("------------------------------------------------------------");

        // mark possible answer phrases
        Tree answerPhraseMarked = questionConvertionTree.deepCopy();

        // mark main clause subject
        String mainClauseSubjectTregex = "ROOT < (S < (NP|SBAR=subj $+ /,/ !$++ NP|SBAR))";
        TregexPattern mainClauseSubjectPattern = TregexPattern.compile(mainClauseSubjectTregex);
        TregexMatcher mainClauseSubjectMatcher = mainClauseSubjectPattern.matcher(answerPhraseMarked);
        int numAnswerPhrases = 0;
        if (mainClauseSubjectMatcher.find()) {
            Tree foundMatch = mainClauseSubjectMatcher.getNode("subj");
            foundMatch.label().setValue(foundMatch.label().toString()+"-"+numAnswerPhrases);
            numAnswerPhrases++;
            System.out.println("**********" + foundMatch);
        }

        // noun phrases
        String nounPhrasesTregex = "ROOT=root << NP|PP|SBAR=np";
        TregexPattern nounPhrasesPattern = TregexPattern.compile(nounPhrasesTregex);
        TregexMatcher nounPhrasesMatcher = nounPhrasesPattern.matcher(answerPhraseMarked);
        while (nounPhrasesMatcher.find()) {
            Tree foundMatch = nounPhrasesMatcher.getNode("np");
            foundMatch.label().setValue(foundMatch.label().toString()+"-"+numAnswerPhrases);
            numAnswerPhrases++;
        }

        System.out.println(answerPhraseMarked);

        System.out.println("Number of Answer Phrases: "+numAnswerPhrases);

        // iterate over the answer phrases & generate questions
        for (int i = 0; i < numAnswerPhrases; i++) {
            Tree question = answerPhraseMarked.deepCopy();

            // get the answer phrase
            String answerPhraseTregex = "/^(NP|PP|SBAR)-"+i+"$/=answer";
            TregexPattern answerPhrasePattern = TregexPattern.compile(answerPhraseTregex);
            TregexMatcher answerPhraseMatcher = answerPhrasePattern.matcher(question);
            answerPhraseMatcher.find();
            Tree answerPhrase = answerPhraseMatcher.getNode("answer");

            System.out.println("----- Answer Phrase ------");
            System.out.println(answerPhrase);
            System.out.println("--------------------------");

            // remove markers from the tree
            String answerPhraseTreeString = answerPhrase.toString();
            answerPhraseTreeString = answerPhraseTreeString.replaceAll("UNMOVABLE-", "");
            answerPhraseTreeString = answerPhraseTreeString.replaceAll("-\\d+ ", " ");
            answerPhrase = Tree.valueOf(answerPhraseTreeString);

            System.out.println("----- Answer Phrase after Markers Removed ------");
            System.out.println(answerPhrase);
            System.out.println("--------------------------");

            // check if the current answer phrase is the subject
             String subjectCheckTregex = "ROOT=root < (S < NP-"+i+"|SBAR-"+i+")";
            TregexPattern subjectCheckPattern = TregexPattern.compile(subjectCheckTregex);
            // Kartik: I do not think this is correct. should check that tregex expression against
            // the whole sentence and not just answerPhrase.
            TregexMatcher subjectCheckMatcher = subjectCheckPattern.matcher(answerPhrase);
            boolean isSubject = subjectCheckMatcher.find();

            //ToDo: some manipulation
            if (isSubject) {
                // ensure verb agreement for subject
            } else {
                // decompose predicate
                Tree decomposePredicateTree = answerPhraseMarked.deepCopy();
                /*
                Kartik:
                ROOT precedes Simple Declarative clause, which precedes a Verb Phrase which either,
                prcedes a tensed verb that does NOT precede is | was | were etc
                OR
                precedes a tensed verb that does NOT precede another verb phrase

                Lol so weird
                 */
                String predicateTregex = "ROOT < (S=mainclause < (VP=predphrase [ < (/VB.?/=tensedverb !< is|was|were|am|are|has|have|had|do|does|did) | < /VB.?/=tensedverb !< VP ]))";
                TregexPattern predicatePattern = TregexPattern.compile(predicateTregex);
                TregexMatcher predicateMatcher = predicatePattern.matcher(decomposePredicateTree);
                if (predicateMatcher.find()) {
                    Tree subTree = predicateMatcher.getNode("tensedverb");
                    String tensedVerb = subTree.getChild(0).toString();
                    String posTag = subTree.label().toString();
			              String lemma = morphology.lemma(tensedVerb, posTag);
			              System.out.println("Lemma: "+lemma);
                    String aux = getAuxiliarySubtree(subTree);

                    List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
                    List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
                    TsurgeonPattern p;

                    if(!lemma.equals("be")){
                        ps.add(Tsurgeon.parseOperation("replace predphrase (MAINVP=newpred PLACEHOLDER)"));
                        ps.add(Tsurgeon.parseOperation("insert predphrase >-1 newpred"));
                        ps.add(Tsurgeon.parseOperation("insert (VBLEMMA PLACEHOLDER) $+ tensedverb"));
                        ps.add(Tsurgeon.parseOperation("delete tensedverb"));
                        p = Tsurgeon.collectOperations(ps);
                        ops.add(new Pair<TregexPattern,TsurgeonPattern>(predicatePattern,p));
                        Tsurgeon.processPatternsOnTree(ops, decomposePredicateTree);
                        TregexPattern matchPattern = TregexPattern.compile("MAINVP=mainvp");
                        TregexMatcher matcher = matchPattern.matcher(decomposePredicateTree);
                        matcher.find();
                        Tree tmpNode = matcher.getNode("mainvp");
                        tmpNode.removeChild(0);
                        tmpNode.label().setValue("VP");
                        tmpNode.addChild(0, Tree.valueOf(aux));

                        matchPattern = TregexPattern.compile("VBLEMMA=vblemma");
                        matcher = matchPattern.matcher(decomposePredicateTree);
                        matcher.find();
                        tmpNode = matcher.getNode("vblemma");
                        tmpNode.removeChild(0);
                        tmpNode.label().setValue("VB");
                        tmpNode.addChild(0, Tree.valueOf(lemma));
                    }
                }


                // subject auxiliary inversion
                List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
                List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
                String auxiliaryTregex = "ROOT=root < (S=mainclause <+(/VP.*/) (VP < /(MD|VB.?)/=aux < (VP < /VB.?/=baseform)))";
                ps.add(Tsurgeon.parseOperation("relabel root TMPROOT"));
                ps.add(Tsurgeon.parseOperation("prune aux"));
                ps.add(Tsurgeon.parseOperation("insert aux >0 mainclause"));

                TregexPattern auxiliaryPattern = TregexPattern.compile(auxiliaryTregex);
                TsurgeonPattern p = Tsurgeon.collectOperations(ps);
                ops.add(new Pair<TregexPattern,TsurgeonPattern>(auxiliaryPattern,p));
                Tsurgeon.processPatternsOnTree(ops, decomposePredicateTree);

                //copula
                ops.clear();
                ps.clear();

                String copulaTregex = "ROOT=root < (S=mainclause <+(/VP.*/) (VP < (/VB.?/=copula < is|are|was|were|am) !< VP))";
                ps.add(Tsurgeon.parseOperation("relabel root TMPROOT"));
                ps.add(Tsurgeon.parseOperation("prune copula\n"));
                ps.add(Tsurgeon.parseOperation("insert copula >0 mainclause"));

                TregexPattern copulaPattern = TregexPattern.compile(copulaTregex);
                p = Tsurgeon.collectOperations(ps);
                ops.add(new Pair<TregexPattern,TsurgeonPattern>(copulaPattern,p));
                Tsurgeon.processPatternsOnTree(ops, decomposePredicateTree);

                ops.clear();
                ps.clear();
                String rootTregex = "TMPROOT=root";
                ps.add(Tsurgeon.parseOperation("relabel root ROOT"));
                TregexPattern rootPattern = TregexPattern.compile(rootTregex);
                p = Tsurgeon.collectOperations(ps);
                ops.add(new Pair<TregexPattern,TsurgeonPattern>(rootPattern,p));
                Tsurgeon.processPatternsOnTree(ops, decomposePredicateTree);

                question = decomposePredicateTree;

            }

            // relabel main clause from S to SQ
            Tree invertedQuestionClause = question.deepCopy();
            String mainClauseTregex = "ROOT < S=mainclause";
            TregexPattern mainClausePattern = TregexPattern.compile(mainClauseTregex);
            TregexMatcher mainClauseMatcher = mainClausePattern.matcher(invertedQuestionClause);
            if (mainClauseMatcher.matches()) {
                mainClauseMatcher.getNode("mainclause").label().setValue("SQ");
            }
            System.out.println("----- After Relabel ------");
            System.out.println(invertedQuestionClause);
            System.out.println("--------------------------");

            // generate questions
            // extract the answer phrase and generate a question phrase from it
            String answerPhraseExtractTregex = "ROOT=root < (SQ=qclause << /^(NP|PP|SBAR)-"+i+"$/=answer < VP=predicate)";
            TregexPattern answerPhraseExtractPattern = TregexPattern.compile(answerPhraseExtractTregex);
            TregexMatcher answerPhraseExtractMatcher = answerPhraseExtractPattern.matcher(invertedQuestionClause);
            answerPhraseExtractMatcher.find();

            Tree phraseToMove = answerPhraseExtractMatcher.getNode("answer");
            System.out.println("----- Phrase to Move ------");
            System.out.println(phraseToMove);
            System.out.println("---------------------------");

            // phrase answer
            List<Label> answerTokensLabel = phraseToMove.yield();
            List<String> answerTokens = new ArrayList<>();
            //System.out.println("---- Answer Tokens ----");
            for (Label label: answerTokensLabel) {
                //System.out.println(label);
                answerTokens.add(label.toString());
            }

            // extract the noun phrase out of the prepositional phrase
            String extractionTregex = "PP !>> NP ?< RB|ADVP=adverb [< (IN|TO=preposition !$ IN) | < (IN=preposition $ IN=preposition2)] < NP=object";
            TregexPattern extractionPattern = TregexPattern.compile(extractionTregex);
            TregexMatcher extractionMatcher = extractionPattern.matcher(phraseToMove);
            Tree answerNP = phraseToMove;
            String answerPreposition = "";
            Tree answerPrepositionModifier = null;
            if (extractionMatcher.find()) {
                System.out.println("Noun Phrase Found");
                answerNP = extractionMatcher.getNode("object");
                answerPreposition = extractionMatcher.getNode("preposition").yield().toString();
                Tree answerPreposition2 = extractionMatcher.getNode("preposition2");
                if (answerPreposition2 != null) {
                    answerPreposition += " " + answerPreposition2.yield().toString();
                }
                answerPrepositionModifier = extractionMatcher.getNode("adverb");
                System.out.println("---- Answer Preposition ----");
                System.out.println(answerPreposition);
                System.out.println("----------------------------");

                System.out.println("---- Answer Preposition Modifier ----");
                System.out.println(answerPrepositionModifier);
                System.out.println("-------------------------------------");
            } else {
                // ToDo: check if this is a partitive construction
                /*String partitiveConstructionTregex = "NP <<# DT|JJ|CD|RB|NN|JJS|JJR=syntactichead < (PP < (IN < of) < (NP <<# NN|NNS|NNP|NNPS=semantichead)) !> NP ";
                TregexPattern partitiveConstructionPattern = TregexPattern.compile(partitiveConstructionTregex);
                TregexMatcher partitiveConstructionMatcher = partitiveConstructionPattern.matcher(phraseToMove);
                if (partitiveConstructionMatcher.find()) {
                    Tree syntacticHead = partitiveConstructionMatcher.getNode("syntactichead");
                    if ()
                }*/
            }

            // find out the start and end indices of the answer phrase in the original sentence
            int start, end = -1;
            for (start = 0; start < sentenceTokens.size(); start++) {
                for (int j = 0; j < answerTokens.size(); j++) {
                    System.out.println("Comparing: "+sentenceTokens.get(start+j)+", "+answerTokens.get(j));
                    if (!sentenceTokens.get(start+j).equalsIgnoreCase(answerTokens.get(j))) {
                        System.out.println("Done");
                        break;
                    }
                    if (j == answerTokens.size() -1) {
                        end = start +j;
                    }
                }
                if(end != -1) {
                    break;
                }
            }
            System.out.println("--- Answer NP ---");
            System.out.println(answerNP);
            System.out.println("Start: " + start);
            System.out.println("Index of AnswerNP: " + phraseToMove.getLeaves().indexOf(answerNP.headTerminal(new CollinsHeadFinder())));
            System.out.println("-----------------");
            int answerNPHeadTokenIdx = start + phraseToMove.getLeaves().indexOf(answerNP.headTerminal(new CollinsHeadFinder()));
            String headWord = sentenceTokens.get(answerNPHeadTokenIdx);
            String headTag = supersenseTags.get(answerNPHeadTokenIdx);

            // ToDo: identify question type
            List<String> questionTypes = new ArrayList<>();
            questionTypes.add("(WHNP (WRB who))");
            List<String> leftOverPreposition = new ArrayList<>();
            for (int j = 0; j < questionTypes.size(); j++) {
                String phrase = questionTypes.get(j);
                questionTypes.set(j, "(WHNP "+phrase+")");
                String prepositionModifierStr = "";
                if (answerPreposition.length() == 0 || phrase.equals("(WHADVP (WRB when))") || phrase.equals("(WHADVP (WRB where))")) {
                    leftOverPreposition.add(null);
                } else {
                    if (answerPrepositionModifier != null) {
                        prepositionModifierStr = answerPrepositionModifier.yield().toString();
                    }
                    leftOverPreposition.add("(PP "+prepositionModifierStr+" (IN "+answerPreposition+"))");
                }
            }

            for (int j = 0; j < questionTypes.size(); j++) {
                System.out.println(questionTypes.get(j));
                System.out.println(leftOverPreposition.get(j));
            }
            // generate WH Phrase subtrees
            Tree copyTree = invertedQuestionClause.deepCopy();

            List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
            ps.add(Tsurgeon.parseOperation("insert (PREPPLACEHOLDER dummy) $+ answer"));
            ps.add(Tsurgeon.parseOperation("prune answer"));
            ps.add(Tsurgeon.parseOperation("insert (SBARQ=mainclause PLACEHOLDER=placeholder) >0 root"));
            ps.add(Tsurgeon.parseOperation("move qclause >-1 mainclause"));
            TsurgeonPattern p = Tsurgeon.collectOperations(ps);
            List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
            ops.add(new Pair<TregexPattern,TsurgeonPattern>(answerPhraseExtractPattern,p));

            System.out.println("**************** Before ****************");
            System.out.println(copyTree);
            System.out.println("****************************************");
            boolean matchedOnTree = false;
            for(Pair<TregexPattern, TsurgeonPattern> op : ops) {
                if (copyTree == null) break;
                for (TregexMatcher m = op.first().matcher(copyTree); m.find(); m = (op.first.matcher(copyTree))) {
                    matchedOnTree = true;
                    copyTree = op.second().matcher().evaluate(copyTree, m);
                    if(copyTree == null) {
                        break;
                    }
                }
            }
            System.out.println("**************** After ****************");
            System.out.println(copyTree);
            System.out.println("***************************************");

            String copyTreeString = copyTree.toString();
            copyTreeString = copyTreeString.replaceAll("UNMOVABLE-", "");
            copyTreeString = copyTreeString.replaceAll("-\\d+ ", " ");
            copyTree = Tree.valueOf(copyTreeString);

            System.out.println("************* Label Removed ************");
            System.out.println(copyTree);
            System.out.println("****************************************");

            // put the question phrase into the tree and remove the original answer
            for (int j = 0; j < questionTypes.size(); j++) {
                Tree qTree = copyTree.deepCopy();
                String qType = questionTypes.get(j);
                String leftOverPrepositionJ = leftOverPreposition.get(j);
                String operationStr = "ROOT < (SBARQ=mainclause < PLACEHOLDER=ph1) << (__=ph2Parent < PREPPLACEHOLDER=ph2)";
                TregexPattern operationPattern = TregexPattern.compile(operationStr);
                TregexMatcher operationMatcher = operationPattern.matcher(qTree);
                if (!operationMatcher.find()) continue;
                Tree mainClauseNode = operationMatcher.getNode("mainclause");
                // replace the placeholder with a question phrase
                mainClauseNode.removeChild(0);
                mainClauseNode.addChild(0, Tree.valueOf(qType));

                // replace the preposition placeholder with the left over preposition
                Tree prepPlaceholderParent = operationMatcher.getNode("ph2Parent");
                int index = prepPlaceholderParent.objectIndexOf(operationMatcher.getNode("ph2"));
                if (leftOverPrepositionJ != null && leftOverPrepositionJ.length() >0) {
                    prepPlaceholderParent.addChild(index, Tree.valueOf(leftOverPrepositionJ));
                }

                // remove the left over preposition placeholder
                ps.clear();
                ps.add(Tsurgeon.parseOperation("prune ph2"));
                p = Tsurgeon.collectOperations(ps);
                ops.clear();
                ops.add(new Pair<TregexPattern,TsurgeonPattern>(TregexPattern.compile("PREPPLACEHOLDER=ph2"),p));
                Tsurgeon.processPatternsOnTree(ops, qTree);

                qTree = moveLeadingAdjuncts(qTree);

                System.out.println("################## Result ###################");
                System.out.println(qTree);
                for (Word w: qTree.yieldWords()) {
                    System.out.print(w + " ");
                }
                System.out.println();
                System.out.println("#############################################");
            }
        }

    }

    private static Tree moveLeadingAdjuncts(Tree inputTree){

        Tree copyTree = inputTree.deepCopy();
        String tregexOpStr;
        TregexPattern matchPattern;
        TregexMatcher matcher;
        boolean matchFound = true;
        List<Pair<TregexPattern, TsurgeonPattern>> ops;
        List<TsurgeonPattern> ps;
        TsurgeonPattern p;

        while(true){
            ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
            ps = new ArrayList<TsurgeonPattern>();
            tregexOpStr = "TMPROOT=root";
            matchPattern = TregexPattern.compile(tregexOpStr);
            matcher = matchPattern.matcher(copyTree);
            matchFound = matcher.find();
            ps.add(Tsurgeon.parseOperation("relabel root ROOT"));
            p = Tsurgeon.collectOperations(ps);
            ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
            Tsurgeon.processPatternsOnTree(ops, copyTree);

            ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
            ps = new ArrayList<TsurgeonPattern>();

            //for yes/no questions, find any phrases that precede the first possible subject (NP|SBAR)
            // and move them to the front of the question clause.
            tregexOpStr = "ROOT=root < (SQ=mainclause < (/,|ADVP|ADJP|SBAR|S|PP/=mover $,, /MD|VB.*/=pivot $ NP=subject))";
            matchPattern = TregexPattern.compile(tregexOpStr);
            matcher = matchPattern.matcher(copyTree);
            matchFound = matcher.find();

            if(!matchFound){
                //for WH questions, move any phrases that precede the first potential subject
                //--or verb phrase for when the original subject is the answer phrase
                tregexOpStr = "ROOT=root < (SBARQ=mainclause < WHNP|WHPP|WHADJP|WHADVP=pivot < (SQ=invertedclause < (/,|S|ADVP|ADJP|SBAR|PP/=mover !$,, /\\*/ $.. /^VP|VB.*/)))";
                matchPattern = TregexPattern.compile(tregexOpStr);
                matcher = matchPattern.matcher(copyTree);
                matchFound = matcher.find();
            }

            if(!matchFound){
                break;
            }

            //need to relabel as TMPROOT so things are moved one at a time, to preserve their order
            ps.add(Tsurgeon.parseOperation("move mover $+ pivot"));
            ps.add(Tsurgeon.parseOperation("relabel root TMPROOT"));
            p = Tsurgeon.collectOperations(ps);
            ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
            Tsurgeon.processPatternsOnTree(ops, copyTree);

            //System.err.println("moving..."+copyTree.toString());
        }

        //remove extra commas for sentences like "Bill, while walking, saw John."
        Tree firstChild = copyTree.getChild(0);

        if(firstChild.getChild(0).label().toString().equals(",")){
            firstChild.removeChild(0);
        }

        return copyTree;
    }
    private static String getAuxiliarySubtree(Tree tensedverb){
        if(tensedverb == null){
            return "";
        }

        String res = "";
        String label;
        Pattern p = Pattern.compile("\\((\\S+) [^\\)]*\\)");
        Matcher m = p.matcher(tensedverb.toString());
        m.find();
        label = m.group(1);

        if(label.equals("VBD")){
            res = "(VBD did)";
        }else if(label.equals("VBZ")){
            res = "(VBZ does)";
        }else if(label.equals("VBP")){
            res = "(VBP do)";
        }else{
            res = "(VB do)";
        }

        return res;
    }
}
