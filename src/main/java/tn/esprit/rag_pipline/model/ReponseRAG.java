package tn.esprit.rag_pipline.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReponseRAG {
    private String reponse;
    private List<SourceDocument> sources;
    private double tempsExecution;
}
