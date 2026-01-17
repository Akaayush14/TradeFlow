package com.example.tradeflow.view

import com.example.tradeflow.Country
import com.example.tradeflow.countries


object PhoneParser {
    fun parseFullPhone(fullPhone: String): Pair<Country, String> {
        if (fullPhone.isEmpty()) {
            return Pair(countries.first { it.name == "Nepal" }, "")
        }

        for (country in countries) {
            val code = country.code
            if (fullPhone.startsWith(code)) {
                val number = fullPhone.substring(code.length)
                return Pair(country, number)
            }
        }

        return Pair(countries.first { it.name == "Nepal" }, fullPhone)
    }

    fun isValidPhoneNumber(number: String): Boolean {
        // Remove any spaces, dashes, parentheses
        val cleanNumber = number.filter { it.isDigit() }
        return cleanNumber.length >= 7
    }
}