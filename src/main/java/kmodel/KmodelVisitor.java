// Generated from kmodel/Kmodel.g4 by ANTLR 4.13.1
package kmeta;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link KmodelParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface KmodelVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link KmodelParser#kmodel_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKmodel_file(KmodelParser.Kmodel_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link KmodelParser#make_object_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMake_object_block(KmodelParser.Make_object_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KmodelParser#object_signature}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_signature(KmodelParser.Object_signatureContext ctx);
	/**
	 * Visit a parse tree produced by {@link KmodelParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(KmodelParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link KmodelParser#prop_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProp_statement(KmodelParser.Prop_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link KmodelParser#has_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHas_statement(KmodelParser.Has_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link KmodelParser#knows_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKnows_statement(KmodelParser.Knows_statementContext ctx);
}