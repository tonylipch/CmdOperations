package com.anton.cmdlineproc.engine;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anton.cmdlineproc.annotations.Option;

import lombok.Getter;
public class CmdLineEngine {
	private List<OptionMeta>  fieldsMeta;
	private Object paramsBean;
	private String baseCommand;
	private Map<String, PropertyDescriptor> descriptors = new HashMap<>();
	private Map<String, OptionMeta> metas = new HashMap<>();
	private Map<String, OptionMeta> metasByFieldName = new HashMap<>();
	
	@Getter
	private List<String> output = new ArrayList<> ();
	public CmdLineEngine(Object paramsBean, String baseCommand) {
		try {
			var beanInfo = Introspector.getBeanInfo(paramsBean.getClass());
			var propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : propertyDescriptors) {
				if ("class".equals(descriptor.getName())) {
					continue;
				}
				descriptors.put(descriptor.getName(), descriptor);
			}
		} catch (IntrospectionException e) {
			throw new ParseException("Can't prepare parsing command line to bean of "+paramsBean.getClass(), e);
		}


		this.paramsBean = paramsBean;
		this.baseCommand = baseCommand;
		this.fieldsMeta = getFieldsMeta(paramsBean.getClass());

		if (this.fieldsMeta.isEmpty()) {
			throw new ParseException("Couldn't get parameters declaration in "+paramsBean.getClass()+". Use "+Option.class.getCanonicalName()+" for declare parameters.");
		}

		if (descriptors.size() < this.fieldsMeta.size() ) {
			List<String> wrongFields = new ArrayList<>();
			for (OptionMeta m : this.fieldsMeta) {
				if (!descriptors.containsKey(m.getFieldName())) {
					wrongFields.add(m.getFieldName());
				}
			}
			throw new ParseException("Couldn't get property descriptors for fields of "+ wrongFields + " of bean "+paramsBean.getClass()+". Make sure that you provided necessary getters and setters");
		}

		for (OptionMeta m : fieldsMeta) {
			checkUniquiness(m.getShortName(), m.getFieldName());
			checkUniquiness(m.getLongName(), m.getFieldName());
			metas.put(m.getShortName(), m);
			metas.put(m.getLongName(), m);
			metasByFieldName.put(m.getFieldName(), m);
			updateParam(m, m.getDefaultValue());
		}
	}

	//check if the given option name (short or long) is unique among the already registered options.
	private void checkUniquiness(String name, String fieldName) {
		if (metas.containsKey(name)) {
			var wrong = metas.get(name);
			throw new ParseException("Dublicate declaration of parameter '"+name+"', see fields "+wrong.getFieldName() + ", " + fieldName);
		}
	}

	//method for retrieve a list of 'OptionMeta' objects representing the options defined as annotated fields in the given class.
	static List<OptionMeta>  getFieldsMeta(Class<?> clazz) {
		List<OptionMeta>  fieldsMeta = new ArrayList<>();
		for (Field f : clazz.getDeclaredFields()) {
			var option = f.getAnnotation(Option.class);
			if (option == null) continue;
			var meta = OptionMeta.builder()
				.fieldType(f.getType())
				.fieldName(f.getName())
				.defaultValue(option.defaultValue())
				.shortName("-"+option.shortName())
				.longName("--"+option.longName())
				.description(option.description())
				.possibleValues(option.possibleValues())
				.required(option.required())
				.build();
			fieldsMeta.add(meta);
		}
		return fieldsMeta;
	}
	
	public List<String> provideHelp() {
		output.add("Use:\n" + baseCommand + "[<option1> <option1-value>, <option2> <option2-value>, ...], \npossible options are:\n");
		for (OptionMeta om : fieldsMeta) {
			printOption(om);
		}
		return output;
	}

	//Returns a list of strings that can be used to display the help information to the user.
	private void printOption(OptionMeta om) {
		output.add(om.getShortName() + "( " + om.getLongName() + " )  "
				+ (om.isRequired() ? "mandatory" : "optional") + " " + om.getDescription() + " "
				+ (om.getPossibleValues().isEmpty() ? "" : " sample: " + om.getPossibleValues()));
	}

	//method for parsing a string value and convert it to the appropriate target data type based on the provided targetType.
	static Object parse(Class<?> targetType, String valueS) throws ParseException {
		
		if (Option.THENULLVALUE.equals(valueS)) return null;
		
		switch (targetType.getCanonicalName()) {
		case "java.lang.String":
			return valueS;
		case "java.lang.Integer":
			return Integer.valueOf(valueS);
			
		case "java.lang.Float":
			return Float.valueOf(valueS);
		case "java.lang.Double":
			return Double.valueOf(valueS);
		case "java.lang.Long":
			return Long.valueOf(valueS);
		case "java.math.BigDecimal":
			if(valueS.indexOf('.') > 0) {
				return BigDecimal.valueOf(Double.valueOf(valueS));
			} else {
				return BigDecimal.valueOf(Long.valueOf(valueS));
			}
		default:
			if (targetType.isEnum()) {
				return Enum.valueOf((Class<Enum>)targetType, valueS);
			}
			throw new ParseException("Unsupported parameter type " + targetType.getCanonicalName());
		}
	}

	//Parses the command-line option value valueS and updates the corresponding field in the paramsBean object using reflection.
	private void updateParam(OptionMeta m, String valueS) {
		Object value = parse(m.getFieldType(), valueS); 
		try {
			descriptors.get(m.getFieldName()).getWriteMethod().invoke(paramsBean, value);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ParseException("Couldn't set member field of '"+m.getFieldName()+"' in class of "+paramsBean.getClass()+". Make sure that you implemented setter for this field. ", e);
		} 
		
	}


	//Parses the command-line arguments provided in the 'argv' array and maps them to the 'paramsBean.
	public boolean parseCommandLine(String[] argv) {
		// -l 10 -r 20 -o mul
		String optionName = "";
		for(int i=0; i < argv.length; i++) {
			if(i%2 == 0) {
				optionName = argv[i];
				if ("-h".equals(optionName) || "--help".equals(optionName)) {
					provideHelp();
					return false;
				}
				if(!optionName.startsWith("--") && !optionName.startsWith("-") ) {
					output.add("Wrong parameter name format: "+optionName + ". Parameter name should be started with - or --");
					provideHelp();
					return false;
				}
				continue;
			}
			
			var meta = metas.get(optionName);
			if (meta == null) {
				output.add("Unknown parameter "+optionName);
				return false;
				
			}
			try {
			  updateParam (meta, argv[i]);
			} catch(ParseException e) {
				throw e;
			} catch(Exception e) {
				output.add("Can't accept value of '" + argv[i] + "' for parameter '" + optionName + "'");
				return false;
			}
		}
		
		return checkMandatority();
	}

	//Checks if all the required options have been provided values in the paramsBean.
	private boolean checkMandatority() {
		List<OptionMeta> emptyRequired = new ArrayList<>();
		for (PropertyDescriptor descriptor : descriptors.values()) {
			Object value = null;
			try {
			  value = descriptor.getReadMethod().invoke(paramsBean);
			} catch (Exception e) {
				
			} 
			var meta = metasByFieldName.get(descriptor.getName());
			if (value==null && meta.isRequired()) {
				emptyRequired.add(meta);
			}
		}
		
		if (!emptyRequired.isEmpty()) {
			output.add("You have missed required parameters:");
			for (OptionMeta m : emptyRequired) {
				printOption(m);
			}
			return false;
		}
		return true;
	}
}
