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
import dev.lambdaurora.lovely_snails.network.SnailSetStoragePagePayload;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;

/**
 * Represents the snail inventory screen.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class SnailInventoryScreen extends AbstractContainerScreen<SnailScreenHandler> {
	private static final Identifier TEXTURE = LovelySnails.id("textures/gui/container/snail.png");
	private static final WidgetSprites ENDER_CHEST_SPRITES = new WidgetSprites(
			LovelySnails.id("container/snail/ender_chest"),
			LovelySnails.id("container/snail/ender_chest_highlighted")
	);
	private static final WidgetSprites[] PAGE_TAB_SPRITES = new WidgetSprites[]{
			new WidgetSprites(
					LovelySnails.id("container/snail/tab/1"),
					LovelySnails.id("container/snail/tab/1_highlighted")
			),
			new WidgetSprites(
					LovelySnails.id("container/snail/tab/2"),
					LovelySnails.id("container/snail/tab/2_highlighted")
			),
			new WidgetSprites(
					LovelySnails.id("container/snail/tab/3"),
					LovelySnails.id("container/snail/tab/3_highlighted")
			),
	};
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
		for (int page = 0; page < PAGE_TAB_SPRITES.length; page++) {
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

	/**
	 * Requests the server to switch to the given storage page.
	 *
	 * @param page the storage page to switch to
	 */
	public void requestStoragePage(int page) {
		ClientPlayNetworking.send(new SnailSetStoragePagePayload(this.menu.syncId, (byte) page));
	}

	/* Input */

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		if (mouseX > x + 98 && mouseY > y + 17 && mouseX <= x + 98 + 5 * 18 && mouseY <= y + 17 + 54) {
			int oldPage = this.getMenu().getCurrentStoragePage();
			int newPage = MathHelper.clamp(oldPage + (amountY > 0 ? -1 : 1), 0, 2);
			if (oldPage == newPage)
				return true;

			if (!this.getMenu().hasChest(newPage)) {
				int otherNewPage = MathHelper.clamp(newPage + (amountY > 0 ? -1 : 1), 0, 2);
				if (newPage == otherNewPage || !this.getMenu().hasChest(otherNewPage))
					return true;

				newPage = otherNewPage;
			}

			this.requestStoragePage(newPage);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, amountX, amountY);
	}

	/* Rendering */

	@Override
	protected void renderBackground(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		//RenderSystem.setShader(GameRenderer::getPositionTexShader);
		//RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		graphics.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

		if (this.entity.canUseSlot(EquipmentSlot.SADDLE)) {
			graphics.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 7 + 18, y + 35 - 18, 18, this.imageHeight + 54, 18, 18, 256, 256);
		}

		graphics.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 7 + 18, y + 35, 36, this.imageHeight + 54, 18, 18, 256, 256);

		if (!this.entity.isBaby()) {
			for (int row = y + 17; row <= y + 35 + 18; row += 18) {
				graphics.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 7, row, 54, this.imageHeight + 54, 18, 18, 256, 256);
			}
		}

		if (this.getMenu().hasChests()) {
			graphics.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 98, y + 17, 0, this.imageHeight, 5 * 18, 54, 256, 256);
		}

		InventoryScreen.renderEntityInInventoryFollowsMouse(
				graphics,
				x + 40, y + 8,
				x + 100, y + 70,
				17, 0.35f,
				this.mouseX, this.mouseY,
				this.entity
		);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		super.render(graphics, mouseX, mouseY, delta);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	private class EnderChestButton extends ImageButton implements ContainerListener {
		public EnderChestButton(int x, int y) {
			super(x, y, 18, 18, ENDER_CHEST_SPRITES,
					btn -> {
						var client = Minecraft.getInstance();
						var screenHandler = SnailInventoryScreen.this.getMenu();
						client.gameMode.handleInventoryButtonClick(screenHandler.syncId, 0);
					}
			);
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
			super(x, y + page * 18 + 1, 15, 16, PAGE_TAB_SPRITES[page],
					btn -> {
						SnailInventoryScreen.this.requestStoragePage(page);
					}
			);
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
