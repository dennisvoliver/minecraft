package net.minecraft.client.render.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

@Environment(EnvType.CLIENT)
public class BlockOutlineDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;

   public BlockOutlineDebugRenderer(MinecraftClient minecraftClient) {
      this.client = minecraftClient;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      BlockView blockView = this.client.player.world;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.lineWidth(2.0F);
      RenderSystem.disableTexture();
      RenderSystem.depthMask(false);
      BlockPos blockPos = new BlockPos(cameraX, cameraY, cameraZ);
      Iterator var11 = BlockPos.iterate(blockPos.add(-6, -6, -6), blockPos.add(6, 6, 6)).iterator();

      while(true) {
         BlockPos blockPos2;
         BlockState blockState;
         do {
            if (!var11.hasNext()) {
               RenderSystem.depthMask(true);
               RenderSystem.enableTexture();
               RenderSystem.disableBlend();
               return;
            }

            blockPos2 = (BlockPos)var11.next();
            blockState = blockView.getBlockState(blockPos2);
         } while(blockState.isOf(Blocks.AIR));

         VoxelShape voxelShape = blockState.getOutlineShape(blockView, blockPos2);
         Iterator var15 = voxelShape.getBoundingBoxes().iterator();

         while(var15.hasNext()) {
            Box box = (Box)var15.next();
            Box box2 = box.offset(blockPos2).expand(0.002D).offset(-cameraX, -cameraY, -cameraZ);
            double d = box2.minX;
            double e = box2.minY;
            double f = box2.minZ;
            double g = box2.maxX;
            double h = box2.maxY;
            double i = box2.maxZ;
            float j = 1.0F;
            float k = 0.0F;
            float l = 0.0F;
            float m = 0.5F;
            Tessellator tessellator6;
            BufferBuilder bufferBuilder6;
            if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.WEST)) {
               tessellator6 = Tessellator.getInstance();
               bufferBuilder6 = tessellator6.getBuffer();
               bufferBuilder6.begin(5, VertexFormats.POSITION_COLOR);
               bufferBuilder6.vertex(d, e, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(d, e, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(d, h, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(d, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               tessellator6.draw();
            }

            if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.SOUTH)) {
               tessellator6 = Tessellator.getInstance();
               bufferBuilder6 = tessellator6.getBuffer();
               bufferBuilder6.begin(5, VertexFormats.POSITION_COLOR);
               bufferBuilder6.vertex(d, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(d, e, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, e, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               tessellator6.draw();
            }

            if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.EAST)) {
               tessellator6 = Tessellator.getInstance();
               bufferBuilder6 = tessellator6.getBuffer();
               bufferBuilder6.begin(5, VertexFormats.POSITION_COLOR);
               bufferBuilder6.vertex(g, e, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, e, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, h, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               tessellator6.draw();
            }

            if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.NORTH)) {
               tessellator6 = Tessellator.getInstance();
               bufferBuilder6 = tessellator6.getBuffer();
               bufferBuilder6.begin(5, VertexFormats.POSITION_COLOR);
               bufferBuilder6.vertex(g, h, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, e, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(d, h, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(d, e, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               tessellator6.draw();
            }

            if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.DOWN)) {
               tessellator6 = Tessellator.getInstance();
               bufferBuilder6 = tessellator6.getBuffer();
               bufferBuilder6.begin(5, VertexFormats.POSITION_COLOR);
               bufferBuilder6.vertex(d, e, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, e, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(d, e, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, e, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               tessellator6.draw();
            }

            if (blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.UP)) {
               tessellator6 = Tessellator.getInstance();
               bufferBuilder6 = tessellator6.getBuffer();
               bufferBuilder6.begin(5, VertexFormats.POSITION_COLOR);
               bufferBuilder6.vertex(d, h, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(d, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, h, f).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               bufferBuilder6.vertex(g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).next();
               tessellator6.draw();
            }
         }
      }
   }
}
