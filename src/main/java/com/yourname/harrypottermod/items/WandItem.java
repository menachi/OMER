package com.yourname.harrypottermod.items;

import com.yourname.harrypottermod.spells.SpellManager;
import com.yourname.harrypottermod.spells.Spell;
import com.yourname.harrypottermod.ManaSystem;
import com.yourname.harrypottermod.LearnedSpells;
import com.yourname.harrypottermod.client.SpellSelection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import java.util.List;
import net.minecraft.world.item.TooltipFlag;
import javax.annotation.Nullable;

public class WandItem extends Item {
    
    public WandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // טען קסמים נלמדים מ-NBT
        if (!level.isClientSide()) {
            LearnedSpells.loadPlayerSpells(player);
        }
        
        // אתחול מנא רק אם עוד לא הוגדרה
        if (!stack.hasTag() || !stack.getTag().contains("current_mana")) {
            ManaSystem.initializeMana(stack, level.getGameTime());
        } else {
            // עדכון התחדשות המנא בלבד
            ManaSystem.updateManaRegen(stack, level.getGameTime());
        }
        
        if (!level.isClientSide()) {
            if (player.isCrouching()) {
                // כשהשחקן בקרוב ולוחץ ימין - מחליף כישוף
                switchSpell(stack, player, level);
            } else {
                // כשהשחקן לוחץ ימין רגיל - מבצע כישוף
                castSpell(stack, level, player);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player) {
            // וידוא שהמנא הוגדרה ועדכון התחדשות
            if (!stack.hasTag() || !stack.getTag().contains("current_mana")) {
                ManaSystem.initializeMana(stack, level.getGameTime());
            } else {
                ManaSystem.updateManaRegen(stack, level.getGameTime());
            }
            
            // בדיקה אם השחקן מחזיק שרביט דולק שלא ביד הפעילה
            if (!isWandInActiveHand(player, stack)) {
                stopGlowing(stack, level, player);
                return;
            }
            
            // הצגת מנא כל כמה שניות כשהשחקן מחזיק השרביט
            long gameTime = level.getGameTime();
            if (selected && gameTime % 100 == 0) { // כל 5 שניות
                player.sendSystemMessage(Component.literal(ManaSystem.getManaBar(stack)));
            }
            
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.getBoolean("glowing")) {
                long glowTime = tag.getLong("glowTime");
                
                if (gameTime < glowTime) {
                    // השרביט עדיין צריך להאיר
                    BlockPos playerPos = player.blockPosition();
                    int lastX = tag.getInt("lastX");
                    int lastY = tag.getInt("lastY");
                    int lastZ = tag.getInt("lastZ");
                    
                    // בודק אם השחקן זז
                    if (playerPos.getX() != lastX || playerPos.getY() != lastY || playerPos.getZ() != lastZ) {
                        // מסיר אור מהמקום הקודם
                        removeLightAroundPosition(level, new BlockPos(lastX, lastY, lastZ));
                        
                        // מעדכן את המיקום החדש
                        tag.putInt("lastX", playerPos.getX());
                        tag.putInt("lastY", playerPos.getY());
                        tag.putInt("lastZ", playerPos.getZ());
                    }
                    
                    // יוצר אור סביב המיקום הנוכחי
                    createLightAroundPosition(level, playerPos);
                } else {
                    // זמן הזוהר נגמר - מכבה את השרביט
                    BlockPos playerPos = player.blockPosition();
                    removeLightAroundPosition(level, playerPos);
                    
                    tag.putBoolean("glowing", false);
                    tag.remove("glowTime");
                    tag.remove("lastX");
                    tag.remove("lastY");
                    tag.remove("lastZ");
                    
                    player.sendSystemMessage(Component.literal("Nox! The wand light has gone out."));
                }
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }
    
    private void createLightAroundPosition(Level level, BlockPos center) {
        // יצירת אור קטן - 2 בלוקים בכל כיוון קרדינלי
        
        // צפון - 2 בלוקים
        for (int i = 1; i <= 2; i++) {
            BlockPos pos = center.north(i);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 9), 3);
            }
        }
        
        // דרום - 2 בלוקים
        for (int i = 1; i <= 2; i++) {
            BlockPos pos = center.south(i);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 9), 3);
            }
        }
        
        // מזרח - 2 בלוקים
        for (int i = 1; i <= 2; i++) {
            BlockPos pos = center.east(i);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 9), 3);
            }
        }
        
        // מערב - 2 בלוקים
        for (int i = 1; i <= 2; i++) {
            BlockPos pos = center.west(i);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 9), 3);
            }
        }
    }
        private boolean isWandInActiveHand(Player player, ItemStack wandStack) {
        // בדיקה אם השרביט נמצא ביד הראשית או שנייה
        return player.getMainHandItem() == wandStack || player.getOffhandItem() == wandStack;
    }
    
    private void stopGlowing(ItemStack stack, Level level, Player player) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getBoolean("glowing")) {
            // כיבוי האור במיקום הנוכחי
            int lastX = tag.getInt("lastX");
            int lastY = tag.getInt("lastY");
            int lastZ = tag.getInt("lastZ");
            
            // מחיקה כפולה לוידוא הסרה מוחלטת
            BlockPos lastPos = new BlockPos(lastX, lastY, lastZ);
            removeLightAroundPosition(level, lastPos);
            
            // מחיקה נוספת במיקום השחקן הנוכחי
            BlockPos currentPos = player.blockPosition();
            removeLightAroundPosition(level, currentPos);
            
            // הסרת הזוהר מהשרביט
            tag.putBoolean("glowing", false);
            tag.remove("glowTime");
            tag.remove("lastX");
            tag.remove("lastY");
            tag.remove("lastZ");
            
            player.sendSystemMessage(Component.literal("Nox! The wand light has gone out."));
        }
    }
        private void removeLightAroundPosition(Level level, BlockPos center) {
        // מוחק אור באזור גדול יותר לוידוא מחיקה מלאה
        for (int x = -4; x <= 4; x++) {
            for (int y = -3; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos lightPos = center.offset(x, y, z);
                    if (level.getBlockState(lightPos).getBlock() == Blocks.LIGHT) {
                        level.setBlock(lightPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void switchSpell(ItemStack stack, Player player, Level level) {
        // עדכון מנא לפני הצגה
        ManaSystem.updateManaRegen(stack, level.getGameTime());
        
        // בדיקה כמה קסמים השחקן למד
        int learnedSpellCount = SpellManager.getLearnedSpellCount(player);
        if (learnedSpellCount == 0) {
            player.sendSystemMessage(Component.literal("You haven't learned any spells yet! Find spell books to learn magic.")
                .withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal(ManaSystem.getManaBar(stack)));
            return;
        }
        
        // החלפה ללחש הבא (רק בין קסמים נלמדים)
        SpellSelection.nextSpell(player.getUUID(), learnedSpellCount);
        int selectedIndex = SpellSelection.getSelectedSpell(player.getUUID());
        
        // קבל את הקסם הנלמד לפי האינדקס
        List<Spell> learnedSpells = SpellManager.getLearnedSpells(player);
        if (selectedIndex >= 0 && selectedIndex < learnedSpells.size()) {
            Spell selectedSpell = learnedSpells.get(selectedIndex);
            player.sendSystemMessage(Component.literal("Selected spell: " + selectedSpell.getName())
                .withStyle(ChatFormatting.GOLD));
        }
        
        player.sendSystemMessage(Component.literal(ManaSystem.getManaBar(stack)));
    }
    
    private void castSpell(ItemStack stack, Level level, Player player) {
        // בדיקה כמה קסמים השחקן למד
        List<Spell> learnedSpells = SpellManager.getLearnedSpells(player);
        if (learnedSpells.isEmpty()) {
            player.sendSystemMessage(Component.literal("You haven't learned any spells yet! Find spell books to learn magic.")
                .withStyle(ChatFormatting.RED));
            return;
        }
        
        // שימוש בלחש הנבחר מתוך הקסמים הנלמדים
        int selectedIndex = SpellSelection.getSelectedSpell(player.getUUID());
        if (selectedIndex >= 0 && selectedIndex < learnedSpells.size()) {
            Spell selectedSpell = learnedSpells.get(selectedIndex);
            boolean success = selectedSpell.castWithWand(level, player, stack);
            if (!success) {
                player.sendSystemMessage(Component.literal("The spell failed!"));
            }
        } else {
            player.sendSystemMessage(Component.literal("No spell selected! Hold crouch and right-click to switch spells.")
                .withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // הסרת הזוהר הקסום - השרביט לא יזהיר
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // הסרת מידע מיותר ו-tooltips חשופים
        tooltip.clear();
        
        // הוספת מידע בסיסי בלבד
        tooltip.add(Component.literal("A magical wand").withStyle(ChatFormatting.GRAY));
        
        // הצגת מנא רק אם יש
        if (stack.hasTag() && stack.getTag().contains("mana")) {
            int mana = stack.getTag().getInt("mana");
            tooltip.add(Component.literal("Mana: " + mana + "/100").withStyle(ChatFormatting.BLUE));
        }
        
        // לא מציג שום קסם נוכחי אם לא נלמד
    }
}