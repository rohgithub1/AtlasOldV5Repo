package com.cci.rest.service.mail;

import lombok.Data;

@Data
public class AtlasMailRequest {
	
	private String name;
	private String[] cc;
	private String[] to;
	private String from;
	private String subject;
	private String Body;
	private StringBuilder BodyNew;
	private String attachment; 
	
	public String getName() {
		return name;
	}
	public String[] getCc() {
		return cc;
	}
	
	public String[] getTo() {
		return to;
	}
	public String getFrom() {
		return from;
	}
	public String getSubject() {
		return subject;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setCc(String[] cc) {
		this.cc = cc;
	}
	
	public void setTo(String[] to) {
		this.to = to;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return Body;
	}
	public void setBody(String body) {
		Body = body;
	}
	public StringBuilder getBodyNew() {
		return BodyNew;
	}
	public void setBodyNew(StringBuilder bodyNew) {
		BodyNew = bodyNew;
	}
	public String getAttachment() {
		return attachment;
	}
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
	
	
	
}
