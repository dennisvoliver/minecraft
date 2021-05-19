package net.minecraft.client.render.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.WorldView;

@Environment(EnvType.CLIENT)
public class WaterDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;

   public WaterDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      BlockPos blockPos = this.client.player.getBlockPos();
      WorldView worldView = this.client.player.world;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.color4f(0.0F, 1.0F, 0.0F, 0.75F);
      RenderSystem.disableTexture();
      RenderSystem.lineWidth(6.0F);
      Iterator var11 = BlockPos.iterate(blockPos.add(-10, -10, -10), blockPos.add(10, 10, 10)).iterator();

      BlockPos blockPos3;
      FluidState fluidState2;
      while(var11.hasNext()) {
         blockPos3 = (BlockPos)var11.next();
         fluidState2 = worldView.getFluidState(blockPos3);
         if (fluidState2.isIn(FluidTags.WATER)) {
            double d = (double)((float)blockPos3.getY() + fluidState2.getHeight(worldView, blockPos3));
            DebugRenderer.drawBox((new Box((double)((float)blockPos3.getX() + 0.01F), (double)((float)blockPos3.getY() + 0.01F), (double)((float)blockPos3.getZ() + 0.01F), (double)((float)blockPos3.getX() + 0.99F), d, (double)((float)blockPos3.getZ() + 0.99F))).offset(-cameraX, -cameraY, -cameraZ), 1.0F, 1.0F, 1.0F, 0.2F);
         }
      }

      var11 = BlockPos.iterate(blockPos.add(-10, -10, -10), blockPos.add(10, 10, 10)).iterator();

      while(var11.hasNext()) {
         blockPos3 = (BlockPos)var11.next();
         fluidState2 = worldView.getFluidState(blockPos3);
         if (fluidState2.isIn(FluidTags.WATER)) {
            DebugRenderer.drawString(String.valueOf(fluidState2.getLevel()), (double)blockPos3.getX() + 0.5D, (double)((float)blockPos3.getY() + fluidState2.getHeight(worldView, blockPos3)), (double)blockPos3.getZ() + 0.5D, -16777216);
         }
      }

      RenderSystem.enableTexture();
      RenderSystem.disableBlend();
   }
}
