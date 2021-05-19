package net.minecraft.entity.boss.dragon;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.EndPortalFeature;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class EnderDragonFight {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Predicate<Entity> VALID_ENTITY;
   private final ServerBossBar bossBar;
   private final ServerWorld world;
   private final List<Integer> gateways;
   private final BlockPattern endPortalPattern;
   private int dragonSeenTimer;
   private int endCrystalsAlive;
   private int crystalCountTimer;
   private int playerUpdateTimer;
   private boolean dragonKilled;
   private boolean previouslyKilled;
   private UUID dragonUuid;
   private boolean doLegacyCheck;
   private BlockPos exitPortalLocation;
   private EnderDragonSpawnState dragonSpawnState;
   private int spawnStateTimer;
   private List<EndCrystalEntity> crystals;

   public EnderDragonFight(ServerWorld world, long l, CompoundTag compoundTag) {
      this.bossBar = (ServerBossBar)(new ServerBossBar(new TranslatableText("entity.minecraft.ender_dragon"), BossBar.Color.PINK, BossBar.Style.PROGRESS)).setDragonMusic(true).setThickenFog(true);
      this.gateways = Lists.newArrayList();
      this.doLegacyCheck = true;
      this.world = world;
      if (compoundTag.contains("DragonKilled", 99)) {
         if (compoundTag.containsUuid("Dragon")) {
            this.dragonUuid = compoundTag.getUuid("Dragon");
         }

         this.dragonKilled = compoundTag.getBoolean("DragonKilled");
         this.previouslyKilled = compoundTag.getBoolean("PreviouslyKilled");
         if (compoundTag.getBoolean("IsRespawning")) {
            this.dragonSpawnState = EnderDragonSpawnState.START;
         }

         if (compoundTag.contains("ExitPortalLocation", 10)) {
            this.exitPortalLocation = NbtHelper.toBlockPos(compoundTag.getCompound("ExitPortalLocation"));
         }
      } else {
         this.dragonKilled = true;
         this.previouslyKilled = true;
      }

      if (compoundTag.contains("Gateways", 9)) {
         ListTag listTag = compoundTag.getList("Gateways", 3);

         for(int i = 0; i < listTag.size(); ++i) {
            this.gateways.add(listTag.getInt(i));
         }
      } else {
         this.gateways.addAll(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
         Collections.shuffle(this.gateways, new Random(l));
      }

      this.endPortalPattern = BlockPatternBuilder.start().aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ").aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ").where('#', CachedBlockPosition.matchesBlockState(BlockPredicate.make(Blocks.BEDROCK))).build();
   }

   public CompoundTag toTag() {
      CompoundTag compoundTag = new CompoundTag();
      if (this.dragonUuid != null) {
         compoundTag.putUuid("Dragon", this.dragonUuid);
      }

      compoundTag.putBoolean("DragonKilled", this.dragonKilled);
      compoundTag.putBoolean("PreviouslyKilled", this.previouslyKilled);
      if (this.exitPortalLocation != null) {
         compoundTag.put("ExitPortalLocation", NbtHelper.fromBlockPos(this.exitPortalLocation));
      }

      ListTag listTag = new ListTag();
      Iterator var3 = this.gateways.iterator();

      while(var3.hasNext()) {
         int i = (Integer)var3.next();
         listTag.add(IntTag.of(i));
      }

      compoundTag.put("Gateways", listTag);
      return compoundTag;
   }

   public void tick() {
      this.bossBar.setVisible(!this.dragonKilled);
      if (++this.playerUpdateTimer >= 20) {
         this.updatePlayers();
         this.playerUpdateTimer = 0;
      }

      if (!this.bossBar.getPlayers().isEmpty()) {
         this.world.getChunkManager().addTicket(ChunkTicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
         boolean bl = this.loadChunks();
         if (this.doLegacyCheck && bl) {
            this.convertFromLegacy();
            this.doLegacyCheck = false;
         }

         if (this.dragonSpawnState != null) {
            if (this.crystals == null && bl) {
               this.dragonSpawnState = null;
               this.respawnDragon();
            }

            this.dragonSpawnState.run(this.world, this, this.crystals, this.spawnStateTimer++, this.exitPortalLocation);
         }

         if (!this.dragonKilled) {
            if ((this.dragonUuid == null || ++this.dragonSeenTimer >= 1200) && bl) {
               this.checkDragonSeen();
               this.dragonSeenTimer = 0;
            }

            if (++this.crystalCountTimer >= 100 && bl) {
               this.countAliveCrystals();
               this.crystalCountTimer = 0;
            }
         }
      } else {
         this.world.getChunkManager().removeTicket(ChunkTicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
      }

   }

   private void convertFromLegacy() {
      LOGGER.info("Scanning for legacy world dragon fight...");
      boolean bl = this.worldContainsEndPortal();
      if (bl) {
         LOGGER.info("Found that the dragon has been killed in this world already.");
         this.previouslyKilled = true;
      } else {
         LOGGER.info("Found that the dragon has not yet been killed in this world.");
         this.previouslyKilled = false;
         if (this.findEndPortal() == null) {
            this.generateEndPortal(false);
         }
      }

      List<EnderDragonEntity> list = this.world.getAliveEnderDragons();
      if (list.isEmpty()) {
         this.dragonKilled = true;
      } else {
         EnderDragonEntity enderDragonEntity = (EnderDragonEntity)list.get(0);
         this.dragonUuid = enderDragonEntity.getUuid();
         LOGGER.info((String)"Found that there's a dragon still alive ({})", (Object)enderDragonEntity);
         this.dragonKilled = false;
         if (!bl) {
            LOGGER.info("But we didn't have a portal, let's remove it.");
            enderDragonEntity.remove();
            this.dragonUuid = null;
         }
      }

      if (!this.previouslyKilled && this.dragonKilled) {
         this.dragonKilled = false;
      }

   }

   private void checkDragonSeen() {
      List<EnderDragonEntity> list = this.world.getAliveEnderDragons();
      if (list.isEmpty()) {
         LOGGER.debug("Haven't seen the dragon, respawning it");
         this.createDragon();
      } else {
         LOGGER.debug("Haven't seen our dragon, but found another one to use.");
         this.dragonUuid = ((EnderDragonEntity)list.get(0)).getUuid();
      }

   }

   protected void setSpawnState(EnderDragonSpawnState enderDragonSpawnState) {
      if (this.dragonSpawnState == null) {
         throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
      } else {
         this.spawnStateTimer = 0;
         if (enderDragonSpawnState == EnderDragonSpawnState.END) {
            this.dragonSpawnState = null;
            this.dragonKilled = false;
            EnderDragonEntity enderDragonEntity = this.createDragon();
            Iterator var3 = this.bossBar.getPlayers().iterator();

            while(var3.hasNext()) {
               ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
               Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, enderDragonEntity);
            }
         } else {
            this.dragonSpawnState = enderDragonSpawnState;
         }

      }
   }

   private boolean worldContainsEndPortal() {
      for(int i = -8; i <= 8; ++i) {
         for(int j = -8; j <= 8; ++j) {
            WorldChunk worldChunk = this.world.getChunk(i, j);
            Iterator var4 = worldChunk.getBlockEntities().values().iterator();

            while(var4.hasNext()) {
               BlockEntity blockEntity = (BlockEntity)var4.next();
               if (blockEntity instanceof EndPortalBlockEntity) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Nullable
   private BlockPattern.Result findEndPortal() {
      int i;
      int l;
      for(i = -8; i <= 8; ++i) {
         for(l = -8; l <= 8; ++l) {
            WorldChunk worldChunk = this.world.getChunk(i, l);
            Iterator var4 = worldChunk.getBlockEntities().values().iterator();

            while(var4.hasNext()) {
               BlockEntity blockEntity = (BlockEntity)var4.next();
               if (blockEntity instanceof EndPortalBlockEntity) {
                  BlockPattern.Result result = this.endPortalPattern.searchAround(this.world, blockEntity.getPos());
                  if (result != null) {
                     BlockPos blockPos = result.translate(3, 3, 3).getBlockPos();
                     if (this.exitPortalLocation == null && blockPos.getX() == 0 && blockPos.getZ() == 0) {
                        this.exitPortalLocation = blockPos;
                     }

                     return result;
                  }
               }
            }
         }
      }

      i = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.ORIGIN).getY();

      for(l = i; l >= 0; --l) {
         BlockPattern.Result result2 = this.endPortalPattern.searchAround(this.world, new BlockPos(EndPortalFeature.ORIGIN.getX(), l, EndPortalFeature.ORIGIN.getZ()));
         if (result2 != null) {
            if (this.exitPortalLocation == null) {
               this.exitPortalLocation = result2.translate(3, 3, 3).getBlockPos();
            }

            return result2;
         }
      }

      return null;
   }

   private boolean loadChunks() {
      for(int i = -8; i <= 8; ++i) {
         for(int j = 8; j <= 8; ++j) {
            Chunk chunk = this.world.getChunk(i, j, ChunkStatus.FULL, false);
            if (!(chunk instanceof WorldChunk)) {
               return false;
            }

            ChunkHolder.LevelType levelType = ((WorldChunk)chunk).getLevelType();
            if (!levelType.isAfter(ChunkHolder.LevelType.TICKING)) {
               return false;
            }
         }
      }

      return true;
   }

   private void updatePlayers() {
      Set<ServerPlayerEntity> set = Sets.newHashSet();
      Iterator var2 = this.world.getPlayers(VALID_ENTITY).iterator();

      while(var2.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var2.next();
         this.bossBar.addPlayer(serverPlayerEntity);
         set.add(serverPlayerEntity);
      }

      Set<ServerPlayerEntity> set2 = Sets.newHashSet((Iterable)this.bossBar.getPlayers());
      set2.removeAll(set);
      Iterator var6 = set2.iterator();

      while(var6.hasNext()) {
         ServerPlayerEntity serverPlayerEntity2 = (ServerPlayerEntity)var6.next();
         this.bossBar.removePlayer(serverPlayerEntity2);
      }

   }

   private void countAliveCrystals() {
      this.crystalCountTimer = 0;
      this.endCrystalsAlive = 0;

      EndSpikeFeature.Spike spike;
      for(Iterator var1 = EndSpikeFeature.getSpikes(this.world).iterator(); var1.hasNext(); this.endCrystalsAlive += this.world.getNonSpectatingEntities(EndCrystalEntity.class, spike.getBoundingBox()).size()) {
         spike = (EndSpikeFeature.Spike)var1.next();
      }

      LOGGER.debug((String)"Found {} end crystals still alive", (Object)this.endCrystalsAlive);
   }

   public void dragonKilled(EnderDragonEntity dragon) {
      if (dragon.getUuid().equals(this.dragonUuid)) {
         this.bossBar.setPercent(0.0F);
         this.bossBar.setVisible(false);
         this.generateEndPortal(true);
         this.generateNewEndGateway();
         if (!this.previouslyKilled) {
            this.world.setBlockState(this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.ORIGIN), Blocks.DRAGON_EGG.getDefaultState());
         }

         this.previouslyKilled = true;
         this.dragonKilled = true;
      }

   }

   private void generateNewEndGateway() {
      if (!this.gateways.isEmpty()) {
         int i = (Integer)this.gateways.remove(this.gateways.size() - 1);
         int j = MathHelper.floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
         int k = MathHelper.floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
         this.generateEndGateway(new BlockPos(j, 75, k));
      }
   }

   private void generateEndGateway(BlockPos blockPos) {
      this.world.syncWorldEvent(3000, blockPos, 0);
      ConfiguredFeatures.END_GATEWAY_DELAYED.generate(this.world, this.world.getChunkManager().getChunkGenerator(), new Random(), blockPos);
   }

   private void generateEndPortal(boolean previouslyKilled) {
      EndPortalFeature endPortalFeature = new EndPortalFeature(previouslyKilled);
      if (this.exitPortalLocation == null) {
         for(this.exitPortalLocation = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN).down(); this.world.getBlockState(this.exitPortalLocation).isOf(Blocks.BEDROCK) && this.exitPortalLocation.getY() > this.world.getSeaLevel(); this.exitPortalLocation = this.exitPortalLocation.down()) {
         }
      }

      endPortalFeature.configure(FeatureConfig.DEFAULT).generate(this.world, this.world.getChunkManager().getChunkGenerator(), new Random(), this.exitPortalLocation);
   }

   private EnderDragonEntity createDragon() {
      this.world.getWorldChunk(new BlockPos(0, 128, 0));
      EnderDragonEntity enderDragonEntity = (EnderDragonEntity)EntityType.ENDER_DRAGON.create(this.world);
      enderDragonEntity.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
      enderDragonEntity.refreshPositionAndAngles(0.0D, 128.0D, 0.0D, this.world.random.nextFloat() * 360.0F, 0.0F);
      this.world.spawnEntity(enderDragonEntity);
      this.dragonUuid = enderDragonEntity.getUuid();
      return enderDragonEntity;
   }

   public void updateFight(EnderDragonEntity dragon) {
      if (dragon.getUuid().equals(this.dragonUuid)) {
         this.bossBar.setPercent(dragon.getHealth() / dragon.getMaxHealth());
         this.dragonSeenTimer = 0;
         if (dragon.hasCustomName()) {
            this.bossBar.setName(dragon.getDisplayName());
         }
      }

   }

   public int getAliveEndCrystals() {
      return this.endCrystalsAlive;
   }

   public void crystalDestroyed(EndCrystalEntity enderCrystal, DamageSource source) {
      if (this.dragonSpawnState != null && this.crystals.contains(enderCrystal)) {
         LOGGER.debug("Aborting respawn sequence");
         this.dragonSpawnState = null;
         this.spawnStateTimer = 0;
         this.resetEndCrystals();
         this.generateEndPortal(true);
      } else {
         this.countAliveCrystals();
         Entity entity = this.world.getEntity(this.dragonUuid);
         if (entity instanceof EnderDragonEntity) {
            ((EnderDragonEntity)entity).crystalDestroyed(enderCrystal, enderCrystal.getBlockPos(), source);
         }
      }

   }

   public boolean hasPreviouslyKilled() {
      return this.previouslyKilled;
   }

   public void respawnDragon() {
      if (this.dragonKilled && this.dragonSpawnState == null) {
         BlockPos blockPos = this.exitPortalLocation;
         if (blockPos == null) {
            LOGGER.debug("Tried to respawn, but need to find the portal first.");
            BlockPattern.Result result = this.findEndPortal();
            if (result == null) {
               LOGGER.debug("Couldn't find a portal, so we made one.");
               this.generateEndPortal(true);
            } else {
               LOGGER.debug("Found the exit portal & temporarily using it.");
            }

            blockPos = this.exitPortalLocation;
         }

         List<EndCrystalEntity> list = Lists.newArrayList();
         BlockPos blockPos2 = blockPos.up(1);
         Iterator var4 = Direction.Type.HORIZONTAL.iterator();

         while(var4.hasNext()) {
            Direction direction = (Direction)var4.next();
            List<EndCrystalEntity> list2 = this.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(blockPos2.offset((Direction)direction, 2)));
            if (list2.isEmpty()) {
               return;
            }

            list.addAll(list2);
         }

         LOGGER.debug("Found all crystals, respawning dragon.");
         this.respawnDragon(list);
      }

   }

   private void respawnDragon(List<EndCrystalEntity> crystals) {
      if (this.dragonKilled && this.dragonSpawnState == null) {
         for(BlockPattern.Result result = this.findEndPortal(); result != null; result = this.findEndPortal()) {
            for(int i = 0; i < this.endPortalPattern.getWidth(); ++i) {
               for(int j = 0; j < this.endPortalPattern.getHeight(); ++j) {
                  for(int k = 0; k < this.endPortalPattern.getDepth(); ++k) {
                     CachedBlockPosition cachedBlockPosition = result.translate(i, j, k);
                     if (cachedBlockPosition.getBlockState().isOf(Blocks.BEDROCK) || cachedBlockPosition.getBlockState().isOf(Blocks.END_PORTAL)) {
                        this.world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.END_STONE.getDefaultState());
                     }
                  }
               }
            }
         }

         this.dragonSpawnState = EnderDragonSpawnState.START;
         this.spawnStateTimer = 0;
         this.generateEndPortal(false);
         this.crystals = crystals;
      }

   }

   public void resetEndCrystals() {
      Iterator var1 = EndSpikeFeature.getSpikes(this.world).iterator();

      while(var1.hasNext()) {
         EndSpikeFeature.Spike spike = (EndSpikeFeature.Spike)var1.next();
         List<EndCrystalEntity> list = this.world.getNonSpectatingEntities(EndCrystalEntity.class, spike.getBoundingBox());
         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            EndCrystalEntity endCrystalEntity = (EndCrystalEntity)var4.next();
            endCrystalEntity.setInvulnerable(false);
            endCrystalEntity.setBeamTarget((BlockPos)null);
         }
      }

   }

   static {
      VALID_ENTITY = EntityPredicates.VALID_ENTITY.and(EntityPredicates.maxDistance(0.0D, 128.0D, 0.0D, 192.0D));
   }
}
