/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package org.apache.pdfbox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.util.CharactersByArticleComparatorH;
import org.apache.pdfbox.util.CharactersByArticleComparatorV;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.PDFText2HTML;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPositionComparatorH;
import org.apache.pdfbox.util.TextPositionComparatorV;



// Eugene Su
import java.io.BufferedWriter;
import java.lang.Math;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.LogManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.ttf.CIDFontType2Parser;
import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.CMAPTable;
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.GlyphTable;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.HorizontalHeaderTable;
import org.apache.fontbox.ttf.HorizontalMetricsTable;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.ttf.VerticalMetricsTable;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ResourceLoader;
import org.apache.pdfbox.util.TextPosition;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * This is the main program that simply parses the pdf document and get text information.
 * @author <a href="mailto:su.eugene@gmail.com">Eugene Su</a>
 * @version $Revision: 1.00 $
 */
public class ExtractTextInfo
{
  private static final String PASSWORD = "-password";
  private static final String ENCODING = "-encoding";
  private static final String CONSOLE = "-console";
  private static final String START_PAGE = "-startPage";
  private static final String END_PAGE = "-endPage";
  private static final String SORT = "-sort";
  private static final String IGNORE_BEADS = "-ignoreBeads";
  private static final String DEBUG = "-debug";
  // jjb - added simple HTML output
  private static final String HTML = "-html";
  // enables pdfbox to skip corrupt objects
  private static final String FORCE = "-force";
  private static final String NONSEQ = "-nonSeq";

  // Eugene Su
  private static final String INFOFILE_XML = "-infoxml";
  private static final String TEST_CJK = "-testcjk";
  private static final String SORTV2 = "-sortv2";
  private boolean hasInfoXml = false;
  private boolean enableTestCJK = false;

  /*
   * debug flag
   */
  private boolean debug = false;

  /**
   * private constructor.
   */
  private ExtractTextInfo()
  {
    // static class

    // Eugene Su
    // super("JTree");
    // setSize(400, 300);
    // addWindowListener(new BasicWindowMonitor());
  }

  /**
   * Infamous main method.
   * @param args Command line arguments, should be one and a reference to a file.
   * @throws Exception If there is an error parsing the document.
   */
  public static void main(String[] args) throws Exception
  {
    ExtractTextInfo extractor = new ExtractTextInfo();
    extractor.startExtraction(args);
  }

  /**
   * Starts the text extraction.
   * @param args the commandline arguments.
   * @throws Exception if something went wrong.
   */
  public void startExtraction(String[] args) throws Exception
  {
    boolean toConsole = false;
    boolean toHTML = false;
    boolean force = false;
    boolean sort = false;
    boolean sortV2 = false; // Eugene Su
    boolean separateBeads = true;
    boolean useNonSeqParser = false;
    String password = "";
    String encoding = null;
    String pdfFile = null;
    String outputFile = null;
    // Defaults to text files
    String ext = ".txt";
    int startPage = 1;
    int endPage = Integer.MAX_VALUE;
    for (int i = 0; i < args.length; i++)
    {
      if (args[i].equals(PASSWORD))
      {
        i++;
        if (i >= args.length)
        {
          usage();
        }
        password = args[i];
      }
      else if (args[i].equals(ENCODING))
      {
        i++;
        if (i >= args.length)
        {
          usage();
        }
        encoding = args[i];
      }
      else if (args[i].equals(START_PAGE))
      {
        i++;
        if (i >= args.length)
        {
          usage();
        }
        startPage = Integer.parseInt(args[i]);
      }
      else if (args[i].equals(HTML))
      {
        toHTML = true;
        ext = ".html";
      }
      else if (args[i].equals(SORT))
      {
        sort = true;
      }
      else if (args[i].equals(SORTV2)) // Eugene Su
      {
        sortV2 = true;
      }
      else if (args[i].equals(IGNORE_BEADS))
      {
        separateBeads = false;
      }
      else if (args[i].equals(DEBUG))
      {
        debug = true;
      }
      else if (args[i].equals(END_PAGE))
      {
        i++;
        if (i >= args.length)
        {
          usage();
        }
        endPage = Integer.parseInt(args[i]);
      }
      else if (args[i].equals(CONSOLE))
      {
        toConsole = true;
      }
      else if (args[i].equals(FORCE))
      {
        force = true;
      }
      else if (args[i].equals(NONSEQ))
      {
        useNonSeqParser = true;
      }
      else if (args[i].equals(INFOFILE_XML)) // Eugene Su
      {
        hasInfoXml = true;
      }
      else if (args[i].equals(TEST_CJK)) // Eugene Su
      {
        enableTestCJK = true;
      }
      else
      {
        if (pdfFile == null)
        {
          pdfFile = args[i];
        }
        else
        {
          outputFile = args[i];
        }
      }
    }

    if (pdfFile == null)
    {
      usage();
    }
    else
    {
      if (debug)
      {
        URL url =
            ExtractTextInfo.class.getClassLoader().getResource(
              "org/apache/pdfbox/resources/debugging.properties");
        LogManager.getLogManager().readConfiguration(url.openStream());
        PDFStreamEngine.enableLogger();
      }

      Writer output = null;
      PDDocument document = null;
      try
      {
        long startTime = startProcessing("Loading PDF " + pdfFile);
        if (outputFile == null && pdfFile.length() > 4)
        {
          outputFile = new File(pdfFile.substring(0, pdfFile.length() - 4) + ext).getAbsolutePath();
        }
        if (useNonSeqParser)
        {
          document = PDDocument.loadNonSeq(new File(pdfFile), null, password);
        }
        else
        {
          document = PDDocument.load(pdfFile, force);
          if (document.isEncrypted())
          {
            StandardDecryptionMaterial sdm = new StandardDecryptionMaterial(password);
            document.openProtection(sdm);
          }
        }

        AccessPermission ap = document.getCurrentAccessPermission();
        if (!ap.canExtractContent())
        {
          throw new IOException("You do not have permission to extract text");
        }

        stopProcessing("Time for loading: ", startTime);

        if ((encoding == null) && (toHTML))
        {
          encoding = "UTF-8";
        }

        if (toConsole)
        {
          output = new OutputStreamWriter(System.out);
        }
        else
        {
          if (encoding != null)
          {
            output = new OutputStreamWriter(new FileOutputStream(outputFile), encoding);
          }
          else
          {
            // use default encoding
            output = new OutputStreamWriter(new FileOutputStream(outputFile));
          }
        }

        PDFTextStripper stripper = null;
        if (toHTML)
        {
          stripper = new PDFText2HTML(encoding);
        }
        else
        {
          stripper =
              new PDFTextStripper(ResourceLoader.loadProperties(
                "org/apache/pdfbox/resources/PDFTextInfoStripper.properties", true), encoding);
        }
        stripper.setForceParsing(force);
        stripper.setSortByPosition(sort);
        stripper.setSortByPositionV2(sortV2); // Eugene Su
        stripper.setShouldSeparateByBeads(separateBeads);
        stripper.setStartPage(startPage);
        stripper.setEndPage(endPage);

        startTime = startProcessing("Starting text extraction");
        if (debug)
        {
          System.err.println("Writing to " + outputFile);
        }

        // Extract text for main document:
        stripper.writeText(document, output);

        // ... also for any embedded PDFs:
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDDocumentNameDictionary names = catalog.getNames();
        if (names != null)
        {
          PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
          if (embeddedFiles != null)
          {
            Map<String, COSObjectable> embeddedFileNames = embeddedFiles.getNames();
            if (embeddedFileNames != null)
            {
              for (Map.Entry<String, COSObjectable> ent : embeddedFileNames.entrySet())
              {
                if (debug)
                {
                  System.err.println("Processing embedded file " + ent.getKey() + ":");
                }
                PDComplexFileSpecification spec = (PDComplexFileSpecification) ent.getValue();
                PDEmbeddedFile file = spec.getEmbeddedFile();
                if (file.getSubtype().equals("application/pdf"))
                {
                  if (debug)
                  {
                    System.err.println("  is PDF (size=" + file.getSize() + ")");
                  }
                  InputStream fis = file.createInputStream();
                  PDDocument subDoc = null;
                  try
                  {
                    subDoc = PDDocument.load(fis);
                  }
                  finally
                  {
                    fis.close();
                  }
                  try
                  {
                    stripper.writeText(subDoc, output);
                  }
                  finally
                  {
                    subDoc.close();
                  }
                }
              }
            }
          }
        }
        stopProcessing("Time for extraction: ", startTime);

        // Eugene Su
        startTime = startProcessing("Starting XML generation");
        if (hasInfoXml)
        {
          outputTextInfoXML(stripper, pdfFile.substring(0, pdfFile.length() - 4));
        }
        if (enableTestCJK)
        {
          // a test function for vertically writing with CJK fonts
          outputTexttoPDF(stripper, pdfFile.substring(0, pdfFile.length() - 4));
        }
        stopProcessing("Time for XML generation: ", startTime);
      }
      finally
      {
        if (output != null)
        {
          output.close();
        }
        if (document != null)
        {
          document.close();
        }
      }
    }
  }

  private long startProcessing(String message)
  {
    if (debug)
    {
      System.err.println(message);
    }
    return System.currentTimeMillis();
  }

  private void stopProcessing(String message, long startTime)
  {
    if (debug)
    {
      long stopTime = System.currentTimeMillis();
      float elapsedTime = ((float) (stopTime - startTime)) / 1000;
      System.err.println(message + elapsedTime + " seconds");
    }
  }

  private int getWMode(TextPosition text)
  {
    int wmode = 0;
    CMap cmap = text.getFont().getCMap();

    if (cmap != null && cmap.getWMode() == 1)
    {
      wmode = 1;
    }

    return wmode;
  }

  private boolean isVerticalWriting(TextPosition text)
  {
    boolean isVerticalWriting = false;
    int wmode = getWMode(text);
    float dir = text.getDir();
    int rotation = text.getRot();

    if (rotation == 0 || rotation == 180)
    {
      if (wmode == 0)
      {
        if (dir == 0 || dir == 180)
        {
          isVerticalWriting = false;
        }
        else if (dir == 270 || dir == 90)
        {
          isVerticalWriting = true;
        }
      }
      else
      {
        if (dir == 0 || dir == 180)
        {
          isVerticalWriting = true;
        }
      }
    }
    else
    {
      if (wmode == 0)
      {
        if (dir == 90 || dir == 270)
        {
          isVerticalWriting = false;
        }
        else if (dir == 0 || dir == 180)
        {
          isVerticalWriting = true;
        }
      }
      else
      {
        if (dir == 90 || dir == 270)
        {
          isVerticalWriting = true;
        }
      }
    }

    return isVerticalWriting;
  }

  private boolean isVerticalWriting(Vector<List<TextPosition>> charactersByArticle)
  {
    boolean isVerticalWriting = false;
    int vCount = 0;
    int hCount = 0;

    if (charactersByArticle.size() != 0)
    {
      for (int i = 0; i < charactersByArticle.size(); i++)
      {
        List<TextPosition> textList = charactersByArticle.get(i);
        for (int j = 0; j < textList.size(); j++)
        {
          if (!isVerticalWriting(textList.get(j)))
          {
            hCount++;
          }
          else
          {
            vCount++;
          }
        }
      }

      if (vCount > hCount)
      {
        isVerticalWriting = true;
      }
    }

    return isVerticalWriting;
  }

  private void sortCharacters(Vector<List<TextPosition>> charactersByArticle)
  {
    if (charactersByArticle.size() != 0)
    {
      boolean isVerticalWriting = isVerticalWriting(charactersByArticle);

      for (int i = 0; i < charactersByArticle.size(); i++)
      {
        List<TextPosition> textList = charactersByArticle.get(i);

        if (!isVerticalWriting)
        {
          Collections.sort(textList, new TextPositionComparatorH());
        }
        else
        {
          Collections.sort(textList, new TextPositionComparatorV());
        }
      }

      if (!isVerticalWriting)
      {
        Collections.sort(charactersByArticle, new CharactersByArticleComparatorH());
      }
      else
      {
        Collections.sort(charactersByArticle, new CharactersByArticleComparatorV());
      }
    }
  }

  /**
   * Outputs text information in XML format
   * @param stripper The pdf text stripper.
   * @param bookName The name of the pdf.
   */
  private void outputTextInfoXML(PDFTextStripper stripper, String bookName)
  {
    Map<Integer, Vector<List<TextPosition>>> positionListMapping =
        stripper.getPositionListMapping();
    Map<String, PDFont> fontMapping = new HashMap<String, PDFont>();

    try
    {
      FileOutputStream outputStreams[] =
          { new FileOutputStream(bookName + ".xml", false),
              new FileOutputStream(bookName + ".simple.xml", false) };

      BufferedWriter writers[] = new BufferedWriter[outputStreams.length];
      for (int i = 0; i < outputStreams.length; i++)
      {
        writers[i] = new BufferedWriter(new OutputStreamWriter(outputStreams[i], "UTF-8"));
      }

      for (int i = 0; i < outputStreams.length; i++)
      {
        writers[i].write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        writers[i].write("<"
            + new String(("Book name=\"" + bookName + "\"").getBytes("UTF-8"), "UTF-8") + ">\r\n");
      }

      for (int pageNo = stripper.getStartPage(); pageNo < (stripper.getStartPage() + positionListMapping
          .size()); pageNo++)
      {
        if (debug)
        {
          System.out.println("Outputing XML page " + pageNo);
        }

        boolean hasContent = true;
        Vector<List<TextPosition>> charactersByArticle =
            positionListMapping.get(new Integer(pageNo));

        sortCharacters(charactersByArticle);

        if (charactersByArticle.size() != 0)
        {
          if (charactersByArticle.get(0).size() != 0)
          {
            writers[0].write("<Page num=\"" + pageNo + "\" " + "width=\""
                + charactersByArticle.get(0).get(0).getPageWidth() + "\"  " + "height=\""
                + charactersByArticle.get(0).get(0).getPageHeight() + "\"  " + "rotation=\""
                + charactersByArticle.get(0).get(0).getRot() + "\">\r\n");

            writers[1].write("<Page num=\"" + pageNo + "\">\r\n");
          }
          else
          {
            hasContent = false;
          }

          for (int i = 0; i < charactersByArticle.size() && hasContent; i++)
          {
            List<TextPosition> lists = charactersByArticle.get(i);

            // Error in writing vertically with CJK fonts if sorting
            // text position // Eugene Su
            // TextPositionComparator comparator = new
            // TextPositionComparator();
            // Collections.sort( lists, comparator );

            for (int j = 0; j < lists.size(); j++)
            {
              short strLen = (short) lists.get(j).getCharacter().length();
              String str = lists.get(j).getCharacter();
              String infoString = new String();
              String simpleString = new String();

              infoString += "<Content text=\"" + StringEscapeUtils.escapeXml(str) + "\">\r\n";

              for (byte strIdx = 0; strIdx < strLen; strIdx++)
              {
                infoString +=
                    "<Unicode>" + Integer.toString(str.codePointAt(strIdx), 16) + "</Unicode>";
                simpleString +=
                    "<Unicode>" + Integer.toString(str.codePointAt(strIdx), 16) + "</Unicode>";
              }

              infoString += "<Dir>" + lists.get(j).getDir() + "</Dir>";

              infoString += "<WMode>" + getWMode(lists.get(j)) + "</WMode>";

              int verticalWriting = isVerticalWriting(lists.get(j)) ? 1 : 0;
              infoString += "<WMDir>" + verticalWriting + "</WMDir>";

              infoString +=
                  "<XPos>" + roundHalfUp(lists.get(j).getTextPos().getXPosition()) + "</XPos>";
              infoString +=
                  "<YPos>" + roundHalfUp(lists.get(j).getTextPos().getYPosition()) + "</YPos>";

              infoString += "<XRot>" + roundHalfUp(lists.get(j).getX()) + "</XRot>";
              infoString += "<YRot>" + roundHalfUp(lists.get(j).getY()) + "</YRot>";
              simpleString += "<XRot>" + roundHalfUp(lists.get(j).getX()) + "</XRot>";
              simpleString += "<YRot>" + roundHalfUp(lists.get(j).getY()) + "</YRot>\r\n";

              infoString += "<XDir>" + roundHalfUp(lists.get(j).getXDirAdj()) + "</XDir>";
              infoString += "<YDir>" + roundHalfUp(lists.get(j).getYDirAdj()) + "</YDir>";

              infoString += "<Height>" + roundHalfUp(lists.get(j).getHeight()) + "</Height>";

              infoString +=
                  "<HeightDir>" + roundHalfUp(lists.get(j).getHeightDir()) + "</HeightDir>";

              infoString += "<Width>" + roundHalfUp(lists.get(j).getWidth()) + "</Width>";

              infoString +=
                  "<WidthDir>" + roundHalfUp(lists.get(j).getWidthDirAdj()) + "</WidthDir>";

              infoString += "<Color>" + lists.get(j).getTextColor().getRGB() + "</Color>";

              int italics = lists.get(j).isItalics() ? 1 : 0;
              infoString += "<Italics>" + italics + "</Italics>";

              infoString +=
                  "<RenderingMode>" + lists.get(j).getTextRenderingMode() + "</RenderingMode>";

              infoString +=
                  "<WidthOfSpace>" + roundHalfUp(lists.get(j).getWidthOfSpace())
                      + "</WidthOfSpace>";

              PDFontDescriptor fontDescriptor = getFontDescriptor(lists.get(j).getFont());
              if (fontDescriptor != null)
              {
                String fontName = fontDescriptor.getFontName();

                infoString += "<FontName>" + fontName + "</FontName>";

                if (fontMapping.get(fontName) == null)
                {
                  fontMapping.put(fontName, lists.get(j).getFont());
                }
              }
              else
              {
                System.err.println("NULL fontDescriptor");
              }

              infoString +=
                  "<FontSizeInPt>" + lists.get(j).getFontSizeInPt() + "</FontSizeInPt>\r\n";

              infoString += "</Content>\r\n";

              writers[0].write(infoString);
              writers[1].write(simpleString);
            }
          }

          if (hasContent)
          {
            for (int i = 0; i < outputStreams.length; i++)
            {
              writers[i].write("</Page>\r\n");
            }
          }
        }
      }

      for (int i = 0; i < outputStreams.length; i++)
      {
        writers[i].write("</Book>");

        writers[i].flush();
        writers[i].close();
        outputStreams[i].flush();
        outputStreams[i].close();
      }

      List<Map.Entry<String, PDFont>> entries =
          new ArrayList<Map.Entry<String, PDFont>>(fontMapping.entrySet());
      FileOutputStream fileOutputStream = new FileOutputStream(bookName + ".font.xml", false);
      BufferedWriter bufferedWriter =
          new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));

      bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
      bufferedWriter.write("<"
          + new String(("Book name=\"" + bookName + "\"").getBytes("UTF-8"), "UTF-8") + ">\r\n");

      //Collections.sort(entries, (e1, e2) -> e1.getKey().compareTo(e2.getKey()));
      Collections.sort(entries, new Comparator<Map.Entry<String, PDFont>>() {
      public int compare(Map.Entry<String, PDFont> e1, 
                         Map.Entry<String, PDFont> e2) {
          return e1.getKey().compareTo(e2.getKey());
        }
      });
      
      for (Map.Entry<String, PDFont> entry : entries)
      {
        String fontKey = entry.getKey();
        PDFont font = entry.getValue();
        String infoString = new String();
        PDFontDescriptorDictionary fontDescriptorDic =
            (PDFontDescriptorDictionary) getFontDescriptor(font);
        if (fontDescriptorDic.getFontFile2() == null)
        {
          continue;
        }
        InputStream fontFile2 = fontDescriptorDic.getFontFile2().createInputStream();
        byte[] buffer = new byte[fontFile2.available() * 2];
        int length = -1;
        CMap cmap = null;
        FileOutputStream fouts = new FileOutputStream(fontKey + ".bin", false);

        infoString +=
            "<" + new String(("Font name=\"" + fontKey + "\"").getBytes("UTF-8"), "UTF-8")
                + ">\r\n";
        infoString += "<FontFamily>" + fontDescriptorDic.getFontFamily() + "</FontFamily>\r\n";
        infoString += "<FontFile2>";
        while ((length = fontFile2.read(buffer)) != -1)
        {
          byte[] data = Arrays.copyOf(buffer, length);
          infoString += Base64.encodeBase64String(data);
          fouts.write(buffer, 0, length);
        }
        infoString += "</FontFile2>\r\n";
        fontFile2.close();
        fouts.flush();
        fouts.close();

        if (font.getToUnicodeCMap() != null)
        {
          cmap = font.getToUnicodeCMap();
        }
        else
        {
          PDFont descendentFont = getDescendentFont(font);

          if (descendentFont != null && descendentFont.getCMap() != null)
          {
            cmap = descendentFont.getCMap();
          }
          else if (font.getCMap() != null)
          {
            cmap = font.getCMap();
          }
        }

        if (cmap != null)
        {
          ByteArrayOutputStream byteOuts = new ByteArrayOutputStream();
          ObjectOutputStream objOutputStream = new ObjectOutputStream(byteOuts);
          objOutputStream.writeObject(cmap);
          objOutputStream.flush();
          objOutputStream.close();
          infoString += "<Cmap>";
          infoString += Base64.encodeBase64String(byteOuts.toByteArray());
          byteOuts.close();
          infoString += "</Cmap>\r\n";
        }

        infoString += "</Font>\r\n";

        bufferedWriter.write(infoString);
      }

      bufferedWriter.write("</Book>");

      bufferedWriter.flush();
      bufferedWriter.close();
      fileOutputStream.flush();
      fileOutputStream.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * This will get the font descriptor for this font.
   * @return The font descriptor for this font.
   */
  public static PDFontDescriptor getFontDescriptor(PDFont font)
  {
    PDFontDescriptor fontDescriptor = font.getFontDescriptor();

    if (fontDescriptor == null)
    {
      COSArray descendantFonts =
          (COSArray) ((COSDictionary) font.getCOSObject())
              .getDictionaryObject(COSName.DESCENDANT_FONTS);

      if (descendantFonts != null)
      {
        COSDictionary descendantFontDictionary = (COSDictionary) descendantFonts.getObject(0);
        if (descendantFontDictionary != null)
        {
          try
          {
            PDFont descendentFont = PDFontFactory.createFont(descendantFontDictionary);
            fontDescriptor = descendentFont.getFontDescriptor();
          }
          catch (IOException exception)
          {

          }
        }
      }
    }

    return fontDescriptor;
  }

  // Eugene Su
  public static float roundHalfUp(float fVal)
  {
    // return new BigDecimal(fVal).setScale(5,
    // BigDecimal.ROUND_HALF_UP).floatValue();
    return fVal;
  }

  // Eugene Su
  private static final String resourceRootCMAP = "org/apache/pdfbox/resources/cmap/";
  private static final String resourceRootTTF = "org/apache/pdfbox/resources/ttf/";

  private void outputTexttoPDF(PDFTextStripper stripper, String bookName) throws IOException
  {
    PDDocument doc = null;
    Map<Integer, Vector<List<TextPosition>>> positionListMapping =
        stripper.getPositionListMapping();
    Map<String, PDFont> fontMapping = new HashMap<String, PDFont>();
    Map<String, TrueTypeFont> ttfMapping = new HashMap<String, TrueTypeFont>();

    // external fonts
    /*
     * String[][] loadedFonts = { { "PMingLiU.ttf", "Identity-V", "Identity", "" }, {
     * "PMingLiU.ttf", "Identity-H", "Identity", "" }, { "calibri.ttf", "Identity-H", "Identity", ""
     * }, { "calibri.ttf", "", "", "" }, { "ae_AlArabiya.ttf", "Identity-H", "Identity", "" }, {
     * "ae_AlArabiya.ttf", "", "", "" }, { "trado.ttf", "Identity-H", "Identity", "" }, {
     * "trado.ttf", "", "", "" } };
     */

    try
    {
      doc = new PDDocument();
      PDPage page = null;
      PDPageContentStream contentStream = null;

      File lineboxXmlFile = new File(bookName + ".linebox.xml");
      Document lineboxXmlDoc = null;
      if (lineboxXmlFile.exists())
      {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        lineboxXmlDoc = dBuilder.parse(lineboxXmlFile);

        lineboxXmlDoc.getDocumentElement().normalize();
      }

      // Load external fonts
      /*
       * for (int i = 0; i < loadedFonts.length; i++) { PDFont font; Encoding enc = new
       * WinAnsiEncoding(); if (loadedFonts[i][1].equals("Identity-V")) { enc = new
       * IdentityVEncoding(); } else if (loadedFonts[i][1].equals("Identity-H")) { enc = new
       * IdentityHEncoding(); } if (COSName.WIN_ANSI_ENCODING.equals(enc.getCOSObject())) { font =
       * PDTrueTypeFont.loadTTF(doc, ResourceLoader.loadResource(resourceRootTTF +
       * loadedFonts[i][0])); } else { font = PDType0Font.loadTTF(doc,
       * ResourceLoader.loadResource(resourceRootTTF + loadedFonts[i][0]), enc); } if
       * (loadedFonts[i][3] != "") { fontMapping.put(((COSName) enc.getCOSObject()).getName() + "+"
       * + loadedFonts[i][3], font); } else { fontMapping.put(((COSName)
       * enc.getCOSObject()).getName() + "+" + font.getBaseFont().replaceAll("\u0000", ""), font); }
       * if (loadedFonts[i][2] != "") { InputStream ins =
       * ResourceLoader.loadResource(resourceRootCMAP + loadedFonts[i][2]); byte[] cmapBin = new
       * byte[ins.available() * 2]; int cmapLength = ins.read(cmapBin); ins.close(); COSStream
       * toUnicode = new COSStream(new RandomAccessBuffer()); OutputStream outs =
       * toUnicode.createUnfilteredStream(); outs.write(cmapBin, 0, cmapLength); outs.flush();
       * outs.close(); toUnicode.setFilters(COSName.FLATE_DECODE); if
       * (COSName.WIN_ANSI_ENCODING.equals(enc)) { ((PDTrueTypeFont) font).setToUnicode(toUnicode);
       * // attach // toUnicode } else { ((PDType0Font) font).setToUnicode(toUnicode); // attach //
       * toUnicode } } }
       */

      for (int pageNo = stripper.getStartPage(); pageNo < (stripper.getStartPage() + positionListMapping
          .size()); pageNo++)
      {
        if (debug)
        {
          System.out.println("Outputing PDF page " + pageNo);
        }

        PDRectangle pageRectangle = new PDRectangle((float) 595.22, (float) 842);

        if (stripper.getCurrentPage().getBleedBox() != null)
        {
          pageRectangle = stripper.getCurrentPage().getBleedBox().createRetranslatedRectangle();
        }
        else if (stripper.getCurrentPage().getCropBox() != null)
        {
          pageRectangle = stripper.getCurrentPage().getCropBox().createRetranslatedRectangle();
        }
        else if (stripper.getCurrentPage().getMediaBox() != null)
        {
          pageRectangle = stripper.getCurrentPage().getMediaBox().createRetranslatedRectangle();
        }

        page = new PDPage();
        doc.addPage(page);
        page.setMediaBox(pageRectangle);
        contentStream = null;
        contentStream = new PDPageContentStream(doc, page);
        contentStream.beginText();

        Vector<List<TextPosition>> charactersByArticle =
            positionListMapping.get(new Integer(pageNo));

        if (charactersByArticle.size() != 0)
        {
          boolean isVerticalWriting = isVerticalWriting(charactersByArticle);

          for (int i = 0; i < charactersByArticle.size(); i++)
          {
            List<TextPosition> lists = charactersByArticle.get(i);

            float xLastPoint = Float.MIN_VALUE;
            float yLastPoint = Float.MIN_VALUE;
            TextPosition lastTextPos = null;

            for (int j = 0; j < lists.size(); j++)
            {
              // Eugene Su mark for testing
              // for (int k = 0; k <
              // lists.get(j).getCharacter().length(); k++)
              {
                String subString = lists.get(j).getCharacter(); // .substring(k,
                // k
                // +
                // 1);
                // //
                // mark
                // for
                // testing
                String baseFontName = lists.get(j).getFont().getBaseFont();
                Encoding enc = lists.get(j).getFont().getFontEncoding();
                String[] fontName = baseFontName.split("\\+", 2);
                String fontKey;
                CMap cmap = lists.get(j).getFont().getCMap();
                CMap toUnicodeCmap = lists.get(j).getFont().getToUnicodeCMap();
                int wmode = 0;
                PDFont font;
                TrueTypeFont ttf;
                double fontSize = lists.get(j).getFontSizeInPt();
                String encName;
                PDFont descendentFont;
                boolean hasTwoByteMappings = false;

                if (enc == null)
                {
                  if (cmap != null)
                  {
                    encName = cmap.getName();
                  }
                  else
                  {
                    /*
                     * if(toUnicodeCmap != null) { fontKey = toUnicodeCmap.getName() + "+"; encName
                     * = toUnicodeCmap.getName(); } else
                     */
                    {
                      encName = "WinAnsiEncoding";
                    }
                  }
                }
                else
                {
                  if ((COSBase) enc.getCOSObject() instanceof COSDictionary)
                  {
                    encName = "DictionaryEncoding";
                  }
                  else
                  {
                    encName = ((COSName) enc.getCOSObject()).getName();
                  }
                }
                fontKey = encName + "+";

                if (cmap != null && cmap.getWMode() == 1)
                {
                  wmode = 1;
                }

                font = fontMapping.get(fontKey + baseFontName);
                if (font == null)
                {
                  if (fontName.length == 2)
                  {
                    fontKey = fontKey + fontName[1];
                  }
                  else if (fontName.length == 1)
                  {
                    fontKey = fontKey + fontName[0];
                  }
                  else
                  {
                    fontKey = fontKey + baseFontName;
                  }

                  font = fontMapping.get(fontKey);
                  if (font == null)
                  {
                    font = lists.get(j).getFont();
                    if (font == null)
                    {
                      throw new Exception(fontKey + " Not Fount!!");
                    }
                    else
                    {
                      fontKey = encName + "+" + baseFontName;
                      contentStream.setFont(font, 1);
                    }
                  }
                  else
                  {
                    contentStream.setFont(font, 1);
                  }
                }
                else
                {
                  contentStream.setFont(font, 1);
                }

                if (lists.get(j).getDir() == 0)
                {
                  contentStream.setTextMatrix(fontSize, 0, 0, fontSize, lists.get(j).getTextPos()
                      .getXPosition(), lists.get(j).getTextPos().getYPosition());
                }
                else if (lists.get(j).getDir() == 90)
                {
                  contentStream.setTextMatrix(0, fontSize, fontSize * -1, 0, lists.get(j)
                      .getTextPos().getXPosition(), lists.get(j).getTextPos().getYPosition());
                }
                else if (lists.get(j).getDir() == 180)
                {
                  contentStream.setTextMatrix(fontSize * -1, 0, 0, fontSize * -1, lists.get(j)
                      .getTextPos().getXPosition(), lists.get(j).getTextPos().getYPosition());
                }
                else if (lists.get(j).getDir() == 270)
                {
                  contentStream.setTextMatrix(0, fontSize * -1, fontSize, 0, lists.get(j)
                      .getTextPos().getXPosition(), lists.get(j).getTextPos().getYPosition());
                }

                if (true)
                {
                  String str = subString;
                  int charCode = -1;

                  descendentFont = getDescendentFont(font);

                  // Eugene Su for debugging
                  if (debug)
                  {
                    if (descendentFont != null)
                    {
                      COSArray descendantFonts =
                          (COSArray) ((COSDictionary) font.getCOSObject())
                              .getDictionaryObject(COSName.DESCENDANT_FONTS);
                      COSDictionary descendantFontDictionary =
                          (COSDictionary) descendantFonts.getObject(0);

                      COSBase map =
                          descendantFontDictionary.getDictionaryObject(COSName.CID_TO_GID_MAP);
                      int[] cid2gid = null;
                      if (map instanceof COSStream)
                      {
                        COSStream stream = (COSStream) map;
                        byte[] mapAsBytes = IOUtils.toByteArray(stream.getUnfilteredStream());
                        int numberOfInts = mapAsBytes.length / 2;
                        cid2gid = new int[numberOfInts];
                        int offset = 0;
                        for (int index = 0; index < numberOfInts; index++)
                        {
                          cid2gid[index] = font.getCodeFromArray(mapAsBytes, offset, 2);
                          offset += 2;
                        }
                      }

                      COSBase fd = descendantFontDictionary.getDictionaryObject(COSName.FONT_DESC);
                      List<Integer> cidList = null;
                      if (fd instanceof COSDictionary)
                      {
                        map = ((COSDictionary) fd).getDictionaryObject("CIDSet");
                        if (map instanceof COSStream)
                        {
                          COSStream stream = (COSStream) map;
                          byte[] mapAsBytes = IOUtils.toByteArray(stream.getUnfilteredStream());
                          int numberOfInts = mapAsBytes.length;
                          cidList = new ArrayList<Integer>();
                          for (int index = 0; index < numberOfInts; index++)
                          {
                            byte cidset = mapAsBytes[index];

                            if ((cidset & 0x80) != 0)
                            {
                              int cid = index * 8 + 0;
                              cidList.add(cid);
                            }

                            if ((cidset & 0x40) != 0)
                            {
                              int cid = index * 8 + 1;
                              cidList.add(cid);
                            }

                            if ((cidset & 0x20) != 0)
                            {
                              int cid = index * 8 + 2;
                              cidList.add(cid);
                            }

                            if ((cidset & 0x10) != 0)
                            {
                              int cid = index * 8 + 3;
                              cidList.add(cid);
                            }

                            if ((cidset & 0x08) != 0)
                            {
                              int cid = index * 8 + 4;
                              cidList.add(cid);
                            }

                            if ((cidset & 0x04) != 0)
                            {
                              int cid = index * 8 + 5;
                              cidList.add(cid);
                            }

                            if ((cidset & 0x02) != 0)
                            {
                              int cid = index * 8 + 6;
                              cidList.add(cid);
                            }

                            if ((cidset & 0x01) != 0)
                            {
                              int cid = index * 8 + 7;
                              cidList.add(cid);
                            }
                          }
                        }
                      }

                      System.out.println();
                    }
                  }

                  if (toUnicodeCmap != null)
                  {
                    charCode = toUnicodeCmap.lookupCharCode(str);
                    hasTwoByteMappings = toUnicodeCmap.hasTwoByteMappings();
                  }

                  if (debug && charCode == -1 && !Character.isSpaceChar(str.charAt(0)))
                  {
                    System.out.println("Warn: Unicode U+"
                        + Integer.toString(str.codePointAt(0), 16)
                        + " may not be in toUnicodeCmap.");
                  }

                  if (charCode == -1 && descendentFont != null && descendentFont.getCMap() != null)
                  {
                    charCode = descendentFont.getCMap().lookupCharCode(str);
                  }

                  if (charCode == -1 && cmap != null)
                  {
                    charCode = cmap.lookupCharCode(str);
                  }

                  if (charCode == -1)
                  {
                    charCode = str.codePointAt(0);
                  }

                  if (font.getFontEncoding() != null
                      && font.getFontEncoding().getCOSObject() != null
                      && !((COSBase) font.getFontEncoding().getCOSObject() instanceof COSDictionary)
                      && ((COSName) font.getFontEncoding().getCOSObject()).getName().equals(
                        ((COSName) COSName.WIN_ANSI_ENCODING).getName()))
                  {
                    contentStream.drawRawText("<" + Integer.toString(charCode, 16) + ">");
                  }
                  else
                  {
                    /*
                     * if (subString.codePointAt(0) == 0xA0) { contentStream.drawRawText("<0020>");
                     * } else
                     */
                    {
                      // check single or two double byte
                      if (charCode < 0x100 && !hasTwoByteMappings)
                      {
                        contentStream.drawRawText("<" + String.format("%02X", charCode) + ">");
                      }
                      else
                      {
                        contentStream.drawRawText("<" + String.format("%04X", charCode) + ">");
                      }
                    }
                  }

                  if (((PDFontDescriptorDictionary) getFontDescriptor(font)).getFontFile2() != null)
                  {
                    if (!Character.isSpaceChar(str.charAt(0)))
                    {
                      // ===============================================
                      //
                      // Testing stating point of fonts
                      //
                      // ===============================================
                      ttf = ttfMapping.get(fontKey);
                      if (ttf == null)
                      {
                        PDFontDescriptorDictionary fontDescriptor =
                            (PDFontDescriptorDictionary) getFontDescriptor(lists.get(j).getFont());
                        InputStream ttfData = fontDescriptor.getFontFile2().createInputStream();
                        CIDFontType2Parser parser = new CIDFontType2Parser(true);
                        System.out.println("\nFont ======= " + fontKey); // Eugene
                        // Su
                        ttf = parser.parseTTF(ttfData);
                        ttfMapping.put(fontKey, ttf);
                      }

                      HeaderTable headerTable = ttf.getHeader();
                      float unitsPerEm = 1000f / headerTable.getUnitsPerEm();
                      float scaling = unitsPerEm * lists.get(j).getFontSizeInPt() / 1000;

                      HorizontalHeaderTable hhea = ttf.getHorizontalHeader();
                      float hDescent = hhea.getDescender();

                      HorizontalMetricsTable hmtx = ttf.getHorizontalMetrics();
                      int[] advanceWidths = hmtx.getAdvanceWidth();
                      short[] leftSideBearings = hmtx.getLeftSideBearing();

                      VerticalMetricsTable vmtx = ttf.getVerticalMetrics();
                      int[] advanceHeights = null;
                      if (vmtx != null)
                      {
                        advanceHeights = vmtx.getAdvanceHeight();
                      }

                      GlyphTable glyphTable = ttf.getGlyph();
                      GlyphData[] glyphs = glyphTable.getGlyphs();

                      CMAPTable cmapTable = ttf.getCMAP();
                      CMAPEncodingEntry[] cmaps = null;
                      if (cmapTable != null)
                      {
                        cmaps = cmapTable.getCmaps();
                      }
                      CMAPEncodingEntry uniMap = null;
                      for (int x = 0; cmaps != null && x < cmaps.length; x++)
                      {
                        if (cmaps[x].getPlatformId() == CMAPTable.PLATFORM_WINDOWS)
                        {
                          int platformEncoding = cmaps[x].getPlatformEncodingId();
                          if (CMAPTable.ENCODING_UNICODE == platformEncoding)
                          {
                            uniMap = cmaps[x];
                            break;
                          }
                        }
                      }

                      if (uniMap == null)
                      {
                        for (int x = 0; cmaps != null && x < cmaps.length; x++)
                        {
                          if (cmaps[x].getPlatformId() == CMAPTable.PLATFORM_MACINTOSH)
                          {
                            int platformEncoding = cmaps[x].getPlatformEncodingId();
                            if (CMAPTable.ENCODING_MACINTOSH == platformEncoding)
                            {
                              uniMap = cmaps[x];
                              break;
                            }
                          }
                        }
                      }

                      int gid = -1;
                      if (uniMap != null)
                      {
                        gid = uniMap.getGlyphId(charCode);
                      }
                      else
                      {
                        gid = charCode;
                      }
                      if (gid < 0 || gid >= glyphs.length || glyphs[gid] == null)
                      {
                        gid = 0;
                        System.err.printf("charCode " + Integer.toString(charCode, 16)
                            + " Not Found gid!!\r\n");
                      }

                      float glyphWidth = glyphs[gid].getBoundingBox().getWidth() * scaling;
                      float glyphHeight = glyphs[gid].getBoundingBox().getHeight() * scaling;
                      float llx = glyphs[gid].getBoundingBox().getLowerLeftX() * scaling;
                      float lly = glyphs[gid].getBoundingBox().getLowerLeftY() * scaling;
                      float urx = glyphs[gid].getBoundingBox().getUpperRightX() * scaling;
                      float ury = glyphs[gid].getBoundingBox().getUpperRightY() * scaling;
                      float deltaY =
                          Math.abs(glyphs[gid].getBoundingBox().getLowerLeftY()
                              + Math.abs(hDescent))
                              * scaling;
                      float advanceWidth;
                      float leftSideBearing;
                      float advanceHeight;
                      if (advanceWidths.length < gid)
                      {
                        advanceWidth = llx + glyphWidth;
                      }
                      else
                      {
                        advanceWidth = advanceWidths[gid] * scaling;
                      }
                      if (leftSideBearings.length < gid)
                      {
                        leftSideBearing = 0;
                      }
                      else
                      {
                        leftSideBearing = leftSideBearings[gid] * scaling;
                      }

                      if (vmtx != null && advanceHeights.length > gid)
                      {
                        advanceHeight = advanceHeights[gid] * scaling;
                      }
                      else
                      {
                        advanceHeight = advanceWidth;
                      }

                      System.out.println(subString + " (U+" + Integer.toString(charCode, 16)
                          + ") : " + headerTable.getFlags() + " : "
                          + glyphs[gid].getBoundingBox().toString() + " : [" + advanceWidth + ", "
                          + advanceHeight + "] : [" + glyphWidth + ", " + glyphHeight + "] : ["
                          + leftSideBearing + ", " + deltaY + "] : [" + llx + ", " + lly + ", "
                          + urx + ", " + ury + "]");

                      if (isVerticalWriting(lists.get(j)))
                      {
                        // vertically writing
                        if (wmode == 1)
                        {
                          contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition()
                              - advanceWidth / 2 + llx)
                              + " "
                              + (Math.floor(lists.get(j).getTextPos().getYPosition())
                                  - advanceHeight + deltaY + glyphHeight)
                              + " "
                              + glyphWidth
                              + " "
                              + 0.1 + " re\n"); // top
                          contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition()
                              - advanceWidth / 2 + llx)
                              + " "
                              + (Math.floor(lists.get(j).getTextPos().getYPosition())
                                  - advanceHeight + deltaY)
                              + " "
                              + glyphWidth
                              + " "
                              + 0.1
                              + " re\n"); // bottom
                          contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition()
                              - advanceWidth / 2 + llx)
                              + " "
                              + (Math.floor(lists.get(j).getTextPos().getYPosition())
                                  - advanceHeight + deltaY)
                              + " "
                              + 0.1
                              + " "
                              + glyphHeight
                              + " re\n"); // left
                          contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition()
                              - advanceWidth / 2 + llx + glyphWidth)
                              + " "
                              + (Math.floor(lists.get(j).getTextPos().getYPosition())
                                  - advanceHeight + deltaY)
                              + " "
                              + 0.1
                              + " "
                              + glyphHeight
                              + " re\n"); // right

                          if (!(yLastPoint < lists.get(j).getTextPos().getYPosition()))
                          {
                            contentStream.drawRawCmd(lists.get(j).getTextPos().getXPosition()
                                + " "
                                + Math.floor(lists.get(j).getTextPos().getYPosition())
                                + " "
                                + 0.1
                                + " "
                                + (yLastPoint - Math
                                    .floor(lists.get(j).getTextPos().getYPosition())) + " re\n");
                          }
                          else if (lastTextPos != null && yLastPoint > 1)
                          {
                            contentStream
                                .drawRawCmd((lastTextPos.getTextPos().getXPosition() + lastTextPos
                                    .getHeightDir() / 2)
                                    + " "
                                    + lastTextPos.getTextPos().getYPosition()
                                    + " "
                                    + 0.1
                                    + " "
                                    + (yLastPoint - lastTextPos.getTextPos().getYPosition())
                                    + " re\n");
                          }
                          xLastPoint = lists.get(j).getTextPos().getXPosition();
                          yLastPoint = (float) Math.floor(lists.get(j).getTextPos().getYPosition());
                          lastTextPos = lists.get(j);
                          contentStream
                              .drawRawCmd((lists.get(j).getTextPos().getXPosition() - 0.5 / 2)
                                  + " "
                                  + (Math.floor(lists.get(j).getTextPos().getYPosition()) - 0.5)
                                  + " " + 0.5 + " " + 0.5 + " re\n");
                        }
                        else
                        {
                          contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition() + lly)
                              + " " + (lists.get(j).getTextPos().getYPosition() - llx) + " "
                              + glyphHeight + " " + 0.1 + " re\n"); // top
                          contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition() + lly)
                              + " " + (lists.get(j).getTextPos().getYPosition() - llx - glyphWidth)
                              + " " + glyphHeight + " " + 0.1 + " re\n"); // bottom
                          contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition() + lly)
                              + " " + (lists.get(j).getTextPos().getYPosition() - llx - glyphWidth)
                              + " " + 0.1 + " " + glyphWidth + " re\n"); // left
                          contentStream
                              .drawRawCmd((lists.get(j).getTextPos().getXPosition() + lly + glyphHeight)
                                  + " "
                                  + (lists.get(j).getTextPos().getYPosition() - llx - glyphWidth)
                                  + " " + 0.1 + " " + glyphWidth + " re\n"); // right

                          if (!(yLastPoint < lists.get(j).getTextPos().getYPosition()))
                          {
                            contentStream
                                .drawRawCmd((lists.get(j).getTextPos().getXPosition() + Math
                                    .ceil(lists.get(j).getHeightDir() / 2))
                                    + " "
                                    + lists.get(j).getTextPos().getYPosition()
                                    + " "
                                    + 0.1
                                    + " "
                                    + (yLastPoint - lists.get(j).getTextPos().getYPosition())
                                    + " re\n");
                          }
                          else if (lastTextPos != null && yLastPoint > 1)
                          {
                            contentStream
                                .drawRawCmd((lastTextPos.getTextPos().getXPosition() + Math
                                    .ceil(lastTextPos.getHeightDir() / 2))
                                    + " "
                                    + lastTextPos.getTextPos().getYPosition()
                                    + " "
                                    + 0.1
                                    + " "
                                    + (yLastPoint - lastTextPos.getTextPos().getYPosition())
                                    + " re\n");
                          }
                          xLastPoint =
                              lists.get(j).getTextPos().getXPosition()
                                  + lists.get(j).getHeightDir() / 2;
                          yLastPoint = lists.get(j).getTextPos().getYPosition();
                          lastTextPos = lists.get(j);
                          contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition()
                              + Math.ceil(lists.get(j).getHeightDir() / 2) - 0.5 / 2)
                              + " "
                              + (lists.get(j).getTextPos().getYPosition() - 0.5)
                              + " "
                              + 0.5
                              + " " + 0.5 + " re\n");
                        }
                      }
                      else
                      {
                        // normal
                        contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition() + llx)
                            + " " + (lists.get(j).getTextPos().getYPosition() + lly + glyphHeight)
                            + " " + glyphWidth + " " + 0.1 + " re\n");
                        contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition() + llx)
                            + " " + (lists.get(j).getTextPos().getYPosition() + lly) + " "
                            + glyphWidth + " " + 0.1 + " re\n");
                        contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition() + llx)
                            + " " + (lists.get(j).getTextPos().getYPosition() + lly) + " " + 0.1
                            + " " + glyphHeight + " re\n");
                        contentStream
                            .drawRawCmd((lists.get(j).getTextPos().getXPosition() + llx + glyphWidth)
                                + " "
                                + (lists.get(j).getTextPos().getYPosition() + lly)
                                + " "
                                + 0.1 + " " + glyphHeight + " re\n");
                      }
                    }
                  }
                  else
                  {
                    if (!Character.isSpaceChar(str.charAt(0)))
                    {
                      // ===============================================
                      //
                      // Testing stating point of fonts
                      //
                      // ===============================================
                      // normal
                      float llx = 0;
                      float lly = 0;
                      float glyphWidth = lists.get(j).getWidthDirAdj();
                      float glyphHeight = lists.get(j).getHeightDir();

                      contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition() + llx)
                          + " " + (lists.get(j).getTextPos().getYPosition() + lly + glyphHeight)
                          + " " + glyphWidth + " " + 0.05 + " re\n");
                      contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition() + llx)
                          + " " + (lists.get(j).getTextPos().getYPosition() + lly) + " "
                          + glyphWidth + " " + 0.05 + " re\n");
                      contentStream.drawRawCmd((lists.get(j).getTextPos().getXPosition() + llx)
                          + " " + (lists.get(j).getTextPos().getYPosition() + lly) + " " + 0.05
                          + " " + glyphHeight + " re\n");
                      contentStream
                          .drawRawCmd((lists.get(j).getTextPos().getXPosition() + llx + glyphWidth)
                              + " " + (lists.get(j).getTextPos().getYPosition() + lly) + " " + 0.05
                              + " " + glyphHeight + " re\n");
                    }
                  }
                }
              }
            }

            if (lineboxXmlFile.exists())
            {
              XPathFactory xPathfactory = XPathFactory.newInstance();
              XPath xpath = xPathfactory.newXPath();
              XPathExpression expr = xpath.compile("//Page[@num=\"" + pageNo + "\"]/Box[@*]");
              NodeList boxlineList =
                  (NodeList) expr.evaluate(lineboxXmlDoc, XPathConstants.NODESET);
              for (int line = 0; line < boxlineList.getLength(); line++)
              {
                Node boxline = boxlineList.item(line);
                NodeList childList = boxline.getChildNodes();
                float xRot1 = Float.valueOf(childList.item(0).getTextContent());
                float yRot1 = Float.valueOf(childList.item(1).getTextContent());
                float xRot2 = Float.valueOf(childList.item(2).getTextContent());
                float yRot2 = Float.valueOf(childList.item(3).getTextContent());
                float boxWidth = xRot2 - xRot1;
                float boxHeight = yRot2 - yRot1;
                float pageWidth = lists.get(0).getPageWidth();
                float pageHeight = lists.get(0).getPageHeight();
                float y1;
                float y2;
                if (lists.get(0).getRot() == 0 || lists.get(0).getRot() == 180)
                {
                  y1 = pageHeight - yRot1;
                  y2 = pageHeight - yRot2;
                }
                else
                {
                  y1 = pageWidth - yRot1;
                  y2 = pageWidth - yRot2;
                }

                if (Math.abs(boxWidth) > Math.max(pageWidth, pageHeight)
                    || Math.abs(boxHeight) > Math.max(pageWidth, pageHeight))
                {
                  continue;
                }

                if (!isVerticalWriting)
                {
                  contentStream
                      .drawRawCmd(xRot1 + " " + y1 + " " + boxWidth + " " + 0.05 + " re\n");
                  contentStream
                      .drawRawCmd(xRot1 + " " + y2 + " " + boxWidth + " " + 0.05 + " re\n");
                }
                else
                {

                  contentStream.drawRawCmd(xRot1 + " " + y2 + " " + 0.05 + " " + boxHeight
                      + " re\n");
                  contentStream.drawRawCmd(xRot2 + " " + y2 + " " + 0.05 + " " + boxHeight
                      + " re\n");
                }
              }
            }

            contentStream.drawRawCmd("f\n");

            if (contentStream != null)
            {
              contentStream.endText();
              contentStream.close();
            }
          }
        }
      }

      doc.save(bookName + ".clone.pdf");
    }
    catch (IOException io)
    {
      throw io;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (doc != null)
      {
        doc.close();
      }
    }
  }

  private static PDFont getDescendentFont(PDFont font)
  {
    PDFont descendentFont = null;
    COSArray descendantFonts =
        (COSArray) ((COSDictionary) font.getCOSObject())
            .getDictionaryObject(COSName.DESCENDANT_FONTS);

    if (descendantFonts != null)
    {
      COSDictionary descendantFontDictionary = (COSDictionary) descendantFonts.getObject(0);
      if (descendantFontDictionary != null)
      {
        try
        {
          descendentFont = PDFontFactory.createFont(descendantFontDictionary);
        }
        catch (IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    return descendentFont;
  }

  /**
   * This will print the usage requirements and exit.
   */
  private static void usage()
  {
    System.err
        .println("Usage: java -jar pdfbox-app-x.y.z.jar ExtractTextInfo [OPTIONS] <PDF file> [Text File]\n"
            + "  -password  <password>        Password to decrypt document\n"
            + "  -encoding  <output encoding> (ISO-8859-1,UTF-16BE,UTF-16LE,...)\n"
            + "  -console                     Send text to console instead of file\n"
            + "  -html                        Output in HTML format instead of raw text\n"
            + "  -sort                        Sort the text before writing\n"
            + "  -ignoreBeads                 Disables the separation by beads\n"
            + "  -force                       Enables pdfbox to ignore corrupt objects\n"
            + "  -debug                       Enables debug output about the time consumption of every stage\n"
            + "  -startPage <number>          The first page to start extraction(1 based)\n"
            + "  -endPage <number>            The last page to extract(inclusive)\n"
            + "  -nonSeq                      Enables the new non-sequential parser\n"
            + "  -infotext                    Output text information in TXT format\n"
            + "  -jtree                       Display tree of text information\n"
            + "  -infoxml                     Output text information in XML format\n"
            + "  <PDF file>                   The PDF document to use\n"
            + "  [Text File]                  The file to write the text to\n");
    System.exit(1);
  }
}
