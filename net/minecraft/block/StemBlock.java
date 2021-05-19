package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StemBlock extends PlantBlock implements Fertilizable {
   public static final IntProperty AGE;
   protected static final VoxelShape[] AGE_TO_SHAPE;
   private final GourdBlock gourdBlock;

   protected StemBlock(GourdBlock gourdBlock, AbstractBlock.Settings settings) {
      super(settings);
      this.gourdBlock = gourdBlock;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return AGE_TO_SHAPE[(Integer)state.get(AGE)];
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isOf(Blocks.FARMLAND);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.getBaseLightLevel(pos, 0) >= 9) {
         float f = CropBlock.getAvailableMoisture(this, world, pos);
         if (random.nextInt((int)(25.0F / f) + 1) == 0) {
            int i = (Integer)state.get(AGE);
            if (i < 7) {
               state = (BlockState)state.with(AGE, i + 1);
               world.setBlockState(pos, state, 2);
            } else {
               Direction direction = Direction.Type.HORIZONTAL.random(random);
               BlockPos blockPos = pos.offset(direction);
               BlockState blockState = world.getBlockState(blockPos.down());
               if (world.getBlockState(blockPos).isAir() && (blockState.isOf(Blocks.FARMLAND) || blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.COARSE_DIRT) || blockState.isOf(Blocks.PODZOL) || blockState.isOf(Blocks.GRASS_BLOCK))) {
                  world.setBlockState(blockPos, this.gourdBlock.getDefaultState());
                  world.setBlockState(pos, (BlockState)this.gourdBlock.getAttachedStem().getDefaultState().with(HorizontalFacingBlock.FACING, direction));
               }
            }
         }

      }
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   protected Item getPickItem() {
      if (this.gourdBlock == Blocks.PUMPKIN) {
         return Items.PUMPKIN_SEEDS;
      } else {
         return this.gourdBlock == Blocks.MELON ? Items.MELON_SEEDS : null;
      }
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      Item item = this.getPickItem();
      return item == null ? ItemStack.EMPTY : new ItemStack(item);
   }

   public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
      return (Integer)state.get(AGE) != 7;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      int i = Math.min(7, (Integer)state.get(AGE) + MathHelper.nextInt(world.random, 2, 5));
      BlockState blockState = (BlockState)state.with(AGE, i);
      world.setBlockState(pos, blockState, 2);
      if (i == 7) {
         blockState.randomTick(world, pos, world.random);
      }

   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }

   public GourdBlock getGourdBlock() {
      return this.gourdBlock;
   }

   static {
      AGE = Properties.AGE_7;
      AGE_TO_SHAPE = new VoxelShape[]{Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 2.0D, 9.0D), Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 4.0D, 9.0D), Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D), Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 8.0D, 9.0D), Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D), Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 12.0D, 9.0D), Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D), Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)};
   }
}
