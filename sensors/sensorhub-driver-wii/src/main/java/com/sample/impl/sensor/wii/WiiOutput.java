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
import motej.event.CoreButtonEvent;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.data.DataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.data.AbstractDataBlock;
import org.vast.data.DataBlockMixed;
import org.vast.swe.SWEHelper;



/**
 * Output specification and provider for {@link WiiDriver}.
 *
 * @author your_name
 * @since date
 */
public class WiiOutput extends AbstractSensorOutput<WiiDriver> implements Runnable{
    private static final String SENSOR_OUTPUT_NAME = "WII_REMOTE_SENSOR";
    private static final String SENSOR_OUTPUT_LABEL = "WII_SENSOR";
    private static final String SENSOR_OUTPUT_DESCRIPTION = "Driver for Wii remote outputting control inputs";
    private static final Logger logger = LoggerFactory.getLogger(WiiOutput.class);
    private DataRecord dataStruct;
    private DataEncoding dataEncoding;
    private Boolean stopProcessing = false;
    private final Object processingLock = new Object();
    private static final int MAX_NUM_TIMING_SAMPLES = 10;
    private int setCount = 0;
    private final long[] timingHistogram = new long[MAX_NUM_TIMING_SAMPLES];
    private final Object histogramLock = new Object();
    private Thread worker;

    WiiRemote wiiRemote;

    /**
     * Constructor
     *
     * @param parent Sensor driver providing this output
     */
    WiiOutput(WiiDriver parent) {

        super(SENSOR_OUTPUT_NAME, parent);

        logger.debug("Wii Output created");
    }

    /**
     * Initializes the data structure for the output, defining the fields, their ordering,
     * and data types.
     */
    void doInit() {

        logger.debug("Initializing Wii Output");

        // Get an instance of SWE Factory suitable to build components
        SWEHelper sweFactory = new SWEHelper();




        // TODO: Create data record description
        dataStruct = sweFactory.createRecord()
                .name(SENSOR_OUTPUT_NAME)
                .label(SENSOR_OUTPUT_LABEL)
                .description(SENSOR_OUTPUT_DESCRIPTION)
                .addField("sampleTime", sweFactory.createTime()
                        .asSamplingTimeIsoUTC()
                        .label("Sample Time")
                        .description("Time of data collection"))
                .addField("Wii Remote Controls", sweFactory.createRecord()
                        .addField("D_PAD",sweFactory.createRecord()
                            .addField("D_PAD_UP",sweFactory.createBoolean()
                                    .label("D-Pad Up Button")
                                    .value(false))
                            .addField("D_PAD_DOWN", sweFactory.createBoolean()
                                    .value(false)
                                    .label("D-Pad Down button"))
                            .addField("D_PAD_LEFT", sweFactory.createBoolean()
                                    .label("D-Pad left button")
                                    .value(false))
                            .addField("D_PAD_RIGHT", sweFactory.createBoolean()
                                    .label("D-Pad right")
                                    .value(false)))
                        .addField("POWER_BUTTON", sweFactory.createBoolean()
                                .label("Power button")
                                .description("Disconnects bluetooth when pressed")
                                .value(false))
                        .addField("HOME_BUTTON", sweFactory.createBoolean()
                                .value(false)
                                .label("Home Button"))
                        .addField("BUTTON_ONE", sweFactory.createBoolean()
                                .value(false)
                                .label("Button One"))
                        .addField("BUTTON_TWO", sweFactory.createBoolean()
                                .value(false)
                                .label("Button Two"))
                        .addField("BUTTON_A", sweFactory.createBoolean()
                                .value(false)
                                .label("Button A"))
                        .addField("BUTTON_B", sweFactory.createBoolean()
                                .value(false)
                                .label("Button B"))
                        .addField("PLUS_BUTTON", sweFactory.createBoolean()
                                .value(false)
                                .label("Plus button"))
                        .addField("MINUS_BUTTON", sweFactory.createBoolean()
                                .value(false)
                                .label("Minus button"))
                        .addField("Motion_Accelerometer", sweFactory.createVector()
                                .label("Accelerometer"))
                        //https://wiibrew.org/wiki/Wiimote#Core_Buttons
//                        .addField("Tilt_Accelerometer", sweFactory.createRecord()
//                                .label("Motion")
//                                .addField("UP", sweFactory.createVector()
//                                        .label("accelerometer rotated up"))
//                                .addField("LEFT", sweFactory.createVector()
//                                        .label("accelerometer rotated left"))
//                                .addField("DOWN", sweFactory.createVector()
//                                        .label("accelerometer rotated down"))
//                                .addField("RIGHT", sweFactory.createVector()
//                                        .label("accelerometer rotated right"))
//                                .addField("FORWARD", sweFactory.createVector()
//                                        .label("accelerometer pushing forward"))
//                                .addField("BACKWARD", sweFactory.createVector()
//                                        .label("accelerometer pulling backwards")))
                        .addField("Gyroscope", sweFactory.createRecord()
                                .label("Rotation and Orientation")
                                .description("Measures orientation and angular velocity")
                                .addField("PITCH_UP", sweFactory.createVector())
                                .addField("PITCH_DOWN", sweFactory.createVector())
                                .addField("ROLL_LEFT", sweFactory.createVector()) //roll is arctan2(z,x)
                                .addField("ROLL_RIGHT",sweFactory.createVector())
                                .addField("YAW_LEFT", sweFactory.createVector())  //pitch arctan2(z,y)
                                .addField("YAW_RIGHT",sweFactory.createVector()))
//                        .addField("Rumble_Motor", sweFactory.createRecord()) //0x10 RR -> setting RR to 1 enables rumble, where 0 disables it
                        .addField("Battery_Status", sweFactory.createRecord()
                                .label("Battery Level")
                                .description("Status of the battery level on the wii remote, given by the led lights on the remote"))
//                        .addField("Speaker", sweFactory.createRecord()) //0x14 used to enable/disable speaker setting bit 2 will enable and clearing will disable & 0x19 is used to mute/unmute
                            //0x18 is used to send speaker data 1-20 bytes can be sent at once
                )
                .build();

        dataEncoding = sweFactory.newTextEncoding(",", "\n");


        logger.debug("Initializing Wii Output Complete");
    }

    /**
     * Begins processing data for output
     */
    public void doStart() {

        // Instantiate a new worker thread
        worker = new Thread(this.name);

        // TODO: Perform other startup
        logger.info("Starting worker thread: {}", worker.getName());

        // Start the worker thread
        worker.start();
    }



    // data will be [0-1] for buttons [2-4] for accel data
//    public void readButtonData(byte[] data){
//        if (data == null || data.length != 2){
//
//        }
//        d_left = (data[0] & 0x01) == 0x01;
//        d_right = (data[0] & 0x02) == 0x02;
//        d_down = (data[0] & 0x04) == 0x04;
//        d_up = (data[0] & 0x08) == 0x08;
//        button_plus = (data[0] & 0x10) == 0x10;
//        button_two = (data[1] & 0x01) == 0x01;
//        button_one = (data[1] & 0x02) == 0x02;
//        button_b = (data[1] & 0x04) == 0x04;
//        button_a = (data[1] & 0x08) == 0x08;
//        button_minus = (data[1] & 0x10) == 0x10;
//        home = (data[1] & 0x80) == 0x80;
//    }
//
//    //read raw accelerometer data
//    public void readAccelData(byte[] data){
//
//    }
//
//    //read raw battery level
//    public void readBatteryLevel(byte[] data){}
//
//
//    public void processData(){
//
//    }

    /**
     * Terminates processing data for output
     */
    public void doStop() {

        synchronized (processingLock) {

            stopProcessing = true;
        }

        // TODO: Perform other shutdown procedures
    }

    /**
     * Check to validate data processing is still running
     *
     * @return true if worker thread is active, false otherwise
     */
    public boolean isAlive() {

        return worker.isAlive();
    }

    @Override
    public DataComponent getRecordDescription() {

        return dataStruct;
    }

    @Override
    public DataEncoding getRecommendedEncoding() {

        return dataEncoding;
    }

    @Override
    public double getAverageSamplingPeriod() {

        long accumulator = 0;

        synchronized (histogramLock) {

            for (int idx = 0; idx < MAX_NUM_TIMING_SAMPLES; ++idx) {

                accumulator += timingHistogram[idx];
            }
        }

        return accumulator / (double) MAX_NUM_TIMING_SAMPLES;
    }

    @Override
    public void run() {

        boolean processSets = true;

        long lastSetTimeMillis = System.currentTimeMillis();

        try {

            while (processSets) {

                DataBlock dataBlock;
                if (latestRecord == null) {

                    dataBlock = dataStruct.createDataBlock();

                } else {

                    dataBlock = latestRecord.renew();
                }

                synchronized (histogramLock) {

                    int setIndex = setCount % MAX_NUM_TIMING_SAMPLES;

                    // Get a sampling time for latest set based on previous set sampling time
                    timingHistogram[setIndex] = System.currentTimeMillis() - lastSetTimeMillis;

                    // Set latest sampling time to now
                    lastSetTimeMillis = timingHistogram[setIndex];
                }

                ++setCount;

                double timestamp = System.currentTimeMillis() / 1000;


                // TODO: Populate data block
                dataBlock.setDoubleValue(0, timestamp);

                //set underlying wii remote button data
                AbstractDataBlock wiiRemoteData = ((DataBlockMixed) dataBlock).getUnderlyingObject()[0];
                AbstractDataBlock buttonData = ((DataBlockMixed) dataBlock).getUnderlyingObject()[1];
//                AbstractDataBlock dPadData = ((DataBlockMixed) dataBlock).getUnderlyingObject()[2];
                AbstractDataBlock accelData = ((DataBlockMixed) dataBlock).getUnderlyingObject()[3];
                AbstractDataBlock gyroData = ((DataBlockMixed) dataBlock).getUnderlyingObject()[4];

//
//                buttonData.setBooleanValue(0,wiiRemote.isButtonPressed(WiiRemote.Button.D_PAD_UP));  // d pad up
//                buttonData.setBooleanValue(1,wiiRemote.isButtonPressed(WiiRemote.Button.D_PAD_DOWN) ); // d pad down
//                buttonData.setBooleanValue(2,wiiRemote.isButtonPressed(WiiRemote.Button.D_PAD_LEFT) ); // d pad left
//                buttonData.setBooleanValue(3,wiiRemote.isButtonPressed(WiiRemote.Button.D_PAD_RIGHT)); //d pad right
//                buttonData.setBooleanValue(4,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_POWER) ); //power
//                buttonData.setBooleanValue(5,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_HOME) ); //home
//                buttonData.setBooleanValue(6,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_ONE) ); //1
//                buttonData.setBooleanValue(7,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_TWO) ); //2
//                buttonData.setBooleanValue(8,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_A) ); //A
//                buttonData.setBooleanValue(9,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_B) ); // B
//                buttonData.setBooleanValue(10,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_PLUS)); //plus
//                buttonData.setBooleanValue(11,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_MINUS)); //minus
                Mote mote;
                mote = wiiRemote.mote;
                CoreButtonEvent event = new CoreButtonEvent(mote, 1);
                buttonData.setBooleanValue(0,wiiRemote.isButtonPressed(WiiRemote.Button.D_PAD_UP, event));  // d pad up
                buttonData.setBooleanValue(1,wiiRemote.isButtonPressed(WiiRemote.Button.D_PAD_DOWN, event)); // d pad down
                buttonData.setBooleanValue(2,wiiRemote.isButtonPressed(WiiRemote.Button.D_PAD_LEFT, event)); // d pad left
                buttonData.setBooleanValue(3,wiiRemote.isButtonPressed(WiiRemote.Button.D_PAD_RIGHT, event)); //d pad right
                buttonData.setBooleanValue(4,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_POWER, event)); //power
                buttonData.setBooleanValue(5,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_HOME, event)); //home
                buttonData.setBooleanValue(6,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_ONE, event)); //1
                buttonData.setBooleanValue(7,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_TWO, event)); //2
                buttonData.setBooleanValue(8,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_A, event)); //A
                buttonData.setBooleanValue(9,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_B,event)); // B
                buttonData.setBooleanValue(10,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_PLUS, event)); //plus
                buttonData.setBooleanValue(11,wiiRemote.isButtonPressed(WiiRemote.Button.BUTTON_MINUS, event)); //minus


//                gyroData.setDoubleValue(0, wiiRemote.getPitch()); //pitch up
//                gyroData.setDoubleValue(1, wiiRemote.getPitch()); //pitch down
                gyroData.setDoubleValue(2); //roll left
                gyroData.setDoubleValue(3); //roll right
                gyroData.setDoubleValue(4); //yaw left
                gyroData.setDoubleValue(5); //yaw right


                latestRecord = dataBlock;

                latestRecordTime = System.currentTimeMillis();

                eventHandler.publish(new DataEvent(latestRecordTime, WiiOutput.this, dataBlock));

                synchronized (processingLock) {

                    processSets = !stopProcessing;
                }
            }

        } catch (Exception e) {

            logger.error("Error in worker thread: {}", Thread.currentThread().getName(), e);

        } finally {

            stopProcessing = false;

            logger.debug("Terminating worker thread: {}", this.name);
        }
    }

}
