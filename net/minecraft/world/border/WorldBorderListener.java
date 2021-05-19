package net.minecraft.world.border;

public interface WorldBorderListener {
   void onSizeChange(WorldBorder border, double size);

   void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time);

   void onCenterChanged(WorldBorder border, double centerX, double centerZ);

   void onWarningTimeChanged(WorldBorder border, int warningTime);

   void onWarningBlocksChanged(WorldBorder border, int warningBlockDistance);

   void onDamagePerBlockChanged(WorldBorder border, double damagePerBlock);

   void onSafeZoneChanged(WorldBorder border, double safeZoneRadius);

   public static class WorldBorderSyncer implements WorldBorderListener {
      private final WorldBorder border;

      public WorldBorderSyncer(WorldBorder border) {
         this.border = border;
      }

      public void onSizeChange(WorldBorder border, double size) {
         this.border.setSize(size);
      }

      public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
         this.border.interpolateSize(fromSize, toSize, time);
      }

      public void onCenterChanged(WorldBorder border, double centerX, double centerZ) {
         this.border.setCenter(centerX, centerZ);
      }

      public void onWarningTimeChanged(WorldBorder border, int warningTime) {
         this.border.setWarningTime(warningTime);
      }

      public void onWarningBlocksChanged(WorldBorder border, int warningBlockDistance) {
         this.border.setWarningBlocks(warningBlockDistance);
      }

      public void onDamagePerBlockChanged(WorldBorder border, double damagePerBlock) {
         this.border.setDamagePerBlock(damagePerBlock);
      }

      public void onSafeZoneChanged(WorldBorder border, double safeZoneRadius) {
         this.border.setBuffer(safeZoneRadius);
      }
   }
}
