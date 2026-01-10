package com.yourname.harrypottermod;

import com.yourname.harrypottermod.init.ItemInit;
import com.yourname.harrypottermod.items.WandItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HarryPotterMod.MOD_ID)
public class HarryPotterMod {
    public static final String MOD_ID = "harrypottermod";

    public HarryPotterMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register items
        ItemInit.ITEMS.register(modEventBus);
        
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new WandEventHandler());
    }
    
    public static class WandEventHandler {
        
        @SubscribeEvent
        public void onItemToss(ItemTossEvent event) {
            ItemStack tossedItem = event.getEntity().getItem();
            if (tossedItem.getItem() instanceof WandItem) {
                // כיבוי האור אם השרביט נזרק
                CompoundTag tag = tossedItem.getTag();
                if (tag != null && tag.getBoolean("glowing")) {
                    int lastX = tag.getInt("lastX");
                    int lastY = tag.getInt("lastY");
                    int lastZ = tag.getInt("lastZ");
                    removeLightAroundPosition(event.getEntity().getLevel(), new BlockPos(lastX, lastY, lastZ));
                    
                    tag.putBoolean("glowing", false);
                    tag.remove("glowTime");
                    tag.remove("lastX");
                    tag.remove("lastY");
                    tag.remove("lastZ");
                    
                    Player player = event.getPlayer();
                    if (player != null) {
                        player.sendSystemMessage(Component.literal("Nox! The wand light has gone out."));
                    }
                }
            }
        }
        
        private void removeLightAroundPosition(net.minecraft.world.level.Level level, BlockPos center) {
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
    }
}