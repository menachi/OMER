package com.yourname.harrypottermod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.*;

public class LearnedSpells {
    private static final Map<UUID, Set<String>> playerSpells = new HashMap<>();
    
    // בדיקה אם שחקן למד קסם מסוים
    public static boolean hasLearnedSpell(Player player, String spellName) {
        UUID playerId = player.getUUID();
        return playerSpells.getOrDefault(playerId, new HashSet<>()).contains(spellName);
    }
    
    // הוספת קסם נלמד לשחקן
    public static void learnSpell(Player player, String spellName) {
        UUID playerId = player.getUUID();
        playerSpells.computeIfAbsent(playerId, k -> new HashSet<>()).add(spellName);
        savePlayerSpells(player);
    }
    
    // קבלת כל הקסמים הנלמדים של שחקן
    public static Set<String> getLearnedSpells(Player player) {
        UUID playerId = player.getUUID();
        return new HashSet<>(playerSpells.getOrDefault(playerId, new HashSet<>()));
    }
    
    // טעינת קסמים מ-NBT
    public static void loadPlayerSpells(Player player) {
        CompoundTag playerData = player.getPersistentData();
        if (playerData.contains("learned_spells")) {
            ListTag spellsList = playerData.getList("learned_spells", 8); // 8 = STRING
            Set<String> spells = new HashSet<>();
            for (int i = 0; i < spellsList.size(); i++) {
                spells.add(spellsList.getString(i));
            }
            playerSpells.put(player.getUUID(), spells);
        }
    }
    
    // שמירת קסמים ל-NBT
    private static void savePlayerSpells(Player player) {
        CompoundTag playerData = player.getPersistentData();
        ListTag spellsList = new ListTag();
        Set<String> spells = playerSpells.get(player.getUUID());
        if (spells != null) {
            for (String spell : spells) {
                spellsList.add(StringTag.valueOf(spell));
            }
        }
        playerData.put("learned_spells", spellsList);
    }
    
    // איפוס כל הקסמים (לטסטים)
    public static void resetPlayerSpells(Player player) {
        UUID playerId = player.getUUID();
        playerSpells.remove(playerId);
        player.getPersistentData().remove("learned_spells");
    }
    
    // ספירת קסמים נלמדים
    public static int getLearnedSpellCount(Player player) {
        UUID playerId = player.getUUID();
        return playerSpells.getOrDefault(playerId, new HashSet<>()).size();
    }
}