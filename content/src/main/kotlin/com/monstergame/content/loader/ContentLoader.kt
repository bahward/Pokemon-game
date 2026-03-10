package com.monstergame.content.loader

import com.monstergame.content.model.*
import com.monstergame.engine.model.*
import com.monstergame.engine.overworld.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Parses JSON content files into engine model objects.
 * The Android layer passes raw JSON strings from assets.
 */
class ContentLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun loadCreatures(jsonText: String): Map<Int, CreatureSpecies> {
        val dtos = json.decodeFromString<List<CreatureDto>>(jsonText)
        return dtos.associate { dto -> dto.id to dto.toModel() }
    }

    fun loadMoves(jsonText: String): Map<Int, Move> {
        val dtos = json.decodeFromString<List<MoveDto>>(jsonText)
        return dtos.associate { dto -> dto.id to dto.toModel() }
    }

    fun loadItems(jsonText: String): Map<Int, Item> {
        val dtos = json.decodeFromString<List<ItemDto>>(jsonText)
        return dtos.associate { dto -> dto.id to dto.toModel() }
    }

    fun loadAreas(jsonText: String): Map<String, AreaData> {
        val dtos = json.decodeFromString<List<AreaDto>>(jsonText)
        return dtos.associate { dto -> dto.id to dto.toModel() }
    }

    fun loadTrainers(jsonText: String): Map<String, TrainerData> {
        val dtos = json.decodeFromString<List<TrainerDto>>(jsonText)
        return dtos.associate { dto -> dto.id to dto.toModel() }
    }

    fun loadRegion(jsonText: String): RegionData {
        val dto = json.decodeFromString<RegionDto>(jsonText)
        return dto.toModel()
    }

    fun loadEvents(jsonText: String): Map<String, GameEvent> {
        val dtos = json.decodeFromString<List<EventDto>>(jsonText)
        return dtos.associate { dto -> dto.id to dto.toModel() }
    }

    fun loadNpcs(jsonText: String): Map<String, NpcData> {
        val dtos = json.decodeFromString<List<NpcDto>>(jsonText)
        return dtos.associate { dto -> dto.id to dto.toModel() }
    }

    fun loadShops(jsonText: String): List<ShopDto> =
        json.decodeFromString(jsonText)

    // --- Mapping functions ---

    private fun CreatureDto.toModel() = CreatureSpecies(
        id = id, name = name,
        types = types.map { ElementType.valueOf(it) },
        baseStats = BaseStats(baseStats.hp, baseStats.attack, baseStats.defense,
            baseStats.specialAttack, baseStats.specialDefense, baseStats.speed),
        catchRate = catchRate, baseExp = baseExp,
        learnset = learnset.map { LearnEntry(it.level, it.moveId) },
        evolutions = evolutions.map { EvolutionEntry(it.toId, EvolutionMethod.valueOf(it.method), it.param, it.paramStr) },
        regionId = regionId, isStarter = isStarter, isLegendary = isLegendary,
        description = description, habitat = habitat
    )

    private fun MoveDto.toModel() = Move(
        id = id, name = name,
        type = ElementType.valueOf(type),
        category = MoveCategory.valueOf(category),
        power = power, accuracy = accuracy, pp = pp,
        priority = priority,
        effect = runCatching { MoveEffect.valueOf(effect) }.getOrDefault(MoveEffect.NONE),
        effectChance = effectChance,
        target = runCatching { MoveTarget.valueOf(target) }.getOrDefault(MoveTarget.SINGLE_OPPONENT),
        description = description
    )

    private fun ItemDto.toModel() = Item(
        id = id, name = name,
        category = ItemCategory.valueOf(category),
        price = price, description = description,
        healAmount = healAmount, curesStatus = curesStatus,
        curesAll = curesAll, revives = revives, reviveFull = reviveFull,
        ballType = ballType?.let { BallType.valueOf(it) },
        evolvesSpeciesId = evolvesSpeciesId, tmMoveId = tmMoveId
    )

    private fun AreaDto.toModel() = AreaData(
        id = id, name = name, regionId = regionId, type = type,
        connections = connections,
        encounters = AreaEncounters(
            grass = encounters.grass.map { EncounterEntry(it.creatureId, it.minLevel, it.maxLevel, it.weight) },
            water = encounters.water.map { EncounterEntry(it.creatureId, it.minLevel, it.maxLevel, it.weight) },
            cave = encounters.cave.map { EncounterEntry(it.creatureId, it.minLevel, it.maxLevel, it.weight) }
        ),
        trainers = trainers, events = events,
        shopId = shopId, healingCenter = healingCenter, pc = pc
    )

    private fun TrainerDto.toModel() = TrainerData(
        id = id, name = name,
        trainerClass = TrainerClass.valueOf(trainerClass),
        areaId = areaId, reward = reward,
        team = team.map { TrainerTeamEntry(it.creatureId, it.level, it.moves) },
        aiProfile = runCatching { AIProfile.valueOf(aiProfile) }.getOrDefault(AIProfile.BASIC),
        badgeId = badgeId,
        preBattleText = preBattleText, postBattleText = postBattleText,
        repeatable = repeatable, items = items
    )

    private fun RegionDto.toModel() = RegionData(
        id = id, name = name, areas = areas,
        startAreaId = startAreaId, gymIds = gymIds, eliteFourId = eliteFourId
    )

    private fun EventDto.toModel() = GameEvent(
        id = id,
        trigger = EventTrigger(
            type = EventTriggerType.valueOf(trigger.type),
            areaId = trigger.areaId,
            x = trigger.x, y = trigger.y,
            npcId = trigger.npcId,
            flag = trigger.flag, flagValue = trigger.flagValue
        ),
        condition = condition?.let { EventCondition(it.flag, it.value) },
        notCondition = notCondition?.let { EventCondition(it.flag, it.value) },
        actions = actions.map { a ->
            EventAction(
                type = EventActionType.valueOf(a.type),
                npcId = a.npcId, trainerId = a.trainerId,
                flag = a.flag, flagValue = a.flagValue,
                itemId = a.itemId, qty = a.qty,
                creatureId = a.creatureId, creatureLevel = a.creatureLevel,
                warpAreaId = a.warpAreaId, warpX = a.warpX, warpY = a.warpY,
                text = a.text, areaId = a.areaId
            )
        },
        repeatable = repeatable
    )

    private fun NpcDto.toModel() = NpcData(
        id = id, name = name, areaId = areaId,
        x = x, y = y, dialogues = dialogues, trainerId = trainerId
    )
}
