package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class ElementListWidget<E extends ElementListWidget.Entry<E>> extends EntryListWidget<E> {
   public ElementListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
      super(minecraftClient, i, j, k, l, m);
   }

   public boolean changeFocus(boolean lookForwards) {
      boolean bl = super.changeFocus(lookForwards);
      if (bl) {
         this.ensureVisible(this.getFocused());
      }

      return bl;
   }

   protected boolean isSelectedEntry(int index) {
      return false;
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry<E extends ElementListWidget.Entry<E>> extends EntryListWidget.Entry<E> implements ParentElement {
      @Nullable
      private Element focused;
      private boolean dragging;

      public boolean isDragging() {
         return this.dragging;
      }

      public void setDragging(boolean dragging) {
         this.dragging = dragging;
      }

      public void setFocused(@Nullable Element focused) {
         this.focused = focused;
      }

      @Nullable
      public Element getFocused() {
         return this.focused;
      }
   }
}
