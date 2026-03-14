@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ftprcar.ui.car

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.ftprcar.data.api.RetrofitClient
import com.example.ftprcar.data.api.toUserError
import com.example.ftprcar.data.model.Car
import com.example.ftprcar.data.model.Place
import com.example.ftprcar.ui.theme.FTPRCarTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CarDetailActivity : ComponentActivity() {
    private var car by mutableStateOf<Car?>(null)
    private var loading by mutableStateOf(true)
    private var saving by mutableStateOf(false)
    private var error by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val carId = intent.getStringExtra(EXTRA_CAR_ID).orEmpty()

        setContent {
            FTPRCarTheme {
                when {
                    loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    car != null -> {
                        CarDetailScreen(
                            car = car!!,
                            saving = saving,
                            onBack = { finish() },
                            onEdit = ::updateCar,
                            onDelete = ::deleteCar,
                            onOpenMap = ::openMap
                        )
                    }
                    else -> {
                        CarDetailErrorScreen(
                            message = error ?: "Nao foi possivel carregar o veiculo",
                            onRetry = { if (carId.isNotBlank()) loadCar(carId) },
                            onBack = { finish() }
                        )
                    }
                }
            }
        }

        if (carId.isNotBlank()) {
            loadCar(carId)
        } else {
            loading = false
            error = "Nao foi possivel identificar o veiculo"
        }
    }

    private fun loadCar(id: String) {
        lifecycleScope.launch {
            loading = true
            error = null
            try {
                car = RetrofitClient.cars.getCar(id)
            } catch (e: Exception) {
                error = e.toUserError("Erro ao carregar veiculo")
            } finally {
                loading = false
            }
        }
    }

    private fun updateCar(updated: Car) {
        val id = car?.id
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "Nao foi possivel identificar o veiculo", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            saving = true
            try {
                val payload = updated.copy(id = id)
                val response = RetrofitClient.cars.updateCar(id, payload)
                if (response.isSuccessful) {
                    car = response.body() ?: payload
                    setResult(RESULT_OK)
                    Toast.makeText(this@CarDetailActivity, "Veiculo atualizado com sucesso", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@CarDetailActivity,
                        response.toUserError("Nao foi possivel atualizar o veiculo"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CarDetailActivity,
                    e.toUserError("Erro ao atualizar veiculo"),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                saving = false
            }
        }
    }

    private fun deleteCar() {
        val id = car?.id
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "Nao foi possivel identificar o veiculo", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            saving = true
            try {
                val response = RetrofitClient.cars.deleteCar(id)
                if (response.isSuccessful) {
                    setResult(RESULT_OK)
                    Toast.makeText(this@CarDetailActivity, "Veiculo excluido com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@CarDetailActivity,
                        response.toUserError("Nao foi possivel excluir o veiculo"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CarDetailActivity,
                    e.toUserError("Erro ao excluir veiculo"),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                saving = false
            }
        }
    }

    private fun openMap(place: Place?) {
        if (place == null) return

        val geoIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:${place.lat},${place.lng}?q=${place.lat},${place.lng}(Veiculo)")
        )
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=${place.lat},${place.lng}")
        )

        try {
            when {
                geoIntent.resolveActivity(packageManager) != null -> startActivity(geoIntent)
                webIntent.resolveActivity(packageManager) != null -> startActivity(webIntent)
                else -> Toast.makeText(this, "Nenhum aplicativo de mapas encontrado", Toast.LENGTH_LONG).show()
            }
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Nenhum aplicativo de mapas encontrado", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_CAR_ID = "car_id"
    }
}

@Composable
private fun CarDetailErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            FilledTonalButton(onClick = onRetry) {
                Text("Tentar novamente")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBack) {
                Text("Voltar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    car: Car,
    saving: Boolean,
    onBack: () -> Unit,
    onEdit: (Car) -> Unit,
    onDelete: () -> Unit,
    onOpenMap: (Place?) -> Unit
) {
    val hasLocation = car.place != null
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        car.name.ifEmpty { "Detalhes" },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(
                        onClick = { showEdit = true },
                        enabled = !saving && !car.id.isNullOrBlank()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(
                        onClick = { showDelete = true },
                        enabled = !saving && !car.id.isNullOrBlank()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(car.imageUrl)
                        .crossfade(true)
                        .size(Size(1080, 640))
                        .build(),
                    contentDescription = "Foto do ${car.name}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Imagem indisponivel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = car.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ano: ${car.year}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Placa: ${car.licence}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (hasLocation) {
                    val place = car.place!!
                    val latLng = LatLng(place.lat, place.lng)
                    var mapReady by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(150)
                        mapReady = true
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Localização",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (mapReady) {
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(latLng, 14f)
                            }
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState
                            ) {
                                Marker(
                                    state = MarkerState(position = latLng),
                                    title = car.name
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FilledTonalButton(onClick = { onOpenMap(car.place) }) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir no Google Maps")
                    }
                }
            }
        }
    }

    if (showEdit) {
        CarFormDialog(
            title = "Editar veiculo",
            confirmLabel = "Salvar",
            saving = saving,
            initial = car.toDraft(),
            carId = car.id,
            onDismiss = {
                if (!saving) showEdit = false
            },
            onSubmit = {
                showEdit = false
                onEdit(it)
            }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = {
                if (!saving) showDelete = false
            },
            title = { Text("Excluir veiculo") },
            text = { Text("Deseja realmente excluir este veiculo?") },
            confirmButton = {
                TextButton(
                    enabled = !saving,
                    onClick = {
                        showDelete = false
                        onDelete()
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !saving,
                    onClick = { showDelete = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
