package com.botts.process.light;

import org.sensorhub.impl.processing.AbstractProcessProvider;

public class SearchlightDescriptor extends AbstractProcessProvider {
    public SearchlightDescriptor(){
        addImpl(SearchlightProcess.INFO);
    }
}
