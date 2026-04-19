import 'package:flutter/material.dart';
import '../services/auth_service.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _authService = AuthService();
  bool _isLoading = false;

  // Controladores estandarizados
  final _nombreController = TextEditingController();
  final _emailController = TextEditingController();
  final _passController = TextEditingController();
  final _pesoController = TextEditingController();
  final _alturaController = TextEditingController();
  int _neat = 3; // Valor por defecto

  void _handleRegister() async {
    // Validación básica antes de enviar
    if (_nombreController.text.isEmpty || _emailController.text.isEmpty || _passController.text.isEmpty || _pesoController.text.isEmpty || _alturaController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text("Por favor, rellena todos los campos"),
          backgroundColor: Color(0xFFFF3333), // Rojo GymTrack
        ),
      );
      return;
    }

    setState(() => _isLoading = true);

    final userData = {
      "nombre": _nombreController.text,
      "email": _emailController.text,
      "password": _passController.text,
      "peso": double.parse(_pesoController.text),
      "altura": int.parse(_alturaController.text),
      "neat": _neat,
      "tipoUsuario": "CLIENTE" // Enum predefinido
    };

    bool success = await _authService.register(userData);
    setState(() => _isLoading = false);

    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text("¡Cuenta creada con éxito! Inicia sesión."),
          backgroundColor: Color(0xFF00BF80), // Verde GymTrack
        ),
      );
      Navigator.pop(context); // Volver al login
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text("Error: El email ya existe o hay un problema"),
          backgroundColor: Color(0xFFFF3333), // Rojo GymTrack
        ),
      );
    }
  }

  // Helper para crear los campos de texto con el estilo unificado
  Widget _buildTextField({
    required TextEditingController controller,
    required String hint,
    required IconData icon,
    bool isPassword = false,
    TextInputType keyboardType = TextInputType.text,
    String? suffixText,
  }) {
    // Colores unificados del diseño
    const deepPurple = Color(0xFF0F0014);
    const primaryDeepPurple = Color(0xFF4D0073);
    const whiteText = Colors.white;
    const magentaText = Color(0xFFFF00BF);

    return TextFormField(
      controller: controller,
      obscureText: isPassword,
      style: const TextStyle(color: whiteText),
      keyboardType: keyboardType,
      decoration: InputDecoration(
        filled: true,
        fillColor: deepPurple,
        hintText: hint,
        hintStyle: TextStyle(color: whiteText.withOpacity(0.4)),
        prefixIcon: Icon(icon, color: primaryDeepPurple),
        suffixText: suffixText,
        suffixStyle: const TextStyle(color: primaryDeepPurple),
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
    );
  }

  @override
  Widget build(BuildContext context) {
    // Paleta de colores unificada
    const deepPurple = Color(0xFF0F0014);
    const magentaText = Color(0xFFFF00BF);
    const primaryDeepPurple = Color(0xFF4D0073);
    const inputFieldBg = Color(0xFF1A0026);
    const whiteText = Colors.white;

    // Etiquetas para el Slider NEAT
    const neatLabels = {
      1: "Muy Sedentario",
      2: "Sedentario",
      3: "Moderado",
      4: "Activo",
      5: "Muy Activo"
    };

    return Scaffold(
      backgroundColor: deepPurple,
      body: SingleChildScrollView(
        child: Container(
          width: double.infinity,
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [Color(0xFF0F0014), Color(0xFF050009)],
            ),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 25.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 70),

              // Cabecera (Simplificada pero con el mismo estilo RichText)
              Column(
                children: [
                  RichText(
                    text: const TextSpan(
                      text: "CREAR\n",
                      style: TextStyle(
                        fontSize: 50,
                        fontWeight: FontWeight.w900,
                        color: magentaText,
                        height: 0.9,
                      ),
                      children: [
                        TextSpan(
                          text: "CUENTA",
                          style: TextStyle(
                            fontSize: 55,
                            fontWeight: FontWeight.w900,
                            color: whiteText,
                            height: 1.0,
                          ),
                        ),
                      ],
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 10),
                  Text(
                    "Únete a GymTrack y empieza a mejorar",
                    style: TextStyle(color: whiteText.withOpacity(0.6), fontSize: 14),
                  ),
                ],
              ),
              const SizedBox(height: 40),

              // Tarjeta de Registro (layered effect)
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
                    // --- DATOS PERSONALES ---
                    _buildTextField(
                      controller: _nombreController,
                      hint: "Nombre Completo",
                      icon: Icons.person_outline,
                    ),
                    const SizedBox(height: 15),
                    _buildTextField(
                      controller: _emailController,
                      hint: "example@email.com",
                      icon: Icons.email_outlined,
                      keyboardType: TextInputType.emailAddress,
                    ),
                    const SizedBox(height: 15),
                    _buildTextField(
                      controller: _passController,
                      hint: "Contraseña (min 6 caracteres)",
                      icon: Icons.lock_outline_rounded,
                      isPassword: true,
                    ),
                    const SizedBox(height: 25),

                    // Separador sutil
                    Divider(color: primaryDeepPurple.withOpacity(0.3), thickness: 1),
                    const SizedBox(height: 15),

                    // --- DATOS FÍSICOS (Row para compactar) ---
                    Row(
                      children: [
                        Expanded(
                          child: _buildTextField(
                            controller: _pesoController,
                            hint: "Peso",
                            icon: Icons.monitor_weight_outlined,
                            keyboardType: TextInputType.number,
                            suffixText: "kg",
                          ),
                        ),
                        const SizedBox(width: 15),
                        Expanded(
                          child: _buildTextField(
                            controller: _alturaController,
                            hint: "Altura",
                            icon: Icons.height_rounded,
                            keyboardType: TextInputType.number,
                            suffixText: "cm",
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 30),

                    // --- NIVEL DE ACTIVIDAD (Slider Estilizado) ---
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Padding(
                          padding: const EdgeInsets.only(left: 8.0),
                          child: Text(
                            "NIVEL DE ACTIVIDAD FISICA DIARIA: ${neatLabels[_neat]}",
                            style: const TextStyle(
                              color: whiteText,
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              letterSpacing: 0.5,
                            ),
                          ),
                        ),
                        const SizedBox(height: 5),
                        SliderTheme(
                          data: SliderTheme.of(context).copyWith(
                            activeTrackColor: magentaText,
                            inactiveTrackColor: primaryDeepPurple.withOpacity(0.3),
                            thumbColor: magentaText,
                            overlayColor: magentaText.withOpacity(0.2),
                            valueIndicatorColor: primaryDeepPurple,
                            valueIndicatorTextStyle: const TextStyle(color: whiteText),
                            tickMarkShape: const RoundSliderTickMarkShape(),
                            activeTickMarkColor: magentaText,
                            inactiveTickMarkColor: Colors.transparent,
                          ),
                          child: Slider(
                            value: _neat.toDouble(),
                            min: 1, max: 5, divisions: 4,
                            label: _neat.toString(),
                            onChanged: (val) => setState(() => _neat = val.toInt()),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 30),

                    // Botón Principal de Registro
                    SizedBox(
                      width: double.infinity,
                      height: 60,
                      child: ElevatedButton(
                        onPressed: _isLoading ? null : _handleRegister,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: magentaText,
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
                                  Icon(Icons.person_add_alt_1_rounded, size: 20),
                                  SizedBox(width: 10),
                                  Text(
                                    "Crear Cuenta",
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

              // Texto para volver al Login
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: Text(
                  "¿Ya tienes cuenta? Inicia Sesión",
                  style: TextStyle(
                    color: whiteText.withOpacity(0.7),
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              const SizedBox(height: 30), // Margen inferior extra para scroll
            ],
          ),
        ),
      ),
    );
  }
}