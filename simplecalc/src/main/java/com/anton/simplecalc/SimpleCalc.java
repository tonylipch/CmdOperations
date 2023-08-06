package com.anton.simplecalc;

import com.anton.cmdlineproc.engine.CmdLineEngine;

public class SimpleCalc {



	public static void main(String[] args) {
		Params params = new Params();
		CmdLineEngine engine = new CmdLineEngine(params, "java -jar simplecalc.jar");
		if (!engine.parseCommandLine(args)) {
			engine.getOutput().stream().forEach(System.out::println);
			return;
		}

		if (args.length == 0) {
			engine.provideHelp();
			return;
		}

		System.out.println(params.getLeft()+" "+params.getOperation()+" "+params.getRight());

	}

	public static int calc (Params params){

		int left = params.left;
		int right = params.right;

		Params.Operation operation = params.getOperation();

		switch (operation){

			case plus  :
				return left+right;

			case minus :
				return left - right;

			case mul :
				return  left*right;

			case div :
				if (right ==0){
					throw new IllegalArgumentException("Division by zero is not allowed.");
				}
				return  left/right;

			default:
				throw new IllegalArgumentException("Unknown operation: " + operation);
		}
	}
}
