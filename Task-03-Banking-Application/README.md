# Task 03 – Banking Console Application (Java)

**Student Name:** Vaishnavi Pise

## Description

A console-based Banking Application written in Java. It repeatedly shows the banking
menu, lets the user check the balance, deposit money, withdraw money and exit.
A single balance variable starts at a fixed opening balance of `Rs.10000` and is
updated only after a valid transaction.

## Features

- Main menu (`1. Check Balance`, `2. Deposit Money`, `3. Withdraw Money`, `4. Exit`) inside a `while (true)` loop
- Fixed opening balance of `Rs.10000`
- Deposit: `balance = balance + amount` (amount must be greater than 0)
- Withdraw: `balance = balance - amount` (amount must be greater than 0 and within the available balance)
- Insufficient balance is reported and the balance is left unchanged
- Invalid menu choices and invalid amounts are reported with a helpful message
- Exit option ends the loop with `break` and prints a goodbye message

## Banking Rules

| Rule | Requirement |
|------|-------------|
| Opening balance | Fixed at `Rs.10000` |
| Deposit | Balance after deposit = current balance + deposit amount |
| Withdrawal | Balance after withdrawal = current balance − withdrawal amount, only when funds are sufficient |
| Validation | Zero or negative amounts are rejected; a failed transaction never changes the balance |

## How to Compile and Run

```
javac BankingApp.java
java BankingApp
```

## Sample Scenario A — Deposit and check balance

Input: `2` (Deposit) → `1500` → `1` (Check Balance) → `4` (Exit)

```
Welcome to Kiran Bank
Opening Balance: Rs.10000.00
...
Enter choice: 2
Enter deposit amount: 1500
Deposit successful.
Updated Balance: Rs.11500.00
Enter choice: 1
Current Balance: Rs.11500.00
Enter choice: 4
Thank you for banking with us. Goodbye!
```

## Sample Scenario B — Insufficient balance

Input: `3` (Withdraw) → `12000` → `1` (Check Balance) → `4` (Exit)

```
Enter choice: 3
Enter withdrawal amount: 12000
Transaction failed: Insufficient balance.
Current Balance: Rs.10000.00
Enter choice: 1
Current Balance: Rs.10000.00
```
