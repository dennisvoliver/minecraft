package net.minecraft.client.search;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloadListener;

@Environment(EnvType.CLIENT)
public class SearchManager implements SynchronousResourceReloadListener {
   public static final SearchManager.Key<ItemStack> ITEM_TOOLTIP = new SearchManager.Key();
   public static final SearchManager.Key<ItemStack> ITEM_TAG = new SearchManager.Key();
   public static final SearchManager.Key<RecipeResultCollection> RECIPE_OUTPUT = new SearchManager.Key();
   private final Map<SearchManager.Key<?>, SearchableContainer<?>> instances = Maps.newHashMap();

   public void apply(ResourceManager manager) {
      Iterator var2 = this.instances.values().iterator();

      while(var2.hasNext()) {
         SearchableContainer<?> searchableContainer = (SearchableContainer)var2.next();
         searchableContainer.reload();
      }

   }

   public <T> void put(SearchManager.Key<T> key, SearchableContainer<T> value) {
      this.instances.put(key, value);
   }

   public <T> SearchableContainer<T> get(SearchManager.Key<T> key) {
      return (SearchableContainer)this.instances.get(key);
   }

   @Environment(EnvType.CLIENT)
   public static class Key<T> {
   }
}
