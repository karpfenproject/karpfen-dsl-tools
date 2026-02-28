# ✅ ANTLR Kmeta Parser - Setup Complete!

## 🎉 Was wurde eingerichtet

Du hast jetzt einen vollständig funktionierenden ANTLR v4 Parser für `.kmeta`-Dateien!

### Dateien im Projekt:

1. **Grammatik-Datei**: `src/main/antlr/kmeta/Kmeta.g4`
   - Kombinierte Lexer/Parser-Grammatik
   - Definiert die Syntax für kmeta-Dateien

2. **Generierte Parser-Klassen** (nach Build): `src/main/java/kmeta/`
   - `KmetaLexer.java` - Tokenizer
   - `KmetaParser.java` - Parser
   - `KmetaBaseVisitor.java` - Basis-Visitor-Klasse
   - `KmetaVisitor.java` - Visitor-Interface

3. **Kotlin Parser-Wrapper**: `src/main/kotlin/parser/KmetaFileParser.kt`
   - Einfache Kotlin-API zum Parsen
   - Data Classes für das AST

4. **Beispiel**: `src/main/kotlin/examples/KmetaParserExample.kt`
   - Zeigt, wie man den Parser verwendet

## 🚀 Verwendung

### 1. Generiere die ANTLR-Dateien

Beim Build werden die ANTLR-Dateien automatisch generiert:

```bash
./gradlew build
```

Oder nur die Generierung:

```bash
./gradlew copyAntlrSources
```

Die generierten Java-Dateien werden nach `src/main/java/kmeta/` kopiert, wo du sie importieren kannst!

### 2. Verwende den Parser in deinem Code

```kotlin
//import parser.KmetaFileParser
//import parser.*

fun main() {
    val parser = KmetaFileParser()
    
    // Parse einen String
    val content = """
        type "Person" "A person entity" {
            prop("name", "String")
            prop("age", "Int")
            has("address", list("Address"))
        }
    """.trimIndent()
    
    val kmetaFile = parser.parseString(content)
    
    // Oder parse eine Datei
    // val kmetaFile = parser.parseFile("path/to/file.kmeta")
    
    // Verwende die geparsten Daten
    for (type in kmetaFile.types) {
        println("Type: ${type.name}")
        println("Description: ${type.description}")
        
        for (prop in type.props) {
            when (val value = prop.value) {
                is StringValue -> println("  ${prop.key}: ${value.value}")
                is ListValue -> println("  ${prop.key}: list(${value.value})")
            }
        }
    }
}
```

### 3. Führe das Beispiel aus

```bash
./gradlew run -PmainClass=examples.KmetaParserExampleKt
```

## 📝 Kmeta-Syntax

```
type "<TypeName>" "<Description>" {
    prop("<property>", "<type>")
    prop("<property>", list("<type>"))
    has("<relation>", "<type>")
    has("<relation>", list("<type>"))
    knows("<relation>", "<type>")
    knows("<relation>", list("<type>"))
}
```

### Beispiel:

```
type "Company" "A company entity" {
    prop("name", "String")
    prop("foundedYear", "Int")
    has("employees", list("Person"))
    has("headquarters", "Address")
    knows("partners", list("Company"))
}
```

## 🛠️ IntelliJ Integration

### Nach dem ersten Build:

1. **File > Invalidate Caches / Restart** (falls Klassen nicht erkannt werden)
2. `src/main/java` sollte automatisch als Source Root markiert sein
3. Code-Vervollständigung für `kmeta.*` sollte funktionieren

### Bei Grammatik-Änderungen:

```bash
./gradlew clean copyAntlrSources
```

Dann in IntelliJ: **Build > Rebuild Project**

## 📂 Projektstruktur

```
datamodel/
├── build.gradle                        # Build-Konfiguration
├── src/
│   ├── main/
│   │   ├── antlr/
│   │   │   └── kmeta/
│   │   │       └── Kmeta.g4           # ⭐ ANTLR Grammatik (EDITIEREN)
│   │   ├── java/
│   │   │   └── kmeta/                 # ⚡ Generierte Parser-Klassen (NICHT EDITIEREN)
│   │   │       ├── KmetaLexer.java
│   │   │       ├── KmetaParser.java
│   │   │       ├── KmetaBaseVisitor.java
│   │   │       └── KmetaVisitor.java
│   │   └── kotlin/
│   │       ├── parser/
│   │       │   └── KmetaFileParser.kt # 🔧 Kotlin Parser-API
│   │       └── examples/
│   │           └── KmetaParserExample.kt # 📖 Beispiel
│   └── test/
└── build/
    └── generated-src/
        └── antlr/
            └── main/                   # Temp-Generierung (wird nach src/ kopiert)
```

## 🔄 Workflow

1. **Grammatik bearbeiten**: `src/main/antlr/kmeta/Kmeta.g4`
2. **Build ausführen**: `./gradlew clean copyAntlrSources`
3. **Parser-Klassen werden generiert**: Nach `src/main/java/kmeta/`
4. **In deinem Code importieren**: `import kmeta.*` und `import parser.*`

## 🎯 Data Classes (AST)

```kotlin
// Hauptklasse - repräsentiert eine komplette .kmeta Datei
data class KmetaFile(val types: List<TypeDefinition>)

// Eine Type-Definition
data class TypeDefinition(
    val name: String,
    val description: String,
    val props: MutableList<Rule>,
    val has: MutableList<Rule>,
    val knows: MutableList<Rule>
)

// Eine Regel (prop, has, knows)
data class Rule(val key: String, val value: RuleValue)

// Wert einer Regel
sealed class RuleValue
data class StringValue(val value: String) : RuleValue()
data class ListValue(val value: String) : RuleValue()
```

## ⚠️ Wichtige Hinweise

1. **Generierte Dateien NICHT editieren** - Sie befinden sich in `src/main/java/kmeta/` und werden bei jedem Build überschrieben
2. **Grammatik editieren** - Nur `src/main/antlr/kmeta/Kmeta.g4` bearbeiten
3. **Nach Grammatik-Änderungen** - Immer `./gradlew clean copyAntlrSources` ausführen
4. **Git** - Die generierten `.java`-Dateien sollten in `.gitignore` aufgenommen werden (optional, da sie reproduzierbar sind)

## 🐛 Troubleshooting

### Problem: "Unresolved reference: KmetaLexer"
**Lösung**: Führe `./gradlew copyAntlrSources` aus

### Problem: Doppelte Klassen beim Kompilieren
**Lösung**: Die `build.gradle` ist bereits konfiguriert, nur `src/main/java` zu verwenden

### Problem: Änderungen an Grammatik werden nicht übernommen
**Lösung**: 
```bash
./gradlew clean
./gradlew copyAntlrSources
```

## 🎊 Fertig!

Dein ANTLR-Parser ist jetzt einsatzbereit! Die generierten Klassen befinden sich in `src/main/java/kmeta/` und können von überall in deinem Projekt importiert werden.

**Teste es:**
```bash
./gradlew build
./gradlew run -PmainClass=examples.KmetaParserExampleKt
```

Viel Erfolg! 🚀

