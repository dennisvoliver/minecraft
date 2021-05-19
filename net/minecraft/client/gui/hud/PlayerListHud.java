package net.minecraft.client.gui.hud;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PlayerListHud extends DrawableHelper {
   private static final Ordering<PlayerListEntry> ENTRY_ORDERING = Ordering.from((Comparator)(new PlayerListHud.EntryOrderComparator()));
   private final MinecraftClient client;
   private final InGameHud inGameHud;
   private Text footer;
   private Text header;
   private long showTime;
   private boolean visible;

   public PlayerListHud(MinecraftClient client, InGameHud inGameHud) {
      this.client = client;
      this.inGameHud = inGameHud;
   }

   public Text getPlayerName(PlayerListEntry entry) {
      return entry.getDisplayName() != null ? this.applyGameModeFormatting(entry, entry.getDisplayName().shallowCopy()) : this.applyGameModeFormatting(entry, Team.modifyText(entry.getScoreboardTeam(), new LiteralText(entry.getProfile().getName())));
   }

   /**
    * {@linkplain net.minecraft.util.Formatting#ITALIC Italicizes} the given text if
    * the given player is in {@linkplain net.minecraft.world.GameMode#SPECTATOR spectator mode}.
    */
   private Text applyGameModeFormatting(PlayerListEntry entry, MutableText name) {
      return entry.getGameMode() == GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name;
   }

   public void tick(boolean visible) {
      if (visible && !this.visible) {
         this.showTime = Util.getMeasuringTimeMs();
      }

      this.visible = visible;
   }

   public void render(MatrixStack matrices, int i, Scoreboard scoreboard, @Nullable ScoreboardObjective objective) {
      ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.player.networkHandler;
      List<PlayerListEntry> list = ENTRY_ORDERING.sortedCopy(clientPlayNetworkHandler.getPlayerList());
      int j = 0;
      int k = 0;
      Iterator var9 = list.iterator();

      int o;
      while(var9.hasNext()) {
         PlayerListEntry playerListEntry = (PlayerListEntry)var9.next();
         o = this.client.textRenderer.getWidth((StringVisitable)this.getPlayerName(playerListEntry));
         j = Math.max(j, o);
         if (objective != null && objective.getRenderType() != ScoreboardCriterion.RenderType.HEARTS) {
            o = this.client.textRenderer.getWidth(" " + scoreboard.getPlayerScore(playerListEntry.getProfile().getName(), objective).getScore());
            k = Math.max(k, o);
         }
      }

      list = list.subList(0, Math.min(list.size(), 80));
      int m = list.size();
      int n = m;

      for(o = 1; n > 20; n = (m + o - 1) / o) {
         ++o;
      }

      boolean bl = this.client.isInSingleplayer() || this.client.getNetworkHandler().getConnection().isEncrypted();
      int r;
      if (objective != null) {
         if (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
            r = 90;
         } else {
            r = k;
         }
      } else {
         r = 0;
      }

      int s = Math.min(o * ((bl ? 9 : 0) + j + r + 13), i - 50) / o;
      int t = i / 2 - (s * o + (o - 1) * 5) / 2;
      int u = 10;
      int v = s * o + (o - 1) * 5;
      List<OrderedText> list2 = null;
      if (this.header != null) {
         list2 = this.client.textRenderer.wrapLines(this.header, i - 50);

         OrderedText orderedText;
         for(Iterator var19 = list2.iterator(); var19.hasNext(); v = Math.max(v, this.client.textRenderer.getWidth(orderedText))) {
            orderedText = (OrderedText)var19.next();
         }
      }

      List<OrderedText> list3 = null;
      OrderedText orderedText3;
      Iterator var37;
      if (this.footer != null) {
         list3 = this.client.textRenderer.wrapLines(this.footer, i - 50);

         for(var37 = list3.iterator(); var37.hasNext(); v = Math.max(v, this.client.textRenderer.getWidth(orderedText3))) {
            orderedText3 = (OrderedText)var37.next();
         }
      }

      int var10001;
      int var10002;
      int var10003;
      int var10005;
      int z;
      if (list2 != null) {
         var10001 = i / 2 - v / 2 - 1;
         var10002 = u - 1;
         var10003 = i / 2 + v / 2 + 1;
         var10005 = list2.size();
         this.client.textRenderer.getClass();
         fill(matrices, var10001, var10002, var10003, u + var10005 * 9, Integer.MIN_VALUE);

         for(var37 = list2.iterator(); var37.hasNext(); u += 9) {
            orderedText3 = (OrderedText)var37.next();
            z = this.client.textRenderer.getWidth(orderedText3);
            this.client.textRenderer.drawWithShadow(matrices, (OrderedText)orderedText3, (float)(i / 2 - z / 2), (float)u, -1);
            this.client.textRenderer.getClass();
         }

         ++u;
      }

      fill(matrices, i / 2 - v / 2 - 1, u - 1, i / 2 + v / 2 + 1, u + n * 9, Integer.MIN_VALUE);
      int x = this.client.options.getTextBackgroundColor(553648127);

      int aj;
      for(int y = 0; y < m; ++y) {
         z = y / n;
         aj = y % n;
         int ab = t + z * s + z * 5;
         int ac = u + aj * 9;
         fill(matrices, ab, ac, ab + s, ac + 8, x);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableAlphaTest();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (y < list.size()) {
            PlayerListEntry playerListEntry2 = (PlayerListEntry)list.get(y);
            GameProfile gameProfile = playerListEntry2.getProfile();
            if (bl) {
               PlayerEntity playerEntity = this.client.world.getPlayerByUuid(gameProfile.getId());
               boolean bl2 = playerEntity != null && playerEntity.isPartVisible(PlayerModelPart.CAPE) && ("Dinnerbone".equals(gameProfile.getName()) || "Grumm".equals(gameProfile.getName()));
               this.client.getTextureManager().bindTexture(playerListEntry2.getSkinTexture());
               int ad = 8 + (bl2 ? 8 : 0);
               int ae = 8 * (bl2 ? -1 : 1);
               DrawableHelper.drawTexture(matrices, ab, ac, 8, 8, 8.0F, (float)ad, 8, ae, 64, 64);
               if (playerEntity != null && playerEntity.isPartVisible(PlayerModelPart.HAT)) {
                  int af = 8 + (bl2 ? 8 : 0);
                  int ag = 8 * (bl2 ? -1 : 1);
                  DrawableHelper.drawTexture(matrices, ab, ac, 8, 8, 40.0F, (float)af, 8, ag, 64, 64);
               }

               ab += 9;
            }

            this.client.textRenderer.drawWithShadow(matrices, this.getPlayerName(playerListEntry2), (float)ab, (float)ac, playerListEntry2.getGameMode() == GameMode.SPECTATOR ? -1862270977 : -1);
            if (objective != null && playerListEntry2.getGameMode() != GameMode.SPECTATOR) {
               int ah = ab + j + 1;
               int ai = ah + r;
               if (ai - ah > 5) {
                  this.renderScoreboardObjective(objective, ac, gameProfile.getName(), ah, ai, playerListEntry2, matrices);
               }
            }

            this.renderLatencyIcon(matrices, s, ab - (bl ? 9 : 0), ac, playerListEntry2);
         }
      }

      if (list3 != null) {
         u += n * 9 + 1;
         var10001 = i / 2 - v / 2 - 1;
         var10002 = u - 1;
         var10003 = i / 2 + v / 2 + 1;
         var10005 = list3.size();
         this.client.textRenderer.getClass();
         fill(matrices, var10001, var10002, var10003, u + var10005 * 9, Integer.MIN_VALUE);

         for(Iterator var40 = list3.iterator(); var40.hasNext(); u += 9) {
            OrderedText orderedText4 = (OrderedText)var40.next();
            aj = this.client.textRenderer.getWidth(orderedText4);
            this.client.textRenderer.drawWithShadow(matrices, (OrderedText)orderedText4, (float)(i / 2 - aj / 2), (float)u, -1);
            this.client.textRenderer.getClass();
         }
      }

   }

   protected void renderLatencyIcon(MatrixStack matrices, int i, int j, int k, PlayerListEntry entry) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(GUI_ICONS_TEXTURE);
      int l = false;
      byte r;
      if (entry.getLatency() < 0) {
         r = 5;
      } else if (entry.getLatency() < 150) {
         r = 0;
      } else if (entry.getLatency() < 300) {
         r = 1;
      } else if (entry.getLatency() < 600) {
         r = 2;
      } else if (entry.getLatency() < 1000) {
         r = 3;
      } else {
         r = 4;
      }

      this.setZOffset(this.getZOffset() + 100);
      this.drawTexture(matrices, j + i - 11, k, 0, 176 + r * 8, 10, 8);
      this.setZOffset(this.getZOffset() - 100);
   }

   private void renderScoreboardObjective(ScoreboardObjective objective, int i, String string, int j, int k, PlayerListEntry entry, MatrixStack matrices) {
      int l = objective.getScoreboard().getPlayerScore(string, objective).getScore();
      if (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
         this.client.getTextureManager().bindTexture(GUI_ICONS_TEXTURE);
         long m = Util.getMeasuringTimeMs();
         if (this.showTime == entry.method_2976()) {
            if (l < entry.method_2973()) {
               entry.method_2978(m);
               entry.method_2975((long)(this.inGameHud.getTicks() + 20));
            } else if (l > entry.method_2973()) {
               entry.method_2978(m);
               entry.method_2975((long)(this.inGameHud.getTicks() + 10));
            }
         }

         if (m - entry.method_2974() > 1000L || this.showTime != entry.method_2976()) {
            entry.method_2972(l);
            entry.method_2965(l);
            entry.method_2978(m);
         }

         entry.method_2964(this.showTime);
         entry.method_2972(l);
         int n = MathHelper.ceil((float)Math.max(l, entry.method_2960()) / 2.0F);
         int o = Math.max(MathHelper.ceil((float)(l / 2)), Math.max(MathHelper.ceil((float)(entry.method_2960() / 2)), 10));
         boolean bl = entry.method_2961() > (long)this.inGameHud.getTicks() && (entry.method_2961() - (long)this.inGameHud.getTicks()) / 3L % 2L == 1L;
         if (n > 0) {
            int p = MathHelper.floor(Math.min((float)(k - j - 4) / (float)o, 9.0F));
            if (p > 3) {
               int r;
               for(r = n; r < o; ++r) {
                  this.drawTexture(matrices, j + r * p, i, bl ? 25 : 16, 0, 9, 9);
               }

               for(r = 0; r < n; ++r) {
                  this.drawTexture(matrices, j + r * p, i, bl ? 25 : 16, 0, 9, 9);
                  if (bl) {
                     if (r * 2 + 1 < entry.method_2960()) {
                        this.drawTexture(matrices, j + r * p, i, 70, 0, 9, 9);
                     }

                     if (r * 2 + 1 == entry.method_2960()) {
                        this.drawTexture(matrices, j + r * p, i, 79, 0, 9, 9);
                     }
                  }

                  if (r * 2 + 1 < l) {
                     this.drawTexture(matrices, j + r * p, i, r >= 10 ? 160 : 52, 0, 9, 9);
                  }

                  if (r * 2 + 1 == l) {
                     this.drawTexture(matrices, j + r * p, i, r >= 10 ? 169 : 61, 0, 9, 9);
                  }
               }
            } else {
               float f = MathHelper.clamp((float)l / 20.0F, 0.0F, 1.0F);
               int s = (int)((1.0F - f) * 255.0F) << 16 | (int)(f * 255.0F) << 8;
               String string2 = "" + (float)l / 2.0F;
               if (k - this.client.textRenderer.getWidth(string2 + "hp") >= j) {
                  string2 = string2 + "hp";
               }

               this.client.textRenderer.drawWithShadow(matrices, string2, (float)((k + j) / 2 - this.client.textRenderer.getWidth(string2) / 2), (float)i, s);
            }
         }
      } else {
         String string3 = Formatting.YELLOW + "" + l;
         this.client.textRenderer.drawWithShadow(matrices, string3, (float)(k - this.client.textRenderer.getWidth(string3)), (float)i, 16777215);
      }

   }

   public void setFooter(@Nullable Text footer) {
      this.footer = footer;
   }

   public void setHeader(@Nullable Text header) {
      this.header = header;
   }

   public void clear() {
      this.header = null;
      this.footer = null;
   }

   @Environment(EnvType.CLIENT)
   static class EntryOrderComparator implements Comparator<PlayerListEntry> {
      private EntryOrderComparator() {
      }

      public int compare(PlayerListEntry playerListEntry, PlayerListEntry playerListEntry2) {
         Team team = playerListEntry.getScoreboardTeam();
         Team team2 = playerListEntry2.getScoreboardTeam();
         return ComparisonChain.start().compareTrueFirst(playerListEntry.getGameMode() != GameMode.SPECTATOR, playerListEntry2.getGameMode() != GameMode.SPECTATOR).compare((Comparable)(team != null ? team.getName() : ""), (Comparable)(team2 != null ? team2.getName() : "")).compare(playerListEntry.getProfile().getName(), playerListEntry2.getProfile().getName(), String::compareToIgnoreCase).result();
      }
   }
}
