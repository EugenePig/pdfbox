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
package org.apache.fontbox.ttf;

import java.io.IOException;

/**
 * A table in a true type font.
 * 
 * @author Eugene Su (su.eugene@gmail.com)
 * @version $Revision: 1.0 $
 */
public class VerticalMetricsTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "vmtx";
    
    private int[] advanceHeight;
    private short[] topSideBearing;
    private short[] nonVerticalTopSideBearing;
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        VerticalHeaderTable vHeader = ttf.getVerticalHeader();
        MaximumProfileTable maxp = ttf.getMaximumProfile();
        int numHMetrics = vHeader.getNumberOfHMetrics();
        int numGlyphs = maxp.getNumGlyphs();
        
        advanceHeight = new int[ numHMetrics ];
        topSideBearing = new short[ numHMetrics ];
        for( int i=0; i<numHMetrics; i++ )
        {
            advanceHeight[i] = data.readUnsignedShort();
            topSideBearing[i] = data.readSignedShort();
        }
        
        int numberNonVertical = numGlyphs - numHMetrics;
        nonVerticalTopSideBearing = new short[ numberNonVertical ];
        for( int i=0; i<numberNonVertical; i++ )
        {
            nonVerticalTopSideBearing[i] = data.readSignedShort();
        }
    }
    /**
     * @return Returns the advanceHeight.
     */
    public int[] getAdvanceHeight()
    {
        return advanceHeight;
    }
    /**
     * @param advanceHeightValue The advanceHeight to set.
     */
    public void setAdvanceHeight(int[] advanceHeightValue)
    {
        this.advanceHeight = advanceHeightValue;
    }
	
    /**
     * @return the topSideBearing
     */
    public short[] getTopSideBearing() 
    {
        return topSideBearing;
    }
}
