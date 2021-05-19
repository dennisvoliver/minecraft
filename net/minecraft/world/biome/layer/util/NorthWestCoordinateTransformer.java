package net.minecraft.world.biome.layer.util;

public interface NorthWestCoordinateTransformer extends CoordinateTransformer {
   default int transformX(int x) {
      return x - 1;
   }

   default int transformZ(int y) {
      return y - 1;
   }
}
