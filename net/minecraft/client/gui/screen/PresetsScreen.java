package net.minecraft.client.gui.screen;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PresetsScreen extends Screen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final List<PresetsScreen.SuperflatPreset> PRESETS = Lists.newArrayList();
   private final CustomizeFlatLevelScreen parent;
   private Text shareText;
   private Text listText;
   private PresetsScreen.SuperflatPresetsListWidget listWidget;
   private ButtonWidget selectPresetButton;
   private TextFieldWidget customPresetField;
   private FlatChunkGeneratorConfig config;

   public PresetsScreen(CustomizeFlatLevelScreen parent) {
      super(new TranslatableText("createWorld.customize.presets.title"));
      this.parent = parent;
   }

   /**
    * Parse a string like {@code "60*minecraft:stone"} to a {@link FlatChunkGeneratorLayer}.
    */
   @Nullable
   private static FlatChunkGeneratorLayer parseLayerString(String layer, int layerStartHeight) {
      String[] strings = layer.split("\\*", 2);
      int j;
      if (strings.length == 2) {
         try {
            j = Math.max(Integer.parseInt(strings[0]), 0);
         } catch (NumberFormatException var10) {
            LOGGER.error((String)"Error while parsing flat world string => {}", (Object)var10.getMessage());
            return null;
         }
      } else {
         j = 1;
      }

      int k = Math.min(layerStartHeight + j, 256);
      int l = k - layerStartHeight;
      String string = strings[strings.length - 1];

      Block block2;
      try {
         block2 = (Block)Registry.BLOCK.getOrEmpty(new Identifier(string)).orElse((Object)null);
      } catch (Exception var9) {
         LOGGER.error((String)"Error while parsing flat world string => {}", (Object)var9.getMessage());
         return null;
      }

      if (block2 == null) {
         LOGGER.error((String)"Error while parsing flat world string => Unknown block, {}", (Object)string);
         return null;
      } else {
         FlatChunkGeneratorLayer flatChunkGeneratorLayer = new FlatChunkGeneratorLayer(l, block2);
         flatChunkGeneratorLayer.setStartY(layerStartHeight);
         return flatChunkGeneratorLayer;
      }
   }

   /**
    * Parse a string like {@code "minecraft:bedrock,3*minecraft:dirt,minecraft:grass_block"}
    * to a list of {@link FlatChunkGeneratorLayer}.
    */
   private static List<FlatChunkGeneratorLayer> parsePresetLayersString(String layers) {
      List<FlatChunkGeneratorLayer> list = Lists.newArrayList();
      String[] strings = layers.split(",");
      int i = 0;
      String[] var4 = strings;
      int var5 = strings.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String string = var4[var6];
         FlatChunkGeneratorLayer flatChunkGeneratorLayer = parseLayerString(string, i);
         if (flatChunkGeneratorLayer == null) {
            return Collections.emptyList();
         }

         list.add(flatChunkGeneratorLayer);
         i += flatChunkGeneratorLayer.getThickness();
      }

      return list;
   }

   public static FlatChunkGeneratorConfig parsePresetString(Registry<Biome> biomeRegistry, String preset, FlatChunkGeneratorConfig generatorConfig) {
      Iterator<String> iterator = Splitter.on(';').split(preset).iterator();
      if (!iterator.hasNext()) {
         return FlatChunkGeneratorConfig.getDefaultConfig(biomeRegistry);
      } else {
         List<FlatChunkGeneratorLayer> list = parsePresetLayersString((String)iterator.next());
         if (list.isEmpty()) {
            return FlatChunkGeneratorConfig.getDefaultConfig(biomeRegistry);
         } else {
            FlatChunkGeneratorConfig flatChunkGeneratorConfig = generatorConfig.method_29965(list, generatorConfig.getStructuresConfig());
            RegistryKey<Biome> registryKey = BiomeKeys.PLAINS;
            if (iterator.hasNext()) {
               try {
                  Identifier identifier = new Identifier((String)iterator.next());
                  registryKey = RegistryKey.of(Registry.BIOME_KEY, identifier);
                  biomeRegistry.getOrEmpty(registryKey).orElseThrow(() -> {
                     return new IllegalArgumentException("Invalid Biome: " + identifier);
                  });
               } catch (Exception var8) {
                  LOGGER.error((String)"Error while parsing flat world string => {}", (Object)var8.getMessage());
               }
            }

            flatChunkGeneratorConfig.setBiome(() -> {
               return (Biome)biomeRegistry.getOrThrow(registryKey);
            });
            return flatChunkGeneratorConfig;
         }
      }
   }

   private static String getGeneratorConfigString(Registry<Biome> biomeRegistry, FlatChunkGeneratorConfig generatorConfig) {
      StringBuilder stringBuilder = new StringBuilder();

      for(int i = 0; i < generatorConfig.getLayers().size(); ++i) {
         if (i > 0) {
            stringBuilder.append(",");
         }

         stringBuilder.append(generatorConfig.getLayers().get(i));
      }

      stringBuilder.append(";");
      stringBuilder.append(biomeRegistry.getId(generatorConfig.getBiome()));
      return stringBuilder.toString();
   }

   protected void init() {
      this.client.keyboard.setRepeatEvents(true);
      this.shareText = new TranslatableText("createWorld.customize.presets.share");
      this.listText = new TranslatableText("createWorld.customize.presets.list");
      this.customPresetField = new TextFieldWidget(this.textRenderer, 50, 40, this.width - 100, 20, this.shareText);
      this.customPresetField.setMaxLength(1230);
      Registry<Biome> registry = this.parent.parent.moreOptionsDialog.getRegistryManager().get(Registry.BIOME_KEY);
      this.customPresetField.setText(getGeneratorConfigString(registry, this.parent.getConfig()));
      this.config = this.parent.getConfig();
      this.children.add(this.customPresetField);
      this.listWidget = new PresetsScreen.SuperflatPresetsListWidget();
      this.children.add(this.listWidget);
      this.selectPresetButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableText("createWorld.customize.presets.select"), (buttonWidget) -> {
         FlatChunkGeneratorConfig flatChunkGeneratorConfig = parsePresetString(registry, this.customPresetField.getText(), this.config);
         this.parent.setConfig(flatChunkGeneratorConfig);
         this.client.openScreen(this.parent);
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 5, this.height - 28, 150, 20, ScreenTexts.CANCEL, (buttonWidget) -> {
         this.client.openScreen(this.parent);
      }));
      this.updateSelectButton(this.listWidget.getSelected() != null);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.listWidget.mouseScrolled(mouseX, mouseY, amount);
   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.customPresetField.getText();
      this.init(client, width, height);
      this.customPresetField.setText(string);
   }

   public void onClose() {
      this.client.openScreen(this.parent);
   }

   public void removed() {
      this.client.keyboard.setRepeatEvents(false);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.listWidget.render(matrices, mouseX, mouseY, delta);
      RenderSystem.pushMatrix();
      RenderSystem.translatef(0.0F, 0.0F, 400.0F);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
      drawTextWithShadow(matrices, this.textRenderer, this.shareText, 50, 30, 10526880);
      drawTextWithShadow(matrices, this.textRenderer, this.listText, 50, 70, 10526880);
      RenderSystem.popMatrix();
      this.customPresetField.render(matrices, mouseX, mouseY, delta);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public void tick() {
      this.customPresetField.tick();
      super.tick();
   }

   public void updateSelectButton(boolean hasSelected) {
      this.selectPresetButton.active = hasSelected || this.customPresetField.getText().length() > 1;
   }

   private static void addPreset(Text presetName, ItemConvertible icon, RegistryKey<Biome> presetBiome, List<StructureFeature<?>> structures, boolean generateStronghold, boolean generateFeatures, boolean generateLakes, FlatChunkGeneratorLayer... layers) {
      PRESETS.add(new PresetsScreen.SuperflatPreset(icon.asItem(), presetName, (registry) -> {
         Map<StructureFeature<?>, StructureConfig> map = Maps.newHashMap();
         Iterator var8 = structures.iterator();

         while(var8.hasNext()) {
            StructureFeature<?> structureFeature = (StructureFeature)var8.next();
            map.put(structureFeature, StructuresConfig.DEFAULT_STRUCTURES.get(structureFeature));
         }

         StructuresConfig structuresConfig = new StructuresConfig(generateStronghold ? Optional.of(StructuresConfig.DEFAULT_STRONGHOLD) : Optional.empty(), map);
         FlatChunkGeneratorConfig flatChunkGeneratorConfig = new FlatChunkGeneratorConfig(structuresConfig, registry);
         if (generateFeatures) {
            flatChunkGeneratorConfig.enableFeatures();
         }

         if (generateLakes) {
            flatChunkGeneratorConfig.enableLakes();
         }

         for(int i = layers.length - 1; i >= 0; --i) {
            flatChunkGeneratorConfig.getLayers().add(layers[i]);
         }

         flatChunkGeneratorConfig.setBiome(() -> {
            return (Biome)registry.getOrThrow(presetBiome);
         });
         flatChunkGeneratorConfig.updateLayerBlocks();
         return flatChunkGeneratorConfig.withStructuresConfig(structuresConfig);
      }));
   }

   static {
      addPreset(new TranslatableText("createWorld.customize.preset.classic_flat"), Blocks.GRASS_BLOCK, BiomeKeys.PLAINS, Arrays.asList(StructureFeature.VILLAGE), false, false, false, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(2, Blocks.DIRT), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
      addPreset(new TranslatableText("createWorld.customize.preset.tunnelers_dream"), Blocks.STONE, BiomeKeys.MOUNTAINS, Arrays.asList(StructureFeature.MINESHAFT), true, true, false, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(5, Blocks.DIRT), new FlatChunkGeneratorLayer(230, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
      addPreset(new TranslatableText("createWorld.customize.preset.water_world"), Items.WATER_BUCKET, BiomeKeys.DEEP_OCEAN, Arrays.asList(StructureFeature.OCEAN_RUIN, StructureFeature.SHIPWRECK, StructureFeature.MONUMENT), false, false, false, new FlatChunkGeneratorLayer(90, Blocks.WATER), new FlatChunkGeneratorLayer(5, Blocks.SAND), new FlatChunkGeneratorLayer(5, Blocks.DIRT), new FlatChunkGeneratorLayer(5, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
      addPreset(new TranslatableText("createWorld.customize.preset.overworld"), Blocks.GRASS, BiomeKeys.PLAINS, Arrays.asList(StructureFeature.VILLAGE, StructureFeature.MINESHAFT, StructureFeature.PILLAGER_OUTPOST, StructureFeature.RUINED_PORTAL), true, true, true, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(3, Blocks.DIRT), new FlatChunkGeneratorLayer(59, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
      addPreset(new TranslatableText("createWorld.customize.preset.snowy_kingdom"), Blocks.SNOW, BiomeKeys.SNOWY_TUNDRA, Arrays.asList(StructureFeature.VILLAGE, StructureFeature.IGLOO), false, false, false, new FlatChunkGeneratorLayer(1, Blocks.SNOW), new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(3, Blocks.DIRT), new FlatChunkGeneratorLayer(59, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
      addPreset(new TranslatableText("createWorld.customize.preset.bottomless_pit"), Items.FEATHER, BiomeKeys.PLAINS, Arrays.asList(StructureFeature.VILLAGE), false, false, false, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(3, Blocks.DIRT), new FlatChunkGeneratorLayer(2, Blocks.COBBLESTONE));
      addPreset(new TranslatableText("createWorld.customize.preset.desert"), Blocks.SAND, BiomeKeys.DESERT, Arrays.asList(StructureFeature.VILLAGE, StructureFeature.DESERT_PYRAMID, StructureFeature.MINESHAFT), true, true, false, new FlatChunkGeneratorLayer(8, Blocks.SAND), new FlatChunkGeneratorLayer(52, Blocks.SANDSTONE), new FlatChunkGeneratorLayer(3, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
      addPreset(new TranslatableText("createWorld.customize.preset.redstone_ready"), Items.REDSTONE, BiomeKeys.DESERT, Collections.emptyList(), false, false, false, new FlatChunkGeneratorLayer(52, Blocks.SANDSTONE), new FlatChunkGeneratorLayer(3, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
      addPreset(new TranslatableText("createWorld.customize.preset.the_void"), Blocks.BARRIER, BiomeKeys.THE_VOID, Collections.emptyList(), false, true, false, new FlatChunkGeneratorLayer(1, Blocks.AIR));
   }

   @Environment(EnvType.CLIENT)
   static class SuperflatPreset {
      public final Item icon;
      public final Text name;
      public final Function<Registry<Biome>, FlatChunkGeneratorConfig> generatorConfigProvider;

      public SuperflatPreset(Item icon, Text name, Function<Registry<Biome>, FlatChunkGeneratorConfig> generatorConfigProvider) {
         this.icon = icon;
         this.name = name;
         this.generatorConfigProvider = generatorConfigProvider;
      }

      public Text getName() {
         return this.name;
      }
   }

   @Environment(EnvType.CLIENT)
   class SuperflatPresetsListWidget extends AlwaysSelectedEntryListWidget<PresetsScreen.SuperflatPresetsListWidget.SuperflatPresetEntry> {
      public SuperflatPresetsListWidget() {
         super(PresetsScreen.this.client, PresetsScreen.this.width, PresetsScreen.this.height, 80, PresetsScreen.this.height - 37, 24);

         for(int i = 0; i < PresetsScreen.PRESETS.size(); ++i) {
            this.addEntry(new PresetsScreen.SuperflatPresetsListWidget.SuperflatPresetEntry());
         }

      }

      public void setSelected(@Nullable PresetsScreen.SuperflatPresetsListWidget.SuperflatPresetEntry superflatPresetEntry) {
         super.setSelected(superflatPresetEntry);
         if (superflatPresetEntry != null) {
            NarratorManager.INSTANCE.narrate((new TranslatableText("narrator.select", new Object[]{((PresetsScreen.SuperflatPreset)PresetsScreen.PRESETS.get(this.children().indexOf(superflatPresetEntry))).getName()})).getString());
         }

         PresetsScreen.this.updateSelectButton(superflatPresetEntry != null);
      }

      protected boolean isFocused() {
         return PresetsScreen.this.getFocused() == this;
      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
         } else {
            if ((keyCode == 257 || keyCode == 335) && this.getSelected() != null) {
               ((PresetsScreen.SuperflatPresetsListWidget.SuperflatPresetEntry)this.getSelected()).setPreset();
            }

            return false;
         }
      }

      @Environment(EnvType.CLIENT)
      public class SuperflatPresetEntry extends AlwaysSelectedEntryListWidget.Entry<PresetsScreen.SuperflatPresetsListWidget.SuperflatPresetEntry> {
         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            PresetsScreen.SuperflatPreset superflatPreset = (PresetsScreen.SuperflatPreset)PresetsScreen.PRESETS.get(index);
            this.renderIcon(matrices, x, y, superflatPreset.icon);
            PresetsScreen.this.textRenderer.draw(matrices, superflatPreset.name, (float)(x + 18 + 5), (float)(y + 6), 16777215);
         }

         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
               this.setPreset();
            }

            return false;
         }

         private void setPreset() {
            SuperflatPresetsListWidget.this.setSelected(this);
            PresetsScreen.SuperflatPreset superflatPreset = (PresetsScreen.SuperflatPreset)PresetsScreen.PRESETS.get(SuperflatPresetsListWidget.this.children().indexOf(this));
            Registry<Biome> registry = PresetsScreen.this.parent.parent.moreOptionsDialog.getRegistryManager().get(Registry.BIOME_KEY);
            PresetsScreen.this.config = (FlatChunkGeneratorConfig)superflatPreset.generatorConfigProvider.apply(registry);
            PresetsScreen.this.customPresetField.setText(PresetsScreen.getGeneratorConfigString(registry, PresetsScreen.this.config));
            PresetsScreen.this.customPresetField.setCursorToStart();
         }

         private void renderIcon(MatrixStack matrices, int x, int y, Item iconItem) {
            this.drawIconBackground(matrices, x + 1, y + 1);
            RenderSystem.enableRescaleNormal();
            PresetsScreen.this.itemRenderer.renderGuiItemIcon(new ItemStack(iconItem), x + 2, y + 2);
            RenderSystem.disableRescaleNormal();
         }

         private void drawIconBackground(MatrixStack matrices, int x, int y) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            SuperflatPresetsListWidget.this.client.getTextureManager().bindTexture(DrawableHelper.STATS_ICON_TEXTURE);
            DrawableHelper.drawTexture(matrices, x, y, PresetsScreen.this.getZOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
         }
      }
   }
}
