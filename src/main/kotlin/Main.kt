import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

enum class TimerState {
    IDLE, RUNNING, PAUSED
}

@Composable
fun PomodoroApp() {
    var timeRemaining by remember { mutableStateOf(1500) } // 25 minutos en segundos
    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    val tasks = remember { mutableStateListOf<String>() }
    var newTask by remember { mutableStateOf("") }

    // Lógica del temporizador con async/await
    LaunchedEffect(timerState) {
        if (timerState == TimerState.RUNNING) {
            coroutineScope {
                val job = async {
                    while (timeRemaining > 0) {
                        delay(1000)
                        timeRemaining--
                    }
                    timerState = TimerState.IDLE // Vuelve a IDLE al llegar a cero
                }
                try {
                    awaitCancellation()
                } finally {
                    job.cancel()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Círculo del temporizador
        Box(
            modifier = Modifier.size(200.dp).background(Color(0xFFBBDEFB), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatTime(timeRemaining),
                style = MaterialTheme.typography.h3,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de control
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    when (timerState) {
                        TimerState.IDLE, TimerState.PAUSED -> timerState = TimerState.RUNNING
                        TimerState.RUNNING -> timerState = TimerState.PAUSED
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
            ) {
                Text(
                    if (timerState == TimerState.RUNNING) "Pausar" else "Iniciar",
                    color = Color.White
                )
            }
            Button(
                onClick = {
                    timerState = TimerState.IDLE
                    timeRemaining = 1500
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336))
            ) {
                Text("Reiniciar", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de tareas con contenedor desplazable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Altura fija para asegurar desplazamiento
                .background(Color(0xFFF5F5F5))
                .border(1.dp, Color(0xFF2196F3))
        ) {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState
            ) {
                itemsIndexed(tasks) { index, task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            task,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.body1
                        )
                        IconButton(onClick = { tasks.removeAt(index) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Agregar tarea
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newTask,
                onValueChange = { newTask = it },
                modifier = Modifier.weight(1f),
                label = { Text("Nueva Tarea") },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color(0xFFE3F2FD),
                    focusedIndicatorColor = Color(0xFF2196F3),
                    unfocusedIndicatorColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newTask.isNotBlank()) {
                        tasks.add(newTask.trim())
                        newTask = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
            ) {
                Text("Agregar", color = Color.White)
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        PomodoroApp()
    }
}