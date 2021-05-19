package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;

public class BlockState extends AbstractBlock.AbstractBlockState {
   public static final Codec<BlockState> CODEC;

   public BlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<BlockState> mapCodec) {
      super(block, immutableMap, mapCodec);
   }

   protected BlockState asBlockState() {
      return this;
   }

   static {
      CODEC = createCodec(Registry.BLOCK, Block::getDefaultState).stable();
   }
}
