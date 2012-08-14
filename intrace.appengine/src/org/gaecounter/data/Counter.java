package org.gaecounter.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Days;
import org.joda.time.Partial;
import org.joda.time.Period;

@PersistenceCapable
public class Counter
{
  public Counter(Type mType, String mDate, String mFile)
  {
    this.mKey = getKey(mFile, mDate);
    this.mType = mType;
    this.mDate = mDate;
    this.mFile = mFile;
    this.mCount = 1;
  }

  public static String getKey(String xiFile, String xiDate)
  {
    return xiFile + "_" + xiDate;
  }

  @PrimaryKey
  @Persistent
  public String mKey;
  public enum Type
  {
    YEAR("yyyy", Period.years(1))
    {
      @Override
      public Partial getPartial(int y, int m, int d)
      {
        return new Partial(new DateTimeFieldType[]
                          {DateTimeFieldType.year()},
                          new int[] {y});
      }
    },
    MONTH("yyyy/MM", Period.months(1))
    {
      @Override
      public Partial getPartial(int y, int m, int d)
      {
        return new Partial(new DateTimeFieldType[]
                          {DateTimeFieldType.year(),
                           DateTimeFieldType.monthOfYear()},
                          new int[] {y, m});
      }
    },
    WEEK("yyyy/MM/dd", Period.days(7))
    {
      @Override
      public Partial getPartial(int y, int m, int d)
      {
        return new Partial(new DateTimeFieldType[]
                          {DateTimeFieldType.year(),
                           DateTimeFieldType.monthOfYear(),
                           DateTimeFieldType.dayOfMonth()},
                          new int[] {y, m, d});
      }
    },
    DAY("yyyy/MM/dd", Period.days(1))
    {
      @Override
      public Partial getPartial(int y, int m, int d)
      {
        return new Partial(new DateTimeFieldType[]
                          {DateTimeFieldType.year(),
                           DateTimeFieldType.monthOfYear(),
                           DateTimeFieldType.dayOfMonth()},
                          new int[] {y, m, d});
      }
    };
    private final String mFormat;
    public final Period mPeriod;
    private Type (String xiF, Period xiP) {mFormat = xiF; mPeriod = xiP;}
    public Partial getPartial(int y, int m, int d) {return null;}
    public String getPartialStr(int y, int m, int d)
    {
      return getPartial(y, m, d).toString(mFormat);
    }
  }
  @Persistent
  public Type mType;
  @Persistent
  public String mDate;
  @Persistent
  public String mFile;
  @Persistent
  public Integer mCount;

  public void incrementCount()
  {
    mCount++;
  }

  @SuppressWarnings("unchecked")
  public static List<Counter> getAllByType(Type xiType)
  {
    List<Counter> detachedList = null, list = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try
    {
      Query lQuery = pm.newQuery(Counter.class);;
      lQuery.setFilter("mType == mTypeParam");
      lQuery.declareParameters("String mTypeParam");
      list = (List<Counter>)lQuery.execute(xiType);
      detachedList = new ArrayList<Counter>();
      for (Counter obj : list)
      {
        detachedList.add(pm.detachCopy(obj));
      }

    }
    finally
    {
      pm.close();
    }
    return detachedList;
  }

  private static final Pattern sVerPattern = Pattern.compile("(.+)_([0-9]+(\\.[0-9]+)*)(\\..+)");

  public static Map<String,Map<String,Integer>> getPerFilePerDateMap(List<Counter> xiList,
                                                                     boolean xiCollapseVers)
  {
    Map<String,Map<String,Integer>> lFileMap = new HashMap<String, Map<String,Integer>>();

    for (Counter lCounter : xiList)
    {
      String lFile = lCounter.mFile;
      String lDateStr = lCounter.mDate;
      Integer lCount = lCounter.mCount;

      if (xiCollapseVers)
      {
        Matcher matcher = sVerPattern.matcher(lFile);
        if (matcher.matches())
        {
          lFile = matcher.group(1) + "*" + matcher.group(4);
        }
      }

      Map<String, Integer> lDateMap = lFileMap.get(lFile);
      if (lDateMap == null)
      {
        lDateMap = new HashMap<String, Integer>();
        lFileMap.put(lFile, lDateMap);
      }

      if (xiCollapseVers)
      {
        Integer lAllVersCount = lDateMap.get(lDateStr);
        if (lAllVersCount == null)
        {
          lAllVersCount = 0;
        }
        lAllVersCount += lCount;
        lDateMap.put(lDateStr, lAllVersCount);
      }
      else
      {
        lDateMap.put(lDateStr, lCount);
      }
    }

    return lFileMap;
  }

  public static List<String> getDateStrs(Type xiType,
                                         Partial xiStart,
                                         Set<String> xiDataDateStrs)
  {
    List<String> lDateStrs = new ArrayList<String>();

    if (xiDataDateStrs.size() > 0)
    {
      if (xiType == Type.WEEK)
      {
        DateMidnight foundMonday = new DateMidnight(xiStart.get(DateTimeFieldType.year()),
                                                    xiStart.get(DateTimeFieldType.monthOfYear()),
                                                    xiStart.get(DateTimeFieldType.dayOfMonth()));
        while (foundMonday.getDayOfWeek() != DateTimeConstants.MONDAY)
        {
          foundMonday = foundMonday.plus(Days.ONE);
        }
        xiStart = new Partial(new DateTimeFieldType[]
                              {DateTimeFieldType.year(),
                              DateTimeFieldType.monthOfYear(),
                              DateTimeFieldType.dayOfMonth()},
                              new int[] {foundMonday.getYear(),
                                         foundMonday.getMonthOfYear(),
                                         foundMonday.getDayOfMonth()});
      }

      List<String> lInDataStrsList = new ArrayList<String>(xiDataDateStrs);
      Collections.sort(lInDataStrsList);
      String lOldestDate = lInDataStrsList.get(0);

      Partial lDate = xiStart;
      String lDateStr = lDate.toString(xiType.mFormat);

      while (lDateStr.compareTo(lOldestDate) >= 0)
      {
        lDateStrs.add(lDateStr);
        lDate = lDate.minus(xiType.mPeriod);
        lDateStr = lDate.toString(xiType.mFormat);
      }
      lDateStrs.add(lDateStr);

      Collections.reverse(lDateStrs);
    }
    return lDateStrs;
  }

  public static Map<String,Map<String,Integer>> getFilePerWeekMap(Map<String,Map<String,Integer>> xiPerFilePerDayData)
  {
    // Filename -> DateStr -> Count
    Map<String,Map<String,Integer>> lRet = new HashMap<String, Map<String,Integer>>();

   for (Entry<String,Map<String,Integer>> fileEntry : xiPerFilePerDayData.entrySet())
   {
     String file = fileEntry.getKey();
     Map<String,Integer> perWeekData = new HashMap<String, Integer>();
     lRet.put(file, perWeekData);

     // DateStr -> Count
     Map<String, Integer> perDateData = fileEntry.getValue();
     List<String> dateStrs = new ArrayList<String>(perDateData.keySet());
     Collections.sort(dateStrs);
     Collections.reverse(dateStrs);

     DateMidnight lastDate = null;
     int weekCount = 0;
     boolean unrecordedItem = false;

     for (String datestr : dateStrs)
     {
       DateMidnight date = new DateMidnight(datestr.replace("/", "-"));
       if (unrecordedItem && (lastDate != null))
       {
         Days d = Days.daysBetween(date, lastDate);
         if (d.getDays() >= 7)
         {
           DateMidnight foundMonday = lastDate;
           while (foundMonday.getDayOfWeek() != DateTimeConstants.MONDAY)
           {
             foundMonday = foundMonday.minus(Days.ONE);
           }
           perWeekData.put(Type.DAY.getPartialStr(foundMonday.getYear(),
                                                  foundMonday.getMonthOfYear(),
                                                  foundMonday.getDayOfMonth()),
                           weekCount);
           weekCount = 0;
           unrecordedItem = false;
         }
       }

       lastDate = date;
       Integer count = perDateData.get(datestr);
       weekCount += count;
       unrecordedItem = true;

       if (date.getDayOfWeek() == DateTimeConstants.MONDAY)
       {
         perWeekData.put(datestr, weekCount);
         weekCount = 0;
         unrecordedItem = false;
       }
     }

     if (unrecordedItem)
     {
       DateMidnight foundMonday = lastDate;
       while (foundMonday.getDayOfWeek() != DateTimeConstants.MONDAY)
       {
         foundMonday = foundMonday.minus(Days.ONE);
       }
       perWeekData.put(Type.DAY.getPartialStr(foundMonday.getYear(),
                                              foundMonday.getMonthOfYear(),
                                              foundMonday.getDayOfMonth()),
                       weekCount);
     }
   }

    return lRet;
  }

}
