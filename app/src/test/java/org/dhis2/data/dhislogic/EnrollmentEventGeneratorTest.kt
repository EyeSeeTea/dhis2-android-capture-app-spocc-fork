package org.dhis2.data.dhislogic

import org.dhis2.commons.Constants
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.GregorianCalendar

class EnrollmentEventGeneratorTest {

    private val eventGeneratorRepository: EnrollmentEventGeneratorRepository = mock()
    private val eventGenerator = EnrollmentEventGenerator(eventGeneratorRepository)

    @Test
    fun `Should autogenerate events based on enrollment date`() {
        mockEnrollment()
        mockAutogeneratedEvents(true)
        mockAddEvent()
        mockNoStageToOpen()

        val result = eventGenerator.generateEnrollmentEvents("enrollmentUid")
        val enrollmentDate = mockedEnrollment().enrollmentDate()!!
        verify(eventGeneratorRepository).setDueDate("eventUid", enrollmentDate, true)
        assertTrue(result.first == "enrollmentUid")
        assertTrue(result.second == null)
    }

    @Test
    fun `Should autogenerate events based on incident date`() {
        mockEnrollment()
        mockAutogeneratedEvents(false)
        mockAddEvent()
        mockNoStageToOpen()

        val result = eventGenerator.generateEnrollmentEvents("enrollmentUid")
        val incidentDate = mockedEnrollment().incidentDate()!!
        verify(eventGeneratorRepository).setDueDate("eventUid", incidentDate, true)
        assertTrue(result.first == "enrollmentUid")
        assertTrue(result.second == null)
    }

    @Test
    fun `Should open first stage in program if already exist`() {
        mockEnrollment()
        mockNoAutogeneratedEvents()
        mockUseFirstStage(Constants.ENROLLMENT_DATE)
        mockEventExist()
        mockEventToReturn()
        val result = eventGenerator.generateEnrollmentEvents("enrollmentUid")
        verify(eventGeneratorRepository, times(0)).addEvent(any(), any(), any(), any())
        assertTrue(result.first == "enrollmentUid")
        assertTrue(result.second == "eventUid")
    }

    @Test
    fun `Should generate by enrollment date and open first stage in program`() {
        mockEnrollment()
        mockNoAutogeneratedEvents()
        mockUseFirstStage(Constants.ENROLLMENT_DATE)
        mockEventDoesNotExist()
        mockAddEvent()
        mockEventToReturn()
        val result = eventGenerator.generateEnrollmentEvents("enrollmentUid")
        val enrollmentDate = mockedEnrollment().enrollmentDate()!!
        verify(eventGeneratorRepository).setEventDate("eventUid", enrollmentDate)
        assertTrue(result.first == "enrollmentUid")
        assertTrue(result.second == "eventUid")
    }

    @Test
    fun `Should generate by incident date and open first stage in program`() {
        mockEnrollment()
        mockNoAutogeneratedEvents()
        mockUseFirstStage(Constants.INCIDENT_DATE)
        mockEventDoesNotExist()
        mockAddEvent()
        mockEventToReturn()
        val result = eventGenerator.generateEnrollmentEvents("enrollmentUid")
        val incidentDate = mockedEnrollment().incidentDate()!!
        verify(eventGeneratorRepository).setEventDate("eventUid", incidentDate)
        assertTrue(result.first == "enrollmentUid")
        assertTrue(result.second == "eventUid")
    }

    private fun mockEnrollment() {
        whenever(eventGeneratorRepository.enrollment(any())) doReturn mockedEnrollment()
    }

    private fun mockNoAutogeneratedEvents() {
        whenever(
            eventGeneratorRepository.enrollmentAutogeneratedEvents(any(), any()),
        ) doReturn emptyList()
    }

    private fun mockAutogeneratedEvents(
        generatedByEnrollmentDate: Boolean,
        hideDueDate: Boolean = false,
        periodType: PeriodType? = null,
        minDaysFromStart: Int = 0,
    ) {
        whenever(
            eventGeneratorRepository.enrollmentAutogeneratedEvents(any(), any()),
        ) doReturn mockedAutogeneratedEvents(
            generatedByEnrollmentDate,
            hideDueDate,
            periodType,
            minDaysFromStart,
        )
    }

    private fun mockAddEvent() {
        whenever(eventGeneratorRepository.addEvent(any(), any(), any(), any())) doReturn "eventUid"
    }

    private fun mockEventToReturn() {
        whenever(eventGeneratorRepository.eventUidInEnrollment(any(), any())) doReturn "eventUid"
    }

    private fun mockNoStageToOpen() {
        whenever(
            eventGeneratorRepository.enrollmentProgram(any()),
        ) doReturn mockedProgram(false)
        whenever(eventGeneratorRepository.firstOpenAfterEnrollmentStage(any())) doReturn null
    }

    private fun mockEventExist() {
        whenever(
            eventGeneratorRepository.eventExistInEnrollment(any(), any()),
        ) doReturn true
    }

    private fun mockEventDoesNotExist() {
        whenever(
            eventGeneratorRepository.eventExistInEnrollment(any(), any()),
        ) doReturn false
    }

    private fun mockUseFirstStage(reportDateToUse: String) {
        whenever(
            eventGeneratorRepository.enrollmentProgram(any()),
        ) doReturn mockedProgram(true)
        whenever(
            eventGeneratorRepository.firstStagesInProgram(any()),
        ) doReturn mockedStageToOpen(reportDateToUse)
    }

    private fun mockedEnrollment() = Enrollment.builder()
        .uid("enrollmentUid")
        .enrollmentDate(
            GregorianCalendar(2019, 0, 9).time,
        )
        .incidentDate(
            GregorianCalendar(2019, 0, 1).time,
        )
        .program("programUid")
        .organisationUnit("enrollingOrgUnit")
        .build()

    private fun mockedProgram(useFirstStage: Boolean) = Program.builder()
        .uid("programUid")
        .useFirstStageDuringRegistration(useFirstStage)
        .build()

    private fun mockedAutogeneratedEvents(
        generatedByEnrollmentDate: Boolean,
        hideDueDate: Boolean = false,
        periodType: PeriodType? = null,
        minDaysFromStart: Int = 0,
    ) = listOf(
        ProgramStage.builder()
            .uid("stageUid")
            .generatedByEnrollmentDate(generatedByEnrollmentDate)
            .hideDueDate(hideDueDate)
            .periodType(periodType)
            .minDaysFromStart(minDaysFromStart)
            .build(),
    )

    private fun mockedStageToOpen(reportDateToUse: String) = ProgramStage.builder()
        .uid("stageUid")
        .reportDateToUse(reportDateToUse)
        .build()
}
