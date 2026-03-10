#!/usr/bin/env python3
"""Generates kanto.json, johto.json, hoenn.json in content/creatures/."""
import json, os

BASE = "/home/user/Pokemon-game/app/src/main/assets/content/creatures"
os.makedirs(BASE, exist_ok=True)

# Helper: build a creature dict
def c(id, name, types, hp, atk, df, spa, spd, spe, catch, exp,
      learnset, evos=None, region="KANTO",
      starter=False, legendary=False,
      habitat=None, desc=""):
    return {
        "id": id, "name": name, "types": types,
        "baseStats": {"hp": hp, "attack": atk, "defense": df,
                      "specialAttack": spa, "specialDefense": spd, "speed": spe},
        "catchRate": catch, "baseExp": exp,
        "learnset": learnset,
        "evolutions": evos or [],
        "regionId": region,
        "isStarter": starter, "isLegendary": legendary,
        "habitat": habitat or [],
        "description": desc
    }

def lv(level, move_id): return {"level": level, "moveId": move_id}
def evo(to_id, method="LEVEL", param=0): return {"toId": to_id, "method": method, "param": param}
def item_evo(to_id, item_id): return {"toId": to_id, "method": "ITEM", "param": item_id}

# ──────────────────────────────────────────────────────────────────────────────
# KANTO  IDs 1 – 151
# Move ID reference (abbreviated):
#  NEUTRAL: 1=Tackle 2=Scratch 3=Pound 4=QuickAtk 5=HyperBeam 6=GigaImpact
#           7=Leer 8=Growl 9=TailWhip 10=Smoke 11=Screech 12=DefCurl 13=Harden
#           14=Agility 15=SwordsDance 16=NastyPlot 17=Recover 19=Protect
#           20=BodySlam 21=Stomp 22=Return 24=Swift 25=Slam 26=HyperVoice
#  FLAME:   31=Ember 32=Flamethrower 33=FireBlast 34=FlameCharge 35=FirePunch
#           36=BlazeKick 37=HeatWave 38=Eruption 39=WillOWisp 40=SunnyDay
#  AQUA:    51=WaterGun 52=Bubble 53=BubbleBeam 54=Surf 55=HydroPump
#           56=WaterPulse 57=Waterfall 58=AquaJet 59=Scald 60=RainDance 61=AquaTail
#  VOLT:    71=ThunderShock 72=Spark 73=Thunderbolt 74=Thunder 75=VoltTackle
#           76=Discharge 77=ThunderWave 78=ChargeBeam 79=WildCharge 80=Electroweb
#  FLORA:   91=VineWhip 92=RazorLeaf 93=Absorb 94=MegaDrain 95=GigaDrain
#           96=SolarBeam 97=PetalDance 98=EnergyBall 99=LeafBlade 100=WoodHammer
#           101=SeedBomb 102=LeechSeed 103=Synthesis 104=Spore 105=StunSpore
#           106=SleepPowder 107=PoisonPowder 108=Growth
#  FROST:   111=IceShard 112=IceBeam 113=Blizzard 114=PowderSnow 115=IcyWind
#           116=AuroraBeam 117=Avalanche 118=FreezeDry 119=Hail
#  BRAWL:   121=KarateChop 122=LowKick 123=BrickBreak 124=CloseCombat
#           125=MachPunch 126=Superpower 127=BulkUp 128=FocusEnergy 130=DrainPunch
#  VENOM:   131=PoisonSting 132=Acid 133=Sludge 134=SludgeBomb 135=PoisonJab
#           136=Toxic 137=CrossPoison
#  TERRA:   141=SandAtk 142=MudSlap 143=Earthquake 144=Dig 146=EarthPower
#           147=MudBomb 148=Sandstorm
#  AERO:    151=Gust 152=WingAtk 153=AerialAce 154=Hurricane 155=BraveBird
#           156=AirSlash 157=Roost 159=Fly 160=Tailwind
#  PSYCHE:  161=Confusion 162=Psybeam 163=Psychic 164=Psystrike 165=FutureSight
#           166=Extrasensory 167=CalmMind 168=Hypnosis 169=Amnesia 170=Barrier 172=Rest
#  INSECT:  181=BugBite 182=StringShot 183=SignalBeam 184=XScissor 185=BugBuzz
#           186=LeechLife 187=QuiverDance
#  STONE:   191=RockThrow 192=RockSlide 193=StoneEdge 194=RockBlast 196=PowerGem
#           197=RockPolish
#  SHADE:   201=Lick 202=ShadowBall 203=ShadowClaw 204=PhantomForce 205=NightShade
#           206=Hex 207=ConfuseRay 208=Curse
#  WYRM:    211=DragonRage 212=DragonBreath 213=DragonClaw 214=DragonPulse
#           215=Outrage 216=DracoMeteor 217=DragonDance
#  MURK:    221=Bite 222=Crunch 223=DarkPulse 224=SuckerPunch 225=NightSlash
#           226=KnockOff 227=DarkNastyPlot
#  IRON:    231=MetalClaw 232=IronTail 233=FlashCannon 234=IronHead 235=MeteorMash
#           236=BulletPunch 237=IronDefense
#  RADIANT: 241=Moonblast 242=DazzlingGleam 243=PlayRough 244=FairyWind
#           245=Moonlight 246=Charm

kanto = [
    # ── Starter line 1: FLORA / FLORA+VENOM ──────────────────────────────────
    c(1,"Verdling",["FLORA"],45,49,49,65,65,45, 45,64,
      [lv(1,8),lv(1,91),lv(7,102),lv(13,91),lv(20,107),lv(26,92),lv(32,95),lv(40,96)],
      [evo(2,param=16)], starter=True, habitat=["GRASSLAND"],
      desc="A small sprout creature energised by sunlight."),
    c(2,"Verdrake",["FLORA","VENOM"],60,62,63,80,80,60, 45,141,
      [lv(1,8),lv(1,91),lv(7,102),lv(16,91),lv(23,107),lv(30,92),lv(40,95),lv(50,96)],
      [evo(3,param=32)], habitat=["GRASSLAND"],
      desc="The flower bulb on its back has bloomed, releasing pollen."),
    c(3,"Verdrazor",["FLORA","VENOM"],80,82,83,100,100,80, 45,208,
      [lv(1,8),lv(1,91),lv(7,102),lv(16,92),lv(23,107),lv(32,95),lv(45,96),lv(55,134)],
      [], habitat=["GRASSLAND"],
      desc="Its massive flower sprays toxic spores that can blanket an entire field."),
    # ── Starter line 2: FLAME / FLAME+WYRM ───────────────────────────────────
    c(4,"Emberling",["FLAME"],39,52,43,60,50,65, 45,62,
      [lv(1,2),lv(1,10),lv(9,31),lv(15,34),lv(22,32),lv(30,14),lv(38,33),lv(46,5)],
      [evo(5,param=16)], starter=True, habitat=["MOUNTAIN"],
      desc="A lizard with a flame on its tail that grows with its emotions."),
    c(5,"Emberfox",["FLAME"],58,64,58,80,65,80, 45,142,
      [lv(1,2),lv(1,10),lv(9,31),lv(15,34),lv(24,32),lv(33,14),lv(42,33),lv(52,5)],
      [evo(6,param=36)], habitat=["MOUNTAIN"],
      desc="Its claws are aflame. It moves too fast to see clearly."),
    c(6,"Embersaur",["FLAME","WYRM"],78,84,78,109,85,100, 45,220,
      [lv(1,2),lv(1,10),lv(9,31),lv(15,34),lv(24,32),lv(33,14),lv(42,33),lv(52,154),lv(60,33),lv(70,216)],
      [], habitat=["MOUNTAIN"],
      desc="A magnificent dragon. Its fiery wings can melt boulders."),
    # ── Starter line 3: AQUA / AQUA+IRON ─────────────────────────────────────
    c(7,"Aqualing",["AQUA"],44,48,65,50,64,43, 45,63,
      [lv(1,9),lv(1,51),lv(7,52),lv(13,19),lv(20,53),lv(27,57),lv(33,54),lv(41,55)],
      [evo(8,param=16)], starter=True, habitat=["WATER"],
      desc="A turtle with a pale blue shell that repels most attacks."),
    c(8,"Aquartle",["AQUA"],59,63,80,65,80,58, 45,142,
      [lv(1,9),lv(1,51),lv(7,52),lv(16,53),lv(20,19),lv(27,57),lv(36,54),lv(46,55)],
      [evo(9,param=36)], habitat=["WATER"],
      desc="Its shell is reinforced with iron deposits, shrugging off cannon fire."),
    c(9,"Aquasteel",["AQUA","IRON"],79,83,100,85,105,78, 45,210,
      [lv(1,9),lv(1,51),lv(16,52),lv(20,53),lv(27,57),lv(36,54),lv(46,55),lv(56,232),lv(66,5)],
      [], habitat=["WATER"],
      desc="Its twin iron cannons can fire water at enormous pressure."),
    # ── Caterpillar lines ─────────────────────────────────────────────────────
    c(10,"Caterpite",["INSECT"],45,30,35,20,20,45, 255,39,
      [lv(1,181),lv(1,182)],[evo(11,param=7)],habitat=["FOREST"],
      desc="A soft-bodied larva that curls up when frightened."),
    c(11,"Cocoite",["INSECT"],50,25,55,25,25,30, 120,72,
      [lv(1,13)],[evo(12,param=10)],habitat=["FOREST"],
      desc="A green cocoon hanging from tree branches."),
    c(12,"Wingoth",["INSECT","PSYCHE"],60,45,50,90,80,70, 45,178,
      [lv(1,151),lv(1,181),lv(12,56),lv(15,162),lv(20,183),lv(30,163)],[],
      habitat=["FOREST"],desc="A graceful moth whose wing scales carry psychic dust."),
    c(13,"Wormbit",["INSECT","VENOM"],40,35,30,20,20,50, 255,39,
      [lv(1,131),lv(1,182)],[evo(14,param=7)],habitat=["FOREST"],
      desc="A small worm with a stinger. Its venom irritates skin."),
    c(14,"Kokubit",["INSECT","VENOM"],45,25,50,25,25,35, 120,72,
      [lv(1,13)],[evo(15,param=10)],habitat=["FOREST"],
      desc="A spiky pupa that stings anything that touches it."),
    c(15,"Venomoth",["INSECT","VENOM"],65,90,40,45,80,75, 45,175,
      [lv(1,131),lv(1,182),lv(12,135),lv(16,183),lv(22,184),lv(30,134),lv(40,185)],[],
      habitat=["FOREST"],desc="A wasp with oversized stingers dripping neurotoxin."),
    # ── Bird line ─────────────────────────────────────────────────────────────
    c(16,"Pidglet",["NEUTRAL","AERO"],40,45,40,35,35,56, 255,50,
      [lv(1,9),lv(5,141),lv(9,151),lv(14,153),lv(19,152),lv(27,160)],
      [evo(17,param=18)],habitat=["GRASSLAND"],desc="A tiny bird that flutters low over the grass."),
    c(17,"Pidgwin",["NEUTRAL","AERO"],63,60,55,50,50,71, 120,122,
      [lv(1,9),lv(5,141),lv(9,151),lv(14,153),lv(21,152),lv(31,160)],
      [evo(18,param=36)],habitat=["GRASSLAND","FOREST"],desc="Its wing muscles have grown powerful enough to create gust attacks."),
    c(18,"Pidgstorm",["NEUTRAL","AERO"],83,80,75,70,70,101, 45,196,
      [lv(1,9),lv(5,141),lv(9,151),lv(14,153),lv(21,152),lv(31,159),lv(44,154)],[],
      habitat=["GRASSLAND"],desc="A raptor whose wingspan generates tornado-force gusts."),
    # ── Rodent line ───────────────────────────────────────────────────────────
    c(19,"Slinkit",["NEUTRAL"],30,56,35,25,35,72, 255,51,
      [lv(1,9),lv(4,20),lv(7,4),lv(14,22)],[evo(20,param=20)],
      habitat=["GRASSLAND"],desc="A quick rodent that darts through underbrush."),
    c(20,"Slinkrat",["NEUTRAL"],55,81,60,50,70,97, 127,145,
      [lv(1,9),lv(4,20),lv(7,4),lv(14,22),lv(20,26),lv(30,6)],[],
      habitat=["GRASSLAND"],desc="Hardened fangs can gnaw through steel cables."),
    # ── Avian line 2 ──────────────────────────────────────────────────────────
    c(21,"Sporedge",["NEUTRAL","AERO"],40,60,30,31,31,70, 255,52,
      [lv(1,2),lv(7,8),lv(13,151),lv(20,153)],[evo(22,param=20)],
      habitat=["GRASSLAND"],desc="A small, aggressive bird known for its sharp beak."),
    c(22,"Sporedax",["NEUTRAL","AERO"],65,90,65,61,61,100, 90,155,
      [lv(1,2),lv(7,8),lv(13,151),lv(20,153),lv(27,156),lv(38,5)],[],
      habitat=["GRASSLAND"],desc="A fearless predator bird that dives at lightning speed."),
    # ── Serpent line ──────────────────────────────────────────────────────────
    c(23,"Acidslith",["VENOM"],35,60,44,40,54,55, 255,58,
      [lv(1,131),lv(7,207),lv(15,132),lv(20,136),lv(28,133)],[evo(24,param=22)],
      habitat=["GRASSLAND"],desc="A purple serpent that paralyses prey before swallowing it."),
    c(24,"Venslith",["VENOM"],60,85,69,65,79,80, 90,157,
      [lv(1,131),lv(7,207),lv(15,132),lv(20,136),lv(28,133),lv(38,134),lv(48,136)],[],
      habitat=["GRASSLAND"],desc="The cobra hood flares to intimidate enemies with a venomous glare."),
    # ── Electric rodent ───────────────────────────────────────────────────────
    c(25,"Voltpup",["VOLT"],35,55,30,50,40,90, 190,112,
      [lv(1,71),lv(5,9),lv(10,77),lv(18,72),lv(26,73),lv(34,75)],[evo(26,param=30)],
      habitat=["FOREST"],desc="Cheek sacs store electricity that crackles when excited."),
    c(26,"Volteon",["VOLT"],60,90,55,90,80,110, 75,218,
      [lv(1,71),lv(5,9),lv(10,77),lv(18,72),lv(26,73),lv(34,75),lv(44,74)],[],
      habitat=["FOREST"],desc="Bolts surge across its fur; it can light a town for a week."),
    # ── Sand-digger line ──────────────────────────────────────────────────────
    c(27,"Sandbur",["TERRA"],50,75,85,20,30,40, 255,74,
      [lv(1,141),lv(7,2),lv(14,142),lv(22,144),lv(30,143)],[evo(28,param=22)],
      habitat=["DESERT"],desc="Its rough skin is covered in tiny rock shards."),
    c(28,"Sandclaw",["TERRA"],75,100,110,45,55,65, 90,163,
      [lv(1,141),lv(7,2),lv(14,142),lv(22,144),lv(30,143),lv(40,146),lv(50,193)],[],
      habitat=["DESERT"],desc="Its claws can tunnel through rock as easily as soft earth."),
    # ── Bat line ──────────────────────────────────────────────────────────────
    c(29,"Nidbat",["VENOM"],55,47,52,40,40,41, 235,55,
      [lv(1,131),lv(8,107),lv(14,105),lv(20,135),lv(30,134)],[evo(30,param=22)],
      habitat=["CAVE"],desc="A small bat that feeds on toxic berries, concentrating venom."),
    c(30,"Venbat",["VENOM"],70,62,67,55,55,56, 120,135,
      [lv(1,131),lv(8,107),lv(14,105),lv(20,135),lv(30,134),lv(40,136),lv(50,5)],[],
      habitat=["CAVE"],desc="Venom drips from fangs long enough to pierce thick hides."),
    c(31,"Venqueen",["VENOM","TERRA"],81,92,77,85,75,76, 45,200,
      [lv(1,131),lv(8,107),lv(14,105),lv(20,135),lv(30,134),lv(40,143),lv(50,5)],[],
      habitat=["CAVE"],desc="Its drill-like horn can bore through cavern walls."),
    # ── Clefairy line ─────────────────────────────────────────────────────────
    c(35,"Starflit",["RADIANT"],70,45,48,60,65,35, 150,113,
      [lv(1,244),lv(3,244),lv(9,245),lv(14,246),lv(22,241),lv(30,163)],
      [item_evo(36,401)],habitat=["CAVE"],desc="Sings a gentle melody under starlight."),
    c(36,"Starglam",["RADIANT"],95,70,73,95,90,60, 25,225,
      [lv(1,244),lv(9,245),lv(14,246),lv(22,241),lv(30,163),lv(42,5)],[],
      habitat=["CAVE"],desc="Radiates a warm pink light that calms those nearby."),
    # ── Fox line ──────────────────────────────────────────────────────────────
    c(37,"Vulfox",["FLAME"],55,70,45,70,60,60, 190,90,
      [lv(1,31),lv(6,34),lv(14,35),lv(22,32),lv(30,16),lv(42,33)],[evo(38,param=30)],
      habitat=["GRASSLAND"],desc="A cunning fox that ignites its fur to intimidate enemies."),
    c(38,"Ninfox",["FLAME"],65,75,60,95,110,65, 75,177,
      [lv(1,31),lv(6,34),lv(14,35),lv(22,32),lv(30,16),lv(42,33),lv(55,38)],[],
      habitat=["GRASSLAND"],desc="Said to have nine fiery tails that cast hypnotic flames."),
    # ── Magnetite line ────────────────────────────────────────────────────────
    c(81,"Magnetite",["IRON","VOLT"],25,35,70,95,55,45, 190,89,
      [lv(1,71),lv(6,237),lv(13,77),lv(21,78),lv(30,76),lv(38,74)],[evo(82,param=30)],
      habitat=["CAVE"],desc="A strange magnetic creature that hovers using electric fields."),
    c(82,"Magnetite2",["IRON","VOLT"],50,60,95,120,70,70, 60,161,
      [lv(1,71),lv(6,237),lv(13,77),lv(21,78),lv(30,76),lv(38,74),lv(50,233)],[evo(83,param=60)],
      habitat=["CAVE"],desc="Three magnetite units orbit each other generating vast power."),
    c(83,"Magnezone",["IRON","VOLT"],70,70,115,130,90,60, 30,241,
      [lv(1,71),lv(6,237),lv(13,77),lv(21,78),lv(30,76),lv(38,74),lv(50,233),lv(60,5)],[],
      habitat=["CAVE"],desc="Has evolved to emit powerful electromagnetic pulses."),
    # ── Ghost line ────────────────────────────────────────────────────────────
    c(92,"Hauntling",["SHADE","VENOM"],30,35,30,100,35,80, 190,95,
      [lv(1,201),lv(5,207),lv(8,106),lv(15,206),lv(25,202),lv(35,104)],[evo(93,param=25)],
      habitat=["CAVE","TOWN"],desc="A drifting spirit found near old graveyards."),
    c(93,"Hauntmare",["SHADE","VENOM"],45,50,45,115,55,95, 90,172,
      [lv(1,201),lv(5,207),lv(8,106),lv(15,206),lv(25,202),lv(35,104),lv(45,202)],
      [evo(94,param=36)],habitat=["CAVE","TOWN"],desc="Stretches its shadowy arms to drag prey into darkness."),
    c(94,"Hauntmare2",["SHADE","VENOM"],60,65,60,130,75,110, 45,218,
      [lv(1,201),lv(5,207),lv(8,106),lv(15,206),lv(25,202),lv(35,104),lv(45,202),lv(55,204)],[],
      habitat=["CAVE"],desc="A phantom with enough power to rend the veil between worlds."),
    # ── Pseudo-legendary ──────────────────────────────────────────────────────
    c(147,"Draklet",["WYRM"],41,64,45,50,50,50, 45,60,
      [lv(1,211),lv(5,52),lv(15,53),lv(24,212),lv(35,213),lv(45,215)],[evo(148,param=30)],
      habitat=["WATER"],desc="A shy water dragon that hides beneath lily pads."),
    c(148,"Drakine",["WYRM"],61,84,65,70,70,70, 45,147,
      [lv(1,211),lv(5,52),lv(15,53),lv(24,212),lv(35,213),lv(45,215),lv(55,217)],[evo(149,param=55)],
      habitat=["WATER"],desc="Its scales shimmer like opals; it dances in river currents."),
    c(149,"Dragonite",["WYRM","AERO"],91,134,95,100,100,80, 45,270,
      [lv(1,211),lv(5,52),lv(15,53),lv(24,212),lv(35,213),lv(45,215),lv(55,217),lv(65,6)],[],
      habitat=["WATER"],desc="A majestic dragon that circles the globe in sixteen hours."),
    # ── Fossil creatures ──────────────────────────────────────────────────────
    c(138,"Shellfossil",["STONE","AQUA"],35,40,100,90,55,35, 45,99,
      [lv(1,191),lv(13,53),lv(22,146),lv(33,54),lv(44,193)],[evo(139,param=40)],
      habitat=["WATER"],desc="Revived from an ancient shell fossil."),
    c(139,"Shelltide",["STONE","AQUA"],70,60,125,115,70,55, 45,205,
      [lv(1,191),lv(13,53),lv(22,146),lv(33,54),lv(44,193),lv(54,55)],[],
      habitat=["WATER"],desc="Ancient oceanic predator restored from fossil DNA."),
    c(140,"Skullfossil",["STONE","AQUA"],30,80,90,55,45,55, 45,99,
      [lv(1,2),lv(13,57),lv(22,143),lv(33,192),lv(44,193)],[evo(141,param=40)],
      habitat=["WATER"],desc="Revived from a skull fossil. An ancient predator."),
    c(141,"Skullmare",["STONE","AQUA"],70,120,110,45,45,80, 45,205,
      [lv(1,2),lv(13,57),lv(22,143),lv(33,192),lv(44,193),lv(54,193)],[],
      habitat=["WATER"],desc="A resurrected marine monster with armour-plated jaws."),
    # ── Legendary birds ───────────────────────────────────────────────────────
    c(144,"Glacivern",["FROST","AERO"],90,85,100,95,125,85, 3,215,
      [lv(1,114),lv(1,151),lv(8,116),lv(15,117),lv(22,112),lv(29,119),lv(43,113),lv(50,154)],[],
      legendary=True, habitat=["CAVE","MOUNTAIN"],desc="The legendary ice bird said to control blizzards."),
    c(145,"Stormavian",["VOLT","AERO"],90,90,85,125,90,100, 3,215,
      [lv(1,71),lv(1,151),lv(8,77),lv(15,78),lv(22,73),lv(29,160),lv(43,74),lv(50,154)],[],
      legendary=True, habitat=["MOUNTAIN"],desc="The legendary thunder bird that calls lightning from storm clouds."),
    c(146,"Embravian",["FLAME","AERO"],90,100,90,125,85,90, 3,215,
      [lv(1,31),lv(1,151),lv(8,152),lv(15,37),lv(22,32),lv(29,40),lv(43,33),lv(50,154)],[],
      legendary=True, habitat=["MOUNTAIN"],desc="The legendary fire bird whose feathers glow like embers."),
    c(150,"Voidmind",["PSYCHE"],106,110,90,154,90,130, 3,220,
      [lv(1,161),lv(1,17),lv(9,167),lv(18,164),lv(27,163),lv(36,165),lv(63,5),lv(72,163)],[],
      legendary=True, habitat=["CAVE"],desc="An artificial psychic creature of immense and terrifying power."),
    c(151,"Miraphel",["PSYCHE"],100,100,100,100,100,100, 45,220,
      [lv(1,161),lv(1,17),lv(10,19),lv(20,163),lv(30,167),lv(40,165),lv(50,5)],[],
      legendary=True, habitat=["GRASSLAND"],desc="The mythical source creature. It can learn any technique."),
]

# Fill in the remaining Kanto creatures not yet defined (IDs missing from above).
# We define them here compactly.
kanto_extra = [
    # 32–34: Bat line
    c(32,"Zubatlet",["VENOM","AERO"],40,45,35,30,40,55,255,54,
      [lv(1,131),lv(5,182),lv(9,153),lv(16,207),lv(25,133),lv(34,156)],[evo(33,param=22)],habitat=["CAVE"],desc="A blind bat that navigates with ultrasonic screeches."),
    c(33,"Zubatfang",["VENOM","AERO"],75,80,70,65,75,90,90,147,
      [lv(1,131),lv(5,182),lv(9,153),lv(16,207),lv(25,133),lv(34,156),lv(44,134)],[evo(34,"HAPPINESS")],habitat=["CAVE"],desc="Uses echolocation to hunt at night; its venom is potent."),
    c(34,"Crobatfang",["VENOM","AERO"],85,90,80,70,80,130,45,204,
      [lv(1,131),lv(5,182),lv(9,153),lv(16,207),lv(25,133),lv(34,156),lv(44,134),lv(55,153)],[],habitat=["CAVE"],desc="Four wings let it fly silently at incredible speeds."),
    # 39-40: Balloon creature
    c(39,"Jiggloon",["RADIANT","NEUTRAL"],115,45,20,45,25,20,170,95,
      [lv(1,3),lv(4,8),lv(9,168),lv(14,244),lv(19,20),lv(29,241)],[item_evo(40,401)],
      habitat=["GRASSLAND"],desc="A round, buoyant creature that sings soothing lullabies."),
    c(40,"Wigglatoon",["RADIANT","NEUTRAL"],140,70,45,85,50,45,45,196,
      [lv(1,3),lv(4,8),lv(9,168),lv(14,244),lv(19,20),lv(29,241),lv(45,26)],[],
      habitat=["GRASSLAND"],desc="Inflates to enormous size and rolls over opponents."),
    # 41-42: Zubat fill (already defined above as 32-34). Let's do Grimer line.
    c(88,"Grimeslug",["VENOM"],80,80,50,40,50,25,190,90,
      [lv(1,131),lv(1,107),lv(7,133),lv(20,136),lv(28,134),lv(38,5)],[evo(89,param=38)],
      habitat=["TOWN"],desc="A mound of toxic sludge born from industrial waste."),
    c(89,"Grimemass",["VENOM"],105,105,75,65,100,50,75,184,
      [lv(1,131),lv(1,107),lv(7,133),lv(20,136),lv(28,134),lv(38,5),lv(55,5)],[],
      habitat=["TOWN"],desc="An enormous toxic sludge heap that corrodes everything it touches."),
    # Slowpoke line 79-80
    c(79,"Slowfin",["AQUA","PSYCHE"],90,65,65,40,40,15,190,99,
      [lv(1,51),lv(5,161),lv(14,56),lv(26,168),lv(37,162),lv(48,163)],[evo(80,param=37)],
      habitat=["WATER"],desc="So laid-back it doesn't notice pain until much later."),
    c(80,"Slowdon",["AQUA","PSYCHE"],95,75,110,100,80,30,75,164,
      [lv(1,51),lv(5,161),lv(14,56),lv(26,168),lv(37,162),lv(48,163),lv(55,165)],[],
      habitat=["WATER"],desc="Its thick skull protects a surprisingly powerful psychic mind."),
    # Growlithe line 58-59
    c(58,"Embarpup",["FLAME"],55,70,45,70,50,60,190,91,
      [lv(1,31),lv(7,8),lv(10,35),lv(18,34),lv(28,32),lv(36,40)],[item_evo(59,402)],
      habitat=["GRASSLAND"],desc="A loyal fire-type pup that guards its trainer fiercely."),
    c(59,"Emberleo",["FLAME"],90,110,80,100,80,95,75,194,
      [lv(1,31),lv(7,8),lv(10,35),lv(18,34),lv(28,32),lv(36,40),lv(50,33),lv(60,38)],[],
      habitat=["GRASSLAND"],desc="A majestic fire lion whose roar echoes across mountains."),
    # Ponyta line 77-78
    c(77,"Blazecolt",["FLAME"],50,85,55,65,65,90,190,82,
      [lv(1,31),lv(6,34),lv(9,152),lv(15,35),lv(24,32),lv(32,36),lv(48,33)],[evo(78,param=40)],
      habitat=["GRASSLAND"],desc="Its fiery mane and tail ignite when it gallops at full speed."),
    c(78,"Blazesteed",["FLAME"],65,100,70,80,80,105,75,175,
      [lv(1,31),lv(6,34),lv(9,152),lv(15,35),lv(24,32),lv(32,36),lv(48,33),lv(58,38)],[],
      habitat=["GRASSLAND"],desc="Gallops so fast its hooves barely touch the ground."),
    # Magmar line 126 and Electabuzz 125
    c(125,"Elecbolt",["VOLT"],65,83,57,95,85,105,45,156,
      [lv(1,71),lv(9,77),lv(17,72),lv(25,73),lv(33,76),lv(41,74),lv(49,75)],[],
      habitat=["GRASSLAND"],desc="Living dynamo; bolts arc between its horns."),
    c(126,"Magmar",["FLAME"],65,95,57,100,85,93,45,173,
      [lv(1,31),lv(9,39),lv(17,35),lv(25,37),lv(33,32),lv(41,33),lv(49,38)],[],
      habitat=["MOUNTAIN"],desc="Its body temperature can exceed 1000 degrees."),
    # Eevee and some evolutions
    c(133,"Eevee",["NEUTRAL"],55,55,50,45,65,55,45,92,
      [lv(1,1),lv(8,9),lv(15,4),lv(29,22)],
      [item_evo(134,403),item_evo(135,404),item_evo(136,402)],
      habitat=["TOWN"],desc="An adaptable creature whose genetic code is highly unstable."),
    c(134,"Aquareon",["AQUA"],130,65,60,110,95,65,45,184,
      [lv(1,51),lv(9,56),lv(17,54),lv(25,59),lv(33,55),lv(41,61)],[],
      habitat=["WATER"],desc="Absorbs water through its velvety hide to strengthen itself."),
    c(135,"Voltareon",["VOLT"],65,65,60,110,95,130,45,184,
      [lv(1,71),lv(9,77),lv(17,73),lv(25,76),lv(33,74),lv(41,75)],[],
      habitat=["GRASSLAND"],desc="Charges up static electricity in its bristly yellow fur."),
    c(136,"Flarareon",["FLAME"],65,130,60,95,110,65,45,184,
      [lv(1,31),lv(9,34),lv(17,32),lv(25,35),lv(33,33),lv(41,38)],[],
      habitat=["GRASSLAND"],desc="Body temperature is so high it can ignite air around it."),
    # Snorlax 143
    c(143,"Snorlith",["NEUTRAL"],160,110,65,65,110,30,25,189,
      [lv(1,1),lv(9,20),lv(17,21),lv(25,17),lv(33,5),lv(41,6)],[],
      habitat=["GRASSLAND"],desc="Consumes 400 kg of food daily then sleeps for a week."),
    # Tauros 128
    c(128,"Taurock",["NEUTRAL"],75,100,95,40,70,110,45,172,
      [lv(1,1),lv(7,9),lv(15,20),lv(25,21),lv(38,6)],[],
      habitat=["GRASSLAND"],desc="Charges with its three tails whipping to build speed."),
    # Kangaskhan 115
    c(115,"Kangasnap",["NEUTRAL"],105,95,80,40,80,90,45,172,
      [lv(1,1),lv(7,8),lv(15,20),lv(20,122),lv(30,21),lv(40,6)],[],
      habitat=["GRASSLAND"],desc="A mother creature that carries its young in a belly pouch."),
    # Scyther 123 → Scizor 212 (but 212 is in the move list... use creature id 123 only for kanto)
    c(123,"Scyther",["INSECT","AERO"],70,110,80,55,80,105,45,187,
      [lv(1,181),lv(9,153),lv(17,184),lv(25,185),lv(38,155)],[],
      habitat=["FOREST"],desc="Razor-sharp scythes on its forearms can split boulders."),
    # Jynx 124
    c(124,"Jynxara",["FROST","PSYCHE"],65,50,35,115,95,95,45,137,
      [lv(1,114),lv(8,168),lv(17,112),lv(25,161),lv(33,163),lv(45,113)],[],
      habitat=["CAVE"],desc="Its dancing movements mesmerise opponents into deep sleep."),
    # Pinsir 127
    c(127,"Pinsirax",["INSECT"],65,125,100,55,70,85,45,200,
      [lv(1,181),lv(9,184),lv(17,121),lv(25,193),lv(38,124)],[],
      habitat=["FOREST"],desc="Its crushing horns can snap tree trunks with one squeeze."),
    # Lapras 131
    c(131,"Laprasia",["AQUA","FROST"],130,85,80,85,95,60,45,187,
      [lv(1,51),lv(5,56),lv(12,115),lv(20,112),lv(28,54),lv(35,113),lv(44,55)],[],
      habitat=["WATER"],desc="A gentle sea giant that ferries people across oceans."),
    # Ditto 132
    c(132,"Mimicoze",["NEUTRAL"],48,48,48,48,48,48,35,101,
      [lv(1,1)],[],habitat=["GRASSLAND"],desc="Can reshape its entire body to copy any other creature."),
    # Mr. Mime 122
    c(122,"Mimiknight",["PSYCHE","RADIANT"],40,45,65,100,120,90,45,136,
      [lv(1,244),lv(9,170),lv(17,162),lv(25,163),lv(33,167),lv(45,165)],[],
      habitat=["TOWN"],desc="Mimes barriers so convincingly they become real."),
    # Tangela 114
    c(114,"Tanglevine",["FLORA"],65,55,115,100,40,60,45,166,
      [lv(1,91),lv(7,102),lv(15,94),lv(25,96),lv(33,95),lv(45,97)],[],
      habitat=["GRASSLAND"],desc="Masses of tangling blue vines that regenerate instantly."),
    # Electabuzz line (only 125 above, fill basic entries for 100s range)
    # Voltorb line 100-101
    c(100,"Voltorb",["VOLT"],40,30,50,55,55,100,190,66,
      [lv(1,71),lv(5,80),lv(14,77),lv(20,76),lv(30,74)],[evo(101,param=30)],
      habitat=["GRASSLAND"],desc="Round electric orb that shocks careless trainers."),
    c(101,"Electrob",["VOLT"],60,50,70,80,80,140,60,150,
      [lv(1,71),lv(5,80),lv(14,77),lv(20,76),lv(30,74),lv(40,74)],[],
      habitat=["GRASSLAND"],desc="Its charge is so powerful it can fuse metal."),
    # Exeggcute line 102-103
    c(102,"Eggcluster",["FLORA","PSYCHE"],60,40,80,60,45,40,90,98,
      [lv(1,91),lv(7,161),lv(14,103),lv(22,96),lv(30,163)],[item_evo(103,405)],
      habitat=["FOREST"],desc="Six psychically linked eggs that roll and dodge together."),
    c(103,"Eggpalm",["FLORA","PSYCHE"],95,95,85,125,75,55,45,182,
      [lv(1,91),lv(7,161),lv(14,103),lv(22,96),lv(30,163),lv(45,97),lv(55,5)],[],
      habitat=["FOREST"],desc="A walking coconut palm with immense psychic power."),
    # Cubone line 104-105
    c(104,"Cubskull",["TERRA"],50,50,95,40,50,35,190,74,
      [lv(1,1),lv(6,141),lv(12,191),lv(20,143),lv(28,192),lv(38,193)],[evo(105,param=28)],
      habitat=["CAVE"],desc="Always wears the skull of its deceased mother."),
    c(105,"Marowitch",["TERRA"],60,80,110,50,80,45,75,149,
      [lv(1,1),lv(6,141),lv(12,191),lv(20,143),lv(28,192),lv(38,193),lv(48,146)],[],
      habitat=["CAVE"],desc="Dances under moonlight, mourning with its bone club raised."),
    # Rhydon line 111-112
    c(111,"Rhinolet",["TERRA","STONE"],80,85,95,30,30,25,120,135,
      [lv(1,141),lv(10,191),lv(20,143),lv(30,192),lv(42,193),lv(52,146)],[evo(112,param=42)],
      habitat=["GRASSLAND"],desc="A rough-skinned rhino that uses its horn as a drill."),
    c(112,"Rhinodon",["TERRA","STONE"],105,130,120,45,45,40,60,204,
      [lv(1,141),lv(10,191),lv(20,143),lv(30,192),lv(42,193),lv(52,146),lv(62,6)],[],
      habitat=["GRASSLAND"],desc="Its drill horn can pierce a skyscraper foundation."),
    # Chansey 113
    c(113,"Chansheal",["NEUTRAL"],250,5,5,35,105,50,30,255,
      [lv(1,3),lv(9,8),lv(17,17),lv(25,29),lv(33,246),lv(45,20)],[],
      habitat=["GRASSLAND"],desc="Kind-hearted healer that shares its life-giving egg freely."),
    # Horsea line 116-117
    c(116,"Seahorse",["AQUA"],30,40,70,70,25,60,225,83,
      [lv(1,51),lv(8,53),lv(15,213),lv(25,55),lv(32,212)],[evo(117,param=32)],
      habitat=["WATER"],desc="Anchors itself with its curled tail during ocean currents."),
    c(117,"Kingdorse",["AQUA","WYRM"],55,95,95,95,45,85,75,170,
      [lv(1,51),lv(8,53),lv(15,213),lv(25,55),lv(32,212),lv(45,216)],[],
      habitat=["WATER"],desc="Majestic sea-horse ruler. Steers with its fan-like dorsal fin."),
    # Goldeen line 118-119
    c(118,"Goldeenlet",["AQUA"],45,67,60,35,50,63,225,111,
      [lv(1,51),lv(10,57),lv(19,54),lv(28,55),lv(38,61)],[evo(119,param=33)],
      habitat=["WATER"],desc="A graceful fish with a horn like a lance."),
    c(119,"Goldash",["AQUA"],80,92,65,65,80,68,60,214,
      [lv(1,51),lv(10,57),lv(19,54),lv(28,55),lv(38,61),lv(48,5)],[],
      habitat=["WATER"],desc="Commands schools of fish with majestic golden scales."),
    # Staryu/Starmie 120-121
    c(120,"Staryte",["AQUA"],30,45,55,70,55,85,225,106,
      [lv(1,51),lv(7,56),lv(17,161),lv(27,162),lv(37,163)],[item_evo(121,405)],
      habitat=["WATER"],desc="A star-shaped sea creature that regenerates lost limbs."),
    c(121,"Starblaze",["AQUA","PSYCHE"],60,75,85,100,85,115,60,207,
      [lv(1,51),lv(7,56),lv(17,161),lv(27,162),lv(37,163),lv(47,165)],[],
      habitat=["WATER"],desc="Its gem glows with otherworldly light linked to distant stars."),
]

kanto_all = kanto + kanto_extra

# Build a complete set by filling gaps (IDs not defined get a minimal placeholder)
kanto_ids = {cre["id"] for cre in kanto_all}
for missing_id in range(1, 152):
    if missing_id not in kanto_ids:
        # Generate a placeholder entry so nothing breaks
        kanto_all.append(c(missing_id, f"Kanto{missing_id:03d}", ["NEUTRAL"],
          50,65,65,50,50,65, 100, 100,
          [lv(1,1),lv(10,7),lv(20,20),lv(30,22)], [], "KANTO",
          False, False, ["GRASSLAND"], f"Creature #{missing_id}."))

kanto_all.sort(key=lambda x: x["id"])

# ──────────────────────────────────────────────────────────────────────────────
# JOHTO  IDs 152 – 251
# ──────────────────────────────────────────────────────────────────────────────
johto = [
    # Starters
    c(152,"Sproutkit",["FLORA"],45,49,65,49,65,45,45,64,
      [lv(1,8),lv(7,91),lv(14,103),lv(20,92),lv(27,108),lv(34,98),lv(44,95),lv(54,96)],
      [evo(153,param=18)],region="JOHTO",starter=True,habitat=["GRASSLAND"],
      desc="A sprout dinosaur that absorbs sunlight through the leaf on its head."),
    c(153,"Sproutwig",["FLORA"],60,62,80,63,80,60,45,141,
      [lv(1,8),lv(7,91),lv(14,103),lv(20,92),lv(27,108),lv(34,98),lv(44,95),lv(54,96)],
      [evo(154,param=32)],region="JOHTO",habitat=["GRASSLAND"],
      desc="A larger leaf dino that swings its thick neck defensively."),
    c(154,"Sproutking",["FLORA"],80,82,100,83,100,80,45,208,
      [lv(1,8),lv(7,91),lv(14,103),lv(20,92),lv(27,108),lv(34,99),lv(44,95),lv(54,100)],
      [],region="JOHTO",habitat=["GRASSLAND"],
      desc="A massive stegosaurus-like creature whose petals release healing pollen."),
    c(155,"Embercub",["FLAME"],39,52,43,60,50,65,45,62,
      [lv(1,2),lv(6,10),lv(10,31),lv(16,34),lv(23,32),lv(31,40),lv(38,33),lv(50,5)],
      [evo(156,param=14)],region="JOHTO",starter=True,habitat=["MOUNTAIN"],
      desc="A spiky-backed cub that ignites the quills on its back when threatened."),
    c(156,"Emberbadge",["FLAME"],58,64,58,80,65,80,45,142,
      [lv(1,2),lv(6,10),lv(10,31),lv(16,34),lv(23,32),lv(31,40),lv(38,33),lv(50,5)],
      [evo(157,param=36)],region="JOHTO",habitat=["MOUNTAIN"],
      desc="Its volcanic back blazes with raw heat; footsteps leave scorch marks."),
    c(157,"Emberblazer",["FLAME"],78,84,78,109,85,100,45,220,
      [lv(1,2),lv(6,10),lv(10,31),lv(16,34),lv(23,32),lv(31,40),lv(38,33),lv(50,38),lv(60,5)],
      [],region="JOHTO",habitat=["MOUNTAIN"],
      desc="A fiery volcano badger. Its back erupts with explosive force."),
    c(158,"Tidekit",["AQUA"],50,65,64,44,48,43,45,63,
      [lv(1,9),lv(6,51),lv(10,52),lv(18,57),lv(27,54),lv(35,55),lv(45,61)],
      [evo(159,param=18)],region="JOHTO",starter=True,habitat=["WATER"],
      desc="A blue crocodilian that chomps anything within reach."),
    c(159,"Tidesnap",["AQUA"],65,80,80,59,63,58,45,142,
      [lv(1,9),lv(6,51),lv(10,52),lv(18,57),lv(27,54),lv(35,55),lv(45,61),lv(55,5)],
      [evo(160,param=30)],region="JOHTO",habitat=["WATER"],
      desc="Its jaws are powerful enough to crush boulders with ease."),
    c(160,"Tidecroc",["AQUA"],79,105,100,79,83,78,45,218,
      [lv(1,9),lv(6,51),lv(10,52),lv(18,57),lv(27,54),lv(35,55),lv(45,61),lv(55,57),lv(65,5)],
      [],region="JOHTO",habitat=["WATER"],
      desc="A titanic crocodile whose hydro-cannon tail can shatter ships."),
    # ── Sentret line 161-162 ──────────────────────────────────────────────────
    c(161,"Sentret",["NEUTRAL"],35,46,34,35,45,20,255,57,
      [lv(1,1),lv(5,9),lv(9,4),lv(15,22)],[evo(162,param=15)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Stands on its tail to scan for danger."),
    c(162,"Furret",["NEUTRAL"],85,76,64,45,55,90,90,145,
      [lv(1,1),lv(5,9),lv(9,4),lv(15,22),lv(25,25),lv(35,20)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="Sleek body lets it slip through tight tunnels to hunt prey."),
    # ── Owl line 163-164 ─────────────────────────────────────────────────────
    c(163,"Hoolet",["NEUTRAL","AERO"],60,30,30,36,56,50,255,52,
      [lv(1,3),lv(5,168),lv(9,151),lv(15,163)],[evo(164,param=20)],region="JOHTO",
      habitat=["FOREST"],desc="A round owl that hypnotises prey with huge spinning eyes."),
    c(164,"Hootdream",["NEUTRAL","AERO"],100,50,50,76,96,70,90,158,
      [lv(1,3),lv(5,168),lv(9,151),lv(15,163),lv(25,162),lv(35,165)],[],region="JOHTO",
      habitat=["FOREST"],desc="Psychic owl whose hypnotic eyes can entrance any foe."),
    # ── Ledyba line 165-166 ───────────────────────────────────────────────────
    c(165,"Ledybug",["INSECT","AERO"],40,20,30,40,80,55,255,54,
      [lv(1,181),lv(6,125),lv(11,183),lv(18,185),lv(28,187)],[evo(166,param=18)],region="JOHTO",
      habitat=["GRASSLAND"],desc="A ladybird that uses team coordination to fight."),
    c(166,"Ledystar",["INSECT","AERO"],55,35,50,55,110,85,90,137,
      [lv(1,181),lv(6,125),lv(11,183),lv(18,185),lv(28,187),lv(40,184)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="A star-patterned beetle renowned for its keen senses."),
    # ── Spider line 167-168 ───────────────────────────────────────────────────
    c(167,"Weblet",["INSECT","VENOM"],40,60,40,40,40,30,255,57,
      [lv(1,131),lv(5,182),lv(12,181),lv(22,135),lv(32,184)],[evo(168,param=22)],region="JOHTO",
      habitat=["FOREST"],desc="Spins sticky webs between trees to trap flying insects."),
    c(168,"Arachfang",["INSECT","VENOM"],70,90,70,60,60,40,90,140,
      [lv(1,131),lv(5,182),lv(12,181),lv(22,135),lv(32,184),lv(42,185)],[],region="JOHTO",
      habitat=["FOREST"],desc="A monstrous spider. Its web can hold creatures ten times its size."),
    # ── Chinchou line 170-171 ─────────────────────────────────────────────────
    c(170,"Chinchourl",["AQUA","VOLT"],75,38,38,56,56,67,190,90,
      [lv(1,51),lv(6,71),lv(12,53),lv(22,77),lv(32,73),lv(43,55)],[evo(171,param=27)],region="JOHTO",
      habitat=["WATER"],desc="Two glowing antennae lure prey in deep dark water."),
    c(171,"Lanturnfin",["AQUA","VOLT"],125,58,58,76,76,67,75,161,
      [lv(1,51),lv(6,71),lv(12,53),lv(22,77),lv(32,73),lv(43,55),lv(55,74)],[],region="JOHTO",
      habitat=["WATER"],desc="Its lantern generates enough electricity to stun large fish."),
    # ── Cleffa/Igglybuff (baby forms) 173-174 ────────────────────────────────
    c(173,"Starbaby",["RADIANT"],50,25,28,45,55,15,150,37,
      [lv(1,244),lv(5,246),lv(9,245)],[evo(35,"HAPPINESS")],region="JOHTO",
      habitat=["GRASSLAND"],desc="A tiny star creature that loves being held."),
    c(174,"Bubbloon",["RADIANT","NEUTRAL"],90,30,15,40,20,15,170,38,
      [lv(1,3),lv(4,8),lv(9,168)],[evo(39,"HAPPINESS")],region="JOHTO",
      habitat=["GRASSLAND"],desc="A tiny bubbly baby that sings off-key lullabies."),
    # ── Mareep line 179-181 (Ampharos not to conflict) ───────────────────────
    c(179,"Fluffolt",["VOLT"],55,40,40,65,45,35,235,78,
      [lv(1,71),lv(6,105),lv(15,77),lv(23,73),lv(30,76),lv(42,74)],[evo(180,param=15)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Static builds in its wool, making its fleece glow."),
    c(180,"Flaireep",["VOLT"],70,55,55,80,60,45,120,161,
      [lv(1,71),lv(6,105),lv(15,77),lv(23,73),lv(30,76),lv(42,74)],[evo(181,param=30)],region="JOHTO",
      habitat=["GRASSLAND"],desc="The red sphere on its tail acts as a lightning rod."),
    c(181,"Flailhorn",["VOLT"],90,75,75,115,90,55,45,230,
      [lv(1,71),lv(6,105),lv(15,77),lv(23,73),lv(30,76),lv(42,74),lv(55,5)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="The beacon on its tail glows so brightly ships mistake it for a lighthouse."),
    # ── Hoppip line 187-189 ───────────────────────────────────────────────────
    c(187,"Driftleaf",["FLORA","AERO"],35,35,40,35,55,50,255,74,
      [lv(1,91),lv(7,105),lv(15,102),lv(22,103),lv(30,96)],[evo(188,param=18)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Floats on the wind, spreading seeds across wide distances."),
    c(188,"Driftfern",["FLORA","AERO"],55,45,50,45,65,80,120,119,
      [lv(1,91),lv(7,105),lv(15,102),lv(22,103),lv(30,96),lv(40,97)],[evo(189,param=27)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Leaps between updrafts with its broadened leaf pads."),
    c(189,"Driftbloom",["FLORA","AERO"],75,55,70,55,80,110,45,180,
      [lv(1,91),lv(7,105),lv(15,102),lv(22,103),lv(30,96),lv(40,97),lv(52,154)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="The enormous flower on its head controls nearby air currents."),
    # ── Sunkern/Sunflora 191-192 ──────────────────────────────────────────────
    c(191,"Sunsprite",["FLORA"],30,30,30,30,30,30,235,36,
      [lv(1,93),lv(7,108),lv(14,103),lv(22,98)],[item_evo(192,405)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Falls from the sky during meteor showers."),
    c(192,"Sunbloom",["FLORA"],75,75,55,105,85,30,120,149,
      [lv(1,93),lv(7,108),lv(14,103),lv(22,98),lv(35,95),lv(48,96)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="Turns to face the sun constantly; withers in the dark."),
    # ── Yanma 193 ─────────────────────────────────────────────────────────────
    c(193,"Dragonfly",["INSECT","AERO"],65,65,45,75,45,95,75,147,
      [lv(1,181),lv(7,153),lv(14,183),lv(24,185),lv(36,185)],[],region="JOHTO",
      habitat=["WATER"],desc="Creates sonic booms with its wingbeats that shatter glass."),
    # ── Wooper line 194-195 ───────────────────────────────────────────────────
    c(194,"Woopert",["AQUA","TERRA"],55,45,45,25,25,15,255,71,
      [lv(1,51),lv(6,141),lv(12,147),lv(20,144),lv(28,143)],[evo(195,param=20)],region="JOHTO",
      habitat=["WATER"],desc="Wanders on land in cool weather; its skin secretes goop."),
    c(195,"Quagswamp",["AQUA","TERRA"],95,85,85,65,65,35,90,175,
      [lv(1,51),lv(6,141),lv(12,147),lv(20,144),lv(28,143),lv(40,146)],[],region="JOHTO",
      habitat=["WATER"],desc="Wallows in mud. Its body absorbs shock from any blow."),
    # ── Misdreavus 200 ────────────────────────────────────────────────────────
    c(200,"Mistwail",["SHADE"],60,60,60,85,85,85,45,147,
      [lv(1,201),lv(5,207),lv(15,206),lv(25,202),lv(35,204),lv(45,205)],[],region="JOHTO",
      habitat=["CAVE"],desc="A screaming spirit that feeds on fear and sadness."),
    # ── Unown 201 ─────────────────────────────────────────────────────────────
    c(201,"Inscribeon",["PSYCHE"],48,72,48,72,48,48,225,90,
      [lv(1,164)],[],region="JOHTO",
      habitat=["CAVE"],desc="A living ancient letter. Its power is magnified in groups."),
    # ── Wobbuffet 202 ─────────────────────────────────────────────────────────
    c(202,"Blobounce",["PSYCHE"],190,33,58,33,58,33,45,142,
      [lv(1,19),lv(1,169),lv(1,170)],[],region="JOHTO",
      habitat=["CAVE"],desc="Never attacks; waits patiently then returns damage doubled."),
    # ── Girafarig 203 ─────────────────────────────────────────────────────────
    c(203,"Twindeer",["NEUTRAL","PSYCHE"],70,80,65,90,65,85,60,144,
      [lv(1,1),lv(9,161),lv(17,20),lv(25,163),lv(33,166),lv(45,165)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="Its tail has a second face that bites anything sneaking up behind."),
    # ── Pineco 204-205 ────────────────────────────────────────────────────────
    c(204,"Pinacone",["INSECT"],50,65,90,35,35,15,190,74,
      [lv(1,181),lv(7,13),lv(13,184),lv(20,192),lv(28,193)],[evo(205,param=31)],region="JOHTO",
      habitat=["FOREST"],desc="Hangs motionless from branches, then explodes when touched."),
    c(205,"Fortrettle",["INSECT","STONE"],75,90,140,60,60,40,45,170,
      [lv(1,181),lv(7,13),lv(13,184),lv(20,192),lv(28,193),lv(40,196)],[],region="JOHTO",
      habitat=["FOREST"],desc="An armoured fortress bug whose protective shell is nearly indestructible."),
    # ── Dunsparce 206 ─────────────────────────────────────────────────────────
    c(206,"Coilsnout",["NEUTRAL"],100,70,70,65,65,45,190,125,
      [lv(1,1),lv(9,20),lv(17,17),lv(25,168),lv(33,212),lv(45,146)],[],region="JOHTO",
      habitat=["CAVE"],desc="A serpentine creature that drills into the earth to hide."),
    # ── Gligar 207 ────────────────────────────────────────────────────────────
    c(207,"Scorpwing",["TERRA","AERO"],65,75,105,35,65,85,60,147,
      [lv(1,2),lv(9,131),lv(17,153),lv(25,141),lv(33,143)],[],region="JOHTO",
      habitat=["MOUNTAIN"],desc="Glides silently and stings prey with its barbed tail."),
    # ── Steelix 208 (evolved Onix via trade item) ─────────────────────────────
    c(208,"Steelonyx",["IRON","TERRA"],75,85,200,55,65,30,25,179,
      [lv(1,141),lv(9,191),lv(20,143),lv(30,232),lv(40,234),lv(50,237)],[],region="JOHTO",
      habitat=["CAVE"],desc="Tempered by the earth's inner heat into impenetrable steel segments."),
    # ── Snubbull 209-210 ──────────────────────────────────────────────────────
    c(209,"Snubbull",["RADIANT"],60,80,50,40,40,30,190,84,
      [lv(1,221),lv(8,246),lv(15,222),lv(22,243),lv(32,241)],[evo(210,param=23)],region="JOHTO",
      habitat=["TOWN"],desc="Looks intimidating but is actually very affectionate."),
    c(210,"Granbull",["RADIANT"],90,120,75,60,60,45,75,177,
      [lv(1,221),lv(8,246),lv(15,222),lv(22,243),lv(32,241),lv(45,6)],[],region="JOHTO",
      habitat=["TOWN"],desc="Powerful jaws can crush stone; hearts as soft as its cheeks."),
    # ── Qwilfish 211 ──────────────────────────────────────────────────────────
    c(211,"Quilfish",["AQUA","VENOM"],65,95,85,55,55,85,45,86,
      [lv(1,51),lv(8,131),lv(14,107),lv(22,135),lv(30,134),lv(40,55)],[],region="JOHTO",
      habitat=["WATER"],desc="Inflates like a balloon and launches sharp toxic spines."),
    # ── Scizor 212 (evolves from Kanto Scyther) ───────────────────────────────
    c(212,"Scizorex",["INSECT","IRON"],70,130,100,55,80,65,25,187,
      [lv(1,181),lv(9,231),lv(17,184),lv(25,185),lv(38,234)],[],region="JOHTO",
      habitat=["FOREST"],desc="Its steel pincers move at bullet speed, leaving opponents no chance."),
    # ── Shuckle 213 ───────────────────────────────────────────────────────────
    c(213,"Shrootle",["INSECT","STONE"],20,10,230,10,230,5,190,177,
      [lv(1,181),lv(8,13),lv(16,192),lv(24,196)],[],region="JOHTO",
      habitat=["MOUNTAIN"],desc="Hides completely inside its rock-hard shell, fermenting berries within."),
    # ── Heracross 214 ─────────────────────────────────────────────────────────
    c(214,"Heracles",["INSECT","BRAWL"],80,125,75,40,95,85,45,200,
      [lv(1,181),lv(9,121),lv(17,122),lv(25,123),lv(33,124),lv(45,126)],[],region="JOHTO",
      habitat=["FOREST"],desc="A beetle warrior who flings opponents over mountaintops."),
    # ── Sneasel 215 ───────────────────────────────────────────────────────────
    c(215,"Sneaselt",["MURK","FROST"],55,95,55,35,75,115,60,86,
      [lv(1,221),lv(7,225),lv(14,111),lv(22,112),lv(30,224),lv(42,115)],[],region="JOHTO",
      habitat=["CAVE"],desc="A cunning thief of the ice tundra; razor claws steal eggs."),
    # ── Teddiursa/Ursaring 216-217 ────────────────────────────────────────────
    c(216,"Honeyursa",["NEUTRAL"],60,80,50,50,50,40,120,111,
      [lv(1,1),lv(9,8),lv(17,93),lv(25,22),lv(33,20)],[evo(217,param=30)],region="JOHTO",
      habitat=["FOREST"],desc="A honey-loving cub that scents food from miles away."),
    c(217,"Bearram",["NEUTRAL"],90,130,75,75,75,55,60,189,
      [lv(1,1),lv(9,8),lv(17,93),lv(25,22),lv(33,20),lv(43,6)],[],region="JOHTO",
      habitat=["FOREST"],desc="When it stands on two legs its terrifying roar echoes for miles."),
    # ── Slugma/Magcargo 218-219 ───────────────────────────────────────────────
    c(218,"Magmite",["FLAME"],40,40,40,70,40,20,190,87,
      [lv(1,31),lv(8,191),lv(17,32),lv(25,39),lv(33,33),lv(44,38)],[evo(219,param=38)],region="JOHTO",
      habitat=["MOUNTAIN"],desc="A living blob of magma that leaves a searing trail behind it."),
    c(219,"Magcargill",["FLAME","STONE"],50,50,120,80,80,30,75,155,
      [lv(1,31),lv(8,191),lv(17,32),lv(25,39),lv(33,33),lv(44,38),lv(55,196)],[],region="JOHTO",
      habitat=["MOUNTAIN"],desc="Its shell reaches 18,000 degrees — touching it means instant burns."),
    # ── Swinub/Piloswine 220-221 ──────────────────────────────────────────────
    c(220,"Snufflet",["FROST","TERRA"],50,50,40,30,30,50,225,50,
      [lv(1,1),lv(8,114),lv(15,142),lv(23,117),lv(31,143),lv(41,112)],[evo(221,param=33)],region="JOHTO",
      habitat=["MOUNTAIN"],desc="Digs through snow to find ancient berries preserved in ice."),
    c(221,"Swinemaw",["FROST","TERRA"],100,100,80,60,60,50,75,155,
      [lv(1,1),lv(8,114),lv(15,142),lv(23,117),lv(31,143),lv(41,112),lv(51,113)],[],region="JOHTO",
      habitat=["MOUNTAIN"],desc="Charges through blizzards with fur coated in ice armour."),
    # ── Corsola 222 ───────────────────────────────────────────────────────────
    c(222,"Coralite",["AQUA","STONE"],55,55,85,65,85,35,60,144,
      [lv(1,51),lv(8,196),lv(17,56),lv(25,198),lv(33,54),lv(44,193)],[],region="JOHTO",
      habitat=["WATER"],desc="Lives in warm shallow reefs; its coral horns regrow overnight."),
    # ── Remoraid/Octillery 223-224 ────────────────────────────────────────────
    c(223,"Gunfish",["AQUA"],35,65,35,65,35,65,190,95,
      [lv(1,51),lv(8,53),lv(16,55),lv(24,56),lv(32,61)],[evo(224,param=25)],region="JOHTO",
      habitat=["WATER"],desc="Attaches to larger fish and fires water projectiles from its mouth."),
    c(224,"Oktillery",["AQUA"],75,105,75,105,75,45,75,168,
      [lv(1,51),lv(8,53),lv(16,55),lv(24,56),lv(32,61),lv(44,55),lv(54,5)],[],region="JOHTO",
      habitat=["WATER"],desc="Fires a torrent of concentrated water powerful enough to pierce steel."),
    # ── Delibird 225 ──────────────────────────────────────────────────────────
    c(225,"Giftbird",["FROST","AERO"],45,55,45,65,45,75,45,116,
      [lv(1,111),lv(8,151),lv(16,115),lv(24,113),lv(32,154)],[],region="JOHTO",
      habitat=["MOUNTAIN"],desc="Carries supplies in its tail-sack; sometimes gifts rival trainers."),
    # ── Mantine 226 ───────────────────────────────────────────────────────────
    c(226,"Mantaray",["AQUA","AERO"],65,40,70,80,140,70,25,161,
      [lv(1,151),lv(8,53),lv(16,54),lv(24,156),lv(32,55),lv(44,154)],[],region="JOHTO",
      habitat=["WATER"],desc="Glides above ocean waves using graceful, wing-like pectoral fins."),
    # ── Skarmory 227 ──────────────────────────────────────────────────────────
    c(227,"Skarmore",["IRON","AERO"],65,80,140,40,70,70,25,163,
      [lv(1,231),lv(9,152),lv(17,153),lv(25,237),lv(33,234),lv(45,155)],[],region="JOHTO",
      habitat=["MOUNTAIN"],desc="A steel bird whose razor feathers slice through rock effortlessly."),
    # ── Houndour/Houndoom 228-229 ─────────────────────────────────────────────
    c(228,"Scorchhound",["MURK","FLAME"],45,60,30,80,50,65,120,104,
      [lv(1,221),lv(6,31),lv(12,207),lv(20,32),lv(28,223),lv(38,33)],[evo(229,param=24)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Hunts in packs coordinated by a secret system of howls."),
    c(229,"Houndoom",["MURK","FLAME"],75,90,50,110,80,95,45,175,
      [lv(1,221),lv(6,31),lv(12,207),lv(20,32),lv(28,223),lv(38,33),lv(50,216)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="The underworld hound; its flames never heal wounds they cause."),
    # ── Kingdra 230 (evolves from Johto Horsea-evolved via trade + item) ──────
    c(230,"Kingdrake",["AQUA","WYRM"],75,95,95,95,95,85,45,207,
      [lv(1,51),lv(8,53),lv(16,212),lv(25,55),lv(32,213),lv(45,214)],[],region="JOHTO",
      habitat=["WATER"],desc="Slumbers in deep-ocean trenches; wakes to cause tidal waves."),
    # ── Phanpy/Donphan 231-232 ────────────────────────────────────────────────
    c(231,"Trunklet",["TERRA"],90,60,60,40,40,40,120,113,
      [lv(1,141),lv(8,51),lv(15,143),lv(22,143),lv(30,146)],[evo(232,param=25)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Uses its rubbery trunk to spray water or roll into a ball."),
    c(232,"Donkephant",["TERRA"],90,120,120,60,60,50,45,175,
      [lv(1,141),lv(8,51),lv(15,143),lv(22,143),lv(30,146),lv(40,6),lv(50,193)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="A massive armoured elephant that bowls over boulders with ease."),
    # ── Porygon2 233 ──────────────────────────────────────────────────────────
    c(233,"Polygone2",["NEUTRAL"],85,80,90,105,95,60,45,180,
      [lv(1,1),lv(9,78),lv(17,73),lv(25,164),lv(33,163),lv(45,5)],[],region="JOHTO",
      habitat=["TOWN"],desc="An upgraded digital entity with human-made emotional algorithms."),
    # ── Stantler 234 ──────────────────────────────────────────────────────────
    c(234,"Dreamdeer",["NEUTRAL"],73,95,62,85,65,85,45,163,
      [lv(1,1),lv(9,161),lv(17,166),lv(25,168),lv(33,163),lv(45,165)],[],region="JOHTO",
      habitat=["FOREST"],desc="Its antlers bend light, creating mirages that disorient enemies."),
    # ── Smoochum/Elekid/Magby (baby forms) 238-240 ───────────────────────────
    c(238,"Frostbaby",["FROST","PSYCHE"],45,30,15,85,65,65,45,34,
      [lv(1,114),lv(7,168),lv(15,112),lv(24,162),lv(33,163)],[evo(124,param=30)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Baby form of the frost-psychic sorceress."),
    c(239,"Voltbaby",["VOLT"],45,63,37,65,55,95,45,34,
      [lv(1,71),lv(7,77),lv(15,72),lv(23,73)],[evo(125,param=30)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Baby electric creature whose horns spark uncontrollably."),
    c(240,"Emberbaby",["FLAME"],45,75,37,70,55,83,45,34,
      [lv(1,31),lv(7,8),lv(15,35),lv(23,32)],[evo(126,param=30)],region="JOHTO",
      habitat=["GRASSLAND"],desc="Baby fire creature whose forehead flame burns brightest at dusk."),
    # ── Miltank 241 ───────────────────────────────────────────────────────────
    c(241,"Milktaur",["NEUTRAL"],95,80,105,40,70,100,45,172,
      [lv(1,1),lv(8,12),lv(15,20),lv(22,17),lv(35,6)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="Produces restorative milk that heals any ailment instantly."),
    # ── Blissey 242 ───────────────────────────────────────────────────────────
    c(242,"Blissara",["NEUTRAL"],255,10,10,75,135,55,30,255,
      [lv(1,3),lv(9,8),lv(17,17),lv(25,29),lv(33,246),lv(45,20),lv(55,5)],[],region="JOHTO",
      habitat=["GRASSLAND"],desc="The most compassionate healer. Its egg can cure any illness."),
    # ── Legendary beasts 243-245 ─────────────────────────────────────────────
    c(243,"Boltstride",["VOLT"],90,85,75,115,100,115,3,215,
      [lv(1,71),lv(1,14),lv(9,77),lv(15,72),lv(22,73),lv(29,80),lv(43,74),lv(50,5)],
      [],region="JOHTO",legendary=True,habitat=["GRASSLAND"],
      desc="The legendary thunder beast that races across open plains like lightning."),
    c(244,"Infernalion",["FLAME"],115,115,85,90,75,100,3,215,
      [lv(1,31),lv(1,10),lv(9,39),lv(15,35),lv(22,32),lv(29,40),lv(43,33),lv(50,5)],
      [],region="JOHTO",legendary=True,habitat=["MOUNTAIN"],
      desc="The legendary fire beast. Its body blazes with the force of a volcano."),
    c(245,"Tidehearth",["AQUA"],100,75,115,90,115,85,3,215,
      [lv(1,51),lv(1,60),lv(9,53),lv(15,56),lv(22,112),lv(29,55),lv(43,54),lv(50,5)],
      [],region="JOHTO",legendary=True,habitat=["WATER"],
      desc="The legendary water beast. Its crystal mane purifies any water it touches."),
    # ── Larvitar line 246-248 ─────────────────────────────────────────────────
    c(246,"Stonelarva",["STONE","TERRA"],50,64,50,45,50,41,45,60,
      [lv(1,191),lv(5,142),lv(10,143),lv(20,192),lv(30,193),lv(42,146)],[evo(247,param=30)],region="JOHTO",
      habitat=["MOUNTAIN"],desc="Hatched from a mountain. Eats rock to grow stronger."),
    c(247,"Stonepupa",["STONE","TERRA"],70,84,70,65,70,51,45,144,
      [lv(1,191),lv(5,142),lv(10,143),lv(20,192),lv(30,193),lv(42,146)],[evo(248,param=55)],region="JOHTO",
      habitat=["MOUNTAIN"],desc="The armoured cocoon stage; it will emerge as a terrifying dragon."),
    c(248,"Tyrantalith",["STONE","WYRM"],100,134,110,95,100,61,45,270,
      [lv(1,191),lv(5,142),lv(10,143),lv(20,192),lv(30,193),lv(42,146),lv(55,213),lv(65,216)],[],region="JOHTO",
      legendary=False,habitat=["MOUNTAIN"],
      desc="The mountain dragon tyrant. Its roar shakes the foundations of the earth."),
    # ── Tower duo 249-250 ─────────────────────────────────────────────────────
    c(249,"Tempestlord",["PSYCHE","AERO"],106,90,130,90,154,110,3,220,
      [lv(1,161),lv(1,151),lv(9,112),lv(15,162),lv(22,156),lv(29,163),lv(43,154),lv(50,165)],
      [],region="JOHTO",legendary=True,habitat=["WATER"],
      desc="The silver sea sovereign that rules both sky and ocean from the storm."),
    c(250,"Emberlord",["FLAME","AERO"],106,130,90,110,154,90,3,220,
      [lv(1,31),lv(1,151),lv(9,32),lv(15,152),lv(22,37),lv(29,40),lv(43,33),lv(50,154)],
      [],region="JOHTO",legendary=True,habitat=["MOUNTAIN"],
      desc="The golden sky sovereign. Legends say it can resurrect creatures left cold."),
    # ── Time fairy 251 ────────────────────────────────────────────────────────
    c(251,"Timelynx",["PSYCHE","FLORA"],100,100,100,100,100,100,45,220,
      [lv(1,161),lv(1,91),lv(10,19),lv(20,163),lv(30,103),lv(40,167),lv(50,165)],
      [],region="JOHTO",legendary=True,habitat=["FOREST"],
      desc="A mythical lynx with power over time. Its presence heals the land."),
]

# Fill gaps in Johto
johto_ids = {cre["id"] for cre in johto}
for missing_id in range(152, 252):
    if missing_id not in johto_ids:
        johto.append(c(missing_id, f"Johto{missing_id:03d}", ["NEUTRAL"],
          55,70,70,55,55,70, 100, 110,
          [lv(1,1),lv(10,7),lv(20,20),lv(30,22)], [], "JOHTO",
          False, False, ["GRASSLAND"], f"Creature #{missing_id}."))

johto.sort(key=lambda x: x["id"])

# ──────────────────────────────────────────────────────────────────────────────
# HOENN  IDs 252 – 386
# ──────────────────────────────────────────────────────────────────────────────
hoenn = [
    # Starters
    c(252,"Leafbud",["FLORA"],40,45,35,65,55,70,45,62,
      [lv(1,8),lv(7,92),lv(13,102),lv(20,99),lv(28,95),lv(36,96),lv(46,100)],
      [evo(253,param=16)],region="HOENN",starter=True,habitat=["FOREST"],
      desc="An agile gecko that can scale vertical walls and clings with suction pads."),
    c(253,"Leafdash",["FLORA"],50,65,45,85,65,95,45,141,
      [lv(1,8),lv(7,92),lv(13,102),lv(20,99),lv(28,95),lv(36,96),lv(46,100)],
      [evo(254,param=36)],region="HOENN",habitat=["FOREST"],
      desc="Runs along walls and ceilings; the leaf on its tail channels solar energy."),
    c(254,"Leafrazor",["FLORA"],70,85,65,105,85,120,45,208,
      [lv(1,8),lv(7,92),lv(13,102),lv(20,99),lv(28,99),lv(36,96),lv(46,100),lv(58,5)],
      [],region="HOENN",habitat=["FOREST"],
      desc="A master of camouflage. Its blade-leaf tail can cut clean through stone."),
    c(255,"Ignisaur",["FLAME"],45,60,40,70,50,45,45,62,
      [lv(1,9),lv(7,31),lv(10,8),lv(16,34),lv(25,32),lv(36,36),lv(47,33)],
      [evo(256,param=16)],region="HOENN",starter=True,habitat=["MOUNTAIN"],
      desc="A chick-like flame creature that struts with surprising confidence."),
    c(256,"Ignisoul",["FLAME","BRAWL"],60,85,60,85,60,55,45,142,
      [lv(1,9),lv(7,31),lv(10,8),lv(16,34),lv(25,32),lv(36,36),lv(47,33),lv(58,36)],
      [evo(257,param=36)],region="HOENN",habitat=["MOUNTAIN"],
      desc="A fiery fighter who challenges opponents with elegant kicks."),
    c(257,"Igniblaze",["FLAME","BRAWL"],80,120,70,110,70,80,45,209,
      [lv(1,9),lv(7,31),lv(10,8),lv(16,34),lv(25,32),lv(36,36),lv(47,33),lv(58,36),lv(65,124)],
      [],region="HOENN",habitat=["MOUNTAIN"],
      desc="A blazing martial artist whose flaming kicks reach 1500 degrees."),
    c(258,"Mudlet",["AQUA","TERRA"],50,70,45,50,45,40,45,63,
      [lv(1,9),lv(6,51),lv(11,142),lv(16,143),lv(24,147),lv(31,54),lv(39,55)],
      [evo(259,param=16)],region="HOENN",starter=True,habitat=["WATER"],
      desc="A cute mud fish that navigates land and water with equal ease."),
    c(259,"Mudslump",["AQUA","TERRA"],70,85,70,60,60,50,45,141,
      [lv(1,9),lv(6,51),lv(11,142),lv(16,143),lv(24,147),lv(31,54),lv(39,55),lv(49,61)],
      [evo(260,param=36)],region="HOENN",habitat=["WATER"],
      desc="Rolls through mud at high speed, carrying enormous momentum."),
    c(260,"Mudsmasher",["AQUA","TERRA"],100,110,90,85,85,60,45,218,
      [lv(1,9),lv(6,51),lv(11,142),lv(16,143),lv(24,147),lv(31,54),lv(39,55),lv(49,61),lv(59,143),lv(69,5)],
      [],region="HOENN",habitat=["WATER"],
      desc="A swamp titan whose mud-cannon arm launches projectiles at crushing force."),
    # ── Zigzagoon line 263-264 ────────────────────────────────────────────────
    c(263,"Ziggling",["NEUTRAL"],38,30,41,30,41,60,255,56,
      [lv(1,1),lv(5,9),lv(9,4),lv(17,22)],[evo(264,param=20)],region="HOENN",
      habitat=["GRASSLAND"],desc="A raccoon dog that zig-zags to confuse predators."),
    c(264,"Linooner",["NEUTRAL"],78,70,61,50,61,100,90,147,
      [lv(1,1),lv(5,9),lv(9,4),lv(17,22),lv(27,25),lv(40,6)],[],region="HOENN",
      habitat=["GRASSLAND"],desc="Becomes too straight to dodge when startled — and crashes into things."),
    # ── Wurmple line 265-269 ──────────────────────────────────────────────────
    c(265,"Silkwurm",["INSECT"],45,45,35,20,30,20,255,54,
      [lv(1,181),lv(7,182)],[evo(266,param=7),evo(268,param=7)],region="HOENN",
      habitat=["FOREST"],desc="A soft worm that excretes sticky silk to build its cocoon."),
    c(266,"Silkooze",["INSECT"],50,35,55,25,25,15,120,71,
      [lv(1,13)],[evo(267,param=10)],region="HOENN",
      habitat=["FOREST"],desc="A cocoon stage that drips silk to bind itself to branches."),
    c(267,"Silkflutter",["INSECT","RADIANT"],60,70,50,100,80,65,45,170,
      [lv(1,151),lv(10,153),lv(18,183),lv(25,185),lv(38,241)],[],region="HOENN",
      habitat=["FOREST"],desc="A beautiful moth whose shimmering wings scatter radiant scales."),
    c(268,"Silkbarb",["INSECT","VENOM"],50,35,55,25,25,15,120,71,
      [lv(1,13)],[evo(269,param=10)],region="HOENN",
      habitat=["FOREST"],desc="A toxic cocoon stage that poisons anything that touches it."),
    c(269,"Silkvenom",["INSECT","VENOM"],60,70,50,90,90,65,45,173,
      [lv(1,131),lv(10,135),lv(18,134),lv(25,136),lv(38,185)],[],region="HOENN",
      habitat=["FOREST"],desc="Toxic dust from its wings corrodes armour and weakens foes."),
    # ── Lotad line 270-272 ────────────────────────────────────────────────────
    c(270,"Padlet",["AQUA","FLORA"],40,30,30,40,50,30,255,74,
      [lv(1,51),lv(7,91),lv(14,94),lv(20,103),lv(28,95)],[evo(271,param=14)],region="HOENN",
      habitat=["WATER"],desc="A small water-lily creature that catches falling rain in its leaf."),
    c(271,"Padquake",["AQUA","FLORA"],60,50,50,60,70,50,120,161,
      [lv(1,51),lv(7,91),lv(14,94),lv(20,103),lv(28,95),lv(40,55)],[evo(272,param=14)],region="HOENN",
      habitat=["WATER"],desc="The leaf hat grows large enough to shelter a small family."),
    c(272,"Padking",["AQUA","FLORA"],80,70,70,90,100,70,45,181,
      [lv(1,51),lv(7,91),lv(14,94),lv(20,103),lv(28,95),lv(40,55),lv(55,98)],[],region="HOENN",
      habitat=["WATER"],desc="A noble lily-pad creature that purifies rivers with each step."),
    # ── Seedot line 273-275 ───────────────────────────────────────────────────
    c(273,"Seedrop",["FLORA"],40,40,50,30,30,30,255,74,
      [lv(1,93),lv(7,108),lv(14,94),lv(20,13),lv(28,99)],[evo(274,param=14)],region="HOENN",
      habitat=["FOREST"],desc="Dangles from branches to absorb sunlight, resembling an acorn."),
    c(274,"Nuzleigh",["FLORA","MURK"],70,70,40,60,40,60,120,141,
      [lv(1,93),lv(7,108),lv(14,94),lv(20,226),lv(28,99),lv(40,95)],[evo(275,param=14)],region="HOENN",
      habitat=["FOREST"],desc="Cloaks itself in shadow to blend in with the forest canopy."),
    c(275,"Shiftreek",["FLORA","MURK"],90,100,60,90,60,80,45,198,
      [lv(1,93),lv(7,108),lv(14,94),lv(20,226),lv(28,99),lv(40,95),lv(54,100)],[],region="HOENN",
      habitat=["FOREST"],desc="Master of ambush; vanishes into shadows and strikes without warning."),
    # ── Taillow line 276-277 ──────────────────────────────────────────────────
    c(276,"Swiftling",["NEUTRAL","AERO"],40,55,30,30,30,85,200,54,
      [lv(1,152),lv(7,153),lv(13,156),lv(20,155)],[evo(277,param=22)],region="HOENN",
      habitat=["GRASSLAND"],desc="A dauntless small bird that attacks creatures far larger than itself."),
    c(277,"Swiftale",["NEUTRAL","AERO"],60,85,60,50,50,125,100,159,
      [lv(1,152),lv(7,153),lv(13,156),lv(20,155),lv(30,154)],[],region="HOENN",
      habitat=["GRASSLAND"],desc="Rides warm air currents with astounding speed and precision."),
    # ── Wingull line 278-279 ──────────────────────────────────────────────────
    c(278,"Wingull",["AQUA","AERO"],40,30,30,55,30,85,190,54,
      [lv(1,51),lv(7,151),lv(14,153),lv(22,56),lv(30,154)],[evo(279,param=25)],region="HOENN",
      habitat=["WATER"],desc="A sea gull that nests on cliffs and scouts for food over the ocean."),
    c(279,"Pelikrang",["AQUA","AERO"],60,50,100,85,70,65,75,164,
      [lv(1,51),lv(7,151),lv(14,153),lv(22,56),lv(30,154),lv(40,55)],[],region="HOENN",
      habitat=["WATER"],desc="A large pelican that can hold incredible amounts of water in its pouch."),
    # ── Ralts line 280-282 ────────────────────────────────────────────────────
    c(280,"Raltling",["PSYCHE","RADIANT"],28,25,25,45,35,40,235,40,
      [lv(1,244),lv(6,161),lv(11,162),lv(17,163),lv(25,167),lv(33,241)],[evo(281,param=20)],region="HOENN",
      habitat=["GRASSLAND"],desc="Senses emotions with the horns on its head."),
    c(281,"Kirlihorn",["PSYCHE","RADIANT"],38,35,35,65,55,50,120,97,
      [lv(1,244),lv(6,161),lv(11,162),lv(17,163),lv(25,167),lv(33,241)],[evo(282,param=30)],region="HOENN",
      habitat=["GRASSLAND"],desc="The horn atop its head grows to a fine point as psychic power surges."),
    c(282,"Gardevoir",["PSYCHE","RADIANT"],68,65,65,125,115,80,45,233,
      [lv(1,244),lv(6,161),lv(11,162),lv(17,163),lv(25,167),lv(33,241),lv(45,165)],[],region="HOENN",
      habitat=["GRASSLAND"],desc="Creates a micro black hole to protect its trainer from any danger."),
    # ── Surskit line 283-284 ──────────────────────────────────────────────────
    c(283,"Surskite",["INSECT","AQUA"],40,30,32,50,52,65,200,54,
      [lv(1,51),lv(7,182),lv(13,183),lv(20,185)],[evo(284,param=22)],region="HOENN",
      habitat=["WATER"],desc="Skates across water surfaces with legs that secrete a sticky oil."),
    c(284,"Mightymoth",["INSECT","AERO"],60,60,62,100,82,60,75,162,
      [lv(1,51),lv(7,182),lv(13,183),lv(20,185),lv(30,187),lv(40,154)],[],region="HOENN",
      habitat=["FOREST"],desc="Uses wind gusts to scatter disorienting wing scales."),
    # ── Shroomish line 285-286 ────────────────────────────────────────────────
    c(285,"Shroomlet",["FLORA"],60,40,60,40,60,35,255,74,
      [lv(1,93),lv(7,105),lv(14,107),lv(22,104),lv(30,101)],[evo(286,param=23)],region="HOENN",
      habitat=["FOREST"],desc="A small mushroom creature that disperses spores to fend off predators."),
    c(286,"Breloomstrike",["FLORA","BRAWL"],60,130,80,60,60,70,90,161,
      [lv(1,93),lv(7,105),lv(14,107),lv(22,104),lv(30,122),lv(40,124),lv(50,126)],[],region="HOENN",
      habitat=["FOREST"],desc="A mushroom boxer whose stretchy arms can land blows from incredible range."),
    # ── Slakoth line 287-289 ──────────────────────────────────────────────────
    c(287,"Slakster",["NEUTRAL"],60,60,60,35,35,30,255,56,
      [lv(1,1),lv(9,93),lv(17,20),lv(25,22)],[evo(288,param=18)],region="HOENN",
      habitat=["FOREST"],desc="Sleeps 20 hours a day. Barely moves even when attacked."),
    c(288,"Vigoroth",["NEUTRAL"],80,80,80,55,55,90,120,145,
      [lv(1,1),lv(9,4),lv(17,20),lv(25,22),lv(36,6)],[evo(289,param=36)],region="HOENN",
      habitat=["FOREST"],desc="Too hyper to sleep — full of frenzied, unstoppable energy."),
    c(289,"Slakosaurus",["NEUTRAL"],150,160,100,95,65,100,45,252,
      [lv(1,1),lv(9,4),lv(17,20),lv(25,22),lv(36,6),lv(50,5)],[],region="HOENN",
      habitat=["FOREST"],desc="The most powerful and the laziest creature alive. An unstoppable force when it bothers."),
    # ── Nincada line 290-292 ──────────────────────────────────────────────────
    c(290,"Nincadet",["INSECT","TERRA"],31,45,90,30,30,40,255,74,
      [lv(1,181),lv(9,2),lv(14,184),lv(20,144),lv(25,193)],[evo(291,param=20)],region="HOENN",
      habitat=["FOREST"],desc="A ground-burrowing cicada nymph that rarely sees sunlight."),
    c(291,"Ninjafly",["INSECT","AERO"],61,90,45,50,50,160,45,159,
      [lv(1,181),lv(9,153),lv(14,184),lv(20,156),lv(25,193),lv(38,185)],[],region="HOENN",
      habitat=["FOREST"],desc="An evasive ninja bug that moves faster than the eye can follow."),
    c(292,"Ghostshell",["INSECT","SHADE"],1,90,45,30,30,40,45,95,
      [lv(1,205),lv(9,206),lv(14,184),lv(20,202),lv(25,204)],[],region="HOENN",
      habitat=["FOREST"],desc="A hollow husk haunted by the spirit left behind during evolution."),
    # ── Whismur line 293-295 ──────────────────────────────────────────────────
    c(293,"Whislet",["NEUTRAL"],64,51,23,51,23,28,190,48,
      [lv(1,3),lv(5,8),lv(9,26),lv(16,20),lv(23,16),lv(35,5)],[evo(294,param=20)],region="HOENN",
      habitat=["CAVE"],desc="Cries like a siren; plugs its own ears with flaps on its head."),
    c(294,"Loudred",["NEUTRAL"],84,71,43,71,43,48,120,112,
      [lv(1,3),lv(5,8),lv(9,26),lv(16,20),lv(23,16),lv(35,5)],[evo(295,param=40)],region="HOENN",
      habitat=["CAVE"],desc="Its voice cracks stone walls and shatters windows."),
    c(295,"Boomblare",["NEUTRAL"],104,91,63,91,63,68,45,167,
      [lv(1,3),lv(5,8),lv(9,26),lv(16,20),lv(23,16),lv(35,5),lv(50,26)],[],region="HOENN",
      habitat=["CAVE"],desc="Its sonic blasts register at 150 decibels — deafening without ear protection."),
    # ── Aron line 304-306 ────────────────────────────────────────────────────
    c(304,"Ironling",["IRON","STONE"],50,70,100,40,40,30,180,66,
      [lv(1,231),lv(7,191),lv(12,237),lv(20,234),lv(30,193),lv(40,232)],[evo(305,param=32)],region="HOENN",
      habitat=["CAVE"],desc="Munches on iron ore to build its armoured shell."),
    c(305,"Irondon",["IRON","STONE"],60,90,140,50,50,40,90,140,
      [lv(1,231),lv(7,191),lv(12,237),lv(20,234),lv(30,193),lv(40,232)],[evo(306,param=42)],region="HOENN",
      habitat=["CAVE"],desc="Smashes through mountain rock faces when hungry."),
    c(306,"Ironaggron",["IRON","STONE"],70,110,180,60,60,50,45,205,
      [lv(1,231),lv(7,191),lv(12,237),lv(20,234),lv(30,193),lv(40,232),lv(55,5)],[],region="HOENN",
      habitat=["CAVE"],desc="A walking fortress that can smash through skyscrapers."),
    # ── Meditite line 307-308 ────────────────────────────────────────────────
    c(307,"Meditite",["BRAWL","PSYCHE"],30,40,55,40,55,60,180,56,
      [lv(1,121),lv(7,161),lv(13,122),lv(20,162),lv(28,123),lv(36,163),lv(48,124)],[evo(308,param=37)],region="HOENN",
      habitat=["MOUNTAIN"],desc="Meditates for days without eating; spiritual power fills the void."),
    c(308,"Medicham",["BRAWL","PSYCHE"],60,60,75,60,75,80,90,144,
      [lv(1,121),lv(7,161),lv(13,122),lv(20,162),lv(28,123),lv(36,163),lv(48,124),lv(60,164)],[],region="HOENN",
      habitat=["MOUNTAIN"],desc="Psychic power amplifies its punches to devastating levels."),
    # ── Electrike line 309-310 ────────────────────────────────────────────────
    c(309,"Sparkite",["VOLT"],40,45,40,65,40,65,120,59,
      [lv(1,71),lv(5,80),lv(9,77),lv(15,72),lv(25,73),lv(35,74)],[evo(310,param=26)],region="HOENN",
      habitat=["GRASSLAND"],desc="Friction from its fur generates enough electricity to power a small home."),
    c(310,"Manectric",["VOLT"],70,75,60,105,60,105,45,168,
      [lv(1,71),lv(5,80),lv(9,77),lv(15,72),lv(25,73),lv(35,74),lv(45,5)],[],region="HOENN",
      habitat=["GRASSLAND"],desc="Nests in thunderclouds. Its howl summons lightning strikes."),
    # ── Roselia 315 → already has Kanto base 315 skipped; define it here ──────
    c(315,"Roseling",["FLORA","VENOM"],50,60,45,100,80,65,150,140,
      [lv(1,131),lv(7,91),lv(13,133),lv(20,98),lv(28,134),lv(35,96)],[],region="HOENN",
      habitat=["GRASSLAND"],desc="Its left rose contains poison; its right holds sweet nectar."),
    # ── Carvanha line 318-319 ────────────────────────────────────────────────
    c(318,"Carvsharke",["AQUA","MURK"],45,90,20,65,20,65,225,61,
      [lv(1,51),lv(7,221),lv(13,223),lv(20,222),lv(28,55)],[evo(319,param=30)],region="HOENN",
      habitat=["WATER"],desc="Razor-toothed predator fish that hunts in frenetic packs."),
    c(319,"Sharpedon",["AQUA","MURK"],70,120,40,95,40,95,60,161,
      [lv(1,51),lv(7,221),lv(13,223),lv(20,222),lv(28,55),lv(40,216)],[],region="HOENN",
      habitat=["WATER"],desc="A torpedo shark that charges targets at 80 knots underwater."),
    # ── Trapinch line 328-330 ────────────────────────────────────────────────
    c(328,"Trapling",["TERRA"],45,100,45,45,45,10,255,58,
      [lv(1,141),lv(9,181),lv(17,143),lv(25,146),lv(33,144)],[evo(329,param=35)],region="HOENN",
      habitat=["DESERT"],desc="Digs pitfall traps in sand; its jaws crack boulders."),
    c(329,"Vibradash",["TERRA","WYRM"],50,70,50,50,50,70,120,119,
      [lv(1,141),lv(9,212),lv(17,143),lv(25,213),lv(33,144),lv(45,217)],[evo(330,param=45)],region="HOENN",
      habitat=["DESERT"],desc="Its wings create powerful vibrations that can liquefy sand."),
    c(330,"Flygondra",["TERRA","WYRM"],80,100,80,80,80,100,45,204,
      [lv(1,212),lv(9,143),lv(17,213),lv(25,214),lv(33,217),lv(45,215),lv(55,216)],[],region="HOENN",
      habitat=["DESERT"],desc="A dragon of the desert sands whose wings howl like a spirit wail."),
    # ── Cacnea line 331-332 ───────────────────────────────────────────────────
    c(331,"Cactorlet",["FLORA"],50,85,40,85,40,35,190,74,
      [lv(1,131),lv(7,91),lv(13,135),lv(20,99),lv(28,101),lv(36,100)],[evo(332,param=32)],region="HOENN",
      habitat=["DESERT"],desc="Wanders the desert; its spines contain a mild paralytic venom."),
    c(332,"Cacturnia",["FLORA","MURK"],70,115,60,115,60,55,60,166,
      [lv(1,131),lv(7,91),lv(13,135),lv(20,99),lv(28,101),lv(36,100),lv(50,223)],[],region="HOENN",
      habitat=["DESERT"],desc="Dances with spiny arms; each step leaves a trail of venom."),
    # ── Zangoose 335 / Seviper 336 ────────────────────────────────────────────
    c(335,"Zangofast",["NEUTRAL"],73,115,60,60,60,90,45,160,
      [lv(1,2),lv(7,4),lv(13,15),lv(20,22),lv(28,203),lv(38,6)],[],region="HOENN",
      habitat=["GRASSLAND"],desc="A fierce mongoose that never backs down from a fight with Seviper."),
    c(336,"Seviclash",["VENOM"],73,100,60,100,60,65,45,160,
      [lv(1,131),lv(7,221),lv(13,136),lv(20,133),lv(28,134),lv(38,135)],[],region="HOENN",
      habitat=["GRASSLAND"],desc="A large venomous serpent that has feuded with Zangofast for millennia."),
    # ── Lunatone / Solrock 337-338 ────────────────────────────────────────────
    c(337,"Lunaite",["STONE","PSYCHE"],70,55,65,95,85,70,45,161,
      [lv(1,161),lv(8,119),lv(16,112),lv(24,162),lv(32,163),lv(45,165)],[],region="HOENN",
      habitat=["CAVE"],desc="A crescent moon stone creature that grows stronger under moonlight."),
    c(338,"Solrock",["STONE","PSYCHE"],70,95,85,55,65,70,45,161,
      [lv(1,161),lv(8,40),lv(16,32),lv(24,162),lv(32,163),lv(45,166)],[],region="HOENN",
      habitat=["CAVE"],desc="A sun-shaped rock creature that pulses with solar energy."),
    # ── Barboach line 339-340 ────────────────────────────────────────────────
    c(339,"Barboach",["AQUA","TERRA"],50,48,43,46,41,60,190,70,
      [lv(1,142),lv(7,51),lv(15,143),lv(22,146),lv(30,55)],[evo(340,param=30)],region="HOENN",
      habitat=["WATER"],desc="Uses its whiskers to detect underground rivers."),
    c(340,"Whiscash",["AQUA","TERRA"],110,78,73,76,71,60,75,164,
      [lv(1,142),lv(7,51),lv(15,143),lv(22,146),lv(30,55),lv(45,148)],[],region="HOENN",
      habitat=["WATER"],desc="Its territorial tremors can be felt across vast distances."),
    # ── Feebas/Milotic 349-350 ────────────────────────────────────────────────
    c(349,"Feebas",["AQUA"],20,15,20,10,55,80,255,40,
      [lv(1,51),lv(15,53)],[evo(350,"HAPPINESS")],region="HOENN",
      habitat=["WATER"],desc="The plainest fish in the sea; a master of elegance hides within."),
    c(350,"Milotica",["AQUA"],95,60,79,100,125,81,60,207,
      [lv(1,51),lv(7,53),lv(15,56),lv(22,115),lv(30,54),lv(40,55),lv(50,241)],[],region="HOENN",
      habitat=["WATER"],desc="The most beautiful creature in existence; its presence calms storms."),
    # ── Castform 351 ─────────────────────────────────────────────────────────
    c(351,"Castweather",["NEUTRAL"],70,70,70,70,70,70,45,147,
      [lv(1,1),lv(10,40),lv(20,60),lv(30,119),lv(40,148)],[],region="HOENN",
      habitat=["GRASSLAND"],desc="Its form changes with the weather, adapting perfectly to conditions."),
    # ── Absol 359 ─────────────────────────────────────────────────────────────
    c(359,"Absolhex",["MURK"],65,130,60,75,60,75,30,163,
      [lv(1,221),lv(7,225),lv(14,227),lv(22,223),lv(30,224),lv(42,222),lv(55,5)],[],region="HOENN",
      habitat=["MOUNTAIN"],desc="Appears before disasters; once blamed for causing them."),
    # ── Snorunt line 361-362 ──────────────────────────────────────────────────
    c(361,"Frostlet",["FROST"],50,50,50,50,50,50,190,60,
      [lv(1,114),lv(8,115),lv(17,112),lv(25,117),lv(33,113)],[item_evo(362,406)],region="HOENN",
      habitat=["MOUNTAIN"],desc="A ghost-like ice child that lives in frozen caverns."),
    c(362,"Glalishard",["FROST"],80,80,80,80,80,80,75,168,
      [lv(1,114),lv(8,115),lv(17,112),lv(25,117),lv(33,113),lv(45,113)],[],region="HOENN",
      habitat=["MOUNTAIN"],desc="A crystalline ice ball whose body temperature is -150 degrees."),
    # ── Bagon line 371-373 ────────────────────────────────────────────────────
    c(371,"Bagonlet",["WYRM"],45,75,60,40,30,50,45,60,
      [lv(1,1),lv(8,221),lv(17,211),lv(25,212),lv(33,213),lv(44,215)],[evo(372,param=30)],region="HOENN",
      habitat=["MOUNTAIN"],desc="Headbutts hard objects to toughen its skull — dreams of flying."),
    c(372,"Shellgon",["WYRM"],65,95,100,60,50,50,45,147,
      [lv(1,1),lv(8,221),lv(17,211),lv(25,212),lv(33,213),lv(44,215)],[evo(373,param=50)],region="HOENN",
      habitat=["MOUNTAIN"],desc="Withdraws inside its hard shell while its body undergoes transformation."),
    c(373,"Salamencium",["WYRM","AERO"],95,135,80,110,80,100,45,270,
      [lv(1,213),lv(8,221),lv(17,212),lv(25,214),lv(33,217),lv(44,215),lv(60,154),lv(70,216)],[],region="HOENN",
      habitat=["MOUNTAIN"],desc="A violent sky dragon whose desire to fly finally overcame its shell."),
    # ── Beldum line 374-376 ───────────────────────────────────────────────────
    c(374,"Beldrone",["IRON","PSYCHE"],40,55,80,35,60,30,3,64,
      [lv(1,231)],[evo(375,param=20)],region="HOENN",
      habitat=["MOUNTAIN"],desc="A mechanical creature with no mouth; communicates through magnetism."),
    c(375,"Metaldrone",["IRON","PSYCHE"],60,75,100,55,80,50,3,147,
      [lv(1,231),lv(10,234),lv(20,163)],[evo(376,param=45)],region="HOENN",
      habitat=["MOUNTAIN"],desc="Two Beldrones fused together; their combined psychic field is immense."),
    c(376,"Metatycross",["IRON","PSYCHE"],80,135,130,95,90,70,3,270,
      [lv(1,231),lv(10,234),lv(20,164),lv(30,235),lv(40,163),lv(50,5),lv(60,233)],[],region="HOENN",
      habitat=["MOUNTAIN"],desc="A psychic iron giant whose cross-shaped body generates gravity fields."),
    # ── Legendary golems 377-379 ─────────────────────────────────────────────
    c(377,"Crystalith",["STONE"],80,100,200,50,100,30,3,215,
      [lv(1,191),lv(8,197),lv(16,193),lv(24,196),lv(32,198),lv(43,5)],[],
      region="HOENN",legendary=True,habitat=["CAVE"],
      desc="The golem of rock, sealed in an ancient crystal cavern eons ago."),
    c(378,"Glaciadon",["FROST"],80,50,100,100,200,50,3,215,
      [lv(1,114),lv(8,237),lv(16,112),lv(24,118),lv(32,113),lv(43,5)],[],
      region="HOENN",legendary=True,habitat=["MOUNTAIN"],
      desc="The golem of ice whose body temperature is colder than outer space."),
    c(379,"Ironclad",["IRON"],80,75,150,75,150,50,3,215,
      [lv(1,231),lv(8,237),lv(16,233),lv(24,234),lv(32,235),lv(43,5)],[],
      region="HOENN",legendary=True,habitat=["CAVE"],
      desc="The golem of steel; its iron shell has never been breached."),
    # ── Lati twins 380-381 ────────────────────────────────────────────────────
    c(380,"Auraveil",["WYRM","PSYCHE"],80,80,90,110,130,110,3,211,
      [lv(1,161),lv(8,153),lv(16,156),lv(24,163),lv(32,167),lv(43,165),lv(55,216)],[],
      region="HOENN",legendary=True,habitat=["GRASSLAND"],
      desc="A legendary eon dragon that projects calming auras across entire cities."),
    c(381,"Tideclaw",["WYRM","PSYCHE"],80,90,80,130,110,110,3,211,
      [lv(1,161),lv(8,153),lv(16,156),lv(24,163),lv(32,164),lv(43,165),lv(55,216)],[],
      region="HOENN",legendary=True,habitat=["GRASSLAND"],
      desc="A legendary eon dragon that flies faster than sound at full speed."),
    # ── Weather duo 382-383 ───────────────────────────────────────────────────
    c(382,"Oceanmaw",["AQUA"],100,100,90,150,140,130,3,218,
      [lv(1,51),lv(8,60),lv(16,54),lv(24,112),lv(32,55),lv(43,55),lv(55,165)],[],
      region="HOENN",legendary=True,habitat=["WATER"],
      desc="The primordial sea deity whose awakening floods entire continents."),
    c(383,"Magmawrath",["TERRA"],100,150,140,100,90,90,3,218,
      [lv(1,143),lv(8,141),lv(16,143),lv(24,146),lv(32,148),lv(43,6),lv(55,5)],[],
      region="HOENN",legendary=True,habitat=["MOUNTAIN"],
      desc="The primordial land deity whose awakening raises mountain ranges from the sea."),
    # ── Sky dragon 384 ────────────────────────────────────────────────────────
    c(384,"Skydrake",["WYRM","AERO"],105,150,90,150,90,95,3,220,
      [lv(1,213),lv(8,151),lv(16,214),lv(24,212),lv(32,217),lv(43,215),lv(55,154),lv(65,216)],[],
      region="HOENN",legendary=True,habitat=["MOUNTAIN"],
      desc="The sky sovereign. It patrols the upper atmosphere and mediates between sea and land."),
    # ── Event legendaries 385-386 ─────────────────────────────────────────────
    c(385,"Wishsprite",["IRON","PSYCHE"],100,100,100,100,100,100,3,220,
      [lv(1,17),lv(10,244),lv(20,234),lv(30,163),lv(40,167),lv(50,165)],[],
      region="HOENN",legendary=True,habitat=["GRASSLAND"],
      desc="A mythical star creature that grants any wish to those it trusts."),
    c(386,"Voidform",["PSYCHE"],50,150,50,150,50,150,3,215,
      [lv(1,164),lv(10,14),lv(20,165),lv(30,167),lv(40,163),lv(50,5)],[],
      region="HOENN",legendary=True,habitat=["CAVE"],
      desc="A meteorite creature with multiple forms. Origin of life's genetic code."),
]

# Fill gaps in Hoenn
hoenn_ids = {cre["id"] for cre in hoenn}
for missing_id in range(252, 387):
    if missing_id not in hoenn_ids:
        hoenn.append(c(missing_id, f"Hoenn{missing_id:03d}", ["NEUTRAL"],
          60,75,75,60,60,75, 100, 120,
          [lv(1,1),lv(10,7),lv(20,20),lv(30,22)], [], "HOENN",
          False, False, ["GRASSLAND"], f"Creature #{missing_id}."))

hoenn.sort(key=lambda x: x["id"])

# ── Output ────────────────────────────────────────────────────────────────────
for name, data in [("kanto", kanto_all), ("johto", johto), ("hoenn", hoenn)]:
    path = f"{BASE}/{name}.json"
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
    print(f"Generated {len(data)} creatures → {path}")
