package net.minecraft.client.render.block.entity;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Environment(EnvType.CLIENT)
public class StructureBlockBlockEntityRenderer extends BlockEntityRenderer<StructureBlockBlockEntity> {
   public StructureBlockBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
   }

   public void render(StructureBlockBlockEntity structureBlockBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      if (MinecraftClient.getInstance().player.isCreativeLevelTwoOp() || MinecraftClient.getInstance().player.isSpectator()) {
         BlockPos blockPos = structureBlockBlockEntity.getOffset();
         BlockPos blockPos2 = structureBlockBlockEntity.getSize();
         if (blockPos2.getX() >= 1 && blockPos2.getY() >= 1 && blockPos2.getZ() >= 1) {
            if (structureBlockBlockEntity.getMode() == StructureBlockMode.SAVE || structureBlockBlockEntity.getMode() == StructureBlockMode.LOAD) {
               double d = (double)blockPos.getX();
               double e = (double)blockPos.getZ();
               double g = (double)blockPos.getY();
               double h = g + (double)blockPos2.getY();
               double o;
               double p;
               switch(structureBlockBlockEntity.getMirror()) {
               case LEFT_RIGHT:
                  o = (double)blockPos2.getX();
                  p = (double)(-blockPos2.getZ());
                  break;
               case FRONT_BACK:
                  o = (double)(-blockPos2.getX());
                  p = (double)blockPos2.getZ();
                  break;
               default:
                  o = (double)blockPos2.getX();
                  p = (double)blockPos2.getZ();
               }

               double ac;
               double ad;
               double ae;
               double af;
               switch(structureBlockBlockEntity.getRotation()) {
               case CLOCKWISE_90:
                  ac = p < 0.0D ? d : d + 1.0D;
                  ad = o < 0.0D ? e + 1.0D : e;
                  ae = ac - p;
                  af = ad + o;
                  break;
               case CLOCKWISE_180:
                  ac = o < 0.0D ? d : d + 1.0D;
                  ad = p < 0.0D ? e : e + 1.0D;
                  ae = ac - o;
                  af = ad - p;
                  break;
               case COUNTERCLOCKWISE_90:
                  ac = p < 0.0D ? d + 1.0D : d;
                  ad = o < 0.0D ? e : e + 1.0D;
                  ae = ac + p;
                  af = ad - o;
                  break;
               default:
                  ac = o < 0.0D ? d + 1.0D : d;
                  ad = p < 0.0D ? e + 1.0D : e;
                  ae = ac + o;
                  af = ad + p;
               }

               float ag = 1.0F;
               float ah = 0.9F;
               float ai = 0.5F;
               VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
               if (structureBlockBlockEntity.getMode() == StructureBlockMode.SAVE || structureBlockBlockEntity.shouldShowBoundingBox()) {
                  WorldRenderer.drawBox(matrixStack, vertexConsumer, ac, g, ad, ae, h, af, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
               }

               if (structureBlockBlockEntity.getMode() == StructureBlockMode.SAVE && structureBlockBlockEntity.shouldShowAir()) {
                  this.method_3585(structureBlockBlockEntity, vertexConsumer, blockPos, true, matrixStack);
                  this.method_3585(structureBlockBlockEntity, vertexConsumer, blockPos, false, matrixStack);
               }

            }
         }
      }
   }

   private void method_3585(StructureBlockBlockEntity structureBlockBlockEntity, VertexConsumer vertexConsumer, BlockPos blockPos, boolean bl, MatrixStack matrixStack) {
      BlockView blockView = structureBlockBlockEntity.getWorld();
      BlockPos blockPos2 = structureBlockBlockEntity.getPos();
      BlockPos blockPos3 = blockPos2.add(blockPos);
      Iterator var9 = BlockPos.iterate(blockPos3, blockPos3.add(structureBlockBlockEntity.getSize()).add(-1, -1, -1)).iterator();

      while(true) {
         BlockPos blockPos4;
         boolean bl2;
         boolean bl3;
         do {
            if (!var9.hasNext()) {
               return;
            }

            blockPos4 = (BlockPos)var9.next();
            BlockState blockState = blockView.getBlockState(blockPos4);
            bl2 = blockState.isAir();
            bl3 = blockState.isOf(Blocks.STRUCTURE_VOID);
         } while(!bl2 && !bl3);

         float f = bl2 ? 0.05F : 0.0F;
         double d = (double)((float)(blockPos4.getX() - blockPos2.getX()) + 0.45F - f);
         double e = (double)((float)(blockPos4.getY() - blockPos2.getY()) + 0.45F - f);
         double g = (double)((float)(blockPos4.getZ() - blockPos2.getZ()) + 0.45F - f);
         double h = (double)((float)(blockPos4.getX() - blockPos2.getX()) + 0.55F + f);
         double i = (double)((float)(blockPos4.getY() - blockPos2.getY()) + 0.55F + f);
         double j = (double)((float)(blockPos4.getZ() - blockPos2.getZ()) + 0.55F + f);
         if (bl) {
            WorldRenderer.drawBox(matrixStack, vertexConsumer, d, e, g, h, i, j, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F);
         } else if (bl2) {
            WorldRenderer.drawBox(matrixStack, vertexConsumer, d, e, g, h, i, j, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
         } else {
            WorldRenderer.drawBox(matrixStack, vertexConsumer, d, e, g, h, i, j, 1.0F, 0.25F, 0.25F, 1.0F, 1.0F, 0.25F, 0.25F);
         }
      }
   }

   public boolean rendersOutsideBoundingBox(StructureBlockBlockEntity structureBlockBlockEntity) {
      return true;
   }
}
