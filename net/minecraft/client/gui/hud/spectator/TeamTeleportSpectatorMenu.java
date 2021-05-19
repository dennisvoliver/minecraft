package net.minecraft.client.gui.hud.spectator;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class TeamTeleportSpectatorMenu implements SpectatorMenuCommandGroup, SpectatorMenuCommand {
   private static final Text TEAM_TELEPORT_TEXT = new TranslatableText("spectatorMenu.team_teleport");
   private static final Text PROMPT_TEXT = new TranslatableText("spectatorMenu.team_teleport.prompt");
   private final List<SpectatorMenuCommand> commands = Lists.newArrayList();

   public TeamTeleportSpectatorMenu() {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      Iterator var2 = minecraftClient.world.getScoreboard().getTeams().iterator();

      while(var2.hasNext()) {
         Team team = (Team)var2.next();
         this.commands.add(new TeamTeleportSpectatorMenu.TeleportToSpecificTeamCommand(team));
      }

   }

   public List<SpectatorMenuCommand> getCommands() {
      return this.commands;
   }

   public Text getPrompt() {
      return PROMPT_TEXT;
   }

   public void use(SpectatorMenu menu) {
      menu.selectElement(this);
   }

   public Text getName() {
      return TEAM_TELEPORT_TEXT;
   }

   public void renderIcon(MatrixStack matrices, float f, int i) {
      MinecraftClient.getInstance().getTextureManager().bindTexture(SpectatorHud.SPECTATOR_TEXTURE);
      DrawableHelper.drawTexture(matrices, 0, 0, 16.0F, 0.0F, 16, 16, 256, 256);
   }

   public boolean isEnabled() {
      Iterator var1 = this.commands.iterator();

      SpectatorMenuCommand spectatorMenuCommand;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         spectatorMenuCommand = (SpectatorMenuCommand)var1.next();
      } while(!spectatorMenuCommand.isEnabled());

      return true;
   }

   @Environment(EnvType.CLIENT)
   class TeleportToSpecificTeamCommand implements SpectatorMenuCommand {
      private final Team team;
      private final Identifier skinId;
      private final List<PlayerListEntry> scoreboardEntries;

      public TeleportToSpecificTeamCommand(Team team) {
         this.team = team;
         this.scoreboardEntries = Lists.newArrayList();
         Iterator var3 = team.getPlayerList().iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            PlayerListEntry playerListEntry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(string);
            if (playerListEntry != null) {
               this.scoreboardEntries.add(playerListEntry);
            }
         }

         if (this.scoreboardEntries.isEmpty()) {
            this.skinId = DefaultSkinHelper.getTexture();
         } else {
            String string2 = ((PlayerListEntry)this.scoreboardEntries.get((new Random()).nextInt(this.scoreboardEntries.size()))).getProfile().getName();
            this.skinId = AbstractClientPlayerEntity.getSkinId(string2);
            AbstractClientPlayerEntity.loadSkin(this.skinId, string2);
         }

      }

      public void use(SpectatorMenu menu) {
         menu.selectElement(new TeleportSpectatorMenu(this.scoreboardEntries));
      }

      public Text getName() {
         return this.team.getDisplayName();
      }

      public void renderIcon(MatrixStack matrices, float f, int i) {
         Integer integer = this.team.getColor().getColorValue();
         if (integer != null) {
            float g = (float)(integer >> 16 & 255) / 255.0F;
            float h = (float)(integer >> 8 & 255) / 255.0F;
            float j = (float)(integer & 255) / 255.0F;
            DrawableHelper.fill((MatrixStack)matrices, 1, 1, 15, 15, MathHelper.packRgb(g * f, h * f, j * f) | i << 24);
         }

         MinecraftClient.getInstance().getTextureManager().bindTexture(this.skinId);
         RenderSystem.color4f(f, f, f, (float)i / 255.0F);
         DrawableHelper.drawTexture(matrices, 2, 2, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
         DrawableHelper.drawTexture(matrices, 2, 2, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);
      }

      public boolean isEnabled() {
         return !this.scoreboardEntries.isEmpty();
      }
   }
}
