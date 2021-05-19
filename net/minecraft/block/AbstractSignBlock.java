package net.minecraft.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SignType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public abstract class AbstractSignBlock extends BlockWithEntity implements Waterloggable {
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape SHAPE;
   private final SignType type;

   protected AbstractSignBlock(AbstractBlock.Settings settings, SignType type) {
      super(settings);
      this.type = type;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public boolean canMobSpawnInside() {
      return true;
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new SignBlockEntity();
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack itemStack = player.getStackInHand(hand);
      boolean bl = itemStack.getItem() instanceof DyeItem && player.abilities.allowModifyWorld;
      if (world.isClient) {
         return bl ? ActionResult.SUCCESS : ActionResult.CONSUME;
      } else {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof SignBlockEntity) {
            SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
            if (bl) {
               boolean bl2 = signBlockEntity.setTextColor(((DyeItem)itemStack.getItem()).getColor());
               if (bl2 && !player.isCreative()) {
                  itemStack.decrement(1);
               }
            }

            return signBlockEntity.onActivate(player) ? ActionResult.SUCCESS : ActionResult.PASS;
         } else {
            return ActionResult.PASS;
         }
      }
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   @Environment(EnvType.CLIENT)
   public SignType getSignType() {
      return this.type;
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      SHAPE = Block.createCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
   }
}
