package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BackupPromptScreen extends Screen {
   @Nullable
   private final Screen parent;
   protected final BackupPromptScreen.Callback callback;
   private final Text subtitle;
   private final boolean showEraseCacheCheckbox;
   private MultilineText wrappedText;
   private CheckboxWidget eraseCacheCheckbox;

   public BackupPromptScreen(@Nullable Screen parent, BackupPromptScreen.Callback callback, Text title, Text subtitle, boolean showEraseCacheCheckBox) {
      super(title);
      this.wrappedText = MultilineText.EMPTY;
      this.parent = parent;
      this.callback = callback;
      this.subtitle = subtitle;
      this.showEraseCacheCheckbox = showEraseCacheCheckBox;
   }

   protected void init() {
      super.init();
      this.wrappedText = MultilineText.create(this.textRenderer, this.subtitle, this.width - 50);
      int var10000 = this.wrappedText.count() + 1;
      this.textRenderer.getClass();
      int i = var10000 * 9;
      this.addButton(new ButtonWidget(this.width / 2 - 155, 100 + i, 150, 20, new TranslatableText("selectWorld.backupJoinConfirmButton"), (buttonWidget) -> {
         this.callback.proceed(true, this.eraseCacheCheckbox.isChecked());
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, 100 + i, 150, 20, new TranslatableText("selectWorld.backupJoinSkipButton"), (buttonWidget) -> {
         this.callback.proceed(false, this.eraseCacheCheckbox.isChecked());
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 155 + 80, 124 + i, 150, 20, ScreenTexts.CANCEL, (buttonWidget) -> {
         this.client.openScreen(this.parent);
      }));
      this.eraseCacheCheckbox = new CheckboxWidget(this.width / 2 - 155 + 80, 76 + i, 150, 20, new TranslatableText("selectWorld.backupEraseCache"), false);
      if (this.showEraseCacheCheckbox) {
         this.addButton(this.eraseCacheCheckbox);
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 50, 16777215);
      this.wrappedText.drawCenterWithShadow(matrices, this.width / 2, 70);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.client.openScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   @Environment(EnvType.CLIENT)
   public interface Callback {
      void proceed(boolean bl, boolean bl2);
   }
}
