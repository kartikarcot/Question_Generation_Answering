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

public class DecomposePredicate {
	/*
	This rule tries to match two types of sentences-
	1. Sentences with predicates of the form VP (VB.? ( Everything except another VP))
		Eg- Kartik had a painful time understanding this.
		(ROOT (S (NP (NNP Kartik)) (VP (VBD had) (NP (NP (DT a) (JJ painful) (NN time)) (VP (VBG understanding) (NP (DT this)))))))
	2. Sentences with predicates of the form VP (VB.? (Everything except other auxilaries like is|was etc))
		Eg- Kartik hates reading about grammar.
		(ROOT (S (NP (NNP Kartik)) (VP (VBZ hates) (S (VP (VBG reading) (PP (IN about) (NP (NN grammar)))))) (. .)))
	 */
	String ruleForSelectingSentencesWithPredicates = "ROOT < (S=mainclause < (VP=predphrase [ < (/VB.?/=tensedverb !< is|was|were|am|are|has|have|had|do|does|did) | < /VB.?/=tensedverb !< VP ]) !<< MAINVP)";
	TregexPattern predicatePattern = TregexPattern.compile(ruleForSelectingSentencesWithPredicates);
	Morphology morphology = new Morphology();

	public DecomposePredicate() throws ParseException {
	}

	/*
	If the sentence matches the conditions needed for subject-auxillary inversion then add
	an auxilarry verb in the correct place to enable this behavior.
	Reference for sub-aux inversion: https://en.wikipedia.org/wiki/Subject%E2%80%93auxiliary_inversion
	 */
	public Tree decomposePredicate(Tree sentenceTree) throws Exception {
		Tree sentenceTreeCopy = sentenceTree.deepCopy();
		TregexMatcher predicateMatcher = predicatePattern.matcher(sentenceTreeCopy);
		if (predicateMatcher.find()) {
			// we want to check if lemma is auxilarry or not. if not auxilarry
			// we have to insert appropriate auxilarry in place
			Tree tensedVerbTree = predicateMatcher.getNode("tensedverb");
			// compute a new subtree that will replace the tensed verb in the question
			// this new subtree will replace the verb with an appropriate auxilarry
			Tree auxSubTree = generateAuxSubtree(tensedVerbTree);
			String tensedVerb = tensedVerbTree.getChild(0).toString();
			String posTag = tensedVerbTree.label().toString();
			String lemma = extractLemma(tensedVerb, posTag);

			// if lemma is not be then we can insert auxillary verb
			List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
			List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
			TsurgeonPattern p;
			if (!lemma.equals("be")) {
				// insert  (MAINVP=newpred PLACEHOLDER) $- predphrase
				// move predphrase >-1 newpred
				// insert (VBLEMMA PLACEHOLDER=vblemma) $+ tensedverb
				// delete tensedverb
				// relabel vblemma does
				ps.add(Tsurgeon.parseOperation("insert  (MAINVP=newpred PLACEHOLDER) $- predphrase"));
				// insert predphrase as the last child of MAINVP
				ps.add(Tsurgeon.parseOperation("move predphrase >-1 newpred"));
				ps.add(Tsurgeon.parseOperation("insert (VBLEMMA PLACEHOLDER) $+ tensedverb"));
				ps.add(Tsurgeon.parseOperation("delete tensedverb"));
				p = Tsurgeon.collectOperations(ps);
				ops.add(new Pair<TregexPattern,TsurgeonPattern>(predicatePattern,p));
				Tsurgeon.processPatternsOnTree(ops, sentenceTreeCopy);
				TregexPattern mainVerbPattern = TregexPattern.compile("MAINVP=mainvp");
				TregexMatcher mainVerbMatcher = mainVerbPattern.matcher(sentenceTreeCopy);
				mainVerbMatcher.find();
				Tree tmpNode = mainVerbMatcher.getNode("mainvp");
				// remove the PLACEHOLDER node
				tmpNode.removeChild(0);
				// change MAINVP to VP
				tmpNode.label().setValue("VP");
				tmpNode.addChild(0, auxSubTree);

				TregexPattern verbLemmaPattern = TregexPattern.compile("VBLEMMA=vblemma");
				TregexMatcher verbLemmaMatcher = verbLemmaPattern.matcher(sentenceTreeCopy);
				verbLemmaMatcher.find();
				tmpNode = verbLemmaMatcher.getNode("vblemma");
				tmpNode.removeChild(0);
				tmpNode.label().setValue("VB");
				tmpNode.addChild(Tree.valueOf("("+lemma+")"));
			}
		}
		System.out.println("Decomposed Predicate: " + sentenceTreeCopy.toString());
		return sentenceTreeCopy;
	}

	private Tree generateAuxSubtree(Tree tensedVerbTree) {
		/*
		VBD - Verb, past tense
		VBG - Verb, gerund or present participle
		VBN - Verb, past participle
		VBP - Verb, non-3rd person singular present
		VBZ - Verb, 3rd person singular present
		 */
		Tree auxSubTree = tensedVerbTree.deepCopy();
		String verbform = tensedVerbTree.label().toString();
		switch (verbform) {
			case "VBD":
				auxSubTree.getChild(0).setValue("did");
				break;
			case "VBZ":
				auxSubTree.getChild(0).setValue("does");
				break;
			case "VBP":
				auxSubTree.getChild(0).setValue("do");
				break;
			default:
				auxSubTree.setValue("VB");
				auxSubTree.getChild(0).setValue("do");
				break;
		}
		return auxSubTree;
	}

	private String extractLemma(String verbForm, String posTag) {
		// function can be removed if no more processing is required.
		return morphology.lemma(verbForm, posTag);
	}
}
