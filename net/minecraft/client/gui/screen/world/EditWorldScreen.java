package net.minecraft.client.gui.screen.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.dynamic.RegistryReadingOps;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class EditWorldScreen extends Screen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
   private static final Text ENTER_NAME_TEXT = new TranslatableText("selectWorld.enterName");
   private ButtonWidget saveButton;
   private final BooleanConsumer callback;
   private TextFieldWidget levelNameTextField;
   private final LevelStorage.Session storageSession;

   public EditWorldScreen(BooleanConsumer callback, LevelStorage.Session storageSession) {
      super(new TranslatableText("selectWorld.edit.title"));
      this.callback = callback;
      this.storageSession = storageSession;
   }

   public void tick() {
      this.levelNameTextField.tick();
   }

   protected void init() {
      this.client.keyboard.setRepeatEvents(true);
      ButtonWidget buttonWidget = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20, new TranslatableText("selectWorld.edit.resetIcon"), (buttonWidgetx) -> {
         FileUtils.deleteQuietly(this.storageSession.getIconFile());
         buttonWidgetx.active = false;
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20, new TranslatableText("selectWorld.edit.openFolder"), (buttonWidgetx) -> {
         Util.getOperatingSystem().open(this.storageSession.getDirectory(WorldSavePath.ROOT).toFile());
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, new TranslatableText("selectWorld.edit.backup"), (buttonWidgetx) -> {
         boolean bl = backupLevel(this.storageSession);
         this.callback.accept(!bl);
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, new TranslatableText("selectWorld.edit.backupFolder"), (buttonWidgetx) -> {
         LevelStorage levelStorage = this.client.getLevelStorage();
         Path path = levelStorage.getBackupsDirectory();

         try {
            Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
         } catch (IOException var5) {
            throw new RuntimeException(var5);
         }

         Util.getOperatingSystem().open(path.toFile());
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20, new TranslatableText("selectWorld.edit.optimize"), (buttonWidgetx) -> {
         this.client.openScreen(new BackupPromptScreen(this, (bl, bl2) -> {
            if (bl) {
               backupLevel(this.storageSession);
            }

            this.client.openScreen(OptimizeWorldScreen.create(this.client, this.callback, this.client.getDataFixer(), this.storageSession, bl2));
         }, new TranslatableText("optimizeWorld.confirm.title"), new TranslatableText("optimizeWorld.confirm.description"), true));
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20, new TranslatableText("selectWorld.edit.export_worldgen_settings"), (buttonWidgetx) -> {
         DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();

         DataResult dataResult4;
         try {
            MinecraftClient.IntegratedResourceManager integratedResourceManager = this.client.method_29604(impl, MinecraftClient::method_29598, MinecraftClient::createSaveProperties, false, this.storageSession);
            Throwable var5 = null;

            try {
               DynamicOps<JsonElement> dynamicOps = RegistryReadingOps.of(JsonOps.INSTANCE, impl);
               DataResult<JsonElement> dataResult = GeneratorOptions.CODEC.encodeStart(dynamicOps, integratedResourceManager.getSaveProperties().getGeneratorOptions());
               dataResult4 = dataResult.flatMap((jsonElement) -> {
                  Path path = this.storageSession.getDirectory(WorldSavePath.ROOT).resolve("worldgen_settings_export.json");

                  try {
                     JsonWriter jsonWriter = GSON.newJsonWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8));
                     Throwable var4 = null;

                     try {
                        GSON.toJson(jsonElement, jsonWriter);
                     } catch (Throwable var14) {
                        var4 = var14;
                        throw var14;
                     } finally {
                        if (jsonWriter != null) {
                           if (var4 != null) {
                              try {
                                 jsonWriter.close();
                              } catch (Throwable var13) {
                                 var4.addSuppressed(var13);
                              }
                           } else {
                              jsonWriter.close();
                           }
                        }

                     }
                  } catch (JsonIOException | IOException var16) {
                     return DataResult.error("Error writing file: " + var16.getMessage());
                  }

                  return DataResult.success(path.toString());
               });
            } catch (Throwable var16) {
               var5 = var16;
               throw var16;
            } finally {
               if (integratedResourceManager != null) {
                  if (var5 != null) {
                     try {
                        integratedResourceManager.close();
                     } catch (Throwable var15) {
                        var5.addSuppressed(var15);
                     }
                  } else {
                     integratedResourceManager.close();
                  }
               }

            }
         } catch (ExecutionException | InterruptedException var18) {
            dataResult4 = DataResult.error("Could not parse level data!");
         }

         Text text = new LiteralText((String)dataResult4.get().map(Function.identity(), PartialResult::message));
         Text text2 = new TranslatableText(dataResult4.result().isPresent() ? "selectWorld.edit.export_worldgen_settings.success" : "selectWorld.edit.export_worldgen_settings.failure");
         dataResult4.error().ifPresent((partialResult) -> {
            LOGGER.error((String)"Error exporting world settings: {}", (Object)partialResult);
         });
         this.client.getToastManager().add(SystemToast.create(this.client, SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, text2, text));
      }));
      this.saveButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, new TranslatableText("selectWorld.edit.save"), (buttonWidgetx) -> {
         this.commit();
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, ScreenTexts.CANCEL, (buttonWidgetx) -> {
         this.callback.accept(false);
      }));
      buttonWidget.active = this.storageSession.getIconFile().isFile();
      LevelSummary levelSummary = this.storageSession.getLevelSummary();
      String string = levelSummary == null ? "" : levelSummary.getDisplayName();
      this.levelNameTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 38, 200, 20, new TranslatableText("selectWorld.enterName"));
      this.levelNameTextField.setText(string);
      this.levelNameTextField.setChangedListener((stringx) -> {
         this.saveButton.active = !stringx.trim().isEmpty();
      });
      this.children.add(this.levelNameTextField);
      this.setInitialFocus(this.levelNameTextField);
   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.levelNameTextField.getText();
      this.init(client, width, height);
      this.levelNameTextField.setText(string);
   }

   public void onClose() {
      this.callback.accept(false);
   }

   public void removed() {
      this.client.keyboard.setRepeatEvents(false);
   }

   private void commit() {
      try {
         this.storageSession.save(this.levelNameTextField.getText().trim());
         this.callback.accept(true);
      } catch (IOException var2) {
         LOGGER.error((String)"Failed to access world '{}'", (Object)this.storageSession.getDirectoryName(), (Object)var2);
         SystemToast.addWorldAccessFailureToast(this.client, this.storageSession.getDirectoryName());
         this.callback.accept(true);
      }

   }

   public static void onBackupConfirm(LevelStorage storage, String levelName) {
      boolean bl = false;

      try {
         LevelStorage.Session session = storage.createSession(levelName);
         Throwable var4 = null;

         try {
            bl = true;
            backupLevel(session);
         } catch (Throwable var14) {
            var4 = var14;
            throw var14;
         } finally {
            if (session != null) {
               if (var4 != null) {
                  try {
                     session.close();
                  } catch (Throwable var13) {
                     var4.addSuppressed(var13);
                  }
               } else {
                  session.close();
               }
            }

         }
      } catch (IOException var16) {
         if (!bl) {
            SystemToast.addWorldAccessFailureToast(MinecraftClient.getInstance(), levelName);
         }

         LOGGER.warn((String)"Failed to create backup of level {}", (Object)levelName, (Object)var16);
      }

   }

   public static boolean backupLevel(LevelStorage.Session storageSession) {
      long l = 0L;
      IOException iOException = null;

      try {
         l = storageSession.createBackup();
      } catch (IOException var6) {
         iOException = var6;
      }

      TranslatableText text3;
      if (iOException != null) {
         text3 = new TranslatableText("selectWorld.edit.backupFailed");
         Text text2 = new LiteralText(iOException.getMessage());
         MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, text3, text2));
         return false;
      } else {
         text3 = new TranslatableText("selectWorld.edit.backupCreated", new Object[]{storageSession.getDirectoryName()});
         Text text4 = new TranslatableText("selectWorld.edit.backupSize", new Object[]{MathHelper.ceil((double)l / 1048576.0D)});
         MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, text3, text4));
         return true;
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
      drawTextWithShadow(matrices, this.textRenderer, ENTER_NAME_TEXT, this.width / 2 - 100, 24, 10526880);
      this.levelNameTextField.render(matrices, mouseX, mouseY, delta);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
