package net.minecraft.client.gui.widget;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ButtonListWidget extends ElementListWidget<ButtonListWidget.ButtonEntry> {
   public ButtonListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
      super(minecraftClient, i, j, k, l, m);
      this.centerListVertically = false;
   }

   public int addSingleOptionEntry(Option option) {
      return this.addEntry(ButtonListWidget.ButtonEntry.create(this.client.options, this.width, option));
   }

   public void addOptionEntry(Option firstOption, @Nullable Option secondOption) {
      this.addEntry(ButtonListWidget.ButtonEntry.create(this.client.options, this.width, firstOption, secondOption));
   }

   public void addAll(Option[] options) {
      for(int i = 0; i < options.length; i += 2) {
         this.addOptionEntry(options[i], i < options.length - 1 ? options[i + 1] : null);
      }

   }

   public int getRowWidth() {
      return 400;
   }

   protected int getScrollbarPositionX() {
      return super.getScrollbarPositionX() + 32;
   }

   @Nullable
   public AbstractButtonWidget getButtonFor(Option option) {
      Iterator var2 = this.children().iterator();

      while(var2.hasNext()) {
         ButtonListWidget.ButtonEntry buttonEntry = (ButtonListWidget.ButtonEntry)var2.next();
         Iterator var4 = buttonEntry.buttons.iterator();

         while(var4.hasNext()) {
            AbstractButtonWidget abstractButtonWidget = (AbstractButtonWidget)var4.next();
            if (abstractButtonWidget instanceof OptionButtonWidget && ((OptionButtonWidget)abstractButtonWidget).getOption() == option) {
               return abstractButtonWidget;
            }
         }
      }

      return null;
   }

   public Optional<AbstractButtonWidget> getHoveredButton(double mouseX, double mouseY) {
      Iterator var5 = this.children().iterator();

      while(var5.hasNext()) {
         ButtonListWidget.ButtonEntry buttonEntry = (ButtonListWidget.ButtonEntry)var5.next();
         Iterator var7 = buttonEntry.buttons.iterator();

         while(var7.hasNext()) {
            AbstractButtonWidget abstractButtonWidget = (AbstractButtonWidget)var7.next();
            if (abstractButtonWidget.isMouseOver(mouseX, mouseY)) {
               return Optional.of(abstractButtonWidget);
            }
         }
      }

      return Optional.empty();
   }

   @Environment(EnvType.CLIENT)
   public static class ButtonEntry extends ElementListWidget.Entry<ButtonListWidget.ButtonEntry> {
      private final List<AbstractButtonWidget> buttons;

      private ButtonEntry(List<AbstractButtonWidget> buttons) {
         this.buttons = buttons;
      }

      public static ButtonListWidget.ButtonEntry create(GameOptions options, int width, Option option) {
         return new ButtonListWidget.ButtonEntry(ImmutableList.of(option.createButton(options, width / 2 - 155, 0, 310)));
      }

      public static ButtonListWidget.ButtonEntry create(GameOptions options, int width, Option firstOption, @Nullable Option secondOption) {
         AbstractButtonWidget abstractButtonWidget = firstOption.createButton(options, width / 2 - 155, 0, 150);
         return secondOption == null ? new ButtonListWidget.ButtonEntry(ImmutableList.of(abstractButtonWidget)) : new ButtonListWidget.ButtonEntry(ImmutableList.of(abstractButtonWidget, secondOption.createButton(options, width / 2 - 155 + 160, 0, 150)));
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.buttons.forEach((button) -> {
            button.y = y;
            button.render(matrices, mouseX, mouseY, tickDelta);
         });
      }

      public List<? extends Element> children() {
         return this.buttons;
      }
   }
}
