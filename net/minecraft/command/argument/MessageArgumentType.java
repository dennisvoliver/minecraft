package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class MessageArgumentType implements ArgumentType<MessageArgumentType.MessageFormat> {
   private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

   public static MessageArgumentType message() {
      return new MessageArgumentType();
   }

   public static Text getMessage(CommandContext<ServerCommandSource> command, String name) throws CommandSyntaxException {
      return ((MessageArgumentType.MessageFormat)command.getArgument(name, MessageArgumentType.MessageFormat.class)).format((ServerCommandSource)command.getSource(), ((ServerCommandSource)command.getSource()).hasPermissionLevel(2));
   }

   public MessageArgumentType.MessageFormat parse(StringReader stringReader) throws CommandSyntaxException {
      return MessageArgumentType.MessageFormat.parse(stringReader, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class MessageSelector {
      private final int start;
      private final int end;
      private final EntitySelector selector;

      public MessageSelector(int i, int j, EntitySelector entitySelector) {
         this.start = i;
         this.end = j;
         this.selector = entitySelector;
      }

      public int getStart() {
         return this.start;
      }

      public int getEnd() {
         return this.end;
      }

      @Nullable
      public Text format(ServerCommandSource serverCommandSource) throws CommandSyntaxException {
         return EntitySelector.getNames(this.selector.getEntities(serverCommandSource));
      }
   }

   public static class MessageFormat {
      private final String contents;
      private final MessageArgumentType.MessageSelector[] selectors;

      public MessageFormat(String string, MessageArgumentType.MessageSelector[] messageSelectors) {
         this.contents = string;
         this.selectors = messageSelectors;
      }

      public Text format(ServerCommandSource serverCommandSource, boolean bl) throws CommandSyntaxException {
         if (this.selectors.length != 0 && bl) {
            MutableText mutableText = new LiteralText(this.contents.substring(0, this.selectors[0].getStart()));
            int i = this.selectors[0].getStart();
            MessageArgumentType.MessageSelector[] var5 = this.selectors;
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               MessageArgumentType.MessageSelector messageSelector = var5[var7];
               Text text = messageSelector.format(serverCommandSource);
               if (i < messageSelector.getStart()) {
                  mutableText.append(this.contents.substring(i, messageSelector.getStart()));
               }

               if (text != null) {
                  mutableText.append(text);
               }

               i = messageSelector.getEnd();
            }

            if (i < this.contents.length()) {
               mutableText.append(this.contents.substring(i, this.contents.length()));
            }

            return mutableText;
         } else {
            return new LiteralText(this.contents);
         }
      }

      public static MessageArgumentType.MessageFormat parse(StringReader stringReader, boolean bl) throws CommandSyntaxException {
         String string = stringReader.getString().substring(stringReader.getCursor(), stringReader.getTotalLength());
         if (!bl) {
            stringReader.setCursor(stringReader.getTotalLength());
            return new MessageArgumentType.MessageFormat(string, new MessageArgumentType.MessageSelector[0]);
         } else {
            List<MessageArgumentType.MessageSelector> list = Lists.newArrayList();
            int i = stringReader.getCursor();

            while(true) {
               int j;
               EntitySelector entitySelector2;
               label38:
               while(true) {
                  while(stringReader.canRead()) {
                     if (stringReader.peek() == '@') {
                        j = stringReader.getCursor();

                        try {
                           EntitySelectorReader entitySelectorReader = new EntitySelectorReader(stringReader);
                           entitySelector2 = entitySelectorReader.read();
                           break label38;
                        } catch (CommandSyntaxException var8) {
                           if (var8.getType() != EntitySelectorReader.MISSING_EXCEPTION && var8.getType() != EntitySelectorReader.UNKNOWN_SELECTOR_EXCEPTION) {
                              throw var8;
                           }

                           stringReader.setCursor(j + 1);
                        }
                     } else {
                        stringReader.skip();
                     }
                  }

                  return new MessageArgumentType.MessageFormat(string, (MessageArgumentType.MessageSelector[])list.toArray(new MessageArgumentType.MessageSelector[list.size()]));
               }

               list.add(new MessageArgumentType.MessageSelector(j - i, stringReader.getCursor() - i, entitySelector2));
            }
         }
      }
   }
}
