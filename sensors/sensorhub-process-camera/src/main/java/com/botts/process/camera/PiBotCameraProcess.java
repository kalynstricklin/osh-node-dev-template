/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.camera;

import net.opengis.swe.v20.Category;
import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.Quantity;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.data.AbstractDataBlock;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;

/**
 * Process for performing the switching of colors of the light on pi-bot
 *
 * @author Kalyn Stricklin
 * @since 1.0.0
 */
public class PiBotCameraProcess extends ExecutableProcessImpl {

    public static final OSHProcessInfo INFO = new OSHProcessInfo(
            "pi-bot:camera",
            "Camera PTZ Algorithm",
            "Control for Pibot Camera PTZ",
            PiBotCameraProcess.class);

    protected static final Logger logger = LoggerFactory.getLogger(PiBotCameraProcess.class);
    private Quantity tiltOutput;
    private Quantity panOutput;
    private Quantity tiltDown;
    private Quantity tiltUp;
    private Quantity dpad;
    double newTilt = 0.0;
    double newPan= 0.0;
    boolean isMinusPressed = false;
    boolean isPlusPressed = false;
    private static final double MIN_ANGLE = 20.0;

    private static final double MAX_ANGLE = 160.0;

    public PiBotCameraProcess() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
        SWEHelper swe = new SWEHelper();

        AbstractDataBlock dataBlock;
        // Inputs
        inputData.add("pov", dpad = swe.createQuantity()
                .label("d-pad")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        inputData.add("Minus", tiltDown = swe.createQuantity()
                .label("Decrease Tilt")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        inputData.add("Plus", tiltUp = swe.createQuantity()
                .label("Increase Tilt")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());


        //Outputs
        outputData.add("Pan", panOutput = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("servo-angle"))
                .description("The angle in degrees to which the servo is to turn")
                .uom("deg")
                .dataType(DataType.DOUBLE)
                .build());
        outputData.add("Tilt", tiltOutput = swe.createQuantity()
                .description("The angle in degrees to which the servo is to turn")
                .updatable(true)
                .uom("deg")
                .definition(SWEHelper.getPropertyUri("servo-angle"))
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
            isPlusPressed = tiltUp.getValue() == 1.0;
            isMinusPressed = tiltDown.getValue() == 1.0;

            dpad.getData().getDoubleValue();

            // direction command using remote dpad
            if(dpad.getData().getDoubleValue() == 0.25){
                // forward
//                driveOutput.getData().setStringValue(DriveDirection.FORWARD.name());
//                newDirection = DriveDirection.FORWARD.name();
//                logger.debug(newPan);

            }
            else if (dpad.getData().getDoubleValue() == 0.75){
                // reverse
//                driveOutput.getData().setStringValue(DriveDirection.REVERSE.name());
//                newDirection = DriveDirection.REVERSE.name();
            }
            else if(dpad.getData().getDoubleValue() == 1.0){
                //left
//                driveOutput.getData().setStringValue(DriveDirection.SPIN_LEFT.name());
//                newDirection = DriveDirection.SPIN_LEFT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.5){
                //right
//                driveOutput.getData().setStringValue(DriveDirection.SPIN_RIGHT.name());
//                newDirection = DriveDirection.SPIN_RIGHT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.125){
                // up and left
//                driveOutput.getData().setStringValue(DriveDirection.FORWARD_TURN_LEFT.name());
//                newDirection = DriveDirection.FORWARD_TURN_LEFT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.625){
                // down and right
//                driveOutput.getData().setStringValue(DriveDirection.REVERSE_TURN_RIGHT.name());
//                newDirection = DriveDirection.REVERSE_TURN_RIGHT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.875){
                // down and left
//                driveOutput.getData().setStringValue(DriveDirection.REVERSE_TURN_LEFT.name());
//                newDirection = DriveDirection.REVERSE_TURN_LEFT.name();
            }
            else if(dpad.getData().getDoubleValue() == 0.375){
                // up and right
//                driveOutput.getData().setStringValue(DriveDirection.FORWARD_TURN_RIGHT.name());\
//                newDirection = DriveDirection.FORWARD_TURN_RIGHT.name();
            }
//            else{
//                //stopped!
//                newDirection = DriveDirection.STOP.name();
//            }

            logger.debug("newPan: {}", panOutput.getData().getDoubleValue());

            // check if tilt is updating
            if(isMinusPressed){
                // decrease tilt
//                newSpeed -= 10.0;
            }else if(isPlusPressed){
                //increase tilt
                newTilt += 10.0;
            }
            // check if pan and tilt are within range!
            if(newTilt >= MAX_ANGLE || newPan >= MAX_ANGLE) {
                newTilt= Math.min(newTilt, MAX_ANGLE);
                newPan= Math.min(newPan, MAX_ANGLE);
            }
            else if(newTilt <= MIN_ANGLE|| newPan >= MAX_ANGLE){
                newTilt = MIN_ANGLE;
                newPan = MIN_ANGLE;
            }

            logger.debug("New Tilt & Pan: [{},{}]", newTilt, newPan);
            tiltOutput.getData().setDoubleValue(newTilt);
            panOutput.getData().setDoubleValue(newPan);

        }catch(Exception e){
            throw new ProcessException("error during execution");
        }
    }



    @Override
    public void dispose() {
        super.dispose();
    }
}
