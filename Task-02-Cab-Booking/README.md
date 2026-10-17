# Task 02 – Cab Booking Chatbot (Java)

**Student Name:** Vaishnavi Pise

## Description

A console-based Cab Booking Chatbot written in Java. It repeatedly shows a main
menu, lets the user choose a cab type and a predefined route, calculates the fare,
and enforces the OTP cancellation rule (a ride can only be cancelled **before** the
OTP is shared).

## Features

- Main menu (`1. Book Ride`, `2. Exit`) inside a `while (true)` loop
- Cab types with per-km rates: Auto `12`, Sedan `18`, SUV `25`
- Three predefined routes with fixed distances
- Fare calculation: `rate per km × distance`
- Cancellation allowed only before the OTP is shared
- 4-digit OTP shared after confirmation; ride is locked afterwards
- Input validation for every menu choice

## Predefined Routes

| Route | Source      | Destination  | Distance |
|-------|-------------|--------------|----------|
| 1     | Pune Station| Shivajinagar | 5 km     |
| 2     | Swargate    | Kothrud      | 8 km     |
| 3     | Hinjawadi   | Wakad        | 10 km    |

## Cab Rates

| Cab Type | Rate per km |
|----------|-------------|
| Auto     | Rs.12       |
| Sedan    | Rs.18       |
| SUV      | Rs.25       |

## How to Compile and Run

```
javac CabBookingChatbot.java
java CabBookingChatbot
```

## Sample Scenario

Input: `1` (Book Ride) → `2` (Sedan) → `3` (Hinjawadi to Wakad) → `1` (Confirm & Share OTP)

Result: Distance = 10 km, Sedan rate = Rs.18/km, Fare = 10 × 18 = **Rs.180**.
The 4-digit OTP is shared only after confirmation, after which cancellation is not allowed.
