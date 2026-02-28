package parser

import kmeta.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

/**
 * Parser für kmeta-Dateien mit ANTLR v4
 *
 * Verwendung:
 * ```kotlin
 * val parser = KmetaFileParser()
 * val kmetaFile = parser.parseFile("path/to/file.kmeta")
 *
 * for (type in kmetaFile.types) {
 *     println("Type: ${type.name}")
 *     for (prop in type.props) {
 *         println("  Prop: ${prop.key} = ${prop.value}")
 *     }
 * }
 * ```
 */
class KmetaFileParser {

    /**
     * Parst eine kmeta-Datei und gibt einen Metamodel-AST zurück
     */
    fun parseFile(filePath: String): KmetaFile {
        val file = File(filePath)
        return parseString(file.readText())
    }

    /**
     * Parst einen String als kmeta-Datei
     */
    fun parseString(content: String): KmetaFile {
        val inputStream = CharStreams.fromString(content)
        val lexer = KmetaLexer(inputStream)
        val tokens = CommonTokenStream(lexer)
        val parser = KmetaParser(tokens)

        val parseTree = parser.kmeta_file()
        val visitor = KmetaParseVisitor()

        return visitor.visit(parseTree) ?: KmetaFile(emptyList())
    }
}

/**
 * Custom Visitor zur Verarbeitung des Parse-Trees
 */
class KmetaParseVisitor : KmetaBaseVisitor<KmetaFile>() {
    private val typeDefinitions = mutableListOf<TypeDefinition>()

    override fun visitKmeta_file(ctx: KmetaParser.Kmeta_fileContext): KmetaFile {
        for (typeDefCtx in ctx.type_definition()) {
            typeDefinitions.add(visitTypeDefinition(typeDefCtx))
        }
        return KmetaFile(typeDefinitions)
    }

    private fun visitTypeDefinition(ctx: KmetaParser.Type_definitionContext): TypeDefinition {
        val typeName = ctx.STRING(0).text.trim('"')
        val typeDescription = ctx.STRING(1).text.trim('"')
        val typeDef = TypeDefinition(typeName, typeDescription)

        val ruleListCtx = ctx.rule_list()
        if (ruleListCtx != null) {
            for (ruleCtx in ruleListCtx.rule_()) {
                when {
                    ruleCtx.prop_rule() != null -> {
                        val propCtx = ruleCtx.prop_rule()
                        val key = propCtx.STRING().text.trim('"')
                        val value = parseRuleValue(propCtx.rule_value())
                        typeDef.addProp(key, value)
                    }
                    ruleCtx.has_rule() != null -> {
                        val hasCtx = ruleCtx.has_rule()
                        val key = hasCtx.STRING().text.trim('"')
                        val value = parseRuleValue(hasCtx.rule_value())
                        typeDef.addHas(key, value)
                    }
                    ruleCtx.knows_rule() != null -> {
                        val knowsCtx = ruleCtx.knows_rule()
                        val key = knowsCtx.STRING().text.trim('"')
                        val value = parseRuleValue(knowsCtx.rule_value())
                        typeDef.addKnows(key, value)
                    }
                }
            }
        }

        return typeDef
    }

    private fun parseRuleValue(ctx: KmetaParser.Rule_valueContext): RuleValue {
        return when {
            ctx.LIST() != null -> {
                // LIST LPAREN STRING RPAREN
                val listValue = ctx.STRING().text.trim('"')
                ListValue(listValue)
            }
            ctx.STRING() != null -> {
                // Just STRING
                val stringValue = ctx.STRING().text.trim('"')
                StringValue(stringValue)
            }
            else -> StringValue("")
        }
    }
}

// Data Classes für den AST
data class KmetaFile(val types: List<TypeDefinition>)

data class TypeDefinition(
    val name: String,
    val description: String
) {
    val props = mutableListOf<Rule>()
    val has = mutableListOf<Rule>()
    val knows = mutableListOf<Rule>()

    fun addProp(key: String, value: RuleValue) {
        props.add(Rule(key, value))
    }

    fun addHas(key: String, value: RuleValue) {
        has.add(Rule(key, value))
    }

    fun addKnows(key: String, value: RuleValue) {
        knows.add(Rule(key, value))
    }
}

data class Rule(val key: String, val value: RuleValue)

sealed class RuleValue
data class StringValue(val value: String) : RuleValue()
data class ListValue(val value: String) : RuleValue()

