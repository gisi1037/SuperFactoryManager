package ca.teamdman.sfm.audit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraft.launchwrapper.LaunchClassLoader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Registry-backed native fluid API tests isolated from the headless fixtures. */
class SfmlFluidTransferTest {

    @Test
    void completeJournalSizeRefusesBeforeAnyActualFluidCallback() throws Throwable {
        invoke("completeJournalSizeRefusesBeforeAnyActualFluidCallback");
    }

    @Test
    void journalPreventsReentrantTransfersDuringActualDrain() throws Throwable {
        invoke("journalPreventsReentrantTransfersDuringActualDrain");
    }

    @Test
    void diskReplacementAfterExtractionRetainsEvidenceOnOriginalDisk() throws Throwable {
        invoke("diskReplacementAfterExtractionRetainsEvidenceOnOriginalDisk");
    }

    @Test
    void throwingRollbackRetainsOnlyTheUncertainRemainder() throws Throwable {
        invoke("throwingRollbackRetainsOnlyTheUncertainRemainder");
    }

    @Test
    void mixedFluidsAndNbtAreConservedAndAmountIsAggregate() throws Throwable {
        invoke("mixedFluidsAndNbtAreConservedAndAmountIsAggregate");
    }

    @Test
    void capturedFluidKeyTransfersOnlyTheExactNbtVariant() throws Throwable {
        invoke("capturedFluidKeyTransfersOnlyTheExactNbtVariant");
    }

    @Test
    void fullSourceGroupMustBeAvailableAndUnloadedTilesAreNeverRead() throws Throwable {
        invoke("fullSourceGroupMustBeAvailableAndUnloadedTilesAreNeverRead");
    }

    @Test
    void providerBudgetConsumptionCannotSpendCommitRecoveryCredit() throws Throwable {
        invoke("providerBudgetConsumptionCannotSpendCommitRecoveryCredit");
    }

    @Test
    void nonblockingReadinessFunctionAcceptsExplicitLabelParameters() throws Throwable {
        invoke("nonblockingReadinessFunctionAcceptsExplicitLabelParameters");
    }

    private static LaunchClassLoader isolated;
    private static Class<?> fixture;

    @BeforeAll
    static void openIsolatedForgeRuntime() throws Throwable {
        Set<URL> sources = new LinkedHashSet<>();
        for (String entry : System.getProperty("java.class.path")
            .split(Pattern.quote(File.pathSeparator)))
            if (!entry.isEmpty()) sources.add(
                new File(entry).toURI()
                    .toURL());
        for (ClassLoader loader = SfmlFluidTransferTest.class.getClassLoader(); loader
            != null; loader = loader.getParent())
            if (loader instanceof URLClassLoader)
                for (URL source : ((URLClassLoader) loader).getURLs()) sources.add(source);
        for (ClassLoader loader = Thread.currentThread()
            .getContextClassLoader(); loader != null; loader = loader.getParent())
            if (loader instanceof URLClassLoader)
                for (URL source : ((URLClassLoader) loader).getURLs()) sources.add(source);
        isolated = new LaunchClassLoader(sources.toArray(new URL[0]));
        // Share assertion/platform classes only. Minecraft, Forge and the fixture stay in the child runtime.
        isolated.addClassLoaderExclusion("javax.");
        isolated.addClassLoaderExclusion("jdk.");
        isolated.addClassLoaderExclusion("org.junit.");
        isolated.addClassLoaderExclusion("org.opentest4j.");
        isolated.addClassLoaderExclusion("org.apiguardian.");
        fixture = Class.forName("ca.teamdman.sfm.audit.SfmlFluidTransferFixture", true, isolated);
        if (fixture.getClassLoader() != isolated) throw new AssertionError("Ore fixture must use the isolated runtime");
        invoke("registerFixtures");
    }

    @AfterAll
    static void closeIsolatedForgeRuntime() throws Exception {
        fixture = null;
        if (isolated != null) isolated.close();
        isolated = null;
    }

    private static void invoke(String method) throws Throwable {
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(isolated);
        try {
            fixture.getMethod(method)
                .invoke(null);
        } catch (InvocationTargetException failure) {
            throw failure.getCause();
        } finally {
            thread.setContextClassLoader(previous);
        }
    }

    @Test
    void allFourFormsTransferInMillibuckets() throws Throwable {
        invoke("allFourFormsTransferInMillibuckets");
    }

    @Test
    void resultRecordsPartialCapacityAndFiniteRequestedStock() throws Throwable {
        invoke("resultRecordsPartialCapacityAndFiniteRequestedStock");
    }

    @Test
    void queriesCountOnePhysicalTankAndLeaveItemQueriesUnchanged() throws Throwable {
        invoke("queriesCountOnePhysicalTankAndLeaveItemQueriesUnchanged");
    }

    @Test
    void missingGroupIsUnavailableRatherThanEmpty() throws Throwable {
        invoke("missingGroupIsUnavailableRatherThanEmpty");
    }

    @Test
    void slotsAreRejectedAndZeroAndNegativeAmountsCallNoProviders() throws Throwable {
        invoke("slotsAreRejectedAndZeroAndNegativeAmountsCallNoProviders");
    }

    @Test
    void equalGroupLabelsCannotFeedTheirOwnSources() throws Throwable {
        invoke("equalGroupLabelsCannotFeedTheirOwnSources");
    }

    @Test
    void qualifiedSideResolvesAmbiguousTankViews() throws Throwable {
        invoke("qualifiedSideResolvesAmbiguousTankViews");
    }

    @Test
    void sidePermissionsAreHonored() throws Throwable {
        invoke("sidePermissionsAreHonored");
    }

    @Test
    void defaultFluidQueriesWorkInFunctionsLoopsAndExpressions() throws Throwable {
        invoke("defaultFluidQueriesWorkInFunctionsLoopsAndExpressions");
    }

    @Test
    void fluidKeyEqualityIncludesNbtAndSnapshotsRemainImmutable() throws Throwable {
        invoke("fluidKeyEqualityIncludesNbtAndSnapshotsRemainImmutable");
    }

    @Test
    void exactFluidKeyRejectsDifferentNbt() throws Throwable {
        invoke("exactFluidKeyRejectsDifferentNbt");
    }

    @Test
    void automaticAmountRemainsFiniteWhenSourceRefills() throws Throwable {
        invoke("automaticAmountRemainsFiniteWhenSourceRefills");
    }

    @Test
    void workExhaustionFailsBeforeExtraction() throws Throwable {
        invoke("workExhaustionFailsBeforeExtraction");
    }

    @Test
    void throwingExtractionPreservesUncertainEvidence() throws Throwable {
        invoke("throwingExtractionPreservesUncertainEvidence");
    }

    @Test
    void throwingInsertionNeverBlindlyRollsBackPossiblyInsertedFluid() throws Throwable {
        invoke("throwingInsertionNeverBlindlyRollsBackPossiblyInsertedFluid");
    }

    @Test
    void knownUnderfillReturnsTheRemainder() throws Throwable {
        invoke("knownUnderfillReturnsTheRemainder");
    }

    @Test
    void rollbackRefusalRetainsKnownOwnedFluid() throws Throwable {
        invoke("rollbackRefusalRetainsKnownOwnedFluid");
    }

    @Test
    void callbackReplacementDoesNotAuthorizeExtractionFromStaleProvider() throws Throwable {
        invoke("callbackReplacementDoesNotAuthorizeExtractionFromStaleProvider");
    }

    @Test
    void simulationDiskReplacementStopsBeforeExtraction() throws Throwable {
        invoke("simulationDiskReplacementStopsBeforeExtraction");
    }

    @Test
    void itemAndFluidActionsShareOneLimit() throws Throwable {
        invoke("itemAndFluidActionsShareOneLimit");
    }

    @Test
    void studioPreservesFluidKeywordAndArgumentSpans() throws Throwable {
        invoke("studioPreservesFluidKeywordAndArgumentSpans");
    }

    @Test
    void invalidOverloadsAreRejectedAndOldItemFormsStayValid() throws Throwable {
        invoke("invalidOverloadsAreRejectedAndOldItemFormsStayValid");
    }
}
