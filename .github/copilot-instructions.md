# Sabbath School Android - Copilot Instructions

## Workflow expectations
- Before starting any sizeable task (new feature, refactor, schema change, etc.), cut a dedicated git branch named after the effort (e.g., `feat/<short-description>`). Stay on that branch until the work is complete, then raise or update the PR.
- After finishing the requested work and verifying builds/tests, create at least one commit that summarizes the change set.
- Make commits after completing a group of related changes, so the history remains clear and easy to follow.
- Write clear, concise commit messages that explain the "why" and "what" of the change, not just the "how".
- Keep commits atomic: avoid mixing unrelated changes in one commit.
- Keep this guide (`.github/copilot-instructions.md`) accurate. When project-wide conventions or expectations change, update the document in the same PR so future iterations stay in sync.

## Architecture Overview

This is a modular Android app using **Navigation 3** (Jetpack) for navigation, **Hilt** for dependency injection, and **Jetpack Compose** for UI. The codebase follows a clean architecture with clear separation:

- **`app/`** - Main Android application module, wires everything together
- **`app-tv/`** - Android TV variant (still uses Circuit)
- **`features/`** - Feature modules (feed, document, auth, media, settings, etc.)
- **`libraries/`** - Reusable libraries (navigation3-api, block-kit, storage, prefs, media)
- **`services/`** - Service implementations (resources, lessons, storage, prefs)
- **`common/`** - Shared modules (auth, core, design, network, models)

## Navigation 3 Pattern (Presentation Layer)

Every screen follows the Navigation 3 pattern with these components:
1. **NavKey** - Serializable data class/object in `libraries/navigation3/api/src/main/kotlin/ss/libraries/navigation3/`
2. **ViewModel** - Business logic using standard Android ViewModel with `@HiltViewModel`
3. **UI** - Composable function registered in `{Feature}NavModule.kt`

Example NavKey from `libraries/navigation3/api`:
```kotlin
@Serializable
data class DocumentKey(
    val index: String,
    val segmentIndex: String? = null,
) : NavKey
```

Example ViewModel from `features/document`:
```kotlin
@HiltViewModel
class DocumentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DocumentRepository,
) : ViewModel() {
    // State management using StateFlow
}
```

**State Pattern**: Use sealed interfaces for UI state, typically exposed via `StateFlow<State>`.

## Dependency Injection

- **Hilt** with `@Module`, `@InstallIn(SingletonComponent::class)`, `@Provides`, `@Binds`
- ViewModels annotated with `@HiltViewModel` and injected via `hiltViewModel()`
- Navigation registration via `{Feature}NavModule` using `EntryProviderBuilder`

## Key Conventions

### Package Structure
- Features use `ss.{feature}` package (e.g., `ss.feed`, `ss.document`)
- Libraries use `ss.libraries.{name}` (e.g., `ss.libraries.navigation3`)
- Services use `ss.services.{name}` (e.g., `ss.services.resources.impl`)

### Repository Pattern
- Interfaces in `api` modules, implementations in `impl` modules
- Return `Flow<T>` for observable data, `Result<T>` for one-shot operations
- Key repositories: `ResourcesRepository`, `AuthRepository`, `LessonsRepository`, `MediaRepository`

### Compose Theme
- Use `SsTheme` wrapper from `common/design-compose`
- Access dimensions via `SsTheme.dimens`, colors via `SsTheme.colors`
- Use `kotlinx-collections-immutable` (`ImmutableList`, `persistentListOf()`) for Compose state

### Data Layer
- **Room** for local storage (DAOs in `libraries/storage/api`)
- **Retrofit + Moshi** for network
- `NetworkResource<T>` sealed class for API responses
- `safeApiCall()` helper for network calls with connectivity checking

## Build Commands

```bash
# Run all checks (lint, tests)
./gradlew check

# Run unit tests
./gradlew testDebugUnitTest

# Screenshot tests (Roborazzi)
./gradlew testDebugUnitTest -Proborazzi.test.verify=true

# Build release bundle
./gradlew app:bundleRelease

# Build specific module
./gradlew :features:feed:assembleDebug
```

## Testing

- **Unit tests**: JUnit4, MockK, Turbine (for Flow testing), Kluent assertions
- **ViewModel tests**: Use `runTest {}` from coroutines-test, Turbine for StateFlow testing
- **Screenshot tests**: Roborazzi with `BaseScreenshotTest` base class from `libraries/test_utils/roborazzi`
- **Fake implementations**: Create `Fake{ClassName}` in test sources (e.g., `FakeAuthRepository`, `FakeSSPrefs`)

## Module Dependencies

Use typesafe project accessors:
```kotlin
implementation(projects.common.designCompose)
implementation(projects.libraries.navigation3.api)
implementation(projects.services.resources.api)
```

## File Naming

- `{Feature}Key.kt` - Navigation 3 NavKey definition
- `{Feature}ViewModel.kt` - ViewModel with state management
- `{Feature}ScreenUi.kt` or `{Feature}Ui.kt` - Composable UI
- `{Feature}NavModule.kt` - Navigation registration module
- `{Feature}State.kt` - State sealed interface (if separate file)
- `BindingsModule.kt` - Hilt module with `@Binds` annotations
