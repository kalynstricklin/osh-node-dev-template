package com.sample.impl.sensor.wii;

import motej.Mote;
import motej.event.AccelerometerListener;
import motej.event.CoreButtonEvent;
import motej.event.CoreButtonListener;
import motej.request.ReportModeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EventListener;


public class WiiRemote implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(WiiRemote.class);
    Mote mote;
    AccelerometerListener accelerometerListener;
    CoreButtonListener buttonListener;
    private String button = "none";
    public enum Button {BUTTON_A, BUTTON_B, BUTTON_HOME, BUTTON_MINUS, BUTTON_ONE, BUTTON_PLUS, BUTTON_POWER, BUTTON_TWO, D_PAD_DOWN, D_PAD_LEFT, D_PAD_RIGHT, D_PAD_UP};
    private int[] motion = {0, 0, 0};


    public WiiRemote(Mote mote) {
        this.mote = mote;
//        accelerometerListener = new AccelerometerListener() {
//            @Override
//            public void accelerometerChanged(AccelerometerEvent accelerometerEvent) {
//                motion[0] = accelerometerEvent.getX();
//                motion[1] = accelerometerEvent.getX();
//                motion[2] = accelerometerEvent.getZ();
//                System.out.println( "x: " + motion[0] + "y: " + motion[1] + "z: " + motion[2]);
//            }
//        };
        buttonListener = new CoreButtonListener() {
            @Override
            public void buttonPressed(CoreButtonEvent coreButtonEvent) {
//                if(coreButtonEvent.isButtonAPressed() && coreButtonEvent.isButtonBPressed()){
//                    button = "AB";
//                }
//                else if (coreButtonEvent.isButtonAPressed()){
//                    coreButtonEvent.getSource();
//                    button = "BUTTON_A";
//                }
//                else if (coreButtonEvent.isButtonBPressed()){
//                    button = "BUTTON_B";
//                }
//                else if (coreButtonEvent.isButtonPlusPressed()){
//                    button = "BUTTON_PLUS";
//                }
//                else if (coreButtonEvent.isButtonMinusPressed()){
//                    button = "BUTTON_MINUS";
//                }
//                else if (coreButtonEvent.isButtonHomePressed()){
//                    button = "BUTTON_HOME";
//                }
//                else if (coreButtonEvent.isDPadLeftPressed()){
//                    button = "D_PAD_LEFT";
//                }
//                else if (coreButtonEvent.isDPadRightPressed()){
//                    button = "D_PAD_RIGHT";
//                }
//                else if (coreButtonEvent.isDPadUpPressed()){
//                    button = "D_PAD_UP";
//                }
//                else if (coreButtonEvent.isDPadDownPressed()){
//                    button = "D_PAD_DOWN";
//                }
//                else if (coreButtonEvent.isButtonOnePressed()){
//                    button = "BUTTON_ONE";
//                }
//                else if (coreButtonEvent.isButtonTwoPressed()){
//                    button = "BUTTON_TWO";
//                }
//                else if (coreButtonEvent.isNoButtonPressed()){
//                    button = "NONE";
//                }
            }
        };
        if (mote != null){
            mote.addAccelerometerListener(accelerometerListener);
            mote.setReportMode(ReportModeRequest.DATA_REPORT_0x31);
            mote.addCoreButtonListener(buttonListener);
        }

    }

    public String getButton(){
        return button;
    }
    public boolean isButtonPressed(Button button, CoreButtonEvent event){
        switch(button){
            case D_PAD_UP:
               // buttonListener.buttonPressed(new CoreButtonEvent(mote, 1));
//                boolean dPadUpPressed = event.isDPadUpPressed();
                return event.isDPadUpPressed();

            case D_PAD_DOWN:
//                return buttonListener.buttonPressed().isDPadDownPressed();
//                boolean dPadDownPressed = event.isDPadDownPressed();
                return event.isDPadDownPressed();

            case D_PAD_RIGHT:
//                return buttonListener.buttonPressed().isDPadRightPressed();
                return event.isDPadRightPressed();

            case D_PAD_LEFT:
//                return buttonListener.buttonPressed().isDPadLeftPressed();

                return event.isDPadLeftPressed();
            case BUTTON_A:
//                return buttonListener.buttonPressed().isButtonAPressed();
                return event.isButtonAPressed();
            case BUTTON_B:
//                return buttonListener.buttonPressed().isButtonBPressed();
                return event.isButtonBPressed();

            case BUTTON_ONE:
//                return buttonListener.buttonPressed().isButtonOnePressed();
                return event.isButtonOnePressed();

            case BUTTON_HOME:
//                return buttonListener.buttonPressed().isButtonHomePressed();
                return event.isButtonHomePressed();

            case BUTTON_TWO:
//                return buttonListener.buttonPressed().isButtonTwoPressed();
                return event.isButtonTwoPressed();

            case BUTTON_PLUS:
//                return buttonListener.buttonPressed().isButtonPlusPressed();
                return event.isButtonPlusPressed();

            case BUTTON_MINUS:
//                return buttonListener.buttonPressed().isButtonMinusPressed();
                return event.isButtonMinusPressed();
            default:
                return false;
        }
    }


    //yaw: A yaw is a counterclockwise rotation of the alpha about the z-axis
    //pitch: A pitch is a counterclockwise rotation of the beta about the  y-axis
    //roll: A roll is a counterclockwise rotation of gamma about the x-axis

//    Calculate the pitch angle (in radians) as: **pitch = atan2(-R[2][0], sqrt(R[0][0]R[0][0] + R[1][0]R[1][0]));
//
//    Calculate the yaw angle (in radians) as: yaw = atan2(R[1][0]/cos(pitch), R[0][0]/cos(pitch));
//
//    Calculate the roll angle (in radians) as: roll = atan2(R2/cos(pitch), R[2][2]/cos(pitch));


}
