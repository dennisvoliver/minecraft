package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.Realms;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
   private final Text title;
   private final Text reason;
   private MultilineText lines;
   private final Screen parent;
   private int textHeight;

   public DisconnectedRealmsScreen(Screen parent, Text text, Text reason) {
      this.lines = MultilineText.EMPTY;
      this.parent = parent;
      this.title = text;
      this.reason = reason;
   }

   public void init() {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      minecraftClient.setConnectedToRealms(false);
      minecraftClient.getResourcePackDownloader().clear();
      Realms.narrateNow(this.title.getString() + ": " + this.reason.getString());
      this.lines = MultilineText.create(this.textRenderer, this.reason, this.width - 50);
      int var10001 = this.lines.count();
      this.textRenderer.getClass();
      this.textHeight = var10001 * 9;
      int var10003 = this.width / 2 - 100;
      int var10004 = this.height / 2 + this.textHeight / 2;
      this.textRenderer.getClass();
      this.addButton(new ButtonWidget(var10003, var10004 + 9, 200, 20, ScreenTexts.BACK, (buttonWidget) -> {
         minecraftClient.openScreen(this.parent);
      }));
   }

   public void onClose() {
      MinecraftClient.getInstance().openScreen(this.parent);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      TextRenderer var10001 = this.textRenderer;
      Text var10002 = this.title;
      int var10003 = this.width / 2;
      int var10004 = this.height / 2 - this.textHeight / 2;
      this.textRenderer.getClass();
      drawCenteredText(matrices, var10001, var10002, var10003, var10004 - 9 * 2, 11184810);
      this.lines.drawCenterWithShadow(matrices, this.width / 2, this.height / 2 - this.textHeight / 2);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
