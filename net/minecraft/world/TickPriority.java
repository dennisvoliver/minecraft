package net.minecraft.world;

public enum TickPriority {
   EXTREMELY_HIGH(-3),
   VERY_HIGH(-2),
   HIGH(-1),
   NORMAL(0),
   LOW(1),
   VERY_LOW(2),
   EXTREMELY_LOW(3);

   private final int index;

   private TickPriority(int index) {
      this.index = index;
   }

   public static TickPriority byIndex(int index) {
      TickPriority[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         TickPriority tickPriority = var1[var3];
         if (tickPriority.index == index) {
            return tickPriority;
         }
      }

      if (index < EXTREMELY_HIGH.index) {
         return EXTREMELY_HIGH;
      } else {
         return EXTREMELY_LOW;
      }
   }

   public int getIndex() {
      return this.index;
   }
}
