package domain;

import dev.langchain4j.model.output.structured.Description;

public class Cv {
    @Description("skills of the cadidate, comma-concatenated")
    private String skills;

    @Description("professional experience of the candidate")
    private String professionalExperience;

    @Description("studies of the candidate")
    private String studies;

    @Override
    public String toString() {
        return "CV:\n" +
                "skills = \"" + skills + "\"\n" +
                "professionalExperience = \"" + professionalExperience + "\"\n" +
                "studies = \"" + studies + "\"\n";
    }
}
