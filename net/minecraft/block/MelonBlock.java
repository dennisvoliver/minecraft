package net.minecraft.block;

public class MelonBlock extends GourdBlock {
   protected MelonBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public StemBlock getStem() {
      return (StemBlock)Blocks.MELON_STEM;
   }

   public AttachedStemBlock getAttachedStem() {
      return (AttachedStemBlock)Blocks.ATTACHED_MELON_STEM;
   }
}
