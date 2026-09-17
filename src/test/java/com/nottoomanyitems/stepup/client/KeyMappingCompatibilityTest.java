package com.nottoomanyitems.stepup.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KeyMappingCompatibilityTest {
    @Test
    void defaultsUseMinecraft263KeyboardCodes() {
        KeyMapping mode = StepChanger.createToggleKeyBinding("test.stepup.mode", InputConstants.KEY_J);
        KeyMapping cycle = StepChanger.createToggleKeyBinding("test.stepup.cycle", InputConstants.KEY_K);
        assertEquals("key.keyboard.j", mode.saveString());
        assertEquals("key.keyboard.k", cycle.saveString());
        assertTrue(mode.matches(new KeyEvent(InputConstants.KEY_J, 0, 0)));
        assertFalse(mode.matches(new KeyEvent(InputConstants.KEY_K, 0, 0)));
        assertTrue(cycle.matches(new KeyEvent(InputConstants.KEY_K, 0, 0)));
    }

    @Test
    void rebindingMatchesOnlyTheNewKeyAndSurvivesSerialization() {
        KeyMapping mapping = StepChanger.createToggleKeyBinding("test.stepup.rebind", InputConstants.KEY_J);
        var rebound = InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_P);
        mapping.setKey(rebound);
        KeyMapping.resetMapping();
        // NeoForge click dispatch reads live modifier state and requires a running client.
        assertFalse(mapping.matches(new KeyEvent(InputConstants.KEY_J, 0, 0)));
        assertTrue(mapping.matches(new KeyEvent(InputConstants.KEY_P, 0, 0)));
        assertEquals(rebound, InputConstants.getKey(mapping.saveString()));
        mapping.setKey(mapping.getDefaultKey());
        KeyMapping.resetMapping();
        assertTrue(mapping.isDefault());
    }

    @Test
    void mouseRebindingAndUnboundKeysRemainSupported() {
        KeyMapping mapping = StepChanger.createToggleKeyBinding("test.stepup.mouse", InputConstants.KEY_K);
        var mouse = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_4);
        mapping.setKey(mouse);
        KeyMapping.resetMapping();
        var mouseEvent = new MouseButtonEvent(0, 0, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_4, 0));
        assertTrue(mapping.matchesMouse(mouseEvent));
        assertFalse(mapping.matches(new KeyEvent(InputConstants.KEY_K, 0, 0)));
        assertEquals(mouse, InputConstants.getKey(mapping.saveString()));
        mapping.setKey(InputConstants.UNKNOWN);
        KeyMapping.resetMapping();
        assertTrue(mapping.isUnbound());
        assertFalse(mapping.matchesMouse(mouseEvent));
    }
}