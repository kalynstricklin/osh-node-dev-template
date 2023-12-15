package com.sample.impl.sensor.wii;

import wiiremotej.WiiRemote;


//    Bit	Mask	First Byte	Second Byte
//0	0x01	D-Pad Left	Two
//1	0x02	D-Pad Right	One
//2	0x04	D-Pad Down	B
//3	0x08	D-Pad Up	A
//4	0x10	Plus	Minus
//5	0x20	Other uses	Other uses
//6	0x40	Other uses	Other uses
//7	0x80	Unknown	Home
public enum WiiRemoteButtons {

    D_PAD_LEFT (2, 0x01),
    D_PAD_RIGHT(2, 0x02),
    D_PAD_UP(2, 0x08),
    D_PAD_DOWN(2, 0x04),
    BUTTON_ONE (3, 0x01),
    BUTTON_TWO (3, 0x02),
    BUTTON_A (3, 0x08),
    BUTTON_B (3, 0x04),
    BUTTON_PLUS (2, 0x10),
    BUTTON_MINUS (3, 0x10),
    BUTTON_HOME (3, 0x80),
    BUTTON_POWER (0,0x00),; //todo check byte value for power button

    private final int byteIndex;
    private final int byteVal;

    private WiiRemoteButtons(int byteIndex, int byteVal){
        this.byteIndex = byteIndex;
        this.byteVal = byteVal;
    }


}

//    public static final int D_PAD_LEFT = 0;
//    public static final int D_PAD_RIGHT = 0;
//    public static final int D_PAD_UP = 0;
//    public static final int D_PAD_DOWN = 0;
//    public static final int BUTTON_ONE = 0;
//    public static final int BUTTON_TWO = 0;
//    public static final int BUTTON_A = 0;
//    public static final int BUTTON_B = 0;
//    public static final int BUTTON_PLUS = 0;
//    public static final int BUTTON_MINUS = 0;
//    public static final int BUTTON_HOME = 0;
//    public static final int BUTTON_POWER = 0;


