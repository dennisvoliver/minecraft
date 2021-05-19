package net.minecraft.entity;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class EntityType<T extends Entity> {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final EntityType<AreaEffectCloudEntity> AREA_EFFECT_CLOUD;
   public static final EntityType<ArmorStandEntity> ARMOR_STAND;
   public static final EntityType<ArrowEntity> ARROW;
   public static final EntityType<BatEntity> BAT;
   public static final EntityType<BeeEntity> BEE;
   public static final EntityType<BlazeEntity> BLAZE;
   public static final EntityType<BoatEntity> BOAT;
   public static final EntityType<CatEntity> CAT;
   public static final EntityType<CaveSpiderEntity> CAVE_SPIDER;
   public static final EntityType<ChickenEntity> CHICKEN;
   public static final EntityType<CodEntity> COD;
   public static final EntityType<CowEntity> COW;
   public static final EntityType<CreeperEntity> CREEPER;
   public static final EntityType<DolphinEntity> DOLPHIN;
   public static final EntityType<DonkeyEntity> DONKEY;
   public static final EntityType<DragonFireballEntity> DRAGON_FIREBALL;
   public static final EntityType<DrownedEntity> DROWNED;
   public static final EntityType<ElderGuardianEntity> ELDER_GUARDIAN;
   public static final EntityType<EndCrystalEntity> END_CRYSTAL;
   public static final EntityType<EnderDragonEntity> ENDER_DRAGON;
   public static final EntityType<EndermanEntity> ENDERMAN;
   public static final EntityType<EndermiteEntity> ENDERMITE;
   public static final EntityType<EvokerEntity> EVOKER;
   public static final EntityType<EvokerFangsEntity> EVOKER_FANGS;
   public static final EntityType<ExperienceOrbEntity> EXPERIENCE_ORB;
   public static final EntityType<EyeOfEnderEntity> EYE_OF_ENDER;
   public static final EntityType<FallingBlockEntity> FALLING_BLOCK;
   public static final EntityType<FireworkRocketEntity> FIREWORK_ROCKET;
   public static final EntityType<FoxEntity> FOX;
   public static final EntityType<GhastEntity> GHAST;
   public static final EntityType<GiantEntity> GIANT;
   public static final EntityType<GuardianEntity> GUARDIAN;
   public static final EntityType<HoglinEntity> HOGLIN;
   public static final EntityType<HorseEntity> HORSE;
   public static final EntityType<HuskEntity> HUSK;
   public static final EntityType<IllusionerEntity> ILLUSIONER;
   public static final EntityType<IronGolemEntity> IRON_GOLEM;
   public static final EntityType<ItemEntity> ITEM;
   public static final EntityType<ItemFrameEntity> ITEM_FRAME;
   public static final EntityType<FireballEntity> FIREBALL;
   public static final EntityType<LeashKnotEntity> LEASH_KNOT;
   public static final EntityType<LightningEntity> LIGHTNING_BOLT;
   public static final EntityType<LlamaEntity> LLAMA;
   public static final EntityType<LlamaSpitEntity> LLAMA_SPIT;
   public static final EntityType<MagmaCubeEntity> MAGMA_CUBE;
   public static final EntityType<MinecartEntity> MINECART;
   public static final EntityType<ChestMinecartEntity> CHEST_MINECART;
   public static final EntityType<CommandBlockMinecartEntity> COMMAND_BLOCK_MINECART;
   public static final EntityType<FurnaceMinecartEntity> FURNACE_MINECART;
   public static final EntityType<HopperMinecartEntity> HOPPER_MINECART;
   public static final EntityType<SpawnerMinecartEntity> SPAWNER_MINECART;
   public static final EntityType<TntMinecartEntity> TNT_MINECART;
   public static final EntityType<MuleEntity> MULE;
   public static final EntityType<MooshroomEntity> MOOSHROOM;
   public static final EntityType<OcelotEntity> OCELOT;
   public static final EntityType<PaintingEntity> PAINTING;
   public static final EntityType<PandaEntity> PANDA;
   public static final EntityType<ParrotEntity> PARROT;
   public static final EntityType<PhantomEntity> PHANTOM;
   public static final EntityType<PigEntity> PIG;
   public static final EntityType<PiglinEntity> PIGLIN;
   public static final EntityType<PiglinBruteEntity> PIGLIN_BRUTE;
   public static final EntityType<PillagerEntity> PILLAGER;
   public static final EntityType<PolarBearEntity> POLAR_BEAR;
   public static final EntityType<TntEntity> TNT;
   public static final EntityType<PufferfishEntity> PUFFERFISH;
   public static final EntityType<RabbitEntity> RABBIT;
   public static final EntityType<RavagerEntity> RAVAGER;
   public static final EntityType<SalmonEntity> SALMON;
   public static final EntityType<SheepEntity> SHEEP;
   public static final EntityType<ShulkerEntity> SHULKER;
   public static final EntityType<ShulkerBulletEntity> SHULKER_BULLET;
   public static final EntityType<SilverfishEntity> SILVERFISH;
   public static final EntityType<SkeletonEntity> SKELETON;
   public static final EntityType<SkeletonHorseEntity> SKELETON_HORSE;
   public static final EntityType<SlimeEntity> SLIME;
   public static final EntityType<SmallFireballEntity> SMALL_FIREBALL;
   public static final EntityType<SnowGolemEntity> SNOW_GOLEM;
   public static final EntityType<SnowballEntity> SNOWBALL;
   public static final EntityType<SpectralArrowEntity> SPECTRAL_ARROW;
   public static final EntityType<SpiderEntity> SPIDER;
   public static final EntityType<SquidEntity> SQUID;
   public static final EntityType<StrayEntity> STRAY;
   public static final EntityType<StriderEntity> STRIDER;
   public static final EntityType<EggEntity> EGG;
   public static final EntityType<EnderPearlEntity> ENDER_PEARL;
   public static final EntityType<ExperienceBottleEntity> EXPERIENCE_BOTTLE;
   public static final EntityType<PotionEntity> POTION;
   public static final EntityType<TridentEntity> TRIDENT;
   public static final EntityType<TraderLlamaEntity> TRADER_LLAMA;
   public static final EntityType<TropicalFishEntity> TROPICAL_FISH;
   public static final EntityType<TurtleEntity> TURTLE;
   public static final EntityType<VexEntity> VEX;
   public static final EntityType<VillagerEntity> VILLAGER;
   public static final EntityType<VindicatorEntity> VINDICATOR;
   public static final EntityType<WanderingTraderEntity> WANDERING_TRADER;
   public static final EntityType<WitchEntity> WITCH;
   public static final EntityType<WitherEntity> WITHER;
   public static final EntityType<WitherSkeletonEntity> WITHER_SKELETON;
   public static final EntityType<WitherSkullEntity> WITHER_SKULL;
   public static final EntityType<WolfEntity> WOLF;
   public static final EntityType<ZoglinEntity> ZOGLIN;
   public static final EntityType<ZombieEntity> ZOMBIE;
   public static final EntityType<ZombieHorseEntity> ZOMBIE_HORSE;
   public static final EntityType<ZombieVillagerEntity> ZOMBIE_VILLAGER;
   public static final EntityType<ZombifiedPiglinEntity> ZOMBIFIED_PIGLIN;
   public static final EntityType<PlayerEntity> PLAYER;
   public static final EntityType<FishingBobberEntity> FISHING_BOBBER;
   private final EntityType.EntityFactory<T> factory;
   private final SpawnGroup spawnGroup;
   private final ImmutableSet<Block> canSpawnInside;
   private final boolean saveable;
   private final boolean summonable;
   private final boolean fireImmune;
   private final boolean spawnableFarFromPlayer;
   private final int maxTrackDistance;
   private final int trackTickInterval;
   @Nullable
   private String translationKey;
   @Nullable
   private Text name;
   @Nullable
   private Identifier lootTableId;
   private final EntityDimensions dimensions;

   private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
      return (EntityType)Registry.register(Registry.ENTITY_TYPE, (String)id, type.build(id));
   }

   public static Identifier getId(EntityType<?> type) {
      return Registry.ENTITY_TYPE.getId(type);
   }

   public static Optional<EntityType<?>> get(String id) {
      return Registry.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(id));
   }

   public EntityType(EntityType.EntityFactory<T> factory, SpawnGroup spawnGroup, boolean saveable, boolean summonable, boolean fireImmune, boolean spawnableFarFromPlayer, ImmutableSet<Block> immutableSet, EntityDimensions entityDimensions, int i, int j) {
      this.factory = factory;
      this.spawnGroup = spawnGroup;
      this.spawnableFarFromPlayer = spawnableFarFromPlayer;
      this.saveable = saveable;
      this.summonable = summonable;
      this.fireImmune = fireImmune;
      this.canSpawnInside = immutableSet;
      this.dimensions = entityDimensions;
      this.maxTrackDistance = i;
      this.trackTickInterval = j;
   }

   @Nullable
   public Entity spawnFromItemStack(ServerWorld serverWorld, @Nullable ItemStack stack, @Nullable PlayerEntity player, BlockPos pos, SpawnReason spawnReason, boolean alignPosition, boolean invertY) {
      return this.spawn(serverWorld, stack == null ? null : stack.getTag(), stack != null && stack.hasCustomName() ? stack.getName() : null, player, pos, spawnReason, alignPosition, invertY);
   }

   @Nullable
   public T spawn(ServerWorld serverWorld, @Nullable CompoundTag itemTag, @Nullable Text name, @Nullable PlayerEntity player, BlockPos pos, SpawnReason spawnReason, boolean alignPosition, boolean invertY) {
      T entity = this.create(serverWorld, itemTag, name, player, pos, spawnReason, alignPosition, invertY);
      if (entity != null) {
         serverWorld.spawnEntityAndPassengers(entity);
      }

      return entity;
   }

   @Nullable
   public T create(ServerWorld serverWorld, @Nullable CompoundTag itemTag, @Nullable Text name, @Nullable PlayerEntity player, BlockPos pos, SpawnReason spawnReason, boolean alignPosition, boolean invertY) {
      T entity = this.create(serverWorld);
      if (entity == null) {
         return null;
      } else {
         double e;
         if (alignPosition) {
            entity.updatePosition((double)pos.getX() + 0.5D, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5D);
            e = getOriginY(serverWorld, pos, invertY, entity.getBoundingBox());
         } else {
            e = 0.0D;
         }

         entity.refreshPositionAndAngles((double)pos.getX() + 0.5D, (double)pos.getY() + e, (double)pos.getZ() + 0.5D, MathHelper.wrapDegrees(serverWorld.random.nextFloat() * 360.0F), 0.0F);
         if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity)entity;
            mobEntity.headYaw = mobEntity.yaw;
            mobEntity.bodyYaw = mobEntity.yaw;
            mobEntity.initialize(serverWorld, serverWorld.getLocalDifficulty(mobEntity.getBlockPos()), spawnReason, (EntityData)null, itemTag);
            mobEntity.playAmbientSound();
         }

         if (name != null && entity instanceof LivingEntity) {
            entity.setCustomName(name);
         }

         loadFromEntityTag(serverWorld, player, entity, itemTag);
         return entity;
      }
   }

   protected static double getOriginY(WorldView world, BlockPos pos, boolean invertY, Box boundingBox) {
      Box box = new Box(pos);
      if (invertY) {
         box = box.stretch(0.0D, -1.0D, 0.0D);
      }

      Stream<VoxelShape> stream = world.getCollisions((Entity)null, box, (entity) -> {
         return true;
      });
      return 1.0D + VoxelShapes.calculateMaxOffset(Direction.Axis.Y, boundingBox, stream, invertY ? -2.0D : -1.0D);
   }

   public static void loadFromEntityTag(World world, @Nullable PlayerEntity player, @Nullable Entity entity, @Nullable CompoundTag itemTag) {
      if (itemTag != null && itemTag.contains("EntityTag", 10)) {
         MinecraftServer minecraftServer = world.getServer();
         if (minecraftServer != null && entity != null) {
            if (world.isClient || !entity.entityDataRequiresOperator() || player != null && minecraftServer.getPlayerManager().isOperator(player.getGameProfile())) {
               CompoundTag compoundTag = entity.toTag(new CompoundTag());
               UUID uUID = entity.getUuid();
               compoundTag.copyFrom(itemTag.getCompound("EntityTag"));
               entity.setUuid(uUID);
               entity.fromTag(compoundTag);
            }
         }
      }
   }

   public boolean isSaveable() {
      return this.saveable;
   }

   public boolean isSummonable() {
      return this.summonable;
   }

   public boolean isFireImmune() {
      return this.fireImmune;
   }

   public boolean isSpawnableFarFromPlayer() {
      return this.spawnableFarFromPlayer;
   }

   public SpawnGroup getSpawnGroup() {
      return this.spawnGroup;
   }

   public String getTranslationKey() {
      if (this.translationKey == null) {
         this.translationKey = Util.createTranslationKey("entity", Registry.ENTITY_TYPE.getId(this));
      }

      return this.translationKey;
   }

   public Text getName() {
      if (this.name == null) {
         this.name = new TranslatableText(this.getTranslationKey());
      }

      return this.name;
   }

   public String toString() {
      return this.getTranslationKey();
   }

   public Identifier getLootTableId() {
      if (this.lootTableId == null) {
         Identifier identifier = Registry.ENTITY_TYPE.getId(this);
         this.lootTableId = new Identifier(identifier.getNamespace(), "entities/" + identifier.getPath());
      }

      return this.lootTableId;
   }

   public float getWidth() {
      return this.dimensions.width;
   }

   public float getHeight() {
      return this.dimensions.height;
   }

   @Nullable
   public T create(World world) {
      return this.factory.create(this, world);
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public static Entity createInstanceFromId(int type, World world) {
      return newInstance(world, (EntityType)Registry.ENTITY_TYPE.get(type));
   }

   public static Optional<Entity> getEntityFromTag(CompoundTag tag, World world) {
      return Util.ifPresentOrElse(fromTag(tag).map((entityType) -> {
         return entityType.create(world);
      }), (entity) -> {
         entity.fromTag(tag);
      }, () -> {
         LOGGER.warn((String)"Skipping Entity with id {}", (Object)tag.getString("id"));
      });
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   private static Entity newInstance(World world, @Nullable EntityType<?> type) {
      return type == null ? null : type.create(world);
   }

   public Box createSimpleBoundingBox(double feetX, double feetY, double feetZ) {
      float f = this.getWidth() / 2.0F;
      return new Box(feetX - (double)f, feetY, feetZ - (double)f, feetX + (double)f, feetY + (double)this.getHeight(), feetZ + (double)f);
   }

   /**
    * Returns whether the EntityType can spawn inside the given block.
    * 
    * <p>By default, non-fire-immune mobs can't spawn in/on blocks dealing fire damage.
    * Any mob can't spawn in wither roses, sweet berry bush, or cacti.
    * 
    * <p>This can be overwritten via {@link EntityType.Builder#allowSpawningInside(Block[])}
    */
   public boolean isInvalidSpawn(BlockState blockState) {
      if (this.canSpawnInside.contains(blockState.getBlock())) {
         return false;
      } else if (this.fireImmune || !blockState.isIn(BlockTags.FIRE) && !blockState.isOf(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockState) && !blockState.isOf(Blocks.LAVA)) {
         return blockState.isOf(Blocks.WITHER_ROSE) || blockState.isOf(Blocks.SWEET_BERRY_BUSH) || blockState.isOf(Blocks.CACTUS);
      } else {
         return true;
      }
   }

   public EntityDimensions getDimensions() {
      return this.dimensions;
   }

   public static Optional<EntityType<?>> fromTag(CompoundTag compoundTag) {
      return Registry.ENTITY_TYPE.getOrEmpty(new Identifier(compoundTag.getString("id")));
   }

   @Nullable
   public static Entity loadEntityWithPassengers(CompoundTag compoundTag, World world, Function<Entity, Entity> entityProcessor) {
      return (Entity)loadEntityFromTag(compoundTag, world).map(entityProcessor).map((entity) -> {
         if (compoundTag.contains("Passengers", 9)) {
            ListTag listTag = compoundTag.getList("Passengers", 10);

            for(int i = 0; i < listTag.size(); ++i) {
               Entity entity2 = loadEntityWithPassengers(listTag.getCompound(i), world, entityProcessor);
               if (entity2 != null) {
                  entity2.startRiding(entity, true);
               }
            }
         }

         return entity;
      }).orElse((Object)null);
   }

   private static Optional<Entity> loadEntityFromTag(CompoundTag compoundTag, World world) {
      try {
         return getEntityFromTag(compoundTag, world);
      } catch (RuntimeException var3) {
         LOGGER.warn((String)"Exception loading entity: ", (Throwable)var3);
         return Optional.empty();
      }
   }

   /**
    * Returns the tracking distance, <b>in chunks</b>, of this type of entity
    * for clients. This will be then modified by the server's tracking
    * distance multiplier.
    */
   public int getMaxTrackDistance() {
      return this.maxTrackDistance;
   }

   public int getTrackTickInterval() {
      return this.trackTickInterval;
   }

   public boolean alwaysUpdateVelocity() {
      return this != PLAYER && this != LLAMA_SPIT && this != WITHER && this != BAT && this != ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != END_CRYSTAL && this != EVOKER_FANGS;
   }

   public boolean isIn(Tag<EntityType<?>> tag) {
      return tag.contains(this);
   }

   static {
      AREA_EFFECT_CLOUD = register("area_effect_cloud", EntityType.Builder.create(AreaEffectCloudEntity::new, SpawnGroup.MISC).makeFireImmune().setDimensions(6.0F, 0.5F).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE));
      ARMOR_STAND = register("armor_stand", EntityType.Builder.create(ArmorStandEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 1.975F).maxTrackingRange(10));
      ARROW = register("arrow", EntityType.Builder.create(ArrowEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20));
      BAT = register("bat", EntityType.Builder.create(BatEntity::new, SpawnGroup.AMBIENT).setDimensions(0.5F, 0.9F).maxTrackingRange(5));
      BEE = register("bee", EntityType.Builder.create(BeeEntity::new, SpawnGroup.CREATURE).setDimensions(0.7F, 0.6F).maxTrackingRange(8));
      BLAZE = register("blaze", EntityType.Builder.create(BlazeEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(0.6F, 1.8F).maxTrackingRange(8));
      BOAT = register("boat", EntityType.Builder.create(BoatEntity::new, SpawnGroup.MISC).setDimensions(1.375F, 0.5625F).maxTrackingRange(10));
      CAT = register("cat", EntityType.Builder.create(CatEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 0.7F).maxTrackingRange(8));
      CAVE_SPIDER = register("cave_spider", EntityType.Builder.create(CaveSpiderEntity::new, SpawnGroup.MONSTER).setDimensions(0.7F, 0.5F).maxTrackingRange(8));
      CHICKEN = register("chicken", EntityType.Builder.create(ChickenEntity::new, SpawnGroup.CREATURE).setDimensions(0.4F, 0.7F).maxTrackingRange(10));
      COD = register("cod", EntityType.Builder.create(CodEntity::new, SpawnGroup.WATER_AMBIENT).setDimensions(0.5F, 0.3F).maxTrackingRange(4));
      COW = register("cow", EntityType.Builder.create(CowEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.4F).maxTrackingRange(10));
      CREEPER = register("creeper", EntityType.Builder.create(CreeperEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.7F).maxTrackingRange(8));
      DOLPHIN = register("dolphin", EntityType.Builder.create(DolphinEntity::new, SpawnGroup.WATER_CREATURE).setDimensions(0.9F, 0.6F));
      DONKEY = register("donkey", EntityType.Builder.create(DonkeyEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.5F).maxTrackingRange(10));
      DRAGON_FIREBALL = register("dragon_fireball", EntityType.Builder.create(DragonFireballEntity::new, SpawnGroup.MISC).setDimensions(1.0F, 1.0F).maxTrackingRange(4).trackingTickInterval(10));
      DROWNED = register("drowned", EntityType.Builder.create(DrownedEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      ELDER_GUARDIAN = register("elder_guardian", EntityType.Builder.create(ElderGuardianEntity::new, SpawnGroup.MONSTER).setDimensions(1.9975F, 1.9975F).maxTrackingRange(10));
      END_CRYSTAL = register("end_crystal", EntityType.Builder.create(EndCrystalEntity::new, SpawnGroup.MISC).setDimensions(2.0F, 2.0F).maxTrackingRange(16).trackingTickInterval(Integer.MAX_VALUE));
      ENDER_DRAGON = register("ender_dragon", EntityType.Builder.create(EnderDragonEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(16.0F, 8.0F).maxTrackingRange(10));
      ENDERMAN = register("enderman", EntityType.Builder.create(EndermanEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 2.9F).maxTrackingRange(8));
      ENDERMITE = register("endermite", EntityType.Builder.create(EndermiteEntity::new, SpawnGroup.MONSTER).setDimensions(0.4F, 0.3F).maxTrackingRange(8));
      EVOKER = register("evoker", EntityType.Builder.create(EvokerEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      EVOKER_FANGS = register("evoker_fangs", EntityType.Builder.create(EvokerFangsEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.8F).maxTrackingRange(6).trackingTickInterval(2));
      EXPERIENCE_ORB = register("experience_orb", EntityType.Builder.create(ExperienceOrbEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(6).trackingTickInterval(20));
      EYE_OF_ENDER = register("eye_of_ender", EntityType.Builder.create(EyeOfEnderEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(4));
      FALLING_BLOCK = register("falling_block", EntityType.Builder.create(FallingBlockEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.98F).maxTrackingRange(10).trackingTickInterval(20));
      FIREWORK_ROCKET = register("firework_rocket", EntityType.Builder.create(FireworkRocketEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      FOX = register("fox", EntityType.Builder.create(FoxEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 0.7F).maxTrackingRange(8).allowSpawningInside(Blocks.SWEET_BERRY_BUSH));
      GHAST = register("ghast", EntityType.Builder.create(GhastEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(4.0F, 4.0F).maxTrackingRange(10));
      GIANT = register("giant", EntityType.Builder.create(GiantEntity::new, SpawnGroup.MONSTER).setDimensions(3.6F, 12.0F).maxTrackingRange(10));
      GUARDIAN = register("guardian", EntityType.Builder.create(GuardianEntity::new, SpawnGroup.MONSTER).setDimensions(0.85F, 0.85F).maxTrackingRange(8));
      HOGLIN = register("hoglin", EntityType.Builder.create(HoglinEntity::new, SpawnGroup.MONSTER).setDimensions(1.3964844F, 1.4F).maxTrackingRange(8));
      HORSE = register("horse", EntityType.Builder.create(HorseEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.6F).maxTrackingRange(10));
      HUSK = register("husk", EntityType.Builder.create(HuskEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      ILLUSIONER = register("illusioner", EntityType.Builder.create(IllusionerEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      IRON_GOLEM = register("iron_golem", EntityType.Builder.create(IronGolemEntity::new, SpawnGroup.MISC).setDimensions(1.4F, 2.7F).maxTrackingRange(10));
      ITEM = register("item", EntityType.Builder.create(ItemEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(6).trackingTickInterval(20));
      ITEM_FRAME = register("item_frame", EntityType.Builder.create(ItemFrameEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE));
      FIREBALL = register("fireball", EntityType.Builder.create(FireballEntity::new, SpawnGroup.MISC).setDimensions(1.0F, 1.0F).maxTrackingRange(4).trackingTickInterval(10));
      LEASH_KNOT = register("leash_knot", EntityType.Builder.create(LeashKnotEntity::new, SpawnGroup.MISC).disableSaving().setDimensions(0.5F, 0.5F).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE));
      LIGHTNING_BOLT = register("lightning_bolt", EntityType.Builder.create(LightningEntity::new, SpawnGroup.MISC).disableSaving().setDimensions(0.0F, 0.0F).maxTrackingRange(16).trackingTickInterval(Integer.MAX_VALUE));
      LLAMA = register("llama", EntityType.Builder.create(LlamaEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.87F).maxTrackingRange(10));
      LLAMA_SPIT = register("llama_spit", EntityType.Builder.create(LlamaSpitEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      MAGMA_CUBE = register("magma_cube", EntityType.Builder.create(MagmaCubeEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(2.04F, 2.04F).maxTrackingRange(8));
      MINECART = register("minecart", EntityType.Builder.create(MinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      CHEST_MINECART = register("chest_minecart", EntityType.Builder.create(ChestMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      COMMAND_BLOCK_MINECART = register("command_block_minecart", EntityType.Builder.create(CommandBlockMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      FURNACE_MINECART = register("furnace_minecart", EntityType.Builder.create(FurnaceMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      HOPPER_MINECART = register("hopper_minecart", EntityType.Builder.create(HopperMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      SPAWNER_MINECART = register("spawner_minecart", EntityType.Builder.create(SpawnerMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      TNT_MINECART = register("tnt_minecart", EntityType.Builder.create(TntMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      MULE = register("mule", EntityType.Builder.create(MuleEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.6F).maxTrackingRange(8));
      MOOSHROOM = register("mooshroom", EntityType.Builder.create(MooshroomEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.4F).maxTrackingRange(10));
      OCELOT = register("ocelot", EntityType.Builder.create(OcelotEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 0.7F).maxTrackingRange(10));
      PAINTING = register("painting", EntityType.Builder.create(PaintingEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE));
      PANDA = register("panda", EntityType.Builder.create(PandaEntity::new, SpawnGroup.CREATURE).setDimensions(1.3F, 1.25F).maxTrackingRange(10));
      PARROT = register("parrot", EntityType.Builder.create(ParrotEntity::new, SpawnGroup.CREATURE).setDimensions(0.5F, 0.9F).maxTrackingRange(8));
      PHANTOM = register("phantom", EntityType.Builder.create(PhantomEntity::new, SpawnGroup.MONSTER).setDimensions(0.9F, 0.5F).maxTrackingRange(8));
      PIG = register("pig", EntityType.Builder.create(PigEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 0.9F).maxTrackingRange(10));
      PIGLIN = register("piglin", EntityType.Builder.create(PiglinEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      PIGLIN_BRUTE = register("piglin_brute", EntityType.Builder.create(PiglinBruteEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      PILLAGER = register("pillager", EntityType.Builder.create(PillagerEntity::new, SpawnGroup.MONSTER).spawnableFarFromPlayer().setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      POLAR_BEAR = register("polar_bear", EntityType.Builder.create(PolarBearEntity::new, SpawnGroup.CREATURE).setDimensions(1.4F, 1.4F).maxTrackingRange(10));
      TNT = register("tnt", EntityType.Builder.create(TntEntity::new, SpawnGroup.MISC).makeFireImmune().setDimensions(0.98F, 0.98F).maxTrackingRange(10).trackingTickInterval(10));
      PUFFERFISH = register("pufferfish", EntityType.Builder.create(PufferfishEntity::new, SpawnGroup.WATER_AMBIENT).setDimensions(0.7F, 0.7F).maxTrackingRange(4));
      RABBIT = register("rabbit", EntityType.Builder.create(RabbitEntity::new, SpawnGroup.CREATURE).setDimensions(0.4F, 0.5F).maxTrackingRange(8));
      RAVAGER = register("ravager", EntityType.Builder.create(RavagerEntity::new, SpawnGroup.MONSTER).setDimensions(1.95F, 2.2F).maxTrackingRange(10));
      SALMON = register("salmon", EntityType.Builder.create(SalmonEntity::new, SpawnGroup.WATER_AMBIENT).setDimensions(0.7F, 0.4F).maxTrackingRange(4));
      SHEEP = register("sheep", EntityType.Builder.create(SheepEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.3F).maxTrackingRange(10));
      SHULKER = register("shulker", EntityType.Builder.create(ShulkerEntity::new, SpawnGroup.MONSTER).makeFireImmune().spawnableFarFromPlayer().setDimensions(1.0F, 1.0F).maxTrackingRange(10));
      SHULKER_BULLET = register("shulker_bullet", EntityType.Builder.create(ShulkerBulletEntity::new, SpawnGroup.MISC).setDimensions(0.3125F, 0.3125F).maxTrackingRange(8));
      SILVERFISH = register("silverfish", EntityType.Builder.create(SilverfishEntity::new, SpawnGroup.MONSTER).setDimensions(0.4F, 0.3F).maxTrackingRange(8));
      SKELETON = register("skeleton", EntityType.Builder.create(SkeletonEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.99F).maxTrackingRange(8));
      SKELETON_HORSE = register("skeleton_horse", EntityType.Builder.create(SkeletonHorseEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.6F).maxTrackingRange(10));
      SLIME = register("slime", EntityType.Builder.create(SlimeEntity::new, SpawnGroup.MONSTER).setDimensions(2.04F, 2.04F).maxTrackingRange(10));
      SMALL_FIREBALL = register("small_fireball", EntityType.Builder.create(SmallFireballEntity::new, SpawnGroup.MISC).setDimensions(0.3125F, 0.3125F).maxTrackingRange(4).trackingTickInterval(10));
      SNOW_GOLEM = register("snow_golem", EntityType.Builder.create(SnowGolemEntity::new, SpawnGroup.MISC).setDimensions(0.7F, 1.9F).maxTrackingRange(8));
      SNOWBALL = register("snowball", EntityType.Builder.create(SnowballEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      SPECTRAL_ARROW = register("spectral_arrow", EntityType.Builder.create(SpectralArrowEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20));
      SPIDER = register("spider", EntityType.Builder.create(SpiderEntity::new, SpawnGroup.MONSTER).setDimensions(1.4F, 0.9F).maxTrackingRange(8));
      SQUID = register("squid", EntityType.Builder.create(SquidEntity::new, SpawnGroup.WATER_CREATURE).setDimensions(0.8F, 0.8F).maxTrackingRange(8));
      STRAY = register("stray", EntityType.Builder.create(StrayEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.99F).maxTrackingRange(8));
      STRIDER = register("strider", EntityType.Builder.create(StriderEntity::new, SpawnGroup.CREATURE).makeFireImmune().setDimensions(0.9F, 1.7F).maxTrackingRange(10));
      EGG = register("egg", EntityType.Builder.create(EggEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      ENDER_PEARL = register("ender_pearl", EntityType.Builder.create(EnderPearlEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      EXPERIENCE_BOTTLE = register("experience_bottle", EntityType.Builder.create(ExperienceBottleEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      POTION = register("potion", EntityType.Builder.create(PotionEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      TRIDENT = register("trident", EntityType.Builder.create(TridentEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20));
      TRADER_LLAMA = register("trader_llama", EntityType.Builder.create(TraderLlamaEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.87F).maxTrackingRange(10));
      TROPICAL_FISH = register("tropical_fish", EntityType.Builder.create(TropicalFishEntity::new, SpawnGroup.WATER_AMBIENT).setDimensions(0.5F, 0.4F).maxTrackingRange(4));
      TURTLE = register("turtle", EntityType.Builder.create(TurtleEntity::new, SpawnGroup.CREATURE).setDimensions(1.2F, 0.4F).maxTrackingRange(10));
      VEX = register("vex", EntityType.Builder.create(VexEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(0.4F, 0.8F).maxTrackingRange(8));
      VILLAGER = register("villager", EntityType.Builder.create(VillagerEntity::new, SpawnGroup.MISC).setDimensions(0.6F, 1.95F).maxTrackingRange(10));
      VINDICATOR = register("vindicator", EntityType.Builder.create(VindicatorEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      WANDERING_TRADER = register("wandering_trader", EntityType.Builder.create(WanderingTraderEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 1.95F).maxTrackingRange(10));
      WITCH = register("witch", EntityType.Builder.create(WitchEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      WITHER = register("wither", EntityType.Builder.create(WitherEntity::new, SpawnGroup.MONSTER).makeFireImmune().allowSpawningInside(Blocks.WITHER_ROSE).setDimensions(0.9F, 3.5F).maxTrackingRange(10));
      WITHER_SKELETON = register("wither_skeleton", EntityType.Builder.create(WitherSkeletonEntity::new, SpawnGroup.MONSTER).makeFireImmune().allowSpawningInside(Blocks.WITHER_ROSE).setDimensions(0.7F, 2.4F).maxTrackingRange(8));
      WITHER_SKULL = register("wither_skull", EntityType.Builder.create(WitherSkullEntity::new, SpawnGroup.MISC).setDimensions(0.3125F, 0.3125F).maxTrackingRange(4).trackingTickInterval(10));
      WOLF = register("wolf", EntityType.Builder.create(WolfEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 0.85F).maxTrackingRange(10));
      ZOGLIN = register("zoglin", EntityType.Builder.create(ZoglinEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(1.3964844F, 1.4F).maxTrackingRange(8));
      ZOMBIE = register("zombie", EntityType.Builder.create(ZombieEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      ZOMBIE_HORSE = register("zombie_horse", EntityType.Builder.create(ZombieHorseEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.6F).maxTrackingRange(10));
      ZOMBIE_VILLAGER = register("zombie_villager", EntityType.Builder.create(ZombieVillagerEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      ZOMBIFIED_PIGLIN = register("zombified_piglin", EntityType.Builder.create(ZombifiedPiglinEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      PLAYER = register("player", EntityType.Builder.create(SpawnGroup.MISC).disableSaving().disableSummon().setDimensions(0.6F, 1.8F).maxTrackingRange(32).trackingTickInterval(2));
      FISHING_BOBBER = register("fishing_bobber", EntityType.Builder.create(SpawnGroup.MISC).disableSaving().disableSummon().setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(5));
   }

   public interface EntityFactory<T extends Entity> {
      T create(EntityType<T> type, World world);
   }

   public static class Builder<T extends Entity> {
      private final EntityType.EntityFactory<T> factory;
      private final SpawnGroup spawnGroup;
      private ImmutableSet<Block> canSpawnInside = ImmutableSet.of();
      private boolean saveable = true;
      private boolean summonable = true;
      private boolean fireImmune;
      private boolean spawnableFarFromPlayer;
      private int maxTrackingRange = 5;
      private int trackingTickInterval = 3;
      private EntityDimensions dimensions = EntityDimensions.changing(0.6F, 1.8F);

      private Builder(EntityType.EntityFactory<T> factory, SpawnGroup spawnGroup) {
         this.factory = factory;
         this.spawnGroup = spawnGroup;
         this.spawnableFarFromPlayer = spawnGroup == SpawnGroup.CREATURE || spawnGroup == SpawnGroup.MISC;
      }

      public static <T extends Entity> EntityType.Builder<T> create(EntityType.EntityFactory<T> factory, SpawnGroup spawnGroup) {
         return new EntityType.Builder(factory, spawnGroup);
      }

      public static <T extends Entity> EntityType.Builder<T> create(SpawnGroup spawnGroup) {
         return new EntityType.Builder((entityType, world) -> {
            return null;
         }, spawnGroup);
      }

      public EntityType.Builder<T> setDimensions(float width, float height) {
         this.dimensions = EntityDimensions.changing(width, height);
         return this;
      }

      public EntityType.Builder<T> disableSummon() {
         this.summonable = false;
         return this;
      }

      public EntityType.Builder<T> disableSaving() {
         this.saveable = false;
         return this;
      }

      public EntityType.Builder<T> makeFireImmune() {
         this.fireImmune = true;
         return this;
      }

      /**
       * Allows this type of entity to spawn inside the given block, bypassing the default
       * wither rose, sweet berry bush, cactus, and fire-damage-dealing blocks for
       * non-fire-resistant mobs.
       * 
       * <p>{@code minecraft:prevent_mob_spawning_inside} tag overrides this.
       * With this setting, fire resistant mobs can spawn on/in fire damage dealing blocks,
       * and wither skeletons can spawn in wither roses. If a block added is not in the default
       * blacklist, the addition has no effect.
       */
      public EntityType.Builder<T> allowSpawningInside(Block... blocks) {
         this.canSpawnInside = ImmutableSet.copyOf((Object[])blocks);
         return this;
      }

      public EntityType.Builder<T> spawnableFarFromPlayer() {
         this.spawnableFarFromPlayer = true;
         return this;
      }

      public EntityType.Builder<T> maxTrackingRange(int maxTrackingRange) {
         this.maxTrackingRange = maxTrackingRange;
         return this;
      }

      public EntityType.Builder<T> trackingTickInterval(int trackingTickInterval) {
         this.trackingTickInterval = trackingTickInterval;
         return this;
      }

      public EntityType<T> build(String id) {
         if (this.saveable) {
            Util.getChoiceType(TypeReferences.ENTITY_TREE, id);
         }

         return new EntityType(this.factory, this.spawnGroup, this.saveable, this.summonable, this.fireImmune, this.spawnableFarFromPlayer, this.canSpawnInside, this.dimensions, this.maxTrackingRange, this.trackingTickInterval);
      }
   }
}
