package com.team.notify

import com.team.notify.fake.FakeNotifyRepository
import com.team.notify.ui.spaces.SpaceViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpaceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addSpace_addsToSpacesFlow() = runTest {
        val repo = FakeNotifyRepository()
        val vm = SpaceViewModel(repository = repo)

        vm.addSpace(name = "My Space")
        advanceUntilIdle()

        val spaces = vm.spaces.value
        assertEquals(1, spaces.size)
        assertEquals("My Space", spaces.first().name)
    }
}
