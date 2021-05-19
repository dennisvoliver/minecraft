package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SignItem extends WallStandingBlockItem {
   public SignItem(Item.Settings settings, Block standingBlock, Block wallBlock) {
      super(standingBlock, wallBlock, settings);
   }

   protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
      boolean bl = super.postPlacement(pos, world, player, stack, state);
      if (!world.isClient && !bl && player != null) {
         player.openEditSignScreen((SignBlockEntity)world.getBlockEntity(pos));
      }

      return bl;
   }
}
