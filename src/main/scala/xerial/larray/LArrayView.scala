//--------------------------------------
//
// LArrayView.scala
// Since: 2013/03/25 18:04
//
//--------------------------------------

package xerial.larray

import reflect.ClassTag

/**
 * @author Taro L. Saito
 */
object LArrayView {

  class LIntArrayView(offset:Long, size:Long, base:LArray[Int]) extends AbstractLArrayView[Int](offset, size, base) {
    protected[this] def newBuilder: LBuilder[Int, LArray[Int]] = new LIntArrayBuilder


    /**
     * Byte size of an element. For example, if A is Int, its elementByteSize is 4
     */
    private[larray] def elementByteSize: Int = 4

    /**
     * Copy the contents of this LSeq[A] into the target LByteArray
     * @param dst
     * @param dstOffset
     */
    def copyTo(dst: LByteArray, dstOffset: Long) {
      base.copyTo(offset, dst, dstOffset, byteLength)
    }

    /**
     * Copy the contents of this sequence into the target LByteArray
     * @param srcOffset
     * @param dst
     * @param dstOffset
     * @param blen the byte length to copy
     */
    def copyTo(srcOffset: Long, dst: LByteArray, dstOffset: Long, blen: Long) {
      base.copyTo(srcOffset, dst, dstOffset, blen)
    }
  }

}

trait LArrayView[A] extends LArray[A] {

}

abstract class AbstractLArrayView[A : ClassTag](offset:Long, val size:Long, base:LArray[A]) extends LArrayView[A] {

  def free {
    // do nothing since this is a view
  }

  /**
   * Clear the contents of the array. It simply fills the array with zero bytes.
   */
  def clear() {
    throw new UnsupportedOperationException("clear")
  }

  /**
   * Retrieve an element
   * @param i index
   * @return the element value
   */
  def apply(i: Long): A = base.apply(i + offset)

  /**
   * Update an element
   * @param i index to be updated
   * @param v value to set
   * @return the value
   */
  def update(i: Long, v: A): A = throw new UnsupportedOperationException("update of view")

}
