package net.minecraft.client.render.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ItemModels {
   public final Int2ObjectMap<ModelIdentifier> modelIds = new Int2ObjectOpenHashMap(256);
   private final Int2ObjectMap<BakedModel> models = new Int2ObjectOpenHashMap(256);
   private final BakedModelManager modelManager;

   public ItemModels(BakedModelManager modelManager) {
      this.modelManager = modelManager;
   }

   public Sprite getSprite(ItemConvertible itemConvertible) {
      return this.getSprite(new ItemStack(itemConvertible));
   }

   public Sprite getSprite(ItemStack stack) {
      BakedModel bakedModel = this.getModel(stack);
      return bakedModel == this.modelManager.getMissingModel() && stack.getItem() instanceof BlockItem ? this.modelManager.getBlockModels().getSprite(((BlockItem)stack.getItem()).getBlock().getDefaultState()) : bakedModel.getSprite();
   }

   public BakedModel getModel(ItemStack stack) {
      BakedModel bakedModel = this.getModel(stack.getItem());
      return bakedModel == null ? this.modelManager.getMissingModel() : bakedModel;
   }

   @Nullable
   public BakedModel getModel(Item item) {
      return (BakedModel)this.models.get(getModelId(item));
   }

   private static int getModelId(Item item) {
      return Item.getRawId(item);
   }

   public void putModel(Item item, ModelIdentifier modelId) {
      this.modelIds.put(getModelId(item), modelId);
   }

   public BakedModelManager getModelManager() {
      return this.modelManager;
   }

   public void reloadModels() {
      this.models.clear();
      ObjectIterator var1 = this.modelIds.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<Integer, ModelIdentifier> entry = (Entry)var1.next();
         this.models.put((Integer)((Integer)entry.getKey()), this.modelManager.getModel((ModelIdentifier)entry.getValue()));
      }

   }
}
