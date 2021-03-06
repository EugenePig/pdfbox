/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.contentstream.operator.state;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;

/**
 * d: Set the line dash pattern.
 *
 * @author Ben Litchfield
 */
public class SetLineDashPattern extends OperatorProcessor
{
    /**
     * log instance
     */
    private static final Log LOG = LogFactory.getLog(SetLineDashPattern.class);

    @Override
    public void process(Operator operator, List<COSBase> arguments)
    {
        COSArray dashArray = (COSArray) arguments.get(0);
        int dashPhase = ((COSNumber) arguments.get(1)).intValue();
        if (dashPhase < 0)
        {
            LOG.warn("dash phaseStart has negative value " + dashPhase + ", set to 0");
            dashPhase = 0;
        }
        PDLineDashPattern lineDash = new PDLineDashPattern(dashArray, dashPhase);
        context.getGraphicsState().setLineDashPattern(lineDash);
    }

    @Override
    public String getName()
    {
        return "d";
    }
}
