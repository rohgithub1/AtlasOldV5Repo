package com.cci.rest.service.dataobjects;

public class AtlasGetUserMapping {

		private String userMapping;

		public String getUserMapping() {
			return userMapping;
		}

		public void setUserMapping(String userMapping) {
			this.userMapping = userMapping;
		}

		@Override
		public String toString() {
			return "AtlasGetUserMapping [userMapping=" + userMapping + "]";
		}
}
