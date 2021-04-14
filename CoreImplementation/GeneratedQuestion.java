package CoreImplementation;

public class GeneratedQuestion {
    public String generatedQuestion;
    public String sourceSentence;
    public String answerPhrase;

    public GeneratedQuestion(String generatedQuestion, String sourceSentence, String answerPhrase) {
        this.generatedQuestion = generatedQuestion;
        this.sourceSentence = sourceSentence;
        this.answerPhrase = answerPhrase;
    }

    public String toString() {
        return this.sourceSentence+" -----> "+this.generatedQuestion+ " --------- "+this.answerPhrase;
    }
}
