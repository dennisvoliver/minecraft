package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class LeadItem extends Item {
   public LeadItem(Item.Settings settings) {
      super(settings);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World world = context.getWorld();
      BlockPos blockPos = context.getBlockPos();
      Block block = world.getBlockState(blockPos).getBlock();
      if (block.isIn(BlockTags.FENCES)) {
         PlayerEntity playerEntity = context.getPlayer();
         if (!world.isClient && playerEntity != null) {
            attachHeldMobsToBlock(playerEntity, world, blockPos);
         }

         return ActionResult.success(world.isClient);
      } else {
         return ActionResult.PASS;
      }
   }

   public static ActionResult attachHeldMobsToBlock(PlayerEntity playerEntity, World world, BlockPos blockPos) {
      LeashKnotEntity leashKnotEntity = null;
      boolean bl = false;
      double d = 7.0D;
      int i = blockPos.getX();
      int j = blockPos.getY();
      int k = blockPos.getZ();
      List<MobEntity> list = world.getNonSpectatingEntities(MobEntity.class, new Box((double)i - 7.0D, (double)j - 7.0D, (double)k - 7.0D, (double)i + 7.0D, (double)j + 7.0D, (double)k + 7.0D));
      Iterator var11 = list.iterator();

      while(var11.hasNext()) {
         MobEntity mobEntity = (MobEntity)var11.next();
         if (mobEntity.getHoldingEntity() == playerEntity) {
            if (leashKnotEntity == null) {
               leashKnotEntity = LeashKnotEntity.getOrCreate(world, blockPos);
            }

            mobEntity.attachLeash(leashKnotEntity, true);
            bl = true;
         }
      }

      return bl ? ActionResult.SUCCESS : ActionResult.PASS;
   }
}
