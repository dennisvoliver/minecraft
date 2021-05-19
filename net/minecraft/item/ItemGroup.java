package net.minecraft.item;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public abstract class ItemGroup {
   public static final ItemGroup[] GROUPS = new ItemGroup[12];
   public static final ItemGroup BUILDING_BLOCKS = (new ItemGroup(0, "buildingBlocks") {
      @Environment(EnvType.CLIENT)
      public ItemStack createIcon() {
         return new ItemStack(Blocks.BRICKS);
      }
   }).setName("building_blocks");
   public static final ItemGroup DECORATIONS = new ItemGroup(1, "decorations") {
      @Environment(EnvType.CLIENT)
      public ItemStack createIcon() {
         return new ItemStack(Blocks.PEONY);
      }
   };
   public static final ItemGroup REDSTONE = new ItemGroup(2, "redstone") {
      @Environment(EnvType.CLIENT)
      public ItemStack createIcon() {
         return new ItemStack(Items.REDSTONE);
      }
   };
   public static final ItemGroup TRANSPORTATION = new ItemGroup(3, "transportation") {
      @Environment(EnvType.CLIENT)
      public ItemStack createIcon() {
         return new ItemStack(Blocks.POWERED_RAIL);
      }
   };
   public static final ItemGroup MISC = new ItemGroup(6, "misc") {
      @Environment(EnvType.CLIENT)
      public ItemStack createIcon() {
         return new ItemStack(Items.LAVA_BUCKET);
      }
   };
   public static final ItemGroup SEARCH = (new ItemGroup(5, "search") {
      @Environment(EnvType.CLIENT)
      public ItemStack createIcon() {
         return new ItemStack(Items.COMPASS);
      }
   }).setTexture("item_search.png");
   public static final ItemGroup FOOD = new ItemGroup(7, "food") {
      @Environment(EnvType.CLIENT)
      public ItemStack createIcon() {
         return new ItemStack(Items.APPLE);
      }
   };
   public static final ItemGroup TOOLS;
   public static final ItemGroup COMBAT;
   public static final ItemGroup BREWING;
   public static final ItemGroup MATERIALS;
   public static final ItemGroup HOTBAR;
   public static final ItemGroup INVENTORY;
   private final int index;
   private final String id;
   private final Text translationKey;
   private String name;
   private String texture = "items.png";
   private boolean scrollbar = true;
   private boolean renderName = true;
   private EnchantmentTarget[] enchantments = new EnchantmentTarget[0];
   private ItemStack icon;

   public ItemGroup(int index, String id) {
      this.index = index;
      this.id = id;
      this.translationKey = new TranslatableText("itemGroup." + id);
      this.icon = ItemStack.EMPTY;
      GROUPS[index] = this;
   }

   @Environment(EnvType.CLIENT)
   public int getIndex() {
      return this.index;
   }

   public String getName() {
      return this.name == null ? this.id : this.name;
   }

   @Environment(EnvType.CLIENT)
   public Text getTranslationKey() {
      return this.translationKey;
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getIcon() {
      if (this.icon.isEmpty()) {
         this.icon = this.createIcon();
      }

      return this.icon;
   }

   @Environment(EnvType.CLIENT)
   public abstract ItemStack createIcon();

   @Environment(EnvType.CLIENT)
   public String getTexture() {
      return this.texture;
   }

   public ItemGroup setTexture(String texture) {
      this.texture = texture;
      return this;
   }

   public ItemGroup setName(String name) {
      this.name = name;
      return this;
   }

   /**
    * Checks if this item group should render its name.
    * 
    * <p>The name is rendered below the top row of item groups and above the inventory.
    */
   @Environment(EnvType.CLIENT)
   public boolean shouldRenderName() {
      return this.renderName;
   }

   /**
    * Specifies that when this item group is selected, the name of the item group should not be rendered.
    */
   public ItemGroup hideName() {
      this.renderName = false;
      return this;
   }

   @Environment(EnvType.CLIENT)
   public boolean hasScrollbar() {
      return this.scrollbar;
   }

   public ItemGroup setNoScrollbar() {
      this.scrollbar = false;
      return this;
   }

   @Environment(EnvType.CLIENT)
   public int getColumn() {
      return this.index % 6;
   }

   @Environment(EnvType.CLIENT)
   public boolean isTopRow() {
      return this.index < 6;
   }

   @Environment(EnvType.CLIENT)
   public boolean isSpecial() {
      return this.getColumn() == 5;
   }

   public EnchantmentTarget[] getEnchantments() {
      return this.enchantments;
   }

   public ItemGroup setEnchantments(EnchantmentTarget... targets) {
      this.enchantments = targets;
      return this;
   }

   public boolean containsEnchantments(@Nullable EnchantmentTarget target) {
      if (target != null) {
         EnchantmentTarget[] var2 = this.enchantments;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            EnchantmentTarget enchantmentTarget = var2[var4];
            if (enchantmentTarget == target) {
               return true;
            }
         }
      }

      return false;
   }

   @Environment(EnvType.CLIENT)
   public void appendStacks(DefaultedList<ItemStack> stacks) {
      Iterator var2 = Registry.ITEM.iterator();

      while(var2.hasNext()) {
         Item item = (Item)var2.next();
         item.appendStacks(this, stacks);
      }

   }

   static {
      TOOLS = (new ItemGroup(8, "tools") {
         @Environment(EnvType.CLIENT)
         public ItemStack createIcon() {
            return new ItemStack(Items.IRON_AXE);
         }
      }).setEnchantments(new EnchantmentTarget[]{EnchantmentTarget.VANISHABLE, EnchantmentTarget.DIGGER, EnchantmentTarget.FISHING_ROD, EnchantmentTarget.BREAKABLE});
      COMBAT = (new ItemGroup(9, "combat") {
         @Environment(EnvType.CLIENT)
         public ItemStack createIcon() {
            return new ItemStack(Items.GOLDEN_SWORD);
         }
      }).setEnchantments(new EnchantmentTarget[]{EnchantmentTarget.VANISHABLE, EnchantmentTarget.ARMOR, EnchantmentTarget.ARMOR_FEET, EnchantmentTarget.ARMOR_HEAD, EnchantmentTarget.ARMOR_LEGS, EnchantmentTarget.ARMOR_CHEST, EnchantmentTarget.BOW, EnchantmentTarget.WEAPON, EnchantmentTarget.WEARABLE, EnchantmentTarget.BREAKABLE, EnchantmentTarget.TRIDENT, EnchantmentTarget.CROSSBOW});
      BREWING = new ItemGroup(10, "brewing") {
         @Environment(EnvType.CLIENT)
         public ItemStack createIcon() {
            return PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER);
         }
      };
      MATERIALS = MISC;
      HOTBAR = new ItemGroup(4, "hotbar") {
         @Environment(EnvType.CLIENT)
         public ItemStack createIcon() {
            return new ItemStack(Blocks.BOOKSHELF);
         }

         @Environment(EnvType.CLIENT)
         public void appendStacks(DefaultedList<ItemStack> stacks) {
            throw new RuntimeException("Implement exception client-side.");
         }

         @Environment(EnvType.CLIENT)
         public boolean isSpecial() {
            return true;
         }
      };
      INVENTORY = (new ItemGroup(11, "inventory") {
         @Environment(EnvType.CLIENT)
         public ItemStack createIcon() {
            return new ItemStack(Blocks.CHEST);
         }
      }).setTexture("inventory.png").setNoScrollbar().hideName();
   }
}
