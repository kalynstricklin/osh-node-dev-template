package org.sensorhub.impl.process.template;

//
//import com.botts.process.light.SearchlightProcess;
//import net.opengis.sensorml.v20.Settings;
//import net.opengis.sensorml.v20.impl.SettingsImpl;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.sensorhub.api.data.DataEvent;
//import org.sensorhub.api.data.IStreamingDataInterface;
//import org.sensorhub.api.event.Event;
//import org.sensorhub.api.event.IEventListener;
//import org.sensorhub.api.module.IModule;
//import org.sensorhub.api.module.ModuleEvent;
//import org.sensorhub.api.processing.IProcessModule;
//import org.sensorhub.api.sensor.ISensorModule;
//import org.sensorhub.api.sensor.SensorConfig;
//import org.sensorhub.impl.SensorHub;
//import org.sensorhub.impl.module.ModuleRegistry;
//import org.sensorhub.impl.processing.SMLProcessConfig;
//import org.sensorhub.impl.processing.SMLProcessImpl;
//import org.sensorhub.impl.sensor.pibot.searchlight.SearchlightSensor;
//import org.vast.cdm.common.DataStreamWriter;
//import org.vast.data.TextEncodingImpl;
//import org.vast.sensorML.AggregateProcessImpl;
//import org.vast.sensorML.LinkImpl;
//import org.vast.sensorML.SMLUtils;
//import org.vast.sensorML.SimpleProcessImpl;
//import org.vast.swe.AsciiDataWriter;
//
//import java.io.IOException;
//import java.util.UUID;
//
//
//public class TestSearchlightProcess implements IEventListener
//{
//    static String SENSOR1_ID = "PI-BOT-SEARCHLIGHT";
//    static String NAME_OUTPUT1 = "color";
//    static String NAME_INPUT1 = "cmd1";
//    static final double SAMPLING_PERIOD = 0.1;
//    static final int SAMPLE_COUNT = 10;
//
//    ModuleRegistry registry;
//    DataStreamWriter writer;
//    volatile int eventCount = 0;
//
//
//    @Before
//    public void setupFramework() throws Exception
//    {
//        // init sensorhub with in-memory config
//        var hub = new SensorHub();
//        hub.start();
//        registry = hub.getModuleRegistry();
//    }
//
//
//    protected ISensorModule<?> createSensorDataSource1() throws Exception
//    {
//        // create test sensor
//        SensorConfig sensorCfg = new SensorConfig();
//        sensorCfg.autoStart = false;
//        sensorCfg.moduleClass = SearchlightSensor.class.getCanonicalName();
//        sensorCfg.id = SENSOR1_ID;
//        sensorCfg.name = "Sensor1";
//        IModule<?> sensor = registry.loadModule(sensorCfg);
//        sensor.init();
////        var sensorOutput = new SearchlightColorProcess((SearchlightSensor)sensor, NAME_OUTPUT1, SAMPLING_PERIOD, SAMPLE_COUNT);
////        ((SearchlightSensor)sensor).setDataInterfaces(sensorOutput);
//        sensor.start();
//        return (SearchlightSensor)sensor;
//    }
//
//
//    protected void runProcess(IProcessModule<?> process) throws Exception
//    {
//        // prepare event writer
//        writer = new AsciiDataWriter();
//        writer.setDataEncoding(new TextEncodingImpl(",", ""));
//        writer.setOutput(System.out);
//
//        process.start();
//        //new SMLUtils(SMLUtils.V2_0).writeProcess(System.out, process.getCurrentDescription(), true);
//        for (IStreamingDataInterface output: process.getOutputs().values()) output.registerListener(this);
////        ((SearchlightSensor)registry.getModuleById(SENSOR1_ID)).startSendingData(100);
//
//        long t0 = System.currentTimeMillis();
//        synchronized (this)
//        {
//            while (eventCount < SAMPLE_COUNT)
//            {
//                if (System.currentTimeMillis() - t0 >= 10000L)
//                    Assert.fail("No data received before timeout");
//                wait(1000L);
//            }
//        }
//
//        System.out.println();
//    }
//
//
//    protected IProcessModule<?> createSMLProcess(String smlUrl) throws Exception
//    {
//        SMLProcessConfig processCfg = new SMLProcessConfig();
//        processCfg.autoStart = false;
//        processCfg.name = "SensorML Process #1";
//        processCfg.moduleClass = SMLProcessImpl.class.getCanonicalName();
//        processCfg.sensorML = smlUrl;
//
//        @SuppressWarnings("unchecked")
//        IProcessModule<SMLProcessConfig> process = (IProcessModule<SMLProcessConfig>)registry.loadModule(processCfg);
//        process.init();
//        for (IStreamingDataInterface output: process.getOutputs().values())
//            output.registerListener(this);
//
//        process.waitForState(ModuleEvent.ModuleState.INITIALIZED, 5000);
//        return process;
//    }
//
//
//    @Test
//    public void testSMLSimpleProcess() throws Exception
//    {
//        createSensorDataSource1();
//        String smlUrl = TestSearchlightProcess.class.getResource("/processchain-template.xml").getFile();
//        IProcessModule<?> process = createSMLProcess(smlUrl);
//        runProcess(process);
//    }
//
//
//    @Test
//    public void testSMLSimpleProcessWithOutputDataStream() throws Exception
//    {
//        createSensorDataSource1();
//        String smlUrl = TestSearchlightProcess.class.getResource("/processchain-template.xml").getFile();
//        IProcessModule<?> process = createSMLProcess(smlUrl);
//        runProcess(process);
//    }
//
//
//    @Test
//    public void testSMLSimpleProcessWithOutputDataInterface() throws Exception
//    {
//        createSensorDataSource1();
//        String smlUrl = TestSearchlightProcess.class.getResource("/processchain-template.xml").getFile();
//        IProcessModule<?> process = createSMLProcess(smlUrl);
//        runProcess(process);
//    }
//
//
//    @Override
//    public void handleEvent(Event e)
//    {
//        if (e instanceof DataEvent)
//        {
//            try
//            {
//                System.out.print(((DataEvent)e).getSource().getName() + ": ");
//
//                writer.setDataComponents(((DataEvent)e).getSource().getRecordDescription());
//                writer.reset();
//                writer.write(((DataEvent)e).getRecords()[0]);
//                writer.flush();
//                System.out.println();
//
//                eventCount++;
//                System.out.println(eventCount);
//            }
//            catch (IOException ex)
//            {
//                ex.printStackTrace();
//            }
//
//            synchronized (this) { this.notify(); }
//        }
//    }
//
//
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
//
//
//    @Test
//    public void testSearchlightProcess() throws Exception
//    {
//        SearchlightProcess searchlightProcess= new SearchlightProcess();
//        searchlightProcess.init();
//
//        SMLUtils smlHelper = new SMLUtils(SMLUtils.V2_0);
//
//        SimpleProcessImpl simple = new SimpleProcessImpl();
//        simple.setExecutableImpl(searchlightProcess);
//
//        // serialize
//        AggregateProcessImpl process = new AggregateProcessImpl();
//
//        // set type
//        process.setTypeOf(simple.getTypeOf());
//        smlHelper.makeProcessExecutable(process,false);
//
//        //set uuid
//        process.setUniqueIdentifier(UUID.randomUUID().toString());
//
//
//        // set inputs and outputs
//        process.addInput("valueIn", searchlightProcess.getInputList().getComponent(0)); //wii
//        process.addOutput("valueOut", searchlightProcess.getOutputList().getComponent(0)); //searchlight
//
//
//        // wiimote Components
//        SimpleProcessImpl buttonSource = new SimpleProcessImpl();
//        buttonSource.setExecutableImpl(searchlightProcess);
//        buttonSource.setUniqueIdentifier("urn:osh:sensor:wii001");
//        // wiimote button config
//        Settings buttonConfig = new SettingsImpl();
//        buttonConfig.addSetValue("parameters/systemUID", "urn:osh:sensor:wii001");
//        buttonConfig.addSetValue("parameters/outputName", "buttonState");
//        buttonSource.setConfiguration(buttonConfig);
//        process.addComponent("source0", buttonSource);
//
//        //process component
//        SimpleProcessImpl processSource = new SimpleProcessImpl();
//        processSource.setExecutableImpl(searchlightProcess);
//        buttonSource.setUniqueIdentifier("urn:osh:process:pibot:SearchLight");
//        Settings processConfig = new SettingsImpl();
//        processConfig.addSetValue("parameters/button", "false");
//        processConfig.addSetValue("parameters/color", "UNKNOWN");
//
//
//        // searchlight component
//        SimpleProcessImpl searchlight = new SimpleProcessImpl();
//        searchlight.setExecutableImpl(searchlightProcess);
//        searchlight.setUniqueIdentifier("urn:osh:process:pibot:SearchLight");
//        // searchlight config
//        Settings colorConfig = new SettingsImpl();
//        buttonConfig.addSetValue("parameters/systemUID", "urn:osh:sensor:wii001");
//        buttonConfig.addSetValue("parameters/inputName", "color");
//        searchlight.setConfiguration(colorConfig);
//        process.addComponent("light", searchlight);
//
//        // add connection links
//        LinkImpl bLink = new LinkImpl(); //link wii output buttons to process input
//        LinkImpl link = new LinkImpl();  // link process input to process output controls?
//        LinkImpl cLink = new LinkImpl(); // link process output to searchlight controls
//
//        bLink.setSource("components/source0/outputs/Buttons/buttonOne");
//        bLink.setDestination("components/buttonSource/inputs/button1");
//
//        link.setSource("components/source0/outputs/button1"); //connect process to control input (button one state = true then send commands...)
//        link.setDestination("components/light/inputs/cmd1/Color"); //send button output to searchlight control
//
//        cLink.setSource("components/source0/outputs/button1");
//        cLink.setDestination("outputs/valueOut");   //send output to output process
//
//        process.addConnection(bLink);
//        process.addConnection(link);
//        process.addConnection(cLink);
//
//        smlHelper.writeProcess(System.out, process, true);
//
//        searchlightProcess.execute();
//    }
//
//}