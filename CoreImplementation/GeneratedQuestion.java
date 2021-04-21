package CoreImplementation;

public class GeneratedQuestion {
    public String generatedQuestion;
    public String sourceSentence;
    public String answerPhrase;

    public GeneratedQuestion(String generatedQuestion, String sourceSentence, String answerPhrase) {
        this.generatedQuestion = generatedQuestion.replaceAll(" *(\\.|\\,|\\?) *", "$1 ");
        this.generatedQuestion = this.generatedQuestion.replaceAll(" *(\\') *","$1 ");
        this.generatedQuestion = this.generatedQuestion.replaceAll(" \"( +)(.*)( +)\"","\"$2\"");
        if (this.generatedQuestion.length() > 2)
            this.generatedQuestion = this.generatedQuestion.substring(0,this.generatedQuestion.length()-2)+"?";
        this.generatedQuestion = this.generatedQuestion.replaceAll("(\\,|\"|\\')\\?","?");
        this.sourceSentence = sourceSentence;
        this.answerPhrase = answerPhrase;
    }

    public String toString() {
        return this.sourceSentence+" -----> "+this.generatedQuestion+ " --------- "+this.answerPhrase;
    }
}
