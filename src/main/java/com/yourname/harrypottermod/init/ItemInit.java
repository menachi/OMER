package com.yourname.harrypottermod.init;

import com.yourname.harrypottermod.HarryPotterMod;
import com.yourname.harrypottermod.items.WandItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, HarryPotterMod.MOD_ID);

    // השרבית - מתחילה עם טקסטורה של מקל
    public static final RegistryObject<Item> WAND = ITEMS.register("wand",
        () -> new WandItem(new Item.Properties().tab(CreativeTabInit.HARRY_POTTER_TAB).stacksTo(1)));
    
    // הסרנו את שמלת הלומוש
}