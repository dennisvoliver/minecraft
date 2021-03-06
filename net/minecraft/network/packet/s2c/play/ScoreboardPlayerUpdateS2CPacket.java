package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.scoreboard.ServerScoreboard;
import org.jetbrains.annotations.Nullable;

public class ScoreboardPlayerUpdateS2CPacket implements Packet<ClientPlayPacketListener> {
   private String playerName = "";
   @Nullable
   private String objectiveName;
   private int score;
   private ServerScoreboard.UpdateMode mode;

   public ScoreboardPlayerUpdateS2CPacket() {
   }

   public ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode mode, @Nullable String objectiveName, String playerName, int score) {
      if (mode != ServerScoreboard.UpdateMode.REMOVE && objectiveName == null) {
         throw new IllegalArgumentException("Need an objective name");
      } else {
         this.playerName = playerName;
         this.objectiveName = objectiveName;
         this.score = score;
         this.mode = mode;
      }
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.playerName = buf.readString(40);
      this.mode = (ServerScoreboard.UpdateMode)buf.readEnumConstant(ServerScoreboard.UpdateMode.class);
      String string = buf.readString(16);
      this.objectiveName = Objects.equals(string, "") ? null : string;
      if (this.mode != ServerScoreboard.UpdateMode.REMOVE) {
         this.score = buf.readVarInt();
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeString(this.playerName);
      buf.writeEnumConstant(this.mode);
      buf.writeString(this.objectiveName == null ? "" : this.objectiveName);
      if (this.mode != ServerScoreboard.UpdateMode.REMOVE) {
         buf.writeVarInt(this.score);
      }

   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onScoreboardPlayerUpdate(this);
   }

   @Environment(EnvType.CLIENT)
   public String getPlayerName() {
      return this.playerName;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public String getObjectiveName() {
      return this.objectiveName;
   }

   @Environment(EnvType.CLIENT)
   public int getScore() {
      return this.score;
   }

   @Environment(EnvType.CLIENT)
   public ServerScoreboard.UpdateMode getUpdateMode() {
      return this.mode;
   }
}
