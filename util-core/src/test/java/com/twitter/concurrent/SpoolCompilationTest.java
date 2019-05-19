package com.twitter.concurrent;

import com.twitter.util.Await;
import com.twitter.util.Future;
import org.junit.Assert;
import org.junit.Test;
import scala.collection.JavaConverters;

import java.util.Arrays;
import java.util.Collection;

public class SpoolCompilationTest {
//  private static class OwnSpool extends AbstractSpool<String> {
//    @Override
//    public boolean isEmpty() {
//      return false;
//    }
//
//    @Override
//    public Future<Spool<String>> tail() {
//      return Future.value(Spools.<String>newEmptySpool());
//    }
//
//    @Override
//    public String head() {
//      return "spool";
//    }
//  }

//  @Test
//  public void testOwnSpool() {
//    Spool<String> a = new OwnSpool();
//    Assert.assertFalse(a.isEmpty());
//    Assert.assertEquals("spool", a.head());
//  }

//  Error:(13, 18) java: name clash: <B>$plus$plus(scala.Function0<com.twitter.util.Future<com.twitter.concurrent.Spool<B>>>) in com.twitter.concurrent.Spool and <B>$plus$plus(scala.Function0<com.twitter.concurrent.Spool<B>>) in com.twitter.concurrent.Spool have the same erasure, yet neither overrides the other

  // We have two ++ methods defined in Spool, i don't see why it becomes a problem now

  @Test
  public void testSpoolCreation() {
    Spool<String> a = Spools.newEmptySpool();
    Spool<?> b = Spools.EMPTY;
    Spool<String> c = Spools.newSpool(Arrays.asList("a", "b"));

    Assert.assertNotNull(a);
    Assert.assertNotNull(b);
    Assert.assertNotNull(c);
  }

  @Test
  public void testSpoolConcat() throws Exception {
    Spool<String> a = Spools.newSpool(Arrays.asList("a"));
    Spool<String> b = Spools.newSpool(Arrays.asList("b"));
    Spool<String> cd = Spools.newSpool(Arrays.asList("c", "d"));

    Spool<String> ab = a.concat(b);
    Spool<String> abNothing = ab.concat(Spools.<String>newEmptySpool());
    Spool<String> abcd = Await.result(ab.concat(Future.value(cd)));

    Collection<String> listA = JavaConverters.seqAsJavaListConverter(Await.result(ab.toSeq())).asJava();
    Collection<String> listB = JavaConverters.seqAsJavaListConverter(Await.result(abNothing.toSeq())).asJava();
    Collection<String> listC = JavaConverters.seqAsJavaListConverter(Await.result(abcd.toSeq())).asJava();

    Assert.assertArrayEquals(new String[] { "a", "b"}, listA.toArray());
    Assert.assertArrayEquals(new String[] { "a", "b"}, listB.toArray());
    Assert.assertArrayEquals(new String[] { "a", "b", "c" , "d"}, listC.toArray());
  }
}
