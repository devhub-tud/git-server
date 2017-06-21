Feature: Create a tag

  Scenario: Create a tag
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
    When I commit the result
    Then a tag is added to the commit