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

📦 com.example.lucene  
├── **data**  
│   ├── **model** – contains API models  
│   │   ├── `request` – models used for sending/receiving data  
│   │   └── `response` – response wrappers or paginated data  
│   ├── **remote** – TMDb API interface  
│   └── **repository** – handles API interaction logic  
│  
├── **ui** – user interface components  
│   ├── `main` – entry point activity  
│   └── `search` – search screen fragment, adapter, and logic  
│  
├── **states** – defines UI actions and event states  
│  
├── **utils** – Lucene indexer, constants, shared preferences, etc.  
│  
├── **worker** – background worker to fetch and index new movies
