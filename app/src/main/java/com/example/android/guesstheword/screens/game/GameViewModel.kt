package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import timber.log.Timber

private  val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

enum class BuzzType(val pattern: LongArray) {
    CORRECT(CORRECT_BUZZ_PATTERN),
    GAME_OVER(GAME_OVER_BUZZ_PATTERN),
    COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
    NO_BUZZ(NO_BUZZ_PATTERN)
}

class GameViewModel : ViewModel() {
    companion object {
        // These represent different important times
        // This is when the game is over
        const val DONE = 0L
        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L
        // This is the total time of the game
        const val COUNTDOWN_TIME = 10000L
    }

    private val timer : CountDownTimer

    // The current word
    private val _word = MutableLiveData<String>()
    val word : LiveData<String>
        get() = _word

    // The current score
    private val _score = MutableLiveData<Int>()
    val score : LiveData<Int>
        get() = _score

    private val _gameFinishedEvent = MutableLiveData<Boolean>()
    val gameFinishedEvent : LiveData<Boolean>
        get() = _gameFinishedEvent

    private val _buzzEvent = MutableLiveData<BuzzType>()
    val buzzEvent: LiveData<BuzzType>
        get() = _buzzEvent

    private val _time = MutableLiveData<Long>()
    val time : LiveData<Long>
        get() = _time

    val timeString = Transformations.map(time) { time ->
        DateUtils.formatElapsedTime(time)
    }

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    init {
        Timber.i("init block")
        resetList()
        nextWord()
        _score.value = 0
        //_word.value = ""
        _gameFinishedEvent.value = false


        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                _time.value = millisUntilFinished / ONE_SECOND
                if (millisUntilFinished.toDouble() / COUNTDOWN_TIME.toDouble() <= 0.2)
                    _buzzEvent.value = BuzzType.COUNTDOWN_PANIC
            }

            override fun onFinish() {
                _buzzEvent.value = BuzzType.GAME_OVER
                _gameFinishedEvent.value = true
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        Timber.i("destroyed")
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            resetList()
        }
        _word.value = wordList.removeAt(0)
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = (_score.value)?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _buzzEvent.value = BuzzType.CORRECT
        _score.value = (_score.value)?.plus(1)
        nextWord()
    }

    fun onGameFinishCompleted() {
        _gameFinishedEvent.value = false
    }

    fun onBuzzEventCompleted() {
        _buzzEvent.value = BuzzType.NO_BUZZ
    }
}