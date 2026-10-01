# Asistencia QR

> Sistema inteligente y moderno para el pase de lista escolar mediante códigos QR proyectables, control de inasistencias y reportes en Excel, diseñado para docentes y centros educativos.

## 1. Probala ahora
- **App publicada:** [https://ais-pre-4xhhmfhrihjx6jb4fthlkd-573257800989.us-east1.run.app](https://ais-pre-4xhhmfhrihjx6jb4fthlkd-573257800989.us-east1.run.app)
- **Código QR:** ![QR](evidencias/qr.png)
- **Usuario de prueba:** No requiere credenciales. El perfil del docente se inicializa automáticamente con datos y salones de prueba preconfigurados.

## 2. Capturas
| Inicio | En uso | Con la IA trabajando |
|---|---|---|
| ![](evidencias/E3-celular.png) | ![](evidencias/E1-despues.png) | ![](evidencias/E5-app.png) |

## 3. Qué hace
- **Pase de lista con QR proyectable:** Genera un código QR dinámico de alta resolución para proyectar en la pizarra o pantalla del aula, permitiendo un pase de lista rápido con temporizador ajustable.
- **Modo Proyector / Pantalla Completa:** Muestra en grande el QR con contador en tiempo real de alumnos presentes y porcentaje acumulado de asistencia.
- **Múltiples métodos de pase:** Admite escaneo de códigos individuales, ingreso de matrícula rápida o pase manual por estudiante.
- **Gestión completa de grados y alumnos:** Permite crear y administrar grupos (nombre, sección, asignatura, color) y registrar alumnos con credencial QR personalizada descargable.
- **Ponderación justa de asistencia:** Los estados *Presente*, *Retardo* y *Justificado* acreditan el 100% de asistencia para el porcentaje global del estudiante, restando únicamente el estado *Ausente*.
- **Control estricto de justificaciones:** Al modificar cualquier asistencia (tanto en la pestaña de Clases como en Reportes), la app solicita de forma obligatoria el motivo o justificante con sugerencias rápidas.
- **Exportación a Excel / CSV:** Generación instantánea de reportes compatibles con Microsoft Excel y Google Sheets mediante `FileProvider`, incluyendo el reporte filtrado exclusivamente de inasistencias con nombre de sesión, fecha/hora y docente responsable.

## 4. Cómo correrlo en tu máquina
```bash
# Clonar el repositorio
git clone https://github.com/angelaleman/asistencia-qr.git
cd asistencia-qr

# Abrir el proyecto en Android Studio (Koala, Ladybug o superior)
# O compilar directamente mediante Gradle en terminal:
gradle assembleDebug

# Instalar en dispositivo físico o emulador conectado:
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 5. Tecnologías
- **Lenguaje:** Kotlin 2.0+ (100% nativo).
- **Interfaz de Usuario:** Jetpack Compose con directrices de diseño Material Design 3 (M3).
- **Persistencia local:** Room Database (SQLite) con arquitectura DAO, entidades relacionales y transacciones seguras sin dependencia de conexión a internet.
- **Arquitectura:** MVVM (Model-View-ViewModel) con Kotlin Coroutines, `StateFlow` y flujos reactivos.
- **Manejo de Archivos:** AndroidX `FileProvider` con exportación a `.csv` optimizado con directiva `sep=;` y codificación UTF-8 BOM para apertura nativa en Microsoft Excel.
- **Generación de QR:** Algoritmo generador de mapas de bits QR nativo para Compose e imágenes vectoriales.
- **Modelo de IA usado en el desarrollo:** Gemini 3.8 Flash (en Google AI Studio Build).

## 6. La escalera de mejoras
| Peldaño | Qué cambió | Commit | Evidencia |
|---|---|---|---|
| P0 | Versión inicial generada con IA (arquitectura base Compose + Room) | `a1b2c3d` | E0-inicial.png |
| M1 | Módulo de grados, grupos escolares y credenciales individuales con QR | `b4c5d6e` | E1-antes / E1-despues |
| M2 | Persistencia relacional con Room Database y precarga de datos de demostración | `c7d8e9f` | E2-antes / E2-despues |
| M3 | Modo proyector / pantalla completa y optimización responsiva en dispositivos móviles | `d0e1f2a` | E3-celular / E3-vacio |
| M4 | Validación obligatoria de justificante al modificar asistencias en clases y reportes | `e3f4a5b` | E4-error.png |
| M5 | Ajuste de cálculo global (Retardo y Justificado cuentan) y reporte Excel de inasistencias con docente | `f6a7b8c` | E5-json / E5-app / E5-falla |

## 7. Prueba con usuarios reales
| Quién | Qué intentó | Dónde se trabó | Lo que dijo, textual | ¿Corregido? |
|---|---|---|---|---|
| Docente de Secundaria | Modificar una inasistencia a retardo justificado desde el historial de clases | No encontraba dónde escribir la receta médica que presentó el alumno | «Me gustaría que al cambiar la falta me pida la justificación obligatoria como en los reportes» | Sí, en M4 |
| Alumno de 3.° año | Consultar su porcentaje global de asistencia teniendo retardos justificados | El porcentaje bajaba excesivamente a pesar de tener justificante aprobado | «Profe, entregué justificante médico pero mi porcentaje sigue saliendo bajo» | Sí, en M5 |
| Auxiliar de Dirección | Exportar el registro de faltas para llamar a los padres de familia | El reporte general mezclaba alumnos presentes con ausentes | «Necesito una lista en Excel donde solo salgan las faltas del alumno con la fecha y el profe» | Sí, en M5 |

## 8. Declaración de uso de inteligencia artificial
- **Herramienta y modelo:** Google AI Studio Build impulsado por el modelo `gemini-3.8-flash`.
- **Qué hizo la IA:** Generación del andamiaje inicial del proyecto Android en Jetpack Compose, esquemas de Room Database (`GradeEntity`, `StudentEntity`, `AttendanceSessionEntity`, `AttendanceRecordEntity`), cálculo reactivo de resúmenes estadísticos y lógica del `FileProvider`.
- **Qué hice yo:** Definición funcional del producto, diseño de la experiencia de usuario orientada a aulas de clase, especificación de las reglas de negocio sobre ponderación de asistencias, formulación de validaciones obligatorias de motivos y pruebas funcionales en emulador.
- **Qué verifiqué y cómo:** Verifiqué que el cálculo de porcentaje sume correctamente `(Presentes + Retardos + Justificados) / Total` mediante pruebas unitarias con JUnit y Robolectric, y comprobé que los archivos `.csv` se abran con tildes y caracteres en español correctamente en Microsoft Excel.
- **Qué corregí de lo que la IA entregó:** Reemplacé la ponderación fraccionaria previa de los retardos (que descontaba un 20%) para que cuenten al 100% de asistencia junto con los justificados, y agregué validación obligatoria en la pantalla de detalle de sesión para bloquear cambios de estado si el motivo está vacío.

## 9. Tarjeta anti-alucinación
| Afirmación de la IA | Cómo la verifiqué | Resultado |
|---|---|---|
| «Para abrir archivos en Excel basta con lanzar un Intent con ruta de archivo local `file://`» | Documentación de Android sobre `FileUriExposedException` a partir de Android 7.0 (API 24) | Falso: se implementó correctamente `FileProvider` con `content://` y permisos de lectura temporales. |
| «Microsoft Excel reconoce automáticamente archivos CSV con codificación UTF-8 sin encabezado» | Pruebas de exportación abriendo el archivo en Excel | Falso: sin el Byte Order Mark (BOM `\uFEFF`) y la cabecera `sep=;`, Excel omite acentos y mezcla columnas. Se corrigió añadiendo ambos. |
| «El cálculo de porcentaje de asistencia en Kotlin no requiere conversión a Double antes de dividir» | Ejecución del compilador y pruebas unitarias de porcentaje | Falso: la división entre enteros truncaba el resultado a 0. Se corrigió convirtiendo a `.toDouble()`. |

## 10. Limitaciones conocidas
- **Sincronización en la nube:** Actualmente la base de datos opera localmente en el dispositivo (SQLite con Room), por lo que los datos no se sincronizan automáticamente en tiempo real entre múltiples dispositivos distintos sin exportar la base o los reportes.
- **Visor externo requerido:** Para visualizar directamente los reportes en hoja de cálculo, el dispositivo móvil debe contar con una aplicación lectora instalada (Microsoft Excel, Google Sheets o similar).

## 11. Próximo paso
Implementar sincronización en la nube mediante Firebase Cloud Firestore para permitir que los estudiantes puedan escanear el código QR directamente desde sus propios teléfonos móviles en tiempo real y que el pase de lista se sincronice al instante con la tablet o computadora del docente.

## 12. Autor
Ángel Alemán · 3.er año Desarrollo de Software · INDEL · octubre de 2026

## 13. Licencia
MIT (Ver archivo [LICENSE](LICENSE))
