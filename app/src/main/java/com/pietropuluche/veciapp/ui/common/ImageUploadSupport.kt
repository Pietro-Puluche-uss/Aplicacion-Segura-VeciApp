package com.pietropuluche.veciapp.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

@Composable
fun OptionalImagePicker(
    selectedImageUri: String?,
    isProcessing: Boolean,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit
) {
    Text(
        text = "Imagen opcional",
        style = MaterialTheme.typography.titleSmall
    )
    Text(
        text = "Puedes adjuntar una foto si quieres dar mas contexto al reporte.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
    if (selectedImageUri != null) {
        AsyncImage(
            model = selectedImageUri,
            contentDescription = "Imagen seleccionada",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentScale = ContentScale.Crop
        )
    }
    if (isProcessing) {
        Text(
            text = "Procesando imagen...",
            style = MaterialTheme.typography.bodySmall
        )
    }
    Button(
        onClick = onPickImage,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isProcessing
    ) {
        Text(if (selectedImageUri == null) "Seleccionar imagen" else "Cambiar imagen")
    }
    if (selectedImageUri != null) {
        OutlinedButton(
            onClick = onClearImage,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Text("Quitar imagen")
        }
    }
}

suspend fun uriToCompressedDataUrl(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    val bitmap = decodeBitmapForUpload(context, uri) ?: return@withContext null
    try {
        bitmapToDataUrl(bitmap)
    } finally {
        bitmap.recycle()
    }
}

private fun decodeBitmapForUpload(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        input.use { stream ->
            val original = BitmapFactory.decodeStream(stream) ?: return null
            normalizeBitmap(original)
        }
    }.getOrNull()
}

private fun normalizeBitmap(source: Bitmap): Bitmap {
    val maxSide = 1280f
    val ratio = max(source.width, source.height) / maxSide
    if (ratio <= 1f) return source
    val targetW = (source.width / ratio).toInt().coerceAtLeast(1)
    val targetH = (source.height / ratio).toInt().coerceAtLeast(1)
    val resized = Bitmap.createScaledBitmap(source, targetW, targetH, true)
    if (resized != source) {
        source.recycle()
    }
    return resized
}

private fun bitmapToDataUrl(bitmap: Bitmap): String {
    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)
    val base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    return "data:image/jpeg;base64,$base64"
}
