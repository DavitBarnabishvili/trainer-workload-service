Feature: Workload Deletion
  As a trainer workload service
  I want to process DELETE messages from the message queue
  So that incorrect or cancelled trainings can be removed from workload

  Background:
    Given the workload database is empty
    And the message queue is ready

  Scenario: Delete training from existing workload
    Given a trainer "john.doe" exists with 120 minutes in January 2025
    When a DELETE message is sent to remove 60 minutes from "john.doe" on "2025-01-15"
    And the message is processed successfully
    Then the total workload for "john.doe" in January 2025 should be 60 minutes

  Scenario: Delete multiple trainings from the same month
    Given a trainer "jane.smith" exists with 180 minutes in February 2025
    When a DELETE message is sent to remove 60 minutes from "jane.smith" on "2025-02-10"
    And the message is processed successfully
    And a DELETE message is sent to remove 30 minutes from "jane.smith" on "2025-02-15"
    And the message is processed successfully
    Then the total workload for "jane.smith" in February 2025 should be 90 minutes

  Scenario: Delete training reducing workload to zero
    Given a trainer "bob.johnson" exists with 60 minutes in March 2025
    When a DELETE message is sent to remove 60 minutes from "bob.johnson" on "2025-03-10"
    And the message is processed successfully
    Then the total workload for "bob.johnson" in March 2025 should be 0 minutes

  Scenario: Delete training from specific month without affecting other months
    Given a trainer "alice.williams" exists with the following workload:
      | year | month | duration |
      | 2025 | 1     | 120      |
      | 2025 | 2     | 90       |
      | 2025 | 3     | 150      |
    When a DELETE message is sent to remove 30 minutes from "alice.williams" on "2025-02-15"
    And the message is processed successfully
    Then the workload for "alice.williams" should show:
      | year | month | duration |
      | 2025 | 1     | 120      |
      | 2025 | 2     | 60       |
      | 2025 | 3     | 150      |

  Scenario: Delete training from non-existent trainer
    Given no trainer workload exists for "unknown.trainer"
    When a DELETE message is sent to remove 60 minutes from "unknown.trainer" on "2025-01-15"
    Then the message should fail with trainer not found error

  Scenario: Delete training with amount exceeding available workload
    Given a trainer "charlie.brown" exists with 60 minutes in January 2025
    When a DELETE message is sent to remove 120 minutes from "charlie.brown" on "2025-01-15"
    Then the message should fail with insufficient workload error

  Scenario: Delete training with negative duration
    Given a trainer "david.miller" exists with 100 minutes in January 2025
    When a DELETE message with negative duration is sent for "david.miller"
    Then the message should be rejected due to validation error

  Scenario: Delete training with duration exceeding maximum
    Given a trainer "emma.davis" exists with 200 minutes in January 2025
    When a DELETE message with duration 500 is sent for "emma.davis" on "2025-01-15"
    Then the message should be rejected due to validation error

  Scenario: Delete training with future date
    Given a trainer "frank.wilson" exists with 100 minutes in January 2025
    When a DELETE message is sent for "frank.wilson" with future date "2030-12-31"
    Then the message should be rejected due to validation error

  Scenario: Delete training with missing required fields
    Given a DELETE workload request with missing trainer username
    When the message is sent to the workload queue
    Then the message should be rejected due to validation error

  Scenario: Delete all trainings from a month across multiple operations
    Given a trainer "grace.moore" exists with 150 minutes in April 2025
    When a DELETE message is sent to remove 50 minutes from "grace.moore" on "2025-04-05"
    And the message is processed successfully
    And a DELETE message is sent to remove 50 minutes from "grace.moore" on "2025-04-10"
    And the message is processed successfully
    And a DELETE message is sent to remove 50 minutes from "grace.moore" on "2025-04-20"
    And the message is processed successfully
    Then the total workload for "grace.moore" in April 2025 should be 0 minutes

  Scenario: Delete training with invalid username format
    Given a DELETE workload request with invalid username "invalid user!"
    When the message is sent to the workload queue
    Then the message should be rejected due to validation error

  Scenario: Delete training from year/month that doesn't exist in workload
    Given a trainer "henry.taylor" exists with 100 minutes in January 2025
    When a DELETE message is sent to remove 60 minutes from "henry.taylor" on "2025-06-15"
    Then the message should fail with month not found error
