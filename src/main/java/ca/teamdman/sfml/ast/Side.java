package ca.teamdman.sfml.ast;

import net.minecraftforge.common.util.ForgeDirection;

public enum Side implements ASTNode {

    TOP(ForgeDirection.UP),
    BOTTOM(ForgeDirection.DOWN),
    NORTH(ForgeDirection.NORTH),
    SOUTH(ForgeDirection.SOUTH),
    EAST(ForgeDirection.EAST),
    WEST(ForgeDirection.WEST),
    NULL(ForgeDirection.UNKNOWN);

    private final ForgeDirection direction;

    Side(ForgeDirection direction) {
        this.direction = direction;
    }

    public ForgeDirection getDirection() {
        return direction;
    }

    public static Side from(String name) {
        if (name == null) return NULL;
        switch (name.toUpperCase(java.util.Locale.ROOT)) {
            case "TOP":
            case "UP":
                return TOP;
            case "BOTTOM":
            case "DOWN":
                return BOTTOM;
            case "NORTH":
                return NORTH;
            case "SOUTH":
                return SOUTH;
            case "EAST":
                return EAST;
            case "WEST":
                return WEST;
            case "NULL":
                return NULL;
            default:
                throw new IllegalArgumentException("Unsupported side: " + name + "; use an absolute direction");
        }
    }
}
