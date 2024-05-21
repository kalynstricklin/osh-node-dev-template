package org.sensorhub.impl.process.drive;


import com.botts.process.camera.PiBotCameraProcess;
import net.opengis.gml.v32.Reference;
import net.opengis.gml.v32.impl.ReferenceImpl;
import net.opengis.sensorml.v20.Settings;
import net.opengis.sensorml.v20.SimpleProcess;
import net.opengis.sensorml.v20.impl.SettingsImpl;
import net.opengis.swe.v20.Quantity;
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


public class TestPiBotCameraProcess
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

        PiBotCameraProcess driveProcess = new PiBotCameraProcess();
        driveProcess.init();
        System.out.println(driveProcess.getProcessInfo().getUri());

        SMLUtils smlHelper = new SMLUtils(SMLUtils.V2_0);
        SWEHelper swe = new SWEHelper();
        SMLFactory smlFac = new SMLFactory();

        SimpleProcessImpl simple = new SimpleProcessImpl();
        Reference processRef = new ReferenceImpl();
        processRef.setHref("urn:osh:process:pi-bot:camera");
        simple.setExecutableImpl(driveProcess);

        //serialize
        AggregateProcessImpl aggregate = new AggregateProcessImpl();

        // set type
        aggregate.setUniqueIdentifier(UUID.randomUUID().toString());


        Quantity tilt = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Tilt"))
                .label("tilt")
                .uom("deg")
                .build();
        aggregate.addOutput("tilt", tilt);

        Quantity pan = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Pan"))
                .label("pan")
                .uom("deg")
                .build();
        aggregate.addOutput("pan", pan);


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

        // searchlight pi-bot command stream
        SimpleProcess sink = new SimpleProcessImpl();
        Reference sinkRef = new ReferenceImpl();
        sinkRef.setHref("urn:osh:process:datasink:commandstream");
        sink.setTypeOf(sinkRef);
        Settings commandConfig = new SettingsImpl();
        commandConfig.addSetValue("parameters/systemUID","urn:pibot:drive-sensor");
        commandConfig.addSetValue("parameters/inputName","DriveControl");
        sink.setConfiguration(commandConfig);
        aggregate.addComponent("sink0", sink);


        // connections
        LinkImpl removeSpeed = new LinkImpl();
        removeSpeed.setSource("components/source0/outputs/output1/gamepads/gamepad0/LeftThumb");
        removeSpeed.setDestination("components/process0/inputs/Minus");

        LinkImpl addSpeed = new LinkImpl();
        addSpeed.setSource("components/source0/outputs/output2/gamepads/gamepad0/RightThumb");
        addSpeed.setDestination("components/process0/inputs/Plus");

        LinkImpl dpad = new LinkImpl();
        dpad.setSource("components/source0/outputs/output3/gamepads/gamepad0/pov");
        dpad.setDestination("components/process0/inputs/pov");

        LinkImpl panC = new LinkImpl();
        panC.setSource("components/process0/outputs/Pan");
        panC.setDestination("components/sink0/inputs/CameraControl/pan");

        LinkImpl tiltC = new LinkImpl();
        tiltC.setSource("components/process0/outputs/Tilt");
        tiltC.setDestination("components/sink0/inputs/CameraControl/tilt");

        LinkImpl output1 = new LinkImpl();
        output1.setSource("components/process0/outputs/Pan");
        output1.setDestination("outputs/pan");

        LinkImpl output2 = new LinkImpl();
        output2.setSource("components/process0/outputs/Tilt");
        output2.setDestination("outputs/tilt");

        // add connections
        aggregate.addConnection(removeSpeed);
        aggregate.addConnection(addSpeed);
        aggregate.addConnection(dpad);
        aggregate.addConnection(panC);
        aggregate.addConnection(tiltC);
        aggregate.addConnection(output1);
        aggregate.addConnection(output2);

        smlHelper.writeProcess(System.out, aggregate, true);

        driveProcess.execute();

    }

}