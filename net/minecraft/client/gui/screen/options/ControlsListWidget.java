package net.minecraft.client.gui.screen.options;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public class ControlsListWidget extends ElementListWidget<ControlsListWidget.Entry> {
   private final ControlsOptionsScreen parent;
   private int maxKeyNameLength;

   public ControlsListWidget(ControlsOptionsScreen parent, MinecraftClient client) {
      super(client, parent.width + 45, parent.height, 43, parent.height - 32, 20);
      this.parent = parent;
      KeyBinding[] keyBindings = (KeyBinding[])ArrayUtils.clone((Object[])client.options.keysAll);
      Arrays.sort(keyBindings);
      String string = null;
      KeyBinding[] var5 = keyBindings;
      int var6 = keyBindings.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         KeyBinding keyBinding = var5[var7];
         String string2 = keyBinding.getCategory();
         if (!string2.equals(string)) {
            string = string2;
            this.addEntry(new ControlsListWidget.CategoryEntry(new TranslatableText(string2)));
         }

         Text text = new TranslatableText(keyBinding.getTranslationKey());
         int i = client.textRenderer.getWidth((StringVisitable)text);
         if (i > this.maxKeyNameLength) {
            this.maxKeyNameLength = i;
         }

         this.addEntry(new ControlsListWidget.KeyBindingEntry(keyBinding, text));
      }

   }

   protected int getScrollbarPositionX() {
      return super.getScrollbarPositionX() + 15;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 32;
   }

   @Environment(EnvType.CLIENT)
   public class KeyBindingEntry extends ControlsListWidget.Entry {
      private final KeyBinding binding;
      private final Text bindingName;
      private final ButtonWidget editButton;
      private final ButtonWidget resetButton;

      private KeyBindingEntry(final KeyBinding binding, final Text text) {
         this.binding = binding;
         this.bindingName = text;
         this.editButton = new ButtonWidget(0, 0, 75, 20, text, (button) -> {
            ControlsListWidget.this.parent.focusedBinding = binding;
         }) {
            protected MutableText getNarrationMessage() {
               return binding.isUnbound() ? new TranslatableText("narrator.controls.unbound", new Object[]{text}) : new TranslatableText("narrator.controls.bound", new Object[]{text, super.getNarrationMessage()});
            }
         };
         this.resetButton = new ButtonWidget(0, 0, 50, 20, new TranslatableText("controls.reset"), (button) -> {
            ControlsListWidget.this.client.options.setKeyCode(binding, binding.getDefaultKey());
            KeyBinding.updateKeysByCode();
         }) {
            protected MutableText getNarrationMessage() {
               return new TranslatableText("narrator.controls.reset", new Object[]{text});
            }
         };
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         boolean bl = ControlsListWidget.this.parent.focusedBinding == this.binding;
         TextRenderer var10000 = ControlsListWidget.this.client.textRenderer;
         Text var10002 = this.bindingName;
         float var10003 = (float)(x + 90 - ControlsListWidget.this.maxKeyNameLength);
         int var10004 = y + entryHeight / 2;
         ControlsListWidget.this.client.textRenderer.getClass();
         var10000.draw(matrices, var10002, var10003, (float)(var10004 - 9 / 2), 16777215);
         this.resetButton.x = x + 190;
         this.resetButton.y = y;
         this.resetButton.active = !this.binding.isDefault();
         this.resetButton.render(matrices, mouseX, mouseY, tickDelta);
         this.editButton.x = x + 105;
         this.editButton.y = y;
         this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
         boolean bl2 = false;
         if (!this.binding.isUnbound()) {
            KeyBinding[] var13 = ControlsListWidget.this.client.options.keysAll;
            int var14 = var13.length;

            for(int var15 = 0; var15 < var14; ++var15) {
               KeyBinding keyBinding = var13[var15];
               if (keyBinding != this.binding && this.binding.equals(keyBinding)) {
                  bl2 = true;
                  break;
               }
            }
         }

         if (bl) {
            this.editButton.setMessage((new LiteralText("> ")).append(this.editButton.getMessage().shallowCopy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW));
         } else if (bl2) {
            this.editButton.setMessage(this.editButton.getMessage().shallowCopy().formatted(Formatting.RED));
         }

         this.editButton.render(matrices, mouseX, mouseY, tickDelta);
      }

      public List<? extends Element> children() {
         return ImmutableList.of(this.editButton, this.resetButton);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (this.editButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
         } else {
            return this.resetButton.mouseClicked(mouseX, mouseY, button);
         }
      }

      public boolean mouseReleased(double mouseX, double mouseY, int button) {
         return this.editButton.mouseReleased(mouseX, mouseY, button) || this.resetButton.mouseReleased(mouseX, mouseY, button);
      }
   }

   @Environment(EnvType.CLIENT)
   public class CategoryEntry extends ControlsListWidget.Entry {
      private final Text text;
      private final int textWidth;

      public CategoryEntry(Text text) {
         this.text = text;
         this.textWidth = ControlsListWidget.this.client.textRenderer.getWidth((StringVisitable)this.text);
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         TextRenderer var10000 = ControlsListWidget.this.client.textRenderer;
         Text var10002 = this.text;
         float var10003 = (float)(ControlsListWidget.this.client.currentScreen.width / 2 - this.textWidth / 2);
         int var10004 = y + entryHeight;
         ControlsListWidget.this.client.textRenderer.getClass();
         var10000.draw(matrices, var10002, var10003, (float)(var10004 - 9 - 1), 16777215);
      }

      public boolean changeFocus(boolean lookForwards) {
         return false;
      }

      public List<? extends Element> children() {
         return Collections.emptyList();
      }
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends ElementListWidget.Entry<ControlsListWidget.Entry> {
   }
}
