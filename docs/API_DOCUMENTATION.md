# 📚 Sabbath School API Documentation

> Неофіційна документація API для Sabbath School Android застосунку від Adventech.
> Згенеровано на основі аналізу вихідного коду.

## Базові URL-адреси

| Середовище | URL |
|-----------|-----|
| **Production** | `https://sabbath-school.adventech.io/` |
| **Staging** | `https://sabbath-school-stage.adventech.io/` |
| **Images** | `https://sabbath-school.adventech.io/api/v3/images/` |

---

## 🔐 Authentication API

Всі ендпоінти авторизації знаходяться за шляхом `/api/v2/auth/`.

### 1. Анонімний вхід

Дозволяє користуватися застосунком без реєстрації.

```http
POST /api/v2/auth/signin/anonymous
```

**Відповідь:** `UserModel`

### 2. Вхід через Google

Авторизація за допомогою Google ID Token.

```http
POST /api/v2/auth/signin/google
Content-Type: application/json
```

**Тіло запиту:**
```json
{
  "id_token": "GOOGLE_ID_TOKEN_FROM_CREDENTIALS_API"
}
```

**Відповідь:** `UserModel`

### 3. Оновлення токену

Автоматично викликається `TokenAuthenticator` при отриманні 401 помилки.

```http
POST /api/v2/auth/refresh
Content-Type: application/json
```

**Тіло запиту:** Повний об'єкт `UserModel`

**Відповідь:** Оновлений `UserModel` з новими токенами

### 4. Видалення акаунту

Видаляє акаунт користувача з сервера.

```http
POST /api/v2/auth/delete
```

**Заголовки:**
```
x-ss-auth-access-token: {access_token}
```

### Модель UserModel

```json
{
  "uid": "унікальний_ідентифікатор_користувача",
  "displayName": "Ім'я Користувача",
  "email": "user@example.com",
  "photoURL": "https://example.com/photo.jpg",
  "emailVerified": true,
  "phoneNumber": "+380123456789",
  "isAnonymous": false,
  "tenantId": null,
  "stsTokenManager": {
    "apiKey": "api_key_string",
    "refreshToken": "refresh_token_string",
    "accessToken": "access_token_string",
    "expirationTime": 1734012033000
  }
}
```

| Поле | Тип | Опис |
|------|-----|------|
| `uid` | string | Унікальний ID користувача |
| `displayName` | string? | Ім'я для відображення |
| `email` | string? | Email користувача |
| `photoURL` | string? | URL аватару |
| `emailVerified` | boolean | Чи підтверджено email |
| `phoneNumber` | string? | Номер телефону |
| `isAnonymous` | boolean | Чи анонімний користувач |
| `tenantId` | string? | ID тенанта |
| `stsTokenManager` | AccountToken | Токени авторизації |

---

## 📖 Quarterlies API (v2)

API для роботи з квартальниками (посібниками) Суботньої школи.

### 1. Список всіх квартальників для мови

```http
GET /api/v2/{lang}/quarterlies/index.json
```

**Параметри:**
| Параметр | Опис | Приклад |
|----------|------|---------|
| `lang` | Код мови (ISO 639-1) | `uk`, `en`, `ru`, `de` |

**Приклад:**
```bash
curl "https://sabbath-school.adventech.io/api/v2/uk/quarterlies/index.json"
```

**Відповідь:**
```json
[
  {
    "id": "2025-04",
    "title": "Уроки віри від Ісуса Навина",
    "description": "Книга Ісуса Навина знаменує перехід...",
    "human_date": "Жовтень · Листопад · Грудень 2025",
    "start_date": "27/09/2025",
    "end_date": "26/12/2025",
    "color_primary": "#5A2C32",
    "color_primary_dark": "#541B23",
    "splash": "https://sabbath-school.adventech.io/api/v2/images/global/2025-04/splash.png",
    "cover": "https://sabbath-school.adventech.io/api/v2/uk/quarterlies/2025-04/cover.png",
    "lang": "uk",
    "index": "uk-2025-04",
    "path": "uk/quarterlies/2025-04",
    "full_path": "https://sabbath-school.adventech.io/api/v2/uk/quarterlies/2025-04",
    "introduction": "### Другий шанс: книга Ісуса Навина\n\n..."
  }
]
```

### 2. Деталі квартальника з уроками

```http
GET /api/v2/{lang}/quarterlies/{quarterly_id}/index.json
```

**Параметри:**
| Параметр | Опис | Приклад |
|----------|------|---------|
| `lang` | Код мови | `uk` |
| `quarterly_id` | ID квартальника | `2025-04` |

**Приклад:**
```bash
curl "https://sabbath-school.adventech.io/api/v2/uk/quarterlies/2025-04/index.json"
```

**Відповідь:**
```json
{
  "quarterly": {
    "id": "2025-04",
    "title": "Уроки віри від Ісуса Навина",
    "description": "...",
    "human_date": "Жовтень · Листопад · Грудень 2025",
    "start_date": "27/09/2025",
    "end_date": "26/12/2025",
    "color_primary": "#5A2C32",
    "color_primary_dark": "#541B23",
    "cover": "https://...",
    "splash": "https://...",
    "lang": "uk",
    "index": "uk-2025-04",
    "quarterly_name": "...",
    "features": [
      {
        "name": "teacher-comments",
        "title": "Коментарі для вчителів",
        "description": "Коментар для вчителів допоможе підготуватися...",
        "image": "https://sabbath-school.adventech.io/api/v1/images/features/feature_teacher_comments.png"
      },
      {
        "name": "audio",
        "title": "Аудіо",
        "description": "Для зручності вивчення використовуйте аудіо версії...",
        "image": "https://sabbath-school.adventech.io/api/v1/images/features/feature_audio.png"
      },
      {
        "name": "video",
        "title": "Відео",
        "description": "Відео версія уроку Суботньої Школи...",
        "image": "https://sabbath-school.adventech.io/api/v1/images/features/feature_video.png"
      },
      {
        "name": "original-layout",
        "title": "Оригінальний макет",
        "description": "Покращіть вивчення уроку, використовуючи оригінальний PDF...",
        "image": "https://sabbath-school.adventech.io/api/v1/images/features/feature_original_layout.png"
      }
    ],
    "credits": []
  },
  "lessons": [
    {
      "id": "01",
      "title": "Рецепт успіху",
      "start_date": "27/09/2025",
      "end_date": "03/10/2025",
      "index": "uk-2025-04-01",
      "path": "uk/quarterlies/2025-04/lessons/01",
      "full_path": "https://sabbath-school.adventech.io/api/v2/uk/quarterlies/2025-04/lessons/01",
      "pdfOnly": false,
      "cover": "https://sabbath-school.adventech.io/api/v2/images/global/2025-04/01/cover.png"
    }
  ]
}
```

### Доступні Features

| Назва | Опис |
|-------|------|
| `teacher-comments` | Коментарі для вчителів |
| `audio` | Аудіо версія уроку |
| `video` | Відео версія уроку |
| `original-layout` | PDF з оригінальним макетом |

---

## 📝 Lessons API (v2)

API для роботи з уроками та днями.

### Деталі уроку

```http
GET /api/v2/{lesson_path}/index.json
```

**Приклад:**
```bash
curl "https://sabbath-school.adventech.io/api/v2/uk/quarterlies/2025-04/lessons/01/index.json"
```

**Відповідь:** Об'єкт `SSLessonInfo` з:
- `lesson` - інформація про урок
- `days` - список днів тижня
- `pdfs` - PDF файли уроку

---

## 🎵 Media API

API для отримання аудіо та відео контенту.

### 1. Аудіо для квартальника

```http
GET /api/v1/{lang}/quarterlies/{quarterly_id}/audio.json
```

**Приклад:**
```bash
curl "https://sabbath-school.adventech.io/api/v1/uk/quarterlies/2025-04/audio.json"
```

**Відповідь:** Список об'єктів `SSAudio`

### 2. Список мов для відео

```http
GET /api/v2/video/languages.json
```

**Відповідь:**
```json
["en", "es", "pt", "de", "fr", "ru", "uk"]
```

### 3. Останні відео для мови

```http
GET /api/v2/{lang}/video/latest.json
```

**Приклад:**
```bash
curl "https://sabbath-school.adventech.io/api/v2/en/video/latest.json"
```

---

## 🌐 Resources API (v3)

Новий API для роботи з різними типами контенту.

### 1. Список мов та типів контенту

```http
GET /api/v3/resources/index.json
```

**Відповідь:**
```json
[
  {
    "name": "Afrikaans",
    "code": "af",
    "devo": false,
    "pm": false,
    "aij": false,
    "ss": true,
    "explore": false
  },
  {
    "name": "English",
    "code": "en",
    "devo": true,
    "pm": true,
    "aij": true,
    "ss": true,
    "explore": true
  },
  {
    "name": "Ukrainian",
    "code": "uk",
    "devo": false,
    "pm": false,
    "aij": false,
    "ss": true,
    "explore": false
  }
]
```

**Типи контенту:**

| Поле | Опис |
|------|------|
| `ss` | Sabbath School - Суботня школа |
| `devo` | Devotionals - Щоденні роздуми |
| `pm` | Personal Ministries - Особисте служіння |
| `aij` | Adventist Identity Journey |
| `explore` | Explore - Дослідження |

### 2. Feed (стрічка контенту)

```http
GET /api/v3/{language}/{type}/index.json
```

**Параметри:**
| Параметр | Опис | Приклад |
|----------|------|---------|
| `language` | Код мови | `en`, `uk` |
| `type` | Тип контенту | `ss`, `devo`, `pm`, `aij`, `explore` |

**Приклад:**
```bash
curl "https://sabbath-school.adventech.io/api/v3/en/ss/index.json"
```

### 3. Feed Group (група в стрічці)

```http
GET /api/v3/{language}/{type}/feeds/{groupId}/index.json
```

### 4. Resource (розділи ресурсу)

```http
GET /api/v3/{index}/sections/index.json
```

### 5. Document (документ)

```http
GET /api/v3/{index}/index.json
```

### 6. Segment

```http
GET /api/v3/{index}/index.json
```

### 7. Медіа для ресурсу

```http
GET /api/v3/{index}/audio.json
GET /api/v3/{index}/video.json
GET /api/v3/{index}/pdf.json
```

---

## ✏️ User Input API (v3)

API для збереження нотаток та виділень користувача.

> ⚠️ **Потребує авторизації**

### 1. Отримати нотатки для документа

```http
GET /api/v3/resources/user/input/document/{documentId}
```

**Заголовки:**
```
x-ss-auth-access-token: {access_token}
```

**Відповідь:** Список об'єктів `UserInput`

### 2. Зберегти нотатку/виділення

```http
POST /api/v3/resources/user/input/{inputType}/{documentId}/{blockId}
Content-Type: application/json
```

**Заголовки:**
```
x-ss-auth-access-token: {access_token}
```

**Параметри:**
| Параметр | Опис |
|----------|------|
| `inputType` | Тип вводу (highlight, note) |
| `documentId` | ID документа |
| `blockId` | ID блоку тексту |

**Тіло запиту:** `UserInputRequest`

---

## 🔑 Авторизація запитів

Для захищених ендпоінтів потрібен заголовок:

```
x-ss-auth-access-token: {accessToken}
```

Де `accessToken` отримується з `stsTokenManager.accessToken` при авторизації.

### Автоматичне оновлення токену

`TokenAuthenticator` автоматично оновлює токен при отриманні 401 помилки:

1. Отримує збережений `UserModel` з локальної БД
2. Викликає `POST /api/v2/auth/refresh`
3. Зберігає оновлений `UserModel`
4. Повторює оригінальний запит з новим токеном

---

## 📋 Підтримувані мови

API підтримує **60+ мов**. Повний список доступний через:

```bash
curl "https://sabbath-school.adventech.io/api/v3/resources/index.json"
```

### Популярні мови:

| Код | Мова | SS | Devo |
|-----|------|-----|------|
| `uk` | Українська | ✅ | ❌ |
| `ru` | Російська | ✅ | ❌ |
| `en` | Англійська | ✅ | ✅ |
| `de` | Німецька | ✅ | ❌ |
| `es` | Іспанська | ✅ | ❌ |
| `fr` | Французька | ✅ | ❌ |
| `pt` | Португальська | ✅ | ❌ |
| `pl` | Польська | ✅ | ❌ |
| `ro` | Румунська | ✅ | ❌ |

---

## 📱 Приклади використання

### Отримати поточний квартальник українською

```bash
# 1. Отримати список квартальників
curl "https://sabbath-school.adventech.io/api/v2/uk/quarterlies/index.json"

# 2. Отримати деталі конкретного квартальника
curl "https://sabbath-school.adventech.io/api/v2/uk/quarterlies/2025-04/index.json"

# 3. Отримати урок
curl "https://sabbath-school.adventech.io/api/v2/uk/quarterlies/2025-04/lessons/01/index.json"
```

### Авторизація та збереження нотаток

```bash
# 1. Анонімний вхід
curl -X POST "https://sabbath-school.adventech.io/api/v2/auth/signin/anonymous"

# 2. Отримати нотатки (з токеном)
curl -H "x-ss-auth-access-token: YOUR_TOKEN" \
  "https://sabbath-school.adventech.io/api/v3/resources/user/input/document/doc123"
```

---

## 🔧 Технічні деталі

### HTTP клієнт
- Застосунок використовує **Retrofit** з **Moshi** для серіалізації JSON
- **OkHttp** з `TokenAuthenticator` для автоматичного оновлення токенів

### Локальне кешування
- **Room Database** для збереження даних офлайн
- Користувач зберігається в `UserEntity`

### Формат дат
- Дати у форматі `dd/MM/yyyy` (наприклад, `27/09/2025`)

---

## 📄 Ліцензія

Цей документ створено на основі відкритого вихідного коду проекту 
[Sabbath School Android](https://github.com/Adventech/sabbath-school-android) 
під ліцензією MIT.

---

*Документ оновлено: 12 грудня 2025*
