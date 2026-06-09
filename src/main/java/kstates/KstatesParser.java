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
		DO=8, SET=9, SETLIST=10, DROPLIST=11, APPEND=12, SETOBJ=13, APPENDOBJ=14, 
		DROPOBJ=15, EVENT=16, WITH=17, AS=18, MACRO=19, EVAL=20, TRANSITIONS=21, 
		TRANSITION=22, SPLIT=23, JOIN=24, NOT=25, LOOPING=26, CONDITION=27, VALUE=28, 
		MACROS=29, TAKES=30, RETURNS=31, DEFINITION=32, REFERENCE=33, IF=34, IN=35, 
		SCOPE=36, ARROW=37, LBRACE=38, RBRACE=39, LPAREN=40, RPAREN=41, COMMA=42, 
		STRING=43, SSTRING=44, IDENT=45, NUMBER=46, WS=47, LINE_COMMENT=48, SYMBOL=49;
	public static final int
		RULE_kstates_file = 0, RULE_statemachine = 1, RULE_states_block = 2, RULE_state_definition = 3, 
		RULE_entry_block = 4, RULE_do_block = 5, RULE_action_block = 6, RULE_action_item = 7, 
		RULE_in_scope_block = 8, RULE_with_block = 9, RULE_action_rule = 10, RULE_action_operation = 11, 
		RULE_action_right_side = 12, RULE_macro_call = 13, RULE_eval_statement = 14, 
		RULE_transitions_block = 15, RULE_transition_definition = 16, RULE_normal_transition = 17, 
		RULE_split_transition = 18, RULE_join_transition = 19, RULE_not_looping = 20, 
		RULE_condition_block = 21, RULE_condition_clause = 22, RULE_event_condition = 23, 
		RULE_value_condition = 24, RULE_macros_block = 25, RULE_macro_definition = 26, 
		RULE_takes_directive = 27, RULE_returns_directive = 28, RULE_return_expression = 29, 
		RULE_type_expression = 30, RULE_typed_call_expression = 31, RULE_reference_expression = 32, 
		RULE_definition_block = 33, RULE_eval_code_block = 34, RULE_code_content = 35;
	private static String[] makeRuleNames() {
		return new String[] {
			"kstates_file", "statemachine", "states_block", "state_definition", "entry_block", 
			"do_block", "action_block", "action_item", "in_scope_block", "with_block", 
			"action_rule", "action_operation", "action_right_side", "macro_call", 
			"eval_statement", "transitions_block", "transition_definition", "normal_transition", 
			"split_transition", "join_transition", "not_looping", "condition_block", 
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
			"'INITIAL'", "'ENTRY'", "'DO'", null, null, null, null, null, null, null, 
			"'EVENT'", null, null, "'MACRO'", "'EVAL'", "'TRANSITIONS'", "'TRANSITION'", 
			"'SPLIT'", "'JOIN'", "'NOT'", "'LOOPING'", "'CONDITION'", "'VALUE'", 
			"'MACROS'", "'TAKES'", "'RETURNS'", "'DEFINITION'", "'reference'", "'IF'", 
			"'IN'", "'SCOPE'", "'->'", "'{'", "'}'", "'('", "')'", "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "STATEMACHINE", "ATTACHED", "TO", "STATES", "STATE", "INITIAL", 
			"ENTRY", "DO", "SET", "SETLIST", "DROPLIST", "APPEND", "SETOBJ", "APPENDOBJ", 
			"DROPOBJ", "EVENT", "WITH", "AS", "MACRO", "EVAL", "TRANSITIONS", "TRANSITION", 
			"SPLIT", "JOIN", "NOT", "LOOPING", "CONDITION", "VALUE", "MACROS", "TAKES", 
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
			setState(72);
			statemachine();
			setState(73);
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
			setState(75);
			match(STATEMACHINE);
			setState(76);
			match(ATTACHED);
			setState(77);
			match(TO);
			setState(78);
			match(STRING);
			setState(79);
			match(LBRACE);
			setState(80);
			states_block();
			setState(81);
			transitions_block();
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MACROS) {
				{
				setState(82);
				macros_block();
				}
			}

			setState(85);
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
			setState(87);
			match(STATES);
			setState(88);
			match(LBRACE);
			setState(92);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==STATE || _la==INITIAL) {
				{
				{
				setState(89);
				state_definition();
				}
				}
				setState(94);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(95);
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
			setState(98);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INITIAL) {
				{
				setState(97);
				match(INITIAL);
				}
			}

			setState(100);
			match(STATE);
			setState(101);
			match(STRING);
			setState(102);
			match(LBRACE);
			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ENTRY) {
				{
				setState(103);
				entry_block();
				}
			}

			setState(107);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DO) {
				{
				setState(106);
				do_block();
				}
			}

			setState(112);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==STATE || _la==INITIAL) {
				{
				{
				setState(109);
				state_definition();
				}
				}
				setState(114);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(115);
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
			setState(117);
			match(ENTRY);
			setState(118);
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
			setState(120);
			match(DO);
			setState(121);
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
			setState(123);
			match(LBRACE);
			setState(127);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 17180130816L) != 0)) {
				{
				{
				setState(124);
				action_item();
				}
				}
				setState(129);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(130);
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
		public With_blockContext with_block() {
			return getRuleContext(With_blockContext.class,0);
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
			setState(135);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SET:
			case SETLIST:
			case DROPLIST:
			case APPEND:
			case SETOBJ:
			case APPENDOBJ:
			case DROPOBJ:
			case EVENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(132);
				action_rule();
				}
				break;
			case IF:
				enterOuterAlt(_localctx, 2);
				{
				setState(133);
				in_scope_block();
				}
				break;
			case WITH:
				enterOuterAlt(_localctx, 3);
				{
				setState(134);
				with_block();
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
			setState(137);
			match(IF);
			setState(138);
			match(IN);
			setState(139);
			match(SCOPE);
			setState(140);
			match(LPAREN);
			setState(141);
			match(STRING);
			setState(146);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(142);
				match(COMMA);
				setState(143);
				match(STRING);
				}
				}
				setState(148);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(149);
			match(RPAREN);
			setState(150);
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
	public static class With_blockContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(KstatesParser.WITH, 0); }
		public Macro_callContext macro_call() {
			return getRuleContext(Macro_callContext.class,0);
		}
		public TerminalNode AS() { return getToken(KstatesParser.AS, 0); }
		public TerminalNode STRING() { return getToken(KstatesParser.STRING, 0); }
		public Action_blockContext action_block() {
			return getRuleContext(Action_blockContext.class,0);
		}
		public With_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_with_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitWith_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final With_blockContext with_block() throws RecognitionException {
		With_blockContext _localctx = new With_blockContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_with_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152);
			match(WITH);
			setState(153);
			macro_call();
			setState(154);
			match(AS);
			setState(155);
			match(STRING);
			setState(156);
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
		public List<TerminalNode> STRING() { return getTokens(KstatesParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KstatesParser.STRING, i);
		}
		public TerminalNode RPAREN() { return getToken(KstatesParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(KstatesParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KstatesParser.COMMA, i);
		}
		public Action_right_sideContext action_right_side() {
			return getRuleContext(Action_right_sideContext.class,0);
		}
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
		enterRule(_localctx, 20, RULE_action_rule);
		try {
			setState(179);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(158);
				action_operation();
				setState(159);
				match(LPAREN);
				setState(160);
				match(STRING);
				setState(161);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
				action_operation();
				setState(164);
				match(LPAREN);
				setState(165);
				match(STRING);
				setState(166);
				match(COMMA);
				setState(167);
				action_right_side();
				setState(168);
				match(RPAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(170);
				action_operation();
				setState(171);
				match(LPAREN);
				setState(172);
				match(STRING);
				setState(173);
				match(COMMA);
				setState(174);
				match(STRING);
				setState(175);
				match(COMMA);
				setState(176);
				action_right_side();
				setState(177);
				match(RPAREN);
				}
				break;
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
		public TerminalNode SETLIST() { return getToken(KstatesParser.SETLIST, 0); }
		public TerminalNode DROPLIST() { return getToken(KstatesParser.DROPLIST, 0); }
		public TerminalNode SETOBJ() { return getToken(KstatesParser.SETOBJ, 0); }
		public TerminalNode APPENDOBJ() { return getToken(KstatesParser.APPENDOBJ, 0); }
		public TerminalNode DROPOBJ() { return getToken(KstatesParser.DROPOBJ, 0); }
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
		enterRule(_localctx, 22, RULE_action_operation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 130560L) != 0)) ) {
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
		enterRule(_localctx, 24, RULE_action_right_side);
		try {
			setState(186);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MACRO:
				enterOuterAlt(_localctx, 1);
				{
				setState(183);
				macro_call();
				}
				break;
			case EVAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(184);
				eval_statement();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 3);
				{
				setState(185);
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
		enterRule(_localctx, 26, RULE_macro_call);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			match(MACRO);
			setState(189);
			match(LPAREN);
			setState(190);
			match(STRING);
			setState(195);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(191);
				match(COMMA);
				setState(192);
				match(STRING);
				}
				}
				setState(197);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(198);
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
		enterRule(_localctx, 28, RULE_eval_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			match(EVAL);
			setState(201);
			match(LBRACE);
			setState(202);
			eval_code_block();
			setState(203);
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
		enterRule(_localctx, 30, RULE_transitions_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205);
			match(TRANSITIONS);
			setState(206);
			match(LBRACE);
			setState(210);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 29360128L) != 0)) {
				{
				{
				setState(207);
				transition_definition();
				}
				}
				setState(212);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(213);
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
		public Normal_transitionContext normal_transition() {
			return getRuleContext(Normal_transitionContext.class,0);
		}
		public Split_transitionContext split_transition() {
			return getRuleContext(Split_transitionContext.class,0);
		}
		public Join_transitionContext join_transition() {
			return getRuleContext(Join_transitionContext.class,0);
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
		enterRule(_localctx, 32, RULE_transition_definition);
		try {
			setState(218);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRANSITION:
				enterOuterAlt(_localctx, 1);
				{
				setState(215);
				normal_transition();
				}
				break;
			case SPLIT:
				enterOuterAlt(_localctx, 2);
				{
				setState(216);
				split_transition();
				}
				break;
			case JOIN:
				enterOuterAlt(_localctx, 3);
				{
				setState(217);
				join_transition();
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
	public static class Normal_transitionContext extends ParserRuleContext {
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
		public Normal_transitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_normal_transition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitNormal_transition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Normal_transitionContext normal_transition() throws RecognitionException {
		Normal_transitionContext _localctx = new Normal_transitionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_normal_transition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(220);
			match(TRANSITION);
			setState(221);
			match(STRING);
			setState(222);
			match(ARROW);
			setState(223);
			match(STRING);
			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(224);
				not_looping();
				}
			}

			setState(227);
			match(LBRACE);
			setState(229);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONDITION) {
				{
				setState(228);
				condition_block();
				}
			}

			setState(231);
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
	public static class Split_transitionContext extends ParserRuleContext {
		public TerminalNode SPLIT() { return getToken(KstatesParser.SPLIT, 0); }
		public List<TerminalNode> STRING() { return getTokens(KstatesParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KstatesParser.STRING, i);
		}
		public TerminalNode ARROW() { return getToken(KstatesParser.ARROW, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(KstatesParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KstatesParser.COMMA, i);
		}
		public Not_loopingContext not_looping() {
			return getRuleContext(Not_loopingContext.class,0);
		}
		public Condition_blockContext condition_block() {
			return getRuleContext(Condition_blockContext.class,0);
		}
		public Split_transitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_split_transition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitSplit_transition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Split_transitionContext split_transition() throws RecognitionException {
		Split_transitionContext _localctx = new Split_transitionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_split_transition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			match(SPLIT);
			setState(234);
			match(STRING);
			setState(235);
			match(ARROW);
			setState(236);
			match(STRING);
			setState(239); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(237);
				match(COMMA);
				setState(238);
				match(STRING);
				}
				}
				setState(241); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==COMMA );
			setState(244);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(243);
				not_looping();
				}
			}

			setState(246);
			match(LBRACE);
			setState(248);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONDITION) {
				{
				setState(247);
				condition_block();
				}
			}

			setState(250);
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
	public static class Join_transitionContext extends ParserRuleContext {
		public TerminalNode JOIN() { return getToken(KstatesParser.JOIN, 0); }
		public List<TerminalNode> STRING() { return getTokens(KstatesParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KstatesParser.STRING, i);
		}
		public TerminalNode ARROW() { return getToken(KstatesParser.ARROW, 0); }
		public TerminalNode LBRACE() { return getToken(KstatesParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KstatesParser.RBRACE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(KstatesParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KstatesParser.COMMA, i);
		}
		public Not_loopingContext not_looping() {
			return getRuleContext(Not_loopingContext.class,0);
		}
		public Condition_blockContext condition_block() {
			return getRuleContext(Condition_blockContext.class,0);
		}
		public Join_transitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join_transition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KstatesVisitor ) return ((KstatesVisitor<? extends T>)visitor).visitJoin_transition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Join_transitionContext join_transition() throws RecognitionException {
		Join_transitionContext _localctx = new Join_transitionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_join_transition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(252);
			match(JOIN);
			setState(253);
			match(STRING);
			setState(256); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(254);
				match(COMMA);
				setState(255);
				match(STRING);
				}
				}
				setState(258); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==COMMA );
			setState(260);
			match(ARROW);
			setState(261);
			match(STRING);
			setState(263);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(262);
				not_looping();
				}
			}

			setState(265);
			match(LBRACE);
			setState(267);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONDITION) {
				{
				setState(266);
				condition_block();
				}
			}

			setState(269);
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
		enterRule(_localctx, 40, RULE_not_looping);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(271);
			match(NOT);
			setState(272);
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
		enterRule(_localctx, 42, RULE_condition_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
			match(CONDITION);
			setState(275);
			match(LBRACE);
			setState(277); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(276);
				condition_clause();
				}
				}
				setState(279); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 269549568L) != 0) );
			setState(281);
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
		enterRule(_localctx, 44, RULE_condition_clause);
		try {
			setState(286);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EVENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(283);
				event_condition();
				}
				break;
			case EVAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(284);
				eval_statement();
				}
				break;
			case VALUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(285);
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
		enterRule(_localctx, 46, RULE_event_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(288);
			match(EVENT);
			setState(289);
			match(LPAREN);
			setState(290);
			match(STRING);
			setState(291);
			match(COMMA);
			setState(292);
			match(STRING);
			setState(293);
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
		enterRule(_localctx, 48, RULE_value_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(295);
			match(VALUE);
			setState(296);
			match(LPAREN);
			setState(297);
			match(STRING);
			setState(298);
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
		enterRule(_localctx, 50, RULE_macros_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(300);
			match(MACROS);
			setState(301);
			match(LBRACE);
			setState(305);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MACRO) {
				{
				{
				setState(302);
				macro_definition();
				}
				}
				setState(307);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(308);
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
		enterRule(_localctx, 52, RULE_macro_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			match(MACRO);
			setState(311);
			match(STRING);
			setState(312);
			match(LBRACE);
			setState(316);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAKES) {
				{
				{
				setState(313);
				takes_directive();
				}
				}
				setState(318);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(319);
			returns_directive();
			setState(320);
			definition_block();
			setState(321);
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
		enterRule(_localctx, 54, RULE_takes_directive);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(323);
			match(TAKES);
			setState(324);
			match(LPAREN);
			setState(325);
			match(STRING);
			setState(326);
			match(COMMA);
			setState(327);
			type_expression();
			setState(328);
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
		enterRule(_localctx, 56, RULE_returns_directive);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(330);
			match(RETURNS);
			setState(331);
			match(LPAREN);
			setState(332);
			return_expression();
			setState(333);
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
		enterRule(_localctx, 58, RULE_return_expression);
		try {
			setState(337);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
			case IDENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(335);
				type_expression();
				}
				break;
			case REFERENCE:
				enterOuterAlt(_localctx, 2);
				{
				setState(336);
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
		enterRule(_localctx, 60, RULE_type_expression);
		try {
			setState(341);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(339);
				match(STRING);
				}
				break;
			case IDENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(340);
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
		enterRule(_localctx, 62, RULE_typed_call_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(343);
			match(IDENT);
			setState(344);
			match(LPAREN);
			setState(345);
			match(STRING);
			setState(346);
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
		enterRule(_localctx, 64, RULE_reference_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(348);
			match(REFERENCE);
			setState(349);
			match(LPAREN);
			setState(350);
			match(STRING);
			setState(351);
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
		enterRule(_localctx, 66, RULE_definition_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(353);
			match(DEFINITION);
			setState(354);
			match(LBRACE);
			setState(355);
			eval_statement();
			setState(356);
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
		enterRule(_localctx, 68, RULE_eval_code_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1125350151028734L) != 0)) {
				{
				{
				setState(358);
				code_content();
				}
				}
				setState(363);
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
		enterRule(_localctx, 70, RULE_code_content);
		int _la;
		try {
			setState(369);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(364);
				match(LBRACE);
				setState(365);
				eval_code_block();
				setState(366);
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
			case SETLIST:
			case DROPLIST:
			case APPEND:
			case SETOBJ:
			case APPENDOBJ:
			case DROPOBJ:
			case EVENT:
			case WITH:
			case AS:
			case MACRO:
			case EVAL:
			case TRANSITIONS:
			case TRANSITION:
			case SPLIT:
			case JOIN:
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
				setState(368);
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
		"\u0004\u00011\u0174\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u0001T\b\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0005\u0002[\b\u0002\n\u0002\f\u0002^\t\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0003\u0003\u0003c\b\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0003\u0003i\b\u0003\u0001\u0003\u0003\u0003l\b\u0003"+
		"\u0001\u0003\u0005\u0003o\b\u0003\n\u0003\f\u0003r\t\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0006\u0001\u0006\u0005\u0006~\b\u0006\n\u0006\f\u0006"+
		"\u0081\t\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0003\u0007\u0088\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0005\b\u0091\b\b\n\b\f\b\u0094\t\b\u0001\b\u0001\b\u0001\b"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0003\n\u00b4\b\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0003"+
		"\f\u00bb\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0005\r\u00c2\b\r"+
		"\n\r\f\r\u00c5\t\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f"+
		"\u00d1\b\u000f\n\u000f\f\u000f\u00d4\t\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u00db\b\u0010\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u00e2\b\u0011\u0001"+
		"\u0011\u0001\u0011\u0003\u0011\u00e6\b\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0004"+
		"\u0012\u00f0\b\u0012\u000b\u0012\f\u0012\u00f1\u0001\u0012\u0003\u0012"+
		"\u00f5\b\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00f9\b\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0004"+
		"\u0013\u0101\b\u0013\u000b\u0013\f\u0013\u0102\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0003\u0013\u0108\b\u0013\u0001\u0013\u0001\u0013\u0003\u0013"+
		"\u010c\b\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0004\u0015\u0116\b\u0015\u000b\u0015"+
		"\f\u0015\u0117\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u011f\b\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019"+
		"\u0130\b\u0019\n\u0019\f\u0019\u0133\t\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a\u013b\b\u001a\n"+
		"\u001a\f\u001a\u013e\t\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001d\u0001\u001d\u0003\u001d\u0152\b\u001d\u0001\u001e\u0001"+
		"\u001e\u0003\u001e\u0156\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0001 \u0001 \u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001\"\u0005\"\u0168\b\"\n\"\f\"\u016b\t\"\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0003#\u0172\b#\u0001#\u0000\u0000$\u0000\u0002"+
		"\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e"+
		" \"$&(*,.02468:<>@BDF\u0000\u0002\u0001\u0000\t\u0010\u0001\u0000&\'\u0172"+
		"\u0000H\u0001\u0000\u0000\u0000\u0002K\u0001\u0000\u0000\u0000\u0004W"+
		"\u0001\u0000\u0000\u0000\u0006b\u0001\u0000\u0000\u0000\bu\u0001\u0000"+
		"\u0000\u0000\nx\u0001\u0000\u0000\u0000\f{\u0001\u0000\u0000\u0000\u000e"+
		"\u0087\u0001\u0000\u0000\u0000\u0010\u0089\u0001\u0000\u0000\u0000\u0012"+
		"\u0098\u0001\u0000\u0000\u0000\u0014\u00b3\u0001\u0000\u0000\u0000\u0016"+
		"\u00b5\u0001\u0000\u0000\u0000\u0018\u00ba\u0001\u0000\u0000\u0000\u001a"+
		"\u00bc\u0001\u0000\u0000\u0000\u001c\u00c8\u0001\u0000\u0000\u0000\u001e"+
		"\u00cd\u0001\u0000\u0000\u0000 \u00da\u0001\u0000\u0000\u0000\"\u00dc"+
		"\u0001\u0000\u0000\u0000$\u00e9\u0001\u0000\u0000\u0000&\u00fc\u0001\u0000"+
		"\u0000\u0000(\u010f\u0001\u0000\u0000\u0000*\u0112\u0001\u0000\u0000\u0000"+
		",\u011e\u0001\u0000\u0000\u0000.\u0120\u0001\u0000\u0000\u00000\u0127"+
		"\u0001\u0000\u0000\u00002\u012c\u0001\u0000\u0000\u00004\u0136\u0001\u0000"+
		"\u0000\u00006\u0143\u0001\u0000\u0000\u00008\u014a\u0001\u0000\u0000\u0000"+
		":\u0151\u0001\u0000\u0000\u0000<\u0155\u0001\u0000\u0000\u0000>\u0157"+
		"\u0001\u0000\u0000\u0000@\u015c\u0001\u0000\u0000\u0000B\u0161\u0001\u0000"+
		"\u0000\u0000D\u0169\u0001\u0000\u0000\u0000F\u0171\u0001\u0000\u0000\u0000"+
		"HI\u0003\u0002\u0001\u0000IJ\u0005\u0000\u0000\u0001J\u0001\u0001\u0000"+
		"\u0000\u0000KL\u0005\u0001\u0000\u0000LM\u0005\u0002\u0000\u0000MN\u0005"+
		"\u0003\u0000\u0000NO\u0005+\u0000\u0000OP\u0005&\u0000\u0000PQ\u0003\u0004"+
		"\u0002\u0000QS\u0003\u001e\u000f\u0000RT\u00032\u0019\u0000SR\u0001\u0000"+
		"\u0000\u0000ST\u0001\u0000\u0000\u0000TU\u0001\u0000\u0000\u0000UV\u0005"+
		"\'\u0000\u0000V\u0003\u0001\u0000\u0000\u0000WX\u0005\u0004\u0000\u0000"+
		"X\\\u0005&\u0000\u0000Y[\u0003\u0006\u0003\u0000ZY\u0001\u0000\u0000\u0000"+
		"[^\u0001\u0000\u0000\u0000\\Z\u0001\u0000\u0000\u0000\\]\u0001\u0000\u0000"+
		"\u0000]_\u0001\u0000\u0000\u0000^\\\u0001\u0000\u0000\u0000_`\u0005\'"+
		"\u0000\u0000`\u0005\u0001\u0000\u0000\u0000ac\u0005\u0006\u0000\u0000"+
		"ba\u0001\u0000\u0000\u0000bc\u0001\u0000\u0000\u0000cd\u0001\u0000\u0000"+
		"\u0000de\u0005\u0005\u0000\u0000ef\u0005+\u0000\u0000fh\u0005&\u0000\u0000"+
		"gi\u0003\b\u0004\u0000hg\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000"+
		"ik\u0001\u0000\u0000\u0000jl\u0003\n\u0005\u0000kj\u0001\u0000\u0000\u0000"+
		"kl\u0001\u0000\u0000\u0000lp\u0001\u0000\u0000\u0000mo\u0003\u0006\u0003"+
		"\u0000nm\u0001\u0000\u0000\u0000or\u0001\u0000\u0000\u0000pn\u0001\u0000"+
		"\u0000\u0000pq\u0001\u0000\u0000\u0000qs\u0001\u0000\u0000\u0000rp\u0001"+
		"\u0000\u0000\u0000st\u0005\'\u0000\u0000t\u0007\u0001\u0000\u0000\u0000"+
		"uv\u0005\u0007\u0000\u0000vw\u0003\f\u0006\u0000w\t\u0001\u0000\u0000"+
		"\u0000xy\u0005\b\u0000\u0000yz\u0003\f\u0006\u0000z\u000b\u0001\u0000"+
		"\u0000\u0000{\u007f\u0005&\u0000\u0000|~\u0003\u000e\u0007\u0000}|\u0001"+
		"\u0000\u0000\u0000~\u0081\u0001\u0000\u0000\u0000\u007f}\u0001\u0000\u0000"+
		"\u0000\u007f\u0080\u0001\u0000\u0000\u0000\u0080\u0082\u0001\u0000\u0000"+
		"\u0000\u0081\u007f\u0001\u0000\u0000\u0000\u0082\u0083\u0005\'\u0000\u0000"+
		"\u0083\r\u0001\u0000\u0000\u0000\u0084\u0088\u0003\u0014\n\u0000\u0085"+
		"\u0088\u0003\u0010\b\u0000\u0086\u0088\u0003\u0012\t\u0000\u0087\u0084"+
		"\u0001\u0000\u0000\u0000\u0087\u0085\u0001\u0000\u0000\u0000\u0087\u0086"+
		"\u0001\u0000\u0000\u0000\u0088\u000f\u0001\u0000\u0000\u0000\u0089\u008a"+
		"\u0005\"\u0000\u0000\u008a\u008b\u0005#\u0000\u0000\u008b\u008c\u0005"+
		"$\u0000\u0000\u008c\u008d\u0005(\u0000\u0000\u008d\u0092\u0005+\u0000"+
		"\u0000\u008e\u008f\u0005*\u0000\u0000\u008f\u0091\u0005+\u0000\u0000\u0090"+
		"\u008e\u0001\u0000\u0000\u0000\u0091\u0094\u0001\u0000\u0000\u0000\u0092"+
		"\u0090\u0001\u0000\u0000\u0000\u0092\u0093\u0001\u0000\u0000\u0000\u0093"+
		"\u0095\u0001\u0000\u0000\u0000\u0094\u0092\u0001\u0000\u0000\u0000\u0095"+
		"\u0096\u0005)\u0000\u0000\u0096\u0097\u0003\f\u0006\u0000\u0097\u0011"+
		"\u0001\u0000\u0000\u0000\u0098\u0099\u0005\u0011\u0000\u0000\u0099\u009a"+
		"\u0003\u001a\r\u0000\u009a\u009b\u0005\u0012\u0000\u0000\u009b\u009c\u0005"+
		"+\u0000\u0000\u009c\u009d\u0003\f\u0006\u0000\u009d\u0013\u0001\u0000"+
		"\u0000\u0000\u009e\u009f\u0003\u0016\u000b\u0000\u009f\u00a0\u0005(\u0000"+
		"\u0000\u00a0\u00a1\u0005+\u0000\u0000\u00a1\u00a2\u0005)\u0000\u0000\u00a2"+
		"\u00b4\u0001\u0000\u0000\u0000\u00a3\u00a4\u0003\u0016\u000b\u0000\u00a4"+
		"\u00a5\u0005(\u0000\u0000\u00a5\u00a6\u0005+\u0000\u0000\u00a6\u00a7\u0005"+
		"*\u0000\u0000\u00a7\u00a8\u0003\u0018\f\u0000\u00a8\u00a9\u0005)\u0000"+
		"\u0000\u00a9\u00b4\u0001\u0000\u0000\u0000\u00aa\u00ab\u0003\u0016\u000b"+
		"\u0000\u00ab\u00ac\u0005(\u0000\u0000\u00ac\u00ad\u0005+\u0000\u0000\u00ad"+
		"\u00ae\u0005*\u0000\u0000\u00ae\u00af\u0005+\u0000\u0000\u00af\u00b0\u0005"+
		"*\u0000\u0000\u00b0\u00b1\u0003\u0018\f\u0000\u00b1\u00b2\u0005)\u0000"+
		"\u0000\u00b2\u00b4\u0001\u0000\u0000\u0000\u00b3\u009e\u0001\u0000\u0000"+
		"\u0000\u00b3\u00a3\u0001\u0000\u0000\u0000\u00b3\u00aa\u0001\u0000\u0000"+
		"\u0000\u00b4\u0015\u0001\u0000\u0000\u0000\u00b5\u00b6\u0007\u0000\u0000"+
		"\u0000\u00b6\u0017\u0001\u0000\u0000\u0000\u00b7\u00bb\u0003\u001a\r\u0000"+
		"\u00b8\u00bb\u0003\u001c\u000e\u0000\u00b9\u00bb\u0005+\u0000\u0000\u00ba"+
		"\u00b7\u0001\u0000\u0000\u0000\u00ba\u00b8\u0001\u0000\u0000\u0000\u00ba"+
		"\u00b9\u0001\u0000\u0000\u0000\u00bb\u0019\u0001\u0000\u0000\u0000\u00bc"+
		"\u00bd\u0005\u0013\u0000\u0000\u00bd\u00be\u0005(\u0000\u0000\u00be\u00c3"+
		"\u0005+\u0000\u0000\u00bf\u00c0\u0005*\u0000\u0000\u00c0\u00c2\u0005+"+
		"\u0000\u0000\u00c1\u00bf\u0001\u0000\u0000\u0000\u00c2\u00c5\u0001\u0000"+
		"\u0000\u0000\u00c3\u00c1\u0001\u0000\u0000\u0000\u00c3\u00c4\u0001\u0000"+
		"\u0000\u0000\u00c4\u00c6\u0001\u0000\u0000\u0000\u00c5\u00c3\u0001\u0000"+
		"\u0000\u0000\u00c6\u00c7\u0005)\u0000\u0000\u00c7\u001b\u0001\u0000\u0000"+
		"\u0000\u00c8\u00c9\u0005\u0014\u0000\u0000\u00c9\u00ca\u0005&\u0000\u0000"+
		"\u00ca\u00cb\u0003D\"\u0000\u00cb\u00cc\u0005\'\u0000\u0000\u00cc\u001d"+
		"\u0001\u0000\u0000\u0000\u00cd\u00ce\u0005\u0015\u0000\u0000\u00ce\u00d2"+
		"\u0005&\u0000\u0000\u00cf\u00d1\u0003 \u0010\u0000\u00d0\u00cf\u0001\u0000"+
		"\u0000\u0000\u00d1\u00d4\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000"+
		"\u0000\u0000\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d3\u00d5\u0001\u0000"+
		"\u0000\u0000\u00d4\u00d2\u0001\u0000\u0000\u0000\u00d5\u00d6\u0005\'\u0000"+
		"\u0000\u00d6\u001f\u0001\u0000\u0000\u0000\u00d7\u00db\u0003\"\u0011\u0000"+
		"\u00d8\u00db\u0003$\u0012\u0000\u00d9\u00db\u0003&\u0013\u0000\u00da\u00d7"+
		"\u0001\u0000\u0000\u0000\u00da\u00d8\u0001\u0000\u0000\u0000\u00da\u00d9"+
		"\u0001\u0000\u0000\u0000\u00db!\u0001\u0000\u0000\u0000\u00dc\u00dd\u0005"+
		"\u0016\u0000\u0000\u00dd\u00de\u0005+\u0000\u0000\u00de\u00df\u0005%\u0000"+
		"\u0000\u00df\u00e1\u0005+\u0000\u0000\u00e0\u00e2\u0003(\u0014\u0000\u00e1"+
		"\u00e0\u0001\u0000\u0000\u0000\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2"+
		"\u00e3\u0001\u0000\u0000\u0000\u00e3\u00e5\u0005&\u0000\u0000\u00e4\u00e6"+
		"\u0003*\u0015\u0000\u00e5\u00e4\u0001\u0000\u0000\u0000\u00e5\u00e6\u0001"+
		"\u0000\u0000\u0000\u00e6\u00e7\u0001\u0000\u0000\u0000\u00e7\u00e8\u0005"+
		"\'\u0000\u0000\u00e8#\u0001\u0000\u0000\u0000\u00e9\u00ea\u0005\u0017"+
		"\u0000\u0000\u00ea\u00eb\u0005+\u0000\u0000\u00eb\u00ec\u0005%\u0000\u0000"+
		"\u00ec\u00ef\u0005+\u0000\u0000\u00ed\u00ee\u0005*\u0000\u0000\u00ee\u00f0"+
		"\u0005+\u0000\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00f0\u00f1\u0001"+
		"\u0000\u0000\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f1\u00f2\u0001"+
		"\u0000\u0000\u0000\u00f2\u00f4\u0001\u0000\u0000\u0000\u00f3\u00f5\u0003"+
		"(\u0014\u0000\u00f4\u00f3\u0001\u0000\u0000\u0000\u00f4\u00f5\u0001\u0000"+
		"\u0000\u0000\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6\u00f8\u0005&\u0000"+
		"\u0000\u00f7\u00f9\u0003*\u0015\u0000\u00f8\u00f7\u0001\u0000\u0000\u0000"+
		"\u00f8\u00f9\u0001\u0000\u0000\u0000\u00f9\u00fa\u0001\u0000\u0000\u0000"+
		"\u00fa\u00fb\u0005\'\u0000\u0000\u00fb%\u0001\u0000\u0000\u0000\u00fc"+
		"\u00fd\u0005\u0018\u0000\u0000\u00fd\u0100\u0005+\u0000\u0000\u00fe\u00ff"+
		"\u0005*\u0000\u0000\u00ff\u0101\u0005+\u0000\u0000\u0100\u00fe\u0001\u0000"+
		"\u0000\u0000\u0101\u0102\u0001\u0000\u0000\u0000\u0102\u0100\u0001\u0000"+
		"\u0000\u0000\u0102\u0103\u0001\u0000\u0000\u0000\u0103\u0104\u0001\u0000"+
		"\u0000\u0000\u0104\u0105\u0005%\u0000\u0000\u0105\u0107\u0005+\u0000\u0000"+
		"\u0106\u0108\u0003(\u0014\u0000\u0107\u0106\u0001\u0000\u0000\u0000\u0107"+
		"\u0108\u0001\u0000\u0000\u0000\u0108\u0109\u0001\u0000\u0000\u0000\u0109"+
		"\u010b\u0005&\u0000\u0000\u010a\u010c\u0003*\u0015\u0000\u010b\u010a\u0001"+
		"\u0000\u0000\u0000\u010b\u010c\u0001\u0000\u0000\u0000\u010c\u010d\u0001"+
		"\u0000\u0000\u0000\u010d\u010e\u0005\'\u0000\u0000\u010e\'\u0001\u0000"+
		"\u0000\u0000\u010f\u0110\u0005\u0019\u0000\u0000\u0110\u0111\u0005\u001a"+
		"\u0000\u0000\u0111)\u0001\u0000\u0000\u0000\u0112\u0113\u0005\u001b\u0000"+
		"\u0000\u0113\u0115\u0005&\u0000\u0000\u0114\u0116\u0003,\u0016\u0000\u0115"+
		"\u0114\u0001\u0000\u0000\u0000\u0116\u0117\u0001\u0000\u0000\u0000\u0117"+
		"\u0115\u0001\u0000\u0000\u0000\u0117\u0118\u0001\u0000\u0000\u0000\u0118"+
		"\u0119\u0001\u0000\u0000\u0000\u0119\u011a\u0005\'\u0000\u0000\u011a+"+
		"\u0001\u0000\u0000\u0000\u011b\u011f\u0003.\u0017\u0000\u011c\u011f\u0003"+
		"\u001c\u000e\u0000\u011d\u011f\u00030\u0018\u0000\u011e\u011b\u0001\u0000"+
		"\u0000\u0000\u011e\u011c\u0001\u0000\u0000\u0000\u011e\u011d\u0001\u0000"+
		"\u0000\u0000\u011f-\u0001\u0000\u0000\u0000\u0120\u0121\u0005\u0010\u0000"+
		"\u0000\u0121\u0122\u0005(\u0000\u0000\u0122\u0123\u0005+\u0000\u0000\u0123"+
		"\u0124\u0005*\u0000\u0000\u0124\u0125\u0005+\u0000\u0000\u0125\u0126\u0005"+
		")\u0000\u0000\u0126/\u0001\u0000\u0000\u0000\u0127\u0128\u0005\u001c\u0000"+
		"\u0000\u0128\u0129\u0005(\u0000\u0000\u0129\u012a\u0005+\u0000\u0000\u012a"+
		"\u012b\u0005)\u0000\u0000\u012b1\u0001\u0000\u0000\u0000\u012c\u012d\u0005"+
		"\u001d\u0000\u0000\u012d\u0131\u0005&\u0000\u0000\u012e\u0130\u00034\u001a"+
		"\u0000\u012f\u012e\u0001\u0000\u0000\u0000\u0130\u0133\u0001\u0000\u0000"+
		"\u0000\u0131\u012f\u0001\u0000\u0000\u0000\u0131\u0132\u0001\u0000\u0000"+
		"\u0000\u0132\u0134\u0001\u0000\u0000\u0000\u0133\u0131\u0001\u0000\u0000"+
		"\u0000\u0134\u0135\u0005\'\u0000\u0000\u01353\u0001\u0000\u0000\u0000"+
		"\u0136\u0137\u0005\u0013\u0000\u0000\u0137\u0138\u0005+\u0000\u0000\u0138"+
		"\u013c\u0005&\u0000\u0000\u0139\u013b\u00036\u001b\u0000\u013a\u0139\u0001"+
		"\u0000\u0000\u0000\u013b\u013e\u0001\u0000\u0000\u0000\u013c\u013a\u0001"+
		"\u0000\u0000\u0000\u013c\u013d\u0001\u0000\u0000\u0000\u013d\u013f\u0001"+
		"\u0000\u0000\u0000\u013e\u013c\u0001\u0000\u0000\u0000\u013f\u0140\u0003"+
		"8\u001c\u0000\u0140\u0141\u0003B!\u0000\u0141\u0142\u0005\'\u0000\u0000"+
		"\u01425\u0001\u0000\u0000\u0000\u0143\u0144\u0005\u001e\u0000\u0000\u0144"+
		"\u0145\u0005(\u0000\u0000\u0145\u0146\u0005+\u0000\u0000\u0146\u0147\u0005"+
		"*\u0000\u0000\u0147\u0148\u0003<\u001e\u0000\u0148\u0149\u0005)\u0000"+
		"\u0000\u01497\u0001\u0000\u0000\u0000\u014a\u014b\u0005\u001f\u0000\u0000"+
		"\u014b\u014c\u0005(\u0000\u0000\u014c\u014d\u0003:\u001d\u0000\u014d\u014e"+
		"\u0005)\u0000\u0000\u014e9\u0001\u0000\u0000\u0000\u014f\u0152\u0003<"+
		"\u001e\u0000\u0150\u0152\u0003@ \u0000\u0151\u014f\u0001\u0000\u0000\u0000"+
		"\u0151\u0150\u0001\u0000\u0000\u0000\u0152;\u0001\u0000\u0000\u0000\u0153"+
		"\u0156\u0005+\u0000\u0000\u0154\u0156\u0003>\u001f\u0000\u0155\u0153\u0001"+
		"\u0000\u0000\u0000\u0155\u0154\u0001\u0000\u0000\u0000\u0156=\u0001\u0000"+
		"\u0000\u0000\u0157\u0158\u0005-\u0000\u0000\u0158\u0159\u0005(\u0000\u0000"+
		"\u0159\u015a\u0005+\u0000\u0000\u015a\u015b\u0005)\u0000\u0000\u015b?"+
		"\u0001\u0000\u0000\u0000\u015c\u015d\u0005!\u0000\u0000\u015d\u015e\u0005"+
		"(\u0000\u0000\u015e\u015f\u0005+\u0000\u0000\u015f\u0160\u0005)\u0000"+
		"\u0000\u0160A\u0001\u0000\u0000\u0000\u0161\u0162\u0005 \u0000\u0000\u0162"+
		"\u0163\u0005&\u0000\u0000\u0163\u0164\u0003\u001c\u000e\u0000\u0164\u0165"+
		"\u0005\'\u0000\u0000\u0165C\u0001\u0000\u0000\u0000\u0166\u0168\u0003"+
		"F#\u0000\u0167\u0166\u0001\u0000\u0000\u0000\u0168\u016b\u0001\u0000\u0000"+
		"\u0000\u0169\u0167\u0001\u0000\u0000\u0000\u0169\u016a\u0001\u0000\u0000"+
		"\u0000\u016aE\u0001\u0000\u0000\u0000\u016b\u0169\u0001\u0000\u0000\u0000"+
		"\u016c\u016d\u0005&\u0000\u0000\u016d\u016e\u0003D\"\u0000\u016e\u016f"+
		"\u0005\'\u0000\u0000\u016f\u0172\u0001\u0000\u0000\u0000\u0170\u0172\b"+
		"\u0001\u0000\u0000\u0171\u016c\u0001\u0000\u0000\u0000\u0171\u0170\u0001"+
		"\u0000\u0000\u0000\u0172G\u0001\u0000\u0000\u0000\u001eS\\bhkp\u007f\u0087"+
		"\u0092\u00b3\u00ba\u00c3\u00d2\u00da\u00e1\u00e5\u00f1\u00f4\u00f8\u0102"+
		"\u0107\u010b\u0117\u011e\u0131\u013c\u0151\u0155\u0169\u0171";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}