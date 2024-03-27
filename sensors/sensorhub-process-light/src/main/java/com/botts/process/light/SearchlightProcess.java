/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.light;

import net.opengis.swe.v20.BinaryBlock;
import net.opengis.swe.v20.Boolean;
import net.opengis.swe.v20.Category;
import net.opengis.swe.v20.Time;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.sensorML.SMLUtils;
import org.vast.swe.SWEHelper;

/**
 * Process for performing switching between colors of light on pi-bot
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

    protected static final Logger logger = LoggerFactory.getLogger(
            SearchlightProcess.class);

    private final Boolean inputButton;
    private final Category outputColor;
    private final Time inputTimeStamp;
    private final Time outputTimeStamp;

    public SearchlightProcess() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
        SWEHelper sweFactory = new SWEHelper();


        // Inputs
        inputData.add("button", sweFactory.createRecord()
                .label("Remote Button")
                .addField("time", inputTimeStamp = sweFactory.createTime()
                        .asSamplingTimeIsoUTC()
                        .build())
                .addField("buttonOne", inputButton = sweFactory.createBoolean()
                        .id("IN_BUTTON_ONE")
                        .label("Input Button One")
                        .build())
                .build());


        // Outputs
        outputData.add("searchlight", sweFactory.createRecord()
                .label("SearchlightSensor")
                .addField("time", outputTimeStamp = sweFactory.createTime()
                        .asSamplingTimeIsoUTC()
                        .build())
                .addField("color", outputColor = sweFactory.createCategory()
                        .id("OUTPUT_COLOR")
                        .label("Searchlight Output RBG Color")
                                .addAllowedValues(
                                        SearchlightColorsEnum.OFF.name(),
                                        SearchlightColorsEnum.WHITE.name(),
                                        SearchlightColorsEnum.RED.name(),
                                        SearchlightColorsEnum.MAGENTA.name(),
                                        SearchlightColorsEnum.BLUE.name(),
                                        SearchlightColorsEnum.CYAN.name(),
                                        SearchlightColorsEnum.GREEN.name(),
                                        SearchlightColorsEnum.YELLOW.name(),
                                        SearchlightColorsEnum.UNKNOWN.name()
                                )
                        .build())

                .build());
        
        // set encoding options so that output datablocks are generated correctly
        BinaryBlock dataEncoding = sweFactory.newBinaryBlock();
//        dataEncoding.setCompression("COLOR");
//        new SMLUtils(SMLUtils.V2_1).writeProcess(System.out, , true);

    }

    @Override
    public void init() throws ProcessException {

        logger.debug("Initializing");

        super.init();

        logger.debug("Initialized");
    }

    @Override
    public void execute() {

        logger.debug("Processing event");

        double timeStamp = inputTimeStamp.getValue().getAsDouble() * 1000.0;

        boolean buttonPressed = inputButton.getData().getBooleanValue();
        SearchlightColorsEnum searchlightColor = SearchlightColorsEnum.valueOf(outputColor.getData().getStringValue());

        if(buttonPressed){
            logger.info("button pressed, changing color of searchlight");
            SearchlightColorsEnum changeColor = alternateColors(searchlightColor);

            // update color
            logger.info("setting output color");
            outputColor.getData().setUnderlyingObject(changeColor);

            //copy timestamp for when button is pressed and light changes
            double timestamp = inputTimeStamp.getData().getDoubleValue();
            outputTimeStamp.getData().setDoubleValue(timeStamp);
        }

        logger.debug("Processed event");
    }

    private SearchlightColorsEnum alternateColors(SearchlightColorsEnum current){
        switch(current){
            case RED:
                return SearchlightColorsEnum.BLUE;
            case BLUE:
                return SearchlightColorsEnum.CYAN;
            case CYAN:
                return SearchlightColorsEnum.GREEN;
            case GREEN:
                return SearchlightColorsEnum.WHITE;
            case WHITE:
                return SearchlightColorsEnum.YELLOW;
            case YELLOW:
                return SearchlightColorsEnum.MAGENTA;
            case MAGENTA:
                return SearchlightColorsEnum.RED;
            case UNKNOWN:
                return SearchlightColorsEnum.UNKNOWN;
            default:
                return SearchlightColorsEnum.OFF;
        }

    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
