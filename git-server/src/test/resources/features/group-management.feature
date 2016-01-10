Feature: Group Management

  Groups are used to create repository access rules that apply for multiple users.
  Groups are created and updated through the Group API, which delegates the changes
  to the Gitolite config. An administrator should be able to create and edit groups
  and their members.

  Scenario: Creating a course group
    Given the group name "@ti1705-assistants"
    And   the following group members:
      | mdejong2 | jgmeligmeyling | lclark |
    When  the group is created
    Then  the group should be added to the Gitolite configuration
    And   the configuration should be pushed to the remote

  Scenario: Adding users to an existing group
    Given the group name "@ti1705-assistants"
    And   the following group members:
      | mdejong2 | jgmeligmeyling | lclark |
    And   the group exists in the configuration
    When  "cgriessman" is added to the group
    Then  the group should contain:
      | mdejong2 | jgmeligmeyling | lclark | cgriessman |
    And   the configuration should be pushed to the remote

  Scenario: Removing users from an existing group
    Given the group name "@ti1705-assistants"
    And   the following group members:
      | mdejong2 | jgmeligmeyling | lclark |
    And   the group exists in the configuration
    When  "lclark" is removed from the group
    Then  the group should contain:
      | mdejong2 | jgmeligmeyling |
    And   the configuration should be pushed to the remote

  Scenario: Removing a group
    Given the group name "@ti1705-assistants"
    And   the following group members:
      | mdejong2 | jgmeligmeyling | lclark |
    And   the group exists in the configuration
    When  the group is deleted
    Then  the configuration should be empty
    And   the configuration should be pushed to the remote