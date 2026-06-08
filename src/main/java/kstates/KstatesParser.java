// Generated from kstates/Kstates.g4 by ANTLR 4.13.1
package kstates;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class KstatesParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		STATEMACHINE=1, ATTACHED=2, TO=3, STATES=4, STATE=5, INITIAL=6, ENTRY=7, 
		DO=8, SET=9, APPEND=10, EVENT=11, MACRO=12, EVAL=13, TRANSITIONS=14, TRANSITION=15, 
		NOT=16, LOOPING=17, CONDITION=18, VALUE=19, MACROS=20, TAKES=21, RETURNS=22, 
		DEFINITION=23, REFERENCE=24, IF=25, IN=26, SCOPE=27, ARROW=28, LBRACE=29, 
		RBRACE=30, LPAREN=31, RPAREN=32, COMMA=33, STRING=34, SSTRING=35, IDENT=36, 
		NUMBER=37, WS=38, LINE_COMMENT=39, SYMBOL=40;
	public static final int
		RULE_kstates_file = 0, RULE_statemachine = 1, RULE_states_block = 2, RULE_state_definition = 3, 
		RULE_entry_block = 4, RULE_do_block = 5, RULE_action_block = 6, RULE_action_item = 7, 
		RULE_in_scope_block = 8, RULE_action_rule = 9, RULE_action_operation = 10, 
		RULE_action_right_side = 11, RULE_macro_call = 12, RULE_eval_statement = 13, 
		RULE_transitions_block = 14, RULE_transition_definition = 15, RULE_not_looping = 16, 
		RULE_condition_block = 17, RULE_condition_clause = 18, RULE_event_condition = 19, 
		RULE_value_condition = 20, RULE_macros_block = 21, RULE_macro_definition = 22, 
		RULE_takes_directive = 23, RULE_returns_directive = 24, RULE_return_expression = 25, 
		RULE_type_expression = 26, RULE_typed_call_expression = 27, RULE_reference_expression = 28, 
		RULE_definition_block = 29, RULE_eval_code_block = 30, RULE_code_content = 31;
	private static String[] makeRuleNames() {
		return new String[] {
			"kstates_file", "statemachine", "states_block", "state_definition", "entry_block", 
			"do_block", "action_block", "action_item", "in_scope_block", "action_rule", 
			"action_operation", "action_right_side", "macro_call", "eval_statement", 
			"transitions_block", "transition_definition", "not_looping", "condition_block", 
			"condition_clause", "event_condition", "value_condition", "macros_block", 
			"macro_definition", "takes_directive", "returns_directive", "return_expression", 
			"type_expression", "typed_call_expression", "reference_expression", "definition_block", 
			"eval_code_block", "code_content"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'STATEMACHINE'", "'ATTACHED'", "'TO'", "'STATES'", "'STATE'", 
			"'INITIAL'", "'ENTRY'", "'DO'", "'SET'", "'APPEND'", "'EVENT'", "'MACRO'", 
			"'EVAL'", "'TRANSITIONS'", "'TRANSITION'", "'NOT'", "'LOOPING'", "'CONDITION'", 
			"'VALUE'", "'MACROS'", "'TAKES'", "'RETURNS'", "'DEFINITION'", "'reference'", 
			"'IF'", "'IN'", "'SCOPE'", "'->'", "'{'", "'}'", "'('", "')'", "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "STATEMACHINE", "ATTACHED", "TO", "STATES", "STATE", "INITIAL", 
			"ENTRY", "DO", "SET", "APPEND", "EVENT", "MACRO", "EVAL", "TRANSITIONS", 
			"TRANSITION", "NOT", "LOOPING", "CONDITION", "VALUE", "MACROS", "TAKES", 
			"RETURNS", "DEFINITION", "REFERENCE", "IF", "IN", "SCOPE", "ARROW", "LBRACE", 
			"RBRACE", "LPAREN", "RPAREN", "COMMA", "STRING", "SSTRING", "IDENT", 
			"NUMBER", "WS", "LINE_COMMENT", "SYMBOL"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Kstates.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public KstatesParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Kstates_fileContext extends ParserRuleContext {
		public StatemachineContext statemachine() {
			return getRuleContext(StatemachineContext.class,0);
		}
		public TerminalNode EOF() { return getToken(KstatesParser.EOF, 0); }
		public Kstates_fileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_kstates_file; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitKstates_file(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Kstates_fileContext kstates_file() throws RecognitionException {
		Kstates_fileContext _localctx = new Kstates_fileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_kstates_file);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			statemachine();
			setState(65);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatemachineContext extends ParserRuleContext {
		public TerminalNode STATEMACHINE() { return getToken(KstatesParser.STATEMACHINE, 0); }
		public TerminalNode ATTACHED() { return getToken(KstatesParser.ATTACHED, 0); }
		public TerminalNode TO() { return getToken(KstatesParser.TO, 0); }
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public States_blockContext states_block() {
			return getRuleContext(States_blockContext.class,0);
		}
		public Transitions_blockContext transitions_block() {
			return getRuleContext(Transitions_blockContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public Macros_blockContext macros_block() {
			return getRuleContext(Macros_blockContext.class,0);
		}
		public StatemachineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statemachine; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitStatemachine(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatemachineContext statemachine() throws RecognitionException {
		StatemachineContext _localctx = new StatemachineContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statemachine);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			match(STATEMACHINE);
			setState(68);
			match(ATTACHED);
			setState(69);
			match(TO);
			setState(70);
			match(STRING);
			setState(71);
			match(LBRACE);
			setState(72);
			states_block();
			setState(73);
			transitions_block();
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MACROS) {
				{
				setState(74);
				macros_block();
				}
			}

			setState(77);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class States_blockContext extends ParserRuleContext {
		public TerminalNode STATES() { return getToken(KstatesParser.STATES, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public List<State_definitionContext> state_definition() {
			return getRuleContexts(State_definitionContext.class);
		}
		public State_definitionContext state_definition(int i) {
			return getRuleContext(State_definitionContext.class,i);
		}
		public States_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_states_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitStates_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final States_blockContext states_block() throws RecognitionException {
		States_blockContext _localctx = new States_blockContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_states_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(STATES);
			setState(80);
			match(LBRACE);
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==STATE || _la==INITIAL) {
				{
				{
				setState(81);
				state_definition();
				}
				}
				setState(86);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(87);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class State_definitionContext extends ParserRuleContext {
		public TerminalNode STATE() { return getToken(KstatesParser.STATE, 0); }
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public TerminalNode INITIAL() { return getToken(KstatesParser.INITIAL, 0); }
		public Entry_blockContext entry_block() {
			return getRuleContext(Entry_blockContext.class,0);
		}
		public Do_blockContext do_block() {
			return getRuleContext(Do_blockContext.class,0);
		}
		public List<State_definitionContext> state_definition() {
			return getRuleContexts(State_definitionContext.class);
		}
		public State_definitionContext state_definition(int i) {
			return getRuleContext(State_definitionContext.class,i);
		}
		public State_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_state_definition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitState_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final State_definitionContext state_definition() throws RecognitionException {
		State_definitionContext _localctx = new State_definitionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_state_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INITIAL) {
				{
				setState(89);
				match(INITIAL);
				}
			}

			setState(92);
			match(STATE);
			setState(93);
			match(STRING);
			setState(94);
			match(LBRACE);
			setState(96);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ENTRY) {
				{
				setState(95);
				entry_block();
				}
			}

			setState(99);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DO) {
				{
				setState(98);
				do_block();
				}
			}

			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==STATE || _la==INITIAL) {
				{
				{
				setState(101);
				state_definition();
				}
				}
				setState(106);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(107);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Entry_blockContext extends ParserRuleContext {
		public TerminalNode ENTRY() { return getToken(KstatesParser.ENTRY, 0); }
		public Action_blockContext action_block() {
			return getRuleContext(Action_blockContext.class,0);
		}
		public Entry_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entry_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitEntry_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Entry_blockContext entry_block() throws RecognitionException {
		Entry_blockContext _localctx = new Entry_blockContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_entry_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			match(ENTRY);
			setState(110);
			action_block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Do_blockContext extends ParserRuleContext {
		public TerminalNode DO() { return getToken(KstatesParser.DO, 0); }
		public Action_blockContext action_block() {
			return getRuleContext(Action_blockContext.class,0);
		}
		public Do_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_do_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitDo_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Do_blockContext do_block() throws RecognitionException {
		Do_blockContext _localctx = new Do_blockContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_do_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			match(DO);
			setState(113);
			action_block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Action_blockContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public List<Action_itemContext> action_item() {
			return getRuleContexts(Action_itemContext.class);
		}
		public Action_itemContext action_item(int i) {
			return getRuleContext(Action_itemContext.class,i);
		}
		public Action_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_action_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitAction_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Action_blockContext action_block() throws RecognitionException {
		Action_blockContext _localctx = new Action_blockContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_action_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			match(LBRACE);
			setState(119);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33558016L) != 0)) {
				{
				{
				setState(116);
				action_item();
				}
				}
				setState(121);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(122);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Action_itemContext extends ParserRuleContext {
		public Action_ruleContext action_rule() {
			return getRuleContext(Action_ruleContext.class,0);
		}
		public In_scope_blockContext in_scope_block() {
			return getRuleContext(In_scope_blockContext.class,0);
		}
		public Action_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_action_item; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitAction_item(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Action_itemContext action_item() throws RecognitionException {
		Action_itemContext _localctx = new Action_itemContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_action_item);
		try {
			setState(126);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SET:
			case APPEND:
			case EVENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(124);
				action_rule();
				}
				break;
			case IF:
				enterOuterAlt(_localctx, 2);
				{
				setState(125);
				in_scope_block();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class In_scope_blockContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(KstatesParser.IF, 0); }
		public TerminalNode IN() { return getToken(KstatesParser.IN, 0); }
		public TerminalNode SCOPE() { return getToken(KstatesParser.SCOPE, 0); }
		public TerminalNode LPAREN() { return getToken(KstatesParser.LPAREN, 0); }
		public List<TerminalNode> STRING() { return getTokens(KstatesParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KstatesParser.STRING, i);
		}
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public Action_blockContext action_block() {
			return getRuleContext(Action_blockContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(KstatesParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KstatesParser.COMMA, i);
		}
		public In_scope_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_in_scope_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitIn_scope_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final In_scope_blockContext in_scope_block() throws RecognitionException {
		In_scope_blockContext _localctx = new In_scope_blockContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_in_scope_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			match(IF);
			setState(129);
			match(IN);
			setState(130);
			match(SCOPE);
			setState(131);
			match(LPAREN);
			setState(132);
			match(STRING);
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(133);
				match(COMMA);
				setState(134);
				match(STRING);
				}
				}
				setState(139);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(140);
			match(RPAREN);
			setState(141);
			action_block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Action_ruleContext extends ParserRuleContext {
		public Action_operationContext action_operation() {
			return getRuleContext(Action_operationContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(KstatesParser.LPAREN, 0); }
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public TerminalNode COMMA() { return getToken(KstatesParser.COMMA, 0); }
		public Action_right_sideContext action_right_side() {
			return getRuleContext(Action_right_sideContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public Action_ruleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_action_rule; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitAction_rule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Action_ruleContext action_rule() throws RecognitionException {
		Action_ruleContext _localctx = new Action_ruleContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_action_rule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			action_operation();
			setState(144);
			match(LPAREN);
			setState(145);
			match(STRING);
			setState(146);
			match(COMMA);
			setState(147);
			action_right_side();
			setState(148);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Action_operationContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(KstatesParser.SET, 0); }
		public TerminalNode APPEND() { return getToken(KstatesParser.APPEND, 0); }
		public TerminalNode EVENT() { return getToken(KstatesParser.EVENT, 0); }
		public Action_operationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_action_operation; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitAction_operation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Action_operationContext action_operation() throws RecognitionException {
		Action_operationContext _localctx = new Action_operationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_action_operation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3584L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Action_right_sideContext extends ParserRuleContext {
		public Macro_callContext macro_call() {
			return getRuleContext(Macro_callContext.class,0);
		}
		public Eval_statementContext eval_statement() {
			return getRuleContext(Eval_statementContext.class,0);
		}
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public Action_right_sideContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_action_right_side; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitAction_right_side(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Action_right_sideContext action_right_side() throws RecognitionException {
		Action_right_sideContext _localctx = new Action_right_sideContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_action_right_side);
		try {
			setState(155);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MACRO:
				enterOuterAlt(_localctx, 1);
				{
				setState(152);
				macro_call();
				}
				break;
			case EVAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(153);
				eval_statement();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 3);
				{
				setState(154);
				match(STRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Macro_callContext extends ParserRuleContext {
		public TerminalNode MACRO() { return getToken(KstatesParser.MACRO, 0); }
		public TerminalNode LPAREN() { return getToken(KstatesParser.LPAREN, 0); }
		public List<TerminalNode> STRING() { return getTokens(KstatesParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KstatesParser.STRING, i);
		}
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(KstatesParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KstatesParser.COMMA, i);
		}
		public Macro_callContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macro_call; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitMacro_call(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Macro_callContext macro_call() throws RecognitionException {
		Macro_callContext _localctx = new Macro_callContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_macro_call);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			match(MACRO);
			setState(158);
			match(LPAREN);
			setState(159);
			match(STRING);
			setState(164);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(160);
				match(COMMA);
				setState(161);
				match(STRING);
				}
				}
				setState(166);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(167);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Eval_statementContext extends ParserRuleContext {
		public TerminalNode EVAL() { return getToken(KstatesParser.EVAL, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public Eval_code_blockContext eval_code_block() {
			return getRuleContext(Eval_code_blockContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public Eval_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eval_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitEval_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Eval_statementContext eval_statement() throws RecognitionException {
		Eval_statementContext _localctx = new Eval_statementContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_eval_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			match(EVAL);
			setState(170);
			match(LBRACE);
			setState(171);
			eval_code_block();
			setState(172);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Transitions_blockContext extends ParserRuleContext {
		public TerminalNode TRANSITIONS() { return getToken(KstatesParser.TRANSITIONS, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public List<Transition_definitionContext> transition_definition() {
			return getRuleContexts(Transition_definitionContext.class);
		}
		public Transition_definitionContext transition_definition(int i) {
			return getRuleContext(Transition_definitionContext.class,i);
		}
		public Transitions_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transitions_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitTransitions_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Transitions_blockContext transitions_block() throws RecognitionException {
		Transitions_blockContext _localctx = new Transitions_blockContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_transitions_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(174);
			match(TRANSITIONS);
			setState(175);
			match(LBRACE);
			setState(179);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TRANSITION) {
				{
				{
				setState(176);
				transition_definition();
				}
				}
				setState(181);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(182);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Transition_definitionContext extends ParserRuleContext {
		public TerminalNode TRANSITION() { return getToken(KstatesParser.TRANSITION, 0); }
		public List<TerminalNode> STRING() { return getTokens(KstatesParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KstatesParser.STRING, i);
		}
		public TerminalNode ARROW() { return getToken(KstatesParser.ARROW, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public Not_loopingContext not_looping() {
			return getRuleContext(Not_loopingContext.class,0);
		}
		public Condition_blockContext condition_block() {
			return getRuleContext(Condition_blockContext.class,0);
		}
		public Transition_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transition_definition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitTransition_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Transition_definitionContext transition_definition() throws RecognitionException {
		Transition_definitionContext _localctx = new Transition_definitionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_transition_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			match(TRANSITION);
			setState(185);
			match(STRING);
			setState(186);
			match(ARROW);
			setState(187);
			match(STRING);
			setState(189);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(188);
				not_looping();
				}
			}

			setState(191);
			match(LBRACE);
			setState(193);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONDITION) {
				{
				setState(192);
				condition_block();
				}
			}

			setState(195);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Not_loopingContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(KstatesParser.NOT, 0); }
		public TerminalNode LOOPING() { return getToken(KstatesParser.LOOPING, 0); }
		public Not_loopingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not_looping; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitNot_looping(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Not_loopingContext not_looping() throws RecognitionException {
		Not_loopingContext _localctx = new Not_loopingContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_not_looping);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(NOT);
			setState(198);
			match(LOOPING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Condition_blockContext extends ParserRuleContext {
		public TerminalNode CONDITION() { return getToken(KstatesParser.CONDITION, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public List<Condition_clauseContext> condition_clause() {
			return getRuleContexts(Condition_clauseContext.class);
		}
		public Condition_clauseContext condition_clause(int i) {
			return getRuleContext(Condition_clauseContext.class,i);
		}
		public Condition_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitCondition_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Condition_blockContext condition_block() throws RecognitionException {
		Condition_blockContext _localctx = new Condition_blockContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_condition_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			match(CONDITION);
			setState(201);
			match(LBRACE);
			setState(203); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(202);
				condition_clause();
				}
				}
				setState(205); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 534528L) != 0) );
			setState(207);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Condition_clauseContext extends ParserRuleContext {
		public Event_conditionContext event_condition() {
			return getRuleContext(Event_conditionContext.class,0);
		}
		public Eval_statementContext eval_statement() {
			return getRuleContext(Eval_statementContext.class,0);
		}
		public Value_conditionContext value_condition() {
			return getRuleContext(Value_conditionContext.class,0);
		}
		public Condition_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition_clause; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitCondition_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Condition_clauseContext condition_clause() throws RecognitionException {
		Condition_clauseContext _localctx = new Condition_clauseContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_condition_clause);
		try {
			setState(212);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EVENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(209);
				event_condition();
				}
				break;
			case EVAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(210);
				eval_statement();
				}
				break;
			case VALUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(211);
				value_condition();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Event_conditionContext extends ParserRuleContext {
		public TerminalNode EVENT() { return getToken(KstatesParser.EVENT, 0); }
		public TerminalNode LPAREN() { return getToken(KstatesParser.LPAREN, 0); }
		public List<TerminalNode> STRING() { return getTokens(KstatesParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KstatesParser.STRING, i);
		}
		public TerminalNode COMMA() { return getToken(KstatesParser.COMMA, 0); }
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public Event_conditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_event_condition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitEvent_condition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Event_conditionContext event_condition() throws RecognitionException {
		Event_conditionContext _localctx = new Event_conditionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_event_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			match(EVENT);
			setState(215);
			match(LPAREN);
			setState(216);
			match(STRING);
			setState(217);
			match(COMMA);
			setState(218);
			match(STRING);
			setState(219);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Value_conditionContext extends ParserRuleContext {
		public TerminalNode VALUE() { return getToken(KstatesParser.VALUE, 0); }
		public TerminalNode LPAREN() { return getToken(KstatesParser.LPAREN, 0); }
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public Value_conditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value_condition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitValue_condition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Value_conditionContext value_condition() throws RecognitionException {
		Value_conditionContext _localctx = new Value_conditionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_value_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			match(VALUE);
			setState(222);
			match(LPAREN);
			setState(223);
			match(STRING);
			setState(224);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Macros_blockContext extends ParserRuleContext {
		public TerminalNode MACROS() { return getToken(KstatesParser.MACROS, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public List<Macro_definitionContext> macro_definition() {
			return getRuleContexts(Macro_definitionContext.class);
		}
		public Macro_definitionContext macro_definition(int i) {
			return getRuleContext(Macro_definitionContext.class,i);
		}
		public Macros_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macros_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitMacros_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Macros_blockContext macros_block() throws RecognitionException {
		Macros_blockContext _localctx = new Macros_blockContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_macros_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			match(MACROS);
			setState(227);
			match(LBRACE);
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MACRO) {
				{
				{
				setState(228);
				macro_definition();
				}
				}
				setState(233);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(234);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Macro_definitionContext extends ParserRuleContext {
		public TerminalNode MACRO() { return getToken(KstatesParser.MACRO, 0); }
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public Returns_directiveContext returns_directive() {
			return getRuleContext(Returns_directiveContext.class,0);
		}
		public Definition_blockContext definition_block() {
			return getRuleContext(Definition_blockContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public List<Takes_directiveContext> takes_directive() {
			return getRuleContexts(Takes_directiveContext.class);
		}
		public Takes_directiveContext takes_directive(int i) {
			return getRuleContext(Takes_directiveContext.class,i);
		}
		public Macro_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macro_definition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitMacro_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Macro_definitionContext macro_definition() throws RecognitionException {
		Macro_definitionContext _localctx = new Macro_definitionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_macro_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(236);
			match(MACRO);
			setState(237);
			match(STRING);
			setState(238);
			match(LBRACE);
			setState(242);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAKES) {
				{
				{
				setState(239);
				takes_directive();
				}
				}
				setState(244);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(245);
			returns_directive();
			setState(246);
			definition_block();
			setState(247);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Takes_directiveContext extends ParserRuleContext {
		public TerminalNode TAKES() { return getToken(KstatesParser.TAKES, 0); }
		public TerminalNode LPAREN() { return getToken(KstatesParser.LPAREN, 0); }
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public TerminalNode COMMA() { return getToken(KstatesParser.COMMA, 0); }
		public Type_expressionContext type_expression() {
			return getRuleContext(Type_expressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public Takes_directiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_takes_directive; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitTakes_directive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Takes_directiveContext takes_directive() throws RecognitionException {
		Takes_directiveContext _localctx = new Takes_directiveContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_takes_directive);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(249);
			match(TAKES);
			setState(250);
			match(LPAREN);
			setState(251);
			match(STRING);
			setState(252);
			match(COMMA);
			setState(253);
			type_expression();
			setState(254);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Returns_directiveContext extends ParserRuleContext {
		public TerminalNode RETURNS() { return getToken(KstatesParser.RETURNS, 0); }
		public TerminalNode LPAREN() { return getToken(KstatesParser.LPAREN, 0); }
		public Return_expressionContext return_expression() {
			return getRuleContext(Return_expressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public Returns_directiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returns_directive; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitReturns_directive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Returns_directiveContext returns_directive() throws RecognitionException {
		Returns_directiveContext _localctx = new Returns_directiveContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_returns_directive);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(256);
			match(RETURNS);
			setState(257);
			match(LPAREN);
			setState(258);
			return_expression();
			setState(259);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Return_expressionContext extends ParserRuleContext {
		public Type_expressionContext type_expression() {
			return getRuleContext(Type_expressionContext.class,0);
		}
		public Reference_expressionContext reference_expression() {
			return getRuleContext(Reference_expressionContext.class,0);
		}
		public Return_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_return_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitReturn_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Return_expressionContext return_expression() throws RecognitionException {
		Return_expressionContext _localctx = new Return_expressionContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_return_expression);
		try {
			setState(263);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
			case IDENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(261);
				type_expression();
				}
				break;
			case REFERENCE:
				enterOuterAlt(_localctx, 2);
				{
				setState(262);
				reference_expression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Type_expressionContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public Typed_call_expressionContext typed_call_expression() {
			return getRuleContext(Typed_call_expressionContext.class,0);
		}
		public Type_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitType_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Type_expressionContext type_expression() throws RecognitionException {
		Type_expressionContext _localctx = new Type_expressionContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_type_expression);
		try {
			setState(267);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(265);
				match(STRING);
				}
				break;
			case IDENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(266);
				typed_call_expression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Typed_call_expressionContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(KstatesParser.IDENT, 0); }
		public TerminalNode LPAREN() { return getToken(KstatesParser.LPAREN, 0); }
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public Typed_call_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typed_call_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitTyped_call_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Typed_call_expressionContext typed_call_expression() throws RecognitionException {
		Typed_call_expressionContext _localctx = new Typed_call_expressionContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_typed_call_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(269);
			match(IDENT);
			setState(270);
			match(LPAREN);
			setState(271);
			match(STRING);
			setState(272);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Reference_expressionContext extends ParserRuleContext {
		public TerminalNode REFERENCE() { return getToken(KstatesParser.REFERENCE, 0); }
		public TerminalNode LPAREN() { return getToken(KstatesParser.LPAREN, 0); }
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public Reference_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reference_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitReference_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Reference_expressionContext reference_expression() throws RecognitionException {
		Reference_expressionContext _localctx = new Reference_expressionContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_reference_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
			match(REFERENCE);
			setState(275);
			match(LPAREN);
			setState(276);
			match(STRING);
			setState(277);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Definition_blockContext extends ParserRuleContext {
		public TerminalNode DEFINITION() { return getToken(KstatesParser.DEFINITION, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public Eval_statementContext eval_statement() {
			return getRuleContext(Eval_statementContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public Definition_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_definition_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitDefinition_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Definition_blockContext definition_block() throws RecognitionException {
		Definition_blockContext _localctx = new Definition_blockContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_definition_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(279);
			match(DEFINITION);
			setState(280);
			match(LBRACE);
			setState(281);
			eval_statement();
			setState(282);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Eval_code_blockContext extends ParserRuleContext {
		public List<Code_contentContext> code_content() {
			return getRuleContexts(Code_contentContext.class);
		}
		public Code_contentContext code_content(int i) {
			return getRuleContext(Code_contentContext.class,i);
		}
		public Eval_code_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eval_code_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitEval_code_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Eval_code_blockContext eval_code_block() throws RecognitionException {
		Eval_code_blockContext _localctx = new Eval_code_blockContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_eval_code_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(287);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2197949513726L) != 0)) {
				{
				{
				setState(284);
				code_content();
				}
				}
				setState(289);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Code_contentContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public Eval_code_blockContext eval_code_block() {
			return getRuleContext(Eval_code_blockContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public Code_contentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_code_content; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitCode_content(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Code_contentContext code_content() throws RecognitionException {
		Code_contentContext _localctx = new Code_contentContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_code_content);
		int _la;
		try {
			setState(295);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(290);
				match(LBRACE);
				setState(291);
				eval_code_block();
				setState(292);
				match(RBRACE);
				}
				break;
			case STATEMACHINE:
			case ATTACHED:
			case TO:
			case STATES:
			case STATE:
			case INITIAL:
			case ENTRY:
			case DO:
			case SET:
			case APPEND:
			case EVENT:
			case MACRO:
			case EVAL:
			case TRANSITIONS:
			case TRANSITION:
			case NOT:
			case LOOPING:
			case CONDITION:
			case VALUE:
			case MACROS:
			case TAKES:
			case RETURNS:
			case DEFINITION:
			case REFERENCE:
			case IF:
			case IN:
			case SCOPE:
			case ARROW:
			case LPAREN:
			case RPAREN:
			case COMMA:
			case STRING:
			case SSTRING:
			case IDENT:
			case NUMBER:
			case WS:
			case LINE_COMMENT:
			case SYMBOL:
				enterOuterAlt(_localctx, 2);
				{
				setState(294);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==LBRACE || _la==RBRACE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001(\u012a\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001L\b\u0001\u0001\u0001\u0001\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0005\u0002S\b\u0002\n\u0002\f\u0002V\t\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0003\u0003[\b\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003a\b\u0003\u0001\u0003"+
		"\u0003\u0003d\b\u0003\u0001\u0003\u0005\u0003g\b\u0003\n\u0003\f\u0003"+
		"j\t\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0005\u0006"+
		"v\b\u0006\n\u0006\f\u0006y\t\u0006\u0001\u0006\u0001\u0006\u0001\u0007"+
		"\u0001\u0007\u0003\u0007\u007f\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b"+
		"\u0001\b\u0001\b\u0001\b\u0005\b\u0088\b\b\n\b\f\b\u008b\t\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u009c\b\u000b"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u00a3\b\f\n\f\f\f\u00a6"+
		"\t\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0005\u000e\u00b2\b\u000e\n\u000e\f\u000e\u00b5"+
		"\t\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0003\u000f\u00be\b\u000f\u0001\u000f\u0001\u000f\u0003"+
		"\u000f\u00c2\b\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0004\u0011\u00cc\b\u0011\u000b"+
		"\u0011\f\u0011\u00cd\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0003\u0012\u00d5\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0005"+
		"\u0015\u00e6\b\u0015\n\u0015\f\u0015\u00e9\t\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u00f1\b\u0016"+
		"\n\u0016\f\u0016\u00f4\t\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0019\u0001\u0019\u0003\u0019\u0108\b\u0019\u0001\u001a\u0001"+
		"\u001a\u0003\u001a\u010c\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001e\u0005\u001e\u011e\b\u001e\n\u001e\f\u001e\u0121\t\u001e\u0001\u001f"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u0128\b\u001f"+
		"\u0001\u001f\u0000\u0000 \u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>\u0000\u0002\u0001"+
		"\u0000\t\u000b\u0001\u0000\u001d\u001e\u0121\u0000@\u0001\u0000\u0000"+
		"\u0000\u0002C\u0001\u0000\u0000\u0000\u0004O\u0001\u0000\u0000\u0000\u0006"+
		"Z\u0001\u0000\u0000\u0000\bm\u0001\u0000\u0000\u0000\np\u0001\u0000\u0000"+
		"\u0000\fs\u0001\u0000\u0000\u0000\u000e~\u0001\u0000\u0000\u0000\u0010"+
		"\u0080\u0001\u0000\u0000\u0000\u0012\u008f\u0001\u0000\u0000\u0000\u0014"+
		"\u0096\u0001\u0000\u0000\u0000\u0016\u009b\u0001\u0000\u0000\u0000\u0018"+
		"\u009d\u0001\u0000\u0000\u0000\u001a\u00a9\u0001\u0000\u0000\u0000\u001c"+
		"\u00ae\u0001\u0000\u0000\u0000\u001e\u00b8\u0001\u0000\u0000\u0000 \u00c5"+
		"\u0001\u0000\u0000\u0000\"\u00c8\u0001\u0000\u0000\u0000$\u00d4\u0001"+
		"\u0000\u0000\u0000&\u00d6\u0001\u0000\u0000\u0000(\u00dd\u0001\u0000\u0000"+
		"\u0000*\u00e2\u0001\u0000\u0000\u0000,\u00ec\u0001\u0000\u0000\u0000."+
		"\u00f9\u0001\u0000\u0000\u00000\u0100\u0001\u0000\u0000\u00002\u0107\u0001"+
		"\u0000\u0000\u00004\u010b\u0001\u0000\u0000\u00006\u010d\u0001\u0000\u0000"+
		"\u00008\u0112\u0001\u0000\u0000\u0000:\u0117\u0001\u0000\u0000\u0000<"+
		"\u011f\u0001\u0000\u0000\u0000>\u0127\u0001\u0000\u0000\u0000@A\u0003"+
		"\u0002\u0001\u0000AB\u0005\u0000\u0000\u0001B\u0001\u0001\u0000\u0000"+
		"\u0000CD\u0005\u0001\u0000\u0000DE\u0005\u0002\u0000\u0000EF\u0005\u0003"+
		"\u0000\u0000FG\u0005\"\u0000\u0000GH\u0005\u001d\u0000\u0000HI\u0003\u0004"+
		"\u0002\u0000IK\u0003\u001c\u000e\u0000JL\u0003*\u0015\u0000KJ\u0001\u0000"+
		"\u0000\u0000KL\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000\u0000MN\u0005"+
		"\u001e\u0000\u0000N\u0003\u0001\u0000\u0000\u0000OP\u0005\u0004\u0000"+
		"\u0000PT\u0005\u001d\u0000\u0000QS\u0003\u0006\u0003\u0000RQ\u0001\u0000"+
		"\u0000\u0000SV\u0001\u0000\u0000\u0000TR\u0001\u0000\u0000\u0000TU\u0001"+
		"\u0000\u0000\u0000UW\u0001\u0000\u0000\u0000VT\u0001\u0000\u0000\u0000"+
		"WX\u0005\u001e\u0000\u0000X\u0005\u0001\u0000\u0000\u0000Y[\u0005\u0006"+
		"\u0000\u0000ZY\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[\\\u0001"+
		"\u0000\u0000\u0000\\]\u0005\u0005\u0000\u0000]^\u0005\"\u0000\u0000^`"+
		"\u0005\u001d\u0000\u0000_a\u0003\b\u0004\u0000`_\u0001\u0000\u0000\u0000"+
		"`a\u0001\u0000\u0000\u0000ac\u0001\u0000\u0000\u0000bd\u0003\n\u0005\u0000"+
		"cb\u0001\u0000\u0000\u0000cd\u0001\u0000\u0000\u0000dh\u0001\u0000\u0000"+
		"\u0000eg\u0003\u0006\u0003\u0000fe\u0001\u0000\u0000\u0000gj\u0001\u0000"+
		"\u0000\u0000hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000ik\u0001"+
		"\u0000\u0000\u0000jh\u0001\u0000\u0000\u0000kl\u0005\u001e\u0000\u0000"+
		"l\u0007\u0001\u0000\u0000\u0000mn\u0005\u0007\u0000\u0000no\u0003\f\u0006"+
		"\u0000o\t\u0001\u0000\u0000\u0000pq\u0005\b\u0000\u0000qr\u0003\f\u0006"+
		"\u0000r\u000b\u0001\u0000\u0000\u0000sw\u0005\u001d\u0000\u0000tv\u0003"+
		"\u000e\u0007\u0000ut\u0001\u0000\u0000\u0000vy\u0001\u0000\u0000\u0000"+
		"wu\u0001\u0000\u0000\u0000wx\u0001\u0000\u0000\u0000xz\u0001\u0000\u0000"+
		"\u0000yw\u0001\u0000\u0000\u0000z{\u0005\u001e\u0000\u0000{\r\u0001\u0000"+
		"\u0000\u0000|\u007f\u0003\u0012\t\u0000}\u007f\u0003\u0010\b\u0000~|\u0001"+
		"\u0000\u0000\u0000~}\u0001\u0000\u0000\u0000\u007f\u000f\u0001\u0000\u0000"+
		"\u0000\u0080\u0081\u0005\u0019\u0000\u0000\u0081\u0082\u0005\u001a\u0000"+
		"\u0000\u0082\u0083\u0005\u001b\u0000\u0000\u0083\u0084\u0005\u001f\u0000"+
		"\u0000\u0084\u0089\u0005\"\u0000\u0000\u0085\u0086\u0005!\u0000\u0000"+
		"\u0086\u0088\u0005\"\u0000\u0000\u0087\u0085\u0001\u0000\u0000\u0000\u0088"+
		"\u008b\u0001\u0000\u0000\u0000\u0089\u0087\u0001\u0000\u0000\u0000\u0089"+
		"\u008a\u0001\u0000\u0000\u0000\u008a\u008c\u0001\u0000\u0000\u0000\u008b"+
		"\u0089\u0001\u0000\u0000\u0000\u008c\u008d\u0005 \u0000\u0000\u008d\u008e"+
		"\u0003\f\u0006\u0000\u008e\u0011\u0001\u0000\u0000\u0000\u008f\u0090\u0003"+
		"\u0014\n\u0000\u0090\u0091\u0005\u001f\u0000\u0000\u0091\u0092\u0005\""+
		"\u0000\u0000\u0092\u0093\u0005!\u0000\u0000\u0093\u0094\u0003\u0016\u000b"+
		"\u0000\u0094\u0095\u0005 \u0000\u0000\u0095\u0013\u0001\u0000\u0000\u0000"+
		"\u0096\u0097\u0007\u0000\u0000\u0000\u0097\u0015\u0001\u0000\u0000\u0000"+
		"\u0098\u009c\u0003\u0018\f\u0000\u0099\u009c\u0003\u001a\r\u0000\u009a"+
		"\u009c\u0005\"\u0000\u0000\u009b\u0098\u0001\u0000\u0000\u0000\u009b\u0099"+
		"\u0001\u0000\u0000\u0000\u009b\u009a\u0001\u0000\u0000\u0000\u009c\u0017"+
		"\u0001\u0000\u0000\u0000\u009d\u009e\u0005\f\u0000\u0000\u009e\u009f\u0005"+
		"\u001f\u0000\u0000\u009f\u00a4\u0005\"\u0000\u0000\u00a0\u00a1\u0005!"+
		"\u0000\u0000\u00a1\u00a3\u0005\"\u0000\u0000\u00a2\u00a0\u0001\u0000\u0000"+
		"\u0000\u00a3\u00a6\u0001\u0000\u0000\u0000\u00a4\u00a2\u0001\u0000\u0000"+
		"\u0000\u00a4\u00a5\u0001\u0000\u0000\u0000\u00a5\u00a7\u0001\u0000\u0000"+
		"\u0000\u00a6\u00a4\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005 \u0000\u0000"+
		"\u00a8\u0019\u0001\u0000\u0000\u0000\u00a9\u00aa\u0005\r\u0000\u0000\u00aa"+
		"\u00ab\u0005\u001d\u0000\u0000\u00ab\u00ac\u0003<\u001e\u0000\u00ac\u00ad"+
		"\u0005\u001e\u0000\u0000\u00ad\u001b\u0001\u0000\u0000\u0000\u00ae\u00af"+
		"\u0005\u000e\u0000\u0000\u00af\u00b3\u0005\u001d\u0000\u0000\u00b0\u00b2"+
		"\u0003\u001e\u000f\u0000\u00b1\u00b0\u0001\u0000\u0000\u0000\u00b2\u00b5"+
		"\u0001\u0000\u0000\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000\u00b3\u00b4"+
		"\u0001\u0000\u0000\u0000\u00b4\u00b6\u0001\u0000\u0000\u0000\u00b5\u00b3"+
		"\u0001\u0000\u0000\u0000\u00b6\u00b7\u0005\u001e\u0000\u0000\u00b7\u001d"+
		"\u0001\u0000\u0000\u0000\u00b8\u00b9\u0005\u000f\u0000\u0000\u00b9\u00ba"+
		"\u0005\"\u0000\u0000\u00ba\u00bb\u0005\u001c\u0000\u0000\u00bb\u00bd\u0005"+
		"\"\u0000\u0000\u00bc\u00be\u0003 \u0010\u0000\u00bd\u00bc\u0001\u0000"+
		"\u0000\u0000\u00bd\u00be\u0001\u0000\u0000\u0000\u00be\u00bf\u0001\u0000"+
		"\u0000\u0000\u00bf\u00c1\u0005\u001d\u0000\u0000\u00c0\u00c2\u0003\"\u0011"+
		"\u0000\u00c1\u00c0\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000"+
		"\u0000\u00c2\u00c3\u0001\u0000\u0000\u0000\u00c3\u00c4\u0005\u001e\u0000"+
		"\u0000\u00c4\u001f\u0001\u0000\u0000\u0000\u00c5\u00c6\u0005\u0010\u0000"+
		"\u0000\u00c6\u00c7\u0005\u0011\u0000\u0000\u00c7!\u0001\u0000\u0000\u0000"+
		"\u00c8\u00c9\u0005\u0012\u0000\u0000\u00c9\u00cb\u0005\u001d\u0000\u0000"+
		"\u00ca\u00cc\u0003$\u0012\u0000\u00cb\u00ca\u0001\u0000\u0000\u0000\u00cc"+
		"\u00cd\u0001\u0000\u0000\u0000\u00cd\u00cb\u0001\u0000\u0000\u0000\u00cd"+
		"\u00ce\u0001\u0000\u0000\u0000\u00ce\u00cf\u0001\u0000\u0000\u0000\u00cf"+
		"\u00d0\u0005\u001e\u0000\u0000\u00d0#\u0001\u0000\u0000\u0000\u00d1\u00d5"+
		"\u0003&\u0013\u0000\u00d2\u00d5\u0003\u001a\r\u0000\u00d3\u00d5\u0003"+
		"(\u0014\u0000\u00d4\u00d1\u0001\u0000\u0000\u0000\u00d4\u00d2\u0001\u0000"+
		"\u0000\u0000\u00d4\u00d3\u0001\u0000\u0000\u0000\u00d5%\u0001\u0000\u0000"+
		"\u0000\u00d6\u00d7\u0005\u000b\u0000\u0000\u00d7\u00d8\u0005\u001f\u0000"+
		"\u0000\u00d8\u00d9\u0005\"\u0000\u0000\u00d9\u00da\u0005!\u0000\u0000"+
		"\u00da\u00db\u0005\"\u0000\u0000\u00db\u00dc\u0005 \u0000\u0000\u00dc"+
		"\'\u0001\u0000\u0000\u0000\u00dd\u00de\u0005\u0013\u0000\u0000\u00de\u00df"+
		"\u0005\u001f\u0000\u0000\u00df\u00e0\u0005\"\u0000\u0000\u00e0\u00e1\u0005"+
		" \u0000\u0000\u00e1)\u0001\u0000\u0000\u0000\u00e2\u00e3\u0005\u0014\u0000"+
		"\u0000\u00e3\u00e7\u0005\u001d\u0000\u0000\u00e4\u00e6\u0003,\u0016\u0000"+
		"\u00e5\u00e4\u0001\u0000\u0000\u0000\u00e6\u00e9\u0001\u0000\u0000\u0000"+
		"\u00e7\u00e5\u0001\u0000\u0000\u0000\u00e7\u00e8\u0001\u0000\u0000\u0000"+
		"\u00e8\u00ea\u0001\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000"+
		"\u00ea\u00eb\u0005\u001e\u0000\u0000\u00eb+\u0001\u0000\u0000\u0000\u00ec"+
		"\u00ed\u0005\f\u0000\u0000\u00ed\u00ee\u0005\"\u0000\u0000\u00ee\u00f2"+
		"\u0005\u001d\u0000\u0000\u00ef\u00f1\u0003.\u0017\u0000\u00f0\u00ef\u0001"+
		"\u0000\u0000\u0000\u00f1\u00f4\u0001\u0000\u0000\u0000\u00f2\u00f0\u0001"+
		"\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3\u00f5\u0001"+
		"\u0000\u0000\u0000\u00f4\u00f2\u0001\u0000\u0000\u0000\u00f5\u00f6\u0003"+
		"0\u0018\u0000\u00f6\u00f7\u0003:\u001d\u0000\u00f7\u00f8\u0005\u001e\u0000"+
		"\u0000\u00f8-\u0001\u0000\u0000\u0000\u00f9\u00fa\u0005\u0015\u0000\u0000"+
		"\u00fa\u00fb\u0005\u001f\u0000\u0000\u00fb\u00fc\u0005\"\u0000\u0000\u00fc"+
		"\u00fd\u0005!\u0000\u0000\u00fd\u00fe\u00034\u001a\u0000\u00fe\u00ff\u0005"+
		" \u0000\u0000\u00ff/\u0001\u0000\u0000\u0000\u0100\u0101\u0005\u0016\u0000"+
		"\u0000\u0101\u0102\u0005\u001f\u0000\u0000\u0102\u0103\u00032\u0019\u0000"+
		"\u0103\u0104\u0005 \u0000\u0000\u01041\u0001\u0000\u0000\u0000\u0105\u0108"+
		"\u00034\u001a\u0000\u0106\u0108\u00038\u001c\u0000\u0107\u0105\u0001\u0000"+
		"\u0000\u0000\u0107\u0106\u0001\u0000\u0000\u0000\u01083\u0001\u0000\u0000"+
		"\u0000\u0109\u010c\u0005\"\u0000\u0000\u010a\u010c\u00036\u001b\u0000"+
		"\u010b\u0109\u0001\u0000\u0000\u0000\u010b\u010a\u0001\u0000\u0000\u0000"+
		"\u010c5\u0001\u0000\u0000\u0000\u010d\u010e\u0005$\u0000\u0000\u010e\u010f"+
		"\u0005\u001f\u0000\u0000\u010f\u0110\u0005\"\u0000\u0000\u0110\u0111\u0005"+
		" \u0000\u0000\u01117\u0001\u0000\u0000\u0000\u0112\u0113\u0005\u0018\u0000"+
		"\u0000\u0113\u0114\u0005\u001f\u0000\u0000\u0114\u0115\u0005\"\u0000\u0000"+
		"\u0115\u0116\u0005 \u0000\u0000\u01169\u0001\u0000\u0000\u0000\u0117\u0118"+
		"\u0005\u0017\u0000\u0000\u0118\u0119\u0005\u001d\u0000\u0000\u0119\u011a"+
		"\u0003\u001a\r\u0000\u011a\u011b\u0005\u001e\u0000\u0000\u011b;\u0001"+
		"\u0000\u0000\u0000\u011c\u011e\u0003>\u001f\u0000\u011d\u011c\u0001\u0000"+
		"\u0000\u0000\u011e\u0121\u0001\u0000\u0000\u0000\u011f\u011d\u0001\u0000"+
		"\u0000\u0000\u011f\u0120\u0001\u0000\u0000\u0000\u0120=\u0001\u0000\u0000"+
		"\u0000\u0121\u011f\u0001\u0000\u0000\u0000\u0122\u0123\u0005\u001d\u0000"+
		"\u0000\u0123\u0124\u0003<\u001e\u0000\u0124\u0125\u0005\u001e\u0000\u0000"+
		"\u0125\u0128\u0001\u0000\u0000\u0000\u0126\u0128\b\u0001\u0000\u0000\u0127"+
		"\u0122\u0001\u0000\u0000\u0000\u0127\u0126\u0001\u0000\u0000\u0000\u0128"+
		"?\u0001\u0000\u0000\u0000\u0016KTZ`chw~\u0089\u009b\u00a4\u00b3\u00bd"+
		"\u00c1\u00cd\u00d4\u00e7\u00f2\u0107\u010b\u011f\u0127";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}