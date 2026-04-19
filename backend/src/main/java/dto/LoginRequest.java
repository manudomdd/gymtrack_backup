package dto;

public class LoginRequest {
	
	private String email; 
	private String password;
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String user) {
		this.email = user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	} 
}
