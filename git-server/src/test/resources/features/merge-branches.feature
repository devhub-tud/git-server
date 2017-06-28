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

  Scenario: Merge a branch to master
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

  Scenario: Merge a branch to release
    Given I clone repository "merge-test"
    And   I checkout branch "release"
    And   I create the file "push.txt" with the contents
      """
      I wish to be seen.
      """
    And   I have added "push.txt" to the index
    And   I committed the result
    And   I push the commit to "release"

    And   I checkout a new branch "develop"
    And   I create the file "README.md" with the contents
      """
      It is an excellent day.
      For merging branches.
      Into other branches.
      """
    And   I have added "README.md" to the index
    And   I committed the result
    And   I push the commit to "develop"

    When  I merge the branch "develop" into "release"
    Then  the branch "develop" is merged into "release"
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

  Scenario: Merge succeeds if master diverged
    Given I clone repository "merge-test"
    And   "develop" is ahead of "master"
    And   "release" is ahead of "develop"
    When  I merge the branch "develop" into "master"
    And   I merge the branch "release" into "master"
    Then  the branch "develop" is merged into "master"
    And   the branch "release" is merged into "master"
    And   the work folder is clean

  Scenario: Merge succeeds if branch has been merged before
    Given I clone repository "merge-test"
    And   "develop" is ahead of "master"
    And   I merge the branch "develop" into "master"
    And   "develop" is ahead of "master"
    When  I merge the branch "develop" into "master"
    Then  the branch "develop" is merged into "master"
    And   the work folder is clean