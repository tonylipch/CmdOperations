package com.anton.simplecalc;

import com.anton.cmdlineproc.annotations.Option;

import lombok.Data;

@Data
public class Params {
	
	public enum Operation {
		plus, minus, mul, div
	}
	
	@Option(shortName = "l", longName = "left", required=true, description="Left operand", possibleValues="any int")
	Integer left;
	
	@Option(shortName = "r", longName = "right", required=true, description="Right operand", possibleValues="any int")
	Integer right;
	
	@Option(shortName = "o", longName = "operation", required=true, description="The operation", possibleValues="any of [plus, minus, mul, div]")
	Operation operation;

}
