# On Time

> *"Every moment is a choice. Every era is a teacher."*

A 3D narrative adventure spanning **12 historical eras** and **100 chapters** — teaching logic, history, and critical thinking through consequences, not lectures.

Built with Java, LWJGL, and OpenGL. For Yen, Rush, and Lu.

---

## What Is This?

On Time is a story-driven game where your choices shape who you become. You follow four friends — Drew, Rush, Luísa, and Yenevieve — through a sprawling narrative that crosses 12 periods of human history, from Ancient Mesopotamia to the Digital Age.

But this isn't a history textbook with a game bolted on. Each era is a **lens** for a way of thinking:

| Era | Period | What It Teaches |
|-----|--------|----------------|
| Ancient Mesopotamia | 3500–2000 BCE | First Principles Thinking |
| Classical Athens | 500–300 BCE | Correlation vs Causation |
| Roman Republic | 509–27 BCE | Incentives Drive Behavior |
| Early Christianity | 30–300 CE | The Steelman Principle |
| Tang Dynasty China | 618–907 CE | Opportunity Cost |
| Medieval Europe | 1000–1300 CE | Long-Term vs Short-Term Thinking |
| Renaissance Italy | 1400–1550 | Emotional Regulation |
| Age of Exploration | 1500–1700 | Bayesian Updating |
| French Revolution | 1789–1799 | Moral Tradeoff Awareness |
| Industrial Revolution | 1760–1840 | Tragedy of the Commons |
| World War II | 1939–1945 | Second-Order Effects |
| Digital Age | 1990–Present | The Danger of Certainty |

You don't study these principles. You **live them** — through dialogue choices, moral dilemmas, and an Evidence Board that asks you to classify what you *know* vs what you *feel* vs what you *infer*.

## Design Target

- **40–50+ hours** of gameplay across 100 chapters and 5 story arcs
- **Heavy branching** — choices matter, characters remember, people die permanently
- **Cognitive Virtue System** — the game quietly tracks how you *think*, not just what you *choose*
- **No right answers** — only tradeoffs, consequences, and who you become

---

## Systems

### 🧠 Cognitive Virtues
Twelve critical thinking principles tracked as gameplay mechanics, distilled into five HUD virtues:

| HUD Virtue | Built From |
|-----------|-----------|
| **Humility** | Bayesian Updating + Danger of Certainty |
| **Patience** | Long-Term Thinking + Opportunity Cost |
| **Clarity** | First Principles + Causal Reasoning + Second-Order Effects |
| **Empathy** | Steelman + Moral Tradeoffs + Tragedy of the Commons |
| **Resolve** | Emotional Regulation + Incentives Awareness |

### 📋 Evidence Board
Press **TAB** to open. Classify evidence as **Fact**, **Inference**, or **Emotional Reaction**. Correct classification strengthens your cognitive virtues. Misclassification triggers gentle feedback — not punishment, learning.

### 🌬️ Breath Mechanic
Hold **SHIFT** during dialogue to slow time. A deliberate pause before reacting — teaching emotional regulation through gameplay. Sustained breathing earns cognitive bonuses.

### 🏛️ Historical Era Transitions
Each chapter maps to a historical era. When you enter a new era, the game transitions with the era's name, time period, and associated cognitive principle.

---

## Running

### Prerequisites
- Java 17+
- Maven 3.8+

### Build & Run
```bash
mvn package
java -XstartOnFirstThread -jar target/on-time-0.2.0.jar
```

> The `-XstartOnFirstThread` flag is required on macOS for GLFW. On Windows/Linux, omit it.

### Controls

| Key | Action |
|-----|--------|
| **SPACE / ENTER / Click** | Advance dialogue / Select choice |
| **W/S** or **↑/↓** | Navigate choices |
| **1/2/3/4** | Quick-select choice |
| **TAB** | Open Evidence Board |
| **Hold SHIFT** | Breath Mechanic (slows time) |
| **ESC** | Pause / Resume |

---

## Auto-Updater

On Time includes a built-in auto-updater. When the game launches, it checks [GitHub Releases](https://github.com/ZRosserMcIntosh/on-time/releases) for a newer version. If one is available, it downloads and applies the update automatically.

### How It Works

```
Launch
  │
  ├─ Apply any pending update from last run
  │
  ├─ Check GitHub API: /repos/ZRosserMcIntosh/on-time/releases/latest
  │
  ├─ Compare semver (current vs latest tag)
  │
  ├─ If newer: download .jar asset → replace running jar → restart
  │     └─ If jar is locked (Windows): defer to next launch
  │
  └─ If current: proceed to game
```

### Releasing an Update

1. Bump `VERSION` in `Main.java` (e.g. `"0.3.0"`)
2. `mvn package`
3. Create a [GitHub Release](https://github.com/ZRosserMcIntosh/on-time/releases/new) with tag `v0.3.0`
4. Attach `target/on-time-0.3.0.jar` as a release asset
5. Every installed copy updates automatically on next launch

### What Gets Updated

| Component | How It Updates |
|-----------|---------------|
| Game engine code | Replaced via new jar |
| Rendering pipeline | Replaced via new jar |
| New chapters/eras | Bundled in jar resources |
| Save files | **Never touched** — saves persist across updates |
| Config/settings | **Never touched** — stored outside the jar |

---

## Project Structure

```
src/main/java/com/ontime/
├── Main.java                    # Entry point, macOS restart, auto-updater
├── GameEngine.java              # Game loop, state machine, system coordination
├── AutoUpdater.java             # GitHub Releases auto-update
├── engine/
│   ├── Window.java              # GLFW window, OpenGL context
│   ├── Timer.java               # Frame timing
│   └── input/
│       ├── InputHandler.java    # Keyboard input
│       └── MouseHandler.java    # Mouse input
├── rendering/
│   ├── MasterRenderer.java      # 3D pipeline: perspective projection, shaders, cube geometry
│   ├── Camera3D.java            # Lerp camera with shake
│   ├── SceneRenderer.java       # Environment + character rendering
│   ├── DialogueRenderer.java    # Dialogue box + choice rendering
│   ├── UIRenderer.java          # Title screen, chapter cards, era transitions
│   ├── HUDRenderer.java         # Cognitive virtue bars, era indicator, breath visual
│   └── TransitionRenderer.java  # Fade to/from black
├── narrative/
│   ├── StoryState.java          # All persistent state (flags, relationships, virtues, beliefs)
│   ├── Chapter.java             # Chapter metadata + scenes
│   ├── Scene.java               # 3D scene (environment, camera, characters, dialogue)
│   ├── DialogueNode.java        # One line of dialogue
│   ├── Choice.java              # Player choice with effects + cognitive metadata
│   ├── Condition.java           # Branching conditions (flags, virtues, beliefs, eras)
│   ├── Effect.java              # State changes (relationships, virtues, evidence, ripples)
│   ├── DialogueRunner.java      # Typewriter text, choice selection, cognitive tracking
│   ├── ChapterManager.java      # JSON chapter loading
│   ├── SaveManager.java         # Save/load to JSON
│   └── CutscenePlayer.java      # Scripted sequences
├── logic/
│   ├── CognitiveVirtueSystem.java  # Virgil's 12 principles as game mechanics
│   ├── EvidenceBoard.java          # Fact/Inference/Emotion classification
│   └── BreathMechanic.java         # Time dilation + emotional regulation
├── history/
│   ├── HistoricalEra.java          # Era metadata (name, period, color, principle)
│   └── HistoricalEraManager.java   # 12 eras + chapter mapping
├── characters/
│   ├── Character.java              # Character state (emotion, alive, color)
│   └── CharacterManager.java       # Character registry
└── audio/
    └── AudioEngine.java            # OpenAL music, SFX, voice with ducking

src/main/resources/chapters/        # Chapter JSON files (prologue, chapter_1–5)
docs/
├── STORY_ARCHITECTURE.md           # Full 100-chapter story design
└── HISTORY_ERAS.md                 # 12 eras + cognitive principle mapping
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Build | Maven |
| Windowing | GLFW (via LWJGL 3.3.3) |
| Rendering | OpenGL 3.3 Core Profile |
| Audio | OpenAL (via LWJGL) |
| Math | JOML 1.10.5 |
| Serialization | Gson 2.10.1 |
| Distribution | GitHub Releases + auto-updater |

## Platforms

| Platform | Status |
|----------|--------|
| macOS (ARM64 + x86_64) | ✅ Supported |
| Windows (x64 + ARM64) | ✅ Supported |
| Linux (x64 + ARM64) | ✅ Supported |

---

## Status

This is an active work in progress.

- [x] 3D rendering pipeline (OpenGL 3.3, perspective projection, mood lighting)
- [x] Cognitive Virtue System (12 principles, 5 HUD virtues)
- [x] Evidence Board (classify facts vs inferences vs emotions)
- [x] Breath Mechanic (time dilation for emotional regulation)
- [x] 12 Historical Eras mapped to cognitive principles
- [x] Dialogue system with branching, timers, typewriter text
- [x] Auto-updater via GitHub Releases
- [x] Save/load system
- [x] Prologue + Chapters 1–5 written
- [ ] Font rendering (currently placeholder blocks)
- [ ] 3D character models (currently colored rectangles)
- [ ] Music and sound assets
- [ ] Chapters 6–100
- [ ] Windows .exe packaging via Launch4j

---

*For Yenevieve, Rush, and Luísa.*
*A small light is still a light.*
