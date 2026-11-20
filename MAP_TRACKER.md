# Interactive Map: Feature Tracker

Tracks the Map section requirements. Check an item off (`[x]`) only once it is fully done, and update the note and status at the same time.

**Legend:** `[x]` done · `[ ]` not done · ⚠️ partial (see note)

**Last updated:** 2026-11-19 (branch `feature/map-screen`)

---

## 1. Interactive Campus Map
**Assigned:** Anton · **Status:** For Review

- [x] Mapbox SDK integration into Android Studio
- [x] Display DLSU Manila campus map
- [x] Enable map zooming, panning, and recentering
- [x] Display clickable markers for campus buildings and points of interest
  - 13 buildings plus 8 points of interest (2 each: food, restroom, study, hangout). POI coordinates are approximate; verify on site.
- [x] Distinguishable icons for food, restrooms, study areas, and buildings
- [x] Display the user's current location when location permission is granted OR by manual selection

## 2. Map Search & Location Discovery
**Assigned:** Anton · **Status:** For Review

- [x] Search for buildings, classrooms, food places, restrooms, study areas, and other campus locations by name or classroom number
  - Classrooms (G301, H305, etc.) and indoor places are **placeholder data**; replace with real rooms.
- [x] Display matching destinations with their relevant building and floor information
  - Placeholder classrooms and indoor restrooms/study areas show "Building · Floor N".
- [x] Allow users to select a result and locate it on the map
- [x] Support category filters for buildings, food, restrooms, study areas, and waiting/hangout areas

## 3. Location Category Filters
**Assigned:** Anton · **Status:** In Progress

- [x] Filter map markers by buildings, food places, restrooms, study areas, and waiting/hangout areas
  - ⚠️ Filter chips work; each category now has 2 sample places, but real campus data is still needed.
  - note this is okay for the time being, real data will be confirmed later
- [x] Allow users to clear selected filters

## 4. Recently Added Places
**Assigned:** Anton · **Status:** Not Started

- [ ] Display recently moderator-approved student recommendations
  - The current "Recently searched" list is the user's search history, not approved places.
- [ ] Allow users to select a recently added place and view its map marker and details

## 5. Place Details, Ratings & Reviews
**Assigned:** Anton · **Status:** In Progress

- [ ] Display the selected location's name, category, description, and relevant building information
  - ⚠️ Name, category, and location line are shown; the description is stored but not displayed.
- [ ] Display average rating and review count, with relevant reviews that can be expanded
- [ ] Users can rate the location (1 to 5 stars)
- [ ] Users can submit written reviews that appear in the place's recent reviews
- [ ] Include a Get Directions action for supported destinations
  - ⚠️ Only a demo straight-line route (St. La Salle Hall → Henry Sy Sr. Hall).

## 6. Community-Approved Map Markers
**Assigned:** Anton · **Status:** Not Started

- [ ] Display moderator-approved, student-created locations as public map markers
- [ ] Use appropriate category icons for approved locations
- [ ] Open location details when a marker is selected

## 7. Hotspot Indicators
**Assigned:** Anton · **Status:** In Progress (MCO2 item done)

- [ ] Identify existing locations receiving recent upvotes and reviews
- [ ] Display a Hotspot label on qualifying locations
- [ ] Show selected hotspots in a popular places section
- [x] **MCO2:** One marker at a fixed location with a Hotspot label
  - "Plaza Hangout Spot" (`sample_hotspot`, between Connon and La Salle) has a flame marker, a "Hotspot: Plaza" label, and "Hotspot · …" on its place card and search result. Fixed sample data; coordinates are approximate. Real hotspot detection needs community upvotes (Pauline's module) and is not needed for MCO2.

---

## Known Gaps
- `CampusMapData` has 13 buildings and 8 approximate points of interest (food, restroom, study, hangout), and 9 placeholder indoor places (6 classrooms, 2 restrooms, 1 study area) with floors. Real classroom/floor data still needs to be collected.
