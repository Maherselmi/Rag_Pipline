package tn.esprit.rag_pipline.service;


import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.rag_pipline.model.QuestionRequest;
import tn.esprit.rag_pipline.model.ReponseRAG;
import tn.esprit.rag_pipline.model.SinistreDocument;
import tn.esprit.rag_pipline.model.SourceDocument;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAGService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatLanguageModel;
    private final PDFProcessingService pdfProcessingService;

    public void indexDocument(SinistreDocument document) {
        TextSegment segment = TextSegment.from(document.getContent());
        Embedding embedding = embeddingModel.embed(segment.text()).content();

        // Ajouter des métadonnées
        segment.metadata().put("fileName", document.getFileName());
        segment.metadata().put("sinistreId", document.getSinistreId());
        segment.metadata().put("typeDocument", document.getTypeDocument());

        embeddingStore.add(embedding, segment);
        log.info("Document indexé: {}", document.getFileName());
    }

    public ReponseRAG answerQuestion(QuestionRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. Embedding de la question
        Embedding questionEmbedding = embeddingModel.embed(request.getQuestion()).content();

        // 2. Recherche des documents pertinents
        int maxResults = request.getMaxResults() > 0 ? request.getMaxResults() : 5;
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                questionEmbedding, maxResults
        );

        // 3. Construction du contexte
        String context = buildContext(matches);

        // 4. Création du prompt
        String prompt = buildPrompt(request.getQuestion(), context);

        // 5. Génération de la réponse
        AiMessage response = chatLanguageModel.generate(UserMessage.from(prompt)).content();

        // 6. Construction de la réponse
        double executionTime = (System.currentTimeMillis() - startTime) / 1000.0;

        return ReponseRAG.builder()
                .reponse(response.text())
                .sources(mapToSources(matches))
                .tempsExecution(executionTime)
                .build();
    }

    private String buildContext(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream()
                .map(match -> {
                    String content = match.embedded().text();
                    String source = match.embedded().metadata().get("fileName");
                    return String.format("[Source: %s]\n%s\n", source, content);
                })
                .collect(Collectors.joining("\n---\n"));
    }

    private String buildPrompt(String question, String context) {
        return String.format("""
            Tu es un expert en gestion de sinistres. Utilise les informations suivantes 
            pour répondre à la question de l'utilisateur. Si l'information n'est pas 
            disponible dans le contexte, dis-le honnêtement.
            
            Contexte:
            %s
            
            Question: %s
            
            Réponse:""", context, question);
    }

    private List<SourceDocument> mapToSources(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream()
                .map(match -> SourceDocument.builder()
                        .fileName(match.embedded().metadata().get("fileName"))
                        .content(match.embedded().text())
                        .score(match.score())
                        .build())
                .collect(Collectors.toList());
    }
}