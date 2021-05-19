package net.minecraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ScreenHandlerFactory {
   @Nullable
   ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player);
}
