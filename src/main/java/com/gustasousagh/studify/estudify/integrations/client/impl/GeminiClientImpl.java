package com.gustasousagh.studify.estudify.integrations.client.impl;

import com.gustasousagh.studify.estudify.integrations.client.GeminiClient;
import com.gustasousagh.studify.estudify.model.request.GenerateCourseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GeminiClientImpl implements GeminiClient {

    private final WebClient webClient;

    @Override
    public Mono<String> generateCourseRoadmap(GenerateCourseRequest generateCourseRequest) {

        String requestBody = String.format("""
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "Você é um assistente especializado em gerar roadmaps de cursos. \\nCrie um roadmap de curso para o seguinte assunto: %s \\nO roadmap deve conter %d módulos, cada um com um título e um objetivo claro. O objetivo deve descrever o que o aluno deve saber ao final do módulo. e assegure que o titulo comece com Modulo X, onde X é o número do módulo. e adicione o numero do modulo em order"
                }
              ]
            }
          ],
          "generationConfig": {
            "responseMimeType": "application/json",
            "responseSchema": {
              "type": "object",
              "properties": {
                "modules": {
                  "type": "array",
                  "minItems": 5,
                  "maxItems": 20,
                  "items": {
                    "type": "object",
                    "properties": {
                      "title": {
                        "type": "string",
                        "description": "Título do módulo"
                      },
                      "objective": {
                        "type": "string",
                        "description": "O que o aluno deve saber ao final"
                      },
                      "order": {
                        "type": "integer",
                        "description": "Número do módulo (1-based)"
                       },
                    },
                    "required": ["title", "objective", "order"]
                  }
                }
              },
              "required": ["modules"]
            }
          }
        }
        """, generateCourseRequest.getCourseDescription(), generateCourseRequest.getCourseNumberOfModules());

        return webClient.post()
                .uri("/v1beta/models/gemini-2.0-flash-lite:generateContent")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public Mono<String> generateCourseContent(String courseContent, String courseTitle) {

        String requestBody = String.format("""
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "Você é um especialista em design instrucional e produção de cursos em Markdown. Título do curso: %s\\\\n- Módulo/assunto: %s - Comece com: # {Título do curso}\\\\n2. Introdução breve (3–5 linhas) contextualizando o tema.\\\\n3. Sumário dos tópicos do módulo (lista).\\\\n4. Crie de 3 a 6 tópicos. Para cada tópico, siga o formato:\\\\n   ## {Título do tópico}\\\\n   **Objetivo de aprendizagem:** {1 frase clara}\\\\n   ### Conteúdo\\\\n   - Conceitos-chave (listas)\\\\n   - Explicações passo a passo\\\\n   ### Exemplos\\\\n   - 2–4 exemplos resolvidos\\\\n   ### Exercícios\\\\n   - 3–5 exercícios práticos (dificuldade crescente)\\\\n5. Finalize com: ## Revisão e próximos passos (síntese + sugestões).\\\\n6. Use apenas Markdown simples (pt-BR). Se precisar de notação matemática, use LaTeX inline entre $...$ ou texto claro.\\\\n7."
                }
              ]
            }
          ],
          "generationConfig": {
            "responseMimeType": "application/json",
            "responseSchema": {
              "type": "object",
              "properties": {
                "content": {
                  "type": "string",
                  "description": "Conteúdo do curso gerado em markdown"
                }
              },
              "required": ["content"]
            }
          }
        }
        """, courseContent, courseTitle);


        return webClient.post()
                .uri("/v1beta/models/gemini-2.0-flash-lite:generateContent")
                .bodyValue(requestBody)
                .retrieve()

                .bodyToMono(String.class);
    }


}
