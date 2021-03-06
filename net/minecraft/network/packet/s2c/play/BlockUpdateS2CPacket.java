package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class BlockUpdateS2CPacket implements Packet<ClientPlayPacketListener> {
   private BlockPos pos;
   private BlockState state;

   public BlockUpdateS2CPacket() {
   }

   public BlockUpdateS2CPacket(BlockPos blockPos, BlockState blockState) {
      this.pos = blockPos;
      this.state = blockState;
   }

   public BlockUpdateS2CPacket(BlockView world, BlockPos pos) {
      this(pos, world.getBlockState(pos));
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.pos = buf.readBlockPos();
      this.state = (BlockState)Block.STATE_IDS.get(buf.readVarInt());
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeBlockPos(this.pos);
      buf.writeVarInt(Block.getRawIdFromState(this.state));
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onBlockUpdate(this);
   }

   @Environment(EnvType.CLIENT)
   public BlockState getState() {
      return this.state;
   }

   @Environment(EnvType.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }
}
