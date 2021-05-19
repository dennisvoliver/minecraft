package net.minecraft.structure.pool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class ListPoolElement extends StructurePoolElement {
   public static final Codec<ListPoolElement> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(StructurePoolElement.CODEC.listOf().fieldOf("elements").forGetter((listPoolElement) -> {
         return listPoolElement.elements;
      }), method_28883()).apply(instance, (BiFunction)(ListPoolElement::new));
   });
   private final List<StructurePoolElement> elements;

   public ListPoolElement(List<StructurePoolElement> elements, StructurePool.Projection projection) {
      super(projection);
      if (elements.isEmpty()) {
         throw new IllegalArgumentException("Elements are empty");
      } else {
         this.elements = elements;
         this.setAllElementsProjection(projection);
      }
   }

   public List<Structure.StructureBlockInfo> getStructureBlockInfos(StructureManager structureManager, BlockPos pos, BlockRotation rotation, Random random) {
      return ((StructurePoolElement)this.elements.get(0)).getStructureBlockInfos(structureManager, pos, rotation, random);
   }

   public BlockBox getBoundingBox(StructureManager structureManager, BlockPos pos, BlockRotation rotation) {
      BlockBox blockBox = BlockBox.empty();
      Iterator var5 = this.elements.iterator();

      while(var5.hasNext()) {
         StructurePoolElement structurePoolElement = (StructurePoolElement)var5.next();
         BlockBox blockBox2 = structurePoolElement.getBoundingBox(structureManager, pos, rotation);
         blockBox.encompass(blockBox2);
      }

      return blockBox;
   }

   public boolean generate(StructureManager structureManager, StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, BlockRotation blockRotation, BlockBox blockBox, Random random, boolean keepJigsaws) {
      Iterator var11 = this.elements.iterator();

      StructurePoolElement structurePoolElement;
      do {
         if (!var11.hasNext()) {
            return true;
         }

         structurePoolElement = (StructurePoolElement)var11.next();
      } while(structurePoolElement.generate(structureManager, structureWorldAccess, structureAccessor, chunkGenerator, blockPos, blockPos2, blockRotation, blockBox, random, keepJigsaws));

      return false;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.LIST_POOL_ELEMENT;
   }

   public StructurePoolElement setProjection(StructurePool.Projection projection) {
      super.setProjection(projection);
      this.setAllElementsProjection(projection);
      return this;
   }

   public String toString() {
      return "List[" + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
   }

   private void setAllElementsProjection(StructurePool.Projection projection) {
      this.elements.forEach((structurePoolElement) -> {
         structurePoolElement.setProjection(projection);
      });
   }
}
