package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraftforge.common.util.ForgeDirection;

public final class SideQualifier implements ASTNode {

    public static final SideQualifier ALL = new SideQualifier(
        Arrays.asList(Side.TOP, Side.BOTTOM, Side.NORTH, Side.SOUTH, Side.EAST, Side.WEST));
    public static final SideQualifier NULL = new SideQualifier(Collections.singletonList(Side.NULL));
    public static final SideQualifier DEFAULT = ALL;

    private final List<Side> sides;

    public SideQualifier(List<Side> sides) {
        this.sides = sides != null ? sides : Collections.emptyList();
    }

    public List<Side> getSides() {
        return sides;
    }

    public List<ForgeDirection> resolveDirections() {
        List<ForgeDirection> directions = new ArrayList<>();
        for (Side side : sides) {
            if (side == Side.NULL) {
                directions.add(ForgeDirection.UNKNOWN);
                continue;
            }
            if (!directions.contains(side.getDirection())) directions.add(side.getDirection());
        }
        return directions;
    }

    public boolean matches(ForgeDirection direction) {
        if (sides.isEmpty() || sides.contains(Side.NULL)) {
            return true;
        }
        for (Side s : sides) {
            if (s.getDirection() == direction) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SideQualifier that)) return false;
        return Objects.equals(sides, that.sides);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sides);
    }
}
