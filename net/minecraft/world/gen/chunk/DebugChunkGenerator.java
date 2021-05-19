package net.minecraft.world.gen.chunk;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;

public class DebugChunkGenerator extends ChunkGenerator {
   public static final Codec<DebugChunkGenerator> CODEC;
   private static final List<BlockState> BLOCK_STATES;
   private static final int X_SIDE_LENGTH;
   private static final int Z_SIDE_LENGTH;
   protected static final BlockState AIR;
   protected static final BlockState BARRIER;
   private final Registry<Biome> biomeRegistry;

   public DebugChunkGenerator(Registry<Biome> biomeRegistry) {
      super(new FixedBiomeSource((Biome)biomeRegistry.getOrThrow(BiomeKeys.PLAINS)), new StructuresConfig(false));
      this.biomeRegistry = biomeRegistry;
   }

   public Registry<Biome> getBiomeRegistry() {
      return this.biomeRegistry;
   }

   protected Codec<? extends ChunkGenerator> getCodec() {
      return CODEC;
   }

   @Environment(EnvType.CLIENT)
   public ChunkGenerator withSeed(long seed) {
      return this;
   }

   public void buildSurface(ChunkRegion region, Chunk chunk) {
   }

   public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
   }

   public void generateFeatures(ChunkRegion region, StructureAccessor accessor) {
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      int i = region.getCenterChunkX();
      int j = region.getCenterChunkZ();

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            int m = (i << 4) + k;
            int n = (j << 4) + l;
            region.setBlockState(mutable.set(m, 60, n), BARRIER, 2);
            BlockState blockState = getBlockState(m, n);
            if (blockState != null) {
               region.setBlockState(mutable.set(m, 70, n), blockState, 2);
            }
         }
      }

   }

   public void populateNoise(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
   }

   public int getHeight(int x, int z, Heightmap.Type heightmapType) {
      return 0;
   }

   public BlockView getColumnSample(int x, int z) {
      return new VerticalBlockSample(new BlockState[0]);
   }

   public static BlockState getBlockState(int x, int z) {
      BlockState blockState = AIR;
      if (x > 0 && z > 0 && x % 2 != 0 && z % 2 != 0) {
         x /= 2;
         z /= 2;
         if (x <= X_SIDE_LENGTH && z <= Z_SIDE_LENGTH) {
            int i = MathHelper.abs(x * X_SIDE_LENGTH + z);
            if (i < BLOCK_STATES.size()) {
               blockState = (BlockState)BLOCK_STATES.get(i);
            }
         }
      }

      return blockState;
   }

   static {
      CODEC = RegistryLookupCodec.of(Registry.BIOME_KEY).xmap(DebugChunkGenerator::new, DebugChunkGenerator::getBiomeRegistry).stable().codec();
      BLOCK_STATES = (List)StreamSupport.stream(Registry.BLOCK.spliterator(), false).flatMap((block) -> {
         return block.getStateManager().getStates().stream();
      }).collect(Collectors.toList());
      X_SIDE_LENGTH = MathHelper.ceil(MathHelper.sqrt((float)BLOCK_STATES.size()));
      Z_SIDE_LENGTH = MathHelper.ceil((float)BLOCK_STATES.size() / (float)X_SIDE_LENGTH);
      AIR = Blocks.AIR.getDefaultState();
      BARRIER = Blocks.BARRIER.getDefaultState();
   }
}
