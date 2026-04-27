# AmitySpace Backend

Backend service for **AmitySpace**, a sensory-aware public space exploration application for Melbourne CBD.

This service aggregates public data sources and structured profile data into a unified API for:

- map-ready location summaries
- location detail pages
- crowd status
- sensory profile information
- preference-based matching and filtering

The current backend implementation primarily supports the backend scope behind:
- Interactive Sensory Map
- Sensory Profile Cards
- Personalised Participation Planning
- parts of Crowd Planning and Data Infrastructure

---

## Overview

AmitySpace Backend is responsible for collecting external location and crowd-related data, combining it with structured sensory profile data, and exposing a simplified API for the frontend.

The backend currently:
- fetches live or recent public data for locations, crowd counts, historical crowd records, and microclimate/noise data
- builds unified `Location` and `SensoryProfile` models
- returns summary data for map display and detail data for selected locations
- supports preference-based matching/filtering
- returns fallback values such as `Data unavailable` when formal structured data is not yet available  
:contentReference[oaicite:3]{index=3} :contentReference[oaicite:4]{index=4}

---

## Tech Stack

- **Java 21**
- **Spring Boot**
- **Maven**
- **Jackson**
- **REST-based external Open Data APIs**
- **Temporary in-memory structured seed data**
- **Future CSV-backed structured profile data**

---

## Current Capabilities

### Location list for map display
The backend returns a list of locations with:
- id
- name
- latitude / longitude
- crowd level
- status label
- place type
- preference match flag  
:contentReference[oaicite:5]{index=5}

### Location detail payload
The backend returns detail data for a selected location, including:
- base location information
- sensory profile
- place type
- preference match flag  
:contentReference[oaicite:6]{index=6}

### Current crowd status
Crowd level is derived from recent crowd count data and converted into a simplified status such as:
- LOW / MODERATE / HIGH
- Calm now / Moderate now / Busy now  
:contentReference[oaicite:7]{index=7} :contentReference[oaicite:8]{index=8}

### Temporary sensory profile support
The backend currently supports the following sensory profile fields:
- noise
- lighting
- predictability
- quiet space availability
- overall sensory score
- best times to visit
- good-to-know tips
- last updated  
Some of these are currently dynamic, while others depend on static seed/fallback data. :contentReference[oaicite:9]{index=9} :contentReference[oaicite:10]{index=10}

### Preference-based matching and filtering
The backend supports matching/filtering by:
- low crowd
- low noise
- low lighting
- quiet space required
- predictable environment
- place type  
:contentReference[oaicite:11]{index=11} :contentReference[oaicite:12]{index=12}

---

## Project Structure

```text
src/main/java/com/fomopotato/amityspace/
├── controller/
├── dto/
├── model/
├── data/record/
├── integration/dto/
└── service/
    ├── LocationApiService.java
    ├── LocationDataService.java
    ├── LocationQueryService.java
    ├── LocationFilterService.java
    ├── LocationPreferenceService.java
    ├── LocationProfileStaticDataService.java
    ├── LocationSensorMappingDataService.java
    ├── LocationHistoricalCrowdPatternDataService.java
    └── calculator/
        ├── CrowdLevelCalculator.java
        ├── NoiseCalculator.java
        └── PredictabilityCalculator.java
