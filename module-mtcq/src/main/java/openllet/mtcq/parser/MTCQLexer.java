// Generated from MTCQ.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class MTCQLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, X_TERMINAL=11, XB_TERMINAL=12, UI_TERMINAL=13, U_TERMINAL=14, 
		FI_TERMINAL=15, F_TERMINAL=16, GI_TERMINAL=17, G_TERMINAL=18, LAST_TERMINAL=19, 
		END_TERMINAL=20, NOT1_TERMINAL=21, NOT2_TERMINAL=22, AND1_TERMINAL=23, 
		AND2_TERMINAL=24, OR1_TERMINAL=25, OR2_TERMINAL=26, IMPL1_TERMINAL=27, 
		IMPL2_TERMINAL=28, EQUIV1_TERMINAL=29, EQUIV2_TERMINAL=30, XOR_TERMINAL=31, 
		TRUE_TERMINAL=32, FALSE_TERMINAL=33, TT_TERMINAL=34, FF_TERMINAL=35, ROLE_DIV=36, 
		TIME_POINT=37, NAME=38, URI=39, COMMENT=40, WS=41;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "X_TERMINAL", "XB_TERMINAL", "UI_TERMINAL", "U_TERMINAL", "FI_TERMINAL", 
			"F_TERMINAL", "GI_TERMINAL", "G_TERMINAL", "LAST_TERMINAL", "END_TERMINAL", 
			"NOT1_TERMINAL", "NOT2_TERMINAL", "AND1_TERMINAL", "AND2_TERMINAL", "OR1_TERMINAL", 
			"OR2_TERMINAL", "IMPL1_TERMINAL", "IMPL2_TERMINAL", "EQUIV1_TERMINAL", 
			"EQUIV2_TERMINAL", "XOR_TERMINAL", "TRUE_TERMINAL", "FALSE_TERMINAL", 
			"TT_TERMINAL", "FF_TERMINAL", "ROLE_DIV", "TIME_POINT", "NAME", "URI", 
			"COMMENT", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'['", "']'", "'<='", "'<'", "':'", "'?'", "'('", "')'", "'PREFIX'", 
			"'>'", "'X'", "'X[!]'", "'U_'", "'U'", "'F_'", "'F'", "'G_'", "'G'", 
			"'last'", "'end'", "'!'", "'~'", "'&'", "'&&'", "'|'", "'||'", "'->'", 
			"'=>'", "'<->'", "'<=>'", "'^'", "'TRUE'", "'FALSE'", "'TT'", "'FF'", 
			"','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, "X_TERMINAL", 
			"XB_TERMINAL", "UI_TERMINAL", "U_TERMINAL", "FI_TERMINAL", "F_TERMINAL", 
			"GI_TERMINAL", "G_TERMINAL", "LAST_TERMINAL", "END_TERMINAL", "NOT1_TERMINAL", 
			"NOT2_TERMINAL", "AND1_TERMINAL", "AND2_TERMINAL", "OR1_TERMINAL", "OR2_TERMINAL", 
			"IMPL1_TERMINAL", "IMPL2_TERMINAL", "EQUIV1_TERMINAL", "EQUIV2_TERMINAL", 
			"XOR_TERMINAL", "TRUE_TERMINAL", "FALSE_TERMINAL", "TT_TERMINAL", "FF_TERMINAL", 
			"ROLE_DIV", "TIME_POINT", "NAME", "URI", "COMMENT", "WS"
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


	public MTCQLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "MTCQ.g4"; }

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
		"\u0004\u0000)\u00de\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002"+
		"\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002"+
		"\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002"+
		"\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002"+
		"\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002"+
		"\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002"+
		"\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007"+
		"!\u0002\"\u0007\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007"+
		"&\u0002\'\u0007\'\u0002(\u0007(\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001"+
		"\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0016\u0001"+
		"\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001"+
		"\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0001!\u0001!\u0001!\u0001\"\u0001"+
		"\"\u0001\"\u0001#\u0001#\u0001$\u0004$\u00bf\b$\u000b$\f$\u00c0\u0001"+
		"%\u0004%\u00c4\b%\u000b%\f%\u00c5\u0001&\u0004&\u00c9\b&\u000b&\f&\u00ca"+
		"\u0001&\u0001&\u0001\'\u0001\'\u0005\'\u00d1\b\'\n\'\f\'\u00d4\t\'\u0001"+
		"\'\u0001\'\u0001(\u0004(\u00d9\b(\u000b(\f(\u00da\u0001(\u0001(\u0000"+
		"\u0000)\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b"+
		"\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b"+
		"\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0016"+
		"-\u0017/\u00181\u00193\u001a5\u001b7\u001c9\u001d;\u001e=\u001f? A!C\""+
		"E#G$I%K&M\'O(Q)\u0001\u0000\u0006\u0001\u000009\u0005\u0000-.09AZ__az"+
		"\u0007\u0000##&&-:??AZ__az\u0002\u0000##//\u0002\u0000\n\n\r\r\u0003\u0000"+
		"\t\n\r\r  \u00e2\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001"+
		"\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001"+
		"\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000"+
		"\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000"+
		"\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000"+
		"\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000"+
		"\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000"+
		"\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000"+
		"\u0000\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000"+
		"%\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000)\u0001"+
		"\u0000\u0000\u0000\u0000+\u0001\u0000\u0000\u0000\u0000-\u0001\u0000\u0000"+
		"\u0000\u0000/\u0001\u0000\u0000\u0000\u00001\u0001\u0000\u0000\u0000\u0000"+
		"3\u0001\u0000\u0000\u0000\u00005\u0001\u0000\u0000\u0000\u00007\u0001"+
		"\u0000\u0000\u0000\u00009\u0001\u0000\u0000\u0000\u0000;\u0001\u0000\u0000"+
		"\u0000\u0000=\u0001\u0000\u0000\u0000\u0000?\u0001\u0000\u0000\u0000\u0000"+
		"A\u0001\u0000\u0000\u0000\u0000C\u0001\u0000\u0000\u0000\u0000E\u0001"+
		"\u0000\u0000\u0000\u0000G\u0001\u0000\u0000\u0000\u0000I\u0001\u0000\u0000"+
		"\u0000\u0000K\u0001\u0000\u0000\u0000\u0000M\u0001\u0000\u0000\u0000\u0000"+
		"O\u0001\u0000\u0000\u0000\u0000Q\u0001\u0000\u0000\u0000\u0001S\u0001"+
		"\u0000\u0000\u0000\u0003U\u0001\u0000\u0000\u0000\u0005W\u0001\u0000\u0000"+
		"\u0000\u0007Z\u0001\u0000\u0000\u0000\t\\\u0001\u0000\u0000\u0000\u000b"+
		"^\u0001\u0000\u0000\u0000\r`\u0001\u0000\u0000\u0000\u000fb\u0001\u0000"+
		"\u0000\u0000\u0011d\u0001\u0000\u0000\u0000\u0013k\u0001\u0000\u0000\u0000"+
		"\u0015m\u0001\u0000\u0000\u0000\u0017o\u0001\u0000\u0000\u0000\u0019t"+
		"\u0001\u0000\u0000\u0000\u001bw\u0001\u0000\u0000\u0000\u001dy\u0001\u0000"+
		"\u0000\u0000\u001f|\u0001\u0000\u0000\u0000!~\u0001\u0000\u0000\u0000"+
		"#\u0081\u0001\u0000\u0000\u0000%\u0083\u0001\u0000\u0000\u0000\'\u0088"+
		"\u0001\u0000\u0000\u0000)\u008c\u0001\u0000\u0000\u0000+\u008e\u0001\u0000"+
		"\u0000\u0000-\u0090\u0001\u0000\u0000\u0000/\u0092\u0001\u0000\u0000\u0000"+
		"1\u0095\u0001\u0000\u0000\u00003\u0097\u0001\u0000\u0000\u00005\u009a"+
		"\u0001\u0000\u0000\u00007\u009d\u0001\u0000\u0000\u00009\u00a0\u0001\u0000"+
		"\u0000\u0000;\u00a4\u0001\u0000\u0000\u0000=\u00a8\u0001\u0000\u0000\u0000"+
		"?\u00aa\u0001\u0000\u0000\u0000A\u00af\u0001\u0000\u0000\u0000C\u00b5"+
		"\u0001\u0000\u0000\u0000E\u00b8\u0001\u0000\u0000\u0000G\u00bb\u0001\u0000"+
		"\u0000\u0000I\u00be\u0001\u0000\u0000\u0000K\u00c3\u0001\u0000\u0000\u0000"+
		"M\u00c8\u0001\u0000\u0000\u0000O\u00ce\u0001\u0000\u0000\u0000Q\u00d8"+
		"\u0001\u0000\u0000\u0000ST\u0005[\u0000\u0000T\u0002\u0001\u0000\u0000"+
		"\u0000UV\u0005]\u0000\u0000V\u0004\u0001\u0000\u0000\u0000WX\u0005<\u0000"+
		"\u0000XY\u0005=\u0000\u0000Y\u0006\u0001\u0000\u0000\u0000Z[\u0005<\u0000"+
		"\u0000[\b\u0001\u0000\u0000\u0000\\]\u0005:\u0000\u0000]\n\u0001\u0000"+
		"\u0000\u0000^_\u0005?\u0000\u0000_\f\u0001\u0000\u0000\u0000`a\u0005("+
		"\u0000\u0000a\u000e\u0001\u0000\u0000\u0000bc\u0005)\u0000\u0000c\u0010"+
		"\u0001\u0000\u0000\u0000de\u0005P\u0000\u0000ef\u0005R\u0000\u0000fg\u0005"+
		"E\u0000\u0000gh\u0005F\u0000\u0000hi\u0005I\u0000\u0000ij\u0005X\u0000"+
		"\u0000j\u0012\u0001\u0000\u0000\u0000kl\u0005>\u0000\u0000l\u0014\u0001"+
		"\u0000\u0000\u0000mn\u0005X\u0000\u0000n\u0016\u0001\u0000\u0000\u0000"+
		"op\u0005X\u0000\u0000pq\u0005[\u0000\u0000qr\u0005!\u0000\u0000rs\u0005"+
		"]\u0000\u0000s\u0018\u0001\u0000\u0000\u0000tu\u0005U\u0000\u0000uv\u0005"+
		"_\u0000\u0000v\u001a\u0001\u0000\u0000\u0000wx\u0005U\u0000\u0000x\u001c"+
		"\u0001\u0000\u0000\u0000yz\u0005F\u0000\u0000z{\u0005_\u0000\u0000{\u001e"+
		"\u0001\u0000\u0000\u0000|}\u0005F\u0000\u0000} \u0001\u0000\u0000\u0000"+
		"~\u007f\u0005G\u0000\u0000\u007f\u0080\u0005_\u0000\u0000\u0080\"\u0001"+
		"\u0000\u0000\u0000\u0081\u0082\u0005G\u0000\u0000\u0082$\u0001\u0000\u0000"+
		"\u0000\u0083\u0084\u0005l\u0000\u0000\u0084\u0085\u0005a\u0000\u0000\u0085"+
		"\u0086\u0005s\u0000\u0000\u0086\u0087\u0005t\u0000\u0000\u0087&\u0001"+
		"\u0000\u0000\u0000\u0088\u0089\u0005e\u0000\u0000\u0089\u008a\u0005n\u0000"+
		"\u0000\u008a\u008b\u0005d\u0000\u0000\u008b(\u0001\u0000\u0000\u0000\u008c"+
		"\u008d\u0005!\u0000\u0000\u008d*\u0001\u0000\u0000\u0000\u008e\u008f\u0005"+
		"~\u0000\u0000\u008f,\u0001\u0000\u0000\u0000\u0090\u0091\u0005&\u0000"+
		"\u0000\u0091.\u0001\u0000\u0000\u0000\u0092\u0093\u0005&\u0000\u0000\u0093"+
		"\u0094\u0005&\u0000\u0000\u00940\u0001\u0000\u0000\u0000\u0095\u0096\u0005"+
		"|\u0000\u0000\u00962\u0001\u0000\u0000\u0000\u0097\u0098\u0005|\u0000"+
		"\u0000\u0098\u0099\u0005|\u0000\u0000\u00994\u0001\u0000\u0000\u0000\u009a"+
		"\u009b\u0005-\u0000\u0000\u009b\u009c\u0005>\u0000\u0000\u009c6\u0001"+
		"\u0000\u0000\u0000\u009d\u009e\u0005=\u0000\u0000\u009e\u009f\u0005>\u0000"+
		"\u0000\u009f8\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005<\u0000\u0000\u00a1"+
		"\u00a2\u0005-\u0000\u0000\u00a2\u00a3\u0005>\u0000\u0000\u00a3:\u0001"+
		"\u0000\u0000\u0000\u00a4\u00a5\u0005<\u0000\u0000\u00a5\u00a6\u0005=\u0000"+
		"\u0000\u00a6\u00a7\u0005>\u0000\u0000\u00a7<\u0001\u0000\u0000\u0000\u00a8"+
		"\u00a9\u0005^\u0000\u0000\u00a9>\u0001\u0000\u0000\u0000\u00aa\u00ab\u0005"+
		"T\u0000\u0000\u00ab\u00ac\u0005R\u0000\u0000\u00ac\u00ad\u0005U\u0000"+
		"\u0000\u00ad\u00ae\u0005E\u0000\u0000\u00ae@\u0001\u0000\u0000\u0000\u00af"+
		"\u00b0\u0005F\u0000\u0000\u00b0\u00b1\u0005A\u0000\u0000\u00b1\u00b2\u0005"+
		"L\u0000\u0000\u00b2\u00b3\u0005S\u0000\u0000\u00b3\u00b4\u0005E\u0000"+
		"\u0000\u00b4B\u0001\u0000\u0000\u0000\u00b5\u00b6\u0005T\u0000\u0000\u00b6"+
		"\u00b7\u0005T\u0000\u0000\u00b7D\u0001\u0000\u0000\u0000\u00b8\u00b9\u0005"+
		"F\u0000\u0000\u00b9\u00ba\u0005F\u0000\u0000\u00baF\u0001\u0000\u0000"+
		"\u0000\u00bb\u00bc\u0005,\u0000\u0000\u00bcH\u0001\u0000\u0000\u0000\u00bd"+
		"\u00bf\u0007\u0000\u0000\u0000\u00be\u00bd\u0001\u0000\u0000\u0000\u00bf"+
		"\u00c0\u0001\u0000\u0000\u0000\u00c0\u00be\u0001\u0000\u0000\u0000\u00c0"+
		"\u00c1\u0001\u0000\u0000\u0000\u00c1J\u0001\u0000\u0000\u0000\u00c2\u00c4"+
		"\u0007\u0001\u0000\u0000\u00c3\u00c2\u0001\u0000\u0000\u0000\u00c4\u00c5"+
		"\u0001\u0000\u0000\u0000\u00c5\u00c3\u0001\u0000\u0000\u0000\u00c5\u00c6"+
		"\u0001\u0000\u0000\u0000\u00c6L\u0001\u0000\u0000\u0000\u00c7\u00c9\u0007"+
		"\u0002\u0000\u0000\u00c8\u00c7\u0001\u0000\u0000\u0000\u00c9\u00ca\u0001"+
		"\u0000\u0000\u0000\u00ca\u00c8\u0001\u0000\u0000\u0000\u00ca\u00cb\u0001"+
		"\u0000\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000\u0000\u00cc\u00cd\u0007"+
		"\u0003\u0000\u0000\u00cdN\u0001\u0000\u0000\u0000\u00ce\u00d2\u0005#\u0000"+
		"\u0000\u00cf\u00d1\b\u0004\u0000\u0000\u00d0\u00cf\u0001\u0000\u0000\u0000"+
		"\u00d1\u00d4\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000\u0000\u0000"+
		"\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d3\u00d5\u0001\u0000\u0000\u0000"+
		"\u00d4\u00d2\u0001\u0000\u0000\u0000\u00d5\u00d6\u0006\'\u0000\u0000\u00d6"+
		"P\u0001\u0000\u0000\u0000\u00d7\u00d9\u0007\u0005\u0000\u0000\u00d8\u00d7"+
		"\u0001\u0000\u0000\u0000\u00d9\u00da\u0001\u0000\u0000\u0000\u00da\u00d8"+
		"\u0001\u0000\u0000\u0000\u00da\u00db\u0001\u0000\u0000\u0000\u00db\u00dc"+
		"\u0001\u0000\u0000\u0000\u00dc\u00dd\u0006(\u0000\u0000\u00ddR\u0001\u0000"+
		"\u0000\u0000\u0006\u0000\u00c0\u00c5\u00ca\u00d2\u00da\u0001\u0006\u0000"+
		"\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}