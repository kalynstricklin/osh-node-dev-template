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

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.data.DataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.swe.helper.GeoPosHelper;

/**
 * Output specification and provider for {@link WiiSensor}.
 *
 * @author your_name
 * @since date
 */
public class WiiOutput extends AbstractSensorOutput<WiiSensor>  {

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

    boolean d_left;
    boolean d_right;
    boolean d_up;
    boolean d_down;
    boolean power;
    boolean home;
    boolean button_a;
    boolean button_b;
    boolean button_minus;
    boolean button_plus;
    boolean button_two;
    boolean button_one;

    private int[] accel_data;

    /**
     * Constructor
     *
     * @param parentWiiSensor Sensor driver providing this output
     */
    WiiOutput(WiiSensor parentWiiSensor) {

        super(SENSOR_OUTPUT_NAME, parentWiiSensor);

        logger.debug("Wii Output created");
    }

    /**
     * Initializes the data structure for the output, defining the fields, their ordering,
     * and data types.
     */
    void doInit() {

        logger.debug("Initializing Wii Output");

        // Get an instance of SWE Factory suitable to build components
        GeoPosHelper sweFactory = new GeoPosHelper();

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
                                    .value(false)
                                    .definition("firorward"))
                            .addField("D_PAD_DOWN", sweFactory.createBoolean()
                                    .value(false)
                                    .label("D-Pad down button move backwards"))
                                .definition("firsbyte")
                            .addField("D_PAD_LEFT", sweFactory.createBoolean()
                                    .label("D-Pad left button")
                                    .value(false)
                                    .definition("firstleft"))
                            .addField("D_PAD_RIGHT", sweFactory.createBoolean()
                                    .label("D-Pad right button :: turns right")
                                    .value(false)
                                    .definition("firstbyte")))
                        .addField("POWER_BUTTON", sweFactory.createBoolean()
                                .label("Power button pressed")
                                .value(false))
                        .addField("HOME_BUTTON", sweFactory.createBoolean()
                                .value(false)
                                .label("Home button pressed")
                                .definition("secondbyte"))
                        .addField("BUTTON_ONE", sweFactory.createBoolean()
                                .value(false)
                                .label("Button One pressed")
                                .definition("secondbyte"))
                        .addField("BUTTON_TWO", sweFactory.createBoolean()
                                .value(false)
                                .label("Button Two pressed")
                                .definition("secondbyte"))
                        .addField("BUTTON_A", sweFactory.createBoolean()
                                .value(false)
                                .label("Button A pressed")
                                .definition("secondbyte"))
                        .addField("BUTTON_B", sweFactory.createBoolean()
                                .value(false)
                                .label("Button B pressed")
                                .definition("secondbyte"))
                        .addField("PLUS_BUTTON", sweFactory.createBoolean()
                                .value(false)
                                .label("Plus button pressed")
                                .definition("firstbyte"))
                        .addField("MINUS_BUTTON", sweFactory.createBoolean()
                                .value(false)
                                .label("Minus button pressed")
                                .definition("secondbyte"))
                        .addField("Motion_Accelerometer", sweFactory.createUnitVectorXYZ()
                                .definition("Measu")
                                .label("Accelerometer"))
                        //https://wiibrew.org/wiki/Wiimote#Core_Buttons
                        .addField("Tilt_Accelerometer", sweFactory.createRecord()
                                .label("Motion")
                                .addField("UP", sweFactory.createUnitVectorXYZ()
                                        .label("accelerometer up"))
                                .addField("LEFT", sweFactory.createUnitVectorXYZ()
                                        .label("accelerometer left"))
                                .addField("DOWN", sweFactory.createUnitVectorXYZ()
                                        .label("accelerometer down"))
                                .addField("RIGHT", sweFactory.createUnitVectorXYZ()
                                        .label("accelerometer right"))
                                .addField("FORWARD", sweFactory.createAccelerationVector("m/s"))
                                .addField("BACKWARD", sweFactory.createAccelerationVector("m/s"))) //TODO: put uom codes

                        .addField("Gyroscope", sweFactory.createRecord()
                                .label("Rotation and Orientation")
                                .description("")
                                .addField("PITCH_UP", sweFactory.createQuantity())
                                .addField("PITCH_DOWN", sweFactory.createQuantity())
                                .addField("PITCH_ROLL_LEFT", sweFactory.createQuantity()) //roll is arctan2(z,x)
                                .addField("PITCH_ROLL_RIGHT",sweFactory.createQuantity())
                                .addField("PITCH_YAW_LEFT", sweFactory.createQuantity())  //pitch arctan2(z,y)
                                .addField("PITCH_YAW_RIGHT",sweFactory.createQuantity()))

                        .addField("Rumble_Motor", sweFactory.createRecord()) //0x10 RR -> setting RR to 1 enables rumble, where 0 disables it
                        .addField("Battery_Status", sweFactory.createRecord()
                                .label("Battery Level")
                                .definition("Theremote"))
                        .addField("Speaker", sweFactory.createRecord()) //0x14 used to enable/disable speaker setting bit 2 will enable and clearing will disable & 0x19 is used to mute/unmute
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
    public void newMessage(){
        DataBlock dataBlock;
        if (latestRecord == null) {

            dataBlock = dataStruct.createDataBlock();

        } else {

            dataBlock = latestRecord.renew();
        }


        // TODO: Populate data block
        dataBlock.setDoubleValue(0, System.currentTimeMillis()/1000.0);

        int pressed;
        boolean isPressed;
        //TODO ADD DATA BLOCK!!!!!!!!!!!!!!!



        latestRecord = dataBlock;

        latestRecordTime = System.currentTimeMillis();

        eventHandler.publish(new DataEvent(latestRecordTime, WiiOutput.this, dataBlock));


    }
    // data will be [0-1] for buttons [2-4] for accel data
    public void readButtonData(byte[] data){
        if (data == null || data.length != 2){

        }
        d_left = (data[0] & 0x01) == 0x01;
        d_right = (data[0] & 0x02) == 0x02;
        d_down = (data[0] & 0x04) == 0x04;
        d_up = (data[0] & 0x08) == 0x08;
        button_plus = (data[0] & 0x10) == 0x10;
        button_two = (data[1] & 0x01) == 0x01;
        button_one = (data[1] & 0x02) == 0x02;
        button_b = (data[1] & 0x04) == 0x04;
        button_a = (data[1] & 0x08) == 0x08;
        button_minus = (data[1] & 0x10) == 0x10;
        home = (data[1] & 0x80) == 0x80;
    }

    //read raw accelerometer data
    public void readAccelData(byte[] data){
//        if (data == null || data.length !=5){
//
//        }

    }

    //read raw battery level
    public void readBatteryLevel(byte[] data){}


    public void processData(){
        
    }

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
//    public void onButtonsEvent(WiiRemoteButtonsEvent wbe){
//        if(wbe.isButtonAPressed()){
//
//        }
//        if(wbe.isButtonBPressed()){
//
//        }
//        if(wbe.isButtonOnePressed()){
//
//        }
//        if(wbe.isButtonTwoPressed()){
//
//        }
//        if(wbe.isButtonPowerPressed()){
//
//        }
//        if(wbe.isButtonHomePressed()){
//
//        }
//        if(wbe.isButtonPlusPressed()){
//
//        }
//        if(wbe.isButtonMinusPressed()){
//
//        }
//        if(wbe.isButtonDownPressed()){
//
//        }
//        if(wbe.isButtonLeftPressed()){
//
//        }
//        if(wbe.isButtonRightPressed()){
//
//        }
//        if(wbe.isButtonUpPressed()){
//
//        }
//        if(wbe.isButtonLeftPressed()){
//
//        }
//    }

//    @Override
//    public void run() {
//
//        boolean processSets = true;
//
//        long lastSetTimeMillis = System.currentTimeMillis();
//
//        try {
//
//            while (processSets) {
//
//                DataBlock dataBlock;
//                if (latestRecord == null) {
//
//                    dataBlock = dataStruct.createDataBlock();
//
//                } else {
//
//                    dataBlock = latestRecord.renew();
//                }
//
//                synchronized (histogramLock) {
//
//                    int setIndex = setCount % MAX_NUM_TIMING_SAMPLES;
//
//                    // Get a sampling time for latest set based on previous set sampling time
//                    timingHistogram[setIndex] = System.currentTimeMillis() - lastSetTimeMillis;
//
//                    // Set latest sampling time to now
//                    lastSetTimeMillis = timingHistogram[setIndex];
//                }
//
//                ++setCount;
//
//                double timestamp = System.currentTimeMillis() / 1000d;
//
//                // TODO: Populate data block
//                dataBlock.setDoubleValue(0, timestamp);
//                dataBlock.setStringValue(1, "Your data here");
//
//                latestRecord = dataBlock;
//
//                latestRecordTime = System.currentTimeMillis();
//
//                eventHandler.publish(new DataEvent(latestRecordTime, WiiOutput.this, dataBlock));
//
//                synchronized (processingLock) {
//
//                    processSets = !stopProcessing;
//                }
//            }
//
//        } catch (Exception e) {
//
//            logger.error("Error in worker thread: {}", Thread.currentThread().getName(), e);
//
//        } finally {
//
//            // Reset the flag so that when driver is restarted loop thread continues
//            // until doStop called on the output again
//            stopProcessing = false;
//
//            logger.debug("Terminating worker thread: {}", this.name);
//        }
//    }
}
