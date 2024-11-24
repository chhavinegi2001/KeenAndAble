package com.assignment;



public class UserRegistrationRequest {
    private String userName;
    public UserRegistrationRequest(String userName, String password) {
		super();
		this.userName = userName;
		this.password = password;
	}

	private String password;

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
