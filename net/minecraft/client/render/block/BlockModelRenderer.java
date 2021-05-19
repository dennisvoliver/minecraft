package net.minecraft.client.render.block;

import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BlockModelRenderer {
   private final BlockColors colorMap;
   private static final ThreadLocal<BlockModelRenderer.BrightnessCache> brightnessCache = ThreadLocal.withInitial(() -> {
      return new BlockModelRenderer.BrightnessCache();
   });

   public BlockModelRenderer(BlockColors colorMap) {
      this.colorMap = colorMap;
   }

   public boolean render(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrix, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
      boolean bl = MinecraftClient.isAmbientOcclusionEnabled() && state.getLuminance() == 0 && model.useAmbientOcclusion();
      Vec3d vec3d = state.getModelOffset(world, pos);
      matrix.translate(vec3d.x, vec3d.y, vec3d.z);

      try {
         return bl ? this.renderSmooth(world, model, state, pos, matrix, vertexConsumer, cull, random, seed, overlay) : this.renderFlat(world, model, state, pos, matrix, vertexConsumer, cull, random, seed, overlay);
      } catch (Throwable var17) {
         CrashReport crashReport = CrashReport.create(var17, "Tesselating block model");
         CrashReportSection crashReportSection = crashReport.addElement("Block model being tesselated");
         CrashReportSection.addBlockInfo(crashReportSection, pos, state);
         crashReportSection.add("Using AO", (Object)bl);
         throw new CrashException(crashReport);
      }
   }

   public boolean renderSmooth(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack buffer, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
      boolean bl = false;
      float[] fs = new float[Direction.values().length * 2];
      BitSet bitSet = new BitSet(3);
      BlockModelRenderer.AmbientOcclusionCalculator ambientOcclusionCalculator = new BlockModelRenderer.AmbientOcclusionCalculator();
      Direction[] var16 = Direction.values();
      int var17 = var16.length;

      for(int var18 = 0; var18 < var17; ++var18) {
         Direction direction = var16[var18];
         random.setSeed(seed);
         List<BakedQuad> list = model.getQuads(state, direction, random);
         if (!list.isEmpty() && (!cull || Block.shouldDrawSide(state, world, pos, direction))) {
            this.renderQuadsSmooth(world, state, pos, buffer, vertexConsumer, list, fs, bitSet, ambientOcclusionCalculator, overlay);
            bl = true;
         }
      }

      random.setSeed(seed);
      List<BakedQuad> list2 = model.getQuads(state, (Direction)null, random);
      if (!list2.isEmpty()) {
         this.renderQuadsSmooth(world, state, pos, buffer, vertexConsumer, list2, fs, bitSet, ambientOcclusionCalculator, overlay);
         bl = true;
      }

      return bl;
   }

   public boolean renderFlat(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack buffer, VertexConsumer vertexConsumer, boolean cull, Random random, long l, int i) {
      boolean bl = false;
      BitSet bitSet = new BitSet(3);
      Direction[] var14 = Direction.values();
      int var15 = var14.length;

      for(int var16 = 0; var16 < var15; ++var16) {
         Direction direction = var14[var16];
         random.setSeed(l);
         List<BakedQuad> list = model.getQuads(state, direction, random);
         if (!list.isEmpty() && (!cull || Block.shouldDrawSide(state, world, pos, direction))) {
            int j = WorldRenderer.getLightmapCoordinates(world, state, pos.offset(direction));
            this.renderQuadsFlat(world, state, pos, j, i, false, buffer, vertexConsumer, list, bitSet);
            bl = true;
         }
      }

      random.setSeed(l);
      List<BakedQuad> list2 = model.getQuads(state, (Direction)null, random);
      if (!list2.isEmpty()) {
         this.renderQuadsFlat(world, state, pos, -1, i, true, buffer, vertexConsumer, list2, bitSet);
         bl = true;
      }

      return bl;
   }

   private void renderQuadsSmooth(BlockRenderView world, BlockState state, BlockPos pos, MatrixStack matrix, VertexConsumer vertexConsumer, List<BakedQuad> quads, float[] box, BitSet flags, BlockModelRenderer.AmbientOcclusionCalculator ambientOcclusionCalculator, int overlay) {
      Iterator var11 = quads.iterator();

      while(var11.hasNext()) {
         BakedQuad bakedQuad = (BakedQuad)var11.next();
         this.getQuadDimensions(world, state, pos, bakedQuad.getVertexData(), bakedQuad.getFace(), box, flags);
         ambientOcclusionCalculator.apply(world, state, pos, bakedQuad.getFace(), box, flags, bakedQuad.hasShade());
         this.renderQuad(world, state, pos, vertexConsumer, matrix.peek(), bakedQuad, ambientOcclusionCalculator.brightness[0], ambientOcclusionCalculator.brightness[1], ambientOcclusionCalculator.brightness[2], ambientOcclusionCalculator.brightness[3], ambientOcclusionCalculator.light[0], ambientOcclusionCalculator.light[1], ambientOcclusionCalculator.light[2], ambientOcclusionCalculator.light[3], overlay);
      }

   }

   private void renderQuad(BlockRenderView world, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad, float brightness0, float brightness1, float brightness2, float brightness3, int light0, int light1, int light2, int light3, int overlay) {
      float j;
      float k;
      float l;
      if (quad.hasColor()) {
         int i = this.colorMap.getColor(state, world, pos, quad.getColorIndex());
         j = (float)(i >> 16 & 255) / 255.0F;
         k = (float)(i >> 8 & 255) / 255.0F;
         l = (float)(i & 255) / 255.0F;
      } else {
         j = 1.0F;
         k = 1.0F;
         l = 1.0F;
      }

      vertexConsumer.quad(matrixEntry, quad, new float[]{brightness0, brightness1, brightness2, brightness3}, j, k, l, new int[]{light0, light1, light2, light3}, overlay, true);
   }

   private void getQuadDimensions(BlockRenderView world, BlockState state, BlockPos pos, int[] vertexData, Direction face, @Nullable float[] box, BitSet flags) {
      float f = 32.0F;
      float g = 32.0F;
      float h = 32.0F;
      float i = -32.0F;
      float j = -32.0F;
      float k = -32.0F;

      int p;
      float r;
      for(p = 0; p < 4; ++p) {
         r = Float.intBitsToFloat(vertexData[p * 8]);
         float n = Float.intBitsToFloat(vertexData[p * 8 + 1]);
         float o = Float.intBitsToFloat(vertexData[p * 8 + 2]);
         f = Math.min(f, r);
         g = Math.min(g, n);
         h = Math.min(h, o);
         i = Math.max(i, r);
         j = Math.max(j, n);
         k = Math.max(k, o);
      }

      if (box != null) {
         box[Direction.WEST.getId()] = f;
         box[Direction.EAST.getId()] = i;
         box[Direction.DOWN.getId()] = g;
         box[Direction.UP.getId()] = j;
         box[Direction.NORTH.getId()] = h;
         box[Direction.SOUTH.getId()] = k;
         p = Direction.values().length;
         box[Direction.WEST.getId() + p] = 1.0F - f;
         box[Direction.EAST.getId() + p] = 1.0F - i;
         box[Direction.DOWN.getId() + p] = 1.0F - g;
         box[Direction.UP.getId() + p] = 1.0F - j;
         box[Direction.NORTH.getId() + p] = 1.0F - h;
         box[Direction.SOUTH.getId() + p] = 1.0F - k;
      }

      float q = 1.0E-4F;
      r = 0.9999F;
      switch(face) {
      case DOWN:
         flags.set(1, f >= 1.0E-4F || h >= 1.0E-4F || i <= 0.9999F || k <= 0.9999F);
         flags.set(0, g == j && (g < 1.0E-4F || state.isFullCube(world, pos)));
         break;
      case UP:
         flags.set(1, f >= 1.0E-4F || h >= 1.0E-4F || i <= 0.9999F || k <= 0.9999F);
         flags.set(0, g == j && (j > 0.9999F || state.isFullCube(world, pos)));
         break;
      case NORTH:
         flags.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
         flags.set(0, h == k && (h < 1.0E-4F || state.isFullCube(world, pos)));
         break;
      case SOUTH:
         flags.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
         flags.set(0, h == k && (k > 0.9999F || state.isFullCube(world, pos)));
         break;
      case WEST:
         flags.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
         flags.set(0, f == i && (f < 1.0E-4F || state.isFullCube(world, pos)));
         break;
      case EAST:
         flags.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
         flags.set(0, f == i && (i > 0.9999F || state.isFullCube(world, pos)));
      }

   }

   private void renderQuadsFlat(BlockRenderView world, BlockState state, BlockPos pos, int light, int overlay, boolean useWorldLight, MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, BitSet flags) {
      Iterator var11 = quads.iterator();

      while(var11.hasNext()) {
         BakedQuad bakedQuad = (BakedQuad)var11.next();
         if (useWorldLight) {
            this.getQuadDimensions(world, state, pos, bakedQuad.getVertexData(), bakedQuad.getFace(), (float[])null, flags);
            BlockPos blockPos = flags.get(0) ? pos.offset(bakedQuad.getFace()) : pos;
            light = WorldRenderer.getLightmapCoordinates(world, state, blockPos);
         }

         float f = world.getBrightness(bakedQuad.getFace(), bakedQuad.hasShade());
         this.renderQuad(world, state, pos, vertexConsumer, matrices.peek(), bakedQuad, f, f, f, f, light, light, light, light, overlay);
      }

   }

   public void render(MatrixStack.Entry entry, VertexConsumer vertexConsumer, @Nullable BlockState blockState, BakedModel bakedModel, float f, float g, float h, int i, int j) {
      Random random = new Random();
      long l = 42L;
      Direction[] var13 = Direction.values();
      int var14 = var13.length;

      for(int var15 = 0; var15 < var14; ++var15) {
         Direction direction = var13[var15];
         random.setSeed(42L);
         renderQuad(entry, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, direction, random), i, j);
      }

      random.setSeed(42L);
      renderQuad(entry, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, (Direction)null, random), i, j);
   }

   private static void renderQuad(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float f, float g, float h, List<BakedQuad> list, int i, int j) {
      BakedQuad bakedQuad;
      float n;
      float o;
      float p;
      for(Iterator var8 = list.iterator(); var8.hasNext(); vertexConsumer.quad(entry, bakedQuad, n, o, p, i, j)) {
         bakedQuad = (BakedQuad)var8.next();
         if (bakedQuad.hasColor()) {
            n = MathHelper.clamp(f, 0.0F, 1.0F);
            o = MathHelper.clamp(g, 0.0F, 1.0F);
            p = MathHelper.clamp(h, 0.0F, 1.0F);
         } else {
            n = 1.0F;
            o = 1.0F;
            p = 1.0F;
         }
      }

   }

   public static void enableBrightnessCache() {
      ((BlockModelRenderer.BrightnessCache)brightnessCache.get()).enable();
   }

   public static void disableBrightnessCache() {
      ((BlockModelRenderer.BrightnessCache)brightnessCache.get()).disable();
   }

   @Environment(EnvType.CLIENT)
   public static enum NeighborData {
      DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5F, true, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.SOUTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.NORTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.NORTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.SOUTH}),
      UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0F, true, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.SOUTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.NORTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.NORTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.SOUTH}),
      NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8F, true, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_WEST}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_EAST}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_EAST}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_WEST}),
      SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8F, true, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.WEST}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.WEST}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.EAST}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.EAST}),
      WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.SOUTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.NORTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.NORTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.SOUTH}),
      EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.SOUTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.NORTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.NORTH}, new BlockModelRenderer.NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.SOUTH});

      private final Direction[] faces;
      private final boolean nonCubicWeight;
      private final BlockModelRenderer.NeighborOrientation[] field_4192;
      private final BlockModelRenderer.NeighborOrientation[] field_4185;
      private final BlockModelRenderer.NeighborOrientation[] field_4180;
      private final BlockModelRenderer.NeighborOrientation[] field_4188;
      private static final BlockModelRenderer.NeighborData[] field_4190 = (BlockModelRenderer.NeighborData[])Util.make(new BlockModelRenderer.NeighborData[6], (neighborDatas) -> {
         neighborDatas[Direction.DOWN.getId()] = DOWN;
         neighborDatas[Direction.UP.getId()] = UP;
         neighborDatas[Direction.NORTH.getId()] = NORTH;
         neighborDatas[Direction.SOUTH.getId()] = SOUTH;
         neighborDatas[Direction.WEST.getId()] = WEST;
         neighborDatas[Direction.EAST.getId()] = EAST;
      });

      private NeighborData(Direction[] directions, float f, boolean bl, BlockModelRenderer.NeighborOrientation[] neighborOrientations, BlockModelRenderer.NeighborOrientation[] neighborOrientations2, BlockModelRenderer.NeighborOrientation[] neighborOrientations3, BlockModelRenderer.NeighborOrientation[] neighborOrientations4) {
         this.faces = directions;
         this.nonCubicWeight = bl;
         this.field_4192 = neighborOrientations;
         this.field_4185 = neighborOrientations2;
         this.field_4180 = neighborOrientations3;
         this.field_4188 = neighborOrientations4;
      }

      public static BlockModelRenderer.NeighborData getData(Direction direction) {
         return field_4190[direction.getId()];
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum NeighborOrientation {
      DOWN(Direction.DOWN, false),
      UP(Direction.UP, false),
      NORTH(Direction.NORTH, false),
      SOUTH(Direction.SOUTH, false),
      WEST(Direction.WEST, false),
      EAST(Direction.EAST, false),
      FLIP_DOWN(Direction.DOWN, true),
      FLIP_UP(Direction.UP, true),
      FLIP_NORTH(Direction.NORTH, true),
      FLIP_SOUTH(Direction.SOUTH, true),
      FLIP_WEST(Direction.WEST, true),
      FLIP_EAST(Direction.EAST, true);

      private final int shape;

      private NeighborOrientation(Direction direction, boolean bl) {
         this.shape = direction.getId() + (bl ? Direction.values().length : 0);
      }
   }

   @Environment(EnvType.CLIENT)
   class AmbientOcclusionCalculator {
      private final float[] brightness = new float[4];
      private final int[] light = new int[4];

      public AmbientOcclusionCalculator() {
      }

      public void apply(BlockRenderView world, BlockState state, BlockPos pos, Direction direction, float[] box, BitSet flags, boolean bl) {
         BlockPos blockPos = flags.get(0) ? pos.offset(direction) : pos;
         BlockModelRenderer.NeighborData neighborData = BlockModelRenderer.NeighborData.getData(direction);
         BlockPos.Mutable mutable = new BlockPos.Mutable();
         BlockModelRenderer.BrightnessCache brightnessCache = (BlockModelRenderer.BrightnessCache)BlockModelRenderer.brightnessCache.get();
         mutable.set(blockPos, neighborData.faces[0]);
         BlockState blockState = world.getBlockState(mutable);
         int i = brightnessCache.getInt(blockState, world, mutable);
         float f = brightnessCache.getFloat(blockState, world, mutable);
         mutable.set(blockPos, neighborData.faces[1]);
         BlockState blockState2 = world.getBlockState(mutable);
         int j = brightnessCache.getInt(blockState2, world, mutable);
         float g = brightnessCache.getFloat(blockState2, world, mutable);
         mutable.set(blockPos, neighborData.faces[2]);
         BlockState blockState3 = world.getBlockState(mutable);
         int k = brightnessCache.getInt(blockState3, world, mutable);
         float h = brightnessCache.getFloat(blockState3, world, mutable);
         mutable.set(blockPos, neighborData.faces[3]);
         BlockState blockState4 = world.getBlockState(mutable);
         int l = brightnessCache.getInt(blockState4, world, mutable);
         float m = brightnessCache.getFloat(blockState4, world, mutable);
         mutable.set(blockPos, neighborData.faces[0]).move(direction);
         boolean bl2 = world.getBlockState(mutable).getOpacity(world, mutable) == 0;
         mutable.set(blockPos, neighborData.faces[1]).move(direction);
         boolean bl3 = world.getBlockState(mutable).getOpacity(world, mutable) == 0;
         mutable.set(blockPos, neighborData.faces[2]).move(direction);
         boolean bl4 = world.getBlockState(mutable).getOpacity(world, mutable) == 0;
         mutable.set(blockPos, neighborData.faces[3]).move(direction);
         boolean bl5 = world.getBlockState(mutable).getOpacity(world, mutable) == 0;
         float p;
         int q;
         BlockState blockState8;
         if (!bl4 && !bl2) {
            p = f;
            q = i;
         } else {
            mutable.set(blockPos, neighborData.faces[0]).move(neighborData.faces[2]);
            blockState8 = world.getBlockState(mutable);
            p = brightnessCache.getFloat(blockState8, world, mutable);
            q = brightnessCache.getInt(blockState8, world, mutable);
         }

         float t;
         int u;
         if (!bl5 && !bl2) {
            t = f;
            u = i;
         } else {
            mutable.set(blockPos, neighborData.faces[0]).move(neighborData.faces[3]);
            blockState8 = world.getBlockState(mutable);
            t = brightnessCache.getFloat(blockState8, world, mutable);
            u = brightnessCache.getInt(blockState8, world, mutable);
         }

         float x;
         int y;
         if (!bl4 && !bl3) {
            x = f;
            y = i;
         } else {
            mutable.set(blockPos, neighborData.faces[1]).move(neighborData.faces[2]);
            blockState8 = world.getBlockState(mutable);
            x = brightnessCache.getFloat(blockState8, world, mutable);
            y = brightnessCache.getInt(blockState8, world, mutable);
         }

         float ab;
         int ac;
         if (!bl5 && !bl3) {
            ab = f;
            ac = i;
         } else {
            mutable.set(blockPos, neighborData.faces[1]).move(neighborData.faces[3]);
            blockState8 = world.getBlockState(mutable);
            ab = brightnessCache.getFloat(blockState8, world, mutable);
            ac = brightnessCache.getInt(blockState8, world, mutable);
         }

         int ad = brightnessCache.getInt(state, world, pos);
         mutable.set(pos, direction);
         BlockState blockState9 = world.getBlockState(mutable);
         if (flags.get(0) || !blockState9.isOpaqueFullCube(world, mutable)) {
            ad = brightnessCache.getInt(blockState9, world, mutable);
         }

         float ae = flags.get(0) ? brightnessCache.getFloat(world.getBlockState(blockPos), world, blockPos) : brightnessCache.getFloat(world.getBlockState(pos), world, pos);
         BlockModelRenderer.Translation translation = BlockModelRenderer.Translation.getTranslations(direction);
         float bh;
         float ag;
         float ah;
         float ai;
         if (flags.get(1) && neighborData.nonCubicWeight) {
            bh = (m + f + t + ae) * 0.25F;
            ag = (h + f + p + ae) * 0.25F;
            ah = (h + g + x + ae) * 0.25F;
            ai = (m + g + ab + ae) * 0.25F;
            float an = box[neighborData.field_4192[0].shape] * box[neighborData.field_4192[1].shape];
            float ao = box[neighborData.field_4192[2].shape] * box[neighborData.field_4192[3].shape];
            float ap = box[neighborData.field_4192[4].shape] * box[neighborData.field_4192[5].shape];
            float aq = box[neighborData.field_4192[6].shape] * box[neighborData.field_4192[7].shape];
            float ar = box[neighborData.field_4185[0].shape] * box[neighborData.field_4185[1].shape];
            float as = box[neighborData.field_4185[2].shape] * box[neighborData.field_4185[3].shape];
            float at = box[neighborData.field_4185[4].shape] * box[neighborData.field_4185[5].shape];
            float au = box[neighborData.field_4185[6].shape] * box[neighborData.field_4185[7].shape];
            float av = box[neighborData.field_4180[0].shape] * box[neighborData.field_4180[1].shape];
            float aw = box[neighborData.field_4180[2].shape] * box[neighborData.field_4180[3].shape];
            float ax = box[neighborData.field_4180[4].shape] * box[neighborData.field_4180[5].shape];
            float ay = box[neighborData.field_4180[6].shape] * box[neighborData.field_4180[7].shape];
            float az = box[neighborData.field_4188[0].shape] * box[neighborData.field_4188[1].shape];
            float ba = box[neighborData.field_4188[2].shape] * box[neighborData.field_4188[3].shape];
            float bb = box[neighborData.field_4188[4].shape] * box[neighborData.field_4188[5].shape];
            float bc = box[neighborData.field_4188[6].shape] * box[neighborData.field_4188[7].shape];
            this.brightness[translation.firstCorner] = bh * an + ag * ao + ah * ap + ai * aq;
            this.brightness[translation.secondCorner] = bh * ar + ag * as + ah * at + ai * au;
            this.brightness[translation.thirdCorner] = bh * av + ag * aw + ah * ax + ai * ay;
            this.brightness[translation.fourthCorner] = bh * az + ag * ba + ah * bb + ai * bc;
            int bd = this.getAmbientOcclusionBrightness(l, i, u, ad);
            int be = this.getAmbientOcclusionBrightness(k, i, q, ad);
            int bf = this.getAmbientOcclusionBrightness(k, j, y, ad);
            int bg = this.getAmbientOcclusionBrightness(l, j, ac, ad);
            this.light[translation.firstCorner] = this.getBrightness(bd, be, bf, bg, an, ao, ap, aq);
            this.light[translation.secondCorner] = this.getBrightness(bd, be, bf, bg, ar, as, at, au);
            this.light[translation.thirdCorner] = this.getBrightness(bd, be, bf, bg, av, aw, ax, ay);
            this.light[translation.fourthCorner] = this.getBrightness(bd, be, bf, bg, az, ba, bb, bc);
         } else {
            bh = (m + f + t + ae) * 0.25F;
            ag = (h + f + p + ae) * 0.25F;
            ah = (h + g + x + ae) * 0.25F;
            ai = (m + g + ab + ae) * 0.25F;
            this.light[translation.firstCorner] = this.getAmbientOcclusionBrightness(l, i, u, ad);
            this.light[translation.secondCorner] = this.getAmbientOcclusionBrightness(k, i, q, ad);
            this.light[translation.thirdCorner] = this.getAmbientOcclusionBrightness(k, j, y, ad);
            this.light[translation.fourthCorner] = this.getAmbientOcclusionBrightness(l, j, ac, ad);
            this.brightness[translation.firstCorner] = bh;
            this.brightness[translation.secondCorner] = ag;
            this.brightness[translation.thirdCorner] = ah;
            this.brightness[translation.fourthCorner] = ai;
         }

         bh = world.getBrightness(direction, bl);

         for(int bi = 0; bi < this.brightness.length; ++bi) {
            float[] var10000 = this.brightness;
            var10000[bi] *= bh;
         }

      }

      private int getAmbientOcclusionBrightness(int i, int j, int k, int l) {
         if (i == 0) {
            i = l;
         }

         if (j == 0) {
            j = l;
         }

         if (k == 0) {
            k = l;
         }

         return i + j + k + l >> 2 & 16711935;
      }

      private int getBrightness(int i, int j, int k, int l, float f, float g, float h, float m) {
         int n = (int)((float)(i >> 16 & 255) * f + (float)(j >> 16 & 255) * g + (float)(k >> 16 & 255) * h + (float)(l >> 16 & 255) * m) & 255;
         int o = (int)((float)(i & 255) * f + (float)(j & 255) * g + (float)(k & 255) * h + (float)(l & 255) * m) & 255;
         return n << 16 | o;
      }
   }

   @Environment(EnvType.CLIENT)
   static class BrightnessCache {
      private boolean enabled;
      private final Long2IntLinkedOpenHashMap intCache;
      private final Long2FloatLinkedOpenHashMap floatCache;

      private BrightnessCache() {
         this.intCache = (Long2IntLinkedOpenHashMap)Util.make(() -> {
            Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25F) {
               protected void rehash(int i) {
               }
            };
            long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
            return long2IntLinkedOpenHashMap;
         });
         this.floatCache = (Long2FloatLinkedOpenHashMap)Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25F) {
               protected void rehash(int i) {
               }
            };
            long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
            return long2FloatLinkedOpenHashMap;
         });
      }

      public void enable() {
         this.enabled = true;
      }

      public void disable() {
         this.enabled = false;
         this.intCache.clear();
         this.floatCache.clear();
      }

      public int getInt(BlockState state, BlockRenderView blockRenderView, BlockPos pos) {
         long l = pos.asLong();
         int j;
         if (this.enabled) {
            j = this.intCache.get(l);
            if (j != Integer.MAX_VALUE) {
               return j;
            }
         }

         j = WorldRenderer.getLightmapCoordinates(blockRenderView, state, pos);
         if (this.enabled) {
            if (this.intCache.size() == 100) {
               this.intCache.removeFirstInt();
            }

            this.intCache.put(l, j);
         }

         return j;
      }

      public float getFloat(BlockState state, BlockRenderView blockView, BlockPos pos) {
         long l = pos.asLong();
         float g;
         if (this.enabled) {
            g = this.floatCache.get(l);
            if (!Float.isNaN(g)) {
               return g;
            }
         }

         g = state.getAmbientOcclusionLightLevel(blockView, pos);
         if (this.enabled) {
            if (this.floatCache.size() == 100) {
               this.floatCache.removeFirstFloat();
            }

            this.floatCache.put(l, g);
         }

         return g;
      }
   }

   @Environment(EnvType.CLIENT)
   static enum Translation {
      DOWN(0, 1, 2, 3),
      UP(2, 3, 0, 1),
      NORTH(3, 0, 1, 2),
      SOUTH(0, 1, 2, 3),
      WEST(3, 0, 1, 2),
      EAST(1, 2, 3, 0);

      private final int firstCorner;
      private final int secondCorner;
      private final int thirdCorner;
      private final int fourthCorner;
      private static final BlockModelRenderer.Translation[] VALUES = (BlockModelRenderer.Translation[])Util.make(new BlockModelRenderer.Translation[6], (translations) -> {
         translations[Direction.DOWN.getId()] = DOWN;
         translations[Direction.UP.getId()] = UP;
         translations[Direction.NORTH.getId()] = NORTH;
         translations[Direction.SOUTH.getId()] = SOUTH;
         translations[Direction.WEST.getId()] = WEST;
         translations[Direction.EAST.getId()] = EAST;
      });

      private Translation(int firstCorner, int secondCorner, int thirdCorner, int fourthCorner) {
         this.firstCorner = firstCorner;
         this.secondCorner = secondCorner;
         this.thirdCorner = thirdCorner;
         this.fourthCorner = fourthCorner;
      }

      public static BlockModelRenderer.Translation getTranslations(Direction direction) {
         return VALUES[direction.getId()];
      }
   }
}
