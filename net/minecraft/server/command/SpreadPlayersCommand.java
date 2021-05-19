package net.minecraft.server.command;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.BlockView;

public class SpreadPlayersCommand {
   private static final Dynamic4CommandExceptionType FAILED_TEAMS_EXCEPTION = new Dynamic4CommandExceptionType((object, object2, object3, object4) -> {
      return new TranslatableText("commands.spreadplayers.failed.teams", new Object[]{object, object2, object3, object4});
   });
   private static final Dynamic4CommandExceptionType FAILED_ENTITIES_EXCEPTION = new Dynamic4CommandExceptionType((object, object2, object3, object4) -> {
      return new TranslatableText("commands.spreadplayers.failed.entities", new Object[]{object, object2, object3, object4});
   });

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spreadplayers").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.argument("center", Vec2ArgumentType.vec2()).then(CommandManager.argument("spreadDistance", FloatArgumentType.floatArg(0.0F)).then(((RequiredArgumentBuilder)CommandManager.argument("maxRange", FloatArgumentType.floatArg(1.0F)).then(CommandManager.argument("respectTeams", BoolArgumentType.bool()).then(CommandManager.argument("targets", EntityArgumentType.entities()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), Vec2ArgumentType.getVec2(commandContext, "center"), FloatArgumentType.getFloat(commandContext, "spreadDistance"), FloatArgumentType.getFloat(commandContext, "maxRange"), 256, BoolArgumentType.getBool(commandContext, "respectTeams"), EntityArgumentType.getEntities(commandContext, "targets"));
      })))).then(CommandManager.literal("under").then(CommandManager.argument("maxHeight", IntegerArgumentType.integer(0)).then(CommandManager.argument("respectTeams", BoolArgumentType.bool()).then(CommandManager.argument("targets", EntityArgumentType.entities()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), Vec2ArgumentType.getVec2(commandContext, "center"), FloatArgumentType.getFloat(commandContext, "spreadDistance"), FloatArgumentType.getFloat(commandContext, "maxRange"), IntegerArgumentType.getInteger(commandContext, "maxHeight"), BoolArgumentType.getBool(commandContext, "respectTeams"), EntityArgumentType.getEntities(commandContext, "targets"));
      })))))))));
   }

   private static int execute(ServerCommandSource source, Vec2f center, float spreadDistance, float maxRange, int i, boolean bl, Collection<? extends Entity> collection) throws CommandSyntaxException {
      Random random = new Random();
      double d = (double)(center.x - maxRange);
      double e = (double)(center.y - maxRange);
      double f = (double)(center.x + maxRange);
      double g = (double)(center.y + maxRange);
      SpreadPlayersCommand.Pile[] piles = makePiles(random, bl ? getPileCountRespectingTeams(collection) : collection.size(), d, e, f, g);
      spread(center, (double)spreadDistance, source.getWorld(), random, d, e, f, g, i, piles, bl);
      double h = getMinDistance(collection, source.getWorld(), piles, i, bl);
      source.sendFeedback(new TranslatableText("commands.spreadplayers.success." + (bl ? "teams" : "entities"), new Object[]{piles.length, center.x, center.y, String.format(Locale.ROOT, "%.2f", h)}), true);
      return piles.length;
   }

   private static int getPileCountRespectingTeams(Collection<? extends Entity> entities) {
      Set<AbstractTeam> set = Sets.newHashSet();
      Iterator var2 = entities.iterator();

      while(var2.hasNext()) {
         Entity entity = (Entity)var2.next();
         if (entity instanceof PlayerEntity) {
            set.add(entity.getScoreboardTeam());
         } else {
            set.add((Object)null);
         }
      }

      return set.size();
   }

   private static void spread(Vec2f center, double spreadDistance, ServerWorld world, Random random, double minX, double minZ, double maxX, double maxZ, int i, SpreadPlayersCommand.Pile[] piles, boolean bl) throws CommandSyntaxException {
      boolean bl2 = true;
      double d = 3.4028234663852886E38D;

      int j;
      for(j = 0; j < 10000 && bl2; ++j) {
         bl2 = false;
         d = 3.4028234663852886E38D;

         int l;
         SpreadPlayersCommand.Pile pile4;
         for(int k = 0; k < piles.length; ++k) {
            SpreadPlayersCommand.Pile pile = piles[k];
            l = 0;
            pile4 = new SpreadPlayersCommand.Pile();

            for(int m = 0; m < piles.length; ++m) {
               if (k != m) {
                  SpreadPlayersCommand.Pile pile3 = piles[m];
                  double e = pile.getDistance(pile3);
                  d = Math.min(e, d);
                  if (e < spreadDistance) {
                     ++l;
                     pile4.x = pile4.x + (pile3.x - pile.x);
                     pile4.z = pile4.z + (pile3.z - pile.z);
                  }
               }
            }

            if (l > 0) {
               pile4.x = pile4.x / (double)l;
               pile4.z = pile4.z / (double)l;
               double f = (double)pile4.absolute();
               if (f > 0.0D) {
                  pile4.normalize();
                  pile.subtract(pile4);
               } else {
                  pile.setPileLocation(random, minX, minZ, maxX, maxZ);
               }

               bl2 = true;
            }

            if (pile.clamp(minX, minZ, maxX, maxZ)) {
               bl2 = true;
            }
         }

         if (!bl2) {
            SpreadPlayersCommand.Pile[] var28 = piles;
            int var29 = piles.length;

            for(l = 0; l < var29; ++l) {
               pile4 = var28[l];
               if (!pile4.isSafe(world, i)) {
                  pile4.setPileLocation(random, minX, minZ, maxX, maxZ);
                  bl2 = true;
               }
            }
         }
      }

      if (d == 3.4028234663852886E38D) {
         d = 0.0D;
      }

      if (j >= 10000) {
         if (bl) {
            throw FAILED_TEAMS_EXCEPTION.create(piles.length, center.x, center.y, String.format(Locale.ROOT, "%.2f", d));
         } else {
            throw FAILED_ENTITIES_EXCEPTION.create(piles.length, center.x, center.y, String.format(Locale.ROOT, "%.2f", d));
         }
      }
   }

   private static double getMinDistance(Collection<? extends Entity> entities, ServerWorld world, SpreadPlayersCommand.Pile[] piles, int i, boolean bl) {
      double d = 0.0D;
      int j = 0;
      Map<AbstractTeam, SpreadPlayersCommand.Pile> map = Maps.newHashMap();

      double e;
      for(Iterator var9 = entities.iterator(); var9.hasNext(); d += e) {
         Entity entity = (Entity)var9.next();
         SpreadPlayersCommand.Pile pile2;
         if (bl) {
            AbstractTeam abstractTeam = entity instanceof PlayerEntity ? entity.getScoreboardTeam() : null;
            if (!map.containsKey(abstractTeam)) {
               map.put(abstractTeam, piles[j++]);
            }

            pile2 = (SpreadPlayersCommand.Pile)map.get(abstractTeam);
         } else {
            pile2 = piles[j++];
         }

         entity.teleport((double)MathHelper.floor(pile2.x) + 0.5D, (double)pile2.getY(world, i), (double)MathHelper.floor(pile2.z) + 0.5D);
         e = Double.MAX_VALUE;
         SpreadPlayersCommand.Pile[] var14 = piles;
         int var15 = piles.length;

         for(int var16 = 0; var16 < var15; ++var16) {
            SpreadPlayersCommand.Pile pile3 = var14[var16];
            if (pile2 != pile3) {
               double f = pile2.getDistance(pile3);
               e = Math.min(f, e);
            }
         }
      }

      if (entities.size() < 2) {
         return 0.0D;
      } else {
         d /= (double)entities.size();
         return d;
      }
   }

   private static SpreadPlayersCommand.Pile[] makePiles(Random random, int count, double minX, double minZ, double maxX, double maxZ) {
      SpreadPlayersCommand.Pile[] piles = new SpreadPlayersCommand.Pile[count];

      for(int i = 0; i < piles.length; ++i) {
         SpreadPlayersCommand.Pile pile = new SpreadPlayersCommand.Pile();
         pile.setPileLocation(random, minX, minZ, maxX, maxZ);
         piles[i] = pile;
      }

      return piles;
   }

   static class Pile {
      private double x;
      private double z;

      double getDistance(SpreadPlayersCommand.Pile other) {
         double d = this.x - other.x;
         double e = this.z - other.z;
         return Math.sqrt(d * d + e * e);
      }

      void normalize() {
         double d = (double)this.absolute();
         this.x /= d;
         this.z /= d;
      }

      float absolute() {
         return MathHelper.sqrt(this.x * this.x + this.z * this.z);
      }

      public void subtract(SpreadPlayersCommand.Pile other) {
         this.x -= other.x;
         this.z -= other.z;
      }

      public boolean clamp(double minX, double minZ, double maxX, double maxZ) {
         boolean bl = false;
         if (this.x < minX) {
            this.x = minX;
            bl = true;
         } else if (this.x > maxX) {
            this.x = maxX;
            bl = true;
         }

         if (this.z < minZ) {
            this.z = minZ;
            bl = true;
         } else if (this.z > maxZ) {
            this.z = maxZ;
            bl = true;
         }

         return bl;
      }

      public int getY(BlockView blockView, int i) {
         BlockPos.Mutable mutable = new BlockPos.Mutable(this.x, (double)(i + 1), this.z);
         boolean bl = blockView.getBlockState(mutable).isAir();
         mutable.move(Direction.DOWN);

         boolean bl3;
         for(boolean bl2 = blockView.getBlockState(mutable).isAir(); mutable.getY() > 0; bl2 = bl3) {
            mutable.move(Direction.DOWN);
            bl3 = blockView.getBlockState(mutable).isAir();
            if (!bl3 && bl2 && bl) {
               return mutable.getY() + 1;
            }

            bl = bl2;
         }

         return i + 1;
      }

      public boolean isSafe(BlockView world, int i) {
         BlockPos blockPos = new BlockPos(this.x, (double)(this.getY(world, i) - 1), this.z);
         BlockState blockState = world.getBlockState(blockPos);
         Material material = blockState.getMaterial();
         return blockPos.getY() < i && !material.isLiquid() && material != Material.FIRE;
      }

      public void setPileLocation(Random random, double minX, double minZ, double maxX, double maxZ) {
         this.x = MathHelper.nextDouble(random, minX, maxX);
         this.z = MathHelper.nextDouble(random, minZ, maxZ);
      }
   }
}
