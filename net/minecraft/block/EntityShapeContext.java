package net.minecraft.block;

import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class EntityShapeContext implements ShapeContext {
   protected static final ShapeContext ABSENT;
   private final boolean descending;
   private final double minY;
   private final Item heldItem;
   private final Predicate<Fluid> field_24425;

   protected EntityShapeContext(boolean descending, double minY, Item heldItem, Predicate<Fluid> predicate) {
      this.descending = descending;
      this.minY = minY;
      this.heldItem = heldItem;
      this.field_24425 = predicate;
   }

   @Deprecated
   protected EntityShapeContext(Entity entity) {
      boolean var10001 = entity.isDescending();
      double var10002 = entity.getY();
      Item var10003 = entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandStack().getItem() : Items.AIR;
      Predicate var2;
      if (entity instanceof LivingEntity) {
         LivingEntity var10004 = (LivingEntity)entity;
         ((LivingEntity)entity).getClass();
         var2 = var10004::canWalkOnFluid;
      } else {
         var2 = (fluid) -> {
            return false;
         };
      }

      this(var10001, var10002, var10003, var2);
   }

   public boolean isHolding(Item item) {
      return this.heldItem == item;
   }

   public boolean method_27866(FluidState state, FlowableFluid fluid) {
      return this.field_24425.test(fluid) && !state.getFluid().matchesType(fluid);
   }

   public boolean isDescending() {
      return this.descending;
   }

   public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
      return this.minY > (double)pos.getY() + shape.getMax(Direction.Axis.Y) - 9.999999747378752E-6D;
   }

   static {
      ABSENT = new EntityShapeContext(false, -1.7976931348623157E308D, Items.AIR, (fluid) -> {
         return false;
      }) {
         public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
            return defaultValue;
         }
      };
   }
}
