package net.minecraft.world.chunk;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class EmptyChunk extends WorldChunk {
   private static final Biome[] BIOMES;

   public EmptyChunk(World world, ChunkPos pos) {
      super(world, pos, new BiomeArray(world.getRegistryManager().get(Registry.BIOME_KEY), BIOMES));
   }

   public BlockState getBlockState(BlockPos pos) {
      return Blocks.VOID_AIR.getDefaultState();
   }

   @Nullable
   public BlockState setBlockState(BlockPos pos, BlockState state, boolean moved) {
      return null;
   }

   public FluidState getFluidState(BlockPos pos) {
      return Fluids.EMPTY.getDefaultState();
   }

   @Nullable
   public LightingProvider getLightingProvider() {
      return null;
   }

   public int getLuminance(BlockPos pos) {
      return 0;
   }

   public void addEntity(Entity entity) {
   }

   public void remove(Entity entity) {
   }

   public void remove(Entity entity, int section) {
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos, WorldChunk.CreationType creationType) {
      return null;
   }

   public void addBlockEntity(BlockEntity blockEntity) {
   }

   public void setBlockEntity(BlockPos pos, BlockEntity blockEntity) {
   }

   public void removeBlockEntity(BlockPos pos) {
   }

   public void markDirty() {
   }

   public void collectOtherEntities(@Nullable Entity except, Box box, List<Entity> entityList, Predicate<? super Entity> predicate) {
   }

   public <T extends Entity> void collectEntitiesByClass(Class<? extends T> entityClass, Box box, List<T> result, Predicate<? super T> predicate) {
   }

   public boolean isEmpty() {
      return true;
   }

   public boolean areSectionsEmptyBetween(int lowerHeight, int upperHeight) {
      return true;
   }

   public ChunkHolder.LevelType getLevelType() {
      return ChunkHolder.LevelType.BORDER;
   }

   static {
      BIOMES = (Biome[])Util.make(new Biome[BiomeArray.DEFAULT_LENGTH], (biomes) -> {
         Arrays.fill(biomes, BuiltinBiomes.PLAINS);
      });
   }
}
