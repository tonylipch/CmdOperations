package com.anton.cmdlineproc.engine;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class OptionMeta {

	private String shortName;
	
	private String longName;
	
	private String defaultValue;

	private boolean required;
	
	private String description;
	
	private String possibleValues;

	private String fieldName;
	
	private Class<?> fieldType;
	
}
