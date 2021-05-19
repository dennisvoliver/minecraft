package net.minecraft.client.gui;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

/**
 * A GUI interface which handles keyboard and mouse callbacks for child GUI elements.
 * The implementation of a parent element can decide whether a child element receives keyboard and mouse callbacks.
 */
@Environment(EnvType.CLIENT)
public interface ParentElement extends Element {
   /**
    * Gets a list of all child GUI elements.
    */
   List<? extends Element> children();

   default Optional<Element> hoveredElement(double mouseX, double mouseY) {
      Iterator var5 = this.children().iterator();

      Element element;
      do {
         if (!var5.hasNext()) {
            return Optional.empty();
         }

         element = (Element)var5.next();
      } while(!element.isMouseOver(mouseX, mouseY));

      return Optional.of(element);
   }

   default boolean mouseClicked(double mouseX, double mouseY, int button) {
      Iterator var6 = this.children().iterator();

      Element element;
      do {
         if (!var6.hasNext()) {
            return false;
         }

         element = (Element)var6.next();
      } while(!element.mouseClicked(mouseX, mouseY, button));

      this.setFocused(element);
      if (button == 0) {
         this.setDragging(true);
      }

      return true;
   }

   default boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.setDragging(false);
      return this.hoveredElement(mouseX, mouseY).filter((element) -> {
         return element.mouseReleased(mouseX, mouseY, button);
      }).isPresent();
   }

   default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.getFocused() != null && this.isDragging() && button == 0 ? this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY) : false;
   }

   boolean isDragging();

   void setDragging(boolean dragging);

   default boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.hoveredElement(mouseX, mouseY).filter((element) -> {
         return element.mouseScrolled(mouseX, mouseY, amount);
      }).isPresent();
   }

   default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
   }

   default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      return this.getFocused() != null && this.getFocused().keyReleased(keyCode, scanCode, modifiers);
   }

   default boolean charTyped(char chr, int modifiers) {
      return this.getFocused() != null && this.getFocused().charTyped(chr, modifiers);
   }

   @Nullable
   Element getFocused();

   void setFocused(@Nullable Element focused);

   default void setInitialFocus(@Nullable Element element) {
      this.setFocused(element);
      element.changeFocus(true);
   }

   default void focusOn(@Nullable Element element) {
      this.setFocused(element);
   }

   default boolean changeFocus(boolean lookForwards) {
      Element element = this.getFocused();
      boolean bl = element != null;
      if (bl && element.changeFocus(lookForwards)) {
         return true;
      } else {
         List<? extends Element> list = this.children();
         int i = list.indexOf(element);
         int l;
         if (bl && i >= 0) {
            l = i + (lookForwards ? 1 : 0);
         } else if (lookForwards) {
            l = 0;
         } else {
            l = list.size();
         }

         ListIterator<? extends Element> listIterator = list.listIterator(l);
         BooleanSupplier booleanSupplier = lookForwards ? listIterator::hasNext : listIterator::hasPrevious;
         Supplier supplier = lookForwards ? listIterator::next : listIterator::previous;

         Element element2;
         do {
            if (!booleanSupplier.getAsBoolean()) {
               this.setFocused((Element)null);
               return false;
            }

            element2 = (Element)supplier.get();
         } while(!element2.changeFocus(lookForwards));

         this.setFocused(element2);
         return true;
      }
   }
}
