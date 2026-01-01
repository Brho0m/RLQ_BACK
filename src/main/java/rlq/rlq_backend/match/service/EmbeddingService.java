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

    public void assertNotTooClose(String userText, String correctText) {
        String u = normalizeStrongly(userText);
        String c = normalizeStrongly(correctText);

        // فلترة سريعة قبل embedding (مهمة)
        if (u.equals(c)) {
            throw new BusinessException("مبروك جبتها صح! الحين لازم تكتب اجابة ثانية");
        }

        if (!u.matches("-?\\p{Nd}+(\\.\\p{Nd}+)?") || !c.matches("-?\\p{Nd}+(\\.\\p{Nd}+)?")) {

            float[] uVec = toFloatArray(embeddingModel.embed(u));
            float[] cVec = toFloatArray(embeddingModel.embed(c));

            double sim = cosineSimilarity(uVec, cVec);

            if (sim >= 0.65) {
                throw new BusinessException("مبروك جبتها صح! الحين لازم تكتب اجابة ثانية");
            }
        }
    }

    private static final String[] DATE_STOP_WORDS = {
            "ميلادي",
            "هجري",
            "دولة",
            "مدينة",
            "دوله",
            "مدينه",
            "م",
            "هـ",
            "ه"
    };

    private String removeDateWords(String s) {
        for (String w : DATE_STOP_WORDS) {
            s = s.replaceAll("(^|\\s)" + java.util.regex.Pattern.quote(w) + "(?=\\s|$)", "");
        }
        return s;
    }


    private String normalizeDigits(String input) {
        if (input == null)
            return null;

        StringBuilder sb = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (ch >= '٠' && ch <= '٩') {
                sb.append((char) ('0' + (ch - '٠'))); // Arabic-Indic
            } else if (ch >= '۰' && ch <= '۹') {
                sb.append((char) ('0' + (ch - '۰'))); // Persian
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private String normalizeStrongly(String s) {
        if (s == null)
            return "";

        s = normalizeDigits(s);
        s = s.trim().toLowerCase();

        // إزالة التشكيل العربي
        s = s.replaceAll("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]", "");

        // توحيد بعض الحروف العربية (اختياري)
        s = s.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("٫", ".").replace("٬", ".")
                .replace(",", ".")
                .replace("ى", "ي").replace("ـ", "").replace("ڤ", "ف").replace("چ", "ج");

        // خلي أي شيء مو حرف/رقم = مسافة
        s = s.replaceAll("[^\\p{L}\\p{Nd}.]+", " ");

        s = s.replaceAll("(\\d)([\\p{L}]+)", "$1 $2");

        // ضغط المسافات
        s = s.replaceAll("\\s+", " ").trim();

        s = removeDateWords(s);

        return s;
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