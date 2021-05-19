package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
public abstract class SpriteBillboardParticle extends BillboardParticle {
   protected Sprite sprite;

   protected SpriteBillboardParticle(ClientWorld clientWorld, double d, double e, double f) {
      super(clientWorld, d, e, f);
   }

   protected SpriteBillboardParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
      super(clientWorld, d, e, f, g, h, i);
   }

   protected void setSprite(Sprite sprite) {
      this.sprite = sprite;
   }

   protected float getMinU() {
      return this.sprite.getMinU();
   }

   protected float getMaxU() {
      return this.sprite.getMaxU();
   }

   protected float getMinV() {
      return this.sprite.getMinV();
   }

   protected float getMaxV() {
      return this.sprite.getMaxV();
   }

   public void setSprite(SpriteProvider spriteProvider) {
      this.setSprite(spriteProvider.getSprite(this.random));
   }

   public void setSpriteForAge(SpriteProvider spriteProvider) {
      this.setSprite(spriteProvider.getSprite(this.age, this.maxAge));
   }
}
