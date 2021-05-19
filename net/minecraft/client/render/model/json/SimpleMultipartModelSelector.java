package net.minecraft.client.render.model.json;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

@Environment(EnvType.CLIENT)
public class SimpleMultipartModelSelector implements MultipartModelSelector {
   private static final Splitter VALUE_SPLITTER = Splitter.on('|').omitEmptyStrings();
   private final String key;
   private final String valueString;

   public SimpleMultipartModelSelector(String key, String valueString) {
      this.key = key;
      this.valueString = valueString;
   }

   public Predicate<BlockState> getPredicate(StateManager<Block, BlockState> stateManager) {
      Property<?> property = stateManager.getProperty(this.key);
      if (property == null) {
         throw new RuntimeException(String.format("Unknown property '%s' on '%s'", this.key, ((Block)stateManager.getOwner()).toString()));
      } else {
         String string = this.valueString;
         boolean bl = !string.isEmpty() && string.charAt(0) == '!';
         if (bl) {
            string = string.substring(1);
         }

         List<String> list = VALUE_SPLITTER.splitToList(string);
         if (list.isEmpty()) {
            throw new RuntimeException(String.format("Empty value '%s' for property '%s' on '%s'", this.valueString, this.key, ((Block)stateManager.getOwner()).toString()));
         } else {
            Predicate predicate2;
            if (list.size() == 1) {
               predicate2 = this.createPredicate(stateManager, property, string);
            } else {
               List<Predicate<BlockState>> list2 = (List)list.stream().map((stringx) -> {
                  return this.createPredicate(stateManager, property, stringx);
               }).collect(Collectors.toList());
               predicate2 = (blockState) -> {
                  return list2.stream().anyMatch((predicate) -> {
                     return predicate.test(blockState);
                  });
               };
            }

            return bl ? predicate2.negate() : predicate2;
         }
      }
   }

   private Predicate<BlockState> createPredicate(StateManager<Block, BlockState> stateFactory, Property<?> property, String valueString) {
      Optional<?> optional = property.parse(valueString);
      if (!optional.isPresent()) {
         throw new RuntimeException(String.format("Unknown value '%s' for property '%s' on '%s' in '%s'", valueString, this.key, ((Block)stateFactory.getOwner()).toString(), this.valueString));
      } else {
         return (blockState) -> {
            return blockState.get(property).equals(optional.get());
         };
      }
   }

   public String toString() {
      return MoreObjects.toStringHelper((Object)this).add("key", this.key).add("value", this.valueString).toString();
   }
}
