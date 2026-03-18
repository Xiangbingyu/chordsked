# ChordSked Phase 1 Prototype

This is a high-fidelity interactive prototype for the ChordSked Music Management System (Phase 1).

## Tech Stack
*   React
*   Tailwind CSS
*   Vite
*   Lucide React (Icons)
*   React Router DOM

## How to Run

1.  Open the terminal in this directory: `prototypes/chord-sked-prototype`
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm run dev
    ```
4.  Open the link shown in the terminal (usually `http://localhost:5173`).

## Implemented Features (Phase 1)

*   **Dashboard**:
    *   KPI cards (Students, Today's Classes, Orders, Homework).
    *   Today's schedule overview.
    *   Quick actions shortcuts.
*   **Schedule Management (排课管理)**:
    *   Weekly calendar view.
    *   Visual distinction for different teachers/course types.
    *   Interactive class slots (click to view details).
*   **Student Management (学员管理)**:
    *   Student list with search and filter.
    *   Visual indicators for course balance and level.
*   **Group Buy Orders (团购核销)**:
    *   Order list from external platforms (Douyin/Meituan).
    *   "Write-off" (核销) action simulation.
    *   Status tracking.
*   **Teaching Center (教学中心)**:
    *   Homework list.
    *   Grading interface simulation.
    *   Multimedia homework types (Video/Audio/Text).
