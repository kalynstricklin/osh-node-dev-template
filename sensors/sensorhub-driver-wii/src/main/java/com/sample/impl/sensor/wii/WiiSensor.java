/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2020-2021 Botts Innovative Research, Inc. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/
package com.sample.impl.sensor.wii;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiiremotej.*;
import wiiremotej.event.*;

import java.io.IOException;

/**
 * Sensor driver providing sensor description, output registration, initialization and shutdown of driver and outputs.
 * TODO: get sensor to connect via ble Bluetooth L2CAP protocol
 * @author kalynstricklin
 * @since date
 */
public class WiiSensor extends AbstractSensorModule<WiiConfig> {

    private static final Logger logger = LoggerFactory.getLogger(WiiSensor.class);
    private WiiOutput wiiOutput;
    private WiiRemote remote;

    boolean wiiConnected =false;
    boolean isPressed = false;

    @Override
    public void doInit() throws SensorHubException {

        super.doInit();

        // Generate identifiers
        generateUniqueID("WII_REMOTE_SENSOR", config.serialNumber);
        generateXmlID("WII_REMOTE", config.serialNumber);


        //TODO: set up ble connection with Wii remote/ or connect via usb? ensure this is connected
        remote = null;
        while (remote == null){
            try {
                remote = WiiRemoteJ.findRemote();
                logger.debug("Looking for Wii Remote");
            } catch (InterruptedException e) {
                remote = null;
                wiiConnected = false;
                logger.debug("Unable to find a Wii remote");
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        try {
            remote = WiiRemoteJ.connectToRemote(remote.getBluetoothAddress().toString());
            logger.debug("Connecting to Wii remote: "+ remote.getBluetoothAddress().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Create and initialize output
        wiiOutput = new WiiOutput(this);

        addOutput(wiiOutput, false);

        wiiOutput.doInit();

        // TODO: Perform other initialization
    }

    @Override
    public void doStart() throws SensorHubException {

        if (null != wiiOutput) {
            // Allocate necessary resources and start outputs
            wiiOutput.doStart();
        }
        // TODO: Perform other startup procedures
    }

    @Override
    public void doStop() throws SensorHubException {

        if (null != wiiOutput) {
            wiiOutput.doStop();
        }

        // TODO: Perform other shutdown procedures
    }

    @Override
    public boolean isConnected() {

        // Determine if sensor is connected
        return wiiConnected;
    }

    public boolean isButtonPressed(){
        return isPressed;
    }


}
