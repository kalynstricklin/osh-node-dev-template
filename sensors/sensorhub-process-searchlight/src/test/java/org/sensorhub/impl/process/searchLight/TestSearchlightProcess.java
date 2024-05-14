package org.sensorhub.impl.process.searchLight;


import com.botts.process.light.SearchlightProcess;
import net.opengis.gml.v32.Reference;
import net.opengis.gml.v32.impl.ReferenceImpl;
import net.opengis.sensorml.v20.Settings;
import net.opengis.sensorml.v20.SimpleProcess;
import net.opengis.sensorml.v20.impl.SettingsImpl;
import net.opengis.swe.v20.Category;
import net.opengis.swe.v20.DataRecord;
import org.junit.Assert;
import org.junit.Test;
import org.sensorhub.api.data.IStreamingDataInterface;
import org.sensorhub.api.processing.IProcessModule;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.processing.SMLProcessConfig;
import org.sensorhub.impl.processing.SMLProcessImpl;
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

        // set type
        aggregate.setUniqueIdentifier(UUID.randomUUID().toString());


       // set output for process :0
        Category output = swe.createCategory()
                        .definition(SWEHelper.getPropertyUri("Color"))
                        .label("searchlight")
                        .build();

        aggregate.addOutput("SearchlightColor", output);



        //COMPONENT LIST
        // universal remote
        SimpleProcess remote = new SimpleProcessImpl();
        Reference sourceRef = new ReferenceImpl();
        sourceRef.setHref("urn:osh:process:datasource:stream");
        remote.setTypeOf(sourceRef);
        Settings sourceConfig = new SettingsImpl();
        sourceConfig.addSetValue("parameters/producerURI","urn:osh:sensor:universalcontroller");
        remote.setConfiguration(sourceConfig);
        aggregate.addComponent("source0", remote);

        // process component
        aggregate.addComponent("process0", simple);
        // pi-bot command stream
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
        LinkImpl inputButton = new LinkImpl();
        inputButton.setSource("components/source0/outputs/output1/gamepads/gamepad0/A");
        inputButton.setDestination("components/process0/inputs/buttonA");


        LinkImpl process = new LinkImpl();
        process.setSource("components/process0/outputs/process/Color");
        process.setDestination("components/sink0/inputs/SearchlightControl/Color");


        LinkImpl outputToCommand = new LinkImpl();
        outputToCommand.setSource("components/process0/outputs/process/Color");
        outputToCommand.setDestination("outputs/SearchlightColor");


        // add connections
        aggregate.addConnection(inputButton);
        aggregate.addConnection(process);
        aggregate.addConnection(outputToCommand);

        smlHelper.writeProcess(System.out, aggregate, true);

        searchlightProcess.execute();

    }

}