package net.minecraft.client.render.debug;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.shape.VoxelShape;

@Environment(EnvType.CLIENT)
public class CollisionDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;
   private double lastUpdateTime = Double.MIN_VALUE;
   private List<VoxelShape> collisions = Collections.emptyList();

   public CollisionDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      double d = (double)Util.getMeasuringTimeNano();
      if (d - this.lastUpdateTime > 1.0E8D) {
         this.lastUpdateTime = d;
         Entity entity = this.client.gameRenderer.getCamera().getFocusedEntity();
         this.collisions = (List)entity.world.getCollisions(entity, entity.getBoundingBox().expand(6.0D), (entityx) -> {
            return true;
         }).collect(Collectors.toList());
      }

      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
      Iterator var12 = this.collisions.iterator();

      while(var12.hasNext()) {
         VoxelShape voxelShape = (VoxelShape)var12.next();
         WorldRenderer.method_22983(matrices, vertexConsumer, voxelShape, -cameraX, -cameraY, -cameraZ, 1.0F, 1.0F, 1.0F, 1.0F);
      }

   }
}
