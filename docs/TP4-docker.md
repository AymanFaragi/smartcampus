# TP 4 — Conteneuriser SmartCampus avec Docker

## Fichiers

| Fichier | Rôle |
|---|---|
| `Dockerfile` | Image `smartcampus:1.0` à partir du JAR produit par Jenkins (`target/smartcampus-0.1.0.jar`) |
| `.dockerignore` | N'envoie au build que le JAR (contexte léger) |
| `compose.yaml` | Services `api` + `db` (PostgreSQL 13), volume `postgres_data`, healthchecks |

Modifications de l'application nécessaires pour PostgreSQL :

- `pom.xml` : ajout de `postgresql` (driver) et `spring-boot-starter-actuator` (endpoint `/actuator/health`).
- `application.properties` : la datasource lit `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DDL_AUTO`
  (H2 en mémoire par défaut, donc les tests et Jenkins fonctionnent sans base).

> Le port 8080 de l'hôte est occupé par Jenkins : on publie donc le port conteneur 8080 sur le port hôte 8081.

## 1. Construire l'image

```bash
mvn -B clean package -DskipTests        # même commande que le stage Build de Jenkins
docker build -t smartcampus:1.0 .
docker images smartcampus
```

## 2. Lancer le conteneur (ports + variables)

```bash
docker run -d --name smartcampus -p 8081:8080 smartcampus:1.0
curl http://localhost:8081/actuator/health     # {"status":"UP"}
docker ps                                      # STATUS : (healthy)
docker rm -f smartcampus
```

Variables d'environnement : `-e DB_URL=... -e DB_USER=... -e DB_PASSWORD=...`

## 3. Volume + Compose (api + PostgreSQL)

```bash
docker compose up -d --build
docker compose ps                  # api et db : (healthy)
docker volume ls                   # smartcampus2_postgres_data
```

L'API joint la base par son nom DNS de service : `jdbc:postgresql://db:5432/smartcampus`.
`depends_on: condition: service_healthy` attend que PostgreSQL soit prêt avant de démarrer l'API.

Persistance vérifiée : une donnée insérée reste présente après `docker compose down` puis `up`
(seul `docker compose down -v` supprime le volume).

## 4. Healthcheck et logs

```bash
docker inspect --format '{{.State.Health.Status}}' smartcampus2-api-1
docker compose logs api
docker compose logs -f api         # suivi en temps réel (Ctrl+C pour quitter)
docker compose logs --tail 50 db
```

## Optimisations

- Image JRE Alpine (pas de JDK ni Maven dans l'image finale).
- `.dockerignore` : contexte de build réduit au JAR.
- Exécution avec un utilisateur non-root (`app`).
- `HEALTHCHECK` dans le Dockerfile (utile aussi avec `docker run`) et dans Compose.
