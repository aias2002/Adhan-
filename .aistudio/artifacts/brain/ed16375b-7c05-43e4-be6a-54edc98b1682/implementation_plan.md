# Integration of QuranHub for Warsh Recitation

This plan outlines the integration of QuranHub as a dedicated data source for Warsh recitation to ensure 100% accuracy and prevent recitation mixing, while ensuring the current Hafs recitation (alquran.cloud) remains untouched and fully functional.

### User Review & Critical Decisions

> [!IMPORTANT]
> The primary requirement is to fix the Warsh recitation inaccuracies without impacting Hafs or overall application performance.

- **Confirmed Decision**: QuranHub will be integrated as the primary data source for Warsh (edition `ara-quranwarsh`).
- **Confirmed Decision**: The current `alquran.cloud` API will remain the source for Hafs (`quran-uthmani`) to ensure zero impact on existing users of Hafs.
- **Confirmed Decision**: A validation layer will be added to ensure only verified Warsh text is displayed.

### 1. Overview & Core Concept

- **What It Does**: Adds a new API service connector for QuranHub, modifying the repository to route requests based on the selected edition.
- **Key Value**: Provides accurate, verified text for Warsh recitation, eliminating the recitation mixing issue.

### 2. Technical Architecture & Data Strategy

The current `QuranRepository` will be updated to act as a router for data requests.

```
┌──────────────────────────┐
│   QuranRepository        │
└─────────────┬────────────┘
              │
      ┌───────┴───────┐
      │  Request      │
      ▼               ▼
┌──────────────┐ ┌──────────────┐
│  Hafs (Cloud)│ │ Warsh (Hub)  │
└──────────────┘ └──────────────┘
```

- **New Service**: Create `QuranHubApiService` for Warsh requests.
- **Router Logic**: `QuranRepository.getSurahDetail` will now check the `edition` parameter:
    - If `edition == "quran-uthmani"`, call existing `AlquranApiService`.
    - If `edition == "quran-warsh"`, call `QuranHubApiService`.
- **Cache Invalidation**: Maintain the already-implemented cache clearing logic when switching editions.

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Separate API Services**
    - *Chosen*: Create a dedicated `QuranHubApiService`.
    - *Why*: Keeps Hafs and Warsh dependencies clean and isolated, reducing risk of breaking Hafs.
- **Decision 2: Runtime Validation**
    - *Chosen*: Repository-level validation of fetched text.
    - *Why*: Ensures that if an API returns incorrect data for the requested edition, it is not displayed to the user.
