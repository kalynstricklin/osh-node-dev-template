package com.botts.process.camera;

import org.sensorhub.impl.processing.AbstractProcessProvider;

public class PiBotCameraDescriptor extends AbstractProcessProvider {
    public PiBotCameraDescriptor(){
        addImpl(PiBotCameraProcess.INFO);
    }
}
