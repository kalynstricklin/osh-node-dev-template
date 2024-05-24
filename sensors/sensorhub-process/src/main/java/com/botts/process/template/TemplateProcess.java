package com.botts.process.template;

import net.opengis.swe.v20.*;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.sensorhub.impl.system.CommandStreamTransactionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.swe.SWEHelper;

import java.util.Arrays;

public class TemplateProcess extends ExecutableProcessImpl {
    protected static final Logger logger = LoggerFactory.getLogger(TemplateProcess.class);

    public static final OSHProcessInfo INFO = new OSHProcessInfo("urn:osh:process", "template", null, TemplateProcess.class);
    public static final String SYSTEM_UID_PARAM = "systemUID";
    public static final String TEMPLATE_PROC_UID = "urn:sensors:process-template";

    private Time inputTimeStamp;
    private Time outputTimeStamp;
    Text systemUidParam;
    Category modeParam;
    DataRecord output;


    public TemplateProcess() {

        super(INFO);

        // Get an instance of SWE Factory suitable to build components
        SWEHelper fac = new SWEHelper();

        // Inputs
        inputData.add("inputs", fac.createRecord()
                .name("input")
                .addField("time", inputTimeStamp = fac.createTime()
                        .definition(SWEHelper.getPropertyUri("SamplingTime"))
                        .asSamplingTimeIsoUTC()
                        .build())
                .build());


        //parameters
        paramData.add("params", modeParam =fac.createCategory()
                .definition(SWEHelper.getPropertyUri("ModeID"))
                .build());

        systemUidParam = fac.createText()
                .definition(SWEHelper.getPropertyUri("SystemUID"))
                .label("Producer Unique ID")
                .build();
        paramData.add(SYSTEM_UID_PARAM, systemUidParam);


        // Outputs
        outputData.add("outputs", output = fac.createRecord()
                .name("output")
                .addField("time", outputTimeStamp = fac.createTime()
                        .definition(SWEHelper.getPropertyUri("SamplingTime"))
                        .asSamplingTimeIsoUTC()
                        .build())
                // Add output fields
                .build());

        // set encoding options so that output data blocks are generated correctly
        BinaryBlock dataEncoding = fac.newBinaryBlock();

    }

    @Override
    public void init() throws ProcessException {

        logger.debug("Initializing");

        super.init();
        // do any initializing for process here

        logger.debug("Initialized");
    }


    @Override
    public void dispose() {
        super.dispose();
    }


    @Override
    public void execute() {
        logger.debug("Processing event");

         // send to output
        output.getData().setDoubleValue(0, 0); // should be similar to this

        logger.debug("Processed event");
    }

}
