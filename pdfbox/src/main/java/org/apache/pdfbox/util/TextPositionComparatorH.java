package org.apache.pdfbox.util;

import java.util.Comparator;

/**
 * @author <a href="mailto:su.eugene@gmail.com">Eugene Su</a>
 */
public class TextPositionComparatorH implements Comparator
{
  /**
   * {@inheritDoc}
   */
  public int compare(Object obj1, Object obj2)
  {
    int retval = 0;

    TextPosition pos1 = (TextPosition) obj1;
    TextPosition pos2 = (TextPosition) obj2;

    float yRot1 = pos1.getY();
    float yRot2 = pos2.getY();

    float pos1YBottom = yRot1;
    float pos2YBottom = yRot2;

    // note that the coordinates have been adjusted so 0,0 is in upper left
    float pos1YTop = pos1YBottom - pos1.getHeightDir();
    float pos2YTop = pos2YBottom - pos2.getHeightDir();

    float yDifference = Math.abs(pos1YBottom - pos2YBottom);
    // we will do a simple tolerance comparison.
    if (yDifference < .1 || (pos2YBottom >= pos1YTop && pos2YBottom <= pos1YBottom)
        || (pos1YBottom >= pos2YTop && pos1YBottom <= pos2YBottom))
    {
      retval = 0;
    }
    else
    {
      if (yRot1 < yRot2)
      {
        retval = -1;
      }
      else if (yRot1 > yRot2)
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