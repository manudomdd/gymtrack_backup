import 'package:flutter/material.dart';
import '../services/auth_service.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _authService = AuthService();
  bool _isLoading = false;

  void _handleLogin() async {
    setState(() => _isLoading = true);
    bool success = await _authService.login(_emailController.text, _passwordController.text);
    setState(() => _isLoading = false);

    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text("¡Bienvenido!"),
          backgroundColor: Color(0xFF00BF80), // Color verde de la imagen de GymTrack
        ),
      );
      // Navegar a la Home (cuando la tengas)
      // Navigator.pushReplacementNamed(context, '/home');
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text("Error: Credenciales inválidas"),
          backgroundColor: Color(0xFFFF3333), // Color rojo de la imagen de GymTrack
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    // Definición de colores de la imagen
    const deepPurple = Color(0xFF0F0014); // Fondo muy profundo
    const magentaText = Color(0xFFFF00BF); // Magenta vibrante del título
    const primaryDeepPurple = Color(0xFF4D0073); // Morado para botones y bordes
    const inputFieldBg = Color(0xFF1A0026); // Fondo de campos de entrada
    const whiteText = Colors.white;

    return Scaffold(
      backgroundColor: deepPurple,
      body: SingleChildScrollView(
        child: Container(
          width: double.infinity,
          height: MediaQuery.of(context).size.height,
          decoration: const BoxDecoration(
            // Sutil degradado de fondo
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [
                Color(0xFF0F0014),
                Color(0xFF050009),
              ],
            ),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 25.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 60), // Margen superior para el notch

              // Sección del Título/Logo
              Column(
                children: [
                  RichText(
                    text: const TextSpan(
                      text: "GYM\n",
                      style: TextStyle(
                        fontSize: 60,
                        fontWeight: FontWeight.w900,
                        color: magentaText,
                        height: 0.9,
                        fontFamily: 'Roboto', // Usar una fuente limpia
                      ),
                      children: [
                        TextSpan(
                          text: "TRACK",
                          style: TextStyle(
                            fontSize: 70,
                            fontWeight: FontWeight.w900,
                            color: whiteText,
                            height: 1.0,
                          ),
                        ),
                      ],
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 15),
                  
                  // El badge de estado
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 8),
                    decoration: BoxDecoration(
                      color: primaryDeepPurple,
                      borderRadius: BorderRadius.circular(20),
                      boxShadow: [
                        BoxShadow(
                          color: primaryDeepPurple.withOpacity(0.5),
                          blurRadius: 10,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: const Text(
                      "ESTADO: EN DESARROLLO",
                      style: TextStyle(
                        color: whiteText,
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        letterSpacing: 0.5,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 50),

              // Tarjeta de Login (layered effect)
              Container(
                padding: const EdgeInsets.all(25),
                decoration: BoxDecoration(
                  color: inputFieldBg,
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.2),
                      blurRadius: 15,
                      offset: const Offset(0, 8),
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    Text(
                      "!Bienvenido a GymTrack!",
                      style: TextStyle(
                        color: whiteText,
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    // Cabecera sutil de la tarjeta (fecha como en la imagen)
                    const SizedBox(height: 25),

                    // Campo de Email Estilizado
                    TextFormField(
                      controller: _emailController,
                      style: const TextStyle(color: whiteText),
                      decoration: InputDecoration(
                        filled: true,
                        fillColor: deepPurple,
                        hintText: "example@email.com",
                        hintStyle: TextStyle(color: whiteText.withOpacity(0.4)),
                        prefixIcon: const Icon(Icons.email_outlined, color: primaryDeepPurple),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(15),
                          borderSide: BorderSide.none,
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(15),
                          borderSide: const BorderSide(color: magentaText, width: 1.5),
                        ),
                        contentPadding: const EdgeInsets.symmetric(vertical: 20, horizontal: 15),
                      ),
                    ),
                    const SizedBox(height: 20),

                    // Campo de Contraseña Estilizado
                    TextFormField(
                      controller: _passwordController,
                      obscureText: true,
                      style: const TextStyle(color: whiteText),
                      decoration: InputDecoration(
                        filled: true,
                        fillColor: deepPurple,
                        hintText: "********",
                        hintStyle: TextStyle(color: whiteText.withOpacity(0.4)),
                        prefixIcon: const Icon(Icons.lock_outline_rounded, color: primaryDeepPurple),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(15),
                          borderSide: BorderSide.none,
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(15),
                          borderSide: const BorderSide(color: magentaText, width: 1.5),
                        ),
                        contentPadding: const EdgeInsets.symmetric(vertical: 20, horizontal: 15),
                      ),
                    ),
                    const SizedBox(height: 30),

                    // Botón Principal de Login "Entrar" (Estilo el botón de 'Empezar Sesión')
                    SizedBox(
                      width: double.infinity,
                      height: 60,
                      child: ElevatedButton(
                        onPressed: _isLoading ? null : _handleLogin,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: magentaText, // Magenta vibrante
                          foregroundColor: whiteText,
                          elevation: 12,
                          shadowColor: magentaText.withOpacity(0.4),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(30),
                          ),
                        ),
                        child: _isLoading
                            ? const CircularProgressIndicator(color: whiteText)
                            : const Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Icon(Icons.login_rounded, size: 20),
                                  SizedBox(width: 10),
                                  Text(
                                    "Entrar",
                                    style: TextStyle(
                                      fontSize: 18,
                                      fontWeight: FontWeight.w900,
                                      letterSpacing: 1.0,
                                    ),
                                  ),
                                ],
                              ),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 20),

              // Texto de Registro
              TextButton(
                onPressed: () => Navigator.pushNamed(context, '/register'),
                child: Text(
                  "¿No tienes cuenta? Regístrate",
                  style: TextStyle(
                    color: whiteText.withOpacity(0.7),
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
    );
  }
}