package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public interface BlockEntityProvider {
   @Nullable
   BlockEntity createBlockEntity(BlockView world);
}
