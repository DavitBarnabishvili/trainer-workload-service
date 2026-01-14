Feature: Workload Retrieval
  As a trainer workload service
  I want to retrieve trainer workload summaries
  So that workload information can be queried and displayed

  Background:
    Given the workload database is empty
    And the message queue is ready

  Scenario: Retrieve workload for trainer with single month
    Given a trainer "john.doe" exists with 120 minutes in January 2025
    When I request the workload for "john.doe"
    Then the response should contain trainer information:
      | trainerUsername | john.doe |
      | trainerFirstName | John     |
      | trainerLastName  | Doe      |
      | isActive         | true     |
    And the workload should show:
      | year | month | duration |
      | 2025 | 1     | 120      |

  Scenario: Retrieve workload for trainer with multiple months
    Given a trainer "jane.smith" exists with the following workload:
      | year | month | duration |
      | 2025 | 1     | 100      |
      | 2025 | 2     | 150      |
      | 2025 | 3     | 80       |
    When I request the workload for "jane.smith"
    Then the workload should show:
      | year | month | duration |
      | 2025 | 1     | 100      |
      | 2025 | 2     | 150      |
      | 2025 | 3     | 80       |

  Scenario: Retrieve workload for trainer spanning multiple years
    Given a trainer "bob.johnson" exists with the following workload:
      | year | month | duration |
      | 2024 | 11    | 90       |
      | 2024 | 12    | 120      |
      | 2025 | 1     | 100      |
      | 2025 | 2     | 110      |
    When I request the workload for "bob.johnson"
    Then the workload should show:
      | year | month | duration |
      | 2024 | 11    | 90       |
      | 2024 | 12    | 120      |
      | 2025 | 1     | 100      |
      | 2025 | 2     | 110      |

  Scenario: Retrieve workload for non-existent trainer
    Given no trainer workload exists for "unknown.trainer"
    When I request the workload for "unknown.trainer"
    Then the request should fail with trainer not found error

  Scenario: Retrieve workload with null username
    When I request the workload with null username
    Then the request should fail with validation error

  Scenario: Retrieve workload with empty username
    When I request the workload with empty username
    Then the request should fail with validation error

  Scenario: Retrieve workload for inactive trainer
    Given an inactive trainer "inactive.trainer" exists with 60 minutes in January 2025
    When I request the workload for "inactive.trainer"
    Then the response should contain trainer information:
      | trainerUsername | inactive.trainer |
      | isActive         | false            |
    And the workload should show:
      | year | month | duration |
      | 2025 | 1     | 60       |

  Scenario: Retrieve workload after adding and deleting trainings
    Given a trainer "alice.williams" exists with 150 minutes in March 2025
    When a DELETE message is sent to remove 50 minutes from "alice.williams" on "2025-03-15"
    And the message is processed successfully
    And a new training of 100 minutes is added for "alice.williams" on "2025-03-20"
    And the message is processed successfully
    Then I request the workload for "alice.williams"
    And the total workload for "alice.williams" in March 2025 should be 200 minutes

  Scenario: Retrieve complete workload summary after multiple operations
    Given a new trainer "charlie.brown"
    When the following trainings are added for "charlie.brown":
      | date       | duration |
      | 2025-01-10 | 60       |
      | 2025-01-20 | 90       |
      | 2025-02-05 | 120      |
      | 2025-03-15 | 75       |
    And all messages are processed successfully
    Then I request the workload for "charlie.brown"
    And the workload should show:
      | year | month | duration |
      | 2025 | 1     | 150      |
      | 2025 | 2     | 120      |
      | 2025 | 3     | 75       |

  Scenario: Verify month names are included in response
    Given a trainer "david.miller" exists with the following workload:
      | year | month | duration |
      | 2025 | 1     | 60       |
      | 2025 | 2     | 90       |
      | 2025 | 12    | 120      |
    When I request the workload for "david.miller"
    Then the response should include month names:
      | month | monthName |
      | 1     | JANUARY   |
      | 2     | FEBRUARY  |
      | 12    | DECEMBER  |

  Scenario: Retrieve workload with zero duration months
    Given a trainer "emma.davis" exists with 100 minutes in January 2025
    When a DELETE message is sent to remove 100 minutes from "emma.davis" on "2025-01-15"
    And the message is processed successfully
    Then I request the workload for "emma.davis"
    And the workload should show:
      | year | month | duration |
      | 2025 | 1     | 0        |
