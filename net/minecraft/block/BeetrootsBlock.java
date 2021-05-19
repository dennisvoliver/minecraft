package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BeetrootsBlock extends CropBlock {
   public static final IntProperty AGE;
   private static final VoxelShape[] AGE_TO_SHAPE;

   public BeetrootsBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public IntProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 3;
   }

   @Environment(EnvType.CLIENT)
   protected ItemConvertible getSeedsItem() {
      return Items.BEETROOT_SEEDS;
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (random.nextInt(3) != 0) {
         super.randomTick(state, world, pos, random);
      }

   }

   protected int getGrowthAmount(World world) {
      return super.getGrowthAmount(world) / 3;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return AGE_TO_SHAPE[(Integer)state.get(this.getAgeProperty())];
   }

   static {
      AGE = Properties.AGE_3;
      AGE_TO_SHAPE = new VoxelShape[]{Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D)};
   }
}
