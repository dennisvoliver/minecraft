package net.minecraft.world.gen.surfacebuilder;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class BasaltDeltasSurfaceBuilder extends AbstractNetherSurfaceBuilder {
   private static final BlockState BASALT;
   private static final BlockState BLACKSTONE;
   private static final BlockState GRAVEL;
   private static final ImmutableList<BlockState> SURFACE_STATES;
   private static final ImmutableList<BlockState> UNDER_LAVA_STATES;

   public BasaltDeltasSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
      super(codec);
   }

   protected ImmutableList<BlockState> getSurfaceStates() {
      return SURFACE_STATES;
   }

   protected ImmutableList<BlockState> getUnderLavaStates() {
      return UNDER_LAVA_STATES;
   }

   protected BlockState getLavaShoreState() {
      return GRAVEL;
   }

   static {
      BASALT = Blocks.BASALT.getDefaultState();
      BLACKSTONE = Blocks.BLACKSTONE.getDefaultState();
      GRAVEL = Blocks.GRAVEL.getDefaultState();
      SURFACE_STATES = ImmutableList.of(BASALT, BLACKSTONE);
      UNDER_LAVA_STATES = ImmutableList.of(BASALT);
   }
}
