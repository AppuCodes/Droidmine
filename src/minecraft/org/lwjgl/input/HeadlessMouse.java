package org.lwjgl.input;

public class HeadlessMouse extends Mouse {

    public void pollGLFW(){
    }
    
    public void createEvent(){
    }

    public void create(){
        clipMouseCoordinatesToWindow = true;
        created = true;
        buttonName = new String[8];

        for (int i = 0; i < 8; i++) {
            buttonName[i] = "BUTTON" + i;
            buttonMap.put(buttonName[i], i);
        }
    }
    
    public boolean next(){
        return false;
    }

    public boolean isButtonDown(int button){
        return false;
    }
}
