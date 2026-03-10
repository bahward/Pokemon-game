package com.monstergame.di

import android.content.Context
import com.monstergame.content.loader.ContentLoader
import com.monstergame.content.loader.ContentRepository
import com.monstergame.engine.persistence.SaveManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContentLoader(): ContentLoader = ContentLoader()

    @Provides
    @Singleton
    fun provideContentRepository(
        @ApplicationContext context: Context,
        loader: ContentLoader
    ): ContentRepository {
        val repo = ContentRepository()
        val assets = context.assets

        fun loadAsset(path: String): String? = runCatching {
            assets.open(path).bufferedReader().readText()
        }.getOrNull()

        // Moves
        loadAsset("content/moves/moves.json")?.let {
            repo.addMoves(loader.loadMoves(it))
        }

        // Creatures — one file per region
        listOf("kanto", "johto", "hoenn").forEach { region ->
            loadAsset("content/creatures/$region.json")?.let {
                repo.addCreatures(loader.loadCreatures(it))
            }
        }

        // Items
        loadAsset("content/items/items.json")?.let {
            repo.addItems(loader.loadItems(it))
        }

        // Regions
        listOf("kanto", "johto", "hoenn").forEach { region ->
            loadAsset("content/regions/$region.json")?.let {
                repo.addRegion(loader.loadRegion(it))
            }
        }

        // Areas — in content/areas/ folder
        listOf("kanto", "johto", "hoenn").forEach { region ->
            loadAsset("content/areas/${region}_areas.json")?.let {
                repo.addAreas(loader.loadAreas(it))
            }
        }

        // Trainers
        listOf("kanto", "johto", "hoenn").forEach { region ->
            loadAsset("content/trainers/${region}_trainers.json")?.let {
                repo.addTrainers(loader.loadTrainers(it))
            }
        }

        // Events — single combined file
        loadAsset("content/events/events.json")?.let {
            repo.addEvents(loader.loadEvents(it))
        }

        // NPCs — single combined file
        loadAsset("content/npcs/npcs.json")?.let {
            repo.addNpcs(loader.loadNpcs(it))
        }

        // Shops
        loadAsset("content/shops/shops.json")?.let {
            repo.addShops(loader.loadShops(it))
        }

        return repo
    }

    @Provides
    @Singleton
    fun provideSaveManager(@ApplicationContext context: Context): SaveManager =
        SaveManager(File(context.filesDir, "saves"))
}
