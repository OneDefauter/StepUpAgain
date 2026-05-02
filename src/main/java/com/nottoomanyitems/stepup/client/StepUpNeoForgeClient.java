package com.nottoomanyitems.stepup.client;

import com.nottoomanyitems.stepup.StepUpConfig;
import com.nottoomanyitems.stepup.StepUpNeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mod(value = StepUpNeoForge.MOD_ID, dist = Dist.CLIENT)
public final class StepUpNeoForgeClient {
    private static final String NEOFORGE_MOD_LIST_SCREEN_CLASS = "net.neoforged.neoforge.client.gui.ModListScreen";

    private final StepChanger stepChanger = new StepChanger();
    private boolean warnedAboutModListPatch;

    public StepUpNeoForgeClient(IEventBus modEventBus, ModContainer modContainer) {
        StepUpConfig.load();

        modEventBus.addListener(this::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onClientTickPre);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onClientPlayerLoggingIn);
        NeoForge.EVENT_BUS.addListener(this::onClientPlayerLoggingOut);
        NeoForge.EVENT_BUS.addListener(this::onScreenInit);
        NeoForge.EVENT_BUS.addListener(this::onScreenMouseClick);
        NeoForge.EVENT_BUS.addListener(this::onScreenKeyPressed);
        NeoForge.EVENT_BUS.addListener(this::onScreenCharacterTyped);
        NeoForge.EVENT_BUS.addListener(this::onScreenRender);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new StepUpConfigScreen(parent));
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        stepChanger.registerKeyMappings(event);
    }

    private void onClientTickPre(ClientTickEvent.Pre event) {
        repairNeoForgeModListScreen(Minecraft.getInstance().screen);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        stepChanger.handleClientTick(Minecraft.getInstance());
    }

    private void onClientPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        stepChanger.handleServerJoin(resolveServerKey(Minecraft.getInstance().getCurrentServer()));
    }

    private void onClientPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        stepChanger.handleDisconnect();
    }

    private void onScreenInit(ScreenEvent.Init.Post event) {
        repairNeoForgeModListScreen(event.getScreen());
        stepChanger.updateControlsAutoJumpLabel(Minecraft.getInstance(), event.getScreen());
    }

    private void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Post event) {
        repairNeoForgeModListScreen(event.getScreen());
        stepChanger.handleControlsAutoJumpClick(
                Minecraft.getInstance(),
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getButton(),
                event.wasClickHandled()
        );
    }

    private void onScreenKeyPressed(ScreenEvent.KeyPressed.Post event) {
        repairNeoForgeModListScreen(event.getScreen());
    }

    private void onScreenCharacterTyped(ScreenEvent.CharacterTyped.Post event) {
        repairNeoForgeModListScreen(event.getScreen());
    }

    private void onScreenRender(ScreenEvent.Render.Pre event) {
        repairNeoForgeModListScreen(event.getScreen());
        stepChanger.updateControlsAutoJumpLabel(Minecraft.getInstance(), event.getScreen());
    }

    private void repairNeoForgeModListScreen(Screen screen) {
        if (screen == null || !NEOFORGE_MOD_LIST_SCREEN_CLASS.equals(screen.getClass().getName())) {
            return;
        }

        try {
            boolean sorted = (boolean) getFieldValue(screen, "sorted");
            Object search = getFieldValue(screen, "search");
            String searchValue = search == null ? "" : (String) search.getClass().getMethod("getValue").invoke(search);
            Object lastFilterValue = getFieldValue(screen, "lastFilterText");
            boolean needsReload = !sorted || !searchValue.equals(lastFilterValue);

            if (needsReload) {
                invokeMethod(screen, "reloadMods");
                makeModListMutable(screen);
                sortModList(screen);
                invokeModListMethod(screen, "refreshList");
                invokeMethod(screen, "updateCache");
                setFieldValue(screen, "sorted", true);
                return;
            }

            makeModListMutable(screen);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            if (!warnedAboutModListPatch) {
                warnedAboutModListPatch = true;
                StepUpNeoForge.LOGGER.debug("Could not patch NeoForge ModListScreen list mutability.", exception);
            }
        }
    }

    private static void makeModListMutable(Object screen) throws ReflectiveOperationException {
        Object value = getFieldValue(screen, "mods");
        if (!(value instanceof List<?> mods) || mods instanceof ArrayList<?>) {
            return;
        }

        setFieldValue(screen, "mods", new ArrayList<>(mods));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void sortModList(Object screen) throws ReflectiveOperationException {
        Object mods = getFieldValue(screen, "mods");
        Object sortType = getFieldValue(screen, "sortType");
        if (mods instanceof List<?> list && sortType instanceof Comparator<?> comparator) {
            ((List) list).sort((Comparator) comparator);
        }
    }

    private static void invokeModListMethod(Object screen, String methodName) throws ReflectiveOperationException {
        Object modList = getFieldValue(screen, "modList");
        if (modList != null) {
            invokeMethod(modList, methodName);
        }
    }

    private static Object getFieldValue(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setFieldValue(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object invokeMethod(Object target, String methodName) throws ReflectiveOperationException {
        Method method = findMethod(target.getClass(), methodName);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private static Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    private static Method findMethod(Class<?> type, String methodName) throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchMethodException(methodName);
    }

    private static String resolveServerKey(ServerData serverInfo) {
        if (serverInfo == null) {
            return StepUpConfig.LOCAL_SERVER_KEY;
        }

        if (serverInfo.ip != null && !serverInfo.ip.isBlank()) {
            return serverInfo.ip;
        }

        if (serverInfo.name != null && !serverInfo.name.isBlank()) {
            return serverInfo.name;
        }

        return "unknown-server";
    }
}
