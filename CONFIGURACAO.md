# Configuração do Projeto FTPR Car

## 1. Firebase (obrigatório)

1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Crie um projeto ou use existente
3. Adicione um app **Android** com package name: `com.example.ftprcar`
4. Baixe o `google-services.json` e coloque na pasta `app/`
5. Em **Authentication** > **Sign-in method** > **Phone** → habilite
6. Em **Phone numbers for testing** adicione:
   - Número: `+5511912345678`
   - Código: `101010`

## 2. Google Maps (opcional)

1. No [Google Cloud Console](https://console.cloud.google.com/), habilite a API "Maps SDK for Android"
2. Crie uma chave de API
3. Adicione a chave no arquivo `local.properties` da raiz do projeto:

```properties
GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

4. O `local.properties` e o `google-services.json` devem permanecer locais e não devem ser versionados

## 3. URL da API (opcional)

Se precisar testar em outro ambiente, adicione tambem no `local.properties`:

```properties
API_BASE_URL=http://10.0.2.2:3000/
```

Para dispositivo fisico, troque pelo IP da sua maquina na rede local.

## 4. API Node Express (para testes)

Para testar a listagem de carros:

```bash
git clone https://github.com/vagnnermartins/FTPR-Car-Api-Node-Express.git
cd FTPR-Car-Api-Node-Express
npm install
node index.js
```

A API rodará em `http://localhost:3000`. O app usa `10.0.2.2:3000` (localhost do emulador).

Para dispositivo físico, altere a URL em `RetrofitClient.kt` para o IP da sua máquina na rede.
