/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.lambdaurora.lovely_snails.client;

import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import dev.lambdaurora.lovely_snails.client.render.SnailEntityRenderer;
import dev.lambdaurora.lovely_snails.client.screen.SnailInventoryScreen;
import dev.lambdaurora.lovely_snails.registry.LovelySnailsRegistry;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.entity.model.EntityModelLayer;

/**
 * Represents the Lovely Snails client mod.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class LovelySnailsClient implements ClientModInitializer {
	public static final EntityModelLayer SNAIL_MODEL_LAYER = new EntityModelLayer(LovelySnails.id("snail"), "main");
	public static final EntityModelLayer SNAIL_SADDLE_MODEL_LAYER = new EntityModelLayer(LovelySnails.id("snail"), "saddle");
	public static final EntityModelLayer SNAIL_DECOR_MODEL_LAYER = new EntityModelLayer(LovelySnails.id("snail"), "decor");

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(LovelySnailsRegistry.SNAIL_ENTITY_TYPE, SnailEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(SNAIL_MODEL_LAYER, () -> SnailModel.model(Dilation.NONE));
		EntityModelLayerRegistry.registerModelLayer(SNAIL_SADDLE_MODEL_LAYER, () -> SnailModel.model(new Dilation(0.5f)));
		EntityModelLayerRegistry.registerModelLayer(SNAIL_DECOR_MODEL_LAYER, () -> SnailModel.model(new Dilation(0.25f)));

		HandledScreens.register(LovelySnailsRegistry.SNAIL_SCREEN_HANDLER_TYPE, SnailInventoryScreen::new);

		ClientPlayNetworking.registerGlobalReceiver(LovelySnailsRegistry.SNAIL_SET_STORAGE_PAGE,
				(client, handler, buf, responseSender) -> {
					int syncId = buf.readVarInt();
					byte storagePage = buf.readByte();
					client.execute(() -> {
						if (client.player.currentScreenHandler instanceof SnailScreenHandler snailScreenHandler
								&& snailScreenHandler.syncId == syncId) {
							snailScreenHandler.setCurrentStoragePage(storagePage);
						}
					});
				});
	}
}
