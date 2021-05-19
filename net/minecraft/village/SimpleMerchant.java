package net.minecraft.village;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SimpleMerchant implements Merchant {
   private final MerchantInventory merchantInventory;
   private final PlayerEntity player;
   private TradeOfferList recipeList = new TradeOfferList();
   private int experience;

   public SimpleMerchant(PlayerEntity playerEntity) {
      this.player = playerEntity;
      this.merchantInventory = new MerchantInventory(this);
   }

   @Nullable
   public PlayerEntity getCurrentCustomer() {
      return this.player;
   }

   public void setCurrentCustomer(@Nullable PlayerEntity customer) {
   }

   public TradeOfferList getOffers() {
      return this.recipeList;
   }

   @Environment(EnvType.CLIENT)
   public void setOffersFromServer(@Nullable TradeOfferList offers) {
      this.recipeList = offers;
   }

   public void trade(TradeOffer offer) {
      offer.use();
   }

   public void onSellingItem(ItemStack stack) {
   }

   public World getMerchantWorld() {
      return this.player.world;
   }

   public int getExperience() {
      return this.experience;
   }

   public void setExperienceFromServer(int experience) {
      this.experience = experience;
   }

   public boolean isLeveledMerchant() {
      return true;
   }

   public SoundEvent getYesSound() {
      return SoundEvents.ENTITY_VILLAGER_YES;
   }
}
