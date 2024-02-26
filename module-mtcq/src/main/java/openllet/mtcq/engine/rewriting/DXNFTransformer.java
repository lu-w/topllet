package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

public class DXNFTransformer implements MTCQVisitor
{
    private MTCQFormula _newFormula;
    private boolean _hasAppliedTransformationRule = false;
    private boolean _isInsideNext = false;

    public static MetricTemporalConjunctiveQuery transform(MetricTemporalConjunctiveQuery formula)
    {
        MetricTemporalConjunctiveQuery mtcq = formula;
        DXNFTransformer transformer;
        do
        {
            transformer = new DXNFTransformer();
            mtcq = transformer.run(mtcq);
        } while(transformer.hasAppliedTransformationRules());
        return new MTCQSimplifier().transform(mtcq);
    }

    protected MTCQFormula run(MetricTemporalConjunctiveQuery formula)
    {
        formula.accept(this);
        return this.getTransformedFormula();
    }

    private boolean hasAppliedTransformationRules()
    {
        return _hasAppliedTransformationRule;
    }

    private void appliedTransformationRule(String rule)
    {
        // only require re-iterating on tree when certain rules were applied
        if ("8.2".equals(rule) || "5".equals(rule) || rule.startsWith("12") || rule.startsWith("13.") ||
                rule.startsWith("14."))
            _hasAppliedTransformationRule = true;
        // System.out.println("Applied #" + rule);
        // System.out.println("Lead to: " + _newFormula);
    }

    protected MTCQFormula getTransformedFormula()
    {
        return _newFormula;
    }

    @Override
    public void visit(NotFormula formula)
    {
        MetricTemporalConjunctiveQuery sub = formula.getSubFormula();
        if (sub instanceof NotFormula subNot)
        {
            // Rule 1
            _newFormula = run(subNot.getSubFormula());
            appliedTransformationRule("1");
        }
        else if (sub instanceof OrFormula subOr)
        {
            // Rule 2
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subOr.getLeftSubFormula())),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subOr.getRightSubFormula()))
            );
            appliedTransformationRule("2");
        }
        else if (sub instanceof AndFormula subAnd)
        {
            // Rule 3
            _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula())),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula()))
            );
            appliedTransformationRule("3");
        }
        else if (sub instanceof StrongNextFormula subNext)
        {
            // Rule 4
            _newFormula = run(
                    new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                            new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subNext.getSubFormula())
                    )
            );
            appliedTransformationRule("4");
        }
        else if (sub instanceof WeakNextFormula subNext)
        {
            // Rule 5
            _isInsideNext = true;
            _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subNext.getSubFormula()))
            );
            _isInsideNext = false;
            appliedTransformationRule("5");
        }
        else if (sub instanceof UntilFormula subU)
        {
            // Rule 6
            _newFormula = run(new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subU.getLeftSubFormula()),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subU.getRightSubFormula())
            ));
            appliedTransformationRule("6.1");
        }
        else if (sub instanceof EventuallyFormula subF)
        {
            // Rule 6.1
            _newFormula = run(new GloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subF.getSubFormula()))
            );
            appliedTransformationRule("6.2");
        }
        else if (sub instanceof GloballyFormula subG)
        {
            // Rule 6.2
            _newFormula = run(new EventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subG.getSubFormula()))
            );
            appliedTransformationRule("6.3");
        }
        else if (sub instanceof ReleaseFormula subR)
        {
            // Rule 7
            _newFormula = run(new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subR.getLeftSubFormula()),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subR.getRightSubFormula())
            ));
            appliedTransformationRule("7");
        }
        else
        {
            // No rule applicable (base case, e.g., CQ or tt/ff), just descent into sub-formula
            _newFormula = new NotFormula(formula.getTemporalKB(), formula.isDistinct(), run(sub));
        }
    }

    @Override
    public void visit(LogicalTrueFormula formula)
    {
        _newFormula = new LogicalTrueFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(LogicalFalseFormula formula)
    {
        _newFormula = new LogicalFalseFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(PropositionalTrueFormula formula)
    {
        _newFormula = new PropositionalTrueFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(PropositionalFalseFormula formula)
    {
        _newFormula = new PropositionalFalseFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(LastFormula formula)
    {
        _newFormula = new LastFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(ImplFormula formula)
    {
        _newFormula = run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                new NotFormula(formula.getTemporalKB(), formula.isDistinct(), formula.getLeftSubFormula()),
                formula.getRightSubFormula())
        );
    }

    @Override
    public void visit(GloballyFormula formula)
    {
        if (!_isInsideNext)
        {
            _newFormula = run(new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    formula.getSubFormula(),
                    new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(), formula.copy())
            ));
            appliedTransformationRule("12.2");
        }
        else
            _newFormula = formula.copy();
    }

    @Override
    public void visit(EventuallyFormula formula)
    {
        if (!_isInsideNext)
        {
            _newFormula = run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    formula.getSubFormula(),
                    new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(), formula.copy())
            ));
            appliedTransformationRule("12.3");
        }
        else
            _newFormula = formula.copy();
    }

    @Override
    public void visit(EquivFormula formula)
    {
        _newFormula = run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                        formula.getLeftSubFormula(),
                        formula.getRightSubFormula()
                ),
                new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                        new NotFormula(formula.getTemporalKB(), formula.isDistinct(), formula.getLeftSubFormula()),
                        new NotFormula(formula.getTemporalKB(), formula.isDistinct(), formula.getRightSubFormula())
                )
        ));
    }

    @Override
    public void visit(EndFormula formula)
    {
        _newFormula = new EndFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(EmptyFormula formula)
    {
        _newFormula = new EmptyFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(ConjunctiveQueryFormula formula)
    {
        _newFormula = new ConjunctiveQueryFormula(formula.getTemporalKB(), formula.isDistinct(),
                formula.getConjunctiveQuery().copy());
    }

    @Override
    public void visit(BoundedUntilFormula formula)
    {
        if (!_isInsideNext)
        {
            if (formula.getLowerBound() == 0 && formula.getUpperBound() == 0)
            {
                _newFormula = run(formula.getRightSubFormula());
            }
            if (formula.getLowerBound() == 0 && formula.getUpperBound() > 0)
            {
                _newFormula =
                        run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                formula.getRightSubFormula(),
                                new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        formula.getLeftSubFormula(),
                                        new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                new BoundedUntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                        formula.getLeftSubFormula(),
                                                        formula.getRightSubFormula(),
                                                        0,
                                                        formula.getUpperBound() - 1
                                                )
                                        )
                                )
                        ));
            }
            else
            {
                _newFormula =
                        run(new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new BoundedUntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        formula.getLeftSubFormula(),
                                        formula.getRightSubFormula(),
                                        formula.getLowerBound() - 1,
                                        formula.getUpperBound() - 1
                                )
                ));
            }
            appliedTransformationRule("12.4");
        }
        else
            _newFormula = formula.copy();
    }

    @Override
    public void visit(BoundedReleaseFormula formula)
    {
        if (!_isInsideNext)
        {
            if (formula.getLowerBound() == 0 && formula.getUpperBound() == 0)
            {
                _newFormula = run(formula.getRightSubFormula());
            }
            if (formula.getLowerBound() == 0 && formula.getUpperBound() > 0)
            {
                _newFormula =
                        run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                formula.getLeftSubFormula(),
                                new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        formula.getRightSubFormula(),
                                        new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                new BoundedReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                        formula.getLeftSubFormula(),
                                                        formula.getRightSubFormula(),
                                                        0,
                                                        formula.getUpperBound() - 1
                                                )
                                        )
                                )
                        ));
            }
            else
            {
                _newFormula =
                        run(new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new BoundedReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        formula.getLeftSubFormula(),
                                        formula.getRightSubFormula(),
                                        formula.getLowerBound() - 1,
                                        formula.getUpperBound() - 1
                                )
                        ));
            }
            appliedTransformationRule("12.5");
        }
        else
            _newFormula = formula.copy();
    }

    @Override
    public void visit(BoundedGloballyFormula formula)
    {
        if (!_isInsideNext)
        {

            if (formula.getLowerBound() == 0 && formula.getUpperBound() > 0)
                _newFormula = run(new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                        formula.getSubFormula(),
                        new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new BoundedGloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        formula.getSubFormula(),
                                        0,
                                        formula.getUpperBound() - 1
                                )
                        )
                ));
            else if (formula.getLowerBound() > 0 && formula.getUpperBound() > 0)
                _newFormula = run(
                        new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new BoundedGloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        formula.getSubFormula(),
                                        formula.getLowerBound() - 1,
                                        formula.getUpperBound() - 1
                                )
                        )
                );
            else // upper and lower bound == 0
                _newFormula = run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                        new EndFormula(formula.getTemporalKB(), formula.isDistinct()),
                        formula.getSubFormula())
                );
            appliedTransformationRule("12.6");
        }
        else
            _newFormula = formula.copy();
    }

    @Override
    public void visit(BoundedEventuallyFormula formula)
    {
        if (!_isInsideNext)
        {
            if (formula.getLowerBound() == 0 && formula.getUpperBound() > 0)
                _newFormula = run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                        formula.getSubFormula(),
                        new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new BoundedEventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        formula.getSubFormula(),
                                        0,
                                        formula.getUpperBound() - 1
                                )
                        )
                ));
            else if (formula.getLowerBound() > 0 && formula.getUpperBound() > 0)
                _newFormula = run(
                        new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new BoundedEventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        formula.getSubFormula(),
                                        formula.getLowerBound() - 1,
                                        formula.getUpperBound() - 1
                                )
                        )
                );
            else // upper and lower bound == 0
                _newFormula = run(formula.getSubFormula());
            appliedTransformationRule("12.7");
        }
        else
            _newFormula = formula.copy();
    }

    public void visit(AndFormula formula)
    {
        _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()), run(formula.getRightSubFormula()));
    }

    @Override
    public void visit(XorFormula formula)
    {
        _newFormula = run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                        new NotFormula(formula.getTemporalKB(), formula.isDistinct(), formula.getLeftSubFormula()),
                        formula.getRightSubFormula()
                ),
                new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                        formula.getLeftSubFormula(),
                        new NotFormula(formula.getTemporalKB(), formula.isDistinct(), formula.getRightSubFormula())
                )
        ));
    }

    @Override
    public void visit(WeakNextFormula formula)
    {
        // Rule 14
        _newFormula = run(
                new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new LastFormula(formula.getTemporalKB(), formula.isDistinct()),
                    new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(), formula.getSubFormula())
                )
        );
        appliedTransformationRule("12");
    }

    @Override
    public void visit(StrongNextFormula formula)
    {
        _isInsideNext = true;
        _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula()));
        _isInsideNext = false;
    }

    @Override
    public void visit(UntilFormula formula)
    {
        if (!_isInsideNext)
        {
            _newFormula =
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            formula.getRightSubFormula(),
                            new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    formula.getLeftSubFormula(),
                                    new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(), formula)
                            )
                    ));
            appliedTransformationRule("12.1");
        }
        else
            _newFormula = formula.copy();
    }

    @Override
    public void visit(ReleaseFormula formula)
    {
        if (!_isInsideNext)
        {
            _newFormula = run(
                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            formula.getLeftSubFormula(),
                            new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    formula.getRightSubFormula(),  // TODO check if weak next is correct
                                    new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(), formula)
                            )
                    )
            );
            appliedTransformationRule("13");
        }
        else
            _newFormula = formula.copy();
    }

    @Override
    public void visit(OrFormula formula)
    {
        MetricTemporalConjunctiveQuery sub1 = formula.getLeftSubFormula();
        MetricTemporalConjunctiveQuery sub2 = formula.getRightSubFormula();
        if (sub1 instanceof WeakNextFormula subW1 && sub2 instanceof WeakNextFormula subW2)
        {
            // Rule 8.1
            _newFormula = run(
                    new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                            new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    subW1.getSubFormula(),
                                    subW2.getSubFormula()
                            )
                    )
            );
            appliedTransformationRule("8.1");
        }
        else if (sub1 instanceof StrongNextFormula subX1 && sub2 instanceof StrongNextFormula subX2)
        {
            // Rule 8.2
            _isInsideNext = true;
            _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            run(subX1.getSubFormula()),
                            run(subX2.getSubFormula())
                    )
            );
            appliedTransformationRule("8.2");
            _isInsideNext = false;
        }
        else if (sub1 instanceof AndFormula sub1A)
        {
            // Rule 9.1
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            sub2,
                            sub1A.getLeftSubFormula()
                    )),
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            sub2,
                            sub1A.getRightSubFormula()
                    )));
            appliedTransformationRule("9.1");
        }
        else if (sub2 instanceof AndFormula sub2A)
        {
            // Rule 9.2
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            sub1,
                            sub2A.getLeftSubFormula()
                    )),
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            sub1,
                            sub2A.getRightSubFormula()
                    )));
            appliedTransformationRule("9.2");
        }
        else if (sub1 instanceof OrFormula sub1O && sub1O.getLeftSubFormula() instanceof StrongNextFormula sub1O1)
        {
            if (sub2 instanceof StrongNextFormula sub2X)
            {
                _newFormula = run(
                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                sub1O1.getSubFormula(),
                                                sub2X.getSubFormula())
                                ),
                                sub1O.getRightSubFormula()
                        )
                );
                appliedTransformationRule("13.1");
            }
            else
            {
                _newFormula = run(
                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        sub1O1.getSubFormula()),
                                new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        sub2,
                                        sub1O.getRightSubFormula())
                        )
                );
                appliedTransformationRule("14.1");
            }
        }
        else if (sub1 instanceof OrFormula sub1O && sub1O.getRightSubFormula() instanceof StrongNextFormula sub1O2)
        {
            if (sub2 instanceof StrongNextFormula sub2X)
            {
                _newFormula = run(
                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                sub1O2.getSubFormula(),
                                                sub2X.getSubFormula())
                                ),
                                sub1O.getLeftSubFormula()
                        )
                );
                appliedTransformationRule("13.2");
            }
            else
            {
                _newFormula = run(
                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        sub1O2.getSubFormula()),
                                new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        sub2,
                                        sub1O.getLeftSubFormula())
                        )
                );
                appliedTransformationRule("14.2");
            }
        }
        else if (sub2 instanceof OrFormula sub2O && sub2O.getLeftSubFormula() instanceof StrongNextFormula sub2O1)
        {
            if (sub1 instanceof StrongNextFormula sub1X)
            {
                _newFormula = run(
                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                sub2O1.getSubFormula(),
                                                sub1X.getSubFormula())
                                ),
                                sub2O.getRightSubFormula()
                        )
                );
                appliedTransformationRule("13.3");
            }
            else
            {
                _newFormula = run(
                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        sub2O1.getSubFormula()),
                                new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        sub1,
                                        sub2O.getRightSubFormula())
                        )
                );
                appliedTransformationRule("14.3");
            }
        }
        else if (sub2 instanceof OrFormula sub2O && sub2O.getRightSubFormula() instanceof StrongNextFormula sub2O2)
        {
            if (sub1 instanceof StrongNextFormula sub1X)
            {
                _newFormula = run(
                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                sub2O2.getSubFormula(),
                                                sub1X.getSubFormula())
                                ),
                                sub2O.getLeftSubFormula()
                        )
                );
                appliedTransformationRule("13.4");
            }
            else
            {
                _newFormula = run(
                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        sub2O2.getSubFormula()),
                                new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        sub1,
                                        sub2O.getLeftSubFormula())
                        )
                );
                appliedTransformationRule("14.4");
            }
        }
        else
        {
            _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(formula.getLeftSubFormula()),
                    run(formula.getRightSubFormula())
            );
        }
    }
}
