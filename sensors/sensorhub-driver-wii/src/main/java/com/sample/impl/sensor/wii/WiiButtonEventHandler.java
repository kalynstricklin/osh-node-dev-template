package com.sample.impl.sensor.wii;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiiremotej.WiiRemote;
import wiiremotej.event.WRButtonEvent;
import wiiremotej.event.WiiRemoteAdapter;

public class WiiButtonEventHandler extends WiiRemoteAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WiiButtonEventHandler.class);
    WiiRemote wiiRemote;
    public WiiButtonEventHandler(){

    }
    @Override
    public void buttonInputReceived(WRButtonEvent event) {
        super.buttonInputReceived(event);
        if (event.wasPressed(WRButtonEvent.TWO)) {
            logger.debug("Button 2 was pressed");
            System.out.println("2");
        }
        else if (event.wasPressed(WRButtonEvent.ONE)) {
            logger.debug("Button 1 was pressed");
            System.out.println("1");
        }
        else if (event.wasPressed(WRButtonEvent.B)) {
            logger.debug("Button B was pressed-> the trigger button");
            System.out.println("B");
        }
        else if (event.wasPressed(WRButtonEvent.A)) {
            logger.debug("Button A was pressed");
            System.out.println("A");
        }
        else if (event.wasPressed(WRButtonEvent.MINUS)) {
            logger.debug("Minus Button was pressed");
            System.out.println("Minus");
        }
        else if (event.wasPressed(WRButtonEvent.PLUS)) {
            logger.debug("Plus Button was pressed");
            System.out.println("Plus");
        }
        else if (event.wasPressed(WRButtonEvent.LEFT)) {
            logger.debug("Left Button was pressed");
            System.out.println("Left");
        }
        else if (event.wasPressed(WRButtonEvent.RIGHT)) {
            logger.debug("Right Button was pressed");
            System.out.println("Right");
        }
        else if (event.wasPressed(WRButtonEvent.DOWN)) {
            logger.debug("Down Button was pressed");
            System.out.println("Down");
        }
        else if (event.wasPressed(WRButtonEvent.UP)) {
            logger.debug("Up Button was pressed");
            System.out.println("Up");
        }
        else if (event.wasPressed(WRButtonEvent.HOME)) {
            if (null != wiiRemote && wiiRemote.isConnected()) {
                logger.debug("Home Button was pressed: Disconnecting Wii");
                wiiRemote.disconnect();
            }
            System.exit(0);
        }
    }

}
