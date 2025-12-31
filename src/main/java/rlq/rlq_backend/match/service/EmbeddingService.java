package rlq.rlq_backend.match.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;

import lombok.RequiredArgsConstructor;
import rlq.rlq_backend.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final double threshold = 0.7;

    public void assertNotTooClose(String userText, String correctText) {
        String u = normalize(userText);
        String c = normalize(correctText);

        // فلترة سريعة قبل embedding (مهمة)
        if (u.equals(c) || u.contains(c) || c.contains(u)) {
            throw new BusinessException("مبروك جبتها صح! الحين لازم تكتب اجابة ثانية");
        }

        if (!userText.matches("-?\\p{Nd}+(\\.\\p{Nd}+)?") || !correctText.matches("-?\\p{Nd}+(\\.\\p{Nd}+)?")) {

            float[] uVec = toFloatArray(embeddingModel.embed(u));
            float[] cVec = toFloatArray(embeddingModel.embed(c));

            double sim = cosineSimilarity(uVec, cVec);

            if (sim >= threshold) {
                throw new BusinessException("مبروك جبتها صح! الحين لازم تكتب اجابة ثانية");
            }
            try {
                Thread.sleep(9000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String normalize(String s) {
        if (s == null)
            return "";
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        int n = Math.min(a.length, b.length);
        for (int i = 0; i < n; i++) {
            dot += (double) a[i] * b[i];
            na += (double) a[i] * a[i];
            nb += (double) b[i] * b[i];
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    // بعض نسخ Spring AI ترجع List<Double> من embed()
    private float[] toFloatArray(Object embedResult) {
        if (embedResult instanceof float[] f)
            return f;

        if (embedResult instanceof java.util.List<?> list) {
            float[] arr = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = ((Number) list.get(i)).floatValue();
            }
            return arr;
        }

        throw new IllegalStateException("Unexpected embedding result type: " + embedResult.getClass());
    }
}