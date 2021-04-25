package CoreImplementation;


import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.ParseException;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//Example sentence: He is a master's student
//(ROOT (S (NP (PRP He)) (VP (VBZ is) (NP (NP (DT a) (NNP Master) (POS 's)) (NN Student))) (. !)))
//Subject : "He", we want to relabel this to SQ
public class RelabelMainClause {

    String ruleForFindingMainClause = "ROOT < S=subject";
    TregexPattern MainClausePattern = TregexPattern.compile(ruleForFindingMainClause);
    Tree sentenceTreeCopy;

    public RelabelMainClause(Tree sentenceTree) throws Exception {

        sentenceTreeCopy = sentenceTree.deepCopy();
        TregexMatcher mainClauseMatcher = MainClausePattern.matcher(sentenceTreeCopy);
        if (mainClauseMatcher.find()) {
            mainClauseMatcher.getNode("subject").label().setValue("SQ");
        }

    }


}
