package com.anton.cmdlineproc.sample;

import java.util.Date;

import com.anton.cmdlineproc.annotations.Option;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Params {
	
	public enum Operation {
		plus, minus, mul, div
	}
	
	@Option(shortName = "l", longName = "left", defaultValue="0", required=true, description="Left operand", possibleValues="any int")
	Integer left;
	
	@Option(shortName = "r", longName = "right", defaultValue="1", required=true, description="Right operand", possibleValues="any int")
	Integer right;
	
	@Option(shortName = "o", longName = "operation", defaultValue="plus", required=true, description="The operation", possibleValues="any of [plus, minus, mul, div]")
	Operation operation;

}
