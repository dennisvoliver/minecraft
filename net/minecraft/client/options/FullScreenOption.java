package net.minecraft.client.options;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class FullScreenOption extends DoubleOption {
   public FullScreenOption(Window window) {
      this(window, window.getMonitor());
   }

   private FullScreenOption(Window window, @Nullable Monitor monitor) {
      super("options.fullscreen.resolution", -1.0D, monitor != null ? (double)(monitor.getVideoModeCount() - 1) : -1.0D, 1.0F, (gameOptions) -> {
         if (monitor == null) {
            return -1.0D;
         } else {
            Optional<VideoMode> optional = window.getVideoMode();
            return (Double)optional.map((videoMode) -> {
               return (double)monitor.findClosestVideoModeIndex(videoMode);
            }).orElse(-1.0D);
         }
      }, (gameOptions, double_) -> {
         if (monitor != null) {
            if (double_ == -1.0D) {
               window.setVideoMode(Optional.empty());
            } else {
               window.setVideoMode(Optional.of(monitor.getVideoMode(double_.intValue())));
            }

         }
      }, (gameOptions, doubleOption) -> {
         if (monitor == null) {
            return new TranslatableText("options.fullscreen.unavailable");
         } else {
            double d = doubleOption.get(gameOptions);
            return d == -1.0D ? doubleOption.getGenericLabel(new TranslatableText("options.fullscreen.current")) : doubleOption.getGenericLabel(new LiteralText(monitor.getVideoMode((int)d).toString()));
         }
      });
   }
}
