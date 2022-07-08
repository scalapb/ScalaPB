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
import com.google.protobuf.test.unittest_import.{ImportEnum, ImportMessage}
import com.google.protobuf.test.unittest_import_public.PublicImportMessage
import protobuf_unittest.unittest.{ForeignMessage, ForeignEnum, TestAllTypes}
import org.scalactic.source.Position

import scala.reflect.ClassTag
import scala.annotation.nowarn

object TestUtil {
  def isScalaJS = System.getProperty("java.vm.name") == "Scala.js"

  private def toBytes(str: String) = ByteString.copyFrom(str.getBytes("UTF-8"))

  def isAnyVal[T](
      @nowarn value: T
  )(implicit pos: Position, tag: ClassTag[T], ev: T <:< AnyVal = null): Unit = {
    if (ev == null) {
      org.scalatest.matchers.must.Matchers.fail(s"${tag.toString()} is not AnyVal")
    }
  }

  def getAllTypesAllSet: TestAllTypes = {
    TestAllTypes().update(
      _.optionalInt32               := 101,
      _.optionalInt64               := 102,
      _.optionalUint32              := 103,
      _.optionalUint64              := 104,
      _.optionalSint32              := 105,
      _.optionalSint64              := 106,
      _.optionalFixed32             := 107,
      _.optionalFixed64             := 108,
      _.optionalSfixed32            := 109,
      _.optionalSfixed64            := 110,
      _.optionalFloat               := 111,
      _.optionalDouble              := 112,
      _.optionalBool                := true,
      _.optionalString              := "115",
      _.optionalBytes               := toBytes("116"),
      _.optionalNestedMessage       := TestAllTypes.NestedMessage(bb = Some(118)),
      _.optionalForeignMessage      := ForeignMessage(c = Some(119)),
      _.optionalImportMessage       := ImportMessage(d = Some(120)),
      _.optionalPublicImportMessage := PublicImportMessage(e = Some(126)),
      _.optionalLazyMessage         := TestAllTypes.NestedMessage(bb = Some(127)),
      _.optionalNestedEnum          := TestAllTypes.NestedEnum.BAZ,
      _.optionalForeignEnum         := ForeignEnum.FOREIGN_BAZ,
      _.optionalImportEnum          := ImportEnum.IMPORT_BAZ,
      _.optionalStringPiece         := "124",
      _.optionalCord                := "125",

      // -----------------------------------------------------------------

      _.repeatedInt32 :+= 201,
      _.repeatedInt64 :+= 202,
      _.repeatedUint32 :+= 203,
      _.repeatedUint64 :+= 204,
      _.repeatedSint32 :+= 205,
      _.repeatedSint64 :+= 206,
      _.repeatedFixed32 :+= 207,
      _.repeatedFixed64 :+= 208,
      _.repeatedSfixed32 :+= 209,
      _.repeatedSfixed64 :+= 210,
      _.repeatedFloat :+= 211,
      _.repeatedDouble :+= 212,
      _.repeatedBool :+= true,
      _.repeatedString :+= "215",
      _.repeatedBytes :+= toBytes("216"),
      _.repeatedNestedMessage :+= TestAllTypes.NestedMessage(bb = Some(218)),
      _.repeatedForeignMessage :+= ForeignMessage(c = Some(219)),
      _.repeatedImportMessage :+= ImportMessage(d = Some(220)),
      _.repeatedLazyMessage :+= TestAllTypes.NestedMessage(bb = Some(227)),
      _.repeatedNestedEnum :+= TestAllTypes.NestedEnum.BAR,
      _.repeatedForeignEnum :+= ForeignEnum.FOREIGN_BAR,
      _.repeatedImportEnum :+= ImportEnum.IMPORT_BAR,
      _.repeatedStringPiece :+= "224",
      _.repeatedCord :+= "225",

      // Add second one of each field.
      _.repeatedInt32 :+= 301,
      _.repeatedInt64 :+= 302,
      _.repeatedUint32 :+= 303,
      _.repeatedUint64 :+= 304,
      _.repeatedSint32 :+= 305,
      _.repeatedSint64 :+= 306,
      _.repeatedFixed32 :+= 307,
      _.repeatedFixed64 :+= 308,
      _.repeatedSfixed32 :+= 309,
      _.repeatedSfixed64 :+= 310,
      _.repeatedFloat :+= 311,
      _.repeatedDouble :+= 312,
      _.repeatedBool :+= false,
      _.repeatedString :+= "315",
      _.repeatedBytes :+= toBytes("316"),
      _.repeatedNestedMessage :+= TestAllTypes.NestedMessage(bb = Some(318)),
      _.repeatedForeignMessage :+= ForeignMessage(c = Some(319)),
      _.repeatedImportMessage :+= ImportMessage(d = Some(320)),
      _.repeatedLazyMessage :+= TestAllTypes.NestedMessage(bb = Some(327)),
      _.repeatedNestedEnum :+= TestAllTypes.NestedEnum.BAZ,
      _.repeatedForeignEnum :+= ForeignEnum.FOREIGN_BAZ,
      _.repeatedImportEnum :+= ImportEnum.IMPORT_BAZ,
      _.repeatedStringPiece :+= "324",
      _.repeatedCord :+= "325",

      // -----------------------------------------------------------------

      _.defaultInt32       := 401,
      _.defaultInt64       := 402,
      _.defaultUint32      := 403,
      _.defaultUint64      := 404,
      _.defaultSint32      := 405,
      _.defaultSint64      := 406,
      _.defaultFixed32     := 407,
      _.defaultFixed64     := 408,
      _.defaultSfixed32    := 409,
      _.defaultSfixed64    := 410,
      _.defaultFloat       := 411,
      _.defaultDouble      := 412,
      _.defaultBool        := false,
      _.defaultString      := "415",
      _.defaultBytes       := toBytes("416"),
      _.defaultNestedEnum  := TestAllTypes.NestedEnum.FOO,
      _.defaultForeignEnum := ForeignEnum.FOREIGN_FOO,
      _.defaultImportEnum  := ImportEnum.IMPORT_FOO,
      _.defaultStringPiece := "424",
      _.defaultCord        := "425",
      _.oneofUint32        := 601,
      _.oneofNestedMessage := TestAllTypes.NestedMessage(bb = Some(602)),
      _.oneofString        := "603",
      _.oneofBytes         := toBytes("604")
    )
  }
}
