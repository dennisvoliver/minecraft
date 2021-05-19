package net.minecraft.client.gui.screen.pack;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PackScreen extends Screen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Text DROP_INFO;
   private static final Text FOLDER_INFO;
   private static final Identifier UNKNOWN_PACK;
   private final ResourcePackOrganizer organizer;
   private final Screen parent;
   @Nullable
   private PackScreen.DirectoryWatcher directoryWatcher;
   private long field_25788;
   private PackListWidget availablePackList;
   private PackListWidget selectedPackList;
   private final File file;
   private ButtonWidget doneButton;
   private final Map<String, Identifier> iconTextures = Maps.newHashMap();

   public PackScreen(Screen parent, ResourcePackManager packManager, Consumer<ResourcePackManager> consumer, File file, Text title) {
      super(title);
      this.parent = parent;
      this.organizer = new ResourcePackOrganizer(this::updatePackLists, this::getPackIconTexture, packManager, consumer);
      this.file = file;
      this.directoryWatcher = PackScreen.DirectoryWatcher.create(file);
   }

   public void onClose() {
      this.organizer.apply();
      this.client.openScreen(this.parent);
      this.closeDirectoryWatcher();
   }

   private void closeDirectoryWatcher() {
      if (this.directoryWatcher != null) {
         try {
            this.directoryWatcher.close();
            this.directoryWatcher = null;
         } catch (Exception var2) {
         }
      }

   }

   protected void init() {
      this.doneButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 48, 150, 20, ScreenTexts.DONE, (buttonWidget) -> {
         this.onClose();
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 48, 150, 20, new TranslatableText("pack.openFolder"), (buttonWidget) -> {
         Util.getOperatingSystem().open(this.file);
      }, (buttonWidget, matrixStack, i, j) -> {
         this.renderTooltip(matrixStack, FOLDER_INFO, i, j);
      }));
      this.availablePackList = new PackListWidget(this.client, 200, this.height, new TranslatableText("pack.available.title"));
      this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
      this.children.add(this.availablePackList);
      this.selectedPackList = new PackListWidget(this.client, 200, this.height, new TranslatableText("pack.selected.title"));
      this.selectedPackList.setLeftPos(this.width / 2 + 4);
      this.children.add(this.selectedPackList);
      this.refresh();
   }

   public void tick() {
      if (this.directoryWatcher != null) {
         try {
            if (this.directoryWatcher.pollForChange()) {
               this.field_25788 = 20L;
            }
         } catch (IOException var2) {
            LOGGER.warn((String)"Failed to poll for directory {} changes, stopping", (Object)this.file);
            this.closeDirectoryWatcher();
         }
      }

      if (this.field_25788 > 0L && --this.field_25788 == 0L) {
         this.refresh();
      }

   }

   private void updatePackLists() {
      this.updatePackList(this.selectedPackList, this.organizer.getEnabledPacks());
      this.updatePackList(this.availablePackList, this.organizer.getDisabledPacks());
      this.doneButton.active = !this.selectedPackList.children().isEmpty();
   }

   private void updatePackList(PackListWidget widget, Stream<ResourcePackOrganizer.Pack> packs) {
      widget.children().clear();
      packs.forEach((pack) -> {
         widget.children().add(new PackListWidget.ResourcePackEntry(this.client, widget, this, pack));
      });
   }

   private void refresh() {
      this.organizer.refresh();
      this.updatePackLists();
      this.field_25788 = 0L;
      this.iconTextures.clear();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackgroundTexture(0);
      this.availablePackList.render(matrices, mouseX, mouseY, delta);
      this.selectedPackList.render(matrices, mouseX, mouseY, delta);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
      drawCenteredText(matrices, this.textRenderer, DROP_INFO, this.width / 2, 20, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }

   protected static void copyPacks(MinecraftClient client, List<Path> srcPaths, Path destPath) {
      MutableBoolean mutableBoolean = new MutableBoolean();
      srcPaths.forEach((path2) -> {
         try {
            Stream<Path> stream = Files.walk(path2);
            Throwable var4 = null;

            try {
               stream.forEach((path3) -> {
                  try {
                     Util.relativeCopy(path2.getParent(), destPath, path3);
                  } catch (IOException var5) {
                     LOGGER.warn((String)"Failed to copy datapack file  from {} to {}", (Object)path3, destPath, var5);
                     mutableBoolean.setTrue();
                  }

               });
            } catch (Throwable var14) {
               var4 = var14;
               throw var14;
            } finally {
               if (stream != null) {
                  if (var4 != null) {
                     try {
                        stream.close();
                     } catch (Throwable var13) {
                        var4.addSuppressed(var13);
                     }
                  } else {
                     stream.close();
                  }
               }

            }
         } catch (IOException var16) {
            LOGGER.warn((String)"Failed to copy datapack file from {} to {}", (Object)path2, (Object)destPath);
            mutableBoolean.setTrue();
         }

      });
      if (mutableBoolean.isTrue()) {
         SystemToast.addPackCopyFailure(client, destPath.toString());
      }

   }

   public void filesDragged(List<Path> paths) {
      String string = (String)paths.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
      this.client.openScreen(new ConfirmScreen((bl) -> {
         if (bl) {
            copyPacks(this.client, paths, this.file.toPath());
            this.refresh();
         }

         this.client.openScreen(this);
      }, new TranslatableText("pack.dropConfirm"), new LiteralText(string)));
   }

   private Identifier loadPackIcon(TextureManager textureManager, ResourcePackProfile resourcePackProfile) {
      try {
         ResourcePack resourcePack = resourcePackProfile.createResourcePack();
         Throwable var4 = null;

         Identifier var10;
         try {
            InputStream inputStream = resourcePack.openRoot("pack.png");
            Throwable var6 = null;

            try {
               String string = resourcePackProfile.getName();
               Identifier identifier = new Identifier("minecraft", "pack/" + Util.replaceInvalidChars(string, Identifier::isPathCharacterValid) + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon");
               NativeImage nativeImage = NativeImage.read(inputStream);
               textureManager.registerTexture(identifier, new NativeImageBackedTexture(nativeImage));
               var10 = identifier;
            } catch (Throwable var37) {
               var6 = var37;
               throw var37;
            } finally {
               if (inputStream != null) {
                  if (var6 != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var36) {
                        var6.addSuppressed(var36);
                     }
                  } else {
                     inputStream.close();
                  }
               }

            }
         } catch (Throwable var39) {
            var4 = var39;
            throw var39;
         } finally {
            if (resourcePack != null) {
               if (var4 != null) {
                  try {
                     resourcePack.close();
                  } catch (Throwable var35) {
                     var4.addSuppressed(var35);
                  }
               } else {
                  resourcePack.close();
               }
            }

         }

         return var10;
      } catch (FileNotFoundException var41) {
      } catch (Exception var42) {
         LOGGER.warn((String)"Failed to load icon from pack {}", (Object)resourcePackProfile.getName(), (Object)var42);
      }

      return UNKNOWN_PACK;
   }

   private Identifier getPackIconTexture(ResourcePackProfile resourcePackProfile) {
      return (Identifier)this.iconTextures.computeIfAbsent(resourcePackProfile.getName(), (string) -> {
         return this.loadPackIcon(this.client.getTextureManager(), resourcePackProfile);
      });
   }

   static {
      DROP_INFO = (new TranslatableText("pack.dropInfo")).formatted(Formatting.GRAY);
      FOLDER_INFO = new TranslatableText("pack.folderInfo");
      UNKNOWN_PACK = new Identifier("textures/misc/unknown_pack.png");
   }

   @Environment(EnvType.CLIENT)
   static class DirectoryWatcher implements AutoCloseable {
      private final WatchService watchService;
      private final Path path;

      public DirectoryWatcher(File file) throws IOException {
         this.path = file.toPath();
         this.watchService = this.path.getFileSystem().newWatchService();

         try {
            this.watchDirectory(this.path);
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.path);
            Throwable var3 = null;

            try {
               Iterator var4 = directoryStream.iterator();

               while(var4.hasNext()) {
                  Path path = (Path)var4.next();
                  if (Files.isDirectory(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
                     this.watchDirectory(path);
                  }
               }
            } catch (Throwable var14) {
               var3 = var14;
               throw var14;
            } finally {
               if (directoryStream != null) {
                  if (var3 != null) {
                     try {
                        directoryStream.close();
                     } catch (Throwable var13) {
                        var3.addSuppressed(var13);
                     }
                  } else {
                     directoryStream.close();
                  }
               }

            }

         } catch (Exception var16) {
            this.watchService.close();
            throw var16;
         }
      }

      @Nullable
      public static PackScreen.DirectoryWatcher create(File file) {
         try {
            return new PackScreen.DirectoryWatcher(file);
         } catch (IOException var2) {
            PackScreen.LOGGER.warn((String)"Failed to initialize pack directory {} monitoring", (Object)file, (Object)var2);
            return null;
         }
      }

      private void watchDirectory(Path path) throws IOException {
         path.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
      }

      public boolean pollForChange() throws IOException {
         boolean bl = false;

         WatchKey watchKey;
         while((watchKey = this.watchService.poll()) != null) {
            List<WatchEvent<?>> list = watchKey.pollEvents();
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
               WatchEvent<?> watchEvent = (WatchEvent)var4.next();
               bl = true;
               if (watchKey.watchable() == this.path && watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                  Path path = this.path.resolve((Path)watchEvent.context());
                  if (Files.isDirectory(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
                     this.watchDirectory(path);
                  }
               }
            }

            watchKey.reset();
         }

         return bl;
      }

      public void close() throws IOException {
         this.watchService.close();
      }
   }
}
