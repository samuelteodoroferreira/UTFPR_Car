
# API de Carros

Este é um projeto de API RESTful desenvolvido com Node.js e Express para gerenciar uma lista de carros. A API permite adicionar, recuperar e excluir carros, além de suportar a validação de dados.

## Pré-requisitos

Antes de começar, você precisará ter instalado em sua máquina:

- [Node.js](https://nodejs.org/) (v14 ou superior)
- [npm](https://www.npmjs.com/) (geralmente vem junto com o Node.js)

## Instalação

1. **Clone o repositório**

   Abra o terminal e execute o seguinte comando para clonar o repositório:

   ```bash
   git clone https://github.com/vagnnermartins/FTPR-Car-Api-Node-Express.git
   cd seurepositorio
   ```

2. **Instale as dependências**

   No diretório do projeto, execute o seguinte comando para instalar as dependências necessárias:

   ```bash
   npm install
   ```

## Inicialização

Após a instalação das dependências, você pode iniciar o servidor com o seguinte comando:

```bash
node index.js
```

O servidor será iniciado e ficará escutando na porta `3000` por padrão. Você verá uma mensagem no terminal indicando que o servidor está rodando:

```
Servidor rodando em http://localhost:3000
```

## Endpoints da API

Aqui estão os principais endpoints disponíveis na API:

### `GET /car`
Retorna a lista de todos os carros.

### `GET /car/:id`
Retorna os dados de um carro específico com o ID fornecido.

### `POST /car`
Adiciona um novo carro ou uma lista de carros. O corpo da requisição deve conter um objeto ou um array de objetos com a seguinte estrutura:

```json
{
    "id": "001",
    "imageUrl": "https://example.com/car.jpg",
    "year": "2020/2021",
    "name": "Carro A",
    "licence": "ABC-1234",
    "place": { "lat": -23.5505, "long": -46.6333 }
}
```

### `DELETE /car/:id`
Remove um carro específico com o ID fornecido.

### `PATCH /car/:id`
Atualiza os dados de um carro específico com o ID fornecido. O corpo da requisição deve conter um objeto com as propriedades a serem atualizadas.

## Documentação da API

A documentação da API está disponível na interface do Swagger. Você pode acessá-la em:

```
http://localhost:3000/api-docs
```


### Como usar

1. **Substitua `seuusuario` e `seurepositorio` pelo seu nome de usuário do GitHub e o nome do repositório.**
2. **Adapte o texto conforme necessário para refletir características específicas do seu projeto.** 

Esse README fornece informações claras sobre como instalar e usar sua API, além de uma breve descrição dos endpoints disponíveis.

## Exemplo de JSON

Um exemplo de estrutura de JSON para os dados dos carros pode ser encontrado no arquivo [content.json](content.json) na raiz do projeto. Este arquivo fornece um modelo que você pode usar ao fazer requisições para a API, seja para adicionar um único carro ou uma lista de carros.
