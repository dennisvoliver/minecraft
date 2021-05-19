package net.minecraft.client.realms.gui.screen;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public abstract class RealmsAcceptRejectButton {
   public final int width;
   public final int height;
   public final int x;
   public final int y;

   public RealmsAcceptRejectButton(int width, int height, int x, int y) {
      this.width = width;
      this.height = height;
      this.x = x;
      this.y = y;
   }

   public void render(MatrixStack matrices, int i, int j, int k, int l) {
      int m = i + this.x;
      int n = j + this.y;
      boolean bl = false;
      if (k >= m && k <= m + this.width && l >= n && l <= n + this.height) {
         bl = true;
      }

      this.render(matrices, m, n, bl);
   }

   protected abstract void render(MatrixStack matrixStack, int y, int i, boolean bl);

   public int getRight() {
      return this.x + this.width;
   }

   public int getBottom() {
      return this.y + this.height;
   }

   public abstract void handleClick(int index);

   public static void render(MatrixStack matrices, List<RealmsAcceptRejectButton> list, RealmsObjectSelectionList<?> realmsObjectSelectionList, int i, int j, int k, int l) {
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         RealmsAcceptRejectButton realmsAcceptRejectButton = (RealmsAcceptRejectButton)var7.next();
         if (realmsObjectSelectionList.getRowWidth() > realmsAcceptRejectButton.getRight()) {
            realmsAcceptRejectButton.render(matrices, i, j, k, l);
         }
      }

   }

   public static void handleClick(RealmsObjectSelectionList<?> selectionList, AlwaysSelectedEntryListWidget.Entry<?> entry, List<RealmsAcceptRejectButton> buttons, int button, double mouseX, double mouseY) {
      if (button == 0) {
         int i = selectionList.children().indexOf(entry);
         if (i > -1) {
            selectionList.setSelected(i);
            int j = selectionList.getRowLeft();
            int k = selectionList.getRowTop(i);
            int l = (int)(mouseX - (double)j);
            int m = (int)(mouseY - (double)k);
            Iterator var13 = buttons.iterator();

            while(var13.hasNext()) {
               RealmsAcceptRejectButton realmsAcceptRejectButton = (RealmsAcceptRejectButton)var13.next();
               if (l >= realmsAcceptRejectButton.x && l <= realmsAcceptRejectButton.getRight() && m >= realmsAcceptRejectButton.y && m <= realmsAcceptRejectButton.getBottom()) {
                  realmsAcceptRejectButton.handleClick(i);
               }
            }
         }
      }

   }
}
