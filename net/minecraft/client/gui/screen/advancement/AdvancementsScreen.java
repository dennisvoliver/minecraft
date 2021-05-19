package net.minecraft.client.gui.screen.advancement;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AdvancementsScreen extends Screen implements ClientAdvancementManager.Listener {
   private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
   private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
   private static final Text SAD_LABEL_TEXT = new TranslatableText("advancements.sad_label");
   private static final Text EMPTY_TEXT = new TranslatableText("advancements.empty");
   private static final Text ADVANCEMENTS_TEXT = new TranslatableText("gui.advancements");
   private final ClientAdvancementManager advancementHandler;
   private final Map<Advancement, AdvancementTab> tabs = Maps.newLinkedHashMap();
   private AdvancementTab selectedTab;
   private boolean movingTab;

   public AdvancementsScreen(ClientAdvancementManager advancementHandler) {
      super(NarratorManager.EMPTY);
      this.advancementHandler = advancementHandler;
   }

   protected void init() {
      this.tabs.clear();
      this.selectedTab = null;
      this.advancementHandler.setListener(this);
      if (this.selectedTab == null && !this.tabs.isEmpty()) {
         this.advancementHandler.selectTab(((AdvancementTab)this.tabs.values().iterator().next()).getRoot(), true);
      } else {
         this.advancementHandler.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot(), true);
      }

   }

   public void removed() {
      this.advancementHandler.setListener((ClientAdvancementManager.Listener)null);
      ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
      if (clientPlayNetworkHandler != null) {
         clientPlayNetworkHandler.sendPacket(AdvancementTabC2SPacket.close());
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         int i = (this.width - 252) / 2;
         int j = (this.height - 140) / 2;
         Iterator var8 = this.tabs.values().iterator();

         while(var8.hasNext()) {
            AdvancementTab advancementTab = (AdvancementTab)var8.next();
            if (advancementTab.isClickOnTab(i, j, mouseX, mouseY)) {
               this.advancementHandler.selectTab(advancementTab.getRoot(), true);
               break;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.client.options.keyAdvancements.matchesKey(keyCode, scanCode)) {
         this.client.openScreen((Screen)null);
         this.client.mouse.lockCursor();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      int i = (this.width - 252) / 2;
      int j = (this.height - 140) / 2;
      this.renderBackground(matrices);
      this.drawAdvancementTree(matrices, mouseX, mouseY, i, j);
      this.drawWidgets(matrices, i, j);
      this.drawWidgetTooltip(matrices, mouseX, mouseY, i, j);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (button != 0) {
         this.movingTab = false;
         return false;
      } else {
         if (!this.movingTab) {
            this.movingTab = true;
         } else if (this.selectedTab != null) {
            this.selectedTab.move(deltaX, deltaY);
         }

         return true;
      }
   }

   private void drawAdvancementTree(MatrixStack matrixStack, int mouseY, int i, int j, int k) {
      AdvancementTab advancementTab = this.selectedTab;
      if (advancementTab == null) {
         fill(matrixStack, j + 9, k + 18, j + 9 + 234, k + 18 + 113, -16777216);
         int l = j + 9 + 117;
         TextRenderer var10001 = this.textRenderer;
         Text var10002 = EMPTY_TEXT;
         int var10004 = k + 18 + 56;
         this.textRenderer.getClass();
         drawCenteredText(matrixStack, var10001, var10002, l, var10004 - 9 / 2, -1);
         var10001 = this.textRenderer;
         var10002 = SAD_LABEL_TEXT;
         var10004 = k + 18 + 113;
         this.textRenderer.getClass();
         drawCenteredText(matrixStack, var10001, var10002, l, var10004 - 9, -1);
      } else {
         RenderSystem.pushMatrix();
         RenderSystem.translatef((float)(j + 9), (float)(k + 18), 0.0F);
         advancementTab.render(matrixStack);
         RenderSystem.popMatrix();
         RenderSystem.depthFunc(515);
         RenderSystem.disableDepthTest();
      }
   }

   public void drawWidgets(MatrixStack matrixStack, int i, int j) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      this.client.getTextureManager().bindTexture(WINDOW_TEXTURE);
      this.drawTexture(matrixStack, i, j, 0, 0, 252, 140);
      if (this.tabs.size() > 1) {
         this.client.getTextureManager().bindTexture(TABS_TEXTURE);
         Iterator var4 = this.tabs.values().iterator();

         AdvancementTab advancementTab2;
         while(var4.hasNext()) {
            advancementTab2 = (AdvancementTab)var4.next();
            advancementTab2.drawBackground(matrixStack, i, j, advancementTab2 == this.selectedTab);
         }

         RenderSystem.enableRescaleNormal();
         RenderSystem.defaultBlendFunc();
         var4 = this.tabs.values().iterator();

         while(var4.hasNext()) {
            advancementTab2 = (AdvancementTab)var4.next();
            advancementTab2.drawIcon(i, j, this.itemRenderer);
         }

         RenderSystem.disableBlend();
      }

      this.textRenderer.draw(matrixStack, ADVANCEMENTS_TEXT, (float)(i + 8), (float)(j + 6), 4210752);
   }

   private void drawWidgetTooltip(MatrixStack matrixStack, int i, int j, int k, int l) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.selectedTab != null) {
         RenderSystem.pushMatrix();
         RenderSystem.enableDepthTest();
         RenderSystem.translatef((float)(k + 9), (float)(l + 18), 400.0F);
         this.selectedTab.drawWidgetTooltip(matrixStack, i - k - 9, j - l - 18, k, l);
         RenderSystem.disableDepthTest();
         RenderSystem.popMatrix();
      }

      if (this.tabs.size() > 1) {
         Iterator var6 = this.tabs.values().iterator();

         while(var6.hasNext()) {
            AdvancementTab advancementTab = (AdvancementTab)var6.next();
            if (advancementTab.isClickOnTab(k, l, (double)i, (double)j)) {
               this.renderTooltip(matrixStack, advancementTab.getTitle(), i, j);
            }
         }
      }

   }

   public void onRootAdded(Advancement root) {
      AdvancementTab advancementTab = AdvancementTab.create(this.client, this, this.tabs.size(), root);
      if (advancementTab != null) {
         this.tabs.put(root, advancementTab);
      }
   }

   public void onRootRemoved(Advancement root) {
   }

   public void onDependentAdded(Advancement dependent) {
      AdvancementTab advancementTab = this.getTab(dependent);
      if (advancementTab != null) {
         advancementTab.addAdvancement(dependent);
      }

   }

   public void onDependentRemoved(Advancement dependent) {
   }

   public void setProgress(Advancement advancement, AdvancementProgress progress) {
      AdvancementWidget advancementWidget = this.getAdvancementWidget(advancement);
      if (advancementWidget != null) {
         advancementWidget.setProgress(progress);
      }

   }

   public void selectTab(@Nullable Advancement advancement) {
      this.selectedTab = (AdvancementTab)this.tabs.get(advancement);
   }

   public void onClear() {
      this.tabs.clear();
      this.selectedTab = null;
   }

   @Nullable
   public AdvancementWidget getAdvancementWidget(Advancement advancement) {
      AdvancementTab advancementTab = this.getTab(advancement);
      return advancementTab == null ? null : advancementTab.getWidget(advancement);
   }

   @Nullable
   private AdvancementTab getTab(Advancement advancement) {
      while(advancement.getParent() != null) {
         advancement = advancement.getParent();
      }

      return (AdvancementTab)this.tabs.get(advancement);
   }
}
