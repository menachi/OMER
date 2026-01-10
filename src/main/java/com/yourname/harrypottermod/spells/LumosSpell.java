package com.yourname.harrypottermod.spells;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import com.yourname.harrypottermod.ManaSystem;

public class LumosSpell extends Spell {
    
    public LumosSpell() {
        super("Lumos", 10);
    }

    @Override
    public void cast(Level level, Player player) {
        if (!level.isClientSide()) {
            // מוצא את השרביט ביד הראשית
            ItemStack wandInHand = player.getMainHandItem();
            
            if (!wandInHand.isEmpty()) {
                // בדיקה אם השרביט כבר דולק
                boolean isGlowing = wandInHand.getOrCreateTag().getBoolean("glowing");
                
                if (isGlowing) {
                    // כיבוי הכישוף הפעיל
                    turnOffWand(level, player, wandInHand);
                } else {
                    // הפעלת כישוף חדש
                    // בדיקה אם יש מספיק מנא
                    if (!ManaSystem.consumeMana(wandInHand, ManaSystem.LUMOS_COST)) {
                        player.sendSystemMessage(Component.literal("§cNot enough mana! " + ManaSystem.getManaBar(wandInHand)));
                        return;
                    }
                    
                    // מוסיף NBT tag שיגרום לשרביט להאיר
                    wandInHand.getOrCreateTag().putBoolean("glowing", true);
                    wandInHand.getOrCreateTag().putLong("glowTime", level.getGameTime() + 600); // 30 שניות
                    
                    // שומר את המיקום הנוכחי של השחקן
                    BlockPos playerPos = player.blockPosition();
                    wandInHand.getOrCreateTag().putInt("lastX", playerPos.getX());
                    wandInHand.getOrCreateTag().putInt("lastY", playerPos.getY());
                    wandInHand.getOrCreateTag().putInt("lastZ", playerPos.getZ());
                    
                    player.sendSystemMessage(Component.literal("Lumos! Your wand glows with magical light!"));
                    
                    // הצגת מנא נוכחי תמיד
                    player.sendSystemMessage(Component.literal(ManaSystem.getManaBar(wandInHand)));
                }
            }
        }
    }
    
    private void turnOffWand(Level level, Player player, ItemStack wandInHand) {
        // כיבוי האור במיקום הנוכחי
        int lastX = wandInHand.getOrCreateTag().getInt("lastX");
        int lastY = wandInHand.getOrCreateTag().getInt("lastY");
        int lastZ = wandInHand.getOrCreateTag().getInt("lastZ");
        
        // מחיקה כפולה לוידוא הסרה מוחלטת
        BlockPos lastPos = new BlockPos(lastX, lastY, lastZ);
        removeLightAroundPosition(level, lastPos);
        
        // מחיקה נוספת במיקום השחקן הנוכחי (למקרה שזז)
        BlockPos currentPos = player.blockPosition();
        removeLightAroundPosition(level, currentPos);
        
        // הסרת הזוהר מהשרביט
        wandInHand.getOrCreateTag().putBoolean("glowing", false);
        wandInHand.getOrCreateTag().remove("glowTime");
        wandInHand.getOrCreateTag().remove("lastX");
        wandInHand.getOrCreateTag().remove("lastY");
        wandInHand.getOrCreateTag().remove("lastZ");
        
        player.sendSystemMessage(Component.literal("Nox! The wand light has gone out."));
    }
    
    private void removeLightAroundPosition(Level level, BlockPos center) {
        // מוחק אור - 2 בלוקים בכל כיוון קרדינלי
        
        // צפון - 2 בלוקים
        for (int i = 1; i <= 2; i++) {
            BlockPos pos = center.north(i);
            if (level.getBlockState(pos).getBlock() == Blocks.LIGHT) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        
        // דרום - 2 בלוקים
        for (int i = 1; i <= 2; i++) {
            BlockPos pos = center.south(i);
            if (level.getBlockState(pos).getBlock() == Blocks.LIGHT) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        
        // מזרח - 2 בלוקים
        for (int i = 1; i <= 2; i++) {
            BlockPos pos = center.east(i);
            if (level.getBlockState(pos).getBlock() == Blocks.LIGHT) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        
        // מערב - 2 בלוקים
        for (int i = 1; i <= 2; i++) {
            BlockPos pos = center.west(i);
            if (level.getBlockState(pos).getBlock() == Blocks.LIGHT) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        
        // בדיקה במרכז גם כן למקרה שנשאר משהו
        if (level.getBlockState(center).getBlock() == Blocks.LIGHT) {
            level.setBlock(center, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}