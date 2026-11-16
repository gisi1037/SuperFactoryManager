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
        TRANSFEREXACT = 8, TRANSFERFEXACT = 9, TRANSFERFORCE = 10, INTO = 11, FUNCTION = 12, RETURNS = 13, RETURN = 14,
        REF = 15, LET = 16, SET = 17, STATE = 18, RISING = 19, FALLING = 20, PRINT = 21, FOR = 22, WHILE = 23,
        STEP = 24, VARIABLE = 25, LBRACKET = 26, RBRACKET = 27, LBRACE = 28, RBRACE = 29, DOT = 30, SEMICOLON = 31,
        PERCENT = 32, BIT_AND = 33, BIT_OR = 34, BIT_XOR = 35, BIT_NOT = 36, SHIFT_LEFT = 37, SHIFT_RIGHT = 38,
        SHIFT_UNSIGNED_RIGHT = 39, IF = 40, THEN = 41, ELSE = 42, HAS = 43, OVERALL = 44, SOME = 45, ONE = 46,
        LONE = 47, TRUE = 48, FALSE = 49, NOT = 50, AND = 51, OR = 52, GT = 53, GT_SYMBOL = 54, LT = 55, LT_SYMBOL = 56,
        EQ = 57, EQ_SYMBOL = 58, EQ_ALIAS = 59, NE_SYMBOL = 60, LE = 61, LE_SYMBOL = 62, GE = 63, GE_SYMBOL = 64,
        FROM = 65, TO = 66, INPUT = 67, OUTPUT = 68, WHERE = 69, SLOTS = 70, SLOT = 71, RETAIN = 72, EACH = 73,
        EXCEPT = 74, FORGET = 75, EMPTY = 76, IN = 77, WITHOUT = 78, WITH = 79, TAG = 80, HASHTAG = 81, ROUND = 82,
        ROBIN = 83, BY = 84, LABEL = 85, BLOCK = 86, TOP = 87, BOTTOM = 88, NORTH = 89, EAST = 90, SOUTH = 91,
        WEST = 92, SIDE = 93, LEFT = 94, RIGHT = 95, FRONT = 96, BACK = 97, NULL = 98, TICKS = 99, TICK = 100,
        SECONDS = 101, SECOND = 102, GLOBAL = 103, PLUS = 104, REDSTONE = 105, LEVEL = 106, LEVELS = 107, PULSE = 108,
        DO = 109, END = 110, NAME = 111, EVERY = 112, COMMA = 113, COLON = 114, SLASH = 115, DASH = 116, LPAREN = 117,
        RPAREN = 118, NUMBER_WITH_G_SUFFIX = 119, NUMBER = 120, IDENTIFIER = 121, STRING = 122, LINE_COMMENT = 123,
        WS = 124, UNUSED = 125;
    public static final int RULE_program = 0, RULE_importDeclaration = 1, RULE_functionDefinition = 2,
        RULE_functionName = 3, RULE_functionParameter = 4, RULE_variableName = 5, RULE_dataType = 6,
        RULE_typeDimension = 7, RULE_typeField = 8, RULE_functionCall = 9, RULE_functionArgument = 10,
        RULE_returnStatement = 11, RULE_functionCallStatement = 12, RULE_name = 13, RULE_trigger = 14,
        RULE_interval = 15, RULE_block = 16, RULE_statement = 17, RULE_breakStatement = 18,
        RULE_redstoneOutputStatement = 19, RULE_forgetStatement = 20, RULE_inputStatement = 21,
        RULE_outputStatement = 22, RULE_inputResourceLimits = 23, RULE_outputResourceLimits = 24,
        RULE_resourceLimitList = 25, RULE_resourceLimit = 26, RULE_limit = 27, RULE_quantity = 28, RULE_retention = 29,
        RULE_resourceExclusion = 30, RULE_resourceId = 31, RULE_resourceIdList = 32, RULE_resourceIdDisjunction = 33,
        RULE_with = 34, RULE_withClause = 35, RULE_tagMatcher = 36, RULE_sidequalifier = 37, RULE_side = 38,
        RULE_slotqualifier = 39, RULE_rangeset = 40, RULE_range = 41, RULE_ifStatement = 42, RULE_forStatement = 43,
        RULE_forInitializer = 44, RULE_forUpdate = 45, RULE_whileStatement = 46, RULE_boolexpr = 47,
        RULE_comparisonOp = 48, RULE_setOp = 49, RULE_labelAccess = 50, RULE_roundrobin = 51, RULE_label = 52,
        RULE_emptyslots = 53, RULE_identifier = 54, RULE_stateDeclaration = 55, RULE_dataDeclaration = 56,
        RULE_dataAssignment = 57, RULE_circuitStatement = 58, RULE_waitStatement = 59, RULE_printStatement = 60,
        RULE_transferStatement = 61, RULE_transferExclusion = 62, RULE_dataTarget = 63, RULE_dataExpression = 64,
        RULE_dataOr = 65, RULE_dataAnd = 66, RULE_dataNot = 67, RULE_dataComparison = 68, RULE_dataBitOr = 69,
        RULE_dataBitXor = 70, RULE_dataBitAnd = 71, RULE_dataShift = 72, RULE_dataAdd = 73, RULE_dataMultiply = 74,
        RULE_multiplyOperator = 75, RULE_dataSign = 76, RULE_dataPostfix = 77, RULE_dataSuffix = 78,
        RULE_dataPrimary = 79, RULE_dataField = 80, RULE_dataName = 81, RULE_string = 82, RULE_number = 83;

    private static String[] makeRuleNames() {
        return new String[] { "program", "importDeclaration", "functionDefinition", "functionName", "functionParameter",
            "variableName", "dataType", "typeDimension", "typeField", "functionCall", "functionArgument",
            "returnStatement", "functionCallStatement", "name", "trigger", "interval", "block", "statement",
            "breakStatement", "redstoneOutputStatement", "forgetStatement", "inputStatement", "outputStatement",
            "inputResourceLimits", "outputResourceLimits", "resourceLimitList", "resourceLimit", "limit", "quantity",
            "retention", "resourceExclusion", "resourceId", "resourceIdList", "resourceIdDisjunction", "with",
            "withClause", "tagMatcher", "sidequalifier", "side", "slotqualifier", "rangeset", "range", "ifStatement",
            "forStatement", "forInitializer", "forUpdate", "whileStatement", "boolexpr", "comparisonOp", "setOp",
            "labelAccess", "roundrobin", "label", "emptyslots", "identifier", "stateDeclaration", "dataDeclaration",
            "dataAssignment", "circuitStatement", "waitStatement", "printStatement", "transferStatement",
            "transferExclusion", "dataTarget", "dataExpression", "dataOr", "dataAnd", "dataNot", "dataComparison",
            "dataBitOr", "dataBitXor", "dataBitAnd", "dataShift", "dataAdd", "dataMultiply", "multiplyOperator",
            "dataSign", "dataPostfix", "dataSuffix", "dataPrimary", "dataField", "dataName", "string", "number" };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, "'['", "']'", "'{'", "'}'", "'.'", "';'",
            "'%'", "'&'", "'|'", "'^'", "'~'", "'<<'", "'>>'", "'>>>'", null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, "'>'", null, "'<'", null, "'='", "'=='", "'!='", null, "'<='", null,
            "'>='", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            "'#'", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, "','", "':'", "'/'",
            "'-'", "'('", "')'" };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[] { null, "IMPORT", "REVISION", "DIGEST", "SETCIRCUIT", "WAIT", "TRANSFER", "TRANSFERF",
            "TRANSFEREXACT", "TRANSFERFEXACT", "TRANSFERFORCE", "INTO", "FUNCTION", "RETURNS", "RETURN", "REF", "LET",
            "SET", "STATE", "RISING", "FALLING", "PRINT", "FOR", "WHILE", "STEP", "VARIABLE", "LBRACKET", "RBRACKET",
            "LBRACE", "RBRACE", "DOT", "SEMICOLON", "PERCENT", "BIT_AND", "BIT_OR", "BIT_XOR", "BIT_NOT", "SHIFT_LEFT",
            "SHIFT_RIGHT", "SHIFT_UNSIGNED_RIGHT", "IF", "THEN", "ELSE", "HAS", "OVERALL", "SOME", "ONE", "LONE",
            "TRUE", "FALSE", "NOT", "AND", "OR", "GT", "GT_SYMBOL", "LT", "LT_SYMBOL", "EQ", "EQ_SYMBOL", "EQ_ALIAS",
            "NE_SYMBOL", "LE", "LE_SYMBOL", "GE", "GE_SYMBOL", "FROM", "TO", "INPUT", "OUTPUT", "WHERE", "SLOTS",
            "SLOT", "RETAIN", "EACH", "EXCEPT", "FORGET", "EMPTY", "IN", "WITHOUT", "WITH", "TAG", "HASHTAG", "ROUND",
            "ROBIN", "BY", "LABEL", "BLOCK", "TOP", "BOTTOM", "NORTH", "EAST", "SOUTH", "WEST", "SIDE", "LEFT", "RIGHT",
            "FRONT", "BACK", "NULL", "TICKS", "TICK", "SECONDS", "SECOND", "GLOBAL", "PLUS", "REDSTONE", "LEVEL",
            "LEVELS", "PULSE", "DO", "END", "NAME", "EVERY", "COMMA", "COLON", "SLASH", "DASH", "LPAREN", "RPAREN",
            "NUMBER_WITH_G_SUFFIX", "NUMBER", "IDENTIFIER", "STRING", "LINE_COMMENT", "WS", "UNUSED" };
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
                setState(169);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
                    case 1: {
                        setState(168);
                        name();
                    }
                        break;
                }
                setState(174);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 1, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(171);
                                importDeclaration();
                            }
                        }
                    }
                    setState(176);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 1, _ctx);
                }
                setState(186);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(179);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case STATE: {
                                        setState(177);
                                        stateDeclaration();
                                    }
                                        break;
                                    case LET: {
                                        setState(178);
                                        dataDeclaration();
                                    }
                                        break;
                                    default:
                                        throw new NoViableAltException(this);
                                }
                                setState(182);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                if (_la == SEMICOLON) {
                                    {
                                        setState(181);
                                        match(SEMICOLON);
                                    }
                                }

                            }
                        }
                    }
                    setState(188);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
                }
                setState(193);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371743246450690L) != 0)
                    || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 72339069014573055L) != 0)) {
                    {
                        setState(191);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 5, _ctx)) {
                            case 1: {
                                setState(189);
                                functionDefinition();
                            }
                                break;
                            case 2: {
                                setState(190);
                                trigger();
                            }
                                break;
                        }
                    }
                    setState(195);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(196);
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
                setState(198);
                match(IMPORT);
                setState(199);
                identifier();
                setState(200);
                match(FROM);
                setState(201);
                string();
                setState(202);
                match(REVISION);
                setState(203);
                match(NUMBER);
                setState(204);
                match(DIGEST);
                setState(205);
                string();
                setState(207);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SEMICOLON) {
                    {
                        setState(206);
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
            setState(259);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 14, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(209);
                    match(FUNCTION);
                    setState(210);
                    functionName();
                    setState(211);
                    match(LPAREN);
                    setState(220);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371743212863490L) != 0)
                        || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 72339069014573055L) != 0)) {
                        {
                            setState(212);
                            functionParameter();
                            setState(217);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(213);
                                        match(COMMA);
                                        setState(214);
                                        functionParameter();
                                    }
                                }
                                setState(219);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                        }
                    }

                    setState(222);
                    match(RPAREN);
                    setState(223);
                    match(RETURNS);
                    setState(224);
                    dataType();
                    setState(233);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(225);
                            match(DO);
                            setState(226);
                            block();
                            setState(227);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(229);
                            match(LBRACE);
                            setState(230);
                            block();
                            setState(231);
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
                    setState(235);
                    dataType();
                    setState(236);
                    functionName();
                    setState(237);
                    match(LPAREN);
                    setState(246);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371743212863490L) != 0)
                        || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 72339069014573055L) != 0)) {
                        {
                            setState(238);
                            functionParameter();
                            setState(243);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(239);
                                        match(COMMA);
                                        setState(240);
                                        functionParameter();
                                    }
                                }
                                setState(245);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                        }
                    }

                    setState(248);
                    match(RPAREN);
                    setState(257);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case THEN:
                        case DO: {
                            setState(249);
                            _la = _input.LA(1);
                            if (!(_la == THEN || _la == DO)) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                            setState(250);
                            block();
                            setState(251);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(253);
                            match(LBRACE);
                            setState(254);
                            block();
                            setState(255);
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
                setState(261);
                dataName();
                setState(266);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == DOT) {
                    {
                        {
                            setState(262);
                            match(DOT);
                            setState(263);
                            dataName();
                        }
                    }
                    setState(268);
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
                setState(270);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == REF) {
                    {
                        setState(269);
                        match(REF);
                    }
                }

                setState(279);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 17, _ctx)) {
                    case 1: {
                        setState(272);
                        variableName();
                        setState(273);
                        match(COLON);
                        setState(274);
                        dataType();
                    }
                        break;
                    case 2: {
                        setState(276);
                        dataType();
                        setState(277);
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
                setState(281);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 33554446L) != 0) || _la == IDENTIFIER)) {
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
                setState(296);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case IMPORT:
                    case REVISION:
                    case DIGEST:
                    case SETCIRCUIT:
                    case WAIT:
                    case TRANSFER:
                    case TRANSFERF:
                    case TRANSFEREXACT:
                    case TRANSFERFEXACT:
                    case TRANSFERFORCE:
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
                        setState(283);
                        dataName();
                    }
                        break;
                    case LBRACE: {
                        setState(284);
                        match(LBRACE);
                        setState(293);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371743514886146L) != 0)
                            || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 72339069014573055L) != 0)) {
                            {
                                setState(285);
                                typeField();
                                setState(290);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(286);
                                            match(COMMA);
                                            setState(287);
                                            typeField();
                                        }
                                    }
                                    setState(292);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                        }

                        setState(295);
                        match(RBRACE);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(301);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACKET) {
                    {
                        {
                            setState(298);
                            typeDimension();
                        }
                    }
                    setState(303);
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
                setState(304);
                match(LBRACKET);
                setState(306);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == NUMBER) {
                    {
                        setState(305);
                        match(NUMBER);
                    }
                }

                setState(308);
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
                setState(310);
                dataName();
                setState(311);
                match(COLON);
                setState(312);
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
                setState(314);
                functionName();
                setState(315);
                match(LPAREN);
                setState(324);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371674426277890L) != 0)
                    || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 259238453550448639L) != 0)) {
                    {
                        setState(316);
                        functionArgument();
                        setState(321);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(317);
                                    match(COMMA);
                                    setState(318);
                                    functionArgument();
                                }
                            }
                            setState(323);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(326);
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
            setState(331);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case REF:
                    enterOuterAlt(_localctx, 1); {
                    setState(328);
                    match(REF);
                    setState(329);
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
                case TRANSFEREXACT:
                case TRANSFERFEXACT:
                case TRANSFERFORCE:
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
                    setState(330);
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
                setState(333);
                match(RETURN);
                setState(335);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 26, _ctx)) {
                    case 1: {
                        setState(334);
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
                setState(337);
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
                setState(339);
                match(NAME);
                setState(340);
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
            setState(382);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 30, _ctx)) {
                case 1:
                    _localctx = new TimerTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(342);
                    match(EVERY);
                    setState(343);
                    interval();
                    setState(352);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(344);
                            match(DO);
                            setState(345);
                            block();
                            setState(346);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(348);
                            match(LBRACE);
                            setState(349);
                            block();
                            setState(350);
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
                    setState(354);
                    match(EVERY);
                    setState(355);
                    match(REDSTONE);
                    setState(356);
                    match(PULSE);
                    setState(365);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(357);
                            match(DO);
                            setState(358);
                            block();
                            setState(359);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(361);
                            match(LBRACE);
                            setState(362);
                            block();
                            setState(363);
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
                    setState(367);
                    match(EVERY);
                    setState(368);
                    match(REDSTONE);
                    setState(369);
                    _la = _input.LA(1);
                    if (!(_la == RISING || _la == FALLING || _la == PULSE)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(370);
                    match(FROM);
                    setState(371);
                    labelAccess();
                    setState(380);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(372);
                            match(DO);
                            setState(373);
                            block();
                            setState(374);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(376);
                            match(LBRACE);
                            setState(377);
                            block();
                            setState(378);
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

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
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

        public TerminalNode LPAREN() {
            return getToken(SFMLParser.LPAREN, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
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
            setState(413);
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
                    setState(385);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == NUMBER) {
                        {
                            setState(384);
                            match(NUMBER);
                        }
                    }

                    setState(388);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == GLOBAL) {
                        {
                            setState(387);
                            match(GLOBAL);
                        }
                    }

                    setState(398);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(390);
                            match(PLUS);
                            setState(396);
                            _errHandler.sync(this);
                            switch (_input.LA(1)) {
                                case NUMBER: {
                                    setState(391);
                                    match(NUMBER);
                                }
                                    break;
                                case LPAREN: {
                                    setState(392);
                                    match(LPAREN);
                                    setState(393);
                                    dataExpression();
                                    setState(394);
                                    match(RPAREN);
                                }
                                    break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                        }
                    }

                    setState(400);
                    _la = _input.LA(1);
                    if (!(((((_la - 99)) & ~0x3f) == 0 && ((1L << (_la - 99)) & 15L) != 0))) {
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
                    setState(401);
                    match(NUMBER_WITH_G_SUFFIX);
                    setState(410);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(402);
                            match(PLUS);
                            setState(408);
                            _errHandler.sync(this);
                            switch (_input.LA(1)) {
                                case NUMBER: {
                                    setState(403);
                                    match(NUMBER);
                                }
                                    break;
                                case LPAREN: {
                                    setState(404);
                                    match(LPAREN);
                                    setState(405);
                                    dataExpression();
                                    setState(406);
                                    match(RPAREN);
                                }
                                    break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                        }
                    }

                    setState(412);
                    _la = _input.LA(1);
                    if (!(((((_la - 99)) & ~0x3f) == 0 && ((1L << (_la - 99)) & 15L) != 0))) {
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
                setState(418);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 38, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(415);
                                statement();
                            }
                        }
                    }
                    setState(420);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 38, _ctx);
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

        public BreakStatementContext breakStatement() {
            return getRuleContext(BreakStatementContext.class, 0);
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
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(437);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 39, _ctx)) {
                    case 1: {
                        setState(421);
                        circuitStatement();
                    }
                        break;
                    case 2: {
                        setState(422);
                        breakStatement();
                    }
                        break;
                    case 3: {
                        setState(423);
                        waitStatement();
                    }
                        break;
                    case 4: {
                        setState(424);
                        dataDeclaration();
                    }
                        break;
                    case 5: {
                        setState(425);
                        dataAssignment();
                    }
                        break;
                    case 6: {
                        setState(426);
                        printStatement();
                    }
                        break;
                    case 7: {
                        setState(427);
                        transferStatement();
                    }
                        break;
                    case 8: {
                        setState(428);
                        returnStatement();
                    }
                        break;
                    case 9: {
                        setState(429);
                        inputStatement();
                    }
                        break;
                    case 10: {
                        setState(430);
                        redstoneOutputStatement();
                    }
                        break;
                    case 11: {
                        setState(431);
                        outputStatement();
                    }
                        break;
                    case 12: {
                        setState(432);
                        ifStatement();
                    }
                        break;
                    case 13: {
                        setState(433);
                        forStatement();
                    }
                        break;
                    case 14: {
                        setState(434);
                        whileStatement();
                    }
                        break;
                    case 15: {
                        setState(435);
                        forgetStatement();
                    }
                        break;
                    case 16: {
                        setState(436);
                        functionCallStatement();
                    }
                        break;
                }
                setState(440);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 40, _ctx)) {
                    case 1: {
                        setState(439);
                        match(SEMICOLON);
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
    public static class BreakStatementContext extends ParserRuleContext {

        public TerminalNode IDENTIFIER() {
            return getToken(SFMLParser.IDENTIFIER, 0);
        }

        public BreakStatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_breakStatement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBreakStatement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBreakStatement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor) return ((SFMLVisitor<? extends T>) visitor).visitBreakStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final BreakStatementContext breakStatement() throws RecognitionException {
        BreakStatementContext _localctx = new BreakStatementContext(_ctx, getState());
        enterRule(_localctx, 36, RULE_breakStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(442);
                if (!("break".equalsIgnoreCase(
                    _input.LT(1)
                        .getText())))
                    throw new FailedPredicateException(this, "\"break\".equalsIgnoreCase(_input.LT(1).getText())");
                setState(443);
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
        enterRule(_localctx, 38, RULE_redstoneOutputStatement);
        int _la;
        try {
            setState(473);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(445);
                    match(OUTPUT);
                    setState(447);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(446);
                            quantity();
                        }
                    }

                    setState(449);
                    match(REDSTONE);
                    setState(450);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(452);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(451);
                            quantity();
                        }
                    }

                    setState(454);
                    match(TO);
                    setState(456);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 43, _ctx)) {
                        case 1: {
                            setState(455);
                            match(EACH);
                        }
                            break;
                    }
                    setState(458);
                    labelAccess();
                }
                    break;
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(459);
                    match(TO);
                    setState(461);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 44, _ctx)) {
                        case 1: {
                            setState(460);
                            match(EACH);
                        }
                            break;
                    }
                    setState(463);
                    labelAccess();
                    setState(464);
                    match(OUTPUT);
                    setState(466);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(465);
                            quantity();
                        }
                    }

                    setState(468);
                    match(REDSTONE);
                    setState(469);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(471);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 46, _ctx)) {
                        case 1: {
                            setState(470);
                            quantity();
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
        enterRule(_localctx, 40, RULE_forgetStatement);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(475);
                match(FORGET);
                setState(477);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 48, _ctx)) {
                    case 1: {
                        setState(476);
                        label();
                    }
                        break;
                }
                setState(483);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 49, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(479);
                                match(COMMA);
                                setState(480);
                                label();
                            }
                        }
                    }
                    setState(485);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 49, _ctx);
                }
                setState(487);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 50, _ctx)) {
                    case 1: {
                        setState(486);
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
        enterRule(_localctx, 42, RULE_inputStatement);
        int _la;
        try {
            setState(513);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case INPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(489);
                    match(INPUT);
                    setState(491);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33521662L) != 0)
                        || ((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & 2005573159715009L) != 0)) {
                        {
                            setState(490);
                            inputResourceLimits();
                        }
                    }

                    setState(494);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(493);
                            resourceExclusion();
                        }
                    }

                    setState(496);
                    match(FROM);
                    setState(498);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 53, _ctx)) {
                        case 1: {
                            setState(497);
                            match(EACH);
                        }
                            break;
                    }
                    setState(500);
                    labelAccess();
                }
                    break;
                case FROM:
                    enterOuterAlt(_localctx, 2); {
                    setState(501);
                    match(FROM);
                    setState(503);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 54, _ctx)) {
                        case 1: {
                            setState(502);
                            match(EACH);
                        }
                            break;
                    }
                    setState(505);
                    labelAccess();
                    setState(506);
                    match(INPUT);
                    setState(508);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 55, _ctx)) {
                        case 1: {
                            setState(507);
                            inputResourceLimits();
                        }
                            break;
                    }
                    setState(511);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 56, _ctx)) {
                        case 1: {
                            setState(510);
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
        enterRule(_localctx, 44, RULE_outputStatement);
        int _la;
        try {
            setState(545);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(515);
                    match(OUTPUT);
                    setState(517);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33521662L) != 0)
                        || ((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & 2005573159715009L) != 0)) {
                        {
                            setState(516);
                            outputResourceLimits();
                        }
                    }

                    setState(520);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(519);
                            resourceExclusion();
                        }
                    }

                    setState(522);
                    match(TO);
                    setState(524);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 60, _ctx)) {
                        case 1: {
                            setState(523);
                            emptyslots();
                        }
                            break;
                    }
                    setState(527);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 61, _ctx)) {
                        case 1: {
                            setState(526);
                            match(EACH);
                        }
                            break;
                    }
                    setState(529);
                    labelAccess();
                }
                    break;
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(530);
                    match(TO);
                    setState(532);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 62, _ctx)) {
                        case 1: {
                            setState(531);
                            emptyslots();
                        }
                            break;
                    }
                    setState(535);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 63, _ctx)) {
                        case 1: {
                            setState(534);
                            match(EACH);
                        }
                            break;
                    }
                    setState(537);
                    labelAccess();
                    setState(538);
                    match(OUTPUT);
                    setState(540);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 64, _ctx)) {
                        case 1: {
                            setState(539);
                            outputResourceLimits();
                        }
                            break;
                    }
                    setState(543);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 65, _ctx)) {
                        case 1: {
                            setState(542);
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
        enterRule(_localctx, 46, RULE_inputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(547);
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
        enterRule(_localctx, 48, RULE_outputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(549);
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
        enterRule(_localctx, 50, RULE_resourceLimitList);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(551);
                resourceLimit();
                setState(556);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 67, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(552);
                                match(COMMA);
                                setState(553);
                                resourceLimit();
                            }
                        }
                    }
                    setState(558);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 67, _ctx);
                }
                setState(560);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 68, _ctx)) {
                    case 1: {
                        setState(559);
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
        enterRule(_localctx, 52, RULE_resourceLimit);
        int _la;
        try {
            setState(574);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 72, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(563);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & 316659348799489L) != 0)) {
                        {
                            setState(562);
                            limit();
                        }
                    }

                    setState(565);
                    resourceIdDisjunction();
                    setState(567);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 70, _ctx)) {
                        case 1: {
                            setState(566);
                            with();
                        }
                            break;
                    }
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(569);
                    limit();
                    setState(571);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 71, _ctx)) {
                        case 1: {
                            setState(570);
                            with();
                        }
                            break;
                    }
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(573);
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
        enterRule(_localctx, 54, RULE_limit);
        try {
            setState(581);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 73, _ctx)) {
                case 1:
                    _localctx = new QuantityRetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(576);
                    quantity();
                    setState(577);
                    retention();
                }
                    break;
                case 2:
                    _localctx = new RetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(579);
                    retention();
                }
                    break;
                case 3:
                    _localctx = new QuantityLimitContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(580);
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
        enterRule(_localctx, 56, RULE_quantity);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(588);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(583);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(584);
                        match(LPAREN);
                        setState(585);
                        dataExpression();
                        setState(586);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(591);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 75, _ctx)) {
                    case 1: {
                        setState(590);
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
        enterRule(_localctx, 58, RULE_retention);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(593);
                match(RETAIN);
                setState(599);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(594);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(595);
                        match(LPAREN);
                        setState(596);
                        dataExpression();
                        setState(597);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(602);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 77, _ctx)) {
                    case 1: {
                        setState(601);
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
        enterRule(_localctx, 60, RULE_resourceExclusion);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(604);
                match(EXCEPT);
                setState(605);
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
        enterRule(_localctx, 62, RULE_resourceId);
        try {
            setState(627);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case IMPORT:
                case REVISION:
                case DIGEST:
                case SETCIRCUIT:
                case WAIT:
                case TRANSFER:
                case TRANSFERF:
                case TRANSFEREXACT:
                case TRANSFERFEXACT:
                case TRANSFERFORCE:
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
                        setState(607);
                        identifier();
                    }
                    setState(624);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 83, _ctx)) {
                        case 1: {
                            setState(608);
                            match(COLON);
                            setState(610);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 78, _ctx)) {
                                case 1: {
                                    setState(609);
                                    identifier();
                                }
                                    break;
                            }
                            setState(622);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 82, _ctx)) {
                                case 1: {
                                    setState(612);
                                    match(COLON);
                                    setState(614);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 79, _ctx)) {
                                        case 1: {
                                            setState(613);
                                            identifier();
                                        }
                                            break;
                                    }
                                    setState(620);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 81, _ctx)) {
                                        case 1: {
                                            setState(616);
                                            match(COLON);
                                            setState(618);
                                            _errHandler.sync(this);
                                            switch (getInterpreter().adaptivePredict(_input, 80, _ctx)) {
                                                case 1: {
                                                    setState(617);
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
                    setState(626);
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
        enterRule(_localctx, 64, RULE_resourceIdList);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(629);
                resourceId();
                setState(634);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 85, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(630);
                                match(COMMA);
                                setState(631);
                                resourceId();
                            }
                        }
                    }
                    setState(636);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 85, _ctx);
                }
                setState(638);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 86, _ctx)) {
                    case 1: {
                        setState(637);
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
        enterRule(_localctx, 66, RULE_resourceIdDisjunction);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(640);
                resourceId();
                setState(645);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 87, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(641);
                                match(OR);
                                setState(642);
                                resourceId();
                            }
                        }
                    }
                    setState(647);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 87, _ctx);
                }
                setState(649);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 88, _ctx)) {
                    case 1: {
                        setState(648);
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
        enterRule(_localctx, 68, RULE_with);
        try {
            setState(655);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case WITH:
                    enterOuterAlt(_localctx, 1); {
                    setState(651);
                    match(WITH);
                    setState(652);
                    withClause(0);
                }
                    break;
                case WITHOUT:
                    enterOuterAlt(_localctx, 2); {
                    setState(653);
                    match(WITHOUT);
                    setState(654);
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
        int _startState = 70;
        enterRecursionRule(_localctx, 70, RULE_withClause, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(672);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case LPAREN: {
                        _localctx = new WithParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(658);
                        match(LPAREN);
                        setState(659);
                        withClause(0);
                        setState(660);
                        match(RPAREN);
                    }
                        break;
                    case NOT: {
                        _localctx = new WithNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(662);
                        match(NOT);
                        setState(663);
                        withClause(4);
                    }
                        break;
                    case TAG:
                    case HASHTAG: {
                        _localctx = new WithTagContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(669);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case TAG: {
                                setState(664);
                                match(TAG);
                                setState(666);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                if (_la == HASHTAG) {
                                    {
                                        setState(665);
                                        match(HASHTAG);
                                    }
                                }

                            }
                                break;
                            case HASHTAG: {
                                setState(668);
                                match(HASHTAG);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(671);
                        tagMatcher();
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(682);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 94, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(680);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 93, _ctx)) {
                                case 1: {
                                    _localctx = new WithConjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(674);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(675);
                                    match(AND);
                                    setState(676);
                                    withClause(4);
                                }
                                    break;
                                case 2: {
                                    _localctx = new WithDisjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(677);
                                    if (!(precpred(_ctx, 2)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                                    setState(678);
                                    match(OR);
                                    setState(679);
                                    withClause(3);
                                }
                                    break;
                            }
                        }
                    }
                    setState(684);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 94, _ctx);
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
        enterRule(_localctx, 72, RULE_tagMatcher);
        try {
            int _alt;
            setState(703);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 97, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(685);
                    identifier();
                    setState(686);
                    match(COLON);
                    setState(687);
                    identifier();
                    setState(692);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 95, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(688);
                                    match(SLASH);
                                    setState(689);
                                    identifier();
                                }
                            }
                        }
                        setState(694);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 95, _ctx);
                    }
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(695);
                    identifier();
                    setState(700);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 96, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(696);
                                    match(SLASH);
                                    setState(697);
                                    identifier();
                                }
                            }
                        }
                        setState(702);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 96, _ctx);
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
        enterRule(_localctx, 74, RULE_sidequalifier);
        int _la;
        try {
            setState(717);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case EACH:
                    _localctx = new EachSideContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(705);
                    match(EACH);
                    setState(706);
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
                    setState(707);
                    side();
                    setState(712);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(708);
                                match(COMMA);
                                setState(709);
                                side();
                            }
                        }
                        setState(714);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(715);
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
        enterRule(_localctx, 76, RULE_side);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(719);
                _la = _input.LA(1);
                if (!(((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & 4031L) != 0))) {
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
        enterRule(_localctx, 78, RULE_slotqualifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(721);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(722);
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
        enterRule(_localctx, 80, RULE_rangeset);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(724);
                range();
                setState(729);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 100, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(725);
                                match(COMMA);
                                setState(726);
                                range();
                            }
                        }
                    }
                    setState(731);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 100, _ctx);
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
        enterRule(_localctx, 82, RULE_range);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(732);
                number();
                setState(735);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 101, _ctx)) {
                    case 1: {
                        setState(733);
                        match(DASH);
                        setState(734);
                        number();
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
        enterRule(_localctx, 84, RULE_ifStatement);
        int _la;
        try {
            int _alt;
            setState(782);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 106, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(737);
                    match(IF);
                    setState(738);
                    boolexpr(0);
                    setState(739);
                    match(THEN);
                    setState(740);
                    block();
                    setState(749);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 102, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(741);
                                    match(ELSE);
                                    setState(742);
                                    match(IF);
                                    setState(743);
                                    boolexpr(0);
                                    setState(744);
                                    match(THEN);
                                    setState(745);
                                    block();
                                }
                            }
                        }
                        setState(751);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 102, _ctx);
                    }
                    setState(754);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == ELSE) {
                        {
                            setState(752);
                            match(ELSE);
                            setState(753);
                            block();
                        }
                    }

                    setState(756);
                    match(END);
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(758);
                    match(IF);
                    setState(759);
                    boolexpr(0);
                    setState(760);
                    match(LBRACE);
                    setState(761);
                    block();
                    setState(762);
                    match(RBRACE);
                    setState(772);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 104, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(763);
                                    match(ELSE);
                                    setState(764);
                                    match(IF);
                                    setState(765);
                                    boolexpr(0);
                                    setState(766);
                                    match(LBRACE);
                                    setState(767);
                                    block();
                                    setState(768);
                                    match(RBRACE);
                                }
                            }
                        }
                        setState(774);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 104, _ctx);
                    }
                    setState(780);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 105, _ctx)) {
                        case 1: {
                            setState(775);
                            match(ELSE);
                            setState(776);
                            match(LBRACE);
                            setState(777);
                            block();
                            setState(778);
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
        enterRule(_localctx, 86, RULE_forStatement);
        int _la;
        try {
            setState(860);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 115, _ctx)) {
                case 1:
                    _localctx = new CountedForContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(784);
                    match(FOR);
                    setState(785);
                    variableName();
                    setState(786);
                    match(EQ_SYMBOL);
                    setState(787);
                    dataExpression();
                    setState(788);
                    match(TO);
                    setState(789);
                    dataExpression();
                    setState(792);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == STEP) {
                        {
                            setState(790);
                            match(STEP);
                            setState(791);
                            dataExpression();
                        }
                    }

                    setState(802);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(794);
                            match(DO);
                            setState(795);
                            block();
                            setState(796);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(798);
                            match(LBRACE);
                            setState(799);
                            block();
                            setState(800);
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
                    setState(804);
                    match(FOR);
                    setState(805);
                    variableName();
                    setState(806);
                    match(IN);
                    setState(807);
                    dataExpression();
                    setState(816);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(808);
                            match(DO);
                            setState(809);
                            block();
                            setState(810);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(812);
                            match(LBRACE);
                            setState(813);
                            block();
                            setState(814);
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
                    setState(818);
                    match(FOR);
                    setState(819);
                    match(LPAREN);
                    setState(821);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371743481331714L) != 0)
                        || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 72339069014573055L) != 0)) {
                        {
                            setState(820);
                            forInitializer();
                        }
                    }

                    setState(823);
                    match(SEMICOLON);
                    setState(825);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371674426310658L) != 0)
                        || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 259238453550448639L) != 0)) {
                        {
                            setState(824);
                            dataExpression();
                        }
                    }

                    setState(827);
                    match(SEMICOLON);
                    setState(829);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371743481331714L) != 0)
                        || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 72339069014573055L) != 0)) {
                        {
                            setState(828);
                            forUpdate();
                        }
                    }

                    setState(831);
                    match(RPAREN);
                    setState(840);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(832);
                            match(DO);
                            setState(833);
                            block();
                            setState(834);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(836);
                            match(LBRACE);
                            setState(837);
                            block();
                            setState(838);
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
                    setState(842);
                    match(FOR);
                    setState(843);
                    match(LPAREN);
                    setState(844);
                    variableName();
                    setState(845);
                    match(COMMA);
                    setState(846);
                    variableName();
                    setState(847);
                    match(RPAREN);
                    setState(848);
                    match(IN);
                    setState(849);
                    dataExpression();
                    setState(858);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(850);
                            match(DO);
                            setState(851);
                            block();
                            setState(852);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(854);
                            match(LBRACE);
                            setState(855);
                            block();
                            setState(856);
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
        enterRule(_localctx, 88, RULE_forInitializer);
        try {
            setState(865);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 116, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(862);
                    dataDeclaration();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(863);
                    dataAssignment();
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(864);
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
        enterRule(_localctx, 90, RULE_forUpdate);
        try {
            setState(869);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 117, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(867);
                    dataAssignment();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(868);
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
        enterRule(_localctx, 92, RULE_whileStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(871);
                match(WHILE);
                setState(872);
                boolexpr(0);
                setState(881);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case DO: {
                        setState(873);
                        match(DO);
                        setState(874);
                        block();
                        setState(875);
                        match(END);
                    }
                        break;
                    case LBRACE: {
                        setState(877);
                        match(LBRACE);
                        setState(878);
                        block();
                        setState(879);
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

    @SuppressWarnings("CheckReturnValue")
    public static class BooleanLabelledRedstoneContext extends BoolexprContext {

        public LabelAccessContext labelAccess() {
            return getRuleContext(LabelAccessContext.class, 0);
        }

        public TerminalNode REDSTONE() {
            return getToken(SFMLParser.REDSTONE, 0);
        }

        public SetOpContext setOp() {
            return getRuleContext(SetOpContext.class, 0);
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

        public BooleanLabelledRedstoneContext(BoolexprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterBooleanLabelledRedstone(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitBooleanLabelledRedstone(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitBooleanLabelledRedstone(this);
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
        int _startState = 94;
        enterRecursionRule(_localctx, 94, RULE_boolexpr, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(951);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 131, _ctx)) {
                    case 1: {
                        _localctx = new BooleanTrueContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(884);
                        match(TRUE);
                    }
                        break;
                    case 2: {
                        _localctx = new BooleanFalseContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(885);
                        match(FALSE);
                    }
                        break;
                    case 3: {
                        _localctx = new BooleanParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(886);
                        match(LPAREN);
                        setState(887);
                        boolexpr(0);
                        setState(888);
                        match(RPAREN);
                    }
                        break;
                    case 4: {
                        _localctx = new BooleanDataContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(890);
                        match(LPAREN);
                        setState(891);
                        dataExpression();
                        setState(892);
                        match(RPAREN);
                    }
                        break;
                    case 5: {
                        _localctx = new BooleanNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(894);
                        match(NOT);
                        setState(895);
                        boolexpr(6);
                    }
                        break;
                    case 6: {
                        _localctx = new BooleanHasContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(897);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 119, _ctx)) {
                            case 1: {
                                setState(896);
                                setOp();
                            }
                                break;
                        }
                        setState(899);
                        labelAccess();
                        setState(900);
                        match(HAS);
                        setState(901);
                        comparisonOp();
                        setState(907);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case NUMBER: {
                                setState(902);
                                number();
                            }
                                break;
                            case LPAREN: {
                                setState(903);
                                match(LPAREN);
                                setState(904);
                                dataExpression();
                                setState(905);
                                match(RPAREN);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(910);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 121, _ctx)) {
                            case 1: {
                                setState(909);
                                resourceIdDisjunction();
                            }
                                break;
                        }
                        setState(913);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 122, _ctx)) {
                            case 1: {
                                setState(912);
                                with();
                            }
                                break;
                        }
                        setState(917);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 123, _ctx)) {
                            case 1: {
                                setState(915);
                                match(EXCEPT);
                                setState(916);
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
                        setState(919);
                        match(REDSTONE);
                        setState(921);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 124, _ctx)) {
                            case 1: {
                                setState(920);
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
                        setState(931);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 126, _ctx)) {
                            case 1: {
                                setState(923);
                                comparisonOp();
                                setState(929);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case NUMBER: {
                                        setState(924);
                                        number();
                                    }
                                        break;
                                    case LPAREN: {
                                        setState(925);
                                        match(LPAREN);
                                        setState(926);
                                        dataExpression();
                                        setState(927);
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
                    case 8: {
                        _localctx = new BooleanLabelledRedstoneContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(934);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 127, _ctx)) {
                            case 1: {
                                setState(933);
                                setOp();
                            }
                                break;
                        }
                        setState(936);
                        labelAccess();
                        setState(937);
                        match(REDSTONE);
                        setState(939);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 128, _ctx)) {
                            case 1: {
                                setState(938);
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
                        setState(949);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 130, _ctx)) {
                            case 1: {
                                setState(941);
                                comparisonOp();
                                setState(947);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case NUMBER: {
                                        setState(942);
                                        number();
                                    }
                                        break;
                                    case LPAREN: {
                                        setState(943);
                                        match(LPAREN);
                                        setState(944);
                                        dataExpression();
                                        setState(945);
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
                setState(961);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 133, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(959);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 132, _ctx)) {
                                case 1: {
                                    _localctx = new BooleanConjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(953);
                                    if (!(precpred(_ctx, 5)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 5)");
                                    setState(954);
                                    match(AND);
                                    setState(955);
                                    boolexpr(6);
                                }
                                    break;
                                case 2: {
                                    _localctx = new BooleanDisjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(956);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(957);
                                    match(OR);
                                    setState(958);
                                    boolexpr(5);
                                }
                                    break;
                            }
                        }
                    }
                    setState(963);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 133, _ctx);
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
        enterRule(_localctx, 96, RULE_comparisonOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(964);
                _la = _input.LA(1);
                if (!(((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 4095L) != 0))) {
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
        enterRule(_localctx, 98, RULE_setOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(966);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 263882790666240L) != 0) || _la == EACH || _la == EVERY)) {
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
        enterRule(_localctx, 100, RULE_labelAccess);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(968);
                label();
                setState(973);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 134, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(969);
                                match(COMMA);
                                setState(970);
                                label();
                            }
                        }
                    }
                    setState(975);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 134, _ctx);
                }
                setState(977);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 135, _ctx)) {
                    case 1: {
                        setState(976);
                        roundrobin();
                    }
                        break;
                }
                setState(980);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 136, _ctx)) {
                    case 1: {
                        setState(979);
                        sidequalifier();
                    }
                        break;
                }
                setState(983);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 137, _ctx)) {
                    case 1: {
                        setState(982);
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
        enterRule(_localctx, 102, RULE_roundrobin);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(985);
                match(ROUND);
                setState(986);
                match(ROBIN);
                setState(987);
                match(BY);
                setState(988);
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
        enterRule(_localctx, 104, RULE_label);
        try {
            int _alt;
            setState(1012);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 141, _ctx)) {
                case 1:
                    _localctx = new RawLabelContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(990);
                    identifier();
                    setState(994);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 138, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(991);
                                    dataSuffix();
                                }
                            }
                        }
                        setState(996);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 138, _ctx);
                    }
                }
                    break;
                case 2:
                    _localctx = new StringLabelContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(997);
                    string();
                }
                    break;
                case 3:
                    _localctx = new VariableLabelContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(998);
                    match(VARIABLE);
                    setState(1002);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 139, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(999);
                                    dataSuffix();
                                }
                            }
                        }
                        setState(1004);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 139, _ctx);
                    }
                }
                    break;
                case 4:
                    _localctx = new CallLabelContext(_localctx);
                    enterOuterAlt(_localctx, 4); {
                    setState(1005);
                    functionCall();
                    setState(1009);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 140, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(1006);
                                    dataSuffix();
                                }
                            }
                        }
                        setState(1011);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 140, _ctx);
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
        enterRule(_localctx, 106, RULE_emptyslots);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1014);
                match(EMPTY);
                setState(1015);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(1016);
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

        public TerminalNode TRANSFEREXACT() {
            return getToken(SFMLParser.TRANSFEREXACT, 0);
        }

        public TerminalNode TRANSFERFEXACT() {
            return getToken(SFMLParser.TRANSFERFEXACT, 0);
        }

        public TerminalNode TRANSFERFORCE() {
            return getToken(SFMLParser.TRANSFERFORCE, 0);
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
        enterRule(_localctx, 108, RULE_identifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1018);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 33521662L) != 0)
                    || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & 17181820803L) != 0))) {
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
        enterRule(_localctx, 110, RULE_stateDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1020);
                match(STATE);
                setState(1021);
                variableName();
                setState(1022);
                match(EQ_SYMBOL);
                setState(1023);
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
        enterRule(_localctx, 112, RULE_dataDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1025);
                match(LET);
                setState(1026);
                variableName();
                setState(1027);
                match(EQ_SYMBOL);
                setState(1028);
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
        enterRule(_localctx, 114, RULE_dataAssignment);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1031);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SET) {
                    {
                        setState(1030);
                        match(SET);
                    }
                }

                setState(1033);
                dataTarget();
                setState(1034);
                match(EQ_SYMBOL);
                setState(1035);
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
        enterRule(_localctx, 116, RULE_circuitStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1037);
                match(SETCIRCUIT);
                setState(1038);
                match(LPAREN);
                setState(1039);
                dataExpression();
                setState(1040);
                match(COMMA);
                setState(1041);
                dataExpression();
                setState(1042);
                match(RPAREN);
                setState(1045);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 143, _ctx)) {
                    case 1: {
                        setState(1043);
                        match(INTO);
                        setState(1044);
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
        enterRule(_localctx, 118, RULE_waitStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1047);
                match(WAIT);
                setState(1048);
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
        enterRule(_localctx, 120, RULE_printStatement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1050);
                match(PRINT);
                setState(1051);
                match(LPAREN);
                setState(1052);
                dataExpression();
                setState(1057);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(1053);
                            match(COMMA);
                            setState(1054);
                            dataExpression();
                        }
                    }
                    setState(1059);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(1060);
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

        public TerminalNode TRANSFEREXACT() {
            return getToken(SFMLParser.TRANSFEREXACT, 0);
        }

        public TerminalNode TRANSFERFEXACT() {
            return getToken(SFMLParser.TRANSFERFEXACT, 0);
        }

        public TerminalNode TRANSFERFORCE() {
            return getToken(SFMLParser.TRANSFERFORCE, 0);
        }

        public TransferExclusionContext transferExclusion() {
            return getRuleContext(TransferExclusionContext.class, 0);
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
        enterRule(_localctx, 122, RULE_transferStatement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1062);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 1984L) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(1063);
                match(LPAREN);
                setState(1064);
                dataExpression();
                setState(1065);
                match(COMMA);
                setState(1066);
                dataExpression();
                setState(1073);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(1067);
                        match(COMMA);
                        setState(1068);
                        dataExpression();
                        setState(1071);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == COMMA) {
                            {
                                setState(1069);
                                match(COMMA);
                                setState(1070);
                                dataExpression();
                            }
                        }

                    }
                }

                setState(1075);
                match(RPAREN);
                setState(1077);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 147, _ctx)) {
                    case 1: {
                        setState(1076);
                        transferExclusion();
                    }
                        break;
                }
                setState(1081);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 148, _ctx)) {
                    case 1: {
                        setState(1079);
                        match(INTO);
                        setState(1080);
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
    public static class TransferExclusionContext extends ParserRuleContext {

        public TerminalNode EXCEPT() {
            return getToken(SFMLParser.EXCEPT, 0);
        }

        public FunctionCallContext functionCall() {
            return getRuleContext(FunctionCallContext.class, 0);
        }

        public VariableNameContext variableName() {
            return getRuleContext(VariableNameContext.class, 0);
        }

        public TransferExclusionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_transferExclusion;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).enterTransferExclusion(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof SFMLListener) ((SFMLListener) listener).exitTransferExclusion(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof SFMLVisitor)
                return ((SFMLVisitor<? extends T>) visitor).visitTransferExclusion(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TransferExclusionContext transferExclusion() throws RecognitionException {
        TransferExclusionContext _localctx = new TransferExclusionContext(_ctx, getState());
        enterRule(_localctx, 124, RULE_transferExclusion);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1083);
                match(EXCEPT);
                setState(1086);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 149, _ctx)) {
                    case 1: {
                        setState(1084);
                        functionCall();
                    }
                        break;
                    case 2: {
                        setState(1085);
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
        enterRule(_localctx, 126, RULE_dataTarget);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1088);
                variableName();
                setState(1097);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACKET || _la == DOT) {
                    {
                        setState(1095);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case DOT: {
                                setState(1089);
                                match(DOT);
                                setState(1090);
                                dataName();
                            }
                                break;
                            case LBRACKET: {
                                setState(1091);
                                match(LBRACKET);
                                setState(1092);
                                dataExpression();
                                setState(1093);
                                match(RBRACKET);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                    }
                    setState(1099);
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
        enterRule(_localctx, 128, RULE_dataExpression);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1100);
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
        enterRule(_localctx, 130, RULE_dataOr);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1102);
                dataAnd();
                setState(1107);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 152, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1103);
                                match(OR);
                                setState(1104);
                                dataAnd();
                            }
                        }
                    }
                    setState(1109);
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
        enterRule(_localctx, 132, RULE_dataAnd);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1110);
                dataNot();
                setState(1115);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 153, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1111);
                                match(AND);
                                setState(1112);
                                dataNot();
                            }
                        }
                    }
                    setState(1117);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 153, _ctx);
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
        enterRule(_localctx, 134, RULE_dataNot);
        try {
            setState(1121);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 154, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(1118);
                    match(NOT);
                    setState(1119);
                    dataNot();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(1120);
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
        enterRule(_localctx, 136, RULE_dataComparison);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1123);
                dataBitOr();
                setState(1127);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 155, _ctx)) {
                    case 1: {
                        setState(1124);
                        comparisonOp();
                        setState(1125);
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
        enterRule(_localctx, 138, RULE_dataBitOr);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1129);
                dataBitXor();
                setState(1134);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 156, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1130);
                                match(BIT_OR);
                                setState(1131);
                                dataBitXor();
                            }
                        }
                    }
                    setState(1136);
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
        enterRule(_localctx, 140, RULE_dataBitXor);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1137);
                dataBitAnd();
                setState(1142);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 157, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1138);
                                match(BIT_XOR);
                                setState(1139);
                                dataBitAnd();
                            }
                        }
                    }
                    setState(1144);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 157, _ctx);
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
        enterRule(_localctx, 142, RULE_dataBitAnd);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1145);
                dataShift();
                setState(1150);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 158, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1146);
                                match(BIT_AND);
                                setState(1147);
                                dataShift();
                            }
                        }
                    }
                    setState(1152);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 158, _ctx);
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
        enterRule(_localctx, 144, RULE_dataShift);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1153);
                dataAdd();
                setState(1158);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 159, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1154);
                                _la = _input.LA(1);
                                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 962072674304L) != 0))) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(1155);
                                dataAdd();
                            }
                        }
                    }
                    setState(1160);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 159, _ctx);
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
        enterRule(_localctx, 146, RULE_dataAdd);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1161);
                dataMultiply();
                setState(1166);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 160, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1162);
                                _la = _input.LA(1);
                                if (!(_la == PLUS || _la == DASH)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(1163);
                                dataMultiply();
                            }
                        }
                    }
                    setState(1168);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 160, _ctx);
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
        enterRule(_localctx, 148, RULE_dataMultiply);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1169);
                dataSign();
                setState(1178);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 162, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1173);
                                _errHandler.sync(this);
                                switch (getInterpreter().adaptivePredict(_input, 161, _ctx)) {
                                    case 1: {
                                        setState(1170);
                                        multiplyOperator();
                                    }
                                        break;
                                    case 2: {
                                        setState(1171);
                                        match(SLASH);
                                    }
                                        break;
                                    case 3: {
                                        setState(1172);
                                        match(PERCENT);
                                    }
                                        break;
                                }
                                setState(1175);
                                dataSign();
                            }
                        }
                    }
                    setState(1180);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 162, _ctx);
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
        enterRule(_localctx, 150, RULE_multiplyOperator);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1181);
                if (!(_input.LT(1)
                    .getText()
                    .equals("*"))) throw new FailedPredicateException(this, "_input.LT(1).getText().equals(\"*\")");
                setState(1182);
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
        enterRule(_localctx, 152, RULE_dataSign);
        int _la;
        try {
            setState(1187);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 163, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(1184);
                    _la = _input.LA(1);
                    if (!(_la == BIT_NOT || _la == PLUS || _la == DASH)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(1185);
                    dataSign();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(1186);
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
        enterRule(_localctx, 154, RULE_dataPostfix);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1189);
                dataPrimary();
                setState(1193);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 164, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1190);
                                dataSuffix();
                            }
                        }
                    }
                    setState(1195);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 164, _ctx);
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
        enterRule(_localctx, 156, RULE_dataSuffix);
        int _la;
        try {
            setState(1216);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case DOT:
                    enterOuterAlt(_localctx, 1); {
                    setState(1196);
                    match(DOT);
                    setState(1197);
                    dataName();
                    setState(1210);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 167, _ctx)) {
                        case 1: {
                            setState(1198);
                            match(LPAREN);
                            setState(1207);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371674426310658L) != 0)
                                || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 259238453550448639L) != 0)) {
                                {
                                    setState(1199);
                                    dataExpression();
                                    setState(1204);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    while (_la == COMMA) {
                                        {
                                            {
                                                setState(1200);
                                                match(COMMA);
                                                setState(1201);
                                                dataExpression();
                                            }
                                        }
                                        setState(1206);
                                        _errHandler.sync(this);
                                        _la = _input.LA(1);
                                    }
                                }
                            }

                            setState(1209);
                            match(RPAREN);
                        }
                            break;
                    }
                }
                    break;
                case LBRACKET:
                    enterOuterAlt(_localctx, 2); {
                    setState(1212);
                    match(LBRACKET);
                    setState(1213);
                    dataExpression();
                    setState(1214);
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
        enterRule(_localctx, 158, RULE_dataPrimary);
        int _la;
        try {
            int _alt;
            setState(1262);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 175, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(1218);
                    match(NUMBER);
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(1219);
                    match(STRING);
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(1220);
                    match(VARIABLE);
                }
                    break;
                case 4:
                    enterOuterAlt(_localctx, 4); {
                    setState(1221);
                    match(IMPORT);
                }
                    break;
                case 5:
                    enterOuterAlt(_localctx, 5); {
                    setState(1222);
                    match(REVISION);
                }
                    break;
                case 6:
                    enterOuterAlt(_localctx, 6); {
                    setState(1223);
                    match(DIGEST);
                }
                    break;
                case 7:
                    enterOuterAlt(_localctx, 7); {
                    setState(1224);
                    match(TRUE);
                }
                    break;
                case 8:
                    enterOuterAlt(_localctx, 8); {
                    setState(1225);
                    match(FALSE);
                }
                    break;
                case 9:
                    enterOuterAlt(_localctx, 9); {
                    setState(1226);
                    match(LPAREN);
                    setState(1227);
                    dataExpression();
                    setState(1228);
                    match(RPAREN);
                }
                    break;
                case 10:
                    enterOuterAlt(_localctx, 10); {
                    setState(1230);
                    functionCall();
                }
                    break;
                case 11:
                    enterOuterAlt(_localctx, 11); {
                    setState(1231);
                    match(IDENTIFIER);
                }
                    break;
                case 12:
                    enterOuterAlt(_localctx, 12); {
                    setState(1232);
                    match(LBRACKET);
                    setState(1244);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371674426310658L) != 0)
                        || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 259238453550448639L) != 0)) {
                        {
                            setState(1233);
                            dataExpression();
                            setState(1238);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 169, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(1234);
                                            match(COMMA);
                                            setState(1235);
                                            dataExpression();
                                        }
                                    }
                                }
                                setState(1240);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 169, _ctx);
                            }
                            setState(1242);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(1241);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(1246);
                    match(RBRACKET);
                }
                    break;
                case 13:
                    enterOuterAlt(_localctx, 13); {
                    setState(1247);
                    match(LBRACE);
                    setState(1259);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371743514886146L) != 0)
                        || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 72339069014573055L) != 0)) {
                        {
                            setState(1248);
                            dataField();
                            setState(1253);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 172, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(1249);
                                            match(COMMA);
                                            setState(1250);
                                            dataField();
                                        }
                                    }
                                }
                                setState(1255);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 172, _ctx);
                            }
                            setState(1257);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(1256);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(1261);
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
        enterRule(_localctx, 160, RULE_dataField);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1264);
                dataName();
                setState(1265);
                match(COLON);
                setState(1266);
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

        public TerminalNode TRANSFEREXACT() {
            return getToken(SFMLParser.TRANSFEREXACT, 0);
        }

        public TerminalNode TRANSFERFEXACT() {
            return getToken(SFMLParser.TRANSFERFEXACT, 0);
        }

        public TerminalNode TRANSFERFORCE() {
            return getToken(SFMLParser.TRANSFERFORCE, 0);
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
        enterRule(_localctx, 162, RULE_dataName);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1268);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & -6719371743514886146L) != 0)
                    || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 72339069014573055L) != 0))) {
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
        enterRule(_localctx, 164, RULE_string);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1270);
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
        enterRule(_localctx, 166, RULE_number);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1272);
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
            case 18:
                return breakStatement_sempred((BreakStatementContext) _localctx, predIndex);
            case 35:
                return withClause_sempred((WithClauseContext) _localctx, predIndex);
            case 47:
                return boolexpr_sempred((BoolexprContext) _localctx, predIndex);
            case 75:
                return multiplyOperator_sempred((MultiplyOperatorContext) _localctx, predIndex);
        }
        return true;
    }

    private boolean breakStatement_sempred(BreakStatementContext _localctx, int predIndex) {
        switch (predIndex) {
            case 0:
                return "break".equalsIgnoreCase(
                    _input.LT(1)
                        .getText());
        }
        return true;
    }

    private boolean withClause_sempred(WithClauseContext _localctx, int predIndex) {
        switch (predIndex) {
            case 1:
                return precpred(_ctx, 3);
            case 2:
                return precpred(_ctx, 2);
        }
        return true;
    }

    private boolean boolexpr_sempred(BoolexprContext _localctx, int predIndex) {
        switch (predIndex) {
            case 3:
                return precpred(_ctx, 5);
            case 4:
                return precpred(_ctx, 4);
        }
        return true;
    }

    private boolean multiplyOperator_sempred(MultiplyOperatorContext _localctx, int predIndex) {
        switch (predIndex) {
            case 5:
                return _input.LT(1)
                    .getText()
                    .equals("*");
        }
        return true;
    }

    public static final String _serializedATN = "\u0004\u0001}\u04fb\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"
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
        + "P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0001\u0000\u0003\u0000"
        + "\u00aa\b\u0000\u0001\u0000\u0005\u0000\u00ad\b\u0000\n\u0000\f\u0000\u00b0"
        + "\t\u0000\u0001\u0000\u0001\u0000\u0003\u0000\u00b4\b\u0000\u0001\u0000"
        + "\u0003\u0000\u00b7\b\u0000\u0005\u0000\u00b9\b\u0000\n\u0000\f\u0000\u00bc"
        + "\t\u0000\u0001\u0000\u0001\u0000\u0005\u0000\u00c0\b\u0000\n\u0000\f\u0000"
        + "\u00c3\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"
        + "\u0003\u0001\u00d0\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"
        + "\u0001\u0002\u0001\u0002\u0005\u0002\u00d8\b\u0002\n\u0002\f\u0002\u00db"
        + "\t\u0002\u0003\u0002\u00dd\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002"
        + "\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"
        + "\u0001\u0002\u0001\u0002\u0003\u0002\u00ea\b\u0002\u0001\u0002\u0001\u0002"
        + "\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002\u00f2\b\u0002"
        + "\n\u0002\f\u0002\u00f5\t\u0002\u0003\u0002\u00f7\b\u0002\u0001\u0002\u0001"
        + "\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"
        + "\u0002\u0001\u0002\u0003\u0002\u0102\b\u0002\u0003\u0002\u0104\b\u0002"
        + "\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u0109\b\u0003\n\u0003"
        + "\f\u0003\u010c\t\u0003\u0001\u0004\u0003\u0004\u010f\b\u0004\u0001\u0004"
        + "\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"
        + "\u0003\u0004\u0118\b\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"
        + "\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006\u0121\b\u0006\n\u0006"
        + "\f\u0006\u0124\t\u0006\u0003\u0006\u0126\b\u0006\u0001\u0006\u0003\u0006"
        + "\u0129\b\u0006\u0001\u0006\u0005\u0006\u012c\b\u0006\n\u0006\f\u0006\u012f"
        + "\t\u0006\u0001\u0007\u0001\u0007\u0003\u0007\u0133\b\u0007\u0001\u0007"
        + "\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001"
        + "\t\u0001\t\u0005\t\u0140\b\t\n\t\f\t\u0143\t\t\u0003\t\u0145\b\t\u0001"
        + "\t\u0001\t\u0001\n\u0001\n\u0001\n\u0003\n\u014c\b\n\u0001\u000b\u0001"
        + "\u000b\u0003\u000b\u0150\b\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001"
        + "\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0161\b\u000e"
        + "\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e"
        + "\u016e\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"
        + "\u0001\u000e\u0001\u000e\u0003\u000e\u017d\b\u000e\u0003\u000e\u017f\b"
        + "\u000e\u0001\u000f\u0003\u000f\u0182\b\u000f\u0001\u000f\u0003\u000f\u0185"
        + "\b\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"
        + "\u000f\u0003\u000f\u018d\b\u000f\u0003\u000f\u018f\b\u000f\u0001\u000f"
        + "\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"
        + "\u0001\u000f\u0003\u000f\u0199\b\u000f\u0003\u000f\u019b\b\u000f\u0001"
        + "\u000f\u0003\u000f\u019e\b\u000f\u0001\u0010\u0005\u0010\u01a1\b\u0010"
        + "\n\u0010\f\u0010\u01a4\t\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001"
        + "\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"
        + "\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"
        + "\u0011\u0003\u0011\u01b6\b\u0011\u0001\u0011\u0003\u0011\u01b9\b\u0011"
        + "\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0003\u0013"
        + "\u01c0\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u01c5\b"
        + "\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u01c9\b\u0013\u0001\u0013\u0001"
        + "\u0013\u0001\u0013\u0003\u0013\u01ce\b\u0013\u0001\u0013\u0001\u0013\u0001"
        + "\u0013\u0003\u0013\u01d3\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003"
        + "\u0013\u01d8\b\u0013\u0003\u0013\u01da\b\u0013\u0001\u0014\u0001\u0014"
        + "\u0003\u0014\u01de\b\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u01e2\b"
        + "\u0014\n\u0014\f\u0014\u01e5\t\u0014\u0001\u0014\u0003\u0014\u01e8\b\u0014"
        + "\u0001\u0015\u0001\u0015\u0003\u0015\u01ec\b\u0015\u0001\u0015\u0003\u0015"
        + "\u01ef\b\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u01f3\b\u0015\u0001"
        + "\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u01f8\b\u0015\u0001\u0015\u0001"
        + "\u0015\u0001\u0015\u0003\u0015\u01fd\b\u0015\u0001\u0015\u0003\u0015\u0200"
        + "\b\u0015\u0003\u0015\u0202\b\u0015\u0001\u0016\u0001\u0016\u0003\u0016"
        + "\u0206\b\u0016\u0001\u0016\u0003\u0016\u0209\b\u0016\u0001\u0016\u0001"
        + "\u0016\u0003\u0016\u020d\b\u0016\u0001\u0016\u0003\u0016\u0210\b\u0016"
        + "\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0215\b\u0016\u0001\u0016"
        + "\u0003\u0016\u0218\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016"
        + "\u021d\b\u0016\u0001\u0016\u0003\u0016\u0220\b\u0016\u0003\u0016\u0222"
        + "\b\u0016\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001"
        + "\u0019\u0001\u0019\u0005\u0019\u022b\b\u0019\n\u0019\f\u0019\u022e\t\u0019"
        + "\u0001\u0019\u0003\u0019\u0231\b\u0019\u0001\u001a\u0003\u001a\u0234\b"
        + "\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u0238\b\u001a\u0001\u001a\u0001"
        + "\u001a\u0003\u001a\u023c\b\u001a\u0001\u001a\u0003\u001a\u023f\b\u001a"
        + "\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b"
        + "\u0246\b\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"
        + "\u0003\u001c\u024d\b\u001c\u0001\u001c\u0003\u001c\u0250\b\u001c\u0001"
        + "\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0003"
        + "\u001d\u0258\b\u001d\u0001\u001d\u0003\u001d\u025b\b\u001d\u0001\u001e"
        + "\u0001\u001e\u0001\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f"
        + "\u0263\b\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u0267\b\u001f\u0001"
        + "\u001f\u0001\u001f\u0003\u001f\u026b\b\u001f\u0003\u001f\u026d\b\u001f"
        + "\u0003\u001f\u026f\b\u001f\u0003\u001f\u0271\b\u001f\u0001\u001f\u0003"
        + "\u001f\u0274\b\u001f\u0001 \u0001 \u0001 \u0005 \u0279\b \n \f \u027c"
        + "\t \u0001 \u0003 \u027f\b \u0001!\u0001!\u0001!\u0005!\u0284\b!\n!\f!"
        + "\u0287\t!\u0001!\u0003!\u028a\b!\u0001\"\u0001\"\u0001\"\u0001\"\u0003"
        + "\"\u0290\b\"\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"
        + "#\u0003#\u029b\b#\u0001#\u0003#\u029e\b#\u0001#\u0003#\u02a1\b#\u0001"
        + "#\u0001#\u0001#\u0001#\u0001#\u0001#\u0005#\u02a9\b#\n#\f#\u02ac\t#\u0001"
        + "$\u0001$\u0001$\u0001$\u0001$\u0005$\u02b3\b$\n$\f$\u02b6\t$\u0001$\u0001"
        + "$\u0001$\u0005$\u02bb\b$\n$\f$\u02be\t$\u0003$\u02c0\b$\u0001%\u0001%"
        + "\u0001%\u0001%\u0001%\u0005%\u02c7\b%\n%\f%\u02ca\t%\u0001%\u0001%\u0003"
        + "%\u02ce\b%\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0001(\u0001(\u0001("
        + "\u0005(\u02d8\b(\n(\f(\u02db\t(\u0001)\u0001)\u0001)\u0003)\u02e0\b)\u0001"
        + "*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0005"
        + "*\u02ec\b*\n*\f*\u02ef\t*\u0001*\u0001*\u0003*\u02f3\b*\u0001*\u0001*"
        + "\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001"
        + "*\u0001*\u0001*\u0005*\u0303\b*\n*\f*\u0306\t*\u0001*\u0001*\u0001*\u0001"
        + "*\u0001*\u0003*\u030d\b*\u0003*\u030f\b*\u0001+\u0001+\u0001+\u0001+\u0001"
        + "+\u0001+\u0001+\u0001+\u0003+\u0319\b+\u0001+\u0001+\u0001+\u0001+\u0001"
        + "+\u0001+\u0001+\u0001+\u0003+\u0323\b+\u0001+\u0001+\u0001+\u0001+\u0001"
        + "+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0003+\u0331\b+\u0001"
        + "+\u0001+\u0001+\u0003+\u0336\b+\u0001+\u0001+\u0003+\u033a\b+\u0001+\u0001"
        + "+\u0003+\u033e\b+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001"
        + "+\u0001+\u0003+\u0349\b+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001"
        + "+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0003"
        + "+\u035b\b+\u0003+\u035d\b+\u0001,\u0001,\u0001,\u0003,\u0362\b,\u0001"
        + "-\u0001-\u0003-\u0366\b-\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001"
        + ".\u0001.\u0001.\u0001.\u0003.\u0372\b.\u0001/\u0001/\u0001/\u0001/\u0001"
        + "/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0003"
        + "/\u0382\b/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0003"
        + "/\u038c\b/\u0001/\u0003/\u038f\b/\u0001/\u0003/\u0392\b/\u0001/\u0001"
        + "/\u0003/\u0396\b/\u0001/\u0001/\u0003/\u039a\b/\u0001/\u0001/\u0001/\u0001"
        + "/\u0001/\u0001/\u0003/\u03a2\b/\u0003/\u03a4\b/\u0001/\u0003/\u03a7\b"
        + "/\u0001/\u0001/\u0001/\u0003/\u03ac\b/\u0001/\u0001/\u0001/\u0001/\u0001"
        + "/\u0001/\u0003/\u03b4\b/\u0003/\u03b6\b/\u0003/\u03b8\b/\u0001/\u0001"
        + "/\u0001/\u0001/\u0001/\u0001/\u0005/\u03c0\b/\n/\f/\u03c3\t/\u00010\u0001"
        + "0\u00011\u00011\u00012\u00012\u00012\u00052\u03cc\b2\n2\f2\u03cf\t2\u0001"
        + "2\u00032\u03d2\b2\u00012\u00032\u03d5\b2\u00012\u00032\u03d8\b2\u0001"
        + "3\u00013\u00013\u00013\u00013\u00014\u00014\u00054\u03e1\b4\n4\f4\u03e4"
        + "\t4\u00014\u00014\u00014\u00054\u03e9\b4\n4\f4\u03ec\t4\u00014\u00014"
        + "\u00054\u03f0\b4\n4\f4\u03f3\t4\u00034\u03f5\b4\u00015\u00015\u00015\u0001"
        + "5\u00016\u00016\u00017\u00017\u00017\u00017\u00017\u00018\u00018\u0001"
        + "8\u00018\u00018\u00019\u00039\u0408\b9\u00019\u00019\u00019\u00019\u0001"
        + ":\u0001:\u0001:\u0001:\u0001:\u0001:\u0001:\u0001:\u0003:\u0416\b:\u0001"
        + ";\u0001;\u0001;\u0001<\u0001<\u0001<\u0001<\u0001<\u0005<\u0420\b<\n<"
        + "\f<\u0423\t<\u0001<\u0001<\u0001=\u0001=\u0001=\u0001=\u0001=\u0001=\u0001"
        + "=\u0001=\u0001=\u0003=\u0430\b=\u0003=\u0432\b=\u0001=\u0001=\u0003=\u0436"
        + "\b=\u0001=\u0001=\u0003=\u043a\b=\u0001>\u0001>\u0001>\u0003>\u043f\b"
        + ">\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0005?\u0448\b?\n?"
        + "\f?\u044b\t?\u0001@\u0001@\u0001A\u0001A\u0001A\u0005A\u0452\bA\nA\fA"
        + "\u0455\tA\u0001B\u0001B\u0001B\u0005B\u045a\bB\nB\fB\u045d\tB\u0001C\u0001"
        + "C\u0001C\u0003C\u0462\bC\u0001D\u0001D\u0001D\u0001D\u0003D\u0468\bD\u0001"
        + "E\u0001E\u0001E\u0005E\u046d\bE\nE\fE\u0470\tE\u0001F\u0001F\u0001F\u0005"
        + "F\u0475\bF\nF\fF\u0478\tF\u0001G\u0001G\u0001G\u0005G\u047d\bG\nG\fG\u0480"
        + "\tG\u0001H\u0001H\u0001H\u0005H\u0485\bH\nH\fH\u0488\tH\u0001I\u0001I"
        + "\u0001I\u0005I\u048d\bI\nI\fI\u0490\tI\u0001J\u0001J\u0001J\u0001J\u0003"
        + "J\u0496\bJ\u0001J\u0005J\u0499\bJ\nJ\fJ\u049c\tJ\u0001K\u0001K\u0001K"
        + "\u0001L\u0001L\u0001L\u0003L\u04a4\bL\u0001M\u0001M\u0005M\u04a8\bM\n"
        + "M\fM\u04ab\tM\u0001N\u0001N\u0001N\u0001N\u0001N\u0001N\u0005N\u04b3\b"
        + "N\nN\fN\u04b6\tN\u0003N\u04b8\bN\u0001N\u0003N\u04bb\bN\u0001N\u0001N"
        + "\u0001N\u0001N\u0003N\u04c1\bN\u0001O\u0001O\u0001O\u0001O\u0001O\u0001"
        + "O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001"
        + "O\u0001O\u0001O\u0005O\u04d5\bO\nO\fO\u04d8\tO\u0001O\u0003O\u04db\bO"
        + "\u0003O\u04dd\bO\u0001O\u0001O\u0001O\u0001O\u0001O\u0005O\u04e4\bO\n"
        + "O\fO\u04e7\tO\u0001O\u0003O\u04ea\bO\u0003O\u04ec\bO\u0001O\u0003O\u04ef"
        + "\bO\u0001P\u0001P\u0001P\u0001P\u0001Q\u0001Q\u0001R\u0001R\u0001S\u0001"
        + "S\u0001S\u0000\u0002F^T\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"
        + "\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\"
        + "^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090"
        + "\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u0000"
        + "\u0010\u0002\u0000))mm\u0003\u0000\u0001\u0003\u0019\u0019yy\u0002\u0000"
        + "\u0013\u0014ll\u0001\u0000cf\u0001\u0000jk\u0002\u0000W\\^b\u0001\u0000"
        + "FG\u0001\u00005@\u0003\u0000,/IIpp\u0001\u0000UV\u0007\u0000\u0001\u000e"
        + "\u0010\u0018WX^aegikyy\u0001\u0000\u0006\n\u0001\u0000%\'\u0002\u0000"
        + "hhtt\u0003\u0000$$hhtt\n\u0000\u0001\u000e\u0010\u0018(57799==??APRpy"
        + "y\u057f\u0000\u00a9\u0001\u0000\u0000\u0000\u0002\u00c6\u0001\u0000\u0000"
        + "\u0000\u0004\u0103\u0001\u0000\u0000\u0000\u0006\u0105\u0001\u0000\u0000"
        + "\u0000\b\u010e\u0001\u0000\u0000\u0000\n\u0119\u0001\u0000\u0000\u0000"
        + "\f\u0128\u0001\u0000\u0000\u0000\u000e\u0130\u0001\u0000\u0000\u0000\u0010"
        + "\u0136\u0001\u0000\u0000\u0000\u0012\u013a\u0001\u0000\u0000\u0000\u0014"
        + "\u014b\u0001\u0000\u0000\u0000\u0016\u014d\u0001\u0000\u0000\u0000\u0018"
        + "\u0151\u0001\u0000\u0000\u0000\u001a\u0153\u0001\u0000\u0000\u0000\u001c"
        + "\u017e\u0001\u0000\u0000\u0000\u001e\u019d\u0001\u0000\u0000\u0000 \u01a2"
        + "\u0001\u0000\u0000\u0000\"\u01b5\u0001\u0000\u0000\u0000$\u01ba\u0001"
        + "\u0000\u0000\u0000&\u01d9\u0001\u0000\u0000\u0000(\u01db\u0001\u0000\u0000"
        + "\u0000*\u0201\u0001\u0000\u0000\u0000,\u0221\u0001\u0000\u0000\u0000."
        + "\u0223\u0001\u0000\u0000\u00000\u0225\u0001\u0000\u0000\u00002\u0227\u0001"
        + "\u0000\u0000\u00004\u023e\u0001\u0000\u0000\u00006\u0245\u0001\u0000\u0000"
        + "\u00008\u024c\u0001\u0000\u0000\u0000:\u0251\u0001\u0000\u0000\u0000<"
        + "\u025c\u0001\u0000\u0000\u0000>\u0273\u0001\u0000\u0000\u0000@\u0275\u0001"
        + "\u0000\u0000\u0000B\u0280\u0001\u0000\u0000\u0000D\u028f\u0001\u0000\u0000"
        + "\u0000F\u02a0\u0001\u0000\u0000\u0000H\u02bf\u0001\u0000\u0000\u0000J"
        + "\u02cd\u0001\u0000\u0000\u0000L\u02cf\u0001\u0000\u0000\u0000N\u02d1\u0001"
        + "\u0000\u0000\u0000P\u02d4\u0001\u0000\u0000\u0000R\u02dc\u0001\u0000\u0000"
        + "\u0000T\u030e\u0001\u0000\u0000\u0000V\u035c\u0001\u0000\u0000\u0000X"
        + "\u0361\u0001\u0000\u0000\u0000Z\u0365\u0001\u0000\u0000\u0000\\\u0367"
        + "\u0001\u0000\u0000\u0000^\u03b7\u0001\u0000\u0000\u0000`\u03c4\u0001\u0000"
        + "\u0000\u0000b\u03c6\u0001\u0000\u0000\u0000d\u03c8\u0001\u0000\u0000\u0000"
        + "f\u03d9\u0001\u0000\u0000\u0000h\u03f4\u0001\u0000\u0000\u0000j\u03f6"
        + "\u0001\u0000\u0000\u0000l\u03fa\u0001\u0000\u0000\u0000n\u03fc\u0001\u0000"
        + "\u0000\u0000p\u0401\u0001\u0000\u0000\u0000r\u0407\u0001\u0000\u0000\u0000"
        + "t\u040d\u0001\u0000\u0000\u0000v\u0417\u0001\u0000\u0000\u0000x\u041a"
        + "\u0001\u0000\u0000\u0000z\u0426\u0001\u0000\u0000\u0000|\u043b\u0001\u0000"
        + "\u0000\u0000~\u0440\u0001\u0000\u0000\u0000\u0080\u044c\u0001\u0000\u0000"
        + "\u0000\u0082\u044e\u0001\u0000\u0000\u0000\u0084\u0456\u0001\u0000\u0000"
        + "\u0000\u0086\u0461\u0001\u0000\u0000\u0000\u0088\u0463\u0001\u0000\u0000"
        + "\u0000\u008a\u0469\u0001\u0000\u0000\u0000\u008c\u0471\u0001\u0000\u0000"
        + "\u0000\u008e\u0479\u0001\u0000\u0000\u0000\u0090\u0481\u0001\u0000\u0000"
        + "\u0000\u0092\u0489\u0001\u0000\u0000\u0000\u0094\u0491\u0001\u0000\u0000"
        + "\u0000\u0096\u049d\u0001\u0000\u0000\u0000\u0098\u04a3\u0001\u0000\u0000"
        + "\u0000\u009a\u04a5\u0001\u0000\u0000\u0000\u009c\u04c0\u0001\u0000\u0000"
        + "\u0000\u009e\u04ee\u0001\u0000\u0000\u0000\u00a0\u04f0\u0001\u0000\u0000"
        + "\u0000\u00a2\u04f4\u0001\u0000\u0000\u0000\u00a4\u04f6\u0001\u0000\u0000"
        + "\u0000\u00a6\u04f8\u0001\u0000\u0000\u0000\u00a8\u00aa\u0003\u001a\r\u0000"
        + "\u00a9\u00a8\u0001\u0000\u0000\u0000\u00a9\u00aa\u0001\u0000\u0000\u0000"
        + "\u00aa\u00ae\u0001\u0000\u0000\u0000\u00ab\u00ad\u0003\u0002\u0001\u0000"
        + "\u00ac\u00ab\u0001\u0000\u0000\u0000\u00ad\u00b0\u0001\u0000\u0000\u0000"
        + "\u00ae\u00ac\u0001\u0000\u0000\u0000\u00ae\u00af\u0001\u0000\u0000\u0000"
        + "\u00af\u00ba\u0001\u0000\u0000\u0000\u00b0\u00ae\u0001\u0000\u0000\u0000"
        + "\u00b1\u00b4\u0003n7\u0000\u00b2\u00b4\u0003p8\u0000\u00b3\u00b1\u0001"
        + "\u0000\u0000\u0000\u00b3\u00b2\u0001\u0000\u0000\u0000\u00b4\u00b6\u0001"
        + "\u0000\u0000\u0000\u00b5\u00b7\u0005\u001f\u0000\u0000\u00b6\u00b5\u0001"
        + "\u0000\u0000\u0000\u00b6\u00b7\u0001\u0000\u0000\u0000\u00b7\u00b9\u0001"
        + "\u0000\u0000\u0000\u00b8\u00b3\u0001\u0000\u0000\u0000\u00b9\u00bc\u0001"
        + "\u0000\u0000\u0000\u00ba\u00b8\u0001\u0000\u0000\u0000\u00ba\u00bb\u0001"
        + "\u0000\u0000\u0000\u00bb\u00c1\u0001\u0000\u0000\u0000\u00bc\u00ba\u0001"
        + "\u0000\u0000\u0000\u00bd\u00c0\u0003\u0004\u0002\u0000\u00be\u00c0\u0003"
        + "\u001c\u000e\u0000\u00bf\u00bd\u0001\u0000\u0000\u0000\u00bf\u00be\u0001"
        + "\u0000\u0000\u0000\u00c0\u00c3\u0001\u0000\u0000\u0000\u00c1\u00bf\u0001"
        + "\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000\u0000\u00c2\u00c4\u0001"
        + "\u0000\u0000\u0000\u00c3\u00c1\u0001\u0000\u0000\u0000\u00c4\u00c5\u0005"
        + "\u0000\u0000\u0001\u00c5\u0001\u0001\u0000\u0000\u0000\u00c6\u00c7\u0005"
        + "\u0001\u0000\u0000\u00c7\u00c8\u0003l6\u0000\u00c8\u00c9\u0005A\u0000"
        + "\u0000\u00c9\u00ca\u0003\u00a4R\u0000\u00ca\u00cb\u0005\u0002\u0000\u0000"
        + "\u00cb\u00cc\u0005x\u0000\u0000\u00cc\u00cd\u0005\u0003\u0000\u0000\u00cd"
        + "\u00cf\u0003\u00a4R\u0000\u00ce\u00d0\u0005\u001f\u0000\u0000\u00cf\u00ce"
        + "\u0001\u0000\u0000\u0000\u00cf\u00d0\u0001\u0000\u0000\u0000\u00d0\u0003"
        + "\u0001\u0000\u0000\u0000\u00d1\u00d2\u0005\f\u0000\u0000\u00d2\u00d3\u0003"
        + "\u0006\u0003\u0000\u00d3\u00dc\u0005u\u0000\u0000\u00d4\u00d9\u0003\b"
        + "\u0004\u0000\u00d5\u00d6\u0005q\u0000\u0000\u00d6\u00d8\u0003\b\u0004"
        + "\u0000\u00d7\u00d5\u0001\u0000\u0000\u0000\u00d8\u00db\u0001\u0000\u0000"
        + "\u0000\u00d9\u00d7\u0001\u0000\u0000\u0000\u00d9\u00da\u0001\u0000\u0000"
        + "\u0000\u00da\u00dd\u0001\u0000\u0000\u0000\u00db\u00d9\u0001\u0000\u0000"
        + "\u0000\u00dc\u00d4\u0001\u0000\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000"
        + "\u0000\u00dd\u00de\u0001\u0000\u0000\u0000\u00de\u00df\u0005v\u0000\u0000"
        + "\u00df\u00e0\u0005\r\u0000\u0000\u00e0\u00e9\u0003\f\u0006\u0000\u00e1"
        + "\u00e2\u0005m\u0000\u0000\u00e2\u00e3\u0003 \u0010\u0000\u00e3\u00e4\u0005"
        + "n\u0000\u0000\u00e4\u00ea\u0001\u0000\u0000\u0000\u00e5\u00e6\u0005\u001c"
        + "\u0000\u0000\u00e6\u00e7\u0003 \u0010\u0000\u00e7\u00e8\u0005\u001d\u0000"
        + "\u0000\u00e8\u00ea\u0001\u0000\u0000\u0000\u00e9\u00e1\u0001\u0000\u0000"
        + "\u0000\u00e9\u00e5\u0001\u0000\u0000\u0000\u00ea\u0104\u0001\u0000\u0000"
        + "\u0000\u00eb\u00ec\u0003\f\u0006\u0000\u00ec\u00ed\u0003\u0006\u0003\u0000"
        + "\u00ed\u00f6\u0005u\u0000\u0000\u00ee\u00f3\u0003\b\u0004\u0000\u00ef"
        + "\u00f0\u0005q\u0000\u0000\u00f0\u00f2\u0003\b\u0004\u0000\u00f1\u00ef"
        + "\u0001\u0000\u0000\u0000\u00f2\u00f5\u0001\u0000\u0000\u0000\u00f3\u00f1"
        + "\u0001\u0000\u0000\u0000\u00f3\u00f4\u0001\u0000\u0000\u0000\u00f4\u00f7"
        + "\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001\u0000\u0000\u0000\u00f6\u00ee"
        + "\u0001\u0000\u0000\u0000\u00f6\u00f7\u0001\u0000\u0000\u0000\u00f7\u00f8"
        + "\u0001\u0000\u0000\u0000\u00f8\u0101\u0005v\u0000\u0000\u00f9\u00fa\u0007"
        + "\u0000\u0000\u0000\u00fa\u00fb\u0003 \u0010\u0000\u00fb\u00fc\u0005n\u0000"
        + "\u0000\u00fc\u0102\u0001\u0000\u0000\u0000\u00fd\u00fe\u0005\u001c\u0000"
        + "\u0000\u00fe\u00ff\u0003 \u0010\u0000\u00ff\u0100\u0005\u001d\u0000\u0000"
        + "\u0100\u0102\u0001\u0000\u0000\u0000\u0101\u00f9\u0001\u0000\u0000\u0000"
        + "\u0101\u00fd\u0001\u0000\u0000\u0000\u0102\u0104\u0001\u0000\u0000\u0000"
        + "\u0103\u00d1\u0001\u0000\u0000\u0000\u0103\u00eb\u0001\u0000\u0000\u0000"
        + "\u0104\u0005\u0001\u0000\u0000\u0000\u0105\u010a\u0003\u00a2Q\u0000\u0106"
        + "\u0107\u0005\u001e\u0000\u0000\u0107\u0109\u0003\u00a2Q\u0000\u0108\u0106"
        + "\u0001\u0000\u0000\u0000\u0109\u010c\u0001\u0000\u0000\u0000\u010a\u0108"
        + "\u0001\u0000\u0000\u0000\u010a\u010b\u0001\u0000\u0000\u0000\u010b\u0007"
        + "\u0001\u0000\u0000\u0000\u010c\u010a\u0001\u0000\u0000\u0000\u010d\u010f"
        + "\u0005\u000f\u0000\u0000\u010e\u010d\u0001\u0000\u0000\u0000\u010e\u010f"
        + "\u0001\u0000\u0000\u0000\u010f\u0117\u0001\u0000\u0000\u0000\u0110\u0111"
        + "\u0003\n\u0005\u0000\u0111\u0112\u0005r\u0000\u0000\u0112\u0113\u0003"
        + "\f\u0006\u0000\u0113\u0118\u0001\u0000\u0000\u0000\u0114\u0115\u0003\f"
        + "\u0006\u0000\u0115\u0116\u0003\n\u0005\u0000\u0116\u0118\u0001\u0000\u0000"
        + "\u0000\u0117\u0110\u0001\u0000\u0000\u0000\u0117\u0114\u0001\u0000\u0000"
        + "\u0000\u0118\t\u0001\u0000\u0000\u0000\u0119\u011a\u0007\u0001\u0000\u0000"
        + "\u011a\u000b\u0001\u0000\u0000\u0000\u011b\u0129\u0003\u00a2Q\u0000\u011c"
        + "\u0125\u0005\u001c\u0000\u0000\u011d\u0122\u0003\u0010\b\u0000\u011e\u011f"
        + "\u0005q\u0000\u0000\u011f\u0121\u0003\u0010\b\u0000\u0120\u011e\u0001"
        + "\u0000\u0000\u0000\u0121\u0124\u0001\u0000\u0000\u0000\u0122\u0120\u0001"
        + "\u0000\u0000\u0000\u0122\u0123\u0001\u0000\u0000\u0000\u0123\u0126\u0001"
        + "\u0000\u0000\u0000\u0124\u0122\u0001\u0000\u0000\u0000\u0125\u011d\u0001"
        + "\u0000\u0000\u0000\u0125\u0126\u0001\u0000\u0000\u0000\u0126\u0127\u0001"
        + "\u0000\u0000\u0000\u0127\u0129\u0005\u001d\u0000\u0000\u0128\u011b\u0001"
        + "\u0000\u0000\u0000\u0128\u011c\u0001\u0000\u0000\u0000\u0129\u012d\u0001"
        + "\u0000\u0000\u0000\u012a\u012c\u0003\u000e\u0007\u0000\u012b\u012a\u0001"
        + "\u0000\u0000\u0000\u012c\u012f\u0001\u0000\u0000\u0000\u012d\u012b\u0001"
        + "\u0000\u0000\u0000\u012d\u012e\u0001\u0000\u0000\u0000\u012e\r\u0001\u0000"
        + "\u0000\u0000\u012f\u012d\u0001\u0000\u0000\u0000\u0130\u0132\u0005\u001a"
        + "\u0000\u0000\u0131\u0133\u0005x\u0000\u0000\u0132\u0131\u0001\u0000\u0000"
        + "\u0000\u0132\u0133\u0001\u0000\u0000\u0000\u0133\u0134\u0001\u0000\u0000"
        + "\u0000\u0134\u0135\u0005\u001b\u0000\u0000\u0135\u000f\u0001\u0000\u0000"
        + "\u0000\u0136\u0137\u0003\u00a2Q\u0000\u0137\u0138\u0005r\u0000\u0000\u0138"
        + "\u0139\u0003\f\u0006\u0000\u0139\u0011\u0001\u0000\u0000\u0000\u013a\u013b"
        + "\u0003\u0006\u0003\u0000\u013b\u0144\u0005u\u0000\u0000\u013c\u0141\u0003"
        + "\u0014\n\u0000\u013d\u013e\u0005q\u0000\u0000\u013e\u0140\u0003\u0014"
        + "\n\u0000\u013f\u013d\u0001\u0000\u0000\u0000\u0140\u0143\u0001\u0000\u0000"
        + "\u0000\u0141\u013f\u0001\u0000\u0000\u0000\u0141\u0142\u0001\u0000\u0000"
        + "\u0000\u0142\u0145\u0001\u0000\u0000\u0000\u0143\u0141\u0001\u0000\u0000"
        + "\u0000\u0144\u013c\u0001\u0000\u0000\u0000\u0144\u0145\u0001\u0000\u0000"
        + "\u0000\u0145\u0146\u0001\u0000\u0000\u0000\u0146\u0147\u0005v\u0000\u0000"
        + "\u0147\u0013\u0001\u0000\u0000\u0000\u0148\u0149\u0005\u000f\u0000\u0000"
        + "\u0149\u014c\u0003~?\u0000\u014a\u014c\u0003\u0080@\u0000\u014b\u0148"
        + "\u0001\u0000\u0000\u0000\u014b\u014a\u0001\u0000\u0000\u0000\u014c\u0015"
        + "\u0001\u0000\u0000\u0000\u014d\u014f\u0005\u000e\u0000\u0000\u014e\u0150"
        + "\u0003\u0080@\u0000\u014f\u014e\u0001\u0000\u0000\u0000\u014f\u0150\u0001"
        + "\u0000\u0000\u0000\u0150\u0017\u0001\u0000\u0000\u0000\u0151\u0152\u0003"
        + "\u0012\t\u0000\u0152\u0019\u0001\u0000\u0000\u0000\u0153\u0154\u0005o"
        + "\u0000\u0000\u0154\u0155\u0003\u00a4R\u0000\u0155\u001b\u0001\u0000\u0000"
        + "\u0000\u0156\u0157\u0005p\u0000\u0000\u0157\u0160\u0003\u001e\u000f\u0000"
        + "\u0158\u0159\u0005m\u0000\u0000\u0159\u015a\u0003 \u0010\u0000\u015a\u015b"
        + "\u0005n\u0000\u0000\u015b\u0161\u0001\u0000\u0000\u0000\u015c\u015d\u0005"
        + "\u001c\u0000\u0000\u015d\u015e\u0003 \u0010\u0000\u015e\u015f\u0005\u001d"
        + "\u0000\u0000\u015f\u0161\u0001\u0000\u0000\u0000\u0160\u0158\u0001\u0000"
        + "\u0000\u0000\u0160\u015c\u0001\u0000\u0000\u0000\u0161\u017f\u0001\u0000"
        + "\u0000\u0000\u0162\u0163\u0005p\u0000\u0000\u0163\u0164\u0005i\u0000\u0000"
        + "\u0164\u016d\u0005l\u0000\u0000\u0165\u0166\u0005m\u0000\u0000\u0166\u0167"
        + "\u0003 \u0010\u0000\u0167\u0168\u0005n\u0000\u0000\u0168\u016e\u0001\u0000"
        + "\u0000\u0000\u0169\u016a\u0005\u001c\u0000\u0000\u016a\u016b\u0003 \u0010"
        + "\u0000\u016b\u016c\u0005\u001d\u0000\u0000\u016c\u016e\u0001\u0000\u0000"
        + "\u0000\u016d\u0165\u0001\u0000\u0000\u0000\u016d\u0169\u0001\u0000\u0000"
        + "\u0000\u016e\u017f\u0001\u0000\u0000\u0000\u016f\u0170\u0005p\u0000\u0000"
        + "\u0170\u0171\u0005i\u0000\u0000\u0171\u0172\u0007\u0002\u0000\u0000\u0172"
        + "\u0173\u0005A\u0000\u0000\u0173\u017c\u0003d2\u0000\u0174\u0175\u0005"
        + "m\u0000\u0000\u0175\u0176\u0003 \u0010\u0000\u0176\u0177\u0005n\u0000"
        + "\u0000\u0177\u017d\u0001\u0000\u0000\u0000\u0178\u0179\u0005\u001c\u0000"
        + "\u0000\u0179\u017a\u0003 \u0010\u0000\u017a\u017b\u0005\u001d\u0000\u0000"
        + "\u017b\u017d\u0001\u0000\u0000\u0000\u017c\u0174\u0001\u0000\u0000\u0000"
        + "\u017c\u0178\u0001\u0000\u0000\u0000\u017d\u017f\u0001\u0000\u0000\u0000"
        + "\u017e\u0156\u0001\u0000\u0000\u0000\u017e\u0162\u0001\u0000\u0000\u0000"
        + "\u017e\u016f\u0001\u0000\u0000\u0000\u017f\u001d\u0001\u0000\u0000\u0000"
        + "\u0180\u0182\u0005x\u0000\u0000\u0181\u0180\u0001\u0000\u0000\u0000\u0181"
        + "\u0182\u0001\u0000\u0000\u0000\u0182\u0184\u0001\u0000\u0000\u0000\u0183"
        + "\u0185\u0005g\u0000\u0000\u0184\u0183\u0001\u0000\u0000\u0000\u0184\u0185"
        + "\u0001\u0000\u0000\u0000\u0185\u018e\u0001\u0000\u0000\u0000\u0186\u018c"
        + "\u0005h\u0000\u0000\u0187\u018d\u0005x\u0000\u0000\u0188\u0189\u0005u"
        + "\u0000\u0000\u0189\u018a\u0003\u0080@\u0000\u018a\u018b\u0005v\u0000\u0000"
        + "\u018b\u018d\u0001\u0000\u0000\u0000\u018c\u0187\u0001\u0000\u0000\u0000"
        + "\u018c\u0188\u0001\u0000\u0000\u0000\u018d\u018f\u0001\u0000\u0000\u0000"
        + "\u018e\u0186\u0001\u0000\u0000\u0000\u018e\u018f\u0001\u0000\u0000\u0000"
        + "\u018f\u0190\u0001\u0000\u0000\u0000\u0190\u019e\u0007\u0003\u0000\u0000"
        + "\u0191\u019a\u0005w\u0000\u0000\u0192\u0198\u0005h\u0000\u0000\u0193\u0199"
        + "\u0005x\u0000\u0000\u0194\u0195\u0005u\u0000\u0000\u0195\u0196\u0003\u0080"
        + "@\u0000\u0196\u0197\u0005v\u0000\u0000\u0197\u0199\u0001\u0000\u0000\u0000"
        + "\u0198\u0193\u0001\u0000\u0000\u0000\u0198\u0194\u0001\u0000\u0000\u0000"
        + "\u0199\u019b\u0001\u0000\u0000\u0000\u019a\u0192\u0001\u0000\u0000\u0000"
        + "\u019a\u019b\u0001\u0000\u0000\u0000\u019b\u019c\u0001\u0000\u0000\u0000"
        + "\u019c\u019e\u0007\u0003\u0000\u0000\u019d\u0181\u0001\u0000\u0000\u0000"
        + "\u019d\u0191\u0001\u0000\u0000\u0000\u019e\u001f\u0001\u0000\u0000\u0000"
        + "\u019f\u01a1\u0003\"\u0011\u0000\u01a0\u019f\u0001\u0000\u0000\u0000\u01a1"
        + "\u01a4\u0001\u0000\u0000\u0000\u01a2\u01a0\u0001\u0000\u0000\u0000\u01a2"
        + "\u01a3\u0001\u0000\u0000\u0000\u01a3!\u0001\u0000\u0000\u0000\u01a4\u01a2"
        + "\u0001\u0000\u0000\u0000\u01a5\u01b6\u0003t:\u0000\u01a6\u01b6\u0003$"
        + "\u0012\u0000\u01a7\u01b6\u0003v;\u0000\u01a8\u01b6\u0003p8\u0000\u01a9"
        + "\u01b6\u0003r9\u0000\u01aa\u01b6\u0003x<\u0000\u01ab\u01b6\u0003z=\u0000"
        + "\u01ac\u01b6\u0003\u0016\u000b\u0000\u01ad\u01b6\u0003*\u0015\u0000\u01ae"
        + "\u01b6\u0003&\u0013\u0000\u01af\u01b6\u0003,\u0016\u0000\u01b0\u01b6\u0003"
        + "T*\u0000\u01b1\u01b6\u0003V+\u0000\u01b2\u01b6\u0003\\.\u0000\u01b3\u01b6"
        + "\u0003(\u0014\u0000\u01b4\u01b6\u0003\u0018\f\u0000\u01b5\u01a5\u0001"
        + "\u0000\u0000\u0000\u01b5\u01a6\u0001\u0000\u0000\u0000\u01b5\u01a7\u0001"
        + "\u0000\u0000\u0000\u01b5\u01a8\u0001\u0000\u0000\u0000\u01b5\u01a9\u0001"
        + "\u0000\u0000\u0000\u01b5\u01aa\u0001\u0000\u0000\u0000\u01b5\u01ab\u0001"
        + "\u0000\u0000\u0000\u01b5\u01ac\u0001\u0000\u0000\u0000\u01b5\u01ad\u0001"
        + "\u0000\u0000\u0000\u01b5\u01ae\u0001\u0000\u0000\u0000\u01b5\u01af\u0001"
        + "\u0000\u0000\u0000\u01b5\u01b0\u0001\u0000\u0000\u0000\u01b5\u01b1\u0001"
        + "\u0000\u0000\u0000\u01b5\u01b2\u0001\u0000\u0000\u0000\u01b5\u01b3\u0001"
        + "\u0000\u0000\u0000\u01b5\u01b4\u0001\u0000\u0000\u0000\u01b6\u01b8\u0001"
        + "\u0000\u0000\u0000\u01b7\u01b9\u0005\u001f\u0000\u0000\u01b8\u01b7\u0001"
        + "\u0000\u0000\u0000\u01b8\u01b9\u0001\u0000\u0000\u0000\u01b9#\u0001\u0000"
        + "\u0000\u0000\u01ba\u01bb\u0004\u0012\u0000\u0000\u01bb\u01bc\u0005y\u0000"
        + "\u0000\u01bc%\u0001\u0000\u0000\u0000\u01bd\u01bf\u0005D\u0000\u0000\u01be"
        + "\u01c0\u00038\u001c\u0000\u01bf\u01be\u0001\u0000\u0000\u0000\u01bf\u01c0"
        + "\u0001\u0000\u0000\u0000\u01c0\u01c1\u0001\u0000\u0000\u0000\u01c1\u01c2"
        + "\u0005i\u0000\u0000\u01c2\u01c4\u0007\u0004\u0000\u0000\u01c3\u01c5\u0003"
        + "8\u001c\u0000\u01c4\u01c3\u0001\u0000\u0000\u0000\u01c4\u01c5\u0001\u0000"
        + "\u0000\u0000\u01c5\u01c6\u0001\u0000\u0000\u0000\u01c6\u01c8\u0005B\u0000"
        + "\u0000\u01c7\u01c9\u0005I\u0000\u0000\u01c8\u01c7\u0001\u0000\u0000\u0000"
        + "\u01c8\u01c9\u0001\u0000\u0000\u0000\u01c9\u01ca\u0001\u0000\u0000\u0000"
        + "\u01ca\u01da\u0003d2\u0000\u01cb\u01cd\u0005B\u0000\u0000\u01cc\u01ce"
        + "\u0005I\u0000\u0000\u01cd\u01cc\u0001\u0000\u0000\u0000\u01cd\u01ce\u0001"
        + "\u0000\u0000\u0000\u01ce\u01cf\u0001\u0000\u0000\u0000\u01cf\u01d0\u0003"
        + "d2\u0000\u01d0\u01d2\u0005D\u0000\u0000\u01d1\u01d3\u00038\u001c\u0000"
        + "\u01d2\u01d1\u0001\u0000\u0000\u0000\u01d2\u01d3\u0001\u0000\u0000\u0000"
        + "\u01d3\u01d4\u0001\u0000\u0000\u0000\u01d4\u01d5\u0005i\u0000\u0000\u01d5"
        + "\u01d7\u0007\u0004\u0000\u0000\u01d6\u01d8\u00038\u001c\u0000\u01d7\u01d6"
        + "\u0001\u0000\u0000\u0000\u01d7\u01d8\u0001\u0000\u0000\u0000\u01d8\u01da"
        + "\u0001\u0000\u0000\u0000\u01d9\u01bd\u0001\u0000\u0000\u0000\u01d9\u01cb"
        + "\u0001\u0000\u0000\u0000\u01da\'\u0001\u0000\u0000\u0000\u01db\u01dd\u0005"
        + "K\u0000\u0000\u01dc\u01de\u0003h4\u0000\u01dd\u01dc\u0001\u0000\u0000"
        + "\u0000\u01dd\u01de\u0001\u0000\u0000\u0000\u01de\u01e3\u0001\u0000\u0000"
        + "\u0000\u01df\u01e0\u0005q\u0000\u0000\u01e0\u01e2\u0003h4\u0000\u01e1"
        + "\u01df\u0001\u0000\u0000\u0000\u01e2\u01e5\u0001\u0000\u0000\u0000\u01e3"
        + "\u01e1\u0001\u0000\u0000\u0000\u01e3\u01e4\u0001\u0000\u0000\u0000\u01e4"
        + "\u01e7\u0001\u0000\u0000\u0000\u01e5\u01e3\u0001\u0000\u0000\u0000\u01e6"
        + "\u01e8\u0005q\u0000\u0000\u01e7\u01e6\u0001\u0000\u0000\u0000\u01e7\u01e8"
        + "\u0001\u0000\u0000\u0000\u01e8)\u0001\u0000\u0000\u0000\u01e9\u01eb\u0005"
        + "C\u0000\u0000\u01ea\u01ec\u0003.\u0017\u0000\u01eb\u01ea\u0001\u0000\u0000"
        + "\u0000\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec\u01ee\u0001\u0000\u0000"
        + "\u0000\u01ed\u01ef\u0003<\u001e\u0000\u01ee\u01ed\u0001\u0000\u0000\u0000"
        + "\u01ee\u01ef\u0001\u0000\u0000\u0000\u01ef\u01f0\u0001\u0000\u0000\u0000"
        + "\u01f0\u01f2\u0005A\u0000\u0000\u01f1\u01f3\u0005I\u0000\u0000\u01f2\u01f1"
        + "\u0001\u0000\u0000\u0000\u01f2\u01f3\u0001\u0000\u0000\u0000\u01f3\u01f4"
        + "\u0001\u0000\u0000\u0000\u01f4\u0202\u0003d2\u0000\u01f5\u01f7\u0005A"
        + "\u0000\u0000\u01f6\u01f8\u0005I\u0000\u0000\u01f7\u01f6\u0001\u0000\u0000"
        + "\u0000\u01f7\u01f8\u0001\u0000\u0000\u0000\u01f8\u01f9\u0001\u0000\u0000"
        + "\u0000\u01f9\u01fa\u0003d2\u0000\u01fa\u01fc\u0005C\u0000\u0000\u01fb"
        + "\u01fd\u0003.\u0017\u0000\u01fc\u01fb\u0001\u0000\u0000\u0000\u01fc\u01fd"
        + "\u0001\u0000\u0000\u0000\u01fd\u01ff\u0001\u0000\u0000\u0000\u01fe\u0200"
        + "\u0003<\u001e\u0000\u01ff\u01fe\u0001\u0000\u0000\u0000\u01ff\u0200\u0001"
        + "\u0000\u0000\u0000\u0200\u0202\u0001\u0000\u0000\u0000\u0201\u01e9\u0001"
        + "\u0000\u0000\u0000\u0201\u01f5\u0001\u0000\u0000\u0000\u0202+\u0001\u0000"
        + "\u0000\u0000\u0203\u0205\u0005D\u0000\u0000\u0204\u0206\u00030\u0018\u0000"
        + "\u0205\u0204\u0001\u0000\u0000\u0000\u0205\u0206\u0001\u0000\u0000\u0000"
        + "\u0206\u0208\u0001\u0000\u0000\u0000\u0207\u0209\u0003<\u001e\u0000\u0208"
        + "\u0207\u0001\u0000\u0000\u0000\u0208\u0209\u0001\u0000\u0000\u0000\u0209"
        + "\u020a\u0001\u0000\u0000\u0000\u020a\u020c\u0005B\u0000\u0000\u020b\u020d"
        + "\u0003j5\u0000\u020c\u020b\u0001\u0000\u0000\u0000\u020c\u020d\u0001\u0000"
        + "\u0000\u0000\u020d\u020f\u0001\u0000\u0000\u0000\u020e\u0210\u0005I\u0000"
        + "\u0000\u020f\u020e\u0001\u0000\u0000\u0000\u020f\u0210\u0001\u0000\u0000"
        + "\u0000\u0210\u0211\u0001\u0000\u0000\u0000\u0211\u0222\u0003d2\u0000\u0212"
        + "\u0214\u0005B\u0000\u0000\u0213\u0215\u0003j5\u0000\u0214\u0213\u0001"
        + "\u0000\u0000\u0000\u0214\u0215\u0001\u0000\u0000\u0000\u0215\u0217\u0001"
        + "\u0000\u0000\u0000\u0216\u0218\u0005I\u0000\u0000\u0217\u0216\u0001\u0000"
        + "\u0000\u0000\u0217\u0218\u0001\u0000\u0000\u0000\u0218\u0219\u0001\u0000"
        + "\u0000\u0000\u0219\u021a\u0003d2\u0000\u021a\u021c\u0005D\u0000\u0000"
        + "\u021b\u021d\u00030\u0018\u0000\u021c\u021b\u0001\u0000\u0000\u0000\u021c"
        + "\u021d\u0001\u0000\u0000\u0000\u021d\u021f\u0001\u0000\u0000\u0000\u021e"
        + "\u0220\u0003<\u001e\u0000\u021f\u021e\u0001\u0000\u0000\u0000\u021f\u0220"
        + "\u0001\u0000\u0000\u0000\u0220\u0222\u0001\u0000\u0000\u0000\u0221\u0203"
        + "\u0001\u0000\u0000\u0000\u0221\u0212\u0001\u0000\u0000\u0000\u0222-\u0001"
        + "\u0000\u0000\u0000\u0223\u0224\u00032\u0019\u0000\u0224/\u0001\u0000\u0000"
        + "\u0000\u0225\u0226\u00032\u0019\u0000\u02261\u0001\u0000\u0000\u0000\u0227"
        + "\u022c\u00034\u001a\u0000\u0228\u0229\u0005q\u0000\u0000\u0229\u022b\u0003"
        + "4\u001a\u0000\u022a\u0228\u0001\u0000\u0000\u0000\u022b\u022e\u0001\u0000"
        + "\u0000\u0000\u022c\u022a\u0001\u0000\u0000\u0000\u022c\u022d\u0001\u0000"
        + "\u0000\u0000\u022d\u0230\u0001\u0000\u0000\u0000\u022e\u022c\u0001\u0000"
        + "\u0000\u0000\u022f\u0231\u0005q\u0000\u0000\u0230\u022f\u0001\u0000\u0000"
        + "\u0000\u0230\u0231\u0001\u0000\u0000\u0000\u02313\u0001\u0000\u0000\u0000"
        + "\u0232\u0234\u00036\u001b\u0000\u0233\u0232\u0001\u0000\u0000\u0000\u0233"
        + "\u0234\u0001\u0000\u0000\u0000\u0234\u0235\u0001\u0000\u0000\u0000\u0235"
        + "\u0237\u0003B!\u0000\u0236\u0238\u0003D\"\u0000\u0237\u0236\u0001\u0000"
        + "\u0000\u0000\u0237\u0238\u0001\u0000\u0000\u0000\u0238\u023f\u0001\u0000"
        + "\u0000\u0000\u0239\u023b\u00036\u001b\u0000\u023a\u023c\u0003D\"\u0000"
        + "\u023b\u023a\u0001\u0000\u0000\u0000\u023b\u023c\u0001\u0000\u0000\u0000"
        + "\u023c\u023f\u0001\u0000\u0000\u0000\u023d\u023f\u0003D\"\u0000\u023e"
        + "\u0233\u0001\u0000\u0000\u0000\u023e\u0239\u0001\u0000\u0000\u0000\u023e"
        + "\u023d\u0001\u0000\u0000\u0000\u023f5\u0001\u0000\u0000\u0000\u0240\u0241"
        + "\u00038\u001c\u0000\u0241\u0242\u0003:\u001d\u0000\u0242\u0246\u0001\u0000"
        + "\u0000\u0000\u0243\u0246\u0003:\u001d\u0000\u0244\u0246\u00038\u001c\u0000"
        + "\u0245\u0240\u0001\u0000\u0000\u0000\u0245\u0243\u0001\u0000\u0000\u0000"
        + "\u0245\u0244\u0001\u0000\u0000\u0000\u02467\u0001\u0000\u0000\u0000\u0247"
        + "\u024d\u0003\u00a6S\u0000\u0248\u0249\u0005u\u0000\u0000\u0249\u024a\u0003"
        + "\u0080@\u0000\u024a\u024b\u0005v\u0000\u0000\u024b\u024d\u0001\u0000\u0000"
        + "\u0000\u024c\u0247\u0001\u0000\u0000\u0000\u024c\u0248\u0001\u0000\u0000"
        + "\u0000\u024d\u024f\u0001\u0000\u0000\u0000\u024e\u0250\u0005I\u0000\u0000"
        + "\u024f\u024e\u0001\u0000\u0000\u0000\u024f\u0250\u0001\u0000\u0000\u0000"
        + "\u02509\u0001\u0000\u0000\u0000\u0251\u0257\u0005H\u0000\u0000\u0252\u0258"
        + "\u0003\u00a6S\u0000\u0253\u0254\u0005u\u0000\u0000\u0254\u0255\u0003\u0080"
        + "@\u0000\u0255\u0256\u0005v\u0000\u0000\u0256\u0258\u0001\u0000\u0000\u0000"
        + "\u0257\u0252\u0001\u0000\u0000\u0000\u0257\u0253\u0001\u0000\u0000\u0000"
        + "\u0258\u025a\u0001\u0000\u0000\u0000\u0259\u025b\u0005I\u0000\u0000\u025a"
        + "\u0259\u0001\u0000\u0000\u0000\u025a\u025b\u0001\u0000\u0000\u0000\u025b"
        + ";\u0001\u0000\u0000\u0000\u025c\u025d\u0005J\u0000\u0000\u025d\u025e\u0003"
        + "@ \u0000\u025e=\u0001\u0000\u0000\u0000\u025f\u0270\u0003l6\u0000\u0260"
        + "\u0262\u0005r\u0000\u0000\u0261\u0263\u0003l6\u0000\u0262\u0261\u0001"
        + "\u0000\u0000\u0000\u0262\u0263\u0001\u0000\u0000\u0000\u0263\u026e\u0001"
        + "\u0000\u0000\u0000\u0264\u0266\u0005r\u0000\u0000\u0265\u0267\u0003l6"
        + "\u0000\u0266\u0265\u0001\u0000\u0000\u0000\u0266\u0267\u0001\u0000\u0000"
        + "\u0000\u0267\u026c\u0001\u0000\u0000\u0000\u0268\u026a\u0005r\u0000\u0000"
        + "\u0269\u026b\u0003l6\u0000\u026a\u0269\u0001\u0000\u0000\u0000\u026a\u026b"
        + "\u0001\u0000\u0000\u0000\u026b\u026d\u0001\u0000\u0000\u0000\u026c\u0268"
        + "\u0001\u0000\u0000\u0000\u026c\u026d\u0001\u0000\u0000\u0000\u026d\u026f"
        + "\u0001\u0000\u0000\u0000\u026e\u0264\u0001\u0000\u0000\u0000\u026e\u026f"
        + "\u0001\u0000\u0000\u0000\u026f\u0271\u0001\u0000\u0000\u0000\u0270\u0260"
        + "\u0001\u0000\u0000\u0000\u0270\u0271\u0001\u0000\u0000\u0000\u0271\u0274"
        + "\u0001\u0000\u0000\u0000\u0272\u0274\u0003\u00a4R\u0000\u0273\u025f\u0001"
        + "\u0000\u0000\u0000\u0273\u0272\u0001\u0000\u0000\u0000\u0274?\u0001\u0000"
        + "\u0000\u0000\u0275\u027a\u0003>\u001f\u0000\u0276\u0277\u0005q\u0000\u0000"
        + "\u0277\u0279\u0003>\u001f\u0000\u0278\u0276\u0001\u0000\u0000\u0000\u0279"
        + "\u027c\u0001\u0000\u0000\u0000\u027a\u0278\u0001\u0000\u0000\u0000\u027a"
        + "\u027b\u0001\u0000\u0000\u0000\u027b\u027e\u0001\u0000\u0000\u0000\u027c"
        + "\u027a\u0001\u0000\u0000\u0000\u027d\u027f\u0005q\u0000\u0000\u027e\u027d"
        + "\u0001\u0000\u0000\u0000\u027e\u027f\u0001\u0000\u0000\u0000\u027fA\u0001"
        + "\u0000\u0000\u0000\u0280\u0285\u0003>\u001f\u0000\u0281\u0282\u00054\u0000"
        + "\u0000\u0282\u0284\u0003>\u001f\u0000\u0283\u0281\u0001\u0000\u0000\u0000"
        + "\u0284\u0287\u0001\u0000\u0000\u0000\u0285\u0283\u0001\u0000\u0000\u0000"
        + "\u0285\u0286\u0001\u0000\u0000\u0000\u0286\u0289\u0001\u0000\u0000\u0000"
        + "\u0287\u0285\u0001\u0000\u0000\u0000\u0288\u028a\u00054\u0000\u0000\u0289"
        + "\u0288\u0001\u0000\u0000\u0000\u0289\u028a\u0001\u0000\u0000\u0000\u028a"
        + "C\u0001\u0000\u0000\u0000\u028b\u028c\u0005O\u0000\u0000\u028c\u0290\u0003"
        + "F#\u0000\u028d\u028e\u0005N\u0000\u0000\u028e\u0290\u0003F#\u0000\u028f"
        + "\u028b\u0001\u0000\u0000\u0000\u028f\u028d\u0001\u0000\u0000\u0000\u0290"
        + "E\u0001\u0000\u0000\u0000\u0291\u0292\u0006#\uffff\uffff\u0000\u0292\u0293"
        + "\u0005u\u0000\u0000\u0293\u0294\u0003F#\u0000\u0294\u0295\u0005v\u0000"
        + "\u0000\u0295\u02a1\u0001\u0000\u0000\u0000\u0296\u0297\u00052\u0000\u0000"
        + "\u0297\u02a1\u0003F#\u0004\u0298\u029a\u0005P\u0000\u0000\u0299\u029b"
        + "\u0005Q\u0000\u0000\u029a\u0299\u0001\u0000\u0000\u0000\u029a\u029b\u0001"
        + "\u0000\u0000\u0000\u029b\u029e\u0001\u0000\u0000\u0000\u029c\u029e\u0005"
        + "Q\u0000\u0000\u029d\u0298\u0001\u0000\u0000\u0000\u029d\u029c\u0001\u0000"
        + "\u0000\u0000\u029e\u029f\u0001\u0000\u0000\u0000\u029f\u02a1\u0003H$\u0000"
        + "\u02a0\u0291\u0001\u0000\u0000\u0000\u02a0\u0296\u0001\u0000\u0000\u0000"
        + "\u02a0\u029d\u0001\u0000\u0000\u0000\u02a1\u02aa\u0001\u0000\u0000\u0000"
        + "\u02a2\u02a3\n\u0003\u0000\u0000\u02a3\u02a4\u00053\u0000\u0000\u02a4"
        + "\u02a9\u0003F#\u0004\u02a5\u02a6\n\u0002\u0000\u0000\u02a6\u02a7\u0005"
        + "4\u0000\u0000\u02a7\u02a9\u0003F#\u0003\u02a8\u02a2\u0001\u0000\u0000"
        + "\u0000\u02a8\u02a5\u0001\u0000\u0000\u0000\u02a9\u02ac\u0001\u0000\u0000"
        + "\u0000\u02aa\u02a8\u0001\u0000\u0000\u0000\u02aa\u02ab\u0001\u0000\u0000"
        + "\u0000\u02abG\u0001\u0000\u0000\u0000\u02ac\u02aa\u0001\u0000\u0000\u0000"
        + "\u02ad\u02ae\u0003l6\u0000\u02ae\u02af\u0005r\u0000\u0000\u02af\u02b4"
        + "\u0003l6\u0000\u02b0\u02b1\u0005s\u0000\u0000\u02b1\u02b3\u0003l6\u0000"
        + "\u02b2\u02b0\u0001\u0000\u0000\u0000\u02b3\u02b6\u0001\u0000\u0000\u0000"
        + "\u02b4\u02b2\u0001\u0000\u0000\u0000\u02b4\u02b5\u0001\u0000\u0000\u0000"
        + "\u02b5\u02c0\u0001\u0000\u0000\u0000\u02b6\u02b4\u0001\u0000\u0000\u0000"
        + "\u02b7\u02bc\u0003l6\u0000\u02b8\u02b9\u0005s\u0000\u0000\u02b9\u02bb"
        + "\u0003l6\u0000\u02ba\u02b8\u0001\u0000\u0000\u0000\u02bb\u02be\u0001\u0000"
        + "\u0000\u0000\u02bc\u02ba\u0001\u0000\u0000\u0000\u02bc\u02bd\u0001\u0000"
        + "\u0000\u0000\u02bd\u02c0\u0001\u0000\u0000\u0000\u02be\u02bc\u0001\u0000"
        + "\u0000\u0000\u02bf\u02ad\u0001\u0000\u0000\u0000\u02bf\u02b7\u0001\u0000"
        + "\u0000\u0000\u02c0I\u0001\u0000\u0000\u0000\u02c1\u02c2\u0005I\u0000\u0000"
        + "\u02c2\u02ce\u0005]\u0000\u0000\u02c3\u02c8\u0003L&\u0000\u02c4\u02c5"
        + "\u0005q\u0000\u0000\u02c5\u02c7\u0003L&\u0000\u02c6\u02c4\u0001\u0000"
        + "\u0000\u0000\u02c7\u02ca\u0001\u0000\u0000\u0000\u02c8\u02c6\u0001\u0000"
        + "\u0000\u0000\u02c8\u02c9\u0001\u0000\u0000\u0000\u02c9\u02cb\u0001\u0000"
        + "\u0000\u0000\u02ca\u02c8\u0001\u0000\u0000\u0000\u02cb\u02cc\u0005]\u0000"
        + "\u0000\u02cc\u02ce\u0001\u0000\u0000\u0000\u02cd\u02c1\u0001\u0000\u0000"
        + "\u0000\u02cd\u02c3\u0001\u0000\u0000\u0000\u02ceK\u0001\u0000\u0000\u0000"
        + "\u02cf\u02d0\u0007\u0005\u0000\u0000\u02d0M\u0001\u0000\u0000\u0000\u02d1"
        + "\u02d2\u0007\u0006\u0000\u0000\u02d2\u02d3\u0003P(\u0000\u02d3O\u0001"
        + "\u0000\u0000\u0000\u02d4\u02d9\u0003R)\u0000\u02d5\u02d6\u0005q\u0000"
        + "\u0000\u02d6\u02d8\u0003R)\u0000\u02d7\u02d5\u0001\u0000\u0000\u0000\u02d8"
        + "\u02db\u0001\u0000\u0000\u0000\u02d9\u02d7\u0001\u0000\u0000\u0000\u02d9"
        + "\u02da\u0001\u0000\u0000\u0000\u02daQ\u0001\u0000\u0000\u0000\u02db\u02d9"
        + "\u0001\u0000\u0000\u0000\u02dc\u02df\u0003\u00a6S\u0000\u02dd\u02de\u0005"
        + "t\u0000\u0000\u02de\u02e0\u0003\u00a6S\u0000\u02df\u02dd\u0001\u0000\u0000"
        + "\u0000\u02df\u02e0\u0001\u0000\u0000\u0000\u02e0S\u0001\u0000\u0000\u0000"
        + "\u02e1\u02e2\u0005(\u0000\u0000\u02e2\u02e3\u0003^/\u0000\u02e3\u02e4"
        + "\u0005)\u0000\u0000\u02e4\u02ed\u0003 \u0010\u0000\u02e5\u02e6\u0005*"
        + "\u0000\u0000\u02e6\u02e7\u0005(\u0000\u0000\u02e7\u02e8\u0003^/\u0000"
        + "\u02e8\u02e9\u0005)\u0000\u0000\u02e9\u02ea\u0003 \u0010\u0000\u02ea\u02ec"
        + "\u0001\u0000\u0000\u0000\u02eb\u02e5\u0001\u0000\u0000\u0000\u02ec\u02ef"
        + "\u0001\u0000\u0000\u0000\u02ed\u02eb\u0001\u0000\u0000\u0000\u02ed\u02ee"
        + "\u0001\u0000\u0000\u0000\u02ee\u02f2\u0001\u0000\u0000\u0000\u02ef\u02ed"
        + "\u0001\u0000\u0000\u0000\u02f0\u02f1\u0005*\u0000\u0000\u02f1\u02f3\u0003"
        + " \u0010\u0000\u02f2\u02f0\u0001\u0000\u0000\u0000\u02f2\u02f3\u0001\u0000"
        + "\u0000\u0000\u02f3\u02f4\u0001\u0000\u0000\u0000\u02f4\u02f5\u0005n\u0000"
        + "\u0000\u02f5\u030f\u0001\u0000\u0000\u0000\u02f6\u02f7\u0005(\u0000\u0000"
        + "\u02f7\u02f8\u0003^/\u0000\u02f8\u02f9\u0005\u001c\u0000\u0000\u02f9\u02fa"
        + "\u0003 \u0010\u0000\u02fa\u0304\u0005\u001d\u0000\u0000\u02fb\u02fc\u0005"
        + "*\u0000\u0000\u02fc\u02fd\u0005(\u0000\u0000\u02fd\u02fe\u0003^/\u0000"
        + "\u02fe\u02ff\u0005\u001c\u0000\u0000\u02ff\u0300\u0003 \u0010\u0000\u0300"
        + "\u0301\u0005\u001d\u0000\u0000\u0301\u0303\u0001\u0000\u0000\u0000\u0302"
        + "\u02fb\u0001\u0000\u0000\u0000\u0303\u0306\u0001\u0000\u0000\u0000\u0304"
        + "\u0302\u0001\u0000\u0000\u0000\u0304\u0305\u0001\u0000\u0000\u0000\u0305"
        + "\u030c\u0001\u0000\u0000\u0000\u0306\u0304\u0001\u0000\u0000\u0000\u0307"
        + "\u0308\u0005*\u0000\u0000\u0308\u0309\u0005\u001c\u0000\u0000\u0309\u030a"
        + "\u0003 \u0010\u0000\u030a\u030b\u0005\u001d\u0000\u0000\u030b\u030d\u0001"
        + "\u0000\u0000\u0000\u030c\u0307\u0001\u0000\u0000\u0000\u030c\u030d\u0001"
        + "\u0000\u0000\u0000\u030d\u030f\u0001\u0000\u0000\u0000\u030e\u02e1\u0001"
        + "\u0000\u0000\u0000\u030e\u02f6\u0001\u0000\u0000\u0000\u030fU\u0001\u0000"
        + "\u0000\u0000\u0310\u0311\u0005\u0016\u0000\u0000\u0311\u0312\u0003\n\u0005"
        + "\u0000\u0312\u0313\u0005:\u0000\u0000\u0313\u0314\u0003\u0080@\u0000\u0314"
        + "\u0315\u0005B\u0000\u0000\u0315\u0318\u0003\u0080@\u0000\u0316\u0317\u0005"
        + "\u0018\u0000\u0000\u0317\u0319\u0003\u0080@\u0000\u0318\u0316\u0001\u0000"
        + "\u0000\u0000\u0318\u0319\u0001\u0000\u0000\u0000\u0319\u0322\u0001\u0000"
        + "\u0000\u0000\u031a\u031b\u0005m\u0000\u0000\u031b\u031c\u0003 \u0010\u0000"
        + "\u031c\u031d\u0005n\u0000\u0000\u031d\u0323\u0001\u0000\u0000\u0000\u031e"
        + "\u031f\u0005\u001c\u0000\u0000\u031f\u0320\u0003 \u0010\u0000\u0320\u0321"
        + "\u0005\u001d\u0000\u0000\u0321\u0323\u0001\u0000\u0000\u0000\u0322\u031a"
        + "\u0001\u0000\u0000\u0000\u0322\u031e\u0001\u0000\u0000\u0000\u0323\u035d"
        + "\u0001\u0000\u0000\u0000\u0324\u0325\u0005\u0016\u0000\u0000\u0325\u0326"
        + "\u0003\n\u0005\u0000\u0326\u0327\u0005M\u0000\u0000\u0327\u0330\u0003"
        + "\u0080@\u0000\u0328\u0329\u0005m\u0000\u0000\u0329\u032a\u0003 \u0010"
        + "\u0000\u032a\u032b\u0005n\u0000\u0000\u032b\u0331\u0001\u0000\u0000\u0000"
        + "\u032c\u032d\u0005\u001c\u0000\u0000\u032d\u032e\u0003 \u0010\u0000\u032e"
        + "\u032f\u0005\u001d\u0000\u0000\u032f\u0331\u0001\u0000\u0000\u0000\u0330"
        + "\u0328\u0001\u0000\u0000\u0000\u0330\u032c\u0001\u0000\u0000\u0000\u0331"
        + "\u035d\u0001\u0000\u0000\u0000\u0332\u0333\u0005\u0016\u0000\u0000\u0333"
        + "\u0335\u0005u\u0000\u0000\u0334\u0336\u0003X,\u0000\u0335\u0334\u0001"
        + "\u0000\u0000\u0000\u0335\u0336\u0001\u0000\u0000\u0000\u0336\u0337\u0001"
        + "\u0000\u0000\u0000\u0337\u0339\u0005\u001f\u0000\u0000\u0338\u033a\u0003"
        + "\u0080@\u0000\u0339\u0338\u0001\u0000\u0000\u0000\u0339\u033a\u0001\u0000"
        + "\u0000\u0000\u033a\u033b\u0001\u0000\u0000\u0000\u033b\u033d\u0005\u001f"
        + "\u0000\u0000\u033c\u033e\u0003Z-\u0000\u033d\u033c\u0001\u0000\u0000\u0000"
        + "\u033d\u033e\u0001\u0000\u0000\u0000\u033e\u033f\u0001\u0000\u0000\u0000"
        + "\u033f\u0348\u0005v\u0000\u0000\u0340\u0341\u0005m\u0000\u0000\u0341\u0342"
        + "\u0003 \u0010\u0000\u0342\u0343\u0005n\u0000\u0000\u0343\u0349\u0001\u0000"
        + "\u0000\u0000\u0344\u0345\u0005\u001c\u0000\u0000\u0345\u0346\u0003 \u0010"
        + "\u0000\u0346\u0347\u0005\u001d\u0000\u0000\u0347\u0349\u0001\u0000\u0000"
        + "\u0000\u0348\u0340\u0001\u0000\u0000\u0000\u0348\u0344\u0001\u0000\u0000"
        + "\u0000\u0349\u035d\u0001\u0000\u0000\u0000\u034a\u034b\u0005\u0016\u0000"
        + "\u0000\u034b\u034c\u0005u\u0000\u0000\u034c\u034d\u0003\n\u0005\u0000"
        + "\u034d\u034e\u0005q\u0000\u0000\u034e\u034f\u0003\n\u0005\u0000\u034f"
        + "\u0350\u0005v\u0000\u0000\u0350\u0351\u0005M\u0000\u0000\u0351\u035a\u0003"
        + "\u0080@\u0000\u0352\u0353\u0005m\u0000\u0000\u0353\u0354\u0003 \u0010"
        + "\u0000\u0354\u0355\u0005n\u0000\u0000\u0355\u035b\u0001\u0000\u0000\u0000"
        + "\u0356\u0357\u0005\u001c\u0000\u0000\u0357\u0358\u0003 \u0010\u0000\u0358"
        + "\u0359\u0005\u001d\u0000\u0000\u0359\u035b\u0001\u0000\u0000\u0000\u035a"
        + "\u0352\u0001\u0000\u0000\u0000\u035a\u0356\u0001\u0000\u0000\u0000\u035b"
        + "\u035d\u0001\u0000\u0000\u0000\u035c\u0310\u0001\u0000\u0000\u0000\u035c"
        + "\u0324\u0001\u0000\u0000\u0000\u035c\u0332\u0001\u0000\u0000\u0000\u035c"
        + "\u034a\u0001\u0000\u0000\u0000\u035dW\u0001\u0000\u0000\u0000\u035e\u0362"
        + "\u0003p8\u0000\u035f\u0362\u0003r9\u0000\u0360\u0362\u0003\u0018\f\u0000"
        + "\u0361\u035e\u0001\u0000\u0000\u0000\u0361\u035f\u0001\u0000\u0000\u0000"
        + "\u0361\u0360\u0001\u0000\u0000\u0000\u0362Y\u0001\u0000\u0000\u0000\u0363"
        + "\u0366\u0003r9\u0000\u0364\u0366\u0003\u0018\f\u0000\u0365\u0363\u0001"
        + "\u0000\u0000\u0000\u0365\u0364\u0001\u0000\u0000\u0000\u0366[\u0001\u0000"
        + "\u0000\u0000\u0367\u0368\u0005\u0017\u0000\u0000\u0368\u0371\u0003^/\u0000"
        + "\u0369\u036a\u0005m\u0000\u0000\u036a\u036b\u0003 \u0010\u0000\u036b\u036c"
        + "\u0005n\u0000\u0000\u036c\u0372\u0001\u0000\u0000\u0000\u036d\u036e\u0005"
        + "\u001c\u0000\u0000\u036e\u036f\u0003 \u0010\u0000\u036f\u0370\u0005\u001d"
        + "\u0000\u0000\u0370\u0372\u0001\u0000\u0000\u0000\u0371\u0369\u0001\u0000"
        + "\u0000\u0000\u0371\u036d\u0001\u0000\u0000\u0000\u0372]\u0001\u0000\u0000"
        + "\u0000\u0373\u0374\u0006/\uffff\uffff\u0000\u0374\u03b8\u00050\u0000\u0000"
        + "\u0375\u03b8\u00051\u0000\u0000\u0376\u0377\u0005u\u0000\u0000\u0377\u0378"
        + "\u0003^/\u0000\u0378\u0379\u0005v\u0000\u0000\u0379\u03b8\u0001\u0000"
        + "\u0000\u0000\u037a\u037b\u0005u\u0000\u0000\u037b\u037c\u0003\u0080@\u0000"
        + "\u037c\u037d\u0005v\u0000\u0000\u037d\u03b8\u0001\u0000\u0000\u0000\u037e"
        + "\u037f\u00052\u0000\u0000\u037f\u03b8\u0003^/\u0006\u0380\u0382\u0003"
        + "b1\u0000\u0381\u0380\u0001\u0000\u0000\u0000\u0381\u0382\u0001\u0000\u0000"
        + "\u0000\u0382\u0383\u0001\u0000\u0000\u0000\u0383\u0384\u0003d2\u0000\u0384"
        + "\u0385\u0005+\u0000\u0000\u0385\u038b\u0003`0\u0000\u0386\u038c\u0003"
        + "\u00a6S\u0000\u0387\u0388\u0005u\u0000\u0000\u0388\u0389\u0003\u0080@"
        + "\u0000\u0389\u038a\u0005v\u0000\u0000\u038a\u038c\u0001\u0000\u0000\u0000"
        + "\u038b\u0386\u0001\u0000\u0000\u0000\u038b\u0387\u0001\u0000\u0000\u0000"
        + "\u038c\u038e\u0001\u0000\u0000\u0000\u038d\u038f\u0003B!\u0000\u038e\u038d"
        + "\u0001\u0000\u0000\u0000\u038e\u038f\u0001\u0000\u0000\u0000\u038f\u0391"
        + "\u0001\u0000\u0000\u0000\u0390\u0392\u0003D\"\u0000\u0391\u0390\u0001"
        + "\u0000\u0000\u0000\u0391\u0392\u0001\u0000\u0000\u0000\u0392\u0395\u0001"
        + "\u0000\u0000\u0000\u0393\u0394\u0005J\u0000\u0000\u0394\u0396\u0003@ "
        + "\u0000\u0395\u0393\u0001\u0000\u0000\u0000\u0395\u0396\u0001\u0000\u0000"
        + "\u0000\u0396\u03b8\u0001\u0000\u0000\u0000\u0397\u0399\u0005i\u0000\u0000"
        + "\u0398\u039a\u0007\u0004\u0000\u0000\u0399\u0398\u0001\u0000\u0000\u0000"
        + "\u0399\u039a\u0001\u0000\u0000\u0000\u039a\u03a3\u0001\u0000\u0000\u0000"
        + "\u039b\u03a1\u0003`0\u0000\u039c\u03a2\u0003\u00a6S\u0000\u039d\u039e"
        + "\u0005u\u0000\u0000\u039e\u039f\u0003\u0080@\u0000\u039f\u03a0\u0005v"
        + "\u0000\u0000\u03a0\u03a2\u0001\u0000\u0000\u0000\u03a1\u039c\u0001\u0000"
        + "\u0000\u0000\u03a1\u039d\u0001\u0000\u0000\u0000\u03a2\u03a4\u0001\u0000"
        + "\u0000\u0000\u03a3\u039b\u0001\u0000\u0000\u0000\u03a3\u03a4\u0001\u0000"
        + "\u0000\u0000\u03a4\u03b8\u0001\u0000\u0000\u0000\u03a5\u03a7\u0003b1\u0000"
        + "\u03a6\u03a5\u0001\u0000\u0000\u0000\u03a6\u03a7\u0001\u0000\u0000\u0000"
        + "\u03a7\u03a8\u0001\u0000\u0000\u0000\u03a8\u03a9\u0003d2\u0000\u03a9\u03ab"
        + "\u0005i\u0000\u0000\u03aa\u03ac\u0007\u0004\u0000\u0000\u03ab\u03aa\u0001"
        + "\u0000\u0000\u0000\u03ab\u03ac\u0001\u0000\u0000\u0000\u03ac\u03b5\u0001"
        + "\u0000\u0000\u0000\u03ad\u03b3\u0003`0\u0000\u03ae\u03b4\u0003\u00a6S"
        + "\u0000\u03af\u03b0\u0005u\u0000\u0000\u03b0\u03b1\u0003\u0080@\u0000\u03b1"
        + "\u03b2\u0005v\u0000\u0000\u03b2\u03b4\u0001\u0000\u0000\u0000\u03b3\u03ae"
        + "\u0001\u0000\u0000\u0000\u03b3\u03af\u0001\u0000\u0000\u0000\u03b4\u03b6"
        + "\u0001\u0000\u0000\u0000\u03b5\u03ad\u0001\u0000\u0000\u0000\u03b5\u03b6"
        + "\u0001\u0000\u0000\u0000\u03b6\u03b8\u0001\u0000\u0000\u0000\u03b7\u0373"
        + "\u0001\u0000\u0000\u0000\u03b7\u0375\u0001\u0000\u0000\u0000\u03b7\u0376"
        + "\u0001\u0000\u0000\u0000\u03b7\u037a\u0001\u0000\u0000\u0000\u03b7\u037e"
        + "\u0001\u0000\u0000\u0000\u03b7\u0381\u0001\u0000\u0000\u0000\u03b7\u0397"
        + "\u0001\u0000\u0000\u0000\u03b7\u03a6\u0001\u0000\u0000\u0000\u03b8\u03c1"
        + "\u0001\u0000\u0000\u0000\u03b9\u03ba\n\u0005\u0000\u0000\u03ba\u03bb\u0005"
        + "3\u0000\u0000\u03bb\u03c0\u0003^/\u0006\u03bc\u03bd\n\u0004\u0000\u0000"
        + "\u03bd\u03be\u00054\u0000\u0000\u03be\u03c0\u0003^/\u0005\u03bf\u03b9"
        + "\u0001\u0000\u0000\u0000\u03bf\u03bc\u0001\u0000\u0000\u0000\u03c0\u03c3"
        + "\u0001\u0000\u0000\u0000\u03c1\u03bf\u0001\u0000\u0000\u0000\u03c1\u03c2"
        + "\u0001\u0000\u0000\u0000\u03c2_\u0001\u0000\u0000\u0000\u03c3\u03c1\u0001"
        + "\u0000\u0000\u0000\u03c4\u03c5\u0007\u0007\u0000\u0000\u03c5a\u0001\u0000"
        + "\u0000\u0000\u03c6\u03c7\u0007\b\u0000\u0000\u03c7c\u0001\u0000\u0000"
        + "\u0000\u03c8\u03cd\u0003h4\u0000\u03c9\u03ca\u0005q\u0000\u0000\u03ca"
        + "\u03cc\u0003h4\u0000\u03cb\u03c9\u0001\u0000\u0000\u0000\u03cc\u03cf\u0001"
        + "\u0000\u0000\u0000\u03cd\u03cb\u0001\u0000\u0000\u0000\u03cd\u03ce\u0001"
        + "\u0000\u0000\u0000\u03ce\u03d1\u0001\u0000\u0000\u0000\u03cf\u03cd\u0001"
        + "\u0000\u0000\u0000\u03d0\u03d2\u0003f3\u0000\u03d1\u03d0\u0001\u0000\u0000"
        + "\u0000\u03d1\u03d2\u0001\u0000\u0000\u0000\u03d2\u03d4\u0001\u0000\u0000"
        + "\u0000\u03d3\u03d5\u0003J%\u0000\u03d4\u03d3\u0001\u0000\u0000\u0000\u03d4"
        + "\u03d5\u0001\u0000\u0000\u0000\u03d5\u03d7\u0001\u0000\u0000\u0000\u03d6"
        + "\u03d8\u0003N\'\u0000\u03d7\u03d6\u0001\u0000\u0000\u0000\u03d7\u03d8"
        + "\u0001\u0000\u0000\u0000\u03d8e\u0001\u0000\u0000\u0000\u03d9\u03da\u0005"
        + "R\u0000\u0000\u03da\u03db\u0005S\u0000\u0000\u03db\u03dc\u0005T\u0000"
        + "\u0000\u03dc\u03dd\u0007\t\u0000\u0000\u03ddg\u0001\u0000\u0000\u0000"
        + "\u03de\u03e2\u0003l6\u0000\u03df\u03e1\u0003\u009cN\u0000\u03e0\u03df"
        + "\u0001\u0000\u0000\u0000\u03e1\u03e4\u0001\u0000\u0000\u0000\u03e2\u03e0"
        + "\u0001\u0000\u0000\u0000\u03e2\u03e3\u0001\u0000\u0000\u0000\u03e3\u03f5"
        + "\u0001\u0000\u0000\u0000\u03e4\u03e2\u0001\u0000\u0000\u0000\u03e5\u03f5"
        + "\u0003\u00a4R\u0000\u03e6\u03ea\u0005\u0019\u0000\u0000\u03e7\u03e9\u0003"
        + "\u009cN\u0000\u03e8\u03e7\u0001\u0000\u0000\u0000\u03e9\u03ec\u0001\u0000"
        + "\u0000\u0000\u03ea\u03e8\u0001\u0000\u0000\u0000\u03ea\u03eb\u0001\u0000"
        + "\u0000\u0000\u03eb\u03f5\u0001\u0000\u0000\u0000\u03ec\u03ea\u0001\u0000"
        + "\u0000\u0000\u03ed\u03f1\u0003\u0012\t\u0000\u03ee\u03f0\u0003\u009cN"
        + "\u0000\u03ef\u03ee\u0001\u0000\u0000\u0000\u03f0\u03f3\u0001\u0000\u0000"
        + "\u0000\u03f1\u03ef\u0001\u0000\u0000\u0000\u03f1\u03f2\u0001\u0000\u0000"
        + "\u0000\u03f2\u03f5\u0001\u0000\u0000\u0000\u03f3\u03f1\u0001\u0000\u0000"
        + "\u0000\u03f4\u03de\u0001\u0000\u0000\u0000\u03f4\u03e5\u0001\u0000\u0000"
        + "\u0000\u03f4\u03e6\u0001\u0000\u0000\u0000\u03f4\u03ed\u0001\u0000\u0000"
        + "\u0000\u03f5i\u0001\u0000\u0000\u0000\u03f6\u03f7\u0005L\u0000\u0000\u03f7"
        + "\u03f8\u0007\u0006\u0000\u0000\u03f8\u03f9\u0005M\u0000\u0000\u03f9k\u0001"
        + "\u0000\u0000\u0000\u03fa\u03fb\u0007\n\u0000\u0000\u03fbm\u0001\u0000"
        + "\u0000\u0000\u03fc\u03fd\u0005\u0012\u0000\u0000\u03fd\u03fe\u0003\n\u0005"
        + "\u0000\u03fe\u03ff\u0005:\u0000\u0000\u03ff\u0400\u0003\u0080@\u0000\u0400"
        + "o\u0001\u0000\u0000\u0000\u0401\u0402\u0005\u0010\u0000\u0000\u0402\u0403"
        + "\u0003\n\u0005\u0000\u0403\u0404\u0005:\u0000\u0000\u0404\u0405\u0003"
        + "\u0080@\u0000\u0405q\u0001\u0000\u0000\u0000\u0406\u0408\u0005\u0011\u0000"
        + "\u0000\u0407\u0406\u0001\u0000\u0000\u0000\u0407\u0408\u0001\u0000\u0000"
        + "\u0000\u0408\u0409\u0001\u0000\u0000\u0000\u0409\u040a\u0003~?\u0000\u040a"
        + "\u040b\u0005:\u0000\u0000\u040b\u040c\u0003\u0080@\u0000\u040cs\u0001"
        + "\u0000\u0000\u0000\u040d\u040e\u0005\u0004\u0000\u0000\u040e\u040f\u0005"
        + "u\u0000\u0000\u040f\u0410\u0003\u0080@\u0000\u0410\u0411\u0005q\u0000"
        + "\u0000\u0411\u0412\u0003\u0080@\u0000\u0412\u0415\u0005v\u0000\u0000\u0413"
        + "\u0414\u0005\u000b\u0000\u0000\u0414\u0416\u0003\n\u0005\u0000\u0415\u0413"
        + "\u0001\u0000\u0000\u0000\u0415\u0416\u0001\u0000\u0000\u0000\u0416u\u0001"
        + "\u0000\u0000\u0000\u0417\u0418\u0005\u0005\u0000\u0000\u0418\u0419\u0003"
        + "\u0080@\u0000\u0419w\u0001\u0000\u0000\u0000\u041a\u041b\u0005\u0015\u0000"
        + "\u0000\u041b\u041c\u0005u\u0000\u0000\u041c\u0421\u0003\u0080@\u0000\u041d"
        + "\u041e\u0005q\u0000\u0000\u041e\u0420\u0003\u0080@\u0000\u041f\u041d\u0001"
        + "\u0000\u0000\u0000\u0420\u0423\u0001\u0000\u0000\u0000\u0421\u041f\u0001"
        + "\u0000\u0000\u0000\u0421\u0422\u0001\u0000\u0000\u0000\u0422\u0424\u0001"
        + "\u0000\u0000\u0000\u0423\u0421\u0001\u0000\u0000\u0000\u0424\u0425\u0005"
        + "v\u0000\u0000\u0425y\u0001\u0000\u0000\u0000\u0426\u0427\u0007\u000b\u0000"
        + "\u0000\u0427\u0428\u0005u\u0000\u0000\u0428\u0429\u0003\u0080@\u0000\u0429"
        + "\u042a\u0005q\u0000\u0000\u042a\u0431\u0003\u0080@\u0000\u042b\u042c\u0005"
        + "q\u0000\u0000\u042c\u042f\u0003\u0080@\u0000\u042d\u042e\u0005q\u0000"
        + "\u0000\u042e\u0430\u0003\u0080@\u0000\u042f\u042d\u0001\u0000\u0000\u0000"
        + "\u042f\u0430\u0001\u0000\u0000\u0000\u0430\u0432\u0001\u0000\u0000\u0000"
        + "\u0431\u042b\u0001\u0000\u0000\u0000\u0431\u0432\u0001\u0000\u0000\u0000"
        + "\u0432\u0433\u0001\u0000\u0000\u0000\u0433\u0435\u0005v\u0000\u0000\u0434"
        + "\u0436\u0003|>\u0000\u0435\u0434\u0001\u0000\u0000\u0000\u0435\u0436\u0001"
        + "\u0000\u0000\u0000\u0436\u0439\u0001\u0000\u0000\u0000\u0437\u0438\u0005"
        + "\u000b\u0000\u0000\u0438\u043a\u0003\n\u0005\u0000\u0439\u0437\u0001\u0000"
        + "\u0000\u0000\u0439\u043a\u0001\u0000\u0000\u0000\u043a{\u0001\u0000\u0000"
        + "\u0000\u043b\u043e\u0005J\u0000\u0000\u043c\u043f\u0003\u0012\t\u0000"
        + "\u043d\u043f\u0003\n\u0005\u0000\u043e\u043c\u0001\u0000\u0000\u0000\u043e"
        + "\u043d\u0001\u0000\u0000\u0000\u043f}\u0001\u0000\u0000\u0000\u0440\u0449"
        + "\u0003\n\u0005\u0000\u0441\u0442\u0005\u001e\u0000\u0000\u0442\u0448\u0003"
        + "\u00a2Q\u0000\u0443\u0444\u0005\u001a\u0000\u0000\u0444\u0445\u0003\u0080"
        + "@\u0000\u0445\u0446\u0005\u001b\u0000\u0000\u0446\u0448\u0001\u0000\u0000"
        + "\u0000\u0447\u0441\u0001\u0000\u0000\u0000\u0447\u0443\u0001\u0000\u0000"
        + "\u0000\u0448\u044b\u0001\u0000\u0000\u0000\u0449\u0447\u0001\u0000\u0000"
        + "\u0000\u0449\u044a\u0001\u0000\u0000\u0000\u044a\u007f\u0001\u0000\u0000"
        + "\u0000\u044b\u0449\u0001\u0000\u0000\u0000\u044c\u044d\u0003\u0082A\u0000"
        + "\u044d\u0081\u0001\u0000\u0000\u0000\u044e\u0453\u0003\u0084B\u0000\u044f"
        + "\u0450\u00054\u0000\u0000\u0450\u0452\u0003\u0084B\u0000\u0451\u044f\u0001"
        + "\u0000\u0000\u0000\u0452\u0455\u0001\u0000\u0000\u0000\u0453\u0451\u0001"
        + "\u0000\u0000\u0000\u0453\u0454\u0001\u0000\u0000\u0000\u0454\u0083\u0001"
        + "\u0000\u0000\u0000\u0455\u0453\u0001\u0000\u0000\u0000\u0456\u045b\u0003"
        + "\u0086C\u0000\u0457\u0458\u00053\u0000\u0000\u0458\u045a\u0003\u0086C"
        + "\u0000\u0459\u0457\u0001\u0000\u0000\u0000\u045a\u045d\u0001\u0000\u0000"
        + "\u0000\u045b\u0459\u0001\u0000\u0000\u0000\u045b\u045c\u0001\u0000\u0000"
        + "\u0000\u045c\u0085\u0001\u0000\u0000\u0000\u045d\u045b\u0001\u0000\u0000"
        + "\u0000\u045e\u045f\u00052\u0000\u0000\u045f\u0462\u0003\u0086C\u0000\u0460"
        + "\u0462\u0003\u0088D\u0000\u0461\u045e\u0001\u0000\u0000\u0000\u0461\u0460"
        + "\u0001\u0000\u0000\u0000\u0462\u0087\u0001\u0000\u0000\u0000\u0463\u0467"
        + "\u0003\u008aE\u0000\u0464\u0465\u0003`0\u0000\u0465\u0466\u0003\u008a"
        + "E\u0000\u0466\u0468\u0001\u0000\u0000\u0000\u0467\u0464\u0001\u0000\u0000"
        + "\u0000\u0467\u0468\u0001\u0000\u0000\u0000\u0468\u0089\u0001\u0000\u0000"
        + "\u0000\u0469\u046e\u0003\u008cF\u0000\u046a\u046b\u0005\"\u0000\u0000"
        + "\u046b\u046d\u0003\u008cF\u0000\u046c\u046a\u0001\u0000\u0000\u0000\u046d"
        + "\u0470\u0001\u0000\u0000\u0000\u046e\u046c\u0001\u0000\u0000\u0000\u046e"
        + "\u046f\u0001\u0000\u0000\u0000\u046f\u008b\u0001\u0000\u0000\u0000\u0470"
        + "\u046e\u0001\u0000\u0000\u0000\u0471\u0476\u0003\u008eG\u0000\u0472\u0473"
        + "\u0005#\u0000\u0000\u0473\u0475\u0003\u008eG\u0000\u0474\u0472\u0001\u0000"
        + "\u0000\u0000\u0475\u0478\u0001\u0000\u0000\u0000\u0476\u0474\u0001\u0000"
        + "\u0000\u0000\u0476\u0477\u0001\u0000\u0000\u0000\u0477\u008d\u0001\u0000"
        + "\u0000\u0000\u0478\u0476\u0001\u0000\u0000\u0000\u0479\u047e\u0003\u0090"
        + "H\u0000\u047a\u047b\u0005!\u0000\u0000\u047b\u047d\u0003\u0090H\u0000"
        + "\u047c\u047a\u0001\u0000\u0000\u0000\u047d\u0480\u0001\u0000\u0000\u0000"
        + "\u047e\u047c\u0001\u0000\u0000\u0000\u047e\u047f\u0001\u0000\u0000\u0000"
        + "\u047f\u008f\u0001\u0000\u0000\u0000\u0480\u047e\u0001\u0000\u0000\u0000"
        + "\u0481\u0486\u0003\u0092I\u0000\u0482\u0483\u0007\f\u0000\u0000\u0483"
        + "\u0485\u0003\u0092I\u0000\u0484\u0482\u0001\u0000\u0000\u0000\u0485\u0488"
        + "\u0001\u0000\u0000\u0000\u0486\u0484\u0001\u0000\u0000\u0000\u0486\u0487"
        + "\u0001\u0000\u0000\u0000\u0487\u0091\u0001\u0000\u0000\u0000\u0488\u0486"
        + "\u0001\u0000\u0000\u0000\u0489\u048e\u0003\u0094J\u0000\u048a\u048b\u0007"
        + "\r\u0000\u0000\u048b\u048d\u0003\u0094J\u0000\u048c\u048a\u0001\u0000"
        + "\u0000\u0000\u048d\u0490\u0001\u0000\u0000\u0000\u048e\u048c\u0001\u0000"
        + "\u0000\u0000\u048e\u048f\u0001\u0000\u0000\u0000\u048f\u0093\u0001\u0000"
        + "\u0000\u0000\u0490\u048e\u0001\u0000\u0000\u0000\u0491\u049a\u0003\u0098"
        + "L\u0000\u0492\u0496\u0003\u0096K\u0000\u0493\u0496\u0005s\u0000\u0000"
        + "\u0494\u0496\u0005 \u0000\u0000\u0495\u0492\u0001\u0000\u0000\u0000\u0495"
        + "\u0493\u0001\u0000\u0000\u0000\u0495\u0494\u0001\u0000\u0000\u0000\u0496"
        + "\u0497\u0001\u0000\u0000\u0000\u0497\u0499\u0003\u0098L\u0000\u0498\u0495"
        + "\u0001\u0000\u0000\u0000\u0499\u049c\u0001\u0000\u0000\u0000\u049a\u0498"
        + "\u0001\u0000\u0000\u0000\u049a\u049b\u0001\u0000\u0000\u0000\u049b\u0095"
        + "\u0001\u0000\u0000\u0000\u049c\u049a\u0001\u0000\u0000\u0000\u049d\u049e"
        + "\u0004K\u0005\u0000\u049e\u049f\u0005y\u0000\u0000\u049f\u0097\u0001\u0000"
        + "\u0000\u0000\u04a0\u04a1\u0007\u000e\u0000\u0000\u04a1\u04a4\u0003\u0098"
        + "L\u0000\u04a2\u04a4\u0003\u009aM\u0000\u04a3\u04a0\u0001\u0000\u0000\u0000"
        + "\u04a3\u04a2\u0001\u0000\u0000\u0000\u04a4\u0099\u0001\u0000\u0000\u0000"
        + "\u04a5\u04a9\u0003\u009eO\u0000\u04a6\u04a8\u0003\u009cN\u0000\u04a7\u04a6"
        + "\u0001\u0000\u0000\u0000\u04a8\u04ab\u0001\u0000\u0000\u0000\u04a9\u04a7"
        + "\u0001\u0000\u0000\u0000\u04a9\u04aa\u0001\u0000\u0000\u0000\u04aa\u009b"
        + "\u0001\u0000\u0000\u0000\u04ab\u04a9\u0001\u0000\u0000\u0000\u04ac\u04ad"
        + "\u0005\u001e\u0000\u0000\u04ad\u04ba\u0003\u00a2Q\u0000\u04ae\u04b7\u0005"
        + "u\u0000\u0000\u04af\u04b4\u0003\u0080@\u0000\u04b0\u04b1\u0005q\u0000"
        + "\u0000\u04b1\u04b3\u0003\u0080@\u0000\u04b2\u04b0\u0001\u0000\u0000\u0000"
        + "\u04b3\u04b6\u0001\u0000\u0000\u0000\u04b4\u04b2\u0001\u0000\u0000\u0000"
        + "\u04b4\u04b5\u0001\u0000\u0000\u0000\u04b5\u04b8\u0001\u0000\u0000\u0000"
        + "\u04b6\u04b4\u0001\u0000\u0000\u0000\u04b7\u04af\u0001\u0000\u0000\u0000"
        + "\u04b7\u04b8\u0001\u0000\u0000\u0000\u04b8\u04b9\u0001\u0000\u0000\u0000"
        + "\u04b9\u04bb\u0005v\u0000\u0000\u04ba\u04ae\u0001\u0000\u0000\u0000\u04ba"
        + "\u04bb\u0001\u0000\u0000\u0000\u04bb\u04c1\u0001\u0000\u0000\u0000\u04bc"
        + "\u04bd\u0005\u001a\u0000\u0000\u04bd\u04be\u0003\u0080@\u0000\u04be\u04bf"
        + "\u0005\u001b\u0000\u0000\u04bf\u04c1\u0001\u0000\u0000\u0000\u04c0\u04ac"
        + "\u0001\u0000\u0000\u0000\u04c0\u04bc\u0001\u0000\u0000\u0000\u04c1\u009d"
        + "\u0001\u0000\u0000\u0000\u04c2\u04ef\u0005x\u0000\u0000\u04c3\u04ef\u0005"
        + "z\u0000\u0000\u04c4\u04ef\u0005\u0019\u0000\u0000\u04c5\u04ef\u0005\u0001"
        + "\u0000\u0000\u04c6\u04ef\u0005\u0002\u0000\u0000\u04c7\u04ef\u0005\u0003"
        + "\u0000\u0000\u04c8\u04ef\u00050\u0000\u0000\u04c9\u04ef\u00051\u0000\u0000"
        + "\u04ca\u04cb\u0005u\u0000\u0000\u04cb\u04cc\u0003\u0080@\u0000\u04cc\u04cd"
        + "\u0005v\u0000\u0000\u04cd\u04ef\u0001\u0000\u0000\u0000\u04ce\u04ef\u0003"
        + "\u0012\t\u0000\u04cf\u04ef\u0005y\u0000\u0000\u04d0\u04dc\u0005\u001a"
        + "\u0000\u0000\u04d1\u04d6\u0003\u0080@\u0000\u04d2\u04d3\u0005q\u0000\u0000"
        + "\u04d3\u04d5\u0003\u0080@\u0000\u04d4\u04d2\u0001\u0000\u0000\u0000\u04d5"
        + "\u04d8\u0001\u0000\u0000\u0000\u04d6\u04d4\u0001\u0000\u0000\u0000\u04d6"
        + "\u04d7\u0001\u0000\u0000\u0000\u04d7\u04da\u0001\u0000\u0000\u0000\u04d8"
        + "\u04d6\u0001\u0000\u0000\u0000\u04d9\u04db\u0005q\u0000\u0000\u04da\u04d9"
        + "\u0001\u0000\u0000\u0000\u04da\u04db\u0001\u0000\u0000\u0000\u04db\u04dd"
        + "\u0001\u0000\u0000\u0000\u04dc\u04d1\u0001\u0000\u0000\u0000\u04dc\u04dd"
        + "\u0001\u0000\u0000\u0000\u04dd\u04de\u0001\u0000\u0000\u0000\u04de\u04ef"
        + "\u0005\u001b\u0000\u0000\u04df\u04eb\u0005\u001c\u0000\u0000\u04e0\u04e5"
        + "\u0003\u00a0P\u0000\u04e1\u04e2\u0005q\u0000\u0000\u04e2\u04e4\u0003\u00a0"
        + "P\u0000\u04e3\u04e1\u0001\u0000\u0000\u0000\u04e4\u04e7\u0001\u0000\u0000"
        + "\u0000\u04e5\u04e3\u0001\u0000\u0000\u0000\u04e5\u04e6\u0001\u0000\u0000"
        + "\u0000\u04e6\u04e9\u0001\u0000\u0000\u0000\u04e7\u04e5\u0001\u0000\u0000"
        + "\u0000\u04e8\u04ea\u0005q\u0000\u0000\u04e9\u04e8\u0001\u0000\u0000\u0000"
        + "\u04e9\u04ea\u0001\u0000\u0000\u0000\u04ea\u04ec\u0001\u0000\u0000\u0000"
        + "\u04eb\u04e0\u0001\u0000\u0000\u0000\u04eb\u04ec\u0001\u0000\u0000\u0000"
        + "\u04ec\u04ed\u0001\u0000\u0000\u0000\u04ed\u04ef\u0005\u001d\u0000\u0000"
        + "\u04ee\u04c2\u0001\u0000\u0000\u0000\u04ee\u04c3\u0001\u0000\u0000\u0000"
        + "\u04ee\u04c4\u0001\u0000\u0000\u0000\u04ee\u04c5\u0001\u0000\u0000\u0000"
        + "\u04ee\u04c6\u0001\u0000\u0000\u0000\u04ee\u04c7\u0001\u0000\u0000\u0000"
        + "\u04ee\u04c8\u0001\u0000\u0000\u0000\u04ee\u04c9\u0001\u0000\u0000\u0000"
        + "\u04ee\u04ca\u0001\u0000\u0000\u0000\u04ee\u04ce\u0001\u0000\u0000\u0000"
        + "\u04ee\u04cf\u0001\u0000\u0000\u0000\u04ee\u04d0\u0001\u0000\u0000\u0000"
        + "\u04ee\u04df\u0001\u0000\u0000\u0000\u04ef\u009f\u0001\u0000\u0000\u0000"
        + "\u04f0\u04f1\u0003\u00a2Q\u0000\u04f1\u04f2\u0005r\u0000\u0000\u04f2\u04f3"
        + "\u0003\u0080@\u0000\u04f3\u00a1\u0001\u0000\u0000\u0000\u04f4\u04f5\u0007"
        + "\u000f\u0000\u0000\u04f5\u00a3\u0001\u0000\u0000\u0000\u04f6\u04f7\u0005"
        + "z\u0000\u0000\u04f7\u00a5\u0001\u0000\u0000\u0000\u04f8\u04f9\u0005x\u0000"
        + "\u0000\u04f9\u00a7\u0001\u0000\u0000\u0000\u00b0\u00a9\u00ae\u00b3\u00b6"
        + "\u00ba\u00bf\u00c1\u00cf\u00d9\u00dc\u00e9\u00f3\u00f6\u0101\u0103\u010a"
        + "\u010e\u0117\u0122\u0125\u0128\u012d\u0132\u0141\u0144\u014b\u014f\u0160"
        + "\u016d\u017c\u017e\u0181\u0184\u018c\u018e\u0198\u019a\u019d\u01a2\u01b5"
        + "\u01b8\u01bf\u01c4\u01c8\u01cd\u01d2\u01d7\u01d9\u01dd\u01e3\u01e7\u01eb"
        + "\u01ee\u01f2\u01f7\u01fc\u01ff\u0201\u0205\u0208\u020c\u020f\u0214\u0217"
        + "\u021c\u021f\u0221\u022c\u0230\u0233\u0237\u023b\u023e\u0245\u024c\u024f"
        + "\u0257\u025a\u0262\u0266\u026a\u026c\u026e\u0270\u0273\u027a\u027e\u0285"
        + "\u0289\u028f\u029a\u029d\u02a0\u02a8\u02aa\u02b4\u02bc\u02bf\u02c8\u02cd"
        + "\u02d9\u02df\u02ed\u02f2\u0304\u030c\u030e\u0318\u0322\u0330\u0335\u0339"
        + "\u033d\u0348\u035a\u035c\u0361\u0365\u0371\u0381\u038b\u038e\u0391\u0395"
        + "\u0399\u03a1\u03a3\u03a6\u03ab\u03b3\u03b5\u03b7\u03bf\u03c1\u03cd\u03d1"
        + "\u03d4\u03d7\u03e2\u03ea\u03f1\u03f4\u0407\u0415\u0421\u042f\u0431\u0435"
        + "\u0439\u043e\u0447\u0449\u0453\u045b\u0461\u0467\u046e\u0476\u047e\u0486"
        + "\u048e\u0495\u049a\u04a3\u04a9\u04b4\u04b7\u04ba\u04c0\u04d6\u04da\u04dc"
        + "\u04e5\u04e9\u04eb\u04ee";
    public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
