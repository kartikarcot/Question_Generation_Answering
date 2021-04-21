//Use Absolute path to initialize the constructor
//getText() returns the content of a file as a string



package CoreImplementation;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DataLoader {

    String content;

    public DataLoader(String documentPath) {
        initialize(documentPath);
    }

    public void initialize(String documentPath) {
        Path path = Paths.get(documentPath);
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            this.content = "";
            for (String line : lines) {
                String[] words = line.split("\\s+");
                if (words.length >= 4) {
                    this.content += " " + line;
                }
            }
//            this.content = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Exception caught in DataLoader::Initialize");
            e.printStackTrace();
        }
    }

    public String getText()
    {
        return this.content;
    }
}
