package CoreImplementation;

public class GeneratedQuestion {
    public String generatedQuestion;
    public String sourceSentence;

    public GeneratedQuestion(String generatedQuestion, String sourceSentence) {
        this.generatedQuestion = generatedQuestion;
        this.sourceSentence = sourceSentence;
    }

    public String toString() {
        return this.sourceSentence+" -----> "+this.generatedQuestion;
    }
}
