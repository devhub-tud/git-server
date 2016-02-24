Feature: Create a repository

  Scenario: Create a new repository based on a template
    Given the Git server is ready
    And   the template repository "https://github.com/octocat/Spoon-Knife.git"
    And   the following permissions:
      | user-a | READ_WRITE |
      | user-b | READ_WRITE |
    When  I create repository "courses/ti1706/1516/group-1"
    Then  the template repository is cloned
    And   the template is pushed to the provisioned repository
    And   the permissions look like this:
      | user-a | READ_WRITE |
      | user-b | READ_WRITE |

  Scenario: Create a bare repository
    Given the Git server is ready
    And   the following permissions:
      | user-a | READ_WRITE |
      | user-b | READ_WRITE |
    When  I create repository "courses/ti1706/1516/group-2"
    Then  A bare repository is initialized at the remote repository
    And   the permissions look like this:
      | user-a | READ_WRITE |
      | user-b | READ_WRITE |
