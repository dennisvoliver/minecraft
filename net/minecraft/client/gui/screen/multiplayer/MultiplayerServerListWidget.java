package net.minecraft.client.gui.screen.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.options.ServerList;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MultiplayerServerListWidget extends AlwaysSelectedEntryListWidget<MultiplayerServerListWidget.Entry> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ThreadPoolExecutor SERVER_PINGER_THREAD_POOL;
   private static final Identifier UNKNOWN_SERVER_TEXTURE;
   private static final Identifier SERVER_SELECTION_TEXTURE;
   private static final Text LAN_SCANNING_TEXT;
   private static final Text CANNOT_RESOLVE_TEXT;
   private static final Text CANNOT_CONNECT_TEXT;
   private static final Text INCOMPATIBLE_TEXT;
   private static final Text NO_CONNECTION_TEXT;
   private static final Text PINGING_TEXT;
   private final MultiplayerScreen screen;
   private final List<MultiplayerServerListWidget.ServerEntry> servers = Lists.newArrayList();
   private final MultiplayerServerListWidget.Entry scanningEntry = new MultiplayerServerListWidget.ScanningEntry();
   private final List<MultiplayerServerListWidget.LanServerEntry> lanServers = Lists.newArrayList();

   public MultiplayerServerListWidget(MultiplayerScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
      super(client, width, height, top, bottom, entryHeight);
      this.screen = screen;
   }

   private void updateEntries() {
      this.clearEntries();
      this.servers.forEach(this::addEntry);
      this.addEntry(this.scanningEntry);
      this.lanServers.forEach(this::addEntry);
   }

   public void setSelected(@Nullable MultiplayerServerListWidget.Entry entry) {
      super.setSelected(entry);
      if (this.getSelected() instanceof MultiplayerServerListWidget.ServerEntry) {
         NarratorManager.INSTANCE.narrate((new TranslatableText("narrator.select", new Object[]{((MultiplayerServerListWidget.ServerEntry)this.getSelected()).server.name})).getString());
      }

      this.screen.updateButtonActivationStates();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      MultiplayerServerListWidget.Entry entry = (MultiplayerServerListWidget.Entry)this.getSelected();
      return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
   }

   protected void moveSelection(EntryListWidget.MoveDirection direction) {
      this.moveSelectionIf(direction, (entry) -> {
         return !(entry instanceof MultiplayerServerListWidget.ScanningEntry);
      });
   }

   public void setServers(ServerList servers) {
      this.servers.clear();

      for(int i = 0; i < servers.size(); ++i) {
         this.servers.add(new MultiplayerServerListWidget.ServerEntry(this.screen, servers.get(i)));
      }

      this.updateEntries();
   }

   public void setLanServers(List<LanServerInfo> lanServers) {
      this.lanServers.clear();
      Iterator var2 = lanServers.iterator();

      while(var2.hasNext()) {
         LanServerInfo lanServerInfo = (LanServerInfo)var2.next();
         this.lanServers.add(new MultiplayerServerListWidget.LanServerEntry(this.screen, lanServerInfo));
      }

      this.updateEntries();
   }

   protected int getScrollbarPositionX() {
      return super.getScrollbarPositionX() + 30;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 85;
   }

   protected boolean isFocused() {
      return this.screen.getFocused() == this;
   }

   static {
      SERVER_PINGER_THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER)).build());
      UNKNOWN_SERVER_TEXTURE = new Identifier("textures/misc/unknown_server.png");
      SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");
      LAN_SCANNING_TEXT = new TranslatableText("lanServer.scanning");
      CANNOT_RESOLVE_TEXT = (new TranslatableText("multiplayer.status.cannot_resolve")).formatted(Formatting.DARK_RED);
      CANNOT_CONNECT_TEXT = (new TranslatableText("multiplayer.status.cannot_connect")).formatted(Formatting.DARK_RED);
      INCOMPATIBLE_TEXT = new TranslatableText("multiplayer.status.incompatible");
      NO_CONNECTION_TEXT = new TranslatableText("multiplayer.status.no_connection");
      PINGING_TEXT = new TranslatableText("multiplayer.status.pinging");
   }

   @Environment(EnvType.CLIENT)
   public class ServerEntry extends MultiplayerServerListWidget.Entry {
      private final MultiplayerScreen screen;
      private final MinecraftClient client;
      private final ServerInfo server;
      private final Identifier iconTextureId;
      private String iconUri;
      private NativeImageBackedTexture icon;
      private long time;

      protected ServerEntry(MultiplayerScreen screen, ServerInfo server) {
         this.screen = screen;
         this.server = server;
         this.client = MinecraftClient.getInstance();
         this.iconTextureId = new Identifier("servers/" + Hashing.sha1().hashUnencodedChars(server.address) + "/icon");
         this.icon = (NativeImageBackedTexture)this.client.getTextureManager().getTexture(this.iconTextureId);
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         if (!this.server.online) {
            this.server.online = true;
            this.server.ping = -2L;
            this.server.label = LiteralText.EMPTY;
            this.server.playerCountLabel = LiteralText.EMPTY;
            MultiplayerServerListWidget.SERVER_PINGER_THREAD_POOL.submit(() -> {
               try {
                  this.screen.getServerListPinger().add(this.server, () -> {
                     this.client.execute(this::saveFile);
                  });
               } catch (UnknownHostException var2) {
                  this.server.ping = -1L;
                  this.server.label = MultiplayerServerListWidget.CANNOT_RESOLVE_TEXT;
               } catch (Exception var3) {
                  this.server.ping = -1L;
                  this.server.label = MultiplayerServerListWidget.CANNOT_CONNECT_TEXT;
               }

            });
         }

         boolean bl = this.server.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion();
         this.client.textRenderer.draw(matrices, this.server.name, (float)(x + 32 + 3), (float)(y + 1), 16777215);
         List<OrderedText> list = this.client.textRenderer.wrapLines(this.server.label, entryWidth - 32 - 2);

         for(int i = 0; i < Math.min(list.size(), 2); ++i) {
            TextRenderer var10000 = this.client.textRenderer;
            OrderedText var10002 = (OrderedText)list.get(i);
            float var10003 = (float)(x + 32 + 3);
            int var10004 = y + 12;
            this.client.textRenderer.getClass();
            var10000.draw(matrices, var10002, var10003, (float)(var10004 + 9 * i), 8421504);
         }

         Text text = bl ? this.server.version.shallowCopy().formatted(Formatting.RED) : this.server.playerCountLabel;
         int j = this.client.textRenderer.getWidth((StringVisitable)text);
         this.client.textRenderer.draw(matrices, (Text)text, (float)(x + entryWidth - j - 15 - 2), (float)(y + 1), 8421504);
         int k = 0;
         int s;
         List list5;
         Object text5;
         if (bl) {
            s = 5;
            text5 = MultiplayerServerListWidget.INCOMPATIBLE_TEXT;
            list5 = this.server.playerListSummary;
         } else if (this.server.online && this.server.ping != -2L) {
            if (this.server.ping < 0L) {
               s = 5;
            } else if (this.server.ping < 150L) {
               s = 0;
            } else if (this.server.ping < 300L) {
               s = 1;
            } else if (this.server.ping < 600L) {
               s = 2;
            } else if (this.server.ping < 1000L) {
               s = 3;
            } else {
               s = 4;
            }

            if (this.server.ping < 0L) {
               text5 = MultiplayerServerListWidget.NO_CONNECTION_TEXT;
               list5 = Collections.emptyList();
            } else {
               text5 = new TranslatableText("multiplayer.status.ping", new Object[]{this.server.ping});
               list5 = this.server.playerListSummary;
            }
         } else {
            k = 1;
            s = (int)(Util.getMeasuringTimeMs() / 100L + (long)(index * 2) & 7L);
            if (s > 4) {
               s = 8 - s;
            }

            text5 = MultiplayerServerListWidget.PINGING_TEXT;
            list5 = Collections.emptyList();
         }

         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
         DrawableHelper.drawTexture(matrices, x + entryWidth - 15, y, (float)(k * 10), (float)(176 + s * 8), 10, 8, 256, 256);
         String string = this.server.getIcon();
         if (!Objects.equals(string, this.iconUri)) {
            if (this.isNewIconValid(string)) {
               this.iconUri = string;
            } else {
               this.server.setIcon((String)null);
               this.saveFile();
            }
         }

         if (this.icon != null) {
            this.draw(matrices, x, y, this.iconTextureId);
         } else {
            this.draw(matrices, x, y, MultiplayerServerListWidget.UNKNOWN_SERVER_TEXTURE);
         }

         int t = mouseX - x;
         int u = mouseY - y;
         if (t >= entryWidth - 15 && t <= entryWidth - 5 && u >= 0 && u <= 8) {
            this.screen.setTooltip(Collections.singletonList(text5));
         } else if (t >= entryWidth - j - 15 - 2 && t <= entryWidth - 15 - 2 && u >= 0 && u <= 8) {
            this.screen.setTooltip(list5);
         }

         if (this.client.options.touchscreen || hovered) {
            this.client.getTextureManager().bindTexture(MultiplayerServerListWidget.SERVER_SELECTION_TEXTURE);
            DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int v = mouseX - x;
            int w = mouseY - y;
            if (this.method_20136()) {
               if (v < 32 && v > 16) {
                  DrawableHelper.drawTexture(matrices, x, y, 0.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (index > 0) {
               if (v < 16 && w < 16) {
                  DrawableHelper.drawTexture(matrices, x, y, 96.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.drawTexture(matrices, x, y, 96.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (index < this.screen.getServerList().size() - 1) {
               if (v < 16 && w > 16) {
                  DrawableHelper.drawTexture(matrices, x, y, 64.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.drawTexture(matrices, x, y, 64.0F, 0.0F, 32, 32, 256, 256);
               }
            }
         }

      }

      public void saveFile() {
         this.screen.getServerList().saveFile();
      }

      protected void draw(MatrixStack matrices, int x, int y, Identifier textureId) {
         this.client.getTextureManager().bindTexture(textureId);
         RenderSystem.enableBlend();
         DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
         RenderSystem.disableBlend();
      }

      private boolean method_20136() {
         return true;
      }

      private boolean isNewIconValid(@Nullable String newIconUri) {
         if (newIconUri == null) {
            this.client.getTextureManager().destroyTexture(this.iconTextureId);
            if (this.icon != null && this.icon.getImage() != null) {
               this.icon.getImage().close();
            }

            this.icon = null;
         } else {
            try {
               NativeImage nativeImage = NativeImage.read(newIconUri);
               Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide");
               Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high");
               if (this.icon == null) {
                  this.icon = new NativeImageBackedTexture(nativeImage);
               } else {
                  this.icon.setImage(nativeImage);
                  this.icon.upload();
               }

               this.client.getTextureManager().registerTexture(this.iconTextureId, this.icon);
            } catch (Throwable var3) {
               MultiplayerServerListWidget.LOGGER.error((String)"Invalid icon for server {} ({})", (Object)this.server.name, this.server.address, var3);
               return false;
            }
         }

         return true;
      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         if (Screen.hasShiftDown()) {
            MultiplayerServerListWidget multiplayerServerListWidget = this.screen.serverListWidget;
            int i = multiplayerServerListWidget.children().indexOf(this);
            if (keyCode == 264 && i < this.screen.getServerList().size() - 1 || keyCode == 265 && i > 0) {
               this.swapEntries(i, keyCode == 264 ? i + 1 : i - 1);
               return true;
            }
         }

         return super.keyPressed(keyCode, scanCode, modifiers);
      }

      private void swapEntries(int i, int j) {
         this.screen.getServerList().swapEntries(i, j);
         this.screen.serverListWidget.setServers(this.screen.getServerList());
         MultiplayerServerListWidget.Entry entry = (MultiplayerServerListWidget.Entry)this.screen.serverListWidget.children().get(j);
         this.screen.serverListWidget.setSelected(entry);
         MultiplayerServerListWidget.this.ensureVisible(entry);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         double d = mouseX - (double)MultiplayerServerListWidget.this.getRowLeft();
         double e = mouseY - (double)MultiplayerServerListWidget.this.getRowTop(MultiplayerServerListWidget.this.children().indexOf(this));
         if (d <= 32.0D) {
            if (d < 32.0D && d > 16.0D && this.method_20136()) {
               this.screen.select(this);
               this.screen.connect();
               return true;
            }

            int i = this.screen.serverListWidget.children().indexOf(this);
            if (d < 16.0D && e < 16.0D && i > 0) {
               this.swapEntries(i, i - 1);
               return true;
            }

            if (d < 16.0D && e > 16.0D && i < this.screen.getServerList().size() - 1) {
               this.swapEntries(i, i + 1);
               return true;
            }
         }

         this.screen.select(this);
         if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.screen.connect();
         }

         this.time = Util.getMeasuringTimeMs();
         return false;
      }

      public ServerInfo getServer() {
         return this.server;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class LanServerEntry extends MultiplayerServerListWidget.Entry {
      private static final Text TITLE_TEXT = new TranslatableText("lanServer.title");
      private static final Text HIDDEN_ADDRESS_TEXT = new TranslatableText("selectServer.hiddenAddress");
      private final MultiplayerScreen screen;
      protected final MinecraftClient client;
      protected final LanServerInfo server;
      private long time;

      protected LanServerEntry(MultiplayerScreen screen, LanServerInfo server) {
         this.screen = screen;
         this.server = server;
         this.client = MinecraftClient.getInstance();
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.client.textRenderer.draw(matrices, TITLE_TEXT, (float)(x + 32 + 3), (float)(y + 1), 16777215);
         this.client.textRenderer.draw(matrices, this.server.getMotd(), (float)(x + 32 + 3), (float)(y + 12), 8421504);
         if (this.client.options.hideServerAddress) {
            this.client.textRenderer.draw(matrices, HIDDEN_ADDRESS_TEXT, (float)(x + 32 + 3), (float)(y + 12 + 11), 3158064);
         } else {
            this.client.textRenderer.draw(matrices, this.server.getAddressPort(), (float)(x + 32 + 3), (float)(y + 12 + 11), 3158064);
         }

      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         this.screen.select(this);
         if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.screen.connect();
         }

         this.time = Util.getMeasuringTimeMs();
         return false;
      }

      public LanServerInfo getLanServerEntry() {
         return this.server;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ScanningEntry extends MultiplayerServerListWidget.Entry {
      private final MinecraftClient client = MinecraftClient.getInstance();

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         int var10000 = y + entryHeight / 2;
         this.client.textRenderer.getClass();
         int i = var10000 - 9 / 2;
         this.client.textRenderer.draw(matrices, MultiplayerServerListWidget.LAN_SCANNING_TEXT, (float)(this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth((StringVisitable)MultiplayerServerListWidget.LAN_SCANNING_TEXT) / 2), (float)i, 16777215);
         String string3;
         switch((int)(Util.getMeasuringTimeMs() / 300L % 4L)) {
         case 0:
         default:
            string3 = "O o o";
            break;
         case 1:
         case 3:
            string3 = "o O o";
            break;
         case 2:
            string3 = "o o O";
         }

         TextRenderer var13 = this.client.textRenderer;
         float var10003 = (float)(this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth(string3) / 2);
         this.client.textRenderer.getClass();
         var13.draw(matrices, string3, var10003, (float)(i + 9), 8421504);
      }
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<MultiplayerServerListWidget.Entry> {
   }
}
