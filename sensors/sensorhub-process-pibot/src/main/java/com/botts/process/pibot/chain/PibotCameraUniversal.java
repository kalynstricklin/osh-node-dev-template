/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.pibot.chain;

import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.Quantity;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.process.ProcessException;
import org.vast.swe.SWEHelper;
import com.botts.process.pibot.helpers.AbstractControllerTaskingProcess;

/**
 * Process for performing the switching of colors of the light on pi-bot
 *
 * @author Kalyn Stricklin
 * @since 1.0.0
 */


public class PibotCameraUniversal extends AbstractControllerTaskingProcess {

    public static final OSHProcessInfo INFO = new OSHProcessInfo(
            "pi-bot:camera3",
            "Drive Algorithm",
            "Control for Pibot DriveSensor",
            PibotCameraUniversal.class);

    protected static final Logger logger = LoggerFactory.getLogger(PibotCameraUniversal.class);
    private Quantity tiltOutput;
    private Quantity panOutput;
//    private Quantity x;
//    private Quantity y;
    float newTilt = 20.0f;
    float newPan= 20.0f;
    float currentX= 0.0f;
    float currentY= 0.0f;
    private static final float MIN_ANGLE = 20.0f;

    private static final float MAX_ANGLE = 160.0f;

    public PibotCameraUniversal() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
//        SWEHelper swe = new SWEHelper();

//        // Inputs
//        inputData.add("x", x = swe.createQuantity()
//                .label("x")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
//        inputData.add("y", y = swe.createQuantity()
//                .label("y")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
        //outputs
        outputData.add("Pan", panOutput = fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("servo-angle"))
                .description("The angle in degrees to which the servo is to turn")
                .uom("deg")
                .dataType(DataType.DOUBLE)
                .build());
        outputData.add("Tilt", tiltOutput = fac.createQuantity()
                .description("The angle in degrees to which the servo is to turn")
                .updatable(true)
                .uom("deg")
                .definition(SWEHelper.getPropertyUri("servo-angle"))
                .build());
    }


    @Override
    public void updateOutputs() throws ProcessException {
        currentX = fac.getComponentValueInput("x");
        currentY = fac.getComponentValueInput("y");
        logger.debug("current x and current y: {} {}", currentX, currentY);
        /**camera**/
        //TODO: fix the tilt issue and figure out the best way to set these
        newPan = currentX + 5;
        newTilt = -(currentY + 5);
        logger.debug("new Pan and Tilt: {} {}", newPan, newTilt);

        if(newTilt >= MAX_ANGLE || newPan >= MAX_ANGLE) {
            newTilt= Math.min(newTilt, MAX_ANGLE);
            newPan= Math.min(newPan, MAX_ANGLE);
        }
        else if(newTilt <= MIN_ANGLE || newPan <= MIN_ANGLE){
            newTilt = MIN_ANGLE;
            newPan = MIN_ANGLE;
        }

        panOutput.getData().setFloatValue(newPan);
        tiltOutput.getData().setFloatValue(newTilt);


    }

//    @Override
//    public void execute() throws ProcessException{
//
//        try{
////            currentX = x.getData().getFloatValue();
////            currentY = y.getData().getFloatValue();
////            logger.debug("Current X and Y: {} {}", x,y);
//
//            //TODO: fix the tilt issue and figure out the best way to set these
//            newPan = currentX + 5;
//            newTilt = -(currentY + 5);
//            logger.debug("new Pan and Tilt: {} {}", newPan, newTilt);
//
//            if(newTilt >= MAX_ANGLE || newPan >= MAX_ANGLE) {
//                newTilt= Math.min(newTilt, MAX_ANGLE);
//                newPan= Math.min(newPan, MAX_ANGLE);
//            }
//            else if(newTilt <= MIN_ANGLE || newPan <= MIN_ANGLE){
//                newTilt = MIN_ANGLE;
//                newPan = MIN_ANGLE;
//            }
//
//            panOutput.getData().setFloatValue(newPan);
//            tiltOutput.getData().setFloatValue(newTilt);
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
