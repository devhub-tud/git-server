Feature: Remove a repository

  Scenario: Remove a new repository
    Given the Git server is ready
    And   the template repository "https://github.com/octocat/Spoon-Knife.git"
    And   the following permissions:
      | user-a | READ_WRITE |
      | user-b | READ_WRITE |
    And   the template is cloned into "courses/ti1706/1516/group-1"
    When  I remove repository "courses/ti1706/1516/group-1"
    Then  the template repository is removed
