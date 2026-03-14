# Exemplos de Código - Projeto Final

Referência de implementação para cada funcionalidade.

---

## 1. Models (Data Classes)

### Car.kt
```kotlin
data class Car(
    val id: String? = null,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place
)

data class Place(
    val lat: Double,
    val long: Double
)
```

---

## 2. Interface Retrofit

### CarApiService.kt
```kotlin
interface CarApiService {
    @GET("car")
    suspend fun getCars(): List<Car>

    @GET("car/{id}")
    suspend fun getCar(@Path("id") id: String): Car

    @POST("car")
    suspend fun addCar(@Body car: Car): Response<Unit>

    @POST("car")
    suspend fun addCars(@Body cars: List<Car>): Response<Unit>
}
```

### Retrofit Instance
```kotlin
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"  // Emulador

    val carApi: CarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CarApiService::class.java)
    }
}
```

---

## 3. Login com Telefone (Firebase)

```kotlin
// Enviar código de verificação
val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        signInWithPhoneAuthCredential(credential)
    }
    override fun onVerificationFailed(e: FirebaseException) {
        // Tratar erro
    }
    override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        // Salvar verificationId e mostrar campo de código
        this.verificationId = verificationId
    }
}

PhoneAuthProvider.verifyPhoneNumber(
    PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
        .setPhoneNumber("+5511912345678")
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(this)
        .setCallbacks(callbacks)
        .build()
)

// Verificar código (101010 nos testes)
val credential = PhoneAuthProvider.getCredential(verificationId, "101010")
FirebaseAuth.getInstance().signInWithCredential(credential)
    .addOnSuccessListener { /* Ir para tela principal */ }
```

---

## 4. Logout

```kotlin
FirebaseAuth.getInstance().signOut()
// Redirecionar para LoginActivity
startActivity(Intent(this, LoginActivity::class.java))
finish()
```

---

## 5. Carregar imagem com Picasso

```kotlin
Picasso.get()
    .load(car.imageUrl)
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error)
    .into(imageView)
```

---

## 6. Configurar número de teste no Firebase

No Firebase Console:
1. **Authentication** > **Sign-in method** > **Phone**
2. Role até **Phone numbers for testing**
3. Adicione: `+5511912345678` com código `101010`

Com isso, ao usar esse número, o Firebase usará automaticamente o código 101010 sem enviar SMS real.
