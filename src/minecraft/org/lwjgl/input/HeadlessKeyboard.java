package org.lwjgl.input;

import java.nio.ByteBuffer;

public class HeadlessKeyboard extends Keyboard {
    public void create() {
        createGLFWKeyMap();
        created = true;
        repeat_enabled = false;
    }

    public void pollGLFW(){
    }

    public boolean next() {
        return false;
    }
}
