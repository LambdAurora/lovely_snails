/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.client.render;

import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.client.LovelySnailsClient;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the snail entity renderer.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
public class SnailEntityRenderer extends MobRenderer<SnailEntity, SnailModel> {
	public static final Identifier TEXTURE = LovelySnails.id("textures/entity/snail/snail.png");

	public SnailEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new SnailModel(context.bakeLayer(LovelySnailsClient.SNAIL_MODEL_LAYER)), .5f);

		this.addLayer(new SaddleLayer<>(this,
				new SnailModel(context.bakeLayer(LovelySnailsClient.SNAIL_SADDLE_MODEL_LAYER)),
				LovelySnails.id("textures/entity/snail/saddle.png")
		));
		this.addLayer(new SnailDecorFeatureRenderer(this, context));
		this.addLayer(new SnailChestFeatureRenderer(this, context));
	}

	@Override
	public @NotNull Identifier getTextureLocation(SnailEntity entity) {
		return TEXTURE;
	}
}
