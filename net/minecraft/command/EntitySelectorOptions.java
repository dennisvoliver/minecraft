package net.minecraft.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NumberRange;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;

public class EntitySelectorOptions {
   private static final Map<String, EntitySelectorOptions.SelectorOption> options = Maps.newHashMap();
   public static final DynamicCommandExceptionType UNKNOWN_OPTION_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("argument.entity.options.unknown", new Object[]{object});
   });
   public static final DynamicCommandExceptionType INAPPLICABLE_OPTION_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("argument.entity.options.inapplicable", new Object[]{object});
   });
   public static final SimpleCommandExceptionType NEGATIVE_DISTANCE_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.entity.options.distance.negative"));
   public static final SimpleCommandExceptionType NEGATIVE_LEVEL_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.entity.options.level.negative"));
   public static final SimpleCommandExceptionType TOO_SMALL_LEVEL_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.entity.options.limit.toosmall"));
   public static final DynamicCommandExceptionType IRREVERSIBLE_SORT_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("argument.entity.options.sort.irreversible", new Object[]{object});
   });
   public static final DynamicCommandExceptionType INVALID_MODE_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("argument.entity.options.mode.invalid", new Object[]{object});
   });
   public static final DynamicCommandExceptionType INVALID_TYPE_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("argument.entity.options.type.invalid", new Object[]{object});
   });

   private static void putOption(String id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
      options.put(id, new EntitySelectorOptions.SelectorOption(handler, condition, description));
   }

   public static void register() {
      if (options.isEmpty()) {
         putOption("name", (entitySelectorReader) -> {
            int i = entitySelectorReader.getReader().getCursor();
            boolean bl = entitySelectorReader.readNegationCharacter();
            String string = entitySelectorReader.getReader().readString();
            if (entitySelectorReader.excludesName() && !bl) {
               entitySelectorReader.getReader().setCursor(i);
               throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(entitySelectorReader.getReader(), "name");
            } else {
               if (bl) {
                  entitySelectorReader.setExcludesName(true);
               } else {
                  entitySelectorReader.setSelectsName(true);
               }

               entitySelectorReader.setPredicate((entity) -> {
                  return entity.getName().getString().equals(string) != bl;
               });
            }
         }, (entitySelectorReader) -> {
            return !entitySelectorReader.selectsName();
         }, new TranslatableText("argument.entity.options.name.description"));
         putOption("distance", (entitySelectorReader) -> {
            int i = entitySelectorReader.getReader().getCursor();
            NumberRange.FloatRange floatRange = NumberRange.FloatRange.parse(entitySelectorReader.getReader());
            if ((floatRange.getMin() == null || !((Float)floatRange.getMin() < 0.0F)) && (floatRange.getMax() == null || !((Float)floatRange.getMax() < 0.0F))) {
               entitySelectorReader.setDistance(floatRange);
               entitySelectorReader.setLocalWorldOnly();
            } else {
               entitySelectorReader.getReader().setCursor(i);
               throw NEGATIVE_DISTANCE_EXCEPTION.createWithContext(entitySelectorReader.getReader());
            }
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getDistance().isDummy();
         }, new TranslatableText("argument.entity.options.distance.description"));
         putOption("level", (entitySelectorReader) -> {
            int i = entitySelectorReader.getReader().getCursor();
            NumberRange.IntRange intRange = NumberRange.IntRange.parse(entitySelectorReader.getReader());
            if ((intRange.getMin() == null || (Integer)intRange.getMin() >= 0) && (intRange.getMax() == null || (Integer)intRange.getMax() >= 0)) {
               entitySelectorReader.setLevelRange(intRange);
               entitySelectorReader.setIncludesNonPlayers(false);
            } else {
               entitySelectorReader.getReader().setCursor(i);
               throw NEGATIVE_LEVEL_EXCEPTION.createWithContext(entitySelectorReader.getReader());
            }
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getLevelRange().isDummy();
         }, new TranslatableText("argument.entity.options.level.description"));
         putOption("x", (entitySelectorReader) -> {
            entitySelectorReader.setLocalWorldOnly();
            entitySelectorReader.setX(entitySelectorReader.getReader().readDouble());
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getX() == null;
         }, new TranslatableText("argument.entity.options.x.description"));
         putOption("y", (entitySelectorReader) -> {
            entitySelectorReader.setLocalWorldOnly();
            entitySelectorReader.setY(entitySelectorReader.getReader().readDouble());
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getY() == null;
         }, new TranslatableText("argument.entity.options.y.description"));
         putOption("z", (entitySelectorReader) -> {
            entitySelectorReader.setLocalWorldOnly();
            entitySelectorReader.setZ(entitySelectorReader.getReader().readDouble());
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getZ() == null;
         }, new TranslatableText("argument.entity.options.z.description"));
         putOption("dx", (entitySelectorReader) -> {
            entitySelectorReader.setLocalWorldOnly();
            entitySelectorReader.setDx(entitySelectorReader.getReader().readDouble());
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getDx() == null;
         }, new TranslatableText("argument.entity.options.dx.description"));
         putOption("dy", (entitySelectorReader) -> {
            entitySelectorReader.setLocalWorldOnly();
            entitySelectorReader.setDy(entitySelectorReader.getReader().readDouble());
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getDy() == null;
         }, new TranslatableText("argument.entity.options.dy.description"));
         putOption("dz", (entitySelectorReader) -> {
            entitySelectorReader.setLocalWorldOnly();
            entitySelectorReader.setDz(entitySelectorReader.getReader().readDouble());
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getDz() == null;
         }, new TranslatableText("argument.entity.options.dz.description"));
         putOption("x_rotation", (entitySelectorReader) -> {
            entitySelectorReader.setPitchRange(FloatRangeArgument.parse(entitySelectorReader.getReader(), true, MathHelper::wrapDegrees));
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getPitchRange() == FloatRangeArgument.ANY;
         }, new TranslatableText("argument.entity.options.x_rotation.description"));
         putOption("y_rotation", (entitySelectorReader) -> {
            entitySelectorReader.setYawRange(FloatRangeArgument.parse(entitySelectorReader.getReader(), true, MathHelper::wrapDegrees));
         }, (entitySelectorReader) -> {
            return entitySelectorReader.getYawRange() == FloatRangeArgument.ANY;
         }, new TranslatableText("argument.entity.options.y_rotation.description"));
         putOption("limit", (entitySelectorReader) -> {
            int i = entitySelectorReader.getReader().getCursor();
            int j = entitySelectorReader.getReader().readInt();
            if (j < 1) {
               entitySelectorReader.getReader().setCursor(i);
               throw TOO_SMALL_LEVEL_EXCEPTION.createWithContext(entitySelectorReader.getReader());
            } else {
               entitySelectorReader.setLimit(j);
               entitySelectorReader.setHasLimit(true);
            }
         }, (entitySelectorReader) -> {
            return !entitySelectorReader.isSenderOnly() && !entitySelectorReader.hasLimit();
         }, new TranslatableText("argument.entity.options.limit.description"));
         putOption("sort", (entitySelectorReader) -> {
            int i = entitySelectorReader.getReader().getCursor();
            String string = entitySelectorReader.getReader().readUnquotedString();
            entitySelectorReader.setSuggestionProvider((suggestionsBuilder, consumer) -> {
               return CommandSource.suggestMatching((Iterable)Arrays.asList("nearest", "furthest", "random", "arbitrary"), suggestionsBuilder);
            });
            byte var5 = -1;
            switch(string.hashCode()) {
            case -938285885:
               if (string.equals("random")) {
                  var5 = 2;
               }
               break;
            case 1510793967:
               if (string.equals("furthest")) {
                  var5 = 1;
               }
               break;
            case 1780188658:
               if (string.equals("arbitrary")) {
                  var5 = 3;
               }
               break;
            case 1825779806:
               if (string.equals("nearest")) {
                  var5 = 0;
               }
            }

            BiConsumer biConsumer5;
            switch(var5) {
            case 0:
               biConsumer5 = EntitySelectorReader.NEAREST;
               break;
            case 1:
               biConsumer5 = EntitySelectorReader.FURTHEST;
               break;
            case 2:
               biConsumer5 = EntitySelectorReader.RANDOM;
               break;
            case 3:
               biConsumer5 = EntitySelectorReader.ARBITRARY;
               break;
            default:
               entitySelectorReader.getReader().setCursor(i);
               throw IRREVERSIBLE_SORT_EXCEPTION.createWithContext(entitySelectorReader.getReader(), string);
            }

            entitySelectorReader.setSorter(biConsumer5);
            entitySelectorReader.setHasSorter(true);
         }, (entitySelectorReader) -> {
            return !entitySelectorReader.isSenderOnly() && !entitySelectorReader.hasSorter();
         }, new TranslatableText("argument.entity.options.sort.description"));
         putOption("gamemode", (entitySelectorReader) -> {
            entitySelectorReader.setSuggestionProvider((suggestionsBuilder, consumer) -> {
               String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
               boolean bl = !entitySelectorReader.excludesGameMode();
               boolean bl2 = true;
               if (!string.isEmpty()) {
                  if (string.charAt(0) == '!') {
                     bl = false;
                     string = string.substring(1);
                  } else {
                     bl2 = false;
                  }
               }

               GameMode[] var6 = GameMode.values();
               int var7 = var6.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  GameMode gameMode = var6[var8];
                  if (gameMode != GameMode.NOT_SET && gameMode.getName().toLowerCase(Locale.ROOT).startsWith(string)) {
                     if (bl2) {
                        suggestionsBuilder.suggest('!' + gameMode.getName());
                     }

                     if (bl) {
                        suggestionsBuilder.suggest(gameMode.getName());
                     }
                  }
               }

               return suggestionsBuilder.buildFuture();
            });
            int i = entitySelectorReader.getReader().getCursor();
            boolean bl = entitySelectorReader.readNegationCharacter();
            if (entitySelectorReader.excludesGameMode() && !bl) {
               entitySelectorReader.getReader().setCursor(i);
               throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(entitySelectorReader.getReader(), "gamemode");
            } else {
               String string = entitySelectorReader.getReader().readUnquotedString();
               GameMode gameMode = GameMode.byName(string, GameMode.NOT_SET);
               if (gameMode == GameMode.NOT_SET) {
                  entitySelectorReader.getReader().setCursor(i);
                  throw INVALID_MODE_EXCEPTION.createWithContext(entitySelectorReader.getReader(), string);
               } else {
                  entitySelectorReader.setIncludesNonPlayers(false);
                  entitySelectorReader.setPredicate((entity) -> {
                     if (!(entity instanceof ServerPlayerEntity)) {
                        return false;
                     } else {
                        GameMode gameMode2 = ((ServerPlayerEntity)entity).interactionManager.getGameMode();
                        return bl ? gameMode2 != gameMode : gameMode2 == gameMode;
                     }
                  });
                  if (bl) {
                     entitySelectorReader.setHasNegatedGameMode(true);
                  } else {
                     entitySelectorReader.setSelectsGameMode(true);
                  }

               }
            }
         }, (entitySelectorReader) -> {
            return !entitySelectorReader.selectsGameMode();
         }, new TranslatableText("argument.entity.options.gamemode.description"));
         putOption("team", (entitySelectorReader) -> {
            boolean bl = entitySelectorReader.readNegationCharacter();
            String string = entitySelectorReader.getReader().readUnquotedString();
            entitySelectorReader.setPredicate((entity) -> {
               if (!(entity instanceof LivingEntity)) {
                  return false;
               } else {
                  AbstractTeam abstractTeam = entity.getScoreboardTeam();
                  String string2 = abstractTeam == null ? "" : abstractTeam.getName();
                  return string2.equals(string) != bl;
               }
            });
            if (bl) {
               entitySelectorReader.setExcludesTeam(true);
            } else {
               entitySelectorReader.setSelectsTeam(true);
            }

         }, (entitySelectorReader) -> {
            return !entitySelectorReader.selectsTeam();
         }, new TranslatableText("argument.entity.options.team.description"));
         putOption("type", (entitySelectorReader) -> {
            entitySelectorReader.setSuggestionProvider((suggestionsBuilder, consumer) -> {
               CommandSource.suggestIdentifiers(Registry.ENTITY_TYPE.getIds(), suggestionsBuilder, String.valueOf('!'));
               CommandSource.suggestIdentifiers(EntityTypeTags.getTagGroup().getTagIds(), suggestionsBuilder, "!#");
               if (!entitySelectorReader.excludesEntityType()) {
                  CommandSource.suggestIdentifiers((Iterable)Registry.ENTITY_TYPE.getIds(), suggestionsBuilder);
                  CommandSource.suggestIdentifiers(EntityTypeTags.getTagGroup().getTagIds(), suggestionsBuilder, String.valueOf('#'));
               }

               return suggestionsBuilder.buildFuture();
            });
            int i = entitySelectorReader.getReader().getCursor();
            boolean bl = entitySelectorReader.readNegationCharacter();
            if (entitySelectorReader.excludesEntityType() && !bl) {
               entitySelectorReader.getReader().setCursor(i);
               throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(entitySelectorReader.getReader(), "type");
            } else {
               if (bl) {
                  entitySelectorReader.setExcludesEntityType();
               }

               Identifier identifier;
               if (entitySelectorReader.readTagCharacter()) {
                  identifier = Identifier.fromCommandInput(entitySelectorReader.getReader());
                  entitySelectorReader.setPredicate((entity) -> {
                     return entity.getServer().getTagManager().getEntityTypes().getTagOrEmpty(identifier).contains(entity.getType()) != bl;
                  });
               } else {
                  identifier = Identifier.fromCommandInput(entitySelectorReader.getReader());
                  EntityType<?> entityType = (EntityType)Registry.ENTITY_TYPE.getOrEmpty(identifier).orElseThrow(() -> {
                     entitySelectorReader.getReader().setCursor(i);
                     return INVALID_TYPE_EXCEPTION.createWithContext(entitySelectorReader.getReader(), identifier.toString());
                  });
                  if (Objects.equals(EntityType.PLAYER, entityType) && !bl) {
                     entitySelectorReader.setIncludesNonPlayers(false);
                  }

                  entitySelectorReader.setPredicate((entity) -> {
                     return Objects.equals(entityType, entity.getType()) != bl;
                  });
                  if (!bl) {
                     entitySelectorReader.setEntityType(entityType);
                  }
               }

            }
         }, (entitySelectorReader) -> {
            return !entitySelectorReader.selectsEntityType();
         }, new TranslatableText("argument.entity.options.type.description"));
         putOption("tag", (entitySelectorReader) -> {
            boolean bl = entitySelectorReader.readNegationCharacter();
            String string = entitySelectorReader.getReader().readUnquotedString();
            entitySelectorReader.setPredicate((entity) -> {
               if ("".equals(string)) {
                  return entity.getScoreboardTags().isEmpty() != bl;
               } else {
                  return entity.getScoreboardTags().contains(string) != bl;
               }
            });
         }, (entitySelectorReader) -> {
            return true;
         }, new TranslatableText("argument.entity.options.tag.description"));
         putOption("nbt", (entitySelectorReader) -> {
            boolean bl = entitySelectorReader.readNegationCharacter();
            CompoundTag compoundTag = (new StringNbtReader(entitySelectorReader.getReader())).parseCompoundTag();
            entitySelectorReader.setPredicate((entity) -> {
               CompoundTag compoundTag2 = entity.toTag(new CompoundTag());
               if (entity instanceof ServerPlayerEntity) {
                  ItemStack itemStack = ((ServerPlayerEntity)entity).inventory.getMainHandStack();
                  if (!itemStack.isEmpty()) {
                     compoundTag2.put("SelectedItem", itemStack.toTag(new CompoundTag()));
                  }
               }

               return NbtHelper.matches(compoundTag, compoundTag2, true) != bl;
            });
         }, (entitySelectorReader) -> {
            return true;
         }, new TranslatableText("argument.entity.options.nbt.description"));
         putOption("scores", (entitySelectorReader) -> {
            StringReader stringReader = entitySelectorReader.getReader();
            Map<String, NumberRange.IntRange> map = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();

            while(stringReader.canRead() && stringReader.peek() != '}') {
               stringReader.skipWhitespace();
               String string = stringReader.readUnquotedString();
               stringReader.skipWhitespace();
               stringReader.expect('=');
               stringReader.skipWhitespace();
               NumberRange.IntRange intRange = NumberRange.IntRange.parse(stringReader);
               map.put(string, intRange);
               stringReader.skipWhitespace();
               if (stringReader.canRead() && stringReader.peek() == ',') {
                  stringReader.skip();
               }
            }

            stringReader.expect('}');
            if (!map.isEmpty()) {
               entitySelectorReader.setPredicate((entity) -> {
                  Scoreboard scoreboard = entity.getServer().getScoreboard();
                  String string = entity.getEntityName();
                  Iterator var4 = map.entrySet().iterator();

                  Entry entry;
                  int i;
                  do {
                     if (!var4.hasNext()) {
                        return true;
                     }

                     entry = (Entry)var4.next();
                     ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective((String)entry.getKey());
                     if (scoreboardObjective == null) {
                        return false;
                     }

                     if (!scoreboard.playerHasObjective(string, scoreboardObjective)) {
                        return false;
                     }

                     ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(string, scoreboardObjective);
                     i = scoreboardPlayerScore.getScore();
                  } while(((NumberRange.IntRange)entry.getValue()).test(i));

                  return false;
               });
            }

            entitySelectorReader.setSelectsScores(true);
         }, (entitySelectorReader) -> {
            return !entitySelectorReader.selectsScores();
         }, new TranslatableText("argument.entity.options.scores.description"));
         putOption("advancements", (entitySelectorReader) -> {
            StringReader stringReader = entitySelectorReader.getReader();
            Map<Identifier, Predicate<AdvancementProgress>> map = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();

            while(stringReader.canRead() && stringReader.peek() != '}') {
               stringReader.skipWhitespace();
               Identifier identifier = Identifier.fromCommandInput(stringReader);
               stringReader.skipWhitespace();
               stringReader.expect('=');
               stringReader.skipWhitespace();
               if (stringReader.canRead() && stringReader.peek() == '{') {
                  Map<String, Predicate<CriterionProgress>> map2 = Maps.newHashMap();
                  stringReader.skipWhitespace();
                  stringReader.expect('{');
                  stringReader.skipWhitespace();

                  while(stringReader.canRead() && stringReader.peek() != '}') {
                     stringReader.skipWhitespace();
                     String string = stringReader.readUnquotedString();
                     stringReader.skipWhitespace();
                     stringReader.expect('=');
                     stringReader.skipWhitespace();
                     boolean bl = stringReader.readBoolean();
                     map2.put(string, (criterionProgress) -> {
                        return criterionProgress.isObtained() == bl;
                     });
                     stringReader.skipWhitespace();
                     if (stringReader.canRead() && stringReader.peek() == ',') {
                        stringReader.skip();
                     }
                  }

                  stringReader.skipWhitespace();
                  stringReader.expect('}');
                  stringReader.skipWhitespace();
                  map.put(identifier, (advancementProgress) -> {
                     Iterator var2 = map2.entrySet().iterator();

                     Entry entry;
                     CriterionProgress criterionProgress;
                     do {
                        if (!var2.hasNext()) {
                           return true;
                        }

                        entry = (Entry)var2.next();
                        criterionProgress = advancementProgress.getCriterionProgress((String)entry.getKey());
                     } while(criterionProgress != null && ((Predicate)entry.getValue()).test(criterionProgress));

                     return false;
                  });
               } else {
                  boolean bl2 = stringReader.readBoolean();
                  map.put(identifier, (advancementProgress) -> {
                     return advancementProgress.isDone() == bl2;
                  });
               }

               stringReader.skipWhitespace();
               if (stringReader.canRead() && stringReader.peek() == ',') {
                  stringReader.skip();
               }
            }

            stringReader.expect('}');
            if (!map.isEmpty()) {
               entitySelectorReader.setPredicate((entity) -> {
                  if (!(entity instanceof ServerPlayerEntity)) {
                     return false;
                  } else {
                     ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                     PlayerAdvancementTracker playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();
                     ServerAdvancementLoader serverAdvancementLoader = serverPlayerEntity.getServer().getAdvancementLoader();
                     Iterator var5 = map.entrySet().iterator();

                     Entry entry;
                     Advancement advancement;
                     do {
                        if (!var5.hasNext()) {
                           return true;
                        }

                        entry = (Entry)var5.next();
                        advancement = serverAdvancementLoader.get((Identifier)entry.getKey());
                     } while(advancement != null && ((Predicate)entry.getValue()).test(playerAdvancementTracker.getProgress(advancement)));

                     return false;
                  }
               });
               entitySelectorReader.setIncludesNonPlayers(false);
            }

            entitySelectorReader.setSelectsAdvancements(true);
         }, (entitySelectorReader) -> {
            return !entitySelectorReader.selectsAdvancements();
         }, new TranslatableText("argument.entity.options.advancements.description"));
         putOption("predicate", (entitySelectorReader) -> {
            boolean bl = entitySelectorReader.readNegationCharacter();
            Identifier identifier = Identifier.fromCommandInput(entitySelectorReader.getReader());
            entitySelectorReader.setPredicate((entity) -> {
               if (!(entity.world instanceof ServerWorld)) {
                  return false;
               } else {
                  ServerWorld serverWorld = (ServerWorld)entity.world;
                  LootCondition lootCondition = serverWorld.getServer().getPredicateManager().get(identifier);
                  if (lootCondition == null) {
                     return false;
                  } else {
                     LootContext lootContext = (new LootContext.Builder(serverWorld)).parameter(LootContextParameters.THIS_ENTITY, entity).parameter(LootContextParameters.ORIGIN, entity.getPos()).build(LootContextTypes.SELECTOR);
                     return bl ^ lootCondition.test(lootContext);
                  }
               }
            });
         }, (entitySelectorReader) -> {
            return true;
         }, new TranslatableText("argument.entity.options.predicate.description"));
      }
   }

   public static EntitySelectorOptions.SelectorHandler getHandler(EntitySelectorReader reader, String option, int restoreCursor) throws CommandSyntaxException {
      EntitySelectorOptions.SelectorOption selectorOption = (EntitySelectorOptions.SelectorOption)options.get(option);
      if (selectorOption != null) {
         if (selectorOption.condition.test(reader)) {
            return selectorOption.handler;
         } else {
            throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
         }
      } else {
         reader.getReader().setCursor(restoreCursor);
         throw UNKNOWN_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
      }
   }

   public static void suggestOptions(EntitySelectorReader reader, SuggestionsBuilder suggestionBuilder) {
      String string = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
      Iterator var3 = options.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<String, EntitySelectorOptions.SelectorOption> entry = (Entry)var3.next();
         if (((EntitySelectorOptions.SelectorOption)entry.getValue()).condition.test(reader) && ((String)entry.getKey()).toLowerCase(Locale.ROOT).startsWith(string)) {
            suggestionBuilder.suggest((String)entry.getKey() + '=', ((EntitySelectorOptions.SelectorOption)entry.getValue()).description);
         }
      }

   }

   static class SelectorOption {
      public final EntitySelectorOptions.SelectorHandler handler;
      public final Predicate<EntitySelectorReader> condition;
      public final Text description;

      private SelectorOption(EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
         this.handler = handler;
         this.condition = condition;
         this.description = description;
      }
   }

   public interface SelectorHandler {
      void handle(EntitySelectorReader reader) throws CommandSyntaxException;
   }
}
