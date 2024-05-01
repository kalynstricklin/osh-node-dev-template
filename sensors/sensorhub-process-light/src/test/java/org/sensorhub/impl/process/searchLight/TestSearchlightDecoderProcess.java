package org.sensorhub.impl.process.searchLight;


import com.botts.process.light.SearchlightProcess;
import net.opengis.gml.v32.impl.ReferenceImpl;
import net.opengis.sensorml.v20.*;
import net.opengis.swe.v20.DataRecord;
import org.junit.Assert;
import org.junit.Test;
import org.sensorhub.api.data.IStreamingDataInterface;
import org.sensorhub.api.processing.IProcessModule;
import org.sensorhub.api.sensor.ISensorModule;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.processing.SMLProcessConfig;
import org.sensorhub.impl.processing.SMLProcessImpl;
import org.sensorhub.impl.sensor.pibot.searchlight.SearchlightConfig;
import org.sensorhub.impl.sensor.pibot.searchlight.SearchlightSensor;
import org.vast.cdm.common.DataStreamWriter;
import org.vast.process.ProcessException;
import org.vast.sensorML.SMLFactory;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLUtils;
import org.vast.sensorML.SimpleProcessImpl;
import org.vast.swe.SWEHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSearchlightDecoderProcess {
    SearchlightProcess searchlightProcess= new SearchlightProcess();

    private static final String SEARCHLIGHT_ID = "SEARCHLIGHT_SENSOR1";
    static String NAME_OUTPUT1 = "color";
    static String NAME_INPUT1 = "buttonOne";
    static ModuleRegistry registry;
    static final double SAMPLING_PERIOD = 0.1;
    static final int SAMPLE_COUNT = 10;
    DataStreamWriter writer;
    enum Colors {OFF, WHITE, RED, MAGENTA, BLUE, CYAN, GREEN, YELLOW, UNKNOWN}

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

    // USE THIS TO PRINT OUT XML!!!!!!!!!!!!!
    @Test
    public void testSearchlightInstance() throws Exception{

        searchlightProcess.init();

        SMLUtils smlUtils = new SMLUtils(SMLUtils.V2_0);

        SWEHelper swe = new SWEHelper();
        SMLHelper sml = new SMLHelper();
        SMLFactory smlFac = new SMLFactory();


        // serialize
        AggregateProcess process = null;
        SimpleProcessImpl simple = new SimpleProcessImpl();
        simple.setExecutableImpl(searchlightProcess);


        //set uuid
        String uid = String.valueOf(UUID.randomUUID());

        process = sml.createAggregateProcess()
        .uniqueID(uid)
        .build();

        // set type
        process.setTypeOf(simple.getTypeOf());

        // inputs
        ObservableProperty obs = smlFac.newObservableProperty();;
        obs.setDefinition("http://sensorml.com/ont/swe/property/Color");
//        system.addInput("buttons", obs);
//        system.getInputList().add("button1", "https://www.nintendo.com/us/store/products/wii-remote", null);

        process.addInput("buttons", obs);
        process.getInputList().add("button1", "urn:osh:process:datasource:stream", null); //TODO: Ask alex for proper placeholder!


        // outputs
        // create output record and set description
        DataRecord dataRecord = swe.createRecord()
                .label("Searchlight Data Record")
                .description("Pi-bot searchlight process")
                .addSamplingTimeIsoUTC("time")
                .addField("color", swe.createCategory()
                        .definition(SWEHelper.getPropertyUri("Color"))
                        .label("RGBColor")
                        .addAllowedValues(
                                Colors.OFF.name(),
                                Colors.WHITE.name(),
                                Colors.RED.name(),
                                Colors.MAGENTA.name(),
                                Colors.BLUE.name(),
                                Colors.CYAN.name(),
                                Colors.GREEN.name(),
                                Colors.YELLOW.name(),
                                Colors.UNKNOWN.name())
                        .build())
                .build();


//        system.addOutput("color", dataRecord);
//        system.getOutputList().add("status_info", "", null);
//
//        system.addParameter("samplingPeriod", swe.createQuantity()
//                .definition("http://sensorml.com/ont/swe/property/SamplingPeriod")
//                .label("Sampling Period")
//                .uomCode("s")
//                .build());

        process.addOutput("color", dataRecord);
        process.getOutputList().add("status_info", "", null);

        process.addParameter("samplingPeriod", swe.createQuantity()
                .definition("http://sensorml.com/ont/swe/property/SamplingPeriod")
                .label("Sampling Period")
                .uomCode("s")
                .build());

        PhysicalComponent sensor = smlFac.newPhysicalComponent();
        sensor.setId("SEARCHLIGHT01");
        sensor.setTypeOf(new ReferenceImpl("urn:osh:process:pi-bot:searchlight")); //TODO: Ask alex for proper placeholder!
        sensor.addOutput("colorLight", swe.createCategory().build());
        Settings config = smlFac.newSettings();
        config.addSetValue(smlFac.newValueSetting("parameters/samplingRate", "1.0"));
        config.addSetStatus(smlFac.newStatusSetting("parameters/active", Status.ENABLED));
        config.addSetMode(smlFac.newModeSetting("modes/choice1", "highAccuracy"));
//        AllowedValues newConstraint = SWEHelper.DEFAULT_SWE_FACTORY.newAllowedValues();
//        newConstraint.addValue(Colors.OFF.ordinal());
//        config.addSetConstraint(smlFac.newConstraintSetting("parameters/samplingRate"));
        sensor.setConfiguration(config);

//        system.addComponent("searchlight1", sensor);
        process.addComponent("searchlight1",sensor);

        // connections
        Link link = smlFac.newLink();
        link.setSource("components/sensor1/outputs/colorLight");
        link.setDestination("outputs/color/colorLight");
//        system.addConnection(link);
        process.addConnection(link);

        // button connections?
        Link blink = smlFac.newLink();
        blink.setSource("components/sensor1/inputs/button1");
        blink.setDestination("inputs/buttons/button1");
//        system.addConnection(blink);

        process.addConnection(blink);

//        smlUtils.makeProcessExecutable((AbstractProcessImpl) process,false);



        // write to byte array
        ByteArrayOutputStream os1 = new ByteArrayOutputStream(10000);
//        smlUtils.writeProcess(os1, system, false);
        smlUtils.writeProcess(os1, process, false);

        // read back
        ByteArrayInputStream is = new ByteArrayInputStream(os1.toByteArray());
//        system = (PhysicalSystem)smlUtils.readProcess(is);
        process = (AggregateProcess) smlUtils.readProcess(is);

        // write back to byte array
        ByteArrayOutputStream os2 = new ByteArrayOutputStream(10000);
//        smlUtils.writeProcess(os2, system, false);
        smlUtils.writeProcess(os2, process, false);

        // write back to sysout
//        smlUtils.writeProcess(System.out, system, true);
        smlUtils.writeProcess(System.out, process, true);


        System.out.println();
        searchlightProcess.execute();

    }

    /**
     * the wii remote button is used as input to set the color of the searchlight
     */
    void setTargetButton(){
        var buttonPressed = searchlightProcess.getInputList().getComponent("button1");
        buttonPressed.assignNewDataBlock();
        buttonPressed.getData().setBooleanValue(0,true);

    }

    double getColorOutput(){
        var color = searchlightProcess.getOutputList().getComponent("color");
        return color.getData().getDoubleValue(2); //index 2 = red on allowable values...
    }
    @Test
    public void testColorChange() throws ProcessException{

        setTargetButton();
        searchlightProcess.execute();
        var colorOut = getColorOutput();
    }
}