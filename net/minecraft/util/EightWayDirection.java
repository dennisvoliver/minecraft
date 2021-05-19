package net.minecraft.util;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.util.math.Direction;

public enum EightWayDirection {
   NORTH(new Direction[]{Direction.NORTH}),
   NORTH_EAST(new Direction[]{Direction.NORTH, Direction.EAST}),
   EAST(new Direction[]{Direction.EAST}),
   SOUTH_EAST(new Direction[]{Direction.SOUTH, Direction.EAST}),
   SOUTH(new Direction[]{Direction.SOUTH}),
   SOUTH_WEST(new Direction[]{Direction.SOUTH, Direction.WEST}),
   WEST(new Direction[]{Direction.WEST}),
   NORTH_WEST(new Direction[]{Direction.NORTH, Direction.WEST});

   private static final int NORTHWEST_BIT = 1 << NORTH_WEST.ordinal();
   private static final int WEST_BIT = 1 << WEST.ordinal();
   private static final int SOUTHWEST_BIT = 1 << SOUTH_WEST.ordinal();
   private static final int SOUTH_BIT = 1 << SOUTH.ordinal();
   private static final int SOUTHEAST_BIT = 1 << SOUTH_EAST.ordinal();
   private static final int EAST_BIT = 1 << EAST.ordinal();
   private static final int NORTHEAST_BIT = 1 << NORTH_EAST.ordinal();
   private static final int NORTH_BIT = 1 << NORTH.ordinal();
   private final Set<Direction> directions;

   private EightWayDirection(Direction... directions) {
      this.directions = Sets.immutableEnumSet(Arrays.asList(directions));
   }

   public Set<Direction> getDirections() {
      return this.directions;
   }
}
