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
    public static final int LET = 1, SET = 2, STATE = 3, VARIABLE = 4, LBRACKET = 5, RBRACKET = 6, LBRACE = 7,
        RBRACE = 8, DOT = 9, PERCENT = 10, IF = 11, THEN = 12, ELSE = 13, HAS = 14, OVERALL = 15, SOME = 16, ONE = 17,
        LONE = 18, TRUE = 19, FALSE = 20, NOT = 21, AND = 22, OR = 23, GT = 24, GT_SYMBOL = 25, LT = 26, LT_SYMBOL = 27,
        EQ = 28, EQ_SYMBOL = 29, LE = 30, LE_SYMBOL = 31, GE = 32, GE_SYMBOL = 33, FROM = 34, TO = 35, INPUT = 36,
        OUTPUT = 37, WHERE = 38, SLOTS = 39, SLOT = 40, RETAIN = 41, EACH = 42, EXCEPT = 43, FORGET = 44, EMPTY = 45,
        IN = 46, WITHOUT = 47, WITH = 48, TAG = 49, HASHTAG = 50, ROUND = 51, ROBIN = 52, BY = 53, LABEL = 54,
        BLOCK = 55, TOP = 56, BOTTOM = 57, NORTH = 58, EAST = 59, SOUTH = 60, WEST = 61, SIDE = 62, LEFT = 63,
        RIGHT = 64, FRONT = 65, BACK = 66, NULL = 67, TICKS = 68, TICK = 69, SECONDS = 70, SECOND = 71, GLOBAL = 72,
        PLUS = 73, REDSTONE = 74, LEVEL = 75, LEVELS = 76, PULSE = 77, DO = 78, END = 79, NAME = 80, EVERY = 81,
        COMMA = 82, COLON = 83, SLASH = 84, DASH = 85, LPAREN = 86, RPAREN = 87, NUMBER_WITH_G_SUFFIX = 88, NUMBER = 89,
        IDENTIFIER = 90, STRING = 91, LINE_COMMENT = 92, WS = 93, UNUSED = 94;
    public static final int RULE_program = 0, RULE_name = 1, RULE_trigger = 2, RULE_interval = 3, RULE_block = 4,
        RULE_statement = 5, RULE_redstoneOutputStatement = 6, RULE_forgetStatement = 7, RULE_inputStatement = 8,
        RULE_outputStatement = 9, RULE_inputResourceLimits = 10, RULE_outputResourceLimits = 11,
        RULE_resourceLimitList = 12, RULE_resourceLimit = 13, RULE_limit = 14, RULE_quantity = 15, RULE_retention = 16,
        RULE_resourceExclusion = 17, RULE_resourceId = 18, RULE_resourceIdList = 19, RULE_resourceIdDisjunction = 20,
        RULE_with = 21, RULE_withClause = 22, RULE_tagMatcher = 23, RULE_sidequalifier = 24, RULE_side = 25,
        RULE_slotqualifier = 26, RULE_rangeset = 27, RULE_range = 28, RULE_ifStatement = 29, RULE_boolexpr = 30,
        RULE_comparisonOp = 31, RULE_setOp = 32, RULE_labelAccess = 33, RULE_roundrobin = 34, RULE_label = 35,
        RULE_emptyslots = 36, RULE_identifier = 37, RULE_stateDeclaration = 38, RULE_dataDeclaration = 39,
        RULE_dataAssignment = 40, RULE_dataTarget = 41, RULE_dataExpression = 42, RULE_dataOr = 43, RULE_dataAnd = 44,
        RULE_dataNot = 45, RULE_dataComparison = 46, RULE_dataAdd = 47, RULE_dataMultiply = 48,
        RULE_multiplyOperator = 49, RULE_dataSign = 50, RULE_dataPostfix = 51, RULE_dataPrimary = 52,
        RULE_dataField = 53, RULE_dataName = 54, RULE_string = 55, RULE_number = 56;

    private static String[] makeRuleNames() {
        return new String[] { "program", "name", "trigger", "interval", "block", "statement", "redstoneOutputStatement",
            "forgetStatement", "inputStatement", "outputStatement", "inputResourceLimits", "outputResourceLimits",
            "resourceLimitList", "resourceLimit", "limit", "quantity", "retention", "resourceExclusion", "resourceId",
            "resourceIdList", "resourceIdDisjunction", "with", "withClause", "tagMatcher", "sidequalifier", "side",
            "slotqualifier", "rangeset", "range", "ifStatement", "boolexpr", "comparisonOp", "setOp", "labelAccess",
            "roundrobin", "label", "emptyslots", "identifier", "stateDeclaration", "dataDeclaration", "dataAssignment",
            "dataTarget", "dataExpression", "dataOr", "dataAnd", "dataNot", "dataComparison", "dataAdd", "dataMultiply",
            "multiplyOperator", "dataSign", "dataPostfix", "dataPrimary", "dataField", "dataName", "string", "number" };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] { null, null, null, null, null, "'['", "']'", "'{'", "'}'", "'.'", "'%'", null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, "'>'", null, "'<'", null, "'='", null,
            "'<='", null, "'>='", null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, "'#'", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "','",
            "':'", "'/'", "'-'", "'('", "')'" };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[] { null, "LET", "SET", "STATE", "VARIABLE", "LBRACKET", "RBRACKET", "LBRACE", "RBRACE",
            "DOT", "PERCENT", "IF", "THEN", "ELSE", "HAS", "OVERALL", "SOME", "ONE", "LONE", "TRUE", "FALSE", "NOT",
            "AND", "OR", "GT", "GT_SYMBOL", "LT", "LT_SYMBOL", "EQ", "EQ_SYMBOL", "LE", "LE_SYMBOL", "GE", "GE_SYMBOL",
            "FROM", "TO", "INPUT", "OUTPUT", "WHERE", "SLOTS", "SLOT", "RETAIN", "EACH", "EXCEPT", "FORGET", "EMPTY",
            "IN", "WITHOUT", "WITH", "TAG", "HASHTAG", "ROUND", "ROBIN", "BY", "LABEL", "BLOCK", "TOP", "BOTTOM",
            "NORTH", "EAST", "SOUTH", "WEST", "SIDE", "LEFT", "RIGHT", "FRONT", "BACK", "NULL", "TICKS", "TICK",
            "SECONDS", "SECOND", "GLOBAL", "PLUS", "REDSTONE", "LEVEL", "LEVELS", "PULSE", "DO", "END", "NAME", "EVERY",
            "COMMA", "COLON", "SLASH", "DASH", "LPAREN", "RPAREN", "NUMBER_WITH_G_SUFFIX", "NUMBER", "IDENTIFIER",
            "STRING", "LINE_COMMENT", "WS", "UNUSED" };
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
                setState(115);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == NAME) {
                    {
                        setState(114);
                        name();
                    }
                }

                setState(120);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == STATE) {
                    {
                        {
                            setState(117);
                            stateDeclaration();
                        }
                    }
                    setState(122);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(126);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == EVERY) {
                    {
                        {
                            setState(123);
                            trigger();
                        }
                    }
                    setState(128);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(129);
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
        enterRule(_localctx, 2, RULE_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(131);
                match(NAME);
                setState(132);
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
        enterRule(_localctx, 4, RULE_trigger);
        try {
            setState(147);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 3, _ctx)) {
                case 1:
                    _localctx = new TimerTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(134);
                    match(EVERY);
                    setState(135);
                    interval();
                    setState(136);
                    match(DO);
                    setState(137);
                    block();
                    setState(138);
                    match(END);
                }
                    break;
                case 2:
                    _localctx = new PulseTriggerContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(140);
                    match(EVERY);
                    setState(141);
                    match(REDSTONE);
                    setState(142);
                    match(PULSE);
                    setState(143);
                    match(DO);
                    setState(144);
                    block();
                    setState(145);
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
        enterRule(_localctx, 6, RULE_interval);
        int _la;
        try {
            setState(166);
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
                    setState(150);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == NUMBER) {
                        {
                            setState(149);
                            match(NUMBER);
                        }
                    }

                    setState(153);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == GLOBAL) {
                        {
                            setState(152);
                            match(GLOBAL);
                        }
                    }

                    setState(157);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(155);
                            match(PLUS);
                            setState(156);
                            match(NUMBER);
                        }
                    }

                    setState(159);
                    _la = _input.LA(1);
                    if (!(((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 15L) != 0))) {
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
                    setState(160);
                    match(NUMBER_WITH_G_SUFFIX);
                    setState(163);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == PLUS) {
                        {
                            setState(161);
                            match(PLUS);
                            setState(162);
                            match(NUMBER);
                        }
                    }

                    setState(165);
                    _la = _input.LA(1);
                    if (!(((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 15L) != 0))) {
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
        enterRule(_localctx, 8, RULE_block);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(171);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 17849884084230L) != 0)) {
                    {
                        {
                            setState(168);
                            statement();
                        }
                    }
                    setState(173);
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
        enterRule(_localctx, 10, RULE_statement);
        try {
            setState(181);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 10, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(174);
                    dataDeclaration();
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(175);
                    dataAssignment();
                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(176);
                    inputStatement();
                }
                    break;
                case 4:
                    enterOuterAlt(_localctx, 4); {
                    setState(177);
                    redstoneOutputStatement();
                }
                    break;
                case 5:
                    enterOuterAlt(_localctx, 5); {
                    setState(178);
                    outputStatement();
                }
                    break;
                case 6:
                    enterOuterAlt(_localctx, 6); {
                    setState(179);
                    ifStatement();
                }
                    break;
                case 7:
                    enterOuterAlt(_localctx, 7); {
                    setState(180);
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
        enterRule(_localctx, 12, RULE_redstoneOutputStatement);
        int _la;
        try {
            setState(211);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(183);
                    match(OUTPUT);
                    setState(185);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(184);
                            quantity();
                        }
                    }

                    setState(187);
                    match(REDSTONE);
                    setState(188);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(190);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(189);
                            quantity();
                        }
                    }

                    setState(192);
                    match(TO);
                    setState(194);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(193);
                            match(EACH);
                        }
                    }

                    setState(196);
                    labelAccess();
                }
                    break;
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(197);
                    match(TO);
                    setState(199);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(198);
                            match(EACH);
                        }
                    }

                    setState(201);
                    labelAccess();
                    setState(202);
                    match(OUTPUT);
                    setState(204);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(203);
                            quantity();
                        }
                    }

                    setState(206);
                    match(REDSTONE);
                    setState(207);
                    _la = _input.LA(1);
                    if (!(_la == LEVEL || _la == LEVELS)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(209);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAREN || _la == NUMBER) {
                        {
                            setState(208);
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
        enterRule(_localctx, 14, RULE_forgetStatement);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(213);
                match(FORGET);
                setState(215);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 18, _ctx)) {
                    case 1: {
                        setState(214);
                        label();
                    }
                        break;
                }
                setState(221);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 19, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(217);
                                match(COMMA);
                                setState(218);
                                label();
                            }
                        }
                    }
                    setState(223);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 19, _ctx);
                }
                setState(225);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(224);
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
        enterRule(_localctx, 16, RULE_inputStatement);
        int _la;
        try {
            setState(251);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case INPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(227);
                    match(INPUT);
                    setState(229);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9006774843252670450L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 239082951L) != 0)) {
                        {
                            setState(228);
                            inputResourceLimits();
                        }
                    }

                    setState(232);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(231);
                            resourceExclusion();
                        }
                    }

                    setState(234);
                    match(FROM);
                    setState(236);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(235);
                            match(EACH);
                        }
                    }

                    setState(238);
                    labelAccess();
                }
                    break;
                case FROM:
                    enterOuterAlt(_localctx, 2); {
                    setState(239);
                    match(FROM);
                    setState(241);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(240);
                            match(EACH);
                        }
                    }

                    setState(243);
                    labelAccess();
                    setState(244);
                    match(INPUT);
                    setState(246);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 25, _ctx)) {
                        case 1: {
                            setState(245);
                            inputResourceLimits();
                        }
                            break;
                    }
                    setState(249);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(248);
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
        enterRule(_localctx, 18, RULE_outputStatement);
        int _la;
        try {
            setState(283);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case OUTPUT:
                    enterOuterAlt(_localctx, 1); {
                    setState(253);
                    match(OUTPUT);
                    setState(255);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9006774843252670450L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 239082951L) != 0)) {
                        {
                            setState(254);
                            outputResourceLimits();
                        }
                    }

                    setState(258);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(257);
                            resourceExclusion();
                        }
                    }

                    setState(260);
                    match(TO);
                    setState(262);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EMPTY) {
                        {
                            setState(261);
                            emptyslots();
                        }
                    }

                    setState(265);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(264);
                            match(EACH);
                        }
                    }

                    setState(267);
                    labelAccess();
                }
                    break;
                case TO:
                    enterOuterAlt(_localctx, 2); {
                    setState(268);
                    match(TO);
                    setState(270);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EMPTY) {
                        {
                            setState(269);
                            emptyslots();
                        }
                    }

                    setState(273);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EACH) {
                        {
                            setState(272);
                            match(EACH);
                        }
                    }

                    setState(275);
                    labelAccess();
                    setState(276);
                    match(OUTPUT);
                    setState(278);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 34, _ctx)) {
                        case 1: {
                            setState(277);
                            outputResourceLimits();
                        }
                            break;
                    }
                    setState(281);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == EXCEPT) {
                        {
                            setState(280);
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
        enterRule(_localctx, 20, RULE_inputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(285);
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
        enterRule(_localctx, 22, RULE_outputResourceLimits);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(287);
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
        enterRule(_localctx, 24, RULE_resourceLimitList);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(289);
                resourceLimit();
                setState(294);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 37, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(290);
                                match(COMMA);
                                setState(291);
                                resourceLimit();
                            }
                        }
                    }
                    setState(296);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 37, _ctx);
                }
                setState(298);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == COMMA) {
                    {
                        setState(297);
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
        enterRule(_localctx, 26, RULE_resourceLimit);
        int _la;
        try {
            setState(312);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 42, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(301);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (((((_la - 41)) & ~0x3f) == 0 && ((1L << (_la - 41)) & 316659348799489L) != 0)) {
                        {
                            setState(300);
                            limit();
                        }
                    }

                    setState(303);
                    resourceIdDisjunction();
                    setState(305);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == WITHOUT || _la == WITH) {
                        {
                            setState(304);
                            with();
                        }
                    }

                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(307);
                    limit();
                    setState(309);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == WITHOUT || _la == WITH) {
                        {
                            setState(308);
                            with();
                        }
                    }

                }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3); {
                    setState(311);
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
        enterRule(_localctx, 28, RULE_limit);
        try {
            setState(319);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 43, _ctx)) {
                case 1:
                    _localctx = new QuantityRetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(314);
                    quantity();
                    setState(315);
                    retention();
                }
                    break;
                case 2:
                    _localctx = new RetentionLimitContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(317);
                    retention();
                }
                    break;
                case 3:
                    _localctx = new QuantityLimitContext(_localctx);
                    enterOuterAlt(_localctx, 3); {
                    setState(318);
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
        enterRule(_localctx, 30, RULE_quantity);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(326);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(321);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(322);
                        match(LPAREN);
                        setState(323);
                        dataExpression();
                        setState(324);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(329);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == EACH) {
                    {
                        setState(328);
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
        enterRule(_localctx, 32, RULE_retention);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(331);
                match(RETAIN);
                setState(337);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        setState(332);
                        number();
                    }
                        break;
                    case LPAREN: {
                        setState(333);
                        match(LPAREN);
                        setState(334);
                        dataExpression();
                        setState(335);
                        match(RPAREN);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(340);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == EACH) {
                    {
                        setState(339);
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
        enterRule(_localctx, 34, RULE_resourceExclusion);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(342);
                match(EXCEPT);
                setState(343);
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
        enterRule(_localctx, 36, RULE_resourceId);
        try {
            setState(365);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case LET:
                case SET:
                case STATE:
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
                        setState(345);
                        identifier();
                    }
                    setState(362);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 53, _ctx)) {
                        case 1: {
                            setState(346);
                            match(COLON);
                            setState(348);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 48, _ctx)) {
                                case 1: {
                                    setState(347);
                                    identifier();
                                }
                                    break;
                            }
                            setState(360);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 52, _ctx)) {
                                case 1: {
                                    setState(350);
                                    match(COLON);
                                    setState(352);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 49, _ctx)) {
                                        case 1: {
                                            setState(351);
                                            identifier();
                                        }
                                            break;
                                    }
                                    setState(358);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 51, _ctx)) {
                                        case 1: {
                                            setState(354);
                                            match(COLON);
                                            setState(356);
                                            _errHandler.sync(this);
                                            switch (getInterpreter().adaptivePredict(_input, 50, _ctx)) {
                                                case 1: {
                                                    setState(355);
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
                    setState(364);
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
        enterRule(_localctx, 38, RULE_resourceIdList);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(367);
                resourceId();
                setState(372);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 55, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(368);
                                match(COMMA);
                                setState(369);
                                resourceId();
                            }
                        }
                    }
                    setState(374);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 55, _ctx);
                }
                setState(376);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 56, _ctx)) {
                    case 1: {
                        setState(375);
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
        enterRule(_localctx, 40, RULE_resourceIdDisjunction);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(378);
                resourceId();
                setState(383);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 57, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(379);
                                match(OR);
                                setState(380);
                                resourceId();
                            }
                        }
                    }
                    setState(385);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 57, _ctx);
                }
                setState(387);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 58, _ctx)) {
                    case 1: {
                        setState(386);
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
        enterRule(_localctx, 42, RULE_with);
        try {
            setState(393);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case WITH:
                    enterOuterAlt(_localctx, 1); {
                    setState(389);
                    match(WITH);
                    setState(390);
                    withClause(0);
                }
                    break;
                case WITHOUT:
                    enterOuterAlt(_localctx, 2); {
                    setState(391);
                    match(WITHOUT);
                    setState(392);
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
        int _startState = 44;
        enterRecursionRule(_localctx, 44, RULE_withClause, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(410);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case LPAREN: {
                        _localctx = new WithParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(396);
                        match(LPAREN);
                        setState(397);
                        withClause(0);
                        setState(398);
                        match(RPAREN);
                    }
                        break;
                    case NOT: {
                        _localctx = new WithNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(400);
                        match(NOT);
                        setState(401);
                        withClause(4);
                    }
                        break;
                    case TAG:
                    case HASHTAG: {
                        _localctx = new WithTagContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(407);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case TAG: {
                                setState(402);
                                match(TAG);
                                setState(404);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                if (_la == HASHTAG) {
                                    {
                                        setState(403);
                                        match(HASHTAG);
                                    }
                                }

                            }
                                break;
                            case HASHTAG: {
                                setState(406);
                                match(HASHTAG);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(409);
                        tagMatcher();
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(420);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 64, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(418);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 63, _ctx)) {
                                case 1: {
                                    _localctx = new WithConjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(412);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(413);
                                    match(AND);
                                    setState(414);
                                    withClause(4);
                                }
                                    break;
                                case 2: {
                                    _localctx = new WithDisjunctionContext(
                                        new WithClauseContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_withClause);
                                    setState(415);
                                    if (!(precpred(_ctx, 2)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                                    setState(416);
                                    match(OR);
                                    setState(417);
                                    withClause(3);
                                }
                                    break;
                            }
                        }
                    }
                    setState(422);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 64, _ctx);
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
        enterRule(_localctx, 46, RULE_tagMatcher);
        try {
            int _alt;
            setState(441);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 67, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1); {
                    setState(423);
                    identifier();
                    setState(424);
                    match(COLON);
                    setState(425);
                    identifier();
                    setState(430);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 65, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(426);
                                    match(SLASH);
                                    setState(427);
                                    identifier();
                                }
                            }
                        }
                        setState(432);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 65, _ctx);
                    }
                }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2); {
                    setState(433);
                    identifier();
                    setState(438);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 66, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(434);
                                    match(SLASH);
                                    setState(435);
                                    identifier();
                                }
                            }
                        }
                        setState(440);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 66, _ctx);
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
        enterRule(_localctx, 48, RULE_sidequalifier);
        int _la;
        try {
            setState(455);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case EACH:
                    _localctx = new EachSideContext(_localctx);
                    enterOuterAlt(_localctx, 1); {
                    setState(443);
                    match(EACH);
                    setState(444);
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
                    setState(445);
                    side();
                    setState(450);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(446);
                                match(COMMA);
                                setState(447);
                                side();
                            }
                        }
                        setState(452);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(453);
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
        enterRule(_localctx, 50, RULE_side);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(457);
                _la = _input.LA(1);
                if (!(((((_la - 56)) & ~0x3f) == 0 && ((1L << (_la - 56)) & 4031L) != 0))) {
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
        enterRule(_localctx, 52, RULE_slotqualifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(459);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(460);
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
        enterRule(_localctx, 54, RULE_rangeset);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(462);
                range();
                setState(467);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(463);
                            match(COMMA);
                            setState(464);
                            range();
                        }
                    }
                    setState(469);
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
        enterRule(_localctx, 56, RULE_range);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(470);
                number();
                setState(473);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == DASH) {
                    {
                        setState(471);
                        match(DASH);
                        setState(472);
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
        enterRule(_localctx, 58, RULE_ifStatement);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(475);
                match(IF);
                setState(476);
                boolexpr(0);
                setState(477);
                match(THEN);
                setState(478);
                block();
                setState(487);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 72, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(479);
                                match(ELSE);
                                setState(480);
                                match(IF);
                                setState(481);
                                boolexpr(0);
                                setState(482);
                                match(THEN);
                                setState(483);
                                block();
                            }
                        }
                    }
                    setState(489);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 72, _ctx);
                }
                setState(492);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == ELSE) {
                    {
                        setState(490);
                        match(ELSE);
                        setState(491);
                        block();
                    }
                }

                setState(494);
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
        int _startState = 60;
        enterRecursionRule(_localctx, 60, RULE_boolexpr, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(546);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 82, _ctx)) {
                    case 1: {
                        _localctx = new BooleanTrueContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(497);
                        match(TRUE);
                    }
                        break;
                    case 2: {
                        _localctx = new BooleanFalseContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(498);
                        match(FALSE);
                    }
                        break;
                    case 3: {
                        _localctx = new BooleanParenContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(499);
                        match(LPAREN);
                        setState(500);
                        boolexpr(0);
                        setState(501);
                        match(RPAREN);
                    }
                        break;
                    case 4: {
                        _localctx = new BooleanDataContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(503);
                        match(LPAREN);
                        setState(504);
                        dataExpression();
                        setState(505);
                        match(RPAREN);
                    }
                        break;
                    case 5: {
                        _localctx = new BooleanNegationContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(507);
                        match(NOT);
                        setState(508);
                        boolexpr(5);
                    }
                        break;
                    case 6: {
                        _localctx = new BooleanHasContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(510);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4398047002624L) != 0) || _la == EVERY) {
                            {
                                setState(509);
                                setOp();
                            }
                        }

                        setState(512);
                        labelAccess();
                        setState(513);
                        match(HAS);
                        setState(514);
                        comparisonOp();
                        setState(520);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case NUMBER: {
                                setState(515);
                                number();
                            }
                                break;
                            case LPAREN: {
                                setState(516);
                                match(LPAREN);
                                setState(517);
                                dataExpression();
                                setState(518);
                                match(RPAREN);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                        setState(523);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 76, _ctx)) {
                            case 1: {
                                setState(522);
                                resourceIdDisjunction();
                            }
                                break;
                        }
                        setState(526);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 77, _ctx)) {
                            case 1: {
                                setState(525);
                                with();
                            }
                                break;
                        }
                        setState(530);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 78, _ctx)) {
                            case 1: {
                                setState(528);
                                match(EXCEPT);
                                setState(529);
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
                        setState(532);
                        match(REDSTONE);
                        setState(534);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 79, _ctx)) {
                            case 1: {
                                setState(533);
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
                        setState(544);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 81, _ctx)) {
                            case 1: {
                                setState(536);
                                comparisonOp();
                                setState(542);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case NUMBER: {
                                        setState(537);
                                        number();
                                    }
                                        break;
                                    case LPAREN: {
                                        setState(538);
                                        match(LPAREN);
                                        setState(539);
                                        dataExpression();
                                        setState(540);
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
                setState(556);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 84, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(554);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 83, _ctx)) {
                                case 1: {
                                    _localctx = new BooleanConjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(548);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(549);
                                    match(AND);
                                    setState(550);
                                    boolexpr(5);
                                }
                                    break;
                                case 2: {
                                    _localctx = new BooleanDisjunctionContext(
                                        new BoolexprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
                                    setState(551);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(552);
                                    match(OR);
                                    setState(553);
                                    boolexpr(4);
                                }
                                    break;
                            }
                        }
                    }
                    setState(558);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 84, _ctx);
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
        enterRule(_localctx, 62, RULE_comparisonOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(559);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 17163091968L) != 0))) {
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
        enterRule(_localctx, 64, RULE_setOp);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(561);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 4398047002624L) != 0) || _la == EVERY)) {
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
        enterRule(_localctx, 66, RULE_labelAccess);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(563);
                label();
                setState(568);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(564);
                            match(COMMA);
                            setState(565);
                            label();
                        }
                    }
                    setState(570);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(572);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == ROUND) {
                    {
                        setState(571);
                        roundrobin();
                    }
                }

                setState(575);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (((((_la - 42)) & ~0x3f) == 0 && ((1L << (_la - 42)) & 66043905L) != 0)) {
                    {
                        setState(574);
                        sidequalifier();
                    }
                }

                setState(578);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SLOTS || _la == SLOT) {
                    {
                        setState(577);
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
        enterRule(_localctx, 68, RULE_roundrobin);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(580);
                match(ROUND);
                setState(581);
                match(ROBIN);
                setState(582);
                match(BY);
                setState(583);
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
        enterRule(_localctx, 70, RULE_label);
        try {
            setState(587);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case LET:
                case SET:
                case STATE:
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
                        setState(585);
                        identifier();
                    }
                }
                    break;
                case STRING:
                    _localctx = new StringLabelContext(_localctx);
                    enterOuterAlt(_localctx, 2); {
                    setState(586);
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
        enterRule(_localctx, 72, RULE_emptyslots);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(589);
                match(EMPTY);
                setState(590);
                _la = _input.LA(1);
                if (!(_la == SLOTS || _la == SLOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(591);
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

        public TerminalNode LET() {
            return getToken(SFMLParser.LET, 0);
        }

        public TerminalNode SET() {
            return getToken(SFMLParser.SET, 0);
        }

        public TerminalNode STATE() {
            return getToken(SFMLParser.STATE, 0);
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
        enterRule(_localctx, 74, RULE_identifier);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(593);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & -9007199254740991986L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 67116487L) != 0))) {
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
        enterRule(_localctx, 76, RULE_stateDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(595);
                match(STATE);
                setState(596);
                match(VARIABLE);
                setState(597);
                match(EQ_SYMBOL);
                setState(598);
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
        enterRule(_localctx, 78, RULE_dataDeclaration);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(600);
                match(LET);
                setState(601);
                match(VARIABLE);
                setState(602);
                match(EQ_SYMBOL);
                setState(603);
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

        public TerminalNode SET() {
            return getToken(SFMLParser.SET, 0);
        }

        public DataTargetContext dataTarget() {
            return getRuleContext(DataTargetContext.class, 0);
        }

        public TerminalNode EQ_SYMBOL() {
            return getToken(SFMLParser.EQ_SYMBOL, 0);
        }

        public DataExpressionContext dataExpression() {
            return getRuleContext(DataExpressionContext.class, 0);
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
        enterRule(_localctx, 80, RULE_dataAssignment);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(605);
                match(SET);
                setState(606);
                dataTarget();
                setState(607);
                match(EQ_SYMBOL);
                setState(608);
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
        enterRule(_localctx, 82, RULE_dataTarget);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(610);
                match(VARIABLE);
                setState(619);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACKET || _la == DOT) {
                    {
                        setState(617);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case DOT: {
                                setState(611);
                                match(DOT);
                                setState(612);
                                dataName();
                            }
                                break;
                            case LBRACKET: {
                                setState(613);
                                match(LBRACKET);
                                setState(614);
                                dataExpression();
                                setState(615);
                                match(RBRACKET);
                            }
                                break;
                            default:
                                throw new NoViableAltException(this);
                        }
                    }
                    setState(621);
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
        enterRule(_localctx, 84, RULE_dataExpression);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(622);
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
        enterRule(_localctx, 86, RULE_dataOr);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(624);
                dataAnd();
                setState(629);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == OR) {
                    {
                        {
                            setState(625);
                            match(OR);
                            setState(626);
                            dataAnd();
                        }
                    }
                    setState(631);
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
        enterRule(_localctx, 88, RULE_dataAnd);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(632);
                dataNot();
                setState(637);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == AND) {
                    {
                        {
                            setState(633);
                            match(AND);
                            setState(634);
                            dataNot();
                        }
                    }
                    setState(639);
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
        enterRule(_localctx, 90, RULE_dataNot);
        try {
            setState(643);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case NOT:
                    enterOuterAlt(_localctx, 1); {
                    setState(640);
                    match(NOT);
                    setState(641);
                    dataNot();
                }
                    break;
                case VARIABLE:
                case LBRACKET:
                case LBRACE:
                case TRUE:
                case FALSE:
                case PLUS:
                case DASH:
                case LPAREN:
                case NUMBER:
                case STRING:
                    enterOuterAlt(_localctx, 2); {
                    setState(642);
                    dataComparison();
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
    public static class DataComparisonContext extends ParserRuleContext {

        public List<DataAddContext> dataAdd() {
            return getRuleContexts(DataAddContext.class);
        }

        public DataAddContext dataAdd(int i) {
            return getRuleContext(DataAddContext.class, i);
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
        enterRule(_localctx, 92, RULE_dataComparison);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(645);
                dataAdd();
                setState(649);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 17163091968L) != 0)) {
                    {
                        setState(646);
                        comparisonOp();
                        setState(647);
                        dataAdd();
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
        enterRule(_localctx, 94, RULE_dataAdd);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(651);
                dataMultiply();
                setState(656);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == PLUS || _la == DASH) {
                    {
                        {
                            setState(652);
                            _la = _input.LA(1);
                            if (!(_la == PLUS || _la == DASH)) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                            setState(653);
                            dataMultiply();
                        }
                    }
                    setState(658);
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
        enterRule(_localctx, 96, RULE_dataMultiply);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(659);
                dataSign();
                setState(668);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 98, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(663);
                                _errHandler.sync(this);
                                switch (getInterpreter().adaptivePredict(_input, 97, _ctx)) {
                                    case 1: {
                                        setState(660);
                                        multiplyOperator();
                                    }
                                        break;
                                    case 2: {
                                        setState(661);
                                        match(SLASH);
                                    }
                                        break;
                                    case 3: {
                                        setState(662);
                                        match(PERCENT);
                                    }
                                        break;
                                }
                                setState(665);
                                dataSign();
                            }
                        }
                    }
                    setState(670);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 98, _ctx);
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
        enterRule(_localctx, 98, RULE_multiplyOperator);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(671);
                if (!(_input.LT(1)
                    .getText()
                    .equals("*"))) throw new FailedPredicateException(this, "_input.LT(1).getText().equals(\"*\")");
                setState(672);
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
        enterRule(_localctx, 100, RULE_dataSign);
        int _la;
        try {
            setState(677);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case PLUS:
                case DASH:
                    enterOuterAlt(_localctx, 1); {
                    setState(674);
                    _la = _input.LA(1);
                    if (!(_la == PLUS || _la == DASH)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(675);
                    dataSign();
                }
                    break;
                case VARIABLE:
                case LBRACKET:
                case LBRACE:
                case TRUE:
                case FALSE:
                case LPAREN:
                case NUMBER:
                case STRING:
                    enterOuterAlt(_localctx, 2); {
                    setState(676);
                    dataPostfix();
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
        enterRule(_localctx, 102, RULE_dataPostfix);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(679);
                dataPrimary();
                setState(688);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 101, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            setState(686);
                            _errHandler.sync(this);
                            switch (_input.LA(1)) {
                                case DOT: {
                                    setState(680);
                                    match(DOT);
                                    setState(681);
                                    dataName();
                                }
                                    break;
                                case LBRACKET: {
                                    setState(682);
                                    match(LBRACKET);
                                    setState(683);
                                    dataExpression();
                                    setState(684);
                                    match(RBRACKET);
                                }
                                    break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                        }
                    }
                    setState(690);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 101, _ctx);
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
        enterRule(_localctx, 104, RULE_dataPrimary);
        int _la;
        try {
            int _alt;
            setState(730);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case NUMBER:
                    enterOuterAlt(_localctx, 1); {
                    setState(691);
                    match(NUMBER);
                }
                    break;
                case STRING:
                    enterOuterAlt(_localctx, 2); {
                    setState(692);
                    match(STRING);
                }
                    break;
                case VARIABLE:
                    enterOuterAlt(_localctx, 3); {
                    setState(693);
                    match(VARIABLE);
                }
                    break;
                case TRUE:
                    enterOuterAlt(_localctx, 4); {
                    setState(694);
                    match(TRUE);
                }
                    break;
                case FALSE:
                    enterOuterAlt(_localctx, 5); {
                    setState(695);
                    match(FALSE);
                }
                    break;
                case LPAREN:
                    enterOuterAlt(_localctx, 6); {
                    setState(696);
                    match(LPAREN);
                    setState(697);
                    dataExpression();
                    setState(698);
                    match(RPAREN);
                }
                    break;
                case LBRACKET:
                    enterOuterAlt(_localctx, 7); {
                    setState(700);
                    match(LBRACKET);
                    setState(712);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3670192L) != 0)
                        || ((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 339969L) != 0)) {
                        {
                            setState(701);
                            dataExpression();
                            setState(706);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 102, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(702);
                                            match(COMMA);
                                            setState(703);
                                            dataExpression();
                                        }
                                    }
                                }
                                setState(708);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 102, _ctx);
                            }
                            setState(710);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(709);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(714);
                    match(RBRACKET);
                }
                    break;
                case LBRACE:
                    enterOuterAlt(_localctx, 8); {
                    setState(715);
                    match(LBRACE);
                    setState(727);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -1125911348905970L) != 0)
                        || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 67371007L) != 0)) {
                        {
                            setState(716);
                            dataField();
                            setState(721);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 105, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(717);
                                            match(COMMA);
                                            setState(718);
                                            dataField();
                                        }
                                    }
                                }
                                setState(723);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 105, _ctx);
                            }
                            setState(725);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(724);
                                    match(COMMA);
                                }
                            }

                        }
                    }

                    setState(729);
                    match(RBRACE);
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
        enterRule(_localctx, 106, RULE_dataField);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(732);
                dataName();
                setState(733);
                match(COLON);
                setState(734);
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

        public TerminalNode LET() {
            return getToken(SFMLParser.LET, 0);
        }

        public TerminalNode SET() {
            return getToken(SFMLParser.SET, 0);
        }

        public TerminalNode STATE() {
            return getToken(SFMLParser.STATE, 0);
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
        enterRule(_localctx, 108, RULE_dataName);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(736);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & -1125911348905970L) != 0)
                    || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 67371007L) != 0))) {
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
        enterRule(_localctx, 110, RULE_string);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(738);
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
        enterRule(_localctx, 112, RULE_number);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(740);
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
            case 22:
                return withClause_sempred((WithClauseContext) _localctx, predIndex);
            case 30:
                return boolexpr_sempred((BoolexprContext) _localctx, predIndex);
            case 49:
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

    public static final String _serializedATN = "\u0004\u0001^\u02e7\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"
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
        + "7\u00077\u00028\u00078\u0001\u0000\u0003\u0000t\b\u0000\u0001\u0000\u0005"
        + "\u0000w\b\u0000\n\u0000\f\u0000z\t\u0000\u0001\u0000\u0005\u0000}\b\u0000"
        + "\n\u0000\f\u0000\u0080\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001"
        + "\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"
        + "\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"
        + "\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u0094\b\u0002\u0001\u0003\u0003"
        + "\u0003\u0097\b\u0003\u0001\u0003\u0003\u0003\u009a\b\u0003\u0001\u0003"
        + "\u0001\u0003\u0003\u0003\u009e\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003"
        + "\u0001\u0003\u0003\u0003\u00a4\b\u0003\u0001\u0003\u0003\u0003\u00a7\b"
        + "\u0003\u0001\u0004\u0005\u0004\u00aa\b\u0004\n\u0004\f\u0004\u00ad\t\u0004"
        + "\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"
        + "\u0001\u0005\u0003\u0005\u00b6\b\u0005\u0001\u0006\u0001\u0006\u0003\u0006"
        + "\u00ba\b\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u00bf\b"
        + "\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u00c3\b\u0006\u0001\u0006\u0001"
        + "\u0006\u0001\u0006\u0003\u0006\u00c8\b\u0006\u0001\u0006\u0001\u0006\u0001"
        + "\u0006\u0003\u0006\u00cd\b\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003"
        + "\u0006\u00d2\b\u0006\u0003\u0006\u00d4\b\u0006\u0001\u0007\u0001\u0007"
        + "\u0003\u0007\u00d8\b\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u00dc\b"
        + "\u0007\n\u0007\f\u0007\u00df\t\u0007\u0001\u0007\u0003\u0007\u00e2\b\u0007"
        + "\u0001\b\u0001\b\u0003\b\u00e6\b\b\u0001\b\u0003\b\u00e9\b\b\u0001\b\u0001"
        + "\b\u0003\b\u00ed\b\b\u0001\b\u0001\b\u0001\b\u0003\b\u00f2\b\b\u0001\b"
        + "\u0001\b\u0001\b\u0003\b\u00f7\b\b\u0001\b\u0003\b\u00fa\b\b\u0003\b\u00fc"
        + "\b\b\u0001\t\u0001\t\u0003\t\u0100\b\t\u0001\t\u0003\t\u0103\b\t\u0001"
        + "\t\u0001\t\u0003\t\u0107\b\t\u0001\t\u0003\t\u010a\b\t\u0001\t\u0001\t"
        + "\u0001\t\u0003\t\u010f\b\t\u0001\t\u0003\t\u0112\b\t\u0001\t\u0001\t\u0001"
        + "\t\u0003\t\u0117\b\t\u0001\t\u0003\t\u011a\b\t\u0003\t\u011c\b\t\u0001"
        + "\n\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005\f\u0125"
        + "\b\f\n\f\f\f\u0128\t\f\u0001\f\u0003\f\u012b\b\f\u0001\r\u0003\r\u012e"
        + "\b\r\u0001\r\u0001\r\u0003\r\u0132\b\r\u0001\r\u0001\r\u0003\r\u0136\b"
        + "\r\u0001\r\u0003\r\u0139\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001"
        + "\u000e\u0001\u000e\u0003\u000e\u0140\b\u000e\u0001\u000f\u0001\u000f\u0001"
        + "\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0147\b\u000f\u0001\u000f\u0003"
        + "\u000f\u014a\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001"
        + "\u0010\u0001\u0010\u0003\u0010\u0152\b\u0010\u0001\u0010\u0003\u0010\u0155"
        + "\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001"
        + "\u0012\u0003\u0012\u015d\b\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0161"
        + "\b\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0165\b\u0012\u0003\u0012"
        + "\u0167\b\u0012\u0003\u0012\u0169\b\u0012\u0003\u0012\u016b\b\u0012\u0001"
        + "\u0012\u0003\u0012\u016e\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0005"
        + "\u0013\u0173\b\u0013\n\u0013\f\u0013\u0176\t\u0013\u0001\u0013\u0003\u0013"
        + "\u0179\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u017e\b"
        + "\u0014\n\u0014\f\u0014\u0181\t\u0014\u0001\u0014\u0003\u0014\u0184\b\u0014"
        + "\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u018a\b\u0015"
        + "\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"
        + "\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0195\b\u0016\u0001\u0016"
        + "\u0003\u0016\u0198\b\u0016\u0001\u0016\u0003\u0016\u019b\b\u0016\u0001"
        + "\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0005"
        + "\u0016\u01a3\b\u0016\n\u0016\f\u0016\u01a6\t\u0016\u0001\u0017\u0001\u0017"
        + "\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017\u01ad\b\u0017\n\u0017"
        + "\f\u0017\u01b0\t\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017"
        + "\u01b5\b\u0017\n\u0017\f\u0017\u01b8\t\u0017\u0003\u0017\u01ba\b\u0017"
        + "\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018"
        + "\u01c1\b\u0018\n\u0018\f\u0018\u01c4\t\u0018\u0001\u0018\u0001\u0018\u0003"
        + "\u0018\u01c8\b\u0018\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0001"
        + "\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0005\u001b\u01d2\b\u001b\n"
        + "\u001b\f\u001b\u01d5\t\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0003"
        + "\u001c\u01da\b\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"
        + "\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005"
        + "\u001d\u01e6\b\u001d\n\u001d\f\u001d\u01e9\t\u001d\u0001\u001d\u0001\u001d"
        + "\u0003\u001d\u01ed\b\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e"
        + "\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"
        + "\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"
        + "\u0003\u001e\u01ff\b\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"
        + "\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0209\b\u001e"
        + "\u0001\u001e\u0003\u001e\u020c\b\u001e\u0001\u001e\u0003\u001e\u020f\b"
        + "\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0213\b\u001e\u0001\u001e\u0001"
        + "\u001e\u0003\u001e\u0217\b\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"
        + "\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u021f\b\u001e\u0003\u001e\u0221"
        + "\b\u001e\u0003\u001e\u0223\b\u001e\u0001\u001e\u0001\u001e\u0001\u001e"
        + "\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u022b\b\u001e\n\u001e"
        + "\f\u001e\u022e\t\u001e\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001!\u0001"
        + "!\u0001!\u0005!\u0237\b!\n!\f!\u023a\t!\u0001!\u0003!\u023d\b!\u0001!"
        + "\u0003!\u0240\b!\u0001!\u0003!\u0243\b!\u0001\"\u0001\"\u0001\"\u0001"
        + "\"\u0001\"\u0001#\u0001#\u0003#\u024c\b#\u0001$\u0001$\u0001$\u0001$\u0001"
        + "%\u0001%\u0001&\u0001&\u0001&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0001"
        + "\'\u0001\'\u0001(\u0001(\u0001(\u0001(\u0001(\u0001)\u0001)\u0001)\u0001"
        + ")\u0001)\u0001)\u0001)\u0005)\u026a\b)\n)\f)\u026d\t)\u0001*\u0001*\u0001"
        + "+\u0001+\u0001+\u0005+\u0274\b+\n+\f+\u0277\t+\u0001,\u0001,\u0001,\u0005"
        + ",\u027c\b,\n,\f,\u027f\t,\u0001-\u0001-\u0001-\u0003-\u0284\b-\u0001."
        + "\u0001.\u0001.\u0001.\u0003.\u028a\b.\u0001/\u0001/\u0001/\u0005/\u028f"
        + "\b/\n/\f/\u0292\t/\u00010\u00010\u00010\u00010\u00030\u0298\b0\u00010"
        + "\u00050\u029b\b0\n0\f0\u029e\t0\u00011\u00011\u00011\u00012\u00012\u0001"
        + "2\u00032\u02a6\b2\u00013\u00013\u00013\u00013\u00013\u00013\u00013\u0005"
        + "3\u02af\b3\n3\f3\u02b2\t3\u00014\u00014\u00014\u00014\u00014\u00014\u0001"
        + "4\u00014\u00014\u00014\u00014\u00014\u00014\u00054\u02c1\b4\n4\f4\u02c4"
        + "\t4\u00014\u00034\u02c7\b4\u00034\u02c9\b4\u00014\u00014\u00014\u0001"
        + "4\u00014\u00054\u02d0\b4\n4\f4\u02d3\t4\u00014\u00034\u02d6\b4\u00034"
        + "\u02d8\b4\u00014\u00034\u02db\b4\u00015\u00015\u00015\u00015\u00016\u0001"
        + "6\u00017\u00017\u00018\u00018\u00018\u0000\u0002,<9\u0000\u0002\u0004"
        + "\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \""
        + "$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnp\u0000\n\u0001\u0000DG\u0001\u0000"
        + "KL\u0002\u00008=?C\u0001\u0000\'(\u0001\u0000\u0018!\u0003\u0000\u000f"
        + "\u0012**QQ\u0001\u000067\u0006\u0000\u0001\u000389?BFHJLZZ\u0002\u0000"
        + "IIUU\t\u0000\u0001\u0003\u000b\u0018\u001a\u001a\u001c\u001c\u001e\u001e"
        + "  \"13QZZ\u032e\u0000s\u0001\u0000\u0000\u0000\u0002\u0083\u0001\u0000"
        + "\u0000\u0000\u0004\u0093\u0001\u0000\u0000\u0000\u0006\u00a6\u0001\u0000"
        + "\u0000\u0000\b\u00ab\u0001\u0000\u0000\u0000\n\u00b5\u0001\u0000\u0000"
        + "\u0000\f\u00d3\u0001\u0000\u0000\u0000\u000e\u00d5\u0001\u0000\u0000\u0000"
        + "\u0010\u00fb\u0001\u0000\u0000\u0000\u0012\u011b\u0001\u0000\u0000\u0000"
        + "\u0014\u011d\u0001\u0000\u0000\u0000\u0016\u011f\u0001\u0000\u0000\u0000"
        + "\u0018\u0121\u0001\u0000\u0000\u0000\u001a\u0138\u0001\u0000\u0000\u0000"
        + "\u001c\u013f\u0001\u0000\u0000\u0000\u001e\u0146\u0001\u0000\u0000\u0000"
        + " \u014b\u0001\u0000\u0000\u0000\"\u0156\u0001\u0000\u0000\u0000$\u016d"
        + "\u0001\u0000\u0000\u0000&\u016f\u0001\u0000\u0000\u0000(\u017a\u0001\u0000"
        + "\u0000\u0000*\u0189\u0001\u0000\u0000\u0000,\u019a\u0001\u0000\u0000\u0000"
        + ".\u01b9\u0001\u0000\u0000\u00000\u01c7\u0001\u0000\u0000\u00002\u01c9"
        + "\u0001\u0000\u0000\u00004\u01cb\u0001\u0000\u0000\u00006\u01ce\u0001\u0000"
        + "\u0000\u00008\u01d6\u0001\u0000\u0000\u0000:\u01db\u0001\u0000\u0000\u0000"
        + "<\u0222\u0001\u0000\u0000\u0000>\u022f\u0001\u0000\u0000\u0000@\u0231"
        + "\u0001\u0000\u0000\u0000B\u0233\u0001\u0000\u0000\u0000D\u0244\u0001\u0000"
        + "\u0000\u0000F\u024b\u0001\u0000\u0000\u0000H\u024d\u0001\u0000\u0000\u0000"
        + "J\u0251\u0001\u0000\u0000\u0000L\u0253\u0001\u0000\u0000\u0000N\u0258"
        + "\u0001\u0000\u0000\u0000P\u025d\u0001\u0000\u0000\u0000R\u0262\u0001\u0000"
        + "\u0000\u0000T\u026e\u0001\u0000\u0000\u0000V\u0270\u0001\u0000\u0000\u0000"
        + "X\u0278\u0001\u0000\u0000\u0000Z\u0283\u0001\u0000\u0000\u0000\\\u0285"
        + "\u0001\u0000\u0000\u0000^\u028b\u0001\u0000\u0000\u0000`\u0293\u0001\u0000"
        + "\u0000\u0000b\u029f\u0001\u0000\u0000\u0000d\u02a5\u0001\u0000\u0000\u0000"
        + "f\u02a7\u0001\u0000\u0000\u0000h\u02da\u0001\u0000\u0000\u0000j\u02dc"
        + "\u0001\u0000\u0000\u0000l\u02e0\u0001\u0000\u0000\u0000n\u02e2\u0001\u0000"
        + "\u0000\u0000p\u02e4\u0001\u0000\u0000\u0000rt\u0003\u0002\u0001\u0000"
        + "sr\u0001\u0000\u0000\u0000st\u0001\u0000\u0000\u0000tx\u0001\u0000\u0000"
        + "\u0000uw\u0003L&\u0000vu\u0001\u0000\u0000\u0000wz\u0001\u0000\u0000\u0000"
        + "xv\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000y~\u0001\u0000\u0000"
        + "\u0000zx\u0001\u0000\u0000\u0000{}\u0003\u0004\u0002\u0000|{\u0001\u0000"
        + "\u0000\u0000}\u0080\u0001\u0000\u0000\u0000~|\u0001\u0000\u0000\u0000"
        + "~\u007f\u0001\u0000\u0000\u0000\u007f\u0081\u0001\u0000\u0000\u0000\u0080"
        + "~\u0001\u0000\u0000\u0000\u0081\u0082\u0005\u0000\u0000\u0001\u0082\u0001"
        + "\u0001\u0000\u0000\u0000\u0083\u0084\u0005P\u0000\u0000\u0084\u0085\u0003"
        + "n7\u0000\u0085\u0003\u0001\u0000\u0000\u0000\u0086\u0087\u0005Q\u0000"
        + "\u0000\u0087\u0088\u0003\u0006\u0003\u0000\u0088\u0089\u0005N\u0000\u0000"
        + "\u0089\u008a\u0003\b\u0004\u0000\u008a\u008b\u0005O\u0000\u0000\u008b"
        + "\u0094\u0001\u0000\u0000\u0000\u008c\u008d\u0005Q\u0000\u0000\u008d\u008e"
        + "\u0005J\u0000\u0000\u008e\u008f\u0005M\u0000\u0000\u008f\u0090\u0005N"
        + "\u0000\u0000\u0090\u0091\u0003\b\u0004\u0000\u0091\u0092\u0005O\u0000"
        + "\u0000\u0092\u0094\u0001\u0000\u0000\u0000\u0093\u0086\u0001\u0000\u0000"
        + "\u0000\u0093\u008c\u0001\u0000\u0000\u0000\u0094\u0005\u0001\u0000\u0000"
        + "\u0000\u0095\u0097\u0005Y\u0000\u0000\u0096\u0095\u0001\u0000\u0000\u0000"
        + "\u0096\u0097\u0001\u0000\u0000\u0000\u0097\u0099\u0001\u0000\u0000\u0000"
        + "\u0098\u009a\u0005H\u0000\u0000\u0099\u0098\u0001\u0000\u0000\u0000\u0099"
        + "\u009a\u0001\u0000\u0000\u0000\u009a\u009d\u0001\u0000\u0000\u0000\u009b"
        + "\u009c\u0005I\u0000\u0000\u009c\u009e\u0005Y\u0000\u0000\u009d\u009b\u0001"
        + "\u0000\u0000\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e\u009f\u0001"
        + "\u0000\u0000\u0000\u009f\u00a7\u0007\u0000\u0000\u0000\u00a0\u00a3\u0005"
        + "X\u0000\u0000\u00a1\u00a2\u0005I\u0000\u0000\u00a2\u00a4\u0005Y\u0000"
        + "\u0000\u00a3\u00a1\u0001\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000"
        + "\u0000\u00a4\u00a5\u0001\u0000\u0000\u0000\u00a5\u00a7\u0007\u0000\u0000"
        + "\u0000\u00a6\u0096\u0001\u0000\u0000\u0000\u00a6\u00a0\u0001\u0000\u0000"
        + "\u0000\u00a7\u0007\u0001\u0000\u0000\u0000\u00a8\u00aa\u0003\n\u0005\u0000"
        + "\u00a9\u00a8\u0001\u0000\u0000\u0000\u00aa\u00ad\u0001\u0000\u0000\u0000"
        + "\u00ab\u00a9\u0001\u0000\u0000\u0000\u00ab\u00ac\u0001\u0000\u0000\u0000"
        + "\u00ac\t\u0001\u0000\u0000\u0000\u00ad\u00ab\u0001\u0000\u0000\u0000\u00ae"
        + "\u00b6\u0003N\'\u0000\u00af\u00b6\u0003P(\u0000\u00b0\u00b6\u0003\u0010"
        + "\b\u0000\u00b1\u00b6\u0003\f\u0006\u0000\u00b2\u00b6\u0003\u0012\t\u0000"
        + "\u00b3\u00b6\u0003:\u001d\u0000\u00b4\u00b6\u0003\u000e\u0007\u0000\u00b5"
        + "\u00ae\u0001\u0000\u0000\u0000\u00b5\u00af\u0001\u0000\u0000\u0000\u00b5"
        + "\u00b0\u0001\u0000\u0000\u0000\u00b5\u00b1\u0001\u0000\u0000\u0000\u00b5"
        + "\u00b2\u0001\u0000\u0000\u0000\u00b5\u00b3\u0001\u0000\u0000\u0000\u00b5"
        + "\u00b4\u0001\u0000\u0000\u0000\u00b6\u000b\u0001\u0000\u0000\u0000\u00b7"
        + "\u00b9\u0005%\u0000\u0000\u00b8\u00ba\u0003\u001e\u000f\u0000\u00b9\u00b8"
        + "\u0001\u0000\u0000\u0000\u00b9\u00ba\u0001\u0000\u0000\u0000\u00ba\u00bb"
        + "\u0001\u0000\u0000\u0000\u00bb\u00bc\u0005J\u0000\u0000\u00bc\u00be\u0007"
        + "\u0001\u0000\u0000\u00bd\u00bf\u0003\u001e\u000f\u0000\u00be\u00bd\u0001"
        + "\u0000\u0000\u0000\u00be\u00bf\u0001\u0000\u0000\u0000\u00bf\u00c0\u0001"
        + "\u0000\u0000\u0000\u00c0\u00c2\u0005#\u0000\u0000\u00c1\u00c3\u0005*\u0000"
        + "\u0000\u00c2\u00c1\u0001\u0000\u0000\u0000\u00c2\u00c3\u0001\u0000\u0000"
        + "\u0000\u00c3\u00c4\u0001\u0000\u0000\u0000\u00c4\u00d4\u0003B!\u0000\u00c5"
        + "\u00c7\u0005#\u0000\u0000\u00c6\u00c8\u0005*\u0000\u0000\u00c7\u00c6\u0001"
        + "\u0000\u0000\u0000\u00c7\u00c8\u0001\u0000\u0000\u0000\u00c8\u00c9\u0001"
        + "\u0000\u0000\u0000\u00c9\u00ca\u0003B!\u0000\u00ca\u00cc\u0005%\u0000"
        + "\u0000\u00cb\u00cd\u0003\u001e\u000f\u0000\u00cc\u00cb\u0001\u0000\u0000"
        + "\u0000\u00cc\u00cd\u0001\u0000\u0000\u0000\u00cd\u00ce\u0001\u0000\u0000"
        + "\u0000\u00ce\u00cf\u0005J\u0000\u0000\u00cf\u00d1\u0007\u0001\u0000\u0000"
        + "\u00d0\u00d2\u0003\u001e\u000f\u0000\u00d1\u00d0\u0001\u0000\u0000\u0000"
        + "\u00d1\u00d2\u0001\u0000\u0000\u0000\u00d2\u00d4\u0001\u0000\u0000\u0000"
        + "\u00d3\u00b7\u0001\u0000\u0000\u0000\u00d3\u00c5\u0001\u0000\u0000\u0000"
        + "\u00d4\r\u0001\u0000\u0000\u0000\u00d5\u00d7\u0005,\u0000\u0000\u00d6"
        + "\u00d8\u0003F#\u0000\u00d7\u00d6\u0001\u0000\u0000\u0000\u00d7\u00d8\u0001"
        + "\u0000\u0000\u0000\u00d8\u00dd\u0001\u0000\u0000\u0000\u00d9\u00da\u0005"
        + "R\u0000\u0000\u00da\u00dc\u0003F#\u0000\u00db\u00d9\u0001\u0000\u0000"
        + "\u0000\u00dc\u00df\u0001\u0000\u0000\u0000\u00dd\u00db\u0001\u0000\u0000"
        + "\u0000\u00dd\u00de\u0001\u0000\u0000\u0000\u00de\u00e1\u0001\u0000\u0000"
        + "\u0000\u00df\u00dd\u0001\u0000\u0000\u0000\u00e0\u00e2\u0005R\u0000\u0000"
        + "\u00e1\u00e0\u0001\u0000\u0000\u0000\u00e1\u00e2\u0001\u0000\u0000\u0000"
        + "\u00e2\u000f\u0001\u0000\u0000\u0000\u00e3\u00e5\u0005$\u0000\u0000\u00e4"
        + "\u00e6\u0003\u0014\n\u0000\u00e5\u00e4\u0001\u0000\u0000\u0000\u00e5\u00e6"
        + "\u0001\u0000\u0000\u0000\u00e6\u00e8\u0001\u0000\u0000\u0000\u00e7\u00e9"
        + "\u0003\"\u0011\u0000\u00e8\u00e7\u0001\u0000\u0000\u0000\u00e8\u00e9\u0001"
        + "\u0000\u0000\u0000\u00e9\u00ea\u0001\u0000\u0000\u0000\u00ea\u00ec\u0005"
        + "\"\u0000\u0000\u00eb\u00ed\u0005*\u0000\u0000\u00ec\u00eb\u0001\u0000"
        + "\u0000\u0000\u00ec\u00ed\u0001\u0000\u0000\u0000\u00ed\u00ee\u0001\u0000"
        + "\u0000\u0000\u00ee\u00fc\u0003B!\u0000\u00ef\u00f1\u0005\"\u0000\u0000"
        + "\u00f0\u00f2\u0005*\u0000\u0000\u00f1\u00f0\u0001\u0000\u0000\u0000\u00f1"
        + "\u00f2\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3"
        + "\u00f4\u0003B!\u0000\u00f4\u00f6\u0005$\u0000\u0000\u00f5\u00f7\u0003"
        + "\u0014\n\u0000\u00f6\u00f5\u0001\u0000\u0000\u0000\u00f6\u00f7\u0001\u0000"
        + "\u0000\u0000\u00f7\u00f9\u0001\u0000\u0000\u0000\u00f8\u00fa\u0003\"\u0011"
        + "\u0000\u00f9\u00f8\u0001\u0000\u0000\u0000\u00f9\u00fa\u0001\u0000\u0000"
        + "\u0000\u00fa\u00fc\u0001\u0000\u0000\u0000\u00fb\u00e3\u0001\u0000\u0000"
        + "\u0000\u00fb\u00ef\u0001\u0000\u0000\u0000\u00fc\u0011\u0001\u0000\u0000"
        + "\u0000\u00fd\u00ff\u0005%\u0000\u0000\u00fe\u0100\u0003\u0016\u000b\u0000"
        + "\u00ff\u00fe\u0001\u0000\u0000\u0000\u00ff\u0100\u0001\u0000\u0000\u0000"
        + "\u0100\u0102\u0001\u0000\u0000\u0000\u0101\u0103\u0003\"\u0011\u0000\u0102"
        + "\u0101\u0001\u0000\u0000\u0000\u0102\u0103\u0001\u0000\u0000\u0000\u0103"
        + "\u0104\u0001\u0000\u0000\u0000\u0104\u0106\u0005#\u0000\u0000\u0105\u0107"
        + "\u0003H$\u0000\u0106\u0105\u0001\u0000\u0000\u0000\u0106\u0107\u0001\u0000"
        + "\u0000\u0000\u0107\u0109\u0001\u0000\u0000\u0000\u0108\u010a\u0005*\u0000"
        + "\u0000\u0109\u0108\u0001\u0000\u0000\u0000\u0109\u010a\u0001\u0000\u0000"
        + "\u0000\u010a\u010b\u0001\u0000\u0000\u0000\u010b\u011c\u0003B!\u0000\u010c"
        + "\u010e\u0005#\u0000\u0000\u010d\u010f\u0003H$\u0000\u010e\u010d\u0001"
        + "\u0000\u0000\u0000\u010e\u010f\u0001\u0000\u0000\u0000\u010f\u0111\u0001"
        + "\u0000\u0000\u0000\u0110\u0112\u0005*\u0000\u0000\u0111\u0110\u0001\u0000"
        + "\u0000\u0000\u0111\u0112\u0001\u0000\u0000\u0000\u0112\u0113\u0001\u0000"
        + "\u0000\u0000\u0113\u0114\u0003B!\u0000\u0114\u0116\u0005%\u0000\u0000"
        + "\u0115\u0117\u0003\u0016\u000b\u0000\u0116\u0115\u0001\u0000\u0000\u0000"
        + "\u0116\u0117\u0001\u0000\u0000\u0000\u0117\u0119\u0001\u0000\u0000\u0000"
        + "\u0118\u011a\u0003\"\u0011\u0000\u0119\u0118\u0001\u0000\u0000\u0000\u0119"
        + "\u011a\u0001\u0000\u0000\u0000\u011a\u011c\u0001\u0000\u0000\u0000\u011b"
        + "\u00fd\u0001\u0000\u0000\u0000\u011b\u010c\u0001\u0000\u0000\u0000\u011c"
        + "\u0013\u0001\u0000\u0000\u0000\u011d\u011e\u0003\u0018\f\u0000\u011e\u0015"
        + "\u0001\u0000\u0000\u0000\u011f\u0120\u0003\u0018\f\u0000\u0120\u0017\u0001"
        + "\u0000\u0000\u0000\u0121\u0126\u0003\u001a\r\u0000\u0122\u0123\u0005R"
        + "\u0000\u0000\u0123\u0125\u0003\u001a\r\u0000\u0124\u0122\u0001\u0000\u0000"
        + "\u0000\u0125\u0128\u0001\u0000\u0000\u0000\u0126\u0124\u0001\u0000\u0000"
        + "\u0000\u0126\u0127\u0001\u0000\u0000\u0000\u0127\u012a\u0001\u0000\u0000"
        + "\u0000\u0128\u0126\u0001\u0000\u0000\u0000\u0129\u012b\u0005R\u0000\u0000"
        + "\u012a\u0129\u0001\u0000\u0000\u0000\u012a\u012b\u0001\u0000\u0000\u0000"
        + "\u012b\u0019\u0001\u0000\u0000\u0000\u012c\u012e\u0003\u001c\u000e\u0000"
        + "\u012d\u012c\u0001\u0000\u0000\u0000\u012d\u012e\u0001\u0000\u0000\u0000"
        + "\u012e\u012f\u0001\u0000\u0000\u0000\u012f\u0131\u0003(\u0014\u0000\u0130"
        + "\u0132\u0003*\u0015\u0000\u0131\u0130\u0001\u0000\u0000\u0000\u0131\u0132"
        + "\u0001\u0000\u0000\u0000\u0132\u0139\u0001\u0000\u0000\u0000\u0133\u0135"
        + "\u0003\u001c\u000e\u0000\u0134\u0136\u0003*\u0015\u0000\u0135\u0134\u0001"
        + "\u0000\u0000\u0000\u0135\u0136\u0001\u0000\u0000\u0000\u0136\u0139\u0001"
        + "\u0000\u0000\u0000\u0137\u0139\u0003*\u0015\u0000\u0138\u012d\u0001\u0000"
        + "\u0000\u0000\u0138\u0133\u0001\u0000\u0000\u0000\u0138\u0137\u0001\u0000"
        + "\u0000\u0000\u0139\u001b\u0001\u0000\u0000\u0000\u013a\u013b\u0003\u001e"
        + "\u000f\u0000\u013b\u013c\u0003 \u0010\u0000\u013c\u0140\u0001\u0000\u0000"
        + "\u0000\u013d\u0140\u0003 \u0010\u0000\u013e\u0140\u0003\u001e\u000f\u0000"
        + "\u013f\u013a\u0001\u0000\u0000\u0000\u013f\u013d\u0001\u0000\u0000\u0000"
        + "\u013f\u013e\u0001\u0000\u0000\u0000\u0140\u001d\u0001\u0000\u0000\u0000"
        + "\u0141\u0147\u0003p8\u0000\u0142\u0143\u0005V\u0000\u0000\u0143\u0144"
        + "\u0003T*\u0000\u0144\u0145\u0005W\u0000\u0000\u0145\u0147\u0001\u0000"
        + "\u0000\u0000\u0146\u0141\u0001\u0000\u0000\u0000\u0146\u0142\u0001\u0000"
        + "\u0000\u0000\u0147\u0149\u0001\u0000\u0000\u0000\u0148\u014a\u0005*\u0000"
        + "\u0000\u0149\u0148\u0001\u0000\u0000\u0000\u0149\u014a\u0001\u0000\u0000"
        + "\u0000\u014a\u001f\u0001\u0000\u0000\u0000\u014b\u0151\u0005)\u0000\u0000"
        + "\u014c\u0152\u0003p8\u0000\u014d\u014e\u0005V\u0000\u0000\u014e\u014f"
        + "\u0003T*\u0000\u014f\u0150\u0005W\u0000\u0000\u0150\u0152\u0001\u0000"
        + "\u0000\u0000\u0151\u014c\u0001\u0000\u0000\u0000\u0151\u014d\u0001\u0000"
        + "\u0000\u0000\u0152\u0154\u0001\u0000\u0000\u0000\u0153\u0155\u0005*\u0000"
        + "\u0000\u0154\u0153\u0001\u0000\u0000\u0000\u0154\u0155\u0001\u0000\u0000"
        + "\u0000\u0155!\u0001\u0000\u0000\u0000\u0156\u0157\u0005+\u0000\u0000\u0157"
        + "\u0158\u0003&\u0013\u0000\u0158#\u0001\u0000\u0000\u0000\u0159\u016a\u0003"
        + "J%\u0000\u015a\u015c\u0005S\u0000\u0000\u015b\u015d\u0003J%\u0000\u015c"
        + "\u015b\u0001\u0000\u0000\u0000\u015c\u015d\u0001\u0000\u0000\u0000\u015d"
        + "\u0168\u0001\u0000\u0000\u0000\u015e\u0160\u0005S\u0000\u0000\u015f\u0161"
        + "\u0003J%\u0000\u0160\u015f\u0001\u0000\u0000\u0000\u0160\u0161\u0001\u0000"
        + "\u0000\u0000\u0161\u0166\u0001\u0000\u0000\u0000\u0162\u0164\u0005S\u0000"
        + "\u0000\u0163\u0165\u0003J%\u0000\u0164\u0163\u0001\u0000\u0000\u0000\u0164"
        + "\u0165\u0001\u0000\u0000\u0000\u0165\u0167\u0001\u0000\u0000\u0000\u0166"
        + "\u0162\u0001\u0000\u0000\u0000\u0166\u0167\u0001\u0000\u0000\u0000\u0167"
        + "\u0169\u0001\u0000\u0000\u0000\u0168\u015e\u0001\u0000\u0000\u0000\u0168"
        + "\u0169\u0001\u0000\u0000\u0000\u0169\u016b\u0001\u0000\u0000\u0000\u016a"
        + "\u015a\u0001\u0000\u0000\u0000\u016a\u016b\u0001\u0000\u0000\u0000\u016b"
        + "\u016e\u0001\u0000\u0000\u0000\u016c\u016e\u0003n7\u0000\u016d\u0159\u0001"
        + "\u0000\u0000\u0000\u016d\u016c\u0001\u0000\u0000\u0000\u016e%\u0001\u0000"
        + "\u0000\u0000\u016f\u0174\u0003$\u0012\u0000\u0170\u0171\u0005R\u0000\u0000"
        + "\u0171\u0173\u0003$\u0012\u0000\u0172\u0170\u0001\u0000\u0000\u0000\u0173"
        + "\u0176\u0001\u0000\u0000\u0000\u0174\u0172\u0001\u0000\u0000\u0000\u0174"
        + "\u0175\u0001\u0000\u0000\u0000\u0175\u0178\u0001\u0000\u0000\u0000\u0176"
        + "\u0174\u0001\u0000\u0000\u0000\u0177\u0179\u0005R\u0000\u0000\u0178\u0177"
        + "\u0001\u0000\u0000\u0000\u0178\u0179\u0001\u0000\u0000\u0000\u0179\'\u0001"
        + "\u0000\u0000\u0000\u017a\u017f\u0003$\u0012\u0000\u017b\u017c\u0005\u0017"
        + "\u0000\u0000\u017c\u017e\u0003$\u0012\u0000\u017d\u017b\u0001\u0000\u0000"
        + "\u0000\u017e\u0181\u0001\u0000\u0000\u0000\u017f\u017d\u0001\u0000\u0000"
        + "\u0000\u017f\u0180\u0001\u0000\u0000\u0000\u0180\u0183\u0001\u0000\u0000"
        + "\u0000\u0181\u017f\u0001\u0000\u0000\u0000\u0182\u0184\u0005\u0017\u0000"
        + "\u0000\u0183\u0182\u0001\u0000\u0000\u0000\u0183\u0184\u0001\u0000\u0000"
        + "\u0000\u0184)\u0001\u0000\u0000\u0000\u0185\u0186\u00050\u0000\u0000\u0186"
        + "\u018a\u0003,\u0016\u0000\u0187\u0188\u0005/\u0000\u0000\u0188\u018a\u0003"
        + ",\u0016\u0000\u0189\u0185\u0001\u0000\u0000\u0000\u0189\u0187\u0001\u0000"
        + "\u0000\u0000\u018a+\u0001\u0000\u0000\u0000\u018b\u018c\u0006\u0016\uffff"
        + "\uffff\u0000\u018c\u018d\u0005V\u0000\u0000\u018d\u018e\u0003,\u0016\u0000"
        + "\u018e\u018f\u0005W\u0000\u0000\u018f\u019b\u0001\u0000\u0000\u0000\u0190"
        + "\u0191\u0005\u0015\u0000\u0000\u0191\u019b\u0003,\u0016\u0004\u0192\u0194"
        + "\u00051\u0000\u0000\u0193\u0195\u00052\u0000\u0000\u0194\u0193\u0001\u0000"
        + "\u0000\u0000\u0194\u0195\u0001\u0000\u0000\u0000\u0195\u0198\u0001\u0000"
        + "\u0000\u0000\u0196\u0198\u00052\u0000\u0000\u0197\u0192\u0001\u0000\u0000"
        + "\u0000\u0197\u0196\u0001\u0000\u0000\u0000\u0198\u0199\u0001\u0000\u0000"
        + "\u0000\u0199\u019b\u0003.\u0017\u0000\u019a\u018b\u0001\u0000\u0000\u0000"
        + "\u019a\u0190\u0001\u0000\u0000\u0000\u019a\u0197\u0001\u0000\u0000\u0000"
        + "\u019b\u01a4\u0001\u0000\u0000\u0000\u019c\u019d\n\u0003\u0000\u0000\u019d"
        + "\u019e\u0005\u0016\u0000\u0000\u019e\u01a3\u0003,\u0016\u0004\u019f\u01a0"
        + "\n\u0002\u0000\u0000\u01a0\u01a1\u0005\u0017\u0000\u0000\u01a1\u01a3\u0003"
        + ",\u0016\u0003\u01a2\u019c\u0001\u0000\u0000\u0000\u01a2\u019f\u0001\u0000"
        + "\u0000\u0000\u01a3\u01a6\u0001\u0000\u0000\u0000\u01a4\u01a2\u0001\u0000"
        + "\u0000\u0000\u01a4\u01a5\u0001\u0000\u0000\u0000\u01a5-\u0001\u0000\u0000"
        + "\u0000\u01a6\u01a4\u0001\u0000\u0000\u0000\u01a7\u01a8\u0003J%\u0000\u01a8"
        + "\u01a9\u0005S\u0000\u0000\u01a9\u01ae\u0003J%\u0000\u01aa\u01ab\u0005"
        + "T\u0000\u0000\u01ab\u01ad\u0003J%\u0000\u01ac\u01aa\u0001\u0000\u0000"
        + "\u0000\u01ad\u01b0\u0001\u0000\u0000\u0000\u01ae\u01ac\u0001\u0000\u0000"
        + "\u0000\u01ae\u01af\u0001\u0000\u0000\u0000\u01af\u01ba\u0001\u0000\u0000"
        + "\u0000\u01b0\u01ae\u0001\u0000\u0000\u0000\u01b1\u01b6\u0003J%\u0000\u01b2"
        + "\u01b3\u0005T\u0000\u0000\u01b3\u01b5\u0003J%\u0000\u01b4\u01b2\u0001"
        + "\u0000\u0000\u0000\u01b5\u01b8\u0001\u0000\u0000\u0000\u01b6\u01b4\u0001"
        + "\u0000\u0000\u0000\u01b6\u01b7\u0001\u0000\u0000\u0000\u01b7\u01ba\u0001"
        + "\u0000\u0000\u0000\u01b8\u01b6\u0001\u0000\u0000\u0000\u01b9\u01a7\u0001"
        + "\u0000\u0000\u0000\u01b9\u01b1\u0001\u0000\u0000\u0000\u01ba/\u0001\u0000"
        + "\u0000\u0000\u01bb\u01bc\u0005*\u0000\u0000\u01bc\u01c8\u0005>\u0000\u0000"
        + "\u01bd\u01c2\u00032\u0019\u0000\u01be\u01bf\u0005R\u0000\u0000\u01bf\u01c1"
        + "\u00032\u0019\u0000\u01c0\u01be\u0001\u0000\u0000\u0000\u01c1\u01c4\u0001"
        + "\u0000\u0000\u0000\u01c2\u01c0\u0001\u0000\u0000\u0000\u01c2\u01c3\u0001"
        + "\u0000\u0000\u0000\u01c3\u01c5\u0001\u0000\u0000\u0000\u01c4\u01c2\u0001"
        + "\u0000\u0000\u0000\u01c5\u01c6\u0005>\u0000\u0000\u01c6\u01c8\u0001\u0000"
        + "\u0000\u0000\u01c7\u01bb\u0001\u0000\u0000\u0000\u01c7\u01bd\u0001\u0000"
        + "\u0000\u0000\u01c81\u0001\u0000\u0000\u0000\u01c9\u01ca\u0007\u0002\u0000"
        + "\u0000\u01ca3\u0001\u0000\u0000\u0000\u01cb\u01cc\u0007\u0003\u0000\u0000"
        + "\u01cc\u01cd\u00036\u001b\u0000\u01cd5\u0001\u0000\u0000\u0000\u01ce\u01d3"
        + "\u00038\u001c\u0000\u01cf\u01d0\u0005R\u0000\u0000\u01d0\u01d2\u00038"
        + "\u001c\u0000\u01d1\u01cf\u0001\u0000\u0000\u0000\u01d2\u01d5\u0001\u0000"
        + "\u0000\u0000\u01d3\u01d1\u0001\u0000\u0000\u0000\u01d3\u01d4\u0001\u0000"
        + "\u0000\u0000\u01d47\u0001\u0000\u0000\u0000\u01d5\u01d3\u0001\u0000\u0000"
        + "\u0000\u01d6\u01d9\u0003p8\u0000\u01d7\u01d8\u0005U\u0000\u0000\u01d8"
        + "\u01da\u0003p8\u0000\u01d9\u01d7\u0001\u0000\u0000\u0000\u01d9\u01da\u0001"
        + "\u0000\u0000\u0000\u01da9\u0001\u0000\u0000\u0000\u01db\u01dc\u0005\u000b"
        + "\u0000\u0000\u01dc\u01dd\u0003<\u001e\u0000\u01dd\u01de\u0005\f\u0000"
        + "\u0000\u01de\u01e7\u0003\b\u0004\u0000\u01df\u01e0\u0005\r\u0000\u0000"
        + "\u01e0\u01e1\u0005\u000b\u0000\u0000\u01e1\u01e2\u0003<\u001e\u0000\u01e2"
        + "\u01e3\u0005\f\u0000\u0000\u01e3\u01e4\u0003\b\u0004\u0000\u01e4\u01e6"
        + "\u0001\u0000\u0000\u0000\u01e5\u01df\u0001\u0000\u0000\u0000\u01e6\u01e9"
        + "\u0001\u0000\u0000\u0000\u01e7\u01e5\u0001\u0000\u0000\u0000\u01e7\u01e8"
        + "\u0001\u0000\u0000\u0000\u01e8\u01ec\u0001\u0000\u0000\u0000\u01e9\u01e7"
        + "\u0001\u0000\u0000\u0000\u01ea\u01eb\u0005\r\u0000\u0000\u01eb\u01ed\u0003"
        + "\b\u0004\u0000\u01ec\u01ea\u0001\u0000\u0000\u0000\u01ec\u01ed\u0001\u0000"
        + "\u0000\u0000\u01ed\u01ee\u0001\u0000\u0000\u0000\u01ee\u01ef\u0005O\u0000"
        + "\u0000\u01ef;\u0001\u0000\u0000\u0000\u01f0\u01f1\u0006\u001e\uffff\uffff"
        + "\u0000\u01f1\u0223\u0005\u0013\u0000\u0000\u01f2\u0223\u0005\u0014\u0000"
        + "\u0000\u01f3\u01f4\u0005V\u0000\u0000\u01f4\u01f5\u0003<\u001e\u0000\u01f5"
        + "\u01f6\u0005W\u0000\u0000\u01f6\u0223\u0001\u0000\u0000\u0000\u01f7\u01f8"
        + "\u0005V\u0000\u0000\u01f8\u01f9\u0003T*\u0000\u01f9\u01fa\u0005W\u0000"
        + "\u0000\u01fa\u0223\u0001\u0000\u0000\u0000\u01fb\u01fc\u0005\u0015\u0000"
        + "\u0000\u01fc\u0223\u0003<\u001e\u0005\u01fd\u01ff\u0003@ \u0000\u01fe"
        + "\u01fd\u0001\u0000\u0000\u0000\u01fe\u01ff\u0001\u0000\u0000\u0000\u01ff"
        + "\u0200\u0001\u0000\u0000\u0000\u0200\u0201\u0003B!\u0000\u0201\u0202\u0005"
        + "\u000e\u0000\u0000\u0202\u0208\u0003>\u001f\u0000\u0203\u0209\u0003p8"
        + "\u0000\u0204\u0205\u0005V\u0000\u0000\u0205\u0206\u0003T*\u0000\u0206"
        + "\u0207\u0005W\u0000\u0000\u0207\u0209\u0001\u0000\u0000\u0000\u0208\u0203"
        + "\u0001\u0000\u0000\u0000\u0208\u0204\u0001\u0000\u0000\u0000\u0209\u020b"
        + "\u0001\u0000\u0000\u0000\u020a\u020c\u0003(\u0014\u0000\u020b\u020a\u0001"
        + "\u0000\u0000\u0000\u020b\u020c\u0001\u0000\u0000\u0000\u020c\u020e\u0001"
        + "\u0000\u0000\u0000\u020d\u020f\u0003*\u0015\u0000\u020e\u020d\u0001\u0000"
        + "\u0000\u0000\u020e\u020f\u0001\u0000\u0000\u0000\u020f\u0212\u0001\u0000"
        + "\u0000\u0000\u0210\u0211\u0005+\u0000\u0000\u0211\u0213\u0003&\u0013\u0000"
        + "\u0212\u0210\u0001\u0000\u0000\u0000\u0212\u0213\u0001\u0000\u0000\u0000"
        + "\u0213\u0223\u0001\u0000\u0000\u0000\u0214\u0216\u0005J\u0000\u0000\u0215"
        + "\u0217\u0007\u0001\u0000\u0000\u0216\u0215\u0001\u0000\u0000\u0000\u0216"
        + "\u0217\u0001\u0000\u0000\u0000\u0217\u0220\u0001\u0000\u0000\u0000\u0218"
        + "\u021e\u0003>\u001f\u0000\u0219\u021f\u0003p8\u0000\u021a\u021b\u0005"
        + "V\u0000\u0000\u021b\u021c\u0003T*\u0000\u021c\u021d\u0005W\u0000\u0000"
        + "\u021d\u021f\u0001\u0000\u0000\u0000\u021e\u0219\u0001\u0000\u0000\u0000"
        + "\u021e\u021a\u0001\u0000\u0000\u0000\u021f\u0221\u0001\u0000\u0000\u0000"
        + "\u0220\u0218\u0001\u0000\u0000\u0000\u0220\u0221\u0001\u0000\u0000\u0000"
        + "\u0221\u0223\u0001\u0000\u0000\u0000\u0222\u01f0\u0001\u0000\u0000\u0000"
        + "\u0222\u01f2\u0001\u0000\u0000\u0000\u0222\u01f3\u0001\u0000\u0000\u0000"
        + "\u0222\u01f7\u0001\u0000\u0000\u0000\u0222\u01fb\u0001\u0000\u0000\u0000"
        + "\u0222\u01fe\u0001\u0000\u0000\u0000\u0222\u0214\u0001\u0000\u0000\u0000"
        + "\u0223\u022c\u0001\u0000\u0000\u0000\u0224\u0225\n\u0004\u0000\u0000\u0225"
        + "\u0226\u0005\u0016\u0000\u0000\u0226\u022b\u0003<\u001e\u0005\u0227\u0228"
        + "\n\u0003\u0000\u0000\u0228\u0229\u0005\u0017\u0000\u0000\u0229\u022b\u0003"
        + "<\u001e\u0004\u022a\u0224\u0001\u0000\u0000\u0000\u022a\u0227\u0001\u0000"
        + "\u0000\u0000\u022b\u022e\u0001\u0000\u0000\u0000\u022c\u022a\u0001\u0000"
        + "\u0000\u0000\u022c\u022d\u0001\u0000\u0000\u0000\u022d=\u0001\u0000\u0000"
        + "\u0000\u022e\u022c\u0001\u0000\u0000\u0000\u022f\u0230\u0007\u0004\u0000"
        + "\u0000\u0230?\u0001\u0000\u0000\u0000\u0231\u0232\u0007\u0005\u0000\u0000"
        + "\u0232A\u0001\u0000\u0000\u0000\u0233\u0238\u0003F#\u0000\u0234\u0235"
        + "\u0005R\u0000\u0000\u0235\u0237\u0003F#\u0000\u0236\u0234\u0001\u0000"
        + "\u0000\u0000\u0237\u023a\u0001\u0000\u0000\u0000\u0238\u0236\u0001\u0000"
        + "\u0000\u0000\u0238\u0239\u0001\u0000\u0000\u0000\u0239\u023c\u0001\u0000"
        + "\u0000\u0000\u023a\u0238\u0001\u0000\u0000\u0000\u023b\u023d\u0003D\""
        + "\u0000\u023c\u023b\u0001\u0000\u0000\u0000\u023c\u023d\u0001\u0000\u0000"
        + "\u0000\u023d\u023f\u0001\u0000\u0000\u0000\u023e\u0240\u00030\u0018\u0000"
        + "\u023f\u023e\u0001\u0000\u0000\u0000\u023f\u0240\u0001\u0000\u0000\u0000"
        + "\u0240\u0242\u0001\u0000\u0000\u0000\u0241\u0243\u00034\u001a\u0000\u0242"
        + "\u0241\u0001\u0000\u0000\u0000\u0242\u0243\u0001\u0000\u0000\u0000\u0243"
        + "C\u0001\u0000\u0000\u0000\u0244\u0245\u00053\u0000\u0000\u0245\u0246\u0005"
        + "4\u0000\u0000\u0246\u0247\u00055\u0000\u0000\u0247\u0248\u0007\u0006\u0000"
        + "\u0000\u0248E\u0001\u0000\u0000\u0000\u0249\u024c\u0003J%\u0000\u024a"
        + "\u024c\u0003n7\u0000\u024b\u0249\u0001\u0000\u0000\u0000\u024b\u024a\u0001"
        + "\u0000\u0000\u0000\u024cG\u0001\u0000\u0000\u0000\u024d\u024e\u0005-\u0000"
        + "\u0000\u024e\u024f\u0007\u0003\u0000\u0000\u024f\u0250\u0005.\u0000\u0000"
        + "\u0250I\u0001\u0000\u0000\u0000\u0251\u0252\u0007\u0007\u0000\u0000\u0252"
        + "K\u0001\u0000\u0000\u0000\u0253\u0254\u0005\u0003\u0000\u0000\u0254\u0255"
        + "\u0005\u0004\u0000\u0000\u0255\u0256\u0005\u001d\u0000\u0000\u0256\u0257"
        + "\u0003T*\u0000\u0257M\u0001\u0000\u0000\u0000\u0258\u0259\u0005\u0001"
        + "\u0000\u0000\u0259\u025a\u0005\u0004\u0000\u0000\u025a\u025b\u0005\u001d"
        + "\u0000\u0000\u025b\u025c\u0003T*\u0000\u025cO\u0001\u0000\u0000\u0000"
        + "\u025d\u025e\u0005\u0002\u0000\u0000\u025e\u025f\u0003R)\u0000\u025f\u0260"
        + "\u0005\u001d\u0000\u0000\u0260\u0261\u0003T*\u0000\u0261Q\u0001\u0000"
        + "\u0000\u0000\u0262\u026b\u0005\u0004\u0000\u0000\u0263\u0264\u0005\t\u0000"
        + "\u0000\u0264\u026a\u0003l6\u0000\u0265\u0266\u0005\u0005\u0000\u0000\u0266"
        + "\u0267\u0003T*\u0000\u0267\u0268\u0005\u0006\u0000\u0000\u0268\u026a\u0001"
        + "\u0000\u0000\u0000\u0269\u0263\u0001\u0000\u0000\u0000\u0269\u0265\u0001"
        + "\u0000\u0000\u0000\u026a\u026d\u0001\u0000\u0000\u0000\u026b\u0269\u0001"
        + "\u0000\u0000\u0000\u026b\u026c\u0001\u0000\u0000\u0000\u026cS\u0001\u0000"
        + "\u0000\u0000\u026d\u026b\u0001\u0000\u0000\u0000\u026e\u026f\u0003V+\u0000"
        + "\u026fU\u0001\u0000\u0000\u0000\u0270\u0275\u0003X,\u0000\u0271\u0272"
        + "\u0005\u0017\u0000\u0000\u0272\u0274\u0003X,\u0000\u0273\u0271\u0001\u0000"
        + "\u0000\u0000\u0274\u0277\u0001\u0000\u0000\u0000\u0275\u0273\u0001\u0000"
        + "\u0000\u0000\u0275\u0276\u0001\u0000\u0000\u0000\u0276W\u0001\u0000\u0000"
        + "\u0000\u0277\u0275\u0001\u0000\u0000\u0000\u0278\u027d\u0003Z-\u0000\u0279"
        + "\u027a\u0005\u0016\u0000\u0000\u027a\u027c\u0003Z-\u0000\u027b\u0279\u0001"
        + "\u0000\u0000\u0000\u027c\u027f\u0001\u0000\u0000\u0000\u027d\u027b\u0001"
        + "\u0000\u0000\u0000\u027d\u027e\u0001\u0000\u0000\u0000\u027eY\u0001\u0000"
        + "\u0000\u0000\u027f\u027d\u0001\u0000\u0000\u0000\u0280\u0281\u0005\u0015"
        + "\u0000\u0000\u0281\u0284\u0003Z-\u0000\u0282\u0284\u0003\\.\u0000\u0283"
        + "\u0280\u0001\u0000\u0000\u0000\u0283\u0282\u0001\u0000\u0000\u0000\u0284"
        + "[\u0001\u0000\u0000\u0000\u0285\u0289\u0003^/\u0000\u0286\u0287\u0003"
        + ">\u001f\u0000\u0287\u0288\u0003^/\u0000\u0288\u028a\u0001\u0000\u0000"
        + "\u0000\u0289\u0286\u0001\u0000\u0000\u0000\u0289\u028a\u0001\u0000\u0000"
        + "\u0000\u028a]\u0001\u0000\u0000\u0000\u028b\u0290\u0003`0\u0000\u028c"
        + "\u028d\u0007\b\u0000\u0000\u028d\u028f\u0003`0\u0000\u028e\u028c\u0001"
        + "\u0000\u0000\u0000\u028f\u0292\u0001\u0000\u0000\u0000\u0290\u028e\u0001"
        + "\u0000\u0000\u0000\u0290\u0291\u0001\u0000\u0000\u0000\u0291_\u0001\u0000"
        + "\u0000\u0000\u0292\u0290\u0001\u0000\u0000\u0000\u0293\u029c\u0003d2\u0000"
        + "\u0294\u0298\u0003b1\u0000\u0295\u0298\u0005T\u0000\u0000\u0296\u0298"
        + "\u0005\n\u0000\u0000\u0297\u0294\u0001\u0000\u0000\u0000\u0297\u0295\u0001"
        + "\u0000\u0000\u0000\u0297\u0296\u0001\u0000\u0000\u0000\u0298\u0299\u0001"
        + "\u0000\u0000\u0000\u0299\u029b\u0003d2\u0000\u029a\u0297\u0001\u0000\u0000"
        + "\u0000\u029b\u029e\u0001\u0000\u0000\u0000\u029c\u029a\u0001\u0000\u0000"
        + "\u0000\u029c\u029d\u0001\u0000\u0000\u0000\u029da\u0001\u0000\u0000\u0000"
        + "\u029e\u029c\u0001\u0000\u0000\u0000\u029f\u02a0\u00041\u0004\u0000\u02a0"
        + "\u02a1\u0005Z\u0000\u0000\u02a1c\u0001\u0000\u0000\u0000\u02a2\u02a3\u0007"
        + "\b\u0000\u0000\u02a3\u02a6\u0003d2\u0000\u02a4\u02a6\u0003f3\u0000\u02a5"
        + "\u02a2\u0001\u0000\u0000\u0000\u02a5\u02a4\u0001\u0000\u0000\u0000\u02a6"
        + "e\u0001\u0000\u0000\u0000\u02a7\u02b0\u0003h4\u0000\u02a8\u02a9\u0005"
        + "\t\u0000\u0000\u02a9\u02af\u0003l6\u0000\u02aa\u02ab\u0005\u0005\u0000"
        + "\u0000\u02ab\u02ac\u0003T*\u0000\u02ac\u02ad\u0005\u0006\u0000\u0000\u02ad"
        + "\u02af\u0001\u0000\u0000\u0000\u02ae\u02a8\u0001\u0000\u0000\u0000\u02ae"
        + "\u02aa\u0001\u0000\u0000\u0000\u02af\u02b2\u0001\u0000\u0000\u0000\u02b0"
        + "\u02ae\u0001\u0000\u0000\u0000\u02b0\u02b1\u0001\u0000\u0000\u0000\u02b1"
        + "g\u0001\u0000\u0000\u0000\u02b2\u02b0\u0001\u0000\u0000\u0000\u02b3\u02db"
        + "\u0005Y\u0000\u0000\u02b4\u02db\u0005[\u0000\u0000\u02b5\u02db\u0005\u0004"
        + "\u0000\u0000\u02b6\u02db\u0005\u0013\u0000\u0000\u02b7\u02db\u0005\u0014"
        + "\u0000\u0000\u02b8\u02b9\u0005V\u0000\u0000\u02b9\u02ba\u0003T*\u0000"
        + "\u02ba\u02bb\u0005W\u0000\u0000\u02bb\u02db\u0001\u0000\u0000\u0000\u02bc"
        + "\u02c8\u0005\u0005\u0000\u0000\u02bd\u02c2\u0003T*\u0000\u02be\u02bf\u0005"
        + "R\u0000\u0000\u02bf\u02c1\u0003T*\u0000\u02c0\u02be\u0001\u0000\u0000"
        + "\u0000\u02c1\u02c4\u0001\u0000\u0000\u0000\u02c2\u02c0\u0001\u0000\u0000"
        + "\u0000\u02c2\u02c3\u0001\u0000\u0000\u0000\u02c3\u02c6\u0001\u0000\u0000"
        + "\u0000\u02c4\u02c2\u0001\u0000\u0000\u0000\u02c5\u02c7\u0005R\u0000\u0000"
        + "\u02c6\u02c5\u0001\u0000\u0000\u0000\u02c6\u02c7\u0001\u0000\u0000\u0000"
        + "\u02c7\u02c9\u0001\u0000\u0000\u0000\u02c8\u02bd\u0001\u0000\u0000\u0000"
        + "\u02c8\u02c9\u0001\u0000\u0000\u0000\u02c9\u02ca\u0001\u0000\u0000\u0000"
        + "\u02ca\u02db\u0005\u0006\u0000\u0000\u02cb\u02d7\u0005\u0007\u0000\u0000"
        + "\u02cc\u02d1\u0003j5\u0000\u02cd\u02ce\u0005R\u0000\u0000\u02ce\u02d0"
        + "\u0003j5\u0000\u02cf\u02cd\u0001\u0000\u0000\u0000\u02d0\u02d3\u0001\u0000"
        + "\u0000\u0000\u02d1\u02cf\u0001\u0000\u0000\u0000\u02d1\u02d2\u0001\u0000"
        + "\u0000\u0000\u02d2\u02d5\u0001\u0000\u0000\u0000\u02d3\u02d1\u0001\u0000"
        + "\u0000\u0000\u02d4\u02d6\u0005R\u0000\u0000\u02d5\u02d4\u0001\u0000\u0000"
        + "\u0000\u02d5\u02d6\u0001\u0000\u0000\u0000\u02d6\u02d8\u0001\u0000\u0000"
        + "\u0000\u02d7\u02cc\u0001\u0000\u0000\u0000\u02d7\u02d8\u0001\u0000\u0000"
        + "\u0000\u02d8\u02d9\u0001\u0000\u0000\u0000\u02d9\u02db\u0005\b\u0000\u0000"
        + "\u02da\u02b3\u0001\u0000\u0000\u0000\u02da\u02b4\u0001\u0000\u0000\u0000"
        + "\u02da\u02b5\u0001\u0000\u0000\u0000\u02da\u02b6\u0001\u0000\u0000\u0000"
        + "\u02da\u02b7\u0001\u0000\u0000\u0000\u02da\u02b8\u0001\u0000\u0000\u0000"
        + "\u02da\u02bc\u0001\u0000\u0000\u0000\u02da\u02cb\u0001\u0000\u0000\u0000"
        + "\u02dbi\u0001\u0000\u0000\u0000\u02dc\u02dd\u0003l6\u0000\u02dd\u02de"
        + "\u0005S\u0000\u0000\u02de\u02df\u0003T*\u0000\u02dfk\u0001\u0000\u0000"
        + "\u0000\u02e0\u02e1\u0007\t\u0000\u0000\u02e1m\u0001\u0000\u0000\u0000"
        + "\u02e2\u02e3\u0005[\u0000\u0000\u02e3o\u0001\u0000\u0000\u0000\u02e4\u02e5"
        + "\u0005Y\u0000\u0000\u02e5q\u0001\u0000\u0000\u0000msx~\u0093\u0096\u0099"
        + "\u009d\u00a3\u00a6\u00ab\u00b5\u00b9\u00be\u00c2\u00c7\u00cc\u00d1\u00d3"
        + "\u00d7\u00dd\u00e1\u00e5\u00e8\u00ec\u00f1\u00f6\u00f9\u00fb\u00ff\u0102"
        + "\u0106\u0109\u010e\u0111\u0116\u0119\u011b\u0126\u012a\u012d\u0131\u0135"
        + "\u0138\u013f\u0146\u0149\u0151\u0154\u015c\u0160\u0164\u0166\u0168\u016a"
        + "\u016d\u0174\u0178\u017f\u0183\u0189\u0194\u0197\u019a\u01a2\u01a4\u01ae"
        + "\u01b6\u01b9\u01c2\u01c7\u01d3\u01d9\u01e7\u01ec\u01fe\u0208\u020b\u020e"
        + "\u0212\u0216\u021e\u0220\u0222\u022a\u022c\u0238\u023c\u023f\u0242\u024b"
        + "\u0269\u026b\u0275\u027d\u0283\u0289\u0290\u0297\u029c\u02a5\u02ae\u02b0"
        + "\u02c2\u02c6\u02c8\u02d1\u02d5\u02d7\u02da";
    public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
