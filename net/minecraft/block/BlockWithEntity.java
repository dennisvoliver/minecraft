package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class BlockWithEntity extends Block implements BlockEntityProvider {
   protected BlockWithEntity(AbstractBlock.Settings settings) {
      super(settings);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
      super.onSyncedBlockEvent(state, world, pos, type, data);
      BlockEntity blockEntity = world.getBlockEntity(pos);
      return blockEntity == null ? false : blockEntity.onSyncedBlockEvent(type, data);
   }

   @Nullable
   public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory)blockEntity : null;
   }
}
