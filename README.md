# NLP

Tregex API Link: https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/trees/tregex/TregexPattern.html

Tsurgeon API Link: https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/trees/tregex/tsurgeon/Tsurgeon.html

Paper talking about syntactic tree tags: http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.9.8216&rep=rep1&type=pdf

Coreference Resolution Tutorial Link: https://stanfordnlp.github.io/CoreNLP/coref.html

In order to run, please download stanford-corenlp-4.2.0 from this link: https://stanfordnlp.github.io/CoreNLP/

Maybe you need stanford-tregex-2020-11-17 but this should already be included stanfrord-corenlp-4.2.0. If some error is thrown about tregex, download it from this link: https://nlp.stanford.edu/software/tregex.shtml

## Code Description
StanfordCoreNLPTest.java is the prototype implementation, you can use it as a reference while implementing your module.

All of the useful code are in the CoreImplementation directory
ImplementationTest.java is a main class for testing your implementations
DocumentParser.java preprocesses a document
ParsedSentence.java stores a parsed sentences in many forms: raw text, tokens, NER labels, and Tree
TregexMatcherWrapper.java is a wrapper for Tregex that we will use
NounPhraseMatcher.java is an example module that uses the TregexMatcherWrapper
TsurgeonWrapper.java is a wrapper for Tsurgeon that we will be using
NodePruner.java is an example module that uses the TsurgeonWrapper

If you are implementing "Extract NER from found Noun Phrase", please note that NER tags are already generated and stored in ParsedSentence. Your job is to find the index of a noun phrase and find the associated NER tag for those indices.
