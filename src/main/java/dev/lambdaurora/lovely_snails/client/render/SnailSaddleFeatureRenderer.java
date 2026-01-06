/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.client.LovelySnailsClient;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Renders decoration on a snail.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.2.0
 */
public class SnailSaddleFeatureRenderer extends RenderLayer<SnailEntityRenderState, SnailModel> {
	private static final Identifier TEXTURE = LovelySnails.id("textures/entity/snail/saddle.png");
	private final SnailModel model;

	public SnailSaddleFeatureRenderer(RenderLayerParent<SnailEntityRenderState, SnailModel> featureRendererContext, EntityRendererProvider.Context context) {
		super(featureRendererContext);
		this.model = new SnailModel(context.bakeLayer(LovelySnailsClient.SNAIL_SADDLE_MODEL_LAYER));
	}

	@Override
	public void submit(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, SnailEntityRenderState state, float tickDelta, float animationProgress) {
		if (!state.hasSaddle) return;
		this.model.setupAnim(state);
		submitNodeCollector.submitModel(this.model, state, matrices, RenderTypes.entityCutoutNoCull(TEXTURE), light, OverlayTexture.NO_OVERLAY, 0xffffffff, null, state.outlineColor, null);
	}
}
