package com.akrubastudios.playquizgames.data.repository
import android.content.Context
import com.akrubastudios.playquizgames.R // Para acceder a los recursos
import com.akrubastudios.playquizgames.domain.QuizLevelPackage
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class QuizRepository(private val context: Context) {

    // Preparamos el decodificador de JSON.
    // Lo configuramos para que ignore claves desconocidas en el JSON, lo cual es más seguro.
    private val json = Json { ignoreUnknownKeys = true }

    // Esta es la función principal que nuestra app llamará para obtener un nivel.
    fun getLevel(levelId: String): QuizLevelPackage? { // Devolvemos un tipo nullable (?) por si hay errores
        try {
            // Buscamos el ID del recurso en la carpeta 'raw'.
            // Por ahora, ignoramos el 'levelId' y siempre cargamos el mismo archivo de prueba.
            val resourceId = R.raw.logos_level_1

            // Abrimos un flujo de lectura para el archivo de recursos.
            val inputStream = context.resources.openRawResource(resourceId)

            // Leemos todo el contenido del archivo como una única cadena de texto.
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // Usamos la librería de serialización para convertir el texto JSON
            // en nuestro objeto de Kotlin 'QuizLevelPackage'.
            return json.decodeFromString<QuizLevelPackage>(jsonString)

        } catch (e: Exception) {
            // Si algo sale mal (ej. el archivo no existe o el JSON está mal formado),
            // imprimimos el error y devolvemos null para indicar que la carga falló.
            e.printStackTrace()
            return null
        }
    }
}