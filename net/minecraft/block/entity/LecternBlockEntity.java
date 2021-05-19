package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class LecternBlockEntity extends BlockEntity implements Clearable, NamedScreenHandlerFactory {
   private final Inventory inventory = new Inventory() {
      public int size() {
         return 1;
      }

      public boolean isEmpty() {
         return LecternBlockEntity.this.book.isEmpty();
      }

      public ItemStack getStack(int slot) {
         return slot == 0 ? LecternBlockEntity.this.book : ItemStack.EMPTY;
      }

      public ItemStack removeStack(int slot, int amount) {
         if (slot == 0) {
            ItemStack itemStack = LecternBlockEntity.this.book.split(amount);
            if (LecternBlockEntity.this.book.isEmpty()) {
               LecternBlockEntity.this.onBookRemoved();
            }

            return itemStack;
         } else {
            return ItemStack.EMPTY;
         }
      }

      public ItemStack removeStack(int slot) {
         if (slot == 0) {
            ItemStack itemStack = LecternBlockEntity.this.book;
            LecternBlockEntity.this.book = ItemStack.EMPTY;
            LecternBlockEntity.this.onBookRemoved();
            return itemStack;
         } else {
            return ItemStack.EMPTY;
         }
      }

      public void setStack(int slot, ItemStack stack) {
      }

      public int getMaxCountPerStack() {
         return 1;
      }

      public void markDirty() {
         LecternBlockEntity.this.markDirty();
      }

      public boolean canPlayerUse(PlayerEntity player) {
         if (LecternBlockEntity.this.world.getBlockEntity(LecternBlockEntity.this.pos) != LecternBlockEntity.this) {
            return false;
         } else {
            return player.squaredDistanceTo((double)LecternBlockEntity.this.pos.getX() + 0.5D, (double)LecternBlockEntity.this.pos.getY() + 0.5D, (double)LecternBlockEntity.this.pos.getZ() + 0.5D) > 64.0D ? false : LecternBlockEntity.this.hasBook();
         }
      }

      public boolean isValid(int slot, ItemStack stack) {
         return false;
      }

      public void clear() {
      }
   };
   private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
      public int get(int index) {
         return index == 0 ? LecternBlockEntity.this.currentPage : 0;
      }

      public void set(int index, int value) {
         if (index == 0) {
            LecternBlockEntity.this.setCurrentPage(value);
         }

      }

      public int size() {
         return 1;
      }
   };
   private ItemStack book;
   private int currentPage;
   private int pageCount;

   public LecternBlockEntity() {
      super(BlockEntityType.LECTERN);
      this.book = ItemStack.EMPTY;
   }

   public ItemStack getBook() {
      return this.book;
   }

   public boolean hasBook() {
      Item item = this.book.getItem();
      return item == Items.WRITABLE_BOOK || item == Items.WRITTEN_BOOK;
   }

   public void setBook(ItemStack book) {
      this.setBook(book, (PlayerEntity)null);
   }

   private void onBookRemoved() {
      this.currentPage = 0;
      this.pageCount = 0;
      LecternBlock.setHasBook(this.getWorld(), this.getPos(), this.getCachedState(), false);
   }

   public void setBook(ItemStack book, @Nullable PlayerEntity player) {
      this.book = this.resolveBook(book, player);
      this.currentPage = 0;
      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.markDirty();
   }

   private void setCurrentPage(int currentPage) {
      int i = MathHelper.clamp(currentPage, 0, this.pageCount - 1);
      if (i != this.currentPage) {
         this.currentPage = i;
         this.markDirty();
         LecternBlock.setPowered(this.getWorld(), this.getPos(), this.getCachedState());
      }

   }

   public int getCurrentPage() {
      return this.currentPage;
   }

   public int getComparatorOutput() {
      float f = this.pageCount > 1 ? (float)this.getCurrentPage() / ((float)this.pageCount - 1.0F) : 1.0F;
      return MathHelper.floor(f * 14.0F) + (this.hasBook() ? 1 : 0);
   }

   private ItemStack resolveBook(ItemStack book, @Nullable PlayerEntity player) {
      if (this.world instanceof ServerWorld && book.getItem() == Items.WRITTEN_BOOK) {
         WrittenBookItem.resolve(book, this.getCommandSource(player), player);
      }

      return book;
   }

   private ServerCommandSource getCommandSource(@Nullable PlayerEntity player) {
      String string2;
      Object text2;
      if (player == null) {
         string2 = "Lectern";
         text2 = new LiteralText("Lectern");
      } else {
         string2 = player.getName().getString();
         text2 = player.getDisplayName();
      }

      Vec3d vec3d = Vec3d.ofCenter(this.pos);
      return new ServerCommandSource(CommandOutput.DUMMY, vec3d, Vec2f.ZERO, (ServerWorld)this.world, 2, string2, (Text)text2, this.world.getServer(), player);
   }

   public boolean copyItemDataRequiresOperator() {
      return true;
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      if (tag.contains("Book", 10)) {
         this.book = this.resolveBook(ItemStack.fromTag(tag.getCompound("Book")), (PlayerEntity)null);
      } else {
         this.book = ItemStack.EMPTY;
      }

      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.currentPage = MathHelper.clamp(tag.getInt("Page"), 0, this.pageCount - 1);
   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      if (!this.getBook().isEmpty()) {
         tag.put("Book", this.getBook().toTag(new CompoundTag()));
         tag.putInt("Page", this.currentPage);
      }

      return tag;
   }

   public void clear() {
      this.setBook(ItemStack.EMPTY);
   }

   public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
      return new LecternScreenHandler(i, this.inventory, this.propertyDelegate);
   }

   public Text getDisplayName() {
      return new TranslatableText("container.lectern");
   }
}
