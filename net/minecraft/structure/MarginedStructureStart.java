package net.minecraft.structure;

import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public abstract class MarginedStructureStart<C extends FeatureConfig> extends StructureStart<C> {
   public MarginedStructureStart(StructureFeature<C> structureFeature, int i, int j, BlockBox blockBox, int k, long l) {
      super(structureFeature, i, j, blockBox, k, l);
   }

   protected void setBoundingBoxFromChildren() {
      super.setBoundingBoxFromChildren();
      int i = true;
      BlockBox var10000 = this.boundingBox;
      var10000.minX -= 12;
      var10000 = this.boundingBox;
      var10000.minY -= 12;
      var10000 = this.boundingBox;
      var10000.minZ -= 12;
      var10000 = this.boundingBox;
      var10000.maxX += 12;
      var10000 = this.boundingBox;
      var10000.maxY += 12;
      var10000 = this.boundingBox;
      var10000.maxZ += 12;
   }
}
