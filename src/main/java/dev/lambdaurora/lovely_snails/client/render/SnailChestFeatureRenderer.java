/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.lovely_snails.client.render;

import dev.lambdaurora.lovely_snails.client.LovelySnailsClient;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Axis;

/**
 * Renders the chests on a snail.
 *
 * @author LambdAurora
 * @version 1.1.1
 * @since 1.0.0
 */
public class SnailChestFeatureRenderer extends FeatureRenderer<SnailEntity, SnailModel> {
	private final SnailModel model;

	public SnailChestFeatureRenderer(FeatureRendererContext<SnailEntity, SnailModel> featureRendererContext, EntityRendererFactory.Context context) {
		super(featureRendererContext);

		this.model = new SnailModel(context.getPart(LovelySnailsClient.SNAIL_MODEL_LAYER));
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SnailEntity entity,
			float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		if (entity.isBaby()) return;

		var itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		this.getContextModel().copyStateTo(this.model);

		float shellRotation = this.model.getCurrentModel().getShell().pitch;

		var rightChest = entity.getChest(0);
		if (!rightChest.isEmpty()) {
			matrices.push();
			matrices.multiply(Axis.X_POSITIVE.rotationDegrees(180));
			matrices.multiply(Axis.X_POSITIVE.rotation(shellRotation));
			matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(90));
			matrices.translate(.65, 0.2, -.505);
			matrices.scale(1.25f, 1.25f, 1.25f);
			itemRenderer.renderItem(rightChest, ModelTransformation.Mode.FIXED, light, OverlayTexture.DEFAULT_UV,
					matrices, vertexConsumers, 0);
			matrices.pop();
		}

		var backChest = entity.getChest(1);
		if (!backChest.isEmpty()) {
			matrices.push();
			matrices.multiply(Axis.X_POSITIVE.rotationDegrees(180));
			matrices.multiply(Axis.X_POSITIVE.rotation(shellRotation));
			matrices.translate(0, 0.2, -.94);
			matrices.scale(1.25f, 1.25f, 1.25f);
			itemRenderer.renderItem(backChest, ModelTransformation.Mode.FIXED, light, OverlayTexture.DEFAULT_UV,
					matrices, vertexConsumers, 0);
			matrices.pop();
		}

		var leftChest = entity.getChest(2);
		if (!leftChest.isEmpty()) {
			matrices.push();
			matrices.multiply(Axis.X_POSITIVE.rotationDegrees(180));
			matrices.multiply(Axis.X_POSITIVE.rotation(shellRotation));
			matrices.multiply(Axis.Y_NEGATIVE.rotationDegrees(90));
			matrices.translate(-.65, 0.2, -.505);
			matrices.scale(1.25f, 1.25f, 1.25f);
			itemRenderer.renderItem(leftChest, ModelTransformation.Mode.FIXED, light, OverlayTexture.DEFAULT_UV,
					matrices, vertexConsumers, 0);
			matrices.pop();
		}
	}
}
