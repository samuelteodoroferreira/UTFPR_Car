package com.example.ftprcar.ui.car

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.ftprcar.data.model.Car
import com.example.ftprcar.data.model.Place
import java.util.UUID

private val DecimalKeys = KeyboardOptions(keyboardType = KeyboardType.Decimal)

data class CarDraft(
    val name: String = "",
    val year: String = "",
    val licence: String = "",
    val imageUrl: String = "",
    val lat: String = "",
    val lng: String = ""
)

private data class CarBuildResult(
    val car: Car? = null,
    val error: String? = null
)

fun Car.toDraft() = CarDraft(
    name = name,
    year = year,
    licence = licence,
    imageUrl = imageUrl,
    lat = place?.lat?.toString().orEmpty(),
    lng = place?.lng?.toString().orEmpty()
)

@Composable
fun CarFormDialog(
    title: String,
    confirmLabel: String,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Car) -> Unit,
    initial: CarDraft = CarDraft(),
    carId: String? = null
) {
    var name by remember(initial) { mutableStateOf(initial.name) }
    var year by remember(initial) { mutableStateOf(initial.year) }
    var licence by remember(initial) { mutableStateOf(initial.licence) }
    var imageUrl by remember(initial) { mutableStateOf(initial.imageUrl) }
    var lat by remember(initial) { mutableStateOf(initial.lat) }
    var lng by remember(initial) { mutableStateOf(initial.lng) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Nome") },
                    singleLine = true,
                    enabled = !saving
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = {
                        year = it
                        error = null
                    },
                    label = { Text("Ano") },
                    placeholder = { Text("2024/2025") },
                    singleLine = true,
                    enabled = !saving
                )
                OutlinedTextField(
                    value = licence,
                    onValueChange = {
                        licence = it
                        error = null
                    },
                    label = { Text("Placa") },
                    placeholder = { Text("ABC-1234") },
                    singleLine = true,
                    enabled = !saving
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = {
                        imageUrl = it
                        error = null
                    },
                    label = { Text("URL da imagem") },
                    singleLine = true,
                    enabled = !saving
                )
                ImagePreview(
                    imageUrl = imageUrl,
                    description = "Preview da imagem do veiculo"
                )
                OutlinedTextField(
                    value = lat,
                    onValueChange = {
                        lat = it
                        error = null
                    },
                    label = { Text("Latitude") },
                    singleLine = true,
                    enabled = !saving,
                    keyboardOptions = DecimalKeys
                )
                OutlinedTextField(
                    value = lng,
                    onValueChange = {
                        lng = it
                        error = null
                    },
                    label = { Text("Longitude") },
                    singleLine = true,
                    enabled = !saving,
                    keyboardOptions = DecimalKeys
                )
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    val result = buildCar(
                        id = carId,
                        name = name,
                        year = year,
                        licence = licence,
                        imageUrl = imageUrl,
                        lat = lat,
                        lng = lng
                    )

                    if (result.car == null) {
                        error = result.error ?: "Preencha todos os campos corretamente."
                        return@TextButton
                    }

                    onSubmit(result.car)
                }
            ) {
                Text(if (saving) "Salvando..." else confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                enabled = !saving,
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

private fun buildCar(
    id: String?,
    name: String,
    year: String,
    licence: String,
    imageUrl: String,
    lat: String,
    lng: String
): CarBuildResult {
    val trimmedName = name.trim()
    val trimmedYear = year.trim()
    val trimmedLicence = licence.trim()
    val trimmedImage = imageUrl.trim()
    val latValue = lat.trim().replace(',', '.').toDoubleOrNull()
    val lngValue = lng.trim().replace(',', '.').toDoubleOrNull()

    val validationError = validateCarInput(
        name = trimmedName,
        year = trimmedYear,
        licence = trimmedLicence,
        imageUrl = trimmedImage,
        lat = latValue,
        lng = lngValue
    )

    if (validationError != null) {
        return CarBuildResult(error = validationError)
    }

    return CarBuildResult(
        car = Car(
            id = id ?: UUID.randomUUID().toString(),
            name = trimmedName,
            year = trimmedYear,
            licence = trimmedLicence,
            imageUrl = trimmedImage,
            place = Place(lat = latValue!!, lng = lngValue!!)
        )
    )
}

internal fun validateCarInput(
    name: String,
    year: String,
    licence: String,
    imageUrl: String,
    lat: Double?,
    lng: Double?
): String? {
    return when {
        name.isBlank() -> "Informe o nome do veiculo."
        year.isBlank() -> "Informe o ano do veiculo."
        licence.isBlank() -> "Informe a placa do veiculo."
        imageUrl.isBlank() -> "Informe a URL da imagem."
        !imageUrl.isHttpUrl() -> "Use uma URL iniciando com http:// ou https://."
        lat == null -> "Informe uma latitude valida."
        lng == null -> "Informe uma longitude valida."
        lat !in -90.0..90.0 -> "A latitude deve estar entre -90 e 90."
        lng !in -180.0..180.0 -> "A longitude deve estar entre -180 e 180."
        else -> null
    }
}

private fun String.isHttpUrl(): Boolean {
    return startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)
}

@Composable
private fun ImagePreview(
    imageUrl: String,
    description: String
) {
    if (imageUrl.isBlank()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        SubcomposeAsyncImage(
            model = imageUrl.trim(),
            contentDescription = description,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        )
    }
}
