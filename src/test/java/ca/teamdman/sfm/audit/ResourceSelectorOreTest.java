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

/** Real Forge membership tests, isolated from the ordinary headless suite's registries and Loader singleton. */
class ResourceSelectorOreTest {

    @Test
    void studioAbsentIoNumbersKeepRealItemDefaultsZeroReserveCapAndConservation() throws Throwable {
        invoke("studioAbsentIoNumbersKeepRealItemDefaultsZeroReserveCapAndConservation");
    }

    @Test
    void studioAbsentIoNumbersKeepRealFluidMillibucketsAndConservation() throws Throwable {
        invoke("studioAbsentIoNumbersKeepRealFluidMillibucketsAndConservation");
    }

    @Test
    void studioOptionalIoNumbersKeepRealItemReserveTargetEachAndConservation() throws Throwable {
        invoke("studioOptionalIoNumbersKeepRealItemReserveTargetEachAndConservation");
    }

    @Test
    void studioOptionalIoNumbersKeepRealFluidMillibucketsDefaultsZeroAndConservation() throws Throwable {
        invoke("studioOptionalIoNumbersKeepRealFluidMillibucketsDefaultsZeroAndConservation");
    }

    @Test
    void studioSingleLimitPromotionKeepsRealItemMetadataQuotasAndOverlapPriority() throws Throwable {
        invoke("studioSingleLimitPromotionKeepsRealItemMetadataQuotasAndOverlapPriority");
    }

    @Test
    void studioSingleLimitPromotionKeepsFluidDefaultsRetentionOrderAndTwoToOne() throws Throwable {
        invoke("studioSingleLimitPromotionKeepsFluidDefaultsRetentionOrderAndTwoToOne");
    }

    @Test
    void automaticReturnCannotClearReplacementOrReplayRemovedDisk() throws Throwable {
        invoke("automaticReturnCannotClearReplacementOrReplayRemovedDisk");
    }

    @Test
    void automaticReturnFencesAbaDiskReplacement() throws Throwable {
        invoke("automaticReturnFencesAbaDiskReplacement");
    }

    @Test
    void automaticReturnJournalsBeforeReentrantManualExecution() throws Throwable {
        invoke("automaticReturnJournalsBeforeReentrantManualExecution");
    }

    @Test
    void saveInsideThrowingAutomaticReturnRetainsUncertaintyAfterReload() throws Throwable {
        invoke("saveInsideThrowingAutomaticReturnRetainsUncertaintyAfterReload");
    }

    @Test
    void confirmedAutomaticReturnRestoresOnlyKnownRemainderAcrossSaveReload() throws Throwable {
        invoke("confirmedAutomaticReturnRestoresOnlyKnownRemainderAcrossSaveReload");
    }

    @Test
    void automaticReturnRejectsOwnerChangedDuringHandlerLookup() throws Throwable {
        invoke("automaticReturnRejectsOwnerChangedDuringHandlerLookup");
    }

    @Test
    void automaticReturnCannotClearReplacedRecordOnSameDisk() throws Throwable {
        invoke("automaticReturnCannotClearReplacedRecordOnSameDisk");
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
        for (ClassLoader loader = ResourceSelectorOreTest.class.getClassLoader(); loader
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
        fixture = Class.forName("ca.teamdman.sfm.audit.ResourceSelectorOreFixture", true, isolated);
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
    void studioGlobalExclusionsKeepRealItemMetadataAndFluidSemantics() throws Throwable {
        invoke("studioGlobalExclusionsKeepRealItemMetadataAndFluidSemantics");
    }

    @Test
    void studioExclusionRowsRetainRealMetadataAndMixedItemFluidMatching() throws Throwable {
        invoke("studioExclusionRowsRetainRealMetadataAndMixedItemFluidMatching");
    }

    @Test
    void studioResourceAlternativesShareOneLimitAndKeepMixedUnionAndExclusions() throws Throwable {
        invoke("studioResourceAlternativesShareOneLimitAndKeepMixedUnionAndExclusions");
    }

    @Test
    void studioHasRowsKeepRealCountsSetOperatorsDuplicatesAndUnavailableDefaults() throws Throwable {
        invoke("studioHasRowsKeepRealCountsSetOperatorsDuplicatesAndUnavailableDefaults");
    }

    @Test
    void studioIndependentLimitRowsKeepItemAllowancesOreMetadataAndOverlapPriority() throws Throwable {
        invoke("studioIndependentLimitRowsKeepItemAllowancesOreMetadataAndOverlapPriority");
    }

    @Test
    void studioIndependentLimitRowsKeepRealFluidMatchingPriorityRemovalAndConservation() throws Throwable {
        invoke("studioIndependentLimitRowsKeepRealFluidMatchingPriorityRemovalAndConservation");
    }

    @Test
    void studioLegacyOreFiltersKeepRealMembershipCaseUnknownAndFluidSemantics() throws Throwable {
        invoke("studioLegacyOreFiltersKeepRealMembershipCaseUnknownAndFluidSemantics");
    }

    @Test
    void wildcardRegistrationMatchesEveryMetadataWhileExactRegistrationRemainsSpecific() throws Throwable {
        invoke("wildcardRegistrationMatchesEveryMetadataWhileExactRegistrationRemainsSpecific");
    }

    @Test
    void membershipIsExactCaseSensitiveAndCanBelongToSeveralCategories() throws Throwable {
        invoke("membershipIsExactCaseSensitiveAndCanBelongToSeveralCategories");
    }

    @Test
    void unknownNameMatchesNothingWithoutRegisteringIt() throws Throwable {
        invoke("unknownNameMatchesNothingWithoutRegisteringIt");
    }

    @Test
    void previouslyCompiledSelectorSeesLaterCategoryAndMembershipRegistrations() throws Throwable {
        invoke("previouslyCompiledSelectorSeesLaterCategoryAndMembershipRegistrations");
    }

    @Test
    void itemSelectorsRetainRegistryIdentityAndWildcardBehavior() throws Throwable {
        invoke("itemSelectorsRetainRegistryIdentityAndWildcardBehavior");
    }

    @Test
    void oreMembershipWorkAndAllocationShareTheInvocationQueryBudget() throws Throwable {
        invoke("oreMembershipWorkAndAllocationShareTheInvocationQueryBudget");
    }

    @Test
    void itemMatchingDoesNotAddOreMembershipBudgetCharges() throws Throwable {
        invoke("itemMatchingDoesNotAddOreMembershipBudgetCharges");
    }

    @Test
    void oreSelectorsRemainResourceSelectorsAndFormatWithTheirOwnKind() throws Throwable {
        invoke("oreSelectorsRemainResourceSelectorsAndFormatWithTheirOwnKind");
    }

    @Test
    void invalidOreNamesRejectPatternsWhitespacePrefixesUnicodeAndOversizedNames() throws Throwable {
        invoke("invalidOreNamesRejectPatternsWhitespacePrefixesUnicodeAndOversizedNames");
    }

    @Test
    void nativeOreTransferSharesOneQuotaAcrossWildcardAndExactMetadataMembers() throws Throwable {
        invoke("nativeOreTransferSharesOneQuotaAcrossWildcardAndExactMetadataMembers");
    }

    @Test
    void nativeUnknownOreTransferMovesNothingAndDoesNotRegisterACategory() throws Throwable {
        invoke("nativeUnknownOreTransferMovesNothingAndDoesNotRegisterACategory");
    }
}
