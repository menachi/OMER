package com.yourname.harrypottermod.client;

import com.yourname.harrypottermod.LearnedSpells;
import com.yourname.harrypottermod.client.SpellSelection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.vertex.PoseStack;

public class SpellBookScreen extends Screen {
    private final String spellName;
    private final String spellDisplayName;
    private final ItemStack spellBook;
    private EditBox spellInput;
    private Button submitButton;
    private String feedback = "";
    private int attempts = 3;
    private boolean gameCompleted = false;
    
    public SpellBookScreen(String spellName, String spellDisplayName, ItemStack spellBook) {
        super(Component.literal("Learning " + spellDisplayName));
        this.spellName = spellName;
        this.spellDisplayName = spellDisplayName;
        this.spellBook = spellBook;
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // תיבת הקלדה לקסם
        spellInput = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20, Component.literal("Enter spell"));
        spellInput.setMaxLength(20);
        this.addRenderableWidget(spellInput);
        
        // כפתור הגשה
        submitButton = new Button(centerX - 50, centerY + 20, 100, 20, Component.literal("Cast Spell"), (button) -> {
            checkSpelling();
        });
        this.addRenderableWidget(submitButton);
        
        // כפתור סגירה
        this.addRenderableWidget(new Button(centerX - 50, centerY + 50, 100, 20, Component.literal("Close"), (button) -> {
            this.onClose();
        }));
    }
    
    private void checkSpelling() {
        String input = spellInput.getValue().toLowerCase().trim();
        String correctSpell = spellName.toLowerCase();
        
        if (input.equals(correctSpell)) {
            // הצלחה!
            gameCompleted = true;
            LearnedSpells.learnSpell(minecraft.player, spellName);
            SpellSelection.initializeFirstSpell(minecraft.player.getUUID()); // מתחיל את הבחירה בקסם הראשון
            feedback = "§aSuccess! You learned " + spellDisplayName + "!";
            minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
            
            // הסר את הספר מהאינוונטרי
            if (spellBook != null) {
                spellBook.shrink(1);
            }
            
            // סגור את המסך אחרי 2 שניות
            minecraft.tell(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) {}
                if (minecraft.screen == this) {
                    this.onClose();
                }
            });
            
        } else {
            // כישלון
            attempts--;
            minecraft.player.playSound(SoundEvents.ITEM_BREAK, 1.0F, 0.5F);
            
            if (attempts > 0) {
                feedback = "§cIncorrect! Try again. Attempts left: " + attempts;
                spellInput.setValue("");
            } else {
                feedback = "§cFailed! The spell book crumbles to dust...";
                spellInput.setEditable(false);
                submitButton.active = false;
                
                // הסר את הספר מהאינוונטרי
                if (spellBook != null) {
                    spellBook.shrink(1);
                }
            }
        }
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // רקע כהה
        this.renderBackground(poseStack);
        
        // כותרת
        drawCenteredString(poseStack, this.font, "Learning " + spellDisplayName, this.width / 2, 50, 0xFFFFFF);
        
        // הוראות
        drawCenteredString(poseStack, this.font, "Type the spell name to learn it:", this.width / 2, 100, 0xCCCCCC);
        drawCenteredString(poseStack, this.font, "Spell: " + spellDisplayName, this.width / 2, 120, 0xFFD700);
        
        // מספר ניסיונות
        drawCenteredString(poseStack, this.font, "Attempts: " + attempts, this.width / 2, 140, 0xFFFFFF);
        
        // משוב
        if (!feedback.isEmpty()) {
            drawCenteredString(poseStack, this.font, feedback, this.width / 2, this.height / 2 + 80, 0xFFFFFF);
        }
        
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}