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
package org.apache.pdfbox.encoding;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;

/**
 * This is an interface to a text encoder.
 *
 * @author <a href="mailto:su.eugene@gmail.com">Eugene Su</a>
 * @version $Revision: 1.00 $
 */
public class IdentityHEncoding extends Encoding
{
	public static final IdentityHEncoding INSTANCE = new IdentityHEncoding();
	
    /**
     * Constructor.
     */
    public IdentityHEncoding()
	{
		
	}
	
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return COSName.IDENTITY_H;
    }
}