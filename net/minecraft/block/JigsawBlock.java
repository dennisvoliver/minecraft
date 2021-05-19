package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.enums.JigsawOrientation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.Structure;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class JigsawBlock extends Block implements BlockEntityProvider {
   public static final EnumProperty<JigsawOrientation> ORIENTATION;

   protected JigsawBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ORIENTATION, JigsawOrientation.NORTH_UP));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(ORIENTATION);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(ORIENTATION, rotation.getDirectionTransformation().mapJigsawOrientation((JigsawOrientation)state.get(ORIENTATION)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return (BlockState)state.with(ORIENTATION, mirror.getDirectionTransformation().mapJigsawOrientation((JigsawOrientation)state.get(ORIENTATION)));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction direction = ctx.getSide();
      Direction direction3;
      if (direction.getAxis() == Direction.Axis.Y) {
         direction3 = ctx.getPlayerFacing().getOpposite();
      } else {
         direction3 = Direction.UP;
      }

      return (BlockState)this.getDefaultState().with(ORIENTATION, JigsawOrientation.byDirections(direction, direction3));
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockView world) {
      return new JigsawBlockEntity();
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof JigsawBlockEntity && player.isCreativeLevelTwoOp()) {
         player.openJigsawScreen((JigsawBlockEntity)blockEntity);
         return ActionResult.success(world.isClient);
      } else {
         return ActionResult.PASS;
      }
   }

   public static boolean attachmentMatches(Structure.StructureBlockInfo info1, Structure.StructureBlockInfo info2) {
      Direction direction = getFacing(info1.state);
      Direction direction2 = getFacing(info2.state);
      Direction direction3 = getRotation(info1.state);
      Direction direction4 = getRotation(info2.state);
      JigsawBlockEntity.Joint joint = (JigsawBlockEntity.Joint)JigsawBlockEntity.Joint.byName(info1.tag.getString("joint")).orElseGet(() -> {
         return direction.getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE;
      });
      boolean bl = joint == JigsawBlockEntity.Joint.ROLLABLE;
      return direction == direction2.getOpposite() && (bl || direction3 == direction4) && info1.tag.getString("target").equals(info2.tag.getString("name"));
   }

   public static Direction getFacing(BlockState blockState) {
      return ((JigsawOrientation)blockState.get(ORIENTATION)).getFacing();
   }

   public static Direction getRotation(BlockState blockState) {
      return ((JigsawOrientation)blockState.get(ORIENTATION)).getRotation();
   }

   static {
      ORIENTATION = Properties.ORIENTATION;
   }
}
