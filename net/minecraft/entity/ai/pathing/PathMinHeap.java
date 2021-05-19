package net.minecraft.entity.ai.pathing;

public class PathMinHeap {
   private PathNode[] pathNodes = new PathNode[128];
   private int count;

   public PathNode push(PathNode node) {
      if (node.heapIndex >= 0) {
         throw new IllegalStateException("OW KNOWS!");
      } else {
         if (this.count == this.pathNodes.length) {
            PathNode[] pathNodes = new PathNode[this.count << 1];
            System.arraycopy(this.pathNodes, 0, pathNodes, 0, this.count);
            this.pathNodes = pathNodes;
         }

         this.pathNodes[this.count] = node;
         node.heapIndex = this.count;
         this.shiftUp(this.count++);
         return node;
      }
   }

   public void clear() {
      this.count = 0;
   }

   public PathNode pop() {
      PathNode pathNode = this.pathNodes[0];
      this.pathNodes[0] = this.pathNodes[--this.count];
      this.pathNodes[this.count] = null;
      if (this.count > 0) {
         this.shiftDown(0);
      }

      pathNode.heapIndex = -1;
      return pathNode;
   }

   public void setNodeWeight(PathNode node, float weight) {
      float f = node.heapWeight;
      node.heapWeight = weight;
      if (weight < f) {
         this.shiftUp(node.heapIndex);
      } else {
         this.shiftDown(node.heapIndex);
      }

   }

   private void shiftUp(int index) {
      PathNode pathNode = this.pathNodes[index];

      int i;
      for(float f = pathNode.heapWeight; index > 0; index = i) {
         i = index - 1 >> 1;
         PathNode pathNode2 = this.pathNodes[i];
         if (!(f < pathNode2.heapWeight)) {
            break;
         }

         this.pathNodes[index] = pathNode2;
         pathNode2.heapIndex = index;
      }

      this.pathNodes[index] = pathNode;
      pathNode.heapIndex = index;
   }

   private void shiftDown(int index) {
      PathNode pathNode = this.pathNodes[index];
      float f = pathNode.heapWeight;

      while(true) {
         int i = 1 + (index << 1);
         int j = i + 1;
         if (i >= this.count) {
            break;
         }

         PathNode pathNode2 = this.pathNodes[i];
         float g = pathNode2.heapWeight;
         PathNode pathNode4;
         float k;
         if (j >= this.count) {
            pathNode4 = null;
            k = Float.POSITIVE_INFINITY;
         } else {
            pathNode4 = this.pathNodes[j];
            k = pathNode4.heapWeight;
         }

         if (g < k) {
            if (!(g < f)) {
               break;
            }

            this.pathNodes[index] = pathNode2;
            pathNode2.heapIndex = index;
            index = i;
         } else {
            if (!(k < f)) {
               break;
            }

            this.pathNodes[index] = pathNode4;
            pathNode4.heapIndex = index;
            index = j;
         }
      }

      this.pathNodes[index] = pathNode;
      pathNode.heapIndex = index;
   }

   public boolean isEmpty() {
      return this.count == 0;
   }
}
