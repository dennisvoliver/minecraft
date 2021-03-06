package net.minecraft.village.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;

public class RaidManager extends PersistentState {
   private final Map<Integer, Raid> raids = Maps.newHashMap();
   private final ServerWorld world;
   private int nextAvailableId;
   private int currentTime;

   public RaidManager(ServerWorld world) {
      super(nameFor(world.getDimension()));
      this.world = world;
      this.nextAvailableId = 1;
      this.markDirty();
   }

   public Raid getRaid(int id) {
      return (Raid)this.raids.get(id);
   }

   public void tick() {
      ++this.currentTime;
      Iterator iterator = this.raids.values().iterator();

      while(iterator.hasNext()) {
         Raid raid = (Raid)iterator.next();
         if (this.world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
            raid.invalidate();
         }

         if (raid.hasStopped()) {
            iterator.remove();
            this.markDirty();
         } else {
            raid.tick();
         }
      }

      if (this.currentTime % 200 == 0) {
         this.markDirty();
      }

      DebugInfoSender.sendRaids(this.world, this.raids.values());
   }

   public static boolean isValidRaiderFor(RaiderEntity raider, Raid raid) {
      if (raider != null && raid != null && raid.getWorld() != null) {
         return raider.isAlive() && raider.canJoinRaid() && raider.getDespawnCounter() <= 2400 && raider.world.getDimension() == raid.getWorld().getDimension();
      } else {
         return false;
      }
   }

   @Nullable
   public Raid startRaid(ServerPlayerEntity player) {
      if (player.isSpectator()) {
         return null;
      } else if (this.world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
         return null;
      } else {
         DimensionType dimensionType = player.world.getDimension();
         if (!dimensionType.hasRaids()) {
            return null;
         } else {
            BlockPos blockPos = player.getBlockPos();
            List<PointOfInterest> list = (List)this.world.getPointOfInterestStorage().getInCircle(PointOfInterestType.ALWAYS_TRUE, blockPos, 64, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED).collect(Collectors.toList());
            int i = 0;
            Vec3d vec3d = Vec3d.ZERO;

            for(Iterator var8 = list.iterator(); var8.hasNext(); ++i) {
               PointOfInterest pointOfInterest = (PointOfInterest)var8.next();
               BlockPos blockPos2 = pointOfInterest.getPos();
               vec3d = vec3d.add((double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ());
            }

            BlockPos blockPos4;
            if (i > 0) {
               vec3d = vec3d.multiply(1.0D / (double)i);
               blockPos4 = new BlockPos(vec3d);
            } else {
               blockPos4 = blockPos;
            }

            Raid raid = this.getOrCreateRaid(player.getServerWorld(), blockPos4);
            boolean bl = false;
            if (!raid.hasStarted()) {
               if (!this.raids.containsKey(raid.getRaidId())) {
                  this.raids.put(raid.getRaidId(), raid);
               }

               bl = true;
            } else if (raid.getBadOmenLevel() < raid.getMaxAcceptableBadOmenLevel()) {
               bl = true;
            } else {
               player.removeStatusEffect(StatusEffects.BAD_OMEN);
               player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, (byte)43));
            }

            if (bl) {
               raid.start(player);
               player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, (byte)43));
               if (!raid.hasSpawned()) {
                  player.incrementStat(Stats.RAID_TRIGGER);
                  Criteria.VOLUNTARY_EXILE.trigger(player);
               }
            }

            this.markDirty();
            return raid;
         }
      }
   }

   private Raid getOrCreateRaid(ServerWorld world, BlockPos pos) {
      Raid raid = world.getRaidAt(pos);
      return raid != null ? raid : new Raid(this.nextId(), world, pos);
   }

   public void fromTag(CompoundTag tag) {
      this.nextAvailableId = tag.getInt("NextAvailableID");
      this.currentTime = tag.getInt("Tick");
      ListTag listTag = tag.getList("Raids", 10);

      for(int i = 0; i < listTag.size(); ++i) {
         CompoundTag compoundTag = listTag.getCompound(i);
         Raid raid = new Raid(this.world, compoundTag);
         this.raids.put(raid.getRaidId(), raid);
      }

   }

   public CompoundTag toTag(CompoundTag tag) {
      tag.putInt("NextAvailableID", this.nextAvailableId);
      tag.putInt("Tick", this.currentTime);
      ListTag listTag = new ListTag();
      Iterator var3 = this.raids.values().iterator();

      while(var3.hasNext()) {
         Raid raid = (Raid)var3.next();
         CompoundTag compoundTag = new CompoundTag();
         raid.toTag(compoundTag);
         listTag.add(compoundTag);
      }

      tag.put("Raids", listTag);
      return tag;
   }

   public static String nameFor(DimensionType dimensionType) {
      return "raids" + dimensionType.getSuffix();
   }

   private int nextId() {
      return ++this.nextAvailableId;
   }

   @Nullable
   public Raid getRaidAt(BlockPos pos, int i) {
      Raid raid = null;
      double d = (double)i;
      Iterator var6 = this.raids.values().iterator();

      while(var6.hasNext()) {
         Raid raid2 = (Raid)var6.next();
         double e = raid2.getCenter().getSquaredDistance(pos);
         if (raid2.isActive() && e < d) {
            raid = raid2;
            d = e;
         }
      }

      return raid;
   }
}
