package net.minecraft.entity.effect;

public class InstantStatusEffect extends StatusEffect {
   public InstantStatusEffect(StatusEffectType statusEffectType, int i) {
      super(statusEffectType, i);
   }

   public boolean isInstant() {
      return true;
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return duration >= 1;
   }
}
