package ca.teamdman.sfm.integration.mui2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

/** Common-side MUI2 binding and optional Dynamic Inventory public API integration. */
public final class PlayerInventoryWidgets {

    private static final SlotGroupWidget.SlotConsumer IDENTITY = (index, slot) -> slot;

    private PlayerInventoryWidgets() {}

    /** Bind the real owner's inventory through MUI2 so inventory capacity extensions participate. */
    public static SlotGroupWidget bind(PanelSyncManager syncManager, EntityPlayer owner) {
        Objects.requireNonNull(syncManager, "syncManager");
        Objects.requireNonNull(owner, "owner");
        syncManager.bindPlayerInventory(owner);
        return create(IDENTITY, owner.inventory);
    }

    /**
     * Build a nine-column, three-row scrolling viewport for slots already bound with
     * MUI2's canonical player sync keys. Fixed presentation keeps manager controls
     * inside the minimum 320x240 scaled screen, including expanded inventories.
     * The optional method is looked up once; no local unpublished inventory JAR is required.
     */
    public static SlotGroupWidget create(SlotGroupWidget.SlotConsumer mapper, InventoryPlayer owner) {
        Objects.requireNonNull(mapper, "mapper");
        Objects.requireNonNull(owner, "owner");
        Method factory = DynamicInventoryApi.FACTORY;
        if (factory == null) return SlotGroupWidget.playerInventory(mapper);
        try {
            return (SlotGroupWidget) factory.invoke(null, mapper, 9, 3, owner);
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("Cannot access Dynamic Inventory's public MUI2 factory", error);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new IllegalStateException("Dynamic Inventory's MUI2 factory failed", cause);
        }
    }

    private static final class DynamicInventoryApi {

        private static final Method FACTORY = findFactory();

        private static Method findFactory() {
            try {
                Class<?> api = Class.forName(
                    "dev.gilbe.dynamicinventory.api.mui2.Mui2InventoryWidgets",
                    false,
                    PlayerInventoryWidgets.class.getClassLoader());
                Method method = api.getMethod(
                    "playerInventory",
                    SlotGroupWidget.SlotConsumer.class,
                    int.class,
                    int.class,
                    InventoryPlayer.class);
                if (!SlotGroupWidget.class.isAssignableFrom(method.getReturnType()))
                    throw new IllegalStateException("Incompatible Dynamic Inventory MUI2 factory return type");
                return method;
            } catch (ClassNotFoundException absent) {
                return null;
            } catch (NoSuchMethodException incompatible) {
                // An installed incompatible API must not silently hide expanded storage slots.
                throw new IllegalStateException(
                    "Dynamic Inventory needs its explicit-owner MUI2 public API",
                    incompatible);
            }
        }
    }
}
