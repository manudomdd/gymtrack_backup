# 🏋️‍♂️ GymTrack

Una solución integral cliente-entrenador para la monitorización avanzada del rendimiento deportivo y la salud. 

GymTrack no es solo un registro de entrenamientos; es una plataforma bidireccional diseñada para que los entrenadores personales puedan llevar un control exhaustivo y en tiempo real de las rutinas, la progresión y los biomarcadores de sus clientes, facilitando la toma de decisiones basadas en datos.

---

## ✨ Características Principales

### 🧑‍💻 Para el Entrenador (Panel de Control)
* **Gestión de Clientes:** Vista unificada de los clientes asignados con sus datos básicos (peso, altura, edad).
* **Sincronización en Tiempo Real:** Acceso de lectura al diario de entrenamiento exacto de cada cliente para supervisar el cumplimiento de las rutinas.
* **Métricas y Progresión:** Sistema analítico que utiliza regresiones lineales para evaluar el progreso, estancamiento o recesión del rendimiento del cliente por grupo muscular.
* **Biomarcadores Diarios:** Monitorización de parámetros de salud y recuperación externos al gimnasio (calidad/horas de sueño y pasos diarios) integrados en un calendario visual.

### 🏃‍♂️ Para el Cliente (App Móvil)
* **Registro Granular de Entrenamientos:** Anotación detallada serie a serie, incluyendo: Grupo muscular, Ejercicio, Peso, Repeticiones y RIR (Repeticiones en Reserva).
* **Calendario Integrado:** Navegación intuitiva por fechas para registrar o revisar entrenamientos pasados y futuros.
* **Registro de Salud:** Formularios diarios rápidos para anotar el sueño y el NEAT (pasos diarios), ayudando al entrenador a ajustar la carga de trabajo.

---

## 🛠️ Stack Tecnológico

El proyecto está dividido en una arquitectura Cliente-Servidor robusta:

* **Frontend (Móvil):** Desarrollado nativamente en Android Studio utilizando **Java** y XML para la interfaz gráfica.
* **Backend (Servidor):** API RESTful desarrollada en **Java** (con Spring boot) encargada de la lógica de negocio, cálculos matemáticos (regresión lineal) y persistencia de datos.
* **Comunicación:** Peticiones HTTP estructuradas con intercambio de datos en formato JSON.

---

## 🚀 Instalación y Despliegue

### Requisitos previos
* [Android Studio](https://developer.android.com/studio) para la ejecución del frontend.
* [Eclipse IDE](https://www.eclipse.org/downloads/) o equivalente para el entorno backend.
* SDK de Java (JDK 11 o superior recomendado).

### Pasos para probar el proyecto en local

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/manudomdd/GymTrack.git] (https://github.com/manudomdd/GymTrack.git)
