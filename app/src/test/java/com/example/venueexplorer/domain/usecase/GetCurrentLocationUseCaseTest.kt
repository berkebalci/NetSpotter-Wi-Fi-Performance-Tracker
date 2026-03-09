package com.example.venueexplorer.domain.usecase
import io.mockk.*
import org.junit.Assert.*
import android.location.Location
import com.example.venueexplorer.domain.repository.LocationRepository
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetCurrentLocationUseCaseTest {
    private lateinit var useCase: GetCurrentLocationUseCase
    private lateinit var repository: LocationRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetCurrentLocationUseCase(repository)
    }


    @Test
    fun `permission verilmedigi zaman direkt null dondur`() = runTest {
        every { repository.hasLocationPermission() } returns false

        val result = useCase()
        assertNull(result)
        verify(exactly = 1){repository.hasLocationPermission()}
        verify(exactly = 0) { repository.getCurrentLocation() }
    }
    @Test
    fun `permission verildigi zaman getCurrentLocation cagrilir`() = runTest {
        every { repository.hasLocationPermission() } returns true
        val result = useCase()

        assertNotNull(result)

        val mockLocation = mockk<Location>(relaxed = true)
        val mockTask = mockk<Task<Location>>()

        every { repository.getCurrentLocation() } returns mockTask
        every{mockTask.addOnSuccessListener { any()}} answers {
            //any() dedigimiz zaman herhangi bir parametre gelmesi task'e bizim icin problem olmuyor
            //answers dedigimiz zaman da test ederken istedigimiz bir kod blogunu calistirmamiza olanak sagliyor
            val listener = firstArg<OnSuccessListener<Location>>()
            listener.onSuccess(mockLocation)
            mockTask

        }
        every {mockTask.addOnFailureListener {any()}} returns mockTask
    }





}