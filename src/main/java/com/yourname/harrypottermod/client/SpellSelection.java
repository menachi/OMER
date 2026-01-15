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
        return playerSelectedSpells.getOrDefault(playerId, -1); // אין קסם נבחר כברירת מחדל
    }
    
    public static void nextSpell(UUID playerId, int maxSpells) {
        int current = getSelectedSpell(playerId);
        if (current == -1 && maxSpells > 0) {
            // אם אין קסם נבחר אבל יש קסמים זמינים, התחל מהקסם הראשון
            setSelectedSpell(playerId, 0);
        } else if (maxSpells > 0) {
            setSelectedSpell(playerId, (current + 1) % maxSpells);
        }
    }
    
    public static void previousSpell(UUID playerId, int maxSpells) {
        int current = getSelectedSpell(playerId);
        if (current == -1 && maxSpells > 0) {
            // אם אין קסם נבחר אבל יש קסמים זמינים, התחל מהקסם האחרון
            setSelectedSpell(playerId, maxSpells - 1);
        } else if (maxSpells > 0) {
            setSelectedSpell(playerId, (current - 1 + maxSpells) % maxSpells);
        }
    }
    
    public static void initializeFirstSpell(UUID playerId) {
        // מתחיל את הבחירה ב-0 כשלומדים את הקסם הראשון
        if (getSelectedSpell(playerId) == -1) {
            setSelectedSpell(playerId, 0);
        }
    }
}