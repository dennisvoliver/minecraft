package net.minecraft.client.network;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ClientAdvancementManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftClient client;
   private final AdvancementManager manager = new AdvancementManager();
   private final Map<Advancement, AdvancementProgress> advancementProgresses = Maps.newHashMap();
   @Nullable
   private ClientAdvancementManager.Listener listener;
   @Nullable
   private Advancement selectedTab;

   public ClientAdvancementManager(MinecraftClient client) {
      this.client = client;
   }

   public void onAdvancements(AdvancementUpdateS2CPacket packet) {
      if (packet.shouldClearCurrent()) {
         this.manager.clear();
         this.advancementProgresses.clear();
      }

      this.manager.removeAll(packet.getAdvancementIdsToRemove());
      this.manager.load(packet.getAdvancementsToEarn());
      Iterator var2 = packet.getAdvancementsToProgress().entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Identifier, AdvancementProgress> entry = (Entry)var2.next();
         Advancement advancement = this.manager.get((Identifier)entry.getKey());
         if (advancement != null) {
            AdvancementProgress advancementProgress = (AdvancementProgress)entry.getValue();
            advancementProgress.init(advancement.getCriteria(), advancement.getRequirements());
            this.advancementProgresses.put(advancement, advancementProgress);
            if (this.listener != null) {
               this.listener.setProgress(advancement, advancementProgress);
            }

            if (!packet.shouldClearCurrent() && advancementProgress.isDone() && advancement.getDisplay() != null && advancement.getDisplay().shouldShowToast()) {
               this.client.getToastManager().add(new AdvancementToast(advancement));
            }
         } else {
            LOGGER.warn("Server informed client about progress for unknown advancement {}", entry.getKey());
         }
      }

   }

   public AdvancementManager getManager() {
      return this.manager;
   }

   public void selectTab(@Nullable Advancement tab, boolean local) {
      ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
      if (clientPlayNetworkHandler != null && tab != null && local) {
         clientPlayNetworkHandler.sendPacket(AdvancementTabC2SPacket.open(tab));
      }

      if (this.selectedTab != tab) {
         this.selectedTab = tab;
         if (this.listener != null) {
            this.listener.selectTab(tab);
         }
      }

   }

   public void setListener(@Nullable ClientAdvancementManager.Listener listener) {
      this.listener = listener;
      this.manager.setListener(listener);
      if (listener != null) {
         Iterator var2 = this.advancementProgresses.entrySet().iterator();

         while(var2.hasNext()) {
            Entry<Advancement, AdvancementProgress> entry = (Entry)var2.next();
            listener.setProgress((Advancement)entry.getKey(), (AdvancementProgress)entry.getValue());
         }

         listener.selectTab(this.selectedTab);
      }

   }

   @Environment(EnvType.CLIENT)
   public interface Listener extends AdvancementManager.Listener {
      void setProgress(Advancement advancement, AdvancementProgress progress);

      void selectTab(@Nullable Advancement advancement);
   }
}
