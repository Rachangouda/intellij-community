package com.intellij.util.containers.hash;


import org.junit.Assert;

import org.junit.Test;


import java.util.Iterator;

import java.util.Map;


public class LinkedHashMapTest {


  @Test

  public void testPutGet() {

    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>();

    for (int i = 0; i < 1000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    Assert.assertEquals(1000, tested.size());

    for (int i = 0; i < 1000; ++i) {

      Assert.assertEquals(Integer.toString(i), tested.get(i));

    }

    for (int i = 0; i < 1000; ++i) {

      Assert.assertEquals(Integer.toString(i), tested.put(i, Integer.toString(i + 1)));

    }

    Assert.assertEquals(1000, tested.size());

    for (int i = 0; i < 1000; ++i) {

      Assert.assertEquals(Integer.toString(i + 1), tested.get(i));

    }

  }


  @Test

  public void testPutGet2() {

    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>();

    for (int i = 0; i < 1000; ++i) {

      tested.put(i - 500, Integer.toString(i));

    }

    Assert.assertEquals(1000, tested.size());

    for (int i = 0; i < 1000; ++i) {

      Assert.assertEquals(Integer.toString(i), tested.get(i - 500));

    }

    for (int i = 0; i < 1000; ++i) {

      Assert.assertEquals(Integer.toString(i), tested.put(i - 500, Integer.toString(i + 1)));

    }

    Assert.assertEquals(1000, tested.size());

    for (int i = 0; i < 1000; ++i) {

      Assert.assertEquals(Integer.toString(i + 1), tested.get(i - 500));

    }

  }


  @Test

  public void testPutGetRemove() {

    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>();

    for (int i = 0; i < 1000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    Assert.assertEquals(1000, tested.size());

    for (int i = 0; i < 1000; i += 2) {

      Assert.assertEquals(Integer.toString(i), tested.remove(i));

    }

    Assert.assertEquals(500, tested.size());

    for (int i = 0; i < 1000; ++i) {

      Assert.assertEquals((i % 2 == 0) ? null : Integer.toString(i), tested.get(i));

    }

  }


  @Test

  public void keySet() {

    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>();

    for (int i = 0; i < 10000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    int i = 10000;

    for (Integer key : tested.keySet()) {

      Assert.assertEquals(--i, key.intValue());

    }

  }


  @Test

  public void keySet2() {

    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>();

    for (int i = 0; i < 10000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    Iterator<Integer> it = tested.keySet().iterator();

    while (it.hasNext()) {

      final int i = it.next();

      if (i % 2 == 0) {

        it.remove();

      }

    }


    Assert.assertEquals(5000, tested.size());


    it = tested.keySet().iterator();

    for (int i = 9999; i > 0; i -= 2) {

      Assert.assertTrue(it.hasNext());

      Assert.assertEquals(i, it.next().intValue());

    }

  }


  @Test

  public void lru() {

    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>() {

      protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {

        return size() > 500;

      }

    };

    for (int i = 0; i < 1000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    Assert.assertEquals(500, tested.size());

    for (int i = 0; i < 500; ++i) {

      Assert.assertNull(tested.remove(i));

    }

    Assert.assertEquals(500, tested.size());

    for (int i = 500; i < 1000; ++i) {

      Assert.assertEquals(Integer.toString(i), tested.remove(i));

    }

    Assert.assertEquals(0, tested.size());

  }


  @Test

  public void lru2() {

    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>() {

      protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {

        return size() > 1000;

      }

    };

    for (int i = 0; i < 1000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    Assert.assertEquals(Integer.toString(0), tested.get(0));

    for (int i = 1000; i < 1999; ++i) {

      tested.put(i, Integer.toString(i));

    }

    Assert.assertEquals(Integer.toString(0), tested.get(0));

    tested.put(2000, Integer.toString(2000));

    Assert.assertNull(tested.get(1000));

  }


  @Test

  public void lru3() {

    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>() {

      protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {

        return size() > 1000;

      }

    };

    for (int i = 0; i < 1000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    Assert.assertEquals(Integer.toString(999), tested.remove(999));

    Assert.assertEquals(999, tested.size());

    Assert.assertEquals(Integer.toString(0), tested.get(0));

    for (int i = 1000; i < 1999; ++i) {

      tested.put(i, Integer.toString(i));

    }

    Assert.assertEquals(Integer.toString(0), tested.get(0));

    tested.put(2000, Integer.toString(2000));

    Assert.assertNull(tested.get(1000));

  }


  //@Test

  public void benchmarkGet() {


    long started;


    final Map<Integer, String> map = new java.util.LinkedHashMap<Integer, String>();

    for (int i = 0; i < 100000; ++i) {

      map.put(i, Integer.toString(i));

    }

    started = System.currentTimeMillis();

    for (int i = 0; i < 1000; ++i) {

      for (int j = 0; j < 100000; ++j) {

        map.get(j);

      }

    }

    System.out.println("100 000 000 lookups in java.util.LinkedHashMap took " + (System.currentTimeMillis() - started));


    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>();

    for (int i = 0; i < 100000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    started = System.currentTimeMillis();

    for (int i = 0; i < 1000; ++i) {

      for (int j = 0; j < 100000; ++j) {

        tested.get(j);

      }

    }

    System.out.println("100 000 000 lookups in LinkedHashMap took " + (System.currentTimeMillis() - started));

  }


  //@Test

  public void benchmarkGetMissingKeys() {


    long started;


    final Map<Integer, String> map = new java.util.LinkedHashMap<Integer, String>();

    for (int i = 0; i < 100000; ++i) {

      map.put(i, Integer.toString(i));

    }

    started = System.currentTimeMillis();

    for (int i = 0; i < 1000; ++i) {

      for (int j = 0; j < 100000; ++j) {

        map.get(j + 1000000);

      }

    }

    System.out.println("100 000 000 lookups in java.util.LinkedHashMap took " + (System.currentTimeMillis() - started));


    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>();

    for (int i = 0; i < 100000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    started = System.currentTimeMillis();

    for (int i = 0; i < 1000; ++i) {

      for (int j = 0; j < 100000; ++j) {

        tested.get(j + 1000000);

      }

    }

    System.out.println("100 000 000 lookups in LinkedHashMap took " + (System.currentTimeMillis() - started));

  }


  //@Test

  public void benchmarkLRU() {


    long started;


    final Map<Integer, String> map = new java.util.LinkedHashMap<Integer, String>();

    for (int i = 0; i < 100000; ++i) {

      map.put(i, Integer.toString(i));

    }

    started = System.currentTimeMillis();

    for (int i = 0; i < 200; ++i) {

      for (int j = 0; j < 100000; ++j) {

        final String v = map.remove(j);

        map.put(j, v);

      }

    }

    System.out.println("20 000 000 LRU lookups in java.util.LinkedHashMap took " + (System.currentTimeMillis() - started));


    final LinkedHashMap<Integer, String> tested = new LinkedHashMap<Integer, String>();

    for (int i = 0; i < 100000; ++i) {

      tested.put(i, Integer.toString(i));

    }

    started = System.currentTimeMillis();

    for (int i = 0; i < 200; ++i) {

      for (int j = 0; j < 100000; ++j) {

        tested.get(j);

      }

    }

    System.out.println("20 000 000 lookups in LinkedHashMap took " + (System.currentTimeMillis() - started));

  }

}

