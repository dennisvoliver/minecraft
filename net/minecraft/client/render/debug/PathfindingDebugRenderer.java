package net.minecraft.client.render.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PathfindingDebugRenderer implements DebugRenderer.Renderer {
   private final Map<Integer, Path> paths = Maps.newHashMap();
   private final Map<Integer, Float> field_4617 = Maps.newHashMap();
   private final Map<Integer, Long> pathTimes = Maps.newHashMap();

   public void addPath(int id, Path path, float f) {
      this.paths.put(id, path);
      this.pathTimes.put(id, Util.getMeasuringTimeMs());
      this.field_4617.put(id, f);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      if (!this.paths.isEmpty()) {
         long l = Util.getMeasuringTimeMs();
         Iterator var11 = this.paths.keySet().iterator();

         while(var11.hasNext()) {
            Integer integer = (Integer)var11.next();
            Path path = (Path)this.paths.get(integer);
            float f = (Float)this.field_4617.get(integer);
            drawPath(path, f, true, true, cameraX, cameraY, cameraZ);
         }

         Integer[] var15 = (Integer[])this.pathTimes.keySet().toArray(new Integer[0]);
         int var16 = var15.length;

         for(int var17 = 0; var17 < var16; ++var17) {
            Integer integer2 = var15[var17];
            if (l - (Long)this.pathTimes.get(integer2) > 5000L) {
               this.paths.remove(integer2);
               this.pathTimes.remove(integer2);
            }
         }

      }
   }

   public static void drawPath(Path path, float nodeSize, boolean bl, boolean drawLabels, double cameraX, double cameraY, double cameraZ) {
      RenderSystem.pushMatrix();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.color4f(0.0F, 1.0F, 0.0F, 0.75F);
      RenderSystem.disableTexture();
      RenderSystem.lineWidth(6.0F);
      drawPathInternal(path, nodeSize, bl, drawLabels, cameraX, cameraY, cameraZ);
      RenderSystem.enableTexture();
      RenderSystem.disableBlend();
      RenderSystem.popMatrix();
   }

   private static void drawPathInternal(Path path, float nodeSize, boolean bl, boolean drawLabels, double cameraX, double cameraY, double cameraZ) {
      drawPathLines(path, cameraX, cameraY, cameraZ);
      BlockPos blockPos = path.getTarget();
      int j;
      PathNode pathNode4;
      if (getManhattanDistance(blockPos, cameraX, cameraY, cameraZ) <= 80.0F) {
         DebugRenderer.drawBox((new Box((double)((float)blockPos.getX() + 0.25F), (double)((float)blockPos.getY() + 0.25F), (double)blockPos.getZ() + 0.25D, (double)((float)blockPos.getX() + 0.75F), (double)((float)blockPos.getY() + 0.75F), (double)((float)blockPos.getZ() + 0.75F))).offset(-cameraX, -cameraY, -cameraZ), 0.0F, 1.0F, 0.0F, 0.5F);

         for(j = 0; j < path.getLength(); ++j) {
            pathNode4 = path.getNode(j);
            if (getManhattanDistance(pathNode4.getPos(), cameraX, cameraY, cameraZ) <= 80.0F) {
               float f = j == path.getCurrentNodeIndex() ? 1.0F : 0.0F;
               float g = j == path.getCurrentNodeIndex() ? 0.0F : 1.0F;
               DebugRenderer.drawBox((new Box((double)((float)pathNode4.x + 0.5F - nodeSize), (double)((float)pathNode4.y + 0.01F * (float)j), (double)((float)pathNode4.z + 0.5F - nodeSize), (double)((float)pathNode4.x + 0.5F + nodeSize), (double)((float)pathNode4.y + 0.25F + 0.01F * (float)j), (double)((float)pathNode4.z + 0.5F + nodeSize))).offset(-cameraX, -cameraY, -cameraZ), f, 0.0F, g, 0.5F);
            }
         }
      }

      if (bl) {
         PathNode[] var15 = path.method_22881();
         int var16 = var15.length;

         int var17;
         PathNode pathNode3;
         for(var17 = 0; var17 < var16; ++var17) {
            pathNode3 = var15[var17];
            if (getManhattanDistance(pathNode3.getPos(), cameraX, cameraY, cameraZ) <= 80.0F) {
               DebugRenderer.drawBox((new Box((double)((float)pathNode3.x + 0.5F - nodeSize / 2.0F), (double)((float)pathNode3.y + 0.01F), (double)((float)pathNode3.z + 0.5F - nodeSize / 2.0F), (double)((float)pathNode3.x + 0.5F + nodeSize / 2.0F), (double)pathNode3.y + 0.1D, (double)((float)pathNode3.z + 0.5F + nodeSize / 2.0F))).offset(-cameraX, -cameraY, -cameraZ), 1.0F, 0.8F, 0.8F, 0.5F);
            }
         }

         var15 = path.method_22880();
         var16 = var15.length;

         for(var17 = 0; var17 < var16; ++var17) {
            pathNode3 = var15[var17];
            if (getManhattanDistance(pathNode3.getPos(), cameraX, cameraY, cameraZ) <= 80.0F) {
               DebugRenderer.drawBox((new Box((double)((float)pathNode3.x + 0.5F - nodeSize / 2.0F), (double)((float)pathNode3.y + 0.01F), (double)((float)pathNode3.z + 0.5F - nodeSize / 2.0F), (double)((float)pathNode3.x + 0.5F + nodeSize / 2.0F), (double)pathNode3.y + 0.1D, (double)((float)pathNode3.z + 0.5F + nodeSize / 2.0F))).offset(-cameraX, -cameraY, -cameraZ), 0.8F, 1.0F, 1.0F, 0.5F);
            }
         }
      }

      if (drawLabels) {
         for(j = 0; j < path.getLength(); ++j) {
            pathNode4 = path.getNode(j);
            if (getManhattanDistance(pathNode4.getPos(), cameraX, cameraY, cameraZ) <= 80.0F) {
               DebugRenderer.drawString(String.format("%s", pathNode4.type), (double)pathNode4.x + 0.5D, (double)pathNode4.y + 0.75D, (double)pathNode4.z + 0.5D, -1, 0.02F, true, 0.0F, true);
               DebugRenderer.drawString(String.format(Locale.ROOT, "%.2f", pathNode4.penalty), (double)pathNode4.x + 0.5D, (double)pathNode4.y + 0.25D, (double)pathNode4.z + 0.5D, -1, 0.02F, true, 0.0F, true);
            }
         }
      }

   }

   public static void drawPathLines(Path path, double cameraX, double cameraY, double cameraZ) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      bufferBuilder.begin(3, VertexFormats.POSITION_COLOR);

      for(int i = 0; i < path.getLength(); ++i) {
         PathNode pathNode = path.getNode(i);
         if (!(getManhattanDistance(pathNode.getPos(), cameraX, cameraY, cameraZ) > 80.0F)) {
            float f = (float)i / (float)path.getLength() * 0.33F;
            int j = i == 0 ? 0 : MathHelper.hsvToRgb(f, 0.9F, 0.9F);
            int k = j >> 16 & 255;
            int l = j >> 8 & 255;
            int m = j & 255;
            bufferBuilder.vertex((double)pathNode.x - cameraX + 0.5D, (double)pathNode.y - cameraY + 0.5D, (double)pathNode.z - cameraZ + 0.5D).color(k, l, m, 255).next();
         }
      }

      tessellator.draw();
   }

   private static float getManhattanDistance(BlockPos pos, double x, double y, double z) {
      return (float)(Math.abs((double)pos.getX() - x) + Math.abs((double)pos.getY() - y) + Math.abs((double)pos.getZ() - z));
   }
}
