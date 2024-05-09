package org.sensorhub.impl.process.searchLight;


import com.botts.process.light.SearchlightProcess;
import net.opengis.gml.v32.Reference;
import net.opengis.gml.v32.impl.ReferenceImpl;
import net.opengis.sensorml.v20.ObservableProperty;
import net.opengis.sensorml.v20.Settings;
import net.opengis.sensorml.v20.SimpleProcess;
import net.opengis.sensorml.v20.impl.SettingsImpl;
import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.DataStream;
import org.junit.Assert;
import org.junit.Test;
import org.sensorhub.api.data.IStreamingDataInterface;
import org.sensorhub.api.processing.IProcessModule;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.processing.SMLProcessConfig;
import org.sensorhub.impl.processing.SMLProcessImpl;
import org.vast.process.ExecutableChainImpl;
import org.vast.sensorML.*;
import org.vast.swe.SWEHelper;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class TestSearchlightProcess
{
    static String SENSOR1_ID = "PI-BOT-SEARCHLIGHT";

    static ModuleRegistry registry;

    protected static void runProcess(IProcessModule<?> process) throws Exception
    {
        AtomicInteger counter = new AtomicInteger();

        for (IStreamingDataInterface output: process.getOutputs().values())
            output.registerListener(e -> {
                //System.out.println(e.getTimeStamp() + ": " + ((DataEvent)e).getRecords()[0].getAtomCount());
                counter.incrementAndGet();
            });

        process.start();

        long t0 = System.currentTimeMillis();
        while (counter.get() < 85-12+1)
        {
            if (System.currentTimeMillis() - t0 >= 100000L)
                Assert.fail("No data received before timeout");
            Thread.sleep(100);
        }

        System.out.println();
    }
    protected static IProcessModule<?> createSMLProcess(String smlUrl) throws Exception
    {
        SMLProcessConfig processCfg = new SMLProcessConfig();
        processCfg.autoStart = false;
        processCfg.name = "SensorML Process #1";
        processCfg.moduleClass = SMLProcessImpl.class.getCanonicalName();
        processCfg.sensorML = smlUrl;

        @SuppressWarnings("unchecked")
        IProcessModule<SMLProcessConfig> process = (IProcessModule<SMLProcessConfig>)registry.loadModule(processCfg);
        process.init();
        return process;
    }





    @Test
    public void testSearchProcess() throws Exception{

        SearchlightProcess searchlightProcess = new SearchlightProcess();
        searchlightProcess.init();
        System.out.println(searchlightProcess.getProcessInfo().getUri());

        SMLUtils smlHelper = new SMLUtils(SMLUtils.V2_0);
        SWEHelper swe = new SWEHelper();
        SMLFactory smlFac = new SMLFactory();

        SimpleProcessImpl simple = new SimpleProcessImpl();
        Reference processRef = new ReferenceImpl();
        processRef.setHref("urn:osh:process:pi-bot:searchlight");
        simple.setExecutableImpl(searchlightProcess);

        //serialize
        AggregateProcessImpl aggregate = new AggregateProcessImpl();
//        smlHelper.makeProcessExecutable(aggregate, false);
//        aggregate.setExecutableImpl(simple);

        // set type
        aggregate.setUniqueIdentifier(UUID.randomUUID().toString());
//        aggregate.setTypeOf(simple.getTypeOf());

       // set output for process :0

//        DataStream stream = swe.
//
//
//        DataRecord rec = swe.createRecord()
//                .addField("light", swe.createQuantity()
//                        .definition("http://sensorml.com/ont/swe/property/DN")
//                        .uom("http://sensorml.com/ont/swe/uom/Any")
//                        .build())
//                .build();
//        aggregate.addOutput("light", rec);



        //COMPONENT LIST
        // wii remote source0
        SimpleProcess wii = new SimpleProcessImpl();
        Reference sourceRef = new ReferenceImpl();
        sourceRef.setHref("urn:osh:process:datasource:stream");
        wii.setTypeOf(sourceRef);
        Settings sourceConfig = new SettingsImpl();
        sourceConfig.addSetValue("parameters/producerURI","urn:osh:sensor:wii001");
//        sourceConfig.addSetValue("parameters/outputName","output1");
        wii.setConfiguration(sourceConfig);
        aggregate.addComponent("source0", wii);

        // process component
        aggregate.addComponent("process0", simple);

        // searchlight pi-bot command stream
        SimpleProcess sink = new SimpleProcessImpl();
        Reference sinkRef = new ReferenceImpl();
        sinkRef.setHref("urn:osh:process:datasink:commandstream");
        sink.setTypeOf(sinkRef);
        Settings commandConfig = new SettingsImpl();
        commandConfig.addSetValue("parameters/systemUID","urn:pibot:searchlight:rgb-searchlight");
        commandConfig.addSetValue("parameters/inputName","SearchlightControl");
        sink.setConfiguration(commandConfig);
        aggregate.addComponent("sink0", sink);


        // connections
        LinkImpl inputToProcess = new LinkImpl();
        inputToProcess.setSource("components/source0/outputs/output1/Remote/Buttons/1");
        inputToProcess.setDestination("components/process0/inputs/buttons/button1");

        LinkImpl process = new LinkImpl();
        process.setSource("components/process0/outputs/colors/color");
        process.setDestination("components/sink0/inputs/SearchlightControl/Color");


        LinkImpl outputToCommand = new LinkImpl();
        outputToCommand.setSource("components/process0/outputs/colors/color");
        outputToCommand.setDestination("outputs/color");


        // add connections
        aggregate.addConnection(inputToProcess);
        aggregate.addConnection(process);
        aggregate.addConnection(outputToCommand);

        smlHelper.writeProcess(System.out, aggregate, true);

        searchlightProcess.execute();

    }

}