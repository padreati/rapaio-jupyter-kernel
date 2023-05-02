package org.rapaio.jupyter.kernel.core.magic.maven;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.MessageLogger;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;

public class IvyDependencies {

    public static DependencyResolver publicResolver() {
        ChainResolver chainResolver = new ChainResolver();
        chainResolver.setName("public");
        chainResolver.add(maven("central", "https://repo.maven.apache.org/maven2/"));
        chainResolver.add(maven("jcenter", "https://jcenter.bintray.com/"));
        chainResolver.add(maven("jboss", "https://repository.jboss.org/nexus/content/repositories/releases/"));
        chainResolver.add(maven("atlassian", "https://packages.atlassian.com/maven/public"));
        return chainResolver;
    }

    private static IBiblioResolver maven(String name, String urlRaw) {
        IBiblioResolver resolver = new IBiblioResolver();
        resolver.setM2compatible(true);
        resolver.setUseMavenMetadata(true);
        resolver.setUsepoms(true);

        resolver.setRoot(urlRaw);
        resolver.setName(name);

        return resolver;
    }

    private static final String[] DEFAULT_MODULE_DESC_CONFIGS = new String[] {"default", "master", "runtime"};
    private static IvyDependencies ivyDependencies;

    private final Ivy ivy;

    public static IvyDependencies getInstance() {
        if (ivyDependencies == null) {
            ivyDependencies = new IvyDependencies();
        }
        return ivyDependencies;
    }

    private IvyDependencies() {
        // create an ivy instance
        IvySettings ivySettings = new IvySettings();
        String kernelPath = IvyDependencies.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File kernelRoot = new File(kernelPath.substring(0, kernelPath.lastIndexOf('/')));
        ivySettings.setDefaultCache(new File(kernelRoot, "ivy_cache"));

        DependencyResolver dependencyResolver = publicResolver();

        ivySettings.addResolver(dependencyResolver);
        ivySettings.setDefaultResolver(dependencyResolver.getName());


        MessageLogger ivyLogger = new DefaultMessageLogger(Message.MSG_WARN);

        ivy = Ivy.newInstance(ivySettings);
        ivy.getLoggerEngine().setDefaultLogger(ivyLogger);
    }

    public ResolveReport resolve(DepCoordinates dependency) throws ParseException, IOException {

        ResolveOptions ro = new ResolveOptions();
        ro.setTransitive(true);
        ro.setDownload(true);

        // 1st create an ivy module (this always(!) has a "default" configuration already)
        ModuleRevisionId ri = dependency.revisionId();
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newCallerInstance(ri, DEFAULT_MODULE_DESC_CONFIGS, true, true);

        return IvyDependencies.getInstance().ivy.resolve(md, ro);
    }

    public static void main(String[] args) throws Exception {

        DepCoordinates rapaio = new DepCoordinates("io.github.padreati:rapaio-core:5.0.0");
        DepCoordinates spring = new DepCoordinates("org.springframework:spring-context-support:4.0.2.RELEASE");

        System.out.println("resolve rapaio:");
        ResolveReport rr = IvyDependencies.getInstance().resolve(rapaio);
        if (rr.hasError()) {
            throw new RuntimeException(rr.getAllProblemMessages().toString());
        }

        for (var adr : rr.getAllArtifactsReports()) {
            System.out.println(ANSI.start().bold().fgBlue().text(adr.getLocalFile().getAbsolutePath()).reset().build());
        }

        System.out.println("solve spring:");
        rr = IvyDependencies.getInstance().resolve(spring);
        for (var adr : rr.getAllArtifactsReports()) {
            System.out.println(ANSI.start().bold().fgBlue().text(adr.getLocalFile().getAbsolutePath()).reset().build());
        }
    }

}
