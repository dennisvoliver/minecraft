package net.minecraft.block;

import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class DropperBlock extends DispenserBlock {
   private static final DispenserBehavior BEHAVIOR = new ItemDispenserBehavior();

   public DropperBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   protected DispenserBehavior getBehaviorForItem(ItemStack stack) {
      return BEHAVIOR;
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new DropperBlockEntity();
   }

   protected void dispense(ServerWorld serverWorld, BlockPos pos) {
      BlockPointerImpl blockPointerImpl = new BlockPointerImpl(serverWorld, pos);
      DispenserBlockEntity dispenserBlockEntity = (DispenserBlockEntity)blockPointerImpl.getBlockEntity();
      int i = dispenserBlockEntity.chooseNonEmptySlot();
      if (i < 0) {
         serverWorld.syncWorldEvent(1001, pos, 0);
      } else {
         ItemStack itemStack = dispenserBlockEntity.getStack(i);
         if (!itemStack.isEmpty()) {
            Direction direction = (Direction)serverWorld.getBlockState(pos).get(FACING);
            Inventory inventory = HopperBlockEntity.getInventoryAt(serverWorld, pos.offset(direction));
            ItemStack itemStack3;
            if (inventory == null) {
               itemStack3 = BEHAVIOR.dispense(blockPointerImpl, itemStack);
            } else {
               itemStack3 = HopperBlockEntity.transfer(dispenserBlockEntity, inventory, itemStack.copy().split(1), direction.getOpposite());
               if (itemStack3.isEmpty()) {
                  itemStack3 = itemStack.copy();
                  itemStack3.decrement(1);
               } else {
                  itemStack3 = itemStack.copy();
               }
            }

            dispenserBlockEntity.setStack(i, itemStack3);
         }
      }
   }
}
