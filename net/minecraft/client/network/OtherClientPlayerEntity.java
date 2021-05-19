package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Represents a player entity that is present on the client but is not the client's own player.
 */
@Environment(EnvType.CLIENT)
public class OtherClientPlayerEntity extends AbstractClientPlayerEntity {
   public OtherClientPlayerEntity(ClientWorld clientWorld, GameProfile gameProfile) {
      super(clientWorld, gameProfile);
      this.stepHeight = 1.0F;
      this.noClip = true;
   }

   public boolean shouldRender(double distance) {
      double d = this.getBoundingBox().getAverageSideLength() * 10.0D;
      if (Double.isNaN(d)) {
         d = 1.0D;
      }

      d *= 64.0D * getRenderDistanceMultiplier();
      return distance < d * d;
   }

   public boolean damage(DamageSource source, float amount) {
      return true;
   }

   public void tick() {
      super.tick();
      this.method_29242(this, false);
   }

   public void tickMovement() {
      if (this.bodyTrackingIncrements > 0) {
         double d = this.getX() + (this.serverX - this.getX()) / (double)this.bodyTrackingIncrements;
         double e = this.getY() + (this.serverY - this.getY()) / (double)this.bodyTrackingIncrements;
         double f = this.getZ() + (this.serverZ - this.getZ()) / (double)this.bodyTrackingIncrements;
         this.yaw = (float)((double)this.yaw + MathHelper.wrapDegrees(this.serverYaw - (double)this.yaw) / (double)this.bodyTrackingIncrements);
         this.pitch = (float)((double)this.pitch + (this.serverPitch - (double)this.pitch) / (double)this.bodyTrackingIncrements);
         --this.bodyTrackingIncrements;
         this.updatePosition(d, e, f);
         this.setRotation(this.yaw, this.pitch);
      }

      if (this.headTrackingIncrements > 0) {
         this.headYaw = (float)((double)this.headYaw + MathHelper.wrapDegrees(this.serverHeadYaw - (double)this.headYaw) / (double)this.headTrackingIncrements);
         --this.headTrackingIncrements;
      }

      this.prevStrideDistance = this.strideDistance;
      this.tickHandSwing();
      float h;
      if (this.onGround && !this.isDead()) {
         h = Math.min(0.1F, MathHelper.sqrt(squaredHorizontalLength(this.getVelocity())));
      } else {
         h = 0.0F;
      }

      float var2;
      if (!this.onGround && !this.isDead()) {
         var2 = (float)Math.atan(-this.getVelocity().y * 0.20000000298023224D) * 15.0F;
      } else {
         var2 = 0.0F;
      }

      this.strideDistance += (h - this.strideDistance) * 0.4F;
      this.world.getProfiler().push("push");
      this.tickCramming();
      this.world.getProfiler().pop();
   }

   protected void updateSize() {
   }

   public void sendSystemMessage(Text message, UUID senderUuid) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (!minecraftClient.shouldBlockMessages(senderUuid)) {
         minecraftClient.inGameHud.getChatHud().addMessage(message);
      }

   }
}
