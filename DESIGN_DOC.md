# Monster RPG — Technical Design Document

> Clean-room monster-catching RPG for Android (Kotlin + Jetpack Compose)
> Regions: Kanto → Johto → Hoenn (internal labels only; all user-facing text is original)

---

## Assumptions

1. Android min SDK 26 (Android 8.0), target SDK 34.
2. Single-player, offline only.
3. Creatures are numbered #001–#386 with entirely original names.
4. Type system mirrors the canonical 18-type chart under clean-room names (see §4).
5. IV/EV system is simplified: creatures have hidden Individual Values (0–31 per stat) rolled at creation; no Effort Values to grind — stat growth is purely level-based + IV offset.
6. Soft level cap: 100. After Hoenn Elite Four, post-game content can push beyond but is out of scope v1.
7. Content lives in JSON files bundled as Android assets; hot-swappable by replacing the `assets/content/` folder.
8. All legendaries are catchable in-game without mystery-event gating; event-only legendaries appear on named "Myth Islands" unlocked via story flags.

---

## 1. Module Structure

```
Pokemon-game/
├── app/                     Android application module
│   └── src/main/
│       ├── kotlin/com/monstergame/
│       │   ├── navigation/  Compose NavHost + destinations
│       │   ├── ui/          Compose screens + components + theme
│       │   ├── viewmodel/   StateFlow-based ViewModels
│       │   └── di/          Hilt modules
│       └── assets/content/  JSON data files (read-only at runtime)
│
├── game-engine/             Pure-Kotlin library (no Android deps)
│   └── src/main/kotlin/com/monstergame/engine/
│       ├── model/           Data classes: Creature, Move, Item, Trainer, etc.
│       ├── battle/          BattleEngine, DamageCalculator, AI, capture, status
│       ├── overworld/       OverworldEngine, TileMap, EncounterGenerator, EventSystem
│       └── persistence/     SaveManager, JSON serialisation
│
├── content/                 Content loading & validation library
│   └── src/main/kotlin/com/monstergame/content/
│       ├── model/           Mirrored data-transfer objects for JSON deserialisation
│       └── loader/          ContentLoader, ContentValidator, ContentRepository
│
└── scripts/                 Python 3 tooling
    ├── generate_content.py  Generates full JSON roster from templates
    ├── validate_content.py  Schema validation + cross-reference checks
    └── generate_maps.py     Generates placeholder tile-map JSON
```

---

## 2. Data Schemas

### 2.1 Creature (creature.schema.json)
```json
{
  "id": 1,
  "name": "Verdling",
  "types": ["FLORA"],
  "baseStats": { "hp": 45, "attack": 49, "defense": 49, "specialAttack": 65, "specialDefense": 65, "speed": 45 },
  "catchRate": 45,
  "baseExp": 64,
  "learnset": [
    { "level": 1, "moveId": 33 },
    { "level": 7, "moveId": 45 }
  ],
  "evolutions": [
    { "toId": 2, "method": "LEVEL", "param": 16 }
  ],
  "eggGroups": ["FLORA", "AMORPHOUS"],
  "genderRatio": 12,
  "habitat": ["GRASSLAND"],
  "regionId": "KANTO",
  "isStarter": true,
  "isLegendary": false,
  "description": "A small sprout creature that draws energy from sunlight."
}
```

### 2.2 Move (move.schema.json)
```json
{
  "id": 1,
  "name": "Tackle",
  "type": "NEUTRAL",
  "category": "PHYSICAL",
  "power": 40,
  "accuracy": 100,
  "pp": 35,
  "priority": 0,
  "effect": null,
  "effectChance": 0,
  "target": "SINGLE_OPPONENT",
  "description": "A full-body charge attack."
}
```

### 2.3 Area / Map (area.schema.json)
```json
{
  "id": "kanto_route_1",
  "name": "Verdant Path",
  "regionId": "KANTO",
  "type": "ROUTE",
  "connections": ["kanto_starttown", "kanto_pewtertown"],
  "tilemap": "kanto_route_1",
  "encounters": {
    "GRASS": [
      { "creatureId": 10, "minLevel": 3, "maxLevel": 5, "weight": 20 }
    ],
    "WATER": [],
    "CAVE": []
  },
  "trainers": ["trainer_001", "trainer_002"],
  "events": ["event_rival_route1"]
}
```

### 2.4 Trainer (trainer.schema.json)
```json
{
  "id": "gym_kanto_1",
  "name": "Brodrik",
  "trainerClass": "GYM_LEADER",
  "areaId": "kanto_gym_1",
  "reward": 1200,
  "badgeId": "KANTO_1",
  "team": [
    { "creatureId": 74, "level": 14, "moves": [33, 106, 111, 88] },
    { "creatureId": 95, "level": 16, "moves": [33, 106, 88, 157] }
  ],
  "aiProfile": "SMART",
  "preBattleText": "My rock-solid team will crush you!",
  "postBattleText": "Impressive. Take this badge.",
  "repeatable": false
}
```

### 2.5 Save Data Format (JSON via kotlinx.serialization)
```json
{
  "version": 1,
  "playerName": "Alex",
  "money": 3000,
  "playtimeSeconds": 7200,
  "position": { "areaId": "kanto_route_1", "x": 5, "y": 8 },
  "party": [ /* SerializedCreature[] max 6 */ ],
  "pc": { "box0": [], "box1": [], ... },
  "badges": ["KANTO_1", "KANTO_2"],
  "defeatedTrainers": ["trainer_001"],
  "bag": { "BALL": {"item_pokeball": 5}, "MEDICINE": {}, "KEY": {} },
  "flags": { "kanto_elite4_defeated": false, "johto_unlocked": false },
  "activeRegion": "KANTO"
}
```

---

## 3. Battle System

### 3.1 Damage Formula
```
Damage = floor(
  floor(
    (floor(2 * Level / 5 + 2) * Power * A / D) / 50
  ) + 2
) * Modifier
```
Where:
- `A` = attacker SpAtk (Special) or Atk (Physical)
- `D` = defender SpDef (Special) or Def (Physical)
- `Modifier` = STAB × TypeEffectiveness × Critical × Random × BurnMod × ScreenMod

| Component | Value |
|---|---|
| STAB (type match) | 1.5× |
| TypeEffectiveness | 0, 0.25, 0.5, 1, 2, 4 |
| Critical hit | 1.5× (base chance: 1/16) |
| Random | Uniform [0.85, 1.00] |
| Burn (physical) | 0.5× |

### 3.2 Stat Calculation
```
Stat = floor(floor((2 * Base + IV) * Level / 100 + 5) * NatureMod)
HP  = floor(floor((2 * Base + IV) * Level / 100 + Level + 10))
```
- IV range: 0–31 (rolled at creation, fixed thereafter)
- NatureMod: 1.0 (natures are present but neutral for simplicity in v1)

### 3.3 Capture Formula
```
CatchValue = floor(
  (3 * MaxHP - 2 * CurrentHP) / (3 * MaxHP) * CatchRate * BallMod * StatusMod
)
Captured if: random(0, 255) < CatchValue

ShakeChecks = 4 consecutive passes of: random(0, 65535) < ShakeThreshold
ShakeThreshold = floor(1048560 / sqrt(sqrt(16711680 / CatchValue)))
```

| Ball | BallMod |
|---|---|
| Basic Ball | 1.0 |
| Good Ball | 1.5 |
| Ultra Ball | 2.0 |
| Master Ball | 255 (auto-catch) |
| Net Ball | 3.5 vs AQUA/INSECT |
| Dusk Ball | 3.5 in caves/night |

| Status | StatusMod |
|---|---|
| Sleep / Frozen | 2.5 |
| Paralysed / Poisoned / Burned | 1.5 |
| None | 1.0 |

### 3.4 Turn Order
Speed ties broken randomly. Priority moves (+1 or higher) always go first. Switching out counts as priority +6 (always first).

### 3.5 Status Effects
| Status | Effect |
|---|---|
| BRN | -1/8 HP per turn; physical damage ×0.5 |
| PSN | -1/8 HP per turn |
| BPSN (bad poison) | -1/16, -2/16, … per turn |
| PAR | 25% chance to skip turn; speed ×0.5 |
| SLP | Skip 1–3 turns |
| FRZ | Skip turns; 20% chance to thaw each turn |
| CNF | 50% self-damage hit; wears off after 2–5 turns |
| BND | Trapped for 4–5 turns; -1/8 HP per turn |

---

## 4. Type Chart (Clean-Room Names)

| Attacking → | NEUTRAL | FLAME | AQUA | VOLT | FLORA | FROST | BRAWL | VENOM | TERRA | AERO | PSYCHE | INSECT | STONE | SHADE | WYRM | MURK | IRON | RADIANT |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| NEUTRAL | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | ½ | 0 | 1 | 1 | ½ | 1 |
| FLAME | 1 | ½ | ½ | 1 | 2 | 2 | 1 | 1 | 1 | 1 | 1 | 2 | ½ | 1 | ½ | 1 | 2 | 1 |
| AQUA | 1 | 2 | ½ | 1 | ½ | 1 | 1 | 1 | 2 | 1 | 1 | 1 | 2 | 1 | ½ | 1 | 1 | 1 |
| VOLT | 1 | 1 | 2 | ½ | ½ | 1 | 1 | 1 | 0 | 2 | 1 | 1 | 1 | 1 | ½ | 1 | 1 | 1 |
| FLORA | 1 | ½ | 2 | 1 | ½ | 1 | 1 | ½ | 2 | ½ | 1 | ½ | 2 | 1 | ½ | 1 | 1 | 1 |
| FROST | 1 | ½ | ½ | 1 | 2 | ½ | 1 | 1 | 2 | 2 | 1 | 1 | 1 | 1 | 2 | 1 | ½ | 1 |
| BRAWL | 2 | 1 | 1 | 1 | 1 | 2 | 1 | ½ | 1 | ½ | ½ | ½ | 2 | 0 | 1 | 2 | 2 | ½ |
| VENOM | 1 | 1 | 1 | 1 | 2 | 1 | 1 | ½ | ½ | 1 | 1 | 1 | ½ | ½ | 1 | 1 | 0 | 2 |
| TERRA | 1 | 2 | 1 | 2 | ½ | 1 | 1 | 2 | 1 | 0 | 1 | ½ | 2 | 1 | 1 | 1 | 2 | 1 |
| AERO | 1 | 1 | 1 | ½ | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 2 | ½ | 1 | 1 | 1 | ½ | 1 |
| PSYCHE | 1 | 1 | 1 | 1 | 1 | 1 | 2 | 2 | 1 | 1 | ½ | 1 | 1 | 1 | 1 | 0 | ½ | 1 |
| INSECT | 1 | ½ | 1 | 1 | 2 | 1 | ½ | ½ | 1 | ½ | 2 | 1 | 1 | ½ | 1 | 2 | ½ | ½ |
| STONE | 1 | 2 | 1 | 1 | 1 | 2 | ½ | 1 | ½ | 2 | 1 | 2 | 1 | 1 | 1 | 1 | ½ | 1 |
| SHADE | 0 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 2 | 1 | 1 | 2 | 1 | ½ | 1 | 1 |
| WYRM | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 2 | 1 | ½ | 0 |
| MURK | 1 | 1 | 1 | 1 | 1 | 1 | ½ | 1 | 1 | 1 | 2 | 1 | 1 | 2 | 1 | ½ | 1 | ½ |
| IRON | 1 | ½ | ½ | ½ | 1 | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 2 | 1 | 1 | 1 | ½ | 2 |
| RADIANT | 1 | 1 | 1 | 1 | 1 | 1 | 2 | ½ | 1 | 1 | 1 | 1 | 1 | 1 | 2 | 2 | ½ | 1 |

---

## 5. Level Curve — All Gyms + Elite Four

### 5.1 Kanto (Gyms 1–8 + Elite Four)

| # | Location (lore name) | Leader | Type | Team Lvl Range | Ace Lvl |
|---|---|---|---|---|---|
| G1 | Stoneridge Town | Brodrik | STONE | 12–16 | 16 |
| G2 | Mistfall City | Marina | AQUA | 18–24 | 24 |
| G3 | Voltharrow City | Killian | VOLT | 24–30 | 30 |
| G4 | Bloomvale City | Elowen | FLORA/VENOM | 30–36 | 36 |
| G5 | Spirithaven City | Morwenna | SHADE/VENOM | 36–42 | 42 |
| G6 | Psygate City | Sabine | PSYCHE | 42–48 | 48 |
| G7 | Cindermark City | Ignar | FLAME | 48–54 | 54 |
| G8 | Groundveil City | Dovak | TERRA | 52–58 | 58 |
| E1 | Apex Citadel | Lorin (FROST) | FROST | 58–62 | 62 |
| E2 | Apex Citadel | Thane (BRAWL) | BRAWL | 60–64 | 64 |
| E3 | Apex Citadel | Syvra (VENOM) | VENOM | 62–66 | 66 |
| E4 | Apex Citadel | Oryn (PSYCHE) | PSYCHE | 64–68 | 68 |
| CH | Apex Citadel | Champion Celeste | MIXED | 66–72 | 72 |

### 5.2 Johto (Gyms 1–8, NO Elite Four)

| # | Location | Leader | Type | Team Lvl Range | Ace Lvl |
|---|---|---|---|---|---|
| G1 | Falconcrest Town | Faro | AERO/NEUTRAL | 54–60 | 60 |
| G2 | Thornhive City | Briana | INSECT | 58–63 | 63 |
| G3 | Goldenharbor City | Whittaker | NEUTRAL | 60–65 | 65 |
| G4 | Echocanyon City | Vesper | SHADE | 62–67 | 67 |
| G5 | Tidalcove City | Nereis | AQUA | 64–68 | 68 |
| G6 | Ironharbor City | Cladd | IRON | 66–70 | 70 |
| G7 | Dragoncrest Town | Draveth | WYRM | 68–73 | 73 |
| G8 | Darkmarsh City | Kelvara | MURK | 71–76 | 76 |

*(After Johto G8, story directs player to Hoenn via sea route.)*

### 5.3 Hoenn (Gyms 1–8 + Elite Four)

| # | Location | Leader | Type | Team Lvl Range | Ace Lvl |
|---|---|---|---|---|---|
| G1 | Tidewatch Town | Shella | AQUA | 72–76 | 76 |
| G2 | Desertspire City | Gurath | BRAWL | 74–77 | 77 |
| G3 | Thornvale City | Elektra | VOLT | 75–78 | 78 |
| G4 | Frostpeak City | Embrik | FLAME | 76–79 | 79 |
| G5 | Skybridge City | Winona | AERO | 77–80 | 80 |
| G6 | Embercaldera City | Normis | NEUTRAL | 77–80 | 80 |
| G7 | Shadowrift City | Psylara | PSYCHE | 78–81 | 81 |
| G8 | Tempestgate City | Draven | WYRM | 79–82 | 82 |
| E1 | Stormspire Keep | Sidra (MURK) | MURK | 84–88 | 88 |
| E2 | Stormspire Keep | Glace (FROST) | FROST | 86–90 | 90 |
| E3 | Stormspire Keep | Draveth II (WYRM) | WYRM | 88–92 | 92 |
| E4 | Stormspire Keep | Auris (PSYCHE) | PSYCHE | 89–93 | 93 |
| CH | Stormspire Keep | Grand Master Kael | MIXED | 90–95 | 95 |

---

## 6. Wild Encounter Level Scaling

| Region | Route Tier | Level Range |
|---|---|---|
| Kanto | Early (Routes 1–5) | 2–8 |
| Kanto | Mid (Routes 6–12) | 10–22 |
| Kanto | Late (Routes 13–22) | 25–45 |
| Kanto | Dungeons | 20–50 |
| Johto | Early (Routes 1–6) | 48–58 |
| Johto | Mid (Routes 7–14) | 58–68 |
| Johto | Late (Routes 15–22) | 68–76 |
| Johto | Dungeons | 55–75 |
| Hoenn | Early (Routes 1–6) | 70–76 |
| Hoenn | Mid (Routes 7–14) | 76–82 |
| Hoenn | Late (Routes 15–22) | 82–90 |
| Hoenn | Dungeons | 78–90 |

---

## 7. Starter Pool & Availability

| # | Name | Type | Region | Notes |
|---|---|---|---|---|
| 1 | Verdling | FLORA | Kanto | Starter choice |
| 4 | Emberling | FLAME | Kanto | Starter choice |
| 7 | Aqualing | AQUA | Kanto | Starter choice |
| 152 | Sproutkit | FLORA | Johto | Starter choice |
| 155 | Embercub | FLAME | Johto | Starter choice |
| 158 | Tidekit | AQUA | Johto | Starter choice |
| 252 | Leafbud | FLORA | Hoenn | Starter choice |
| 255 | Ignisaur | FLAME | Hoenn | Starter choice |
| 258 | Mudlet | AQUA/TERRA | Hoenn | Starter choice |

**At game start:** Player picks ANY 2 from the 9 starters above.
**Unchosen starters:** Available as rare wild encounters in their home region (early routes, low encounter weight).

---

## 8. Legendary Locations

| Creature | Lore Name | Region | Location | Unlock Condition |
|---|---|---|---|---|
| #144 | Glacivern | Kanto | Frostspire Cave (Kanto NW) | Defeat Kanto G8 |
| #145 | Stormavian | Kanto | Thunderpeak Summit (Kanto E) | Defeat Kanto G8 |
| #146 | Embravian | Kanto | Cinderrift (Kanto S) | Defeat Kanto G8 |
| #150 | Voidmind | Kanto | Obsidian Cavern (Kanto SW) | Defeat Kanto Elite Four |
| #151 | Miraphel | Kanto | Myth Island ALPHA (sea S of Kanto) | Deliver Jade Tablet quest |
| #243 | Boltstride | Johto | Thunderplain (Johto E) | Defeat Johto G4 |
| #244 | Infernalion | Johto | Volcano Depths (Johto S) | Defeat Johto G4 |
| #245 | Tidehearth | Johto | Abyssal Grotto (Johto W) | Defeat Johto G4 |
| #249 | Tempestlord | Johto | Stormrift Island (Johto sea) | Defeat Johto G6 |
| #250 | Emberlord | Johto | Skyfire Peak (Johto N) | Defeat Johto G8 |
| #251 | Timelynx | Johto | Myth Island BETA (sea E of Johto) | Collect all Johto timestamps |
| #377 | Crystalith | Hoenn | Crystalcave (Hoenn NE) | Defeat Hoenn G4 |
| #378 | Glaciadon | Hoenn | Frostpeaks (Hoenn NW) | Defeat Hoenn G4 |
| #379 | Ironclad | Hoenn | Rusted Ruin (Hoenn SW) | Defeat Hoenn G4 |
| #380 | Auraveil | Hoenn | Myth Island GAMMA (Hoenn sea W) | Defeat Hoenn G6 |
| #381 | Tideclaw | Hoenn | Myth Island DELTA (Hoenn sea E) | Defeat Hoenn G6 |
| #382 | Oceanmaw | Hoenn | Marine Trench (Hoenn deep sea) | Defeat Hoenn G8 |
| #383 | Magmawrath | Hoenn | Magma Core (Hoenn volcano) | Defeat Hoenn G8 |
| #384 | Skydrake | Hoenn | Sky Pinnacle (Hoenn peak) | Defeat Hoenn Elite Four |
| #385 | Wishsprite | Hoenn | Myth Island EPSILON | Full Hoenn Dex |
| #386 | Voidform | Hoenn | Myth Island ZETA | Defeat all 3 region Champions |

---

## 9. Island Unlock Logic

Myth Islands appear on the overworld sea when the corresponding flag is set:
```
MYTH_ALPHA_UNLOCK: flag "jade_tablet_delivered"
MYTH_BETA_UNLOCK:  flag "johto_timestamps_all"  (collect 8 time shards from Johto ruins)
MYTH_GAMMA_UNLOCK: flag "hoenn_gym6_defeated"
MYTH_DELTA_UNLOCK: flag "hoenn_gym6_defeated"
MYTH_EPSILON_UNLOCK: flag "hoenn_dex_complete" (seen all Hoenn non-legend creatures)
MYTH_ZETA_UNLOCK: flag "all_champions_defeated"
```

---

## 10. AI Profiles

| Profile | Behaviour |
|---|---|
| RANDOM | Picks a random valid move each turn |
| BASIC | Prefers moves with higher power |
| SMART | Uses super-effective moves when available; switches if HP < 25% |
| CHAMPION | Full prediction: considers type matchups, status spreading, healing items at 50% HP; switches strategically |

---

## 11. PC Storage

- 30 boxes × 30 slots = 900 creature slots.
- Boxes persist in SaveData.
- HealingCenter NPCs offer free full party heal + PC access.

---

## 12. Money & Shops

- Starting money: 3 000 gold.
- Trainer battle reward = leader_money × (avg team level).
- Shops sell balls, potions, status heals per region tier.
- Department stores (one per region) stock full item catalogue.

---

## 13. Quest/Event System

Events are JSON-driven:
```json
{
  "id": "event_rival_route1",
  "trigger": { "type": "AREA_ENTER", "areaId": "kanto_route_1" },
  "condition": { "flag": "starter_chosen", "value": true },
  "actions": [
    { "type": "DIALOGUE", "npcId": "rival_kanto" },
    { "type": "BATTLE", "trainerId": "rival_kanto_1" },
    { "type": "SET_FLAG", "flag": "rival_route1_done" }
  ]
}
```

---
