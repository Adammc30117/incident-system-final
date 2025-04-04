package com.example.demo.search;

import com.example.demo.models.Incident;
import jakarta.annotation.PostConstruct;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.demo.dto.IncidentMatchResult;

import java.io.File;
import java.util.*;

/**
 * IncidentSearchService.java
 *
 * This service provides semantic search capabilities for incidents using a hybrid
 * combination of TF-IDF and Word2Vec. It calculates similarity scores between
 * a target incident and a pool of existing incidents to recommend related ones.
 *
 * Features:
 * - Loads a pre-trained Word2Vec binary model at startup
 * - Calculates TF-IDF vectors and Word2Vec vectors for incident text
 * - Computes cosine similarity between incidents
 * - Returns matches that exceed a defined similarity threshold
 *
 * Developed as part of the Intelligent Incident Resolution System.
 */

@Service
public class IncidentSearchService {

    @Value("${word2vec.modelPath}")
    private String modelPath;

    // Weighted approach
    private static final float TFIDF_WEIGHT = 0.5f;
    private static final float WORD2VEC_WEIGHT = 0.5f;

    // Threshold for deciding if an incident is "similar"
    private static final float SIMILARITY_THRESHOLD = 0.5f;

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

    /**
     * Searches for incidents that are semantically similar to the specified target incident.
     *
     * This method combines TF-IDF cosine similarity and Word2Vec embedding similarity to calculate
     * a hybrid similarity score between the target incident and every other incident in the dataset.
     * Only incidents that exceed a predefined similarity threshold are returned.
     *
     * @param incidentNumber The unique identifier of the incident to compare others against.
     * @param allIncidents   A list of all available incidents in the system.
     * @return A list of IncidentMatchResult objects representing the most similar incidents.
     */
    public List<IncidentMatchResult> searchSimilarIncidents(String incidentNumber, List<Incident> allIncidents) {
        List<IncidentMatchResult> results = new ArrayList<>();

        // 1. Build global document frequency (DF) map across all incidents
        Map<String, Integer> globalDF = buildGlobalDF(allIncidents);
        int totalDocuments = allIncidents.size();

        // 2. Find the target incident
        Incident targetIncident = allIncidents.stream()
                .filter(inc -> inc.getIncidentNumber().equals(incidentNumber))
                .findFirst()
                .orElse(null);

        if (targetIncident == null) {
            return results; // returns an empty list
        }

        // 3. Compute global TF-IDF vector for the target incident
        String combinedTargetText = targetIncident.getTitle() + " " + targetIncident.getDescription();
        Map<String, Float> targetTFIDF = computeTFIDFMap(combinedTargetText, globalDF, totalDocuments);

        // 4. Compare with each other incident
        for (Incident incident : allIncidents) {
            if (incident.getIncidentNumber().equals(incidentNumber)) continue;

            String combinedIncidentText = incident.getTitle() + " " + incident.getDescription();
            Map<String, Float> comparisonTFIDF = computeTFIDFMap(combinedIncidentText, globalDF, totalDocuments);
            float cosineSim = cosineSimilarity(targetTFIDF, comparisonTFIDF);
            float word2vecSim = calculateWord2VecSimilarity(combinedTargetText, combinedIncidentText);
            float finalSimilarity = TFIDF_WEIGHT * cosineSim + WORD2VEC_WEIGHT * word2vecSim;
            finalSimilarity *= SCALING_FACTOR;
            if (finalSimilarity > 1.0f) {
                finalSimilarity = 1.0f;
            }

            if (finalSimilarity > SIMILARITY_THRESHOLD) {
                String teamName = incident.getAssignedTeam() != null ? incident.getAssignedTeam().getName() : "Unassigned";
                String adminUsername = incident.getAssignedAdmin() != null ? incident.getAssignedAdmin().getUsername() : "Unassigned";

                IncidentMatchResult matchResult = new IncidentMatchResult(
                        incident.getIncidentNumber(),
                        incident.getTitle(),
                        incident.getDescription(),
                        incident.getSeverityLevel(),
                        incident.getStatus(),
                        teamName,
                        adminUsername,
                        finalSimilarity * 100.0, // match percentage
                        incident.getResolution() != null ? incident.getResolution() : "No resolution available"
                );

                results.add(matchResult);
            }
        }

        return results;
    }

    /**
     * Builds a global document frequency (DF) map from a list of incidents.
     *
     * The DF map stores how many documents (incident descriptions) each unique token appears in.
     * It ensures each token is only counted once per document by using a Set.
     *
     * @param allIncidents The list of all incidents in the system.
     * @return A map containing each token and the number of documents it appears in.
     */
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

    /**
     * Computes the TF-IDF vector for a given text using a global document frequency map and total document count.
     *
     * The method calculates term frequency (TF) for each token in the input text, then multiplies each TF value
     * by the corresponding inverse document frequency (IDF), derived from the global document frequency (DF) map.
     *
     * @param text            The text to convert into a TF-IDF vector.
     * @param globalDF        A map containing the number of documents each term appears in.
     * @param totalDocuments  The total number of documents in the dataset.
     * @return A map of terms to their corresponding TF-IDF scores.
     */
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

    /**
     * Computes the cosine similarity between two TF-IDF vectors.
     *
     * This function takes two maps representing TF-IDF vectors and calculates
     * the cosine of the angle between them, which is a measure of their
     * similarity. A higher value indicates greater similarity.
     *
     * @param map1 The first TF-IDF vector represented as a map of token to weight
     * @param map2 The second TF-IDF vector represented as a map of token to weight
     * @return A float value between 0 and 1 indicating the cosine similarity
     */
    private float cosineSimilarity(Map<String, Float> map1, Map<String, Float> map2) {
        float dot = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;

        // Create a union of all tokens from both maps
        Set<String> allTokens = new HashSet<>();
        allTokens.addAll(map1.keySet());
        allTokens.addAll(map2.keySet());

        // Compute dot product and norms
        for (String token : allTokens) {
            float v1 = map1.getOrDefault(token, 0.0f);
            float v2 = map2.getOrDefault(token, 0.0f);
            dot += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        // Compute square roots of norms
        norm1 = (float) Math.sqrt(norm1);
        norm2 = (float) Math.sqrt(norm2);

        // Avoid division by zero
        if (norm1 == 0 || norm2 == 0) {
            return 0.0f;
        }

        // Return cosine similarity
        return dot / (norm1 * norm2);
    }

    /**
     * Calculates the semantic similarity between two incident descriptions using Word2Vec embeddings.
     *
     * This method preprocesses both descriptions, filters out tokens not present in the Word2Vec vocabulary,
     * generates average vector embeddings, and then computes cosine similarity between the vectors.
     *
     * @param description1 The first incident description
     * @param description2 The second incident description
     * @return A float between 0 and 1 representing the Word2Vec-based similarity score
     */
    private float calculateWord2VecSimilarity(String description1, String description2) {
        // Step 1: Preprocess both descriptions (e.g., lowercasing, stopword removal, synonym expansion)
        List<String> tokens1 = preprocessText(description1);
        List<String> tokens2 = preprocessText(description2);

        // Step 2: Filter tokens to only include those present in the Word2Vec model's vocabulary
        tokens1 = filterTokensInVocab(tokens1);
        tokens2 = filterTokensInVocab(tokens2);

        // Step 3: If either token list is empty, return 0 similarity
        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0f;
        }

        // Step 4: Generate average vector representations of both descriptions
        float[] vector1 = word2VecModel.getWordVectorsMean(tokens1).toFloatVector();
        float[] vector2 = word2VecModel.getWordVectorsMean(tokens2).toFloatVector();

        // Step 5: Calculate cosine similarity between the two vectors
        return arrayCosineSimilarity(vector1, vector2);
    }


    /**
     * Filters the input token list to only include tokens that exist
     * in the loaded Word2Vec model's vocabulary.
     *
     * @param tokens List of preprocessed text tokens
     * @return List of tokens that are found in the Word2Vec vocabulary
     */
    private List<String> filterTokensInVocab(List<String> tokens) {
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            // Check if the current token exists in the Word2Vec vocabulary
            if (word2VecModel.hasWord(token)) {
                result.add(token); // Keep token if it exists in the model
            }
        }
        return result;
    }

    /**
     * Calculates the cosine similarity between two float vectors.
     * Used for comparing average Word2Vec vectors generated from tokenized incident descriptions.
     *
     * @param vectorA First float vector.
     * @param vectorB Second float vector.
     * @return Cosine similarity score in the range [0.0, 1.0], or 0.0 if either vector has zero magnitude.
     */
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

    /**
     * A predefined domain-specific synonym map used to improve semantic similarity.
     * These synonyms are injected during preprocessing to ensure different phrasings
     * for the same concept are matched more accurately in TF-IDF and Word2Vec comparisons.
     *
     * Categories:
     * - General system terminology (e.g., login, error, update)
     * - IT-specific terms (e.g., network, software, security)
     * - HR-related language (e.g., payroll, employee, benefit)
     * - Support/ticketing terms (e.g., troubleshoot, resolution, ticket)
     * - Email/infrastructure variations (e.g., email, SMTP, wifi)
     */
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
        synonyms.put("payslip", Arrays.asList("salaryslip", "paymentstatement", "wageslip", "remuneration"));
        synonyms.put("salary", Arrays.asList("wage", "pay", "income", "compensation"));
        synonyms.put("payment", Arrays.asList("pay", "remittance", "salary", "deposit"));
        synonyms.put("statement", Arrays.asList("document", "record", "summary"));
        synonyms.put("issue", Arrays.asList("problem", "error", "difficulty", "fault"));
        synonyms.put("receive", Arrays.asList("get", "obtain", "collect"));
        synonyms.put("missing", Arrays.asList("notreceived", "delayed", "unavailable", "absent"));
        synonyms.put("march", Arrays.asList("thismonth", "lastmonth")); // For temporal fuzziness
        synonyms.put("slip", Arrays.asList("document", "form", "note"));

        // Support-related synonyms
        synonyms.put("support", Arrays.asList("help", "assistance", "service"));
        synonyms.put("ticket", Arrays.asList("issue", "request", "case"));
        synonyms.put("troubleshoot", Arrays.asList("diagnose", "debug"));
        synonyms.put("resolution", Arrays.asList("fix", "solution", "remedy"));
        synonyms.put("customer", Arrays.asList("client", "user"));
        synonyms.put("service", Arrays.asList("assistance", "support", "helpdesk"));

        synonyms.put("wifi", Arrays.asList("wi-fi", "network", "internet", "wireless"));
        synonyms.put("connect", Arrays.asList("access", "reach", "link"));
        synonyms.put("laptop", Arrays.asList("device", "machine", "computer"));
        synonyms.put("not", Arrays.asList("unable", "fail", "error"));

        synonyms.put("email", Arrays.asList("mail", "message", "communication"));
        synonyms.put("outbox", Arrays.asList("sent", "delivery"));
        synonyms.put("undeliverable", Arrays.asList("bounced", "failed"));
        synonyms.put("external", Arrays.asList("outside", "third-party", "client"));
        synonyms.put("smtp", Arrays.asList("mailserver", "emailserver"));
    }

    /**
     * Preprocesses a block of text for NLP similarity comparison.
     * - Lowercases all tokens
     * - Removes punctuation and stopwords
     * - Expands tokens using a predefined synonym dictionary
     *
     * Note: Bigrams and stemming are intentionally excluded for consistency with Word2Vec.
     *
     * @param text Raw input text (incident title or description)
     * @return List of cleaned and expanded tokens
     */
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
