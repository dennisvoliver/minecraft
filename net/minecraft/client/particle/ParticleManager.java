package net.minecraft.client.particle;

import com.google.common.base.Charsets;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ParticleManager implements ResourceReloadListener {
   private static final List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS;
   protected ClientWorld world;
   private final Map<ParticleTextureSheet, Queue<Particle>> particles = Maps.newIdentityHashMap();
   private final Queue<EmitterParticle> newEmitterParticles = Queues.newArrayDeque();
   private final TextureManager textureManager;
   private final Random random = new Random();
   private final Int2ObjectMap<ParticleFactory<?>> factories = new Int2ObjectOpenHashMap();
   private final Queue<Particle> newParticles = Queues.newArrayDeque();
   private final Map<Identifier, ParticleManager.SimpleSpriteProvider> spriteAwareFactories = Maps.newHashMap();
   private final SpriteAtlasTexture particleAtlasTexture;

   public ParticleManager(ClientWorld world, TextureManager textureManager) {
      this.particleAtlasTexture = new SpriteAtlasTexture(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
      textureManager.registerTexture(this.particleAtlasTexture.getId(), this.particleAtlasTexture);
      this.world = world;
      this.textureManager = textureManager;
      this.registerDefaultFactories();
   }

   private void registerDefaultFactories() {
      this.registerFactory(ParticleTypes.AMBIENT_ENTITY_EFFECT, (ParticleManager.SpriteAwareFactory)(SpellParticle.EntityAmbientFactory::new));
      this.registerFactory(ParticleTypes.ANGRY_VILLAGER, (ParticleManager.SpriteAwareFactory)(EmotionParticle.AngryVillagerFactory::new));
      this.registerFactory(ParticleTypes.BARRIER, (ParticleFactory)(new BarrierParticle.Factory()));
      this.registerFactory(ParticleTypes.BLOCK, (ParticleFactory)(new BlockDustParticle.Factory()));
      this.registerFactory(ParticleTypes.BUBBLE, (ParticleManager.SpriteAwareFactory)(WaterBubbleParticle.Factory::new));
      this.registerFactory(ParticleTypes.BUBBLE_COLUMN_UP, (ParticleManager.SpriteAwareFactory)(BubbleColumnUpParticle.Factory::new));
      this.registerFactory(ParticleTypes.BUBBLE_POP, (ParticleManager.SpriteAwareFactory)(BubblePopParticle.Factory::new));
      this.registerFactory(ParticleTypes.CAMPFIRE_COSY_SMOKE, (ParticleManager.SpriteAwareFactory)(CampfireSmokeParticle.CosySmokeFactory::new));
      this.registerFactory(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, (ParticleManager.SpriteAwareFactory)(CampfireSmokeParticle.SignalSmokeFactory::new));
      this.registerFactory(ParticleTypes.CLOUD, (ParticleManager.SpriteAwareFactory)(CloudParticle.CloudFactory::new));
      this.registerFactory(ParticleTypes.COMPOSTER, (ParticleManager.SpriteAwareFactory)(SuspendParticle.Factory::new));
      this.registerFactory(ParticleTypes.CRIT, (ParticleManager.SpriteAwareFactory)(DamageParticle.Factory::new));
      this.registerFactory(ParticleTypes.CURRENT_DOWN, (ParticleManager.SpriteAwareFactory)(CurrentDownParticle.Factory::new));
      this.registerFactory(ParticleTypes.DAMAGE_INDICATOR, (ParticleManager.SpriteAwareFactory)(DamageParticle.DefaultFactory::new));
      this.registerFactory(ParticleTypes.DRAGON_BREATH, (ParticleManager.SpriteAwareFactory)(DragonBreathParticle.Factory::new));
      this.registerFactory(ParticleTypes.DOLPHIN, (ParticleManager.SpriteAwareFactory)(SuspendParticle.DolphinFactory::new));
      this.registerFactory(ParticleTypes.DRIPPING_LAVA, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.DrippingLavaFactory::new));
      this.registerFactory(ParticleTypes.FALLING_LAVA, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.FallingLavaFactory::new));
      this.registerFactory(ParticleTypes.LANDING_LAVA, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.LandingLavaFactory::new));
      this.registerFactory(ParticleTypes.DRIPPING_WATER, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.DrippingWaterFactory::new));
      this.registerFactory(ParticleTypes.FALLING_WATER, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.FallingWaterFactory::new));
      this.registerFactory(ParticleTypes.DUST, RedDustParticle.Factory::new);
      this.registerFactory(ParticleTypes.EFFECT, (ParticleManager.SpriteAwareFactory)(SpellParticle.DefaultFactory::new));
      this.registerFactory(ParticleTypes.ELDER_GUARDIAN, (ParticleFactory)(new ElderGuardianAppearanceParticle.Factory()));
      this.registerFactory(ParticleTypes.ENCHANTED_HIT, (ParticleManager.SpriteAwareFactory)(DamageParticle.EnchantedHitFactory::new));
      this.registerFactory(ParticleTypes.ENCHANT, (ParticleManager.SpriteAwareFactory)(EnchantGlyphParticle.EnchantFactory::new));
      this.registerFactory(ParticleTypes.END_ROD, (ParticleManager.SpriteAwareFactory)(EndRodParticle.Factory::new));
      this.registerFactory(ParticleTypes.ENTITY_EFFECT, (ParticleManager.SpriteAwareFactory)(SpellParticle.EntityFactory::new));
      this.registerFactory(ParticleTypes.EXPLOSION_EMITTER, (ParticleFactory)(new ExplosionEmitterParticle.Factory()));
      this.registerFactory(ParticleTypes.EXPLOSION, (ParticleManager.SpriteAwareFactory)(ExplosionLargeParticle.Factory::new));
      this.registerFactory(ParticleTypes.FALLING_DUST, BlockFallingDustParticle.Factory::new);
      this.registerFactory(ParticleTypes.FIREWORK, (ParticleManager.SpriteAwareFactory)(FireworksSparkParticle.ExplosionFactory::new));
      this.registerFactory(ParticleTypes.FISHING, (ParticleManager.SpriteAwareFactory)(FishingParticle.Factory::new));
      this.registerFactory(ParticleTypes.FLAME, (ParticleManager.SpriteAwareFactory)(FlameParticle.Factory::new));
      this.registerFactory(ParticleTypes.SOUL, (ParticleManager.SpriteAwareFactory)(SoulParticle.Factory::new));
      this.registerFactory(ParticleTypes.SOUL_FIRE_FLAME, (ParticleManager.SpriteAwareFactory)(FlameParticle.Factory::new));
      this.registerFactory(ParticleTypes.FLASH, (ParticleManager.SpriteAwareFactory)(FireworksSparkParticle.FlashFactory::new));
      this.registerFactory(ParticleTypes.HAPPY_VILLAGER, (ParticleManager.SpriteAwareFactory)(SuspendParticle.HappyVillagerFactory::new));
      this.registerFactory(ParticleTypes.HEART, (ParticleManager.SpriteAwareFactory)(EmotionParticle.HeartFactory::new));
      this.registerFactory(ParticleTypes.INSTANT_EFFECT, (ParticleManager.SpriteAwareFactory)(SpellParticle.InstantFactory::new));
      this.registerFactory(ParticleTypes.ITEM, (ParticleFactory)(new CrackParticle.ItemFactory()));
      this.registerFactory(ParticleTypes.ITEM_SLIME, (ParticleFactory)(new CrackParticle.SlimeballFactory()));
      this.registerFactory(ParticleTypes.ITEM_SNOWBALL, (ParticleFactory)(new CrackParticle.SnowballFactory()));
      this.registerFactory(ParticleTypes.LARGE_SMOKE, (ParticleManager.SpriteAwareFactory)(LargeFireSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.LAVA, (ParticleManager.SpriteAwareFactory)(LavaEmberParticle.Factory::new));
      this.registerFactory(ParticleTypes.MYCELIUM, (ParticleManager.SpriteAwareFactory)(SuspendParticle.MyceliumFactory::new));
      this.registerFactory(ParticleTypes.NAUTILUS, (ParticleManager.SpriteAwareFactory)(EnchantGlyphParticle.NautilusFactory::new));
      this.registerFactory(ParticleTypes.NOTE, (ParticleManager.SpriteAwareFactory)(NoteParticle.Factory::new));
      this.registerFactory(ParticleTypes.POOF, (ParticleManager.SpriteAwareFactory)(ExplosionSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.PORTAL, (ParticleManager.SpriteAwareFactory)(PortalParticle.Factory::new));
      this.registerFactory(ParticleTypes.RAIN, (ParticleManager.SpriteAwareFactory)(RainSplashParticle.Factory::new));
      this.registerFactory(ParticleTypes.SMOKE, (ParticleManager.SpriteAwareFactory)(FireSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.SNEEZE, (ParticleManager.SpriteAwareFactory)(CloudParticle.SneezeFactory::new));
      this.registerFactory(ParticleTypes.SPIT, (ParticleManager.SpriteAwareFactory)(SpitParticle.Factory::new));
      this.registerFactory(ParticleTypes.SWEEP_ATTACK, (ParticleManager.SpriteAwareFactory)(SweepAttackParticle.Factory::new));
      this.registerFactory(ParticleTypes.TOTEM_OF_UNDYING, (ParticleManager.SpriteAwareFactory)(TotemParticle.Factory::new));
      this.registerFactory(ParticleTypes.SQUID_INK, (ParticleManager.SpriteAwareFactory)(SquidInkParticle.Factory::new));
      this.registerFactory(ParticleTypes.UNDERWATER, (ParticleManager.SpriteAwareFactory)(WaterSuspendParticle.UnderwaterFactory::new));
      this.registerFactory(ParticleTypes.SPLASH, (ParticleManager.SpriteAwareFactory)(WaterSplashParticle.SplashFactory::new));
      this.registerFactory(ParticleTypes.WITCH, (ParticleManager.SpriteAwareFactory)(SpellParticle.WitchFactory::new));
      this.registerFactory(ParticleTypes.DRIPPING_HONEY, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.DrippingHoneyFactory::new));
      this.registerFactory(ParticleTypes.FALLING_HONEY, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.FallingHoneyFactory::new));
      this.registerFactory(ParticleTypes.LANDING_HONEY, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.LandingHoneyFactory::new));
      this.registerFactory(ParticleTypes.FALLING_NECTAR, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.FallingNectarFactory::new));
      this.registerFactory(ParticleTypes.ASH, (ParticleManager.SpriteAwareFactory)(AshParticle.Factory::new));
      this.registerFactory(ParticleTypes.CRIMSON_SPORE, (ParticleManager.SpriteAwareFactory)(WaterSuspendParticle.CrimsonSporeFactory::new));
      this.registerFactory(ParticleTypes.WARPED_SPORE, (ParticleManager.SpriteAwareFactory)(WaterSuspendParticle.WarpedSporeFactory::new));
      this.registerFactory(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.DrippingObsidianTearFactory::new));
      this.registerFactory(ParticleTypes.FALLING_OBSIDIAN_TEAR, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.FallingObsidianTearFactory::new));
      this.registerFactory(ParticleTypes.LANDING_OBSIDIAN_TEAR, (ParticleManager.SpriteAwareFactory)(BlockLeakParticle.LandingObsidianTearFactory::new));
      this.registerFactory(ParticleTypes.REVERSE_PORTAL, (ParticleManager.SpriteAwareFactory)(ReversePortalParticle.Factory::new));
      this.registerFactory(ParticleTypes.WHITE_ASH, (ParticleManager.SpriteAwareFactory)(WhiteAshParticle.Factory::new));
   }

   private <T extends ParticleEffect> void registerFactory(ParticleType<T> type, ParticleFactory<T> factory) {
      this.factories.put(Registry.PARTICLE_TYPE.getRawId(type), factory);
   }

   private <T extends ParticleEffect> void registerFactory(ParticleType<T> particleType, ParticleManager.SpriteAwareFactory<T> spriteAwareFactory) {
      ParticleManager.SimpleSpriteProvider simpleSpriteProvider = new ParticleManager.SimpleSpriteProvider();
      this.spriteAwareFactories.put(Registry.PARTICLE_TYPE.getId(particleType), simpleSpriteProvider);
      this.factories.put(Registry.PARTICLE_TYPE.getRawId(particleType), spriteAwareFactory.create(simpleSpriteProvider));
   }

   public CompletableFuture<Void> reload(ResourceReloadListener.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      Map<Identifier, List<Identifier>> map = Maps.newConcurrentMap();
      CompletableFuture<?>[] completableFutures = (CompletableFuture[])Registry.PARTICLE_TYPE.getIds().stream().map((identifier) -> {
         return CompletableFuture.runAsync(() -> {
            this.loadTextureList(manager, identifier, map);
         }, prepareExecutor);
      }).toArray((i) -> {
         return new CompletableFuture[i];
      });
      CompletableFuture var10000 = CompletableFuture.allOf(completableFutures).thenApplyAsync((void_) -> {
         prepareProfiler.startTick();
         prepareProfiler.push("stitching");
         SpriteAtlasTexture.Data data = this.particleAtlasTexture.stitch(manager, map.values().stream().flatMap(Collection::stream), prepareProfiler, 0);
         prepareProfiler.pop();
         prepareProfiler.endTick();
         return data;
      }, prepareExecutor);
      synchronizer.getClass();
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((data) -> {
         this.particles.clear();
         applyProfiler.startTick();
         applyProfiler.push("upload");
         this.particleAtlasTexture.upload(data);
         applyProfiler.swap("bindSpriteSets");
         Sprite sprite = this.particleAtlasTexture.getSprite(MissingSprite.getMissingSpriteId());
         map.forEach((identifier, list) -> {
            ImmutableList var10000;
            if (list.isEmpty()) {
               var10000 = ImmutableList.of(sprite);
            } else {
               Stream var5 = list.stream();
               SpriteAtlasTexture var10001 = this.particleAtlasTexture;
               var10001.getClass();
               var10000 = (ImmutableList)var5.map(var10001::getSprite).collect(ImmutableList.toImmutableList());
            }

            ImmutableList<Sprite> immutableList = var10000;
            ((ParticleManager.SimpleSpriteProvider)this.spriteAwareFactories.get(identifier)).setSprites(immutableList);
         });
         applyProfiler.pop();
         applyProfiler.endTick();
      }, applyExecutor);
   }

   public void clearAtlas() {
      this.particleAtlasTexture.clear();
   }

   private void loadTextureList(ResourceManager resourceManager, Identifier id, Map<Identifier, List<Identifier>> result) {
      Identifier identifier = new Identifier(id.getNamespace(), "particles/" + id.getPath() + ".json");

      try {
         Resource resource = resourceManager.getResource(identifier);
         Throwable var6 = null;

         try {
            Reader reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
            Throwable var8 = null;

            try {
               ParticleTextureData particleTextureData = ParticleTextureData.load(JsonHelper.deserialize((Reader)reader));
               List<Identifier> list = particleTextureData.getTextureList();
               boolean bl = this.spriteAwareFactories.containsKey(id);
               if (list == null) {
                  if (bl) {
                     throw new IllegalStateException("Missing texture list for particle " + id);
                  }
               } else {
                  if (!bl) {
                     throw new IllegalStateException("Redundant texture list for particle " + id);
                  }

                  result.put(id, list.stream().map((identifierx) -> {
                     return new Identifier(identifierx.getNamespace(), "particle/" + identifierx.getPath());
                  }).collect(Collectors.toList()));
               }
            } catch (Throwable var35) {
               var8 = var35;
               throw var35;
            } finally {
               if (reader != null) {
                  if (var8 != null) {
                     try {
                        reader.close();
                     } catch (Throwable var34) {
                        var8.addSuppressed(var34);
                     }
                  } else {
                     reader.close();
                  }
               }

            }
         } catch (Throwable var37) {
            var6 = var37;
            throw var37;
         } finally {
            if (resource != null) {
               if (var6 != null) {
                  try {
                     resource.close();
                  } catch (Throwable var33) {
                     var6.addSuppressed(var33);
                  }
               } else {
                  resource.close();
               }
            }

         }

      } catch (IOException var39) {
         throw new IllegalStateException("Failed to load description for particle " + id, var39);
      }
   }

   public void addEmitter(Entity entity, ParticleEffect parameters) {
      this.newEmitterParticles.add(new EmitterParticle(this.world, entity, parameters));
   }

   public void addEmitter(Entity entity, ParticleEffect parameters, int maxAge) {
      this.newEmitterParticles.add(new EmitterParticle(this.world, entity, parameters, maxAge));
   }

   @Nullable
   public Particle addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      Particle particle = this.createParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
      if (particle != null) {
         this.addParticle(particle);
         return particle;
      } else {
         return null;
      }
   }

   @Nullable
   private <T extends ParticleEffect> Particle createParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      ParticleFactory<T> particleFactory = (ParticleFactory)this.factories.get(Registry.PARTICLE_TYPE.getRawId(parameters.getType()));
      return particleFactory == null ? null : particleFactory.createParticle(parameters, this.world, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addParticle(Particle particle) {
      this.newParticles.add(particle);
   }

   public void tick() {
      this.particles.forEach((particleTextureSheet, queue) -> {
         this.world.getProfiler().push(particleTextureSheet.toString());
         this.tickParticles(queue);
         this.world.getProfiler().pop();
      });
      if (!this.newEmitterParticles.isEmpty()) {
         List<EmitterParticle> list = Lists.newArrayList();
         Iterator var2 = this.newEmitterParticles.iterator();

         while(var2.hasNext()) {
            EmitterParticle emitterParticle = (EmitterParticle)var2.next();
            emitterParticle.tick();
            if (!emitterParticle.isAlive()) {
               list.add(emitterParticle);
            }
         }

         this.newEmitterParticles.removeAll(list);
      }

      Particle particle;
      if (!this.newParticles.isEmpty()) {
         while((particle = (Particle)this.newParticles.poll()) != null) {
            ((Queue)this.particles.computeIfAbsent(particle.getType(), (particleTextureSheet) -> {
               return EvictingQueue.create(16384);
            })).add(particle);
         }
      }

   }

   private void tickParticles(Collection<Particle> collection) {
      if (!collection.isEmpty()) {
         Iterator iterator = collection.iterator();

         while(iterator.hasNext()) {
            Particle particle = (Particle)iterator.next();
            this.tickParticle(particle);
            if (!particle.isAlive()) {
               iterator.remove();
            }
         }
      }

   }

   private void tickParticle(Particle particle) {
      try {
         particle.tick();
      } catch (Throwable var5) {
         CrashReport crashReport = CrashReport.create(var5, "Ticking Particle");
         CrashReportSection crashReportSection = crashReport.addElement("Particle being ticked");
         crashReportSection.add("Particle", particle::toString);
         ParticleTextureSheet var10002 = particle.getType();
         crashReportSection.add("Particle Type", var10002::toString);
         throw new CrashException(crashReport);
      }
   }

   public void renderParticles(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate, LightmapTextureManager lightmapTextureManager, Camera camera, float f) {
      lightmapTextureManager.enable();
      RenderSystem.enableAlphaTest();
      RenderSystem.defaultAlphaFunc();
      RenderSystem.enableDepthTest();
      RenderSystem.enableFog();
      RenderSystem.pushMatrix();
      RenderSystem.multMatrix(matrixStack.peek().getModel());
      Iterator var6 = PARTICLE_TEXTURE_SHEETS.iterator();

      while(true) {
         ParticleTextureSheet particleTextureSheet;
         Iterable iterable;
         do {
            if (!var6.hasNext()) {
               RenderSystem.popMatrix();
               RenderSystem.depthMask(true);
               RenderSystem.depthFunc(515);
               RenderSystem.disableBlend();
               RenderSystem.defaultAlphaFunc();
               lightmapTextureManager.disable();
               RenderSystem.disableFog();
               return;
            }

            particleTextureSheet = (ParticleTextureSheet)var6.next();
            iterable = (Iterable)this.particles.get(particleTextureSheet);
         } while(iterable == null);

         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder bufferBuilder = tessellator.getBuffer();
         particleTextureSheet.begin(bufferBuilder, this.textureManager);
         Iterator var11 = iterable.iterator();

         while(var11.hasNext()) {
            Particle particle = (Particle)var11.next();

            try {
               particle.buildGeometry(bufferBuilder, camera, f);
            } catch (Throwable var16) {
               CrashReport crashReport = CrashReport.create(var16, "Rendering Particle");
               CrashReportSection crashReportSection = crashReport.addElement("Particle being rendered");
               crashReportSection.add("Particle", particle::toString);
               crashReportSection.add("Particle Type", particleTextureSheet::toString);
               throw new CrashException(crashReport);
            }
         }

         particleTextureSheet.draw(tessellator);
      }
   }

   public void setWorld(@Nullable ClientWorld world) {
      this.world = world;
      this.particles.clear();
      this.newEmitterParticles.clear();
   }

   public void addBlockBreakParticles(BlockPos pos, BlockState state) {
      if (!state.isAir()) {
         VoxelShape voxelShape = state.getOutlineShape(this.world, pos);
         double d = 0.25D;
         voxelShape.forEachBox((dx, e, f, g, h, i) -> {
            double j = Math.min(1.0D, g - dx);
            double k = Math.min(1.0D, h - e);
            double l = Math.min(1.0D, i - f);
            int m = Math.max(2, MathHelper.ceil(j / 0.25D));
            int n = Math.max(2, MathHelper.ceil(k / 0.25D));
            int o = Math.max(2, MathHelper.ceil(l / 0.25D));

            for(int p = 0; p < m; ++p) {
               for(int q = 0; q < n; ++q) {
                  for(int r = 0; r < o; ++r) {
                     double s = ((double)p + 0.5D) / (double)m;
                     double t = ((double)q + 0.5D) / (double)n;
                     double u = ((double)r + 0.5D) / (double)o;
                     double v = s * j + dx;
                     double w = t * k + e;
                     double x = u * l + f;
                     this.addParticle((new BlockDustParticle(this.world, (double)pos.getX() + v, (double)pos.getY() + w, (double)pos.getZ() + x, s - 0.5D, t - 0.5D, u - 0.5D, state)).setBlockPos(pos));
                  }
               }
            }

         });
      }
   }

   public void addBlockBreakingParticles(BlockPos pos, Direction direction) {
      BlockState blockState = this.world.getBlockState(pos);
      if (blockState.getRenderType() != BlockRenderType.INVISIBLE) {
         int i = pos.getX();
         int j = pos.getY();
         int k = pos.getZ();
         float f = 0.1F;
         Box box = blockState.getOutlineShape(this.world, pos).getBoundingBox();
         double d = (double)i + this.random.nextDouble() * (box.maxX - box.minX - 0.20000000298023224D) + 0.10000000149011612D + box.minX;
         double e = (double)j + this.random.nextDouble() * (box.maxY - box.minY - 0.20000000298023224D) + 0.10000000149011612D + box.minY;
         double g = (double)k + this.random.nextDouble() * (box.maxZ - box.minZ - 0.20000000298023224D) + 0.10000000149011612D + box.minZ;
         if (direction == Direction.DOWN) {
            e = (double)j + box.minY - 0.10000000149011612D;
         }

         if (direction == Direction.UP) {
            e = (double)j + box.maxY + 0.10000000149011612D;
         }

         if (direction == Direction.NORTH) {
            g = (double)k + box.minZ - 0.10000000149011612D;
         }

         if (direction == Direction.SOUTH) {
            g = (double)k + box.maxZ + 0.10000000149011612D;
         }

         if (direction == Direction.WEST) {
            d = (double)i + box.minX - 0.10000000149011612D;
         }

         if (direction == Direction.EAST) {
            d = (double)i + box.maxX + 0.10000000149011612D;
         }

         this.addParticle((new BlockDustParticle(this.world, d, e, g, 0.0D, 0.0D, 0.0D, blockState)).setBlockPos(pos).move(0.2F).scale(0.6F));
      }
   }

   public String getDebugString() {
      return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
   }

   static {
      PARTICLE_TEXTURE_SHEETS = ImmutableList.of(ParticleTextureSheet.TERRAIN_SHEET, ParticleTextureSheet.PARTICLE_SHEET_OPAQUE, ParticleTextureSheet.PARTICLE_SHEET_LIT, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT, ParticleTextureSheet.CUSTOM);
   }

   @Environment(EnvType.CLIENT)
   class SimpleSpriteProvider implements SpriteProvider {
      private List<Sprite> sprites;

      private SimpleSpriteProvider() {
      }

      public Sprite getSprite(int i, int j) {
         return (Sprite)this.sprites.get(i * (this.sprites.size() - 1) / j);
      }

      public Sprite getSprite(Random random) {
         return (Sprite)this.sprites.get(random.nextInt(this.sprites.size()));
      }

      public void setSprites(List<Sprite> sprites) {
         this.sprites = ImmutableList.copyOf((Collection)sprites);
      }
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   interface SpriteAwareFactory<T extends ParticleEffect> {
      ParticleFactory<T> create(SpriteProvider spriteProvider);
   }
}
