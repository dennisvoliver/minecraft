package net.minecraft.client.options;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.VideoMode;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Arm;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GameOptions {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new Gson();
   private static final TypeToken<List<String>> STRING_LIST_TYPE = new TypeToken<List<String>>() {
   };
   private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
   public double mouseSensitivity = 0.5D;
   public int viewDistance = -1;
   public float entityDistanceScaling = 1.0F;
   public int maxFps = 120;
   public CloudRenderMode cloudRenderMode;
   public GraphicsMode graphicsMode;
   public AoMode ao;
   public List<String> resourcePacks;
   public List<String> incompatibleResourcePacks;
   public ChatVisibility chatVisibility;
   public double chatOpacity;
   public double chatLineSpacing;
   public double textBackgroundOpacity;
   @Nullable
   public String fullscreenResolution;
   public boolean hideServerAddress;
   public boolean advancedItemTooltips;
   public boolean pauseOnLostFocus;
   private final Set<PlayerModelPart> enabledPlayerModelParts;
   public Arm mainArm;
   public int overrideWidth;
   public int overrideHeight;
   public boolean heldItemTooltips;
   public double chatScale;
   public double chatWidth;
   public double chatHeightUnfocused;
   public double chatHeightFocused;
   public double chatDelay;
   public int mipmapLevels;
   private final Map<SoundCategory, Float> soundVolumeLevels;
   public boolean useNativeTransport;
   public AttackIndicator attackIndicator;
   public TutorialStep tutorialStep;
   public boolean joinedFirstServer;
   public int biomeBlendRadius;
   public double mouseWheelSensitivity;
   public boolean rawMouseInput;
   public int glDebugVerbosity;
   public boolean autoJump;
   public boolean autoSuggestions;
   public boolean chatColors;
   public boolean chatLinks;
   public boolean chatLinksPrompt;
   public boolean enableVsync;
   public boolean entityShadows;
   public boolean forceUnicodeFont;
   public boolean invertYMouse;
   public boolean discreteMouseScroll;
   public boolean realmsNotifications;
   public boolean reducedDebugInfo;
   public boolean snooperEnabled;
   public boolean showSubtitles;
   public boolean backgroundForChatOnly;
   public boolean touchscreen;
   public boolean fullscreen;
   public boolean bobView;
   public boolean sneakToggled;
   public boolean sprintToggled;
   public boolean skipMultiplayerWarning;
   public boolean field_26926;
   public final KeyBinding keyForward;
   public final KeyBinding keyLeft;
   public final KeyBinding keyBack;
   public final KeyBinding keyRight;
   public final KeyBinding keyJump;
   public final KeyBinding keySneak;
   public final KeyBinding keySprint;
   public final KeyBinding keyInventory;
   public final KeyBinding keySwapHands;
   public final KeyBinding keyDrop;
   public final KeyBinding keyUse;
   public final KeyBinding keyAttack;
   public final KeyBinding keyPickItem;
   public final KeyBinding keyChat;
   public final KeyBinding keyPlayerList;
   public final KeyBinding keyCommand;
   public final KeyBinding keySocialInteractions;
   public final KeyBinding keyScreenshot;
   public final KeyBinding keyTogglePerspective;
   public final KeyBinding keySmoothCamera;
   public final KeyBinding keyFullscreen;
   public final KeyBinding keySpectatorOutlines;
   public final KeyBinding keyAdvancements;
   public final KeyBinding[] keysHotbar;
   public final KeyBinding keySaveToolbarActivator;
   public final KeyBinding keyLoadToolbarActivator;
   public final KeyBinding[] keysAll;
   protected MinecraftClient client;
   private final File optionsFile;
   public Difficulty difficulty;
   public boolean hudHidden;
   private Perspective perspective;
   public boolean debugEnabled;
   public boolean debugProfilerEnabled;
   public boolean debugTpsEnabled;
   public String lastServer;
   public boolean smoothCameraEnabled;
   public double fov;
   public float distortionEffectScale;
   public float fovEffectScale;
   public double gamma;
   public int guiScale;
   public ParticlesMode particles;
   public NarratorMode narrator;
   public String language;
   public boolean syncChunkWrites;

   public GameOptions(MinecraftClient client, File optionsFile) {
      this.cloudRenderMode = CloudRenderMode.FANCY;
      this.graphicsMode = GraphicsMode.FANCY;
      this.ao = AoMode.MAX;
      this.resourcePacks = Lists.newArrayList();
      this.incompatibleResourcePacks = Lists.newArrayList();
      this.chatVisibility = ChatVisibility.FULL;
      this.chatOpacity = 1.0D;
      this.chatLineSpacing = 0.0D;
      this.textBackgroundOpacity = 0.5D;
      this.pauseOnLostFocus = true;
      this.enabledPlayerModelParts = Sets.newHashSet((Object[])PlayerModelPart.values());
      this.mainArm = Arm.RIGHT;
      this.heldItemTooltips = true;
      this.chatScale = 1.0D;
      this.chatWidth = 1.0D;
      this.chatHeightUnfocused = 0.44366195797920227D;
      this.chatHeightFocused = 1.0D;
      this.chatDelay = 0.0D;
      this.mipmapLevels = 4;
      this.soundVolumeLevels = Maps.newEnumMap(SoundCategory.class);
      this.useNativeTransport = true;
      this.attackIndicator = AttackIndicator.CROSSHAIR;
      this.tutorialStep = TutorialStep.MOVEMENT;
      this.joinedFirstServer = false;
      this.biomeBlendRadius = 2;
      this.mouseWheelSensitivity = 1.0D;
      this.rawMouseInput = true;
      this.glDebugVerbosity = 1;
      this.autoJump = true;
      this.autoSuggestions = true;
      this.chatColors = true;
      this.chatLinks = true;
      this.chatLinksPrompt = true;
      this.enableVsync = true;
      this.entityShadows = true;
      this.realmsNotifications = true;
      this.snooperEnabled = true;
      this.backgroundForChatOnly = true;
      this.bobView = true;
      this.field_26926 = true;
      this.keyForward = new KeyBinding("key.forward", 87, "key.categories.movement");
      this.keyLeft = new KeyBinding("key.left", 65, "key.categories.movement");
      this.keyBack = new KeyBinding("key.back", 83, "key.categories.movement");
      this.keyRight = new KeyBinding("key.right", 68, "key.categories.movement");
      this.keyJump = new KeyBinding("key.jump", 32, "key.categories.movement");
      this.keySneak = new StickyKeyBinding("key.sneak", 340, "key.categories.movement", () -> {
         return this.sneakToggled;
      });
      this.keySprint = new StickyKeyBinding("key.sprint", 341, "key.categories.movement", () -> {
         return this.sprintToggled;
      });
      this.keyInventory = new KeyBinding("key.inventory", 69, "key.categories.inventory");
      this.keySwapHands = new KeyBinding("key.swapOffhand", 70, "key.categories.inventory");
      this.keyDrop = new KeyBinding("key.drop", 81, "key.categories.inventory");
      this.keyUse = new KeyBinding("key.use", InputUtil.Type.MOUSE, 1, "key.categories.gameplay");
      this.keyAttack = new KeyBinding("key.attack", InputUtil.Type.MOUSE, 0, "key.categories.gameplay");
      this.keyPickItem = new KeyBinding("key.pickItem", InputUtil.Type.MOUSE, 2, "key.categories.gameplay");
      this.keyChat = new KeyBinding("key.chat", 84, "key.categories.multiplayer");
      this.keyPlayerList = new KeyBinding("key.playerlist", 258, "key.categories.multiplayer");
      this.keyCommand = new KeyBinding("key.command", 47, "key.categories.multiplayer");
      this.keySocialInteractions = new KeyBinding("key.socialInteractions", 80, "key.categories.multiplayer");
      this.keyScreenshot = new KeyBinding("key.screenshot", 291, "key.categories.misc");
      this.keyTogglePerspective = new KeyBinding("key.togglePerspective", 294, "key.categories.misc");
      this.keySmoothCamera = new KeyBinding("key.smoothCamera", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.misc");
      this.keyFullscreen = new KeyBinding("key.fullscreen", 300, "key.categories.misc");
      this.keySpectatorOutlines = new KeyBinding("key.spectatorOutlines", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.misc");
      this.keyAdvancements = new KeyBinding("key.advancements", 76, "key.categories.misc");
      this.keysHotbar = new KeyBinding[]{new KeyBinding("key.hotbar.1", 49, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 50, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 51, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 52, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 53, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 54, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 55, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 56, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 57, "key.categories.inventory")};
      this.keySaveToolbarActivator = new KeyBinding("key.saveToolbarActivator", 67, "key.categories.creative");
      this.keyLoadToolbarActivator = new KeyBinding("key.loadToolbarActivator", 88, "key.categories.creative");
      this.keysAll = (KeyBinding[])ArrayUtils.addAll((Object[])(new KeyBinding[]{this.keyAttack, this.keyUse, this.keyForward, this.keyLeft, this.keyBack, this.keyRight, this.keyJump, this.keySneak, this.keySprint, this.keyDrop, this.keyInventory, this.keyChat, this.keyPlayerList, this.keyPickItem, this.keyCommand, this.keySocialInteractions, this.keyScreenshot, this.keyTogglePerspective, this.keySmoothCamera, this.keyFullscreen, this.keySpectatorOutlines, this.keySwapHands, this.keySaveToolbarActivator, this.keyLoadToolbarActivator, this.keyAdvancements}), (Object[])this.keysHotbar);
      this.difficulty = Difficulty.NORMAL;
      this.perspective = Perspective.FIRST_PERSON;
      this.lastServer = "";
      this.fov = 70.0D;
      this.distortionEffectScale = 1.0F;
      this.fovEffectScale = 1.0F;
      this.particles = ParticlesMode.ALL;
      this.narrator = NarratorMode.OFF;
      this.language = "en_us";
      this.client = client;
      this.optionsFile = new File(optionsFile, "options.txt");
      if (client.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
         Option.RENDER_DISTANCE.setMax(32.0F);
      } else {
         Option.RENDER_DISTANCE.setMax(16.0F);
      }

      this.viewDistance = client.is64Bit() ? 12 : 8;
      this.syncChunkWrites = Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS;
      this.load();
   }

   public float getTextBackgroundOpacity(float fallback) {
      return this.backgroundForChatOnly ? fallback : (float)this.textBackgroundOpacity;
   }

   public int getTextBackgroundColor(float fallbackOpacity) {
      return (int)(this.getTextBackgroundOpacity(fallbackOpacity) * 255.0F) << 24 & -16777216;
   }

   public int getTextBackgroundColor(int fallbackColor) {
      return this.backgroundForChatOnly ? fallbackColor : (int)(this.textBackgroundOpacity * 255.0D) << 24 & -16777216;
   }

   public void setKeyCode(KeyBinding key, InputUtil.Key code) {
      key.setBoundKey(code);
      this.write();
   }

   public void load() {
      try {
         if (!this.optionsFile.exists()) {
            return;
         }

         this.soundVolumeLevels.clear();
         CompoundTag compoundTag = new CompoundTag();
         BufferedReader bufferedReader = Files.newReader(this.optionsFile, Charsets.UTF_8);
         Throwable var3 = null;

         try {
            bufferedReader.lines().forEach((stringx) -> {
               try {
                  Iterator<String> iterator = COLON_SPLITTER.split(stringx).iterator();
                  compoundTag.putString((String)iterator.next(), (String)iterator.next());
               } catch (Exception var3) {
                  LOGGER.warn((String)"Skipping bad option: {}", (Object)stringx);
               }

            });
         } catch (Throwable var17) {
            var3 = var17;
            throw var17;
         } finally {
            if (bufferedReader != null) {
               if (var3 != null) {
                  try {
                     bufferedReader.close();
                  } catch (Throwable var16) {
                     var3.addSuppressed(var16);
                  }
               } else {
                  bufferedReader.close();
               }
            }

         }

         CompoundTag compoundTag2 = this.update(compoundTag);
         if (!compoundTag2.contains("graphicsMode") && compoundTag2.contains("fancyGraphics")) {
            if ("true".equals(compoundTag2.getString("fancyGraphics"))) {
               this.graphicsMode = GraphicsMode.FANCY;
            } else {
               this.graphicsMode = GraphicsMode.FAST;
            }
         }

         Iterator var22 = compoundTag2.getKeys().iterator();

         while(var22.hasNext()) {
            String string = (String)var22.next();
            String string2 = compoundTag2.getString(string);

            try {
               if ("autoJump".equals(string)) {
                  Option.AUTO_JUMP.set(this, string2);
               }

               if ("autoSuggestions".equals(string)) {
                  Option.AUTO_SUGGESTIONS.set(this, string2);
               }

               if ("chatColors".equals(string)) {
                  Option.CHAT_COLOR.set(this, string2);
               }

               if ("chatLinks".equals(string)) {
                  Option.CHAT_LINKS.set(this, string2);
               }

               if ("chatLinksPrompt".equals(string)) {
                  Option.CHAT_LINKS_PROMPT.set(this, string2);
               }

               if ("enableVsync".equals(string)) {
                  Option.VSYNC.set(this, string2);
               }

               if ("entityShadows".equals(string)) {
                  Option.ENTITY_SHADOWS.set(this, string2);
               }

               if ("forceUnicodeFont".equals(string)) {
                  Option.FORCE_UNICODE_FONT.set(this, string2);
               }

               if ("discrete_mouse_scroll".equals(string)) {
                  Option.DISCRETE_MOUSE_SCROLL.set(this, string2);
               }

               if ("invertYMouse".equals(string)) {
                  Option.INVERT_MOUSE.set(this, string2);
               }

               if ("realmsNotifications".equals(string)) {
                  Option.REALMS_NOTIFICATIONS.set(this, string2);
               }

               if ("reducedDebugInfo".equals(string)) {
                  Option.REDUCED_DEBUG_INFO.set(this, string2);
               }

               if ("showSubtitles".equals(string)) {
                  Option.SUBTITLES.set(this, string2);
               }

               if ("snooperEnabled".equals(string)) {
                  Option.SNOOPER.set(this, string2);
               }

               if ("touchscreen".equals(string)) {
                  Option.TOUCHSCREEN.set(this, string2);
               }

               if ("fullscreen".equals(string)) {
                  Option.FULLSCREEN.set(this, string2);
               }

               if ("bobView".equals(string)) {
                  Option.VIEW_BOBBING.set(this, string2);
               }

               if ("toggleCrouch".equals(string)) {
                  this.sneakToggled = "true".equals(string2);
               }

               if ("toggleSprint".equals(string)) {
                  this.sprintToggled = "true".equals(string2);
               }

               if ("mouseSensitivity".equals(string)) {
                  this.mouseSensitivity = (double)parseFloat(string2);
               }

               if ("fov".equals(string)) {
                  this.fov = (double)(parseFloat(string2) * 40.0F + 70.0F);
               }

               if ("screenEffectScale".equals(string)) {
                  this.distortionEffectScale = parseFloat(string2);
               }

               if ("fovEffectScale".equals(string)) {
                  this.fovEffectScale = parseFloat(string2);
               }

               if ("gamma".equals(string)) {
                  this.gamma = (double)parseFloat(string2);
               }

               if ("renderDistance".equals(string)) {
                  this.viewDistance = Integer.parseInt(string2);
               }

               if ("entityDistanceScaling".equals(string)) {
                  this.entityDistanceScaling = Float.parseFloat(string2);
               }

               if ("guiScale".equals(string)) {
                  this.guiScale = Integer.parseInt(string2);
               }

               if ("particles".equals(string)) {
                  this.particles = ParticlesMode.byId(Integer.parseInt(string2));
               }

               if ("maxFps".equals(string)) {
                  this.maxFps = Integer.parseInt(string2);
                  if (this.client.getWindow() != null) {
                     this.client.getWindow().setFramerateLimit(this.maxFps);
                  }
               }

               if ("difficulty".equals(string)) {
                  this.difficulty = Difficulty.byOrdinal(Integer.parseInt(string2));
               }

               if ("graphicsMode".equals(string)) {
                  this.graphicsMode = GraphicsMode.byId(Integer.parseInt(string2));
               }

               if ("tutorialStep".equals(string)) {
                  this.tutorialStep = TutorialStep.byName(string2);
               }

               if ("ao".equals(string)) {
                  if ("true".equals(string2)) {
                     this.ao = AoMode.MAX;
                  } else if ("false".equals(string2)) {
                     this.ao = AoMode.OFF;
                  } else {
                     this.ao = AoMode.byId(Integer.parseInt(string2));
                  }
               }

               if ("renderClouds".equals(string)) {
                  if ("true".equals(string2)) {
                     this.cloudRenderMode = CloudRenderMode.FANCY;
                  } else if ("false".equals(string2)) {
                     this.cloudRenderMode = CloudRenderMode.OFF;
                  } else if ("fast".equals(string2)) {
                     this.cloudRenderMode = CloudRenderMode.FAST;
                  }
               }

               if ("attackIndicator".equals(string)) {
                  this.attackIndicator = AttackIndicator.byId(Integer.parseInt(string2));
               }

               if ("resourcePacks".equals(string)) {
                  this.resourcePacks = (List)JsonHelper.deserialize(GSON, string2, STRING_LIST_TYPE);
                  if (this.resourcePacks == null) {
                     this.resourcePacks = Lists.newArrayList();
                  }
               }

               if ("incompatibleResourcePacks".equals(string)) {
                  this.incompatibleResourcePacks = (List)JsonHelper.deserialize(GSON, string2, STRING_LIST_TYPE);
                  if (this.incompatibleResourcePacks == null) {
                     this.incompatibleResourcePacks = Lists.newArrayList();
                  }
               }

               if ("lastServer".equals(string)) {
                  this.lastServer = string2;
               }

               if ("lang".equals(string)) {
                  this.language = string2;
               }

               if ("chatVisibility".equals(string)) {
                  this.chatVisibility = ChatVisibility.byId(Integer.parseInt(string2));
               }

               if ("chatOpacity".equals(string)) {
                  this.chatOpacity = (double)parseFloat(string2);
               }

               if ("chatLineSpacing".equals(string)) {
                  this.chatLineSpacing = (double)parseFloat(string2);
               }

               if ("textBackgroundOpacity".equals(string)) {
                  this.textBackgroundOpacity = (double)parseFloat(string2);
               }

               if ("backgroundForChatOnly".equals(string)) {
                  this.backgroundForChatOnly = "true".equals(string2);
               }

               if ("fullscreenResolution".equals(string)) {
                  this.fullscreenResolution = string2;
               }

               if ("hideServerAddress".equals(string)) {
                  this.hideServerAddress = "true".equals(string2);
               }

               if ("advancedItemTooltips".equals(string)) {
                  this.advancedItemTooltips = "true".equals(string2);
               }

               if ("pauseOnLostFocus".equals(string)) {
                  this.pauseOnLostFocus = "true".equals(string2);
               }

               if ("overrideHeight".equals(string)) {
                  this.overrideHeight = Integer.parseInt(string2);
               }

               if ("overrideWidth".equals(string)) {
                  this.overrideWidth = Integer.parseInt(string2);
               }

               if ("heldItemTooltips".equals(string)) {
                  this.heldItemTooltips = "true".equals(string2);
               }

               if ("chatHeightFocused".equals(string)) {
                  this.chatHeightFocused = (double)parseFloat(string2);
               }

               if ("chatDelay".equals(string)) {
                  this.chatDelay = (double)parseFloat(string2);
               }

               if ("chatHeightUnfocused".equals(string)) {
                  this.chatHeightUnfocused = (double)parseFloat(string2);
               }

               if ("chatScale".equals(string)) {
                  this.chatScale = (double)parseFloat(string2);
               }

               if ("chatWidth".equals(string)) {
                  this.chatWidth = (double)parseFloat(string2);
               }

               if ("mipmapLevels".equals(string)) {
                  this.mipmapLevels = Integer.parseInt(string2);
               }

               if ("useNativeTransport".equals(string)) {
                  this.useNativeTransport = "true".equals(string2);
               }

               if ("mainHand".equals(string)) {
                  this.mainArm = "left".equals(string2) ? Arm.LEFT : Arm.RIGHT;
               }

               if ("narrator".equals(string)) {
                  this.narrator = NarratorMode.byId(Integer.parseInt(string2));
               }

               if ("biomeBlendRadius".equals(string)) {
                  this.biomeBlendRadius = Integer.parseInt(string2);
               }

               if ("mouseWheelSensitivity".equals(string)) {
                  this.mouseWheelSensitivity = (double)parseFloat(string2);
               }

               if ("rawMouseInput".equals(string)) {
                  this.rawMouseInput = "true".equals(string2);
               }

               if ("glDebugVerbosity".equals(string)) {
                  this.glDebugVerbosity = Integer.parseInt(string2);
               }

               if ("skipMultiplayerWarning".equals(string)) {
                  this.skipMultiplayerWarning = "true".equals(string2);
               }

               if ("hideMatchedNames".equals(string)) {
                  this.field_26926 = "true".equals(string2);
               }

               if ("joinedFirstServer".equals(string)) {
                  this.joinedFirstServer = "true".equals(string2);
               }

               if ("syncChunkWrites".equals(string)) {
                  this.syncChunkWrites = "true".equals(string2);
               }

               KeyBinding[] var6 = this.keysAll;
               int var7 = var6.length;

               int var8;
               for(var8 = 0; var8 < var7; ++var8) {
                  KeyBinding keyBinding = var6[var8];
                  if (string.equals("key_" + keyBinding.getTranslationKey())) {
                     keyBinding.setBoundKey(InputUtil.fromTranslationKey(string2));
                  }
               }

               SoundCategory[] var23 = SoundCategory.values();
               var7 = var23.length;

               for(var8 = 0; var8 < var7; ++var8) {
                  SoundCategory soundCategory = var23[var8];
                  if (string.equals("soundCategory_" + soundCategory.getName())) {
                     this.soundVolumeLevels.put(soundCategory, parseFloat(string2));
                  }
               }

               PlayerModelPart[] var24 = PlayerModelPart.values();
               var7 = var24.length;

               for(var8 = 0; var8 < var7; ++var8) {
                  PlayerModelPart playerModelPart = var24[var8];
                  if (string.equals("modelPart_" + playerModelPart.getName())) {
                     this.setPlayerModelPart(playerModelPart, "true".equals(string2));
                  }
               }
            } catch (Exception var19) {
               LOGGER.warn((String)"Skipping bad option: {}:{}", (Object)string, (Object)string2);
            }
         }

         KeyBinding.updateKeysByCode();
      } catch (Exception var20) {
         LOGGER.error((String)"Failed to load options", (Throwable)var20);
      }

   }

   private CompoundTag update(CompoundTag tag) {
      int i = 0;

      try {
         i = Integer.parseInt(tag.getString("version"));
      } catch (RuntimeException var4) {
      }

      return NbtHelper.update(this.client.getDataFixer(), DataFixTypes.OPTIONS, tag, i);
   }

   private static float parseFloat(String string) {
      if ("true".equals(string)) {
         return 1.0F;
      } else {
         return "false".equals(string) ? 0.0F : Float.parseFloat(string);
      }
   }

   public void write() {
      try {
         PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));
         Throwable var2 = null;

         try {
            printWriter.println("version:" + SharedConstants.getGameVersion().getWorldVersion());
            printWriter.println("autoJump:" + Option.AUTO_JUMP.get(this));
            printWriter.println("autoSuggestions:" + Option.AUTO_SUGGESTIONS.get(this));
            printWriter.println("chatColors:" + Option.CHAT_COLOR.get(this));
            printWriter.println("chatLinks:" + Option.CHAT_LINKS.get(this));
            printWriter.println("chatLinksPrompt:" + Option.CHAT_LINKS_PROMPT.get(this));
            printWriter.println("enableVsync:" + Option.VSYNC.get(this));
            printWriter.println("entityShadows:" + Option.ENTITY_SHADOWS.get(this));
            printWriter.println("forceUnicodeFont:" + Option.FORCE_UNICODE_FONT.get(this));
            printWriter.println("discrete_mouse_scroll:" + Option.DISCRETE_MOUSE_SCROLL.get(this));
            printWriter.println("invertYMouse:" + Option.INVERT_MOUSE.get(this));
            printWriter.println("realmsNotifications:" + Option.REALMS_NOTIFICATIONS.get(this));
            printWriter.println("reducedDebugInfo:" + Option.REDUCED_DEBUG_INFO.get(this));
            printWriter.println("snooperEnabled:" + Option.SNOOPER.get(this));
            printWriter.println("showSubtitles:" + Option.SUBTITLES.get(this));
            printWriter.println("touchscreen:" + Option.TOUCHSCREEN.get(this));
            printWriter.println("fullscreen:" + Option.FULLSCREEN.get(this));
            printWriter.println("bobView:" + Option.VIEW_BOBBING.get(this));
            printWriter.println("toggleCrouch:" + this.sneakToggled);
            printWriter.println("toggleSprint:" + this.sprintToggled);
            printWriter.println("mouseSensitivity:" + this.mouseSensitivity);
            printWriter.println("fov:" + (this.fov - 70.0D) / 40.0D);
            printWriter.println("screenEffectScale:" + this.distortionEffectScale);
            printWriter.println("fovEffectScale:" + this.fovEffectScale);
            printWriter.println("gamma:" + this.gamma);
            printWriter.println("renderDistance:" + this.viewDistance);
            printWriter.println("entityDistanceScaling:" + this.entityDistanceScaling);
            printWriter.println("guiScale:" + this.guiScale);
            printWriter.println("particles:" + this.particles.getId());
            printWriter.println("maxFps:" + this.maxFps);
            printWriter.println("difficulty:" + this.difficulty.getId());
            printWriter.println("graphicsMode:" + this.graphicsMode.getId());
            printWriter.println("ao:" + this.ao.getId());
            printWriter.println("biomeBlendRadius:" + this.biomeBlendRadius);
            switch(this.cloudRenderMode) {
            case FANCY:
               printWriter.println("renderClouds:true");
               break;
            case FAST:
               printWriter.println("renderClouds:fast");
               break;
            case OFF:
               printWriter.println("renderClouds:false");
            }

            printWriter.println("resourcePacks:" + GSON.toJson((Object)this.resourcePacks));
            printWriter.println("incompatibleResourcePacks:" + GSON.toJson((Object)this.incompatibleResourcePacks));
            printWriter.println("lastServer:" + this.lastServer);
            printWriter.println("lang:" + this.language);
            printWriter.println("chatVisibility:" + this.chatVisibility.getId());
            printWriter.println("chatOpacity:" + this.chatOpacity);
            printWriter.println("chatLineSpacing:" + this.chatLineSpacing);
            printWriter.println("textBackgroundOpacity:" + this.textBackgroundOpacity);
            printWriter.println("backgroundForChatOnly:" + this.backgroundForChatOnly);
            if (this.client.getWindow().getVideoMode().isPresent()) {
               printWriter.println("fullscreenResolution:" + ((VideoMode)this.client.getWindow().getVideoMode().get()).asString());
            }

            printWriter.println("hideServerAddress:" + this.hideServerAddress);
            printWriter.println("advancedItemTooltips:" + this.advancedItemTooltips);
            printWriter.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
            printWriter.println("overrideWidth:" + this.overrideWidth);
            printWriter.println("overrideHeight:" + this.overrideHeight);
            printWriter.println("heldItemTooltips:" + this.heldItemTooltips);
            printWriter.println("chatHeightFocused:" + this.chatHeightFocused);
            printWriter.println("chatDelay: " + this.chatDelay);
            printWriter.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
            printWriter.println("chatScale:" + this.chatScale);
            printWriter.println("chatWidth:" + this.chatWidth);
            printWriter.println("mipmapLevels:" + this.mipmapLevels);
            printWriter.println("useNativeTransport:" + this.useNativeTransport);
            printWriter.println("mainHand:" + (this.mainArm == Arm.LEFT ? "left" : "right"));
            printWriter.println("attackIndicator:" + this.attackIndicator.getId());
            printWriter.println("narrator:" + this.narrator.getId());
            printWriter.println("tutorialStep:" + this.tutorialStep.getName());
            printWriter.println("mouseWheelSensitivity:" + this.mouseWheelSensitivity);
            printWriter.println("rawMouseInput:" + Option.RAW_MOUSE_INPUT.get(this));
            printWriter.println("glDebugVerbosity:" + this.glDebugVerbosity);
            printWriter.println("skipMultiplayerWarning:" + this.skipMultiplayerWarning);
            printWriter.println("hideMatchedNames:" + this.field_26926);
            printWriter.println("joinedFirstServer:" + this.joinedFirstServer);
            printWriter.println("syncChunkWrites:" + this.syncChunkWrites);
            KeyBinding[] var3 = this.keysAll;
            int var4 = var3.length;

            int var5;
            for(var5 = 0; var5 < var4; ++var5) {
               KeyBinding keyBinding = var3[var5];
               printWriter.println("key_" + keyBinding.getTranslationKey() + ":" + keyBinding.getBoundKeyTranslationKey());
            }

            SoundCategory[] var18 = SoundCategory.values();
            var4 = var18.length;

            for(var5 = 0; var5 < var4; ++var5) {
               SoundCategory soundCategory = var18[var5];
               printWriter.println("soundCategory_" + soundCategory.getName() + ":" + this.getSoundVolume(soundCategory));
            }

            PlayerModelPart[] var19 = PlayerModelPart.values();
            var4 = var19.length;

            for(var5 = 0; var5 < var4; ++var5) {
               PlayerModelPart playerModelPart = var19[var5];
               printWriter.println("modelPart_" + playerModelPart.getName() + ":" + this.enabledPlayerModelParts.contains(playerModelPart));
            }
         } catch (Throwable var15) {
            var2 = var15;
            throw var15;
         } finally {
            if (printWriter != null) {
               if (var2 != null) {
                  try {
                     printWriter.close();
                  } catch (Throwable var14) {
                     var2.addSuppressed(var14);
                  }
               } else {
                  printWriter.close();
               }
            }

         }
      } catch (Exception var17) {
         LOGGER.error((String)"Failed to save options", (Throwable)var17);
      }

      this.onPlayerModelPartChange();
   }

   public float getSoundVolume(SoundCategory category) {
      return this.soundVolumeLevels.containsKey(category) ? (Float)this.soundVolumeLevels.get(category) : 1.0F;
   }

   public void setSoundVolume(SoundCategory category, float volume) {
      this.soundVolumeLevels.put(category, volume);
      this.client.getSoundManager().updateSoundVolume(category, volume);
   }

   public void onPlayerModelPartChange() {
      if (this.client.player != null) {
         int i = 0;

         PlayerModelPart playerModelPart;
         for(Iterator var2 = this.enabledPlayerModelParts.iterator(); var2.hasNext(); i |= playerModelPart.getBitFlag()) {
            playerModelPart = (PlayerModelPart)var2.next();
         }

         this.client.player.networkHandler.sendPacket(new ClientSettingsC2SPacket(this.language, this.viewDistance, this.chatVisibility, this.chatColors, i, this.mainArm));
      }

   }

   public Set<PlayerModelPart> getEnabledPlayerModelParts() {
      return ImmutableSet.copyOf((Collection)this.enabledPlayerModelParts);
   }

   public void setPlayerModelPart(PlayerModelPart part, boolean enabled) {
      if (enabled) {
         this.enabledPlayerModelParts.add(part);
      } else {
         this.enabledPlayerModelParts.remove(part);
      }

      this.onPlayerModelPartChange();
   }

   public void togglePlayerModelPart(PlayerModelPart part) {
      if (this.getEnabledPlayerModelParts().contains(part)) {
         this.enabledPlayerModelParts.remove(part);
      } else {
         this.enabledPlayerModelParts.add(part);
      }

      this.onPlayerModelPartChange();
   }

   public CloudRenderMode getCloudRenderMode() {
      return this.viewDistance >= 4 ? this.cloudRenderMode : CloudRenderMode.OFF;
   }

   public boolean shouldUseNativeTransport() {
      return this.useNativeTransport;
   }

   public void addResourcePackProfilesToManager(ResourcePackManager manager) {
      Set<String> set = Sets.newLinkedHashSet();
      Iterator iterator = this.resourcePacks.iterator();

      while(true) {
         while(iterator.hasNext()) {
            String string = (String)iterator.next();
            ResourcePackProfile resourcePackProfile = manager.getProfile(string);
            if (resourcePackProfile == null && !string.startsWith("file/")) {
               resourcePackProfile = manager.getProfile("file/" + string);
            }

            if (resourcePackProfile == null) {
               LOGGER.warn((String)"Removed resource pack {} from options because it doesn't seem to exist anymore", (Object)string);
               iterator.remove();
            } else if (!resourcePackProfile.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(string)) {
               LOGGER.warn((String)"Removed resource pack {} from options because it is no longer compatible", (Object)string);
               iterator.remove();
            } else if (resourcePackProfile.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(string)) {
               LOGGER.info((String)"Removed resource pack {} from incompatibility list because it's now compatible", (Object)string);
               this.incompatibleResourcePacks.remove(string);
            } else {
               set.add(resourcePackProfile.getName());
            }
         }

         manager.setEnabledProfiles(set);
         return;
      }
   }

   public Perspective getPerspective() {
      return this.perspective;
   }

   public void setPerspective(Perspective perspective) {
      this.perspective = perspective;
   }
}
