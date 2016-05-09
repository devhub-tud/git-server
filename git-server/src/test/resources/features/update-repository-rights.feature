Feature: Edit access rights for a repository

  Scenario: Swap a user
    Given the Git server is ready
    And   the template repository "https://github.com/octocat/Spoon-Knife.git"
    And   the following permissions:
      | user-a | READ_WRITE |
      | user-b | READ_WRITE |
    And   the template is cloned into "courses/ti1706/1516/group-1"
    When  I update the permissions to:
      | user-a | READ_WRITE |
      | user-c | READ_WRITE |
    Then  the permissions look like this:
      | user-a | READ_WRITE |
      | user-c | READ_WRITE |

  Scenario: Add a user
    Given the Git server is ready
    And   the template repository "https://github.com/octocat/Spoon-Knife.git"
    And   the following permissions:
      | user-a | READ_WRITE |
      | user-b | READ_WRITE |
    And   the template is cloned into "courses/ti1706/1516/group-1"
    When  I update the permissions to:
      | user-a | READ_WRITE |
      | user-c | READ_ONLY  |
    Then  the permissions look like this:
      | user-a | READ_WRITE |
      | user-c | READ_ONLY  |
