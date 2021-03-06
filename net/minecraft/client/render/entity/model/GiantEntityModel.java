package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.mob.GiantEntity;

@Environment(EnvType.CLIENT)
public class GiantEntityModel extends AbstractZombieModel<GiantEntity> {
   public GiantEntityModel() {
      this(0.0F, false);
   }

   public GiantEntityModel(float scale, boolean bl) {
      super(scale, 0.0F, 64, bl ? 32 : 64);
   }

   public boolean isAttacking(GiantEntity giantEntity) {
      return false;
   }
}
