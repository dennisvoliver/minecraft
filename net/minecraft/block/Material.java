package net.minecraft.block;

import net.minecraft.block.piston.PistonBehavior;

public final class Material {
   public static final Material AIR;
   /**
    * Material for structure void block.
    */
   public static final Material STRUCTURE_VOID;
   /**
    * Material for the various portal blocks.
    */
   public static final Material PORTAL;
   public static final Material CARPET;
   /**
    * Material for plants such as flowers and crops
    */
   public static final Material PLANT;
   /**
    * Material for underwater plants without the replaceable property.
    */
   public static final Material UNDERWATER_PLANT;
   public static final Material REPLACEABLE_PLANT;
   /**
    * Material for crimson and warped roots, as well as Nether sprouts.
    */
   public static final Material NETHER_SHOOTS;
   public static final Material REPLACEABLE_UNDERWATER_PLANT;
   public static final Material WATER;
   public static final Material BUBBLE_COLUMN;
   public static final Material LAVA;
   /**
    * Material for non-full blocks of snow. Has the replaceable property.
    */
   public static final Material SNOW_LAYER;
   public static final Material FIRE;
   /**
    * Material for blocks that require a supporting block such as redstone components, torches, flower pots, and skulls.
    */
   public static final Material SUPPORTED;
   public static final Material COBWEB;
   public static final Material REDSTONE_LAMP;
   /**
    * Material for blocks that come from mobs such as honey, slime, or infested blocks. Includes clay but not bone blocks.
    */
   public static final Material ORGANIC_PRODUCT;
   /**
    * Material for the top layer of soil. Path, dirt, podzol, soul soil, farmland and similar.
    */
   public static final Material SOIL;
   /**
    * Organic blocks that are solid, including hay, target, and grass blocks.
    */
   public static final Material SOLID_ORGANIC;
   /**
    * Material for ice blocks that do not melt. See {@link #ICE} for meltable ice.
    */
   public static final Material DENSE_ICE;
   /**
    * A material or structure formed from a loosely compacted mass of fragments or particles.
    */
   public static final Material AGGREGATE;
   public static final Material SPONGE;
   public static final Material SHULKER_BOX;
   /**
    * Material for wood logs, and things crafted from them.
    */
   public static final Material WOOD;
   /**
    * Material for blocks crafted from Nether stems and hyphae.
    */
   public static final Material NETHER_WOOD;
   public static final Material BAMBOO_SAPLING;
   public static final Material BAMBOO;
   /**
    * Material for wool and bed blocks.
    */
   public static final Material WOOL;
   public static final Material TNT;
   public static final Material LEAVES;
   /**
    * Material for glass and glass-like blocks (includes sea lanterns and conduits).
    */
   public static final Material GLASS;
   /**
    * Material for ice that can melt. See {@link #DENSE_ICE} for unmeltable ice.
    */
   public static final Material ICE;
   public static final Material CACTUS;
   /**
    * Material for blocks that are stone or made from it, and generally prefer to be broken by a pickaxe.
    */
   public static final Material STONE;
   /**
    * Material for blocks metallic in nature, such as cauldrons, bells, iron doors, and iron trapdoors. It also includes non-obvious blocks such as brewing stands and compressed ore blocks, including diamond, redstone, and lapis blocks.
    */
   public static final Material METAL;
   /**
    * Material for full sized snow blocks.
    */
   public static final Material SNOW_BLOCK;
   /**
    * Material for blocks that can repair tools, including grindstone and anvils.
    */
   public static final Material REPAIR_STATION;
   public static final Material BARRIER;
   public static final Material PISTON;
   /**
    * Not in use, but has foliage color.
    */
   public static final Material UNUSED_PLANT;
   /**
    * Material for gourds. Includes the carved pumpkin and jack o' lantern.
    */
   public static final Material GOURD;
   /**
    * Material for egg blocks, such as dragon and turtle eggs.
    */
   public static final Material EGG;
   public static final Material CAKE;
   private final MaterialColor color;
   private final PistonBehavior pistonBehavior;
   private final boolean blocksMovement;
   private final boolean burnable;
   private final boolean liquid;
   private final boolean blocksLight;
   private final boolean replaceable;
   private final boolean solid;

   public Material(MaterialColor color, boolean liquid, boolean solid, boolean blocksMovement, boolean blocksLight, boolean breakByHand, boolean burnable, PistonBehavior pistonBehavior) {
      this.color = color;
      this.liquid = liquid;
      this.solid = solid;
      this.blocksMovement = blocksMovement;
      this.blocksLight = blocksLight;
      this.burnable = breakByHand;
      this.replaceable = burnable;
      this.pistonBehavior = pistonBehavior;
   }

   public boolean isLiquid() {
      return this.liquid;
   }

   public boolean isSolid() {
      return this.solid;
   }

   public boolean blocksMovement() {
      return this.blocksMovement;
   }

   public boolean isBurnable() {
      return this.burnable;
   }

   public boolean isReplaceable() {
      return this.replaceable;
   }

   public boolean blocksLight() {
      return this.blocksLight;
   }

   public PistonBehavior getPistonBehavior() {
      return this.pistonBehavior;
   }

   public MaterialColor getColor() {
      return this.color;
   }

   static {
      AIR = (new Material.Builder(MaterialColor.CLEAR)).allowsMovement().lightPassesThrough().notSolid().replaceable().build();
      STRUCTURE_VOID = (new Material.Builder(MaterialColor.CLEAR)).allowsMovement().lightPassesThrough().notSolid().replaceable().build();
      PORTAL = (new Material.Builder(MaterialColor.CLEAR)).allowsMovement().lightPassesThrough().notSolid().blocksPistons().build();
      CARPET = (new Material.Builder(MaterialColor.WEB)).allowsMovement().lightPassesThrough().notSolid().burnable().build();
      PLANT = (new Material.Builder(MaterialColor.FOLIAGE)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build();
      UNDERWATER_PLANT = (new Material.Builder(MaterialColor.WATER)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build();
      REPLACEABLE_PLANT = (new Material.Builder(MaterialColor.FOLIAGE)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().burnable().build();
      NETHER_SHOOTS = (new Material.Builder(MaterialColor.FOLIAGE)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().build();
      REPLACEABLE_UNDERWATER_PLANT = (new Material.Builder(MaterialColor.WATER)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().build();
      WATER = (new Material.Builder(MaterialColor.WATER)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().liquid().build();
      BUBBLE_COLUMN = (new Material.Builder(MaterialColor.WATER)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().liquid().build();
      LAVA = (new Material.Builder(MaterialColor.LAVA)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().liquid().build();
      SNOW_LAYER = (new Material.Builder(MaterialColor.WHITE)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().build();
      FIRE = (new Material.Builder(MaterialColor.CLEAR)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().build();
      SUPPORTED = (new Material.Builder(MaterialColor.CLEAR)).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build();
      COBWEB = (new Material.Builder(MaterialColor.WEB)).allowsMovement().lightPassesThrough().destroyedByPiston().build();
      REDSTONE_LAMP = (new Material.Builder(MaterialColor.CLEAR)).build();
      ORGANIC_PRODUCT = (new Material.Builder(MaterialColor.CLAY)).build();
      SOIL = (new Material.Builder(MaterialColor.DIRT)).build();
      SOLID_ORGANIC = (new Material.Builder(MaterialColor.GRASS)).build();
      DENSE_ICE = (new Material.Builder(MaterialColor.ICE)).build();
      AGGREGATE = (new Material.Builder(MaterialColor.SAND)).build();
      SPONGE = (new Material.Builder(MaterialColor.YELLOW)).build();
      SHULKER_BOX = (new Material.Builder(MaterialColor.PURPLE)).build();
      WOOD = (new Material.Builder(MaterialColor.WOOD)).burnable().build();
      NETHER_WOOD = (new Material.Builder(MaterialColor.WOOD)).build();
      BAMBOO_SAPLING = (new Material.Builder(MaterialColor.WOOD)).burnable().destroyedByPiston().allowsMovement().build();
      BAMBOO = (new Material.Builder(MaterialColor.WOOD)).burnable().destroyedByPiston().build();
      WOOL = (new Material.Builder(MaterialColor.WEB)).burnable().build();
      TNT = (new Material.Builder(MaterialColor.LAVA)).burnable().lightPassesThrough().build();
      LEAVES = (new Material.Builder(MaterialColor.FOLIAGE)).burnable().lightPassesThrough().destroyedByPiston().build();
      GLASS = (new Material.Builder(MaterialColor.CLEAR)).lightPassesThrough().build();
      ICE = (new Material.Builder(MaterialColor.ICE)).lightPassesThrough().build();
      CACTUS = (new Material.Builder(MaterialColor.FOLIAGE)).lightPassesThrough().destroyedByPiston().build();
      STONE = (new Material.Builder(MaterialColor.STONE)).build();
      METAL = (new Material.Builder(MaterialColor.IRON)).build();
      SNOW_BLOCK = (new Material.Builder(MaterialColor.WHITE)).build();
      REPAIR_STATION = (new Material.Builder(MaterialColor.IRON)).blocksPistons().build();
      BARRIER = (new Material.Builder(MaterialColor.CLEAR)).blocksPistons().build();
      PISTON = (new Material.Builder(MaterialColor.STONE)).blocksPistons().build();
      UNUSED_PLANT = (new Material.Builder(MaterialColor.FOLIAGE)).destroyedByPiston().build();
      GOURD = (new Material.Builder(MaterialColor.FOLIAGE)).destroyedByPiston().build();
      EGG = (new Material.Builder(MaterialColor.FOLIAGE)).destroyedByPiston().build();
      CAKE = (new Material.Builder(MaterialColor.CLEAR)).destroyedByPiston().build();
   }

   public static class Builder {
      private PistonBehavior pistonBehavior;
      private boolean blocksMovement;
      private boolean burnable;
      private boolean liquid;
      private boolean replaceable;
      private boolean solid;
      private final MaterialColor color;
      private boolean blocksLight;

      public Builder(MaterialColor color) {
         this.pistonBehavior = PistonBehavior.NORMAL;
         this.blocksMovement = true;
         this.solid = true;
         this.blocksLight = true;
         this.color = color;
      }

      public Material.Builder liquid() {
         this.liquid = true;
         return this;
      }

      public Material.Builder notSolid() {
         this.solid = false;
         return this;
      }

      public Material.Builder allowsMovement() {
         this.blocksMovement = false;
         return this;
      }

      private Material.Builder lightPassesThrough() {
         this.blocksLight = false;
         return this;
      }

      protected Material.Builder burnable() {
         this.burnable = true;
         return this;
      }

      public Material.Builder replaceable() {
         this.replaceable = true;
         return this;
      }

      protected Material.Builder destroyedByPiston() {
         this.pistonBehavior = PistonBehavior.DESTROY;
         return this;
      }

      protected Material.Builder blocksPistons() {
         this.pistonBehavior = PistonBehavior.BLOCK;
         return this;
      }

      public Material build() {
         return new Material(this.color, this.liquid, this.solid, this.blocksMovement, this.blocksLight, this.burnable, this.replaceable, this.pistonBehavior);
      }
   }
}
