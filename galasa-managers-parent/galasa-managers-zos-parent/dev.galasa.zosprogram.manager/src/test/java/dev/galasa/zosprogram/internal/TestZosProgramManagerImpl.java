/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.internal.ArtifactManagerImpl;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramException;
import dev.galasa.zosprogram.ZosProgramManagerException;
import dev.galasa.zosprogram.internal.properties.CICSDatasetPrefix;
import dev.galasa.zosprogram.internal.properties.LanguageEnvironmentDatasetPrefix;
import dev.galasa.zosprogram.internal.properties.ProgramLanguageCompileSyslibs;
import dev.galasa.zosprogram.internal.properties.ProgramLanguageDatasetPrefix;
import dev.galasa.zosprogram.internal.properties.ProgramLanguageLinkSyslibs;
import dev.galasa.zosprogram.internal.properties.ZosProgramPropertiesSingleton;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ProgramLanguageDatasetPrefix.class, LanguageEnvironmentDatasetPrefix.class, ProgramLanguageCompileSyslibs.class, ProgramLanguageLinkSyslibs.class, CICSDatasetPrefix.class})
public class TestZosProgramManagerImpl {
    
    private ZosProgramManagerImpl zosProgramManager;
    
    private ZosProgramManagerImpl zosProgramManagerSpy;
    
    private ZosProgramPropertiesSingleton zosProgramZosProgramPropertiesSingleton;

    private List<IManager> allManagers;
    
    private List<IManager> activeManagers;
    
    @Mock
    private IFramework frameworkMock;
    
    @Mock
    private IResultArchiveStore resultArchiveStoreMock;
    
    @Mock
    public IManager managerMock;
    
    @Mock
    private ZosManagerImpl zosManagerMock;
    
    @Mock
    private IZosBatchSpi zosBatchManagerMock;
    
    @Mock
    private IZosFileSpi zosFileManagerMock;
    
    @Mock
    private ArtifactManagerImpl artifactManagerMock;
    
    @Mock
    private IBundleResources bundleResourcesMock;
    
    @Mock
    private IZosBatch zosBatchMock;

    @Mock
    private IZosBatchJob zosBatchJobMock;

    @Mock
    private IZosImage zosImageMock;

    @Mock
    private ZosProgramImpl zosProgramMock;

    @Mock
    private IZosDataset loadlibMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String IMAGE = "image";

    private static final String NAME = "NAME";

    private static final String SOURCE = "PROGRAM-SOURCE";

    private static final String JCL = "JCL";

    @Before
    public void setup() throws Exception {
        ZosProgramManagerImpl.setArtifactManager(artifactManagerMock);
        PowerMockito.doReturn(bundleResourcesMock).when(artifactManagerMock).getBundleResources(Mockito.any());
        
        ZosProgramManagerImpl.setZosManager(zosManagerMock);
        zosProgramZosProgramPropertiesSingleton = new ZosProgramPropertiesSingleton();
        zosProgramZosProgramPropertiesSingleton.activate();
        
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        
        zosProgramManager = new ZosProgramManagerImpl();
        zosProgramManagerSpy = Mockito.spy(zosProgramManager);
        Mockito.when(zosProgramManagerSpy.getFramework()).thenReturn(frameworkMock);
        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(new File("/").toPath());
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }
    
    @Test
    public void testInitialise() throws ManagerException {
        Mockito.doNothing().when(zosProgramManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        GalasaTest galasaTestMock = Mockito.mock(GalasaTest.class);
        Mockito.doReturn(false).when(galasaTestMock).isJava();
        zosProgramManagerSpy.initialise(frameworkMock, allManagers, activeManagers, galasaTestMock);
        Assert.assertEquals("Error in initialise() method", zosProgramManagerSpy.getFramework(), frameworkMock);
    }
    
    @Test
    public void testInitialise2() throws ManagerException {
        Mockito.doNothing().when(zosProgramManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        zosProgramManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
        Assert.assertEquals("Error in initialise() method", zosProgramManagerSpy.getFramework(), frameworkMock);
        zosProgramManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(String.class));
        Assert.assertEquals("Error in initialise() method", zosProgramManagerSpy.getFramework(), frameworkMock);
    }

    @Test
    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        zosProgramManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
    }
    
    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(zosProgramManagerSpy, "generateAnnotatedFields", Mockito.any());
        zosProgramManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(zosProgramManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
    }
    
    @Test
    public void testStartOfTestClass() throws Exception {
        Mockito.clearInvocations(artifactManagerMock);
        zosProgramManagerSpy.startOfTestClass();
        PowerMockito.verifyPrivate(artifactManagerMock, Mockito.times(2)).invoke("getBundleResources", Mockito.any());
        
        Mockito.clearInvocations(artifactManagerMock);
        LinkedHashMap<String, ZosProgramImpl> zosPrograms = new LinkedHashMap<>();
        zosPrograms.put("DUMMY", zosProgramMock);
        Whitebox.setInternalState(zosProgramManagerSpy, "zosPrograms", zosPrograms);
        PowerMockito.doReturn(zosProgramMock).when(zosProgramManagerSpy, "compile", Mockito.any());
        zosProgramManagerSpy.startOfTestClass();
        PowerMockito.verifyPrivate(artifactManagerMock, Mockito.times(2)).invoke("getBundleResources", Mockito.any());
    }
    
    @Test
    public void testYouAreRequired() throws Exception {
        allManagers.add(zosManagerMock);
        allManagers.add(new DummyBatch());
        allManagers.add(new DummyFile());
        allManagers.add(artifactManagerMock);
        zosProgramManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosProgramManagerSpy, Mockito.times(4)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosProgramManagerSpy);
        zosProgramManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosProgramManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
    }
    
    @Test
    public void testYouAreRequiredException1() throws ManagerException {
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("The zOS Manager is not available");
        zosProgramManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException2() throws ManagerException {
        allManagers.add(zosManagerMock);
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("The zOS Batch Manager is not available");
        zosProgramManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException3() throws ManagerException {
        allManagers.add(zosManagerMock);
        allManagers.add(new DummyBatch());
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("The zOS File Manager is not available");
        zosProgramManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException4() throws ManagerException {
        allManagers.add(zosManagerMock);
        allManagers.add(new DummyBatch());
        allManagers.add(new DummyFile());
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("The Artifact Manager is not available");
        zosProgramManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testAreYouProvisionalDependentOn() {
        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosProgramManager.areYouProvisionalDependentOn(zosManagerMock));
        Assert.assertFalse("Should not be dependent on IManager" , zosProgramManager.areYouProvisionalDependentOn(managerMock));
    }
    
    @Test
    public void testGenerateZosProgram() throws ZosManagerException, NoSuchFieldException, SecurityException {
        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
        
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosprogram.ZosProgram.class);
        annotations.add(annotation);
        
        Object zosProgramImplObject = zosProgramManager.generateZosProgram(DummyTestClass.class.getDeclaredField("zosProgram"), annotations);
        Assert.assertTrue("Error in generateZosProgram() method", zosProgramImplObject instanceof ZosProgramImpl);
    }
    
    @Test
    public void testNewZosProgram() throws ZosProgramManagerException {
        IZosProgram zosProgram = zosProgramManagerSpy.newZosProgram(zosImageMock, NAME, SOURCE, Language.COBOL, false, null);
        Assert.assertNotNull("newZosProgram() should not be null", zosProgram);
    }
    
    @Test
    public void testCompile() throws ZosProgramManagerException, ZosBatchException, IOException {
        Mockito.when(zosProgramMock.getLanguage()).thenReturn(Language.COBOL);
        Mockito.when(zosProgramMock.getLoadlib()).thenReturn(loadlibMock);
        Mockito.when(zosProgramMock.getImage()).thenReturn(zosImageMock);
        Mockito.when(zosProgramMock.getName()).thenReturn(NAME);
        PowerMockito.mockStatic(ProgramLanguageDatasetPrefix.class);
        Mockito.when(ProgramLanguageDatasetPrefix.get(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());
        PowerMockito.mockStatic(LanguageEnvironmentDatasetPrefix.class);
        Mockito.when(LanguageEnvironmentDatasetPrefix.get(Mockito.any())).thenReturn(Arrays.asList());
        PowerMockito.mockStatic(ProgramLanguageCompileSyslibs.class);
        Mockito.when(ProgramLanguageCompileSyslibs.get(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());
        PowerMockito.mockStatic(ProgramLanguageLinkSyslibs.class);
        Mockito.when(ProgramLanguageLinkSyslibs.get(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());
        PowerMockito.mockStatic(CICSDatasetPrefix.class);
        Mockito.when(CICSDatasetPrefix.get(Mockito.any())).thenReturn(Arrays.asList());
        Mockito.when(bundleResourcesMock.streamAsString(Mockito.any())).thenReturn(JCL);
        ZosProgramManagerImpl.setManagerBundleResources(bundleResourcesMock);
        ZosProgramManagerImpl.setTestBundleResources(bundleResourcesMock);
        Mockito.when(zosBatchManagerMock.getZosBatch(Mockito.any())).thenReturn(zosBatchMock);
        Mockito.when(zosBatchJobMock.getRetcode()).thenReturn("CC 0000");
        IZosBatchJobname zosJobnameMock = Mockito.mock(IZosBatchJobname.class);
        Mockito.when(zosJobnameMock.getName()).thenReturn("JOBNAME");
        Mockito.when(zosBatchJobMock.getJobname()).thenReturn(zosJobnameMock);
        Mockito.when(zosBatchJobMock.getJobId()).thenReturn("JOBID");
        Mockito.when(zosBatchMock.submitJob(Mockito.any(), Mockito.isNull())).thenReturn(zosBatchJobMock);
        ZosProgramManagerImpl.setZosBatch(zosBatchManagerMock);
        
        Assert.assertEquals("Error in compile() method", zosProgramMock, zosProgramManagerSpy.compile(zosProgramMock));

        Mockito.when(zosProgramMock.getLanguage()).thenReturn(Language.INVALID);
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("Invalid program language: " + Language.INVALID);
        zosProgramManagerSpy.compile(zosProgramMock);
    }
    
    @Test
    public void testGetTestBundleResources() {
        ZosProgramManagerImpl.setTestBundleResources(bundleResourcesMock);
        Assert.assertEquals("Error in getTestBundleResources() method", bundleResourcesMock, ZosProgramManagerImpl.getTestBundleResources());
    }
    
    @Test
    public void testGetRunLoadlib() throws ZosManagerException {
        ZosProgramManagerImpl.setZosFile(zosFileManagerMock);
        IZosFileHandler zOSFileHandlerMock = Mockito.mock(IZosFileHandler.class);
        Mockito.when(zOSFileHandlerMock.newDataset(Mockito.any(), Mockito.any())).thenReturn(loadlibMock);
        Mockito.when(zosFileManagerMock.getZosFileHandler()).thenReturn(zOSFileHandlerMock);
        Mockito.when(zosManagerMock.getRunDatasetHLQ(Mockito.any())).thenReturn("HLQ");
        Assert.assertEquals("Error in getRunLoadlib() method", loadlibMock, ZosProgramManagerImpl.getRunLoadlib(zosImageMock));

        Assert.assertEquals("Error in getRunLoadlib() method", loadlibMock, ZosProgramManagerImpl.getRunLoadlib(zosImageMock));

        Whitebox.setInternalState(ZosProgramManagerImpl.class, "runLoadlib", (IZosDataset) null);
        Mockito.when(loadlibMock.exists()).thenReturn(true);
        Assert.assertEquals("Error in getRunLoadlib() method", loadlibMock, ZosProgramManagerImpl.getRunLoadlib(zosImageMock));

        Whitebox.setInternalState(ZosProgramManagerImpl.class, "runLoadlib", (IZosDataset) null);
        Mockito.when(loadlibMock.exists()).thenThrow(new ZosDatasetException("EXCEPTION"));
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("EXCEPTION");
        ZosProgramManagerImpl.getRunLoadlib(zosImageMock);
    }

    class DummyTestClass {
        @dev.galasa.zosprogram.ZosProgram(imageTag="tag", 
                                          language = dev.galasa.zosprogram.ZosProgram.Language.COBOL, 
                                          name = "DUMMYPGM")
        public dev.galasa.zosprogram.IZosProgram zosProgram;
        @dev.galasa.Test
        public void dummyTestMethod() throws ZosProgramException {
            zosProgram.getLoadlib();
        }
    }
    
    class DummyBatch extends AbstractManager implements IZosBatchSpi {
        @Override
        public @NotNull IZosBatch getZosBatch(@NotNull IZosImage image) {
            return null;
        }        
    }
    
    class DummyFile extends AbstractManager implements IZosFileSpi {
        @Override
        public @NotNull IZosFileHandler getZosFileHandler() throws ZosFileManagerException {
            return null;
        }        
    }
}