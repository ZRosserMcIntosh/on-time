package com.ontime.i18n;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Manages localization for On Time.
 *
 * Supports two modes:
 * 1. UI strings — loaded from JSON resource bundles (locale/ui_en.json, locale/ui_pt_BR.json)
 * 2. Chapter dialogue — chapters can be loaded from locale-specific directories
 *    (chapters/pt-BR/chapter_1.json falls back to chapters/chapter_1.json)
 *
 * Default locale: en
 * Supported locales: en, pt-BR
 */
public class LocaleManager {

    public static final String ENGLISH = "en";
    public static final String PORTUGUESE_BR = "pt-BR";
    public static final String[] SUPPORTED_LOCALES = { ENGLISH, PORTUGUESE_BR };

    private static LocaleManager instance;

    private String currentLocale = ENGLISH;
    private Map<String, String> uiStrings = new LinkedHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private LocaleManager() {
        loadUIStrings();
    }

    public static LocaleManager getInstance() {
        if (instance == null) {
            instance = new LocaleManager();
        }
        return instance;
    }

    /**
     * Get current locale code (e.g. "en", "pt-BR").
     */
    public String getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Set locale and reload UI strings.
     */
    public void setLocale(String locale) {
        if (!isSupportedLocale(locale)) {
            System.err.println("Unsupported locale: " + locale + ". Falling back to English.");
            locale = ENGLISH;
        }
        this.currentLocale = locale;
        loadUIStrings();
        System.out.println("[i18n] Locale set to: " + currentLocale);
    }

    /**
     * Cycle to next locale. Useful for a language toggle button.
     */
    public void cycleLocale() {
        int idx = Arrays.asList(SUPPORTED_LOCALES).indexOf(currentLocale);
        int next = (idx + 1) % SUPPORTED_LOCALES.length;
        setLocale(SUPPORTED_LOCALES[next]);
    }

    /**
     * Get a UI string by key. Returns the key itself if not found.
     */
    public String get(String key) {
        return uiStrings.getOrDefault(key, key);
    }

    /**
     * Get a UI string with format arguments.
     * e.g. get("chapter_title", 1) → "Chapter 1"
     */
    public String get(String key, Object... args) {
        String template = uiStrings.getOrDefault(key, key);
        try {
            return String.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }

    /**
     * Get the resource path for a chapter JSON file, respecting locale.
     * Tries locale-specific path first, falls back to default.
     *
     * e.g. for pt-BR: /chapters/pt-BR/chapter_1.json → /chapters/chapter_1.json
     */
    public String getChapterResourcePath(String chapterId) {
        if (!ENGLISH.equals(currentLocale)) {
            String localePath = "/chapters/" + currentLocale + "/" + chapterId + ".json";
            if (getClass().getResourceAsStream(localePath) != null) {
                return localePath;
            }
        }
        return "/chapters/" + chapterId + ".json";
    }

    /**
     * Check if a locale-specific chapter exists.
     */
    public boolean hasLocalizedChapter(String chapterId) {
        if (ENGLISH.equals(currentLocale)) return true;
        String localePath = "/chapters/" + currentLocale + "/" + chapterId + ".json";
        return getClass().getResourceAsStream(localePath) != null;
    }

    /**
     * Get display name for current locale.
     */
    public String getLocaleDisplayName() {
        return switch (currentLocale) {
            case ENGLISH -> "English";
            case PORTUGUESE_BR -> "Português (Brasil)";
            default -> currentLocale;
        };
    }

    /**
     * Get flag/icon identifier for current locale.
     */
    public String getLocaleFlag() {
        return switch (currentLocale) {
            case ENGLISH -> "🇺🇸";
            case PORTUGUESE_BR -> "🇧🇷";
            default -> "🌐";
        };
    }

    public boolean isSupportedLocale(String locale) {
        for (String s : SUPPORTED_LOCALES) {
            if (s.equals(locale)) return true;
        }
        return false;
    }

    // --- Internal ---

    private void loadUIStrings() {
        String suffix = currentLocale.replace("-", "_");
        String path = "/locale/ui_" + suffix + ".json";

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[i18n] UI strings not found: " + path + ". Falling back to English.");
                if (!ENGLISH.equals(currentLocale)) {
                    path = "/locale/ui_en.json";
                    try (InputStream fallback = getClass().getResourceAsStream(path)) {
                        if (fallback != null) {
                            loadFromStream(fallback);
                        }
                    }
                }
                return;
            }
            loadFromStream(is);
        } catch (Exception e) {
            System.err.println("[i18n] Failed to load UI strings: " + e.getMessage());
        }
    }

    private void loadFromStream(InputStream is) throws IOException {
        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        uiStrings = gson.fromJson(json, type);
        System.out.println("[i18n] Loaded " + uiStrings.size() + " UI strings for locale: " + currentLocale);
    }
}
