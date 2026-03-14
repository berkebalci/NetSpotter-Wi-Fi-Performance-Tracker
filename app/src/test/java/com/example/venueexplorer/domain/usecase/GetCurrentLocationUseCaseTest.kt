package com.example.venueexplorer.domain.usecase

import android.location.Location
import com.example.venueexplorer.domain.repository.LocationRepository
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
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
        // Log sınıfını mock'la (statik olduğu için mockkStatic kullanılır)
        mockkStatic(android.util.Log::class)
        every { android.util.Log.v(any(), any()) } returns 0
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
    }

    @Test
    fun `permission verilmedigi zaman direkt null dondur`() = runTest {
        every { repository.hasLocationPermission() } returns false

        val result = useCase()
        assertNull(result)
        verify(exactly = 1) { repository.hasLocationPermission() }
        verify(exactly = 0) { repository.getCurrentLocation() }
    }

    @Test
    fun `permission verildigi zaman getCurrentLocation cagrilir`() = runTest {
        // 1. ARRANGE
        val mockLocation = mockk<Location>(relaxed = true)
        val mockTask = mockk<Task<Location>>()

        every { repository.hasLocationPermission() } returns true
        every { repository.getCurrentLocation() } returns mockTask

        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = arg<OnSuccessListener<Location>>(0)
            listener.onSuccess(mockLocation)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask

        // 2. ACT
        val result = useCase()

        // 3. ASSERT
        assertNotNull(result)
        assertEquals(mockLocation, result)

        verify(exactly = 1) { repository.getCurrentLocation() }
        verify(exactly = 0) { repository.requestSingleLocationUpdate(any(), any()) }
    }

    @Test
    fun `getCurrentLocation null donerse requestSingleLocationUpdate cagrilir`() = runTest {
        val mockLocation = mockk<Location>(relaxed = true)
        val mockTask = mockk<Task<Location>>() // MockTask kullanıyoruz

        every { repository.hasLocationPermission() } returns true
        every { repository.getCurrentLocation() } returns mockTask

        // getCurrentLocation null dönsün (onSuccess'e null geçiyoruz)
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = arg<OnSuccessListener<Location>>(0)
            listener.onSuccess(null)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask

        every {
            repository.requestSingleLocationUpdate(
                onLocationReceived = any(),
                timeoutMillis = any()
            )
        }.answers {
            val callback_param = arg<(Location?) -> Unit>(0)
            callback_param.invoke(mockLocation)
        }

        val result = useCase()

        assertNotNull(result)
        assertEquals(mockLocation, result)

        verify(exactly = 1) { repository.getCurrentLocation() }
        verify(exactly = 1) { repository.requestSingleLocationUpdate(any(), any()) }
        verify(exactly = 0) { repository.getLastLocation() }
    }

    @Test
    fun `getCurrentLocation ve requestSingleLocationUpdate null donerse getLastLocation cagrilir`() = runTest {
        val mockLocation = mockk<Location>(relaxed = true)
        val mockTask1 = mockk<Task<Location>>()
        val mockTask2 = mockk<Task<Location?>>()

        every { repository.hasLocationPermission() } returns true

        // 1. getCurrentLocation -> null
        every { repository.getCurrentLocation() } returns mockTask1
        every { mockTask1.addOnSuccessListener(any()) } answers {
            val listener = arg<OnSuccessListener<Location>>(0)
            listener.onSuccess(null)
            mockTask1
        }
        every { mockTask1.addOnFailureListener(any()) } returns mockTask1

        // 2. requestSingleLocationUpdate -> null
        every {
            repository.requestSingleLocationUpdate(any(), any())
        } answers {
            val callback = arg<(Location?) -> Unit>(0)
            callback.invoke(null)
        }

        // 3. getLastLocation -> mockLocation
        every { repository.getLastLocation() } returns mockTask2
        every { mockTask2.addOnSuccessListener(any()) } answers {
            val listener = arg<OnSuccessListener<Location?>>(0)
            listener.onSuccess(mockLocation)
            mockTask2
        }
        every { mockTask2.addOnFailureListener(any()) } returns mockTask2

        val result = useCase()

        assertEquals(mockLocation, result)
        verify(exactly = 1) { repository.getLastLocation() }
    }
}
