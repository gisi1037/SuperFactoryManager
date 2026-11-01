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
    public static final int SETCIRCUIT = 1, WAIT = 2, TRANSFER = 3, TRANSFERF = 4, INTO = 5, FUNCTION = 6, RETURNS = 7,
        RETURN = 8, REF = 9, LET = 10, SET = 11, STATE = 12, RISING = 13, FALLING = 14, PRINT = 15, FOR = 16,
        WHILE = 17, STEP = 18, VARIABLE = 19, LBRACKET = 20, RBRACKET = 21, LBRACE = 22, RBRACE = 23, DOT = 24,
        SEMICOLON = 25, PERCENT = 26, BIT_AND = 27, BIT_OR = 28, BIT_XOR = 29, BIT_NOT = 30, SHIFT_LEFT = 31,
        SHIFT_RIGHT = 32, SHIFT_UNSIGNED_RIGHT = 33, IF = 34, THEN = 35, ELSE = 36, HAS = 37, OVERALL = 38, SOME = 39,
        ONE = 40, LONE = 41, TRUE = 42, FALSE = 43, NOT = 44, AND = 45, OR = 46, GT = 47, GT_SYMBOL = 48, LT = 49,
        LT_SYMBOL = 50, EQ = 51, EQ_SYMBOL = 52, EQ_ALIAS = 53, NE_SYMBOL = 54, LE = 55, LE_SYMBOL = 56, GE = 57,
        GE_SYMBOL = 58, FROM = 59, TO = 60, INPUT = 61, OUTPUT = 62, WHERE = 63, SLOTS = 64, SLOT = 65, RETAIN = 66,
        EACH = 67, EXCEPT = 68, FORGET = 69, EMPTY = 70, IN = 71, WITHOUT = 72, WITH = 73, TAG = 74, HASHTAG = 75,
        ROUND = 76, ROBIN = 77, BY = 78, LABEL = 79, BLOCK = 80, TOP = 81, BOTTOM = 82, NORTH = 83, EAST = 84,
        SOUTH = 85, WEST = 86, SIDE = 87, LEFT = 88, RIGHT = 89, FRONT = 90, BACK = 91, NULL = 92, TICKS = 93,
        TICK = 94, SECONDS = 95, SECOND = 96, GLOBAL = 97, PLUS = 98, REDSTONE = 99, LEVEL = 100, LEVELS = 101,
        PULSE = 102, DO = 103, END = 104, NAME = 105, EVERY = 106, COMMA = 107, COLON = 108, SLASH = 109, DASH = 110,
        LPAREN = 111, RPAREN = 112, NUMBER_WITH_G_SUFFIX = 113, NUMBER = 114, IDENTIFIER = 115, STRING = 116,
        LINE_COMMENT = 117, WS = 118, UNUSED = 119;
    public static final int RULE_program = 0, RULE_functionDefinition = 1, RULE_functionName = 2,
        RULE_functionParameter = 3, RULE_variableName = 4, RULE_dataType = 5, RULE_typeField = 6, RULE_functionCall = 7,
        RULE_functionArgument = 8, RULE_returnStatement = 9, RULE_functionCallStatement = 10, RULE_name = 11,
        RULE_trigger = 12, RULE_interval = 13, RULE_block = 14, RULE_statement = 15, RULE_redstoneOutputStatement = 16,
        RULE_forgetStatement = 17, RULE_inputStatement = 18, RULE_outputStatement = 19, RULE_inputResourceLimits = 20,
        RULE_outputResourceLimits = 21, RULE_resourceLimitList = 22, RULE_resourceLimit = 23, RULE_limit = 24,
        RULE_quantity = 25, RULE_retention = 26, RULE_resourceExclusion = 27, RULE_resourceId = 28,
        RULE_resourceIdList = 29, RULE_resourceIdDisjunction = 30, RULE_with = 31, RULE_withClause = 32,
        RULE_tagMatcher = 33, RULE_sidequalifier = 34, RULE_side = 35, RULE_slotqualifier = 36, RULE_rangeset = 37,
        RULE_range = 38, RULE_ifStatement = 39, RULE_forStatement = 40, RULE_whileStatement = 41, RULE_boolexpr = 42,
        RULE_comparisonOp = 43, RULE_setOp = 44, RULE_labelAccess = 45, RULE_roundrobin = 46, RULE_label = 47,
        RULE_emptyslots = 48, RULE_identifier = 49, RULE_stateDeclaration = 50, RULE_dataDeclaration = 51,
        RULE_dataAssignment = 52, RULE_circuitStatement = 53, RULE_waitStatement = 54, RULE_printStatement = 55,
        RULE_transferStatement = 56, RULE_dataTarget = 57, RULE_dataExpression = 58, RULE_dataOr = 59,
        RULE_dataAnd = 60, RULE_dataNot = 61, RULE_dataComparison = 62, RULE_dataBitOr = 63, RULE_dataBitXor = 64,
        RULE_dataBitAnd = 65, RULE_dataShift = 66, RULE_dataAdd = 67, RULE_dataMultiply = 68,
        RULE_multiplyOperator = 69, RULE_dataSign = 70, RULE_dataPostfix = 71, RULE_dataSuffix = 72,
        RULE_dataPrimary = 73, RULE_dataField = 74, RULE_dataName = 75, RULE_string = 76, RULE_number = 77;

    private static String[] makeRuleNames() {
        return new String[] { "program", "functionDefinition", "functionName", "functionParameter", "variableName",
            "dataType", "typeField", "functionCall", "functionArgument", "returnStatement", "functionCallStatement",
            "name", "trigger", "interval", "block", "statement", "redstoneOutputStatement", "forgetStatement",
            "inputStatement", "outputStatement", "inputResourceLimits", "outputResourceLimits", "resourceLimitList",
            "resourceLimit", "limit", "quantity", "retention", "resourceExclusion", "resourceId", "resourceIdList",
            "resourceIdDisjunction", "with", "withClause", "tagMatcher", "sidequalifier", "side", "slotqualifier",
            "rangeset", "range", "ifStatement", "forStatement", "whileStatement", "boolexpr", "comparisonOp", "setOp",
            "labelAccess", "roundrobin", "label", "emptyslots", "identifier", "stateDeclaration", "dataDeclaration",
            "dataAssignment", "circuitStatement", "waitStatement", "printStatement", "transferStatement", "dataTarget",
            "dataExpression", "dataOr", "dataAnd", "dataNot", "dataComparison", "dataBitOr", "dataBitXor", "dataBitAnd",
            "dataShift", "dataAdd", "dataMultiply", "multiplyOperator", "dataSign", "dataPostfix", "dataSuffix",
            "dataPrimary", "dataField", "dataName", "string", "number" };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, "'['", "']'", "'{'", "'}'", "'.'", "';'", "'%'", "'&'", "'|'", "'^'", "'~'",
            "'<<'", "'>>'", "'>>>'", null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            "'>'", null, "'<'", null, "'='", "'=='", "'!='", null, "'<='", null, "'>='", null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, "'#'", null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, "','", "':'", "'/'", "'-'", "'('", "')'" };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[] { null, "SETCIRCUIT", "WAIT", "TRANSFER", "TRANSFERF", "INTO", "FUNCTION", "RETURNS",
            "RETURN", "REF", "LET", "SET", "STATE", "RISING", "FALLING", "PRINT", "FOR", "WHILE", "STEP", "VARIABLE",
            "LBRACKET", "RBRACKET", "LBRACE", "RBRACE", "DOT", "SEMICOLON", "PERCENT", "BIT_AND", "BIT_OR", "BIT_XOR",
            "BIT_NOT", "SHIFT_LEFT", "SHIFT_RIGHT", "SHIFT_UNSIGNED_RIGHT", "IF", "THEN", "ELSE", "HAS", "OVERALL",
            "SOME", "ONE", "LONE", "TRUE", "FALSE", "NOT", "AND", "OR", "GT", "GT_SYMBOL", "LT", "LT_SYMBOL", "EQ",
            "EQ_SYMBOL", "EQ_ALIAS", "NE_SYMBOL", "LE", "LE_SYMBOL", "GE", "GE_SYMBOL", "FROM", "TO", "INPUT", "OUTPUT",
            "WHERE", "SLOTS", "SLOT", "RETAIN", "EACH", "EXCEPT", "FORGET", "EMPTY", "IN", "WITHOUT", "WITH", "TAG",
            "HASHTAG", "ROUND", "ROBIN", "BY", "LABEL", "BLOCK", "TOP", "BOTTOM", "NORTH", "EAST", "SOUTH", "WEST",
            "SIDE", "LEFT", "RIGHT", "FRONT", "BACK", "NULL", "TICKS", "TICK", "SECONDS", "SECOND", "GLOBAL", "PLUS",
            "REDSTONE", "LEVEL", "LEVELS", "PULSE", "DO", "END", "NAME", "EVERY", "COMMA", "COLON", "SLASH", "DASH",
            "LPAREN", "RPAREN", "NUMBER_WITH_G_SUFFIX", "NUMBER", "IDENTIFIER", "STRING", "LINE_COMMENT", "WS",
            "UNUSED" };
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
                setState(157);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
                    case 1: {
                        setState(156);
                        name();
                    }
                        break;
                }
                setState(168);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(161);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case STATE: {
                                        setState(159);
                                        stateDeclaration();
                                    }
                                        break;
                                    case LET: {
                                        setState(160);
                                        dataDeclaration();
                                    }
                                        break;
                                    default:
                                        throw new NoViableAltException(this);
                                }
                                setState(164);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                if (_la == SEMICOLON) {
                                    {
                                        setState(163);
                                        match(SEMICOLON);
                                    }
                                }

                            }
                        }
                    }
                    setState(170);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
                }
                setState(175);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -393220559639937538L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 2260595906705407L) != 0)) {
                    {
                        setState(173);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 4, _ctx)) {
                            case 1: {
                                setState(171);
                                functionDefinition();
                            }
                                break;
                            case 2: {
                                setState(172);
                                trigger();
                            }
                                break;
                        }
                    }
                    setState(177);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(178);
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
        enterRule(_localctx, 2, RULE_functionDefinition);
        int _la;
        try {
            setState(230);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 12, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(180);
                    match(FUNCTION);
                    setState(181);
                    functionName();
                    setState(182);
                    match(LPAREN);
                    setState(191);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -393220559639412738L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 2260595906705407L) != 0)) {
                        {
                            setState(183);
                            functionParameter();
                            setState(188);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(184);
                                        match(COMMA);
                                        setState(185);
                                        functionParameter();
                                    }
                                }
                                setState(190);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                        }
                    }

                    setState(193);
                    match(RPAREN);
                    setState(194);
                    match(RETURNS);
                    setState(195);
                    dataType();
                    setState(204);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(196);
                            match(DO);
                            setState(197);
                            block();
                            setState(198);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(200);
                            match(LBRACE);
                            setState(201);
                            block();
                            setState(202);
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
                    setState(206);
                    dataType();
                    setState(207);
                    functionName();
                    setState(208);
                    match(LPAREN);
                    setState(217);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -393220559639412738L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 2260595906705407L) != 0)) {
                        {
                            setState(209);
                            functionParameter();
                            setState(214);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(210);
                                        match(COMMA);
                                        setState(211);
                                        functionParameter();
                                    }
                                }
                                setState(216);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                        }
                    }

                    setState(219);
                    match(RPAREN);
                    setState(228);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case THEN:
                        case DO: {
                            setState(220);
                            _la = _input.LA(1);
                            if (!(_la == THEN || _la == DO)) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                            setState(221);
                            block();
                            setState(222);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(224);
                            match(LBRACE);
                            setState(225);
                            block();
                            setState(226);
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
        enterRule(_localctx, 4, RULE_functionName);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(232);
                dataName();
                setState(237);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == DOT) {
                    {
                        {
                            setState(233);
                            match(DOT);
                            setState(234);
                            dataName();
                        }
                    }
                    setState(239);
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
        enterRule(_localctx, 6, RULE_functionParameter);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(241);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == REF) {
                    {
                        setState(240);
                        match(REF);
                    }
                }

                setState(250);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 15, _ctx)) {
                    case 1: {
                        setState(243);
                        variableName();
                        setState(244);
                        match(COLON);
                        setState(245);
                        dataType();
                    }
                        break;
                    case 2: {
                        setState(247);
                        dataType();
                        setState(248);
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
        enterRule(_localctx, 8, RULE_variableName);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(252);
                _la = _input.LA(1);
                if (!(_la == VARIABLE || _la == IDENTIFIER)) {
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

        public List<TerminalNode> LBRACKET() {
            return getTokens(SFMLParser.LBRACKET);
        }

        public TerminalNode LBRACKET(int i) {
            return getToken(SFMLParser.LBRACKET, i);
        }

        public List<TerminalNode> NUMBER() {
            return getTokens(SFMLParser.NUMBER);
        }

        public TerminalNode NUMBER(int i) {
            return getToken(SFMLParser.NUMBER, i);
        }

        public List<TerminalNode> RBRACKET() {
            return getTokens(SFMLParser.RBRACKET);
        }

        public TerminalNode RBRACKET(int i) {
            return getToken(SFMLParser.RBRACKET, i);
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
        enterRule(_localctx, 10, RULE_dataType);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(267);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
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
                        setState(254);
                        dataName();
                    }
                        break;
                    case LBRACE: {
                        setState(255);
                        match(LBRACE);
                        setState(264);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -393220559644131842L) != 0)
                            || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 2260595906705407L) != 0)) {
                            {
                                setState(256);
                                typeField();
                                setState(261);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(257);
                                            match(COMMA);
                                            setState(258);
                                            typeField();
                                        }
                                    }
                                    setState(263);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                        }

                        setState(266);
                        match(RBRACE);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(274);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACKET) {
                    {
                        {
                            setState(269);
                            match(LBRACKET);
                            setState(270);
                            match(NUMBER);
                            setState(271);
                            match(RBRACKET);
                        }
                    }
                    setState(276);
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
        enterRule(_localctx, 12, RULE_typeField);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(277);
                dataName();
                setState(278);
                match(COLON);
                setState(279);
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
        enterRule(_localctx, 14, RULE_functionCall);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(281);
                functionName();
                setState(282);
                match(LPAREN);
                setState(291);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -393220558564622338L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 8101201673451519L) != 0)) {
                    {
                        setState(283);
                        functionArgument();
                        setState(288);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(284);
                                    match(COMMA);
                                    setState(285);
                                    functionArgument();
                                }
                            }
                            setState(290);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(293);
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
        enterRule(_localctx, 16, RULE_functionArgument);
        try {
            setState(298);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case REF:
                    enterOuterAlt(_localctx, 1); {
                    setState(295);
                    match(REF);
                    setState(296);
                    dataTarget();
                }
                    break;
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
                    setState(297);
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
        enterRule(_localctx, 18, RULE_returnStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(300);
                match(RETURN);
                setState(302);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 23, _ctx)) {
                    case 1: {
                        setState(301);
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
        enterRule(_localctx, 20, RULE_functionCallStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(304);
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
        enterRule(_localctx, 22, RULE_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(306);
                match(NAME);
                setState(307);
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
        enterRule(_localctx, 24, RULE_trigger);
        int _la;
        try {
            setState(349);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 27, _ctx)) {
                case 1:
                    _localctx = new TimerTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(309);
                    match(EVERY);
                    setState(310);
                    interval();
                    setState(319);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(311);
                            match(DO);
                            setState(312);
                            block();
                            setState(313);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(315);
                            match(LBRACE);
                            setState(316);
                            block();
                            setState(317);
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
                    setState(321);
                    match(EVERY);
                    setState(322);
                    match(REDSTONE);
                    setState(323);
                    match(PULSE);
                    setState(332);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(324);
                            match(DO);
                            setState(325);
                            block();
                            setState(326);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(328);
                            match(LBRACE);
                            setState(329);
                            block();
                            setState(330);
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
                    setState(334);
                    match(EVERY);
                    setState(335);
                    match(REDSTONE);
                    setState(336);
                    _la = _input.LA(1);
                    if (!(_la == RISING || _la == FALLING || _la == PULSE)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(337);
                    match(FROM);
                    setState(338);
                    labelAccess();
                    setState(347);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(339);
                            match(DO);
                            setState(340);
                            block();
                            setState(341);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(343);
                            match(LBRACE);
                            setState(344);
                            block();
                            setState(345);
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
        enterRule(_localctx, 26, RULE_interval);
        int _la;
        try {
            setState(368);
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
                    setState(352);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == NUMBER) {
                        {
                            setState(351);
                            match(NUMBER);
                        }
                    }

                    setState(355);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == GLOBAL) {
                        {
                            setState(354);
                            match(GLOBAL);
                        }
                    }

                    setState(359);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(357);
                            match(PLUS);
                            setState(358);
                            match(NUMBER);
                        }
                    }

                    setState(361);
                    _la = _input.LA(1);
                    if (!(((((_la - 93)) & ~0x3f) == 0 && ((1L << (_la - 93)) & 15L) != 0))) {
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
                    setState(362);
                    match(NUMBER_WITH_G_SUFFIX);
                    setState(365);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(363);
                            match(PLUS);
                            setState(364);
                            match(NUMBER);
                        }
                    }

                    setState(367);
                    _la = _input.LA(1);
                    if (!(((((_la - 93)) & ~0x3f) == 0 && ((1L << (_la - 93)) & 15L) != 0))) {
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
        enterRule(_localctx, 28, RULE_block);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(373);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 33, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(370);
                                statement();
                            }
                        }
                    }
                    setState(375);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 33, _ctx);
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
        enterRule(_localctx, 30, RULE_statement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(391);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 34, _ctx)) {
                    case 1: {
                        setState(376);
                        circuitStatement();
                    }
                        break;
                    case 2: {
                        setState(377);
                        waitStatement();
                    }
                        break;
                    case 3: {
                        setState(378);
                        dataDeclaration();
                    }
                        break;
                    case 4: {
                        setState(379);
                        dataAssignment();
                    }
                        break;
                    case 5: {
                        setState(380);
                        printStatement();
                    }
                        break;
                    case 6: {
                        setState(381);
                        transferStatement();
                    }
                        break;
                    case 7: {
                        setState(382);
                        returnStatement();
                    }
                        break;
                    case 8: {
                        setState(383);
                        inputStatement();
                    }
                        break;
                    case 9: {
                        setState(384);
                        redstoneOutputStatement();
                    }
                        break;
                    case 10: {
                        setState(385);
                        outputStatement();
                    }
                        break;
                    case 11: {
                        setState(386);
                        ifStatement();
                    }
                        break;
                    case 12: {
                        setState(387);
                        forStatement();
                    }
                        break;
                    case 13: {
                        setState(388);
                        whileStatement();
                    }
                        break;
                    case 14: {
                        setState(389);
                        forgetStatement();
                    }
                        break;
                    case 15: {
                        setState(390);
                        functionCallStatement();
                    }
                        break;
                }
                setState(394);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SEMICOLON) {
                    {
                        setState(393);
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
        enterRule(_localctx, 32, RULE_redstoneOutputStatement);
        int _la;
        try {
            setState(424);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(396);
                    match(OUTPUT);
                    setState(398);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(397);
                            quantity();
                        }
                    }

                    setState(400);
                    match(REDSTONE);
                    setState(401);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(403);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(402);
                            quantity();
                        }
                    }

                    setState(405);
                    match(TO);
                    setState(407);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 38, _ctx)) {
                        case 1: {
                            setState(406);
                            match(EACH);
                        }
                            break;
                    }
                    setState(409);
                    labelAccess();
                }
                    break;
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(410);
                    match(TO);
                    setState(412);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 39, _ctx)) {
                        case 1: {
                            setState(411);
                            match(EACH);
                        }
                            break;
                    }
                    setState(414);
                    labelAccess();
                    setState(415);
                    match(OUTPUT);
                    setState(417);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(416);
                            quantity();
                        }
                    }

                    setState(419);
                    match(REDSTONE);
                    setState(420);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(422);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(421);
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
        enterRule(_localctx, 34, RULE_forgetStatement);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(426);
                match(FORGET);
                setState(428);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 43, _ctx)) {
                    case 1: {
                        setState(427);
                        label();
                    }
                        break;
                }
                setState(434);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 44, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(430);
                                match(COMMA);
                                setState(431);
                                label();
                            }
                        }
                    }
                    setState(436);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 44, _ctx);
                }
                setState(438);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(437);
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
        enterRule(_localctx, 36, RULE_inputStatement);
        int _la;
        try {
            setState(464);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case INPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(440);
                    match(INPUT);
                    setState(442);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 523774L) != 0)
                        || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & 2005573159715009L) != 0)) {
                        {
                            setState(441);
                            inputResourceLimits();
                        }
                    }

                    setState(445);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(444);
                            resourceExclusion();
                        }
                    }

                    setState(447);
                    match(FROM);
                    setState(449);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 48, _ctx)) {
                        case 1: {
                            setState(448);
                            match(EACH);
                        }
                            break;
                    }
                    setState(451);
                    labelAccess();
                }
                    break;
                case FROM:
                    enterOuterAlt(_localctx, 2); {
                    setState(452);
                    match(FROM);
                    setState(454);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 49, _ctx)) {
                        case 1: {
                            setState(453);
                            match(EACH);
                        }
                            break;
                    }
                    setState(456);
                    labelAccess();
                    setState(457);
                    match(INPUT);
                    setState(459);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 50, _ctx)) {
                        case 1: {
                            setState(458);
                            inputResourceLimits();
                        }
                            break;
                    }
                    setState(462);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 51, _ctx)) {
                        case 1: {
                            setState(461);
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
        enterRule(_localctx, 38, RULE_outputStatement);
        int _la;
        try {
            setState(496);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(466);
                    match(OUTPUT);
                    setState(468);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 523774L) != 0)
                        || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & 2005573159715009L) != 0)) {
                        {
                            setState(467);
                            outputResourceLimits();
                        }
                    }

                    setState(471);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(470);
                            resourceExclusion();
                        }
                    }

                    setState(473);
                    match(TO);
                    setState(475);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 55, _ctx)) {
                        case 1: {
                            setState(474);
                            emptyslots();
                        }
                            break;
                    }
                    setState(478);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 56, _ctx)) {
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
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(481);
                    match(TO);
                    setState(483);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 57, _ctx)) {
                        case 1: {
                            setState(482);
                            emptyslots();
                        }
                            break;
                    }
                    setState(486);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 58, _ctx)) {
                        case 1: {
                            setState(485);
                            match(EACH);
                        }
                            break;
                    }
                    setState(488);
                    labelAccess();
                    setState(489);
                    match(OUTPUT);
                    setState(491);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 59, _ctx)) {
                        case 1: {
                            setState(490);
                            outputResourceLimits();
                        }
                            break;
                    }
                    setState(494);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 60, _ctx)) {
                        case 1: {
                            setState(493);
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
        enterRule(_localctx, 40, RULE_inputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(498);
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
        enterRule(_localctx, 42, RULE_outputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(500);
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
        enterRule(_localctx, 44, RULE_resourceLimitList);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(502);
                resourceLimit();
                setState(507);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 62, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(503);
                                match(COMMA);
                                setState(504);
                                resourceLimit();
                            }
                        }
                    }
                    setState(509);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 62, _ctx);
                }
                setState(511);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(510);
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
        enterRule(_localctx, 46, RULE_resourceLimit);
        int _la;
        try {
            setState(525);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 67, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(514);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & 316659348799489L) != 0)) {
                        {
                            setState(513);
                            limit();
                        }
                    }

                    setState(516);
                    resourceIdDisjunction();
                    setState(518);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 65, _ctx)) {
                        case 1: {
                            setState(517);
                            with();
                        }
                            break;
                    }
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(520);
                    limit();
                    setState(522);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 66, _ctx)) {
                        case 1: {
                            setState(521);
                            with();
                        }
                            break;
                    }
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(524);
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
        enterRule(_localctx, 48, RULE_limit);
        try {
            setState(532);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 68, _ctx)) {
                case 1:
                    _localctx = new QuantityRetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(527);
                    quantity();
                    setState(528);
                    retention();
                }
                    break;
                case 2:
                    _localctx = new RetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(530);
                    retention();
                }
                    break;
                case 3:
                    _localctx = new QuantityLimitContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(531);
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
        enterRule(_localctx, 50, RULE_quantity);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(539);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(534);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(535);
                        match(LPAREN);
                        setState(536);
                        dataExpression();
                        setState(537);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(542);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 70, _ctx)) {
                    case 1: {
                        setState(541);
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
        enterRule(_localctx, 52, RULE_retention);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(544);
                match(RETAIN);
                setState(550);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(545);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(546);
                        match(LPAREN);
                        setState(547);
                        dataExpression();
                        setState(548);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(553);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 72, _ctx)) {
                    case 1: {
                        setState(552);
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
        enterRule(_localctx, 54, RULE_resourceExclusion);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(555);
                match(EXCEPT);
                setState(556);
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
        enterRule(_localctx, 56, RULE_resourceId);
        try {
            setState(578);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
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
                        setState(558);
                        identifier();
                    }
                    setState(575);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 78, _ctx)) {
                        case 1: {
                            setState(559);
                            match(COLON);
                            setState(561);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 73, _ctx)) {
                                case 1: {
                                    setState(560);
                                    identifier();
                                }
                                    break;
                            }
                            setState(573);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 77, _ctx)) {
                                case 1: {
                                    setState(563);
                                    match(COLON);
                                    setState(565);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 74, _ctx)) {
                                        case 1: {
                                            setState(564);
                                            identifier();
                                        }
                                            break;
                                    }
                                    setState(571);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 76, _ctx)) {
                                        case 1: {
                                            setState(567);
                                            match(COLON);
                                            setState(569);
                                            _errHandler.sync(this);
                                            switch (getInterpreter().adaptivePredict(_input, 75, _ctx)) {
                                                case 1: {
                                                    setState(568);
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
                    setState(577);
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
        enterRule(_localctx, 58, RULE_resourceIdList);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(580);
                resourceId();
                setState(585);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 80, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(581);
                                match(COMMA);
                                setState(582);
                                resourceId();
                            }
                        }
                    }
                    setState(587);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 80, _ctx);
                }
                setState(589);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 81, _ctx)) {
                    case 1: {
                        setState(588);
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
        enterRule(_localctx, 60, RULE_resourceIdDisjunction);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(591);
                resourceId();
                setState(596);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 82, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(592);
                                match(OR);
                                setState(593);
                                resourceId();
                            }
                        }
                    }
                    setState(598);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 82, _ctx);
                }
                setState(600);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 83, _ctx)) {
                    case 1: {
                        setState(599);
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
        enterRule(_localctx, 62, RULE_with);
        try {
            setState(606);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case WITH:
                    enterOuterAlt(_localctx, 1); {
                    setState(602);
                    match(WITH);
                    setState(603);
                    withClause(0);
                }
                    break;
                case WITHOUT:
                    enterOuterAlt(_localctx, 2); {
                    setState(604);
                    match(WITHOUT);
                    setState(605);
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
        int _startState = 64;
        enterRecursionRule(_localctx, 64, RULE_withClause, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(623);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case LPAREN: {
                        _localctx = new WithParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(609);
                        match(LPAREN);
                        setState(610);
                        withClause(0);
                        setState(611);
                        match(RPAREN);
                    }
                        break;
                    case NOT: {
                        _localctx = new WithNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(613);
                        match(NOT);
                        setState(614);
                        withClause(4);
                    }
                        break;
                    case TAG:
                    case HASHTAG: {
                        _localctx = new WithTagContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(620);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case TAG: {
                                setState(615);
                                match(TAG);
                                setState(617);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                if (_la == HASHTAG) {
                                    {
                                        setState(616);
                                        match(HASHTAG);
                                    }
                                }

                            }
                                break;
                            case HASHTAG: {
                                setState(619);
                                match(HASHTAG);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(622);
                        tagMatcher();
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(633);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 89, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(631);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 88, _ctx)) {
                                case 1: {
                                    _localctx = new WithConjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(625);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(626);
                                    match(AND);
                                    setState(627);
                                    withClause(4);
                                }
                                    break;
                                case 2: {
                                    _localctx = new WithDisjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(628);
                                    if (!(precpred(_ctx, 2)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                                    setState(629);
                                    match(OR);
                                    setState(630);
                                    withClause(3);
                                }
                                    break;
                            }
                        }
                    }
                    setState(635);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 89, _ctx);
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
        enterRule(_localctx, 66, RULE_tagMatcher);
        try {
            int _alt;
            setState(654);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 92, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(636);
                    identifier();
                    setState(637);
                    match(COLON);
                    setState(638);
                    identifier();
                    setState(643);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 90, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(639);
                                    match(SLASH);
                                    setState(640);
                                    identifier();
                                }
                            }
                        }
                        setState(645);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 90, _ctx);
                    }
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(646);
                    identifier();
                    setState(651);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 91, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(647);
                                    match(SLASH);
                                    setState(648);
                                    identifier();
                                }
                            }
                        }
                        setState(653);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 91, _ctx);
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
        enterRule(_localctx, 68, RULE_sidequalifier);
        int _la;
        try {
            setState(668);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case EACH:
                    _localctx = new EachSideContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(656);
                    match(EACH);
                    setState(657);
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
                    setState(658);
                    side();
                    setState(663);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(659);
                                match(COMMA);
                                setState(660);
                                side();
                            }
                        }
                        setState(665);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(666);
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
        enterRule(_localctx, 70, RULE_side);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(670);
                _la = _input.LA(1);
                if (!(((((_la - 81)) & ~0x3f) == 0 && ((1L << (_la - 81)) & 4031L) != 0))) {
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
        enterRule(_localctx, 72, RULE_slotqualifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(672);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(673);
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
        enterRule(_localctx, 74, RULE_rangeset);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(675);
                range();
                setState(680);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(676);
                            match(COMMA);
                            setState(677);
                            range();
                        }
                    }
                    setState(682);
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
        enterRule(_localctx, 76, RULE_range);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(683);
                number();
                setState(686);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == DASH) {
                    {
                        setState(684);
                        match(DASH);
                        setState(685);
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
        enterRule(_localctx, 78, RULE_ifStatement);
        int _la;
        try {
            int _alt;
            setState(733);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 101, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(688);
                    match(IF);
                    setState(689);
                    boolexpr(0);
                    setState(690);
                    match(THEN);
                    setState(691);
                    block();
                    setState(700);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 97, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(692);
                                    match(ELSE);
                                    setState(693);
                                    match(IF);
                                    setState(694);
                                    boolexpr(0);
                                    setState(695);
                                    match(THEN);
                                    setState(696);
                                    block();
                                }
                            }
                        }
                        setState(702);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 97, _ctx);
                    }
                    setState(705);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == ELSE) {
                        {
                            setState(703);
                            match(ELSE);
                            setState(704);
                            block();
                        }
                    }

                    setState(707);
                    match(END);
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(709);
                    match(IF);
                    setState(710);
                    boolexpr(0);
                    setState(711);
                    match(LBRACE);
                    setState(712);
                    block();
                    setState(713);
                    match(RBRACE);
                    setState(723);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 99, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(714);
                                    match(ELSE);
                                    setState(715);
                                    match(IF);
                                    setState(716);
                                    boolexpr(0);
                                    setState(717);
                                    match(LBRACE);
                                    setState(718);
                                    block();
                                    setState(719);
                                    match(RBRACE);
                                }
                            }
                        }
                        setState(725);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 99, _ctx);
                    }
                    setState(731);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 100, _ctx)) {
                        case 1: {
                            setState(726);
                            match(ELSE);
                            setState(727);
                            match(LBRACE);
                            setState(728);
                            block();
                            setState(729);
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
        enterRule(_localctx, 80, RULE_forStatement);
        int _la;
        try {
            setState(769);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 105, _ctx)) {
                case 1:
                    _localctx = new CountedForContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(735);
                    match(FOR);
                    setState(736);
                    variableName();
                    setState(737);
                    match(EQ_SYMBOL);
                    setState(738);
                    dataExpression();
                    setState(739);
                    match(TO);
                    setState(740);
                    dataExpression();
                    setState(743);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == STEP) {
                        {
                            setState(741);
                            match(STEP);
                            setState(742);
                            dataExpression();
                        }
                    }

                    setState(753);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(745);
                            match(DO);
                            setState(746);
                            block();
                            setState(747);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(749);
                            match(LBRACE);
                            setState(750);
                            block();
                            setState(751);
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
                    setState(755);
                    match(FOR);
                    setState(756);
                    variableName();
                    setState(757);
                    match(IN);
                    setState(758);
                    dataExpression();
                    setState(767);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case DO: {
                            setState(759);
                            match(DO);
                            setState(760);
                            block();
                            setState(761);
                            match(END);
                        }
                            break;
                        case LBRACE: {
                            setState(763);
                            match(LBRACE);
                            setState(764);
                            block();
                            setState(765);
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
        enterRule(_localctx, 82, RULE_whileStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(771);
                match(WHILE);
                setState(772);
                boolexpr(0);
                setState(781);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case DO: {
                        setState(773);
                        match(DO);
                        setState(774);
                        block();
                        setState(775);
                        match(END);
                    }
                        break;
                    case LBRACE: {
                        setState(777);
                        match(LBRACE);
                        setState(778);
                        block();
                        setState(779);
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
        int _startState = 84;
        enterRecursionRule(_localctx, 84, RULE_boolexpr, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(833);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 115, _ctx)) {
                    case 1: {
                        _localctx = new BooleanTrueContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(784);
                        match(TRUE);
                    }
                        break;
                    case 2: {
                        _localctx = new BooleanFalseContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(785);
                        match(FALSE);
                    }
                        break;
                    case 3: {
                        _localctx = new BooleanParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(786);
                        match(LPAREN);
                        setState(787);
                        boolexpr(0);
                        setState(788);
                        match(RPAREN);
                    }
                        break;
                    case 4: {
                        _localctx = new BooleanDataContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(790);
                        match(LPAREN);
                        setState(791);
                        dataExpression();
                        setState(792);
                        match(RPAREN);
                    }
                        break;
                    case 5: {
                        _localctx = new BooleanNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(794);
                        match(NOT);
                        setState(795);
                        boolexpr(5);
                    }
                        break;
                    case 6: {
                        _localctx = new BooleanHasContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(797);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 107, _ctx)) {
                            case 1: {
                                setState(796);
                                setOp();
                            }
                                break;
                        }
                        setState(799);
                        labelAccess();
                        setState(800);
                        match(HAS);
                        setState(801);
                        comparisonOp();
                        setState(807);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case NUMBER: {
                                setState(802);
                                number();
                            }
                                break;
                            case LPAREN: {
                                setState(803);
                                match(LPAREN);
                                setState(804);
                                dataExpression();
                                setState(805);
                                match(RPAREN);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(810);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 109, _ctx)) {
                            case 1: {
                                setState(809);
                                resourceIdDisjunction();
                            }
                                break;
                        }
                        setState(813);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 110, _ctx)) {
                            case 1: {
                                setState(812);
                                with();
                            }
                                break;
                        }
                        setState(817);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 111, _ctx)) {
                            case 1: {
                                setState(815);
                                match(EXCEPT);
                                setState(816);
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
                        setState(819);
                        match(REDSTONE);
                        setState(821);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 112, _ctx)) {
                            case 1: {
                                setState(820);
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
                        setState(831);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 114, _ctx)) {
                            case 1: {
                                setState(823);
                                comparisonOp();
                                setState(829);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case NUMBER: {
                                        setState(824);
                                        number();
                                    }
                                        break;
                                    case LPAREN: {
                                        setState(825);
                                        match(LPAREN);
                                        setState(826);
                                        dataExpression();
                                        setState(827);
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
                setState(843);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 117, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(841);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 116, _ctx)) {
                                case 1: {
                                    _localctx = new BooleanConjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(835);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(836);
                                    match(AND);
                                    setState(837);
                                    boolexpr(5);
                                }
                                    break;
                                case 2: {
                                    _localctx = new BooleanDisjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(838);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(839);
                                    match(OR);
                                    setState(840);
                                    boolexpr(4);
                                }
                                    break;
                            }
                        }
                    }
                    setState(845);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 117, _ctx);
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
        enterRule(_localctx, 86, RULE_comparisonOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(846);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 576320014815068160L) != 0))) {
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
        enterRule(_localctx, 88, RULE_setOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(848);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 4123168604160L) != 0) || _la == EACH || _la == EVERY)) {
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
        enterRule(_localctx, 90, RULE_labelAccess);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(850);
                label();
                setState(855);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(851);
                            match(COMMA);
                            setState(852);
                            label();
                        }
                    }
                    setState(857);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(859);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 119, _ctx)) {
                    case 1: {
                        setState(858);
                        roundrobin();
                    }
                        break;
                }
                setState(862);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 120, _ctx)) {
                    case 1: {
                        setState(861);
                        sidequalifier();
                    }
                        break;
                }
                setState(865);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 121, _ctx)) {
                    case 1: {
                        setState(864);
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
        enterRule(_localctx, 92, RULE_roundrobin);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(867);
                match(ROUND);
                setState(868);
                match(ROBIN);
                setState(869);
                match(BY);
                setState(870);
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
        enterRule(_localctx, 94, RULE_label);
        int _la;
        try {
            setState(894);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 125, _ctx)) {
                case 1:
                    _localctx = new RawLabelContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(872);
                    identifier();
                    setState(876);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == LBRACKET || _la == DOT) {
                        {
                            {
                                setState(873);
                                dataSuffix();
                            }
                        }
                        setState(878);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                }
                    break;
                case 2:
                    _localctx = new StringLabelContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(879);
                    string();
                }
                    break;
                case 3:
                    _localctx = new VariableLabelContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(880);
                    match(VARIABLE);
                    setState(884);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == LBRACKET || _la == DOT) {
                        {
                            {
                                setState(881);
                                dataSuffix();
                            }
                        }
                        setState(886);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                }
                    break;
                case 4:
                    _localctx = new CallLabelContext(_localctx);
                    enterOuterAlt(_localctx, 4); {
                    setState(887);
                    functionCall();
                    setState(891);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == LBRACKET || _la == DOT) {
                        {
                            {
                                setState(888);
                                dataSuffix();
                            }
                        }
                        setState(893);
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
        enterRule(_localctx, 96, RULE_emptyslots);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(896);
                match(EMPTY);
                setState(897);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(898);
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
        enterRule(_localctx, 98, RULE_identifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(900);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 523774L) != 0)
                    || ((((_la - 81)) & ~0x3f) == 0 && ((1L << (_la - 81)) & 17181820803L) != 0))) {
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
        enterRule(_localctx, 100, RULE_stateDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(902);
                match(STATE);
                setState(903);
                variableName();
                setState(904);
                match(EQ_SYMBOL);
                setState(905);
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
        enterRule(_localctx, 102, RULE_dataDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(907);
                match(LET);
                setState(908);
                variableName();
                setState(909);
                match(EQ_SYMBOL);
                setState(910);
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
        enterRule(_localctx, 104, RULE_dataAssignment);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(913);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SET) {
                    {
                        setState(912);
                        match(SET);
                    }
                }

                setState(915);
                dataTarget();
                setState(916);
                match(EQ_SYMBOL);
                setState(917);
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
        enterRule(_localctx, 106, RULE_circuitStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(919);
                match(SETCIRCUIT);
                setState(920);
                match(LPAREN);
                setState(921);
                dataExpression();
                setState(922);
                match(COMMA);
                setState(923);
                dataExpression();
                setState(924);
                match(RPAREN);
                setState(927);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 127, _ctx)) {
                    case 1: {
                        setState(925);
                        match(INTO);
                        setState(926);
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
        enterRule(_localctx, 108, RULE_waitStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(929);
                match(WAIT);
                setState(930);
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
        enterRule(_localctx, 110, RULE_printStatement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(932);
                match(PRINT);
                setState(933);
                match(LPAREN);
                setState(934);
                dataExpression();
                setState(939);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(935);
                            match(COMMA);
                            setState(936);
                            dataExpression();
                        }
                    }
                    setState(941);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(942);
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
        enterRule(_localctx, 112, RULE_transferStatement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(944);
                _la = _input.LA(1);
                if (!(_la == TRANSFER || _la == TRANSFERF)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(945);
                match(LPAREN);
                setState(946);
                dataExpression();
                setState(947);
                match(COMMA);
                setState(948);
                dataExpression();
                setState(955);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(949);
                        match(COMMA);
                        setState(950);
                        dataExpression();
                        setState(953);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == COMMA) {
                            {
                                setState(951);
                                match(COMMA);
                                setState(952);
                                dataExpression();
                            }
                        }

                    }
                }

                setState(957);
                match(RPAREN);
                setState(960);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 131, _ctx)) {
                    case 1: {
                        setState(958);
                        match(INTO);
                        setState(959);
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
        enterRule(_localctx, 114, RULE_dataTarget);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(962);
                variableName();
                setState(971);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACKET || _la == DOT) {
                    {
                        setState(969);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case DOT: {
                                setState(963);
                                match(DOT);
                                setState(964);
                                dataName();
                            }
                                break;
                            case LBRACKET: {
                                setState(965);
                                match(LBRACKET);
                                setState(966);
                                dataExpression();
                                setState(967);
                                match(RBRACKET);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                    }
                    setState(973);
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
        enterRule(_localctx, 116, RULE_dataExpression);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(974);
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
        enterRule(_localctx, 118, RULE_dataOr);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(976);
                dataAnd();
                setState(981);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 134, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(977);
                                match(OR);
                                setState(978);
                                dataAnd();
                            }
                        }
                    }
                    setState(983);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 134, _ctx);
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
        enterRule(_localctx, 120, RULE_dataAnd);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(984);
                dataNot();
                setState(989);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 135, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(985);
                                match(AND);
                                setState(986);
                                dataNot();
                            }
                        }
                    }
                    setState(991);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 135, _ctx);
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
        enterRule(_localctx, 122, RULE_dataNot);
        try {
            setState(995);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 136, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(992);
                    match(NOT);
                    setState(993);
                    dataNot();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(994);
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
        enterRule(_localctx, 124, RULE_dataComparison);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(997);
                dataBitOr();
                setState(1001);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 137, _ctx)) {
                    case 1: {
                        setState(998);
                        comparisonOp();
                        setState(999);
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
        enterRule(_localctx, 126, RULE_dataBitOr);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1003);
                dataBitXor();
                setState(1008);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == BIT_OR) {
                    {
                        {
                            setState(1004);
                            match(BIT_OR);
                            setState(1005);
                            dataBitXor();
                        }
                    }
                    setState(1010);
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
        enterRule(_localctx, 128, RULE_dataBitXor);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1011);
                dataBitAnd();
                setState(1016);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == BIT_XOR) {
                    {
                        {
                            setState(1012);
                            match(BIT_XOR);
                            setState(1013);
                            dataBitAnd();
                        }
                    }
                    setState(1018);
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
        enterRule(_localctx, 130, RULE_dataBitAnd);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1019);
                dataShift();
                setState(1024);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == BIT_AND) {
                    {
                        {
                            setState(1020);
                            match(BIT_AND);
                            setState(1021);
                            dataShift();
                        }
                    }
                    setState(1026);
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
        enterRule(_localctx, 132, RULE_dataShift);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1027);
                dataAdd();
                setState(1032);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 15032385536L) != 0)) {
                    {
                        {
                            setState(1028);
                            _la = _input.LA(1);
                            if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 15032385536L) != 0))) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                            setState(1029);
                            dataAdd();
                        }
                    }
                    setState(1034);
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
        enterRule(_localctx, 134, RULE_dataAdd);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1035);
                dataMultiply();
                setState(1040);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 142, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1036);
                                _la = _input.LA(1);
                                if (!(_la == PLUS || _la == DASH)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(1037);
                                dataMultiply();
                            }
                        }
                    }
                    setState(1042);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 142, _ctx);
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
        enterRule(_localctx, 136, RULE_dataMultiply);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1043);
                dataSign();
                setState(1052);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 144, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1047);
                                _errHandler.sync(this);
                                switch (getInterpreter().adaptivePredict(_input, 143, _ctx)) {
                                    case 1: {
                                        setState(1044);
                                        multiplyOperator();
                                    }
                                        break;
                                    case 2: {
                                        setState(1045);
                                        match(SLASH);
                                    }
                                        break;
                                    case 3: {
                                        setState(1046);
                                        match(PERCENT);
                                    }
                                        break;
                                }
                                setState(1049);
                                dataSign();
                            }
                        }
                    }
                    setState(1054);
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
        enterRule(_localctx, 138, RULE_multiplyOperator);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1055);
                if (!(_input.LT(1)
                    .getText()
                    .equals("*"))) throw new FailedPredicateException(this, "_input.LT(1).getText().equals(\"*\")");
                setState(1056);
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
        enterRule(_localctx, 140, RULE_dataSign);
        int _la;
        try {
            setState(1061);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 145, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(1058);
                    _la = _input.LA(1);
                    if (!(_la == BIT_NOT || _la == PLUS || _la == DASH)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(1059);
                    dataSign();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(1060);
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
        enterRule(_localctx, 142, RULE_dataPostfix);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1063);
                dataPrimary();
                setState(1067);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 146, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1064);
                                dataSuffix();
                            }
                        }
                    }
                    setState(1069);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 146, _ctx);
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
        enterRule(_localctx, 144, RULE_dataSuffix);
        int _la;
        try {
            setState(1090);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case DOT:
                    enterOuterAlt(_localctx, 1); {
                    setState(1070);
                    match(DOT);
                    setState(1071);
                    dataName();
                    setState(1084);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 149, _ctx)) {
                        case 1: {
                            setState(1072);
                            match(LPAREN);
                            setState(1081);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -393220558564622850L) != 0)
                                || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 8101201673451519L) != 0)) {
                                {
                                    setState(1073);
                                    dataExpression();
                                    setState(1078);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    while (_la == COMMA) {
                                        {
                                            {
                                                setState(1074);
                                                match(COMMA);
                                                setState(1075);
                                                dataExpression();
                                            }
                                        }
                                        setState(1080);
                                        _errHandler.sync(this);
                                        _la = _input.LA(1);
                                    }
                                }
                            }

                            setState(1083);
                            match(RPAREN);
                        }
                            break;
                    }
                }
                    break;
                case LBRACKET:
                    enterOuterAlt(_localctx, 2); {
                    setState(1086);
                    match(LBRACKET);
                    setState(1087);
                    dataExpression();
                    setState(1088);
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
        enterRule(_localctx, 146, RULE_dataPrimary);
        int _la;
        try {
            int _alt;
            setState(1133);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 157, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(1092);
                    match(NUMBER);
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(1093);
                    match(STRING);
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(1094);
                    match(VARIABLE);
                }
                    break;
                case 4:
                    enterOuterAlt(_localctx, 4); {
                    setState(1095);
                    match(TRUE);
                }
                    break;
                case 5:
                    enterOuterAlt(_localctx, 5); {
                    setState(1096);
                    match(FALSE);
                }
                    break;
                case 6:
                    enterOuterAlt(_localctx, 6); {
                    setState(1097);
                    match(LPAREN);
                    setState(1098);
                    dataExpression();
                    setState(1099);
                    match(RPAREN);
                }
                    break;
                case 7:
                    enterOuterAlt(_localctx, 7); {
                    setState(1101);
                    functionCall();
                }
                    break;
                case 8:
                    enterOuterAlt(_localctx, 8); {
                    setState(1102);
                    match(IDENTIFIER);
                }
                    break;
                case 9:
                    enterOuterAlt(_localctx, 9); {
                    setState(1103);
                    match(LBRACKET);
                    setState(1115);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -393220558564622850L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 8101201673451519L) != 0)) {
                        {
                            setState(1104);
                            dataExpression();
                            setState(1109);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 151, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(1105);
                                            match(COMMA);
                                            setState(1106);
                                            dataExpression();
                                        }
                                    }
                                }
                                setState(1111);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 151, _ctx);
                            }
                            setState(1113);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(1112);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(1117);
                    match(RBRACKET);
                }
                    break;
                case 10:
                    enterOuterAlt(_localctx, 10); {
                    setState(1118);
                    match(LBRACE);
                    setState(1130);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -393220559644131842L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 2260595906705407L) != 0)) {
                        {
                            setState(1119);
                            dataField();
                            setState(1124);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 154, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(1120);
                                            match(COMMA);
                                            setState(1121);
                                            dataField();
                                        }
                                    }
                                }
                                setState(1126);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 154, _ctx);
                            }
                            setState(1128);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(1127);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(1132);
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
        enterRule(_localctx, 148, RULE_dataField);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1135);
                dataName();
                setState(1136);
                match(COLON);
                setState(1137);
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
        enterRule(_localctx, 150, RULE_dataName);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1139);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & -393220559644131842L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 2260595906705407L) != 0))) {
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
        enterRule(_localctx, 152, RULE_string);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1141);
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
        enterRule(_localctx, 154, RULE_number);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1143);
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
            case 32:
                return withClause_sempred((WithClauseContext) _localctx, predIndex);
            case 42:
                return boolexpr_sempred((BoolexprContext) _localctx, predIndex);
            case 69:
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

    public static final String _serializedATN = "\u0004\u0001w\u047a\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"
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
        + "K\u0007K\u0002L\u0007L\u0002M\u0007M\u0001\u0000\u0003\u0000\u009e\b\u0000"
        + "\u0001\u0000\u0001\u0000\u0003\u0000\u00a2\b\u0000\u0001\u0000\u0003\u0000"
        + "\u00a5\b\u0000\u0005\u0000\u00a7\b\u0000\n\u0000\f\u0000\u00aa\t\u0000"
        + "\u0001\u0000\u0001\u0000\u0005\u0000\u00ae\b\u0000\n\u0000\f\u0000\u00b1"
        + "\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0001\u0005\u0001\u00bb\b\u0001\n\u0001\f\u0001"
        + "\u00be\t\u0001\u0003\u0001\u00c0\b\u0001\u0001\u0001\u0001\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00cd\b\u0001\u0001\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001\u00d5"
        + "\b\u0001\n\u0001\f\u0001\u00d8\t\u0001\u0003\u0001\u00da\b\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00e5\b\u0001\u0003\u0001\u00e7"
        + "\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002\u00ec\b\u0002"
        + "\n\u0002\f\u0002\u00ef\t\u0002\u0001\u0003\u0003\u0003\u00f2\b\u0003\u0001"
        + "\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"
        + "\u0003\u0003\u0003\u00fb\b\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001"
        + "\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u0104\b\u0005\n"
        + "\u0005\f\u0005\u0107\t\u0005\u0003\u0005\u0109\b\u0005\u0001\u0005\u0003"
        + "\u0005\u010c\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u0111"
        + "\b\u0005\n\u0005\f\u0005\u0114\t\u0005\u0001\u0006\u0001\u0006\u0001\u0006"
        + "\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"
        + "\u0005\u0007\u011f\b\u0007\n\u0007\f\u0007\u0122\t\u0007\u0003\u0007\u0124"
        + "\b\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0003\b\u012b"
        + "\b\b\u0001\t\u0001\t\u0003\t\u012f\b\t\u0001\n\u0001\n\u0001\u000b\u0001"
        + "\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"
        + "\f\u0001\f\u0001\f\u0001\f\u0003\f\u0140\b\f\u0001\f\u0001\f\u0001\f\u0001"
        + "\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u014d"
        + "\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"
        + "\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u015c\b\f\u0003\f\u015e\b\f"
        + "\u0001\r\u0003\r\u0161\b\r\u0001\r\u0003\r\u0164\b\r\u0001\r\u0001\r\u0003"
        + "\r\u0168\b\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u016e\b\r\u0001\r"
        + "\u0003\r\u0171\b\r\u0001\u000e\u0005\u000e\u0174\b\u000e\n\u000e\f\u000e"
        + "\u0177\t\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"
        + "\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"
        + "\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0188\b\u000f"
        + "\u0001\u000f\u0003\u000f\u018b\b\u000f\u0001\u0010\u0001\u0010\u0003\u0010"
        + "\u018f\b\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u0194\b"
        + "\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u0198\b\u0010\u0001\u0010\u0001"
        + "\u0010\u0001\u0010\u0003\u0010\u019d\b\u0010\u0001\u0010\u0001\u0010\u0001"
        + "\u0010\u0003\u0010\u01a2\b\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003"
        + "\u0010\u01a7\b\u0010\u0003\u0010\u01a9\b\u0010\u0001\u0011\u0001\u0011"
        + "\u0003\u0011\u01ad\b\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u01b1\b"
        + "\u0011\n\u0011\f\u0011\u01b4\t\u0011\u0001\u0011\u0003\u0011\u01b7\b\u0011"
        + "\u0001\u0012\u0001\u0012\u0003\u0012\u01bb\b\u0012\u0001\u0012\u0003\u0012"
        + "\u01be\b\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u01c2\b\u0012\u0001"
        + "\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u01c7\b\u0012\u0001\u0012\u0001"
        + "\u0012\u0001\u0012\u0003\u0012\u01cc\b\u0012\u0001\u0012\u0003\u0012\u01cf"
        + "\b\u0012\u0003\u0012\u01d1\b\u0012\u0001\u0013\u0001\u0013\u0003\u0013"
        + "\u01d5\b\u0013\u0001\u0013\u0003\u0013\u01d8\b\u0013\u0001\u0013\u0001"
        + "\u0013\u0003\u0013\u01dc\b\u0013\u0001\u0013\u0003\u0013\u01df\b\u0013"
        + "\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u01e4\b\u0013\u0001\u0013"
        + "\u0003\u0013\u01e7\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013"
        + "\u01ec\b\u0013\u0001\u0013\u0003\u0013\u01ef\b\u0013\u0003\u0013\u01f1"
        + "\b\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0016\u0001"
        + "\u0016\u0001\u0016\u0005\u0016\u01fa\b\u0016\n\u0016\f\u0016\u01fd\t\u0016"
        + "\u0001\u0016\u0003\u0016\u0200\b\u0016\u0001\u0017\u0003\u0017\u0203\b"
        + "\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0207\b\u0017\u0001\u0017\u0001"
        + "\u0017\u0003\u0017\u020b\b\u0017\u0001\u0017\u0003\u0017\u020e\b\u0017"
        + "\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018"
        + "\u0215\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"
        + "\u0003\u0019\u021c\b\u0019\u0001\u0019\u0003\u0019\u021f\b\u0019\u0001"
        + "\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0003"
        + "\u001a\u0227\b\u001a\u0001\u001a\u0003\u001a\u022a\b\u001a\u0001\u001b"
        + "\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c"
        + "\u0232\b\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u0236\b\u001c\u0001"
        + "\u001c\u0001\u001c\u0003\u001c\u023a\b\u001c\u0003\u001c\u023c\b\u001c"
        + "\u0003\u001c\u023e\b\u001c\u0003\u001c\u0240\b\u001c\u0001\u001c\u0003"
        + "\u001c\u0243\b\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u0248"
        + "\b\u001d\n\u001d\f\u001d\u024b\t\u001d\u0001\u001d\u0003\u001d\u024e\b"
        + "\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u0253\b\u001e\n"
        + "\u001e\f\u001e\u0256\t\u001e\u0001\u001e\u0003\u001e\u0259\b\u001e\u0001"
        + "\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u025f\b\u001f\u0001"
        + " \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0003 \u026a"
        + "\b \u0001 \u0003 \u026d\b \u0001 \u0003 \u0270\b \u0001 \u0001 \u0001"
        + " \u0001 \u0001 \u0001 \u0005 \u0278\b \n \f \u027b\t \u0001!\u0001!\u0001"
        + "!\u0001!\u0001!\u0005!\u0282\b!\n!\f!\u0285\t!\u0001!\u0001!\u0001!\u0005"
        + "!\u028a\b!\n!\f!\u028d\t!\u0003!\u028f\b!\u0001\"\u0001\"\u0001\"\u0001"
        + "\"\u0001\"\u0005\"\u0296\b\"\n\"\f\"\u0299\t\"\u0001\"\u0001\"\u0003\""
        + "\u029d\b\"\u0001#\u0001#\u0001$\u0001$\u0001$\u0001%\u0001%\u0001%\u0005"
        + "%\u02a7\b%\n%\f%\u02aa\t%\u0001&\u0001&\u0001&\u0003&\u02af\b&\u0001\'"
        + "\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001"
        + "\'\u0005\'\u02bb\b\'\n\'\f\'\u02be\t\'\u0001\'\u0001\'\u0003\'\u02c2\b"
        + "\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001"
        + "\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0005\'\u02d2\b\'\n\'\f\'\u02d5"
        + "\t\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u02dc\b\'\u0003\'"
        + "\u02de\b\'\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003"
        + "(\u02e8\b(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003"
        + "(\u02f2\b(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"
        + "(\u0001(\u0001(\u0001(\u0003(\u0300\b(\u0003(\u0302\b(\u0001)\u0001)\u0001"
        + ")\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0003)\u030e\b)\u0001"
        + "*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001"
        + "*\u0001*\u0001*\u0001*\u0003*\u031e\b*\u0001*\u0001*\u0001*\u0001*\u0001"
        + "*\u0001*\u0001*\u0001*\u0003*\u0328\b*\u0001*\u0003*\u032b\b*\u0001*\u0003"
        + "*\u032e\b*\u0001*\u0001*\u0003*\u0332\b*\u0001*\u0001*\u0003*\u0336\b"
        + "*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0003*\u033e\b*\u0003*\u0340"
        + "\b*\u0003*\u0342\b*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0005*\u034a"
        + "\b*\n*\f*\u034d\t*\u0001+\u0001+\u0001,\u0001,\u0001-\u0001-\u0001-\u0005"
        + "-\u0356\b-\n-\f-\u0359\t-\u0001-\u0003-\u035c\b-\u0001-\u0003-\u035f\b"
        + "-\u0001-\u0003-\u0362\b-\u0001.\u0001.\u0001.\u0001.\u0001.\u0001/\u0001"
        + "/\u0005/\u036b\b/\n/\f/\u036e\t/\u0001/\u0001/\u0001/\u0005/\u0373\b/"
        + "\n/\f/\u0376\t/\u0001/\u0001/\u0005/\u037a\b/\n/\f/\u037d\t/\u0003/\u037f"
        + "\b/\u00010\u00010\u00010\u00010\u00011\u00011\u00012\u00012\u00012\u0001"
        + "2\u00012\u00013\u00013\u00013\u00013\u00013\u00014\u00034\u0392\b4\u0001"
        + "4\u00014\u00014\u00014\u00015\u00015\u00015\u00015\u00015\u00015\u0001"
        + "5\u00015\u00035\u03a0\b5\u00016\u00016\u00016\u00017\u00017\u00017\u0001"
        + "7\u00017\u00057\u03aa\b7\n7\f7\u03ad\t7\u00017\u00017\u00018\u00018\u0001"
        + "8\u00018\u00018\u00018\u00018\u00018\u00018\u00038\u03ba\b8\u00038\u03bc"
        + "\b8\u00018\u00018\u00018\u00038\u03c1\b8\u00019\u00019\u00019\u00019\u0001"
        + "9\u00019\u00019\u00059\u03ca\b9\n9\f9\u03cd\t9\u0001:\u0001:\u0001;\u0001"
        + ";\u0001;\u0005;\u03d4\b;\n;\f;\u03d7\t;\u0001<\u0001<\u0001<\u0005<\u03dc"
        + "\b<\n<\f<\u03df\t<\u0001=\u0001=\u0001=\u0003=\u03e4\b=\u0001>\u0001>"
        + "\u0001>\u0001>\u0003>\u03ea\b>\u0001?\u0001?\u0001?\u0005?\u03ef\b?\n"
        + "?\f?\u03f2\t?\u0001@\u0001@\u0001@\u0005@\u03f7\b@\n@\f@\u03fa\t@\u0001"
        + "A\u0001A\u0001A\u0005A\u03ff\bA\nA\fA\u0402\tA\u0001B\u0001B\u0001B\u0005"
        + "B\u0407\bB\nB\fB\u040a\tB\u0001C\u0001C\u0001C\u0005C\u040f\bC\nC\fC\u0412"
        + "\tC\u0001D\u0001D\u0001D\u0001D\u0003D\u0418\bD\u0001D\u0005D\u041b\b"
        + "D\nD\fD\u041e\tD\u0001E\u0001E\u0001E\u0001F\u0001F\u0001F\u0003F\u0426"
        + "\bF\u0001G\u0001G\u0005G\u042a\bG\nG\fG\u042d\tG\u0001H\u0001H\u0001H"
        + "\u0001H\u0001H\u0001H\u0005H\u0435\bH\nH\fH\u0438\tH\u0003H\u043a\bH\u0001"
        + "H\u0003H\u043d\bH\u0001H\u0001H\u0001H\u0001H\u0003H\u0443\bH\u0001I\u0001"
        + "I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001"
        + "I\u0001I\u0001I\u0001I\u0005I\u0454\bI\nI\fI\u0457\tI\u0001I\u0003I\u045a"
        + "\bI\u0003I\u045c\bI\u0001I\u0001I\u0001I\u0001I\u0001I\u0005I\u0463\b"
        + "I\nI\fI\u0466\tI\u0001I\u0003I\u0469\bI\u0003I\u046b\bI\u0001I\u0003I"
        + "\u046e\bI\u0001J\u0001J\u0001J\u0001J\u0001K\u0001K\u0001L\u0001L\u0001"
        + "M\u0001M\u0001M\u0000\u0002@TN\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"
        + "\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPR"
        + "TVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e"
        + "\u0090\u0092\u0094\u0096\u0098\u009a\u0000\u0010\u0002\u0000##gg\u0002"
        + "\u0000\u0013\u0013ss\u0002\u0000\r\u000eff\u0001\u0000]`\u0001\u0000d"
        + "e\u0002\u0000QVX\\\u0001\u0000@A\u0001\u0000/:\u0003\u0000&)CCjj\u0001"
        + "\u0000OP\u0007\u0000\u0001\b\n\u0012QRX[_acess\u0001\u0000\u0003\u0004"
        + "\u0001\u0000\u001f!\u0002\u0000bbnn\u0003\u0000\u001e\u001ebbnn\n\u0000"
        + "\u0001\b\n\u0012\"/11337799;JLjss\u04ea\u0000\u009d\u0001\u0000\u0000"
        + "\u0000\u0002\u00e6\u0001\u0000\u0000\u0000\u0004\u00e8\u0001\u0000\u0000"
        + "\u0000\u0006\u00f1\u0001\u0000\u0000\u0000\b\u00fc\u0001\u0000\u0000\u0000"
        + "\n\u010b\u0001\u0000\u0000\u0000\f\u0115\u0001\u0000\u0000\u0000\u000e"
        + "\u0119\u0001\u0000\u0000\u0000\u0010\u012a\u0001\u0000\u0000\u0000\u0012"
        + "\u012c\u0001\u0000\u0000\u0000\u0014\u0130\u0001\u0000\u0000\u0000\u0016"
        + "\u0132\u0001\u0000\u0000\u0000\u0018\u015d\u0001\u0000\u0000\u0000\u001a"
        + "\u0170\u0001\u0000\u0000\u0000\u001c\u0175\u0001\u0000\u0000\u0000\u001e"
        + "\u0187\u0001\u0000\u0000\u0000 \u01a8\u0001\u0000\u0000\u0000\"\u01aa"
        + "\u0001\u0000\u0000\u0000$\u01d0\u0001\u0000\u0000\u0000&\u01f0\u0001\u0000"
        + "\u0000\u0000(\u01f2\u0001\u0000\u0000\u0000*\u01f4\u0001\u0000\u0000\u0000"
        + ",\u01f6\u0001\u0000\u0000\u0000.\u020d\u0001\u0000\u0000\u00000\u0214"
        + "\u0001\u0000\u0000\u00002\u021b\u0001\u0000\u0000\u00004\u0220\u0001\u0000"
        + "\u0000\u00006\u022b\u0001\u0000\u0000\u00008\u0242\u0001\u0000\u0000\u0000"
        + ":\u0244\u0001\u0000\u0000\u0000<\u024f\u0001\u0000\u0000\u0000>\u025e"
        + "\u0001\u0000\u0000\u0000@\u026f\u0001\u0000\u0000\u0000B\u028e\u0001\u0000"
        + "\u0000\u0000D\u029c\u0001\u0000\u0000\u0000F\u029e\u0001\u0000\u0000\u0000"
        + "H\u02a0\u0001\u0000\u0000\u0000J\u02a3\u0001\u0000\u0000\u0000L\u02ab"
        + "\u0001\u0000\u0000\u0000N\u02dd\u0001\u0000\u0000\u0000P\u0301\u0001\u0000"
        + "\u0000\u0000R\u0303\u0001\u0000\u0000\u0000T\u0341\u0001\u0000\u0000\u0000"
        + "V\u034e\u0001\u0000\u0000\u0000X\u0350\u0001\u0000\u0000\u0000Z\u0352"
        + "\u0001\u0000\u0000\u0000\\\u0363\u0001\u0000\u0000\u0000^\u037e\u0001"
        + "\u0000\u0000\u0000`\u0380\u0001\u0000\u0000\u0000b\u0384\u0001\u0000\u0000"
        + "\u0000d\u0386\u0001\u0000\u0000\u0000f\u038b\u0001\u0000\u0000\u0000h"
        + "\u0391\u0001\u0000\u0000\u0000j\u0397\u0001\u0000\u0000\u0000l\u03a1\u0001"
        + "\u0000\u0000\u0000n\u03a4\u0001\u0000\u0000\u0000p\u03b0\u0001\u0000\u0000"
        + "\u0000r\u03c2\u0001\u0000\u0000\u0000t\u03ce\u0001\u0000\u0000\u0000v"
        + "\u03d0\u0001\u0000\u0000\u0000x\u03d8\u0001\u0000\u0000\u0000z\u03e3\u0001"
        + "\u0000\u0000\u0000|\u03e5\u0001\u0000\u0000\u0000~\u03eb\u0001\u0000\u0000"
        + "\u0000\u0080\u03f3\u0001\u0000\u0000\u0000\u0082\u03fb\u0001\u0000\u0000"
        + "\u0000\u0084\u0403\u0001\u0000\u0000\u0000\u0086\u040b\u0001\u0000\u0000"
        + "\u0000\u0088\u0413\u0001\u0000\u0000\u0000\u008a\u041f\u0001\u0000\u0000"
        + "\u0000\u008c\u0425\u0001\u0000\u0000\u0000\u008e\u0427\u0001\u0000\u0000"
        + "\u0000\u0090\u0442\u0001\u0000\u0000\u0000\u0092\u046d\u0001\u0000\u0000"
        + "\u0000\u0094\u046f\u0001\u0000\u0000\u0000\u0096\u0473\u0001\u0000\u0000"
        + "\u0000\u0098\u0475\u0001\u0000\u0000\u0000\u009a\u0477\u0001\u0000\u0000"
        + "\u0000\u009c\u009e\u0003\u0016\u000b\u0000\u009d\u009c\u0001\u0000\u0000"
        + "\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e\u00a8\u0001\u0000\u0000"
        + "\u0000\u009f\u00a2\u0003d2\u0000\u00a0\u00a2\u0003f3\u0000\u00a1\u009f"
        + "\u0001\u0000\u0000\u0000\u00a1\u00a0\u0001\u0000\u0000\u0000\u00a2\u00a4"
        + "\u0001\u0000\u0000\u0000\u00a3\u00a5\u0005\u0019\u0000\u0000\u00a4\u00a3"
        + "\u0001\u0000\u0000\u0000\u00a4\u00a5\u0001\u0000\u0000\u0000\u00a5\u00a7"
        + "\u0001\u0000\u0000\u0000\u00a6\u00a1\u0001\u0000\u0000\u0000\u00a7\u00aa"
        + "\u0001\u0000\u0000\u0000\u00a8\u00a6\u0001\u0000\u0000\u0000\u00a8\u00a9"
        + "\u0001\u0000\u0000\u0000\u00a9\u00af\u0001\u0000\u0000\u0000\u00aa\u00a8"
        + "\u0001\u0000\u0000\u0000\u00ab\u00ae\u0003\u0002\u0001\u0000\u00ac\u00ae"
        + "\u0003\u0018\f\u0000\u00ad\u00ab\u0001\u0000\u0000\u0000\u00ad\u00ac\u0001"
        + "\u0000\u0000\u0000\u00ae\u00b1\u0001\u0000\u0000\u0000\u00af\u00ad\u0001"
        + "\u0000\u0000\u0000\u00af\u00b0\u0001\u0000\u0000\u0000\u00b0\u00b2\u0001"
        + "\u0000\u0000\u0000\u00b1\u00af\u0001\u0000\u0000\u0000\u00b2\u00b3\u0005"
        + "\u0000\u0000\u0001\u00b3\u0001\u0001\u0000\u0000\u0000\u00b4\u00b5\u0005"
        + "\u0006\u0000\u0000\u00b5\u00b6\u0003\u0004\u0002\u0000\u00b6\u00bf\u0005"
        + "o\u0000\u0000\u00b7\u00bc\u0003\u0006\u0003\u0000\u00b8\u00b9\u0005k\u0000"
        + "\u0000\u00b9\u00bb\u0003\u0006\u0003\u0000\u00ba\u00b8\u0001\u0000\u0000"
        + "\u0000\u00bb\u00be\u0001\u0000\u0000\u0000\u00bc\u00ba\u0001\u0000\u0000"
        + "\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd\u00c0\u0001\u0000\u0000"
        + "\u0000\u00be\u00bc\u0001\u0000\u0000\u0000\u00bf\u00b7\u0001\u0000\u0000"
        + "\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u00c1\u0001\u0000\u0000"
        + "\u0000\u00c1\u00c2\u0005p\u0000\u0000\u00c2\u00c3\u0005\u0007\u0000\u0000"
        + "\u00c3\u00cc\u0003\n\u0005\u0000\u00c4\u00c5\u0005g\u0000\u0000\u00c5"
        + "\u00c6\u0003\u001c\u000e\u0000\u00c6\u00c7\u0005h\u0000\u0000\u00c7\u00cd"
        + "\u0001\u0000\u0000\u0000\u00c8\u00c9\u0005\u0016\u0000\u0000\u00c9\u00ca"
        + "\u0003\u001c\u000e\u0000\u00ca\u00cb\u0005\u0017\u0000\u0000\u00cb\u00cd"
        + "\u0001\u0000\u0000\u0000\u00cc\u00c4\u0001\u0000\u0000\u0000\u00cc\u00c8"
        + "\u0001\u0000\u0000\u0000\u00cd\u00e7\u0001\u0000\u0000\u0000\u00ce\u00cf"
        + "\u0003\n\u0005\u0000\u00cf\u00d0\u0003\u0004\u0002\u0000\u00d0\u00d9\u0005"
        + "o\u0000\u0000\u00d1\u00d6\u0003\u0006\u0003\u0000\u00d2\u00d3\u0005k\u0000"
        + "\u0000\u00d3\u00d5\u0003\u0006\u0003\u0000\u00d4\u00d2\u0001\u0000\u0000"
        + "\u0000\u00d5\u00d8\u0001\u0000\u0000\u0000\u00d6\u00d4\u0001\u0000\u0000"
        + "\u0000\u00d6\u00d7\u0001\u0000\u0000\u0000\u00d7\u00da\u0001\u0000\u0000"
        + "\u0000\u00d8\u00d6\u0001\u0000\u0000\u0000\u00d9\u00d1\u0001\u0000\u0000"
        + "\u0000\u00d9\u00da\u0001\u0000\u0000\u0000\u00da\u00db\u0001\u0000\u0000"
        + "\u0000\u00db\u00e4\u0005p\u0000\u0000\u00dc\u00dd\u0007\u0000\u0000\u0000"
        + "\u00dd\u00de\u0003\u001c\u000e\u0000\u00de\u00df\u0005h\u0000\u0000\u00df"
        + "\u00e5\u0001\u0000\u0000\u0000\u00e0\u00e1\u0005\u0016\u0000\u0000\u00e1"
        + "\u00e2\u0003\u001c\u000e\u0000\u00e2\u00e3\u0005\u0017\u0000\u0000\u00e3"
        + "\u00e5\u0001\u0000\u0000\u0000\u00e4\u00dc\u0001\u0000\u0000\u0000\u00e4"
        + "\u00e0\u0001\u0000\u0000\u0000\u00e5\u00e7\u0001\u0000\u0000\u0000\u00e6"
        + "\u00b4\u0001\u0000\u0000\u0000\u00e6\u00ce\u0001\u0000\u0000\u0000\u00e7"
        + "\u0003\u0001\u0000\u0000\u0000\u00e8\u00ed\u0003\u0096K\u0000\u00e9\u00ea"
        + "\u0005\u0018\u0000\u0000\u00ea\u00ec\u0003\u0096K\u0000\u00eb\u00e9\u0001"
        + "\u0000\u0000\u0000\u00ec\u00ef\u0001\u0000\u0000\u0000\u00ed\u00eb\u0001"
        + "\u0000\u0000\u0000\u00ed\u00ee\u0001\u0000\u0000\u0000\u00ee\u0005\u0001"
        + "\u0000\u0000\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00f0\u00f2\u0005"
        + "\t\u0000\u0000\u00f1\u00f0\u0001\u0000\u0000\u0000\u00f1\u00f2\u0001\u0000"
        + "\u0000\u0000\u00f2\u00fa\u0001\u0000\u0000\u0000\u00f3\u00f4\u0003\b\u0004"
        + "\u0000\u00f4\u00f5\u0005l\u0000\u0000\u00f5\u00f6\u0003\n\u0005\u0000"
        + "\u00f6\u00fb\u0001\u0000\u0000\u0000\u00f7\u00f8\u0003\n\u0005\u0000\u00f8"
        + "\u00f9\u0003\b\u0004\u0000\u00f9\u00fb\u0001\u0000\u0000\u0000\u00fa\u00f3"
        + "\u0001\u0000\u0000\u0000\u00fa\u00f7\u0001\u0000\u0000\u0000\u00fb\u0007"
        + "\u0001\u0000\u0000\u0000\u00fc\u00fd\u0007\u0001\u0000\u0000\u00fd\t\u0001"
        + "\u0000\u0000\u0000\u00fe\u010c\u0003\u0096K\u0000\u00ff\u0108\u0005\u0016"
        + "\u0000\u0000\u0100\u0105\u0003\f\u0006\u0000\u0101\u0102\u0005k\u0000"
        + "\u0000\u0102\u0104\u0003\f\u0006\u0000\u0103\u0101\u0001\u0000\u0000\u0000"
        + "\u0104\u0107\u0001\u0000\u0000\u0000\u0105\u0103\u0001\u0000\u0000\u0000"
        + "\u0105\u0106\u0001\u0000\u0000\u0000\u0106\u0109\u0001\u0000\u0000\u0000"
        + "\u0107\u0105\u0001\u0000\u0000\u0000\u0108\u0100\u0001\u0000\u0000\u0000"
        + "\u0108\u0109\u0001\u0000\u0000\u0000\u0109\u010a\u0001\u0000\u0000\u0000"
        + "\u010a\u010c\u0005\u0017\u0000\u0000\u010b\u00fe\u0001\u0000\u0000\u0000"
        + "\u010b\u00ff\u0001\u0000\u0000\u0000\u010c\u0112\u0001\u0000\u0000\u0000"
        + "\u010d\u010e\u0005\u0014\u0000\u0000\u010e\u010f\u0005r\u0000\u0000\u010f"
        + "\u0111\u0005\u0015\u0000\u0000\u0110\u010d\u0001\u0000\u0000\u0000\u0111"
        + "\u0114\u0001\u0000\u0000\u0000\u0112\u0110\u0001\u0000\u0000\u0000\u0112"
        + "\u0113\u0001\u0000\u0000\u0000\u0113\u000b\u0001\u0000\u0000\u0000\u0114"
        + "\u0112\u0001\u0000\u0000\u0000\u0115\u0116\u0003\u0096K\u0000\u0116\u0117"
        + "\u0005l\u0000\u0000\u0117\u0118\u0003\n\u0005\u0000\u0118\r\u0001\u0000"
        + "\u0000\u0000\u0119\u011a\u0003\u0004\u0002\u0000\u011a\u0123\u0005o\u0000"
        + "\u0000\u011b\u0120\u0003\u0010\b\u0000\u011c\u011d\u0005k\u0000\u0000"
        + "\u011d\u011f\u0003\u0010\b\u0000\u011e\u011c\u0001\u0000\u0000\u0000\u011f"
        + "\u0122\u0001\u0000\u0000\u0000\u0120\u011e\u0001\u0000\u0000\u0000\u0120"
        + "\u0121\u0001\u0000\u0000\u0000\u0121\u0124\u0001\u0000\u0000\u0000\u0122"
        + "\u0120\u0001\u0000\u0000\u0000\u0123\u011b\u0001\u0000\u0000\u0000\u0123"
        + "\u0124\u0001\u0000\u0000\u0000\u0124\u0125\u0001\u0000\u0000\u0000\u0125"
        + "\u0126\u0005p\u0000\u0000\u0126\u000f\u0001\u0000\u0000\u0000\u0127\u0128"
        + "\u0005\t\u0000\u0000\u0128\u012b\u0003r9\u0000\u0129\u012b\u0003t:\u0000"
        + "\u012a\u0127\u0001\u0000\u0000\u0000\u012a\u0129\u0001\u0000\u0000\u0000"
        + "\u012b\u0011\u0001\u0000\u0000\u0000\u012c\u012e\u0005\b\u0000\u0000\u012d"
        + "\u012f\u0003t:\u0000\u012e\u012d\u0001\u0000\u0000\u0000\u012e\u012f\u0001"
        + "\u0000\u0000\u0000\u012f\u0013\u0001\u0000\u0000\u0000\u0130\u0131\u0003"
        + "\u000e\u0007\u0000\u0131\u0015\u0001\u0000\u0000\u0000\u0132\u0133\u0005"
        + "i\u0000\u0000\u0133\u0134\u0003\u0098L\u0000\u0134\u0017\u0001\u0000\u0000"
        + "\u0000\u0135\u0136\u0005j\u0000\u0000\u0136\u013f\u0003\u001a\r\u0000"
        + "\u0137\u0138\u0005g\u0000\u0000\u0138\u0139\u0003\u001c\u000e\u0000\u0139"
        + "\u013a\u0005h\u0000\u0000\u013a\u0140\u0001\u0000\u0000\u0000\u013b\u013c"
        + "\u0005\u0016\u0000\u0000\u013c\u013d\u0003\u001c\u000e\u0000\u013d\u013e"
        + "\u0005\u0017\u0000\u0000\u013e\u0140\u0001\u0000\u0000\u0000\u013f\u0137"
        + "\u0001\u0000\u0000\u0000\u013f\u013b\u0001\u0000\u0000\u0000\u0140\u015e"
        + "\u0001\u0000\u0000\u0000\u0141\u0142\u0005j\u0000\u0000\u0142\u0143\u0005"
        + "c\u0000\u0000\u0143\u014c\u0005f\u0000\u0000\u0144\u0145\u0005g\u0000"
        + "\u0000\u0145\u0146\u0003\u001c\u000e\u0000\u0146\u0147\u0005h\u0000\u0000"
        + "\u0147\u014d\u0001\u0000\u0000\u0000\u0148\u0149\u0005\u0016\u0000\u0000"
        + "\u0149\u014a\u0003\u001c\u000e\u0000\u014a\u014b\u0005\u0017\u0000\u0000"
        + "\u014b\u014d\u0001\u0000\u0000\u0000\u014c\u0144\u0001\u0000\u0000\u0000"
        + "\u014c\u0148\u0001\u0000\u0000\u0000\u014d\u015e\u0001\u0000\u0000\u0000"
        + "\u014e\u014f\u0005j\u0000\u0000\u014f\u0150\u0005c\u0000\u0000\u0150\u0151"
        + "\u0007\u0002\u0000\u0000\u0151\u0152\u0005;\u0000\u0000\u0152\u015b\u0003"
        + "Z-\u0000\u0153\u0154\u0005g\u0000\u0000\u0154\u0155\u0003\u001c\u000e"
        + "\u0000\u0155\u0156\u0005h\u0000\u0000\u0156\u015c\u0001\u0000\u0000\u0000"
        + "\u0157\u0158\u0005\u0016\u0000\u0000\u0158\u0159\u0003\u001c\u000e\u0000"
        + "\u0159\u015a\u0005\u0017\u0000\u0000\u015a\u015c\u0001\u0000\u0000\u0000"
        + "\u015b\u0153\u0001\u0000\u0000\u0000\u015b\u0157\u0001\u0000\u0000\u0000"
        + "\u015c\u015e\u0001\u0000\u0000\u0000\u015d\u0135\u0001\u0000\u0000\u0000"
        + "\u015d\u0141\u0001\u0000\u0000\u0000\u015d\u014e\u0001\u0000\u0000\u0000"
        + "\u015e\u0019\u0001\u0000\u0000\u0000\u015f\u0161\u0005r\u0000\u0000\u0160"
        + "\u015f\u0001\u0000\u0000\u0000\u0160\u0161\u0001\u0000\u0000\u0000\u0161"
        + "\u0163\u0001\u0000\u0000\u0000\u0162\u0164\u0005a\u0000\u0000\u0163\u0162"
        + "\u0001\u0000\u0000\u0000\u0163\u0164\u0001\u0000\u0000\u0000\u0164\u0167"
        + "\u0001\u0000\u0000\u0000\u0165\u0166\u0005b\u0000\u0000\u0166\u0168\u0005"
        + "r\u0000\u0000\u0167\u0165\u0001\u0000\u0000\u0000\u0167\u0168\u0001\u0000"
        + "\u0000\u0000\u0168\u0169\u0001\u0000\u0000\u0000\u0169\u0171\u0007\u0003"
        + "\u0000\u0000\u016a\u016d\u0005q\u0000\u0000\u016b\u016c\u0005b\u0000\u0000"
        + "\u016c\u016e\u0005r\u0000\u0000\u016d\u016b\u0001\u0000\u0000\u0000\u016d"
        + "\u016e\u0001\u0000\u0000\u0000\u016e\u016f\u0001\u0000\u0000\u0000\u016f"
        + "\u0171\u0007\u0003\u0000\u0000\u0170\u0160\u0001\u0000\u0000\u0000\u0170"
        + "\u016a\u0001\u0000\u0000\u0000\u0171\u001b\u0001\u0000\u0000\u0000\u0172"
        + "\u0174\u0003\u001e\u000f\u0000\u0173\u0172\u0001\u0000\u0000\u0000\u0174"
        + "\u0177\u0001\u0000\u0000\u0000\u0175\u0173\u0001\u0000\u0000\u0000\u0175"
        + "\u0176\u0001\u0000\u0000\u0000\u0176\u001d\u0001\u0000\u0000\u0000\u0177"
        + "\u0175\u0001\u0000\u0000\u0000\u0178\u0188\u0003j5\u0000\u0179\u0188\u0003"
        + "l6\u0000\u017a\u0188\u0003f3\u0000\u017b\u0188\u0003h4\u0000\u017c\u0188"
        + "\u0003n7\u0000\u017d\u0188\u0003p8\u0000\u017e\u0188\u0003\u0012\t\u0000"
        + "\u017f\u0188\u0003$\u0012\u0000\u0180\u0188\u0003 \u0010\u0000\u0181\u0188"
        + "\u0003&\u0013\u0000\u0182\u0188\u0003N\'\u0000\u0183\u0188\u0003P(\u0000"
        + "\u0184\u0188\u0003R)\u0000\u0185\u0188\u0003\"\u0011\u0000\u0186\u0188"
        + "\u0003\u0014\n\u0000\u0187\u0178\u0001\u0000\u0000\u0000\u0187\u0179\u0001"
        + "\u0000\u0000\u0000\u0187\u017a\u0001\u0000\u0000\u0000\u0187\u017b\u0001"
        + "\u0000\u0000\u0000\u0187\u017c\u0001\u0000\u0000\u0000\u0187\u017d\u0001"
        + "\u0000\u0000\u0000\u0187\u017e\u0001\u0000\u0000\u0000\u0187\u017f\u0001"
        + "\u0000\u0000\u0000\u0187\u0180\u0001\u0000\u0000\u0000\u0187\u0181\u0001"
        + "\u0000\u0000\u0000\u0187\u0182\u0001\u0000\u0000\u0000\u0187\u0183\u0001"
        + "\u0000\u0000\u0000\u0187\u0184\u0001\u0000\u0000\u0000\u0187\u0185\u0001"
        + "\u0000\u0000\u0000\u0187\u0186\u0001\u0000\u0000\u0000\u0188\u018a\u0001"
        + "\u0000\u0000\u0000\u0189\u018b\u0005\u0019\u0000\u0000\u018a\u0189\u0001"
        + "\u0000\u0000\u0000\u018a\u018b\u0001\u0000\u0000\u0000\u018b\u001f\u0001"
        + "\u0000\u0000\u0000\u018c\u018e\u0005>\u0000\u0000\u018d\u018f\u00032\u0019"
        + "\u0000\u018e\u018d\u0001\u0000\u0000\u0000\u018e\u018f\u0001\u0000\u0000"
        + "\u0000\u018f\u0190\u0001\u0000\u0000\u0000\u0190\u0191\u0005c\u0000\u0000"
        + "\u0191\u0193\u0007\u0004\u0000\u0000\u0192\u0194\u00032\u0019\u0000\u0193"
        + "\u0192\u0001\u0000\u0000\u0000\u0193\u0194\u0001\u0000\u0000\u0000\u0194"
        + "\u0195\u0001\u0000\u0000\u0000\u0195\u0197\u0005<\u0000\u0000\u0196\u0198"
        + "\u0005C\u0000\u0000\u0197\u0196\u0001\u0000\u0000\u0000\u0197\u0198\u0001"
        + "\u0000\u0000\u0000\u0198\u0199\u0001\u0000\u0000\u0000\u0199\u01a9\u0003"
        + "Z-\u0000\u019a\u019c\u0005<\u0000\u0000\u019b\u019d\u0005C\u0000\u0000"
        + "\u019c\u019b\u0001\u0000\u0000\u0000\u019c\u019d\u0001\u0000\u0000\u0000"
        + "\u019d\u019e\u0001\u0000\u0000\u0000\u019e\u019f\u0003Z-\u0000\u019f\u01a1"
        + "\u0005>\u0000\u0000\u01a0\u01a2\u00032\u0019\u0000\u01a1\u01a0\u0001\u0000"
        + "\u0000\u0000\u01a1\u01a2\u0001\u0000\u0000\u0000\u01a2\u01a3\u0001\u0000"
        + "\u0000\u0000\u01a3\u01a4\u0005c\u0000\u0000\u01a4\u01a6\u0007\u0004\u0000"
        + "\u0000\u01a5\u01a7\u00032\u0019\u0000\u01a6\u01a5\u0001\u0000\u0000\u0000"
        + "\u01a6\u01a7\u0001\u0000\u0000\u0000\u01a7\u01a9\u0001\u0000\u0000\u0000"
        + "\u01a8\u018c\u0001\u0000\u0000\u0000\u01a8\u019a\u0001\u0000\u0000\u0000"
        + "\u01a9!\u0001\u0000\u0000\u0000\u01aa\u01ac\u0005E\u0000\u0000\u01ab\u01ad"
        + "\u0003^/\u0000\u01ac\u01ab\u0001\u0000\u0000\u0000\u01ac\u01ad\u0001\u0000"
        + "\u0000\u0000\u01ad\u01b2\u0001\u0000\u0000\u0000\u01ae\u01af\u0005k\u0000"
        + "\u0000\u01af\u01b1\u0003^/\u0000\u01b0\u01ae\u0001\u0000\u0000\u0000\u01b1"
        + "\u01b4\u0001\u0000\u0000\u0000\u01b2\u01b0\u0001\u0000\u0000\u0000\u01b2"
        + "\u01b3\u0001\u0000\u0000\u0000\u01b3\u01b6\u0001\u0000\u0000\u0000\u01b4"
        + "\u01b2\u0001\u0000\u0000\u0000\u01b5\u01b7\u0005k\u0000\u0000\u01b6\u01b5"
        + "\u0001\u0000\u0000\u0000\u01b6\u01b7\u0001\u0000\u0000\u0000\u01b7#\u0001"
        + "\u0000\u0000\u0000\u01b8\u01ba\u0005=\u0000\u0000\u01b9\u01bb\u0003(\u0014"
        + "\u0000\u01ba\u01b9\u0001\u0000\u0000\u0000\u01ba\u01bb\u0001\u0000\u0000"
        + "\u0000\u01bb\u01bd\u0001\u0000\u0000\u0000\u01bc\u01be\u00036\u001b\u0000"
        + "\u01bd\u01bc\u0001\u0000\u0000\u0000\u01bd\u01be\u0001\u0000\u0000\u0000"
        + "\u01be\u01bf\u0001\u0000\u0000\u0000\u01bf\u01c1\u0005;\u0000\u0000\u01c0"
        + "\u01c2\u0005C\u0000\u0000\u01c1\u01c0\u0001\u0000\u0000\u0000\u01c1\u01c2"
        + "\u0001\u0000\u0000\u0000\u01c2\u01c3\u0001\u0000\u0000\u0000\u01c3\u01d1"
        + "\u0003Z-\u0000\u01c4\u01c6\u0005;\u0000\u0000\u01c5\u01c7\u0005C\u0000"
        + "\u0000\u01c6\u01c5\u0001\u0000\u0000\u0000\u01c6\u01c7\u0001\u0000\u0000"
        + "\u0000\u01c7\u01c8\u0001\u0000\u0000\u0000\u01c8\u01c9\u0003Z-\u0000\u01c9"
        + "\u01cb\u0005=\u0000\u0000\u01ca\u01cc\u0003(\u0014\u0000\u01cb\u01ca\u0001"
        + "\u0000\u0000\u0000\u01cb\u01cc\u0001\u0000\u0000\u0000\u01cc\u01ce\u0001"
        + "\u0000\u0000\u0000\u01cd\u01cf\u00036\u001b\u0000\u01ce\u01cd\u0001\u0000"
        + "\u0000\u0000\u01ce\u01cf\u0001\u0000\u0000\u0000\u01cf\u01d1\u0001\u0000"
        + "\u0000\u0000\u01d0\u01b8\u0001\u0000\u0000\u0000\u01d0\u01c4\u0001\u0000"
        + "\u0000\u0000\u01d1%\u0001\u0000\u0000\u0000\u01d2\u01d4\u0005>\u0000\u0000"
        + "\u01d3\u01d5\u0003*\u0015\u0000\u01d4\u01d3\u0001\u0000\u0000\u0000\u01d4"
        + "\u01d5\u0001\u0000\u0000\u0000\u01d5\u01d7\u0001\u0000\u0000\u0000\u01d6"
        + "\u01d8\u00036\u001b\u0000\u01d7\u01d6\u0001\u0000\u0000\u0000\u01d7\u01d8"
        + "\u0001\u0000\u0000\u0000\u01d8\u01d9\u0001\u0000\u0000\u0000\u01d9\u01db"
        + "\u0005<\u0000\u0000\u01da\u01dc\u0003`0\u0000\u01db\u01da\u0001\u0000"
        + "\u0000\u0000\u01db\u01dc\u0001\u0000\u0000\u0000\u01dc\u01de\u0001\u0000"
        + "\u0000\u0000\u01dd\u01df\u0005C\u0000\u0000\u01de\u01dd\u0001\u0000\u0000"
        + "\u0000\u01de\u01df\u0001\u0000\u0000\u0000\u01df\u01e0\u0001\u0000\u0000"
        + "\u0000\u01e0\u01f1\u0003Z-\u0000\u01e1\u01e3\u0005<\u0000\u0000\u01e2"
        + "\u01e4\u0003`0\u0000\u01e3\u01e2\u0001\u0000\u0000\u0000\u01e3\u01e4\u0001"
        + "\u0000\u0000\u0000\u01e4\u01e6\u0001\u0000\u0000\u0000\u01e5\u01e7\u0005"
        + "C\u0000\u0000\u01e6\u01e5\u0001\u0000\u0000\u0000\u01e6\u01e7\u0001\u0000"
        + "\u0000\u0000\u01e7\u01e8\u0001\u0000\u0000\u0000\u01e8\u01e9\u0003Z-\u0000"
        + "\u01e9\u01eb\u0005>\u0000\u0000\u01ea\u01ec\u0003*\u0015\u0000\u01eb\u01ea"
        + "\u0001\u0000\u0000\u0000\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec\u01ee"
        + "\u0001\u0000\u0000\u0000\u01ed\u01ef\u00036\u001b\u0000\u01ee\u01ed\u0001"
        + "\u0000\u0000\u0000\u01ee\u01ef\u0001\u0000\u0000\u0000\u01ef\u01f1\u0001"
        + "\u0000\u0000\u0000\u01f0\u01d2\u0001\u0000\u0000\u0000\u01f0\u01e1\u0001"
        + "\u0000\u0000\u0000\u01f1\'\u0001\u0000\u0000\u0000\u01f2\u01f3\u0003,"
        + "\u0016\u0000\u01f3)\u0001\u0000\u0000\u0000\u01f4\u01f5\u0003,\u0016\u0000"
        + "\u01f5+\u0001\u0000\u0000\u0000\u01f6\u01fb\u0003.\u0017\u0000\u01f7\u01f8"
        + "\u0005k\u0000\u0000\u01f8\u01fa\u0003.\u0017\u0000\u01f9\u01f7\u0001\u0000"
        + "\u0000\u0000\u01fa\u01fd\u0001\u0000\u0000\u0000\u01fb\u01f9\u0001\u0000"
        + "\u0000\u0000\u01fb\u01fc\u0001\u0000\u0000\u0000\u01fc\u01ff\u0001\u0000"
        + "\u0000\u0000\u01fd\u01fb\u0001\u0000\u0000\u0000\u01fe\u0200\u0005k\u0000"
        + "\u0000\u01ff\u01fe\u0001\u0000\u0000\u0000\u01ff\u0200\u0001\u0000\u0000"
        + "\u0000\u0200-\u0001\u0000\u0000\u0000\u0201\u0203\u00030\u0018\u0000\u0202"
        + "\u0201\u0001\u0000\u0000\u0000\u0202\u0203\u0001\u0000\u0000\u0000\u0203"
        + "\u0204\u0001\u0000\u0000\u0000\u0204\u0206\u0003<\u001e\u0000\u0205\u0207"
        + "\u0003>\u001f\u0000\u0206\u0205\u0001\u0000\u0000\u0000\u0206\u0207\u0001"
        + "\u0000\u0000\u0000\u0207\u020e\u0001\u0000\u0000\u0000\u0208\u020a\u0003"
        + "0\u0018\u0000\u0209\u020b\u0003>\u001f\u0000\u020a\u0209\u0001\u0000\u0000"
        + "\u0000\u020a\u020b\u0001\u0000\u0000\u0000\u020b\u020e\u0001\u0000\u0000"
        + "\u0000\u020c\u020e\u0003>\u001f\u0000\u020d\u0202\u0001\u0000\u0000\u0000"
        + "\u020d\u0208\u0001\u0000\u0000\u0000\u020d\u020c\u0001\u0000\u0000\u0000"
        + "\u020e/\u0001\u0000\u0000\u0000\u020f\u0210\u00032\u0019\u0000\u0210\u0211"
        + "\u00034\u001a\u0000\u0211\u0215\u0001\u0000\u0000\u0000\u0212\u0215\u0003"
        + "4\u001a\u0000\u0213\u0215\u00032\u0019\u0000\u0214\u020f\u0001\u0000\u0000"
        + "\u0000\u0214\u0212\u0001\u0000\u0000\u0000\u0214\u0213\u0001\u0000\u0000"
        + "\u0000\u02151\u0001\u0000\u0000\u0000\u0216\u021c\u0003\u009aM\u0000\u0217"
        + "\u0218\u0005o\u0000\u0000\u0218\u0219\u0003t:\u0000\u0219\u021a\u0005"
        + "p\u0000\u0000\u021a\u021c\u0001\u0000\u0000\u0000\u021b\u0216\u0001\u0000"
        + "\u0000\u0000\u021b\u0217\u0001\u0000\u0000\u0000\u021c\u021e\u0001\u0000"
        + "\u0000\u0000\u021d\u021f\u0005C\u0000\u0000\u021e\u021d\u0001\u0000\u0000"
        + "\u0000\u021e\u021f\u0001\u0000\u0000\u0000\u021f3\u0001\u0000\u0000\u0000"
        + "\u0220\u0226\u0005B\u0000\u0000\u0221\u0227\u0003\u009aM\u0000\u0222\u0223"
        + "\u0005o\u0000\u0000\u0223\u0224\u0003t:\u0000\u0224\u0225\u0005p\u0000"
        + "\u0000\u0225\u0227\u0001\u0000\u0000\u0000\u0226\u0221\u0001\u0000\u0000"
        + "\u0000\u0226\u0222\u0001\u0000\u0000\u0000\u0227\u0229\u0001\u0000\u0000"
        + "\u0000\u0228\u022a\u0005C\u0000\u0000\u0229\u0228\u0001\u0000\u0000\u0000"
        + "\u0229\u022a\u0001\u0000\u0000\u0000\u022a5\u0001\u0000\u0000\u0000\u022b"
        + "\u022c\u0005D\u0000\u0000\u022c\u022d\u0003:\u001d\u0000\u022d7\u0001"
        + "\u0000\u0000\u0000\u022e\u023f\u0003b1\u0000\u022f\u0231\u0005l\u0000"
        + "\u0000\u0230\u0232\u0003b1\u0000\u0231\u0230\u0001\u0000\u0000\u0000\u0231"
        + "\u0232\u0001\u0000\u0000\u0000\u0232\u023d\u0001\u0000\u0000\u0000\u0233"
        + "\u0235\u0005l\u0000\u0000\u0234\u0236\u0003b1\u0000\u0235\u0234\u0001"
        + "\u0000\u0000\u0000\u0235\u0236\u0001\u0000\u0000\u0000\u0236\u023b\u0001"
        + "\u0000\u0000\u0000\u0237\u0239\u0005l\u0000\u0000\u0238\u023a\u0003b1"
        + "\u0000\u0239\u0238\u0001\u0000\u0000\u0000\u0239\u023a\u0001\u0000\u0000"
        + "\u0000\u023a\u023c\u0001\u0000\u0000\u0000\u023b\u0237\u0001\u0000\u0000"
        + "\u0000\u023b\u023c\u0001\u0000\u0000\u0000\u023c\u023e\u0001\u0000\u0000"
        + "\u0000\u023d\u0233\u0001\u0000\u0000\u0000\u023d\u023e\u0001\u0000\u0000"
        + "\u0000\u023e\u0240\u0001\u0000\u0000\u0000\u023f\u022f\u0001\u0000\u0000"
        + "\u0000\u023f\u0240\u0001\u0000\u0000\u0000\u0240\u0243\u0001\u0000\u0000"
        + "\u0000\u0241\u0243\u0003\u0098L\u0000\u0242\u022e\u0001\u0000\u0000\u0000"
        + "\u0242\u0241\u0001\u0000\u0000\u0000\u02439\u0001\u0000\u0000\u0000\u0244"
        + "\u0249\u00038\u001c\u0000\u0245\u0246\u0005k\u0000\u0000\u0246\u0248\u0003"
        + "8\u001c\u0000\u0247\u0245\u0001\u0000\u0000\u0000\u0248\u024b\u0001\u0000"
        + "\u0000\u0000\u0249\u0247\u0001\u0000\u0000\u0000\u0249\u024a\u0001\u0000"
        + "\u0000\u0000\u024a\u024d\u0001\u0000\u0000\u0000\u024b\u0249\u0001\u0000"
        + "\u0000\u0000\u024c\u024e\u0005k\u0000\u0000\u024d\u024c\u0001\u0000\u0000"
        + "\u0000\u024d\u024e\u0001\u0000\u0000\u0000\u024e;\u0001\u0000\u0000\u0000"
        + "\u024f\u0254\u00038\u001c\u0000\u0250\u0251\u0005.\u0000\u0000\u0251\u0253"
        + "\u00038\u001c\u0000\u0252\u0250\u0001\u0000\u0000\u0000\u0253\u0256\u0001"
        + "\u0000\u0000\u0000\u0254\u0252\u0001\u0000\u0000\u0000\u0254\u0255\u0001"
        + "\u0000\u0000\u0000\u0255\u0258\u0001\u0000\u0000\u0000\u0256\u0254\u0001"
        + "\u0000\u0000\u0000\u0257\u0259\u0005.\u0000\u0000\u0258\u0257\u0001\u0000"
        + "\u0000\u0000\u0258\u0259\u0001\u0000\u0000\u0000\u0259=\u0001\u0000\u0000"
        + "\u0000\u025a\u025b\u0005I\u0000\u0000\u025b\u025f\u0003@ \u0000\u025c"
        + "\u025d\u0005H\u0000\u0000\u025d\u025f\u0003@ \u0000\u025e\u025a\u0001"
        + "\u0000\u0000\u0000\u025e\u025c\u0001\u0000\u0000\u0000\u025f?\u0001\u0000"
        + "\u0000\u0000\u0260\u0261\u0006 \uffff\uffff\u0000\u0261\u0262\u0005o\u0000"
        + "\u0000\u0262\u0263\u0003@ \u0000\u0263\u0264\u0005p\u0000\u0000\u0264"
        + "\u0270\u0001\u0000\u0000\u0000\u0265\u0266\u0005,\u0000\u0000\u0266\u0270"
        + "\u0003@ \u0004\u0267\u0269\u0005J\u0000\u0000\u0268\u026a\u0005K\u0000"
        + "\u0000\u0269\u0268\u0001\u0000\u0000\u0000\u0269\u026a\u0001\u0000\u0000"
        + "\u0000\u026a\u026d\u0001\u0000\u0000\u0000\u026b\u026d\u0005K\u0000\u0000"
        + "\u026c\u0267\u0001\u0000\u0000\u0000\u026c\u026b\u0001\u0000\u0000\u0000"
        + "\u026d\u026e\u0001\u0000\u0000\u0000\u026e\u0270\u0003B!\u0000\u026f\u0260"
        + "\u0001\u0000\u0000\u0000\u026f\u0265\u0001\u0000\u0000\u0000\u026f\u026c"
        + "\u0001\u0000\u0000\u0000\u0270\u0279\u0001\u0000\u0000\u0000\u0271\u0272"
        + "\n\u0003\u0000\u0000\u0272\u0273\u0005-\u0000\u0000\u0273\u0278\u0003"
        + "@ \u0004\u0274\u0275\n\u0002\u0000\u0000\u0275\u0276\u0005.\u0000\u0000"
        + "\u0276\u0278\u0003@ \u0003\u0277\u0271\u0001\u0000\u0000\u0000\u0277\u0274"
        + "\u0001\u0000\u0000\u0000\u0278\u027b\u0001\u0000\u0000\u0000\u0279\u0277"
        + "\u0001\u0000\u0000\u0000\u0279\u027a\u0001\u0000\u0000\u0000\u027aA\u0001"
        + "\u0000\u0000\u0000\u027b\u0279\u0001\u0000\u0000\u0000\u027c\u027d\u0003"
        + "b1\u0000\u027d\u027e\u0005l\u0000\u0000\u027e\u0283\u0003b1\u0000\u027f"
        + "\u0280\u0005m\u0000\u0000\u0280\u0282\u0003b1\u0000\u0281\u027f\u0001"
        + "\u0000\u0000\u0000\u0282\u0285\u0001\u0000\u0000\u0000\u0283\u0281\u0001"
        + "\u0000\u0000\u0000\u0283\u0284\u0001\u0000\u0000\u0000\u0284\u028f\u0001"
        + "\u0000\u0000\u0000\u0285\u0283\u0001\u0000\u0000\u0000\u0286\u028b\u0003"
        + "b1\u0000\u0287\u0288\u0005m\u0000\u0000\u0288\u028a\u0003b1\u0000\u0289"
        + "\u0287\u0001\u0000\u0000\u0000\u028a\u028d\u0001\u0000\u0000\u0000\u028b"
        + "\u0289\u0001\u0000\u0000\u0000\u028b\u028c\u0001\u0000\u0000\u0000\u028c"
        + "\u028f\u0001\u0000\u0000\u0000\u028d\u028b\u0001\u0000\u0000\u0000\u028e"
        + "\u027c\u0001\u0000\u0000\u0000\u028e\u0286\u0001\u0000\u0000\u0000\u028f"
        + "C\u0001\u0000\u0000\u0000\u0290\u0291\u0005C\u0000\u0000\u0291\u029d\u0005"
        + "W\u0000\u0000\u0292\u0297\u0003F#\u0000\u0293\u0294\u0005k\u0000\u0000"
        + "\u0294\u0296\u0003F#\u0000\u0295\u0293\u0001\u0000\u0000\u0000\u0296\u0299"
        + "\u0001\u0000\u0000\u0000\u0297\u0295\u0001\u0000\u0000\u0000\u0297\u0298"
        + "\u0001\u0000\u0000\u0000\u0298\u029a\u0001\u0000\u0000\u0000\u0299\u0297"
        + "\u0001\u0000\u0000\u0000\u029a\u029b\u0005W\u0000\u0000\u029b\u029d\u0001"
        + "\u0000\u0000\u0000\u029c\u0290\u0001\u0000\u0000\u0000\u029c\u0292\u0001"
        + "\u0000\u0000\u0000\u029dE\u0001\u0000\u0000\u0000\u029e\u029f\u0007\u0005"
        + "\u0000\u0000\u029fG\u0001\u0000\u0000\u0000\u02a0\u02a1\u0007\u0006\u0000"
        + "\u0000\u02a1\u02a2\u0003J%\u0000\u02a2I\u0001\u0000\u0000\u0000\u02a3"
        + "\u02a8\u0003L&\u0000\u02a4\u02a5\u0005k\u0000\u0000\u02a5\u02a7\u0003"
        + "L&\u0000\u02a6\u02a4\u0001\u0000\u0000\u0000\u02a7\u02aa\u0001\u0000\u0000"
        + "\u0000\u02a8\u02a6\u0001\u0000\u0000\u0000\u02a8\u02a9\u0001\u0000\u0000"
        + "\u0000\u02a9K\u0001\u0000\u0000\u0000\u02aa\u02a8\u0001\u0000\u0000\u0000"
        + "\u02ab\u02ae\u0003\u009aM\u0000\u02ac\u02ad\u0005n\u0000\u0000\u02ad\u02af"
        + "\u0003\u009aM\u0000\u02ae\u02ac\u0001\u0000\u0000\u0000\u02ae\u02af\u0001"
        + "\u0000\u0000\u0000\u02afM\u0001\u0000\u0000\u0000\u02b0\u02b1\u0005\""
        + "\u0000\u0000\u02b1\u02b2\u0003T*\u0000\u02b2\u02b3\u0005#\u0000\u0000"
        + "\u02b3\u02bc\u0003\u001c\u000e\u0000\u02b4\u02b5\u0005$\u0000\u0000\u02b5"
        + "\u02b6\u0005\"\u0000\u0000\u02b6\u02b7\u0003T*\u0000\u02b7\u02b8\u0005"
        + "#\u0000\u0000\u02b8\u02b9\u0003\u001c\u000e\u0000\u02b9\u02bb\u0001\u0000"
        + "\u0000\u0000\u02ba\u02b4\u0001\u0000\u0000\u0000\u02bb\u02be\u0001\u0000"
        + "\u0000\u0000\u02bc\u02ba\u0001\u0000\u0000\u0000\u02bc\u02bd\u0001\u0000"
        + "\u0000\u0000\u02bd\u02c1\u0001\u0000\u0000\u0000\u02be\u02bc\u0001\u0000"
        + "\u0000\u0000\u02bf\u02c0\u0005$\u0000\u0000\u02c0\u02c2\u0003\u001c\u000e"
        + "\u0000\u02c1\u02bf\u0001\u0000\u0000\u0000\u02c1\u02c2\u0001\u0000\u0000"
        + "\u0000\u02c2\u02c3\u0001\u0000\u0000\u0000\u02c3\u02c4\u0005h\u0000\u0000"
        + "\u02c4\u02de\u0001\u0000\u0000\u0000\u02c5\u02c6\u0005\"\u0000\u0000\u02c6"
        + "\u02c7\u0003T*\u0000\u02c7\u02c8\u0005\u0016\u0000\u0000\u02c8\u02c9\u0003"
        + "\u001c\u000e\u0000\u02c9\u02d3\u0005\u0017\u0000\u0000\u02ca\u02cb\u0005"
        + "$\u0000\u0000\u02cb\u02cc\u0005\"\u0000\u0000\u02cc\u02cd\u0003T*\u0000"
        + "\u02cd\u02ce\u0005\u0016\u0000\u0000\u02ce\u02cf\u0003\u001c\u000e\u0000"
        + "\u02cf\u02d0\u0005\u0017\u0000\u0000\u02d0\u02d2\u0001\u0000\u0000\u0000"
        + "\u02d1\u02ca\u0001\u0000\u0000\u0000\u02d2\u02d5\u0001\u0000\u0000\u0000"
        + "\u02d3\u02d1\u0001\u0000\u0000\u0000\u02d3\u02d4\u0001\u0000\u0000\u0000"
        + "\u02d4\u02db\u0001\u0000\u0000\u0000\u02d5\u02d3\u0001\u0000\u0000\u0000"
        + "\u02d6\u02d7\u0005$\u0000\u0000\u02d7\u02d8\u0005\u0016\u0000\u0000\u02d8"
        + "\u02d9\u0003\u001c\u000e\u0000\u02d9\u02da\u0005\u0017\u0000\u0000\u02da"
        + "\u02dc\u0001\u0000\u0000\u0000\u02db\u02d6\u0001\u0000\u0000\u0000\u02db"
        + "\u02dc\u0001\u0000\u0000\u0000\u02dc\u02de\u0001\u0000\u0000\u0000\u02dd"
        + "\u02b0\u0001\u0000\u0000\u0000\u02dd\u02c5\u0001\u0000\u0000\u0000\u02de"
        + "O\u0001\u0000\u0000\u0000\u02df\u02e0\u0005\u0010\u0000\u0000\u02e0\u02e1"
        + "\u0003\b\u0004\u0000\u02e1\u02e2\u00054\u0000\u0000\u02e2\u02e3\u0003"
        + "t:\u0000\u02e3\u02e4\u0005<\u0000\u0000\u02e4\u02e7\u0003t:\u0000\u02e5"
        + "\u02e6\u0005\u0012\u0000\u0000\u02e6\u02e8\u0003t:\u0000\u02e7\u02e5\u0001"
        + "\u0000\u0000\u0000\u02e7\u02e8\u0001\u0000\u0000\u0000\u02e8\u02f1\u0001"
        + "\u0000\u0000\u0000\u02e9\u02ea\u0005g\u0000\u0000\u02ea\u02eb\u0003\u001c"
        + "\u000e\u0000\u02eb\u02ec\u0005h\u0000\u0000\u02ec\u02f2\u0001\u0000\u0000"
        + "\u0000\u02ed\u02ee\u0005\u0016\u0000\u0000\u02ee\u02ef\u0003\u001c\u000e"
        + "\u0000\u02ef\u02f0\u0005\u0017\u0000\u0000\u02f0\u02f2\u0001\u0000\u0000"
        + "\u0000\u02f1\u02e9\u0001\u0000\u0000\u0000\u02f1\u02ed\u0001\u0000\u0000"
        + "\u0000\u02f2\u0302\u0001\u0000\u0000\u0000\u02f3\u02f4\u0005\u0010\u0000"
        + "\u0000\u02f4\u02f5\u0003\b\u0004\u0000\u02f5\u02f6\u0005G\u0000\u0000"
        + "\u02f6\u02ff\u0003t:\u0000\u02f7\u02f8\u0005g\u0000\u0000\u02f8\u02f9"
        + "\u0003\u001c\u000e\u0000\u02f9\u02fa\u0005h\u0000\u0000\u02fa\u0300\u0001"
        + "\u0000\u0000\u0000\u02fb\u02fc\u0005\u0016\u0000\u0000\u02fc\u02fd\u0003"
        + "\u001c\u000e\u0000\u02fd\u02fe\u0005\u0017\u0000\u0000\u02fe\u0300\u0001"
        + "\u0000\u0000\u0000\u02ff\u02f7\u0001\u0000\u0000\u0000\u02ff\u02fb\u0001"
        + "\u0000\u0000\u0000\u0300\u0302\u0001\u0000\u0000\u0000\u0301\u02df\u0001"
        + "\u0000\u0000\u0000\u0301\u02f3\u0001\u0000\u0000\u0000\u0302Q\u0001\u0000"
        + "\u0000\u0000\u0303\u0304\u0005\u0011\u0000\u0000\u0304\u030d\u0003T*\u0000"
        + "\u0305\u0306\u0005g\u0000\u0000\u0306\u0307\u0003\u001c\u000e\u0000\u0307"
        + "\u0308\u0005h\u0000\u0000\u0308\u030e\u0001\u0000\u0000\u0000\u0309\u030a"
        + "\u0005\u0016\u0000\u0000\u030a\u030b\u0003\u001c\u000e\u0000\u030b\u030c"
        + "\u0005\u0017\u0000\u0000\u030c\u030e\u0001\u0000\u0000\u0000\u030d\u0305"
        + "\u0001\u0000\u0000\u0000\u030d\u0309\u0001\u0000\u0000\u0000\u030eS\u0001"
        + "\u0000\u0000\u0000\u030f\u0310\u0006*\uffff\uffff\u0000\u0310\u0342\u0005"
        + "*\u0000\u0000\u0311\u0342\u0005+\u0000\u0000\u0312\u0313\u0005o\u0000"
        + "\u0000\u0313\u0314\u0003T*\u0000\u0314\u0315\u0005p\u0000\u0000\u0315"
        + "\u0342\u0001\u0000\u0000\u0000\u0316\u0317\u0005o\u0000\u0000\u0317\u0318"
        + "\u0003t:\u0000\u0318\u0319\u0005p\u0000\u0000\u0319\u0342\u0001\u0000"
        + "\u0000\u0000\u031a\u031b\u0005,\u0000\u0000\u031b\u0342\u0003T*\u0005"
        + "\u031c\u031e\u0003X,\u0000\u031d\u031c\u0001\u0000\u0000\u0000\u031d\u031e"
        + "\u0001\u0000\u0000\u0000\u031e\u031f\u0001\u0000\u0000\u0000\u031f\u0320"
        + "\u0003Z-\u0000\u0320\u0321\u0005%\u0000\u0000\u0321\u0327\u0003V+\u0000"
        + "\u0322\u0328\u0003\u009aM\u0000\u0323\u0324\u0005o\u0000\u0000\u0324\u0325"
        + "\u0003t:\u0000\u0325\u0326\u0005p\u0000\u0000\u0326\u0328\u0001\u0000"
        + "\u0000\u0000\u0327\u0322\u0001\u0000\u0000\u0000\u0327\u0323\u0001\u0000"
        + "\u0000\u0000\u0328\u032a\u0001\u0000\u0000\u0000\u0329\u032b\u0003<\u001e"
        + "\u0000\u032a\u0329\u0001\u0000\u0000\u0000\u032a\u032b\u0001\u0000\u0000"
        + "\u0000\u032b\u032d\u0001\u0000\u0000\u0000\u032c\u032e\u0003>\u001f\u0000"
        + "\u032d\u032c\u0001\u0000\u0000\u0000\u032d\u032e\u0001\u0000\u0000\u0000"
        + "\u032e\u0331\u0001\u0000\u0000\u0000\u032f\u0330\u0005D\u0000\u0000\u0330"
        + "\u0332\u0003:\u001d\u0000\u0331\u032f\u0001\u0000\u0000\u0000\u0331\u0332"
        + "\u0001\u0000\u0000\u0000\u0332\u0342\u0001\u0000\u0000\u0000\u0333\u0335"
        + "\u0005c\u0000\u0000\u0334\u0336\u0007\u0004\u0000\u0000\u0335\u0334\u0001"
        + "\u0000\u0000\u0000\u0335\u0336\u0001\u0000\u0000\u0000\u0336\u033f\u0001"
        + "\u0000\u0000\u0000\u0337\u033d\u0003V+\u0000\u0338\u033e\u0003\u009aM"
        + "\u0000\u0339\u033a\u0005o\u0000\u0000\u033a\u033b\u0003t:\u0000\u033b"
        + "\u033c\u0005p\u0000\u0000\u033c\u033e\u0001\u0000\u0000\u0000\u033d\u0338"
        + "\u0001\u0000\u0000\u0000\u033d\u0339\u0001\u0000\u0000\u0000\u033e\u0340"
        + "\u0001\u0000\u0000\u0000\u033f\u0337\u0001\u0000\u0000\u0000\u033f\u0340"
        + "\u0001\u0000\u0000\u0000\u0340\u0342\u0001\u0000\u0000\u0000\u0341\u030f"
        + "\u0001\u0000\u0000\u0000\u0341\u0311\u0001\u0000\u0000\u0000\u0341\u0312"
        + "\u0001\u0000\u0000\u0000\u0341\u0316\u0001\u0000\u0000\u0000\u0341\u031a"
        + "\u0001\u0000\u0000\u0000\u0341\u031d\u0001\u0000\u0000\u0000\u0341\u0333"
        + "\u0001\u0000\u0000\u0000\u0342\u034b\u0001\u0000\u0000\u0000\u0343\u0344"
        + "\n\u0004\u0000\u0000\u0344\u0345\u0005-\u0000\u0000\u0345\u034a\u0003"
        + "T*\u0005\u0346\u0347\n\u0003\u0000\u0000\u0347\u0348\u0005.\u0000\u0000"
        + "\u0348\u034a\u0003T*\u0004\u0349\u0343\u0001\u0000\u0000\u0000\u0349\u0346"
        + "\u0001\u0000\u0000\u0000\u034a\u034d\u0001\u0000\u0000\u0000\u034b\u0349"
        + "\u0001\u0000\u0000\u0000\u034b\u034c\u0001\u0000\u0000\u0000\u034cU\u0001"
        + "\u0000\u0000\u0000\u034d\u034b\u0001\u0000\u0000\u0000\u034e\u034f\u0007"
        + "\u0007\u0000\u0000\u034fW\u0001\u0000\u0000\u0000\u0350\u0351\u0007\b"
        + "\u0000\u0000\u0351Y\u0001\u0000\u0000\u0000\u0352\u0357\u0003^/\u0000"
        + "\u0353\u0354\u0005k\u0000\u0000\u0354\u0356\u0003^/\u0000\u0355\u0353"
        + "\u0001\u0000\u0000\u0000\u0356\u0359\u0001\u0000\u0000\u0000\u0357\u0355"
        + "\u0001\u0000\u0000\u0000\u0357\u0358\u0001\u0000\u0000\u0000\u0358\u035b"
        + "\u0001\u0000\u0000\u0000\u0359\u0357\u0001\u0000\u0000\u0000\u035a\u035c"
        + "\u0003\\.\u0000\u035b\u035a\u0001\u0000\u0000\u0000\u035b\u035c\u0001"
        + "\u0000\u0000\u0000\u035c\u035e\u0001\u0000\u0000\u0000\u035d\u035f\u0003"
        + "D\"\u0000\u035e\u035d\u0001\u0000\u0000\u0000\u035e\u035f\u0001\u0000"
        + "\u0000\u0000\u035f\u0361\u0001\u0000\u0000\u0000\u0360\u0362\u0003H$\u0000"
        + "\u0361\u0360\u0001\u0000\u0000\u0000\u0361\u0362\u0001\u0000\u0000\u0000"
        + "\u0362[\u0001\u0000\u0000\u0000\u0363\u0364\u0005L\u0000\u0000\u0364\u0365"
        + "\u0005M\u0000\u0000\u0365\u0366\u0005N\u0000\u0000\u0366\u0367\u0007\t"
        + "\u0000\u0000\u0367]\u0001\u0000\u0000\u0000\u0368\u036c\u0003b1\u0000"
        + "\u0369\u036b\u0003\u0090H\u0000\u036a\u0369\u0001\u0000\u0000\u0000\u036b"
        + "\u036e\u0001\u0000\u0000\u0000\u036c\u036a\u0001\u0000\u0000\u0000\u036c"
        + "\u036d\u0001\u0000\u0000\u0000\u036d\u037f\u0001\u0000\u0000\u0000\u036e"
        + "\u036c\u0001\u0000\u0000\u0000\u036f\u037f\u0003\u0098L\u0000\u0370\u0374"
        + "\u0005\u0013\u0000\u0000\u0371\u0373\u0003\u0090H\u0000\u0372\u0371\u0001"
        + "\u0000\u0000\u0000\u0373\u0376\u0001\u0000\u0000\u0000\u0374\u0372\u0001"
        + "\u0000\u0000\u0000\u0374\u0375\u0001\u0000\u0000\u0000\u0375\u037f\u0001"
        + "\u0000\u0000\u0000\u0376\u0374\u0001\u0000\u0000\u0000\u0377\u037b\u0003"
        + "\u000e\u0007\u0000\u0378\u037a\u0003\u0090H\u0000\u0379\u0378\u0001\u0000"
        + "\u0000\u0000\u037a\u037d\u0001\u0000\u0000\u0000\u037b\u0379\u0001\u0000"
        + "\u0000\u0000\u037b\u037c\u0001\u0000\u0000\u0000\u037c\u037f\u0001\u0000"
        + "\u0000\u0000\u037d\u037b\u0001\u0000\u0000\u0000\u037e\u0368\u0001\u0000"
        + "\u0000\u0000\u037e\u036f\u0001\u0000\u0000\u0000\u037e\u0370\u0001\u0000"
        + "\u0000\u0000\u037e\u0377\u0001\u0000\u0000\u0000\u037f_\u0001\u0000\u0000"
        + "\u0000\u0380\u0381\u0005F\u0000\u0000\u0381\u0382\u0007\u0006\u0000\u0000"
        + "\u0382\u0383\u0005G\u0000\u0000\u0383a\u0001\u0000\u0000\u0000\u0384\u0385"
        + "\u0007\n\u0000\u0000\u0385c\u0001\u0000\u0000\u0000\u0386\u0387\u0005"
        + "\f\u0000\u0000\u0387\u0388\u0003\b\u0004\u0000\u0388\u0389\u00054\u0000"
        + "\u0000\u0389\u038a\u0003t:\u0000\u038ae\u0001\u0000\u0000\u0000\u038b"
        + "\u038c\u0005\n\u0000\u0000\u038c\u038d\u0003\b\u0004\u0000\u038d\u038e"
        + "\u00054\u0000\u0000\u038e\u038f\u0003t:\u0000\u038fg\u0001\u0000\u0000"
        + "\u0000\u0390\u0392\u0005\u000b\u0000\u0000\u0391\u0390\u0001\u0000\u0000"
        + "\u0000\u0391\u0392\u0001\u0000\u0000\u0000\u0392\u0393\u0001\u0000\u0000"
        + "\u0000\u0393\u0394\u0003r9\u0000\u0394\u0395\u00054\u0000\u0000\u0395"
        + "\u0396\u0003t:\u0000\u0396i\u0001\u0000\u0000\u0000\u0397\u0398\u0005"
        + "\u0001\u0000\u0000\u0398\u0399\u0005o\u0000\u0000\u0399\u039a\u0003t:"
        + "\u0000\u039a\u039b\u0005k\u0000\u0000\u039b\u039c\u0003t:\u0000\u039c"
        + "\u039f\u0005p\u0000\u0000\u039d\u039e\u0005\u0005\u0000\u0000\u039e\u03a0"
        + "\u0003\b\u0004\u0000\u039f\u039d\u0001\u0000\u0000\u0000\u039f\u03a0\u0001"
        + "\u0000\u0000\u0000\u03a0k\u0001\u0000\u0000\u0000\u03a1\u03a2\u0005\u0002"
        + "\u0000\u0000\u03a2\u03a3\u0003t:\u0000\u03a3m\u0001\u0000\u0000\u0000"
        + "\u03a4\u03a5\u0005\u000f\u0000\u0000\u03a5\u03a6\u0005o\u0000\u0000\u03a6"
        + "\u03ab\u0003t:\u0000\u03a7\u03a8\u0005k\u0000\u0000\u03a8\u03aa\u0003"
        + "t:\u0000\u03a9\u03a7\u0001\u0000\u0000\u0000\u03aa\u03ad\u0001\u0000\u0000"
        + "\u0000\u03ab\u03a9\u0001\u0000\u0000\u0000\u03ab\u03ac\u0001\u0000\u0000"
        + "\u0000\u03ac\u03ae\u0001\u0000\u0000\u0000\u03ad\u03ab\u0001\u0000\u0000"
        + "\u0000\u03ae\u03af\u0005p\u0000\u0000\u03afo\u0001\u0000\u0000\u0000\u03b0"
        + "\u03b1\u0007\u000b\u0000\u0000\u03b1\u03b2\u0005o\u0000\u0000\u03b2\u03b3"
        + "\u0003t:\u0000\u03b3\u03b4\u0005k\u0000\u0000\u03b4\u03bb\u0003t:\u0000"
        + "\u03b5\u03b6\u0005k\u0000\u0000\u03b6\u03b9\u0003t:\u0000\u03b7\u03b8"
        + "\u0005k\u0000\u0000\u03b8\u03ba\u0003t:\u0000\u03b9\u03b7\u0001\u0000"
        + "\u0000\u0000\u03b9\u03ba\u0001\u0000\u0000\u0000\u03ba\u03bc\u0001\u0000"
        + "\u0000\u0000\u03bb\u03b5\u0001\u0000\u0000\u0000\u03bb\u03bc\u0001\u0000"
        + "\u0000\u0000\u03bc\u03bd\u0001\u0000\u0000\u0000\u03bd\u03c0\u0005p\u0000"
        + "\u0000\u03be\u03bf\u0005\u0005\u0000\u0000\u03bf\u03c1\u0003\b\u0004\u0000"
        + "\u03c0\u03be\u0001\u0000\u0000\u0000\u03c0\u03c1\u0001\u0000\u0000\u0000"
        + "\u03c1q\u0001\u0000\u0000\u0000\u03c2\u03cb\u0003\b\u0004\u0000\u03c3"
        + "\u03c4\u0005\u0018\u0000\u0000\u03c4\u03ca\u0003\u0096K\u0000\u03c5\u03c6"
        + "\u0005\u0014\u0000\u0000\u03c6\u03c7\u0003t:\u0000\u03c7\u03c8\u0005\u0015"
        + "\u0000\u0000\u03c8\u03ca\u0001\u0000\u0000\u0000\u03c9\u03c3\u0001\u0000"
        + "\u0000\u0000\u03c9\u03c5\u0001\u0000\u0000\u0000\u03ca\u03cd\u0001\u0000"
        + "\u0000\u0000\u03cb\u03c9\u0001\u0000\u0000\u0000\u03cb\u03cc\u0001\u0000"
        + "\u0000\u0000\u03ccs\u0001\u0000\u0000\u0000\u03cd\u03cb\u0001\u0000\u0000"
        + "\u0000\u03ce\u03cf\u0003v;\u0000\u03cfu\u0001\u0000\u0000\u0000\u03d0"
        + "\u03d5\u0003x<\u0000\u03d1\u03d2\u0005.\u0000\u0000\u03d2\u03d4\u0003"
        + "x<\u0000\u03d3\u03d1\u0001\u0000\u0000\u0000\u03d4\u03d7\u0001\u0000\u0000"
        + "\u0000\u03d5\u03d3\u0001\u0000\u0000\u0000\u03d5\u03d6\u0001\u0000\u0000"
        + "\u0000\u03d6w\u0001\u0000\u0000\u0000\u03d7\u03d5\u0001\u0000\u0000\u0000"
        + "\u03d8\u03dd\u0003z=\u0000\u03d9\u03da\u0005-\u0000\u0000\u03da\u03dc"
        + "\u0003z=\u0000\u03db\u03d9\u0001\u0000\u0000\u0000\u03dc\u03df\u0001\u0000"
        + "\u0000\u0000\u03dd\u03db\u0001\u0000\u0000\u0000\u03dd\u03de\u0001\u0000"
        + "\u0000\u0000\u03dey\u0001\u0000\u0000\u0000\u03df\u03dd\u0001\u0000\u0000"
        + "\u0000\u03e0\u03e1\u0005,\u0000\u0000\u03e1\u03e4\u0003z=\u0000\u03e2"
        + "\u03e4\u0003|>\u0000\u03e3\u03e0\u0001\u0000\u0000\u0000\u03e3\u03e2\u0001"
        + "\u0000\u0000\u0000\u03e4{\u0001\u0000\u0000\u0000\u03e5\u03e9\u0003~?"
        + "\u0000\u03e6\u03e7\u0003V+\u0000\u03e7\u03e8\u0003~?\u0000\u03e8\u03ea"
        + "\u0001\u0000\u0000\u0000\u03e9\u03e6\u0001\u0000\u0000\u0000\u03e9\u03ea"
        + "\u0001\u0000\u0000\u0000\u03ea}\u0001\u0000\u0000\u0000\u03eb\u03f0\u0003"
        + "\u0080@\u0000\u03ec\u03ed\u0005\u001c\u0000\u0000\u03ed\u03ef\u0003\u0080"
        + "@\u0000\u03ee\u03ec\u0001\u0000\u0000\u0000\u03ef\u03f2\u0001\u0000\u0000"
        + "\u0000\u03f0\u03ee\u0001\u0000\u0000\u0000\u03f0\u03f1\u0001\u0000\u0000"
        + "\u0000\u03f1\u007f\u0001\u0000\u0000\u0000\u03f2\u03f0\u0001\u0000\u0000"
        + "\u0000\u03f3\u03f8\u0003\u0082A\u0000\u03f4\u03f5\u0005\u001d\u0000\u0000"
        + "\u03f5\u03f7\u0003\u0082A\u0000\u03f6\u03f4\u0001\u0000\u0000\u0000\u03f7"
        + "\u03fa\u0001\u0000\u0000\u0000\u03f8\u03f6\u0001\u0000\u0000\u0000\u03f8"
        + "\u03f9\u0001\u0000\u0000\u0000\u03f9\u0081\u0001\u0000\u0000\u0000\u03fa"
        + "\u03f8\u0001\u0000\u0000\u0000\u03fb\u0400\u0003\u0084B\u0000\u03fc\u03fd"
        + "\u0005\u001b\u0000\u0000\u03fd\u03ff\u0003\u0084B\u0000\u03fe\u03fc\u0001"
        + "\u0000\u0000\u0000\u03ff\u0402\u0001\u0000\u0000\u0000\u0400\u03fe\u0001"
        + "\u0000\u0000\u0000\u0400\u0401\u0001\u0000\u0000\u0000\u0401\u0083\u0001"
        + "\u0000\u0000\u0000\u0402\u0400\u0001\u0000\u0000\u0000\u0403\u0408\u0003"
        + "\u0086C\u0000\u0404\u0405\u0007\f\u0000\u0000\u0405\u0407\u0003\u0086"
        + "C\u0000\u0406\u0404\u0001\u0000\u0000\u0000\u0407\u040a\u0001\u0000\u0000"
        + "\u0000\u0408\u0406\u0001\u0000\u0000\u0000\u0408\u0409\u0001\u0000\u0000"
        + "\u0000\u0409\u0085\u0001\u0000\u0000\u0000\u040a\u0408\u0001\u0000\u0000"
        + "\u0000\u040b\u0410\u0003\u0088D\u0000\u040c\u040d\u0007\r\u0000\u0000"
        + "\u040d\u040f\u0003\u0088D\u0000\u040e\u040c\u0001\u0000\u0000\u0000\u040f"
        + "\u0412\u0001\u0000\u0000\u0000\u0410\u040e\u0001\u0000\u0000\u0000\u0410"
        + "\u0411\u0001\u0000\u0000\u0000\u0411\u0087\u0001\u0000\u0000\u0000\u0412"
        + "\u0410\u0001\u0000\u0000\u0000\u0413\u041c\u0003\u008cF\u0000\u0414\u0418"
        + "\u0003\u008aE\u0000\u0415\u0418\u0005m\u0000\u0000\u0416\u0418\u0005\u001a"
        + "\u0000\u0000\u0417\u0414\u0001\u0000\u0000\u0000\u0417\u0415\u0001\u0000"
        + "\u0000\u0000\u0417\u0416\u0001\u0000\u0000\u0000\u0418\u0419\u0001\u0000"
        + "\u0000\u0000\u0419\u041b\u0003\u008cF\u0000\u041a\u0417\u0001\u0000\u0000"
        + "\u0000\u041b\u041e\u0001\u0000\u0000\u0000\u041c\u041a\u0001\u0000\u0000"
        + "\u0000\u041c\u041d\u0001\u0000\u0000\u0000\u041d\u0089\u0001\u0000\u0000"
        + "\u0000\u041e\u041c\u0001\u0000\u0000\u0000\u041f\u0420\u0004E\u0004\u0000"
        + "\u0420\u0421\u0005s\u0000\u0000\u0421\u008b\u0001\u0000\u0000\u0000\u0422"
        + "\u0423\u0007\u000e\u0000\u0000\u0423\u0426\u0003\u008cF\u0000\u0424\u0426"
        + "\u0003\u008eG\u0000\u0425\u0422\u0001\u0000\u0000\u0000\u0425\u0424\u0001"
        + "\u0000\u0000\u0000\u0426\u008d\u0001\u0000\u0000\u0000\u0427\u042b\u0003"
        + "\u0092I\u0000\u0428\u042a\u0003\u0090H\u0000\u0429\u0428\u0001\u0000\u0000"
        + "\u0000\u042a\u042d\u0001\u0000\u0000\u0000\u042b\u0429\u0001\u0000\u0000"
        + "\u0000\u042b\u042c\u0001\u0000\u0000\u0000\u042c\u008f\u0001\u0000\u0000"
        + "\u0000\u042d\u042b\u0001\u0000\u0000\u0000\u042e\u042f\u0005\u0018\u0000"
        + "\u0000\u042f\u043c\u0003\u0096K\u0000\u0430\u0439\u0005o\u0000\u0000\u0431"
        + "\u0436\u0003t:\u0000\u0432\u0433\u0005k\u0000\u0000\u0433\u0435\u0003"
        + "t:\u0000\u0434\u0432\u0001\u0000\u0000\u0000\u0435\u0438\u0001\u0000\u0000"
        + "\u0000\u0436\u0434\u0001\u0000\u0000\u0000\u0436\u0437\u0001\u0000\u0000"
        + "\u0000\u0437\u043a\u0001\u0000\u0000\u0000\u0438\u0436\u0001\u0000\u0000"
        + "\u0000\u0439\u0431\u0001\u0000\u0000\u0000\u0439\u043a\u0001\u0000\u0000"
        + "\u0000\u043a\u043b\u0001\u0000\u0000\u0000\u043b\u043d\u0005p\u0000\u0000"
        + "\u043c\u0430\u0001\u0000\u0000\u0000\u043c\u043d\u0001\u0000\u0000\u0000"
        + "\u043d\u0443\u0001\u0000\u0000\u0000\u043e\u043f\u0005\u0014\u0000\u0000"
        + "\u043f\u0440\u0003t:\u0000\u0440\u0441\u0005\u0015\u0000\u0000\u0441\u0443"
        + "\u0001\u0000\u0000\u0000\u0442\u042e\u0001\u0000\u0000\u0000\u0442\u043e"
        + "\u0001\u0000\u0000\u0000\u0443\u0091\u0001\u0000\u0000\u0000\u0444\u046e"
        + "\u0005r\u0000\u0000\u0445\u046e\u0005t\u0000\u0000\u0446\u046e\u0005\u0013"
        + "\u0000\u0000\u0447\u046e\u0005*\u0000\u0000\u0448\u046e\u0005+\u0000\u0000"
        + "\u0449\u044a\u0005o\u0000\u0000\u044a\u044b\u0003t:\u0000\u044b\u044c"
        + "\u0005p\u0000\u0000\u044c\u046e\u0001\u0000\u0000\u0000\u044d\u046e\u0003"
        + "\u000e\u0007\u0000\u044e\u046e\u0005s\u0000\u0000\u044f\u045b\u0005\u0014"
        + "\u0000\u0000\u0450\u0455\u0003t:\u0000\u0451\u0452\u0005k\u0000\u0000"
        + "\u0452\u0454\u0003t:\u0000\u0453\u0451\u0001\u0000\u0000\u0000\u0454\u0457"
        + "\u0001\u0000\u0000\u0000\u0455\u0453\u0001\u0000\u0000\u0000\u0455\u0456"
        + "\u0001\u0000\u0000\u0000\u0456\u0459\u0001\u0000\u0000\u0000\u0457\u0455"
        + "\u0001\u0000\u0000\u0000\u0458\u045a\u0005k\u0000\u0000\u0459\u0458\u0001"
        + "\u0000\u0000\u0000\u0459\u045a\u0001\u0000\u0000\u0000\u045a\u045c\u0001"
        + "\u0000\u0000\u0000\u045b\u0450\u0001\u0000\u0000\u0000\u045b\u045c\u0001"
        + "\u0000\u0000\u0000\u045c\u045d\u0001\u0000\u0000\u0000\u045d\u046e\u0005"
        + "\u0015\u0000\u0000\u045e\u046a\u0005\u0016\u0000\u0000\u045f\u0464\u0003"
        + "\u0094J\u0000\u0460\u0461\u0005k\u0000\u0000\u0461\u0463\u0003\u0094J"
        + "\u0000\u0462\u0460\u0001\u0000\u0000\u0000\u0463\u0466\u0001\u0000\u0000"
        + "\u0000\u0464\u0462\u0001\u0000\u0000\u0000\u0464\u0465\u0001\u0000\u0000"
        + "\u0000\u0465\u0468\u0001\u0000\u0000\u0000\u0466\u0464\u0001\u0000\u0000"
        + "\u0000\u0467\u0469\u0005k\u0000\u0000\u0468\u0467\u0001\u0000\u0000\u0000"
        + "\u0468\u0469\u0001\u0000\u0000\u0000\u0469\u046b\u0001\u0000\u0000\u0000"
        + "\u046a\u045f\u0001\u0000\u0000\u0000\u046a\u046b\u0001\u0000\u0000\u0000"
        + "\u046b\u046c\u0001\u0000\u0000\u0000\u046c\u046e\u0005\u0017\u0000\u0000"
        + "\u046d\u0444\u0001\u0000\u0000\u0000\u046d\u0445\u0001\u0000\u0000\u0000"
        + "\u046d\u0446\u0001\u0000\u0000\u0000\u046d\u0447\u0001\u0000\u0000\u0000"
        + "\u046d\u0448\u0001\u0000\u0000\u0000\u046d\u0449\u0001\u0000\u0000\u0000"
        + "\u046d\u044d\u0001\u0000\u0000\u0000\u046d\u044e\u0001\u0000\u0000\u0000"
        + "\u046d\u044f\u0001\u0000\u0000\u0000\u046d\u045e\u0001\u0000\u0000\u0000"
        + "\u046e\u0093\u0001\u0000\u0000\u0000\u046f\u0470\u0003\u0096K\u0000\u0470"
        + "\u0471\u0005l\u0000\u0000\u0471\u0472\u0003t:\u0000\u0472\u0095\u0001"
        + "\u0000\u0000\u0000\u0473\u0474\u0007\u000f\u0000\u0000\u0474\u0097\u0001"
        + "\u0000\u0000\u0000\u0475\u0476\u0005t\u0000\u0000\u0476\u0099\u0001\u0000"
        + "\u0000\u0000\u0477\u0478\u0005r\u0000\u0000\u0478\u009b\u0001\u0000\u0000"
        + "\u0000\u009e\u009d\u00a1\u00a4\u00a8\u00ad\u00af\u00bc\u00bf\u00cc\u00d6"
        + "\u00d9\u00e4\u00e6\u00ed\u00f1\u00fa\u0105\u0108\u010b\u0112\u0120\u0123"
        + "\u012a\u012e\u013f\u014c\u015b\u015d\u0160\u0163\u0167\u016d\u0170\u0175"
        + "\u0187\u018a\u018e\u0193\u0197\u019c\u01a1\u01a6\u01a8\u01ac\u01b2\u01b6"
        + "\u01ba\u01bd\u01c1\u01c6\u01cb\u01ce\u01d0\u01d4\u01d7\u01db\u01de\u01e3"
        + "\u01e6\u01eb\u01ee\u01f0\u01fb\u01ff\u0202\u0206\u020a\u020d\u0214\u021b"
        + "\u021e\u0226\u0229\u0231\u0235\u0239\u023b\u023d\u023f\u0242\u0249\u024d"
        + "\u0254\u0258\u025e\u0269\u026c\u026f\u0277\u0279\u0283\u028b\u028e\u0297"
        + "\u029c\u02a8\u02ae\u02bc\u02c1\u02d3\u02db\u02dd\u02e7\u02f1\u02ff\u0301"
        + "\u030d\u031d\u0327\u032a\u032d\u0331\u0335\u033d\u033f\u0341\u0349\u034b"
        + "\u0357\u035b\u035e\u0361\u036c\u0374\u037b\u037e\u0391\u039f\u03ab\u03b9"
        + "\u03bb\u03c0\u03c9\u03cb\u03d5\u03dd\u03e3\u03e9\u03f0\u03f8\u0400\u0408"
        + "\u0410\u0417\u041c\u0425\u042b\u0436\u0439\u043c\u0442\u0455\u0459\u045b"
        + "\u0464\u0468\u046a\u046d";
    public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
