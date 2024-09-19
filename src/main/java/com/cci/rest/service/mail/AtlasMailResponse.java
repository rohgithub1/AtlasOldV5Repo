package com.cci.rest.service.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AtlasMailResponse {
	private String message;
	private boolean status;
	
	public String getMessage() {
		return message;
	}
	public boolean isStatus() {
		return status;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
}
