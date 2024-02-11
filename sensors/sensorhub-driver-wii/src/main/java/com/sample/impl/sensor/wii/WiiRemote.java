package com.sample.impl.sensor.wii;


import javafx.util.Pair;
import motej.event.*;
import motej.Mote;
import motej.request.ReportModeRequest;

public class WiiRemote {
    private static WiiRemote instance;
    private Mote mote;
    private final int[] motion = {0,0,0};
    public enum Button {NONE, BUTTON_A, BUTTON_B, BUTTON_HOME, BUTTON_MINUS, BUTTON_ONE, BUTTON_PLUS, BUTTON_TWO, D_PAD_DOWN, D_PAD_LEFT, D_PAD_RIGHT, D_PAD_UP};

    public static synchronized WiiRemote getInstance(){
        if (instance == null){
            instance = new WiiRemote();
        }
        return instance;
    }
    private WiiRemote(){
        mote = PairWii.getInstance().mote;
        if(mote != null){
            mote.addCoreButtonListener(new CoreButtonListener() {
                @Override
                public void buttonPressed(CoreButtonEvent coreButtonEvent) {
                    if (coreButtonEvent.isButtonAPressed()) {
                        System.out.println("BUTTON_A");
                    } else if (coreButtonEvent.isButtonBPressed()) {
                        System.out.println("BUTTON_B");
                    } else if (coreButtonEvent.isButtonPlusPressed()) {
                        System.out.println("BUTTON_PLUS");
                    } else if (coreButtonEvent.isButtonMinusPressed()) {
                        System.out.println("BUTTON_MINUS");
                    } else if (coreButtonEvent.isButtonHomePressed()) {
                        System.out.println("BUTTON_HOME");
                    } else if (coreButtonEvent.isDPadLeftPressed()) {
                        System.out.println("D_PAD_LEFT");
                    } else if (coreButtonEvent.isDPadRightPressed()) {
                        System.out.println("D_PAD_RIGHT");
                    } else if (coreButtonEvent.isDPadUpPressed()) {
                        System.out.println("D_PAD_UP");
                    } else if (coreButtonEvent.isDPadDownPressed()) {
                        System.out.println("D_PAD_DOWN");
                    } else if (coreButtonEvent.isButtonOnePressed()) {
                        System.out.println("BUTTON_ONE");
                    } else if (coreButtonEvent.isButtonTwoPressed()) {
                        System.out.println("BUTTON_TWO");
                    } else if (coreButtonEvent.isNoButtonPressed()) {
                        System.out.println("NONE");
                    }

                }
            });
            mote.addAccelerometerListener(new AccelerometerListener<Mote>() {
                @Override
                public void accelerometerChanged(AccelerometerEvent<Mote> accelerometerEvent) {
                    motion[0] = accelerometerEvent.getX();
                    motion[1] = accelerometerEvent.getY();
                    motion[2] = accelerometerEvent.getZ();

                }
            });
            mote.setReportMode(ReportModeRequest.DATA_REPORT_0x31);
            mote.setReportMode(ReportModeRequest.DATA_REPORT_0x30);
        }

    }

    /**
     * method to check if button is pressed and return the boolean event
     * @param button
     * @return
     */
    public boolean isButtonPressed(Button button) {
        CoreButtonEvent event = new CoreButtonEvent(mote, 0);
        switch (button) {
            case D_PAD_UP:
                return event.isDPadUpPressed();
            case D_PAD_DOWN:
                return event.isDPadDownPressed();
            case D_PAD_RIGHT:
                return event.isDPadRightPressed();
            case D_PAD_LEFT:
                return event.isDPadLeftPressed();
            case NONE:
                return event.isNoButtonPressed();
            case BUTTON_A:
                return event.isButtonAPressed();
            case BUTTON_B:
                return event.isButtonBPressed();
            case BUTTON_ONE:
                return event.isButtonOnePressed();
            case BUTTON_HOME:
                return event.isButtonHomePressed();
            case BUTTON_TWO:
                return event.isButtonTwoPressed();
            case BUTTON_PLUS:
                return event.isButtonPlusPressed();
            case BUTTON_MINUS:
                return event.isButtonMinusPressed();
            default:
                return false;
        }
    }


}


