import dev.langchain4j.data.audio.Audio;
import dev.langchain4j.model.audio.AudioTranscriptionModel;
import dev.langchain4j.model.audio.AudioTranscriptionRequest;
import dev.langchain4j.model.audio.AudioTranscriptionResponse;
import dev.langchain4j.model.openai.OpenAiAudioTranscriptionModel;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.langchain4j.model.openai.OpenAiAudioTranscriptionModelName.WHISPER_1;

public class OpenAiAudioModelExamples {

    public static void main(String[] args) {
        AudioTranscriptionModel model = OpenAiAudioTranscriptionModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(WHISPER_1)
                .logRequests(true)
                .logResponses(true)
                .build();

        AudioTranscriptionResponse response = model.transcribe(AudioTranscriptionRequest.builder()
                .audio(Audio.builder()
                        .mimeType("audio/wav") // required
                        .binaryData(toBytes("audio.wav")).build())
                .build());

        System.out.println(response.text());
    }

    private static byte[] toBytes(String fileName) {
        try {
            URL fileUrl = OpenAiAudioModelExamples.class.getResource(fileName);
            return Files.readAllBytes(Path.of(fileUrl.toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
