package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ProgressListener;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ProgressScreen extends Screen implements ProgressListener {
   @Nullable
   private Text title;
   @Nullable
   private Text task;
   private int progress;
   private boolean done;

   public ProgressScreen() {
      super(NarratorManager.EMPTY);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void method_15412(Text text) {
      this.method_15413(text);
   }

   public void method_15413(Text text) {
      this.title = text;
      this.method_15414(new TranslatableText("progress.working"));
   }

   public void method_15414(Text text) {
      this.task = text;
      this.progressStagePercentage(0);
   }

   public void progressStagePercentage(int i) {
      this.progress = i;
   }

   public void setDone() {
      this.done = true;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.done) {
         if (!this.client.isConnectedToRealms()) {
            this.client.openScreen((Screen)null);
         }

      } else {
         this.renderBackground(matrices);
         if (this.title != null) {
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 70, 16777215);
         }

         if (this.task != null && this.progress != 0) {
            drawCenteredText(matrices, this.textRenderer, (new LiteralText("")).append(this.task).append(" " + this.progress + "%"), this.width / 2, 90, 16777215);
         }

         super.render(matrices, mouseX, mouseY, delta);
      }
   }
}
