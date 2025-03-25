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
import com.mojang.math.Axis;
import dev.lambdaurora.lovely_snails.client.LovelySnailsClient;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Renders the chests on a snail.
 *
 * @author LambdAurora
 * @version 1.1.1
 * @since 1.0.0
 */
public class SnailChestFeatureRenderer extends RenderLayer<SnailEntity, SnailModel> {
	private final SnailModel model;

	public SnailChestFeatureRenderer(RenderLayerParent<SnailEntity, SnailModel> featureRendererContext, EntityRendererProvider.Context context) {
		super(featureRendererContext);

		this.model = new SnailModel(context.bakeLayer(LovelySnailsClient.SNAIL_MODEL_LAYER));
	}

	@Override
	public void render(MatrixStack matrices, MultiBufferSource bufferSource, int light, SnailEntity entity,
			float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		if (entity.isBaby()) return;

		var itemRenderer = Minecraft.getInstance().getItemRenderer();
		this.getParentModel().copyPropertiesTo(this.model);

		float shellRotation = this.model.getCurrentModel().getShell().pitch;

		var rightChest = entity.getChest(0);
		if (!rightChest.isEmpty()) {
			matrices.push();
			matrices.rotate(Axis.XP.rotationDegrees(180));
			matrices.rotate(Axis.XP.rotation(shellRotation));
			matrices.rotate(Axis.YP.rotationDegrees(90));
			matrices.translate(.65, 0.2, -.505);
			matrices.scale(1.25f, 1.25f, 1.25f);
			itemRenderer.renderStatic(
					rightChest, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
					matrices, bufferSource, entity.level(), 0
			);
			matrices.pop();
		}

		var backChest = entity.getChest(1);
		if (!backChest.isEmpty()) {
			matrices.push();
			matrices.rotate(Axis.XP.rotationDegrees(180));
			matrices.rotate(Axis.XP.rotation(shellRotation));
			matrices.translate(0, 0.2, -.94);
			matrices.scale(1.25f, 1.25f, 1.25f);
			itemRenderer.renderStatic(
					backChest, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
					matrices, bufferSource, entity.level(), 0
			);
			matrices.pop();
		}

		var leftChest = entity.getChest(2);
		if (!leftChest.isEmpty()) {
			matrices.push();
			matrices.rotate(Axis.XP.rotationDegrees(180));
			matrices.rotate(Axis.XP.rotation(shellRotation));
			matrices.rotate(Axis.YN.rotationDegrees(90));
			matrices.translate(-.65, 0.2, -.505);
			matrices.scale(1.25f, 1.25f, 1.25f);
			itemRenderer.renderStatic(
					leftChest, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
					matrices, bufferSource, entity.level(), 0
			);
			matrices.pop();
		}
	}
}
