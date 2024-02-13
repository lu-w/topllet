// Generated from MTCQ.g4 by ANTLR 4.13.1

package openllet.mtcq.parser;

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
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, X_TERMINAL=12, XB_TERMINAL=13, UI_TERMINAL=14, U_TERMINAL=15, 
		FI_TERMINAL=16, F_TERMINAL=17, GI_TERMINAL=18, G_TERMINAL=19, LAST_TERMINAL=20, 
		END_TERMINAL=21, NOT1_TERMINAL=22, NOT2_TERMINAL=23, AND1_TERMINAL=24, 
		AND2_TERMINAL=25, OR1_TERMINAL=26, OR2_TERMINAL=27, IMPL1_TERMINAL=28, 
		IMPL2_TERMINAL=29, EQUIV1_TERMINAL=30, EQUIV2_TERMINAL=31, XOR_TERMINAL=32, 
		TRUE_TERMINAL=33, FALSE_TERMINAL=34, TT_TERMINAL=35, FF_TERMINAL=36, TIME_POINT=37, 
		NAME=38, URI=39, COMMENT=40, WS=41;
	public static final int
		RULE_trace_position = 0, RULE_prop_booleans = 1, RULE_logic_booleans = 2, 
		RULE_not = 3, RULE_and = 4, RULE_or = 5, RULE_impl = 6, RULE_equiv = 7, 
		RULE_xor = 8, RULE_full_interval = 9, RULE_upper_including_bound_interval = 10, 
		RULE_upper_excluding_bound_interval = 11, RULE_interval = 12, RULE_weak_next = 13, 
		RULE_next = 14, RULE_until = 15, RULE_eventually = 16, RULE_always = 17, 
		RULE_term = 18, RULE_subject = 19, RULE_role_atom = 20, RULE_concept_atom = 21, 
		RULE_atom = 22, RULE_conjunctive_query = 23, RULE_mtcq_formula = 24, RULE_prefix = 25, 
		RULE_start = 26;
	private static String[] makeRuleNames() {
		return new String[] {
			"trace_position", "prop_booleans", "logic_booleans", "not", "and", "or", 
			"impl", "equiv", "xor", "full_interval", "upper_including_bound_interval", 
			"upper_excluding_bound_interval", "interval", "weak_next", "next", "until", 
			"eventually", "always", "term", "subject", "role_atom", "concept_atom", 
			"atom", "conjunctive_query", "mtcq_formula", "prefix", "start"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'['", "','", "']'", "'<='", "'<'", "':'", "'?'", "'('", "')'", 
			"'PREFIX'", "'>'", "'X'", "'X[!]'", "'U_'", "'U'", "'F_'", "'F'", "'G_'", 
			"'G'", "'last'", "'end'", "'!'", "'~'", "'&'", "'&&'", "'|'", "'||'", 
			"'->'", "'=>'", "'<->'", "'<=>'", "'^'", "'TRUE'", "'FALSE'", "'TT'", 
			"'FF'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"X_TERMINAL", "XB_TERMINAL", "UI_TERMINAL", "U_TERMINAL", "FI_TERMINAL", 
			"F_TERMINAL", "GI_TERMINAL", "G_TERMINAL", "LAST_TERMINAL", "END_TERMINAL", 
			"NOT1_TERMINAL", "NOT2_TERMINAL", "AND1_TERMINAL", "AND2_TERMINAL", "OR1_TERMINAL", 
			"OR2_TERMINAL", "IMPL1_TERMINAL", "IMPL2_TERMINAL", "EQUIV1_TERMINAL", 
			"EQUIV2_TERMINAL", "XOR_TERMINAL", "TRUE_TERMINAL", "FALSE_TERMINAL", 
			"TT_TERMINAL", "FF_TERMINAL", "TIME_POINT", "NAME", "URI", "COMMENT", 
			"WS"
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
	public static class Trace_positionContext extends ParserRuleContext {
		public TerminalNode LAST_TERMINAL() { return getToken(MTCQParser.LAST_TERMINAL, 0); }
		public TerminalNode END_TERMINAL() { return getToken(MTCQParser.END_TERMINAL, 0); }
		public Trace_positionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trace_position; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterTrace_position(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitTrace_position(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitTrace_position(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Trace_positionContext trace_position() throws RecognitionException {
		Trace_positionContext _localctx = new Trace_positionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_trace_position);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			_la = _input.LA(1);
			if ( !(_la==LAST_TERMINAL || _la==END_TERMINAL) ) {
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitProp_booleans(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Prop_booleansContext prop_booleans() throws RecognitionException {
		Prop_booleansContext _localctx = new Prop_booleansContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_prop_booleans);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitLogic_booleans(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Logic_booleansContext logic_booleans() throws RecognitionException {
		Logic_booleansContext _localctx = new Logic_booleansContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_logic_booleans);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(58);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitNot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotContext not() throws RecognitionException {
		NotContext _localctx = new NotContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_not);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitAnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_and);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitOr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_or);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitImpl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImplContext impl() throws RecognitionException {
		ImplContext _localctx = new ImplContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_impl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitEquiv(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EquivContext equiv() throws RecognitionException {
		EquivContext _localctx = new EquivContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_equiv);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitXor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XorContext xor() throws RecognitionException {
		XorContext _localctx = new XorContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_xor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitFull_interval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Full_intervalContext full_interval() throws RecognitionException {
		Full_intervalContext _localctx = new Full_intervalContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_full_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			match(T__0);
			setState(73);
			match(TIME_POINT);
			setState(74);
			match(T__1);
			setState(75);
			match(TIME_POINT);
			setState(76);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitUpper_including_bound_interval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Upper_including_bound_intervalContext upper_including_bound_interval() throws RecognitionException {
		Upper_including_bound_intervalContext _localctx = new Upper_including_bound_intervalContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_upper_including_bound_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			match(T__3);
			setState(79);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitUpper_excluding_bound_interval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Upper_excluding_bound_intervalContext upper_excluding_bound_interval() throws RecognitionException {
		Upper_excluding_bound_intervalContext _localctx = new Upper_excluding_bound_intervalContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_upper_excluding_bound_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			match(T__4);
			setState(82);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalContext interval() throws RecognitionException {
		IntervalContext _localctx = new IntervalContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_interval);
		try {
			setState(87);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(84);
				full_interval();
				}
				break;
			case T__3:
				enterOuterAlt(_localctx, 2);
				{
				setState(85);
				upper_including_bound_interval();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 3);
				{
				setState(86);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitWeak_next(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Weak_nextContext weak_next() throws RecognitionException {
		Weak_nextContext _localctx = new Weak_nextContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_weak_next);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitNext(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NextContext next() throws RecognitionException {
		NextContext _localctx = new NextContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_next);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(91);
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
		public TerminalNode UI_TERMINAL() { return getToken(MTCQParser.UI_TERMINAL, 0); }
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitUntil(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UntilContext until() throws RecognitionException {
		UntilContext _localctx = new UntilContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_until);
		try {
			setState(96);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case U_TERMINAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(93);
				match(U_TERMINAL);
				}
				break;
			case UI_TERMINAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(94);
				match(UI_TERMINAL);
				setState(95);
				interval();
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
	public static class EventuallyContext extends ParserRuleContext {
		public TerminalNode F_TERMINAL() { return getToken(MTCQParser.F_TERMINAL, 0); }
		public TerminalNode FI_TERMINAL() { return getToken(MTCQParser.FI_TERMINAL, 0); }
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitEventually(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EventuallyContext eventually() throws RecognitionException {
		EventuallyContext _localctx = new EventuallyContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_eventually);
		try {
			setState(101);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case F_TERMINAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(98);
				match(F_TERMINAL);
				}
				break;
			case FI_TERMINAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(99);
				match(FI_TERMINAL);
				setState(100);
				interval();
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
	public static class AlwaysContext extends ParserRuleContext {
		public TerminalNode G_TERMINAL() { return getToken(MTCQParser.G_TERMINAL, 0); }
		public TerminalNode GI_TERMINAL() { return getToken(MTCQParser.GI_TERMINAL, 0); }
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitAlways(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlwaysContext always() throws RecognitionException {
		AlwaysContext _localctx = new AlwaysContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_always);
		try {
			setState(106);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case G_TERMINAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(103);
				match(G_TERMINAL);
				}
				break;
			case GI_TERMINAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(104);
				match(GI_TERMINAL);
				setState(105);
				interval();
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
	public static class TermContext extends ParserRuleContext {
		public List<TerminalNode> NAME() { return getTokens(MTCQParser.NAME); }
		public TerminalNode NAME(int i) {
			return getToken(MTCQParser.NAME, i);
		}
		public TerminalNode URI() { return getToken(MTCQParser.URI, 0); }
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_term);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(108);
				match(URI);
				}
				break;
			case 2:
				{
				setState(109);
				match(NAME);
				setState(110);
				match(T__5);
				}
				break;
			}
			setState(113);
			match(NAME);
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
		public TerminalNode NAME() { return getToken(MTCQParser.NAME, 0); }
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitSubject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubjectContext subject() throws RecognitionException {
		SubjectContext _localctx = new SubjectContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_subject);
		try {
			setState(118);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__6:
				enterOuterAlt(_localctx, 1);
				{
				setState(115);
				match(T__6);
				setState(116);
				match(NAME);
				}
				break;
			case NAME:
			case URI:
				enterOuterAlt(_localctx, 2);
				{
				setState(117);
				term();
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
	public static class Role_atomContext extends ParserRuleContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public List<SubjectContext> subject() {
			return getRuleContexts(SubjectContext.class);
		}
		public SubjectContext subject(int i) {
			return getRuleContext(SubjectContext.class,i);
		}
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitRole_atom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Role_atomContext role_atom() throws RecognitionException {
		Role_atomContext _localctx = new Role_atomContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_role_atom);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(120);
			term();
			setState(121);
			match(T__7);
			setState(122);
			subject();
			setState(123);
			match(T__1);
			setState(124);
			subject();
			setState(125);
			match(T__8);
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
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public SubjectContext subject() {
			return getRuleContext(SubjectContext.class,0);
		}
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitConcept_atom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Concept_atomContext concept_atom() throws RecognitionException {
		Concept_atomContext _localctx = new Concept_atomContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_concept_atom);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			term();
			setState(128);
			match(T__7);
			setState(129);
			subject();
			setState(130);
			match(T__8);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_atom);
		try {
			setState(134);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(132);
				role_atom();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(133);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitConjunctive_query(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Conjunctive_queryContext conjunctive_query() throws RecognitionException {
		Conjunctive_queryContext _localctx = new Conjunctive_queryContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_conjunctive_query);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			atom();
			setState(142);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(137);
					and();
					setState(138);
					atom();
					}
					} 
				}
				setState(144);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
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
	public static class Mtcq_formulaContext extends ParserRuleContext {
		public Mtcq_formulaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mtcq_formula; }
	 
		public Mtcq_formulaContext() { }
		public void copyFrom(Mtcq_formulaContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TracePositionFormulaContext extends Mtcq_formulaContext {
		public Trace_positionContext trace_position() {
			return getRuleContext(Trace_positionContext.class,0);
		}
		public TracePositionFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterTracePositionFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitTracePositionFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitTracePositionFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicBooleanFormulaContext extends Mtcq_formulaContext {
		public Logic_booleansContext logic_booleans() {
			return getRuleContext(Logic_booleansContext.class,0);
		}
		public LogicBooleanFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterLogicBooleanFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitLogicBooleanFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitLogicBooleanFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class XorFormulaContext extends Mtcq_formulaContext {
		public List<Mtcq_formulaContext> mtcq_formula() {
			return getRuleContexts(Mtcq_formulaContext.class);
		}
		public Mtcq_formulaContext mtcq_formula(int i) {
			return getRuleContext(Mtcq_formulaContext.class,i);
		}
		public XorContext xor() {
			return getRuleContext(XorContext.class,0);
		}
		public XorFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterXorFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitXorFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitXorFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ImplFormulaContext extends Mtcq_formulaContext {
		public List<Mtcq_formulaContext> mtcq_formula() {
			return getRuleContexts(Mtcq_formulaContext.class);
		}
		public Mtcq_formulaContext mtcq_formula(int i) {
			return getRuleContext(Mtcq_formulaContext.class,i);
		}
		public ImplContext impl() {
			return getRuleContext(ImplContext.class,0);
		}
		public ImplFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterImplFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitImplFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitImplFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PropositionalBooleanFormulaContext extends Mtcq_formulaContext {
		public Prop_booleansContext prop_booleans() {
			return getRuleContext(Prop_booleansContext.class,0);
		}
		public PropositionalBooleanFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterPropositionalBooleanFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitPropositionalBooleanFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitPropositionalBooleanFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BracketFormulaContext extends Mtcq_formulaContext {
		public Mtcq_formulaContext mtcq_formula() {
			return getRuleContext(Mtcq_formulaContext.class,0);
		}
		public BracketFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterBracketFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitBracketFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitBracketFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AlwaysFormulaContext extends Mtcq_formulaContext {
		public AlwaysContext always() {
			return getRuleContext(AlwaysContext.class,0);
		}
		public Mtcq_formulaContext mtcq_formula() {
			return getRuleContext(Mtcq_formulaContext.class,0);
		}
		public AlwaysFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterAlwaysFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitAlwaysFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitAlwaysFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WeakNextFormulaContext extends Mtcq_formulaContext {
		public Weak_nextContext weak_next() {
			return getRuleContext(Weak_nextContext.class,0);
		}
		public Mtcq_formulaContext mtcq_formula() {
			return getRuleContext(Mtcq_formulaContext.class,0);
		}
		public WeakNextFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterWeakNextFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitWeakNextFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitWeakNextFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EventuallyFormulaContext extends Mtcq_formulaContext {
		public EventuallyContext eventually() {
			return getRuleContext(EventuallyContext.class,0);
		}
		public Mtcq_formulaContext mtcq_formula() {
			return getRuleContext(Mtcq_formulaContext.class,0);
		}
		public EventuallyFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterEventuallyFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitEventuallyFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitEventuallyFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EquivFormulaContext extends Mtcq_formulaContext {
		public List<Mtcq_formulaContext> mtcq_formula() {
			return getRuleContexts(Mtcq_formulaContext.class);
		}
		public Mtcq_formulaContext mtcq_formula(int i) {
			return getRuleContext(Mtcq_formulaContext.class,i);
		}
		public EquivContext equiv() {
			return getRuleContext(EquivContext.class,0);
		}
		public EquivFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterEquivFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitEquivFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitEquivFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConjunctiveQueryFormulaContext extends Mtcq_formulaContext {
		public Conjunctive_queryContext conjunctive_query() {
			return getRuleContext(Conjunctive_queryContext.class,0);
		}
		public ConjunctiveQueryFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterConjunctiveQueryFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitConjunctiveQueryFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitConjunctiveQueryFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NextFormulaContext extends Mtcq_formulaContext {
		public NextContext next() {
			return getRuleContext(NextContext.class,0);
		}
		public Mtcq_formulaContext mtcq_formula() {
			return getRuleContext(Mtcq_formulaContext.class,0);
		}
		public NextFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterNextFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitNextFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitNextFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AndFormulaContext extends Mtcq_formulaContext {
		public List<Mtcq_formulaContext> mtcq_formula() {
			return getRuleContexts(Mtcq_formulaContext.class);
		}
		public Mtcq_formulaContext mtcq_formula(int i) {
			return getRuleContext(Mtcq_formulaContext.class,i);
		}
		public AndContext and() {
			return getRuleContext(AndContext.class,0);
		}
		public AndFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterAndFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitAndFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitAndFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UntilFormulaContext extends Mtcq_formulaContext {
		public List<Mtcq_formulaContext> mtcq_formula() {
			return getRuleContexts(Mtcq_formulaContext.class);
		}
		public Mtcq_formulaContext mtcq_formula(int i) {
			return getRuleContext(Mtcq_formulaContext.class,i);
		}
		public UntilContext until() {
			return getRuleContext(UntilContext.class,0);
		}
		public UntilFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterUntilFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitUntilFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitUntilFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotFormulaContext extends Mtcq_formulaContext {
		public NotContext not() {
			return getRuleContext(NotContext.class,0);
		}
		public Mtcq_formulaContext mtcq_formula() {
			return getRuleContext(Mtcq_formulaContext.class,0);
		}
		public NotFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterNotFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitNotFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitNotFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OrFormulaContext extends Mtcq_formulaContext {
		public List<Mtcq_formulaContext> mtcq_formula() {
			return getRuleContexts(Mtcq_formulaContext.class);
		}
		public Mtcq_formulaContext mtcq_formula(int i) {
			return getRuleContext(Mtcq_formulaContext.class,i);
		}
		public OrContext or() {
			return getRuleContext(OrContext.class,0);
		}
		public OrFormulaContext(Mtcq_formulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).enterOrFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MTCQListener ) ((MTCQListener)listener).exitOrFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitOrFormula(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Mtcq_formulaContext mtcq_formula() throws RecognitionException {
		return mtcq_formula(0);
	}

	private Mtcq_formulaContext mtcq_formula(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Mtcq_formulaContext _localctx = new Mtcq_formulaContext(_ctx, _parentState);
		Mtcq_formulaContext _prevctx = _localctx;
		int _startState = 48;
		enterRecursionRule(_localctx, 48, RULE_mtcq_formula, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NAME:
			case URI:
				{
				_localctx = new ConjunctiveQueryFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(146);
				conjunctive_query();
				}
				break;
			case TRUE_TERMINAL:
			case FALSE_TERMINAL:
				{
				_localctx = new PropositionalBooleanFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(147);
				prop_booleans();
				}
				break;
			case TT_TERMINAL:
			case FF_TERMINAL:
				{
				_localctx = new LogicBooleanFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(148);
				logic_booleans();
				}
				break;
			case LAST_TERMINAL:
			case END_TERMINAL:
				{
				_localctx = new TracePositionFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(149);
				trace_position();
				}
				break;
			case T__7:
				{
				_localctx = new BracketFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(150);
				match(T__7);
				setState(151);
				mtcq_formula(0);
				setState(152);
				match(T__8);
				}
				break;
			case NOT1_TERMINAL:
			case NOT2_TERMINAL:
				{
				_localctx = new NotFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(154);
				not();
				setState(155);
				mtcq_formula(6);
				}
				break;
			case FI_TERMINAL:
			case F_TERMINAL:
				{
				_localctx = new EventuallyFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(157);
				eventually();
				setState(158);
				mtcq_formula(5);
				}
				break;
			case GI_TERMINAL:
			case G_TERMINAL:
				{
				_localctx = new AlwaysFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(160);
				always();
				setState(161);
				mtcq_formula(4);
				}
				break;
			case X_TERMINAL:
				{
				_localctx = new WeakNextFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(163);
				weak_next();
				setState(164);
				mtcq_formula(3);
				}
				break;
			case XB_TERMINAL:
				{
				_localctx = new NextFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(166);
				next();
				setState(167);
				mtcq_formula(2);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(197);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(195);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						_localctx = new AndFormulaContext(new Mtcq_formulaContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mtcq_formula);
						setState(171);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(172);
						and();
						setState(173);
						mtcq_formula(12);
						}
						break;
					case 2:
						{
						_localctx = new OrFormulaContext(new Mtcq_formulaContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mtcq_formula);
						setState(175);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(176);
						or();
						setState(177);
						mtcq_formula(11);
						}
						break;
					case 3:
						{
						_localctx = new ImplFormulaContext(new Mtcq_formulaContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mtcq_formula);
						setState(179);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(180);
						impl();
						setState(181);
						mtcq_formula(10);
						}
						break;
					case 4:
						{
						_localctx = new EquivFormulaContext(new Mtcq_formulaContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mtcq_formula);
						setState(183);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(184);
						equiv();
						setState(185);
						mtcq_formula(9);
						}
						break;
					case 5:
						{
						_localctx = new XorFormulaContext(new Mtcq_formulaContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mtcq_formula);
						setState(187);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(188);
						xor();
						setState(189);
						mtcq_formula(8);
						}
						break;
					case 6:
						{
						_localctx = new UntilFormulaContext(new Mtcq_formulaContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mtcq_formula);
						setState(191);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(192);
						until();
						setState(193);
						mtcq_formula(2);
						}
						break;
					}
					} 
				}
				setState(199);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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
		public TerminalNode NAME() { return getToken(MTCQParser.NAME, 0); }
		public TerminalNode URI() { return getToken(MTCQParser.URI, 0); }
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitPrefix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrefixContext prefix() throws RecognitionException {
		PrefixContext _localctx = new PrefixContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_prefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			match(T__9);
			setState(201);
			match(NAME);
			setState(202);
			match(T__5);
			setState(203);
			match(T__4);
			setState(204);
			match(URI);
			setState(205);
			match(T__10);
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
		public Mtcq_formulaContext mtcq_formula() {
			return getRuleContext(Mtcq_formulaContext.class,0);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MTCQVisitor ) return ((MTCQVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_start);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(210);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__9) {
				{
				{
				setState(207);
				prefix();
				}
				}
				setState(212);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(213);
			mtcq_formula(0);
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
		case 24:
			return mtcq_formula_sempred((Mtcq_formulaContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean mtcq_formula_sempred(Mtcq_formulaContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 11);
		case 1:
			return precpred(_ctx, 10);
		case 2:
			return precpred(_ctx, 9);
		case 3:
			return precpred(_ctx, 8);
		case 4:
			return precpred(_ctx, 7);
		case 5:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001)\u00d8\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\f\u0001\f\u0001\f\u0003\fX\b\f\u0001\r\u0001\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000fa\b\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010f\b\u0010\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0003\u0011k\b\u0011\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0003\u0012p\b\u0012\u0001\u0012\u0001\u0012\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0003\u0013w\b\u0013\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u0087\b\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0005\u0017\u008d\b\u0017\n\u0017\f\u0017\u0090\t\u0017\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u00aa"+
		"\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0005\u0018\u00c4\b\u0018\n\u0018\f\u0018\u00c7\t\u0018\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u001a\u0005\u001a\u00d1\b\u001a\n\u001a\f\u001a\u00d4\t\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0000\u00010\u001b\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,."+
		"024\u0000\b\u0001\u0000\u0014\u0015\u0001\u0000!\"\u0001\u0000#$\u0001"+
		"\u0000\u0016\u0017\u0001\u0000\u0018\u0019\u0001\u0000\u001a\u001b\u0001"+
		"\u0000\u001c\u001d\u0001\u0000\u001e\u001f\u00d6\u00006\u0001\u0000\u0000"+
		"\u0000\u00028\u0001\u0000\u0000\u0000\u0004:\u0001\u0000\u0000\u0000\u0006"+
		"<\u0001\u0000\u0000\u0000\b>\u0001\u0000\u0000\u0000\n@\u0001\u0000\u0000"+
		"\u0000\fB\u0001\u0000\u0000\u0000\u000eD\u0001\u0000\u0000\u0000\u0010"+
		"F\u0001\u0000\u0000\u0000\u0012H\u0001\u0000\u0000\u0000\u0014N\u0001"+
		"\u0000\u0000\u0000\u0016Q\u0001\u0000\u0000\u0000\u0018W\u0001\u0000\u0000"+
		"\u0000\u001aY\u0001\u0000\u0000\u0000\u001c[\u0001\u0000\u0000\u0000\u001e"+
		"`\u0001\u0000\u0000\u0000 e\u0001\u0000\u0000\u0000\"j\u0001\u0000\u0000"+
		"\u0000$o\u0001\u0000\u0000\u0000&v\u0001\u0000\u0000\u0000(x\u0001\u0000"+
		"\u0000\u0000*\u007f\u0001\u0000\u0000\u0000,\u0086\u0001\u0000\u0000\u0000"+
		".\u0088\u0001\u0000\u0000\u00000\u00a9\u0001\u0000\u0000\u00002\u00c8"+
		"\u0001\u0000\u0000\u00004\u00d2\u0001\u0000\u0000\u000067\u0007\u0000"+
		"\u0000\u00007\u0001\u0001\u0000\u0000\u000089\u0007\u0001\u0000\u0000"+
		"9\u0003\u0001\u0000\u0000\u0000:;\u0007\u0002\u0000\u0000;\u0005\u0001"+
		"\u0000\u0000\u0000<=\u0007\u0003\u0000\u0000=\u0007\u0001\u0000\u0000"+
		"\u0000>?\u0007\u0004\u0000\u0000?\t\u0001\u0000\u0000\u0000@A\u0007\u0005"+
		"\u0000\u0000A\u000b\u0001\u0000\u0000\u0000BC\u0007\u0006\u0000\u0000"+
		"C\r\u0001\u0000\u0000\u0000DE\u0007\u0007\u0000\u0000E\u000f\u0001\u0000"+
		"\u0000\u0000FG\u0005 \u0000\u0000G\u0011\u0001\u0000\u0000\u0000HI\u0005"+
		"\u0001\u0000\u0000IJ\u0005%\u0000\u0000JK\u0005\u0002\u0000\u0000KL\u0005"+
		"%\u0000\u0000LM\u0005\u0003\u0000\u0000M\u0013\u0001\u0000\u0000\u0000"+
		"NO\u0005\u0004\u0000\u0000OP\u0005%\u0000\u0000P\u0015\u0001\u0000\u0000"+
		"\u0000QR\u0005\u0005\u0000\u0000RS\u0005%\u0000\u0000S\u0017\u0001\u0000"+
		"\u0000\u0000TX\u0003\u0012\t\u0000UX\u0003\u0014\n\u0000VX\u0003\u0016"+
		"\u000b\u0000WT\u0001\u0000\u0000\u0000WU\u0001\u0000\u0000\u0000WV\u0001"+
		"\u0000\u0000\u0000X\u0019\u0001\u0000\u0000\u0000YZ\u0005\f\u0000\u0000"+
		"Z\u001b\u0001\u0000\u0000\u0000[\\\u0005\r\u0000\u0000\\\u001d\u0001\u0000"+
		"\u0000\u0000]a\u0005\u000f\u0000\u0000^_\u0005\u000e\u0000\u0000_a\u0003"+
		"\u0018\f\u0000`]\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000\u0000a\u001f"+
		"\u0001\u0000\u0000\u0000bf\u0005\u0011\u0000\u0000cd\u0005\u0010\u0000"+
		"\u0000df\u0003\u0018\f\u0000eb\u0001\u0000\u0000\u0000ec\u0001\u0000\u0000"+
		"\u0000f!\u0001\u0000\u0000\u0000gk\u0005\u0013\u0000\u0000hi\u0005\u0012"+
		"\u0000\u0000ik\u0003\u0018\f\u0000jg\u0001\u0000\u0000\u0000jh\u0001\u0000"+
		"\u0000\u0000k#\u0001\u0000\u0000\u0000lp\u0005\'\u0000\u0000mn\u0005&"+
		"\u0000\u0000np\u0005\u0006\u0000\u0000ol\u0001\u0000\u0000\u0000om\u0001"+
		"\u0000\u0000\u0000op\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000\u0000"+
		"qr\u0005&\u0000\u0000r%\u0001\u0000\u0000\u0000st\u0005\u0007\u0000\u0000"+
		"tw\u0005&\u0000\u0000uw\u0003$\u0012\u0000vs\u0001\u0000\u0000\u0000v"+
		"u\u0001\u0000\u0000\u0000w\'\u0001\u0000\u0000\u0000xy\u0003$\u0012\u0000"+
		"yz\u0005\b\u0000\u0000z{\u0003&\u0013\u0000{|\u0005\u0002\u0000\u0000"+
		"|}\u0003&\u0013\u0000}~\u0005\t\u0000\u0000~)\u0001\u0000\u0000\u0000"+
		"\u007f\u0080\u0003$\u0012\u0000\u0080\u0081\u0005\b\u0000\u0000\u0081"+
		"\u0082\u0003&\u0013\u0000\u0082\u0083\u0005\t\u0000\u0000\u0083+\u0001"+
		"\u0000\u0000\u0000\u0084\u0087\u0003(\u0014\u0000\u0085\u0087\u0003*\u0015"+
		"\u0000\u0086\u0084\u0001\u0000\u0000\u0000\u0086\u0085\u0001\u0000\u0000"+
		"\u0000\u0087-\u0001\u0000\u0000\u0000\u0088\u008e\u0003,\u0016\u0000\u0089"+
		"\u008a\u0003\b\u0004\u0000\u008a\u008b\u0003,\u0016\u0000\u008b\u008d"+
		"\u0001\u0000\u0000\u0000\u008c\u0089\u0001\u0000\u0000\u0000\u008d\u0090"+
		"\u0001\u0000\u0000\u0000\u008e\u008c\u0001\u0000\u0000\u0000\u008e\u008f"+
		"\u0001\u0000\u0000\u0000\u008f/\u0001\u0000\u0000\u0000\u0090\u008e\u0001"+
		"\u0000\u0000\u0000\u0091\u0092\u0006\u0018\uffff\uffff\u0000\u0092\u00aa"+
		"\u0003.\u0017\u0000\u0093\u00aa\u0003\u0002\u0001\u0000\u0094\u00aa\u0003"+
		"\u0004\u0002\u0000\u0095\u00aa\u0003\u0000\u0000\u0000\u0096\u0097\u0005"+
		"\b\u0000\u0000\u0097\u0098\u00030\u0018\u0000\u0098\u0099\u0005\t\u0000"+
		"\u0000\u0099\u00aa\u0001\u0000\u0000\u0000\u009a\u009b\u0003\u0006\u0003"+
		"\u0000\u009b\u009c\u00030\u0018\u0006\u009c\u00aa\u0001\u0000\u0000\u0000"+
		"\u009d\u009e\u0003 \u0010\u0000\u009e\u009f\u00030\u0018\u0005\u009f\u00aa"+
		"\u0001\u0000\u0000\u0000\u00a0\u00a1\u0003\"\u0011\u0000\u00a1\u00a2\u0003"+
		"0\u0018\u0004\u00a2\u00aa\u0001\u0000\u0000\u0000\u00a3\u00a4\u0003\u001a"+
		"\r\u0000\u00a4\u00a5\u00030\u0018\u0003\u00a5\u00aa\u0001\u0000\u0000"+
		"\u0000\u00a6\u00a7\u0003\u001c\u000e\u0000\u00a7\u00a8\u00030\u0018\u0002"+
		"\u00a8\u00aa\u0001\u0000\u0000\u0000\u00a9\u0091\u0001\u0000\u0000\u0000"+
		"\u00a9\u0093\u0001\u0000\u0000\u0000\u00a9\u0094\u0001\u0000\u0000\u0000"+
		"\u00a9\u0095\u0001\u0000\u0000\u0000\u00a9\u0096\u0001\u0000\u0000\u0000"+
		"\u00a9\u009a\u0001\u0000\u0000\u0000\u00a9\u009d\u0001\u0000\u0000\u0000"+
		"\u00a9\u00a0\u0001\u0000\u0000\u0000\u00a9\u00a3\u0001\u0000\u0000\u0000"+
		"\u00a9\u00a6\u0001\u0000\u0000\u0000\u00aa\u00c5\u0001\u0000\u0000\u0000"+
		"\u00ab\u00ac\n\u000b\u0000\u0000\u00ac\u00ad\u0003\b\u0004\u0000\u00ad"+
		"\u00ae\u00030\u0018\f\u00ae\u00c4\u0001\u0000\u0000\u0000\u00af\u00b0"+
		"\n\n\u0000\u0000\u00b0\u00b1\u0003\n\u0005\u0000\u00b1\u00b2\u00030\u0018"+
		"\u000b\u00b2\u00c4\u0001\u0000\u0000\u0000\u00b3\u00b4\n\t\u0000\u0000"+
		"\u00b4\u00b5\u0003\f\u0006\u0000\u00b5\u00b6\u00030\u0018\n\u00b6\u00c4"+
		"\u0001\u0000\u0000\u0000\u00b7\u00b8\n\b\u0000\u0000\u00b8\u00b9\u0003"+
		"\u000e\u0007\u0000\u00b9\u00ba\u00030\u0018\t\u00ba\u00c4\u0001\u0000"+
		"\u0000\u0000\u00bb\u00bc\n\u0007\u0000\u0000\u00bc\u00bd\u0003\u0010\b"+
		"\u0000\u00bd\u00be\u00030\u0018\b\u00be\u00c4\u0001\u0000\u0000\u0000"+
		"\u00bf\u00c0\n\u0001\u0000\u0000\u00c0\u00c1\u0003\u001e\u000f\u0000\u00c1"+
		"\u00c2\u00030\u0018\u0002\u00c2\u00c4\u0001\u0000\u0000\u0000\u00c3\u00ab"+
		"\u0001\u0000\u0000\u0000\u00c3\u00af\u0001\u0000\u0000\u0000\u00c3\u00b3"+
		"\u0001\u0000\u0000\u0000\u00c3\u00b7\u0001\u0000\u0000\u0000\u00c3\u00bb"+
		"\u0001\u0000\u0000\u0000\u00c3\u00bf\u0001\u0000\u0000\u0000\u00c4\u00c7"+
		"\u0001\u0000\u0000\u0000\u00c5\u00c3\u0001\u0000\u0000\u0000\u00c5\u00c6"+
		"\u0001\u0000\u0000\u0000\u00c61\u0001\u0000\u0000\u0000\u00c7\u00c5\u0001"+
		"\u0000\u0000\u0000\u00c8\u00c9\u0005\n\u0000\u0000\u00c9\u00ca\u0005&"+
		"\u0000\u0000\u00ca\u00cb\u0005\u0006\u0000\u0000\u00cb\u00cc\u0005\u0005"+
		"\u0000\u0000\u00cc\u00cd\u0005\'\u0000\u0000\u00cd\u00ce\u0005\u000b\u0000"+
		"\u0000\u00ce3\u0001\u0000\u0000\u0000\u00cf\u00d1\u00032\u0019\u0000\u00d0"+
		"\u00cf\u0001\u0000\u0000\u0000\u00d1\u00d4\u0001\u0000\u0000\u0000\u00d2"+
		"\u00d0\u0001\u0000\u0000\u0000\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d3"+
		"\u00d5\u0001\u0000\u0000\u0000\u00d4\u00d2\u0001\u0000\u0000\u0000\u00d5"+
		"\u00d6\u00030\u0018\u0000\u00d65\u0001\u0000\u0000\u0000\fW`ejov\u0086"+
		"\u008e\u00a9\u00c3\u00c5\u00d2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}