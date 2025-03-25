/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Inventory;

/**
 * Represents the snail inventory screen.
 *
 * @author LambdAurora
 * @version 1.1.1
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class SnailInventoryScreen extends AbstractContainerScreen<SnailScreenHandler> {
	private static final Identifier TEXTURE = LovelySnails.id("textures/gui/container/snail.png");
	private final SnailEntity entity;
	private float mouseX;
	private float mouseY;
	private EnderChestButton enderChestButton;
	private final PageButton[] pageButtons = new PageButton[3];

	public SnailInventoryScreen(SnailScreenHandler handler, Inventory inventory, Text title) {
		super(handler, inventory, handler.snail().getDisplayName());
		this.imageWidth += 19;
		this.entity = handler.snail();
	}

	private void clearListeners() {
		if (this.enderChestButton != null) {
			this.getMenu().getInventory().removeListener(this.enderChestButton);
		}
		this.enderChestButton = null;

		for (int page = 0; page < 3; page++) {
			if (this.pageButtons[page] != null) {
				this.getMenu().getInventory().removeListener(this.pageButtons[page]);
				this.getMenu().removePageChangeListener(this.pageButtons[page]);
			}

			this.pageButtons[page] = null;
		}
	}

	@Override
	protected void init() {
		super.init();
		this.clearListeners();

		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		this.addRenderableWidget(this.enderChestButton = new EnderChestButton(x + 7 + 18, y + 35 + 18));
		this.getMenu().getInventory().addListener(this.enderChestButton);

		int buttonX = x + this.imageWidth - 3;
		int buttonY = y + 17;
		for (int page = 0; page < 3; page++) {
			this.addRenderableWidget(this.pageButtons[page] = new PageButton(buttonX, buttonY, page));
			this.getMenu().getInventory().addListener(this.pageButtons[page]);
			this.getMenu().addPageChangeListener(this.pageButtons[page]);
		}
	}

	@Override
	public void removed() {
		super.removed();
		this.clearListeners();
	}

	@Override
	public void onClose() {
		super.onClose();
		this.clearListeners();
	}

	/* Input */

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		if (mouseX > x + 98 && mouseY > y + 17 && mouseX <= x + 98 + 5 * 18 && mouseY <= y + 17 + 54) {
			int oldPage = this.getMenu().getCurrentStoragePage();
			int newPage = MathHelper.clamp(oldPage + (amount > 0 ? -1 : 1), 0, 2);
			if (oldPage == newPage)
				return true;

			if (!this.getMenu().hasChest(newPage)) {
				int otherNewPage = MathHelper.clamp(newPage + (amount > 0 ? -1 : 1), 0, 2);
				if (newPage == otherNewPage || !this.getMenu().hasChest(otherNewPage))
					return true;

				newPage = otherNewPage;
			}

			this.getMenu().requestStoragePage(newPage);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	/* Rendering */

	@Override
	protected void renderBackground(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		graphics.drawTexture(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

		if (this.entity.isSaddleable()) {
			graphics.drawTexture(TEXTURE, x + 7 + 18, y + 35 - 18, 18, this.imageHeight + 54, 18, 18);
		}

		graphics.drawTexture(TEXTURE, x + 7 + 18, y + 35, 36, this.imageHeight + 54, 18, 18);

		if (!this.entity.isBaby()) {
			for (int row = y + 17; row <= y + 35 + 18; row += 18) {
				graphics.drawTexture(TEXTURE, x + 7, row, 54, this.imageHeight + 54, 18, 18);
			}
		}

		if (this.getMenu().hasChests()) {
			graphics.drawTexture(TEXTURE, x + 98, y + 17, 0, this.imageHeight, 5 * 18, 54);
		}

		InventoryScreen.renderEntityInInventoryFollowsMouse(
				graphics, x + 70, y + 60, 17,
				(x + 51) - this.mouseX, (y + 75 - 50) - this.mouseY,
				this.entity
		);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.renderBackground(graphics);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		super.render(graphics, mouseX, mouseY, delta);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	private class EnderChestButton extends ImageButton implements ContainerListener {
		public EnderChestButton(int x, int y) {
			super(x, y, 18, 18, 0, 0, 18, LovelySnails.id("textures/gui/snail_ender_chest_button.png"),
					18, 36,
					btn -> {
						var client = Minecraft.getInstance();
						var screenHandler = SnailInventoryScreen.this.getMenu();
						client.gameMode.handleInventoryButtonClick(screenHandler.syncId, 0);
					});
		}

		@Override
		public void playDownSound(SoundManager soundManager) {
			var snail = SnailInventoryScreen.this.getMenu().snail();
			soundManager.play(SimpleSoundInstance.forUI(SoundEvents.ENDER_CHEST_OPEN, snail.getRandom().nextFloat() * .1f + .9f, .5f));
		}

		@Override
		public void onContainerChanged(Container sender) {
			this.visible = this.active = SnailInventoryScreen.this.getMenu().hasEnderChest();
		}
	}

	private class PageButton extends ImageButton implements ContainerListener, SnailScreenHandler.InventoryPageChangeListener {
		private final int page;

		public PageButton(int x, int y, int page) {
			super(x, y + page * 18 + 1, 15, 16, 211 + page * 15, 0, 16, TEXTURE,
					256, 256,
					btn -> {
						SnailInventoryScreen.this.getMenu().requestStoragePage(page);
					});
			this.page = page;

			this.visible = SnailInventoryScreen.this.getMenu().hasChest(this.page);
			this.onCurrentPageSet(SnailInventoryScreen.this.getMenu().getCurrentStoragePage());
		}

		@Override
		public void onContainerChanged(Container sender) {
			this.visible = SnailInventoryScreen.this.getMenu().hasChest(page);
		}

		@Override
		public void onCurrentPageSet(int page) {
			this.active = this.page != page;
			this.setFocused(this.page == page);
		}
	}
}
