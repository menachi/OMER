package com.yourname.harrypottermod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class ManaSystem {
    
    public static final int MAX_MANA = 100;
    public static final int MANA_REGEN_RATE = 2; // 2 מנא לשנייה
    
    // עלויות כישופים
    public static final int LUMOS_COST = 20;
    public static final int NOX_COST = 5;
    
    public static int getCurrentMana(ItemStack wand) {
        CompoundTag tag = wand.getOrCreateTag();
        return tag.getInt("current_mana");
    }
    
    public static void setCurrentMana(ItemStack wand, int mana) {
        CompoundTag tag = wand.getOrCreateTag();
        tag.putInt("current_mana", Math.max(0, Math.min(MAX_MANA, mana)));
    }
    
    public static long getLastManaUpdate(ItemStack wand) {
        CompoundTag tag = wand.getOrCreateTag();
        return tag.getLong("last_mana_update");
    }
    
    public static void setLastManaUpdate(ItemStack wand, long time) {
        CompoundTag tag = wand.getOrCreateTag();
        tag.putLong("last_mana_update", time);
    }
    
    public static void initializeMana(ItemStack wand, long gameTime) {
        CompoundTag tag = wand.getOrCreateTag();
        // תמיד מתחיל עם מנא מלא
        tag.putInt("current_mana", MAX_MANA);
        tag.putLong("last_mana_update", gameTime);
    }
    
    // פונקציה עם ברירת מחדל לתאימות לאחור
    public static void initializeMana(ItemStack wand) {
        CompoundTag tag = wand.getOrCreateTag();
        tag.putInt("current_mana", MAX_MANA);
        tag.putLong("last_mana_update", 0);
    }
    
    public static void updateManaRegen(ItemStack wand, long gameTime) {
        long lastUpdate = getLastManaUpdate(wand);
        
        // אם המנא כבר מלא, אין צורך לעדכן
        int currentMana = getCurrentMana(wand);
        if (currentMana >= MAX_MANA) {
            setLastManaUpdate(wand, gameTime);
            return;
        }
        
        // אם זה הפעם הראשון או lastUpdate הוא 0, אתחל מהזמן הנוכחי
        if (lastUpdate == 0) {
            setLastManaUpdate(wand, gameTime);
            return;
        }
        
        long timeDiff = gameTime - lastUpdate;
        
        // בדיקה אם עברה לפחות שנייה
        if (timeDiff >= 20) { // שנייה עברה (20 ticks)
            // מעדכנים רק שנייה אחת בכל פעם למנוע קפיצות
            int manaToAdd = Math.min((int)(timeDiff / 20), 5); // מקסימום 5 מנא בכל עדכון
            int newMana = Math.min(MAX_MANA, currentMana + manaToAdd);
            
            setCurrentMana(wand, newMana);
            setLastManaUpdate(wand, gameTime);
        }
    }
    
    public static boolean consumeMana(ItemStack wand, int amount, long gameTime) {
        updateManaRegen(wand, gameTime);
        int currentMana = getCurrentMana(wand);
        
        if (currentMana >= amount) {
            setCurrentMana(wand, currentMana - amount);
            return true;
        }
        return false;
    }
    
    // הסרנו את השמלה - כל השחקנים צריכים מנא
    public static boolean hasManaCloak(Player player) {
        return false; // תמיד false - אין שמלה
    }
    
    public static String getManaBar(ItemStack wand) {
        int currentMana = getCurrentMana(wand);
        int bars = (currentMana * 10) / MAX_MANA;
        
        StringBuilder manaBar = new StringBuilder("§5§lMana: §9§l["); // סגול + כחול
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                manaBar.append("§d§l█"); // בלוק מלא סגול בהיר
            } else {
                manaBar.append("§8▒"); // בלוק ריק אפור
            }
        }
        manaBar.append("§9§l] §d§l").append(currentMana).append("§7/§d").append(MAX_MANA);
        
        return manaBar.toString();
    }
    
    public static String getManaBarSimple(ItemStack wand) {
        int currentMana = getCurrentMana(wand);
        int bars = (currentMana * 20) / MAX_MANA;
        
        StringBuilder manaBar = new StringBuilder("§5§l["); // סגול
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                manaBar.append("§d§l|"); // סגול בהיר
            } else {
                manaBar.append("§8§l|"); // אפור כהה
            }
        }
        manaBar.append("§5§l]");
        
        return manaBar.toString();
    }
}