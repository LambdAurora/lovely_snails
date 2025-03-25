/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.client.render;

import com.mojang.blaze3d.vertex.MatrixStack;
import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.client.LovelySnailsClient;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

/**
 * Renders decoration on a snail.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnailDecorFeatureRenderer extends RenderLayer<SnailEntity, SnailModel> {
	private static final Identifier[] TEXTURES;
	private final SnailModel model;

	public SnailDecorFeatureRenderer(RenderLayerParent<SnailEntity, SnailModel> featureRendererContext, EntityRendererProvider.Context context) {
		super(featureRendererContext);

		this.model = new SnailModel(context.bakeLayer(LovelySnailsClient.SNAIL_DECOR_MODEL_LAYER));
	}

	@Override
	public void render(MatrixStack matrices, MultiBufferSource bufferSource, int light, SnailEntity entity,
			float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		var dyeColor = entity.getCarpetColor();
		if (dyeColor == null) return;
		var texture = TEXTURES[dyeColor.getId()];

		this.getParentModel().copyPropertiesTo(this.model);
		this.model.setupAnim(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
		var vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
		this.model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.f, 1.f, 1.f, 1.f);
	}

	static {
		var colors = DyeColor.values();
		TEXTURES = new Identifier[colors.length];
		for (var color : colors) {
			TEXTURES[color.getId()] = LovelySnails.id("textures/entity/snail/decor/" + color.getName() + ".png");
		}
	}
}
