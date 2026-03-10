#!/usr/bin/env python3
"""
Generate region and area JSON files for the Monster RPG game.
Creates:
  - regions/johto.json
  - regions/hoenn.json
  - areas/kanto_areas.json
  - areas/johto_areas.json
  - areas/hoenn_areas.json
"""

import json
import os

BASE_DIR = "/home/user/Pokemon-game/app/src/main/assets/content"
REGIONS_DIR = os.path.join(BASE_DIR, "regions")
AREAS_DIR = os.path.join(BASE_DIR, "areas")

os.makedirs(REGIONS_DIR, exist_ok=True)
os.makedirs(AREAS_DIR, exist_ok=True)


def enc(creature_id, min_lv, max_lv, weight):
    return {"creatureId": creature_id, "minLevel": min_lv, "maxLevel": max_lv, "weight": weight}


def area(area_id, name, region_id, area_type="ROUTE",
         connections=None, grass=None, water=None, cave=None,
         trainers=None, events=None, shop_id=None,
         healing=False, pc=False, gym_level=None):
    obj = {
        "id": area_id,
        "name": name,
        "regionId": region_id,
        "type": area_type,
        "connections": connections or [],
        "encounters": {
            "grass": grass or [],
            "water": water or [],
            "cave": cave or []
        },
        "trainers": trainers or [],
        "events": events or [],
        "shopId": shop_id,
        "healingCenter": healing,
        "pc": pc
    }
    if gym_level is not None:
        obj["gymLevel"] = gym_level
    return obj


# ============================================================
# KANTO AREAS
# ============================================================
kanto_areas = [
    area("kanto_starttown", "Pallet Shore", "KANTO", "TOWN",
         connections=["kanto_route1", "kanto_route22"],
         healing=True, pc=True,
         events=["event_kanto_intro"]),

    area("kanto_route1", "Route 1", "KANTO", "ROUTE",
         connections=["kanto_starttown", "kanto_stoneridge"],
         grass=[enc(16,3,5,40), enc(19,3,5,30), enc(1,3,5,30)]),

    area("kanto_stoneridge", "Stoneridge City", "KANTO", "TOWN",
         connections=["kanto_route1", "kanto_route2", "kanto_route22"],
         healing=True, pc=True, gym_level=[12,16]),

    area("kanto_route2", "Route 2", "KANTO", "ROUTE",
         connections=["kanto_stoneridge", "kanto_viridian_forest"],
         grass=[enc(16,4,7,30), enc(19,4,7,30), enc(10,4,7,25), enc(13,4,7,15)]),

    area("kanto_viridian_forest", "Viridian Forest", "KANTO", "CAVE",
         connections=["kanto_route2", "kanto_route3"],
         cave=[enc(10,5,8,30), enc(13,5,8,30), enc(48,5,8,20), enc(16,5,8,20)]),

    area("kanto_route3", "Route 3", "KANTO", "ROUTE",
         connections=["kanto_viridian_forest", "kanto_mistfall"],
         grass=[enc(16,8,12,30), enc(21,8,12,25), enc(27,8,12,25), enc(19,8,12,20)]),

    area("kanto_mistfall", "Mistfall City", "KANTO", "TOWN",
         connections=["kanto_route3", "kanto_mt_moon", "kanto_route4"],
         healing=True, pc=True, gym_level=[17,22]),

    area("kanto_mt_moon", "Mt. Moon", "KANTO", "CAVE",
         connections=["kanto_mistfall", "kanto_route4"],
         cave=[enc(50,10,14,30), enc(23,10,14,25), enc(74,10,14,25), enc(41,10,14,20)]),

    area("kanto_route4", "Route 4", "KANTO", "ROUTE",
         connections=["kanto_mt_moon", "kanto_voltharrow"],
         grass=[enc(21,14,17,30), enc(27,14,17,25), enc(23,14,17,25), enc(41,14,17,20)]),

    area("kanto_voltharrow", "Voltharrow City", "KANTO", "TOWN",
         connections=["kanto_route4", "kanto_route6", "kanto_route5"],
         healing=True, pc=True, gym_level=[22,27]),

    area("kanto_route6", "Route 6", "KANTO", "ROUTE",
         connections=["kanto_voltharrow", "kanto_bloomvale"],
         grass=[enc(19,20,23,30), enc(52,20,23,25), enc(43,20,23,25), enc(69,20,23,20)]),

    area("kanto_route5", "Route 5", "KANTO", "ROUTE",
         connections=["kanto_voltharrow", "kanto_bloomvale"],
         grass=[enc(19,20,23,30), enc(43,20,23,25), enc(52,20,23,25), enc(69,20,23,20)]),

    area("kanto_bloomvale", "Bloomvale Town", "KANTO", "TOWN",
         connections=["kanto_route5", "kanto_route6", "kanto_route7", "kanto_pokemon_tower"],
         healing=True, pc=True),

    area("kanto_route7", "Route 7", "KANTO", "ROUTE",
         connections=["kanto_bloomvale", "kanto_celadon_dept"],
         grass=[enc(52,22,26,30), enc(43,22,26,25), enc(23,22,26,25), enc(69,22,26,20)]),

    area("kanto_celadon_dept", "Celadon Department", "KANTO", "TOWN",
         connections=["kanto_route7", "kanto_route8", "kanto_route9"],
         healing=True, pc=True, gym_level=[26,32]),

    area("kanto_route8", "Route 8", "KANTO", "ROUTE",
         connections=["kanto_celadon_dept", "kanto_spirithaven"],
         grass=[enc(19,25,29,30), enc(52,25,29,25), enc(43,25,29,25), enc(69,25,29,20)]),

    area("kanto_route9", "Route 9", "KANTO", "ROUTE",
         connections=["kanto_celadon_dept", "kanto_route10"],
         grass=[enc(21,26,30,30), enc(50,26,30,25), enc(48,26,30,25), enc(88,26,30,20)]),

    area("kanto_spirithaven", "Spirithaven City", "KANTO", "TOWN",
         connections=["kanto_route8", "kanto_route10", "kanto_silph"],
         healing=True, pc=True, gym_level=[30,37]),

    area("kanto_route10", "Route 10", "KANTO", "ROUTE",
         connections=["kanto_route9", "kanto_spirithaven", "kanto_thunderpeak"],
         grass=[enc(100,28,33,25), enc(21,28,33,25), enc(50,28,33,25), enc(48,28,33,25)]),

    area("kanto_route11", "Route 11", "KANTO", "ROUTE",
         connections=["kanto_voltharrow", "kanto_psygate"],
         grass=[enc(27,28,32,30), enc(56,28,32,25), enc(128,28,32,20), enc(21,28,32,25)]),

    area("kanto_route12", "Route 12", "KANTO", "ROUTE",
         connections=["kanto_psygate", "kanto_route13"],
         grass=[enc(69,30,35,30), enc(43,30,35,25), enc(19,30,35,25), enc(21,30,35,20)],
         water=[enc(54,30,35,30), enc(116,30,35,30), enc(118,30,35,40)]),

    area("kanto_psygate", "Psygate City", "KANTO", "TOWN",
         connections=["kanto_route11", "kanto_route12"],
         healing=True, pc=True, gym_level=[36,42]),

    area("kanto_route13", "Route 13", "KANTO", "ROUTE",
         connections=["kanto_route12", "kanto_route14"],
         grass=[enc(27,34,38,25), enc(69,34,38,25), enc(19,34,38,25), enc(128,34,38,25)]),

    area("kanto_route14", "Route 14", "KANTO", "ROUTE",
         connections=["kanto_route13", "kanto_route15"],
         grass=[enc(27,35,40,25), enc(56,35,40,25), enc(128,35,40,25), enc(84,35,40,25)]),

    area("kanto_route15", "Route 15", "KANTO", "ROUTE",
         connections=["kanto_route14", "kanto_cindermark"],
         grass=[enc(128,36,41,30), enc(27,36,41,25), enc(56,36,41,25), enc(84,36,41,20)]),

    area("kanto_cindermark", "Cindermark City", "KANTO", "TOWN",
         connections=["kanto_route15", "kanto_route16", "kanto_cinderrift"],
         healing=True, pc=True, gym_level=[42,48]),

    area("kanto_route16", "Route 16", "KANTO", "ROUTE",
         connections=["kanto_cindermark", "kanto_route17"],
         grass=[enc(84,38,43,30), enc(128,38,43,25), enc(19,38,43,25), enc(114,38,43,20)]),

    area("kanto_route17", "Route 17", "KANTO", "ROUTE",
         connections=["kanto_route16", "kanto_route18"],
         grass=[enc(84,40,46,30), enc(128,40,46,25), enc(19,40,46,25), enc(114,40,46,20)]),

    area("kanto_route18", "Route 18", "KANTO", "ROUTE",
         connections=["kanto_route17", "kanto_groundveil"],
         grass=[enc(84,42,47,30), enc(128,42,47,30), enc(114,42,47,20), enc(131,42,47,20)]),

    area("kanto_groundveil", "Groundveil City", "KANTO", "TOWN",
         connections=["kanto_route18", "kanto_route19", "kanto_obsidian_cavern"],
         healing=True, pc=True, gym_level=[50,56]),

    area("kanto_route19", "Route 19", "KANTO", "ROUTE",
         connections=["kanto_groundveil", "kanto_route20"],
         water=[enc(54,40,48,30), enc(119,40,48,25), enc(116,40,48,25), enc(72,40,48,20)]),

    area("kanto_route20", "Route 20", "KANTO", "ROUTE",
         connections=["kanto_route19", "kanto_seafoam_islands", "kanto_route21"],
         water=[enc(54,42,50,30), enc(119,42,50,25), enc(116,42,50,25), enc(72,42,50,20)]),

    area("kanto_route21", "Route 21", "KANTO", "ROUTE",
         connections=["kanto_route20", "kanto_starttown"],
         water=[enc(54,44,52,30), enc(119,44,52,25), enc(120,44,52,25), enc(117,44,52,20)]),

    area("kanto_route22", "Route 22", "KANTO", "ROUTE",
         connections=["kanto_starttown", "kanto_stoneridge", "kanto_apex_citadel"],
         grass=[enc(19,12,16,30), enc(27,12,16,25), enc(56,12,16,25), enc(21,12,16,20)]),

    area("kanto_apex_citadel", "Apex Citadel", "KANTO", "TOWN",
         connections=["kanto_route22"],
         healing=True, pc=True,
         events=["event_kanto_elite4"]),

    area("kanto_frostspire_cave", "Frostspire Cave", "KANTO", "CAVE",
         connections=["kanto_groundveil"],
         cave=[enc(86,44,52,30), enc(50,44,52,25), enc(74,44,52,25), enc(87,44,52,20)]),

    area("kanto_thunderpeak", "Thunderpeak", "KANTO", "CAVE",
         connections=["kanto_route10"],
         cave=[enc(100,40,50,30), enc(41,40,50,25), enc(42,40,50,25), enc(101,44,54,20)]),

    area("kanto_cinderrift", "Cinderrift", "KANTO", "CAVE",
         connections=["kanto_cindermark"],
         cave=[enc(23,42,52,25), enc(88,42,52,25), enc(109,42,52,25), enc(50,42,52,25)]),

    area("kanto_obsidian_cavern", "Obsidian Cavern", "KANTO", "CAVE",
         connections=["kanto_groundveil"],
         cave=[enc(92,45,55,30), enc(93,48,58,25), enc(96,45,55,25), enc(95,45,55,20)]),

    area("kanto_myth_island_alpha", "Myth Island Alpha", "KANTO", "CAVE",
         connections=[],
         cave=[],
         events=["event_mew_encounter"]),

    area("kanto_seafoam_islands", "Seafoam Islands", "KANTO", "CAVE",
         connections=["kanto_route20"],
         cave=[enc(86,45,55,30), enc(87,50,60,30), enc(131,50,60,30), enc(131,50,60,10)],
         events=["event_windfrost_encounter"]),

    area("kanto_pokemon_tower", "Pokemon Tower", "KANTO", "CAVE",
         connections=["kanto_bloomvale"],
         cave=[enc(92,25,35,40), enc(93,28,38,30), enc(41,25,35,30)]),

    area("kanto_silph", "Silph Co.", "KANTO", "TOWN",
         connections=["kanto_spirithaven"],
         events=["event_silph_boss"]),

    area("kanto_mewtwo_cave", "Mewtwo Cave", "KANTO", "CAVE",
         connections=[],
         cave=[],
         events=["event_mewtwo_encounter"]),

    area("kanto_zapdos_peak", "Zapdos Peak", "KANTO", "CAVE",
         connections=[],
         cave=[],
         events=["event_zapdos_encounter"]),

    area("kanto_moltres_peak", "Moltres Peak", "KANTO", "CAVE",
         connections=[],
         cave=[],
         events=["event_moltres_encounter"]),

    area("kanto_aerodactyl_cavern", "Aerodactyl Cavern", "KANTO", "CAVE",
         connections=[],
         cave=[enc(142,50,60,20)],
         events=[]),
]

# ============================================================
# JOHTO AREAS
# ============================================================
johto_areas = [
    area("johto_newbark", "New Bark Town", "JOHTO", "TOWN",
         connections=["johto_route29"],
         healing=True, pc=True,
         events=["event_johto_intro"]),

    area("johto_route29", "Route 29", "JOHTO", "ROUTE",
         connections=["johto_newbark", "johto_cherrygrove"],
         grass=[enc(161,5,9,40), enc(163,4,8,30), enc(16,4,7,30)]),

    area("johto_cherrygrove", "Cherrygrove City", "JOHTO", "TOWN",
         connections=["johto_route29", "johto_route30"],
         healing=True, pc=True),

    area("johto_route30", "Route 30", "JOHTO", "ROUTE",
         connections=["johto_cherrygrove", "johto_route31"],
         grass=[enc(161,6,10,30), enc(167,5,9,25), enc(187,5,8,25), enc(10,5,8,20)]),

    area("johto_route31", "Route 31", "JOHTO", "ROUTE",
         connections=["johto_route30", "johto_violet"],
         grass=[enc(167,7,11,30), enc(163,6,10,25), enc(187,7,10,25), enc(41,6,9,20)]),

    area("johto_violet", "Violet City", "JOHTO", "TOWN",
         connections=["johto_route31", "johto_sprout_tower", "johto_route32"],
         healing=True, pc=True, gym_level=[10,14],
         events=["event_sprout_tower"]),

    area("johto_sprout_tower", "Sprout Tower", "JOHTO", "CAVE",
         connections=["johto_violet"],
         cave=[enc(92,8,13,40), enc(96,8,12,30), enc(201,10,13,30)]),

    area("johto_route32", "Route 32", "JOHTO", "ROUTE",
         connections=["johto_violet", "johto_ruins_of_alph", "johto_route33"],
         grass=[enc(60,10,13,30), enc(194,10,14,30), enc(222,12,15,20)],
         water=[enc(60,10,14,40), enc(194,11,15,30)]),

    area("johto_ruins_of_alph", "Ruins of Alph", "JOHTO", "CAVE",
         connections=["johto_route32"],
         cave=[enc(201,15,20,100)]),

    area("johto_route33", "Route 33", "JOHTO", "ROUTE",
         connections=["johto_route32", "johto_azalea"],
         grass=[enc(42,11,15,30), enc(167,11,14,25), enc(56,11,15,25), enc(216,13,16,20)]),

    area("johto_azalea", "Azalea Town", "JOHTO", "TOWN",
         connections=["johto_route33", "johto_slowpoke_well", "johto_route34"],
         healing=True, pc=True, gym_level=[18,22]),

    area("johto_slowpoke_well", "Slowpoke Well", "JOHTO", "CAVE",
         connections=["johto_azalea"],
         cave=[enc(79,15,20,40), enc(60,15,20,30), enc(170,15,20,30)]),

    area("johto_route34", "Route 34", "JOHTO", "ROUTE",
         connections=["johto_azalea", "johto_goldenrod"],
         grass=[enc(52,15,18,30), enc(190,15,19,25), enc(234,16,20,25), enc(185,17,21,20)]),

    area("johto_goldenrod", "Goldenrod City", "JOHTO", "TOWN",
         connections=["johto_route34", "johto_route35"],
         healing=True, pc=True, gym_level=[22,27]),

    area("johto_route35", "Route 35", "JOHTO", "ROUTE",
         connections=["johto_goldenrod", "johto_national_park", "johto_route36"],
         grass=[enc(193,18,22,30), enc(163,18,22,30), enc(16,18,22,20), enc(190,18,22,20)]),

    area("johto_route36", "Route 36", "JOHTO", "ROUTE",
         connections=["johto_route35", "johto_national_park"],
         grass=[enc(177,18,22,30), enc(234,18,22,30), enc(187,18,22,25), enc(43,18,22,15)]),

    area("johto_national_park", "National Park", "JOHTO", "ROUTE",
         connections=["johto_route35", "johto_route36", "johto_route37"],
         grass=[enc(46,18,25,30), enc(167,18,25,25), enc(193,18,25,25), enc(183,19,24,20)]),

    area("johto_route37", "Route 37", "JOHTO", "ROUTE",
         connections=["johto_national_park", "johto_ecruteak"],
         grass=[enc(228,23,28,30), enc(163,23,28,25), enc(198,23,28,25), enc(215,23,28,20)]),

    area("johto_ecruteak", "Ecruteak City", "JOHTO", "TOWN",
         connections=["johto_route37", "johto_tin_tower", "johto_route38", "johto_route39"],
         healing=True, pc=True, gym_level=[28,34],
         events=["event_burned_tower"]),

    area("johto_tin_tower", "Tin Tower", "JOHTO", "CAVE",
         connections=["johto_ecruteak"],
         cave=[enc(92,30,35,40), enc(93,30,35,30), enc(94,30,36,20)],
         events=["event_raikou_roam", "event_entei_roam", "event_suicune_encounter"]),

    area("johto_route38", "Route 38", "JOHTO", "ROUTE",
         connections=["johto_ecruteak", "johto_olivine"],
         grass=[enc(241,26,32,30), enc(220,25,30,25), enc(216,26,31,25), enc(179,25,30,20)]),

    area("johto_route39", "Route 39", "JOHTO", "ROUTE",
         connections=["johto_ecruteak", "johto_olivine"],
         grass=[enc(179,27,32,30), enc(241,27,32,25), enc(183,27,32,25), enc(234,27,32,20)]),

    area("johto_olivine", "Olivine City", "JOHTO", "TOWN",
         connections=["johto_route38", "johto_route39", "johto_route40"],
         healing=True, pc=True, gym_level=[32,38]),

    area("johto_route40", "Route 40", "JOHTO", "ROUTE",
         connections=["johto_olivine", "johto_cianwood"],
         water=[enc(72,28,34,40), enc(226,29,35,30), enc(117,28,34,30)]),

    area("johto_cianwood", "Cianwood City", "JOHTO", "TOWN",
         connections=["johto_route40", "johto_route41"],
         healing=True, pc=True, gym_level=[36,42]),

    area("johto_route41", "Route 41", "JOHTO", "ROUTE",
         connections=["johto_cianwood", "johto_olivine"],
         water=[enc(226,35,42,40), enc(72,34,41,30), enc(211,35,42,30)]),

    area("johto_route42", "Route 42", "JOHTO", "ROUTE",
         connections=["johto_ecruteak", "johto_mahogany", "johto_mt_mortar"],
         grass=[enc(56,34,40,30), enc(207,34,40,25), enc(220,35,41,25), enc(183,34,40,20)],
         water=[enc(60,34,40,40), enc(194,35,41,30)]),

    area("johto_mt_mortar", "Mt. Mortar", "JOHTO", "CAVE",
         connections=["johto_route42"],
         cave=[enc(50,35,42,30), enc(74,36,43,25), enc(66,36,43,25), enc(104,36,43,20)]),

    area("johto_route43", "Route 43", "JOHTO", "ROUTE",
         connections=["johto_mahogany", "johto_lake_rage"],
         grass=[enc(193,38,44,30), enc(228,38,44,25), enc(17,38,44,25), enc(235,38,44,20)]),

    area("johto_mahogany", "Mahogany Town", "JOHTO", "TOWN",
         connections=["johto_route42", "johto_route43", "johto_ice_path"],
         healing=True, pc=True, gym_level=[42,48]),

    area("johto_lake_rage", "Lake of Rage", "JOHTO", "ROUTE",
         connections=["johto_route43"],
         water=[enc(129,40,45,70), enc(130,40,45,30)]),

    area("johto_ice_path", "Ice Path", "JOHTO", "CAVE",
         connections=["johto_mahogany", "johto_blackthorn"],
         cave=[enc(220,40,48,40), enc(119,0,0,0), enc(86,40,48,30), enc(225,40,47,30)]),

    area("johto_blackthorn", "Blackthorn City", "JOHTO", "TOWN",
         connections=["johto_ice_path", "johto_dragon_den", "johto_victory_road", "johto_mt_silver"],
         healing=True, pc=True, gym_level=[50,56]),

    area("johto_dragon_den", "Dragon's Den", "JOHTO", "CAVE",
         connections=["johto_blackthorn"],
         cave=[enc(147,35,45,40), enc(148,45,55,30), enc(79,40,50,20)],
         water=[enc(147,35,45,50), enc(148,45,55,30), enc(130,45,55,20)]),

    area("johto_mt_silver", "Mt. Silver", "JOHTO", "CAVE",
         connections=["johto_blackthorn"],
         cave=[enc(56,50,60,20), enc(66,50,60,20), enc(111,52,62,20), enc(147,50,60,20), enc(104,52,62,20)]),

    area("johto_victory_road", "Victory Road", "JOHTO", "CAVE",
         connections=["johto_blackthorn"],
         cave=[enc(108,55,65,20), enc(74,55,65,20), enc(66,55,65,20), enc(111,55,65,20)]),

    area("johto_raikou_island", "Raikou Island", "JOHTO", "ROUTE",
         connections=[],
         events=["event_raikou_encounter"]),

    area("johto_entei_island", "Entei Island", "JOHTO", "ROUTE",
         connections=[],
         events=["event_entei_encounter"]),

    area("johto_suicune_spring", "Suicune Spring", "JOHTO", "ROUTE",
         connections=[],
         events=["event_suicune_battle"]),

    area("johto_lugia_cavern", "Lugia Cavern", "JOHTO", "CAVE",
         connections=[],
         cave=[],
         events=["event_lugia_encounter"]),

    area("johto_hooh_peak", "Ho-Oh Peak", "JOHTO", "ROUTE",
         connections=[],
         events=["event_hooh_encounter"]),

    area("johto_celebi_shrine", "Celebi Shrine", "JOHTO", "ROUTE",
         connections=[],
         events=["event_celebi_encounter"]),
]

# ============================================================
# HOENN AREAS
# ============================================================
hoenn_areas = [
    area("hoenn_littleroot", "Littleroot Town", "HOENN", "TOWN",
         connections=["hoenn_route101"],
         healing=True, pc=True,
         events=["event_hoenn_intro"]),

    area("hoenn_route101", "Route 101", "HOENN", "ROUTE",
         connections=["hoenn_littleroot", "hoenn_oldale"],
         grass=[enc(261,3,5,40), enc(265,3,5,30), enc(263,3,5,30)]),

    area("hoenn_oldale", "Oldale Town", "HOENN", "TOWN",
         connections=["hoenn_route101", "hoenn_route102", "hoenn_route103"],
         healing=True, pc=True),

    area("hoenn_route102", "Route 102", "HOENN", "ROUTE",
         connections=["hoenn_oldale", "hoenn_petalburg"],
         grass=[enc(261,4,7,30), enc(263,4,7,25), enc(265,4,7,25), enc(283,4,7,20)]),

    area("hoenn_route103", "Route 103", "HOENN", "ROUTE",
         connections=["hoenn_oldale", "hoenn_petalburg"],
         grass=[enc(263,4,7,30), enc(261,4,7,30), enc(276,4,7,40)]),

    area("hoenn_petalburg", "Petalburg City", "HOENN", "TOWN",
         connections=["hoenn_route102", "hoenn_route103", "hoenn_route104", "hoenn_petalburg_gym"],
         healing=True, pc=True),

    area("hoenn_route104", "Route 104", "HOENN", "ROUTE",
         connections=["hoenn_petalburg", "hoenn_petalburg_woods"],
         grass=[enc(278,7,10,30), enc(276,7,10,30), enc(263,7,10,25), enc(285,7,10,15)],
         water=[enc(278,7,10,50), enc(72,7,10,50)]),

    area("hoenn_petalburg_woods", "Petalburg Woods", "HOENN", "CAVE",
         connections=["hoenn_route104", "hoenn_rustboro"],
         cave=[enc(265,7,12,30), enc(285,7,12,30), enc(283,7,12,20), enc(193,7,12,20)]),

    area("hoenn_rustboro", "Rustboro City", "HOENN", "TOWN",
         connections=["hoenn_petalburg_woods", "hoenn_route116"],
         healing=True, pc=True, gym_level=[12,16]),

    area("hoenn_route116", "Route 116", "HOENN", "ROUTE",
         connections=["hoenn_rustboro", "hoenn_rusturf_tunnel"],
         grass=[enc(293,10,14,30), enc(276,10,14,25), enc(263,10,14,25), enc(304,10,14,20)]),

    area("hoenn_rusturf_tunnel", "Rusturf Tunnel", "HOENN", "CAVE",
         connections=["hoenn_route116", "hoenn_dewford"],
         cave=[enc(293,10,15,50), enc(304,12,15,50)]),

    area("hoenn_dewford", "Dewford Town", "HOENN", "TOWN",
         connections=["hoenn_rusturf_tunnel", "hoenn_granite_cave", "hoenn_route109"],
         healing=True, pc=True, gym_level=[18,22]),

    area("hoenn_granite_cave", "Granite Cave", "HOENN", "CAVE",
         connections=["hoenn_dewford"],
         cave=[enc(304,15,20,30), enc(296,15,20,30), enc(300,15,20,20), enc(374,15,20,20)]),

    area("hoenn_route109", "Route 109", "HOENN", "ROUTE",
         connections=["hoenn_dewford", "hoenn_slateport"],
         water=[enc(278,18,22,40), enc(72,18,22,30), enc(320,18,22,30)]),

    area("hoenn_slateport", "Slateport City", "HOENN", "TOWN",
         connections=["hoenn_route109", "hoenn_route110"],
         healing=True, pc=True),

    area("hoenn_route110", "Route 110", "HOENN", "ROUTE",
         connections=["hoenn_slateport", "hoenn_mauville"],
         grass=[enc(309,18,22,30), enc(313,18,22,25), enc(314,18,22,25), enc(263,18,22,20)]),

    area("hoenn_mauville", "Mauville City", "HOENN", "TOWN",
         connections=["hoenn_route110", "hoenn_route117", "hoenn_route118"],
         healing=True, pc=True, gym_level=[24,28]),

    area("hoenn_route117", "Route 117", "HOENN", "ROUTE",
         connections=["hoenn_mauville"],
         grass=[enc(300,20,25,30), enc(183,20,25,30), enc(327,20,25,25), enc(285,20,25,15)]),

    area("hoenn_route118", "Route 118", "HOENN", "ROUTE",
         connections=["hoenn_mauville", "hoenn_route119"],
         grass=[enc(335,22,27,30), enc(336,22,27,30), enc(352,22,27,20), enc(339,22,27,20)],
         water=[enc(339,22,27,40), enc(341,22,27,30), enc(349,22,27,30)]),

    area("hoenn_route119", "Route 119", "HOENN", "ROUTE",
         connections=["hoenn_route118", "hoenn_fortree"],
         grass=[enc(315,24,30,30), enc(285,24,30,25), enc(352,25,31,25), enc(357,25,31,20)]),

    area("hoenn_fortree", "Fortree City", "HOENN", "TOWN",
         connections=["hoenn_route119", "hoenn_route120"],
         healing=True, pc=True, gym_level=[30,36]),

    area("hoenn_route120", "Route 120", "HOENN", "ROUTE",
         connections=["hoenn_fortree", "hoenn_route121"],
         grass=[enc(359,28,34,25), enc(335,28,34,25), enc(336,28,34,25), enc(337,28,34,15), enc(338,28,34,10)]),

    area("hoenn_route121", "Route 121", "HOENN", "ROUTE",
         connections=["hoenn_route120", "hoenn_lilycove"],
         grass=[enc(288,30,36,30), enc(261,30,36,25), enc(352,30,36,25), enc(315,30,36,20)]),

    area("hoenn_lilycove", "Lilycove City", "HOENN", "TOWN",
         connections=["hoenn_route121", "hoenn_route123", "hoenn_route124"],
         healing=True, pc=True),

    area("hoenn_route123", "Route 123", "HOENN", "ROUTE",
         connections=["hoenn_lilycove", "hoenn_mossdeep"],
         grass=[enc(331,32,38,30), enc(359,32,38,25), enc(315,32,38,25), enc(352,32,38,20)]),

    area("hoenn_mossdeep", "Mossdeep City", "HOENN", "TOWN",
         connections=["hoenn_route123", "hoenn_route124", "hoenn_seafloor_cavern"],
         healing=True, pc=True, gym_level=[44,50]),

    area("hoenn_route124", "Route 124", "HOENN", "ROUTE",
         connections=["hoenn_lilycove", "hoenn_mossdeep"],
         water=[enc(320,35,42,40), enc(226,35,42,30), enc(369,38,45,30)]),

    area("hoenn_seafloor_cavern", "Seafloor Cavern", "HOENN", "CAVE",
         connections=["hoenn_mossdeep", "hoenn_sootopolis"],
         cave=[enc(339,40,50,30), enc(343,40,50,25), enc(316,40,50,25), enc(72,40,50,20)],
         water=[enc(341,40,50,40), enc(349,40,50,30), enc(363,40,50,30)]),

    area("hoenn_sootopolis", "Sootopolis City", "HOENN", "TOWN",
         connections=["hoenn_seafloor_cavern", "hoenn_route126"],
         healing=True, pc=True, gym_level=[52,58]),

    area("hoenn_route126", "Route 126", "HOENN", "ROUTE",
         connections=["hoenn_sootopolis", "hoenn_ever_grande"],
         water=[enc(369,45,55,30), enc(363,45,55,30), enc(320,45,55,30), enc(366,45,55,10)]),

    area("hoenn_ever_grande", "Ever Grande City", "HOENN", "TOWN",
         connections=["hoenn_route126", "hoenn_victory_road"],
         healing=True, pc=True),

    area("hoenn_victory_road", "Victory Road", "HOENN", "CAVE",
         connections=["hoenn_ever_grande", "hoenn_elite4_chamber"],
         cave=[enc(371,50,60,20), enc(304,50,60,20), enc(296,50,60,20), enc(74,50,60,20), enc(111,50,60,20)]),

    area("hoenn_petalburg_gym", "Petalburg Gym", "HOENN", "TOWN",
         connections=["hoenn_petalburg"],
         healing=True, gym_level=[38,44]),

    area("hoenn_elite4_chamber", "Elite Four Chamber", "HOENN", "TOWN",
         connections=["hoenn_victory_road"],
         healing=True, pc=True,
         events=["event_hoenn_elite4"]),

    area("hoenn_regirock_cave", "Regirock Cave", "HOENN", "CAVE",
         connections=[],
         cave=[],
         events=["event_regirock_encounter"]),

    area("hoenn_regice_cave", "Regice Cave", "HOENN", "CAVE",
         connections=[],
         cave=[],
         events=["event_regice_encounter"]),

    area("hoenn_registeel_cave", "Registeel Cave", "HOENN", "CAVE",
         connections=[],
         cave=[],
         events=["event_registeel_encounter"]),

    area("hoenn_latias_island", "Latias Island", "HOENN", "ROUTE",
         connections=[],
         events=["event_latias_encounter"]),

    area("hoenn_latios_island", "Latios Island", "HOENN", "ROUTE",
         connections=[],
         events=["event_latios_encounter"]),

    area("hoenn_kyogre_cavern", "Kyogre Cavern", "HOENN", "CAVE",
         connections=[],
         cave=[],
         events=["event_kyogre_encounter"]),

    area("hoenn_groudon_cavern", "Groudon Cavern", "HOENN", "CAVE",
         connections=[],
         cave=[],
         events=["event_groudon_encounter"]),

    area("hoenn_sky_pillar", "Sky Pillar", "HOENN", "CAVE",
         connections=[],
         cave=[],
         events=["event_rayquaza_encounter"]),

    area("hoenn_jirachi_crater", "Jirachi Crater", "HOENN", "ROUTE",
         connections=[],
         events=["event_jirachi_encounter"]),

    area("hoenn_deoxys_meteor", "Deoxys Meteor", "HOENN", "ROUTE",
         connections=[],
         events=["event_deoxys_encounter"]),
]

# ============================================================
# REGION DEFINITIONS
# ============================================================
johto_area_ids = [a["id"] for a in johto_areas]
hoenn_area_ids = [a["id"] for a in hoenn_areas]

# The kanto region already exists; kanto_areas.json includes the existing list
# plus the new extra areas
kanto_base_ids = [
    "kanto_starttown", "kanto_route1", "kanto_stoneridge", "kanto_route2",
    "kanto_viridian_forest", "kanto_route3", "kanto_mistfall", "kanto_mt_moon",
    "kanto_route4", "kanto_voltharrow", "kanto_route6", "kanto_route5",
    "kanto_bloomvale", "kanto_route7", "kanto_celadon_dept", "kanto_route8",
    "kanto_route9", "kanto_spirithaven", "kanto_route10", "kanto_route11",
    "kanto_route12", "kanto_psygate", "kanto_route13", "kanto_route14",
    "kanto_route15", "kanto_cindermark", "kanto_route16", "kanto_route17",
    "kanto_route18", "kanto_groundveil", "kanto_route19", "kanto_route20",
    "kanto_route21", "kanto_route22", "kanto_apex_citadel",
    "kanto_frostspire_cave", "kanto_thunderpeak", "kanto_cinderrift",
    "kanto_obsidian_cavern", "kanto_myth_island_alpha", "kanto_seafoam_islands",
    "kanto_pokemon_tower", "kanto_silph",
    # new extras
    "kanto_mewtwo_cave", "kanto_zapdos_peak", "kanto_moltres_peak",
    "kanto_aerodactyl_cavern"
]

johto_region = {
    "id": "JOHTO",
    "name": "Argento",
    "startAreaId": "johto_newbark",
    "areas": johto_area_ids,
    "gymIds": [f"gym_johto_{i}" for i in range(1, 9)],
    "eliteFourId": None
}

hoenn_region = {
    "id": "HOENN",
    "name": "Verdaine",
    "startAreaId": "hoenn_littleroot",
    "areas": hoenn_area_ids,
    "gymIds": [f"gym_hoenn_{i}" for i in range(1, 9)],
    "eliteFourId": "elite4_hoenn"
}

# ============================================================
# WRITE FILES
# ============================================================
def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
    print(f"Wrote {path}  ({len(data) if isinstance(data, list) else 1} item(s))")


write_json(os.path.join(REGIONS_DIR, "johto.json"), johto_region)
write_json(os.path.join(REGIONS_DIR, "hoenn.json"), hoenn_region)
write_json(os.path.join(AREAS_DIR, "kanto_areas.json"), kanto_areas)
write_json(os.path.join(AREAS_DIR, "johto_areas.json"), johto_areas)
write_json(os.path.join(AREAS_DIR, "hoenn_areas.json"), hoenn_areas)

print("\nDone. Summary:")
print(f"  johto region  : {len(johto_area_ids)} areas, {len(johto_region['gymIds'])} gyms")
print(f"  hoenn region  : {len(hoenn_area_ids)} areas, {len(hoenn_region['gymIds'])} gyms")
print(f"  kanto_areas   : {len(kanto_areas)} areas")
print(f"  johto_areas   : {len(johto_areas)} areas")
print(f"  hoenn_areas   : {len(hoenn_areas)} areas")
