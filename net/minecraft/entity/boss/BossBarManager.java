package net.minecraft.entity.boss;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BossBarManager {
   private final Map<Identifier, CommandBossBar> commandBossBars = Maps.newHashMap();

   @Nullable
   public CommandBossBar get(Identifier id) {
      return (CommandBossBar)this.commandBossBars.get(id);
   }

   public CommandBossBar add(Identifier id, Text displayName) {
      CommandBossBar commandBossBar = new CommandBossBar(id, displayName);
      this.commandBossBars.put(id, commandBossBar);
      return commandBossBar;
   }

   public void remove(CommandBossBar bossBar) {
      this.commandBossBars.remove(bossBar.getId());
   }

   public Collection<Identifier> getIds() {
      return this.commandBossBars.keySet();
   }

   public Collection<CommandBossBar> getAll() {
      return this.commandBossBars.values();
   }

   public CompoundTag toTag() {
      CompoundTag compoundTag = new CompoundTag();
      Iterator var2 = this.commandBossBars.values().iterator();

      while(var2.hasNext()) {
         CommandBossBar commandBossBar = (CommandBossBar)var2.next();
         compoundTag.put(commandBossBar.getId().toString(), commandBossBar.toTag());
      }

      return compoundTag;
   }

   public void fromTag(CompoundTag tag) {
      Iterator var2 = tag.getKeys().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         Identifier identifier = new Identifier(string);
         this.commandBossBars.put(identifier, CommandBossBar.fromTag(tag.getCompound(string), identifier));
      }

   }

   public void onPlayerConnect(ServerPlayerEntity player) {
      Iterator var2 = this.commandBossBars.values().iterator();

      while(var2.hasNext()) {
         CommandBossBar commandBossBar = (CommandBossBar)var2.next();
         commandBossBar.onPlayerConnect(player);
      }

   }

   public void onPlayerDisconnect(ServerPlayerEntity player) {
      Iterator var2 = this.commandBossBars.values().iterator();

      while(var2.hasNext()) {
         CommandBossBar commandBossBar = (CommandBossBar)var2.next();
         commandBossBar.onPlayerDisconnect(player);
      }

   }
}
