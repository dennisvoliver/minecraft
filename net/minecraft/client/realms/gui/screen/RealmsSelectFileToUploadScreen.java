package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.Realms;
import net.minecraft.client.realms.RealmsLabel;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RealmsSelectFileToUploadScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Text worldLang = new TranslatableText("selectWorld.world");
   private static final Text conversionLang = new TranslatableText("selectWorld.conversion");
   private static final Text field_26507;
   private static final Text field_26508;
   private static final DateFormat DATE_FORMAT;
   private final RealmsResetWorldScreen parent;
   private final long worldId;
   private final int slotId;
   private ButtonWidget uploadButton;
   private List<LevelSummary> levelList = Lists.newArrayList();
   private int selectedWorld = -1;
   private RealmsSelectFileToUploadScreen.WorldSelectionList worldSelectionList;
   private RealmsLabel titleLabel;
   private RealmsLabel subtitleLabel;
   private RealmsLabel field_20063;
   private final Runnable field_22717;

   public RealmsSelectFileToUploadScreen(long worldId, int slotId, RealmsResetWorldScreen parent, Runnable runnable) {
      this.parent = parent;
      this.worldId = worldId;
      this.slotId = slotId;
      this.field_22717 = runnable;
   }

   private void loadLevelList() throws Exception {
      this.levelList = (List)this.client.getLevelStorage().getLevelList().stream().sorted((levelSummaryx, levelSummary2) -> {
         if (levelSummaryx.getLastPlayed() < levelSummary2.getLastPlayed()) {
            return 1;
         } else {
            return levelSummaryx.getLastPlayed() > levelSummary2.getLastPlayed() ? -1 : levelSummaryx.getName().compareTo(levelSummary2.getName());
         }
      }).collect(Collectors.toList());
      Iterator var1 = this.levelList.iterator();

      while(var1.hasNext()) {
         LevelSummary levelSummary = (LevelSummary)var1.next();
         this.worldSelectionList.addEntry(levelSummary);
      }

   }

   public void init() {
      this.client.keyboard.setRepeatEvents(true);
      this.worldSelectionList = new RealmsSelectFileToUploadScreen.WorldSelectionList();

      try {
         this.loadLevelList();
      } catch (Exception var2) {
         LOGGER.error((String)"Couldn't load level list", (Throwable)var2);
         this.client.openScreen(new RealmsGenericErrorScreen(new LiteralText("Unable to load worlds"), Text.of(var2.getMessage()), this.parent));
         return;
      }

      this.addChild(this.worldSelectionList);
      this.uploadButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 32, 153, 20, new TranslatableText("mco.upload.button.name"), (buttonWidget) -> {
         this.upload();
      }));
      this.uploadButton.active = this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size();
      this.addButton(new ButtonWidget(this.width / 2 + 6, this.height - 32, 153, 20, ScreenTexts.BACK, (buttonWidget) -> {
         this.client.openScreen(this.parent);
      }));
      this.titleLabel = (RealmsLabel)this.addChild(new RealmsLabel(new TranslatableText("mco.upload.select.world.title"), this.width / 2, 13, 16777215));
      this.subtitleLabel = (RealmsLabel)this.addChild(new RealmsLabel(new TranslatableText("mco.upload.select.world.subtitle"), this.width / 2, row(-1), 10526880));
      if (this.levelList.isEmpty()) {
         this.field_20063 = (RealmsLabel)this.addChild(new RealmsLabel(new TranslatableText("mco.upload.select.world.none"), this.width / 2, this.height / 2 - 20, 16777215));
      } else {
         this.field_20063 = null;
      }

      this.narrateLabels();
   }

   public void removed() {
      this.client.keyboard.setRepeatEvents(false);
   }

   private void upload() {
      if (this.selectedWorld != -1 && !((LevelSummary)this.levelList.get(this.selectedWorld)).isHardcore()) {
         LevelSummary levelSummary = (LevelSummary)this.levelList.get(this.selectedWorld);
         this.client.openScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.parent, levelSummary, this.field_22717));
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.worldSelectionList.render(matrices, mouseX, mouseY, delta);
      this.titleLabel.render(this, matrices);
      this.subtitleLabel.render(this, matrices);
      if (this.field_20063 != null) {
         this.field_20063.render(this, matrices);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.client.openScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private static Text method_21400(LevelSummary levelSummary) {
      return levelSummary.getGameMode().getTranslatableName();
   }

   private static String method_21404(LevelSummary levelSummary) {
      return DATE_FORMAT.format(new Date(levelSummary.getLastPlayed()));
   }

   static {
      field_26507 = (new TranslatableText("mco.upload.hardcore")).formatted(Formatting.DARK_RED);
      field_26508 = new TranslatableText("selectWorld.cheats");
      DATE_FORMAT = new SimpleDateFormat();
   }

   @Environment(EnvType.CLIENT)
   class WorldListEntry extends AlwaysSelectedEntryListWidget.Entry<RealmsSelectFileToUploadScreen.WorldListEntry> {
      private final LevelSummary field_22718;
      private final String field_26509;
      private final String field_26510;
      private final Text field_26511;

      public WorldListEntry(LevelSummary levelSummary) {
         this.field_22718 = levelSummary;
         this.field_26509 = levelSummary.getDisplayName();
         this.field_26510 = levelSummary.getName() + " (" + RealmsSelectFileToUploadScreen.method_21404(levelSummary) + ")";
         if (levelSummary.requiresConversion()) {
            this.field_26511 = RealmsSelectFileToUploadScreen.conversionLang;
         } else {
            Object text2;
            if (levelSummary.isHardcore()) {
               text2 = RealmsSelectFileToUploadScreen.field_26507;
            } else {
               text2 = RealmsSelectFileToUploadScreen.method_21400(levelSummary);
            }

            if (levelSummary.hasCheats()) {
               text2 = ((Text)text2).shallowCopy().append(", ").append(RealmsSelectFileToUploadScreen.field_26508);
            }

            this.field_26511 = (Text)text2;
         }

      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.renderItem(matrices, this.field_22718, index, x, y);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         RealmsSelectFileToUploadScreen.this.worldSelectionList.setSelected(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.field_22718));
         return true;
      }

      protected void renderItem(MatrixStack matrixStack, LevelSummary levelSummary, int i, int j, int k) {
         String string2;
         if (this.field_26509.isEmpty()) {
            string2 = RealmsSelectFileToUploadScreen.worldLang + " " + (i + 1);
         } else {
            string2 = this.field_26509;
         }

         RealmsSelectFileToUploadScreen.this.textRenderer.draw(matrixStack, string2, (float)(j + 2), (float)(k + 1), 16777215);
         RealmsSelectFileToUploadScreen.this.textRenderer.draw(matrixStack, this.field_26510, (float)(j + 2), (float)(k + 12), 8421504);
         RealmsSelectFileToUploadScreen.this.textRenderer.draw(matrixStack, this.field_26511, (float)(j + 2), (float)(k + 12 + 10), 8421504);
      }
   }

   @Environment(EnvType.CLIENT)
   class WorldSelectionList extends RealmsObjectSelectionList<RealmsSelectFileToUploadScreen.WorldListEntry> {
      public WorldSelectionList() {
         super(RealmsSelectFileToUploadScreen.this.width, RealmsSelectFileToUploadScreen.this.height, RealmsSelectFileToUploadScreen.row(0), RealmsSelectFileToUploadScreen.this.height - 40, 36);
      }

      public void addEntry(LevelSummary levelSummary) {
         this.addEntry(RealmsSelectFileToUploadScreen.this.new WorldListEntry(levelSummary));
      }

      public int getMaxPosition() {
         return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
      }

      public boolean isFocused() {
         return RealmsSelectFileToUploadScreen.this.getFocused() == this;
      }

      public void renderBackground(MatrixStack matrices) {
         RealmsSelectFileToUploadScreen.this.renderBackground(matrices);
      }

      public void setSelected(int index) {
         this.setSelectedItem(index);
         if (index != -1) {
            LevelSummary levelSummary = (LevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(index);
            String string = I18n.translate("narrator.select.list.position", index + 1, RealmsSelectFileToUploadScreen.this.levelList.size());
            String string2 = Realms.joinNarrations(Arrays.asList(levelSummary.getDisplayName(), RealmsSelectFileToUploadScreen.method_21404(levelSummary), RealmsSelectFileToUploadScreen.method_21400(levelSummary).getString(), string));
            Realms.narrateNow(I18n.translate("narrator.select", string2));
         }

      }

      public void setSelected(@Nullable RealmsSelectFileToUploadScreen.WorldListEntry worldListEntry) {
         super.setSelected(worldListEntry);
         RealmsSelectFileToUploadScreen.this.selectedWorld = this.children().indexOf(worldListEntry);
         RealmsSelectFileToUploadScreen.this.uploadButton.active = RealmsSelectFileToUploadScreen.this.selectedWorld >= 0 && RealmsSelectFileToUploadScreen.this.selectedWorld < this.getEntryCount() && !((LevelSummary)RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld)).isHardcore();
      }
   }
}
