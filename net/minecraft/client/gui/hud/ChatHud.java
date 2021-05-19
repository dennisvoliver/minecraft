package net.minecraft.client.gui.hud;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChatHud extends DrawableHelper {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftClient client;
   private final List<String> messageHistory = Lists.newArrayList();
   private final List<ChatHudLine<Text>> messages = Lists.newArrayList();
   private final List<ChatHudLine<OrderedText>> visibleMessages = Lists.newArrayList();
   private final Deque<Text> messageQueue = Queues.newArrayDeque();
   private int scrolledLines;
   private boolean hasUnreadNewMessages;
   private long lastMessageAddedTime = 0L;

   public ChatHud(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, int tickDelta) {
      if (!this.isChatHidden()) {
         this.processMessageQueue();
         int i = this.getVisibleLineCount();
         int j = this.visibleMessages.size();
         if (j > 0) {
            boolean bl = false;
            if (this.isChatFocused()) {
               bl = true;
            }

            double d = this.getChatScale();
            int k = MathHelper.ceil((double)this.getWidth() / d);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(2.0F, 8.0F, 0.0F);
            RenderSystem.scaled(d, d, 1.0D);
            double e = this.client.options.chatOpacity * 0.8999999761581421D + 0.10000000149011612D;
            double f = this.client.options.textBackgroundOpacity;
            double g = 9.0D * (this.client.options.chatLineSpacing + 1.0D);
            double h = -8.0D * (this.client.options.chatLineSpacing + 1.0D) + 4.0D * this.client.options.chatLineSpacing;
            int l = 0;

            int m;
            int x;
            int aa;
            int ab;
            for(m = 0; m + this.scrolledLines < this.visibleMessages.size() && m < i; ++m) {
               ChatHudLine<OrderedText> chatHudLine = (ChatHudLine)this.visibleMessages.get(m + this.scrolledLines);
               if (chatHudLine != null) {
                  x = tickDelta - chatHudLine.getCreationTick();
                  if (x < 200 || bl) {
                     double o = bl ? 1.0D : getMessageOpacityMultiplier(x);
                     aa = (int)(255.0D * o * e);
                     ab = (int)(255.0D * o * f);
                     ++l;
                     if (aa > 3) {
                        int r = false;
                        double s = (double)(-m) * g;
                        matrices.push();
                        matrices.translate(0.0D, 0.0D, 50.0D);
                        fill(matrices, -2, (int)(s - g), 0 + k + 4, (int)s, ab << 24);
                        RenderSystem.enableBlend();
                        matrices.translate(0.0D, 0.0D, 50.0D);
                        this.client.textRenderer.drawWithShadow(matrices, (OrderedText)chatHudLine.getText(), 0.0F, (float)((int)(s + h)), 16777215 + (aa << 24));
                        RenderSystem.disableAlphaTest();
                        RenderSystem.disableBlend();
                        matrices.pop();
                     }
                  }
               }
            }

            int w;
            if (!this.messageQueue.isEmpty()) {
               m = (int)(128.0D * e);
               w = (int)(255.0D * f);
               matrices.push();
               matrices.translate(0.0D, 0.0D, 50.0D);
               fill(matrices, -2, 0, k + 4, 9, w << 24);
               RenderSystem.enableBlend();
               matrices.translate(0.0D, 0.0D, 50.0D);
               this.client.textRenderer.drawWithShadow(matrices, (Text)(new TranslatableText("chat.queue", new Object[]{this.messageQueue.size()})), 0.0F, 1.0F, 16777215 + (m << 24));
               matrices.pop();
               RenderSystem.disableAlphaTest();
               RenderSystem.disableBlend();
            }

            if (bl) {
               this.client.textRenderer.getClass();
               int v = 9;
               RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
               w = j * v + j;
               x = l * v + l;
               int y = this.scrolledLines * x / j;
               int z = x * x / w;
               if (w != x) {
                  aa = y > 0 ? 170 : 96;
                  ab = this.hasUnreadNewMessages ? 13382451 : 3355562;
                  fill(matrices, 0, -y, 2, -y - z, ab + (aa << 24));
                  fill(matrices, 2, -y, 1, -y - z, 13421772 + (aa << 24));
               }
            }

            RenderSystem.popMatrix();
         }
      }
   }

   private boolean isChatHidden() {
      return this.client.options.chatVisibility == ChatVisibility.HIDDEN;
   }

   private static double getMessageOpacityMultiplier(int age) {
      double d = (double)age / 200.0D;
      d = 1.0D - d;
      d *= 10.0D;
      d = MathHelper.clamp(d, 0.0D, 1.0D);
      d *= d;
      return d;
   }

   public void clear(boolean clearHistory) {
      this.messageQueue.clear();
      this.visibleMessages.clear();
      this.messages.clear();
      if (clearHistory) {
         this.messageHistory.clear();
      }

   }

   public void addMessage(Text message) {
      this.addMessage(message, 0);
   }

   private void addMessage(Text message, int messageId) {
      this.addMessage(message, messageId, this.client.inGameHud.getTicks(), false);
      LOGGER.info((String)"[CHAT] {}", (Object)message.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
   }

   private void addMessage(Text message, int messageId, int timestamp, boolean refresh) {
      if (messageId != 0) {
         this.removeMessage(messageId);
      }

      int i = MathHelper.floor((double)this.getWidth() / this.getChatScale());
      List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(message, i, this.client.textRenderer);
      boolean bl = this.isChatFocused();

      OrderedText orderedText;
      for(Iterator var8 = list.iterator(); var8.hasNext(); this.visibleMessages.add(0, new ChatHudLine(timestamp, orderedText, messageId))) {
         orderedText = (OrderedText)var8.next();
         if (bl && this.scrolledLines > 0) {
            this.hasUnreadNewMessages = true;
            this.scroll(1.0D);
         }
      }

      while(this.visibleMessages.size() > 100) {
         this.visibleMessages.remove(this.visibleMessages.size() - 1);
      }

      if (!refresh) {
         this.messages.add(0, new ChatHudLine(timestamp, message, messageId));

         while(this.messages.size() > 100) {
            this.messages.remove(this.messages.size() - 1);
         }
      }

   }

   public void reset() {
      this.visibleMessages.clear();
      this.resetScroll();

      for(int i = this.messages.size() - 1; i >= 0; --i) {
         ChatHudLine<Text> chatHudLine = (ChatHudLine)this.messages.get(i);
         this.addMessage((Text)chatHudLine.getText(), chatHudLine.getId(), chatHudLine.getCreationTick(), true);
      }

   }

   public List<String> getMessageHistory() {
      return this.messageHistory;
   }

   public void addToMessageHistory(String message) {
      if (this.messageHistory.isEmpty() || !((String)this.messageHistory.get(this.messageHistory.size() - 1)).equals(message)) {
         this.messageHistory.add(message);
      }

   }

   public void resetScroll() {
      this.scrolledLines = 0;
      this.hasUnreadNewMessages = false;
   }

   public void scroll(double amount) {
      this.scrolledLines = (int)((double)this.scrolledLines + amount);
      int i = this.visibleMessages.size();
      if (this.scrolledLines > i - this.getVisibleLineCount()) {
         this.scrolledLines = i - this.getVisibleLineCount();
      }

      if (this.scrolledLines <= 0) {
         this.scrolledLines = 0;
         this.hasUnreadNewMessages = false;
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY) {
      if (this.isChatFocused() && !this.client.options.hudHidden && !this.isChatHidden() && !this.messageQueue.isEmpty()) {
         double d = mouseX - 2.0D;
         double e = (double)this.client.getWindow().getScaledHeight() - mouseY - 40.0D;
         if (d <= (double)MathHelper.floor((double)this.getWidth() / this.getChatScale()) && e < 0.0D && e > (double)MathHelper.floor(-9.0D * this.getChatScale())) {
            this.addMessage((Text)this.messageQueue.remove());
            this.lastMessageAddedTime = System.currentTimeMillis();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Nullable
   public Style getText(double x, double y) {
      if (this.isChatFocused() && !this.client.options.hudHidden && !this.isChatHidden()) {
         double d = x - 2.0D;
         double e = (double)this.client.getWindow().getScaledHeight() - y - 40.0D;
         d = (double)MathHelper.floor(d / this.getChatScale());
         e = (double)MathHelper.floor(e / (this.getChatScale() * (this.client.options.chatLineSpacing + 1.0D)));
         if (!(d < 0.0D) && !(e < 0.0D)) {
            int i = Math.min(this.getVisibleLineCount(), this.visibleMessages.size());
            if (d <= (double)MathHelper.floor((double)this.getWidth() / this.getChatScale())) {
               this.client.textRenderer.getClass();
               if (e < (double)(9 * i + i)) {
                  this.client.textRenderer.getClass();
                  int j = (int)(e / 9.0D + (double)this.scrolledLines);
                  if (j >= 0 && j < this.visibleMessages.size()) {
                     ChatHudLine<OrderedText> chatHudLine = (ChatHudLine)this.visibleMessages.get(j);
                     return this.client.textRenderer.getTextHandler().getStyleAt((OrderedText)chatHudLine.getText(), (int)d);
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private boolean isChatFocused() {
      return this.client.currentScreen instanceof ChatScreen;
   }

   private void removeMessage(int messageId) {
      this.visibleMessages.removeIf((message) -> {
         return message.getId() == messageId;
      });
      this.messages.removeIf((message) -> {
         return message.getId() == messageId;
      });
   }

   public int getWidth() {
      return getWidth(this.client.options.chatWidth);
   }

   public int getHeight() {
      return getHeight((this.isChatFocused() ? this.client.options.chatHeightFocused : this.client.options.chatHeightUnfocused) / (this.client.options.chatLineSpacing + 1.0D));
   }

   public double getChatScale() {
      return this.client.options.chatScale;
   }

   public static int getWidth(double widthOption) {
      int i = true;
      int j = true;
      return MathHelper.floor(widthOption * 280.0D + 40.0D);
   }

   public static int getHeight(double heightOption) {
      int i = true;
      int j = true;
      return MathHelper.floor(heightOption * 160.0D + 20.0D);
   }

   public int getVisibleLineCount() {
      return this.getHeight() / 9;
   }

   private long getChatDelayMillis() {
      return (long)(this.client.options.chatDelay * 1000.0D);
   }

   private void processMessageQueue() {
      if (!this.messageQueue.isEmpty()) {
         long l = System.currentTimeMillis();
         if (l - this.lastMessageAddedTime >= this.getChatDelayMillis()) {
            this.addMessage((Text)this.messageQueue.remove());
            this.lastMessageAddedTime = l;
         }

      }
   }

   public void queueMessage(Text message) {
      if (this.client.options.chatDelay <= 0.0D) {
         this.addMessage(message);
      } else {
         long l = System.currentTimeMillis();
         if (l - this.lastMessageAddedTime >= this.getChatDelayMillis()) {
            this.addMessage(message);
            this.lastMessageAddedTime = l;
         } else {
            this.messageQueue.add(message);
         }
      }

   }
}
