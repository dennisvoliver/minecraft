package net.minecraft.block.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.EndGatewayFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class EndGatewayBlockEntity extends EndPortalBlockEntity implements Tickable {
   private static final Logger LOGGER = LogManager.getLogger();
   private long age;
   private int teleportCooldown;
   @Nullable
   private BlockPos exitPortalPos;
   private boolean exactTeleport;

   public EndGatewayBlockEntity() {
      super(BlockEntityType.END_GATEWAY);
   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      tag.putLong("Age", this.age);
      if (this.exitPortalPos != null) {
         tag.put("ExitPortal", NbtHelper.fromBlockPos(this.exitPortalPos));
      }

      if (this.exactTeleport) {
         tag.putBoolean("ExactTeleport", this.exactTeleport);
      }

      return tag;
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      this.age = tag.getLong("Age");
      if (tag.contains("ExitPortal", 10)) {
         this.exitPortalPos = NbtHelper.toBlockPos(tag.getCompound("ExitPortal"));
      }

      this.exactTeleport = tag.getBoolean("ExactTeleport");
   }

   @Environment(EnvType.CLIENT)
   public double getSquaredRenderDistance() {
      return 256.0D;
   }

   public void tick() {
      boolean bl = this.isRecentlyGenerated();
      boolean bl2 = this.needsCooldownBeforeTeleporting();
      ++this.age;
      if (bl2) {
         --this.teleportCooldown;
      } else if (!this.world.isClient) {
         List<Entity> list = this.world.getEntitiesByClass(Entity.class, new Box(this.getPos()), EndGatewayBlockEntity::method_30276);
         if (!list.isEmpty()) {
            this.tryTeleportingEntity((Entity)list.get(this.world.random.nextInt(list.size())));
         }

         if (this.age % 2400L == 0L) {
            this.startTeleportCooldown();
         }
      }

      if (bl != this.isRecentlyGenerated() || bl2 != this.needsCooldownBeforeTeleporting()) {
         this.markDirty();
      }

   }

   public static boolean method_30276(Entity entity) {
      return EntityPredicates.EXCEPT_SPECTATOR.test(entity) && !entity.getRootVehicle().hasNetherPortalCooldown();
   }

   public boolean isRecentlyGenerated() {
      return this.age < 200L;
   }

   public boolean needsCooldownBeforeTeleporting() {
      return this.teleportCooldown > 0;
   }

   @Environment(EnvType.CLIENT)
   public float getRecentlyGeneratedBeamHeight(float tickDelta) {
      return MathHelper.clamp(((float)this.age + tickDelta) / 200.0F, 0.0F, 1.0F);
   }

   @Environment(EnvType.CLIENT)
   public float getCooldownBeamHeight(float tickDelta) {
      return 1.0F - MathHelper.clamp(((float)this.teleportCooldown - tickDelta) / 40.0F, 0.0F, 1.0F);
   }

   @Nullable
   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return new BlockEntityUpdateS2CPacket(this.pos, 8, this.toInitialChunkDataTag());
   }

   public CompoundTag toInitialChunkDataTag() {
      return this.toTag(new CompoundTag());
   }

   public void startTeleportCooldown() {
      if (!this.world.isClient) {
         this.teleportCooldown = 40;
         this.world.addSyncedBlockEvent(this.getPos(), this.getCachedState().getBlock(), 1, 0);
         this.markDirty();
      }

   }

   public boolean onSyncedBlockEvent(int type, int data) {
      if (type == 1) {
         this.teleportCooldown = 40;
         return true;
      } else {
         return super.onSyncedBlockEvent(type, data);
      }
   }

   public void tryTeleportingEntity(Entity entity) {
      if (this.world instanceof ServerWorld && !this.needsCooldownBeforeTeleporting()) {
         this.teleportCooldown = 100;
         if (this.exitPortalPos == null && this.world.getRegistryKey() == World.END) {
            this.createPortal((ServerWorld)this.world);
         }

         if (this.exitPortalPos != null) {
            BlockPos blockPos = this.exactTeleport ? this.exitPortalPos : this.findBestPortalExitPos();
            Entity entity3;
            if (entity instanceof EnderPearlEntity) {
               Entity entity2 = ((EnderPearlEntity)entity).getOwner();
               if (entity2 instanceof ServerPlayerEntity) {
                  Criteria.ENTER_BLOCK.trigger((ServerPlayerEntity)entity2, this.world.getBlockState(this.getPos()));
               }

               if (entity2 != null) {
                  entity3 = entity2;
                  entity.remove();
               } else {
                  entity3 = entity;
               }
            } else {
               entity3 = entity.getRootVehicle();
            }

            entity3.resetNetherPortalCooldown();
            entity3.teleport((double)blockPos.getX() + 0.5D, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5D);
         }

         this.startTeleportCooldown();
      }
   }

   private BlockPos findBestPortalExitPos() {
      BlockPos blockPos = findExitPortalPos(this.world, this.exitPortalPos.add(0, 2, 0), 5, false);
      LOGGER.debug((String)"Best exit position for portal at {} is {}", (Object)this.exitPortalPos, (Object)blockPos);
      return blockPos.up();
   }

   private void createPortal(ServerWorld world) {
      Vec3d vec3d = (new Vec3d((double)this.getPos().getX(), 0.0D, (double)this.getPos().getZ())).normalize();
      Vec3d vec3d2 = vec3d.multiply(1024.0D);

      int var4;
      for(var4 = 16; getChunk(world, vec3d2).getHighestNonEmptySectionYOffset() > 0 && var4-- > 0; vec3d2 = vec3d2.add(vec3d.multiply(-16.0D))) {
         LOGGER.debug((String)"Skipping backwards past nonempty chunk at {}", (Object)vec3d2);
      }

      for(var4 = 16; getChunk(world, vec3d2).getHighestNonEmptySectionYOffset() == 0 && var4-- > 0; vec3d2 = vec3d2.add(vec3d.multiply(16.0D))) {
         LOGGER.debug((String)"Skipping forward past empty chunk at {}", (Object)vec3d2);
      }

      LOGGER.debug((String)"Found chunk at {}", (Object)vec3d2);
      WorldChunk worldChunk = getChunk(world, vec3d2);
      this.exitPortalPos = findPortalPosition(worldChunk);
      if (this.exitPortalPos == null) {
         this.exitPortalPos = new BlockPos(vec3d2.x + 0.5D, 75.0D, vec3d2.z + 0.5D);
         LOGGER.debug((String)"Failed to find suitable block, settling on {}", (Object)this.exitPortalPos);
         ConfiguredFeatures.END_ISLAND.generate(world, world.getChunkManager().getChunkGenerator(), new Random(this.exitPortalPos.asLong()), this.exitPortalPos);
      } else {
         LOGGER.debug((String)"Found block at {}", (Object)this.exitPortalPos);
      }

      this.exitPortalPos = findExitPortalPos(world, this.exitPortalPos, 16, true);
      LOGGER.debug((String)"Creating portal at {}", (Object)this.exitPortalPos);
      this.exitPortalPos = this.exitPortalPos.up(10);
      this.createPortal(world, this.exitPortalPos);
      this.markDirty();
   }

   private static BlockPos findExitPortalPos(BlockView world, BlockPos pos, int searchRadius, boolean bl) {
      BlockPos blockPos = null;

      for(int i = -searchRadius; i <= searchRadius; ++i) {
         for(int j = -searchRadius; j <= searchRadius; ++j) {
            if (i != 0 || j != 0 || bl) {
               for(int k = 255; k > (blockPos == null ? 0 : blockPos.getY()); --k) {
                  BlockPos blockPos2 = new BlockPos(pos.getX() + i, k, pos.getZ() + j);
                  BlockState blockState = world.getBlockState(blockPos2);
                  if (blockState.isFullCube(world, blockPos2) && (bl || !blockState.isOf(Blocks.BEDROCK))) {
                     blockPos = blockPos2;
                     break;
                  }
               }
            }
         }
      }

      return blockPos == null ? pos : blockPos;
   }

   private static WorldChunk getChunk(World world, Vec3d pos) {
      return world.getChunk(MathHelper.floor(pos.x / 16.0D), MathHelper.floor(pos.z / 16.0D));
   }

   @Nullable
   private static BlockPos findPortalPosition(WorldChunk chunk) {
      ChunkPos chunkPos = chunk.getPos();
      BlockPos blockPos = new BlockPos(chunkPos.getStartX(), 30, chunkPos.getStartZ());
      int i = chunk.getHighestNonEmptySectionYOffset() + 16 - 1;
      BlockPos blockPos2 = new BlockPos(chunkPos.getEndX(), i, chunkPos.getEndZ());
      BlockPos blockPos3 = null;
      double d = 0.0D;
      Iterator var8 = BlockPos.iterate(blockPos, blockPos2).iterator();

      while(true) {
         BlockPos blockPos4;
         double e;
         do {
            BlockPos blockPos5;
            BlockPos blockPos6;
            do {
               BlockState blockState;
               do {
                  do {
                     if (!var8.hasNext()) {
                        return blockPos3;
                     }

                     blockPos4 = (BlockPos)var8.next();
                     blockState = chunk.getBlockState(blockPos4);
                     blockPos5 = blockPos4.up();
                     blockPos6 = blockPos4.up(2);
                  } while(!blockState.isOf(Blocks.END_STONE));
               } while(chunk.getBlockState(blockPos5).isFullCube(chunk, blockPos5));
            } while(chunk.getBlockState(blockPos6).isFullCube(chunk, blockPos6));

            e = blockPos4.getSquaredDistance(0.0D, 0.0D, 0.0D, true);
         } while(blockPos3 != null && !(e < d));

         blockPos3 = blockPos4;
         d = e;
      }
   }

   private void createPortal(ServerWorld world, BlockPos pos) {
      Feature.END_GATEWAY.configure(EndGatewayFeatureConfig.createConfig(this.getPos(), false)).generate(world, world.getChunkManager().getChunkGenerator(), new Random(), pos);
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldDrawSide(Direction direction) {
      return Block.shouldDrawSide(this.getCachedState(), this.world, this.getPos(), direction);
   }

   @Environment(EnvType.CLIENT)
   public int getDrawnSidesCount() {
      int i = 0;
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction direction = var2[var4];
         i += this.shouldDrawSide(direction) ? 1 : 0;
      }

      return i;
   }

   public void setExitPortalPos(BlockPos pos, boolean exactTeleport) {
      this.exactTeleport = exactTeleport;
      this.exitPortalPos = pos;
   }
}
