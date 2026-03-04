package com.therushlight.characters;

import java.util.*;

/**
 * Registry of all characters in the story.
 */
public class CharacterManager {

    private final Map<String, Character> characters = new LinkedHashMap<>();

    public CharacterManager() {
        registerCoreCharacters();
    }

    private void registerCoreCharacters() {
        // The four siblings
        Character drew = new Character("drew", "Drew");
        drew.setColor(0.5f, 0.35f, 0.25f);
        register(drew);

        Character rush = new Character("rush", "Rush");
        rush.setColor(0.3f, 0.4f, 0.5f);
        register(rush);

        Character lu = new Character("lu", "Luísa");
        lu.setColor(0.5f, 0.3f, 0.45f);
        register(lu);

        Character yen = new Character("yen", "Yenevieve");
        yen.setColor(0.45f, 0.45f, 0.3f);
        register(yen);

        // Kimiru
        Character kimiru = new Character("kimiru", "Kimiru");
        kimiru.setColor(0.1f, 0.1f, 0.12f); // Near black, like his fur
        register(kimiru);
    }

    public void register(Character character) {
        characters.put(character.getId(), character);
    }

    public Character get(String id) {
        return characters.get(id);
    }

    public Collection<Character> getAll() {
        return characters.values();
    }
}
