package org.sensorhub.impl.process.drive;


import com.botts.process.pibot.chain.GamepadProcessChain;
import com.botts.process.pibot.chain.PibotDriveUniversal;
import net.opengis.gml.v32.Reference;
import net.opengis.gml.v32.impl.ReferenceImpl;
import net.opengis.sensorml.v20.Link;
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


public class TestPiBotProcess
{
    static String SENSOR1_ID = "PI-BOT-Process";

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

        PibotDriveUniversal driveProcessUniversal = new PibotDriveUniversal();
        driveProcessUniversal.init();
        System.out.println(driveProcessUniversal.getProcessInfo().getUri());

        //gamepad process
        GamepadProcessChain gamePadProcessChain = new GamepadProcessChain();
        gamePadProcessChain.init();
        System.out.println(gamePadProcessChain.getProcessInfo().getUri());

        SMLUtils smlHelper = new SMLUtils(SMLUtils.V2_0);
        SWEHelper swe = new SWEHelper();

        // pibot drive process
        SimpleProcessImpl driveProcess = new SimpleProcessImpl();
        Reference processRef = new ReferenceImpl();
        processRef.setHref("urn:osh:process:pi-bot:drive");
        driveProcess.setExecutableImpl(driveProcessUniversal);

        // remote process
        SimpleProcessImpl gamePad = new SimpleProcessImpl();
        Reference processReference = new ReferenceImpl();
        processReference.setHref("urn:osh:process:gamepadchain");
        gamePad.setExecutableImpl(gamePadProcessChain);

        //serialize
        AggregateProcessImpl aggregate = new AggregateProcessImpl();

        // set type
        aggregate.setUniqueIdentifier(UUID.randomUUID().toString());

        Quantity pov = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("pov"))
                .label("pov")
                .build();
        aggregate.addOutput("pov", pov);
        Quantity rightThumb = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("RightThumb"))
                .label("Right Thumb")
                .build();
        aggregate.addOutput("plus", rightThumb);
        Quantity leftThumb = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("LeftThumb"))
                .label("LeftThumb")
                .build();
        aggregate.addOutput("minus", leftThumb);
//        Quantity buttonA = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("A"))
//                .label("A")
//                .build();
//        aggregate.addOutput("A", buttonA);
//        Quantity buttonB = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("B"))
//                .label("B")
//                .build();
//        aggregate.addOutput("B", buttonB);


        Quantity forwards = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Forward"))
                .uom("%")
                .build();
        aggregate.addOutput("forward", forwards);

        Quantity rev = swe.createQuantity()
                .definition(SWEHelper.getPropertyUri("Reverse"))
                .build();
        aggregate.addOutput("reverse", rev);

//        Quantity forRight = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ForwardRight"))
//                .uom("%")
//                .build();
//        aggregate.addOutput("forwardRight", forRight);
//
//        Quantity revRight = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ReverseRight"))
//                .build();
//        aggregate.addOutput("reverseRight", revRight);
//
//        Quantity forLeft = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ForwardLeft"))
//                .uom("%")
//                .build();
//        aggregate.addOutput("forwardLeft", forLeft);

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

//        Quantity revLeft = swe.createQuantity()
//                .definition(SWEHelper.getPropertyUri("ReverseLeft"))
//                .uom("%")
//                .build();
//        aggregate.addOutput("reverseLeft", revLeft);


        //COMPONENT LIST
        // universal remote
        SimpleProcess remote = new SimpleProcessImpl();
        Reference sourceRef = new ReferenceImpl();
        sourceRef.setHref("urn:osh:process:datasource:stream");
        remote.setTypeOf(sourceRef);
        Settings sourceConfig = new SettingsImpl();
        sourceConfig.addSetValue("parameters/producerURI","urn:osh:sensor:universalcontroller");
        remote.setConfiguration(sourceConfig);
        aggregate.addComponent("remoteSource", remote);

        // process component
        aggregate.addComponent("driveProcess", driveProcess);
        aggregate.addComponent("gamepadProcess", gamePad);

        // searchlight pi-bot command stream
        SimpleProcess driveControl = new SimpleProcessImpl();
        Reference sinkRef = new ReferenceImpl();
        sinkRef.setHref("urn:osh:process:datasink:commandstream");
        driveControl.setTypeOf(sinkRef);
        Settings commandConfig = new SettingsImpl();
        commandConfig.addSetValue("parameters/systemUID","urn:pibot:drive-sensor");
        commandConfig.addSetValue("parameters/inputName","DriveControl");
        driveControl.setConfiguration(commandConfig);
        aggregate.addComponent("FORWARD", driveControl);
        aggregate.addComponent("REVERSE", driveControl);
        aggregate.addComponent("SPIN_LEFT", driveControl);
        aggregate.addComponent("SPIN_RIGHT", driveControl);
//        aggregate.addComponent("driveForwardLeft", driveControl);
//        aggregate.addComponent("driveForwardRight", driveControl);
//        aggregate.addComponent("driveReverseLeft", driveControl);
//        aggregate.addComponent("driveReverseRight", driveControl);
        aggregate.addComponent("STOP", driveControl);

        // connections

        LinkImpl numGamepads = new LinkImpl();
        numGamepads.setSource("components/remoteSource/outputs/output1/numGamepads");
        numGamepads.setDestination("components/gamepadProcess/inputs/gamepadRecord/numGamepads");

        Link gamepadLink = new LinkImpl();
        gamepadLink.setSource("components/remoteSource/outputs/output1/gamepads");
        gamepadLink.setDestination("components/gamepadProcess/inputs/gamepadRecord/gamepads");


//        Link povLink = new LinkImpl();
//        povLink.setSource("components/gamepadProcess/outputs/pov");
//        povLink.setDestination("components/driveProcess/inputs/pov");
//
//        Link rightThumbLink = new LinkImpl();
//        rightThumbLink.setSource("components/gamepadProcess/outputs/RightThumb");
//        rightThumbLink.setDestination("components/driveProcess/inputs/plus");
//
//        Link leftThumbLink = new LinkImpl();
//        leftThumbLink.setSource("components/gamepadProcess/outputs/LeftThumb");
//        leftThumbLink.setDestination("components/driveProcess/inputs/minus");
//
//        Link forwardControl = new LinkImpl();
//        forwardControl.setSource("components/driveProcess/outputs/Forward");
//        forwardControl.setDestination("components/driveForward/outputs/Forward");
//
//        Link reverseControl = new LinkImpl();
//        reverseControl.setSource("components/driveProcess/outputs/Reverse");
//        reverseControl.setDestination("components/driveReverse/outputs/Reverse");
//
//        Link leftControl = new LinkImpl();
//        leftControl.setSource("components/driveProcess/outputs/Left");
//        leftControl.setDestination("components/driveLeft/outputs/Left");
//
//        Link rightControl = new LinkImpl();
//        rightControl.setSource("components/driveProcess/outputs/Right");
//        rightControl.setDestination("components/driveRight/outputs/Right");

//        Link forwardRightControl = new LinkImpl();
//        forwardRightControl.setSource("components/driveProcess/outputs/ForwardRight");
//        forwardRightControl.setDestination("components/driveForwardRight/outputs/ForwardRight");
//
//        Link ReverseRightControl = new LinkImpl();
//        ReverseRightControl.setSource("components/driveProcess/outputs/ReverseRight");
//        ReverseRightControl.setDestination("components/driveReverseRight/outputs/ReverseRight");
//
//        Link forwardLeftControl = new LinkImpl();
//        forwardLeftControl.setSource("components/driveProcess/outputs/ForwardLeft");
//        forwardLeftControl.setDestination("components/driveForwardLeft/outputs/ForwardLeft");
//
//        Link reverseLeftControl = new LinkImpl();
//        reverseLeftControl.setSource("components/driveProcess/outputs/ReverseLeft");
//        reverseLeftControl.setDestination("components/driveReverseLeft/outputs/ReverseLeft");

//        Link stopControl = new LinkImpl();
//        stopControl.setSource("components/driveProcess/outputs/Stop");
//        stopControl.setDestination("components/driveStop/outputs/Stop");
//
//        LinkImpl driveLink = new LinkImpl();
//        driveLink.setSource("components/driveProcess/outputs/Forward");
//        driveLink.setSource("components/driveControl/inputs/DriveControl/Forward");
//
//
//        LinkImpl povOut = new LinkImpl();
//        povOut.setSource("components/driveProcess/outputs/pov");
//        povOut.setDestination("outputs/pov");
//
//        LinkImpl minusOut = new LinkImpl();
//        minusOut.setSource("components/driveProcess/outputs/minus");
//        minusOut.setDestination("outputs/minus");
//
//        LinkImpl plusOut = new LinkImpl();
//        plusOut.setSource("components/driveProcess/outputs/plus");
//        plusOut.setDestination("outputs/plus");
//
//        LinkImpl forward = new LinkImpl();
//        forward.setSource("components/driveProcess/outputs/Forward");
//        forward.setDestination("outputs/forward");
//
//        LinkImpl reverse = new LinkImpl();
//        reverse.setSource("components/driveProcess/outputs/Reverse");
//        reverse.setDestination("outputs/reverse");
//
//        LinkImpl left = new LinkImpl();
//        left.setSource("components/driveProcess/outputs/SpinLeft");
//        left.setDestination("outputs/left");
//
//        LinkImpl right = new LinkImpl();
//        right.setSource("components/driveProcess/outputs/SpinRight");
//        right.setDestination("outputs/right");
//
////        LinkImpl reverseRight = new LinkImpl();
////        reverseRight.setSource("components/driveProcess/outputs/ReverseRight");
////        reverseRight.setDestination("outputs/reverseRight");
////
////        LinkImpl reverseLeft = new LinkImpl();
////        reverseLeft.setSource("components/driveProcess/outputs/ReverseLeft");
////        reverseLeft.setDestination("outputs/reverseLeft");
////
////        LinkImpl forwardLeft = new LinkImpl();
////        forwardLeft.setSource("components/driveProcess/outputs/ForwardLeft");
////        forwardLeft.setDestination("outputs/forwardLeft");
////
////        LinkImpl forwardRight = new LinkImpl();
////        forwardRight.setSource("components/driveProcess/outputs/ForwardRight");
////        forwardRight.setDestination("outputs/forwardRight");
//
//        LinkImpl stop = new LinkImpl();
//        stop.setSource("components/driveProcess/outputs/Stop");
//        stop.setDestination("outputs/stop");


        // add connections
//        aggregate.addConnection(numGamepads);
//        aggregate.addConnection(gamepadLink);
//        aggregate.addConnection(povLink);
//        aggregate.addConnection(rightThumbLink);
//        aggregate.addConnection(leftThumbLink);
//        aggregate.addConnection(driveLink);
//        aggregate.addConnection(plusOut);
//        aggregate.addConnection(povOut);
//        aggregate.addConnection(minusOut);
//        aggregate.addConnection(forward);
//        aggregate.addConnection(reverse);
////        aggregate.addConnection(reverseLeft);
////        aggregate.addConnection(reverseRight);
////        aggregate.addConnection(forwardRight);
////        aggregate.addConnection(forwardLeft);
//        aggregate.addConnection(stop);
//        aggregate.addConnection(right);
//        aggregate.addConnection(left);

        smlHelper.writeProcess(System.out, aggregate, true);

        driveProcessUniversal.execute();

    }

}