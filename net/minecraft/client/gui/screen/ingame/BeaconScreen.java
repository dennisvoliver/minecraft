package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class BeaconScreen extends HandledScreen<BeaconScreenHandler> {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/beacon.png");
   private static final Text PRIMARY_POWER_TEXT = new TranslatableText("block.minecraft.beacon.primary");
   private static final Text SECONDARY_POWER_TEXT = new TranslatableText("block.minecraft.beacon.secondary");
   private BeaconScreen.DoneButtonWidget doneButton;
   private boolean consumeGem;
   private StatusEffect primaryEffect;
   private StatusEffect secondaryEffect;

   public BeaconScreen(final BeaconScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundWidth = 230;
      this.backgroundHeight = 219;
      handler.addListener(new ScreenHandlerListener() {
         public void onHandlerRegistered(ScreenHandler handlerx, DefaultedList<ItemStack> stacks) {
         }

         public void onSlotUpdate(ScreenHandler handlerx, int slotId, ItemStack stack) {
         }

         public void onPropertyUpdate(ScreenHandler handlerx, int property, int value) {
            BeaconScreen.this.primaryEffect = handler.getPrimaryEffect();
            BeaconScreen.this.secondaryEffect = handler.getSecondaryEffect();
            BeaconScreen.this.consumeGem = true;
         }
      });
   }

   protected void init() {
      super.init();
      this.doneButton = (BeaconScreen.DoneButtonWidget)this.addButton(new BeaconScreen.DoneButtonWidget(this.x + 164, this.y + 107));
      this.addButton(new BeaconScreen.CancelButtonWidget(this.x + 190, this.y + 107));
      this.consumeGem = true;
      this.doneButton.active = false;
   }

   public void tick() {
      super.tick();
      int i = ((BeaconScreenHandler)this.handler).getProperties();
      if (this.consumeGem && i >= 0) {
         this.consumeGem = false;

         int o;
         int p;
         int q;
         StatusEffect statusEffect2;
         BeaconScreen.EffectButtonWidget effectButtonWidget2;
         for(int j = 0; j <= 2; ++j) {
            o = BeaconBlockEntity.EFFECTS_BY_LEVEL[j].length;
            p = o * 22 + (o - 1) * 2;

            for(q = 0; q < o; ++q) {
               statusEffect2 = BeaconBlockEntity.EFFECTS_BY_LEVEL[j][q];
               effectButtonWidget2 = new BeaconScreen.EffectButtonWidget(this.x + 76 + q * 24 - p / 2, this.y + 22 + j * 25, statusEffect2, true);
               this.addButton(effectButtonWidget2);
               if (j >= i) {
                  effectButtonWidget2.active = false;
               } else if (statusEffect2 == this.primaryEffect) {
                  effectButtonWidget2.setDisabled(true);
               }
            }
         }

         int n = true;
         o = BeaconBlockEntity.EFFECTS_BY_LEVEL[3].length + 1;
         p = o * 22 + (o - 1) * 2;

         for(q = 0; q < o - 1; ++q) {
            statusEffect2 = BeaconBlockEntity.EFFECTS_BY_LEVEL[3][q];
            effectButtonWidget2 = new BeaconScreen.EffectButtonWidget(this.x + 167 + q * 24 - p / 2, this.y + 47, statusEffect2, false);
            this.addButton(effectButtonWidget2);
            if (3 >= i) {
               effectButtonWidget2.active = false;
            } else if (statusEffect2 == this.secondaryEffect) {
               effectButtonWidget2.setDisabled(true);
            }
         }

         if (this.primaryEffect != null) {
            BeaconScreen.EffectButtonWidget effectButtonWidget3 = new BeaconScreen.EffectButtonWidget(this.x + 167 + (o - 1) * 24 - p / 2, this.y + 47, this.primaryEffect, false);
            this.addButton(effectButtonWidget3);
            if (3 >= i) {
               effectButtonWidget3.active = false;
            } else if (this.primaryEffect == this.secondaryEffect) {
               effectButtonWidget3.setDisabled(true);
            }
         }
      }

      this.doneButton.active = ((BeaconScreenHandler)this.handler).hasPayment() && this.primaryEffect != null;
   }

   protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
      drawCenteredText(matrices, this.textRenderer, PRIMARY_POWER_TEXT, 62, 10, 14737632);
      drawCenteredText(matrices, this.textRenderer, SECONDARY_POWER_TEXT, 169, 10, 14737632);
      Iterator var4 = this.buttons.iterator();

      while(var4.hasNext()) {
         AbstractButtonWidget abstractButtonWidget = (AbstractButtonWidget)var4.next();
         if (abstractButtonWidget.isHovered()) {
            abstractButtonWidget.renderToolTip(matrices, mouseX - this.x, mouseY - this.y);
            break;
         }
      }

   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(TEXTURE);
      int i = (this.width - this.backgroundWidth) / 2;
      int j = (this.height - this.backgroundHeight) / 2;
      this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
      this.itemRenderer.zOffset = 100.0F;
      this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.NETHERITE_INGOT), i + 20, j + 109);
      this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.EMERALD), i + 41, j + 109);
      this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.DIAMOND), i + 41 + 22, j + 109);
      this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.GOLD_INGOT), i + 42 + 44, j + 109);
      this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.IRON_INGOT), i + 42 + 66, j + 109);
      this.itemRenderer.zOffset = 0.0F;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      super.render(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   @Environment(EnvType.CLIENT)
   class CancelButtonWidget extends BeaconScreen.IconButtonWidget {
      public CancelButtonWidget(int x, int y) {
         super(x, y, 112, 220);
      }

      public void onPress() {
         BeaconScreen.this.client.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(BeaconScreen.this.client.player.currentScreenHandler.syncId));
         BeaconScreen.this.client.openScreen((Screen)null);
      }

      public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
         BeaconScreen.this.renderTooltip(matrices, ScreenTexts.CANCEL, mouseX, mouseY);
      }
   }

   @Environment(EnvType.CLIENT)
   class DoneButtonWidget extends BeaconScreen.IconButtonWidget {
      public DoneButtonWidget(int x, int y) {
         super(x, y, 90, 220);
      }

      public void onPress() {
         BeaconScreen.this.client.getNetworkHandler().sendPacket(new UpdateBeaconC2SPacket(StatusEffect.getRawId(BeaconScreen.this.primaryEffect), StatusEffect.getRawId(BeaconScreen.this.secondaryEffect)));
         BeaconScreen.this.client.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(BeaconScreen.this.client.player.currentScreenHandler.syncId));
         BeaconScreen.this.client.openScreen((Screen)null);
      }

      public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
         BeaconScreen.this.renderTooltip(matrices, ScreenTexts.DONE, mouseX, mouseY);
      }
   }

   @Environment(EnvType.CLIENT)
   abstract static class IconButtonWidget extends BeaconScreen.BaseButtonWidget {
      private final int u;
      private final int v;

      protected IconButtonWidget(int x, int y, int u, int v) {
         super(x, y);
         this.u = u;
         this.v = v;
      }

      protected void renderExtra(MatrixStack matrices) {
         this.drawTexture(matrices, this.x + 2, this.y + 2, this.u, this.v, 18, 18);
      }
   }

   @Environment(EnvType.CLIENT)
   class EffectButtonWidget extends BeaconScreen.BaseButtonWidget {
      private final StatusEffect effect;
      private final Sprite sprite;
      private final boolean primary;
      private final Text tooltip;

      public EffectButtonWidget(int x, int y, StatusEffect statusEffect, boolean primary) {
         super(x, y);
         this.effect = statusEffect;
         this.sprite = MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(statusEffect);
         this.primary = primary;
         this.tooltip = this.getTextForEffect(statusEffect, primary);
      }

      private Text getTextForEffect(StatusEffect effect, boolean primary) {
         MutableText mutableText = new TranslatableText(effect.getTranslationKey());
         if (!primary && effect != StatusEffects.REGENERATION) {
            mutableText.append(" II");
         }

         return mutableText;
      }

      public void onPress() {
         if (!this.isDisabled()) {
            if (this.primary) {
               BeaconScreen.this.primaryEffect = this.effect;
            } else {
               BeaconScreen.this.secondaryEffect = this.effect;
            }

            BeaconScreen.this.buttons.clear();
            BeaconScreen.this.children.clear();
            BeaconScreen.this.init();
            BeaconScreen.this.tick();
         }
      }

      public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
         BeaconScreen.this.renderTooltip(matrices, this.tooltip, mouseX, mouseY);
      }

      protected void renderExtra(MatrixStack matrices) {
         MinecraftClient.getInstance().getTextureManager().bindTexture(this.sprite.getAtlas().getId());
         drawSprite(matrices, this.x + 2, this.y + 2, this.getZOffset(), 18, 18, this.sprite);
      }
   }

   @Environment(EnvType.CLIENT)
   abstract static class BaseButtonWidget extends AbstractPressableButtonWidget {
      private boolean disabled;

      protected BaseButtonWidget(int x, int y) {
         super(x, y, 22, 22, LiteralText.EMPTY);
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         MinecraftClient.getInstance().getTextureManager().bindTexture(BeaconScreen.TEXTURE);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         int i = true;
         int j = 0;
         if (!this.active) {
            j += this.width * 2;
         } else if (this.disabled) {
            j += this.width * 1;
         } else if (this.isHovered()) {
            j += this.width * 3;
         }

         this.drawTexture(matrices, this.x, this.y, j, 219, this.width, this.height);
         this.renderExtra(matrices);
      }

      protected abstract void renderExtra(MatrixStack matrices);

      public boolean isDisabled() {
         return this.disabled;
      }

      public void setDisabled(boolean disabled) {
         this.disabled = disabled;
      }
   }
}
