# Sabbath School Android - Copilot Instructions

## Workflow expectations
- Before starting any sizeable task (new feature, refactor, schema change, etc.), cut a dedicated git branch named after the effort (e.g., `feat/<short-description>`). Stay on that branch until the work is complete, then raise or update the PR.
- After finishing the requested work and verifying builds/tests, create at least one commit that summarizes the change set.
- Make commits after completing a group of related changes, so the history remains clear and easy to follow.
- Write clear, concise commit messages that explain the "why" and "what" of the change, not just the "how".
- Keep commits atomic: avoid mixing unrelated changes in one commit.
- Keep this guide (`.github/copilot-instructions.md`) accurate. When project-wide conventions or expectations change, update the document in the same PR so future iterations stay in sync.

## Architecture Overview

This is a modular Android app using **Circuit** (Slack's UI framework) for presentation, **Hilt** for dependency injection, and **Jetpack Compose** for UI. The codebase follows a clean architecture with clear separation:

- **`app/`** - Main Android application module, wires everything together
- **`app-tv/`** - Android TV variant
- **`features/`** - Feature modules (feed, document, auth, media, settings, etc.)
- **`libraries/`** - Reusable libraries (circuit-api, block-kit, storage, prefs, media)
- **`services/`** - Service implementations (resources, lessons, storage, prefs)
- **`common/`** - Shared modules (auth, core, design, network, models)

## Circuit Pattern (Presentation Layer)

Every screen follows the Circuit pattern with three files:
1. **Screen** - Parcelable data class in `libraries/circuit/api/src/main/kotlin/ss/libraries/circuit/navigation/`
2. **Presenter** - Business logic, annotated with `@CircuitInject(ScreenName::class, SingletonComponent::class)`
3. **UI** - Composable function, also annotated with `@CircuitInject`

Example from `features/feed/src/main/kotlin/ss/feed/FeedPresenter.kt`:
```kotlin
@CircuitInject(FeedScreen::class, SingletonComponent::class)
@AssistedFactory
interface Factory {
    fun create(navigator: Navigator, screen: FeedScreen): FeedPresenter
}
```

**State Pattern**: Use sealed interfaces for UI state with `CircuitUiState`, events with `CircuitUiEvent`.

## Dependency Injection

- **Hilt** with `@Module`, `@InstallIn(SingletonComponent::class)`, `@Provides`, `@Binds`
- Circuit uses `@CircuitInject` for automatic presenter/UI factory registration
- KSP configured with `circuit.codegen.mode = "hilt"` in build.gradle.kts files

## Key Conventions

### Package Structure
- Features use `ss.{feature}` package (e.g., `ss.feed`, `ss.document`)
- Libraries use `ss.libraries.{name}` (e.g., `ss.libraries.circuit.navigation`)
- Services use `ss.services.{name}` (e.g., `ss.services.circuit.impl`)

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
- **Circuit tests**: Use `FakeNavigator`, `circuit-test` library, and `Presenter.test {}` extension
- **Screenshot tests**: Roborazzi with `BaseScreenshotTest` base class from `libraries/test_utils/roborazzi`
- **Fake implementations**: Create `Fake{ClassName}` in test sources (e.g., `FakeAuthRepository`, `FakeSSPrefs`)

## Module Dependencies

Use typesafe project accessors:
```kotlin
implementation(projects.common.designCompose)
implementation(projects.libraries.circuit.api)
implementation(projects.services.resources.api)
```

## File Naming

- `{Feature}Screen.kt` - Circuit Screen definition
- `{Feature}Presenter.kt` - Presenter with state management
- `{Feature}ScreenUi.kt` or `{Feature}Ui.kt` - Composable UI
- `{Feature}State.kt` - State sealed interface (if separate file)
- `BindingsModule.kt` - Hilt module with `@Binds` annotations
