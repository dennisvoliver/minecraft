package net.minecraft.world.gen.tree;

import com.mojang.serialization.Codec;
import net.minecraft.util.registry.Registry;

public class TreeDecoratorType<P extends TreeDecorator> {
   public static final TreeDecoratorType<TrunkVineTreeDecorator> TRUNK_VINE;
   public static final TreeDecoratorType<LeaveVineTreeDecorator> LEAVE_VINE;
   public static final TreeDecoratorType<CocoaBeansTreeDecorator> COCOA;
   public static final TreeDecoratorType<BeehiveTreeDecorator> BEEHIVE;
   public static final TreeDecoratorType<AlterGroundTreeDecorator> ALTER_GROUND;
   private final Codec<P> codec;

   private static <P extends TreeDecorator> TreeDecoratorType<P> register(String id, Codec<P> codec) {
      return (TreeDecoratorType)Registry.register(Registry.TREE_DECORATOR_TYPE, (String)id, new TreeDecoratorType(codec));
   }

   private TreeDecoratorType(Codec<P> codec) {
      this.codec = codec;
   }

   public Codec<P> getCodec() {
      return this.codec;
   }

   static {
      TRUNK_VINE = register("trunk_vine", TrunkVineTreeDecorator.CODEC);
      LEAVE_VINE = register("leave_vine", LeaveVineTreeDecorator.CODEC);
      COCOA = register("cocoa", CocoaBeansTreeDecorator.CODEC);
      BEEHIVE = register("beehive", BeehiveTreeDecorator.CODEC);
      ALTER_GROUND = register("alter_ground", AlterGroundTreeDecorator.CODEC);
   }
}
