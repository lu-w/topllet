package openllet.core.boxes.abox;

import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.exceptions.UnsupportedFeatureException;
import openllet.core.utils.ATermUtils;
import openllet.shared.tools.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A central interface to ABox changes that enables applying and rolling back such changes (in an orderly fashion).
 */
public class ABoxChanges
{
    private static final Logger _logger = Log.getLogger(ABoxChanges.class);

    /**
     * A change of the ABox.
     */
    public abstract static class ABoxChange
    {
        protected ABox _abox;

        /**
         * Sets the ABox to which the change applies - only for internal use, but has to be set before calling apply()
         * @param abox ABox on which the change is applied
         */
        protected void setABox(ABox abox)
        {
            _abox = abox;
        }

        /**
         * Reverts the change in its ABox
         */
        protected abstract void revert();
        protected abstract void apply();
    }

    /**
     * Represents adding a type to some existing individual in the ABox.
     */
    public static class TypeChange extends ABoxChange
    {
        private final ATermAppl _ind;
        private final ATermAppl _type;

        public TypeChange(ATermAppl ind, ATermAppl type)
        {
            _ind = ind;
            _type = type;
        }

        @Override
        public String toString() {
            return _type + "(" + _ind + ")";
        }

        @Override
        protected void revert()
        {
            // _abox.removeType(_ind, _type); // -> not needed: ABox is copied
        }

        @Override
        protected void apply()
        {
            _abox.addType(_ind, _type);
        }
    }

    public static class PropertyChange extends ABoxChange
    {
        private final ATermAppl _subj;
        private final ATermAppl _pred;
        private final ATermAppl _obj;

        public PropertyChange(ATermAppl subj, ATermAppl pred, ATermAppl obj)
        {
            _subj = subj;
            _pred = pred;
            _obj = obj;
        }

        @Override
        public String toString() {
            return _pred + "(" + _subj + ", " + _obj + ")";
        }

        @Override
        protected void revert()
        {
            // _abox.removePropertyValue(_pred, _subj, _obj); // -> not needed: ABox is copied
        }

        @Override
        protected void apply()
        {
            _abox.addEdge(_pred, _subj, _obj, DependencySet.INDEPENDENT);
            // TODO Lukas: do we need to add smth for the KB as well?
        }
    }

    // TODO Lukas: may want to think about using C subseteq TOP instead of creating fresh individuals. However, this
    //  requires rolling up the positive part of the CNCQ.
    public static class FreshIndChange extends ABoxChange
    {
        private Individual _ind = null;
        private int _freshIndCounter = 0;

        public FreshIndChange() { }

        @Override
        public String toString() {
            return "FreshInd(" + (_ind != null ? _ind.toString() : "not yet applied") + ")";
        }

        /**
         * @return the fresh individual if the change is applied and null otherwise.
         */
        public Individual getInd()
        {
            return _ind;
        }

        @Override
        protected void revert()
        {
            // _abox.removeNodeEntirely(_ind.getTerm()); // -> not needed: ABox is copied
            _abox.getKB().removeIndividual(_ind.getTerm());
        }

        @Override
        protected void apply()
        {
            //_ind = _abox.addFreshIndividual(null, DependencySet.EMPTY);
            //_abox.getKB().addIndividual(_ind.getTerm());
            ATermAppl newName;
            StringBuilder prefix = new StringBuilder();
            // Safely creates new individuals by prepending "_" until no collision is found
            do
                newName = ATermUtils.makeTermAppl(prefix.append("_") + "NEW_IND_" + _freshIndCounter);
            while (_abox.getKB().getIndividuals().contains(newName));
            _ind = _abox.getKB().addIndividual(newName);
            _abox.addIndividual(_ind.getTerm(), DependencySet.INDEPENDENT);
            _freshIndCounter++;
        }
    }

    private List<ABoxChange> _changes = new ArrayList<>();

    private final ABox _abox;

    /**
     * Collects all changes to be applied and potentially (orderly) reverted.
     * @param abox The ABox on which changes are to be applied and reverted
     */
    public ABoxChanges(ABox abox)
    {
        _abox = abox.copy();
    }

    public ABox getABox()
    {
        return _abox;
    }

    /**
     * Applies a given change. Changes the state of the ABox set in the constructor.
     * @param change the change to apply
     */
    public void apply(ABoxChange change)
    {
        change.setABox(_abox);
        _changes.add(change);
        change.apply();
    }

    /**
     * Reverts all the changes that were applied previously in an ordered fashion (thus, without causing conflicts).
     * Changes the state of the ABox set in the constructor.
     */
    public void revertAll()
    {
        // Sorting because we want to delete individuals at the very latest (otherwise, to-be-reverted changes may
        // refer to deleted individuals)
        _changes.sort((c1, c2) -> c1 instanceof FreshIndChange && !(c2 instanceof FreshIndChange) ? 1 :
                !(c1 instanceof FreshIndChange) && c2 instanceof FreshIndChange ? -1 : 0);
        for (ABoxChange change : _changes)
            change.revert();
        _changes = new ArrayList<>();
    }
}
