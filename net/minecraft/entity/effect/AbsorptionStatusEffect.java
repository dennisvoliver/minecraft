package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;

public class AbsorptionStatusEffect extends StatusEffect {
   protected AbsorptionStatusEffect(StatusEffectType statusEffectType, int i) {
      super(statusEffectType, i);
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (float)(4 * (amplifier + 1)));
      super.onRemoved(entity, attributes, amplifier);
   }

   public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      entity.setAbsorptionAmount(entity.getAbsorptionAmount() + (float)(4 * (amplifier + 1)));
      super.onApplied(entity, attributes, amplifier);
   }
}
