package net.minecraft.data.report;

import com.google.common.collect.UnmodifiableIterator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public class BlockListProvider implements DataProvider {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final DataGenerator root;

   public BlockListProvider(DataGenerator dataGenerator) {
      this.root = dataGenerator;
   }

   public void run(DataCache cache) throws IOException {
      JsonObject jsonObject = new JsonObject();
      Iterator var3 = Registry.BLOCK.iterator();

      while(var3.hasNext()) {
         Block block = (Block)var3.next();
         Identifier identifier = Registry.BLOCK.getId(block);
         JsonObject jsonObject2 = new JsonObject();
         StateManager<Block, BlockState> stateManager = block.getStateManager();
         if (!stateManager.getProperties().isEmpty()) {
            JsonObject jsonObject3 = new JsonObject();
            Iterator var9 = stateManager.getProperties().iterator();

            while(true) {
               if (!var9.hasNext()) {
                  jsonObject2.add("properties", jsonObject3);
                  break;
               }

               Property<?> property = (Property)var9.next();
               JsonArray jsonArray = new JsonArray();
               Iterator var12 = property.getValues().iterator();

               while(var12.hasNext()) {
                  Comparable<?> comparable = (Comparable)var12.next();
                  jsonArray.add(Util.getValueAsString(property, comparable));
               }

               jsonObject3.add(property.getName(), jsonArray);
            }
         }

         JsonArray jsonArray2 = new JsonArray();

         JsonObject jsonObject4;
         for(UnmodifiableIterator var17 = stateManager.getStates().iterator(); var17.hasNext(); jsonArray2.add((JsonElement)jsonObject4)) {
            BlockState blockState = (BlockState)var17.next();
            jsonObject4 = new JsonObject();
            JsonObject jsonObject5 = new JsonObject();
            Iterator var21 = stateManager.getProperties().iterator();

            while(var21.hasNext()) {
               Property<?> property2 = (Property)var21.next();
               jsonObject5.addProperty(property2.getName(), Util.getValueAsString(property2, blockState.get(property2)));
            }

            if (jsonObject5.size() > 0) {
               jsonObject4.add("properties", jsonObject5);
            }

            jsonObject4.addProperty("id", (Number)Block.getRawIdFromState(blockState));
            if (blockState == block.getDefaultState()) {
               jsonObject4.addProperty("default", true);
            }
         }

         jsonObject2.add("states", jsonArray2);
         jsonObject.add(identifier.toString(), jsonObject2);
      }

      Path path = this.root.getOutput().resolve("reports/blocks.json");
      DataProvider.writeToPath(GSON, cache, jsonObject, path);
   }

   public String getName() {
      return "Block List";
   }
}
