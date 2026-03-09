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
        verify(exactly = 1){repository.hasLocationPermission()}
        verify(exactly = 0) { repository.getCurrentLocation() }
    }
    @Test
    fun `permission verildigi zaman getCurrentLocation cagrilir`() = runTest {
        // 1. ARRANGE
        val mockLocation = mockk<Location>(relaxed = true)
        val mockTask = mockk<Task<Location>>()

        every { repository.hasLocationPermission() } returns true
        every { repository.getCurrentLocation() } returns mockTask

        // DÜZELTME: any() parantez içine alındı, firstArg yerine arg(0) kullanıldı
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = arg<OnSuccessListener<Location>>(0)
            listener.onSuccess(mockLocation)
            mockTask
        }

        // DÜZELTME: any() parantez içine alındı
        every { mockTask.addOnFailureListener(any()) } returns mockTask

        // 2. ACT
        val result = useCase()

        // 3. ASSERT
        assertNotNull(result)
        assertEquals(mockLocation, result)

        verify(exactly = 1) { repository.getCurrentLocation() }
        verify(exactly = 0) { repository.requestSingleLocationUpdate(any(), any()) }
    }







}