package net.minecraft.world;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ChunkRegion implements StructureWorldAccess {
   private static final Logger LOGGER = LogManager.getLogger();
   private final List<Chunk> chunks;
   private final int centerChunkX;
   private final int centerChunkZ;
   private final int width;
   private final ServerWorld world;
   private final long seed;
   private final WorldProperties levelProperties;
   private final Random random;
   private final DimensionType dimension;
   private final TickScheduler<Block> blockTickScheduler = new MultiTickScheduler((pos) -> {
      return this.getChunk(pos).getBlockTickScheduler();
   });
   private final TickScheduler<Fluid> fluidTickScheduler = new MultiTickScheduler((pos) -> {
      return this.getChunk(pos).getFluidTickScheduler();
   });
   private final BiomeAccess biomeAccess;
   private final ChunkPos lowerCorner;
   private final ChunkPos upperCorner;
   private final StructureAccessor field_26822;

   public ChunkRegion(ServerWorld world, List<Chunk> chunks) {
      int i = MathHelper.floor(Math.sqrt((double)chunks.size()));
      if (i * i != chunks.size()) {
         throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("Cache size is not a square."));
      } else {
         ChunkPos chunkPos = ((Chunk)chunks.get(chunks.size() / 2)).getPos();
         this.chunks = chunks;
         this.centerChunkX = chunkPos.x;
         this.centerChunkZ = chunkPos.z;
         this.width = i;
         this.world = world;
         this.seed = world.getSeed();
         this.levelProperties = world.getLevelProperties();
         this.random = world.getRandom();
         this.dimension = world.getDimension();
         this.biomeAccess = new BiomeAccess(this, BiomeAccess.hashSeed(this.seed), world.getDimension().getBiomeAccessType());
         this.lowerCorner = ((Chunk)chunks.get(0)).getPos();
         this.upperCorner = ((Chunk)chunks.get(chunks.size() - 1)).getPos();
         this.field_26822 = world.getStructureAccessor().forRegion(this);
      }
   }

   public int getCenterChunkX() {
      return this.centerChunkX;
   }

   public int getCenterChunkZ() {
      return this.centerChunkZ;
   }

   public Chunk getChunk(int chunkX, int chunkZ) {
      return this.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY);
   }

   @Nullable
   public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
      Chunk chunk2;
      if (this.isChunkLoaded(chunkX, chunkZ)) {
         int i = chunkX - this.lowerCorner.x;
         int j = chunkZ - this.lowerCorner.z;
         chunk2 = (Chunk)this.chunks.get(i + j * this.width);
         if (chunk2.getStatus().isAtLeast(leastStatus)) {
            return chunk2;
         }
      } else {
         chunk2 = null;
      }

      if (!create) {
         return null;
      } else {
         LOGGER.error((String)"Requested chunk : {} {}", (Object)chunkX, (Object)chunkZ);
         LOGGER.error((String)"Region bounds : {} {} | {} {}", (Object)this.lowerCorner.x, this.lowerCorner.z, this.upperCorner.x, this.upperCorner.z);
         if (chunk2 != null) {
            throw (RuntimeException)Util.throwOrPause(new RuntimeException(String.format("Chunk is not of correct status. Expecting %s, got %s | %s %s", leastStatus, chunk2.getStatus(), chunkX, chunkZ)));
         } else {
            throw (RuntimeException)Util.throwOrPause(new RuntimeException(String.format("We are asking a region for a chunk out of bound | %s %s", chunkX, chunkZ)));
         }
      }
   }

   public boolean isChunkLoaded(int chunkX, int chunkZ) {
      return chunkX >= this.lowerCorner.x && chunkX <= this.upperCorner.x && chunkZ >= this.lowerCorner.z && chunkZ <= this.upperCorner.z;
   }

   public BlockState getBlockState(BlockPos pos) {
      return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4).getBlockState(pos);
   }

   public FluidState getFluidState(BlockPos pos) {
      return this.getChunk(pos).getFluidState(pos);
   }

   @Nullable
   public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, Predicate<Entity> targetPredicate) {
      return null;
   }

   public int getAmbientDarkness() {
      return 0;
   }

   public BiomeAccess getBiomeAccess() {
      return this.biomeAccess;
   }

   public Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
      return this.world.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
   }

   @Environment(EnvType.CLIENT)
   public float getBrightness(Direction direction, boolean shaded) {
      return 1.0F;
   }

   public LightingProvider getLightingProvider() {
      return this.world.getLightingProvider();
   }

   public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
      BlockState blockState = this.getBlockState(pos);
      if (blockState.isAir()) {
         return false;
      } else {
         if (drop) {
            BlockEntity blockEntity = blockState.getBlock().hasBlockEntity() ? this.getBlockEntity(pos) : null;
            Block.dropStacks(blockState, this.world, pos, blockEntity, breakingEntity, ItemStack.EMPTY);
         }

         return this.setBlockState(pos, Blocks.AIR.getDefaultState(), 3, maxUpdateDepth);
      }
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      Chunk chunk = this.getChunk(pos);
      BlockEntity blockEntity = chunk.getBlockEntity(pos);
      if (blockEntity != null) {
         return blockEntity;
      } else {
         CompoundTag compoundTag = chunk.getBlockEntityTag(pos);
         BlockState blockState = chunk.getBlockState(pos);
         if (compoundTag != null) {
            if ("DUMMY".equals(compoundTag.getString("id"))) {
               Block block = blockState.getBlock();
               if (!(block instanceof BlockEntityProvider)) {
                  return null;
               }

               blockEntity = ((BlockEntityProvider)block).createBlockEntity(this.world);
            } else {
               blockEntity = BlockEntity.createFromTag(blockState, compoundTag);
            }

            if (blockEntity != null) {
               chunk.setBlockEntity(pos, blockEntity);
               return blockEntity;
            }
         }

         if (blockState.getBlock() instanceof BlockEntityProvider) {
            LOGGER.warn((String)"Tried to access a block entity before it was created. {}", (Object)pos);
         }

         return null;
      }
   }

   public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
      Chunk chunk = this.getChunk(pos);
      BlockState blockState = chunk.setBlockState(pos, state, false);
      if (blockState != null) {
         this.world.onBlockChanged(pos, blockState, state);
      }

      Block block = state.getBlock();
      if (block.hasBlockEntity()) {
         if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.field_12807) {
            chunk.setBlockEntity(pos, ((BlockEntityProvider)block).createBlockEntity(this));
         } else {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt("x", pos.getX());
            compoundTag.putInt("y", pos.getY());
            compoundTag.putInt("z", pos.getZ());
            compoundTag.putString("id", "DUMMY");
            chunk.addPendingBlockEntityTag(compoundTag);
         }
      } else if (blockState != null && blockState.getBlock().hasBlockEntity()) {
         chunk.removeBlockEntity(pos);
      }

      if (state.shouldPostProcess(this, pos)) {
         this.markBlockForPostProcessing(pos);
      }

      return true;
   }

   private void markBlockForPostProcessing(BlockPos pos) {
      this.getChunk(pos).markBlockForPostProcessing(pos);
   }

   public boolean spawnEntity(Entity entity) {
      int i = MathHelper.floor(entity.getX() / 16.0D);
      int j = MathHelper.floor(entity.getZ() / 16.0D);
      this.getChunk(i, j).addEntity(entity);
      return true;
   }

   public boolean removeBlock(BlockPos pos, boolean move) {
      return this.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
   }

   public WorldBorder getWorldBorder() {
      return this.world.getWorldBorder();
   }

   public boolean isClient() {
      return false;
   }

   @Deprecated
   public ServerWorld toServerWorld() {
      return this.world;
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.world.getRegistryManager();
   }

   public WorldProperties getLevelProperties() {
      return this.levelProperties;
   }

   public LocalDifficulty getLocalDifficulty(BlockPos pos) {
      if (!this.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
         throw new RuntimeException("We are asking a region for a chunk out of bound");
      } else {
         return new LocalDifficulty(this.world.getDifficulty(), this.world.getTimeOfDay(), 0L, this.world.getMoonSize());
      }
   }

   public ChunkManager getChunkManager() {
      return this.world.getChunkManager();
   }

   public long getSeed() {
      return this.seed;
   }

   public TickScheduler<Block> getBlockTickScheduler() {
      return this.blockTickScheduler;
   }

   public TickScheduler<Fluid> getFluidTickScheduler() {
      return this.fluidTickScheduler;
   }

   public int getSeaLevel() {
      return this.world.getSeaLevel();
   }

   public Random getRandom() {
      return this.random;
   }

   public int getTopY(Heightmap.Type heightmap, int x, int z) {
      return this.getChunk(x >> 4, z >> 4).sampleHeightmap(heightmap, x & 15, z & 15) + 1;
   }

   public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
   }

   public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
   }

   public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
   }

   public DimensionType getDimension() {
      return this.dimension;
   }

   public boolean testBlockState(BlockPos pos, Predicate<BlockState> state) {
      return state.test(this.getBlockState(pos));
   }

   public <T extends Entity> List<T> getEntitiesByClass(Class<? extends T> entityClass, Box box, @Nullable Predicate<? super T> predicate) {
      return Collections.emptyList();
   }

   public List<Entity> getOtherEntities(@Nullable Entity except, Box box, @Nullable Predicate<? super Entity> predicate) {
      return Collections.emptyList();
   }

   public List<PlayerEntity> getPlayers() {
      return Collections.emptyList();
   }

   public Stream<? extends StructureStart<?>> getStructures(ChunkSectionPos pos, StructureFeature<?> feature) {
      return this.field_26822.getStructuresWithChildren(pos, feature);
   }
}
