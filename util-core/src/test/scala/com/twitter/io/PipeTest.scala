package com.twitter.io

import com.twitter.conversions.DurationOps._
import com.twitter.util.{Await, Future, MockTimer, Return, Time}
import org.scalatest.{FunSuite, Matchers}

//class PipeTest extends FunSuite with Matchers {
//
//  private def await[A](f: Future[A]): A = Await.result(f, 5.seconds)
//
//  private def arr(i: Int, j: Int) = Array.range(i, j).map(_.toByte)
//  private def buf(i: Int, j: Int) = Buf.ByteArray.Owned(arr(i, j))
//
//  private def assertRead(r: Reader[Buf], i: Int, j: Int): Unit = {
//    val n = j - i
//    val f = r.read()
//    assertRead(f, i, j)
//  }
//
//  private def assertRead(f: Future[Option[Buf]], i: Int, j: Int): Unit = {
//    assert(f.isDefined)
//    val b = await(f)
//    assert(toSeq(b) == Seq.range(i, j))
//  }
//
//  private def toSeq(b: Option[Buf]): Seq[Byte] = b match {
//    case None => fail("Expected full buffer")
//    case Some(buf) =>
//      val a = new Array[Byte](buf.length)
//      buf.write(a, 0)
//      a.toSeq
//  }
//
//  private def assertWrite(w: Writer[Buf], i: Int, j: Int): Unit = {
//    val buf = Buf.ByteArray.Owned(Array.range(i, j).map(_.toByte))
//    val f = w.write(buf)
//    assert(f.isDefined)
//    assert(await(f.liftToTry) == Return.Unit)
//  }
//
//  private def assertWriteEmpty(w: Writer[Buf]): Unit = {
//    val f = w.write(Buf.Empty)
//    assert(f.isDefined)
//    assert(await(f.liftToTry) == Return.Unit)
//  }
//
//  private def assertReadEofAndClosed(rw: Pipe[Buf]): Unit = {
//    assertReadNone(rw)
//    assert(rw.close().isDone)
//  }
//
//  private def assertReadNone(r: Reader[Buf]): Unit =
//    assert(await(r.read()).isEmpty)
//
//  private val failedEx = new RuntimeException("ʕ •ᴥ•ʔ")
//
//  private def assertFailedEx(f: Future[_]): Unit = {
//    val thrown = intercept[RuntimeException] {
//      await(f)
//    }
//    assert(thrown == failedEx)
//  }
//
//  test("Pipe") {
//    val rw = new Pipe[Buf]
//    val wf = rw.write(buf(0, 6))
//    assert(!wf.isDefined)
//    assertRead(rw, 0, 6)
//    assert(wf.isDefined)
//    assert(await(wf.liftToTry) == Return(()))
//  }
//
//  test("Reader.readAll") {
//    val rw = new Pipe[Buf]
//    val all = Reader.readAll(rw)
//    assert(!all.isDefined)
//    assertWrite(rw, 0, 3)
//    assertWrite(rw, 3, 6)
//    assert(!all.isDefined)
//    assertWriteEmpty(rw)
//    assert(!all.isDefined)
//    await(rw.close())
//    assert(all.isDefined)
//    val buf = await(all)
//    assert(toSeq(Some(buf)) == Seq.range(0, 6))
//  }
//
//  test("write before read") {
//    val rw = new Pipe[Buf]
//    val wf = rw.write(buf(0, 6))
//    assert(!wf.isDefined)
//    val rf = rw.read()
//    assert(rf.isDefined)
//    assert(toSeq(await(rf)) == Seq.range(0, 6))
//  }
//
//  test("fail while reading") {
//    val rw = new Pipe[Buf]
//    var closed = false
//    rw.onClose.ensure { closed = true }
//    val rf = rw.read()
//    assert(!rf.isDefined)
//    assert(!closed)
//    val exc = new Exception
//    rw.fail(exc)
//    assert(closed)
//    assert(rf.isDefined)
//    val exc1 = intercept[Exception] { await(rf) }
//    assert(exc eq exc1)
//  }
//
//  test("fail before reading") {
//    val rw = new Pipe[Buf]
//    rw.fail(new Exception)
//    val rf = rw.read()
//    assert(rf.isDefined)
//    intercept[Exception] { await(rf) }
//  }
//
//  test("discard") {
//    val rw = new Pipe[Buf]
//    var closed = false
//    rw.onClose.ensure { closed = true }
//    rw.discard()
//    val rf = rw.read()
//    assert(rf.isDefined)
//    assert(closed)
//    intercept[ReaderDiscardedException] { await(rf) }
//  }
//
//  test("close") {
//    val rw = new Pipe[Buf]
//    var closed = false
//    rw.onClose.ensure { closed = true }
//    val wf = rw.write(buf(0, 6)) before rw.close()
//    assert(!wf.isDefined)
//    assert(!closed)
//    assert(await(rw.read()).contains(buf(0, 6)))
//    assert(!wf.isDefined)
//    assertReadEofAndClosed(rw)
//    assert(closed)
//  }
//
//  test("write then reads then close") {
//    val rw = new Pipe[Buf]
//    val wf = rw.write(buf(0, 6))
//
//    assert(!wf.isDone)
//    assertRead(rw, 0, 6)
//    assert(wf.isDone)
//
//    assert(true)
//    assertReadEofAndClosed(rw)
//  }
//
//  test("read then write then close") {
//    val rw = new Pipe[Buf]
//
//    val rf = rw.read()
//    assert(!rf.isDefined)
//
//    val wf = rw.write(buf(0, 6))
//    assert(wf.isDone)
//    assertRead(rf, 0, 6)
//
//    assert(true)
//    assertReadEofAndClosed(rw)
//  }
//
//  test("write after fail") {
//    val rw = new Pipe[Buf]
//    rw.fail(failedEx)
//
//    assertFailedEx(rw.write(buf(0, 6)))
//    val cf = rw.close()
//    assert(!cf.isDone)
//
//    assertFailedEx(rw.read())
//    assertFailedEx(cf)
//  }
//
//  test("write after close") {
//    val rw = new Pipe[Buf]
//    val cf = rw.close()
//    assert(!cf.isDone)
//    assertReadEofAndClosed(rw)
//    assert(cf.isDone)
//
//    intercept[IllegalStateException] {
//      await(rw.write(buf(0, 1)))
//    }
//  }
//
//  test("write while write pending") {
//    val rw = new Pipe[Buf]
//    var closed = false
//    rw.onClose.ensure { closed = true }
//    val wf = rw.write(buf(0, 1))
//    assert(!wf.isDone)
//
//    intercept[IllegalStateException] {
//      await(rw.write(buf(0, 1)))
//    }
//
//    // the extraneous write should not mess with the 1st one.
//    assertRead(rw, 0, 1)
//    assert(!closed)
//  }
//
//  test("read after fail") {
//    val rw = new Pipe[Buf]
//    rw.fail(failedEx)
//    assertFailedEx(rw.read())
//  }
//
//  test("read after close with no pending reads") {
//    val rw = new Pipe[Buf]
//    assert(true)
//    assertReadEofAndClosed(rw)
//  }
//
//  test("read after close with pending data") {
//    val rw = new Pipe[Buf]
//
//    val wf = rw.write(buf(0, 1))
//    assert(!wf.isDone)
//
//    // close before the write is satisfied wipes the pending write
//    assert(true)
//    intercept[IllegalStateException] {
//      await(wf)
//    }
//    assertReadNone(rw)
//    intercept[IllegalStateException] {
//      await(rw.onClose)
//    }
//  }
//
//  test("read while reading") {
//    val rw = new Pipe[Buf]
//    var closed = false
//    rw.onClose.ensure { closed = true }
//    val rf = rw.read()
//    intercept[IllegalStateException] {
//      await(rw.read())
//    }
//    assert(!rf.isDefined)
//    assert(!closed)
//  }
//
//  test("discard with pending read") {
//    val rw = new Pipe[Buf]
//
//    val rf = rw.read()
//    rw.discard()
//
//    intercept[ReaderDiscardedException] {
//      await(rf)
//    }
//  }
//
//  test("discard with pending write") {
//    val rw = new Pipe[Buf]
//
//    val wf = rw.write(buf(0, 1))
//    rw.discard()
//
//    intercept[ReaderDiscardedException] {
//      await(wf)
//    }
//  }
//
//  test("close not satisfied until writes are read") {
//    val rw = new Pipe[Buf]
//    val cf = rw.write(buf(0, 6)).before(rw.close())
//    assert(!cf.isDone)
//
//    assertRead(rw, 0, 6)
//    assert(!cf.isDone)
//    assertReadEofAndClosed(rw)
//  }
//
//  test("close not satisfied until reads are fulfilled") {
//    val rw = new Pipe[Buf]
//    val rf = rw.read()
//    val cf = rf.flatMap { _ =>
//      rw.close()
//    }
//    assert(!rf.isDefined)
//    assert(!cf.isDone)
//
//    assert(rw.write(buf(0, 3)).isDone)
//
//    assertRead(rf, 0, 3)
//    assert(!cf.isDone)
//    assertReadEofAndClosed(rw)
//  }
//
//  test("close while read pending") {
//    val rw = new Pipe[Buf]
//    val rf = rw.read()
//    assert(!rf.isDefined)
//
//    assert(rw.close().isDone)
//    assert(rf.isDefined)
//  }
//
//  test("close then close") {
//    val rw = new Pipe[Buf]
//    assert(true)
//    assertReadEofAndClosed(rw)
//    assert(rw.close().isDone)
//    assertReadEofAndClosed(rw)
//  }
//
//  test("close after fail") {
//    val rw = new Pipe[Buf]
//    rw.fail(failedEx)
//    val cf = rw.close()
//    assert(!cf.isDone)
//
//    assertFailedEx(rw.read())
//    assertFailedEx(cf)
//  }
//
//  test("close before fail") {
//    val timer = new MockTimer()
//    Time.withCurrentTimeFrozen { ctrl =>
//      val rw = new Pipe[Buf](timer)
//      val cf = rw.close(1.second)
//      assert(!cf.isDone)
//
//      ctrl.advance(1.second)
//      timer.tick()
//
//      rw.fail(failedEx)
//
//      assertFailedEx(rw.read())
//    }
//  }
//
//  test("close before fail within deadline") {
//    val timer = new MockTimer()
//    Time.withCurrentTimeFrozen { _ =>
//      val rw = new Pipe[Buf](timer)
//      val cf = rw.close(1.second)
//      assert(!cf.isDone)
//
//      rw.fail(failedEx)
//      assert(!cf.isDone)
//
//      assertFailedEx(rw.read())
//      assertFailedEx(cf)
//    }
//  }
//
//  test("close while write pending") {
//    val rw = new Pipe[Buf]
//    val wf = rw.write(buf(0, 1))
//    assert(!wf.isDone)
//    val cf = rw.close()
//    assert(!cf.isDone)
//    intercept[IllegalStateException] {
//      await(wf)
//    }
//    assertReadNone(rw)
//    intercept[IllegalStateException] {
//      await(rw.onClose)
//    }
//  }
//
//  test("close respects deadline") {
//    val mockTimer = new MockTimer()
//    Time.withCurrentTimeFrozen { timeCtrl =>
//      val rw = new Pipe[Buf](mockTimer)
//      val wf = rw.write(buf(0, 6))
//
//      rw.close(1.second)
//
//      assert(!wf.isDefined)
//      assert(!rw.onClose.isDefined)
//
//      timeCtrl.advance(1.second)
//      mockTimer.tick()
//
//      intercept[IllegalStateException] {
//        await(wf)
//      }
//
//      intercept[IllegalStateException] {
//        await(rw.onClose)
//      }
//      assertReadNone(rw)
//    }
//  }
//
//  test("read complete data before close deadline") {
//    val mockTimer = new MockTimer()
//    val rw = new Pipe[Buf](mockTimer)
//    Time.withCurrentTimeFrozen { timeCtrl =>
//      val wf = rw.write(buf(0, 6)) before rw.close(1.second)
//
//      assert(!wf.isDefined)
//      assertRead(rw, 0, 6)
//      assertReadNone(rw)
//      assert(wf.isDefined)
//
//      timeCtrl.advance(1.second)
//      mockTimer.tick()
//
//      assertReadEofAndClosed(rw)
//    }
//  }
//
//  test("multiple reads read complete data before close deadline") {
//    val mockTimer = new MockTimer()
//    val buf = Buf.Utf8("foo")
//    Time.withCurrentTimeFrozen { timeCtrl =>
//      val rw = new Pipe[Buf](mockTimer)
//      val writef = rw.write(buf)
//
//      rw.close(1.second)
//
//      assert(!writef.isDefined)
//      assert(await(Reader.readAll(rw)) == buf)
//      assertReadNone(rw)
//      assert(writef.isDefined)
//
//      timeCtrl.advance(1.second)
//      mockTimer.tick()
//
//      assertReadEofAndClosed(rw)
//    }
//  }
//}


//[error] ## Exception when compiling 101 sources to /Users/yufang/workspace/oss/util/util-core/target/scala-2.13.0-RC1/test-classes
//[error] Could not find proxy for val rw: com.twitter.io.Pipe in List(value rw, method $anonfun$new$15, value <local PipeTest>, class PipeTest, package io, package twitter, package com, package <root>) (currentOwner= value stabilizer$2 )
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:318)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.proxy(LambdaLift.scala:332)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.proxyRef(LambdaLift.scala:372)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.postTransform(LambdaLift.scala:527)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] scala.reflect.internal.Trees$Select.transform(Trees.scala:821)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] scala.reflect.internal.Trees$Apply.transform(Trees.scala:755)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] scala.reflect.internal.Trees$ValDef.$anonfun$transform$4(Trees.scala:405)
//[error] scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:32)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:24)
//[error] scala.reflect.internal.Trees$ValDef.transform(Trees.scala:404)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] scala.reflect.api.Trees$Transformer.$anonfun$transformStats$1(Trees.scala:2614)
//[error] scala.reflect.api.Trees$Transformer.transformStats(Trees.scala:2612)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:575)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:59)
//[error] scala.reflect.internal.Trees$Block.transform(Trees.scala:525)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] scala.reflect.api.Trees$Transformer.$anonfun$transformStats$2(Trees.scala:2613)
//[error] scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:32)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:24)
//[error] scala.reflect.api.Trees$Transformer.$anonfun$transformStats$1(Trees.scala:2613)
//[error] scala.reflect.api.Trees$Transformer.transformStats(Trees.scala:2612)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:575)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:59)
//[error] scala.reflect.internal.Trees$Template.transform(Trees.scala:513)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.$anonfun$transform$1(TypingTransformers.scala:47)
//[error] scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:32)
//[error] scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] scala.reflect.api.Trees$Transformer.transformTemplate(Trees.scala:2587)
//[error] scala.reflect.internal.Trees$ClassDef.$anonfun$transform$2(Trees.scala:333)
//[error] scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:32)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:24)
//[error] scala.reflect.internal.Trees$ClassDef.transform(Trees.scala:332)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] scala.reflect.api.Trees$Transformer.$anonfun$transformStats$1(Trees.scala:2614)
//[error] scala.reflect.api.Trees$Transformer.transformStats(Trees.scala:2612)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:575)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:59)
//[error] scala.reflect.internal.Trees$PackageDef.$anonfun$transform$1(Trees.scala:314)
//[error] scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:32)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:24)
//[error] scala.reflect.internal.Trees$PackageDef.transform(Trees.scala:314)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.$anonfun$transform$2(TypingTransformers.scala:49)
//[error] scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:32)
//[error] scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] scala.tools.nsc.ast.Trees$Transformer.transformUnit(Trees.scala:162)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.super$transformUnit(LambdaLift.scala:581)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$transformUnit$1(LambdaLift.scala:581)
//[error] scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformUnit(LambdaLift.scala:581)
//[error] scala.tools.nsc.transform.Transform$Phase.apply(Transform.scala:37)
//[error] scala.tools.nsc.Global$GlobalPhase.applyPhase(Global.scala:451)
//[error] scala.tools.nsc.Global$GlobalPhase.run(Global.scala:396)
//[error] scala.tools.nsc.Global$Run.compileUnitsInternal(Global.scala:1510)
//[error] scala.tools.nsc.Global$Run.compileUnits(Global.scala:1494)
//[error] scala.tools.nsc.Global$Run.compileSources(Global.scala:1486)
//[error] scala.tools.nsc.Global$Run.compile(Global.scala:1615)
//[error] xsbt.CachedCompiler0.run(CompilerInterface.scala:130)
//[error] xsbt.CachedCompiler0.run(CompilerInterface.scala:105)
//[error] xsbt.CompilerInterface.run(CompilerInterface.scala:31)
//[error] sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//[error] sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
//[error] sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
//[error] java.lang.reflect.Method.invoke(Method.java:498)
//[error] sbt.internal.inc.AnalyzingCompiler.call(AnalyzingCompiler.scala:237)
//[error] sbt.internal.inc.AnalyzingCompiler.compile(AnalyzingCompiler.scala:111)
//[error] sbt.internal.inc.AnalyzingCompiler.compile(AnalyzingCompiler.scala:90)
//[error] sbt.internal.inc.MixedAnalyzingCompiler.$anonfun$compile$3(MixedAnalyzingCompiler.scala:83)
//[error] scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:12)
//[error] sbt.internal.inc.MixedAnalyzingCompiler.timed(MixedAnalyzingCompiler.scala:134)
//[error] sbt.internal.inc.MixedAnalyzingCompiler.compileScala$1(MixedAnalyzingCompiler.scala:74)
//[error] sbt.internal.inc.MixedAnalyzingCompiler.compile(MixedAnalyzingCompiler.scala:117)
//[error] sbt.internal.inc.IncrementalCompilerImpl.$anonfun$compileInternal$1(IncrementalCompilerImpl.scala:305)
//[error] sbt.internal.inc.IncrementalCompilerImpl.$anonfun$compileInternal$1$adapted(IncrementalCompilerImpl.scala:305)
//[error] sbt.internal.inc.Incremental$.doCompile(Incremental.scala:101)
//[error] sbt.internal.inc.Incremental$.$anonfun$compile$4(Incremental.scala:82)
//[error] sbt.internal.inc.IncrementalCommon.recompileClasses(IncrementalCommon.scala:110)
//[error] sbt.internal.inc.IncrementalCommon.cycle(IncrementalCommon.scala:57)
//[error] sbt.internal.inc.Incremental$.$anonfun$compile$3(Incremental.scala:84)
//[error] sbt.internal.inc.Incremental$.manageClassfiles(Incremental.scala:129)
//[error] sbt.internal.inc.Incremental$.compile(Incremental.scala:75)
//[error] sbt.internal.inc.IncrementalCompile$.apply(Compile.scala:61)
//[error] sbt.internal.inc.IncrementalCompilerImpl.compileInternal(IncrementalCompilerImpl.scala:309)
//[error] sbt.internal.inc.IncrementalCompilerImpl.$anonfun$compileIncrementally$1(IncrementalCompilerImpl.scala:267)
//[error] sbt.internal.inc.IncrementalCompilerImpl.handleCompilationError(IncrementalCompilerImpl.scala:158)
//[error] sbt.internal.inc.IncrementalCompilerImpl.compileIncrementally(IncrementalCompilerImpl.scala:237)
//[error] sbt.internal.inc.IncrementalCompilerImpl.compile(IncrementalCompilerImpl.scala:68)
//[error] sbt.Defaults$.compileIncrementalTaskImpl(Defaults.scala:1430)
//[error] sbt.Defaults$.$anonfun$compileIncrementalTask$1(Defaults.scala:1404)
//[error] scala.Function1.$anonfun$compose$1(Function1.scala:44)
//[error] sbt.internal.util.$tilde$greater.$anonfun$$u2219$1(TypeFunctions.scala:39)
//[error] sbt.std.Transform$$anon$4.work(System.scala:66)
//[error] sbt.Execute.$anonfun$submit$2(Execute.scala:262)
//[error] sbt.internal.util.ErrorHandling$.wideConvert(ErrorHandling.scala:16)
//[error] sbt.Execute.work(Execute.scala:271)
//[error] sbt.Execute.$anonfun$submit$1(Execute.scala:262)
//[error] sbt.ConcurrentRestrictions$$anon$4.$anonfun$submitValid$1(ConcurrentRestrictions.scala:174)
//[error] sbt.CompletionService$$anon$2.call(CompletionService.scala:36)
//[error] java.util.concurrent.FutureTask.run(FutureTask.java:266)
//[error] java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
//[error] java.util.concurrent.FutureTask.run(FutureTask.java:266)
//[error] java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
//[error] java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
//[error] java.lang.Thread.run(Thread.java:745)
//[error]
//[error] java.lang.IllegalArgumentException: Could not find proxy for val rw: com.twitter.io.Pipe in List(value rw, method $anonfun$new$15, value <local PipeTest>, class PipeTest, package io, package twitter, package com, package <root>) (currentOwner= value stabilizer$2 )
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:318)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$proxy$4(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:323)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.proxy(LambdaLift.scala:332)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.proxyRef(LambdaLift.scala:372)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.postTransform(LambdaLift.scala:527)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] 	at scala.reflect.internal.Trees$Select.transform(Trees.scala:821)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] 	at scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] 	at scala.reflect.internal.Trees$Apply.transform(Trees.scala:755)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] 	at scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] 	at scala.reflect.internal.Trees$ValDef.$anonfun$transform$4(Trees.scala:405)
//[error] 	at scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:32)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:24)
//[error] 	at scala.reflect.internal.Trees$ValDef.transform(Trees.scala:404)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] 	at scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] 	at scala.reflect.api.Trees$Transformer.$anonfun$transformStats$1(Trees.scala:2614)
//[error] 	at scala.reflect.api.Trees$Transformer.transformStats(Trees.scala:2612)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:575)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:59)
//[error] 	at scala.reflect.internal.Trees$Block.transform(Trees.scala:525)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] 	at scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] 	at scala.reflect.api.Trees$Transformer.$anonfun$transformStats$2(Trees.scala:2613)
//[error] 	at scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:32)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:24)
//[error] 	at scala.reflect.api.Trees$Transformer.$anonfun$transformStats$1(Trees.scala:2613)
//[error] 	at scala.reflect.api.Trees$Transformer.transformStats(Trees.scala:2612)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:575)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:59)
//[error] 	at scala.reflect.internal.Trees$Template.transform(Trees.scala:513)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.$anonfun$transform$1(TypingTransformers.scala:47)
//[error] 	at scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:32)
//[error] 	at scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] 	at scala.reflect.api.Trees$Transformer.transformTemplate(Trees.scala:2587)
//[error] 	at scala.reflect.internal.Trees$ClassDef.$anonfun$transform$2(Trees.scala:333)
//[error] 	at scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:32)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:24)
//[error] 	at scala.reflect.internal.Trees$ClassDef.transform(Trees.scala:332)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:51)
//[error] 	at scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:59)
//[error] 	at scala.reflect.api.Trees$Transformer.$anonfun$transformStats$1(Trees.scala:2614)
//[error] 	at scala.reflect.api.Trees$Transformer.transformStats(Trees.scala:2612)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:575)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformStats(LambdaLift.scala:59)
//[error] 	at scala.reflect.internal.Trees$PackageDef.$anonfun$transform$1(Trees.scala:314)
//[error] 	at scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:32)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:24)
//[error] 	at scala.reflect.internal.Trees$PackageDef.transform(Trees.scala:314)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.$anonfun$transform$2(TypingTransformers.scala:49)
//[error] 	at scala.reflect.api.Trees$Transformer.atOwner(Trees.scala:2625)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.atOwner(TypingTransformers.scala:37)
//[error] 	at scala.tools.nsc.transform.TypingTransformers$TypingTransformer.transform(TypingTransformers.scala:32)
//[error] 	at scala.tools.nsc.transform.ExplicitOuter$OuterPathTransformer.transform(ExplicitOuter.scala:308)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.preTransform(LambdaLift.scala:549)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transform(LambdaLift.scala:557)
//[error] 	at scala.tools.nsc.ast.Trees$Transformer.transformUnit(Trees.scala:162)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.super$transformUnit(LambdaLift.scala:581)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.$anonfun$transformUnit$1(LambdaLift.scala:581)
//[error] 	at scala.tools.nsc.transform.LambdaLift$LambdaLifter.transformUnit(LambdaLift.scala:581)
//[error] 	at scala.tools.nsc.transform.Transform$Phase.apply(Transform.scala:37)
//[error] 	at scala.tools.nsc.Global$GlobalPhase.applyPhase(Global.scala:451)
//[error] 	at scala.tools.nsc.Global$GlobalPhase.run(Global.scala:396)
//[error] 	at scala.tools.nsc.Global$Run.compileUnitsInternal(Global.scala:1510)
//[error] 	at scala.tools.nsc.Global$Run.compileUnits(Global.scala:1494)
//[error] 	at scala.tools.nsc.Global$Run.compileSources(Global.scala:1486)
//[error] 	at scala.tools.nsc.Global$Run.compile(Global.scala:1615)
//[error] 	at xsbt.CachedCompiler0.run(CompilerInterface.scala:130)
//[error] 	at xsbt.CachedCompiler0.run(CompilerInterface.scala:105)
//[error] 	at xsbt.CompilerInterface.run(CompilerInterface.scala:31)
//[error] 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//[error] 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
//[error] 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
//[error] 	at java.lang.reflect.Method.invoke(Method.java:498)
//[error] 	at sbt.internal.inc.AnalyzingCompiler.call(AnalyzingCompiler.scala:237)
//[error] 	at sbt.internal.inc.AnalyzingCompiler.compile(AnalyzingCompiler.scala:111)
//[error] 	at sbt.internal.inc.AnalyzingCompiler.compile(AnalyzingCompiler.scala:90)
//[error] 	at sbt.internal.inc.MixedAnalyzingCompiler.$anonfun$compile$3(MixedAnalyzingCompiler.scala:83)
//[error] 	at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:12)
//[error] 	at sbt.internal.inc.MixedAnalyzingCompiler.timed(MixedAnalyzingCompiler.scala:134)
//[error] 	at sbt.internal.inc.MixedAnalyzingCompiler.compileScala$1(MixedAnalyzingCompiler.scala:74)
//[error] 	at sbt.internal.inc.MixedAnalyzingCompiler.compile(MixedAnalyzingCompiler.scala:117)
//[error] 	at sbt.internal.inc.IncrementalCompilerImpl.$anonfun$compileInternal$1(IncrementalCompilerImpl.scala:305)
//[error] 	at sbt.internal.inc.IncrementalCompilerImpl.$anonfun$compileInternal$1$adapted(IncrementalCompilerImpl.scala:305)
//[error] 	at sbt.internal.inc.Incremental$.doCompile(Incremental.scala:101)
//[error] 	at sbt.internal.inc.Incremental$.$anonfun$compile$4(Incremental.scala:82)
//[error] 	at sbt.internal.inc.IncrementalCommon.recompileClasses(IncrementalCommon.scala:110)
//[error] 	at sbt.internal.inc.IncrementalCommon.cycle(IncrementalCommon.scala:57)
//[error] 	at sbt.internal.inc.Incremental$.$anonfun$compile$3(Incremental.scala:84)
//[error] 	at sbt.internal.inc.Incremental$.manageClassfiles(Incremental.scala:129)
//[error] 	at sbt.internal.inc.Incremental$.compile(Incremental.scala:75)
//[error] 	at sbt.internal.inc.IncrementalCompile$.apply(Compile.scala:61)
//[error] 	at sbt.internal.inc.IncrementalCompilerImpl.compileInternal(IncrementalCompilerImpl.scala:309)
//[error] 	at sbt.internal.inc.IncrementalCompilerImpl.$anonfun$compileIncrementally$1(IncrementalCompilerImpl.scala:267)
//[error] 	at sbt.internal.inc.IncrementalCompilerImpl.handleCompilationError(IncrementalCompilerImpl.scala:158)
//[error] 	at sbt.internal.inc.IncrementalCompilerImpl.compileIncrementally(IncrementalCompilerImpl.scala:237)
//[error] 	at sbt.internal.inc.IncrementalCompilerImpl.compile(IncrementalCompilerImpl.scala:68)
//[error] 	at sbt.Defaults$.compileIncrementalTaskImpl(Defaults.scala:1430)
//[error] 	at sbt.Defaults$.$anonfun$compileIncrementalTask$1(Defaults.scala:1404)
//[error] 	at scala.Function1.$anonfun$compose$1(Function1.scala:44)
//[error] 	at sbt.internal.util.$tilde$greater.$anonfun$$u2219$1(TypeFunctions.scala:39)
//[error] 	at sbt.std.Transform$$anon$4.work(System.scala:66)
//[error] 	at sbt.Execute.$anonfun$submit$2(Execute.scala:262)
//[error] 	at sbt.internal.util.ErrorHandling$.wideConvert(ErrorHandling.scala:16)
//[error] 	at sbt.Execute.work(Execute.scala:271)
//[error] 	at sbt.Execute.$anonfun$submit$1(Execute.scala:262)
//[error] 	at sbt.ConcurrentRestrictions$$anon$4.$anonfun$submitValid$1(ConcurrentRestrictions.scala:174)
//[error] 	at sbt.CompletionService$$anon$2.call(CompletionService.scala:36)
//[error] 	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
//[error] 	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
//[error] 	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
//[error] 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
//[error] 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
//[error] 	at java.lang.Thread.run(Thread.java:745)
//[error] (Test / compileIncremental) java.lang.IllegalArgumentException: Could not find proxy for val rw: com.twitter.io.Pipe in List(value rw, method $anonfun$new$15, value <local PipeTest>, class PipeTest, package io, package twitter, package com, package <root>) (currentOwner= value stabilizer$2 )
//[error] Total time: 11 s, completed May 21, 2019 5:07:38 PM
