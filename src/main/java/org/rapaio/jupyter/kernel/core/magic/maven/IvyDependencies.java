package org.rapaio.jupyter.kernel.core.magic.maven;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.rapaio.jupyter.kernel.core.display.text.ANSIText;

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

    public static List<ArtifactDownloadReport> resolve(DepCoordinates dependency) throws ParseException, IOException {

        // create an ivy instance
        IvySettings ivySettings = new IvySettings();
        String kernelPath = IvyDependencies.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File kernelRoot = new File(kernelPath.substring(0, kernelPath.lastIndexOf('/')));
        ivySettings.setDefaultCache(new File(kernelRoot, "ivy_cache"));

        DependencyResolver dependencyResolver = publicResolver();

        ivySettings.addResolver(dependencyResolver);
        ivySettings.setDefaultResolver(dependencyResolver.getName());

        Ivy ivy = Ivy.newInstance(ivySettings);

        // Step 1: you always need to resolve before you can retrieve
        //
        ResolveOptions ro = new ResolveOptions();
        // this seems to have no impact, if you resolve by module descriptor (in contrast to resolve by ModuleRevisionId)
        ro.setTransitive(true);
        // if set to false, nothing will be downloaded
        ro.setDownload(true);


        // 1st create an ivy module (this always(!) has a "default" configuration already)
        ModuleRevisionId ri = dependency.revisionId();
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newCallerInstance(ri, DEFAULT_MODULE_DESC_CONFIGS, true, true);

        // now resolve
        ResolveReport rr = ivy.resolve(md, ro);
        if (rr.hasError()) {
            throw new RuntimeException(rr.getAllProblemMessages().toString());
        }

        return Arrays.asList(rr.getAllArtifactsReports());
    }

    public static void main(String[] args) throws Exception {

        DepCoordinates rapaio = new DepCoordinates("io.github.padreati:rapaio-core:5.0.0");
        DepCoordinates spring = new DepCoordinates("org.springframework:spring-context-support:4.0.2.RELEASE");

        System.out.println("resolve rapaio:");
        for(var adr : resolve(rapaio)) {
            System.out.println(ANSIText.start().bold().fgBlue().text(adr.getLocalFile().getAbsolutePath()).reset().build());
        }

        System.out.println("solve spring:");
        for(var adr : resolve(spring)) {
            System.out.println(ANSIText.start().bold().fgBlue().text(adr.getLocalFile().getAbsolutePath()).reset().build());
        }
    }

}
