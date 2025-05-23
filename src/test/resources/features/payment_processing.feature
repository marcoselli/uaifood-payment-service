Feature: Payment Processing
  As a payment service
  I want to process payments securely
  So that orders can be completed

  Background:
    Given the payment service is running
    And the database is clean

  Scenario: Process a successful credit card payment
    Given a new payment of 100.00 for order "123e4567-e89b-12d3-a456-426614174000" using "CREDIT_CARD"
    When the payment is processed
    Then the payment should be "APPROVED"
    And the payment should be saved in the database

  Scenario: Process a failed credit card payment
    Given a new payment of 200.00 for order "123e4567-e89b-12d3-a456-426614174001" using "CREDIT_CARD"
    When the payment is processed
    Then the payment should be "REJECTED"
    And the payment should be saved in the database

  Scenario: Process a PIX payment
    Given a new payment of 50.00 for order "123e4567-e89b-12d3-a456-426614174002" using "PIX"
    When the payment is processed
    Then the payment should be "PENDING"
    And the payment should be saved in the database

  Scenario: Process a payment with invalid amount
    Given a new payment of -10.00 for order "123e4567-e89b-12d3-a456-426614174003" using "CREDIT_CARD"
    When the payment is processed
    Then the payment should fail with error "Invalid payment amount"

  Scenario: Process a payment with invalid order ID
    Given a new payment of 75.00 for order "invalid-uuid" using "CREDIT_CARD"
    When the payment is processed
    Then the payment should fail with error "Invalid order ID format" 