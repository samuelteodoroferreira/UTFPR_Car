const express = require('express');
const fs = require('fs');
const path = require('path');
const swaggerJsdoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');
const app = express();
const port = 3000;

const swaggerDefinition = {
    openapi: '3.0.0',
    info: {
        title: 'API de Carros',
        version: '1.0.0',
        description: 'API para gerenciar dados de carros.',
    },
    servers: [
        {
            url: `http://localhost:${port}`,
            description: 'Servidor local',
        },
    ],
};

const options = {
    swaggerDefinition,
    apis: ['./index.js'],
};

const swaggerSpec = swaggerJsdoc(options);

app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));
app.use(express.json());

const carMap = new Map();
const contentFilePath = path.join(__dirname, 'content.json');

const validateCar = (car) => {
    const { id, imageUrl, year, name, licence, place } = car;
    return (
        id &&
        imageUrl &&
        year &&
        name &&
        licence &&
        place &&
        place.lat !== undefined &&
        place.long !== undefined
    );
};

const saveCarsToFile = () => {
    const cars = Array.from(carMap.values());
    fs.writeFileSync(contentFilePath, JSON.stringify(cars, null, 2));
};

const loadCarsFromFile = () => {
    if (!fs.existsSync(contentFilePath)) {
        return;
    }

    const fileContent = fs.readFileSync(contentFilePath, 'utf-8').trim();
    if (!fileContent) {
        return;
    }

    const cars = JSON.parse(fileContent);
    if (!Array.isArray(cars)) {
        throw new Error('content.json deve conter uma lista de carros');
    }

    for (const car of cars) {
        if (validateCar(car)) {
            carMap.set(car.id, car);
        }
    }
};

loadCarsFromFile();

/**
 * @swagger
 * components:
 *   schemas:
 *     Car:
 *       type: object
 *       required:
 *         - id
 *         - imageUrl
 *         - year
 *         - name
 *         - licence
 *         - place
 *       properties:
 *         id:
 *           type: string
 *           description: ID do carro
 *         imageUrl:
 *           type: string
 *           description: URL da imagem do carro
 *         year:
 *           type: string
 *           description: Ano do carro no formato '2020/2020'
 *         name:
 *           type: string
 *           description: Nome do carro
 *         licence:
 *           type: string
 *           description: Placa do carro
 *         place:
 *           type: object
 *           properties:
 *             lat:
 *               type: number
 *               description: Latitude do local
 *             long:
 *               type: number
 *               description: Longitude do local
 *       example:
 *         id: "001"
 *         imageUrl: "https://image"
 *         year: "2020/2020"
 *         name: "Gaspar"
 *         licence: "ABC-1234"
 *         place:
 *           lat: 0
 *           long: 0
 */

/**
 * @swagger
 * /car:
 *   post:
 *     summary: Adiciona um novo carro ou uma lista de carros
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             oneOf:
 *               - $ref: '#/components/schemas/Car'
 *               - type: array
 *                 items:
 *                   $ref: '#/components/schemas/Car'
 *     responses:
 *       201:
 *         description: Carro(s) adicionado(s) com sucesso
 *       400:
 *         description: Erro de validação nos dados fornecidos
 */
app.post('/car', (req, res) => {
    const body = req.body;

    if (Array.isArray(body)) {
        const errors = [];
        const batchIds = new Set();

        for (const car of body) {
            if (!validateCar(car)) {
                errors.push({ car, error: 'JSON inválido ou incompleto' });
            } else if (carMap.has(car.id)) {
                errors.push({ id: car.id, error: 'ID já existe' });
            } else if (batchIds.has(car.id)) {
                errors.push({ id: car.id, error: 'ID duplicado no lote' });
            } else {
                batchIds.add(car.id);
            }
        }

        if (errors.length > 0) {
            return res.status(400).json({ errors });
        }

        for (const car of body) {
            carMap.set(car.id, car);
        }

        saveCarsToFile();
        return res.status(201).json(body);
    } else {
        if (!validateCar(body)) {
            return res.status(400).json({ error: 'JSON inválido ou incompleto' });
        }

        if (carMap.has(body.id)) {
            return res.status(400).json({ error: 'ID já existe' });
        }

        carMap.set(body.id, body);
        saveCarsToFile();
        return res.status(201).json(body);
    }
});


/**
 * @swagger
 * /car:
 *   get:
 *     summary: Retorna a lista de todos os carros
 *     responses:
 *       200:
 *         description: Lista de carros retornada com sucesso
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/Car'
 */
app.get('/car', (req, res) => {
    const cars = Array.from(carMap.values());
    res.json(cars);
});


/**
 * @swagger
 * /car/{id}:
 *   get:
 *     summary: Retorna os dados de um carro específico
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *         description: ID do carro
 *     responses:
 *       200:
 *         description: Dados do carro retornados com sucesso
 *       404:
 *         description: Carro não encontrado
 */
app.get('/car/:id', (req, res) => {
    const id = req.params.id;

    if (carMap.has(id)) {
        res.json(carMap.get(id));
    } else {
        res.status(404).json({ error: 'Carro não encontrado' });
    }
});

/**
 * @swagger
 * /car/{id}:
 *   patch:
 *     summary: Atualiza os dados de um carro específico
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *         description: ID do carro
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/Car'
 *     responses:
 *       200:
 *         description: Carro atualizado com sucesso
 *       400:
 *         description: Erro de validação nos dados fornecidos
 *       404:
 *         description: Carro não encontrado
 */
app.patch('/car/:id', (req, res) => {
    const id = req.params.id;
    const { imageUrl, year, name, licence, place } = req.body;

    if (!carMap.has(id)) {
        return res.status(404).json({ error: 'Carro não encontrado' });
    }

    if (!imageUrl || !year || !name || !licence || !place || place.lat == undefined || place.long == undefined) {
        return res.status(400).json({ error: 'JSON inválido ou incompleto' });
    }

    const updatedCar = { ...req.body, id };
    carMap.set(id, updatedCar);
    saveCarsToFile();
    res.json(updatedCar);
});

/**
 * @swagger
 * /car/{id}:
 *   delete:
 *     summary: Remove um carro específico
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *         description: ID do carro
 *     responses:
 *       200:
 *         description: Carro deletado com sucesso
 *       404:
 *         description: Carro não encontrado
 */
app.delete('/car/:id', (req, res) => {
    const id = req.params.id;

    if (carMap.has(id)) {
        carMap.delete(id);
        saveCarsToFile();
        res.json({ message: 'Carro deletado com sucesso' });
    } else {
        res.status(404).json({ error: 'Carro não encontrado' });
    }
});

app.listen(port, () => {
    console.log(`Servidor rodando em http://localhost:${port}`);
});
