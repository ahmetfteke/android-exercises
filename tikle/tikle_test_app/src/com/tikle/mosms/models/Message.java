package com.tikle.mosms.models;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int ID;
	private String ShortNumber;
	private String Text;
	private int SleepMinutes;
	private Operator Operator;
	private String Description;
	
	public int getID() {
		return ID;
	}
	public void setID(int Id) {
		ID = Id;
	}
	public String getShortNumber() {
		return ShortNumber;
	}
	public void setShortNumber(String shortNumber) {
		ShortNumber = shortNumber;
	}
	public String getText() {
		return Text;
	}
	public void setText(String text) {
		Text = text;
	}
	public int getSleepMinutes() {
		return SleepMinutes;
	}
	public void setSleepMinutes(int sleepMinutes) {
		SleepMinutes = sleepMinutes;
	}
	public Operator getOperator() {
		return Operator;
	}
	public void setOperator(Operator operator) {
		Operator = operator;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Message)) {
	        return false;
	    }

		Message message = (Message) o;
		return this.getID() == message.getID()
				&& this.getShortNumber().equals(message.getShortNumber())
				&& this.getText().equals(message.getText())
				&& this.getSleepMinutes() == message.getSleepMinutes()
				&& this.getOperator().getType() == message.getOperator().getType()
				&& this.getDescription().equals(message.getDescription());
	}
}
