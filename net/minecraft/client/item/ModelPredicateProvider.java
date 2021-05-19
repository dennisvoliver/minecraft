package net.minecraft.client.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface ModelPredicateProvider {
   float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity);
}
