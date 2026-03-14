# FTPR Car Android

Aplicativo Android desenvolvido como projeto final da disciplina de APIs para dispositivos móveis, com autenticação via Firebase, consumo de API REST para gerenciamento de veículos e visualização de localização no mapa.

Este `README` tem dois objetivos:

1. Descrever o que foi solicitado no projeto.
2. Documentar o que foi implementado e como instalar e executar a aplicação.

## 1. Objetivo do projeto

O projeto consiste em um aplicativo Android integrado com:

- Firebase Authentication para login por telefone.
- API REST `/car` para listar, cadastrar, consultar, editar e excluir veículos.
- Google Maps para exibir a localização associada a cada veículo.

## 2. Requisitos solicitados na atividade

Com base no guia do projeto, os itens pedidos foram:

### Obrigatórios

- Tela de login com Firebase.
- Opção de logout.
- Integração com a API REST `/car`.

### Opcionais

- Exibição da localização do veículo usando Google Maps.

## 3. O que foi realizado

### Funcionalidades implementadas

- Login com telefone usando Firebase Auth.
- Suporte ao fluxo de teste com número e código informados no guia.
- Redirecionamento automático para a tela principal após autenticação.
- Logout com limpeza da pilha de navegação.
- Listagem de veículos consumindo a API REST.
- Cadastro individual de veículo.
- Cadastro em lote de veículos usando o endpoint que recebe lista.
- Consulta de veículo por ID usando `GET /car/:id`.
- Tela de detalhes do veículo.
- Edição de veículo com atualização via API.
- Exclusão de veículo com confirmação.
- Atualização automática da lista após editar ou excluir, sem precisar reabrir telas.
- Exibição das imagens dos veículos na listagem, no cadastro e na tela de detalhes.
- Pré-visualização da imagem ao informar a URL.
- Exibição da localização do veículo.
- Carregamento sob demanda do Google Maps para melhorar desempenho.
- Ícone do aplicativo personalizado com tema de carro.

### Ajustes técnicos realizados

- Configuração da URL da API para o emulador Android usando `10.0.2.2`.
- Habilitação de `INTERNET` no manifesto.
- Liberação de tráfego HTTP local com `usesCleartextTraffic="true"`.
- Inclusão de dependências necessárias para `ComponentActivity`, Firebase Auth, Retrofit, Coil e Maps.
- Remoção da chave do Google Maps hardcoded, com leitura segura via `local.properties` ou variável de ambiente.
- Correção de problemas de import e compilação.
- Tratamento de carregamento e erro nas chamadas da API.
- Melhorias de feedback visual com `Snackbar`, `Toast`, `CircularProgressIndicator` e diálogos de confirmação.

### Ajustes realizados na API local

Na API Node/Express utilizada para testes locais, também foram aplicadas melhorias para estabilizar o fluxo do app:

- Carga automática dos veículos a partir do arquivo `content.json`.
- Persistência dos dados em `content.json` após cadastro, edição e exclusão.
- Suporte ao `POST /car` com objeto único e também com lista de objetos.
- Correção do `PATCH /car/:id` para manter o `id` do veículo corretamente.
- Disponibilização da documentação Swagger em `/api-docs`.

## 4. Tecnologias utilizadas

- Kotlin
- Jetpack Compose
- Material 3
- Firebase Authentication
- Retrofit
- OkHttp
- Coil
- Google Maps Compose
- Node.js
- Express
- Swagger

## 5. Estrutura resumida do projeto

```text
Projeto_Final_APIs/
├── app/
│   ├── src/main/java/com/example/ftprcar/
│   │   ├── data/
│   │   │   ├── api/
│   │   │   └── model/
│   │   └── ui/
│   │       ├── login/
│   │       ├── main/
│   │       └── car/
│   └── google-services.json
├── repositorio/
│   └── FTPR-Car-Api-Node-Express/
└── README.md
```

Neste projeto, a pasta `repositorio` foi adicionada para armazenar a API local utilizada durante os testes do aplicativo. Dentro dela está o projeto `FTPR-Car-Api-Node-Express`, desenvolvido com Node.js e Express, responsável por disponibilizar os endpoints REST consumidos pelo app Android.

Essa organização permite manter no mesmo ambiente:

- o aplicativo Android, na pasta principal do projeto;
- a API local de apoio, dentro de `repositorio/FTPR-Car-Api-Node-Express`.

Na prática, isso facilita a execução local, porque o backend necessário para testar a aplicação já fica junto do projeto e pode ser iniciado separadamente quando necessário.

## 6. Requisitos para executar

Antes de rodar o projeto, é recomendado ter instalado:

- Android Studio
- Android SDK 34
- JDK 17
- Emulador Android configurado no Android Studio
- Node.js 18 ou superior
- NPM

## 7. Configuração do Firebase

O aplicativo utiliza autenticação por telefone com Firebase.

Para funcionar corretamente:

1. Crie ou abra um projeto no Firebase Console.
2. Cadastre o aplicativo Android com o pacote `com.example.ftprcar`.
3. Baixe o arquivo `google-services.json`.
4. Coloque esse arquivo dentro da pasta `app/`.
5. Em `Authentication`, habilite o login por telefone.
6. Configure o número de teste:
   - Número: `+5511912345678`
   - Código: `101010`

Observações:

- O arquivo `google-services.json` deve permanecer apenas no ambiente local e não deve ser versionado.
- Se o projeto for configurado em outro ambiente, use um `google-services.json` próprio do seu Firebase.

## 8. Configuração do Google Maps

Para usar o mapa, defina a chave no arquivo `local.properties` da raiz do projeto:

```properties
GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

Também é possível fornecer a chave pela variável de ambiente `GOOGLE_MAPS_API_KEY`.

Opcionalmente, a URL da API tambem pode ser configurada em `local.properties`:

```properties
API_BASE_URL=http://10.0.2.2:3000/
```

Se esse valor nao for informado, o app usa `http://10.0.2.2:3000/` por padrao.

## 9. Como rodar a API local

A API usada para testes está dentro da pasta `repositorio`, que foi incluída no projeto justamente para concentrar também o backend local utilizado no desenvolvimento e na validação das funcionalidades da aplicação.

O caminho da API é:

`repositorio/FTPR-Car-Api-Node-Express`

### Passos

1. Abra um terminal na raiz do projeto.
2. Acesse a pasta da API:

```bash
cd repositorio/FTPR-Car-Api-Node-Express
```

3. Instale as dependências:

```bash
npm install
```

4. Inicie o servidor:

```bash
node index.js
```

5. Confirme no terminal que a API está rodando em:

```text
http://localhost:3000
```

### Documentação da API

Com a API em execução, a documentação Swagger pode ser acessada em:

- [http://localhost:3000/api-docs](http://localhost:3000/api-docs)

### Persistência dos dados

Os veículos ficam armazenados no arquivo:

`repositorio/FTPR-Car-Api-Node-Express/content.json`

Isso significa que:

- a lista inicial é carregada desse arquivo ao iniciar a API;
- novos cadastros são salvos nele;
- edições e exclusões também atualizam esse arquivo.

## 10. Como rodar o aplicativo Android

### Passos

1. Abra a pasta do projeto `Projeto_Final_APIs` no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Inicie um emulador Android.
4. Verifique se a API local já está rodando na porta `3000`.
5. Execute o app pelo Android Studio.

### Importante sobre a URL da API

O app está configurado para usar:

```text
http://10.0.2.2:3000/
```

Esse endereço funciona no emulador Android e aponta para o `localhost` da máquina hospedeira.

Se for testar em dispositivo físico, será necessário trocar a base URL pela URL da máquina na rede local.

## 11. Fluxo de uso da aplicação

### Login

1. Abra o app.
2. Informe o telefone de teste:
   - `+5511912345678`
3. Envie o código.
4. Informe o código:
   - `101010`
5. Após autenticar, o app abre a tela principal.

### Tela principal

Na tela principal é possível:

- visualizar a lista de veículos;
- atualizar a listagem;
- cadastrar um veículo;
- cadastrar vários veículos em lote;
- abrir os detalhes de um veículo;
- sair da conta.

### Cadastro individual

O cadastro individual solicita:

- nome;
- ano;
- placa;
- URL da imagem;
- latitude;
- longitude.

### Cadastro em lote

Foi implementado um cadastro em lote para utilizar o endpoint de múltiplos veículos.

Formato aceito no campo de lote:

```text
nome;ano;placa;urlImagem;latitude;longitude
```

Exemplo:

```text
Fusca;1978/1979;ABC-1234;https://site.com/fusca.jpg;-23.55;-46.63
Gol;2020/2021;DEF-5678;https://site.com/gol.jpg;-22.90;-43.20
```

### Tela de detalhes

Na tela de detalhes:

- os dados são carregados pelo `id` usando `GET /car/:id`;
- a imagem é exibida em destaque;
- os dados do veículo podem ser editados;
- o veículo pode ser excluído;
- a localização pode ser visualizada;
- o mapa é carregado apenas quando o usuário solicita.

## 12. Endpoints utilizados pelo app

Os endpoints principais da API são:

- `GET /car` -> lista todos os veículos
- `GET /car/:id` -> busca um veículo específico
- `POST /car` -> cadastra um veículo
- `POST /car` com lista -> cadastra vários veículos
- `PATCH /car/:id` -> atualiza um veículo
- `DELETE /car/:id` -> remove um veículo

## 13. Observações importantes

- O projeto foi testado com o emulador Android Studio, por isso a configuração usa `10.0.2.2`.
- O login depende de configuração correta do Firebase.
- O Google Maps depende de uma chave válida configurada localmente.
- O mapa foi implementado como recurso opcional, conforme permitido no guia.
- O backend local precisa estar em execução para que o app carregue, cadastre, edite e exclua veículos.

## 14. Status de entrega em relação ao guia

- [x] Login com Firebase
- [x] Logout
- [x] Integração com API REST `/car`
- [x] Listagem de veículos
- [x] Exibição de imagens
- [x] Consulta de veículo por ID
- [x] Cadastro de veículo
- [x] Cadastro em lote
- [x] Edição de veículo
- [x] Exclusão de veículo
- [x] Atualização da lista após mudanças
- [x] Google Maps
- [x] Ícone personalizado

## 15. Considerações finais

O projeto atende ao escopo principal solicitado no guia, contemplando autenticação, consumo de API REST, operações CRUD de veículos e visualização de localização. Além disso, foram adicionadas melhorias de usabilidade e estabilidade para tornar a aplicação mais completa e consistente durante a execução local.
