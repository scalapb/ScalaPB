// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package scalapb.options

object ScalapbProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    com.google.protobuf.descriptor.DescriptorProtoCompanion
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      scalapb.options.ScalaPbOptions,
      scalapb.options.MessageOptions,
      scalapb.options.Collection,
      scalapb.options.FieldOptions,
      scalapb.options.EnumOptions,
      scalapb.options.EnumValueOptions,
      scalapb.options.OneofOptions,
      scalapb.options.FieldTransformation,
      scalapb.options.PreprocessorOutput
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """ChVzY2FsYXBiL3NjYWxhcGIucHJvdG8SB3NjYWxhcGIaIGdvb2dsZS9wcm90b2J1Zi9kZXNjcmlwdG9yLnByb3RvIqgWCg5TY
  2FsYVBiT3B0aW9ucxIzCgxwYWNrYWdlX25hbWUYASABKAlCEOI/DRILcGFja2FnZU5hbWVSC3BhY2thZ2VOYW1lEjMKDGZsYXRfc
  GFja2FnZRgCIAEoCEIQ4j8NEgtmbGF0UGFja2FnZVILZmxhdFBhY2thZ2USIwoGaW1wb3J0GAMgAygJQgviPwgSBmltcG9ydFIGa
  W1wb3J0EikKCHByZWFtYmxlGAQgAygJQg3iPwoSCHByZWFtYmxlUghwcmVhbWJsZRIwCgtzaW5nbGVfZmlsZRgFIAEoCEIP4j8ME
  gpzaW5nbGVGaWxlUgpzaW5nbGVGaWxlEkwKFW5vX3ByaW1pdGl2ZV93cmFwcGVycxgHIAEoCEIY4j8VEhNub1ByaW1pdGl2ZVdyY
  XBwZXJzUhNub1ByaW1pdGl2ZVdyYXBwZXJzEkUKEnByaW1pdGl2ZV93cmFwcGVycxgGIAEoCEIW4j8TEhFwcmltaXRpdmVXcmFwc
  GVyc1IRcHJpbWl0aXZlV3JhcHBlcnMSPAoPY29sbGVjdGlvbl90eXBlGAggASgJQhPiPxASDmNvbGxlY3Rpb25UeXBlUg5jb2xsZ
  WN0aW9uVHlwZRJYChdwcmVzZXJ2ZV91bmtub3duX2ZpZWxkcxgJIAEoCDoEdHJ1ZUIa4j8XEhVwcmVzZXJ2ZVVua25vd25GaWVsZ
  HNSFXByZXNlcnZlVW5rbm93bkZpZWxkcxIwCgtvYmplY3RfbmFtZRgKIAEoCUIP4j8MEgpvYmplY3ROYW1lUgpvYmplY3ROYW1lE
  kYKBXNjb3BlGAsgASgOMiQuc2NhbGFwYi5TY2FsYVBiT3B0aW9ucy5PcHRpb25zU2NvcGVCCuI/BxIFc2NvcGVSBXNjb3BlEikKB
  mxlbnNlcxgMIAEoCDoEdHJ1ZUIL4j8IEgZsZW5zZXNSBmxlbnNlcxJQChdyZXRhaW5fc291cmNlX2NvZGVfaW5mbxgNIAEoCEIZ4
  j8WEhRyZXRhaW5Tb3VyY2VDb2RlSW5mb1IUcmV0YWluU291cmNlQ29kZUluZm8SJwoIbWFwX3R5cGUYDiABKAlCDOI/CRIHbWFwV
  HlwZVIHbWFwVHlwZRJpCiBub19kZWZhdWx0X3ZhbHVlc19pbl9jb25zdHJ1Y3RvchgPIAEoCEIh4j8eEhxub0RlZmF1bHRWYWx1Z
  XNJbkNvbnN0cnVjdG9yUhxub0RlZmF1bHRWYWx1ZXNJbkNvbnN0cnVjdG9yEmkKEWVudW1fdmFsdWVfbmFtaW5nGBAgASgOMicuc
  2NhbGFwYi5TY2FsYVBiT3B0aW9ucy5FbnVtVmFsdWVOYW1pbmdCFOI/ERIPZW51bVZhbHVlTmFtaW5nUg9lbnVtVmFsdWVOYW1pb
  mcSRwoRZW51bV9zdHJpcF9wcmVmaXgYESABKAg6BWZhbHNlQhTiPxESD2VudW1TdHJpcFByZWZpeFIPZW51bVN0cmlwUHJlZml4E
  i0KCmJ5dGVzX3R5cGUYFSABKAlCDuI/CxIJYnl0ZXNUeXBlUglieXRlc1R5cGUSPwoQamF2YV9jb252ZXJzaW9ucxgXIAEoCEIU4
  j8REg9qYXZhQ29udmVyc2lvbnNSD2phdmFDb252ZXJzaW9ucxJxChNhdXhfbWVzc2FnZV9vcHRpb25zGBIgAygLMikuc2NhbGFwY
  i5TY2FsYVBiT3B0aW9ucy5BdXhNZXNzYWdlT3B0aW9uc0IW4j8TEhFhdXhNZXNzYWdlT3B0aW9uc1IRYXV4TWVzc2FnZU9wdGlvb
  nMSaQoRYXV4X2ZpZWxkX29wdGlvbnMYEyADKAsyJy5zY2FsYXBiLlNjYWxhUGJPcHRpb25zLkF1eEZpZWxkT3B0aW9uc0IU4j8RE
  g9hdXhGaWVsZE9wdGlvbnNSD2F1eEZpZWxkT3B0aW9ucxJlChBhdXhfZW51bV9vcHRpb25zGBQgAygLMiYuc2NhbGFwYi5TY2FsY
  VBiT3B0aW9ucy5BdXhFbnVtT3B0aW9uc0IT4j8QEg5hdXhFbnVtT3B0aW9uc1IOYXV4RW51bU9wdGlvbnMSegoWYXV4X2VudW1fd
  mFsdWVfb3B0aW9ucxgWIAMoCzIrLnNjYWxhcGIuU2NhbGFQYk9wdGlvbnMuQXV4RW51bVZhbHVlT3B0aW9uc0IY4j8VEhNhdXhFb
  nVtVmFsdWVPcHRpb25zUhNhdXhFbnVtVmFsdWVPcHRpb25zEjgKDXByZXByb2Nlc3NvcnMYGCADKAlCEuI/DxINcHJlcHJvY2Vzc
  29yc1INcHJlcHJvY2Vzc29ycxJsChVmaWVsZF90cmFuc2Zvcm1hdGlvbnMYGSADKAsyHC5zY2FsYXBiLkZpZWxkVHJhbnNmb3JtY
  XRpb25CGeI/FhIUZmllbGRUcmFuc2Zvcm1hdGlvbnNSFGZpZWxkVHJhbnNmb3JtYXRpb25zElsKGmlnbm9yZV9hbGxfdHJhbnNmb
  3JtYXRpb25zGBogASgIQh3iPxoSGGlnbm9yZUFsbFRyYW5zZm9ybWF0aW9uc1IYaWdub3JlQWxsVHJhbnNmb3JtYXRpb25zEiwKB
  2dldHRlcnMYGyABKAg6BHRydWVCDOI/CRIHZ2V0dGVyc1IHZ2V0dGVycxI5Cg5zY2FsYTNfc291cmNlcxgcIAEoCEIS4j8PEg1zY
  2FsYTNTb3VyY2VzUg1zY2FsYTNTb3VyY2VzEmEKHXRlc3Rfb25seV9ub19qYXZhX2NvbnZlcnNpb25zGOcHIAEoCEIe4j8bEhl0Z
  XN0T25seU5vSmF2YUNvbnZlcnNpb25zUhl0ZXN0T25seU5vSmF2YUNvbnZlcnNpb25zGq4BChFBdXhNZXNzYWdlT3B0aW9ucxIjC
  gZ0YXJnZXQYASABKAlCC+I/CBIGdGFyZ2V0UgZ0YXJnZXQSPwoHb3B0aW9ucxgCIAEoCzIXLnNjYWxhcGIuTWVzc2FnZU9wdGlvb
  nNCDOI/CRIHb3B0aW9uc1IHb3B0aW9ucxIzCgx0YXJnZXRfcmVnZXgYAyABKAlCEOI/DRILdGFyZ2V0UmVnZXhSC3RhcmdldFJlZ
  2V4GnUKD0F1eEZpZWxkT3B0aW9ucxIjCgZ0YXJnZXQYASABKAlCC+I/CBIGdGFyZ2V0UgZ0YXJnZXQSPQoHb3B0aW9ucxgCIAEoC
  zIVLnNjYWxhcGIuRmllbGRPcHRpb25zQgziPwkSB29wdGlvbnNSB29wdGlvbnMacwoOQXV4RW51bU9wdGlvbnMSIwoGdGFyZ2V0G
  AEgASgJQgviPwgSBnRhcmdldFIGdGFyZ2V0EjwKB29wdGlvbnMYAiABKAsyFC5zY2FsYXBiLkVudW1PcHRpb25zQgziPwkSB29wd
  GlvbnNSB29wdGlvbnMafQoTQXV4RW51bVZhbHVlT3B0aW9ucxIjCgZ0YXJnZXQYASABKAlCC+I/CBIGdGFyZ2V0UgZ0YXJnZXQSQ
  QoHb3B0aW9ucxgCIAEoCzIZLnNjYWxhcGIuRW51bVZhbHVlT3B0aW9uc0IM4j8JEgdvcHRpb25zUgdvcHRpb25zIj4KDE9wdGlvb
  nNTY29wZRITCgRGSUxFEAAaCeI/BhIERklMRRIZCgdQQUNLQUdFEAEaDOI/CRIHUEFDS0FHRSJVCg9FbnVtVmFsdWVOYW1pbmcSI
  QoLQVNfSU5fUFJPVE8QABoQ4j8NEgtBU19JTl9QUk9UTxIfCgpDQU1FTF9DQVNFEAEaD+I/DBIKQ0FNRUxfQ0FTRSoJCOgHEICAg
  IACIu8FCg5NZXNzYWdlT3B0aW9ucxImCgdleHRlbmRzGAEgAygJQgziPwkSB2V4dGVuZHNSB2V4dGVuZHMSQgoRY29tcGFuaW9uX
  2V4dGVuZHMYAiADKAlCFeI/EhIQY29tcGFuaW9uRXh0ZW5kc1IQY29tcGFuaW9uRXh0ZW5kcxIyCgthbm5vdGF0aW9ucxgDIAMoC
  UIQ4j8NEgthbm5vdGF0aW9uc1ILYW5ub3RhdGlvbnMSHQoEdHlwZRgEIAEoCUIJ4j8GEgR0eXBlUgR0eXBlEk4KFWNvbXBhbmlvb
  l9hbm5vdGF0aW9ucxgFIAMoCUIZ4j8WEhRjb21wYW5pb25Bbm5vdGF0aW9uc1IUY29tcGFuaW9uQW5ub3RhdGlvbnMSSQoUc2Vhb
  GVkX29uZW9mX2V4dGVuZHMYBiADKAlCF+I/FBISc2VhbGVkT25lb2ZFeHRlbmRzUhJzZWFsZWRPbmVvZkV4dGVuZHMSIQoGbm9fY
  m94GAcgASgIQgriPwcSBW5vQm94UgVub0JveBJbChp1bmtub3duX2ZpZWxkc19hbm5vdGF0aW9ucxgIIAMoCUId4j8aEhh1bmtub
  3duRmllbGRzQW5ub3RhdGlvbnNSGHVua25vd25GaWVsZHNBbm5vdGF0aW9ucxJpCiBub19kZWZhdWx0X3ZhbHVlc19pbl9jb25zd
  HJ1Y3RvchgJIAEoCEIh4j8eEhxub0RlZmF1bHRWYWx1ZXNJbkNvbnN0cnVjdG9yUhxub0RlZmF1bHRWYWx1ZXNJbkNvbnN0cnVjd
  G9yEmUKHnNlYWxlZF9vbmVvZl9jb21wYW5pb25fZXh0ZW5kcxgKIAMoCUIg4j8dEhtzZWFsZWRPbmVvZkNvbXBhbmlvbkV4dGVuZ
  HNSG3NlYWxlZE9uZW9mQ29tcGFuaW9uRXh0ZW5kcxImCgdkZXJpdmVzGAsgAygJQgziPwkSB2Rlcml2ZXNSB2Rlcml2ZXMqCQjoB
  xCAgICAAiJ/CgpDb2xsZWN0aW9uEh0KBHR5cGUYASABKAlCCeI/BhIEdHlwZVIEdHlwZRIqCglub25fZW1wdHkYAiABKAhCDeI/C
  hIIbm9uRW1wdHlSCG5vbkVtcHR5EiYKB2FkYXB0ZXIYAyABKAlCDOI/CRIHYWRhcHRlclIHYWRhcHRlciLWBAoMRmllbGRPcHRpb
  25zEh0KBHR5cGUYASABKAlCCeI/BhIEdHlwZVIEdHlwZRItCgpzY2FsYV9uYW1lGAIgASgJQg7iPwsSCXNjYWxhTmFtZVIJc2Nhb
  GFOYW1lEjwKD2NvbGxlY3Rpb25fdHlwZRgDIAEoCUIT4j8QEg5jb2xsZWN0aW9uVHlwZVIOY29sbGVjdGlvblR5cGUSRAoKY29sb
  GVjdGlvbhgIIAEoCzITLnNjYWxhcGIuQ29sbGVjdGlvbkIP4j8MEgpjb2xsZWN0aW9uUgpjb2xsZWN0aW9uEicKCGtleV90eXBlG
  AQgASgJQgziPwkSB2tleVR5cGVSB2tleVR5cGUSLQoKdmFsdWVfdHlwZRgFIAEoCUIO4j8LEgl2YWx1ZVR5cGVSCXZhbHVlVHlwZ
  RIyCgthbm5vdGF0aW9ucxgGIAMoCUIQ4j8NEgthbm5vdGF0aW9uc1ILYW5ub3RhdGlvbnMSJwoIbWFwX3R5cGUYByABKAlCDOI/C
  RIHbWFwVHlwZVIHbWFwVHlwZRJmCh9ub19kZWZhdWx0X3ZhbHVlX2luX2NvbnN0cnVjdG9yGAkgASgIQiDiPx0SG25vRGVmYXVsd
  FZhbHVlSW5Db25zdHJ1Y3RvclIbbm9EZWZhdWx0VmFsdWVJbkNvbnN0cnVjdG9yEiEKBm5vX2JveBgeIAEoCEIK4j8HEgVub0Jve
  FIFbm9Cb3gSKQoIcmVxdWlyZWQYHyABKAhCDeI/ChIIcmVxdWlyZWRSCHJlcXVpcmVkKgkI6AcQgICAgAIikAMKC0VudW1PcHRpb
  25zEiYKB2V4dGVuZHMYASADKAlCDOI/CRIHZXh0ZW5kc1IHZXh0ZW5kcxJCChFjb21wYW5pb25fZXh0ZW5kcxgCIAMoCUIV4j8SE
  hBjb21wYW5pb25FeHRlbmRzUhBjb21wYW5pb25FeHRlbmRzEh0KBHR5cGUYAyABKAlCCeI/BhIEdHlwZVIEdHlwZRI/ChBiYXNlX
  2Fubm90YXRpb25zGAQgAygJQhTiPxESD2Jhc2VBbm5vdGF0aW9uc1IPYmFzZUFubm90YXRpb25zElEKFnJlY29nbml6ZWRfYW5ub
  3RhdGlvbnMYBSADKAlCGuI/FxIVcmVjb2duaXplZEFubm90YXRpb25zUhVyZWNvZ25pemVkQW5ub3RhdGlvbnMSVwoYdW5yZWNvZ
  25pemVkX2Fubm90YXRpb25zGAYgAygJQhziPxkSF3VucmVjb2duaXplZEFubm90YXRpb25zUhd1bnJlY29nbml6ZWRBbm5vdGF0a
  W9ucyoJCOgHEICAgIACIqgBChBFbnVtVmFsdWVPcHRpb25zEiYKB2V4dGVuZHMYASADKAlCDOI/CRIHZXh0ZW5kc1IHZXh0ZW5kc
  xItCgpzY2FsYV9uYW1lGAIgASgJQg7iPwsSCXNjYWxhTmFtZVIJc2NhbGFOYW1lEjIKC2Fubm90YXRpb25zGAMgAygJQhDiPw0SC
  2Fubm90YXRpb25zUgthbm5vdGF0aW9ucyoJCOgHEICAgIACInAKDE9uZW9mT3B0aW9ucxImCgdleHRlbmRzGAEgAygJQgziPwkSB
  2V4dGVuZHNSB2V4dGVuZHMSLQoKc2NhbGFfbmFtZRgCIAEoCUIO4j8LEglzY2FsYU5hbWVSCXNjYWxhTmFtZSoJCOgHEICAgIACI
  uMBChNGaWVsZFRyYW5zZm9ybWF0aW9uEkQKBHdoZW4YASABKAsyJS5nb29nbGUucHJvdG9idWYuRmllbGREZXNjcmlwdG9yUHJvd
  G9CCeI/BhIEd2hlblIEd2hlbhJLCgptYXRjaF90eXBlGAIgASgOMhIuc2NhbGFwYi5NYXRjaFR5cGU6CENPTlRBSU5TQg7iPwsSC
  W1hdGNoVHlwZVIJbWF0Y2hUeXBlEjkKA3NldBgDIAEoCzIdLmdvb2dsZS5wcm90b2J1Zi5GaWVsZE9wdGlvbnNCCOI/BRIDc2V0U
  gNzZXQi8QEKElByZXByb2Nlc3Nvck91dHB1dBJqCg9vcHRpb25zX2J5X2ZpbGUYASADKAsyLi5zY2FsYXBiLlByZXByb2Nlc3Nvc
  k91dHB1dC5PcHRpb25zQnlGaWxlRW50cnlCEuI/DxINb3B0aW9uc0J5RmlsZVINb3B0aW9uc0J5RmlsZRpvChJPcHRpb25zQnlGa
  WxlRW50cnkSGgoDa2V5GAEgASgJQgjiPwUSA2tleVIDa2V5EjkKBXZhbHVlGAIgASgLMhcuc2NhbGFwYi5TY2FsYVBiT3B0aW9uc
  0IK4j8HEgV2YWx1ZVIFdmFsdWU6AjgBKlwKCU1hdGNoVHlwZRIbCghDT05UQUlOUxAAGg3iPwoSCENPTlRBSU5TEhUKBUVYQUNUE
  AEaCuI/BxIFRVhBQ1QSGwoIUFJFU0VOQ0UQAhoN4j8KEghQUkVTRU5DRTpQCgdvcHRpb25zEhwuZ29vZ2xlLnByb3RvYnVmLkZpb
  GVPcHRpb25zGPwHIAEoCzIXLnNjYWxhcGIuU2NhbGFQYk9wdGlvbnNSB29wdGlvbnM6UwoHbWVzc2FnZRIfLmdvb2dsZS5wcm90b
  2J1Zi5NZXNzYWdlT3B0aW9ucxj8ByABKAsyFy5zY2FsYXBiLk1lc3NhZ2VPcHRpb25zUgdtZXNzYWdlOksKBWZpZWxkEh0uZ29vZ
  2xlLnByb3RvYnVmLkZpZWxkT3B0aW9ucxj8ByABKAsyFS5zY2FsYXBiLkZpZWxkT3B0aW9uc1IFZmllbGQ6VgoMZW51bV9vcHRpb
  25zEhwuZ29vZ2xlLnByb3RvYnVmLkVudW1PcHRpb25zGPwHIAEoCzIULnNjYWxhcGIuRW51bU9wdGlvbnNSC2VudW1PcHRpb25zO
  lwKCmVudW1fdmFsdWUSIS5nb29nbGUucHJvdG9idWYuRW51bVZhbHVlT3B0aW9ucxj8ByABKAsyGS5zY2FsYXBiLkVudW1WYWx1Z
  U9wdGlvbnNSCWVudW1WYWx1ZTpLCgVvbmVvZhIdLmdvb2dsZS5wcm90b2J1Zi5PbmVvZk9wdGlvbnMY/AcgASgLMhUuc2NhbGFwY
  i5PbmVvZk9wdGlvbnNSBW9uZW9mQksKD3NjYWxhcGIub3B0aW9uc1oic2NhbGFwYi5naXRodWIuaW8vcHJvdG9idWYvc2NhbGFwY
  uI/EwoPc2NhbGFwYi5vcHRpb25zEAE="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      com.google.protobuf.descriptor.DescriptorProtoCompanion.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
  val options: _root_.scalapb.GeneratedExtension[com.google.protobuf.descriptor.FileOptions, _root_.scala.Option[scalapb.options.ScalaPbOptions]] =
    _root_.scalapb.GeneratedExtension.forOptionalUnknownMessageField(1020, _root_.scalapb.UnknownFieldSet.Field.lengthDelimitedLens)({__valueIn => _root_.scalapb.GeneratedExtension.readMessageFromByteString(scalapb.options.ScalaPbOptions)(__valueIn)}, {(__valueIn: scalapb.options.ScalaPbOptions) => __valueIn.toByteString})
  val message: _root_.scalapb.GeneratedExtension[com.google.protobuf.descriptor.MessageOptions, _root_.scala.Option[scalapb.options.MessageOptions]] =
    _root_.scalapb.GeneratedExtension.forOptionalUnknownMessageField(1020, _root_.scalapb.UnknownFieldSet.Field.lengthDelimitedLens)({__valueIn => _root_.scalapb.GeneratedExtension.readMessageFromByteString(scalapb.options.MessageOptions)(__valueIn)}, {(__valueIn: scalapb.options.MessageOptions) => __valueIn.toByteString})
  val field: _root_.scalapb.GeneratedExtension[com.google.protobuf.descriptor.FieldOptions, _root_.scala.Option[scalapb.options.FieldOptions]] =
    _root_.scalapb.GeneratedExtension.forOptionalUnknownMessageField(1020, _root_.scalapb.UnknownFieldSet.Field.lengthDelimitedLens)({__valueIn => _root_.scalapb.GeneratedExtension.readMessageFromByteString(scalapb.options.FieldOptions)(__valueIn)}, {(__valueIn: scalapb.options.FieldOptions) => __valueIn.toByteString})
  val enumOptions: _root_.scalapb.GeneratedExtension[com.google.protobuf.descriptor.EnumOptions, _root_.scala.Option[scalapb.options.EnumOptions]] =
    _root_.scalapb.GeneratedExtension.forOptionalUnknownMessageField(1020, _root_.scalapb.UnknownFieldSet.Field.lengthDelimitedLens)({__valueIn => _root_.scalapb.GeneratedExtension.readMessageFromByteString(scalapb.options.EnumOptions)(__valueIn)}, {(__valueIn: scalapb.options.EnumOptions) => __valueIn.toByteString})
  val enumValue: _root_.scalapb.GeneratedExtension[com.google.protobuf.descriptor.EnumValueOptions, _root_.scala.Option[scalapb.options.EnumValueOptions]] =
    _root_.scalapb.GeneratedExtension.forOptionalUnknownMessageField(1020, _root_.scalapb.UnknownFieldSet.Field.lengthDelimitedLens)({__valueIn => _root_.scalapb.GeneratedExtension.readMessageFromByteString(scalapb.options.EnumValueOptions)(__valueIn)}, {(__valueIn: scalapb.options.EnumValueOptions) => __valueIn.toByteString})
  val oneof: _root_.scalapb.GeneratedExtension[com.google.protobuf.descriptor.OneofOptions, _root_.scala.Option[scalapb.options.OneofOptions]] =
    _root_.scalapb.GeneratedExtension.forOptionalUnknownMessageField(1020, _root_.scalapb.UnknownFieldSet.Field.lengthDelimitedLens)({__valueIn => _root_.scalapb.GeneratedExtension.readMessageFromByteString(scalapb.options.OneofOptions)(__valueIn)}, {(__valueIn: scalapb.options.OneofOptions) => __valueIn.toByteString})
}