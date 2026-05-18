# SkillExchange Architecture

## MVVM Architecture

`
UI Layer (Fragments + ViewModels + Adapters)
        |
        | LiveData / StateFlow
        |
Repository Layer (coordinates Local DB + Firebase)
        |              |
Room (Local DB)   Firebase Cloud
14 Entities       Firestore + RTDB
14 DAOs           Auth + Sync
`

## Key Packages

| Package | Purpose |
|---------|---------|
| ui/fragment | All screen Fragments |
| ui/viewmodel | ViewModels with business logic |
| ui/adapter | RecyclerView adapters |
| data/local | Room DB, DAOs, Entities |
| data/firebase | Firebase sync layer |
| data/repository | Repository pattern |
| i | AI match engine |
| utils | Helpers and extensions |

## Database Schema
- 14 Room Entities (User, Offer, Swap, Chat, Notification, etc.)
- Schema version: 5 with migration support
