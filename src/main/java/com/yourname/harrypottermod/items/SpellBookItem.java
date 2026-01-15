package com.yourname.harrypottermod.items;

import com.yourname.harrypottermod.LearnedSpells;
import com.yourname.harrypottermod.client.SpellBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.ChatFormatting;
import java.util.List;
import javax.annotation.Nullable;

public class SpellBookItem extends Item {
    private final String spellName;
    private final String spellDisplayName;
    
    public SpellBookItem(Properties properties, String spellName, String spellDisplayName) {
        super(properties);
        this.spellName = spellName;
        this.spellDisplayName = spellDisplayName;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide()) {
            // בדיקה אם השחקן כבר למד את הקסם
            if (LearnedSpells.hasLearnedSpell(player, spellName)) {
                player.sendSystemMessage(Component.literal("You already know this spell!").withStyle(ChatFormatting.YELLOW));
                return InteractionResultHolder.pass(stack);
            }
            
            // פתח את מסך המיני-גיים
            Minecraft.getInstance().setScreen(new SpellBookScreen(spellName, spellDisplayName, stack));
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    
    public String getSpellName() {
        return spellName;
    }
    
    public String getSpellDisplayName() {
        return spellDisplayName;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Spell Book: " + spellDisplayName).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Right-click to learn this spell").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Complete the mini-game to master it!").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}