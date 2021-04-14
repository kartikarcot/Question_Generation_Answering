package CoreImplementation;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class SubjectAuxillaryInversion {

	private List<Pair<TregexPattern, TsurgeonPattern>> copulaTsurgeonOperations;
	private List<Pair<TregexPattern, TsurgeonPattern>> auxTsurgeonOperations;

	SubjectAuxillaryInversion() throws Exception {

		List<TsurgeonPattern> auxTsurgeonPatternList = new ArrayList<TsurgeonPattern>();
		String auxTregexOpStr;
		TregexPattern auxMatchPattern;
		TsurgeonPattern auxTSurgeonPatternCollected;

		/*
		root precedes S
		S precedes (VP < (MD|VB.?)/=aux < VP < /VB.?/=baseform))) with a continuous chain of verb phrases (VP*)
		(VP < (MD|VB.?)/=aux < VP < /VB.?/=baseform)
		VP precedes modals or adverbs. models are irregular verbs that add additional meaning to the main verb. Eg- can could, had, may might
		VP also precedes another VP which contains the main verb
		So in essence, we are looking for verb phrases that have an verb or auxillary and it precedes another verb phrase that has the main verb?
		 */
		auxTregexOpStr = "ROOT=root < (S=mainclause <+(/VP.*/) (VP < /(MD|VB.?)/=aux < (VP < /VB.?/=baseform)))";

		/*
		No we wish to invert the subject and auxillary verb
		to do this we prune the tree rooted at aux
		then we insert the aux tree before the subject which is at the very beginning. This is because usually the NP
		is the subject and it is in the beginning. Will this work with passive voice?
		 */
		auxTsurgeonOperations = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		auxTsurgeonPatternList.add(Tsurgeon.parseOperation("relabel root TMPROOT"));
		auxTsurgeonPatternList.add(Tsurgeon.parseOperation("prune aux"));
		auxTsurgeonPatternList.add(Tsurgeon.parseOperation("insert aux >0 mainclause"));


		auxMatchPattern = TregexPattern.compile(auxTregexOpStr);
		auxTSurgeonPatternCollected = Tsurgeon.collectOperations(auxTsurgeonPatternList);

		auxTsurgeonOperations.add(new Pair<TregexPattern,TsurgeonPattern>(auxMatchPattern,auxTSurgeonPatternCollected));

		/*
		root precedes mainclause which precedes a verb phrase that has a verb of the form is/are/was etc
		this verb phrase also is not dominated by another verb phrase
		Eg: Alvin is a cool guy
		(ROOT (S (NP (N Alvin)) (VP (VB is) (NP ((DET a) (ADJ cool) (N guy))
		Does not match
		Eg: Alvin is walking his dog
		(ROOT (S (NP (N Alvin)) (VP (VB is) (VP (VB walking) (PRON his) (N dog)))
		because the VP (is walking his dog) has another VP (walking his dog)
		however this would be matched by the previous rule for auxillary verbs.
		 */
		List<TsurgeonPattern> copulaTsurgeonPatternList = new ArrayList<TsurgeonPattern>();
		String copulaTregexOpStr;
		TregexPattern copulaMatchPattern;
		TsurgeonPattern copulaTSurgeonPatternCollected;
		copulaTregexOpStr = "ROOT=root < (S=mainclause <+(/VP.*/) (VP < (/VB.?/=copula < is|are|was|were|am) !< VP))";
		copulaTsurgeonPatternList.add(Tsurgeon.parseOperation("relabel root TMPROOT"));
		copulaTsurgeonPatternList.add(Tsurgeon.parseOperation("prune copula\n"));
		copulaTsurgeonPatternList.add(Tsurgeon.parseOperation("insert copula >0 mainclause"));

		copulaMatchPattern = TregexPattern.compile(copulaTregexOpStr);
		copulaTSurgeonPatternCollected = Tsurgeon.collectOperations(copulaTsurgeonPatternList);
		copulaTsurgeonOperations = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		copulaTsurgeonOperations.add(new Pair<TregexPattern,TsurgeonPattern>(copulaMatchPattern,copulaTSurgeonPatternCollected));
	}

	public Tree invertSubjectAuxillary(Tree sentenceTree) throws Exception{
		Tree sentenceTreeCopy = sentenceTree.deepCopy();

		//auxilaries
		Tsurgeon.processPatternsOnTree(auxTsurgeonOperations, sentenceTreeCopy);

		//sentences with the subject that is connected by copula verbs like is are were am etc
		Tsurgeon.processPatternsOnTree(copulaTsurgeonOperations, sentenceTreeCopy);

		// Relabeling tmproot
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		String tregexOpStr = "TMPROOT=root";
		ps.add(Tsurgeon.parseOperation("relabel root ROOT"));
		TregexPattern matchPattern = TregexPattern.compile(tregexOpStr);
		TsurgeonPattern p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, sentenceTreeCopy);

		// print stuff for debugging
		//System.out.println("Subject auxillary inversion: "+sentenceTreeCopy.toString());
		return sentenceTreeCopy;
	}
}
