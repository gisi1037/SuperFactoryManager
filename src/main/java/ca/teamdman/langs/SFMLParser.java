// Generated from src/main/resources/assets/superfactorymanager/grammar/SFML.g4 by ANTLR 4.13.2

package ca.teamdman.langs;

import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

@SuppressWarnings({ "all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape" })
public class SFMLParser extends Parser {

    static {
        RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
    public static final int IMPORT = 1, REVISION = 2, DIGEST = 3, SETCIRCUIT = 4, WAIT = 5, TRANSFER = 6, TRANSFERF = 7,
        INTO = 8, FUNCTION = 9, RETURNS = 10, RETURN = 11, REF = 12, LET = 13, SET = 14, STATE = 15, RISING = 16,
        FALLING = 17, PRINT = 18, FOR = 19, WHILE = 20, STEP = 21, VARIABLE = 22, LBRACKET = 23, RBRACKET = 24,
        LBRACE = 25, RBRACE = 26, DOT = 27, SEMICOLON = 28, PERCENT = 29, BIT_AND = 30, BIT_OR = 31, BIT_XOR = 32,
        BIT_NOT = 33, SHIFT_LEFT = 34, SHIFT_RIGHT = 35, SHIFT_UNSIGNED_RIGHT = 36, IF = 37, THEN = 38, ELSE = 39,
        HAS = 40, OVERALL = 41, SOME = 42, ONE = 43, LONE = 44, TRUE = 45, FALSE = 46, NOT = 47, AND = 48, OR = 49,
        GT = 50, GT_SYMBOL = 51, LT = 52, LT_SYMBOL = 53, EQ = 54, EQ_SYMBOL = 55, EQ_ALIAS = 56, NE_SYMBOL = 57,
        LE = 58, LE_SYMBOL = 59, GE = 60, GE_SYMBOL = 61, FROM = 62, TO = 63, INPUT = 64, OUTPUT = 65, WHERE = 66,
        SLOTS = 67, SLOT = 68, RETAIN = 69, EACH = 70, EXCEPT = 71, FORGET = 72, EMPTY = 73, IN = 74, WITHOUT = 75,
        WITH = 76, TAG = 77, HASHTAG = 78, ROUND = 79, ROBIN = 80, BY = 81, LABEL = 82, BLOCK = 83, TOP = 84,
        BOTTOM = 85, NORTH = 86, EAST = 87, SOUTH = 88, WEST = 89, SIDE = 90, LEFT = 91, RIGHT = 92, FRONT = 93,
        BACK = 94, NULL = 95, TICKS = 96, TICK = 97, SECONDS = 98, SECOND = 99, GLOBAL = 100, PLUS = 101,
        REDSTONE = 102, LEVEL = 103, LEVELS = 104, PULSE = 105, DO = 106, END = 107, NAME = 108, EVERY = 109,
        COMMA = 110, COLON = 111, SLASH = 112, DASH = 113, LPAREN = 114, RPAREN = 115, NUMBER_WITH_G_SUFFIX = 116,
        NUMBER = 117, IDENTIFIER = 118, STRING = 119, LINE_COMMENT = 120, WS = 121, UNUSED = 122;
    public static final int RULE_program = 0, RULE_importDeclaration = 1, RULE_functionDefinition = 2,
        RULE_functionName = 3, RULE_functionParameter = 4, RULE_variableName = 5, RULE_dataType = 6,
        RULE_typeDimension = 7, RULE_typeField = 8, RULE_functionCall = 9, RULE_functionArgument = 10,
        RULE_returnStatement = 11, RULE_functionCallStatement = 12, RULE_name = 13, RULE_trigger = 14,
        RULE_interval = 15, RULE_block = 16, RULE_statement = 17, RULE_redstoneOutputStatement = 18,
        RULE_forgetStatement = 19, RULE_inputStatement = 20, RULE_outputStatement = 21, RULE_inputResourceLimits = 22,
        RULE_outputResourceLimits = 23, RULE_resourceLimitList = 24, RULE_resourceLimit = 25, RULE_limit = 26,
        RULE_quantity = 27, RULE_retention = 28, RULE_resourceExclusion = 29, RULE_resourceId = 30,
        RULE_resourceIdList = 31, RULE_resourceIdDisjunction = 32, RULE_with = 33, RULE_withClause = 34,
        RULE_tagMatcher = 35, RULE_sidequalifier = 36, RULE_side = 37, RULE_slotqualifier = 38, RULE_rangeset = 39,
        RULE_range = 40, RULE_ifStatement = 41, RULE_forStatement = 42, RULE_forInitializer = 43, RULE_forUpdate = 44,
        RULE_whileStatement = 45, RULE_boolexpr = 46, RULE_comparisonOp = 47, RULE_setOp = 48, RULE_labelAccess = 49,
        RULE_roundrobin = 50, RULE_label = 51, RULE_emptyslots = 52, RULE_identifier = 53, RULE_stateDeclaration = 54,
        RULE_dataDeclaration = 55, RULE_dataAssignment = 56, RULE_circuitStatement = 57, RULE_waitStatement = 58,
        RULE_printStatement = 59, RULE_transferStatement = 60, RULE_dataTarget = 61, RULE_dataExpression = 62,
        RULE_dataOr = 63, RULE_dataAnd = 64, RULE_dataNot = 65, RULE_dataComparison = 66, RULE_dataBitOr = 67,
        RULE_dataBitXor = 68, RULE_dataBitAnd = 69, RULE_dataShift = 70, RULE_dataAdd = 71, RULE_dataMultiply = 72,
        RULE_multiplyOperator = 73, RULE_dataSign = 74, RULE_dataPostfix = 75, RULE_dataSuffix = 76,
        RULE_dataPrimary = 77, RULE_dataField = 78, RULE_dataName = 79, RULE_string = 80, RULE_number = 81;

    private static String[] makeRuleNames() {
        return new String[] { "program", "importDeclaration", "functionDefinition", "functionName", "functionParameter",
            "variableName", "dataType", "typeDimension", "typeField", "functionCall", "functionArgument",
            "returnStatement", "functionCallStatement", "name", "trigger", "interval", "block", "statement",
            "redstoneOutputStatement", "forgetStatement", "inputStatement", "outputStatement", "inputResourceLimits",
            "outputResourceLimits", "resourceLimitList", "resourceLimit", "limit", "quantity", "retention",
            "resourceExclusion", "resourceId", "resourceIdList", "resourceIdDisjunction", "with", "withClause",
            "tagMatcher", "sidequalifier", "side", "slotqualifier", "rangeset", "range", "ifStatement", "forStatement",
            "forInitializer", "forUpdate", "whileStatement", "boolexpr", "comparisonOp", "setOp", "labelAccess",
            "roundrobin", "label", "emptyslots", "identifier", "stateDeclaration", "dataDeclaration", "dataAssignment",
            "circuitStatement", "waitStatement", "printStatement", "transferStatement", "dataTarget", "dataExpression",
            "dataOr", "dataAnd", "dataNot", "dataComparison", "dataBitOr", "dataBitXor", "dataBitAnd", "dataShift",
            "dataAdd", "dataMultiply", "multiplyOperator", "dataSign", "dataPostfix", "dataSuffix", "dataPrimary",
            "dataField", "dataName", "string", "number" };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, "'['", "']'", "'{'", "'}'", "'.'", "';'", "'%'", "'&'",
            "'|'", "'^'", "'~'", "'<<'", "'>>'", "'>>>'", null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, "'>'", null, "'<'", null, "'='", "'=='", "'!='", null, "'<='", null, "'>='", null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "'#'", null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, "','", "':'", "'/'", "'-'", "'('",
            "')'" };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[] { null, "IMPORT", "REVISION", "DIGEST", "SETCIRCUIT", "WAIT", "TRANSFER", "TRANSFERF",
            "INTO", "FUNCTION", "RETURNS", "RETURN", "REF", "LET", "SET", "STATE", "RISING", "FALLING", "PRINT", "FOR",
            "WHILE", "STEP", "VARIABLE", "LBRACKET", "RBRACKET", "LBRACE", "RBRACE", "DOT", "SEMICOLON", "PERCENT",
            "BIT_AND", "BIT_OR", "BIT_XOR", "BIT_NOT", "SHIFT_LEFT", "SHIFT_RIGHT", "SHIFT_UNSIGNED_RIGHT", "IF",
            "THEN", "ELSE", "HAS", "OVERALL", "SOME", "ONE", "LONE", "TRUE", "FALSE", "NOT", "AND", "OR", "GT",
            "GT_SYMBOL", "LT", "LT_SYMBOL", "EQ", "EQ_SYMBOL", "EQ_ALIAS", "NE_SYMBOL", "LE", "LE_SYMBOL", "GE",
            "GE_SYMBOL", "FROM", "TO", "INPUT", "OUTPUT", "WHERE", "SLOTS", "SLOT", "RETAIN", "EACH", "EXCEPT",
            "FORGET", "EMPTY", "IN", "WITHOUT", "WITH", "TAG", "HASHTAG", "ROUND", "ROBIN", "BY", "LABEL", "BLOCK",
            "TOP", "BOTTOM", "NORTH", "EAST", "SOUTH", "WEST", "SIDE", "LEFT", "RIGHT", "FRONT", "BACK", "NULL",
            "TICKS", "TICK", "SECONDS", "SECOND", "GLOBAL", "PLUS", "REDSTONE", "LEVEL", "LEVELS", "PULSE", "DO", "END",
            "NAME", "EVERY", "COMMA", "COLON", "SLASH", "DASH", "LPAREN", "RPAREN", "NUMBER_WITH_G_SUFFIX", "NUMBER",
            "IDENTIFIER", "STRING", "LINE_COMMENT", "WS", "UNUSED" };
    }

    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "SFML.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public SFMLParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ProgramContext extends ParserRuleContext {

        public TerminalNode EOF() {
            return getToken(SFMLParser.EOF, 0);
        }

        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        public List<ImportDeclarationContext> importDeclaration() {
            return getRuleContexts(ImportDeclarationContext.class);
        }

        public ImportDeclarationContext importDeclaration(int i) {
            return getRuleContext(ImportDeclarationContext.class, i);
        }

        public List<FunctionDefinitionContext> functionDefinition() {
            return getRuleContexts(FunctionDefinitionContext.class);
        }

        public FunctionDefinitionContext functionDefinition(int i) {
            return getRuleContext(FunctionDefinitionContext.class, i);
        }

        public List<TriggerContext> trigger() {
            return getRuleContexts(TriggerContext.class);
        }

        public TriggerContext trigger(int i) {
            return getRuleContext(TriggerContext.class, i);
        }

        public List<StateDeclarationContext> stateDeclaration() {
            return getRuleContexts(StateDeclarationContext.class);
        }

        public StateDeclarationContext stateDeclaration(int i) {
            return getRuleContext(StateDeclarationContext.class, i);
        }

        public List<DataDeclarationContext> dataDeclaration() {
            return getRuleContexts(DataDeclarationContext.class);
        }

        public DataDeclarationContext dataDeclaration(int i) {
            return getRuleContext(DataDeclarationContext.class, i);
        }

        public List<TerminalNode> SEMICOLON() {
            return getTokens(SFMLParser.SEMICOLON);
        }

        public TerminalNode SEMICOLON(int i) {
            return getToken(SFMLParser.SEMICOLON, i);
        }

        public ProgramContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_program;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterProgram(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitProgram(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitProgram(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ProgramContext program() throws RecognitionException {
        ProgramContext _localctx = new ProgramContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_program);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(165);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
                    case 1: {
                        setState(164);
                        name();
                    }
                        break;
                }
                setState(170);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 1, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(167);
                                importDeclaration();
                            }
                        }
                    }
                    setState(172);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 1, _ctx);
                }
                setState(182);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(175);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case STATE: {
                                        setState(173);
                                        stateDeclaration();
                                    }
                                        break;
                                    case LET: {
                                        setState(174);
                                        dataDeclaration();
                                    }
                                        break;
                                    default:
                                        throw new NoViableAltException(this);
                                }
                                setState(178);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                if (_la == SEMICOLON) {
                                    {
                                        setState(177);
                                        match(SEMICOLON);
                                    }
                                }

                            }
                        }
                    }
                    setState(184);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
                }
                setState(189);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764477119500290L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 18084767253643263L) != 0)) {
                    {
                        setState(187);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 5, _ctx)) {
                            case 1: {
                                setState(185);
                                functionDefinition();
                            }
                                break;
                            case 2: {
                                setState(186);
                                trigger();
                            }
                                break;
                        }
                    }
                    setState(191);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(192);
                match(EOF);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ImportDeclarationContext extends ParserRuleContext {

        public TerminalNode IMPORT() {
            return getToken(SFMLParser.IMPORT, 0);
        }

        public IdentifierContext identifier() {
            return getRuleContext(IdentifierContext.class, 0);
        }

        public TerminalNode FROM() {
            return getToken(SFMLParser.FROM, 0);
        }

        public List<StringContext> string() {
            return getRuleContexts(StringContext.class);
        }

        public StringContext string(int i) {
            return getRuleContext(StringContext.class, i);
        }

        public TerminalNode REVISION() {
            return getToken(SFMLParser.REVISION, 0);
        }

        public TerminalNode NUMBER() {
            return getToken(SFMLParser.NUMBER, 0);
        }

        public TerminalNode DIGEST() {
            return getToken(SFMLParser.DIGEST, 0);
        }

        public TerminalNode SEMICOLON() {
            return getToken(SFMLParser.SEMICOLON, 0);
        }

        public ImportDeclarationContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_importDeclaration;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterImportDeclaration(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitImportDeclaration(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitImportDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ImportDeclarationContext importDeclaration() throws RecognitionException {
        ImportDeclarationContext _localctx = new ImportDeclarationContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_importDeclaration);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(194);
                match(IMPORT);
                setState(195);
                identifier();
                setState(196);
                match(FROM);
                setState(197);
                string();
                setState(198);
                match(REVISION);
                setState(199);
                match(NUMBER);
                setState(200);
                match(DIGEST);
                setState(201);
                string();
                setState(203);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SEMICOLON) {
                    {
                        setState(202);
                        match(SEMICOLON);
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class FunctionDefinitionContext extends ParserRuleContext {

        public TerminalNode FUNCTION() {
            return getToken(SFMLParser.FUNCTION, 0);
        }

        public FunctionNameContext functionName() {
            return getRuleContext(FunctionNameContext.class, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public TerminalNode RETURNS() {
            return getToken(SFMLParser.RETURNS, 0);
        }

        public DataTypeContext dataType() {
            return getRuleContext(DataTypeContext.class, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public List<FunctionParameterContext> functionParameter() {
            return getRuleContexts(FunctionParameterContext.class);
        }

        public FunctionParameterContext functionParameter(int i) {
            return getRuleContext(FunctionParameterContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public TerminalNode THEN() {
            return getToken(SFMLParser.THEN, 0);
        }

        public FunctionDefinitionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_functionDefinition;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterFunctionDefinition(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitFunctionDefinition(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitFunctionDefinition(this);
            else return visitor.visitChildren(this);
        }
    }

    public final FunctionDefinitionContext functionDefinition() throws RecognitionException {
        FunctionDefinitionContext _localctx = new FunctionDefinitionContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_functionDefinition);
        int _la;
        try {
            setState(255);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 14, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(205);
                    match(FUNCTION);
                    setState(206);
                    functionName();
                    setState(207);
                    match(LPAREN);
                    setState(216);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764477115301890L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 18084767253643263L) != 0)) {
                        {
                            setState(208);
                            functionParameter();
                            setState(213);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(209);
                                        match(COMMA);
                                        setState(210);
                                        functionParameter();
                                    }
                                }
                                setState(215);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                        }
                    }

                    setState(218);
                    match(RPAREN);
                    setState(219);
                    match(RETURNS);
                    setState(220);
                    dataType();
                    setState(229);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(221);
                            match(DO);
                            setState(222);
                            block();
                            setState(223);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(225);
                            match(LBRACE);
                            setState(226);
                            block();
                            setState(227);
                            match(RBRACE);
                        }
                            break;
                        default:
                            throw new NoViableAltException(this);
                    }
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(231);
                    dataType();
                    setState(232);
                    functionName();
                    setState(233);
                    match(LPAREN);
                    setState(242);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764477115301890L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 18084767253643263L) != 0)) {
                        {
                            setState(234);
                            functionParameter();
                            setState(239);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(235);
                                        match(COMMA);
                                        setState(236);
                                        functionParameter();
                                    }
                                }
                                setState(241);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                        }
                    }

                    setState(244);
                    match(RPAREN);
                    setState(253);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case THEN:
                        case DO: {
                            setState(245);
                            _la = _input.LA(1);
                            if (!(_la == THEN || _la == DO)) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                            setState(246);
                            block();
                            setState(247);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(249);
                            match(LBRACE);
                            setState(250);
                            block();
                            setState(251);
                            match(RBRACE);
                        }
                            break;
                        default:
                            throw new NoViableAltException(this);
                    }
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class FunctionNameContext extends ParserRuleContext {

        public List<DataNameContext> dataName() {
            return getRuleContexts(DataNameContext.class);
        }

        public DataNameContext dataName(int i) {
            return getRuleContext(DataNameContext.class, i);
        }

        public List<TerminalNode> DOT() {
            return getTokens(SFMLParser.DOT);
        }

        public TerminalNode DOT(int i) {
            return getToken(SFMLParser.DOT, i);
        }

        public FunctionNameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_functionName;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterFunctionName(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitFunctionName(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitFunctionName(this);
            else return visitor.visitChildren(this);
        }
    }

    public final FunctionNameContext functionName() throws RecognitionException {
        FunctionNameContext _localctx = new FunctionNameContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_functionName);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(257);
                dataName();
                setState(262);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == DOT) {
                    {
                        {
                            setState(258);
                            match(DOT);
                            setState(259);
                            dataName();
                        }
                    }
                    setState(264);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class FunctionParameterContext extends ParserRuleContext {

        public VariableNameContext variableName() {
            return getRuleContext(VariableNameContext.class, 0);
        }

        public TerminalNode COLON() {
            return getToken(SFMLParser.COLON, 0);
        }

        public DataTypeContext dataType() {
            return getRuleContext(DataTypeContext.class, 0);
        }

        public TerminalNode REF() {
            return getToken(SFMLParser.REF, 0);
        }

        public FunctionParameterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_functionParameter;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterFunctionParameter(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitFunctionParameter(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitFunctionParameter(this);
            else return visitor.visitChildren(this);
        }
    }

    public final FunctionParameterContext functionParameter() throws RecognitionException {
        FunctionParameterContext _localctx = new FunctionParameterContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_functionParameter);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(266);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == REF) {
                    {
                        setState(265);
                        match(REF);
                    }
                }

                setState(275);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 17, _ctx)) {
                    case 1: {
                        setState(268);
                        variableName();
                        setState(269);
                        match(COLON);
                        setState(270);
                        dataType();
                    }
                        break;
                    case 2: {
                        setState(272);
                        dataType();
                        setState(273);
                        variableName();
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class VariableNameContext extends ParserRuleContext {

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
        }

        public TerminalNode IDENTIFIER() {
            return getToken(SFMLParser.IDENTIFIER, 0);
        }

        public TerminalNode IMPORT() {
            return getToken(SFMLParser.IMPORT, 0);
        }

        public TerminalNode REVISION() {
            return getToken(SFMLParser.REVISION, 0);
        }

        public TerminalNode DIGEST() {
            return getToken(SFMLParser.DIGEST, 0);
        }

        public VariableNameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_variableName;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterVariableName(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitVariableName(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitVariableName(this);
            else return visitor.visitChildren(this);
        }
    }

    public final VariableNameContext variableName() throws RecognitionException {
        VariableNameContext _localctx = new VariableNameContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_variableName);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(277);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 4194318L) != 0) || _la == IDENTIFIER)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataTypeContext extends ParserRuleContext {

        public DataNameContext dataName() {
            return getRuleContext(DataNameContext.class, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public List<TypeDimensionContext> typeDimension() {
            return getRuleContexts(TypeDimensionContext.class);
        }

        public TypeDimensionContext typeDimension(int i) {
            return getRuleContext(TypeDimensionContext.class, i);
        }

        public List<TypeFieldContext> typeField() {
            return getRuleContexts(TypeFieldContext.class);
        }

        public TypeFieldContext typeField(int i) {
            return getRuleContext(TypeFieldContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public DataTypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataType;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataType(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataType(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataType(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataTypeContext dataType() throws RecognitionException {
        DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_dataType);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(292);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case IMPORT:
                    case REVISION:
                    case DIGEST:
                    case SETCIRCUIT:
                    case WAIT:
                    case TRANSFER:
                    case TRANSFERF:
                    case INTO:
                    case FUNCTION:
                    case RETURNS:
                    case RETURN:
                    case LET:
                    case SET:
                    case STATE:
                    case RISING:
                    case FALLING:
                    case PRINT:
                    case FOR:
                    case WHILE:
                    case STEP:
                    case IF:
                    case THEN:
                    case ELSE:
                    case HAS:
                    case OVERALL:
                    case SOME:
                    case ONE:
                    case LONE:
                    case TRUE:
                    case FALSE:
                    case NOT:
                    case AND:
                    case OR:
                    case GT:
                    case LT:
                    case EQ:
                    case LE:
                    case GE:
                    case FROM:
                    case TO:
                    case INPUT:
                    case OUTPUT:
                    case WHERE:
                    case SLOTS:
                    case SLOT:
                    case RETAIN:
                    case EACH:
                    case EXCEPT:
                    case FORGET:
                    case EMPTY:
                    case IN:
                    case WITHOUT:
                    case WITH:
                    case TAG:
                    case ROUND:
                    case ROBIN:
                    case BY:
                    case LABEL:
                    case BLOCK:
                    case TOP:
                    case BOTTOM:
                    case NORTH:
                    case EAST:
                    case SOUTH:
                    case WEST:
                    case SIDE:
                    case LEFT:
                    case RIGHT:
                    case FRONT:
                    case BACK:
                    case NULL:
                    case TICKS:
                    case TICK:
                    case SECONDS:
                    case SECOND:
                    case GLOBAL:
                    case PLUS:
                    case REDSTONE:
                    case LEVEL:
                    case LEVELS:
                    case PULSE:
                    case DO:
                    case END:
                    case NAME:
                    case EVERY:
                    case IDENTIFIER: {
                        setState(279);
                        dataName();
                    }
                        break;
                    case LBRACE: {
                        setState(280);
                        match(LBRACE);
                        setState(289);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764477153054722L) != 0)
                            || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 18084767253643263L) != 0)) {
                            {
                                setState(281);
                                typeField();
                                setState(286);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(282);
                                            match(COMMA);
                                            setState(283);
                                            typeField();
                                        }
                                    }
                                    setState(288);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                        }

                        setState(291);
                        match(RBRACE);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(297);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACKET) {
                    {
                        {
                            setState(294);
                            typeDimension();
                        }
                    }
                    setState(299);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class TypeDimensionContext extends ParserRuleContext {

        public TerminalNode LBRACKET() {
            return getToken(SFMLParser.LBRACKET, 0);
        }

        public TerminalNode RBRACKET() {
            return getToken(SFMLParser.RBRACKET, 0);
        }

        public TerminalNode NUMBER() {
            return getToken(SFMLParser.NUMBER, 0);
        }

        public TypeDimensionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_typeDimension;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterTypeDimension(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitTypeDimension(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitTypeDimension(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TypeDimensionContext typeDimension() throws RecognitionException {
        TypeDimensionContext _localctx = new TypeDimensionContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_typeDimension);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(300);
                match(LBRACKET);
                setState(302);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == NUMBER) {
                    {
                        setState(301);
                        match(NUMBER);
                    }
                }

                setState(304);
                match(RBRACKET);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class TypeFieldContext extends ParserRuleContext {

        public DataNameContext dataName() {
            return getRuleContext(DataNameContext.class, 0);
        }

        public TerminalNode COLON() {
            return getToken(SFMLParser.COLON, 0);
        }

        public DataTypeContext dataType() {
            return getRuleContext(DataTypeContext.class, 0);
        }

        public TypeFieldContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_typeField;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterTypeField(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitTypeField(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitTypeField(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TypeFieldContext typeField() throws RecognitionException {
        TypeFieldContext _localctx = new TypeFieldContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_typeField);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(306);
                dataName();
                setState(307);
                match(COLON);
                setState(308);
                dataType();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class FunctionCallContext extends ParserRuleContext {

        public FunctionNameContext functionName() {
            return getRuleContext(FunctionNameContext.class, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public List<FunctionArgumentContext> functionArgument() {
            return getRuleContexts(FunctionArgumentContext.class);
        }

        public FunctionArgumentContext functionArgument(int i) {
            return getRuleContext(FunctionArgumentContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public FunctionCallContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_functionCall;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterFunctionCall(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitFunctionCall(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitFunctionCall(this);
            else return visitor.visitChildren(this);
        }
    }

    public final FunctionCallContext functionCall() throws RecognitionException {
        FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
        enterRule(_localctx, 18, RULE_functionCall);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(310);
                functionName();
                setState(311);
                match(LPAREN);
                setState(320);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764468516978690L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 64809613387612159L) != 0)) {
                    {
                        setState(312);
                        functionArgument();
                        setState(317);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(313);
                                    match(COMMA);
                                    setState(314);
                                    functionArgument();
                                }
                            }
                            setState(319);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(322);
                match(RPAREN);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class FunctionArgumentContext extends ParserRuleContext {

        public TerminalNode REF() {
            return getToken(SFMLParser.REF, 0);
        }

        public DataTargetContext dataTarget() {
            return getRuleContext(DataTargetContext.class, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public FunctionArgumentContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_functionArgument;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterFunctionArgument(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitFunctionArgument(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitFunctionArgument(this);
            else return visitor.visitChildren(this);
        }
    }

    public final FunctionArgumentContext functionArgument() throws RecognitionException {
        FunctionArgumentContext _localctx = new FunctionArgumentContext(_ctx, getState());
        enterRule(_localctx, 20, RULE_functionArgument);
        try {
            setState(327);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case REF:
                    enterOuterAlt(_localctx, 1); {
                    setState(324);
                    match(REF);
                    setState(325);
                    dataTarget();
                }
                    break;
                case IMPORT:
                case REVISION:
                case DIGEST:
                case SETCIRCUIT:
                case WAIT:
                case TRANSFER:
                case TRANSFERF:
                case INTO:
                case FUNCTION:
                case RETURNS:
                case RETURN:
                case LET:
                case SET:
                case STATE:
                case RISING:
                case FALLING:
                case PRINT:
                case FOR:
                case WHILE:
                case STEP:
                case VARIABLE:
                case LBRACKET:
                case LBRACE:
                case BIT_NOT:
                case IF:
                case THEN:
                case ELSE:
                case HAS:
                case OVERALL:
                case SOME:
                case ONE:
                case LONE:
                case TRUE:
                case FALSE:
                case NOT:
                case AND:
                case OR:
                case GT:
                case LT:
                case EQ:
                case LE:
                case GE:
                case FROM:
                case TO:
                case INPUT:
                case OUTPUT:
                case WHERE:
                case SLOTS:
                case SLOT:
                case RETAIN:
                case EACH:
                case EXCEPT:
                case FORGET:
                case EMPTY:
                case IN:
                case WITHOUT:
                case WITH:
                case TAG:
                case ROUND:
                case ROBIN:
                case BY:
                case LABEL:
                case BLOCK:
                case TOP:
                case BOTTOM:
                case NORTH:
                case EAST:
                case SOUTH:
                case WEST:
                case SIDE:
                case LEFT:
                case RIGHT:
                case FRONT:
                case BACK:
                case NULL:
                case TICKS:
                case TICK:
                case SECONDS:
                case SECOND:
                case GLOBAL:
                case PLUS:
                case REDSTONE:
                case LEVEL:
                case LEVELS:
                case PULSE:
                case DO:
                case END:
                case NAME:
                case EVERY:
                case DASH:
                case LPAREN:
                case NUMBER:
                case IDENTIFIER:
                case STRING:
                    enterOuterAlt(_localctx, 2); {
                    setState(326);
                    dataExpression();
                }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ReturnStatementContext extends ParserRuleContext {

        public TerminalNode RETURN() {
            return getToken(SFMLParser.RETURN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public ReturnStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_returnStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterReturnStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitReturnStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitReturnStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ReturnStatementContext returnStatement() throws RecognitionException {
        ReturnStatementContext _localctx = new ReturnStatementContext(_ctx, getState());
        enterRule(_localctx, 22, RULE_returnStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(329);
                match(RETURN);
                setState(331);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 26, _ctx)) {
                    case 1: {
                        setState(330);
                        dataExpression();
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class FunctionCallStatementContext extends ParserRuleContext {

        public FunctionCallContext functionCall() {
            return getRuleContext(FunctionCallContext.class, 0);
        }

        public FunctionCallStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_functionCallStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterFunctionCallStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitFunctionCallStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitFunctionCallStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final FunctionCallStatementContext functionCallStatement() throws RecognitionException {
        FunctionCallStatementContext _localctx = new FunctionCallStatementContext(_ctx, getState());
        enterRule(_localctx, 24, RULE_functionCallStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(333);
                functionCall();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class NameContext extends ParserRuleContext {

        public TerminalNode NAME() {
            return getToken(SFMLParser.NAME, 0);
        }

        public StringContext string() {
            return getRuleContext(StringContext.class, 0);
        }

        public NameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterName(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitName(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitName(this);
            else return visitor.visitChildren(this);
        }
    }

    public final NameContext name() throws RecognitionException {
        NameContext _localctx = new NameContext(_ctx, getState());
        enterRule(_localctx, 26, RULE_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(335);
                match(NAME);
                setState(336);
                string();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class TriggerContext extends ParserRuleContext {

        public TriggerContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_trigger;
        }

        public TriggerContext() {}

        public void copyFrom(TriggerContext ctx) {
            super.copyFrom(ctx);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class NamedRedstoneTriggerContext extends TriggerContext {

        public TerminalNode EVERY() {
            return getToken(SFMLParser.EVERY, 0);
        }

        public TerminalNode REDSTONE() {
            return getToken(SFMLParser.REDSTONE, 0);
        }

        public TerminalNode FROM() {
            return getToken(SFMLParser.FROM, 0);
        }

        public LabelAccessContext labelAccess() {
            return getRuleContext(LabelAccessContext.class, 0);
        }

        public TerminalNode PULSE() {
            return getToken(SFMLParser.PULSE, 0);
        }

        public TerminalNode RISING() {
            return getToken(SFMLParser.RISING, 0);
        }

        public TerminalNode FALLING() {
            return getToken(SFMLParser.FALLING, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public NamedRedstoneTriggerContext(TriggerContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterNamedRedstoneTrigger(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitNamedRedstoneTrigger(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitNamedRedstoneTrigger(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class PulseTriggerContext extends TriggerContext {

        public TerminalNode EVERY() {
            return getToken(SFMLParser.EVERY, 0);
        }

        public TerminalNode REDSTONE() {
            return getToken(SFMLParser.REDSTONE, 0);
        }

        public TerminalNode PULSE() {
            return getToken(SFMLParser.PULSE, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public PulseTriggerContext(TriggerContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterPulseTrigger(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitPulseTrigger(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitPulseTrigger(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class TimerTriggerContext extends TriggerContext {

        public TerminalNode EVERY() {
            return getToken(SFMLParser.EVERY, 0);
        }

        public IntervalContext interval() {
            return getRuleContext(IntervalContext.class, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public TimerTriggerContext(TriggerContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterTimerTrigger(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitTimerTrigger(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitTimerTrigger(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TriggerContext trigger() throws RecognitionException {
        TriggerContext _localctx = new TriggerContext(_ctx, getState());
        enterRule(_localctx, 28, RULE_trigger);
        int _la;
        try {
            setState(378);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 30, _ctx)) {
                case 1:
                    _localctx = new TimerTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(338);
                    match(EVERY);
                    setState(339);
                    interval();
                    setState(348);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(340);
                            match(DO);
                            setState(341);
                            block();
                            setState(342);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(344);
                            match(LBRACE);
                            setState(345);
                            block();
                            setState(346);
                            match(RBRACE);
                        }
                            break;
                        default:
                            throw new NoViableAltException(this);
                    }
                }
                    break;
                case 2:
                    _localctx = new PulseTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(350);
                    match(EVERY);
                    setState(351);
                    match(REDSTONE);
                    setState(352);
                    match(PULSE);
                    setState(361);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(353);
                            match(DO);
                            setState(354);
                            block();
                            setState(355);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(357);
                            match(LBRACE);
                            setState(358);
                            block();
                            setState(359);
                            match(RBRACE);
                        }
                            break;
                        default:
                            throw new NoViableAltException(this);
                    }
                }
                    break;
                case 3:
                    _localctx = new NamedRedstoneTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(363);
                    match(EVERY);
                    setState(364);
                    match(REDSTONE);
                    setState(365);
                    _la = _input.LA(1);
                    if (!(_la == RISING || _la == FALLING || _la == PULSE)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(366);
                    match(FROM);
                    setState(367);
                    labelAccess();
                    setState(376);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(368);
                            match(DO);
                            setState(369);
                            block();
                            setState(370);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(372);
                            match(LBRACE);
                            setState(373);
                            block();
                            setState(374);
                            match(RBRACE);
                        }
                            break;
                        default:
                            throw new NoViableAltException(this);
                    }
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class IntervalContext extends ParserRuleContext {

        public IntervalContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_interval;
        }

        public IntervalContext() {}

        public void copyFrom(IntervalContext ctx) {
            super.copyFrom(ctx);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class IntervalNoSpaceContext extends IntervalContext {

        public TerminalNode NUMBER_WITH_G_SUFFIX() {
            return getToken(SFMLParser.NUMBER_WITH_G_SUFFIX, 0);
        }

        public TerminalNode TICKS() {
            return getToken(SFMLParser.TICKS, 0);
        }

        public TerminalNode TICK() {
            return getToken(SFMLParser.TICK, 0);
        }

        public TerminalNode SECONDS() {
            return getToken(SFMLParser.SECONDS, 0);
        }

        public TerminalNode SECOND() {
            return getToken(SFMLParser.SECOND, 0);
        }

        public TerminalNode PLUS() {
            return getToken(SFMLParser.PLUS, 0);
        }

        public TerminalNode NUMBER() {
            return getToken(SFMLParser.NUMBER, 0);
        }

        public IntervalNoSpaceContext(IntervalContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterIntervalNoSpace(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitIntervalNoSpace(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitIntervalNoSpace(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class IntervalSpaceContext extends IntervalContext {

        public TerminalNode TICKS() {
            return getToken(SFMLParser.TICKS, 0);
        }

        public TerminalNode TICK() {
            return getToken(SFMLParser.TICK, 0);
        }

        public TerminalNode SECONDS() {
            return getToken(SFMLParser.SECONDS, 0);
        }

        public TerminalNode SECOND() {
            return getToken(SFMLParser.SECOND, 0);
        }

        public List<TerminalNode> NUMBER() {
            return getTokens(SFMLParser.NUMBER);
        }

        public TerminalNode NUMBER(int i) {
            return getToken(SFMLParser.NUMBER, i);
        }

        public TerminalNode GLOBAL() {
            return getToken(SFMLParser.GLOBAL, 0);
        }

        public TerminalNode PLUS() {
            return getToken(SFMLParser.PLUS, 0);
        }

        public IntervalSpaceContext(IntervalContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterIntervalSpace(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitIntervalSpace(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitIntervalSpace(this);
            else return visitor.visitChildren(this);
        }
    }

    public final IntervalContext interval() throws RecognitionException {
        IntervalContext _localctx = new IntervalContext(_ctx, getState());
        enterRule(_localctx, 30, RULE_interval);
        int _la;
        try {
            setState(397);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case TICKS:
                case TICK:
                case SECONDS:
                case SECOND:
                case GLOBAL:
                case PLUS:
                case NUMBER:
                    _localctx = new IntervalSpaceContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(381);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == NUMBER) {
                        {
                            setState(380);
                            match(NUMBER);
                        }
                    }

                    setState(384);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == GLOBAL) {
                        {
                            setState(383);
                            match(GLOBAL);
                        }
                    }

                    setState(388);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(386);
                            match(PLUS);
                            setState(387);
                            match(NUMBER);
                        }
                    }

                    setState(390);
                    _la = _input.LA(1);
                    if (!(((((_la - 96)) & ~0x3f) == 0 && ((1L << (_la - 96)) & 15L) != 0))) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                }
                    break;
                case NUMBER_WITH_G_SUFFIX:
                    _localctx = new IntervalNoSpaceContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(391);
                    match(NUMBER_WITH_G_SUFFIX);
                    setState(394);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(392);
                            match(PLUS);
                            setState(393);
                            match(NUMBER);
                        }
                    }

                    setState(396);
                    _la = _input.LA(1);
                    if (!(((((_la - 96)) & ~0x3f) == 0 && ((1L << (_la - 96)) & 15L) != 0))) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BlockContext extends ParserRuleContext {

        public List<StatementContext> statement() {
            return getRuleContexts(StatementContext.class);
        }

        public StatementContext statement(int i) {
            return getRuleContext(StatementContext.class, i);
        }

        public BlockContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_block;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBlock(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBlock(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitBlock(this);
            else return visitor.visitChildren(this);
        }
    }

    public final BlockContext block() throws RecognitionException {
        BlockContext _localctx = new BlockContext(_ctx, getState());
        enterRule(_localctx, 32, RULE_block);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(402);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 36, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(399);
                                statement();
                            }
                        }
                    }
                    setState(404);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 36, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class StatementContext extends ParserRuleContext {

        public CircuitStatementContext circuitStatement() {
            return getRuleContext(CircuitStatementContext.class, 0);
        }

        public WaitStatementContext waitStatement() {
            return getRuleContext(WaitStatementContext.class, 0);
        }

        public DataDeclarationContext dataDeclaration() {
            return getRuleContext(DataDeclarationContext.class, 0);
        }

        public DataAssignmentContext dataAssignment() {
            return getRuleContext(DataAssignmentContext.class, 0);
        }

        public PrintStatementContext printStatement() {
            return getRuleContext(PrintStatementContext.class, 0);
        }

        public TransferStatementContext transferStatement() {
            return getRuleContext(TransferStatementContext.class, 0);
        }

        public ReturnStatementContext returnStatement() {
            return getRuleContext(ReturnStatementContext.class, 0);
        }

        public InputStatementContext inputStatement() {
            return getRuleContext(InputStatementContext.class, 0);
        }

        public RedstoneOutputStatementContext redstoneOutputStatement() {
            return getRuleContext(RedstoneOutputStatementContext.class, 0);
        }

        public OutputStatementContext outputStatement() {
            return getRuleContext(OutputStatementContext.class, 0);
        }

        public IfStatementContext ifStatement() {
            return getRuleContext(IfStatementContext.class, 0);
        }

        public ForStatementContext forStatement() {
            return getRuleContext(ForStatementContext.class, 0);
        }

        public WhileStatementContext whileStatement() {
            return getRuleContext(WhileStatementContext.class, 0);
        }

        public ForgetStatementContext forgetStatement() {
            return getRuleContext(ForgetStatementContext.class, 0);
        }

        public FunctionCallStatementContext functionCallStatement() {
            return getRuleContext(FunctionCallStatementContext.class, 0);
        }

        public TerminalNode SEMICOLON() {
            return getToken(SFMLParser.SEMICOLON, 0);
        }

        public StatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_statement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final StatementContext statement() throws RecognitionException {
        StatementContext _localctx = new StatementContext(_ctx, getState());
        enterRule(_localctx, 34, RULE_statement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(420);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 37, _ctx)) {
                    case 1: {
                        setState(405);
                        circuitStatement();
                    }
                        break;
                    case 2: {
                        setState(406);
                        waitStatement();
                    }
                        break;
                    case 3: {
                        setState(407);
                        dataDeclaration();
                    }
                        break;
                    case 4: {
                        setState(408);
                        dataAssignment();
                    }
                        break;
                    case 5: {
                        setState(409);
                        printStatement();
                    }
                        break;
                    case 6: {
                        setState(410);
                        transferStatement();
                    }
                        break;
                    case 7: {
                        setState(411);
                        returnStatement();
                    }
                        break;
                    case 8: {
                        setState(412);
                        inputStatement();
                    }
                        break;
                    case 9: {
                        setState(413);
                        redstoneOutputStatement();
                    }
                        break;
                    case 10: {
                        setState(414);
                        outputStatement();
                    }
                        break;
                    case 11: {
                        setState(415);
                        ifStatement();
                    }
                        break;
                    case 12: {
                        setState(416);
                        forStatement();
                    }
                        break;
                    case 13: {
                        setState(417);
                        whileStatement();
                    }
                        break;
                    case 14: {
                        setState(418);
                        forgetStatement();
                    }
                        break;
                    case 15: {
                        setState(419);
                        functionCallStatement();
                    }
                        break;
                }
                setState(423);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SEMICOLON) {
                    {
                        setState(422);
                        match(SEMICOLON);
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class RedstoneOutputStatementContext extends ParserRuleContext {

        public TerminalNode OUTPUT() {
            return getToken(SFMLParser.OUTPUT, 0);
        }

        public TerminalNode REDSTONE() {
            return getToken(SFMLParser.REDSTONE, 0);
        }

        public TerminalNode TO() {
            return getToken(SFMLParser.TO, 0);
        }

        public LabelAccessContext labelAccess() {
            return getRuleContext(LabelAccessContext.class, 0);
        }

        public TerminalNode LEVEL() {
            return getToken(SFMLParser.LEVEL, 0);
        }

        public TerminalNode LEVELS() {
            return getToken(SFMLParser.LEVELS, 0);
        }

        public List<QuantityContext> quantity() {
            return getRuleContexts(QuantityContext.class);
        }

        public QuantityContext quantity(int i) {
            return getRuleContext(QuantityContext.class, i);
        }

        public TerminalNode EACH() {
            return getToken(SFMLParser.EACH, 0);
        }

        public RedstoneOutputStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_redstoneOutputStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterRedstoneOutputStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitRedstoneOutputStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitRedstoneOutputStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final RedstoneOutputStatementContext redstoneOutputStatement() throws RecognitionException {
        RedstoneOutputStatementContext _localctx = new RedstoneOutputStatementContext(_ctx, getState());
        enterRule(_localctx, 36, RULE_redstoneOutputStatement);
        int _la;
        try {
            setState(453);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(425);
                    match(OUTPUT);
                    setState(427);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(426);
                            quantity();
                        }
                    }

                    setState(429);
                    match(REDSTONE);
                    setState(430);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(432);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(431);
                            quantity();
                        }
                    }

                    setState(434);
                    match(TO);
                    setState(436);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 41, _ctx)) {
                        case 1: {
                            setState(435);
                            match(EACH);
                        }
                            break;
                    }
                    setState(438);
                    labelAccess();
                }
                    break;
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(439);
                    match(TO);
                    setState(441);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 42, _ctx)) {
                        case 1: {
                            setState(440);
                            match(EACH);
                        }
                            break;
                    }
                    setState(443);
                    labelAccess();
                    setState(444);
                    match(OUTPUT);
                    setState(446);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(445);
                            quantity();
                        }
                    }

                    setState(448);
                    match(REDSTONE);
                    setState(449);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(451);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(450);
                            quantity();
                        }
                    }

                }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ForgetStatementContext extends ParserRuleContext {

        public TerminalNode FORGET() {
            return getToken(SFMLParser.FORGET, 0);
        }

        public List<LabelContext> label() {
            return getRuleContexts(LabelContext.class);
        }

        public LabelContext label(int i) {
            return getRuleContext(LabelContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public ForgetStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_forgetStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterForgetStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitForgetStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitForgetStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ForgetStatementContext forgetStatement() throws RecognitionException {
        ForgetStatementContext _localctx = new ForgetStatementContext(_ctx, getState());
        enterRule(_localctx, 38, RULE_forgetStatement);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(455);
                match(FORGET);
                setState(457);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 46, _ctx)) {
                    case 1: {
                        setState(456);
                        label();
                    }
                        break;
                }
                setState(463);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 47, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(459);
                                match(COMMA);
                                setState(460);
                                label();
                            }
                        }
                    }
                    setState(465);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 47, _ctx);
                }
                setState(467);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(466);
                        match(COMMA);
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class InputStatementContext extends ParserRuleContext {

        public TerminalNode INPUT() {
            return getToken(SFMLParser.INPUT, 0);
        }

        public TerminalNode FROM() {
            return getToken(SFMLParser.FROM, 0);
        }

        public LabelAccessContext labelAccess() {
            return getRuleContext(LabelAccessContext.class, 0);
        }

        public InputResourceLimitsContext inputResourceLimits() {
            return getRuleContext(InputResourceLimitsContext.class, 0);
        }

        public ResourceExclusionContext resourceExclusion() {
            return getRuleContext(ResourceExclusionContext.class, 0);
        }

        public TerminalNode EACH() {
            return getToken(SFMLParser.EACH, 0);
        }

        public InputStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_inputStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterInputStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitInputStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitInputStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final InputStatementContext inputStatement() throws RecognitionException {
        InputStatementContext _localctx = new InputStatementContext(_ctx, getState());
        enterRule(_localctx, 40, RULE_inputStatement);
        int _la;
        try {
            setState(493);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case INPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(469);
                    match(INPUT);
                    setState(471);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4190206L) != 0)
                        || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & 2005573159715009L) != 0)) {
                        {
                            setState(470);
                            inputResourceLimits();
                        }
                    }

                    setState(474);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(473);
                            resourceExclusion();
                        }
                    }

                    setState(476);
                    match(FROM);
                    setState(478);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 51, _ctx)) {
                        case 1: {
                            setState(477);
                            match(EACH);
                        }
                            break;
                    }
                    setState(480);
                    labelAccess();
                }
                    break;
                case FROM:
                    enterOuterAlt(_localctx, 2); {
                    setState(481);
                    match(FROM);
                    setState(483);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 52, _ctx)) {
                        case 1: {
                            setState(482);
                            match(EACH);
                        }
                            break;
                    }
                    setState(485);
                    labelAccess();
                    setState(486);
                    match(INPUT);
                    setState(488);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 53, _ctx)) {
                        case 1: {
                            setState(487);
                            inputResourceLimits();
                        }
                            break;
                    }
                    setState(491);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 54, _ctx)) {
                        case 1: {
                            setState(490);
                            resourceExclusion();
                        }
                            break;
                    }
                }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class OutputStatementContext extends ParserRuleContext {

        public TerminalNode OUTPUT() {
            return getToken(SFMLParser.OUTPUT, 0);
        }

        public TerminalNode TO() {
            return getToken(SFMLParser.TO, 0);
        }

        public LabelAccessContext labelAccess() {
            return getRuleContext(LabelAccessContext.class, 0);
        }

        public OutputResourceLimitsContext outputResourceLimits() {
            return getRuleContext(OutputResourceLimitsContext.class, 0);
        }

        public ResourceExclusionContext resourceExclusion() {
            return getRuleContext(ResourceExclusionContext.class, 0);
        }

        public EmptyslotsContext emptyslots() {
            return getRuleContext(EmptyslotsContext.class, 0);
        }

        public TerminalNode EACH() {
            return getToken(SFMLParser.EACH, 0);
        }

        public OutputStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_outputStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterOutputStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitOutputStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitOutputStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final OutputStatementContext outputStatement() throws RecognitionException {
        OutputStatementContext _localctx = new OutputStatementContext(_ctx, getState());
        enterRule(_localctx, 42, RULE_outputStatement);
        int _la;
        try {
            setState(525);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(495);
                    match(OUTPUT);
                    setState(497);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4190206L) != 0)
                        || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & 2005573159715009L) != 0)) {
                        {
                            setState(496);
                            outputResourceLimits();
                        }
                    }

                    setState(500);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(499);
                            resourceExclusion();
                        }
                    }

                    setState(502);
                    match(TO);
                    setState(504);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 58, _ctx)) {
                        case 1: {
                            setState(503);
                            emptyslots();
                        }
                            break;
                    }
                    setState(507);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 59, _ctx)) {
                        case 1: {
                            setState(506);
                            match(EACH);
                        }
                            break;
                    }
                    setState(509);
                    labelAccess();
                }
                    break;
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(510);
                    match(TO);
                    setState(512);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 60, _ctx)) {
                        case 1: {
                            setState(511);
                            emptyslots();
                        }
                            break;
                    }
                    setState(515);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 61, _ctx)) {
                        case 1: {
                            setState(514);
                            match(EACH);
                        }
                            break;
                    }
                    setState(517);
                    labelAccess();
                    setState(518);
                    match(OUTPUT);
                    setState(520);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 62, _ctx)) {
                        case 1: {
                            setState(519);
                            outputResourceLimits();
                        }
                            break;
                    }
                    setState(523);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 63, _ctx)) {
                        case 1: {
                            setState(522);
                            resourceExclusion();
                        }
                            break;
                    }
                }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class InputResourceLimitsContext extends ParserRuleContext {

        public ResourceLimitListContext resourceLimitList() {
            return getRuleContext(ResourceLimitListContext.class, 0);
        }

        public InputResourceLimitsContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_inputResourceLimits;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterInputResourceLimits(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitInputResourceLimits(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitInputResourceLimits(this);
            else return visitor.visitChildren(this);
        }
    }

    public final InputResourceLimitsContext inputResourceLimits() throws RecognitionException {
        InputResourceLimitsContext _localctx = new InputResourceLimitsContext(_ctx, getState());
        enterRule(_localctx, 44, RULE_inputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(527);
                resourceLimitList();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class OutputResourceLimitsContext extends ParserRuleContext {

        public ResourceLimitListContext resourceLimitList() {
            return getRuleContext(ResourceLimitListContext.class, 0);
        }

        public OutputResourceLimitsContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_outputResourceLimits;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterOutputResourceLimits(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitOutputResourceLimits(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitOutputResourceLimits(this);
            else return visitor.visitChildren(this);
        }
    }

    public final OutputResourceLimitsContext outputResourceLimits() throws RecognitionException {
        OutputResourceLimitsContext _localctx = new OutputResourceLimitsContext(_ctx, getState());
        enterRule(_localctx, 46, RULE_outputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(529);
                resourceLimitList();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ResourceLimitListContext extends ParserRuleContext {

        public List<ResourceLimitContext> resourceLimit() {
            return getRuleContexts(ResourceLimitContext.class);
        }

        public ResourceLimitContext resourceLimit(int i) {
            return getRuleContext(ResourceLimitContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public ResourceLimitListContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_resourceLimitList;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterResourceLimitList(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitResourceLimitList(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitResourceLimitList(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ResourceLimitListContext resourceLimitList() throws RecognitionException {
        ResourceLimitListContext _localctx = new ResourceLimitListContext(_ctx, getState());
        enterRule(_localctx, 48, RULE_resourceLimitList);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(531);
                resourceLimit();
                setState(536);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 65, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(532);
                                match(COMMA);
                                setState(533);
                                resourceLimit();
                            }
                        }
                    }
                    setState(538);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 65, _ctx);
                }
                setState(540);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(539);
                        match(COMMA);
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ResourceLimitContext extends ParserRuleContext {

        public ResourceIdDisjunctionContext resourceIdDisjunction() {
            return getRuleContext(ResourceIdDisjunctionContext.class, 0);
        }

        public LimitContext limit() {
            return getRuleContext(LimitContext.class, 0);
        }

        public WithContext with() {
            return getRuleContext(WithContext.class, 0);
        }

        public ResourceLimitContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_resourceLimit;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterResourceLimit(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitResourceLimit(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitResourceLimit(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ResourceLimitContext resourceLimit() throws RecognitionException {
        ResourceLimitContext _localctx = new ResourceLimitContext(_ctx, getState());
        enterRule(_localctx, 50, RULE_resourceLimit);
        int _la;
        try {
            setState(554);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 70, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(543);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & 316659348799489L) != 0)) {
                        {
                            setState(542);
                            limit();
                        }
                    }

                    setState(545);
                    resourceIdDisjunction();
                    setState(547);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 68, _ctx)) {
                        case 1: {
                            setState(546);
                            with();
                        }
                            break;
                    }
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(549);
                    limit();
                    setState(551);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 69, _ctx)) {
                        case 1: {
                            setState(550);
                            with();
                        }
                            break;
                    }
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(553);
                    with();
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class LimitContext extends ParserRuleContext {

        public LimitContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_limit;
        }

        public LimitContext() {}

        public void copyFrom(LimitContext ctx) {
            super.copyFrom(ctx);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class RetentionLimitContext extends LimitContext {

        public RetentionContext retention() {
            return getRuleContext(RetentionContext.class, 0);
        }

        public RetentionLimitContext(LimitContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterRetentionLimit(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitRetentionLimit(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitRetentionLimit(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class QuantityRetentionLimitContext extends LimitContext {

        public QuantityContext quantity() {
            return getRuleContext(QuantityContext.class, 0);
        }

        public RetentionContext retention() {
            return getRuleContext(RetentionContext.class, 0);
        }

        public QuantityRetentionLimitContext(LimitContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterQuantityRetentionLimit(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitQuantityRetentionLimit(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitQuantityRetentionLimit(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class QuantityLimitContext extends LimitContext {

        public QuantityContext quantity() {
            return getRuleContext(QuantityContext.class, 0);
        }

        public QuantityLimitContext(LimitContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterQuantityLimit(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitQuantityLimit(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitQuantityLimit(this);
            else return visitor.visitChildren(this);
        }
    }

    public final LimitContext limit() throws RecognitionException {
        LimitContext _localctx = new LimitContext(_ctx, getState());
        enterRule(_localctx, 52, RULE_limit);
        try {
            setState(561);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 71, _ctx)) {
                case 1:
                    _localctx = new QuantityRetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(556);
                    quantity();
                    setState(557);
                    retention();
                }
                    break;
                case 2:
                    _localctx = new RetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(559);
                    retention();
                }
                    break;
                case 3:
                    _localctx = new QuantityLimitContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(560);
                    quantity();
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class QuantityContext extends ParserRuleContext {

        public NumberContext number() {
            return getRuleContext(NumberContext.class, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public TerminalNode EACH() {
            return getToken(SFMLParser.EACH, 0);
        }

        public QuantityContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_quantity;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterQuantity(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitQuantity(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitQuantity(this);
            else return visitor.visitChildren(this);
        }
    }

    public final QuantityContext quantity() throws RecognitionException {
        QuantityContext _localctx = new QuantityContext(_ctx, getState());
        enterRule(_localctx, 54, RULE_quantity);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(568);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(563);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(564);
                        match(LPAREN);
                        setState(565);
                        dataExpression();
                        setState(566);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(571);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 73, _ctx)) {
                    case 1: {
                        setState(570);
                        match(EACH);
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class RetentionContext extends ParserRuleContext {

        public TerminalNode RETAIN() {
            return getToken(SFMLParser.RETAIN, 0);
        }

        public NumberContext number() {
            return getRuleContext(NumberContext.class, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public TerminalNode EACH() {
            return getToken(SFMLParser.EACH, 0);
        }

        public RetentionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_retention;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterRetention(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitRetention(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitRetention(this);
            else return visitor.visitChildren(this);
        }
    }

    public final RetentionContext retention() throws RecognitionException {
        RetentionContext _localctx = new RetentionContext(_ctx, getState());
        enterRule(_localctx, 56, RULE_retention);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(573);
                match(RETAIN);
                setState(579);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(574);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(575);
                        match(LPAREN);
                        setState(576);
                        dataExpression();
                        setState(577);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(582);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 75, _ctx)) {
                    case 1: {
                        setState(581);
                        match(EACH);
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ResourceExclusionContext extends ParserRuleContext {

        public TerminalNode EXCEPT() {
            return getToken(SFMLParser.EXCEPT, 0);
        }

        public ResourceIdListContext resourceIdList() {
            return getRuleContext(ResourceIdListContext.class, 0);
        }

        public ResourceExclusionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_resourceExclusion;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterResourceExclusion(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitResourceExclusion(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitResourceExclusion(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ResourceExclusionContext resourceExclusion() throws RecognitionException {
        ResourceExclusionContext _localctx = new ResourceExclusionContext(_ctx, getState());
        enterRule(_localctx, 58, RULE_resourceExclusion);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(584);
                match(EXCEPT);
                setState(585);
                resourceIdList();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ResourceIdContext extends ParserRuleContext {

        public ResourceIdContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_resourceId;
        }

        public ResourceIdContext() {}

        public void copyFrom(ResourceIdContext ctx) {
            super.copyFrom(ctx);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class StringResourceContext extends ResourceIdContext {

        public StringContext string() {
            return getRuleContext(StringContext.class, 0);
        }

        public StringResourceContext(ResourceIdContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterStringResource(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitStringResource(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitStringResource(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ResourceContext extends ResourceIdContext {

        public List<IdentifierContext> identifier() {
            return getRuleContexts(IdentifierContext.class);
        }

        public IdentifierContext identifier(int i) {
            return getRuleContext(IdentifierContext.class, i);
        }

        public List<TerminalNode> COLON() {
            return getTokens(SFMLParser.COLON);
        }

        public TerminalNode COLON(int i) {
            return getToken(SFMLParser.COLON, i);
        }

        public ResourceContext(ResourceIdContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterResource(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitResource(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitResource(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ResourceIdContext resourceId() throws RecognitionException {
        ResourceIdContext _localctx = new ResourceIdContext(_ctx, getState());
        enterRule(_localctx, 60, RULE_resourceId);
        try {
            setState(607);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case IMPORT:
                case REVISION:
                case DIGEST:
                case SETCIRCUIT:
                case WAIT:
                case TRANSFER:
                case TRANSFERF:
                case INTO:
                case FUNCTION:
                case RETURNS:
                case RETURN:
                case LET:
                case SET:
                case STATE:
                case RISING:
                case FALLING:
                case PRINT:
                case FOR:
                case WHILE:
                case STEP:
                case TOP:
                case BOTTOM:
                case LEFT:
                case RIGHT:
                case FRONT:
                case BACK:
                case SECONDS:
                case SECOND:
                case GLOBAL:
                case REDSTONE:
                case LEVEL:
                case LEVELS:
                case IDENTIFIER:
                    _localctx = new ResourceContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    {
                        setState(587);
                        identifier();
                    }
                    setState(604);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 81, _ctx)) {
                        case 1: {
                            setState(588);
                            match(COLON);
                            setState(590);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 76, _ctx)) {
                                case 1: {
                                    setState(589);
                                    identifier();
                                }
                                    break;
                            }
                            setState(602);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 80, _ctx)) {
                                case 1: {
                                    setState(592);
                                    match(COLON);
                                    setState(594);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 77, _ctx)) {
                                        case 1: {
                                            setState(593);
                                            identifier();
                                        }
                                            break;
                                    }
                                    setState(600);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 79, _ctx)) {
                                        case 1: {
                                            setState(596);
                                            match(COLON);
                                            setState(598);
                                            _errHandler.sync(this);
                                            switch (getInterpreter().adaptivePredict(_input, 78, _ctx)) {
                                                case 1: {
                                                    setState(597);
                                                    identifier();
                                                }
                                                    break;
                                            }
                                        }
                                            break;
                                    }
                                }
                                    break;
                            }
                        }
                            break;
                    }
                }
                    break;
                case STRING:
                    _localctx = new StringResourceContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(606);
                    string();
                }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ResourceIdListContext extends ParserRuleContext {

        public List<ResourceIdContext> resourceId() {
            return getRuleContexts(ResourceIdContext.class);
        }

        public ResourceIdContext resourceId(int i) {
            return getRuleContext(ResourceIdContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public ResourceIdListContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_resourceIdList;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterResourceIdList(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitResourceIdList(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitResourceIdList(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ResourceIdListContext resourceIdList() throws RecognitionException {
        ResourceIdListContext _localctx = new ResourceIdListContext(_ctx, getState());
        enterRule(_localctx, 62, RULE_resourceIdList);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(609);
                resourceId();
                setState(614);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 83, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(610);
                                match(COMMA);
                                setState(611);
                                resourceId();
                            }
                        }
                    }
                    setState(616);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 83, _ctx);
                }
                setState(618);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 84, _ctx)) {
                    case 1: {
                        setState(617);
                        match(COMMA);
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ResourceIdDisjunctionContext extends ParserRuleContext {

        public List<ResourceIdContext> resourceId() {
            return getRuleContexts(ResourceIdContext.class);
        }

        public ResourceIdContext resourceId(int i) {
            return getRuleContext(ResourceIdContext.class, i);
        }

        public List<TerminalNode> OR() {
            return getTokens(SFMLParser.OR);
        }

        public TerminalNode OR(int i) {
            return getToken(SFMLParser.OR, i);
        }

        public ResourceIdDisjunctionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_resourceIdDisjunction;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterResourceIdDisjunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitResourceIdDisjunction(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitResourceIdDisjunction(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ResourceIdDisjunctionContext resourceIdDisjunction() throws RecognitionException {
        ResourceIdDisjunctionContext _localctx = new ResourceIdDisjunctionContext(_ctx, getState());
        enterRule(_localctx, 64, RULE_resourceIdDisjunction);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(620);
                resourceId();
                setState(625);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 85, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(621);
                                match(OR);
                                setState(622);
                                resourceId();
                            }
                        }
                    }
                    setState(627);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 85, _ctx);
                }
                setState(629);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 86, _ctx)) {
                    case 1: {
                        setState(628);
                        match(OR);
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class WithContext extends ParserRuleContext {

        public TerminalNode WITH() {
            return getToken(SFMLParser.WITH, 0);
        }

        public WithClauseContext withClause() {
            return getRuleContext(WithClauseContext.class, 0);
        }

        public TerminalNode WITHOUT() {
            return getToken(SFMLParser.WITHOUT, 0);
        }

        public WithContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_with;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterWith(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitWith(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitWith(this);
            else return visitor.visitChildren(this);
        }
    }

    public final WithContext with() throws RecognitionException {
        WithContext _localctx = new WithContext(_ctx, getState());
        enterRule(_localctx, 66, RULE_with);
        try {
            setState(635);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case WITH:
                    enterOuterAlt(_localctx, 1); {
                    setState(631);
                    match(WITH);
                    setState(632);
                    withClause(0);
                }
                    break;
                case WITHOUT:
                    enterOuterAlt(_localctx, 2); {
                    setState(633);
                    match(WITHOUT);
                    setState(634);
                    withClause(0);
                }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class WithClauseContext extends ParserRuleContext {

        public WithClauseContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_withClause;
        }

        public WithClauseContext() {}

        public void copyFrom(WithClauseContext ctx) {
            super.copyFrom(ctx);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class WithConjunctionContext extends WithClauseContext {

        public List<WithClauseContext> withClause() {
            return getRuleContexts(WithClauseContext.class);
        }

        public WithClauseContext withClause(int i) {
            return getRuleContext(WithClauseContext.class, i);
        }

        public TerminalNode AND() {
            return getToken(SFMLParser.AND, 0);
        }

        public WithConjunctionContext(WithClauseContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterWithConjunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitWithConjunction(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitWithConjunction(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class WithParenContext extends WithClauseContext {

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public WithClauseContext withClause() {
            return getRuleContext(WithClauseContext.class, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public WithParenContext(WithClauseContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterWithParen(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitWithParen(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitWithParen(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class WithNegationContext extends WithClauseContext {

        public TerminalNode NOT() {
            return getToken(SFMLParser.NOT, 0);
        }

        public WithClauseContext withClause() {
            return getRuleContext(WithClauseContext.class, 0);
        }

        public WithNegationContext(WithClauseContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterWithNegation(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitWithNegation(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitWithNegation(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class WithTagContext extends WithClauseContext {

        public TagMatcherContext tagMatcher() {
            return getRuleContext(TagMatcherContext.class, 0);
        }

        public TerminalNode TAG() {
            return getToken(SFMLParser.TAG, 0);
        }

        public TerminalNode HASHTAG() {
            return getToken(SFMLParser.HASHTAG, 0);
        }

        public WithTagContext(WithClauseContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterWithTag(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitWithTag(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitWithTag(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class WithDisjunctionContext extends WithClauseContext {

        public List<WithClauseContext> withClause() {
            return getRuleContexts(WithClauseContext.class);
        }

        public WithClauseContext withClause(int i) {
            return getRuleContext(WithClauseContext.class, i);
        }

        public TerminalNode OR() {
            return getToken(SFMLParser.OR, 0);
        }

        public WithDisjunctionContext(WithClauseContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterWithDisjunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitWithDisjunction(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitWithDisjunction(this);
            else return visitor.visitChildren(this);
        }
    }

    public final WithClauseContext withClause() throws RecognitionException {
        return withClause(0);
    }

    private WithClauseContext withClause(int _p) throws RecognitionException {
        ParserRuleContext _parentctx = _ctx;
        int _parentState = getState();
        WithClauseContext _localctx = new WithClauseContext(_ctx, _parentState);
        WithClauseContext _prevctx = _localctx;
        int _startState = 68;
        enterRecursionRule(_localctx, 68, RULE_withClause, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(652);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case LPAREN: {
                        _localctx = new WithParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(638);
                        match(LPAREN);
                        setState(639);
                        withClause(0);
                        setState(640);
                        match(RPAREN);
                    }
                        break;
                    case NOT: {
                        _localctx = new WithNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(642);
                        match(NOT);
                        setState(643);
                        withClause(4);
                    }
                        break;
                    case TAG:
                    case HASHTAG: {
                        _localctx = new WithTagContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(649);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case TAG: {
                                setState(644);
                                match(TAG);
                                setState(646);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                if (_la == HASHTAG) {
                                    {
                                        setState(645);
                                        match(HASHTAG);
                                    }
                                }

                            }
                                break;
                            case HASHTAG: {
                                setState(648);
                                match(HASHTAG);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(651);
                        tagMatcher();
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(662);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 92, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(660);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 91, _ctx)) {
                                case 1: {
                                    _localctx = new WithConjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(654);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(655);
                                    match(AND);
                                    setState(656);
                                    withClause(4);
                                }
                                    break;
                                case 2: {
                                    _localctx = new WithDisjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(657);
                                    if (!(precpred(_ctx, 2)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                                    setState(658);
                                    match(OR);
                                    setState(659);
                                    withClause(3);
                                }
                                    break;
                            }
                        }
                    }
                    setState(664);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 92, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            unrollRecursionContexts(_parentctx);
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class TagMatcherContext extends ParserRuleContext {

        public List<IdentifierContext> identifier() {
            return getRuleContexts(IdentifierContext.class);
        }

        public IdentifierContext identifier(int i) {
            return getRuleContext(IdentifierContext.class, i);
        }

        public TerminalNode COLON() {
            return getToken(SFMLParser.COLON, 0);
        }

        public List<TerminalNode> SLASH() {
            return getTokens(SFMLParser.SLASH);
        }

        public TerminalNode SLASH(int i) {
            return getToken(SFMLParser.SLASH, i);
        }

        public TagMatcherContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_tagMatcher;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterTagMatcher(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitTagMatcher(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitTagMatcher(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TagMatcherContext tagMatcher() throws RecognitionException {
        TagMatcherContext _localctx = new TagMatcherContext(_ctx, getState());
        enterRule(_localctx, 70, RULE_tagMatcher);
        try {
            int _alt;
            setState(683);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 95, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(665);
                    identifier();
                    setState(666);
                    match(COLON);
                    setState(667);
                    identifier();
                    setState(672);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 93, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(668);
                                    match(SLASH);
                                    setState(669);
                                    identifier();
                                }
                            }
                        }
                        setState(674);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 93, _ctx);
                    }
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(675);
                    identifier();
                    setState(680);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 94, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(676);
                                    match(SLASH);
                                    setState(677);
                                    identifier();
                                }
                            }
                        }
                        setState(682);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 94, _ctx);
                    }
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class SidequalifierContext extends ParserRuleContext {

        public SidequalifierContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_sidequalifier;
        }

        public SidequalifierContext() {}

        public void copyFrom(SidequalifierContext ctx) {
            super.copyFrom(ctx);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class EachSideContext extends SidequalifierContext {

        public TerminalNode EACH() {
            return getToken(SFMLParser.EACH, 0);
        }

        public TerminalNode SIDE() {
            return getToken(SFMLParser.SIDE, 0);
        }

        public EachSideContext(SidequalifierContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterEachSide(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitEachSide(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitEachSide(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ListedSidesContext extends SidequalifierContext {

        public List<SideContext> side() {
            return getRuleContexts(SideContext.class);
        }

        public SideContext side(int i) {
            return getRuleContext(SideContext.class, i);
        }

        public TerminalNode SIDE() {
            return getToken(SFMLParser.SIDE, 0);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public ListedSidesContext(SidequalifierContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterListedSides(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitListedSides(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitListedSides(this);
            else return visitor.visitChildren(this);
        }
    }

    public final SidequalifierContext sidequalifier() throws RecognitionException {
        SidequalifierContext _localctx = new SidequalifierContext(_ctx, getState());
        enterRule(_localctx, 72, RULE_sidequalifier);
        int _la;
        try {
            setState(697);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case EACH:
                    _localctx = new EachSideContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(685);
                    match(EACH);
                    setState(686);
                    match(SIDE);
                }
                    break;
                case TOP:
                case BOTTOM:
                case NORTH:
                case EAST:
                case SOUTH:
                case WEST:
                case LEFT:
                case RIGHT:
                case FRONT:
                case BACK:
                case NULL:
                    _localctx = new ListedSidesContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(687);
                    side();
                    setState(692);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(688);
                                match(COMMA);
                                setState(689);
                                side();
                            }
                        }
                        setState(694);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(695);
                    match(SIDE);
                }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class SideContext extends ParserRuleContext {

        public TerminalNode TOP() {
            return getToken(SFMLParser.TOP, 0);
        }

        public TerminalNode BOTTOM() {
            return getToken(SFMLParser.BOTTOM, 0);
        }

        public TerminalNode NORTH() {
            return getToken(SFMLParser.NORTH, 0);
        }

        public TerminalNode EAST() {
            return getToken(SFMLParser.EAST, 0);
        }

        public TerminalNode SOUTH() {
            return getToken(SFMLParser.SOUTH, 0);
        }

        public TerminalNode WEST() {
            return getToken(SFMLParser.WEST, 0);
        }

        public TerminalNode LEFT() {
            return getToken(SFMLParser.LEFT, 0);
        }

        public TerminalNode RIGHT() {
            return getToken(SFMLParser.RIGHT, 0);
        }

        public TerminalNode FRONT() {
            return getToken(SFMLParser.FRONT, 0);
        }

        public TerminalNode BACK() {
            return getToken(SFMLParser.BACK, 0);
        }

        public TerminalNode NULL() {
            return getToken(SFMLParser.NULL, 0);
        }

        public SideContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_side;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterSide(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitSide(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitSide(this);
            else return visitor.visitChildren(this);
        }
    }

    public final SideContext side() throws RecognitionException {
        SideContext _localctx = new SideContext(_ctx, getState());
        enterRule(_localctx, 74, RULE_side);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(699);
                _la = _input.LA(1);
                if (!(((((_la - 84)) & ~0x3f) == 0 && ((1L << (_la - 84)) & 4031L) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class SlotqualifierContext extends ParserRuleContext {

        public RangesetContext rangeset() {
            return getRuleContext(RangesetContext.class, 0);
        }

        public TerminalNode SLOTS() {
            return getToken(SFMLParser.SLOTS, 0);
        }

        public TerminalNode SLOT() {
            return getToken(SFMLParser.SLOT, 0);
        }

        public SlotqualifierContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_slotqualifier;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterSlotqualifier(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitSlotqualifier(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitSlotqualifier(this);
            else return visitor.visitChildren(this);
        }
    }

    public final SlotqualifierContext slotqualifier() throws RecognitionException {
        SlotqualifierContext _localctx = new SlotqualifierContext(_ctx, getState());
        enterRule(_localctx, 76, RULE_slotqualifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(701);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(702);
                rangeset();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class RangesetContext extends ParserRuleContext {

        public List<RangeContext> range() {
            return getRuleContexts(RangeContext.class);
        }

        public RangeContext range(int i) {
            return getRuleContext(RangeContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public RangesetContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_rangeset;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterRangeset(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitRangeset(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitRangeset(this);
            else return visitor.visitChildren(this);
        }
    }

    public final RangesetContext rangeset() throws RecognitionException {
        RangesetContext _localctx = new RangesetContext(_ctx, getState());
        enterRule(_localctx, 78, RULE_rangeset);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(704);
                range();
                setState(709);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(705);
                            match(COMMA);
                            setState(706);
                            range();
                        }
                    }
                    setState(711);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class RangeContext extends ParserRuleContext {

        public List<NumberContext> number() {
            return getRuleContexts(NumberContext.class);
        }

        public NumberContext number(int i) {
            return getRuleContext(NumberContext.class, i);
        }

        public TerminalNode DASH() {
            return getToken(SFMLParser.DASH, 0);
        }

        public RangeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_range;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterRange(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitRange(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitRange(this);
            else return visitor.visitChildren(this);
        }
    }

    public final RangeContext range() throws RecognitionException {
        RangeContext _localctx = new RangeContext(_ctx, getState());
        enterRule(_localctx, 80, RULE_range);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(712);
                number();
                setState(715);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == DASH) {
                    {
                        setState(713);
                        match(DASH);
                        setState(714);
                        number();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class IfStatementContext extends ParserRuleContext {

        public List<TerminalNode> IF() {
            return getTokens(SFMLParser.IF);
        }

        public TerminalNode IF(int i) {
            return getToken(SFMLParser.IF, i);
        }

        public List<BoolexprContext> boolexpr() {
            return getRuleContexts(BoolexprContext.class);
        }

        public BoolexprContext boolexpr(int i) {
            return getRuleContext(BoolexprContext.class, i);
        }

        public List<TerminalNode> THEN() {
            return getTokens(SFMLParser.THEN);
        }

        public TerminalNode THEN(int i) {
            return getToken(SFMLParser.THEN, i);
        }

        public List<BlockContext> block() {
            return getRuleContexts(BlockContext.class);
        }

        public BlockContext block(int i) {
            return getRuleContext(BlockContext.class, i);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public List<TerminalNode> ELSE() {
            return getTokens(SFMLParser.ELSE);
        }

        public TerminalNode ELSE(int i) {
            return getToken(SFMLParser.ELSE, i);
        }

        public List<TerminalNode> LBRACE() {
            return getTokens(SFMLParser.LBRACE);
        }

        public TerminalNode LBRACE(int i) {
            return getToken(SFMLParser.LBRACE, i);
        }

        public List<TerminalNode> RBRACE() {
            return getTokens(SFMLParser.RBRACE);
        }

        public TerminalNode RBRACE(int i) {
            return getToken(SFMLParser.RBRACE, i);
        }

        public IfStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_ifStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterIfStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitIfStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitIfStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final IfStatementContext ifStatement() throws RecognitionException {
        IfStatementContext _localctx = new IfStatementContext(_ctx, getState());
        enterRule(_localctx, 82, RULE_ifStatement);
        int _la;
        try {
            int _alt;
            setState(762);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 104, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(717);
                    match(IF);
                    setState(718);
                    boolexpr(0);
                    setState(719);
                    match(THEN);
                    setState(720);
                    block();
                    setState(729);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 100, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(721);
                                    match(ELSE);
                                    setState(722);
                                    match(IF);
                                    setState(723);
                                    boolexpr(0);
                                    setState(724);
                                    match(THEN);
                                    setState(725);
                                    block();
                                }
                            }
                        }
                        setState(731);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 100, _ctx);
                    }
                    setState(734);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == ELSE) {
                        {
                            setState(732);
                            match(ELSE);
                            setState(733);
                            block();
                        }
                    }

                    setState(736);
                    match(END);
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(738);
                    match(IF);
                    setState(739);
                    boolexpr(0);
                    setState(740);
                    match(LBRACE);
                    setState(741);
                    block();
                    setState(742);
                    match(RBRACE);
                    setState(752);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 102, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(743);
                                    match(ELSE);
                                    setState(744);
                                    match(IF);
                                    setState(745);
                                    boolexpr(0);
                                    setState(746);
                                    match(LBRACE);
                                    setState(747);
                                    block();
                                    setState(748);
                                    match(RBRACE);
                                }
                            }
                        }
                        setState(754);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 102, _ctx);
                    }
                    setState(760);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 103, _ctx)) {
                        case 1: {
                            setState(755);
                            match(ELSE);
                            setState(756);
                            match(LBRACE);
                            setState(757);
                            block();
                            setState(758);
                            match(RBRACE);
                        }
                            break;
                    }
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ForStatementContext extends ParserRuleContext {

        public ForStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_forStatement;
        }

        public ForStatementContext() {}

        public void copyFrom(ForStatementContext ctx) {
            super.copyFrom(ctx);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class CountedForContext extends ForStatementContext {

        public TerminalNode FOR() {
            return getToken(SFMLParser.FOR, 0);
        }

        public VariableNameContext variableName() {
            return getRuleContext(VariableNameContext.class, 0);
        }

        public TerminalNode EQ_SYMBOL() {
            return getToken(SFMLParser.EQ_SYMBOL, 0);
        }

        public List<DataExpressionContext> dataExpression() {
            return getRuleContexts(DataExpressionContext.class);
        }

        public DataExpressionContext dataExpression(int i) {
            return getRuleContext(DataExpressionContext.class, i);
        }

        public TerminalNode TO() {
            return getToken(SFMLParser.TO, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public TerminalNode STEP() {
            return getToken(SFMLParser.STEP, 0);
        }

        public CountedForContext(ForStatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterCountedFor(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitCountedFor(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitCountedFor(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class IndexedArrayForContext extends ForStatementContext {

        public TerminalNode FOR() {
            return getToken(SFMLParser.FOR, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public List<VariableNameContext> variableName() {
            return getRuleContexts(VariableNameContext.class);
        }

        public VariableNameContext variableName(int i) {
            return getRuleContext(VariableNameContext.class, i);
        }

        public TerminalNode COMMA() {
            return getToken(SFMLParser.COMMA, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public TerminalNode IN() {
            return getToken(SFMLParser.IN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public IndexedArrayForContext(ForStatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterIndexedArrayFor(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitIndexedArrayFor(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitIndexedArrayFor(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class JavaForContext extends ForStatementContext {

        public TerminalNode FOR() {
            return getToken(SFMLParser.FOR, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public List<TerminalNode> SEMICOLON() {
            return getTokens(SFMLParser.SEMICOLON);
        }

        public TerminalNode SEMICOLON(int i) {
            return getToken(SFMLParser.SEMICOLON, i);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public ForInitializerContext forInitializer() {
            return getRuleContext(ForInitializerContext.class, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public ForUpdateContext forUpdate() {
            return getRuleContext(ForUpdateContext.class, 0);
        }

        public JavaForContext(ForStatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterJavaFor(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitJavaFor(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitJavaFor(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ArrayForContext extends ForStatementContext {

        public TerminalNode FOR() {
            return getToken(SFMLParser.FOR, 0);
        }

        public VariableNameContext variableName() {
            return getRuleContext(VariableNameContext.class, 0);
        }

        public TerminalNode IN() {
            return getToken(SFMLParser.IN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public ArrayForContext(ForStatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterArrayFor(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitArrayFor(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitArrayFor(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ForStatementContext forStatement() throws RecognitionException {
        ForStatementContext _localctx = new ForStatementContext(_ctx, getState());
        enterRule(_localctx, 84, RULE_forStatement);
        int _la;
        try {
            setState(840);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 113, _ctx)) {
                case 1:
                    _localctx = new CountedForContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(764);
                    match(FOR);
                    setState(765);
                    variableName();
                    setState(766);
                    match(EQ_SYMBOL);
                    setState(767);
                    dataExpression();
                    setState(768);
                    match(TO);
                    setState(769);
                    dataExpression();
                    setState(772);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == STEP) {
                        {
                            setState(770);
                            match(STEP);
                            setState(771);
                            dataExpression();
                        }
                    }

                    setState(782);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(774);
                            match(DO);
                            setState(775);
                            block();
                            setState(776);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(778);
                            match(LBRACE);
                            setState(779);
                            block();
                            setState(780);
                            match(RBRACE);
                        }
                            break;
                        default:
                            throw new NoViableAltException(this);
                    }
                }
                    break;
                case 2:
                    _localctx = new ArrayForContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(784);
                    match(FOR);
                    setState(785);
                    variableName();
                    setState(786);
                    match(IN);
                    setState(787);
                    dataExpression();
                    setState(796);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(788);
                            match(DO);
                            setState(789);
                            block();
                            setState(790);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(792);
                            match(LBRACE);
                            setState(793);
                            block();
                            setState(794);
                            match(RBRACE);
                        }
                            break;
                        default:
                            throw new NoViableAltException(this);
                    }
                }
                    break;
                case 3:
                    _localctx = new JavaForContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(798);
                    match(FOR);
                    setState(799);
                    match(LPAREN);
                    setState(801);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764477148860418L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 18084767253643263L) != 0)) {
                        {
                            setState(800);
                            forInitializer();
                        }
                    }

                    setState(803);
                    match(SEMICOLON);
                    setState(805);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764468516982786L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 64809613387612159L) != 0)) {
                        {
                            setState(804);
                            dataExpression();
                        }
                    }

                    setState(807);
                    match(SEMICOLON);
                    setState(809);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764477148860418L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 18084767253643263L) != 0)) {
                        {
                            setState(808);
                            forUpdate();
                        }
                    }

                    setState(811);
                    match(RPAREN);
                    setState(820);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(812);
                            match(DO);
                            setState(813);
                            block();
                            setState(814);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(816);
                            match(LBRACE);
                            setState(817);
                            block();
                            setState(818);
                            match(RBRACE);
                        }
                            break;
                        default:
                            throw new NoViableAltException(this);
                    }
                }
                    break;
                case 4:
                    _localctx = new IndexedArrayForContext(_localctx);
                    enterOuterAlt(_localctx, 4); {
                    setState(822);
                    match(FOR);
                    setState(823);
                    match(LPAREN);
                    setState(824);
                    variableName();
                    setState(825);
                    match(COMMA);
                    setState(826);
                    variableName();
                    setState(827);
                    match(RPAREN);
                    setState(828);
                    match(IN);
                    setState(829);
                    dataExpression();
                    setState(838);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(830);
                            match(DO);
                            setState(831);
                            block();
                            setState(832);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(834);
                            match(LBRACE);
                            setState(835);
                            block();
                            setState(836);
                            match(RBRACE);
                        }
                            break;
                        default:
                            throw new NoViableAltException(this);
                    }
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ForInitializerContext extends ParserRuleContext {

        public DataDeclarationContext dataDeclaration() {
            return getRuleContext(DataDeclarationContext.class, 0);
        }

        public DataAssignmentContext dataAssignment() {
            return getRuleContext(DataAssignmentContext.class, 0);
        }

        public FunctionCallStatementContext functionCallStatement() {
            return getRuleContext(FunctionCallStatementContext.class, 0);
        }

        public ForInitializerContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_forInitializer;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterForInitializer(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitForInitializer(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitForInitializer(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ForInitializerContext forInitializer() throws RecognitionException {
        ForInitializerContext _localctx = new ForInitializerContext(_ctx, getState());
        enterRule(_localctx, 86, RULE_forInitializer);
        try {
            setState(845);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 114, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(842);
                    dataDeclaration();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(843);
                    dataAssignment();
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(844);
                    functionCallStatement();
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ForUpdateContext extends ParserRuleContext {

        public DataAssignmentContext dataAssignment() {
            return getRuleContext(DataAssignmentContext.class, 0);
        }

        public FunctionCallStatementContext functionCallStatement() {
            return getRuleContext(FunctionCallStatementContext.class, 0);
        }

        public ForUpdateContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_forUpdate;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterForUpdate(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitForUpdate(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitForUpdate(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ForUpdateContext forUpdate() throws RecognitionException {
        ForUpdateContext _localctx = new ForUpdateContext(_ctx, getState());
        enterRule(_localctx, 88, RULE_forUpdate);
        try {
            setState(849);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 115, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(847);
                    dataAssignment();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(848);
                    functionCallStatement();
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class WhileStatementContext extends ParserRuleContext {

        public TerminalNode WHILE() {
            return getToken(SFMLParser.WHILE, 0);
        }

        public BoolexprContext boolexpr() {
            return getRuleContext(BoolexprContext.class, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public WhileStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_whileStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterWhileStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitWhileStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitWhileStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final WhileStatementContext whileStatement() throws RecognitionException {
        WhileStatementContext _localctx = new WhileStatementContext(_ctx, getState());
        enterRule(_localctx, 90, RULE_whileStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(851);
                match(WHILE);
                setState(852);
                boolexpr(0);
                setState(861);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case DO: {
                        setState(853);
                        match(DO);
                        setState(854);
                        block();
                        setState(855);
                        match(END);
                    }
                        break;
                    case LBRACE: {
                        setState(857);
                        match(LBRACE);
                        setState(858);
                        block();
                        setState(859);
                        match(RBRACE);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BoolexprContext extends ParserRuleContext {

        public BoolexprContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_boolexpr;
        }

        public BoolexprContext() {}

        public void copyFrom(BoolexprContext ctx) {
            super.copyFrom(ctx);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanHasContext extends BoolexprContext {

        public LabelAccessContext labelAccess() {
            return getRuleContext(LabelAccessContext.class, 0);
        }

        public TerminalNode HAS() {
            return getToken(SFMLParser.HAS, 0);
        }

        public ComparisonOpContext comparisonOp() {
            return getRuleContext(ComparisonOpContext.class, 0);
        }

        public NumberContext number() {
            return getRuleContext(NumberContext.class, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public SetOpContext setOp() {
            return getRuleContext(SetOpContext.class, 0);
        }

        public ResourceIdDisjunctionContext resourceIdDisjunction() {
            return getRuleContext(ResourceIdDisjunctionContext.class, 0);
        }

        public WithContext with() {
            return getRuleContext(WithContext.class, 0);
        }

        public TerminalNode EXCEPT() {
            return getToken(SFMLParser.EXCEPT, 0);
        }

        public ResourceIdListContext resourceIdList() {
            return getRuleContext(ResourceIdListContext.class, 0);
        }

        public BooleanHasContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanHas(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanHas(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitBooleanHas(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanDataContext extends BoolexprContext {

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public BooleanDataContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanData(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanData(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitBooleanData(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanConjunctionContext extends BoolexprContext {

        public List<BoolexprContext> boolexpr() {
            return getRuleContexts(BoolexprContext.class);
        }

        public BoolexprContext boolexpr(int i) {
            return getRuleContext(BoolexprContext.class, i);
        }

        public TerminalNode AND() {
            return getToken(SFMLParser.AND, 0);
        }

        public BooleanConjunctionContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanConjunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanConjunction(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitBooleanConjunction(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanRedstoneContext extends BoolexprContext {

        public TerminalNode REDSTONE() {
            return getToken(SFMLParser.REDSTONE, 0);
        }

        public ComparisonOpContext comparisonOp() {
            return getRuleContext(ComparisonOpContext.class, 0);
        }

        public TerminalNode LEVEL() {
            return getToken(SFMLParser.LEVEL, 0);
        }

        public TerminalNode LEVELS() {
            return getToken(SFMLParser.LEVELS, 0);
        }

        public NumberContext number() {
            return getRuleContext(NumberContext.class, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public BooleanRedstoneContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanRedstone(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanRedstone(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitBooleanRedstone(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanDisjunctionContext extends BoolexprContext {

        public List<BoolexprContext> boolexpr() {
            return getRuleContexts(BoolexprContext.class);
        }

        public BoolexprContext boolexpr(int i) {
            return getRuleContext(BoolexprContext.class, i);
        }

        public TerminalNode OR() {
            return getToken(SFMLParser.OR, 0);
        }

        public BooleanDisjunctionContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanDisjunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanDisjunction(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitBooleanDisjunction(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanFalseContext extends BoolexprContext {

        public TerminalNode FALSE() {
            return getToken(SFMLParser.FALSE, 0);
        }

        public BooleanFalseContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanFalse(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanFalse(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitBooleanFalse(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanParenContext extends BoolexprContext {

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public BoolexprContext boolexpr() {
            return getRuleContext(BoolexprContext.class, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public BooleanParenContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanParen(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanParen(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitBooleanParen(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanNegationContext extends BoolexprContext {

        public TerminalNode NOT() {
            return getToken(SFMLParser.NOT, 0);
        }

        public BoolexprContext boolexpr() {
            return getRuleContext(BoolexprContext.class, 0);
        }

        public BooleanNegationContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanNegation(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanNegation(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitBooleanNegation(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanTrueContext extends BoolexprContext {

        public TerminalNode TRUE() {
            return getToken(SFMLParser.TRUE, 0);
        }

        public BooleanTrueContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanTrue(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanTrue(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitBooleanTrue(this);
            else return visitor.visitChildren(this);
        }
    }

    public final BoolexprContext boolexpr() throws RecognitionException {
        return boolexpr(0);
    }

    private BoolexprContext boolexpr(int _p) throws RecognitionException {
        ParserRuleContext _parentctx = _ctx;
        int _parentState = getState();
        BoolexprContext _localctx = new BoolexprContext(_ctx, _parentState);
        BoolexprContext _prevctx = _localctx;
        int _startState = 92;
        enterRecursionRule(_localctx, 92, RULE_boolexpr, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(913);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 125, _ctx)) {
                    case 1: {
                        _localctx = new BooleanTrueContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(864);
                        match(TRUE);
                    }
                        break;
                    case 2: {
                        _localctx = new BooleanFalseContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(865);
                        match(FALSE);
                    }
                        break;
                    case 3: {
                        _localctx = new BooleanParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(866);
                        match(LPAREN);
                        setState(867);
                        boolexpr(0);
                        setState(868);
                        match(RPAREN);
                    }
                        break;
                    case 4: {
                        _localctx = new BooleanDataContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(870);
                        match(LPAREN);
                        setState(871);
                        dataExpression();
                        setState(872);
                        match(RPAREN);
                    }
                        break;
                    case 5: {
                        _localctx = new BooleanNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(874);
                        match(NOT);
                        setState(875);
                        boolexpr(5);
                    }
                        break;
                    case 6: {
                        _localctx = new BooleanHasContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(877);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 117, _ctx)) {
                            case 1: {
                                setState(876);
                                setOp();
                            }
                                break;
                        }
                        setState(879);
                        labelAccess();
                        setState(880);
                        match(HAS);
                        setState(881);
                        comparisonOp();
                        setState(887);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case NUMBER: {
                                setState(882);
                                number();
                            }
                                break;
                            case LPAREN: {
                                setState(883);
                                match(LPAREN);
                                setState(884);
                                dataExpression();
                                setState(885);
                                match(RPAREN);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(890);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 119, _ctx)) {
                            case 1: {
                                setState(889);
                                resourceIdDisjunction();
                            }
                                break;
                        }
                        setState(893);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 120, _ctx)) {
                            case 1: {
                                setState(892);
                                with();
                            }
                                break;
                        }
                        setState(897);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 121, _ctx)) {
                            case 1: {
                                setState(895);
                                match(EXCEPT);
                                setState(896);
                                resourceIdList();
                            }
                                break;
                        }
                    }
                        break;
                    case 7: {
                        _localctx = new BooleanRedstoneContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(899);
                        match(REDSTONE);
                        setState(901);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 122, _ctx)) {
                            case 1: {
                                setState(900);
                                _la = _input.LA(1);
                                if (!(_la == LEVEL || _la == LEVELS)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                            }
                                break;
                        }
                        setState(911);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 124, _ctx)) {
                            case 1: {
                                setState(903);
                                comparisonOp();
                                setState(909);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case NUMBER: {
                                        setState(904);
                                        number();
                                    }
                                        break;
                                    case LPAREN: {
                                        setState(905);
                                        match(LPAREN);
                                        setState(906);
                                        dataExpression();
                                        setState(907);
                                        match(RPAREN);
                                    }
                                        break;
                                    default:
                                        throw new NoViableAltException(this);
                                }
                            }
                                break;
                        }
                    }
                        break;
                }
                _ctx.stop = _input.LT(-1);
                setState(923);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 127, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(921);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 126, _ctx)) {
                                case 1: {
                                    _localctx = new BooleanConjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(915);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(916);
                                    match(AND);
                                    setState(917);
                                    boolexpr(5);
                                }
                                    break;
                                case 2: {
                                    _localctx = new BooleanDisjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(918);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(919);
                                    match(OR);
                                    setState(920);
                                    boolexpr(4);
                                }
                                    break;
                            }
                        }
                    }
                    setState(925);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 127, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            unrollRecursionContexts(_parentctx);
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class ComparisonOpContext extends ParserRuleContext {

        public TerminalNode GT() {
            return getToken(SFMLParser.GT, 0);
        }

        public TerminalNode NE_SYMBOL() {
            return getToken(SFMLParser.NE_SYMBOL, 0);
        }

        public TerminalNode EQ_ALIAS() {
            return getToken(SFMLParser.EQ_ALIAS, 0);
        }

        public TerminalNode LT() {
            return getToken(SFMLParser.LT, 0);
        }

        public TerminalNode EQ() {
            return getToken(SFMLParser.EQ, 0);
        }

        public TerminalNode LE() {
            return getToken(SFMLParser.LE, 0);
        }

        public TerminalNode GE() {
            return getToken(SFMLParser.GE, 0);
        }

        public TerminalNode GT_SYMBOL() {
            return getToken(SFMLParser.GT_SYMBOL, 0);
        }

        public TerminalNode LT_SYMBOL() {
            return getToken(SFMLParser.LT_SYMBOL, 0);
        }

        public TerminalNode EQ_SYMBOL() {
            return getToken(SFMLParser.EQ_SYMBOL, 0);
        }

        public TerminalNode LE_SYMBOL() {
            return getToken(SFMLParser.LE_SYMBOL, 0);
        }

        public TerminalNode GE_SYMBOL() {
            return getToken(SFMLParser.GE_SYMBOL, 0);
        }

        public ComparisonOpContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_comparisonOp;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterComparisonOp(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitComparisonOp(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitComparisonOp(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ComparisonOpContext comparisonOp() throws RecognitionException {
        ComparisonOpContext _localctx = new ComparisonOpContext(_ctx, getState());
        enterRule(_localctx, 94, RULE_comparisonOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(926);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 4610560118520545280L) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class SetOpContext extends ParserRuleContext {

        public TerminalNode OVERALL() {
            return getToken(SFMLParser.OVERALL, 0);
        }

        public TerminalNode SOME() {
            return getToken(SFMLParser.SOME, 0);
        }

        public TerminalNode EVERY() {
            return getToken(SFMLParser.EVERY, 0);
        }

        public TerminalNode EACH() {
            return getToken(SFMLParser.EACH, 0);
        }

        public TerminalNode ONE() {
            return getToken(SFMLParser.ONE, 0);
        }

        public TerminalNode LONE() {
            return getToken(SFMLParser.LONE, 0);
        }

        public SetOpContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_setOp;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterSetOp(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitSetOp(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitSetOp(this);
            else return visitor.visitChildren(this);
        }
    }

    public final SetOpContext setOp() throws RecognitionException {
        SetOpContext _localctx = new SetOpContext(_ctx, getState());
        enterRule(_localctx, 96, RULE_setOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(928);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 32985348833280L) != 0) || _la == EACH || _la == EVERY)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class LabelAccessContext extends ParserRuleContext {

        public List<LabelContext> label() {
            return getRuleContexts(LabelContext.class);
        }

        public LabelContext label(int i) {
            return getRuleContext(LabelContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public RoundrobinContext roundrobin() {
            return getRuleContext(RoundrobinContext.class, 0);
        }

        public SidequalifierContext sidequalifier() {
            return getRuleContext(SidequalifierContext.class, 0);
        }

        public SlotqualifierContext slotqualifier() {
            return getRuleContext(SlotqualifierContext.class, 0);
        }

        public LabelAccessContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_labelAccess;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterLabelAccess(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitLabelAccess(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitLabelAccess(this);
            else return visitor.visitChildren(this);
        }
    }

    public final LabelAccessContext labelAccess() throws RecognitionException {
        LabelAccessContext _localctx = new LabelAccessContext(_ctx, getState());
        enterRule(_localctx, 98, RULE_labelAccess);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(930);
                label();
                setState(935);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(931);
                            match(COMMA);
                            setState(932);
                            label();
                        }
                    }
                    setState(937);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(939);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 129, _ctx)) {
                    case 1: {
                        setState(938);
                        roundrobin();
                    }
                        break;
                }
                setState(942);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 130, _ctx)) {
                    case 1: {
                        setState(941);
                        sidequalifier();
                    }
                        break;
                }
                setState(945);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 131, _ctx)) {
                    case 1: {
                        setState(944);
                        slotqualifier();
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class RoundrobinContext extends ParserRuleContext {

        public TerminalNode ROUND() {
            return getToken(SFMLParser.ROUND, 0);
        }

        public TerminalNode ROBIN() {
            return getToken(SFMLParser.ROBIN, 0);
        }

        public TerminalNode BY() {
            return getToken(SFMLParser.BY, 0);
        }

        public TerminalNode LABEL() {
            return getToken(SFMLParser.LABEL, 0);
        }

        public TerminalNode BLOCK() {
            return getToken(SFMLParser.BLOCK, 0);
        }

        public RoundrobinContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_roundrobin;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterRoundrobin(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitRoundrobin(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitRoundrobin(this);
            else return visitor.visitChildren(this);
        }
    }

    public final RoundrobinContext roundrobin() throws RecognitionException {
        RoundrobinContext _localctx = new RoundrobinContext(_ctx, getState());
        enterRule(_localctx, 100, RULE_roundrobin);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(947);
                match(ROUND);
                setState(948);
                match(ROBIN);
                setState(949);
                match(BY);
                setState(950);
                _la = _input.LA(1);
                if (!(_la == LABEL || _la == BLOCK)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class LabelContext extends ParserRuleContext {

        public LabelContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_label;
        }

        public LabelContext() {}

        public void copyFrom(LabelContext ctx) {
            super.copyFrom(ctx);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class CallLabelContext extends LabelContext {

        public FunctionCallContext functionCall() {
            return getRuleContext(FunctionCallContext.class, 0);
        }

        public List<DataSuffixContext> dataSuffix() {
            return getRuleContexts(DataSuffixContext.class);
        }

        public DataSuffixContext dataSuffix(int i) {
            return getRuleContext(DataSuffixContext.class, i);
        }

        public CallLabelContext(LabelContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterCallLabel(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitCallLabel(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitCallLabel(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class RawLabelContext extends LabelContext {

        public IdentifierContext identifier() {
            return getRuleContext(IdentifierContext.class, 0);
        }

        public List<DataSuffixContext> dataSuffix() {
            return getRuleContexts(DataSuffixContext.class);
        }

        public DataSuffixContext dataSuffix(int i) {
            return getRuleContext(DataSuffixContext.class, i);
        }

        public RawLabelContext(LabelContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterRawLabel(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitRawLabel(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitRawLabel(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class VariableLabelContext extends LabelContext {

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
        }

        public List<DataSuffixContext> dataSuffix() {
            return getRuleContexts(DataSuffixContext.class);
        }

        public DataSuffixContext dataSuffix(int i) {
            return getRuleContext(DataSuffixContext.class, i);
        }

        public VariableLabelContext(LabelContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterVariableLabel(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitVariableLabel(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitVariableLabel(this);
            else return visitor.visitChildren(this);
        }
    }

    @SuppressWarnings("CheckReturnValue")
    public static class StringLabelContext extends LabelContext {

        public StringContext string() {
            return getRuleContext(StringContext.class, 0);
        }

        public StringLabelContext(LabelContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterStringLabel(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitStringLabel(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitStringLabel(this);
            else return visitor.visitChildren(this);
        }
    }

    public final LabelContext label() throws RecognitionException {
        LabelContext _localctx = new LabelContext(_ctx, getState());
        enterRule(_localctx, 102, RULE_label);
        int _la;
        try {
            setState(974);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 135, _ctx)) {
                case 1:
                    _localctx = new RawLabelContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(952);
                    identifier();
                    setState(956);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == LBRACKET || _la == DOT) {
                        {
                            {
                                setState(953);
                                dataSuffix();
                            }
                        }
                        setState(958);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                }
                    break;
                case 2:
                    _localctx = new StringLabelContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(959);
                    string();
                }
                    break;
                case 3:
                    _localctx = new VariableLabelContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(960);
                    match(VARIABLE);
                    setState(964);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == LBRACKET || _la == DOT) {
                        {
                            {
                                setState(961);
                                dataSuffix();
                            }
                        }
                        setState(966);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                }
                    break;
                case 4:
                    _localctx = new CallLabelContext(_localctx);
                    enterOuterAlt(_localctx, 4); {
                    setState(967);
                    functionCall();
                    setState(971);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == LBRACKET || _la == DOT) {
                        {
                            {
                                setState(968);
                                dataSuffix();
                            }
                        }
                        setState(973);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class EmptyslotsContext extends ParserRuleContext {

        public TerminalNode EMPTY() {
            return getToken(SFMLParser.EMPTY, 0);
        }

        public TerminalNode IN() {
            return getToken(SFMLParser.IN, 0);
        }

        public TerminalNode SLOTS() {
            return getToken(SFMLParser.SLOTS, 0);
        }

        public TerminalNode SLOT() {
            return getToken(SFMLParser.SLOT, 0);
        }

        public EmptyslotsContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_emptyslots;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterEmptyslots(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitEmptyslots(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitEmptyslots(this);
            else return visitor.visitChildren(this);
        }
    }

    public final EmptyslotsContext emptyslots() throws RecognitionException {
        EmptyslotsContext _localctx = new EmptyslotsContext(_ctx, getState());
        enterRule(_localctx, 104, RULE_emptyslots);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(976);
                match(EMPTY);
                setState(977);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(978);
                match(IN);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class IdentifierContext extends ParserRuleContext {

        public TerminalNode IDENTIFIER() {
            return getToken(SFMLParser.IDENTIFIER, 0);
        }

        public TerminalNode IMPORT() {
            return getToken(SFMLParser.IMPORT, 0);
        }

        public TerminalNode REVISION() {
            return getToken(SFMLParser.REVISION, 0);
        }

        public TerminalNode DIGEST() {
            return getToken(SFMLParser.DIGEST, 0);
        }

        public TerminalNode WAIT() {
            return getToken(SFMLParser.WAIT, 0);
        }

        public TerminalNode SETCIRCUIT() {
            return getToken(SFMLParser.SETCIRCUIT, 0);
        }

        public TerminalNode TRANSFER() {
            return getToken(SFMLParser.TRANSFER, 0);
        }

        public TerminalNode TRANSFERF() {
            return getToken(SFMLParser.TRANSFERF, 0);
        }

        public TerminalNode INTO() {
            return getToken(SFMLParser.INTO, 0);
        }

        public TerminalNode LET() {
            return getToken(SFMLParser.LET, 0);
        }

        public TerminalNode SET() {
            return getToken(SFMLParser.SET, 0);
        }

        public TerminalNode STATE() {
            return getToken(SFMLParser.STATE, 0);
        }

        public TerminalNode PRINT() {
            return getToken(SFMLParser.PRINT, 0);
        }

        public TerminalNode FUNCTION() {
            return getToken(SFMLParser.FUNCTION, 0);
        }

        public TerminalNode RETURNS() {
            return getToken(SFMLParser.RETURNS, 0);
        }

        public TerminalNode RETURN() {
            return getToken(SFMLParser.RETURN, 0);
        }

        public TerminalNode FOR() {
            return getToken(SFMLParser.FOR, 0);
        }

        public TerminalNode WHILE() {
            return getToken(SFMLParser.WHILE, 0);
        }

        public TerminalNode STEP() {
            return getToken(SFMLParser.STEP, 0);
        }

        public TerminalNode RISING() {
            return getToken(SFMLParser.RISING, 0);
        }

        public TerminalNode FALLING() {
            return getToken(SFMLParser.FALLING, 0);
        }

        public TerminalNode REDSTONE() {
            return getToken(SFMLParser.REDSTONE, 0);
        }

        public TerminalNode LEVEL() {
            return getToken(SFMLParser.LEVEL, 0);
        }

        public TerminalNode LEVELS() {
            return getToken(SFMLParser.LEVELS, 0);
        }

        public TerminalNode GLOBAL() {
            return getToken(SFMLParser.GLOBAL, 0);
        }

        public TerminalNode SECOND() {
            return getToken(SFMLParser.SECOND, 0);
        }

        public TerminalNode SECONDS() {
            return getToken(SFMLParser.SECONDS, 0);
        }

        public TerminalNode TOP() {
            return getToken(SFMLParser.TOP, 0);
        }

        public TerminalNode BOTTOM() {
            return getToken(SFMLParser.BOTTOM, 0);
        }

        public TerminalNode LEFT() {
            return getToken(SFMLParser.LEFT, 0);
        }

        public TerminalNode RIGHT() {
            return getToken(SFMLParser.RIGHT, 0);
        }

        public TerminalNode FRONT() {
            return getToken(SFMLParser.FRONT, 0);
        }

        public TerminalNode BACK() {
            return getToken(SFMLParser.BACK, 0);
        }

        public IdentifierContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_identifier;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterIdentifier(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitIdentifier(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitIdentifier(this);
            else return visitor.visitChildren(this);
        }
    }

    public final IdentifierContext identifier() throws RecognitionException {
        IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
        enterRule(_localctx, 106, RULE_identifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(980);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 4190206L) != 0)
                    || ((((_la - 84)) & ~0x3f) == 0 && ((1L << (_la - 84)) & 17181820803L) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class StateDeclarationContext extends ParserRuleContext {

        public TerminalNode STATE() {
            return getToken(SFMLParser.STATE, 0);
        }

        public VariableNameContext variableName() {
            return getRuleContext(VariableNameContext.class, 0);
        }

        public TerminalNode EQ_SYMBOL() {
            return getToken(SFMLParser.EQ_SYMBOL, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public StateDeclarationContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_stateDeclaration;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterStateDeclaration(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitStateDeclaration(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitStateDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public final StateDeclarationContext stateDeclaration() throws RecognitionException {
        StateDeclarationContext _localctx = new StateDeclarationContext(_ctx, getState());
        enterRule(_localctx, 108, RULE_stateDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(982);
                match(STATE);
                setState(983);
                variableName();
                setState(984);
                match(EQ_SYMBOL);
                setState(985);
                dataExpression();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataDeclarationContext extends ParserRuleContext {

        public TerminalNode LET() {
            return getToken(SFMLParser.LET, 0);
        }

        public VariableNameContext variableName() {
            return getRuleContext(VariableNameContext.class, 0);
        }

        public TerminalNode EQ_SYMBOL() {
            return getToken(SFMLParser.EQ_SYMBOL, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public DataDeclarationContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataDeclaration;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataDeclaration(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataDeclaration(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataDeclarationContext dataDeclaration() throws RecognitionException {
        DataDeclarationContext _localctx = new DataDeclarationContext(_ctx, getState());
        enterRule(_localctx, 110, RULE_dataDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(987);
                match(LET);
                setState(988);
                variableName();
                setState(989);
                match(EQ_SYMBOL);
                setState(990);
                dataExpression();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataAssignmentContext extends ParserRuleContext {

        public DataTargetContext dataTarget() {
            return getRuleContext(DataTargetContext.class, 0);
        }

        public TerminalNode EQ_SYMBOL() {
            return getToken(SFMLParser.EQ_SYMBOL, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode SET() {
            return getToken(SFMLParser.SET, 0);
        }

        public DataAssignmentContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataAssignment;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataAssignment(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataAssignment(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataAssignment(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataAssignmentContext dataAssignment() throws RecognitionException {
        DataAssignmentContext _localctx = new DataAssignmentContext(_ctx, getState());
        enterRule(_localctx, 112, RULE_dataAssignment);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(993);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SET) {
                    {
                        setState(992);
                        match(SET);
                    }
                }

                setState(995);
                dataTarget();
                setState(996);
                match(EQ_SYMBOL);
                setState(997);
                dataExpression();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class CircuitStatementContext extends ParserRuleContext {

        public TerminalNode SETCIRCUIT() {
            return getToken(SFMLParser.SETCIRCUIT, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public List<DataExpressionContext> dataExpression() {
            return getRuleContexts(DataExpressionContext.class);
        }

        public DataExpressionContext dataExpression(int i) {
            return getRuleContext(DataExpressionContext.class, i);
        }

        public TerminalNode COMMA() {
            return getToken(SFMLParser.COMMA, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public TerminalNode INTO() {
            return getToken(SFMLParser.INTO, 0);
        }

        public VariableNameContext variableName() {
            return getRuleContext(VariableNameContext.class, 0);
        }

        public CircuitStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_circuitStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterCircuitStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitCircuitStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitCircuitStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final CircuitStatementContext circuitStatement() throws RecognitionException {
        CircuitStatementContext _localctx = new CircuitStatementContext(_ctx, getState());
        enterRule(_localctx, 114, RULE_circuitStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(999);
                match(SETCIRCUIT);
                setState(1000);
                match(LPAREN);
                setState(1001);
                dataExpression();
                setState(1002);
                match(COMMA);
                setState(1003);
                dataExpression();
                setState(1004);
                match(RPAREN);
                setState(1007);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 137, _ctx)) {
                    case 1: {
                        setState(1005);
                        match(INTO);
                        setState(1006);
                        variableName();
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class WaitStatementContext extends ParserRuleContext {

        public TerminalNode WAIT() {
            return getToken(SFMLParser.WAIT, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public WaitStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_waitStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterWaitStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitWaitStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitWaitStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final WaitStatementContext waitStatement() throws RecognitionException {
        WaitStatementContext _localctx = new WaitStatementContext(_ctx, getState());
        enterRule(_localctx, 116, RULE_waitStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1009);
                match(WAIT);
                setState(1010);
                dataExpression();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class PrintStatementContext extends ParserRuleContext {

        public TerminalNode PRINT() {
            return getToken(SFMLParser.PRINT, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public List<DataExpressionContext> dataExpression() {
            return getRuleContexts(DataExpressionContext.class);
        }

        public DataExpressionContext dataExpression(int i) {
            return getRuleContext(DataExpressionContext.class, i);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public PrintStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_printStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterPrintStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitPrintStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitPrintStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PrintStatementContext printStatement() throws RecognitionException {
        PrintStatementContext _localctx = new PrintStatementContext(_ctx, getState());
        enterRule(_localctx, 118, RULE_printStatement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1012);
                match(PRINT);
                setState(1013);
                match(LPAREN);
                setState(1014);
                dataExpression();
                setState(1019);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(1015);
                            match(COMMA);
                            setState(1016);
                            dataExpression();
                        }
                    }
                    setState(1021);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(1022);
                match(RPAREN);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class TransferStatementContext extends ParserRuleContext {

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public List<DataExpressionContext> dataExpression() {
            return getRuleContexts(DataExpressionContext.class);
        }

        public DataExpressionContext dataExpression(int i) {
            return getRuleContext(DataExpressionContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public TerminalNode TRANSFER() {
            return getToken(SFMLParser.TRANSFER, 0);
        }

        public TerminalNode TRANSFERF() {
            return getToken(SFMLParser.TRANSFERF, 0);
        }

        public TerminalNode INTO() {
            return getToken(SFMLParser.INTO, 0);
        }

        public VariableNameContext variableName() {
            return getRuleContext(VariableNameContext.class, 0);
        }

        public TransferStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_transferStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterTransferStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitTransferStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitTransferStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TransferStatementContext transferStatement() throws RecognitionException {
        TransferStatementContext _localctx = new TransferStatementContext(_ctx, getState());
        enterRule(_localctx, 120, RULE_transferStatement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1024);
                _la = _input.LA(1);
                if (!(_la == TRANSFER || _la == TRANSFERF)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(1025);
                match(LPAREN);
                setState(1026);
                dataExpression();
                setState(1027);
                match(COMMA);
                setState(1028);
                dataExpression();
                setState(1035);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(1029);
                        match(COMMA);
                        setState(1030);
                        dataExpression();
                        setState(1033);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == COMMA) {
                            {
                                setState(1031);
                                match(COMMA);
                                setState(1032);
                                dataExpression();
                            }
                        }

                    }
                }

                setState(1037);
                match(RPAREN);
                setState(1040);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 141, _ctx)) {
                    case 1: {
                        setState(1038);
                        match(INTO);
                        setState(1039);
                        variableName();
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataTargetContext extends ParserRuleContext {

        public VariableNameContext variableName() {
            return getRuleContext(VariableNameContext.class, 0);
        }

        public List<TerminalNode> DOT() {
            return getTokens(SFMLParser.DOT);
        }

        public TerminalNode DOT(int i) {
            return getToken(SFMLParser.DOT, i);
        }

        public List<DataNameContext> dataName() {
            return getRuleContexts(DataNameContext.class);
        }

        public DataNameContext dataName(int i) {
            return getRuleContext(DataNameContext.class, i);
        }

        public List<TerminalNode> LBRACKET() {
            return getTokens(SFMLParser.LBRACKET);
        }

        public TerminalNode LBRACKET(int i) {
            return getToken(SFMLParser.LBRACKET, i);
        }

        public List<DataExpressionContext> dataExpression() {
            return getRuleContexts(DataExpressionContext.class);
        }

        public DataExpressionContext dataExpression(int i) {
            return getRuleContext(DataExpressionContext.class, i);
        }

        public List<TerminalNode> RBRACKET() {
            return getTokens(SFMLParser.RBRACKET);
        }

        public TerminalNode RBRACKET(int i) {
            return getToken(SFMLParser.RBRACKET, i);
        }

        public DataTargetContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataTarget;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataTarget(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataTarget(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataTarget(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataTargetContext dataTarget() throws RecognitionException {
        DataTargetContext _localctx = new DataTargetContext(_ctx, getState());
        enterRule(_localctx, 122, RULE_dataTarget);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1042);
                variableName();
                setState(1051);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACKET || _la == DOT) {
                    {
                        setState(1049);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case DOT: {
                                setState(1043);
                                match(DOT);
                                setState(1044);
                                dataName();
                            }
                                break;
                            case LBRACKET: {
                                setState(1045);
                                match(LBRACKET);
                                setState(1046);
                                dataExpression();
                                setState(1047);
                                match(RBRACKET);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                    }
                    setState(1053);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataExpressionContext extends ParserRuleContext {

        public DataOrContext dataOr() {
            return getRuleContext(DataOrContext.class, 0);
        }

        public DataExpressionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataExpression;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataExpression(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataExpression(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataExpressionContext dataExpression() throws RecognitionException {
        DataExpressionContext _localctx = new DataExpressionContext(_ctx, getState());
        enterRule(_localctx, 124, RULE_dataExpression);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1054);
                dataOr();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataOrContext extends ParserRuleContext {

        public List<DataAndContext> dataAnd() {
            return getRuleContexts(DataAndContext.class);
        }

        public DataAndContext dataAnd(int i) {
            return getRuleContext(DataAndContext.class, i);
        }

        public List<TerminalNode> OR() {
            return getTokens(SFMLParser.OR);
        }

        public TerminalNode OR(int i) {
            return getToken(SFMLParser.OR, i);
        }

        public DataOrContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataOr;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataOr(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataOr(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataOr(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataOrContext dataOr() throws RecognitionException {
        DataOrContext _localctx = new DataOrContext(_ctx, getState());
        enterRule(_localctx, 126, RULE_dataOr);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1056);
                dataAnd();
                setState(1061);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 144, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1057);
                                match(OR);
                                setState(1058);
                                dataAnd();
                            }
                        }
                    }
                    setState(1063);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 144, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataAndContext extends ParserRuleContext {

        public List<DataNotContext> dataNot() {
            return getRuleContexts(DataNotContext.class);
        }

        public DataNotContext dataNot(int i) {
            return getRuleContext(DataNotContext.class, i);
        }

        public List<TerminalNode> AND() {
            return getTokens(SFMLParser.AND);
        }

        public TerminalNode AND(int i) {
            return getToken(SFMLParser.AND, i);
        }

        public DataAndContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataAnd;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataAnd(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataAnd(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataAnd(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataAndContext dataAnd() throws RecognitionException {
        DataAndContext _localctx = new DataAndContext(_ctx, getState());
        enterRule(_localctx, 128, RULE_dataAnd);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1064);
                dataNot();
                setState(1069);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 145, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1065);
                                match(AND);
                                setState(1066);
                                dataNot();
                            }
                        }
                    }
                    setState(1071);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 145, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataNotContext extends ParserRuleContext {

        public TerminalNode NOT() {
            return getToken(SFMLParser.NOT, 0);
        }

        public DataNotContext dataNot() {
            return getRuleContext(DataNotContext.class, 0);
        }

        public DataComparisonContext dataComparison() {
            return getRuleContext(DataComparisonContext.class, 0);
        }

        public DataNotContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataNot;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataNot(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataNot(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataNot(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataNotContext dataNot() throws RecognitionException {
        DataNotContext _localctx = new DataNotContext(_ctx, getState());
        enterRule(_localctx, 130, RULE_dataNot);
        try {
            setState(1075);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 146, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(1072);
                    match(NOT);
                    setState(1073);
                    dataNot();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(1074);
                    dataComparison();
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataComparisonContext extends ParserRuleContext {

        public List<DataBitOrContext> dataBitOr() {
            return getRuleContexts(DataBitOrContext.class);
        }

        public DataBitOrContext dataBitOr(int i) {
            return getRuleContext(DataBitOrContext.class, i);
        }

        public ComparisonOpContext comparisonOp() {
            return getRuleContext(ComparisonOpContext.class, 0);
        }

        public DataComparisonContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataComparison;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataComparison(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataComparison(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataComparison(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataComparisonContext dataComparison() throws RecognitionException {
        DataComparisonContext _localctx = new DataComparisonContext(_ctx, getState());
        enterRule(_localctx, 132, RULE_dataComparison);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1077);
                dataBitOr();
                setState(1081);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 147, _ctx)) {
                    case 1: {
                        setState(1078);
                        comparisonOp();
                        setState(1079);
                        dataBitOr();
                    }
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataBitOrContext extends ParserRuleContext {

        public List<DataBitXorContext> dataBitXor() {
            return getRuleContexts(DataBitXorContext.class);
        }

        public DataBitXorContext dataBitXor(int i) {
            return getRuleContext(DataBitXorContext.class, i);
        }

        public List<TerminalNode> BIT_OR() {
            return getTokens(SFMLParser.BIT_OR);
        }

        public TerminalNode BIT_OR(int i) {
            return getToken(SFMLParser.BIT_OR, i);
        }

        public DataBitOrContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataBitOr;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataBitOr(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataBitOr(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataBitOr(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataBitOrContext dataBitOr() throws RecognitionException {
        DataBitOrContext _localctx = new DataBitOrContext(_ctx, getState());
        enterRule(_localctx, 134, RULE_dataBitOr);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1083);
                dataBitXor();
                setState(1088);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == BIT_OR) {
                    {
                        {
                            setState(1084);
                            match(BIT_OR);
                            setState(1085);
                            dataBitXor();
                        }
                    }
                    setState(1090);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataBitXorContext extends ParserRuleContext {

        public List<DataBitAndContext> dataBitAnd() {
            return getRuleContexts(DataBitAndContext.class);
        }

        public DataBitAndContext dataBitAnd(int i) {
            return getRuleContext(DataBitAndContext.class, i);
        }

        public List<TerminalNode> BIT_XOR() {
            return getTokens(SFMLParser.BIT_XOR);
        }

        public TerminalNode BIT_XOR(int i) {
            return getToken(SFMLParser.BIT_XOR, i);
        }

        public DataBitXorContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataBitXor;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataBitXor(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataBitXor(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataBitXor(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataBitXorContext dataBitXor() throws RecognitionException {
        DataBitXorContext _localctx = new DataBitXorContext(_ctx, getState());
        enterRule(_localctx, 136, RULE_dataBitXor);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1091);
                dataBitAnd();
                setState(1096);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == BIT_XOR) {
                    {
                        {
                            setState(1092);
                            match(BIT_XOR);
                            setState(1093);
                            dataBitAnd();
                        }
                    }
                    setState(1098);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataBitAndContext extends ParserRuleContext {

        public List<DataShiftContext> dataShift() {
            return getRuleContexts(DataShiftContext.class);
        }

        public DataShiftContext dataShift(int i) {
            return getRuleContext(DataShiftContext.class, i);
        }

        public List<TerminalNode> BIT_AND() {
            return getTokens(SFMLParser.BIT_AND);
        }

        public TerminalNode BIT_AND(int i) {
            return getToken(SFMLParser.BIT_AND, i);
        }

        public DataBitAndContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataBitAnd;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataBitAnd(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataBitAnd(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataBitAnd(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataBitAndContext dataBitAnd() throws RecognitionException {
        DataBitAndContext _localctx = new DataBitAndContext(_ctx, getState());
        enterRule(_localctx, 138, RULE_dataBitAnd);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1099);
                dataShift();
                setState(1104);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == BIT_AND) {
                    {
                        {
                            setState(1100);
                            match(BIT_AND);
                            setState(1101);
                            dataShift();
                        }
                    }
                    setState(1106);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataShiftContext extends ParserRuleContext {

        public List<DataAddContext> dataAdd() {
            return getRuleContexts(DataAddContext.class);
        }

        public DataAddContext dataAdd(int i) {
            return getRuleContext(DataAddContext.class, i);
        }

        public List<TerminalNode> SHIFT_LEFT() {
            return getTokens(SFMLParser.SHIFT_LEFT);
        }

        public TerminalNode SHIFT_LEFT(int i) {
            return getToken(SFMLParser.SHIFT_LEFT, i);
        }

        public List<TerminalNode> SHIFT_RIGHT() {
            return getTokens(SFMLParser.SHIFT_RIGHT);
        }

        public TerminalNode SHIFT_RIGHT(int i) {
            return getToken(SFMLParser.SHIFT_RIGHT, i);
        }

        public List<TerminalNode> SHIFT_UNSIGNED_RIGHT() {
            return getTokens(SFMLParser.SHIFT_UNSIGNED_RIGHT);
        }

        public TerminalNode SHIFT_UNSIGNED_RIGHT(int i) {
            return getToken(SFMLParser.SHIFT_UNSIGNED_RIGHT, i);
        }

        public DataShiftContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataShift;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataShift(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataShift(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataShift(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataShiftContext dataShift() throws RecognitionException {
        DataShiftContext _localctx = new DataShiftContext(_ctx, getState());
        enterRule(_localctx, 140, RULE_dataShift);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1107);
                dataAdd();
                setState(1112);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 120259084288L) != 0)) {
                    {
                        {
                            setState(1108);
                            _la = _input.LA(1);
                            if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 120259084288L) != 0))) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                            setState(1109);
                            dataAdd();
                        }
                    }
                    setState(1114);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataAddContext extends ParserRuleContext {

        public List<DataMultiplyContext> dataMultiply() {
            return getRuleContexts(DataMultiplyContext.class);
        }

        public DataMultiplyContext dataMultiply(int i) {
            return getRuleContext(DataMultiplyContext.class, i);
        }

        public List<TerminalNode> PLUS() {
            return getTokens(SFMLParser.PLUS);
        }

        public TerminalNode PLUS(int i) {
            return getToken(SFMLParser.PLUS, i);
        }

        public List<TerminalNode> DASH() {
            return getTokens(SFMLParser.DASH);
        }

        public TerminalNode DASH(int i) {
            return getToken(SFMLParser.DASH, i);
        }

        public DataAddContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataAdd;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataAdd(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataAdd(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataAdd(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataAddContext dataAdd() throws RecognitionException {
        DataAddContext _localctx = new DataAddContext(_ctx, getState());
        enterRule(_localctx, 142, RULE_dataAdd);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1115);
                dataMultiply();
                setState(1120);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 152, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1116);
                                _la = _input.LA(1);
                                if (!(_la == PLUS || _la == DASH)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(1117);
                                dataMultiply();
                            }
                        }
                    }
                    setState(1122);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 152, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataMultiplyContext extends ParserRuleContext {

        public List<DataSignContext> dataSign() {
            return getRuleContexts(DataSignContext.class);
        }

        public DataSignContext dataSign(int i) {
            return getRuleContext(DataSignContext.class, i);
        }

        public List<MultiplyOperatorContext> multiplyOperator() {
            return getRuleContexts(MultiplyOperatorContext.class);
        }

        public MultiplyOperatorContext multiplyOperator(int i) {
            return getRuleContext(MultiplyOperatorContext.class, i);
        }

        public List<TerminalNode> SLASH() {
            return getTokens(SFMLParser.SLASH);
        }

        public TerminalNode SLASH(int i) {
            return getToken(SFMLParser.SLASH, i);
        }

        public List<TerminalNode> PERCENT() {
            return getTokens(SFMLParser.PERCENT);
        }

        public TerminalNode PERCENT(int i) {
            return getToken(SFMLParser.PERCENT, i);
        }

        public DataMultiplyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataMultiply;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataMultiply(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataMultiply(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataMultiply(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataMultiplyContext dataMultiply() throws RecognitionException {
        DataMultiplyContext _localctx = new DataMultiplyContext(_ctx, getState());
        enterRule(_localctx, 144, RULE_dataMultiply);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1123);
                dataSign();
                setState(1132);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 154, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1127);
                                _errHandler.sync(this);
                                switch (getInterpreter().adaptivePredict(_input, 153, _ctx)) {
                                    case 1: {
                                        setState(1124);
                                        multiplyOperator();
                                    }
                                        break;
                                    case 2: {
                                        setState(1125);
                                        match(SLASH);
                                    }
                                        break;
                                    case 3: {
                                        setState(1126);
                                        match(PERCENT);
                                    }
                                        break;
                                }
                                setState(1129);
                                dataSign();
                            }
                        }
                    }
                    setState(1134);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 154, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class MultiplyOperatorContext extends ParserRuleContext {

        public TerminalNode IDENTIFIER() {
            return getToken(SFMLParser.IDENTIFIER, 0);
        }

        public MultiplyOperatorContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_multiplyOperator;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterMultiplyOperator(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitMultiplyOperator(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitMultiplyOperator(this);
            else return visitor.visitChildren(this);
        }
    }

    public final MultiplyOperatorContext multiplyOperator() throws RecognitionException {
        MultiplyOperatorContext _localctx = new MultiplyOperatorContext(_ctx, getState());
        enterRule(_localctx, 146, RULE_multiplyOperator);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1135);
                if (!(_input.LT(1)
                    .getText()
                    .equals("*"))) throw new FailedPredicateException(this, "_input.LT(1).getText().equals(\"*\")");
                setState(1136);
                match(IDENTIFIER);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataSignContext extends ParserRuleContext {

        public DataSignContext dataSign() {
            return getRuleContext(DataSignContext.class, 0);
        }

        public TerminalNode PLUS() {
            return getToken(SFMLParser.PLUS, 0);
        }

        public TerminalNode DASH() {
            return getToken(SFMLParser.DASH, 0);
        }

        public TerminalNode BIT_NOT() {
            return getToken(SFMLParser.BIT_NOT, 0);
        }

        public DataPostfixContext dataPostfix() {
            return getRuleContext(DataPostfixContext.class, 0);
        }

        public DataSignContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataSign;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataSign(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataSign(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataSign(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataSignContext dataSign() throws RecognitionException {
        DataSignContext _localctx = new DataSignContext(_ctx, getState());
        enterRule(_localctx, 148, RULE_dataSign);
        int _la;
        try {
            setState(1141);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 155, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(1138);
                    _la = _input.LA(1);
                    if (!(_la == BIT_NOT || _la == PLUS || _la == DASH)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(1139);
                    dataSign();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(1140);
                    dataPostfix();
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataPostfixContext extends ParserRuleContext {

        public DataPrimaryContext dataPrimary() {
            return getRuleContext(DataPrimaryContext.class, 0);
        }

        public List<DataSuffixContext> dataSuffix() {
            return getRuleContexts(DataSuffixContext.class);
        }

        public DataSuffixContext dataSuffix(int i) {
            return getRuleContext(DataSuffixContext.class, i);
        }

        public DataPostfixContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataPostfix;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataPostfix(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataPostfix(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataPostfix(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataPostfixContext dataPostfix() throws RecognitionException {
        DataPostfixContext _localctx = new DataPostfixContext(_ctx, getState());
        enterRule(_localctx, 150, RULE_dataPostfix);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1143);
                dataPrimary();
                setState(1147);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 156, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1144);
                                dataSuffix();
                            }
                        }
                    }
                    setState(1149);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 156, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataSuffixContext extends ParserRuleContext {

        public TerminalNode DOT() {
            return getToken(SFMLParser.DOT, 0);
        }

        public DataNameContext dataName() {
            return getRuleContext(DataNameContext.class, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public List<DataExpressionContext> dataExpression() {
            return getRuleContexts(DataExpressionContext.class);
        }

        public DataExpressionContext dataExpression(int i) {
            return getRuleContext(DataExpressionContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public TerminalNode LBRACKET() {
            return getToken(SFMLParser.LBRACKET, 0);
        }

        public TerminalNode RBRACKET() {
            return getToken(SFMLParser.RBRACKET, 0);
        }

        public DataSuffixContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataSuffix;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataSuffix(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataSuffix(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataSuffix(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataSuffixContext dataSuffix() throws RecognitionException {
        DataSuffixContext _localctx = new DataSuffixContext(_ctx, getState());
        enterRule(_localctx, 152, RULE_dataSuffix);
        int _la;
        try {
            setState(1170);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case DOT:
                    enterOuterAlt(_localctx, 1); {
                    setState(1150);
                    match(DOT);
                    setState(1151);
                    dataName();
                    setState(1164);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 159, _ctx)) {
                        case 1: {
                            setState(1152);
                            match(LPAREN);
                            setState(1161);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764468516982786L) != 0)
                                || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 64809613387612159L) != 0)) {
                                {
                                    setState(1153);
                                    dataExpression();
                                    setState(1158);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    while (_la == COMMA) {
                                        {
                                            {
                                                setState(1154);
                                                match(COMMA);
                                                setState(1155);
                                                dataExpression();
                                            }
                                        }
                                        setState(1160);
                                        _errHandler.sync(this);
                                        _la = _input.LA(1);
                                    }
                                }
                            }

                            setState(1163);
                            match(RPAREN);
                        }
                            break;
                    }
                }
                    break;
                case LBRACKET:
                    enterOuterAlt(_localctx, 2); {
                    setState(1166);
                    match(LBRACKET);
                    setState(1167);
                    dataExpression();
                    setState(1168);
                    match(RBRACKET);
                }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataPrimaryContext extends ParserRuleContext {

        public TerminalNode NUMBER() {
            return getToken(SFMLParser.NUMBER, 0);
        }

        public TerminalNode STRING() {
            return getToken(SFMLParser.STRING, 0);
        }

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
        }

        public TerminalNode IMPORT() {
            return getToken(SFMLParser.IMPORT, 0);
        }

        public TerminalNode REVISION() {
            return getToken(SFMLParser.REVISION, 0);
        }

        public TerminalNode DIGEST() {
            return getToken(SFMLParser.DIGEST, 0);
        }

        public TerminalNode TRUE() {
            return getToken(SFMLParser.TRUE, 0);
        }

        public TerminalNode FALSE() {
            return getToken(SFMLParser.FALSE, 0);
        }

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public List<DataExpressionContext> dataExpression() {
            return getRuleContexts(DataExpressionContext.class);
        }

        public DataExpressionContext dataExpression(int i) {
            return getRuleContext(DataExpressionContext.class, i);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public FunctionCallContext functionCall() {
            return getRuleContext(FunctionCallContext.class, 0);
        }

        public TerminalNode IDENTIFIER() {
            return getToken(SFMLParser.IDENTIFIER, 0);
        }

        public TerminalNode LBRACKET() {
            return getToken(SFMLParser.LBRACKET, 0);
        }

        public TerminalNode RBRACKET() {
            return getToken(SFMLParser.RBRACKET, 0);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public TerminalNode LBRACE() {
            return getToken(SFMLParser.LBRACE, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(SFMLParser.RBRACE, 0);
        }

        public List<DataFieldContext> dataField() {
            return getRuleContexts(DataFieldContext.class);
        }

        public DataFieldContext dataField(int i) {
            return getRuleContext(DataFieldContext.class, i);
        }

        public DataPrimaryContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataPrimary;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataPrimary(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataPrimary(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataPrimary(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataPrimaryContext dataPrimary() throws RecognitionException {
        DataPrimaryContext _localctx = new DataPrimaryContext(_ctx, getState());
        enterRule(_localctx, 154, RULE_dataPrimary);
        int _la;
        try {
            int _alt;
            setState(1216);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 167, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(1172);
                    match(NUMBER);
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(1173);
                    match(STRING);
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(1174);
                    match(VARIABLE);
                }
                    break;
                case 4:
                    enterOuterAlt(_localctx, 4); {
                    setState(1175);
                    match(IMPORT);
                }
                    break;
                case 5:
                    enterOuterAlt(_localctx, 5); {
                    setState(1176);
                    match(REVISION);
                }
                    break;
                case 6:
                    enterOuterAlt(_localctx, 6); {
                    setState(1177);
                    match(DIGEST);
                }
                    break;
                case 7:
                    enterOuterAlt(_localctx, 7); {
                    setState(1178);
                    match(TRUE);
                }
                    break;
                case 8:
                    enterOuterAlt(_localctx, 8); {
                    setState(1179);
                    match(FALSE);
                }
                    break;
                case 9:
                    enterOuterAlt(_localctx, 9); {
                    setState(1180);
                    match(LPAREN);
                    setState(1181);
                    dataExpression();
                    setState(1182);
                    match(RPAREN);
                }
                    break;
                case 10:
                    enterOuterAlt(_localctx, 10); {
                    setState(1184);
                    functionCall();
                }
                    break;
                case 11:
                    enterOuterAlt(_localctx, 11); {
                    setState(1185);
                    match(IDENTIFIER);
                }
                    break;
                case 12:
                    enterOuterAlt(_localctx, 12); {
                    setState(1186);
                    match(LBRACKET);
                    setState(1198);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764468516982786L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 64809613387612159L) != 0)) {
                        {
                            setState(1187);
                            dataExpression();
                            setState(1192);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 161, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(1188);
                                            match(COMMA);
                                            setState(1189);
                                            dataExpression();
                                        }
                                    }
                                }
                                setState(1194);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 161, _ctx);
                            }
                            setState(1196);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(1195);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(1200);
                    match(RBRACKET);
                }
                    break;
                case 13:
                    enterOuterAlt(_localctx, 13); {
                    setState(1201);
                    match(LBRACE);
                    setState(1213);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764477153054722L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 18084767253643263L) != 0)) {
                        {
                            setState(1202);
                            dataField();
                            setState(1207);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 164, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(1203);
                                            match(COMMA);
                                            setState(1204);
                                            dataField();
                                        }
                                    }
                                }
                                setState(1209);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 164, _ctx);
                            }
                            setState(1211);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(1210);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(1215);
                    match(RBRACE);
                }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataFieldContext extends ParserRuleContext {

        public DataNameContext dataName() {
            return getRuleContext(DataNameContext.class, 0);
        }

        public TerminalNode COLON() {
            return getToken(SFMLParser.COLON, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public DataFieldContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataField;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataField(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataField(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataField(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataFieldContext dataField() throws RecognitionException {
        DataFieldContext _localctx = new DataFieldContext(_ctx, getState());
        enterRule(_localctx, 156, RULE_dataField);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1218);
                dataName();
                setState(1219);
                match(COLON);
                setState(1220);
                dataExpression();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class DataNameContext extends ParserRuleContext {

        public TerminalNode IDENTIFIER() {
            return getToken(SFMLParser.IDENTIFIER, 0);
        }

        public TerminalNode IMPORT() {
            return getToken(SFMLParser.IMPORT, 0);
        }

        public TerminalNode REVISION() {
            return getToken(SFMLParser.REVISION, 0);
        }

        public TerminalNode DIGEST() {
            return getToken(SFMLParser.DIGEST, 0);
        }

        public TerminalNode WAIT() {
            return getToken(SFMLParser.WAIT, 0);
        }

        public TerminalNode SETCIRCUIT() {
            return getToken(SFMLParser.SETCIRCUIT, 0);
        }

        public TerminalNode TRANSFER() {
            return getToken(SFMLParser.TRANSFER, 0);
        }

        public TerminalNode TRANSFERF() {
            return getToken(SFMLParser.TRANSFERF, 0);
        }

        public TerminalNode INTO() {
            return getToken(SFMLParser.INTO, 0);
        }

        public TerminalNode LET() {
            return getToken(SFMLParser.LET, 0);
        }

        public TerminalNode SET() {
            return getToken(SFMLParser.SET, 0);
        }

        public TerminalNode STATE() {
            return getToken(SFMLParser.STATE, 0);
        }

        public TerminalNode PRINT() {
            return getToken(SFMLParser.PRINT, 0);
        }

        public TerminalNode FUNCTION() {
            return getToken(SFMLParser.FUNCTION, 0);
        }

        public TerminalNode RETURNS() {
            return getToken(SFMLParser.RETURNS, 0);
        }

        public TerminalNode RETURN() {
            return getToken(SFMLParser.RETURN, 0);
        }

        public TerminalNode IF() {
            return getToken(SFMLParser.IF, 0);
        }

        public TerminalNode THEN() {
            return getToken(SFMLParser.THEN, 0);
        }

        public TerminalNode ELSE() {
            return getToken(SFMLParser.ELSE, 0);
        }

        public TerminalNode HAS() {
            return getToken(SFMLParser.HAS, 0);
        }

        public TerminalNode OVERALL() {
            return getToken(SFMLParser.OVERALL, 0);
        }

        public TerminalNode SOME() {
            return getToken(SFMLParser.SOME, 0);
        }

        public TerminalNode ONE() {
            return getToken(SFMLParser.ONE, 0);
        }

        public TerminalNode LONE() {
            return getToken(SFMLParser.LONE, 0);
        }

        public TerminalNode TRUE() {
            return getToken(SFMLParser.TRUE, 0);
        }

        public TerminalNode FALSE() {
            return getToken(SFMLParser.FALSE, 0);
        }

        public TerminalNode NOT() {
            return getToken(SFMLParser.NOT, 0);
        }

        public TerminalNode AND() {
            return getToken(SFMLParser.AND, 0);
        }

        public TerminalNode OR() {
            return getToken(SFMLParser.OR, 0);
        }

        public TerminalNode GT() {
            return getToken(SFMLParser.GT, 0);
        }

        public TerminalNode LT() {
            return getToken(SFMLParser.LT, 0);
        }

        public TerminalNode EQ() {
            return getToken(SFMLParser.EQ, 0);
        }

        public TerminalNode LE() {
            return getToken(SFMLParser.LE, 0);
        }

        public TerminalNode GE() {
            return getToken(SFMLParser.GE, 0);
        }

        public TerminalNode FROM() {
            return getToken(SFMLParser.FROM, 0);
        }

        public TerminalNode TO() {
            return getToken(SFMLParser.TO, 0);
        }

        public TerminalNode INPUT() {
            return getToken(SFMLParser.INPUT, 0);
        }

        public TerminalNode OUTPUT() {
            return getToken(SFMLParser.OUTPUT, 0);
        }

        public TerminalNode WHERE() {
            return getToken(SFMLParser.WHERE, 0);
        }

        public TerminalNode SLOTS() {
            return getToken(SFMLParser.SLOTS, 0);
        }

        public TerminalNode SLOT() {
            return getToken(SFMLParser.SLOT, 0);
        }

        public TerminalNode RETAIN() {
            return getToken(SFMLParser.RETAIN, 0);
        }

        public TerminalNode EACH() {
            return getToken(SFMLParser.EACH, 0);
        }

        public TerminalNode EXCEPT() {
            return getToken(SFMLParser.EXCEPT, 0);
        }

        public TerminalNode FORGET() {
            return getToken(SFMLParser.FORGET, 0);
        }

        public TerminalNode EMPTY() {
            return getToken(SFMLParser.EMPTY, 0);
        }

        public TerminalNode IN() {
            return getToken(SFMLParser.IN, 0);
        }

        public TerminalNode WITHOUT() {
            return getToken(SFMLParser.WITHOUT, 0);
        }

        public TerminalNode WITH() {
            return getToken(SFMLParser.WITH, 0);
        }

        public TerminalNode TAG() {
            return getToken(SFMLParser.TAG, 0);
        }

        public TerminalNode ROUND() {
            return getToken(SFMLParser.ROUND, 0);
        }

        public TerminalNode ROBIN() {
            return getToken(SFMLParser.ROBIN, 0);
        }

        public TerminalNode BY() {
            return getToken(SFMLParser.BY, 0);
        }

        public TerminalNode LABEL() {
            return getToken(SFMLParser.LABEL, 0);
        }

        public TerminalNode BLOCK() {
            return getToken(SFMLParser.BLOCK, 0);
        }

        public TerminalNode TOP() {
            return getToken(SFMLParser.TOP, 0);
        }

        public TerminalNode BOTTOM() {
            return getToken(SFMLParser.BOTTOM, 0);
        }

        public TerminalNode NORTH() {
            return getToken(SFMLParser.NORTH, 0);
        }

        public TerminalNode EAST() {
            return getToken(SFMLParser.EAST, 0);
        }

        public TerminalNode SOUTH() {
            return getToken(SFMLParser.SOUTH, 0);
        }

        public TerminalNode WEST() {
            return getToken(SFMLParser.WEST, 0);
        }

        public TerminalNode SIDE() {
            return getToken(SFMLParser.SIDE, 0);
        }

        public TerminalNode LEFT() {
            return getToken(SFMLParser.LEFT, 0);
        }

        public TerminalNode RIGHT() {
            return getToken(SFMLParser.RIGHT, 0);
        }

        public TerminalNode FRONT() {
            return getToken(SFMLParser.FRONT, 0);
        }

        public TerminalNode BACK() {
            return getToken(SFMLParser.BACK, 0);
        }

        public TerminalNode NULL() {
            return getToken(SFMLParser.NULL, 0);
        }

        public TerminalNode TICKS() {
            return getToken(SFMLParser.TICKS, 0);
        }

        public TerminalNode TICK() {
            return getToken(SFMLParser.TICK, 0);
        }

        public TerminalNode SECONDS() {
            return getToken(SFMLParser.SECONDS, 0);
        }

        public TerminalNode SECOND() {
            return getToken(SFMLParser.SECOND, 0);
        }

        public TerminalNode GLOBAL() {
            return getToken(SFMLParser.GLOBAL, 0);
        }

        public TerminalNode REDSTONE() {
            return getToken(SFMLParser.REDSTONE, 0);
        }

        public TerminalNode LEVEL() {
            return getToken(SFMLParser.LEVEL, 0);
        }

        public TerminalNode LEVELS() {
            return getToken(SFMLParser.LEVELS, 0);
        }

        public TerminalNode PULSE() {
            return getToken(SFMLParser.PULSE, 0);
        }

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
        }

        public TerminalNode NAME() {
            return getToken(SFMLParser.NAME, 0);
        }

        public TerminalNode EVERY() {
            return getToken(SFMLParser.EVERY, 0);
        }

        public TerminalNode PLUS() {
            return getToken(SFMLParser.PLUS, 0);
        }

        public TerminalNode FOR() {
            return getToken(SFMLParser.FOR, 0);
        }

        public TerminalNode WHILE() {
            return getToken(SFMLParser.WHILE, 0);
        }

        public TerminalNode STEP() {
            return getToken(SFMLParser.STEP, 0);
        }

        public TerminalNode RISING() {
            return getToken(SFMLParser.RISING, 0);
        }

        public TerminalNode FALLING() {
            return getToken(SFMLParser.FALLING, 0);
        }

        public DataNameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dataName;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterDataName(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitDataName(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitDataName(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataNameContext dataName() throws RecognitionException {
        DataNameContext _localctx = new DataNameContext(_ctx, getState());
        enterRule(_localctx, 158, RULE_dataName);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1222);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & -3145764477153054722L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 18084767253643263L) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class StringContext extends ParserRuleContext {

        public TerminalNode STRING() {
            return getToken(SFMLParser.STRING, 0);
        }

        public StringContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_string;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterString(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitString(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitString(this);
            else return visitor.visitChildren(this);
        }
    }

    public final StringContext string() throws RecognitionException {
        StringContext _localctx = new StringContext(_ctx, getState());
        enterRule(_localctx, 160, RULE_string);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1224);
                match(STRING);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    @SuppressWarnings("CheckReturnValue")
    public static class NumberContext extends ParserRuleContext {

        public TerminalNode NUMBER() {
            return getToken(SFMLParser.NUMBER, 0);
        }

        public NumberContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_number;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterNumber(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitNumber(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitNumber(this);
            else return visitor.visitChildren(this);
        }
    }

    public final NumberContext number() throws RecognitionException {
        NumberContext _localctx = new NumberContext(_ctx, getState());
        enterRule(_localctx, 162, RULE_number);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1226);
                match(NUMBER);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
        switch (ruleIndex) {
            case 34:
                return withClause_sempred((WithClauseContext) _localctx, predIndex);
            case 46:
                return boolexpr_sempred((BoolexprContext) _localctx, predIndex);
            case 73:
                return multiplyOperator_sempred((MultiplyOperatorContext) _localctx, predIndex);
        }
        return true;
    }

    private boolean withClause_sempred(WithClauseContext _localctx, int predIndex) {
        switch (predIndex) {
            case 0:
                return precpred(_ctx, 3);
            case 1:
                return precpred(_ctx, 2);
        }
        return true;
    }

    private boolean boolexpr_sempred(BoolexprContext _localctx, int predIndex) {
        switch (predIndex) {
            case 2:
                return precpred(_ctx, 4);
            case 3:
                return precpred(_ctx, 3);
        }
        return true;
    }

    private boolean multiplyOperator_sempred(MultiplyOperatorContext _localctx, int predIndex) {
        switch (predIndex) {
            case 4:
                return _input.LT(1)
                    .getText()
                    .equals("*");
        }
        return true;
    }

    public static final String _serializedATN = "\u0004\u0001z\u04cd\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"
        + "\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"
        + "\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"
        + "\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"
        + "\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"
        + "\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"
        + "\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"
        + "\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"
        + "\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"
        + "\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"
        + "\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"
        + "#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"
        + "(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"
        + "-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u00071\u0002"
        + "2\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u00076\u0002"
        + "7\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007;\u0002"
        + "<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007@\u0002"
        + "A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0002E\u0007E\u0002"
        + "F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007J\u0002"
        + "K\u0007K\u0002L\u0007L\u0002M\u0007M\u0002N\u0007N\u0002O\u0007O\u0002"
        + "P\u0007P\u0002Q\u0007Q\u0001\u0000\u0003\u0000\u00a6\b\u0000\u0001\u0000"
        + "\u0005\u0000\u00a9\b\u0000\n\u0000\f\u0000\u00ac\t\u0000\u0001\u0000\u0001"
        + "\u0000\u0003\u0000\u00b0\b\u0000\u0001\u0000\u0003\u0000\u00b3\b\u0000"
        + "\u0005\u0000\u00b5\b\u0000\n\u0000\f\u0000\u00b8\t\u0000\u0001\u0000\u0001"
        + "\u0000\u0005\u0000\u00bc\b\u0000\n\u0000\f\u0000\u00bf\t\u0000\u0001\u0000"
        + "\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00cc\b\u0001"
        + "\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"
        + "\u0005\u0002\u00d4\b\u0002\n\u0002\f\u0002\u00d7\t\u0002\u0003\u0002\u00d9"
        + "\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"
        + "\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003"
        + "\u0002\u00e6\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"
        + "\u0002\u0001\u0002\u0005\u0002\u00ee\b\u0002\n\u0002\f\u0002\u00f1\t\u0002"
        + "\u0003\u0002\u00f3\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"
        + "\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002"
        + "\u00fe\b\u0002\u0003\u0002\u0100\b\u0002\u0001\u0003\u0001\u0003\u0001"
        + "\u0003\u0005\u0003\u0105\b\u0003\n\u0003\f\u0003\u0108\t\u0003\u0001\u0004"
        + "\u0003\u0004\u010b\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"
        + "\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u0114\b\u0004\u0001\u0005"
        + "\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"
        + "\u0005\u0006\u011d\b\u0006\n\u0006\f\u0006\u0120\t\u0006\u0003\u0006\u0122"
        + "\b\u0006\u0001\u0006\u0003\u0006\u0125\b\u0006\u0001\u0006\u0005\u0006"
        + "\u0128\b\u0006\n\u0006\f\u0006\u012b\t\u0006\u0001\u0007\u0001\u0007\u0003"
        + "\u0007\u012f\b\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001"
        + "\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0005\t\u013c\b\t\n\t\f\t\u013f"
        + "\t\t\u0003\t\u0141\b\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0003\n"
        + "\u0148\b\n\u0001\u000b\u0001\u000b\u0003\u000b\u014c\b\u000b\u0001\f\u0001"
        + "\f\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0003\u000e\u015d\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0001\u000e\u0003\u000e\u016a\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0179\b\u000e"
        + "\u0003\u000e\u017b\b\u000e\u0001\u000f\u0003\u000f\u017e\b\u000f\u0001"
        + "\u000f\u0003\u000f\u0181\b\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0185"
        + "\b\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u018b"
        + "\b\u000f\u0001\u000f\u0003\u000f\u018e\b\u000f\u0001\u0010\u0005\u0010"
        + "\u0191\b\u0010\n\u0010\f\u0010\u0194\t\u0010\u0001\u0011\u0001\u0011\u0001"
        + "\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"
        + "\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"
        + "\u0011\u0003\u0011\u01a5\b\u0011\u0001\u0011\u0003\u0011\u01a8\b\u0011"
        + "\u0001\u0012\u0001\u0012\u0003\u0012\u01ac\b\u0012\u0001\u0012\u0001\u0012"
        + "\u0001\u0012\u0003\u0012\u01b1\b\u0012\u0001\u0012\u0001\u0012\u0003\u0012"
        + "\u01b5\b\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u01ba\b"
        + "\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u01bf\b\u0012\u0001"
        + "\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u01c4\b\u0012\u0003\u0012\u01c6"
        + "\b\u0012\u0001\u0013\u0001\u0013\u0003\u0013\u01ca\b\u0013\u0001\u0013"
        + "\u0001\u0013\u0005\u0013\u01ce\b\u0013\n\u0013\f\u0013\u01d1\t\u0013\u0001"
        + "\u0013\u0003\u0013\u01d4\b\u0013\u0001\u0014\u0001\u0014\u0003\u0014\u01d8"
        + "\b\u0014\u0001\u0014\u0003\u0014\u01db\b\u0014\u0001\u0014\u0001\u0014"
        + "\u0003\u0014\u01df\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014"
        + "\u01e4\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u01e9\b"
        + "\u0014\u0001\u0014\u0003\u0014\u01ec\b\u0014\u0003\u0014\u01ee\b\u0014"
        + "\u0001\u0015\u0001\u0015\u0003\u0015\u01f2\b\u0015\u0001\u0015\u0003\u0015"
        + "\u01f5\b\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u01f9\b\u0015\u0001"
        + "\u0015\u0003\u0015\u01fc\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003"
        + "\u0015\u0201\b\u0015\u0001\u0015\u0003\u0015\u0204\b\u0015\u0001\u0015"
        + "\u0001\u0015\u0001\u0015\u0003\u0015\u0209\b\u0015\u0001\u0015\u0003\u0015"
        + "\u020c\b\u0015\u0003\u0015\u020e\b\u0015\u0001\u0016\u0001\u0016\u0001"
        + "\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u0217"
        + "\b\u0018\n\u0018\f\u0018\u021a\t\u0018\u0001\u0018\u0003\u0018\u021d\b"
        + "\u0018\u0001\u0019\u0003\u0019\u0220\b\u0019\u0001\u0019\u0001\u0019\u0003"
        + "\u0019\u0224\b\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u0228\b\u0019"
        + "\u0001\u0019\u0003\u0019\u022b\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a"
        + "\u0001\u001a\u0001\u001a\u0003\u001a\u0232\b\u001a\u0001\u001b\u0001\u001b"
        + "\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u0239\b\u001b\u0001\u001b"
        + "\u0003\u001b\u023c\b\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"
        + "\u0001\u001c\u0001\u001c\u0003\u001c\u0244\b\u001c\u0001\u001c\u0003\u001c"
        + "\u0247\b\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e"
        + "\u0001\u001e\u0003\u001e\u024f\b\u001e\u0001\u001e\u0001\u001e\u0003\u001e"
        + "\u0253\b\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0257\b\u001e\u0003"
        + "\u001e\u0259\b\u001e\u0003\u001e\u025b\b\u001e\u0003\u001e\u025d\b\u001e"
        + "\u0001\u001e\u0003\u001e\u0260\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f"
        + "\u0005\u001f\u0265\b\u001f\n\u001f\f\u001f\u0268\t\u001f\u0001\u001f\u0003"
        + "\u001f\u026b\b\u001f\u0001 \u0001 \u0001 \u0005 \u0270\b \n \f \u0273"
        + "\t \u0001 \u0003 \u0276\b \u0001!\u0001!\u0001!\u0001!\u0003!\u027c\b"
        + "!\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"
        + "\"\u0003\"\u0287\b\"\u0001\"\u0003\"\u028a\b\"\u0001\"\u0003\"\u028d\b"
        + "\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0005\"\u0295\b\"\n"
        + "\"\f\"\u0298\t\"\u0001#\u0001#\u0001#\u0001#\u0001#\u0005#\u029f\b#\n"
        + "#\f#\u02a2\t#\u0001#\u0001#\u0001#\u0005#\u02a7\b#\n#\f#\u02aa\t#\u0003"
        + "#\u02ac\b#\u0001$\u0001$\u0001$\u0001$\u0001$\u0005$\u02b3\b$\n$\f$\u02b6"
        + "\t$\u0001$\u0001$\u0003$\u02ba\b$\u0001%\u0001%\u0001&\u0001&\u0001&\u0001"
        + "\'\u0001\'\u0001\'\u0005\'\u02c4\b\'\n\'\f\'\u02c7\t\'\u0001(\u0001(\u0001"
        + "(\u0003(\u02cc\b(\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001"
        + ")\u0001)\u0001)\u0005)\u02d8\b)\n)\f)\u02db\t)\u0001)\u0001)\u0003)\u02df"
        + "\b)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001"
        + ")\u0001)\u0001)\u0001)\u0001)\u0005)\u02ef\b)\n)\f)\u02f2\t)\u0001)\u0001"
        + ")\u0001)\u0001)\u0001)\u0003)\u02f9\b)\u0003)\u02fb\b)\u0001*\u0001*\u0001"
        + "*\u0001*\u0001*\u0001*\u0001*\u0001*\u0003*\u0305\b*\u0001*\u0001*\u0001"
        + "*\u0001*\u0001*\u0001*\u0001*\u0001*\u0003*\u030f\b*\u0001*\u0001*\u0001"
        + "*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0003"
        + "*\u031d\b*\u0001*\u0001*\u0001*\u0003*\u0322\b*\u0001*\u0001*\u0003*\u0326"
        + "\b*\u0001*\u0001*\u0003*\u032a\b*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001"
        + "*\u0001*\u0001*\u0001*\u0003*\u0335\b*\u0001*\u0001*\u0001*\u0001*\u0001"
        + "*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001"
        + "*\u0001*\u0003*\u0347\b*\u0003*\u0349\b*\u0001+\u0001+\u0001+\u0003+\u034e"
        + "\b+\u0001,\u0001,\u0003,\u0352\b,\u0001-\u0001-\u0001-\u0001-\u0001-\u0001"
        + "-\u0001-\u0001-\u0001-\u0001-\u0003-\u035e\b-\u0001.\u0001.\u0001.\u0001"
        + ".\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001"
        + ".\u0003.\u036e\b.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001"
        + ".\u0003.\u0378\b.\u0001.\u0003.\u037b\b.\u0001.\u0003.\u037e\b.\u0001"
        + ".\u0001.\u0003.\u0382\b.\u0001.\u0001.\u0003.\u0386\b.\u0001.\u0001.\u0001"
        + ".\u0001.\u0001.\u0001.\u0003.\u038e\b.\u0003.\u0390\b.\u0003.\u0392\b"
        + ".\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0005.\u039a\b.\n.\f.\u039d"
        + "\t.\u0001/\u0001/\u00010\u00010\u00011\u00011\u00011\u00051\u03a6\b1\n"
        + "1\f1\u03a9\t1\u00011\u00031\u03ac\b1\u00011\u00031\u03af\b1\u00011\u0003"
        + "1\u03b2\b1\u00012\u00012\u00012\u00012\u00012\u00013\u00013\u00053\u03bb"
        + "\b3\n3\f3\u03be\t3\u00013\u00013\u00013\u00053\u03c3\b3\n3\f3\u03c6\t"
        + "3\u00013\u00013\u00053\u03ca\b3\n3\f3\u03cd\t3\u00033\u03cf\b3\u00014"
        + "\u00014\u00014\u00014\u00015\u00015\u00016\u00016\u00016\u00016\u0001"
        + "6\u00017\u00017\u00017\u00017\u00017\u00018\u00038\u03e2\b8\u00018\u0001"
        + "8\u00018\u00018\u00019\u00019\u00019\u00019\u00019\u00019\u00019\u0001"
        + "9\u00039\u03f0\b9\u0001:\u0001:\u0001:\u0001;\u0001;\u0001;\u0001;\u0001"
        + ";\u0005;\u03fa\b;\n;\f;\u03fd\t;\u0001;\u0001;\u0001<\u0001<\u0001<\u0001"
        + "<\u0001<\u0001<\u0001<\u0001<\u0001<\u0003<\u040a\b<\u0003<\u040c\b<\u0001"
        + "<\u0001<\u0001<\u0003<\u0411\b<\u0001=\u0001=\u0001=\u0001=\u0001=\u0001"
        + "=\u0001=\u0005=\u041a\b=\n=\f=\u041d\t=\u0001>\u0001>\u0001?\u0001?\u0001"
        + "?\u0005?\u0424\b?\n?\f?\u0427\t?\u0001@\u0001@\u0001@\u0005@\u042c\b@"
        + "\n@\f@\u042f\t@\u0001A\u0001A\u0001A\u0003A\u0434\bA\u0001B\u0001B\u0001"
        + "B\u0001B\u0003B\u043a\bB\u0001C\u0001C\u0001C\u0005C\u043f\bC\nC\fC\u0442"
        + "\tC\u0001D\u0001D\u0001D\u0005D\u0447\bD\nD\fD\u044a\tD\u0001E\u0001E"
        + "\u0001E\u0005E\u044f\bE\nE\fE\u0452\tE\u0001F\u0001F\u0001F\u0005F\u0457"
        + "\bF\nF\fF\u045a\tF\u0001G\u0001G\u0001G\u0005G\u045f\bG\nG\fG\u0462\t"
        + "G\u0001H\u0001H\u0001H\u0001H\u0003H\u0468\bH\u0001H\u0005H\u046b\bH\n"
        + "H\fH\u046e\tH\u0001I\u0001I\u0001I\u0001J\u0001J\u0001J\u0003J\u0476\b"
        + "J\u0001K\u0001K\u0005K\u047a\bK\nK\fK\u047d\tK\u0001L\u0001L\u0001L\u0001"
        + "L\u0001L\u0001L\u0005L\u0485\bL\nL\fL\u0488\tL\u0003L\u048a\bL\u0001L"
        + "\u0003L\u048d\bL\u0001L\u0001L\u0001L\u0001L\u0003L\u0493\bL\u0001M\u0001"
        + "M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"
        + "M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0005M\u04a7\bM\nM\fM\u04aa"
        + "\tM\u0001M\u0003M\u04ad\bM\u0003M\u04af\bM\u0001M\u0001M\u0001M\u0001"
        + "M\u0001M\u0005M\u04b6\bM\nM\fM\u04b9\tM\u0001M\u0003M\u04bc\bM\u0003M"
        + "\u04be\bM\u0001M\u0003M\u04c1\bM\u0001N\u0001N\u0001N\u0001N\u0001O\u0001"
        + "O\u0001P\u0001P\u0001Q\u0001Q\u0001Q\u0000\u0002D\\R\u0000\u0002\u0004"
        + "\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \""
        + "$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086"
        + "\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e"
        + "\u00a0\u00a2\u0000\u0010\u0002\u0000&&jj\u0003\u0000\u0001\u0003\u0016"
        + "\u0016vv\u0002\u0000\u0010\u0011ii\u0001\u0000`c\u0001\u0000gh\u0002\u0000"
        + "TY[_\u0001\u0000CD\u0001\u00002=\u0003\u0000),FFmm\u0001\u0000RS\u0007"
        + "\u0000\u0001\u000b\r\u0015TU[^bdfhvv\u0001\u0000\u0006\u0007\u0001\u0000"
        + "\"$\u0002\u0000eeqq\u0003\u0000!!eeqq\n\u0000\u0001\u000b\r\u0015%244"
        + "66::<<>MOmvv\u0549\u0000\u00a5\u0001\u0000\u0000\u0000\u0002\u00c2\u0001"
        + "\u0000\u0000\u0000\u0004\u00ff\u0001\u0000\u0000\u0000\u0006\u0101\u0001"
        + "\u0000\u0000\u0000\b\u010a\u0001\u0000\u0000\u0000\n\u0115\u0001\u0000"
        + "\u0000\u0000\f\u0124\u0001\u0000\u0000\u0000\u000e\u012c\u0001\u0000\u0000"
        + "\u0000\u0010\u0132\u0001\u0000\u0000\u0000\u0012\u0136\u0001\u0000\u0000"
        + "\u0000\u0014\u0147\u0001\u0000\u0000\u0000\u0016\u0149\u0001\u0000\u0000"
        + "\u0000\u0018\u014d\u0001\u0000\u0000\u0000\u001a\u014f\u0001\u0000\u0000"
        + "\u0000\u001c\u017a\u0001\u0000\u0000\u0000\u001e\u018d\u0001\u0000\u0000"
        + "\u0000 \u0192\u0001\u0000\u0000\u0000\"\u01a4\u0001\u0000\u0000\u0000"
        + "$\u01c5\u0001\u0000\u0000\u0000&\u01c7\u0001\u0000\u0000\u0000(\u01ed"
        + "\u0001\u0000\u0000\u0000*\u020d\u0001\u0000\u0000\u0000,\u020f\u0001\u0000"
        + "\u0000\u0000.\u0211\u0001\u0000\u0000\u00000\u0213\u0001\u0000\u0000\u0000"
        + "2\u022a\u0001\u0000\u0000\u00004\u0231\u0001\u0000\u0000\u00006\u0238"
        + "\u0001\u0000\u0000\u00008\u023d\u0001\u0000\u0000\u0000:\u0248\u0001\u0000"
        + "\u0000\u0000<\u025f\u0001\u0000\u0000\u0000>\u0261\u0001\u0000\u0000\u0000"
        + "@\u026c\u0001\u0000\u0000\u0000B\u027b\u0001\u0000\u0000\u0000D\u028c"
        + "\u0001\u0000\u0000\u0000F\u02ab\u0001\u0000\u0000\u0000H\u02b9\u0001\u0000"
        + "\u0000\u0000J\u02bb\u0001\u0000\u0000\u0000L\u02bd\u0001\u0000\u0000\u0000"
        + "N\u02c0\u0001\u0000\u0000\u0000P\u02c8\u0001\u0000\u0000\u0000R\u02fa"
        + "\u0001\u0000\u0000\u0000T\u0348\u0001\u0000\u0000\u0000V\u034d\u0001\u0000"
        + "\u0000\u0000X\u0351\u0001\u0000\u0000\u0000Z\u0353\u0001\u0000\u0000\u0000"
        + "\\\u0391\u0001\u0000\u0000\u0000^\u039e\u0001\u0000\u0000\u0000`\u03a0"
        + "\u0001\u0000\u0000\u0000b\u03a2\u0001\u0000\u0000\u0000d\u03b3\u0001\u0000"
        + "\u0000\u0000f\u03ce\u0001\u0000\u0000\u0000h\u03d0\u0001\u0000\u0000\u0000"
        + "j\u03d4\u0001\u0000\u0000\u0000l\u03d6\u0001\u0000\u0000\u0000n\u03db"
        + "\u0001\u0000\u0000\u0000p\u03e1\u0001\u0000\u0000\u0000r\u03e7\u0001\u0000"
        + "\u0000\u0000t\u03f1\u0001\u0000\u0000\u0000v\u03f4\u0001\u0000\u0000\u0000"
        + "x\u0400\u0001\u0000\u0000\u0000z\u0412\u0001\u0000\u0000\u0000|\u041e"
        + "\u0001\u0000\u0000\u0000~\u0420\u0001\u0000\u0000\u0000\u0080\u0428\u0001"
        + "\u0000\u0000\u0000\u0082\u0433\u0001\u0000\u0000\u0000\u0084\u0435\u0001"
        + "\u0000\u0000\u0000\u0086\u043b\u0001\u0000\u0000\u0000\u0088\u0443\u0001"
        + "\u0000\u0000\u0000\u008a\u044b\u0001\u0000\u0000\u0000\u008c\u0453\u0001"
        + "\u0000\u0000\u0000\u008e\u045b\u0001\u0000\u0000\u0000\u0090\u0463\u0001"
        + "\u0000\u0000\u0000\u0092\u046f\u0001\u0000\u0000\u0000\u0094\u0475\u0001"
        + "\u0000\u0000\u0000\u0096\u0477\u0001\u0000\u0000\u0000\u0098\u0492\u0001"
        + "\u0000\u0000\u0000\u009a\u04c0\u0001\u0000\u0000\u0000\u009c\u04c2\u0001"
        + "\u0000\u0000\u0000\u009e\u04c6\u0001\u0000\u0000\u0000\u00a0\u04c8\u0001"
        + "\u0000\u0000\u0000\u00a2\u04ca\u0001\u0000\u0000\u0000\u00a4\u00a6\u0003"
        + "\u001a\r\u0000\u00a5\u00a4\u0001\u0000\u0000\u0000\u00a5\u00a6\u0001\u0000"
        + "\u0000\u0000\u00a6\u00aa\u0001\u0000\u0000\u0000\u00a7\u00a9\u0003\u0002"
        + "\u0001\u0000\u00a8\u00a7\u0001\u0000\u0000\u0000\u00a9\u00ac\u0001\u0000"
        + "\u0000\u0000\u00aa\u00a8\u0001\u0000\u0000\u0000\u00aa\u00ab\u0001\u0000"
        + "\u0000\u0000\u00ab\u00b6\u0001\u0000\u0000\u0000\u00ac\u00aa\u0001\u0000"
        + "\u0000\u0000\u00ad\u00b0\u0003l6\u0000\u00ae\u00b0\u0003n7\u0000\u00af"
        + "\u00ad\u0001\u0000\u0000\u0000\u00af\u00ae\u0001\u0000\u0000\u0000\u00b0"
        + "\u00b2\u0001\u0000\u0000\u0000\u00b1\u00b3\u0005\u001c\u0000\u0000\u00b2"
        + "\u00b1\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000\u0000\u00b3"
        + "\u00b5\u0001\u0000\u0000\u0000\u00b4\u00af\u0001\u0000\u0000\u0000\u00b5"
        + "\u00b8\u0001\u0000\u0000\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b6"
        + "\u00b7\u0001\u0000\u0000\u0000\u00b7\u00bd\u0001\u0000\u0000\u0000\u00b8"
        + "\u00b6\u0001\u0000\u0000\u0000\u00b9\u00bc\u0003\u0004\u0002\u0000\u00ba"
        + "\u00bc\u0003\u001c\u000e\u0000\u00bb\u00b9\u0001\u0000\u0000\u0000\u00bb"
        + "\u00ba\u0001\u0000\u0000\u0000\u00bc\u00bf\u0001\u0000\u0000\u0000\u00bd"
        + "\u00bb\u0001\u0000\u0000\u0000\u00bd\u00be\u0001\u0000\u0000\u0000\u00be"
        + "\u00c0\u0001\u0000\u0000\u0000\u00bf\u00bd\u0001\u0000\u0000\u0000\u00c0"
        + "\u00c1\u0005\u0000\u0000\u0001\u00c1\u0001\u0001\u0000\u0000\u0000\u00c2"
        + "\u00c3\u0005\u0001\u0000\u0000\u00c3\u00c4\u0003j5\u0000\u00c4\u00c5\u0005"
        + ">\u0000\u0000\u00c5\u00c6\u0003\u00a0P\u0000\u00c6\u00c7\u0005\u0002\u0000"
        + "\u0000\u00c7\u00c8\u0005u\u0000\u0000\u00c8\u00c9\u0005\u0003\u0000\u0000"
        + "\u00c9\u00cb\u0003\u00a0P\u0000\u00ca\u00cc\u0005\u001c\u0000\u0000\u00cb"
        + "\u00ca\u0001\u0000\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000\u0000\u00cc"
        + "\u0003\u0001\u0000\u0000\u0000\u00cd\u00ce\u0005\t\u0000\u0000\u00ce\u00cf"
        + "\u0003\u0006\u0003\u0000\u00cf\u00d8\u0005r\u0000\u0000\u00d0\u00d5\u0003"
        + "\b\u0004\u0000\u00d1\u00d2\u0005n\u0000\u0000\u00d2\u00d4\u0003\b\u0004"
        + "\u0000\u00d3\u00d1\u0001\u0000\u0000\u0000\u00d4\u00d7\u0001\u0000\u0000"
        + "\u0000\u00d5\u00d3\u0001\u0000\u0000\u0000\u00d5\u00d6\u0001\u0000\u0000"
        + "\u0000\u00d6\u00d9\u0001\u0000\u0000\u0000\u00d7\u00d5\u0001\u0000\u0000"
        + "\u0000\u00d8\u00d0\u0001\u0000\u0000\u0000\u00d8\u00d9\u0001\u0000\u0000"
        + "\u0000\u00d9\u00da\u0001\u0000\u0000\u0000\u00da\u00db\u0005s\u0000\u0000"
        + "\u00db\u00dc\u0005\n\u0000\u0000\u00dc\u00e5\u0003\f\u0006\u0000\u00dd"
        + "\u00de\u0005j\u0000\u0000\u00de\u00df\u0003 \u0010\u0000\u00df\u00e0\u0005"
        + "k\u0000\u0000\u00e0\u00e6\u0001\u0000\u0000\u0000\u00e1\u00e2\u0005\u0019"
        + "\u0000\u0000\u00e2\u00e3\u0003 \u0010\u0000\u00e3\u00e4\u0005\u001a\u0000"
        + "\u0000\u00e4\u00e6\u0001\u0000\u0000\u0000\u00e5\u00dd\u0001\u0000\u0000"
        + "\u0000\u00e5\u00e1\u0001\u0000\u0000\u0000\u00e6\u0100\u0001\u0000\u0000"
        + "\u0000\u00e7\u00e8\u0003\f\u0006\u0000\u00e8\u00e9\u0003\u0006\u0003\u0000"
        + "\u00e9\u00f2\u0005r\u0000\u0000\u00ea\u00ef\u0003\b\u0004\u0000\u00eb"
        + "\u00ec\u0005n\u0000\u0000\u00ec\u00ee\u0003\b\u0004\u0000\u00ed\u00eb"
        + "\u0001\u0000\u0000\u0000\u00ee\u00f1\u0001\u0000\u0000\u0000\u00ef\u00ed"
        + "\u0001\u0000\u0000\u0000\u00ef\u00f0\u0001\u0000\u0000\u0000\u00f0\u00f3"
        + "\u0001\u0000\u0000\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f2\u00ea"
        + "\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3\u00f4"
        + "\u0001\u0000\u0000\u0000\u00f4\u00fd\u0005s\u0000\u0000\u00f5\u00f6\u0007"
        + "\u0000\u0000\u0000\u00f6\u00f7\u0003 \u0010\u0000\u00f7\u00f8\u0005k\u0000"
        + "\u0000\u00f8\u00fe\u0001\u0000\u0000\u0000\u00f9\u00fa\u0005\u0019\u0000"
        + "\u0000\u00fa\u00fb\u0003 \u0010\u0000\u00fb\u00fc\u0005\u001a\u0000\u0000"
        + "\u00fc\u00fe\u0001\u0000\u0000\u0000\u00fd\u00f5\u0001\u0000\u0000\u0000"
        + "\u00fd\u00f9\u0001\u0000\u0000\u0000\u00fe\u0100\u0001\u0000\u0000\u0000"
        + "\u00ff\u00cd\u0001\u0000\u0000\u0000\u00ff\u00e7\u0001\u0000\u0000\u0000"
        + "\u0100\u0005\u0001\u0000\u0000\u0000\u0101\u0106\u0003\u009eO\u0000\u0102"
        + "\u0103\u0005\u001b\u0000\u0000\u0103\u0105\u0003\u009eO\u0000\u0104\u0102"
        + "\u0001\u0000\u0000\u0000\u0105\u0108\u0001\u0000\u0000\u0000\u0106\u0104"
        + "\u0001\u0000\u0000\u0000\u0106\u0107\u0001\u0000\u0000\u0000\u0107\u0007"
        + "\u0001\u0000\u0000\u0000\u0108\u0106\u0001\u0000\u0000\u0000\u0109\u010b"
        + "\u0005\f\u0000\u0000\u010a\u0109\u0001\u0000\u0000\u0000\u010a\u010b\u0001"
        + "\u0000\u0000\u0000\u010b\u0113\u0001\u0000\u0000\u0000\u010c\u010d\u0003"
        + "\n\u0005\u0000\u010d\u010e\u0005o\u0000\u0000\u010e\u010f\u0003\f\u0006"
        + "\u0000\u010f\u0114\u0001\u0000\u0000\u0000\u0110\u0111\u0003\f\u0006\u0000"
        + "\u0111\u0112\u0003\n\u0005\u0000\u0112\u0114\u0001\u0000\u0000\u0000\u0113"
        + "\u010c\u0001\u0000\u0000\u0000\u0113\u0110\u0001\u0000\u0000\u0000\u0114"
        + "\t\u0001\u0000\u0000\u0000\u0115\u0116\u0007\u0001\u0000\u0000\u0116\u000b"
        + "\u0001\u0000\u0000\u0000\u0117\u0125\u0003\u009eO\u0000\u0118\u0121\u0005"
        + "\u0019\u0000\u0000\u0119\u011e\u0003\u0010\b\u0000\u011a\u011b\u0005n"
        + "\u0000\u0000\u011b\u011d\u0003\u0010\b\u0000\u011c\u011a\u0001\u0000\u0000"
        + "\u0000\u011d\u0120\u0001\u0000\u0000\u0000\u011e\u011c\u0001\u0000\u0000"
        + "\u0000\u011e\u011f\u0001\u0000\u0000\u0000\u011f\u0122\u0001\u0000\u0000"
        + "\u0000\u0120\u011e\u0001\u0000\u0000\u0000\u0121\u0119\u0001\u0000\u0000"
        + "\u0000\u0121\u0122\u0001\u0000\u0000\u0000\u0122\u0123\u0001\u0000\u0000"
        + "\u0000\u0123\u0125\u0005\u001a\u0000\u0000\u0124\u0117\u0001\u0000\u0000"
        + "\u0000\u0124\u0118\u0001\u0000\u0000\u0000\u0125\u0129\u0001\u0000\u0000"
        + "\u0000\u0126\u0128\u0003\u000e\u0007\u0000\u0127\u0126\u0001\u0000\u0000"
        + "\u0000\u0128\u012b\u0001\u0000\u0000\u0000\u0129\u0127\u0001\u0000\u0000"
        + "\u0000\u0129\u012a\u0001\u0000\u0000\u0000\u012a\r\u0001\u0000\u0000\u0000"
        + "\u012b\u0129\u0001\u0000\u0000\u0000\u012c\u012e\u0005\u0017\u0000\u0000"
        + "\u012d\u012f\u0005u\u0000\u0000\u012e\u012d\u0001\u0000\u0000\u0000\u012e"
        + "\u012f\u0001\u0000\u0000\u0000\u012f\u0130\u0001\u0000\u0000\u0000\u0130"
        + "\u0131\u0005\u0018\u0000\u0000\u0131\u000f\u0001\u0000\u0000\u0000\u0132"
        + "\u0133\u0003\u009eO\u0000\u0133\u0134\u0005o\u0000\u0000\u0134\u0135\u0003"
        + "\f\u0006\u0000\u0135\u0011\u0001\u0000\u0000\u0000\u0136\u0137\u0003\u0006"
        + "\u0003\u0000\u0137\u0140\u0005r\u0000\u0000\u0138\u013d\u0003\u0014\n"
        + "\u0000\u0139\u013a\u0005n\u0000\u0000\u013a\u013c\u0003\u0014\n\u0000"
        + "\u013b\u0139\u0001\u0000\u0000\u0000\u013c\u013f\u0001\u0000\u0000\u0000"
        + "\u013d\u013b\u0001\u0000\u0000\u0000\u013d\u013e\u0001\u0000\u0000\u0000"
        + "\u013e\u0141\u0001\u0000\u0000\u0000\u013f\u013d\u0001\u0000\u0000\u0000"
        + "\u0140\u0138\u0001\u0000\u0000\u0000\u0140\u0141\u0001\u0000\u0000\u0000"
        + "\u0141\u0142\u0001\u0000\u0000\u0000\u0142\u0143\u0005s\u0000\u0000\u0143"
        + "\u0013\u0001\u0000\u0000\u0000\u0144\u0145\u0005\f\u0000\u0000\u0145\u0148"
        + "\u0003z=\u0000\u0146\u0148\u0003|>\u0000\u0147\u0144\u0001\u0000\u0000"
        + "\u0000\u0147\u0146\u0001\u0000\u0000\u0000\u0148\u0015\u0001\u0000\u0000"
        + "\u0000\u0149\u014b\u0005\u000b\u0000\u0000\u014a\u014c\u0003|>\u0000\u014b"
        + "\u014a\u0001\u0000\u0000\u0000\u014b\u014c\u0001\u0000\u0000\u0000\u014c"
        + "\u0017\u0001\u0000\u0000\u0000\u014d\u014e\u0003\u0012\t\u0000\u014e\u0019"
        + "\u0001\u0000\u0000\u0000\u014f\u0150\u0005l\u0000\u0000\u0150\u0151\u0003"
        + "\u00a0P\u0000\u0151\u001b\u0001\u0000\u0000\u0000\u0152\u0153\u0005m\u0000"
        + "\u0000\u0153\u015c\u0003\u001e\u000f\u0000\u0154\u0155\u0005j\u0000\u0000"
        + "\u0155\u0156\u0003 \u0010\u0000\u0156\u0157\u0005k\u0000\u0000\u0157\u015d"
        + "\u0001\u0000\u0000\u0000\u0158\u0159\u0005\u0019\u0000\u0000\u0159\u015a"
        + "\u0003 \u0010\u0000\u015a\u015b\u0005\u001a\u0000\u0000\u015b\u015d\u0001"
        + "\u0000\u0000\u0000\u015c\u0154\u0001\u0000\u0000\u0000\u015c\u0158\u0001"
        + "\u0000\u0000\u0000\u015d\u017b\u0001\u0000\u0000\u0000\u015e\u015f\u0005"
        + "m\u0000\u0000\u015f\u0160\u0005f\u0000\u0000\u0160\u0169\u0005i\u0000"
        + "\u0000\u0161\u0162\u0005j\u0000\u0000\u0162\u0163\u0003 \u0010\u0000\u0163"
        + "\u0164\u0005k\u0000\u0000\u0164\u016a\u0001\u0000\u0000\u0000\u0165\u0166"
        + "\u0005\u0019\u0000\u0000\u0166\u0167\u0003 \u0010\u0000\u0167\u0168\u0005"
        + "\u001a\u0000\u0000\u0168\u016a\u0001\u0000\u0000\u0000\u0169\u0161\u0001"
        + "\u0000\u0000\u0000\u0169\u0165\u0001\u0000\u0000\u0000\u016a\u017b\u0001"
        + "\u0000\u0000\u0000\u016b\u016c\u0005m\u0000\u0000\u016c\u016d\u0005f\u0000"
        + "\u0000\u016d\u016e\u0007\u0002\u0000\u0000\u016e\u016f\u0005>\u0000\u0000"
        + "\u016f\u0178\u0003b1\u0000\u0170\u0171\u0005j\u0000\u0000\u0171\u0172"
        + "\u0003 \u0010\u0000\u0172\u0173\u0005k\u0000\u0000\u0173\u0179\u0001\u0000"
        + "\u0000\u0000\u0174\u0175\u0005\u0019\u0000\u0000\u0175\u0176\u0003 \u0010"
        + "\u0000\u0176\u0177\u0005\u001a\u0000\u0000\u0177\u0179\u0001\u0000\u0000"
        + "\u0000\u0178\u0170\u0001\u0000\u0000\u0000\u0178\u0174\u0001\u0000\u0000"
        + "\u0000\u0179\u017b\u0001\u0000\u0000\u0000\u017a\u0152\u0001\u0000\u0000"
        + "\u0000\u017a\u015e\u0001\u0000\u0000\u0000\u017a\u016b\u0001\u0000\u0000"
        + "\u0000\u017b\u001d\u0001\u0000\u0000\u0000\u017c\u017e\u0005u\u0000\u0000"
        + "\u017d\u017c\u0001\u0000\u0000\u0000\u017d\u017e\u0001\u0000\u0000\u0000"
        + "\u017e\u0180\u0001\u0000\u0000\u0000\u017f\u0181\u0005d\u0000\u0000\u0180"
        + "\u017f\u0001\u0000\u0000\u0000\u0180\u0181\u0001\u0000\u0000\u0000\u0181"
        + "\u0184\u0001\u0000\u0000\u0000\u0182\u0183\u0005e\u0000\u0000\u0183\u0185"
        + "\u0005u\u0000\u0000\u0184\u0182\u0001\u0000\u0000\u0000\u0184\u0185\u0001"
        + "\u0000\u0000\u0000\u0185\u0186\u0001\u0000\u0000\u0000\u0186\u018e\u0007"
        + "\u0003\u0000\u0000\u0187\u018a\u0005t\u0000\u0000\u0188\u0189\u0005e\u0000"
        + "\u0000\u0189\u018b\u0005u\u0000\u0000\u018a\u0188\u0001\u0000\u0000\u0000"
        + "\u018a\u018b\u0001\u0000\u0000\u0000\u018b\u018c\u0001\u0000\u0000\u0000"
        + "\u018c\u018e\u0007\u0003\u0000\u0000\u018d\u017d\u0001\u0000\u0000\u0000"
        + "\u018d\u0187\u0001\u0000\u0000\u0000\u018e\u001f\u0001\u0000\u0000\u0000"
        + "\u018f\u0191\u0003\"\u0011\u0000\u0190\u018f\u0001\u0000\u0000\u0000\u0191"
        + "\u0194\u0001\u0000\u0000\u0000\u0192\u0190\u0001\u0000\u0000\u0000\u0192"
        + "\u0193\u0001\u0000\u0000\u0000\u0193!\u0001\u0000\u0000\u0000\u0194\u0192"
        + "\u0001\u0000\u0000\u0000\u0195\u01a5\u0003r9\u0000\u0196\u01a5\u0003t"
        + ":\u0000\u0197\u01a5\u0003n7\u0000\u0198\u01a5\u0003p8\u0000\u0199\u01a5"
        + "\u0003v;\u0000\u019a\u01a5\u0003x<\u0000\u019b\u01a5\u0003\u0016\u000b"
        + "\u0000\u019c\u01a5\u0003(\u0014\u0000\u019d\u01a5\u0003$\u0012\u0000\u019e"
        + "\u01a5\u0003*\u0015\u0000\u019f\u01a5\u0003R)\u0000\u01a0\u01a5\u0003"
        + "T*\u0000\u01a1\u01a5\u0003Z-\u0000\u01a2\u01a5\u0003&\u0013\u0000\u01a3"
        + "\u01a5\u0003\u0018\f\u0000\u01a4\u0195\u0001\u0000\u0000\u0000\u01a4\u0196"
        + "\u0001\u0000\u0000\u0000\u01a4\u0197\u0001\u0000\u0000\u0000\u01a4\u0198"
        + "\u0001\u0000\u0000\u0000\u01a4\u0199\u0001\u0000\u0000\u0000\u01a4\u019a"
        + "\u0001\u0000\u0000\u0000\u01a4\u019b\u0001\u0000\u0000\u0000\u01a4\u019c"
        + "\u0001\u0000\u0000\u0000\u01a4\u019d\u0001\u0000\u0000\u0000\u01a4\u019e"
        + "\u0001\u0000\u0000\u0000\u01a4\u019f\u0001\u0000\u0000\u0000\u01a4\u01a0"
        + "\u0001\u0000\u0000\u0000\u01a4\u01a1\u0001\u0000\u0000\u0000\u01a4\u01a2"
        + "\u0001\u0000\u0000\u0000\u01a4\u01a3\u0001\u0000\u0000\u0000\u01a5\u01a7"
        + "\u0001\u0000\u0000\u0000\u01a6\u01a8\u0005\u001c\u0000\u0000\u01a7\u01a6"
        + "\u0001\u0000\u0000\u0000\u01a7\u01a8\u0001\u0000\u0000\u0000\u01a8#\u0001"
        + "\u0000\u0000\u0000\u01a9\u01ab\u0005A\u0000\u0000\u01aa\u01ac\u00036\u001b"
        + "\u0000\u01ab\u01aa\u0001\u0000\u0000\u0000\u01ab\u01ac\u0001\u0000\u0000"
        + "\u0000\u01ac\u01ad\u0001\u0000\u0000\u0000\u01ad\u01ae\u0005f\u0000\u0000"
        + "\u01ae\u01b0\u0007\u0004\u0000\u0000\u01af\u01b1\u00036\u001b\u0000\u01b0"
        + "\u01af\u0001\u0000\u0000\u0000\u01b0\u01b1\u0001\u0000\u0000\u0000\u01b1"
        + "\u01b2\u0001\u0000\u0000\u0000\u01b2\u01b4\u0005?\u0000\u0000\u01b3\u01b5"
        + "\u0005F\u0000\u0000\u01b4\u01b3\u0001\u0000\u0000\u0000\u01b4\u01b5\u0001"
        + "\u0000\u0000\u0000\u01b5\u01b6\u0001\u0000\u0000\u0000\u01b6\u01c6\u0003"
        + "b1\u0000\u01b7\u01b9\u0005?\u0000\u0000\u01b8\u01ba\u0005F\u0000\u0000"
        + "\u01b9\u01b8\u0001\u0000\u0000\u0000\u01b9\u01ba\u0001\u0000\u0000\u0000"
        + "\u01ba\u01bb\u0001\u0000\u0000\u0000\u01bb\u01bc\u0003b1\u0000\u01bc\u01be"
        + "\u0005A\u0000\u0000\u01bd\u01bf\u00036\u001b\u0000\u01be\u01bd\u0001\u0000"
        + "\u0000\u0000\u01be\u01bf\u0001\u0000\u0000\u0000\u01bf\u01c0\u0001\u0000"
        + "\u0000\u0000\u01c0\u01c1\u0005f\u0000\u0000\u01c1\u01c3\u0007\u0004\u0000"
        + "\u0000\u01c2\u01c4\u00036\u001b\u0000\u01c3\u01c2\u0001\u0000\u0000\u0000"
        + "\u01c3\u01c4\u0001\u0000\u0000\u0000\u01c4\u01c6\u0001\u0000\u0000\u0000"
        + "\u01c5\u01a9\u0001\u0000\u0000\u0000\u01c5\u01b7\u0001\u0000\u0000\u0000"
        + "\u01c6%\u0001\u0000\u0000\u0000\u01c7\u01c9\u0005H\u0000\u0000\u01c8\u01ca"
        + "\u0003f3\u0000\u01c9\u01c8\u0001\u0000\u0000\u0000\u01c9\u01ca\u0001\u0000"
        + "\u0000\u0000\u01ca\u01cf\u0001\u0000\u0000\u0000\u01cb\u01cc\u0005n\u0000"
        + "\u0000\u01cc\u01ce\u0003f3\u0000\u01cd\u01cb\u0001\u0000\u0000\u0000\u01ce"
        + "\u01d1\u0001\u0000\u0000\u0000\u01cf\u01cd\u0001\u0000\u0000\u0000\u01cf"
        + "\u01d0\u0001\u0000\u0000\u0000\u01d0\u01d3\u0001\u0000\u0000\u0000\u01d1"
        + "\u01cf\u0001\u0000\u0000\u0000\u01d2\u01d4\u0005n\u0000\u0000\u01d3\u01d2"
        + "\u0001\u0000\u0000\u0000\u01d3\u01d4\u0001\u0000\u0000\u0000\u01d4\'\u0001"
        + "\u0000\u0000\u0000\u01d5\u01d7\u0005@\u0000\u0000\u01d6\u01d8\u0003,\u0016"
        + "\u0000\u01d7\u01d6\u0001\u0000\u0000\u0000\u01d7\u01d8\u0001\u0000\u0000"
        + "\u0000\u01d8\u01da\u0001\u0000\u0000\u0000\u01d9\u01db\u0003:\u001d\u0000"
        + "\u01da\u01d9\u0001\u0000\u0000\u0000\u01da\u01db\u0001\u0000\u0000\u0000"
        + "\u01db\u01dc\u0001\u0000\u0000\u0000\u01dc\u01de\u0005>\u0000\u0000\u01dd"
        + "\u01df\u0005F\u0000\u0000\u01de\u01dd\u0001\u0000\u0000\u0000\u01de\u01df"
        + "\u0001\u0000\u0000\u0000\u01df\u01e0\u0001\u0000\u0000\u0000\u01e0\u01ee"
        + "\u0003b1\u0000\u01e1\u01e3\u0005>\u0000\u0000\u01e2\u01e4\u0005F\u0000"
        + "\u0000\u01e3\u01e2\u0001\u0000\u0000\u0000\u01e3\u01e4\u0001\u0000\u0000"
        + "\u0000\u01e4\u01e5\u0001\u0000\u0000\u0000\u01e5\u01e6\u0003b1\u0000\u01e6"
        + "\u01e8\u0005@\u0000\u0000\u01e7\u01e9\u0003,\u0016\u0000\u01e8\u01e7\u0001"
        + "\u0000\u0000\u0000\u01e8\u01e9\u0001\u0000\u0000\u0000\u01e9\u01eb\u0001"
        + "\u0000\u0000\u0000\u01ea\u01ec\u0003:\u001d\u0000\u01eb\u01ea\u0001\u0000"
        + "\u0000\u0000\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec\u01ee\u0001\u0000"
        + "\u0000\u0000\u01ed\u01d5\u0001\u0000\u0000\u0000\u01ed\u01e1\u0001\u0000"
        + "\u0000\u0000\u01ee)\u0001\u0000\u0000\u0000\u01ef\u01f1\u0005A\u0000\u0000"
        + "\u01f0\u01f2\u0003.\u0017\u0000\u01f1\u01f0\u0001\u0000\u0000\u0000\u01f1"
        + "\u01f2\u0001\u0000\u0000\u0000\u01f2\u01f4\u0001\u0000\u0000\u0000\u01f3"
        + "\u01f5\u0003:\u001d\u0000\u01f4\u01f3\u0001\u0000\u0000\u0000\u01f4\u01f5"
        + "\u0001\u0000\u0000\u0000\u01f5\u01f6\u0001\u0000\u0000\u0000\u01f6\u01f8"
        + "\u0005?\u0000\u0000\u01f7\u01f9\u0003h4\u0000\u01f8\u01f7\u0001\u0000"
        + "\u0000\u0000\u01f8\u01f9\u0001\u0000\u0000\u0000\u01f9\u01fb\u0001\u0000"
        + "\u0000\u0000\u01fa\u01fc\u0005F\u0000\u0000\u01fb\u01fa\u0001\u0000\u0000"
        + "\u0000\u01fb\u01fc\u0001\u0000\u0000\u0000\u01fc\u01fd\u0001\u0000\u0000"
        + "\u0000\u01fd\u020e\u0003b1\u0000\u01fe\u0200\u0005?\u0000\u0000\u01ff"
        + "\u0201\u0003h4\u0000\u0200\u01ff\u0001\u0000\u0000\u0000\u0200\u0201\u0001"
        + "\u0000\u0000\u0000\u0201\u0203\u0001\u0000\u0000\u0000\u0202\u0204\u0005"
        + "F\u0000\u0000\u0203\u0202\u0001\u0000\u0000\u0000\u0203\u0204\u0001\u0000"
        + "\u0000\u0000\u0204\u0205\u0001\u0000\u0000\u0000\u0205\u0206\u0003b1\u0000"
        + "\u0206\u0208\u0005A\u0000\u0000\u0207\u0209\u0003.\u0017\u0000\u0208\u0207"
        + "\u0001\u0000\u0000\u0000\u0208\u0209\u0001\u0000\u0000\u0000\u0209\u020b"
        + "\u0001\u0000\u0000\u0000\u020a\u020c\u0003:\u001d\u0000\u020b\u020a\u0001"
        + "\u0000\u0000\u0000\u020b\u020c\u0001\u0000\u0000\u0000\u020c\u020e\u0001"
        + "\u0000\u0000\u0000\u020d\u01ef\u0001\u0000\u0000\u0000\u020d\u01fe\u0001"
        + "\u0000\u0000\u0000\u020e+\u0001\u0000\u0000\u0000\u020f\u0210\u00030\u0018"
        + "\u0000\u0210-\u0001\u0000\u0000\u0000\u0211\u0212\u00030\u0018\u0000\u0212"
        + "/\u0001\u0000\u0000\u0000\u0213\u0218\u00032\u0019\u0000\u0214\u0215\u0005"
        + "n\u0000\u0000\u0215\u0217\u00032\u0019\u0000\u0216\u0214\u0001\u0000\u0000"
        + "\u0000\u0217\u021a\u0001\u0000\u0000\u0000\u0218\u0216\u0001\u0000\u0000"
        + "\u0000\u0218\u0219\u0001\u0000\u0000\u0000\u0219\u021c\u0001\u0000\u0000"
        + "\u0000\u021a\u0218\u0001\u0000\u0000\u0000\u021b\u021d\u0005n\u0000\u0000"
        + "\u021c\u021b\u0001\u0000\u0000\u0000\u021c\u021d\u0001\u0000\u0000\u0000"
        + "\u021d1\u0001\u0000\u0000\u0000\u021e\u0220\u00034\u001a\u0000\u021f\u021e"
        + "\u0001\u0000\u0000\u0000\u021f\u0220\u0001\u0000\u0000\u0000\u0220\u0221"
        + "\u0001\u0000\u0000\u0000\u0221\u0223\u0003@ \u0000\u0222\u0224\u0003B"
        + "!\u0000\u0223\u0222\u0001\u0000\u0000\u0000\u0223\u0224\u0001\u0000\u0000"
        + "\u0000\u0224\u022b\u0001\u0000\u0000\u0000\u0225\u0227\u00034\u001a\u0000"
        + "\u0226\u0228\u0003B!\u0000\u0227\u0226\u0001\u0000\u0000\u0000\u0227\u0228"
        + "\u0001\u0000\u0000\u0000\u0228\u022b\u0001\u0000\u0000\u0000\u0229\u022b"
        + "\u0003B!\u0000\u022a\u021f\u0001\u0000\u0000\u0000\u022a\u0225\u0001\u0000"
        + "\u0000\u0000\u022a\u0229\u0001\u0000\u0000\u0000\u022b3\u0001\u0000\u0000"
        + "\u0000\u022c\u022d\u00036\u001b\u0000\u022d\u022e\u00038\u001c\u0000\u022e"
        + "\u0232\u0001\u0000\u0000\u0000\u022f\u0232\u00038\u001c\u0000\u0230\u0232"
        + "\u00036\u001b\u0000\u0231\u022c\u0001\u0000\u0000\u0000\u0231\u022f\u0001"
        + "\u0000\u0000\u0000\u0231\u0230\u0001\u0000\u0000\u0000\u02325\u0001\u0000"
        + "\u0000\u0000\u0233\u0239\u0003\u00a2Q\u0000\u0234\u0235\u0005r\u0000\u0000"
        + "\u0235\u0236\u0003|>\u0000\u0236\u0237\u0005s\u0000\u0000\u0237\u0239"
        + "\u0001\u0000\u0000\u0000\u0238\u0233\u0001\u0000\u0000\u0000\u0238\u0234"
        + "\u0001\u0000\u0000\u0000\u0239\u023b\u0001\u0000\u0000\u0000\u023a\u023c"
        + "\u0005F\u0000\u0000\u023b\u023a\u0001\u0000\u0000\u0000\u023b\u023c\u0001"
        + "\u0000\u0000\u0000\u023c7\u0001\u0000\u0000\u0000\u023d\u0243\u0005E\u0000"
        + "\u0000\u023e\u0244\u0003\u00a2Q\u0000\u023f\u0240\u0005r\u0000\u0000\u0240"
        + "\u0241\u0003|>\u0000\u0241\u0242\u0005s\u0000\u0000\u0242\u0244\u0001"
        + "\u0000\u0000\u0000\u0243\u023e\u0001\u0000\u0000\u0000\u0243\u023f\u0001"
        + "\u0000\u0000\u0000\u0244\u0246\u0001\u0000\u0000\u0000\u0245\u0247\u0005"
        + "F\u0000\u0000\u0246\u0245\u0001\u0000\u0000\u0000\u0246\u0247\u0001\u0000"
        + "\u0000\u0000\u02479\u0001\u0000\u0000\u0000\u0248\u0249\u0005G\u0000\u0000"
        + "\u0249\u024a\u0003>\u001f\u0000\u024a;\u0001\u0000\u0000\u0000\u024b\u025c"
        + "\u0003j5\u0000\u024c\u024e\u0005o\u0000\u0000\u024d\u024f\u0003j5\u0000"
        + "\u024e\u024d\u0001\u0000\u0000\u0000\u024e\u024f\u0001\u0000\u0000\u0000"
        + "\u024f\u025a\u0001\u0000\u0000\u0000\u0250\u0252\u0005o\u0000\u0000\u0251"
        + "\u0253\u0003j5\u0000\u0252\u0251\u0001\u0000\u0000\u0000\u0252\u0253\u0001"
        + "\u0000\u0000\u0000\u0253\u0258\u0001\u0000\u0000\u0000\u0254\u0256\u0005"
        + "o\u0000\u0000\u0255\u0257\u0003j5\u0000\u0256\u0255\u0001\u0000\u0000"
        + "\u0000\u0256\u0257\u0001\u0000\u0000\u0000\u0257\u0259\u0001\u0000\u0000"
        + "\u0000\u0258\u0254\u0001\u0000\u0000\u0000\u0258\u0259\u0001\u0000\u0000"
        + "\u0000\u0259\u025b\u0001\u0000\u0000\u0000\u025a\u0250\u0001\u0000\u0000"
        + "\u0000\u025a\u025b\u0001\u0000\u0000\u0000\u025b\u025d\u0001\u0000\u0000"
        + "\u0000\u025c\u024c\u0001\u0000\u0000\u0000\u025c\u025d\u0001\u0000\u0000"
        + "\u0000\u025d\u0260\u0001\u0000\u0000\u0000\u025e\u0260\u0003\u00a0P\u0000"
        + "\u025f\u024b\u0001\u0000\u0000\u0000\u025f\u025e\u0001\u0000\u0000\u0000"
        + "\u0260=\u0001\u0000\u0000\u0000\u0261\u0266\u0003<\u001e\u0000\u0262\u0263"
        + "\u0005n\u0000\u0000\u0263\u0265\u0003<\u001e\u0000\u0264\u0262\u0001\u0000"
        + "\u0000\u0000\u0265\u0268\u0001\u0000\u0000\u0000\u0266\u0264\u0001\u0000"
        + "\u0000\u0000\u0266\u0267\u0001\u0000\u0000\u0000\u0267\u026a\u0001\u0000"
        + "\u0000\u0000\u0268\u0266\u0001\u0000\u0000\u0000\u0269\u026b\u0005n\u0000"
        + "\u0000\u026a\u0269\u0001\u0000\u0000\u0000\u026a\u026b\u0001\u0000\u0000"
        + "\u0000\u026b?\u0001\u0000\u0000\u0000\u026c\u0271\u0003<\u001e\u0000\u026d"
        + "\u026e\u00051\u0000\u0000\u026e\u0270\u0003<\u001e\u0000\u026f\u026d\u0001"
        + "\u0000\u0000\u0000\u0270\u0273\u0001\u0000\u0000\u0000\u0271\u026f\u0001"
        + "\u0000\u0000\u0000\u0271\u0272\u0001\u0000\u0000\u0000\u0272\u0275\u0001"
        + "\u0000\u0000\u0000\u0273\u0271\u0001\u0000\u0000\u0000\u0274\u0276\u0005"
        + "1\u0000\u0000\u0275\u0274\u0001\u0000\u0000\u0000\u0275\u0276\u0001\u0000"
        + "\u0000\u0000\u0276A\u0001\u0000\u0000\u0000\u0277\u0278\u0005L\u0000\u0000"
        + "\u0278\u027c\u0003D\"\u0000\u0279\u027a\u0005K\u0000\u0000\u027a\u027c"
        + "\u0003D\"\u0000\u027b\u0277\u0001\u0000\u0000\u0000\u027b\u0279\u0001"
        + "\u0000\u0000\u0000\u027cC\u0001\u0000\u0000\u0000\u027d\u027e\u0006\""
        + "\uffff\uffff\u0000\u027e\u027f\u0005r\u0000\u0000\u027f\u0280\u0003D\""
        + "\u0000\u0280\u0281\u0005s\u0000\u0000\u0281\u028d\u0001\u0000\u0000\u0000"
        + "\u0282\u0283\u0005/\u0000\u0000\u0283\u028d\u0003D\"\u0004\u0284\u0286"
        + "\u0005M\u0000\u0000\u0285\u0287\u0005N\u0000\u0000\u0286\u0285\u0001\u0000"
        + "\u0000\u0000\u0286\u0287\u0001\u0000\u0000\u0000\u0287\u028a\u0001\u0000"
        + "\u0000\u0000\u0288\u028a\u0005N\u0000\u0000\u0289\u0284\u0001\u0000\u0000"
        + "\u0000\u0289\u0288\u0001\u0000\u0000\u0000\u028a\u028b\u0001\u0000\u0000"
        + "\u0000\u028b\u028d\u0003F#\u0000\u028c\u027d\u0001\u0000\u0000\u0000\u028c"
        + "\u0282\u0001\u0000\u0000\u0000\u028c\u0289\u0001\u0000\u0000\u0000\u028d"
        + "\u0296\u0001\u0000\u0000\u0000\u028e\u028f\n\u0003\u0000\u0000\u028f\u0290"
        + "\u00050\u0000\u0000\u0290\u0295\u0003D\"\u0004\u0291\u0292\n\u0002\u0000"
        + "\u0000\u0292\u0293\u00051\u0000\u0000\u0293\u0295\u0003D\"\u0003\u0294"
        + "\u028e\u0001\u0000\u0000\u0000\u0294\u0291\u0001\u0000\u0000\u0000\u0295"
        + "\u0298\u0001\u0000\u0000\u0000\u0296\u0294\u0001\u0000\u0000\u0000\u0296"
        + "\u0297\u0001\u0000\u0000\u0000\u0297E\u0001\u0000\u0000\u0000\u0298\u0296"
        + "\u0001\u0000\u0000\u0000\u0299\u029a\u0003j5\u0000\u029a\u029b\u0005o"
        + "\u0000\u0000\u029b\u02a0\u0003j5\u0000\u029c\u029d\u0005p\u0000\u0000"
        + "\u029d\u029f\u0003j5\u0000\u029e\u029c\u0001\u0000\u0000\u0000\u029f\u02a2"
        + "\u0001\u0000\u0000\u0000\u02a0\u029e\u0001\u0000\u0000\u0000\u02a0\u02a1"
        + "\u0001\u0000\u0000\u0000\u02a1\u02ac\u0001\u0000\u0000\u0000\u02a2\u02a0"
        + "\u0001\u0000\u0000\u0000\u02a3\u02a8\u0003j5\u0000\u02a4\u02a5\u0005p"
        + "\u0000\u0000\u02a5\u02a7\u0003j5\u0000\u02a6\u02a4\u0001\u0000\u0000\u0000"
        + "\u02a7\u02aa\u0001\u0000\u0000\u0000\u02a8\u02a6\u0001\u0000\u0000\u0000"
        + "\u02a8\u02a9\u0001\u0000\u0000\u0000\u02a9\u02ac\u0001\u0000\u0000\u0000"
        + "\u02aa\u02a8\u0001\u0000\u0000\u0000\u02ab\u0299\u0001\u0000\u0000\u0000"
        + "\u02ab\u02a3\u0001\u0000\u0000\u0000\u02acG\u0001\u0000\u0000\u0000\u02ad"
        + "\u02ae\u0005F\u0000\u0000\u02ae\u02ba\u0005Z\u0000\u0000\u02af\u02b4\u0003"
        + "J%\u0000\u02b0\u02b1\u0005n\u0000\u0000\u02b1\u02b3\u0003J%\u0000\u02b2"
        + "\u02b0\u0001\u0000\u0000\u0000\u02b3\u02b6\u0001\u0000\u0000\u0000\u02b4"
        + "\u02b2\u0001\u0000\u0000\u0000\u02b4\u02b5\u0001\u0000\u0000\u0000\u02b5"
        + "\u02b7\u0001\u0000\u0000\u0000\u02b6\u02b4\u0001\u0000\u0000\u0000\u02b7"
        + "\u02b8\u0005Z\u0000\u0000\u02b8\u02ba\u0001\u0000\u0000\u0000\u02b9\u02ad"
        + "\u0001\u0000\u0000\u0000\u02b9\u02af\u0001\u0000\u0000\u0000\u02baI\u0001"
        + "\u0000\u0000\u0000\u02bb\u02bc\u0007\u0005\u0000\u0000\u02bcK\u0001\u0000"
        + "\u0000\u0000\u02bd\u02be\u0007\u0006\u0000\u0000\u02be\u02bf\u0003N\'"
        + "\u0000\u02bfM\u0001\u0000\u0000\u0000\u02c0\u02c5\u0003P(\u0000\u02c1"
        + "\u02c2\u0005n\u0000\u0000\u02c2\u02c4\u0003P(\u0000\u02c3\u02c1\u0001"
        + "\u0000\u0000\u0000\u02c4\u02c7\u0001\u0000\u0000\u0000\u02c5\u02c3\u0001"
        + "\u0000\u0000\u0000\u02c5\u02c6\u0001\u0000\u0000\u0000\u02c6O\u0001\u0000"
        + "\u0000\u0000\u02c7\u02c5\u0001\u0000\u0000\u0000\u02c8\u02cb\u0003\u00a2"
        + "Q\u0000\u02c9\u02ca\u0005q\u0000\u0000\u02ca\u02cc\u0003\u00a2Q\u0000"
        + "\u02cb\u02c9\u0001\u0000\u0000\u0000\u02cb\u02cc\u0001\u0000\u0000\u0000"
        + "\u02ccQ\u0001\u0000\u0000\u0000\u02cd\u02ce\u0005%\u0000\u0000\u02ce\u02cf"
        + "\u0003\\.\u0000\u02cf\u02d0\u0005&\u0000\u0000\u02d0\u02d9\u0003 \u0010"
        + "\u0000\u02d1\u02d2\u0005\'\u0000\u0000\u02d2\u02d3\u0005%\u0000\u0000"
        + "\u02d3\u02d4\u0003\\.\u0000\u02d4\u02d5\u0005&\u0000\u0000\u02d5\u02d6"
        + "\u0003 \u0010\u0000\u02d6\u02d8\u0001\u0000\u0000\u0000\u02d7\u02d1\u0001"
        + "\u0000\u0000\u0000\u02d8\u02db\u0001\u0000\u0000\u0000\u02d9\u02d7\u0001"
        + "\u0000\u0000\u0000\u02d9\u02da\u0001\u0000\u0000\u0000\u02da\u02de\u0001"
        + "\u0000\u0000\u0000\u02db\u02d9\u0001\u0000\u0000\u0000\u02dc\u02dd\u0005"
        + "\'\u0000\u0000\u02dd\u02df\u0003 \u0010\u0000\u02de\u02dc\u0001\u0000"
        + "\u0000\u0000\u02de\u02df\u0001\u0000\u0000\u0000\u02df\u02e0\u0001\u0000"
        + "\u0000\u0000\u02e0\u02e1\u0005k\u0000\u0000\u02e1\u02fb\u0001\u0000\u0000"
        + "\u0000\u02e2\u02e3\u0005%\u0000\u0000\u02e3\u02e4\u0003\\.\u0000\u02e4"
        + "\u02e5\u0005\u0019\u0000\u0000\u02e5\u02e6\u0003 \u0010\u0000\u02e6\u02f0"
        + "\u0005\u001a\u0000\u0000\u02e7\u02e8\u0005\'\u0000\u0000\u02e8\u02e9\u0005"
        + "%\u0000\u0000\u02e9\u02ea\u0003\\.\u0000\u02ea\u02eb\u0005\u0019\u0000"
        + "\u0000\u02eb\u02ec\u0003 \u0010\u0000\u02ec\u02ed\u0005\u001a\u0000\u0000"
        + "\u02ed\u02ef\u0001\u0000\u0000\u0000\u02ee\u02e7\u0001\u0000\u0000\u0000"
        + "\u02ef\u02f2\u0001\u0000\u0000\u0000\u02f0\u02ee\u0001\u0000\u0000\u0000"
        + "\u02f0\u02f1\u0001\u0000\u0000\u0000\u02f1\u02f8\u0001\u0000\u0000\u0000"
        + "\u02f2\u02f0\u0001\u0000\u0000\u0000\u02f3\u02f4\u0005\'\u0000\u0000\u02f4"
        + "\u02f5\u0005\u0019\u0000\u0000\u02f5\u02f6\u0003 \u0010\u0000\u02f6\u02f7"
        + "\u0005\u001a\u0000\u0000\u02f7\u02f9\u0001\u0000\u0000\u0000\u02f8\u02f3"
        + "\u0001\u0000\u0000\u0000\u02f8\u02f9\u0001\u0000\u0000\u0000\u02f9\u02fb"
        + "\u0001\u0000\u0000\u0000\u02fa\u02cd\u0001\u0000\u0000\u0000\u02fa\u02e2"
        + "\u0001\u0000\u0000\u0000\u02fbS\u0001\u0000\u0000\u0000\u02fc\u02fd\u0005"
        + "\u0013\u0000\u0000\u02fd\u02fe\u0003\n\u0005\u0000\u02fe\u02ff\u00057"
        + "\u0000\u0000\u02ff\u0300\u0003|>\u0000\u0300\u0301\u0005?\u0000\u0000"
        + "\u0301\u0304\u0003|>\u0000\u0302\u0303\u0005\u0015\u0000\u0000\u0303\u0305"
        + "\u0003|>\u0000\u0304\u0302\u0001\u0000\u0000\u0000\u0304\u0305\u0001\u0000"
        + "\u0000\u0000\u0305\u030e\u0001\u0000\u0000\u0000\u0306\u0307\u0005j\u0000"
        + "\u0000\u0307\u0308\u0003 \u0010\u0000\u0308\u0309\u0005k\u0000\u0000\u0309"
        + "\u030f\u0001\u0000\u0000\u0000\u030a\u030b\u0005\u0019\u0000\u0000\u030b"
        + "\u030c\u0003 \u0010\u0000\u030c\u030d\u0005\u001a\u0000\u0000\u030d\u030f"
        + "\u0001\u0000\u0000\u0000\u030e\u0306\u0001\u0000\u0000\u0000\u030e\u030a"
        + "\u0001\u0000\u0000\u0000\u030f\u0349\u0001\u0000\u0000\u0000\u0310\u0311"
        + "\u0005\u0013\u0000\u0000\u0311\u0312\u0003\n\u0005\u0000\u0312\u0313\u0005"
        + "J\u0000\u0000\u0313\u031c\u0003|>\u0000\u0314\u0315\u0005j\u0000\u0000"
        + "\u0315\u0316\u0003 \u0010\u0000\u0316\u0317\u0005k\u0000\u0000\u0317\u031d"
        + "\u0001\u0000\u0000\u0000\u0318\u0319\u0005\u0019\u0000\u0000\u0319\u031a"
        + "\u0003 \u0010\u0000\u031a\u031b\u0005\u001a\u0000\u0000\u031b\u031d\u0001"
        + "\u0000\u0000\u0000\u031c\u0314\u0001\u0000\u0000\u0000\u031c\u0318\u0001"
        + "\u0000\u0000\u0000\u031d\u0349\u0001\u0000\u0000\u0000\u031e\u031f\u0005"
        + "\u0013\u0000\u0000\u031f\u0321\u0005r\u0000\u0000\u0320\u0322\u0003V+"
        + "\u0000\u0321\u0320\u0001\u0000\u0000\u0000\u0321\u0322\u0001\u0000\u0000"
        + "\u0000\u0322\u0323\u0001\u0000\u0000\u0000\u0323\u0325\u0005\u001c\u0000"
        + "\u0000\u0324\u0326\u0003|>\u0000\u0325\u0324\u0001\u0000\u0000\u0000\u0325"
        + "\u0326\u0001\u0000\u0000\u0000\u0326\u0327\u0001\u0000\u0000\u0000\u0327"
        + "\u0329\u0005\u001c\u0000\u0000\u0328\u032a\u0003X,\u0000\u0329\u0328\u0001"
        + "\u0000\u0000\u0000\u0329\u032a\u0001\u0000\u0000\u0000\u032a\u032b\u0001"
        + "\u0000\u0000\u0000\u032b\u0334\u0005s\u0000\u0000\u032c\u032d\u0005j\u0000"
        + "\u0000\u032d\u032e\u0003 \u0010\u0000\u032e\u032f\u0005k\u0000\u0000\u032f"
        + "\u0335\u0001\u0000\u0000\u0000\u0330\u0331\u0005\u0019\u0000\u0000\u0331"
        + "\u0332\u0003 \u0010\u0000\u0332\u0333\u0005\u001a\u0000\u0000\u0333\u0335"
        + "\u0001\u0000\u0000\u0000\u0334\u032c\u0001\u0000\u0000\u0000\u0334\u0330"
        + "\u0001\u0000\u0000\u0000\u0335\u0349\u0001\u0000\u0000\u0000\u0336\u0337"
        + "\u0005\u0013\u0000\u0000\u0337\u0338\u0005r\u0000\u0000\u0338\u0339\u0003"
        + "\n\u0005\u0000\u0339\u033a\u0005n\u0000\u0000\u033a\u033b\u0003\n\u0005"
        + "\u0000\u033b\u033c\u0005s\u0000\u0000\u033c\u033d\u0005J\u0000\u0000\u033d"
        + "\u0346\u0003|>\u0000\u033e\u033f\u0005j\u0000\u0000\u033f\u0340\u0003"
        + " \u0010\u0000\u0340\u0341\u0005k\u0000\u0000\u0341\u0347\u0001\u0000\u0000"
        + "\u0000\u0342\u0343\u0005\u0019\u0000\u0000\u0343\u0344\u0003 \u0010\u0000"
        + "\u0344\u0345\u0005\u001a\u0000\u0000\u0345\u0347\u0001\u0000\u0000\u0000"
        + "\u0346\u033e\u0001\u0000\u0000\u0000\u0346\u0342\u0001\u0000\u0000\u0000"
        + "\u0347\u0349\u0001\u0000\u0000\u0000\u0348\u02fc\u0001\u0000\u0000\u0000"
        + "\u0348\u0310\u0001\u0000\u0000\u0000\u0348\u031e\u0001\u0000\u0000\u0000"
        + "\u0348\u0336\u0001\u0000\u0000\u0000\u0349U\u0001\u0000\u0000\u0000\u034a"
        + "\u034e\u0003n7\u0000\u034b\u034e\u0003p8\u0000\u034c\u034e\u0003\u0018"
        + "\f\u0000\u034d\u034a\u0001\u0000\u0000\u0000\u034d\u034b\u0001\u0000\u0000"
        + "\u0000\u034d\u034c\u0001\u0000\u0000\u0000\u034eW\u0001\u0000\u0000\u0000"
        + "\u034f\u0352\u0003p8\u0000\u0350\u0352\u0003\u0018\f\u0000\u0351\u034f"
        + "\u0001\u0000\u0000\u0000\u0351\u0350\u0001\u0000\u0000\u0000\u0352Y\u0001"
        + "\u0000\u0000\u0000\u0353\u0354\u0005\u0014\u0000\u0000\u0354\u035d\u0003"
        + "\\.\u0000\u0355\u0356\u0005j\u0000\u0000\u0356\u0357\u0003 \u0010\u0000"
        + "\u0357\u0358\u0005k\u0000\u0000\u0358\u035e\u0001\u0000\u0000\u0000\u0359"
        + "\u035a\u0005\u0019\u0000\u0000\u035a\u035b\u0003 \u0010\u0000\u035b\u035c"
        + "\u0005\u001a\u0000\u0000\u035c\u035e\u0001\u0000\u0000\u0000\u035d\u0355"
        + "\u0001\u0000\u0000\u0000\u035d\u0359\u0001\u0000\u0000\u0000\u035e[\u0001"
        + "\u0000\u0000\u0000\u035f\u0360\u0006.\uffff\uffff\u0000\u0360\u0392\u0005"
        + "-\u0000\u0000\u0361\u0392\u0005.\u0000\u0000\u0362\u0363\u0005r\u0000"
        + "\u0000\u0363\u0364\u0003\\.\u0000\u0364\u0365\u0005s\u0000\u0000\u0365"
        + "\u0392\u0001\u0000\u0000\u0000\u0366\u0367\u0005r\u0000\u0000\u0367\u0368"
        + "\u0003|>\u0000\u0368\u0369\u0005s\u0000\u0000\u0369\u0392\u0001\u0000"
        + "\u0000\u0000\u036a\u036b\u0005/\u0000\u0000\u036b\u0392\u0003\\.\u0005"
        + "\u036c\u036e\u0003`0\u0000\u036d\u036c\u0001\u0000\u0000\u0000\u036d\u036e"
        + "\u0001\u0000\u0000\u0000\u036e\u036f\u0001\u0000\u0000\u0000\u036f\u0370"
        + "\u0003b1\u0000\u0370\u0371\u0005(\u0000\u0000\u0371\u0377\u0003^/\u0000"
        + "\u0372\u0378\u0003\u00a2Q\u0000\u0373\u0374\u0005r\u0000\u0000\u0374\u0375"
        + "\u0003|>\u0000\u0375\u0376\u0005s\u0000\u0000\u0376\u0378\u0001\u0000"
        + "\u0000\u0000\u0377\u0372\u0001\u0000\u0000\u0000\u0377\u0373\u0001\u0000"
        + "\u0000\u0000\u0378\u037a\u0001\u0000\u0000\u0000\u0379\u037b\u0003@ \u0000"
        + "\u037a\u0379\u0001\u0000\u0000\u0000\u037a\u037b\u0001\u0000\u0000\u0000"
        + "\u037b\u037d\u0001\u0000\u0000\u0000\u037c\u037e\u0003B!\u0000\u037d\u037c"
        + "\u0001\u0000\u0000\u0000\u037d\u037e\u0001\u0000\u0000\u0000\u037e\u0381"
        + "\u0001\u0000\u0000\u0000\u037f\u0380\u0005G\u0000\u0000\u0380\u0382\u0003"
        + ">\u001f\u0000\u0381\u037f\u0001\u0000\u0000\u0000\u0381\u0382\u0001\u0000"
        + "\u0000\u0000\u0382\u0392\u0001\u0000\u0000\u0000\u0383\u0385\u0005f\u0000"
        + "\u0000\u0384\u0386\u0007\u0004\u0000\u0000\u0385\u0384\u0001\u0000\u0000"
        + "\u0000\u0385\u0386\u0001\u0000\u0000\u0000\u0386\u038f\u0001\u0000\u0000"
        + "\u0000\u0387\u038d\u0003^/\u0000\u0388\u038e\u0003\u00a2Q\u0000\u0389"
        + "\u038a\u0005r\u0000\u0000\u038a\u038b\u0003|>\u0000\u038b\u038c\u0005"
        + "s\u0000\u0000\u038c\u038e\u0001\u0000\u0000\u0000\u038d\u0388\u0001\u0000"
        + "\u0000\u0000\u038d\u0389\u0001\u0000\u0000\u0000\u038e\u0390\u0001\u0000"
        + "\u0000\u0000\u038f\u0387\u0001\u0000\u0000\u0000\u038f\u0390\u0001\u0000"
        + "\u0000\u0000\u0390\u0392\u0001\u0000\u0000\u0000\u0391\u035f\u0001\u0000"
        + "\u0000\u0000\u0391\u0361\u0001\u0000\u0000\u0000\u0391\u0362\u0001\u0000"
        + "\u0000\u0000\u0391\u0366\u0001\u0000\u0000\u0000\u0391\u036a\u0001\u0000"
        + "\u0000\u0000\u0391\u036d\u0001\u0000\u0000\u0000\u0391\u0383\u0001\u0000"
        + "\u0000\u0000\u0392\u039b\u0001\u0000\u0000\u0000\u0393\u0394\n\u0004\u0000"
        + "\u0000\u0394\u0395\u00050\u0000\u0000\u0395\u039a\u0003\\.\u0005\u0396"
        + "\u0397\n\u0003\u0000\u0000\u0397\u0398\u00051\u0000\u0000\u0398\u039a"
        + "\u0003\\.\u0004\u0399\u0393\u0001\u0000\u0000\u0000\u0399\u0396\u0001"
        + "\u0000\u0000\u0000\u039a\u039d\u0001\u0000\u0000\u0000\u039b\u0399\u0001"
        + "\u0000\u0000\u0000\u039b\u039c\u0001\u0000\u0000\u0000\u039c]\u0001\u0000"
        + "\u0000\u0000\u039d\u039b\u0001\u0000\u0000\u0000\u039e\u039f\u0007\u0007"
        + "\u0000\u0000\u039f_\u0001\u0000\u0000\u0000\u03a0\u03a1\u0007\b\u0000"
        + "\u0000\u03a1a\u0001\u0000\u0000\u0000\u03a2\u03a7\u0003f3\u0000\u03a3"
        + "\u03a4\u0005n\u0000\u0000\u03a4\u03a6\u0003f3\u0000\u03a5\u03a3\u0001"
        + "\u0000\u0000\u0000\u03a6\u03a9\u0001\u0000\u0000\u0000\u03a7\u03a5\u0001"
        + "\u0000\u0000\u0000\u03a7\u03a8\u0001\u0000\u0000\u0000\u03a8\u03ab\u0001"
        + "\u0000\u0000\u0000\u03a9\u03a7\u0001\u0000\u0000\u0000\u03aa\u03ac\u0003"
        + "d2\u0000\u03ab\u03aa\u0001\u0000\u0000\u0000\u03ab\u03ac\u0001\u0000\u0000"
        + "\u0000\u03ac\u03ae\u0001\u0000\u0000\u0000\u03ad\u03af\u0003H$\u0000\u03ae"
        + "\u03ad\u0001\u0000\u0000\u0000\u03ae\u03af\u0001\u0000\u0000\u0000\u03af"
        + "\u03b1\u0001\u0000\u0000\u0000\u03b0\u03b2\u0003L&\u0000\u03b1\u03b0\u0001"
        + "\u0000\u0000\u0000\u03b1\u03b2\u0001\u0000\u0000\u0000\u03b2c\u0001\u0000"
        + "\u0000\u0000\u03b3\u03b4\u0005O\u0000\u0000\u03b4\u03b5\u0005P\u0000\u0000"
        + "\u03b5\u03b6\u0005Q\u0000\u0000\u03b6\u03b7\u0007\t\u0000\u0000\u03b7"
        + "e\u0001\u0000\u0000\u0000\u03b8\u03bc\u0003j5\u0000\u03b9\u03bb\u0003"
        + "\u0098L\u0000\u03ba\u03b9\u0001\u0000\u0000\u0000\u03bb\u03be\u0001\u0000"
        + "\u0000\u0000\u03bc\u03ba\u0001\u0000\u0000\u0000\u03bc\u03bd\u0001\u0000"
        + "\u0000\u0000\u03bd\u03cf\u0001\u0000\u0000\u0000\u03be\u03bc\u0001\u0000"
        + "\u0000\u0000\u03bf\u03cf\u0003\u00a0P\u0000\u03c0\u03c4\u0005\u0016\u0000"
        + "\u0000\u03c1\u03c3\u0003\u0098L\u0000\u03c2\u03c1\u0001\u0000\u0000\u0000"
        + "\u03c3\u03c6\u0001\u0000\u0000\u0000\u03c4\u03c2\u0001\u0000\u0000\u0000"
        + "\u03c4\u03c5\u0001\u0000\u0000\u0000\u03c5\u03cf\u0001\u0000\u0000\u0000"
        + "\u03c6\u03c4\u0001\u0000\u0000\u0000\u03c7\u03cb\u0003\u0012\t\u0000\u03c8"
        + "\u03ca\u0003\u0098L\u0000\u03c9\u03c8\u0001\u0000\u0000\u0000\u03ca\u03cd"
        + "\u0001\u0000\u0000\u0000\u03cb\u03c9\u0001\u0000\u0000\u0000\u03cb\u03cc"
        + "\u0001\u0000\u0000\u0000\u03cc\u03cf\u0001\u0000\u0000\u0000\u03cd\u03cb"
        + "\u0001\u0000\u0000\u0000\u03ce\u03b8\u0001\u0000\u0000\u0000\u03ce\u03bf"
        + "\u0001\u0000\u0000\u0000\u03ce\u03c0\u0001\u0000\u0000\u0000\u03ce\u03c7"
        + "\u0001\u0000\u0000\u0000\u03cfg\u0001\u0000\u0000\u0000\u03d0\u03d1\u0005"
        + "I\u0000\u0000\u03d1\u03d2\u0007\u0006\u0000\u0000\u03d2\u03d3\u0005J\u0000"
        + "\u0000\u03d3i\u0001\u0000\u0000\u0000\u03d4\u03d5\u0007\n\u0000\u0000"
        + "\u03d5k\u0001\u0000\u0000\u0000\u03d6\u03d7\u0005\u000f\u0000\u0000\u03d7"
        + "\u03d8\u0003\n\u0005\u0000\u03d8\u03d9\u00057\u0000\u0000\u03d9\u03da"
        + "\u0003|>\u0000\u03dam\u0001\u0000\u0000\u0000\u03db\u03dc\u0005\r\u0000"
        + "\u0000\u03dc\u03dd\u0003\n\u0005\u0000\u03dd\u03de\u00057\u0000\u0000"
        + "\u03de\u03df\u0003|>\u0000\u03dfo\u0001\u0000\u0000\u0000\u03e0\u03e2"
        + "\u0005\u000e\u0000\u0000\u03e1\u03e0\u0001\u0000\u0000\u0000\u03e1\u03e2"
        + "\u0001\u0000\u0000\u0000\u03e2\u03e3\u0001\u0000\u0000\u0000\u03e3\u03e4"
        + "\u0003z=\u0000\u03e4\u03e5\u00057\u0000\u0000\u03e5\u03e6\u0003|>\u0000"
        + "\u03e6q\u0001\u0000\u0000\u0000\u03e7\u03e8\u0005\u0004\u0000\u0000\u03e8"
        + "\u03e9\u0005r\u0000\u0000\u03e9\u03ea\u0003|>\u0000\u03ea\u03eb\u0005"
        + "n\u0000\u0000\u03eb\u03ec\u0003|>\u0000\u03ec\u03ef\u0005s\u0000\u0000"
        + "\u03ed\u03ee\u0005\b\u0000\u0000\u03ee\u03f0\u0003\n\u0005\u0000\u03ef"
        + "\u03ed\u0001\u0000\u0000\u0000\u03ef\u03f0\u0001\u0000\u0000\u0000\u03f0"
        + "s\u0001\u0000\u0000\u0000\u03f1\u03f2\u0005\u0005\u0000\u0000\u03f2\u03f3"
        + "\u0003|>\u0000\u03f3u\u0001\u0000\u0000\u0000\u03f4\u03f5\u0005\u0012"
        + "\u0000\u0000\u03f5\u03f6\u0005r\u0000\u0000\u03f6\u03fb\u0003|>\u0000"
        + "\u03f7\u03f8\u0005n\u0000\u0000\u03f8\u03fa\u0003|>\u0000\u03f9\u03f7"
        + "\u0001\u0000\u0000\u0000\u03fa\u03fd\u0001\u0000\u0000\u0000\u03fb\u03f9"
        + "\u0001\u0000\u0000\u0000\u03fb\u03fc\u0001\u0000\u0000\u0000\u03fc\u03fe"
        + "\u0001\u0000\u0000\u0000\u03fd\u03fb\u0001\u0000\u0000\u0000\u03fe\u03ff"
        + "\u0005s\u0000\u0000\u03ffw\u0001\u0000\u0000\u0000\u0400\u0401\u0007\u000b"
        + "\u0000\u0000\u0401\u0402\u0005r\u0000\u0000\u0402\u0403\u0003|>\u0000"
        + "\u0403\u0404\u0005n\u0000\u0000\u0404\u040b\u0003|>\u0000\u0405\u0406"
        + "\u0005n\u0000\u0000\u0406\u0409\u0003|>\u0000\u0407\u0408\u0005n\u0000"
        + "\u0000\u0408\u040a\u0003|>\u0000\u0409\u0407\u0001\u0000\u0000\u0000\u0409"
        + "\u040a\u0001\u0000\u0000\u0000\u040a\u040c\u0001\u0000\u0000\u0000\u040b"
        + "\u0405\u0001\u0000\u0000\u0000\u040b\u040c\u0001\u0000\u0000\u0000\u040c"
        + "\u040d\u0001\u0000\u0000\u0000\u040d\u0410\u0005s\u0000\u0000\u040e\u040f"
        + "\u0005\b\u0000\u0000\u040f\u0411\u0003\n\u0005\u0000\u0410\u040e\u0001"
        + "\u0000\u0000\u0000\u0410\u0411\u0001\u0000\u0000\u0000\u0411y\u0001\u0000"
        + "\u0000\u0000\u0412\u041b\u0003\n\u0005\u0000\u0413\u0414\u0005\u001b\u0000"
        + "\u0000\u0414\u041a\u0003\u009eO\u0000\u0415\u0416\u0005\u0017\u0000\u0000"
        + "\u0416\u0417\u0003|>\u0000\u0417\u0418\u0005\u0018\u0000\u0000\u0418\u041a"
        + "\u0001\u0000\u0000\u0000\u0419\u0413\u0001\u0000\u0000\u0000\u0419\u0415"
        + "\u0001\u0000\u0000\u0000\u041a\u041d\u0001\u0000\u0000\u0000\u041b\u0419"
        + "\u0001\u0000\u0000\u0000\u041b\u041c\u0001\u0000\u0000\u0000\u041c{\u0001"
        + "\u0000\u0000\u0000\u041d\u041b\u0001\u0000\u0000\u0000\u041e\u041f\u0003"
        + "~?\u0000\u041f}\u0001\u0000\u0000\u0000\u0420\u0425\u0003\u0080@\u0000"
        + "\u0421\u0422\u00051\u0000\u0000\u0422\u0424\u0003\u0080@\u0000\u0423\u0421"
        + "\u0001\u0000\u0000\u0000\u0424\u0427\u0001\u0000\u0000\u0000\u0425\u0423"
        + "\u0001\u0000\u0000\u0000\u0425\u0426\u0001\u0000\u0000\u0000\u0426\u007f"
        + "\u0001\u0000\u0000\u0000\u0427\u0425\u0001\u0000\u0000\u0000\u0428\u042d"
        + "\u0003\u0082A\u0000\u0429\u042a\u00050\u0000\u0000\u042a\u042c\u0003\u0082"
        + "A\u0000\u042b\u0429\u0001\u0000\u0000\u0000\u042c\u042f\u0001\u0000\u0000"
        + "\u0000\u042d\u042b\u0001\u0000\u0000\u0000\u042d\u042e\u0001\u0000\u0000"
        + "\u0000\u042e\u0081\u0001\u0000\u0000\u0000\u042f\u042d\u0001\u0000\u0000"
        + "\u0000\u0430\u0431\u0005/\u0000\u0000\u0431\u0434\u0003\u0082A\u0000\u0432"
        + "\u0434\u0003\u0084B\u0000\u0433\u0430\u0001\u0000\u0000\u0000\u0433\u0432"
        + "\u0001\u0000\u0000\u0000\u0434\u0083\u0001\u0000\u0000\u0000\u0435\u0439"
        + "\u0003\u0086C\u0000\u0436\u0437\u0003^/\u0000\u0437\u0438\u0003\u0086"
        + "C\u0000\u0438\u043a\u0001\u0000\u0000\u0000\u0439\u0436\u0001\u0000\u0000"
        + "\u0000\u0439\u043a\u0001\u0000\u0000\u0000\u043a\u0085\u0001\u0000\u0000"
        + "\u0000\u043b\u0440\u0003\u0088D\u0000\u043c\u043d\u0005\u001f\u0000\u0000"
        + "\u043d\u043f\u0003\u0088D\u0000\u043e\u043c\u0001\u0000\u0000\u0000\u043f"
        + "\u0442\u0001\u0000\u0000\u0000\u0440\u043e\u0001\u0000\u0000\u0000\u0440"
        + "\u0441\u0001\u0000\u0000\u0000\u0441\u0087\u0001\u0000\u0000\u0000\u0442"
        + "\u0440\u0001\u0000\u0000\u0000\u0443\u0448\u0003\u008aE\u0000\u0444\u0445"
        + "\u0005 \u0000\u0000\u0445\u0447\u0003\u008aE\u0000\u0446\u0444\u0001\u0000"
        + "\u0000\u0000\u0447\u044a\u0001\u0000\u0000\u0000\u0448\u0446\u0001\u0000"
        + "\u0000\u0000\u0448\u0449\u0001\u0000\u0000\u0000\u0449\u0089\u0001\u0000"
        + "\u0000\u0000\u044a\u0448\u0001\u0000\u0000\u0000\u044b\u0450\u0003\u008c"
        + "F\u0000\u044c\u044d\u0005\u001e\u0000\u0000\u044d\u044f\u0003\u008cF\u0000"
        + "\u044e\u044c\u0001\u0000\u0000\u0000\u044f\u0452\u0001\u0000\u0000\u0000"
        + "\u0450\u044e\u0001\u0000\u0000\u0000\u0450\u0451\u0001\u0000\u0000\u0000"
        + "\u0451\u008b\u0001\u0000\u0000\u0000\u0452\u0450\u0001\u0000\u0000\u0000"
        + "\u0453\u0458\u0003\u008eG\u0000\u0454\u0455\u0007\f\u0000\u0000\u0455"
        + "\u0457\u0003\u008eG\u0000\u0456\u0454\u0001\u0000\u0000\u0000\u0457\u045a"
        + "\u0001\u0000\u0000\u0000\u0458\u0456\u0001\u0000\u0000\u0000\u0458\u0459"
        + "\u0001\u0000\u0000\u0000\u0459\u008d\u0001\u0000\u0000\u0000\u045a\u0458"
        + "\u0001\u0000\u0000\u0000\u045b\u0460\u0003\u0090H\u0000\u045c\u045d\u0007"
        + "\r\u0000\u0000\u045d\u045f\u0003\u0090H\u0000\u045e\u045c\u0001\u0000"
        + "\u0000\u0000\u045f\u0462\u0001\u0000\u0000\u0000\u0460\u045e\u0001\u0000"
        + "\u0000\u0000\u0460\u0461\u0001\u0000\u0000\u0000\u0461\u008f\u0001\u0000"
        + "\u0000\u0000\u0462\u0460\u0001\u0000\u0000\u0000\u0463\u046c\u0003\u0094"
        + "J\u0000\u0464\u0468\u0003\u0092I\u0000\u0465\u0468\u0005p\u0000\u0000"
        + "\u0466\u0468\u0005\u001d\u0000\u0000\u0467\u0464\u0001\u0000\u0000\u0000"
        + "\u0467\u0465\u0001\u0000\u0000\u0000\u0467\u0466\u0001\u0000\u0000\u0000"
        + "\u0468\u0469\u0001\u0000\u0000\u0000\u0469\u046b\u0003\u0094J\u0000\u046a"
        + "\u0467\u0001\u0000\u0000\u0000\u046b\u046e\u0001\u0000\u0000\u0000\u046c"
        + "\u046a\u0001\u0000\u0000\u0000\u046c\u046d\u0001\u0000\u0000\u0000\u046d"
        + "\u0091\u0001\u0000\u0000\u0000\u046e\u046c\u0001\u0000\u0000\u0000\u046f"
        + "\u0470\u0004I\u0004\u0000\u0470\u0471\u0005v\u0000\u0000\u0471\u0093\u0001"
        + "\u0000\u0000\u0000\u0472\u0473\u0007\u000e\u0000\u0000\u0473\u0476\u0003"
        + "\u0094J\u0000\u0474\u0476\u0003\u0096K\u0000\u0475\u0472\u0001\u0000\u0000"
        + "\u0000\u0475\u0474\u0001\u0000\u0000\u0000\u0476\u0095\u0001\u0000\u0000"
        + "\u0000\u0477\u047b\u0003\u009aM\u0000\u0478\u047a\u0003\u0098L\u0000\u0479"
        + "\u0478\u0001\u0000\u0000\u0000\u047a\u047d\u0001\u0000\u0000\u0000\u047b"
        + "\u0479\u0001\u0000\u0000\u0000\u047b\u047c\u0001\u0000\u0000\u0000\u047c"
        + "\u0097\u0001\u0000\u0000\u0000\u047d\u047b\u0001\u0000\u0000\u0000\u047e"
        + "\u047f\u0005\u001b\u0000\u0000\u047f\u048c\u0003\u009eO\u0000\u0480\u0489"
        + "\u0005r\u0000\u0000\u0481\u0486\u0003|>\u0000\u0482\u0483\u0005n\u0000"
        + "\u0000\u0483\u0485\u0003|>\u0000\u0484\u0482\u0001\u0000\u0000\u0000\u0485"
        + "\u0488\u0001\u0000\u0000\u0000\u0486\u0484\u0001\u0000\u0000\u0000\u0486"
        + "\u0487\u0001\u0000\u0000\u0000\u0487\u048a\u0001\u0000\u0000\u0000\u0488"
        + "\u0486\u0001\u0000\u0000\u0000\u0489\u0481\u0001\u0000\u0000\u0000\u0489"
        + "\u048a\u0001\u0000\u0000\u0000\u048a\u048b\u0001\u0000\u0000\u0000\u048b"
        + "\u048d\u0005s\u0000\u0000\u048c\u0480\u0001\u0000\u0000\u0000\u048c\u048d"
        + "\u0001\u0000\u0000\u0000\u048d\u0493\u0001\u0000\u0000\u0000\u048e\u048f"
        + "\u0005\u0017\u0000\u0000\u048f\u0490\u0003|>\u0000\u0490\u0491\u0005\u0018"
        + "\u0000\u0000\u0491\u0493\u0001\u0000\u0000\u0000\u0492\u047e\u0001\u0000"
        + "\u0000\u0000\u0492\u048e\u0001\u0000\u0000\u0000\u0493\u0099\u0001\u0000"
        + "\u0000\u0000\u0494\u04c1\u0005u\u0000\u0000\u0495\u04c1\u0005w\u0000\u0000"
        + "\u0496\u04c1\u0005\u0016\u0000\u0000\u0497\u04c1\u0005\u0001\u0000\u0000"
        + "\u0498\u04c1\u0005\u0002\u0000\u0000\u0499\u04c1\u0005\u0003\u0000\u0000"
        + "\u049a\u04c1\u0005-\u0000\u0000\u049b\u04c1\u0005.\u0000\u0000\u049c\u049d"
        + "\u0005r\u0000\u0000\u049d\u049e\u0003|>\u0000\u049e\u049f\u0005s\u0000"
        + "\u0000\u049f\u04c1\u0001\u0000\u0000\u0000\u04a0\u04c1\u0003\u0012\t\u0000"
        + "\u04a1\u04c1\u0005v\u0000\u0000\u04a2\u04ae\u0005\u0017\u0000\u0000\u04a3"
        + "\u04a8\u0003|>\u0000\u04a4\u04a5\u0005n\u0000\u0000\u04a5\u04a7\u0003"
        + "|>\u0000\u04a6\u04a4\u0001\u0000\u0000\u0000\u04a7\u04aa\u0001\u0000\u0000"
        + "\u0000\u04a8\u04a6\u0001\u0000\u0000\u0000\u04a8\u04a9\u0001\u0000\u0000"
        + "\u0000\u04a9\u04ac\u0001\u0000\u0000\u0000\u04aa\u04a8\u0001\u0000\u0000"
        + "\u0000\u04ab\u04ad\u0005n\u0000\u0000\u04ac\u04ab\u0001\u0000\u0000\u0000"
        + "\u04ac\u04ad\u0001\u0000\u0000\u0000\u04ad\u04af\u0001\u0000\u0000\u0000"
        + "\u04ae\u04a3\u0001\u0000\u0000\u0000\u04ae\u04af\u0001\u0000\u0000\u0000"
        + "\u04af\u04b0\u0001\u0000\u0000\u0000\u04b0\u04c1\u0005\u0018\u0000\u0000"
        + "\u04b1\u04bd\u0005\u0019\u0000\u0000\u04b2\u04b7\u0003\u009cN\u0000\u04b3"
        + "\u04b4\u0005n\u0000\u0000\u04b4\u04b6\u0003\u009cN\u0000\u04b5\u04b3\u0001"
        + "\u0000\u0000\u0000\u04b6\u04b9\u0001\u0000\u0000\u0000\u04b7\u04b5\u0001"
        + "\u0000\u0000\u0000\u04b7\u04b8\u0001\u0000\u0000\u0000\u04b8\u04bb\u0001"
        + "\u0000\u0000\u0000\u04b9\u04b7\u0001\u0000\u0000\u0000\u04ba\u04bc\u0005"
        + "n\u0000\u0000\u04bb\u04ba\u0001\u0000\u0000\u0000\u04bb\u04bc\u0001\u0000"
        + "\u0000\u0000\u04bc\u04be\u0001\u0000\u0000\u0000\u04bd\u04b2\u0001\u0000"
        + "\u0000\u0000\u04bd\u04be\u0001\u0000\u0000\u0000\u04be\u04bf\u0001\u0000"
        + "\u0000\u0000\u04bf\u04c1\u0005\u001a\u0000\u0000\u04c0\u0494\u0001\u0000"
        + "\u0000\u0000\u04c0\u0495\u0001\u0000\u0000\u0000\u04c0\u0496\u0001\u0000"
        + "\u0000\u0000\u04c0\u0497\u0001\u0000\u0000\u0000\u04c0\u0498\u0001\u0000"
        + "\u0000\u0000\u04c0\u0499\u0001\u0000\u0000\u0000\u04c0\u049a\u0001\u0000"
        + "\u0000\u0000\u04c0\u049b\u0001\u0000\u0000\u0000\u04c0\u049c\u0001\u0000"
        + "\u0000\u0000\u04c0\u04a0\u0001\u0000\u0000\u0000\u04c0\u04a1\u0001\u0000"
        + "\u0000\u0000\u04c0\u04a2\u0001\u0000\u0000\u0000\u04c0\u04b1\u0001\u0000"
        + "\u0000\u0000\u04c1\u009b\u0001\u0000\u0000\u0000\u04c2\u04c3\u0003\u009e"
        + "O\u0000\u04c3\u04c4\u0005o\u0000\u0000\u04c4\u04c5\u0003|>\u0000\u04c5"
        + "\u009d\u0001\u0000\u0000\u0000\u04c6\u04c7\u0007\u000f\u0000\u0000\u04c7"
        + "\u009f\u0001\u0000\u0000\u0000\u04c8\u04c9\u0005w\u0000\u0000\u04c9\u00a1"
        + "\u0001\u0000\u0000\u0000\u04ca\u04cb\u0005u\u0000\u0000\u04cb\u00a3\u0001"
        + "\u0000\u0000\u0000\u00a8\u00a5\u00aa\u00af\u00b2\u00b6\u00bb\u00bd\u00cb"
        + "\u00d5\u00d8\u00e5\u00ef\u00f2\u00fd\u00ff\u0106\u010a\u0113\u011e\u0121"
        + "\u0124\u0129\u012e\u013d\u0140\u0147\u014b\u015c\u0169\u0178\u017a\u017d"
        + "\u0180\u0184\u018a\u018d\u0192\u01a4\u01a7\u01ab\u01b0\u01b4\u01b9\u01be"
        + "\u01c3\u01c5\u01c9\u01cf\u01d3\u01d7\u01da\u01de\u01e3\u01e8\u01eb\u01ed"
        + "\u01f1\u01f4\u01f8\u01fb\u0200\u0203\u0208\u020b\u020d\u0218\u021c\u021f"
        + "\u0223\u0227\u022a\u0231\u0238\u023b\u0243\u0246\u024e\u0252\u0256\u0258"
        + "\u025a\u025c\u025f\u0266\u026a\u0271\u0275\u027b\u0286\u0289\u028c\u0294"
        + "\u0296\u02a0\u02a8\u02ab\u02b4\u02b9\u02c5\u02cb\u02d9\u02de\u02f0\u02f8"
        + "\u02fa\u0304\u030e\u031c\u0321\u0325\u0329\u0334\u0346\u0348\u034d\u0351"
        + "\u035d\u036d\u0377\u037a\u037d\u0381\u0385\u038d\u038f\u0391\u0399\u039b"
        + "\u03a7\u03ab\u03ae\u03b1\u03bc\u03c4\u03cb\u03ce\u03e1\u03ef\u03fb\u0409"
        + "\u040b\u0410\u0419\u041b\u0425\u042d\u0433\u0439\u0440\u0448\u0450\u0458"
        + "\u0460\u0467\u046c\u0475\u047b\u0486\u0489\u048c\u0492\u04a8\u04ac\u04ae"
        + "\u04b7\u04bb\u04bd\u04c0";
    public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
