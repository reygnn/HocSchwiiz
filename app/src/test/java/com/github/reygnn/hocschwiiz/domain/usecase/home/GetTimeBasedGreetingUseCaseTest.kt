package com.github.reygnn.hocschwiiz.domain.usecase.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class GetTimeBasedGreetingUseCaseTest {

    private val useCase = GetTimeBasedGreetingUseCase()

    @Test
    fun `returns Guete Morge for early morning 5am`() {
        assertEquals("Guete Morge!", useCase(LocalTime.of(5, 0)))
    }

    @Test
    fun `returns Guete Morge for mid morning 9am`() {
        assertEquals("Guete Morge!", useCase(LocalTime.of(9, 0)))
    }

    @Test
    fun `returns Guete Morge for late morning 11-59am`() {
        assertEquals("Guete Morge!", useCase(LocalTime.of(11, 59)))
    }

    @Test
    fun `returns Guete Tag for noon 12pm`() {
        assertEquals("Guete Tag!", useCase(LocalTime.of(12, 0)))
    }

    @Test
    fun `returns Guete Tag for afternoon 3pm`() {
        assertEquals("Guete Tag!", useCase(LocalTime.of(15, 0)))
    }

    @Test
    fun `returns Guete Tag for late afternoon 5-59pm`() {
        assertEquals("Guete Tag!", useCase(LocalTime.of(17, 59)))
    }

    @Test
    fun `returns Guete Aabig for evening 6pm`() {
        assertEquals("Guete Aabig!", useCase(LocalTime.of(18, 0)))
    }

    @Test
    fun `returns Guete Aabig for night 11pm`() {
        assertEquals("Guete Aabig!", useCase(LocalTime.of(23, 0)))
    }

    @Test
    fun `returns Guete Aabig for midnight`() {
        assertEquals("Guete Aabig!", useCase(LocalTime.of(0, 0)))
    }

    @Test
    fun `returns Guete Aabig for early night 4am`() {
        assertEquals("Guete Aabig!", useCase(LocalTime.of(4, 0)))
    }

    @Test
    fun `returns Guete Aabig for 4-59am`() {
        assertEquals("Guete Aabig!", useCase(LocalTime.of(4, 59)))
    }

    @Test
    fun `boundary test - 4-59 is evening, 5-00 is morning`() {
        assertEquals("Guete Aabig!", useCase(LocalTime.of(4, 59)))
        assertEquals("Guete Morge!", useCase(LocalTime.of(5, 0)))
    }

    @Test
    fun `boundary test - 11-59 is morning, 12-00 is day`() {
        assertEquals("Guete Morge!", useCase(LocalTime.of(11, 59)))
        assertEquals("Guete Tag!", useCase(LocalTime.of(12, 0)))
    }

    @Test
    fun `boundary test - 17-59 is day, 18-00 is evening`() {
        assertEquals("Guete Tag!", useCase(LocalTime.of(17, 59)))
        assertEquals("Guete Aabig!", useCase(LocalTime.of(18, 0)))
    }
}