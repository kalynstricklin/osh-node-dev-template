/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.pibot.chain;

import com.botts.process.pibot.helpers.AbstractControllerTaskingProcess;
import net.opengis.swe.v20.Category;
import net.opengis.swe.v20.Quantity;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.sensorhub.impl.sensor.pibot.searchlight.SearchlightState;
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
public class SearchlightProcessUniversal extends AbstractControllerTaskingProcess {

    public static final OSHProcessInfo INFO = new OSHProcessInfo(
            "pi-bot:searchlight3",
            "Searchlight Algorithm",
            "Light changing algorithm, can be used to alternate between colors on Searchlight pi-bot sensor",
            SearchlightProcessUniversal.class);

    protected static final Logger logger = LoggerFactory.getLogger(SearchlightProcessUniversal.class);

    private Quantity buttonA;
    private Category outputColor;
    String newColor;
    boolean isAPressed = false;

    public SearchlightProcessUniversal() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
//        SWEHelper swe = new SWEHelper();

//        // Inputs
//        inputData.add("buttonA", buttonA = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("A"))
//                .label("A")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());

        outputData.add("searchlight", outputColor = fac.createCategory()
                .label("Searchlight Color")
                .definition(SWEHelper.getPropertyUri("Searchlight"))
                .build());

    }

    @Override
    public void init() throws ProcessException {

        logger.debug("Initializing");

        super.init();

        logger.debug("Initialized");
    }

    @Override
    public void updateOutputs() throws ProcessException {
        isAPressed = fac.getComponentValueInput("A")  == 1.0;
        /**searchlight**/
        if(isAPressed){
            logger.debug("button pressed!\n");
            newColor = alternateColors(outputColor.getData().getStringValue());
        }
        logger.debug("New color: {}", newColor);
        outputColor.getData().setStringValue(newColor);


    }

//    @Override
//    public void execute() {
//
//        try{
//            // check if it is pressed
//            isPressed = buttonA.getValue() == 1.0;
//
//            if(isPressed){
//                logger.debug("button pressed!\n");
//                newColor = alternateColors(outputColor.getData().getStringValue());
//            }
//            logger.debug("New color: {}", newColor);
//            outputColor.getData().setStringValue(newColor);
//
//        }catch (Exception e){
//            logger.debug("Error during execution of process");
//        }
//
//    }


    private String alternateColors(String state) {
        logger.debug("Alt Color: {}", state);

        if(state == null){
            state = "OFF";
        }

        switch (state) {
            case "OFF":
                return SearchlightState.RED.name();
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
            default:
                return SearchlightState.OFF.name(); // Default to OFF if current color is not recognized or chosen!
        }
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}
