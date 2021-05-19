package net.minecraft.util.function;

import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import org.jetbrains.annotations.Nullable;

public class MaterialPredicate implements Predicate<BlockState> {
   private static final MaterialPredicate IS_AIR;
   private final Material material;

   private MaterialPredicate(Material material) {
      this.material = material;
   }

   public static MaterialPredicate create(Material material) {
      return material == Material.AIR ? IS_AIR : new MaterialPredicate(material);
   }

   public boolean test(@Nullable BlockState blockState) {
      return blockState != null && blockState.getMaterial() == this.material;
   }

   static {
      IS_AIR = new MaterialPredicate(Material.AIR) {
         public boolean test(@Nullable BlockState blockState) {
            return blockState != null && blockState.isAir();
         }
      };
   }
}
