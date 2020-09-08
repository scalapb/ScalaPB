package com.thesamet.pb

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{beanGetter, beanSetter, field, getter, setter}

class CustomAnnotation() extends StaticAnnotation

class CustomAnnotation1() extends StaticAnnotation

class CustomAnnotation2() extends StaticAnnotation

@getter @setter @beanGetter @beanSetter @field
final class CustomFieldAnnotation1 extends StaticAnnotation

@getter @setter @beanGetter @beanSetter @field
final class CustomFieldAnnotation2 extends StaticAnnotation
