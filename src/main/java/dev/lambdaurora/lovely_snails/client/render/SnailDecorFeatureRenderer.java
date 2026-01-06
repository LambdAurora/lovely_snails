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
import net.minecraft.world.item.DyeColor;

/**
 * Renders decoration on a snail.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
public class SnailDecorFeatureRenderer extends RenderLayer<SnailEntityRenderState, SnailModel> {
	private static final Identifier[] TEXTURES;
	private final SnailModel model;

	public SnailDecorFeatureRenderer(RenderLayerParent<SnailEntityRenderState, SnailModel> featureRendererContext, EntityRendererProvider.Context context) {
		super(featureRendererContext);
		this.model = new SnailModel(context.bakeLayer(LovelySnailsClient.SNAIL_DECOR_MODEL_LAYER));
	}

	@Override
	public void submit(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, SnailEntityRenderState state, float tickDelta, float animationProgress) {
		var dyeColor = state.carpetColor;
		if (dyeColor == null) return;
		var texture = TEXTURES[dyeColor.getId()];

		this.model.setupAnim(state);
		submitNodeCollector.submitModel(this.model, state, matrices, RenderTypes.entityCutoutNoCull(texture), light, OverlayTexture.NO_OVERLAY, 0xffffffff, null, state.outlineColor, null);
	}

	static {
		var colors = DyeColor.values();
		TEXTURES = new Identifier[colors.length];
		for (var color : colors) {
			TEXTURES[color.getId()] = LovelySnails.id("textures/entity/snail/decor/" + color.getName() + ".png");
		}
	}
}
