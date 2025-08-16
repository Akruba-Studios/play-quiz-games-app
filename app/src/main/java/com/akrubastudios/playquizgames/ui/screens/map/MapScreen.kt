package com.akrubastudios.playquizgames.ui.screens.map

import android.R.attr.path
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akrubastudios.playquizgames.Routes
import com.akrubastudios.playquizgames.domain.Country

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import com.caverock.androidsvg.SVG
import java.io.IOException

import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive
import androidx.core.graphics.PathParser
import kotlinx.coroutines.withContext

import androidx.compose.animation.core.*
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Scaffold nos da la estructura de la pantalla principal
    Scaffold(
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Routes.RANKING_SCREEN) },
                    icon = { Icon(Icons.Filled.Leaderboard, contentDescription = "Ranking") },
                    label = { Text("Ranking") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navegar a Perfil en el futuro */ },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding -> // El contenido principal debe usar este padding

        // CAMBIO: Usar Box en lugar de Column para layering correcto
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Usa el padding proporcionado por el Scaffold
        ) {
            // CAPA 1: El mapa de fondo
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                InteractiveWorldMap(
                    countries = uiState.countries,
                    conqueredCountryIds = uiState.conqueredCountryIds,
                    availableCountryIds = uiState.availableCountryIds,
                    onCountryClick = { countryId ->
                        navController.navigate(
                            Routes.COUNTRY_SCREEN.replace("{countryId}", countryId)
                        )
                    },
                    modifier = Modifier.fillMaxSize() // El mapa ocupa todo el espacio
                )
            }

            // CAPA 2: El título ENCIMA del mapa
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Text(
                    text = "Mapa del Conocimiento",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun InteractiveWorldMap(
    countries: List<Country>,
    conqueredCountryIds: List<String>,
    availableCountryIds: List<String>,
    onCountryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados para zoom y pan
    var scale by remember { mutableStateOf(1.6f) } // 1.5f es la escala para agrandar por defecto el mapa
    var offset by remember { mutableStateOf(Offset.Zero) }

    var canvasWidth by remember { mutableStateOf(1080f) }
    var canvasHeight by remember { mutableStateOf(1812f) }

    // Estados para el SVG y su procesamiento
    var svgDocument by remember { mutableStateOf<SVG?>(null) }
    var processedSvgBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // NUEVO: Estado para controlar la inicialización completa
    var isMapReady by remember { mutableStateOf(false) }
    var isInitialProcessing by remember { mutableStateOf(true) }

    // Cache de paths y colores
    var pathColorMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var countryPaths by remember { mutableStateOf<Map<String, android.graphics.Path>>(emptyMap()) }

    var pulseAlpha by remember { mutableStateOf(0.7f) }

    val context = LocalContext.current
    val density = LocalDensity.current

    // Definir colores
    val conqueredColor = Color(0xFFD4AF37).toArgb() // Dorado
    val availableColor = Color(0xFF2196F3).toArgb() // Azul
    val defaultColor = Color(0xFF4A5568).toArgb() // Gris azulado congelado

    // Función para extraer coordenadas de paths del SVG
    suspend fun extractPathCoordinates(
        context: Context,
        countryIds: Set<String>
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val pathMap = mutableMapOf<String, String>()

        try {
            val inputStream = context.assets.open("world-map.min.svg")
            val svgContent = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            countryIds.forEach { countryId ->
                var pathData: String? = null

                // ESTRATEGIA 1: Buscar <path id="xx" d="...">
                val directPathPattern = """<path[^>]*id=["']$countryId["'][^>]*d=["']([^"']*)["']""".toRegex()
                directPathPattern.find(svgContent)?.let {
                    pathData = it.groupValues[1]
                }

                // ESTRATEGIA 2: Si no se encontró, buscar <g id="xx">...<path d="...">
                if (pathData == null) {
                    val groupPattern = """<g[^>]*id=["']$countryId["'][^>]*>(.*?)</g>""".toRegex(RegexOption.DOT_MATCHES_ALL)
                    groupPattern.find(svgContent)?.let { groupMatch ->
                        val groupContent = groupMatch.groupValues[1]
                        val pathsInGroupPattern = """<path[^>]*d=["']([^"']*)["']""".toRegex()
                        val allPaths = pathsInGroupPattern.findAll(groupContent).map { it.groupValues[1] }.toList()

                        if (allPaths.isNotEmpty()) {
                            pathData = allPaths.joinToString(" ")
                        }
                    }
                }

                pathData?.let { data ->
                    pathMap[countryId] = data
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("InteractiveWorldMap", "Error extrayendo paths", e)
        }

        pathMap
    }

    // Función para convertir path data SVG a Path de Android
    fun parsePathData(pathData: String, path: android.graphics.Path) {
        try {
            androidx.core.graphics.PathParser.createPathFromPathData(pathData)?.let { parsedPath ->
                path.set(parsedPath)
            }
        } catch (e: Exception) {
            android.util.Log.e("InteractiveWorldMap", "Error parseando path data: ${pathData.take(50)}", e)
            path.addRect(0f, 0f, 100f, 100f, android.graphics.Path.Direction.CW)
        }
    }

    // MODIFICADO: Carga inicial del SVG con mejor control de estado
    LaunchedEffect(Unit) {
        try {
            isInitialProcessing = true
            val inputStream = context.assets.open("world-map.min.svg")
            val svg = SVG.getFromInputStream(inputStream)
            svgDocument = svg
        } catch (e: IOException) {
            android.util.Log.e("InteractiveWorldMap", "Error cargando SVG", e)
            isInitialProcessing = false
        }
    }

    // Recalcular colores cuando cambien las listas
    LaunchedEffect(conqueredCountryIds, availableCountryIds, countries) {
        if (countries.isEmpty()) return@LaunchedEffect

        val newColorMap = mutableMapOf<String, Int>()

        // Colorear países conquistados
        conqueredCountryIds.forEach { countryId ->
            newColorMap[countryId] = conqueredColor
        }

        // Colorear países disponibles (que no estén conquistados)
        availableCountryIds.forEach { countryId ->
            if (!conqueredCountryIds.contains(countryId)) {
                newColorMap[countryId] = availableColor
            }
        }

        // Todos los demás países en gris por defecto
        countries.forEach { country ->
            if (!newColorMap.containsKey(country.countryId)) {
                newColorMap[country.countryId] = defaultColor
            }
        }

        pathColorMap = newColorMap
    }

    // MODIFICADO: Procesamiento mejorado sin flash
    LaunchedEffect(svgDocument, pathColorMap) {
        svgDocument?.let { svg ->
            if (pathColorMap.isNotEmpty()) {
                try {
                    // NO limpiar processedSvgBitmap aquí para evitar flash
                    // processedSvgBitmap = null  // <-- REMOVIDO

                    val pathCoordinates = extractPathCoordinates(context, pathColorMap.keys)

                    if (!isActive) return@LaunchedEffect

                    val width = 1200
                    val height = (width * svg.documentHeight / svg.documentWidth).toInt()

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.TRANSPARENT)

                    svg.setDocumentViewBox(0f, 0f, svg.documentWidth, svg.documentHeight)
                    svg.renderToCanvas(canvas)

                    if (!isActive) return@LaunchedEffect

                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        style = android.graphics.Paint.Style.FILL
                    }

                    val newCountryPaths = mutableMapOf<String, android.graphics.Path>()

                    pathColorMap.forEach { (countryId, color) ->
                        pathCoordinates[countryId]?.let { pathData ->
                            paint.color = color
                            val path = android.graphics.Path()
                            parsePathData(pathData, path)
                            newCountryPaths[countryId] = path
                            canvas.drawPath(path, paint)

                            // NUEVO: Agregar grietas para países grises
                            if (color == defaultColor) {
                                val crackPaint = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = 2f
                                    setColor(android.graphics.Color.argb(64, 255, 0, 0)) // Blanco 25% transparente
                                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 4f), 0f)
                                }
                                canvas.drawPath(path, crackPaint)
                            }
                        }
                    }

                    if (isActive) {
                        // ACTUALIZAR TODO ATOMICAMENTE para evitar estados inconsistentes
                        processedSvgBitmap = bitmap
                        countryPaths = newCountryPaths

                        // MARCAR COMO LISTO SOLO CUANDO TODO ESTÉ PROCESADO
                        if (isInitialProcessing) {
                            isMapReady = true
                            isInitialProcessing = false
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("InteractiveWorldMap", "Error procesando SVG con colores", e)
                    isInitialProcessing = false
                }
            }
        }
    }

    // NUEVO: Animación de pulso para candados
    LaunchedEffect(Unit) {
        while (true) {
            animate(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = tween(1500, easing = FastOutSlowInEasing)
            ) { value, _ ->
                pulseAlpha = value
            }
            animate(
                initialValue = 0.7f,
                targetValue = 0.3f,
                animationSpec = tween(1500, easing = FastOutSlowInEasing)
            ) { value, _ ->
                pulseAlpha = value
            }
        }
    }

    // Función para detectar qué país fue tocado
    fun detectCountryFromTap(
        tapOffset: Offset,
        svgBitmap: Bitmap,
        canvasSize: androidx.compose.ui.geometry.Size
    ): String? {
        return try {
            val bitmapAspectRatio = svgBitmap.width.toFloat() / svgBitmap.height.toFloat()
            val canvasAspectRatio = canvasSize.width / canvasSize.height

            val scaleFactor = if (bitmapAspectRatio > canvasAspectRatio) {
                canvasSize.width / svgBitmap.width
            } else {
                canvasSize.height / svgBitmap.height
            }

            val scaledWidth = svgBitmap.width * scaleFactor * scale
            val scaledHeight = svgBitmap.height * scaleFactor * scale

            val centerX = canvasSize.width / 2f
            val centerY = canvasSize.height / 2f
            val left = centerX - (scaledWidth / 2f) + offset.x + (canvasSize.width * 0.20f) // + (205/1080=0.20) +205 es el offset manual para centrar el mapa, tiene que ser el mismo valor en canvas
            val top = centerY - (scaledHeight / 2f) + offset.y - (canvasSize.height * 0.055f) // - (100/1812=0.055) -100 es el offset manual para centrar el mapa, tiene que ser el mismo valor en canvas

            val svgX = (tapOffset.x - left) / (scaleFactor * scale)
            val svgY = (tapOffset.y - top) / (scaleFactor * scale)

            if (svgX < 0 || svgX >= svgBitmap.width || svgY < 0 || svgY >= svgBitmap.height) {
                return null
            }

            val interactableCountries = (conqueredCountryIds + availableCountryIds).toSet()

            for (countryId in interactableCountries) {
                countryPaths[countryId]?.let { path ->
                    val region = android.graphics.Region()
                    val clipRegion = android.graphics.Region(
                        0, 0, svgBitmap.width, svgBitmap.height
                    )

                    region.setPath(path, clipRegion)

                    if (region.contains(svgX.toInt(), svgY.toInt())) {
                        return countryId
                    }
                }
            }
            null

        } catch (e: Exception) {
            android.util.Log.e("HitTesting", "Error en detección de tap", e)
            null
        }
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(0.8f, 3f)

        // Usar valores fijos basados en una pantalla promedio (más simple)
        val maxOffsetX = canvasWidth * 0.18f * newScale   // 18% para hacer drag en horizontal
        val maxOffsetY = canvasHeight * 0.10f * newScale  // 12% para hacer drag en vertical

        val newOffset = (offset + panChange).copy(
            x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
            y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
        )
        scale = newScale
        offset = newOffset
    }

    // MODIFICADO: Canvas con mejor control de renderizado
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .transformable(transformableState)
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (isMapReady) {  // Solo permitir taps cuando esté listo
                        processedSvgBitmap?.let { bitmap ->
                            detectCountryFromTap(tapOffset, bitmap, size.toSize())?.let { countryId ->
                                onCountryClick(countryId)
                            }
                        }
                    }
                }
            }
    ) {
        canvasWidth = size.width
        canvasHeight = size.height

        // Fondo gris claro
        drawRect(
            color = Color.White,
            size = size
        )

        // MODIFICADO: Renderizado condicional mejorado
        when {
            // Mostrar el mapa solo cuando esté completamente listo
            isMapReady && processedSvgBitmap != null -> {
                val bitmap = processedSvgBitmap!!

                val bitmapAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val canvasAspectRatio = size.width / size.height

                val scaleFactor = if (bitmapAspectRatio > canvasAspectRatio) {
                    size.width / bitmap.width
                } else {
                    size.height / bitmap.height
                }

                val scaledWidth = bitmap.width * scaleFactor * scale
                val scaledHeight = bitmap.height * scaleFactor * scale

                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val left = centerX - (scaledWidth / 2f) + offset.x + (size.width * 0.20f) // + (205/1080=0.20) +205 es el offset manual para centrar el mapa, tiene que ser el mismo valor en detectcountryfromtap
                val top = centerY - (scaledHeight / 2f) + offset.y - (size.height * 0.055f) // - (100/1812=0.055) -100 es el offset manual para centrar el mapa, tiene que ser el mismo valor en detectcountryfromtap

                drawImage(
                    image = bitmap.asImageBitmap(),
                    dstOffset = IntOffset(left.toInt(), top.toInt()),
                    dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt()),
                    filterQuality = FilterQuality.High
                )

                // NUEVO: Dibujar candados con coordenadas manuales para países problemáticos
                countries.forEach { country ->
                    if (pathColorMap[country.countryId] == defaultColor) {
                        countryPaths[country.countryId]?.let { path ->
                            // Coordenadas manuales para países problemáticos
                            val (centerX, centerY) = when (country.countryId) {
                                "es" -> Pair(395f, 420f)  // España - coordenadas ajustadas manualmente
                                "fr" -> Pair(410f, 410f)  // Francia - coordenadas ajustadas manualmente
                                else -> {
                                    // Para el resto usar cálculo automático
                                    val bounds = android.graphics.RectF()
                                    path.computeBounds(bounds, true)
                                    Pair(bounds.centerX(), bounds.centerY())
                                }
                            }

                            // USAR LA MISMA LÓGICA EXACTA QUE EL BITMAP
                            val bitmapAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val canvasAspectRatio = size.width / size.height

                            val scaleFactor = if (bitmapAspectRatio > canvasAspectRatio) {
                                size.width / bitmap.width
                            } else {
                                size.height / bitmap.height
                            }

                            val transformedX = left + (centerX * scaleFactor * scale)
                            val transformedY = top + (centerY * scaleFactor * scale)

                            val lockSize = 4.dp.toPx() * scale // 6. Tamaño del candado

                            drawContext.canvas.nativeCanvas.apply {
                                val lockPaint = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    setColor(android.graphics.Color.argb(
                                        (255 * pulseAlpha).toInt(), 255, 0, 0 // Color candados: rgb 220,220,220 = gris blanco
                                    ))
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = 3f * scale
                                }

                                // Dibujar candado simple
                                val lockRect = android.graphics.RectF(
                                    transformedX - lockSize/2,
                                    transformedY - lockSize/4,
                                    transformedX + lockSize/2,
                                    transformedY + lockSize/2
                                )
                                drawRoundRect(lockRect, 4f * scale, 4f * scale, lockPaint)

                                // Arco del candado
                                val arcRect = android.graphics.RectF(
                                    transformedX - lockSize/3,
                                    transformedY - lockSize/2,
                                    transformedX + lockSize/3,
                                    transformedY
                                )
                                drawArc(arcRect, 180f, 180f, false, lockPaint)
                            }
                        }
                    }
                }

                // Debug info (opcional - puedes removarlo)
                /*
                drawContext.canvas.nativeCanvas.drawText(
                    "Scale: ${String.format("%.1f", scale)} | Países: ${pathColorMap.size}",
                    20f,
                    40f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 30f
                        isAntiAlias = true
                    }
                )
                 */
            }
        }
    }
}