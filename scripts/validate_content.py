#!/usr/bin/env python3
"""Validates cross-references in all content JSON files."""
import json, os, sys

BASE = "/home/user/Pokemon-game/app/src/main/assets/content"
errors = []

def load(path):
    try:
        with open(path) as f:
            return json.load(f)
    except Exception as e:
        errors.append(f"LOAD ERROR {path}: {e}")
        return None

# Load everything
moves_raw = load(f"{BASE}/moves/moves.json") or []
items_raw = load(f"{BASE}/items/items.json") or []
kanto_c = load(f"{BASE}/creatures/kanto.json") or []
johto_c = load(f"{BASE}/creatures/johto.json") or []
hoenn_c = load(f"{BASE}/creatures/hoenn.json") or []
all_creatures = kanto_c + johto_c + hoenn_c

kanto_a = load(f"{BASE}/areas/kanto_areas.json") or []
johto_a = load(f"{BASE}/areas/johto_areas.json") or []
hoenn_a = load(f"{BASE}/areas/hoenn_areas.json") or []
all_areas = kanto_a + johto_a + hoenn_a

kanto_t = load(f"{BASE}/trainers/kanto_trainers.json") or []
johto_t = load(f"{BASE}/trainers/johto_trainers.json") or []
hoenn_t = load(f"{BASE}/trainers/hoenn_trainers.json") or []
all_trainers = kanto_t + johto_t + hoenn_t

events  = load(f"{BASE}/events/events.json") or []
npcs    = load(f"{BASE}/npcs/npcs.json") or []
shops   = load(f"{BASE}/shops/shops.json") or []

# Build ID sets
move_ids    = {m["id"] for m in moves_raw}
item_ids    = {i["id"] for i in items_raw}
creature_ids = {c["id"] for c in all_creatures}
area_ids    = {a["id"] for a in all_areas}
trainer_ids = {t["id"] for t in all_trainers}
npc_ids     = {n["id"] for n in npcs}

print(f"Loaded: {len(move_ids)} moves, {len(item_ids)} items, "
      f"{len(creature_ids)} creatures, {len(area_ids)} areas, "
      f"{len(trainer_ids)} trainers, {len(npc_ids)} npcs")

# Check creature learnsets
for c in all_creatures:
    for le in c.get("learnset", []):
        mid = le["moveId"]
        if mid not in move_ids:
            errors.append(f"Creature {c['id']} '{c['name']}': unknown move {mid}")
    for ev in c.get("evolutions", []):
        eid = ev["toId"]
        if eid not in creature_ids:
            errors.append(f"Creature {c['id']} '{c['name']}': evolves to unknown {eid}")

# Check creature ID continuity
for region, rng, region_list in [("Kanto",range(1,152),kanto_c),
                                   ("Johto",range(152,252),johto_c),
                                   ("Hoenn",range(252,387),hoenn_c)]:
    ids_found = {c["id"] for c in region_list}
    for i in rng:
        if i not in ids_found:
            errors.append(f"MISSING {region} creature #{i}")

# Check trainer teams
for t in all_trainers:
    for entry in t.get("team", []):
        cid = entry["creatureId"]
        if cid not in creature_ids:
            errors.append(f"Trainer '{t['id']}': unknown creature {cid}")
        for mid in entry.get("moves", []):
            if mid not in move_ids:
                errors.append(f"Trainer '{t['id']}': unknown move {mid}")

# Check area encounter creature IDs
for a in all_areas:
    enc = a.get("encounters", {})
    for terrain in ("grass", "water", "cave"):
        for entry in enc.get(terrain, []):
            cid = entry["creatureId"]
            if cid not in creature_ids:
                errors.append(f"Area '{a['id']}' {terrain}: unknown creature {cid}")
    for tid in a.get("trainers", []):
        if tid not in trainer_ids:
            errors.append(f"Area '{a['id']}': unknown trainer {tid}")

# Report
if errors:
    print(f"\n{'='*60}")
    print(f"VALIDATION FAILED — {len(errors)} error(s):")
    for e in errors[:50]:  # show first 50
        print(f"  • {e}")
    if len(errors) > 50:
        print(f"  … and {len(errors)-50} more")
    sys.exit(1)
else:
    print("\n✓ All cross-references valid!")
