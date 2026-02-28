package parser

import parser.*

fun main() {
    val parser = KmetaFileParser()

    // Beispiel-Inhalt
    val content = """
        type "Person" "A person entity" {
            prop("name", "String")
            prop("age", "Int")
            has("address", list("Address"))
            knows("friends", list("Person"))
        }
        
        type "Address" "An address entity" {
            prop("street", "String")
            prop("city", "String")
            prop("zipCode", "String")
        }
    """.trimIndent()

    // Parse den String
    val kmetaFile = parser.parseString(content)

    // Ausgabe der geparsten Daten
    println("=".repeat(50))
    println("Parsed ${kmetaFile.types.size} type(s):")
    println("=".repeat(50))

    for (type in kmetaFile.types) {
        println("\n📦 Type: ${type.name}")
        println("   Description: ${type.description}")

        if (type.props.isNotEmpty()) {
            println("\n   Properties:")
            for (prop in type.props) {
                val valueStr = when (val v = prop.value) {
                    is StringValue -> v.value
                    is ListValue -> "list(${v.value})"
                }
                println("      • ${prop.key}: $valueStr")
            }
        }

        if (type.has.isNotEmpty()) {
            println("\n   Has:")
            for (hasRule in type.has) {
                val valueStr = when (val v = hasRule.value) {
                    is StringValue -> v.value
                    is ListValue -> "list(${v.value})"
                }
                println("      • ${hasRule.key}: $valueStr")
            }
        }

        if (type.knows.isNotEmpty()) {
            println("\n   Knows:")
            for (knowsRule in type.knows) {
                val valueStr = when (val v = knowsRule.value) {
                    is StringValue -> v.value
                    is ListValue -> "list(${v.value})"
                }
                println("      • ${knowsRule.key}: $valueStr")
            }
        }
    }

    println("\n" + "=".repeat(50))
}

