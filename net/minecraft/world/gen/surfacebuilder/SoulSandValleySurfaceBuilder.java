package net.minecraft.world.gen.surfacebuilder;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class SoulSandValleySurfaceBuilder extends AbstractNetherSurfaceBuilder {
   private static final BlockState SOUL_SAND;
   private static final BlockState SOUL_SOIL;
   private static final BlockState GRAVEL;
   private static final ImmutableList<BlockState> SURFACE_STATES;

   public SoulSandValleySurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
      super(codec);
   }

   protected ImmutableList<BlockState> getSurfaceStates() {
      return SURFACE_STATES;
   }

   protected ImmutableList<BlockState> getUnderLavaStates() {
      return SURFACE_STATES;
   }

   protected BlockState getLavaShoreState() {
      return GRAVEL;
   }

   static {
      SOUL_SAND = Blocks.SOUL_SAND.getDefaultState();
      SOUL_SOIL = Blocks.SOUL_SOIL.getDefaultState();
      GRAVEL = Blocks.GRAVEL.getDefaultState();
      SURFACE_STATES = ImmutableList.of(SOUL_SAND, SOUL_SOIL);
   }
}
