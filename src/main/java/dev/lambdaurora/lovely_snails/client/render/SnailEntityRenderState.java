package dev.lambdaurora.lovely_snails.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class SnailEntityRenderState extends LivingEntityRenderState {
    public boolean isScared = false;
    public DyeColor carpetColor = null;
    public ItemStack[] chests = new ItemStack[3];
    public boolean hasSaddle = false;
}
