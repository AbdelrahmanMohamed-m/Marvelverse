package com.example.marvelverse.app.ui.comics.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.marvelverse.DataState
import com.example.marvelverse.app.ui.home.interfaces.CharacterInteractionListener
import com.example.marvelverse.app.ui.home.interfaces.EventInteractionListener
import com.example.marvelverse.data.repositories.MarvelRepository
import com.example.marvelverse.domain.entities.main.Character
import com.example.marvelverse.domain.entities.main.Event
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ComicsDetailsViewModel : ViewModel() , CharacterInteractionListener,
    EventInteractionListener {
    private val compositeDisposable = CompositeDisposable()
    private var _character = MutableLiveData<List<Character>>()
    private var _event= MutableLiveData<List<Event>>()
    private  var _comicsDetailsEvent=MutableLiveData<ComicDetailsEvents>()
    val character: LiveData<List<Character>> get() = _character
    val event: LiveData<List<Event>> get() =_event
    val comicsDetailsEvent:LiveData<ComicDetailsEvents> get() =_comicsDetailsEvent
    fun getCharacter(url:String) {
        MarvelRepository.getCharactersByUrl(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _character.postValue(it)
                },
                {
                }).addTo(compositeDisposable)
    }
    fun getEvent(url:String) {
        MarvelRepository.getEventsByUrl(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                  _event.postValue(it)
                },
                {
                }).addTo(compositeDisposable)
    }

   private fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
        compositeDisposable.add(this)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    override fun onCharacterClick(character: Character) {
        _comicsDetailsEvent.postValue(ComicDetailsEvents.ClickCharacterEvent(character))

    }

    override fun onEventClick(event: Event) {
        _comicsDetailsEvent.postValue(ComicDetailsEvents.ClickEventEvent(event))


    }
}