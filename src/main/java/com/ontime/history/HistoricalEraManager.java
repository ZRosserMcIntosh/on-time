package com.ontime.history;

import java.util.*;

/**
 * Manages the 12 historical eras and maps them to chapters and cognitive principles.
 *
 * Era ↔ Principle Mapping (based on Virgil's 12 Cognitive Principles):
 *
 *  1. Ancient Mesopotamia (3500–2000 BCE)  → First Principles Thinking
 *     "Where did truth begin? Strip away assumptions at the dawn of civilisation."
 *
 *  2. Classical Athens (500–300 BCE)        → Correlation vs Causation
 *     "The birthplace of logic — but even Athenians confused coincidence for cause."
 *
 *  3. Roman Republic (509–27 BCE)           → Incentives Drive Behavior
 *     "Power, land, glory — watch how incentives shaped an empire."
 *
 *  4. Early Christianity (30–300 CE)        → Steelman Principle
 *     "To understand a belief, you must argue it better than its believers."
 *
 *  5. Tang Dynasty China (618–907 CE)       → Opportunity Cost
 *     "Every road taken is a road not taken. The Silk Road taught this."
 *
 *  6. Medieval Europe (1000–1300 CE)        → Long-Term vs Short-Term Thinking
 *     "Cathedrals built over centuries. Crusades launched in a moment."
 *
 *  7. Renaissance Italy (1400–1550)         → Emotional Regulation
 *     "Art born from passion — but mastered through discipline."
 *
 *  8. Age of Exploration (1500–1700)        → Bayesian Updating
 *     "Every new shore rewrites the map. Update your beliefs."
 *
 *  9. French Revolution (1789–1799)         → Moral Tradeoff Awareness
 *     "Liberty, equality, fraternity — but at what cost?"
 *
 * 10. Industrial Revolution (1760–1840)     → Tragedy of the Commons
 *     "Shared rivers poisoned. Shared skies blackened. Who is responsible?"
 *
 * 11. World War II Era (1939–1945)          → Second-Order Effects
 *     "Every action echoes. The war that rewrote the world."
 *
 * 12. Digital Age (1990–Present)            → The Danger of Certainty
 *     "Information everywhere, wisdom scarce. Are you sure you're sure?"
 */
public class HistoricalEraManager {

    private final Map<String, HistoricalEra> eras = new LinkedHashMap<>();
    private final Map<String, String> chapterToEra = new HashMap<>();

    public HistoricalEraManager() {
        registerEras();
        mapChaptersToEras();
    }

    private void registerEras() {
        register(new HistoricalEra(
            "mesopotamia", "Ancient Mesopotamia", "3500–2000 BCE",
            "first_principles",
            "Where did truth begin? Strip away assumptions at the dawn of civilisation.",
            0.72f, 0.58f, 0.36f  // warm sand
        ));
        register(new HistoricalEra(
            "athens", "Classical Athens", "500–300 BCE",
            "causal_reasoning",
            "The birthplace of logic — but even Athenians confused coincidence for cause.",
            0.55f, 0.65f, 0.75f  // marble blue-grey
        ));
        register(new HistoricalEra(
            "rome", "Roman Republic", "509–27 BCE",
            "incentives",
            "Power, land, glory — watch how incentives shaped an empire.",
            0.70f, 0.30f, 0.25f  // imperial red
        ));
        register(new HistoricalEra(
            "early_christian", "Early Christianity", "30–300 CE",
            "steelman",
            "To understand a belief, you must argue it better than its believers.",
            0.60f, 0.55f, 0.40f  // parchment gold
        ));
        register(new HistoricalEra(
            "tang_china", "Tang Dynasty China", "618–907 CE",
            "opportunity_cost",
            "Every road taken is a road not taken. The Silk Road taught this.",
            0.80f, 0.25f, 0.15f  // vermilion
        ));
        register(new HistoricalEra(
            "medieval", "Medieval Europe", "1000–1300 CE",
            "long_term",
            "Cathedrals built over centuries. Crusades launched in a moment.",
            0.35f, 0.35f, 0.40f  // stone grey
        ));
        register(new HistoricalEra(
            "renaissance", "Renaissance Italy", "1400–1550",
            "emotional_regulation",
            "Art born from passion — but mastered through discipline.",
            0.65f, 0.50f, 0.35f  // Florentine ochre
        ));
        register(new HistoricalEra(
            "exploration", "Age of Exploration", "1500–1700",
            "bayesian_updating",
            "Every new shore rewrites the map. Update your beliefs.",
            0.20f, 0.45f, 0.60f  // ocean blue
        ));
        register(new HistoricalEra(
            "french_revolution", "French Revolution", "1789–1799",
            "moral_tradeoff",
            "Liberty, equality, fraternity — but at what cost?",
            0.25f, 0.25f, 0.55f  // tricolore blue
        ));
        register(new HistoricalEra(
            "industrial", "Industrial Revolution", "1760–1840",
            "tragedy_of_commons",
            "Shared rivers poisoned. Shared skies blackened. Who is responsible?",
            0.40f, 0.40f, 0.38f  // soot grey
        ));
        register(new HistoricalEra(
            "ww2", "World War II Era", "1939–1945",
            "second_order",
            "Every action echoes. The war that rewrote the world.",
            0.30f, 0.35f, 0.30f  // olive drab
        ));
        register(new HistoricalEra(
            "digital", "Digital Age", "1990–Present",
            "danger_of_certainty",
            "Information everywhere, wisdom scarce. Are you sure you're sure?",
            0.15f, 0.55f, 0.70f  // digital cyan
        ));
    }

    /**
     * Maps chapter IDs (from chapter JSON) to their associated era.
     * Chapters without an explicit mapping are considered "present day".
     */
    private void mapChaptersToEras() {
        // Arc I — The Present & Origins
        chapterToEra.put("prologue",    null);              // present day
        chapterToEra.put("chapter_1",   null);              // present day
        chapterToEra.put("chapter_2",   "mesopotamia");
        chapterToEra.put("chapter_3",   "mesopotamia");
        chapterToEra.put("chapter_4",   "athens");
        chapterToEra.put("chapter_5",   "athens");

        // Arc II — Power & Belief
        chapterToEra.put("chapter_6",   "rome");
        chapterToEra.put("chapter_7",   "rome");
        chapterToEra.put("chapter_8",   "early_christian");
        chapterToEra.put("chapter_9",   "early_christian");
        chapterToEra.put("chapter_10",  "tang_china");

        // Arc III — Building & Breaking
        chapterToEra.put("chapter_11",  "tang_china");
        chapterToEra.put("chapter_12",  "medieval");
        chapterToEra.put("chapter_13",  "medieval");
        chapterToEra.put("chapter_14",  "renaissance");
        chapterToEra.put("chapter_15",  "renaissance");

        // Arc IV — Revolution & Consequence
        chapterToEra.put("chapter_16",  "exploration");
        chapterToEra.put("chapter_17",  "exploration");
        chapterToEra.put("chapter_18",  "french_revolution");
        chapterToEra.put("chapter_19",  "french_revolution");
        chapterToEra.put("chapter_20",  "industrial");

        // Arc V — Modern World & Synthesis
        chapterToEra.put("chapter_21",  "industrial");
        chapterToEra.put("chapter_22",  "ww2");
        chapterToEra.put("chapter_23",  "ww2");
        chapterToEra.put("chapter_24",  "digital");
        chapterToEra.put("chapter_25",  "digital");

        // Later chapters cycle back through eras with deeper engagement
        // (chapters 26–100 will be mapped as content is authored)
    }

    // ── Public API ───────────────────────────────────────────────

    public void register(HistoricalEra era) {
        eras.put(era.getId(), era);
    }

    /**
     * Returns the era associated with a chapter, or null if the chapter
     * takes place in the "present day" (no era overlay).
     */
    public HistoricalEra getEraForChapter(String chapterId) {
        String eraId = chapterToEra.get(chapterId);
        if (eraId == null) return null;
        return eras.get(eraId);
    }

    /**
     * Gets an era by its ID.
     */
    public HistoricalEra getEra(String eraId) {
        return eras.get(eraId);
    }

    /**
     * Returns all 12 eras in chronological order.
     */
    public Collection<HistoricalEra> getAllEras() {
        return eras.values();
    }

    /**
     * Maps a chapter to an era (for dynamically authored content).
     */
    public void mapChapter(String chapterId, String eraId) {
        chapterToEra.put(chapterId, eraId);
    }
}
