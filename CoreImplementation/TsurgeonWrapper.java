package CoreImplementation;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class TsurgeonWrapper {
    public Tree resultingTree;
    public TsurgeonWrapper(Tree sentenceTree,
                           String pattern,
                           List<String> operations) {
        initialize(sentenceTree, pattern, operations);
    }

    public void initialize(Tree sentenceTree,
                           String pattern,
                           List<String> operations) {
        resultingTree = sentenceTree;

        List<Pair<TregexPattern, TsurgeonPattern>> operationList = new ArrayList<>();
        List<TsurgeonPattern> tsurgeonPatternList = new ArrayList<>();

        for (String operation : operations) {
            tsurgeonPatternList.add(Tsurgeon.parseOperation(operation));
        }

        TsurgeonPattern collectedTsurgeonPattern = Tsurgeon.collectOperations(tsurgeonPatternList);

        TregexPattern tregexPattern = TregexPattern.compile(pattern);
        operationList.add(new Pair<>(tregexPattern, collectedTsurgeonPattern));
        //System.out.println("verify: "+ resultingTree);
        Tsurgeon.processPatternsOnTree(operationList, resultingTree);
    }
}
