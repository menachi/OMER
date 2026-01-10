package com.yourname.harrypottermod.init;

import com.yourname.harrypottermod.HarryPotterMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTabInit {
    public static final CreativeModeTab HARRY_POTTER_TAB = new CreativeModeTab(HarryPotterMod.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemInit.WAND.get());
        }
    };
}