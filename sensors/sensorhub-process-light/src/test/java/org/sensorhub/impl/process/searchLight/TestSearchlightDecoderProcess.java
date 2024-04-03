package org.sensorhub.impl.process.searchLight;


import com.botts.process.light.SearchlightColorProcess;
import com.botts.process.light.SearchlightProcess;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.sensorml.v20.Settings;
import net.opengis.sensorml.v20.SimpleProcess;
import net.opengis.sensorml.v20.impl.SettingsImpl;
import org.junit.Assert;
import org.junit.Test;
import org.sensorhub.api.data.IStreamingDataInterface;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.processing.IProcessModule;
import org.sensorhub.api.sensor.ISensorModule;
import org.sensorhub.api.sensor.SensorConfig;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.processing.SMLProcessConfig;
import org.sensorhub.impl.processing.SMLProcessImpl;
import org.sensorhub.impl.sensor.pibot.searchlight.SearchlightConfig;
import org.sensorhub.impl.sensor.pibot.searchlight.SearchlightSensor;
import org.vast.cdm.common.DataStreamWriter;
import org.vast.sensorML.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSearchlightDecoderProcess {
    private static final String SEARCHLIGHT_ID = "SEARCHLIGHT_SENSOR1";
    static String NAME_OUTPUT1 = "color";
    static String NAME_INPUT1 = "buttonOne";
    static ModuleRegistry registry;
    static final double SAMPLING_PERIOD = 0.1;
    static final int SAMPLE_COUNT = 10;
    DataStreamWriter writer;

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

    protected static ISensorModule<?> createSensorDataSource1() throws Exception
    {
        // create test sensor
        var sensorCfg = new SearchlightConfig();
        sensorCfg.autoStart = false;
        sensorCfg.moduleClass = SearchlightSensor.class.getCanonicalName();
        sensorCfg.id = "PI_SEARCHLIGHT";
        sensorCfg.name = "searchlight";

        var sensor = registry.loadModule(sensorCfg);
        sensor.init();
        sensor.start();
        return (SearchlightSensor)sensor;
    }


    public static void main(String[] args) throws Exception
    {
        // init sensorhub with in-memory config
        var hub = new SensorHub();
        hub.start();
        registry = hub.getModuleRegistry();

        createSensorDataSource1();
        String smlUrl = TestSearchlightDecoderProcess.class.getResource("processchain-search-light.xml").getFile();
        IProcessModule<?> process = createSMLProcess(smlUrl);
        runProcess(process);
    }
    @Test
    public void testSearchlightProcess() throws Exception
    {
        SearchlightProcess searchlightProcess= new SearchlightProcess();
        searchlightProcess.init();

        SMLUtils smlHelper = new SMLUtils(SMLUtils.V2_0);

        SimpleProcessImpl simple = new SimpleProcessImpl();
        simple.setExecutableImpl(searchlightProcess);

        // serialize
        AggregateProcessImpl process = new AggregateProcessImpl();

        // set type
        process.setTypeOf(simple.getTypeOf());
        smlHelper.makeProcessExecutable(process,false);

        //set uuid
        process.setUniqueIdentifier(UUID.randomUUID().toString());


        // set inputs and outputs
        process.addInput("valueIn", searchlightProcess.getInputList().getComponent(0)); //wii
        process.addOutput("valueOut", searchlightProcess.getOutputList().getComponent(0)); //searchlight


        // wiimote Components
        SimpleProcessImpl buttonSource = new SimpleProcessImpl();
        buttonSource.setExecutableImpl(searchlightProcess);
        buttonSource.setUniqueIdentifier("urn:osh:sensor:wii001");
        // wiimote button config
        Settings buttonConfig = new SettingsImpl();
        buttonConfig.addSetValue("parameters/systemUID", "urn:osh:sensor:wii001");
        buttonConfig.addSetValue("parameters/outputName", "buttonState");
        buttonSource.setConfiguration(buttonConfig);
        process.addComponent("source0", buttonSource);

        //process component
        SimpleProcessImpl processSource = new SimpleProcessImpl();
        processSource.setExecutableImpl(searchlightProcess);
        buttonSource.setUniqueIdentifier("urn:osh:process:pibot:SearchLight");
        Settings processConfig = new SettingsImpl();
        processConfig.addSetValue("parameters/button", "false");
        processConfig.addSetValue("parameters/color", "UNKNOWN");


        // searchlight component
        SimpleProcessImpl searchlight = new SimpleProcessImpl();
        searchlight.setExecutableImpl(searchlightProcess);
        searchlight.setUniqueIdentifier("urn:osh:process:pibot:SearchLight");
        // searchlight config
        Settings colorConfig = new SettingsImpl();
        buttonConfig.addSetValue("parameters/systemUID", "urn:osh:sensor:wii001");
        buttonConfig.addSetValue("parameters/inputName", "color");
        searchlight.setConfiguration(colorConfig);
        process.addComponent("light", searchlight);

        // add connection links
        LinkImpl bLink = new LinkImpl(); //link wii output buttons to process input
        LinkImpl link = new LinkImpl();  // link process input to process output controls?
        LinkImpl cLink = new LinkImpl(); // link process output to searchlight controls


        bLink.setSource("components/source0/outputs/Buttons/buttonOne");
        bLink.setDestination("components/buttonSource/inputs/button1");

        link.setSource("components/source0/outputs/button1"); //connect process to control input (button one state = true then send commands...)
        link.setDestination("components/light/inputs/cmd1/Color"); //send button output to searchlight control

        cLink.setSource("components/source0/outputs/button1");
        cLink.setDestination("outputs/valueOut");   //send output to output process


        process.addConnection(bLink);
        process.addConnection(link);
        process.addConnection(cLink);

        smlHelper.writeProcess(System.out, process, true);

        searchlightProcess.execute();
    }

}