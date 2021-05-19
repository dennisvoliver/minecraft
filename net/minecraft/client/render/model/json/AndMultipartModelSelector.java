package net.minecraft.client.render.model.json;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;

@Environment(EnvType.CLIENT)
public class AndMultipartModelSelector implements MultipartModelSelector {
   private final Iterable<? extends MultipartModelSelector> selectors;

   public AndMultipartModelSelector(Iterable<? extends MultipartModelSelector> selectors) {
      this.selectors = selectors;
   }

   public Predicate<BlockState> getPredicate(StateManager<Block, BlockState> stateManager) {
      List<Predicate<BlockState>> list = (List)Streams.stream(this.selectors).map((multipartModelSelector) -> {
         return multipartModelSelector.getPredicate(stateManager);
      }).collect(Collectors.toList());
      return (blockState) -> {
         return list.stream().allMatch((predicate) -> {
            return predicate.test(blockState);
         });
      };
   }
}
