@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ftprcar.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.ftprcar.data.api.RetrofitClient
import com.example.ftprcar.data.api.toUserError
import com.example.ftprcar.data.model.Car
import com.example.ftprcar.data.model.Place
import com.example.ftprcar.ui.car.CarDetailActivity
import com.example.ftprcar.ui.car.CarFormDialog
import com.example.ftprcar.ui.car.validateCarInput
import com.example.ftprcar.ui.login.LoginActivity
import com.example.ftprcar.ui.theme.FTPRCarTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

private const val ExtraCarId = "car_id"
private const val BulkFormat = "nome;ano;placa;urlImagem;latitude;longitude"
private const val BulkPlaceholder = "Fusca;1978/1979;ABC-1234;https://site.com/fusca.jpg;-23.55;-46.63"

class MainActivity : ComponentActivity() {
    private var refreshKey by mutableIntStateOf(0)

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) refreshKey++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FTPRCarTheme {
                MainScreen(
                    refreshKey = refreshKey,
                    onCarClick = ::openDetail,
                    onLogout = ::logout
                )
            }
        }
    }

    private fun openDetail(car: Car) {
        detailLauncher.launch(
            Intent(this, CarDetailActivity::class.java).apply {
                putExtra(ExtraCarId, car.id)
            }
        )
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    refreshKey: Int,
    onCarClick: (Car) -> Unit,
    onLogout: () -> Unit
) {
    var cars by remember { mutableStateOf(emptyList<Car>()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showLogout by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var showBulkCreate by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val actionsEnabled = !saving

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                cars = RetrofitClient.cars.listCars()
            } catch (e: Exception) {
                val message = e.toUserError("Falha ao carregar dados da API")
                error = message
                snack.showSnackbar(message)
            } finally {
                loading = false
            }
        }
    }

    fun create(car: Car) {
        scope.launch {
            saving = true
            try {
                val response = RetrofitClient.cars.createCar(car)
                if (response.isSuccessful) {
                    showCreate = false
                    snack.showSnackbar("Veiculo cadastrado com sucesso")
                    refresh()
                } else {
                    snack.showSnackbar(response.toUserError("Nao foi possivel cadastrar o veiculo"))
                }
            } catch (e: Exception) {
                snack.showSnackbar(e.toUserError("Erro ao cadastrar veiculo"))
            } finally {
                saving = false
            }
        }
    }

    fun createMany(batch: List<Car>) {
        scope.launch {
            saving = true
            try {
                val response = RetrofitClient.cars.createCars(batch)
                if (response.isSuccessful) {
                    showBulkCreate = false
                    snack.showSnackbar("${batch.size} veiculos cadastrados com sucesso")
                    refresh()
                } else {
                    snack.showSnackbar(response.toUserError("Nao foi possivel cadastrar os veiculos"))
                }
            } catch (e: Exception) {
                snack.showSnackbar(e.toUserError("Erro ao cadastrar veiculos"))
            } finally {
                saving = false
            }
        }
    }

    LaunchedEffect(refreshKey) { refresh() }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Sair", fontWeight = FontWeight.SemiBold) },
            text = { Text("Deseja encerrar a sessão?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogout = false
                        onLogout()
                    }
                ) {
                    Text("Sim", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) {
                    Text("Nao")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showCreate) {
        CarFormDialog(
            title = "Cadastrar veiculo",
            confirmLabel = "Salvar",
            saving = saving,
            onDismiss = {
                if (!saving) showCreate = false
            },
            onSubmit = ::create
        )
    }

    if (showBulkCreate) {
        BulkCreateDialog(
            saving = saving,
            onDismiss = {
                if (!saving) showBulkCreate = false
            },
            onSubmit = ::createMany
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snack) },
        topBar = {
            TopAppBar(
                title = { Text("Carros", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(
                        onClick = { showCreate = true },
                        enabled = actionsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Cadastrar veiculo",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    TextButton(
                        onClick = { showBulkCreate = true },
                        enabled = actionsEnabled
                    ) {
                        Text("Lote", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(
                        onClick = ::refresh,
                        enabled = actionsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = { showLogout = true },
                        enabled = actionsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sair",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                loading -> LoadingState()
                error != null -> ErrorState(
                    message = error.orEmpty(),
                    onRetry = ::refresh
                )
                cars.isEmpty() -> EmptyState()
                else -> CarList(
                    cars = cars,
                    onCarClick = onCarClick
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Carregando carros...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Tentar novamente")
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nenhum carro encontrado",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CarList(
    cars: List<Car>,
    onCarClick: (Car) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = cars
        ) { car ->
            CarItem(
                car = car,
                onClick = { onCarClick(car) }
            )
        }
    }
}

@Composable
private fun BulkCreateDialog(
    saving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (List<Car>) -> Unit
) {
    var raw by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar veiculos em lote", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Informe um veiculo por linha no formato: $BulkFormat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = raw,
                    onValueChange = {
                        raw = it
                        error = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp),
                    enabled = !saving,
                    placeholder = { Text(BulkPlaceholder) }
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
                    val batch = raw.parseBatch()
                    when {
                        batch.error != null -> error = batch.error
                        batch.cars.isEmpty() -> error = "Informe ao menos um veiculo."
                        else -> onSubmit(batch.cars)
                    }
                }
            ) {
                Text(if (saving) "Salvando..." else "Salvar lote")
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

private data class BatchParseResult(
    val cars: List<Car> = emptyList(),
    val error: String? = null
)

private fun String.parseBatch(): BatchParseResult {
    if (isBlank()) return BatchParseResult()

    val items = mutableListOf<Car>()
    val lines = lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .toList()

    for ((index, line) in lines.withIndex()) {
        val parts = line.split(';').map(String::trim)
        if (parts.size != 6) {
            return BatchParseResult(
                error = "Linha ${index + 1}: use 6 campos separados por ponto e virgula."
            )
        }

        val lat = parts[4].replace(',', '.').toDoubleOrNull()
        val lng = parts[5].replace(',', '.').toDoubleOrNull()
        val validationError = validateCarInput(
            name = parts[0],
            year = parts[1],
            licence = parts[2],
            imageUrl = parts[3],
            lat = lat,
            lng = lng
        )

        if (validationError != null) {
            return BatchParseResult(error = "Linha ${index + 1}: $validationError")
        }

        items += Car(
            id = UUID.randomUUID().toString(),
            name = parts[0],
            year = parts[1],
            licence = parts[2],
            imageUrl = parts[3],
            place = Place(lat = lat!!, lng = lng!!)
        )
    }

    return BatchParseResult(cars = items)
}

@Composable
fun CarItem(
    car: Car,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(car.imageUrl)
                    .crossfade(true)
                    .size(Size(192, 192))
                    .build(),
                contentDescription = "Foto do ${car.name}",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
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
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = car.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = car.year,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = car.licence,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
