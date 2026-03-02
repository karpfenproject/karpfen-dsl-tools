// Generated from kmodel/Kmodel.g4 by ANTLR 4.13.1
package kmeta;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class KmodelLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		MAKE=1, OBJECT=2, PROP=3, HAS=4, KNOWS=5, LBRACE=6, RBRACE=7, LPAREN=8, 
		RPAREN=9, COLON=10, ARROW=11, STRING=12, WS=13, COMMENT=14;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"MAKE", "OBJECT", "PROP", "HAS", "KNOWS", "LBRACE", "RBRACE", "LPAREN", 
			"RPAREN", "COLON", "ARROW", "STRING", "WS", "COMMENT"
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


	public KmodelLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Kmodel.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u000em\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000bJ\b\u000b\n\u000b\f\u000b"+
		"M\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0005\u000bT\b\u000b\n\u000b\f\u000bW\t\u000b\u0001\u000b\u0003\u000b"+
		"Z\b\u000b\u0001\f\u0004\f]\b\f\u000b\f\f\f^\u0001\f\u0001\f\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0005\rg\b\r\n\r\f\rj\t\r\u0001\r\u0001\r\u0000\u0000"+
		"\u000e\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006"+
		"\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e"+
		"\u0001\u0000\u0004\u0003\u0000\n\n\r\r\"\"\u0003\u0000\n\n\r\r\'\'\u0003"+
		"\u0000\t\n\r\r  \u0002\u0000\n\n\r\rs\u0000\u0001\u0001\u0000\u0000\u0000"+
		"\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000"+
		"\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000"+
		"\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f"+
		"\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013"+
		"\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017"+
		"\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b"+
		"\u0001\u0000\u0000\u0000\u0001\u001d\u0001\u0000\u0000\u0000\u0003\"\u0001"+
		"\u0000\u0000\u0000\u0005)\u0001\u0000\u0000\u0000\u0007.\u0001\u0000\u0000"+
		"\u0000\t2\u0001\u0000\u0000\u0000\u000b8\u0001\u0000\u0000\u0000\r:\u0001"+
		"\u0000\u0000\u0000\u000f<\u0001\u0000\u0000\u0000\u0011>\u0001\u0000\u0000"+
		"\u0000\u0013@\u0001\u0000\u0000\u0000\u0015B\u0001\u0000\u0000\u0000\u0017"+
		"Y\u0001\u0000\u0000\u0000\u0019\\\u0001\u0000\u0000\u0000\u001bb\u0001"+
		"\u0000\u0000\u0000\u001d\u001e\u0005m\u0000\u0000\u001e\u001f\u0005a\u0000"+
		"\u0000\u001f \u0005k\u0000\u0000 !\u0005e\u0000\u0000!\u0002\u0001\u0000"+
		"\u0000\u0000\"#\u0005o\u0000\u0000#$\u0005b\u0000\u0000$%\u0005j\u0000"+
		"\u0000%&\u0005e\u0000\u0000&\'\u0005c\u0000\u0000\'(\u0005t\u0000\u0000"+
		"(\u0004\u0001\u0000\u0000\u0000)*\u0005p\u0000\u0000*+\u0005r\u0000\u0000"+
		"+,\u0005o\u0000\u0000,-\u0005p\u0000\u0000-\u0006\u0001\u0000\u0000\u0000"+
		"./\u0005h\u0000\u0000/0\u0005a\u0000\u000001\u0005s\u0000\u00001\b\u0001"+
		"\u0000\u0000\u000023\u0005k\u0000\u000034\u0005n\u0000\u000045\u0005o"+
		"\u0000\u000056\u0005w\u0000\u000067\u0005s\u0000\u00007\n\u0001\u0000"+
		"\u0000\u000089\u0005{\u0000\u00009\f\u0001\u0000\u0000\u0000:;\u0005}"+
		"\u0000\u0000;\u000e\u0001\u0000\u0000\u0000<=\u0005(\u0000\u0000=\u0010"+
		"\u0001\u0000\u0000\u0000>?\u0005)\u0000\u0000?\u0012\u0001\u0000\u0000"+
		"\u0000@A\u0005:\u0000\u0000A\u0014\u0001\u0000\u0000\u0000BC\u0005-\u0000"+
		"\u0000CD\u0005>\u0000\u0000D\u0016\u0001\u0000\u0000\u0000EK\u0005\"\u0000"+
		"\u0000FJ\b\u0000\u0000\u0000GH\u0005\\\u0000\u0000HJ\t\u0000\u0000\u0000"+
		"IF\u0001\u0000\u0000\u0000IG\u0001\u0000\u0000\u0000JM\u0001\u0000\u0000"+
		"\u0000KI\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000LN\u0001\u0000"+
		"\u0000\u0000MK\u0001\u0000\u0000\u0000NZ\u0005\"\u0000\u0000OU\u0005\'"+
		"\u0000\u0000PT\b\u0001\u0000\u0000QR\u0005\\\u0000\u0000RT\t\u0000\u0000"+
		"\u0000SP\u0001\u0000\u0000\u0000SQ\u0001\u0000\u0000\u0000TW\u0001\u0000"+
		"\u0000\u0000US\u0001\u0000\u0000\u0000UV\u0001\u0000\u0000\u0000VX\u0001"+
		"\u0000\u0000\u0000WU\u0001\u0000\u0000\u0000XZ\u0005\'\u0000\u0000YE\u0001"+
		"\u0000\u0000\u0000YO\u0001\u0000\u0000\u0000Z\u0018\u0001\u0000\u0000"+
		"\u0000[]\u0007\u0002\u0000\u0000\\[\u0001\u0000\u0000\u0000]^\u0001\u0000"+
		"\u0000\u0000^\\\u0001\u0000\u0000\u0000^_\u0001\u0000\u0000\u0000_`\u0001"+
		"\u0000\u0000\u0000`a\u0006\f\u0000\u0000a\u001a\u0001\u0000\u0000\u0000"+
		"bc\u0005/\u0000\u0000cd\u0005/\u0000\u0000dh\u0001\u0000\u0000\u0000e"+
		"g\b\u0003\u0000\u0000fe\u0001\u0000\u0000\u0000gj\u0001\u0000\u0000\u0000"+
		"hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000ik\u0001\u0000\u0000"+
		"\u0000jh\u0001\u0000\u0000\u0000kl\u0006\r\u0000\u0000l\u001c\u0001\u0000"+
		"\u0000\u0000\b\u0000IKSUY^h\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}