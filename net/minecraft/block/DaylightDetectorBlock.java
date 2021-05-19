package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class DaylightDetectorBlock extends BlockWithEntity {
   public static final IntProperty POWER;
   public static final BooleanProperty INVERTED;
   protected static final VoxelShape SHAPE;

   public DaylightDetectorBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWER, 0)).with(INVERTED, false));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Integer)state.get(POWER);
   }

   public static void updateState(BlockState state, World world, BlockPos pos) {
      if (world.getDimension().hasSkyLight()) {
         int i = world.getLightLevel(LightType.SKY, pos) - world.getAmbientDarkness();
         float f = world.getSkyAngleRadians(1.0F);
         boolean bl = (Boolean)state.get(INVERTED);
         if (bl) {
            i = 15 - i;
         } else if (i > 0) {
            float g = f < 3.1415927F ? 0.0F : 6.2831855F;
            f += (g - f) * 0.2F;
            i = Math.round((float)i * MathHelper.cos(f));
         }

         i = MathHelper.clamp(i, 0, 15);
         if ((Integer)state.get(POWER) != i) {
            world.setBlockState(pos, (BlockState)state.with(POWER, i), 3);
         }

      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (player.canModifyBlocks()) {
         if (world.isClient) {
            return ActionResult.SUCCESS;
         } else {
            BlockState blockState = (BlockState)state.cycle(INVERTED);
            world.setBlockState(pos, blockState, 4);
            updateState(blockState, world, pos);
            return ActionResult.CONSUME;
         }
      } else {
         return super.onUse(state, world, pos, player, hand, hit);
      }
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new DaylightDetectorBlockEntity();
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(POWER, INVERTED);
   }

   static {
      POWER = Properties.POWER;
      INVERTED = Properties.INVERTED;
      SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);
   }
}
