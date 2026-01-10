package com.yourname.harrypottermod;

import com.yourname.harrypottermod.client.ManaHudRenderer;
import com.yourname.harrypottermod.client.SpellSelection;
import com.yourname.harrypottermod.items.WandItem;
import com.yourname.harrypottermod.spells.SpellManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = HarryPotterMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    // מקשי בחירת לחשים נוחים יותר
    public static final KeyMapping LUMOS_SPELL = new KeyMapping("key.harrypottermod.lumos", GLFW.GLFW_KEY_Z, "key.categories.harrypottermod");
    public static final KeyMapping SPELL_2 = new KeyMapping("key.harrypottermod.spell2", GLFW.GLFW_KEY_C, "key.categories.harrypottermod");
    public static final KeyMapping SPELL_3 = new KeyMapping("key.harrypottermod.spell3", GLFW.GLFW_KEY_V, "key.categories.harrypottermod");
    public static final KeyMapping SPELL_4 = new KeyMapping("key.harrypottermod.spell4", GLFW.GLFW_KEY_B, "key.categories.harrypottermod");
    public static final KeyMapping SPELL_5 = new KeyMapping("key.harrypottermod.spell5", GLFW.GLFW_KEY_N, "key.categories.harrypottermod");
    public static final KeyMapping NEXT_SPELL = new KeyMapping("key.harrypottermod.next_spell", GLFW.GLFW_KEY_X, "key.categories.harrypottermod");

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        // רישום ה-HUD לתצוגת המנא
        MinecraftForge.EVENT_BUS.register(ManaHudRenderer.class);
        MinecraftForge.EVENT_BUS.register(KeyHandler.class);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        // רישום מקשים נוחים יותר
        event.register(LUMOS_SPELL);
        event.register(SPELL_2);
        event.register(SPELL_3);
        event.register(SPELL_4);
        event.register(SPELL_5);
        event.register(NEXT_SPELL);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register renderers here
    }
    
    // מחלקה נפרדת לטיפול במקשים
    public static class KeyHandler {
        private static long lastKeyTime = 0;
        private static int lastKey = -1;
        
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return; // לא לעבוד כשיש מסך פתוח
            
            // רק על לחיצה (לא על שחרור)
            if (event.getAction() == GLFW.GLFW_PRESS) {
                int key = event.getKey();
                long currentTime = System.currentTimeMillis();
                
                // מניעת לחיצות חוזרות על אותו מקש
                if (key == lastKey && (currentTime - lastKeyTime) < 500) {
                    return; // מקש זה נלחץ לפני פחות מחצי שנייה
                }
                
                lastKey = key;
                lastKeyTime = currentTime;
                
                // בדיקת המקש שנלחץ
                if (key == GLFW.GLFW_KEY_C) {
                    selectAndCastSpell(mc, 1); // כישוף 2
                } else if (key == GLFW.GLFW_KEY_V) {
                    selectAndCastSpell(mc, 2); // כישוף 3
                } else if (key == GLFW.GLFW_KEY_B) {
                    selectAndCastSpell(mc, 3); // כישוף 4
                } else if (key == GLFW.GLFW_KEY_N) {
                    selectAndCastSpell(mc, 4); // כישוף 5
                } else if (key == GLFW.GLFW_KEY_T) {
                    // הפעלת הכישוף הנבחר כרגע
                    int selectedIndex = SpellSelection.getSelectedSpell(mc.player.getUUID());
                    
                    // אם אין כישוף נבחר, בחר את הראשון (לומוס)
                    if (selectedIndex < 0 || SpellManager.getSpellByIndex(selectedIndex) == null) {
                        selectedIndex = 0; // לומוס
                        SpellSelection.setSelectedSpell(mc.player.getUUID(), selectedIndex);
                    }
                    
                    selectAndCastSpell(mc, selectedIndex);
                } else if (key == GLFW.GLFW_KEY_M) {
                    // מעבר לכישוף הבא
                    SpellSelection.nextSpell(mc.player.getUUID(), SpellManager.getSpellCount());
                    int selected = SpellSelection.getSelectedSpell(mc.player.getUUID());
                    if (SpellManager.getSpellByIndex(selected) != null) {
                        mc.player.sendSystemMessage(Component.literal("§6Selected: §e" + SpellManager.getSpellByIndex(selected).getName()));
                    }
                }
            }
        }
        
        private static void selectAndCastSpell(Minecraft mc, int spellIndex) {
            // בדיקה אם יש שרביט ב-inventory
            boolean hasWand = false;
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                if (mc.player.getInventory().getItem(i).getItem() instanceof WandItem) {
                    hasWand = true;
                    break;
                }
            }
            
            if (!hasWand) {
                mc.player.sendSystemMessage(Component.literal("§cYou need a wand to cast spells!"));
                return;
            }
            
            // בדיקה אם הכישוף קיים
            if (SpellManager.getSpellByIndex(spellIndex) == null) {
                mc.player.sendSystemMessage(Component.literal("§cSpell slot " + (spellIndex + 1) + " is empty!"));
                return;
            }
            
            // בחירת הכישוף והפעלתו
            SpellSelection.setSelectedSpell(mc.player.getUUID(), spellIndex);
            boolean success = SpellManager.castSpell(spellIndex, mc.level, mc.player);
            
            if (!success) {
                mc.player.sendSystemMessage(Component.literal("§cSpell failed! (Not enough mana?)"));
            }
        }
    }
}