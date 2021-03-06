import static com.google.dart.compiler.SystemLibraryManager.DEFAULT_PLATFORM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jdart.compiler.cha.ClassHierarchyAnalysisPhase;
import jdart.compiler.flow.InterProceduralMethodCallResolver;
import jdart.compiler.flow.TypeHelper;
import jdart.compiler.gen.Gen;
import jdart.compiler.type.CoreTypeRepository;
import jdart.compiler.type.Type;
import jdart.compiler.type.TypeRepository;

import org.kohsuke.args4j.CmdLineException;

import com.google.dart.compiler.CommandLineOptions;
import com.google.dart.compiler.CommandLineOptions.CompilerOptions;
import com.google.dart.compiler.CompilerConfiguration;
import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompiler;
import com.google.dart.compiler.DefaultCompilerConfiguration;
import com.google.dart.compiler.SystemLibraryManager;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.resolver.CompileTimeConstantAnalyzer;
import com.google.dart.compiler.resolver.MethodNodeElement;
import com.google.dart.compiler.resolver.Resolver;

public class Main {
  public static void compile(File sourceFile, String sdkPath) throws IOException {
    File sdkFile = new File(sdkPath);

    CompilerOptions compilerOptions = new CompilerOptions();
    SystemLibraryManager libraryManager = new SystemLibraryManager(sdkFile, DEFAULT_PLATFORM);

    String[] options = { "--dart-sdk", sdkPath };
    try {
      CommandLineOptions.parse(options, compilerOptions);
    } catch (CmdLineException e) {
      e.printStackTrace();
    }

    final ClassHierarchyAnalysisPhase chaInstance = ClassHierarchyAnalysisPhase.getInstance();
    
    CompilerConfiguration config = new DefaultCompilerConfiguration(compilerOptions, libraryManager) {
      @Override
      public List<DartCompilationPhase> getPhases() {
        List<DartCompilationPhase> phases = new ArrayList<>();
        phases.add(new CompileTimeConstantAnalyzer.Phase());
        phases.add(new Resolver.Phase());
        
        phases.add(chaInstance);
        return phases;
      }
    };

    boolean result = DartCompiler.compilerMain(sourceFile, config);
    if (result == false) {
      System.err.println("an error occured !");
      return;
    }
    
    Set<DartUnit> units = chaInstance.getUnits();
    DartUnit mainUnit = units.iterator().next();
    MethodNodeElement mainMethod = (MethodNodeElement)mainUnit.getLibrary().getElement().getEntryPoint();
    if (mainMethod == null) {
      System.err.println("unit "+mainUnit.getSourceName()+" has no entry point");
      return;
    }
    
     // initialize core type repository
    CoreTypeRepository coreTypeRepository = CoreTypeRepository.initCoreTypeRepository(chaInstance.getCoreTypeProvider());
    TypeRepository typeRepository = new TypeRepository(coreTypeRepository);
    TypeHelper typeHelper = new TypeHelper(typeRepository);
    
    // type flow starting with main method
    InterProceduralMethodCallResolver methodCallResolver = new InterProceduralMethodCallResolver(typeHelper);
    methodCallResolver.functionCall(mainMethod, Collections.<Type>emptyList(), CoreTypeRepository.VOID_TYPE);
    
    Gen.genAll(mainMethod, methodCallResolver.getMethodMap());
  }

  public static void main(String[] args) throws IOException {
    String sdkPath = "../../dart-sdk/";

    String[] paths = { 
         "DartTest/Fibo.dart"
        // "DartTest/Mandelbrot.dart",
        // "DartTest/For2.dart",
        // "DartTest/Hello.dart"
    };

    for (String path : paths) {
      File sourceFile = new File(path);
      compile(sourceFile, sdkPath);
    }
  }
}
