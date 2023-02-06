package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.*;
import org.junit.Test;

import java.util.*;

import static openllet.core.utils.TermFactory.*;
import static openllet.query.sparqldl.model.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.QueryAtomFactory.TypeAtom;
import static org.junit.Assert.*;

public class TestUnionQueries extends AbstractQueryTest
{
    private static <T> List<List<T>> powerset(List<T> vals)
    {
        // Source: https://stackoverflow.com/a/1670871/4145563
        List<List<T>> lists = new ArrayList<>();
        if (vals.isEmpty())
        {
            lists.add(new ArrayList<T>());
            return lists;
        }
        List<T> list = new ArrayList<T>(vals);
        T head = list.get(0);
        List<T> rest = new ArrayList<T>(list.subList(1, list.size()));
        for (List<T> set : powerset(rest))
        {
            List<T> newList = new ArrayList<T>();
            newList.add(head);
            newList.addAll(set);
            lists.add(newList);
            lists.add(set);
        }
        return lists;
    }

    @Test
    public void testUnionQueries1()
    {
        classes(_A, _B, _C, _D, _E);
        individuals(_a, _b, _c);
        objectProperties(_r, _p, _q);

        _kb.addSubClass(_A, or(_B, _C));
        _kb.addSubClass(_A, not(_D));
        _kb.addSubClass(_B, some(_p, TOP));
        _kb.addSubClass(_C, not(some(_r, TOP)));
        _kb.addType(_a, _A);
        _kb.addType(_b, _D);
        _kb.addPropertyValue(_r, _a, _b);

        // TODO not all vars can be dist. -> check then assess expected bindings below

        // UCQ 1: B(x) ^ r(x,b) v C(x) ^ r(x,b) -> is entailed (x -> a) but second disjunct can never be true
        UnionQuery ucq1 = unionQuery(select(x), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _r, _b)),
                query(TypeAtom(y, _C), PropertyValueAtom(y, _r, _b))));

        // UCQ 2: B(x) ^ p(x,b) v C(x) ^ r(x,b) -> not entailed
        UnionQuery ucq2 = unionQuery(select(x, y), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _p, _b)),
                query(TypeAtom(y, _C), PropertyValueAtom(y, _r, _b))));

        // UCQ 3: B(a) ^ r(a,x) v D(z) ^ r(y,z) -> entailed (both disjuncts, all mappings)
        UnionQuery ucq3 = unionQuery(select(y, z), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x)),
                query(TypeAtom(z, _D), PropertyValueAtom(y, _r, z))));

        // UCQ 4: B(a) ^ r(a,x) v E(z) ^ r(y,z) -> entailed (only first disjunct, empty mapping)
        UnionQuery ucq4 = unionQuery(select(x, y, z), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x)),
                query(TypeAtom(z, _E), PropertyValueAtom(y, _r, z))));

        // UCQ 5: B(a) ^ r(a,x) ^ p(y,z) ^ D(y) v C(a) v B(a) ^ p(a,x) ^ r(a,y) ^ D(b) -> entailed
        UnionQuery ucq5 = unionQuery(select(y1), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z), TypeAtom(y, _D)),
                query(TypeAtom(_a, _C)),
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x1), PropertyValueAtom(_a, _r, y1), TypeAtom(_b, _D)))
        );

        // UCQ 6: B(a) ^ r(a,x) ^ p(y,z) ^ D(y) v B(a) ^ p(a,x) ^ r(a,y) ^ D(b) -> entailed
        UnionQuery ucq6 = unionQuery(select(x1, y1), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z), TypeAtom(y, _D)),
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, x1), PropertyValueAtom(_a, _r, y1), TypeAtom(_b, _D)))
        );

        // UCQ 7: B(a) ^ r(a,x) ^ p(y,z) ^ D(y) v C(a)
        UnionQuery ucq7 = unionQuery(select(x, y), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _r, x), PropertyValueAtom(y, _p, z)),
                query(TypeAtom(x, _D)))
        );

        // UCQ 8: C(a) -> not entailed
        UnionQuery ucq8 = unionQuery(select(x), where(query(TypeAtom(x, _C))));

        // UCQ 9: B(a) v C(a) -> entailed
        UnionQuery ucq9 = unionQuery(select(x), where(query(TypeAtom(x, _A)), query(TypeAtom(x, _B))));

        // UCQ 10: p(x,y) -> entailed
        UnionQuery ucq10 = unionQuery(select(x), where(query(PropertyValueAtom(x, _p, y))));

        // UCQ 11: p(x,y) -> not entailed
        UnionQuery ucq11 = unionQuery(select(x, y), where(query(PropertyValueAtom(x, _p, y))));

        // UCQ 3: B(a) ^ r(a,x) v D(z) ^ r(y,z) -> entailed (only second disjunct, x -> Ind, y -> a, z -> d)
        UnionQuery ucq12 = unionQuery(select(y, z), where(
                query(TypeAtom(_a, _B), PropertyValueAtom(_a, _p, y)),
                query(TypeAtom(z, _D), PropertyValueAtom(y, _r, z))));

        testUnionQuery(ucq1, new ATermAppl[][] { { _a } });
        testUnionQuery(ucq2, new ATermAppl[][] { null }); // TODO false - how to check non-entailment?
        testUnionQuery(ucq3, powerset(List.of(_a, _b, _c)));
        testUnionQuery(ucq4, new ATermAppl[][] { { } });
        testUnionQuery(ucq5, new ATermAppl[][] { { _b } });
        testUnionQuery(ucq6, new ATermAppl[][] { null }); // TODO false - how to check non-entailment?
        testUnionQuery(ucq7, new ATermAppl[][] { { _b, _a }, { _b, _b } });
        testUnionQuery(ucq8, new ATermAppl[][] { null }); // TODO false - how to check non-entailment?
        testUnionQuery(ucq9, new ATermAppl[][] { { _a }, { _b } });
        testUnionQuery(ucq10, new ATermAppl[][] { { _a } });
        testUnionQuery(ucq11, new ATermAppl[][] { null }); // TODO false - how to check non-entailment?
        testUnionQuery(ucq12, new ATermAppl[][] { { _a, _b } });
    }

    // TODO Lukas: more test cases (Polyneikes could be very interesting)
}
