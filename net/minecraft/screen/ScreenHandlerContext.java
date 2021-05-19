package net.minecraft.screen;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Screen handler contexts allow screen handlers to interact with the
 * logical server's world safely.
 */
public interface ScreenHandlerContext {
   /**
    * The dummy screen handler context for clientside screen handlers.
    */
   ScreenHandlerContext EMPTY = new ScreenHandlerContext() {
      public <T> Optional<T> run(BiFunction<World, BlockPos, T> function) {
         return Optional.empty();
      }
   };

   /**
    * Returns an active screen handler context. Used on the logical server.
    */
   static ScreenHandlerContext create(final World world, final BlockPos pos) {
      return new ScreenHandlerContext() {
         public <T> Optional<T> run(BiFunction<World, BlockPos, T> function) {
            return Optional.of(function.apply(world, pos));
         }
      };
   }

   <T> Optional<T> run(BiFunction<World, BlockPos, T> function);

   default <T> T run(BiFunction<World, BlockPos, T> function, T defaultValue) {
      return this.run(function).orElse(defaultValue);
   }

   default void run(BiConsumer<World, BlockPos> function) {
      this.run((world, blockPos) -> {
         function.accept(world, blockPos);
         return Optional.empty();
      });
   }
}
