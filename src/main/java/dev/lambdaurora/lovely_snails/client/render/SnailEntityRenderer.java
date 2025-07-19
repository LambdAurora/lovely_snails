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
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the snail entity renderer.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
public class SnailEntityRenderer extends MobRenderer<SnailEntity, SnailEntityRenderState, SnailModel> {
	public static final Identifier TEXTURE = LovelySnails.id("textures/entity/snail/snail.png");

	public SnailEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new SnailModel(context.bakeLayer(LovelySnailsClient.SNAIL_MODEL_LAYER)), .5f);

		this.addLayer(new SnailSaddleFeatureRenderer(this, context));
		this.addLayer(new SnailDecorFeatureRenderer(this, context));
		this.addLayer(new SnailChestFeatureRenderer(this, context));
	}

	@Override
	protected void scale(SnailEntityRenderState state, MatrixStack matrices) {
		super.scale(state, matrices);
		this.getModel().getCurrentModel(state).updateMatrix(matrices);
	}

	@Override
	public @NotNull SnailEntityRenderState createRenderState() {
		return new SnailEntityRenderState();
	}

	@Override
	public void extractRenderState(SnailEntity snail, SnailEntityRenderState state, float tickDelta) {
		super.extractRenderState(snail, state, tickDelta);
		state.isScared = snail.isScared();
		state.hasSaddle = !snail.getSaddle().isEmpty();
		state.carpetColor = snail.getCarpetColor();
		for (int i = 0; i < 3; i++) {
			state.chests[i] = snail.getChest(i).copy();
		}
	}

	@Override
	public @NotNull Identifier getTextureLocation(SnailEntityRenderState livingEntityRenderState) {
		return TEXTURE;
	}
}
