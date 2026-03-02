// Generated from kmodel/Kmodel.g4 by ANTLR 4.13.1
package kmeta;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class KmodelParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		MAKE=1, OBJECT=2, PROP=3, HAS=4, KNOWS=5, LBRACE=6, RBRACE=7, LPAREN=8, 
		RPAREN=9, COLON=10, ARROW=11, STRING=12, WS=13, COMMENT=14;
	public static final int
		RULE_kmodel_file = 0, RULE_make_object_block = 1, RULE_object_signature = 2, 
		RULE_statement = 3, RULE_prop_statement = 4, RULE_has_statement = 5, RULE_knows_statement = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"kmodel_file", "make_object_block", "object_signature", "statement", 
			"prop_statement", "has_statement", "knows_statement"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'make'", "'object'", "'prop'", "'has'", "'knows'", "'{'", "'}'", 
			"'('", "')'", "':'", "'->'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "MAKE", "OBJECT", "PROP", "HAS", "KNOWS", "LBRACE", "RBRACE", "LPAREN", 
			"RPAREN", "COLON", "ARROW", "STRING", "WS", "COMMENT"
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
	public String getGrammarFileName() { return "Kmodel.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public KmodelParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Kmodel_fileContext extends ParserRuleContext {
		public Make_object_blockContext make_object_block() {
			return getRuleContext(Make_object_blockContext.class,0);
		}
		public TerminalNode EOF() { return getToken(KmodelParser.EOF, 0); }
		public Kmodel_fileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_kmodel_file; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KmodelVisitor ) return ((KmodelVisitor<? extends T>)visitor).visitKmodel_file(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Kmodel_fileContext kmodel_file() throws RecognitionException {
		Kmodel_fileContext _localctx = new Kmodel_fileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_kmodel_file);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			make_object_block();
			setState(15);
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
	public static class Make_object_blockContext extends ParserRuleContext {
		public TerminalNode MAKE() { return getToken(KmodelParser.MAKE, 0); }
		public TerminalNode OBJECT() { return getToken(KmodelParser.OBJECT, 0); }
		public Object_signatureContext object_signature() {
			return getRuleContext(Object_signatureContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(KmodelParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KmodelParser.RBRACE, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public Make_object_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_make_object_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KmodelVisitor ) return ((KmodelVisitor<? extends T>)visitor).visitMake_object_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Make_object_blockContext make_object_block() throws RecognitionException {
		Make_object_blockContext _localctx = new Make_object_blockContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_make_object_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(17);
			match(MAKE);
			setState(18);
			match(OBJECT);
			setState(19);
			object_signature();
			setState(20);
			match(LBRACE);
			setState(24);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 56L) != 0)) {
				{
				{
				setState(21);
				statement();
				}
				}
				setState(26);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(27);
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
	public static class Object_signatureContext extends ParserRuleContext {
		public List<TerminalNode> STRING() { return getTokens(KmodelParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KmodelParser.STRING, i);
		}
		public TerminalNode COLON() { return getToken(KmodelParser.COLON, 0); }
		public Object_signatureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_object_signature; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KmodelVisitor ) return ((KmodelVisitor<? extends T>)visitor).visitObject_signature(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Object_signatureContext object_signature() throws RecognitionException {
		Object_signatureContext _localctx = new Object_signatureContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_object_signature);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(29);
			match(STRING);
			setState(30);
			match(COLON);
			setState(31);
			match(STRING);
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
	public static class StatementContext extends ParserRuleContext {
		public Prop_statementContext prop_statement() {
			return getRuleContext(Prop_statementContext.class,0);
		}
		public Has_statementContext has_statement() {
			return getRuleContext(Has_statementContext.class,0);
		}
		public Knows_statementContext knows_statement() {
			return getRuleContext(Knows_statementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KmodelVisitor ) return ((KmodelVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_statement);
		try {
			setState(36);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PROP:
				enterOuterAlt(_localctx, 1);
				{
				setState(33);
				prop_statement();
				}
				break;
			case HAS:
				enterOuterAlt(_localctx, 2);
				{
				setState(34);
				has_statement();
				}
				break;
			case KNOWS:
				enterOuterAlt(_localctx, 3);
				{
				setState(35);
				knows_statement();
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
	public static class Prop_statementContext extends ParserRuleContext {
		public TerminalNode PROP() { return getToken(KmodelParser.PROP, 0); }
		public TerminalNode LPAREN() { return getToken(KmodelParser.LPAREN, 0); }
		public List<TerminalNode> STRING() { return getTokens(KmodelParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KmodelParser.STRING, i);
		}
		public TerminalNode RPAREN() { return getToken(KmodelParser.RPAREN, 0); }
		public TerminalNode ARROW() { return getToken(KmodelParser.ARROW, 0); }
		public Prop_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prop_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KmodelVisitor ) return ((KmodelVisitor<? extends T>)visitor).visitProp_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Prop_statementContext prop_statement() throws RecognitionException {
		Prop_statementContext _localctx = new Prop_statementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_prop_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			match(PROP);
			setState(39);
			match(LPAREN);
			setState(40);
			match(STRING);
			setState(41);
			match(RPAREN);
			setState(42);
			match(ARROW);
			setState(43);
			match(STRING);
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
	public static class Has_statementContext extends ParserRuleContext {
		public TerminalNode HAS() { return getToken(KmodelParser.HAS, 0); }
		public TerminalNode LPAREN() { return getToken(KmodelParser.LPAREN, 0); }
		public TerminalNode STRING() { return getToken(KmodelParser.STRING, 0); }
		public TerminalNode RPAREN() { return getToken(KmodelParser.RPAREN, 0); }
		public TerminalNode ARROW() { return getToken(KmodelParser.ARROW, 0); }
		public Make_object_blockContext make_object_block() {
			return getRuleContext(Make_object_blockContext.class,0);
		}
		public Has_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_has_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KmodelVisitor ) return ((KmodelVisitor<? extends T>)visitor).visitHas_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Has_statementContext has_statement() throws RecognitionException {
		Has_statementContext _localctx = new Has_statementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_has_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			match(HAS);
			setState(46);
			match(LPAREN);
			setState(47);
			match(STRING);
			setState(48);
			match(RPAREN);
			setState(49);
			match(ARROW);
			setState(50);
			make_object_block();
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
	public static class Knows_statementContext extends ParserRuleContext {
		public TerminalNode KNOWS() { return getToken(KmodelParser.KNOWS, 0); }
		public TerminalNode LPAREN() { return getToken(KmodelParser.LPAREN, 0); }
		public List<TerminalNode> STRING() { return getTokens(KmodelParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(KmodelParser.STRING, i);
		}
		public TerminalNode RPAREN() { return getToken(KmodelParser.RPAREN, 0); }
		public TerminalNode ARROW() { return getToken(KmodelParser.ARROW, 0); }
		public Knows_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_knows_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KmodelVisitor ) return ((KmodelVisitor<? extends T>)visitor).visitKnows_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Knows_statementContext knows_statement() throws RecognitionException {
		Knows_statementContext _localctx = new Knows_statementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_knows_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			match(KNOWS);
			setState(53);
			match(LPAREN);
			setState(54);
			match(STRING);
			setState(55);
			match(RPAREN);
			setState(56);
			match(ARROW);
			setState(57);
			match(STRING);
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
		"\u0004\u0001\u000e<\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005"+
		"\u0001\u0017\b\u0001\n\u0001\f\u0001\u001a\t\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0003\u0003%\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0000\u0000\u0007\u0000\u0002\u0004\u0006\b\n\f\u0000\u0000"+
		"7\u0000\u000e\u0001\u0000\u0000\u0000\u0002\u0011\u0001\u0000\u0000\u0000"+
		"\u0004\u001d\u0001\u0000\u0000\u0000\u0006$\u0001\u0000\u0000\u0000\b"+
		"&\u0001\u0000\u0000\u0000\n-\u0001\u0000\u0000\u0000\f4\u0001\u0000\u0000"+
		"\u0000\u000e\u000f\u0003\u0002\u0001\u0000\u000f\u0010\u0005\u0000\u0000"+
		"\u0001\u0010\u0001\u0001\u0000\u0000\u0000\u0011\u0012\u0005\u0001\u0000"+
		"\u0000\u0012\u0013\u0005\u0002\u0000\u0000\u0013\u0014\u0003\u0004\u0002"+
		"\u0000\u0014\u0018\u0005\u0006\u0000\u0000\u0015\u0017\u0003\u0006\u0003"+
		"\u0000\u0016\u0015\u0001\u0000\u0000\u0000\u0017\u001a\u0001\u0000\u0000"+
		"\u0000\u0018\u0016\u0001\u0000\u0000\u0000\u0018\u0019\u0001\u0000\u0000"+
		"\u0000\u0019\u001b\u0001\u0000\u0000\u0000\u001a\u0018\u0001\u0000\u0000"+
		"\u0000\u001b\u001c\u0005\u0007\u0000\u0000\u001c\u0003\u0001\u0000\u0000"+
		"\u0000\u001d\u001e\u0005\f\u0000\u0000\u001e\u001f\u0005\n\u0000\u0000"+
		"\u001f \u0005\f\u0000\u0000 \u0005\u0001\u0000\u0000\u0000!%\u0003\b\u0004"+
		"\u0000\"%\u0003\n\u0005\u0000#%\u0003\f\u0006\u0000$!\u0001\u0000\u0000"+
		"\u0000$\"\u0001\u0000\u0000\u0000$#\u0001\u0000\u0000\u0000%\u0007\u0001"+
		"\u0000\u0000\u0000&\'\u0005\u0003\u0000\u0000\'(\u0005\b\u0000\u0000("+
		")\u0005\f\u0000\u0000)*\u0005\t\u0000\u0000*+\u0005\u000b\u0000\u0000"+
		"+,\u0005\f\u0000\u0000,\t\u0001\u0000\u0000\u0000-.\u0005\u0004\u0000"+
		"\u0000./\u0005\b\u0000\u0000/0\u0005\f\u0000\u000001\u0005\t\u0000\u0000"+
		"12\u0005\u000b\u0000\u000023\u0003\u0002\u0001\u00003\u000b\u0001\u0000"+
		"\u0000\u000045\u0005\u0005\u0000\u000056\u0005\b\u0000\u000067\u0005\f"+
		"\u0000\u000078\u0005\t\u0000\u000089\u0005\u000b\u0000\u00009:\u0005\f"+
		"\u0000\u0000:\r\u0001\u0000\u0000\u0000\u0002\u0018$";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}