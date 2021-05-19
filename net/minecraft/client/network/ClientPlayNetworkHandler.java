package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DemoScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsListener;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.ServerList;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.render.debug.BeeDebugRenderer;
import net.minecraft.client.render.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.render.debug.NeighborUpdateDebugRenderer;
import net.minecraft.client.render.debug.VillageDebugRenderer;
import net.minecraft.client.render.debug.WorldGenAttemptDebugRenderer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.client.sound.AggressiveBeeSoundInstance;
import net.minecraft.client.sound.GuardianAttackSoundInstance;
import net.minecraft.client.sound.MovingMinecartSoundInstance;
import net.minecraft.client.sound.PassiveBeeSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
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
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ConfirmScreenActionC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CombatEventS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.ConfirmScreenActionS2CPacket;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.HeldItemChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.PaintingSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.TagQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.tag.RequiredTagListRegistry;
import net.minecraft.tag.TagManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.explosion.Explosion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ClientPlayNetworkHandler implements ClientPlayPacketListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Text field_26620 = new TranslatableText("disconnect.lost");
   private final ClientConnection connection;
   private final GameProfile profile;
   private final Screen loginScreen;
   private MinecraftClient client;
   private ClientWorld world;
   private ClientWorld.Properties worldProperties;
   private boolean positionLookSetup;
   private final Map<UUID, PlayerListEntry> playerListEntries = Maps.newHashMap();
   private final ClientAdvancementManager advancementHandler;
   private final ClientCommandSource commandSource;
   private TagManager tagManager;
   private final DataQueryHandler dataQueryHandler;
   private int chunkLoadDistance;
   private final Random random;
   private CommandDispatcher<CommandSource> commandDispatcher;
   private final RecipeManager recipeManager;
   private final UUID sessionId;
   private Set<RegistryKey<World>> worldKeys;
   private DynamicRegistryManager registryManager;

   public ClientPlayNetworkHandler(MinecraftClient client, Screen screen, ClientConnection connection, GameProfile profile) {
      this.tagManager = TagManager.EMPTY;
      this.dataQueryHandler = new DataQueryHandler(this);
      this.chunkLoadDistance = 3;
      this.random = new Random();
      this.commandDispatcher = new CommandDispatcher();
      this.recipeManager = new RecipeManager();
      this.sessionId = UUID.randomUUID();
      this.registryManager = DynamicRegistryManager.create();
      this.client = client;
      this.loginScreen = screen;
      this.connection = connection;
      this.profile = profile;
      this.advancementHandler = new ClientAdvancementManager(client);
      this.commandSource = new ClientCommandSource(this, client);
   }

   public ClientCommandSource getCommandSource() {
      return this.commandSource;
   }

   public void clearWorld() {
      this.world = null;
   }

   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   public void onGameJoin(GameJoinS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.interactionManager = new ClientPlayerInteractionManager(this.client, this);
      if (!this.connection.isLocal()) {
         RequiredTagListRegistry.clearAllTags();
      }

      ArrayList<RegistryKey<World>> arrayList = Lists.newArrayList((Iterable)packet.getDimensionIds());
      Collections.shuffle(arrayList);
      this.worldKeys = Sets.newLinkedHashSet(arrayList);
      this.registryManager = packet.getRegistryManager();
      RegistryKey<World> registryKey = packet.getDimensionId();
      DimensionType dimensionType = packet.getDimensionType();
      this.chunkLoadDistance = packet.getViewDistance();
      boolean bl = packet.isDebugWorld();
      boolean bl2 = packet.isFlatWorld();
      ClientWorld.Properties properties = new ClientWorld.Properties(Difficulty.NORMAL, packet.isHardcore(), bl2);
      this.worldProperties = properties;
      this.world = new ClientWorld(this, properties, registryKey, dimensionType, this.chunkLoadDistance, this.client::getProfiler, this.client.worldRenderer, bl, packet.getSha256Seed());
      this.client.joinWorld(this.world);
      if (this.client.player == null) {
         this.client.player = this.client.interactionManager.createPlayer(this.world, new StatHandler(), new ClientRecipeBook());
         this.client.player.yaw = -180.0F;
         if (this.client.getServer() != null) {
            this.client.getServer().setLocalPlayerUuid(this.client.player.getUuid());
         }
      }

      this.client.debugRenderer.reset();
      this.client.player.afterSpawn();
      int i = packet.getEntityId();
      this.world.addPlayer(i, this.client.player);
      this.client.player.input = new KeyboardInput(this.client.options);
      this.client.interactionManager.copyAbilities(this.client.player);
      this.client.cameraEntity = this.client.player;
      this.client.openScreen(new DownloadingTerrainScreen());
      this.client.player.setEntityId(i);
      this.client.player.setReducedDebugInfo(packet.hasReducedDebugInfo());
      this.client.player.setShowsDeathScreen(packet.showsDeathScreen());
      this.client.interactionManager.setGameMode(packet.getGameMode());
      this.client.interactionManager.setPreviousGameMode(packet.getPreviousGameMode());
      this.client.options.onPlayerModelPartChange();
      this.connection.send(new CustomPayloadC2SPacket(CustomPayloadC2SPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
      this.client.getGame().onStartGameSession();
   }

   public void onEntitySpawn(EntitySpawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      double d = packet.getX();
      double e = packet.getY();
      double f = packet.getZ();
      EntityType<?> entityType = packet.getEntityTypeId();
      Object entity15;
      if (entityType == EntityType.CHEST_MINECART) {
         entity15 = new ChestMinecartEntity(this.world, d, e, f);
      } else if (entityType == EntityType.FURNACE_MINECART) {
         entity15 = new FurnaceMinecartEntity(this.world, d, e, f);
      } else if (entityType == EntityType.TNT_MINECART) {
         entity15 = new TntMinecartEntity(this.world, d, e, f);
      } else if (entityType == EntityType.SPAWNER_MINECART) {
         entity15 = new SpawnerMinecartEntity(this.world, d, e, f);
      } else if (entityType == EntityType.HOPPER_MINECART) {
         entity15 = new HopperMinecartEntity(this.world, d, e, f);
      } else if (entityType == EntityType.COMMAND_BLOCK_MINECART) {
         entity15 = new CommandBlockMinecartEntity(this.world, d, e, f);
      } else if (entityType == EntityType.MINECART) {
         entity15 = new MinecartEntity(this.world, d, e, f);
      } else {
         Entity entity16;
         if (entityType == EntityType.FISHING_BOBBER) {
            entity16 = this.world.getEntityById(packet.getEntityData());
            if (entity16 instanceof PlayerEntity) {
               entity15 = new FishingBobberEntity(this.world, (PlayerEntity)entity16, d, e, f);
            } else {
               entity15 = null;
            }
         } else if (entityType == EntityType.ARROW) {
            entity15 = new ArrowEntity(this.world, d, e, f);
            entity16 = this.world.getEntityById(packet.getEntityData());
            if (entity16 != null) {
               ((PersistentProjectileEntity)entity15).setOwner(entity16);
            }
         } else if (entityType == EntityType.SPECTRAL_ARROW) {
            entity15 = new SpectralArrowEntity(this.world, d, e, f);
            entity16 = this.world.getEntityById(packet.getEntityData());
            if (entity16 != null) {
               ((PersistentProjectileEntity)entity15).setOwner(entity16);
            }
         } else if (entityType == EntityType.TRIDENT) {
            entity15 = new TridentEntity(this.world, d, e, f);
            entity16 = this.world.getEntityById(packet.getEntityData());
            if (entity16 != null) {
               ((PersistentProjectileEntity)entity15).setOwner(entity16);
            }
         } else if (entityType == EntityType.SNOWBALL) {
            entity15 = new SnowballEntity(this.world, d, e, f);
         } else if (entityType == EntityType.LLAMA_SPIT) {
            entity15 = new LlamaSpitEntity(this.world, d, e, f, packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
         } else if (entityType == EntityType.ITEM_FRAME) {
            entity15 = new ItemFrameEntity(this.world, new BlockPos(d, e, f), Direction.byId(packet.getEntityData()));
         } else if (entityType == EntityType.LEASH_KNOT) {
            entity15 = new LeashKnotEntity(this.world, new BlockPos(d, e, f));
         } else if (entityType == EntityType.ENDER_PEARL) {
            entity15 = new EnderPearlEntity(this.world, d, e, f);
         } else if (entityType == EntityType.EYE_OF_ENDER) {
            entity15 = new EyeOfEnderEntity(this.world, d, e, f);
         } else if (entityType == EntityType.FIREWORK_ROCKET) {
            entity15 = new FireworkRocketEntity(this.world, d, e, f, ItemStack.EMPTY);
         } else if (entityType == EntityType.FIREBALL) {
            entity15 = new FireballEntity(this.world, d, e, f, packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
         } else if (entityType == EntityType.DRAGON_FIREBALL) {
            entity15 = new DragonFireballEntity(this.world, d, e, f, packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
         } else if (entityType == EntityType.SMALL_FIREBALL) {
            entity15 = new SmallFireballEntity(this.world, d, e, f, packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
         } else if (entityType == EntityType.WITHER_SKULL) {
            entity15 = new WitherSkullEntity(this.world, d, e, f, packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
         } else if (entityType == EntityType.SHULKER_BULLET) {
            entity15 = new ShulkerBulletEntity(this.world, d, e, f, packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
         } else if (entityType == EntityType.EGG) {
            entity15 = new EggEntity(this.world, d, e, f);
         } else if (entityType == EntityType.EVOKER_FANGS) {
            entity15 = new EvokerFangsEntity(this.world, d, e, f, 0.0F, 0, (LivingEntity)null);
         } else if (entityType == EntityType.POTION) {
            entity15 = new PotionEntity(this.world, d, e, f);
         } else if (entityType == EntityType.EXPERIENCE_BOTTLE) {
            entity15 = new ExperienceBottleEntity(this.world, d, e, f);
         } else if (entityType == EntityType.BOAT) {
            entity15 = new BoatEntity(this.world, d, e, f);
         } else if (entityType == EntityType.TNT) {
            entity15 = new TntEntity(this.world, d, e, f, (LivingEntity)null);
         } else if (entityType == EntityType.ARMOR_STAND) {
            entity15 = new ArmorStandEntity(this.world, d, e, f);
         } else if (entityType == EntityType.END_CRYSTAL) {
            entity15 = new EndCrystalEntity(this.world, d, e, f);
         } else if (entityType == EntityType.ITEM) {
            entity15 = new ItemEntity(this.world, d, e, f);
         } else if (entityType == EntityType.FALLING_BLOCK) {
            entity15 = new FallingBlockEntity(this.world, d, e, f, Block.getStateFromRawId(packet.getEntityData()));
         } else if (entityType == EntityType.AREA_EFFECT_CLOUD) {
            entity15 = new AreaEffectCloudEntity(this.world, d, e, f);
         } else if (entityType == EntityType.LIGHTNING_BOLT) {
            entity15 = new LightningEntity(EntityType.LIGHTNING_BOLT, this.world);
         } else {
            entity15 = null;
         }
      }

      if (entity15 != null) {
         int i = packet.getId();
         ((Entity)entity15).updateTrackedPosition(d, e, f);
         ((Entity)entity15).refreshPositionAfterTeleport(d, e, f);
         ((Entity)entity15).pitch = (float)(packet.getPitch() * 360) / 256.0F;
         ((Entity)entity15).yaw = (float)(packet.getYaw() * 360) / 256.0F;
         ((Entity)entity15).setEntityId(i);
         ((Entity)entity15).setUuid(packet.getUuid());
         this.world.addEntity(i, (Entity)entity15);
         if (entity15 instanceof AbstractMinecartEntity) {
            this.client.getSoundManager().play(new MovingMinecartSoundInstance((AbstractMinecartEntity)entity15));
         }
      }

   }

   public void onExperienceOrbSpawn(ExperienceOrbSpawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      double d = packet.getX();
      double e = packet.getY();
      double f = packet.getZ();
      Entity entity = new ExperienceOrbEntity(this.world, d, e, f, packet.getExperience());
      entity.updateTrackedPosition(d, e, f);
      entity.yaw = 0.0F;
      entity.pitch = 0.0F;
      entity.setEntityId(packet.getId());
      this.world.addEntity(packet.getId(), entity);
   }

   public void onPaintingSpawn(PaintingSpawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PaintingEntity paintingEntity = new PaintingEntity(this.world, packet.getPos(), packet.getFacing(), packet.getMotive());
      paintingEntity.setEntityId(packet.getId());
      paintingEntity.setUuid(packet.getPaintingUuid());
      this.world.addEntity(packet.getId(), paintingEntity);
   }

   public void onVelocityUpdate(EntityVelocityUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getId());
      if (entity != null) {
         entity.setVelocityClient((double)packet.getVelocityX() / 8000.0D, (double)packet.getVelocityY() / 8000.0D, (double)packet.getVelocityZ() / 8000.0D);
      }
   }

   public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.id());
      if (entity != null && packet.getTrackedValues() != null) {
         entity.getDataTracker().writeUpdatedEntries(packet.getTrackedValues());
      }

   }

   public void onPlayerSpawn(PlayerSpawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      double d = packet.getX();
      double e = packet.getY();
      double f = packet.getZ();
      float g = (float)(packet.getYaw() * 360) / 256.0F;
      float h = (float)(packet.getPitch() * 360) / 256.0F;
      int i = packet.getId();
      OtherClientPlayerEntity otherClientPlayerEntity = new OtherClientPlayerEntity(this.client.world, this.getPlayerListEntry(packet.getPlayerUuid()).getProfile());
      otherClientPlayerEntity.setEntityId(i);
      otherClientPlayerEntity.resetPosition(d, e, f);
      otherClientPlayerEntity.updateTrackedPosition(d, e, f);
      otherClientPlayerEntity.updatePositionAndAngles(d, e, f, g, h);
      this.world.addPlayer(i, otherClientPlayerEntity);
   }

   public void onEntityPosition(EntityPositionS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getId());
      if (entity != null) {
         double d = packet.getX();
         double e = packet.getY();
         double f = packet.getZ();
         entity.updateTrackedPosition(d, e, f);
         if (!entity.isLogicalSideForUpdatingMovement()) {
            float g = (float)(packet.getYaw() * 360) / 256.0F;
            float h = (float)(packet.getPitch() * 360) / 256.0F;
            entity.updateTrackedPositionAndAngles(d, e, f, g, h, 3, true);
            entity.setOnGround(packet.isOnGround());
         }

      }
   }

   public void onHeldItemChange(HeldItemChangeS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (PlayerInventory.isValidHotbarIndex(packet.getSlot())) {
         this.client.player.inventory.selectedSlot = packet.getSlot();
      }

   }

   public void onEntityUpdate(EntityS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = packet.getEntity(this.world);
      if (entity != null) {
         if (!entity.isLogicalSideForUpdatingMovement()) {
            float i;
            if (packet.isPositionChanged()) {
               Vec3d vec3d = packet.calculateDeltaPosition(entity.getTrackedPosition());
               entity.updateTrackedPosition(vec3d);
               i = packet.hasRotation() ? (float)(packet.getYaw() * 360) / 256.0F : entity.yaw;
               float g = packet.hasRotation() ? (float)(packet.getPitch() * 360) / 256.0F : entity.pitch;
               entity.updateTrackedPositionAndAngles(vec3d.getX(), vec3d.getY(), vec3d.getZ(), i, g, 3, false);
            } else if (packet.hasRotation()) {
               float h = (float)(packet.getYaw() * 360) / 256.0F;
               i = (float)(packet.getPitch() * 360) / 256.0F;
               entity.updateTrackedPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), h, i, 3, false);
            }

            entity.setOnGround(packet.isOnGround());
         }

      }
   }

   public void onEntitySetHeadYaw(EntitySetHeadYawS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = packet.getEntity(this.world);
      if (entity != null) {
         float f = (float)(packet.getHeadYaw() * 360) / 256.0F;
         entity.updateTrackedHeadRotation(f, 3);
      }
   }

   public void onEntitiesDestroy(EntitiesDestroyS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);

      for(int i = 0; i < packet.getEntityIds().length; ++i) {
         int j = packet.getEntityIds()[i];
         this.world.removeEntity(j);
      }

   }

   public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity playerEntity = this.client.player;
      Vec3d vec3d = playerEntity.getVelocity();
      boolean bl = packet.getFlags().contains(PlayerPositionLookS2CPacket.Flag.X);
      boolean bl2 = packet.getFlags().contains(PlayerPositionLookS2CPacket.Flag.Y);
      boolean bl3 = packet.getFlags().contains(PlayerPositionLookS2CPacket.Flag.Z);
      double f;
      double g;
      if (bl) {
         f = vec3d.getX();
         g = playerEntity.getX() + packet.getX();
         playerEntity.lastRenderX += packet.getX();
      } else {
         f = 0.0D;
         g = packet.getX();
         playerEntity.lastRenderX = g;
      }

      double j;
      double k;
      if (bl2) {
         j = vec3d.getY();
         k = playerEntity.getY() + packet.getY();
         playerEntity.lastRenderY += packet.getY();
      } else {
         j = 0.0D;
         k = packet.getY();
         playerEntity.lastRenderY = k;
      }

      double n;
      double o;
      if (bl3) {
         n = vec3d.getZ();
         o = playerEntity.getZ() + packet.getZ();
         playerEntity.lastRenderZ += packet.getZ();
      } else {
         n = 0.0D;
         o = packet.getZ();
         playerEntity.lastRenderZ = o;
      }

      if (playerEntity.age > 0 && playerEntity.getVehicle() != null) {
         playerEntity.method_29239();
      }

      playerEntity.setPos(g, k, o);
      playerEntity.prevX = g;
      playerEntity.prevY = k;
      playerEntity.prevZ = o;
      playerEntity.setVelocity(f, j, n);
      float p = packet.getYaw();
      float q = packet.getPitch();
      if (packet.getFlags().contains(PlayerPositionLookS2CPacket.Flag.X_ROT)) {
         q += playerEntity.pitch;
      }

      if (packet.getFlags().contains(PlayerPositionLookS2CPacket.Flag.Y_ROT)) {
         p += playerEntity.yaw;
      }

      playerEntity.updatePositionAndAngles(g, k, o, p, q);
      this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
      this.connection.send(new PlayerMoveC2SPacket.Both(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.yaw, playerEntity.pitch, false));
      if (!this.positionLookSetup) {
         this.positionLookSetup = true;
         this.client.openScreen((Screen)null);
      }

   }

   public void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      int i = 19 | (packet.method_31179() ? 128 : 0);
      packet.visitUpdates((blockPos, blockState) -> {
         this.world.setBlockState(blockPos, blockState, i);
      });
   }

   public void onChunkData(ChunkDataS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      int i = packet.getX();
      int j = packet.getZ();
      BiomeArray biomeArray = packet.getBiomeArray() == null ? null : new BiomeArray(this.registryManager.get(Registry.BIOME_KEY), packet.getBiomeArray());
      WorldChunk worldChunk = this.world.getChunkManager().loadChunkFromPacket(i, j, biomeArray, packet.getReadBuffer(), packet.getHeightmaps(), packet.getVerticalStripBitmask(), packet.isFullChunk());
      if (worldChunk != null && packet.isFullChunk()) {
         this.world.addEntitiesToChunk(worldChunk);
      }

      for(int k = 0; k < 16; ++k) {
         this.world.scheduleBlockRenders(i, k, j);
      }

      Iterator var10 = packet.getBlockEntityTagList().iterator();

      while(var10.hasNext()) {
         CompoundTag compoundTag = (CompoundTag)var10.next();
         BlockPos blockPos = new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
         BlockEntity blockEntity = this.world.getBlockEntity(blockPos);
         if (blockEntity != null) {
            blockEntity.fromTag(this.world.getBlockState(blockPos), compoundTag);
         }
      }

   }

   public void onUnloadChunk(UnloadChunkS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      int i = packet.getX();
      int j = packet.getZ();
      ClientChunkManager clientChunkManager = this.world.getChunkManager();
      clientChunkManager.unload(i, j);
      LightingProvider lightingProvider = clientChunkManager.getLightingProvider();

      for(int k = 0; k < 16; ++k) {
         this.world.scheduleBlockRenders(i, k, j);
         lightingProvider.setSectionStatus(ChunkSectionPos.from(i, k, j), true);
      }

      lightingProvider.setColumnEnabled(new ChunkPos(i, j), false);
   }

   public void onBlockUpdate(BlockUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.setBlockStateWithoutNeighborUpdates(packet.getPos(), packet.getState());
   }

   public void onDisconnect(DisconnectS2CPacket packet) {
      this.connection.disconnect(packet.getReason());
   }

   public void onDisconnected(Text reason) {
      this.client.disconnect();
      if (this.loginScreen != null) {
         if (this.loginScreen instanceof RealmsScreen) {
            this.client.openScreen(new DisconnectedRealmsScreen(this.loginScreen, field_26620, reason));
         } else {
            this.client.openScreen(new DisconnectedScreen(this.loginScreen, field_26620, reason));
         }
      } else {
         this.client.openScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), field_26620, reason));
      }

   }

   /**
    * Sends a packet to the server.
    * 
    * @param packet the packet to send
    */
   public void sendPacket(Packet<?> packet) {
      this.connection.send(packet);
   }

   public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getEntityId());
      LivingEntity livingEntity = (LivingEntity)this.world.getEntityById(packet.getCollectorEntityId());
      if (livingEntity == null) {
         livingEntity = this.client.player;
      }

      if (entity != null) {
         if (entity instanceof ExperienceOrbEntity) {
            this.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F, false);
         } else {
            this.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F, false);
         }

         this.client.particleManager.addParticle(new ItemPickupParticle(this.client.getEntityRenderDispatcher(), this.client.getBufferBuilders(), this.world, entity, (Entity)livingEntity));
         if (entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity)entity;
            ItemStack itemStack = itemEntity.getStack();
            itemStack.decrement(packet.getStackAmount());
            if (itemStack.isEmpty()) {
               this.world.removeEntity(packet.getEntityId());
            }
         } else {
            this.world.removeEntity(packet.getEntityId());
         }
      }

   }

   public void onGameMessage(GameMessageS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.inGameHud.addChatMessage(packet.getLocation(), packet.getMessage(), packet.getSenderUuid());
   }

   public void onEntityAnimation(EntityAnimationS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getId());
      if (entity != null) {
         LivingEntity livingEntity2;
         if (packet.getAnimationId() == 0) {
            livingEntity2 = (LivingEntity)entity;
            livingEntity2.swingHand(Hand.MAIN_HAND);
         } else if (packet.getAnimationId() == 3) {
            livingEntity2 = (LivingEntity)entity;
            livingEntity2.swingHand(Hand.OFF_HAND);
         } else if (packet.getAnimationId() == 1) {
            entity.animateDamage();
         } else if (packet.getAnimationId() == 2) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            playerEntity.wakeUp(false, false);
         } else if (packet.getAnimationId() == 4) {
            this.client.particleManager.addEmitter(entity, ParticleTypes.CRIT);
         } else if (packet.getAnimationId() == 5) {
            this.client.particleManager.addEmitter(entity, ParticleTypes.ENCHANTED_HIT);
         }

      }
   }

   public void onMobSpawn(MobSpawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      double d = packet.getX();
      double e = packet.getY();
      double f = packet.getZ();
      float g = (float)(packet.getYaw() * 360) / 256.0F;
      float h = (float)(packet.getPitch() * 360) / 256.0F;
      LivingEntity livingEntity = (LivingEntity)EntityType.createInstanceFromId(packet.getEntityTypeId(), this.client.world);
      if (livingEntity != null) {
         livingEntity.updateTrackedPosition(d, e, f);
         livingEntity.bodyYaw = (float)(packet.getHeadYaw() * 360) / 256.0F;
         livingEntity.headYaw = (float)(packet.getHeadYaw() * 360) / 256.0F;
         if (livingEntity instanceof EnderDragonEntity) {
            EnderDragonPart[] enderDragonParts = ((EnderDragonEntity)livingEntity).getBodyParts();

            for(int i = 0; i < enderDragonParts.length; ++i) {
               enderDragonParts[i].setEntityId(i + packet.getId());
            }
         }

         livingEntity.setEntityId(packet.getId());
         livingEntity.setUuid(packet.getUuid());
         livingEntity.updatePositionAndAngles(d, e, f, g, h);
         livingEntity.setVelocity((double)((float)packet.getVelocityX() / 8000.0F), (double)((float)packet.getVelocityY() / 8000.0F), (double)((float)packet.getVelocityZ() / 8000.0F));
         this.world.addEntity(packet.getId(), livingEntity);
         if (livingEntity instanceof BeeEntity) {
            boolean bl = ((BeeEntity)livingEntity).hasAngerTime();
            Object abstractBeeSoundInstance2;
            if (bl) {
               abstractBeeSoundInstance2 = new AggressiveBeeSoundInstance((BeeEntity)livingEntity);
            } else {
               abstractBeeSoundInstance2 = new PassiveBeeSoundInstance((BeeEntity)livingEntity);
            }

            this.client.getSoundManager().playNextTick((TickableSoundInstance)abstractBeeSoundInstance2);
         }
      } else {
         LOGGER.warn((String)"Skipping Entity with id {}", (Object)packet.getEntityTypeId());
      }

   }

   public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.method_29089(packet.getTime());
      this.client.world.setTimeOfDay(packet.getTimeOfDay());
   }

   public void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.setSpawnPos(packet.getPos(), packet.getAngle());
   }

   public void onEntityPassengersSet(EntityPassengersSetS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getId());
      if (entity == null) {
         LOGGER.warn("Received passengers for unknown entity");
      } else {
         boolean bl = entity.hasPassengerDeep(this.client.player);
         entity.removeAllPassengers();
         int[] var4 = packet.getPassengerIds();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int i = var4[var6];
            Entity entity2 = this.world.getEntityById(i);
            if (entity2 != null) {
               entity2.startRiding(entity, true);
               if (entity2 == this.client.player && !bl) {
                  this.client.inGameHud.setOverlayMessage(new TranslatableText("mount.onboard", new Object[]{this.client.options.keySneak.getBoundKeyLocalizedText()}), false);
               }
            }
         }

      }
   }

   public void onEntityAttach(EntityAttachS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getAttachedEntityId());
      if (entity instanceof MobEntity) {
         ((MobEntity)entity).setHoldingEntityId(packet.getHoldingEntityId());
      }

   }

   private static ItemStack getActiveTotemOfUndying(PlayerEntity player) {
      Hand[] var1 = Hand.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Hand hand = var1[var3];
         ItemStack itemStack = player.getStackInHand(hand);
         if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
            return itemStack;
         }
      }

      return new ItemStack(Items.TOTEM_OF_UNDYING);
   }

   public void onEntityStatus(EntityStatusS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = packet.getEntity(this.world);
      if (entity != null) {
         if (packet.getStatus() == 21) {
            this.client.getSoundManager().play(new GuardianAttackSoundInstance((GuardianEntity)entity));
         } else if (packet.getStatus() == 35) {
            int i = true;
            this.client.particleManager.addEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
            this.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);
            if (entity == this.client.player) {
               this.client.gameRenderer.showFloatingItem(getActiveTotemOfUndying(this.client.player));
            }
         } else {
            entity.handleStatus(packet.getStatus());
         }
      }

   }

   public void onHealthUpdate(HealthUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.player.updateHealth(packet.getHealth());
      this.client.player.getHungerManager().setFoodLevel(packet.getFood());
      this.client.player.getHungerManager().setSaturationLevelClient(packet.getSaturation());
   }

   public void onExperienceBarUpdate(ExperienceBarUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.player.setExperience(packet.getBarProgress(), packet.getExperienceLevel(), packet.getExperience());
   }

   public void onPlayerRespawn(PlayerRespawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      RegistryKey<World> registryKey = packet.getDimension();
      DimensionType dimensionType = packet.method_29445();
      ClientPlayerEntity clientPlayerEntity = this.client.player;
      int i = clientPlayerEntity.getEntityId();
      this.positionLookSetup = false;
      if (registryKey != clientPlayerEntity.world.getRegistryKey()) {
         Scoreboard scoreboard = this.world.getScoreboard();
         boolean bl = packet.isDebugWorld();
         boolean bl2 = packet.isFlatWorld();
         ClientWorld.Properties properties = new ClientWorld.Properties(this.worldProperties.getDifficulty(), this.worldProperties.isHardcore(), bl2);
         this.worldProperties = properties;
         this.world = new ClientWorld(this, properties, registryKey, dimensionType, this.chunkLoadDistance, this.client::getProfiler, this.client.worldRenderer, bl, packet.getSha256Seed());
         this.world.setScoreboard(scoreboard);
         this.client.joinWorld(this.world);
         this.client.openScreen(new DownloadingTerrainScreen());
      }

      this.world.finishRemovingEntities();
      String string = clientPlayerEntity.getServerBrand();
      this.client.cameraEntity = null;
      ClientPlayerEntity clientPlayerEntity2 = this.client.interactionManager.createPlayer(this.world, clientPlayerEntity.getStatHandler(), clientPlayerEntity.getRecipeBook(), clientPlayerEntity.isSneaking(), clientPlayerEntity.isSprinting());
      clientPlayerEntity2.setEntityId(i);
      this.client.player = clientPlayerEntity2;
      if (registryKey != clientPlayerEntity.world.getRegistryKey()) {
         this.client.getMusicTracker().stop();
      }

      this.client.cameraEntity = clientPlayerEntity2;
      clientPlayerEntity2.getDataTracker().writeUpdatedEntries(clientPlayerEntity.getDataTracker().getAllEntries());
      if (packet.shouldKeepPlayerAttributes()) {
         clientPlayerEntity2.getAttributes().setFrom(clientPlayerEntity.getAttributes());
      }

      clientPlayerEntity2.afterSpawn();
      clientPlayerEntity2.setServerBrand(string);
      this.world.addPlayer(i, clientPlayerEntity2);
      clientPlayerEntity2.yaw = -180.0F;
      clientPlayerEntity2.input = new KeyboardInput(this.client.options);
      this.client.interactionManager.copyAbilities(clientPlayerEntity2);
      clientPlayerEntity2.setReducedDebugInfo(clientPlayerEntity.getReducedDebugInfo());
      clientPlayerEntity2.setShowsDeathScreen(clientPlayerEntity.showsDeathScreen());
      if (this.client.currentScreen instanceof DeathScreen) {
         this.client.openScreen((Screen)null);
      }

      this.client.interactionManager.setGameMode(packet.getGameMode());
      this.client.interactionManager.setPreviousGameMode(packet.getPreviousGameMode());
   }

   public void onExplosion(ExplosionS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Explosion explosion = new Explosion(this.client.world, (Entity)null, packet.getX(), packet.getY(), packet.getZ(), packet.getRadius(), packet.getAffectedBlocks());
      explosion.affectWorld(true);
      this.client.player.setVelocity(this.client.player.getVelocity().add((double)packet.getPlayerVelocityX(), (double)packet.getPlayerVelocityY(), (double)packet.getPlayerVelocityZ()));
   }

   public void onOpenHorseScreen(OpenHorseScreenS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getHorseId());
      if (entity instanceof HorseBaseEntity) {
         ClientPlayerEntity clientPlayerEntity = this.client.player;
         HorseBaseEntity horseBaseEntity = (HorseBaseEntity)entity;
         SimpleInventory simpleInventory = new SimpleInventory(packet.getSlotCount());
         HorseScreenHandler horseScreenHandler = new HorseScreenHandler(packet.getSyncId(), clientPlayerEntity.inventory, simpleInventory, horseBaseEntity);
         clientPlayerEntity.currentScreenHandler = horseScreenHandler;
         this.client.openScreen(new HorseScreen(horseScreenHandler, clientPlayerEntity.inventory, horseBaseEntity));
      }

   }

   public void onOpenScreen(OpenScreenS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      HandledScreens.open(packet.getScreenHandlerType(), this.client, packet.getSyncId(), packet.getName());
   }

   public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity playerEntity = this.client.player;
      ItemStack itemStack = packet.getItemStack();
      int i = packet.getSlot();
      this.client.getTutorialManager().onSlotUpdate(itemStack);
      if (packet.getSyncId() == -1) {
         if (!(this.client.currentScreen instanceof CreativeInventoryScreen)) {
            playerEntity.inventory.setCursorStack(itemStack);
         }
      } else if (packet.getSyncId() == -2) {
         playerEntity.inventory.setStack(i, itemStack);
      } else {
         boolean bl = false;
         if (this.client.currentScreen instanceof CreativeInventoryScreen) {
            CreativeInventoryScreen creativeInventoryScreen = (CreativeInventoryScreen)this.client.currentScreen;
            bl = creativeInventoryScreen.getSelectedTab() != ItemGroup.INVENTORY.getIndex();
         }

         if (packet.getSyncId() == 0 && packet.getSlot() >= 36 && i < 45) {
            if (!itemStack.isEmpty()) {
               ItemStack itemStack2 = playerEntity.playerScreenHandler.getSlot(i).getStack();
               if (itemStack2.isEmpty() || itemStack2.getCount() < itemStack.getCount()) {
                  itemStack.setCooldown(5);
               }
            }

            playerEntity.playerScreenHandler.setStackInSlot(i, itemStack);
         } else if (packet.getSyncId() == playerEntity.currentScreenHandler.syncId && (packet.getSyncId() != 0 || !bl)) {
            playerEntity.currentScreenHandler.setStackInSlot(i, itemStack);
         }
      }

   }

   public void onConfirmScreenAction(ConfirmScreenActionS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      ScreenHandler screenHandler = null;
      PlayerEntity playerEntity = this.client.player;
      if (packet.getSyncId() == 0) {
         screenHandler = playerEntity.playerScreenHandler;
      } else if (packet.getSyncId() == playerEntity.currentScreenHandler.syncId) {
         screenHandler = playerEntity.currentScreenHandler;
      }

      if (screenHandler != null && !packet.wasAccepted()) {
         this.sendPacket(new ConfirmScreenActionC2SPacket(packet.getSyncId(), packet.getActionId(), true));
      }

   }

   public void onInventory(InventoryS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity playerEntity = this.client.player;
      if (packet.getSyncId() == 0) {
         playerEntity.playerScreenHandler.updateSlotStacks(packet.getContents());
      } else if (packet.getSyncId() == playerEntity.currentScreenHandler.syncId) {
         playerEntity.currentScreenHandler.updateSlotStacks(packet.getContents());
      }

   }

   public void onSignEditorOpen(SignEditorOpenS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      BlockEntity blockEntity = this.world.getBlockEntity(packet.getPos());
      if (!(blockEntity instanceof SignBlockEntity)) {
         blockEntity = new SignBlockEntity();
         ((BlockEntity)blockEntity).setLocation(this.world, packet.getPos());
      }

      this.client.player.openEditSignScreen((SignBlockEntity)blockEntity);
   }

   public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      BlockPos blockPos = packet.getPos();
      BlockEntity blockEntity = this.client.world.getBlockEntity(blockPos);
      int i = packet.getBlockEntityType();
      boolean bl = i == 2 && blockEntity instanceof CommandBlockBlockEntity;
      if (i == 1 && blockEntity instanceof MobSpawnerBlockEntity || bl || i == 3 && blockEntity instanceof BeaconBlockEntity || i == 4 && blockEntity instanceof SkullBlockEntity || i == 6 && blockEntity instanceof BannerBlockEntity || i == 7 && blockEntity instanceof StructureBlockBlockEntity || i == 8 && blockEntity instanceof EndGatewayBlockEntity || i == 9 && blockEntity instanceof SignBlockEntity || i == 11 && blockEntity instanceof BedBlockEntity || i == 5 && blockEntity instanceof ConduitBlockEntity || i == 12 && blockEntity instanceof JigsawBlockEntity || i == 13 && blockEntity instanceof CampfireBlockEntity || i == 14 && blockEntity instanceof BeehiveBlockEntity) {
         blockEntity.fromTag(this.client.world.getBlockState(blockPos), packet.getCompoundTag());
      }

      if (bl && this.client.currentScreen instanceof CommandBlockScreen) {
         ((CommandBlockScreen)this.client.currentScreen).updateCommandBlock();
      }

   }

   public void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity playerEntity = this.client.player;
      if (playerEntity.currentScreenHandler != null && playerEntity.currentScreenHandler.syncId == packet.getSyncId()) {
         playerEntity.currentScreenHandler.setProperty(packet.getPropertyId(), packet.getValue());
      }

   }

   public void onEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getId());
      if (entity != null) {
         packet.getEquipmentList().forEach((pair) -> {
            entity.equipStack((EquipmentSlot)pair.getFirst(), (ItemStack)pair.getSecond());
         });
      }

   }

   public void onCloseScreen(CloseScreenS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.player.closeScreen();
   }

   public void onBlockEvent(BlockEventS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.addSyncedBlockEvent(packet.getPos(), packet.getBlock(), packet.getType(), packet.getData());
   }

   public void onBlockDestroyProgress(BlockBreakingProgressS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.setBlockBreakingInfo(packet.getEntityId(), packet.getPos(), packet.getProgress());
   }

   public void onGameStateChange(GameStateChangeS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity playerEntity = this.client.player;
      GameStateChangeS2CPacket.Reason reason = packet.getReason();
      float f = packet.getValue();
      int i = MathHelper.floor(f + 0.5F);
      if (reason == GameStateChangeS2CPacket.NO_RESPAWN_BLOCK) {
         playerEntity.sendMessage(new TranslatableText("block.minecraft.spawn.not_valid"), false);
      } else if (reason == GameStateChangeS2CPacket.RAIN_STARTED) {
         this.world.getLevelProperties().setRaining(true);
         this.world.setRainGradient(0.0F);
      } else if (reason == GameStateChangeS2CPacket.RAIN_STOPPED) {
         this.world.getLevelProperties().setRaining(false);
         this.world.setRainGradient(1.0F);
      } else if (reason == GameStateChangeS2CPacket.GAME_MODE_CHANGED) {
         this.client.interactionManager.setGameMode(GameMode.byId(i));
      } else if (reason == GameStateChangeS2CPacket.GAME_WON) {
         if (i == 0) {
            this.client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
            this.client.openScreen(new DownloadingTerrainScreen());
         } else if (i == 1) {
            this.client.openScreen(new CreditsScreen(true, () -> {
               this.client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
            }));
         }
      } else if (reason == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN) {
         GameOptions gameOptions = this.client.options;
         if (f == 0.0F) {
            this.client.openScreen(new DemoScreen());
         } else if (f == 101.0F) {
            this.client.inGameHud.getChatHud().addMessage(new TranslatableText("demo.help.movement", new Object[]{gameOptions.keyForward.getBoundKeyLocalizedText(), gameOptions.keyLeft.getBoundKeyLocalizedText(), gameOptions.keyBack.getBoundKeyLocalizedText(), gameOptions.keyRight.getBoundKeyLocalizedText()}));
         } else if (f == 102.0F) {
            this.client.inGameHud.getChatHud().addMessage(new TranslatableText("demo.help.jump", new Object[]{gameOptions.keyJump.getBoundKeyLocalizedText()}));
         } else if (f == 103.0F) {
            this.client.inGameHud.getChatHud().addMessage(new TranslatableText("demo.help.inventory", new Object[]{gameOptions.keyInventory.getBoundKeyLocalizedText()}));
         } else if (f == 104.0F) {
            this.client.inGameHud.getChatHud().addMessage(new TranslatableText("demo.day.6", new Object[]{gameOptions.keyScreenshot.getBoundKeyLocalizedText()}));
         }
      } else if (reason == GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER) {
         this.world.playSound(playerEntity, playerEntity.getX(), playerEntity.getEyeY(), playerEntity.getZ(), SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.18F, 0.45F);
      } else if (reason == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED) {
         this.world.setRainGradient(f);
      } else if (reason == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
         this.world.setThunderGradient(f);
      } else if (reason == GameStateChangeS2CPacket.PUFFERFISH_STING) {
         this.world.playSound(playerEntity, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_PUFFER_FISH_STING, SoundCategory.NEUTRAL, 1.0F, 1.0F);
      } else if (reason == GameStateChangeS2CPacket.ELDER_GUARDIAN_EFFECT) {
         this.world.addParticle(ParticleTypes.ELDER_GUARDIAN, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), 0.0D, 0.0D, 0.0D);
         if (i == 1) {
            this.world.playSound(playerEntity, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 1.0F, 1.0F);
         }
      } else if (reason == GameStateChangeS2CPacket.IMMEDIATE_RESPAWN) {
         this.client.player.setShowsDeathScreen(f == 0.0F);
      }

   }

   public void onMapUpdate(MapUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      MapRenderer mapRenderer = this.client.gameRenderer.getMapRenderer();
      String string = FilledMapItem.getMapName(packet.getId());
      MapState mapState = this.client.world.getMapState(string);
      if (mapState == null) {
         mapState = new MapState(string);
         if (mapRenderer.getTexture(string) != null) {
            MapState mapState2 = mapRenderer.getState(mapRenderer.getTexture(string));
            if (mapState2 != null) {
               mapState = mapState2;
            }
         }

         this.client.world.putMapState(mapState);
      }

      packet.apply(mapState);
      mapRenderer.updateTexture(mapState);
   }

   public void onWorldEvent(WorldEventS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (packet.isGlobal()) {
         this.client.world.syncGlobalEvent(packet.getEventId(), packet.getPos(), packet.getData());
      } else {
         this.client.world.syncWorldEvent(packet.getEventId(), packet.getPos(), packet.getData());
      }

   }

   public void onAdvancements(AdvancementUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.advancementHandler.onAdvancements(packet);
   }

   public void onSelectAdvancementTab(SelectAdvancementTabS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Identifier identifier = packet.getTabId();
      if (identifier == null) {
         this.advancementHandler.selectTab((Advancement)null, false);
      } else {
         Advancement advancement = this.advancementHandler.getManager().get(identifier);
         this.advancementHandler.selectTab(advancement, false);
      }

   }

   public void onCommandTree(CommandTreeS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.commandDispatcher = new CommandDispatcher(packet.getCommandTree());
   }

   public void onStopSound(StopSoundS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.getSoundManager().stopSounds(packet.getSoundId(), packet.getCategory());
   }

   public void onCommandSuggestions(CommandSuggestionsS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.commandSource.onCommandSuggestions(packet.getCompletionId(), packet.getSuggestions());
   }

   public void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.recipeManager.setRecipes(packet.getRecipes());
      SearchableContainer<RecipeResultCollection> searchableContainer = this.client.getSearchableContainer(SearchManager.RECIPE_OUTPUT);
      searchableContainer.clear();
      ClientRecipeBook clientRecipeBook = this.client.player.getRecipeBook();
      clientRecipeBook.reload(this.recipeManager.values());
      clientRecipeBook.getOrderedResults().forEach(searchableContainer::add);
      searchableContainer.reload();
   }

   public void onLookAt(LookAtS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Vec3d vec3d = packet.getTargetPosition(this.world);
      if (vec3d != null) {
         this.client.player.lookAt(packet.getSelfAnchor(), vec3d);
      }

   }

   public void onTagQuery(TagQueryResponseS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (!this.dataQueryHandler.handleQueryResponse(packet.getTransactionId(), packet.getTag())) {
         LOGGER.debug((String)"Got unhandled response to tag query {}", (Object)packet.getTransactionId());
      }

   }

   public void onStatistics(StatisticsS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Iterator var2 = packet.getStatMap().entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Stat<?>, Integer> entry = (Entry)var2.next();
         Stat<?> stat = (Stat)entry.getKey();
         int i = (Integer)entry.getValue();
         this.client.player.getStatHandler().setStat(this.client.player, stat, i);
      }

      if (this.client.currentScreen instanceof StatsListener) {
         ((StatsListener)this.client.currentScreen).onStatsReady();
      }

   }

   public void onUnlockRecipes(UnlockRecipesS2CPacket packet) {
      ClientRecipeBook clientRecipeBook;
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      clientRecipeBook = this.client.player.getRecipeBook();
      clientRecipeBook.setOptions(packet.getOptions());
      UnlockRecipesS2CPacket.Action action = packet.getAction();
      Iterator var4;
      Identifier identifier3;
      label45:
      switch(action) {
      case REMOVE:
         var4 = packet.getRecipeIdsToChange().iterator();

         while(true) {
            if (!var4.hasNext()) {
               break label45;
            }

            identifier3 = (Identifier)var4.next();
            this.recipeManager.get(identifier3).ifPresent(clientRecipeBook::remove);
         }
      case INIT:
         var4 = packet.getRecipeIdsToChange().iterator();

         while(var4.hasNext()) {
            identifier3 = (Identifier)var4.next();
            this.recipeManager.get(identifier3).ifPresent(clientRecipeBook::add);
         }

         var4 = packet.getRecipeIdsToInit().iterator();

         while(true) {
            if (!var4.hasNext()) {
               break label45;
            }

            identifier3 = (Identifier)var4.next();
            this.recipeManager.get(identifier3).ifPresent(clientRecipeBook::display);
         }
      case ADD:
         var4 = packet.getRecipeIdsToChange().iterator();

         while(var4.hasNext()) {
            identifier3 = (Identifier)var4.next();
            this.recipeManager.get(identifier3).ifPresent((recipe) -> {
               clientRecipeBook.add(recipe);
               clientRecipeBook.display(recipe);
               RecipeToast.show(this.client.getToastManager(), recipe);
            });
         }
      }

      clientRecipeBook.getOrderedResults().forEach((recipeResultCollection) -> {
         recipeResultCollection.initialize(clientRecipeBook);
      });
      if (this.client.currentScreen instanceof RecipeBookProvider) {
         ((RecipeBookProvider)this.client.currentScreen).refreshRecipeBook();
      }

   }

   public void onEntityPotionEffect(EntityStatusEffectS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getEntityId());
      if (entity instanceof LivingEntity) {
         StatusEffect statusEffect = StatusEffect.byRawId(packet.getEffectId());
         if (statusEffect != null) {
            StatusEffectInstance statusEffectInstance = new StatusEffectInstance(statusEffect, packet.getDuration(), packet.getAmplifier(), packet.isAmbient(), packet.shouldShowParticles(), packet.shouldShowIcon());
            statusEffectInstance.setPermanent(packet.isPermanent());
            ((LivingEntity)entity).applyStatusEffect(statusEffectInstance);
         }
      }
   }

   public void onSynchronizeTags(SynchronizeTagsS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      TagManager tagManager = packet.getTagManager();
      Multimap<Identifier, Identifier> multimap = RequiredTagListRegistry.getMissingTags(tagManager);
      if (!multimap.isEmpty()) {
         LOGGER.warn((String)"Incomplete server tags, disconnecting. Missing: {}", (Object)multimap);
         this.connection.disconnect(new TranslatableText("multiplayer.disconnect.missing_tags"));
      } else {
         this.tagManager = tagManager;
         if (!this.connection.isLocal()) {
            tagManager.apply();
         }

         this.client.getSearchableContainer(SearchManager.ITEM_TAG).reload();
      }
   }

   public void onCombatEvent(CombatEventS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (packet.type == CombatEventS2CPacket.Type.ENTITY_DIED) {
         Entity entity = this.world.getEntityById(packet.entityId);
         if (entity == this.client.player) {
            if (this.client.player.showsDeathScreen()) {
               this.client.openScreen(new DeathScreen(packet.deathMessage, this.world.getLevelProperties().isHardcore()));
            } else {
               this.client.player.requestRespawn();
            }
         }
      }

   }

   public void onDifficulty(DifficultyS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.worldProperties.setDifficulty(packet.getDifficulty());
      this.worldProperties.setDifficultyLocked(packet.isDifficultyLocked());
   }

   public void onSetCameraEntity(SetCameraEntityS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = packet.getEntity(this.world);
      if (entity != null) {
         this.client.setCameraEntity(entity);
      }

   }

   public void onWorldBorder(WorldBorderS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      packet.apply(this.world.getWorldBorder());
   }

   public void onTitle(TitleS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      TitleS2CPacket.Action action = packet.getAction();
      Text text = null;
      Text text2 = null;
      Text text3 = packet.getText() != null ? packet.getText() : LiteralText.EMPTY;
      switch(action) {
      case TITLE:
         text = text3;
         break;
      case SUBTITLE:
         text2 = text3;
         break;
      case ACTIONBAR:
         this.client.inGameHud.setOverlayMessage(text3, false);
         return;
      case RESET:
         this.client.inGameHud.setTitles((Text)null, (Text)null, -1, -1, -1);
         this.client.inGameHud.setDefaultTitleFade();
         return;
      }

      this.client.inGameHud.setTitles(text, text2, packet.getFadeInTicks(), packet.getStayTicks(), packet.getFadeOutTicks());
   }

   public void onPlayerListHeader(PlayerListHeaderS2CPacket packet) {
      this.client.inGameHud.getPlayerListHud().setHeader(packet.getHeader().getString().isEmpty() ? null : packet.getHeader());
      this.client.inGameHud.getPlayerListHud().setFooter(packet.getFooter().getString().isEmpty() ? null : packet.getFooter());
   }

   public void onRemoveEntityEffect(RemoveEntityStatusEffectS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = packet.getEntity(this.world);
      if (entity instanceof LivingEntity) {
         ((LivingEntity)entity).removeStatusEffectInternal(packet.getEffectType());
      }

   }

   public void onPlayerList(PlayerListS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Iterator var2 = packet.getEntries().iterator();

      while(var2.hasNext()) {
         PlayerListS2CPacket.Entry entry = (PlayerListS2CPacket.Entry)var2.next();
         if (packet.getAction() == PlayerListS2CPacket.Action.REMOVE_PLAYER) {
            this.client.getSocialInteractionsManager().method_31341(entry.getProfile().getId());
            this.playerListEntries.remove(entry.getProfile().getId());
         } else {
            PlayerListEntry playerListEntry = (PlayerListEntry)this.playerListEntries.get(entry.getProfile().getId());
            if (packet.getAction() == PlayerListS2CPacket.Action.ADD_PLAYER) {
               playerListEntry = new PlayerListEntry(entry);
               this.playerListEntries.put(playerListEntry.getProfile().getId(), playerListEntry);
               this.client.getSocialInteractionsManager().method_31337(playerListEntry);
            }

            if (playerListEntry != null) {
               switch(packet.getAction()) {
               case ADD_PLAYER:
                  playerListEntry.setGameMode(entry.getGameMode());
                  playerListEntry.setLatency(entry.getLatency());
                  playerListEntry.setDisplayName(entry.getDisplayName());
                  break;
               case UPDATE_GAME_MODE:
                  playerListEntry.setGameMode(entry.getGameMode());
                  break;
               case UPDATE_LATENCY:
                  playerListEntry.setLatency(entry.getLatency());
                  break;
               case UPDATE_DISPLAY_NAME:
                  playerListEntry.setDisplayName(entry.getDisplayName());
               }
            }
         }
      }

   }

   public void onKeepAlive(KeepAliveS2CPacket packet) {
      this.sendPacket(new KeepAliveC2SPacket(packet.getId()));
   }

   public void onPlayerAbilities(PlayerAbilitiesS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity playerEntity = this.client.player;
      playerEntity.abilities.flying = packet.isFlying();
      playerEntity.abilities.creativeMode = packet.isCreativeMode();
      playerEntity.abilities.invulnerable = packet.isInvulnerable();
      playerEntity.abilities.allowFlying = packet.allowFlying();
      playerEntity.abilities.setFlySpeed(packet.getFlySpeed());
      playerEntity.abilities.setWalkSpeed(packet.getWalkSpeed());
   }

   public void onPlaySound(PlaySoundS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.playSound(this.client.player, packet.getX(), packet.getY(), packet.getZ(), packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch());
   }

   public void onPlaySoundFromEntity(PlaySoundFromEntityS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getEntityId());
      if (entity != null) {
         this.client.world.playSoundFromEntity(this.client.player, entity, packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch());
      }
   }

   public void onPlaySoundId(PlaySoundIdS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.getSoundManager().play(new PositionedSoundInstance(packet.getSoundId(), packet.getCategory(), packet.getVolume(), packet.getPitch(), false, 0, SoundInstance.AttenuationType.LINEAR, packet.getX(), packet.getY(), packet.getZ(), false));
   }

   public void onResourcePackSend(ResourcePackSendS2CPacket packet) {
      String string = packet.getURL();
      String string2 = packet.getSHA1();
      if (this.validateResourcePackUrl(string)) {
         if (string.startsWith("level://")) {
            try {
               String string3 = URLDecoder.decode(string.substring("level://".length()), StandardCharsets.UTF_8.toString());
               File file = new File(this.client.runDirectory, "saves");
               File file2 = new File(file, string3);
               if (file2.isFile()) {
                  this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
                  CompletableFuture<?> completableFuture = this.client.getResourcePackDownloader().loadServerPack(file2, ResourcePackSource.PACK_SOURCE_WORLD);
                  this.feedbackAfterDownload(completableFuture);
                  return;
               }
            } catch (UnsupportedEncodingException var8) {
            }

            this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD);
         } else {
            ServerInfo serverInfo = this.client.getCurrentServerEntry();
            if (serverInfo != null && serverInfo.getResourcePack() == ServerInfo.ResourcePackState.ENABLED) {
               this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
               this.feedbackAfterDownload(this.client.getResourcePackDownloader().download(string, string2));
            } else if (serverInfo != null && serverInfo.getResourcePack() != ServerInfo.ResourcePackState.PROMPT) {
               this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.DECLINED);
            } else {
               this.client.execute(() -> {
                  this.client.openScreen(new ConfirmScreen((bl) -> {
                     this.client = MinecraftClient.getInstance();
                     ServerInfo serverInfo = this.client.getCurrentServerEntry();
                     if (bl) {
                        if (serverInfo != null) {
                           serverInfo.setResourcePackState(ServerInfo.ResourcePackState.ENABLED);
                        }

                        this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
                        this.feedbackAfterDownload(this.client.getResourcePackDownloader().download(string, string2));
                     } else {
                        if (serverInfo != null) {
                           serverInfo.setResourcePackState(ServerInfo.ResourcePackState.DISABLED);
                        }

                        this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.DECLINED);
                     }

                     ServerList.updateServerListEntry(serverInfo);
                     this.client.openScreen((Screen)null);
                  }, new TranslatableText("multiplayer.texturePrompt.line1"), new TranslatableText("multiplayer.texturePrompt.line2")));
               });
            }

         }
      }
   }

   private boolean validateResourcePackUrl(String url) {
      try {
         URI uRI = new URI(url);
         String string = uRI.getScheme();
         boolean bl = "level".equals(string);
         if (!"http".equals(string) && !"https".equals(string) && !bl) {
            throw new URISyntaxException(url, "Wrong protocol");
         } else if (!bl || !url.contains("..") && url.endsWith("/resources.zip")) {
            return true;
         } else {
            throw new URISyntaxException(url, "Invalid levelstorage resourcepack path");
         }
      } catch (URISyntaxException var5) {
         this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD);
         return false;
      }
   }

   private void feedbackAfterDownload(CompletableFuture<?> downloadFuture) {
      downloadFuture.thenRun(() -> {
         this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED);
      }).exceptionally((throwable) -> {
         this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD);
         return null;
      });
   }

   private void sendResourcePackStatus(ResourcePackStatusC2SPacket.Status packStatus) {
      this.connection.send(new ResourcePackStatusC2SPacket(packStatus));
   }

   public void onBossBar(BossBarS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.inGameHud.getBossBarHud().handlePacket(packet);
   }

   public void onCooldownUpdate(CooldownUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (packet.getCooldown() == 0) {
         this.client.player.getItemCooldownManager().remove(packet.getItem());
      } else {
         this.client.player.getItemCooldownManager().set(packet.getItem(), packet.getCooldown());
      }

   }

   public void onVehicleMove(VehicleMoveS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.client.player.getRootVehicle();
      if (entity != this.client.player && entity.isLogicalSideForUpdatingMovement()) {
         entity.updatePositionAndAngles(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
         this.connection.send(new VehicleMoveC2SPacket(entity));
      }

   }

   public void onOpenWrittenBook(OpenWrittenBookS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      ItemStack itemStack = this.client.player.getStackInHand(packet.getHand());
      if (itemStack.getItem() == Items.WRITTEN_BOOK) {
         this.client.openScreen(new BookScreen(new BookScreen.WrittenBookContents(itemStack)));
      }

   }

   public void onCustomPayload(CustomPayloadS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Identifier identifier = packet.getChannel();
      PacketByteBuf packetByteBuf = null;

      try {
         packetByteBuf = packet.getData();
         if (CustomPayloadS2CPacket.BRAND.equals(identifier)) {
            this.client.player.setServerBrand(packetByteBuf.readString(32767));
         } else {
            int y;
            if (CustomPayloadS2CPacket.DEBUG_PATH.equals(identifier)) {
               y = packetByteBuf.readInt();
               float f = packetByteBuf.readFloat();
               Path path = Path.fromBuffer(packetByteBuf);
               this.client.debugRenderer.pathfindingDebugRenderer.addPath(y, path, f);
            } else if (CustomPayloadS2CPacket.DEBUG_NEIGHBORS_UPDATE.equals(identifier)) {
               long l = packetByteBuf.readVarLong();
               BlockPos blockPos = packetByteBuf.readBlockPos();
               ((NeighborUpdateDebugRenderer)this.client.debugRenderer.neighborUpdateDebugRenderer).addNeighborUpdate(l, blockPos);
            } else {
               ArrayList list5;
               int w;
               BlockPos blockPos13;
               int ba;
               if (CustomPayloadS2CPacket.DEBUG_CAVES.equals(identifier)) {
                  blockPos13 = packetByteBuf.readBlockPos();
                  ba = packetByteBuf.readInt();
                  List<BlockPos> list = Lists.newArrayList();
                  list5 = Lists.newArrayList();

                  for(w = 0; w < ba; ++w) {
                     list.add(packetByteBuf.readBlockPos());
                     list5.add(packetByteBuf.readFloat());
                  }

                  this.client.debugRenderer.caveDebugRenderer.method_3704(blockPos13, list, list5);
               } else {
                  int x;
                  int ay;
                  if (CustomPayloadS2CPacket.DEBUG_STRUCTURES.equals(identifier)) {
                     DimensionType dimensionType = (DimensionType)this.registryManager.getDimensionTypes().get(packetByteBuf.readIdentifier());
                     BlockBox blockBox = new BlockBox(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt());
                     ay = packetByteBuf.readInt();
                     list5 = Lists.newArrayList();
                     List<Boolean> list4 = Lists.newArrayList();

                     for(x = 0; x < ay; ++x) {
                        list5.add(new BlockBox(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt()));
                        list4.add(packetByteBuf.readBoolean());
                     }

                     this.client.debugRenderer.structureDebugRenderer.method_3871(blockBox, list5, list4, dimensionType);
                  } else if (CustomPayloadS2CPacket.DEBUG_WORLDGEN_ATTEMPT.equals(identifier)) {
                     ((WorldGenAttemptDebugRenderer)this.client.debugRenderer.worldGenAttemptDebugRenderer).method_3872(packetByteBuf.readBlockPos(), packetByteBuf.readFloat(), packetByteBuf.readFloat(), packetByteBuf.readFloat(), packetByteBuf.readFloat(), packetByteBuf.readFloat());
                  } else if (CustomPayloadS2CPacket.DEBUG_VILLAGE_SECTIONS.equals(identifier)) {
                     y = packetByteBuf.readInt();

                     for(ba = 0; ba < y; ++ba) {
                        this.client.debugRenderer.villageSectionsDebugRenderer.addSection(packetByteBuf.readChunkSectionPos());
                     }

                     ba = packetByteBuf.readInt();

                     for(ay = 0; ay < ba; ++ay) {
                        this.client.debugRenderer.villageSectionsDebugRenderer.removeSection(packetByteBuf.readChunkSectionPos());
                     }
                  } else {
                     String string11;
                     if (CustomPayloadS2CPacket.DEBUG_POI_ADDED.equals(identifier)) {
                        blockPos13 = packetByteBuf.readBlockPos();
                        string11 = packetByteBuf.readString();
                        ay = packetByteBuf.readInt();
                        VillageDebugRenderer.PointOfInterest pointOfInterest = new VillageDebugRenderer.PointOfInterest(blockPos13, string11, ay);
                        this.client.debugRenderer.villageDebugRenderer.addPointOfInterest(pointOfInterest);
                     } else if (CustomPayloadS2CPacket.DEBUG_POI_REMOVED.equals(identifier)) {
                        blockPos13 = packetByteBuf.readBlockPos();
                        this.client.debugRenderer.villageDebugRenderer.removePointOfInterest(blockPos13);
                     } else if (CustomPayloadS2CPacket.DEBUG_POI_TICKET_COUNT.equals(identifier)) {
                        blockPos13 = packetByteBuf.readBlockPos();
                        ba = packetByteBuf.readInt();
                        this.client.debugRenderer.villageDebugRenderer.setFreeTicketCount(blockPos13, ba);
                     } else if (CustomPayloadS2CPacket.DEBUG_GOAL_SELECTOR.equals(identifier)) {
                        blockPos13 = packetByteBuf.readBlockPos();
                        ba = packetByteBuf.readInt();
                        ay = packetByteBuf.readInt();
                        list5 = Lists.newArrayList();

                        for(w = 0; w < ay; ++w) {
                           x = packetByteBuf.readInt();
                           boolean bl = packetByteBuf.readBoolean();
                           String string2 = packetByteBuf.readString(255);
                           list5.add(new GoalSelectorDebugRenderer.GoalSelector(blockPos13, x, string2, bl));
                        }

                        this.client.debugRenderer.goalSelectorDebugRenderer.setGoalSelectorList(ba, list5);
                     } else if (CustomPayloadS2CPacket.DEBUG_RAIDS.equals(identifier)) {
                        y = packetByteBuf.readInt();
                        Collection<BlockPos> collection = Lists.newArrayList();

                        for(ay = 0; ay < y; ++ay) {
                           collection.add(packetByteBuf.readBlockPos());
                        }

                        this.client.debugRenderer.raidCenterDebugRenderer.setRaidCenters(collection);
                     } else {
                        int as;
                        int ax;
                        double ap;
                        double aq;
                        double ar;
                        PositionImpl position2;
                        UUID uUID2;
                        if (CustomPayloadS2CPacket.DEBUG_BRAIN.equals(identifier)) {
                           ap = packetByteBuf.readDouble();
                           aq = packetByteBuf.readDouble();
                           ar = packetByteBuf.readDouble();
                           position2 = new PositionImpl(ap, aq, ar);
                           uUID2 = packetByteBuf.readUuid();
                           as = packetByteBuf.readInt();
                           String string3 = packetByteBuf.readString();
                           String string4 = packetByteBuf.readString();
                           int ab = packetByteBuf.readInt();
                           float h = packetByteBuf.readFloat();
                           float ac = packetByteBuf.readFloat();
                           String string5 = packetByteBuf.readString();
                           boolean bl2 = packetByteBuf.readBoolean();
                           Path path3;
                           if (bl2) {
                              path3 = Path.fromBuffer(packetByteBuf);
                           } else {
                              path3 = null;
                           }

                           boolean bl3 = packetByteBuf.readBoolean();
                           VillageDebugRenderer.Brain brain = new VillageDebugRenderer.Brain(uUID2, as, string3, string4, ab, h, ac, position2, string5, path3, bl3);
                           ax = packetByteBuf.readInt();

                           int af;
                           for(af = 0; af < ax; ++af) {
                              String string6 = packetByteBuf.readString();
                              brain.field_18927.add(string6);
                           }

                           af = packetByteBuf.readInt();

                           int ah;
                           for(ah = 0; ah < af; ++ah) {
                              String string7 = packetByteBuf.readString();
                              brain.field_18928.add(string7);
                           }

                           ah = packetByteBuf.readInt();

                           int aj;
                           for(aj = 0; aj < ah; ++aj) {
                              String string8 = packetByteBuf.readString();
                              brain.field_19374.add(string8);
                           }

                           aj = packetByteBuf.readInt();

                           int al;
                           for(al = 0; al < aj; ++al) {
                              BlockPos blockPos7 = packetByteBuf.readBlockPos();
                              brain.pointsOfInterest.add(blockPos7);
                           }

                           al = packetByteBuf.readInt();

                           int an;
                           for(an = 0; an < al; ++an) {
                              BlockPos blockPos8 = packetByteBuf.readBlockPos();
                              brain.field_25287.add(blockPos8);
                           }

                           an = packetByteBuf.readInt();

                           for(int ao = 0; ao < an; ++ao) {
                              String string9 = packetByteBuf.readString();
                              brain.field_19375.add(string9);
                           }

                           this.client.debugRenderer.villageDebugRenderer.addBrain(brain);
                        } else if (CustomPayloadS2CPacket.DEBUG_BEE.equals(identifier)) {
                           ap = packetByteBuf.readDouble();
                           aq = packetByteBuf.readDouble();
                           ar = packetByteBuf.readDouble();
                           position2 = new PositionImpl(ap, aq, ar);
                           uUID2 = packetByteBuf.readUuid();
                           as = packetByteBuf.readInt();
                           boolean bl4 = packetByteBuf.readBoolean();
                           BlockPos blockPos9 = null;
                           if (bl4) {
                              blockPos9 = packetByteBuf.readBlockPos();
                           }

                           boolean bl5 = packetByteBuf.readBoolean();
                           BlockPos blockPos10 = null;
                           if (bl5) {
                              blockPos10 = packetByteBuf.readBlockPos();
                           }

                           int at = packetByteBuf.readInt();
                           boolean bl6 = packetByteBuf.readBoolean();
                           Path path4 = null;
                           if (bl6) {
                              path4 = Path.fromBuffer(packetByteBuf);
                           }

                           BeeDebugRenderer.Bee bee = new BeeDebugRenderer.Bee(uUID2, as, position2, path4, blockPos9, blockPos10, at);
                           int au = packetByteBuf.readInt();

                           int aw;
                           for(aw = 0; aw < au; ++aw) {
                              String string10 = packetByteBuf.readString();
                              bee.labels.add(string10);
                           }

                           aw = packetByteBuf.readInt();

                           for(ax = 0; ax < aw; ++ax) {
                              BlockPos blockPos11 = packetByteBuf.readBlockPos();
                              bee.blacklist.add(blockPos11);
                           }

                           this.client.debugRenderer.beeDebugRenderer.addBee(bee);
                        } else {
                           int bb;
                           if (CustomPayloadS2CPacket.DEBUG_HIVE.equals(identifier)) {
                              blockPos13 = packetByteBuf.readBlockPos();
                              string11 = packetByteBuf.readString();
                              ay = packetByteBuf.readInt();
                              bb = packetByteBuf.readInt();
                              boolean bl7 = packetByteBuf.readBoolean();
                              BeeDebugRenderer.Hive hive = new BeeDebugRenderer.Hive(blockPos13, string11, ay, bb, bl7, this.world.getTime());
                              this.client.debugRenderer.beeDebugRenderer.addHive(hive);
                           } else if (CustomPayloadS2CPacket.DEBUG_GAME_TEST_CLEAR.equals(identifier)) {
                              this.client.debugRenderer.gameTestDebugRenderer.clear();
                           } else if (CustomPayloadS2CPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(identifier)) {
                              blockPos13 = packetByteBuf.readBlockPos();
                              ba = packetByteBuf.readInt();
                              String string12 = packetByteBuf.readString();
                              bb = packetByteBuf.readInt();
                              this.client.debugRenderer.gameTestDebugRenderer.addMarker(blockPos13, ba, string12, bb);
                           } else {
                              LOGGER.warn((String)"Unknown custom packed identifier: {}", (Object)identifier);
                           }
                        }
                     }
                  }
               }
            }
         }
      } finally {
         if (packetByteBuf != null) {
            packetByteBuf.release();
         }

      }

   }

   public void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Scoreboard scoreboard = this.world.getScoreboard();
      String string = packet.getName();
      if (packet.getMode() == 0) {
         scoreboard.addObjective(string, ScoreboardCriterion.DUMMY, packet.getDisplayName(), packet.getType());
      } else if (scoreboard.containsObjective(string)) {
         ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(string);
         if (packet.getMode() == 1) {
            scoreboard.removeObjective(scoreboardObjective);
         } else if (packet.getMode() == 2) {
            scoreboardObjective.setRenderType(packet.getType());
            scoreboardObjective.setDisplayName(packet.getDisplayName());
         }
      }

   }

   public void onScoreboardPlayerUpdate(ScoreboardPlayerUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Scoreboard scoreboard = this.world.getScoreboard();
      String string = packet.getObjectiveName();
      switch(packet.getUpdateMode()) {
      case CHANGE:
         ScoreboardObjective scoreboardObjective = scoreboard.getObjective(string);
         ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(packet.getPlayerName(), scoreboardObjective);
         scoreboardPlayerScore.setScore(packet.getScore());
         break;
      case REMOVE:
         scoreboard.resetPlayerScore(packet.getPlayerName(), scoreboard.getNullableObjective(string));
      }

   }

   public void onScoreboardDisplay(ScoreboardDisplayS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Scoreboard scoreboard = this.world.getScoreboard();
      String string = packet.getName();
      ScoreboardObjective scoreboardObjective = string == null ? null : scoreboard.getObjective(string);
      scoreboard.setObjectiveSlot(packet.getSlot(), scoreboardObjective);
   }

   public void onTeam(TeamS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Scoreboard scoreboard = this.world.getScoreboard();
      Team team2;
      if (packet.getMode() == 0) {
         team2 = scoreboard.addTeam(packet.getTeamName());
      } else {
         team2 = scoreboard.getTeam(packet.getTeamName());
      }

      if (packet.getMode() == 0 || packet.getMode() == 2) {
         team2.setDisplayName(packet.getDisplayName());
         team2.setColor(packet.getPlayerPrefix());
         team2.setFriendlyFlagsBitwise(packet.getFlags());
         AbstractTeam.VisibilityRule visibilityRule = AbstractTeam.VisibilityRule.getRule(packet.getNameTagVisibilityRule());
         if (visibilityRule != null) {
            team2.setNameTagVisibilityRule(visibilityRule);
         }

         AbstractTeam.CollisionRule collisionRule = AbstractTeam.CollisionRule.getRule(packet.getCollisionRule());
         if (collisionRule != null) {
            team2.setCollisionRule(collisionRule);
         }

         team2.setPrefix(packet.getPrefix());
         team2.setSuffix(packet.getSuffix());
      }

      Iterator var6;
      String string2;
      if (packet.getMode() == 0 || packet.getMode() == 3) {
         var6 = packet.getPlayerList().iterator();

         while(var6.hasNext()) {
            string2 = (String)var6.next();
            scoreboard.addPlayerToTeam(string2, team2);
         }
      }

      if (packet.getMode() == 4) {
         var6 = packet.getPlayerList().iterator();

         while(var6.hasNext()) {
            string2 = (String)var6.next();
            scoreboard.removePlayerFromTeam(string2, team2);
         }
      }

      if (packet.getMode() == 1) {
         scoreboard.removeTeam(team2);
      }

   }

   public void onParticle(ParticleS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (packet.getCount() == 0) {
         double d = (double)(packet.getSpeed() * packet.getOffsetX());
         double e = (double)(packet.getSpeed() * packet.getOffsetY());
         double f = (double)(packet.getSpeed() * packet.getOffsetZ());

         try {
            this.world.addParticle(packet.getParameters(), packet.isLongDistance(), packet.getX(), packet.getY(), packet.getZ(), d, e, f);
         } catch (Throwable var17) {
            LOGGER.warn((String)"Could not spawn particle effect {}", (Object)packet.getParameters());
         }
      } else {
         for(int i = 0; i < packet.getCount(); ++i) {
            double g = this.random.nextGaussian() * (double)packet.getOffsetX();
            double h = this.random.nextGaussian() * (double)packet.getOffsetY();
            double j = this.random.nextGaussian() * (double)packet.getOffsetZ();
            double k = this.random.nextGaussian() * (double)packet.getSpeed();
            double l = this.random.nextGaussian() * (double)packet.getSpeed();
            double m = this.random.nextGaussian() * (double)packet.getSpeed();

            try {
               this.world.addParticle(packet.getParameters(), packet.isLongDistance(), packet.getX() + g, packet.getY() + h, packet.getZ() + j, k, l, m);
            } catch (Throwable var16) {
               LOGGER.warn((String)"Could not spawn particle effect {}", (Object)packet.getParameters());
               return;
            }
         }
      }

   }

   public void onEntityAttributes(EntityAttributesS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity entity = this.world.getEntityById(packet.getEntityId());
      if (entity != null) {
         if (!(entity instanceof LivingEntity)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
         } else {
            AttributeContainer attributeContainer = ((LivingEntity)entity).getAttributes();
            Iterator var4 = packet.getEntries().iterator();

            while(true) {
               while(var4.hasNext()) {
                  EntityAttributesS2CPacket.Entry entry = (EntityAttributesS2CPacket.Entry)var4.next();
                  EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(entry.getId());
                  if (entityAttributeInstance == null) {
                     LOGGER.warn((String)"Entity {} does not have attribute {}", (Object)entity, (Object)Registry.ATTRIBUTE.getId(entry.getId()));
                  } else {
                     entityAttributeInstance.setBaseValue(entry.getBaseValue());
                     entityAttributeInstance.clearModifiers();
                     Iterator var7 = entry.getModifiers().iterator();

                     while(var7.hasNext()) {
                        EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)var7.next();
                        entityAttributeInstance.addTemporaryModifier(entityAttributeModifier);
                     }
                  }
               }

               return;
            }
         }
      }
   }

   public void onCraftFailedResponse(CraftFailedResponseS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      ScreenHandler screenHandler = this.client.player.currentScreenHandler;
      if (screenHandler.syncId == packet.getSyncId() && screenHandler.isNotRestricted(this.client.player)) {
         this.recipeManager.get(packet.getRecipeId()).ifPresent((recipe) -> {
            if (this.client.currentScreen instanceof RecipeBookProvider) {
               RecipeBookWidget recipeBookWidget = ((RecipeBookProvider)this.client.currentScreen).getRecipeBookWidget();
               recipeBookWidget.showGhostRecipe(recipe, screenHandler.slots);
            }

         });
      }
   }

   public void onLightUpdate(LightUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      int i = packet.getChunkX();
      int j = packet.getChunkZ();
      LightingProvider lightingProvider = this.world.getChunkManager().getLightingProvider();
      int k = packet.getSkyLightMask();
      int l = packet.getFilledSkyLightMask();
      Iterator<byte[]> iterator = packet.getSkyLightUpdates().iterator();
      this.updateLighting(i, j, lightingProvider, LightType.SKY, k, l, iterator, packet.method_30006());
      int m = packet.getBlockLightMask();
      int n = packet.getFilledBlockLightMask();
      Iterator<byte[]> iterator2 = packet.getBlockLightUpdates().iterator();
      this.updateLighting(i, j, lightingProvider, LightType.BLOCK, m, n, iterator2, packet.method_30006());
   }

   public void onSetTradeOffers(SetTradeOffersS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      ScreenHandler screenHandler = this.client.player.currentScreenHandler;
      if (packet.getSyncId() == screenHandler.syncId && screenHandler instanceof MerchantScreenHandler) {
         ((MerchantScreenHandler)screenHandler).setOffers(new TradeOfferList(packet.getOffers().toTag()));
         ((MerchantScreenHandler)screenHandler).setExperienceFromServer(packet.getExperience());
         ((MerchantScreenHandler)screenHandler).setLevelProgress(packet.getLevelProgress());
         ((MerchantScreenHandler)screenHandler).setCanLevel(packet.isLeveled());
         ((MerchantScreenHandler)screenHandler).setRefreshTrades(packet.isRefreshable());
      }

   }

   public void onChunkLoadDistance(ChunkLoadDistanceS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.chunkLoadDistance = packet.getDistance();
      this.world.getChunkManager().updateLoadDistance(packet.getDistance());
   }

   public void onChunkRenderDistanceCenter(ChunkRenderDistanceCenterS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.getChunkManager().setChunkMapCenter(packet.getChunkX(), packet.getChunkZ());
   }

   public void onPlayerActionResponse(PlayerActionResponseS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.interactionManager.processPlayerActionResponse(this.world, packet.getBlockPos(), packet.getBlockState(), packet.getAction(), packet.isApproved());
   }

   private void updateLighting(int chunkX, int chunkZ, LightingProvider provider, LightType type, int mask, int filledMask, Iterator<byte[]> updates, boolean bl) {
      for(int i = 0; i < 18; ++i) {
         int j = -1 + i;
         boolean bl2 = (mask & 1 << i) != 0;
         boolean bl3 = (filledMask & 1 << i) != 0;
         if (bl2 || bl3) {
            provider.enqueueSectionData(type, ChunkSectionPos.from(chunkX, j, chunkZ), bl2 ? new ChunkNibbleArray((byte[])((byte[])updates.next()).clone()) : new ChunkNibbleArray(), bl);
            this.world.scheduleBlockRenders(chunkX, j, chunkZ);
         }
      }

   }

   public ClientConnection getConnection() {
      return this.connection;
   }

   public Collection<PlayerListEntry> getPlayerList() {
      return this.playerListEntries.values();
   }

   public Collection<UUID> getPlayerUuids() {
      return this.playerListEntries.keySet();
   }

   @Nullable
   public PlayerListEntry getPlayerListEntry(UUID uuid) {
      return (PlayerListEntry)this.playerListEntries.get(uuid);
   }

   @Nullable
   public PlayerListEntry getPlayerListEntry(String profileName) {
      Iterator var2 = this.playerListEntries.values().iterator();

      PlayerListEntry playerListEntry;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         playerListEntry = (PlayerListEntry)var2.next();
      } while(!playerListEntry.getProfile().getName().equals(profileName));

      return playerListEntry;
   }

   public GameProfile getProfile() {
      return this.profile;
   }

   public ClientAdvancementManager getAdvancementHandler() {
      return this.advancementHandler;
   }

   public CommandDispatcher<CommandSource> getCommandDispatcher() {
      return this.commandDispatcher;
   }

   public ClientWorld getWorld() {
      return this.world;
   }

   public TagManager getTagManager() {
      return this.tagManager;
   }

   public DataQueryHandler getDataQueryHandler() {
      return this.dataQueryHandler;
   }

   public UUID getSessionId() {
      return this.sessionId;
   }

   public Set<RegistryKey<World>> getWorldKeys() {
      return this.worldKeys;
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.registryManager;
   }
}
