package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexFormat {
   private final ImmutableList<VertexFormatElement> elements;
   private final IntList offsets = new IntArrayList();
   private final int size;

   public VertexFormat(ImmutableList<VertexFormatElement> immutableList) {
      this.elements = immutableList;
      int i = 0;

      VertexFormatElement vertexFormatElement;
      for(UnmodifiableIterator var3 = immutableList.iterator(); var3.hasNext(); i += vertexFormatElement.getSize()) {
         vertexFormatElement = (VertexFormatElement)var3.next();
         this.offsets.add(i);
      }

      this.size = i;
   }

   public String toString() {
      return "format: " + this.elements.size() + " elements: " + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(" "));
   }

   public int getVertexSizeInteger() {
      return this.getVertexSize() / 4;
   }

   public int getVertexSize() {
      return this.size;
   }

   public ImmutableList<VertexFormatElement> getElements() {
      return this.elements;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VertexFormat vertexFormat = (VertexFormat)o;
         return this.size != vertexFormat.size ? false : this.elements.equals(vertexFormat.elements);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.elements.hashCode();
   }

   public void startDrawing(long pointer) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this.startDrawing(pointer);
         });
      } else {
         int i = this.getVertexSize();
         List<VertexFormatElement> list = this.getElements();

         for(int j = 0; j < list.size(); ++j) {
            ((VertexFormatElement)list.get(j)).startDrawing(pointer + (long)this.offsets.getInt(j), i);
         }

      }
   }

   public void endDrawing() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::endDrawing);
      } else {
         UnmodifiableIterator var1 = this.getElements().iterator();

         while(var1.hasNext()) {
            VertexFormatElement vertexFormatElement = (VertexFormatElement)var1.next();
            vertexFormatElement.endDrawing();
         }

      }
   }
}
