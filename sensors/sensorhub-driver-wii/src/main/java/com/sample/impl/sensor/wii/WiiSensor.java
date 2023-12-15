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
import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteJ;
import wiiremotej.event.WiiDeviceDiscoveredEvent;
import wiiremotej.event.WiiDeviceDiscoveryListener;

import java.io.IOException;
//import wiiusej.Wiimote;
//import wiiusej.wiiusejevents.utils.WiimoteListener;


//import edu.wpi.first.wpilibj.GenericHID;

/**
 * Sensor driver providing sensor description, output registration, initialization and shutdown of driver and outputs.
 * TODO: get sensor to connect via ble Bluetooth JSRE 82
 * @author kalynstricklin
 * @since date
 */
public class WiiSensor extends AbstractSensorModule<WiiConfig> implements WiiDeviceDiscoveryListener {
    private static final Logger logger = LoggerFactory.getLogger(WiiSensor.class);
    private WiiOutput wiiOutput; // osh class
    private WiiRemote remote; //wii jar file class
    WiiRemote wiiRemote;
    String btAddress;
    WiiButtonEventHandler remoteHandler;
   // private Wii wii; // osh class to find wii remote

//    private Wiimote wiimote; //wii use j jar file with native
//    private WiimoteListener wiimoteListener; // wii use j native

    boolean wiiConnected = false;
    boolean isPressed = false;

    @Override
    public void doInit() throws SensorHubException {

        super.doInit();

        // Generate identifiers
        generateUniqueID("WII_REMOTE_SENSOR", config.serialNumber);
        generateXmlID("WII_REMOTE", config.serialNumber);

//        config.bluetoothAddress = btAddress;
        remoteHandler = new WiiButtonEventHandler(); //create new event handler for Wii
        WiiRemoteJ.findRemotes(this,1);
        logger.debug("Finding Wii Remotes");

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
            //disconnect from wii and set it to null
            wiiRemote.disconnect();
            wiiRemote = null;
        }

        // TODO: Perform other shutdown procedures
    }

    @Override
    public boolean isConnected() {
        // Determine if sensor is connected
        return true;
    }

    public boolean isButtonPressed(){
        return true;
    }


    // reads the messages received when collecting data from the wii remote
    public void onMessage(String msg){

    }

    /**
     *  method to discover wii devices and initial set up
     * @param wiiEvt
     */
    @Override
    public void wiiDeviceDiscovered(WiiDeviceDiscoveredEvent wiiEvt) {
        wiiRemote = (WiiRemote) wiiEvt.getWiiDevice();
        wiiRemote.addWiiRemoteListener(remoteHandler);
        try{
            wiiRemote.setAccelerometerEnabled(true);
            wiiRemote.setSpeakerEnabled(true);
            wiiRemote.setLEDIlluminated(0, true);
        } catch (IOException e) {
            if(null != wiiRemote && wiiRemote.isConnected()) {
                wiiRemote.disconnect();
            }

        }
    }

    @Override
    public void findFinished(int i) {

    }

//     while (remote == null){
//            try {
//                remote = WiiRemoteJ.findRemotes(wii, 1);
//                logger.debug("Looking for Wii Remote");
//                try {
//                    remote = WiiRemoteJ.connectToRemote(remote.getBluetoothAddress().toString());
//                    logger.debug("Connecting to Wii remote: "+ remote.getBluetoothAddress().toString());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//            } catch (InterruptedException e) {
//                remote = null;
//                wiiConnected = false;
//                logger.debug("Unable to find a Wii remote: no bluetooth device found");
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//        }

//        try {
//            remote = WiiRemoteJ.connectToRemote(remote.getBluetoothAddress().toString());
//            logger.debug("Connecting to Wii remote: "+ remote.getBluetoothAddress().toString());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
}
