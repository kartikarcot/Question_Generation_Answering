package CoreImplementation;

public class GeneratedQuestion implements Comparable<GeneratedQuestion>{
    public String generatedQuestion;
    public String sourceSentence;
    public String answerPhrase;
    public Double score;

    public GeneratedQuestion(String generatedQuestion, String sourceSentence, String answerPhrase, Double score) {
        this.generatedQuestion = generatedQuestion.replaceAll(" *(\\.|\\,|\\?) *", "$1 ");
        this.generatedQuestion = this.generatedQuestion.replaceAll(" *(\\') *","$1 ");
        this.generatedQuestion = this.generatedQuestion.replaceAll(" \"( +)(.*)( +)\"","\"$2\"");
        if (this.generatedQuestion.length() > 2)
            this.generatedQuestion = this.generatedQuestion.substring(0,this.generatedQuestion.length()-2)+"?";
        this.generatedQuestion = this.generatedQuestion.replaceAll("(\\,|\"|\\')\\?","?");
        this.sourceSentence = sourceSentence;
        this.answerPhrase = answerPhrase;
        this.score = score;
    }

    public String toString() {
        return this.sourceSentence+" -----> "+this.generatedQuestion+ " --------- "+this.answerPhrase + " --------- "+this.score;
    }

    @Override
    public int compareTo(GeneratedQuestion o) {
        if (this.score < o.score) return 1;
        return -1;
    }
}
