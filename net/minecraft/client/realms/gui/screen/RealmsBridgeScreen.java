package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RealmsBridgeScreen extends RealmsScreen {
   private Screen previousScreen;

   public void switchToRealms(Screen parentScreen) {
      this.previousScreen = parentScreen;
      MinecraftClient.getInstance().openScreen(new RealmsMainScreen(this));
   }

   @Nullable
   public RealmsScreen getNotificationScreen(Screen parentScreen) {
      this.previousScreen = parentScreen;
      return new RealmsNotificationsScreen();
   }

   public void init() {
      MinecraftClient.getInstance().openScreen(this.previousScreen);
   }
}
