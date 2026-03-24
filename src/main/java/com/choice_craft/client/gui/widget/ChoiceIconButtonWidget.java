package com.choice_craft.client.gui.widget;

import java.util.function.Consumer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChoiceIconButtonWidget extends ClickableWidget {
	private final Identifier texture;
	private final Identifier hoveredTexture;
	private final Consumer<ChoiceIconButtonWidget> onPress;

	public ChoiceIconButtonWidget(int x, int y, int width, int height, Identifier texture, Identifier hoveredTexture, Text message, Consumer<ChoiceIconButtonWidget> onPress) {
		super(x, y, width, height, message);
		this.texture = texture;
		this.hoveredTexture = hoveredTexture;
		this.onPress = onPress;
		this.setTooltip(Tooltip.of(message));
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		Identifier textureId = this.active && this.isHovered() ? this.hoveredTexture : this.texture;
		context.drawTexture(RenderPipelines.GUI_TEXTURED, textureId, this.getX(), this.getY(), 0.0F, 0.0F, this.width, this.height, this.width, this.height);
	}

	@Override
	public void onClick(net.minecraft.client.gui.Click click, boolean doubleClick) {
		if (this.active) {
			this.onPress.accept(this);
		}
	}

	@Override
	protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
		this.appendDefaultNarrations(builder);
	}
}
