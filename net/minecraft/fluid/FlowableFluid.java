package net.minecraft.fluid;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.Material;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/**
 * Represents a fluid which can flow.
 */
public abstract class FlowableFluid extends Fluid {
   public static final BooleanProperty FALLING;
   public static final IntProperty LEVEL;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.NeighborGroup>> field_15901;
   private final Map<FluidState, VoxelShape> shapeCache = Maps.newIdentityHashMap();

   protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
      builder.add(FALLING);
   }

   public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
      double d = 0.0D;
      double e = 0.0D;
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      Iterator var9 = Direction.Type.HORIZONTAL.iterator();

      while(var9.hasNext()) {
         Direction direction = (Direction)var9.next();
         mutable.set(pos, direction);
         FluidState fluidState = world.getFluidState(mutable);
         if (this.method_15748(fluidState)) {
            float f = fluidState.getHeight();
            float g = 0.0F;
            if (f == 0.0F) {
               if (!world.getBlockState(mutable).getMaterial().blocksMovement()) {
                  BlockPos blockPos = mutable.down();
                  FluidState fluidState2 = world.getFluidState(blockPos);
                  if (this.method_15748(fluidState2)) {
                     f = fluidState2.getHeight();
                     if (f > 0.0F) {
                        g = state.getHeight() - (f - 0.8888889F);
                     }
                  }
               }
            } else if (f > 0.0F) {
               g = state.getHeight() - f;
            }

            if (g != 0.0F) {
               d += (double)((float)direction.getOffsetX() * g);
               e += (double)((float)direction.getOffsetZ() * g);
            }
         }
      }

      Vec3d vec3d = new Vec3d(d, 0.0D, e);
      if ((Boolean)state.get(FALLING)) {
         Iterator var17 = Direction.Type.HORIZONTAL.iterator();

         Direction direction2;
         do {
            if (!var17.hasNext()) {
               return vec3d.normalize();
            }

            direction2 = (Direction)var17.next();
            mutable.set(pos, direction2);
         } while(!this.method_15749(world, mutable, direction2) && !this.method_15749(world, mutable.up(), direction2));

         vec3d = vec3d.normalize().add(0.0D, -6.0D, 0.0D);
      }

      return vec3d.normalize();
   }

   private boolean method_15748(FluidState state) {
      return state.isEmpty() || state.getFluid().matchesType(this);
   }

   protected boolean method_15749(BlockView world, BlockPos pos, Direction direction) {
      BlockState blockState = world.getBlockState(pos);
      FluidState fluidState = world.getFluidState(pos);
      if (fluidState.getFluid().matchesType(this)) {
         return false;
      } else if (direction == Direction.UP) {
         return true;
      } else {
         return blockState.getMaterial() == Material.ICE ? false : blockState.isSideSolidFullSquare(world, pos, direction);
      }
   }

   protected void tryFlow(WorldAccess world, BlockPos fluidPos, FluidState state) {
      if (!state.isEmpty()) {
         BlockState blockState = world.getBlockState(fluidPos);
         BlockPos blockPos = fluidPos.down();
         BlockState blockState2 = world.getBlockState(blockPos);
         FluidState fluidState = this.getUpdatedState(world, blockPos, blockState2);
         if (this.canFlow(world, fluidPos, blockState, Direction.DOWN, blockPos, blockState2, world.getFluidState(blockPos), fluidState.getFluid())) {
            this.flow(world, blockPos, blockState2, Direction.DOWN, fluidState);
            if (this.method_15740(world, fluidPos) >= 3) {
               this.method_15744(world, fluidPos, state, blockState);
            }
         } else if (state.isStill() || !this.method_15736(world, fluidState.getFluid(), fluidPos, blockState, blockPos, blockState2)) {
            this.method_15744(world, fluidPos, state, blockState);
         }

      }
   }

   private void method_15744(WorldAccess world, BlockPos pos, FluidState fluidState, BlockState blockState) {
      int i = fluidState.getLevel() - this.getLevelDecreasePerBlock(world);
      if ((Boolean)fluidState.get(FALLING)) {
         i = 7;
      }

      if (i > 0) {
         Map<Direction, FluidState> map = this.getSpread(world, pos, blockState);
         Iterator var7 = map.entrySet().iterator();

         while(var7.hasNext()) {
            Entry<Direction, FluidState> entry = (Entry)var7.next();
            Direction direction = (Direction)entry.getKey();
            FluidState fluidState2 = (FluidState)entry.getValue();
            BlockPos blockPos = pos.offset(direction);
            BlockState blockState2 = world.getBlockState(blockPos);
            if (this.canFlow(world, pos, blockState, direction, blockPos, blockState2, world.getFluidState(blockPos), fluidState2.getFluid())) {
               this.flow(world, blockPos, blockState2, direction, fluidState2);
            }
         }

      }
   }

   protected FluidState getUpdatedState(WorldView world, BlockPos pos, BlockState state) {
      int i = 0;
      int j = 0;
      Iterator var6 = Direction.Type.HORIZONTAL.iterator();

      while(var6.hasNext()) {
         Direction direction = (Direction)var6.next();
         BlockPos blockPos = pos.offset(direction);
         BlockState blockState = world.getBlockState(blockPos);
         FluidState fluidState = blockState.getFluidState();
         if (fluidState.getFluid().matchesType(this) && this.receivesFlow(direction, world, pos, state, blockPos, blockState)) {
            if (fluidState.isStill()) {
               ++j;
            }

            i = Math.max(i, fluidState.getLevel());
         }
      }

      if (this.isInfinite() && j >= 2) {
         BlockState blockState2 = world.getBlockState(pos.down());
         FluidState fluidState2 = blockState2.getFluidState();
         if (blockState2.getMaterial().isSolid() || this.isMatchingAndStill(fluidState2)) {
            return this.getStill(false);
         }
      }

      BlockPos blockPos2 = pos.up();
      BlockState blockState3 = world.getBlockState(blockPos2);
      FluidState fluidState3 = blockState3.getFluidState();
      if (!fluidState3.isEmpty() && fluidState3.getFluid().matchesType(this) && this.receivesFlow(Direction.UP, world, pos, state, blockPos2, blockState3)) {
         return this.getFlowing(8, true);
      } else {
         int k = i - this.getLevelDecreasePerBlock(world);
         return k <= 0 ? Fluids.EMPTY.getDefaultState() : this.getFlowing(k, false);
      }
   }

   private boolean receivesFlow(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
      Object2ByteLinkedOpenHashMap object2ByteLinkedOpenHashMap2;
      if (!state.getBlock().hasDynamicBounds() && !fromState.getBlock().hasDynamicBounds()) {
         object2ByteLinkedOpenHashMap2 = (Object2ByteLinkedOpenHashMap)field_15901.get();
      } else {
         object2ByteLinkedOpenHashMap2 = null;
      }

      Block.NeighborGroup neighborGroup2;
      if (object2ByteLinkedOpenHashMap2 != null) {
         neighborGroup2 = new Block.NeighborGroup(state, fromState, face);
         byte b = object2ByteLinkedOpenHashMap2.getAndMoveToFirst(neighborGroup2);
         if (b != 127) {
            return b != 0;
         }
      } else {
         neighborGroup2 = null;
      }

      VoxelShape voxelShape = state.getCollisionShape(world, pos);
      VoxelShape voxelShape2 = fromState.getCollisionShape(world, fromPos);
      boolean bl = !VoxelShapes.adjacentSidesCoverSquare(voxelShape, voxelShape2, face);
      if (object2ByteLinkedOpenHashMap2 != null) {
         if (object2ByteLinkedOpenHashMap2.size() == 200) {
            object2ByteLinkedOpenHashMap2.removeLastByte();
         }

         object2ByteLinkedOpenHashMap2.putAndMoveToFirst(neighborGroup2, (byte)(bl ? 1 : 0));
      }

      return bl;
   }

   public abstract Fluid getFlowing();

   public FluidState getFlowing(int level, boolean falling) {
      return (FluidState)((FluidState)this.getFlowing().getDefaultState().with(LEVEL, level)).with(FALLING, falling);
   }

   public abstract Fluid getStill();

   public FluidState getStill(boolean falling) {
      return (FluidState)this.getStill().getDefaultState().with(FALLING, falling);
   }

   protected abstract boolean isInfinite();

   protected void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
      if (state.getBlock() instanceof FluidFillable) {
         ((FluidFillable)state.getBlock()).tryFillWithFluid(world, pos, state, fluidState);
      } else {
         if (!state.isAir()) {
            this.beforeBreakingBlock(world, pos, state);
         }

         world.setBlockState(pos, fluidState.getBlockState(), 3);
      }

   }

   protected abstract void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state);

   private static short method_15747(BlockPos blockPos, BlockPos blockPos2) {
      int i = blockPos2.getX() - blockPos.getX();
      int j = blockPos2.getZ() - blockPos.getZ();
      return (short)((i + 128 & 255) << 8 | j + 128 & 255);
   }

   protected int method_15742(WorldView world, BlockPos blockPos, int i, Direction direction, BlockState blockState, BlockPos blockPos2, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
      int j = 1000;
      Iterator var10 = Direction.Type.HORIZONTAL.iterator();

      while(var10.hasNext()) {
         Direction direction2 = (Direction)var10.next();
         if (direction2 != direction) {
            BlockPos blockPos3 = blockPos.offset(direction2);
            short s = method_15747(blockPos2, blockPos3);
            Pair<BlockState, FluidState> pair = (Pair)short2ObjectMap.computeIfAbsent(s, (ix) -> {
               BlockState blockState = world.getBlockState(blockPos3);
               return Pair.of(blockState, blockState.getFluidState());
            });
            BlockState blockState2 = (BlockState)pair.getFirst();
            FluidState fluidState = (FluidState)pair.getSecond();
            if (this.canFlowThrough(world, this.getFlowing(), blockPos, blockState, direction2, blockPos3, blockState2, fluidState)) {
               boolean bl = short2BooleanMap.computeIfAbsent(s, (ix) -> {
                  BlockPos blockPos2 = blockPos3.down();
                  BlockState blockState2x = world.getBlockState(blockPos2);
                  return this.method_15736(world, this.getFlowing(), blockPos3, blockState2, blockPos2, blockState2x);
               });
               if (bl) {
                  return i;
               }

               if (i < this.getFlowSpeed(world)) {
                  int k = this.method_15742(world, blockPos3, i + 1, direction2.getOpposite(), blockState2, blockPos2, short2ObjectMap, short2BooleanMap);
                  if (k < j) {
                     j = k;
                  }
               }
            }
         }
      }

      return j;
   }

   private boolean method_15736(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
      if (!this.receivesFlow(Direction.DOWN, world, pos, state, fromPos, fromState)) {
         return false;
      } else {
         return fromState.getFluidState().getFluid().matchesType(this) ? true : this.canFill(world, fromPos, fromState, fluid);
      }
   }

   private boolean canFlowThrough(BlockView world, Fluid fluid, BlockPos pos, BlockState state, Direction face, BlockPos fromPos, BlockState fromState, FluidState fluidState) {
      return !this.isMatchingAndStill(fluidState) && this.receivesFlow(face, world, pos, state, fromPos, fromState) && this.canFill(world, fromPos, fromState, fluid);
   }

   private boolean isMatchingAndStill(FluidState state) {
      return state.getFluid().matchesType(this) && state.isStill();
   }

   protected abstract int getFlowSpeed(WorldView world);

   private int method_15740(WorldView world, BlockPos pos) {
      int i = 0;
      Iterator var4 = Direction.Type.HORIZONTAL.iterator();

      while(var4.hasNext()) {
         Direction direction = (Direction)var4.next();
         BlockPos blockPos = pos.offset(direction);
         FluidState fluidState = world.getFluidState(blockPos);
         if (this.isMatchingAndStill(fluidState)) {
            ++i;
         }
      }

      return i;
   }

   protected Map<Direction, FluidState> getSpread(WorldView world, BlockPos pos, BlockState state) {
      int i = 1000;
      Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
      Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap();
      Short2BooleanMap short2BooleanMap = new Short2BooleanOpenHashMap();
      Iterator var8 = Direction.Type.HORIZONTAL.iterator();

      while(var8.hasNext()) {
         Direction direction = (Direction)var8.next();
         BlockPos blockPos = pos.offset(direction);
         short s = method_15747(pos, blockPos);
         Pair<BlockState, FluidState> pair = (Pair)short2ObjectMap.computeIfAbsent(s, (ix) -> {
            BlockState blockState = world.getBlockState(blockPos);
            return Pair.of(blockState, blockState.getFluidState());
         });
         BlockState blockState = (BlockState)pair.getFirst();
         FluidState fluidState = (FluidState)pair.getSecond();
         FluidState fluidState2 = this.getUpdatedState(world, blockPos, blockState);
         if (this.canFlowThrough(world, fluidState2.getFluid(), pos, state, direction, blockPos, blockState, fluidState)) {
            BlockPos blockPos2 = blockPos.down();
            boolean bl = short2BooleanMap.computeIfAbsent(s, (ix) -> {
               BlockState blockState2 = world.getBlockState(blockPos2);
               return this.method_15736(world, this.getFlowing(), blockPos, blockState, blockPos2, blockState2);
            });
            int k;
            if (bl) {
               k = 0;
            } else {
               k = this.method_15742(world, blockPos, 1, direction.getOpposite(), blockState, pos, short2ObjectMap, short2BooleanMap);
            }

            if (k < i) {
               map.clear();
            }

            if (k <= i) {
               map.put(direction, fluidState2);
               i = k;
            }
         }
      }

      return map;
   }

   private boolean canFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
      Block block = state.getBlock();
      if (block instanceof FluidFillable) {
         return ((FluidFillable)block).canFillWithFluid(world, pos, state, fluid);
      } else if (!(block instanceof DoorBlock) && !block.isIn(BlockTags.SIGNS) && block != Blocks.LADDER && block != Blocks.SUGAR_CANE && block != Blocks.BUBBLE_COLUMN) {
         Material material = state.getMaterial();
         if (material != Material.PORTAL && material != Material.STRUCTURE_VOID && material != Material.UNDERWATER_PLANT && material != Material.REPLACEABLE_UNDERWATER_PLANT) {
            return !material.blocksMovement();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected boolean canFlow(BlockView world, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo, BlockState flowToBlockState, FluidState fluidState, Fluid fluid) {
      return fluidState.canBeReplacedWith(world, flowTo, fluid, flowDirection) && this.receivesFlow(flowDirection, world, fluidPos, fluidBlockState, flowTo, flowToBlockState) && this.canFill(world, flowTo, flowToBlockState, fluid);
   }

   protected abstract int getLevelDecreasePerBlock(WorldView world);

   protected int getNextTickDelay(World world, BlockPos pos, FluidState oldState, FluidState newState) {
      return this.getTickRate(world);
   }

   public void onScheduledTick(World world, BlockPos pos, FluidState state) {
      if (!state.isStill()) {
         FluidState fluidState = this.getUpdatedState(world, pos, world.getBlockState(pos));
         int i = this.getNextTickDelay(world, pos, state, fluidState);
         if (fluidState.isEmpty()) {
            state = fluidState;
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
         } else if (!fluidState.equals(state)) {
            state = fluidState;
            BlockState blockState = fluidState.getBlockState();
            world.setBlockState(pos, blockState, 2);
            world.getFluidTickScheduler().schedule(pos, fluidState.getFluid(), i);
            world.updateNeighborsAlways(pos, blockState.getBlock());
         }
      }

      this.tryFlow(world, pos, state);
   }

   protected static int method_15741(FluidState state) {
      return state.isStill() ? 0 : 8 - Math.min(state.getLevel(), 8) + ((Boolean)state.get(FALLING) ? 8 : 0);
   }

   private static boolean isFluidAboveEqual(FluidState state, BlockView world, BlockPos pos) {
      return state.getFluid().matchesType(world.getFluidState(pos.up()).getFluid());
   }

   public float getHeight(FluidState state, BlockView world, BlockPos pos) {
      return isFluidAboveEqual(state, world, pos) ? 1.0F : state.getHeight();
   }

   public float getHeight(FluidState state) {
      return (float)state.getLevel() / 9.0F;
   }

   public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
      return state.getLevel() == 9 && isFluidAboveEqual(state, world, pos) ? VoxelShapes.fullCube() : (VoxelShape)this.shapeCache.computeIfAbsent(state, (fluidState) -> {
         return VoxelShapes.cuboid(0.0D, 0.0D, 0.0D, 1.0D, (double)fluidState.getHeight(world, pos), 1.0D);
      });
   }

   static {
      FALLING = Properties.FALLING;
      LEVEL = Properties.LEVEL_1_8;
      field_15901 = ThreadLocal.withInitial(() -> {
         Object2ByteLinkedOpenHashMap<Block.NeighborGroup> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<Block.NeighborGroup>(200) {
            protected void rehash(int i) {
            }
         };
         object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
         return object2ByteLinkedOpenHashMap;
      });
   }
}
