package ca.teamdman.sfm.compat.nei;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import org.lwjgl.input.Keyboard;

import ca.teamdman.sfm.client.gui.RecipeSelectionHint;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerInputHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.Recipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Optional client hook. Display data is only a search hint; Studio asks the server for actual candidates. */
@SideOnly(Side.CLIENT)
public final class NeiRecipeSelectionClient implements IContainerInputHandler {

    private static final RecipeSelectionHint HINT = RecipeSelectionHint.NEI;
    private static boolean initialized;
    private Method recipeMap;
    private Field mapName;

    public static void init() {
        if (!initialized) {
            GuiContainerManager.addInputHandler(new NeiRecipeSelectionClient());
            initialized = true;
        }
    }

    public static RecipeSelectionHint.Value current() {
        return HINT.current(Minecraft.getMinecraft().theWorld);
    }

    @Override
    public boolean keyTyped(GuiContainer gui, char character, int key) {
        if (!(gui instanceof GuiRecipe) || key != Keyboard.KEY_S
            || !GuiScreen.isCtrlKeyDown()
            || !GuiScreen.isShiftKeyDown()) return false;
        if (Keyboard.isRepeatEvent()) return true;
        Minecraft client = Minecraft.getMinecraft();
        try {
            GuiRecipe<?> screen = (GuiRecipe<?>) gui;
            IRecipeHandler handler = screen.getHandler();
            if (handler == null || !handler.getClass()
                .getName()
                .equals("gregtech.nei.GTNEIDefaultHandler"))
                throw new IllegalArgumentException("Choose a supported GregTech recipe tab first");
            Recipe focused = screen.getFocusedRecipe();
            ItemStack stack = focused == null ? null : focused.getResult();
            if (stack == null || stack.getTagCompound() != null)
                throw new IllegalArgumentException("This shortcut currently supports item outputs with no NBT only");
            if (recipeMap == null) {
                recipeMap = handler.getClass()
                    .getMethod("getRecipeMap");
                mapName = recipeMap.getReturnType()
                    .getField("unlocalizedName");
            }
            Object map = recipeMap.invoke(handler);
            if (map == null) throw new IllegalArgumentException("This recipe tab has no supported map");
            String id = Item.itemRegistry.getNameForObject(stack.getItem());
            int metadata = stack.getItemDamage();
            RecipeSelectionHint.Value hint = new RecipeSelectionHint.Value(id, metadata, (String) mapName.get(map));
            // Metadata getters are third-party callbacks. Do not silently drop a tag added during inspection.
            if (stack.getTagCompound() != null)
                throw new IllegalArgumentException("NBT-bearing outputs are not supported by this shortcut yet");
            HINT.capture(client.theWorld, hint);
            message("Output/map hint remembered. Open the station Recipe inspector to query server candidates.");
        } catch (IllegalArgumentException failure) {
            message(failure.getMessage());
        } catch (ReflectiveOperationException | LinkageError failure) {
            message("This NEI/GregTech recipe handler is not supported");
        } catch (RuntimeException failure) {
            message("The displayed recipe output could not be inspected");
        }
        return true;
    }

    private static void message(String text) {
        if (Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText("[SFM] " + ca.teamdman.sfm.common.program.DebugValueFormatter.sanitize(text)));
    }

    @Override
    public void onKeyTyped(GuiContainer gui, char character, int key) {}

    @Override
    public boolean lastKeyTyped(GuiContainer gui, char character, int key) {
        return false;
    }

    @Override
    public boolean mouseClicked(GuiContainer gui, int x, int y, int button) {
        return false;
    }

    @Override
    public void onMouseClicked(GuiContainer gui, int x, int y, int button) {}

    @Override
    public void onMouseUp(GuiContainer gui, int x, int y, int button) {}

    @Override
    public boolean mouseScrolled(GuiContainer gui, int x, int y, int amount) {
        return false;
    }

    @Override
    public void onMouseScrolled(GuiContainer gui, int x, int y, int amount) {}

    @Override
    public void onMouseDragged(GuiContainer gui, int x, int y, int button, long elapsed) {}
}
