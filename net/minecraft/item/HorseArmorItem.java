package net.minecraft.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

public class HorseArmorItem extends Item {
   private final int bonus;
   private final String entityTexture;

   public HorseArmorItem(int bonus, String name, Item.Settings settings) {
      super(settings);
      this.bonus = bonus;
      this.entityTexture = "textures/entity/horse/armor/horse_armor_" + name + ".png";
   }

   @Environment(EnvType.CLIENT)
   public Identifier getEntityTexture() {
      return new Identifier(this.entityTexture);
   }

   public int getBonus() {
      return this.bonus;
   }
}
