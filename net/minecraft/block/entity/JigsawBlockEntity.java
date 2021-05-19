package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.JigsawBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class JigsawBlockEntity extends BlockEntity {
   private Identifier name;
   private Identifier target;
   private Identifier pool;
   private JigsawBlockEntity.Joint joint;
   private String finalState;

   public JigsawBlockEntity(BlockEntityType<?> blockEntityType) {
      super(blockEntityType);
      this.name = new Identifier("empty");
      this.target = new Identifier("empty");
      this.pool = new Identifier("empty");
      this.joint = JigsawBlockEntity.Joint.ROLLABLE;
      this.finalState = "minecraft:air";
   }

   public JigsawBlockEntity() {
      this(BlockEntityType.JIGSAW);
   }

   @Environment(EnvType.CLIENT)
   public Identifier getName() {
      return this.name;
   }

   @Environment(EnvType.CLIENT)
   public Identifier getTarget() {
      return this.target;
   }

   @Environment(EnvType.CLIENT)
   public Identifier getPool() {
      return this.pool;
   }

   @Environment(EnvType.CLIENT)
   public String getFinalState() {
      return this.finalState;
   }

   @Environment(EnvType.CLIENT)
   public JigsawBlockEntity.Joint getJoint() {
      return this.joint;
   }

   public void setAttachmentType(Identifier value) {
      this.name = value;
   }

   public void setTargetPool(Identifier target) {
      this.target = target;
   }

   public void setPool(Identifier pool) {
      this.pool = pool;
   }

   public void setFinalState(String finalState) {
      this.finalState = finalState;
   }

   public void setJoint(JigsawBlockEntity.Joint joint) {
      this.joint = joint;
   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      tag.putString("name", this.name.toString());
      tag.putString("target", this.target.toString());
      tag.putString("pool", this.pool.toString());
      tag.putString("final_state", this.finalState);
      tag.putString("joint", this.joint.asString());
      return tag;
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      this.name = new Identifier(tag.getString("name"));
      this.target = new Identifier(tag.getString("target"));
      this.pool = new Identifier(tag.getString("pool"));
      this.finalState = tag.getString("final_state");
      this.joint = (JigsawBlockEntity.Joint)JigsawBlockEntity.Joint.byName(tag.getString("joint")).orElseGet(() -> {
         return JigsawBlock.getFacing(state).getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE;
      });
   }

   @Nullable
   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return new BlockEntityUpdateS2CPacket(this.pos, 12, this.toInitialChunkDataTag());
   }

   public CompoundTag toInitialChunkDataTag() {
      return this.toTag(new CompoundTag());
   }

   public void generate(ServerWorld world, int maxDepth, boolean keepJigsaws) {
      ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
      StructureManager structureManager = world.getStructureManager();
      StructureAccessor structureAccessor = world.getStructureAccessor();
      Random random = world.getRandom();
      BlockPos blockPos = this.getPos();
      List<PoolStructurePiece> list = Lists.newArrayList();
      Structure structure = new Structure();
      structure.saveFromWorld(world, blockPos, new BlockPos(1, 1, 1), false, (Block)null);
      StructurePoolElement structurePoolElement = new SinglePoolElement(structure);
      PoolStructurePiece poolStructurePiece = new PoolStructurePiece(structureManager, structurePoolElement, blockPos, 1, BlockRotation.NONE, new BlockBox(blockPos, blockPos));
      StructurePoolBasedGenerator.method_27230(world.getRegistryManager(), poolStructurePiece, maxDepth, PoolStructurePiece::new, chunkGenerator, structureManager, list, random);
      Iterator var13 = list.iterator();

      while(var13.hasNext()) {
         PoolStructurePiece poolStructurePiece2 = (PoolStructurePiece)var13.next();
         poolStructurePiece2.method_27236(world, structureAccessor, chunkGenerator, random, BlockBox.infinite(), blockPos, keepJigsaws);
      }

   }

   public static enum Joint implements StringIdentifiable {
      ROLLABLE("rollable"),
      ALIGNED("aligned");

      private final String name;

      private Joint(String name) {
         this.name = name;
      }

      public String asString() {
         return this.name;
      }

      public static Optional<JigsawBlockEntity.Joint> byName(String name) {
         return Arrays.stream(values()).filter((joint) -> {
            return joint.asString().equals(name);
         }).findFirst();
      }
   }
}
