package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class AbstractParentElement extends DrawableHelper implements ParentElement {
   @Nullable
   private Element focused;
   private boolean dragging;

   public final boolean isDragging() {
      return this.dragging;
   }

   public final void setDragging(boolean dragging) {
      this.dragging = dragging;
   }

   @Nullable
   public Element getFocused() {
      return this.focused;
   }

   public void setFocused(@Nullable Element focused) {
      this.focused = focused;
   }
}
