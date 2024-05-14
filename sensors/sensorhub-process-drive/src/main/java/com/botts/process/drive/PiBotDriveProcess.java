/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.drive;

import net.opengis.swe.v20.Category;
import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.Quantity;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.sensorhub.impl.sensor.pibot.drive.DriveDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.data.AbstractDataBlock;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;

import javax.lang.model.element.QualifiedNameable;

/**
 * Process for performing the switching of colors of the light on pi-bot
 *
 * @author Kalyn Stricklin
 * @since 1.0.0
 */
public class PiBotDriveProcess extends ExecutableProcessImpl {

    public static final OSHProcessInfo INFO = new OSHProcessInfo(
            "pi-bot:drive",
            "Drive Algorithm",
            "Control for Pibot DriveSensor",
            PiBotDriveProcess.class);

    protected static final Logger logger = LoggerFactory.getLogger(PiBotDriveProcess.class);
    private Category driveOutput;
    private Quantity speedOutput;
    private Quantity addSpeed;
    private Quantity removeSpeed;
    private Quantity dpad;

//    private Quantity forward;
//    private Quantity backward;
//    private Quantity left;
//    private Quantity right;
    private Quantity remoteMinus;
    private Quantity remotePlus;

    double newSpeed = 0.0;
//    double newDirection = 0.0;
    String newDirection;

//    boolean isLeftPressed = false;
//    boolean isRightPressed = false;
//    boolean isUpPressed = false;
//    boolean isDownPressed = false;
    boolean isMinusPressed = false;
    boolean isPlusPressed = false;

    public PiBotDriveProcess() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
        SWEHelper swe = new SWEHelper();

        AbstractDataBlock dataBlock;
        // Inputs
        inputData.add("pov", dpad = swe.createQuantity()
                .label("d-pad")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        inputData.add("Minus", removeSpeed = swe.createQuantity()
                .label("Decrease Speed")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        inputData.add("Plus", addSpeed = swe.createQuantity()
                .label("Increase Speed")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());


        //Outputs
        outputData.add("Power", speedOutput = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Power"))
                .description("Denotes the percentage of power to apply to the drive actuators")
                .uom("%")
                .dataType(DataType.DOUBLE)
                .build());
        outputData.add("Direction", driveOutput = swe.createCategory()
                        .addAllowedValues(
                                DriveDirection.FORWARD.name(),
                                DriveDirection.FORWARD_TURN_LEFT.name(),
                                DriveDirection.FORWARD_TURN_RIGHT.name(),
                                DriveDirection.SPIN_LEFT.name(),
                                DriveDirection.SPIN_RIGHT.name(),
                                DriveDirection.REVERSE_TURN_LEFT.name(),
                                DriveDirection.REVERSE_TURN_RIGHT.name(),
                                DriveDirection.REVERSE.name(),
                                DriveDirection.STOP.name()
                        )
                .build());
    }

    @Override
    public void init() throws ProcessException {

        logger.debug("Initializing");

        super.init();

        logger.debug("Initialized");
    }

    @Override
    public void execute() throws ProcessException{

        try{
            isPlusPressed = addSpeed.getValue() == 1.0;
            isMinusPressed = removeSpeed.getValue() == 1.0;

            dpad.getData().getDoubleValue();

            // direction command using remote dpad
            if(dpad.getData().getDoubleValue() == 0.25){
                // forward
//                driveOutput.getData().setStringValue(DriveDirection.FORWARD.name());
                newDirection = DriveDirection.FORWARD.name();
                logger.debug(newDirection);

            }
            else if (dpad.getData().getDoubleValue() == 0.75){
                // reverse
//                driveOutput.getData().setStringValue(DriveDirection.REVERSE.name());
                newDirection = DriveDirection.REVERSE.name();
            }
            else if(dpad.getData().getDoubleValue() == 1.0){
                //left
//                driveOutput.getData().setStringValue(DriveDirection.SPIN_LEFT.name());
                newDirection = DriveDirection.SPIN_LEFT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.5){
                //right
//                driveOutput.getData().setStringValue(DriveDirection.SPIN_RIGHT.name());
                newDirection = DriveDirection.SPIN_RIGHT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.125){
                // up and left
//                driveOutput.getData().setStringValue(DriveDirection.FORWARD_TURN_LEFT.name());
                newDirection = DriveDirection.FORWARD_TURN_LEFT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.625){
                // down and right
//                driveOutput.getData().setStringValue(DriveDirection.REVERSE_TURN_RIGHT.name());
                newDirection = DriveDirection.REVERSE_TURN_RIGHT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.875){
                // down and left
//                driveOutput.getData().setStringValue(DriveDirection.REVERSE_TURN_LEFT.name());
                newDirection = DriveDirection.REVERSE_TURN_LEFT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.375){
                // up and right
//                driveOutput.getData().setStringValue(DriveDirection.FORWARD_TURN_RIGHT.name());\
                newDirection = DriveDirection.FORWARD_TURN_RIGHT.name();
            }
//            else{
//                //stopped!
//                newDirection = DriveDirection.STOP.name();
//            }

            logger.debug("new direction: {}", driveOutput.getData().getStringValue());

            // increase the motor power
            if(isMinusPressed){
                // decrease speed
                newSpeed -= 10.0;
            }else if(isPlusPressed){
                //increase speed
                newSpeed += 10.0;
            }
            if(newSpeed >= 100.0){
                newSpeed= Math.min(newSpeed, 100.0d);
            }
            else if(newSpeed <= 0.0){
                newSpeed = 0.0;
            }

            logger.debug("new Speed & direction: [{},{}]", newSpeed, newDirection);
            speedOutput.getData().setDoubleValue(newSpeed);
            driveOutput.getData().setStringValue(newDirection);

        }catch(Exception e){
            throw new ProcessException("error during execution");
        }
    }



    @Override
    public void dispose() {
        super.dispose();
    }
}
