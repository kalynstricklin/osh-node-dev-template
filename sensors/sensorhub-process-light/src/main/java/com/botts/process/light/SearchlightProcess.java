/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.light;

import net.opengis.swe.v20.*;
import net.opengis.swe.v20.Boolean;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.swe.SWEHelper;

/**
 * Process for performing the switching of colors of the light on pi-bot
 *
 * @author Kalyn Stricklin
 * @since 1.0.0
 */
public class SearchlightProcess extends ExecutableProcessImpl {

    public static final OSHProcessInfo INFO = new OSHProcessInfo(
            "pi-bot:searchlight",
            "Searchlight Algorithm",
            "Light changing algorithm, can be used to alternate between colors on Searchlight pi-bot sensor",
            SearchlightProcess.class);

    protected static final Logger logger = LoggerFactory.getLogger(SearchlightProcess.class);
    private final Boolean inputButton;
//    private Text outputColor;
//    private Category outputColor;
    private DataRecord outputColor;
    String newColor;

    enum SearchlightState{
        OFF,
        WHITE,
        RED,
        MAGENTA,
        BLUE,
        CYAN,
        GREEN,
        YELLOW
    }

    String
        OFF,
        WHITE,
        RED,
        MAGENTA,
        BLUE,
        CYAN,
        GREEN,
        YELLOW;
    boolean isOnePressed = false;

    public SearchlightProcess() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
        SWEHelper sweFactory = new SWEHelper();


        // Inputs
        inputData.add("buttons", sweFactory.createRecord()
                .addField("button1", inputButton = sweFactory.createBoolean()
                        .definition(SWEHelper.getPropertyUri("button1"))
                        .build())
                .build());

        outputData.add("process", outputColor = sweFactory.createRecord()
                .updatable(true)
                .definition(SWEHelper.getPropertyUri("SearchlightProcess"))
                .addField("Color", sweFactory.createCategory()
                        .definition(SWEHelper.getPropertyUri("Color"))
                        .addAllowedValues(
                                SearchlightState.OFF.name(),
                                SearchlightState.WHITE.name(),
                                SearchlightState.RED.name(),
                                SearchlightState.GREEN.name(),
                                SearchlightState.MAGENTA.name(),
                                SearchlightState.BLUE.name(),
                                SearchlightState.YELLOW.name(),
                                SearchlightState.CYAN.name())
                        )
                .build());
        // Outputs
//        outputData.add("color", outputColor = sweFactory.createCategory()
//                        .definition(SWEHelper.getPropertyUri("Color"))
//                        .addAllowedValues(
//                                SearchlightState.OFF.name(),
//                                SearchlightState.WHITE.name(),
//                                SearchlightState.RED.name(),
//                                SearchlightState.GREEN.name(),
//                                SearchlightState.MAGENTA.name(),
//                                SearchlightState.BLUE.name(),
//                                SearchlightState.YELLOW.name(),
//                                SearchlightState.CYAN.name())
//                        .build());

//        outputData.add("color", outputColor = sweFactory.createText()
//                .definition(SWEHelper.getPropertyUri("Color"))
//                .addAllowedValues(
//                        SearchlightState.OFF.name(),
//                        SearchlightState.WHITE.name(),
//                        SearchlightState.RED.name(),
//                        SearchlightState.GREEN.name(),
//                        SearchlightState.MAGENTA.name(),
//                        SearchlightState.BLUE.name(),
//                        SearchlightState.YELLOW.name(),
//                        SearchlightState.CYAN.name())
//                .build());

//        outputData.add("Color", outputColor = sweFactory.createText()
//                .definition(SWEHelper.getPropertyUri("Color"))
//                .addAllowedValues(
//                        OFF,
//                        WHITE,
//                        RED,
//                        MAGENTA,
//                        BLUE,
//                        CYAN,
//                        GREEN,
//                        YELLOW
//                )
//                .build());
    }

    @Override
    public void init() throws ProcessException {

        logger.debug("Initializing");

        super.init();

        logger.debug("Initialized");
    }

    @Override
    public void execute() {

        // color changing = button 1 on wii remote
        isOnePressed = inputButton.getData().getBooleanValue();

        try{
            Thread.sleep(250);
            if(isOnePressed){
                newColor = alternateColors(outputColor.getData().getStringValue());
                logger.debug("New color: {}", newColor);
                outputColor.getData().setStringValue(newColor);
            }

        }catch (Exception e){
            logger.debug("Error during execution of process");
        }

//        try{
//            // color changing = button 1 on wii remote
//            isOnePressed = inputButton.getData().getBooleanValue();
//
//            // change color if button 1 is pressed
//            if(isOnePressed){
//                newColor = alternateColors(outputColor.getData().getStringValue());
//            }
//
//            logger.debug("New color: {}", newColor);
//            outputColor.getData().setStringValue(newColor);
//
//        }catch (Exception e){
//            logger.debug("Error during execution process");
//        }
    }


    private String alternateColors(String state) {
        logger.debug("Alt Color: {}", state);

        if(state == null){
            state = "OFF";
        }

        switch (state) {
            case "OFF":
                return SearchlightState.RED.name();
//                return RED;
            case "RED":
                return SearchlightState.BLUE.name();
//                return BLUE;
            case "BLUE":
                return SearchlightState.CYAN.name();
//                return CYAN;
            case "CYAN":
                return SearchlightState.GREEN.name();
//                return GREEN;
            case "GREEN":
                return SearchlightState.WHITE.name();
//                return WHITE;
            case "WHITE":
                return SearchlightState.YELLOW.name();
//                return YELLOW;
            case "YELLOW":
                return SearchlightState.MAGENTA.name();
//                return MAGENTA;
            case "MAGENTA":
                return SearchlightState.RED.name();
//                return RED;
            default:
//                return OFF;
                return SearchlightState.OFF.name(); // Default to OFF if current color is not recognized or chosen!
        }
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}
