package net.minecraft.client.render.debug;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;

@Environment(EnvType.CLIENT)
public class StructureDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient field_4624;
   private final Map<DimensionType, Map<String, BlockBox>> field_4626 = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, BlockBox>> field_4627 = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, Boolean>> field_4625 = Maps.newIdentityHashMap();

   public StructureDebugRenderer(MinecraftClient minecraftClient) {
      this.field_4624 = minecraftClient;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      Camera camera = this.field_4624.gameRenderer.getCamera();
      WorldAccess worldAccess = this.field_4624.world;
      DimensionType dimensionType = worldAccess.getDimension();
      BlockPos blockPos = new BlockPos(camera.getPos().x, 0.0D, camera.getPos().z);
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
      Iterator var14;
      if (this.field_4626.containsKey(dimensionType)) {
         var14 = ((Map)this.field_4626.get(dimensionType)).values().iterator();

         while(var14.hasNext()) {
            BlockBox blockBox = (BlockBox)var14.next();
            if (blockPos.isWithinDistance(blockBox.getCenter(), 500.0D)) {
               WorldRenderer.drawBox(matrices, vertexConsumer, (double)blockBox.minX - cameraX, (double)blockBox.minY - cameraY, (double)blockBox.minZ - cameraZ, (double)(blockBox.maxX + 1) - cameraX, (double)(blockBox.maxY + 1) - cameraY, (double)(blockBox.maxZ + 1) - cameraZ, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }

      if (this.field_4627.containsKey(dimensionType)) {
         var14 = ((Map)this.field_4627.get(dimensionType)).entrySet().iterator();

         while(var14.hasNext()) {
            Entry<String, BlockBox> entry = (Entry)var14.next();
            String string = (String)entry.getKey();
            BlockBox blockBox2 = (BlockBox)entry.getValue();
            Boolean boolean_ = (Boolean)((Map)this.field_4625.get(dimensionType)).get(string);
            if (blockPos.isWithinDistance(blockBox2.getCenter(), 500.0D)) {
               if (boolean_) {
                  WorldRenderer.drawBox(matrices, vertexConsumer, (double)blockBox2.minX - cameraX, (double)blockBox2.minY - cameraY, (double)blockBox2.minZ - cameraZ, (double)(blockBox2.maxX + 1) - cameraX, (double)(blockBox2.maxY + 1) - cameraY, (double)(blockBox2.maxZ + 1) - cameraZ, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
               } else {
                  WorldRenderer.drawBox(matrices, vertexConsumer, (double)blockBox2.minX - cameraX, (double)blockBox2.minY - cameraY, (double)blockBox2.minZ - cameraZ, (double)(blockBox2.maxX + 1) - cameraX, (double)(blockBox2.maxY + 1) - cameraY, (double)(blockBox2.maxZ + 1) - cameraZ, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
               }
            }
         }
      }

   }

   public void method_3871(BlockBox blockBox, List<BlockBox> list, List<Boolean> list2, DimensionType dimensionType) {
      if (!this.field_4626.containsKey(dimensionType)) {
         this.field_4626.put(dimensionType, Maps.newHashMap());
      }

      if (!this.field_4627.containsKey(dimensionType)) {
         this.field_4627.put(dimensionType, Maps.newHashMap());
         this.field_4625.put(dimensionType, Maps.newHashMap());
      }

      ((Map)this.field_4626.get(dimensionType)).put(blockBox.toString(), blockBox);

      for(int i = 0; i < list.size(); ++i) {
         BlockBox blockBox2 = (BlockBox)list.get(i);
         Boolean boolean_ = (Boolean)list2.get(i);
         ((Map)this.field_4627.get(dimensionType)).put(blockBox2.toString(), blockBox2);
         ((Map)this.field_4625.get(dimensionType)).put(blockBox2.toString(), boolean_);
      }

   }

   public void clear() {
      this.field_4626.clear();
      this.field_4627.clear();
      this.field_4625.clear();
   }
}
