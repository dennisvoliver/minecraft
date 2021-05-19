package net.minecraft.client.sound;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SoundSystem {
   private static final Marker MARKER = MarkerManager.getMarker("SOUNDS");
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Set<Identifier> unknownSounds = Sets.newHashSet();
   private final SoundManager loader;
   private final GameOptions settings;
   private boolean started;
   private final SoundEngine soundEngine = new SoundEngine();
   private final SoundListener listener;
   private final SoundLoader soundLoader;
   private final SoundExecutor taskQueue;
   private final Channel channel;
   private int ticks;
   private final Map<SoundInstance, Channel.SourceManager> sources;
   private final Multimap<SoundCategory, SoundInstance> sounds;
   private final List<TickableSoundInstance> tickingSounds;
   private final Map<SoundInstance, Integer> startTicks;
   private final Map<SoundInstance, Integer> soundEndTicks;
   private final List<SoundInstanceListener> listeners;
   private final List<TickableSoundInstance> soundsToPlayNextTick;
   private final List<Sound> preloadedSounds;

   public SoundSystem(SoundManager loader, GameOptions settings, ResourceManager resourceManager) {
      this.listener = this.soundEngine.getListener();
      this.taskQueue = new SoundExecutor();
      this.channel = new Channel(this.soundEngine, this.taskQueue);
      this.sources = Maps.newHashMap();
      this.sounds = HashMultimap.create();
      this.tickingSounds = Lists.newArrayList();
      this.startTicks = Maps.newHashMap();
      this.soundEndTicks = Maps.newHashMap();
      this.listeners = Lists.newArrayList();
      this.soundsToPlayNextTick = Lists.newArrayList();
      this.preloadedSounds = Lists.newArrayList();
      this.loader = loader;
      this.settings = settings;
      this.soundLoader = new SoundLoader(resourceManager);
   }

   public void reloadSounds() {
      unknownSounds.clear();
      Iterator var1 = Registry.SOUND_EVENT.iterator();

      while(var1.hasNext()) {
         SoundEvent soundEvent = (SoundEvent)var1.next();
         Identifier identifier = soundEvent.getId();
         if (this.loader.get(identifier) == null) {
            LOGGER.warn((String)"Missing sound for event: {}", (Object)Registry.SOUND_EVENT.getId(soundEvent));
            unknownSounds.add(identifier);
         }
      }

      this.stop();
      this.start();
   }

   private synchronized void start() {
      if (!this.started) {
         try {
            this.soundEngine.init();
            this.listener.init();
            this.listener.setVolume(this.settings.getSoundVolume(SoundCategory.MASTER));
            CompletableFuture var10000 = this.soundLoader.loadStatic((Collection)this.preloadedSounds);
            List var10001 = this.preloadedSounds;
            var10000.thenRun(var10001::clear);
            this.started = true;
            LOGGER.info(MARKER, "Sound engine started");
         } catch (RuntimeException var2) {
            LOGGER.error((Marker)MARKER, (String)"Error starting SoundSystem. Turning off sounds & music", (Throwable)var2);
         }

      }
   }

   private float getSoundVolume(@Nullable SoundCategory soundCategory) {
      return soundCategory != null && soundCategory != SoundCategory.MASTER ? this.settings.getSoundVolume(soundCategory) : 1.0F;
   }

   public void updateSoundVolume(SoundCategory soundCategory, float volume) {
      if (this.started) {
         if (soundCategory == SoundCategory.MASTER) {
            this.listener.setVolume(volume);
         } else {
            this.sources.forEach((soundInstance, sourceManager) -> {
               float f = this.getAdjustedVolume(soundInstance);
               sourceManager.run((source) -> {
                  if (f <= 0.0F) {
                     source.stop();
                  } else {
                     source.setVolume(f);
                  }

               });
            });
         }
      }
   }

   public void stop() {
      if (this.started) {
         this.stopAll();
         this.soundLoader.close();
         this.soundEngine.close();
         this.started = false;
      }

   }

   public void stop(SoundInstance soundInstance) {
      if (this.started) {
         Channel.SourceManager sourceManager = (Channel.SourceManager)this.sources.get(soundInstance);
         if (sourceManager != null) {
            sourceManager.run(Source::stop);
         }
      }

   }

   public void stopAll() {
      if (this.started) {
         this.taskQueue.restart();
         this.sources.values().forEach((sourceManager) -> {
            sourceManager.run(Source::stop);
         });
         this.sources.clear();
         this.channel.close();
         this.startTicks.clear();
         this.tickingSounds.clear();
         this.sounds.clear();
         this.soundEndTicks.clear();
         this.soundsToPlayNextTick.clear();
      }

   }

   public void registerListener(SoundInstanceListener soundInstanceListener) {
      this.listeners.add(soundInstanceListener);
   }

   public void unregisterListener(SoundInstanceListener soundInstanceListener) {
      this.listeners.remove(soundInstanceListener);
   }

   public void tick(boolean bl) {
      if (!bl) {
         this.tick();
      }

      this.channel.tick();
   }

   private void tick() {
      ++this.ticks;
      this.soundsToPlayNextTick.stream().filter(SoundInstance::canPlay).forEach(this::play);
      this.soundsToPlayNextTick.clear();
      Iterator iterator = this.tickingSounds.iterator();

      while(iterator.hasNext()) {
         TickableSoundInstance tickableSoundInstance = (TickableSoundInstance)iterator.next();
         if (!tickableSoundInstance.canPlay()) {
            this.stop(tickableSoundInstance);
         }

         tickableSoundInstance.tick();
         if (tickableSoundInstance.isDone()) {
            this.stop(tickableSoundInstance);
         } else {
            float f = this.getAdjustedVolume(tickableSoundInstance);
            float g = this.getAdjustedPitch(tickableSoundInstance);
            Vec3d vec3d = new Vec3d(tickableSoundInstance.getX(), tickableSoundInstance.getY(), tickableSoundInstance.getZ());
            Channel.SourceManager sourceManager = (Channel.SourceManager)this.sources.get(tickableSoundInstance);
            if (sourceManager != null) {
               sourceManager.run((source) -> {
                  source.setVolume(f);
                  source.setPitch(g);
                  source.setPosition(vec3d);
               });
            }
         }
      }

      iterator = this.sources.entrySet().iterator();

      SoundInstance soundInstance;
      while(iterator.hasNext()) {
         Entry<SoundInstance, Channel.SourceManager> entry = (Entry)iterator.next();
         Channel.SourceManager sourceManager2 = (Channel.SourceManager)entry.getValue();
         soundInstance = (SoundInstance)entry.getKey();
         float h = this.settings.getSoundVolume(soundInstance.getCategory());
         if (h <= 0.0F) {
            sourceManager2.run(Source::stop);
            iterator.remove();
         } else if (sourceManager2.isStopped()) {
            int i = (Integer)this.soundEndTicks.get(soundInstance);
            if (i <= this.ticks) {
               if (isRepeatDelayed(soundInstance)) {
                  this.startTicks.put(soundInstance, this.ticks + soundInstance.getRepeatDelay());
               }

               iterator.remove();
               LOGGER.debug((Marker)MARKER, (String)"Removed channel {} because it's not playing anymore", (Object)sourceManager2);
               this.soundEndTicks.remove(soundInstance);

               try {
                  this.sounds.remove(soundInstance.getCategory(), soundInstance);
               } catch (RuntimeException var8) {
               }

               if (soundInstance instanceof TickableSoundInstance) {
                  this.tickingSounds.remove(soundInstance);
               }
            }
         }
      }

      Iterator iterator2 = this.startTicks.entrySet().iterator();

      while(iterator2.hasNext()) {
         Entry<SoundInstance, Integer> entry2 = (Entry)iterator2.next();
         if (this.ticks >= (Integer)entry2.getValue()) {
            soundInstance = (SoundInstance)entry2.getKey();
            if (soundInstance instanceof TickableSoundInstance) {
               ((TickableSoundInstance)soundInstance).tick();
            }

            this.play(soundInstance);
            iterator2.remove();
         }
      }

   }

   private static boolean canRepeatInstantly(SoundInstance soundInstance) {
      return soundInstance.getRepeatDelay() > 0;
   }

   private static boolean isRepeatDelayed(SoundInstance soundInstance) {
      return soundInstance.isRepeatable() && canRepeatInstantly(soundInstance);
   }

   private static boolean shouldRepeatInstantly(SoundInstance soundInstance) {
      return soundInstance.isRepeatable() && !canRepeatInstantly(soundInstance);
   }

   public boolean isPlaying(SoundInstance soundInstance) {
      if (!this.started) {
         return false;
      } else {
         return this.soundEndTicks.containsKey(soundInstance) && (Integer)this.soundEndTicks.get(soundInstance) <= this.ticks ? true : this.sources.containsKey(soundInstance);
      }
   }

   public void play(SoundInstance soundInstance) {
      if (this.started) {
         if (soundInstance.canPlay()) {
            WeightedSoundSet weightedSoundSet = soundInstance.getSoundSet(this.loader);
            Identifier identifier = soundInstance.getId();
            if (weightedSoundSet == null) {
               if (unknownSounds.add(identifier)) {
                  LOGGER.warn((Marker)MARKER, (String)"Unable to play unknown soundEvent: {}", (Object)identifier);
               }

            } else {
               Sound sound = soundInstance.getSound();
               if (sound == SoundManager.MISSING_SOUND) {
                  if (unknownSounds.add(identifier)) {
                     LOGGER.warn((Marker)MARKER, (String)"Unable to play empty soundEvent: {}", (Object)identifier);
                  }

               } else {
                  float f = soundInstance.getVolume();
                  float g = Math.max(f, 1.0F) * (float)sound.getAttenuation();
                  SoundCategory soundCategory = soundInstance.getCategory();
                  float h = this.getAdjustedVolume(soundInstance);
                  float i = this.getAdjustedPitch(soundInstance);
                  SoundInstance.AttenuationType attenuationType = soundInstance.getAttenuationType();
                  boolean bl = soundInstance.isLooping();
                  if (h == 0.0F && !soundInstance.shouldAlwaysPlay()) {
                     LOGGER.debug((Marker)MARKER, (String)"Skipped playing sound {}, volume was zero.", (Object)sound.getIdentifier());
                  } else {
                     Vec3d vec3d = new Vec3d(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
                     boolean bl3;
                     if (!this.listeners.isEmpty()) {
                        bl3 = bl || attenuationType == SoundInstance.AttenuationType.NONE || this.listener.getPos().squaredDistanceTo(vec3d) < (double)(g * g);
                        if (bl3) {
                           Iterator var14 = this.listeners.iterator();

                           while(var14.hasNext()) {
                              SoundInstanceListener soundInstanceListener = (SoundInstanceListener)var14.next();
                              soundInstanceListener.onSoundPlayed(soundInstance, weightedSoundSet);
                           }
                        } else {
                           LOGGER.debug((Marker)MARKER, (String)"Did not notify listeners of soundEvent: {}, it is too far away to hear", (Object)identifier);
                        }
                     }

                     if (this.listener.getVolume() <= 0.0F) {
                        LOGGER.debug((Marker)MARKER, (String)"Skipped playing soundEvent: {}, master volume was zero", (Object)identifier);
                     } else {
                        bl3 = shouldRepeatInstantly(soundInstance);
                        boolean bl4 = sound.isStreamed();
                        CompletableFuture<Channel.SourceManager> completableFuture = this.channel.createSource(sound.isStreamed() ? SoundEngine.RunMode.STREAMING : SoundEngine.RunMode.STATIC);
                        Channel.SourceManager sourceManager = (Channel.SourceManager)completableFuture.join();
                        if (sourceManager == null) {
                           LOGGER.warn("Failed to create new sound handle");
                        } else {
                           LOGGER.debug((Marker)MARKER, (String)"Playing sound {} for event {}", sound.getIdentifier(), identifier);
                           this.soundEndTicks.put(soundInstance, this.ticks + 20);
                           this.sources.put(soundInstance, sourceManager);
                           this.sounds.put(soundCategory, soundInstance);
                           sourceManager.run((source) -> {
                              source.setPitch(i);
                              source.setVolume(h);
                              if (attenuationType == SoundInstance.AttenuationType.LINEAR) {
                                 source.setAttenuation(g);
                              } else {
                                 source.disableAttenuation();
                              }

                              source.setLooping(bl3 && !bl4);
                              source.setPosition(vec3d);
                              source.setRelative(bl);
                           });
                           if (!bl4) {
                              this.soundLoader.loadStatic(sound.getLocation()).thenAccept((staticSound) -> {
                                 sourceManager.run((source) -> {
                                    source.setBuffer(staticSound);
                                    source.play();
                                 });
                              });
                           } else {
                              this.soundLoader.loadStreamed(sound.getLocation(), bl3).thenAccept((audioStream) -> {
                                 sourceManager.run((source) -> {
                                    source.setStream(audioStream);
                                    source.play();
                                 });
                              });
                           }

                           if (soundInstance instanceof TickableSoundInstance) {
                              this.tickingSounds.add((TickableSoundInstance)soundInstance);
                           }

                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void playNextTick(TickableSoundInstance sound) {
      this.soundsToPlayNextTick.add(sound);
   }

   public void addPreloadedSound(Sound sound) {
      this.preloadedSounds.add(sound);
   }

   private float getAdjustedPitch(SoundInstance soundInstance) {
      return MathHelper.clamp(soundInstance.getPitch(), 0.5F, 2.0F);
   }

   private float getAdjustedVolume(SoundInstance soundInstance) {
      return MathHelper.clamp(soundInstance.getVolume() * this.getSoundVolume(soundInstance.getCategory()), 0.0F, 1.0F);
   }

   public void pauseAll() {
      if (this.started) {
         this.channel.execute((stream) -> {
            stream.forEach(Source::pause);
         });
      }

   }

   public void resumeAll() {
      if (this.started) {
         this.channel.execute((stream) -> {
            stream.forEach(Source::resume);
         });
      }

   }

   public void play(SoundInstance sound, int delay) {
      this.startTicks.put(sound, this.ticks + delay);
   }

   public void updateListenerPosition(Camera camera) {
      if (this.started && camera.isReady()) {
         Vec3d vec3d = camera.getPos();
         Vector3f vector3f = camera.getHorizontalPlane();
         Vector3f vector3f2 = camera.getVerticalPlane();
         this.taskQueue.execute(() -> {
            this.listener.setPosition(vec3d);
            this.listener.setOrientation(vector3f, vector3f2);
         });
      }
   }

   public void stopSounds(@Nullable Identifier identifier, @Nullable SoundCategory soundCategory) {
      Iterator var3;
      SoundInstance soundInstance2;
      if (soundCategory != null) {
         var3 = this.sounds.get(soundCategory).iterator();

         while(true) {
            do {
               if (!var3.hasNext()) {
                  return;
               }

               soundInstance2 = (SoundInstance)var3.next();
            } while(identifier != null && !soundInstance2.getId().equals(identifier));

            this.stop(soundInstance2);
         }
      } else if (identifier == null) {
         this.stopAll();
      } else {
         var3 = this.sources.keySet().iterator();

         while(var3.hasNext()) {
            soundInstance2 = (SoundInstance)var3.next();
            if (soundInstance2.getId().equals(identifier)) {
               this.stop(soundInstance2);
            }
         }
      }

   }

   public String getDebugString() {
      return this.soundEngine.getDebugString();
   }
}
