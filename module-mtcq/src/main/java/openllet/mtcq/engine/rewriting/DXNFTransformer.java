package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

// TODO
//  - check if all insideNext are set correctly
//  - check if all appliedTransformationRule are set correctly (this is only required if the transformation has an effect "above it" - so e.g. rule 8
//  - check if all runs are correctly set (sometimes they can be pushed inwards or pushed outwards to reduce code complexity)
//  - check if copy() can be used instead of creating the formula on the fly.
//  - check if there is redundant code (that could be solved by using symmetry (see rule 14) or using existing rules and put run() outside)
//  - build in some simplifcations (true or ... = true, a or a = a, a and a = a, etc.)

public class DXNFTransformer implements MTCQVisitor
{
    private MTCQFormula _newFormula;
    private boolean _hasAppliedTransformationRule = false;
    private boolean _isInsideNext = false;

    public static MTCQFormula transform(MTCQFormula formula)
    {
        MTCQFormula mtcq = formula;
        DXNFTransformer transformer;
        do
        {
            transformer = new DXNFTransformer();
            mtcq = transformer.run(mtcq);
        } while(transformer.hasAppliedTransformationRules());
        return mtcq;
    }

    protected MTCQFormula run(MTCQFormula formula)
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
        // only require re-iterating on tree when certain rules were applied (those that add some X)
        if ("8.2".equals(rule) || "5".equals(rule) || "14".equals(rule) || rule.startsWith("15.") || rule.startsWith("12."))
            _hasAppliedTransformationRule = true;
        System.out.println("Applied #" + rule);
        System.out.println("Lead to: " + _newFormula);
    }

    protected MTCQFormula getTransformedFormula()
    {
        return _newFormula;
    }

    @Override
    public void visit(NotFormula formula)
    {
        MTCQFormula sub = formula.getSubFormula();
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
            _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subNext.getSubFormula()))
            );
            appliedTransformationRule("5");
        }
        else if (sub instanceof UntilFormula subU)
        {
            // Rule 6
            _newFormula = run(new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subU.getLeftSubFormula()),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subU.getRightSubFormula())
            ));
            appliedTransformationRule("6");
        }
        else if (sub instanceof EventuallyFormula subF)
        {
            // Rule 6.1
            _newFormula = run(new GloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subF.getSubFormula()))
            );
            appliedTransformationRule("6.1");
        }
        else if (sub instanceof GloballyFormula subG)
        {
            // Rule 6.2
            _newFormula = run(new EventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subG.getSubFormula()))
            );
            appliedTransformationRule("6.1");
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
        // Done
        _newFormula = new LogicalTrueFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(LogicalFalseFormula formula)
    {
        // Done
        _newFormula = new LogicalFalseFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(PropositionalTrueFormula formula)
    {
        // Done
        _newFormula = new PropositionalTrueFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(PropositionalFalseFormula formula)
    {
        // Done
        _newFormula = new PropositionalFalseFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(LastFormula formula)
    {
        // Done
        _newFormula = new LastFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(ImplFormula formula)
    {
        // Done
        _newFormula = run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                new NotFormula(formula.getTemporalKB(), formula.isDistinct(), formula.getLeftSubFormula()),
                formula.getRightSubFormula())
        );
    }

    @Override
    public void visit(GloballyFormula formula)
    {
        // Done
        if (!_isInsideNext)
        {
            _newFormula = run(new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    formula.getSubFormula(),
                    new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(), formula.copy())
            ));
            appliedTransformationRule("12.2");
        }
        else
            _newFormula = new GloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                    formula.getSubFormula().copy());
    }

    @Override
    public void visit(EventuallyFormula formula)
    {
        // Done
        if (!_isInsideNext)
        {
            _newFormula = run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    formula.getSubFormula(),
                    new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(), formula.copy())
            ));
            appliedTransformationRule("12.3");
        }
        else
            _newFormula = new EventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                    formula.getSubFormula().copy());
    }

    @Override
    public void visit(EquivFormula formula)
    {
        // Done
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
        // Done
        _newFormula = new EndFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(EmptyFormula formula)
    {
        // Done
        _newFormula = new EmptyFormula(formula.getTemporalKB(), formula.isDistinct());
    }

    @Override
    public void visit(ConjunctiveQueryFormula formula)
    {
        // Done
        _newFormula = new ConjunctiveQueryFormula(formula.getTemporalKB(), formula.isDistinct(),
                formula.getConjunctiveQuery().copy());
    }

    @Override
    public void visit(BoundedUntilFormula formula)
    {
        // TODO (12,13)

    }

    @Override
    public void visit(BoundedReleaseFormula formula)
    {
        // TODO (12,13)

    }

    @Override
    public void visit(BoundedGloballyFormula formula)
    {
        // TODO (12,13)

    }

    @Override
    public void visit(BoundedEventuallyFormula formula)
    {
        // TODO (12,13)

    }

    public void visit(AndFormula formula)
    {
        // Done
        _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()), run(formula.getRightSubFormula()));
    }

    @Override
    public void visit(XorFormula formula)
    {
        // Done
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
        appliedTransformationRule("14");
    }

    @Override
    public void visit(StrongNextFormula formula)
    {
        // Done
        _isInsideNext = true;
        _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula()));
        _isInsideNext = false;
    }

    @Override
    public void visit(UntilFormula formula)
    {
        // Done
        if (!_isInsideNext)
        {
            _newFormula =
                    run(new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            formula.getRightSubFormula(),
                            new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    formula.getLeftSubFormula(),
                                    new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                    formula.getLeftSubFormula(),
                                                    formula.getRightSubFormula()
                                            )
                                    )
                            )
                    ));
            appliedTransformationRule("12.1");
        }
        else
            _newFormula = new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                    formula.getLeftSubFormula().copy(), formula.getRightSubFormula().copy());
    }

    @Override
    public void visit(ReleaseFormula formula)
    {
        // Done
        if (!_isInsideNext)
        {
            _newFormula = run(
                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            formula.getLeftSubFormula(),
                            new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    formula.getRightSubFormula(),
                                    new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(), // TODO check if weak is correct
                                            new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                    formula.getLeftSubFormula(),
                                                    formula.getRightSubFormula()
                                            )
                                    )
                            )
                    )
            );
            appliedTransformationRule("13");
        }
        else
            _newFormula = new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                    formula.getLeftSubFormula().copy(), formula.getRightSubFormula().copy());
    }

    @Override
    public void visit(OrFormula formula)
    {
        MTCQFormula sub1 = formula.getLeftSubFormula();
        MTCQFormula sub2 = formula.getRightSubFormula();
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
        else if (sub1 instanceof UntilFormula sub1U && !_isInsideNext)
        {
            // Rule 10.1
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(
                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                        sub2,
                        new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                sub1U.getLeftSubFormula(),
                                sub1U.getRightSubFormula()
                        )
                    )
                ),
                run(
                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    sub2,
                                    sub1U.getRightSubFormula()
                            ),
                            new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            sub1U.getLeftSubFormula(),
                                            sub1U.getRightSubFormula()
                                    )
                            )
                    )
                )
            );
            appliedTransformationRule("10.1");
        }
        else if (sub2 instanceof UntilFormula sub2U && !_isInsideNext)
        {
            // Rule 10.2
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(
                            new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    sub1,
                                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            sub2U.getLeftSubFormula(),
                                            sub2U.getRightSubFormula()
                                    )
                            )
                    ),
                    run(
                            new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            sub1,
                                            sub2U.getRightSubFormula()
                                    ),
                                    new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                    sub2U.getLeftSubFormula(),
                                                    sub2U.getRightSubFormula()
                                            )
                                    )
                            )
                    )
            );
            appliedTransformationRule("10.2");
        }
        else if (sub1 instanceof ReleaseFormula sub1R && !_isInsideNext)
        {
            // Rule 11.1
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(
                            new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    sub2,
                                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            sub1R.getLeftSubFormula(),
                                            sub1R.getRightSubFormula()
                                    )
                            )
                    ),
                    new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                            run(
                                new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                        sub2,
                                        sub1R.getLeftSubFormula()
                                )
                            ),
                            new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            sub1R.getLeftSubFormula(),
                                            sub1R.getRightSubFormula()
                                    )
                            )
                    )
            );
            appliedTransformationRule("11.1");
        }
        else if (sub2 instanceof ReleaseFormula sub2R && !_isInsideNext)
        {
            // Rule 11.2
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(
                            new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    sub1,
                                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            sub2R.getLeftSubFormula(),
                                            sub2R.getRightSubFormula()
                                    )
                            )
                    ),
                    new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                            run(
                                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            sub1,
                                            sub2R.getLeftSubFormula()
                                    )
                            ),
                            new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            sub2R.getLeftSubFormula(),
                                            sub2R.getRightSubFormula()
                                    )
                            )
                    )
            );
            appliedTransformationRule("11.2");
        }
        else
        {
            if (sub1 instanceof OrFormula sub1O && sub1O.getLeftSubFormula() instanceof StrongNextFormula sub1O1)
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
                    appliedTransformationRule("15.1.1");
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
                    appliedTransformationRule("15.1.2");
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
                    appliedTransformationRule("15.2.1");
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
                    appliedTransformationRule("15.2.2");
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
                    appliedTransformationRule("15.3.1");
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
                    appliedTransformationRule("15.3.2");
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
                    appliedTransformationRule("15.4.1");
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
                    appliedTransformationRule("15.4.2");
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
}
