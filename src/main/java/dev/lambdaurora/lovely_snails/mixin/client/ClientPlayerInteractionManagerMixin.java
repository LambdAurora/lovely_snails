/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.mixin.client;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "hasRidingInventory", at = @At("HEAD"), cancellable = true)
	private void onHasRidingInventory(CallbackInfoReturnable<Boolean> cir) {
		//noinspection ConstantConditions
		if (this.client.player.hasVehicle() && this.client.player.getVehicle() instanceof SnailEntity) {
			cir.setReturnValue(true);
		}
	}
}
