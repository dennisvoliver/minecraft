package net.minecraft.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowSettings;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class WindowProvider implements AutoCloseable {
   private final MinecraftClient client;
   private final MonitorTracker monitorTracker;

   public WindowProvider(MinecraftClient client) {
      this.client = client;
      this.monitorTracker = new MonitorTracker(Monitor::new);
   }

   public Window createWindow(WindowSettings settings, @Nullable String videoMode, String title) {
      return new Window(this.client, this.monitorTracker, settings, videoMode, title);
   }

   public void close() {
      this.monitorTracker.stop();
   }
}
