package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class RenderLayer extends RenderPhase {
   private static final RenderLayer SOLID;
   private static final RenderLayer CUTOUT_MIPPED;
   private static final RenderLayer CUTOUT;
   private static final RenderLayer TRANSLUCENT;
   private static final RenderLayer TRANSLUCENT_MOVING_BLOCK;
   private static final RenderLayer TRANSLUCENT_NO_CRUMBLING;
   private static final RenderLayer LEASH;
   private static final RenderLayer WATER_MASK;
   private static final RenderLayer ARMOR_GLINT;
   private static final RenderLayer ARMOR_ENTITY_GLINT;
   private static final RenderLayer GLINT_TRANSLUCENT;
   private static final RenderLayer GLINT;
   private static final RenderLayer DIRECT_GLINT;
   private static final RenderLayer ENTITY_GLINT;
   private static final RenderLayer DIRECT_ENTITY_GLINT;
   private static final RenderLayer LIGHTNING;
   private static final RenderLayer TRIPWIRE;
   public static final RenderLayer.MultiPhase LINES;
   private final VertexFormat vertexFormat;
   private final int drawMode;
   private final int expectedBufferSize;
   private final boolean hasCrumbling;
   private final boolean translucent;
   private final Optional<RenderLayer> optionalThis;

   public static RenderLayer getSolid() {
      return SOLID;
   }

   public static RenderLayer getCutoutMipped() {
      return CUTOUT_MIPPED;
   }

   public static RenderLayer getCutout() {
      return CUTOUT;
   }

   private static RenderLayer.MultiPhaseParameters createTranslucentPhaseData() {
      return RenderLayer.MultiPhaseParameters.builder().shadeModel(SMOOTH_SHADE_MODEL).lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY).target(TRANSLUCENT_TARGET).build(true);
   }

   public static RenderLayer getTranslucent() {
      return TRANSLUCENT;
   }

   private static RenderLayer.MultiPhaseParameters getItemPhaseData() {
      return RenderLayer.MultiPhaseParameters.builder().shadeModel(SMOOTH_SHADE_MODEL).lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).build(true);
   }

   public static RenderLayer getTranslucentMovingBlock() {
      return TRANSLUCENT_MOVING_BLOCK;
   }

   public static RenderLayer getTranslucentNoCrumbling() {
      return TRANSLUCENT_NO_CRUMBLING;
   }

   public static RenderLayer getArmorCutoutNoCull(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING).build(true);
      return of("armor_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, multiPhaseParameters);
   }

   public static RenderLayer getEntitySolid(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
      return of("entity_solid", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, multiPhaseParameters);
   }

   public static RenderLayer getEntityCutout(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
      return of("entity_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, multiPhaseParameters);
   }

   public static RenderLayer getEntityCutoutNoCull(Identifier texture, boolean affectsOutline) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(affectsOutline);
      return of("entity_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, multiPhaseParameters);
   }

   public static RenderLayer getEntityCutoutNoCull(Identifier texture) {
      return getEntityCutoutNoCull(texture, true);
   }

   public static RenderLayer getEntityCutoutNoCullZOffset(Identifier texture, boolean affectsOutline) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING).build(affectsOutline);
      return of("entity_cutout_no_cull_z_offset", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, multiPhaseParameters);
   }

   public static RenderLayer getEntityCutoutNoCullZOffset(Identifier texture) {
      return getEntityCutoutNoCullZOffset(texture, true);
   }

   public static RenderLayer getItemEntityTranslucentCull(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).writeMaskState(RenderPhase.ALL_MASK).build(true);
      return of("item_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, multiPhaseParameters);
   }

   public static RenderLayer getEntityTranslucentCull(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
      return of("entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, multiPhaseParameters);
   }

   public static RenderLayer getEntityTranslucent(Identifier texture, boolean affectsOutline) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(affectsOutline);
      return of("entity_translucent", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, multiPhaseParameters);
   }

   public static RenderLayer getEntityTranslucent(Identifier texture) {
      return getEntityTranslucent(texture, true);
   }

   public static RenderLayer getEntitySmoothCutout(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).alpha(HALF_ALPHA).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).shadeModel(SMOOTH_SHADE_MODEL).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).build(true);
      return of("entity_smooth_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, multiPhaseParameters);
   }

   public static RenderLayer getBeaconBeam(Identifier texture, boolean translucent) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(translucent ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY).writeMaskState(translucent ? COLOR_MASK : ALL_MASK).fog(NO_FOG).build(false);
      return of("beacon_beam", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 256, false, true, multiPhaseParameters);
   }

   public static RenderLayer getEntityDecal(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).depthTest(EQUAL_DEPTH_TEST).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(false);
      return of("entity_decal", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, multiPhaseParameters);
   }

   public static RenderLayer getEntityNoOutline(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).writeMaskState(COLOR_MASK).build(false);
      return of("entity_no_outline", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, false, true, multiPhaseParameters);
   }

   public static RenderLayer getEntityShadow(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).cull(ENABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).writeMaskState(COLOR_MASK).depthTest(LEQUAL_DEPTH_TEST).layering(VIEW_OFFSET_Z_LAYERING).build(false);
      return of("entity_shadow", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, false, false, multiPhaseParameters);
   }

   public static RenderLayer getEntityAlpha(Identifier texture, float alpha) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).alpha(new RenderPhase.Alpha(alpha)).cull(DISABLE_CULLING).build(true);
      return of("entity_alpha", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, multiPhaseParameters);
   }

   public static RenderLayer getEyes(Identifier texture) {
      RenderPhase.Texture texture2 = new RenderPhase.Texture(texture, false, false);
      return of("eyes", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, false, true, RenderLayer.MultiPhaseParameters.builder().texture(texture2).transparency(ADDITIVE_TRANSPARENCY).writeMaskState(COLOR_MASK).fog(BLACK_FOG).build(false));
   }

   public static RenderLayer getEnergySwirl(Identifier texture, float x, float y) {
      return of("energy_swirl", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, false, true, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).texturing(new RenderPhase.OffsetTexturing(x, y)).fog(BLACK_FOG).transparency(ADDITIVE_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(false));
   }

   public static RenderLayer getLeash() {
      return LEASH;
   }

   public static RenderLayer getWaterMask() {
      return WATER_MASK;
   }

   public static RenderLayer getOutline(Identifier texture) {
      return getOutline(texture, DISABLE_CULLING);
   }

   public static RenderLayer getOutline(Identifier texture, RenderPhase.Cull cull) {
      return of("outline", VertexFormats.POSITION_COLOR_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).cull(cull).depthTest(ALWAYS_DEPTH_TEST).alpha(ONE_TENTH_ALPHA).texturing(OUTLINE_TEXTURING).fog(NO_FOG).target(OUTLINE_TARGET).build(RenderLayer.OutlineMode.IS_OUTLINE));
   }

   public static RenderLayer getArmorGlint() {
      return ARMOR_GLINT;
   }

   public static RenderLayer getArmorEntityGlint() {
      return ARMOR_ENTITY_GLINT;
   }

   public static RenderLayer method_30676() {
      return GLINT_TRANSLUCENT;
   }

   public static RenderLayer getGlint() {
      return GLINT;
   }

   public static RenderLayer getDirectGlint() {
      return DIRECT_GLINT;
   }

   public static RenderLayer getEntityGlint() {
      return ENTITY_GLINT;
   }

   public static RenderLayer getDirectEntityGlint() {
      return DIRECT_ENTITY_GLINT;
   }

   public static RenderLayer getBlockBreaking(Identifier texture) {
      RenderPhase.Texture texture2 = new RenderPhase.Texture(texture, false, false);
      return of("crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 256, false, true, RenderLayer.MultiPhaseParameters.builder().texture(texture2).alpha(ONE_TENTH_ALPHA).transparency(CRUMBLING_TRANSPARENCY).writeMaskState(COLOR_MASK).layering(POLYGON_OFFSET_LAYERING).build(false));
   }

   public static RenderLayer getText(Identifier texture) {
      return of("text", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, 7, 256, false, true, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).alpha(ONE_TENTH_ALPHA).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).build(false));
   }

   public static RenderLayer getTextSeeThrough(Identifier texture) {
      return of("text_see_through", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, 7, 256, false, true, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).alpha(ONE_TENTH_ALPHA).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).depthTest(ALWAYS_DEPTH_TEST).writeMaskState(COLOR_MASK).build(false));
   }

   public static RenderLayer getLightning() {
      return LIGHTNING;
   }

   private static RenderLayer.MultiPhaseParameters getTripwirePhaseData() {
      return RenderLayer.MultiPhaseParameters.builder().shadeModel(SMOOTH_SHADE_MODEL).lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY).target(WEATHER_TARGET).build(true);
   }

   public static RenderLayer getTripwire() {
      return TRIPWIRE;
   }

   public static RenderLayer getEndPortal(int layer) {
      RenderPhase.Transparency transparency2;
      RenderPhase.Texture texture2;
      if (layer <= 1) {
         transparency2 = TRANSLUCENT_TRANSPARENCY;
         texture2 = new RenderPhase.Texture(EndPortalBlockEntityRenderer.SKY_TEXTURE, false, false);
      } else {
         transparency2 = ADDITIVE_TRANSPARENCY;
         texture2 = new RenderPhase.Texture(EndPortalBlockEntityRenderer.PORTAL_TEXTURE, false, false);
      }

      return of("end_portal", VertexFormats.POSITION_COLOR, 7, 256, false, true, RenderLayer.MultiPhaseParameters.builder().transparency(transparency2).texture(texture2).texturing(new RenderPhase.PortalTexturing(layer)).fog(BLACK_FOG).build(false));
   }

   public static RenderLayer getLines() {
      return LINES;
   }

   public RenderLayer(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
      super(name, startAction, endAction);
      this.vertexFormat = vertexFormat;
      this.drawMode = drawMode;
      this.expectedBufferSize = expectedBufferSize;
      this.hasCrumbling = hasCrumbling;
      this.translucent = translucent;
      this.optionalThis = Optional.of(this);
   }

   public static RenderLayer.MultiPhase of(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, RenderLayer.MultiPhaseParameters phaseData) {
      return of(name, vertexFormat, drawMode, expectedBufferSize, false, false, phaseData);
   }

   public static RenderLayer.MultiPhase of(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, RenderLayer.MultiPhaseParameters phases) {
      return RenderLayer.MultiPhase.of(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, phases);
   }

   public void draw(BufferBuilder buffer, int cameraX, int cameraY, int cameraZ) {
      if (buffer.isBuilding()) {
         if (this.translucent) {
            buffer.sortQuads((float)cameraX, (float)cameraY, (float)cameraZ);
         }

         buffer.end();
         this.startDrawing();
         BufferRenderer.draw(buffer);
         this.endDrawing();
      }
   }

   public String toString() {
      return this.name;
   }

   public static List<RenderLayer> getBlockLayers() {
      return ImmutableList.of(getSolid(), getCutoutMipped(), getCutout(), getTranslucent(), getTripwire());
   }

   public int getExpectedBufferSize() {
      return this.expectedBufferSize;
   }

   public VertexFormat getVertexFormat() {
      return this.vertexFormat;
   }

   public int getDrawMode() {
      return this.drawMode;
   }

   public Optional<RenderLayer> getAffectedOutline() {
      return Optional.empty();
   }

   public boolean isOutline() {
      return false;
   }

   public boolean hasCrumbling() {
      return this.hasCrumbling;
   }

   public Optional<RenderLayer> asOptional() {
      return this.optionalThis;
   }

   static {
      SOLID = of("solid", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 2097152, true, false, RenderLayer.MultiPhaseParameters.builder().shadeModel(SMOOTH_SHADE_MODEL).lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).build(true));
      CUTOUT_MIPPED = of("cutout_mipped", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 131072, true, false, RenderLayer.MultiPhaseParameters.builder().shadeModel(SMOOTH_SHADE_MODEL).lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).alpha(HALF_ALPHA).build(true));
      CUTOUT = of("cutout", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 131072, true, false, RenderLayer.MultiPhaseParameters.builder().shadeModel(SMOOTH_SHADE_MODEL).lightmap(ENABLE_LIGHTMAP).texture(BLOCK_ATLAS_TEXTURE).alpha(HALF_ALPHA).build(true));
      TRANSLUCENT = of("translucent", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 262144, true, true, createTranslucentPhaseData());
      TRANSLUCENT_MOVING_BLOCK = of("translucent_moving_block", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 262144, false, true, getItemPhaseData());
      TRANSLUCENT_NO_CRUMBLING = of("translucent_no_crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 262144, false, true, createTranslucentPhaseData());
      LEASH = of("leash", VertexFormats.POSITION_COLOR_LIGHT, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(NO_TEXTURE).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).build(false));
      WATER_MASK = of("water_mask", VertexFormats.POSITION, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(NO_TEXTURE).writeMaskState(DEPTH_MASK).build(false));
      ARMOR_GLINT = of("armor_glint", VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ENCHANTED_ITEM_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(GLINT_TEXTURING).layering(VIEW_OFFSET_Z_LAYERING).build(false));
      ARMOR_ENTITY_GLINT = of("armor_entity_glint", VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ENCHANTED_ITEM_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(ENTITY_GLINT_TEXTURING).layering(VIEW_OFFSET_Z_LAYERING).build(false));
      GLINT_TRANSLUCENT = of("glint_translucent", VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ENCHANTED_ITEM_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(GLINT_TEXTURING).target(ITEM_TARGET).build(false));
      GLINT = of("glint", VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ENCHANTED_ITEM_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(GLINT_TEXTURING).build(false));
      DIRECT_GLINT = of("glint_direct", VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ENCHANTED_ITEM_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(GLINT_TEXTURING).build(false));
      ENTITY_GLINT = of("entity_glint", VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ENCHANTED_ITEM_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).target(ITEM_TARGET).texturing(ENTITY_GLINT_TEXTURING).build(false));
      DIRECT_ENTITY_GLINT = of("entity_glint_direct", VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ItemRenderer.ENCHANTED_ITEM_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(ENTITY_GLINT_TEXTURING).build(false));
      LIGHTNING = of("lightning", VertexFormats.POSITION_COLOR, 7, 256, false, true, RenderLayer.MultiPhaseParameters.builder().writeMaskState(ALL_MASK).transparency(LIGHTNING_TRANSPARENCY).target(WEATHER_TARGET).shadeModel(SMOOTH_SHADE_MODEL).build(false));
      TRIPWIRE = of("tripwire", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 262144, true, true, getTripwirePhaseData());
      LINES = of("lines", VertexFormats.POSITION_COLOR, 1, 256, RenderLayer.MultiPhaseParameters.builder().lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty())).layering(VIEW_OFFSET_Z_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).writeMaskState(ALL_MASK).build(false));
   }

   @Environment(EnvType.CLIENT)
   static final class MultiPhase extends RenderLayer {
      private static final ObjectOpenCustomHashSet<RenderLayer.MultiPhase> CACHE;
      private final RenderLayer.MultiPhaseParameters phases;
      private final int hash;
      private final Optional<RenderLayer> affectedOutline;
      private final boolean outline;

      private MultiPhase(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, RenderLayer.MultiPhaseParameters phases) {
         super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, () -> {
            phases.phases.forEach(RenderPhase::startDrawing);
         }, () -> {
            phases.phases.forEach(RenderPhase::endDrawing);
         });
         this.phases = phases;
         this.affectedOutline = phases.outlineMode == RenderLayer.OutlineMode.AFFECTS_OUTLINE ? phases.texture.getId().map((identifier) -> {
            return getOutline(identifier, phases.cull);
         }) : Optional.empty();
         this.outline = phases.outlineMode == RenderLayer.OutlineMode.IS_OUTLINE;
         this.hash = Objects.hash(new Object[]{super.hashCode(), phases});
      }

      private static RenderLayer.MultiPhase of(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, RenderLayer.MultiPhaseParameters phases) {
         return (RenderLayer.MultiPhase)CACHE.addOrGet(new RenderLayer.MultiPhase(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, phases));
      }

      public Optional<RenderLayer> getAffectedOutline() {
         return this.affectedOutline;
      }

      public boolean isOutline() {
         return this.outline;
      }

      public boolean equals(@Nullable Object object) {
         return this == object;
      }

      public int hashCode() {
         return this.hash;
      }

      public String toString() {
         return "RenderType[" + this.phases + ']';
      }

      static {
         CACHE = new ObjectOpenCustomHashSet(RenderLayer.MultiPhase.HashStrategy.INSTANCE);
      }

      @Environment(EnvType.CLIENT)
      static enum HashStrategy implements Strategy<RenderLayer.MultiPhase> {
         INSTANCE;

         public int hashCode(@Nullable RenderLayer.MultiPhase multiPhase) {
            return multiPhase == null ? 0 : multiPhase.hash;
         }

         public boolean equals(@Nullable RenderLayer.MultiPhase multiPhase, @Nullable RenderLayer.MultiPhase multiPhase2) {
            if (multiPhase == multiPhase2) {
               return true;
            } else {
               return multiPhase != null && multiPhase2 != null ? Objects.equals(multiPhase.phases, multiPhase2.phases) : false;
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static final class MultiPhaseParameters {
      private final RenderPhase.Texture texture;
      private final RenderPhase.Transparency transparency;
      private final RenderPhase.DiffuseLighting diffuseLighting;
      private final RenderPhase.ShadeModel shadeModel;
      private final RenderPhase.Alpha alpha;
      private final RenderPhase.DepthTest depthTest;
      private final RenderPhase.Cull cull;
      private final RenderPhase.Lightmap lightmap;
      private final RenderPhase.Overlay overlay;
      private final RenderPhase.Fog fog;
      private final RenderPhase.Layering layering;
      private final RenderPhase.Target target;
      private final RenderPhase.Texturing texturing;
      private final RenderPhase.WriteMaskState writeMaskState;
      private final RenderPhase.LineWidth lineWidth;
      private final RenderLayer.OutlineMode outlineMode;
      private final ImmutableList<RenderPhase> phases;

      private MultiPhaseParameters(RenderPhase.Texture texture, RenderPhase.Transparency transparency, RenderPhase.DiffuseLighting diffuseLighting, RenderPhase.ShadeModel shadeModel, RenderPhase.Alpha alpha, RenderPhase.DepthTest depthTest, RenderPhase.Cull cull, RenderPhase.Lightmap lightmap, RenderPhase.Overlay overlay, RenderPhase.Fog fog, RenderPhase.Layering layering, RenderPhase.Target target, RenderPhase.Texturing texturing, RenderPhase.WriteMaskState writeMaskState, RenderPhase.LineWidth lineWidth, RenderLayer.OutlineMode outlineMode) {
         this.texture = texture;
         this.transparency = transparency;
         this.diffuseLighting = diffuseLighting;
         this.shadeModel = shadeModel;
         this.alpha = alpha;
         this.depthTest = depthTest;
         this.cull = cull;
         this.lightmap = lightmap;
         this.overlay = overlay;
         this.fog = fog;
         this.layering = layering;
         this.target = target;
         this.texturing = texturing;
         this.writeMaskState = writeMaskState;
         this.lineWidth = lineWidth;
         this.outlineMode = outlineMode;
         this.phases = ImmutableList.of(this.texture, this.transparency, this.diffuseLighting, this.shadeModel, this.alpha, this.depthTest, this.cull, this.lightmap, this.overlay, this.fog, this.layering, this.target, this.texturing, this.writeMaskState, this.lineWidth);
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            RenderLayer.MultiPhaseParameters multiPhaseParameters = (RenderLayer.MultiPhaseParameters)object;
            return this.outlineMode == multiPhaseParameters.outlineMode && this.phases.equals(multiPhaseParameters.phases);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.phases, this.outlineMode});
      }

      public String toString() {
         return "CompositeState[" + this.phases + ", outlineProperty=" + this.outlineMode + ']';
      }

      public static RenderLayer.MultiPhaseParameters.Builder builder() {
         return new RenderLayer.MultiPhaseParameters.Builder();
      }

      @Environment(EnvType.CLIENT)
      public static class Builder {
         private RenderPhase.Texture texture;
         private RenderPhase.Transparency transparency;
         private RenderPhase.DiffuseLighting diffuseLighting;
         private RenderPhase.ShadeModel shadeModel;
         private RenderPhase.Alpha alpha;
         private RenderPhase.DepthTest depthTest;
         private RenderPhase.Cull cull;
         private RenderPhase.Lightmap lightmap;
         private RenderPhase.Overlay overlay;
         private RenderPhase.Fog fog;
         private RenderPhase.Layering layering;
         private RenderPhase.Target target;
         private RenderPhase.Texturing texturing;
         private RenderPhase.WriteMaskState writeMaskState;
         private RenderPhase.LineWidth lineWidth;

         private Builder() {
            this.texture = RenderPhase.NO_TEXTURE;
            this.transparency = RenderPhase.NO_TRANSPARENCY;
            this.diffuseLighting = RenderPhase.DISABLE_DIFFUSE_LIGHTING;
            this.shadeModel = RenderPhase.SHADE_MODEL;
            this.alpha = RenderPhase.ZERO_ALPHA;
            this.depthTest = RenderPhase.LEQUAL_DEPTH_TEST;
            this.cull = RenderPhase.ENABLE_CULLING;
            this.lightmap = RenderPhase.DISABLE_LIGHTMAP;
            this.overlay = RenderPhase.DISABLE_OVERLAY_COLOR;
            this.fog = RenderPhase.FOG;
            this.layering = RenderPhase.NO_LAYERING;
            this.target = RenderPhase.MAIN_TARGET;
            this.texturing = RenderPhase.DEFAULT_TEXTURING;
            this.writeMaskState = RenderPhase.ALL_MASK;
            this.lineWidth = RenderPhase.FULL_LINE_WIDTH;
         }

         public RenderLayer.MultiPhaseParameters.Builder texture(RenderPhase.Texture texture) {
            this.texture = texture;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder transparency(RenderPhase.Transparency transparency) {
            this.transparency = transparency;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder diffuseLighting(RenderPhase.DiffuseLighting diffuseLighting) {
            this.diffuseLighting = diffuseLighting;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder shadeModel(RenderPhase.ShadeModel shadeModel) {
            this.shadeModel = shadeModel;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder alpha(RenderPhase.Alpha alpha) {
            this.alpha = alpha;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder depthTest(RenderPhase.DepthTest depthTest) {
            this.depthTest = depthTest;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder cull(RenderPhase.Cull cull) {
            this.cull = cull;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder lightmap(RenderPhase.Lightmap lightmap) {
            this.lightmap = lightmap;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder overlay(RenderPhase.Overlay overlay) {
            this.overlay = overlay;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder fog(RenderPhase.Fog fog) {
            this.fog = fog;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder layering(RenderPhase.Layering layering) {
            this.layering = layering;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder target(RenderPhase.Target target) {
            this.target = target;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder texturing(RenderPhase.Texturing texturing) {
            this.texturing = texturing;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder writeMaskState(RenderPhase.WriteMaskState writeMaskState) {
            this.writeMaskState = writeMaskState;
            return this;
         }

         public RenderLayer.MultiPhaseParameters.Builder lineWidth(RenderPhase.LineWidth lineWidth) {
            this.lineWidth = lineWidth;
            return this;
         }

         public RenderLayer.MultiPhaseParameters build(boolean affectsOutline) {
            return this.build(affectsOutline ? RenderLayer.OutlineMode.AFFECTS_OUTLINE : RenderLayer.OutlineMode.NONE);
         }

         public RenderLayer.MultiPhaseParameters build(RenderLayer.OutlineMode outlineMode) {
            return new RenderLayer.MultiPhaseParameters(this.texture, this.transparency, this.diffuseLighting, this.shadeModel, this.alpha, this.depthTest, this.cull, this.lightmap, this.overlay, this.fog, this.layering, this.target, this.texturing, this.writeMaskState, this.lineWidth, outlineMode);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   static enum OutlineMode {
      NONE("none"),
      IS_OUTLINE("is_outline"),
      AFFECTS_OUTLINE("affects_outline");

      private final String name;

      private OutlineMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
