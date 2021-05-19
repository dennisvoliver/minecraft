package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class DemoScreen extends Screen {
   private static final Identifier DEMO_BG = new Identifier("textures/gui/demo_background.png");
   private MultilineText field_26538;
   private MultilineText field_26539;

   public DemoScreen() {
      super(new TranslatableText("demo.help.title"));
      this.field_26538 = MultilineText.EMPTY;
      this.field_26539 = MultilineText.EMPTY;
   }

   protected void init() {
      int i = true;
      this.addButton(new ButtonWidget(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20, new TranslatableText("demo.help.buy"), (buttonWidget) -> {
         buttonWidget.active = false;
         Util.getOperatingSystem().open("http://www.minecraft.net/store?source=demo");
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20, new TranslatableText("demo.help.later"), (buttonWidget) -> {
         this.client.openScreen((Screen)null);
         this.client.mouse.lockCursor();
      }));
      GameOptions gameOptions = this.client.options;
      this.field_26538 = MultilineText.create(this.textRenderer, new TranslatableText("demo.help.movementShort", new Object[]{gameOptions.keyForward.getBoundKeyLocalizedText(), gameOptions.keyLeft.getBoundKeyLocalizedText(), gameOptions.keyBack.getBoundKeyLocalizedText(), gameOptions.keyRight.getBoundKeyLocalizedText()}), new TranslatableText("demo.help.movementMouse"), new TranslatableText("demo.help.jump", new Object[]{gameOptions.keyJump.getBoundKeyLocalizedText()}), new TranslatableText("demo.help.inventory", new Object[]{gameOptions.keyInventory.getBoundKeyLocalizedText()}));
      this.field_26539 = MultilineText.create(this.textRenderer, new TranslatableText("demo.help.fullWrapped"), 218);
   }

   public void renderBackground(MatrixStack matrices) {
      super.renderBackground(matrices);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(DEMO_BG);
      int i = (this.width - 248) / 2;
      int j = (this.height - 166) / 2;
      this.drawTexture(matrices, i, j, 0, 0, 248, 166);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      int i = (this.width - 248) / 2 + 10;
      int j = (this.height - 166) / 2 + 8;
      this.textRenderer.draw(matrices, this.title, (float)i, (float)j, 2039583);
      j = this.field_26538.draw(matrices, i, j + 12, 12, 5197647);
      MultilineText var10000 = this.field_26539;
      int var10003 = j + 20;
      this.textRenderer.getClass();
      var10000.draw(matrices, i, var10003, 9, 2039583);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
