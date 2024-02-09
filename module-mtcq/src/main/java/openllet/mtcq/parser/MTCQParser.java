// Generated from MTCQ.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class MTCQParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, INTERVAL_IDENTIFIER=5, X_TERMINAL=6, XB_TERMINAL=7, 
		U_TERMINAL=8, F_TERMINAL=9, G_TERMINAL=10, LAST_TERMINAL=11, END_TERMINAL=12, 
		NOT1_TERMINAL=13, NOT2_TERMINAL=14, AND1_TERMINAL=15, AND2_TERMINAL=16, 
		OR1_TERMINAL=17, OR2_TERMINAL=18, IMPL1_TERMINAL=19, IMPL2_TERMINAL=20, 
		EQUIV1_TERMINAL=21, EQUIV2_TERMINAL=22, XOR_TERMINAL=23, LEFT_PAREN=24, 
		RIGHT_PAREN=25, TRUE_TERMINAL=26, FALSE_TERMINAL=27, TT_TERMINAL=28, FF_TERMINAL=29, 
		LEFT_PREF=30, RIGHT_PREF=31, NAME=32, PREFIX=33, PREFIX_STRING=34, VAR=35, 
		NOT_QUANTIFIED_VAR=36, QUANTIFIED_VAR=37, TERM=38, URI=39, TIME_POINT=40, 
		INLINE_COMMENT=41, LINE_COMMENT=42, WS=43;
	public static final int
		RULE_prop_booleans = 0, RULE_logic_booleans = 1, RULE_not = 2, RULE_and = 3, 
		RULE_or = 4, RULE_impl = 5, RULE_equiv = 6, RULE_xor = 7, RULE_interval = 8, 
		RULE_full_interval = 9, RULE_upper_including_bound_interval = 10, RULE_upper_excluding_bound_interval = 11, 
		RULE_weak_next = 12, RULE_next = 13, RULE_until = 14, RULE_eventually = 15, 
		RULE_always = 16, RULE_subject = 17, RULE_role_atom = 18, RULE_concept_atom = 19, 
		RULE_atom = 20, RULE_conjunctive_query = 21, RULE_mltl_formula = 22, RULE_prefix = 23, 
		RULE_start = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"prop_booleans", "logic_booleans", "not", "and", "or", "impl", "equiv", 
			"xor", "interval", "full_interval", "upper_including_bound_interval", 
			"upper_excluding_bound_interval", "weak_next", "next", "until", "eventually", 
			"always", "subject", "role_atom", "concept_atom", "atom", "conjunctive_query", 
			"mltl_formula", "prefix", "start"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'['", "','", "']'", "'<='", "'_'", "'X'", "'X[!]'", "'U'", "'F'", 
			"'G'", "'last'", "'end'", "'!'", "'~'", "'&'", "'&&'", "'|'", "'||'", 
			"'->'", "'=>'", "'<->'", "'<=>'", "'^'", "'('", "')'", "'TRUE'", "'FALSE'", 
			"'TT'", "'FF'", "'<'", "'>'", null, "'PREFIX'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, "INTERVAL_IDENTIFIER", "X_TERMINAL", "XB_TERMINAL", 
			"U_TERMINAL", "F_TERMINAL", "G_TERMINAL", "LAST_TERMINAL", "END_TERMINAL", 
			"NOT1_TERMINAL", "NOT2_TERMINAL", "AND1_TERMINAL", "AND2_TERMINAL", "OR1_TERMINAL", 
			"OR2_TERMINAL", "IMPL1_TERMINAL", "IMPL2_TERMINAL", "EQUIV1_TERMINAL", 
			"EQUIV2_TERMINAL", "XOR_TERMINAL", "LEFT_PAREN", "RIGHT_PAREN", "TRUE_TERMINAL", 
			"FALSE_TERMINAL", "TT_TERMINAL", "FF_TERMINAL", "LEFT_PREF", "RIGHT_PREF", 
			"NAME", "PREFIX", "PREFIX_STRING", "VAR", "NOT_QUANTIFIED_VAR", "QUANTIFIED_VAR", 
			"TERM", "URI", "TIME_POINT", "INLINE_COMMENT", "LINE_COMMENT", "WS"
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
	public String getGrammarFileName() { return "MTCQ.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MTCQParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Prop_booleansContext extends ParserRuleContext {
		public TerminalNode TRUE_TERMINAL() { return getToken(MTCQParser.TRUE_TERMINAL, 0); }
		public TerminalNode FALSE_TERMINAL() { return getToken(MTCQParser.FALSE_TERMINAL, 0); }
		public Prop_booleansContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prop_booleans; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterProp_booleans(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitProp_booleans(this);
		}
	}

	public final Prop_booleansContext prop_booleans() throws RecognitionException {
		Prop_booleansContext _localctx = new Prop_booleansContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_prop_booleans);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			_la = _input.LA(1);
			if ( !(_la==TRUE_TERMINAL || _la==FALSE_TERMINAL) ) {
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
	public static class Logic_booleansContext extends ParserRuleContext {
		public TerminalNode TT_TERMINAL() { return getToken(MTCQParser.TT_TERMINAL, 0); }
		public TerminalNode FF_TERMINAL() { return getToken(MTCQParser.FF_TERMINAL, 0); }
		public Logic_booleansContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logic_booleans; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterLogic_booleans(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitLogic_booleans(this);
		}
	}

	public final Logic_booleansContext logic_booleans() throws RecognitionException {
		Logic_booleansContext _localctx = new Logic_booleansContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_logic_booleans);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			_la = _input.LA(1);
			if ( !(_la==TT_TERMINAL || _la==FF_TERMINAL) ) {
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
	public static class NotContext extends ParserRuleContext {
		public TerminalNode NOT1_TERMINAL() { return getToken(MTCQParser.NOT1_TERMINAL, 0); }
		public TerminalNode NOT2_TERMINAL() { return getToken(MTCQParser.NOT2_TERMINAL, 0); }
		public NotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitNot(this);
		}
	}

	public final NotContext not() throws RecognitionException {
		NotContext _localctx = new NotContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_not);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			_la = _input.LA(1);
			if ( !(_la==NOT1_TERMINAL || _la==NOT2_TERMINAL) ) {
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
	public static class AndContext extends ParserRuleContext {
		public TerminalNode AND1_TERMINAL() { return getToken(MTCQParser.AND1_TERMINAL, 0); }
		public TerminalNode AND2_TERMINAL() { return getToken(MTCQParser.AND2_TERMINAL, 0); }
		public AndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitAnd(this);
		}
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_and);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
			_la = _input.LA(1);
			if ( !(_la==AND1_TERMINAL || _la==AND2_TERMINAL) ) {
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
	public static class OrContext extends ParserRuleContext {
		public TerminalNode OR1_TERMINAL() { return getToken(MTCQParser.OR1_TERMINAL, 0); }
		public TerminalNode OR2_TERMINAL() { return getToken(MTCQParser.OR2_TERMINAL, 0); }
		public OrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitOr(this);
		}
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_or);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(58);
			_la = _input.LA(1);
			if ( !(_la==OR1_TERMINAL || _la==OR2_TERMINAL) ) {
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
	public static class ImplContext extends ParserRuleContext {
		public TerminalNode IMPL1_TERMINAL() { return getToken(MTCQParser.IMPL1_TERMINAL, 0); }
		public TerminalNode IMPL2_TERMINAL() { return getToken(MTCQParser.IMPL2_TERMINAL, 0); }
		public ImplContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_impl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterImpl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitImpl(this);
		}
	}

	public final ImplContext impl() throws RecognitionException {
		ImplContext _localctx = new ImplContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_impl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			_la = _input.LA(1);
			if ( !(_la==IMPL1_TERMINAL || _la==IMPL2_TERMINAL) ) {
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
	public static class EquivContext extends ParserRuleContext {
		public TerminalNode EQUIV1_TERMINAL() { return getToken(MTCQParser.EQUIV1_TERMINAL, 0); }
		public TerminalNode EQUIV2_TERMINAL() { return getToken(MTCQParser.EQUIV2_TERMINAL, 0); }
		public EquivContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equiv; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterEquiv(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitEquiv(this);
		}
	}

	public final EquivContext equiv() throws RecognitionException {
		EquivContext _localctx = new EquivContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_equiv);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			_la = _input.LA(1);
			if ( !(_la==EQUIV1_TERMINAL || _la==EQUIV2_TERMINAL) ) {
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
	public static class XorContext extends ParserRuleContext {
		public TerminalNode XOR_TERMINAL() { return getToken(MTCQParser.XOR_TERMINAL, 0); }
		public XorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterXor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitXor(this);
		}
	}

	public final XorContext xor() throws RecognitionException {
		XorContext _localctx = new XorContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_xor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			match(XOR_TERMINAL);
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
	public static class IntervalContext extends ParserRuleContext {
		public Full_intervalContext full_interval() {
			return getRuleContext(Full_intervalContext.class,0);
		}
		public Upper_including_bound_intervalContext upper_including_bound_interval() {
			return getRuleContext(Upper_including_bound_intervalContext.class,0);
		}
		public Upper_excluding_bound_intervalContext upper_excluding_bound_interval() {
			return getRuleContext(Upper_excluding_bound_intervalContext.class,0);
		}
		public IntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitInterval(this);
		}
	}

	public final IntervalContext interval() throws RecognitionException {
		IntervalContext _localctx = new IntervalContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_interval);
		try {
			setState(69);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(66);
				full_interval();
				}
				break;
			case T__3:
				enterOuterAlt(_localctx, 2);
				{
				setState(67);
				upper_including_bound_interval();
				}
				break;
			case LEFT_PREF:
				enterOuterAlt(_localctx, 3);
				{
				setState(68);
				upper_excluding_bound_interval();
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
	public static class Full_intervalContext extends ParserRuleContext {
		public List<TerminalNode> TIME_POINT() { return getTokens(MTCQParser.TIME_POINT); }
		public TerminalNode TIME_POINT(int i) {
			return getToken(MTCQParser.TIME_POINT, i);
		}
		public Full_intervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_full_interval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterFull_interval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitFull_interval(this);
		}
	}

	public final Full_intervalContext full_interval() throws RecognitionException {
		Full_intervalContext _localctx = new Full_intervalContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_full_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			match(T__0);
			setState(72);
			match(TIME_POINT);
			setState(73);
			match(T__1);
			setState(74);
			match(TIME_POINT);
			setState(75);
			match(T__2);
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
	public static class Upper_including_bound_intervalContext extends ParserRuleContext {
		public TerminalNode TIME_POINT() { return getToken(MTCQParser.TIME_POINT, 0); }
		public Upper_including_bound_intervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_upper_including_bound_interval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterUpper_including_bound_interval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitUpper_including_bound_interval(this);
		}
	}

	public final Upper_including_bound_intervalContext upper_including_bound_interval() throws RecognitionException {
		Upper_including_bound_intervalContext _localctx = new Upper_including_bound_intervalContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_upper_including_bound_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			match(T__3);
			setState(78);
			match(TIME_POINT);
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
	public static class Upper_excluding_bound_intervalContext extends ParserRuleContext {
		public TerminalNode LEFT_PREF() { return getToken(MTCQParser.LEFT_PREF, 0); }
		public TerminalNode TIME_POINT() { return getToken(MTCQParser.TIME_POINT, 0); }
		public Upper_excluding_bound_intervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_upper_excluding_bound_interval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterUpper_excluding_bound_interval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitUpper_excluding_bound_interval(this);
		}
	}

	public final Upper_excluding_bound_intervalContext upper_excluding_bound_interval() throws RecognitionException {
		Upper_excluding_bound_intervalContext _localctx = new Upper_excluding_bound_intervalContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_upper_excluding_bound_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			match(LEFT_PREF);
			setState(81);
			match(TIME_POINT);
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
	public static class Weak_nextContext extends ParserRuleContext {
		public TerminalNode X_TERMINAL() { return getToken(MTCQParser.X_TERMINAL, 0); }
		public Weak_nextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_weak_next; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterWeak_next(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitWeak_next(this);
		}
	}

	public final Weak_nextContext weak_next() throws RecognitionException {
		Weak_nextContext _localctx = new Weak_nextContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_weak_next);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(X_TERMINAL);
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
	public static class NextContext extends ParserRuleContext {
		public TerminalNode XB_TERMINAL() { return getToken(MTCQParser.XB_TERMINAL, 0); }
		public NextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_next; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterNext(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitNext(this);
		}
	}

	public final NextContext next() throws RecognitionException {
		NextContext _localctx = new NextContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_next);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			match(XB_TERMINAL);
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
	public static class UntilContext extends ParserRuleContext {
		public TerminalNode U_TERMINAL() { return getToken(MTCQParser.U_TERMINAL, 0); }
		public TerminalNode INTERVAL_IDENTIFIER() { return getToken(MTCQParser.INTERVAL_IDENTIFIER, 0); }
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public UntilContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_until; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterUntil(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitUntil(this);
		}
	}

	public final UntilContext until() throws RecognitionException {
		UntilContext _localctx = new UntilContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_until);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(U_TERMINAL);
			setState(90);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INTERVAL_IDENTIFIER) {
				{
				setState(88);
				match(INTERVAL_IDENTIFIER);
				setState(89);
				interval();
				}
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
	public static class EventuallyContext extends ParserRuleContext {
		public TerminalNode F_TERMINAL() { return getToken(MTCQParser.F_TERMINAL, 0); }
		public TerminalNode INTERVAL_IDENTIFIER() { return getToken(MTCQParser.INTERVAL_IDENTIFIER, 0); }
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public EventuallyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eventually; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterEventually(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitEventually(this);
		}
	}

	public final EventuallyContext eventually() throws RecognitionException {
		EventuallyContext _localctx = new EventuallyContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_eventually);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92);
			match(F_TERMINAL);
			setState(95);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INTERVAL_IDENTIFIER) {
				{
				setState(93);
				match(INTERVAL_IDENTIFIER);
				setState(94);
				interval();
				}
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
	public static class AlwaysContext extends ParserRuleContext {
		public TerminalNode G_TERMINAL() { return getToken(MTCQParser.G_TERMINAL, 0); }
		public TerminalNode INTERVAL_IDENTIFIER() { return getToken(MTCQParser.INTERVAL_IDENTIFIER, 0); }
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public AlwaysContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_always; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterAlways(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitAlways(this);
		}
	}

	public final AlwaysContext always() throws RecognitionException {
		AlwaysContext _localctx = new AlwaysContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_always);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			match(G_TERMINAL);
			setState(100);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INTERVAL_IDENTIFIER) {
				{
				setState(98);
				match(INTERVAL_IDENTIFIER);
				setState(99);
				interval();
				}
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
	public static class SubjectContext extends ParserRuleContext {
		public TerminalNode QUANTIFIED_VAR() { return getToken(MTCQParser.QUANTIFIED_VAR, 0); }
		public TerminalNode NOT_QUANTIFIED_VAR() { return getToken(MTCQParser.NOT_QUANTIFIED_VAR, 0); }
		public SubjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subject; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterSubject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitSubject(this);
		}
	}

	public final SubjectContext subject() throws RecognitionException {
		SubjectContext _localctx = new SubjectContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_subject);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			_la = _input.LA(1);
			if ( !(_la==NOT_QUANTIFIED_VAR || _la==QUANTIFIED_VAR) ) {
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
	public static class Role_atomContext extends ParserRuleContext {
		public TerminalNode TERM() { return getToken(MTCQParser.TERM, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MTCQParser.LEFT_PAREN, 0); }
		public List<SubjectContext> subject() {
			return getRuleContexts(SubjectContext.class);
		}
		public SubjectContext subject(int i) {
			return getRuleContext(SubjectContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MTCQParser.RIGHT_PAREN, 0); }
		public Role_atomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_role_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterRole_atom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitRole_atom(this);
		}
	}

	public final Role_atomContext role_atom() throws RecognitionException {
		Role_atomContext _localctx = new Role_atomContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_role_atom);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			match(TERM);
			setState(105);
			match(LEFT_PAREN);
			setState(106);
			subject();
			setState(107);
			match(T__1);
			setState(108);
			subject();
			setState(109);
			match(RIGHT_PAREN);
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
	public static class Concept_atomContext extends ParserRuleContext {
		public TerminalNode TERM() { return getToken(MTCQParser.TERM, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MTCQParser.LEFT_PAREN, 0); }
		public SubjectContext subject() {
			return getRuleContext(SubjectContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MTCQParser.RIGHT_PAREN, 0); }
		public Concept_atomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_concept_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterConcept_atom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitConcept_atom(this);
		}
	}

	public final Concept_atomContext concept_atom() throws RecognitionException {
		Concept_atomContext _localctx = new Concept_atomContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_concept_atom);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			match(TERM);
			setState(112);
			match(LEFT_PAREN);
			setState(113);
			subject();
			setState(114);
			match(RIGHT_PAREN);
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
	public static class AtomContext extends ParserRuleContext {
		public Role_atomContext role_atom() {
			return getRuleContext(Role_atomContext.class,0);
		}
		public Concept_atomContext concept_atom() {
			return getRuleContext(Concept_atomContext.class,0);
		}
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitAtom(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_atom);
		try {
			setState(118);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(116);
				role_atom();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(117);
				concept_atom();
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
	public static class Conjunctive_queryContext extends ParserRuleContext {
		public List<AtomContext> atom() {
			return getRuleContexts(AtomContext.class);
		}
		public AtomContext atom(int i) {
			return getRuleContext(AtomContext.class,i);
		}
		public List<AndContext> and() {
			return getRuleContexts(AndContext.class);
		}
		public AndContext and(int i) {
			return getRuleContext(AndContext.class,i);
		}
		public Conjunctive_queryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conjunctive_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterConjunctive_query(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitConjunctive_query(this);
		}
	}

	public final Conjunctive_queryContext conjunctive_query() throws RecognitionException {
		Conjunctive_queryContext _localctx = new Conjunctive_queryContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_conjunctive_query);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(120);
			atom();
			setState(126);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(121);
					and();
					setState(122);
					atom();
					}
					} 
				}
				setState(128);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
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
	public static class Mltl_formulaContext extends ParserRuleContext {
		public Conjunctive_queryContext conjunctive_query() {
			return getRuleContext(Conjunctive_queryContext.class,0);
		}
		public Prop_booleansContext prop_booleans() {
			return getRuleContext(Prop_booleansContext.class,0);
		}
		public Logic_booleansContext logic_booleans() {
			return getRuleContext(Logic_booleansContext.class,0);
		}
		public TerminalNode LAST_TERMINAL() { return getToken(MTCQParser.LAST_TERMINAL, 0); }
		public TerminalNode END_TERMINAL() { return getToken(MTCQParser.END_TERMINAL, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MTCQParser.LEFT_PAREN, 0); }
		public List<Mltl_formulaContext> mltl_formula() {
			return getRuleContexts(Mltl_formulaContext.class);
		}
		public Mltl_formulaContext mltl_formula(int i) {
			return getRuleContext(Mltl_formulaContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MTCQParser.RIGHT_PAREN, 0); }
		public NotContext not() {
			return getRuleContext(NotContext.class,0);
		}
		public EventuallyContext eventually() {
			return getRuleContext(EventuallyContext.class,0);
		}
		public AlwaysContext always() {
			return getRuleContext(AlwaysContext.class,0);
		}
		public Weak_nextContext weak_next() {
			return getRuleContext(Weak_nextContext.class,0);
		}
		public NextContext next() {
			return getRuleContext(NextContext.class,0);
		}
		public AndContext and() {
			return getRuleContext(AndContext.class,0);
		}
		public OrContext or() {
			return getRuleContext(OrContext.class,0);
		}
		public ImplContext impl() {
			return getRuleContext(ImplContext.class,0);
		}
		public EquivContext equiv() {
			return getRuleContext(EquivContext.class,0);
		}
		public XorContext xor() {
			return getRuleContext(XorContext.class,0);
		}
		public UntilContext until() {
			return getRuleContext(UntilContext.class,0);
		}
		public Mltl_formulaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mltl_formula; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterMltl_formula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitMltl_formula(this);
		}
	}

	public final Mltl_formulaContext mltl_formula() throws RecognitionException {
		return mltl_formula(0);
	}

	private Mltl_formulaContext mltl_formula(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Mltl_formulaContext _localctx = new Mltl_formulaContext(_ctx, _parentState);
		Mltl_formulaContext _prevctx = _localctx;
		int _startState = 44;
		enterRecursionRule(_localctx, 44, RULE_mltl_formula, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TERM:
				{
				setState(130);
				conjunctive_query();
				}
				break;
			case TRUE_TERMINAL:
			case FALSE_TERMINAL:
				{
				setState(131);
				prop_booleans();
				}
				break;
			case TT_TERMINAL:
			case FF_TERMINAL:
				{
				setState(132);
				logic_booleans();
				}
				break;
			case LAST_TERMINAL:
				{
				setState(133);
				match(LAST_TERMINAL);
				}
				break;
			case END_TERMINAL:
				{
				setState(134);
				match(END_TERMINAL);
				}
				break;
			case LEFT_PAREN:
				{
				setState(135);
				match(LEFT_PAREN);
				setState(136);
				mltl_formula(0);
				setState(137);
				match(RIGHT_PAREN);
				}
				break;
			case NOT1_TERMINAL:
			case NOT2_TERMINAL:
				{
				setState(139);
				not();
				setState(140);
				mltl_formula(11);
				}
				break;
			case F_TERMINAL:
				{
				setState(142);
				eventually();
				setState(143);
				mltl_formula(4);
				}
				break;
			case G_TERMINAL:
				{
				setState(145);
				always();
				setState(146);
				mltl_formula(3);
				}
				break;
			case X_TERMINAL:
				{
				setState(148);
				weak_next();
				setState(149);
				mltl_formula(2);
				}
				break;
			case XB_TERMINAL:
				{
				setState(151);
				next();
				setState(152);
				mltl_formula(1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(182);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(180);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
					case 1:
						{
						_localctx = new Mltl_formulaContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_mltl_formula);
						setState(156);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(157);
						and();
						setState(158);
						mltl_formula(11);
						}
						break;
					case 2:
						{
						_localctx = new Mltl_formulaContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_mltl_formula);
						setState(160);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(161);
						or();
						setState(162);
						mltl_formula(10);
						}
						break;
					case 3:
						{
						_localctx = new Mltl_formulaContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_mltl_formula);
						setState(164);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(165);
						impl();
						setState(166);
						mltl_formula(9);
						}
						break;
					case 4:
						{
						_localctx = new Mltl_formulaContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_mltl_formula);
						setState(168);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(169);
						equiv();
						setState(170);
						mltl_formula(8);
						}
						break;
					case 5:
						{
						_localctx = new Mltl_formulaContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_mltl_formula);
						setState(172);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(173);
						xor();
						setState(174);
						mltl_formula(7);
						}
						break;
					case 6:
						{
						_localctx = new Mltl_formulaContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_mltl_formula);
						setState(176);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(177);
						until();
						setState(178);
						mltl_formula(6);
						}
						break;
					}
					} 
				}
				setState(184);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrefixContext extends ParserRuleContext {
		public TerminalNode PREFIX() { return getToken(MTCQParser.PREFIX, 0); }
		public TerminalNode PREFIX_STRING() { return getToken(MTCQParser.PREFIX_STRING, 0); }
		public TerminalNode LEFT_PREF() { return getToken(MTCQParser.LEFT_PREF, 0); }
		public TerminalNode URI() { return getToken(MTCQParser.URI, 0); }
		public TerminalNode RIGHT_PREF() { return getToken(MTCQParser.RIGHT_PREF, 0); }
		public PrefixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterPrefix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitPrefix(this);
		}
	}

	public final PrefixContext prefix() throws RecognitionException {
		PrefixContext _localctx = new PrefixContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_prefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
			match(PREFIX);
			setState(186);
			match(PREFIX_STRING);
			setState(187);
			match(LEFT_PREF);
			setState(188);
			match(URI);
			setState(189);
			match(RIGHT_PREF);
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
	public static class StartContext extends ParserRuleContext {
		public Mltl_formulaContext mltl_formula() {
			return getRuleContext(Mltl_formulaContext.class,0);
		}
		public List<PrefixContext> prefix() {
			return getRuleContexts(PrefixContext.class);
		}
		public PrefixContext prefix(int i) {
			return getRuleContext(PrefixContext.class,i);
		}
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitStart(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_start);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PREFIX) {
				{
				{
				setState(191);
				prefix();
				}
				}
				setState(196);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(197);
			mltl_formula(0);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 22:
			return mltl_formula_sempred((Mltl_formulaContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean mltl_formula_sempred(Mltl_formulaContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 10);
		case 1:
			return precpred(_ctx, 9);
		case 2:
			return precpred(_ctx, 8);
		case 3:
			return precpred(_ctx, 7);
		case 4:
			return precpred(_ctx, 6);
		case 5:
			return precpred(_ctx, 5);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001+\u00c8\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\b\u0003\bF\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f"+
		"\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e[\b\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f`\b\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0003\u0010e\b\u0010\u0001\u0011\u0001\u0011"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0014\u0001\u0014\u0003\u0014w\b\u0014\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0005\u0015}\b\u0015\n\u0015\f\u0015\u0080\t"+
		"\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u009b\b\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u00b5\b\u0016\n"+
		"\u0016\f\u0016\u00b8\t\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0005\u0018\u00c1\b\u0018\n"+
		"\u0018\f\u0018\u00c4\t\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0000"+
		"\u0001,\u0019\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \"$&(*,.0\u0000\b\u0001\u0000\u001a\u001b\u0001"+
		"\u0000\u001c\u001d\u0001\u0000\r\u000e\u0001\u0000\u000f\u0010\u0001\u0000"+
		"\u0011\u0012\u0001\u0000\u0013\u0014\u0001\u0000\u0015\u0016\u0001\u0000"+
		"$%\u00c6\u00002\u0001\u0000\u0000\u0000\u00024\u0001\u0000\u0000\u0000"+
		"\u00046\u0001\u0000\u0000\u0000\u00068\u0001\u0000\u0000\u0000\b:\u0001"+
		"\u0000\u0000\u0000\n<\u0001\u0000\u0000\u0000\f>\u0001\u0000\u0000\u0000"+
		"\u000e@\u0001\u0000\u0000\u0000\u0010E\u0001\u0000\u0000\u0000\u0012G"+
		"\u0001\u0000\u0000\u0000\u0014M\u0001\u0000\u0000\u0000\u0016P\u0001\u0000"+
		"\u0000\u0000\u0018S\u0001\u0000\u0000\u0000\u001aU\u0001\u0000\u0000\u0000"+
		"\u001cW\u0001\u0000\u0000\u0000\u001e\\\u0001\u0000\u0000\u0000 a\u0001"+
		"\u0000\u0000\u0000\"f\u0001\u0000\u0000\u0000$h\u0001\u0000\u0000\u0000"+
		"&o\u0001\u0000\u0000\u0000(v\u0001\u0000\u0000\u0000*x\u0001\u0000\u0000"+
		"\u0000,\u009a\u0001\u0000\u0000\u0000.\u00b9\u0001\u0000\u0000\u00000"+
		"\u00c2\u0001\u0000\u0000\u000023\u0007\u0000\u0000\u00003\u0001\u0001"+
		"\u0000\u0000\u000045\u0007\u0001\u0000\u00005\u0003\u0001\u0000\u0000"+
		"\u000067\u0007\u0002\u0000\u00007\u0005\u0001\u0000\u0000\u000089\u0007"+
		"\u0003\u0000\u00009\u0007\u0001\u0000\u0000\u0000:;\u0007\u0004\u0000"+
		"\u0000;\t\u0001\u0000\u0000\u0000<=\u0007\u0005\u0000\u0000=\u000b\u0001"+
		"\u0000\u0000\u0000>?\u0007\u0006\u0000\u0000?\r\u0001\u0000\u0000\u0000"+
		"@A\u0005\u0017\u0000\u0000A\u000f\u0001\u0000\u0000\u0000BF\u0003\u0012"+
		"\t\u0000CF\u0003\u0014\n\u0000DF\u0003\u0016\u000b\u0000EB\u0001\u0000"+
		"\u0000\u0000EC\u0001\u0000\u0000\u0000ED\u0001\u0000\u0000\u0000F\u0011"+
		"\u0001\u0000\u0000\u0000GH\u0005\u0001\u0000\u0000HI\u0005(\u0000\u0000"+
		"IJ\u0005\u0002\u0000\u0000JK\u0005(\u0000\u0000KL\u0005\u0003\u0000\u0000"+
		"L\u0013\u0001\u0000\u0000\u0000MN\u0005\u0004\u0000\u0000NO\u0005(\u0000"+
		"\u0000O\u0015\u0001\u0000\u0000\u0000PQ\u0005\u001e\u0000\u0000QR\u0005"+
		"(\u0000\u0000R\u0017\u0001\u0000\u0000\u0000ST\u0005\u0006\u0000\u0000"+
		"T\u0019\u0001\u0000\u0000\u0000UV\u0005\u0007\u0000\u0000V\u001b\u0001"+
		"\u0000\u0000\u0000WZ\u0005\b\u0000\u0000XY\u0005\u0005\u0000\u0000Y[\u0003"+
		"\u0010\b\u0000ZX\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[\u001d"+
		"\u0001\u0000\u0000\u0000\\_\u0005\t\u0000\u0000]^\u0005\u0005\u0000\u0000"+
		"^`\u0003\u0010\b\u0000_]\u0001\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000"+
		"`\u001f\u0001\u0000\u0000\u0000ad\u0005\n\u0000\u0000bc\u0005\u0005\u0000"+
		"\u0000ce\u0003\u0010\b\u0000db\u0001\u0000\u0000\u0000de\u0001\u0000\u0000"+
		"\u0000e!\u0001\u0000\u0000\u0000fg\u0007\u0007\u0000\u0000g#\u0001\u0000"+
		"\u0000\u0000hi\u0005&\u0000\u0000ij\u0005\u0018\u0000\u0000jk\u0003\""+
		"\u0011\u0000kl\u0005\u0002\u0000\u0000lm\u0003\"\u0011\u0000mn\u0005\u0019"+
		"\u0000\u0000n%\u0001\u0000\u0000\u0000op\u0005&\u0000\u0000pq\u0005\u0018"+
		"\u0000\u0000qr\u0003\"\u0011\u0000rs\u0005\u0019\u0000\u0000s\'\u0001"+
		"\u0000\u0000\u0000tw\u0003$\u0012\u0000uw\u0003&\u0013\u0000vt\u0001\u0000"+
		"\u0000\u0000vu\u0001\u0000\u0000\u0000w)\u0001\u0000\u0000\u0000x~\u0003"+
		"(\u0014\u0000yz\u0003\u0006\u0003\u0000z{\u0003(\u0014\u0000{}\u0001\u0000"+
		"\u0000\u0000|y\u0001\u0000\u0000\u0000}\u0080\u0001\u0000\u0000\u0000"+
		"~|\u0001\u0000\u0000\u0000~\u007f\u0001\u0000\u0000\u0000\u007f+\u0001"+
		"\u0000\u0000\u0000\u0080~\u0001\u0000\u0000\u0000\u0081\u0082\u0006\u0016"+
		"\uffff\uffff\u0000\u0082\u009b\u0003*\u0015\u0000\u0083\u009b\u0003\u0000"+
		"\u0000\u0000\u0084\u009b\u0003\u0002\u0001\u0000\u0085\u009b\u0005\u000b"+
		"\u0000\u0000\u0086\u009b\u0005\f\u0000\u0000\u0087\u0088\u0005\u0018\u0000"+
		"\u0000\u0088\u0089\u0003,\u0016\u0000\u0089\u008a\u0005\u0019\u0000\u0000"+
		"\u008a\u009b\u0001\u0000\u0000\u0000\u008b\u008c\u0003\u0004\u0002\u0000"+
		"\u008c\u008d\u0003,\u0016\u000b\u008d\u009b\u0001\u0000\u0000\u0000\u008e"+
		"\u008f\u0003\u001e\u000f\u0000\u008f\u0090\u0003,\u0016\u0004\u0090\u009b"+
		"\u0001\u0000\u0000\u0000\u0091\u0092\u0003 \u0010\u0000\u0092\u0093\u0003"+
		",\u0016\u0003\u0093\u009b\u0001\u0000\u0000\u0000\u0094\u0095\u0003\u0018"+
		"\f\u0000\u0095\u0096\u0003,\u0016\u0002\u0096\u009b\u0001\u0000\u0000"+
		"\u0000\u0097\u0098\u0003\u001a\r\u0000\u0098\u0099\u0003,\u0016\u0001"+
		"\u0099\u009b\u0001\u0000\u0000\u0000\u009a\u0081\u0001\u0000\u0000\u0000"+
		"\u009a\u0083\u0001\u0000\u0000\u0000\u009a\u0084\u0001\u0000\u0000\u0000"+
		"\u009a\u0085\u0001\u0000\u0000\u0000\u009a\u0086\u0001\u0000\u0000\u0000"+
		"\u009a\u0087\u0001\u0000\u0000\u0000\u009a\u008b\u0001\u0000\u0000\u0000"+
		"\u009a\u008e\u0001\u0000\u0000\u0000\u009a\u0091\u0001\u0000\u0000\u0000"+
		"\u009a\u0094\u0001\u0000\u0000\u0000\u009a\u0097\u0001\u0000\u0000\u0000"+
		"\u009b\u00b6\u0001\u0000\u0000\u0000\u009c\u009d\n\n\u0000\u0000\u009d"+
		"\u009e\u0003\u0006\u0003\u0000\u009e\u009f\u0003,\u0016\u000b\u009f\u00b5"+
		"\u0001\u0000\u0000\u0000\u00a0\u00a1\n\t\u0000\u0000\u00a1\u00a2\u0003"+
		"\b\u0004\u0000\u00a2\u00a3\u0003,\u0016\n\u00a3\u00b5\u0001\u0000\u0000"+
		"\u0000\u00a4\u00a5\n\b\u0000\u0000\u00a5\u00a6\u0003\n\u0005\u0000\u00a6"+
		"\u00a7\u0003,\u0016\t\u00a7\u00b5\u0001\u0000\u0000\u0000\u00a8\u00a9"+
		"\n\u0007\u0000\u0000\u00a9\u00aa\u0003\f\u0006\u0000\u00aa\u00ab\u0003"+
		",\u0016\b\u00ab\u00b5\u0001\u0000\u0000\u0000\u00ac\u00ad\n\u0006\u0000"+
		"\u0000\u00ad\u00ae\u0003\u000e\u0007\u0000\u00ae\u00af\u0003,\u0016\u0007"+
		"\u00af\u00b5\u0001\u0000\u0000\u0000\u00b0\u00b1\n\u0005\u0000\u0000\u00b1"+
		"\u00b2\u0003\u001c\u000e\u0000\u00b2\u00b3\u0003,\u0016\u0006\u00b3\u00b5"+
		"\u0001\u0000\u0000\u0000\u00b4\u009c\u0001\u0000\u0000\u0000\u00b4\u00a0"+
		"\u0001\u0000\u0000\u0000\u00b4\u00a4\u0001\u0000\u0000\u0000\u00b4\u00a8"+
		"\u0001\u0000\u0000\u0000\u00b4\u00ac\u0001\u0000\u0000\u0000\u00b4\u00b0"+
		"\u0001\u0000\u0000\u0000\u00b5\u00b8\u0001\u0000\u0000\u0000\u00b6\u00b4"+
		"\u0001\u0000\u0000\u0000\u00b6\u00b7\u0001\u0000\u0000\u0000\u00b7-\u0001"+
		"\u0000\u0000\u0000\u00b8\u00b6\u0001\u0000\u0000\u0000\u00b9\u00ba\u0005"+
		"!\u0000\u0000\u00ba\u00bb\u0005\"\u0000\u0000\u00bb\u00bc\u0005\u001e"+
		"\u0000\u0000\u00bc\u00bd\u0005\'\u0000\u0000\u00bd\u00be\u0005\u001f\u0000"+
		"\u0000\u00be/\u0001\u0000\u0000\u0000\u00bf\u00c1\u0003.\u0017\u0000\u00c0"+
		"\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c4\u0001\u0000\u0000\u0000\u00c2"+
		"\u00c0\u0001\u0000\u0000\u0000\u00c2\u00c3\u0001\u0000\u0000\u0000\u00c3"+
		"\u00c5\u0001\u0000\u0000\u0000\u00c4\u00c2\u0001\u0000\u0000\u0000\u00c5"+
		"\u00c6\u0003,\u0016\u0000\u00c61\u0001\u0000\u0000\u0000\nEZ_dv~\u009a"+
		"\u00b4\u00b6\u00c2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}