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
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.util.Identifier;

/**
 * Represents the snail entity renderer.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnailEntityRenderer extends MobEntityRenderer<SnailEntity, SnailModel> {
	public static final Identifier TEXTURE = LovelySnails.id("textures/entity/snail/snail.png");

	public SnailEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new SnailModel(context.getPart(LovelySnailsClient.SNAIL_MODEL_LAYER)), .5f);

		this.addFeature(new SaddleFeatureRenderer<>(this,
				new SnailModel(context.getPart(LovelySnailsClient.SNAIL_SADDLE_MODEL_LAYER)),
				LovelySnails.id("textures/entity/snail/saddle.png")));
		this.addFeature(new SnailDecorFeatureRenderer(this, context));
		this.addFeature(new SnailChestFeatureRenderer(this, context));
	}

	@Override
	public Identifier getTexture(SnailEntity entity) {
		return TEXTURE;
	}
}
