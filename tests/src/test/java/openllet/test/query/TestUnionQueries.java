package openllet.test.query;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.*;
import org.junit.Test;

import java.util.*;

import static openllet.core.utils.TermFactory.*;
import static openllet.query.sparqldl.model.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.QueryAtomFactory.TypeAtom;

public class TestUnionQueries extends AbstractQueryTest
{
    private static List<List<ATermAppl>> allResults(List<ATermAppl> individuals, int resultSize)
    {
        // https://stackoverflow.com/a/40101377/4145563
        List<List<ATermAppl>> res = new ArrayList<>();
        int[] indexes = new int[individuals.size()];
        ATermAppl[] permutation = new ATermAppl[resultSize];
        for (int j = (int) Math.pow(individuals.size(), resultSize); j > 0; j--)
        {
            for (int i = 0; i < resultSize; i++)
                permutation[i] = individuals.get(indexes[i]);
            res.add(Arrays.stream(permutation).toList());
            for (int i = 0; i < resultSize; i++)
            {
                if (indexes[i] >= individuals.size() - 1)
                    indexes[i] = 0;
                else
                {
                    indexes[i]++;
                    break;
                }
            }
        }
        return res;
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

        // UCQ 4: B(a) ^ r(a,x) v E(z) ^ r(y,z) -> entailed (only first disjunct, x -> a)
        UnionQuery ucq4 = unionQuery(select(x, y), where(
                query(TypeAtom(x, _B), PropertyValueAtom(x, _p, z)),
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
        testUnionQuery(ucq2, new ATermAppl[] {  });
        testUnionQuery(ucq3, allResults(List.of(_a, _b, _c), 2));
        testUnionQuery(ucq4, new ATermAppl[][] { { _a, _a }, { _a, _b }, { _a, _c } });
        testUnionQuery(ucq5, new ATermAppl[][] { { _b } });
        testUnionQuery(ucq6, new ATermAppl[] {  });
        testUnionQuery(ucq7, new ATermAppl[][] { { _b, _a }, { _b, _b }, { _b, _c } });
        testUnionQuery(ucq8, new ATermAppl[] {  });
        testUnionQuery(ucq9, new ATermAppl[][] { { _a } });
        testUnionQuery(ucq10, new ATermAppl[][] { { _a } });
        testUnionQuery(ucq11, new ATermAppl[] {  });
        testUnionQuery(ucq12, new ATermAppl[][] { { _a, _b } });
    }

    @Test
    public void testOedipus()
    {
        final ATermAppl Patricide = term("Patricide");
        final ATermAppl oedipus = term("oedipus");
        final ATermAppl iokaste = term("iokaste");
        final ATermAppl polyneikes = term("polyneikes");
        final ATermAppl thersandros = term("thersandros");
        final ATermAppl hasChild = term("hasChild");

        classes(Patricide);
        individuals(oedipus, iokaste, polyneikes, thersandros);
        objectProperties(hasChild);

        _kb.addPropertyValue(hasChild, iokaste, oedipus);
        _kb.addPropertyValue(hasChild, oedipus, polyneikes);
        _kb.addPropertyValue(hasChild, iokaste, polyneikes);
        _kb.addPropertyValue(hasChild, polyneikes, thersandros);
        _kb.addType(oedipus, Patricide);
        _kb.addType(thersandros, not(Patricide));

        UnionQuery ucq1 = unionQuery(select(x, y), where(query(TypeAtom(x, Patricide), PropertyValueAtom(iokaste,
                        hasChild, x), TypeAtom(y, not(Patricide)), PropertyValueAtom(x, hasChild, y))));

        testUnionQuery(ucq1, new ATermAppl[][] { { oedipus, polyneikes }, { polyneikes, thersandros } });
    }

    // TODO Lukas: more test cases
}
