grammar MTCQ;

INTERVAL_IDENTIFIER : '_';
X_TERMINAL          : 'X';
XB_TERMINAL         : 'X[!]';
U_TERMINAL          : 'U';
F_TERMINAL          : 'F';
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
LEFT_PAREN          : '(';
RIGHT_PAREN         : ')';
TRUE_TERMINAL       : 'TRUE';
FALSE_TERMINAL      : 'FALSE';
TT_TERMINAL         : 'TT';
FF_TERMINAL         : 'FF';
LEFT_PREF           : '<';
RIGHT_PREF          : '>';
NAME                : [0-9a-zA-Z_.-]+;
PREFIX              : 'PREFIX';
PREFIX_STRING       : NAME ':';
VAR                 : NAME;
NOT_QUANTIFIED_VAR  : ~'?'VAR;
QUANTIFIED_VAR      : '?'VAR;
TERM                : (URI | PREFIX_STRING)? NAME;
URI                 : [a-zA-Z0-9.:/#?&_-]+('/'|'#');
TIME_POINT          : [0-9]+;
INLINE_COMMENT      :  '# ' ~('\r' | '\n')* -> skip;
LINE_COMMENT        :  '#' ~('\r' | '\n')* -> skip;
WS                  : ~'#'[ \t\r\n]+ -> skip;

prop_booleans  : TRUE_TERMINAL | FALSE_TERMINAL;
logic_booleans : TT_TERMINAL | FF_TERMINAL;

not   : NOT1_TERMINAL | NOT2_TERMINAL;
and   : AND1_TERMINAL | AND2_TERMINAL;
or    : OR1_TERMINAL | OR2_TERMINAL;
impl  : IMPL1_TERMINAL | IMPL2_TERMINAL;
equiv : EQUIV1_TERMINAL | EQUIV2_TERMINAL;
xor   : XOR_TERMINAL;

interval : full_interval | upper_including_bound_interval | upper_excluding_bound_interval;
full_interval : '[' TIME_POINT ',' TIME_POINT ']';
upper_including_bound_interval : '<=' TIME_POINT;
upper_excluding_bound_interval : '<' TIME_POINT;

weak_next      : X_TERMINAL;
next           : XB_TERMINAL;
until          : U_TERMINAL (INTERVAL_IDENTIFIER interval)?;
eventually     : F_TERMINAL (INTERVAL_IDENTIFIER interval)?;
always         : G_TERMINAL (INTERVAL_IDENTIFIER interval)?;

subject           : QUANTIFIED_VAR | NOT_QUANTIFIED_VAR;
role_atom         : TERM '(' subject ',' subject ')';
concept_atom      : TERM '(' subject ')';
atom              : role_atom | concept_atom;
conjunctive_query : atom (and atom)*;

mltl_formula : conjunctive_query
                | prop_booleans
                | logic_booleans
                | LAST_TERMINAL
                | END_TERMINAL
                | LEFT_PAREN mltl_formula RIGHT_PAREN
                | not mltl_formula
                | mltl_formula and mltl_formula
                | mltl_formula or mltl_formula
                | mltl_formula impl mltl_formula
                | mltl_formula equiv mltl_formula
                | mltl_formula xor mltl_formula
                | mltl_formula until mltl_formula
                | eventually mltl_formula
                | always mltl_formula
                | weak_next mltl_formula
                | next mltl_formula;
                
prefix : PREFIX PREFIX_STRING LEFT_PREF URI RIGHT_PREF;

start: (prefix)* mltl_formula;
