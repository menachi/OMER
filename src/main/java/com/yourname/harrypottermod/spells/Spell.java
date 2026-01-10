package com.yourname.harrypottermod.spells;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class Spell {
    private final String name;
    private final int manaCost;
    
    public Spell(String name, int manaCost) {
        this.name = name;
        this.manaCost = manaCost;
    }
    
    public abstract void cast(Level level, Player player);
    
    public String getName() {
        return name;
    }
    
    public int getManaCost() {
        return manaCost;
    }
}