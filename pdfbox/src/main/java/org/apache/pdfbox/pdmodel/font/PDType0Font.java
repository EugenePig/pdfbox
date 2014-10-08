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
package org.apache.pdfbox.pdmodel.font;

import java.awt.Font;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

// Eugene Su
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.CMAPTable;
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.GlyphTable;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.HorizontalHeaderTable;
import org.apache.fontbox.ttf.HorizontalMetricsTable;
import org.apache.fontbox.ttf.NameRecord;
import org.apache.fontbox.ttf.NamingTable;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.PostScriptTable;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TTFSubFont;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This is implementation of the Type0 Font. See <a
 * href="https://issues.apache.org/jira/browse/PDFBOX-605">PDFBOX-605</a>
 * for the related improvement issue.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class PDType0Font extends PDSimpleFont
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDType0Font.class);

    private COSArray descendantFontArray;
    private PDFont descendantFont;
    private COSDictionary descendantFontDictionary;
    private Font awtFont;
    private TrueTypeFont ttfFont = null; // Eugene Su
    /**
     * Constructor.
     */
    public PDType0Font()
    {
        super();
        font.setItem(COSName.SUBTYPE, COSName.TYPE0);
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF
     * specification.
     */
    public PDType0Font(COSDictionary fontDictionary)
    {
        super(fontDictionary);
        descendantFontDictionary = (COSDictionary) getDescendantFonts().getObject(0);
        if (descendantFontDictionary != null)
        {
            try
            {
                descendantFont = PDFontFactory.createFont(descendantFontDictionary);
            }
            catch (IOException exception)
            {
                LOG.error("Error while creating the descendant font!");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Font getawtFont() throws IOException
    {
        if (awtFont == null)
        {
            if (descendantFont != null)
            {
                awtFont = ((PDSimpleFont) descendantFont).getawtFont();
                if (awtFont != null)
                {
                    setIsFontSubstituted(((PDSimpleFont) descendantFont).isFontSubstituted());
                    /*
                     * Fix Oracle JVM Crashes.
                     * Tested with Oracle JRE 6.0_45-b06 and 7.0_21-b11
                     */
                    awtFont.canDisplay(1);
                }
            }
            if (awtFont == null)
            {
                awtFont = FontManager.getStandardFont();
                LOG.info("Using font " + awtFont.getName()
                        + " instead of " + descendantFont.getFontDescriptor().getFontName());
                setIsFontSubstituted(true);
            }
        }
        return awtFont;
    }

    /**
     * This will get the fonts bounding box.
     *
     * @return The fonts bounding box.
     *
     * @throws IOException If there is an error getting the bounding box.
     */
    @Override
    public PDRectangle getFontBoundingBox() throws IOException
    {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * This will get the font width for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontWidth(byte[] c, int offset, int length) throws IOException
    {
        return descendantFont.getFontWidth(c, offset, length);
    }

    /**
     * This will get the font height for a character.
     *
     * @param c The character code to get the height for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    @Override
    public float getFontHeight(byte[] c, int offset, int length) throws IOException
    {
        return descendantFont.getFontHeight(c, offset, length);
    }

    /**
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    @Override
    public float getAverageFontWidth() throws IOException
    {
        return descendantFont.getAverageFontWidth();
    }

    private COSArray getDescendantFonts()
    {
        if (descendantFontArray == null)
        {
            descendantFontArray = (COSArray) font.getDictionaryObject(COSName.DESCENDANT_FONTS);
        }
        return descendantFontArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFontWidth(int charCode)
    {
        return descendantFont.getFontWidth(charCode);
    }

    @Override
    public String encode(byte[] c, int offset, int length) throws IOException
    {
        String retval = null;
        if (hasToUnicode())
        {
            retval = super.encode(c, offset, length);
        }

        if (retval == null)
        {
            int result = cmap.lookupCID(c, offset, length);
            if (result != -1)
            {
                retval = descendantFont.cmapEncoding(result, 2, true, null);
            }
        }
        return retval;
    }

    /**
     *
     * Provides the descendant font.
     *
     * @return the descendant font.
     *
     */
    public PDFont getDescendantFont()
    {
        return descendantFont;
    }
    @Override
    public void clear()
    {
        super.clear();
        descendantFontArray = null;
        if (descendantFont != null)
        {
            descendantFont.clear();
            descendantFont = null;
        }
        descendantFontDictionary = null;
    }

    // Eugene Su
    /**
     * This will load a TTF font from a font file.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param file The file on the filesystem that holds the font file.
     * @return A true type font.
     * @throws IOException If there is an error loading the file data.
     */
    public static PDType0Font loadTTF( PDDocument doc, String file ) throws IOException
    {
        return loadTTF( doc, new File( file ) );
    }
    
    // Eugene Su
    /**
     * This will load a TTF to be embedded into a document.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param file a ttf file.
     * @return a PDTrueTypeFont instance.
     * @throws IOException If there is an error loading the data.
     */
    public static PDType0Font loadTTF( PDDocument doc, File file ) throws IOException
    {
        return loadTTF( doc, new FileInputStream( file ) );
    } 
    
    // Eugene Su
    /**
     * This will load a TTF to be embedded into a document.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param stream a ttf input stream.
     * @return a PDTrueTypeFont instance.
     * @throws IOException If there is an error loading the data.
     */
    public static PDType0Font loadTTF( PDDocument doc, InputStream stream ) throws IOException
    { 
        return PDType0Font.loadTTF(doc, stream, new WinAnsiEncoding());
    }
    
    // Eugene Su
    /**
     * This will load a TTF to be embedded into a document.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param stream a ttf input stream.
     * @param enc The font encoding.
     * @return a PDTrueTypeFont instance.
     * @throws IOException If there is an error loading the data.
     */
    public static PDType0Font loadTTF( PDDocument doc, InputStream stream, Encoding enc ) throws IOException
    { 
        PDStream fontStream = new PDStream(doc, stream, false);
        fontStream.getStream().setInt( COSName.LENGTH1, fontStream.getByteArray().length );
        fontStream.addCompression();
        //only support winansi encoding right now, should really
        //just use Identity-H with unicode mapping
        return PDType0Font.loadTTF(fontStream, enc);
    }

    // Eugene Su
    /**
     * This will load a TTF to be embedded into a document.
     *
     * @param fontStream a ttf input stream.
     * @param enc The font encoding.
     * @return a PDTrueTypeFont instance.
     * @throws IOException If there is an error loading the data.
     */
    public static PDType0Font loadTTF( PDStream fontStream, Encoding enc ) throws IOException
    { 
    	PDType0Font retval = new PDType0Font();
        retval.setFontEncoding(enc);
        retval.setEncoding(enc.getCOSObject());

        COSArray descendantFonts = new COSArray();
        COSDictionary type0FontDic = (COSDictionary)retval.getCOSObject();
        type0FontDic.setItem(COSName.DESCENDANT_FONTS, descendantFonts);
        PDCIDFontType2Font type2CIDFont = new PDCIDFontType2Font();
        descendantFonts.add(type2CIDFont);
        PDFontDescriptorDictionary fontDescriptor = new PDFontDescriptorDictionary();
        type2CIDFont.setFontDescriptor(fontDescriptor);
        
        COSDictionary type2CIDFontDic = (COSDictionary)type2CIDFont.getCOSObject();
        
        COSDictionary cidsys = new COSDictionary();
        cidsys.setItem(COSName.ORDERING, new COSString("Identity"));
        cidsys.setItem(COSName.REGISTRY, new COSString("Adobe"));
        cidsys.setItem(COSName.SUPPLEMENT, new COSString("0"));
        type2CIDFontDic.setItem(COSName.CIDSYSTEMINFO, cidsys); 
        
        /*COSStream cidtogidmap = new COSStream(new RandomAccessBuffer());
        OutputStream out = cidtogidmap.createUnfilteredStream();
        byte[] bin = {00, 00, 0x22, (byte)0xf9};
        out.write(bin, 0, bin.length);
        out.flush();
        out.close();
        cidtogidmap.setFilters(COSName.FLATE_DECODE);
        type2CIDFontDic.setItem(COSName.CID_TO_GID_MAP, cidtogidmap);*/
        
        fontDescriptor.setFontFile2(fontStream);
        // As the stream was close within the PDStream constructor, we have to recreate it
        InputStream stream = fontStream.createInputStream();
        try
        {
            retval.loadDescriptorDictionary(type2CIDFont); 
        }
        finally
        {
            stream.close();
        }
        return retval;
    }

    // Eugene Su
    private void loadDescriptorDictionary(PDCIDFontType2Font type2CIDFont) throws IOException
    {
    	PDFontDescriptorDictionary fd = (PDFontDescriptorDictionary)type2CIDFont.getFontDescriptor();
    	InputStream ttfData = fd.getFontFile2().createInputStream();
    	
        try
        {
        	TTFParser parser = new TTFParser();
       	    ttfFont = parser.parseTTF( ttfData );
            NamingTable naming = ttfFont.getNaming();
            List<NameRecord> records = naming.getNameRecords();
            for( int i=0; i<records.size(); i++ )
            {
                NameRecord nr = records.get( i );
                if( nr.getNameId() == NameRecord.NAME_POSTSCRIPT_NAME )
                {
                    setBaseFont( nr.getString() );
                    fd.setFontName( nr.getString() );
                    type2CIDFont.setBaseFont(nr.getString());
                }
                else if( nr.getNameId() == NameRecord.NAME_FONT_FAMILY_NAME )
                {
                	fd.setFontFamily( nr.getString() );
                }
            }

            OS2WindowsMetricsTable os2 = ttfFont.getOS2Windows();
            boolean isSymbolic = false;
            switch( os2.getFamilyClass() )
            {
                case OS2WindowsMetricsTable.FAMILY_CLASS_SYMBOLIC:
                    isSymbolic = true;
                    break;
                case OS2WindowsMetricsTable.FAMILY_CLASS_SCRIPTS:
                    fd.setScript( true );
                    break;
                case OS2WindowsMetricsTable.FAMILY_CLASS_CLAREDON_SERIFS:
                case OS2WindowsMetricsTable.FAMILY_CLASS_FREEFORM_SERIFS:
                case OS2WindowsMetricsTable.FAMILY_CLASS_MODERN_SERIFS:
                case OS2WindowsMetricsTable.FAMILY_CLASS_OLDSTYLE_SERIFS:
                case OS2WindowsMetricsTable.FAMILY_CLASS_SLAB_SERIFS:
                    fd.setSerif( true );
                    break;
                default:
                    //do nothing
            }
            switch( os2.getWidthClass() )
            {
                case OS2WindowsMetricsTable.WIDTH_CLASS_ULTRA_CONDENSED:
                    fd.setFontStretch( "UltraCondensed" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_EXTRA_CONDENSED:
                    fd.setFontStretch( "ExtraCondensed" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_CONDENSED:
                    fd.setFontStretch( "Condensed" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_SEMI_CONDENSED:
                    fd.setFontStretch( "SemiCondensed" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_MEDIUM:
                    fd.setFontStretch( "Normal" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_SEMI_EXPANDED:
                    fd.setFontStretch( "SemiExpanded" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_EXPANDED:
                    fd.setFontStretch( "Expanded" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_EXTRA_EXPANDED:
                    fd.setFontStretch( "ExtraExpanded" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_ULTRA_EXPANDED:
                    fd.setFontStretch( "UltraExpanded" );
                    break;
                default:
                    //do nothing
            }
            fd.setFontWeight( os2.getWeightClass() );
            fd.setSymbolic( isSymbolic );
            fd.setNonSymbolic( !isSymbolic );

            //todo retval.setFixedPitch
            //todo retval.setItalic
            //todo retval.setAllCap
            //todo retval.setSmallCap
            //todo retval.setForceBold

            HeaderTable header = ttfFont.getHeader();
            PDRectangle rect = new PDRectangle();
            float scaling = 1000f/header.getUnitsPerEm();
            rect.setLowerLeftX( header.getXMin() * scaling );
            rect.setLowerLeftY( header.getYMin() * scaling );
            rect.setUpperRightX( header.getXMax() * scaling );
            rect.setUpperRightY( header.getYMax() * scaling );
            fd.setFontBoundingBox( rect );

            HorizontalHeaderTable hHeader = ttfFont.getHorizontalHeader();
            fd.setAscent( hHeader.getAscender() * scaling );
            fd.setDescent( hHeader.getDescender() * scaling );

            GlyphTable glyphTable = ttfFont.getGlyph();
            GlyphData[] glyphs = glyphTable.getGlyphs();

            PostScriptTable ps = ttfFont.getPostScript();
            fd.setFixedPitch( ps.getIsFixedPitch() > 0 );
            fd.setItalicAngle( ps.getItalicAngle() );

            String[] names = ps.getGlyphNames();
            
            if( names != null )
            {
                for( int i=0; i<names.length; i++ )
                {
                     //if we have a capital H then use that, otherwise use the
                    //tallest letter
                    if( names[i].equals( "H" ) )
                    {
                        fd.setCapHeight( glyphs[i].getBoundingBox().getUpperRightY()/scaling );
                    }
                    if( names[i].equals( "x" ) )
                    {
                        fd.setXHeight( glyphs[i].getBoundingBox().getUpperRightY()/scaling );
                    }
                }
            }

            //hmm there does not seem to be a clear definition for StemV,
            //this is close enough and I am told it doesn't usually get used.
            fd.setStemV( (fd.getFontBoundingBox().getWidth() * .13f) );

            CMAPTable cmapTable = ttfFont.getCMAP();
            CMAPEncodingEntry[] cmaps = cmapTable.getCmaps();
            CMAPEncodingEntry uniMap = null;
            
            for( int i=0; i<cmaps.length; i++ )
            {
                if( cmaps[i].getPlatformId() == CMAPTable.PLATFORM_WINDOWS) 
                {
                    int platformEncoding = cmaps[i].getPlatformEncodingId();
                    if ( CMAPTable.ENCODING_UNICODE == platformEncoding )
                    {
                        uniMap = cmaps[i];
                        break;
                    }
                }
            }
            
            if(uniMap != null)
            {
                COSStream cidtogidmap = new COSStream(new RandomAccessBuffer());
                OutputStream out = cidtogidmap.createUnfilteredStream();
                byte[] bin = new byte[0x20000];
                for (int i = 0; i < 0x10000 ; i++ )
                {
                	int gid = uniMap.getGlyphId(i);
                	bin[i*2] = (byte)(gid >> 8);
                	bin[i*2 + 1] = (byte)gid;
                }
                out.write(bin, 0, bin.length);
                out.flush();
                out.close();
                cidtogidmap.setFilters(COSName.FLATE_DECODE);
                ((COSDictionary)type2CIDFont.getCOSObject()).setItem(COSName.CID_TO_GID_MAP, cidtogidmap);
            }
        }
        finally
        {
            if( ttfFont != null )
            {
            	ttfFont.close();
            }
        }
    }
    
    /**
     * @return the ttfFont
     */
    public TrueTypeFont getTTF() 
    {
        return ttfFont;
    }
}
