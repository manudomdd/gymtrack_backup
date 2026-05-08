package app.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table (name = "users")

public class User implements UserDetails {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String email;
    private String password;
    private int edad; 
    @Enumerated(EnumType.STRING)
    private TipoUsuario tipoUsuario; 
    private double peso; 
    private int altura;
    private int neat; 
    
    @jakarta.persistence.Column(unique = true)
    private String trainerCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    @JsonIgnore
    private User trainer;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> clients;
    
	public User() {
		super();
	}

	public User(Long id, String nombre, String email, String password) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.email = email;
		this.password = password;
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	public int getEdad() {
		return edad;
	}

	public void setEdad(int edad) {
		this.edad = edad;
	}

	public TipoUsuario getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(TipoUsuario tipoUsuario) {
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

	public String getTrainerCode() {
		return trainerCode;
	}

	public void setTrainerCode(String trainerCode) {
		this.trainerCode = trainerCode;
	}

	public User getTrainer() {
		return trainer;
	}

	public void setTrainer(User trainer) {
		this.trainer = trainer;
	}

	public List<User> getClients() {
		return clients;
	}

	public void setClients(List<User> clients) {
		this.clients = clients;
	}
}
