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


import motej.Mote;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.bluetooth.BlueCoveConfigProperties;

//import edu.wpi.first.wpilibj.GenericHID;

/**
 * Sensor driver providing sensor description, output registration, initialization and shutdown of driver and outputs.
 * TODO: get sensor to connect via ble Bluetooth JSRE 82
 * @author kalynstricklin
 * @since date
 */
public class WiiDriver extends AbstractSensorModule<WiiConfig> {
    private static final Logger logger = LoggerFactory.getLogger(WiiDriver.class);
    WiiOutput output;
    WiiRemoteFinder remoteFinder;
//    WiiRemote[] wiiRemotes;

    Mote mote;
    @Override
    public void doInit() throws SensorHubException {

        super.doInit();

        // Generate identifiers
        generateUniqueID("urn:osh:sensor:", config.serialNumber);
        generateXmlID("WII_REMOTE", config.serialNumber);

//        remoteFinder = new WiiRemoteFinder[1];
//        wiiRemotes = new WiiRemote[1];

        // discover wii remote
        System.setProperty(BlueCoveConfigProperties.PROPERTY_JSR_82_PSM_MINIMUM_OFF, "true");
        remoteFinder = new WiiRemoteFinder();
        remoteFinder.findRemote(); // finding wii remote
        remoteFinder.moteFound(mote); //found wii remote




        // Create and initialize output
        output = new WiiOutput(this);

        addOutput(output, false);

        output.doInit();

        // TODO: Perform other initialization
    }

    @Override
    public void doStart() throws SensorHubException {

        if (null != output) {
            // Allocate necessary resources and start outputs
            output.doStart();
        }
        // TODO: Perform other startup procedures
    }

    @Override
    public void doStop() throws SensorHubException {

        if (null != output) {
            output.doStop();
            //disconnect from wii and set it to null

        }
        // TODO: Perform other shutdown procedures
    }

    @Override
    public boolean isConnected() {
        // Determine if sensor is connected
        return true;
    }

}
