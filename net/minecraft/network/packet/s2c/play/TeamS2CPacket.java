package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TeamS2CPacket implements Packet<ClientPlayPacketListener> {
   private String teamName = "";
   private Text displayName;
   private Text prefix;
   private Text suffix;
   private String nameTagVisibilityRule;
   private String collisionRule;
   private Formatting color;
   private final Collection<String> playerList;
   private int mode;
   private int flags;

   public TeamS2CPacket() {
      this.displayName = LiteralText.EMPTY;
      this.prefix = LiteralText.EMPTY;
      this.suffix = LiteralText.EMPTY;
      this.nameTagVisibilityRule = AbstractTeam.VisibilityRule.ALWAYS.name;
      this.collisionRule = AbstractTeam.CollisionRule.ALWAYS.name;
      this.color = Formatting.RESET;
      this.playerList = Lists.newArrayList();
   }

   public TeamS2CPacket(Team team, int mode) {
      this.displayName = LiteralText.EMPTY;
      this.prefix = LiteralText.EMPTY;
      this.suffix = LiteralText.EMPTY;
      this.nameTagVisibilityRule = AbstractTeam.VisibilityRule.ALWAYS.name;
      this.collisionRule = AbstractTeam.CollisionRule.ALWAYS.name;
      this.color = Formatting.RESET;
      this.playerList = Lists.newArrayList();
      this.teamName = team.getName();
      this.mode = mode;
      if (mode == 0 || mode == 2) {
         this.displayName = team.getDisplayName();
         this.flags = team.getFriendlyFlagsBitwise();
         this.nameTagVisibilityRule = team.getNameTagVisibilityRule().name;
         this.collisionRule = team.getCollisionRule().name;
         this.color = team.getColor();
         this.prefix = team.getPrefix();
         this.suffix = team.getSuffix();
      }

      if (mode == 0) {
         this.playerList.addAll(team.getPlayerList());
      }

   }

   public TeamS2CPacket(Team team, Collection<String> playerList, int mode) {
      this.displayName = LiteralText.EMPTY;
      this.prefix = LiteralText.EMPTY;
      this.suffix = LiteralText.EMPTY;
      this.nameTagVisibilityRule = AbstractTeam.VisibilityRule.ALWAYS.name;
      this.collisionRule = AbstractTeam.CollisionRule.ALWAYS.name;
      this.color = Formatting.RESET;
      this.playerList = Lists.newArrayList();
      if (mode != 3 && mode != 4) {
         throw new IllegalArgumentException("Method must be join or leave for player constructor");
      } else if (playerList != null && !playerList.isEmpty()) {
         this.mode = mode;
         this.teamName = team.getName();
         this.playerList.addAll(playerList);
      } else {
         throw new IllegalArgumentException("Players cannot be null/empty");
      }
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.teamName = buf.readString(16);
      this.mode = buf.readByte();
      if (this.mode == 0 || this.mode == 2) {
         this.displayName = buf.readText();
         this.flags = buf.readByte();
         this.nameTagVisibilityRule = buf.readString(40);
         this.collisionRule = buf.readString(40);
         this.color = (Formatting)buf.readEnumConstant(Formatting.class);
         this.prefix = buf.readText();
         this.suffix = buf.readText();
      }

      if (this.mode == 0 || this.mode == 3 || this.mode == 4) {
         int i = buf.readVarInt();

         for(int j = 0; j < i; ++j) {
            this.playerList.add(buf.readString(40));
         }
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeString(this.teamName);
      buf.writeByte(this.mode);
      if (this.mode == 0 || this.mode == 2) {
         buf.writeText(this.displayName);
         buf.writeByte(this.flags);
         buf.writeString(this.nameTagVisibilityRule);
         buf.writeString(this.collisionRule);
         buf.writeEnumConstant(this.color);
         buf.writeText(this.prefix);
         buf.writeText(this.suffix);
      }

      if (this.mode == 0 || this.mode == 3 || this.mode == 4) {
         buf.writeVarInt(this.playerList.size());
         Iterator var2 = this.playerList.iterator();

         while(var2.hasNext()) {
            String string = (String)var2.next();
            buf.writeString(string);
         }
      }

   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onTeam(this);
   }

   @Environment(EnvType.CLIENT)
   public String getTeamName() {
      return this.teamName;
   }

   @Environment(EnvType.CLIENT)
   public Text getDisplayName() {
      return this.displayName;
   }

   @Environment(EnvType.CLIENT)
   public Collection<String> getPlayerList() {
      return this.playerList;
   }

   @Environment(EnvType.CLIENT)
   public int getMode() {
      return this.mode;
   }

   @Environment(EnvType.CLIENT)
   public int getFlags() {
      return this.flags;
   }

   @Environment(EnvType.CLIENT)
   public Formatting getPlayerPrefix() {
      return this.color;
   }

   @Environment(EnvType.CLIENT)
   public String getNameTagVisibilityRule() {
      return this.nameTagVisibilityRule;
   }

   @Environment(EnvType.CLIENT)
   public String getCollisionRule() {
      return this.collisionRule;
   }

   @Environment(EnvType.CLIENT)
   public Text getPrefix() {
      return this.prefix;
   }

   @Environment(EnvType.CLIENT)
   public Text getSuffix() {
      return this.suffix;
   }
}
