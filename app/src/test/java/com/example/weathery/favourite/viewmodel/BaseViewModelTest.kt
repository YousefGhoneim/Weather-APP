@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.weathery.favourite.viewmodel

import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@ExperimentalCoroutinesApi
abstract class BaseViewModelTest {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    open fun baseSetUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    open fun baseTearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
        unmockkAll()
    }
}
