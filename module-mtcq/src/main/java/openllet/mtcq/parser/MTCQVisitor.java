// Generated from MTCQ.g4 by ANTLR 4.13.1

package openllet.mtcq.parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MTCQParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MTCQVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MTCQParser#trace_position}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrace_position(MTCQParser.Trace_positionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#prop_booleans}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProp_booleans(MTCQParser.Prop_booleansContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#logic_booleans}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_booleans(MTCQParser.Logic_booleansContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot(MTCQParser.NotContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(MTCQParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(MTCQParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#impl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImpl(MTCQParser.ImplContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#equiv}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquiv(MTCQParser.EquivContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#xor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXor(MTCQParser.XorContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#full_interval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFull_interval(MTCQParser.Full_intervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#upper_including_bound_interval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpper_including_bound_interval(MTCQParser.Upper_including_bound_intervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#upper_excluding_bound_interval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpper_excluding_bound_interval(MTCQParser.Upper_excluding_bound_intervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#interval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval(MTCQParser.IntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#weak_next}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeak_next(MTCQParser.Weak_nextContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#next}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNext(MTCQParser.NextContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#until}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUntil(MTCQParser.UntilContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#eventually}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventually(MTCQParser.EventuallyContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#globally}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobally(MTCQParser.GloballyContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(MTCQParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#subject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubject(MTCQParser.SubjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#role_atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_atom(MTCQParser.Role_atomContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#concept_atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConcept_atom(MTCQParser.Concept_atomContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(MTCQParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#conjunctive_query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConjunctive_query(MTCQParser.Conjunctive_queryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TracePositionFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTracePositionFormula(MTCQParser.TracePositionFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LogicBooleanFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicBooleanFormula(MTCQParser.LogicBooleanFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code XorFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXorFormula(MTCQParser.XorFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ImplFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplFormula(MTCQParser.ImplFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PropositionalBooleanFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropositionalBooleanFormula(MTCQParser.PropositionalBooleanFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BracketFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBracketFormula(MTCQParser.BracketFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code WeakNextFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeakNextFormula(MTCQParser.WeakNextFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EventuallyFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventuallyFormula(MTCQParser.EventuallyFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EquivFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquivFormula(MTCQParser.EquivFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ConjunctiveQueryFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConjunctiveQueryFormula(MTCQParser.ConjunctiveQueryFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NextFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNextFormula(MTCQParser.NextFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code GloballyFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGloballyFormula(MTCQParser.GloballyFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AndFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndFormula(MTCQParser.AndFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UntilFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUntilFormula(MTCQParser.UntilFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NotFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotFormula(MTCQParser.NotFormulaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OrFormula}
	 * labeled alternative in {@link MTCQParser#mtcq_formula}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrFormula(MTCQParser.OrFormulaContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#prefix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefix(MTCQParser.PrefixContext ctx);
	/**
	 * Visit a parse tree produced by {@link MTCQParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(MTCQParser.StartContext ctx);
}