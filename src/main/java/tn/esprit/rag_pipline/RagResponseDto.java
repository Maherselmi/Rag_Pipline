package tn.esprit.rag_pipline;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RagResponseDto {

    private String answer;

}