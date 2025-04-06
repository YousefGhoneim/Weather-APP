@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.weathery.favourite.viewmodel

import com.example.weathery.data.local.CityEntity
import com.example.weathery.data.models.UiState
import com.example.weathery.data.repo.IWeatherRepository
import com.example.weathery.favourite.FavoritesViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class FavoritesViewModelTest {

    private lateinit var repo: IWeatherRepository
    private lateinit var viewModel: FavoritesViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        coEvery { repo.getFavoriteCities() } returns flowOf(emptyList())
        viewModel = FavoritesViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun favoritesViewModel_should_emit_Empty_on_startup_when_repo_returns_empty() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.favoritesState.value is UiState.Empty)
    }

    @Test
    fun favoritesViewModel_should_emit_Success_after_saving_city() = runTest {
        val city = CityEntity("Cairo", 30.0, 31.0)
        coEvery { repo.saveCity(city) } returns Unit
        coEvery { repo.getFavoriteCities() } returns flowOf(listOf(city))

        viewModel.saveCity(city)
        advanceUntilIdle()

        val state = viewModel.favoritesState.value
        assertTrue(state is UiState.Success)
        assertEquals("Cairo", (state as UiState.Success).data.first().name)

        coVerify { repo.saveCity(city) }
    }

    @Test
    fun favoritesViewModel_should_emit_Empty_after_deleting_last_city() = runTest {
        val city = CityEntity("Riyadh", 24.7, 46.7)
        coEvery { repo.deleteCity(city) } returns Unit
        coEvery { repo.getFavoriteCities() } returns flowOf(emptyList())

        viewModel.deleteCity(city)
        advanceUntilIdle()

        val state = viewModel.favoritesState.value
        assertTrue(state is UiState.Empty)

        coVerify { repo.deleteCity(city) }
    }
}
