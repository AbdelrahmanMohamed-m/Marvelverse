package com.example.marvelverse.data.repositories

import com.example.marvelverse.app.ui.home.HomeItem
import com.example.marvelverse.data.dataSources.local.FakeLocalData
import com.example.marvelverse.data.dataSources.local.MarvelDatabase
import com.example.marvelverse.data.dataSources.remote.MarvelApiServices
import com.example.marvelverse.data.dataSources.remote.reponses.CharacterDto
import com.example.marvelverse.data.dataSources.remote.reponses.ComicDto
import com.example.marvelverse.data.dataSources.remote.reponses.EventDto
import com.example.marvelverse.data.dataSources.remote.reponses.SeriesDto
import com.example.marvelverse.domain.entities.Character
import com.example.marvelverse.domain.entities.Comic
import com.example.marvelverse.domain.entities.Event
import com.example.marvelverse.domain.entities.Series
import com.example.marvelverse.domain.mapper.CharacterMapper
import com.example.marvelverse.domain.mapper.CharacterSearchEntityToCharacterMapper
import com.example.marvelverse.domain.mapper.CharacterToCharacterSearchEntityMapper
import com.example.marvelverse.domain.mapper.ComicMapper
import com.example.marvelverse.domain.mapper.ComicSearchEntityToComicMapper
import com.example.marvelverse.domain.mapper.ComicToComicSearchEntityMapper
import com.example.marvelverse.domain.mapper.EventMapper
import com.example.marvelverse.domain.mapper.EventSearchEntityToEventMapper
import com.example.marvelverse.domain.mapper.EventToEventSearchEntityMapper
import com.example.marvelverse.domain.mapper.SeriesMapper
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject


class MarvelRepository @Inject constructor(
	private val marvelApiServices: MarvelApiServices,
	private val characterMapper: CharacterMapper,
	private val comicMapper: ComicMapper,
	private val eventMapper: EventMapper,
	private val seriesMapper: SeriesMapper,
	private val charToCharSearchEntityMapper: CharacterToCharacterSearchEntityMapper,
	private val charSearchEntityToCharMapper: CharacterSearchEntityToCharacterMapper,
	private val comicToComicSearchEntityMapper: ComicToComicSearchEntityMapper,
	private val comicSearchEntityToComicMapper: ComicSearchEntityToComicMapper,
	private val eventToEventSearchEntity: EventToEventSearchEntityMapper,
	private val eventSearchEntityToEventMapper: EventSearchEntityToEventMapper,
	private val fakeLocalData: FakeLocalData
) {

	private val disposables = CompositeDisposable()

	// TODO: Remove
	lateinit var db: MarvelDatabase

	fun searchCharacters(limit: Int? = null, title: String? = null): Single<List<Character>> {
		return marvelApiServices.fetchCharacters(limit, title)
			.map { baseResponse ->
				baseResponse.data?.results?.map { characterDto ->
					characterDto.mapToCharacter()
				} ?: emptyList()
			}
	}

	fun searchCacheCharacters(limit: Int? = null, name: String): Single<List<Character>> {
		val savedCharacters = getCachedCharacters(name)

		return if (savedCharacters.isNotEmpty()) {
			Single.just(savedCharacters)
		} else {
			try {
				val response = searchCharacters(limit, name)
				cacheCharacters(response)
				response
			} catch (e: Exception) {
				Single.just(emptyList())
			}
		}
	}

	private fun getCachedCharacters(name: String): List<Character> {
		val savedEntities = db.searchDao.getAllCharacters(name).blockingGet()
		return savedEntities.map { charSearchEntityToCharMapper.map(it) }
	}

	private fun cacheCharacters(characters: Single<List<Character>>) {
		val cachedResponse = characters.blockingGet().map { charToCharSearchEntityMapper.map(it) }
		db.searchDao.insertCharacters(cachedResponse).subscribe()
	}

	fun searchComics(limit: Int? = null, title: String? = null): Single<List<Comic>> {
		return marvelApiServices.fetchComics(limit, title)
			.map { baseResponse ->
				baseResponse.data?.results?.map { comicDto ->
					comicDto.mapToComic()
				} ?: emptyList()
			}
	}

	fun searchCachedComics(limit: Int? = null, title: String): Single<List<Comic>> {
		val savedComics = getCachedComics(title)

		return if (savedComics.isNotEmpty()) {
			Single.just(savedComics)
		} else {
			try {
				val response = searchComics(limit, title)
				cacheComics(response)
				response
			} catch (e: Exception) {
				Single.just(emptyList())
			}
		}
	}

	private fun getCachedComics(title: String): List<Comic> {
		val savedEntities = db.searchDao.getAllComics(title).blockingGet()
		return savedEntities.map { comicSearchEntityToComicMapper.map(it) }
	}

	private fun cacheComics(comics: Single<List<Comic>>) {
		val cachedResponse = comics.blockingGet().map { comicToComicSearchEntityMapper.map(it) }
		db.searchDao.insertComics(cachedResponse).subscribe()
	}

	fun searchEvents(limit: Int? = null, title: String? = null): Single<List<Event>> {
		return marvelApiServices.fetchEvents(limit, title)
			.map { baseResponse ->
				baseResponse.data?.results?.map { eventDto ->
					eventDto.mapToEvent()
				} ?: emptyList()
			}
	}

	fun searchCachedEvents(limit: Int? = null, title: String): Single<List<Event>> {
		val savedEvents = getCachedEvents(title)

		return if (savedEvents.isNotEmpty()) {
			Single.just(savedEvents)
		} else {
			try {
				val response = searchEvents(limit, title)
				cacheEvents(response)
				response
			} catch (e: Exception) {
				Single.just(emptyList())
			}
		}
	}

	private fun getCachedEvents(title: String): List<Event> {
		val savedEntities = db.searchDao.getAllEvents(title).blockingGet()
		return savedEntities.map { eventSearchEntityToEventMapper.map(it) }
	}

	private fun cacheEvents(events: Single<List<Event>>) {
		val cachedResponse = events.blockingGet().map { eventToEventSearchEntity.map(it) }
		db.searchDao.insertEvents(cachedResponse).subscribe()
	}


	fun searchSeries(limit: Int? = null, title: String? = null): Single<List<Series>> {
		return marvelApiServices.fetchSeries(limit, title).map { baseResponse ->
			baseResponse.data?.results?.map { seriesDto ->
				seriesDto.mapToSeries()
			} ?: emptyList()
		}
	}

	fun getComicsByUrl(url: String): Single<List<Comic>> {
		return marvelApiServices.fetchComicsByUrl(url).map { baseResponse ->
			baseResponse.data?.results?.map { comicDto ->
				comicDto.mapToComic()
			} ?: emptyList()
		}
	}

	fun getSeriesByUrl(url: String): Single<List<Series>> {
		return marvelApiServices.fetchSeriesByUrl(url).map { baseResponse ->
			baseResponse.data?.results?.map { seriesDto ->
				seriesDto.mapToSeries()
			} ?: emptyList()
		}
	}

	fun getCharactersByUrl(url: String): Single<List<Character>> {
		return marvelApiServices.fetchCharactersByUrl(url).map { baseResponse ->
			baseResponse.data?.results?.map { characterDto ->
				characterDto.mapToCharacter()
			} ?: emptyList()
		}
	}

	fun getEventsByUrl(url: String): Single<List<Event>> {
		return marvelApiServices.fetchEventsByUrl(url).map { baseResponse ->
			baseResponse.data?.results?.map { eventDto ->
				eventDto.mapToEvent()
			} ?: emptyList()
		}
	}

	fun getRandomCharacters(): Single<List<Character>> {
		return marvelApiServices.fetchCharacters(80, null).map { baseResponse ->
			baseResponse.data?.results?.shuffled()?.take(20)?.map { characterDto ->
				characterDto.mapToCharacter()
			} ?: emptyList()
		}
	}

	fun getRandomComics(): Single<List<Comic>> {
		return marvelApiServices.fetchComics(50, null).map { baseResponse ->
			baseResponse.data?.results?.shuffled()?.take(10)?.map { comicDto ->
				comicDto.mapToComic()
			} ?: emptyList()
		}
	}

	fun getRandomSeries(): Single<List<Series>> {
		return marvelApiServices.fetchSeries(50, null).map { baseResponse ->
			baseResponse.data?.results?.shuffled()?.take(10)?.map { seriesDto ->
				seriesDto.mapToSeries()
			} ?: emptyList()
		}
	}

	fun getRandomEvents(): Single<List<Event>> {
		return marvelApiServices.fetchEvents(50, null).map { baseResponse ->
			baseResponse.data?.results?.shuffled()?.take(10)?.map { eventDto ->
				eventDto.mapToEvent()
			} ?: emptyList()
		}
	}

	fun getHomeItems(): Single<List<HomeItem>> {
		return Single.zip(
			getRandomCharacters(),
			getRandomComics(),
			getRandomSeries(),
			getRandomEvents()
		) { characters, comics, series, events ->
			listOf(
				HomeItem.CharactersItem(characters),
				HomeItem.ComicsItem(comics),
				HomeItem.EventsItem(events),
				HomeItem.SeriesItem(series)
			)
		}
	}

	fun getItems() = fakeLocalData.getAboutItems()
	private fun ComicDto.mapToComic(): Comic = comicMapper.map(this)

	private fun SeriesDto.mapToSeries(): Series = seriesMapper.map(this)

	private fun CharacterDto.mapToCharacter(): Character = characterMapper.map(this)
	private fun EventDto.mapToEvent(): Event = eventMapper.map(this)

	fun clearDisposables() {
		disposables.clear()
	}


}