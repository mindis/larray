//--------------------------------------
//
// RawByteArrayTestTest.scala
// Since: 2013/12/03 12:02 PM
//
//--------------------------------------

package xerial.larray.buffer

import java.nio.ByteBuffer
import xerial.larray.LArraySpec
import scala.util.Random
import xerial.core.util.DataUnit


/**
 * @author Taro L. Saito
 */
class LBufferTest extends LArraySpec {

  implicit class RichArray(m:LBuffer) {
    def toCSV = m.toArray.mkString(", ")
  }


  "LBuffer" should {

    "allocate memory" in {
      val size = 1000
      val m = new LBuffer(size)
      m.putInt(0, 0)
      m.putInt(4, 1)
      m.putInt(8, 130)

      m.getInt(0) shouldBe 0
      m.getInt(4) shouldBe 1
      m.getInt(8) shouldBe 130

      m.size() shouldBe size.toLong

      (0 until size).foreach(i => m.putByte(i, (i % 128).toByte))
      (0 until size).forall(i => m.getByte(i) == (i % 128).toByte) should be (true)

      m.clear()

      (0 until size).forall(i => m.getByte(i) == 0) should be (true)

      m.release()
    }

    "convert to array" in {
      val size = 12
      val m = new LBuffer(size);
      for(i <- 0 until size)
        m(i) = i.toByte
      debug(m.toCSV)

      m.clear()
      debug(m.toCSV)
    }


    "allocate in single-thread" taggedAs("bench-single") in {

      val N = 100
      def range = (0 until N)
      val R = 2
      val S = 1024 * 1024

      info("start buffer allocation test")

      time("single-thread allocation", repeat=10) {
        block("without zero-filling", repeat=R) {
          for(i <- range) yield {
            new LBuffer(S)
          }
        }

        block("with zero-filling", repeat=R) {
          for(i <- range) yield {
            val m = new LBuffer(S)
            m.clear()
            m
          }
        }

        block("java array", repeat=R) {
          for(i <- range) yield {
            new Array[Byte](S)
          }
        }

        block("byte buffer", repeat=R) {
          for(i <- range) yield {
            ByteBuffer.allocate(S)
          }
        }

        block("direct byte buffer", repeat=R) {
          for(i <- range) yield {
            ByteBuffer.allocateDirect(S)
          }
        }

      }
    }

    "allocate concurrently" taggedAs("bench") in {

      val N = 100
      def range = (0 until N).par
      val R = 2
      val S = 1024 * 1024

      info("start buffer allocation test")

      time("concurrent allocation", repeat=10) {
        block("without zero-filling", repeat=R) {
          for(i <- range) yield {
            new LBuffer(S)
          }
        }

        block("with zero-filling", repeat=R) {
          for(i <- range) yield {
            val m = new LBuffer(S)
            m.clear()
            m
          }
        }

        block("java array", repeat=R) {
          for(i <- range) yield {
            new Array[Byte](S)
          }
        }

        block("byte buffer", repeat=R) {
          for(i <- range) yield {
            ByteBuffer.allocate(S)
          }
        }

        block("direct byte buffer", repeat=R) {
          for(i <- range) yield {
            ByteBuffer.allocateDirect(S)
          }
        }

      }
    }

    "Use less memory" taggedAs("heap") in {

      // Need to produce meaningful memory usage
      pending

      val N = 100000
      val M = 1024

      val rt = Runtime.getRuntime

      case class Report(tag:String, free:Long, offHeap:Long, total:Long) {
        override def toString = s"[${tag}] free:${DataUnit.toHumanReadableFormat(free)}, offheap:${DataUnit.toHumanReadableFormat(offHeap)}"
      }

      val memUsage = Seq.newBuilder[Report]

      def report(tag:String) = {
        val offHeap = LBufferConfig.allocator.allocatedSize()
        val rep = Report(tag, rt.freeMemory(), offHeap, rt.totalMemory())
        //memUsage += rep
        rep
      }

      var r1 : Seq[Array[Byte]] = null
      var r2 : Seq[LBuffer] = null

      time("memory allocation", repeat=3) {
        Thread.sleep(5000)
        block("Array") {
          info(report("Array"))
          val result = for(i <- 0 until N) yield {
            val a = new Array[Byte](M)
            report("Array")
            a
          }
          info(report("Array"))
        }

//        info("gc")
//        System.gc()
        Thread.sleep(5000)

        block("LBuffer") {
          info(report("LBuffer"))
          val result = for(i <- 0 until N) yield {
            val l = new LBuffer(M)
            report("LBuffer")
            l
          }
          info(report("LBuffer"))
        }

      }


    }

  }
}