/***************************** BEGIN LICENSE BLOCK ***************************

 Copyright (C) 2022 Botts Innovative Research, Inc. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/
package com.botts.process.template;

import org.sensorhub.impl.processing.AbstractProcessProvider;

/**
 * Descriptor for the {@link TemplateProcess}
 *
 * @author Kalyn Stricklin
 * @since 1.0.0
 */
public class TemplateDescriptor extends AbstractProcessProvider
{
    public TemplateDescriptor()
    {
        addImpl(TemplateProcess.INFO);
    }
}
