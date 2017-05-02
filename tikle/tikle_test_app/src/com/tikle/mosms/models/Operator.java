package com.tikle.mosms.models;

public enum Operator {
	Turkcell(1),
	Vodafone(2),
	Avea(3);
	
	private final int type;
	
	private Operator(int type) {
		this.type=type;
	}
	
	public int getType(){
		return type;
	}
	
	public static Operator fromType(int type) {
		for(Operator operator:Operator.values()){
			if(type==operator.type)
				return operator;
		}
		return null;
	}
}
