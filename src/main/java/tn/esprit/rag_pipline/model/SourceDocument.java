package tn.esprit.rag_pipline.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceDocument {
    private String fileName;
    private String content;
    private double score;
}