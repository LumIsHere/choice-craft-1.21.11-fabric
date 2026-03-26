package com.choice_craft.client.gui.widget;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ChoiceIconButtonWidget extends AbstractWidget {
	private final Identifier texture;
	private final Identifier hoveredTexture;
	private final Consumer<ChoiceIconButtonWidget> onPress;

	public ChoiceIconButtonWidget(int x, int y, int width, int height, Identifier texture, Identifier hoveredTexture, Component message, Consumer<ChoiceIconButtonWidget> onPress) {
		super(x, y, width, height, message);
		this.texture = texture;
		this.hoveredTexture = hoveredTexture;
		this.onPress = onPress;
		this.setTooltip(Tooltip.create(message));
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		Identifier textureId = this.active && this.isHovered() ? this.hoveredTexture : this.texture;
		context.blit(RenderPipelines.GUI_TEXTURED, textureId, this.getX(), this.getY(), 0.0F, 0.0F, this.width, this.height, this.width, this.height);
	}

	@Override
	public void onClick(net.minecraft.client.input.MouseButtonEvent click, boolean doubleClick) {
		if (this.active) {
			this.onPress.accept(this);
		}
	}

	@Override
	protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput builder) {
		this.defaultButtonNarrationText(builder);
	}
}
