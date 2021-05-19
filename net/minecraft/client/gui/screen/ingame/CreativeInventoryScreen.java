package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.HotbarStorage;
import net.minecraft.client.options.HotbarStorageEntry;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CreativeInventoryScreen extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
   private static final SimpleInventory INVENTORY = new SimpleInventory(45);
   private static final Text DELETE_ITEM_SLOT_TEXT = new TranslatableText("inventory.binSlot");
   private static int selectedTab;
   private float scrollPosition;
   private boolean scrolling;
   private TextFieldWidget searchBox;
   @Nullable
   private List<Slot> slots;
   @Nullable
   private Slot deleteItemSlot;
   private CreativeInventoryListener listener;
   private boolean ignoreTypedCharacter;
   private boolean lastClickOutsideBounds;
   private final Map<Identifier, Tag<Item>> searchResultTags = Maps.newTreeMap();

   public CreativeInventoryScreen(PlayerEntity player) {
      super(new CreativeInventoryScreen.CreativeScreenHandler(player), player.inventory, LiteralText.EMPTY);
      player.currentScreenHandler = this.handler;
      this.passEvents = true;
      this.backgroundHeight = 136;
      this.backgroundWidth = 195;
   }

   public void tick() {
      if (!this.client.interactionManager.hasCreativeInventory()) {
         this.client.openScreen(new InventoryScreen(this.client.player));
      } else if (this.searchBox != null) {
         this.searchBox.tick();
      }

   }

   protected void onMouseClick(@Nullable Slot slot, int invSlot, int clickData, SlotActionType actionType) {
      if (this.isCreativeInventorySlot(slot)) {
         this.searchBox.setCursorToEnd();
         this.searchBox.setSelectionEnd(0);
      }

      boolean bl = actionType == SlotActionType.QUICK_MOVE;
      actionType = invSlot == -999 && actionType == SlotActionType.PICKUP ? SlotActionType.THROW : actionType;
      ItemStack itemStack3;
      PlayerInventory playerInventory;
      if (slot == null && selectedTab != ItemGroup.INVENTORY.getIndex() && actionType != SlotActionType.QUICK_CRAFT) {
         playerInventory = this.client.player.inventory;
         if (!playerInventory.getCursorStack().isEmpty() && this.lastClickOutsideBounds) {
            if (clickData == 0) {
               this.client.player.dropItem(playerInventory.getCursorStack(), true);
               this.client.interactionManager.dropCreativeStack(playerInventory.getCursorStack());
               playerInventory.setCursorStack(ItemStack.EMPTY);
            }

            if (clickData == 1) {
               itemStack3 = playerInventory.getCursorStack().split(1);
               this.client.player.dropItem(itemStack3, true);
               this.client.interactionManager.dropCreativeStack(itemStack3);
            }
         }
      } else {
         if (slot != null && !slot.canTakeItems(this.client.player)) {
            return;
         }

         if (slot == this.deleteItemSlot && bl) {
            for(int i = 0; i < this.client.player.playerScreenHandler.getStacks().size(); ++i) {
               this.client.interactionManager.clickCreativeStack(ItemStack.EMPTY, i);
            }
         } else {
            ItemStack itemStack8;
            if (selectedTab == ItemGroup.INVENTORY.getIndex()) {
               if (slot == this.deleteItemSlot) {
                  this.client.player.inventory.setCursorStack(ItemStack.EMPTY);
               } else if (actionType == SlotActionType.THROW && slot != null && slot.hasStack()) {
                  itemStack8 = slot.takeStack(clickData == 0 ? 1 : slot.getStack().getMaxCount());
                  itemStack3 = slot.getStack();
                  this.client.player.dropItem(itemStack8, true);
                  this.client.interactionManager.dropCreativeStack(itemStack8);
                  this.client.interactionManager.clickCreativeStack(itemStack3, ((CreativeInventoryScreen.CreativeSlot)slot).slot.id);
               } else if (actionType == SlotActionType.THROW && !this.client.player.inventory.getCursorStack().isEmpty()) {
                  this.client.player.dropItem(this.client.player.inventory.getCursorStack(), true);
                  this.client.interactionManager.dropCreativeStack(this.client.player.inventory.getCursorStack());
                  this.client.player.inventory.setCursorStack(ItemStack.EMPTY);
               } else {
                  this.client.player.playerScreenHandler.onSlotClick(slot == null ? invSlot : ((CreativeInventoryScreen.CreativeSlot)slot).slot.id, clickData, actionType, this.client.player);
                  this.client.player.playerScreenHandler.sendContentUpdates();
               }
            } else {
               ItemStack itemStack10;
               if (actionType != SlotActionType.QUICK_CRAFT && slot.inventory == INVENTORY) {
                  playerInventory = this.client.player.inventory;
                  itemStack3 = playerInventory.getCursorStack();
                  ItemStack itemStack4 = slot.getStack();
                  if (actionType == SlotActionType.SWAP) {
                     if (!itemStack4.isEmpty()) {
                        itemStack10 = itemStack4.copy();
                        itemStack10.setCount(itemStack10.getMaxCount());
                        this.client.player.inventory.setStack(clickData, itemStack10);
                        this.client.player.playerScreenHandler.sendContentUpdates();
                     }

                     return;
                  }

                  if (actionType == SlotActionType.CLONE) {
                     if (playerInventory.getCursorStack().isEmpty() && slot.hasStack()) {
                        itemStack10 = slot.getStack().copy();
                        itemStack10.setCount(itemStack10.getMaxCount());
                        playerInventory.setCursorStack(itemStack10);
                     }

                     return;
                  }

                  if (actionType == SlotActionType.THROW) {
                     if (!itemStack4.isEmpty()) {
                        itemStack10 = itemStack4.copy();
                        itemStack10.setCount(clickData == 0 ? 1 : itemStack10.getMaxCount());
                        this.client.player.dropItem(itemStack10, true);
                        this.client.interactionManager.dropCreativeStack(itemStack10);
                     }

                     return;
                  }

                  if (!itemStack3.isEmpty() && !itemStack4.isEmpty() && itemStack3.isItemEqualIgnoreDamage(itemStack4) && ItemStack.areTagsEqual(itemStack3, itemStack4)) {
                     if (clickData == 0) {
                        if (bl) {
                           itemStack3.setCount(itemStack3.getMaxCount());
                        } else if (itemStack3.getCount() < itemStack3.getMaxCount()) {
                           itemStack3.increment(1);
                        }
                     } else {
                        itemStack3.decrement(1);
                     }
                  } else if (!itemStack4.isEmpty() && itemStack3.isEmpty()) {
                     playerInventory.setCursorStack(itemStack4.copy());
                     itemStack3 = playerInventory.getCursorStack();
                     if (bl) {
                        itemStack3.setCount(itemStack3.getMaxCount());
                     }
                  } else if (clickData == 0) {
                     playerInventory.setCursorStack(ItemStack.EMPTY);
                  } else {
                     playerInventory.getCursorStack().decrement(1);
                  }
               } else if (this.handler != null) {
                  itemStack8 = slot == null ? ItemStack.EMPTY : ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).getSlot(slot.id).getStack();
                  ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).onSlotClick(slot == null ? invSlot : slot.id, clickData, actionType, this.client.player);
                  if (ScreenHandler.unpackQuickCraftStage(clickData) == 2) {
                     for(int j = 0; j < 9; ++j) {
                        this.client.interactionManager.clickCreativeStack(((CreativeInventoryScreen.CreativeScreenHandler)this.handler).getSlot(45 + j).getStack(), 36 + j);
                     }
                  } else if (slot != null) {
                     itemStack3 = ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).getSlot(slot.id).getStack();
                     this.client.interactionManager.clickCreativeStack(itemStack3, slot.id - ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.size() + 9 + 36);
                     int k = 45 + clickData;
                     if (actionType == SlotActionType.SWAP) {
                        this.client.interactionManager.clickCreativeStack(itemStack8, k - ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.size() + 9 + 36);
                     } else if (actionType == SlotActionType.THROW && !itemStack8.isEmpty()) {
                        itemStack10 = itemStack8.copy();
                        itemStack10.setCount(clickData == 0 ? 1 : itemStack10.getMaxCount());
                        this.client.player.dropItem(itemStack10, true);
                        this.client.interactionManager.dropCreativeStack(itemStack10);
                     }

                     this.client.player.playerScreenHandler.sendContentUpdates();
                  }
               }
            }
         }
      }

   }

   private boolean isCreativeInventorySlot(@Nullable Slot slot) {
      return slot != null && slot.inventory == INVENTORY;
   }

   protected void applyStatusEffectOffset() {
      int i = this.x;
      super.applyStatusEffectOffset();
      if (this.searchBox != null && this.x != i) {
         this.searchBox.setX(this.x + 82);
      }

   }

   protected void init() {
      if (this.client.interactionManager.hasCreativeInventory()) {
         super.init();
         this.client.keyboard.setRepeatEvents(true);
         TextRenderer var10003 = this.textRenderer;
         int var10004 = this.x + 82;
         int var10005 = this.y + 6;
         this.textRenderer.getClass();
         this.searchBox = new TextFieldWidget(var10003, var10004, var10005, 80, 9, new TranslatableText("itemGroup.search"));
         this.searchBox.setMaxLength(50);
         this.searchBox.setDrawsBackground(false);
         this.searchBox.setVisible(false);
         this.searchBox.setEditableColor(16777215);
         this.children.add(this.searchBox);
         int i = selectedTab;
         selectedTab = -1;
         this.setSelectedTab(ItemGroup.GROUPS[i]);
         this.client.player.playerScreenHandler.removeListener(this.listener);
         this.listener = new CreativeInventoryListener(this.client);
         this.client.player.playerScreenHandler.addListener(this.listener);
      } else {
         this.client.openScreen(new InventoryScreen(this.client.player));
      }

   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.searchBox.getText();
      this.init(client, width, height);
      this.searchBox.setText(string);
      if (!this.searchBox.getText().isEmpty()) {
         this.search();
      }

   }

   public void removed() {
      super.removed();
      if (this.client.player != null && this.client.player.inventory != null) {
         this.client.player.playerScreenHandler.removeListener(this.listener);
      }

      this.client.keyboard.setRepeatEvents(false);
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.ignoreTypedCharacter) {
         return false;
      } else if (selectedTab != ItemGroup.SEARCH.getIndex()) {
         return false;
      } else {
         String string = this.searchBox.getText();
         if (this.searchBox.charTyped(chr, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
               this.search();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.ignoreTypedCharacter = false;
      if (selectedTab != ItemGroup.SEARCH.getIndex()) {
         if (this.client.options.keyChat.matchesKey(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            this.setSelectedTab(ItemGroup.SEARCH);
            return true;
         } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
         }
      } else {
         boolean bl = !this.isCreativeInventorySlot(this.focusedSlot) || this.focusedSlot.hasStack();
         boolean bl2 = InputUtil.fromKeyCode(keyCode, scanCode).method_30103().isPresent();
         if (bl && bl2 && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
         } else {
            String string = this.searchBox.getText();
            if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
               if (!Objects.equals(string, this.searchBox.getText())) {
                  this.search();
               }

               return true;
            } else {
               return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 ? true : super.keyPressed(keyCode, scanCode, modifiers);
            }
         }
      }
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      this.ignoreTypedCharacter = false;
      return super.keyReleased(keyCode, scanCode, modifiers);
   }

   private void search() {
      ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.clear();
      this.searchResultTags.clear();
      String string = this.searchBox.getText();
      if (string.isEmpty()) {
         Iterator var2 = Registry.ITEM.iterator();

         while(var2.hasNext()) {
            Item item = (Item)var2.next();
            item.appendStacks(ItemGroup.SEARCH, ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList);
         }
      } else {
         SearchableContainer searchable2;
         if (string.startsWith("#")) {
            string = string.substring(1);
            searchable2 = this.client.getSearchableContainer(SearchManager.ITEM_TAG);
            this.searchForTags(string);
         } else {
            searchable2 = this.client.getSearchableContainer(SearchManager.ITEM_TOOLTIP);
         }

         ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.addAll(searchable2.findAll(string.toLowerCase(Locale.ROOT)));
      }

      this.scrollPosition = 0.0F;
      ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).scrollItems(0.0F);
   }

   private void searchForTags(String string) {
      int i = string.indexOf(58);
      Predicate predicate2;
      if (i == -1) {
         predicate2 = (identifier) -> {
            return identifier.getPath().contains(string);
         };
      } else {
         String string2 = string.substring(0, i).trim();
         String string3 = string.substring(i + 1).trim();
         predicate2 = (identifier) -> {
            return identifier.getNamespace().contains(string2) && identifier.getPath().contains(string3);
         };
      }

      TagGroup<Item> tagGroup = ItemTags.getTagGroup();
      tagGroup.getTagIds().stream().filter(predicate2).forEach((identifier) -> {
         Tag var10000 = (Tag)this.searchResultTags.put(identifier, tagGroup.getTag(identifier));
      });
   }

   protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
      ItemGroup itemGroup = ItemGroup.GROUPS[selectedTab];
      if (itemGroup.shouldRenderName()) {
         RenderSystem.disableBlend();
         this.textRenderer.draw(matrices, itemGroup.getTranslationKey(), 8.0F, 6.0F, 4210752);
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         double d = mouseX - (double)this.x;
         double e = mouseY - (double)this.y;
         ItemGroup[] var10 = ItemGroup.GROUPS;
         int var11 = var10.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            ItemGroup itemGroup = var10[var12];
            if (this.isClickInTab(itemGroup, d, e)) {
               return true;
            }
         }

         if (selectedTab != ItemGroup.INVENTORY.getIndex() && this.isClickInScrollbar(mouseX, mouseY)) {
            this.scrolling = this.hasScrollbar();
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         double d = mouseX - (double)this.x;
         double e = mouseY - (double)this.y;
         this.scrolling = false;
         ItemGroup[] var10 = ItemGroup.GROUPS;
         int var11 = var10.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            ItemGroup itemGroup = var10[var12];
            if (this.isClickInTab(itemGroup, d, e)) {
               this.setSelectedTab(itemGroup);
               return true;
            }
         }
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   private boolean hasScrollbar() {
      return selectedTab != ItemGroup.INVENTORY.getIndex() && ItemGroup.GROUPS[selectedTab].hasScrollbar() && ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).shouldShowScrollbar();
   }

   private void setSelectedTab(ItemGroup group) {
      int i = selectedTab;
      selectedTab = group.getIndex();
      this.cursorDragSlots.clear();
      ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.clear();
      int l;
      int aa;
      if (group == ItemGroup.HOTBAR) {
         HotbarStorage hotbarStorage = this.client.getCreativeHotbarStorage();

         for(l = 0; l < 9; ++l) {
            HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(l);
            if (hotbarStorageEntry.isEmpty()) {
               for(aa = 0; aa < 9; ++aa) {
                  if (aa == l) {
                     ItemStack itemStack = new ItemStack(Items.PAPER);
                     itemStack.getOrCreateSubTag("CustomCreativeLock");
                     Text text = this.client.options.keysHotbar[l].getBoundKeyLocalizedText();
                     Text text2 = this.client.options.keySaveToolbarActivator.getBoundKeyLocalizedText();
                     itemStack.setCustomName(new TranslatableText("inventory.hotbarInfo", new Object[]{text2, text}));
                     ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.add(itemStack);
                  } else {
                     ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.add(ItemStack.EMPTY);
                  }
               }
            } else {
               ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.addAll(hotbarStorageEntry);
            }
         }
      } else if (group != ItemGroup.SEARCH) {
         group.appendStacks(((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList);
      }

      if (group == ItemGroup.INVENTORY) {
         ScreenHandler screenHandler = this.client.player.playerScreenHandler;
         if (this.slots == null) {
            this.slots = ImmutableList.copyOf((Collection)((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots);
         }

         ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.clear();

         for(l = 0; l < screenHandler.slots.size(); ++l) {
            int t;
            int v;
            int w;
            int x;
            if (l >= 5 && l < 9) {
               v = l - 5;
               w = v / 2;
               x = v % 2;
               t = 54 + w * 54;
               aa = 6 + x * 27;
            } else if (l >= 0 && l < 5) {
               t = -2000;
               aa = -2000;
            } else if (l == 45) {
               t = 35;
               aa = 20;
            } else {
               v = l - 9;
               w = v % 9;
               x = v / 9;
               t = 9 + w * 18;
               if (l >= 36) {
                  aa = 112;
               } else {
                  aa = 54 + x * 18;
               }
            }

            Slot slot = new CreativeInventoryScreen.CreativeSlot((Slot)screenHandler.slots.get(l), l, t, aa);
            ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.add(slot);
         }

         this.deleteItemSlot = new Slot(INVENTORY, 0, 173, 112);
         ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.add(this.deleteItemSlot);
      } else if (i == ItemGroup.INVENTORY.getIndex()) {
         ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.clear();
         ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.addAll(this.slots);
         this.slots = null;
      }

      if (this.searchBox != null) {
         if (group == ItemGroup.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setFocusUnlocked(false);
            this.searchBox.setTextFieldFocused(true);
            if (i != group.getIndex()) {
               this.searchBox.setText("");
            }

            this.search();
         } else {
            this.searchBox.setVisible(false);
            this.searchBox.setFocusUnlocked(true);
            this.searchBox.setTextFieldFocused(false);
            this.searchBox.setText("");
         }
      }

      this.scrollPosition = 0.0F;
      ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).scrollItems(0.0F);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (!this.hasScrollbar()) {
         return false;
      } else {
         int i = (((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.size() + 9 - 1) / 9 - 5;
         this.scrollPosition = (float)((double)this.scrollPosition - amount / (double)i);
         this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
         ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
         return true;
      }
   }

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
      boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
      this.lastClickOutsideBounds = bl && !this.isClickInTab(ItemGroup.GROUPS[selectedTab], mouseX, mouseY);
      return this.lastClickOutsideBounds;
   }

   protected boolean isClickInScrollbar(double mouseX, double mouseY) {
      int i = this.x;
      int j = this.y;
      int k = i + 175;
      int l = j + 18;
      int m = k + 14;
      int n = l + 112;
      return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)m && mouseY < (double)n;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.scrolling) {
         int i = this.y + 18;
         int j = i + 112;
         this.scrollPosition = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
         this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
         ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      super.render(matrices, mouseX, mouseY, delta);
      ItemGroup[] var5 = ItemGroup.GROUPS;
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         ItemGroup itemGroup = var5[var7];
         if (this.renderTabTooltipIfHovered(matrices, itemGroup, mouseX, mouseY)) {
            break;
         }
      }

      if (this.deleteItemSlot != null && selectedTab == ItemGroup.INVENTORY.getIndex() && this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, (double)mouseX, (double)mouseY)) {
         this.renderTooltip(matrices, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
      }

      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
      if (selectedTab == ItemGroup.SEARCH.getIndex()) {
         List<Text> list = stack.getTooltip(this.client.player, this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
         List<Text> list2 = Lists.newArrayList((Iterable)list);
         Item item = stack.getItem();
         ItemGroup itemGroup = item.getGroup();
         if (itemGroup == null && item == Items.ENCHANTED_BOOK) {
            Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);
            if (map.size() == 1) {
               Enchantment enchantment = (Enchantment)map.keySet().iterator().next();
               ItemGroup[] var11 = ItemGroup.GROUPS;
               int var12 = var11.length;

               for(int var13 = 0; var13 < var12; ++var13) {
                  ItemGroup itemGroup2 = var11[var13];
                  if (itemGroup2.containsEnchantments(enchantment.type)) {
                     itemGroup = itemGroup2;
                     break;
                  }
               }
            }
         }

         this.searchResultTags.forEach((identifier, tag) -> {
            if (tag.contains(item)) {
               list2.add(1, (new LiteralText("#" + identifier)).formatted(Formatting.DARK_PURPLE));
            }

         });
         if (itemGroup != null) {
            list2.add(1, itemGroup.getTranslationKey().shallowCopy().formatted(Formatting.BLUE));
         }

         this.renderTooltip(matrices, list2, x, y);
      } else {
         super.renderTooltip(matrices, stack, x, y);
      }

   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      ItemGroup itemGroup = ItemGroup.GROUPS[selectedTab];
      ItemGroup[] var6 = ItemGroup.GROUPS;
      int j = var6.length;

      int k;
      for(k = 0; k < j; ++k) {
         ItemGroup itemGroup2 = var6[k];
         this.client.getTextureManager().bindTexture(TEXTURE);
         if (itemGroup2.getIndex() != selectedTab) {
            this.renderTabIcon(matrices, itemGroup2);
         }
      }

      this.client.getTextureManager().bindTexture(new Identifier("textures/gui/container/creative_inventory/tab_" + itemGroup.getTexture()));
      this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
      this.searchBox.render(matrices, mouseX, mouseY, delta);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      int i = this.x + 175;
      j = this.y + 18;
      k = j + 112;
      this.client.getTextureManager().bindTexture(TEXTURE);
      if (itemGroup.hasScrollbar()) {
         this.drawTexture(matrices, i, j + (int)((float)(k - j - 17) * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
      }

      this.renderTabIcon(matrices, itemGroup);
      if (itemGroup == ItemGroup.INVENTORY) {
         InventoryScreen.drawEntity(this.x + 88, this.y + 45, 20, (float)(this.x + 88 - mouseX), (float)(this.y + 45 - 30 - mouseY), this.client.player);
      }

   }

   protected boolean isClickInTab(ItemGroup group, double mouseX, double mouseY) {
      int i = group.getColumn();
      int j = 28 * i;
      int k = 0;
      if (group.isSpecial()) {
         j = this.backgroundWidth - 28 * (6 - i) + 2;
      } else if (i > 0) {
         j += i;
      }

      int k;
      if (group.isTopRow()) {
         k = k - 32;
      } else {
         k = k + this.backgroundHeight;
      }

      return mouseX >= (double)j && mouseX <= (double)(j + 28) && mouseY >= (double)k && mouseY <= (double)(k + 32);
   }

   protected boolean renderTabTooltipIfHovered(MatrixStack matrixStack, ItemGroup itemGroup, int i, int j) {
      int k = itemGroup.getColumn();
      int l = 28 * k;
      int m = 0;
      if (itemGroup.isSpecial()) {
         l = this.backgroundWidth - 28 * (6 - k) + 2;
      } else if (k > 0) {
         l += k;
      }

      int m;
      if (itemGroup.isTopRow()) {
         m = m - 32;
      } else {
         m = m + this.backgroundHeight;
      }

      if (this.isPointWithinBounds(l + 3, m + 3, 23, 27, (double)i, (double)j)) {
         this.renderTooltip(matrixStack, itemGroup.getTranslationKey(), i, j);
         return true;
      } else {
         return false;
      }
   }

   protected void renderTabIcon(MatrixStack matrixStack, ItemGroup itemGroup) {
      boolean bl = itemGroup.getIndex() == selectedTab;
      boolean bl2 = itemGroup.isTopRow();
      int i = itemGroup.getColumn();
      int j = i * 28;
      int k = 0;
      int l = this.x + 28 * i;
      int m = this.y;
      int n = true;
      if (bl) {
         k += 32;
      }

      if (itemGroup.isSpecial()) {
         l = this.x + this.backgroundWidth - 28 * (6 - i);
      } else if (i > 0) {
         l += i;
      }

      if (bl2) {
         m -= 28;
      } else {
         k += 64;
         m += this.backgroundHeight - 4;
      }

      this.drawTexture(matrixStack, l, m, j, k, 28, 32);
      this.itemRenderer.zOffset = 100.0F;
      l += 6;
      m += 8 + (bl2 ? 1 : -1);
      RenderSystem.enableRescaleNormal();
      ItemStack itemStack = itemGroup.getIcon();
      this.itemRenderer.renderInGuiWithOverrides(itemStack, l, m);
      this.itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack, l, m);
      this.itemRenderer.zOffset = 0.0F;
   }

   public int getSelectedTab() {
      return selectedTab;
   }

   public static void onHotbarKeyPress(MinecraftClient client, int index, boolean restore, boolean save) {
      ClientPlayerEntity clientPlayerEntity = client.player;
      HotbarStorage hotbarStorage = client.getCreativeHotbarStorage();
      HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(index);
      int j;
      if (restore) {
         for(j = 0; j < PlayerInventory.getHotbarSize(); ++j) {
            ItemStack itemStack = ((ItemStack)hotbarStorageEntry.get(j)).copy();
            clientPlayerEntity.inventory.setStack(j, itemStack);
            client.interactionManager.clickCreativeStack(itemStack, 36 + j);
         }

         clientPlayerEntity.playerScreenHandler.sendContentUpdates();
      } else if (save) {
         for(j = 0; j < PlayerInventory.getHotbarSize(); ++j) {
            hotbarStorageEntry.set(j, clientPlayerEntity.inventory.getStack(j).copy());
         }

         Text text = client.options.keysHotbar[index].getBoundKeyLocalizedText();
         Text text2 = client.options.keyLoadToolbarActivator.getBoundKeyLocalizedText();
         client.inGameHud.setOverlayMessage(new TranslatableText("inventory.hotbarSaved", new Object[]{text2, text}), false);
         hotbarStorage.save();
      }

   }

   static {
      selectedTab = ItemGroup.BUILDING_BLOCKS.getIndex();
   }

   @Environment(EnvType.CLIENT)
   static class LockableSlot extends Slot {
      public LockableSlot(Inventory inventory, int i, int j, int k) {
         super(inventory, i, j, k);
      }

      public boolean canTakeItems(PlayerEntity playerEntity) {
         if (super.canTakeItems(playerEntity) && this.hasStack()) {
            return this.getStack().getSubTag("CustomCreativeLock") == null;
         } else {
            return !this.hasStack();
         }
      }
   }

   @Environment(EnvType.CLIENT)
   static class CreativeSlot extends Slot {
      private final Slot slot;

      public CreativeSlot(Slot slot, int invSlot, int x, int y) {
         super(slot.inventory, invSlot, x, y);
         this.slot = slot;
      }

      public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
         return this.slot.onTakeItem(player, stack);
      }

      public boolean canInsert(ItemStack stack) {
         return this.slot.canInsert(stack);
      }

      public ItemStack getStack() {
         return this.slot.getStack();
      }

      public boolean hasStack() {
         return this.slot.hasStack();
      }

      public void setStack(ItemStack stack) {
         this.slot.setStack(stack);
      }

      public void markDirty() {
         this.slot.markDirty();
      }

      public int getMaxItemCount() {
         return this.slot.getMaxItemCount();
      }

      public int getMaxItemCount(ItemStack stack) {
         return this.slot.getMaxItemCount(stack);
      }

      @Nullable
      public Pair<Identifier, Identifier> getBackgroundSprite() {
         return this.slot.getBackgroundSprite();
      }

      public ItemStack takeStack(int amount) {
         return this.slot.takeStack(amount);
      }

      public boolean doDrawHoveringEffect() {
         return this.slot.doDrawHoveringEffect();
      }

      public boolean canTakeItems(PlayerEntity playerEntity) {
         return this.slot.canTakeItems(playerEntity);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class CreativeScreenHandler extends ScreenHandler {
      public final DefaultedList<ItemStack> itemList = DefaultedList.of();

      public CreativeScreenHandler(PlayerEntity playerEntity) {
         super((ScreenHandlerType)null, 0);
         PlayerInventory playerInventory = playerEntity.inventory;

         int k;
         for(k = 0; k < 5; ++k) {
            for(int j = 0; j < 9; ++j) {
               this.addSlot(new CreativeInventoryScreen.LockableSlot(CreativeInventoryScreen.INVENTORY, k * 9 + j, 9 + j * 18, 18 + k * 18));
            }
         }

         for(k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 9 + k * 18, 112));
         }

         this.scrollItems(0.0F);
      }

      public boolean canUse(PlayerEntity player) {
         return true;
      }

      public void scrollItems(float position) {
         int i = (this.itemList.size() + 9 - 1) / 9 - 5;
         int j = (int)((double)(position * (float)i) + 0.5D);
         if (j < 0) {
            j = 0;
         }

         for(int k = 0; k < 5; ++k) {
            for(int l = 0; l < 9; ++l) {
               int m = l + (k + j) * 9;
               if (m >= 0 && m < this.itemList.size()) {
                  CreativeInventoryScreen.INVENTORY.setStack(l + k * 9, (ItemStack)this.itemList.get(m));
               } else {
                  CreativeInventoryScreen.INVENTORY.setStack(l + k * 9, ItemStack.EMPTY);
               }
            }
         }

      }

      public boolean shouldShowScrollbar() {
         return this.itemList.size() > 45;
      }

      public ItemStack transferSlot(PlayerEntity player, int index) {
         if (index >= this.slots.size() - 9 && index < this.slots.size()) {
            Slot slot = (Slot)this.slots.get(index);
            if (slot != null && slot.hasStack()) {
               slot.setStack(ItemStack.EMPTY);
            }
         }

         return ItemStack.EMPTY;
      }

      public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
         return slot.inventory != CreativeInventoryScreen.INVENTORY;
      }

      public boolean canInsertIntoSlot(Slot slot) {
         return slot.inventory != CreativeInventoryScreen.INVENTORY;
      }
   }
}
