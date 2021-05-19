package net.minecraft.resource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.tag.TagManager;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.Unit;

public class ServerResourceManager implements AutoCloseable {
   private static final CompletableFuture<Unit> field_25334;
   private final ReloadableResourceManager resourceManager;
   private final CommandManager commandManager;
   private final RecipeManager recipeManager;
   private final TagManagerLoader registryTagManager;
   private final LootConditionManager lootConditionManager;
   private final LootManager lootManager;
   private final ServerAdvancementLoader serverAdvancementLoader;
   private final FunctionLoader functionLoader;

   public ServerResourceManager(CommandManager.RegistrationEnvironment registrationEnvironment, int i) {
      this.resourceManager = new ReloadableResourceManagerImpl(ResourceType.SERVER_DATA);
      this.recipeManager = new RecipeManager();
      this.registryTagManager = new TagManagerLoader();
      this.lootConditionManager = new LootConditionManager();
      this.lootManager = new LootManager(this.lootConditionManager);
      this.serverAdvancementLoader = new ServerAdvancementLoader(this.lootConditionManager);
      this.commandManager = new CommandManager(registrationEnvironment);
      this.functionLoader = new FunctionLoader(i, this.commandManager.getDispatcher());
      this.resourceManager.registerListener(this.registryTagManager);
      this.resourceManager.registerListener(this.lootConditionManager);
      this.resourceManager.registerListener(this.recipeManager);
      this.resourceManager.registerListener(this.lootManager);
      this.resourceManager.registerListener(this.functionLoader);
      this.resourceManager.registerListener(this.serverAdvancementLoader);
   }

   public FunctionLoader getFunctionLoader() {
      return this.functionLoader;
   }

   public LootConditionManager getLootConditionManager() {
      return this.lootConditionManager;
   }

   public LootManager getLootManager() {
      return this.lootManager;
   }

   public TagManager getRegistryTagManager() {
      return this.registryTagManager.getTagManager();
   }

   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   public ServerAdvancementLoader getServerAdvancementLoader() {
      return this.serverAdvancementLoader;
   }

   public ResourceManager getResourceManager() {
      return this.resourceManager;
   }

   public static CompletableFuture<ServerResourceManager> reload(List<ResourcePack> list, CommandManager.RegistrationEnvironment registrationEnvironment, int i, Executor executor, Executor executor2) {
      ServerResourceManager serverResourceManager = new ServerResourceManager(registrationEnvironment, i);
      CompletableFuture<Unit> completableFuture = serverResourceManager.resourceManager.beginReload(executor, executor2, list, field_25334);
      return completableFuture.whenComplete((unit, throwable) -> {
         if (throwable != null) {
            serverResourceManager.close();
         }

      }).thenApply((unit) -> {
         return serverResourceManager;
      });
   }

   public void loadRegistryTags() {
      this.registryTagManager.getTagManager().apply();
   }

   public void close() {
      this.resourceManager.close();
   }

   static {
      field_25334 = CompletableFuture.completedFuture(Unit.INSTANCE);
   }
}
