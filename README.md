# kmp-pocketbase

A Kotlin Multiplatform SDK for [PocketBase](https://pocketbase.io) — the open-source backend in one file.

## Platforms

| Platform | Status |
|---|---|
| Android | ✅ |
| iOS (arm64 + simulatorArm64) | ✅ |
| JVM (Desktop) | ✅ |
| JavaScript (Browser) | ✅ |
| WebAssembly (WasmJS) | ✅ |

The same API works identically across all platforms. No platform-specific code required in your shared logic.

---

## Prerequisites

Your module must apply the **Kotlin Serialization Gradle plugin**. This is required because the SDK uses `@Serializable` generics — the compiler plugin generates serializers at compile time in *your* module, and cannot be bundled inside the library.

```kotlin
// build.gradle.kts
plugins {
    kotlin("multiplatform") // or kotlin("android"), kotlin("jvm"), etc.
    kotlin("plugin.serialization") version "2.1.21"
}
```

The runtime (`kotlinx-serialization-json`) is included transitively — no need to add it yourself.

---

## Installation

GitHub Packages requires authentication even for public packages.

**1. Crea `local.properties`** en la raíz de tu proyecto (ya está en `.gitignore` en proyectos Android/KMP, nunca se sube al repositorio):

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

El token necesita el scope `read:packages`. Puedes generarlo en **GitHub → Settings → Developer settings → Personal access tokens**.

**2. Agrega el repositorio en `settings.gradle.kts`:**

```kotlin
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

dependencyResolutionManagement {
    repositories {
        // ... tus otros repositorios
        maven {
            url = uri("https://maven.pkg.github.com/hunterhamlet/kmp-pocketbase")
            credentials {
                username = localProperties.getProperty("gpr.user")
                password = localProperties.getProperty("gpr.key")
            }
        }
    }
}
```

**3. Agrega la dependencia en `build.gradle.kts`:**

```kotlin
dependencies {
    implementation("com.hamon:kmp-pocketbase:0.1.0-alpha01")
}
```

### Android — one-time init

Call this before creating any `PocketBase` instance (e.g. in your `Application.onCreate`):

```kotlin
PocketBase.init(context)
```

Required only when using `TokenPersistence.Encrypted` on Android.

---

## Quick Start

```kotlin
@Serializable
data class Article(
    val title: String,
    val content: String,
    val published: Boolean,
)

val pb = PocketBase(baseUrl = "https://your-pocketbase.io")

val auth = pb.collection("users").authWithPassword<UserProfile>(
    identity = "user@example.com",
    password = "password123",
)

val articles = pb.collection("articles").getList<Article>()
```

---

## Creating the Client

```kotlin
val pb = PocketBase(
    baseUrl          = "https://your-pocketbase.io",
    tokenPersistence = TokenPersistence.None,       // default
    logLevel         = PocketBaseLogLevel.NONE,     // default
    logger           = PocketBaseLogger.Default,
)
```

### Token Persistence

| Value | Behavior |
|---|---|
| `TokenPersistence.None` | Token lives in memory — lost on restart |
| `TokenPersistence.Encrypted` | Persisted in Keystore (Android) / Keychain (iOS) |

```kotlin
val pb = PocketBase(
    baseUrl          = "https://your-pocketbase.io",
    tokenPersistence = TokenPersistence.Encrypted,
)
```

The token is restored automatically on the next app launch.

### Logging

```kotlin
val pb = PocketBase(
    baseUrl  = "https://your-pocketbase.io",
    logLevel = PocketBaseLogLevel.BASIC,    // URL, status, body
    // logLevel = PocketBaseLogLevel.HEADERS   // also request/response headers
)
```

Custom logger:

```kotlin
val pb = PocketBase(
    baseUrl  = "https://your-pocketbase.io",
    logLevel = PocketBaseLogLevel.BASIC,
    logger   = PocketBaseLogger { entry -> println(entry) },
)
```

---

## Collections

All operations are accessed through `pb.collection("name")`. Instances are cached internally.

```kotlin
val articles = pb.collection("articles")
val users    = pb.collection("users")
```

---

## Reading Records

### Get list (paginated)

```kotlin
val result: ResultList<Article> = pb.collection("articles").getList<Article>(
    page    = 1,
    perPage = 30,
)

result.items        // List<RecordModel<Article>>
result.totalItems   // Int
result.totalPages   // Int
```

### Get full list (auto-paginated)

```kotlin
val all: List<RecordModel<Article>> = pb.collection("articles").getFullList<Article>(
    perPage = 200,
)
```

### Get one record

```kotlin
val record: RecordModel<Article> = pb.collection("articles").getOne<Article>("RECORD_ID")

record.id
record.collectionName
record.created
record.updated
record.fields   // Article instance
```

---

## Writing Records

### Create

```kotlin
val record = pb.collection("articles").create<Article>(
    buildJsonObject {
        put("title", "Hello World")
        put("content", "My first article")
        put("published", true)
    }
)
```

### Update

```kotlin
val record = pb.collection("articles").update<Article>(
    id   = "RECORD_ID",
    body = buildJsonObject { put("published", false) },
)
```

### Delete

```kotlin
pb.collection("articles").delete("RECORD_ID")
```

---

## Query DSL

```kotlin
val result = pb.collection("articles").getList<Article>(
    query = pbQuery {
        filter {
            "published" eq true
            "views" gt 100
        }
        sort {
            add("created".desc())
            add("title".asc())
        }
        expand("author", "category")
        fields("id", "title", "published")
        skipTotal()
    }
)
```

### Filter operators

| Operator | Symbol | Example |
|---|---|---|
| `eq` | `=` | `"status" eq "active"` |
| `neq` | `!=` | `"role" neq "admin"` |
| `gt` | `>` | `"views" gt 100` |
| `gte` | `>=` | `"price" gte 10.0` |
| `lt` | `<` | `"stock" lt 5` |
| `lte` | `<=` | `"age" lte 18` |
| `like` | `~` | `"name" like "John"` |
| `notLike` | `!~` | `"email" notLike "spam"` |
| `anyEq` | `?=` | `"tags" anyEq "kotlin"` |
| `anyNeq` | `?!=` | `"tags" anyNeq "java"` |
| `anyGt` | `?>` | `"scores" anyGt 90` |
| `anyGte` | `?>=` | `"scores" anyGte 90` |
| `anyLt` | `?<` | `"scores" anyLt 50` |
| `anyLte` | `?<=` | `"scores" anyLte 50` |
| `inRange` | `>= && <=` | `"price" inRange (10..50)` |
| `notInRange` | `< \|\| >` | `"age" notInRange (18..65)` |

### Logical grouping

```kotlin
filter {
    "published" eq true
    or {
        "category" eq "news"
        "category" eq "sports"
    }
    not {
        "status" eq "archived"
    }
}
```

Top-level conditions are combined with `&&`. Use `or {}` and `not {}` for grouping.

### Sort

```kotlin
sort {
    add("created".desc())   // -created
    add("title".asc())      // +title
}
```

---

## Authentication

### Auth with password

```kotlin
@Serializable
data class UserProfile(val name: String, val email: String)

val auth: AuthResponse<UserProfile> = pb.collection("users")
    .authWithPassword<UserProfile>(
        identity = "user@example.com",
        password = "secret",
    )

auth.token          // JWT string
auth.record.fields  // UserProfile
```

### Auth Refresh

Fetches a new token and updates both the in-memory store and encrypted storage:

```kotlin
val auth = pb.collection("users").authRefresh<UserProfile>()

// with optional params
val auth = pb.collection("users").authRefresh<UserProfile>(
    expand = "profile",
    fields = "id,email,name",
)
```

### Auth Store

```kotlin
pb.authStore.isValid   // Boolean — true when token present
pb.authStore.token     // String? — current JWT
pb.authStore.model     // RecordModel<*>? — authenticated record

pb.authStore.clear()   // removes token from memory and storage
```

---

## Safe Variants (`try*`)

Every operation has a `try*` variant returning `PocketBaseResult<T>` instead of throwing:

```kotlin
val result: PocketBaseResult<ResultList<Article>> =
    pb.collection("articles").tryGetList<Article>()

result
    .onSuccess { list -> println(list.totalItems) }
    .onFailure { error -> println(error.message) }

val list  = result.getOrNull()        // null on failure
val error = result.exceptionOrNull()  // null on success
val ids   = result.map { it.items.map { r -> r.id } }
```

| Method | Safe variant |
|---|---|
| `getList` | `tryGetList` |
| `getFullList` | `tryGetFullList` |
| `getOne` | `tryGetOne` |
| `create` | `tryCreate` |
| `update` | `tryUpdate` |
| `delete` | `tryDelete` |
| `authWithPassword` | `tryAuthWithPassword` |
| `authRefresh` | `tryAuthRefresh` |
| `createWithFiles` | `tryCreateWithFiles` |
| `updateWithFiles` | `tryUpdateWithFiles` |

---

## Flow Variants

```kotlin
pb.collection("articles").getListAsFlow<Article>()
    .collect { list -> ... }

pb.collection("articles").tryGetListAsFlow<Article>()
    .collect { result -> result.onSuccess { ... }.onFailure { ... } }
```

Available flow variants: `getListAsFlow`, `tryGetListAsFlow`, `getFullListAsFlow`, `tryGetFullListAsFlow`, `getOneAsFlow`, `tryGetOneAsFlow`.

---

## File Uploads

```kotlin
val file = FileUpload(
    field    = "avatar",            // collection field name
    filename = "photo.jpg",
    data     = byteArrayOf(/*…*/),
    mimeType = "image/jpeg",        // default: "application/octet-stream"
)
```

### Create with files

```kotlin
val record = pb.collection("users").createWithFiles<UserProfile>(
    body       = buildJsonObject { put("name", "Alice") },
    files      = listOf(file),
    onProgress = { fraction -> println("${(fraction * 100).toInt()}%") },
)
```

### Update with files

```kotlin
val record = pb.collection("users").updateWithFiles<UserProfile>(
    id         = "RECORD_ID",
    body       = buildJsonObject {},
    files      = listOf(file),
    onProgress = { fraction -> updateProgressBar(fraction) },
)
```

`onProgress` is optional. Receives a `Float` in `0.0..1.0`.

### Multi-file field modifiers

```kotlin
// append without replacing existing files
FileUpload(field = "documents+", filename = "new.pdf", data = bytes)

// delete specific files
buildJsonObject {
    put("documents-", buildJsonArray { add("old_filename_abc123.pdf") })
}
```

### File URLs

```kotlin
val url = pb.collection("users").getFileUrl(
    recordId = record.id,
    filename  = record.fields.avatar,  // filename returned by PocketBase
)

// with thumbnail (jpg, png, gif, webp)
val thumb = pb.collection("users").getFileUrl(
    recordId = record.id,
    filename  = record.fields.avatar,
    thumb     = "100x100",
)
```

URL format: `{baseUrl}/api/files/{collection}/{recordId}/{filename}`

> PocketBase appends a 10-character random suffix to filenames (e.g. `photo_52iwbgds7l.jpg`). The final filename is returned in the record after upload.

---

## Realtime

```kotlin
pb.collection("articles").subscribe<Article>()
    .collect { result ->
        result.onSuccess { event ->
            when (event.action) {
                RealtimeAction.CREATE -> println("created ${event.record.id}")
                RealtimeAction.UPDATE -> println("updated ${event.record.id}")
                RealtimeAction.DELETE -> println("deleted ${event.record.id}")
            }
        }
        .onFailure { error -> println(error.message) }
    }

// subscribe to a specific record
pb.collection("articles").subscribe<Article>(recordId = "RECORD_ID")
    .collect { ... }

// auto-reconnect on connection loss (3s retry)
pb.collection("articles").subscribe<Article>(autoReconnect = true)
    .collect { ... }
```

---

## RecordModel

All records are wrapped in:

```kotlin
data class RecordModel<T>(
    val id             : String,
    val collectionId   : String,
    val collectionName : String,
    val created        : String,
    val updated        : String,
    val fields         : T,       // your @Serializable data class
)
```

---

## Codegen Plugin

`kmp-pocketbase-codegen` is a Gradle plugin that reads a PocketBase schema JSON and generates `@Serializable` Kotlin Multiplatform data classes, one per collection.

### What it generates

Given a collection named `posts` with fields `title`, `content`, and `author` (relation), the plugin produces:

```kotlin
// AUTO-GENERATED by kmp-pocketbase-codegen — do not edit manually
@Serializable
data class PostsRecord(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val created: String? = null,
    val updated: String? = null,
)
```

`select` fields produce an accompanying `@Serializable enum class` in the same package. The `generateParcelize` flag can wrap every class with `@PbParcelize : PbParcelable` (real `@Parcelize`/`Parcelable` on Android, no-ops everywhere else).

### PocketbaseCollection — collection name constants

The plugin also generates a `PocketbaseCollection` object with a `const val` for every collection in your schema. This lets you reference collection names without raw strings anywhere in your code.

```kotlin
// AUTO-GENERATED by kmp-pocketbase-codegen — do not edit manually
object PocketbaseCollection {
    const val users: String = "users"
    const val posts: String = "posts"
    const val messages: String = "messages"
}
```

**Usage:**

```kotlin
// Before
pb.collection("posts").getFullList<PostsRecord>()

// After
pb.collection(PocketbaseCollection.posts).getFullList<PostsRecord>()
```

- Typos in collection names become compile-time errors instead of runtime 404s.
- Renaming a collection only requires updating the schema — all call sites follow automatically on the next codegen run.
- The object respects `excludeSystemCollections` — system collections (`_superusers`, `_authOrigins`, etc.) are omitted by default.

### Export the schema

In your PocketBase admin UI go to **Settings → Export collections** and save the JSON file. Keep it out of version control:

```
# .gitignore
pb_schema.json
```

### Apply the plugin

**Option A — same repo (includeBuild)**

The plugin lives in `/codegen/` and is already wired up via `pluginManagement { includeBuild("codegen") }` in `settings.gradle.kts`. No publishing step needed.

**Option B — GitHub Packages**

Add the plugin repository to `settings.gradle.kts` using the same `local.properties` credentials as the SDK:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.github.com/hunterhamlet/kmp-pocketbase")
            credentials {
                username = localProperties.getProperty("gpr.user")
                password = localProperties.getProperty("gpr.key")
            }
        }
    }
}
```

### Configure

```kotlin
// build.gradle.kts (your KMP module)
plugins {
    id("com.hamon.kmp-pocketbase.codegen") version "0.1.0-alpha01"
}

pocketbaseCodegen {
    schemaFile = rootProject.file("pb_schema.json")   // path to your exported schema
    packageName.set("com.example.generated")          // package for generated classes
    generateParcelize.set(false)                      // true to add @PbParcelize (needs kotlin-parcelize)
    excludeSystemCollections.set(true)                // skip _superusers, _authOrigins, etc.
}
```

Generated sources land in `build/generated/pocketbase/` and are registered automatically as `commonMain` source roots.

### DSL options

| Property | Type | Default | Description |
|---|---|---|---|
| `schemaFile` | `RegularFileProperty` | — | Path to PocketBase schema JSON (**required**) |
| `packageName` | `Property<String>` | — | Package for all generated classes (**required**) |
| `outputDir` | `DirectoryProperty` | `build/generated/pocketbase` | Where files are written |
| `generateParcelize` | `Property<Boolean>` | `true` | Wrap classes with expect/actual `@PbParcelize : PbParcelable` |
| `excludeSystemCollections` | `Property<Boolean>` | `true` | Skip collections where `system = true` |

### PocketBase field → Kotlin type mapping

| PocketBase type | Kotlin type (single) | Kotlin type (multi) |
|---|---|---|
| `text`, `email`, `url` | `String` | — |
| `number` | `Double` / `Int` (onlyInt) | — |
| `bool` | `Boolean` | — |
| `date`, `autodate` | `String` | — |
| `select` | `Enum` | `List<Enum>` |
| `relation` | `String` (ID) | `List<String>` |
| `file` | `String` (filename) | `List<String>` |
| `json` | `JsonElement` | — |
| `password` | *(skipped)* | — |

Required fields are non-nullable; optional fields default to `null`.

### Publish a new version

Tag the codegen commit to trigger the GitHub Actions workflow:

```bash
git tag codegen-v0.1.0
git push origin codegen-v0.1.0
```

The workflow at `.github/workflows/publish-codegen.yml` runs the tests and publishes to GitHub Packages automatically using `GITHUB_TOKEN`.

---

## Tech Stack

| Component | Library | Version |
|---|---|---|
| Kotlin | — | 2.4.0 |
| Networking | Ktor Client | 3.5.0 |
| Serialization | kotlinx-serialization-json | 1.8.1 |
| Coroutines | kotlinx-coroutines-core | 1.11.0 |
| Encrypted Storage | KSafe | 2.1.1 |
| UI (demo) | Compose Multiplatform | 1.11.1 |
| Code generation | KotlinPoet | 2.1.0 |
| Android Gradle Plugin | AGP | 9.0.1 |
| Android minSdk / compileSdk | — | 24 / 36 |
| AndroidX Activity | activity-compose | 1.13.0 |
| AndroidX Lifecycle | lifecycle-viewmodel-compose | 2.11.0-beta01 |

---

## License

MIT
