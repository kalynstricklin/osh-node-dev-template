/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.drive;

import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.Quantity;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


public class PibotDriveUniversal extends ExecutableProcessImpl {

    public static final OSHProcessInfo INFO = new OSHProcessInfo(
            "pi-bot:drive3",
            "Drive Algorithm",
            "Control for Pibot DriveSensor",
            PibotDriveUniversal.class);

    protected static final Logger logger = LoggerFactory.getLogger(PibotDriveUniversal.class);
    private Quantity forwardOutput;
    private Quantity reverseOutput;
    private Quantity leftOutput;
    private Quantity rightOutput;
    private Quantity forwardLeftOutput;
    private Quantity forwardRightOutput;
    private Quantity reverseLeftOutput;
    private Quantity reverseRightOutput;
    private Quantity stopOutput;
    boolean isLeftThumbPressed = false;
    boolean isRightThumbPressed = false;
    double newSpeed = 0.0;
    private Quantity addSpeed;
    private Quantity removeSpeed;
    private Quantity dpad;
    private Quantity buttonA;

    public PibotDriveUniversal() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
        SWEHelper swe = new SWEHelper();

        // Inputs
        inputData.add("pov", dpad = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("pov"))
                .label("Hat Switch")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        inputData.add("LeftThumb", removeSpeed = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("LeftThumb"))
                .label("LeftThumb")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        inputData.add("RightThumb", addSpeed = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("RightThumb"))
                .label("RightThumb")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        //outputs
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
        outputData.add("ReverseRight", reverseRightOutput= swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("ReverseRight"))
                .label("Reverse Turn Right")
                .uom("%")
                .updatable(true)
                .build());
        outputData.add("ReverseLeft", reverseLeftOutput = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("ReverseLeft"))
                .label("Reverse Turn Left")
                .updatable(true)
                .uom("%")
                .build());
        outputData.add("ForwardLeft", forwardLeftOutput=swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("ForwardLeft"))
                .label("Forward Turn Left")
                .updatable(true)
                .uom("%")
                .build());
        outputData.add("ForwardRight",forwardRightOutput= swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("ForwardRight"))
                .label("Forward Turn Right")
                .uom("%")
                .updatable(true)
                .build());
        outputData.add("Stop", stopOutput = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Stop"))
                .label("Stop")
                .updatable(true)
                .dataType(DataType.DOUBLE)
                .uom("%")
                .build());


    }


    @Override
    public void execute() throws ProcessException{

        try{

            isLeftThumbPressed = removeSpeed.getValue() == 1.0;
            isRightThumbPressed = addSpeed.getValue() == 1.0;

            // increase the motor power
            if(isLeftThumbPressed){
                // decrease speed
                newSpeed -= 10.0;
                logger.debug("decreasing speed {}", newSpeed);

            }else if(isRightThumbPressed){
                //increase speed
                newSpeed += 10.0;
                logger.debug("Increasing speed {}", newSpeed);
            }
            if(newSpeed >= 100.0){
                newSpeed= Math.min(newSpeed, 100.0);
            }
            else if(newSpeed <= 0.0){
                newSpeed = 0.0;
            }
            logger.debug("Speed: {}", newSpeed);

            if(dpad.getData().getDoubleValue() == 0.25){
                forwardOutput.getData().setDoubleValue(newSpeed);
                forwardLeftOutput.getData().setDoubleValue(0.0);
                forwardRightOutput.getData().setDoubleValue(0.0);
                rightOutput.getData().setDoubleValue(0.0);
                reverseRightOutput.getData().setDoubleValue(0.0);
                reverseOutput.getData().setDoubleValue(0.0);
                reverseLeftOutput.getData().setDoubleValue(0.0);
                leftOutput.getData().setDoubleValue(0.0);
            }else if(dpad.getData().getDoubleValue() == 0.125){
                forwardLeftOutput.getData().setDoubleValue(newSpeed);
                forwardOutput.getData().setDoubleValue(0.0);
                forwardRightOutput.getData().setDoubleValue(0.0);
                rightOutput.getData().setDoubleValue(0.0);
                reverseRightOutput.getData().setDoubleValue(0.0);
                reverseOutput.getData().setDoubleValue(0.0);
                reverseLeftOutput.getData().setDoubleValue(0.0);
                leftOutput.getData().setDoubleValue(0.0);
            }else if(dpad.getData().getDoubleValue() == 0.375){
                forwardRightOutput.getData().setDoubleValue(newSpeed);
                forwardLeftOutput.getData().setDoubleValue(0.0);
                forwardOutput.getData().setDoubleValue(0.0);
                rightOutput.getData().setDoubleValue(0.0);
                reverseRightOutput.getData().setDoubleValue(0.0);
                reverseOutput.getData().setDoubleValue(0.0);
                reverseLeftOutput.getData().setDoubleValue(0.0);
                leftOutput.getData().setDoubleValue(0.0);
            } else if (dpad.getData().getDoubleValue()== 0.5) {
                rightOutput.getData().setDoubleValue(newSpeed);
                forwardRightOutput.getData().setDoubleValue(0.0);
                forwardLeftOutput.getData().setDoubleValue(0.0);
                forwardOutput.getData().setDoubleValue(0.0);
                reverseRightOutput.getData().setDoubleValue(0.0);
                reverseOutput.getData().setDoubleValue(0.0);
                reverseLeftOutput.getData().setDoubleValue(0.0);
                leftOutput.getData().setDoubleValue(0.0);
            }else if(dpad.getData().getDoubleValue() == 0.625){
                reverseRightOutput.getData().setDoubleValue(newSpeed);
                rightOutput.getData().setDoubleValue(0.0);
                forwardRightOutput.getData().setDoubleValue(0.0);
                forwardLeftOutput.getData().setDoubleValue(0.0);
                forwardOutput.getData().setDoubleValue(0.0);
                reverseOutput.getData().setDoubleValue(0.0);
                reverseLeftOutput.getData().setDoubleValue(0.0);
                leftOutput.getData().setDoubleValue(0.0);
            }else if(dpad.getData().getDoubleValue() == 0.75){
                reverseOutput.getData().setDoubleValue(newSpeed);
                reverseRightOutput.getData().setDoubleValue(0.0);
                rightOutput.getData().setDoubleValue(0.0);
                forwardRightOutput.getData().setDoubleValue(0.0);
                forwardLeftOutput.getData().setDoubleValue(0.0);
                forwardOutput.getData().setDoubleValue(0.0);
                reverseLeftOutput.getData().setDoubleValue(0.0);
                leftOutput.getData().setDoubleValue(0.0);
            }else if(dpad.getData().getDoubleValue() == 0.875){
                reverseLeftOutput.getData().setDoubleValue(newSpeed);
                reverseOutput.getData().setDoubleValue(0.0);
                reverseRightOutput.getData().setDoubleValue(0.0);
                rightOutput.getData().setDoubleValue(0.0);
                forwardRightOutput.getData().setDoubleValue(0.0);
                forwardLeftOutput.getData().setDoubleValue(0.0);
                forwardOutput.getData().setDoubleValue(0.0);
                leftOutput.getData().setDoubleValue(0.0);
            }else if(dpad.getData().getDoubleValue() == 1.0){
                leftOutput.getData().setDoubleValue(newSpeed);
                reverseLeftOutput.getData().setDoubleValue(0.0);
                reverseOutput.getData().setDoubleValue(0.0);
                reverseRightOutput.getData().setDoubleValue(0.0);
                rightOutput.getData().setDoubleValue(0.0);
                forwardRightOutput.getData().setDoubleValue(0.0);
                forwardLeftOutput.getData().setDoubleValue(0.0);
                forwardOutput.getData().setDoubleValue(0.0);
            }


        }catch(Exception e){
            throw new ProcessException("error during execution {}", e);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
