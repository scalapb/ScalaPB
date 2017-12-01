// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package scalapb

import com.google.protobuf.ByteString
import com.google.protobuf.test.UnittestImport.{ImportEnum, ImportMessage}
import com.google.protobuf.test.UnittestImportPublic.PublicImportMessage
import protobuf_unittest.UnittestProto.{ForeignEnum, ForeignMessage, TestAllTypes}
import protobuf_unittest.unittest

object TestUtil {
  private def toBytes(str: String) = ByteString.copyFrom(str.getBytes("UTF-8"))

  def getAllTypesAllSet: unittest.TestAllTypes = {
    val message = TestAllTypes.newBuilder()
    message.setOptionalInt32(101)
    message.setOptionalInt64(102)
    message.setOptionalUint32(103)
    message.setOptionalUint64(104)
    message.setOptionalSint32(105)
    message.setOptionalSint64(106)
    message.setOptionalFixed32(107)
    message.setOptionalFixed64(108)
    message.setOptionalSfixed32(109)
    message.setOptionalSfixed64(110)
    message.setOptionalFloat(111)
    message.setOptionalDouble(112)
    message.setOptionalBool(true)
    message.setOptionalString("115")
    message.setOptionalBytes(toBytes("116"))

    message.setOptionalNestedMessage(
      TestAllTypes.NestedMessage.newBuilder().setBb(118).build())
    message.setOptionalForeignMessage(
      ForeignMessage.newBuilder().setC(119).build())
    message.setOptionalImportMessage(
      ImportMessage.newBuilder().setD(120).build())
    message.setOptionalPublicImportMessage(
      PublicImportMessage.newBuilder().setE(126).build())
    message.setOptionalLazyMessage(
      TestAllTypes.NestedMessage.newBuilder().setBb(127).build())

    message.setOptionalNestedEnum(TestAllTypes.NestedEnum.BAZ)
    message.setOptionalForeignEnum(ForeignEnum.FOREIGN_BAZ)
    message.setOptionalImportEnum(ImportEnum.IMPORT_BAZ)

    message.setOptionalStringPiece("124")
    message.setOptionalCord("125")

    // -----------------------------------------------------------------

    message.addRepeatedInt32(201)
    message.addRepeatedInt64(202)
    message.addRepeatedUint32(203)
    message.addRepeatedUint64(204)
    message.addRepeatedSint32(205)
    message.addRepeatedSint64(206)
    message.addRepeatedFixed32(207)
    message.addRepeatedFixed64(208)
    message.addRepeatedSfixed32(209)
    message.addRepeatedSfixed64(210)
    message.addRepeatedFloat(211)
    message.addRepeatedDouble(212)
    message.addRepeatedBool(true)
    message.addRepeatedString("215")
    message.addRepeatedBytes(toBytes("216"))

    message.addRepeatedNestedMessage(
      TestAllTypes.NestedMessage.newBuilder().setBb(218).build())
    message.addRepeatedForeignMessage(
      ForeignMessage.newBuilder().setC(219).build())
    message.addRepeatedImportMessage(
      ImportMessage.newBuilder().setD(220).build())
    message.addRepeatedLazyMessage(
      TestAllTypes.NestedMessage.newBuilder().setBb(227).build())

    message.addRepeatedNestedEnum(TestAllTypes.NestedEnum.BAR)
    message.addRepeatedForeignEnum(ForeignEnum.FOREIGN_BAR)
    message.addRepeatedImportEnum(ImportEnum.IMPORT_BAR)

    message.addRepeatedStringPiece("224")
    message.addRepeatedCord("225")

    // Add a second one of each field.
    message.addRepeatedInt32(301)
    message.addRepeatedInt64(302)
    message.addRepeatedUint32(303)
    message.addRepeatedUint64(304)
    message.addRepeatedSint32(305)
    message.addRepeatedSint64(306)
    message.addRepeatedFixed32(307)
    message.addRepeatedFixed64(308)
    message.addRepeatedSfixed32(309)
    message.addRepeatedSfixed64(310)
    message.addRepeatedFloat(311)
    message.addRepeatedDouble(312)
    message.addRepeatedBool(false)
    message.addRepeatedString("315")
    message.addRepeatedBytes(toBytes("316"))

    message.addRepeatedNestedMessage(
      TestAllTypes.NestedMessage.newBuilder().setBb(318).build())
    message.addRepeatedForeignMessage(
      ForeignMessage.newBuilder().setC(319).build())
    message.addRepeatedImportMessage(
      ImportMessage.newBuilder().setD(320).build())
    message.addRepeatedLazyMessage(
      TestAllTypes.NestedMessage.newBuilder().setBb(327).build())

    message.addRepeatedNestedEnum(TestAllTypes.NestedEnum.BAZ)
    message.addRepeatedForeignEnum(ForeignEnum.FOREIGN_BAZ)
    message.addRepeatedImportEnum(ImportEnum.IMPORT_BAZ)

    message.addRepeatedStringPiece("324")
    message.addRepeatedCord("325")

    // -----------------------------------------------------------------

    message.setDefaultInt32(401)
    message.setDefaultInt64(402)
    message.setDefaultUint32(403)
    message.setDefaultUint64(404)
    message.setDefaultSint32(405)
    message.setDefaultSint64(406)
    message.setDefaultFixed32(407)
    message.setDefaultFixed64(408)
    message.setDefaultSfixed32(409)
    message.setDefaultSfixed64(410)
    message.setDefaultFloat(411)
    message.setDefaultDouble(412)
    message.setDefaultBool(false)
    message.setDefaultString("415")
    message.setDefaultBytes(toBytes("416"))

    message.setDefaultNestedEnum(TestAllTypes.NestedEnum.FOO)
    message.setDefaultForeignEnum(ForeignEnum.FOREIGN_FOO)
    message.setDefaultImportEnum(ImportEnum.IMPORT_FOO)

    message.setDefaultStringPiece("424")
    message.setDefaultCord("425")

    message.setOneofUint32(601)
    message.setOneofNestedMessage(
      TestAllTypes.NestedMessage.newBuilder().setBb(602).build())
    message.setOneofString("603")
    message.setOneofBytes(toBytes("604"))
    protobuf_unittest.unittest.TestAllTypes.fromJavaProto(message.build())
  }
}
