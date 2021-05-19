package net.minecraft.data.server;

import java.nio.file.Path;
import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FluidTagsProvider extends AbstractTagProvider<Fluid> {
   public FluidTagsProvider(DataGenerator dataGenerator) {
      super(dataGenerator, Registry.FLUID);
   }

   protected void configure() {
      this.getOrCreateTagBuilder(FluidTags.WATER).add((Object[])(Fluids.WATER, Fluids.FLOWING_WATER));
      this.getOrCreateTagBuilder(FluidTags.LAVA).add((Object[])(Fluids.LAVA, Fluids.FLOWING_LAVA));
   }

   protected Path getOutput(Identifier identifier) {
      return this.root.getOutput().resolve("data/" + identifier.getNamespace() + "/tags/fluids/" + identifier.getPath() + ".json");
   }

   public String getName() {
      return "Fluid Tags";
   }
}
