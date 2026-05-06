# Election System

Election System is a full-stack voting platform built for transparent and privacy-focused elections.
It combines a Spring Boot backend and a React frontend to support voter registration, candidate management, election configuration, secure voting, and live public counts.

## Ratonale Behind this System

Elections should be easy to participate in, hard to manipulate, transparent in various forms and clear to understand.
This project focuses on:

- role-based access (`SUPER_ADMIN`, `ADMIN`, `CANDIDATE`, `VOTER`)
- voter verification and approval workflows
- one-person-one-vote per election
- state-aware election eligibility
- private ballots and public aggregate results

## What it currently supports

- Account registration and login for voters, candidates and admins
- Voter approval and rejection flow from admin dashboard
- Election creation with category and scope:
  - `PRESIDENT`
  - `GOVERNOR`
  - `CHAIRMAN`
- National and state-level elections
- Candidate management with:
  - party
  - biography
  - image upload
- Candidate profile display in voter portal
- Secure vote casting (no duplicate vote in same election)
- Live tally and participation metrics

## Tech stack

### Backend
- Java (Spring Boot)
- MongoDB for Data Persistence
- Spring Data MongoDB
- Token-based auth via interceptor/session records

### Frontend
- React Framework (Javscript)


## Project structure

- `src/main/java/electionsystem`: backend source
- `src/main/resources`: backend config/resources
- `election-frontend`: React frontend app

## Getting started

### 1) Prerequisites

- Java 17+ (project currently runs with newer Java as well)
- Maven
- Node.js + npm
- MongoDB running on `localhost:27017`

### 2) Run backend

```bash
cd /home/thaguymaxx/Documents/Election_System
mvn spring-boot:run
```

Backend API default URL:

`http://localhost:8080`

### 3) Run frontend

```bash
cd /home/thaguymaxx/Documents/Election_System/election-frontend
npm install
npm start
```

Frontend default URL:

`http://localhost:3000`

## Notes on seeded data

The backend includes startup seed logic for baseline elections/candidates.
You can review and adjust this in:

`src/main/java/electionsystem/config/ElectionSeedConfig.java`

## Privacy model

- Votes are stored and counted, but the UI/API expose only aggregated results.
- Other users, candidates and admins cannot see who a specific voter selected.

## Future Implementation

- audit trail for admin actions
- exportable election analytics
- stronger media storage (S3/Cloudinary) for candidate images