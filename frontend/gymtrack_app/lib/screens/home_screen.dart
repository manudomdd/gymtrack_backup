import 'package:flutter/material.dart';

/// Pantalla principal para usuarios normales (clientes)
/// 
/// Esta pantalla actúa como hub central post-login para usuarios regulares.
/// Contiene un Drawer (menú lateral) con opciones de navegación principal:
/// - Registro de Entrenamiento: para anotar sesiones de ejercicios
/// - Métricas de Actividad: para tracking de pasos, sueño y otros parámetros
/// - Cerrar Sesión: para desconectarse de la aplicación
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  /// Variable para controlar cuál pantalla mostrar
  /// 0: Dashboard principal
  /// 1: Registro de Entrenamiento
  /// 2: Métricas de Actividad
  int _currentIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F0014),
      appBar: AppBar(
        title: const Text(
          'GymTrack',
          style: TextStyle(
            color: Color(0xFFFF00BF),
            fontWeight: FontWeight.bold,
            fontSize: 24,
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

  /// Construye el menú lateral (Drawer)
  /// 
  /// Contiene opciones de navegación principales para el usuario:
  /// - Home
  /// - Registro de Entrenamiento
  /// - Métricas de Actividad
  /// - Cerrar Sesión
  Widget _buildDrawer() {
    return Drawer(
      backgroundColor: const Color(0xFF1A0026),
      child: ListView(
        padding: EdgeInsets.zero,
        children: [
          // Header del drawer con información del usuario
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
                  'Usuario GymTrack',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),
          // Opción: Dashboard Principal
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
          
          // Opción: Registro de Entrenamiento
          ListTile(
            leading: const Icon(Icons.fitness_center, color: Color(0xFFFF00BF)),
            title: const Text(
              'Registro de Entrenamiento',
              style: TextStyle(color: Colors.white),
            ),
            onTap: () {
              setState(() => _currentIndex = 1);
              Navigator.pop(context);
            },
            selected: _currentIndex == 1,
            selectedTileColor: const Color(0xFF4D0073),
          ),

          // Opción: Métricas de Actividad
          ListTile(
            leading: const Icon(Icons.monitor_heart, color: Color(0xFFFF00BF)),
            title: const Text(
              'Métricas de Actividad',
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
        return const TrainingLogScreen();
      case 2:
        return const ActivityMetricsScreen();
      default:
        return _buildDashboard();
    }
  }

  /// Pantalla principal (Dashboard)
  /// 
  /// Muestra un resumen general de estadísticas y accesos rápidos.
  /// Será personalizada según los datos del usuario.
  Widget _buildDashboard() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Bienvenida
          const Text(
            'Bienvenido de vuelta',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Color(0xFFFF00BF),
            ),
          ),
          const SizedBox(height: 24),

          // Cards de estadísticas rápidas
          Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  title: 'Entrenamientos',
                  value: '12',
                  icon: Icons.fitness_center,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _buildStatCard(
                  title: 'Pasos Hoy',
                  value: '8.5k',
                  icon: Icons.directions_walk,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  title: 'Calorías',
                  value: '542',
                  icon: Icons.local_fire_department,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _buildStatCard(
                  title: 'Horas Sueño',
                  value: '7.5h',
                  icon: Icons.bedtime,
                ),
              ),
            ],
          ),
          const SizedBox(height: 32),

          // Sección de accesos rápidos
          const Text(
            'Accesos Rápidos',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Color(0xFFFF00BF),
            ),
          ),
          const SizedBox(height: 12),

          // Botón: Nuevo Entrenamiento
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              onPressed: () {
                setState(() => _currentIndex = 1);
              },
              icon: const Icon(Icons.add),
              label: const Text('Registrar Nuevo Entrenamiento'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4D0073),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
            ),
          ),
          const SizedBox(height: 12),

          // Botón: Ver Métricas
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              onPressed: () {
                setState(() => _currentIndex = 2);
              },
              icon: const Icon(Icons.analytics),
              label: const Text('Ver Métricas de Actividad'),
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

  /// Widget reutilizable para mostrar tarjetas de estadísticas
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

  /// Maneja el logout del usuario
  /// 
  /// En el futuro, limpiará tokens y datos locales
  /// TODO: Implementar limpieza de datos locales (SharedPreferences/SQLite)
  /// TODO: Llamar a endpoint de logout en el backend
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

/// Pantalla para el registro de entrenamientos
/// 
/// Los usuarios podrán ver un calendario y registrar sus entrenamientos
/// seleccionando fechas y añadiendo:
/// - Nombre del ejercicio
/// - Series
/// - Repeticiones
/// - RIR (Reps In Reserve)
/// - Comentarios
class TrainingLogScreen extends StatefulWidget {
  const TrainingLogScreen({super.key});

  @override
  State<TrainingLogScreen> createState() => _TrainingLogScreenState();
}

class _TrainingLogScreenState extends State<TrainingLogScreen> {
  /// Fecha actualmente seleccionada en el calendario
  DateTime _selectedDate = DateTime.now();

  /// Lista de entrenamientos registrados (estructura temporal para ejemplo)
  /// TODO: Reemplazar con datos reales del backend
  final List<Map<String, dynamic>> _workoutSessions = [
    {
      'date': DateTime.now(),
      'exercise': 'Press de Banca',
      'sets': 4,
      'reps': 8,
      'rir': 2,
      'comment': 'Excelente sesión, muy fuerte hoy',
    },
    {
      'date': DateTime.now().subtract(const Duration(days: 1)),
      'exercise': 'Sentadilla',
      'sets': 3,
      'reps': 10,
      'rir': 1,
      'comment': 'Un poco cansado pero completé todo',
    },
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F0014),
      appBar: AppBar(
        title: const Text(
          'Registro de Entrenamiento',
          style: TextStyle(
            color: Color(0xFFFF00BF),
            fontWeight: FontWeight.bold,
          ),
        ),
        elevation: 0,
        backgroundColor: const Color(0xFF1A0026),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Calendario para seleccionar fechas
            _buildCalendar(),
            const SizedBox(height: 32),

            // Entrenamientos del día seleccionado
            const Text(
              'Entrenamientos del día',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: Color(0xFFFF00BF),
              ),
            ),
            const SizedBox(height: 16),

            // Mostrar entrenamientos filtrados por fecha seleccionada
            _buildWorkoutsList(),
            const SizedBox(height: 24),

            // Botón para añadir nuevo entrenamiento
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: _showAddWorkoutDialog,
                icon: const Icon(Icons.add),
                label: const Text('Añadir Entrenamiento'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF4D0073),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Construye un calendario simple para seleccionar fechas
  /// 
  /// Permite al usuario seleccionar el día para ver/registrar entrenamientos
  Widget _buildCalendar() {
    return Card(
      elevation: 0,
      color: const Color(0xFF1A0026),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Mes y año actual
            Text(
              '${_selectedDate.month}/${_selectedDate.year}',
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: Color(0xFFFF00BF),
              ),
            ),
            const SizedBox(height: 16),

            // Grid de días de la semana
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: const [
                Text('Lun', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white54)),
                Text('Mar', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white54)),
                Text('Mié', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white54)),
                Text('Jue', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white54)),
                Text('Vie', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white54)),
                Text('Sáb', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white54)),
                Text('Dom', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white54)),
              ],
            ),
            const SizedBox(height: 12),

            // Grid de días del mes
            _buildDaysGrid(),
          ],
        ),
      ),
    );
  }

  /// Construye un grid con los días del mes
  /// 
  /// Permite seleccionar un día específico para ver los entrenamientos
  /// TODO: Mejorar con una librería de calendario más robusta como table_calendar
  Widget _buildDaysGrid() {
    final firstDay = DateTime(_selectedDate.year, _selectedDate.month, 1);
    final lastDay = DateTime(_selectedDate.year, _selectedDate.month + 1, 0);
    final daysInMonth = lastDay.day;

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 7,
        childAspectRatio: 1,
      ),
      itemCount: daysInMonth,
      itemBuilder: (context, index) {
        final day = index + 1;
        final date = DateTime(_selectedDate.year, _selectedDate.month, day);
        final isSelected = _selectedDate.day == day;

        return GestureDetector(
          onTap: () {
            setState(() => _selectedDate = date);
          },
          child: Container(
            decoration: BoxDecoration(
              color: isSelected
                  ? const Color(0xFFFF00BF)
                  : Colors.transparent,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Center(
              child: Text(
                day.toString(),
                style: TextStyle(
                  color: isSelected ? const Color(0xFF0F0014) : Colors.white,
                  fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  /// Construye la lista de entrenamientos para el día seleccionado
  /// 
  /// Filtra los entrenamientos por la fecha seleccionada y los muestra
  /// TODO: Implementar conexión con backend para obtener entrenamientos
  Widget _buildWorkoutsList() {
    // Filtrar entrenamientos por fecha seleccionada
    final dayWorkouts = _workoutSessions.where((workout) {
      final workoutDate = workout['date'] as DateTime;
      return workoutDate.year == _selectedDate.year &&
          workoutDate.month == _selectedDate.month &&
          workoutDate.day == _selectedDate.day;
    }).toList();

    // Si no hay entrenamientos
    if (dayWorkouts.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 32),
          child: Column(
            children: [
              Icon(
                Icons.fitness_center,
                size: 48,
                color: Colors.grey.withOpacity(0.5),
              ),
              const SizedBox(height: 12),
              const Text(
                'No hay entrenamientos registrados',
                style: TextStyle(color: Colors.grey),
              ),
            ],
          ),
        ),
      );
    }

    // Mostrar lista de entrenamientos
    return ListView.separated(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: dayWorkouts.length,
      separatorBuilder: (context, index) => const SizedBox(height: 12),
      itemBuilder: (context, index) {
        final workout = dayWorkouts[index];
        return _buildWorkoutCard(workout);
      },
    );
  }

  /// Construye una tarjeta individual para un entrenamiento
  /// 
  /// Muestra:
  /// - Nombre del ejercicio
  /// - Series y repeticiones
  /// - RIR (Reps In Reserve)
  /// - Comentario si existe
  /// - Opciones para editar/eliminar
  Widget _buildWorkoutCard(Map<String, dynamic> workout) {
    return Card(
      color: const Color(0xFF1A0026),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Título del ejercicio
            Text(
              workout['exercise'],
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFFFF00BF),
              ),
            ),
            const SizedBox(height: 12),

            // Datos principales: Series, Repeticiones, RIR
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _buildWorkoutInfo('Series', '${workout['sets']}'),
                _buildWorkoutInfo('Repeticiones', '${workout['reps']}'),
                _buildWorkoutInfo('RIR', '${workout['rir']}'),
              ],
            ),

            // Comentario si existe
            if (workout['comment'] != null && workout['comment'].isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Comentario:',
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.white54,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      workout['comment'],
                      style: const TextStyle(fontSize: 14, color: Colors.white),
                    ),
                  ],
                ),
              ),
            const SizedBox(height: 12),

            // Botones de acción
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                IconButton(
                  icon: const Icon(Icons.edit, size: 20, color: Color(0xFFFF00BF)),
                  onPressed: () {
                    // TODO: Implementar edición de entrenamiento
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Editar en desarrollo')),
                    );
                  },
                ),
                IconButton(
                  icon: const Icon(Icons.delete, size: 20, color: Color(0xFFFF3333)),
                  onPressed: () {
                    // TODO: Implementar eliminación de entrenamiento
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Eliminar en desarrollo')),
                    );
                  },
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  /// Widget helper para mostrar pares de información en entrenamientos
  Widget _buildWorkoutInfo(String label, String value) {
    return Column(
      children: [
        Text(
          label,
          style: const TextStyle(
            fontSize: 12,
            color: Colors.white54,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
      ],
    );
  }

  /// Muestra un diálogo para añadir un nuevo entrenamiento
  /// 
  /// Permite ingresar:
  /// - Nombre del ejercicio
  /// - Número de series
  /// - Repeticiones (o rango de reps)
  /// - RIR (Reps In Reserve)
  /// - Comentario adicional
  void _showAddWorkoutDialog() {
    // Controladores para los campos del formulario
    final exerciseController = TextEditingController();
    final setsController = TextEditingController();
    final repsController = TextEditingController();
    final rirController = TextEditingController();
    final commentController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Registrar Entrenamiento'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Campo: Ejercicio
              TextField(
                controller: exerciseController,
                decoration: const InputDecoration(
                  labelText: 'Nombre del Ejercicio',
                  hintText: 'Ej: Press de Banca',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 12),

              // Row: Series y Repeticiones
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: setsController,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        labelText: 'Series',
                        border: OutlineInputBorder(),
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: TextField(
                      controller: repsController,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        labelText: 'Repeticiones',
                        border: OutlineInputBorder(),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),

              // Campo: RIR (Reps In Reserve)
              TextField(
                controller: rirController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'RIR (Reps In Reserve)',
                  hintText: '0 a 5',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 12),

              // Campo: Comentario
              TextField(
                controller: commentController,
                decoration: const InputDecoration(
                  labelText: 'Comentario (Opcional)',
                  hintText: 'Cualquier nota adicional...',
                  border: OutlineInputBorder(),
                ),
                maxLines: 3,
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () {
              // TODO: Validar campos y enviar al backend
              if (exerciseController.text.isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Por favor ingresa el nombre del ejercicio'),
                  ),
                );
                return;
              }

              // Crear nuevo entrenamiento (temporal, hasta implementar backend)
              final newWorkout = {
                'date': _selectedDate,
                'exercise': exerciseController.text,
                'sets': int.tryParse(setsController.text) ?? 0,
                'reps': int.tryParse(repsController.text) ?? 0,
                'rir': int.tryParse(rirController.text) ?? 0,
                'comment': commentController.text,
              };

              // Añadir a la lista local
              setState(() => _workoutSessions.add(newWorkout));

              // TODO: Enviar al backend
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Entrenamiento registrado correctamente'),
                  backgroundColor: Colors.green,
                ),
              );
            },
            child: const Text('Guardar'),
          ),
        ],
      ),
    );
  }
}

/// Pantalla de Métricas de Actividad
/// 
/// Muestra parámetros de vida del usuario como:
/// - Pasos diarios
/// - Estadísticas de sueño (hora de dormir, duración, calidad)
/// - Calorías quemadas
/// - Frecuencia cardíaca
/// - Datos de otros sensores/dispositivos
/// 
/// TODO: Integrar con APIs de fitness (Google Fit, Apple HealthKit)
/// TODO: Permitir entrada manual de datos
class ActivityMetricsScreen extends StatefulWidget {
  const ActivityMetricsScreen({super.key});

  @override
  State<ActivityMetricsScreen> createState() => _ActivityMetricsScreenState();
}

class _ActivityMetricsScreenState extends State<ActivityMetricsScreen> {
  /// Datos de ejemplo de métricas
  /// TODO: Reemplazar con datos reales del backend o sensores
  final Map<String, dynamic> _metricsData = {
    'steps': 8523,
    'stepsGoal': 10000,
    'sleepHours': 7.5,
    'sleepQuality': 'Buena',
    'calories': 542,
    'heartRate': 72,
    'water': 2.5,
  };

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F0014),
      appBar: AppBar(
        title: const Text(
          'Métricas de Actividad',
          style: TextStyle(
            color: Color(0xFFFF00BF),
            fontWeight: FontWeight.bold,
          ),
        ),
        elevation: 0,
        backgroundColor: const Color(0xFF1A0026),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Título de fecha
            Text(
              'Hoy, ${DateTime.now().day} de ${_getMonthName(DateTime.now().month)} de ${DateTime.now().year}',
              style: const TextStyle(
                fontSize: 16,
                color: Colors.white54,
              ),
            ),
            const SizedBox(height: 24),

            // Sección: Pasos
            _buildMetricSection(
              icon: Icons.directions_walk,
              title: 'Pasos',
              value: _metricsData['steps'],
              unit: 'pasos',
              goal: _metricsData['stepsGoal'],
              color: const Color(0xFFFF00BF),
            ),
            const SizedBox(height: 20),

            // Sección: Sueño
            _buildMetricSection(
              icon: Icons.bedtime,
              title: 'Sueño',
              value: _metricsData['sleepHours'],
              unit: 'horas',
              goal: 8,
              color: const Color(0xFF4D0073),
            ),
            const SizedBox(height: 20),

            // Sección: Calorías
            _buildMetricSection(
              icon: Icons.local_fire_department,
              title: 'Calorías Quemadas',
              value: _metricsData['calories'],
              unit: 'kcal',
              goal: 500,
              color: const Color(0xFFFF3333),
            ),
            const SizedBox(height: 20),

            // Sección: Frecuencia Cardíaca
            _buildMetricSection(
              icon: Icons.favorite,
              title: 'Frecuencia Cardíaca',
              value: _metricsData['heartRate'],
              unit: 'bpm',
              goal: 100,
              color: const Color(0xFF00BF80),
            ),
            const SizedBox(height: 20),

            // Sección: Agua
            _buildMetricSection(
              icon: Icons.water_drop,
              title: 'Agua Consumida',
              value: _metricsData['water'],
              unit: 'litros',
              goal: 3,
              color: const Color(0xFFFF00BF),
            ),
            const SizedBox(height: 32),

            // Botón para añadir datos manuales
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: _showAddMetricDialog,
                icon: const Icon(Icons.add),
                label: const Text('Registrar Métrica Manual'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF4D0073),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Construye una sección de métrica individual
  /// 
  /// Muestra un card con:
  /// - Icono
  /// - Nombre de la métrica
  /// - Valor actual
  /// - Progreso hacia el objetivo
  /// - Opción para editar/registrar datos
  Widget _buildMetricSection({
    required IconData icon,
    required String title,
    required dynamic value,
    required String unit,
    required dynamic goal,
    required Color color,
  }) {
    // Calcular porcentaje de progreso
    final double percentage = (value / goal).clamp(0.0, 1.0);

    return Card(
      color: const Color(0xFF1A0026),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Encabezado: Icono y título
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: color.withOpacity(0.2),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Icon(icon, color: color),
                    ),
                    const SizedBox(width: 12),
                    Text(
                      title,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                  ],
                ),
                IconButton(
                  icon: const Icon(Icons.edit, size: 20, color: Color(0xFFFF00BF)),
                  onPressed: () {
                    // TODO: Implementar edición de métrica
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Editar $title en desarrollo')),
                    );
                  },
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Valor actual
            Text(
              '$value $unit',
              style: const TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              'Objetivo: $goal $unit',
              style: const TextStyle(
                fontSize: 12,
                color: Colors.white54,
              ),
            ),
            const SizedBox(height: 12),

            // Barra de progreso
            ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(
                value: percentage,
                minHeight: 8,
                backgroundColor: Colors.grey.withOpacity(0.2),
                valueColor: AlwaysStoppedAnimation<Color>(color),
              ),
            ),
            const SizedBox(height: 8),

            // Porcentaje de progreso
            Text(
              '${(percentage * 100).toStringAsFixed(0)}% completado',
              style: const TextStyle(
                fontSize: 12,
                color: Colors.white54,
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Muestra un diálogo para añadir una métrica manual
  void _showAddMetricDialog() {
    String selectedMetric = 'Pasos';
    final valueController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Registrar Métrica'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Selector de métrica
              DropdownButtonFormField<String>(
                value: selectedMetric,
                items: const [
                  DropdownMenuItem(value: 'Pasos', child: Text('Pasos')),
                  DropdownMenuItem(value: 'Sueño', child: Text('Sueño (horas)')),
                  DropdownMenuItem(value: 'Calorías', child: Text('Calorías')),
                  DropdownMenuItem(value: 'Frecuencia Cardíaca', child: Text('Frecuencia Cardíaca (bpm)')),
                  DropdownMenuItem(value: 'Agua', child: Text('Agua (litros)')),
                ],
                onChanged: (value) {
                  selectedMetric = value ?? 'Pasos';
                },
                decoration: const InputDecoration(
                  labelText: 'Tipo de Métrica',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 12),

              // Campo de valor
              TextField(
                controller: valueController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Valor',
                  border: OutlineInputBorder(),
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () {
              // TODO: Validar y enviar al backend
              if (valueController.text.isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Por favor ingresa un valor')),
                );
                return;
              }

              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Métrica registrada correctamente'),
                  backgroundColor: Colors.green,
                ),
              );
            },
            child: const Text('Guardar'),
          ),
        ],
      ),
    );
  }

  /// Helper para obtener el nombre del mes en español
  String _getMonthName(int month) {
    const monthNames = [
      'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
      'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
    ];
    return monthNames[month - 1];
  }
}
