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
import com.mojang.math.Axis;
import dev.lambdaurora.lovely_snails.client.LovelySnailsClient;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the chests on a snail.
 *
 * @author LambdAurora
 * @version 1.1.1
 * @since 1.0.0
 */
public class SnailChestFeatureRenderer extends RenderLayer<SnailEntityRenderState, SnailModel> {
	private final SnailModel model;

	public SnailChestFeatureRenderer(RenderLayerParent<SnailEntityRenderState, SnailModel> featureRendererContext, EntityRendererProvider.Context context) {
		super(featureRendererContext);

		this.model = new SnailModel(context.bakeLayer(LovelySnailsClient.SNAIL_MODEL_LAYER));
	}

	@Override
	public void submit(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, SnailEntityRenderState state, float tickDelta, float animationProgress) {
		if (state.isBaby) return;

		float shellRotation = this.model.getCurrentModel(state).getShell().xRot;

		var rightChest = state.chests[0];
		if (!rightChest.isEmpty()) {
			matrices.pushPose();
			matrices.mulPose(Axis.XP.rotationDegrees(180));
			matrices.mulPose(Axis.XP.rotation(shellRotation));
			matrices.mulPose(Axis.YP.rotationDegrees(90));
			matrices.translate(.65, 0.2, -.505);
			matrices.scale(1.25f, 1.25f, 1.25f);
			renderChest(matrices, submitNodeCollector, state, rightChest);
			matrices.popPose();
		}

		var backChest = state.chests[1];
		if (!backChest.isEmpty()) {
			matrices.pushPose();
			matrices.mulPose(Axis.XP.rotationDegrees(180));
			matrices.mulPose(Axis.XP.rotation(shellRotation));
			matrices.translate(0, 0.2, -.94);
			matrices.scale(1.25f, 1.25f, 1.25f);
			renderChest(matrices, submitNodeCollector, state, backChest);
			matrices.popPose();
		}

		var leftChest = state.chests[2];
		if (!leftChest.isEmpty()) {
			matrices.pushPose();
			matrices.mulPose(Axis.XP.rotationDegrees(180));
			matrices.mulPose(Axis.XP.rotation(shellRotation));
			matrices.mulPose(Axis.YN.rotationDegrees(90));
			matrices.translate(-.65, 0.2, -.505);
			matrices.scale(1.25f, 1.25f, 1.25f);
			renderChest(matrices, submitNodeCollector, state, leftChest);
			matrices.popPose();
		}
	}

	private void renderChest(PoseStack matrices, SubmitNodeCollector submitNodeCollector, SnailEntityRenderState state, ItemStack chest) {
		var itemModelResolver = Minecraft.getInstance().getItemModelResolver();

		ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
		itemModelResolver.updateForTopItem(itemStackRenderState, chest, ItemDisplayContext.FIXED, null, null, 0);
		itemStackRenderState.submit(matrices, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
	}
}
