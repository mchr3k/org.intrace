package org.gaecounter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Utils
{
  public static String enc(String xiVal)
  {
    try
    {
      return URLEncoder.encode(xiVal, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      // We will never hit this
      return null;
    }
  }

  public static String dec(String xiVal)
  {
    try
    {
      return URLDecoder.decode(xiVal, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      // We will never hit this
      return null;
    }
  }
}
