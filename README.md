# Multilingual Airport Kiosk Agent — Hackathon Build

Full-stack demo: Angular frontend + Spring Boot backend + Gemini 2.5 Flash
(free tier) for multilingual conversational AI. **No database** — all mock
flights, bookings, seats, and baggage policies live in one JSON file that's
loaded into memory at startup. No external airline API needed.

## Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- Angular CLI: `npm install -g @angular/cli`
- A free Gemini API key: https://aistudio.google.com/apikey

## 1. Backend setup

```bash
cd backend
export GEMINI_API_KEY=your_gemini_key_here
mvn spring-boot:run
```

Backend runs on **http://localhost:8080**.
- Test flights list: http://localhost:8080/api/flights
- Chat endpoint: `POST http://localhost:8080/api/chat`
  ```json
  { "sessionId": "demo-1", "message": "I need to check in, my PNR is ABC123" }
  ```

If you don't want to use an environment variable, paste your key directly
into `backend/src/main/resources/application.properties` under
`gemini.api.key`.

## 2. Frontend setup

```bash
cd frontend
npm install
ng serve
```

Frontend runs on **http://localhost:4200**. Open it in **Chrome** — the
voice features use the Web Speech API, which Chrome supports best.

## Mock data — no database

All demo data lives in `backend/src/main/resources/mock-data.json`:
flights, passengers, bookings, seats, and baggage policies. On startup,
`MockDataStore.java` reads this file into memory once and keeps it there
for the app's lifetime — no JPA, no schema, no H2 console.

Actions like check-in or seat selection mutate these in-memory objects
directly (e.g. `booking.setCheckedIn(true)`), so state changes persist
for the rest of that run but **reset when you restart the backend** — same
behavior as before, just simpler underneath.

**To add or change test data**, just edit `mock-data.json` directly — no
migrations, no SQL, no restart-the-database step. Keep `id` values unique
within each array, and `flightId`/`passengerId` in `bookings`/`seats` must
match an `id` in `flights`/`passengers`.

## Language support (current build)

**Text input:** fully automatic for any language. Gemini detects the
language of whatever's typed and replies in the same language — no setup.

**Voice input:** tap the mic and speak. The browser's SpeechRecognition
API listens using your **browser/OS locale** (`navigator.language`)
automatically — no dropdown, no manual step. This is a real trade-off, not
a full fix: if the kiosk's browser is set to English but a passenger
speaks Hindi, transcription accuracy will suffer, because SpeechRecognition
can't detect a spoken language from audio the way Gemini can from text.
Good enough for a hackathon demo where you control the browser's locale
per language you're demonstrating; not what you'd ship to a real airport.

**Voice output:** the reply is spoken back in whatever language Gemini
detected from the text, regardless of the browser's locale — this part is
always automatic and correct.

## Stretch goal: true automatic spoken-language detection

If time allows, `backend/.../service/WhisperService.java` and
`TranscriptionController.java` are already built and wired to Groq's free
Whisper API, which *does* detect the spoken language directly from audio
— no locale guessing needed. They're just not called by the frontend yet.
To switch it on:

1. Get a free Groq key: https://console.groq.com/keys, set `groq.api.key`
   in `application.properties` (or `GROQ_API_KEY` env var).
2. In `frontend/src/app/services/voice.service.ts`, replace the
   `SpeechRecognition`-based `listen()` method with `MediaRecorder`-based
   audio capture (record on mic-tap, stop on second tap).
3. In `chat.service.ts`, use the existing `transcribeAudio(blob)` method —
   already implemented, POSTs to `/api/transcribe` and returns `{ text }`.
4. In `app.component.ts`, call `transcribeAudio()` after recording stops,
   then pass the returned text into the existing `callBackend()` flow.

This roughly doubles API calls per voice turn (Whisper + Gemini instead of
just Gemini) and adds an audio upload round-trip, which is why it's a
stretch goal rather than the default — worth it only if the demo needs to
prove true unattended multilingual voice input.

## Demo script (3 minutes, 5 languages)

Set your browser/OS language to match before each voice turn, or just
type — typed input needs no locale matching at all.

1. **English** — type: *"What's the status of flight AI202?"* → gate,
   terminal, departure time.
2. **Hindi** — say (or type): *"मुझे चेक-इन करना है, मेरा PNR ABC123 है"* →
   assistant checks in Rohan Mehta, assigns a seat, replies and speaks
   back in Hindi.
3. **Spanish** — say (or type): *"¿Cuál es la política de equipaje de Air
   India?"* → baggage allowance and fees, spoken in Spanish.
4. **Arabic** — say (or type): *"أحتاج إلى تسجيل الوصول، رقم الحجز
   MNO654"* → checks in Fatima Noor (3 bags booked — good follow-up to ask
   about excess baggage fees).
5. **Japanese** — say (or type): *"フライトEK512の状況を教えてください"* →
   flight status for EK512, reply spoken in Japanese.

Typing is the safest path live if you're not confident switching the
browser's locale mid-demo — the language detection and reply generation
are identical either way.

## Test PNRs seeded in mock-data.json
| PNR | Passenger | Language | Flight | Status |
|---|---|---|---|---|
| ABC123 | Rohan Mehta | Hindi | AI202 | Not checked in |
| XYZ789 | Maria Gonzalez | Spanish | 6E345 | Not checked in |
| QWE456 | John Smith | English | UA118 | Already checked in |
| JKL321 | Aiko Tanaka | Japanese | EK512 | Not checked in |
| MNO654 | Fatima Noor | Arabic | LH760 | Not checked in (3 bags) |

## Architecture

```
Angular (voice + text UI, Web Speech API)
   ↓ POST /api/chat
Spring Boot ChatController
   ↓
GeminiService  →  Gemini 2.5 Flash API (free tier)
   ↓ (parsed intent + entities + reply, language auto-detected from text)
CheckInService / SeatService / BaggageService / FlightStatusService
   ↓
MockDataStore → mock-data.json loaded into memory at startup
```

One Gemini call per message does language detection + intent
classification + entity extraction + reply generation — keeps latency and
API usage low, which matters on a free-tier rate limit during a live demo.

## Known shortcuts (intentional, for hackathon speed)
- No database — mock-data.json in memory, resets on backend restart.
- No auth — kiosk is anonymous, as a real airport kiosk would be.
- Session memory is a plain in-memory `Map`, not Redis — resets on backend restart.
- Voice input language is the browser/OS locale, not detected from audio
  (see Stretch goal above for the proper fix).
- 5 languages have full mock-data test coverage (Hindi, Spanish, English,
  Japanese, Arabic); Gemini itself understands far more via text input.
- Seat selection UI shows raw JSON in the "data card" rather than a visual
  seat map — fine for demo narration, worth polishing if you have time left.
