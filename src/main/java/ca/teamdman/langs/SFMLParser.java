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
    public static final int TRANSFER = 1, INTO = 2, FUNCTION = 3, RETURNS = 4, RETURN = 5, LET = 6, SET = 7, STATE = 8,
        RISING = 9, FALLING = 10, PRINT = 11, FOR = 12, WHILE = 13, STEP = 14, VARIABLE = 15, LBRACKET = 16,
        RBRACKET = 17, LBRACE = 18, RBRACE = 19, DOT = 20, PERCENT = 21, BIT_AND = 22, BIT_OR = 23, BIT_XOR = 24,
        BIT_NOT = 25, SHIFT_LEFT = 26, SHIFT_RIGHT = 27, SHIFT_UNSIGNED_RIGHT = 28, IF = 29, THEN = 30, ELSE = 31,
        HAS = 32, OVERALL = 33, SOME = 34, ONE = 35, LONE = 36, TRUE = 37, FALSE = 38, NOT = 39, AND = 40, OR = 41,
        GT = 42, GT_SYMBOL = 43, LT = 44, LT_SYMBOL = 45, EQ = 46, EQ_SYMBOL = 47, EQ_ALIAS = 48, NE_SYMBOL = 49,
        LE = 50, LE_SYMBOL = 51, GE = 52, GE_SYMBOL = 53, FROM = 54, TO = 55, INPUT = 56, OUTPUT = 57, WHERE = 58,
        SLOTS = 59, SLOT = 60, RETAIN = 61, EACH = 62, EXCEPT = 63, FORGET = 64, EMPTY = 65, IN = 66, WITHOUT = 67,
        WITH = 68, TAG = 69, HASHTAG = 70, ROUND = 71, ROBIN = 72, BY = 73, LABEL = 74, BLOCK = 75, TOP = 76,
        BOTTOM = 77, NORTH = 78, EAST = 79, SOUTH = 80, WEST = 81, SIDE = 82, LEFT = 83, RIGHT = 84, FRONT = 85,
        BACK = 86, NULL = 87, TICKS = 88, TICK = 89, SECONDS = 90, SECOND = 91, GLOBAL = 92, PLUS = 93, REDSTONE = 94,
        LEVEL = 95, LEVELS = 96, PULSE = 97, DO = 98, END = 99, NAME = 100, EVERY = 101, COMMA = 102, COLON = 103,
        SLASH = 104, DASH = 105, LPAREN = 106, RPAREN = 107, NUMBER_WITH_G_SUFFIX = 108, NUMBER = 109, IDENTIFIER = 110,
        STRING = 111, LINE_COMMENT = 112, WS = 113, UNUSED = 114;
    public static final int RULE_program = 0, RULE_functionDefinition = 1, RULE_functionName = 2,
        RULE_functionParameter = 3, RULE_dataType = 4, RULE_typeField = 5, RULE_functionCall = 6,
        RULE_returnStatement = 7, RULE_name = 8, RULE_trigger = 9, RULE_interval = 10, RULE_block = 11,
        RULE_statement = 12, RULE_redstoneOutputStatement = 13, RULE_forgetStatement = 14, RULE_inputStatement = 15,
        RULE_outputStatement = 16, RULE_inputResourceLimits = 17, RULE_outputResourceLimits = 18,
        RULE_resourceLimitList = 19, RULE_resourceLimit = 20, RULE_limit = 21, RULE_quantity = 22, RULE_retention = 23,
        RULE_resourceExclusion = 24, RULE_resourceId = 25, RULE_resourceIdList = 26, RULE_resourceIdDisjunction = 27,
        RULE_with = 28, RULE_withClause = 29, RULE_tagMatcher = 30, RULE_sidequalifier = 31, RULE_side = 32,
        RULE_slotqualifier = 33, RULE_rangeset = 34, RULE_range = 35, RULE_ifStatement = 36, RULE_forStatement = 37,
        RULE_whileStatement = 38, RULE_boolexpr = 39, RULE_comparisonOp = 40, RULE_setOp = 41, RULE_labelAccess = 42,
        RULE_roundrobin = 43, RULE_label = 44, RULE_emptyslots = 45, RULE_identifier = 46, RULE_stateDeclaration = 47,
        RULE_dataDeclaration = 48, RULE_dataAssignment = 49, RULE_printStatement = 50, RULE_transferStatement = 51,
        RULE_dataTarget = 52, RULE_dataExpression = 53, RULE_dataOr = 54, RULE_dataAnd = 55, RULE_dataNot = 56,
        RULE_dataComparison = 57, RULE_dataBitOr = 58, RULE_dataBitXor = 59, RULE_dataBitAnd = 60, RULE_dataShift = 61,
        RULE_dataAdd = 62, RULE_dataMultiply = 63, RULE_multiplyOperator = 64, RULE_dataSign = 65,
        RULE_dataPostfix = 66, RULE_dataPrimary = 67, RULE_dataField = 68, RULE_dataName = 69, RULE_string = 70,
        RULE_number = 71;

    private static String[] makeRuleNames() {
        return new String[] { "program", "functionDefinition", "functionName", "functionParameter", "dataType",
            "typeField", "functionCall", "returnStatement", "name", "trigger", "interval", "block", "statement",
            "redstoneOutputStatement", "forgetStatement", "inputStatement", "outputStatement", "inputResourceLimits",
            "outputResourceLimits", "resourceLimitList", "resourceLimit", "limit", "quantity", "retention",
            "resourceExclusion", "resourceId", "resourceIdList", "resourceIdDisjunction", "with", "withClause",
            "tagMatcher", "sidequalifier", "side", "slotqualifier", "rangeset", "range", "ifStatement", "forStatement",
            "whileStatement", "boolexpr", "comparisonOp", "setOp", "labelAccess", "roundrobin", "label", "emptyslots",
            "identifier", "stateDeclaration", "dataDeclaration", "dataAssignment", "printStatement",
            "transferStatement", "dataTarget", "dataExpression", "dataOr", "dataAnd", "dataNot", "dataComparison",
            "dataBitOr", "dataBitXor", "dataBitAnd", "dataShift", "dataAdd", "dataMultiply", "multiplyOperator",
            "dataSign", "dataPostfix", "dataPrimary", "dataField", "dataName", "string", "number" };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, "'['", "']'", "'{'", "'}'", "'.'", "'%'", "'&'", "'|'", "'^'", "'~'", "'<<'", "'>>'", "'>>>'", null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, "'>'", null, "'<'", null,
            "'='", "'=='", "'!='", null, "'<='", null, "'>='", null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, "'#'", null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, "','", "':'", "'/'", "'-'", "'('", "')'" };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[] { null, "TRANSFER", "INTO", "FUNCTION", "RETURNS", "RETURN", "LET", "SET", "STATE",
            "RISING", "FALLING", "PRINT", "FOR", "WHILE", "STEP", "VARIABLE", "LBRACKET", "RBRACKET", "LBRACE",
            "RBRACE", "DOT", "PERCENT", "BIT_AND", "BIT_OR", "BIT_XOR", "BIT_NOT", "SHIFT_LEFT", "SHIFT_RIGHT",
            "SHIFT_UNSIGNED_RIGHT", "IF", "THEN", "ELSE", "HAS", "OVERALL", "SOME", "ONE", "LONE", "TRUE", "FALSE",
            "NOT", "AND", "OR", "GT", "GT_SYMBOL", "LT", "LT_SYMBOL", "EQ", "EQ_SYMBOL", "EQ_ALIAS", "NE_SYMBOL", "LE",
            "LE_SYMBOL", "GE", "GE_SYMBOL", "FROM", "TO", "INPUT", "OUTPUT", "WHERE", "SLOTS", "SLOT", "RETAIN", "EACH",
            "EXCEPT", "FORGET", "EMPTY", "IN", "WITHOUT", "WITH", "TAG", "HASHTAG", "ROUND", "ROBIN", "BY", "LABEL",
            "BLOCK", "TOP", "BOTTOM", "NORTH", "EAST", "SOUTH", "WEST", "SIDE", "LEFT", "RIGHT", "FRONT", "BACK",
            "NULL", "TICKS", "TICK", "SECONDS", "SECOND", "GLOBAL", "PLUS", "REDSTONE", "LEVEL", "LEVELS", "PULSE",
            "DO", "END", "NAME", "EVERY", "COMMA", "COLON", "SLASH", "DASH", "LPAREN", "RPAREN", "NUMBER_WITH_G_SUFFIX",
            "NUMBER", "IDENTIFIER", "STRING", "LINE_COMMENT", "WS", "UNUSED" };
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
            enterOuterAlt(_localctx, 1);
            {
                setState(145);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == NAME) {
                    {
                        setState(144);
                        name();
                    }
                }

                setState(151);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LET || _la == STATE) {
                    {
                        setState(149);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case STATE: {
                                setState(147);
                                stateDeclaration();
                            }
                                break;
                            case LET: {
                                setState(148);
                                dataDeclaration();
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                    }
                    setState(153);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(157);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == FUNCTION) {
                    {
                        {
                            setState(154);
                            functionDefinition();
                        }
                    }
                    setState(159);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(163);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == EVERY) {
                    {
                        {
                            setState(160);
                            trigger();
                        }
                    }
                    setState(165);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(166);
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
            enterOuterAlt(_localctx, 1);
            {
                setState(168);
                match(FUNCTION);
                setState(169);
                functionName();
                setState(170);
                match(LPAREN);
                setState(179);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == VARIABLE) {
                    {
                        setState(171);
                        functionParameter();
                        setState(176);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(172);
                                    match(COMMA);
                                    setState(173);
                                    functionParameter();
                                }
                            }
                            setState(178);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(181);
                match(RPAREN);
                setState(182);
                match(RETURNS);
                setState(183);
                dataType();
                setState(184);
                match(DO);
                setState(185);
                block();
                setState(186);
                match(END);
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
                setState(188);
                dataName();
                setState(193);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == DOT) {
                    {
                        {
                            setState(189);
                            match(DOT);
                            setState(190);
                            dataName();
                        }
                    }
                    setState(195);
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

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
        }

        public TerminalNode COLON() {
            return getToken(SFMLParser.COLON, 0);
        }

        public DataTypeContext dataType() {
            return getRuleContext(DataTypeContext.class, 0);
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
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(196);
                match(VARIABLE);
                setState(197);
                match(COLON);
                setState(198);
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
        enterRule(_localctx, 8, RULE_dataType);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(213);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case TRANSFER:
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
                        setState(200);
                        dataName();
                    }
                        break;
                    case LBRACE: {
                        setState(201);
                        match(LBRACE);
                        setState(210);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -12288142488862722L) != 0)
                            || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 70643622084543L) != 0)) {
                            {
                                setState(202);
                                typeField();
                                setState(207);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(203);
                                            match(COMMA);
                                            setState(204);
                                            typeField();
                                        }
                                    }
                                    setState(209);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                        }

                        setState(212);
                        match(RBRACE);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(220);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACKET) {
                    {
                        {
                            setState(215);
                            match(LBRACKET);
                            setState(216);
                            match(NUMBER);
                            setState(217);
                            match(RBRACKET);
                        }
                    }
                    setState(222);
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
        enterRule(_localctx, 10, RULE_typeField);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(223);
                dataName();
                setState(224);
                match(COLON);
                setState(225);
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
        enterRule(_localctx, 12, RULE_functionCall);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(227);
                functionName();
                setState(228);
                match(LPAREN);
                setState(237);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -12288142454947842L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 253162552295359L) != 0)) {
                    {
                        setState(229);
                        dataExpression();
                        setState(234);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(230);
                                    match(COMMA);
                                    setState(231);
                                    dataExpression();
                                }
                            }
                            setState(236);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(239);
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
        enterRule(_localctx, 14, RULE_returnStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(241);
                match(RETURN);
                setState(242);
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
        enterRule(_localctx, 16, RULE_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(244);
                match(NAME);
                setState(245);
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

        public TerminalNode DO() {
            return getToken(SFMLParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode END() {
            return getToken(SFMLParser.END, 0);
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
        enterRule(_localctx, 18, RULE_trigger);
        int _la;
        try {
            setState(269);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 14, _ctx)) {
                case 1:
                    _localctx = new TimerTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(247);
                    match(EVERY);
                    setState(248);
                    interval();
                    setState(249);
                    match(DO);
                    setState(250);
                    block();
                    setState(251);
                    match(END);
                }
                    break;
                case 2:
                    _localctx = new PulseTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(253);
                    match(EVERY);
                    setState(254);
                    match(REDSTONE);
                    setState(255);
                    match(PULSE);
                    setState(256);
                    match(DO);
                    setState(257);
                    block();
                    setState(258);
                    match(END);
                }
                    break;
                case 3:
                    _localctx = new NamedRedstoneTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(260);
                    match(EVERY);
                    setState(261);
                    match(REDSTONE);
                    setState(262);
                    _la = _input.LA(1);
                    if (!(_la == RISING || _la == FALLING || _la == PULSE)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(263);
                    match(FROM);
                    setState(264);
                    labelAccess();
                    setState(265);
                    match(DO);
                    setState(266);
                    block();
                    setState(267);
                    match(END);
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
        enterRule(_localctx, 20, RULE_interval);
        int _la;
        try {
            setState(288);
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
                    setState(272);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == NUMBER) {
                        {
                            setState(271);
                            match(NUMBER);
                        }
                    }

                    setState(275);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == GLOBAL) {
                        {
                            setState(274);
                            match(GLOBAL);
                        }
                    }

                    setState(279);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(277);
                            match(PLUS);
                            setState(278);
                            match(NUMBER);
                        }
                    }

                    setState(281);
                    _la = _input.LA(1);
                    if (!(((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 15L) != 0))) {
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
                    setState(282);
                    match(NUMBER_WITH_G_SUFFIX);
                    setState(285);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(283);
                            match(PLUS);
                            setState(284);
                            match(NUMBER);
                        }
                    }

                    setState(287);
                    _la = _input.LA(1);
                    if (!(((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 15L) != 0))) {
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
        enterRule(_localctx, 22, RULE_block);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(293);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (((((_la - 1)) & ~0x3f) == 0 && ((1L << (_la - 1)) & -9088264047765201807L) != 0)) {
                    {
                        {
                            setState(290);
                            statement();
                        }
                    }
                    setState(295);
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
    public static class StatementContext extends ParserRuleContext {

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
        enterRule(_localctx, 24, RULE_statement);
        try {
            setState(308);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 21, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(296);
                    dataDeclaration();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(297);
                    dataAssignment();
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(298);
                    printStatement();
                }
                    break;
                case 4:
                    enterOuterAlt(_localctx, 4); {
                    setState(299);
                    transferStatement();
                }
                    break;
                case 5:
                    enterOuterAlt(_localctx, 5); {
                    setState(300);
                    returnStatement();
                }
                    break;
                case 6:
                    enterOuterAlt(_localctx, 6); {
                    setState(301);
                    inputStatement();
                }
                    break;
                case 7:
                    enterOuterAlt(_localctx, 7); {
                    setState(302);
                    redstoneOutputStatement();
                }
                    break;
                case 8:
                    enterOuterAlt(_localctx, 8); {
                    setState(303);
                    outputStatement();
                }
                    break;
                case 9:
                    enterOuterAlt(_localctx, 9); {
                    setState(304);
                    ifStatement();
                }
                    break;
                case 10:
                    enterOuterAlt(_localctx, 10); {
                    setState(305);
                    forStatement();
                }
                    break;
                case 11:
                    enterOuterAlt(_localctx, 11); {
                    setState(306);
                    whileStatement();
                }
                    break;
                case 12:
                    enterOuterAlt(_localctx, 12); {
                    setState(307);
                    forgetStatement();
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
        enterRule(_localctx, 26, RULE_redstoneOutputStatement);
        int _la;
        try {
            setState(338);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(310);
                    match(OUTPUT);
                    setState(312);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(311);
                            quantity();
                        }
                    }

                    setState(314);
                    match(REDSTONE);
                    setState(315);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(317);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(316);
                            quantity();
                        }
                    }

                    setState(319);
                    match(TO);
                    setState(321);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(320);
                            match(EACH);
                        }
                    }

                    setState(323);
                    labelAccess();
                }
                    break;
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(324);
                    match(TO);
                    setState(326);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(325);
                            match(EACH);
                        }
                    }

                    setState(328);
                    labelAccess();
                    setState(329);
                    match(OUTPUT);
                    setState(331);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(330);
                            quantity();
                        }
                    }

                    setState(333);
                    match(REDSTONE);
                    setState(334);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(336);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(335);
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
        enterRule(_localctx, 28, RULE_forgetStatement);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(340);
                match(FORGET);
                setState(342);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 29, _ctx)) {
                    case 1: {
                        setState(341);
                        label();
                    }
                        break;
                }
                setState(348);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 30, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(344);
                                match(COMMA);
                                setState(345);
                                label();
                            }
                        }
                    }
                    setState(350);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 30, _ctx);
                }
                setState(352);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(351);
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
        enterRule(_localctx, 30, RULE_inputStatement);
        int _la;
        try {
            setState(378);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case INPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(354);
                    match(INPUT);
                    setState(356);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2305843009213726718L) != 0)
                        || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 31337080620547L) != 0)) {
                        {
                            setState(355);
                            inputResourceLimits();
                        }
                    }

                    setState(359);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(358);
                            resourceExclusion();
                        }
                    }

                    setState(361);
                    match(FROM);
                    setState(363);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(362);
                            match(EACH);
                        }
                    }

                    setState(365);
                    labelAccess();
                }
                    break;
                case FROM:
                    enterOuterAlt(_localctx, 2); {
                    setState(366);
                    match(FROM);
                    setState(368);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(367);
                            match(EACH);
                        }
                    }

                    setState(370);
                    labelAccess();
                    setState(371);
                    match(INPUT);
                    setState(373);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 36, _ctx)) {
                        case 1: {
                            setState(372);
                            inputResourceLimits();
                        }
                            break;
                    }
                    setState(376);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(375);
                            resourceExclusion();
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
        enterRule(_localctx, 32, RULE_outputStatement);
        int _la;
        try {
            setState(410);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(380);
                    match(OUTPUT);
                    setState(382);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2305843009213726718L) != 0)
                        || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 31337080620547L) != 0)) {
                        {
                            setState(381);
                            outputResourceLimits();
                        }
                    }

                    setState(385);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(384);
                            resourceExclusion();
                        }
                    }

                    setState(387);
                    match(TO);
                    setState(389);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EMPTY) {
                        {
                            setState(388);
                            emptyslots();
                        }
                    }

                    setState(392);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(391);
                            match(EACH);
                        }
                    }

                    setState(394);
                    labelAccess();
                }
                    break;
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(395);
                    match(TO);
                    setState(397);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EMPTY) {
                        {
                            setState(396);
                            emptyslots();
                        }
                    }

                    setState(400);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(399);
                            match(EACH);
                        }
                    }

                    setState(402);
                    labelAccess();
                    setState(403);
                    match(OUTPUT);
                    setState(405);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 45, _ctx)) {
                        case 1: {
                            setState(404);
                            outputResourceLimits();
                        }
                            break;
                    }
                    setState(408);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(407);
                            resourceExclusion();
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
        enterRule(_localctx, 34, RULE_inputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(412);
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
        enterRule(_localctx, 36, RULE_outputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(414);
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
        enterRule(_localctx, 38, RULE_resourceLimitList);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(416);
                resourceLimit();
                setState(421);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 48, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(417);
                                match(COMMA);
                                setState(418);
                                resourceLimit();
                            }
                        }
                    }
                    setState(423);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 48, _ctx);
                }
                setState(425);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(424);
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
        enterRule(_localctx, 40, RULE_resourceLimit);
        int _la;
        try {
            setState(439);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 53, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(428);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (((((_la - 61)) & ~0x3f) == 0 && ((1L << (_la - 61)) & 316659348799489L) != 0)) {
                        {
                            setState(427);
                            limit();
                        }
                    }

                    setState(430);
                    resourceIdDisjunction();
                    setState(432);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == WITHOUT || _la == WITH) {
                        {
                            setState(431);
                            with();
                        }
                    }

                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(434);
                    limit();
                    setState(436);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == WITHOUT || _la == WITH) {
                        {
                            setState(435);
                            with();
                        }
                    }

                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(438);
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
        enterRule(_localctx, 42, RULE_limit);
        try {
            setState(446);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 54, _ctx)) {
                case 1:
                    _localctx = new QuantityRetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(441);
                    quantity();
                    setState(442);
                    retention();
                }
                    break;
                case 2:
                    _localctx = new RetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(444);
                    retention();
                }
                    break;
                case 3:
                    _localctx = new QuantityLimitContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(445);
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
        enterRule(_localctx, 44, RULE_quantity);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(453);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(448);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(449);
                        match(LPAREN);
                        setState(450);
                        dataExpression();
                        setState(451);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(456);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == EACH) {
                    {
                        setState(455);
                        match(EACH);
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
        enterRule(_localctx, 46, RULE_retention);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(458);
                match(RETAIN);
                setState(464);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(459);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(460);
                        match(LPAREN);
                        setState(461);
                        dataExpression();
                        setState(462);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(467);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == EACH) {
                    {
                        setState(466);
                        match(EACH);
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
        enterRule(_localctx, 48, RULE_resourceExclusion);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(469);
                match(EXCEPT);
                setState(470);
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
        enterRule(_localctx, 50, RULE_resourceId);
        try {
            setState(492);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case TRANSFER:
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
                        setState(472);
                        identifier();
                    }
                    setState(489);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 64, _ctx)) {
                        case 1: {
                            setState(473);
                            match(COLON);
                            setState(475);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 59, _ctx)) {
                                case 1: {
                                    setState(474);
                                    identifier();
                                }
                                    break;
                            }
                            setState(487);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 63, _ctx)) {
                                case 1: {
                                    setState(477);
                                    match(COLON);
                                    setState(479);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 60, _ctx)) {
                                        case 1: {
                                            setState(478);
                                            identifier();
                                        }
                                            break;
                                    }
                                    setState(485);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 62, _ctx)) {
                                        case 1: {
                                            setState(481);
                                            match(COLON);
                                            setState(483);
                                            _errHandler.sync(this);
                                            switch (getInterpreter().adaptivePredict(_input, 61, _ctx)) {
                                                case 1: {
                                                    setState(482);
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
                    setState(491);
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
        enterRule(_localctx, 52, RULE_resourceIdList);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(494);
                resourceId();
                setState(499);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 66, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(495);
                                match(COMMA);
                                setState(496);
                                resourceId();
                            }
                        }
                    }
                    setState(501);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 66, _ctx);
                }
                setState(503);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 67, _ctx)) {
                    case 1: {
                        setState(502);
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
        enterRule(_localctx, 54, RULE_resourceIdDisjunction);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(505);
                resourceId();
                setState(510);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 68, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(506);
                                match(OR);
                                setState(507);
                                resourceId();
                            }
                        }
                    }
                    setState(512);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 68, _ctx);
                }
                setState(514);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 69, _ctx)) {
                    case 1: {
                        setState(513);
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
        enterRule(_localctx, 56, RULE_with);
        try {
            setState(520);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case WITH:
                    enterOuterAlt(_localctx, 1); {
                    setState(516);
                    match(WITH);
                    setState(517);
                    withClause(0);
                }
                    break;
                case WITHOUT:
                    enterOuterAlt(_localctx, 2); {
                    setState(518);
                    match(WITHOUT);
                    setState(519);
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
        int _startState = 58;
        enterRecursionRule(_localctx, 58, RULE_withClause, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(537);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case LPAREN: {
                        _localctx = new WithParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(523);
                        match(LPAREN);
                        setState(524);
                        withClause(0);
                        setState(525);
                        match(RPAREN);
                    }
                        break;
                    case NOT: {
                        _localctx = new WithNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(527);
                        match(NOT);
                        setState(528);
                        withClause(4);
                    }
                        break;
                    case TAG:
                    case HASHTAG: {
                        _localctx = new WithTagContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(534);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case TAG: {
                                setState(529);
                                match(TAG);
                                setState(531);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                if (_la == HASHTAG) {
                                    {
                                        setState(530);
                                        match(HASHTAG);
                                    }
                                }

                            }
                                break;
                            case HASHTAG: {
                                setState(533);
                                match(HASHTAG);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(536);
                        tagMatcher();
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(547);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 75, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(545);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 74, _ctx)) {
                                case 1: {
                                    _localctx = new WithConjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(539);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(540);
                                    match(AND);
                                    setState(541);
                                    withClause(4);
                                }
                                    break;
                                case 2: {
                                    _localctx = new WithDisjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(542);
                                    if (!(precpred(_ctx, 2)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                                    setState(543);
                                    match(OR);
                                    setState(544);
                                    withClause(3);
                                }
                                    break;
                            }
                        }
                    }
                    setState(549);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 75, _ctx);
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
        enterRule(_localctx, 60, RULE_tagMatcher);
        try {
            int _alt;
            setState(568);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 78, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(550);
                    identifier();
                    setState(551);
                    match(COLON);
                    setState(552);
                    identifier();
                    setState(557);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 76, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(553);
                                    match(SLASH);
                                    setState(554);
                                    identifier();
                                }
                            }
                        }
                        setState(559);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 76, _ctx);
                    }
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(560);
                    identifier();
                    setState(565);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 77, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(561);
                                    match(SLASH);
                                    setState(562);
                                    identifier();
                                }
                            }
                        }
                        setState(567);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 77, _ctx);
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
        enterRule(_localctx, 62, RULE_sidequalifier);
        int _la;
        try {
            setState(582);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case EACH:
                    _localctx = new EachSideContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(570);
                    match(EACH);
                    setState(571);
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
                    setState(572);
                    side();
                    setState(577);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(573);
                                match(COMMA);
                                setState(574);
                                side();
                            }
                        }
                        setState(579);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(580);
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
        enterRule(_localctx, 64, RULE_side);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(584);
                _la = _input.LA(1);
                if (!(((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76)) & 4031L) != 0))) {
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
        enterRule(_localctx, 66, RULE_slotqualifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(586);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(587);
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
        enterRule(_localctx, 68, RULE_rangeset);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(589);
                range();
                setState(594);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(590);
                            match(COMMA);
                            setState(591);
                            range();
                        }
                    }
                    setState(596);
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
        enterRule(_localctx, 70, RULE_range);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(597);
                number();
                setState(600);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == DASH) {
                    {
                        setState(598);
                        match(DASH);
                        setState(599);
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
        enterRule(_localctx, 72, RULE_ifStatement);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(602);
                match(IF);
                setState(603);
                boolexpr(0);
                setState(604);
                match(THEN);
                setState(605);
                block();
                setState(614);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 83, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(606);
                                match(ELSE);
                                setState(607);
                                match(IF);
                                setState(608);
                                boolexpr(0);
                                setState(609);
                                match(THEN);
                                setState(610);
                                block();
                            }
                        }
                    }
                    setState(616);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 83, _ctx);
                }
                setState(619);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == ELSE) {
                    {
                        setState(617);
                        match(ELSE);
                        setState(618);
                        block();
                    }
                }

                setState(621);
                match(END);
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

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
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

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
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
        enterRule(_localctx, 74, RULE_forStatement);
        int _la;
        try {
            setState(645);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 86, _ctx)) {
                case 1:
                    _localctx = new CountedForContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(623);
                    match(FOR);
                    setState(624);
                    match(VARIABLE);
                    setState(625);
                    match(EQ_SYMBOL);
                    setState(626);
                    dataExpression();
                    setState(627);
                    match(TO);
                    setState(628);
                    dataExpression();
                    setState(631);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == STEP) {
                        {
                            setState(629);
                            match(STEP);
                            setState(630);
                            dataExpression();
                        }
                    }

                    setState(633);
                    match(DO);
                    setState(634);
                    block();
                    setState(635);
                    match(END);
                }
                    break;
                case 2:
                    _localctx = new ArrayForContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(637);
                    match(FOR);
                    setState(638);
                    match(VARIABLE);
                    setState(639);
                    match(IN);
                    setState(640);
                    dataExpression();
                    setState(641);
                    match(DO);
                    setState(642);
                    block();
                    setState(643);
                    match(END);
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
        enterRule(_localctx, 76, RULE_whileStatement);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(647);
                match(WHILE);
                setState(648);
                boolexpr(0);
                setState(649);
                match(DO);
                setState(650);
                block();
                setState(651);
                match(END);
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
        int _startState = 78;
        enterRecursionRule(_localctx, 78, RULE_boolexpr, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(703);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 95, _ctx)) {
                    case 1: {
                        _localctx = new BooleanTrueContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(654);
                        match(TRUE);
                    }
                        break;
                    case 2: {
                        _localctx = new BooleanFalseContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(655);
                        match(FALSE);
                    }
                        break;
                    case 3: {
                        _localctx = new BooleanParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(656);
                        match(LPAREN);
                        setState(657);
                        boolexpr(0);
                        setState(658);
                        match(RPAREN);
                    }
                        break;
                    case 4: {
                        _localctx = new BooleanDataContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(660);
                        match(LPAREN);
                        setState(661);
                        dataExpression();
                        setState(662);
                        match(RPAREN);
                    }
                        break;
                    case 5: {
                        _localctx = new BooleanNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(664);
                        match(NOT);
                        setState(665);
                        boolexpr(5);
                    }
                        break;
                    case 6: {
                        _localctx = new BooleanHasContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(667);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4611686147276406784L) != 0) || _la == EVERY) {
                            {
                                setState(666);
                                setOp();
                            }
                        }

                        setState(669);
                        labelAccess();
                        setState(670);
                        match(HAS);
                        setState(671);
                        comparisonOp();
                        setState(677);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case NUMBER: {
                                setState(672);
                                number();
                            }
                                break;
                            case LPAREN: {
                                setState(673);
                                match(LPAREN);
                                setState(674);
                                dataExpression();
                                setState(675);
                                match(RPAREN);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(680);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 89, _ctx)) {
                            case 1: {
                                setState(679);
                                resourceIdDisjunction();
                            }
                                break;
                        }
                        setState(683);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 90, _ctx)) {
                            case 1: {
                                setState(682);
                                with();
                            }
                                break;
                        }
                        setState(687);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 91, _ctx)) {
                            case 1: {
                                setState(685);
                                match(EXCEPT);
                                setState(686);
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
                        setState(689);
                        match(REDSTONE);
                        setState(691);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 92, _ctx)) {
                            case 1: {
                                setState(690);
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
                        setState(701);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 94, _ctx)) {
                            case 1: {
                                setState(693);
                                comparisonOp();
                                setState(699);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case NUMBER: {
                                        setState(694);
                                        number();
                                    }
                                        break;
                                    case LPAREN: {
                                        setState(695);
                                        match(LPAREN);
                                        setState(696);
                                        dataExpression();
                                        setState(697);
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
                setState(713);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 97, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(711);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 96, _ctx)) {
                                case 1: {
                                    _localctx = new BooleanConjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(705);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(706);
                                    match(AND);
                                    setState(707);
                                    boolexpr(5);
                                }
                                    break;
                                case 2: {
                                    _localctx = new BooleanDisjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(708);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(709);
                                    match(OR);
                                    setState(710);
                                    boolexpr(4);
                                }
                                    break;
                            }
                        }
                    }
                    setState(715);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 97, _ctx);
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
        enterRule(_localctx, 80, RULE_comparisonOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(716);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 18010000462970880L) != 0))) {
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
        enterRule(_localctx, 82, RULE_setOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(718);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 4611686147276406784L) != 0) || _la == EVERY)) {
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
        enterRule(_localctx, 84, RULE_labelAccess);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(720);
                label();
                setState(725);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(721);
                            match(COMMA);
                            setState(722);
                            label();
                        }
                    }
                    setState(727);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(729);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == ROUND) {
                    {
                        setState(728);
                        roundrobin();
                    }
                }

                setState(732);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (((((_la - 62)) & ~0x3f) == 0 && ((1L << (_la - 62)) & 66043905L) != 0)) {
                    {
                        setState(731);
                        sidequalifier();
                    }
                }

                setState(735);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SLOTS || _la == SLOT) {
                    {
                        setState(734);
                        slotqualifier();
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
        enterRule(_localctx, 86, RULE_roundrobin);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(737);
                match(ROUND);
                setState(738);
                match(ROBIN);
                setState(739);
                match(BY);
                setState(740);
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
    public static class RawLabelContext extends LabelContext {

        public IdentifierContext identifier() {
            return getRuleContext(IdentifierContext.class, 0);
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
        enterRule(_localctx, 88, RULE_label);
        try {
            setState(744);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case TRANSFER:
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
                    _localctx = new RawLabelContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    {
                        setState(742);
                        identifier();
                    }
                }
                    break;
                case STRING:
                    _localctx = new StringLabelContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(743);
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
        enterRule(_localctx, 90, RULE_emptyslots);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(746);
                match(EMPTY);
                setState(747);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(748);
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

        public TerminalNode TRANSFER() {
            return getToken(SFMLParser.TRANSFER, 0);
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
        enterRule(_localctx, 92, RULE_identifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(750);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 32766L) != 0)
                    || ((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76)) & 17181820803L) != 0))) {
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

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
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
        enterRule(_localctx, 94, RULE_stateDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(752);
                match(STATE);
                setState(753);
                match(VARIABLE);
                setState(754);
                match(EQ_SYMBOL);
                setState(755);
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

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
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
        enterRule(_localctx, 96, RULE_dataDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(757);
                match(LET);
                setState(758);
                match(VARIABLE);
                setState(759);
                match(EQ_SYMBOL);
                setState(760);
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
        enterRule(_localctx, 98, RULE_dataAssignment);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(763);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SET) {
                    {
                        setState(762);
                        match(SET);
                    }
                }

                setState(765);
                dataTarget();
                setState(766);
                match(EQ_SYMBOL);
                setState(767);
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
        enterRule(_localctx, 100, RULE_printStatement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(769);
                match(PRINT);
                setState(770);
                match(LPAREN);
                setState(771);
                dataExpression();
                setState(776);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(772);
                            match(COMMA);
                            setState(773);
                            dataExpression();
                        }
                    }
                    setState(778);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(779);
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

        public TerminalNode TRANSFER() {
            return getToken(SFMLParser.TRANSFER, 0);
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

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
        }

        public TerminalNode RPAREN() {
            return getToken(SFMLParser.RPAREN, 0);
        }

        public TerminalNode INTO() {
            return getToken(SFMLParser.INTO, 0);
        }

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
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
        enterRule(_localctx, 102, RULE_transferStatement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(781);
                match(TRANSFER);
                setState(782);
                match(LPAREN);
                setState(783);
                dataExpression();
                setState(784);
                match(COMMA);
                setState(785);
                dataExpression();
                setState(792);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(786);
                        match(COMMA);
                        setState(787);
                        dataExpression();
                        setState(790);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == COMMA) {
                            {
                                setState(788);
                                match(COMMA);
                                setState(789);
                                dataExpression();
                            }
                        }

                    }
                }

                setState(794);
                match(RPAREN);
                setState(797);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == INTO) {
                    {
                        setState(795);
                        match(INTO);
                        setState(796);
                        match(VARIABLE);
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
    public static class DataTargetContext extends ParserRuleContext {

        public TerminalNode VARIABLE() {
            return getToken(SFMLParser.VARIABLE, 0);
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
        enterRule(_localctx, 104, RULE_dataTarget);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(799);
                match(VARIABLE);
                setState(808);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACKET || _la == DOT) {
                    {
                        setState(806);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case DOT: {
                                setState(800);
                                match(DOT);
                                setState(801);
                                dataName();
                            }
                                break;
                            case LBRACKET: {
                                setState(802);
                                match(LBRACKET);
                                setState(803);
                                dataExpression();
                                setState(804);
                                match(RBRACKET);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                    }
                    setState(810);
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
        enterRule(_localctx, 106, RULE_dataExpression);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(811);
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
        enterRule(_localctx, 108, RULE_dataOr);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(813);
                dataAnd();
                setState(818);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == OR) {
                    {
                        {
                            setState(814);
                            match(OR);
                            setState(815);
                            dataAnd();
                        }
                    }
                    setState(820);
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
        enterRule(_localctx, 110, RULE_dataAnd);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(821);
                dataNot();
                setState(826);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == AND) {
                    {
                        {
                            setState(822);
                            match(AND);
                            setState(823);
                            dataNot();
                        }
                    }
                    setState(828);
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
        enterRule(_localctx, 112, RULE_dataNot);
        try {
            setState(832);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 112, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(829);
                    match(NOT);
                    setState(830);
                    dataNot();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(831);
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
        enterRule(_localctx, 114, RULE_dataComparison);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(834);
                dataBitOr();
                setState(838);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18010000462970880L) != 0)) {
                    {
                        setState(835);
                        comparisonOp();
                        setState(836);
                        dataBitOr();
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
        enterRule(_localctx, 116, RULE_dataBitOr);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(840);
                dataBitXor();
                setState(845);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == BIT_OR) {
                    {
                        {
                            setState(841);
                            match(BIT_OR);
                            setState(842);
                            dataBitXor();
                        }
                    }
                    setState(847);
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
        enterRule(_localctx, 118, RULE_dataBitXor);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(848);
                dataBitAnd();
                setState(853);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == BIT_XOR) {
                    {
                        {
                            setState(849);
                            match(BIT_XOR);
                            setState(850);
                            dataBitAnd();
                        }
                    }
                    setState(855);
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
        enterRule(_localctx, 120, RULE_dataBitAnd);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(856);
                dataShift();
                setState(861);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == BIT_AND) {
                    {
                        {
                            setState(857);
                            match(BIT_AND);
                            setState(858);
                            dataShift();
                        }
                    }
                    setState(863);
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
        enterRule(_localctx, 122, RULE_dataShift);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(864);
                dataAdd();
                setState(869);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 469762048L) != 0)) {
                    {
                        {
                            setState(865);
                            _la = _input.LA(1);
                            if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 469762048L) != 0))) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                            setState(866);
                            dataAdd();
                        }
                    }
                    setState(871);
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
        enterRule(_localctx, 124, RULE_dataAdd);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(872);
                dataMultiply();
                setState(877);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == PLUS || _la == DASH) {
                    {
                        {
                            setState(873);
                            _la = _input.LA(1);
                            if (!(_la == PLUS || _la == DASH)) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                            setState(874);
                            dataMultiply();
                        }
                    }
                    setState(879);
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
        enterRule(_localctx, 126, RULE_dataMultiply);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(880);
                dataSign();
                setState(889);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 120, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(884);
                                _errHandler.sync(this);
                                switch (getInterpreter().adaptivePredict(_input, 119, _ctx)) {
                                    case 1: {
                                        setState(881);
                                        multiplyOperator();
                                    }
                                        break;
                                    case 2: {
                                        setState(882);
                                        match(SLASH);
                                    }
                                        break;
                                    case 3: {
                                        setState(883);
                                        match(PERCENT);
                                    }
                                        break;
                                }
                                setState(886);
                                dataSign();
                            }
                        }
                    }
                    setState(891);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 120, _ctx);
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
        enterRule(_localctx, 128, RULE_multiplyOperator);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(892);
                if (!(_input.LT(1)
                    .getText()
                    .equals("*"))) throw new FailedPredicateException(this, "_input.LT(1).getText().equals(\"*\")");
                setState(893);
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
        enterRule(_localctx, 130, RULE_dataSign);
        int _la;
        try {
            setState(898);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 121, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(895);
                    _la = _input.LA(1);
                    if (!(_la == BIT_NOT || _la == PLUS || _la == DASH)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(896);
                    dataSign();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(897);
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

        public List<TerminalNode> LPAREN() {
            return getTokens(SFMLParser.LPAREN);
        }

        public TerminalNode LPAREN(int i) {
            return getToken(SFMLParser.LPAREN, i);
        }

        public List<TerminalNode> RPAREN() {
            return getTokens(SFMLParser.RPAREN);
        }

        public TerminalNode RPAREN(int i) {
            return getToken(SFMLParser.RPAREN, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(SFMLParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(SFMLParser.COMMA, i);
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
        enterRule(_localctx, 132, RULE_dataPostfix);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(900);
                dataPrimary();
                setState(923);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 126, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            setState(921);
                            _errHandler.sync(this);
                            switch (_input.LA(1)) {
                                case DOT: {
                                    setState(901);
                                    match(DOT);
                                    setState(902);
                                    dataName();
                                    setState(915);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 124, _ctx)) {
                                        case 1: {
                                            setState(903);
                                            match(LPAREN);
                                            setState(912);
                                            _errHandler.sync(this);
                                            _la = _input.LA(1);
                                            if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -12288142454947842L) != 0)
                                                || ((((_la - 64)) & ~0x3f) == 0
                                                    && ((1L << (_la - 64)) & 253162552295359L) != 0)) {
                                                {
                                                    setState(904);
                                                    dataExpression();
                                                    setState(909);
                                                    _errHandler.sync(this);
                                                    _la = _input.LA(1);
                                                    while (_la == COMMA) {
                                                        {
                                                            {
                                                                setState(905);
                                                                match(COMMA);
                                                                setState(906);
                                                                dataExpression();
                                                            }
                                                        }
                                                        setState(911);
                                                        _errHandler.sync(this);
                                                        _la = _input.LA(1);
                                                    }
                                                }
                                            }

                                            setState(914);
                                            match(RPAREN);
                                        }
                                            break;
                                    }
                                }
                                    break;
                                case LBRACKET: {
                                    setState(917);
                                    match(LBRACKET);
                                    setState(918);
                                    dataExpression();
                                    setState(919);
                                    match(RBRACKET);
                                }
                                    break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                        }
                    }
                    setState(925);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 126, _ctx);
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
        enterRule(_localctx, 134, RULE_dataPrimary);
        int _la;
        try {
            int _alt;
            setState(966);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 133, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(926);
                    match(NUMBER);
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(927);
                    match(STRING);
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(928);
                    match(VARIABLE);
                }
                    break;
                case 4:
                    enterOuterAlt(_localctx, 4); {
                    setState(929);
                    match(TRUE);
                }
                    break;
                case 5:
                    enterOuterAlt(_localctx, 5); {
                    setState(930);
                    match(FALSE);
                }
                    break;
                case 6:
                    enterOuterAlt(_localctx, 6); {
                    setState(931);
                    match(LPAREN);
                    setState(932);
                    dataExpression();
                    setState(933);
                    match(RPAREN);
                }
                    break;
                case 7:
                    enterOuterAlt(_localctx, 7); {
                    setState(935);
                    functionCall();
                }
                    break;
                case 8:
                    enterOuterAlt(_localctx, 8); {
                    setState(936);
                    match(LBRACKET);
                    setState(948);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -12288142454947842L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 253162552295359L) != 0)) {
                        {
                            setState(937);
                            dataExpression();
                            setState(942);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 127, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(938);
                                            match(COMMA);
                                            setState(939);
                                            dataExpression();
                                        }
                                    }
                                }
                                setState(944);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 127, _ctx);
                            }
                            setState(946);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(945);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(950);
                    match(RBRACKET);
                }
                    break;
                case 9:
                    enterOuterAlt(_localctx, 9); {
                    setState(951);
                    match(LBRACE);
                    setState(963);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -12288142488862722L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 70643622084543L) != 0)) {
                        {
                            setState(952);
                            dataField();
                            setState(957);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 130, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(953);
                                            match(COMMA);
                                            setState(954);
                                            dataField();
                                        }
                                    }
                                }
                                setState(959);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 130, _ctx);
                            }
                            setState(961);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(960);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(965);
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
        enterRule(_localctx, 136, RULE_dataField);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(968);
                dataName();
                setState(969);
                match(COLON);
                setState(970);
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

        public TerminalNode TRANSFER() {
            return getToken(SFMLParser.TRANSFER, 0);
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
        enterRule(_localctx, 138, RULE_dataName);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(972);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & -12288142488862722L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 70643622084543L) != 0))) {
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
        enterRule(_localctx, 140, RULE_string);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(974);
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
        enterRule(_localctx, 142, RULE_number);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(976);
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
            case 29:
                return withClause_sempred((WithClauseContext) _localctx, predIndex);
            case 39:
                return boolexpr_sempred((BoolexprContext) _localctx, predIndex);
            case 64:
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

    public static final String _serializedATN = "\u0004\u0001r\u03d3\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"
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
        + "F\u0007F\u0002G\u0007G\u0001\u0000\u0003\u0000\u0092\b\u0000\u0001\u0000"
        + "\u0001\u0000\u0005\u0000\u0096\b\u0000\n\u0000\f\u0000\u0099\t\u0000\u0001"
        + "\u0000\u0005\u0000\u009c\b\u0000\n\u0000\f\u0000\u009f\t\u0000\u0001\u0000"
        + "\u0005\u0000\u00a2\b\u0000\n\u0000\f\u0000\u00a5\t\u0000\u0001\u0000\u0001"
        + "\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"
        + "\u0001\u0005\u0001\u00af\b\u0001\n\u0001\f\u0001\u00b2\t\u0001\u0003\u0001"
        + "\u00b4\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002"
        + "\u00c0\b\u0002\n\u0002\f\u0002\u00c3\t\u0002\u0001\u0003\u0001\u0003\u0001"
        + "\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"
        + "\u0004\u0005\u0004\u00ce\b\u0004\n\u0004\f\u0004\u00d1\t\u0004\u0003\u0004"
        + "\u00d3\b\u0004\u0001\u0004\u0003\u0004\u00d6\b\u0004\u0001\u0004\u0001"
        + "\u0004\u0001\u0004\u0005\u0004\u00db\b\u0004\n\u0004\f\u0004\u00de\t\u0004"
        + "\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"
        + "\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006\u00e9\b\u0006\n\u0006"
        + "\f\u0006\u00ec\t\u0006\u0003\u0006\u00ee\b\u0006\u0001\u0006\u0001\u0006"
        + "\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\t\u0001"
        + "\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"
        + "\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"
        + "\t\u0001\t\u0001\t\u0003\t\u010e\b\t\u0001\n\u0003\n\u0111\b\n\u0001\n"
        + "\u0003\n\u0114\b\n\u0001\n\u0001\n\u0003\n\u0118\b\n\u0001\n\u0001\n\u0001"
        + "\n\u0001\n\u0003\n\u011e\b\n\u0001\n\u0003\n\u0121\b\n\u0001\u000b\u0005"
        + "\u000b\u0124\b\u000b\n\u000b\f\u000b\u0127\t\u000b\u0001\f\u0001\f\u0001"
        + "\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"
        + "\f\u0003\f\u0135\b\f\u0001\r\u0001\r\u0003\r\u0139\b\r\u0001\r\u0001\r"
        + "\u0001\r\u0003\r\u013e\b\r\u0001\r\u0001\r\u0003\r\u0142\b\r\u0001\r\u0001"
        + "\r\u0001\r\u0003\r\u0147\b\r\u0001\r\u0001\r\u0001\r\u0003\r\u014c\b\r"
        + "\u0001\r\u0001\r\u0001\r\u0003\r\u0151\b\r\u0003\r\u0153\b\r\u0001\u000e"
        + "\u0001\u000e\u0003\u000e\u0157\b\u000e\u0001\u000e\u0001\u000e\u0005\u000e"
        + "\u015b\b\u000e\n\u000e\f\u000e\u015e\t\u000e\u0001\u000e\u0003\u000e\u0161"
        + "\b\u000e\u0001\u000f\u0001\u000f\u0003\u000f\u0165\b\u000f\u0001\u000f"
        + "\u0003\u000f\u0168\b\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u016c\b"
        + "\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0171\b\u000f\u0001"
        + "\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0176\b\u000f\u0001\u000f\u0003"
        + "\u000f\u0179\b\u000f\u0003\u000f\u017b\b\u000f\u0001\u0010\u0001\u0010"
        + "\u0003\u0010\u017f\b\u0010\u0001\u0010\u0003\u0010\u0182\b\u0010\u0001"
        + "\u0010\u0001\u0010\u0003\u0010\u0186\b\u0010\u0001\u0010\u0003\u0010\u0189"
        + "\b\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u018e\b\u0010"
        + "\u0001\u0010\u0003\u0010\u0191\b\u0010\u0001\u0010\u0001\u0010\u0001\u0010"
        + "\u0003\u0010\u0196\b\u0010\u0001\u0010\u0003\u0010\u0199\b\u0010\u0003"
        + "\u0010\u019b\b\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001"
        + "\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u01a4\b\u0013\n\u0013\f\u0013"
        + "\u01a7\t\u0013\u0001\u0013\u0003\u0013\u01aa\b\u0013\u0001\u0014\u0003"
        + "\u0014\u01ad\b\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u01b1\b\u0014"
        + "\u0001\u0014\u0001\u0014\u0003\u0014\u01b5\b\u0014\u0001\u0014\u0003\u0014"
        + "\u01b8\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"
        + "\u0003\u0015\u01bf\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"
        + "\u0001\u0016\u0003\u0016\u01c6\b\u0016\u0001\u0016\u0003\u0016\u01c9\b"
        + "\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001"
        + "\u0017\u0003\u0017\u01d1\b\u0017\u0001\u0017\u0003\u0017\u01d4\b\u0017"
        + "\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019"
        + "\u0003\u0019\u01dc\b\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u01e0\b"
        + "\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u01e4\b\u0019\u0003\u0019\u01e6"
        + "\b\u0019\u0003\u0019\u01e8\b\u0019\u0003\u0019\u01ea\b\u0019\u0001\u0019"
        + "\u0003\u0019\u01ed\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a"
        + "\u01f2\b\u001a\n\u001a\f\u001a\u01f5\t\u001a\u0001\u001a\u0003\u001a\u01f8"
        + "\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0005\u001b\u01fd\b\u001b"
        + "\n\u001b\f\u001b\u0200\t\u001b\u0001\u001b\u0003\u001b\u0203\b\u001b\u0001"
        + "\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u0209\b\u001c\u0001"
        + "\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"
        + "\u001d\u0001\u001d\u0001\u001d\u0003\u001d\u0214\b\u001d\u0001\u001d\u0003"
        + "\u001d\u0217\b\u001d\u0001\u001d\u0003\u001d\u021a\b\u001d\u0001\u001d"
        + "\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d"
        + "\u0222\b\u001d\n\u001d\f\u001d\u0225\t\u001d\u0001\u001e\u0001\u001e\u0001"
        + "\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u022c\b\u001e\n\u001e\f\u001e"
        + "\u022f\t\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u0234\b"
        + "\u001e\n\u001e\f\u001e\u0237\t\u001e\u0003\u001e\u0239\b\u001e\u0001\u001f"
        + "\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0005\u001f\u0240\b\u001f"
        + "\n\u001f\f\u001f\u0243\t\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u0247"
        + "\b\u001f\u0001 \u0001 \u0001!\u0001!\u0001!\u0001\"\u0001\"\u0001\"\u0005"
        + "\"\u0251\b\"\n\"\f\"\u0254\t\"\u0001#\u0001#\u0001#\u0003#\u0259\b#\u0001"
        + "$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0005"
        + "$\u0265\b$\n$\f$\u0268\t$\u0001$\u0001$\u0003$\u026c\b$\u0001$\u0001$"
        + "\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0003%\u0278"
        + "\b%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001"
        + "%\u0001%\u0001%\u0003%\u0286\b%\u0001&\u0001&\u0001&\u0001&\u0001&\u0001"
        + "&\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001"
        + "\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u029c\b\'\u0001\'\u0001"
        + "\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u02a6\b\'\u0001"
        + "\'\u0003\'\u02a9\b\'\u0001\'\u0003\'\u02ac\b\'\u0001\'\u0001\'\u0003\'"
        + "\u02b0\b\'\u0001\'\u0001\'\u0003\'\u02b4\b\'\u0001\'\u0001\'\u0001\'\u0001"
        + "\'\u0001\'\u0001\'\u0003\'\u02bc\b\'\u0003\'\u02be\b\'\u0003\'\u02c0\b"
        + "\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0005\'\u02c8\b\'\n"
        + "\'\f\'\u02cb\t\'\u0001(\u0001(\u0001)\u0001)\u0001*\u0001*\u0001*\u0005"
        + "*\u02d4\b*\n*\f*\u02d7\t*\u0001*\u0003*\u02da\b*\u0001*\u0003*\u02dd\b"
        + "*\u0001*\u0003*\u02e0\b*\u0001+\u0001+\u0001+\u0001+\u0001+\u0001,\u0001"
        + ",\u0003,\u02e9\b,\u0001-\u0001-\u0001-\u0001-\u0001.\u0001.\u0001/\u0001"
        + "/\u0001/\u0001/\u0001/\u00010\u00010\u00010\u00010\u00010\u00011\u0003"
        + "1\u02fc\b1\u00011\u00011\u00011\u00011\u00012\u00012\u00012\u00012\u0001"
        + "2\u00052\u0307\b2\n2\f2\u030a\t2\u00012\u00012\u00013\u00013\u00013\u0001"
        + "3\u00013\u00013\u00013\u00013\u00013\u00033\u0317\b3\u00033\u0319\b3\u0001"
        + "3\u00013\u00013\u00033\u031e\b3\u00014\u00014\u00014\u00014\u00014\u0001"
        + "4\u00014\u00054\u0327\b4\n4\f4\u032a\t4\u00015\u00015\u00016\u00016\u0001"
        + "6\u00056\u0331\b6\n6\f6\u0334\t6\u00017\u00017\u00017\u00057\u0339\b7"
        + "\n7\f7\u033c\t7\u00018\u00018\u00018\u00038\u0341\b8\u00019\u00019\u0001"
        + "9\u00019\u00039\u0347\b9\u0001:\u0001:\u0001:\u0005:\u034c\b:\n:\f:\u034f"
        + "\t:\u0001;\u0001;\u0001;\u0005;\u0354\b;\n;\f;\u0357\t;\u0001<\u0001<"
        + "\u0001<\u0005<\u035c\b<\n<\f<\u035f\t<\u0001=\u0001=\u0001=\u0005=\u0364"
        + "\b=\n=\f=\u0367\t=\u0001>\u0001>\u0001>\u0005>\u036c\b>\n>\f>\u036f\t"
        + ">\u0001?\u0001?\u0001?\u0001?\u0003?\u0375\b?\u0001?\u0005?\u0378\b?\n"
        + "?\f?\u037b\t?\u0001@\u0001@\u0001@\u0001A\u0001A\u0001A\u0003A\u0383\b"
        + "A\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0005B\u038c\bB\nB"
        + "\fB\u038f\tB\u0003B\u0391\bB\u0001B\u0003B\u0394\bB\u0001B\u0001B\u0001"
        + "B\u0001B\u0005B\u039a\bB\nB\fB\u039d\tB\u0001C\u0001C\u0001C\u0001C\u0001"
        + "C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0005"
        + "C\u03ad\bC\nC\fC\u03b0\tC\u0001C\u0003C\u03b3\bC\u0003C\u03b5\bC\u0001"
        + "C\u0001C\u0001C\u0001C\u0001C\u0005C\u03bc\bC\nC\fC\u03bf\tC\u0001C\u0003"
        + "C\u03c2\bC\u0003C\u03c4\bC\u0001C\u0003C\u03c7\bC\u0001D\u0001D\u0001"
        + "D\u0001D\u0001E\u0001E\u0001F\u0001F\u0001G\u0001G\u0001G\u0000\u0002"
        + ":NH\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018"
        + "\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080"
        + "\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0000\r\u0002\u0000\t\naa\u0001"
        + "\u0000X[\u0001\u0000_`\u0002\u0000LQSW\u0001\u0000;<\u0001\u0000*5\u0003"
        + "\u0000!$>>ee\u0001\u0000JK\u0006\u0000\u0001\u000eLMSVZ\\^`nn\u0001\u0000"
        + "\u001a\u001c\u0002\u0000]]ii\u0003\u0000\u0019\u0019]]ii\t\u0000\u0001"
        + "\u000e\u001d*,,..22446EGenn\u042b\u0000\u0091\u0001\u0000\u0000\u0000"
        + "\u0002\u00a8\u0001\u0000\u0000\u0000\u0004\u00bc\u0001\u0000\u0000\u0000"
        + "\u0006\u00c4\u0001\u0000\u0000\u0000\b\u00d5\u0001\u0000\u0000\u0000\n"
        + "\u00df\u0001\u0000\u0000\u0000\f\u00e3\u0001\u0000\u0000\u0000\u000e\u00f1"
        + "\u0001\u0000\u0000\u0000\u0010\u00f4\u0001\u0000\u0000\u0000\u0012\u010d"
        + "\u0001\u0000\u0000\u0000\u0014\u0120\u0001\u0000\u0000\u0000\u0016\u0125"
        + "\u0001\u0000\u0000\u0000\u0018\u0134\u0001\u0000\u0000\u0000\u001a\u0152"
        + "\u0001\u0000\u0000\u0000\u001c\u0154\u0001\u0000\u0000\u0000\u001e\u017a"
        + "\u0001\u0000\u0000\u0000 \u019a\u0001\u0000\u0000\u0000\"\u019c\u0001"
        + "\u0000\u0000\u0000$\u019e\u0001\u0000\u0000\u0000&\u01a0\u0001\u0000\u0000"
        + "\u0000(\u01b7\u0001\u0000\u0000\u0000*\u01be\u0001\u0000\u0000\u0000,"
        + "\u01c5\u0001\u0000\u0000\u0000.\u01ca\u0001\u0000\u0000\u00000\u01d5\u0001"
        + "\u0000\u0000\u00002\u01ec\u0001\u0000\u0000\u00004\u01ee\u0001\u0000\u0000"
        + "\u00006\u01f9\u0001\u0000\u0000\u00008\u0208\u0001\u0000\u0000\u0000:"
        + "\u0219\u0001\u0000\u0000\u0000<\u0238\u0001\u0000\u0000\u0000>\u0246\u0001"
        + "\u0000\u0000\u0000@\u0248\u0001\u0000\u0000\u0000B\u024a\u0001\u0000\u0000"
        + "\u0000D\u024d\u0001\u0000\u0000\u0000F\u0255\u0001\u0000\u0000\u0000H"
        + "\u025a\u0001\u0000\u0000\u0000J\u0285\u0001\u0000\u0000\u0000L\u0287\u0001"
        + "\u0000\u0000\u0000N\u02bf\u0001\u0000\u0000\u0000P\u02cc\u0001\u0000\u0000"
        + "\u0000R\u02ce\u0001\u0000\u0000\u0000T\u02d0\u0001\u0000\u0000\u0000V"
        + "\u02e1\u0001\u0000\u0000\u0000X\u02e8\u0001\u0000\u0000\u0000Z\u02ea\u0001"
        + "\u0000\u0000\u0000\\\u02ee\u0001\u0000\u0000\u0000^\u02f0\u0001\u0000"
        + "\u0000\u0000`\u02f5\u0001\u0000\u0000\u0000b\u02fb\u0001\u0000\u0000\u0000"
        + "d\u0301\u0001\u0000\u0000\u0000f\u030d\u0001\u0000\u0000\u0000h\u031f"
        + "\u0001\u0000\u0000\u0000j\u032b\u0001\u0000\u0000\u0000l\u032d\u0001\u0000"
        + "\u0000\u0000n\u0335\u0001\u0000\u0000\u0000p\u0340\u0001\u0000\u0000\u0000"
        + "r\u0342\u0001\u0000\u0000\u0000t\u0348\u0001\u0000\u0000\u0000v\u0350"
        + "\u0001\u0000\u0000\u0000x\u0358\u0001\u0000\u0000\u0000z\u0360\u0001\u0000"
        + "\u0000\u0000|\u0368\u0001\u0000\u0000\u0000~\u0370\u0001\u0000\u0000\u0000"
        + "\u0080\u037c\u0001\u0000\u0000\u0000\u0082\u0382\u0001\u0000\u0000\u0000"
        + "\u0084\u0384\u0001\u0000\u0000\u0000\u0086\u03c6\u0001\u0000\u0000\u0000"
        + "\u0088\u03c8\u0001\u0000\u0000\u0000\u008a\u03cc\u0001\u0000\u0000\u0000"
        + "\u008c\u03ce\u0001\u0000\u0000\u0000\u008e\u03d0\u0001\u0000\u0000\u0000"
        + "\u0090\u0092\u0003\u0010\b\u0000\u0091\u0090\u0001\u0000\u0000\u0000\u0091"
        + "\u0092\u0001\u0000\u0000\u0000\u0092\u0097\u0001\u0000\u0000\u0000\u0093"
        + "\u0096\u0003^/\u0000\u0094\u0096\u0003`0\u0000\u0095\u0093\u0001\u0000"
        + "\u0000\u0000\u0095\u0094\u0001\u0000\u0000\u0000\u0096\u0099\u0001\u0000"
        + "\u0000\u0000\u0097\u0095\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000"
        + "\u0000\u0000\u0098\u009d\u0001\u0000\u0000\u0000\u0099\u0097\u0001\u0000"
        + "\u0000\u0000\u009a\u009c\u0003\u0002\u0001\u0000\u009b\u009a\u0001\u0000"
        + "\u0000\u0000\u009c\u009f\u0001\u0000\u0000\u0000\u009d\u009b\u0001\u0000"
        + "\u0000\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e\u00a3\u0001\u0000"
        + "\u0000\u0000\u009f\u009d\u0001\u0000\u0000\u0000\u00a0\u00a2\u0003\u0012"
        + "\t\u0000\u00a1\u00a0\u0001\u0000\u0000\u0000\u00a2\u00a5\u0001\u0000\u0000"
        + "\u0000\u00a3\u00a1\u0001\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000"
        + "\u0000\u00a4\u00a6\u0001\u0000\u0000\u0000\u00a5\u00a3\u0001\u0000\u0000"
        + "\u0000\u00a6\u00a7\u0005\u0000\u0000\u0001\u00a7\u0001\u0001\u0000\u0000"
        + "\u0000\u00a8\u00a9\u0005\u0003\u0000\u0000\u00a9\u00aa\u0003\u0004\u0002"
        + "\u0000\u00aa\u00b3\u0005j\u0000\u0000\u00ab\u00b0\u0003\u0006\u0003\u0000"
        + "\u00ac\u00ad\u0005f\u0000\u0000\u00ad\u00af\u0003\u0006\u0003\u0000\u00ae"
        + "\u00ac\u0001\u0000\u0000\u0000\u00af\u00b2\u0001\u0000\u0000\u0000\u00b0"
        + "\u00ae\u0001\u0000\u0000\u0000\u00b0\u00b1\u0001\u0000\u0000\u0000\u00b1"
        + "\u00b4\u0001\u0000\u0000\u0000\u00b2\u00b0\u0001\u0000\u0000\u0000\u00b3"
        + "\u00ab\u0001\u0000\u0000\u0000\u00b3\u00b4\u0001\u0000\u0000\u0000\u00b4"
        + "\u00b5\u0001\u0000\u0000\u0000\u00b5\u00b6\u0005k\u0000\u0000\u00b6\u00b7"
        + "\u0005\u0004\u0000\u0000\u00b7\u00b8\u0003\b\u0004\u0000\u00b8\u00b9\u0005"
        + "b\u0000\u0000\u00b9\u00ba\u0003\u0016\u000b\u0000\u00ba\u00bb\u0005c\u0000"
        + "\u0000\u00bb\u0003\u0001\u0000\u0000\u0000\u00bc\u00c1\u0003\u008aE\u0000"
        + "\u00bd\u00be\u0005\u0014\u0000\u0000\u00be\u00c0\u0003\u008aE\u0000\u00bf"
        + "\u00bd\u0001\u0000\u0000\u0000\u00c0\u00c3\u0001\u0000\u0000\u0000\u00c1"
        + "\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000\u0000\u00c2"
        + "\u0005\u0001\u0000\u0000\u0000\u00c3\u00c1\u0001\u0000\u0000\u0000\u00c4"
        + "\u00c5\u0005\u000f\u0000\u0000\u00c5\u00c6\u0005g\u0000\u0000\u00c6\u00c7"
        + "\u0003\b\u0004\u0000\u00c7\u0007\u0001\u0000\u0000\u0000\u00c8\u00d6\u0003"
        + "\u008aE\u0000\u00c9\u00d2\u0005\u0012\u0000\u0000\u00ca\u00cf\u0003\n"
        + "\u0005\u0000\u00cb\u00cc\u0005f\u0000\u0000\u00cc\u00ce\u0003\n\u0005"
        + "\u0000\u00cd\u00cb\u0001\u0000\u0000\u0000\u00ce\u00d1\u0001\u0000\u0000"
        + "\u0000\u00cf\u00cd\u0001\u0000\u0000\u0000\u00cf\u00d0\u0001\u0000\u0000"
        + "\u0000\u00d0\u00d3\u0001\u0000\u0000\u0000\u00d1\u00cf\u0001\u0000\u0000"
        + "\u0000\u00d2\u00ca\u0001\u0000\u0000\u0000\u00d2\u00d3\u0001\u0000\u0000"
        + "\u0000\u00d3\u00d4\u0001\u0000\u0000\u0000\u00d4\u00d6\u0005\u0013\u0000"
        + "\u0000\u00d5\u00c8\u0001\u0000\u0000\u0000\u00d5\u00c9\u0001\u0000\u0000"
        + "\u0000\u00d6\u00dc\u0001\u0000\u0000\u0000\u00d7\u00d8\u0005\u0010\u0000"
        + "\u0000\u00d8\u00d9\u0005m\u0000\u0000\u00d9\u00db\u0005\u0011\u0000\u0000"
        + "\u00da\u00d7\u0001\u0000\u0000\u0000\u00db\u00de\u0001\u0000\u0000\u0000"
        + "\u00dc\u00da\u0001\u0000\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000"
        + "\u00dd\t\u0001\u0000\u0000\u0000\u00de\u00dc\u0001\u0000\u0000\u0000\u00df"
        + "\u00e0\u0003\u008aE\u0000\u00e0\u00e1\u0005g\u0000\u0000\u00e1\u00e2\u0003"
        + "\b\u0004\u0000\u00e2\u000b\u0001\u0000\u0000\u0000\u00e3\u00e4\u0003\u0004"
        + "\u0002\u0000\u00e4\u00ed\u0005j\u0000\u0000\u00e5\u00ea\u0003j5\u0000"
        + "\u00e6\u00e7\u0005f\u0000\u0000\u00e7\u00e9\u0003j5\u0000\u00e8\u00e6"
        + "\u0001\u0000\u0000\u0000\u00e9\u00ec\u0001\u0000\u0000\u0000\u00ea\u00e8"
        + "\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001\u0000\u0000\u0000\u00eb\u00ee"
        + "\u0001\u0000\u0000\u0000\u00ec\u00ea\u0001\u0000\u0000\u0000\u00ed\u00e5"
        + "\u0001\u0000\u0000\u0000\u00ed\u00ee\u0001\u0000\u0000\u0000\u00ee\u00ef"
        + "\u0001\u0000\u0000\u0000\u00ef\u00f0\u0005k\u0000\u0000\u00f0\r\u0001"
        + "\u0000\u0000\u0000\u00f1\u00f2\u0005\u0005\u0000\u0000\u00f2\u00f3\u0003"
        + "j5\u0000\u00f3\u000f\u0001\u0000\u0000\u0000\u00f4\u00f5\u0005d\u0000"
        + "\u0000\u00f5\u00f6\u0003\u008cF\u0000\u00f6\u0011\u0001\u0000\u0000\u0000"
        + "\u00f7\u00f8\u0005e\u0000\u0000\u00f8\u00f9\u0003\u0014\n\u0000\u00f9"
        + "\u00fa\u0005b\u0000\u0000\u00fa\u00fb\u0003\u0016\u000b\u0000\u00fb\u00fc"
        + "\u0005c\u0000\u0000\u00fc\u010e\u0001\u0000\u0000\u0000\u00fd\u00fe\u0005"
        + "e\u0000\u0000\u00fe\u00ff\u0005^\u0000\u0000\u00ff\u0100\u0005a\u0000"
        + "\u0000\u0100\u0101\u0005b\u0000\u0000\u0101\u0102\u0003\u0016\u000b\u0000"
        + "\u0102\u0103\u0005c\u0000\u0000\u0103\u010e\u0001\u0000\u0000\u0000\u0104"
        + "\u0105\u0005e\u0000\u0000\u0105\u0106\u0005^\u0000\u0000\u0106\u0107\u0007"
        + "\u0000\u0000\u0000\u0107\u0108\u00056\u0000\u0000\u0108\u0109\u0003T*"
        + "\u0000\u0109\u010a\u0005b\u0000\u0000\u010a\u010b\u0003\u0016\u000b\u0000"
        + "\u010b\u010c\u0005c\u0000\u0000\u010c\u010e\u0001\u0000\u0000\u0000\u010d"
        + "\u00f7\u0001\u0000\u0000\u0000\u010d\u00fd\u0001\u0000\u0000\u0000\u010d"
        + "\u0104\u0001\u0000\u0000\u0000\u010e\u0013\u0001\u0000\u0000\u0000\u010f"
        + "\u0111\u0005m\u0000\u0000\u0110\u010f\u0001\u0000\u0000\u0000\u0110\u0111"
        + "\u0001\u0000\u0000\u0000\u0111\u0113\u0001\u0000\u0000\u0000\u0112\u0114"
        + "\u0005\\\u0000\u0000\u0113\u0112\u0001\u0000\u0000\u0000\u0113\u0114\u0001"
        + "\u0000\u0000\u0000\u0114\u0117\u0001\u0000\u0000\u0000\u0115\u0116\u0005"
        + "]\u0000\u0000\u0116\u0118\u0005m\u0000\u0000\u0117\u0115\u0001\u0000\u0000"
        + "\u0000\u0117\u0118\u0001\u0000\u0000\u0000\u0118\u0119\u0001\u0000\u0000"
        + "\u0000\u0119\u0121\u0007\u0001\u0000\u0000\u011a\u011d\u0005l\u0000\u0000"
        + "\u011b\u011c\u0005]\u0000\u0000\u011c\u011e\u0005m\u0000\u0000\u011d\u011b"
        + "\u0001\u0000\u0000\u0000\u011d\u011e\u0001\u0000\u0000\u0000\u011e\u011f"
        + "\u0001\u0000\u0000\u0000\u011f\u0121\u0007\u0001\u0000\u0000\u0120\u0110"
        + "\u0001\u0000\u0000\u0000\u0120\u011a\u0001\u0000\u0000\u0000\u0121\u0015"
        + "\u0001\u0000\u0000\u0000\u0122\u0124\u0003\u0018\f\u0000\u0123\u0122\u0001"
        + "\u0000\u0000\u0000\u0124\u0127\u0001\u0000\u0000\u0000\u0125\u0123\u0001"
        + "\u0000\u0000\u0000\u0125\u0126\u0001\u0000\u0000\u0000\u0126\u0017\u0001"
        + "\u0000\u0000\u0000\u0127\u0125\u0001\u0000\u0000\u0000\u0128\u0135\u0003"
        + "`0\u0000\u0129\u0135\u0003b1\u0000\u012a\u0135\u0003d2\u0000\u012b\u0135"
        + "\u0003f3\u0000\u012c\u0135\u0003\u000e\u0007\u0000\u012d\u0135\u0003\u001e"
        + "\u000f\u0000\u012e\u0135\u0003\u001a\r\u0000\u012f\u0135\u0003 \u0010"
        + "\u0000\u0130\u0135\u0003H$\u0000\u0131\u0135\u0003J%\u0000\u0132\u0135"
        + "\u0003L&\u0000\u0133\u0135\u0003\u001c\u000e\u0000\u0134\u0128\u0001\u0000"
        + "\u0000\u0000\u0134\u0129\u0001\u0000\u0000\u0000\u0134\u012a\u0001\u0000"
        + "\u0000\u0000\u0134\u012b\u0001\u0000\u0000\u0000\u0134\u012c\u0001\u0000"
        + "\u0000\u0000\u0134\u012d\u0001\u0000\u0000\u0000\u0134\u012e\u0001\u0000"
        + "\u0000\u0000\u0134\u012f\u0001\u0000\u0000\u0000\u0134\u0130\u0001\u0000"
        + "\u0000\u0000\u0134\u0131\u0001\u0000\u0000\u0000\u0134\u0132\u0001\u0000"
        + "\u0000\u0000\u0134\u0133\u0001\u0000\u0000\u0000\u0135\u0019\u0001\u0000"
        + "\u0000\u0000\u0136\u0138\u00059\u0000\u0000\u0137\u0139\u0003,\u0016\u0000"
        + "\u0138\u0137\u0001\u0000\u0000\u0000\u0138\u0139\u0001\u0000\u0000\u0000"
        + "\u0139\u013a\u0001\u0000\u0000\u0000\u013a\u013b\u0005^\u0000\u0000\u013b"
        + "\u013d\u0007\u0002\u0000\u0000\u013c\u013e\u0003,\u0016\u0000\u013d\u013c"
        + "\u0001\u0000\u0000\u0000\u013d\u013e\u0001\u0000\u0000\u0000\u013e\u013f"
        + "\u0001\u0000\u0000\u0000\u013f\u0141\u00057\u0000\u0000\u0140\u0142\u0005"
        + ">\u0000\u0000\u0141\u0140\u0001\u0000\u0000\u0000\u0141\u0142\u0001\u0000"
        + "\u0000\u0000\u0142\u0143\u0001\u0000\u0000\u0000\u0143\u0153\u0003T*\u0000"
        + "\u0144\u0146\u00057\u0000\u0000\u0145\u0147\u0005>\u0000\u0000\u0146\u0145"
        + "\u0001\u0000\u0000\u0000\u0146\u0147\u0001\u0000\u0000\u0000\u0147\u0148"
        + "\u0001\u0000\u0000\u0000\u0148\u0149\u0003T*\u0000\u0149\u014b\u00059"
        + "\u0000\u0000\u014a\u014c\u0003,\u0016\u0000\u014b\u014a\u0001\u0000\u0000"
        + "\u0000\u014b\u014c\u0001\u0000\u0000\u0000\u014c\u014d\u0001\u0000\u0000"
        + "\u0000\u014d\u014e\u0005^\u0000\u0000\u014e\u0150\u0007\u0002\u0000\u0000"
        + "\u014f\u0151\u0003,\u0016\u0000\u0150\u014f\u0001\u0000\u0000\u0000\u0150"
        + "\u0151\u0001\u0000\u0000\u0000\u0151\u0153\u0001\u0000\u0000\u0000\u0152"
        + "\u0136\u0001\u0000\u0000\u0000\u0152\u0144\u0001\u0000\u0000\u0000\u0153"
        + "\u001b\u0001\u0000\u0000\u0000\u0154\u0156\u0005@\u0000\u0000\u0155\u0157"
        + "\u0003X,\u0000\u0156\u0155\u0001\u0000\u0000\u0000\u0156\u0157\u0001\u0000"
        + "\u0000\u0000\u0157\u015c\u0001\u0000\u0000\u0000\u0158\u0159\u0005f\u0000"
        + "\u0000\u0159\u015b\u0003X,\u0000\u015a\u0158\u0001\u0000\u0000\u0000\u015b"
        + "\u015e\u0001\u0000\u0000\u0000\u015c\u015a\u0001\u0000\u0000\u0000\u015c"
        + "\u015d\u0001\u0000\u0000\u0000\u015d\u0160\u0001\u0000\u0000\u0000\u015e"
        + "\u015c\u0001\u0000\u0000\u0000\u015f\u0161\u0005f\u0000\u0000\u0160\u015f"
        + "\u0001\u0000\u0000\u0000\u0160\u0161\u0001\u0000\u0000\u0000\u0161\u001d"
        + "\u0001\u0000\u0000\u0000\u0162\u0164\u00058\u0000\u0000\u0163\u0165\u0003"
        + "\"\u0011\u0000\u0164\u0163\u0001\u0000\u0000\u0000\u0164\u0165\u0001\u0000"
        + "\u0000\u0000\u0165\u0167\u0001\u0000\u0000\u0000\u0166\u0168\u00030\u0018"
        + "\u0000\u0167\u0166\u0001\u0000\u0000\u0000\u0167\u0168\u0001\u0000\u0000"
        + "\u0000\u0168\u0169\u0001\u0000\u0000\u0000\u0169\u016b\u00056\u0000\u0000"
        + "\u016a\u016c\u0005>\u0000\u0000\u016b\u016a\u0001\u0000\u0000\u0000\u016b"
        + "\u016c\u0001\u0000\u0000\u0000\u016c\u016d\u0001\u0000\u0000\u0000\u016d"
        + "\u017b\u0003T*\u0000\u016e\u0170\u00056\u0000\u0000\u016f\u0171\u0005"
        + ">\u0000\u0000\u0170\u016f\u0001\u0000\u0000\u0000\u0170\u0171\u0001\u0000"
        + "\u0000\u0000\u0171\u0172\u0001\u0000\u0000\u0000\u0172\u0173\u0003T*\u0000"
        + "\u0173\u0175\u00058\u0000\u0000\u0174\u0176\u0003\"\u0011\u0000\u0175"
        + "\u0174\u0001\u0000\u0000\u0000\u0175\u0176\u0001\u0000\u0000\u0000\u0176"
        + "\u0178\u0001\u0000\u0000\u0000\u0177\u0179\u00030\u0018\u0000\u0178\u0177"
        + "\u0001\u0000\u0000\u0000\u0178\u0179\u0001\u0000\u0000\u0000\u0179\u017b"
        + "\u0001\u0000\u0000\u0000\u017a\u0162\u0001\u0000\u0000\u0000\u017a\u016e"
        + "\u0001\u0000\u0000\u0000\u017b\u001f\u0001\u0000\u0000\u0000\u017c\u017e"
        + "\u00059\u0000\u0000\u017d\u017f\u0003$\u0012\u0000\u017e\u017d\u0001\u0000"
        + "\u0000\u0000\u017e\u017f\u0001\u0000\u0000\u0000\u017f\u0181\u0001\u0000"
        + "\u0000\u0000\u0180\u0182\u00030\u0018\u0000\u0181\u0180\u0001\u0000\u0000"
        + "\u0000\u0181\u0182\u0001\u0000\u0000\u0000\u0182\u0183\u0001\u0000\u0000"
        + "\u0000\u0183\u0185\u00057\u0000\u0000\u0184\u0186\u0003Z-\u0000\u0185"
        + "\u0184\u0001\u0000\u0000\u0000\u0185\u0186\u0001\u0000\u0000\u0000\u0186"
        + "\u0188\u0001\u0000\u0000\u0000\u0187\u0189\u0005>\u0000\u0000\u0188\u0187"
        + "\u0001\u0000\u0000\u0000\u0188\u0189\u0001\u0000\u0000\u0000\u0189\u018a"
        + "\u0001\u0000\u0000\u0000\u018a\u019b\u0003T*\u0000\u018b\u018d\u00057"
        + "\u0000\u0000\u018c\u018e\u0003Z-\u0000\u018d\u018c\u0001\u0000\u0000\u0000"
        + "\u018d\u018e\u0001\u0000\u0000\u0000\u018e\u0190\u0001\u0000\u0000\u0000"
        + "\u018f\u0191\u0005>\u0000\u0000\u0190\u018f\u0001\u0000\u0000\u0000\u0190"
        + "\u0191\u0001\u0000\u0000\u0000\u0191\u0192\u0001\u0000\u0000\u0000\u0192"
        + "\u0193\u0003T*\u0000\u0193\u0195\u00059\u0000\u0000\u0194\u0196\u0003"
        + "$\u0012\u0000\u0195\u0194\u0001\u0000\u0000\u0000\u0195\u0196\u0001\u0000"
        + "\u0000\u0000\u0196\u0198\u0001\u0000\u0000\u0000\u0197\u0199\u00030\u0018"
        + "\u0000\u0198\u0197\u0001\u0000\u0000\u0000\u0198\u0199\u0001\u0000\u0000"
        + "\u0000\u0199\u019b\u0001\u0000\u0000\u0000\u019a\u017c\u0001\u0000\u0000"
        + "\u0000\u019a\u018b\u0001\u0000\u0000\u0000\u019b!\u0001\u0000\u0000\u0000"
        + "\u019c\u019d\u0003&\u0013\u0000\u019d#\u0001\u0000\u0000\u0000\u019e\u019f"
        + "\u0003&\u0013\u0000\u019f%\u0001\u0000\u0000\u0000\u01a0\u01a5\u0003("
        + "\u0014\u0000\u01a1\u01a2\u0005f\u0000\u0000\u01a2\u01a4\u0003(\u0014\u0000"
        + "\u01a3\u01a1\u0001\u0000\u0000\u0000\u01a4\u01a7\u0001\u0000\u0000\u0000"
        + "\u01a5\u01a3\u0001\u0000\u0000\u0000\u01a5\u01a6\u0001\u0000\u0000\u0000"
        + "\u01a6\u01a9\u0001\u0000\u0000\u0000\u01a7\u01a5\u0001\u0000\u0000\u0000"
        + "\u01a8\u01aa\u0005f\u0000\u0000\u01a9\u01a8\u0001\u0000\u0000\u0000\u01a9"
        + "\u01aa\u0001\u0000\u0000\u0000\u01aa\'\u0001\u0000\u0000\u0000\u01ab\u01ad"
        + "\u0003*\u0015\u0000\u01ac\u01ab\u0001\u0000\u0000\u0000\u01ac\u01ad\u0001"
        + "\u0000\u0000\u0000\u01ad\u01ae\u0001\u0000\u0000\u0000\u01ae\u01b0\u0003"
        + "6\u001b\u0000\u01af\u01b1\u00038\u001c\u0000\u01b0\u01af\u0001\u0000\u0000"
        + "\u0000\u01b0\u01b1\u0001\u0000\u0000\u0000\u01b1\u01b8\u0001\u0000\u0000"
        + "\u0000\u01b2\u01b4\u0003*\u0015\u0000\u01b3\u01b5\u00038\u001c\u0000\u01b4"
        + "\u01b3\u0001\u0000\u0000\u0000\u01b4\u01b5\u0001\u0000\u0000\u0000\u01b5"
        + "\u01b8\u0001\u0000\u0000\u0000\u01b6\u01b8\u00038\u001c\u0000\u01b7\u01ac"
        + "\u0001\u0000\u0000\u0000\u01b7\u01b2\u0001\u0000\u0000\u0000\u01b7\u01b6"
        + "\u0001\u0000\u0000\u0000\u01b8)\u0001\u0000\u0000\u0000\u01b9\u01ba\u0003"
        + ",\u0016\u0000\u01ba\u01bb\u0003.\u0017\u0000\u01bb\u01bf\u0001\u0000\u0000"
        + "\u0000\u01bc\u01bf\u0003.\u0017\u0000\u01bd\u01bf\u0003,\u0016\u0000\u01be"
        + "\u01b9\u0001\u0000\u0000\u0000\u01be\u01bc\u0001\u0000\u0000\u0000\u01be"
        + "\u01bd\u0001\u0000\u0000\u0000\u01bf+\u0001\u0000\u0000\u0000\u01c0\u01c6"
        + "\u0003\u008eG\u0000\u01c1\u01c2\u0005j\u0000\u0000\u01c2\u01c3\u0003j"
        + "5\u0000\u01c3\u01c4\u0005k\u0000\u0000\u01c4\u01c6\u0001\u0000\u0000\u0000"
        + "\u01c5\u01c0\u0001\u0000\u0000\u0000\u01c5\u01c1\u0001\u0000\u0000\u0000"
        + "\u01c6\u01c8\u0001\u0000\u0000\u0000\u01c7\u01c9\u0005>\u0000\u0000\u01c8"
        + "\u01c7\u0001\u0000\u0000\u0000\u01c8\u01c9\u0001\u0000\u0000\u0000\u01c9"
        + "-\u0001\u0000\u0000\u0000\u01ca\u01d0\u0005=\u0000\u0000\u01cb\u01d1\u0003"
        + "\u008eG\u0000\u01cc\u01cd\u0005j\u0000\u0000\u01cd\u01ce\u0003j5\u0000"
        + "\u01ce\u01cf\u0005k\u0000\u0000\u01cf\u01d1\u0001\u0000\u0000\u0000\u01d0"
        + "\u01cb\u0001\u0000\u0000\u0000\u01d0\u01cc\u0001\u0000\u0000\u0000\u01d1"
        + "\u01d3\u0001\u0000\u0000\u0000\u01d2\u01d4\u0005>\u0000\u0000\u01d3\u01d2"
        + "\u0001\u0000\u0000\u0000\u01d3\u01d4\u0001\u0000\u0000\u0000\u01d4/\u0001"
        + "\u0000\u0000\u0000\u01d5\u01d6\u0005?\u0000\u0000\u01d6\u01d7\u00034\u001a"
        + "\u0000\u01d71\u0001\u0000\u0000\u0000\u01d8\u01e9\u0003\\.\u0000\u01d9"
        + "\u01db\u0005g\u0000\u0000\u01da\u01dc\u0003\\.\u0000\u01db\u01da\u0001"
        + "\u0000\u0000\u0000\u01db\u01dc\u0001\u0000\u0000\u0000\u01dc\u01e7\u0001"
        + "\u0000\u0000\u0000\u01dd\u01df\u0005g\u0000\u0000\u01de\u01e0\u0003\\"
        + ".\u0000\u01df\u01de\u0001\u0000\u0000\u0000\u01df\u01e0\u0001\u0000\u0000"
        + "\u0000\u01e0\u01e5\u0001\u0000\u0000\u0000\u01e1\u01e3\u0005g\u0000\u0000"
        + "\u01e2\u01e4\u0003\\.\u0000\u01e3\u01e2\u0001\u0000\u0000\u0000\u01e3"
        + "\u01e4\u0001\u0000\u0000\u0000\u01e4\u01e6\u0001\u0000\u0000\u0000\u01e5"
        + "\u01e1\u0001\u0000\u0000\u0000\u01e5\u01e6\u0001\u0000\u0000\u0000\u01e6"
        + "\u01e8\u0001\u0000\u0000\u0000\u01e7\u01dd\u0001\u0000\u0000\u0000\u01e7"
        + "\u01e8\u0001\u0000\u0000\u0000\u01e8\u01ea\u0001\u0000\u0000\u0000\u01e9"
        + "\u01d9\u0001\u0000\u0000\u0000\u01e9\u01ea\u0001\u0000\u0000\u0000\u01ea"
        + "\u01ed\u0001\u0000\u0000\u0000\u01eb\u01ed\u0003\u008cF\u0000\u01ec\u01d8"
        + "\u0001\u0000\u0000\u0000\u01ec\u01eb\u0001\u0000\u0000\u0000\u01ed3\u0001"
        + "\u0000\u0000\u0000\u01ee\u01f3\u00032\u0019\u0000\u01ef\u01f0\u0005f\u0000"
        + "\u0000\u01f0\u01f2\u00032\u0019\u0000\u01f1\u01ef\u0001\u0000\u0000\u0000"
        + "\u01f2\u01f5\u0001\u0000\u0000\u0000\u01f3\u01f1\u0001\u0000\u0000\u0000"
        + "\u01f3\u01f4\u0001\u0000\u0000\u0000\u01f4\u01f7\u0001\u0000\u0000\u0000"
        + "\u01f5\u01f3\u0001\u0000\u0000\u0000\u01f6\u01f8\u0005f\u0000\u0000\u01f7"
        + "\u01f6\u0001\u0000\u0000\u0000\u01f7\u01f8\u0001\u0000\u0000\u0000\u01f8"
        + "5\u0001\u0000\u0000\u0000\u01f9\u01fe\u00032\u0019\u0000\u01fa\u01fb\u0005"
        + ")\u0000\u0000\u01fb\u01fd\u00032\u0019\u0000\u01fc\u01fa\u0001\u0000\u0000"
        + "\u0000\u01fd\u0200\u0001\u0000\u0000\u0000\u01fe\u01fc\u0001\u0000\u0000"
        + "\u0000\u01fe\u01ff\u0001\u0000\u0000\u0000\u01ff\u0202\u0001\u0000\u0000"
        + "\u0000\u0200\u01fe\u0001\u0000\u0000\u0000\u0201\u0203\u0005)\u0000\u0000"
        + "\u0202\u0201\u0001\u0000\u0000\u0000\u0202\u0203\u0001\u0000\u0000\u0000"
        + "\u02037\u0001\u0000\u0000\u0000\u0204\u0205\u0005D\u0000\u0000\u0205\u0209"
        + "\u0003:\u001d\u0000\u0206\u0207\u0005C\u0000\u0000\u0207\u0209\u0003:"
        + "\u001d\u0000\u0208\u0204\u0001\u0000\u0000\u0000\u0208\u0206\u0001\u0000"
        + "\u0000\u0000\u02099\u0001\u0000\u0000\u0000\u020a\u020b\u0006\u001d\uffff"
        + "\uffff\u0000\u020b\u020c\u0005j\u0000\u0000\u020c\u020d\u0003:\u001d\u0000"
        + "\u020d\u020e\u0005k\u0000\u0000\u020e\u021a\u0001\u0000\u0000\u0000\u020f"
        + "\u0210\u0005\'\u0000\u0000\u0210\u021a\u0003:\u001d\u0004\u0211\u0213"
        + "\u0005E\u0000\u0000\u0212\u0214\u0005F\u0000\u0000\u0213\u0212\u0001\u0000"
        + "\u0000\u0000\u0213\u0214\u0001\u0000\u0000\u0000\u0214\u0217\u0001\u0000"
        + "\u0000\u0000\u0215\u0217\u0005F\u0000\u0000\u0216\u0211\u0001\u0000\u0000"
        + "\u0000\u0216\u0215\u0001\u0000\u0000\u0000\u0217\u0218\u0001\u0000\u0000"
        + "\u0000\u0218\u021a\u0003<\u001e\u0000\u0219\u020a\u0001\u0000\u0000\u0000"
        + "\u0219\u020f\u0001\u0000\u0000\u0000\u0219\u0216\u0001\u0000\u0000\u0000"
        + "\u021a\u0223\u0001\u0000\u0000\u0000\u021b\u021c\n\u0003\u0000\u0000\u021c"
        + "\u021d\u0005(\u0000\u0000\u021d\u0222\u0003:\u001d\u0004\u021e\u021f\n"
        + "\u0002\u0000\u0000\u021f\u0220\u0005)\u0000\u0000\u0220\u0222\u0003:\u001d"
        + "\u0003\u0221\u021b\u0001\u0000\u0000\u0000\u0221\u021e\u0001\u0000\u0000"
        + "\u0000\u0222\u0225\u0001\u0000\u0000\u0000\u0223\u0221\u0001\u0000\u0000"
        + "\u0000\u0223\u0224\u0001\u0000\u0000\u0000\u0224;\u0001\u0000\u0000\u0000"
        + "\u0225\u0223\u0001\u0000\u0000\u0000\u0226\u0227\u0003\\.\u0000\u0227"
        + "\u0228\u0005g\u0000\u0000\u0228\u022d\u0003\\.\u0000\u0229\u022a\u0005"
        + "h\u0000\u0000\u022a\u022c\u0003\\.\u0000\u022b\u0229\u0001\u0000\u0000"
        + "\u0000\u022c\u022f\u0001\u0000\u0000\u0000\u022d\u022b\u0001\u0000\u0000"
        + "\u0000\u022d\u022e\u0001\u0000\u0000\u0000\u022e\u0239\u0001\u0000\u0000"
        + "\u0000\u022f\u022d\u0001\u0000\u0000\u0000\u0230\u0235\u0003\\.\u0000"
        + "\u0231\u0232\u0005h\u0000\u0000\u0232\u0234\u0003\\.\u0000\u0233\u0231"
        + "\u0001\u0000\u0000\u0000\u0234\u0237\u0001\u0000\u0000\u0000\u0235\u0233"
        + "\u0001\u0000\u0000\u0000\u0235\u0236\u0001\u0000\u0000\u0000\u0236\u0239"
        + "\u0001\u0000\u0000\u0000\u0237\u0235\u0001\u0000\u0000\u0000\u0238\u0226"
        + "\u0001\u0000\u0000\u0000\u0238\u0230\u0001\u0000\u0000\u0000\u0239=\u0001"
        + "\u0000\u0000\u0000\u023a\u023b\u0005>\u0000\u0000\u023b\u0247\u0005R\u0000"
        + "\u0000\u023c\u0241\u0003@ \u0000\u023d\u023e\u0005f\u0000\u0000\u023e"
        + "\u0240\u0003@ \u0000\u023f\u023d\u0001\u0000\u0000\u0000\u0240\u0243\u0001"
        + "\u0000\u0000\u0000\u0241\u023f\u0001\u0000\u0000\u0000\u0241\u0242\u0001"
        + "\u0000\u0000\u0000\u0242\u0244\u0001\u0000\u0000\u0000\u0243\u0241\u0001"
        + "\u0000\u0000\u0000\u0244\u0245\u0005R\u0000\u0000\u0245\u0247\u0001\u0000"
        + "\u0000\u0000\u0246\u023a\u0001\u0000\u0000\u0000\u0246\u023c\u0001\u0000"
        + "\u0000\u0000\u0247?\u0001\u0000\u0000\u0000\u0248\u0249\u0007\u0003\u0000"
        + "\u0000\u0249A\u0001\u0000\u0000\u0000\u024a\u024b\u0007\u0004\u0000\u0000"
        + "\u024b\u024c\u0003D\"\u0000\u024cC\u0001\u0000\u0000\u0000\u024d\u0252"
        + "\u0003F#\u0000\u024e\u024f\u0005f\u0000\u0000\u024f\u0251\u0003F#\u0000"
        + "\u0250\u024e\u0001\u0000\u0000\u0000\u0251\u0254\u0001\u0000\u0000\u0000"
        + "\u0252\u0250\u0001\u0000\u0000\u0000\u0252\u0253\u0001\u0000\u0000\u0000"
        + "\u0253E\u0001\u0000\u0000\u0000\u0254\u0252\u0001\u0000\u0000\u0000\u0255"
        + "\u0258\u0003\u008eG\u0000\u0256\u0257\u0005i\u0000\u0000\u0257\u0259\u0003"
        + "\u008eG\u0000\u0258\u0256\u0001\u0000\u0000\u0000\u0258\u0259\u0001\u0000"
        + "\u0000\u0000\u0259G\u0001\u0000\u0000\u0000\u025a\u025b\u0005\u001d\u0000"
        + "\u0000\u025b\u025c\u0003N\'\u0000\u025c\u025d\u0005\u001e\u0000\u0000"
        + "\u025d\u0266\u0003\u0016\u000b\u0000\u025e\u025f\u0005\u001f\u0000\u0000"
        + "\u025f\u0260\u0005\u001d\u0000\u0000\u0260\u0261\u0003N\'\u0000\u0261"
        + "\u0262\u0005\u001e\u0000\u0000\u0262\u0263\u0003\u0016\u000b\u0000\u0263"
        + "\u0265\u0001\u0000\u0000\u0000\u0264\u025e\u0001\u0000\u0000\u0000\u0265"
        + "\u0268\u0001\u0000\u0000\u0000\u0266\u0264\u0001\u0000\u0000\u0000\u0266"
        + "\u0267\u0001\u0000\u0000\u0000\u0267\u026b\u0001\u0000\u0000\u0000\u0268"
        + "\u0266\u0001\u0000\u0000\u0000\u0269\u026a\u0005\u001f\u0000\u0000\u026a"
        + "\u026c\u0003\u0016\u000b\u0000\u026b\u0269\u0001\u0000\u0000\u0000\u026b"
        + "\u026c\u0001\u0000\u0000\u0000\u026c\u026d\u0001\u0000\u0000\u0000\u026d"
        + "\u026e\u0005c\u0000\u0000\u026eI\u0001\u0000\u0000\u0000\u026f\u0270\u0005"
        + "\f\u0000\u0000\u0270\u0271\u0005\u000f\u0000\u0000\u0271\u0272\u0005/"
        + "\u0000\u0000\u0272\u0273\u0003j5\u0000\u0273\u0274\u00057\u0000\u0000"
        + "\u0274\u0277\u0003j5\u0000\u0275\u0276\u0005\u000e\u0000\u0000\u0276\u0278"
        + "\u0003j5\u0000\u0277\u0275\u0001\u0000\u0000\u0000\u0277\u0278\u0001\u0000"
        + "\u0000\u0000\u0278\u0279\u0001\u0000\u0000\u0000\u0279\u027a\u0005b\u0000"
        + "\u0000\u027a\u027b\u0003\u0016\u000b\u0000\u027b\u027c\u0005c\u0000\u0000"
        + "\u027c\u0286\u0001\u0000\u0000\u0000\u027d\u027e\u0005\f\u0000\u0000\u027e"
        + "\u027f\u0005\u000f\u0000\u0000\u027f\u0280\u0005B\u0000\u0000\u0280\u0281"
        + "\u0003j5\u0000\u0281\u0282\u0005b\u0000\u0000\u0282\u0283\u0003\u0016"
        + "\u000b\u0000\u0283\u0284\u0005c\u0000\u0000\u0284\u0286\u0001\u0000\u0000"
        + "\u0000\u0285\u026f\u0001\u0000\u0000\u0000\u0285\u027d\u0001\u0000\u0000"
        + "\u0000\u0286K\u0001\u0000\u0000\u0000\u0287\u0288\u0005\r\u0000\u0000"
        + "\u0288\u0289\u0003N\'\u0000\u0289\u028a\u0005b\u0000\u0000\u028a\u028b"
        + "\u0003\u0016\u000b\u0000\u028b\u028c\u0005c\u0000\u0000\u028cM\u0001\u0000"
        + "\u0000\u0000\u028d\u028e\u0006\'\uffff\uffff\u0000\u028e\u02c0\u0005%"
        + "\u0000\u0000\u028f\u02c0\u0005&\u0000\u0000\u0290\u0291\u0005j\u0000\u0000"
        + "\u0291\u0292\u0003N\'\u0000\u0292\u0293\u0005k\u0000\u0000\u0293\u02c0"
        + "\u0001\u0000\u0000\u0000\u0294\u0295\u0005j\u0000\u0000\u0295\u0296\u0003"
        + "j5\u0000\u0296\u0297\u0005k\u0000\u0000\u0297\u02c0\u0001\u0000\u0000"
        + "\u0000\u0298\u0299\u0005\'\u0000\u0000\u0299\u02c0\u0003N\'\u0005\u029a"
        + "\u029c\u0003R)\u0000\u029b\u029a\u0001\u0000\u0000\u0000\u029b\u029c\u0001"
        + "\u0000\u0000\u0000\u029c\u029d\u0001\u0000\u0000\u0000\u029d\u029e\u0003"
        + "T*\u0000\u029e\u029f\u0005 \u0000\u0000\u029f\u02a5\u0003P(\u0000\u02a0"
        + "\u02a6\u0003\u008eG\u0000\u02a1\u02a2\u0005j\u0000\u0000\u02a2\u02a3\u0003"
        + "j5\u0000\u02a3\u02a4\u0005k\u0000\u0000\u02a4\u02a6\u0001\u0000\u0000"
        + "\u0000\u02a5\u02a0\u0001\u0000\u0000\u0000\u02a5\u02a1\u0001\u0000\u0000"
        + "\u0000\u02a6\u02a8\u0001\u0000\u0000\u0000\u02a7\u02a9\u00036\u001b\u0000"
        + "\u02a8\u02a7\u0001\u0000\u0000\u0000\u02a8\u02a9\u0001\u0000\u0000\u0000"
        + "\u02a9\u02ab\u0001\u0000\u0000\u0000\u02aa\u02ac\u00038\u001c\u0000\u02ab"
        + "\u02aa\u0001\u0000\u0000\u0000\u02ab\u02ac\u0001\u0000\u0000\u0000\u02ac"
        + "\u02af\u0001\u0000\u0000\u0000\u02ad\u02ae\u0005?\u0000\u0000\u02ae\u02b0"
        + "\u00034\u001a\u0000\u02af\u02ad\u0001\u0000\u0000\u0000\u02af\u02b0\u0001"
        + "\u0000\u0000\u0000\u02b0\u02c0\u0001\u0000\u0000\u0000\u02b1\u02b3\u0005"
        + "^\u0000\u0000\u02b2\u02b4\u0007\u0002\u0000\u0000\u02b3\u02b2\u0001\u0000"
        + "\u0000\u0000\u02b3\u02b4\u0001\u0000\u0000\u0000\u02b4\u02bd\u0001\u0000"
        + "\u0000\u0000\u02b5\u02bb\u0003P(\u0000\u02b6\u02bc\u0003\u008eG\u0000"
        + "\u02b7\u02b8\u0005j\u0000\u0000\u02b8\u02b9\u0003j5\u0000\u02b9\u02ba"
        + "\u0005k\u0000\u0000\u02ba\u02bc\u0001\u0000\u0000\u0000\u02bb\u02b6\u0001"
        + "\u0000\u0000\u0000\u02bb\u02b7\u0001\u0000\u0000\u0000\u02bc\u02be\u0001"
        + "\u0000\u0000\u0000\u02bd\u02b5\u0001\u0000\u0000\u0000\u02bd\u02be\u0001"
        + "\u0000\u0000\u0000\u02be\u02c0\u0001\u0000\u0000\u0000\u02bf\u028d\u0001"
        + "\u0000\u0000\u0000\u02bf\u028f\u0001\u0000\u0000\u0000\u02bf\u0290\u0001"
        + "\u0000\u0000\u0000\u02bf\u0294\u0001\u0000\u0000\u0000\u02bf\u0298\u0001"
        + "\u0000\u0000\u0000\u02bf\u029b\u0001\u0000\u0000\u0000\u02bf\u02b1\u0001"
        + "\u0000\u0000\u0000\u02c0\u02c9\u0001\u0000\u0000\u0000\u02c1\u02c2\n\u0004"
        + "\u0000\u0000\u02c2\u02c3\u0005(\u0000\u0000\u02c3\u02c8\u0003N\'\u0005"
        + "\u02c4\u02c5\n\u0003\u0000\u0000\u02c5\u02c6\u0005)\u0000\u0000\u02c6"
        + "\u02c8\u0003N\'\u0004\u02c7\u02c1\u0001\u0000\u0000\u0000\u02c7\u02c4"
        + "\u0001\u0000\u0000\u0000\u02c8\u02cb\u0001\u0000\u0000\u0000\u02c9\u02c7"
        + "\u0001\u0000\u0000\u0000\u02c9\u02ca\u0001\u0000\u0000\u0000\u02caO\u0001"
        + "\u0000\u0000\u0000\u02cb\u02c9\u0001\u0000\u0000\u0000\u02cc\u02cd\u0007"
        + "\u0005\u0000\u0000\u02cdQ\u0001\u0000\u0000\u0000\u02ce\u02cf\u0007\u0006"
        + "\u0000\u0000\u02cfS\u0001\u0000\u0000\u0000\u02d0\u02d5\u0003X,\u0000"
        + "\u02d1\u02d2\u0005f\u0000\u0000\u02d2\u02d4\u0003X,\u0000\u02d3\u02d1"
        + "\u0001\u0000\u0000\u0000\u02d4\u02d7\u0001\u0000\u0000\u0000\u02d5\u02d3"
        + "\u0001\u0000\u0000\u0000\u02d5\u02d6\u0001\u0000\u0000\u0000\u02d6\u02d9"
        + "\u0001\u0000\u0000\u0000\u02d7\u02d5\u0001\u0000\u0000\u0000\u02d8\u02da"
        + "\u0003V+\u0000\u02d9\u02d8\u0001\u0000\u0000\u0000\u02d9\u02da\u0001\u0000"
        + "\u0000\u0000\u02da\u02dc\u0001\u0000\u0000\u0000\u02db\u02dd\u0003>\u001f"
        + "\u0000\u02dc\u02db\u0001\u0000\u0000\u0000\u02dc\u02dd\u0001\u0000\u0000"
        + "\u0000\u02dd\u02df\u0001\u0000\u0000\u0000\u02de\u02e0\u0003B!\u0000\u02df"
        + "\u02de\u0001\u0000\u0000\u0000\u02df\u02e0\u0001\u0000\u0000\u0000\u02e0"
        + "U\u0001\u0000\u0000\u0000\u02e1\u02e2\u0005G\u0000\u0000\u02e2\u02e3\u0005"
        + "H\u0000\u0000\u02e3\u02e4\u0005I\u0000\u0000\u02e4\u02e5\u0007\u0007\u0000"
        + "\u0000\u02e5W\u0001\u0000\u0000\u0000\u02e6\u02e9\u0003\\.\u0000\u02e7"
        + "\u02e9\u0003\u008cF\u0000\u02e8\u02e6\u0001\u0000\u0000\u0000\u02e8\u02e7"
        + "\u0001\u0000\u0000\u0000\u02e9Y\u0001\u0000\u0000\u0000\u02ea\u02eb\u0005"
        + "A\u0000\u0000\u02eb\u02ec\u0007\u0004\u0000\u0000\u02ec\u02ed\u0005B\u0000"
        + "\u0000\u02ed[\u0001\u0000\u0000\u0000\u02ee\u02ef\u0007\b\u0000\u0000"
        + "\u02ef]\u0001\u0000\u0000\u0000\u02f0\u02f1\u0005\b\u0000\u0000\u02f1"
        + "\u02f2\u0005\u000f\u0000\u0000\u02f2\u02f3\u0005/\u0000\u0000\u02f3\u02f4"
        + "\u0003j5\u0000\u02f4_\u0001\u0000\u0000\u0000\u02f5\u02f6\u0005\u0006"
        + "\u0000\u0000\u02f6\u02f7\u0005\u000f\u0000\u0000\u02f7\u02f8\u0005/\u0000"
        + "\u0000\u02f8\u02f9\u0003j5\u0000\u02f9a\u0001\u0000\u0000\u0000\u02fa"
        + "\u02fc\u0005\u0007\u0000\u0000\u02fb\u02fa\u0001\u0000\u0000\u0000\u02fb"
        + "\u02fc\u0001\u0000\u0000\u0000\u02fc\u02fd\u0001\u0000\u0000\u0000\u02fd"
        + "\u02fe\u0003h4\u0000\u02fe\u02ff\u0005/\u0000\u0000\u02ff\u0300\u0003"
        + "j5\u0000\u0300c\u0001\u0000\u0000\u0000\u0301\u0302\u0005\u000b\u0000"
        + "\u0000\u0302\u0303\u0005j\u0000\u0000\u0303\u0308\u0003j5\u0000\u0304"
        + "\u0305\u0005f\u0000\u0000\u0305\u0307\u0003j5\u0000\u0306\u0304\u0001"
        + "\u0000\u0000\u0000\u0307\u030a\u0001\u0000\u0000\u0000\u0308\u0306\u0001"
        + "\u0000\u0000\u0000\u0308\u0309\u0001\u0000\u0000\u0000\u0309\u030b\u0001"
        + "\u0000\u0000\u0000\u030a\u0308\u0001\u0000\u0000\u0000\u030b\u030c\u0005"
        + "k\u0000\u0000\u030ce\u0001\u0000\u0000\u0000\u030d\u030e\u0005\u0001\u0000"
        + "\u0000\u030e\u030f\u0005j\u0000\u0000\u030f\u0310\u0003j5\u0000\u0310"
        + "\u0311\u0005f\u0000\u0000\u0311\u0318\u0003j5\u0000\u0312\u0313\u0005"
        + "f\u0000\u0000\u0313\u0316\u0003j5\u0000\u0314\u0315\u0005f\u0000\u0000"
        + "\u0315\u0317\u0003j5\u0000\u0316\u0314\u0001\u0000\u0000\u0000\u0316\u0317"
        + "\u0001\u0000\u0000\u0000\u0317\u0319\u0001\u0000\u0000\u0000\u0318\u0312"
        + "\u0001\u0000\u0000\u0000\u0318\u0319\u0001\u0000\u0000\u0000\u0319\u031a"
        + "\u0001\u0000\u0000\u0000\u031a\u031d\u0005k\u0000\u0000\u031b\u031c\u0005"
        + "\u0002\u0000\u0000\u031c\u031e\u0005\u000f\u0000\u0000\u031d\u031b\u0001"
        + "\u0000\u0000\u0000\u031d\u031e\u0001\u0000\u0000\u0000\u031eg\u0001\u0000"
        + "\u0000\u0000\u031f\u0328\u0005\u000f\u0000\u0000\u0320\u0321\u0005\u0014"
        + "\u0000\u0000\u0321\u0327\u0003\u008aE\u0000\u0322\u0323\u0005\u0010\u0000"
        + "\u0000\u0323\u0324\u0003j5\u0000\u0324\u0325\u0005\u0011\u0000\u0000\u0325"
        + "\u0327\u0001\u0000\u0000\u0000\u0326\u0320\u0001\u0000\u0000\u0000\u0326"
        + "\u0322\u0001\u0000\u0000\u0000\u0327\u032a\u0001\u0000\u0000\u0000\u0328"
        + "\u0326\u0001\u0000\u0000\u0000\u0328\u0329\u0001\u0000\u0000\u0000\u0329"
        + "i\u0001\u0000\u0000\u0000\u032a\u0328\u0001\u0000\u0000\u0000\u032b\u032c"
        + "\u0003l6\u0000\u032ck\u0001\u0000\u0000\u0000\u032d\u0332\u0003n7\u0000"
        + "\u032e\u032f\u0005)\u0000\u0000\u032f\u0331\u0003n7\u0000\u0330\u032e"
        + "\u0001\u0000\u0000\u0000\u0331\u0334\u0001\u0000\u0000\u0000\u0332\u0330"
        + "\u0001\u0000\u0000\u0000\u0332\u0333\u0001\u0000\u0000\u0000\u0333m\u0001"
        + "\u0000\u0000\u0000\u0334\u0332\u0001\u0000\u0000\u0000\u0335\u033a\u0003"
        + "p8\u0000\u0336\u0337\u0005(\u0000\u0000\u0337\u0339\u0003p8\u0000\u0338"
        + "\u0336\u0001\u0000\u0000\u0000\u0339\u033c\u0001\u0000\u0000\u0000\u033a"
        + "\u0338\u0001\u0000\u0000\u0000\u033a\u033b\u0001\u0000\u0000\u0000\u033b"
        + "o\u0001\u0000\u0000\u0000\u033c\u033a\u0001\u0000\u0000\u0000\u033d\u033e"
        + "\u0005\'\u0000\u0000\u033e\u0341\u0003p8\u0000\u033f\u0341\u0003r9\u0000"
        + "\u0340\u033d\u0001\u0000\u0000\u0000\u0340\u033f\u0001\u0000\u0000\u0000"
        + "\u0341q\u0001\u0000\u0000\u0000\u0342\u0346\u0003t:\u0000\u0343\u0344"
        + "\u0003P(\u0000\u0344\u0345\u0003t:\u0000\u0345\u0347\u0001\u0000\u0000"
        + "\u0000\u0346\u0343\u0001\u0000\u0000\u0000\u0346\u0347\u0001\u0000\u0000"
        + "\u0000\u0347s\u0001\u0000\u0000\u0000\u0348\u034d\u0003v;\u0000\u0349"
        + "\u034a\u0005\u0017\u0000\u0000\u034a\u034c\u0003v;\u0000\u034b\u0349\u0001"
        + "\u0000\u0000\u0000\u034c\u034f\u0001\u0000\u0000\u0000\u034d\u034b\u0001"
        + "\u0000\u0000\u0000\u034d\u034e\u0001\u0000\u0000\u0000\u034eu\u0001\u0000"
        + "\u0000\u0000\u034f\u034d\u0001\u0000\u0000\u0000\u0350\u0355\u0003x<\u0000"
        + "\u0351\u0352\u0005\u0018\u0000\u0000\u0352\u0354\u0003x<\u0000\u0353\u0351"
        + "\u0001\u0000\u0000\u0000\u0354\u0357\u0001\u0000\u0000\u0000\u0355\u0353"
        + "\u0001\u0000\u0000\u0000\u0355\u0356\u0001\u0000\u0000\u0000\u0356w\u0001"
        + "\u0000\u0000\u0000\u0357\u0355\u0001\u0000\u0000\u0000\u0358\u035d\u0003"
        + "z=\u0000\u0359\u035a\u0005\u0016\u0000\u0000\u035a\u035c\u0003z=\u0000"
        + "\u035b\u0359\u0001\u0000\u0000\u0000\u035c\u035f\u0001\u0000\u0000\u0000"
        + "\u035d\u035b\u0001\u0000\u0000\u0000\u035d\u035e\u0001\u0000\u0000\u0000"
        + "\u035ey\u0001\u0000\u0000\u0000\u035f\u035d\u0001\u0000\u0000\u0000\u0360"
        + "\u0365\u0003|>\u0000\u0361\u0362\u0007\t\u0000\u0000\u0362\u0364\u0003"
        + "|>\u0000\u0363\u0361\u0001\u0000\u0000\u0000\u0364\u0367\u0001\u0000\u0000"
        + "\u0000\u0365\u0363\u0001\u0000\u0000\u0000\u0365\u0366\u0001\u0000\u0000"
        + "\u0000\u0366{\u0001\u0000\u0000\u0000\u0367\u0365\u0001\u0000\u0000\u0000"
        + "\u0368\u036d\u0003~?\u0000\u0369\u036a\u0007\n\u0000\u0000\u036a\u036c"
        + "\u0003~?\u0000\u036b\u0369\u0001\u0000\u0000\u0000\u036c\u036f\u0001\u0000"
        + "\u0000\u0000\u036d\u036b\u0001\u0000\u0000\u0000\u036d\u036e\u0001\u0000"
        + "\u0000\u0000\u036e}\u0001\u0000\u0000\u0000\u036f\u036d\u0001\u0000\u0000"
        + "\u0000\u0370\u0379\u0003\u0082A\u0000\u0371\u0375\u0003\u0080@\u0000\u0372"
        + "\u0375\u0005h\u0000\u0000\u0373\u0375\u0005\u0015\u0000\u0000\u0374\u0371"
        + "\u0001\u0000\u0000\u0000\u0374\u0372\u0001\u0000\u0000\u0000\u0374\u0373"
        + "\u0001\u0000\u0000\u0000\u0375\u0376\u0001\u0000\u0000\u0000\u0376\u0378"
        + "\u0003\u0082A\u0000\u0377\u0374\u0001\u0000\u0000\u0000\u0378\u037b\u0001"
        + "\u0000\u0000\u0000\u0379\u0377\u0001\u0000\u0000\u0000\u0379\u037a\u0001"
        + "\u0000\u0000\u0000\u037a\u007f\u0001\u0000\u0000\u0000\u037b\u0379\u0001"
        + "\u0000\u0000\u0000\u037c\u037d\u0004@\u0004\u0000\u037d\u037e\u0005n\u0000"
        + "\u0000\u037e\u0081\u0001\u0000\u0000\u0000\u037f\u0380\u0007\u000b\u0000"
        + "\u0000\u0380\u0383\u0003\u0082A\u0000\u0381\u0383\u0003\u0084B\u0000\u0382"
        + "\u037f\u0001\u0000\u0000\u0000\u0382\u0381\u0001\u0000\u0000\u0000\u0383"
        + "\u0083\u0001\u0000\u0000\u0000\u0384\u039b\u0003\u0086C\u0000\u0385\u0386"
        + "\u0005\u0014\u0000\u0000\u0386\u0393\u0003\u008aE\u0000\u0387\u0390\u0005"
        + "j\u0000\u0000\u0388\u038d\u0003j5\u0000\u0389\u038a\u0005f\u0000\u0000"
        + "\u038a\u038c\u0003j5\u0000\u038b\u0389\u0001\u0000\u0000\u0000\u038c\u038f"
        + "\u0001\u0000\u0000\u0000\u038d\u038b\u0001\u0000\u0000\u0000\u038d\u038e"
        + "\u0001\u0000\u0000\u0000\u038e\u0391\u0001\u0000\u0000\u0000\u038f\u038d"
        + "\u0001\u0000\u0000\u0000\u0390\u0388\u0001\u0000\u0000\u0000\u0390\u0391"
        + "\u0001\u0000\u0000\u0000\u0391\u0392\u0001\u0000\u0000\u0000\u0392\u0394"
        + "\u0005k\u0000\u0000\u0393\u0387\u0001\u0000\u0000\u0000\u0393\u0394\u0001"
        + "\u0000\u0000\u0000\u0394\u039a\u0001\u0000\u0000\u0000\u0395\u0396\u0005"
        + "\u0010\u0000\u0000\u0396\u0397\u0003j5\u0000\u0397\u0398\u0005\u0011\u0000"
        + "\u0000\u0398\u039a\u0001\u0000\u0000\u0000\u0399\u0385\u0001\u0000\u0000"
        + "\u0000\u0399\u0395\u0001\u0000\u0000\u0000\u039a\u039d\u0001\u0000\u0000"
        + "\u0000\u039b\u0399\u0001\u0000\u0000\u0000\u039b\u039c\u0001\u0000\u0000"
        + "\u0000\u039c\u0085\u0001\u0000\u0000\u0000\u039d\u039b\u0001\u0000\u0000"
        + "\u0000\u039e\u03c7\u0005m\u0000\u0000\u039f\u03c7\u0005o\u0000\u0000\u03a0"
        + "\u03c7\u0005\u000f\u0000\u0000\u03a1\u03c7\u0005%\u0000\u0000\u03a2\u03c7"
        + "\u0005&\u0000\u0000\u03a3\u03a4\u0005j\u0000\u0000\u03a4\u03a5\u0003j"
        + "5\u0000\u03a5\u03a6\u0005k\u0000\u0000\u03a6\u03c7\u0001\u0000\u0000\u0000"
        + "\u03a7\u03c7\u0003\f\u0006\u0000\u03a8\u03b4\u0005\u0010\u0000\u0000\u03a9"
        + "\u03ae\u0003j5\u0000\u03aa\u03ab\u0005f\u0000\u0000\u03ab\u03ad\u0003"
        + "j5\u0000\u03ac\u03aa\u0001\u0000\u0000\u0000\u03ad\u03b0\u0001\u0000\u0000"
        + "\u0000\u03ae\u03ac\u0001\u0000\u0000\u0000\u03ae\u03af\u0001\u0000\u0000"
        + "\u0000\u03af\u03b2\u0001\u0000\u0000\u0000\u03b0\u03ae\u0001\u0000\u0000"
        + "\u0000\u03b1\u03b3\u0005f\u0000\u0000\u03b2\u03b1\u0001\u0000\u0000\u0000"
        + "\u03b2\u03b3\u0001\u0000\u0000\u0000\u03b3\u03b5\u0001\u0000\u0000\u0000"
        + "\u03b4\u03a9\u0001\u0000\u0000\u0000\u03b4\u03b5\u0001\u0000\u0000\u0000"
        + "\u03b5\u03b6\u0001\u0000\u0000\u0000\u03b6\u03c7\u0005\u0011\u0000\u0000"
        + "\u03b7\u03c3\u0005\u0012\u0000\u0000\u03b8\u03bd\u0003\u0088D\u0000\u03b9"
        + "\u03ba\u0005f\u0000\u0000\u03ba\u03bc\u0003\u0088D\u0000\u03bb\u03b9\u0001"
        + "\u0000\u0000\u0000\u03bc\u03bf\u0001\u0000\u0000\u0000\u03bd\u03bb\u0001"
        + "\u0000\u0000\u0000\u03bd\u03be\u0001\u0000\u0000\u0000\u03be\u03c1\u0001"
        + "\u0000\u0000\u0000\u03bf\u03bd\u0001\u0000\u0000\u0000\u03c0\u03c2\u0005"
        + "f\u0000\u0000\u03c1\u03c0\u0001\u0000\u0000\u0000\u03c1\u03c2\u0001\u0000"
        + "\u0000\u0000\u03c2\u03c4\u0001\u0000\u0000\u0000\u03c3\u03b8\u0001\u0000"
        + "\u0000\u0000\u03c3\u03c4\u0001\u0000\u0000\u0000\u03c4\u03c5\u0001\u0000"
        + "\u0000\u0000\u03c5\u03c7\u0005\u0013\u0000\u0000\u03c6\u039e\u0001\u0000"
        + "\u0000\u0000\u03c6\u039f\u0001\u0000\u0000\u0000\u03c6\u03a0\u0001\u0000"
        + "\u0000\u0000\u03c6\u03a1\u0001\u0000\u0000\u0000\u03c6\u03a2\u0001\u0000"
        + "\u0000\u0000\u03c6\u03a3\u0001\u0000\u0000\u0000\u03c6\u03a7\u0001\u0000"
        + "\u0000\u0000\u03c6\u03a8\u0001\u0000\u0000\u0000\u03c6\u03b7\u0001\u0000"
        + "\u0000\u0000\u03c7\u0087\u0001\u0000\u0000\u0000\u03c8\u03c9\u0003\u008a"
        + "E\u0000\u03c9\u03ca\u0005g\u0000\u0000\u03ca\u03cb\u0003j5\u0000\u03cb"
        + "\u0089\u0001\u0000\u0000\u0000\u03cc\u03cd\u0007\f\u0000\u0000\u03cd\u008b"
        + "\u0001\u0000\u0000\u0000\u03ce\u03cf\u0005o\u0000\u0000\u03cf\u008d\u0001"
        + "\u0000\u0000\u0000\u03d0\u03d1\u0005m\u0000\u0000\u03d1\u008f\u0001\u0000"
        + "\u0000\u0000\u0086\u0091\u0095\u0097\u009d\u00a3\u00b0\u00b3\u00c1\u00cf"
        + "\u00d2\u00d5\u00dc\u00ea\u00ed\u010d\u0110\u0113\u0117\u011d\u0120\u0125"
        + "\u0134\u0138\u013d\u0141\u0146\u014b\u0150\u0152\u0156\u015c\u0160\u0164"
        + "\u0167\u016b\u0170\u0175\u0178\u017a\u017e\u0181\u0185\u0188\u018d\u0190"
        + "\u0195\u0198\u019a\u01a5\u01a9\u01ac\u01b0\u01b4\u01b7\u01be\u01c5\u01c8"
        + "\u01d0\u01d3\u01db\u01df\u01e3\u01e5\u01e7\u01e9\u01ec\u01f3\u01f7\u01fe"
        + "\u0202\u0208\u0213\u0216\u0219\u0221\u0223\u022d\u0235\u0238\u0241\u0246"
        + "\u0252\u0258\u0266\u026b\u0277\u0285\u029b\u02a5\u02a8\u02ab\u02af\u02b3"
        + "\u02bb\u02bd\u02bf\u02c7\u02c9\u02d5\u02d9\u02dc\u02df\u02e8\u02fb\u0308"
        + "\u0316\u0318\u031d\u0326\u0328\u0332\u033a\u0340\u0346\u034d\u0355\u035d"
        + "\u0365\u036d\u0374\u0379\u0382\u038d\u0390\u0393\u0399\u039b\u03ae\u03b2"
        + "\u03b4\u03bd\u03c1\u03c3\u03c6";
    public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
