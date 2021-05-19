package net.minecraft.block;

import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class GrindstoneBlock extends WallMountedBlock {
   public static final VoxelShape WEST_FLOOR_LEG = Block.createCuboidShape(2.0D, 0.0D, 6.0D, 4.0D, 7.0D, 10.0D);
   public static final VoxelShape EAST_FLOOR_LEG = Block.createCuboidShape(12.0D, 0.0D, 6.0D, 14.0D, 7.0D, 10.0D);
   public static final VoxelShape WEST_FLOOR_HINGE = Block.createCuboidShape(2.0D, 7.0D, 5.0D, 4.0D, 13.0D, 11.0D);
   public static final VoxelShape EAST_FLOOR_HINGE = Block.createCuboidShape(12.0D, 7.0D, 5.0D, 14.0D, 13.0D, 11.0D);
   public static final VoxelShape WEST_FLOOR_SIDE;
   public static final VoxelShape EAST_FLOOR_SIDE;
   public static final VoxelShape Z_FLOOR_SIDES;
   public static final VoxelShape Z_FLOOR_SHAPE;
   public static final VoxelShape NORTH_FLOOR_LEG;
   public static final VoxelShape SOUTH_FLOOR_LEG;
   public static final VoxelShape NORTH_FLOOR_HINGE;
   public static final VoxelShape SOUTH_FLOOR_HINGE;
   public static final VoxelShape NORTH_FLOOR_SIDE;
   public static final VoxelShape SOUTH_FLOOR_SIDE;
   public static final VoxelShape X_FLOOR_SIDES;
   public static final VoxelShape X_FLOOR_SHAPE;
   public static final VoxelShape SOUTH_WALL_WEST_LEG;
   public static final VoxelShape SOUTH_WALL_EAST_LEG;
   public static final VoxelShape SOUTH_WALL_WEST_HINGE;
   public static final VoxelShape SOUTH_WALL_EAST_HINGE;
   public static final VoxelShape SOUTH_WALL_WEST_SIDE;
   public static final VoxelShape SOUTH_WALL_EAST_SIDE;
   public static final VoxelShape SOUTH_WALL_SIDES;
   public static final VoxelShape SOUTH_WALL_SHAPE;
   public static final VoxelShape NORTH_WALL_WEST_LEG;
   public static final VoxelShape NORTH_WALL_EAST_LEG;
   public static final VoxelShape NORTH_WALL_WEST_HINGE;
   public static final VoxelShape NORTH_WALL_EAST_HINGE;
   public static final VoxelShape NORTH_WALL_WEST_SIDE;
   public static final VoxelShape NORTH_WALL_EAST_SIDE;
   public static final VoxelShape NORTH_WALL_SIDES;
   public static final VoxelShape NORTH_WALL_SHAPE;
   public static final VoxelShape WEST_WALL_NORTH_LEG;
   public static final VoxelShape WEST_WALL_SOUTH_LEG;
   public static final VoxelShape WEST_WALL_NORTH_HINGE;
   public static final VoxelShape WEST_WALL_SOUTH_HINGE;
   public static final VoxelShape WEST_WALL_NORTH_SIDE;
   public static final VoxelShape WEST_WALL_SOUTH_SIDE;
   public static final VoxelShape WEST_WALL_SIDES;
   public static final VoxelShape WEST_WALL_SHAPE;
   public static final VoxelShape EAST_WALL_NORTH_LEG;
   public static final VoxelShape EAST_WALL_SOUTH_LEG;
   public static final VoxelShape EAST_WALL_NORTH_HINGE;
   public static final VoxelShape EAST_WALL_SOUTH_HINGE;
   public static final VoxelShape EAST_WALL_NORTH_SIDE;
   public static final VoxelShape EAST_WALL_SOUTH_SIDE;
   public static final VoxelShape EAST_WALL_SIDES;
   public static final VoxelShape EAST_WALL_SHAPE;
   public static final VoxelShape WEST_CEILING_LEG;
   public static final VoxelShape EAST_CEILING_LEG;
   public static final VoxelShape WEST_CEILING_HINGE;
   public static final VoxelShape EAST_CEILING_HINGE;
   public static final VoxelShape WEST_CEILING_SIDE;
   public static final VoxelShape EAST_CEILING_SIDE;
   public static final VoxelShape Z_CEILING_SIDES;
   public static final VoxelShape Z_CEILING_SHAPE;
   public static final VoxelShape NORTH_CEILING_LEG;
   public static final VoxelShape SOUTH_CEILING_LEG;
   public static final VoxelShape NORTH_CEILING_HINGE;
   public static final VoxelShape SOUTH_CEILING_HINGE;
   public static final VoxelShape NORTH_CEILING_SIDE;
   public static final VoxelShape SOUTH_CEILING_SIDE;
   public static final VoxelShape X_CEILING_SIDES;
   public static final VoxelShape X_CEILING_SHAPE;
   private static final Text TITLE;

   protected GrindstoneBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(FACE, WallMountLocation.WALL));
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   private VoxelShape getShape(BlockState state) {
      Direction direction = (Direction)state.get(FACING);
      switch((WallMountLocation)state.get(FACE)) {
      case FLOOR:
         if (direction != Direction.NORTH && direction != Direction.SOUTH) {
            return X_FLOOR_SHAPE;
         }

         return Z_FLOOR_SHAPE;
      case WALL:
         if (direction == Direction.NORTH) {
            return NORTH_WALL_SHAPE;
         } else if (direction == Direction.SOUTH) {
            return SOUTH_WALL_SHAPE;
         } else {
            if (direction == Direction.EAST) {
               return EAST_WALL_SHAPE;
            }

            return WEST_WALL_SHAPE;
         }
      case CEILING:
         if (direction != Direction.NORTH && direction != Direction.SOUTH) {
            return X_CEILING_SHAPE;
         }

         return Z_CEILING_SHAPE;
      default:
         return X_FLOOR_SHAPE;
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShape(state);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShape(state);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return true;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
         player.incrementStat(Stats.INTERACT_WITH_GRINDSTONE);
         return ActionResult.CONSUME;
      }
   }

   public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
      return new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> {
         return new GrindstoneScreenHandler(i, playerInventory, ScreenHandlerContext.create(world, pos));
      }, TITLE);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, FACE);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      WEST_FLOOR_SIDE = VoxelShapes.union(WEST_FLOOR_LEG, WEST_FLOOR_HINGE);
      EAST_FLOOR_SIDE = VoxelShapes.union(EAST_FLOOR_LEG, EAST_FLOOR_HINGE);
      Z_FLOOR_SIDES = VoxelShapes.union(WEST_FLOOR_SIDE, EAST_FLOOR_SIDE);
      Z_FLOOR_SHAPE = VoxelShapes.union(Z_FLOOR_SIDES, Block.createCuboidShape(4.0D, 4.0D, 2.0D, 12.0D, 16.0D, 14.0D));
      NORTH_FLOOR_LEG = Block.createCuboidShape(6.0D, 0.0D, 2.0D, 10.0D, 7.0D, 4.0D);
      SOUTH_FLOOR_LEG = Block.createCuboidShape(6.0D, 0.0D, 12.0D, 10.0D, 7.0D, 14.0D);
      NORTH_FLOOR_HINGE = Block.createCuboidShape(5.0D, 7.0D, 2.0D, 11.0D, 13.0D, 4.0D);
      SOUTH_FLOOR_HINGE = Block.createCuboidShape(5.0D, 7.0D, 12.0D, 11.0D, 13.0D, 14.0D);
      NORTH_FLOOR_SIDE = VoxelShapes.union(NORTH_FLOOR_LEG, NORTH_FLOOR_HINGE);
      SOUTH_FLOOR_SIDE = VoxelShapes.union(SOUTH_FLOOR_LEG, SOUTH_FLOOR_HINGE);
      X_FLOOR_SIDES = VoxelShapes.union(NORTH_FLOOR_SIDE, SOUTH_FLOOR_SIDE);
      X_FLOOR_SHAPE = VoxelShapes.union(X_FLOOR_SIDES, Block.createCuboidShape(2.0D, 4.0D, 4.0D, 14.0D, 16.0D, 12.0D));
      SOUTH_WALL_WEST_LEG = Block.createCuboidShape(2.0D, 6.0D, 0.0D, 4.0D, 10.0D, 7.0D);
      SOUTH_WALL_EAST_LEG = Block.createCuboidShape(12.0D, 6.0D, 0.0D, 14.0D, 10.0D, 7.0D);
      SOUTH_WALL_WEST_HINGE = Block.createCuboidShape(2.0D, 5.0D, 7.0D, 4.0D, 11.0D, 13.0D);
      SOUTH_WALL_EAST_HINGE = Block.createCuboidShape(12.0D, 5.0D, 7.0D, 14.0D, 11.0D, 13.0D);
      SOUTH_WALL_WEST_SIDE = VoxelShapes.union(SOUTH_WALL_WEST_LEG, SOUTH_WALL_WEST_HINGE);
      SOUTH_WALL_EAST_SIDE = VoxelShapes.union(SOUTH_WALL_EAST_LEG, SOUTH_WALL_EAST_HINGE);
      SOUTH_WALL_SIDES = VoxelShapes.union(SOUTH_WALL_WEST_SIDE, SOUTH_WALL_EAST_SIDE);
      SOUTH_WALL_SHAPE = VoxelShapes.union(SOUTH_WALL_SIDES, Block.createCuboidShape(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 16.0D));
      NORTH_WALL_WEST_LEG = Block.createCuboidShape(2.0D, 6.0D, 7.0D, 4.0D, 10.0D, 16.0D);
      NORTH_WALL_EAST_LEG = Block.createCuboidShape(12.0D, 6.0D, 7.0D, 14.0D, 10.0D, 16.0D);
      NORTH_WALL_WEST_HINGE = Block.createCuboidShape(2.0D, 5.0D, 3.0D, 4.0D, 11.0D, 9.0D);
      NORTH_WALL_EAST_HINGE = Block.createCuboidShape(12.0D, 5.0D, 3.0D, 14.0D, 11.0D, 9.0D);
      NORTH_WALL_WEST_SIDE = VoxelShapes.union(NORTH_WALL_WEST_LEG, NORTH_WALL_WEST_HINGE);
      NORTH_WALL_EAST_SIDE = VoxelShapes.union(NORTH_WALL_EAST_LEG, NORTH_WALL_EAST_HINGE);
      NORTH_WALL_SIDES = VoxelShapes.union(NORTH_WALL_WEST_SIDE, NORTH_WALL_EAST_SIDE);
      NORTH_WALL_SHAPE = VoxelShapes.union(NORTH_WALL_SIDES, Block.createCuboidShape(4.0D, 2.0D, 0.0D, 12.0D, 14.0D, 12.0D));
      WEST_WALL_NORTH_LEG = Block.createCuboidShape(7.0D, 6.0D, 2.0D, 16.0D, 10.0D, 4.0D);
      WEST_WALL_SOUTH_LEG = Block.createCuboidShape(7.0D, 6.0D, 12.0D, 16.0D, 10.0D, 14.0D);
      WEST_WALL_NORTH_HINGE = Block.createCuboidShape(3.0D, 5.0D, 2.0D, 9.0D, 11.0D, 4.0D);
      WEST_WALL_SOUTH_HINGE = Block.createCuboidShape(3.0D, 5.0D, 12.0D, 9.0D, 11.0D, 14.0D);
      WEST_WALL_NORTH_SIDE = VoxelShapes.union(WEST_WALL_NORTH_LEG, WEST_WALL_NORTH_HINGE);
      WEST_WALL_SOUTH_SIDE = VoxelShapes.union(WEST_WALL_SOUTH_LEG, WEST_WALL_SOUTH_HINGE);
      WEST_WALL_SIDES = VoxelShapes.union(WEST_WALL_NORTH_SIDE, WEST_WALL_SOUTH_SIDE);
      WEST_WALL_SHAPE = VoxelShapes.union(WEST_WALL_SIDES, Block.createCuboidShape(0.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D));
      EAST_WALL_NORTH_LEG = Block.createCuboidShape(0.0D, 6.0D, 2.0D, 9.0D, 10.0D, 4.0D);
      EAST_WALL_SOUTH_LEG = Block.createCuboidShape(0.0D, 6.0D, 12.0D, 9.0D, 10.0D, 14.0D);
      EAST_WALL_NORTH_HINGE = Block.createCuboidShape(7.0D, 5.0D, 2.0D, 13.0D, 11.0D, 4.0D);
      EAST_WALL_SOUTH_HINGE = Block.createCuboidShape(7.0D, 5.0D, 12.0D, 13.0D, 11.0D, 14.0D);
      EAST_WALL_NORTH_SIDE = VoxelShapes.union(EAST_WALL_NORTH_LEG, EAST_WALL_NORTH_HINGE);
      EAST_WALL_SOUTH_SIDE = VoxelShapes.union(EAST_WALL_SOUTH_LEG, EAST_WALL_SOUTH_HINGE);
      EAST_WALL_SIDES = VoxelShapes.union(EAST_WALL_NORTH_SIDE, EAST_WALL_SOUTH_SIDE);
      EAST_WALL_SHAPE = VoxelShapes.union(EAST_WALL_SIDES, Block.createCuboidShape(4.0D, 2.0D, 4.0D, 16.0D, 14.0D, 12.0D));
      WEST_CEILING_LEG = Block.createCuboidShape(2.0D, 9.0D, 6.0D, 4.0D, 16.0D, 10.0D);
      EAST_CEILING_LEG = Block.createCuboidShape(12.0D, 9.0D, 6.0D, 14.0D, 16.0D, 10.0D);
      WEST_CEILING_HINGE = Block.createCuboidShape(2.0D, 3.0D, 5.0D, 4.0D, 9.0D, 11.0D);
      EAST_CEILING_HINGE = Block.createCuboidShape(12.0D, 3.0D, 5.0D, 14.0D, 9.0D, 11.0D);
      WEST_CEILING_SIDE = VoxelShapes.union(WEST_CEILING_LEG, WEST_CEILING_HINGE);
      EAST_CEILING_SIDE = VoxelShapes.union(EAST_CEILING_LEG, EAST_CEILING_HINGE);
      Z_CEILING_SIDES = VoxelShapes.union(WEST_CEILING_SIDE, EAST_CEILING_SIDE);
      Z_CEILING_SHAPE = VoxelShapes.union(Z_CEILING_SIDES, Block.createCuboidShape(4.0D, 0.0D, 2.0D, 12.0D, 12.0D, 14.0D));
      NORTH_CEILING_LEG = Block.createCuboidShape(6.0D, 9.0D, 2.0D, 10.0D, 16.0D, 4.0D);
      SOUTH_CEILING_LEG = Block.createCuboidShape(6.0D, 9.0D, 12.0D, 10.0D, 16.0D, 14.0D);
      NORTH_CEILING_HINGE = Block.createCuboidShape(5.0D, 3.0D, 2.0D, 11.0D, 9.0D, 4.0D);
      SOUTH_CEILING_HINGE = Block.createCuboidShape(5.0D, 3.0D, 12.0D, 11.0D, 9.0D, 14.0D);
      NORTH_CEILING_SIDE = VoxelShapes.union(NORTH_CEILING_LEG, NORTH_CEILING_HINGE);
      SOUTH_CEILING_SIDE = VoxelShapes.union(SOUTH_CEILING_LEG, SOUTH_CEILING_HINGE);
      X_CEILING_SIDES = VoxelShapes.union(NORTH_CEILING_SIDE, SOUTH_CEILING_SIDE);
      X_CEILING_SHAPE = VoxelShapes.union(X_CEILING_SIDES, Block.createCuboidShape(2.0D, 0.0D, 4.0D, 14.0D, 12.0D, 12.0D));
      TITLE = new TranslatableText("container.grindstone_title");
   }
}
