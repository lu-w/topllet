package openllet.mtcq.engine.rewriting;

import openllet.mtcq.model.query.*;

// TODO
//  - check if all insideNext are set correctly
//  - check if all appliedTransformationRule are set correctly (this is only required if the transformation has an effect "above it" - so e.g. rule 8

public class DNFTransformer implements MTCQVisitor
{
    private MTCQFormula _newFormula;
    private boolean _appliedTransformationRule = false;
    private boolean _isInsideNext = false;

    public static MTCQFormula transform(MTCQFormula formula)
    {
        MTCQFormula mtcq;
        DNFTransformer transformer;
        do
        {
            transformer = new DNFTransformer();
            mtcq = transformer.run(formula);
        } while(transformer.appliedTransformationRules());
        return mtcq;
    }

    protected MTCQFormula run(MTCQFormula formula)
    {
        formula.accept(this);
        return this.getTransformedFormula();
    }

    private boolean appliedTransformationRules()
    {
        return _appliedTransformationRule;
    }

    public MTCQFormula getTransformedFormula()
    {
        return _newFormula;
    }

    @Override
    public void visit(NotFormula formula)
    {
        MTCQFormula sub = formula.getSubFormula();
        if (sub instanceof NotFormula)
        {
            // Rule 1
            _newFormula = sub;
            _appliedTransformationRule = true;
        }
        else if (sub instanceof OrFormula subOr)
        {
            // Rule 2
            _newFormula = new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subOr.getLeftSubFormula())),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subOr.getRightSubFormula()))
            );
            _appliedTransformationRule = true;
        }
        else if (sub instanceof AndFormula subAnd)
        {
            // Rule 3
            _newFormula = new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula())),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula()))
            );
            _appliedTransformationRule = true;
        }
        else if (sub instanceof StrongNextFormula subNext)
        {
            // Rule 4
            _newFormula = new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subNext.getSubFormula()))
            );
            _appliedTransformationRule = true;
        }
        else if (sub instanceof WeakNextFormula subNext)
        {
            // Rule 5
            _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subNext.getSubFormula()))
            );
            _appliedTransformationRule = true;
        }
        else if (sub instanceof UntilFormula subAnd)
        {
            // Rule 6
            _newFormula = new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula())),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula()))
            );
            _appliedTransformationRule = true;
        }
        else if (sub instanceof ReleaseFormula subAnd)
        {
            // Rule 7
            _newFormula = new UntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getLeftSubFormula())),
                    run(new NotFormula(formula.getTemporalKB(), formula.isDistinct(), subAnd.getRightSubFormula()))
            );
            _appliedTransformationRule = true;
        }
        else
            // No rule applicable, just descent into sub-formula
            _newFormula = new NotFormula(formula.getTemporalKB(), formula.isDistinct(), run(sub));
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
        _newFormula = new GloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula()));
    }

    @Override
    public void visit(EventuallyFormula formula)
    {
        // Done
        _newFormula = new EventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula()));
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
        _newFormula = new BoundedUntilFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()), run(formula.getRightSubFormula()),
                formula.getLowerBound(), formula.getUpperBound());
    }

    @Override
    public void visit(BoundedReleaseFormula formula)
    {
        // TODO (12,13)
        _newFormula = new BoundedReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getLeftSubFormula()), run(formula.getRightSubFormula()),
                formula.getLowerBound(), formula.getUpperBound());
    }

    @Override
    public void visit(BoundedGloballyFormula formula)
    {
        // TODO (12,13)
        _newFormula = new BoundedGloballyFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula()), formula.getLowerBound(), formula.getUpperBound());
    }

    @Override
    public void visit(BoundedEventuallyFormula formula)
    {
        // TODO (12,13)
        _newFormula = new BoundedEventuallyFormula(formula.getTemporalKB(), formula.isDistinct(),
                run(formula.getSubFormula()), formula.getLowerBound(), formula.getUpperBound());
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
        // Done
        _isInsideNext = true;
        _newFormula = new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(), run(formula.getSubFormula()));
        _appliedTransformationRule = true;
        _isInsideNext = false;
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
            run(
                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
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
                    )
            );
    }

    @Override
    public void visit(ReleaseFormula formula)
    {
        // Done
        if (!_isInsideNext)
            run(
                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            formula.getLeftSubFormula(),
                            new AndFormula(formula.getTemporalKB(), formula.isDistinct(),
                                    formula.getRightSubFormula(),
                                    new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                                            new ReleaseFormula(formula.getTemporalKB(), formula.isDistinct(),
                                                    formula.getLeftSubFormula(),
                                                    formula.getRightSubFormula()
                                            )
                                    )
                            )
                    )
            );
    }

    @Override
    public void visit(OrFormula formula)
    {
        MTCQFormula sub1 = formula.getLeftSubFormula();
        MTCQFormula sub2 = formula.getRightSubFormula();
        if (sub1 instanceof WeakNextFormula subW1 && sub2 instanceof WeakNextFormula subW2)
        {
            // Rule 8.1
            _isInsideNext = true;
            _newFormula = new WeakNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            run(subW1),
                            run(subW2))
            );
            _appliedTransformationRule = true;
            _isInsideNext = false;
        }
        else if (sub1 instanceof StrongNextFormula subX1 && sub2 instanceof StrongNextFormula subX2)
        {
            // Rule 8.2
            _isInsideNext = true;
            _newFormula = new StrongNextFormula(formula.getTemporalKB(), formula.isDistinct(),
                    new OrFormula(formula.getTemporalKB(), formula.isDistinct(),
                            run(subX1),
                            run(subX2))
            );
            _appliedTransformationRule = true;
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
        }
        else if (sub1 instanceof UntilFormula sub1U)
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
        }
        else if (sub2 instanceof UntilFormula sub2U)
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
        }
        else if (sub1 instanceof ReleaseFormula sub1R)
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
        }
        else if (sub2 instanceof ReleaseFormula sub2R)
        {
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
        }
        else if (sub1 instanceof OrFormula sub1O)
        {
            // sub1O.left = X.. and sub1O.right != X..
            // if sub2 = X...
            // else if:
        }
        else if (sub2 instanceof OrFormula sub2O)
        {
            // symmetric to above
        }
    }
}
