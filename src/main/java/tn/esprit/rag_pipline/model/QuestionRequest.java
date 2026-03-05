package tn.esprit.rag_pipline.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class QuestionRequest {
    private String question;
    private String sinistreId; // optionnel, pour filtrer par sinistre
    private int maxResults;
}

