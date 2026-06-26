package com.github.jiwoofar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Scanner;

public class Main {
    /*private static final String API_URL = "https://api.openai.com/v1/responses";
    private static final String OPENAI_API_KEY_ENV = "OPENAI_API_KEY";
    private static final String MODEL = "gpt-5.4-mini";*/

    private static final String API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "qwen3:4b";

    public static void main(String[] args) throws Exception {
        /*String apiKey = System.getenv(OPENAI_API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY 환경 변수를 설정하세요.");
            return;
        }*/

        String question = readQuestion(args);
        if (question.isEmpty()) {
            System.err.println("질문이 비어 있습니다.");
            return;
        }

        //String answer = askOpenAI(apiKey, question);
        String answer = askOpenAI(question);
        System.out.println();
        System.out.println("답변:");
        System.out.println(answer);
    }

    private static String readQuestion(String[] args) {
        if (args.length > 0) {
            return String.join(" ", args).trim();
        }

        System.out.print("질문: ");
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
    }

    //private static String askOpenAI(String apiKey, String question) throws IOException, InterruptedException {
    private static String askOpenAI(String question) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.createObjectNode()
                .put("model", MODEL)
                .put("prompt", question)
                .put("stream", false)
                .toString();
        System.out.println(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(60))
                //.header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());

        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("OpenAI API 호출 실패: " + response.statusCode() + " " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("response").asText();

        /*JsonNode output = root.path("output");
        if (output.isMissingNode() || !output.isArray()) {
            throw new IllegalStateException("응답 형식을 해석할 수 없습니다.");
        }

        for (JsonNode item : output) {
            JsonNode content = item.path("content");
            if (!content.isArray()) {
                continue;
            }
            for (JsonNode part : content) {
                if ("output_text".equals(part.path("type").asText())) {
                    return part.path("text").asText();
                }
            }
        }

        throw new IllegalStateException("응답에서 텍스트를 찾지 못했습니다.");
         */
    }
}
