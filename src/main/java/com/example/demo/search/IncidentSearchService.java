package com.example.demo.search;

import com.example.demo.models.Incident;
import jakarta.annotation.PostConstruct;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class IncidentSearchService {

    @Value("${word2vec.modelPath}")
    private String modelPath;  // e.g., "C:/models/glove.6B.50d.bin" or "C:/models/glove.6B.100d.bin"

    // Weighted approach
    private static final float TFIDF_WEIGHT = 0.3f;
    private static final float WORD2VEC_WEIGHT = 0.7f;

    // Threshold for deciding if an incident is "similar"
    private static final float SIMILARITY_THRESHOLD = 0.05f;

    // Post-scaling factor to artificially bump displayed percentages
    // Increase or decrease to get the range you want
    private static final float SCALING_FACTOR = 2.0f;

    private WordVectors word2VecModel;

    @PostConstruct
    public void initModel() {
        try {
            File binFile = new File(modelPath);
            if (!binFile.exists()) {
                System.err.println("Binary model file not found at: " + modelPath);
                return;
            }

            long start = System.currentTimeMillis();
            this.word2VecModel = WordVectorSerializer.loadStaticModel(binFile);
            long end = System.currentTimeMillis();

            System.out.printf("Binary Word2Vec model loaded successfully in %.2f seconds.%n",
                    (end - start) / 1000.0);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load binary model: " + e.getMessage());
        }
    }

    // Main method to search for similar incidents
    public List<String> searchSimilarIncidents(String incidentNumber, List<Incident> allIncidents) {
        List<String> results = new ArrayList<>();

        // 1. Build global document frequency (DF) map across all incidents
        Map<String, Integer> globalDF = buildGlobalDF(allIncidents);
        int totalDocuments = allIncidents.size();

        // 2. Find the target incident
        Incident targetIncident = allIncidents.stream()
                .filter(inc -> inc.getIncidentNumber().equals(incidentNumber))
                .findFirst()
                .orElse(null);

        if (targetIncident == null) {
            results.add("No matching incident found.");
            return results;
        }

        // 3. Compute global TF-IDF vector for the target incident
        Map<String, Float> targetTFIDF = computeTFIDFMap(targetIncident.getDescription(), globalDF, totalDocuments);

        // 4. Compare with each other incident
        for (Incident incident : allIncidents) {
            if (incident.getIncidentNumber().equals(incidentNumber)) continue;

            Map<String, Float> comparisonTFIDF = computeTFIDFMap(incident.getDescription(), globalDF, totalDocuments);

            float cosineSim = cosineSimilarity(targetTFIDF, comparisonTFIDF);
            float word2vecSim = calculateWord2VecSimilarity(targetIncident.getDescription(), incident.getDescription());

            // Weighted combination
            float finalSimilarity = TFIDF_WEIGHT * cosineSim + WORD2VEC_WEIGHT * word2vecSim;

            // Post-scale the final similarity
            finalSimilarity *= SCALING_FACTOR;  // e.g., multiply by 2.0

            // Clamp to 1.0 if it exceeds
            if (finalSimilarity > 1.0f) {
                finalSimilarity = 1.0f;
            }

            if (finalSimilarity > SIMILARITY_THRESHOLD) {
                results.add(String.format("Incident %s - %s (%.2f%% match)",
                        incident.getIncidentNumber(),
                        incident.getTitle(),
                        finalSimilarity * 100));
            }
        }

        if (results.isEmpty()) {
            results.add("No similar incidents found.");
        }

        return results;
    }

    // Build global document frequency map (DF) from all incidents
    private Map<String, Integer> buildGlobalDF(List<Incident> allIncidents) {
        Map<String, Integer> globalDF = new HashMap<>();
        for (Incident inc : allIncidents) {
            // Use a set to count each token only once per incident
            Set<String> tokens = new HashSet<>(preprocessText(inc.getDescription()));
            for (String token : tokens) {
                globalDF.put(token, globalDF.getOrDefault(token, 0) + 1);
            }
        }
        return globalDF;
    }

    // Compute TF-IDF map using global DF and total document count
    private Map<String, Float> computeTFIDFMap(String text, Map<String, Integer> globalDF, int totalDocuments) {
        List<String> tokens = preprocessText(text);
        Map<String, Integer> tf = new HashMap<>();
        for (String token : tokens) {
            tf.put(token, tf.getOrDefault(token, 0) + 1);
        }
        Map<String, Float> tfidf = new HashMap<>();
        // Use the global vocabulary (keys from globalDF)
        for (String token : globalDF.keySet()) {
            int termFreq = tf.getOrDefault(token, 0);
            int docFreq = globalDF.get(token);
            float idf = (float) Math.log((double) totalDocuments / (docFreq + 1)); // smoothing
            tfidf.put(token, termFreq * idf);
        }
        return tfidf;
    }

    // Cosine similarity between two TF-IDF maps
    private float cosineSimilarity(Map<String, Float> map1, Map<String, Float> map2) {
        float dot = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;
        // Iterate over the union of keys from both maps
        Set<String> allTokens = new HashSet<>();
        allTokens.addAll(map1.keySet());
        allTokens.addAll(map2.keySet());
        for (String token : allTokens) {
            float v1 = map1.getOrDefault(token, 0.0f);
            float v2 = map2.getOrDefault(token, 0.0f);
            dot += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }
        norm1 = (float) Math.sqrt(norm1);
        norm2 = (float) Math.sqrt(norm2);
        if (norm1 == 0 || norm2 == 0) {
            return 0.0f;
        }
        return dot / (norm1 * norm2);
    }

    // Calculate Word2Vec similarity using consistent preprocessing
    private float calculateWord2VecSimilarity(String description1, String description2) {
        List<String> tokens1 = preprocessText(description1);
        List<String> tokens2 = preprocessText(description2);

        tokens1 = filterTokensInVocab(tokens1);
        tokens2 = filterTokensInVocab(tokens2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0f;
        }

        float[] vector1 = word2VecModel.getWordVectorsMean(tokens1).toFloatVector();
        float[] vector2 = word2VecModel.getWordVectorsMean(tokens2).toFloatVector();

        return arrayCosineSimilarity(vector1, vector2);
    }

    private List<String> filterTokensInVocab(List<String> tokens) {
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (word2VecModel.hasWord(token)) {
                result.add(token);
            }
        }
        return result;
    }

    // Cosine similarity for float arrays
    private float arrayCosineSimilarity(float[] vectorA, float[] vectorB) {
        int length = Math.min(vectorA.length, vectorB.length);
        if (length == 0) {
            return 0.0f;
        }
        float dot = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;
        for (int i = 0; i < length; i++) {
            dot += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        normA = (float) Math.sqrt(normA);
        normB = (float) Math.sqrt(normB);
        if (normA == 0 || normB == 0) {
            return 0.0f;
        }
        return dot / (normA * normB);
    }

    // Synonym map for domain-specific terms
    private static final Map<String, List<String>> synonyms = new HashMap<>();
    static {
        // Existing synonyms
        synonyms.put("login", Arrays.asList("signon", "authenticate"));
        synonyms.put("error", Arrays.asList("issue", "bug", "fault", "problem"));
        synonyms.put("update", Arrays.asList("patch", "upgrade"));
        synonyms.put("patch", Arrays.asList("update", "fix"));
        synonyms.put("server", Arrays.asList("system", "machine", "host"));
        synonyms.put("crash", Arrays.asList("failure", "shutdown", "halt"));
        synonyms.put("post", Arrays.asList("after", "next")); // bridging "post" <-> "after"

        // IT-related synonyms
        synonyms.put("it", Arrays.asList("technology", "tech"));
        synonyms.put("network", Arrays.asList("infrastructure", "connectivity"));
        synonyms.put("security", Arrays.asList("protection", "safeguard"));
        synonyms.put("software", Arrays.asList("program", "application"));
        synonyms.put("hardware", Arrays.asList("equipment", "machinery"));
        synonyms.put("maintenance", Arrays.asList("upkeep", "servicing"));

        // HR-related synonyms
        synonyms.put("hr", Arrays.asList("human resources", "personnel", "staffing"));
        synonyms.put("recruit", Arrays.asList("hire", "enlist", "staff"));
        synonyms.put("onboard", Arrays.asList("induct", "integrate"));
        synonyms.put("benefit", Arrays.asList("perk", "advantage", "compensation"));
        synonyms.put("employee", Arrays.asList("staff", "worker", "personnel"));
        synonyms.put("training", Arrays.asList("development", "education"));
        synonyms.put("payroll", Arrays.asList("salary", "wages", "compensation"));

        // Support-related synonyms
        synonyms.put("support", Arrays.asList("help", "assistance", "service"));
        synonyms.put("ticket", Arrays.asList("issue", "request", "case"));
        synonyms.put("troubleshoot", Arrays.asList("diagnose", "debug"));
        synonyms.put("resolution", Arrays.asList("fix", "solution", "remedy"));
        synonyms.put("customer", Arrays.asList("client", "user"));
        synonyms.put("service", Arrays.asList("assistance", "support", "helpdesk"));
    }


    // Preprocess text: tokenize, lowercase, remove punctuation/stop words, and expand synonyms.
    // (No bigrams for consistency with the Word2Vec model.)
    private List<String> preprocessText(String text) {
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "a", "the", "is", "in", "and", "to", "of", "for", "on",
                "with", "as", "this", "that", "it", "by", "an", "be",
                "or", "we", "has", "have", "was", "from"
        ));

        List<String> tokens = new ArrayList<>();
        for (String word : text.split("\\s+")) {
            String token = word.toLowerCase().replaceAll("[^a-zA-Z]", "");
            if (!stopWords.contains(token) && !token.isEmpty()) {
                tokens.add(token);
            }
        }

        // Expand synonyms
        List<String> expandedTokens = new ArrayList<>(tokens);
        for (String token : tokens) {
            if (synonyms.containsKey(token)) {
                expandedTokens.addAll(synonyms.get(token));
            }
        }

        return expandedTokens;
    }
}
