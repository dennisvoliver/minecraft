package net.minecraft.data.client.model;

import org.jetbrains.annotations.Nullable;

public final class TextureKey {
   public static final TextureKey ALL = method_27043("all");
   public static final TextureKey TEXTURE;
   public static final TextureKey PARTICLE;
   public static final TextureKey END;
   public static final TextureKey BOTTOM;
   public static final TextureKey TOP;
   public static final TextureKey FRONT;
   public static final TextureKey BACK;
   public static final TextureKey SIDE;
   public static final TextureKey NORTH;
   public static final TextureKey SOUTH;
   public static final TextureKey EAST;
   public static final TextureKey WEST;
   public static final TextureKey UP;
   public static final TextureKey DOWN;
   public static final TextureKey CROSS;
   public static final TextureKey PLANT;
   public static final TextureKey WALL;
   public static final TextureKey RAIL;
   public static final TextureKey WOOL;
   public static final TextureKey PATTERN;
   public static final TextureKey PANE;
   public static final TextureKey EDGE;
   public static final TextureKey FAN;
   public static final TextureKey STEM;
   public static final TextureKey UPPERSTEM;
   public static final TextureKey CROP;
   public static final TextureKey DIRT;
   public static final TextureKey FIRE;
   public static final TextureKey LANTERN;
   public static final TextureKey PLATFORM;
   public static final TextureKey UNSTICKY;
   public static final TextureKey TORCH;
   public static final TextureKey LAYER0;
   public static final TextureKey LIT_LOG;
   private final String name;
   @Nullable
   private final TextureKey parent;

   private static TextureKey method_27043(String string) {
      return new TextureKey(string, (TextureKey)null);
   }

   private static TextureKey method_27044(String string, TextureKey textureKey) {
      return new TextureKey(string, textureKey);
   }

   private TextureKey(String string, @Nullable TextureKey textureKey) {
      this.name = string;
      this.parent = textureKey;
   }

   public String getName() {
      return this.name;
   }

   @Nullable
   public TextureKey getParent() {
      return this.parent;
   }

   public String toString() {
      return "#" + this.name;
   }

   static {
      TEXTURE = method_27044("texture", ALL);
      PARTICLE = method_27044("particle", TEXTURE);
      END = method_27044("end", ALL);
      BOTTOM = method_27044("bottom", END);
      TOP = method_27044("top", END);
      FRONT = method_27044("front", ALL);
      BACK = method_27044("back", ALL);
      SIDE = method_27044("side", ALL);
      NORTH = method_27044("north", SIDE);
      SOUTH = method_27044("south", SIDE);
      EAST = method_27044("east", SIDE);
      WEST = method_27044("west", SIDE);
      UP = method_27043("up");
      DOWN = method_27043("down");
      CROSS = method_27043("cross");
      PLANT = method_27043("plant");
      WALL = method_27044("wall", ALL);
      RAIL = method_27043("rail");
      WOOL = method_27043("wool");
      PATTERN = method_27043("pattern");
      PANE = method_27043("pane");
      EDGE = method_27043("edge");
      FAN = method_27043("fan");
      STEM = method_27043("stem");
      UPPERSTEM = method_27043("upperstem");
      CROP = method_27043("crop");
      DIRT = method_27043("dirt");
      FIRE = method_27043("fire");
      LANTERN = method_27043("lantern");
      PLATFORM = method_27043("platform");
      UNSTICKY = method_27043("unsticky");
      TORCH = method_27043("torch");
      LAYER0 = method_27043("layer0");
      LIT_LOG = method_27043("lit_log");
   }
}
