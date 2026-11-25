```java
███████╗██╗   ██╗██████╗ ███████╗██████╗  ██████╗ ██████╗ ███╗   ██╗██████╗ ██╗   ██╗ ██████╗████████╗ ██████╗ ██████╗
██╔════╝██║   ██║██╔══██╗██╔════╝██╔══██╗██╔════╝██╔═══██╗████╗  ██║██╔══██╗██║   ██║██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗
███████╗██║   ██║██████╔╝█████╗  ██████╔╝██║     ██║   ██║██╔██╗ ██║██║  ██║██║   ██║██║        ██║   ██║   ██║██████╔╝
╚════██║██║   ██║██╔═══╝ ██╔══╝  ██╔══██╗██║     ██║   ██║██║╚██╗██║██║  ██║██║   ██║██║        ██║   ██║   ██║██╔══██╗
███████║╚██████╔╝██║     ███████╗██║  ██║╚██████╗╚██████╔╝██║ ╚████║██████╔╝╚██████╔╝╚██████╗   ██║   ╚██████╔╝██║  ██║
╚══════╝ ╚═════╝ ╚═╝     ╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═════╝ ╚═╝  ╚═══╝╚═════╝  ╚═════╝  ╚═════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝
```
# Java Nostr-Relay Framework & Web Application

### Supported Nips
  - [NIP-01](https://nostr-nips.com/nip-01) (Basic protocol & Standard Tags)
  - [NIP-02](https://nostr-nips.com/nip-02) (Contact List and Petnames)
  - [NIP-03](https://nostr-nips.com/nip-03) (OpenTimestamps Attestations for Events)
  - [NIP-04](https://nostr-nips.com/nip-04) (Encrypted Direct Message)
  - [NIP-05](https://nostr-nips.com/nip-05) (DNS-based internet identifiers)
  - [NIP-06](https://nostr-nips.com/nip-06) (Basic key derivation from mnemonic seed phrase)
  - [NIP-07](https://nostr-nips.com/nip-07) (window.nostr capability for web browsers)
  - [NIP-08](https://nostr-nips.com/nip-08) (Handling Mentions)
  - [NIP-10](https://nostr-nips.com/nip-10) (Marked "e" tags)
  - [NIP-11](https://nostr-nips.com/nip-11) (Relay Information Document)
  - [NIP-12](https://nostr-nips.com/nip-12) (Generic Tag Queries)
  - [NIP-14](https://nostr-nips.com/nip-14) (Subject tag in Text events)
  - [NIP-16](https://nostr-nips.com/nip-16) (Event treatment)
  - [NIP-18](https://nostr-nips.com/nip-18) (Reposts)
  - [NIP-19](https://nostr-nips.com/nip-19) (Bech-32 encoded entities)
  - [NIP-20](https://nostr-nips.com/nip-20) (Command Results)
  - [NIP-21](https://nostr-nips.com/nip-21) (URI scheme)
  - [NIP-22](https://nostr-nips.com/nip-22) (Event "created_at" limits)
  - [NIP-25](https://nostr-nips.com/nip-25) (Reactions)
  - [NIP-27](https://nostr-nips.com/nip-27) (Text Note References)
  - [NIP-28](https://nostr-nips.com/nip-28) (Public Chat)
  - [NIP-30](https://nostr-nips.com/nip-30) (Custom Emoji)
  - [NIP-31](https://nostr-nips.com/nip-31) (Unknown event kinds)
  - [NIP-33](https://nostr-nips.com/nip-33) (Parameterized Replaceable Events)
  - [NIP-36](https://nostr-nips.com/nip-36) (Sensitive Content / Content Warning)
  - [NIP-38](https://nostr-nips.com/nip-38) (User Statuses)
  - [NIP-39](https://nostr-nips.com/nip-39) (External Identities in Profiles)
  - [NIP-42](https://nostr-nips.com/nip-42) (Authentication of clients to relays)
  - [NIP-46](https://nostr-nips.com/nip-46) (Nostr Connect)
  - [NIP-48](https://nostr-nips.com/nip-48) (Proxy Tags)
  - [NIP-52](https://nostr-nips.com/nip-52) (Calendar Events)
  - [NIP-53](https://nostr-nips.com/nip-53) (Live Activities)
  - [NIP-56](https://nostr-nips.com/nip-56) (Reporting)
  - [NIP-57](https://nostr-nips.com/nip-57) (Lightning Zaps)
  - [NIP-58](https://nostr-nips.com/nip-58) (Badges)
  - [NIP-89](https://nostr-nips.com/nip-89) (Recommended Application Handlers)
  - [NIP-99](https://nostr-nips.com/nip-99) (Classified Listings)
  - used by [Barchetta](https://github.com/avlo/barchetta) Smart-Contract Negotiation Protocol (in progress) atop [Bitcoin](https://en.wikipedia.org/wiki/Bitcoin) [Lightning-Network](https://en.wikipedia.org/wiki/Lightning_Network) [RGB](https://rgb.tech/)

#### In-Progress
  - [NIP-15](https://nostr-nips.com/nip-15) (Nostr Marketplace)
    - used by [Barchetta](https://github.com/avlo/barchetta) Smart-Contract Negotiation Protocol (in progress) atop [Bitcoin](https://en.wikipedia.org/wiki/Bitcoin) [Lightning-Network](https://en.wikipedia.org/wiki/Lightning_Network) [RGB](https://rgb.tech/)
    
----
### Production deployment (lighter-nostr-relay)

Uses [docker-compose.yaml](docker-compose.yaml): runs only the app container and connects to an **external MySQL** (e.g. a MySQL container already published on the host, such as `10.0.0.2:3306`). Database credentials come from environment variables.

#### Requirements

- Java 22 + Maven (or `./mvnw`) to package the app
- [Docker](https://docs.docker.com/get-docker/) + [Docker Compose](https://docs.docker.com/compose/install/) v2

```bash
docker --version
docker compose version
java -version   # 22+
```

#### 1. Package the application

This project ships a thin jar plus dependencies under `target/lib` (see `pom.xml`). Build them before creating the image:

```bash
./mvnw clean package -DskipTests
```

Confirm artifacts exist:

```bash
ls target/superconductor-1.7.4.jar target/lib
```

#### 2. Build the Docker image

Default image tag: `superconductor:1.7.4` (override with `APP_IMAGE`).

```bash
docker compose build
# equivalent:
# docker build -t superconductor:1.7.4 .
```

#### 3. Configure environment

Set datasource variables (shell export or a `.env` file next to `docker-compose.yaml`):

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://10.0.0.2:3306/lighterim?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true'
export SPRING_DATASOURCE_USERNAME='lighter_im'
export SPRING_DATASOURCE_PASSWORD='your-password'
# optional:
# export APP_IMAGE=superconductor:1.7.4
# export APP_PORT=5555
# export SUPERCONDUCTOR_AUTH_ACTIVE=false
```

Use the MySQL publish address from `docker ps` (e.g. `10.0.0.2:3306->3306/tcp`), not `localhost` inside the app container.

#### 4. Run

```bash
docker compose up -d
```

Relay endpoint: `ws://<host>:5555`

#### Stop / remove

```bash
docker compose stop
docker compose down --remove-orphans
```

#### Rebuild after code changes

```bash
./mvnw clean package -DskipTests
docker compose build --no-cache
docker compose up -d
```

----

<details>
<summary>Legacy compose (bundled MySQL + prebuilt hub image)</summary>

[docker-compose-prod.yml](docker-compose-prod.yml) still pulls `avlo/superconductor:1.7.4` and starts an in-compose MySQL. Prefer [docker-compose.yaml](docker-compose.yaml) for this project’s production layout.

```bash
docker pull avlo/superconductor:1.7.4
docker compose -f docker-compose-prod.yml up -d
```

</details>

<hr style="border:2px solid grey">

#### [Development Mode Instructions](DEVELOPMENT.md)