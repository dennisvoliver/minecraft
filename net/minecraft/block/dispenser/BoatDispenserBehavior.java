package net.minecraft.block.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BoatDispenserBehavior extends ItemDispenserBehavior {
   private final ItemDispenserBehavior itemDispenser = new ItemDispenserBehavior();
   private final BoatEntity.Type boatType;

   public BoatDispenserBehavior(BoatEntity.Type type) {
      this.boatType = type;
   }

   public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
      Direction direction = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
      World world = pointer.getWorld();
      double d = pointer.getX() + (double)((float)direction.getOffsetX() * 1.125F);
      double e = pointer.getY() + (double)((float)direction.getOffsetY() * 1.125F);
      double f = pointer.getZ() + (double)((float)direction.getOffsetZ() * 1.125F);
      BlockPos blockPos = pointer.getBlockPos().offset(direction);
      double i;
      if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
         i = 1.0D;
      } else {
         if (!world.getBlockState(blockPos).isAir() || !world.getFluidState(blockPos.down()).isIn(FluidTags.WATER)) {
            return this.itemDispenser.dispense(pointer, stack);
         }

         i = 0.0D;
      }

      BoatEntity boatEntity = new BoatEntity(world, d, e + i, f);
      boatEntity.setBoatType(this.boatType);
      boatEntity.yaw = direction.asRotation();
      world.spawnEntity(boatEntity);
      stack.decrement(1);
      return stack;
   }

   protected void playSound(BlockPointer pointer) {
      pointer.getWorld().syncWorldEvent(1000, pointer.getBlockPos(), 0);
   }
}
