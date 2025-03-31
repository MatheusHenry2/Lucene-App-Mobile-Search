# Lucene App Mobile Search 🚀🔍

## Technologies Used and Their Concepts

- **Android** 📱  
  *The platform used to build the app, manage navigation, and create the user interface.*

- **Kotlin** ⚡  
  *The primary programming language chosen for its concise syntax, modern features, and enhanced safety.*

- **Apache Lucene** 🔎  
  *A full-text search library used to index and perform efficient searches on film.*

- **Coroutines** ⏱  
  *A tool for asynchronous programming that enables executing background operations (like indexing and search) without blocking the UI.*

- **LiveData & ViewModel** 🔄  
  *Android architecture components that manage data in a lifecycle-aware manner, helping update the UI based on state changes while separating business logic from the UI.*

- **WorkManager** 🔄  
  *A background task scheduler used to periodically download new films from the API and index them, ensuring that the search index remains up-to-date even when the app is running in the background..*

 - **App Architecture**

## 🗂️ Project Structure

## 📁 Project Structure

📦 com.example.lucene  
├── data  
│   ├── model  
│   │   ├── request  
│   │   │   └── TMDbRequest.kt  
│   │   └── response  
│   │       └── TMDbMovieResponse.kt  
│   └── remote  
│       ├── repository  
│       │   └── TMDbRepository.kt  
│       └── service  
│           ├── TMDbApi.kt  
│           └── TMDbService.kt  
├── states  
│   ├── BaseStates.kt  
│   └── SearchStates.kt  
├── ui  
│   ├── main  
│   │   └── MainActivity.kt  
│   └── search  
│       ├── FilmAdapter.kt  
│       ├── SearchFragment.kt  
│       └── SearchViewModel.kt  
├── utils  
│   ├── AppPreferences.kt  
│   ├── Constants.kt  
│   ├── LuceneMovieIndexer.kt  
│   └── LuceneMovieIndexerSingleton.kt  
└── worker  
    └── DownloadMoviesWorker.kt

