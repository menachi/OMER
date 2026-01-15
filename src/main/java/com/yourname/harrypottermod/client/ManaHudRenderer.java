package com.yourname.harrypottermod.client;

import com.yourname.harrypottermod.ManaSystem;
import com.yourname.harrypottermod.init.ItemInit;
import com.yourname.harrypottermod.spells.SpellManager;
import com.yourname.harrypottermod.spells.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ManaHudRenderer {
    
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player == null) return;
        
        // בודק אם יש שרביט באינוונטרי כולו (לא רק ביד)
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack wandStack = null;
        
        // בודק תחילה ביד הראשית ואז ביד השנייה
        if (mainHand.getItem() == ItemInit.WAND.get()) {
            wandStack = mainHand;
        } else if (offHand.getItem() == ItemInit.WAND.get()) {
            wandStack = offHand;
        } else {
            // בודק באינוונטרי הרגיל (36 משבצות)
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() == ItemInit.WAND.get()) {
                    wandStack = stack;
                    break; // לוקח את השרביט הראשון שמוצא
                }
            }
        }
        
        if (wandStack != null) {
            // אתחול ועדכון מנא
            if (!wandStack.hasTag() || !wandStack.getTag().contains("current_mana")) {
                ManaSystem.initializeMana(wandStack, mc.level.getGameTime());
            } else {
                ManaSystem.updateManaRegen(wandStack, mc.level.getGameTime());
            }
            
            // קבלת מידע על המנא
            int currentMana = ManaSystem.getCurrentMana(wandStack);
            int maxMana = ManaSystem.MAX_MANA;
            
            // הגדרות מיקום על המסך - מעל מד האוכל
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            
            // מיקום מעל מד האוכל (בצד ימין)
            int left = screenWidth / 2 + 10; // אותו מיקום של מד האוכל
            int top = screenHeight - 50; // פיקסל אחד מעל מד האוכל
            
            // גודל המלבן - כמו מד האוכל
            int barWidth = 81; // אורך של 10 אייקונים (8*10 + 1)
            int barHeight = 9; // גובה של אייקון אחד
            
            // חישוב כמה מהמלבן מלא
            int filledWidth = (currentMana * barWidth) / maxMana;
            
            // רקע אפור כהה (ריק)
            GuiComponent.fill(event.getPoseStack(), left, top, left + barWidth, top + barHeight, 0xFF222222);
            
            // מנא מלא - צבע כחול-סגול
            if (filledWidth > 0) {
                GuiComponent.fill(event.getPoseStack(), left, top, left + filledWidth, top + barHeight, 0xFF9966FF);
                
                // ברק עליון
                GuiComponent.fill(event.getPoseStack(), left, top, left + filledWidth, top + 3, 0xFFCC99FF);
            }
            
            // גבול שחור סביב כל המלבן
            GuiComponent.fill(event.getPoseStack(), left - 1, top - 1, left + barWidth + 1, top, 0xFF000000); // למעלה
            GuiComponent.fill(event.getPoseStack(), left - 1, top + barHeight, left + barWidth + 1, top + barHeight + 1, 0xFF000000); // למטה  
            GuiComponent.fill(event.getPoseStack(), left - 1, top - 1, left, top + barHeight + 1, 0xFF000000); // שמאל
            GuiComponent.fill(event.getPoseStack(), left + barWidth, top - 1, left + barWidth + 1, top + barHeight + 1, 0xFF000000); // ימין
            
            // טקסט שמציג את כמות המנא
            String manaText = currentMana + "/" + maxMana;
            int textWidth = mc.font.width(manaText);
            int textX = left + (barWidth - textWidth) / 2; // מרכז הטקסט במלבן
            int textY = top + 1; // מעט מתחת לחלק העליון
            
            // ציור הטקסט בלבן
            mc.font.draw(event.getPoseStack(), manaText, textX, textY, 0xFFFFFFFF);
            
            // מד כישופים זמינים - בצד שמאל מעל מד הבריאות
            renderSpellIndicators(event, mc, player, screenWidth, screenHeight);
            
            // הצגת הלחש הנבחר
            renderSelectedSpell(event, mc, player, screenWidth, screenHeight);
        }
    }
    
    private static void renderSelectedSpell(RenderGuiOverlayEvent.Post event, Minecraft mc, Player player, int screenWidth, int screenHeight) {
        // קבלת קסמים נלמדים בלבד
        List<Spell> learnedSpells = SpellManager.getLearnedSpells(player);
        if (learnedSpells.isEmpty()) {
            return; // אין קסמים נלמדים - לא מציג כלום
        }
        
        int selectedIndex = SpellSelection.getSelectedSpell(player.getUUID());
        if (selectedIndex >= 0 && selectedIndex < learnedSpells.size()) {
            Spell selectedSpell = learnedSpells.get(selectedIndex);
            String spellText = "Current: " + selectedSpell.getName();
            int textWidth = mc.font.width(spellText);
            
            // מיקום במרכז-למטה של המסך
            int textX = (screenWidth - textWidth) / 2;
            int textY = screenHeight - 70; // מעל מדי הבריאות והמנא
            
            // רקע כהה מאחורי הטקסט
            GuiComponent.fill(event.getPoseStack(), textX - 5, textY - 2, textX + textWidth + 5, textY + 10, 0x88000000);
            
            // הטקסט בצבע הלחש
            int color = getSpellColorForText(selectedSpell.getName());
            mc.font.draw(event.getPoseStack(), spellText, textX, textY, color);
        }
    }
    
    private static int getSpellColorForText(String spellName) {
        switch (spellName) {
            case "Lumos":
                return 0xFFFFDD00; // צהוב זהב
            case "Nox":
                return 0xFFAA66AA; // סגול בהיר
            case "Expelliarmus":
                return 0xFFFF6666; // אדום בהיר
            default:
                return 0xFFFFFFFF; // לבן
        }
    }
    
    private static void renderSpellIndicators(RenderGuiOverlayEvent.Post event, Minecraft mc, Player player, int screenWidth, int screenHeight) {
        // קבלת רק קסמים נלמדים
        List<Spell> learnedSpells = SpellManager.getLearnedSpells(player);
        int maxSpells = 5; // תמיד 5 ריבועים
        
        // מיקום בצד ימין תחתון
        int startX = screenWidth - 110; // 110 פיקסלים מהקצה הימני
        int startY = screenHeight - 30; // 30 פיקסלים מהתחתית
        
        // איקון לכל כישוף - 5 משבצות
        for (int i = 0; i < maxSpells; i++) {
            int iconX = startX + (i * 20); // 20 פיקסלים בין איקונים
            int iconY = startY;
            int iconSize = 18; // גודל איקון
            
            if (i < learnedSpells.size()) {
                // יש כישוף נלמד במיקום הזה
                Spell spell = learnedSpells.get(i);
                
                // בדיקה אם זה הלחש הנבחר
                int selectedIndex = SpellSelection.getSelectedSpell(player.getUUID());
                boolean isSelected = (i == selectedIndex);
                
                // צבע רקע לפי כישוף
                int backgroundColor = getSpellColor(spell.getName());
                int borderColor = isSelected ? 0xFFFFFFFF : 0xFF000000; // גבול לבן אם נבחר
                
                // ציור האיקון
                GuiComponent.fill(event.getPoseStack(), iconX, iconY, iconX + iconSize, iconY + iconSize, backgroundColor);
                
                // גבול
                GuiComponent.fill(event.getPoseStack(), iconX - 1, iconY - 1, iconX + iconSize + 1, iconY, borderColor); // למעלה
                GuiComponent.fill(event.getPoseStack(), iconX - 1, iconY + iconSize, iconX + iconSize + 1, iconY + iconSize + 1, borderColor); // למטה
                GuiComponent.fill(event.getPoseStack(), iconX - 1, iconY - 1, iconX, iconY + iconSize + 1, borderColor); // שמאל
                GuiComponent.fill(event.getPoseStack(), iconX + iconSize, iconY - 1, iconX + iconSize + 1, iconY + iconSize + 1, borderColor); // ימין
                
                // סמל הכישוף במרכז האיקון
                String symbol = getSpellSymbol(spell.getName());
                int symbolWidth = mc.font.width(symbol);
                int symbolX = iconX + (iconSize - symbolWidth) / 2;
                int symbolY = iconY + 5;
                mc.font.draw(event.getPoseStack(), symbol, symbolX, symbolY, 0xFFFFFFFF);
                
                // מספר הכישוף (1, 2, 3, 4, 5)
                String number = String.valueOf(i + 1);
                int numberX = iconX + 1;
                int numberY = iconY - 8;
                mc.font.draw(event.getPoseStack(), number, numberX, numberY, 0xFFFFFFFF);
            } else {
                // משבצת ריקה - אין כישוף נלמד במיקום הזה
                int backgroundColor = 0xFF333333; // אפור כהה
                int borderColor = 0xFF000000; // גבול שחור
                
                // ציור הריבוע הריק
                GuiComponent.fill(event.getPoseStack(), iconX, iconY, iconX + iconSize, iconY + iconSize, backgroundColor);
                
                // גבול שחור
                GuiComponent.fill(event.getPoseStack(), iconX - 1, iconY - 1, iconX + iconSize + 1, iconY, borderColor); // למעלה
                GuiComponent.fill(event.getPoseStack(), iconX - 1, iconY + iconSize, iconX + iconSize + 1, iconY + iconSize + 1, borderColor); // למטה
                GuiComponent.fill(event.getPoseStack(), iconX - 1, iconY - 1, iconX, iconY + iconSize + 1, borderColor); // שמאל
                GuiComponent.fill(event.getPoseStack(), iconX + iconSize, iconY - 1, iconX + iconSize + 1, iconY + iconSize + 1, borderColor); // ימין
                
                // מספר הכישוף הריק (2, 3, 4, 5)
                String number = String.valueOf(i + 1);
                int numberX = iconX + 1;
                int numberY = iconY - 8;
                mc.font.draw(event.getPoseStack(), number, numberX, numberY, 0xFF666666); // צבע אפור בהיר
                
                // סימן שאין כישוף
                String emptySymbol = "?";
                int symbolWidth = mc.font.width(emptySymbol);
                int symbolX = iconX + (iconSize - symbolWidth) / 2;
                int symbolY = iconY + 5;
                mc.font.draw(event.getPoseStack(), emptySymbol, symbolX, symbolY, 0xFF666666); // צבע אפור בהיר
            }
        }
    }
    
    private static int getSpellColor(String spellName) {
        switch (spellName) {
            case "Lumos":
                return 0xFFFFFF00; // צהוב זוהר
            case "Nox":
                return 0xFF800080; // סגול כהה
            case "Expelliarmus":
                return 0xFFFF4444; // אדום
            default:
                return 0xFF888888; // אפור
        }
    }
    
    private static String getSpellSymbol(String spellName) {
        switch (spellName) {
            case "Lumos":
                return "☀"; // סמל שמש
            case "Nox":
                return "☾"; // סמל ירח
            case "Expelliarmus":
                return "⚔"; // חרב
            default:
                return "?"; // סמל לא מוכר
        }
    }
}