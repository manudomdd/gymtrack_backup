import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class AuthService {
  // 10.0.2.2 es la IP para que el emulador vea tu PC en Linux
  final String baseUrl = "http://10.0.2.2:8080/api/auth";

  Future<bool> login(String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email, 'password': password}),
      ).timeout(const Duration(seconds: 5)); // No se queda colgado infinitamente

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final prefs = await SharedPreferences.getInstance();
        await prefs.setString('token', data['token']);
        return true;
      }
      return false;
    } catch (e) {
      print("Error de conexión: $e"); // Esto saldrá en tu consola de VS Code
      return false;
    }
  }

  Future<bool> register(Map<String, dynamic> userData) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(userData),
      ).timeout(const Duration(seconds: 5));
      
      return response.statusCode == 201 || response.statusCode == 200;
    } catch (e) {
      print("Error de registro: $e");
      return false;
    }
  }
}