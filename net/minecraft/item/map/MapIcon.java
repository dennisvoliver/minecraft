package net.minecraft.item.map;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class MapIcon {
   private final MapIcon.Type type;
   private byte x;
   private byte z;
   private byte rotation;
   private final Text text;

   public MapIcon(MapIcon.Type type, byte x, byte z, byte rotation, @Nullable Text text) {
      this.type = type;
      this.x = x;
      this.z = z;
      this.rotation = rotation;
      this.text = text;
   }

   @Environment(EnvType.CLIENT)
   public byte getTypeId() {
      return this.type.getId();
   }

   public MapIcon.Type getType() {
      return this.type;
   }

   public byte getX() {
      return this.x;
   }

   public byte getZ() {
      return this.z;
   }

   public byte getRotation() {
      return this.rotation;
   }

   @Environment(EnvType.CLIENT)
   public boolean isAlwaysRendered() {
      return this.type.isAlwaysRendered();
   }

   @Nullable
   public Text getText() {
      return this.text;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof MapIcon)) {
         return false;
      } else {
         MapIcon mapIcon = (MapIcon)o;
         if (this.type != mapIcon.type) {
            return false;
         } else if (this.rotation != mapIcon.rotation) {
            return false;
         } else if (this.x != mapIcon.x) {
            return false;
         } else if (this.z != mapIcon.z) {
            return false;
         } else {
            return Objects.equals(this.text, mapIcon.text);
         }
      }
   }

   public int hashCode() {
      int i = this.type.getId();
      int i = 31 * i + this.x;
      i = 31 * i + this.z;
      i = 31 * i + this.rotation;
      i = 31 * i + Objects.hashCode(this.text);
      return i;
   }

   public static enum Type {
      PLAYER(false),
      FRAME(true),
      RED_MARKER(false),
      BLUE_MARKER(false),
      TARGET_X(true),
      TARGET_POINT(true),
      PLAYER_OFF_MAP(false),
      PLAYER_OFF_LIMITS(false),
      MANSION(true, 5393476),
      MONUMENT(true, 3830373),
      BANNER_WHITE(true),
      BANNER_ORANGE(true),
      BANNER_MAGENTA(true),
      BANNER_LIGHT_BLUE(true),
      BANNER_YELLOW(true),
      BANNER_LIME(true),
      BANNER_PINK(true),
      BANNER_GRAY(true),
      BANNER_LIGHT_GRAY(true),
      BANNER_CYAN(true),
      BANNER_PURPLE(true),
      BANNER_BLUE(true),
      BANNER_BROWN(true),
      BANNER_GREEN(true),
      BANNER_RED(true),
      BANNER_BLACK(true),
      RED_X(true);

      private final byte id;
      private final boolean alwaysRender;
      private final int tintColor;

      private Type(boolean renderNotHeld) {
         this(renderNotHeld, -1);
      }

      private Type(boolean alwaysRender, int tintColor) {
         this.id = (byte)this.ordinal();
         this.alwaysRender = alwaysRender;
         this.tintColor = tintColor;
      }

      public byte getId() {
         return this.id;
      }

      @Environment(EnvType.CLIENT)
      public boolean isAlwaysRendered() {
         return this.alwaysRender;
      }

      public boolean hasTintColor() {
         return this.tintColor >= 0;
      }

      public int getTintColor() {
         return this.tintColor;
      }

      public static MapIcon.Type byId(byte id) {
         return values()[MathHelper.clamp(id, 0, values().length - 1)];
      }
   }
}
