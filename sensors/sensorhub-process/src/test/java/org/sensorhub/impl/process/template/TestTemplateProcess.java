package org.sensorhub.impl.process.template;


import com.botts.process.template.TemplateProcess;
import net.opengis.gml.v32.impl.ReferenceImpl;
import net.opengis.sensorml.v20.*;
import net.opengis.swe.v20.DataRecord;
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
import org.vast.cdm.common.DataStreamWriter;
import org.vast.sensorML.SMLFactory;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLUtils;
import org.vast.sensorML.SimpleProcessImpl;
import org.vast.swe.SWEHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestTemplateProcess {
    private static final String PROCESS_ID = "PROCESS_SENSOR1";
    static String NAME_OUTPUT1 = "output1";
    static String NAME_INPUT1 = "input1";
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
        var sensorCfg = new SensorConfig();
        sensorCfg.autoStart = false;
        sensorCfg.moduleClass = SearchlightSensor.class.getCanonicalName();
        sensorCfg.id = "SENSOR_01";
        sensorCfg.name = "template_sensor";

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
        String smlUrl = TestTemplateProcess.class.getResource("processchain-template.xml").getFile();
        IProcessModule<?> process = createSMLProcess(smlUrl);
        runProcess(process);
    }

    // USE THIS TO PRINT OUT XML!!!!!!!!!!!!!
    @Test
    public void testTemplateProcess() throws Exception{

        TemplateProcess templateProcess = new TemplateProcess();
        templateProcess.init();

        SMLUtils smlUtils = new SMLUtils(SMLUtils.V2_0);

        SWEHelper swe = new SWEHelper();
        SMLHelper sml = new SMLHelper();
        SMLFactory smlFac = new SMLFactory();


        AggregateProcess aggregate = null;
        SimpleProcessImpl simple = new SimpleProcessImpl();
        simple.setExecutableImpl(templateProcess);


        //set uuid
        String uid = String.valueOf(UUID.randomUUID());

        aggregate = sml.createAggregateProcess()
        .uniqueID(uid)
        .build();

        // set type
        aggregate.setTypeOf(simple.getTypeOf());

        // inputs
        ObservableProperty obs = smlFac.newObservableProperty();;
        obs.setDefinition("http://sensorml.com/ont/swe/property/");

        aggregate.addInput("source", obs);
        aggregate.getInputList().add("input1", "www.", null); //TODO: Ask alex for proper href placeholder!


        // outputs
        // create output record and set description
        DataRecord outputRecord = swe.createRecord()
                .label("Output Data Record")
                .description("Template process")
                .addSamplingTimeIsoUTC("time")
                .addField("output", swe.createCategory())
                .build();


        aggregate.addOutput("outputName", outputRecord);
        aggregate.getOutputList().add("status_info", "", null);

        aggregate.addParameter("samplingPeriod", swe.createQuantity()
                .definition("http://sensorml.com/ont/swe/property/SamplingPeriod")
                .label("Sampling Period")
                .uomCode("s")
                .build());

        PhysicalComponent sensor = smlFac.newPhysicalComponent();
        sensor.setId("sensor");
        sensor.setTypeOf(new ReferenceImpl("")); //xml file for sensor?
        sensor.addOutput("outputSensor", swe.createCategory().build());
        Settings config = smlFac.newSettings();
        config.addSetValue(smlFac.newValueSetting("parameters/samplingRate", "1.0"));
        config.addSetStatus(smlFac.newStatusSetting("parameters/active", Status.ENABLED));
        config.addSetMode(smlFac.newModeSetting("modes/choice1", "highAccuracy"));

        // if necessary, add constraints here is an example
//        AllowedValues newConstraint = SWEHelper.DEFAULT_SWE_FACTORY.newAllowedValues();
//        newConstraint.addValue(Colors.OFF.ordinal());
//        config.addSetConstraint(smlFac.newConstraintSetting("parameters/samplingRate"));

        sensor.setConfiguration(config);

        aggregate.addComponent("component_name",sensor);

        // connections
        Link link = smlFac.newLink();
        link.setSource("components/sensor1/outputs/outputSensor");
        link.setDestination("outputs/outputName/outputSensor");
        aggregate.addConnection(link);

        // button connections?
        Link link1 = smlFac.newLink();
        link1.setSource("components/sensor1/inputs/input1");
        link1.setDestination("inputs/source/input1");
        aggregate.addConnection(link1);

//        smlUtils.makeProcessExecutable((AbstractProcessImpl) process,false);


        // write to byte array
        ByteArrayOutputStream os1 = new ByteArrayOutputStream(10000);
        smlUtils.writeProcess(os1, aggregate, false);

        // read back
        ByteArrayInputStream is = new ByteArrayInputStream(os1.toByteArray());
        aggregate= (AggregateProcess) smlUtils.readProcess(is);

        // write back to byte array
        ByteArrayOutputStream os2 = new ByteArrayOutputStream(10000);
        smlUtils.writeProcess(os2, aggregate, false);

        // write back to sysout
        smlUtils.writeProcess(System.out, aggregate, true);

        System.out.println();
        templateProcess.execute();

    }

}