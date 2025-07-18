/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * Rendering data needed for snails
 *
 * @author Patbox
 * @version 1.2.1
 * @since 1.2.1
 */
public class SnailEntityRenderState extends LivingEntityRenderState {
    public boolean isScared = false;
    public DyeColor carpetColor = null;
    public ItemStack[] chests = new ItemStack[3];
    public boolean hasSaddle = false;
}
