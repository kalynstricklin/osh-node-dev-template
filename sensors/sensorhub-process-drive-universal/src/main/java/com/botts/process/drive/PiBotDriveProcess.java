/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.drive;

import net.opengis.swe.v20.Boolean;
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
    private Quantity forwardOutput;
    private Quantity reverseOutput;
    private Quantity leftOutput;
    private Quantity rightOutput;
    private Quantity forwardLeftOutput;
    private Quantity forwardRightOutput;
    private Quantity reverseLeftOutput;
    private Quantity reverseRightOutput;
    private Quantity stopOutput;
    boolean isMinusPressed = false;
    boolean isPlusPressed = false;
    boolean isUpPressed = false;
    boolean isLeftPressed = false;
    boolean isRightPressed = false;
    boolean isDownPressed = false;
    Boolean left;
    Boolean right;
    Boolean forward;
    Boolean reverse;
    Boolean aButton;
    Boolean two;
    Boolean plus;
    Boolean minus;
    Boolean bButton;
    Boolean one;
    double newSpeed = 0.0;

    public PiBotDriveProcess() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
        SWEHelper swe = new SWEHelper();

        inputData.add("buttons", swe.createRecord()
                .addField("forward", forward = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("Forward"))
                        .build())
                .addField("reverse", reverse = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("Reverse"))
                        .build())
                .addField("left", left = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("Left"))
                        .build())
                .addField("right", right = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("Right"))
                        .build())
                .addField("plus", plus = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("IncreaseSpeed"))
                        .build())
                .addField("minus", minus = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("DecreaseSpeed"))
                        .build())
                .addField("one", one = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("1"))
                        .build())
                .addField("two", two = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("2"))
                        .build())
                .addField("a", aButton = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("a"))
                        .build())
                .addField("b", bButton = swe.createBoolean()
                        .definition(SWEHelper.getPropertyUri("b"))
                        .build())
                .build());

        outputData.add("Forward", forwardOutput = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Forward"))
                .label("Forward")
                .dataType(DataType.DOUBLE)
                .uom("%")
                .updatable(true)
                .build());
        outputData.add("Reverse", reverseOutput= swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Reverse"))
                .label("Reverse")
                .uom("%")
                .dataType(DataType.DOUBLE)
                .updatable(true)
                .build());
        outputData.add("SpinLeft", leftOutput =swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("SpinLeft"))
                .label("Spin Left")
                .updatable(true)
                .dataType(DataType.DOUBLE)
                .uom("%")
                .build());
        outputData.add("SpinRight", rightOutput = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("SpinRight"))
                .label("Spin Right")
                .dataType(DataType.DOUBLE)
                .uom("%")
                .updatable(true)
                .build());
//        outputData.add("ReverseRight", reverseRightOutput= swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ReverseRight"))
//                .label("Reverse Turn Right")
//                .uom("%")
//                .updatable(true)
//                .build());
//        outputData.add("ReverseLeft", reverseLeftOutput = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ReverseLeft"))
//                .label("Reverse Turn Left")
//                .updatable(true)
//                .uom("%")
//                .build());
//        outputData.add("ForwardLeft", forwardLeftOutput=swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ForwardLeft"))
//                .label("Forward Turn Left")
//                .updatable(true)
//                .uom("%")
//                .build());
//        outputData.add("ForwardRight",forwardRightOutput= swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ForwardRight"))
//                .label("Forward Turn Right")
//                .uom("%")
//                .updatable(true)
//                .build());
        outputData.add("Stop", stopOutput = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Stop"))
                .label("Stop")
                .updatable(true)
                .dataType(DataType.DOUBLE)
                .uom("%")
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

            isUpPressed = forward.getData().getBooleanValue();
            isDownPressed = reverse.getData().getBooleanValue();
            isRightPressed = right.getData().getBooleanValue();
            isLeftPressed = left.getData().getBooleanValue();
            isMinusPressed = minus.getData().getBooleanValue();
            isPlusPressed = plus.getData().getBooleanValue();

            // increase the motor power
            if(isMinusPressed){
                // decrease speed
                newSpeed -= 10.0;
                logger.debug("decreasing speed");
            }else if(isPlusPressed){
                //increase speed
                newSpeed += 10.0;
            }
            if(newSpeed >= 100.0){
                newSpeed= Math.min(newSpeed, 100.0);
            }
            else if(newSpeed <= 0.0){
                newSpeed = 0.0;
            }
            logger.debug("Speed: {}", newSpeed);

            // direction!!!!!!
            if(isLeftPressed){
                leftOutput.getData().setDoubleValue(newSpeed);
                reverseOutput.getData().setDoubleValue(0.0);
                forwardOutput.getData().setDoubleValue(0.0);
                rightOutput.getData().setDoubleValue(0.0);
                logger.debug("Left at speed: {}", newSpeed);
            } else if(isDownPressed){
                reverseOutput.getData().setDoubleValue(newSpeed);
                leftOutput.getData().setDoubleValue(0.0);
                forwardOutput.getData().setDoubleValue(0.0);
                rightOutput.getData().setDoubleValue(0.0);
                logger.debug("Reverse at speed: {}", newSpeed);
            } else if (isUpPressed) {
                forwardOutput.getData().setDoubleValue(newSpeed);
                leftOutput.getData().setDoubleValue(0.0);
                reverseOutput.getData().setDoubleValue(0.0);
                rightOutput.getData().setDoubleValue(0.0);
                logger.debug("Forward at speed: {}", newSpeed);
            } else if (isRightPressed) {
                rightOutput.getData().setDoubleValue(newSpeed);
                leftOutput.getData().setDoubleValue(0.0);
                reverseOutput.getData().setDoubleValue(0.0);
                forwardOutput.getData().setDoubleValue(0.0);
                logger.debug("Right at speed: {}", newSpeed);
            }

        }catch(Exception e){
            throw new ProcessException("error during execution");
        }


    }

    @Override
    public void dispose() {
        super.dispose();
    }
}


//public class PiBotDriveProcess extends ExecutableProcessImpl {
//
//    public static final OSHProcessInfo INFO = new OSHProcessInfo(
//            "pi-bot:drive",
//            "Drive Algorithm",
//            "Control for Pibot DriveSensor",
//            PiBotDriveProcess.class);
//
//    protected static final Logger logger = LoggerFactory.getLogger(PiBotDriveProcess.class);
//    private Quantity forwardOutput;
//    private Quantity reverseOutput;
//    private Quantity leftOutput;
//    private Quantity rightOutput;
//    private Quantity forwardLeftOutput;
//    private Quantity forwardRightOutput;
//    private Quantity reverseLeftOutput;
//    private Quantity reverseRightOutput;
//    private Quantity stopOutput;
//    private Quantity speedOutput;
//    private Quantity addSpeed;
//    private Quantity removeSpeed;
//    private Quantity dpad;
//    boolean isMinusPressed = false;
//    boolean isPlusPressed = false;
//    String newDirection;
//    double newSpeed = 0.0;
//
//    public PiBotDriveProcess() {
//
//        super(INFO);
//
//        // Get an instance of SWE Factory suitable to build components
//        SWEHelper swe = new SWEHelper();
//
//        // Inputs
//        inputData.add("pov", dpad = swe.createQuantity()
//                .label("d-pad")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
//        inputData.add("Minus", removeSpeed = swe.createQuantity()
//                .label("Decrease Speed")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
//        inputData.add("Plus", addSpeed = swe.createQuantity()
//                .label("Increase Speed")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
//
//
//        //Outputs
//        outputData.add("Forward", forwardOutput = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("Forward"))
//                .label("Forward")
//                .uomCode("%")
//                .updatable(true)
////                .dataType(DataType.DOUBLE)
//                .build());
//
//        outputData.add("Reverse", reverseOutput= swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("Reverse"))
//                .label("Reverse")
//                .uomCode("%")
//                .updatable(true)
////                .dataType(DataType.DOUBLE)
//                .build());
//        outputData.add("SpinLeft", leftOutput =swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("SpinLeft"))
//                .label("Spin Left")
//                .updatable(true)
//                .uomCode("%")
////                .dataType(DataType.DOUBLE)
//                .build());
//        outputData.add("SpinRight", rightOutput = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("SpinRight"))
//                .label("Spin Right")
//                .uomCode("%")
//                .updatable(true)
////                .dataType(DataType.DOUBLE)
//                .build());
//        outputData.add("ReverseRight", reverseRightOutput= swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ReverseRight"))
//                .label("Reverse Turn Right")
//                .uomCode("%")
//                .updatable(true)
////                .dataType(DataType.DOUBLE)
//                .build());
//        outputData.add("ReverseLeft", reverseLeftOutput = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ReverseLeft"))
//                .label("Reverse Turn Left")
//                .updatable(true)
//                .uomCode("%")
////                .dataType(DataType.DOUBLE)
//                .build());
//        outputData.add("ForwardLeft", forwardLeftOutput=swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ForwardLeft"))
//                .label("Forward Turn Left")
//                .updatable(true)
//                .uomCode("%")
////                .dataType(DataType.DOUBLE)
//                .build());
//        outputData.add("ForwardRight",forwardRightOutput= swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ForwardRight"))
//                .label("Forward Turn Right")
//                .uomCode("%")
//                .updatable(true)
////                .dataType(DataType.DOUBLE)
//                .build());
//        outputData.add("Stop", stopOutput = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("Stop"))
//                .label("Stop")
//                .updatable(true)
//                .uomCode("%")
////                .dataType(DataType.DOUBLE)
//                .build());
//
//    }
//
//    @Override
//    public void init() throws ProcessException {
//
//        logger.debug("Initializing");
//
//        super.init();
//
//        logger.debug("Initialized");
//    }
//
//    @Override
//    public void execute() throws ProcessException{
//
//        try{
//
//            isPlusPressed = addSpeed.getValue() == 1.0;
//            isMinusPressed = removeSpeed.getValue() == 1.0;
//
//            // increase the motor power
//            if(isMinusPressed){
//                // decrease speed
//                newSpeed -= 10.0;
//            }else if(isPlusPressed){
//                //increase speed
//                newSpeed += 10.0;
//            }
//            if(newSpeed >= 100.0){
//                newSpeed= Math.min(newSpeed, 100.0d);
//            }
//            else if(newSpeed <= 0.0){
//                newSpeed = 0.0;
//            }
//
//
//
////            // direction command using remote dpad
//            if(dpad.getData().getDoubleValue() == 0.25){
//                // forward
////                driveOutput.getData().setStringValue(DriveDirection.FORWARD.name());
////                newDirection = DriveDirection.FORWARD.name();
////                forwardOutput.getData().setStringValue(newDirection);
//                forwardOutput.getData().setDoubleValue(newSpeed);
//            }
//            else if (dpad.getData().getDoubleValue() == 0.75){
//                // reverse
////                driveOutput.getData().setStringValue(DriveDirection.REVERSE.name());
////                newDirection = DriveDirection.REVERSE.name();
////                reverseOutput.getData().setStringValue(newDirection);
//                reverseOutput.getData().setDoubleValue(newSpeed);
//            }
//            else if(dpad.getData().getDoubleValue() == 1.0){
//                //left
////                driveOutput.getData().setStringValue(DriveDirection.SPIN_LEFT.name());
////                newDirection = DriveDirection.SPIN_LEFT.name();
////                leftOutput.getData().setStringValue(newDirection);
//                leftOutput.getData().setDoubleValue(newSpeed);
//            }
//            else if(dpad.getData().getDoubleValue() == 0.5){
//                //right
////                driveOutput.getData().setStringValue(DriveDirection.SPIN_RIGHT.name());
////                newDirection = DriveDirection.SPIN_RIGHT.name();
////                rightOutput.getData().setStringValue(newDirection);
//                rightOutput.getData().setDoubleValue(newSpeed);
//            }
//            else if(dpad.getData().getDoubleValue() == 0.125){
//                // up and left
//                //TODO: get it to take two commands for wii remote?
////                driveOutput.getData().setStringValue(DriveDirection.FORWARD_TURN_LEFT.name());
////                newDirection = DriveDirection.FORWARD_TURN_LEFT.name();
////                forwardLeftOutput.getData().setStringValue(newDirection);
//                forwardLeftOutput.getData().setDoubleValue(newSpeed);
//            }
//            else if(dpad.getData().getDoubleValue() == 0.625){
//                // down and right
////                driveOutput.getData().setStringValue(DriveDirection.REVERSE_TURN_RIGHT.name());
////                newDirection = DriveDirection.REVERSE_TURN_RIGHT.name();
////                reverseRightOutput.getData().setStringValue(newDirection);
//                reverseRightOutput.getData().setDoubleValue(newSpeed);
//            }
//            else if(dpad.getData().getDoubleValue() == 0.875){
//                // down and left
////                driveOutput.getData().setStringValue(DriveDirection.REVERSE_TURN_LEFT.name());
////                newDirection = DriveDirection.REVERSE_TURN_LEFT.name();
////                reverseLeftOutput.getData().setStringValue(newDirection);
//                reverseLeftOutput.getData().setDoubleValue(newSpeed);
//            }
//            else if(dpad.getData().getDoubleValue() == 0.375){
//                // up and right
////                driveOutput.getData().setStringValue(DriveDirection.FORWARD_TURN_RIGHT.name());
////                newDirection = DriveDirection.FORWARD_TURN_RIGHT.name();
////                forwardRightOutput.getData().setStringValue(newDirection);
//                forwardRightOutput.getData().setDoubleValue(newSpeed);
//            }
//
//        }catch(Exception e){
//            throw new ProcessException("error during execution");
//        }
//
//
//    }
//
//    @Override
//    public void dispose() {
//        super.dispose();
//    }
//
//}
