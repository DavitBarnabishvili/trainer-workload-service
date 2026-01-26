Feature: Workload Addition
  As a trainer workload service
  I want to process ADD messages from the message queue
  So that trainer workload can be tracked and accumulated

  Background:
    Given the workload database is empty
    And the message queue is ready

  Scenario: Add training for a new trainer
    Given a trainer workload request with the following details:
      | trainerUsername  | john.doe     |
      | trainerFirstName | John         |
      | trainerLastName  | Doe          |
      | isActive         | true         |
      | trainingDate     | 2025-01-15   |
      | trainingDuration | 60           |
      | actionType       | ADD          |
    When the message is sent to the workload queue
    And the message is processed successfully
    Then the trainer workload should be created in the database
    And the workload should contain 60 minutes for January 2025

  Scenario: Add multiple trainings for the same trainer in the same month
    Given a trainer "jane.smith" exists with 60 minutes in January 2025
    When a new training of 90 minutes is added for "jane.smith" on "2025-01-20"
    And the message is processed successfully
    Then the total workload for "jane.smith" in January 2025 should be 150 minutes

  Scenario: Add trainings for the same trainer in different months
    Given a trainer "bob.johnson" exists with 120 minutes in January 2025
    When a new training of 60 minutes is added for "bob.johnson" on "2025-02-10"
    And the message is processed successfully
    Then the workload for "bob.johnson" should show:
      | year | month | duration |
      | 2025 | 1     | 120      |
      | 2025 | 2     | 60       |

  Scenario: Add trainings across multiple years
    Given a trainer "alice.williams" exists with 100 minutes in December 2024
    When a new training of 80 minutes is added for "alice.williams" on "2025-01-05"
    And the message is processed successfully
    Then the workload for "alice.williams" should show:
      | year | month | duration |
      | 2024 | 12    | 100      |
      | 2025 | 1     | 80       |

  Scenario: Update trainer information when adding training
    Given a trainer workload exists for "charlie.brown" with name "Charlie Brown"
    When a training is added for "charlie.brown" with updated name "Charles Brown"
    And the message is processed successfully
    Then the trainer name should be updated to "Charles Brown"

  Scenario: Add training with maximum allowed duration
    Given a trainer workload request with the following details:
      | trainerUsername  | max.duration |
      | trainerFirstName | Max          |
      | trainerLastName  | Duration     |
      | isActive         | true         |
      | trainingDate     | 2025-01-15   |
      | trainingDuration | 480          |
      | actionType       | ADD          |
    When the message is sent to the workload queue
    And the message is processed successfully
    Then the trainer workload should be created in the database
    And the workload should contain 480 minutes for January 2025

  Scenario: Reject training with invalid duration (negative)
    Given a trainer workload request with the following details:
      | trainerUsername  | invalid.user |
      | trainerFirstName | Invalid      |
      | trainerLastName  | User         |
      | isActive         | true         |
      | trainingDate     | 2025-01-15   |
      | trainingDuration | -60          |
      | actionType       | ADD          |
    When the message is sent to the workload queue
    Then the message should be rejected due to validation error

  Scenario: Reject training with duration exceeding maximum (8 hours)
    Given a trainer workload request with the following details:
      | trainerUsername  | over.limit   |
      | trainerFirstName | Over         |
      | trainerLastName  | Limit        |
      | isActive         | true         |
      | trainingDate     | 2025-01-15   |
      | trainingDuration | 500          |
      | actionType       | ADD          |
    When the message is sent to the workload queue
    Then the message should be rejected due to validation error

  Scenario: Reject training with future date
    Given a trainer workload request with the following details:
      | trainerUsername  | future.user  |
      | trainerFirstName | Future       |
      | trainerLastName  | User         |
      | isActive         | true         |
      | trainingDate     | 2030-12-31   |
      | trainingDuration | 60           |
      | actionType       | ADD          |
    When the message is sent to the workload queue
    Then the message should be rejected due to validation error

  Scenario: Reject training with missing required fields
    Given a trainer workload request with missing trainer username
    When the message is sent to the workload queue
    Then the message should be rejected due to validation error

  Scenario: Reject training with invalid username format
    Given a trainer workload request with the following details:
      | trainerUsername  | invalid user! |
      | trainerFirstName | Invalid       |
      | trainerLastName  | User          |
      | isActive         | true          |
      | trainingDate     | 2025-01-15    |
      | trainingDuration | 60            |
      | actionType       | ADD           |
    When the message is sent to the workload queue
    Then the message should be rejected due to validation error

  Scenario: Add training for inactive trainer
    Given a trainer workload request with the following details:
      | trainerUsername  | inactive.trainer |
      | trainerFirstName | Inactive         |
      | trainerLastName  | Trainer          |
      | isActive         | false            |
      | trainingDate     | 2025-01-15       |
      | trainingDuration | 60               |
      | actionType       | ADD              |
    When the message is sent to the workload queue
    And the message is processed successfully
    Then the trainer workload should be created in the database
    And the trainer should be marked as inactive
