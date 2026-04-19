package dto;

public class RegisterRequest {
	
	private String nombre; 
	private String email; 
	private String password;
	private int edad; 
	private Enum tipoUsuario; 
	private double peso; 
	private int altura; 
	private int neat;
	
	public RegisterRequest(String nombre, String email, String password, Enum tipoUsuario, double peso, int altura,
			int neat) {
		super();
		this.nombre = nombre;
		this.email = email;
		this.password = password;
		this.tipoUsuario = tipoUsuario;
		this.peso = peso;
		this.altura = altura;
		this.neat = neat;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Enum getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(Enum tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}

	public double getPeso() {
		return peso;
	}

	public void setPeso(double peso) {
		this.peso = peso;
	}

	public int getAltura() {
		return altura;
	}

	public void setAltura(int altura) {
		this.altura = altura;
	}

	public int getNeat() {
		return neat;
	}

	public void setNeat(int neat) {
		this.neat = neat;
	}

	public int getEdad() {
		return edad;
	}

	public void setEdad(int edad) {
		this.edad = edad;
	} 
	
	
	
	

	
}
