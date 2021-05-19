package net.minecraft.client.render.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BeeDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;
   private final Map<BlockPos, BeeDebugRenderer.Hive> hives = Maps.newHashMap();
   private final Map<UUID, BeeDebugRenderer.Bee> bees = Maps.newHashMap();
   private UUID targetedEntity;

   public BeeDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void clear() {
      this.hives.clear();
      this.bees.clear();
      this.targetedEntity = null;
   }

   public void addHive(BeeDebugRenderer.Hive hive) {
      this.hives.put(hive.pos, hive);
   }

   public void addBee(BeeDebugRenderer.Bee bee) {
      this.bees.put(bee.uuid, bee);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      RenderSystem.pushMatrix();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableTexture();
      this.removeOutdatedHives();
      this.removeInvalidBees();
      this.render();
      RenderSystem.enableTexture();
      RenderSystem.disableBlend();
      RenderSystem.popMatrix();
      if (!this.client.player.isSpectator()) {
         this.updateTargetedEntity();
      }

   }

   private void removeInvalidBees() {
      this.bees.entrySet().removeIf((entry) -> {
         return this.client.world.getEntityById(((BeeDebugRenderer.Bee)entry.getValue()).entityId) == null;
      });
   }

   private void removeOutdatedHives() {
      long l = this.client.world.getTime() - 20L;
      this.hives.entrySet().removeIf((entry) -> {
         return ((BeeDebugRenderer.Hive)entry.getValue()).time < l;
      });
   }

   private void render() {
      BlockPos blockPos = this.getCameraPos().getBlockPos();
      this.bees.values().forEach((bee) -> {
         if (this.isInRange(bee)) {
            this.drawBee(bee);
         }

      });
      this.drawFlowers();
      Iterator var2 = this.hives.keySet().iterator();

      while(var2.hasNext()) {
         BlockPos blockPos2 = (BlockPos)var2.next();
         if (blockPos.isWithinDistance(blockPos2, 30.0D)) {
            drawHive(blockPos2);
         }
      }

      Map<BlockPos, Set<UUID>> map = this.getBlacklistingBees();
      this.hives.values().forEach((hive) -> {
         if (blockPos.isWithinDistance(hive.pos, 30.0D)) {
            Set<UUID> set = (Set)map.get(hive.pos);
            this.drawHiveInfo(hive, (Collection)(set == null ? Sets.newHashSet() : set));
         }

      });
      this.getBeesByHive().forEach((blockPos2x, list) -> {
         if (blockPos.isWithinDistance(blockPos2x, 30.0D)) {
            this.drawHiveBees(blockPos2x, list);
         }

      });
   }

   private Map<BlockPos, Set<UUID>> getBlacklistingBees() {
      Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
      this.bees.values().forEach((bee) -> {
         bee.blacklist.forEach((blockPos) -> {
            ((Set)map.computeIfAbsent(blockPos, (blockPosx) -> {
               return Sets.newHashSet();
            })).add(bee.getUuid());
         });
      });
      return map;
   }

   private void drawFlowers() {
      Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
      this.bees.values().stream().filter(BeeDebugRenderer.Bee::hasFlower).forEach((bee) -> {
         ((Set)map.computeIfAbsent(bee.flower, (blockPos) -> {
            return Sets.newHashSet();
         })).add(bee.getUuid());
      });
      map.entrySet().forEach((entry) -> {
         BlockPos blockPos = (BlockPos)entry.getKey();
         Set<UUID> set = (Set)entry.getValue();
         Set<String> set2 = (Set)set.stream().map(NameGenerator::name).collect(Collectors.toSet());
         int i = 1;
         String var10000 = set2.toString();
         int var6 = i + 1;
         drawString(var10000, (BlockPos)blockPos, i, -256);
         drawString("Flower", (BlockPos)blockPos, var6++, -1);
         float f = 0.05F;
         drawBox(blockPos, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
      });
   }

   private static String toString(Collection<UUID> bees) {
      if (bees.isEmpty()) {
         return "-";
      } else {
         return bees.size() > 3 ? "" + bees.size() + " bees" : ((Set)bees.stream().map(NameGenerator::name).collect(Collectors.toSet())).toString();
      }
   }

   private static void drawHive(BlockPos pos) {
      float f = 0.05F;
      drawBox(pos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void drawHiveBees(BlockPos pos, List<String> bees) {
      float f = 0.05F;
      drawBox(pos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      drawString("" + bees, (BlockPos)pos, 0, -256);
      drawString("Ghost Hive", (BlockPos)pos, 1, -65536);
   }

   private static void drawBox(BlockPos pos, float expand, float red, float green, float blue, float alpha) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      DebugRenderer.drawBox(pos, expand, red, green, blue, alpha);
   }

   private void drawHiveInfo(BeeDebugRenderer.Hive hive, Collection<UUID> blacklistingBees) {
      int i = 0;
      if (!blacklistingBees.isEmpty()) {
         drawString("Blacklisted by " + toString(blacklistingBees), hive, i++, -65536);
      }

      drawString("Out: " + toString(this.getBeesForHive(hive.pos)), hive, i++, -3355444);
      if (hive.beeCount == 0) {
         drawString("In: -", (BeeDebugRenderer.Hive)hive, i++, -256);
      } else if (hive.beeCount == 1) {
         drawString("In: 1 bee", (BeeDebugRenderer.Hive)hive, i++, -256);
      } else {
         drawString("In: " + hive.beeCount + " bees", (BeeDebugRenderer.Hive)hive, i++, -256);
      }

      drawString("Honey: " + hive.honeyLevel, (BeeDebugRenderer.Hive)hive, i++, -23296);
      drawString(hive.label + (hive.sedated ? " (sedated)" : ""), (BeeDebugRenderer.Hive)hive, i++, -1);
   }

   private void drawPath(BeeDebugRenderer.Bee bee) {
      if (bee.path != null) {
         PathfindingDebugRenderer.drawPath(bee.path, 0.5F, false, false, this.getCameraPos().getPos().getX(), this.getCameraPos().getPos().getY(), this.getCameraPos().getPos().getZ());
      }

   }

   private void drawBee(BeeDebugRenderer.Bee bee) {
      boolean bl = this.isTargeted(bee);
      int i = 0;
      int var6 = i + 1;
      drawString(bee.position, i, bee.toString(), -1, 0.03F);
      if (bee.hive == null) {
         drawString(bee.position, var6++, "No hive", -98404, 0.02F);
      } else {
         drawString(bee.position, var6++, "Hive: " + this.getPositionString(bee, bee.hive), -256, 0.02F);
      }

      if (bee.flower == null) {
         drawString(bee.position, var6++, "No flower", -98404, 0.02F);
      } else {
         drawString(bee.position, var6++, "Flower: " + this.getPositionString(bee, bee.flower), -256, 0.02F);
      }

      Iterator var4 = bee.labels.iterator();

      while(var4.hasNext()) {
         String string = (String)var4.next();
         drawString(bee.position, var6++, string, -16711936, 0.02F);
      }

      if (bl) {
         this.drawPath(bee);
      }

      if (bee.travelTicks > 0) {
         int j = bee.travelTicks < 600 ? -3355444 : -23296;
         drawString(bee.position, var6++, "Travelling: " + bee.travelTicks + " ticks", j, 0.02F);
      }

   }

   private static void drawString(String string, BeeDebugRenderer.Hive hive, int line, int color) {
      BlockPos blockPos = hive.pos;
      drawString(string, blockPos, line, color);
   }

   private static void drawString(String string, BlockPos pos, int line, int color) {
      double d = 1.3D;
      double e = 0.2D;
      double f = (double)pos.getX() + 0.5D;
      double g = (double)pos.getY() + 1.3D + (double)line * 0.2D;
      double h = (double)pos.getZ() + 0.5D;
      DebugRenderer.drawString(string, f, g, h, color, 0.02F, true, 0.0F, true);
   }

   private static void drawString(Position pos, int line, String string, int color, float size) {
      double d = 2.4D;
      double e = 0.25D;
      BlockPos blockPos = new BlockPos(pos);
      double f = (double)blockPos.getX() + 0.5D;
      double g = pos.getY() + 2.4D + (double)line * 0.25D;
      double h = (double)blockPos.getZ() + 0.5D;
      float i = 0.5F;
      DebugRenderer.drawString(string, f, g, h, color, size, false, 0.5F, true);
   }

   private Camera getCameraPos() {
      return this.client.gameRenderer.getCamera();
   }

   private String getPositionString(BeeDebugRenderer.Bee bee, BlockPos pos) {
      float f = MathHelper.sqrt(pos.getSquaredDistance(bee.position.getX(), bee.position.getY(), bee.position.getZ(), true));
      double d = (double)Math.round(f * 10.0F) / 10.0D;
      return pos.toShortString() + " (dist " + d + ")";
   }

   private boolean isTargeted(BeeDebugRenderer.Bee bee) {
      return Objects.equals(this.targetedEntity, bee.uuid);
   }

   private boolean isInRange(BeeDebugRenderer.Bee bee) {
      PlayerEntity playerEntity = this.client.player;
      BlockPos blockPos = new BlockPos(playerEntity.getX(), bee.position.getY(), playerEntity.getZ());
      BlockPos blockPos2 = new BlockPos(bee.position);
      return blockPos.isWithinDistance(blockPos2, 30.0D);
   }

   private Collection<UUID> getBeesForHive(BlockPos hivePos) {
      return (Collection)this.bees.values().stream().filter((bee) -> {
         return bee.isHiveAt(hivePos);
      }).map(BeeDebugRenderer.Bee::getUuid).collect(Collectors.toSet());
   }

   private Map<BlockPos, List<String>> getBeesByHive() {
      Map<BlockPos, List<String>> map = Maps.newHashMap();
      Iterator var2 = this.bees.values().iterator();

      while(var2.hasNext()) {
         BeeDebugRenderer.Bee bee = (BeeDebugRenderer.Bee)var2.next();
         if (bee.hive != null && !this.hives.containsKey(bee.hive)) {
            ((List)map.computeIfAbsent(bee.hive, (blockPos) -> {
               return Lists.newArrayList();
            })).add(bee.getName());
         }
      }

      return map;
   }

   private void updateTargetedEntity() {
      DebugRenderer.getTargetedEntity(this.client.getCameraEntity(), 8).ifPresent((entity) -> {
         this.targetedEntity = entity.getUuid();
      });
   }

   @Environment(EnvType.CLIENT)
   public static class Bee {
      public final UUID uuid;
      public final int entityId;
      public final Position position;
      @Nullable
      public final Path path;
      @Nullable
      public final BlockPos hive;
      @Nullable
      public final BlockPos flower;
      public final int travelTicks;
      public final List<String> labels = Lists.newArrayList();
      public final Set<BlockPos> blacklist = Sets.newHashSet();

      public Bee(UUID uuid, int entityId, Position position, Path path, BlockPos hive, BlockPos flower, int travelTicks) {
         this.uuid = uuid;
         this.entityId = entityId;
         this.position = position;
         this.path = path;
         this.hive = hive;
         this.flower = flower;
         this.travelTicks = travelTicks;
      }

      public boolean isHiveAt(BlockPos pos) {
         return this.hive != null && this.hive.equals(pos);
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public String getName() {
         return NameGenerator.name(this.uuid);
      }

      public String toString() {
         return this.getName();
      }

      public boolean hasFlower() {
         return this.flower != null;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Hive {
      public final BlockPos pos;
      public final String label;
      public final int beeCount;
      public final int honeyLevel;
      public final boolean sedated;
      public final long time;

      public Hive(BlockPos pos, String label, int beeCount, int honeyLevel, boolean sedated, long time) {
         this.pos = pos;
         this.label = label;
         this.beeCount = beeCount;
         this.honeyLevel = honeyLevel;
         this.sedated = sedated;
         this.time = time;
      }
   }
}
