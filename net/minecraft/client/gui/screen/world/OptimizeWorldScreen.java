package net.minecraft.client.gui.screen.world;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class OptimizeWorldScreen extends Screen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Object2IntMap<RegistryKey<World>> DIMENSION_COLORS = (Object2IntMap)Util.make(new Object2IntOpenCustomHashMap(Util.identityHashStrategy()), (object2IntOpenCustomHashMap) -> {
      object2IntOpenCustomHashMap.put(World.OVERWORLD, -13408734);
      object2IntOpenCustomHashMap.put(World.NETHER, -10075085);
      object2IntOpenCustomHashMap.put(World.END, -8943531);
      object2IntOpenCustomHashMap.defaultReturnValue(-2236963);
   });
   private final BooleanConsumer callback;
   private final WorldUpdater updater;

   @Nullable
   public static OptimizeWorldScreen create(MinecraftClient client, BooleanConsumer callback, DataFixer dataFixer, LevelStorage.Session storageSession, boolean eraseCache) {
      DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();

      try {
         MinecraftClient.IntegratedResourceManager integratedResourceManager = client.method_29604(impl, MinecraftClient::method_29598, MinecraftClient::createSaveProperties, false, storageSession);
         Throwable var7 = null;

         OptimizeWorldScreen var10;
         try {
            SaveProperties saveProperties = integratedResourceManager.getSaveProperties();
            storageSession.backupLevelDataFile(impl, saveProperties);
            ImmutableSet<RegistryKey<World>> immutableSet = saveProperties.getGeneratorOptions().getWorlds();
            var10 = new OptimizeWorldScreen(callback, dataFixer, storageSession, saveProperties.getLevelInfo(), eraseCache, immutableSet);
         } catch (Throwable var20) {
            var7 = var20;
            throw var20;
         } finally {
            if (integratedResourceManager != null) {
               if (var7 != null) {
                  try {
                     integratedResourceManager.close();
                  } catch (Throwable var19) {
                     var7.addSuppressed(var19);
                  }
               } else {
                  integratedResourceManager.close();
               }
            }

         }

         return var10;
      } catch (Exception var22) {
         LOGGER.warn((String)"Failed to load datapacks, can't optimize world", (Throwable)var22);
         return null;
      }
   }

   private OptimizeWorldScreen(BooleanConsumer callback, DataFixer dataFixer, LevelStorage.Session storageSession, LevelInfo levelInfo, boolean eraseCache, ImmutableSet<RegistryKey<World>> worlds) {
      super(new TranslatableText("optimizeWorld.title", new Object[]{levelInfo.getLevelName()}));
      this.callback = callback;
      this.updater = new WorldUpdater(storageSession, dataFixer, worlds, eraseCache);
   }

   protected void init() {
      super.init();
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 150, 200, 20, ScreenTexts.CANCEL, (buttonWidget) -> {
         this.updater.cancel();
         this.callback.accept(false);
      }));
   }

   public void tick() {
      if (this.updater.isDone()) {
         this.callback.accept(true);
      }

   }

   public void onClose() {
      this.callback.accept(false);
   }

   public void removed() {
      this.updater.cancel();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
      int i = this.width / 2 - 150;
      int j = this.width / 2 + 150;
      int k = this.height / 4 + 100;
      int l = k + 10;
      TextRenderer var10001 = this.textRenderer;
      Text var10002 = this.updater.getStatus();
      int var10003 = this.width / 2;
      this.textRenderer.getClass();
      drawCenteredText(matrices, var10001, var10002, var10003, k - 9 - 2, 10526880);
      if (this.updater.getTotalChunkCount() > 0) {
         fill(matrices, i - 1, k - 1, j + 1, l + 1, -16777216);
         drawTextWithShadow(matrices, this.textRenderer, new TranslatableText("optimizeWorld.info.converted", new Object[]{this.updater.getUpgradedChunkCount()}), i, 40, 10526880);
         var10001 = this.textRenderer;
         TranslatableText var14 = new TranslatableText("optimizeWorld.info.skipped", new Object[]{this.updater.getSkippedChunkCount()});
         this.textRenderer.getClass();
         drawTextWithShadow(matrices, var10001, var14, i, 40 + 9 + 3, 10526880);
         var10001 = this.textRenderer;
         var14 = new TranslatableText("optimizeWorld.info.total", new Object[]{this.updater.getTotalChunkCount()});
         this.textRenderer.getClass();
         drawTextWithShadow(matrices, var10001, var14, i, 40 + (9 + 3) * 2, 10526880);
         int m = 0;

         int n;
         for(UnmodifiableIterator var10 = this.updater.method_28304().iterator(); var10.hasNext(); m += n) {
            RegistryKey<World> registryKey = (RegistryKey)var10.next();
            n = MathHelper.floor(this.updater.getProgress(registryKey) * (float)(j - i));
            fill(matrices, i + m, k, i + m + n, l, DIMENSION_COLORS.getInt(registryKey));
         }

         int o = this.updater.getUpgradedChunkCount() + this.updater.getSkippedChunkCount();
         var10001 = this.textRenderer;
         String var15 = o + " / " + this.updater.getTotalChunkCount();
         var10003 = this.width / 2;
         this.textRenderer.getClass();
         drawCenteredString(matrices, var10001, var15, var10003, k + 2 * 9 + 2, 10526880);
         var10001 = this.textRenderer;
         var15 = MathHelper.floor(this.updater.getProgress() * 100.0F) + "%";
         var10003 = this.width / 2;
         int var10004 = k + (l - k) / 2;
         this.textRenderer.getClass();
         drawCenteredString(matrices, var10001, var15, var10003, var10004 - 9 / 2, 10526880);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }
}
