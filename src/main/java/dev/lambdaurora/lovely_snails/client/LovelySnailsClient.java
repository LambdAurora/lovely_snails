/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.client;

import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import dev.lambdaurora.lovely_snails.client.render.SnailEntityRenderer;
import dev.lambdaurora.lovely_snails.client.screen.SnailInventoryScreen;
import dev.lambdaurora.lovely_snails.network.SnailSetStoragePagePayload;
import dev.lambdaurora.lovely_snails.registry.LovelySnailsRegistry;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.EntityRenderers;

/**
 * Represents the Lovely Snails client mod.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class LovelySnailsClient implements ClientModInitializer {
	public static final ModelLayerLocation SNAIL_MODEL_LAYER = new ModelLayerLocation(LovelySnails.id("snail"), "main");
	public static final ModelLayerLocation SNAIL_SADDLE_MODEL_LAYER = new ModelLayerLocation(LovelySnails.id("snail"), "saddle");
	public static final ModelLayerLocation SNAIL_DECOR_MODEL_LAYER = new ModelLayerLocation(LovelySnails.id("snail"), "decor");

	@Override
	public void onInitializeClient() {
		EntityRenderers.register(LovelySnailsRegistry.SNAIL_ENTITY_TYPE, SnailEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(SNAIL_MODEL_LAYER, () -> SnailModel.model(CubeDeformation.NONE));
		EntityModelLayerRegistry.registerModelLayer(SNAIL_SADDLE_MODEL_LAYER, () -> SnailModel.model(new CubeDeformation(0.5f)));
		EntityModelLayerRegistry.registerModelLayer(SNAIL_DECOR_MODEL_LAYER, () -> SnailModel.model(new CubeDeformation(0.25f)));

		MenuScreens.register(LovelySnailsRegistry.SNAIL_SCREEN_HANDLER_TYPE, SnailInventoryScreen::new);

		ClientPlayNetworking.registerGlobalReceiver(SnailSetStoragePagePayload.TYPE,
				(payload, context) -> {
					context.client().execute(() -> {
						if (context.player().containerMenu instanceof SnailScreenHandler snailScreenHandler
								&& snailScreenHandler.syncId == payload.syncId()) {
							snailScreenHandler.setCurrentStoragePage(payload.storagePage());
						}
					});
				});
	}
}
