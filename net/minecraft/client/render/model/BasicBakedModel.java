package net.minecraft.client.render.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BasicBakedModel implements BakedModel {
   protected final List<BakedQuad> quads;
   protected final Map<Direction, List<BakedQuad>> faceQuads;
   protected final boolean usesAo;
   protected final boolean hasDepth;
   protected final boolean isSideLit;
   protected final Sprite sprite;
   protected final ModelTransformation transformation;
   protected final ModelOverrideList itemPropertyOverrides;

   public BasicBakedModel(List<BakedQuad> quads, Map<Direction, List<BakedQuad>> faceQuads, boolean usesAo, boolean isSideLit, boolean hasDepth, Sprite sprite, ModelTransformation modelTransformation, ModelOverrideList modelOverrideList) {
      this.quads = quads;
      this.faceQuads = faceQuads;
      this.usesAo = usesAo;
      this.hasDepth = hasDepth;
      this.isSideLit = isSideLit;
      this.sprite = sprite;
      this.transformation = modelTransformation;
      this.itemPropertyOverrides = modelOverrideList;
   }

   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
      return face == null ? this.quads : (List)this.faceQuads.get(face);
   }

   public boolean useAmbientOcclusion() {
      return this.usesAo;
   }

   public boolean hasDepth() {
      return this.hasDepth;
   }

   public boolean isSideLit() {
      return this.isSideLit;
   }

   public boolean isBuiltin() {
      return false;
   }

   public Sprite getSprite() {
      return this.sprite;
   }

   public ModelTransformation getTransformation() {
      return this.transformation;
   }

   public ModelOverrideList getOverrides() {
      return this.itemPropertyOverrides;
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      private final List<BakedQuad> quads;
      private final Map<Direction, List<BakedQuad>> faceQuads;
      private final ModelOverrideList itemPropertyOverrides;
      private final boolean usesAo;
      private Sprite particleTexture;
      private final boolean isSideLit;
      private final boolean hasDepth;
      private final ModelTransformation transformation;

      public Builder(JsonUnbakedModel unbakedModel, ModelOverrideList itemPropertyOverrides, boolean hasDepth) {
         this(unbakedModel.useAmbientOcclusion(), unbakedModel.getGuiLight().isSide(), hasDepth, unbakedModel.getTransformations(), itemPropertyOverrides);
      }

      private Builder(boolean usesAo, boolean isSideLit, boolean hasDepth, ModelTransformation modelTransformation, ModelOverrideList modelOverrideList) {
         this.quads = Lists.newArrayList();
         this.faceQuads = Maps.newEnumMap(Direction.class);
         Direction[] var6 = Direction.values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Direction direction = var6[var8];
            this.faceQuads.put(direction, Lists.newArrayList());
         }

         this.itemPropertyOverrides = modelOverrideList;
         this.usesAo = usesAo;
         this.isSideLit = isSideLit;
         this.hasDepth = hasDepth;
         this.transformation = modelTransformation;
      }

      public BasicBakedModel.Builder addQuad(Direction side, BakedQuad quad) {
         ((List)this.faceQuads.get(side)).add(quad);
         return this;
      }

      public BasicBakedModel.Builder addQuad(BakedQuad quad) {
         this.quads.add(quad);
         return this;
      }

      public BasicBakedModel.Builder setParticle(Sprite sprite) {
         this.particleTexture = sprite;
         return this;
      }

      public BakedModel build() {
         if (this.particleTexture == null) {
            throw new RuntimeException("Missing particle!");
         } else {
            return new BasicBakedModel(this.quads, this.faceQuads, this.usesAo, this.isSideLit, this.hasDepth, this.particleTexture, this.transformation, this.itemPropertyOverrides);
         }
      }
   }
}
