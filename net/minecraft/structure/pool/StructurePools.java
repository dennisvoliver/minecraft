package net.minecraft.structure.pool;

import com.google.common.collect.ImmutableList;
import net.minecraft.structure.BastionRemnantGenerator;
import net.minecraft.structure.PillagerOutpostGenerator;
import net.minecraft.structure.VillageGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class StructurePools {
   public static final RegistryKey<StructurePool> EMPTY;
   private static final StructurePool INVALID;

   public static StructurePool register(StructurePool templatePool) {
      return (StructurePool)BuiltinRegistries.add(BuiltinRegistries.STRUCTURE_POOL, (Identifier)templatePool.getId(), templatePool);
   }

   public static StructurePool initDefaultPools() {
      BastionRemnantGenerator.init();
      PillagerOutpostGenerator.init();
      VillageGenerator.init();
      return INVALID;
   }

   static {
      EMPTY = RegistryKey.of(Registry.TEMPLATE_POOL_WORLDGEN, new Identifier("empty"));
      INVALID = register(new StructurePool(EMPTY.getValue(), EMPTY.getValue(), ImmutableList.of(), StructurePool.Projection.RIGID));
      initDefaultPools();
   }
}
