package com.yourname.harrypottermod.spells;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;

public abstract class Spell {
    private final String name;
    private final int manaCost;
    
    public Spell(String name, int manaCost) {
        this.name = name;
        this.manaCost = manaCost;
    }
    
    public abstract void cast(Level level, Player player);
    
    // פונקציה חדשה שמקבלת את השרביט
    public boolean castWithWand(Level level, Player player, ItemStack wand) {
        cast(level, player);
        return true;
    }
    
    public String getName() {
        return name;
    }
    
    public int getManaCost() {
        return manaCost;
    }
}