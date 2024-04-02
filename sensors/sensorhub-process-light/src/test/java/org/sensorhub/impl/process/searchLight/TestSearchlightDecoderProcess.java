package org.sensorhub.impl.process.searchLight;


import com.botts.process.light.SearchlightProcess;
import net.opengis.sensorml.v20.Settings;
import net.opengis.sensorml.v20.SimpleProcess;
import net.opengis.sensorml.v20.impl.SettingsImpl;
import org.junit.Assert;
import org.junit.Test;
import org.sensorhub.api.data.IStreamingDataInterface;
import org.sensorhub.api.processing.IProcessModule;
import org.sensorhub.api.sensor.ISensorModule;
import org.sensorhub.api.sensor.SensorConfig;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.processing.SMLProcessConfig;
import org.sensorhub.impl.processing.SMLProcessImpl;
import org.sensorhub.impl.sensor.pibot.searchlight.SearchlightConfig;
import org.sensorhub.impl.sensor.pibot.searchlight.SearchlightSensor;
import org.vast.sensorML.AggregateProcessImpl;
import org.vast.sensorML.LinkImpl;
import org.vast.sensorML.SMLUtils;
import org.vast.sensorML.SimpleProcessImpl;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSearchlightDecoderProcess {
    private static final String SEARCHLIGHT_ID = "SEARCHLIGHT_SENSOR1";
    static String NAME_OUTPUT1 = "color";
    static String NAME_INPUT1 = "buttonOne";
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

    protected static ISensorModule<?> createSensorDataSource1() throws Exception
    {
        // create test sensor
        var sensorCfg = new SearchlightConfig();
        sensorCfg.autoStart = false;
        sensorCfg.moduleClass = SearchlightSensor.class.getCanonicalName();
        sensorCfg.id = "PI_SEARCHLIGHT";
        sensorCfg.name = "searchlgiht";

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
        SearchlightProcess p = new SearchlightProcess();
        p.init();

        SMLUtils smlHelper = new SMLUtils(SMLUtils.V2_0);

        SimpleProcessImpl simple = new SimpleProcessImpl();
        simple.setExecutableImpl(p);

        // serialize
        AggregateProcessImpl wp = new AggregateProcessImpl();

        // set type
        wp.setTypeOf(simple.getTypeOf());
        smlHelper.makeProcessExecutable(wp,false);

        //set uuid
        wp.setUniqueIdentifier(UUID.randomUUID().toString());


        // set inputs and outputs
        wp.addOutput("valueOut", p.getOutputList().getComponent(0));
        wp.addInput("valueIn", p.getInputList().getComponent(0));

        // wiimote Components
        SimpleProcess buttonSource = new SimpleProcessImpl();
        buttonSource.setUniqueIdentifier("urn:osh:sensor:wii001");
        // wiimote button config
        Settings buttonConfig = new SettingsImpl();
        buttonConfig.addSetValue("buttonState", "false");
//        buttonSource.setTypeOf();
        buttonSource.setConfiguration(buttonConfig);
        wp.addComponent("source0", buttonSource);

        // searchlight component
        SimpleProcess searchlight = new SimpleProcessImpl();
        searchlight.setUniqueIdentifier("urn:osh:process:pibot:SearchLight");
//        searchlight.getTypeOf();
        // searchlight config
        Settings colorConfig = new SettingsImpl();
        colorConfig.addSetValue("color", "OFF");
        searchlight.setConfiguration(colorConfig);
        wp.addComponent("decoder", searchlight);

        // add connection links
        LinkImpl buttonLink = new LinkImpl();
        buttonLink.setSource("components/source0/outputs/button/buttonOne");
        buttonLink.setDestination("components/inputs/button/one");
        wp.addConnection(buttonLink);

        LinkImpl colorLink = new LinkImpl();
        colorLink.setSource("components/decoder/outputs/searchlight/Color");
        colorLink.setDestination("outputs/valueOut");
        wp.addConnection(colorLink);

        LinkImpl timeLink = new LinkImpl();
        timeLink.setSource("components/buttonSRC/inputs/valueIn/time");
        timeLink.setDestination("components/searchlightSRC/outputs/valueOut/time");
        wp.addConnection(timeLink);

        smlHelper.writeProcess(System.out, wp, true);

        p.execute();
    }

//    @After
//    public void cleanup()
//    {
//        try
//        {
//            registry.shutdown(false, false);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }

    protected ISensorModule<?> createSensor() throws Exception
    {
        // create test sensor
        SensorConfig sensorCfg = new SensorConfig();
        sensorCfg.autoStart = false;
        sensorCfg.moduleClass = SearchlightSensor.class.getCanonicalName();
        sensorCfg.id = SEARCHLIGHT_ID;
        sensorCfg.name = "Sensor1";
        IModule<?> sensor = registry.loadModule(sensorCfg);
        sensor.init();
        var sensorOutput = new FakeSensorData((FakeSensor)sensor, NAME_OUTPUT1, SAMPLING_PERIOD, SAMPLE_COUNT);
        ((FakeSensor)sensor).setDataInterfaces(sensorOutput);
        var controlInput = new FakeSensorControl1((FakeSensor)sensor, NAME_INPUT1);
        ((FakeSensor)sensor).setControlInterfaces(controlInput);
        controlInput.registerListener(this);
        sensor.start();
        return (FakeSensor)sensor;
    }

    @Test
    public void testSMLSimpleProcess() throws Exception
    {
        createSensor();
        String smlUrl = TestCommandSinkProcess.class.getResource("/test-processchain-controlloop.xml").getFile();
        IProcessModule<?> process = createSMLProcess(smlUrl);
        runProcess(process);
    }

}