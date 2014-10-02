package org.thesamet.pb
/**
 * Created by thesamet on 9/29/14.
 */
trait MessageCompanion[A] {
  def parseFrom(s: Array[Byte]): A

  def serialize(a: A): Array[Byte]
}
