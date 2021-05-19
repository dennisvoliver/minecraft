package net.minecraft.client.gui.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ConfirmScreen extends Screen {
   private final Text message;
   private MultilineText messageSplit;
   protected Text yesTranslated;
   protected Text noTranslated;
   private int buttonEnableTimer;
   protected final BooleanConsumer callback;

   public ConfirmScreen(BooleanConsumer callback, Text title, Text message) {
      this(callback, title, message, ScreenTexts.YES, ScreenTexts.NO);
   }

   public ConfirmScreen(BooleanConsumer callback, Text title, Text message, Text text, Text text2) {
      super(title);
      this.messageSplit = MultilineText.EMPTY;
      this.callback = callback;
      this.message = message;
      this.yesTranslated = text;
      this.noTranslated = text2;
   }

   public String getNarrationMessage() {
      return super.getNarrationMessage() + ". " + this.message.getString();
   }

   protected void init() {
      super.init();
      this.addButton(new ButtonWidget(this.width / 2 - 155, this.height / 6 + 96, 150, 20, this.yesTranslated, (buttonWidget) -> {
         this.callback.accept(true);
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, this.noTranslated, (buttonWidget) -> {
         this.callback.accept(false);
      }));
      this.messageSplit = MultilineText.create(this.textRenderer, this.message, this.width - 50);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 70, 16777215);
      this.messageSplit.drawCenterWithShadow(matrices, this.width / 2, 90);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public void disableButtons(int i) {
      this.buttonEnableTimer = i;

      AbstractButtonWidget abstractButtonWidget;
      for(Iterator var2 = this.buttons.iterator(); var2.hasNext(); abstractButtonWidget.active = false) {
         abstractButtonWidget = (AbstractButtonWidget)var2.next();
      }

   }

   public void tick() {
      super.tick();
      AbstractButtonWidget abstractButtonWidget;
      if (--this.buttonEnableTimer == 0) {
         for(Iterator var1 = this.buttons.iterator(); var1.hasNext(); abstractButtonWidget.active = true) {
            abstractButtonWidget = (AbstractButtonWidget)var1.next();
         }
      }

   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.callback.accept(false);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
