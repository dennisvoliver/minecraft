package net.minecraft.client.toast;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ToastManager extends DrawableHelper {
   private final MinecraftClient client;
   private final ToastManager.Entry<?>[] visibleEntries = new ToastManager.Entry[5];
   private final Deque<Toast> toastQueue = Queues.newArrayDeque();

   public ToastManager(MinecraftClient client) {
      this.client = client;
   }

   public void draw(MatrixStack matrices) {
      if (!this.client.options.hudHidden) {
         for(int i = 0; i < this.visibleEntries.length; ++i) {
            ToastManager.Entry<?> entry = this.visibleEntries[i];
            if (entry != null && entry.draw(this.client.getWindow().getScaledWidth(), i, matrices)) {
               this.visibleEntries[i] = null;
            }

            if (this.visibleEntries[i] == null && !this.toastQueue.isEmpty()) {
               this.visibleEntries[i] = new ToastManager.Entry((Toast)this.toastQueue.removeFirst());
            }
         }

      }
   }

   @Nullable
   public <T extends Toast> T getToast(Class<? extends T> toastClass, Object type) {
      ToastManager.Entry[] var3 = this.visibleEntries;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ToastManager.Entry<?> entry = var3[var5];
         if (entry != null && toastClass.isAssignableFrom(entry.getInstance().getClass()) && entry.getInstance().getType().equals(type)) {
            return entry.getInstance();
         }
      }

      Iterator var7 = this.toastQueue.iterator();

      Toast toast;
      do {
         if (!var7.hasNext()) {
            return null;
         }

         toast = (Toast)var7.next();
      } while(!toastClass.isAssignableFrom(toast.getClass()) || !toast.getType().equals(type));

      return toast;
   }

   public void clear() {
      Arrays.fill(this.visibleEntries, (Object)null);
      this.toastQueue.clear();
   }

   public void add(Toast toast) {
      this.toastQueue.add(toast);
   }

   public MinecraftClient getGame() {
      return this.client;
   }

   @Environment(EnvType.CLIENT)
   class Entry<T extends Toast> {
      private final T instance;
      private long field_2243;
      private long field_2242;
      private Toast.Visibility visibility;

      private Entry(T toast) {
         this.field_2243 = -1L;
         this.field_2242 = -1L;
         this.visibility = Toast.Visibility.SHOW;
         this.instance = toast;
      }

      public T getInstance() {
         return this.instance;
      }

      private float getDisappearProgress(long time) {
         float f = MathHelper.clamp((float)(time - this.field_2243) / 600.0F, 0.0F, 1.0F);
         f *= f;
         return this.visibility == Toast.Visibility.HIDE ? 1.0F - f : f;
      }

      public boolean draw(int x, int y, MatrixStack matrices) {
         long l = Util.getMeasuringTimeMs();
         if (this.field_2243 == -1L) {
            this.field_2243 = l;
            this.visibility.playSound(ToastManager.this.client.getSoundManager());
         }

         if (this.visibility == Toast.Visibility.SHOW && l - this.field_2243 <= 600L) {
            this.field_2242 = l;
         }

         RenderSystem.pushMatrix();
         RenderSystem.translatef((float)x - (float)this.instance.getWidth() * this.getDisappearProgress(l), (float)(y * this.instance.getHeight()), (float)(800 + y));
         Toast.Visibility visibility = this.instance.draw(matrices, ToastManager.this, l - this.field_2242);
         RenderSystem.popMatrix();
         if (visibility != this.visibility) {
            this.field_2243 = l - (long)((int)((1.0F - this.getDisappearProgress(l)) * 600.0F));
            this.visibility = visibility;
            this.visibility.playSound(ToastManager.this.client.getSoundManager());
         }

         return this.visibility == Toast.Visibility.HIDE && l - this.field_2243 > 600L;
      }
   }
}
