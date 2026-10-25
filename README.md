# Mini Militia (Doodle Army 2) - Native Android & Desktop Replica

A complete, high-performance 2D multiplayer shooter replica of **Doodle Army 2: Mini Militia**, developed in **Kotlin** and **LibGDX** with **Box2D physics**.

Built following a systematic reverse-engineering analysis using the **REA** (Reverse Engineering Anything) framework on the original Android APK.

---

## 🎮 Key Features

- **Accurate Movement & Jetpack Physics**:
  - Box2D 2D rigid-body simulation calibrated to match Chipmunk2D mechanics (`PPM = 32.0`, `Gravity = -22.0 m/s²`).
  - Jetpack thrust with dynamic fuel consumption (`25 fuel/sec`) and passive recharge (`18 fuel/sec`).
  - Ground walking, airborne strafing, inertial drift, and terminal velocity capping.
- **24 Complete Weapons Across 6 Categories**:
  - **Handguns**: Desert Eagle, .44 Magnum, Hunting Pistol.
  - **SMGs**: Tec-9, Micro UZI, MP5.
  - **Rifles**: AK-47, M16 (3-round burst), Tavor TAR-21, XM8.
  - **Shotguns**: Pump Shotgun (8 spread pellets), AA-12 Automatic Shotgun.
  - **Snipers**: M93BA Bolt-Action, M14 Semi-Auto Sniper.
  - **Heavy & Special**: SMAW Rocket Launcher (explosive splash), RG-6 Grenade Launcher, Saw Gun (bouncing ricochet saw blades), PHASR Laser Rifle, Flamethrower.
  - **Throwables & Melee**: Machete (instant melee strike), Frag Grenade, Poison Gas Grenade, Proximity Mine, EMP Grenade.
  - Dynamic recoil kick, spread cones, dual-wielding, reload cooldowns, and audio playback.
- **40 Original & Authentic TMX Maps**:
  - Outpost, Catacombs, Ambush, Crossfire, Bottleneck, Pyramid, Snowblind, Icebox, Suspension, and more.
  - Automated parser converting TMX polygon and polyline layers (`co_poly`, `collision`) into Box2D static `ChainShape` and `PolygonShape` fixtures.
  - Dynamic item spawners for health packs, boost tanks, and weapon pickups with respawn cooldowns.
- **Smart Solo Practice AI Bots**:
  - Autonomous finite-state machine AI (`Patrol`, `Chase`, `Attack`, `Retreat`).
  - Jetpack hovering, platform navigation, obstacle avoidance, dynamic aiming, and responsive burst shooting.
- **Multiplayer Networking (LAN & Online Rooms)**:
  - Embedded **Ktor WebSocket** server hosted directly on Android/Desktop device for zero-config Local Wi-Fi / Hotspot battles.
  - Client state synchronization: 25 Hz position/velocity updates, deterministic bullet propagation, authoritative hit registration, and synchronized kill feed.
- **Mobile-First Touch Controls & Responsive HUD**:
  - Dual virtual joysticks (left: walk & jetpack thrust; right: 360° aim & auto-fire threshold).
  - Quick action buttons: Reload, Swap Weapon, Throw Grenade, Melee Knife Strike, Zoom Scope.
  - Viewport-unprojected coordinate mapping supporting all phone aspect ratios, notches, and screen resolutions.
  - HUD with health bar, boost meter, active weapon magazine/reserve, match countdown timer, and live kill feed.
- **Interactive Armory Catalog**:
  - Weapon browser displaying live stats: Power/Damage, Fire Rate, Velocity, Mag Capacity, Reload Duration, Recoil, and Dual-Wield support.
  - "Test Fire SFX" button for instant audio verification.

---

## 🏗️ Project Architecture

```
fighting-game/
├── android/                   # Native Android application module
│   ├── src/main/AndroidManifest.xml
│   ├── src/main/kotlin/com/vinay/minimilitia/AndroidLauncher.kt
│   └── build.gradle.kts       # Android Gradle Plugin 8.3.2, compileSdk 34
├── core/                      # Shared multiplatform game logic (LibGDX + Kotlin)
│   ├── src/main/kotlin/com/vinay/minimilitia/
│   │   ├── constants/         # Physics scale (PPM), viewport, collision category bitmasks
│   │   ├── weapons/           # WeaponConfig, WeaponDefinitions (24 weapons), Weapon state
│   │   ├── entities/          # Player, Projectile, WeaponPickup, BotAI
│   │   ├── physics/           # Box2D wrapper, ContactListener, fixed timestep accumulator
│   │   ├── maps/              # TMX map loader, collision shape generator, spawn parser
│   │   ├── ui/                # TouchControls (dual joysticks), InGameHUD
│   │   ├── audio/             # AudioManager (spatial sound effects, jetpack loop)
│   │   ├── net/               # Ktor WebSockets: LocalServer, NetworkManager, NetworkProtocol
│   │   ├── screens/           # MainMenuScreen, PlayScreen, ArmoryScreen
│   │   └── MiniMilitiaGame.kt # LibGDX Game entrypoint
│   └── src/test/kotlin/       # Deterministic automated unit test suite
├── desktop/                   # Desktop LWJGL3 runner module
│   └── src/main/kotlin/com/vinay/minimilitia/DesktopLauncher.kt
└── assets/                    # Shared game assets (40 maps, 94 SFX, 19 voices, textures)
    ├── audio/sfx/
    ├── audio/voice/
    ├── maps/
    └── textures/
```

---

## 🚀 Building & Running

### Prerequisites
- **JDK 21** or later
- **Android SDK 34** (with `cmdline-tools`, `build-tools 34.0.0`, `platforms;android-34`)
- Gradle 8.7 (or the included Gradle wrapper)

### 1. Run on Desktop
To launch the game on desktop (with keyboard, mouse, and virtual controls):
```bash
./gradlew :desktop:run
```
**Controls**:
- `W` / `Space`: Jetpack thrust
- `A` / `D`: Walk left / right
- `S`: Crouch / fast descent
- `Mouse`: Aim crosshair (left click to fire)
- `R`: Reload
- `Q`: Swap weapon
- `G`: Throw grenade
- `F`: Melee knife attack
- `Z`: Scope zoom

### 2. Build Android Debug APK
```bash
./gradlew :android:assembleDebug
```
Output:
`android/build/outputs/apk/debug/android-debug.apk` (~17 MB)

### 3. Build Signed Android Release APK
```bash
./gradlew :android:assembleRelease
```
Output:
`android/build/outputs/apk/release/android-release.apk` (~15 MB)

### 4. Install onto Android Phone or Emulator
```bash
adb install -r android/build/outputs/apk/release/android-release.apk
```

### 5. Run Automated Tests
```bash
./gradlew :core:test
```
Verifies weapon stats, reload cooldowns, projectile calculations, packet serialization, and physics constants.

---

## 📜 Reverse Engineering & Legal Note

This project was built from scratch as an original implementation in Kotlin and LibGDX using observable gameplay mechanics, mathematical constants, and reverse-engineered behavior gathered through the REA framework. It does not contain decompiled proprietary source code.
