package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum DoorHinge implements StringIdentifiable {
   LEFT,
   RIGHT;

   public String toString() {
      return this.asString();
   }

   public String asString() {
      return this == LEFT ? "left" : "right";
   }
}
