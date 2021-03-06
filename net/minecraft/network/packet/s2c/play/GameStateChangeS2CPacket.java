package net.minecraft.network.packet.s2c.play;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class GameStateChangeS2CPacket implements Packet<ClientPlayPacketListener> {
   public static final GameStateChangeS2CPacket.Reason NO_RESPAWN_BLOCK = new GameStateChangeS2CPacket.Reason(0);
   public static final GameStateChangeS2CPacket.Reason RAIN_STARTED = new GameStateChangeS2CPacket.Reason(1);
   public static final GameStateChangeS2CPacket.Reason RAIN_STOPPED = new GameStateChangeS2CPacket.Reason(2);
   public static final GameStateChangeS2CPacket.Reason GAME_MODE_CHANGED = new GameStateChangeS2CPacket.Reason(3);
   public static final GameStateChangeS2CPacket.Reason GAME_WON = new GameStateChangeS2CPacket.Reason(4);
   public static final GameStateChangeS2CPacket.Reason DEMO_MESSAGE_SHOWN = new GameStateChangeS2CPacket.Reason(5);
   public static final GameStateChangeS2CPacket.Reason PROJECTILE_HIT_PLAYER = new GameStateChangeS2CPacket.Reason(6);
   public static final GameStateChangeS2CPacket.Reason RAIN_GRADIENT_CHANGED = new GameStateChangeS2CPacket.Reason(7);
   public static final GameStateChangeS2CPacket.Reason THUNDER_GRADIENT_CHANGED = new GameStateChangeS2CPacket.Reason(8);
   public static final GameStateChangeS2CPacket.Reason PUFFERFISH_STING = new GameStateChangeS2CPacket.Reason(9);
   public static final GameStateChangeS2CPacket.Reason ELDER_GUARDIAN_EFFECT = new GameStateChangeS2CPacket.Reason(10);
   public static final GameStateChangeS2CPacket.Reason IMMEDIATE_RESPAWN = new GameStateChangeS2CPacket.Reason(11);
   private GameStateChangeS2CPacket.Reason reason;
   private float value;

   public GameStateChangeS2CPacket() {
   }

   public GameStateChangeS2CPacket(GameStateChangeS2CPacket.Reason reason, float value) {
      this.reason = reason;
      this.value = value;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.reason = (GameStateChangeS2CPacket.Reason)GameStateChangeS2CPacket.Reason.REASONS.get(buf.readUnsignedByte());
      this.value = buf.readFloat();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeByte(this.reason.id);
      buf.writeFloat(this.value);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onGameStateChange(this);
   }

   @Environment(EnvType.CLIENT)
   public GameStateChangeS2CPacket.Reason getReason() {
      return this.reason;
   }

   @Environment(EnvType.CLIENT)
   public float getValue() {
      return this.value;
   }

   public static class Reason {
      private static final Int2ObjectMap<GameStateChangeS2CPacket.Reason> REASONS = new Int2ObjectOpenHashMap();
      private final int id;

      public Reason(int id) {
         this.id = id;
         REASONS.put(id, this);
      }
   }
}
