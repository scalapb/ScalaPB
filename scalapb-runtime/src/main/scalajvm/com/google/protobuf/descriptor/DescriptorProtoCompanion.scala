// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!

package com.google.protobuf.descriptor

object DescriptorProtoCompanion extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq.empty
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      com.google.protobuf.descriptor.FileDescriptorSet,
      com.google.protobuf.descriptor.FileDescriptorProto,
      com.google.protobuf.descriptor.DescriptorProto,
      com.google.protobuf.descriptor.ExtensionRangeOptions,
      com.google.protobuf.descriptor.FieldDescriptorProto,
      com.google.protobuf.descriptor.OneofDescriptorProto,
      com.google.protobuf.descriptor.EnumDescriptorProto,
      com.google.protobuf.descriptor.EnumValueDescriptorProto,
      com.google.protobuf.descriptor.ServiceDescriptorProto,
      com.google.protobuf.descriptor.MethodDescriptorProto,
      com.google.protobuf.descriptor.FileOptions,
      com.google.protobuf.descriptor.MessageOptions,
      com.google.protobuf.descriptor.FieldOptions,
      com.google.protobuf.descriptor.OneofOptions,
      com.google.protobuf.descriptor.EnumOptions,
      com.google.protobuf.descriptor.EnumValueOptions,
      com.google.protobuf.descriptor.ServiceOptions,
      com.google.protobuf.descriptor.MethodOptions,
      com.google.protobuf.descriptor.UninterpretedOption,
      com.google.protobuf.descriptor.FeatureSet,
      com.google.protobuf.descriptor.FeatureSetDefaults,
      com.google.protobuf.descriptor.SourceCodeInfo,
      com.google.protobuf.descriptor.GeneratedCodeInfo
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CiBnb29nbGUvcHJvdG9idWYvZGVzY3JpcHRvci5wcm90bxIPZ29vZ2xlLnByb3RvYnVmIlgKEUZpbGVEZXNjcmlwdG9yU2V0E
  kMKBGZpbGUYASADKAsyJC5nb29nbGUucHJvdG9idWYuRmlsZURlc2NyaXB0b3JQcm90b0IJ4j8GEgRmaWxlUgRmaWxlIusGChNGa
  WxlRGVzY3JpcHRvclByb3RvEh0KBG5hbWUYASABKAlCCeI/BhIEbmFtZVIEbmFtZRImCgdwYWNrYWdlGAIgASgJQgziPwkSB3BhY
  2thZ2VSB3BhY2thZ2USLwoKZGVwZW5kZW5jeRgDIAMoCUIP4j8MEgpkZXBlbmRlbmN5UgpkZXBlbmRlbmN5EkIKEXB1YmxpY19kZ
  XBlbmRlbmN5GAogAygFQhXiPxISEHB1YmxpY0RlcGVuZGVuY3lSEHB1YmxpY0RlcGVuZGVuY3kSPAoPd2Vha19kZXBlbmRlbmN5G
  AsgAygFQhPiPxASDndlYWtEZXBlbmRlbmN5Ug53ZWFrRGVwZW5kZW5jeRJVCgxtZXNzYWdlX3R5cGUYBCADKAsyIC5nb29nbGUuc
  HJvdG9idWYuRGVzY3JpcHRvclByb3RvQhDiPw0SC21lc3NhZ2VUeXBlUgttZXNzYWdlVHlwZRJQCgllbnVtX3R5cGUYBSADKAsyJ
  C5nb29nbGUucHJvdG9idWYuRW51bURlc2NyaXB0b3JQcm90b0IN4j8KEghlbnVtVHlwZVIIZW51bVR5cGUSTwoHc2VydmljZRgGI
  AMoCzInLmdvb2dsZS5wcm90b2J1Zi5TZXJ2aWNlRGVzY3JpcHRvclByb3RvQgziPwkSB3NlcnZpY2VSB3NlcnZpY2USUwoJZXh0Z
  W5zaW9uGAcgAygLMiUuZ29vZ2xlLnByb3RvYnVmLkZpZWxkRGVzY3JpcHRvclByb3RvQg7iPwsSCWV4dGVuc2lvblIJZXh0ZW5za
  W9uEkQKB29wdGlvbnMYCCABKAsyHC5nb29nbGUucHJvdG9idWYuRmlsZU9wdGlvbnNCDOI/CRIHb3B0aW9uc1IHb3B0aW9ucxJeC
  hBzb3VyY2VfY29kZV9pbmZvGAkgASgLMh8uZ29vZ2xlLnByb3RvYnVmLlNvdXJjZUNvZGVJbmZvQhPiPxASDnNvdXJjZUNvZGVJb
  mZvUg5zb3VyY2VDb2RlSW5mbxIjCgZzeW50YXgYDCABKAlCC+I/CBIGc3ludGF4UgZzeW50YXgSQAoHZWRpdGlvbhgOIAEoDjIYL
  mdvb2dsZS5wcm90b2J1Zi5FZGl0aW9uQgziPwkSB2VkaXRpb25SB2VkaXRpb24ilQgKD0Rlc2NyaXB0b3JQcm90bxIdCgRuYW1lG
  AEgASgJQgniPwYSBG5hbWVSBG5hbWUSRwoFZmllbGQYAiADKAsyJS5nb29nbGUucHJvdG9idWYuRmllbGREZXNjcmlwdG9yUHJvd
  G9CCuI/BxIFZmllbGRSBWZpZWxkElMKCWV4dGVuc2lvbhgGIAMoCzIlLmdvb2dsZS5wcm90b2J1Zi5GaWVsZERlc2NyaXB0b3JQc
  m90b0IO4j8LEglleHRlbnNpb25SCWV4dGVuc2lvbhJSCgtuZXN0ZWRfdHlwZRgDIAMoCzIgLmdvb2dsZS5wcm90b2J1Zi5EZXNjc
  mlwdG9yUHJvdG9CD+I/DBIKbmVzdGVkVHlwZVIKbmVzdGVkVHlwZRJQCgllbnVtX3R5cGUYBCADKAsyJC5nb29nbGUucHJvdG9id
  WYuRW51bURlc2NyaXB0b3JQcm90b0IN4j8KEghlbnVtVHlwZVIIZW51bVR5cGUSbQoPZXh0ZW5zaW9uX3JhbmdlGAUgAygLMi8uZ
  29vZ2xlLnByb3RvYnVmLkRlc2NyaXB0b3JQcm90by5FeHRlbnNpb25SYW5nZUIT4j8QEg5leHRlbnNpb25SYW5nZVIOZXh0ZW5za
  W9uUmFuZ2USVAoKb25lb2ZfZGVjbBgIIAMoCzIlLmdvb2dsZS5wcm90b2J1Zi5PbmVvZkRlc2NyaXB0b3JQcm90b0IO4j8LEglvb
  mVvZkRlY2xSCW9uZW9mRGVjbBJHCgdvcHRpb25zGAcgASgLMh8uZ29vZ2xlLnByb3RvYnVmLk1lc3NhZ2VPcHRpb25zQgziPwkSB
  29wdGlvbnNSB29wdGlvbnMSaQoOcmVzZXJ2ZWRfcmFuZ2UYCSADKAsyLi5nb29nbGUucHJvdG9idWYuRGVzY3JpcHRvclByb3RvL
  lJlc2VydmVkUmFuZ2VCEuI/DxINcmVzZXJ2ZWRSYW5nZVINcmVzZXJ2ZWRSYW5nZRI2Cg1yZXNlcnZlZF9uYW1lGAogAygJQhHiP
  w4SDHJlc2VydmVkTmFtZVIMcmVzZXJ2ZWROYW1lGp4BCg5FeHRlbnNpb25SYW5nZRIgCgVzdGFydBgBIAEoBUIK4j8HEgVzdGFyd
  FIFc3RhcnQSGgoDZW5kGAIgASgFQgjiPwUSA2VuZFIDZW5kEk4KB29wdGlvbnMYAyABKAsyJi5nb29nbGUucHJvdG9idWYuRXh0Z
  W5zaW9uUmFuZ2VPcHRpb25zQgziPwkSB29wdGlvbnNSB29wdGlvbnMaTQoNUmVzZXJ2ZWRSYW5nZRIgCgVzdGFydBgBIAEoBUIK4
  j8HEgVzdGFydFIFc3RhcnQSGgoDZW5kGAIgASgFQgjiPwUSA2VuZFIDZW5kIv4FChVFeHRlbnNpb25SYW5nZU9wdGlvbnMScgoUd
  W5pbnRlcnByZXRlZF9vcHRpb24Y5wcgAygLMiQuZ29vZ2xlLnByb3RvYnVmLlVuaW50ZXJwcmV0ZWRPcHRpb25CGOI/FRITdW5pb
  nRlcnByZXRlZE9wdGlvblITdW5pbnRlcnByZXRlZE9wdGlvbhJpCgtkZWNsYXJhdGlvbhgCIAMoCzIyLmdvb2dsZS5wcm90b2J1Z
  i5FeHRlbnNpb25SYW5nZU9wdGlvbnMuRGVjbGFyYXRpb25CE4gBAuI/DRILZGVjbGFyYXRpb25SC2RlY2xhcmF0aW9uEkYKCGZlY
  XR1cmVzGDIgASgLMhsuZ29vZ2xlLnByb3RvYnVmLkZlYXR1cmVTZXRCDeI/ChIIZmVhdHVyZXNSCGZlYXR1cmVzEn4KDHZlcmlma
  WNhdGlvbhgDIAEoDjI4Lmdvb2dsZS5wcm90b2J1Zi5FeHRlbnNpb25SYW5nZU9wdGlvbnMuVmVyaWZpY2F0aW9uU3RhdGU6ClVOV
  kVSSUZJRURCFIgBAuI/DhIMdmVyaWZpY2F0aW9uUgx2ZXJpZmljYXRpb24a2QEKC0RlY2xhcmF0aW9uEiMKBm51bWJlchgBIAEoB
  UIL4j8IEgZudW1iZXJSBm51bWJlchIqCglmdWxsX25hbWUYAiABKAlCDeI/ChIIZnVsbE5hbWVSCGZ1bGxOYW1lEh0KBHR5cGUYA
  yABKAlCCeI/BhIEdHlwZVIEdHlwZRIpCghyZXNlcnZlZBgFIAEoCEIN4j8KEghyZXNlcnZlZFIIcmVzZXJ2ZWQSKQoIcmVwZWF0Z
  WQYBiABKAhCDeI/ChIIcmVwZWF0ZWRSCHJlcGVhdGVkSgQIBBAFIlcKEVZlcmlmaWNhdGlvblN0YXRlEiEKC0RFQ0xBUkFUSU9OE
  AAaEOI/DRILREVDTEFSQVRJT04SHwoKVU5WRVJJRklFRBABGg/iPwwSClVOVkVSSUZJRUQqCQjoBxCAgICAAiLmCgoURmllbGREZ
  XNjcmlwdG9yUHJvdG8SHQoEbmFtZRgBIAEoCUIJ4j8GEgRuYW1lUgRuYW1lEiMKBm51bWJlchgDIAEoBUIL4j8IEgZudW1iZXJSB
  m51bWJlchJNCgVsYWJlbBgEIAEoDjIrLmdvb2dsZS5wcm90b2J1Zi5GaWVsZERlc2NyaXB0b3JQcm90by5MYWJlbEIK4j8HEgVsY
  WJlbFIFbGFiZWwSSQoEdHlwZRgFIAEoDjIqLmdvb2dsZS5wcm90b2J1Zi5GaWVsZERlc2NyaXB0b3JQcm90by5UeXBlQgniPwYSB
  HR5cGVSBHR5cGUSKgoJdHlwZV9uYW1lGAYgASgJQg3iPwoSCHR5cGVOYW1lUgh0eXBlTmFtZRIpCghleHRlbmRlZRgCIAEoCUIN4
  j8KEghleHRlbmRlZVIIZXh0ZW5kZWUSNgoNZGVmYXVsdF92YWx1ZRgHIAEoCUIR4j8OEgxkZWZhdWx0VmFsdWVSDGRlZmF1bHRWY
  Wx1ZRIwCgtvbmVvZl9pbmRleBgJIAEoBUIP4j8MEgpvbmVvZkluZGV4UgpvbmVvZkluZGV4EioKCWpzb25fbmFtZRgKIAEoCUIN4
  j8KEghqc29uTmFtZVIIanNvbk5hbWUSRQoHb3B0aW9ucxgIIAEoCzIdLmdvb2dsZS5wcm90b2J1Zi5GaWVsZE9wdGlvbnNCDOI/C
  RIHb3B0aW9uc1IHb3B0aW9ucxI8Cg9wcm90bzNfb3B0aW9uYWwYESABKAhCE+I/EBIOcHJvdG8zT3B0aW9uYWxSDnByb3RvM09wd
  GlvbmFsIvgECgRUeXBlEiEKC1RZUEVfRE9VQkxFEAEaEOI/DRILVFlQRV9ET1VCTEUSHwoKVFlQRV9GTE9BVBACGg/iPwwSClRZU
  EVfRkxPQVQSHwoKVFlQRV9JTlQ2NBADGg/iPwwSClRZUEVfSU5UNjQSIQoLVFlQRV9VSU5UNjQQBBoQ4j8NEgtUWVBFX1VJTlQ2N
  BIfCgpUWVBFX0lOVDMyEAUaD+I/DBIKVFlQRV9JTlQzMhIjCgxUWVBFX0ZJWEVENjQQBhoR4j8OEgxUWVBFX0ZJWEVENjQSIwoMV
  FlQRV9GSVhFRDMyEAcaEeI/DhIMVFlQRV9GSVhFRDMyEh0KCVRZUEVfQk9PTBAIGg7iPwsSCVRZUEVfQk9PTBIhCgtUWVBFX1NUU
  klORxAJGhDiPw0SC1RZUEVfU1RSSU5HEh8KClRZUEVfR1JPVVAQChoP4j8MEgpUWVBFX0dST1VQEiMKDFRZUEVfTUVTU0FHRRALG
  hHiPw4SDFRZUEVfTUVTU0FHRRIfCgpUWVBFX0JZVEVTEAwaD+I/DBIKVFlQRV9CWVRFUxIhCgtUWVBFX1VJTlQzMhANGhDiPw0SC
  1RZUEVfVUlOVDMyEh0KCVRZUEVfRU5VTRAOGg7iPwsSCVRZUEVfRU5VTRIlCg1UWVBFX1NGSVhFRDMyEA8aEuI/DxINVFlQRV9TR
  klYRUQzMhIlCg1UWVBFX1NGSVhFRDY0EBAaEuI/DxINVFlQRV9TRklYRUQ2NBIhCgtUWVBFX1NJTlQzMhARGhDiPw0SC1RZUEVfU
  0lOVDMyEiEKC1RZUEVfU0lOVDY0EBIaEOI/DRILVFlQRV9TSU5UNjQiggEKBUxhYmVsEicKDkxBQkVMX09QVElPTkFMEAEaE+I/E
  BIOTEFCRUxfT1BUSU9OQUwSJwoOTEFCRUxfUkVQRUFURUQQAxoT4j8QEg5MQUJFTF9SRVBFQVRFRBInCg5MQUJFTF9SRVFVSVJFR
  BACGhPiPxASDkxBQkVMX1JFUVVJUkVEInwKFE9uZW9mRGVzY3JpcHRvclByb3RvEh0KBG5hbWUYASABKAlCCeI/BhIEbmFtZVIEb
  mFtZRJFCgdvcHRpb25zGAIgASgLMh0uZ29vZ2xlLnByb3RvYnVmLk9uZW9mT3B0aW9uc0IM4j8JEgdvcHRpb25zUgdvcHRpb25zI
  sUDChNFbnVtRGVzY3JpcHRvclByb3RvEh0KBG5hbWUYASABKAlCCeI/BhIEbmFtZVIEbmFtZRJLCgV2YWx1ZRgCIAMoCzIpLmdvb
  2dsZS5wcm90b2J1Zi5FbnVtVmFsdWVEZXNjcmlwdG9yUHJvdG9CCuI/BxIFdmFsdWVSBXZhbHVlEkQKB29wdGlvbnMYAyABKAsyH
  C5nb29nbGUucHJvdG9idWYuRW51bU9wdGlvbnNCDOI/CRIHb3B0aW9uc1IHb3B0aW9ucxJxCg5yZXNlcnZlZF9yYW5nZRgEIAMoC
  zI2Lmdvb2dsZS5wcm90b2J1Zi5FbnVtRGVzY3JpcHRvclByb3RvLkVudW1SZXNlcnZlZFJhbmdlQhLiPw8SDXJlc2VydmVkUmFuZ
  2VSDXJlc2VydmVkUmFuZ2USNgoNcmVzZXJ2ZWRfbmFtZRgFIAMoCUIR4j8OEgxyZXNlcnZlZE5hbWVSDHJlc2VydmVkTmFtZRpRC
  hFFbnVtUmVzZXJ2ZWRSYW5nZRIgCgVzdGFydBgBIAEoBUIK4j8HEgVzdGFydFIFc3RhcnQSGgoDZW5kGAIgASgFQgjiPwUSA2VuZ
  FIDZW5kIqkBChhFbnVtVmFsdWVEZXNjcmlwdG9yUHJvdG8SHQoEbmFtZRgBIAEoCUIJ4j8GEgRuYW1lUgRuYW1lEiMKBm51bWJlc
  hgCIAEoBUIL4j8IEgZudW1iZXJSBm51bWJlchJJCgdvcHRpb25zGAMgASgLMiEuZ29vZ2xlLnByb3RvYnVmLkVudW1WYWx1ZU9wd
  GlvbnNCDOI/CRIHb3B0aW9uc1IHb3B0aW9ucyLNAQoWU2VydmljZURlc2NyaXB0b3JQcm90bxIdCgRuYW1lGAEgASgJQgniPwYSB
  G5hbWVSBG5hbWUSSwoGbWV0aG9kGAIgAygLMiYuZ29vZ2xlLnByb3RvYnVmLk1ldGhvZERlc2NyaXB0b3JQcm90b0IL4j8IEgZtZ
  XRob2RSBm1ldGhvZBJHCgdvcHRpb25zGAMgASgLMh8uZ29vZ2xlLnByb3RvYnVmLlNlcnZpY2VPcHRpb25zQgziPwkSB29wdGlvb
  nNSB29wdGlvbnMi7wIKFU1ldGhvZERlc2NyaXB0b3JQcm90bxIdCgRuYW1lGAEgASgJQgniPwYSBG5hbWVSBG5hbWUSLQoKaW5wd
  XRfdHlwZRgCIAEoCUIO4j8LEglpbnB1dFR5cGVSCWlucHV0VHlwZRIwCgtvdXRwdXRfdHlwZRgDIAEoCUIP4j8MEgpvdXRwdXRUe
  XBlUgpvdXRwdXRUeXBlEkYKB29wdGlvbnMYBCABKAsyHi5nb29nbGUucHJvdG9idWYuTWV0aG9kT3B0aW9uc0IM4j8JEgdvcHRpb
  25zUgdvcHRpb25zEkYKEGNsaWVudF9zdHJlYW1pbmcYBSABKAg6BWZhbHNlQhTiPxESD2NsaWVudFN0cmVhbWluZ1IPY2xpZW50U
  3RyZWFtaW5nEkYKEHNlcnZlcl9zdHJlYW1pbmcYBiABKAg6BWZhbHNlQhTiPxESD3NlcnZlclN0cmVhbWluZ1IPc2VydmVyU3RyZ
  WFtaW5nIo8NCgtGaWxlT3B0aW9ucxIzCgxqYXZhX3BhY2thZ2UYASABKAlCEOI/DRILamF2YVBhY2thZ2VSC2phdmFQYWNrYWdlE
  kkKFGphdmFfb3V0ZXJfY2xhc3NuYW1lGAggASgJQhfiPxQSEmphdmFPdXRlckNsYXNzbmFtZVISamF2YU91dGVyQ2xhc3NuYW1lE
  k0KE2phdmFfbXVsdGlwbGVfZmlsZXMYCiABKAg6BWZhbHNlQhbiPxMSEWphdmFNdWx0aXBsZUZpbGVzUhFqYXZhTXVsdGlwbGVGa
  WxlcxJiCh1qYXZhX2dlbmVyYXRlX2VxdWFsc19hbmRfaGFzaBgUIAEoCEIgGAHiPxsSGWphdmFHZW5lcmF0ZUVxdWFsc0FuZEhhc
  2hSGWphdmFHZW5lcmF0ZUVxdWFsc0FuZEhhc2gSVAoWamF2YV9zdHJpbmdfY2hlY2tfdXRmOBgbIAEoCDoFZmFsc2VCGOI/FRITa
  mF2YVN0cmluZ0NoZWNrVXRmOFITamF2YVN0cmluZ0NoZWNrVXRmOBJlCgxvcHRpbWl6ZV9mb3IYCSABKA4yKS5nb29nbGUucHJvd
  G9idWYuRmlsZU9wdGlvbnMuT3B0aW1pemVNb2RlOgVTUEVFREIQ4j8NEgtvcHRpbWl6ZUZvclILb3B0aW1pemVGb3ISLQoKZ29fc
  GFja2FnZRgLIAEoCUIO4j8LEglnb1BhY2thZ2VSCWdvUGFja2FnZRJNChNjY19nZW5lcmljX3NlcnZpY2VzGBAgASgIOgVmYWxzZ
  UIW4j8TEhFjY0dlbmVyaWNTZXJ2aWNlc1IRY2NHZW5lcmljU2VydmljZXMSUwoVamF2YV9nZW5lcmljX3NlcnZpY2VzGBEgASgIO
  gVmYWxzZUIY4j8VEhNqYXZhR2VuZXJpY1NlcnZpY2VzUhNqYXZhR2VuZXJpY1NlcnZpY2VzEk0KE3B5X2dlbmVyaWNfc2VydmljZ
  XMYEiABKAg6BWZhbHNlQhbiPxMSEXB5R2VuZXJpY1NlcnZpY2VzUhFweUdlbmVyaWNTZXJ2aWNlcxI2CgpkZXByZWNhdGVkGBcgA
  SgIOgVmYWxzZUIP4j8MEgpkZXByZWNhdGVkUgpkZXByZWNhdGVkEkMKEGNjX2VuYWJsZV9hcmVuYXMYHyABKAg6BHRydWVCE+I/E
  BIOY2NFbmFibGVBcmVuYXNSDmNjRW5hYmxlQXJlbmFzEkAKEW9iamNfY2xhc3NfcHJlZml4GCQgASgJQhTiPxESD29iamNDbGFzc
  1ByZWZpeFIPb2JqY0NsYXNzUHJlZml4Ej8KEGNzaGFycF9uYW1lc3BhY2UYJSABKAlCFOI/ERIPY3NoYXJwTmFtZXNwYWNlUg9jc
  2hhcnBOYW1lc3BhY2USMwoMc3dpZnRfcHJlZml4GCcgASgJQhDiPw0SC3N3aWZ0UHJlZml4Ugtzd2lmdFByZWZpeBI9ChBwaHBfY
  2xhc3NfcHJlZml4GCggASgJQhPiPxASDnBocENsYXNzUHJlZml4Ug5waHBDbGFzc1ByZWZpeBI2Cg1waHBfbmFtZXNwYWNlGCkgA
  SgJQhHiPw4SDHBocE5hbWVzcGFjZVIMcGhwTmFtZXNwYWNlEk8KFnBocF9tZXRhZGF0YV9uYW1lc3BhY2UYLCABKAlCGeI/FhIUc
  GhwTWV0YWRhdGFOYW1lc3BhY2VSFHBocE1ldGFkYXRhTmFtZXNwYWNlEjMKDHJ1YnlfcGFja2FnZRgtIAEoCUIQ4j8NEgtydWJ5U
  GFja2FnZVILcnVieVBhY2thZ2USRgoIZmVhdHVyZXMYMiABKAsyGy5nb29nbGUucHJvdG9idWYuRmVhdHVyZVNldEIN4j8KEghmZ
  WF0dXJlc1IIZmVhdHVyZXMScgoUdW5pbnRlcnByZXRlZF9vcHRpb24Y5wcgAygLMiQuZ29vZ2xlLnByb3RvYnVmLlVuaW50ZXJwc
  mV0ZWRPcHRpb25CGOI/FRITdW5pbnRlcnByZXRlZE9wdGlvblITdW5pbnRlcnByZXRlZE9wdGlvbiJpCgxPcHRpbWl6ZU1vZGUSF
  QoFU1BFRUQQARoK4j8HEgVTUEVFRBIdCglDT0RFX1NJWkUQAhoO4j8LEglDT0RFX1NJWkUSIwoMTElURV9SVU5USU1FEAMaEeI/D
  hIMTElURV9SVU5USU1FKgkI6AcQgICAgAJKBAgqECtKBAgmECciogUKDk1lc3NhZ2VPcHRpb25zElcKF21lc3NhZ2Vfc2V0X3dpc
  mVfZm9ybWF0GAEgASgIOgVmYWxzZUIZ4j8WEhRtZXNzYWdlU2V0V2lyZUZvcm1hdFIUbWVzc2FnZVNldFdpcmVGb3JtYXQSbwofb
  m9fc3RhbmRhcmRfZGVzY3JpcHRvcl9hY2Nlc3NvchgCIAEoCDoFZmFsc2VCIeI/HhIcbm9TdGFuZGFyZERlc2NyaXB0b3JBY2Nlc
  3NvclIcbm9TdGFuZGFyZERlc2NyaXB0b3JBY2Nlc3NvchI2CgpkZXByZWNhdGVkGAMgASgIOgVmYWxzZUIP4j8MEgpkZXByZWNhd
  GVkUgpkZXByZWNhdGVkEioKCW1hcF9lbnRyeRgHIAEoCEIN4j8KEghtYXBFbnRyeVIIbWFwRW50cnkSfQomZGVwcmVjYXRlZF9sZ
  WdhY3lfanNvbl9maWVsZF9jb25mbGljdHMYCyABKAhCKRgB4j8kEiJkZXByZWNhdGVkTGVnYWN5SnNvbkZpZWxkQ29uZmxpY3RzU
  iJkZXByZWNhdGVkTGVnYWN5SnNvbkZpZWxkQ29uZmxpY3RzEkYKCGZlYXR1cmVzGAwgASgLMhsuZ29vZ2xlLnByb3RvYnVmLkZlY
  XR1cmVTZXRCDeI/ChIIZmVhdHVyZXNSCGZlYXR1cmVzEnIKFHVuaW50ZXJwcmV0ZWRfb3B0aW9uGOcHIAMoCzIkLmdvb2dsZS5wc
  m90b2J1Zi5VbmludGVycHJldGVkT3B0aW9uQhjiPxUSE3VuaW50ZXJwcmV0ZWRPcHRpb25SE3VuaW50ZXJwcmV0ZWRPcHRpb24qC
  QjoBxCAgICAAkoECAQQBUoECAUQBkoECAYQB0oECAgQCUoECAkQCiK/DwoMRmllbGRPcHRpb25zEk0KBWN0eXBlGAEgASgOMiMuZ
  29vZ2xlLnByb3RvYnVmLkZpZWxkT3B0aW9ucy5DVHlwZToGU1RSSU5HQgriPwcSBWN0eXBlUgVjdHlwZRIjCgZwYWNrZWQYAiABK
  AhCC+I/CBIGcGFja2VkUgZwYWNrZWQSVAoGanN0eXBlGAYgASgOMiQuZ29vZ2xlLnByb3RvYnVmLkZpZWxkT3B0aW9ucy5KU1R5c
  GU6CUpTX05PUk1BTEIL4j8IEgZqc3R5cGVSBmpzdHlwZRIkCgRsYXp5GAUgASgIOgVmYWxzZUIJ4j8GEgRsYXp5UgRsYXp5EkMKD
  3VudmVyaWZpZWRfbGF6eRgPIAEoCDoFZmFsc2VCE+I/EBIOdW52ZXJpZmllZExhenlSDnVudmVyaWZpZWRMYXp5EjYKCmRlcHJlY
  2F0ZWQYAyABKAg6BWZhbHNlQg/iPwwSCmRlcHJlY2F0ZWRSCmRlcHJlY2F0ZWQSJAoEd2VhaxgKIAEoCDoFZmFsc2VCCeI/BhIEd
  2Vha1IEd2VhaxI6CgxkZWJ1Z19yZWRhY3QYECABKAg6BWZhbHNlQhDiPw0SC2RlYnVnUmVkYWN0UgtkZWJ1Z1JlZGFjdBJbCglyZ
  XRlbnRpb24YESABKA4yLS5nb29nbGUucHJvdG9idWYuRmllbGRPcHRpb25zLk9wdGlvblJldGVudGlvbkIO4j8LEglyZXRlbnRpb
  25SCXJldGVudGlvbhJWCgd0YXJnZXRzGBMgAygOMi4uZ29vZ2xlLnByb3RvYnVmLkZpZWxkT3B0aW9ucy5PcHRpb25UYXJnZXRUe
  XBlQgziPwkSB3RhcmdldHNSB3RhcmdldHMSbQoQZWRpdGlvbl9kZWZhdWx0cxgUIAMoCzIsLmdvb2dsZS5wcm90b2J1Zi5GaWVsZ
  E9wdGlvbnMuRWRpdGlvbkRlZmF1bHRCFOI/ERIPZWRpdGlvbkRlZmF1bHRzUg9lZGl0aW9uRGVmYXVsdHMSRgoIZmVhdHVyZXMYF
  SABKAsyGy5nb29nbGUucHJvdG9idWYuRmVhdHVyZVNldEIN4j8KEghmZWF0dXJlc1IIZmVhdHVyZXMScgoUdW5pbnRlcnByZXRlZ
  F9vcHRpb24Y5wcgAygLMiQuZ29vZ2xlLnByb3RvYnVmLlVuaW50ZXJwcmV0ZWRPcHRpb25CGOI/FRITdW5pbnRlcnByZXRlZE9wd
  GlvblITdW5pbnRlcnByZXRlZE9wdGlvbhp0Cg5FZGl0aW9uRGVmYXVsdBJACgdlZGl0aW9uGAMgASgOMhguZ29vZ2xlLnByb3RvY
  nVmLkVkaXRpb25CDOI/CRIHZWRpdGlvblIHZWRpdGlvbhIgCgV2YWx1ZRgCIAEoCUIK4j8HEgV2YWx1ZVIFdmFsdWUiWgoFQ1R5c
  GUSFwoGU1RSSU5HEAAaC+I/CBIGU1RSSU5HEhMKBENPUkQQARoJ4j8GEgRDT1JEEiMKDFNUUklOR19QSUVDRRACGhHiPw4SDFNUU
  klOR19QSUVDRSJlCgZKU1R5cGUSHQoJSlNfTk9STUFMEAAaDuI/CxIJSlNfTk9STUFMEh0KCUpTX1NUUklORxABGg7iPwsSCUpTX
  1NUUklORxIdCglKU19OVU1CRVIQAhoO4j8LEglKU19OVU1CRVIinAEKD09wdGlvblJldGVudGlvbhItChFSRVRFTlRJT05fVU5LT
  k9XThAAGhbiPxMSEVJFVEVOVElPTl9VTktOT1dOEi0KEVJFVEVOVElPTl9SVU5USU1FEAEaFuI/ExIRUkVURU5USU9OX1JVTlRJT
  UUSKwoQUkVURU5USU9OX1NPVVJDRRACGhXiPxISEFJFVEVOVElPTl9TT1VSQ0UikAQKEE9wdGlvblRhcmdldFR5cGUSMQoTVEFSR
  0VUX1RZUEVfVU5LTk9XThAAGhjiPxUSE1RBUkdFVF9UWVBFX1VOS05PV04SKwoQVEFSR0VUX1RZUEVfRklMRRABGhXiPxISEFRBU
  kdFVF9UWVBFX0ZJTEUSQQobVEFSR0VUX1RZUEVfRVhURU5TSU9OX1JBTkdFEAIaIOI/HRIbVEFSR0VUX1RZUEVfRVhURU5TSU9OX
  1JBTkdFEjEKE1RBUkdFVF9UWVBFX01FU1NBR0UQAxoY4j8VEhNUQVJHRVRfVFlQRV9NRVNTQUdFEi0KEVRBUkdFVF9UWVBFX0ZJR
  UxEEAQaFuI/ExIRVEFSR0VUX1RZUEVfRklFTEQSLQoRVEFSR0VUX1RZUEVfT05FT0YQBRoW4j8TEhFUQVJHRVRfVFlQRV9PTkVPR
  hIrChBUQVJHRVRfVFlQRV9FTlVNEAYaFeI/EhIQVEFSR0VUX1RZUEVfRU5VTRI3ChZUQVJHRVRfVFlQRV9FTlVNX0VOVFJZEAcaG
  +I/GBIWVEFSR0VUX1RZUEVfRU5VTV9FTlRSWRIxChNUQVJHRVRfVFlQRV9TRVJWSUNFEAgaGOI/FRITVEFSR0VUX1RZUEVfU0VSV
  klDRRIvChJUQVJHRVRfVFlQRV9NRVRIT0QQCRoX4j8UEhJUQVJHRVRfVFlQRV9NRVRIT0QqCQjoBxCAgICAAkoECAQQBUoECBIQE
  yLVAQoMT25lb2ZPcHRpb25zEkYKCGZlYXR1cmVzGAEgASgLMhsuZ29vZ2xlLnByb3RvYnVmLkZlYXR1cmVTZXRCDeI/ChIIZmVhd
  HVyZXNSCGZlYXR1cmVzEnIKFHVuaW50ZXJwcmV0ZWRfb3B0aW9uGOcHIAMoCzIkLmdvb2dsZS5wcm90b2J1Zi5VbmludGVycHJld
  GVkT3B0aW9uQhjiPxUSE3VuaW50ZXJwcmV0ZWRPcHRpb25SE3VuaW50ZXJwcmV0ZWRPcHRpb24qCQjoBxCAgICAAiLDAwoLRW51b
  U9wdGlvbnMSMAoLYWxsb3dfYWxpYXMYAiABKAhCD+I/DBIKYWxsb3dBbGlhc1IKYWxsb3dBbGlhcxI2CgpkZXByZWNhdGVkGAMgA
  SgIOgVmYWxzZUIP4j8MEgpkZXByZWNhdGVkUgpkZXByZWNhdGVkEn0KJmRlcHJlY2F0ZWRfbGVnYWN5X2pzb25fZmllbGRfY29uZ
  mxpY3RzGAYgASgIQikYAeI/JBIiZGVwcmVjYXRlZExlZ2FjeUpzb25GaWVsZENvbmZsaWN0c1IiZGVwcmVjYXRlZExlZ2FjeUpzb
  25GaWVsZENvbmZsaWN0cxJGCghmZWF0dXJlcxgHIAEoCzIbLmdvb2dsZS5wcm90b2J1Zi5GZWF0dXJlU2V0Qg3iPwoSCGZlYXR1c
  mVzUghmZWF0dXJlcxJyChR1bmludGVycHJldGVkX29wdGlvbhjnByADKAsyJC5nb29nbGUucHJvdG9idWYuVW5pbnRlcnByZXRlZ
  E9wdGlvbkIY4j8VEhN1bmludGVycHJldGVkT3B0aW9uUhN1bmludGVycHJldGVkT3B0aW9uKgkI6AcQgICAgAJKBAgFEAYizQIKE
  EVudW1WYWx1ZU9wdGlvbnMSNgoKZGVwcmVjYXRlZBgBIAEoCDoFZmFsc2VCD+I/DBIKZGVwcmVjYXRlZFIKZGVwcmVjYXRlZBJGC
  ghmZWF0dXJlcxgCIAEoCzIbLmdvb2dsZS5wcm90b2J1Zi5GZWF0dXJlU2V0Qg3iPwoSCGZlYXR1cmVzUghmZWF0dXJlcxI6CgxkZ
  WJ1Z19yZWRhY3QYAyABKAg6BWZhbHNlQhDiPw0SC2RlYnVnUmVkYWN0UgtkZWJ1Z1JlZGFjdBJyChR1bmludGVycHJldGVkX29wd
  GlvbhjnByADKAsyJC5nb29nbGUucHJvdG9idWYuVW5pbnRlcnByZXRlZE9wdGlvbkIY4j8VEhN1bmludGVycHJldGVkT3B0aW9uU
  hN1bmludGVycHJldGVkT3B0aW9uKgkI6AcQgICAgAIijwIKDlNlcnZpY2VPcHRpb25zEkYKCGZlYXR1cmVzGCIgASgLMhsuZ29vZ
  2xlLnByb3RvYnVmLkZlYXR1cmVTZXRCDeI/ChIIZmVhdHVyZXNSCGZlYXR1cmVzEjYKCmRlcHJlY2F0ZWQYISABKAg6BWZhbHNlQ
  g/iPwwSCmRlcHJlY2F0ZWRSCmRlcHJlY2F0ZWQScgoUdW5pbnRlcnByZXRlZF9vcHRpb24Y5wcgAygLMiQuZ29vZ2xlLnByb3RvY
  nVmLlVuaW50ZXJwcmV0ZWRPcHRpb25CGOI/FRITdW5pbnRlcnByZXRlZE9wdGlvblITdW5pbnRlcnByZXRlZE9wdGlvbioJCOgHE
  ICAgIACIq0ECg1NZXRob2RPcHRpb25zEjYKCmRlcHJlY2F0ZWQYISABKAg6BWZhbHNlQg/iPwwSCmRlcHJlY2F0ZWRSCmRlcHJlY
  2F0ZWQSiAEKEWlkZW1wb3RlbmN5X2xldmVsGCIgASgOMi8uZ29vZ2xlLnByb3RvYnVmLk1ldGhvZE9wdGlvbnMuSWRlbXBvdGVuY
  3lMZXZlbDoTSURFTVBPVEVOQ1lfVU5LTk9XTkIV4j8SEhBpZGVtcG90ZW5jeUxldmVsUhBpZGVtcG90ZW5jeUxldmVsEkYKCGZlY
  XR1cmVzGCMgASgLMhsuZ29vZ2xlLnByb3RvYnVmLkZlYXR1cmVTZXRCDeI/ChIIZmVhdHVyZXNSCGZlYXR1cmVzEnIKFHVuaW50Z
  XJwcmV0ZWRfb3B0aW9uGOcHIAMoCzIkLmdvb2dsZS5wcm90b2J1Zi5VbmludGVycHJldGVkT3B0aW9uQhjiPxUSE3VuaW50ZXJwc
  mV0ZWRPcHRpb25SE3VuaW50ZXJwcmV0ZWRPcHRpb24ikQEKEElkZW1wb3RlbmN5TGV2ZWwSMQoTSURFTVBPVEVOQ1lfVU5LTk9XT
  hAAGhjiPxUSE0lERU1QT1RFTkNZX1VOS05PV04SKQoPTk9fU0lERV9FRkZFQ1RTEAEaFOI/ERIPTk9fU0lERV9FRkZFQ1RTEh8KC
  klERU1QT1RFTlQQAhoP4j8MEgpJREVNUE9URU5UKgkI6AcQgICAgAIiwwQKE1VuaW50ZXJwcmV0ZWRPcHRpb24STAoEbmFtZRgCI
  AMoCzItLmdvb2dsZS5wcm90b2J1Zi5VbmludGVycHJldGVkT3B0aW9uLk5hbWVQYXJ0QgniPwYSBG5hbWVSBG5hbWUSPwoQaWRlb
  nRpZmllcl92YWx1ZRgDIAEoCUIU4j8REg9pZGVudGlmaWVyVmFsdWVSD2lkZW50aWZpZXJWYWx1ZRJDChJwb3NpdGl2ZV9pbnRfd
  mFsdWUYBCABKARCFeI/EhIQcG9zaXRpdmVJbnRWYWx1ZVIQcG9zaXRpdmVJbnRWYWx1ZRJDChJuZWdhdGl2ZV9pbnRfdmFsdWUYB
  SABKANCFeI/EhIQbmVnYXRpdmVJbnRWYWx1ZVIQbmVnYXRpdmVJbnRWYWx1ZRIzCgxkb3VibGVfdmFsdWUYBiABKAFCEOI/DRILZ
  G91YmxlVmFsdWVSC2RvdWJsZVZhbHVlEjMKDHN0cmluZ192YWx1ZRgHIAEoDEIQ4j8NEgtzdHJpbmdWYWx1ZVILc3RyaW5nVmFsd
  WUSPAoPYWdncmVnYXRlX3ZhbHVlGAggASgJQhPiPxASDmFnZ3JlZ2F0ZVZhbHVlUg5hZ2dyZWdhdGVWYWx1ZRprCghOYW1lUGFyd
  BIqCgluYW1lX3BhcnQYASACKAlCDeI/ChIIbmFtZVBhcnRSCG5hbWVQYXJ0EjMKDGlzX2V4dGVuc2lvbhgCIAIoCEIQ4j8NEgtpc
  0V4dGVuc2lvblILaXNFeHRlbnNpb24i/w0KCkZlYXR1cmVTZXQSnQEKDmZpZWxkX3ByZXNlbmNlGAEgASgOMikuZ29vZ2xlLnByb
  3RvYnVmLkZlYXR1cmVTZXQuRmllbGRQcmVzZW5jZUJLiAEBmAEEmAEBogENEghFWFBMSUNJVBjmB6IBDRIISU1QTElDSVQY5weiA
  Q0SCEVYUExJQ0lUGOgH4j8PEg1maWVsZFByZXNlbmNlUg1maWVsZFByZXNlbmNlEnMKCWVudW1fdHlwZRgCIAEoDjIkLmdvb2dsZ
  S5wcm90b2J1Zi5GZWF0dXJlU2V0LkVudW1UeXBlQjCIAQGYAQaYAQGiAQsSBkNMT1NFRBjmB6IBCRIET1BFThjnB+I/ChIIZW51b
  VR5cGVSCGVudW1UeXBlEqwBChdyZXBlYXRlZF9maWVsZF9lbmNvZGluZxgDIAEoDjIxLmdvb2dsZS5wcm90b2J1Zi5GZWF0dXJlU
  2V0LlJlcGVhdGVkRmllbGRFbmNvZGluZ0JBiAEBmAEEmAEBogENEghFWFBBTkRFRBjmB6IBCxIGUEFDS0VEGOcH4j8XEhVyZXBlY
  XRlZEZpZWxkRW5jb2RpbmdSFXJlcGVhdGVkRmllbGRFbmNvZGluZxKLAQoPdXRmOF92YWxpZGF0aW9uGAQgASgOMiouZ29vZ2xlL
  nByb3RvYnVmLkZlYXR1cmVTZXQuVXRmOFZhbGlkYXRpb25CNogBAZgBBJgBAaIBCRIETk9ORRjmB6IBCxIGVkVSSUZZGOcH4j8QE
  g51dGY4VmFsaWRhdGlvblIOdXRmOFZhbGlkYXRpb24SjAEKEG1lc3NhZ2VfZW5jb2RpbmcYBSABKA4yKy5nb29nbGUucHJvdG9id
  WYuRmVhdHVyZVNldC5NZXNzYWdlRW5jb2RpbmdCNIgBAZgBBJgBAaIBFBIPTEVOR1RIX1BSRUZJWEVEGOYH4j8REg9tZXNzYWdlR
  W5jb2RpbmdSD21lc3NhZ2VFbmNvZGluZxKLAQoLanNvbl9mb3JtYXQYBiABKA4yJi5nb29nbGUucHJvdG9idWYuRmVhdHVyZVNld
  C5Kc29uRm9ybWF0QkKIAQGYAQOYAQaYAQGiARcSEkxFR0FDWV9CRVNUX0VGRk9SVBjmB6IBChIFQUxMT1cY5wfiPwwSCmpzb25Gb
  3JtYXRSCmpzb25Gb3JtYXQirQEKDUZpZWxkUHJlc2VuY2USNwoWRklFTERfUFJFU0VOQ0VfVU5LTk9XThAAGhviPxgSFkZJRUxEX
  1BSRVNFTkNFX1VOS05PV04SGwoIRVhQTElDSVQQARoN4j8KEghFWFBMSUNJVBIbCghJTVBMSUNJVBACGg3iPwoSCElNUExJQ0lUE
  ikKD0xFR0FDWV9SRVFVSVJFRBADGhTiPxESD0xFR0FDWV9SRVFVSVJFRCJnCghFbnVtVHlwZRItChFFTlVNX1RZUEVfVU5LTk9XT
  hAAGhbiPxMSEUVOVU1fVFlQRV9VTktOT1dOEhMKBE9QRU4QARoJ4j8GEgRPUEVOEhcKBkNMT1NFRBACGgviPwgSBkNMT1NFRCKYA
  QoVUmVwZWF0ZWRGaWVsZEVuY29kaW5nEkkKH1JFUEVBVEVEX0ZJRUxEX0VOQ09ESU5HX1VOS05PV04QABok4j8hEh9SRVBFQVRFR
  F9GSUVMRF9FTkNPRElOR19VTktOT1dOEhcKBlBBQ0tFRBABGgviPwgSBlBBQ0tFRBIbCghFWFBBTkRFRBACGg3iPwoSCEVYUEFOR
  EVEInkKDlV0ZjhWYWxpZGF0aW9uEjkKF1VURjhfVkFMSURBVElPTl9VTktOT1dOEAAaHOI/GRIXVVRGOF9WQUxJREFUSU9OX1VOS
  05PV04SFwoGVkVSSUZZEAIaC+I/CBIGVkVSSUZZEhMKBE5PTkUQAxoJ4j8GEgROT05FIpgBCg9NZXNzYWdlRW5jb2RpbmcSOwoYT
  UVTU0FHRV9FTkNPRElOR19VTktOT1dOEAAaHeI/GhIYTUVTU0FHRV9FTkNPRElOR19VTktOT1dOEikKD0xFTkdUSF9QUkVGSVhFR
  BABGhTiPxESD0xFTkdUSF9QUkVGSVhFRBIdCglERUxJTUlURUQQAhoO4j8LEglERUxJTUlURUQihwEKCkpzb25Gb3JtYXQSMQoTS
  lNPTl9GT1JNQVRfVU5LTk9XThAAGhjiPxUSE0pTT05fRk9STUFUX1VOS05PV04SFQoFQUxMT1cQARoK4j8HEgVBTExPVxIvChJMR
  UdBQ1lfQkVTVF9FRkZPUlQQAhoX4j8UEhJMRUdBQ1lfQkVTVF9FRkZPUlQqBgjoBxDpByoGCOkHEOoHKgYI6gcQ6wcqBgiLThCQT
  ioGCJBOEJFOSgYI5wcQ6Aci1AMKEkZlYXR1cmVTZXREZWZhdWx0cxJnCghkZWZhdWx0cxgBIAMoCzI8Lmdvb2dsZS5wcm90b2J1Z
  i5GZWF0dXJlU2V0RGVmYXVsdHMuRmVhdHVyZVNldEVkaXRpb25EZWZhdWx0Qg3iPwoSCGRlZmF1bHRzUghkZWZhdWx0cxJWCg9ta
  W5pbXVtX2VkaXRpb24YBCABKA4yGC5nb29nbGUucHJvdG9idWYuRWRpdGlvbkIT4j8QEg5taW5pbXVtRWRpdGlvblIObWluaW11b
  UVkaXRpb24SVgoPbWF4aW11bV9lZGl0aW9uGAUgASgOMhguZ29vZ2xlLnByb3RvYnVmLkVkaXRpb25CE+I/EBIObWF4aW11bUVka
  XRpb25SDm1heGltdW1FZGl0aW9uGqQBChhGZWF0dXJlU2V0RWRpdGlvbkRlZmF1bHQSQAoHZWRpdGlvbhgDIAEoDjIYLmdvb2dsZ
  S5wcm90b2J1Zi5FZGl0aW9uQgziPwkSB2VkaXRpb25SB2VkaXRpb24SRgoIZmVhdHVyZXMYAiABKAsyGy5nb29nbGUucHJvdG9id
  WYuRmVhdHVyZVNldEIN4j8KEghmZWF0dXJlc1IIZmVhdHVyZXMikwMKDlNvdXJjZUNvZGVJbmZvElMKCGxvY2F0aW9uGAEgAygLM
  iguZ29vZ2xlLnByb3RvYnVmLlNvdXJjZUNvZGVJbmZvLkxvY2F0aW9uQg3iPwoSCGxvY2F0aW9uUghsb2NhdGlvbhqrAgoITG9jY
  XRpb24SHwoEcGF0aBgBIAMoBUILEAHiPwYSBHBhdGhSBHBhdGgSHwoEc3BhbhgCIAMoBUILEAHiPwYSBHNwYW5SBHNwYW4SPwoQb
  GVhZGluZ19jb21tZW50cxgDIAEoCUIU4j8REg9sZWFkaW5nQ29tbWVudHNSD2xlYWRpbmdDb21tZW50cxJCChF0cmFpbGluZ19jb
  21tZW50cxgEIAEoCUIV4j8SEhB0cmFpbGluZ0NvbW1lbnRzUhB0cmFpbGluZ0NvbW1lbnRzElgKGWxlYWRpbmdfZGV0YWNoZWRfY
  29tbWVudHMYBiADKAlCHOI/GRIXbGVhZGluZ0RldGFjaGVkQ29tbWVudHNSF2xlYWRpbmdEZXRhY2hlZENvbW1lbnRzIsEDChFHZ
  W5lcmF0ZWRDb2RlSW5mbxJeCgphbm5vdGF0aW9uGAEgAygLMi0uZ29vZ2xlLnByb3RvYnVmLkdlbmVyYXRlZENvZGVJbmZvLkFub
  m90YXRpb25CD+I/DBIKYW5ub3RhdGlvblIKYW5ub3RhdGlvbhrLAgoKQW5ub3RhdGlvbhIfCgRwYXRoGAEgAygFQgsQAeI/BhIEc
  GF0aFIEcGF0aBIwCgtzb3VyY2VfZmlsZRgCIAEoCUIP4j8MEgpzb3VyY2VGaWxlUgpzb3VyY2VGaWxlEiAKBWJlZ2luGAMgASgFQ
  griPwcSBWJlZ2luUgViZWdpbhIaCgNlbmQYBCABKAVCCOI/BRIDZW5kUgNlbmQSYQoIc2VtYW50aWMYBSABKA4yNi5nb29nbGUuc
  HJvdG9idWYuR2VuZXJhdGVkQ29kZUluZm8uQW5ub3RhdGlvbi5TZW1hbnRpY0IN4j8KEghzZW1hbnRpY1IIc2VtYW50aWMiSQoIU
  2VtYW50aWMSEwoETk9ORRAAGgniPwYSBE5PTkUSEQoDU0VUEAEaCOI/BRIDU0VUEhUKBUFMSUFTEAIaCuI/BxIFQUxJQVMqmAQKB
  0VkaXRpb24SKQoPRURJVElPTl9VTktOT1dOEAAaFOI/ERIPRURJVElPTl9VTktOT1dOEigKDkVESVRJT05fUFJPVE8yEOYHGhPiP
  xASDkVESVRJT05fUFJPVE8yEigKDkVESVRJT05fUFJPVE8zEOcHGhPiPxASDkVESVRJT05fUFJPVE8zEiQKDEVESVRJT05fMjAyM
  xDoBxoR4j8OEgxFRElUSU9OXzIwMjMSJAoMRURJVElPTl8yMDI0EOkHGhHiPw4SDEVESVRJT05fMjAyNBIxChNFRElUSU9OXzFfV
  EVTVF9PTkxZEAEaGOI/FRITRURJVElPTl8xX1RFU1RfT05MWRIxChNFRElUSU9OXzJfVEVTVF9PTkxZEAIaGOI/FRITRURJVElPT
  l8yX1RFU1RfT05MWRI7ChdFRElUSU9OXzk5OTk3X1RFU1RfT05MWRCdjQYaHOI/GRIXRURJVElPTl85OTk5N19URVNUX09OTFkSO
  woXRURJVElPTl85OTk5OF9URVNUX09OTFkQno0GGhziPxkSF0VESVRJT05fOTk5OThfVEVTVF9PTkxZEjsKF0VESVRJT05fOTk5O
  TlfVEVTVF9PTkxZEJ+NBhoc4j8ZEhdFRElUSU9OXzk5OTk5X1RFU1RfT05MWRIlCgtFRElUSU9OX01BWBD/////BxoQ4j8NEgtFR
  ElUSU9OX01BWEJ+ChNjb20uZ29vZ2xlLnByb3RvYnVmQhBEZXNjcmlwdG9yUHJvdG9zSAFaLWdvb2dsZS5nb2xhbmcub3JnL3Byb
  3RvYnVmL3R5cGVzL2Rlc2NyaXB0b3JwYvgBAaICA0dQQqoCGkdvb2dsZS5Qcm90b2J1Zi5SZWZsZWN0aW9u"""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor =
    com.google.protobuf.DescriptorProtos.getDescriptor()
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}