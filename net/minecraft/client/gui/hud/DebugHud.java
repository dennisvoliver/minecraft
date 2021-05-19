package net.minecraft.client.gui.hud;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.MetricsData;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class DebugHud extends DrawableHelper {
   private static final Map<Heightmap.Type, String> HEIGHT_MAP_TYPES = (Map)Util.make(new EnumMap(Heightmap.Type.class), (enumMap) -> {
      enumMap.put(Heightmap.Type.WORLD_SURFACE_WG, "SW");
      enumMap.put(Heightmap.Type.WORLD_SURFACE, "S");
      enumMap.put(Heightmap.Type.OCEAN_FLOOR_WG, "OW");
      enumMap.put(Heightmap.Type.OCEAN_FLOOR, "O");
      enumMap.put(Heightmap.Type.MOTION_BLOCKING, "M");
      enumMap.put(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, "ML");
   });
   private final MinecraftClient client;
   private final TextRenderer fontRenderer;
   private HitResult blockHit;
   private HitResult fluidHit;
   @Nullable
   private ChunkPos pos;
   @Nullable
   private WorldChunk chunk;
   @Nullable
   private CompletableFuture<WorldChunk> chunkFuture;

   public DebugHud(MinecraftClient client) {
      this.client = client;
      this.fontRenderer = client.textRenderer;
   }

   public void resetChunk() {
      this.chunkFuture = null;
      this.chunk = null;
   }

   public void render(MatrixStack matrices) {
      this.client.getProfiler().push("debug");
      RenderSystem.pushMatrix();
      Entity entity = this.client.getCameraEntity();
      this.blockHit = entity.raycast(20.0D, 0.0F, false);
      this.fluidHit = entity.raycast(20.0D, 0.0F, true);
      this.renderLeftText(matrices);
      this.renderRightText(matrices);
      RenderSystem.popMatrix();
      if (this.client.options.debugTpsEnabled) {
         int i = this.client.getWindow().getScaledWidth();
         this.drawMetricsData(matrices, this.client.getMetricsData(), 0, i / 2, true);
         IntegratedServer integratedServer = this.client.getServer();
         if (integratedServer != null) {
            this.drawMetricsData(matrices, integratedServer.getMetricsData(), i - Math.min(i / 2, 240), i / 2, false);
         }
      }

      this.client.getProfiler().pop();
   }

   protected void renderLeftText(MatrixStack matrices) {
      List<String> list = this.getLeftText();
      list.add("");
      boolean bl = this.client.getServer() != null;
      list.add("Debug: Pie [shift]: " + (this.client.options.debugProfilerEnabled ? "visible" : "hidden") + (bl ? " FPS + TPS" : " FPS") + " [alt]: " + (this.client.options.debugTpsEnabled ? "visible" : "hidden"));
      list.add("For help: press F3 + Q");

      for(int i = 0; i < list.size(); ++i) {
         String string = (String)list.get(i);
         if (!Strings.isNullOrEmpty(string)) {
            this.fontRenderer.getClass();
            int j = 9;
            int k = this.fontRenderer.getWidth(string);
            int l = true;
            int m = 2 + j * i;
            fill(matrices, 1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
            this.fontRenderer.draw(matrices, string, 2.0F, (float)m, 14737632);
         }
      }

   }

   protected void renderRightText(MatrixStack matrices) {
      List<String> list = this.getRightText();

      for(int i = 0; i < list.size(); ++i) {
         String string = (String)list.get(i);
         if (!Strings.isNullOrEmpty(string)) {
            this.fontRenderer.getClass();
            int j = 9;
            int k = this.fontRenderer.getWidth(string);
            int l = this.client.getWindow().getScaledWidth() - 2 - k;
            int m = 2 + j * i;
            fill(matrices, l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
            this.fontRenderer.draw(matrices, string, (float)l, (float)m, 14737632);
         }
      }

   }

   protected List<String> getLeftText() {
      IntegratedServer integratedServer = this.client.getServer();
      ClientConnection clientConnection = this.client.getNetworkHandler().getConnection();
      float f = clientConnection.getAveragePacketsSent();
      float g = clientConnection.getAveragePacketsReceived();
      String string2;
      if (integratedServer != null) {
         string2 = String.format("Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedServer.getTickTime(), f, g);
      } else {
         string2 = String.format("\"%s\" server, %.0f tx, %.0f rx", this.client.player.getServerBrand(), f, g);
      }

      BlockPos blockPos = this.client.getCameraEntity().getBlockPos();
      if (this.client.hasReducedDebugInfo()) {
         return Lists.newArrayList((Object[])("Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.client.fpsDebugString, string2, this.client.worldRenderer.getChunksDebugString(), this.client.worldRenderer.getEntitiesDebugString(), "P: " + this.client.particleManager.getDebugString() + ". T: " + this.client.world.getRegularEntityCount(), this.client.world.getDebugString(), "", String.format("Chunk-relative: %d %d %d", blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15)));
      } else {
         Entity entity = this.client.getCameraEntity();
         Direction direction = entity.getHorizontalFacing();
         String string7;
         switch(direction) {
         case NORTH:
            string7 = "Towards negative Z";
            break;
         case SOUTH:
            string7 = "Towards positive Z";
            break;
         case WEST:
            string7 = "Towards negative X";
            break;
         case EAST:
            string7 = "Towards positive X";
            break;
         default:
            string7 = "Invalid";
         }

         ChunkPos chunkPos = new ChunkPos(blockPos);
         if (!Objects.equals(this.pos, chunkPos)) {
            this.pos = chunkPos;
            this.resetChunk();
         }

         World world = this.getWorld();
         LongSet longSet = world instanceof ServerWorld ? ((ServerWorld)world).getForcedChunks() : LongSets.EMPTY_SET;
         List<String> list = Lists.newArrayList((Object[])("Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType()) + ")", this.client.fpsDebugString, string2, this.client.worldRenderer.getChunksDebugString(), this.client.worldRenderer.getEntitiesDebugString(), "P: " + this.client.particleManager.getDebugString() + ". T: " + this.client.world.getRegularEntityCount(), this.client.world.getDebugString()));
         String string8 = this.getServerWorldDebugString();
         if (string8 != null) {
            list.add(string8);
         }

         list.add(this.client.world.getRegistryKey().getValue() + " FC: " + ((LongSet)longSet).size());
         list.add("");
         list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.client.getCameraEntity().getX(), this.client.getCameraEntity().getY(), this.client.getCameraEntity().getZ()));
         list.add(String.format("Block: %d %d %d", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
         list.add(String.format("Chunk: %d %d %d in %d %d %d", blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15, blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4));
         list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, string7, MathHelper.wrapDegrees(entity.yaw), MathHelper.wrapDegrees(entity.pitch)));
         int m;
         if (this.client.world != null) {
            if (this.client.world.isChunkLoaded(blockPos)) {
               WorldChunk worldChunk = this.getClientChunk();
               if (worldChunk.isEmpty()) {
                  list.add("Waiting for chunk...");
               } else {
                  int i = this.client.world.getChunkManager().getLightingProvider().getLight(blockPos, 0);
                  int j = this.client.world.getLightLevel(LightType.SKY, blockPos);
                  m = this.client.world.getLightLevel(LightType.BLOCK, blockPos);
                  list.add("Client Light: " + i + " (" + j + " sky, " + m + " block)");
                  WorldChunk worldChunk2 = this.getChunk();
                  if (worldChunk2 != null) {
                     LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
                     list.add("Server Light: (" + lightingProvider.get(LightType.SKY).getLightLevel(blockPos) + " sky, " + lightingProvider.get(LightType.BLOCK).getLightLevel(blockPos) + " block)");
                  } else {
                     list.add("Server Light: (?? sky, ?? block)");
                  }

                  StringBuilder stringBuilder = new StringBuilder("CH");
                  Heightmap.Type[] var21 = Heightmap.Type.values();
                  int var22 = var21.length;

                  int var23;
                  Heightmap.Type type2;
                  for(var23 = 0; var23 < var22; ++var23) {
                     type2 = var21[var23];
                     if (type2.shouldSendToClient()) {
                        stringBuilder.append(" ").append((String)HEIGHT_MAP_TYPES.get(type2)).append(": ").append(worldChunk.sampleHeightmap(type2, blockPos.getX(), blockPos.getZ()));
                     }
                  }

                  list.add(stringBuilder.toString());
                  stringBuilder.setLength(0);
                  stringBuilder.append("SH");
                  var21 = Heightmap.Type.values();
                  var22 = var21.length;

                  for(var23 = 0; var23 < var22; ++var23) {
                     type2 = var21[var23];
                     if (type2.isStoredServerSide()) {
                        stringBuilder.append(" ").append((String)HEIGHT_MAP_TYPES.get(type2)).append(": ");
                        if (worldChunk2 != null) {
                           stringBuilder.append(worldChunk2.sampleHeightmap(type2, blockPos.getX(), blockPos.getZ()));
                        } else {
                           stringBuilder.append("??");
                        }
                     }
                  }

                  list.add(stringBuilder.toString());
                  if (blockPos.getY() >= 0 && blockPos.getY() < 256) {
                     list.add("Biome: " + this.client.world.getRegistryManager().get(Registry.BIOME_KEY).getId(this.client.world.getBiome(blockPos)));
                     long l = 0L;
                     float h = 0.0F;
                     if (worldChunk2 != null) {
                        h = world.getMoonSize();
                        l = worldChunk2.getInhabitedTime();
                     }

                     LocalDifficulty localDifficulty = new LocalDifficulty(world.getDifficulty(), world.getTimeOfDay(), l, h);
                     list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", localDifficulty.getLocalDifficulty(), localDifficulty.getClampedLocalDifficulty(), this.client.world.getTimeOfDay() / 24000L));
                  }
               }
            } else {
               list.add("Outside of world...");
            }
         } else {
            list.add("Outside of world...");
         }

         ServerWorld serverWorld = this.getServerWorld();
         if (serverWorld != null) {
            SpawnHelper.Info info = serverWorld.getChunkManager().getSpawnInfo();
            if (info != null) {
               Object2IntMap<SpawnGroup> object2IntMap = info.getGroupToCount();
               m = info.getSpawningChunkCount();
               list.add("SC: " + m + ", " + (String)Stream.of(SpawnGroup.values()).map((spawnGroup) -> {
                  return Character.toUpperCase(spawnGroup.getName().charAt(0)) + ": " + object2IntMap.getInt(spawnGroup);
               }).collect(Collectors.joining(", ")));
            } else {
               list.add("SC: N/A");
            }
         }

         ShaderEffect shaderEffect = this.client.gameRenderer.getShader();
         if (shaderEffect != null) {
            list.add("Shader: " + shaderEffect.getName());
         }

         list.add(this.client.getSoundManager().getDebugString() + String.format(" (Mood %d%%)", Math.round(this.client.player.getMoodPercentage() * 100.0F)));
         return list;
      }
   }

   @Nullable
   private ServerWorld getServerWorld() {
      IntegratedServer integratedServer = this.client.getServer();
      return integratedServer != null ? integratedServer.getWorld(this.client.world.getRegistryKey()) : null;
   }

   @Nullable
   private String getServerWorldDebugString() {
      ServerWorld serverWorld = this.getServerWorld();
      return serverWorld != null ? serverWorld.getDebugString() : null;
   }

   private World getWorld() {
      return (World)DataFixUtils.orElse(Optional.ofNullable(this.client.getServer()).flatMap((integratedServer) -> {
         return Optional.ofNullable(integratedServer.getWorld(this.client.world.getRegistryKey()));
      }), this.client.world);
   }

   @Nullable
   private WorldChunk getChunk() {
      if (this.chunkFuture == null) {
         ServerWorld serverWorld = this.getServerWorld();
         if (serverWorld != null) {
            this.chunkFuture = serverWorld.getChunkManager().getChunkFutureSyncOnMainThread(this.pos.x, this.pos.z, ChunkStatus.FULL, false).thenApply((either) -> {
               return (WorldChunk)either.map((chunk) -> {
                  return (WorldChunk)chunk;
               }, (unloaded) -> {
                  return null;
               });
            });
         }

         if (this.chunkFuture == null) {
            this.chunkFuture = CompletableFuture.completedFuture(this.getClientChunk());
         }
      }

      return (WorldChunk)this.chunkFuture.getNow((Object)null);
   }

   private WorldChunk getClientChunk() {
      if (this.chunk == null) {
         this.chunk = this.client.world.getChunk(this.pos.x, this.pos.z);
      }

      return this.chunk;
   }

   protected List<String> getRightText() {
      long l = Runtime.getRuntime().maxMemory();
      long m = Runtime.getRuntime().totalMemory();
      long n = Runtime.getRuntime().freeMemory();
      long o = m - n;
      List<String> list = Lists.newArrayList((Object[])(String.format("Java: %s %dbit", System.getProperty("java.version"), this.client.is64Bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", o * 100L / l, toMiB(o), toMiB(l)), String.format("Allocated: % 2d%% %03dMB", m * 100L / l, toMiB(m)), "", String.format("CPU: %s", GlDebugInfo.getCpuInfo()), "", String.format("Display: %dx%d (%s)", MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight(), GlDebugInfo.getVendor()), GlDebugInfo.getRenderer(), GlDebugInfo.getVersion()));
      if (this.client.hasReducedDebugInfo()) {
         return list;
      } else {
         BlockPos blockPos2;
         UnmodifiableIterator var12;
         Entry entry2;
         Iterator var16;
         Identifier identifier2;
         if (this.blockHit.getType() == HitResult.Type.BLOCK) {
            blockPos2 = ((BlockHitResult)this.blockHit).getBlockPos();
            BlockState blockState = this.client.world.getBlockState(blockPos2);
            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Block: " + blockPos2.getX() + ", " + blockPos2.getY() + ", " + blockPos2.getZ());
            list.add(String.valueOf(Registry.BLOCK.getId(blockState.getBlock())));
            var12 = blockState.getEntries().entrySet().iterator();

            while(var12.hasNext()) {
               entry2 = (Entry)var12.next();
               list.add(this.propertyToString(entry2));
            }

            var16 = this.client.getNetworkHandler().getTagManager().getBlocks().getTagsFor(blockState.getBlock()).iterator();

            while(var16.hasNext()) {
               identifier2 = (Identifier)var16.next();
               list.add("#" + identifier2);
            }
         }

         if (this.fluidHit.getType() == HitResult.Type.BLOCK) {
            blockPos2 = ((BlockHitResult)this.fluidHit).getBlockPos();
            FluidState fluidState = this.client.world.getFluidState(blockPos2);
            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Fluid: " + blockPos2.getX() + ", " + blockPos2.getY() + ", " + blockPos2.getZ());
            list.add(String.valueOf(Registry.FLUID.getId(fluidState.getFluid())));
            var12 = fluidState.getEntries().entrySet().iterator();

            while(var12.hasNext()) {
               entry2 = (Entry)var12.next();
               list.add(this.propertyToString(entry2));
            }

            var16 = this.client.getNetworkHandler().getTagManager().getFluids().getTagsFor(fluidState.getFluid()).iterator();

            while(var16.hasNext()) {
               identifier2 = (Identifier)var16.next();
               list.add("#" + identifier2);
            }
         }

         Entity entity = this.client.targetedEntity;
         if (entity != null) {
            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Entity");
            list.add(String.valueOf(Registry.ENTITY_TYPE.getId(entity.getType())));
         }

         return list;
      }
   }

   private String propertyToString(Entry<Property<?>, Comparable<?>> propEntry) {
      Property<?> property = (Property)propEntry.getKey();
      Comparable<?> comparable = (Comparable)propEntry.getValue();
      String string = Util.getValueAsString(property, comparable);
      if (Boolean.TRUE.equals(comparable)) {
         string = Formatting.GREEN + string;
      } else if (Boolean.FALSE.equals(comparable)) {
         string = Formatting.RED + string;
      }

      return property.getName() + ": " + string;
   }

   private void drawMetricsData(MatrixStack matrices, MetricsData metricsData, int x, int width, boolean showFps) {
      RenderSystem.disableDepthTest();
      int i = metricsData.getStartIndex();
      int j = metricsData.getCurrentIndex();
      long[] ls = metricsData.getSamples();
      int l = x;
      int m = Math.max(0, ls.length - width);
      int n = ls.length - m;
      int k = metricsData.wrapIndex(i + m);
      long o = 0L;
      int p = Integer.MAX_VALUE;
      int q = Integer.MIN_VALUE;

      int t;
      for(t = 0; t < n; ++t) {
         int s = (int)(ls[metricsData.wrapIndex(k + t)] / 1000000L);
         p = Math.min(p, s);
         q = Math.max(q, s);
         o += (long)s;
      }

      t = this.client.getWindow().getScaledHeight();
      fill(matrices, x, t - 60, x + n, t, -1873784752);
      BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
      RenderSystem.enableBlend();
      RenderSystem.disableTexture();
      RenderSystem.defaultBlendFunc();
      bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);

      for(Matrix4f matrix4f = AffineTransformation.identity().getMatrix(); k != j; k = metricsData.wrapIndex(k + 1)) {
         int u = metricsData.method_15248(ls[k], showFps ? 30 : 60, showFps ? 60 : 20);
         int v = showFps ? 100 : 60;
         int w = this.getMetricsLineColor(MathHelper.clamp(u, 0, v), 0, v / 2, v);
         int y = w >> 24 & 255;
         int z = w >> 16 & 255;
         int aa = w >> 8 & 255;
         int ab = w & 255;
         bufferBuilder.vertex(matrix4f, (float)(l + 1), (float)t, 0.0F).color(z, aa, ab, y).next();
         bufferBuilder.vertex(matrix4f, (float)(l + 1), (float)(t - u + 1), 0.0F).color(z, aa, ab, y).next();
         bufferBuilder.vertex(matrix4f, (float)l, (float)(t - u + 1), 0.0F).color(z, aa, ab, y).next();
         bufferBuilder.vertex(matrix4f, (float)l, (float)t, 0.0F).color(z, aa, ab, y).next();
         ++l;
      }

      bufferBuilder.end();
      BufferRenderer.draw(bufferBuilder);
      RenderSystem.enableTexture();
      RenderSystem.disableBlend();
      if (showFps) {
         fill(matrices, x + 1, t - 30 + 1, x + 14, t - 30 + 10, -1873784752);
         this.fontRenderer.draw(matrices, "60 FPS", (float)(x + 2), (float)(t - 30 + 2), 14737632);
         this.drawHorizontalLine(matrices, x, x + n - 1, t - 30, -1);
         fill(matrices, x + 1, t - 60 + 1, x + 14, t - 60 + 10, -1873784752);
         this.fontRenderer.draw(matrices, "30 FPS", (float)(x + 2), (float)(t - 60 + 2), 14737632);
         this.drawHorizontalLine(matrices, x, x + n - 1, t - 60, -1);
      } else {
         fill(matrices, x + 1, t - 60 + 1, x + 14, t - 60 + 10, -1873784752);
         this.fontRenderer.draw(matrices, "20 TPS", (float)(x + 2), (float)(t - 60 + 2), 14737632);
         this.drawHorizontalLine(matrices, x, x + n - 1, t - 60, -1);
      }

      this.drawHorizontalLine(matrices, x, x + n - 1, t - 1, -1);
      this.drawVerticalLine(matrices, x, t - 60, t, -1);
      this.drawVerticalLine(matrices, x + n - 1, t - 60, t, -1);
      if (showFps && this.client.options.maxFps > 0 && this.client.options.maxFps <= 250) {
         this.drawHorizontalLine(matrices, x, x + n - 1, t - 1 - (int)(1800.0D / (double)this.client.options.maxFps), -16711681);
      }

      String string = p + " ms min";
      String string2 = o / (long)n + " ms avg";
      String string3 = q + " ms max";
      TextRenderer var10000 = this.fontRenderer;
      float var10003 = (float)(x + 2);
      int var10004 = t - 60;
      this.fontRenderer.getClass();
      var10000.drawWithShadow(matrices, string, var10003, (float)(var10004 - 9), 14737632);
      var10000 = this.fontRenderer;
      var10003 = (float)(x + n / 2 - this.fontRenderer.getWidth(string2) / 2);
      var10004 = t - 60;
      this.fontRenderer.getClass();
      var10000.drawWithShadow(matrices, string2, var10003, (float)(var10004 - 9), 14737632);
      var10000 = this.fontRenderer;
      var10003 = (float)(x + n - this.fontRenderer.getWidth(string3));
      var10004 = t - 60;
      this.fontRenderer.getClass();
      var10000.drawWithShadow(matrices, string3, var10003, (float)(var10004 - 9), 14737632);
      RenderSystem.enableDepthTest();
   }

   private int getMetricsLineColor(int value, int greenValue, int yellowValue, int redValue) {
      return value < yellowValue ? this.interpolateColor(-16711936, -256, (float)value / (float)yellowValue) : this.interpolateColor(-256, -65536, (float)(value - yellowValue) / (float)(redValue - yellowValue));
   }

   private int interpolateColor(int color1, int color2, float dt) {
      int i = color1 >> 24 & 255;
      int j = color1 >> 16 & 255;
      int k = color1 >> 8 & 255;
      int l = color1 & 255;
      int m = color2 >> 24 & 255;
      int n = color2 >> 16 & 255;
      int o = color2 >> 8 & 255;
      int p = color2 & 255;
      int q = MathHelper.clamp((int)MathHelper.lerp(dt, (float)i, (float)m), 0, 255);
      int r = MathHelper.clamp((int)MathHelper.lerp(dt, (float)j, (float)n), 0, 255);
      int s = MathHelper.clamp((int)MathHelper.lerp(dt, (float)k, (float)o), 0, 255);
      int t = MathHelper.clamp((int)MathHelper.lerp(dt, (float)l, (float)p), 0, 255);
      return q << 24 | r << 16 | s << 8 | t;
   }

   private static long toMiB(long bytes) {
      return bytes / 1024L / 1024L;
   }
}
