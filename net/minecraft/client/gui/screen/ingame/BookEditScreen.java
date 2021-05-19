package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Rect2i;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BookEditScreen extends Screen {
   private static final Text EDIT_TITLE_TEXT = new TranslatableText("book.editTitle");
   private static final Text FINALIZE_WARNING_TEXT = new TranslatableText("book.finalizeWarning");
   private static final OrderedText BLACK_CURSOR_TEXT;
   private static final OrderedText GRAY_CURSOR_TEXT;
   private final PlayerEntity player;
   private final ItemStack itemStack;
   private boolean dirty;
   private boolean signing;
   private int tickCounter;
   private int currentPage;
   private final List<String> pages = Lists.newArrayList();
   private String title = "";
   private final SelectionManager currentPageSelectionManager = new SelectionManager(this::getCurrentPageContent, this::setPageContent, this::getClipboard, this::setClipboard, (string) -> {
      return string.length() < 1024 && this.textRenderer.getStringBoundedHeight(string, 114) <= 128;
   });
   private final SelectionManager bookTitleSelectionManager = new SelectionManager(() -> {
      return this.title;
   }, (string) -> {
      this.title = string;
   }, this::getClipboard, this::setClipboard, (string) -> {
      return string.length() < 16;
   });
   private long lastClickTime;
   private int lastClickIndex = -1;
   private PageTurnWidget nextPageButton;
   private PageTurnWidget previousPageButton;
   private ButtonWidget doneButton;
   private ButtonWidget signButton;
   private ButtonWidget finalizeButton;
   private ButtonWidget cancelButton;
   private final Hand hand;
   @Nullable
   private BookEditScreen.PageContent pageContent;
   private Text pageIndicatorText;
   private final Text signedByText;

   public BookEditScreen(PlayerEntity player, ItemStack itemStack, Hand hand) {
      super(NarratorManager.EMPTY);
      this.pageContent = BookEditScreen.PageContent.EMPTY;
      this.pageIndicatorText = LiteralText.EMPTY;
      this.player = player;
      this.itemStack = itemStack;
      this.hand = hand;
      CompoundTag compoundTag = itemStack.getTag();
      if (compoundTag != null) {
         ListTag listTag = compoundTag.getList("pages", 8).copy();

         for(int i = 0; i < listTag.size(); ++i) {
            this.pages.add(listTag.getString(i));
         }
      }

      if (this.pages.isEmpty()) {
         this.pages.add("");
      }

      this.signedByText = (new TranslatableText("book.byAuthor", new Object[]{player.getName()})).formatted(Formatting.DARK_GRAY);
   }

   private void setClipboard(String clipboard) {
      if (this.client != null) {
         SelectionManager.setClipboard(this.client, clipboard);
      }

   }

   private String getClipboard() {
      return this.client != null ? SelectionManager.getClipboard(this.client) : "";
   }

   private int countPages() {
      return this.pages.size();
   }

   public void tick() {
      super.tick();
      ++this.tickCounter;
   }

   protected void init() {
      this.invalidatePageContent();
      this.client.keyboard.setRepeatEvents(true);
      this.signButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 100, 196, 98, 20, new TranslatableText("book.signButton"), (buttonWidget) -> {
         this.signing = true;
         this.updateButtons();
      }));
      this.doneButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 2, 196, 98, 20, ScreenTexts.DONE, (buttonWidget) -> {
         this.client.openScreen((Screen)null);
         this.finalizeBook(false);
      }));
      this.finalizeButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 100, 196, 98, 20, new TranslatableText("book.finalizeButton"), (buttonWidget) -> {
         if (this.signing) {
            this.finalizeBook(true);
            this.client.openScreen((Screen)null);
         }

      }));
      this.cancelButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 2, 196, 98, 20, ScreenTexts.CANCEL, (buttonWidget) -> {
         if (this.signing) {
            this.signing = false;
         }

         this.updateButtons();
      }));
      int i = (this.width - 192) / 2;
      int j = true;
      this.nextPageButton = (PageTurnWidget)this.addButton(new PageTurnWidget(i + 116, 159, true, (buttonWidget) -> {
         this.openNextPage();
      }, true));
      this.previousPageButton = (PageTurnWidget)this.addButton(new PageTurnWidget(i + 43, 159, false, (buttonWidget) -> {
         this.openPreviousPage();
      }, true));
      this.updateButtons();
   }

   private void openPreviousPage() {
      if (this.currentPage > 0) {
         --this.currentPage;
      }

      this.updateButtons();
      this.method_27872();
   }

   private void openNextPage() {
      if (this.currentPage < this.countPages() - 1) {
         ++this.currentPage;
      } else {
         this.appendNewPage();
         if (this.currentPage < this.countPages() - 1) {
            ++this.currentPage;
         }
      }

      this.updateButtons();
      this.method_27872();
   }

   public void removed() {
      this.client.keyboard.setRepeatEvents(false);
   }

   private void updateButtons() {
      this.previousPageButton.visible = !this.signing && this.currentPage > 0;
      this.nextPageButton.visible = !this.signing;
      this.doneButton.visible = !this.signing;
      this.signButton.visible = !this.signing;
      this.cancelButton.visible = this.signing;
      this.finalizeButton.visible = this.signing;
      this.finalizeButton.active = !this.title.trim().isEmpty();
   }

   private void removeEmptyPages() {
      ListIterator listIterator = this.pages.listIterator(this.pages.size());

      while(listIterator.hasPrevious() && ((String)listIterator.previous()).isEmpty()) {
         listIterator.remove();
      }

   }

   private void finalizeBook(boolean signBook) {
      if (this.dirty) {
         this.removeEmptyPages();
         ListTag listTag = new ListTag();
         this.pages.stream().map(StringTag::of).forEach(listTag::add);
         if (!this.pages.isEmpty()) {
            this.itemStack.putSubTag("pages", listTag);
         }

         if (signBook) {
            this.itemStack.putSubTag("author", StringTag.of(this.player.getGameProfile().getName()));
            this.itemStack.putSubTag("title", StringTag.of(this.title.trim()));
         }

         int i = this.hand == Hand.MAIN_HAND ? this.player.inventory.selectedSlot : 40;
         this.client.getNetworkHandler().sendPacket(new BookUpdateC2SPacket(this.itemStack, signBook, i));
      }
   }

   private void appendNewPage() {
      if (this.countPages() < 100) {
         this.pages.add("");
         this.dirty = true;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (this.signing) {
         return this.keyPressedSignMode(keyCode, scanCode, modifiers);
      } else {
         boolean bl = this.method_27592(keyCode, scanCode, modifiers);
         if (bl) {
            this.invalidatePageContent();
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (super.charTyped(chr, modifiers)) {
         return true;
      } else if (this.signing) {
         boolean bl = this.bookTitleSelectionManager.insert(chr);
         if (bl) {
            this.updateButtons();
            this.dirty = true;
            return true;
         } else {
            return false;
         }
      } else if (SharedConstants.isValidChar(chr)) {
         this.currentPageSelectionManager.insert(Character.toString(chr));
         this.invalidatePageContent();
         return true;
      } else {
         return false;
      }
   }

   private boolean method_27592(int i, int j, int k) {
      if (Screen.isSelectAll(i)) {
         this.currentPageSelectionManager.selectAll();
         return true;
      } else if (Screen.isCopy(i)) {
         this.currentPageSelectionManager.copy();
         return true;
      } else if (Screen.isPaste(i)) {
         this.currentPageSelectionManager.paste();
         return true;
      } else if (Screen.isCut(i)) {
         this.currentPageSelectionManager.cut();
         return true;
      } else {
         switch(i) {
         case 257:
         case 335:
            this.currentPageSelectionManager.insert("\n");
            return true;
         case 259:
            this.currentPageSelectionManager.delete(-1);
            return true;
         case 261:
            this.currentPageSelectionManager.delete(1);
            return true;
         case 262:
            this.currentPageSelectionManager.moveCursor(1, Screen.hasShiftDown());
            return true;
         case 263:
            this.currentPageSelectionManager.moveCursor(-1, Screen.hasShiftDown());
            return true;
         case 264:
            this.method_27598();
            return true;
         case 265:
            this.method_27597();
            return true;
         case 266:
            this.previousPageButton.onPress();
            return true;
         case 267:
            this.nextPageButton.onPress();
            return true;
         case 268:
            this.moveCursorToTop();
            return true;
         case 269:
            this.moveCursorToBottom();
            return true;
         default:
            return false;
         }
      }
   }

   private void method_27597() {
      this.method_27580(-1);
   }

   private void method_27598() {
      this.method_27580(1);
   }

   private void method_27580(int i) {
      int j = this.currentPageSelectionManager.getSelectionStart();
      int k = this.getPageContent().method_27601(j, i);
      this.currentPageSelectionManager.method_27560(k, Screen.hasShiftDown());
   }

   private void moveCursorToTop() {
      int i = this.currentPageSelectionManager.getSelectionStart();
      int j = this.getPageContent().method_27600(i);
      this.currentPageSelectionManager.method_27560(j, Screen.hasShiftDown());
   }

   private void moveCursorToBottom() {
      BookEditScreen.PageContent pageContent = this.getPageContent();
      int i = this.currentPageSelectionManager.getSelectionStart();
      int j = pageContent.method_27604(i);
      this.currentPageSelectionManager.method_27560(j, Screen.hasShiftDown());
   }

   private boolean keyPressedSignMode(int keyCode, int scanCode, int modifiers) {
      switch(keyCode) {
      case 257:
      case 335:
         if (!this.title.isEmpty()) {
            this.finalizeBook(true);
            this.client.openScreen((Screen)null);
         }

         return true;
      case 259:
         this.bookTitleSelectionManager.delete(-1);
         this.updateButtons();
         this.dirty = true;
         return true;
      default:
         return false;
      }
   }

   private String getCurrentPageContent() {
      return this.currentPage >= 0 && this.currentPage < this.pages.size() ? (String)this.pages.get(this.currentPage) : "";
   }

   private void setPageContent(String newContent) {
      if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
         this.pages.set(this.currentPage, newContent);
         this.dirty = true;
         this.invalidatePageContent();
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.setFocused((Element)null);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(BookScreen.BOOK_TEXTURE);
      int i = (this.width - 192) / 2;
      int j = true;
      this.drawTexture(matrices, i, 2, 0, 0, 192, 192);
      int l;
      int m;
      if (this.signing) {
         boolean bl = this.tickCounter / 6 % 2 == 0;
         OrderedText orderedText = OrderedText.concat(OrderedText.styledString(this.title, Style.EMPTY), bl ? BLACK_CURSOR_TEXT : GRAY_CURSOR_TEXT);
         int k = this.textRenderer.getWidth((StringVisitable)EDIT_TITLE_TEXT);
         this.textRenderer.draw(matrices, (Text)EDIT_TITLE_TEXT, (float)(i + 36 + (114 - k) / 2), 34.0F, 0);
         l = this.textRenderer.getWidth(orderedText);
         this.textRenderer.draw(matrices, (OrderedText)orderedText, (float)(i + 36 + (114 - l) / 2), 50.0F, 0);
         m = this.textRenderer.getWidth((StringVisitable)this.signedByText);
         this.textRenderer.draw(matrices, (Text)this.signedByText, (float)(i + 36 + (114 - m) / 2), 60.0F, 0);
         this.textRenderer.drawTrimmed(FINALIZE_WARNING_TEXT, i + 36, 82, 114, 0);
      } else {
         int n = this.textRenderer.getWidth((StringVisitable)this.pageIndicatorText);
         this.textRenderer.draw(matrices, (Text)this.pageIndicatorText, (float)(i - n + 192 - 44), 18.0F, 0);
         BookEditScreen.PageContent pageContent = this.getPageContent();
         BookEditScreen.Line[] var15 = pageContent.lines;
         l = var15.length;

         for(m = 0; m < l; ++m) {
            BookEditScreen.Line line = var15[m];
            this.textRenderer.draw(matrices, line.text, (float)line.x, (float)line.y, -16777216);
         }

         this.method_27588(pageContent.field_24277);
         this.method_27581(matrices, pageContent.position, pageContent.field_24274);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   private void method_27581(MatrixStack matrices, BookEditScreen.Position position, boolean bl) {
      if (this.tickCounter / 6 % 2 == 0) {
         position = this.method_27590(position);
         if (!bl) {
            int var10001 = position.x;
            int var10002 = position.y - 1;
            int var10003 = position.x + 1;
            int var10004 = position.y;
            this.textRenderer.getClass();
            DrawableHelper.fill(matrices, var10001, var10002, var10003, var10004 + 9, -16777216);
         } else {
            this.textRenderer.draw(matrices, (String)"_", (float)position.x, (float)position.y, 0);
         }
      }

   }

   private void method_27588(Rect2i[] rect2is) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
      RenderSystem.disableTexture();
      RenderSystem.enableColorLogicOp();
      RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
      bufferBuilder.begin(7, VertexFormats.POSITION);
      Rect2i[] var4 = rect2is;
      int var5 = rect2is.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Rect2i rect2i = var4[var6];
         int i = rect2i.getX();
         int j = rect2i.getY();
         int k = i + rect2i.getWidth();
         int l = j + rect2i.getHeight();
         bufferBuilder.vertex((double)i, (double)l, 0.0D).next();
         bufferBuilder.vertex((double)k, (double)l, 0.0D).next();
         bufferBuilder.vertex((double)k, (double)j, 0.0D).next();
         bufferBuilder.vertex((double)i, (double)j, 0.0D).next();
      }

      tessellator.draw();
      RenderSystem.disableColorLogicOp();
      RenderSystem.enableTexture();
   }

   private BookEditScreen.Position method_27582(BookEditScreen.Position position) {
      return new BookEditScreen.Position(position.x - (this.width - 192) / 2 - 36, position.y - 32);
   }

   private BookEditScreen.Position method_27590(BookEditScreen.Position position) {
      return new BookEditScreen.Position(position.x + (this.width - 192) / 2 + 36, position.y + 32);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         if (button == 0) {
            long l = Util.getMeasuringTimeMs();
            BookEditScreen.PageContent pageContent = this.getPageContent();
            int i = pageContent.method_27602(this.textRenderer, this.method_27582(new BookEditScreen.Position((int)mouseX, (int)mouseY)));
            if (i >= 0) {
               if (i == this.lastClickIndex && l - this.lastClickTime < 250L) {
                  if (!this.currentPageSelectionManager.method_27568()) {
                     this.method_27589(i);
                  } else {
                     this.currentPageSelectionManager.selectAll();
                  }
               } else {
                  this.currentPageSelectionManager.method_27560(i, Screen.hasShiftDown());
               }

               this.invalidatePageContent();
            }

            this.lastClickIndex = i;
            this.lastClickTime = l;
         }

         return true;
      }
   }

   private void method_27589(int i) {
      String string = this.getCurrentPageContent();
      this.currentPageSelectionManager.method_27548(TextHandler.moveCursorByWords(string, -1, i, false), TextHandler.moveCursorByWords(string, 1, i, false));
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      } else {
         if (button == 0) {
            BookEditScreen.PageContent pageContent = this.getPageContent();
            int i = pageContent.method_27602(this.textRenderer, this.method_27582(new BookEditScreen.Position((int)mouseX, (int)mouseY)));
            this.currentPageSelectionManager.method_27560(i, true);
            this.invalidatePageContent();
         }

         return true;
      }
   }

   private BookEditScreen.PageContent getPageContent() {
      if (this.pageContent == null) {
         this.pageContent = this.createPageContent();
         this.pageIndicatorText = new TranslatableText("book.pageIndicator", new Object[]{this.currentPage + 1, this.countPages()});
      }

      return this.pageContent;
   }

   private void invalidatePageContent() {
      this.pageContent = null;
   }

   private void method_27872() {
      this.currentPageSelectionManager.moveCaretToEnd();
      this.invalidatePageContent();
   }

   private BookEditScreen.PageContent createPageContent() {
      String string = this.getCurrentPageContent();
      if (string.isEmpty()) {
         return BookEditScreen.PageContent.EMPTY;
      } else {
         int i = this.currentPageSelectionManager.getSelectionStart();
         int j = this.currentPageSelectionManager.getSelectionEnd();
         IntList intList = new IntArrayList();
         List<BookEditScreen.Line> list = Lists.newArrayList();
         MutableInt mutableInt = new MutableInt();
         MutableBoolean mutableBoolean = new MutableBoolean();
         TextHandler textHandler = this.textRenderer.getTextHandler();
         textHandler.wrapLines(string, 114, Style.EMPTY, true, (style, ix, jx) -> {
            int k = mutableInt.getAndIncrement();
            String string2 = string.substring(ix, jx);
            mutableBoolean.setValue(string2.endsWith("\n"));
            String string3 = StringUtils.stripEnd(string2, " \n");
            this.textRenderer.getClass();
            int l = k * 9;
            BookEditScreen.Position position = this.method_27590(new BookEditScreen.Position(0, l));
            intList.add(ix);
            list.add(new BookEditScreen.Line(style, string3, position.x, position.y));
         });
         int[] is = intList.toIntArray();
         boolean bl = i == string.length();
         BookEditScreen.Position position2;
         int m;
         if (bl && mutableBoolean.isTrue()) {
            int var10003 = list.size();
            this.textRenderer.getClass();
            position2 = new BookEditScreen.Position(0, var10003 * 9);
         } else {
            int k = method_27591(is, i);
            m = this.textRenderer.getWidth(string.substring(is[k], i));
            this.textRenderer.getClass();
            position2 = new BookEditScreen.Position(m, k * 9);
         }

         List<Rect2i> list2 = Lists.newArrayList();
         if (i != j) {
            m = Math.min(i, j);
            int n = Math.max(i, j);
            int o = method_27591(is, m);
            int p = method_27591(is, n);
            int q;
            int t;
            if (o == p) {
               this.textRenderer.getClass();
               q = o * 9;
               t = is[o];
               list2.add(this.method_27585(string, textHandler, m, n, q, t));
            } else {
               q = o + 1 > is.length ? string.length() : is[o + 1];
               this.textRenderer.getClass();
               list2.add(this.method_27585(string, textHandler, m, q, o * 9, is[o]));

               for(t = o + 1; t < p; ++t) {
                  this.textRenderer.getClass();
                  int u = t * 9;
                  String string2 = string.substring(is[t], is[t + 1]);
                  int v = (int)textHandler.getWidth(string2);
                  BookEditScreen.Position var10002 = new BookEditScreen.Position(0, u);
                  this.textRenderer.getClass();
                  list2.add(this.method_27583(var10002, new BookEditScreen.Position(v, u + 9)));
               }

               int var10004 = is[p];
               this.textRenderer.getClass();
               list2.add(this.method_27585(string, textHandler, var10004, n, p * 9, is[p]));
            }
         }

         return new BookEditScreen.PageContent(string, position2, bl, is, (BookEditScreen.Line[])list.toArray(new BookEditScreen.Line[0]), (Rect2i[])list2.toArray(new Rect2i[0]));
      }
   }

   private static int method_27591(int[] is, int i) {
      int j = Arrays.binarySearch(is, i);
      return j < 0 ? -(j + 2) : j;
   }

   private Rect2i method_27585(String string, TextHandler textHandler, int i, int j, int k, int l) {
      String string2 = string.substring(l, i);
      String string3 = string.substring(l, j);
      BookEditScreen.Position position = new BookEditScreen.Position((int)textHandler.getWidth(string2), k);
      int var10002 = (int)textHandler.getWidth(string3);
      this.textRenderer.getClass();
      BookEditScreen.Position position2 = new BookEditScreen.Position(var10002, k + 9);
      return this.method_27583(position, position2);
   }

   private Rect2i method_27583(BookEditScreen.Position position, BookEditScreen.Position position2) {
      BookEditScreen.Position position3 = this.method_27590(position);
      BookEditScreen.Position position4 = this.method_27590(position2);
      int i = Math.min(position3.x, position4.x);
      int j = Math.max(position3.x, position4.x);
      int k = Math.min(position3.y, position4.y);
      int l = Math.max(position3.y, position4.y);
      return new Rect2i(i, k, j - i, l - k);
   }

   static {
      BLACK_CURSOR_TEXT = OrderedText.styledString("_", Style.EMPTY.withColor(Formatting.BLACK));
      GRAY_CURSOR_TEXT = OrderedText.styledString("_", Style.EMPTY.withColor(Formatting.GRAY));
   }

   @Environment(EnvType.CLIENT)
   static class PageContent {
      private static final BookEditScreen.PageContent EMPTY;
      private final String pageContent;
      private final BookEditScreen.Position position;
      private final boolean field_24274;
      private final int[] field_24275;
      private final BookEditScreen.Line[] lines;
      private final Rect2i[] field_24277;

      public PageContent(String pageContent, BookEditScreen.Position position, boolean bl, int[] is, BookEditScreen.Line[] lines, Rect2i[] rect2is) {
         this.pageContent = pageContent;
         this.position = position;
         this.field_24274 = bl;
         this.field_24275 = is;
         this.lines = lines;
         this.field_24277 = rect2is;
      }

      public int method_27602(TextRenderer textRenderer, BookEditScreen.Position position) {
         int var10000 = position.y;
         textRenderer.getClass();
         int i = var10000 / 9;
         if (i < 0) {
            return 0;
         } else if (i >= this.lines.length) {
            return this.pageContent.length();
         } else {
            BookEditScreen.Line line = this.lines[i];
            return this.field_24275[i] + textRenderer.getTextHandler().getTrimmedLength(line.content, position.x, line.style);
         }
      }

      public int method_27601(int i, int j) {
         int k = BookEditScreen.method_27591(this.field_24275, i);
         int l = k + j;
         int p;
         if (0 <= l && l < this.field_24275.length) {
            int m = i - this.field_24275[k];
            int n = this.lines[l].content.length();
            p = this.field_24275[l] + Math.min(m, n);
         } else {
            p = i;
         }

         return p;
      }

      public int method_27600(int i) {
         int j = BookEditScreen.method_27591(this.field_24275, i);
         return this.field_24275[j];
      }

      public int method_27604(int i) {
         int j = BookEditScreen.method_27591(this.field_24275, i);
         return this.field_24275[j] + this.lines[j].content.length();
      }

      static {
         EMPTY = new BookEditScreen.PageContent("", new BookEditScreen.Position(0, 0), true, new int[]{0}, new BookEditScreen.Line[]{new BookEditScreen.Line(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
      }
   }

   @Environment(EnvType.CLIENT)
   static class Line {
      private final Style style;
      private final String content;
      private final Text text;
      private final int x;
      private final int y;

      public Line(Style style, String content, int x, int y) {
         this.style = style;
         this.content = content;
         this.x = x;
         this.y = y;
         this.text = (new LiteralText(content)).setStyle(style);
      }
   }

   @Environment(EnvType.CLIENT)
   static class Position {
      public final int x;
      public final int y;

      Position(int x, int y) {
         this.x = x;
         this.y = y;
      }
   }
}
