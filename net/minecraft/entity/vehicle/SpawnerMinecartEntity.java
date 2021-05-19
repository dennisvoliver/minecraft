package net.minecraft.entity.vehicle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

public class SpawnerMinecartEntity extends AbstractMinecartEntity {
   private final MobSpawnerLogic logic = new MobSpawnerLogic() {
      public void sendStatus(int status) {
         SpawnerMinecartEntity.this.world.sendEntityStatus(SpawnerMinecartEntity.this, (byte)status);
      }

      public World getWorld() {
         return SpawnerMinecartEntity.this.world;
      }

      public BlockPos getPos() {
         return SpawnerMinecartEntity.this.getBlockPos();
      }
   };

   public SpawnerMinecartEntity(EntityType<? extends SpawnerMinecartEntity> entityType, World world) {
      super(entityType, world);
   }

   public SpawnerMinecartEntity(World world, double x, double y, double z) {
      super(EntityType.SPAWNER_MINECART, world, x, y, z);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.SPAWNER;
   }

   public BlockState getDefaultContainedBlock() {
      return Blocks.SPAWNER.getDefaultState();
   }

   protected void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.logic.fromTag(tag);
   }

   protected void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      this.logic.toTag(tag);
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      this.logic.method_8275(status);
   }

   public void tick() {
      super.tick();
      this.logic.update();
   }

   public boolean entityDataRequiresOperator() {
      return true;
   }
}
