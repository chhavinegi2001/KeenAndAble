package com.assignment;

public class UserRegistrationResponse {
	 private String userName;
	    private String status;
	    private String message;
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public UserRegistrationResponse(String userName, String status, String message) {
			super();
			this.userName = userName;
			this.status = status;
			this.message = message;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
}
