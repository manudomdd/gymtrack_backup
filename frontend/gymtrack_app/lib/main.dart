import 'package:flutter/material.dart';
import 'screens/login_screen.dart';
import 'screens/register_screen.dart';

void main() {
  runApp(const GymTrackApp());
}

class GymTrackApp extends StatelessWidget {
  const GymTrackApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'GymTrack',
      debugShowCheckedModeBanner: false,
      
      // Configuración del tema para que se vea "pro"
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.blueAccent,
          brightness: Brightness.dark, // Estética oscura como tus capturas
        ),
        inputDecorationTheme: const InputDecorationTheme(
          border: OutlineInputBorder(),
          filled: true,
        ),
      ),

      // Definición de rutas de navegación
      initialRoute: '/',
      routes: {
        '/': (context) => LoginScreen(), // Quita el const aquí
        '/register': (context) => RegisterScreen(), // Y aquí
      },
    );
  }
}