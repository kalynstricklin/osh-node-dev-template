package com.botts.process.pibot;

import com.botts.process.pibot.helpers.AbstractControllerTaskingProcess;
import net.opengis.swe.v20.Category;
import net.opengis.swe.v20.Quantity;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.sensorhub.impl.sensor.pibot.searchlight.SearchlightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.process.ProcessException;
import org.vast.swe.SWEHelper;

public class PibotProcess extends AbstractControllerTaskingProcess {
    public static final OSHProcessInfo INFO = new OSHProcessInfo("pibot", "Pibot Process", null, PibotProcess.class);
    protected static final Logger logger = LoggerFactory.getLogger(PibotProcess.class);
    private Quantity forwardOutput;
    private Quantity reverseOutput;
    private Quantity leftOutput;
    private Quantity rightOutput;
    private Quantity forwardLeftOutput;
    private Quantity forwardRightOutput;
    private Quantity reverseLeftOutput;
    private Quantity reverseRightOutput;
    private Quantity stopOutput;
    private Quantity tiltOutput;
    private Quantity panOutput;
    private Category outputColor;

    //color
    String newColor;
    boolean isAPressed = false;

    // stops drive and turns off searchlight
    boolean isBPressed = false;
    //drive
    boolean isLeftThumbPressed = false;
    boolean isRightThumbPressed = false;
    float newSpeed = 0.0f;

    //camera
    float newTilt = 20.0f;
    float newPan= 20.0f;
    float currentX= 0.0f;
    float currentY= 0.0f;
    private static final float MIN_ANGLE = 20.0f;

    private static final float MAX_ANGLE = 160.0f;

    public PibotProcess() {
        super(INFO);
        //camera
        outputData.add("Pan", panOutput = fac.createQuantity()
                .definition(SWEHelper.getPropertyUri("servo-angle"))
                .description("The angle in degrees to which the servo is to turn")
                .uom("deg")
                .build());
        outputData.add("Tilt", tiltOutput = fac.createQuantity()
                .description("The angle in degrees to which the servo is to turn")
                .updatable(true)
                .uom("deg")
                .definition(SWEHelper.getPropertyUri("servo-angle"))
                .build());

        //searchlight
        outputData.add("searchlight", outputColor = fac.createCategory()
                .label("Searchlight Color")
                .definition(SWEHelper.getPropertyUri("Searchlight"))
                .build());

        //drive
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
        try{
            currentX = fac.getComponentValueInput("x");
            currentY = fac.getComponentValueInput("y");
            logger.debug("current x and current y: {} {}", currentX, currentY);

            isLeftThumbPressed = fac.getComponentValueInput("LeftThumb") == 1.0f;
            isRightThumbPressed = fac.getComponentValueInput("RightThumb") == 1.0f;
            isAPressed = fac.getComponentValueInput("A")  == 1.0;
            isBPressed = fac.getComponentValueInput("B")  == 1.0;


            /**searchlight**/
            if(isAPressed){
                logger.debug("button pressed!\n");
                newColor = alternateColors(outputColor.getData().getStringValue());
            }
            logger.debug("New color: {}", newColor);
            outputColor.getData().setStringValue(newColor);

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

                outputColor.getData().setStringValue(SearchlightState.OFF.name());
            }

            /**camera**/
            //TODO: fix the tilt issue and figure out the best way to set these
            newPan = currentX * 5;
            newTilt = (currentY * 5);
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

        }catch(Exception e){
            throw new ProcessException("error during execution {}", e);
        }

    }

    private String alternateColors(String state) {
        logger.debug("Alt Color: {}", state);

        if(state == null){
            state = "OFF";
        }

        switch (state) {
            case "RED":
                return SearchlightState.BLUE.name();
            case "BLUE":
                return SearchlightState.CYAN.name();
            case "CYAN":
                return SearchlightState.GREEN.name();
            case "GREEN":
                return SearchlightState.WHITE.name();
            case "WHITE":
                return SearchlightState.YELLOW.name();
            case "YELLOW":
                return SearchlightState.MAGENTA.name();
            case "MAGENTA":
                return SearchlightState.RED.name();
            case "OFF":
            default:
                return SearchlightState.OFF.name(); // Default to OFF if current color is not recognized or chosen!
        }
    }
}
