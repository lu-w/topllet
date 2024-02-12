grammar MTCQ;

@header {
package openllet.mtcq.parser;
}

X_TERMINAL          : 'X';
XB_TERMINAL         : 'X[!]';
UI_TERMINAL         : 'U_';
U_TERMINAL          : 'U';
FI_TERMINAL         : 'F_';
F_TERMINAL          : 'F';
GI_TERMINAL         : 'G_';
G_TERMINAL          : 'G';
LAST_TERMINAL       : 'last';
END_TERMINAL        : 'end';
NOT1_TERMINAL       : '!';
NOT2_TERMINAL       : '~';
AND1_TERMINAL       : '&';
AND2_TERMINAL       : '&&';
OR1_TERMINAL        : '|';
OR2_TERMINAL        : '||';
IMPL1_TERMINAL      : '->';
IMPL2_TERMINAL      : '=>';
EQUIV1_TERMINAL     : '<->';
EQUIV2_TERMINAL     : '<=>';
XOR_TERMINAL        : '^';
TRUE_TERMINAL       : 'TRUE';
FALSE_TERMINAL      : 'FALSE';
TT_TERMINAL         : 'TT';
FF_TERMINAL         : 'FF';
TIME_POINT          : [0-9]+;
NAME                : [0-9a-zA-Z_.-]+;
URI                 : [a-zA-Z0-9.:/#?&_-]+('/'|'#');
COMMENT             : '#' ~('\r' | '\n')* -> skip;
WS                  : [ \t\r\n]+ -> skip;

// Boolean terminals
trace_position : LAST_TERMINAL | END_TERMINAL;
prop_booleans  : TRUE_TERMINAL | FALSE_TERMINAL;
logic_booleans : TT_TERMINAL | FF_TERMINAL;
not            : NOT1_TERMINAL | NOT2_TERMINAL;
and            : AND1_TERMINAL | AND2_TERMINAL;
or             : OR1_TERMINAL | OR2_TERMINAL;
impl           : IMPL1_TERMINAL | IMPL2_TERMINAL;
equiv          : EQUIV1_TERMINAL | EQUIV2_TERMINAL;
xor            : XOR_TERMINAL;

// temporal operators
full_interval                  : '[' TIME_POINT ',' TIME_POINT ']';
upper_including_bound_interval : '<=' TIME_POINT;
upper_excluding_bound_interval : '<' TIME_POINT;
interval                       : full_interval 
                                  | upper_including_bound_interval
                                  | upper_excluding_bound_interval;
weak_next                      : X_TERMINAL;
next                           : XB_TERMINAL;
until                          : U_TERMINAL | UI_TERMINAL interval;
eventually                     : F_TERMINAL | FI_TERMINAL interval;
always                         : G_TERMINAL | GI_TERMINAL interval;

// conjunctive query
term              : (URI | NAME ':')? NAME;
subject           : '?' NAME | term;
role_atom         : term '(' subject ',' subject ')';
concept_atom      : term '(' subject ')';
atom              : role_atom | concept_atom;
conjunctive_query : atom (and atom)*;

// formula 
mtcq_formula : conjunctive_query
                | prop_booleans
                | logic_booleans
                | trace_position
                | '(' mtcq_formula ')'
                | mtcq_formula (and | or | impl | equiv | xor) mtcq_formula
                | (not | eventually | always | weak_next | next) mtcq_formula
                | mtcq_formula until mtcq_formula;

// SPARQL-like prefix
prefix : 'PREFIX' NAME ':' '<' URI '>';

start: (prefix)* mtcq_formula;
