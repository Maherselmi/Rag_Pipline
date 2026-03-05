package tn.esprit.rag_pipline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.rag_pipline.model.QuestionRequest;
import tn.esprit.rag_pipline.model.ReponseRAG;
import tn.esprit.rag_pipline.model.SinistreDocument;
import tn.esprit.rag_pipline.service.PDFProcessingService;
import tn.esprit.rag_pipline.service.RAGService;

import java.util.List;

@RestController
@RequestMapping("/api/sinistres")
@RequiredArgsConstructor
public class SinistreController {

    private final PDFProcessingService pdfProcessingService;
    private final RAGService ragService;

    @PostMapping("/documents/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            List<SinistreDocument> documents = pdfProcessingService.processPDF(file);
            documents.forEach(ragService::indexDocument);
            return ResponseEntity.ok("Document traité et indexé avec succès: " + documents.size() + " segments");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du traitement: " + e.getMessage());
        }
    }

    @PostMapping("/question")
    public ResponseEntity<ReponseRAG> poserQuestion(@RequestBody QuestionRequest request) {
        try {
            ReponseRAG reponse = ragService.answerQuestion(request);
            return ResponseEntity.ok(reponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/documents/{sinistreId}")
    public ResponseEntity<List<SinistreDocument>> getDocumentsBySinistre(@PathVariable String sinistreId) {
        // Implémenter la récupération des documents par sinistre
        return ResponseEntity.ok().build();
    }
}