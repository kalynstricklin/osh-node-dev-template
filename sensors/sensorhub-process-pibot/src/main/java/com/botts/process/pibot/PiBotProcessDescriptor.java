package com.botts.process.pibot;

//import com.botts.process.pibot.chain.GamepadProcessChain;
//import com.botts.process.pibot.chain.PibotCameraUniversal;
//import com.botts.process.pibot.chain.PibotDriveUniversal;
//import com.botts.process.pibot.chain.SearchlightProcessUniversal;
import com.botts.process.pibot.helpers.PrimaryControllerSelector;
import org.sensorhub.impl.processing.AbstractProcessProvider;

public class PiBotProcessDescriptor extends AbstractProcessProvider {
    public PiBotProcessDescriptor(){
        // chained together!
//        addImpl(PibotDriveUniversal.INFO);
////        addImpl(GamepadProcessChain.INFO);
//        addImpl(PibotCameraUniversal.INFO);
//        addImpl(SearchlightProcessUniversal.INFO);

        // with updated universal controller process
        addImpl(PibotProcess.INFO);
//        addImpl(GamepadProcessChainUniversal.INFO);
        addImpl(PrimaryControllerSelector.INFO);
    }
}
