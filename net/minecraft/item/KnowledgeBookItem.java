package net.minecraft.item;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnowledgeBookItem extends Item {
   private static final Logger LOGGER = LogManager.getLogger();

   public KnowledgeBookItem(Item.Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      CompoundTag compoundTag = itemStack.getTag();
      if (!user.abilities.creativeMode) {
         user.setStackInHand(hand, ItemStack.EMPTY);
      }

      if (compoundTag != null && compoundTag.contains("Recipes", 9)) {
         if (!world.isClient) {
            ListTag listTag = compoundTag.getList("Recipes", 8);
            List<Recipe<?>> list = Lists.newArrayList();
            RecipeManager recipeManager = world.getServer().getRecipeManager();

            for(int i = 0; i < listTag.size(); ++i) {
               String string = listTag.getString(i);
               Optional<? extends Recipe<?>> optional = recipeManager.get(new Identifier(string));
               if (!optional.isPresent()) {
                  LOGGER.error((String)"Invalid recipe: {}", (Object)string);
                  return TypedActionResult.fail(itemStack);
               }

               list.add(optional.get());
            }

            user.unlockRecipes((Collection)list);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
         }

         return TypedActionResult.success(itemStack, world.isClient());
      } else {
         LOGGER.error((String)"Tag not valid: {}", (Object)compoundTag);
         return TypedActionResult.fail(itemStack);
      }
   }
}
