import 'package:flutter/material.dart';

/// Pantalla principal para entrenadores (coaches)
/// 
/// Esta pantalla actúa como hub central post-login para entrenadores.
/// Contiene un Drawer (menú lateral) con opciones de navegación:
/// - Dashboard: Vista general de estadísticas
/// - Clientes: Ver lista de clientes y acceder a su información
/// - Perfil del Entrenador: Ver/editar información personal
/// - Cerrar Sesión: Desconectarse de la aplicación
class TrainerHomeScreen extends StatefulWidget {
  const TrainerHomeScreen({super.key});

  @override
  State<TrainerHomeScreen> createState() => _TrainerHomeScreenState();
}

class _TrainerHomeScreenState extends State<TrainerHomeScreen> {
  /// Index para controlar cuál pantalla mostrar
  /// 0: Dashboard principal
  /// 1: Lista de clientes
  /// 2: Perfil del entrenador
  int _currentIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F0014),
      appBar: AppBar(
        title: const Text(
          'GymTrack - Entrenador',
          style: TextStyle(
            color: Color(0xFFFF00BF),
            fontWeight: FontWeight.bold,
          ),
        ),
        elevation: 0,
        centerTitle: true,
        backgroundColor: const Color(0xFF1A0026),
      ),
      drawer: _buildDrawer(),
      body: _buildBody(),
    );
  }

  /// Construye el menú lateral (Drawer) para entrenadores
  /// 
  /// Contiene:
  /// - Header con información del entrenador
  /// - Opciones de navegación
  /// - Opción de logout
  Widget _buildDrawer() {
    return Drawer(
      backgroundColor: const Color(0xFF1A0026),
      child: ListView(
        padding: EdgeInsets.zero,
        children: [
          // Header con información del entrenador
          DrawerHeader(
            decoration: const BoxDecoration(
              color: Color(0xFF4D0073),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                const CircleAvatar(
                  radius: 30,
                  backgroundColor: Color(0xFFFF00BF),
                  child: Icon(
                    Icons.person,
                    size: 35,
                    color: Color(0xFF0F0014),
                  ),
                ),
                const SizedBox(height: 12),
                const Text(
                  'Coach - GymTrack',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                const Text(
                  'Entrenador Personal',
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),

          // Opción: Dashboard
          ListTile(
            leading: const Icon(Icons.home, color: Color(0xFFFF00BF)),
            title: const Text(
              'Dashboard',
              style: TextStyle(color: Colors.white),
            ),
            onTap: () {
              setState(() => _currentIndex = 0);
              Navigator.pop(context);
            },
            selected: _currentIndex == 0,
            selectedTileColor: const Color(0xFF4D0073),
          ),
          const Divider(color: Color(0xFF4D0073)),

          // Opción: Clientes (PRINCIPAL PARA ENTRENADORES)
          ListTile(
            leading: const Icon(Icons.group, color: Color(0xFFFF00BF)),
            title: const Text(
              'Clientes',
              style: TextStyle(color: Colors.white),
            ),
            onTap: () {
              setState(() => _currentIndex = 1);
              Navigator.pop(context);
            },
            selected: _currentIndex == 1,
            selectedTileColor: const Color(0xFF4D0073),
          ),

          // Opción: Perfil del Entrenador
          ListTile(
            leading: const Icon(Icons.account_circle, color: Color(0xFFFF00BF)),
            title: const Text(
              'Mi Perfil',
              style: TextStyle(color: Colors.white),
            ),
            onTap: () {
              setState(() => _currentIndex = 2);
              Navigator.pop(context);
            },
            selected: _currentIndex == 2,
            selectedTileColor: const Color(0xFF4D0073),
          ),
          const Divider(color: Color(0xFF4D0073)),

          // Opción: Cerrar Sesión
          ListTile(
            leading: const Icon(Icons.logout, color: Color(0xFFFF3333)),
            title: const Text(
              'Cerrar Sesión',
              style: TextStyle(color: Color(0xFFFF3333)),
            ),
            onTap: () {
              _handleLogout();
            },
          ),
        ],
      ),
    );
  }

  /// Construye el cuerpo principal según la pantalla seleccionada
  Widget _buildBody() {
    switch (_currentIndex) {
      case 0:
        return _buildDashboard();
      case 1:
        return const TrainerClientsScreen();
      case 2:
        return _buildTrainerProfile();
      default:
        return _buildDashboard();
    }
  }

  /// Dashboard principal para entrenadores
  /// 
  /// Muestra estadísticas generales:
  /// - Número total de clientes
  /// - Entrenamientos registrados
  /// - Clientes activos hoy
  /// - Accesos rápidos
  Widget _buildDashboard() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Saludo
          const Text(
            'Bienvenido, Coach',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Color(0xFFFF00BF),
            ),
          ),
          const SizedBox(height: 24),

          // Cards de estadísticas
          Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  title: 'Clientes Totales',
                  value: '8',
                  icon: Icons.group,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _buildStatCard(
                  title: 'Activos Hoy',
                  value: '5',
                  icon: Icons.check_circle,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  title: 'Entrenamientos',
                  value: '127',
                  icon: Icons.fitness_center,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _buildStatCard(
                  title: 'Esta Semana',
                  value: '34',
                  icon: Icons.calendar_today,
                ),
              ),
            ],
          ),
          const SizedBox(height: 32),

          // Accesos rápidos
          const Text(
            'Accesos Rápidos',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Color(0xFFFF00BF),
            ),
          ),
          const SizedBox(height: 12),

          // Botón: Ver todos los clientes
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              onPressed: () {
                setState(() => _currentIndex = 1);
              },
              icon: const Icon(Icons.group),
              label: const Text('Ver Todos mis Clientes'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4D0073),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
            ),
          ),
          const SizedBox(height: 12),

          // Botón: Crear nuevo plan
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              onPressed: () {
                // TODO: Implementar creación de planes de entrenamiento
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Crear plan en desarrollo')),
                );
              },
              icon: const Icon(Icons.add),
              label: const Text('Crear Nuevo Plan de Entrenamiento'),
              style: OutlinedButton.styleFrom(
                foregroundColor: const Color(0xFFFF00BF),
                side: const BorderSide(color: Color(0xFFFF00BF)),
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// Widget reutilizable para tarjetas de estadísticas
  Widget _buildStatCard({
    required String title,
    required String value,
    required IconData icon,
  }) {
    return Card(
      elevation: 0,
      color: const Color(0xFF1A0026),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, color: const Color(0xFFFF00BF)),
            const SizedBox(height: 8),
            Text(
              title,
              style: const TextStyle(
                fontSize: 12,
                color: Colors.white54,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              value,
              style: const TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Perfil del entrenador
  /// 
  /// Muestra información personal del entrenador:
  /// - Nombre
  /// - Especialidades
  /// - Experiencia
  /// - Contacto
  /// TODO: Implementar edición de perfil
  Widget _buildTrainerProfile() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          // Avatar
          const CircleAvatar(
            radius: 50,
            backgroundColor: Colors.blueAccent,
            child: Icon(
              Icons.person,
              size: 60,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 24),

          // Información básica
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Información Personal',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),
                  _buildProfileField('Nombre', 'Manuel López'),
                  _buildProfileField('Email', 'manuel@gymtrack.com'),
                  _buildProfileField('Teléfono', '+34 612 345 678'),
                  _buildProfileField('Especialidad', 'Musculación y Fuerza'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Estadísticas
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Estadísticas',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),
                  _buildProfileField('Clientes Activos', '8'),
                  _buildProfileField('Años de Experiencia', '5'),
                  _buildProfileField('Entrenamientos Registrados', '127'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // Botón de edición
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              onPressed: () {
                // TODO: Implementar edición de perfil
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Editar perfil en desarrollo')),
                );
              },
              icon: const Icon(Icons.edit),
              label: const Text('Editar Perfil'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// Widget helper para mostrar campos de perfil
  Widget _buildProfileField(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              color: Colors.grey,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  /// Maneja el cierre de sesión
  /// 
  /// TODO: Implementar limpieza de datos locales
  /// TODO: Llamar a endpoint de logout en backend
  void _handleLogout() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Cerrar Sesión'),
        content: const Text('¿Estás seguro de que quieres cerrar sesión?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () {
              // TODO: Implementar logout con backend
              Navigator.pushReplacementNamed(context, '/');
            },
            child: const Text('Cerrar Sesión'),
          ),
        ],
      ),
    );
  }
}

/// Pantalla para que los entrenadores vean sus clientes
/// 
/// Muestra:
/// - Lista de clientes del entrenador
/// - Información básica de cada cliente
/// - Opciones para ver detalles, ver entrenamientos, editar plan
/// TODO: Integrar con backend para obtener lista de clientes
/// TODO: Implementar búsqueda y filtrado de clientes
class TrainerClientsScreen extends StatefulWidget {
  const TrainerClientsScreen({super.key});

  @override
  State<TrainerClientsScreen> createState() => _TrainerClientsScreenState();
}

class _TrainerClientsScreenState extends State<TrainerClientsScreen> {
  /// Lista de clientes del entrenador
  /// 
  /// TODO: Reemplazar con datos del backend
  /// Estructura de cada cliente:
  /// - id: identificador único
  /// - nombre: nombre completo
  /// - email: correo electrónico
  /// - peso: peso actual en kg
  /// - altura: altura en cm
  /// - estadoActual: estado físico general
  /// - ultimoEntrenamiento: fecha del último entrenamiento registrado
  /// - rutina: nombre de la rutina actual
  final List<Map<String, dynamic>> _clients = [
    {
      'id': 1,
      'nombre': 'Juan García',
      'email': 'juan@gmail.com',
      'peso': 82.5,
      'altura': 178,
      'estadoActual': 'Bueno',
      'ultimoEntrenamiento': DateTime.now().subtract(const Duration(days: 1)),
      'rutina': 'Push/Pull/Legs',
    },
    {
      'id': 2,
      'nombre': 'Ana Martínez',
      'email': 'ana@gmail.com',
      'peso': 65.0,
      'altura': 168,
      'estadoActual': 'Muy Bueno',
      'ultimoEntrenamiento': DateTime.now(),
      'rutina': 'Full Body',
    },
    {
      'id': 3,
      'nombre': 'Carlos López',
      'email': 'carlos@gmail.com',
      'peso': 90.0,
      'altura': 182,
      'estadoActual': 'Regular',
      'ultimoEntrenamiento': DateTime.now().subtract(const Duration(days: 3)),
      'rutina': 'Upper/Lower',
    },
    {
      'id': 4,
      'nombre': 'María Rodríguez',
      'email': 'maria@gmail.com',
      'peso': 70.5,
      'altura': 172,
      'estadoActual': 'Bueno',
      'ultimoEntrenamiento': DateTime.now().subtract(const Duration(days: 2)),
      'rutina': 'HIIT',
    },
    {
      'id': 5,
      'nombre': 'Pedro Sánchez',
      'email': 'pedro@gmail.com',
      'peso': 78.0,
      'altura': 175,
      'estadoActual': 'Muy Bueno',
      'ultimoEntrenamiento': DateTime.now(),
      'rutina': 'Body Part Split',
    },
  ];

  /// Controlador para el filtro de búsqueda
  final TextEditingController _searchController = TextEditingController();

  /// Texto de búsqueda actual
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();
    // Listener para cambios en el campo de búsqueda
    _searchController.addListener(() {
      setState(() => _searchQuery = _searchController.text);
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Filtrar clientes según búsqueda
    final filteredClients = _clients
        .where((client) =>
            client['nombre']
                .toString()
                .toLowerCase()
                .contains(_searchQuery.toLowerCase()) ||
            client['email']
                .toString()
                .toLowerCase()
                .contains(_searchQuery.toLowerCase()))
        .toList();

    return Scaffold(
      backgroundColor: const Color(0xFF0F0014),
      appBar: AppBar(
        title: const Text(
          'Mis Clientes',
          style: TextStyle(
            color: Color(0xFFFF00BF),
            fontWeight: FontWeight.bold,
          ),
        ),
        elevation: 0,
        backgroundColor: const Color(0xFF1A0026),
      ),
      body: Column(
        children: [
          // Barra de búsqueda
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _searchController,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                hintText: 'Buscar cliente por nombre o email',
                hintStyle: const TextStyle(color: Colors.white54),
                prefixIcon: const Icon(Icons.search, color: Color(0xFFFF00BF)),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(color: Color(0xFF4D0073)),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(color: Color(0xFFFF00BF)),
                ),
                filled: true,
                fillColor: const Color(0xFF1A0026),
                contentPadding: const EdgeInsets.symmetric(horizontal: 16),
              ),
            ),
          ),

          // Lista de clientes
          if (filteredClients.isEmpty)
            Expanded(
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(
                      Icons.group,
                      size: 48,
                      color: Colors.white24,
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      'No se encontraron clientes',
                      style: TextStyle(color: Colors.white54),
                    ),
                  ],
                ),
              ),
            )
          else
            Expanded(
              child: ListView.separated(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: filteredClients.length,
                separatorBuilder: (context, index) => const SizedBox(height: 12),
                itemBuilder: (context, index) {
                  final client = filteredClients[index];
                  return _buildClientCard(client);
                },
              ),
            ),
        ],
      ),
    );
  }

  /// Construye una tarjeta individual para un cliente
  /// 
  /// Muestra:
  /// - Nombre del cliente
  /// - Email
  /// - Rutina actual
  /// - Último entrenamiento
  /// - Botones de acción (ver detalles, editar plan, etc.)
  Widget _buildClientCard(Map<String, dynamic> client) {
    final ultimoEntrenamiento = client['ultimoEntrenamiento'] as DateTime;
    final diasDesdeUltimo =
        DateTime.now().difference(ultimoEntrenamiento).inDays;

    return Card(
      color: const Color(0xFF1A0026),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header: Nombre y email
            Row(
              children: [
                // Avatar
                CircleAvatar(
                  radius: 24,
                  backgroundColor: const Color(0xFF4D0073),
                  child: Text(
                    client['nombre'][0],
                    style: const TextStyle(
                      color: Color(0xFFFF00BF),
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                // Información del cliente
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        client['nombre'],
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        client['email'],
                        style: const TextStyle(
                          fontSize: 12,
                          color: Colors.white54,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Información de la rutina y último entrenamiento
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: const Color(0xFF4D0073).withOpacity(0.3),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Rutina',
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.white54,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        client['rutina'],
                        style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                    ],
                  ),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      const Text(
                        'Último Entrenamiento',
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.white54,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        diasDesdeUltimo == 0
                            ? 'Hoy'
                            : diasDesdeUltimo == 1
                                ? 'Ayer'
                                : 'Hace $diasDesdeUltimo días',
                        style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Datos físicos
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _buildClientInfoChip(
                  'Peso',
                  '${client['peso']} kg',
                ),
                _buildClientInfoChip(
                  'Altura',
                  '${client['altura']} cm',
                ),
                _buildClientInfoChip(
                  'Estado',
                  client['estadoActual'],
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Botones de acción
            Row(
              children: [
                // Botón: Ver Detalles
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _showClientDetailDialog(client),
                    icon: const Icon(Icons.visibility, size: 18),
                    label: const Text('Detalles'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: const Color(0xFFFF00BF),
                      side: const BorderSide(color: Color(0xFFFF00BF)),
                    ),
                  ),
                ),
                const SizedBox(width: 8),

                // Botón: Ver Entrenamientos
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () {
                      // TODO: Navegar a pantalla de entrenamientos del cliente
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text(
                            'Ver entrenamientos de ${client['nombre']}',
                          ),
                        ),
                      );
                    },
                    icon: const Icon(Icons.fitness_center, size: 18),
                    label: const Text('Entrenamientos'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: const Color(0xFFFF00BF),
                      side: const BorderSide(color: Color(0xFFFF00BF)),
                    ),
                  ),
                ),
                const SizedBox(width: 8),

                // Botón: Editar Plan
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () {
                      // TODO: Navegar a pantalla de edición de plan
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text(
                            'Editar plan de ${client['nombre']}',
                          ),
                        ),
                      );
                    },
                    icon: const Icon(Icons.edit, size: 18),
                    label: const Text('Plan'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: const Color(0xFFFF00BF),
                      side: const BorderSide(color: Color(0xFFFF00BF)),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  /// Widget helper para mostrar información del cliente (peso, altura, estado)
  Widget _buildClientInfoChip(String label, String value) {
    return Column(
      children: [
        Text(
          label,
          style: const TextStyle(
            fontSize: 12,
            color: Colors.grey,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: const TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }

  /// Muestra un diálogo con los detalles completos del cliente
  /// 
  /// TODO: Obtener más información del backend
  void _showClientDetailDialog(Map<String, dynamic> client) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(client['nombre']),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildDetailField('Email', client['email']),
              _buildDetailField('Peso', '${client['peso']} kg'),
              _buildDetailField('Altura', '${client['altura']} cm'),
              _buildDetailField('Rutina Actual', client['rutina']),
              _buildDetailField('Estado Actual', client['estadoActual']),
              _buildDetailField('Número de Cliente', client['id'].toString()),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cerrar'),
          ),
          TextButton(
            onPressed: () {
              // TODO: Implementar edición de cliente
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Editar cliente en desarrollo')),
              );
              Navigator.pop(context);
            },
            child: const Text('Editar'),
          ),
        ],
      ),
    );
  }

  /// Widget helper para mostrar campos de detalle
  Widget _buildDetailField(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              color: Colors.grey,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 4),
          Text(value),
        ],
      ),
    );
  }
}
