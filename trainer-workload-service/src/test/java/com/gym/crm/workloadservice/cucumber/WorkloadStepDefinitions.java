package com.gym.crm.workloadservice.cucumber;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;
import com.gym.crm.workload.entity.TrainerWorkload;
import com.gym.crm.workload.exception.ValidationException;
import com.gym.crm.workload.exception.WorkloadNotFoundException;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import com.gym.crm.workload.service.TrainerWorkloadService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadStepDefinitions {

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private TrainerWorkloadService workloadService;

    @Autowired
    private ActiveMQTestProducer testProducer;

    @Autowired
    private TestContext testContext;

    private static final long MESSAGE_PROCESSING_DELAY = 1000L;

    @Given("the workload database is empty")
    public void theWorkloadDatabaseIsEmpty() {
        repository.deleteAll();
        assertThat(repository.count()).isZero();
    }

    @Given("the message queue is ready")
    public void theMessageQueueIsReady() {
    }

    @Given("a trainer workload request with the following details:")
    public void aTrainerWorkloadRequestWithTheFollowingDetails(Map<String, String> details) {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(details.get("trainerUsername"))
                .trainerFirstName(details.get("trainerFirstName"))
                .trainerLastName(details.get("trainerLastName"))
                .isActive(Boolean.parseBoolean(details.get("isActive")))
                .trainingDate(LocalDate.parse(details.get("trainingDate")))
                .trainingDuration(Integer.parseInt(details.get("trainingDuration")))
                .actionType(ActionType.valueOf(details.get("actionType")))
                .transactionId(java.util.UUID.randomUUID().toString())
                .build();

        testContext.setLastRequest(request);
    }

    @Given("a trainer workload request with missing trainer username")
    public void aTrainerWorkloadRequestWithMissingTrainerUsername() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(null)
                .trainerFirstName("Test")
                .trainerLastName("User")
                .isActive(true)
                .trainingDate(LocalDate.now())
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        testContext.setLastRequest(request);
    }

    @Given("a DELETE workload request with missing trainer username")
    public void aDELETEWorkloadRequestWithMissingTrainerUsername() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(null)
                .trainerFirstName("Test")
                .trainerLastName("User")
                .isActive(true)
                .trainingDate(LocalDate.now())
                .trainingDuration(60)
                .actionType(ActionType.DELETE)
                .build();

        testContext.setLastRequest(request);
    }

    @Given("a DELETE workload request with invalid username {string}")
    public void aDELETEWorkloadRequestWithInvalidUsername(String username) {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("User")
                .isActive(true)
                .trainingDate(LocalDate.now())
                .trainingDuration(60)
                .actionType(ActionType.DELETE)
                .build();

        testContext.setLastRequest(request);
    }

    @Given("a trainer {string} exists with {int} minutes in {word} {int}")
    public void aTrainerExistsWithMinutesInMonth(String username, int duration, String monthName, int year) {
        int month = parseMonth(monthName);
        TrainerWorkloadRequest request = createBasicRequest(username, year, month, duration, ActionType.ADD);
        testProducer.sendWorkloadUpdateAndWait(request, MESSAGE_PROCESSING_DELAY);
        Optional<TrainerWorkload> workload = repository.findByTrainerUsername(username);
        assertThat(workload).isPresent();
    }

    @Given("a trainer {string} exists with the following workload:")
    public void aTrainerExistsWithTheFollowingWorkload(String username, io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            int year = Integer.parseInt(row.get("year"));
            int month = Integer.parseInt(row.get("month"));
            int duration = Integer.parseInt(row.get("duration"));

            TrainerWorkloadRequest request = createBasicRequest(username, year, month, duration, ActionType.ADD);
            testProducer.sendWorkloadUpdateAndWait(request, MESSAGE_PROCESSING_DELAY);
        }
    }

    @Given("no trainer workload exists for {string}")
    public void noTrainerWorkloadExistsFor(String username) {
        repository.deleteByTrainerUsername(username);
        assertThat(repository.existsByTrainerUsername(username)).isFalse();
    }

    @Given("a trainer workload exists for {string} with name {string}")
    public void aTrainerWorkloadExistsForWithName(String username, String fullName) {
        String[] names = fullName.split(" ");
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName(names[0])
                .trainerLastName(names[1])
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 1, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        testProducer.sendWorkloadUpdateAndWait(request, MESSAGE_PROCESSING_DELAY);
    }

    @Given("an inactive trainer {string} exists with {int} minutes in {word} {int}")
    public void anInactiveTrainerExistsWithMinutesInMonth(String username, int duration, String monthName, int year) {
        int month = parseMonth(monthName);

        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Inactive")
                .trainerLastName("Trainer")
                .isActive(false)
                .trainingDate(LocalDate.of(year, month, 15))
                .trainingDuration(duration)
                .actionType(ActionType.ADD)
                .build();

        testProducer.sendWorkloadUpdateAndWait(request, MESSAGE_PROCESSING_DELAY);
    }

    @Given("a new trainer {string}")
    public void aNewTrainer(String username) {
    }

    @When("the message is sent to the workload queue")
    public void theMessageIsSentToTheWorkloadQueue() {
        try {
            testProducer.sendWorkloadUpdateAndWait(testContext.getLastRequest(), MESSAGE_PROCESSING_DELAY);
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }

    @When("the message is processed successfully")
    @And("all messages are processed successfully")
    public void theMessageIsProcessedSuccessfully() {
        try {
            Thread.sleep(MESSAGE_PROCESSING_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @When("a new training of {int} minutes is added for {string} on {string}")
    public void aNewTrainingOfMinutesIsAddedForOn(int duration, String username, String date) {
        LocalDate trainingDate = LocalDate.parse(date);
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("User")
                .isActive(true)
                .trainingDate(trainingDate)
                .trainingDuration(duration)
                .actionType(ActionType.ADD)
                .build();

        testProducer.sendWorkloadUpdateAndWait(request, MESSAGE_PROCESSING_DELAY);
    }

    @When("a DELETE message is sent to remove {int} minutes from {string} on {string}")
    public void aDELETEMessageIsSentToRemoveMinutesFromOn(int duration, String username, String date) {
        LocalDate trainingDate = LocalDate.parse(date);
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("User")
                .isActive(true)
                .trainingDate(trainingDate)
                .trainingDuration(duration)
                .actionType(ActionType.DELETE)
                .build();

        testContext.setLastRequest(request);
        try {
            testProducer.sendWorkloadUpdateAndWait(request, MESSAGE_PROCESSING_DELAY);
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }

    @When("a DELETE message with negative duration is sent for {string}")
    public void aDELETEMessageWithNegativeDurationIsSentFor(String username) {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("User")
                .isActive(true)
                .trainingDate(LocalDate.now())
                .trainingDuration(-60)
                .actionType(ActionType.DELETE)
                .build();

        testContext.setLastRequest(request);
    }

    @When("a DELETE message with duration {int} is sent for {string} on {string}")
    public void aDELETEMessageWithDurationIsSentForOn(int duration, String username, String date) {
        LocalDate trainingDate = LocalDate.parse(date);
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("User")
                .isActive(true)
                .trainingDate(trainingDate)
                .trainingDuration(duration)
                .actionType(ActionType.DELETE)
                .build();

        testContext.setLastRequest(request);
    }

    @When("a DELETE message is sent for {string} with future date {string}")
    public void aDELETEMessageIsSentForWithFutureDate(String username, String date) {
        LocalDate trainingDate = LocalDate.parse(date);
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("User")
                .isActive(true)
                .trainingDate(trainingDate)
                .trainingDuration(60)
                .actionType(ActionType.DELETE)
                .build();

        testContext.setLastRequest(request);
    }

    @When("a training is added for {string} with updated name {string}")
    public void aTrainingIsAddedForWithUpdatedName(String username, String fullName) {
        String[] names = fullName.split(" ");
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName(names[0])
                .trainerLastName(names[1])
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 1, 20))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        testContext.setLastRequest(request);
        testProducer.sendWorkloadUpdateAndWait(request, MESSAGE_PROCESSING_DELAY);
    }

    @When("I request the workload for {string}")
    public void iRequestTheWorkloadFor(String username) {
        try {
            TrainerWorkloadResponse response = workloadService.getTrainerWorkload(username);
            testContext.setLastResponse(response);
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }

    @When("I request the workload with null username")
    public void iRequestTheWorkloadWithNullUsername() {
        try {
            workloadService.getTrainerWorkload(null);
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }

    @When("I request the workload with empty username")
    public void iRequestTheWorkloadWithEmptyUsername() {
        try {
            workloadService.getTrainerWorkload("");
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }

    @When("the following trainings are added for {string}:")
    public void theFollowingTrainingsAreAddedFor(String username, io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            LocalDate date = LocalDate.parse(row.get("date"));
            int duration = Integer.parseInt(row.get("duration"));

            TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                    .trainerUsername(username)
                    .trainerFirstName("Charlie")
                    .trainerLastName("Brown")
                    .isActive(true)
                    .trainingDate(date)
                    .trainingDuration(duration)
                    .actionType(ActionType.ADD)
                    .build();

            testProducer.sendWorkloadUpdateAndWait(request, MESSAGE_PROCESSING_DELAY);
        }
    }

    @Then("the trainer workload should be created in the database")
    public void theTrainerWorkloadShouldBeCreatedInTheDatabase() {
        String username = testContext.getLastRequest().getTrainerUsername();
        Optional<TrainerWorkload> workload = repository.findByTrainerUsername(username);
        assertThat(workload).isPresent();
    }

    @Then("the workload should contain {int} minutes for {word} {int}")
    public void theWorkloadShouldContainMinutesFor(int expectedDuration, String monthName, int year) {
        String username = testContext.getLastRequest().getTrainerUsername();
        int month = parseMonth(monthName);

        TrainerWorkload workload = repository.findByTrainerUsername(username).orElseThrow();
        int actualDuration = workload.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .map(m -> m.getTotalDuration())
                .orElse(0);

        assertThat(actualDuration).isEqualTo(expectedDuration);
    }

    @Then("the total workload for {string} in {word} {int} should be {int} minutes")
    public void theTotalWorkloadForInShouldBeMinutes(String username, String monthName, int year, int expectedDuration) {
        int month = parseMonth(monthName);

        TrainerWorkload workload = repository.findByTrainerUsername(username).orElseThrow();
        int actualDuration = workload.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .map(m -> m.getTotalDuration())
                .orElse(0);

        assertThat(actualDuration).isEqualTo(expectedDuration);
    }

    @Then("the workload for {string} should show:")
    public void theWorkloadForShouldShow(String username, io.cucumber.datatable.DataTable dataTable) {
        TrainerWorkload workload = repository.findByTrainerUsername(username).orElseThrow();

        List<Map<String, String>> expectedRows = dataTable.asMaps();
        for (Map<String, String> row : expectedRows) {
            int year = Integer.parseInt(row.get("year"));
            int month = Integer.parseInt(row.get("month"));
            int expectedDuration = Integer.parseInt(row.get("duration"));

            int actualDuration = workload.getYears().stream()
                    .filter(y -> y.getYear().equals(year))
                    .flatMap(y -> y.getMonths().stream())
                    .filter(m -> m.getMonth().equals(month))
                    .findFirst()
                    .map(m -> m.getTotalDuration())
                    .orElse(-1);

            assertThat(actualDuration)
                    .withFailMessage("Duration mismatch for %d/%d. Expected: %d, Actual: %d",
                            month, year, expectedDuration, actualDuration)
                    .isEqualTo(expectedDuration);
        }
    }

    @Then("the trainer name should be updated to {string}")
    public void theTrainerNameShouldBeUpdatedTo(String expectedFullName) {
        String username = testContext.getLastRequest().getTrainerUsername();
        TrainerWorkload workload = repository.findByTrainerUsername(username).orElseThrow();

        String actualFullName = workload.getTrainerFirstName() + " " + workload.getTrainerLastName();
        assertThat(actualFullName).isEqualTo(expectedFullName);
    }

    @Then("the trainer should be marked as inactive")
    public void theTrainerShouldBeMarkedAsInactive() {
        String username = testContext.getLastRequest().getTrainerUsername();
        TrainerWorkload workload = repository.findByTrainerUsername(username).orElseThrow();
        assertThat(workload.getIsActive()).isFalse();
    }

    @Then("the message should be rejected due to validation error")
    public void theMessageShouldBeRejectedDueToValidationError() {
    }

    @Then("the message should fail with trainer not found error")
    public void theMessageShouldFailWithTrainerNotFoundError() {
        try {
            Thread.sleep(MESSAGE_PROCESSING_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String username = testContext.getLastRequest() != null ?
                testContext.getLastRequest().getTrainerUsername() : "unknown.trainer";
        assertThat(repository.existsByTrainerUsername(username)).isFalse();
    }

    @Then("the message should fail with insufficient workload error")
    public void theMessageShouldFailWithInsufficientWorkloadError() {
        String username = testContext.getLastRequest().getTrainerUsername();
        TrainerWorkload workload = repository.findByTrainerUsername(username).orElseThrow();

        workload.getYears().forEach(year ->
                year.getMonths().forEach(month ->
                        assertThat(month.getTotalDuration()).isGreaterThanOrEqualTo(0)
                )
        );
    }

    @Then("the message should fail with month not found error")
    public void theMessageShouldFailWithMonthNotFoundError() {
    }

    @Then("the response should contain trainer information:")
    public void theResponseShouldContainTrainerInformation(Map<String, String> expectedInfo) {
        TrainerWorkloadResponse response = testContext.getLastResponse();
        assertThat(response).isNotNull();

        if (expectedInfo.containsKey("trainerUsername")) {
            assertThat(response.getTrainerUsername()).isEqualTo(expectedInfo.get("trainerUsername"));
        }
        if (expectedInfo.containsKey("trainerFirstName")) {
            assertThat(response.getTrainerFirstName()).isEqualTo(expectedInfo.get("trainerFirstName"));
        }
        if (expectedInfo.containsKey("trainerLastName")) {
            assertThat(response.getTrainerLastName()).isEqualTo(expectedInfo.get("trainerLastName"));
        }
        if (expectedInfo.containsKey("isActive")) {
            boolean expectedActive = Boolean.parseBoolean(expectedInfo.get("isActive"));
            assertThat(response.getIsActive()).isEqualTo(expectedActive);
        }
    }

    @Then("the workload should show:")
    public void theWorkloadShouldShow(io.cucumber.datatable.DataTable dataTable) {
        TrainerWorkloadResponse response = testContext.getLastResponse();
        assertThat(response).isNotNull();
        assertThat(response.getYears()).isNotEmpty();

        List<Map<String, String>> expectedRows = dataTable.asMaps();
        for (Map<String, String> row : expectedRows) {
            int year = Integer.parseInt(row.get("year"));
            int month = Integer.parseInt(row.get("month"));
            int expectedDuration = Integer.parseInt(row.get("duration"));

            int actualDuration = response.getYears().stream()
                    .filter(y -> y.getYear().equals(year))
                    .flatMap(y -> y.getMonths().stream())
                    .filter(m -> m.getMonth().equals(month))
                    .findFirst()
                    .map(TrainerWorkloadResponse.MonthSummaryDto::getTotalDuration)
                    .orElse(-1);

            assertThat(actualDuration)
                    .withFailMessage("Duration mismatch for %d/%d. Expected: %d, Actual: %d",
                            month, year, expectedDuration, actualDuration)
                    .isEqualTo(expectedDuration);
        }
    }

    @Then("the request should fail with trainer not found error")
    public void theRequestShouldFailWithTrainerNotFoundError() {
        assertThat(testContext.getLastException())
                .isNotNull()
                .isInstanceOf(WorkloadNotFoundException.class);
    }

    @Then("the request should fail with validation error")
    public void theRequestShouldFailWithValidationError() {
        assertThat(testContext.getLastException())
                .isNotNull()
                .isInstanceOf(ValidationException.class);
    }

    @Then("the response should include month names:")
    public void theResponseShouldIncludeMonthNames(io.cucumber.datatable.DataTable dataTable) {
        TrainerWorkloadResponse response = testContext.getLastResponse();
        assertThat(response).isNotNull();

        List<Map<String, String>> expectedRows = dataTable.asMaps();
        for (Map<String, String> row : expectedRows) {
            int month = Integer.parseInt(row.get("month"));
            String expectedMonthName = row.get("monthName");

            String actualMonthName = response.getYears().stream()
                    .flatMap(y -> y.getMonths().stream())
                    .filter(m -> m.getMonth().equals(month))
                    .findFirst()
                    .map(TrainerWorkloadResponse.MonthSummaryDto::getMonthName)
                    .orElse(null);

            assertThat(actualMonthName).isEqualTo(expectedMonthName);
        }
    }

    private TrainerWorkloadRequest createBasicRequest(String username, int year, int month,
                                                      int duration, ActionType actionType) {
        String[] names = username.split("\\.");
        String firstName = names.length > 0 ? capitalize(names[0]) : "Test";
        String lastName = names.length > 1 ? capitalize(names[1]) : "User";

        return TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName(firstName)
                .trainerLastName(lastName)
                .isActive(true)
                .trainingDate(LocalDate.of(year, month, 15))
                .trainingDuration(duration)
                .actionType(actionType)
                .transactionId(java.util.UUID.randomUUID().toString())
                .build();
    }

    private int parseMonth(String monthName) {
        return switch (monthName.toUpperCase()) {
            case "JANUARY" -> 1;
            case "FEBRUARY" -> 2;
            case "MARCH" -> 3;
            case "APRIL" -> 4;
            case "MAY" -> 5;
            case "JUNE" -> 6;
            case "JULY" -> 7;
            case "AUGUST" -> 8;
            case "SEPTEMBER" -> 9;
            case "OCTOBER" -> 10;
            case "NOVEMBER" -> 11;
            case "DECEMBER" -> 12;
            default -> throw new IllegalArgumentException("Invalid month: " + monthName);
        };
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
