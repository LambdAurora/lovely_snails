/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.mixin;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handlePlayerCommand", at = @At("RETURN"))
	private void lovely_snails$handlePlayerCommand(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
		if (packet.getAction() == ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY && this.player.getVehicle() instanceof SnailEntity snail) {
			snail.openInventory(this.player);
		}
	}
}
