// Generated from MTCQ.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MTCQParser}.
 */
public interface MTCQListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MTCQParser#trace_position}.
	 * @param ctx the parse tree
	 */
	void enterTrace_position(MTCQParser.Trace_positionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#trace_position}.
	 * @param ctx the parse tree
	 */
	void exitTrace_position(MTCQParser.Trace_positionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#prop_booleans}.
	 * @param ctx the parse tree
	 */
	void enterProp_booleans(MTCQParser.Prop_booleansContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#prop_booleans}.
	 * @param ctx the parse tree
	 */
	void exitProp_booleans(MTCQParser.Prop_booleansContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#logic_booleans}.
	 * @param ctx the parse tree
	 */
	void enterLogic_booleans(MTCQParser.Logic_booleansContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#logic_booleans}.
	 * @param ctx the parse tree
	 */
	void exitLogic_booleans(MTCQParser.Logic_booleansContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#not}.
	 * @param ctx the parse tree
	 */
	void enterNot(MTCQParser.NotContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#not}.
	 * @param ctx the parse tree
	 */
	void exitNot(MTCQParser.NotContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#and}.
	 * @param ctx the parse tree
	 */
	void enterAnd(MTCQParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#and}.
	 * @param ctx the parse tree
	 */
	void exitAnd(MTCQParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#or}.
	 * @param ctx the parse tree
	 */
	void enterOr(MTCQParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#or}.
	 * @param ctx the parse tree
	 */
	void exitOr(MTCQParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#impl}.
	 * @param ctx the parse tree
	 */
	void enterImpl(MTCQParser.ImplContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#impl}.
	 * @param ctx the parse tree
	 */
	void exitImpl(MTCQParser.ImplContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#equiv}.
	 * @param ctx the parse tree
	 */
	void enterEquiv(MTCQParser.EquivContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#equiv}.
	 * @param ctx the parse tree
	 */
	void exitEquiv(MTCQParser.EquivContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#xor}.
	 * @param ctx the parse tree
	 */
	void enterXor(MTCQParser.XorContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#xor}.
	 * @param ctx the parse tree
	 */
	void exitXor(MTCQParser.XorContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#full_interval}.
	 * @param ctx the parse tree
	 */
	void enterFull_interval(MTCQParser.Full_intervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#full_interval}.
	 * @param ctx the parse tree
	 */
	void exitFull_interval(MTCQParser.Full_intervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#upper_including_bound_interval}.
	 * @param ctx the parse tree
	 */
	void enterUpper_including_bound_interval(MTCQParser.Upper_including_bound_intervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#upper_including_bound_interval}.
	 * @param ctx the parse tree
	 */
	void exitUpper_including_bound_interval(MTCQParser.Upper_including_bound_intervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#upper_excluding_bound_interval}.
	 * @param ctx the parse tree
	 */
	void enterUpper_excluding_bound_interval(MTCQParser.Upper_excluding_bound_intervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#upper_excluding_bound_interval}.
	 * @param ctx the parse tree
	 */
	void exitUpper_excluding_bound_interval(MTCQParser.Upper_excluding_bound_intervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#interval}.
	 * @param ctx the parse tree
	 */
	void enterInterval(MTCQParser.IntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#interval}.
	 * @param ctx the parse tree
	 */
	void exitInterval(MTCQParser.IntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#weak_next}.
	 * @param ctx the parse tree
	 */
	void enterWeak_next(MTCQParser.Weak_nextContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#weak_next}.
	 * @param ctx the parse tree
	 */
	void exitWeak_next(MTCQParser.Weak_nextContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#next}.
	 * @param ctx the parse tree
	 */
	void enterNext(MTCQParser.NextContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#next}.
	 * @param ctx the parse tree
	 */
	void exitNext(MTCQParser.NextContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#until}.
	 * @param ctx the parse tree
	 */
	void enterUntil(MTCQParser.UntilContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#until}.
	 * @param ctx the parse tree
	 */
	void exitUntil(MTCQParser.UntilContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#eventually}.
	 * @param ctx the parse tree
	 */
	void enterEventually(MTCQParser.EventuallyContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#eventually}.
	 * @param ctx the parse tree
	 */
	void exitEventually(MTCQParser.EventuallyContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#always}.
	 * @param ctx the parse tree
	 */
	void enterAlways(MTCQParser.AlwaysContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#always}.
	 * @param ctx the parse tree
	 */
	void exitAlways(MTCQParser.AlwaysContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(MTCQParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(MTCQParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#subject}.
	 * @param ctx the parse tree
	 */
	void enterSubject(MTCQParser.SubjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#subject}.
	 * @param ctx the parse tree
	 */
	void exitSubject(MTCQParser.SubjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#role_atom}.
	 * @param ctx the parse tree
	 */
	void enterRole_atom(MTCQParser.Role_atomContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#role_atom}.
	 * @param ctx the parse tree
	 */
	void exitRole_atom(MTCQParser.Role_atomContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#concept_atom}.
	 * @param ctx the parse tree
	 */
	void enterConcept_atom(MTCQParser.Concept_atomContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#concept_atom}.
	 * @param ctx the parse tree
	 */
	void exitConcept_atom(MTCQParser.Concept_atomContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(MTCQParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(MTCQParser.AtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#conjunctive_query}.
	 * @param ctx the parse tree
	 */
	void enterConjunctive_query(MTCQParser.Conjunctive_queryContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#conjunctive_query}.
	 * @param ctx the parse tree
	 */
	void exitConjunctive_query(MTCQParser.Conjunctive_queryContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 */
	void enterMtcq_formula(MTCQParser.Mtcq_formulaContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 */
	void exitMtcq_formula(MTCQParser.Mtcq_formulaContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#prefix}.
	 * @param ctx the parse tree
	 */
	void enterPrefix(MTCQParser.PrefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#prefix}.
	 * @param ctx the parse tree
	 */
	void exitPrefix(MTCQParser.PrefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link MTCQParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(MTCQParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link MTCQParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(MTCQParser.StartContext ctx);
}