/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.light;

import org.sensorhub.impl.processing.AbstractProcessProvider;

/**
 * Descriptor for the {@link SearchlightProcess}
 *
 * @author Kalyn Stricklin
 * @since 1.0.0
 */
public class SearchlightDescriptor extends AbstractProcessProvider
{
    public SearchlightDescriptor()
    {
        addImpl(SearchlightProcess.INFO);
    }
}
