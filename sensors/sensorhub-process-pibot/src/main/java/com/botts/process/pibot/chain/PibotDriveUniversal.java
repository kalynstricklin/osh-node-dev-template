/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.pibot.chain;

import com.botts.process.pibot.helpers.AbstractControllerTaskingProcess;
import net.opengis.swe.v20.Quantity;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.process.ProcessException;
import org.vast.swe.SWEHelper;

/**
 * Process for performing the switching of colors of the light on pi-bot
 *
 * @author Kalyn Stricklin
 * @since 1.0.0
 */


public class PibotDriveUniversal extends AbstractControllerTaskingProcess {

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
    boolean isBPressed = false;
    float newSpeed = 0.0f;

    public PibotDriveUniversal() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
//        SWEHelper swe = new SWEHelper();
//
//        inputData.add("", gamepad = swe.createQuantity()
//                .build());
//        // Inputs
//        inputData.add("pov", dpad = swe.createQuantity()
////                .definition(SWEHelper.getPropertyUri("pov"))
//                .label("Hat Switch")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
//        inputData.add("LeftThumb", removeSpeed = swe.createQuantity()
////                .definition(SWEHelper.getPropertyUri("LeftThumb"))
//                .label("LeftThumb")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
//        inputData.add("RightThumb", addSpeed = swe.createQuantity()
////                .definition(SWEHelper.getPropertyUri("RightThumb"))
//                .label("RightThumb")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
//        inputData.add("A", buttonA = swe.createQuantity()
//                .label("A")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
//        inputData.add("B", buttonB = swe.createQuantity()
//                .label("B")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());


        //outputs
        outputData.add("Forward", forwardOutput = fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("Forward"))
                .label("Forward")
                .uom("%")
                .updatable(true)
                .build());
        outputData.add("Reverse", reverseOutput= fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("Reverse"))
                .label("Reverse")
                .uom("%")
                .updatable(true)
                .build());
        outputData.add("SpinLeft", leftOutput =fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("SpinLeft"))
                .label("Spin Left")
                .updatable(true)
                .uom("%")
                .build());
        outputData.add("SpinRight", rightOutput = fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("SpinRight"))
                .label("Spin Right")
                .uom("%")
                .updatable(true)
                .build());
        outputData.add("ReverseRight", reverseRightOutput= fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("ReverseRight"))
                .label("Reverse Turn Right")
                .uom("%")
                .updatable(true)
                .build());
        outputData.add("ReverseLeft", reverseLeftOutput = fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("ReverseLeft"))
                .label("Reverse Turn Left")
                .updatable(true)
                .uom("%")
                .build());
        outputData.add("ForwardLeft", forwardLeftOutput=fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("ForwardLeft"))
                .label("Forward Turn Left")
                .updatable(true)
                .uom("%")
                .build());
        outputData.add("ForwardRight",forwardRightOutput= fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("ForwardRight"))
                .label("Forward Turn Right")
                .uom("%")
                .updatable(true)
                .build());
        outputData.add("Stop", stopOutput = fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("Stop"))
                .label("Stop")
                .updatable(true)
                .uom("%")
                .build());
    }


    @Override
    public void updateOutputs() throws ProcessException {
        /**drive**/
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
            newSpeed= Math.min(newSpeed, 100.0f);
        }
        else if(newSpeed <= 0.0){
            newSpeed = 0.0f;
        }
        logger.debug("Speed: {}", newSpeed);

        if(fac.getComponentValueInput("pov") == 0.25){
            forwardOutput.getData().setFloatValue(newSpeed);
            forwardLeftOutput.getData().setFloatValue(0.0f);
            forwardRightOutput.getData().setFloatValue(0.0f);
            rightOutput.getData().setFloatValue(0.0f);
            reverseRightOutput.getData().setFloatValue(0.0f);
            reverseOutput.getData().setFloatValue(0.0f);
            reverseLeftOutput.getData().setFloatValue(0.0f);
            leftOutput.getData().setFloatValue(0.0f);
        }else if(fac.getComponentValueInput("pov") == 0.125){
            forwardLeftOutput.getData().setFloatValue(newSpeed);
            forwardOutput.getData().setFloatValue(0.0f);
            forwardRightOutput.getData().setFloatValue(0.0f);
            rightOutput.getData().setFloatValue(0.0f);
            reverseRightOutput.getData().setFloatValue(0.0f);
            reverseOutput.getData().setFloatValue(0.0f);
            reverseLeftOutput.getData().setFloatValue(0.0f);
            leftOutput.getData().setFloatValue(0.0f);
        }else if(fac.getComponentValueInput("pov") == 0.375){
            forwardRightOutput.getData().setFloatValue(newSpeed);
            forwardLeftOutput.getData().setFloatValue(0.0f);
            forwardOutput.getData().setFloatValue(0.0f);
            rightOutput.getData().setFloatValue(0.0f);
            reverseRightOutput.getData().setFloatValue(0.0f);
            reverseOutput.getData().setFloatValue(0.0f);
            reverseLeftOutput.getData().setFloatValue(0.0f);
            leftOutput.getData().setFloatValue(0.0f);
        } else if (fac.getComponentValueInput("pov") == 0.5) {
            rightOutput.getData().setFloatValue(newSpeed);
            forwardRightOutput.getData().setFloatValue(0.0f);
            forwardLeftOutput.getData().setFloatValue(0.0f);
            forwardOutput.getData().setFloatValue(0.0f);
            reverseRightOutput.getData().setFloatValue(0.0f);
            reverseOutput.getData().setFloatValue(0.0f);
            reverseLeftOutput.getData().setFloatValue(0.0f);
            leftOutput.getData().setFloatValue(0.0f);
        }else if(fac.getComponentValueInput("pov") == 0.625){
            reverseRightOutput.getData().setFloatValue(newSpeed);
            rightOutput.getData().setFloatValue(0.0f);
            forwardRightOutput.getData().setFloatValue(0.0f);
            forwardLeftOutput.getData().setFloatValue(0.0f);
            forwardOutput.getData().setFloatValue(0.0f);
            reverseOutput.getData().setFloatValue(0.0f);
            reverseLeftOutput.getData().setFloatValue(0.0f);
            leftOutput.getData().setFloatValue(0.0f);
        }else if(fac.getComponentValueInput("pov") == 0.75){
            reverseOutput.getData().setFloatValue(newSpeed);
            reverseRightOutput.getData().setFloatValue(0.0f);
            rightOutput.getData().setFloatValue(0.0f);
            forwardRightOutput.getData().setFloatValue(0.0f);
            forwardLeftOutput.getData().setFloatValue(0.0f);
            forwardOutput.getData().setFloatValue(0.0f);
            reverseLeftOutput.getData().setFloatValue(0.0f);
            leftOutput.getData().setFloatValue(0.0f);
        }else if(fac.getComponentValueInput("pov") == 0.875){
            reverseLeftOutput.getData().setFloatValue(newSpeed);
            reverseOutput.getData().setFloatValue(0.0f);
            reverseRightOutput.getData().setFloatValue(0.0f);
            rightOutput.getData().setFloatValue(0.0f);
            forwardRightOutput.getData().setFloatValue(0.0f);
            forwardLeftOutput.getData().setFloatValue(0.0f);
            forwardOutput.getData().setFloatValue(0.0f);
            leftOutput.getData().setFloatValue(0.0f);
        }else if(fac.getComponentValueInput("pov") == 1.0){
            leftOutput.getData().setFloatValue(newSpeed);
            reverseLeftOutput.getData().setFloatValue(0.0f);
            reverseOutput.getData().setFloatValue(0.0f);
            reverseRightOutput.getData().setFloatValue(0.0f);
            rightOutput.getData().setFloatValue(0.0f);
            forwardRightOutput.getData().setFloatValue(0.0f);
            forwardLeftOutput.getData().setFloatValue(0.0f);
            forwardOutput.getData().setFloatValue(0.0f);
        }
        //stop robot and turn searchlight off!
        if(isBPressed){
            forwardOutput.getData().setFloatValue(0.0f);
            forwardLeftOutput.getData().setFloatValue(0.0f);
            forwardRightOutput.getData().setFloatValue(0.0f);
            rightOutput.getData().setFloatValue(0.0f);
            reverseRightOutput.getData().setFloatValue(0.0f);
            reverseOutput.getData().setFloatValue(0.0f);
            reverseLeftOutput.getData().setFloatValue(0.0f);
            leftOutput.getData().setFloatValue(0.0f);
            stopOutput.getData().setFloatValue(100.0f);

        }

    }

//    @Override
//    public void execute() throws ProcessException{
//
//        try{
//
//            isLeftThumbPressed = removeSpeed.getValue() == 1.0;
//            isRightThumbPressed = addSpeed.getValue() == 1.0;
//            isAPressed = buttonA.getValue()  == 1.0;
//            isBPressed = buttonB.getValue()  == 1.0;
//
//            // increase the motor power
//            if(isLeftThumbPressed){
//                // decrease speed
//                newSpeed -= 10.0;
//                logger.debug("decreasing speed {}", newSpeed);
//
//            }else if(isRightThumbPressed){
//                //increase speed
//                newSpeed += 10.0;
//                logger.debug("Increasing speed {}", newSpeed);
//            }
//            if(newSpeed >= 100.0){
//                newSpeed= Math.min(newSpeed, 100.0f);
//            }
//            else if(newSpeed <= 0.0){
//                newSpeed = 0.0f;
//            }
//            logger.debug("Speed: {}", newSpeed);
//
//            if(dpad.getData().getFloatValue() == 0.25){
//                forwardOutput.getData().setFloatValue(newSpeed);
//                forwardLeftOutput.getData().setFloatValue(0.0f);
//                forwardRightOutput.getData().setFloatValue(0.0f);
//                rightOutput.getData().setFloatValue(0.0f);
//                reverseRightOutput.getData().setFloatValue(0.0f);
//                reverseOutput.getData().setFloatValue(0.0f);
//                reverseLeftOutput.getData().setFloatValue(0.0f);
//                leftOutput.getData().setFloatValue(0.0f);
//            }else if(dpad.getData().getFloatValue() == 0.125){
//                forwardLeftOutput.getData().setFloatValue(newSpeed);
//                forwardOutput.getData().setFloatValue(0.0f);
//                forwardRightOutput.getData().setFloatValue(0.0f);
//                rightOutput.getData().setFloatValue(0.0f);
//                reverseRightOutput.getData().setFloatValue(0.0f);
//                reverseOutput.getData().setFloatValue(0.0f);
//                reverseLeftOutput.getData().setFloatValue(0.0f);
//                leftOutput.getData().setFloatValue(0.0f);
//            }else if(dpad.getData().getFloatValue() == 0.375){
//                forwardRightOutput.getData().setFloatValue(newSpeed);
//                forwardLeftOutput.getData().setFloatValue(0.0f);
//                forwardOutput.getData().setFloatValue(0.0f);
//                rightOutput.getData().setFloatValue(0.0f);
//                reverseRightOutput.getData().setFloatValue(0.0f);
//                reverseOutput.getData().setFloatValue(0.0f);
//                reverseLeftOutput.getData().setFloatValue(0.0f);
//                leftOutput.getData().setFloatValue(0.0f);
//            } else if (dpad.getData().getFloatValue()== 0.5) {
//                rightOutput.getData().setFloatValue(newSpeed);
//                forwardRightOutput.getData().setFloatValue(0.0f);
//                forwardLeftOutput.getData().setFloatValue(0.0f);
//                forwardOutput.getData().setFloatValue(0.0f);
//                reverseRightOutput.getData().setFloatValue(0.0f);
//                reverseOutput.getData().setFloatValue(0.0f);
//                reverseLeftOutput.getData().setFloatValue(0.0f);
//                leftOutput.getData().setFloatValue(0.0f);
//            }else if(dpad.getData().getFloatValue() == 0.625){
//                reverseRightOutput.getData().setFloatValue(newSpeed);
//                rightOutput.getData().setFloatValue(0.0f);
//                forwardRightOutput.getData().setFloatValue(0.0f);
//                forwardLeftOutput.getData().setFloatValue(0.0f);
//                forwardOutput.getData().setFloatValue(0.0f);
//                reverseOutput.getData().setFloatValue(0.0f);
//                reverseLeftOutput.getData().setFloatValue(0.0f);
//                leftOutput.getData().setFloatValue(0.0f);
//            }else if(dpad.getData().getFloatValue() == 0.75){
//                reverseOutput.getData().setFloatValue(newSpeed);
//                reverseRightOutput.getData().setFloatValue(0.0f);
//                rightOutput.getData().setFloatValue(0.0f);
//                forwardRightOutput.getData().setFloatValue(0.0f);
//                forwardLeftOutput.getData().setFloatValue(0.0f);
//                forwardOutput.getData().setFloatValue(0.0f);
//                reverseLeftOutput.getData().setFloatValue(0.0f);
//                leftOutput.getData().setFloatValue(0.0f);
//            }else if(dpad.getData().getFloatValue() == 0.875){
//                reverseLeftOutput.getData().setFloatValue(newSpeed);
//                reverseOutput.getData().setFloatValue(0.0f);
//                reverseRightOutput.getData().setFloatValue(0.0f);
//                rightOutput.getData().setFloatValue(0.0f);
//                forwardRightOutput.getData().setFloatValue(0.0f);
//                forwardLeftOutput.getData().setFloatValue(0.0f);
//                forwardOutput.getData().setFloatValue(0.0f);
//                leftOutput.getData().setFloatValue(0.0f);
//            }else if(dpad.getData().getFloatValue() == 1.0){
//                leftOutput.getData().setFloatValue(newSpeed);
//                reverseLeftOutput.getData().setFloatValue(0.0f);
//                reverseOutput.getData().setFloatValue(0.0f);
//                reverseRightOutput.getData().setFloatValue(0.0f);
//                rightOutput.getData().setFloatValue(0.0f);
//                forwardRightOutput.getData().setFloatValue(0.0f);
//                forwardLeftOutput.getData().setFloatValue(0.0f);
//                forwardOutput.getData().setFloatValue(0.0f);
//            }
//
//            //stop robot!
//            if(isAPressed && isBPressed){
//                forwardOutput.getData().setFloatValue(0.0f);
//                forwardLeftOutput.getData().setFloatValue(0.0f);
//                forwardRightOutput.getData().setFloatValue(0.0f);
//                rightOutput.getData().setFloatValue(0.0f);
//                reverseRightOutput.getData().setFloatValue(0.0f);
//                reverseOutput.getData().setFloatValue(0.0f);
//                reverseLeftOutput.getData().setFloatValue(0.0f);
//                leftOutput.getData().setFloatValue(0.0f);
//                stopOutput.getData().setFloatValue(100.0f);
//            }
//
//
//        }catch(Exception e){
//            throw new ProcessException("error during execution {}", e);
//        }
//    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
