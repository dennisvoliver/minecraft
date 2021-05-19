package net.minecraft.fluid;

import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import net.minecraft.util.registry.Registry;

public class Fluids {
   public static final Fluid EMPTY = register("empty", new EmptyFluid());
   public static final FlowableFluid FLOWING_WATER = (FlowableFluid)register("flowing_water", new WaterFluid.Flowing());
   public static final FlowableFluid WATER = (FlowableFluid)register("water", new WaterFluid.Still());
   public static final FlowableFluid FLOWING_LAVA = (FlowableFluid)register("flowing_lava", new LavaFluid.Flowing());
   public static final FlowableFluid LAVA = (FlowableFluid)register("lava", new LavaFluid.Still());

   private static <T extends Fluid> T register(String id, T value) {
      return (Fluid)Registry.register(Registry.FLUID, (String)id, value);
   }

   static {
      Iterator var0 = Registry.FLUID.iterator();

      while(var0.hasNext()) {
         Fluid fluid = (Fluid)var0.next();
         UnmodifiableIterator var2 = fluid.getStateManager().getStates().iterator();

         while(var2.hasNext()) {
            FluidState fluidState = (FluidState)var2.next();
            Fluid.STATE_IDS.add(fluidState);
         }
      }

   }
}
