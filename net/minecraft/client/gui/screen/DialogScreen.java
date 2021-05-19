package net.minecraft.client.gui.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class DialogScreen extends Screen {
   private final StringVisitable message;
   private final ImmutableList<DialogScreen.ChoiceButton> choiceButtons;
   private MultilineText lines;
   private int linesY;
   private int buttonWidth;

   protected DialogScreen(Text title, List<StringVisitable> list, ImmutableList<DialogScreen.ChoiceButton> choiceButtons) {
      super(title);
      this.lines = MultilineText.EMPTY;
      this.message = StringVisitable.concat(list);
      this.choiceButtons = choiceButtons;
   }

   public String getNarrationMessage() {
      return super.getNarrationMessage() + ". " + this.message.getString();
   }

   public void init(MinecraftClient client, int width, int height) {
      super.init(client, width, height);

      DialogScreen.ChoiceButton choiceButton;
      for(UnmodifiableIterator var4 = this.choiceButtons.iterator(); var4.hasNext(); this.buttonWidth = Math.max(this.buttonWidth, 20 + this.textRenderer.getWidth((StringVisitable)choiceButton.message) + 20)) {
         choiceButton = (DialogScreen.ChoiceButton)var4.next();
      }

      int i = 5 + this.buttonWidth + 5;
      int j = i * this.choiceButtons.size();
      this.lines = MultilineText.create(this.textRenderer, this.message, j);
      int var10000 = this.lines.count();
      this.textRenderer.getClass();
      int k = var10000 * 9;
      this.linesY = (int)((double)height / 2.0D - (double)k / 2.0D);
      var10000 = this.linesY + k;
      this.textRenderer.getClass();
      int l = var10000 + 9 * 2;
      int m = (int)((double)width / 2.0D - (double)j / 2.0D);

      for(UnmodifiableIterator var9 = this.choiceButtons.iterator(); var9.hasNext(); m += i) {
         DialogScreen.ChoiceButton choiceButton2 = (DialogScreen.ChoiceButton)var9.next();
         this.addButton(new ButtonWidget(m, l, this.buttonWidth, 20, choiceButton2.message, choiceButton2.pressAction));
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackgroundTexture(0);
      TextRenderer var10001 = this.textRenderer;
      Text var10002 = this.title;
      int var10003 = this.width / 2;
      int var10004 = this.linesY;
      this.textRenderer.getClass();
      drawCenteredText(matrices, var10001, var10002, var10003, var10004 - 9 * 2, -1);
      this.lines.drawCenterWithShadow(matrices, this.width / 2, this.linesY);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Environment(EnvType.CLIENT)
   public static final class ChoiceButton {
      private final Text message;
      private final ButtonWidget.PressAction pressAction;

      public ChoiceButton(Text message, ButtonWidget.PressAction pressAction) {
         this.message = message;
         this.pressAction = pressAction;
      }
   }
}
