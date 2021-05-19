package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum ComparatorMode implements StringIdentifiable {
   COMPARE("compare"),
   SUBTRACT("subtract");

   private final String name;

   private ComparatorMode(String name) {
      this.name = name;
   }

   public String toString() {
      return this.name;
   }

   public String asString() {
      return this.name;
   }
}
