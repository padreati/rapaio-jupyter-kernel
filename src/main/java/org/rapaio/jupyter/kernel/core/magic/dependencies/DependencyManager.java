package org.rapaio.jupyter.kernel.core.magic.dependencies;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.OverrideDependencyDescriptorMediator;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.conflict.LatestCompatibleConflictManager;
import org.apache.ivy.plugins.conflict.LatestConflictManager;
import org.apache.ivy.plugins.conflict.NoConflictManager;
import org.apache.ivy.plugins.conflict.StrictConflictManager;
import org.apache.ivy.plugins.latest.LatestRevisionStrategy;
import org.apache.ivy.plugins.latest.LatestStrategy;
import org.apache.ivy.plugins.latest.LatestTimeStrategy;
import org.apache.ivy.plugins.matcher.ExactPatternMatcher;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.MessageLogger;

public class DependencyManager {

    private static final String[] DEFAULT_MODULE_DESC_CONFIGS = new String[] {"default", "master", "runtime"};

    private final Ivy ivy;
    private final DefaultModuleDescriptor md;

    private final ChainResolver resolver = new ChainResolver();

    private final List<Dependency> directDependencies = new ArrayList<>();
    private final List<Dependency> conflictDependencies = new ArrayList<>();

    private final List<ArtifactDownloadReport> loadedArtifacts = new ArrayList<>();

    public DependencyManager() {

        resolver.setName("public");
        resolver.add(maven("central", "https://repo.maven.apache.org/maven2/"));
        resolver.add(maven("jcenter", "https://jcenter.bintray.com/"));
        resolver.add(maven("jboss", "https://repository.jboss.org/nexus/content/repositories/releases/"));
        resolver.add(maven("atlassian", "https://packages.atlassian.com/maven/public"));

        // create an ivy instance
        IvySettings ivySettings = new IvySettings();
        String kernelPath = DependencyManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File kernelRoot = new File(kernelPath.substring(0, kernelPath.lastIndexOf('/')));
        ivySettings.setDefaultCache(new File(kernelRoot, "ivy_cache"));


        ivySettings.addResolver(resolver);
        ivySettings.setDefaultResolver(resolver.getName());

        LatestConflictManager conflictManager = new LatestConflictManager(new LatestRevisionStrategy());
        conflictManager.setSettings(ivySettings);
        ivySettings.setDefaultConflictManager(conflictManager);

        MessageLogger ivyLogger = new DefaultMessageLogger(Message.MSG_WARN);

        ivy = Ivy.newInstance(ivySettings);
        ivy.getLoggerEngine().setDefaultLogger(ivyLogger);

        md = new DefaultModuleDescriptor(
                ModuleRevisionId.newInstance("notebook", "jupyter-kernel", "version"), "integration", null, true);
        for (String conf : DEFAULT_MODULE_DESC_CONFIGS) {
            md.addConfiguration(new Configuration(conf));
        }
   }

    public List<Dependency> getDirectDependencies() {
        return directDependencies;
    }

    public List<Dependency> getConflictDependencies() {
        return conflictDependencies;
    }

    public List<ArtifactDownloadReport> getLoadedArtifacts() {
        return loadedArtifacts;
    }

    public void setAllConflictManager() {
        NoConflictManager conflictManager = new NoConflictManager();
        conflictManager.setSettings(ivy.getSettings());
        ivy.getSettings().setDefaultConflictManager(conflictManager);

    }

    public void setLatestTimeConflictManager() {
        LatestStrategy strategy = new LatestTimeStrategy();
        LatestConflictManager conflictManager = new LatestConflictManager(strategy);
        conflictManager.setSettings(ivy.getSettings());
        ivy.getSettings().setDefaultConflictManager(conflictManager);
    }

    public void setLatestCompatibleConflictManager() {
        LatestCompatibleConflictManager conflictManager = new LatestCompatibleConflictManager();
        conflictManager.setSettings(ivy.getSettings());
        ivy.getSettings().setDefaultConflictManager(conflictManager);
    }

    public void setLatestRevisionConflictManager() {
        LatestStrategy strategy = new LatestRevisionStrategy();
        LatestConflictManager conflictManager = new LatestConflictManager(strategy);
        conflictManager.setSettings(ivy.getSettings());
        ivy.getSettings().setDefaultConflictManager(conflictManager);
    }

    public void setStrictConflictManager() {
        StrictConflictManager conflictManager = new StrictConflictManager();
        conflictManager.setSettings(ivy.getSettings());
        ivy.getSettings().setDefaultConflictManager(conflictManager);
    }

    private IBiblioResolver maven(String name, String urlRaw) {
        IBiblioResolver resolver = new IBiblioResolver();
        resolver.setM2compatible(true);
        resolver.setUseMavenMetadata(true);
        resolver.setUsepoms(true);

        resolver.setRoot(urlRaw);
        resolver.setName(name);

        return resolver;
    }

    public void addDependency(Dependency dependency) {
        directDependencies.add(dependency);
    }

    public void addOverrideDependency(Dependency dependency) {
        conflictDependencies.add(dependency);
    }

    public ResolveReport resolve() throws ParseException, IOException {

        ResolveOptions ro = new ResolveOptions();
        ro.setTransitive(true);
        ro.setDownload(true);

        for (Dependency resolveDependency : conflictDependencies) {
            md.addDependencyDescriptorMediator(resolveDependency.moduleId(), new ExactPatternMatcher(),
                    new OverrideDependencyDescriptorMediator(null, resolveDependency.version()));
        }

        for (Dependency dependency : directDependencies) {
            ModuleRevisionId ri = dependency.revisionId();
            DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, dependency.force(), true, true);
            for (String conf : DEFAULT_MODULE_DESC_CONFIGS) {
                dd.addDependencyConfiguration(conf, conf);
            }
            md.addDependency(dd);
            md.setLastModified(System.currentTimeMillis());
        }

        return ivy.resolve(md, ro);
    }

    public void addLoadedArtifact(ArtifactDownloadReport report) {
        loadedArtifacts.add(report);
    }
}
