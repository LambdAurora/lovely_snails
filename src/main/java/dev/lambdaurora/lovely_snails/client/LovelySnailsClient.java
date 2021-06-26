/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
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

package dev.lambdaurora.lovely_snails.client;

import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import dev.lambdaurora.lovely_snails.client.render.SnailEntityRenderer;
import dev.lambdaurora.lovely_snails.client.screen.SnailInventoryScreen;
import dev.lambdaurora.lovely_snails.registry.LovelySnailsRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.entity.model.EntityModelLayer;

/**
 * Represents the Lovely Snails client mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class LovelySnailsClient implements ClientModInitializer {
    public static final EntityModelLayer SNAIL_MODEL_LAYER = new EntityModelLayer(LovelySnails.id("snail"), "main");
    public static final EntityModelLayer SNAIL_SADDLE_MODEL_LAYER = new EntityModelLayer(LovelySnails.id("snail"), "saddle");
    public static final EntityModelLayer SNAIL_DECOR_MODEL_LAYER = new EntityModelLayer(LovelySnails.id("snail"), "decor");

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(LovelySnailsRegistry.SNAIL_ENTITY_TYPE, SnailEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(SNAIL_MODEL_LAYER, () -> SnailModel.model(Dilation.NONE));
        EntityModelLayerRegistry.registerModelLayer(SNAIL_SADDLE_MODEL_LAYER, () -> SnailModel.model(new Dilation(0.5f)));
        EntityModelLayerRegistry.registerModelLayer(SNAIL_DECOR_MODEL_LAYER, () -> SnailModel.model(new Dilation(0.25f)));

        ScreenRegistry.register(LovelySnailsRegistry.SNAIL_SCREEN_HANDLER_TYPE, SnailInventoryScreen::new);
    }
}
