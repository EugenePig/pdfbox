package org.apache.pdfbox.util;

import java.util.Comparator;
import java.util.List;

import org.apache.fontbox.cmap.CMap;

/**
 * @author <a href="mailto:su.eugene@gmail.com">Eugene Su</a>
 */
public class CharactersByArticleComparatorV implements Comparator
{
  /**
   * {@inheritDoc}
   */
  public int compare(Object obj1, Object obj2)
  {
    int retval = 0;
    
    TextPosition pos1 = ((List<TextPosition>)obj1).get(0);
    TextPosition pos2 = ((List<TextPosition>)obj2).get(0);
    
    float xRot1 = pos1.getX();
    float xRot2 = pos2.getX();
   
    int wmode1 = 0;
    int wmode2 = 0;
    CMap cmap1 = pos1.getFont().getCMap();
    CMap cmap2 = pos2.getFont().getCMap();
    
    if (cmap1 != null && cmap1.getWMode() == 1)
    {
      wmode1 = 1;
    }
    
    if (cmap2 != null && cmap2.getWMode() == 1)
    {
      wmode2 = 1;
    }
    
    if(wmode1 == 1)
    {
      xRot1 = xRot1 - pos1.getHeightDir()/2;
    }
    
    if(wmode2 == 1)
    {
      xRot2 = xRot2 - pos2.getHeightDir()/2;
    }
        
    float pos1XLeft = xRot1;
    float pos2XLeft = xRot2;
    
    // note that the coordinates have been adjusted so 0,0 is in upper left
    float pos1XRight = pos1XLeft + pos1.getHeightDir();
    float pos2XRight = pos2XLeft + pos2.getHeightDir();

    float yDifference = Math.abs( pos1XLeft-pos2XLeft);
    //we will do a simple tolerance comparison.
    if( yDifference < .1 ||
        (pos2XLeft <= pos1XRight && pos2XLeft >= pos1XLeft) ||
        (pos1XLeft <= pos2XRight && pos1XLeft >= pos2XLeft))
    {
      retval = 0;
    }
    else
    {
      if(xRot1 > xRot2)
      {
        retval = -1;
      }  
      else if(xRot1 < xRot2)
      {
        retval = 1;
      }  
      else
      {
        retval = 0;
      }
    }
    
    return retval;
  }
}