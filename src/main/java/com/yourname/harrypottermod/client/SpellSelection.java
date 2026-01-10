package com.yourname.harrypottermod.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpellSelection {
    private static final Map<UUID, Integer> playerSelectedSpells = new HashMap<>();
    
    public static void setSelectedSpell(UUID playerId, int spellIndex) {
        playerSelectedSpells.put(playerId, spellIndex);
    }
    
    public static int getSelectedSpell(UUID playerId) {
        return playerSelectedSpells.getOrDefault(playerId, 0); // לחש ראשון כברירת מחדל
    }
    
    public static void nextSpell(UUID playerId, int maxSpells) {
        int current = getSelectedSpell(playerId);
        setSelectedSpell(playerId, (current + 1) % maxSpells);
    }
    
    public static void previousSpell(UUID playerId, int maxSpells) {
        int current = getSelectedSpell(playerId);
        setSelectedSpell(playerId, (current - 1 + maxSpells) % maxSpells);
    }
}