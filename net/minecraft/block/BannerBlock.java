package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class BannerBlock extends AbstractBannerBlock {
   public static final IntProperty ROTATION;
   private static final Map<DyeColor, Block> COLORED_BANNERS;
   private static final VoxelShape SHAPE;

   public BannerBlock(DyeColor dyeColor, AbstractBlock.Settings settings) {
      super(dyeColor, settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ROTATION, 0));
      COLORED_BANNERS.put(dyeColor, this);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos.down()).getMaterial().isSolid();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(ROTATION, MathHelper.floor((double)((180.0F + ctx.getPlayerYaw()) * 16.0F / 360.0F) + 0.5D) & 15);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      return direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(ROTATION, rotation.rotate((Integer)state.get(ROTATION), 16));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return (BlockState)state.with(ROTATION, mirror.mirror((Integer)state.get(ROTATION), 16));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(ROTATION);
   }

   @Environment(EnvType.CLIENT)
   public static Block getForColor(DyeColor color) {
      return (Block)COLORED_BANNERS.getOrDefault(color, Blocks.WHITE_BANNER);
   }

   static {
      ROTATION = Properties.ROTATION;
      COLORED_BANNERS = Maps.newHashMap();
      SHAPE = Block.createCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
   }
}
