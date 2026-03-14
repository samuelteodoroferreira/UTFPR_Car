# Guia do Projeto Final: APIs de Desenvolvimento para Dispositivos Móveis

Este guia consolida as tarefas da atividade final e orienta a implementação do aplicativo Android com integração Firebase e API REST.

---

## 📋 Resumo das Tarefas

| Tarefa | Descrição | Prioridade |
|--------|-----------|------------|
| 1 | Tela de Login com Firebase (telefone/Google) | Obrigatório |
| 2 | Opção de Logout | Obrigatório |
| 3 | Integração API REST `/car` | Obrigatório |
| 4 | Google Maps (localização do place) | Opcional |

---

## 🚀 Passo 1: Preparação do Ambiente

### 1.1 Clone os repositórios

```bash
# Clone o projeto Android (faça um fork primeiro no GitHub)
git clone https://github.com/vagnnermartins/FTPR-Car-Android.git
cd FTPR-Car-Android

# Em outro diretório - API para testes locais (se necessário)
git clone https://github.com/vagnnermartins/FTPR-Car-Api-Node-Express.git
cd FTPR-Car-Api-Node-Express
npm install
node index.js
# API rodará em http://localhost:3000
```

### 1.2 Dependências Gradle

No `app/build.gradle` (módulo app), adicione:

```gradle
dependencies {
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-storage'

    // Retrofit - API REST
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

    // Carregamento de imagens
    implementation 'com.squareup.picasso:picasso:2.8'

    // Google Maps (opcional)
    implementation 'com.google.android.gms:play-services-maps:18.2.0'
}
```

No `build.gradle` (projeto) - plugin do Google Services:
```gradle
plugins {
    id 'com.google.gms.google-services' version '4.4.0' apply false
}
```

---

## 🔐 Tarefa 1: Tela de Login com Firebase

### Configuração para testes

| Configuração | Valor |
|--------------|-------|
| **Número de telefone** | +5511912345678 |
| **Código de verificação** | 101010 |

### 1.1 Configurar Firebase Console

1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Crie um projeto ou use um existente
3. Adicione um app Android (pacote do seu app)
4. Baixe o `google-services.json` e coloque em `app/`
5. Em **Authentication** > **Sign-in method** > **Phone** → habilite
6. Para testes com número fixo: em **Phone numbers for testing**, adicione:
   - Número: `+5511912345678`
   - Código: `101010`

### 1.2 Estrutura do JSON esperada (carros)

```json
{
  "id": "001",
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "year": "2020/2021",
  "name": "Carro A",
  "licence": "ABC-1234",
  "place": {
    "lat": -23.5505,
    "long": -46.6333
  }
}
```

> ⚠️ O campo `imageUrl` deve ser uma URL de imagem no Firebase Storage.

---

## 🚪 Tarefa 2: Logout

- Adicione um **botão ou item de menu** para logout
- Use: `FirebaseAuth.getInstance().signOut()`
- Após logout, redirecione para a tela de login

---

## 🌐 Tarefa 3: Integração com API REST

### Base URL da API

- **Produção (API hospedada)**: verifique se existe URL pública ou use emulador
- **Local**: `http://10.0.2.2:3000` (emulador Android = localhost da máquina)

### Endpoints principais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/car` | Lista todos os carros |
| GET | `/car/:id` | Carro específico |
| POST | `/car` | Adicionar carro(s) |
| DELETE | `/car/:id` | Remover carro |
| PATCH | `/car/:id` | Atualizar carro |

### Fluxo recomendado

1. **Models (Data classes)** – representar Car e Place
2. **Retrofit Interface** – definir endpoints
3. **Repository/Service** – chamadas à API
4. **ViewModel** – lógica e estado
5. **UI** – RecyclerView para lista, Picasso para imagens

---

## 🗺️ Tarefa 4 (Opcional): Google Maps

- Usar coordenadas `place.lat` e `place.long` de cada carro
- Habilitar Google Maps API e obter chave no Google Cloud Console
- Adicionar no `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="SUA_CHAVE_GOOGLE_MAPS"/>
```

---

## 📁 Estrutura sugerida do projeto

```
app/
├── data/
│   ├── model/
│   │   ├── Car.kt
│   │   └── Place.kt
│   ├── api/
│   │   └── CarApiService.kt
│   └── repository/
│       └── CarRepository.kt
├── ui/
│   ├── login/
│   │   └── LoginActivity.kt
│   ├── main/
│   │   └── MainActivity.kt
│   └── car/
│       ├── CarAdapter.kt
│       └── CarViewModel.kt
└── ...
```

---

## ✅ Checklist de entrega

- [ ] Fork do FTPR-Car-Android no seu GitHub
- [ ] Login funcionando com +5511912345678 / 101010
- [ ] Logout implementado
- [ ] Listagem de carros da API
- [ ] Exibição de imagens (imageUrl do Firebase Storage)
- [ ] (Opcional) Google Maps com localização do place
- [ ] Código organizado e testado
- [ ] README atualizado com instruções

---

## 🔗 Links úteis

- [FTPR-Car-Android](https://github.com/vagnnermartins/FTPR-Car-Android) – Projeto base
- [FTPR-Car-Api-Node-Express](https://github.com/vagnnermartins/FTPR-Car-Api-Node-Express) – API REST
- [Firebase Auth - Phone](https://firebase.google.com/docs/auth/android/phone-auth)
- [Retrofit](https://square.github.io/retrofit/)
- [Picasso](https://square.github.io/picasso/)

---

> **Nota**: Não é necessário publicar um novo servidor Node Express. O app deve se conectar à API disponibilizada (ou à instância local para testes).
