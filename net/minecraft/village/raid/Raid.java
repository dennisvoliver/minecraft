package net.minecraft.village.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class Raid {
   private static final Text EVENT_TEXT = new TranslatableText("event.minecraft.raid");
   private static final Text VICTORY_SUFFIX_TEXT = new TranslatableText("event.minecraft.raid.victory");
   private static final Text DEFEAT_SUFFIX_TEXT = new TranslatableText("event.minecraft.raid.defeat");
   private static final Text VICTORY_TITLE;
   private static final Text DEFEAT_TITLE;
   private final Map<Integer, RaiderEntity> waveToCaptain = Maps.newHashMap();
   private final Map<Integer, Set<RaiderEntity>> waveToRaiders = Maps.newHashMap();
   private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
   private long ticksActive;
   private BlockPos center;
   private final ServerWorld world;
   private boolean started;
   private final int id;
   private float totalHealth;
   private int badOmenLevel;
   private boolean active;
   private int wavesSpawned;
   private final ServerBossBar bar;
   private int postRaidTicks;
   private int preRaidTicks;
   private final Random random;
   private final int waveCount;
   private Raid.Status status;
   private int finishCooldown;
   private Optional<BlockPos> preCalculatedRavagerSpawnLocation;

   public Raid(int id, ServerWorld world, BlockPos pos) {
      this.bar = new ServerBossBar(EVENT_TEXT, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
      this.random = new Random();
      this.preCalculatedRavagerSpawnLocation = Optional.empty();
      this.id = id;
      this.world = world;
      this.active = true;
      this.preRaidTicks = 300;
      this.bar.setPercent(0.0F);
      this.center = pos;
      this.waveCount = this.getMaxWaves(world.getDifficulty());
      this.status = Raid.Status.ONGOING;
   }

   public Raid(ServerWorld world, CompoundTag tag) {
      this.bar = new ServerBossBar(EVENT_TEXT, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
      this.random = new Random();
      this.preCalculatedRavagerSpawnLocation = Optional.empty();
      this.world = world;
      this.id = tag.getInt("Id");
      this.started = tag.getBoolean("Started");
      this.active = tag.getBoolean("Active");
      this.ticksActive = tag.getLong("TicksActive");
      this.badOmenLevel = tag.getInt("BadOmenLevel");
      this.wavesSpawned = tag.getInt("GroupsSpawned");
      this.preRaidTicks = tag.getInt("PreRaidTicks");
      this.postRaidTicks = tag.getInt("PostRaidTicks");
      this.totalHealth = tag.getFloat("TotalHealth");
      this.center = new BlockPos(tag.getInt("CX"), tag.getInt("CY"), tag.getInt("CZ"));
      this.waveCount = tag.getInt("NumGroups");
      this.status = Raid.Status.fromName(tag.getString("Status"));
      this.heroesOfTheVillage.clear();
      if (tag.contains("HeroesOfTheVillage", 9)) {
         ListTag listTag = tag.getList("HeroesOfTheVillage", 11);

         for(int i = 0; i < listTag.size(); ++i) {
            this.heroesOfTheVillage.add(NbtHelper.toUuid(listTag.get(i)));
         }
      }

   }

   public boolean isFinished() {
      return this.hasWon() || this.hasLost();
   }

   public boolean isPreRaid() {
      return this.hasSpawned() && this.getRaiderCount() == 0 && this.preRaidTicks > 0;
   }

   public boolean hasSpawned() {
      return this.wavesSpawned > 0;
   }

   public boolean hasStopped() {
      return this.status == Raid.Status.STOPPED;
   }

   public boolean hasWon() {
      return this.status == Raid.Status.VICTORY;
   }

   public boolean hasLost() {
      return this.status == Raid.Status.LOSS;
   }

   public World getWorld() {
      return this.world;
   }

   public boolean hasStarted() {
      return this.started;
   }

   public int getGroupsSpawned() {
      return this.wavesSpawned;
   }

   private Predicate<ServerPlayerEntity> isInRaidDistance() {
      return (player) -> {
         BlockPos blockPos = player.getBlockPos();
         return player.isAlive() && this.world.getRaidAt(blockPos) == this;
      };
   }

   private void updateBarToPlayers() {
      Set<ServerPlayerEntity> set = Sets.newHashSet((Iterable)this.bar.getPlayers());
      List<ServerPlayerEntity> list = this.world.getPlayers(this.isInRaidDistance());
      Iterator var3 = list.iterator();

      ServerPlayerEntity serverPlayerEntity2;
      while(var3.hasNext()) {
         serverPlayerEntity2 = (ServerPlayerEntity)var3.next();
         if (!set.contains(serverPlayerEntity2)) {
            this.bar.addPlayer(serverPlayerEntity2);
         }
      }

      var3 = set.iterator();

      while(var3.hasNext()) {
         serverPlayerEntity2 = (ServerPlayerEntity)var3.next();
         if (!list.contains(serverPlayerEntity2)) {
            this.bar.removePlayer(serverPlayerEntity2);
         }
      }

   }

   public int getMaxAcceptableBadOmenLevel() {
      return 5;
   }

   public int getBadOmenLevel() {
      return this.badOmenLevel;
   }

   public void start(PlayerEntity player) {
      if (player.hasStatusEffect(StatusEffects.BAD_OMEN)) {
         this.badOmenLevel += player.getStatusEffect(StatusEffects.BAD_OMEN).getAmplifier() + 1;
         this.badOmenLevel = MathHelper.clamp(this.badOmenLevel, 0, this.getMaxAcceptableBadOmenLevel());
      }

      player.removeStatusEffect(StatusEffects.BAD_OMEN);
   }

   public void invalidate() {
      this.active = false;
      this.bar.clearPlayers();
      this.status = Raid.Status.STOPPED;
   }

   public void tick() {
      if (!this.hasStopped()) {
         if (this.status == Raid.Status.ONGOING) {
            boolean bl = this.active;
            this.active = this.world.isChunkLoaded(this.center);
            if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
               this.invalidate();
               return;
            }

            if (bl != this.active) {
               this.bar.setVisible(this.active);
            }

            if (!this.active) {
               return;
            }

            if (!this.world.isNearOccupiedPointOfInterest(this.center)) {
               this.moveRaidCenter();
            }

            if (!this.world.isNearOccupiedPointOfInterest(this.center)) {
               if (this.wavesSpawned > 0) {
                  this.status = Raid.Status.LOSS;
               } else {
                  this.invalidate();
               }
            }

            ++this.ticksActive;
            if (this.ticksActive >= 48000L) {
               this.invalidate();
               return;
            }

            int i = this.getRaiderCount();
            boolean bl4;
            if (i == 0 && this.shouldSpawnMoreGroups()) {
               if (this.preRaidTicks <= 0) {
                  if (this.preRaidTicks == 0 && this.wavesSpawned > 0) {
                     this.preRaidTicks = 300;
                     this.bar.setName(EVENT_TEXT);
                     return;
                  }
               } else {
                  bl4 = this.preCalculatedRavagerSpawnLocation.isPresent();
                  boolean bl3 = !bl4 && this.preRaidTicks % 5 == 0;
                  if (bl4 && !this.world.getChunkManager().shouldTickChunk(new ChunkPos((BlockPos)this.preCalculatedRavagerSpawnLocation.get()))) {
                     bl3 = true;
                  }

                  if (bl3) {
                     int j = 0;
                     if (this.preRaidTicks < 100) {
                        j = 1;
                     } else if (this.preRaidTicks < 40) {
                        j = 2;
                     }

                     this.preCalculatedRavagerSpawnLocation = this.preCalculateRavagerSpawnLocation(j);
                  }

                  if (this.preRaidTicks == 300 || this.preRaidTicks % 20 == 0) {
                     this.updateBarToPlayers();
                  }

                  --this.preRaidTicks;
                  this.bar.setPercent(MathHelper.clamp((float)(300 - this.preRaidTicks) / 300.0F, 0.0F, 1.0F));
               }
            }

            if (this.ticksActive % 20L == 0L) {
               this.updateBarToPlayers();
               this.removeObsoleteRaiders();
               if (i > 0) {
                  if (i <= 2) {
                     this.bar.setName(EVENT_TEXT.shallowCopy().append(" - ").append((Text)(new TranslatableText("event.minecraft.raid.raiders_remaining", new Object[]{i}))));
                  } else {
                     this.bar.setName(EVENT_TEXT);
                  }
               } else {
                  this.bar.setName(EVENT_TEXT);
               }
            }

            bl4 = false;
            int k = 0;

            while(this.canSpawnRaiders()) {
               BlockPos blockPos = this.preCalculatedRavagerSpawnLocation.isPresent() ? (BlockPos)this.preCalculatedRavagerSpawnLocation.get() : this.getRavagerSpawnLocation(k, 20);
               if (blockPos != null) {
                  this.started = true;
                  this.spawnNextWave(blockPos);
                  if (!bl4) {
                     this.playRaidHorn(blockPos);
                     bl4 = true;
                  }
               } else {
                  ++k;
               }

               if (k > 3) {
                  this.invalidate();
                  break;
               }
            }

            if (this.hasStarted() && !this.shouldSpawnMoreGroups() && i == 0) {
               if (this.postRaidTicks < 40) {
                  ++this.postRaidTicks;
               } else {
                  this.status = Raid.Status.VICTORY;
                  Iterator var12 = this.heroesOfTheVillage.iterator();

                  while(var12.hasNext()) {
                     UUID uUID = (UUID)var12.next();
                     Entity entity = this.world.getEntity(uUID);
                     if (entity instanceof LivingEntity && !entity.isSpectator()) {
                        LivingEntity livingEntity = (LivingEntity)entity;
                        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                        if (livingEntity instanceof ServerPlayerEntity) {
                           ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)livingEntity;
                           serverPlayerEntity.incrementStat(Stats.RAID_WIN);
                           Criteria.HERO_OF_THE_VILLAGE.trigger(serverPlayerEntity);
                        }
                     }
                  }
               }
            }

            this.markDirty();
         } else if (this.isFinished()) {
            ++this.finishCooldown;
            if (this.finishCooldown >= 600) {
               this.invalidate();
               return;
            }

            if (this.finishCooldown % 20 == 0) {
               this.updateBarToPlayers();
               this.bar.setVisible(true);
               if (this.hasWon()) {
                  this.bar.setPercent(0.0F);
                  this.bar.setName(VICTORY_TITLE);
               } else {
                  this.bar.setName(DEFEAT_TITLE);
               }
            }
         }

      }
   }

   private void moveRaidCenter() {
      Stream<ChunkSectionPos> stream = ChunkSectionPos.stream((ChunkSectionPos)ChunkSectionPos.from(this.center), 2);
      ServerWorld var10001 = this.world;
      var10001.getClass();
      stream.filter(var10001::isNearOccupiedPointOfInterest).map(ChunkSectionPos::getCenterPos).min(Comparator.comparingDouble((blockPos) -> {
         return blockPos.getSquaredDistance(this.center);
      })).ifPresent(this::setCenter);
   }

   private Optional<BlockPos> preCalculateRavagerSpawnLocation(int proximity) {
      for(int i = 0; i < 3; ++i) {
         BlockPos blockPos = this.getRavagerSpawnLocation(proximity, 1);
         if (blockPos != null) {
            return Optional.of(blockPos);
         }
      }

      return Optional.empty();
   }

   private boolean shouldSpawnMoreGroups() {
      if (this.hasExtraWave()) {
         return !this.hasSpawnedExtraWave();
      } else {
         return !this.hasSpawnedFinalWave();
      }
   }

   private boolean hasSpawnedFinalWave() {
      return this.getGroupsSpawned() == this.waveCount;
   }

   private boolean hasExtraWave() {
      return this.badOmenLevel > 1;
   }

   private boolean hasSpawnedExtraWave() {
      return this.getGroupsSpawned() > this.waveCount;
   }

   private boolean isSpawningExtraWave() {
      return this.hasSpawnedFinalWave() && this.getRaiderCount() == 0 && this.hasExtraWave();
   }

   private void removeObsoleteRaiders() {
      Iterator<Set<RaiderEntity>> iterator = this.waveToRaiders.values().iterator();
      HashSet set = Sets.newHashSet();

      label54:
      while(iterator.hasNext()) {
         Set<RaiderEntity> set2 = (Set)iterator.next();
         Iterator var4 = set2.iterator();

         while(true) {
            while(true) {
               if (!var4.hasNext()) {
                  continue label54;
               }

               RaiderEntity raiderEntity = (RaiderEntity)var4.next();
               BlockPos blockPos = raiderEntity.getBlockPos();
               if (!raiderEntity.removed && raiderEntity.world.getRegistryKey() == this.world.getRegistryKey() && !(this.center.getSquaredDistance(blockPos) >= 12544.0D)) {
                  if (raiderEntity.age > 600) {
                     if (this.world.getEntity(raiderEntity.getUuid()) == null) {
                        set.add(raiderEntity);
                     }

                     if (!this.world.isNearOccupiedPointOfInterest(blockPos) && raiderEntity.getDespawnCounter() > 2400) {
                        raiderEntity.setOutOfRaidCounter(raiderEntity.getOutOfRaidCounter() + 1);
                     }

                     if (raiderEntity.getOutOfRaidCounter() >= 30) {
                        set.add(raiderEntity);
                     }
                  }
               } else {
                  set.add(raiderEntity);
               }
            }
         }
      }

      Iterator var7 = set.iterator();

      while(var7.hasNext()) {
         RaiderEntity raiderEntity2 = (RaiderEntity)var7.next();
         this.removeFromWave(raiderEntity2, true);
      }

   }

   private void playRaidHorn(BlockPos pos) {
      float f = 13.0F;
      int i = true;
      Collection<ServerPlayerEntity> collection = this.bar.getPlayers();
      Iterator var5 = this.world.getPlayers().iterator();

      while(true) {
         ServerPlayerEntity serverPlayerEntity;
         float g;
         double d;
         double e;
         do {
            if (!var5.hasNext()) {
               return;
            }

            serverPlayerEntity = (ServerPlayerEntity)var5.next();
            Vec3d vec3d = serverPlayerEntity.getPos();
            Vec3d vec3d2 = Vec3d.ofCenter(pos);
            g = MathHelper.sqrt((vec3d2.x - vec3d.x) * (vec3d2.x - vec3d.x) + (vec3d2.z - vec3d.z) * (vec3d2.z - vec3d.z));
            d = vec3d.x + (double)(13.0F / g) * (vec3d2.x - vec3d.x);
            e = vec3d.z + (double)(13.0F / g) * (vec3d2.z - vec3d.z);
         } while(!(g <= 64.0F) && !collection.contains(serverPlayerEntity));

         serverPlayerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.EVENT_RAID_HORN, SoundCategory.NEUTRAL, d, serverPlayerEntity.getY(), e, 64.0F, 1.0F));
      }
   }

   private void spawnNextWave(BlockPos pos) {
      boolean bl = false;
      int i = this.wavesSpawned + 1;
      this.totalHealth = 0.0F;
      LocalDifficulty localDifficulty = this.world.getLocalDifficulty(pos);
      boolean bl2 = this.isSpawningExtraWave();
      Raid.Member[] var6 = Raid.Member.VALUES;
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Raid.Member member = var6[var8];
         int j = this.getCount(member, i, bl2) + this.getBonusCount(member, this.random, i, localDifficulty, bl2);
         int k = 0;

         for(int l = 0; l < j; ++l) {
            RaiderEntity raiderEntity = (RaiderEntity)member.type.create(this.world);
            if (!bl && raiderEntity.canLead()) {
               raiderEntity.setPatrolLeader(true);
               this.setWaveCaptain(i, raiderEntity);
               bl = true;
            }

            this.addRaider(i, raiderEntity, pos, false);
            if (member.type == EntityType.RAVAGER) {
               RaiderEntity raiderEntity2 = null;
               if (i == this.getMaxWaves(Difficulty.NORMAL)) {
                  raiderEntity2 = (RaiderEntity)EntityType.PILLAGER.create(this.world);
               } else if (i >= this.getMaxWaves(Difficulty.HARD)) {
                  if (k == 0) {
                     raiderEntity2 = (RaiderEntity)EntityType.EVOKER.create(this.world);
                  } else {
                     raiderEntity2 = (RaiderEntity)EntityType.VINDICATOR.create(this.world);
                  }
               }

               ++k;
               if (raiderEntity2 != null) {
                  this.addRaider(i, raiderEntity2, pos, false);
                  raiderEntity2.refreshPositionAndAngles(pos, 0.0F, 0.0F);
                  raiderEntity2.startRiding(raiderEntity);
               }
            }
         }
      }

      this.preCalculatedRavagerSpawnLocation = Optional.empty();
      ++this.wavesSpawned;
      this.updateBar();
      this.markDirty();
   }

   public void addRaider(int wave, RaiderEntity raider, @Nullable BlockPos pos, boolean existing) {
      boolean bl = this.addToWave(wave, raider);
      if (bl) {
         raider.setRaid(this);
         raider.setWave(wave);
         raider.setAbleToJoinRaid(true);
         raider.setOutOfRaidCounter(0);
         if (!existing && pos != null) {
            raider.updatePosition((double)pos.getX() + 0.5D, (double)pos.getY() + 1.0D, (double)pos.getZ() + 0.5D);
            raider.initialize(this.world, this.world.getLocalDifficulty(pos), SpawnReason.EVENT, (EntityData)null, (CompoundTag)null);
            raider.addBonusForWave(wave, false);
            raider.setOnGround(true);
            this.world.spawnEntityAndPassengers(raider);
         }
      }

   }

   public void updateBar() {
      this.bar.setPercent(MathHelper.clamp(this.getCurrentRaiderHealth() / this.totalHealth, 0.0F, 1.0F));
   }

   public float getCurrentRaiderHealth() {
      float f = 0.0F;
      Iterator var2 = this.waveToRaiders.values().iterator();

      while(var2.hasNext()) {
         Set<RaiderEntity> set = (Set)var2.next();

         RaiderEntity raiderEntity;
         for(Iterator var4 = set.iterator(); var4.hasNext(); f += raiderEntity.getHealth()) {
            raiderEntity = (RaiderEntity)var4.next();
         }
      }

      return f;
   }

   private boolean canSpawnRaiders() {
      return this.preRaidTicks == 0 && (this.wavesSpawned < this.waveCount || this.isSpawningExtraWave()) && this.getRaiderCount() == 0;
   }

   public int getRaiderCount() {
      return this.waveToRaiders.values().stream().mapToInt(Set::size).sum();
   }

   public void removeFromWave(RaiderEntity entity, boolean countHealth) {
      Set<RaiderEntity> set = (Set)this.waveToRaiders.get(entity.getWave());
      if (set != null) {
         boolean bl = set.remove(entity);
         if (bl) {
            if (countHealth) {
               this.totalHealth -= entity.getHealth();
            }

            entity.setRaid((Raid)null);
            this.updateBar();
            this.markDirty();
         }
      }

   }

   private void markDirty() {
      this.world.getRaidManager().markDirty();
   }

   public static ItemStack getOminousBanner() {
      ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
      CompoundTag compoundTag = itemStack.getOrCreateSubTag("BlockEntityTag");
      ListTag listTag = (new BannerPattern.Patterns()).add(BannerPattern.RHOMBUS_MIDDLE, DyeColor.CYAN).add(BannerPattern.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).add(BannerPattern.STRIPE_CENTER, DyeColor.GRAY).add(BannerPattern.BORDER, DyeColor.LIGHT_GRAY).add(BannerPattern.STRIPE_MIDDLE, DyeColor.BLACK).add(BannerPattern.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).add(BannerPattern.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY).add(BannerPattern.BORDER, DyeColor.BLACK).toTag();
      compoundTag.put("Patterns", listTag);
      itemStack.addHideFlag(ItemStack.TooltipSection.ADDITIONAL);
      itemStack.setCustomName((new TranslatableText("block.minecraft.ominous_banner")).formatted(Formatting.GOLD));
      return itemStack;
   }

   @Nullable
   public RaiderEntity getCaptain(int wave) {
      return (RaiderEntity)this.waveToCaptain.get(wave);
   }

   @Nullable
   private BlockPos getRavagerSpawnLocation(int proximity, int tries) {
      int i = proximity == 0 ? 2 : 2 - proximity;
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      for(int j = 0; j < tries; ++j) {
         float f = this.world.random.nextFloat() * 6.2831855F;
         int k = this.center.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0F * (float)i) + this.world.random.nextInt(5);
         int l = this.center.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0F * (float)i) + this.world.random.nextInt(5);
         int m = this.world.getTopY(Heightmap.Type.WORLD_SURFACE, k, l);
         mutable.set(k, m, l);
         if ((!this.world.isNearOccupiedPointOfInterest((BlockPos)mutable) || proximity >= 2) && this.world.isRegionLoaded(mutable.getX() - 10, mutable.getY() - 10, mutable.getZ() - 10, mutable.getX() + 10, mutable.getY() + 10, mutable.getZ() + 10) && this.world.getChunkManager().shouldTickChunk(new ChunkPos(mutable)) && (SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, this.world, mutable, EntityType.RAVAGER) || this.world.getBlockState(mutable.down()).isOf(Blocks.SNOW) && this.world.getBlockState(mutable).isAir())) {
            return mutable;
         }
      }

      return null;
   }

   private boolean addToWave(int wave, RaiderEntity entity) {
      return this.addToWave(wave, entity, true);
   }

   public boolean addToWave(int wave, RaiderEntity entity, boolean countHealth) {
      this.waveToRaiders.computeIfAbsent(wave, (integer) -> {
         return Sets.newHashSet();
      });
      Set<RaiderEntity> set = (Set)this.waveToRaiders.get(wave);
      RaiderEntity raiderEntity = null;
      Iterator var6 = set.iterator();

      while(var6.hasNext()) {
         RaiderEntity raiderEntity2 = (RaiderEntity)var6.next();
         if (raiderEntity2.getUuid().equals(entity.getUuid())) {
            raiderEntity = raiderEntity2;
            break;
         }
      }

      if (raiderEntity != null) {
         set.remove(raiderEntity);
         set.add(entity);
      }

      set.add(entity);
      if (countHealth) {
         this.totalHealth += entity.getHealth();
      }

      this.updateBar();
      this.markDirty();
      return true;
   }

   public void setWaveCaptain(int wave, RaiderEntity entity) {
      this.waveToCaptain.put(wave, entity);
      entity.equipStack(EquipmentSlot.HEAD, getOminousBanner());
      entity.setEquipmentDropChance(EquipmentSlot.HEAD, 2.0F);
   }

   public void removeLeader(int wave) {
      this.waveToCaptain.remove(wave);
   }

   public BlockPos getCenter() {
      return this.center;
   }

   private void setCenter(BlockPos center) {
      this.center = center;
   }

   public int getRaidId() {
      return this.id;
   }

   private int getCount(Raid.Member member, int wave, boolean extra) {
      return extra ? member.countInWave[this.waveCount] : member.countInWave[wave];
   }

   private int getBonusCount(Raid.Member member, Random random, int wave, LocalDifficulty localDifficulty, boolean extra) {
      Difficulty difficulty = localDifficulty.getGlobalDifficulty();
      boolean bl = difficulty == Difficulty.EASY;
      boolean bl2 = difficulty == Difficulty.NORMAL;
      int n;
      switch(member) {
      case WITCH:
         if (!bl && wave > 2 && wave != 4) {
            n = 1;
            break;
         }

         return 0;
      case PILLAGER:
      case VINDICATOR:
         if (bl) {
            n = random.nextInt(2);
         } else if (bl2) {
            n = 1;
         } else {
            n = 2;
         }
         break;
      case RAVAGER:
         n = !bl && extra ? 1 : 0;
         break;
      default:
         return 0;
      }

      return n > 0 ? random.nextInt(n + 1) : 0;
   }

   public boolean isActive() {
      return this.active;
   }

   public CompoundTag toTag(CompoundTag tag) {
      tag.putInt("Id", this.id);
      tag.putBoolean("Started", this.started);
      tag.putBoolean("Active", this.active);
      tag.putLong("TicksActive", this.ticksActive);
      tag.putInt("BadOmenLevel", this.badOmenLevel);
      tag.putInt("GroupsSpawned", this.wavesSpawned);
      tag.putInt("PreRaidTicks", this.preRaidTicks);
      tag.putInt("PostRaidTicks", this.postRaidTicks);
      tag.putFloat("TotalHealth", this.totalHealth);
      tag.putInt("NumGroups", this.waveCount);
      tag.putString("Status", this.status.getName());
      tag.putInt("CX", this.center.getX());
      tag.putInt("CY", this.center.getY());
      tag.putInt("CZ", this.center.getZ());
      ListTag listTag = new ListTag();
      Iterator var3 = this.heroesOfTheVillage.iterator();

      while(var3.hasNext()) {
         UUID uUID = (UUID)var3.next();
         listTag.add(NbtHelper.fromUuid(uUID));
      }

      tag.put("HeroesOfTheVillage", listTag);
      return tag;
   }

   public int getMaxWaves(Difficulty difficulty) {
      switch(difficulty) {
      case EASY:
         return 3;
      case NORMAL:
         return 5;
      case HARD:
         return 7;
      default:
         return 0;
      }
   }

   public float getEnchantmentChance() {
      int i = this.getBadOmenLevel();
      if (i == 2) {
         return 0.1F;
      } else if (i == 3) {
         return 0.25F;
      } else if (i == 4) {
         return 0.5F;
      } else {
         return i == 5 ? 0.75F : 0.0F;
      }
   }

   public void addHero(Entity entity) {
      this.heroesOfTheVillage.add(entity.getUuid());
   }

   static {
      VICTORY_TITLE = EVENT_TEXT.shallowCopy().append(" - ").append(VICTORY_SUFFIX_TEXT);
      DEFEAT_TITLE = EVENT_TEXT.shallowCopy().append(" - ").append(DEFEAT_SUFFIX_TEXT);
   }

   static enum Member {
      VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
      EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
      PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
      WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
      RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

      private static final Raid.Member[] VALUES = values();
      private final EntityType<? extends RaiderEntity> type;
      private final int[] countInWave;

      private Member(EntityType<? extends RaiderEntity> type, int[] countInWave) {
         this.type = type;
         this.countInWave = countInWave;
      }
   }

   static enum Status {
      ONGOING,
      VICTORY,
      LOSS,
      STOPPED;

      private static final Raid.Status[] VALUES = values();

      private static Raid.Status fromName(String string) {
         Raid.Status[] var1 = VALUES;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Raid.Status status = var1[var3];
            if (string.equalsIgnoreCase(status.name())) {
               return status;
            }
         }

         return ONGOING;
      }

      public String getName() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }
}
