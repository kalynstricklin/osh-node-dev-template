package org.sensorhub.impl.process.drive;


import com.botts.process.drive.PiBotDriveProcess;
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


public class TestPiBotDriveProcess
{
    static String SENSOR1_ID = "PI-BOT-DRIVE";

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
    public void testDriveProcess() throws Exception{

        PiBotDriveProcess driveProcess = new PiBotDriveProcess();
        driveProcess.init();
        System.out.println(driveProcess.getProcessInfo().getUri());

        SMLUtils smlHelper = new SMLUtils(SMLUtils.V2_0);
        SWEHelper swe = new SWEHelper();
        SMLFactory smlFac = new SMLFactory();

        SimpleProcessImpl simple = new SimpleProcessImpl();
        Reference processRef = new ReferenceImpl();
        processRef.setHref("urn:osh:process:pi-bot:drive");
        simple.setExecutableImpl(driveProcess);

        //serialize
        AggregateProcessImpl aggregate = new AggregateProcessImpl();

        // set type
        aggregate.setUniqueIdentifier(UUID.randomUUID().toString());


        Quantity forwards = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Forward"))
                .uom("%")
                .build();
        aggregate.addOutput("forward", forwards);

        Quantity rev = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Reverse"))
                .build();
        aggregate.addOutput("reverse", rev);

        Quantity forRight = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("ForwardRight"))
                .uom("%")
                .build();
        aggregate.addOutput("forwardRight", forRight);

        Quantity revRight = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("ReverseRight"))
                .build();
        aggregate.addOutput("reverseRight", revRight);

        Quantity forLeft = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("ForwardLeft"))
                .uom("%")
                .build();
        aggregate.addOutput("forwardLeft", forLeft);

        Quantity spinRight = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("SpinRight"))
                .build();
        aggregate.addOutput("right", spinRight);

        Quantity spinLeft = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("SpinLeft"))
                .uom("%")
                .build();
        aggregate.addOutput("left", spinLeft);

        Quantity stopped = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Stop"))
                .build();
        aggregate.addOutput("stop", stopped);

        Quantity revLeft = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("ReverseLeft"))
                .uom("%")
                .build();
        aggregate.addOutput("reverseLeft", revLeft);


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

        LinkImpl forward = new LinkImpl();
        forward.setSource("components/process0/outputs/Forward");
        forward.setDestination("outputs/forward");

        LinkImpl reverse = new LinkImpl();
        reverse.setSource("components/process0/outputs/Reverse");
        reverse.setDestination("outputs/reverse");

        LinkImpl left = new LinkImpl();
        left.setSource("components/process0/outputs/SpinLeft");
        left.setDestination("outputs/left");

        LinkImpl right = new LinkImpl();
        right.setSource("components/process0/outputs/SpinRight");
        right.setDestination("outputs/right");

        LinkImpl reverseRight = new LinkImpl();
        reverseRight.setSource("components/process0/outputs/ReverseRight");
        reverseRight.setDestination("outputs/reverseRight");

        LinkImpl reverseLeft = new LinkImpl();
        reverseLeft.setSource("components/process0/outputs/ReverseLeft");
        reverseLeft.setDestination("outputs/reverseLeft");

        LinkImpl forwardLeft = new LinkImpl();
        forwardLeft.setSource("components/process0/outputs/ForwardLeft");
        forwardLeft.setDestination("outputs/forwardLeft");

        LinkImpl forwardRight = new LinkImpl();
        forwardRight.setSource("components/process0/outputs/ForwardRight");
        forwardRight.setDestination("outputs/forwardRight");

        LinkImpl stop = new LinkImpl();
        stop.setSource("components/process0/outputs/Stop");
        stop.setDestination("outputs/stop");


        // add connections
        aggregate.addConnection(removeSpeed);
        aggregate.addConnection(addSpeed);
        aggregate.addConnection(dpad);
        aggregate.addConnection(forward);
        aggregate.addConnection(reverse);
        aggregate.addConnection(reverseLeft);
        aggregate.addConnection(reverseRight);
        aggregate.addConnection(forwardRight);
        aggregate.addConnection(forwardLeft);
        aggregate.addConnection(stop);
        aggregate.addConnection(right);
        aggregate.addConnection(left);

        smlHelper.writeProcess(System.out, aggregate, true);

        driveProcess.execute();

    }

}