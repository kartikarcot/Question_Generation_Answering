package CoreImplementation;

import edu.stanford.nlp.naturalli.VerbTense;
import edu.stanford.nlp.process.Morphology;

public class MainClauseSubjectVerbConversion {
    Morphology morphology = new Morphology();

    public String convertToAppropriateForm(String verb, String tag) {
        if (tag.equals("VBD") || tag.equals("VBZ")) {
            return "("+tag+" "+verb+")";
        }
        String originalForm = morphology.lemma(verb, tag);
        return "(VBZ "+ VerbTense.SINGULAR_PRESENT_THIRD_PERSON.conjugateEnglish(originalForm)+")";
    }
}
