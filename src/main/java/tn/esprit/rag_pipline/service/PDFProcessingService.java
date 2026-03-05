package tn.esprit.rag_pipline.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.rag_pipline.model.SinistreDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PDFProcessingService {

    public List<SinistreDocument> processPDF(MultipartFile file) throws IOException {
        List<SinistreDocument> documents = new ArrayList<>();

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Diviser le document en segments significatifs
            List<String> segments = splitIntoChunks(text, 500); // chunks de 500 caractères

            for (int i = 0; i < segments.size(); i++) {
                SinistreDocument sinistreDoc = SinistreDocument.builder()
                        .id(file.getOriginalFilename() + "_chunk_" + i)
                        .fileName(file.getOriginalFilename())
                        .content(segments.get(i))
                        .sinistreId(extractSinistreId(text))
                        .typeDocument(detectDocumentType(text))
                        .dateSinistre(extractDate(text))
                        .montant(extractMontant(text))
                        .build();

                documents.add(sinistreDoc);
            }
        }

        return documents;
    }

    private List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(text.length(), i + chunkSize);
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }

    private String extractSinistreId(String text) {
        // Pattern pour trouver un ID de sinistre (ex: SIN-2024-001)
        Pattern pattern = Pattern.compile("SIN-\\d{4}-\\d{3}");
        var matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : "UNKNOWN";
    }

    private String detectDocumentType(String text) {
        if (text.toLowerCase().contains("constat")) return "CONSTAT";
        if (text.toLowerCase().contains("rapport")) return "RAPPORT";
        if (text.toLowerCase().contains("facture")) return "FACTURE";
        return "AUTRE";
    }

    private String extractDate(String text) {
        // Pattern pour trouver une date (format simplifié)
        Pattern pattern = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");
        var matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String extractMontant(String text) {
        // Pattern pour trouver un montant
        Pattern pattern = Pattern.compile("(\\d+[.,]?\\d*)\\s*(€|EUR)");
        var matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }
}