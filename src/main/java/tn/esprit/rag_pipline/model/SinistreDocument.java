package tn.esprit.rag_pipline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinistreDocument {
    private String id;
    private String fileName;
    private String content;
    private String sinistreId;
    private String typeDocument; // constat, rapport, facture, etc.
    private String dateSinistre;
    private String montant;
    private String statut;
}
