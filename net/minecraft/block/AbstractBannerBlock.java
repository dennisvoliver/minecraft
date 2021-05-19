package net.minecraft.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBannerBlock extends BlockWithEntity {
   private final DyeColor color;

   protected AbstractBannerBlock(DyeColor color, AbstractBlock.Settings settings) {
      super(settings);
      this.color = color;
   }

   public boolean canMobSpawnInside() {
      return true;
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new BannerBlockEntity(this.color);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof BannerBlockEntity) {
            ((BannerBlockEntity)blockEntity).setCustomName(itemStack.getName());
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      return blockEntity instanceof BannerBlockEntity ? ((BannerBlockEntity)blockEntity).getPickStack(state) : super.getPickStack(world, pos, state);
   }

   public DyeColor getColor() {
      return this.color;
   }
}
