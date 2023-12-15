//package com.sample.impl.sensor.wii;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import wiiremotej.WiiRemote;
//import wiiremotej.event.WRButtonEvent;
//import wiiremotej.event.WiiDeviceDiscoveredEvent;
//import wiiremotej.event.WiiDeviceDiscoveryListener;
//import wiiremotej.event.WiiRemoteAdapter;
//
//import java.io.IOException;
//
//public class Wii extends WiiRemoteAdapter implements WiiDeviceDiscoveryListener {
//    private static final Logger logger = LoggerFactory.getLogger(Wii.class);
//    WiiRemote wiiRemote;
//
//    @Override
//    public void wiiDeviceDiscovered(WiiDeviceDiscoveredEvent event) {
//        wiiRemote = (WiiRemote) event.getWiiDevice();
//        try{
//            wiiRemote.setAccelerometerEnabled(true);
//            wiiRemote.setSpeakerEnabled(true);
//            wiiRemote.setLEDIlluminated(0, true);
//        } catch (IOException e) {
//            if(null != wiiRemote && wiiRemote.isConnected()){
//                wiiRemote.disconnect();
//            }
//        }
//        wiiRemote.addWiiRemoteListener(this);
//    }
//
//    @Override
//    public void findFinished(int i) {
//    }
//    @Override
//    public void buttonInputReceived(WRButtonEvent event) {
//        super.buttonInputReceived(event);
//        if (event.wasPressed(WRButtonEvent.TWO)) {
//            logger.debug("Button 2 was pressed");
//            System.out.println("2");
//        }
//        else if (event.wasPressed(WRButtonEvent.ONE)) {
//            logger.debug("Button 1 was pressed");
//            System.out.println("1");
//        }
//        else if (event.wasPressed(WRButtonEvent.B)) {
//            logger.debug("Button B was pressed-> the trigger button");
//            System.out.println("B");
//        }
//        else if (event.wasPressed(WRButtonEvent.A)) {
//            logger.debug("Button A was pressed");
//            System.out.println("A");
//        }
//        else if (event.wasPressed(WRButtonEvent.MINUS)) {
//            logger.debug("Minus Button was pressed");
//            System.out.println("Minus");
//        }
//        else if (event.wasPressed(WRButtonEvent.PLUS)) {
//            logger.debug("Plus Button was pressed");
//            System.out.println("Plus");
//        }
//        else if (event.wasPressed(WRButtonEvent.LEFT)) {
//            logger.debug("Left Button was pressed");
//            System.out.println("Left");
//        }
//        else if (event.wasPressed(WRButtonEvent.RIGHT)) {
//            logger.debug("Right Button was pressed");
//            System.out.println("Right");
//        }
//        else if (event.wasPressed(WRButtonEvent.DOWN)) {
//            logger.debug("Down Button was pressed");
//            System.out.println("Down");
//        }
//        else if (event.wasPressed(WRButtonEvent.UP)) {
//            logger.debug("Up Button was pressed");
//            System.out.println("Up");
//        }
//        else if (event.wasPressed(WRButtonEvent.HOME)) {
//            if (null != wiiRemote && wiiRemote.isConnected()) {
//                logger.debug("Home Button was pressed: Disconnecting Wii");
//                wiiRemote.disconnect();
//            }
//            System.exit(0);
//        }
//    }
//}
