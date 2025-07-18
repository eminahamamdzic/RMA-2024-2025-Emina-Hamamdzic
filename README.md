# 📰 NewsFeedApp

**NewsFeedApp** je Android mobilna aplikacija razvijena u okviru predmeta **Razvoj mobilnih aplikacija** (ETF Sarajevo), koja omogućava korisnicima pregled, filtriranje, i čuvanje vijesti iz različitih kategorija, uz podršku za offline režim pomoću Room baze.

---

## 🚀 Funkcionalnosti

### 📲 Prikaz vijesti (NewsFeedScreen)
- Lazy prikaz vijesti (standardne i featured).
- Vizuelni filteri po kategorijama: Politika, Sport, Nauka/Tehnologija, Sve.
- Sekcija sa dodatnim filterima:
  - Filtriranje po opsegu datuma.
  - Filtriranje po nepoželjnim riječima.
- Automatsko izdvajanje "featured" vijesti pri novom pozivu servisa.

### 🔍 Detalji vijesti (NewsDetailsScreen)
- Prikaz naslova, sažetka, kategorije, izvora i datuma.
- Prikaz tagova slike (imageTags) dobijenih putem API poziva (`ImagaDAO.getTags`).
- Povezane vijesti iz iste kategorije (online ili iz lokalne baze, u zavisnosti od konekcije).

### 🧠 Offline podrška (Room baza)
- Sve vijesti se lokalno čuvaju u Room bazi (`news-db`) ako ne postoje već.
- Vijesti se povezuju s tagovima kroz međutabelu `NewsTags`.
- Offline fallback uključuje:
  - Prikaz sačuvanih vijesti (`SavedNewsDAO.allNews()`).
  - Prikaz povezanih vijesti preko `getSimilarNews()` ako nema interneta.
  - Prikaz tagova iz baze (`getTags()`), bez ponovnog API poziva.

---

## 🛠️ Tehnologije

- **Kotlin**
- **Jetpack Compose**
- **Room (Database)**
- **Retrofit (Web servis)**
- **Material3 (UI dizajn)**
- **MVVM arhitektura**

---

## 🗃️ Struktura baze (`news-db`)

- **News**
  - `id` (auto-increment)
  - `uuid`, `title`, `snippet`, `imageUrl`, `category`, `source`, `publishedDate`, `isFeatured`
- **Tags**
  - `id` (auto-increment), `value`
- **NewsTags** (M:N)
  - `id`, `newsId`, `tagId`

### DAO metode (`SavedNewsDAO`)
- `saveNews(news: NewsItem): Boolean`
- `allNews(): List<NewsItem>`
- `getNewsWithCategory(category: String): List<NewsItem>`
- `addTags(tags: List<String>, newsId: Int): Int`
- `getTags(newsId: Int): List<String>`
- `getSimilarNews(tags: List<String>): List<NewsItem>`

---

## 🔌 Web API metode

- `getTopStoriesByCategory(category: String): List<NewsItem>`
- `getAllStories(): List<NewsItem>`
- `getSimilarStories(uuid: String): List<NewsItem>`
- `getTags(imageUrl: String): List<String>`

---

## 📦 Kako pokrenuti aplikaciju

1. Kloniraj repozitorij:
   ```bash
   git clone https://github.com/eminahamamdzic/RMA-2024-2025-Emina-Hamamdzic.git
