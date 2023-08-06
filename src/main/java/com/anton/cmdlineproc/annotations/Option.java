package com.anton.cmdlineproc.annotations;

import java.lang.annotation.Target;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
  public static String THENULLVALUE="<<THENULLVALUE>>";
	
  String defaultValue() default THENULLVALUE;

  String description() default "";


  String longName() default "";

  String shortName();
  
  String possibleValues() default "";

  boolean required() default false;
}
