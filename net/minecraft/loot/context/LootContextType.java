package net.minecraft.loot.context;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.minecraft.loot.LootTableReporter;

public class LootContextType {
   private final Set<LootContextParameter<?>> required;
   private final Set<LootContextParameter<?>> allowed;

   private LootContextType(Set<LootContextParameter<?>> required, Set<LootContextParameter<?>> allowed) {
      this.required = ImmutableSet.copyOf((Collection)required);
      this.allowed = ImmutableSet.copyOf((Collection)Sets.union(required, allowed));
   }

   public Set<LootContextParameter<?>> getRequired() {
      return this.required;
   }

   public Set<LootContextParameter<?>> getAllowed() {
      return this.allowed;
   }

   public String toString() {
      return "[" + Joiner.on(", ").join(this.allowed.stream().map((parameter) -> {
         return (this.required.contains(parameter) ? "!" : "") + parameter.getIdentifier();
      }).iterator()) + "]";
   }

   public void validate(LootTableReporter reporter, LootContextAware parameterConsumer) {
      Set<LootContextParameter<?>> set = parameterConsumer.getRequiredParameters();
      Set<LootContextParameter<?>> set2 = Sets.difference(set, this.allowed);
      if (!set2.isEmpty()) {
         reporter.report("Parameters " + set2 + " are not provided in this context");
      }

   }

   public static class Builder {
      private final Set<LootContextParameter<?>> required = Sets.newIdentityHashSet();
      private final Set<LootContextParameter<?>> allowed = Sets.newIdentityHashSet();

      public LootContextType.Builder require(LootContextParameter<?> parameter) {
         if (this.allowed.contains(parameter)) {
            throw new IllegalArgumentException("Parameter " + parameter.getIdentifier() + " is already optional");
         } else {
            this.required.add(parameter);
            return this;
         }
      }

      public LootContextType.Builder allow(LootContextParameter<?> parameter) {
         if (this.required.contains(parameter)) {
            throw new IllegalArgumentException("Parameter " + parameter.getIdentifier() + " is already required");
         } else {
            this.allowed.add(parameter);
            return this;
         }
      }

      public LootContextType build() {
         return new LootContextType(this.required, this.allowed);
      }
   }
}
