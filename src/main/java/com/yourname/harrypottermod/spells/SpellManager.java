package com.yourname.harrypottermod.spells;

import com.yourname.harrypottermod.spells.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

public class SpellManager {
    private static final List<Spell> SPELLS = new ArrayList<>();
    
    static {
        SPELLS.add(new LumosSpell());
    }
    
    public static List<Spell> getAllSpells() {
        return SPELLS;
    }
    
    public static Spell getSpellByIndex(int index) {
        if (index >= 0 && index < SPELLS.size()) {
            return SPELLS.get(index);
        }
        return null;
    }
    
    public static boolean castSpell(int spellIndex, Level level, Player player) {
        Spell spell = getSpellByIndex(spellIndex);
        if (spell != null) {
            // מוצא את השרביט ביד הראשית
            net.minecraft.world.item.ItemStack wandStack = player.getMainHandItem();
            if (!wandStack.isEmpty()) {
                // מעביר את השרביט לקסם לבדיקת מנא
                return spell.castWithWand(level, player, wandStack);
            }
        }
        return false;
    }
    
    public static int getSpellCount() {
        return SPELLS.size();
    }
}