package ca.teamdman.sfm.compat;

import cpw.mods.fml.common.Loader;

public final class Compat {

    public static final String MODID_SFM = "StevesFactoryManager";
    public static final String MODID_STEVES_ADDONS = "StevesAddons";
    public static final String MODID_NEI = "NotEnoughItems";

    private static Boolean sfmLoaded = null;
    private static Boolean stevesAddonsLoaded = null;
    private static Boolean neiLoaded = null;

    private Compat() {}

    public static boolean isSFMLoaded() {
        if (sfmLoaded == null) {
            try {
                sfmLoaded = Loader.isModLoaded(MODID_SFM);
            } catch (Throwable t) {
                sfmLoaded = false;
            }
        }
        return sfmLoaded;
    }

    public static boolean isStevesAddonsLoaded() {
        if (stevesAddonsLoaded == null) {
            try {
                stevesAddonsLoaded = Loader.isModLoaded(MODID_STEVES_ADDONS);
            } catch (Throwable t) {
                stevesAddonsLoaded = false;
            }
        }
        return stevesAddonsLoaded;
    }

    public static boolean isNEILoaded() {
        if (neiLoaded == null) {
            try {
                neiLoaded = Loader.isModLoaded(MODID_NEI);
            } catch (Throwable t) {
                neiLoaded = false;
            }
        }
        return neiLoaded;
    }
}
