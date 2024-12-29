package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    // Game UI state
    // A StateFlow can be exposed from the GameUiState so that
    // the composables can listen for UI state updates and make the screen state
    // survive configuration changes

    // Set of words used in the game
    private var usedWords: MutableSet<String> = mutableSetOf()

    // GameUiState is passed to GameScreen by GameViewModel
    // Backing property to avoid state updates from other classes

    // StateFlow is a data holder observable flow that emits the current and new state updates.
    // Its value property reflects the current state value. To update state and send it
    // to the flow, assign a new value to the value property of the MutableStateFlow class.

    // In coroutines, a flow is a type that can emit multiple values sequentially,
    // as opposed to suspend functions that return only a single value.

// StateFlow<GameUiState> — это поток, который содержит объект типа GameUiState.
// Этот поток предоставляет текущее состояние игры (счёт, текущее слово,
// количество слов и т.д.) и обновляет UI, когда эти данные изменяются.

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var currentWord: String

    var userGuess by mutableStateOf("")
        private set

    // The init block is a special block of code that is used to initialize an object.
    // It is executed immediately after the primary constructor of the class is called.
    init {
        resetGame()
    }

    private fun pickRandomWordAndShuffle(): String {
        // Continue picking up a new random word until you get one
        // that hasn't been used before
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        tempWord.shuffle()

        while (String(tempWord).equals(word))
            tempWord.shuffle()

        return String(tempWord)
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // User's guess is correct, increase the score
            // and call updateGameState() to prepare the game for next round
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            // User's guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

// _uiState.value — это текущие данные внутри потока MutableStateFlow.
// Когда ты присваиваешь ему новое значение через .value = ...,
// поток автоматически обновляется.

// .copy() — это удобный способ обновить только определённые поля,
// не меняя остальные. Например, ты обновляешь только слово,
// счёт и количество слов, но не трогаешь флаг isGameOver.

    private fun updateGameState(updatedScore: Int) {
        if(usedWords.size == MAX_NO_OF_WORDS){
            //Last round in the game, update isGameOver to true, don't pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            // Normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    currentWordCount = currentState.currentWordCount.inc()
                )
            }
        }
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }
}






