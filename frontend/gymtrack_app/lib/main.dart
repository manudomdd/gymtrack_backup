import 'package:flutter/material.dart';
import 'screens/login_screen.dart';
import 'screens/register_screen.dart';
import 'screens/home_screen.dart';
import 'screens/trainer_home_screen.dart';

void main() {
  runApp(const GymTrackApp());
}

/// Clase principal de la aplicación GymTrack
/// 
/// Configura el tema Material 3 de la app y define todas las rutas de navegación.
/// Las rutas disponibles son:
/// - '/': Pantalla de login
/// - '/register': Pantalla de registro
/// - '/home': Pantalla principal para usuarios/clientes
/// - '/trainer_home': Pantalla principal para entrenadores
/// 
/// TODO: Agregar lógica en el LoginScreen para navegar a '/home' o '/trainer_home'
/// basándose en el tipo de usuario que inicia sesión (cliente o entrenador)
class GymTrackApp extends StatelessWidget {
  const GymTrackApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'GymTrack',
      debugShowCheckedModeBanner: false,
      
      /// Configuración del tema para una estética profesional
      /// Utiliza Material 3 con un esquema de colores basado en azul
      /// y modo oscuro para una mejor experiencia visual
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.blueAccent,
          brightness: Brightness.dark, // Estética oscura
        ),
        inputDecorationTheme: const InputDecorationTheme(
          border: OutlineInputBorder(),
          filled: true,
        ),
      ),

      /// Definición de rutas de navegación de la aplicación
      initialRoute: '/home',
      routes: {
        /// Ruta raíz: Pantalla de login
        '/': (context) => LoginScreen(),
        
        /// Ruta de registro: Pantalla para crear nueva cuenta
        '/register': (context) => RegisterScreen(),
        
        /// Ruta para usuarios normales/clientes post-login
        /// Muestra el dashboard principal con menú lateral
        /// Acceso a: Registro de entrenamientos, Métricas de actividad
        '/home': (context) => const HomeScreen(),
        
        /// Ruta para entrenadores/coaches post-login
        /// Muestra dashboard de entrenador con acceso a clientes
        /// Acceso a: Lista de clientes, información de entrenamientos
        '/trainer_home': (context) => const TrainerHomeScreen(),
      },
    );
  }
}