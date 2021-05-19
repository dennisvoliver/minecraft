package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.PandaHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.PandaEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PandaEntityRenderer extends MobEntityRenderer<PandaEntity, PandaEntityModel<PandaEntity>> {
   private static final Map<PandaEntity.Gene, Identifier> TEXTURES = (Map)Util.make(Maps.newEnumMap(PandaEntity.Gene.class), (enumMap) -> {
      enumMap.put(PandaEntity.Gene.NORMAL, new Identifier("textures/entity/panda/panda.png"));
      enumMap.put(PandaEntity.Gene.LAZY, new Identifier("textures/entity/panda/lazy_panda.png"));
      enumMap.put(PandaEntity.Gene.WORRIED, new Identifier("textures/entity/panda/worried_panda.png"));
      enumMap.put(PandaEntity.Gene.PLAYFUL, new Identifier("textures/entity/panda/playful_panda.png"));
      enumMap.put(PandaEntity.Gene.BROWN, new Identifier("textures/entity/panda/brown_panda.png"));
      enumMap.put(PandaEntity.Gene.WEAK, new Identifier("textures/entity/panda/weak_panda.png"));
      enumMap.put(PandaEntity.Gene.AGGRESSIVE, new Identifier("textures/entity/panda/aggressive_panda.png"));
   });

   public PandaEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, new PandaEntityModel(9, 0.0F), 0.9F);
      this.addFeature(new PandaHeldItemFeatureRenderer(this));
   }

   public Identifier getTexture(PandaEntity pandaEntity) {
      return (Identifier)TEXTURES.getOrDefault(pandaEntity.getProductGene(), TEXTURES.get(PandaEntity.Gene.NORMAL));
   }

   protected void setupTransforms(PandaEntity pandaEntity, MatrixStack matrixStack, float f, float g, float h) {
      super.setupTransforms(pandaEntity, matrixStack, f, g, h);
      float ae;
      if (pandaEntity.playingTicks > 0) {
         int i = pandaEntity.playingTicks;
         int j = i + 1;
         ae = 7.0F;
         float l = pandaEntity.isBaby() ? 0.3F : 0.8F;
         float aa;
         float x;
         float y;
         if (i < 8) {
            x = (float)(90 * i) / 7.0F;
            y = (float)(90 * j) / 7.0F;
            aa = this.method_4086(x, y, j, h, 8.0F);
            matrixStack.translate(0.0D, (double)((l + 0.2F) * (aa / 90.0F)), 0.0D);
            matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-aa));
         } else {
            float z;
            if (i < 16) {
               x = ((float)i - 8.0F) / 7.0F;
               y = 90.0F + 90.0F * x;
               z = 90.0F + 90.0F * ((float)j - 8.0F) / 7.0F;
               aa = this.method_4086(y, z, j, h, 16.0F);
               matrixStack.translate(0.0D, (double)(l + 0.2F + (l - 0.2F) * (aa - 90.0F) / 90.0F), 0.0D);
               matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-aa));
            } else if ((float)i < 24.0F) {
               x = ((float)i - 16.0F) / 7.0F;
               y = 180.0F + 90.0F * x;
               z = 180.0F + 90.0F * ((float)j - 16.0F) / 7.0F;
               aa = this.method_4086(y, z, j, h, 24.0F);
               matrixStack.translate(0.0D, (double)(l + l * (270.0F - aa) / 90.0F), 0.0D);
               matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-aa));
            } else if (i < 32) {
               x = ((float)i - 24.0F) / 7.0F;
               y = 270.0F + 90.0F * x;
               z = 270.0F + 90.0F * ((float)j - 24.0F) / 7.0F;
               aa = this.method_4086(y, z, j, h, 32.0F);
               matrixStack.translate(0.0D, (double)(l * ((360.0F - aa) / 90.0F)), 0.0D);
               matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-aa));
            }
         }
      }

      float ab = pandaEntity.getScaredAnimationProgress(h);
      float ad;
      if (ab > 0.0F) {
         matrixStack.translate(0.0D, (double)(0.8F * ab), 0.0D);
         matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(MathHelper.lerp(ab, pandaEntity.pitch, pandaEntity.pitch + 90.0F)));
         matrixStack.translate(0.0D, (double)(-1.0F * ab), 0.0D);
         if (pandaEntity.isScaredByThunderstorm()) {
            ad = (float)(Math.cos((double)pandaEntity.age * 1.25D) * 3.141592653589793D * 0.05000000074505806D);
            matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(ad));
            if (pandaEntity.isBaby()) {
               matrixStack.translate(0.0D, 0.800000011920929D, 0.550000011920929D);
            }
         }
      }

      ad = pandaEntity.getLieOnBackAnimationProgress(h);
      if (ad > 0.0F) {
         ae = pandaEntity.isBaby() ? 0.5F : 1.3F;
         matrixStack.translate(0.0D, (double)(ae * ad), 0.0D);
         matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(MathHelper.lerp(ad, pandaEntity.pitch, pandaEntity.pitch + 180.0F)));
      }

   }

   private float method_4086(float f, float g, int i, float h, float j) {
      return (float)i < j ? MathHelper.lerp(h, f, g) : f;
   }
}
