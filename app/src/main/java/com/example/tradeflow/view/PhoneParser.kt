package com.example.tradeflow.view

import com.example.tradeflow.Country
import com.example.tradeflow.countries


object PhoneParser {
    fun parseFullPhone(fullPhone: String): Pair<Country, String> {
        val matchingCountry = countries.find { fullPhone.startsWith(it.code) }

        return if (matchingCountry != null) {
            val phoneNumber = fullPhone.removePrefix(matchingCountry.code)
            Pair(matchingCountry, phoneNumber)
        } else {
            val nepalCountry = countries.find { it.name == "Nepal" } ?: countries[0]
            Pair(nepalCountry, fullPhone)
        }
    }

    fun combinePhone(country: Country, phoneNumber: String): String {
        return country.code + phoneNumber.trim()
    }

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.matches(Regex("^[0-9]{7,15}$")) // 7-15 digits
    }
}