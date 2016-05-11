Feature: Merge Branches

  As a maintainer,
  I want to be able to merge branches from collaborators,
  So that their contributions will be added to the project.

  Background:
    Given the Git server is ready
    And   the following permissions:
      | user-a | READ_WRITE |
    And   I create repository "merge-test"
    And   I clone repository "merge-test"
    And   I create the file "README.md" with the contents
      """
      Hello Sir.
      """
    And   I have added "README.md" to the index
    And   I committed the result
    And   I checkout branch "master"
    And   I push the commit to "master"

  Scenario: Merge a branch
    Given I clone repository "merge-test"
    And   I create the file "README.md" with the contents
      """
      Hello Sir.
      How is your day?
      """
    And   I have added "README.md" to the index
    And   I committed the result
    And   I checkout a new branch "develop"
    And   I push the commit to "develop"
    When  I merge the branch "develop" into "master"
    Then  the branch "develop" is merged into "master"
    And   the work folder is clean

  Scenario: Merge fails on merge conflict
    Given I clone repository "merge-test"
    And   "develop" is ahead of "master"
    And   I clone repository "merge-test"
    And   I create the file "README.md" with the contents
      """
      Hello Sir.
      Would you like a cup of tea?
      """
    And   I have added "README.md" to the index
    And   I committed the result
    And   I checkout a new branch "conflict"
    And   I push the commit to "conflict"
    When  I merge the branch "develop" into "master"
    And   I merge the branch "conflict" into "master"
    Then  the merge fails with an exception
    But   the work folder is clean
