package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class DisconnectedScreen extends Screen {
   private final Text reason;
   private MultilineText reasonFormatted;
   private final Screen parent;
   private int reasonHeight;

   public DisconnectedScreen(Screen parent, Text text, Text reason) {
      super(text);
      this.reasonFormatted = MultilineText.EMPTY;
      this.parent = parent;
      this.reason = reason;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected void init() {
      this.reasonFormatted = MultilineText.create(this.textRenderer, this.reason, this.width - 50);
      int var10001 = this.reasonFormatted.count();
      this.textRenderer.getClass();
      this.reasonHeight = var10001 * 9;
      int var10003 = this.width / 2 - 100;
      int var10004 = this.height / 2 + this.reasonHeight / 2;
      this.textRenderer.getClass();
      this.addButton(new ButtonWidget(var10003, Math.min(var10004 + 9, this.height - 30), 200, 20, new TranslatableText("gui.toMenu"), (buttonWidget) -> {
         this.client.openScreen(this.parent);
      }));
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      TextRenderer var10001 = this.textRenderer;
      Text var10002 = this.title;
      int var10003 = this.width / 2;
      int var10004 = this.height / 2 - this.reasonHeight / 2;
      this.textRenderer.getClass();
      drawCenteredText(matrices, var10001, var10002, var10003, var10004 - 9 * 2, 11184810);
      this.reasonFormatted.drawCenterWithShadow(matrices, this.width / 2, this.height / 2 - this.reasonHeight / 2);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
