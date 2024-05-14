package com.botts.process.drive;

import org.sensorhub.impl.processing.AbstractProcessProvider;

public class PiBotDriveDescriptor extends AbstractProcessProvider {
    public PiBotDriveDescriptor(){
        addImpl(PiBotDriveProcess.INFO);
    }
}
