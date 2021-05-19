package net.minecraft.client.render.debug;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class RaidCenterDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;
   private Collection<BlockPos> raidCenters = Lists.newArrayList();

   public RaidCenterDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void setRaidCenters(Collection<BlockPos> centers) {
      this.raidCenters = centers;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      BlockPos blockPos = this.method_23125().getBlockPos();
      Iterator var10 = this.raidCenters.iterator();

      while(var10.hasNext()) {
         BlockPos blockPos2 = (BlockPos)var10.next();
         if (blockPos.isWithinDistance(blockPos2, 160.0D)) {
            method_23122(blockPos2);
         }
      }

   }

   private static void method_23122(BlockPos blockPos) {
      DebugRenderer.drawBox(blockPos.add(-0.5D, -0.5D, -0.5D), blockPos.add(1.5D, 1.5D, 1.5D), 1.0F, 0.0F, 0.0F, 0.15F);
      int i = -65536;
      method_23123("Raid center", blockPos, -65536);
   }

   private static void method_23123(String string, BlockPos blockPos, int i) {
      double d = (double)blockPos.getX() + 0.5D;
      double e = (double)blockPos.getY() + 1.3D;
      double f = (double)blockPos.getZ() + 0.5D;
      DebugRenderer.drawString(string, d, e, f, i, 0.04F, true, 0.0F, true);
   }

   private Camera method_23125() {
      return this.client.gameRenderer.getCamera();
   }
}
