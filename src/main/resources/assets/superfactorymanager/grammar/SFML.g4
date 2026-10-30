grammar SFML;
@header {
package ca.teamdman.langs;
}
@lexer::members {
    public boolean INCLUDE_UNUSED = false; // we want syntax highlighting to not break on unexpected tokens

    private LexerATNSimulator strictSimulator;
    private LexerATNSimulator tolerantSimulator;

    // Lazily initialized after the generated _ATN, safely published by class initialization. Never mix policies.
    private static final class PolicyDfas {
        static final DFA[] STRICT = ca.teamdman.sfml.program_builder.CachedSfmlLexerSimulator.createDecisionDfas(_ATN);
        static final DFA[] TOLERANT = ca.teamdman.sfml.program_builder.CachedSfmlLexerSimulator.createDecisionDfas(_ATN);
        static final org.antlr.v4.runtime.atn.PredicateTransition UNUSED_GUARD =
            ca.teamdman.sfml.program_builder.CachedSfmlLexerSimulator.findUnusedGuard(_ATN, UNUSED);
    }

    /** Override only for simulator differential tests; original simulators must use independent DFA arrays. */
    protected LexerATNSimulator createLexerSimulator(boolean includeUnused) {
        return new ca.teamdman.sfml.program_builder.CachedSfmlLexerSimulator(this, _ATN,
            includeUnused ? PolicyDfas.TOLERANT : PolicyDfas.STRICT, _sharedContextCache,
            includeUnused, PolicyDfas.UNUSED_GUARD);
    }

    @Override
    public Token nextToken() {
        // The public flag may change between tokens. Snapshot it for this match and preserve position on swaps.
        LexerATNSimulator selected;
        if (INCLUDE_UNUSED) {
            if (tolerantSimulator == null) tolerantSimulator = createLexerSimulator(true);
            selected = tolerantSimulator;
        } else {
            if (strictSimulator == null) strictSimulator = createLexerSimulator(false);
            selected = strictSimulator;
        }
        if (_interp != selected) {
            selected.copyState(_interp);
            _interp = selected;
        }
        return super.nextToken();
    }
}

program : name? (stateDeclaration | dataDeclaration)* functionDefinition* trigger* EOF;

functionDefinition : FUNCTION functionName LPAREN (functionParameter (COMMA functionParameter)*)? RPAREN RETURNS dataType DO block END;
functionName : dataName (DOT dataName)*;
functionParameter : VARIABLE COLON dataType;
dataType : (dataName | LBRACE (typeField (COMMA typeField)*)? RBRACE) (LBRACKET NUMBER RBRACKET)*;
typeField : dataName COLON dataType;
functionCall : functionName LPAREN (dataExpression (COMMA dataExpression)*)? RPAREN;
returnStatement : RETURN dataExpression;

name: NAME string ;

//
// TRIGGERS
//

trigger : EVERY interval DO block END           #TimerTrigger
        | EVERY REDSTONE PULSE DO block END     #PulseTrigger
        | EVERY REDSTONE (PULSE | RISING | FALLING) FROM labelAccess DO block END #NamedRedstoneTrigger
        ;

interval: NUMBER? GLOBAL? (PLUS NUMBER)? (TICKS | TICK | SECONDS | SECOND)      # IntervalSpace
        | NUMBER_WITH_G_SUFFIX (PLUS NUMBER)? (TICKS | TICK | SECONDS | SECOND) # IntervalNoSpace;

//
// BLOCK STATEMENT
//

block           : statement* ;
statement       : dataDeclaration
                | dataAssignment
                | printStatement
                | transferStatement
                | returnStatement
                | inputStatement
                | redstoneOutputStatement
                | outputStatement
                | ifStatement
                | forStatement
                | whileStatement
                | forgetStatement
                ;

redstoneOutputStatement
                : OUTPUT quantity? REDSTONE (LEVEL | LEVELS) quantity? TO EACH? labelAccess
                | TO EACH? labelAccess OUTPUT quantity? REDSTONE (LEVEL | LEVELS) quantity?
                ;

// IO STATEMENT
forgetStatement : FORGET label? (COMMA label)* COMMA?;
inputStatement  : INPUT inputResourceLimits? resourceExclusion? FROM EACH? labelAccess
                | FROM EACH? labelAccess INPUT inputResourceLimits? resourceExclusion?
                ;
outputStatement : OUTPUT outputResourceLimits? resourceExclusion? TO emptyslots? EACH? labelAccess
                | TO emptyslots? EACH? labelAccess OUTPUT outputResourceLimits? resourceExclusion?
                ;

inputResourceLimits   : resourceLimitList; // separate for different defaults
outputResourceLimits  : resourceLimitList; // separate for different defaults

resourceLimitList  : resourceLimit (COMMA resourceLimit)* COMMA?;
resourceLimit   : limit? resourceIdDisjunction with?
                | limit with?
                | with
                ;
limit           : quantity retention    #QuantityRetentionLimit
                | retention             #RetentionLimit
                | quantity              #QuantityLimit
                ;

quantity        : (number | LPAREN dataExpression RPAREN) EACH?;
retention       : RETAIN (number | LPAREN dataExpression RPAREN) EACH?;

resourceExclusion       : EXCEPT resourceIdList;

resourceId      : (identifier) (COLON (identifier)? (COLON (identifier)? (COLON (identifier)?)?)?)? # Resource
                | string                                                                            # StringResource
                ;

resourceIdList          : resourceId (COMMA resourceId)* COMMA?;
resourceIdDisjunction   : resourceId (OR resourceId)* OR?;


with        : WITH withClause
            | WITHOUT withClause
            ;
withClause  : LPAREN withClause RPAREN           # WithParen
            | NOT withClause                     # WithNegation
            | withClause AND withClause          # WithConjunction
            | withClause OR withClause           # WithDisjunction
            | (TAG HASHTAG?|HASHTAG) tagMatcher  # WithTag
            ;

tagMatcher  : identifier COLON identifier (SLASH identifier)*
            | identifier (SLASH identifier)*
            ;


sidequalifier   : EACH SIDE                  #EachSide
                | side (COMMA side)* SIDE    #ListedSides
                ;

side            : TOP
                | BOTTOM
                | NORTH
                | EAST
                | SOUTH
                | WEST
                | LEFT
                | RIGHT
                | FRONT
                | BACK
                | NULL
                ;

slotqualifier   : (SLOTS | SLOT) rangeset;
rangeset        : range (COMMA range)*;
range           : number (DASH number)? ;


ifStatement     : IF boolexpr THEN block (ELSE IF boolexpr THEN block)* (ELSE block)? END;
forStatement    : FOR VARIABLE EQ_SYMBOL dataExpression TO dataExpression (STEP dataExpression)? DO block END #CountedFor
                | FOR VARIABLE IN dataExpression DO block END #ArrayFor;
whileStatement  : WHILE boolexpr DO block END;
boolexpr        : TRUE                              #BooleanTrue
                | FALSE                             #BooleanFalse
                | LPAREN boolexpr RPAREN            #BooleanParen
                | LPAREN dataExpression RPAREN      #BooleanData
                | NOT boolexpr                      #BooleanNegation
                | boolexpr AND boolexpr             #BooleanConjunction
                | boolexpr OR boolexpr              #BooleanDisjunction
                | setOp? labelAccess HAS comparisonOp (number | LPAREN dataExpression RPAREN) resourceIdDisjunction? with? (EXCEPT resourceIdList)?  #BooleanHas
                | REDSTONE (LEVEL | LEVELS)? (comparisonOp (number | LPAREN dataExpression RPAREN))?   #BooleanRedstone
                ;

comparisonOp    : GT
                | NE_SYMBOL
                | EQ_ALIAS
                | LT
                | EQ
                | LE
                | GE
                | GT_SYMBOL
                | LT_SYMBOL
                | EQ_SYMBOL
                | LE_SYMBOL
                | GE_SYMBOL
                ;
setOp           : OVERALL
                | SOME
                | EVERY
                | EACH
                | ONE
                | LONE
                ;





//
// IO HELPERS
//
labelAccess     : label (COMMA label)* roundrobin? sidequalifier? slotqualifier?;
roundrobin      : ROUND ROBIN BY (LABEL | BLOCK);

label           : (identifier)  #RawLabel
                | string        #StringLabel
                ;

emptyslots      : EMPTY (SLOTS | SLOT) IN ;

identifier : (IDENTIFIER | TRANSFER | INTO | LET | SET | STATE | PRINT | FUNCTION | RETURNS | RETURN | FOR | WHILE | STEP | RISING | FALLING | REDSTONE | LEVEL | LEVELS | GLOBAL | SECOND | SECONDS | TOP | BOTTOM | LEFT | RIGHT | FRONT | BACK) ;

// Typed data expressions. Multiplication retains the legacy wildcard token.
stateDeclaration : STATE VARIABLE EQ_SYMBOL dataExpression;
dataDeclaration : LET VARIABLE EQ_SYMBOL dataExpression;
dataAssignment : SET? dataTarget EQ_SYMBOL dataExpression;
printStatement : PRINT LPAREN dataExpression (COMMA dataExpression)* RPAREN;
// Actions are statements: no transfer can hide inside a predicate or ordinary function call.
transferStatement : TRANSFER LPAREN dataExpression COMMA dataExpression
                    (COMMA dataExpression (COMMA dataExpression)?)? RPAREN (INTO VARIABLE)?;
dataTarget : VARIABLE (DOT dataName | LBRACKET dataExpression RBRACKET)*;
dataExpression : dataOr;
dataOr : dataAnd (OR dataAnd)*;
dataAnd : dataNot (AND dataNot)*;
dataNot : NOT dataNot | dataComparison;
dataComparison : dataBitOr (comparisonOp dataBitOr)?;
// Bitwise precedence mirrors numeric masks: arithmetic, shifts, &, ^, |, comparisons.
dataBitOr : dataBitXor (BIT_OR dataBitXor)*;
dataBitXor : dataBitAnd (BIT_XOR dataBitAnd)*;
dataBitAnd : dataShift (BIT_AND dataShift)*;
dataShift : dataAdd ((SHIFT_LEFT | SHIFT_RIGHT | SHIFT_UNSIGNED_RIGHT) dataAdd)*;
dataAdd : dataMultiply ((PLUS | DASH) dataMultiply)*;
dataMultiply : dataSign ((multiplyOperator | SLASH | PERCENT) dataSign)*;
multiplyOperator : {_input.LT(1).getText().equals("*")}? IDENTIFIER;
dataSign : (PLUS | DASH | BIT_NOT) dataSign | dataPostfix;
dataPostfix : dataPrimary (DOT dataName (LPAREN (dataExpression (COMMA dataExpression)*)? RPAREN)?
                         | LBRACKET dataExpression RBRACKET)*;
dataPrimary : NUMBER | STRING | VARIABLE | TRUE | FALSE | LPAREN dataExpression RPAREN
            | functionCall
            | LBRACKET (dataExpression (COMMA dataExpression)* COMMA?)? RBRACKET
            | LBRACE (dataField (COMMA dataField)* COMMA?)? RBRACE;
dataField : dataName COLON dataExpression;
// All legacy keyword spellings are legal fixed field names.
dataName : IDENTIFIER | TRANSFER | INTO | LET | SET | STATE | PRINT | FUNCTION | RETURNS | RETURN | IF | THEN | ELSE | HAS | OVERALL | SOME | ONE | LONE
         | TRUE | FALSE | NOT | AND | OR | GT | LT | EQ | LE | GE | FROM | TO | INPUT | OUTPUT
         | WHERE | SLOTS | SLOT | RETAIN | EACH | EXCEPT | FORGET | EMPTY | IN | WITHOUT | WITH
         | TAG | ROUND | ROBIN | BY | LABEL | BLOCK | TOP | BOTTOM | NORTH | EAST | SOUTH | WEST
         | SIDE | LEFT | RIGHT | FRONT | BACK | NULL | TICKS | TICK | SECONDS | SECOND | GLOBAL
         | REDSTONE | LEVEL | LEVELS | PULSE | DO | END | NAME | EVERY | PLUS | FOR | WHILE | STEP | RISING | FALLING;

// GENERAL
string: STRING ;
number: NUMBER ;



//
// LEXER
//

// DATA STATEMENTS
TRANSFER : T R A N S F E R;
INTO : I N T O;
FUNCTION : F U N C T I O N;
RETURNS : R E T U R N S;
RETURN : R E T U R N;
LET : L E T;
SET : S E T;
STATE : S T A T E;
RISING : R I S I N G;
FALLING : F A L L I N G;
PRINT : P R I N T;
FOR : F O R;
WHILE : W H I L E;
STEP : S T E P;
VARIABLE : '$' [a-zA-Z_] [a-zA-Z0-9_]*;
LBRACKET : '[';
RBRACKET : ']';
LBRACE : '{';
RBRACE : '}';
DOT : '.';
PERCENT : '%';
BIT_AND : '&';
BIT_OR : '|';
BIT_XOR : '^';
BIT_NOT : '~';
SHIFT_LEFT : '<<';
SHIFT_RIGHT : '>>';
SHIFT_UNSIGNED_RIGHT : '>>>';
// IF STATEMENT
IF      : I F ;
THEN    : T H E N ;
ELSE    : E L S E ;

HAS     : H A S ;
OVERALL : O V E R A L L ;
SOME    : S O M E ;
ONE     : O N E ;
LONE    : L O N E ;

// BOOLEAN LOGIC
TRUE    : T R U E ;
FALSE   : F A L S E ;
NOT     : N O T | '!' ;
AND     : A N D | '&&' ;
OR      : O R | '||' ;

// QUANTITY LOGIC
GT        : G T ;
GT_SYMBOL : '>' ;
LT        : L T ;
LT_SYMBOL : '<' ;
EQ        : E Q ;
EQ_SYMBOL : '=' ;
EQ_ALIAS : '==' ;
NE_SYMBOL : '!=' ;
LE        : L E ;
LE_SYMBOL : '<=' ;
GE        : G E ;
GE_SYMBOL : '>=' ;

// IO LOGIC
FROM    : F R O M ;
TO      : T O ;
INPUT   : I N P U T ;
OUTPUT  : O U T P U T ;
WHERE   : W H E R E ;
SLOTS   : S L O T S ;
SLOT   : S L O T ;
RETAIN  : R E T A I N ;
EACH    : E A C H ;
EXCEPT  : E X C E P T ;
FORGET  : F O R G E T ;
EMPTY   : E M P T Y ;
IN      : I N ;

// WITH LOGIC
WITHOUT : W I T H O U T;
WITH    : W I T H ;
TAG     : T A G ;
HASHTAG : '#' ;

// ROUND ROBIN
ROUND : R O U N D ;
ROBIN : R O B I N ;
BY    : B Y ;
LABEL : L A B E L ;
BLOCK : B L O C K ;

// SIDE LOGIC
TOP     : T O P ;
BOTTOM  : B O T T O M ;
NORTH   : N O R T H ;
EAST    : E A S T ;
SOUTH   : S O U T H ;
WEST    : W E S T ;
SIDE    : S I D E ;
LEFT    : L E F T ;
RIGHT   : R I G H T ;
FRONT   : F R O N T ;
BACK    : B A C K ;
NULL    : N U L L ;


// TIMER TRIGGERS
TICKS   : T I C K S ;
TICK    : T I C K ;
SECONDS : S E C O N D S ;
SECOND  : S E C O N D ;
GLOBAL  : (G L O B A L) | G;
PLUS    : '+' | P L U S;

// REDSTONE TRIGGER
REDSTONE        : R E D S T O N E ;
LEVEL           : L E V E L ;
LEVELS          : L E V E L S ;
PULSE           : P U L S E;

// PROGRAM SYMBOLS
DO              : D O ;
END             : E N D ;
NAME            : N A M E ;

// GENERAL SYMBOLS
// used by triggers and as a set operator
EVERY           : E V E R Y ;

COMMA   : ',';
COLON   : ':';
SLASH   : '/';
DASH    : '-';
LPAREN  : '(';
RPAREN  : ')';


NUMBER_WITH_G_SUFFIX    : [0-9]+[gG] ;
NUMBER                  : [0-9]+ ;
IDENTIFIER              : [a-zA-Z_*][a-zA-Z0-9_*]* | '*'; // Note that the * in the square brackets is a literl

// Pair backslashes before considering a closing quote. Legacy label decoding stays unchanged.
STRING : '"' ('\\' . | ~["\\])* '"' ;

LINE_COMMENT : '--' ~[\r\n]* -> channel(HIDDEN);
//LINE_COMMENT : '--' ~[\r\n]* (EOF|'\r'? '\n');

WS
        :   [ \r\t\n]+ -> channel(HIDDEN)
        ;

UNUSED
        :   {INCLUDE_UNUSED}? . -> channel(HIDDEN)
        ;

fragment A  :('a' | 'A') ;
fragment B  :('b' | 'B') ;
fragment C  :('c' | 'C') ;
fragment D  :('d' | 'D') ;
fragment E  :('e' | 'E') ;
fragment F  :('f' | 'F') ;
fragment G  :('g' | 'G') ;
fragment H  :('h' | 'H') ;
fragment I  :('i' | 'I') ;
fragment J  :('j' | 'J') ;
fragment K  :('k' | 'K') ;
fragment L  :('l' | 'L') ;
fragment M  :('m' | 'M') ;
fragment N  :('n' | 'N') ;
fragment O  :('o' | 'O') ;
fragment P  :('p' | 'P') ;
fragment Q  :('q' | 'Q') ;
fragment R  :('r' | 'R') ;
fragment S  :('s' | 'S') ;
fragment T  :('t' | 'T') ;
fragment U  :('u' | 'U') ;
fragment V  :('v' | 'V') ;
fragment W  :('w' | 'W') ;
fragment X  :('x' | 'X') ;
fragment Y  :('y' | 'Y') ;
fragment Z  :('z' | 'Z') ;
