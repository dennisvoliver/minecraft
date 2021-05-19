package net.minecraft.client.render.model;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BuiltinBakedModel implements BakedModel {
   private final ModelTransformation transformation;
   private final ModelOverrideList itemPropertyOverrides;
   private final Sprite sprite;
   private final boolean sideLit;

   public BuiltinBakedModel(ModelTransformation transformation, ModelOverrideList itemPropertyOverrides, Sprite sprite, boolean sideLit) {
      this.transformation = transformation;
      this.itemPropertyOverrides = itemPropertyOverrides;
      this.sprite = sprite;
      this.sideLit = sideLit;
   }

   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
      return Collections.emptyList();
   }

   public boolean useAmbientOcclusion() {
      return false;
   }

   public boolean hasDepth() {
      return true;
   }

   public boolean isSideLit() {
      return this.sideLit;
   }

   public boolean isBuiltin() {
      return true;
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
}
