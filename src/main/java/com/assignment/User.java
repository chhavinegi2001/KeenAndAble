package com.assignment;



	import lombok.Data;
	import org.springframework.data.annotation.Id;
	import org.springframework.data.mongodb.core.mapping.Document;




	
	@Document(collection = "users")
	public class User {

	    @Id
	    private String id;
	    private String userName;
	    private String encryptedPassword;
	    
	    public String getGeneratedPassword() {
			return generatedPassword;
		}

		public void setGeneratedPassword(String generatedPassword) {
			this.generatedPassword = generatedPassword;
		}

		public void setDeleted(boolean isDeleted) {
			this.isDeleted = isDeleted;
		}

		private boolean isDeleted;
	    private String generatedPassword;

	    public boolean isDeleted() {
			return isDeleted;
		}

		// Constructor, getters, and setters
	    public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }

	    public String getUserName() {
	        return userName;
	    }

	    public void setUserName(String userName) {
	        this.userName = userName;
	    }

	    public User(String id, String userName, String encryptedPassword, boolean isDeleted, String generatedPassword) {
			super();
			this.id = id;
			this.userName = userName;
			this.encryptedPassword = encryptedPassword;
			this.isDeleted = isDeleted;
			this.generatedPassword = generatedPassword;
		}

		public User() {
			// TODO Auto-generated constructor stub
		}

		public String getEncryptedPassword() {
	        return encryptedPassword;
	    }

	    public void setEncryptedPassword(String encryptedPassword) {
	        this.encryptedPassword = encryptedPassword;
	    }

	   
	}
