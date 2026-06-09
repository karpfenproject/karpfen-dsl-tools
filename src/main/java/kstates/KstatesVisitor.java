// Generated from kstates/Kstates.g4 by ANTLR 4.13.1
package kstates;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link KstatesParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface KstatesVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link KstatesParser#kstates_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKstates_file(KstatesParser.Kstates_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#statemachine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatemachine(KstatesParser.StatemachineContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#states_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStates_block(KstatesParser.States_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#state_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitState_definition(KstatesParser.State_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#entry_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntry_block(KstatesParser.Entry_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#do_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDo_block(KstatesParser.Do_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#action_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAction_block(KstatesParser.Action_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#action_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAction_item(KstatesParser.Action_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#in_scope_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_scope_block(KstatesParser.In_scope_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#with_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_block(KstatesParser.With_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#action_rule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAction_rule(KstatesParser.Action_ruleContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#action_operation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAction_operation(KstatesParser.Action_operationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#action_right_side}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAction_right_side(KstatesParser.Action_right_sideContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#macro_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacro_call(KstatesParser.Macro_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#eval_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEval_statement(KstatesParser.Eval_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#transitions_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransitions_block(KstatesParser.Transitions_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#transition_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransition_definition(KstatesParser.Transition_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#normal_transition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNormal_transition(KstatesParser.Normal_transitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#split_transition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSplit_transition(KstatesParser.Split_transitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#join_transition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_transition(KstatesParser.Join_transitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#not_looping}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot_looping(KstatesParser.Not_loopingContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#condition_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition_block(KstatesParser.Condition_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#condition_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition_clause(KstatesParser.Condition_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#event_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_condition(KstatesParser.Event_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#value_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_condition(KstatesParser.Value_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#macros_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacros_block(KstatesParser.Macros_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#macro_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacro_definition(KstatesParser.Macro_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#takes_directive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTakes_directive(KstatesParser.Takes_directiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#returns_directive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturns_directive(KstatesParser.Returns_directiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#return_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_expression(KstatesParser.Return_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#type_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_expression(KstatesParser.Type_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#typed_call_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTyped_call_expression(KstatesParser.Typed_call_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#reference_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReference_expression(KstatesParser.Reference_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#definition_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition_block(KstatesParser.Definition_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#eval_code_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEval_code_block(KstatesParser.Eval_code_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KstatesParser#code_content}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCode_content(KstatesParser.Code_contentContext ctx);
}